#!/usr/bin/env sh
set -eu

REPO_ROOT="$(CDPATH= cd -- "$(dirname -- "$0")/.." && pwd)"
PROPS_PATH="$REPO_ROOT/.mvn/wrapper/maven-wrapper.properties"
JAR_PATH="$REPO_ROOT/.mvn/wrapper/maven-wrapper.jar"

if [ ! -f "$PROPS_PATH" ]; then
  echo "Missing $PROPS_PATH" >&2
  exit 1
fi

WRAPPER_URL="$(grep '^wrapperUrl=' "$PROPS_PATH" | head -n 1 | sed 's/^wrapperUrl=//')"
if [ -z "$WRAPPER_URL" ]; then
  echo "wrapperUrl not found in $PROPS_PATH" >&2
  exit 1
fi

mkdir -p "$(dirname "$JAR_PATH")"

echo "Downloading Maven Wrapper jar..."
echo "  From: $WRAPPER_URL"
echo "  To:   $JAR_PATH"

if command -v curl >/dev/null 2>&1; then
  curl -fsSL "$WRAPPER_URL" -o "$JAR_PATH"
elif command -v wget >/dev/null 2>&1; then
  wget -qO "$JAR_PATH" "$WRAPPER_URL"
else
  echo "Need curl or wget to download wrapper jar." >&2
  exit 1
fi

echo "Done."


