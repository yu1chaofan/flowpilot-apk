#!/bin/bash
# Rebuild the `flowy/src` symlink farm after a plain `git clone`.
#
# Why this exists: upstream flowy/src/{selfdrive,cereal,common,opendbc,panda,...}
# are symlinks to ../../<dir>. Git can store symlinks, but the API-pushed tree
# cannot represent them and a naive `cp -r` of a symlink dir would flatten it.
# Instead the toplevel content is shipped normally and the link map lives in
# `.flowy-src-links` (path<TAB>target). buildozer copies `source.dir = src`
# with copytree(symlinks=False), so real directories are what it wants -- a
# link would be dereferenced at copy time anyway. We materialise real dirs.
set -euo pipefail

ROOT="${1:-$(pwd)}"
MANIFEST="$ROOT/.flowy-src-links"

if [ ! -f "$MANIFEST" ]; then
  echo "!! no $MANIFEST -- nothing to relink"
  exit 1
fi

cd "$ROOT"
# 1. Drop the placeholders (empty dirs or stale links) so `cp -r` cannot nest
#    them inside the real content it is about to copy.
while IFS=$'\t' read -r rel target; do
  [ -z "$rel" ] && continue
  case "$rel" in \#*) continue ;; esac
  if [ -L "$rel" ] || [ -e "$rel" ]; then
    rm -rf "$rel"
  fi
done < "$MANIFEST"

# 2. Materialise. `cp -rL` dereferences at the source end (the toplevel dirs are
#    real), which is what we want; the link text is only used to locate it.
COUNT=0
while IFS=$'\t' read -r rel target; do
  [ -z "$rel" ] && continue
  case "$rel" in \#*) continue ;; esac
  # target is relative to the directory containing $rel
  parent="$(dirname "$rel")"
  resolved="$(cd "$parent" && cd "$target" && pwd)"
  mkdir -p "$(dirname "$rel")"
  cp -rL "$resolved" "$rel"
  echo "  relinked $rel -> $resolved"
  COUNT=$((COUNT + 1))
done < "$MANIFEST"

echo "[relink] $COUNT entries materialised under $ROOT"
ls -la flowy/src/ 2>/dev/null | head -20 || true
