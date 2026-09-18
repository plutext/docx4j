# docx4j-export-FO

Export docx to PDF via XSL FO, using FOP

With docx4j 8, this maven module builds as an optional jar. (From 3.3.0 to 6.1.x, this was an optional separate project at https://github.com/plutext/docx4j-export-fo )

docx4j will use export-FO automatically if it finds it in your classpath.

## The FO renderer: Apache FOP 2.11, or the docx4j FO renderer

Two renderers are supported (docx4j CR-020):

* **Apache FOP 2.11** (`org.apache.xmlgraphics:fop`), the default dependency of this
  module today.
* **The docx4j FO renderer** (`org.docx4j:docx4j-fo-renderer`, recommended once released):
  an upstream-tracking fork of Apache FOP 2.11 maintained at
  https://github.com/plutext/xmlgraphics-fop (branch `docx4j-2.11`), carrying the fixes
  docx4j found in FOP ahead of their upstream release and, from CR-020 phase 1, hooks for
  the Word layout rules that reflection cannot reach. Until its first release it is a
  locally installed snapshot; `-Pfo-renderer-fork` builds this module against it.

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