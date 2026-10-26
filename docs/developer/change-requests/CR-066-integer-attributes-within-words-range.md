# CR-066: integer attributes written within Word's range; an out-of-range value reported on write and on read

Status: ACCEPTED 2026-10-26 (Jason: "CR-066 decisions as recommended"): D1 to D8 as recommended in §4, D1, D4 and D7 as
amended by §3.1 (the width is per attribute; the type-level report is exact for the uniform types and the certain range for
`ST_TwipsMeasure`; `ST_DecimalNumber` by attribute through D6 or a setter patch). Phase 0 DONE; phases 1 and 2 ready (CR-065
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
`before`), the other borders' attributes (assumed as `w:top`'s).

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
| D6 | A whole-tree check ("find every out-of-range value in this package") | (a) in the CR-064 D8(b) tree validator and the CR-050 inspector when they exist; (b) a new walker here | (a), later; D1's report is the floor every save gets. |
| D7 | Types covered | the integer simple types of CR-065 D4 that phase 0 shows are 16-bit; `ST_DecimalNumber` attributes individually (`CT_TblWidth/@w:w` by its `w:type`; `w:pgNumType/@w:start` as phase 0 finds); EMUs and counts excluded | **Amended by §3.1:** the range table is per attribute (§3.1's table); the type-level report carries the exact range for `ST_SignedTwipsMeasure`, `ST_SignedHpsMeasure`, `ST_TextScale`, `CT_HpsMeasure/@w:val`, `ST_EighthPointMeasure` and `ST_PointMeasure`, the certain range (outside -32768..65535) for `ST_TwipsMeasure`, nothing for `ST_DecimalNumber`; `w:tblW`, `w:tcW`, `w:tblInd` (int16) and `w:pgNumType/@w:start` (int32) by attribute (D6 or a setter patch). |
| D8 | Report once per document | the same question as CR-065 D6: a static method has no document context | the same mechanism (a thread-local counter per load or save, or per value); decided in CR-065 phase 2 and reused. |

## 5. Phases

| Phase | Work | Status |
|---|---|---|
| 0 | Round 5 of the Word probe (§3); the range per attribute recorded in §3.1 and sent to python-cd, objects-ts and core-ts | DONE 2026-10-26 (Jason's run; §3.1) |
| 1 | D1 to D4, D7, D8: the range table (§3.1) and the report in the print and parse methods of CR-065 phase 2's class, exact for the uniform types and the certain range for `ST_TwipsMeasure`; tests per type with the probe's values; CHANGELOG | after CR-065 phase 2 |
| 2 | D5: the guard at `TblFactory` (int16 for `w:tcW`, `w:gridCol` assumed uint16), the WARN at the CSS converters and `TocEntry` (tab stops ±31680); tests; CHANGELOG | ready |
| 3 | Hand-offs: §3.1's table to the three ports and to ImportXHTML (its CSS length conversion); D6, the per-attribute check, to the CR-064 validator and the inspector when they exist | ports told 2026-10-26; the rest ready |

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
matches; `w:trHeight/@w:val` is the attribute whose storage sign differs from its schema type. objects-ts's CR-015 and core-ts
take the measured range when it exists (phase 3).
