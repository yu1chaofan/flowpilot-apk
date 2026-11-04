#!/bin/bash

source ~/.venv/bin/activate

# Function to build flowy
build_flowy() {
  echo "Building flowy..."
  cd flowy
  CI=1 buildozer android debug
  rc=$?
  cd -
  if [ $rc -ne 0 ]; then
    echo "!!! buildozer failed with rc=$rc"
    echo "!!! last 120 lines of the python-for-android build log:"
    find flowy/.buildozer/android/platform -name "*.log" -o -type d -name "*build*" 2>/dev/null | head -5
    exit $rc
  fi

  # The AAR name is produced by p4a from the dist name; do not assume it.
  AAR=$(ls -1 flowy/bin/*.aar 2>/dev/null | head -1)
  if [ -z "$AAR" ]; then
    echo "!!! buildozer reported success but produced no .aar in flowy/bin/"
    ls -la flowy/bin/ 2>/dev/null || true
    exit 1
  fi
  echo "using AAR: $AAR"
  cp "$AAR" android/libs/openpilot-0.1-arm64-v8a-debug.aar
}

# Function to build with scons
build_scons() {
  echo "Building with scons..."
  ## scons/SConscript shell out to git to stamp the build; the docker bind mount
  ## makes /work owned by a different uid than root, so git refuses with
  ## "detected dubious ownership" and the build continues with a bogus version.
  git config --global --add safe.directory /work 2>/dev/null || true
  ## Resolve the NDK buildozer actually downloaded (upstream hardcoded r25b; p4a develop now pulls r28c)
  ## NOTE: the -d glob also matches the downloaded android-ndk-r28c-linux.zip,
  ## and sort -V puts ".zip" after the directory, so tail -1 used to pick the
  ## archive and every NDK call failed with "Not a directory". Filter to
  ## directories that actually contain ndk-build before sorting.
  NDK_DIR=$(for d in ~/.buildozer/android/platform/android-ndk-r*/; do
             [ -x "${d}ndk-build" ] && echo "${d%/}"
           done | sort -V | tail -1)
  NDK_TOOLCHAIN="$NDK_DIR/toolchains/llvm/prebuilt/linux-x86_64"
  echo "Using NDK: $NDK_DIR"
  ## libusb
  "$NDK_DIR/ndk-build" -C third_party/libusb/android/jni USE_PC_NAME=1
  ## libjson11
  "$NDK_TOOLCHAIN/bin/aarch64-linux-android30-clang++" -shared third_party/json11/json11.cpp -fPIC -O3 -o third_party/json11/libjson11.so
  ## liblmdb
  cd third_party/lmdb
  make
  cd -

  # Build openpilot
  scons .

  # Move libs
  cp third_party/libusb/android/libs/arm64-v8a/libusb-1.0.so android/libs/arm64-v8a
  cp third_party/lmdb/liblmdb.so android/libs/arm64-v8a
  cp third_party/json11/libjson11.so android/libs/arm64-v8a
  cp wrappers/libpandaflash.so android/libs/arm64-v8a
  cp wrappers/libthneedrunner.so android/libs/arm64-v8a
  cp cereal/libmessaging.so android/libs/arm64-v8a
  cp selfdrive/modeld/libmodelparser.so android/libs/arm64-v8a
  cp selfdrive/boardd/libpandad.so android/libs/arm64-v8a
}

# Function to build the app
build_app() {
  echo "Building the app..."
  ANDROID_HOME="$HOME/.buildozer/android/platform/android-sdk" ./gradlew assembleRelease
}

# Function to build everything
build_full() {
  echo "Building everything..."

  build_scons
  build_flowy
  build_app
}

run() {
  "$@"
}

# The first build is slow because it bootstraps the Python environment.
# Only do that for targets that actually need buildozer: doing it for `scons`
# made the staged `native` job silently run the whole buildozer pipeline and
# then fail on it, even though scons alone does not need it at all.
build_target="$1"
shift

# Check if an argument is provided
if [ -z "$build_target" ]; then
  echo "Usage: $0 {full|scons|flowy|app}"
  exit 1
fi

# Call the appropriate function based on the argument
case "$build_target" in
  full)
    build_full
    ;;
  scons)
    build_scons
    ;;
  flowy)
    build_flowy
    ;;
  app)
    build_app
    ;;
  run)
    run "$@"
    ;;
  *)
    echo "Invalid argument: $1"
    echo "Usage: $0 {full|scons|flowy|app}"
    exit 1
    ;;
esac

echo "Build complete!"