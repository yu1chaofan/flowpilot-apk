from os.path import join, dirname, exists
from pythonforandroid.util import current_directory, ensure_dir
from pythonforandroid.recipe import Recipe
from pythonforandroid.toolchain import shprint
import sh


class AcadosRecipe(Recipe):
    # p4a's download() runs `git fetch --tags --depth 1` (recipe.py:270) with no
    # refspec and then `git checkout <version>`. A shallow fetch does not bring
    # down tag objects, so checking out the tag name fails with:
    #   error: pathspec 'v0.2.2' did not match any file(s) known to git
    # Pinning the commit SHA that v0.2.2 points at sidesteps this entirely --
    # a SHA is fetched by --depth 1 and is always resolvable.
    version = '8af9b0ad180940ef611884574a0b27a43504311d'
    url = 'git+https://github.com/acados/acados.git'
    # NDK r28c clang treats implicit function declarations as errors; qpOASES 3.1embedded
    # calls malloc() without including <stdlib.h>. MessageHandling.h is pulled in by every
    # qpOASES source through Utils.h, so one include fixes all 6 malloc sites.
    patches = ['patches/qpoases_stdlib.patch']
    generated_libraries = [
        'acados/libacados.so',
        'external/blasfeo/libblasfeo.so',
        'external/qpoases/lib/libqpOASES_e.so',
        'external/hpipm/libhpipm.so',
    ]

    def build_arch(self, arch, **kwargs):        
        build_dir = join(self.get_build_dir(arch.arch), 'build')
        ensure_dir(build_dir)
        with current_directory(build_dir):
            env = self.get_recipe_env(arch)

            shprint(sh.cmake,
                    '-DANDROID_ABI={}'.format(arch.arch),
                    '-DANDROID_NATIVE_API_LEVEL={}'.format(self.ctx.ndk_api),
                    '-DCMAKE_TOOLCHAIN_FILE={}'.format(
                        join(self.ctx.ndk_dir, 'build', 'cmake',
                             'android.toolchain.cmake')),
                    '-DCMAKE_ASM_FLAGS=-DOS_LINUX',
                    '-DACADOS_WITH_QPOASES=ON',
                    '-UBLASFEO_TARGET',
                    '-DBLASFEO_TARGET=ARMV8A_ARM_CORTEX_A57',
                    # NDK r28c clang: -std=c99 hides posix_memalign -> implicit decl error in
                    # blasfeo_stdlib.c; qpOASES 3.1 also trips on older -Werror defaults.
                    # _POSIX_C_SOURCE=200112L exposes posix_memalign under -std=c99.
                    '-DCMAKE_C_FLAGS=-D_POSIX_C_SOURCE=200112L',

                    '..',
                    _env=env)
            shprint(sh.make)
            
            sh.cp('-a', *self.generated_libraries, self.ctx.get_libs_dir(arch.arch))

recipe = AcadosRecipe()