# CR-065: integer attributes read as Word reads them; a part that cannot be read is never replaced by an empty one

Status: ACCEPTED 2026-10-26 (Jason: "CR-065: D1 to D6 as recommended, do phase 1"): D1 to D6 as recommended in §4; phase 1
DONE 2026-10-26 (9ea65238a, §7); phase 2 DONE 2026-10-26 (§8); phase 3 DONE; the 16-bit range report (§3's aside) is CR-066's, whose phase 0 is round 5 of
this probe: phase 2 ships first and gains the report when round 5 answers (Jason, 2026-10-26). PROPOSED 2026-10-26, opened by Claude Fable 5.1 (session docx4j-25) at
Jason's word ("CR-065: open it, build the probe, and put it on the share with its README"), after docx4j-python-cd found the defect through JPype and
docx4j-generated-objects-ts-82 reproduced it (both 2026-10-26; portfolio relay). Reproduced here in Java (§1). Phase 0,
the shared Word probe, DONE 2026-10-26 (§3, four rounds, Jason): Word truncates a decimal toward zero, reads the empty
string as 0, and refuses the document for any other form (whitespace included). Recommendations in §4; phases in §5.

## 0. Summary

A decimal in an integer attribute of WordprocessingML (`w:spacing w:before="120.5"`, `w:ind w:left="720.5"`, a border's
`w:sz="7.5"`, `w:tab w:pos="1440.5"`, `w:tcW w:w="50.5" w:type="pct"`, `w:pgMar w:top="1440.5"`) fails the part's
unmarshal unless the attribute is on `mc-preprocessor.xslt`'s fixed rounding list. The document loads without error,
because unmarshalling is lazy; the first read of the main part fails and returns null; the first edit through
`MainDocumentPart.getContent()` then builds a fresh empty document, and the save holds the new content alone. The
original content is lost, with one log line to say so.

Two defects, fixed separately: **the data loss** (phase 1: a part that could not be read is never replaced by an empty
one), which is independent of what made the read fail; and **the coverage** (phase 2: every integer attribute read
leniently, by Word's rule, in the JAXB binding rather than by a list in a stylesheet). Word's rule is unmeasured, so
phase 0 is one Word probe shared by the three ports (§3).

## 1. The chain (Java, measured 2026-10-26, 17.3.5-SNAPSHOT)

Fixtures: a docx4j-made document whose `document.xml` carries one attribute changed (scratchpad `decimal/Decimal.java`).

| Case | first unmarshal | retry | `getJaxbElement()` | save untouched | save after `addParagraphOfText` |
|---|---|---|---|---|---|
| integers | ok | - | Document | original | original + new paragraph |
| `w:spacing/@w:after="120.5"` (on the list) | NumberFormatException | rounds to 120 | Document | original | original (after=120) + new paragraph |
| `w:spacing/@w:before="120.5"` (not on the list) | NumberFormatException | NumberFormatException | **null** | original bytes | **the new paragraph alone** |
| `w:ind/@w:left="720.5"` | same | same | **null** | original bytes | **the new paragraph alone** |
| `w:spacing/@w:before="abc"` | same | same | **null** | original bytes | **the new paragraph alone** |

1. JAXB RI binds `ST_TwipsMeasure`, `ST_SignedTwipsMeasure`, `ST_EighthPointMeasure`, `ST_HpsMeasure`,
   `ST_DecimalNumber` and the like to `BigInteger`; `"120.5"` throws a raw `NumberFormatException` out of the
   unmarshaller. It is not a `ValidationEvent`: `JaxbValidationEventHandler` never sees it, cannot continue past it,
   and could not substitute a value if it did.
2. `JaxbXmlPartXPathAware.unmarshal` catches it, logs "encountered unexpected content; pre-processing", and retries
   through `mc-preprocessor.xslt`. Its rounding is a fixed list: `@w:w` under `type=dxa` or `pgSz`, `gridCol`,
   `tblW`, `tblInd`; `pgSz/@w:h`; `spacing/@w:line` and `@w:after`; `ind/@w:hanging` and `@w:firstLine`;
   `trHeight/@w:val`; a border's `@w:space` on `top`, `left`, `bottom`, `right` (not `between`). The rounding is
   `format-number(., '#')`, Xalan's half-even. Anything else throws the same exception on the retry, wrapped as
   `JAXBException` then `Docx4JException`.
3. `JaxbXmlPart.getJaxbElement()` catches that, logs ERROR and returns null (`getContents()`, the newer API,
   throws). Each later call runs the unmarshal again and fails again.
4. `MainDocumentPart.getContent()` reads null as "no content yet" and creates a fresh `Document` and `Body`;
   `addParagraphOfText` lands there; save marshals it. The same create-when-null convenience is in
   `HeaderPart.getContent`, `FooterPart.getContent`, `DocumentSettingsPart` (two sites) and
   `DocPropsExtendedPart.setDocSecurity`: each replaces a part whose unmarshal failed with an empty one on first
   access. (`StyleDefinitionsPart` was listed here at first; its null test throws rather than creating, so it is not a
   site. Portfolio's reading, 2026-10-26.)

The ports' reports agree: python-cd on 17.3.4 and a 17.3.5-SNAPSHOT bundle through JPype (its probe files in its
scratchpad `decimals/`); objects-ts on `MainDocumentPart.unmarshal` at 2f27dca82, adding `w:pgMar/@w:top`, a
`w:between` border's `@w:space`, a part with one listed and one unlisted decimal (fails), and that SSRS's empty rsids
are dropped only because `header=""` made the stylesheet run (a lone `<w:p w:rsidR="">` keeps `""`).

## 2. What users and producers write

Real producers: Google Docs (`w:tblW w:w="10206.0"`, 2014; `w:pgSz w:h="16839.0" w:w="11907.0"`, 2015), pandoc 2.2.1
(`tblW`, #298), a float artefact `w:hanging="141.99999999999994"` (2017), `w:firstLine` decimals (2025), SSRS 2012's
empty `header`, `footer`, `gutter` and rsids, `w:spacing w:line="259.2"`, border `w:space="1.8"`. Each was found by a
user's failed load and added to the list; the list is where the next one is missing from.

## 3. Phase 0: the Word probe (built 2026-10-26; Jason's run awaited)

One document, 65 labelled cases, assembled from the three sessions' lists (python-cd's families and value forms,
objects-ts's 44 cases, the Java reproduction), on the share at `__tmp/cr065/`: `cr065-decimals-probe.docx`, `cases.txt`
(id, attribute, value), `README.txt` (Jason's steps), and `families/cr065-A..I.docx` for the case where Word refuses the
whole file. Generator: scratchpad `probe65/Probe65.java` (a docx4j-made template whose `document.xml`, `styles.xml`,
`settings.xml` and header are rewritten with the values).

| Family | Attribute | Values |
|---|---|---|
| A | `w:spacing/@w:before` (twips; not on the list) | 120.5, 120.4, 121.5, 122.5, 240.0, 141.99999999999994, "240.", ".5", "+240", "+240.5", " 240.5 ", "240,5", "0x10", "1.5e2", "2.5e0", "1e3", "", "abc", " 240 " (a spaced integer, to tell the spaces from the decimal in " 240.5 ") |
| B | `w:ind/@w:left` (signed twips; not on the list) | 720.5, 720.4, 721.5, -120.5, -121.5, -0.5, -1.5, 10206.0 |
| C | on the list: `@w:after`, `@w:line` (lineRule auto), `@w:hanging`, `@w:firstLine` | 141.5, 120.5; 259.2, 240.5, 241.5; 141.99999999999994; 12.5 |
| D | borders: `w:top/@w:sz`; `@w:space` on top, bottom (listed) and between (not) | 7.5; 1.8, 7.2; 1.8 |
| E | `w:tab/@w:pos` | 1440.5, 720.5 |
| F | run: `w:sz`, `w:w` (text scale), `w:spacing`, `w:position` | 22.5, 90.5, 20.5, 6.5 |
| G | tables: `tblW` dxa (listed), `tblInd` dxa (listed), `gridCol` (listed), `trHeight` (listed), `tcW` pct and `tblW` pct (not) | 9000.5, 9001.5, 10206.0, 100.5; 10.5; 4500.5 + 5705.5, 1234.5; 400.7, 300.4; 50.5; 50.5 |
| H | sections (each continuous): `pgSz` (listed), `pgMar/@w:top` (not), SSRS's empties, a lone `pgMar/@w:top=""` | 11907.0 x 16839.0, 11907.5 x 16838.5; 1133.5, 1440.5; rsid*="" + header/footer/gutter=""; "" |
| I | other parts: a style's `w:ind/@w:left`; `settings` `w:defaultTabStop`; the header's `w:spacing/@w:before` | 720.5; 720.5; 120.5 |
| Z | control | `w:spacing/@w:before="120"` |

What Word answers: open or repair, per file; Word's save (its reading of every value at once, or the drop); the
Paragraph dialog on six halves (A01, A03, A04, B20, B23, B24), which tells half-even from half-up from half-away-from-
zero from truncation and covers the case where Word keeps the text as written. If Word keeps decimals verbatim, the
second oracle is core-ts reading `spaceBefore` and `leftIndent` through Office JS (objects-ts's suggestion).

**Round 1 (Jason, 2026-10-26): Word refused the all-cases file outright**, "Word experienced an error trying to open the
file", with no repair offered. So at least one of the 65 values is one Word will not even repair, and the per-family
files are round 2 (which families open, repair, refuse; Word's save of each that opens; the Paragraph dialog on A and
B). A structural control was added first, `cr065-structure-control.docx`: the same document with every tested value a
plain integer (docx4j loads all four parts of it), to show the refusal was the values and not the file's shape.
Jason, 2026-10-26: the control "opens fine". So the refusal was the values.

**Round 2 (Jason, 2026-10-26): families B to I open clean; A is refused.** Read from Word's saves
(`families/cr065-{B..I}-word365.docx`, scratchpad `probe65/readsave.py`):

*Word truncates a decimal toward zero; it does not round.* Every decimal in B to I became its integer part:
720.5 and 720.4 to 720, 721.5 to 721, -120.5 to -120, -121.5 to -121, -1.5 to -1 (B); 141.5 to 141, 120.5 to 120,
259.2 to 259, 240.5 to 240, 241.5 to 241, 141.99999999999994 to **141**, 12.5 to 12 (C); border 7.5 to 7, space 1.8
to 1, 7.2 to 7, between 1.8 to 1 (D); tab 1440.5 to 1440, 720.5 to 720 (E); text scale 90.5 to 90, character spacing
20.5 to 20, position 6.5 to 6 (F); tblW 9000.5 to 9000, 9001.5 to **9001**, 10206.0 to 10206, 100.5 to 100, tblInd
10.5 to 10, trHeight 400.7 to 400, 300.4 to 300, tcW pct 50.5 to 50, tblW pct 50.5 to 50 (G); pgSz 11907.0 x 16839.0
to 11907 x 16839, 11907.5 x 16838.5 to 11907 x 16838, pgMar top 1133.5 to 1133, 1440.5 to 1440 (H); the style's
indent 720.5 to 720, `w:defaultTabStop` 720.5 to 720, the header's 120.5 to 120 (I). So docx4j's stylesheet, whose
`format-number` is half-even, disagrees with Word on seven of the probe's values on its own list (objects-ts's count,
verified): 141.5, 241.5, 9001.5 and 11907.5 (halves rounding up), 1.8, 400.7 (rounding up) and 141.99999999999994
(docx4j 142, Word 141): the repairs docx4j has made since 2014 give a different integer from Word's in those cases.
B27 (`w:ind/@w:left="10206.0"`) became 10206 (objects-ts's reading; Word split the label across runs). Two values vanished for a reason other than parsing: `w:sz w:val="22.5"` (F41) became 22, the
document default, and `w:ind w:left="-0.5"` (B25) became 0, the default, and Word writes neither (it stores
direct formatting as a difference from the style); the Paragraph dialog is not needed, the saves answer.

*The empty string reads as 0, and empty rsids are dropped:* SSRS's `header="" footer="" gutter=""` became `0 0 0`
and the four `rsid*=""` were dropped (H60); a lone `pgMar/@w:top=""` became 0 (H61). *The grid is recomputed:*
`gridCol` 4500.5 and 5705.5 became 4500 and 4500 under a `tblW` auto table of two 4500 cells (G50, G51), so a
decimal there has no observable reading beyond "not refused".

*An aside, from a generator slip:* families B to H carried, in `styles.xml`, `settings.xml` and the header, the
generator's sentinel integers 7205001, 7205002 and 1205003 (meant to be replaced; family I and the all-cases file had
the decimals). Word kept them modulo 2^16: the style's `w:ind/@w:left` became -3959 (signed), `w:defaultTabStop`
61578 and the header's `w:spacing/@w:before` 25355 (unsigned); the header's 25355 twips of space before is what made
those files "100's of pages" (Jason). So Word's twips measures are 16-bit internally, and an out-of-range value wraps
rather than being refused or clamped. Fixed in the generator (plain integers where not under test); the body cases
were unaffected. Taken up by CR-066 (the write side and the range per type; round 5 of this probe is its phase 0).

*Family A* (`w:spacing/@w:before` with the odd forms) was refused whole. Round 3 splits it: A1 the pure decimals
(120.5, 120.4, 121.5, 122.5, 240.0, 141.99999999999994), A2 the forms ("240.", ".5", "+240", "+240.5", " 240.5 ",
" 240 "), A3 the comma decimal, A4 hex, A5 the exponents, A6 the empty string, A7 "abc" (`families/cr065-A1..A7-*.docx`).
Expected from B to I: A1 opens (truncation); the rest is the question.

**Round 3 (Jason, 2026-10-26): A1 and A6 open clean; A2, A3, A4, A5 and A7 are refused** ("Word experienced an
error trying to open the file", no repair). A1's save truncates as B to I did (120.5 and 120.4 to 120, 121.5 to 121,
122.5 to 122, 240.0 to 240, 141.99999999999994 to 141). A6's `w:spacing/@w:before=""` read as 0, the default, so
Word wrote no `w:spacing` at all. So **Word's lexical form for an integer attribute is an optional minus, digits, and
an optional fraction after a dot, or nothing at all**; the fraction is dropped. Everything else refuses the whole
document, not the attribute: a comma decimal ("240,5"), hex ("0x10"), the exponent forms ("1.5e2", "2.5e0",
"1e3"), letters ("abc"), and at least one of A2's six forms ("240.", ".5", "+240", "+240.5", " 240.5 ",
" 240 "), which were refused as a group; which of the six Word accepts, if any, is round 4 (singles, optional).

**Round 4 (Jason, 2026-10-26): A2a "240.", A2b ".5", A2c "+240" and A2d "+240.5" open clean; A2e " 240.5 " and
A2f " 240 " are refused.** The saves: "240." to 240, ".5" to 0 (the default; no `w:spacing` written), "+240" to 240,
"+240.5" to 240. So **Word's lexical form is an optional sign (+ or -), digits, optionally a dot and digits, with
either digit group allowed to be empty, or the empty string; no surrounding whitespace.** The fraction is dropped
(truncation toward zero). Whitespace, a comma, hex, an exponent or letters refuse the whole document. XML schema's
whitespace collapse does not apply in Word's reading of these attributes (unlike the on/off reading CR-018 found,
where " false" was read). **Phase 0 is complete**: four rounds, 65 cases, every value's reading or refusal known.

**core-ts today on the probe files** (core-ts, 2026-10-26, 0.6.0): every decimal fails its part's unmarshal, with an
error naming the part, attribute, value, line and column; `getBody()` throws; a part that failed is never marshalled,
so a save copies its source bytes. No counterpart of docx4j's empty main part. core-ts parses no integer itself (jsonix
does, through the objects facade), so Word's rule reaches it through a jsonix CR and an objects CR.

**docx4j today on the probe files** (`probe65/Today.java`): the all-cases file and families A, B, D, E, F, G, H fail
the main part; C and I load (C's listed values round half-even: 141.5 to 142, 120.5 to 120, 240.5 to 240, 241.5 to
242, 141.99999999999994 to 142, 12.5 to 12); the all-cases file and I fail `styles.xml`, `settings.xml` and the header
too.

## 4. Decisions

| # | Question | Options | Recommendation |
|---|---|---|---|
| D1 | A part whose unmarshal failed, on the create-when-null conveniences | (a) throw, with the cause ("the part's content could not be read; refusing to replace it"), distinguishing "never had content" (a created package: no source part store) from "content that could not be read" (the part records the failed unmarshal); (b) today | (a). `getJaxbElement()` keeps returning null for compatibility; the six sites call `getContents()` and let its exception out, or test the recorded failure. Unchecked (`Docx4JRuntimeException`), since the conveniences declare nothing. |
| D2 | Where the lenient integer read lives | (a) an XJC `javaType` customisation in the xjb binding the integer simple types to `BigInteger` through a parse method (`org.docx4j.jaxb.LenientInteger.parse`), so the first unmarshal succeeds for every integer attribute in every part; (b) a general stylesheet template over the (element, attribute) pairs derived from `wml.xsd`, on the retry only; (c) the event handler | (a). (c) is impossible (§1 step 1). (b) needs the pair list because `w:val` is polymorphic (`w:tag w:val="1.5"` is a legal string) and runs only on the retry. (a) covers the universal measures too ("12pt", which the stylesheet converts for a few attributes only). The stylesheet's list stays and retires later. The regeneration follows `adding-a-schema.md`'s steps for an xjb change. |
| D3 | The rounding rule and the odd forms ("", "abc", "1e3", "0x10", " 240.5 ", "240,5", a negative where unsigned) | Word's, from the probe | **Measured (rounds 2 and 3): a decimal is truncated toward zero (its integer part); the empty string reads as 0; every other form (comma, hex, exponent, letters, and at least some of sign, trailing dot, leading dot, surrounding spaces) makes Word refuse the whole document.** Recommended for phase 2: read what Word reads as Word reads it; for a form Word refuses, drop the attribute and log at WARN (the document still opens in docx4j, which is more than Word gives; a dropped attribute is a smaller loss than a failed part, and phase 1 makes a failed part loud). Round 4 overruled trimming: Word refuses surrounding whitespace, so a spaced value is a refused form (dropped, WARN), and the lexical test is Word's exactly: `[+-]?\d*(\.\d*)?` non-empty, or the empty string. Alternative (b): trim first, then read; more lenient than Word for a form whose intent is plain. Recommended (a), so the three ports read identically and the rule has one source. If Word repairs a form rather than reading it, docx4j drops that attribute and logs; if Word refuses the file, docx4j may still read it (more lenient than Word is a choice, recorded). |
| D4 | Which simple types get the lenient parse | `ST_DecimalNumber`, `ST_UnsignedDecimalNumber`, `ST_TwipsMeasure`, `ST_SignedTwipsMeasure`, `ST_HpsMeasure`, `ST_SignedHpsMeasure`, `ST_EighthPointMeasure`, `ST_PointMeasure`, `ST_TextScale`, `ST_PixelsMeasure`; the full list from `wml.xsd` at phase 2 | all of them; one method. |
| D5 | The empty string (`header=""`, `rsidR=""`) | (a) as Word reads it (probe); (b) today's stylesheet rule (0 for header/footer, dropped for gutter and rsids) | phase 0 decides; rsids are strings and are a separate question (kept as `""` today). |
| D6 | Report the repairs | (a) each lenient read logs at INFO with the attribute, the text and the value taken, once per document; (b) silent | (a); the portfolio survey's skipped-content report (`broken_producer_repairs_across_ports.md` §4 item 4) would carry the same. |

## 5. Phases

| Phase | Work | Status |
|---|---|---|
| 0 | The shared Word probe (§3); the rule recorded in §3 and sent to python-cd, objects-ts and core-ts | DONE 2026-10-26 (four rounds, Jason) |
| 1 | D1: the data-loss fix in the six conveniences, with a test per part type (a decimal that fails today; the edit throws; the save keeps the bytes) | DONE 2026-10-26 (§7) |
| 2 | D2, D4, D6: `LenientInteger`, the xjb customisation, regeneration, the three JAXB contexts unchanged; tests for each value form with Word's rule; the stylesheet list left in place; CHANGELOG. Ships without a range check; CR-066 phase 1 adds the 16-bit report to the same print and parse methods | DONE 2026-10-26 (§8) |
| 3 | Hand-offs: the measured rule to the three ports (objects CR-015, python's integer reader, core-ts) | DONE 2026-10-26 (sent round by round during phase 0; objects CR-015 and python took truncation the same day) |

## 6. Risks

- **Phase 2 changes every load.** A lenient parse runs for every integer attribute of every document; the parse is a
  string test and a `BigInteger` construction, as today's, plus a fallback path taken only by bad values. Measured
  before commit, as CR-064 §3 was.
- **MOXy.** The xjb customisation is XJC's and applies to the generated classes, so both runtimes see it; tested on
  both.
- **Rounding differently from Word** is the risk phase 0 removes.

## 7. Phase 1 done (2026-10-26)

Jason, 2026-10-26: "CR-065: D1 to D6 as recommended, do phase 1". `JaxbXmlPart` records the failed lazy unmarshal
(`unreadable`): `getContents()` throws it again on each later read without a second attempt, `getJaxbElement()` logs it
once and returns null as before, `setJaxbElement` clears it (an explicit replacement is the caller's decision), and
`isContentUnreadable()` tells "could not be read" from "no content yet". A protected `contentsOrNew(Supplier)` serves
the conveniences: the content, or a new one for a part that never had any, or `Docx4JRuntimeException` carrying the
read failure ("The content of /word/document.xml could not be read, so it is not replaced with an empty one; fix or
replace the content first (setJaxbElement). Cause: ..."). The six sites use it: `MainDocumentPart.getContent` (so
`addObject` and `addParagraphOfText` too), `HeaderPart.getContent`, `FooterPart.getContent`,
`DocumentSettingsPart` (`safeGetDocumentProtection`, `attachTemplate`), `DocPropsExtendedPart.setDocSecurity`. An
untouched save still writes the source bytes. `UnreadablePartTest` (core-tests, `org.docx4j.openpackaging.parts`):
the main part, a header and settings each made unreadable with `"abc"` in an integer attribute (a value no rule reads,
so the fixtures outlive phase 2): the read returns null once and again without a retry, each edit is refused with the
cause, the save keeps the bytes; a replacement by `setJaxbElement` is accepted and saved; a created package's parts
still get new content from the conveniences.

## 8. Phase 2 done (2026-10-26)

Jason, 2026-10-26: "do CR-065 phase 2". **The binding.** `org.docx4j.wml.LenientInteger` (hand-written, in
docx4j-generated-objects beside `OnOffAdapter`) is the `parseMethod` and `printMethod` that `xsd/bindings.xjb` now
binds, by schema component designator, to `ST_DecimalNumber`, `ST_UnsignedDecimalNumber`, `ST_TwipsMeasure`,
`ST_SignedTwipsMeasure`, `ST_SignedHpsMeasure`, `ST_EighthPointMeasure`, `ST_PointMeasure`, `ST_PixelsMeasure`, to
`CT_HpsMeasure/@w:val` (`w:sz`, typed `xsd:unsignedLong` directly in docx4j's schema), and, through `parseInteger`,
to `ST_TextScale`, which XJC binds to `Integer` because the schema bounds it (0..600); every other field stays
`BigInteger`. XJC generates one adapter per customisation (`org.docx4j.wml.Adapter1` to `Adapter10`), and 138
attribute fields in the wml classes carry `@XmlJavaTypeAdapter` (every integer-typed attribute field, by a scan of
the generated sources; 105 before the schema change below); MOXy honours the same annotation (checked: the
lenient read and the probe file on the MOXy runtime). A `javap` diff of every `org.docx4j.wml`, `w14` and `w15`
class against the jar built before the change shows no other difference than the adapters and the new class
(`CTTextScale.val` was `Integer` and is again; a first regeneration had made it `BigInteger`, which
`PropertyCatalogueTest` caught).

**The schema.** 33 attributes of `wml.xsd` carried an anonymous inline `xsd:integer` type where ECMA-376 has
`CT_DecimalNumber` (`w:gridSpan`, `w:gridBefore`, `w:gridAfter`, `w:numId`, `w:ilvl`, `w:outlineLvl`, `w:divId`,
`w:id` of a comment or bookmark range, a list level's `w:start`, `w:lvlRestart` and `w:lvlPicBulletId`, a level override's `w:startOverride`, the table
style band sizes, settings such as `w:consecutiveHyphenLimit`, `w:pixelsPerInch`, mail-merge and form-field
values); a named-type customisation does not reach an anonymous type, and `w:gridSpan w:val="2.0"` still threw.
They are retyped to `ST_DecimalNumber`, the Java type unchanged (`BigInteger` either way). Four
`restriction base="xsd:integer"` remain, the named types.

**The rule as coded.** `parse`: a fast path for Word's integer form (`[+-]?\d+`, also BigInteger's); else the empty
string is 0; else the lexical form `[+-]?\d*(\.\d*)?` with at least one digit gives its integer part (truncation
toward zero; `-0.5` is 0); else null, the attribute dropped, one WARN per value naming the text. Unmeasured and
treated as refused: a sign or a dot with no digit (`-`, `.`, `+`, `+.`), and digits outside ASCII `0`-`9` (BigInteger
accepted Unicode digits; Word's measured form is ASCII). docx4j-python reads those four as 0 (its CR-021 D1 records
the difference); its phase C files on the share (`__tmp/python-cr021/cr021-edge-E2..E4-raw.docx`: `.`, `+`, `-`) will
settle it in Word, and both ports then follow Word. `239.99999999999999` reads 239 here (the string is parsed, never a
double). **Jason's run of those files (2026-10-26, python-cd):** `239.99999999999999` saved as 239 (the string truncated,
as here); `.`, `+` and `-` were **not refused**: Word opened each file and its save holds no `w:spacing` and no `w:ind`
for them, which, with a style value of 0 in those fixtures, cannot tell "read as 0" from "ignored". So the WARN's
wording ("a form Word refuses") overstates these three; whether docx4j keeps the drop or reads 0 waits on a probe
under a non-zero style value (round 6's group E, `__tmp/cr066/round6/`). python-cd also saw a `w:pgMar` dropped by a
save open with Word's Normal margins (2.54 cm top). No trimming (D3 as accepted). `print` writes the value as
held (CR-066 adds the range report there).

**Reporting (D6).** The parse method knows the type, not the part, so the part logs for it:
`JaxbXmlPartXPathAware.unmarshal` (stream and DOM entries) brackets the read in a re-entrant thread-local scope
(`LenientInteger.beginReport` / `endReport`; the preprocessor retry nests inside it) and logs one INFO line per part
with the counts and up to four values (`/word/document.xml: 48 decimal integer attribute values truncated as Word
reads them; 2 empty read as 0; 9 of a form Word refuses dropped (e.g. "120.5" -> 120, ...)`). Outside a scope
(`XmlUtils.unmarshalString`) each lenient read logs at INFO on its own. A dropped value is always WARNed as met.

**The stylesheet.** `mc-preprocessor.xslt`'s rounding list stays (D2) and no longer fires for a decimal: the first
unmarshal succeeds, so the retry is never reached for one. Its half-even results are gone with it: 141.5 reads 141
(was 142), 241.5 reads 241, 9001.5 reads 9001, 11907.5 reads 11907, 1.8 reads 1, 400.7 reads 400,
141.99999999999994 reads 141 (`DecimalAttributesProbeTest`, families C, D, G, H).

**Tests.** `LenientIntegerTest` (core-tests, `org.docx4j.jaxb`): every measured form of §3 through `parse`, the
attribute types of D4 through the binding (`w:sz`, borders, `w:tcW` pct, `w:gridSpan`, `w:pgMar` empties,
`w:pgNumType`), a marshal round trip, the report scope. `DecimalAttributesProbeTest`: phase 0's all-cases file
(`cr065-decimals-probe.docx`, the bytes Word was given) loads all four parts, and each of its 65 cases reads what
Word's saves hold (§3), the refused forms as null. `UnreadablePartTest` (phase 1) keeps its guarantee with fixtures
that are not well-formed XML; its `"abc"` fixtures now read (the attribute dropped), which is the point of this
phase.

**Cost.** The 30,000-paragraph synthetic document of CR-064 §3: load best/median 358/379 ms before, 359-368/364-393
ms after, three runs each; within the run-to-run noise. The adapter is one indirection and a character scan per
integer attribute.

**Not done here.** The out-of-range report (CR-066 phase 1, in `print` and `parse`); the retirement of the
stylesheet's list (left in place, D2); other namespaces' integer attributes (`w14`, `w15`, drawing, PresentationML,
SpreadsheetML) keep JAXB's strict parse: Word's rule was measured for WordprocessingML only.

