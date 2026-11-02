#!/usr/bin/env bash
# Runs the adapter that install.sh built.
set -euo pipefail
here="$(cd "$(dirname "$0")" && pwd)"
exec java -jar "$here/target/docx4j-platform-tests-adapter.jar" "$@"
