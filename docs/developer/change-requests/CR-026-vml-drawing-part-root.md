# CR-026: A vmlDrawing part binds — the `<xml>` root's namespace

Status: DONE 2026-09-23 (option A, Jason's choice; §10 is the record). Found by CR-019 phase 3 (the
anonymiser on xlsx), whose "left for later" names it; the anonymiser works
around it by reading the part as DOM. Drafted with Claude Opus 5, with the
measurements of §1 to §3. Owner: Jason Harrop. One phase.

Scope: `org.docx4j.openpackaging.parts.VMLPart` and the schema behind it,
`xsd/vml/vml__ROOT.xsd`, so that a `vmlDrawing` part unmarshals to its typed
objects instead of failing. Not in scope: VML inside `w:pict` or `p:pic`,
which binds today and is unaffected (§3); the content of the VML schemas
themselves; `VMLBinaryPart`, which is a different (binary) reading of the
same part name and is left alone.

## 1. What happens today

A `vmlDrawing` part — `/xl/drawings/vmlDrawing1.vml`, `/word/drawings/…`,
`/ppt/drawings/…` — is created by `ContentTypeManager` as a `VMLPart`, which
is a `JaxbXmlPartXPathAware<org.docx4j.vml.root.Xml>`. Every one of them fails
to unmarshal:

```
ERROR JaxbXmlPartXPathAware - unexpected element (uri:"", local:"xml").
      Expected elements are <…>,<{urn:schemas-microsoft-com:office:excel}ClientData>,<…>
WARN  Anonymize - /xl/drawings/vmlDrawing1.vml could not be read as JAXB
```

Measured consequences (2026-09-23):

| | |
|---|---|
| `getContents()` on the part | throws `Docx4JException: Problem with part …` — always, for every package kind |
| a **transitional** package containing one, loaded and saved | **survives**: the part was never unmarshalled, so `ZipPartStore.saveRawXmlPart` copies the source bytes; verified byte-identical for `comments.xlsx` |
| a **strict** package containing one, loaded and saved | **fails**: `isWasStrict()` makes the save force `getContents()` on every part, which throws, and `pkg.save()` ends in `Docx4JException: Failed to add parts from relationships of /` with a truncated file left behind (measured on `strict-simple.xlsx` with `loadAndSave.xlsx`'s vmlDrawing injected — no strict sample in the repository has one) |

So: a comment or a form control in a **strict** workbook — a `vmlDrawing` part
is how both are drawn — makes the workbook unsaveable by docx4j; in a
transitional one the part is opaque (no API reaches its shapes) but is
preserved. A caller who asks for the contents gets an exception either way.

**Why the strict case differs.** A strict package is not converted whole on
open: `Load3` detects it from the package relationships and sets
`OpcPackage.setWasStrict(true)`, and each part is converted **lazily**, the
first time its contents are asked for (`JaxbXmlPartXPathAware`:
`transformFirst = isWasStrict() || …`, then `xlsx-preprocessor.xslt`). Saving
such a package writes a **transitional** one — verified: the output's
`[Content_Types].xml` and every part root are the
`schemas.openxmlformats.org` namespaces, not `purl.oclc.org`. That is exactly
why `ZipPartStore` cannot take its usual shortcut for a part nobody touched:
those bytes are still strict, and copying them verbatim would put
purl-namespaced XML inside a transitional package. So it forces
`getContents()` — and the one part whose `getContents()` can never succeed
takes the save down with it.

**Answered by a real file (Jason, 2026-09-23).** The first measurement used a
synthetic fixture (a transitional `vmlDrawing` injected into
`strict-simple.xlsx`), so Jason saved a workbook with a comment as *Strict Open
XML Spreadsheet* — docx4j's own `comments.xlsx`, anonymised, re-saved by Excel
365; it is committed as `docx4j-core-tests/src/test/resources/strict/strict-comments.xlsx`.
What it settles:

1. **Excel does write a `vmlDrawing` part in a strict workbook**
   (`xl/drawings/vmlDrawing1.vml`, beside `xl/comments1.xml`). The package is
   strict in every other respect: `workbook.xml` carries
   `conformance="strict"`, the part roots and the package relationship types
   are `purl.oclc.org`.
2. **The VML part itself is not strict-namespaced**: its content is the usual
   `urn:schemas-microsoft-com:vml` / `…office:office` / `…office:excel`, with
   no purl anywhere in it and — in this specimen, a comment shape — no
   relationship attribute at all. VML is not in ISO 29500, so it has no strict
   dialect; even the *relationship type* pointing at the part stays
   transitional (`…openxmlformats.org/officeDocument/2006/relationships/vmlDrawing`)
   in a rels part whose other entries are purl. So the fix of §6 needs no
   preprocessor rule for this part, and the same `<xml>` root is what both
   conformance classes present.
3. The failure of §1 reproduces exactly on the real file: `pkg.save()` ends in
   `Docx4JException: Failed to add parts from relationships of /` → `… of
   /xl/workbook.xml` → `… of /xl/worksheets/sheet1.xml` → `Problem with part
   /xl/drawings/vmlDrawing1.vml`, leaving a 5,825-byte fragment of what should
   be a 12,645-byte workbook.

So **a strict workbook with a comment cannot be saved by docx4j at all** — not
a theoretical case: a comment is the ordinary way to get a `vmlDrawing`, and
Excel offers Strict in its Save-as list.

## 2. Why

VML is Office's old "XML island": the part's root element is literally `<xml>`
**in no namespace** — the file declares only the prefixes it uses.

```xml
<xml xmlns:v="urn:schemas-microsoft-com:vml"
     xmlns:o="urn:schemas-microsoft-com:office:office"
     xmlns:x="urn:schemas-microsoft-com:office:excel">
  <o:shapelayout v:ext="edit">…</o:shapelayout>
  <v:shapetype id="_x0000_t202" …>…</v:shapetype>
  <v:shape …>…<x:ClientData ObjectType="Note">…</x:ClientData></v:shape>
</xml>
```

docx4j binds that root from `xsd/vml/vml__ROOT.xsd`, whose own header says why
it exists: *"This XSD exists because you need one schema document per
namespace"*. To give the namespace-less element a home it invents one:

```xml
<xsd:schema targetNamespace="urn:docx4j:vml:root" … elementFormDefault="unqualified">
  <xsd:element name="xml">
    <xsd:complexType><xsd:sequence>
      <xsd:any namespace="urn:schemas-microsoft-com:vml" minOccurs="1" maxOccurs="unbounded" processContents="strict"/>
    </xsd:sequence></xsd:complexType>
  </xsd:element>
</xsd:schema>
```

A **global** element declaration always takes the schema's `targetNamespace`
(`elementFormDefault` governs *local* elements only), so XJC generates
`@XmlRootElement(name = "xml")` in a package annotated
`@XmlSchema(namespace = "urn:docx4j:vml:root")`: JAXB has a global element
`{urn:docx4j:vml:root}xml` and the document presents `{}xml`. Nothing matches,
and the unmarshaller lists every other global element it knows.

**Evidence that this is the only obstacle.** The same bytes, with
`xmlns="urn:docx4j:vml:root"` spliced onto the root and nothing else changed,
unmarshal cleanly through `Context.jc` into `org.docx4j.vml.root.Xml` with its
three children. The `<xsd:any namespace="urn:schemas-microsoft-com:vml">`
restriction does not bite at runtime: XJC generates `@XmlAnyElement(lax = true)`,
which does not enforce the wildcard's namespace, so the office-namespace
`o:shapelayout` and the excel-namespace `x:ClientData` bind as happily as the
VML ones.

The invented namespace is referenced nowhere else: not in
`NamespacePrefixMappings`, not in `Uris2`, not in any test or resource — only
in `ROOT.xsd`'s import of this schema, `Context.jc`'s package list (which names
the *package*, `org.docx4j.vml.root`) and `module-info`.

## 3. Inline VML in `w:pict` is a different binding, and is not affected

The shapes inside a `w:pict` (or a `p:pic`, or `xdr:` drawing) are **not**
wrapped in `<xml>`: they are elements of the real VML namespace, children of
`CTPictureBase.getAnyAndAny()`. Measured on `vml/textbox.docx`, walking the
main document part:

```
inline VML in w:pict binds as:
   org.docx4j.vml.CTShape
   org.docx4j.vml.CTShapetype
   org.docx4j.vml.CTTextbox
```

Those classes come from `xsd/vml/vml-main.xsd`, package `org.docx4j.vml`,
`@XmlSchema(namespace = "urn:schemas-microsoft-com:vml", elementFormDefault =
QUALIFIED)`, with `@XmlElementDecl(namespace = "urn:schemas-microsoft-com:vml",
name = "shape")` and friends in its `ObjectFactory` — a different namespace,
package and schema from the `<xml>` wrapper, which is one class
(`org.docx4j.vml.root.Xml`) holding a lax `any` list.

So whichever option below is taken, inline VML binds exactly as it does now,
to the same classes: the change is confined to the name of one wrapper
element. The same `org.docx4j.vml.*` objects are what a fixed `VMLPart` would
hand back — the shapes of a comment or a control become reachable by the same
API as the shapes in a text box.

## 4. Options

**A. The schema tells the truth: no target namespace.** Remove
`targetNamespace="urn:docx4j:vml:root"` from `vml__ROOT.xsd` (and the
`namespace` attribute from `ROOT.xsd`'s import of it, which is legal because
`ROOT.xsd` itself has no target namespace), so the global element is `{}xml`
and XJC generates the root class with `@XmlRootElement(name = "xml")` in a
package with no `@XmlSchema` namespace. The class keeps its name and package.

- For: the binding then says what the format says; load and save are the
  ordinary JaxbXmlPart paths; no special case anywhere.
- Against: it is a regeneration of `docx4j-generated-objects`, so the
  TypeScript and Python ports regenerate too (§8) — a hand-off for a one-line
  schema edit. `Context.jc` must still list the package (it will). No schema in
  the tree declares a global element in no namespace today (`ROOT.xsd`,
  `wml/wml__ROOT.xsd` and `sections.xsd` have no target namespace, but they
  declare no elements), so what XJC makes of one — the `@XmlRootElement`
  namespace, and whether the `ObjectFactory` keeps an element factory method —
  is to be confirmed by the regeneration, not assumed.

**B. The part does the mapping.** Leave the schema and the generated classes
alone; have `VMLPart` unmarshal **by declared type**
(`unmarshaller.unmarshal(source, Xml.class)`, which ignores the root element's
name entirely) and marshal a `JAXBElement<Xml>` with `new QName("", "xml")`.

- Measured: both halves work today, with the classes exactly as they are. The
  declared-type unmarshal of `loadAndSave.xlsx`'s and `comments.xlsx`'s parts
  gives `CTShapeLayout`, `CTShapetype`, `CTShape`; the QName-overridden
  marshal writes `<xml …>` with no namespace on the root.
- For: no schema change, no regeneration, no port hand-off, and it can ship in
  a patch release.
- Against: two overrides in `VMLPart` (and `JaxbXmlPartXPathAware`'s binder and
  XPath paths would need the same treatment, or to be declared unsupported for
  this part); the binding still claims a namespace the format does not have,
  so the next reader of the schema meets the same puzzle.

**C. Do nothing; read it as DOM where needed.** What CR-019 phase 3 does
(`Anonymize` swaps the unreadable `VMLPart` for a `DefaultXmlPart` from the
source part store and `VmlDomScrubber` works on the DOM).

- For: nothing to regenerate; already written.
- Against: leaves the strict-save failure of §1 in place, leaves every caller
  to invent the same workaround, and leaves `VMLPart`'s type parameter a
  promise the class cannot keep.

## 5. Recommendation

**A**, with **B** as the fallback if the regeneration is unwelcome this close
to a release. The defect is in the schema, the fix there is one line and one
import attribute, and it makes every path — load, save, XPath, the binder —
ordinary. B is a genuine alternative rather than a hack (declared-type
unmarshalling is JAXB's own answer to "the root element is not what the
context expects"), but it leaves the schema wrong, and the ports read the
schema.

Either way the strict-save failure of §1 goes: the part unmarshals, so the
`isWasStrict()` branch has something to marshal.

## 6. Plan (one phase)

1. `xsd/vml/vml__ROOT.xsd`: drop the target namespace; `xsd/ROOT.xsd`: drop the
   `namespace` attribute from its import. Regenerate; confirm
   `org.docx4j.vml.root.Xml` is `@XmlRootElement(name = "xml")` with no
   namespace and that its `ObjectFactory` is unchanged in shape.
2. `module-info` and `Context.jc`'s package list: unchanged (the package name
   does not move) — confirm.
3. `VMLPart`: nothing, if step 1 is enough. Check `getXML()`, the binder and
   the XPath paths on a part that now has contents.
4. Retire the anonymiser's workaround: `Anonymize.vmlAsDom` and the
   `DefaultXmlPart` branch of `PartsAnalyzer` go, and `VmlDomScrubber` is
   replaced by handling the typed objects in `ScrambleText`/`MarkupScrubber`
   (`CTShape.getAlt`/`getTitle`/`getHref` and `CTTextbox` are handled there
   already for inline VML, so this is mostly deletion). Keep a DOM fallback
   only if step 3 finds a part docx4j still cannot read.
5. CHANGELOG under "SpreadsheetML" (or "Packaging"): a vmlDrawing part's
   contents are available, and a strict package containing one saves.

## 7. Tests

- `VMLPart` round trip, transitional and strict, over `comments.xlsx`,
  `loadAndSave.xlsx` (both have one) and `strict/strict-comments.xlsx` (§1),
  which is a save-and-reload regression test on its own: today it throws.
- The shapes are reachable and typed: `CTShapeLayout`, `CTShapetype`,
  `CTShape`, and the `x:ClientData` inside a shape.
- Inline VML is untouched: `vml/textbox.docx` still binds `CTShape`,
  `CTShapetype`, `CTTextbox` (§3), and the HTML and FO exporters' VML tests
  still pass.
- The anonymiser's xlsx corpus stays clean and verified with the workaround
  removed (`AnonymizeXlsxCorpusTest`, `AnonymizeXlsxProbesTest`).
- `docx4j-core-tests` in full.

## 8. Hand-offs, if A is taken

Per `docs/developer/adding-a-schema.md` §11 — it is a schema change, though
not a new namespace, and it *removes* one:

- **TypeScript objects** (`../docx4j-generated-objects-ts`): regenerates from
  `xsd/ROOT.xsd` at the commit. Tell it the xsd files touched
  (`vml/vml__ROOT.xsd`, `ROOT.xsd`), that the `urn:docx4j:vml:root` namespace
  is gone and the root element is now `{}xml`, and that nothing else in the
  model changes.
- **Python** (`../docx4j-python`): VML is not generated yet (its CR-001 phase
  D), so this reaches it only when it is; tell it anyway, so its copy of the
  tree is re-taken after this commit rather than before.
- Both ports have the same defect latent in the same place if they generated
  from the old schema.

## 9. Risks

- **A regeneration for a one-line change.** Mitigated by the ports being told,
  and by the change being subtractive (a namespace disappears; no class moves,
  no property changes).
- **A part docx4j now unmarshals is a part docx4j now rewrites.** Today a
  transitional package's vmlDrawing is copied byte-for-byte; afterwards it is
  marshalled from the objects, so Office sees docx4j's serialisation of it
  (attribute order, prefixes, the `style` attribute's whitespace). This is the
  same exposure every other part already has, but it is new for this one, and
  it is what the Office-open check in §7 is for. If it proves troublesome, the
  part can keep its bytes when nothing asked for its contents (`isUnmarshalled()`
  already governs exactly that).
- **`<xsd:any namespace="urn:schemas-microsoft-com:vml">`** does not describe
  what Office writes (`o:`, `x:` and `w10:` children are normal). It binds
  anyway, because the generated accessor is lax; worth widening to
  `##any` in the same edit for honesty, at no runtime cost.

## 10. Record (2026-09-23)

Option A, as recommended. What shipped:

| | |
|---|---|
| `xsd/vml/vml__ROOT.xsd` | the invented `targetNamespace="urn:docx4j:vml:root"` removed, with a comment saying why there is none; the wildcard widened from the VML namespace to `##any` (Office writes `o:`, `x:` and `w10:` children there) |
| `xsd/ROOT.xsd` | `<xsd:import id="vml" namespace="…">` becomes `<xsd:include schemaLocation="vml/vml__ROOT.xsd"/>` — an import with no namespace attribute is illegal from a schema which has no target namespace of its own (`src-import.1.2`), and include is the construct for a no-namespace schema; neither schema has a target namespace, so nothing is absorbed |
| regenerated | `org.docx4j.vml.root.Xml` is `@XmlRootElement(name = "xml")` with no namespace, and its `package-info.java` is gone (there is no `@XmlSchema` namespace to declare). The class, its package and its `ObjectFactory` are otherwise unchanged; `module-info` and `Context.jc`'s package list needed nothing |
| `VMLPart` | nothing: load, save, `getXML()` and the XPath paths are the ordinary ones now |

Measured on the two fixtures: `anon/comments.xlsx`'s part unmarshals to
`CTShapeLayout`, `CTShapetype`, `CTShape`, and `strict/strict-comments.xlsx`
now loads **and saves** (14,039 bytes where it used to die at 5,825), the
output transitional and its VML in the ordinary VML namespaces.
`VmlDrawingPartTest` pins all three.

**The anonymiser's workaround is retired** (step 4 of §6): `Anonymize.vmlAsDom`
and `readable`, the `DefaultXmlPart`-VML branch of `PartsAnalyzer` and
`PartsAnalyzer.isVml`, and `VmlDomScrubber` are gone; a vmlDrawing part is
walked as JAXB like any other. Two things moved into `ScrambleText` for that:

- the HTML of a VML text box (`v:textbox` holds a `div` of `font` and `span`
  elements — the text of a comment or a check box) is scrambled by the DOM
  path, which until now scrambled only `t` and `v` elements. This was measured,
  not assumed: with the typed path and without it, `cr022-checkbox.xlsx` leaked
  "Check Box 1" and `Verify` caught it.
- `x:ClientData`'s children are `JAXBElement<String>`, so the generic
  string case would have scrambled the anchor's digits and the alignment
  words; the `Fmla*` ones go through the formula scrubber and the rest are
  left alone, as the DOM scrubber did by element name.

### Gate

| step | result |
|---|---|
| regeneration and the whole reactor | BUILD SUCCESS |
| `VmlDrawingPartTest` (3: the shapes are reachable, a transitional package round trips, a strict package with a comment saves) | pass |
| the anonymiser's suites with the workaround gone | 51 tests, 0 failures (the xlsx corpus includes `strict/strict-comments.xlsx` now) |
| `docx4j-core-tests`, full | 1308 tests, 0 failures, 11 skipped |
| Office check (Excel 365, Jason, 2026-09-23; the share's `fidelity/cr026-vml/`) | **passed**. `strict-comments-saved.xlsx` — the workbook that could not be saved at all before — opens with its comment; `comments-saved.xlsx` and `checkbox-saved.xlsx` open with their comment and check box in place. Two false alarms in the README were mine, not the code's: `comments.xlsx` has one comment (three sheets), and `cr022-checkbox.xlsx`'s check box was never linked to a cell — in both files the parts that carry them are byte-identical to the originals' |
| the linked-control path, which no fixture covered | checked separately, by bisect (below): a check box linked to `$D$4` survives docx4j's round trip and still drives the cell in Excel |

### Hand-offs

As §8: the TypeScript objects regenerate from `xsd/ROOT.xsd` at this commit —
the `urn:docx4j:vml:root` namespace is gone, the root element is `{}xml`, the
wildcard is `##any`, and `ROOT.xsd` includes rather than imports that schema.
Python does not generate VML yet, so it is told for when it does.

## 11. Where Excel reads a form control's cell link (measured 2026-09-23)

No fixture had a *linked* control, so one was made by hand and bisected in
Excel 365 (Jason). Each variant is `cr022-checkbox.xlsx` plus exactly one
thing; `-saved` is that file through docx4j:

| variant | Excel opens | the box drives D4 | docx4j's `-saved` |
|---|---|---|---|
| A: `fmlaLink="$D$4"` on the ctrlProps part | yes | **yes** | opens, still linked |
| B: `<x:FmlaLink>$D$4</x:FmlaLink>` in the VML `x:ClientData` | yes | **no** | not worth opening |
| C: both | yes | **yes** | opens, still linked |

So for a modern (x14) form control the link Excel honours is the one in the
**ctrlProps part**; the VML `ClientData`'s copy is the legacy fallback and is
ignored while the x14 branch is there. docx4j preserves both, and the
anonymiser rewrites both as formulas (`CTFormControlPr`'s `fmlaLink` through
`SmlFormulas`, the VML's through `ScrambleText`'s `CTClientData` case) rather
than scrambling them as text.

Variant C is committed as
`docx4j-core-tests/src/test/resources/cr022-checkbox-linked.xlsx` — hand-made
but Excel-validated — and `AnonymizeXlsxCorpusTest.aLinkedFormControlStillPointsAtItsCell`
holds both halves of the link to `$D$4` through an anonymisation.

An earlier attempt at the same fixture added a checked state as well as the
link (`checked="Unchecked"` on ctrlProps, `<x:Checked>0</x:Checked>` in the
VML) and Excel dropped the check box; since A, B and C all open, one of those
two is what it objected to. Not chased: it is a fact about hand-edited XML,
not about docx4j, and Excel writes `checked` itself when the box is ticked.
