# docx4j-markdown

Markdown import (markdown→docx) and export (docx→markdown) for docx4j.

- **Import**: walks the [commonmark-java](https://github.com/commonmark/commonmark-java)
  AST directly into WordprocessingML — real heading/quote/code styles, real
  list numbering, real footnotes; no HTML detour.  Entry point:
  `org.docx4j.markdown.MarkdownImporter`.
- **Export**: since 17.3.4 the text is docx4j's text model's
  (`org.docx4j.model.text.Texts`, CR-046): its block model is written as a
  commonmark AST which commonmark-java's `MarkdownRenderer` serializes (so
  markdown escaping is handled by the reference implementation).  So the
  Markdown says what the plain text says: the same view, the same stories, the
  same hidden text left out.  Entry point: `org.docx4j.markdown.MarkdownExporter`.

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
`\begin{matrix}` (`pmatrix`/`bmatrix`) with `&` cells ·
`\boxed` · `\xrightarrow`/`\xleftarrow`/`\overset`/`\stackrel`/`\underset` ·
`\underbrace`/`\overbrace` · `\not` ·
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
outside the subset is its runs' text inside the delimiters (the text model's
rule, WD REQ-604).
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
        .setView(TextView.MARKUP));   // CriticMarkup: {++added++}{--deleted--}

// the text model's options, under the WD's names: stories, hidden text, metadata ...
MarkdownExportOptions options = new MarkdownExportOptions();
options.getTextOptions().include(TextOptions.Include.HEADERS).metadata(true);  // YAML front matter
// ... or one reading, written as plain text and as Markdown:
TextResult result = Texts.of(pkg, TextOptions.of(TextView.ACCEPTED));
String text = PlainText.of(result), md = new MarkdownExporter().export(result, pkg);
```

Tracked changes are written in the text model's views: `accepted` (the
default: as Word's Accept All leaves the document), `original` (Reject All), or
`markup`, CriticMarkup's `{++ ++}`, `{-- --}` and `{>> <<}` for comments.
Before 17.3.4 the markup policy struck deletions through with `~~`;
`setTrackedChangesPolicy` is now a deprecated alias of `setView`.

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
result text (a HYPERLINK field a link), content controls their content (or
their bound value), a text box its paragraphs after the paragraph it is
anchored in; headers and footers are left out unless the text options include
them, and so is a table of contents unless `toc` is set.  Everything outside CommonMark+GFM degrades to the nearest construct
rather than inventing syntax.

A table style's formatting is not carried either: a cell's bold, italic and
alignment are its paragraphs' and runs' own (and their styles'), and the
`w:tblHeader` rows (else the first row) are the GFM header whatever the table
style makes of it.  GFM has no table
styling to carry it in.  (docx4j's `PropertyResolver` gives a cell paragraph's
formatting with its table style, given its `CellContext`, should that change:
CR-030 decision 4.)

## Dependencies

`org.commonmark:commonmark` and extension artifacts (BSD-2-Clause; see
`legals/`), docx4j-core.  docx4j-ImportXHTML is optional (reflection), used
only for the `IMPORT_XHTML` embedded-HTML policy.
