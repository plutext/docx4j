#!/usr/bin/env bash
set -euo pipefail
if [[ $# != 2 ]]; then
  echo 'Usage from repository root: run-phase0.sh <dependency-classpath> <scratch-output-directory>' >&2
  exit 2
fi
probeClasspath=$1
probeOutput=$2
probeSources=docx4j-core-tests/src/test/java/org/docx4j/textextraction
probeClasses=$(mktemp -d "${TMPDIR:-/tmp}/cr046-classes.XXXXXX")
trap 'rm -rf "$probeClasses"' EXIT
javac --release 11 -cp "$probeClasspath" -d "$probeClasses" \
  "$probeSources/ExtractionProbeBuilder.java" \
  "$probeSources/ExtractionPhase0Cases.java" \
  "$probeSources/ExtractionAdditionalCases.java" \
  "$probeSources/ExtractionFixtureChecks.java" \
  "$probeSources/ProbeVbaProject.java" \
  "$probeSources/ProbeActiveXControl.java"
java -Djava.awt.headless=true -cp "$probeClasses:$probeClasspath" \
  org.docx4j.textextraction.ExtractionProbeBuilder "$probeOutput"
