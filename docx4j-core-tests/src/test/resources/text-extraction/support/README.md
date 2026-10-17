# Binary fixture and statement provenance

`vba-project.bin` is a rebuilt derivative of Apache POI's
`test-data/document/SimpleMacro.docm`, local POI checkout
`ae62bb5116b9aee19ebd5834e3a82066132c9f7f` (Apache License 2.0).
Original DOCM SHA-256:
`fd591958fcf5322f72c0a740e9606309c949254bda4c3d9bd966481ddf220563`.

`ProbeVbaProject` retains the project/module descriptions, rewrites module offsets,
removes compiled/performance caches, and replaces module sources with their attributes.
Module1 also has `Option Explicit` and the comment `' VBA51_MODULE_MARK`.
No executable procedures or event handlers remain. The generator and builder use the
bundled VBAMacroReader to verify the marker is in the decoded module and that every
source line is an attribute, Option Explicit, blank, or comment.

Rebuild with the compiled test classes and normal docx4j classpath:

```
java -cp <test-classes-and-dependencies> org.docx4j.textextraction.ProbeVbaProject \
  /path/to/poi/test-data/document/SimpleMacro.docm \
  docx4j-core-tests/src/test/resources/text-extraction/support/vba-project.bin
```

The output SHA-256 at generation was
`64af5e4741e9bae722a7074944ef4c79830e4ac035fc5231720e494bb4c963f0`.
The builder reads this committed support asset, so building the probes does not require
a POI checkout. The VBA project still needs Jason's Word-open check in its DOCM host.

`appendix-a-statements.tsv` transcribes WD 0.3 Appendix A (portfolio commit e8f14e1).
It is maintained separately from the generator so changing the case mapping cannot
silently change the test's reference. REQ numbers keep Appendix A's ordering.

OLE preview shape formulas follow the existing Word-authored
`src/test/resources/OLE/dragged txt.docx` fixture. The case 41 payload is generated
by docx4j's bundled Ole10Native/POIFS implementation and round-tripped through its reader.
The first ActiveX attempt used a hand-built `persistPropertyBag`; Word removed it when
repairing the document on 18 October 2026. The replacement uses the Word-authored Label1
control (`word/activeX/activeX3.bin`) from the existing `LegacyForms.docx` fixture.
`ProbeActiveXControl` changes only the compressed caption, its length, padding and enclosing
LabelControl byte count, preserving the size/font data and CompObj stream. The builder
registers the control's binary relationship using `persistStorage`. Its reader checks the
caption and byte count. No new external fixture is required.
