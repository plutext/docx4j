# CR: Configurable XHTMLImporter formatting options for OpenDoPE XHTML binding

Status: IN PROGRESS — phases 1 and 2 SHIPPED 2026-09-11 (bd072179d);
phase 3 (ImportXHTML-side test) DONE 2026-09-11 in the ImportXHTML working
tree (uncommitted); phase 4 (8.3.x backport) open; phase 5 out
(see "Decisions")
Scope: how the OpenDoPE binding traversers configure the `XHTMLImporterImpl`
they construct for `od:ContentType=application/xhtml+xml` content controls.
Primarily the three `FormattingOption` setters; a general extension point is
surveyed as well.  Must be backportable to `VERSION_8_3_15` (→ 8.3.16).
Phases: 1. property-driven setters in docx4j-core; 2. programmatic hook
(optional); 3. test in docx4j-ImportXHTML; 4. backport to 8.3.x;
5. (optional) same for altChunk conversion
Related: docx4j-ImportXHTML CR-001 (fonts honour the run FormattingOption) —
required for "styles only" fonts; see interaction 1.  IMPLEMENTED 2026-09-11
on ImportXHTML `VERSION_17_1_1`, commit a8ec734 (ships in ImportXHTML 17.1.1; verified here:
`RunFormattingFontTest` 5/5 green against docx4j 17.1.1-SNAPSHOT; not
backported to ImportXHTML 8.3.x — Jason)

## Background

A user binds XHTML into content controls and wants the importer run with

    importer.setRunFormatting(FormattingOption.CLASS_TO_STYLE_ONLY);

(and potentially `setParagraphFormatting` / `setTableFormatting` likewise), so
that `@class` values in the bound XHTML map to Word styles and inline CSS is
*not* converted to direct formatting.  Today that is impossible without
forking `BindingTraverserXSLT`: the importer is created inside
`BindingTraverserXSLT.convertXHTML` and there is no switch — not in
`docx4j.properties`, not in `docx4j-ImportXHTML.properties`, not in code — in
8.3.15 or in the current line.

### How the importer is constructed today

docx4j-core has **no compile-time dependency** on docx4j-ImportXHTML (it is
LGPL and optional), so `convertXHTML` does everything by reflection:

1. `Class.forName("org.docx4j.convert.in.xhtml.XHTMLImporterImpl")`, then the
   `(WordprocessingMLPackage)` constructor — a **fresh importer per content
   control**.
2. Loops over `getMethods()` and invokes, if present: `setBookmarkIdNext`,
   `setSequenceCounters`, `setMaxWidth(-1, null)`.
3. If inside a table cell, `BindingTraverserTableHelper.setupMaxWidthAndStyleForTc`
   invokes `setMaxWidth(int, String)` again (image scaling; the String feeds
   the image handler, not table styling).
4. Evaluates the XPath, then — depending on
   `docx4j.model.datastorage.BindingTraverser.XHTML.PrioritiseRPr` (since
   11.5.12, default false) or the deprecated `...XHTML.Block.rStyle.Adopt` —
   wraps or decorates the XHTML string with CSS derived from the SDT's
   effective `rPr`/`pPr`.
5. `setHyperlinkStyle(String)` from `BindingHandler.getHyperlinkResolver()`.
6. `convert(String, String)`; with `PrioritiseRPr=true`, post-processes the
   result with `PFromXHTMLVisitor` / `RFromXHTMLVisitor`.

All three traversers reach this code: `BindingTraverserXSLT` calls it as an
XSLT extension function, and `BindingTraverserNonXSLT` (the 17.0.4+ default)
via `BindingTraverserCommonImpl.applyXHTMLBinding`, which delegates to the
same static `convertXHTML` (CR-001 phase 5); `BindingTraverserStAX` delegates
each intercepted sdt's subtree to a `BindingTraverserNonXSLT` (its inner
`StaXBindingHandler`, a `SdtStAXHandler` subclass, news one up per sdt), so
it arrives there too — CR-001 row 15 has all three at
parity and `TextBindParityTest` asserts StAX output equals XSLT output for
the XHTML altChunk fallback.  (An earlier draft of this CR said StAX had no
XHTML support; that was a grep of the wrong file.)  So there is exactly
**one construction site for binding**,
and a second, unrelated one for altChunk conversion in
`JaxbXmlPartAltChunkHost.convertAltChunks` (XHTML/MHT altChunks → WML).

In 8.3.15 the code is the same method, at the same place
(`docx4j-core/src/main/java/org/docx4j/model/datastorage/BindingTraverserXSLT.java`,
`convertXHTML` from line 626), minus the `PrioritiseRPr` branch and the
post-import visitors (those arrived in 11.5.12).  Only the XSLT traverser
supports XHTML on that line.  `Docx4jProperties` has the identical public API
on both branches.

### What the setters do (ImportXHTML side)

`FormattingOption` = `CLASS_TO_STYLE_ONLY | CLASS_PLUS_OTHER | IGNORE_CLASS`,
default `CLASS_PLUS_OTHER` for all three, per importer instance (private
fields in `XHTMLImporterImpl`, setters declared on the `XHTMLImporter`
interface).  Present in ImportXHTML 8.3.15 and 17.x alike, so **no ImportXHTML
change is needed** for options A and C below to *call* the setters.  (Fonts
are the exception: making `CLASS_TO_STYLE_ONLY` actually cover `w:rFonts`
needed an ImportXHTML fix — its CR-001, in 17.1.1; see interaction 1.)

- Runs (`formatRPr`): `@class` (first token) → `w:rStyle` if a character
  style of that id exists; CSS → direct `rPr` only under `CLASS_PLUS_OTHER`
  or `IGNORE_CLASS`.  **Before ImportXHTML 17.1.1, `font-family` was applied
  regardless** of the option (`FontHandler.setRFont` ran after the switch);
  from 17.1.1 it is skipped under `CLASS_TO_STYLE_ONLY` unless
  `docx4j-ImportXHTML.Fonts.IgnoreRunFormattingOption=true`.
- Paragraphs (`populatePPr`): `@class` → `w:pStyle` if a paragraph style of
  that id exists; heading elements map to Heading styles when
  `docx4j-ImportXHTML.Element.Heading.MapToStyle` is on; CSS → direct `pPr`
  only under `CLASS_PLUS_OTHER`/`IGNORE_CLASS`.
- List paragraphs: under `CLASS_TO_STYLE_ONLY` an `ol`/`ul` **without** a
  `@class` naming a numbering style gets no numbering at all ("since we have
  no @class, do nothing"); each nesting level needs its own `@class`.
- Tables (`TableHelper.setTableStyle`): `@class` → `w:tblStyle`; CSS borders /
  widths → direct `tblPr` only under `CLASS_PLUS_OTHER`/`IGNORE_CLASS`.
- The deprecated `Block.rStyle.Adopt` special case *inside ImportXHTML*
  (setting `pStyle` on a numbered paragraph) only fires under
  `CLASS_PLUS_OTHER`.

## Interactions to document (whichever option is chosen)

### First, what `PrioritiseRPr` is

When XHTML is bound into a content control, two sources can each say
something about how the result should look: the **content control's own
formatting**, and the **XHTML's own CSS**.  The control's formatting is
`w:sdtPr/w:rPr` — the easiest way to author it is to select the control in
Word and apply direct formatting (font, size, colour...); a character style
via `w:rPr/w:rStyle` works too.  There is no `w:sdtPr/w:pPr`, so paragraph
properties can only come from the paragraph style *linked* to that
`rStyle` (an experimental route: Word's UI doesn't render it) or, failing
that, from the document's default paragraph style.

`PrioritiseRPr` says which source wins when both speak to the same property:

- **false (the default)**: the XHTML wins.  The control's formatting is the
  *baseline* — `convertXHTML` resolves the effective `rPr` (and `pPr`, via
  the linked style) with `PropertyResolver`, turns it into CSS with
  `HtmlCssHelper.createCss`, and wraps the XHTML string in
  `<div style="...">` (block) or `<span style="...">` (inline) before
  handing it to the importer.  Anything the XHTML itself declares, being
  inner, overrides that CSS in the normal cascade.
- **true**: the control wins.  The XHTML is imported as-is, then the result
  is walked with `PFromXHTMLVisitor` / `RFromXHTMLVisitor`: every paragraph
  gets the linked (or default) `pStyle`, and on every run the properties
  that the effective `rPr` defines are unset and the control's `w:sdtPr/w:rPr`
  applied over the top.  Meant for the case where you don't control the
  XHTML but want it harmonised with the document.

Where it is set: the `docx4j.properties` key
`docx4j.model.datastorage.BindingTraverser.XHTML.PrioritiseRPr`
(`true`/`false`; absent = false), or programmatically
`Docx4jProperties.setProperty(...)` before binding — JVM-wide, like every
other key.  It is read inside `BindingTraverserXSLT.convertXHTML` at two
points: before `convert` (build the wrapper or not) and after (run the
visitors or not).  Introduced in 11.5.12, where it superseded the deprecated
`...XHTML.Block.rStyle.Adopt` (which injects `@class` = the linked
paragraph style's class chain and `@style` = the control's direct `rPr` into
the XHTML elements, and only when the `rStyle` has a linked paragraph
style).  8.3.15 predates `PrioritiseRPr`, so on that line only the
`rStyle.Adopt` route exists (interaction 3).

### The interactions

These are the non-obvious consequences of switching to `CLASS_TO_STYLE_ONLY`
from the binding path; they belong in the property comments and the
CHANGELOG entry.

1. **`PrioritiseRPr=false` (17.x default)** wraps the XHTML in
   `<div style="...">` / `<span style="...">` carrying the SDT's effective
   `pPr`/`rPr` as CSS.  With `RunFormatting=CLASS_TO_STYLE_ONLY` the run CSS
   is discarded, *except* `font-family` (see above), so the SDT's font still
   becomes direct `w:rFonts` on every run while its size/colour/etc. vanish.
   With `ParagraphFormatting=CLASS_TO_STYLE_ONLY` the paragraph CSS (indent,
   spacing) is discarded too.  Net effect: the wrapper degenerates to a
   font-only override.
   **But the font leak is not the wrapper's doing** (traced 2026-09-11): the
   importer applies `font-family` *unconditionally*, outside its
   `FormattingOption` switch — a 2013 ordering accident ("TODO: review
   this"), never revisited.  Its CSS map always holds a computed
   `font-family` (renderer default `serif`), and `FontHandler` maps the
   generic families and every Microsoft font name, so under
   `CLASS_TO_STYLE_ONLY` **every run gets `w:rFonts` regardless**: the
   control's font via the wrapper, or Times New Roman without it.  Dropping
   the wrapper only swaps one for the other.  **Fixed in ImportXHTML** (its
   CR-001, `docx4j-ImportXHTML/docs/change-requests/`, implemented
   2026-09-11 on `VERSION_17_1_1`): the single `setRFont` call at the tail
   of `formatRPr` is now guarded — it runs unless `runFormatting` is
   `CLASS_TO_STYLE_ONLY`, or the legacy opt-out
   `docx4j-ImportXHTML.Fonts.IgnoreRunFormattingOption` is `true` (default
   false).  `CLASS_PLUS_OTHER` / `IGNORE_CLASS` unchanged.  Verified:
   `RunFormattingFontTest` (5 cases: class-only gives rStyle and no rFonts;
   no default-font leak; the other two options still map fonts; the
   property restores the old behaviour) passes against docx4j
   17.1.1-SNAPSHOT.  Not backported to ImportXHTML 8.3.x.
   Consequences for this CR: "styles only" for fonts in binding needs
   **ImportXHTML ≥ 17.1.1** (say so in the property comment); with it the
   wrapper guard here is merely tidy (nothing left to leak) — still
   recommended, but no longer load-bearing.  On the 8.3.16 backport
   (phase 4) the leak remains: see that phase.
2. **`PrioritiseRPr=true`** post-processes the import result:
   `PFromXHTMLVisitor` sets `pStyle` on **every** paragraph to the SDT-linked
   style (or the document default), which **clobbers the `@class`-derived
   `pStyle`**; `RFromXHTMLVisitor` applies the SDT `rPr` over the run,
   overriding a class-derived `rStyle` whenever the SDT has its own `rStyle`.
   So `PrioritiseRPr=true` + `CLASS_TO_STYLE_ONLY` largely defeats the point.
   Document as "not a useful combination"; do not try to reconcile them in
   this CR.
3. **8.3.15** has neither of those; its `Block.rStyle.Adopt` path injects
   `@class` (style-tree class chain; ImportXHTML uses the first token) and
   `@style` (CSS of the SDT's direct `rPr`) into the XHTML.  Under
   `CLASS_TO_STYLE_ONLY` the `@class` part still works and the `@style` part
   is dropped — a coherent result, no special handling needed.
4. **Lists** need `@class` on every `ol`/`ul` level (see above) or they
   silently lose their numbering.  This is the most likely support question.
5. **Hyperlink style**, **heading mapping**, **image max-width in table
   cells** and the bookmark/sequence counters are independent of the
   formatting options; nothing changes there.

## Options surveyed

### A. `docx4j.properties` keys, applied by reflection in core (recommended)

New keys, named alongside the existing XHTML binding keys:

    docx4j.model.datastorage.BindingTraverser.XHTML.RunFormatting=CLASS_TO_STYLE_ONLY
    docx4j.model.datastorage.BindingTraverser.XHTML.ParagraphFormatting=CLASS_PLUS_OTHER
    docx4j.model.datastorage.BindingTraverser.XHTML.TableFormatting=CLASS_PLUS_OTHER

Value = the `FormattingOption` enum constant name.  Absent or blank = do not
call the setter (importer default, i.e. no behaviour change for existing
users).  Unknown value = `log.warn` and skip.

Implementation: after the existing `getMethods()` loop in `convertXHTML`,

    Class<?> enumClass = Class.forName("org.docx4j.convert.in.xhtml.FormattingOption");
    Object opt = Enum.valueOf((Class<Enum>) enumClass, value.trim());
    xhtmlImporterClass.getMethod("setRunFormatting", enumClass).invoke(xHTMLImporter, opt);

(× 3, table-driven), factored into a small package-private helper so the
altChunk site (phase 5) can reuse it.

Pros: matches how every other binding switch works (`PrioritiseRPr`,
`Block.rStyle.Adopt`, `BindingHandler.Implementation`); no ImportXHTML
change or release; ports to 8.3.15 verbatim (same method, same
`Docx4jProperties` API, Java 8 fine); the user's ask is literally a setting.
Cons: JVM-global like all `docx4j.properties` keys; a string method name
and enum name held together by convention (mitigated by warn-and-skip, and
by the reflection already being the established pattern here).

### B. `docx4j-ImportXHTML.properties` keys read by `XHTMLImporterImpl` itself

e.g. `docx4j-ImportXHTML.Formatting.Run=CLASS_TO_STYLE_ONLY` as the initial
value of the private fields, via `ImportXHTMLProperties`.

Pros: one place; no reflection; applies to *every* importer (binding,
altChunk, direct callers) without core changes.
Cons: needs an ImportXHTML release **and** an 8.3.x ImportXHTML release for
the backport (two artifacts to upgrade, and the ImportXHTML 8.3.x line has
its own cadence); the scope is "all importers in the JVM", which is wider
than asked — a direct caller who also sets nothing would be affected by a
key intended for binding; cannot express "binding only".  Not recommended as
the primary mechanism; it could be added later as an importer-wide default
without conflicting with A (A's setter call would simply override it).

### C. Programmatic hook on `BindingHandler` (recommended as the escape hatch)

Follow the `setHyperlinkResolver` / `setValueInserterPlainText` precedent
(static setters on `BindingHandler`, present in 8.3.15 too):

    public interface XHTMLImporterCustomizer {
        /** @param xhtmlImporter an org.docx4j.convert.in.xhtml.XHTMLImporterImpl (cast it; core cannot name the type) */
        void customize(Object xhtmlImporter, SdtPr sdtPr, boolean inTableCell);
    }
    BindingHandler.setXHTMLImporterCustomizer(XHTMLImporterCustomizer c);

Invoked immediately before `convert`, i.e. after the built-in setters, the
property-driven setters (A) and `setHyperlinkStyle`, so it wins.  `Object`
because core cannot reference ImportXHTML types; the caller has ImportXHTML
on the classpath and casts.  `SdtPr` gives the tag (hence the `od:xpath` id)
for per-control decisions.

Pros: full power — `setCssWhiteList`, `setDivHandler`, `addFontMapping`, any
future setter — with no further core changes; type-safe on the user's side;
per-control variation possible.
Cons: code rather than configuration; static (same as its precedents);
`Object` in a public signature is slightly ugly.  Java 8 compatible, so it
backports.

### D. A + C together (recommended)

A covers the concrete request and the common case as a setting; C means the
next such request ("can I set the CSS white list from binding?") needs no
core change.  Phase 2 is marked optional so A can ship alone if Jason prefers
to keep the surface small.

### E. Per-control control via the OpenDoPE tag

e.g. `od:xhtmlFormatting=classOnly` in the `w:tag` query string, or an
attribute on the `od:xpath` entry, letting the template author choose per
content control.

Pros: finest granularity; template-driven rather than JVM-driven.
Cons: extends the OpenDoPE convention (spec, Word authoring add-in, the
`od:` parts); far more work than asked for; a setting was requested.  Not
proposed now; C already allows per-control logic in code if a user needs it,
and E could be layered on A/C later without conflict.  Recorded so the
question is not re-surveyed.

## Recommendation

D: phase 1 (A) is the deliverable and the backport; phase 2 (C) optional;
plus the wrapper guard from interaction 1.  Property keys as named above.
Do not touch the `PrioritiseRPr` visitors (interaction 2) beyond documenting.

## Plan

### Phase 1 — property-driven setters (docx4j-core, current line)

**SHIPPED 2026-09-11** (bd072179d).  As planned,
with one refinement: the wrapper guard is per level, not all-or-nothing —
`ParagraphFormatting=CLASS_TO_STYLE_ONLY` drops the pPr-derived CSS,
`RunFormatting=CLASS_TO_STYLE_ONLY` drops the rPr-derived CSS (fonts
included), and the wrapper element is omitted only when nothing is left; so
paragraph CSS still reaches a `CLASS_PLUS_OTHER` paragraph level when only
runs are styles-only.  Helper is `XHTMLImporterFormatting` (public, so the
altChunk site can reuse it if phase 5 is ever wanted).  Covered by
`XHTMLImporterFormattingTest` (6 cases) using a fake importer and a
test-only `org.docx4j.convert.in.xhtml.FormattingOption` stand-in in
docx4j-core-tests — deliberately no stand-in `XHTMLImporterImpl`, since
`TextBindParityTest` relies on that class being absent for the altChunk
fallback (re-run: still green).

- `BindingTraverserXSLT.convertXHTML`: apply the three keys via a new
  package-private helper (`XHTMLImporterFormatting.apply(Object importer,
  Class<?> importerClass)` or similar; name to taste) after the
  `getMethods()` loop.  Warn-and-skip on unknown value or missing method
  (older ImportXHTML without a setter).
- Wrapper guard (interaction 1), if decided.
- `docx4j-samples-resources/.../docx4j.properties`: the three keys, commented
  out, with the interactions above summarised in the comments (lists!).
- `CHANGELOG.md` entry (terse, per the usual style).
- Nothing in docx4j-core-tests can exercise it (ImportXHTML is not on that
  classpath, by CR-001's decision); the property parsing is trivial enough
  that the ImportXHTML-side test in phase 3 is the real coverage.

### Phase 2 — `XHTMLImporterCustomizer` hook (optional)

**SHIPPED 2026-09-11** with phase 1 (same commit).  `customize(Object
importer, SdtPr sdtPr, boolean inTableCell)`; static get/set on
`BindingHandler`; invoked after `setHyperlinkStyle`, immediately before
`convert`; exceptions from it are logged, not propagated.

- Interface in `org.docx4j.model.datastorage`; static get/set on
  `BindingHandler`; call site immediately before `convert`.
- Javadoc example showing the cast to `XHTMLImporterImpl`.

### Phase 3 — test (docx4j-ImportXHTML repo, `docx4j-ImportXHTML-core-tests`)

**DONE 2026-09-11**: `BindingFormattingOptionsTest`
(`org.docx4j.convert.in.xhtml.tests`), 5 cases, green against docx4j
17.1.1-SNAPSHOT.  Builds its package programmatically (custom XML part holding
the escaped XHTML, an XPaths part with one entry, and an
`od:xpath=x1&od:ContentType=application/xhtml+xml` block sdt with a matching
`w:dataBinding`), so no binary resource.  The XHTML has `@class` naming a
paragraph, a character and a table style, each element also carrying inline
CSS.  Cases: keys unset → `pStyle`/`rStyle`/`tblStyle` *plus* direct `ind`,
`jc`, `color`, `rFonts`, `b` (today's behaviour); all three keys
`CLASS_TO_STYLE_ONLY` → the styles and none of that direct formatting, under
the default (NonXSLT) traverser and again with
`BindingHandler.Implementation=BindingTraverserXSLT`; `RunFormatting` alone
→ run CSS gone, paragraph `ind` kept (the keys are independent); an unknown
value → ignored, behaves as unset.  Keys are removed from `Docx4jProperties`
in `@After`.  (ImportXHTML commit hash: to be recorded when committed.)

That module has both docx4j-core and ImportXHTML on the classpath.  A
binding test: template with an XHTML-bound control, XHTML with `@class`
naming a character style and a paragraph style plus inline CSS; with the
three keys set (via `Docx4jProperties.setProperty`) assert `rStyle`/`pStyle`
present and no direct `rPr`/`pPr` from the CSS; and the unset case asserts
today's behaviour.  Needs the docx4j version bump in that pom (or a
SNAPSHOT) — lands after the core release, or against the snapshot.

### Phase 4 — backport to `VERSION_8_3_15` (→ 8.3.16)

- Same helper + call in the 8.3.15 `convertXHTML` (line ~667, after the
  `getMethods()` loop); no `PrioritiseRPr` code there so the wrapper guard
  does not apply — instead confirm interaction 3 (the `Block.rStyle.Adopt`
  injection) behaves as described.
- Phase 2, if shipped, ports unchanged (`BindingHandler` hooks exist there).
- `docx4j.properties` reference + CHANGELOG on that branch.
- Verify against ImportXHTML 8.3.15 (the `docx4j-ImportXHTML-8_3_x` checkout
  has the setters at `XHTMLImporter.java:45-47`).  The 8.3.x line is Java 8
  (`<source>1.8</source>`): no `List.of`, no `var`.
- **Known limitation on 8.3.x**: ImportXHTML CR-001 is not backported, so
  with ImportXHTML 8.3.15 `RunFormatting=CLASS_TO_STYLE_ONLY` still emits
  `w:rFonts` on every run (interaction 1).  The setting is still useful there
  (size, colour, bold etc. do come from styles only); document the font
  caveat in the 8.3.x property comment and CHANGELOG rather than trying to
  strip `w:rFonts` in core.

### Phase 5 — altChunk conversion (optional, separate decision)

`JaxbXmlPartAltChunkHost.convertAltChunks` builds an importer the same way.
Reusing the helper there is a one-liner, but whether the *same* keys should
govern altChunk conversion (a different use case: Word-authored altChunks,
BIRT `.mht` output) is a product decision; if wanted, prefer distinct keys
(e.g. `docx4j.openpackaging.parts.AltChunk.XHTML.*`) defaulting to the
binding keys' values.  Out of scope unless asked.

## Decisions

1. A + C: Jason, 2026-09-11 ("good to implement phases 1 and 2").
2. Wrapper guard: implemented, per level (see phase 1); the property comment
   says fonts need ImportXHTML ≥ 17.1.1 (that fix is ImportXHTML CR-001, not
   this guard).
3. Phase 5 (altChunk): out for now, per the recommendation; nobody has
   asked for it.
4. Backport target version: 8.3.16 assumed; not yet confirmed (phase 4 open).

## Risks

- Low: everything is opt-in (keys absent = today's behaviour) and confined
  to one method plus a hook.
- The list-numbering loss under `ParagraphFormatting=CLASS_TO_STYLE_ONLY`
  (interaction 4) is ImportXHTML's existing behaviour, not something this CR
  introduces, but users will meet it through this switch first; the property
  comment must say so.
- Reflection against method names: a future ImportXHTML rename would
  silently disable the setting (warn-and-skip).  Acceptable; the whole
  construction path already has this property.
