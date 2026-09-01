# docx4j-markdown

Markdown import (markdown→docx) and export (docx→markdown) for docx4j.

- **Import**: walks the [commonmark-java](https://github.com/commonmark/commonmark-java)
  AST directly into WordprocessingML — real heading/quote/code styles, real
  list numbering, real footnotes; no HTML detour.  Entry point:
  `org.docx4j.markdown.MarkdownImporter`.
- **Export**: a TraversalUtil-based walker builds a commonmark AST which
  commonmark-java's `MarkdownRenderer` serializes (so markdown escaping is
  handled by the reference implementation).  Entry point:
  `org.docx4j.markdown.MarkdownExporter`.

Dialect: **CommonMark + the GFM extensions** (tables, strikethrough, task list
items, footnotes) plus YAML front matter and TeX math.  Deliberately nothing
more.

## Math

`$...$` (inline) and `$$ ... $$` (display) — also `\(...\)` and, at line
start, `\[ ... \]` (single-line or multi-line) — are translated to **native
OMML equations**, both directions.  The one unrecognized form is `\[...\]`
buried inside a running line of text: markdown's own escaping owns brackets
there, so use `$...$` for inline math.
The supported LaTeX subset is the contract (anything else falls back,
loudly — see below):

`\frac` (incl `\frac12`) · `_`/`^` scripts · `\sqrt[n]` · `\sum \prod \int
\iint \iiint \oint \bigcup \bigcap` with limits · `\left ( [ \{ | \langle
\lfloor \lceil . \right` with `\middle` · `\text` / `\textbf` / `\textit` ·
`\mathrm` / `{\rm}` / `\operatorname` · `\mathbf` / `\mathit` / `\mathcal` /
`\mathbb` / `\mathfrak` / `\bf` / `\it` · `\begin{aligned}`
(`align`/`align*`) and `\begin{cases}` with `\\` rows and `&` alignment ·
`\boxed` · `\xrightarrow`/`\xleftarrow`/`\overset`/`\stackrel` · `\not` ·
accents `\hat \tilde \bar \vec \dot \ddot \check \breve \acute \grave`
(incl `\widehat`/`\widetilde`) · `\overline`/`\underline` · greek (incl
var-forms) · ~70 operator/relation/arrow/set symbols · spacing `\, \; \:
\quad \qquad` and `\ ` · sizing prefixes `\big`…`\Biggr` (delimiter kept,
sizing dropped) · function names (`\sin`, `\log`, `\lim`, …) · escapes
`\{ \} \$ \% \& \# \_`.

**Nothing fails silently.**  An equation outside the subset degrades — whole,
delimiters preserved — to its literal source in the `CodeChar` style, and is
reported through `MarkdownImportOptions.setIssueListener(...)` (source +
reason; collect into a list for programmatic triage).  On export, OMML
outside the subset flattens to its text with a warning.
`MathPolicy.LITERAL` skips translation entirely;
`Extension.MATH` turns recognition off.

## Usage

```java
// markdown -> docx
WordprocessingMLPackage pkg = new MarkdownImporter().createPackage(markdown);
// ... or into your own styles-template docx:
new MarkdownImporter().importToMainDocumentPart(markdown, templatePkg);

// docx -> markdown
String md = new MarkdownExporter().export(pkg);

// options per call (never Docx4jProperties globals):
new MarkdownImporter(new MarkdownImportOptions()
        .setHtmlPolicy(MarkdownImportOptions.HtmlPolicy.LITERAL)
        .setImageHandler(new DefaultMarkdownImageHandler("/base/dir")));
new MarkdownExporter(new MarkdownExportOptions()
        .setImageDirPath("/out/images").setImageTargetUri("images")
        .setTrackedChangesPolicy(MarkdownExportOptions.TrackedChangesPolicy.MARKUP));
```

Convenience facade (docx4j-core), when docx4j-markdown is on the classpath:
`Docx4J.fromMarkdown(String)` and `Docx4J.toMarkdown(pkg)`.

## docx4j-markdown vs flexmark-java

[flexmark-java](https://github.com/vsch/flexmark-java) ships
`flexmark-docx-converter`, which is itself built on docx4j, and supports many
markdown dialects.  Use it if you need dialects beyond CommonMark+GFM.

docx4j-markdown is the first-party mapping: house styles via a styles template
docx, real numbering, real footnotes, supported alongside docx4j itself — and
it also offers the reverse direction (docx→markdown), which flexmark does not.

## Lossiness (by design)

Markdown has no styles, columns, sections, headers/footers or floating
objects.  Exporting a docx that uses them is lossy: fields become their cached
result text, content controls their content; headers/footers, textboxes and
VML are dropped.  Everything outside CommonMark+GFM degrades to the nearest
construct rather than inventing syntax.

## Dependencies

`org.commonmark:commonmark` and extension artifacts (BSD-2-Clause; see
`legals/`), docx4j-core.  docx4j-ImportXHTML is optional (reflection), used
only for the `IMPORT_XHTML` embedded-HTML policy.
