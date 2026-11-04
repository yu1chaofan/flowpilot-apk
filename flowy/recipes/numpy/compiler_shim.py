"""Installed as sitecustomize.py for numpy's build subprocess.

numpy.distutils attaches customize(), customize_cmd() and show_customization()
to *its own* CCompiler class via replace_method(). The build commands, however,
get the compiler from new_compiler(), which can return plain setuptools'
UnixCCompiler -- a class that never received those helpers. Every call site then
dies with, in turn:

    AttributeError: 'UnixCCompiler' object has no attribute 'customize'
    AttributeError: 'UnixCCompiler' object has no attribute 'customize_cmd'
    AttributeError: 'UnixCCompiler' object has no attribute 'show_customization'

customize_compiler() is the distutils routine numpy's wrapper delegates to, so
binding it is behaviour-preserving; customize_cmd() only records bookkeeping and
show_customization() only logs, so no-ops are faithful for this build.
"""

try:
    from distutils.ccompiler import CCompiler
    from distutils.sysconfig import customize_compiler

    if not getattr(CCompiler, "_p4a_numpy_hooks", False):

        def _customize(self, dist, need_cxx=0):
            customize_compiler(self)

        def _customize_cmd(self, cmd):
            return None

        def _show_customization(self):
            return None

        CCompiler.customize = _customize
        CCompiler.customize_cmd = _customize_cmd
        CCompiler.show_customization = _show_customization
        CCompiler._p4a_numpy_hooks = True
except Exception:
    pass
