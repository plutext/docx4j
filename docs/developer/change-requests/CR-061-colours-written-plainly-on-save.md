# CR-061: colours written plainly on save (Word's reading, in the schema's form)

Status: COMPLETE 2026-10-24 (phases 0 to 3, §8 and §9). ACCEPTED 2026-10-24 (Jason: "CR-061 6 questions decided as
recommended. Please start."): D1 to D6 as recommended (§6). PROPOSED 2026-10-24, drafted by Claude Opus 5.5 (session
docx4j-25) at Jason's word ("write the new CR with the recommended scope. No need to switch on/off to bare or 0").
Phases in §5.

## 0. Summary

CR-060 made docx4j read run properties as Word 365 reads them, and its D4 kept a colour as read on save: `red`,
`ff0000`, `#FF0000`, `" FF0000"` and invalid values like `F00` or `Auto` are written back as they came. Word reads
them all the same way again, so meaning is kept; but only six hex digits (and `auto`) are `ST_HexColor`, so the rest
are content a schema-checking reader rejects and other readers may misread. Jason's direction for docx4j-python
(2026-10-24): "A user who edits a file then blames us is a real concern: they are entitled to assume we don't write
dodgy content. I'd be inclined to make the normaliser opt-out." This CR applies it to docx4j's colours.

Fix: on save, every colour attribute docx4j writes is written in the schema's form, holding Word's reading: a value
Word accepts as upper-case `RRGGBB`; `auto` as `auto`; a value Word rejects dropped where Word reads it as absent (a
text or underline colour) and written `auto` where Word reads it as automatic (a fill or a border colour). The same in
a revision's former run properties (`w:rPrChange/w:rPr`), which Reject All restores, with CR-060's drop of an invalid
`highlight`, `vertAlign` or `em` extended there. On by default; a setting turns it off. On/off stays `true`/`false`
(CR-060's D2: valid in every edition, Strict included, and read exactly by Word). Small to medium.

## 1. Word's readings (measured, CR-060 §1, §12, §13)

| Attribute | A value Word accepts | `auto` | A value Word rejects (`F00`, `bogus`, `Auto`, `""`, ...) |
|---|---|---|---|
| `w:color/@w:val` | six hex digits in either case, optional `#`, trimmed; a classic HTML name in any case | automatic, overrides a style | absent: a style's colour shows |
| `w:u/@w:color` | the same | automatic | absent: a style's underline colour shows |
| `w:shd/@w:fill` | the same | no shading | automatic: no shading, overriding a style's (Word saves `auto`) |
| a border's `w:color` | the same | automatic | automatic: black, overriding a style's (Word saves `auto`) |

Word's own saves write exactly the forms §3 writes: upper-case `RRGGBB` for a name or any hex spelling (rounds 3,
4, 5), the rejected text or underline colour dropped (rounds 3, 5, 6), `auto` for the rejected fill or border colour
(round 6). Where a `w:color` also names a theme colour, the theme colour is drawn and `w:val` is its cache (CR-034).

## 2. docx4j today (CR-060 as implemented, 1d1b824a9)

- Reading follows §1 everywhere (`WordColors.normalise`, `WordColors.stated`, `Property.fillColour`,
  `Property.borderColour`).
- Writing keeps the value as read (CR-060 D4): `red` stays `red`, `F00` stays `F00`, in `w:color`, `w:u/@w:color`,
  `w:shd`, the borders and `w:background`. Word-authored files are unaffected (Word writes upper-case hex).
- A revision's former properties (`CTRPrChange.RPr`, `CT_ParaRPrOriginal`) are lists of elements, not fields, so
  CR-060's marshal-only adapters for `highlight`, `vertAlign` and `em` (`WordRepairs`, on `RPr` and `ParaRPr`'s
  fields) don't reach them; its on/off and `w:u` adapters do (they are on the shared classes).

## 3. Proposed approach

### 3.1 The normal form

| Attribute | Accepted value | `auto` | Rejected value |
|---|---|---|---|
| `w:color/@w:val` | `RRGGBB` | `auto` | the `w:color` element dropped; beside a `w:themeColor`, `w:val="auto"` (required; the theme colour still wins) |
| `w:u/@w:color` | `RRGGBB` | `auto` | the attribute dropped (the `w:u` keeps its `w:val`) |
| `w:shd/@w:fill` | `RRGGBB` | `auto` | `auto` |
| a border's `w:color` | `RRGGBB` | `auto` | `auto` |

A value already in the normal form is written as it is. The theme attributes (`w:themeColor`, `w:themeFill`,
`w:themeTint`, `w:themeShade`) are untouched. Two attributes of the same simple type that rounds 3 to 6 didn't
measure, `w:shd/@w:color` (the pattern colour) and `w:background/@w:color`, follow the fill's rule (D3).

### 3.2 Where: marshal-only, as CR-060's D3

The object in memory keeps the value as read; only what is written changes, on every marshal path (a part's save,
`XmlUtils.marshaltoString`). As CR-060's `WordRepairs`:

- Attribute adapters, `XmlAdapter<String, String>` (identity on unmarshal, the normal form on marshal), bound in
  `wml.xsd` by annox field annotations on `CT_Border/@color`, `CT_Shd/@fill` (and `@color`), `CT_Background/@color`,
  `w:u/@color` (a rejected value: null, so the attribute is left out) and `w:color/@val` (a rejected value: `auto`,
  reached only where the element is kept, i.e. beside a theme colour).
- An element adapter on `RPr.color` and `ParaRPr.color` (placed by `ModifyGeneratedSources`, as `WordRepairs`):
  a `w:color` whose value Word rejects and which names no theme colour is left out.
- The revision lists (D4): `CTRPrChange.RPr` and `CT_ParaRPrOriginal` get a class-defined `beforeMarshal` that swaps
  in a filtered copy of their list (dropping a `w:color` as above and a `highlight`, `vertAlign` or `em` whose value
  is missing or invalid) and an `afterMarshal` that puts the original back; colours inside are normalised by the
  attribute adapters as they are marshalled.

### 3.3 The opt-out (D1, D2)

A `docx4j.properties` setting, `docx4j.save.normalise` (default `true`), read by the adapters; and, for one package,
`OpcPackage.setNormaliseOnSave(boolean)`, which `JaxbXmlPart.marshal` honours by giving its marshaller
non-normalising adapter instances (`Marshaller.setAdapter`). Off, docx4j writes what it read as far as the binding
keeps it: colours as read, and CR-060's D3 drop off too; on/off is `true`/`false` either way (the binding's).

### 3.4 What it doesn't change

Reading (CR-060). On/off (`true`/`false`). DrawingML, VML and `w14` colours (other simple types, not measured). A
part docx4j never unmarshals (it is copied as it came).

## 4. What it affects

- A file whose producer wrote colour names, lower-case or `#`-prefixed hex, or values Word rejects: those colours
  are written as Word would write them, and the file validates where it didn't. Word's reading is unchanged.
- `w:rPrChange`: Reject All in Word gives what it gave before the save.
- The ports: docx4j-python's CR-018 §4.7 normal form is the same for colours (and keeps a source spelling with
  `save(normalise=False)`); core-ts CR-015 waits on this CR's answer to match docx4j (its oracle). The remaining
  difference is on/off's spelling (`true` here, bare there), which no reader distinguishes.

## 5. Phases

| Phase | What | Size |
|---|---|---|
| 0 | Red tests: a docx4j save of each colour probe (rounds 3 to 6, already fixtures) writes §3.1's form, and the save's reading equals Word's (the existing oracle); synthetic `w:rPrChange` and theme-colour cases; the opt-out keeps the source's spelling | 2 h |
| 1 | §3.2's attribute and element adapters; `wml.xsd` bindings, a clean regeneration; §3.3's opt-out | ½ day |
| 2 | §3.2's revision lists (D4) | 2 h |
| 3 | The Word check (Jason): docx4j's saves of the probes open in Word 365 without a repair prompt and draw as Word's PDFs of the probes; `w:shd/@w:color` and `w:background/@w:color` checked by a small probe (D3). CHANGELOG; hand-offs (core-ts, python, objects-ts, the portfolio) | 2 h + Jason's run |

## 6. Decisions (accepted as recommended, Jason, 2026-10-24)

| # | Decision | Recommendation |
|---|---|---|
| D1 | How the opt-out is given | the `docx4j.properties` setting (default on) and a per-package setter honoured by the save. Alternatives: the setting alone (simplest, global); a `Docx4J.save` flag (another FLAG_ constant) |
| D2 | What the opt-out turns off | this CR's normal form and CR-060's D3 drop together, so "off" means "write what was read" as far as the binding allows |
| D3 | `w:shd/@w:color` (pattern colour) and `w:background/@w:color`, unmeasured | include them under the fill's rule (same simple type, Word's parser presumably shared), and check them in phase 3's Word run |
| D4 | A revision's former properties (lists) | normalise them as live properties are, by swapping a filtered copy in for the marshal and putting the original back (the object in memory unchanged). Alternatives: normalise them in memory at save (simpler, but a save then changes the object); leave them as read |
| D5 | Telling the caller | each rewritten value logged at DEBUG (part, attribute, before, after); no report object. Alternative: a save report on the package, as docx4j-python's `last_save.normalised` |
| D6 | The object in memory | unchanged: only what is written is normalised (as CR-060's D3), so the opt-out can keep the source's spelling. Alternative: normalise at load (one place, but the source's spelling is gone) |

## 7. Sources

- CR-060 §1, §12, §13 (Word's readings and saves, rounds 1 to 6; probes and saves in core-tests
  `runprops/word365/`), §11 and §14 (what docx4j writes and reads now); CR-034 (theme colours).
- docx4j-python `docs/change-requests/CR-018-run-properties-as-word-reads-them.md` §4.7 (its normal form and
  opt-out; Jason's direction, 2026-10-24); docx4j-core-ts CR-015 (its D2 waits on this).
- ECMA-376 Part 1, `ST_HexColor`; Part 1 Strict, `ST_OnOff` (`xsd:boolean`).

## 8. Phases 0 to 2 record (2026-10-24, Claude Opus 5.5, session docx4j-25)

- Phase 0: `org.docx4j.model.properties.run.ColourNormalFormTest`, red against 1d1b824a9 (4 of 5: every colour a
  save writes out of form over the thirteen colour probes, 122 values; each rule against Word's own saves; a rejected
  value beside a theme colour; a revision's former properties); the fifth, the object in memory unchanged, holds
  either way. `ColourNormalFormOptOutTest` (the opt-out) came with phase 1's API. Found while making them green: a
  part docx4j never unmarshals is copied as it came, so a load-and-save that touches nothing writes the source's
  spelling; the tests read every XML part first, as a caller who edited the file has (§3.4).
- Phase 1: the colour rule moved beside the generated classes, `org.docx4j.wml.WordColorValues` (`normalise`,
  `stated`, the 140 names), since their adapters can't reach docx4j-core; `WordColors.normalise` and `stated`
  delegate, and `ColorTables` no longer holds the names. `org.docx4j.wml.NormaliseOnSave`: `ColourValue`
  (`w:color/@w:val`), `UnderlineColour` (`w:u/@w:color`), `FillOrBorderColour` (`CT_Shd/@fill`, `CT_Shd/@color`,
  `CT_Border/@color`, `CT_Background/@color`), bound in `wml.xsd` by annox; `ColourElement` on `RPr.color` and
  `ParaRPr.color` (ModifyGeneratedSources). Each is a `Switched` adapter: on per its own setting, else the default,
  which `org.docx4j.jaxb.Context` sets to read `docx4j.save.normalise`; `NormaliseOnSave.configure` gives a
  marshaller fixed instances, which `JaxbXmlPart.marshal` does for a package's `setNormaliseOnSave(Boolean)`.
  CR-060's `WordRepairs` adapters became `Switched` too (D2). Each rewrite is logged at DEBUG (D5). `CTColor`, a
  generated class no element uses, is left alone.
- Phase 2: `CTRPrChange.RPr` and `CTParaRPrOriginal` gained (ModifyGeneratedSources) a `beforeMarshal` that swaps in
  `NormaliseOnSave.filterOnSave`'s copy of their list (no `w:color` Word reads as absent; no `highlight`, `vertAlign`
  or `em` Word repairs away; the list itself where there's nothing to leave out, or normalising is off) and an
  `afterMarshal` that puts the original back. JAXB RI finds a class's declared lifecycle methods and makes them
  accessible; MOXy calls them too (probe).
- A `w:color` whose value is rejected is left out only where no theme attribute is beside it; with a `w:themeTint`
  or `w:themeShade` alone (no `w:themeColor`), the element is kept and its value written `auto`.
- MOXy (docx4j-JAXB-MOXy, scratch probe): the same output, the revision list filtered, the opt-out honoured.
- Suites: core-tests 2072 run, 0 failures, 30 skipped; docx4j-export-fo-tests 858, 0, 9; docx4j-diffx-tests green.

## 9. Phase 3 record, the Word check (2026-10-24, Jason's run; read by Claude Opus 5.5, session docx4j-25)

- docx4j's saves of fifteen probes (`<probe>-docx4j.docx`, made at aaadd93f9, every XML part read first): Word 365
  offered no repair on any, `props-enum-*` included, which Word repaired as probes. Word's save of each docx4j save
  equals Word's save of the probe at all 251 markers (run properties compared, fonts and language aside), and the PDFs
  are pixel-identical at 100 dpi, all fifteen pairs. Word reads docx4j's normal form exactly as it read the source.
- Round 7 (`build7.py`; fixtures since): a shading pattern's colour (`w:shd/@w:color`) follows the fill's rule, as
  D3 assumed: a name is drawn and saved as hex (`red` `FF0000`, `Navy` `000080`); `F00` is automatic (a 50% black
  pattern) and saved `auto`, over a character style's red pattern too. The page colour (`w:background/@w:color`)
  reads names (`red` `FF0000`, `Navy` `000080`), but Word drops a `w:background` whose colour it rejects (`F00`),
  where docx4j writes `w:color="auto"`: both valid and both no page colour (presumed; the PDF doesn't carry the page
  colour), so kept. Leaving the element out would need an element adapter on the document's `background`.
- docx4j doesn't draw a pattern colour (`RShading` fills only); unchanged here.
- CHANGELOG (the entry names pattern and page colours now); hand-offs to core-ts, docx4j-python, objects-ts and jsonix
  (the `wml.xsd` change is annotations only), and the portfolio.
