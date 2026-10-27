# CR-066: integer attributes written within Word's range; an out-of-range value reported on write and on read

Status: PHASE 4 DONE 2026-10-27 (§10.6; gate green; hand-offs to python, core-ts, the portfolio, objects-ts and docx4j-mcp sent; docx4j-mcp
and objects-ts acknowledged (no row differs; one type-level row added, 118); Word's check of fixture cases 75 to 82 pending, Jason). PHASE 4 ACCEPTED 2026-10-27 (Jason: "commit, Decisions D9 to D14 as recommended"). Phase 4 PROPOSED 2026-10-27 (§10, D6 in
the CR-050 inspector, at Jason's word: "a CR-066 phase 3 addendum please", after "should we do CR-064 tree validator or CR-050
inspector first?"). PHASES 1 AND 2 DONE 2026-10-26 (§9; Jason: "do
CR-066 phases 1 and 2"); phase 3's hand-offs sent (ImportXHTML and the ports, 2026-10-26); D6's receiver decided by §10: the inspector,
which exists, not the CR-064 validator, which does not. ACCEPTED 2026-10-26 (Jason: "CR-066 decisions as recommended"): D1 to D8 as recommended in §4, D1, D4 and D7 as
amended by §3.1 (the width is per attribute; the type-level report is exact for the uniform types and the certain range for
`ST_TwipsMeasure`; `ST_DecimalNumber` by attribute through D6 or a setter patch). Phase 0 DONE (CR-065
phase 2 is in, fe449c35c). PROPOSED 2026-10-26, opened by Claude Fable 5.1 (session docx4j-25) at Jason's word ("Do new write-side CR with
round 5 as its shared phase 0, separate from CR-065, whose phase 2 ships first and gains the range report when round 5
answers. open it and build the probe"), from CR-065 §3's round 2 aside: Word kept 7205001 twips as -3959. Phase 0, round
5 of the CR-065 Word probe, DONE 2026-10-26 (§3: Jason's run, all six group files opened clean; the storage width is per
attribute, not per type: 16 bits signed or unsigned, 8 and 5 bits for a border, 32 bits for a page number, with clamps on
page width and tab stops and the rule carried in the sign for row height and exact line spacing). docx4j's write side
measured (§1, §2). Recommendations in §4 (D1, D4 and D7 amended by the result); phases in §5.

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
caller's units become twips (phase 2). The whole-tree check belongs to the CR-050 inspector (phase 4, §10: a `value.out_of_range`
finding per attribute, with the element, the attribute and Word's reading), not to a new walker; the CR-064 validator,
named with it in D6, does not exist and is not needed for it.

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
questions. The Office JS side (what Word's own setters accept) is core-ts's check 71: written 2026-10-26 at Jason's
word, on the share at `__tmp/71/` (96 cases: the paragraph indents and spacings, a cell's width and a list level's
indent at 1638, 1638.4, 1639, 3276.7, 3276.8, 400000 pt and negatives; font size at 1638 to 400000 and -1), Jason's
run awaited; its Word save also measures `w:firstLine`, `w:hanging` and `w:after`, unmeasured in §3.1. Results go
beside §3.1 when they arrive. core-ts itself today writes pt × 20 for every measurement setter with no check,
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

## 3. Phase 0: round 5 of the Word probe (built 2026-10-26; Jason's run 2026-10-26: DONE)

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
| S | S3 | `w:pgMar/@w:top` (ST_SignedTwipsMeasure) | as P1 |
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

### 3.1 Result (Jason, 2026-10-26: "cr066 groups all opened without needing repair")

All six group files opened clean, no repair; the family and single files were not needed. Read from Word's saves
(`groups/cr066-{P,B,R,T,S,N}-word365.docx`, `probe66/read66.py`, then the raw XML of every case that was not a plain
wrap). **No value was refused or repaired, and none was kept beyond its storage width; a negative in an attribute the
schema calls unsigned is accepted everywhere.** The width and the sign are the attribute's, not the simple type's:

| Attribute (schema type) | Word's storage, from the saves | Evidence (written -> saved) |
|---|---|---|
| `w:ind/@w:left`, `w:spacing/@w:line` auto, `w:rPr/w:spacing` (signed twips); `w:position` (signed half-points); `w:tblW`, `w:tcW`, `w:tblInd` `@w:w` under `dxa` and under `pct` (ST_DecimalNumber) | **int16, wraps**; no UI clamp | 32768 -> -32768; 65535 -> -1; 65536 -> 0; -32769 -> 32767; 7205001 -> -3959; 3169 (position), 31681 (run spacing), 5001 and 32767 (pct) kept; tblInd 0 not written |
| `w:pgMar/@w:top` (ST_SignedTwipsMeasure; `top` and `bottom` are signed in the schema, the other margins unsigned) | **int16, wraps** | 32768 -> -32768; 65535 -> -1; -1 kept; -32769 -> 32767; 1205003 -> 25355 |
| `w:spacing/@w:before` (ST_TwipsMeasure) | **uint16, wraps**; 0xFFFF is written as -1 | 32768 kept; 65536 -> 0; 65537 -> 1; 1205003 -> 25355; 65535 -> **-1**; -1 -> -1; -32769 -> 32767 |
| `w:sz/@w:val` (half-points) | **uint16, wraps**; no UI clamp | 3277, 32768, 65535 kept; 65536 -> 0; 65537 -> 1; -1 -> 65535; -32769 -> 32767 |
| `w:w/@w:val` (ST_TextScale, schema 0..600) | **uint16, wraps**; no clamp at 600 | 601, 32767, 65535 kept; 65536 -> 0; -1 -> 65535; -32769 -> 32767 |
| `w:tab/@w:pos` (signed twips) | **int16, wraps, then clamped to ±31680** (22 in, the UI limit) | 32767 -> 31680; 32768 -> -31680; 65535 -> -1; -32768 -> -31680; -32769 -> 31680; 7205001 -> -3959 |
| `w:pgSz/@w:w` (ST_TwipsMeasure) | **uint16, wraps, then clamped to 144..31681** | 31681 kept; 32767, 32768, 65535, -1, -32769 -> 31681; 65536, 65537 -> 144; 1205003 -> 25355 |
| `w:pgSz/@w:h` (ST_TwipsMeasure) | **uint16, wraps, then a minimum of 144**; no maximum | 31681, 32767, 32768, 65535 kept; 65536, 65537 -> 144; 1205003 -> 25355 |
| `w:spacing/@w:line` exact (signed twips) | **int16 after negation**: exact is stored as the negative, so the wrapped value's sign re-derives the rule and its magnitude the line | exact 32768 -> exact 32768; exact 65535 -> **atLeast 1**; 65536 -> atLeast 0; 65537 -> exact 1; -1 -> atLeast 1; -32768 -> exact 32768; -32769 -> exact 32767; 7205001 -> atLeast 3959 |
| `w:trHeight/@w:val` (ST_TwipsMeasure, unsigned in the schema) | **int16, the sign is the rule** (negative = exact); the one attribute whose storage sign differs from its schema type | 32767 kept; 32768 -> **exact 32768**; 65535 -> exact 1; 65536 -> dropped; 65537 -> 1; -1 -> exact 1; -32769 -> 32767; 1205003 -> 25355 |
| a border's `@w:sz` (ST_EighthPointMeasure) | **8 bits, wraps** (mod 256); no clamp at the UI's 48 | 49, 255 kept; 256 -> 0; 32767 -> 255; 32768 -> 0; 65537 -> 1; -1 -> 255; -32769 -> 255 |
| a border's `@w:space` (ST_PointMeasure, schema max 31) | **5 bits, wraps** (mod 32) | 31 kept; 32 -> 0; 255 -> 31; 256 -> 0; 65537 -> 1; -1 -> 31; -32769 -> 31 |
| `w:pgNumType/@w:start` (ST_DecimalNumber) | **int32, wraps** | 65536, 2147483647 kept; 2147483648 -> -2147483648; 4294967295 -> -1; 4294967296 -> 0; -1 kept |

The widths are those of Word's binary format (the DOC `PAP`, `CHP`, `BRC` and `SEP` structures: `dyaBefore` unsigned,
`dyaTop` signed as the schema's `ST_SignedTwipsMeasure` says, `dyaRowHeight` and `lspd.dyaLine` signed with the sign as the rule, `brc.dptLineWidth` 8 bits,
`brc.dptSpace` 5 bits), so the table is unlikely to move between Word versions. The UI limits (1638 pt, 6 pt, 600 %,
22 in) do not apply on read except where shown (tab stops, page width). Two side effects of the big values: Word
splits a run whose font is 1638 pt or more into one run per character, each with `w:lastRenderedPageBreak` (R1), and a
page at 22 in with 32767 twips of space before a paragraph is the "100's of pages" of CR-065 round 2. The footer's
PAGE fields kept their cached "1" in every section (Jason saw pages 1 and 2 on screen, "of 3"), so the display did not
measure `w:pgNumType/@w:start`; the saves did.

**What this means for the design.** A `printMethod` or `parseMethod` bound to a simple type knows the type, not the
attribute, and the width differs within `ST_TwipsMeasure` (before unsigned with 0xFFFF written as -1, trHeight signed
with a rule, pgSz clamped) and within `ST_DecimalNumber` (TblWidth int16, pgNumType int32, ids and counts not measured and not 16-bit).
So the type-level report can be exact for `ST_SignedTwipsMeasure` (int16 for every measured attribute),
`ST_SignedHpsMeasure` (int16), `ST_TextScale` and `CT_HpsMeasure/@w:val` (uint16), `ST_EighthPointMeasure` (8 bits) and
`ST_PointMeasure` (5 bits); for `ST_TwipsMeasure` it reports the certain range only, a value outside -32768..65535
(changed for every measured attribute of the type), and names the attribute-level table for the rest; for
`ST_DecimalNumber` it reports nothing (D7 amended). The exact per-attribute check (the twips attributes of
`ST_TwipsMeasure`, `w:tblW`, `w:tcW`, `w:tblInd`, `w:pgNumType/@w:start`) is the whole-tree check's (D6), or a patch
on the owning class's setter through `ModifyGeneratedSources` where one class owns the attribute (`TblWidth.setW`).
Unmeasured and left to the certain range: the other 26 `ST_TwipsMeasure` attributes (`w:gridCol/@w:w`, the unsigned
margins, `w:ind/@w:hanging` and `@w:firstLine`, frame and drawing distances), `w:spacing/@w:after` (assumed as
`before`), the other borders' attributes (assumed as `w:top`'s). Whether Word drops a literal `w:trHeight/@w:val="0"` (or `w:spacing/@w:line="0"` under `atLeast`) as it drops the zero that 65536 wraps to is unmeasured (objects-ts's question, 2026-10-27); a literal 0 is within range, so the inspector never reads it.

### 3.2 core-ts's check 71: the Office JS side (Jason, 2026-10-26; Word 365 16.0.20326, Design Mode off)

core-ts's check 71 (96 cases, `__tmp/71/`; its CR-002 §65.5) sets values through Office JS and reads Word's save:

- **The setters take any value and store it wrapped.** `leftIndent`, `rightIndent`, `firstLineIndent` and
  `TableCell.columnWidth` raise no error: 1638.4 pt -> -32768 twips; 1639 -> -32756; 3276.7 -> -2; 3276.75 -> -1;
  3276.8 -> 0 (for an indent, no element); 400,000 -> 4608; -1638.45 -> 32767; and they read back the wrapped value
  (`leftIndent` 400,000 reads 230.4). A negative `columnWidth` is refused (InvalidArgument). So the API wraps exactly as
  the file reader does; it is not a guard.
- **`w:firstLine` and `w:hanging` are one signed field** (DOC's `dxaLeft1`): firstLine F is stored as F, hanging H as
  -H, so firstLine is 0..32767 and hanging 0..32768, and a wrap crosses between them: `firstLineIndent` 1638.4 pt ->
  `w:hanging="32768"`; 1639 -> hanging 32756; 3276.7 -> hanging 2; 400,000 -> firstLine 4608; -1 -> hanging 20;
  -1638.4 -> hanging 32768; -1638.45 -> firstLine 32767. (Round 6, §3.3, measures the file side of the same pair.)
- **Refused at the API's own limit, below storage:** `spaceBefore`, `spaceAfter` and `lineSpacing` at 1638 pt and up
  and every negative (InvalidArgument; Word wrote nothing), so `w:after` stays unmeasured on this side;
  `setLevelIndents` text indent at 1638 and up and at -1638.4; `font.size` above 1638 (1638.5 refused) and 0.
- Side finding (core-ts's): `setLevelIndents(0, -1, 0)` wrote `w:left="-20" w:firstLine="0"`, so
  `bulletNumberPictureIndent` is the level's first-line indent relative to the text, as documented.

### 3.3 Round 6 (built 2026-10-26 at objects-ts's request, Jason's yes; run the same day, §3.4)

The attributes round 5 left unmeasured or inferred, by the same method, on the share at `__tmp/cr066/round6/`
(generator scratchpad `probe66/Probe66r6.java`; `cases.txt` with id, group, attribute, place, value and the two wrap
predictions): 481 cases in seven group files. A: `w:ind` firstLine, hanging, right, start, end; `w:spacing` after;
`w:line` under atLeast. F: `w:framePr` w, h, hSpace, vSpace, x, y. R: `w:fitText`, `w:szCs`, `w:kern`, `w:bdr`
sz and space. T: `w:trHeight` under exact and auto; `w:gridCol` under `tblLayout fixed` with no `tcW` and `tblW`
auto; `w:tblCellSpacing`; `w:tblCellMar`; `w:tcMar`; `w:wBefore`, `w:wAfter`; `w:tcBorders` and `w:tblBorders` sz and
space. P: `w:tblpPr` the four FromText, tblpX, tblpY. S: `w:pgMar` left, right, header, footer, gutter, bottom;
`w:cols` space, `w:col` w and space (equalWidth=0); `w:lnNumType` distance; `w:pgBorders` sz and space. N: a
list level's `w:legacy` legacySpace and legacyIndent in numbering.xml (ten lists, one value each). D: the
document-wide `w:defaultTabStop` and `w:hyphenationZone` as singles, one value per file. Not covered: `w:object`
dxaOrig/dyaOrig (an embedded OLE object). Families (46) and singles (264, for F, P, S, N) are the fallback for a
refused group, as in round 5.

### 3.4 Round 6 result (Jason, 2026-10-26: "cr066/round6 all opened cleanly"; 28 saves, nothing refused or repaired)

Read with `probe66/read66r6.py` and the raw XML of every row that was not a plain wrap. The storage per attribute:

| Attribute (schema type) | Word's storage | Evidence |
|---|---|---|
| `w:ind/@w:firstLine` and `@w:hanging` (ST_TwipsMeasure) | **one int16 field** (DOC `dxaLeft1`): firstLine F is stored +F, hanging H is stored -H, each after a wrap to int16; written as `firstLine` when the stored value is >= 0 (0 included) and as `hanging` with the magnitude when < 0 | firstLine 32768 -> hanging 32768; 65535 -> hanging 1; 65536 -> firstLine 0; -1 -> hanging 1; -32768 -> hanging 32768; -32769 -> firstLine 32767; 7205001 -> hanging 3959; 1205003 -> firstLine 25355. hanging 32768 kept; 65535 -> firstLine 1; 65536 -> firstLine 0; -1 -> firstLine 1; -32768 -> hanging 32768; -32769 -> hanging 32767; 7205001 -> firstLine 3959 |
| `w:spacing/@w:after` (ST_TwipsMeasure) | **uint16**, 0xFFFF written -1, as `before` | 32768 kept; 65535 -> -1; -1 kept; -32768 -> 32768; 7205001 -> 61577; 1205003 -> 25355 |
| `w:spacing/@w:line` under `atLeast` | **int16, stored positive; a wrapped negative becomes exact** with its magnitude | 32768 -> exact 32768; 65535 -> exact 1; 65536 -> atLeast 0; -1 -> exact 1; -32768 -> exact 32768; -32769 -> atLeast 32767; 7205001 -> exact 3959 |
| `w:ind/@w:right`; `w:ind/@w:start` and `@w:end` (read in a transitional document and **written as `left` and `right`**) | **int16** | right 32768 -> -32768, 7205001 -> -3959; start 32767 -> left 32767, 32768 -> left -32768; end 65536 -> right 0 |
| `w:framePr/@w:w` (ST_TwipsMeasure), `@w:hSpace`, `@w:vSpace`, `@w:x`, `@w:y` | **int16**; 0 is not written (w: auto width) | w 32768 -> -32768, 65535 -> -1, 65536 -> absent, -1 kept, 7205001 -> -3959; hSpace, vSpace, x, y the same |
| `w:framePr/@w:h` | **15 bits plus a flag** (DOC `dyaHeight`): the low 15 bits are the height and bit 15 turns `hRule="exact"` off | 32767 kept exact; 32768 -> h 0, no hRule; 65535 -> 32767, no hRule; -1 -> 32767, no hRule; 65537 -> 1 exact; -32769 -> 32767 exact; 7205001 -> 28809 (61577 and 0x7FFF), no hRule |
| `w:rPr/w:fitText/@w:val` (ST_TwipsMeasure) | **kept as written, at least 32 bits** (Word fitted the text and split the run; the value is not 16-bit) | 32767, 32768, 65535, 65536, 65537, -1, -32768, -32769, 7205001, 1205003 all kept |
| `w:szCs`, `w:kern` (half-points) | **uint16**, as `w:sz` | 65535 kept; 65536 -> 0; -1 -> 65535; -32768 -> 32768; 7205001 -> 61577 |
| `w:rPr/w:bdr`, `w:tcBorders`, `w:tblBorders`, `w:pgBorders` `@w:sz` and `@w:space` | **8 bits and 5 bits**, as a paragraph border's | 256 -> 0; 32767 -> 255; -1 -> 255; space 32 -> 0, 255 -> 31, -1 -> 31 (all four families alike) |
| `w:trHeight/@w:val` under `hRule="exact"` | **int16, the sign is the rule**, as round 5 | exact 32768 kept; 65535 -> atLeast 1; -1 -> atLeast 1; -32768 -> exact 32768; -32769 -> exact 32767; 7205001 -> atLeast 3959 |
| `w:trHeight/@w:val` under `hRule="auto"` | **discarded** (no `w:trHeight` written at any value) | all eight absent |
| `w:tblCellSpacing`, `w:tblCellMar/w:left`, `w:tcMar/w:top` `@w:w` under `dxa` (TblWidth) | **int16**, as `tblW` | 32768 -> -32768; 65535 -> -1; 65536 -> 0; -1 kept; 7205001 -> -3959 |
| `w:tblpPr` `@w:leftFromText`, `@w:rightFromText`, `@w:topFromText`, `@w:bottomFromText` (ST_TwipsMeasure), `@w:tblpX`, `@w:tblpY` | **int16**, all six; 0 not written | 32768 -> -32768; 65535 -> -1; 65536 -> absent; -1 kept; -32768 kept; 7205001 -> -3959 |
| `w:pgMar` `@w:left`, `@w:right`, `@w:header`, `@w:footer`, `@w:gutter` (ST_TwipsMeasure) | **uint16**, 0xFFFF written -1, as `before` | 32768 kept; 65535 -> -1; -1 kept; -32768 -> 32768; 7205001 -> 61577 |
| `w:pgMar/@w:bottom` | **int16**, as `top` | 32768 -> -32768; -1 kept; -32768 kept; 7205001 -> -3959 |
| `w:cols/@w:space` | **int16** | 32768 -> -32768; -1 kept; 7205001 -> -3959 |
| `w:cols/w:col/@w:w` (equalWidth=0) | **int16, then a negative width is replaced by a computed one** (9360 here); a width wider than the page is kept | 31681, 32767 kept; 32768, 65535, -1, 7205001 -> 9360; 65536 -> 0; 65537 -> 1; -32769 -> 32767 |
| `w:cols/w:col/@w:space` | **int16, then a negative space becomes -1** | 32768 -> -1; 65535 -> -1; -1 kept; -32768 -> -1; 7205001 -> -1; -32769 -> 32767; 1205003 -> 25355 |
| `w:lnNumType/@w:distance` (ST_TwipsMeasure) | **int16**; 0 not written | 32768 -> -32768; 65535 -> -1; 65536 -> absent; -1 kept; 7205001 -> -3959 |
| `w:settings/w:defaultTabStop`, `w:hyphenationZone` `@w:val` (CTTwipsMeasure) | **uint16** | 65535 kept; 65536 -> 0; -1 -> 65535; -32768 -> 32768; 7205001 -> 61577 (round 2's 7205002 -> 61578 agrees) |
| `w:spacing/@w:before`, `w:ind/@w:left` with `.`, `+`, `-`, `+.` (group E) | **read as 0** (an explicit 0 written under a style value of 100, all eight cases) | CR-065 §8 takes it |

Not measured by this round: `w:trPr/w:wBefore` and `@w:wAfter` (Word dropped them with the `gridBefore`/`gridAfter`
that did not fit the two-cell row; a probe needs a consistent grid); `w:gridCol/@w:w` (recomputed from the cells even
under `tblLayout fixed` without `tcW`: no file-side measurement exists for the grid); a list level's `w:legacy`
(Word 365 dropped the element from numbering.xml and renumbered the lists; not round-tripped at all).

**For the type-level report (D1, D7):** every measured `ST_SignedTwipsMeasure` attribute is int16 (right, start,
end, framePr x and y, tblpX, tblpY, pgMar bottom, cols space). `ST_TwipsMeasure` now shows five behaviours (uint16
with -1 written for 0xFFFF: before, after, the five unsigned margins, defaultTabStop, hyphenationZone; int16: frame w
and spaces, tblpPr's four distances, lnNumType distance; one shared signed field: firstLine and hanging; 15 bits plus
a flag: framePr h; the sign as the rule: trHeight; and fitText not 16-bit at all), so the certain range for that type
stays "outside -32768..65535 is changed", with the per-attribute table for the exact report. The border types are
uniform (8 and 5 bits) across paragraph, run, cell, table and page borders; `CT_HpsMeasure` (sz, szCs, kern) is
uniform uint16.

## 4. Decisions

| # | Question | Options | Recommendation |
|---|---|---|---|
| D1 | Where the range report lives (amended by §3.1: the width is per attribute, so the type-level method reports the exact range for the uniform types and the certain range for `ST_TwipsMeasure`; the per-attribute table belongs to the whole-tree check, D6, or a `ModifyGeneratedSources` patch on `TblWidth.setW`) | (a) the `printMethod` and `parseMethod` CR-065 phase 2 binds to each integer simple type (the xjb `javaType` customisation gives both; the print method runs at marshal for every attribute of the type, in every part, under both JAXB runtimes); (b) a save-time tree walk; (c) the setters (generated; no hook) | (a). One class, one range table (type to signed or unsigned 16-bit, or wider, from phase 0). CR-065 phase 2 ships the class without the report and gains it here. (b) is the CR-064 validator's job (D6). |
| D2 | Severity at marshal | (a) WARN, naming the part is not possible there, so the attribute's type, the value and what Word will read ("w:ind/@w:left 7205001 is outside Word's 16-bit range; Word reads it as -3959"), and write the value as held; (b) throw; (c) rewrite to the wrapped or clamped value | (a). A failed save loses more than a wrong size, and marshal is too late to name the caller; a silent rewrite hides the defect and writes a number nobody asked for. The caller's number is the one to fix, and the guard (D5) is where the caller is. |
| D3 | On read | (a) WARN the same way and hold the value as written; (b) emulate Word's reading (wrap or clamp) | (a), agreeing with python-cd: emulating swaps one wrong number for another and destroys the evidence; the report says what Word shows. docx4j's HTML and PDF output of such a document therefore differ from Word's, knowingly. |
| D4 | Which boundary | (a) the storage limit phase 0 shows (signed -32768..32767, unsigned 0..65535, if Word wraps); (b) Word's UI limit per attribute (pgSz 31680, `w:sz` 3276, position 3168, border sz 48, space 31, text scale 600, pct 5000) | **Answered by §3.1:** the storage limit, per attribute; the UI limits do not apply except tab stops (±31680 after the wrap) and page width (144..31681). A value between a UI limit and its storage limit is kept by Word and is not reported. |
| D5 | Hard guards where a caller's units become twips | (a) `IllegalArgumentException` naming Word's range at `TblFactory.createTable` and the CSS converters' public constructors in `model/properties`; (b) WARN only; (c) nothing beyond D1 | (a) at `TblFactory` (a cell width beyond 16 bits is always a caller's mistake) and (b) at the CSS converters and `TocEntry`/`PageDimensions` for now (a CSS length that converts out of range, or margins wider than the page, come from a document, and ImportXHTML's users expect a document back; its own CR decides whether to throw). (a) for the converters when ImportXHTML's CR lands. |
| D6 | A whole-tree check ("find every out-of-range value in this package") | (a) in the CR-064 D8(b) tree validator and the CR-050 inspector when they exist; (b) a new walker here | (a), later; D1's report is the floor every save gets. **Settled 2026-10-27 (§10, D9):** the CR-050 inspector, which exists; the CR-064 validator is not waited for. |
| D7 | Types covered | the integer simple types of CR-065 D4 that phase 0 shows are 16-bit; `ST_DecimalNumber` attributes individually (`CT_TblWidth/@w:w` by its `w:type`; `w:pgNumType/@w:start` as phase 0 finds); EMUs and counts excluded | **Amended by §3.1:** the range table is per attribute (§3.1's table); the type-level report carries the exact range for `ST_SignedTwipsMeasure`, `ST_SignedHpsMeasure`, `ST_TextScale`, `CT_HpsMeasure/@w:val`, `ST_EighthPointMeasure` and `ST_PointMeasure`, the certain range (outside -32768..65535) for `ST_TwipsMeasure`, nothing for `ST_DecimalNumber`; `w:tblW`, `w:tcW`, `w:tblInd` (int16) and `w:pgNumType/@w:start` (int32) by attribute (D6 or a setter patch). |
| D8 | Report once per document | the same question as CR-065 D6: a static method has no document context | the same mechanism (a thread-local counter per load or save, or per value); decided in CR-065 phase 2 and reused. |

## 5. Phases

| Phase | Work | Status |
|---|---|---|
| 0 | Round 5 of the Word probe (§3); the range per attribute recorded in §3.1 and sent to python-cd, objects-ts and core-ts | DONE 2026-10-26 (Jason's run; §3.1) |
| 1 | D1 to D4, D7, D8: the range table (§3.1) and the report in the print and parse methods of CR-065 phase 2's class, exact for the uniform types and the certain range for `ST_TwipsMeasure`; tests per type with the probe's values; CHANGELOG | DONE 2026-10-26 (§9) |
| 2 | D5: the guard at `TblFactory` (int16 for `w:tcW`, `w:gridCol` assumed uint16), the WARN at the CSS converters and `TocEntry` (tab stops ±31680); tests; CHANGELOG | DONE 2026-10-26 (§9) |
| 3 | Hand-offs: §3.1's table to the three ports and to ImportXHTML (its CSS length conversion); D6, the per-attribute check, to the inspector (§10) | ports told 2026-10-26; ImportXHTML told 2026-10-26 (§9); D6 is phase 4 (2026-10-27) |
| 4 | D6 in the CR-050 inspector (§10): the per-attribute table as one resource, `value.out_of_range` findings with the element, attribute, value, storage, range and Word's reading; the oracle; fixture cases; the WD proposal; the inspector's marshal silenced | DONE 2026-10-27 (§10.6); Word's check and the objects-ts and docx4j-mcp hand-offs pending |

## 6. Risks

- **Log volume.** A document with many out-of-range values would log once per attribute per save; D8's counter keeps
  it to one line per document with a count, as CR-065 D6 does for the lenient read.
- **Behaviour change at a guard.** A caller that produced a (wrong) document from `TblFactory.createTable` gets an
  exception; that is the point, and the message names the range and the argument. The CSS converters keep producing a
  document (WARN) until ImportXHTML decides.
- **UI limit against storage limit.** Resolved by §3.1: the table carries the two clamps (tab stops, page width) beside
  the wraps; an attribute not measured gets the certain range only, never a guess.
- **One Word.** Word 365 is the measurement; older Words may differ at the UI limits, not at 16 bits.
- **Cost.** The print method runs for every integer attribute of every save; a range compare on a `BigInteger` is
  cheap, measured with phase 1 as CR-064 §3 and CR-065 phase 2 are.

## 7. Relation to CR-065

CR-065 is the read side: Word's lexical form and truncation, the lenient parse bound to the integer simple types (its
phase 2), and the data-loss fix (its phase 1, done). Its §3 round 2 aside is this CR's §1. Its phase 2 class carries this
CR's report once phase 0 answers; until then it ships without a range check, and a value outside 16 bits is read and
written as it is today.

## 8. The ports

docx4j-python's CR-022 (its write-side guard, 85be8ef, local) names this CR's phase 0 as its own phase 0 and builds its
limits table from §3's result. One difference by design (python-cd, 2026-10-26): CR-022 raises at every setter of its
content API, facade and builders, and reports values already in a tree through its CR-020 validator; docx4j's setters
are generated with no hook (D1 option c), so the report here is at marshal and unmarshal and the guard only where docx4j
computes a measure itself (D5). Both leave a file's value as it is. python-cd checked the round 5 files read-only before
Jason's run: all 76 well-formed, each loaded with nothing kept and nothing skipped; it re-read a sample of P, B and R in
Word's saves and agrees with §3.1 (its CR-022 §3.1, 4db3814, keys its guard by attribute, the storage width narrowed
where Word clamps; a negative in a Word-unsigned attribute is refused there, where this CR only reports, since Word
accepts it: `w:trHeight/@w:val="-1"` is exact 1, `w:spacing/@w:before="-1"` is kept as -1). Corrected 2026-10-26
(objects-ts, portfolio, python-cd): `w:pgMar/@w:top` is `ST_SignedTwipsMeasure` in the schema, so its int16 storage
matches; `w:trHeight/@w:val` is the attribute whose storage sign differs from its schema type. objects-ts's CR-016 (objects 86ae5cb) encodes §3.1 and §3.4 row for row in its WORD_INTEGER_STORAGE, with three
structures beyond a width (the firstLine/hanging pair, framePr h's height flag, the column negatives) and a test,
test/word365-ranges.mjs, that reads both rounds' saves (652 cases, 614 compared) and fails when a rule is broken;
core-ts takes the measured range when its setters are guarded (phase 3).

## 9. Phases 1 and 2 done (2026-10-26)

**Phase 1, the report (D1 to D4, D7, D8).** `xsd/bindings.xjb` gives each integer simple type its own parse and print
pair in `org.docx4j.wml.LenientInteger`, so the method knows the type: `parseSignedTwips`/`printSignedTwips`
(`ST_SignedTwipsMeasure`, int16), `parseSignedHps`/`printSignedHps` (`ST_SignedHpsMeasure`, int16), `parseHps`/`printHps`
(`CT_HpsMeasure/@w:val`: `w:sz`, `w:szCs`, `w:kern`; uint16), `parseTextScale`/`printTextScale` (`ST_TextScale`, uint16,
Integer-typed), `parseEighthPoint`/`printEighthPoint` (a border's `w:sz`, 8 bits), `parsePoint`/`printPoint` (a border's
`w:space`, 5 bits), `parseTwips`/`printTwips` (`ST_TwipsMeasure`, the certain range -32768..65535 only, no "reads it as"),
and `parse`/`print` unchanged for `ST_DecimalNumber`, `ST_UnsignedDecimalNumber` and `ST_PixelsMeasure` (no report). A
`Range` enum holds the width and the sign; the in-range test is a `bitLength` compare, and Word's reading is the value
modulo the width (signed where the type is). The value is held as written and written as held (D2, D3): nothing is
wrapped, clamped or refused.

The report reuses CR-065 D6's thread-local scope (D8): `Report` gains `getOutOfRange()` and its own four samples
(`7205001 -> -3959 in Word`; `1205003 (ST_TwipsMeasure)` for the certain range), and `summary()` a clause. A part's
unmarshal (`JaxbXmlPartXPathAware.unmarshal`, both entries, as before) logs at WARN instead of INFO when the count is
above zero; a part's marshal (`JaxbXmlPart.marshal(OutputStream, mapper)` and `marshal(Node, mapper)`, which every
save reaches through the part stores) now opens the same scope and logs one WARN with the part name when the count is
above zero (`/word/document.xml: value.out_of_range: 3 integer attribute values outside Word's 16-bit range, held as
written (CR-066; e.g. 7205001 (int16 -32768..32767; Word reads -3959), 70000 (uint16 0..65535; Word reads 4464),
1205003 (int16 or uint16 by attribute (ST_TwipsMeasure) -32768..65535; Word changes it, how depends on the attribute))`).
Outside a scope (a string marshal) each value logs its own WARN. **The shared code** (python-cd's request, 2026-10-26,
after the first commit): every line, the guards' included, carries `value.out_of_range` (`LenientInteger.CODE`), the
code docx4j-python's CR-020 §4.7 and CR-022 and core-ts's CR-017 agreed for an integer attribute Word would misread, so
one search finds every port's report. Of that finding's fields the type-level report gives the value, Word's reading
(`word_reads`; none for `ST_TwipsMeasure`), the storage in the shared vocabulary (`int16`, `uint16`, `8 bits`, `5 bits`)
and the range; the part is on the part's line; the element and attribute are the whole-tree check's (D6). The generated API is unchanged except for the additions (javap of `org.docx4j.wml`, `w14`,
`w15` before and after: the new methods, the `Range` enum and the `Report` members; the ten `Adapter` classes keep their
names and field types, their bodies call the per-type methods). Cost: none measurable (scratchpad `bench/LoadBench`,
bench-30k.docx: best 370 ms before, 358 and 361 ms after).

**Phase 2, the guards (D5).** `TblFactory.createTable` throws `IllegalArgumentException` for `cellWidthTwips` outside
0..`MAX_CELL_WIDTH_TWIPS` (32767), the message naming the argument, the range in twips and inches, and Word's modulo
reading. The CSS converters WARN through a new `Property.warnIfOutsideWordRange` (the CSS name and text, the converted
value and unit, the attribute's range from §3.1 and §3.4) and write the value as computed: paragraph `Indent`
(`w:ind/@w:left`, int16), `SpaceBefore` and `SpaceAfter` (uint16), `LineSpacing` (int16), `AbstractCellMargin` (int16),
table `Indent` (`w:tblInd`, int16), run `Spacing` (int16), `FontSize` (`w:sz`, uint16). `TrHeight`'s CSS constructor is
commented out in the source ("Won't ever get a tr height property from CSS"), so nothing to guard there. `TocEntry`
WARNs for a tab stop outside ±31680 (the TOC style's indent plus the number's width, or the page's writable width) and,
once per TOC, for a `PageDimensions` writable width of zero or less, naming the page width, margins and gutter.

**Tests** (`docx4j-core-tests`): `LenientIntegerTest` (+7: each type's boundaries with the probe's values, the certain
range, `ST_DecimalNumber` silent, the binding reporting through the types and writing as held);
`IntegerRangeReportTest` (a save logs one WARN for the main part and the file holds the numbers; a load logs one WARN
and holds them; a document at the boundaries logs nothing); `TblFactoryRangeTest` (32767 and 0 accepted; 70000, 32768
and -1 refused with the range in the message); `CssLengthRangeWarnTest` (each converter's WARN with the value and the
attribute; 1638.5 pt, beyond the UI, inside the storage range, silent). Gate 2026-10-26: the full reactor build
(`mvn clean install`) green; docx4j-core-tests 2168 tests (2153 before plus the 15 new), 0 failures, 30 skipped;
docx4j-export-fo-tests 858, docx4j-diffx-tests 246.

**Hand-offs (phase 3).** ImportXHTML (session docx4j-importxhtml-4f): the CSS converters it calls now WARN for an
out-of-range length and still write it; §3.1 and §3.4 are the ranges if its own CR decides to throw or clamp. The ports
had §3.1 and §3.4 on 2026-10-26 (§8); core-ts's setter guard takes the measured range. D6 (the per-attribute check:
`w:tblW`, `w:tcW`, `w:tblInd` int16, `w:pgNumType/@w:start` int32, the `ST_TwipsMeasure` attributes by their own width)
went to the CR-064 tree validator and the CR-050 inspector "when they exist"; §10 (2026-10-27) takes it to the inspector, which does.

## 10. Phase 3 addendum (2026-10-27): D6 goes to the inspector, as phase 4

Written at Jason's word ("a CR-066 phase 3 addendum please"), after the question of 2026-10-27, "should we do CR-064
tree validator or CR-050 inspector first?". The answer is neither in the order asked: the CR-050 inspector exists
(its phases 0 to 4 done and pushed by 2026-10-21, `org.docx4j.model.text.Inspect`), so D6 is a new kind in a walker that
already visits every part, not a validator to build first. CR-064's phase 2, the D8(b) validator, is optional, has one
table row of design, and its consumers (tests, the MCP server) would be served by the inspector's walk if it is ever
wanted; it stays optional and is not waited for (D9). CR-064 §5's phase 2 row is annotated to say so.

### 10.1 What the inspector has, read 2026-10-27 on VERSION_17_3_5 at 109a8d47b

- **Every part is reachable as a DOM.** `InspectPackage` indexes every part of the package (`pkg.getParts()`), and
  `dom(name)` marshals a JAXB part on demand (`InspectXml.marshal`: the part's own `JAXBContext`, so the document, the
  styles, settings, numbering, headers, footers, notes, comments, the glossary and an embedded docx one level deep are
  all one call away; a `word`-mode load's `mc:AlternateContent` records are put back, so unselected branches are
  walked too, CR-051). The DOM holds each integer attribute as docx4j holds it, written as held (D2), so it shows the
  value a save would write, which is what Word will misread.
- **A finding has the fields the ports asked for.** `InspectFinding`'s location is the package chain, the part name
  and an element path (§2.4 of CR-050), with an optional text range and extraction address; its `summary` is free
  text. python-cd's CR-020 §4.7 vocabulary for `value.out_of_range` (element, attribute, value, word_reads, storage,
  range, part, path) fits that shape: part and path are the location, the rest the summary.
- **A defect, by reading:** `InspectXml.marshal` opens no `LenientInteger` report scope, so the print methods of §9
  run outside one and log one WARN per out-of-range value for every part the inspector marshals (`reportOutOfRange`'s
  no-scope branch). An inspection of a document with such values therefore logs what the inspection should report.
  Fixed in phase 4 whatever else is decided (D14).
- **The oracle.** `support/inspect_oracle.py` is a second reading of the contract (lxml) over the same fixtures; it
  gets the same check from the same table (D11), so the exact-set comparison of `expected-inspection.tsv` covers the
  new kind as it covers the others.

### 10.2 The kind

`value.out_of_range` (the shared code of §9, `LenientInteger.CODE`; it already has the inspector's `family.name`
shape), proposed to the WD as a REQ-902 row:

| Kind | One finding | Visible | Reveal | Group |
|---|---|---|---|---|
| `value.out_of_range` | an integer attribute whose value is outside the width Word stores it in (§3.1, §3.4), at the element that carries it; one per attribute occurrence, never merged; every attribute of the measured types in every WordprocessingML part, unselected branches and an embedded docx one level deep included | - (not content a person reads: Word shows the wrapped or clamped measure, the page shows its effect) | unextractable (the number is not text; the WARN of §9 and this finding are its only report) | info |

The summary carries the shared vocabulary in the agreed order, one `key=value` per field:
`attribute=w:ind/@w:left value=7205001 storage=int16 range=-32768..32767 word_reads=-3959`. `word_reads` is given in
the attribute's own terms, a string, since §3.1 and §3.4 show that Word's reading is not always a number in the same
attribute: `hanging 3959` for `w:ind/@w:firstLine="7205001"` (one int16 field shared by the pair), `atLeast 3959` for
`w:spacing/@w:line="7205001"` under `exact`, `h=28809 hRule dropped` for `w:framePr/@w:h`, `exact 32768` for
`w:trHeight/@w:val="32768"`, `-31680` for a tab stop (wrap then clamp), `31681` for `w:pgSz/@w:w="32767"`,
`computed (9360 in the probe)` for a negative `w:col/@w:w`, `dropped` for `w:trHeight` under `auto` (D12). For an
attribute of a measured type that no round measured (`w:gridCol/@w:w`, `w:object`'s `dxaOrig`, the frame distances
round 6 did not reach), the finding gives the type's certain range (§3.1, "outside -32768..65535 is changed") and
`word_reads=changed` (D13), so the inspector is never less than the log. `ST_DecimalNumber` ids and counts, EMUs,
`w:fitText` (not 16-bit, §3.4) and `w:pgNumType/@w:start` within int32 are not findings. Where the element path
alone would not identify the attribute's rule, the summary also carries the deciding sibling: `type=dxa` or `pct`
for a `CT_TblWidth`, `lineRule=exact` for `w:spacing/@w:line`, `hRule=exact` for `w:trHeight` and `w:framePr`.

Location: the element's path in the part, no text range; the extraction address of the enclosing block where the
text model has one (a paragraph of the body), else none (a style, the settings, a section's `w:sectPr`). Order: with
the part's other findings, in document order (CR-050 §2.4). Membership: in every output, since it is not content, so
its group is `info` under every option and format and it moves REQ-903's `info` count only; `extract_text` over MCP
therefore shows it to an agent without an `inspect` call. Bounds: the 10,000-findings limit and the element budget
apply as to every kind; a document with more such values than that is `PARTIAL`, as any other is.

### 10.3 The table (D11)

One resource, `docx4j-core/src/main/resources/org/docx4j/model/text/word-integer-storage.tsv`, §3.1 and §3.4 row for
row: element (local name, with its parent where the attribute's owner matters: `w:pBdr/w:top`, `w:tcBorders/*`,
`w:cols/w:col`), attribute, the deciding sibling and its value where there is one (`w:type`, `w:lineRule`, `w:hRule`),
the storage (`int16`, `uint16`, `int32`, `8 bits`, `5 bits`, `15 bits + flag`, `int16 pair`, `int16 sign-rule`, `int16
clamp a..b`, `uint16 clamp a..b`, `uint16 min a`, `int16 computed-if-negative`, `int16 -1-if-negative`, `discarded`,
`kept`), the range, and the evidence column's probe id. The inspector reads it at class load (`InspectRanges`,
package-private beside `InspectWalk`); `inspect_oracle.py` reads the same file from the source tree, so the two
implementations cannot drift from each other; objects-ts's `WORD_INTEGER_STORAGE` (its CR-016) is the third copy, and
its `test/word365-ranges.mjs` is the test against Word's saves that this table inherits by construction (every row
here is a row there). A unit test (`WordIntegerStorageTest`) reads the resource and checks it against the probe
saves' expectations in `probe66/cases.txt` for the rows that have one, so the table is tied to Word's evidence, not to
§3.1's prose. The type-level `Range` of `LenientInteger` stays as it is: the marshal-time floor, in the generated
module, needs no table.

### 10.4 Tests, fixture and the WD

- **Fixture:** cases 75 on in `extraction-probe-inspect.docx` (`ExtractionInspectCases`, built by
  `ExtractionProbeBuilder`), one or two attributes each, the probe's values so Word's reading is already measured:
  75 `w:ind/@w:left="7205001"` on a body paragraph (int16, -3959) and `w:spacing/@w:before="1205003"` (uint16, 25355);
  76 `w:tcW/@w:w="70000"` `type=dxa` (int16, 4464) and `w:tblW` under `pct` 65536 (0); 77 `w:tab/@w:pos="32768"`
  (clamp, -31680); 78 `w:ind/@w:firstLine="7205001"` (hanging 3959) and `w:spacing/@w:line="7205001"` under `exact`
  (atLeast 3959); 79 `w:trHeight/@w:val="32768"` under `exact` (exact 32768) and a border `w:sz="256"` (0); 80 a style
  in `styles.xml` with `w:ind/@w:left="7205001"` and `w:settings/w:defaultTabStop="65536"` (uint16, 0); 81 the same
  value in the header the section uses and in an unselected `mc:Fallback`; 82 `w:pgNumType/@w:start="4294967295"`
  (int32, -1) on the final section and `w:gridCol/@w:w="70000"` (unmeasured: certain range, `changed`). No run is set
  to a huge `w:sz` (Word splits such a run per character, §3.1, which would move the fixture's other rows). Every value
  is one Word opened clean in rounds 5 and 6, so the fixture's Word-open check (Jason, `phase0/evidence/word-open-results.md`)
  is a confirmation, not a measurement; what it adds is the fixture's own save, read for the cases above.
- **Rows:** `expected-inspection.tsv` gains one row per attribute (about 14), `visible` `-`, `reveal` `unextractable`,
  every group column `info`, the summary as §10.2, from the oracle; `expected-findings.tsv` follows; INSPECTION.md gains
  the kind's identity bullet. The five existing fixtures are checked for values already outside a range (none expected;
  a hit is a row, not a fixture change).
- **Tests:** the exact-set comparison (`ExtractionConformanceTest`) over the fixture; `WordIntegerStorageTest`
  (§10.3); a test that inspecting the fixture logs nothing from `LenientInteger` (D14; a logback `AppenderBase` as
  `IntegerRangeReportTest` uses); a test that a document saved by docx4j from a `BigInteger` out of range is found by
  `Inspect.of` before the save and the same after a reload (held as written both ways, D2 and D3). MOXy: the DOM
  marshal runs the same adapters under MOXy, so `MoxyInspectTest` (or the existing MOXy inspection case) adds the
  fixture.
- **The WD** (the portfolio session owns it): REQ-902's row (§10.2); 9.4's identity sentence (one per attribute
  occurrence, never merged); REQ-904 unchanged (what is not measured is reported at the certain range, not left out);
  Appendix A.4's cases 75 to 82 and REQ-1201's rows; a NOTE pointing at §3.1 and §3.4 as the measurement and at the
  ports' shared code.
- **Cost:** one attribute scan per DOM the inspector already builds; the lookup is a map on the element's local name.
  Measured on the fixture and on `bench-30k.docx` with the bench of §9, recorded here.

### 10.5 Decisions

| # | Question | Options | Recommendation |
|---|---|---|---|
| D9 | D6's receiver | (a) the CR-050 inspector now; CR-064.2 not waited for and not needed for this; (b) build the CR-064.2 validator first and put the check in both; (c) a walker of its own in `wml` | (a). The inspector exists, has the location and the vocabulary the ports asked for, the oracle and the fixture recipe, and reaches MCP's `extract_text` counts; CR-064.2 is optional and undesigned, and would gain nothing D6 needs. |
| D10 | Identity and group | (a) one finding per attribute occurrence, group info, `visible` `-`, reveal unextractable; (b) one finding per part with a count, as the log line is; (c) group unseen (the page differs from the file) | (a). An agent that fixes the value needs the path; the limit of 10,000 findings bounds it. Not unseen: the kind is not text a person may miss, and REQ-903's unseen count is the one an agent acts on before reading. |
| D11 | Where the per-attribute table lives | (a) one TSV resource in docx4j-core, read by the inspector and by the oracle; (b) code in each (`InspectRanges` in Java, a dict in the oracle); (c) the oracle imports objects-ts's table | (a). Two readers of one file cannot drift; (b) is how the four CR-050 kind tables drifted before INSPECTION.md was written; (c) crosses repositories for a test resource. |
| D12 | `word_reads` for the structured attributes (the firstLine/hanging pair, exact and atLeast line, trHeight's sign, framePr h's flag, the clamps, a column's negatives) | (a) the exact reading in the attribute's terms, a string; (b) the modulo value and the storage word only, the rule left to §3.4 | (a). objects-ts encodes them already (CR-016's three structures); a reading an agent can act on is the point of the kind; the rules are measured, not inferred. |
| D13 | Attributes of a measured type that no round measured | (a) report at the type's certain range with `word_reads=changed`, as the log does; (b) leave out; (c) report at the type's commonest width as a guess | (a). The log already says so for the same value, and the inspector must not say less; (c) is a guess, which the register's rule forbids (§3.1's last paragraph names them unmeasured). |
| D14 | The inspector's marshal logs the WARNs of §9 | (a) `InspectXml.marshal` opens a report scope and discards the report (the finding is the report); (b) keep the log lines beside the findings | (a). One report per fact; a WARN from a read-only inspection reads as a defect in the document the user did not ask about. |

Accepted 2026-10-27, D9 to D14 as recommended (Jason: "commit, Decisions D9 to D14 as recommended"). Estimate: a day (the table and its test half of it); the Word-open check and the WD proposal are Jason's and the
portfolio's. Hand-offs after the build: docx4j-mcp (a new kind in `inspect` and in `extract_text`'s info count),
core-ts (CR-012), python (CR-011; its CR-022 table is the same rows), objects-ts (the TSV against its
`WORD_INTEGER_STORAGE`), the portfolio (the WD proposal; the registry: CR-066.3's D6 item closed, CR-066.4 added).

### 10.6 Phase 4 built (2026-10-27)

Built the day it was accepted (Claude Fable 5.1, session docx4j-12), D9 to D14 as recommended.

- **The table** (D11): `docx4j-core/src/main/resources/org/docx4j/model/text/word-integer-storage.tsv`, 118 rows (117 at 338beec18; `w:checkBox/w:size` added the same day),
  §3.1 and §3.4 row for row with the type-level rows for the uniform types the probe did not reach by name (a ruby's
  `hps*`, a `w:div`'s margins) and the `twips-unmeasured` rows (D13: `w:gridCol`, the drawing grid, `w:object`'s
  extent, a frameset's splitbar). Columns: element, attribute, context (`type=auto|nil`, `lineRule=exact`,
  `hRule=exact`; the first matching row wins; an empty context matches any), storage (seventeen words, documented
  in the file's header: the five widths, the firstLine/hanging pair, the four sign-as-rule forms, the 15-bit height
  with its flag, the three clamps, the two column negatives, unmeasured, ignored), range (what Word keeps as
  written), evidence. `ignored` rows record what is not a finding (`w:fitText`, `w:trHeight` under `auto`, a
  table width under `auto` or `nil`, `w:legacy`).
- **docx4j** (`org.docx4j.model.text`): `InspectRanges` reads the table at class load and gives, per attribute of
  an element, the finding's summary or null (`summary`, `reading`, `parse`: Word's lexical reading first, as
  CR-065 has it); `InspectWalk.walkRanges` runs at the end of the Word walk over the main part and then every
  other WordprocessingML XML part by name (a docx `altChunk` declares the main part's type and is a zip: its DOM is
  null and it is skipped; the nested walk of an embedded docx finds its own), one `value.out_of_range` finding per
  attribute occurrence, `needs` never, group info (`InspectFinding.INVENTORY`), `visible` `-`, the summary of §10.2
  (`; ` between the fields: `attribute=w:tcW/@w:w; type=dxa; value=70000; storage=int16; range=-32768..32767;
  word_reads=4464`; the deciding sibling is the key's, printed whenever the element has it, so a width under
  `dxa` names its type though its row is the catch-all), the finding's subject the part, attribute and value
  (the harness's selector, unique per case). **One rule §10.2 did not state:** a run's property in the run after
  one with the same finding is the same finding, as CR-050 §2.4 has it for every run kind, since splitting a run
  copies its `w:rPr`; `InspectTest.countsSurviveSplitRuns` caught it (info 37 against 38 after the split). A
  paragraph's, a mark's or a style's never merges. `InspectPackage.dom` opens a `LenientInteger` report scope
  around the inspector's marshal and discards it (D14). `Inspection.LIMITATIONS` gains `value.unmeasured`.
- **The oracle**: `inspect_oracle.py` reads the same TSV (`STORAGE_TABLE`, six `..` from `support/`), `word_reads`
  mirrors `InspectRanges.reading` form for form, `walk_ranges` the walk (the non-XML part guard, the run merge,
  the selector), `RANGE_PART_CASES` attributes a finding in `styles.xml`, `settings.xml` or the used header to its
  case. Fixture and docx4j agree on every fixture (`ExtractionConformanceTest`'s exact comparison).
- **Fixture**: cases 75 to 82 in `extraction-probe-inspect.docx` (`ExtractionInspectCases.ranges`), 17 findings:
  75 `before` 65537 and `left` 66256; 76 `tblW` 65536 pct, `gridCol` 70000 (unmeasured), `tcW` 70000 dxa; 77 a tab at
  32768; 78 `line` 65535 exact and `firstLine` 65535; 79 `trHeight` 65537 exact (the table's `tblW` under `auto`
  unchecked), a border `sz` 256 and `space` 32, `w:w` 65636; 80 a style's `left` 66256 and `hyphenationZone` 65537;
  81 `right` 66976 in the used header and a run's `spacing` 65537 in an unselected Fallback; 82 `pgNumType/@start`
  4294967295 on the body's section. Departures from §10.4: `w:gridCol` sits in case 76 with the table, not 82;
  `hyphenationZone` stands in for `defaultTabStop` (a default tab stop of 0 would move the fixture's other
  cases); every value wraps to a small number so the page stays readable (the big values were measured in rounds
  5 and 6). `expected-inspection.tsv` 242 findings (225 before), `expected-findings.tsv` 17 inspect rows,
  `case-statements.tsv` and `additional-case-statements.tsv` cases 75 to 82 (REQ-902, REQ-904; 81 also REQ-302,
  REQ-502), the fixture's README regenerated. Side effects in the table: the used header's `header_footer` excerpt
  is 41 code points (28), and the header picture's `alt_text.description` row is attributed to case 82 (74 before),
  both coincidences of the sectPr's place; no other row moved.
- **Word's check**: staged for Jason on the share at `__tmp/cr066/phase4/extraction-probe-inspect-r5.docx` (sha256
  d5b6ee0c…) with `README.txt` (what each case should look like); recorded in
  `phase0/evidence/word-open-results.md` when done.
- **Tests**: `WordIntegerStorageTest` (177 written-to-saved pairs from §3.1 and §3.4, the summary's field order,
  every row parses and every storage word reads); `InspectRangesTest` (eleven findings in one body with their
  paths and readings, group info in plain text and the block model, status complete; the run merge; no line
  logged by `LenientInteger` during an inspection, D14; held as written through a save and a reload; the styles
  and settings parts after the main part); `InspectTest.limitationsAreStated` gains `value.unmeasured`;
  `ExtractionConformanceTest` 82 cases. Targeted: textextraction, model.text, `IntegerRangeReportTest`,
  `LenientIntegerTest`: 172 tests, 0 failures, 19 skipped (the HTML-reader rows). Full gate 2026-10-27 (`mvn clean install`): BUILD SUCCESS; docx4j-core-tests 2176 tests (2168 before plus the 8 new), 0 failures, 30 skipped; docx4j-diffx-tests 246; docx4j-export-fo-tests 858; docx4j-digsig 138.
- **Cost**: the attribute scan of the main part is 0.03 ms on `extraction-probe-inspect.docx` and
  `extraction-probe.docx` (best of 50; `Inspect.of` 50 and 71 ms best of 12) and 0.01 ms on the 1 MB
  `jaxb-binder-issue.docx`: nothing measurable.
- **Hand-offs** (2026-10-27, after the commit): python (session docx4j-python-64: the kind, the summary's fields, the TSV its CR-022 table can be checked against, the run-merge rule), core-ts (docx4j-core-ts-7b: the same for its CR-012), the portfolio (docx4j-portfolio-10: the WD proposal, REQ-902's row, 9.4's identity sentence, Appendix A.4's cases 75 to 82, REQ-1201's rows; the registry: CR-066.4 done). objects-ts (the TSV against `WORD_INTEGER_STORAGE`) and docx4j-mcp (the new kind in `inspect` and in `extract_text`'s info count) had no session running at the commit; sent later the same day to docx4j-generated-objects-ts-08 (the row-for-row diff, setter-side narrowings to name, the storage tokens) and docx4j-mcp-63 (the kind through `inspect`, `value.unmeasured` in any enumerated limitations, `extract_text`'s info count against pinned goldens). **Acknowledged** 2026-10-27 by docx4j-mcp-63: no code change; `inspect` is not yet implemented there (its CR §19 phase 4, on 17.1.0 today), so nothing enumerates kinds, limitations or info counts; its CR §19.3 now says the kind inventory is docx4j's to grow, the tool passes `kind` and `limitations` through as strings with INSPECTION.md the authority, and the copied inspection tables, not hand-written counts, are its goldens when that phase lands. **Acknowledged** 2026-10-27 by objects-ts-08: `WORD_INTEGER_STORAGE` diffed row for row against the TSV at 338beec18 (its 26 classes and 51 properties mapped onto the rows, the kept-as-written range computed from its `wordIntegerReading` over -70000..70000 and the int32 edges): no storage or range differs where both measured. Differences of claim, its by type and the TSV's by evidence, no action: a `CT_TblWidth` under `type=auto|nil` (the TSV ignores, its reports an int16 overflow whatever `type` says, an over-report it may contextualise later); `w:fitText/@w:val` (the TSV ignores, its int32 from round 6 R1, a report only beyond 2^31 which nobody measured); `w:legacy` (the TSV ignores both, Word drops the element; its has `legacyIndent` int16 by type, `legacySpace` unmeasured: Jason's call whether it moves to ignored for parity); `CT_TwipsMeasure` (its one uint16 entry for all seven elements, the TSV uint16 for the two measured and `twips-unmeasured` for the five others, so its also reports a negative and a value above 65535 there). Coverage: `w:checkBox/w:size/@w:val` (`CT_HpsMeasure`, a legacy form field) was missing from the TSV: added the same day as a type-level row, uint16, not measured, as the other `CT_HpsMeasure` rows; **118 rows** now. Read-side leniency recorded: it keeps -1 as written on the uint16 attributes Word writes 0xFFFF as -1 (before, after, the unsigned margins), a round-trip rule, where the TSV's 0..65535 is the stricter statement; setter rules (its CR-016 G4): widths 0..32767, run size 0..65535 half-points. Its question on the two `-drop` rows (`w:trHeight` with no `hRule`, 0..32767, and the `rule-negated` line row, 1..32768): the token describes a stored zero reached by a wrap (round 5 T6's 65536 dropped); a literal 0 is within the range and never read, and whether Word also drops a literal 0 is unmeasured (added to §3.1's list); the header comment now says so, no range changes. Tokens: it keeps its three structures and maps ("keep and map"); recorded in objects CR-016 §8, uncommitted, with its 0.6.1 candidate. Word's check of the fixture: Jason's, pending (the share). **Acknowledged** 2026-10-27 by python-64: its CR-022 table (integer-limits.json, 60 rows, 82 attribute keys) checked key by key against the TSV at 338beec18, no range differs where both measured; its deliberate write-side narrowings (before, after and the unsigned margins stop at 65534 since Word writes 0xFFFF as -1; col space 0..32767 with -1 the sentinel) are setter rules, and its CR-020 phase B read-side report takes this table's reading; the context layout differs (its explicit rows are this table's empty-context fallbacks), the storage tokens will be adopted there; field names as agreed, no change on its side. The portfolio (78dc02f: CR-066.4 done) drafted WD 0.35 the same day for Jason's ruling: the five items, with REQ-904 also gaining `value.unmeasured` (REQ-1204 compares an inspector's limitations with REQ-904's identifiers, so a stated limitation must be there; confirmed) and `value.out_of_range` in REQ-905's info list.
