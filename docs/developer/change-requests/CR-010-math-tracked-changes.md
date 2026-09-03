# CR: Tracked changes inside equations (issue #348)

Status: DONE (2026-09-03) — phases 1 and 2 shipped in 992c8d572 (schema +
regenerate + round-trip tests; consumer traversal with accepted-revisions
semantics).  Open decision resolved by Jason: rendering deleted math
struck-through in HTML/PDF is DEFERRED.  Full core suite 540 green,
markdown module 139 green; sample docx is now the committed test fixture
docx4j-core-tests/src/test/resources/tracked-changes-equations.docx.
Phase 3 (CHANGELOG + issue comment) with this status update.
Scope: schema (`xsd/wml/wml.xsd`, `xsd/shared/shared-math-2ed.xsd`) + regenerated
`org.docx4j.math.CTR`; the OMML consumers that walk math run content
Related: issue #348 (2019, still open — verified reproducible 2026-09-03)

## Background

When revisions are tracked inside an equation, Word wraps math run content in
`w:ins`/`w:del` **inside `m:r`**:

```xml
<m:r>
  <w:del w:id="0" w:author="…" w:date="…" w16du:dateUtc="…">
    <m:rPr><m:sty m:val="p"/></m:rPr>
    <w:rPr><w:rFonts w:ascii="Cambria Math"/></w:rPr>
    <m:t>s^{-1}</m:t>
  </w:del>
</m:r>
```

Notes on Word's serialization, from a current-Word sample
(`tmp/CR-010 eq_del sample.docx`, kept as the test corpus for this CR):

- the wrapper can contain `m:rPr` as well as `w:rPr` and `m:t` — the *whole*
  math-run content moves inside it, in the same order as in a plain `m:r`;
- deleted math text stays `m:t` (there is **no** `m:delText` analogue);
- one wrapper per `m:r` in every observed case, but the model should not
  assume that;
- `w16du:dateUtc` appears alongside the usual track-change attributes
  (handled — or not — the same as on regular runs; parity is the bar).

## Gap analysis (verified by probe, 2026-09-03)

- `w:ins` / `w:del` inside `m:r`: **rejected** — math `CT_R`'s content model
  (per ECMA-376: `m:rPr?, w:EG_RPr?, (w:EG_RunInnerContent | m:t)*`) has no
  revision wrappers.  Under docx4j's lenient unmarshalling the wrapper *and
  everything inside it* is dropped: the equation content is silently lost,
  and a load→touch→save round trip writes the equation back empty.  This is
  the substance of issue #348, and it is data loss, not just a read failure.
- `w:ins` / `w:del` inside `m:ctrlPr` (whole-structure insert/delete, eg an
  inserted `m:sSup` or deleted `m:acc`): **already works** — ECMA's
  `w:EG_RPrMath` group (`choice(EG_RPr | ins | del)`, type `CT_RPrChange`)
  is referenced by math `CT_CtrlPr`, and round-trips today.  Out of scope.

## Design

The obvious fix — admitting `w:ins` of the existing `CT_RunTrackChange` type —
does NOT work: that type's content model holds whole runs (`w:r`, `w:sdt`, …),
not the bare `m:rPr`/`w:rPr`/`m:t` Word writes here, so JAXB would accept the
wrapper and then drop its children: the same loss, one level down.

Instead (mutual wml↔math xsd imports already exist, so this is legal):

1. `xsd/wml/wml.xsd`: new complexType `CT_MathRunTrackChange` — the
   track-change attribute group (`id`, `author`, `date`, as `CT_TrackChange`)
   with content `(m:rPr?, EG_RPr?, (EG_RunInnerContent | m:t)*)` — plus a new
   group `EG_MathRunTrackChanges` = `choice(ins | del)` of that type (local
   elements, so they serialize as `w:ins`/`w:del`).
2. `xsd/shared/shared-math-2ed.xsd`: add `<xsd:group
   ref="w:EG_MathRunTrackChanges"/>` to `CT_R`'s repeating choice.
3. Regenerate (`ModifyGeneratedSources` is unaffected — none of its search
   strings touch `math.CTR`'s changed region; verify on first build).

## Consumers to update

Once the model holds the wrappers, everything that walks `math.CTR` content
must see through them.  Semantics decision (OPEN — see below): `ins` content
is included; `del` content is excluded by default (the "accepted revisions"
view), matching how a reader of the final document sees the equation.

- `org.docx4j.convert.out.mathml.OmmlToMathML` (HTML + PDF pathways both
  route through it)
- `org.docx4j.markdown.math.OmmlToLatex`, and the markdown exporter's
  flatten-to-text fallback
- text extraction over math runs (verify `TextUtils`; its reflective
  traversal may handle the new content list entries automatically)
- OpenDoPE / binding does not touch math run internals — no change expected

## Open decision

Whether `w:del` inside equations should ever be *rendered* (HTML/PDF show
regular deleted text struck through in red).  Rendering deleted math inline
is genuinely hard (struck-through fragments inside a live equation layout);
proposal: phase 1 ships the accepted-revisions view (ins in, del out) with
full model fidelity (nothing lost on round-trip), and rendering of deletions
is deferred unless someone asks.

## Phases

1. Schema + regenerate + round-trip tests (the sample docx above, plus
   programmatic cases for ins and del, including m:rPr inside the wrapper;
   assert byte-level content survival load→save).
2. Consumer traversal (OmmlToMathML, OmmlToLatex, markdown fallback) with
   the ins-in/del-out semantics; tests per converter.
3. CHANGELOG; comment on and close issue #348.

## Risks / notes

- Schema surgery regenerates `org.docx4j.math.CTR` with new content-list
  element types: any user code doing exhaustive `instanceof` over math run
  content will now see a new type.  Additive, and precedented (that list
  already mixes m: and w: types).
- The `\,s^{-1}`-style text in the sample is LaTeX-flavoured linear text
  from a markdown-imported document later edited in Word — incidental;
  the structures are what matter.
