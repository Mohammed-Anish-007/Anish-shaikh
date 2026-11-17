#!/usr/bin/env bash
# Downloads the Vosk models into app/src/main/assets/models and verifies their SHA-256
# against models.sha256 (supply-chain protection). Used by CI and for local builds.
set -euo pipefail
cd "$(dirname "$0")/.."
DEST=app/src/main/assets/models
mkdir -p "$DEST"
while read -r sha name; do
  case "$sha" in ''|\#*) continue ;; esac
  f="$DEST/$name.zip"
  if [ ! -f "$f" ]; then
    echo "Downloading $name…"
    curl -fsSL --retry 3 -o "$f" "https://alphacephei.com/vosk/models/$name.zip"
  fi
  actual=$(sha256sum "$f" | cut -d' ' -f1)
  echo "$name sha256=$actual"
  if [ "$sha" != "-" ] && [ "$sha" != "$actual" ]; then
    echo "::error::Checksum mismatch for $name (expected $sha). Refusing to build."
    exit 1
  fi
done < models.sha256
