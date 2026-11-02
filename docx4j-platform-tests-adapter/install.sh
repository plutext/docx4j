#!/usr/bin/env bash
# Builds the docx4j adapter: one runnable jar holding docx4j (from Maven Central) and the adapter.
# Needs a JDK 11 or later and Maven on PATH.
set -euo pipefail
cd "$(dirname "$0")"
mvn -q -B -DskipTests package
bash run-adapter.sh --print-library-version
