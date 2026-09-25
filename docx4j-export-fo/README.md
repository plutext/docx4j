# docx4j-export-FO

Export docx to PDF via XSL FO, using FOP

With docx4j 8, this maven module builds as an optional jar. (From 3.3.0 to 6.1.x, this was an optional separate project at https://github.com/plutext/docx4j-export-fo )

docx4j will use export-FO automatically if it finds it in your classpath.

## The FO renderer: Apache FOP 2.11, or the docx4j FO renderer

Two renderers are supported (docx4j CR-020):

* **Apache FOP 2.11** (`org.apache.xmlgraphics:fop`), the default dependency of this
  module today.
* **The docx4j FO renderer** (`org.docx4j:docx4j-fo-renderer`, `2.11-docx4j.1` on Maven
  Central since 2026-09-25): an upstream-tracking fork of Apache FOP 2.11 maintained at
  https://github.com/plutext/xmlgraphics-fop (branch `docx4j-2.11`), carrying the fixes
  docx4j found in FOP ahead of their upstream release and, from CR-020 phase 1, hooks for
  the Word layout rules that reflection cannot reach. `-Pfo-renderer-fork` builds this
  module against it; the default dependency stays Apache FOP until a minor release
  (17.3.0 at the earliest), because the switch replaces a transitive dependency for every
  consumer.

  **To switch**, replace `org.apache.xmlgraphics:fop` with
  `org.docx4j:docx4j-fo-renderer-core:2.11-docx4j.1` in your own dependencies and
  exclude Apache's `fop` and `fop-core` from `docx4j-export-fo` (the fork keeps Apache's
  packages, so the two must never share a classpath; `FopCapabilities` warns when they
  do). **What you get today, measured** (CR-020 §8): the same layout - the batch 49
  baselines on the two renderers agree on every document of a 449-document corpus and
  146 probes, because every hook-dependent rule has a reflective fallback on Apache FOP;
  a correct text layer for CJK ideographs that share a glyph with a Kangxi radical (no
  workaround exists for that on Apache FOP); and a renderer whose internals the Word
  layout managers are built against, so an Apache point release cannot break them. The
  fixes that change what a PDF *says* - ligatures published to ToUnicode as their letters
  rather than U+E000, and Word's ligature setting honoured per run - are the next fork
  release (`fop/CR-002`, `fop/CR-001`), and the point at which switching is worth
  recommending.

  > This is a modified distribution derived from Apache FOP 2.11. It is maintained by
  > Plutext/docx4j and is not an Apache Software Foundation release. Apache FOP is a
  > trademark of the Apache Software Foundation.

At start-up `org.docx4j.convert.out.fo.FopCapabilities` probes which one is present and
logs one line at INFO, e.g. `FO renderer: Apache FOP 2.11, hooks: none (...)` or
`FO renderer: docx4j-fo-renderer 2.11-docx4j.1, hooks: ...`. Every rule that needs a hook
of the fork asks `FopCapabilities.has(...)` and falls back on Apache FOP, so the rendering
on Apache FOP is what it was before the fork, and the line says which rules are off.

The fork keeps Apache's Java packages (`org.apache.fop`) and automatic module names, so a
consumer's own FOP extensions work on either. The corollary: **never put both on one
classpath**. The probe counts the copies of FOP it can see and warns naming each jar
(classpath order decides which wins, silently); the fix is to exclude
`org.apache.xmlgraphics:fop`, `fop-core`, `fop-events` and `fop-util` wherever another
dependency drags them in. It also warns when the FOP found is of another line than the
2.11 this module's layout managers subclass.

## Font jars

A PDF is laid out in the fonts the machine has. Where it has not got a document's font,
docx4j substitutes the closest open face it can find, and these optional jars are what it
finds on a machine with no fonts installed at all - which is what the stock ubuntu, debian,
fedora and alpine images are. Add the ones your documents need:

| jar | carries | substitutes for |
| --- | --- | --- |
| `docx4j-export-fo-fonts-liberation` | Liberation Serif / Sans / Mono | Times New Roman, Arial, Courier New |
| `docx4j-export-fo-fonts-croscore` | Tinos, Arimo, Cousine | the same three, metric-compatible |
| `docx4j-export-fo-fonts-crosextra` | Carlito, Caladea | Calibri, Cambria |
| `docx4j-export-fo-fonts-theme2023` | Akasia, Intos Display | **Aptos, Aptos Display** - the faces of Word's 2023 default theme |
| `docx4j-export-fo-fonts-symbol` | OpenSymbol | the Symbol and Wingdings bullets |

**Aptos and Aptos Display** are worth a word of their own, because they are what Word 365
gives every document that carries no theme of its own - and because Microsoft does not ship
them with Windows: they are cloud fonts, downloaded by an Office installation. So:

* Where your licence permits it - your own Windows or Mac, licensed for Office, and not a
  server - install Microsoft's Aptos. It is the real thing, and an installed font wins over
  any substitute by precedence.
* Otherwise add `docx4j-export-fo-fonts-theme2023`. Akasia and Intos Display are OFL 1.1
  clones drawn to Aptos's own metrics (all 138 advances identical, kerned line widths
  identical to 0.001pt against Aptos 2.01), so lines break where Word breaks them.
* With neither, docx4j falls back to Carlito, which is a few per cent out and can break a
  line elsewhere - and with no font jar at all FOP draws the text in a base-14 font and
  does not embed it.

## Alternatives

* documents4j: since 8.2.0, use Microsoft Word to do the conversion

* via-Microsoft-Graph: new in 8.2.3, use java-docx-to-pdf-using-Microsoft-Graph to do the conversion

For comparison of approaches, please see https://www.docx4java.org/blog/2020/09/office-pptxxlsxdocx-to-pdf-to-in-docx4j-8-2-3/