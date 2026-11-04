import os
from os.path import dirname, join

import pythonforandroid.recipes.numpy as p4a_numpy
from pythonforandroid.recipes.numpy import NumpyRecipe

# p4a's own numpy patches live next to the built-in recipe. Referencing them by
# bare filename fails, because patch paths are resolved relative to *this*
# recipe's directory -- p4a then looks for
#   flowy/recipes/numpy/patches/remove-default-paths.patch
# and aborts with "Can't open patch file". Resolve them absolutely.
P4A_PATCH_DIR = join(dirname(p4a_numpy.__file__), 'patches')


class NumpyCompatRecipe(NumpyRecipe):
    """p4a's numpy (1.22.3) plus distutils and cross-compile compatibility patches.

    cxx_compiler.patch
    ------------------
    numpy 1.22.3's bundled numpy.distutils calls compiler.cxx_compiler() in
    numpy/core/setup.py:

        config_cmd.compiler = bk_c.cxx_compiler()

    Newer setuptools' distutils no longer exposes that helper:

        AttributeError: 'UnixCCompiler' object has no attribute 'cxx_compiler'

    Downgrading setuptools globally is not an option -- pyzmq's setup.py does
    `from setuptools.errors import OptionError`, which old releases lack -- so
    patch numpy to fall back to the C compiler when the C++ one is missing.
    get_mathlib_info() only needs *a* compiler to probe libm; the extension
    itself is still compiled with the C++ compiler afterwards.

    disable-x86-simd-probe.patch
    ----------------------------
    arm64 builds died while *probing* the compiler, long before any numpy
    source was compiled:

        numpy/core/setup_common.py -> OPTIONAL_HEADERS / OPTIONAL_INTRINSICS
        -> distutils _configtest.c -> #include <xmmintrin.h>

        .../clang/14.0.6/include/xmmintrin.h:14:2: error:
        "This header is only meant to be used on x86 and x64 architecture"

    numpy's probe lists are architecture-agnostic, so a cross build for
    aarch64 still asks the NDK clang to compile the x86 headers. Clang ships
    them for every target and rejects them for non-x86, so the probe cannot
    succeed -- there is nothing the recipe can set to make it pass. Note this
    is *not* what NPY_DISABLE_CPU_FEATURES does: numpy 1.22.3 only parses that
    variable at runtime (npy_cpu_features.c) and it never reaches distutils,
    so setting it in get_recipe_env() would leave the failure untouched.
    """

    name = 'numpy'
    def prebuild_arch(self, arch):
        """Repair the npymath headers before numpy is configured.

        The build then dies at the very first npymath compile with:

            numpy/core/src/npymath/npy_math_internal.h.src:56:10:
            fatal error: 'npy_math_private.h' file not found

        npy_math_private.h is a plain header in numpy's own tree (it is NOT a
        .src template -- upstream numpy has npy_math_private.h at 17 KB and no
        .src siblings for it). It goes missing in the p4a build tree because
        numpy's data-file pass (which copies headers from the source tree into
        build/src.<plat>/) emits nothing under the p4a patches, so whatever was
        not already on the compiler's include path never arrives.

        Rather than reconstruct the header (it is 17 KB of platform-dependent
        #defines we would only get subtly wrong), make the include resolvable:
        copy the existing header into the build/src.<plat>/npymath directory
        that the compile line actually adds with -I. If the header is absent
        from the source tree too, say so loudly instead of failing later with a
        confusing missing-file error at line 56.
        """
        import os
        import shutil
        from os.path import join as _join, isdir, exists
        from pythonforandroid.logger import info

        build_dir = self.get_build_dir(arch.arch)
        npymath = _join(build_dir, 'numpy', 'core', 'src', 'npymath')
        if not isdir(npymath):
            info('numpy: no npymath sources at {}'.format(npymath))
            return

        header = _join(npymath, 'npy_math_private.h')
        if not exists(header):
            # numpy's sdist genuinely ships this file (17 KB, not a template),
            # so if it is missing the unpack step dropped it. Walk the source
            # root once before giving up -- a moved copy is still usable.
            found = None
            for dirpath, _dirnames, filenames in os.walk(build_dir):
                if 'npy_math_private.h' in filenames:
                    found = _join(dirpath, 'npy_math_private.h')
                    break
            if found:
                info('numpy: npy_math_private.h found at {}'.format(found))
                header = found
            else:
                info('numpy: !! npy_math_private.h absent from the unpacked '
                     'numpy tree ({}); the build will fail at '
                     'npy_math_internal.h.src:56'.format(npymath))
                info('numpy: tree listing: {}'.format(sorted(os.listdir(npymath))))
                return

        # -Ibuild/src.<plat>-<pyver>/numpy/core/src/npymath is on the compile
        # line, and that path is relative to the numpy source root (setup.py's
        # cwd). Mirror the header into every build/src* dir that exists or
        # should exist.
        targets = set()
        for entry in os.listdir(build_dir):
            if entry.startswith('build'):
                targets.add(_join(build_dir, entry, 'numpy', 'core', 'src',
                                  'npymath'))
        for out_dir in sorted(targets):
            os.makedirs(out_dir, exist_ok=True)
            dst = _join(out_dir, 'npy_math_private.h')
            if not exists(dst):
                shutil.copyfile(header, dst)
                info('numpy: staged npy_math_private.h -> {}'.format(dst))

    patches = [
        join(P4A_PATCH_DIR, 'remove-default-paths.patch'),
        join(P4A_PATCH_DIR, 'add_libm_explicitly_to_build.patch'),
        join(P4A_PATCH_DIR, 'ranlib.patch'),
        join('patches', 'cxx_compiler.patch'),
        join('patches', 'build_clib_customize.patch'),
        # Must come after build_clib_customize.patch only for readability;
        # it touches numpy/core/setup_common.py, which neither of the other
        # custom patches modifies.
        join('patches', 'disable-x86-simd-probe.patch'),
    ]


recipe = NumpyCompatRecipe()
