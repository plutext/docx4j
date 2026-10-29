# CR-058: A bound date formatted as Word formats it, and a store item ID that names no part resolved as Word repairs it

Status: COMPLETE 2026-10-23 (phases 0-2). ACCEPTED 2026-10-23 (Jason: D1-D5 as recommended; on D5, whether the document with the repaired
`w:storeItemID` is saved is the caller's decision, as with every other change the bind makes in memory). Proposed
earlier the same day, drafted by Claude Fable 5.1 (session docx4j-25) at Jason's request ("do a cr for the
remaining binding items the portfolio flagged"), from the two items docx4j-portfolio's text-extraction paper (§12a,
22 October 2026) lists as still open against the binding generator after CR-054 and the fixes of 17.3.4
(f62314d3b, 24642aa59). Both confirmed in this repository's source at 64ebb9ae9 (§1). Phases in §4. Phase 0 BUILT
2026-10-23 (§8): red tests and fixtures in core-tests, three probes run in Word 365 the same day. The Word run
confirms D1, D3 and D4 and revises D2 (§5a); it raises D6, the time zone of a stored date-time (§5a). D2 (revised)
and D6 ACCEPTED as recommended (Jason, 2026-10-23). Phase 1 DONE 2026-10-23 (§9); phase 2 DONE the same day (§10). COMPLETE.

## 0. Summary

1. **A bound date is formatted by Java's `SimpleDateFormat` with Word's format string nearly raw.**
   `BindingTraverserXSLT.xpathDateRun` (both traversers end there) translates one token, `dddd`, and hands the rest
   of `w:dateFormat` to `SimpleDateFormat` in the JVM's default locale; `w:lid` is ignored. Word's format string is
   its own date picture language, the one a DATE field's `\@` switch uses, so `ddd` (Word: "Thu") comes out as a
   three-digit day of the month ("022"), `am/pm` throws (`t` is no Java pattern letter, and so does `/`-adjacent
   `p`), and `MMMM`, `dddd` and `MMM` come out in the server's language, not the control's (`en-AU` on a German
   server gives "Oktober"). A value the stored XML holds that is not a date is shown as **today's date, in red**
   (`new Date()`), where Word shows the stored text. docx4j already has the right converter: the field code's
   `FormattingSwitchHelper.convertDatePattern` plus `formatDate(..., lang)`, which maps every Word token and takes
   a language. Fix: use it, with `w:lid` as the language. Small.
2. **A `w:storeItemID` that names no part aborts the bind, or empties the control.** `BindingHandler.xpathGetString`
   throws `InputIntegrityException("Couldn't locate part by storeItemId ...")`: the NonXSLT traverser fails the
   whole document on it; the XSLT traverser's error listener logs it and the transform goes on with the control's
   content gone (`<w:sdtContent/>`), so the text is lost without an exception (measured in phase 0, §8). Word
   repairs it: measured on the portfolio's `bind-plain.docx` (B7, store item ID
   `...00FF`, one custom XML part `...0001`), Word 365 showed the value and its save rewrote the control's
   `w:storeItemID` to the part's. The text model (`BoundValues`) gives null for such a binding, so the stored
   surface, where Word shows the value. Fix: a resolver that finds the part the XPath fits, shared by the generator
   and the text model; the generator writes the resolved ID back as Word does. Small to medium.

## 1. Confirmed in source, and measured

Source, VERSION_17_3_5 at 64ebb9ae9:

- `model/datastorage/BindingTraverserXSLT.java` `xpathDateRun` (~1970-2060): `format.replace("dddd", "EEEE")`
  is the only translation; `new SimpleDateFormat(format)` with no locale; the `CTSdtDate`'s `getLid()` is never
  read; on `DateTimeParseException` the date becomes `new Date()` and the run is coloured `FF0000`. Reached from
  `bind.xslt` (the `w:sdtPr/w:dataBinding and w:sdtPr/w:date` template, through `xpathDate`) and from
  `BindingTraverserCommonImpl.applyDateBinding` (the NonXSLT traverser): both traversers, one method.
- `model/fields/FormattingSwitchHelper.java`: `convertDatePattern(String)` maps Word's picture to Java's
  (`DATE_FORMAT_ITEMS_TO_JAVA`: `ddd`→`EEE`, `dddd`→`EEEE`, `am/pm`/`AM/PM`→`a`, quoted literals kept);
  `formatDate(model, format, date, lang)` formats in the language's locale (`getSimpleDateFormat(lang)`,
  `localeforLanguageTag`), measured against Word in 17.1.0 (`DateFieldLanguageTest`: a German `MMM.` and Word's
  own abbreviations).
- `model/datastorage/BindingHandler.java` `xpathGetString` (~455): `part==null` → `throw new
  InputIntegrityException(...)`. Out of the XSLT traverser it reaches Xalan as a `TransformerException`, which
  `XmlUtils`' error listener (line 1885) logs at ERROR and does not rethrow, so the `w:sdtContent` the template
  was writing is left empty; the NonXSLT traverser lets it out of `Docx4J.bind`. `xpathDateRun` (1983) logs an
  error and returns null for the same case, which the traversers turn into `[missing!]`.
  `model/text/BoundValues.document()` (85-110) returns null; `value()` null; the surface is shown.

Measured by docx4j-portfolio (Word 365, Jason, 22 October 2026; `~/vbShares/Office 2016/bind-fun/`):

| Probe | Word | docx4j today |
|---|---|---|
| `bind-plain.docx` B7: `w:storeItemID="{...00FF}"`, one part `{...0001}`, XPath `/p:root[1]/p:b7[1]` | shows `B7_XML_VALUE`; the save (`bind-plain-word365.docx`) has `w:storeItemID="{...0001}"` on B7 | NonXSLT: `InputIntegrityException`, bind aborted; XSLT: B7 emptied, the rest bound (generator); surface `B7_STALE_SURFACE` (text model) |
| `bind-typed.docx` DT1: `d MMMM yyyy`, `en-AU`, value `2026-10-22T00:00:00Z` | "22 October 2026" | the same on an English JVM (TypedControlBindingTest); "22 Oktober 2026" on a German one |
| LibreOffice 26.2.5, ONLYOFFICE on DT1 | the same formatted date | |

All of these were measured in phase 0 (§8, Word 365, Jason, 23 October 2026): `ddd`, `am/pm`, `h:mm`, `de-DE` on
an English machine; a stored value that is not a date; which part Word picks when two fit; none fitting; and, on the
second probe (§8, bind-typed-twelve), `h` at 12 local and explicit offsets in the stored value. Nothing is left unmeasured.

## 2. What it affects, and what it does not

Affected (1): every date control bound through either traverser (`Docx4J.bind`, OpenDoPE processing, the
`w:date` branch of `bind.xslt`). A format with `ddd`, `am/pm`, `h:mm am/pm` or `tt` today gives wrong text or an
exception the traverser turns into `[missing!]`; a format with month or weekday names gives them in the server's
language. The text model is not affected: it reads the stored surface for a date control (REQ-509), and the paper's
D34 proposes showing the formatted date there too, for which this CR's formatter is the shared piece.

Affected (2): every binding whose store item ID names no part: the generator aborts (an `InputIntegrityException`
out of `Docx4J.bind`, NonXSLT) or empties the control (XSLT, the default), the text model shows the surface. Common in practice: a template whose custom XML part was
replaced by a tool that minted a new `ds:itemID` (docx4j's own `CustomXmlDataStoragePartSelector` comments on the
well-known IDs; a part added by `addTargetPart` gets a fresh GUID), and Word repairs such documents silently on
open, so authors never see the mismatch.

Not affected: a binding to the core, extended or cover-page properties (well-known IDs, resolved before the lookup);
the OpenDoPE `od:xpath` path, which carries the same `w:dataBinding` and goes through the same `xpathGetString`
(so it is fixed by the same change); saved documents, except through the repaired `w:storeItemID` (D5).

## 3. What the specifications say

- ECMA-376 17.5.2.9 `w:dateFormat`: "the format which shall be used to display the date ... using the date and time
  formatting syntax" of the DATE field's `\@` switch (17.16.4.1): `d dd ddd dddd`, `M MM MMM MMMM`, `yy yyyy`,
  `h hh H HH`, `m mm`, `s ss`, `am/pm AM/PM`, literals in single quotes, other characters as written. That is the
  language `FormattingSwitchHelper` implements for fields.
- 17.5.2.20 `w:lid`: "the language ID which shall be used to display the date in the date picker and to format the
  date for display" (month and weekday names, AM/PM). 17.5.2.7 `w:date/@w:fullDate` holds the last picked date;
  17.5.2.40 `w:storeMappedDataAs` says whether the XML holds `text`, `date` or `dateTime`; 17.5.2.4 `w:calendar`
  (gregorian here; others out of scope).
- 17.5.2.6 `w:dataBinding/@w:storeItemID`: "the custom XML data part ... which this XPath shall be evaluated
  against", with no repair rule; Word's repair is behaviour, measured, not specified. OpenDoPE v3 WD REQ-509
  (bound view) and the paper's D34 follow Word.

## 4. Phases

| Phase | What | Size |
|---|---|---|
| 0 | DONE 2026-10-23 (§8). Red tests for both (core-tests): a `TypedControlBindingTest`-style fixture per format (`ddd`, `am/pm`, `de-DE`, a non-date value); `bind-plain.docx` as the B7 fixture, bound through both traversers and read by the text model. Probes for Jason's Word: the §1 "not measured" list, built from `bind-typed.docx` (one control per format, a non-date value) and `bind-plain.docx` (a second custom XML part that also fits; none that fits) | 2 h |
| 1 | DONE 2026-10-23 (§9). Date (D1-D3, D2 revised, D6): `xpathDateRun` formats through `FormattingSwitchHelper` with `w:lid`; the non-date case per D3 and the measurement; tests green; CHANGELOG | 2 h |
| 2 | DONE 2026-10-23 (§10). Store item (D4-D5): the resolver, shared by `BindingHandler.xpathGetString`, `xpathDateRun` (and the picture, XHTML and checkbox paths that look the part up themselves) and `BoundValues.document`; the write-back; tests green; CHANGELOG; the ports' hand-off (their binders: date language, missing-part repair) and the portfolio's §12a.3 | ½ day |

## 5. Decisions

| # | Decision | Recommendation |
|---|---|---|
| D1 | The date formatter | reuse `FormattingSwitchHelper.convertDatePattern` + `formatDate(..., lang)` (Word's picture language, already measured for fields), not a second token table in `xpathDateRun`; the one place both fields and controls format a Word date |
| D2 | The language | `w:lid` where present; else the control's `w:rPr/w:lang` (`w:val`) as the field path uses the run's; else the JVM default, as today. `Locale.forLanguageTag` |
| D3 | A stored value that is not a date | show the stored text as it is, in the control's formatting, with a WARN (Word's behaviour, to be confirmed by the phase 0 probe); today's date in red is a server's date leaking into a document and goes. If Word shows something else, follow Word |
| D4 | A store item ID naming no part | resolve to the custom XML part the XPath fits: the parts (not the property parts) whose root element's namespace and local name match the XPath's first step read with the binding's prefix mappings; exactly one → use it, INFO once per document; several → the first (part-name order) whose XPath yields a node, else the first that fits, WARN (which one Word picks is a phase 0 probe); none → the control is left as it is (its stored content stays, like Word's surface for an unresolved binding), WARN, no exception. The `InputIntegrityException` becomes opt-in: property `docx4j.model.datastorage.BindingHandler.strictStoreItemIds=true` restores it for a deployment that wants a bad template to fail |
| D5 | Write-back | the generator writes the resolved ID into the control's `w:dataBinding/@w:storeItemID` as Word's save does, through `postBindSdtPrHygiene` (NonXSLT) and the sdtPr template (XSLT), so the saved document binds without the repair next time. The text model writes nothing |

Accepted 2026-10-23: D1 to D5 as recommended. On D5 Jason adds that it is the user's code that decides whether the
repaired document is saved: the bind writes the resolved ID into the in-memory package, as it writes the bound
values, and docx4j saves nothing on its own.

### 5a. After the Word run (23 October 2026, §8): one revision, one new decision

Accepted as recommended, Jason, 23 October 2026: D2 (revised) and D6.

| # | Decision | Recommendation |
|---|---|---|
| D2 (revised) | The language where `w:lid` is absent | Word does not use the run's language: DF7 (no `w:lid`, run `fr-FR`) showed "22 October 2026", and Word's save wrote `w:lid="en-AU"`, its own editing language, into the control. A server has no editing language; the nearest document-side equivalent is the document's default language, `w:docDefaults/w:rPrDefault/w:rPr/w:lang/@w:val` (what Word's editing language becomes in a document it creates). Recommend: `w:lid`; else the document default language; else the JVM default, as today. The run's `w:lang` is not consulted. docx4j need not write a `w:lid` back |
| D3 (refined) | An empty stored value | Word shows the placeholder (`w:showingPlcHdr`, empty content), as it does for an empty text binding (bind-plain B5). Recommend the date path does what the text path does for an empty value: placeholder. The other non-dates (`not-a-date`, `2026-13-45`) are shown as stored, as D3 says; Word's save drops `w:fullDate` for them |
| D6 (new) | The time zone of a stored `dateTime` | Word reads the stored value's `Z` as UTC and displays it in the machine's zone: on Jason's machine (UTC+11) `2026-10-22T15:05:00Z` showed "2:05 AM" (the 23rd) and `...T12:05:00Z` "11:05 PM"; the save's `w:fullDate` carries the local time labelled `Z`. docx4j today ignores the offset and shows the nominal time ("3:05 PM", "12:05 PM"), which is also what every Word in UTC shows, and what Word's own date picker means by the value it writes (local midnight labelled `Z`; west of UTC Word then shows the previous day, a known Word wart). Recommend: nominal by default, as today (no server zone leaks into the document, no day shifts), with `docx4j.model.datastorage.BindingHandler.dateTimeZone` (an IANA zone ID) to convert as Word does for a deployment whose users share a zone. The tests expect the nominal reading |

## 6. Risks

- **D1 output changes** for a template whose format Java read differently by luck: `ddd` ("022") becomes "Thu",
  `MMMM` on a non-English server becomes the control's language. That is the fix; CHANGELOG names it.
- **D3** changes a visible marker (red today's date) to plain text; a deployment that relied on the red date to
  spot bad data loses it. The WARN names the control and value. Judged acceptable, since the red date is wrong
  data in a document.
- **D4** removes an exception some callers may catch to reject a template (NonXSLT only; the XSLT traverser never
  let it out). The property restores it, for both traversers.
- **Several parts fit (D4)**: a wrong pick binds a control to the wrong part's value. Word's own pick is to be
  measured; until then the rule is deterministic and logged.

## 7. Sources

- docx4j-portfolio `docs/text_extraction_behaviour_model.md` §12a.3 (Word 365 measurements, 22 October 2026),
  §12a.6 ("still open" list), D34 and "Not known yet"; the probes on `~/vbShares/Office 2016/bind-fun/`
  (`bind-plain.docx`, `bind-plain-word365.docx`, `bind-typed.docx`, README.txt).
- This repository: `BindingTraverserXSLT.xpathDateRun`, `BindingTraverserCommonImpl.applyDateBinding`,
  `bind.xslt` (the `w:date` template), `BindingHandler.xpathGetString`, `BoundValues.document`,
  `FormattingSwitchHelper.convertDatePattern` / `formatDate`, `DateFieldLanguageTest`; `TypedControlBindingTest`
  and the `OpenDoPE/typed-controls*.docx` fixtures.
- ECMA-376 Part 1: 17.5.2.4, 17.5.2.6, 17.5.2.7, 17.5.2.9, 17.5.2.20, 17.5.2.40, 17.16.4.1.
- CR-054 (the prefix mappings, done 64ebb9ae9), the 17.3.4 binding fixes (f62314d3b, 24642aa59).

## 8. Phase 0 record (2026-10-23, Claude Fable 5.1, session docx4j-25)

Built, uncommitted at writing. Fixtures are written as XML text by a python zipfile script (scratch `p0/build.py`,
after the portfolio's bind-fun builder), so the same bytes serve as the core-tests fixture and the Word probe.

**Fixtures** (`docx4j-core-tests/src/test/resources/OpenDoPE/`):

- `typed-controls-dates.docx` (= the probe `bind-typed-formats.docx`): one custom XML part, thirteen date controls
  DF1-DF13, surfaces `DFn_STALE`, stored values `2026-10-22T00:00:00Z` (d), `2026-10-22T15:05:00Z` (t),
  `2026-10-22T12:05:00Z` (noon), `not-a-date`, `2026-13-45`, empty, `2026-10-22` (date-only, `storeMappedDataAs`
  date). Formats: DF1 `ddd d MMM yyyy`, DF2 `h:mm am/pm`, DF3 `h:mm AM/PM` (noon), DF4 `HH:mm`, DF5 `d MMMM yyyy`
  de-DE, DF6 `dddd, d MMMM yyyy`, DF7 `d MMMM yyyy` with no `w:lid` and `w:rPr/w:lang` fr-FR, DF8-DF10 `d MMMM
  yyyy` on the three non-dates, DF11 `yyyy-MM-dd`, DF12 `'Day' d 'of' MMMM`, DF13 `MMM. yyyy` de-DE; en-AU
  elsewhere.
- `bind-plain.docx`: the portfolio's probe verbatim (B7's stale ID; Word's measured case).
- `bind-plain-two-parts.docx`: parts `{...0001}` (b1, b7 = `B7_XML_VALUE_PART1`) and `{...0002}` (b7 =
  `B7_XML_VALUE_PART2`, b11 = `B11_XML_VALUE_PART2`); B1 bound by its real ID; B7 and B11 name `{...00FF}`.
- `bind-plain-no-part.docx`: one part; B7 names `{...00FF}` with its XPath in `urn:probe:other`; B12 names
  `{...00FF}` with the XPath `/p:nothing[1]/p:b7[1]`, a root no part has.

**Tests** (`docx4j-core-tests`, `org.docx4j.model.datastorage`), `@Ignore`d until their phase:

- `BoundDateFormatTest`: `boundDatesAreFormattedAsWordFormatsThem{XSLT,NonXSLT}` (phase 1; the thirteen
  expectations in the source, D1-D3 and the field converter's reading, to be confirmed by the probe) and the green
  control `localeFreeFormatsAgreeToday` (DF4, DF11 on both traversers).
- `StoreItemRepairTest` (phase 2): `theGeneratorResolvesAStaleStoreItemIdAsWordDoes` (both traversers; B7's value
  and the ID written back), `theTextModelShowsTheValueWordShows`, `theStrictPropertyFailsTheBind`,
  `whereTwoPartsFitTheFirstThatYieldsANodeIsBound` (B7 from part 1, B11 from part 2; D4's rule, revised if Word's
  pick differs), `whereNoPartFitsTheControlIsLeftAndTheRestBinds`; green control
  `theTextModelShowsTheSurfaceWhereNoPartFits`.
- Run: `mvn -o test -pl docx4j-core-tests -Dtest='BoundDateFormatTest,StoreItemRepairTest,TypedControlBindingTest'`:
  14 run, 0 failed, 7 skipped. With the ignores lifted: 7 of the 9 fail (every red test; the two controls pass).

**Measured today** (both traversers alike unless said; scratch `p0/Probe.java` on the test classpath):

| Control | Today | Expected (CR) |
|---|---|---|
| DF1 `ddd d MMM yyyy` | `022 22 Oct 2026` (day of the year) | `Thu 22 Oct 2026` |
| DF2 `h:mm am/pm`, DF3 `h:mm AM/PM` | `[missing!]` (`IllegalArgumentException: Illegal pattern character 'p'`) | `3:05 PM`, `12:05 PM` |
| DF4 `HH:mm`, DF11, DF12 | `15:05`, `2026-10-22`, `Day 22 of October` | the same |
| DF5 de-DE, DF13 `MMM.` de-DE | `22 October 2026`, `Oct. 2026` (the JVM's English) | `22 Oktober 2026`, `Okt. 2026` |
| DF6 | `Thursday, 22 October 2026` (right on an English JVM only) | the same on any JVM |
| DF7 no `w:lid`, run fr-FR | `22 October 2026` | `22 octobre 2026` |
| DF8 `not-a-date`, DF9 `2026-13-45`, DF10 empty | `23 October 2026` in red (the server's date) | the stored text, no colour |
| bind-plain B7 | XSLT: B7 emptied (`<w:sdtContent/>`), the rest bound; NonXSLT: `InputIntegrityException`; text model: surface | `B7_XML_VALUE`, ID `{...0001}` |
| two-parts B7, B11; no-part B7, B12 | XSLT: emptied; NonXSLT: exception; text model: surfaces | B7 part 1, B11 part 2; no-part: surfaces kept, no exception |

**Findings for phase 1**, beyond the CR's list:

1. The XSLT traverser (the default) does not abort on a stale store item ID: the exception is swallowed by
   `XmlUtils`' transformer error listener and the control is emptied. §0, §1, §2 and §6 corrected above. The
   `strictStoreItemIds` property therefore makes both traversers fail, not "restores" an exception.
2. `FormattingSwitchHelper.DATE_FORMAT_ITEMS_TO_JAVA` maps Word's `h` to Java `K` (hour 0-11) and `hh` to `KK`;
   Word's `h` is 1-12 (17.16.4.1: "12:05 PM" at noon, where `K` gives "0:05 PM"). The DATE field has the same
   defect. Phase 1 corrects the table (`h`→`h`, `hh`→`hh`) for fields and controls alike, with a
   `DateFieldLanguageTest` case. DF3's value is 23:05 local at UTC+11, where `h` and `K` agree (the portfolio's
   reading of the run), so the second probe, `bind-typed-twelve.docx` (Jason, Word 365 at UTC+11, 23 October
   2026, the table below), settled it: 1-12.
3. `am/pm` against `AM/PM`: Word's documentation says both display upper case ("9 AM", "5 PM"); the converter maps
   both to Java `a`, which is upper case in English. DF2 and DF3 measure it; if Word writes "pm" for `am/pm`, the
   converter needs a lower-case variant (a `DateFormatSymbols` with lower-case AM/PM strings).
4. DF13 on the English JVM gives `Oct.` (CLDR's English abbreviation has no period; the format's does), so the
   trimming `wordMonthAbbreviations` applies is only visible in German; expected `Okt. 2026` with `w:lid`.

**Probes for Jason's Word** (`~/vbShares/Office 2016/bind-fun/`, README addendum there): `bind-typed-formats.docx`,
`bind-plain-two-parts.docx`, `bind-plain-no-part.docx`; run by Jason in Word 365 on 23 October 2026 (screenshots
`*.PNG`, saves `*-word365.docx`, machine zone UTC+11):

| Control | Word 365 shows | Word's save | Against the expectation |
|---|---|---|---|
| DF1 `ddd d MMM yyyy` | `Thu 22 Oct 2026` | `w:fullDate="2026-10-22T11:00:00Z"` | as expected |
| DF2 `h:mm am/pm` (15:05Z) | `2:05 AM` | `fullDate` `2026-10-23T02:05:00Z` | upper case AM/PM for `am/pm` as expected; the time is local (D6) |
| DF3 `h:mm AM/PM` (12:05Z) | `11:05 PM` | `fullDate` `2026-10-22T23:05:00Z` | local (D6); 12:05Z is 23:05 at UTC+11, so this run does not separate Word's `h` (1-12) from Java's `K` (0-11): both give 11 |
| DF4 `HH:mm` (15:05Z) | `02:05` | | local (D6) |
| DF5 `d MMMM yyyy` de-DE | `22 Oktober 2026` | | as expected |
| DF6 `dddd, d MMMM yyyy` | `Thursday, 22 October 2026` | | as expected |
| DF7 no `w:lid`, run fr-FR | `22 October 2026` | `<w:lid w:val="en-AU"/>` added | NOT the run's language: Word's editing language (D2 revised) |
| DF8 `not-a-date`, DF9 `2026-13-45` | the stored text, plain | `w:fullDate` dropped; XML unchanged | as D3 |
| DF10 empty | the placeholder (empty) | `w:showingPlcHdr`, `fullDate` kept at 2001-01-01 | D3 refined: as an empty text binding |
| DF11 `yyyy-MM-dd`, date-only | `2026-10-22` | `fullDate` `2026-10-22T00:00:00Z` | as expected; no zone shift for a date |
| DF12 `'Day' d 'of' MMMM` | `Day 22 of October` | | as expected |
| DF13 `MMM. yyyy` de-DE | `Okt. 2026` | | as expected |
| two-parts B7 (both parts have b7) | `B7_XML_VALUE_PART1` | `w:storeItemID` → `{...0001}` | D4: the first part, as expected |
| two-parts B11 (only part 2 has b11) | `B11_XML_VALUE_PART2` | `w:storeItemID` → `{...0002}` | D4: the first whose XPath yields, as expected |
| no-part B7, B12 | `B7_STALE_SURFACE`, `B12_STALE_SURFACE` (shown as unbound) | binding and stale ID kept as they were | D4: left as it is, as expected |

The second probe, `bind-typed-twelve.docx` (six controls, run by Jason the same evening; `bind-typed-twelve.PNG`,
`bind-typed-twelve-word365.docx`):

| Control | Stored value, format | Word 365 shows | `w:fullDate` written | Reading |
|---|---|---|---|---|
| TW1 | `2026-10-22T01:05:00Z`, `h:mm AM/PM` | `12:05 PM` | `2026-10-22T12:05:00Z` | `h` is 1-12 (Java `K` would give 0:05); the `Z` read as UTC, shown at UTC+11 |
| TW2 | `...T13:05:00Z`, `hh:mm am/pm` | `12:05 AM` | `2026-10-23T00:05:00Z` | midnight is 12, the next day |
| TW3 | `...T12:05:00+11:00`, `h:mm AM/PM d MMM` | `12:05 PM 22 Oct` | `2026-10-22T12:05:00Z` | an explicit offset is honoured |
| TW4 | `...T12:05:00` (no offset) | `12:05 PM 22 Oct` | `2026-10-22T12:05:00Z` | no offset: local time, as written |
| TW5 | `...T12:05:00-05:00` | `4:05 AM 23 Oct` | `2026-10-23T04:05:00Z` | a western offset honoured: the next day locally |
| TW6 | `...T01:05:00Z`, `HH:mm` | `12:05` | | as TW1 |

So Word's rule for a stored `dateTime`: an offset or `Z` is an instant, shown in the machine's zone; no offset is
local time; `w:fullDate` carries the local time labelled `Z`. docx4j's `parseStoredDate(String, ZoneId)` does the
same with the zone property set (`OffsetDateTime.parse` for a value with an offset or `Z`, the nominal fields
otherwise), and the nominal fields by default (D6); `BoundDateFormatTest.twelveOClockAndOffsetsAsWordShowsThem`
holds both readings to this table, with the probe as the fixture `typed-controls-twelve.docx`.

Word also rewrote each `itemProps` with a `ds:schemaRef` for the part's namespace (cosmetic). Findings 5-7, from
the run:

5. **Time zone.** Word reads the stored `dateTime`'s `Z` (or offset, second probe) as UTC and formats in the
   machine's zone; docx4j reads the nominal time. D6 (§5a): nominal by default, a zone property converts.
6. **No `w:lid`.** Word uses its editing language, not the run's. D2 revised (§5a); `BoundDateFormatTest` DF7
   now expects the JVM default locale's names (the document has no `w:docDefaults` language), not French.
7. **Empty value.** The placeholder, as for an empty text binding; `BoundDateFormatTest` DF10 now expects
   `w:showingPlcHdr` and no red, not an empty run.

D1, D3 (for a non-date text) and D4 (all three cases) are confirmed as accepted; the `StoreItemRepairTest`
expectations stand.

## 9. Phase 1 record (2026-10-23, Claude Fable 5.1, session docx4j-25)

Jason: "do CR-058 phase 1". Built and tested; commit on Jason's word.

- `FormattingSwitchHelper`: `h`→`h` and `hh`→`hh` in `DATE_FORMAT_ITEMS_TO_JAVA` (were `K`/`KK`, 0-11; finding 2
  of §8), for the DATE field and the control alike; the AM/PM names upper-cased in the locale (`wordAmPm`; en-AU's
  CLDR data has "am"/"pm", Word 365 showed "2:05 AM"); a public `formatDate(String wordFormat, Date, String lang)`
  (`@since 17.3.5`) for a caller with no field model.
- `BindingTraverserXSLT.xpathDateRun(parts, sdtPr, pkg)` (the two-argument form delegates with a null package):
  the value formatted by `FormattingSwitchHelper.formatDate` in `dateLanguage` (`w:lid`, else
  `documentDefaultLanguage`, `w:docDefaults/w:rPrDefault/w:rPr/w:lang/@w:val`, else null = the platform's; D2
  revised); a value that is not a date (`DateTimeParseException`) is the run's text as stored, WARN, no colour
  (D3); an empty value returns a run with no content, which `xpathDate` (XSLT) turns into the placeholder through
  `createPlaceholder` and `applyDateBinding` (NonXSLT) through the same plus `postBindSdtPrHygiene`, so
  `w:showingPlcHdr` is set (D3 refined); `parseStoredDate(String, ZoneId)` converts a value with an offset or `Z`
  to the zone `docx4j.model.datastorage.BindingHandler.dateTimeZone` names, else the nominal fields as before
  (D6; `DATE_TIME_ZONE_PROPERTY`). `bind.xslt`'s `w:date` branch generates the content first and sends the
  `w:sdtPr` through the `w:sdtPr` template with it (wrapped in `w:sdtPr`, as the text branch does), so the
  placeholder case gets `w:showingPlcHdr` and `w:placeholder` is stripped as for a text binding.
- `BindingTraverserCommonImpl.applyDateBinding` passes the package and handles the empty run.
- Tests: `BoundDateFormatTest` un-ignored (both traversers, DF1-DF13 as §8's table, DF7 in the platform's locale
  since the fixture has no `w:docDefaults`), plus `withoutALidTheDocumentDefaultLanguageIsUsed` (a German
  `w:docDefaults` added in the test: DF7 "22 Oktober 2026", DF5's own `w:lid` still wins) and
  `theDateTimeZonePropertyConvertsAsWordDoes` (`Australia/Sydney`: DF2 "2:05 AM", DF3 "11:05 PM", DF4 "02:05",
  DF1 and the date-only DF11 unshifted, Word's measured values); `DateFieldLanguageTest.twelveHourClockRunsOneToTwelve`
  (noon "12:05 PM", five past midnight "12:05 AM", `hh`, `HH`, `ddd`). Runs: the datastorage, fields, samples and
  binding suites, 232 tests, 0 failures; the wider text, revisions, output and openpackaging suites, 262 tests,
  one failure unrelated to this phase (`ViewAgreementTest` on the check 57 equation fixture of ebb21c990: the
  text model's LaTeX reading of a deleted math run, fixed separately).
- Reference `docx4j.properties` documents `dateTimeZone`; CHANGELOG 17.3.5.
- Not changed: the text model (`BoundValues`) still reads a date control's surface (the paper's D34 is its own
  item); `w:lid` is not written back where Word's save adds one.

## 10. Phase 2 record (2026-10-23, Claude Fable 5.1, session docx4j-25)

Jason: "start CR-058 phase 2". Built and tested; commit on Jason's word.

- `org.docx4j.model.datastorage.StoreItemResolver` (new, `@since 17.3.5`): `resolve(parts, storeItemId, xpath,
  prefixMappings[, strict])` gives the part the ID names, else the custom XML part the XPath fits (D4: the
  `CustomXmlDataStoragePart`s whose root element's namespace and local name match the XPath's first step read with
  the binding's prefix mappings, docx4j's table as the fallback a lookup would use; an XPath with no named first
  step fits every part); one fitting part is taken, INFO; of several, in part-name order, the first whose XPath
  selects a node, else the first, WARN; none, null and WARN (the generator leaves the control as it is, the text
  model reads its surface). Resolved once per document and XPath (a `WeakHashMap` on the parts map; one stale ID
  fits different parts for different XPaths, as the two-parts probe showed: B7 part 1, B11 part 2).
  `resolvedStoreItemId(...)` gives the part's own ID for the write-back; `resolvable(...)` is the guard.
  `STRICT_PROPERTY` (`docx4j.model.datastorage.BindingHandler.strictStoreItemIds`, default false): a stale ID
  throws `InputIntegrityException` before any repair, whether or not a part fits; inside Xalan an extension
  function's exception is logged by the error listener and swallowed, so the resolver also records it in a
  thread-local that `BindingTraverserXSLT` rethrows after its transform, which makes the failure the same on both
  traversers.
- Lookups routed through it: `BindingHandler.xpathGetString` (text; null, not an exception, where none fits),
  `BindingTraverserXSLT`'s `xpathDateRun`, two picture methods, the two checkbox methods and the flat-OPC/XHTML
  one, `BindingTraverserNonXSLT`'s picture, `BindingTraverserStAX`'s, `BindingTraverserCommonImpl.replaceBlipEmbed`,
  `UpdateXmlFromDocumentSurface.getCustomXmlPart` (the surface-to-XML direction writes to the part Word would), and
  `model/text/BoundValues.document` (strict false; its document cache keyed by the part resolved).
- Left as it is (D4, none fits): `bind.xslt`'s `w:sdt` template gains a first branch, for `w:dataBinding` and
  `w15:dataBinding`, that copies the control whole when `resolvable` is false; `BindingTraverserNonXSLT`'s dispatch
  does the same. Without the guard the XSLT emptied the control and the NonXSLT wrote `[missing!]`.
- Write-back (D5): `bind.xslt` template `w:dataBinding/@w:storeItemID | w15:dataBinding/@w:storeItemID` writes
  `resolvedStoreItemId`, reached where the `w:sdtPr` goes through its template (the text and date branches; the
  branches that `copy-of` the `w:sdtPr`, picture and checkbox, keep the old ID). `BindingTraverserCommonImpl.
  writeBackStoreItemId(pkg, sdtPr)` (text, date, picture, checkbox paths). In memory only; saving is the caller's.
- Tests: `StoreItemRepairTest` un-ignored, 6 green: bind-plain B7 bound and its ID rewritten on both traversers,
  the text model's value, the strict failure on both traversers, two-parts B7 → part 1 and B11 → part 2 with the
  IDs, no-part B7 and B12 left with their surfaces and stale IDs while B1 binds. Runs: datastorage, text, text
  extraction, samples, binding, OpenDoPE suites, 372 tests, 0 failures; the whole docx4j-core-tests module, 2026
  tests, 0 failures, 30 skipped.
- Reference `docx4j.properties` documents `strictStoreItemIds`; CHANGELOG 17.3.5.
- Measured after the phase (the second probe, §8): `h` at 12 local and explicit offsets, both as implemented. The OpenDoPE
  `od:xpath` branch of `bind.xslt` has no "left as it is" guard (its bindings come from the xpaths part, which the
  generator itself wrote; a stale ID there resolves through `xpathGetString` the same way, and none fitting
  empties the control as before). The paper's D34 (the text model showing a formatted date) is its own item.

## Amendment (2026-10-28): D6's conversion is its own opt-in, in the package's zone

The portfolio's date review (Jason's ruling 2026-10-28; docx4j CR-067 D12 and §12): the conversion D6 tied to
`docx4j.model.datastorage.BindingHandler.dateTimeZone` being set is now a separate opt-in named for what it does,
`OpcPackage.setBoundDatesConvertedAsWord(true)` or the property
`docx4j.model.datastorage.BindingHandler.boundDatesConvertedAsWord` (default false), and it converts in the package's
zone (`OpcPackage.effectiveDateTimeZone()`: the package's own, else the zone property, else the JVM default). Setting
a zone alone, which now also governs date fields and revision dates, does not turn Word's day-early reading on. The
nominal default is unchanged. `BoundDateFormatTest` amended.

