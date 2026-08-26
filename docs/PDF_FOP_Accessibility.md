# PDF accessibility (Tagged PDF), PDF/A and PDF/UA via Apache FOP

*Applies to the docx → XSL-FO → PDF pathway (`docx4j-export-fo`), which
currently uses Apache FOP 2.11.  Last reviewed against docx4j 17.0.4
(August 2026).*

Working sample:
[`ConvertOutPDFviaXSLFO.java`](../docx4j-samples-docx-export-fo/src/main/java/org/docx4j/samples/ConvertOutPDFviaXSLFO.java)
(in `docx4j-samples-docx-export-fo`), which contains this material in
runnable form.

## Where these settings go

The sample configures PDF output in three steps:

```java
FOSettings foSettings = new FOSettings(wordMLPackage);                            // step 1
FopFactoryBuilder fopFactoryBuilder = FORendererApacheFOP.getFopFactoryBuilder(foSettings);
FopFactory fopFactory = fopFactoryBuilder.build();                                // step 2
FOUserAgent foUserAgent = FORendererApacheFOP.getFOUserAgent(foSettings, fopFactory); // step 3
// ... configure foUserAgent ...
Docx4J.toFO(foSettings, outputStream, Docx4J.FLAG_EXPORT_PREFER_XSL);
```

The accessibility-related settings all live on the **`FOUserAgent`** (step 3).
`getFOUserAgent` stores the instance in `foSettings`, so what you configure
there is what `Docx4J.toFO` renders with.

## 1. Tagged PDF

FOP can produce "Tagged PDF": a logical structure tree plus marked content, so
screen readers and other assistive technology can establish reading order.
Reference: [FOP 2.11 accessibility](https://xmlgraphics.apache.org/fop/2.11/accessibility.html).

Programmatically:

```java
foUserAgent.setAccessibility(true);
```

This is the equivalent of the following in a `fop.xconf`:

```xml
<fop version="1.0">
   <accessibility>true</accessibility>
</fop>
```

The xconf form also takes optional attributes the boolean API can't express:

- `keep-empty-tags="false"` — drop empty blocks from the structure tree;
- `static-region-per-page="true"` — repeat header/footer content in the
  structure tree on each page (for screen readers).

## 2. PDF/A (archival)

Reference: [FOP 2.11 PDF/A](https://xmlgraphics.apache.org/fop/2.11/pdfa.html).
The profile is a renderer option:

```java
foUserAgent.getRendererOptions().put("pdf-a-mode", "PDF/A-1b");
```

Supported modes: `PDF/A-1a`, `PDF/A-1b`, `PDF/A-2a`, `PDF/A-2b`, `PDF/A-2u`,
`PDF/A-3a`, `PDF/A-3b`, `PDF/A-3u`.

- Conformance level **"b"** (basic) works without accessibility.
- Levels **"a"** (accessible) and **"u"** (unicode) additionally require
  `foUserAgent.setAccessibility(true)`, or FOP will object.
  (See further https://stackoverflow.com/a/54587413/1031689 )

PDF/A also requires **every font to be embedded** (docx4j's generated FOP
configuration embeds the fonts it maps), **prohibits PDF encryption**, and
PDF/A-1 forces PDF version 1.4.

## 3. PDF/UA (ISO 14289-1)

Also a renderer option; needs accessibility, plus a document title:

```java
foUserAgent.getRendererOptions().put("pdf-ua-mode", "PDF/UA-1");
foUserAgent.setAccessibility(true);
foUserAgent.setTitle("my title");
```

(The title could alternatively come from `dc:title` metadata in
`fo:declarations` — but docx4j does not emit that, so use `setTitle`.)

## What to expect from docx4j output in accessible modes

FOP derives the structure tree from the XSL-FO docx4j generates.  docx4j does
not currently emit:

- **`language`/`country` properties** — so with accessibility enabled, FOP
  raises the INFO event *"A piece of text or an image's alternate text is
  missing language information"* (`PDFEventProducer.unknownLanguage`) for the
  affected content;
- **`fox:alt-text`** on images — FOP's mechanism for alternate text (Word's
  alt text in `wp:docPr/@descr` is not carried across);
- **role hints** — structure types come from FOP's default mapping of FO
  elements.

The result is valid Tagged PDF, but tag quality is basic.  **Run a checker
over your output before claiming conformance** — eg [veraPDF](https://verapdf.org/)
for PDF/A, or the PDF Accessibility Checker (PAC) for PDF/UA.

To quieten the INFO events, register your own listener via
`foUserAgent.getEventBroadcaster().addEventListener(..)` and filter there.

## See also

- The sample: `docx4j-samples-docx-export-fo` /
  `org.docx4j.samples.ConvertOutPDFviaXSLFO`
- FOP configuration generally:
  https://xmlgraphics.apache.org/fop/2.11/configuration.html
- Font substitution for the FOP config docx4j generates: the docx4j property
  `docx4j.fonts.fop.util.FopConfigUtil.substitutions` (see the sample).
