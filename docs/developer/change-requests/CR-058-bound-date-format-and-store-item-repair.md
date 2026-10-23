# CR-058: A bound date formatted as Word formats it, and a store item ID that names no part resolved as Word repairs it

Status: PROPOSED 2026-10-23, drafted by Claude Fable 5.1 (session docx4j-25) at Jason's request ("do a cr for the
remaining binding items the portfolio flagged"), from the two items docx4j-portfolio's text-extraction paper (§12a,
22 October 2026) lists as still open against the binding generator after CR-054 and the fixes of 17.3.4
(f62314d3b, 24642aa59). Both confirmed in this repository's source at 64ebb9ae9 (§1). Decisions in §5 await Jason;
phases in §4.

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
2. **A `w:storeItemID` that names no part aborts the bind.** `BindingHandler.xpathGetString` throws
   `InputIntegrityException("Couldn't locate part by storeItemId ...")`, so one stale ID in one control fails the
   whole document. Word repairs it: measured on the portfolio's `bind-plain.docx` (B7, store item ID
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
  InputIntegrityException(...)`. `xpathDateRun` (1983) logs an error and returns null for the same case, which the
  traversers turn into `[missing!]`. `model/text/BoundValues.document()` (85-110) returns null; `value()` null;
  the surface is shown.

Measured by docx4j-portfolio (Word 365, Jason, 22 October 2026; `~/vbShares/Office 2016/bind-fun/`):

| Probe | Word | docx4j today |
|---|---|---|
| `bind-plain.docx` B7: `w:storeItemID="{...00FF}"`, one part `{...0001}`, XPath `/p:root[1]/p:b7[1]` | shows `B7_XML_VALUE`; the save (`bind-plain-word365.docx`) has `w:storeItemID="{...0001}"` on B7 | `InputIntegrityException`, bind aborted (generator); surface `B7_STALE_SURFACE` (text model) |
| `bind-typed.docx` DT1: `d MMMM yyyy`, `en-AU`, value `2026-10-22T00:00:00Z` | "22 October 2026" | the same on an English JVM (TypedControlBindingTest); "22 Oktober 2026" on a German one |
| LibreOffice 26.2.5, ONLYOFFICE on DT1 | the same formatted date | |

Not measured yet (phase 0 probes, §4): Word's output for `ddd`, `am/pm`, `h:mm` and a `de-DE` `w:lid` on an
English machine; what Word shows for a stored value that is not a date (`not-a-date`, `2026-13-45`, empty); which
part Word picks for a store item ID naming none when two parts fit; what it does when none fits.

## 2. What it affects, and what it does not

Affected (1): every date control bound through either traverser (`Docx4J.bind`, OpenDoPE processing, the
`w:date` branch of `bind.xslt`). A format with `ddd`, `am/pm`, `h:mm am/pm` or `tt` today gives wrong text or an
exception the traverser turns into `[missing!]`; a format with month or weekday names gives them in the server's
language. The text model is not affected: it reads the stored surface for a date control (REQ-509), and the paper's
D34 proposes showing the formatted date there too, for which this CR's formatter is the shared piece.

Affected (2): every binding whose store item ID names no part: the generator aborts (an `InputIntegrityException`
out of `Docx4J.bind`), the text model shows the surface. Common in practice: a template whose custom XML part was
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
| 0 | Red tests for both (core-tests): a `TypedControlBindingTest`-style fixture per format (`ddd`, `am/pm`, `de-DE`, a non-date value); `bind-plain.docx` as the B7 fixture, bound through both traversers and read by the text model. Probes for Jason's Word: the §1 "not measured" list, built from `bind-typed.docx` (one control per format, a non-date value) and `bind-plain.docx` (a second custom XML part that also fits; none that fits) | 2 h |
| 1 | Date (D1-D3): `xpathDateRun` formats through `FormattingSwitchHelper` with `w:lid`; the non-date case per D3 and the measurement; tests green; CHANGELOG | 2 h |
| 2 | Store item (D4-D5): the resolver, shared by `BindingHandler.xpathGetString`, `xpathDateRun` (and the picture, XHTML and checkbox paths that look the part up themselves) and `BoundValues.document`; the write-back; tests green; CHANGELOG; the ports' hand-off (their binders: date language, missing-part repair) and the portfolio's §12a.3 | ½ day |

## 5. Decisions

| # | Decision | Recommendation |
|---|---|---|
| D1 | The date formatter | reuse `FormattingSwitchHelper.convertDatePattern` + `formatDate(..., lang)` (Word's picture language, already measured for fields), not a second token table in `xpathDateRun`; the one place both fields and controls format a Word date |
| D2 | The language | `w:lid` where present; else the control's `w:rPr/w:lang` (`w:val`) as the field path uses the run's; else the JVM default, as today. `Locale.forLanguageTag` |
| D3 | A stored value that is not a date | show the stored text as it is, in the control's formatting, with a WARN (Word's behaviour, to be confirmed by the phase 0 probe); today's date in red is a server's date leaking into a document and goes. If Word shows something else, follow Word |
| D4 | A store item ID naming no part | resolve to the custom XML part the XPath fits: the parts (not the property parts) whose root element's namespace and local name match the XPath's first step read with the binding's prefix mappings; exactly one → use it, INFO once per document; several → the first (part-name order) whose XPath yields a node, else the first that fits, WARN (which one Word picks is a phase 0 probe); none → the control is left as it is (its stored content stays, like Word's surface for an unresolved binding), WARN, no exception. The `InputIntegrityException` becomes opt-in: property `docx4j.model.datastorage.BindingHandler.strictStoreItemIds=true` restores it for a deployment that wants a bad template to fail |
| D5 | Write-back | the generator writes the resolved ID into the control's `w:dataBinding/@w:storeItemID` as Word's save does, through `postBindSdtPrHygiene` (NonXSLT) and the sdtPr template (XSLT), so the saved document binds without the repair next time. The text model writes nothing |

## 6. Risks

- **D1 output changes** for a template whose format Java read differently by luck: `ddd` ("022") becomes "Thu",
  `MMMM` on a non-English server becomes the control's language. That is the fix; CHANGELOG names it.
- **D3** changes a visible marker (red today's date) to plain text; a deployment that relied on the red date to
  spot bad data loses it. The WARN names the control and value. Judged acceptable, since the red date is wrong
  data in a document.
- **D4** removes an exception some callers may catch to reject a template. The property restores it.
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
