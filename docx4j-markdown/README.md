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
items, footnotes) plus YAML front matter.  Deliberately nothing more.

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
