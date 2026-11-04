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
        """Materialise the npymath headers before numpy is configured.

        numpy emits npy_math_private.h / npy_math_internal.h from their .src
        templates during build_src, driven by the add_data_files() entries in
        numpy/core/setup.py. Under p4a's cross-compile patches that data-files
        pass produces no output at all -- build_src creates the target dirs and
        then stops without copying anything -- so the very first compile of the
        npymath library dies:

            numpy/core/src/npymath/npy_math_internal.h.src:56:10:
            fatal error: 'npy_math_private.h' file not found

        The compiler looks the headers up via
        -Ibuild/src.<plat>-<pyver>/numpy/core/src/npymath, a path relative to
        the numpy source root (the cwd of setup.py). Expanding the templates
        into that exact directory up front is deterministic and idempotent.
        """
        import os
        from os.path import join as _join, isdir, exists
        from pythonforandroid.logger import info

        build_dir = self.get_build_dir(arch.arch)
        npymath = _join(build_dir, 'numpy', 'core', 'src', 'npymath')
        if not isdir(npymath):
            info('numpy: no npymath sources at {}'.format(npymath))
            return

        # <plat> matches what numpy uses for build/src.<plat>-<pyver>; the
        # exact suffix varies (aarch64-linux-android-3.11 here), so mirror the
        # path numpy itself announced by scanning for build/src.* dirs, and
        # additionally drop the header beside the template, which is on the
        # include path through -Inumpy/core/src/npymath.
        out_dirs = [npymath]
        for entry in os.listdir(build_dir):
            if entry.startswith('build'):
                cand = _join(build_dir, entry, 'numpy', 'core', 'src', 'npymath')
                out_dirs.append(cand)

        for name in ('npy_math_private.h', 'npy_math_internal.h'):
            tpl = _join(npymath, name + '.src')
            if not exists(tpl):
                info('numpy: template {} missing'.format(tpl))
                continue
            for out_dir in out_dirs:
                os.makedirs(out_dir, exist_ok=True)
                dst = _join(out_dir, name)
                if exists(dst):
                    continue
                # numpy's conv_template does the @VAR@ substitution; it has no
                # numpy imports at module scope, so load it straight from the
                # tree being built.
                import importlib.util
                spec = importlib.util.spec_from_file_location(
                    '_np_conv_template',
                    _join(build_dir, 'numpy', 'distutils', 'conv_template.py'))
                mod = importlib.util.module_from_spec(spec)
                spec.loader.exec_module(mod)
                mod.process_file(tpl, dst)
                info('numpy: generated {}'.format(dst))

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
