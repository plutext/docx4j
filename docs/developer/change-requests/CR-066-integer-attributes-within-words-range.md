# CR-066: integer attributes written within Word's range; an out-of-range value reported on write and on read

Status: PROPOSED 2026-10-26, opened by Claude Fable 5.1 (session docx4j-25) at Jason's word ("Do new write-side CR with
round 5 as its shared phase 0, separate from CR-065, whose phase 2 ships first and gains the range report when round 5
answers. open it and build the probe"), from CR-065 §3's round 2 aside: Word kept 7205001 twips as -3959. Phase 0, round
5 of the CR-065 Word probe, BUILT 2026-10-26 (§3, on the share at `__tmp/cr066/`); Jason's run awaited. docx4j's write
side measured (§1, §2). Recommendations in §4; phases in §5.

## 0. Summary

Word stores WordprocessingML's measures in 16 bits. A twips value outside that range is not refused and not repaired:
Word reads it modulo 2^16 (7205001 in `w:ind/@w:left` becomes -3959; 1205003 in `w:spacing/@w:before` becomes 25355,
which is 17.6 inches of space before every paragraph of a header, and a document of "100's of pages"). docx4j holds
these attributes as `BigInteger` and writes whatever it is given; twenty-two of its own helpers compute such a value
from units (a CSS length, a page width divided by a column count, a sum of merged widths) with no range check (§2); and
on read it holds an out-of-range value as written, so the indent docx4j reports is 7205001 where Word shows -3959.

Three things, in order: **measure** the range per attribute type (phase 0: wrap, clamp at the storage limit, clamp at
the UI limit, keep, or refuse; one Word probe shared with the three ports, as CR-065's was); **report** an out-of-range
value at marshal and at unmarshal (phase 1, in the print and parse methods CR-065 phase 2 binds to the integer simple
types: WARN with the value Word will read, never a throw at save, never a rewrite on read); **guard** the helpers where a
caller's units become twips (phase 2). The whole-tree check belongs to the CR-064 tree validator and the inspector when
they exist, not to a new walker.

CR-065 is the read side (a decimal, an odd form; the lenient parse). This CR is the write side and the range. CR-065
phase 2 ships first, without the range report, and gains it when phase 0 here answers; the class is the same.

## 1. What is known

**Word (measured 2026-10-26, CR-065 round 2, by a generator slip):** families B to H of the CR-065 probe carried, in
`styles.xml`, `settings.xml` and the header, the sentinel integers meant to be replaced. Word 365 opened each file clean
and its saves hold:

| Attribute | Type | Written | Word's save | Reading |
|---|---|---|---|---|
| a style's `w:ind/@w:left` | ST_SignedTwipsMeasure | 7205001 | -3959 | 7205001 mod 65536 = 61577, as a signed 16-bit value |
| `w:settings/w:defaultTabStop/@w:val` | ST_TwipsMeasure | 7205002 | 61578 | mod 65536, unsigned |
| the header's `w:spacing/@w:before` | ST_TwipsMeasure | 1205003 | 25355 | mod 65536, unsigned |

So Word's twips are 16-bit internally, signed where the schema type is signed, and an out-of-range value wraps. Whether
every integer type wraps the same way, whether Word's UI limits (22 inches for a page, 1638 pt for a font) also apply
on read, what a negative does in an unsigned attribute, and how wide `w:pgNumType/@w:start` is, are phase 0's
questions. The Office JS side (what Word's own setters accept) is not measured: core-ts's check 71 is a proposal, not
written or run (core-ts, 2026-10-26). core-ts itself today writes pt × 20 for every measurement setter with no check,
400,000 pt included, and reads the value back unchanged (measured at its 7f00fad); so none of the ports refuses, wraps
or clamps.

**docx4j today (measured 2026-10-26, 17.3.5-SNAPSHOT, scratchpad `probe66/Today66.java`), on the six round 5 group
files (§3):** every file loads, every part; the main part holds each value as written (`BigInteger`, no wrap, no clamp,
no log line); a save after an edit writes every out-of-range value back unchanged (82 values across the six files,
identical before and after). On the write side: `TblFactory.createTable(1, 1, 70000)` writes `w:gridCol w:w="70000"`
and `w:tcW w:w="70000"`; a `PageDimensions` whose left margin is 70000 gives `getWritableWidthTwips()` -59533, which
`TocEntry` would put in a right tab stop; `PPrBase.Spacing.setBefore(BigInteger.valueOf(-1))`, on the unsigned type,
writes `w:before="-1"`. No exception anywhere, nothing logged.

## 2. Where docx4j computes such values

An inventory of `docx4j-core` and `docx4j-export-fo` (2026-10-26, an Explore agent's sweep, checked at the sites named
in §1): 22 places compute a measure and set it on a wml object; none has an upper bound or a 16-bit clamp;
`docx4j-export-fo` has none of its own (it sets literals or copies). `UnitsOfMeasurement`'s `inchToTwip`, `mmToTwip`,
`pointToTwip` and `pxToTwip` are a `Math.round` with no range check (a huge input stops at `Integer.MAX_VALUE`, and
`BigInteger.valueOf` writes it).

Persisted into a saved document:

| Site | Sets | From |
|---|---|---|
| `model/table/TblFactory.java:76, 97` | `w:gridCol/@w:w`, `w:tcW/@w:w` (dxa) | the caller's `cellWidthTwips`; the usual idiom is `writableWidthTwips / cols` |
| `toc/TocEntry.java:244, 246, 253` | `w:tab/@w:pos` | the TOC style's left indent + 330 + 110 × the label's length; the writable width from `PageDimensions` (negative when the margins exceed the page) |
| `model/revisions/Tables.java:377, 460` | `w:gridCol/@w:w`, `w:tcW/@w:w` | a sum of merged grid columns; a removed cell's width added to its neighbour's |
| `model/properties/paragraph/Indent.java:112`, `SpaceBefore.java:94`, `SpaceAfter.java:93`, `LineSpacing.java:99` | `w:ind/@w:left`, `w:spacing/@w:before`, `@w:after`, `@w:line` | a CSS length (px, cm, mm, in, pt, em, ex; a percentage for line) converted to twips |
| `model/properties/table/AbstractCellMargin.java:72`, `table/Indent.java:79`, `table/tr/TrHeight.java:75` | cell margins, `w:tblInd`, `w:trHeight/@w:val` | a CSS length converted to twips |
| `model/properties/run/Spacing.java:97`, `run/FontSize.java:84-131` | `w:rPr/w:spacing`, `w:sz` | a CSS length; a keyword or em/ex/pt/px font size to half-points |

Set on a conversion copy only (PDF and HTML output; reach a document only if the preprocessed package is saved):
`model/table/TableModel.java:690, 741` (an absorbed row's height; a grid column from `tblW` less the declared
columns, guarded `w > 0` only) and `convert/out/common/wrappers/UnequalColumns.java:305-336` (`tblW`, `tcW`, `gridCol`
from a section's `w:col` widths and spaces, which are checked for null and ≤ 0 but not summed within range).

Excluded: literal-only sites (`PageDimensions.setMargins` and `setPgSize`, `PropertyResolver.twips`, `StyleTree`,
`FOPAreaTreeHelper`); copies with no arithmetic (`StyleUtil.apply`, `ImmutablePropertyResolver`,
`PageDimensions.withVerticalMargins`, `ConversionSectionWrapperFactory`, the FO and HTML XSLT functions); the border
CSS constructors (TODO stubs; no `w:sz` or `w:space` is ever computed); counts and ids (gridSpan, `w:cols/@w:num`,
`NumberingDefinitionsPart.restart`, bookmark and num ids); EMUs (`BinaryPartAbstractImage`). `w:position`, text scale
and `w:pgNumType/@w:start` are never computed by docx4j. The CSS converters' main caller, ImportXHTML, is a separate
repository and the likeliest real source of an out-of-range twips value (a CSS width in a large unit, a percentage read
as a length); that is its own hand-off (phase 3). User code is the other source: points passed where twips are wanted
(×20), EMUs where twips (×635), a width computed from a page width whose margins are wrong.

The schema's count of the attributes concerned (`xsd/wml/wml.xsd`): 30 `ST_TwipsMeasure` and 14 `ST_SignedTwipsMeasure`
(objects-ts's 44, verified), 50 `ST_DecimalNumber` (among them `CT_TblWidth/@w:w`, whose unit is the sibling `w:type`,
and `w:pgNumType/@w:start`), one each of `ST_SignedHpsMeasure`, `ST_EighthPointMeasure`, `ST_PointMeasure` and
`ST_TextScale`, and `CT_HpsMeasure/@w:val` (`w:sz`), bound as `xsd:unsignedLong` in docx4j's copy of the schema.

## 3. Phase 0: round 5 of the Word probe (built 2026-10-26; Jason's run awaited)

On the share at `__tmp/cr066/`: `groups/cr066-{P,B,R,T,S,N}.docx` (open these first), `families/cr066-<F>.docx` (one
attribute each, 22 files, for a refused group), `singles/cr066-<id>.docx` (one value each, 48 files, for a refused
section or N family), `cases.txt` (id, group, attribute, value, the value Word keeps if it wraps at 16 bits, the value if
it clamps), `families.txt`, `README.txt` (Jason's steps). 191 cases. Generator: scratchpad `probe66/Probe66.java` (a
docx4j-made template whose `document.xml` is rewritten); reader of Word's saves: `probe66/read66.py` (kept, wrap, clamp
or absent per case, against `cases.txt`'s two prediction columns; self-tested: every case of the unsaved files reads
back as kept); docx4j today: `probe66/Today66.java` (§1).

| Group | Family | Attribute (type) | Values |
|---|---|---|---|
| P | P1 | `w:spacing/@w:before` (ST_TwipsMeasure) | 32767, 32768, 65535, 65536, 65537, 1205003 |
| P | P2 | `w:ind/@w:left` (ST_SignedTwipsMeasure) | 32767, 32768, 65535, 65536, 65537, -1, -32768, -32769, 7205001 |
| P | P3, P4 | `w:spacing/@w:line` under `lineRule` auto and exact (ST_SignedTwipsMeasure) | as P2 |
| P | P5 | `w:tab/@w:pos` (ST_SignedTwipsMeasure) | as P2 |
| B | B1 | `w:pBdr/w:top/@w:sz` (ST_EighthPointMeasure; the UI stops at 48) | 48, 49, 255, 256, 32767, 32768, 65535, 65536, 65537 |
| B | B2 | `w:pBdr/w:top/@w:space` (ST_PointMeasure; schema max 31) | 31, 32, 255, 256, 32767, 32768, 65535, 65536, 65537 |
| R | R1 | `w:sz/@w:val` (half-points; the UI stops at 3276) | 3276, 3277, 32767, 32768, 65535, 65536, 65537 |
| R | R2 | `w:position/@w:val` (ST_SignedHpsMeasure; the UI stops at ±3168) | 3168, 3169, 32767, 32768, 65535, 65536, -1, -3169, -32768, -32769 |
| R | R3 | `w:rPr/w:spacing/@w:val` (ST_SignedTwipsMeasure; the UI stops at 31680) | 31680, 31681, 32767, 32768, 65535, 65536, -1, -32768, -32769 |
| R | R4 | `w:w/@w:val` (ST_TextScale; schema 0..600) | 600, 601, 32767, 32768, 65535, 65536, 65537 |
| T | T1, T2, T3 | `w:tblW`, `w:tcW`, `w:tblInd` `@w:w` with `type=dxa` (ST_DecimalNumber; twips) | as P2 |
| T | T4, T5 | `w:tcW`, `w:tblW` `@w:w` with `type=pct` (fiftieths of a percent) | 5000, 5001, 32767, 32768, 65535, 65536, 65537, -1 |
| T | T6 | `w:trHeight/@w:val` (ST_TwipsMeasure) | as P1 |
| S | S1, S2 | `w:pgSz/@w:w`, `@w:h` (ST_TwipsMeasure; the UI stops at 31680) | 31680, 31681, 32767, 32768, 65535, 65536, 65537, 1205003 |
| S | S3 | `w:pgMar/@w:top` (ST_TwipsMeasure) | as P1 |
| S | S4 | `w:pgNumType/@w:start` (ST_DecimalNumber; a PAGE field in the footer shows it) | 32767, 32768, 65535, 65536, 65537, 2147483647, 2147483648, 4294967295, 4294967296, -1 |
| N | N1 | -1 and -32769 in unsigned attributes: before, border sz and space, `w:sz`, text scale, trHeight, pgMar top, pgSz w | 16 cases |
| Z | | control: `w:spacing/@w:before="120"` | in every file |

Design: every value is a plain integer, Word's lexical form (CR-065 round 4), so nothing is refused for its form; a
negative in an unsigned attribute is the one form that might be, so group N holds all of those. The paragraph style
`ProbeBase` (before 100, left 100, line 300 auto) and the character style `ProbeRun` (sz 18, position 2, spacing 2, w
90) give every tested property a non-zero inherited value, so a value Word wraps to 0 is written as 0 in its save and a
value Word drops is absent (Word writes direct formatting as a difference from the style; CR-065 §3 lost two values to
that). Each section case carries the header and footer references and `w:type="continuous"`. `w:gridCol` is not probed
(Word recomputes the grid from the cells, CR-065 §3); the style, settings and header parts are not probed again (§1
measured them). The six group files isolate the refusal risk (S: a page of width 0; N: the sign) so that a refusal costs
one more round on the family files, and the singles are ready for a refused section family.

What Word answers: open, repair or refuse, per group file; Word's save of each that opens (the oracle: each value kept,
wrapped, clamped or dropped); the footer's page number on S4's pages. The question per family is **wrap, clamp (at the
storage limit or at the UI limit), keep, or refuse**, and for N whether a negative is read as its two's complement.

**docx4j today on the probe files:** everything loads and round-trips unchanged (§1).

## 4. Decisions

| # | Question | Options | Recommendation |
|---|---|---|---|
| D1 | Where the range report lives | (a) the `printMethod` and `parseMethod` CR-065 phase 2 binds to each integer simple type (the xjb `javaType` customisation gives both; the print method runs at marshal for every attribute of the type, in every part, under both JAXB runtimes); (b) a save-time tree walk; (c) the setters (generated; no hook) | (a). One class, one range table (type to signed or unsigned 16-bit, or wider, from phase 0). CR-065 phase 2 ships the class without the report and gains it here. (b) is the CR-064 validator's job (D6). |
| D2 | Severity at marshal | (a) WARN, naming the part is not possible there, so the attribute's type, the value and what Word will read ("w:ind/@w:left 7205001 is outside Word's 16-bit range; Word reads it as -3959"), and write the value as held; (b) throw; (c) rewrite to the wrapped or clamped value | (a). A failed save loses more than a wrong size, and marshal is too late to name the caller; a silent rewrite hides the defect and writes a number nobody asked for. The caller's number is the one to fix, and the guard (D5) is where the caller is. |
| D3 | On read | (a) WARN the same way and hold the value as written; (b) emulate Word's reading (wrap or clamp) | (a), agreeing with python-cd: emulating swaps one wrong number for another and destroys the evidence; the report says what Word shows. docx4j's HTML and PDF output of such a document therefore differ from Word's, knowingly. |
| D4 | Which boundary | (a) the storage limit phase 0 shows (signed -32768..32767, unsigned 0..65535, if Word wraps); (b) Word's UI limit per attribute (pgSz 31680, `w:sz` 3276, position 3168, border sz 48, space 31, text scale 600, pct 5000) | Phase 0 decides per type: report at the boundary where Word changes the value. If Word keeps a value between the UI limit and the storage limit, that value is not reported (it is what Word holds, whatever the dialog allows). |
| D5 | Hard guards where a caller's units become twips | (a) `IllegalArgumentException` naming Word's range at `TblFactory.createTable` and the CSS converters' public constructors in `model/properties`; (b) WARN only; (c) nothing beyond D1 | (a) at `TblFactory` (a cell width beyond 16 bits is always a caller's mistake) and (b) at the CSS converters and `TocEntry`/`PageDimensions` for now (a CSS length that converts out of range, or margins wider than the page, come from a document, and ImportXHTML's users expect a document back; its own CR decides whether to throw). (a) for the converters when ImportXHTML's CR lands. |
| D6 | A whole-tree check ("find every out-of-range value in this package") | (a) in the CR-064 D8(b) tree validator and the CR-050 inspector when they exist; (b) a new walker here | (a), later; D1's report is the floor every save gets. |
| D7 | Types covered | the integer simple types of CR-065 D4 that phase 0 shows are 16-bit; `ST_DecimalNumber` attributes individually (`CT_TblWidth/@w:w` by its `w:type`; `w:pgNumType/@w:start` as phase 0 finds); EMUs and counts excluded | as stated; the range table is per type, with the two `ST_DecimalNumber` cases by attribute. |
| D8 | Report once per document | the same question as CR-065 D6: a static method has no document context | the same mechanism (a thread-local counter per load or save, or per value); decided in CR-065 phase 2 and reused. |

## 5. Phases

| Phase | Work | Status |
|---|---|---|
| 0 | Round 5 of the Word probe (§3); the range per type recorded in §3 and sent to python-cd, objects-ts and core-ts | BUILT 2026-10-26; Jason's run awaited |
| 1 | D1 to D4, D7, D8: the range table and the report in the print and parse methods of CR-065 phase 2's class; tests per type with the probe's values; CHANGELOG | after phase 0 and CR-065 phase 2 |
| 2 | D5: the guard at `TblFactory`, the WARN at the CSS converters and `TocEntry`; tests; CHANGELOG | after phase 0 |
| 3 | Hand-offs: the measured range to the three ports and to ImportXHTML (its CSS length conversion); D6 to the CR-064 validator and the inspector when they exist | after phase 0 |

## 6. Risks

- **Log volume.** A document with many out-of-range values would log once per attribute per save; D8's counter keeps
  it to one line per document with a count, as CR-065 D6 does for the lenient read.
- **Behaviour change at a guard.** A caller that produced a (wrong) document from `TblFactory.createTable` gets an
  exception; that is the point, and the message names the range and the argument. The CSS converters keep producing a
  document (WARN) until ImportXHTML decides.
- **UI limit against storage limit.** If phase 0 shows clamping at the UI limit for some types and wrapping for others,
  the range table carries both kinds; a type is never guessed.
- **One Word.** Word 365 is the measurement; older Words may differ at the UI limits, not at 16 bits.
- **Cost.** The print method runs for every integer attribute of every save; a range compare on a `BigInteger` is
  cheap, measured with phase 1 as CR-064 §3 and CR-065 phase 2 are.

## 7. Relation to CR-065

CR-065 is the read side: Word's lexical form and truncation, the lenient parse bound to the integer simple types (its
phase 2), and the data-loss fix (its phase 1, done). Its §3 round 2 aside is this CR's §1. Its phase 2 class carries this
CR's report once phase 0 answers; until then it ships without a range check, and a value outside 16 bits is read and
written as it is today.
