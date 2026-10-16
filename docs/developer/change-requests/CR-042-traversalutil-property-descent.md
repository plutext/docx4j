# CR-042: `TraversalUtil` into the property objects - an opt-in descent into `w:pPr`, `w:rPr`, `w:tblPr`, `w:tcPr` and DrawingML properties, for a callback that rewrites attributes

Status: PROPOSED 2026-10-14 (drafted in the Enterprise session at Jason Harrop's direction,
after enterprise CR-003 phase 4 had to write its own reflective walk; handed to the docx4j
session to add here). Drafted with Claude Fable 5.1. Owner: Jason Harrop. Numbered CR-042 on
arrival (the draft said CR-041, taken the same day by the table CR); §1 and §3.2's `Body` row
corrected against VERSION_17_3_3 by the docx4j session, and a risk added (§8 lists the changes).

Scope: `org.docx4j.TraversalUtil` (`getChildrenImpl`, `Callback`, `CallbackImpl`, the
constructor and `visit` overloads) and what a walk visits. Not in scope: what any existing
callback does with what it sees today, the `McMode` selection rule (CR-021), or the
`TraversalUtilVisitor` typed visitors beyond their inheriting the new option.

## 1. Background: what a walk visits today (read from the code, docx4j 17.3.3-SNAPSHOT)

`TraversalUtil.getChildrenImpl(Object, McMode)` is an explicit list of *content* types: a
`ContentAccessor` gives its content (`P`, `R`, `Tbl`, `Tr`, `Tc`, `Body`, `Hdr`, `Ftr`, the
notes and comments), an `SdtElement` its `sdtContent`, `Anchor` and `Inline` their graphic
data (and the hyperlink of their `wp:docPr`, `handleCTNonVisualDrawingProps`), `Pict` its
`getAnyAndAny()`, `CTObject` that and its `w:control`, the VML and wps text boxes their
`w:txbxContent`, a diagram's data model its points, `AlternateContent` the branches `McMode`
selects, `Document` an artificial list holding only its `Body`, and `FldChar` its type,
`w:ffData`, `w:fldData` and `w:numberingChange`. A short list are leaves (`Text`, `Br`,
`R.Tab`, `R.LastRenderedPageBreak`, bookmarks and other `CTMarkupRange`s, `ProofErr`).
Anything else falls through to a reflective search (the `log.debug("... looking for method
which returns list")` branch): the class's *declared* methods only, not inherited ones, and the
first whose return type is `java.util.List`, in whatever order the JVM lists them. That is how
`w:drawing` reaches its anchor or inline, how a `Styles` element reaches its `Style`s, and how a
`Style` reaches its `w:tblStylePr` - the `CTTblStylePr` object itself, though nothing inside it.

What a walk never visits: the property objects. A paragraph's `w:pPr` and a run's `w:rPr`, a
table's `w:tblPr`, `w:trPr` and `w:tcPr`, a body's `w:sectPr` (`Body.getContent()` excludes it,
and `Document`'s artificial list holds the `Body` alone), a style's properties and the inside of
its `w:tblStylePr`, a numbering level's `w:rPr`, and everything inside them: `w:color`, `w:shd`, the border elements
(`CTBorder` in `w:pBdr`, `w:tblBorders`, `w:tcBorders`, `w:bdr`), `w:u`, `w:rFonts`; and on the
DrawingML side a shape's `spPr` and `style`, its fills, lines, gradient stops and effects, and
a chart's `c:spPr` and `c:txPr`. `apply(Object)` is called for `P` and `R`, never for their
`Color`.

Every consumer that rewrites attributes rather than content has therefore written its own
descent. In docx4j itself `StyleUtil` and `PropertyResolver` work on properties they are handed,
not by walking. Enterprise MergeDocx's `RFontsThemeResolver` reaches `w:rFonts` from inside the
style renamer's `apply(R)` (one level down, by hand), and enterprise CR-003 phase 4's
`ThemeResolver` (2026-10-14), which has to rewrite `w:color`, `w:shd`, every `CTBorder`,
`w:u`, `a:schemeClr`, DrawingML typefaces and shape style references wherever they occur, gave
up on `TraversalUtil` and walks the JAXB tree by reflection: every public getter returning a
docx4j JAXB type or a `List`, an identity set against revisits, `getParent` excluded, both
branches of `AlternateContent`. It works, and it is the second such walk in the Enterprise
code after `CopyGeneric`'s. That is the gap.

## 2. The gap

A callback that edits *attributes* (colours, fonts, borders, ids inside properties, a theme
reference anywhere) has no way to be a `TraversalUtil` callback: the walk will not bring it the
objects. It either re-implements the descent, or hangs its logic off `apply(P)`/`apply(R)` and
reaches down by hand, which covers the few places it thought of and misses the rest (the
`w:tblBorders` inside a table style's `w:tblStylePr`, say, or an `a:schemeClr` in a gradient
stop). Since CR-021 made the walk mode-aware (`McMode`), the natural home for "how deep" is
beside "which branches".

## 3. Design

### 3.1 The option

A second enum beside `McMode`, in `org.docx4j.jaxb` or `org.docx4j`:

```java
public enum Descent {
    /** Content only: paragraphs, runs, tables, text boxes ... - today's walk, the default. */
    CONTENT,
    /** Content and every property object beneath it, and their descendants. */
    PROPERTIES
}
```

Carried like `McMode`: `CallbackImpl.setDescent(Descent)` / `getDescent()`, a constructor
overload `TraversalUtil(Object parent, Callback cb, McMode mcMode, Descent descent)`, the
matching `visit` overloads, and `getChildrenImpl(Object o, McMode mcMode, Descent descent)`.
The three-argument forms keep `CONTENT`. `CallbackImpl.getChildren(o)` passes both of its
fields, so a callback that overrides `walkJAXBElements` and calls `getChildren(parent)` (the
usual pattern) gets the descent for free.

### 3.2 What `PROPERTIES` descends into

Under `PROPERTIES`, `getChildrenImpl` returns today's content children **plus** the object's
property children, properties first (so a callback sees `w:pPr` before the paragraph's runs,
as the XML has it), and for a property object (or anything else not in the content list)
every child reachable through a public getter that returns a docx4j JAXB type, a `JAXBElement`
or a `List` of those: the reflective descent, cached per class, `getParent` excluded, leaves
(`String`, numbers, enums, `byte[]`) skipped. `JAXBElement`s are unwrapped as today. The
content list keeps its explicit shape so that `CONTENT` is byte-for-byte today's walk and
`apply` keeps its pre-order contract.

Where property objects hang off content objects (so that the generic descent need not be
applied to a `P` or a `Tbl`, which would also reach their content twice):

| content object | its properties |
|---|---|
| `P` | `w:pPr` (and inside it `w:rPr`, `w:pBdr`, `w:shd`, `w:sectPr` ...) |
| `R` | `w:rPr` |
| `Tbl` | `w:tblPr`, `w:tblGrid` |
| `Tr` | `w:trPr`, `w:tblPrEx` |
| `Tc` | `w:tcPr` |
| `SdtElement` | `w:sdtPr`, `w:sdtEndPr` |
| `Body` | its `w:sectPr` (not reached today: `Document`'s artificial list holds the `Body` only, and `Body.getContent()` excludes it) |
| `CTTxbxContent`, `Hdr`, `Ftr`, notes, comments | none |
| `Anchor`, `Inline` | `wp:docPr`, `wp:cNvGraphicFramePr`, the extents and wrap elements, before the graphic data |
| wps `CTWordprocessingShape` | `wps:cNvSpPr`, `wps:spPr`, `wps:style`, `wps:bodyPr`, before `w:txbxContent` |
| `Pic`, `CTGvmlShape`, the diagram shapes | `spPr`, `style`, `nvPr` ... |

Objects that are *not* content (a `Styles` part's element, a `Numbering`, a chart's
`CTChartSpace`, a theme) have no content-list entry today and reach the "method returning a
list" fallback; under `PROPERTIES` they take the generic descent, so `visit(styles, cb, ALL,
PROPERTIES)` visits every `Style`, its properties and its `w:tblStylePr`, and
`visit(chartSpace, cb, ALL, PROPERTIES)` every `c:spPr` and `a:schemeClr`.

### 3.3 What the Enterprise side then does

`ThemeResolver`'s walk becomes a `CallbackImpl` walked with `Walks.all(parent, cb)` plus
`setDescent(PROPERTIES)` (or a `Walks.allProperties` helper); its handlers (`Color`, `CTShd`,
`CTBorder`, `U`, `RFonts`, `TextFont`, `CTShapeStyle`, any `getSchemeClr` holder) move into
`apply`. Its reflective walk goes. The `CopyGeneric` descent is a separate question, not this
CR's.

## 4. Phases

### Phase 0: the tests

`TraversalUtilDescentTest`: a document with a coloured run, a shaded paragraph with a border, a
table with `w:tblBorders` and a cell with `w:tcPr/w:shd`, a wps text box with a scheme-coloured
`spPr` and a `wps:style`, in both branches of an `mc:AlternateContent`; a styles element with a
table style carrying `w:tblStylePr/w:tcPr/w:shd`. A `CONTENT` walk's `apply` sees none of
`Color`, `CTShd`, `CTBorder`, `CTSchemeColor`; a `PROPERTIES` walk sees each of them, each
once, `w:pPr` before the paragraph's first run, both branches under `McMode.ALL`. And the
regression gate: a `CONTENT` walk over every document in docx4j's test corpus visits exactly
the objects, in the order, it visits today (record once, compare).

### Phase 1: the descent

§3.1 and §3.2. The 17.2.0 CHANGELOG entry for `McMode` is the model for the note.

### Phase 2 (Enterprise, not this repository)

§3.3, in enterprise CR-003's record.

## 5. Decisions for Jason

1. **Opt-in, never the default.** `CONTENT` stays what every existing walk gets; `PROPERTIES`
   is asked for. Recommended: yes (the alternative changes what the 51 walkers CR-021
   classified see in `apply`).
2. **Generic reflection for the property subtree, the explicit list for content.**
   Recommended: yes; an explicit list of every property type would be long and would miss the
   next one, and the reflective fallback is already in `getChildrenImpl`.
3. **Version.** 17.3.3 is not released until it carries CR-039; this is small (one day) and
   could ride on it, or wait for 17.3.4. The Enterprise side is not blocked either way (its
   reflective walk stays until the swap). Recommended: 17.3.3 if it ships before the release
   is cut, else 17.3.4.

## 6. Risks

- **Double visits.** A property object reachable both through the explicit table (§3.2) and
  the generic fallback; the phase 0 "each once" assertion is the guard, and an identity set in
  the walk is the belt and braces.
- **Order.** Callbacks that depended on seeing a run before anything in its paragraph's
  properties cannot exist today (they never saw the properties), so properties-first is safe.
- **Cost.** Reflection per class is cached; the Enterprise walk over a large merged document
  runs in the noise against the merge itself.
- **`shouldTraverse`.** A callback returning false for a `P` under `PROPERTIES` skips its
  `w:pPr` too; documented.
- **The parent fix.** `CallbackImpl.walkJAXBElements` sets the parent of a `JAXBElement`-wrapped
  child whose parent is null to the object being walked, and logs at warn level where it is
  some other object ("getChildren() skips layers"). Under `PROPERTIES` that reaches property
  objects, some of them wrapped and some reached through a layer the generic descent skips.
  Phase 0's tests also assert that a `PROPERTIES` walk changes no parent pointer and logs no new
  warning on the test documents; the descent hands the immediate owner as the parent.

## 7. Effort (rough)

One day: the enum and overloads, the table in §3.2, the tests of phase 0, the CHANGELOG note.

## 8. Changes made on arrival (docx4j session, 2026-10-14)

Against the Enterprise draft (Enterprise 3287bfc), read against VERSION_17_3_3:
- renumbered CR-041 to CR-042;
- §1: `FldChar`'s children are its type, `w:ffData`, `w:fldData` and `w:numberingChange`, not
  runs; the leaves listed as the code has them; `Anchor`/`Inline` also give their `wp:docPr`
  hyperlink; the reflective fallback takes declared methods only, the first `List` getter in the
  JVM's order, and reaches a `Style`'s `w:tblStylePr` objects (not their contents) and a
  `Styles` element's styles; a body's `w:sectPr` is not reached today;
- §3.2: the `Body` row says the same (the draft had it reachable through `Document`);
- §5 decision 1: the 51 walkers are CR-021's classification (its §§1 to 3), not its §4;
- §6: the parent-fix risk added.
