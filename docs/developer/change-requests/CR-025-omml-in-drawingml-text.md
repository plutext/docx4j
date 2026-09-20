# CR-025: OMML inside DrawingML text - m:r and m:ctrlPr admit a:rPr

Status: PROPOSED 2026-09-20 (Jason Harrop: "let's do option 2 (as CR-025)",
choosing the truthful schema over a `processContents="skip"` wildcard, after
the docx4j-generated-objects-ts session reported the 0.1.5 regression below).
Written by `docs/developer/adding-a-schema.md` step 0 (no new namespace; §4
names the one departure). Drafted with Claude Opus 5. Owner: Jason Harrop.
One phase. Ships in 17.2.0.

Scope: the two OMML types whose run properties are WordprocessingML's alone in
`shared-math-2ed.xsd` - `CT_R` (`m:r`) and `CT_CtrlPr` (`m:ctrlPr`) - admit
DrawingML's `a:rPr` beside the `w:` groups, because that is what PowerPoint
and Excel write when an equation sits in a DrawingML text body ([MS-ODRAWXML]
2.3.1: `a14:m` holds `m:oMathPara` or `m:oMath`, and inside it the run
properties are `a:rPr`, there being no `w:rPr` in a slide). Not in scope:
typing `a14:m`'s content in the PresentationML and SpreadsheetML JAXB
contexts (CR-021 §8.9 measured why not; §4), and the rest of what an OMML run
may hold in DrawingML (nothing else was found - §1).

## 1. The measurement (2026-09-20)

What Office writes. Every `a14:m` in the repository's Office files and the
three real-document corpora (454 docx), by the survey script `survey-a14m.py`
(a namespace-aware count of `m:r`'s and `m:ctrlPr`'s first child over every
XML part):

| file, part | `a14:m` | `m:r/a:rPr` | `m:ctrlPr/a:rPr` | `m:r/w:rPr` |
|---|---|---|---|---|
| `loadAndSave.pptx`, `ppt/slides/slide2.xml` | 1 | 16 | 7 | 0 |
| `loadAndSave.xlsx`, `xl/drawings/drawing1.xml` | 1 | 16 | 7 | 0 |
| the three corpora, 454 docx | 0 | - | - | - |

So in a DrawingML text body every run and every control-properties element
of the equation carries `a:rPr` (`lang`, `i`, `smtClean`, an `a:latin`
naming Cambria Math), and never `w:rPr`. Word never writes `a14:m` into a
docx's DrawingML text (a Word text box is `w:txbxContent`, WordprocessingML);
the two hosts are PowerPoint slides and Excel drawings, both inside an
`mc:AlternateContent` Choice `Requires="a14"`.

What docx4j does with it today, measured on the slide's `a14:m` fragment
(`Probe.java`, `Probe2.java` in the session scratchpad):

| where the equation is unmarshalled | outcome |
|---|---|
| `jcPML` (a slide: `p:spTree`'s kept Choice is typed, so `a14:m` is typed) | the equation is DOM (`CT_TextMath`'s lax wildcard finds no `m:` classes in that context) and is written back as it came: 16 + 7 `a:rPr` in, 16 + 7 out |
| `Context.jc` on the xlsx drawing part (`JaxbDmlPart` uses the WML context) | the anchor's kept Choice is DOM (CR-024 §5), so `a14:m` never reaches the binder: 16 + 7 in, 16 + 7 out |
| `Context.jc` on the fragment itself (`XmlUtils.unmarshalString(a14:m, Context.jc)`) | **fails**: `unexpected element (uri:"...drawingml/2006/main", local:"rPr"). Expected elements are w:rPr, w:del, w:ins`; the strict unmarshal is rejected by `JaxbValidationEventHandler`, the preprocessing retry fails the same way, and `JAXBException: Preprocessing exception` comes out |

So in Java the loss is latent: it needs a typed `a14:m` in the WML context,
which today's parts do not produce (charts and SmartArt data are DrawingML
parts in the WML context, but Office does not put an equation in a chart
title or a diagram node). A consumer unmarshalling a slide's equation through
`Context.jc` - the port's parity fixtures, a caller taking the Choice's
content typed - hits it.

The regression the ports hit (`@docx4j/generated-objects-ts` 0.1.5, found by
docx4j-core-ts's CR-001 §17.5, reproduced by objects-ts): Jsonix has one
context over every module, so `m:oMath` inside `a14:m` unmarshals typed
through `org_docx4j_math`, and `CT_R`'s `a:rPr` is an unknown element in a
property that allows no DOM: "Element a:rPr could not be unmarshalled as is
not known in this context and the property does not allow DOM content". A
pptx or xlsx with an equation in a text body no longer unmarshals once the
`a14` Choice is taken. Under 0.1.4 `a14:m` was not admitted at all (CR-021
§8.9 admitted it), stayed DOM, and was written back as it came. core-ts holds
at ^0.1.4; its CR-004.B is blocked on this.

## 2. The change (recipe steps 1-3, 7, 9)

Step 1, the schema: `xsd/shared/shared-math-2ed.xsd` (ECMA-376 Part 1 §22.1,
OMML) and `xsd/dml/dml-textCharacter.xsd` (§21.1.2.3, `CT_TextCharacterProperties`).
The Office files exercising it: `loadAndSave.pptx` slide 2 and
`loadAndSave.xlsx` drawing 1, already in `docx4j-core-tests`.

Step 2, where it joins the tree:

- `dml-textCharacter.xsd` gains a model group in the DrawingML namespace,
  `EG_TextRunPropertiesInMath`, holding one optional local element `rPr` of
  type `CT_TextCharacterProperties`. A group rather than a global element:
  dml-main declares no global `a:rPr` (every `rPr` in DrawingML is local -
  `CT_RegularTextRun`, `CT_TextField`, `CT_TextLineBreak`), a global one would
  add a `JAXBElement` factory to the DrawingML `ObjectFactory` and a name every
  local `rPr` binding could be measured against, and a group's local elements
  take the group's namespace, so referencing it from the math schema yields
  `a:rPr` without a global declaration. Its element carries
  `jaxb:property name="rPrDml"` (`CT_CtrlPr` already has a property `rPr`,
  WordprocessingML's).
- `shared-math-2ed.xsd` imports the DrawingML namespace (`xmlns:a`,
  `schemaLocation="../dml/dml-textCharacter.xsd"`, as `oart14docprop.xsd`
  imports the dml files it needs) and:
  - `CT_R`: `<xsd:group ref="a:EG_TextRunPropertiesInMath" minOccurs="0"/>`
    after `<xsd:group ref="w:EG_RPr" minOccurs="0"/>`;
  - `CT_CtrlPr`: its sequence becomes a choice of `w:EG_RPrMath` and
    `a:EG_TextRunPropertiesInMath`, `minOccurs="0"`.
- The dependency it brings: `org.docx4j.math` now references
  `org.docx4j.dml.CTTextCharacterProperties`. Every JAXB context that carries
  `org.docx4j.math` (`Context.jc`, and the Jsonix single context) already
  carries `org.docx4j.dml`, so no package list changes (step 5 is empty).
  `jcPML` and `jcSML` carry neither and are unchanged. The import is circular
  (dml-textParagraph → a14 → (wildcard) math → wml → math; math → dml) as wml ↔
  math already is; XJC compiles the closure from `ROOT.xsd` in one run.

Step 3, the generated shape (from today's `CTR`, `CTCtrlPr`, `ObjectFactory`):

- `CTR.content` (a general content list, `@XmlElementRefs`) gains one more
  scoped `JAXBElement<CTTextCharacterProperties>`, declared like `w:rPr` is
  today (`ObjectFactory.createCTRRPr`, `@XmlElementDecl(scope = CTR.class)`);
  the factory method needs its own name (`createCTRRPrDml`) since `createCTRRPr`
  and `createCTRRPrMath` exist. Every consumer of `CTR.content` iterates by
  `instanceof` (`OmmlToMathML`, `OmmlToLatex`, `HTMLExporterVisitorGenerator`,
  `XsltHTMLFunctions`, `WmlToMarkdown`) and passes an unknown `JAXBElement`
  by, so they are unchanged.
- `CTCtrlPr` gains a field `rPrDml` (`CTTextCharacterProperties`) beside
  `rPr`, `ins`, `del`. No code in docx4j reads `CTCtrlPr`.
- `CT_TextMath` keeps its lax wildcard (CR-021 §8.9): in the WML context the
  equation is typed and now complete; in `jcPML`/`jcSML` it stays DOM, as
  today.

Step 7 (module-info): nothing - no new package. Step 9 (prefix table):
nothing - `a` and `m` are known.

## 3. Tests and the gate (steps 10-11)

- `OmmlInDrawingMLTextTest` (docx4j-core-tests): the slide's `a14:m` fragment
  unmarshalled through `Context.jc` is typed (`CTTextMath.getAny()` is a
  `JAXBElement<CTOMath>`, its runs `CTR` with the `a:rPr` in `content` as
  `JAXBElement<CTTextCharacterProperties>`, its `m:ctrlPr` with `rPrDml`
  set); marshalled back, it is canonically equal to the input (the 16 + 7
  `a:rPr` in place, with their attributes and `a:latin`) - which is why the
  Office-open check is not needed here: the typed re-serialisation is asserted
  identical to what PowerPoint wrote, and no docx4j-written part changes
  bytes (the slide and the drawing are DOM in their own contexts, as before).
  The same fragment through `jcPML` still round-trips as DOM (the CR-021
  behaviour pinned).
- `OmmlToMathML` over that typed equation produces MathML (the `a:rPr` is
  skipped, not thrown on), and the MathML matches the one from the same
  equation with its `a:rPr` stripped.
- `SlideAlternateContentKeptTest` unchanged and green.
- Gate: docx4j-core-tests whole suite green on jars verified as this tree's
  (`no-concurrent-mvn-verify-before-push`); docx4j-markdown's math tests
  green (they build `CTR` objects and read `content`).

## 4. Departures from the recipe, named

- The DrawingML schema is patched from its ECMA source (a model group added
  to `dml-textCharacter.xsd`), as `dml-textParagraph.xsd` already was for
  `a14:m` (CR-021 phase 3) and `oart14docprop.xsd` for `CT_TextMath`. Reason:
  XSD 1.0 cannot declare a local element in another namespace, and the
  element must be `a:rPr`.
- `CT_TextMath` stays a lax wildcard rather than referencing `m:oMath` /
  `m:oMathPara` (CR-021 §8.9: element references made `jcPML` and `jcSML`
  fail to initialise, `IllegalAnnotationsException` on `w15:appearance`,
  measured). Typing the equation in those contexts would mean adding
  `org.docx4j.math` and the WordprocessingML packages it reaches to them -
  a different CR, if ever wanted.

## 5. Hand-offs (step 12)

- **docx4j-generated-objects-ts**: regenerate on the phase-1 commit, release
  0.1.6; both fixture parts into objects-ts CR-004's fidelity set with the
  `a14` branch taken (the peer's plan). Registry: `objects-ts/a14-math-regression`
  depends on `docx4j/CR-025`.
- **docx4j-core-ts**: CR-004.B unblocks through objects-ts 0.1.6; the typed
  shape (`content` gains the `a:rPr` element; `CT_CtrlPr` gains `rPrDml`) is
  what its parity harness should expect from the oracle.
- **docx4j-python**: for the record; its generated objects follow the same
  xsd.
- CHANGELOG under "Schema (CR-025)"; `tasks.yaml` entry `docx4j/CR-025`.

## 6. Risks

- XJC may reject `jaxb:property` on a group element that lands in `CTR`'s
  collapsed content list. Fallback: two groups (one per host type), or the
  customisation in `bindings.xjb` scoped to `CT_CtrlPr`. Either keeps the
  XML shape; only the Java property name is at stake.
- MOXy: no new package, no new root element; the mapper tables are untouched.
  Covered by the release check's MOXy run.

## 7. Phase record

Phase 1 (the schema, regeneration, tests, hand-offs): not started.
