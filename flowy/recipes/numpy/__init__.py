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
    def _patch_build_ext_customize(self, arch):
        """Rewrite build_ext.py so it never calls the missing helpers.

        numpy.distutils attaches customize() / customize_cmd() /
        show_customization() to *its own* CCompiler class, but the command gets
        whatever new_compiler() returns -- under p4a that is setuptools'
        UnixCCompiler, which never received them:

            AttributeError: 'UnixCCompiler' object has no attribute 'customize'
            AttributeError: 'UnixCCompiler' object has no attribute 'customize_cmd'
            AttributeError: 'UnixCCompiler' object has no attribute 'show_customization'

        Monkeypatching the class does not help because setup.py runs in a child
        interpreter; editing the file is interpreter-independent. Every call is
        rewritten to a getattr() guard, and customize() falls back to distutils'
        customize_compiler() -- the routine numpy's wrapper delegates to. The
        other two only record bookkeeping / log, so skipping them is faithful.
        """
        import os
        import re
        from pythonforandroid.logger import info

        path = os.path.join(self.get_build_dir(arch.arch), 'numpy', 'distutils',
                            'command', 'build_ext.py')
        if not os.path.exists(path):
            info('numpy: build_ext.py not found, skipping hook patch')
            return
        with open(path) as handle:
            text = handle.read()
        original = text

        guard = ('{i}_cz = getattr({t}, "{m}", None)\n'
                 '{i}if _cz is not None:\n'
                 '{i}    _cz({args})\n')

        def _customize(match):
            indent, target = match.group(1), match.group(2)
            return (indent + '_cz = getattr({0}, "customize", None)\n'.format(target)
                    + indent + 'if _cz is not None:\n'
                    + indent + '    _cz(self.distribution)\n'
                    + indent + 'else:\n'
                    + indent + '    from distutils.sysconfig import customize_compiler\n'
                    + indent + '    customize_compiler({0})\n'.format(target))

        def _cmd(match):
            indent, target = match.group(1), match.group(2)
            return (indent + '_cz = getattr({0}, "customize_cmd", None)\n'.format(target)
                    + indent + 'if _cz is not None:\n'
                    + indent + '    _cz(self)\n')

        def _show(match):
            indent, target = match.group(1), match.group(2)
            return (indent + '_cz = getattr({0}, "show_customization", None)\n'.format(target)
                    + indent + 'if _cz is not None:\n'
                    + indent + '    _cz()\n')

        text, n1 = re.subn(
            r'(?m)^(\s*)([A-Za-z_][\w.]*)\.customize\(self\.distribution[^)]*\)\n',
            _customize, text)
        text, n2 = re.subn(
            r'(?m)^(\s*)([A-Za-z_][\w.]*)\.customize_cmd\([^)]*\)\n', _cmd, text)
        text, n3 = re.subn(
            r'(?m)^(\s*)([A-Za-z_][\w.]*)\.show_customization\(\)\n', _show, text)

        if text != original:
            with open(path, 'w') as handle:
                handle.write(text)
        info('numpy: build_ext.py guarded customize={} customize_cmd={} '
             'show_customization={}'.format(n1, n2, n3))

    def get_recipe_env(self, arch=None, with_flags_in_cc=True):
        """Ship the compiler-hook shim to the setup.py subprocess.

        build_arch installs the missing methods on distutils' CCompiler, but
        numpy's build runs in a *child* interpreter (hostpython setup.py
        build_ext), so the patch has to travel with the environment. A
        sitecustomize.py on PYTHONPATH is executed automatically at startup.
        """
        env = super().get_recipe_env(arch, with_flags_in_cc)
        # numpy's CPU-dispatch machinery generates per-feature variants of an
        # extension's sources (_umath_tests.dispatch.asimdhp.c) and relies on
        # build_src having materialised the base .c alongside them. With that
        # pass inert only the variants exist, so the link step looks for an
        # object that was never compiled:
        #   no such file or directory:
        #     'build/temp.../numpy/core/src/umath/_umath_tests.o'
        # Disabling optimisation skips dispatch generation entirely; the plain
        # baseline build is what the NDK toolchain produces anyway.
        env['NPY_DISABLE_OPTIMIZATION'] = '1'
        import os
        import shutil
        shim_dir = os.path.join(self.get_build_dir(arch.arch), '.p4a_shim')
        os.makedirs(shim_dir, exist_ok=True)
        src_shim = os.path.join(dirname(__file__), 'compiler_shim.py')
        shutil.copyfile(src_shim, os.path.join(shim_dir, 'sitecustomize.py'))
        existing = env.get('PYTHONPATH', '')
        env['PYTHONPATH'] = shim_dir + (os.pathsep + existing if existing else '')
        return env

    def build_arch(self, arch, **kwargs):
        """Probe the npymath include path before numpy compiles anything.

        p4a's ``Prebuilding numpy ... / numpy has no prebuild_arm64_v8a,
        skipping`` message shows this recipe class carries no prebuild hook, so
        the earlier prebuild_arch() here never ran. Do the repair in build_arch
        instead: it is called for every recipe unconditionally.
        """
        self._ensure_npymath_headers(arch)
        self._patch_build_ext_customize(arch)
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
        inc_numpy = os.path.join(base, 'numpy', 'core', 'include', 'numpy')
        if os.path.isdir(inc_numpy):
            hp = sorted(n for n in os.listdir(inc_numpy) if n.endswith('.h'))
            fp = [n for n in hp if 'fpmath' in n]
            info('numpy: include/numpy has {} headers, fpmath={}'.format(
                len(hp), fp))
        # Expand the .src templates in place first: the mirror step copies
        # whatever headers exist, and the generated ones only appear after
        # this runs.
        self._expand_c_templates(base, info)

        # Mirror every header first: npy_math_private.h transitively includes
        # npy_config.h and npy_fpmath.h, so staging only the one current error
        # names would just move the failure one include further down.
        self._mirror_numpy_headers(base, info)
        self._mirror_numpy_sources(base, info)

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

            # npy_math_private.h includes "npy_config.h", which build_src would
            # normally write into build/src.<plat>/numpy/core/include/numpy/.
            # Because that pass produces nothing here, generate the header from
            # numpy's own template so the include resolves:
            #   npy_math_private.h:30:10: fatal error: 'npy_config.h' not found
            # out_dir is .../build/src.<plat>/numpy/core/src/npymath; the
            # include root numpy passes as -Ibuild/src.<plat>/numpy/core/include
            # sits three levels up from npymath.
            # out_dir is .../build/src.<plat>/numpy/core/src/npymath, so the
            # "core" directory is two levels up; numpy adds
            # -Ibuild/src.<plat>/numpy/core/include and the header lives in
            # its numpy/ subdirectory.
            core_dir = os.path.dirname(os.path.dirname(out_dir))
            inc_dir = os.path.join(core_dir, 'include', 'numpy')
            info('numpy: config header dir {}'.format(inc_dir))
            os.makedirs(inc_dir, exist_ok=True)
            cfg = os.path.join(inc_dir, 'npy_config.h')
            # Always rewrite: the recipe is the single source of truth for this
            # header, and leaving a stale copy in a cached build tree is how a
            # fixed macro set silently kept failing.
            self._write_npy_config_h(base, cfg, info)

        self._write_npy_fpmath_h(base, info)
        self._write_config_h(base, info)
        self._write_numpyconfig_h(base, info)

    @staticmethod
    def _mirror_numpy_sources(base, info):
        """Mirror every numpy/core/{include,src} file into build/src.<plat>/.

        numpy's build plan compiles objects from build/src.<plat>/numpy/core/...,
        because build_src normally populates that tree by copying the core
        sources (and their generated forms) into it. With that pass inert the
        compile step has nothing to chew on and the link fails with dozens of:

            error: no such file or directory:
              'build/temp.../numpy/core/src/umath/_umath_tests.o'

        Mirroring the whole tree is the same thing build_src would do, just
        without its probe bookkeeping.
        """
        import os
        import shutil

        build_root = os.path.join(base, 'build')
        if not os.path.isdir(build_root):
            return
        dest_roots = [os.path.join(build_root, e) for e in os.listdir(build_root)
                      if e.startswith('src.')]
        if not dest_roots:
            return
        copied = 0
        for sub in ('include', 'src'):
            top = os.path.join(base, 'numpy', 'core', sub)
            if not os.path.isdir(top):
                continue
            for dirpath, _dirnames, filenames in os.walk(top):
                inner = os.path.relpath(dirpath, os.path.join(
                    base, 'numpy', 'core'))
                for name in filenames:
                    if name.endswith('.orig') or name.endswith('.pyc'):
                        continue
                    source = os.path.join(dirpath, name)
                    for dest_root in dest_roots:
                        out_dir = os.path.join(dest_root, 'numpy', 'core', inner)
                        os.makedirs(out_dir, exist_ok=True)
                        dst = os.path.join(out_dir, name)
                        try:
                            shutil.copyfile(source, dst)
                            copied += 1
                        except OSError:
                            pass
        info('numpy: mirrored {} files into build/src.*'.format(copied))

    @staticmethod
    def _mirror_numpy_headers(base, info):
        """Copy every header the C compiler will look for into build/src.*.

        build_src's data-file pass emits nothing in this build, so headers that
        numpy copies from numpy/core/{include,src} into build/src.<plat>/ are
        simply absent. The failures surface one at a time:
            npy_math_private.h -> 'npy_config.h' not found
                               -> 'npy_fpmath.h' not found
        Mirroring the whole header set up front breaks that cycle instead of
        chasing one missing include per CI round.
        """
        import glob
        import os
        import shutil

        src_roots = [
            os.path.join(base, 'numpy', 'core', 'include'),
            os.path.join(base, 'numpy', 'core', 'src'),
        ]
        # build/src.<plat>-<pyver> is created by numpy during build_src, which
        # has not run at this point. Build the canonical name ourselves and
        # use it as the fallback target rather than skipping the mirror (a
        # silent skip is what made the previous round behave exactly like the
        # one before it).
        dest_roots = glob.glob(os.path.join(base, 'build', 'src.*'))
        if not dest_roots:
            dest_roots = [os.path.join(
                base, 'build', 'src.aarch64-linux-android-3.11')]
            os.makedirs(dest_roots[0], exist_ok=True)
            info('numpy: build/src.* absent, created {}'.format(dest_roots[0]))

        copied = 0
        for dest_root in dest_roots:
            for src_root in src_roots:
                rel = os.path.relpath(src_root, os.path.join(base, 'numpy', 'core'))
                target_root = os.path.join(dest_root, 'numpy', 'core', rel)
                os.makedirs(target_root, exist_ok=True)
                for dirpath, _dirnames, filenames in os.walk(src_root):
                    inner = os.path.relpath(dirpath, src_root)
                    out_dir = os.path.join(target_root, inner) if inner != '.' \
                        else target_root
                    os.makedirs(out_dir, exist_ok=True)
                    for name in filenames:
                        # Only headers matter here; sources are compiled in
                        # place by the build commands.
                        if not name.endswith('.h'):
                            continue
                        dst = os.path.join(out_dir, name)
                        if os.path.exists(dst):
                            continue
                        try:
                            shutil.copyfile(os.path.join(dirpath, name), dst)
                            copied += 1
                        except OSError:
                            pass
        info('numpy: mirrored {} headers into build/src.*'.format(copied))

    @staticmethod
    def _expand_c_templates(base, info):
        """Expand numpy's .src templates with numpy's own processor.

        numpy's npymath sources live as .src templates using the
        /**begin repeat ... /**end repeat**/ dialect, where a block declares

            /**begin repeat
             * #type = npy_longdouble, npy_float#
             * #c = l,f#
             */

        and every ``@type@`` / ``@c@`` inside the block is replaced once per
        iteration. numpy expands them in build_src, which emits nothing here, so
        the compiler reads the raw template and reports:

            npy_math_internal.h:414:8: error: macro name must be an identifier
            npy_math_internal.h:627:13: error: expected identifier or '('

        conv_template.process_file(path) returns the expanded text (single
        argument, no in-place mode), which is what numpy itself calls.
        """
        import os
        import re
        import importlib.util
        import shutil

        conv = os.path.join(base, 'numpy', 'distutils', 'conv_template.py')
        if not os.path.exists(conv):
            info('numpy: conv_template.py missing')
            return
        spec = importlib.util.spec_from_file_location('_np_conv', conv)
        mod = importlib.util.module_from_spec(spec)
        try:
            spec.loader.exec_module(mod)
        except Exception as exc:
            info('numpy: conv_template import failed: {}'.format(exc))
            return

        expanded = 0
        for root in ('include', 'src'):
            top = os.path.join(base, 'numpy', 'core', root)
            if not os.path.isdir(top):
                continue
            for dirpath, _dirnames, filenames in os.walk(top):
                for name in filenames:
                    if not name.endswith('.src'):
                        continue
                    src = os.path.join(dirpath, name)
                    dst = src[:-4]          # strip ".src"
                    try:
                        text = mod.process_file(src)
                    except Exception as exc:
                        info('numpy: template {} failed: {}'.format(name, exc))
                        continue
                    if not text or '@type@' in text:
                        # Leave a template that did not expand untouched --
                        # writing it out would only move the error.
                        continue
                    with open(dst, 'w') as handle:
                        handle.write(text)
                    # npymath's npy_math.c does
                    #     #include "npy_math_internal.h.src"
                    # -- it names the template, not the generated header, and
                    # relies on build_src having expanded it in place. Since
                    # that pass is a no-op here, overwrite the .src with its own
                    # expanded text so the include picks up real code. A
                    # pristine sibling (.src.orig) is kept for reference and
                    # idempotency.
                    # Write the expansion back over the template: npy_math.c
                    # and friends select it by its ".src" name, so the plain
                    # .h/.c outputs above are never consulted.
                    #
                    # conv_template prefixes its output with
                    #     #line 1 "<the .src path>"
                    # which is fine for the file itself, but a template that
                    # includes a sibling by bare name (ieee754.c.src does
                    # `#include "_signbit.c"`) then fails to resolve it. Anchor
                    # every quoted sibling include on the template's own
                    # directory instead.
                    orig = src + '.orig'
                    if not os.path.exists(orig):
                        shutil.copyfile(src, orig)

                    def _anchor(match, _dir=dirpath):
                        return '#include "{}"'.format(
                            os.path.join(_dir, match.group(1)))

                    text = re.sub(r'#\s*include\s*"([^"/]+\.(?:c|h))"',
                                  _anchor, text)
                    with open(src, 'w') as handle:
                        handle.write(text)

                    # The compile plan also references the generated copies
                    # under build/src.<plat>/, e.g.
                    #   build/src.aarch64-.../numpy/core/src/multiarray/arraytypes.c
                    # With build_src inert those files never appear, and the
                    # link step then fails on every missing .o:
                    #   error: no such file or directory:
                    #     'build/temp.../numpy/core/src/npysort/quicksort.o'
                    # Mirror the expanded source into the same relative path
                    # under every existing build/src.<plat>/ root.
                    rel_to_core = os.path.relpath(src, os.path.join(
                        base, 'numpy', 'core'))
                    build_root = os.path.join(base, 'build')
                    if os.path.isdir(build_root):
                        for entry in os.listdir(build_root):
                            if not entry.startswith('src.'):
                                continue
                            out = os.path.join(build_root, entry, 'numpy',
                                               'core', rel_to_core)
                            os.makedirs(os.path.dirname(out), exist_ok=True)
                            with open(out, 'w') as handle:
                                handle.write(text)
                    expanded += 1
        info('numpy: expanded {} .src templates'.format(expanded))

    @staticmethod
    def _write_numpyconfig_h(base, info):
        """Write _numpyconfig.h selecting the binary128 long double.

        numpyconfig.h pulls this in first, and it is where the public-API
        macros live, including the long-double flavour selectors.
        npy_math_private.h then uses them with #ifdef:

            #if defined(NPY_HAVE_LDOUBLE_INTEL_EXTENDED_16_BYTES_LE)
            ... x87 bit-field helpers using LDBL_MANL_INDEX ...

        Leaving that undefined on aarch64 is what routes ieee754.c to the
        binary128 code. The generated variant normally comes from numpy's
        probe pass, which produces nothing here, so write the AArch64 answers.
        """
        import os

        content = """\
#ifndef NUMPY_CORE_INCLUDE_NUMPY__NUMPYCONFIG_H_
#define NUMPY_CORE_INCLUDE_NUMPY__NUMPYCONFIG_H_

#include "config.h"

#define NPY_SIZEOF_SHORT SIZEOF_SHORT
#define NPY_SIZEOF_INT SIZEOF_INT
#define NPY_SIZEOF_LONG SIZEOF_LONG
#define NPY_SIZEOF_FLOAT 4
#define NPY_SIZEOF_COMPLEX_FLOAT 8
#define NPY_SIZEOF_DOUBLE 8
#define NPY_SIZEOF_COMPLEX_DOUBLE 16
#define NPY_SIZEOF_LONGDOUBLE 16
#define NPY_SIZEOF_COMPLEX_LONGDOUBLE 32
#define NPY_SIZEOF_PY_INTPTR_T 8
#define NPY_SIZEOF_INTP 8
#define NPY_SIZEOF_UINTP 8
#define NPY_SIZEOF_WCHAR_T 4
#define NPY_SIZEOF_OFF_T 8
#define NPY_SIZEOF_PY_LONG_LONG 8
/* npy_math_internal.h spells this one without the second underscore. */
#define NPY_SIZEOF_LONGLONG 8

#define NPY_HAVE_LDOUBLE_IEEE_QUAD_LE 1
#define HAVE_LDOUBLE_IEEE_QUAD_LE 1

#define NPY_HAVE_ENDIAN_H 1
#define NPY_HAVE_DECL_ISNAN 1
#define NPY_HAVE_DECL_ISINF 1
#define NPY_HAVE_DECL_ISFINITE 1
#define NPY_HAVE_DECL_SIGNBIT 1
#define NPY_USE_C99_COMPLEX 1
#define NPY_USE_C99_FORMATS 1
#define NPY_VISIBILITY_HIDDEN __attribute__((visibility("hidden")))
#define NPY_ABI_VERSION 0x02000000
#define NPY_API_VERSION 0x0000000E

#endif
"""
        targets = [
            os.path.join(base, 'build', 'src.aarch64-linux-android-3.11',
                         'numpy', 'core', 'include', 'numpy'),
            os.path.join(base, 'numpy', 'core', 'include', 'numpy'),
        ]
        for out_dir in targets:
            os.makedirs(out_dir, exist_ok=True)
            dst = os.path.join(out_dir, '_numpyconfig.h')
            with open(dst, 'w') as handle:
                handle.write(content)
            info('numpy: wrote {}'.format(dst))

    @staticmethod
    def _write_config_h(base, info):
        """Write numpy's config.h for aarch64.

        numpy's config.h carries the SIZEOF_*/HAVE_*/inline results from its
        configure probes, and _numpyconfig.h re-exports them as the NPY_*
        names the public headers use:

            #define NPY_SIZEOF_SHORT SIZEOF_SHORT

        With config.h missing, SIZEOF_SHORT is an unknown identifier in #if
        expressions and evaluates to 0 -- which makes
        NPY_SIZEOF_LONGDOUBLE == NPY_SIZEOF_DOUBLE true for the wrong reason
        and leaves npy_math_internal.h choosing a decorator whose CAT() helper
        was never declared:

            #define NPY__FP_SFX(X) NPY_CAT(X, l)  -> use of undeclared 'NPY_CAT'

        The values below are the AArch64 LP64 answers the probes would return.
        """
        import os

        content = """\
#ifndef NUMPY_CORE_SRC_COMMON_CONFIG_H_
#define NUMPY_CORE_SRC_COMMON_CONFIG_H_

/* aarch64-linux-android, NDK r25b clang, libc++ */
#define SIZEOF_LONG 8
#define SIZEOF_INT 4
#define SIZEOF_SHORT 2
#define SIZEOF_FLOAT 4
#define SIZEOF_DOUBLE 8
#define SIZEOF_LONG_DOUBLE 16
#define SIZEOF_LONG_LONG 8
#define SIZEOF_PY_LONG_LONG 8
#define SIZEOF_PYTHON_OBJECT 4
#define SIZEOF_PY_INTPTR_T 8
#define SIZEOF_OFF_T 8
#define SIZEOF_SIZE_T 8
#define SIZEOF_VOID_P 8
#define SIZEOF_WCHAR_T 4
#define SIZEOF_PTHREAD_T 8
#define SIZEOF_TIME_T 8
#define SIZEOF_OFF64_T 8

#define HAVE_LONG_DOUBLE 1
#define HAVE_BACKTRACE 1
#define HAVE_MADVISE 1
#define HAVE_MMAP 1
#define HAVE_MUNMAP 1
#define HAVE_SOCKET 1
#define HAVE_SELECT 1
#define HAVE_LOCKF 1
#define HAVE_FSEEKO 1
#define HAVE_FTELLO 1
#define HAVE_STRTOK_R 1
#define HAVE_STRTOL 1
#define HAVE_STRTOUL 1
#define HAVE_STRTOLL 1
#define HAVE_STRTOULL 1
#define HAVE_STRTOD 1
#define HAVE_STRTOF 1
#define HAVE_EXPM1 1
#define HAVE_LOG1P 1
#define HAVE_ACOSH 1
#define HAVE_ASINH 1
#define HAVE_ATANH 1
#define HAVE_EXP2 1
#define HAVE_LOG2 1
#define HAVE_CBRT 1
#define HAVE_RINT 1
#define HAVE_TRUNC 1
#define HAVE_HYPOT 1
#define HAVE_COPYSIGN 1
#define HAVE_NEXTAFTER 1
#define HAVE_ERRNO 1
#define HAVE_WCHAR_T 1
#define HAVE_GETC_UNLOCKED 1
#define HAVE_PUTWC 1
#define HAVE_FOPENCOOKIE 0
#define HAVE_FUNOPEN 0
#define HAVE_C99_COMPLEX 1
#define HAVE_C99_STDINT_H 1
#define HAVE_INF 1
#define HAVE_NAN 1
#define HAVE_C99_INFINITY 1
#define HAVE_C99_NAN 1
#define HAVE_DLFCN_H 1
#define HAVE_UNISTD_H 1
#define HAVE_SYS_TYPES_H 1
#define HAVE_SYS_MMAN_H 1
#define HAVE_FEATURES_H 0
#define HAVE_ENDIAN_H 1
#define HAVE_SYS_ENDIAN_H 1
#define HAVE_LIBUNWIND_H 0
#define HAVE_LIBUNWIND 0
/* x86 SIMD headers must stay UNDEFINED: npy_common.h tests them with #ifdef. */

#define inline inline
#define restrict __restrict__

#endif /* NUMPY_CORE_SRC_COMMON_CONFIG_H_ */
"""
        targets = [
            os.path.join(base, 'build', 'src.aarch64-linux-android-3.11',
                         'numpy', 'core', 'include', 'numpy'),
            os.path.join(base, 'numpy', 'core', 'include', 'numpy'),
        ]
        for out_dir in targets:
            os.makedirs(out_dir, exist_ok=True)
            dst = os.path.join(out_dir, 'config.h')
            with open(dst, 'w') as handle:
                handle.write(content)
            info('numpy: wrote {}'.format(dst))

    @staticmethod
    def _write_npy_fpmath_h(base, info):
        """Write npy_fpmath.h for aarch64.

        npy_math_private.h does ``#include "npy_fpmath.h"``, which numpy's
        build_src generates from the result of its long-double probe. Under p4a
        that pass emits nothing, so the header is simply absent:

            npy_math_private.h:31:10: fatal error: 'npy_fpmath.h' file not found

        On AArch64 the C ABI defines long double as IEEE-754 binary128 (the
        NDK's clang implements it in software via compiler-rt), so the probe
        result is known ahead of time. The macro set below is the one numpy
        1.22.3 emits for a binary128 long double; it selects the "float128"
        npymath implementation, exactly as an x86 build selects its 80-bit
        variant.
        """
        import os

        content = """\
#ifndef NUMPY_CORE_INCLUDE_NUMPY_NPY_FPMATH_H_
#define NUMPY_CORE_INCLUDE_NUMPY_NPY_FPMATH_H_

/* aarch64-linux-android: the AArch64 ABI defines long double as IEEE-754
   binary128, so numpy must take the "float128" path.

   numpy selects that path with #ifdef, not #if: npy_math_private.h contains
       #if defined(NPY_HAVE_LDOUBLE_INTEL_EXTENDED_16_BYTES_LE)
   and defines the x87 bit-field macros (LDBL_MANL_INDEX, ...) inside those
   blocks. Writing `#define NPY_HAVE_LDOUBLE_INTEL_EXTENDED_... 0` therefore
   counts as "present" and drags ieee754.c into code that only compiles on
   x86. Leave every x86 variant undefined. */

#if !defined(NPY_HAVE_LDOUBLE_IEEE_QUAD_LE)
#define NPY_HAVE_LDOUBLE_IEEE_QUAD_LE 1
#define HAVE_LDOUBLE_IEEE_QUAD_LE 1
#endif

#endif /* NUMPY_CORE_INCLUDE_NUMPY_NPY_FPMATH_H_ */
"""
        # Place it wherever -Inumpy/core/include/numpy resolves: both the
        # staged build tree and the source tree (the latter is on CPPPATH too).
        targets = [
            os.path.join(base, 'build', 'src.aarch64-linux-android-3.11',
                         'numpy', 'core', 'include', 'numpy'),
            os.path.join(base, 'numpy', 'core', 'include', 'numpy'),
        ]
        for out_dir in targets:
            os.makedirs(out_dir, exist_ok=True)
            dst = os.path.join(out_dir, 'npy_fpmath.h')
            with open(dst, 'w') as handle:
                handle.write(content)
            info('numpy: wrote {}'.format(dst))

    @staticmethod
    def _write_npy_config_h(base, dst, info):
        """Write npy_config.h for the android arm64 target.

        numpy produces this header from its configure probes (numpy/core/setup.py
        -> generate_config_h), pairing it with a config.h that carries the
        SIZEOF_*/HAVE_* results. Under p4a the probe loop cannot bootstrap --
        each test compiles against the very macros it is supposed to discover
        and dies with:

            _configtest.c:7:12: error: use of undeclared identifier
            'SIZEOF_LONGDOUBLE'

        leaving npy_config.h unwritten and npymath uncompilable. The values are
        fixed for the target anyway (aarch64-linux-android24 with the NDK's
        clang and libc++), so emit them directly. Each entry is a documented
        property of the AArch64 ABI, not a guess.
        """
        import os

        content = """\
#ifndef NUMPY_CORE_INCLUDE_NUMPY_NPY_CONFIG_H_
#define NUMPY_CORE_INCLUDE_NUMPY_NPY_CONFIG_H_

/* numpy's real npy_config.h starts by pulling in the generated config.h; the
   SIZEOF_* macros below are what _numpyconfig.h re-exports as NPY_SIZEOF_*. */
#include "config.h"

/* Generated for aarch64-linux-android (arm64-v8a, NDK r25b clang, libc++).
   AArch64 LP64 data model: char 1, short 2, int 4, long 8, long long 8,
   float 4, double 8, long double 16 (IEEE binary128 in clang/NDK),
   pointer 8, off_t 8, Py_ssize_t 8. */
#define SIZEOF_LONG 8
#define SIZEOF_INT 4
#define SIZEOF_FLOAT 4
#define SIZEOF_DOUBLE 8
#define SIZEOF_LONG_DOUBLE 16
#define SIZEOF_LONG_LONG 8
#define SIZEOF_PY_LONG_LONG 8
#define SIZEOF_PYTHON_OBJECT 4
#define SIZEOF_PY_INTPTR_T 8
#define SIZEOF_OFF_T 8
#define SIZEOF_SIZE_T 8
#define SIZEOF_VOID_P 8
#define SIZEOF_FLOAT128 0
#define SIZEOF___FLOAT128 0

/* Android bionic provides these; libm is linked explicitly. */
#define HAVE_FEATURES_H 0
#define HAVE_DLFCN_H 1
#define HAVE_SYS_MMAN_H 1
#define HAVE_SYS_TYPES_H 1
#define HAVE_UNISTD_H 1
#define HAVE_ENDIAN_H 1
#define HAVE_SYS_ENDIAN_H 0
#define HAVE_LIBUNWIND_H 0
#define HAVE_LIBUNWIND 0
#define HAVE_BACKTRACE 1
#define HAVE_MADVISE 1
#define HAVE_EXPM1 1
#define HAVE_LOG1P 1
#define HAVE_ACOSH 1
#define HAVE_ASINH 1
#define HAVE_ATANH 1
#define HAVE_EXP2 1
#define HAVE_LOG2 1
#define HAVE_CBRT 1
#define HAVE_RINT 1
#define HAVE_TRUNC 1
#define HAVE_HYPOT 1
#define HAVE_COPYSIGN 1
#define HAVE_NEXTAFTER 1
#define HAVE_STRTOK_R 1
#define HAVE_STRTOL 1
#define HAVE_STRTOUL 1
#define HAVE_STRTOLL 1
#define HAVE_STRTOULL 1
#define HAVE_STRTOD 1
#define HAVE_STRTOF 1
#define HAVE_STRTOQ 0
#define HAVE_STRTOUQ 0
#define HAVE_GETC_UNLOCKED 1
#define HAVE_PUTWC 1
#define HAVE_PUTWCHAR 1
#define HAVE_GETCWCHAR 1
#define HAVE_GETWCUNLOCKED 0
#define HAVE_ERRNO 1
#define HAVE_WCHAR_T 1
#define HAVE_LONG_DOUBLE 1
#define HAVE_MMAP 1
#define HAVE_MUNMAP 1
#define HAVE_SOCKET 1
#define HAVE_SELECT 1
#define HAVE_LOCKF 1
#define HAVE_FSEEKO 1
#define HAVE_FTELLO 1
#define HAVE_FOPENCOOKIE 0
#define HAVE_FUNOPEN 0
#define HAVE_C99_COMPLEX 1
#define HAVE_C99_STDINT_H 1
#define HAVE_C99_LONG_LONG 1
#define HAVE_INF 1
#define HAVE_NAN 1
#define HAVE_C99_INFINITY 1
#define HAVE_C99_NAN 1

/* x86-only intrinsics stay off on arm64 by REMAINING UNDEFINED.
   npy_common.h tests these with #ifdef, not #if, so a `#define ... 0` would
   read as "present" and drag in <xmmintrin.h>/<immintrin.h>:
       npy_common.h:87: "NPY_HAVE_SSE_INTRINSICS" macro redefined
   and then the x86 headers fail to compile for aarch64. */

/* npy_math_internal.h picks its symbol decorator from these:
     #if NPY_SIZEOF_LONGDOUBLE == NPY_SIZEOF_DOUBLE
         #define NPY__FP_SFX(X) X
     #else
         #define NPY__FP_SFX(X) NPY_CAT(X, l)
     #endif
   They are normally produced by the same probe pass as the SIZEOF_* block
   above; leaving them undefined makes the comparison evaluate false and every
   float helper is decorated with a CAT() that is never declared. */
#define NPY_SIZEOF_SHORT 2
#define NPY_SIZEOF_INT 4
#define NPY_SIZEOF_LONG 8
#define NPY_SIZEOF_FLOAT 4
#define NPY_SIZEOF_COMPLEX_FLOAT 8
#define NPY_SIZEOF_DOUBLE 8
#define NPY_SIZEOF_COMPLEX_DOUBLE 16
#define NPY_SIZEOF_LONGDOUBLE 16
#define NPY_SIZEOF_COMPLEX_LONGDOUBLE 32
#define NPY_SIZEOF_PY_INTPTR_T 8
#define NPY_SIZEOF_INTP 8
#define NPY_SIZEOF_UINTP 8
#define NPY_SIZEOF_WCHAR_T 4
#define NPY_SIZEOF_OFF_T 8
#define NPY_SIZEOF_PY_LONG_LONG 8

/* npymath's npy_math_internal.h.src decorates per-precision symbols with
   NPY_CAT(X, l) / NPY_CAT(X, f) / NPY_CAT(X, ). The macro itself is normally
   supplied by numpy's build machinery; when it is absent every one of those
   decorators expands to an undeclared identifier:
       npy_math_internal.h:599: use of undeclared identifier 'NPY_CAT'
   A two-level CAT is the standard token-paste idiom. */
/* npymath's npy_math_internal.h.src decorates every per-precision symbol with
   NPY_CAT(X, l) / NPY_CAT(X, f) / NPY_CAT(X, ).

   numpy normally gets NPY_CAT from numpy/core/include/numpy/utils.h, but the
   npymath translation units never include that header -- the generated
   npy_math_internal.h is supposed to arrive with the macro already in scope.
   Defining the standard two-level paste here is equivalent and keeps the file
   self-contained; the #ifndef leaves utils.h free to redefine it later. */
#ifndef NPY_CAT
#define NPY_CAT_(a, b) a ## b
#define NPY_CAT(a, b) NPY_CAT_(a, b)
#endif

/* npy_math_internal.h spells this without the second underscore. */
#define NPY_SIZEOF_LONGLONG 8

/* npy_math_private.h selects its long-double bit layout with the UNPREFIXED
   name (see #elif defined(HAVE_LDOUBLE_IEEE_QUAD_LE)); the NPY_ prefixed form
   is the public-API spelling and does not drive the choice. Without this,
   none of the LDBL_*_INDEX macros get defined and ieee754.c fails with
   "use of undeclared identifier 'LDBL_MANL_INDEX'". */
#define HAVE_LDOUBLE_IEEE_QUAD_LE 1

#define NPY_POLYLIKE 1
#define NPY_USE_C99_COMPLEX 1
#define NPY_USE_C99_FORMATS 1
#define NPY_NO_SIGNAL 0
#define NPY_NO_SMP 0
#define NPY_BLAS_ORDER 0
#define NPY_LAPACK_ORDER 0

#endif /* NUMPY_CORE_INCLUDE_NUMPY_NPY_CONFIG_H_ */
"""
        os.makedirs(os.path.dirname(dst), exist_ok=True)
        with open(dst, 'w') as handle:
            handle.write(content)
        info('numpy: wrote {} ({} bytes)'.format(dst, os.path.getsize(dst)))

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
