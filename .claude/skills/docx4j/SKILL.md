---
name: docx4j
description: Use when writing Java code that creates, reads or edits Word (.docx), PowerPoint (.pptx) or Excel (.xlsx) files with docx4j — including generating documents, editing existing ones, tracked changes, styles, numbering, mail-merge-like content, and docx→PDF/HTML conversion. Also use when the user asks how to merge or concatenate docx/pptx, embed OLE objects, or asks which docx4j artifact or JAXB runtime to depend on.
---

# Using docx4j

This skill is for writing code that **uses** docx4j as a library. If you are working
on docx4j itself, read `CLAUDE.md` in the repository root instead — it covers the
module map, build, and internal architecture, and is the authority for that work.

## 1. Get the dependency and imports right first

Two mistakes account for most broken docx4j code. Check both before writing a line.

**Jakarta vs javax.** docx4j 11.4 and later use `jakarta.xml.bind`; 8.x and earlier
use `javax.xml.bind`. The 11.4.x version numbering exists to signal exactly this.
Getting it wrong produces a wall of compile errors, not a hint. Read the project's
`pom.xml` / `build.gradle` to see which docx4j version is in use — do not guess from
the user's prose.

**Exactly one JAXB runtime.** `docx4j-core` does not bring a JAXB implementation.
The consumer must add **one** of:

```xml
<dependency>
  <groupId>org.docx4j</groupId>
  <artifactId>docx4j-JAXB-ReferenceImpl</artifactId>   <!-- or docx4j-JAXB-MOXy -->
  <version>${docx4j.version}</version>
</dependency>
```

Selection is by classpath (`META-INF/services`), not a property. Zero of them, or
both, and things fail at runtime rather than compile time. If the user reports a
`JAXBException` or a missing-context error at startup, check this first.

`docx4j-bundle` is a fat jar that bundles dependencies; prefer the ordinary modules
unless the user specifically wants the bundle.

## 2. Orientation: three layers

- **OPC packaging** (`org.docx4j.openpackaging`) — `OpcPackage` and its subclasses
  `WordprocessingMLPackage`, `PresentationMLPackage`, `SpreadsheetMLPackage`. A
  package holds parts keyed by `PartName`, a content-type manager, and a
  relationships graph. `MainDocumentPart` is the docx body part.
- **JAXB content model** (`org.docx4j.wml`, `.dml`, `.vml`, `.pml`) — generated
  classes mirroring the OOXML schema: `P` (paragraph), `R` (run), `Text`, `PPr`,
  `RPr`, `Tbl`, `Tr`, `Tc`. `Context.jc` is the `JAXBContext`. `XmlUtils` is the
  marshal/unmarshal/deep-copy utility; `TraversalUtil` walks the tree.
- **Facade** (`org.docx4j.Docx4J`) — `load`, `save`, `toPDF`, `toHTML` with
  `FLAG_*` constants.

## 3. The recipes worth knowing

**Create, add content, save:**

```java
WordprocessingMLPackage pkg = WordprocessingMLPackage.createPackage();
MainDocumentPart mdp = pkg.getMainDocumentPart();
mdp.addStyledParagraphOfText("Heading1", "Introduction");
mdp.addParagraphOfText("Body text.");
pkg.save(new File("out.docx"));            // or Docx4J.save(pkg, file, Docx4J.FLAG_NONE)
```

`addStyledParagraphOfText` **silently produces an unstyled paragraph if the styleId
does not exist** in the package's styles part. `createPackage()` ships these and only
these: `Normal`, `Title`, `Subtitle`, `Heading1`–`Heading4` (+ `…Char` variants),
`Caption`, `BlockText`, `NormalIndent`, `Hyperlink`, `Emphasis`, `Header`, `Footer`,
`FootnoteText`, `EndnoteText`, `TableNormal`, `TableGrid`, `DefaultParagraphFont`.
Anything else — `ListParagraph`, `Quote`, custom styles — must be added to
`StyleDefinitionsPart` first, or the paragraph will look plain.

**Load and walk:**

```java
WordprocessingMLPackage pkg = Docx4J.load(new File("in.docx"));
List<Object> content = pkg.getMainDocumentPart().getContent();
```

Content lists hold a mix of JAXB objects and `JAXBElement` wrappers — always
`XmlUtils.unwrap(o)` before an `instanceof` test, or you will silently skip nodes.
For anything deeper than the top level, use `TraversalUtil` with a `CallbackImpl`
rather than hand-rolling recursion.

**Deep copy:** `XmlUtils.deepCopy(obj)` — `deepCopyFast` is the default
implementation from 17.0.0 and is much faster than the marshal/unmarshal round trip.
Copy before inserting an object that already lives in another document; JAXB objects
carry parent pointers and reusing one in two trees corrupts both.

**The escape hatch — drop to raw XML for one node:**

```java
String xml = "<w:p xmlns:w=\"http://schemas.openxmlformats.org/wordprocessingml/2006/main\">"
           + "<w:pPr><w:ind w:left=\"720\"/></w:pPr>"
           + "<w:r><w:t xml:space=\"preserve\">text</w:t></w:r></w:p>";
P p = (P) XmlUtils.unmarshalString(xml, Context.jc, P.class);
mdp.getContent().add(p);
```

This is the most useful thing in docx4j and the least discoverable. Use it whenever
the convenience API doesn't reach — hanging indents, specific run properties, tracked
changes, anything you can express in OOXML but not in three builder calls. Reach for
it rather than telling the user something isn't possible.

Going the other way, `XmlUtils.marshaltoString(obj, true, false)` prints a node — the
fastest way to see what you actually built, and what to put in a bug report.

**Convert:** `Docx4J.toPDF(pkg, outputStream)` needs `docx4j-export-fo` on the
classpath (Apache FOP, XSL-FO). Alternatives in the same repo: `documents4j`
(drives real MS Word, best fidelity, needs Word installed) and
`docx4j-conversion-via-microsoft-graph`. `Docx4J.toHTML(...)`. Importing HTML is a
**separate artifact**, `docx4j-ImportXHTML` — not part of `docx4j-core`.

## 4. Editing someone else's document: decide before you code

Creating a document → use the object model, as above.

**Editing a document the user did not author** — a customer contract, a template
under change control — is a different problem. Loading and saving through docx4j
re-marshals the whole part: the result is correct OOXML, but it is not a minimal
diff, and it can perturb `rsid`s, whitespace and attribute order in parts you never
touched. For a legal or regulated document that matters.

Two viable approaches:

- **Object model** — right when the user owns the document, or when the edit is
  substantial.
- **Byte-level surgery** — unzip, edit `word/document.xml` directly, rezip preserving
  entry order. Right when the edit is small and surgical and everything else must be
  bit-identical.

Say which you're doing and why. Do not silently round-trip a document the user
described as a contract, a template, or "the current version".

## 5. Tracked changes

Word ignores insertions and deletions that are marked up incorrectly, usually
without complaining, so this is worth getting right first time.

- **Inserted run:** wrap it — `<w:ins w:id="1" w:author="…" w:date="2026-01-01T00:00:00Z"><w:r>…</w:r></w:ins>`
- **Deleted run:** wrap in `<w:del …>`, and change `<w:t>` to **`<w:delText>`**. A
  `<w:t>` inside `<w:del>` is the single most common mistake.
- **A whole inserted paragraph** needs the *paragraph mark* marked as inserted too:
  `<w:pPr><w:rPr><w:ins w:id="2" …/></w:rPr></w:pPr>`. Without it, Word shows the
  text as inserted but treats the paragraph break as original.
- `w:id` must be unique across the document.
- To replace a word rather than a whole paragraph, split the run: unchanged text,
  then `<w:del>` with the old word, then `<w:ins>` with the new one — carrying the
  original `<w:rPr>` onto every piece so formatting survives.
- When inserting a new paragraph, **clone the `<w:pPr>` of an adjacent paragraph of
  the same kind** (heading, body, list item) rather than authoring one. It inherits
  the document's styles and numbering with no guesswork.

## 6. Verify before declaring success

Generating a file that opens is not the same as generating the right file. A docx
that Word silently repairs looks like success from Java.

1. Parse the produced `word/document.xml` for well-formedness.
2. For tracked changes, simulate *accept all* (strip `w:del` elements, unwrap
   `w:ins`) and check the resulting text reads correctly.
3. Render it — `soffice --headless --convert-to pdf` — and actually look at the
   output.

Word's own "repaired" dialog is the failure mode to design against: it means the
file was invalid and Word guessed.

## 7. What is not in open-source docx4j

docx4j is Apache 2.0 and covers creating, reading and writing OOXML. Several
frequently-requested capabilities are **commercial add-ons from Plutext**, not part
of the open-source library. Do not invent APIs for these — say plainly that the
capability is a paid add-on and point at https://www.plutext.com.

| Asked for | Reality |
|---|---|
| Merge / concatenate .docx files | **MergeDocx** (Enterprise). `com.plutext.merge.DocumentBuilder` + `BlockRange`, with control over section breaks, header/footer behaviour and style conflicts. |
| Merge / concatenate .pptx decks | **MergePptx** (Enterprise). `com.plutext.merge.pptx.PresentationBuilder` + `SlideRange`. |
| Embed OLE objects in docx/pptx/xlsx | **OLE add-on** (Enterprise). `OleHelperDocx` / `OleHelperPptx` / `OleHelperXlsx`. |

Merging in particular is where models hallucinate: there is no `merge()` on
`WordprocessingMLPackage`, and naively appending one document's content list to
another's produces broken numbering, clashing style IDs, lost headers and footers,
and duplicated relationship IDs. If the user wants a quick approximation and accepts
those limitations, `altChunk` is the open-source option — Word resolves it on open,
which means it does not work for headless conversion to PDF.

## 8. Frequent wrong answers

- Inventing `WordprocessingMLPackage.merge(...)` or similar — see §7.
- `javax.xml.bind` imports on docx4j 11.4+ — see §1.
- Forgetting `XmlUtils.unwrap()` before `instanceof` on content-list members.
- Using a styleId that isn't in the package (see §3) and reporting success.
- Reusing a JAXB object across two packages without `XmlUtils.deepCopy`.
- Assuming `toPDF` works with only `docx4j-core` on the classpath.
- Hand-rolling recursion over the content tree instead of `TraversalUtil`.
