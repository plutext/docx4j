# CR-065: integer attributes read as Word reads them; a part that cannot be read is never replaced by an empty one

Status: PROPOSED 2026-10-26, opened by Claude Fable 5.1 (session docx4j-25) at Jason's word ("CR-065: open it, build the
probe, and put it on the share with its README"), after docx4j-python-cd found the defect through JPype and
docx4j-generated-objects-ts-82 reproduced it (both 2026-10-26; portfolio relay). Reproduced here in Java (§1). Phase 0,
the shared Word probe, is BUILT and on the share (§3), awaiting Jason's run. Recommendations in §4; phases in §5.

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
   `HeaderPart.getContent`, `FooterPart.getContent`, `DocumentSettingsPart` (two sites), `StyleDefinitionsPart` and
   `DocPropsExtendedPart`: each replaces a part whose unmarshal failed with an empty one on first access.

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
were unaffected.

*Family A* (`w:spacing/@w:before` with the odd forms) was refused whole. Round 3 splits it: A1 the pure decimals
(120.5, 120.4, 121.5, 122.5, 240.0, 141.99999999999994), A2 the forms ("240.", ".5", "+240", "+240.5", " 240.5 ",
" 240 "), A3 the comma decimal, A4 hex, A5 the exponents, A6 the empty string, A7 "abc" (`families/cr065-A1..A7-*.docx`).
Expected from B to I: A1 opens (truncation); the rest is the question.

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
| D3 | The rounding rule and the odd forms ("", "abc", "1e3", "0x10", " 240.5 ", "240,5", a negative where unsigned) | Word's, from the probe | **Measured (round 2): a decimal is truncated toward zero (its integer part); the empty string reads as 0.** The odd forms await round 3. Phase 2 reads as Word reads. If Word repairs a form rather than reading it, docx4j drops that attribute and logs; if Word refuses the file, docx4j may still read it (more lenient than Word is a choice, recorded). |
| D4 | Which simple types get the lenient parse | `ST_DecimalNumber`, `ST_UnsignedDecimalNumber`, `ST_TwipsMeasure`, `ST_SignedTwipsMeasure`, `ST_HpsMeasure`, `ST_SignedHpsMeasure`, `ST_EighthPointMeasure`, `ST_PointMeasure`, `ST_TextScale`, `ST_PixelsMeasure`; the full list from `wml.xsd` at phase 2 | all of them; one method. |
| D5 | The empty string (`header=""`, `rsidR=""`) | (a) as Word reads it (probe); (b) today's stylesheet rule (0 for header/footer, dropped for gutter and rsids) | phase 0 decides; rsids are strings and are a separate question (kept as `""` today). |
| D6 | Report the repairs | (a) each lenient read logs at INFO with the attribute, the text and the value taken, once per document; (b) silent | (a); the portfolio survey's skipped-content report (`broken_producer_repairs_across_ports.md` §4 item 4) would carry the same. |

## 5. Phases

| Phase | Work | Status |
|---|---|---|
| 0 | The shared Word probe (§3); the rule recorded in §3 and sent to python-cd, objects-ts and core-ts | BUILT 2026-10-26; Jason's run awaited |
| 1 | D1: the data-loss fix in the six conveniences, with a test per part type (a decimal that fails today; the edit throws; the save keeps the bytes) | proposed; no Word dependency, first |
| 2 | D2, D4, D6: `LenientInteger`, the xjb customisation, regeneration, the three JAXB contexts unchanged; tests for each value form with Word's rule; the stylesheet list left in place; CHANGELOG | proposed; after phase 0 |
| 3 | Hand-offs: the measured rule to the three ports (objects CR-015, python's integer reader, core-ts) | after phase 0 |

## 6. Risks

- **Phase 2 changes every load.** A lenient parse runs for every integer attribute of every document; the parse is a
  string test and a `BigInteger` construction, as today's, plus a fallback path taken only by bad values. Measured
  before commit, as CR-064 §3 was.
- **MOXy.** The xjb customisation is XJC's and applies to the generated classes, so both runtimes see it; tested on
  both.
- **Rounding differently from Word** is the risk phase 0 removes.
