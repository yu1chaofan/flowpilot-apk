from os.path import dirname, join
from pythonforandroid.util import ensure_dir

from pythonforandroid.recipe import CythonRecipe, Recipe
from pythonforandroid.logger import info


class PycapnpRecipe(CythonRecipe):
    """pycapnp for python-for-android.

    Why this subclasses CythonRecipe instead of PythonRecipe
    -------------------------------------------------------
    PythonRecipe only builds a wheel via `pip install . --compile --target`.
    Because pycapnp v1.0.0 ships no pyproject.toml, pip builds it in an
    isolated env (/tmp/pip-build-env-*) containing just setuptools, while
    setup.py does `from Cython.Distutils import build_ext` at import time.
    That combination dies with:

        ModuleNotFoundError: No module named 'Cython'

    CythonRecipe instead runs `setup.py build_ext` against the *host* python
    (which has our pinned cython==0.29.37) and, if that fails as expected,
    calls cythonize_build() to produce capnp/lib/capnp.cpp itself. By the time
    pip runs, the .cpp already exists and Cython is no longer needed.

    The Cython pin matters for a second reason: Cython 3 emits CPython private
    symbols (_PyDict_SetItem_KnownHash, _PyInterpreterState_GetConfig) that the
    p4a python3 headers do not expose, which is exactly how pycapnp v2.0.0
    failed in run #1. 0.29.x generates compatible code.

    The build.patch additionally removes the `import pkgconfig` (absent in the
    p4a image) and forces the system-libcapnp path, since p4a's 'capnp' recipe
    already provides libcapnp and exports its dirs through CXXFLAGS.
    """

    url = 'git+https://github.com/capnproto/pycapnp.git'
    version = '6aa59476a021cef601b0a7193a8bc7573aad31c0'  # v1.1.0
    depends = ['python3', 'capnp']
    hostpython_prerequisites = ['setuptools', 'cython==0.29.37']
    call_hostpython_via_targetpython = False
    patches = ['build.patch']

    # pycapnp's capnp/lib/capnp.pyx is C++ throughout (it uses libc++, STL
    # containers and `cdef extern from *` C++ helpers). p4a's cythonize_file()
    # shells out to `cython <file>` with only self.cython_args appended and
    # never passes --cplus, so the default C mode fails with:
    #   capnp/lib/capnp.pyx:3924:17: Operation only allowed in c++
    # These args are appended verbatim by recipe.py:1194.
    cython_args = ['-3', '--cplus', '-I', '.']

    def _ensure_host_deps(self, arch):
        """Make sure the host python has setuptools/wheel/cython for setup.py.

        Constraints learned from failed runs:

        * The p4a host python is configured --without-ensurepip, so it has no
          pip: `hostpython -m pip` fails with "No module named pip". Bootstrap
          through the building interpreter (which has pip) with --target.
        * --target + --upgrade alone does NOT replace an existing newer
          setuptools, so --ignore-installed is required.
        * numpy 1.22.3's bundled numpy.distutils calls
          compiler.cxx_compiler(), which newer setuptools removed:
              AttributeError: 'UnixCCompiler' object has no attribute 'cxx_compiler'
          numpy is built by its own recipe against the same site-packages, so
          the version we write here must also satisfy numpy -> setuptools<70.
        * A recipe-private dir instead (tried first) leaked into every later
          recipe's environment via PYTHONPATH and hid packages they need
          (pyzmq lost `packaging`). One shared location is simpler and correct.
        """
        import sys
        import sh
        from pythonforandroid.toolchain import shprint

        host_site = join(dirname(self.hostpython_location), 'Lib', 'site-packages')
        ensure_dir(host_site)

        env = self.get_recipe_env(arch)
        env.pop('PYTHONPATH', None)
        # Cython must stay 0.29.x: Cython 3 emits CPython private symbols
        # (_PyDict_SetItem_KnownHash, _PyInterpreterState_GetConfig) that p4a's
        # headers do not expose.
        # setuptools range, not an exact pin:
        #   51.3.3 (p4a's own recipe version) is too old -- pyzmq's setup.py does
        #     from setuptools.errors import OptionError
        #   which only exists in newer setuptools.
        #   >=70 is too new -- numpy 1.22.3's bundled numpy.distutils calls
        #     compiler.cxx_compiler()
        #   which modern distutils dropped.
        # Anything in between works for both. numpy keeps its own recipe; we
        # additionally patch it (see recipes/numpy) so it tolerates this.
        shprint(sh.Command(sys.executable), '-m', 'pip', 'install', '-q',
                '--disable-pip-version-check', '--upgrade', '--ignore-installed',
                '--target', host_site,
                'setuptools<70,>=60', 'wheel', 'packaging', 'cython==0.29.37',
                _env=env)
        info("host deps prepared in {}".format(host_site))

    def build_arch(self, arch):
        self._ensure_host_deps(arch)
        super().build_arch(arch)

    def cythonize_build(self, env, build_dir="."):
        """Cythonize only the real .pyx files.

        p4a's default walks the tree with fnmatch("*.pyx"), which also picks up
        capnp/templates/module.pyx. That file is a Jinja2 template, not Cython
        source, so cython fails on it:

            capnp/templates/module.pyx:10:1: Expected an identifier or literal
            {% macro getter(field, type) -%}

        pycapnp's own setup.py templates it into capnp/lib/capnp.pyx at build
        time, so we only translate that generated file (plus the standalone
        benchmark helper, which is harmless but skip-worthy).
        """
        from os.path import join
        info("Cythonizing capnp/lib/capnp.pyx only (skipping Jinja2 templates)")
        self.cythonize_file(env, build_dir, join(build_dir, "capnp/lib/capnp.pyx"))

    def get_recipe_env(self, arch, **kwargs):
        env = super().get_recipe_env(arch)
        capnp_dir = Recipe.get_recipe("capnp", self.ctx).get_build_dir(arch.arch)
        env['CXXFLAGS'] += ' -I{}/c++/src/'.format(capnp_dir)
        env['CXXFLAGS'] += ' -L{}/c++/.libs/'.format(capnp_dir)
        # Cython's SetNameInClass utility calls _PyDict_SetItem_KnownHash on
        # PY_VERSION_HEX >= 0x030500A1 (i.e. every Python we care about). That
        # symbol is declared only in the *private* pycore_dict.h, which the p4a
        # python3 recipe does not install, so the generated C++ fails with
        #   error: use of undeclared identifier '_PyDict_SetItem_KnownHash'
        # (this is not a Cython 3 symptom -- 0.29.33/0.29.37 emit it too, and
        # p4a's python is 3.14 where the symbol is gone entirely).
        # Force-include a shim that maps it onto the public PyDict_SetItem().
        shim = join(dirname(__file__), 'cython_compat.h')
        env['CXXFLAGS'] += ' -include {}'.format(shim)
        # pycapnp compiles capnp/helpers/capabilityHelper.cpp through distutils'
        # plain `clang` (not clang++), and that path only inherits CFLAGS. It
        # therefore needs the capnp include dir too, otherwise:
        #   ./capnp/helpers/capabilityHelper.h:3:10:
        #     fatal error: 'capnp/dynamic.h' file not found
        # CXXFLAGS alone does not reach it.
        env['CFLAGS'] += ' -include {}'.format(shim)
        env['CFLAGS'] += ' -I{}/c++/src/'.format(capnp_dir)
        return env


recipe = PycapnpRecipe()
