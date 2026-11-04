from pythonforandroid.recipe import Recipe
from pythonforandroid.logger import shprint
from pythonforandroid.util import current_directory
from os.path import join, basename
import sh


class CapnpRecipe(Recipe):
    # capnproto >= 1.0 made CallContext::allowCancellation() a KJ_UNAVAILABLE
    # stub -- calling it is a hard compile error ("As of Cap'n Proto 1.0,
    # allowCancellation must be applied statically using an annotation"). It is
    # a *live* method only up to and including 0.10.x, which is why pycapnp's
    # generated bindings call it:
    #
    #   capnp/lib/capnp.cpp:44100:28: error: no member named 'allowCancellation'
    #     in 'capnp::CallContext<capnp::DynamicStruct, capnp::DynamicStruct>'
    #
    # Use v0.10.4: the newest release where the call is valid (verified against
    # capability.h in 0.9.1, 0.10.4, 1.1.0 and 1.3.0).
    version = 'a91ec65323c2577732224d0cf6f5bf1e79d3a724'  # v0.10.4
    url = 'git+https://github.com/capnproto/capnproto.git'
    patches = ['path.patch']

    def get_recipe_env(self, arch, **kwargs):
        env = super().get_recipe_env(arch)
        env['ANDROID_NDK_ROOT'] = self.ctx.ndk_dir
        env['NDK_VERSION'] = 'android-ndk-r25b'
        env['PATH'] = '{}:{}'.format(self.ctx.ndk.llvm_prebuilt_dir, env['PATH'])
        return env

    def build_arch(self, arch):
        env = self.get_recipe_env(arch)

        curdir = join(self.get_build_dir(arch.arch), "c++")

        with current_directory(curdir):
            bash = sh.Command('bash')
            shprint(sh.autoreconf, '-i', _env=env)
            shprint(bash,
                './configure',
                '--host=arm-linux-androideabi',
                'CC={}-clang'.format(arch.target),
                'CXX={}-clang++'.format(arch.target),
                '--with-external-capnp',
                '--without-fibers',
                # Without this autoconf builds libcapnp/.a only, so .libs ends
                # up with no lib*.so.* at all -- which is exactly what the
                # explicit check below reported after the version bump.
                '--enable-shared',
                '--disable-static',
                'CXXFLAGS=-fPIC -fPIE -shared -mno-outline-atomics',
                'LIBS=-ldl',
                _env=env)
            shprint(sh.make, _env=env)
            # Copy the freshly linked shared objects out of .libs.
            #
            # Three traps here, each observed in a CI round:
            #  1. The version in the .so name tracks the capnproto release, so a
            #     hardcoded 'lib*-1.0.1.so' glob silently matches nothing after a
            #     version bump; pycapnp then compiles against stale headers and
            #     fails with "no member named 'allowCancellation'".
            #  2. autoconf needs --enable-shared, otherwise only .a files exist.
            #  3. sh.glob() proved unreliable here -- it returned nothing even
            #     with an absolute pattern, while libtool had demonstrably
            #     linked libkj.so/libcapnp.so into that very directory. Use the
            #     stdlib instead; it has no cwd/ quoting surprises.
            import os as _os

            libs_dir = join(curdir, '.libs')
            if not _os.path.isdir(libs_dir):
                raise RuntimeError("capnp: {} does not exist".format(libs_dir))
            real = []
            for entry in sorted(_os.listdir(libs_dir)):
                path = join(libs_dir, entry)
                # Capnproto names its shared objects with a dash, not a dot:
                #     libcapnp-1.1.0.so, libkj-async-1.1.0.so
                # (not libcapnp.so.1.1.0). Only the .so suffix carries meaning
                # here; anything else in this dir is .la/.lai libtool metadata
                # or the capnpc executables.
                if not entry.startswith('lib') or not entry.endswith('.so'):
                    continue
                if _os.path.islink(path):
                    continue
                real.append(path)
            if not real:
                raise RuntimeError(
                    "capnp: no built libraries in {} (contents: {})".format(
                        libs_dir, sorted(_os.listdir(libs_dir))[:20]))
            for lib in real:
                name = _os.path.basename(lib)
                shprint(sh.cp, '-a', lib, self.ctx.get_libs_dir(arch.arch))
                # Publish the unversioned name so biglink can resolve
                # -lcapnp / -lkj / -lkj-async: strip the -<version> part.
                stem, _, _ = name.rpartition('-')
                if not stem:
                    stem = name[:-3]
                shprint(sh.cp, '-a', lib, join(self.ctx.get_libs_dir(arch.arch),
                                               stem + '.so'))

recipe = CapnpRecipe()