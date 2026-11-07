# The understood and traversal namespaces

Two published sets (WD REQ-306; docx4j CR-046 section 2.2), each one absolute namespace URI per line, UTF-8, LF line
endings, no comments and no blank lines, so that every engine can read them with a split.

**`understood-namespaces.txt`, the understood set:** the namespaces Word was measured to understand. 114 URIs, Word
16.0.20326.20232, 7 November 2026, a `mc:Choice`-only file in a main document part for each (the rounds `req305`,
`understood-choice` and `understood-list`). It decides every markup compatibility question in a word-processing package. A
reader selecting a branch of `mc:AlternateContent` for text takes the first `mc:Choice` each of whose `Requires` prefixes
resolves to a namespace in this set, else the `mc:Fallback` (WD REQ-302; ISO/IEC 29500-3 section 9.3). A namespace not
measured counts as not understood. In docx4j that is `McSelection`'s `TEXT` purpose, and through it the load-time splice, the
inspector and the rule for an element in a `w:t`. Drawing does not use it: the FO and HTML exporters keep
`docx4j.jaxb.mc.preferChoice` (CR-021 section 8.6).

**`traversal-namespaces.txt`, the traversal set:** the namespaces whose content this engine's reader can reach. 118 URIs. No
docx4j code reads it at run time: it is published for the ports, and `UnderstoodNamespacesTest` holds it to what docx4j's JAXB
contexts bind. Its first content (18 October 2026) is docx4j-core-ts's `UNDERSTOOD_NAMESPACES`
(`src/opc/mce/understood.mts` at 6b178af), 107 URIs, in its order. Eleven follow it, the namespaces docx4j's JAXB contexts
bind that core-ts's list lacks (CR-046 phase 1): `dgm1611` (2016/11 diagram), `w16sdtdh`, `w16du` and `w16sdtfl` (the Word
attributes and elements of CR-023 and CR-027), and the SpreadsheetML `x14ac` (2009/9/ac) and revision namespaces `xr2`, `xr3`,
`xr6`, `xr9`, `xr10` and `xr16` (CR-022).

**Where they differ:**

- Thirteen are in the traversal set alone: a reader reads them in their own parts, and Word doesn't understand them. They are
  the extended, custom and core properties, `docPropsVTypes`, `dc` and `dcterms`, `coverPageProps`, the three encryption
  namespaces, `urn:schemas-microsoft-com:office:excel`, `urn:schemas-microsoft-com:office:powerpoint` and
  `office/excel/2008/2/main`.
- Nine are in the understood set alone: `wpi`, `oel`, `cx3`, `cx4`, `cx6`, `cx7`, `c16r2`, `ax` and `xsi`.
- `cx5`, `cx8`, `mo`, `arto` and `dcmitype` are in neither. Word showed neither branch, though it declares `cx5` and `cx8` on
  its own roots.

docx4j binds eight more namespaces that are in neither, on purpose: the six OpenDoPE namespaces (`http://opendope.org/...`),
which only custom XML parts use, and custom XML is never text (WD REQ-516); the package's content types; and the XML namespace
itself.

Rules:

- Additions are made here first, then copied by each port (WD REQ-306). Each engine's tests assert its own sets against its
  copy of these files: docx4j's `UnderstoodNamespacesTest`, core-ts's `mce.test.mjs`, docx4j-python's equivalent.
- A namespace joins the understood set when Word has been measured to understand it, and only then.
- A namespace belongs in the traversal set when the reader can traverse its content to reach text: docx4j binds it in one of
  its JAXB contexts, or its Choice holds nothing but content docx4j binds or carries whole (core-ts's slicer, ink and chartex
  groups, whose Choice holds no text a Fallback would have given).
- The sets are about reading. Whether a Choice survives a load and a save is a different question (core-ts's rule for its
  preprocessor, CR-010), and so is whether docx4j can draw it.
