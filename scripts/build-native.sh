#!/bin/bash
# Native (scons) stage, extracted from scripts/build.sh's build_scons() so it can
# run on a bare runner instead of inside the `builder` docker image.
#
# Differences from the original, each one deliberate:
#   * NDK discovery: upstream globbed android-ndk-r* and took the last after
#     sort -V, which also matches the downloaded *.zip and made every NDK call
#     fail with "Not a directory". Here we require an actual ndk-build binary.
#   * The toolchain prebuilt dir is host-arch dependent; the original hardcoded
#     linux-x86_64, which breaks on an arm64 runner.
#   * `git config --global --add safe.directory` guards the SConscript git
#     stamp when the checkout is owned by another uid.
set -uo pipefail

ROOT="${1:-$(pwd)}"
cd "$ROOT" || exit 1

git config --global --add safe.directory "$ROOT" 2>/dev/null || true

# ---------------------------------------------------------------- NDK lookup
NDK_DIR=""
for d in "$HOME"/.buildozer/android/platform/android-ndk-r*/; do
  [ -x "${d}ndk-build" ] && NDK_DIR="${d%/}"
done
if [ -z "$NDK_DIR" ]; then
  echo "!! no NDK with ndk-build found under ~/.buildozer/android/platform"
  ls -la "$HOME"/.buildozer/android/platform/ 2>/dev/null || true
  exit 1
fi
case "$(uname -m)" in
  aarch64|arm64) HOST_TAG=linux-aarch64 ;;
  *)             HOST_TAG=linux-x86_64 ;;
esac
TOOLCHAIN="$NDK_DIR/toolchains/llvm/prebuilt/$HOST_TAG"
echo "NDK:       $NDK_DIR"
echo "toolchain: $TOOLCHAIN"
export NDK_DIR TOOLCHAIN

# ------------------------------------------------------------ libusb (jni)
echo "== libusb"
"$NDK_DIR/ndk-build" -C third_party/libusb/android/jni USE_PC_NAME=1 || exit 1

# ---------------------------------------------------------------- libjson11
echo "== libjson11"
"$TOOLCHAIN/bin/aarch64-linux-android30-clang++" \
  -shared third_party/json11/json11.cpp -fPIC -O3 \
  -o third_party/json11/libjson11.so || exit 1

# ---------------------------------------------------------------- liblmdb
echo "== liblmdb"
make -C third_party/lmdb || exit 1

# ------------------------------------------------------------------ openpilot
echo "== scons"
scons -j"$(nproc)" . || exit 1

# --------------------------------------------------------- collect artefacts
echo "== collect"
mkdir -p android/libs/arm64-v8a
copy() {
  if [ -e "$1" ]; then
    cp "$1" android/libs/arm64-v8a/ && echo "   + $(basename "$1")"
  else
    echo "   ! missing: $1"
  fi
}
copy third_party/libusb/android/libs/arm64-v8a/libusb-1.0.so
copy third_party/lmdb/liblmdb.so
copy third_party/json11/libjson11.so
copy wrappers/libpandaflash.so
copy wrappers/libthneedrunner.so
copy cereal/libmessaging.so
copy selfdrive/modeld/libmodelparser.so
copy selfdrive/boardd/libpandad.so

echo "== android/libs/arm64-v8a"
ls -la android/libs/arm64-v8a
