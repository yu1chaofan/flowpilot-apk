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
    def build_arch(self, arch, **kwargs):
        """Probe the npymath include path before numpy compiles anything.

        p4a's ``Prebuilding numpy ... / numpy has no prebuild_arm64_v8a,
        skipping`` message shows this recipe class carries no prebuild hook, so
        the earlier prebuild_arch() here never ran. Do the repair in build_arch
        instead: it is called for every recipe unconditionally.
        """
        self._ensure_npymath_headers(arch)
        return super().build_arch(arch, **kwargs)

    def _ensure_npymath_headers(self, arch):
        import glob
        import os
        import shutil
        from pythonforandroid.logger import info

        base = self.get_build_dir(arch.arch)
        info('numpy: build dir {}'.format(base))
        npymath = os.path.join(base, 'numpy', 'core', 'src', 'npymath')
        if not os.path.isdir(npymath):
            info('numpy: npymath dir missing: {}'.format(npymath))
            return
        entries = sorted(os.listdir(npymath))
        info('numpy: npymath has {}'.format(entries))

        source = None
        for name in ('npy_math_private.h', 'npy_math_private.h.src'):
            cand = os.path.join(npymath, name)
            if os.path.exists(cand):
                source = cand
                break
        if source is None:
            for dirpath, _d, filenames in os.walk(base):
                if 'npy_math_private.h' in filenames:
                    source = os.path.join(dirpath, 'npy_math_private.h')
                    break
        info('numpy: npy_math_private.h source = {}'.format(source))
        if source is None:
            return

        targets = glob.glob(os.path.join(base, 'build', 'src.*', 'numpy',
                                         'core', 'src', 'npymath'))
        if not targets:
            # numpy has not created build/src.* yet; create the canonical one.
            targets = [os.path.join(base, 'build', 'src.aarch64-linux-android-3.11',
                                    'numpy', 'core', 'src', 'npymath')]
        for out_dir in targets:
            os.makedirs(out_dir, exist_ok=True)
            dst = os.path.join(out_dir, 'npy_math_private.h')
            if not os.path.exists(dst):
                shutil.copyfile(source, dst)
                info('numpy: staged -> {}'.format(dst))

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
