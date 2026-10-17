# The understood namespaces

`understood-namespaces.txt` is the docx4j family's published understood set (WD REQ-306; docx4j
CR-046 section 2.2): **the namespaces whose content this engine's reader can reach**. A reader
selecting a branch of `mc:AlternateContent` for text takes the first `mc:Choice` each of whose
`Requires` prefixes resolves to a namespace in this set, else the `mc:Fallback` (WD REQ-302;
ISO/IEC 29500-3 section 9.3). In docx4j that is `McSelection`'s `TEXT` purpose. Drawing does not use
it: the FO and HTML exporters keep `docx4j.jaxb.mc.preferChoice` (CR-021 section 8.6).

The format is one absolute namespace URI per line, UTF-8, LF line endings, no comments and no blank
lines, so that every engine can read it with a split.

The first content (18 October 2026) is docx4j-core-ts's `UNDERSTOOD_NAMESPACES`
(`src/opc/mce/understood.mts` at 6b178af), 107 URIs, in its order. Eleven follow it, the namespaces
docx4j's JAXB contexts bind that core-ts's list lacks (CR-046 phase 1): `dgm1611` (2016/11 diagram),
`w16sdtdh`, `w16du` and `w16sdtfl` (the Word attributes and elements of CR-023 and CR-027), and the
SpreadsheetML `x14ac` (2009/9/ac) and revision namespaces `xr2`, `xr3`, `xr6`, `xr9`, `xr10` and
`xr16` (CR-022).

docx4j binds eight more namespaces that are not here, on purpose: the six OpenDoPE namespaces
(`http://opendope.org/...`), which only custom XML parts use, and custom XML is never text
(WD REQ-516); the package's content types; and the XML namespace itself.

Rules:

- Additions are made here first, then copied by each port (WD REQ-306). Each engine's tests assert
  its own set against its copy of this file: docx4j's `UnderstoodNamespacesTest`, core-ts's
  `mce.test.mjs`, docx4j-python's equivalent.
- A namespace belongs here when the reader can traverse its content to reach text: docx4j binds it
  in one of its JAXB contexts, or its Choice holds nothing but content docx4j binds or carries
  whole (core-ts's slicer, ink and chartex groups, whose Choice holds no text a Fallback would have
  given). A namespace whose content is lost or unreadable does not belong, whatever a producer gates
  with it: taking a Choice the reader cannot read loses the Fallback the producer wrote for exactly
  that reader.
- The set is about reading. Whether a Choice survives a load and a save is a different question
  (core-ts's rule for its preprocessor, CR-010), and so is whether docx4j can draw it.
