# `word/settings.xml` and the layout rules

Which document-level settings the rules in
[word-layout-rules.md](word-layout-rules.md) are sensitive to, and which of them docx4j
reads. An audit, not a set of rules: nothing here has been measured against a Word
golden unless the rules doc says so. It is the map for deciding what to probe next.

Describes docx4j 17.0.6.

---

## 1. What settings.xml governs, and the policy

`word/settings.xml` (`CT_Settings`, ECMA-376-1 §17.15.1) carries the document-wide
switches Word's layout engine reads before it lays out a single line: the default tab
interval, whether hyphenation is on, whether margins mirror, where the gutter goes - and,
in `w:compat`, some fifty flags each of which turns one Word-version-specific layout
behaviour on or off.

**The mode is a bundle, not a switch.** Every `w:compat` child is a `CT_OnOff`, and by
the schema an absent element is *off*. That is not how Word behaves. Word's layout engine
version is recorded separately, in
`w:compat/w:compatSetting[@w:name="compatibilityMode"]/@w:val` (11 = Word 2003,
12 = Word 2007, 14 = Word 2010, 15 = Word 2013 and later; a Microsoft extension in the
`http://schemas.microsoft.com/office/word` namespace, not in ECMA-376), and the engine
that mode names applies its own era's rules whether or not the corresponding flag is
written. A Word 2007 file writes seven of these flags explicitly and gets a dozen more
behaviours from the engine; a Word 2013 file writes none of them and gets the modern
behaviour for all of them.

**The policy, implemented in 17.0.6**: *a rule keys on the flag; the mode supplies the
flag's default where the document does not state it.* So `growAutofit` is read from
`w:compat/w:growAutofit` where it is there, and takes its mode's default where it is not -
one lookup, one place, and a mode-12 document that happens to have been resaved without its
default block still behaves like a mode-12 document.

**But what the mode supplies turned out to be narrower than §2 suggests**, and the batch's
main finding is narrower still: §2's table says what Word *writes* at each mode, and
measured, **Word 365 does not honour the legacy `w:compat` flags at all** - neither when the
mode implies them nor when the document states them (§4(e)). So every legacy flag defaults
**off**, and the three rules keyed on one that a real document does state were measured and
taken out again. The two flags whose defaults are not off (`splitPgBreakAndParaMark`, on
everywhere; `suppressSpBfAfterPgBrk`, on from mode 15) have defaults that are themselves
measurements of Word 365's behaviour.

`org.docx4j.model.CompatibilityOptions` is that one place (§5). Fourteen flags resolve
through it and **five rules read it** - `doNotExpandShiftReturn`,
`splitPgBreakAndParaMark`, `suppressSpBfAfterPgBrk`, `noTabHangInd` and
`allowSpaceOfSameStyleInTable`. Four more were wired, measured and taken out:
`doNotUseIndentAsNumberingTabStop` (§4(d)), and `growAutofit`,
`useWord2002TableStyleRules` and `forgetLastTabAlignment` (§4(e)). The rest of this
document is the audit those choices were made from, and is unchanged except for the status
column.

What is left keys on the **mode** alone - the table grid edge and the content-autofit grid
allowance (§6.1 of the rules doc) and justified space compression (§4.2), none of which has
a `w:compat` flag at all - or reads a **named setting** (`w:defaultTabStop`, the four
hyphenation settings, `w:evenAndOddHeaders`, `w:mirrorMargins`, `w:gutterAtTop`,
`w:decimalSymbol`, `w:themeFontLang`, `w:doNotUseHTMLParagraphAutoSpacing` and the
`overrideTableStyleFontSizeAndJustification` compat setting) - or is unread.

**Citations.** ECMA-376-1 numbers each `w:settings` child under §17.15.1 alphabetically,
and the numbering shifts between editions as elements were added, so only the four numbers
this repository already records (`w:autoHyphenation` 17.15.1.10, `w:consecutiveHyphenLimit`
17.15.1.22, `w:doNotHyphenateCaps` 17.15.1.37, `w:hyphenationZone` 17.15.1.44, in
`org.docx4j.model.HyphenationSettings`) are given as numbers. Everything else is cited as
"§17.15.1, `w:name`" with the spec's own title for the element, which is what the schema
annotation in `xsd/wml/wml.xsd` carries. Where a claim is inference rather than spec text
or measurement it says **assumed**.

**Corpus counts** are over the 454-document corpus (`w:compat` present in 65 of them;
modes 15:169, 14:124, 12:32, 11:21). They say how often a setting is *stated*, not how
often it matters - and some of them are short: `doNotUseIndentAsNumberingTabStop` is counted
0 below and nine documents of the same corpora turned out to state it (§4(d)),
`allowSpaceOfSameStyleInTable` is counted 0 against 32, and `splitPgBreakAndParaMark` is
counted 0 although every producer-written compat block carries it. A zero here means "not
found by the sweep that built this table", not "certainly absent".

---

## 2. The compatibility-mode bundle

What Word writes at each mode, and what the engine applies when the flag is absent. Only
the **mode 12** column is corroborated here: the corpus's 32 mode-12 files all carry the
Word-2007 default block explicitly, and its members are the seven marked `written` below.
The mode 11 column is the shape of the corpus's 21 mode-11 files (the flag counts in the
table match a Word-2003-era block); the 14 and 15 columns are **assumed** from the fact
that Word 2010 and later write no legacy flags at all and from each flag's own "Emulate
Word N" title.

`written` = Word saves the element; `off` = absent and not applied; `on` = absent but the
engine applies it anyway (assumed throughout).

**The `on` column is what 17.0.6 measured and did not find**, and neither is the `written`
column's implication. Three mode-11 corpus documents carry a full Word-2003 compat block -
one of them without `useWord2002TableStyleRules`, two with it - and Word 365's own PDFs of
all three show the **modern** behaviour (§4(e)). So the era's flags are not applied to a
document which omits them, and are not applied to a document which states them either.
docx4j therefore defaults every legacy flag off, and the rules which read one are only the
ones no such document contradicts; this table stays as the record of what Word writes.

| Flag | 11 | 12 | 14 | 15 | Basis |
| --- | --- | --- | --- | --- | --- |
| `spaceForUL` | written | written | off | off | corpus (23) |
| `balanceSingleByteDoubleByteWidth` | written | written | off | off | corpus (14) |
| `doNotLeaveBackslashAlone` | written | written | off | off | corpus (23) |
| `ulTrailSpace` | written | written | off | off | corpus (39) |
| `doNotExpandShiftReturn` | written | written | off | off | corpus (21) |
| `adjustLineHeightInTable` | written | written | off | off | corpus (22) |
| `useFELayout` | written | written | off | off | corpus (50) |
| `useWord2002TableStyleRules` | written | - | off | off | corpus (14), assumed for 12 |
| `growAutofit` | written | - | off | off | corpus (15), assumed for 12 |
| `underlineTabInNumList` | written | - | off | off | corpus (12) |
| `doNotSnapToGridInCell` | written | - | off | off | corpus (9) |
| `doNotWrapTextWithPunct` | written | - | off | off | corpus (9) |
| `doNotUseEastAsianBreakRules` | written | - | off | off | corpus (9) |
| `doNotBreakWrappedTables` | written | - | off | off | corpus (8) |
| `selectFldWithFirstOrLastChar` | written | - | off | off | corpus (8) |
| `footnoteLayoutLikeWW8` | written | - | off | off | corpus (5) |
| `shapeLayoutLikeWW8` | written | - | off | off | corpus (5) |
| `alignTablesRowByRow` | written | - | off | off | corpus (5) |
| `forgetLastTabAlignment` | written | - | off | off | corpus (5) |
| `layoutRawTableWidth` | written | - | off | off | corpus (5) |
| `layoutTableRowsApart` | written | - | off | off | corpus (5) |
| `useWord97LineBreakRules` | written | - | off | off | corpus (5) |
| every other `w:compat` child | off | off | off | off | assumed |

Three rules docx4j already keys on the mode have **no corresponding `w:compat` flag at
all**, so the policy above cannot be applied to them and the mode stays their only key:
justified space compression (§4.2), the table grid edge and the content-autofit grid
allowance (§6.1, [§6.1](word-layout-rules.md#s61autofit)). The fourth,
space-before after a hard page break (§3.3), does have one - `suppressSpBfAfterPgBrk` -
and was re-keyed to it in 17.0.6.

**Two flags' defaults are not the era pattern.** `suppressSpBfAfterPgBrk` is applied *from*
mode 15 and not below it, which is the polarity measured against Word's goldens (§3.3 of
the rules doc), and `splitPgBreakAndParaMark` is applied in every mode, which is what Word
365 was measured doing for a mode-12 and a mode-15 golden alike. Neither is stated by any
corpus document, so which way Word reads a stated one is not established; the defaults
above are the measurements, and `CompatibilityOptions` records both as assumed.

The `w:compatSetting` names are the modern half of the same mechanism, and Word 2013 and
later write four of them on every save: `compatibilityMode`,
`overrideTableStyleFontSizeAndJustification`, `enableOpenTypeFeatures` and
`doNotFlipMirrorIndents` (corpus 293 / 291 / 291, against 293 documents at mode 14 or 15),
plus `differentiateMultirowTableHeaders` (169, exactly the mode-15 count).

---

## 3. The settings, one row each

Status:

* **HONOURED** - a rule reads the setting; the class that reads it is named.
* **MODE-DEFAULTED** - a rule implements the behaviour but keys on `compatibilityMode`;
  the flag itself is not read.
* **IGNORED** - a rule implements the behaviour one way, the flag would change it, and it
  is not read.
* **NO RULE** - docx4j does not implement the behaviour either way, so the flag is moot.
* **N/A** - not a layout setting (an authoring, printing, proofing or UI switch).

### 3.1 `w:compat` children (ECMA-376-1 §17.15.1)

| Setting | What it changes in Word | Rules-doc sections | Status | Corpus | Probe |
| --- | --- | --- | --- | --- | --- |
| `adjustLineHeightInTable` | "Add Document Grid Line Pitch To Lines in Table Cells" | §2.1, §2.2, [§3.5](word-layout-rules.md#s35) | NO RULE - `w:sectPr/w:docGrid` is not implemented at all | 22 | yes, but needs a document grid first |
| `alignTablesRowByRow` | "Align Table Rows Independently" (each row wraps floating objects on its own) | §6.7, [§3.3rowtop](word-layout-rules.md#s33rowtop) | IGNORED - and a candidate discriminator for the open row-levelling question | 5 | yes: a two-cell row whose cells carry different `w:before`, with and without |
| `allowSpaceOfSameStyleInTable` | "Allow Contextual Spacing of Paragraphs in Tables" | [§3.5](word-layout-rules.md#s35) (the "only paragraph of a cell" rule) | **IGNORED** (17.0.6) - resolved by `CompatibilityOptions` but not applied: the compat-breaks probe pair, identical but for the flag, renders identically in Word 365 (row pitch 13.7pt either way) | 32 stated (the count of 0 was a miscount) | `compat-breaks` (no golden) |
| `applyBreakingRules` | Legacy Ethiopic/Amharic line breaking | §4.1, §4.3 | NO RULE | 0 | no (out of scope) |
| `autoSpaceLikeWord95` | Word 95 full-width character spacing | §4.6 | NO RULE | 0 | no (CJK) |
| `autofitToFirstFixedWidthCell` | "Allow Table Columns To Exceed Preferred Widths of Constituent Cells" | [§6.3grid](word-layout-rules.md#s63grid), §6.4 | IGNORED - the grid-outranks-`w:tcW` rule is unconditional. **Deferred in 17.0.6**: [§6.3grid](word-layout-rules.md#s63grid) measured that rule as Word 365's behaviour on documents which do **not** state the flag, i.e. as the flag's own default, so its polarity cannot be settled without a golden that states it | 0 | `compat-tables` carries the shape |
| `balanceSingleByteDoubleByteWidth` | Balance single- and double-byte character widths | §4.6, §5.7 | NO RULE | 14 | no (CJK) |
| `cachedColBalance` | "Use Cached Paragraph Information for Column Balancing" | [§7](word-layout-rules.md#s73) (`Balance.java`) | IGNORED | 0 | low |
| `convMailMergeEsc` | Backslash quotation delimiter handling | none | N/A | 0 | no |
| `displayHangulFixedWidth` | Fixed width for Hangul | §4.6 | NO RULE | 0 | no (CJK) |
| `doNotAutofitConstrainedTables` | "Do Not AutoFit Tables To Fit Next To Wrapped Objects" | §6.3, [§6.8](word-layout-rules.md#s69) | IGNORED - **deferred in 17.0.6**: docx4j does not narrow a table beside a wrapped object at all, so there is no rule for the flag to switch | 0 | low |
| `doNotBreakConstrainedForcedTable` | "Don't Break Table Rows Around Floating Tables" | [§6.8](word-layout-rules.md#s69) | IGNORED | 0 | low |
| `doNotBreakWrappedTables` | "Do Not Allow Floating Tables To Break Across Pages" | [§6.8](word-layout-rules.md#s69) | IGNORED - neither the float nor the positioned container carries a keep. **Deferred in 17.0.6**: §6.8's rules have no notion of a floating table breaking at all, so there is nothing to switch | 8 | yes: a tall `w:tblpPr` table straddling a page boundary |
| `doNotExpandShiftReturn` | "Don't Justify Lines Ending in Soft Line Break" | [§4.2](word-layout-rules.md#s42shiftreturn), [§2.5brline](word-layout-rules.md#s25brline), §4.4 | **HONOURED** (17.0.6) - `WordLayoutFixups.justifySoftReturns` marks the block and `WordLineLayoutManager` justifies the line the soft return ends; off in every mode, so the 21 documents which state it keep 17.0.5's behaviour and every other document gains the rule. **36 documents of the three corpora carry the rule; 49 lines move, 46 of them closer to Word and none further, 2,878pt of x recovered**, at unchanged line parity (justifying a line does not move where it breaks) | 21 | `compat-shift-return` (no golden) |
| `doNotLeaveBackslashAlone` | Convert backslash to yen sign | none | N/A | 23 | no |
| `doNotSnapToGridInCell` | Do not snap to the document grid in cells with objects | §2.1 | NO RULE (no document grid) | 9 | no |
| `doNotSuppressIndentation` | "Do Not Ignore Floating Objects When Calculating Paragraph Indentation" | [§9.1](word-layout-rules.md#s91nofloat), [§9.5](word-layout-rules.md#s95) | IGNORED | 0 | low |
| `doNotSuppressParagraphBorders` | "Do Not Suppress Paragraph Borders Next To Frames" | [§3pbdr](word-layout-rules.md#s3pbdr), [§9.5](word-layout-rules.md#s95) | IGNORED | 0 | low |
| `doNotUseEastAsianBreakRules` | Do not compress compressible characters on a grid | §4.1 | NO RULE | 9 | no (CJK) |
| `doNotUseHTMLParagraphAutoSpacing` | "Use Fixed Paragraph Spacing for HTML Auto Setting" | §3.3 ("HTML auto spacing"), [§3.5autospacecell](word-layout-rules.md#s35autospacecell) | **HONOURED** - `DocumentSettingsPart.isDoNotUseHTMLParagraphAutoSpacing`, applied in `org.docx4j.model.properties.PropertyFactory`. The only `w:compat` flag docx4j reads | 8 | done |
| `doNotUseIndentAsNumberingTabStop` | "Ignore Hanging Indent When Creating Tab Stop After Numbering" | [§2.8ind](word-layout-rules.md#s28ind), [§2.8wide](word-layout-rules.md#s28wide), §4.4 (the implicit stop a hanging indent makes) | IGNORED, **measured and rejected in 17.0.6** - see §4(d) | 8 stated (the corpus count of 0 was a miscount) | `compat-numbering-tab` (no golden) |
| `doNotVertAlignCellWithSp` | "Don't Vertically Align Cells Containing Floating Objects" | §6.7, [§9.1nofloat](word-layout-rules.md#s91nofloat) | IGNORED | 0 | low |
| `doNotVertAlignInTxbx` | "Ignore Vertical Alignment in Textboxes" | §9.2 | NO RULE - a box's `wps:bodyPr/@anchor` is not applied either way | 0 | needs the vertical anchoring first |
| `doNotWrapTextWithPunct` | Hanging punctuation on a character grid | §4.1 | NO RULE | 9 | no (CJK) |
| `footnoteLayoutLikeWW8` | "Emulate Word 6.x/95/97 Footnote Placement" | §8, [§3.10](word-layout-rules.md#s310) | IGNORED | 5 | medium: a footnote near a page bottom, both ways |
| `forgetLastTabAlignment` | "Ignore Width of Last Tab Stop When Aligning Paragraph If It Is Not Left Aligned" | [§4.4jc](word-layout-rules.md#s44jc), [§4.4jcboth](word-layout-rules.md#s44jcboth) | IGNORED, **measured and rejected in 17.0.6** (§4(e)): the one corpus document which states it fell when §4.4jc was switched off for it. It remains the flag §4.4jc's rule would answer to | 5 | `compat-breaks` (no golden) |
| `growAutofit` | "Allow Tables to AutoFit Into Page Margins" | §6.3, [§6.5](word-layout-rules.md#s65pct), `GRID_OVERHANG_LIMIT` | IGNORED, **measured and rejected in 17.0.6** (§4(e)): the two corpus documents which state it are laid out by Word 365 with their autofit grids fitted to the column anyway. Still the candidate for §6.5's unresolved over-wide-grid disagreement | 15 | `compat-tables` (no golden) |
| `layoutRawTableWidth` | "Ignore Space Before Table When Deciding If Table Should Wrap Floating Object" | [§6.8](word-layout-rules.md#s69) | IGNORED - **deferred in 17.0.6**: the decision it modifies (whether a table wraps a floating object) is not one docx4j makes | 5 | low |
| `layoutTableRowsApart` | "Allow Table Rows to Wrap Inline Objects Independently" | §6.7, [§6.8](word-layout-rules.md#s69) | IGNORED | 5 | low |
| `lineWrapLikeWord6` | Word 6.0 East Asian line wrapping | §4.1 | NO RULE | 0 | no (CJK) |
| `mwSmallCaps` | Word 5.x Mac small caps | [§5.7smallcapsline](word-layout-rules.md#s57smallcapsline) | IGNORED - the 80% scale is unconditional | 0 | low |
| `noColumnBalance` | "Do Not Balance Text Columns within a Section" | [§7](word-layout-rules.md#s73), [§7equal](word-layout-rules.md#s73equal) | IGNORED - `Balance.java` always balances | 0 | yes: a short two-column section, both ways |
| `noExtraLineSpacing` | "Do Not Center Content on Lines With Exact Line Height" | §2.3 (the `exact` baseline at `usWinAscent/(asc+desc)` of the box) | IGNORED | 0 | yes: `line-exact-atleast` with the flag |
| `noLeading` | "Do Not Add Leading Between Lines of Text" | §2.1, §2.3, [§2.7](word-layout-rules.md#s27alias) | IGNORED - `WordLineMetrics` always adds `tmExternalLeading`. **Deferred in 17.0.6**: `WordLineMetrics` is a static, document-independent font-metric service read by the block writer, by each run's span, by the line manager and by the table autofit measurer, so a per-document flag would have to be threaded through all four; no corpus document states the flag | 0 | **yes**: `line-auto`'s four fonts with the flag |
| `noSpaceRaiseLower` | "Do Not Increase Line Height for Raised/Lowered Text" | §2.4 ("a raised or lowered run counts its full height"), §2.6 | IGNORED - and the FO root's `line-height-shift-adjustment="disregard-shifts"` is nearer the flag-on behaviour than §2.4's rule is | 0 | yes: a raised run in a prose paragraph, both ways |
| `noTabHangInd` | "Do Not Create Custom Tab Stop for Hanging Indent" | §4.4 (the implicit stop at the left indent), [§2.8ind](word-layout-rules.md#s28ind) | **HONOURED** (17.0.6) - `docx4j:no-tab-hang-ind` on `fo:root`, read by `WordLineLayoutManager.nextTabStop`; off in every mode | 0 | `compat-numbering-tab` (no golden) |
| `printBodyTextBeforeHeader` | Paint order of body against header | §7 | N/A (paint order, invisible unless they overlap) | 0 | no |
| `printColBlack` | Print colours as black and white | none | N/A | 0 | no |
| `selectFldWithFirstOrLastChar` | Field selection behaviour in the UI | none | N/A | 8 | no |
| `shapeLayoutLikeWW8` | "Emulate Word 97 Text Wrapping Around Floating Objects" | [§9.1](word-layout-rules.md#s91nofloat), [§6.8](word-layout-rules.md#s69) | IGNORED | 5 | medium |
| `spaceForUL` | Extra space below the baseline for underlined East Asian text | §2.1, §2.3 | NO RULE | 23 | no (CJK) |
| `spacingInWholePoints` | "Only Expand/Condense Text By Whole Points" | §4.6, [§4.6w](word-layout-rules.md#s46w) | IGNORED - the letter space is a fraction of a point | 0 | yes: `spacing-char` with the flag |
| `splitPgBreakAndParaMark` | "Always Move Paragraph Mark to Page after a Page Break" | [§3.3](word-layout-rules.md#s33), [§3.3each](word-layout-rules.md#s33each), [§3.3sect](word-layout-rules.md#s33sect) | **HONOURED** (17.0.6) - `convert/out/common/preprocess/PageBreak`; **on in every mode**, which is what Word 365 was measured doing, so nothing changes unless a document states `w:val="0"`. Still the most likely explanation of §3.3each's one unreconciled corpus document | 0 | `compat-breaks` (no golden) |
| `subFontBySize` | "Increase Priority Of Font Size During Font Substitution" | §5.1, §5.2 | IGNORED | 0 | low |
| `suppressBottomSpacing` | "Ignore Exact Line Height for Last Line on Page" | §2.3 (leading dropped at a page bottom) | IGNORED | 0 | yes: an `exact` line rule at a page bottom |
| `suppressSpBfAfterPgBrk` | "Do Not Use Space Before On First Line After a Page Break" | [§3.3](word-layout-rules.md#s33) | **HONOURED** (17.0.6) - `WordLayoutFixups.mergePageBreakParagraphs` reads the flag; its default is the measured polarity, on from mode 15 and off below it, so nothing changes for a document which does not state it. The polarity of a *stated* flag is still unmeasured | 0 | `compat-breaks` (no golden) |
| `suppressSpacingAtTopOfPage` | "Ignore Minimum Line Height for First Line on Page" | §2.3 | IGNORED | 0 | with `suppressTopSpacing` |
| `suppressTopSpacing` | "Ignore Minimum and Exact Line Height for First Line on Page" | §2.3 ("a page's first line starts with its ascent") | IGNORED | 0 | yes: an `atLeast` line rule at a page top |
| `suppressTopSpacingWP` | WordPerfect 5.x line spacing | §2.3 | NO RULE | 0 | no (legacy) |
| `swapBordersFacingPages` | "Swap Paragraph Borders on Odd Numbered Pages" | [§3pbdr](word-layout-rules.md#s3pbdr), §7 (`w:mirrorMargins`) | IGNORED | 0 | low |
| `truncateFontHeightsLikeWP6` | WordPerfect 6.x font height calculation | §2.1 | NO RULE | 0 | no (legacy) |
| `ulTrailSpace` | "Underline All Trailing Spaces" | [§4.5](word-layout-rules.md#s45lead) | NO RULE - ink, not geometry | 39 | cheap, low value |
| `underlineTabInNumList` | Underline the character following the numbering | §2.8, §4.4 | NO RULE - ink, not geometry | 12 | cheap, low value |
| `useAltKinsokuLineBreakRules` | Alternate East Asian line breaking | §4.1 | NO RULE | 0 | no (CJK) |
| `useAnsiKerningPairs` | "Use ANSI Kerning Pairs from Fonts" | §5.4 | IGNORED - `RunFontSelector.isKerned` reads `w:kern` alone | 0 | low |
| `useFELayout` | "Do Not Bypass East Asian/Complex Script Layout Code" | §5.7, §4.1 | NO RULE | 50 | no (CJK; and its count is mostly "this is a mode-12 file") |
| `useNormalStyleForList` | Which style Word applies when you click the list button | none | N/A (authoring) | 0 | no |
| `useSingleBorderforContiguousCells` | "Use Simplified Rules For Table Border Conflicts" | [§6.5borders](word-layout-rules.md#s65borders) | IGNORED | 0 | low |
| `useWord2002TableStyleRules` | "Emulate Word 2002 Table Style Rules" | [§7hftblstyle](word-layout-rules.md#s7hftblstyle), §6.3 (`ParagraphStylesInTableFix`) | IGNORED, **measured and rejected in 17.0.6** (§4(e)): the three corpus documents which state it have Word 365 applying the table style's properties to their paragraphs anyway | 14 | `compat-tables` (no golden) |
| `useWord97LineBreakRules` | Word 97 East Asian line breaking | §4.1 | NO RULE | 5 | no (CJK) |
| `wpJustification` | WordPerfect 6.x justification | §4.2 | NO RULE | 0 | no (legacy) |
| `wpSpaceWidth` | WordPerfect space width | §4.5 | NO RULE | 0 | no (legacy) |
| `wrapTrailSpaces` | "Line Wrap Trailing Spaces" | §4.1, [§4.5](word-layout-rules.md#s45lead) | IGNORED - a trailing space always hangs past the line end | 0 | yes, cheap: a line ending in several spaces, both ways |
| `showBreaksInFrames` | "Display Page/Column Breaks Present in Frames" | [§9.5](word-layout-rules.md#s95), §9.2 (breaks dropped inside a positioned container) | IGNORED - the breaks are always dropped, which is the flag-off behaviour | 0 | low |
| `usePrinterMetrics` | "Use Printer Metrics To Display Documents" | [§2.1grid](word-layout-rules.md#s21grid), [§10advances](word-layout-rules.md#s10advances) | IGNORED - and it is the document-level form of the question `docx4j.fonts.wordLineMetrics.deviceGrid` already asks | 0 | worth one, alongside the deviceGrid measurement |

### 3.2 `w:compatSetting` names

| Setting | What it changes in Word | Rules-doc sections | Status | Corpus | Probe |
| --- | --- | --- | --- | --- | --- |
| `compatibilityMode` | Which layout engine lays the document out | §1.4, §3.3, [§3.5](word-layout-rules.md#s35), §4.2, §6.1, [§6.1autofit](word-layout-rules.md#s61autofit) | **HONOURED** - `DocumentSettingsPart.getCompatibilityMode` (12 where absent); read by `TableWriter`, `AbstractFOExporter` and `WordLayoutFixups` | 346 | done (7 goldens) |
| `overrideTableStyleFontSizeAndJustification` | Whether the default paragraph style's size and `w:jc` override a table style's | §6.3, [§7hftblstyle](word-layout-rules.md#s7hftblstyle) | **HONOURED** - `DocumentSettingsPart.overrideTableStyleFontSizeAndJustification`, applied in `convert/out/common/preprocess/ParagraphStylesInTableFix`; named at [§7hftblstyle](word-layout-rules.md#s7hftblstyle) since 17.0.6 | 293 | tested (`PStyle*InTable*Test`) |
| `enableOpenTypeFeatures` | Whether Word applies OpenType features (ligatures, contextual alternates) | §5.4, §5.5 | IGNORED - the ligature policy is `docx4j.convert.out.fo.ligatures` plus the run's `w14:ligatures`; the document switch is not read | 291 | medium: a Calibri document with the setting off |
| `doNotFlipMirrorIndents` | Whether paragraph indents flip on even pages with mirrored margins | §7 (`w:mirrorMargins`), §3 | IGNORED - docx4j mirrors the *margins* and never the indents, i.e. it behaves as if this were on, which is Word 2013's own default | 291 | low |
| `differentiateMultirowTableHeaders` | Whether a multi-row `w:tblHeader` set repeats as one header | §6.7 | IGNORED - `TableWriter` emits `fo:table-header` from `w:tblHeader` unconditionally | 169 | medium: a two-row header repeated over a page break, both ways |
| `useWord2013TrackBottomHyphenation` | Hyphenation at the bottom of a page/column | §4.7 | NO RULE | 0 | no |
| `allowHyphenationAtTrackBottom` | The same, the other switch | §4.7 | NO RULE | 0 | no |
| `allowTextAfterFloatingTableBreak` | Whether text may follow a floating table that breaks | [§6.8](word-layout-rules.md#s69), [§6.8reserve](word-layout-rules.md#s68reserve) | IGNORED | 0 | medium: a floating table broken over a page |

### 3.3 Top-level `w:settings` children

| Setting | What it changes in Word | Rules-doc sections | Status | Corpus | Probe |
| --- | --- | --- | --- | --- | --- |
| `defaultTabStop` | The interval of the default tab grid | §4.4 | **HONOURED** - `XsltFOFunctions.defaultTabStop` (720 twips where absent) | 452 | done |
| `decimalSymbol` | What a decimal tab stop aligns on | [§4.4](word-layout-rules.md#s44clamp) | **HONOURED** - `XsltFOFunctions.decimalSymbol`, carried on `docx4j:tab-ind` | - | done |
| `autoHyphenation` (17.15.1.10) | Hyphenate automatically | §4.7 | **HONOURED** - `HyphenationSettings` / `DocumentSettingsPart.isAutoHyphenation` | - | done |
| `hyphenationZone` (17.15.1.44) | Largest gap tolerated before hyphenating | [§4.7zone](word-layout-rules.md#s47zone) | **HONOURED**, and measured to be inert: enforcing it is off by default | 185 | done |
| `consecutiveHyphenLimit` (17.15.1.22) | Hyphenated lines in a row | §4.7 | **HONOURED** | - | done |
| `doNotHyphenateCaps` (17.15.1.37) | Leave all-capital words whole | §4.7 | **HONOURED** | - | done |
| `evenAndOddHeaders` | Separate headers/footers on odd and even pages | [§7nohdr](word-layout-rules.md#s7nohdr), §7 (`w:pgNumType`) | **HONOURED** - `model/structure/DocumentModel`, `ConversionSectionWrapperFactory` | - | done |
| `mirrorMargins` | `w:pgMar/@w:left` is the *inside* margin | §7 | **HONOURED** - `LayoutMasterSetBuilder.settingIsOn`; property `docx4j.convert.out.fo.mirrorMargins` | 4 | done |
| `gutterAtTop` | The binding margin goes at the top | [§7gutter](word-layout-rules.md#s7gutter), [§7gutterland](word-layout-rules.md#s7gutterland) | **HONOURED** - `LayoutMasterSetBuilder.settingIsOn` | 2 | done |
| `themeFontLang` | Which theme font (`minorHAnsi`/`minorEastAsia`/`minorBidi`) a run resolves to | §5.1, §5.2 | **HONOURED** - `org.docx4j.model.PropertyResolver`; named at §5.1 of the rules doc since 17.0.6 | - | none needed |
| `footnotePr` / `endnotePr` (`w:pos`, `w:numFmt`, `w:numStart`, `w:numRestart`) | Where notes sit, and how they are numbered | §8 | NO RULE - notes always go in `fo:footnote` at the page foot, numbered with plain sequential integers (`FOExporterVisitorGenerator.handleFootnoteReference`) | 315 | **yes, high**: `w:numFmt="lowerRoman"`, `w:numRestart="eachPage"`, `w:pos="beneathText"` |
| `characterSpacingControl` | Which characters may be compressed for justification | §4.2, §4.6 | NO RULE | 452 | no (CJK; the count is "every Word file writes it") |
| `noPunctuationKerning` | Do not kern punctuation pairs | §5.4 | IGNORED - `RunFontSelector.isKerned` sends every pair of a `w:kern` run to the kerned twin | 71 | medium: a kerned Title containing punctuation |
| `printFractionalCharacterWidth` | Whether glyph advances are fractional or whole device units | [§10advances](word-layout-rules.md#s10advances), [§2.1grid](word-layout-rules.md#s21grid) | IGNORED | 0 | with `usePrinterMetrics` |
| `updateFields` | Whether Word updates fields when it opens the document | §7 (`DOCPROPERTY` cached results, `PAGEREF`) | IGNORED - the choice is the property `docx4j.convert.out.fields.docPropertyCachedResult`, where the document itself has an answer | 0 | yes, cheap: a stale `DOCPROPERTY` with the setting on |
| `bordersDoNotSurroundHeader` / `bordersDoNotSurroundFooter` | Whether a page border encloses the header/footer | §7 | NO RULE - `w:sectPr/w:pgBorders` is not rendered at all, so the flags are moot | 19 | only after page borders exist |
| `alignBordersAndEdges` | Align paragraph and table borders with the page border | §3, §6 | NO RULE (no page borders) | 0 | no |
| `displayBackgroundShape` | Paint `w:background` when displaying | §9 | NO RULE - the document background is not painted; ink, not geometry | 23 | no |
| `doNotShadeFormData` | Shading behind form fields | §9 | NO RULE - ink, not geometry | 20 | no |
| `strictFirstAndLastChars`, `noLineBreaksAfter`, `noLineBreaksBefore` | Kinsoku line-breaking sets | §4.1, §4.3 | NO RULE | - | no (CJK) |
| `attachedTemplate` + `linkStyles` | Styles come from the template, not from `styles.xml` | §2.8, §3, §5.1 (all style resolution) | NO RULE - docx4j never loads the attached template, so a document whose styles live there resolves against `docDefaults` alone | - | no; a documented limitation is enough |
| `documentProtection` | Editing restrictions | none | N/A - read by `openpackaging/packages/ProtectDocument` | 18 | no |
| `printTwoOnOne`, `bookFoldPrinting`, `bookFoldPrintingSheets`, `bookFoldRevPrinting` | Printer imposition | none | N/A (assumed: Word's own PDF export ignores them too) | - | no |
| `drawingGridHorizontalSpacing`, `drawingGridVerticalSpacing`, `displayHorizontalDrawingGridEvery`, `displayVerticalDrawingGridEvery`, `drawingGridHorizontalOrigin`, `drawingGridVerticalOrigin`, `doNotUseMarginsForDrawingGridOrigin` | The *display* grid drawing objects snap to while editing - **not** `w:sectPr/w:docGrid`, which is the text grid | none | N/A | - | no |
| `defaultTableStyle`, `clickAndTypeStyle` | Which style Word applies to new content | none | N/A (authoring) | - | no |
| `embedTrueTypeFonts`, `saveSubsetFonts`, `embedSystemFonts`, `savePreviewPicture`, `doNotAutoCompressPictures` | What Word stores in the package | §5.1, §5.6 (the fonts that *are* embedded are used) | N/A | - | no |
| `trackRevisions`, `revisionView`, `doNotTrackMoves`, `doNotTrackFormatting`, `removePersonalInformation`, `removeDateAndTime` | Revision tracking and privacy | none | N/A | - | no |
| `view`, `zoom`, `hideSpellingErrors`, `hideGrammaticalErrors`, `proofState`, `activeWritingStyle`, `doNotDisplayPageBoundaries`, `showXMLTags`, `alwaysShowPlaceholderText` | UI and proofing | none | N/A | - | no |

### 3.4 Counts

After 17.0.6's re-keying:

| Status | `w:compat` | `w:compatSetting` | top-level | total |
| --- | --- | --- | --- | --- |
| HONOURED | 6 | 2 | 10 | **18** |
| MODE-DEFAULTED | - | - | - | **0** |
| IGNORED | 27 | 4 | 3 | **34** |
| NO RULE | 19 | 2 | 8 | **29** |
| N/A | 5 | - | 8 (grouped rows) | **13** |

Three rules key on `compatibilityMode` with no flag to re-key to (§4.2, §6.1 twice), and
`w:sectPr/w:docGrid`, `w:sectPr/w:pgBorders` and `w:background` are unimplemented, which
is what makes eleven of the NO RULE rows moot rather than wrong.

---

## 4. What the follow-up batch should do

### (a) Flags whose rule exists and should be re-keyed, with the mode as the default

**All ten were wired in 17.0.6** through `org.docx4j.model.CompatibilityOptions`, and
**four of them were then measured against the corpora and taken out again** - the list is
kept as the record of why each was chosen and of what the measurement said. Ordered by
corpus count against the importance of the rule the flag would switch.

1. **`growAutofit`** (15) - *rejected*, see (e) - §6.3 / §6.5. Table column widths are the largest remaining
   geometric error class, and §6.5 records two corpus measurements of over-wide grids that
   disagree with each other; this flag is the most plausible discriminator. Probe: the
   existing `table-grid-overwide` document, saved once with the flag and once without.
2. **`useWord2002TableStyleRules`** (14) - *rejected*, see (e) - §6.3, [§7hftblstyle](word-layout-rules.md#s7hftblstyle).
   Table-style resolution feeds every cell's spacing and font; 152 corpus documents use a
   table style with a `w:pPr/w:spacing`.
3. **`doNotExpandShiftReturn`** (21) - *done* - §4.2. See (b): docx4j has the flag-on behaviour
   hard-wired, so the 293 corpus documents at mode 14 or 15 - none of which state it - are
   the ones getting the wrong answer.
4. **`forgetLastTabAlignment`** (5) - *rejected*, see (e) - [§4.4jc](word-layout-rules.md#s44jc). §4.4jc is a
   17.0.6 rule worth 372pt of dx on its probe; the flag turns exactly it off.
5. **`splitPgBreakAndParaMark`** (0 stated) - *done* - [§3.3each](word-layout-rules.md#s33each).
   Zero in the corpus, but the mode default is the point: §3.3each has one document whose
   page count the rule gets wrong, and this flag is the shape of the difference.
6. **`suppressSpBfAfterPgBrk`** (0 stated) - *done* - [§3.3](word-layout-rules.md#s33). The one
   mode-keyed rule that has a flag; re-keying it is a one-line change and settles the
   polarity question the rule leaves open.
7. **`doNotUseIndentAsNumberingTabStop`** + **`noTabHangInd`** (0 stated) - `noTabHangInd`
   *done*; `doNotUseIndentAsNumberingTabStop` **measured and rejected**, see (d) - §2.8, §4.4.
   List-label geometry is measured to the glyph in §2.8; these two say whether the hanging
   indent makes a stop at all.
8. **`doNotBreakWrappedTables`** (8) - *deferred*, there being no floating-table breaking
   rule for it to switch - [§6.8](word-layout-rules.md#s69).
9. **`allowSpaceOfSameStyleInTable`** (0 stated) - *wired, then taken out: Word 365 ignores it (compat-breaks probe)* - [§3.5](word-layout-rules.md#s35). The
   cell contextual-spacing rule cost 37 Word pages against 43 on one document; the flag
   switches it.
10. **`noLeading`** (0 stated) - *deferred*: `WordLineMetrics` is a static font-metric
    service read from four places, and no corpus document states the flag - §2.1. It changes the pitch of every line in the document,
    so it is the highest-leverage flag if any real document sets it.

### (d) Measured and rejected: `doNotUseIndentAsNumberingTabStop`

Keying [§2.8wide](word-layout-rules.md#s28wide)'s label column on the flag - the label's
text sent to the first real tab stop past it rather than to the hanging indent, which is
what the element's own title says - was measured over the three corpora and **every one of
the eight documents it moved got worse, none better**: 0.922 -> 0.778, 0.855 -> 0.702,
0.945 -> 0.908, 0.929 -> 0.905, 0.846 -> 0.823, 0.804 -> 0.768, 0.782 -> 0.743, and one
more, at unchanged page counts throughout. So Word's own PDFs of documents that state the
flag show the hanging indent standing anyway, and docx4j's unconditional rule is the one
that matches Word.

Whether Word ignores the flag outright, or reads it differently from its title, is not
established here; what is established is that honouring it as titled is wrong on every
document that has it. The `compat-numbering-tab` probe pair carries the shape for a golden
to settle it.

### (e) Measured and rejected: Word 365 does not honour the legacy `w:compat` flags

**The premise §2 is built on is false**, in both directions, and this is the batch's main
finding.

*The mode does not supply a flag Word did not write.* Measured on a mode-11 corpus document
whose `w:compat` is a full Word-2003 block (`useNormalStyleForList`,
`doNotUseIndentAsNumberingTabStop`, `useAltKinsokuLineBreakRules`,
`allowSpaceOfSameStyleInTable`, `doNotSuppressIndentation`, `doNotAutofitConstrainedTables`,
`autofitToFirstFixedWidthCell`, `displayHangulFixedWidth`, `splitPgBreakAndParaMark`,
`doNotVertAlignCellWithSp`, `doNotBreakConstrainedForcedTable`, `doNotVertAlignInTxbx`,
`useAnsiKerningPairs`, `cachedColBalance`) but **not** `useWord2002TableStyleRules`: Word's
own PDF of it applies the table style's `w:pPr`/`w:rPr` to the paragraphs of its tables, the
modern rule. Defaulting the flag on for its mode - which is what §2's table would have
docx4j do - dropped its whole body from 14pt Tinos to 10pt and cost it **0.951 -> 0.105 of
Word's lines and eleven of its fifty pages**.

*And a flag Word did write is not honoured either.* Two more mode-11 corpus documents carry
the same block **with** `useWord2002TableStyleRules` and `growAutofit` (one of them also
with `forgetLastTabAlignment` and `layoutRawTableWidth`), and Word 365's PDFs of those two
are the modern layout as well: honouring the flags cost them **0.948 -> 0.248 of Word's
lines with 13 of its pages becoming 11**, and **0.930 -> 0.842**.

So the three rules keyed on those flags were taken out again. `growAutofit`
(`AbstractTableWriter.fitToAvailableWidth`), `useWord2002TableStyleRules` (the whole
`pp.common.tbl-p-style-fix` step) and `forgetLastTabAlignment` (§4.4jc's whole-line
alignment) are unconditional once more, and each site says so and why. The flags still
resolve through `CompatibilityOptions`, so re-keying any of them is one line the day a
golden says which way.

What is left of the policy: **a legacy flag defaults off** - which is also exactly what the
schema says an absent `CT_OnOff` means - and a rule reads one only where no corpus document
contradicts it. That is `doNotExpandShiftReturn` (21 documents state it and keep 17.0.5's
behaviour), `noTabHangInd` and `allowSpaceOfSameStyleInTable` (32 corpus documents state
the latter and **none of them moves**, measured on the 17 of one corpus), plus the two
whose defaults are measurements of their own, `splitPgBreakAndParaMark` and
`suppressSpBfAfterPgBrk`.

The obvious hypothesis - that Word 365 ignores a `w:compat` block outright and lays a
document out from `compatibilityMode` alone - is consistent with all five measurements
above, and with §4(d)'s eight documents, but is not established: every document measured
here is mode 11 or carries a producer's block, and no probe with a Word golden tests a
stated flag yet. The four `compat-*` probe pairs are that measurement, waiting for
goldens.

`alignTablesRowByRow` (5) and `usePrinterMetrics` (0) are worth measuring next, both
because they are candidate answers to open questions
([§3.3rowtop](word-layout-rules.md#s33rowtop) and [§2.1grid](word-layout-rules.md#s21grid))
rather than for their counts.

### (b) Flags with no rule that plausibly matter

1. **`doNotExpandShiftReturn`** (21 stated) - **done in 17.0.6**. Word justifies a line
   ending in a soft line break unless this is set; docx4j never did, because `BrWriter`
   writes the break as a nested `fo:block` and the line before it is therefore a block's
   last line, so docx4j had the *legacy* behaviour for every document including the 293 at
   mode 14/15. The rule is [§4.2](word-layout-rules.md#s42shiftreturn); the probe pair is
   `compat-shift-return` / `-set` and awaits a Word golden.
2. **`w:footnotePr` / `w:endnotePr`** (315). Note position (`w:pos="beneathText"`,
   `"sectEnd"`, `"docEnd"`), number format (`w:numFmt`), start value (`w:numStart`) and
   restart (`w:numRestart="eachPage"` / `"eachSect"`) are all unimplemented; docx4j always
   numbers `1, 2, 3` from the document start and always puts the note at the page foot. At
   315 documents this is the largest untouched setting in the corpus, and a wrong note
   number is a wrong line on every page that carries one.
   **Probe**: one document with `w:numFmt="lowerRoman" w:numStart="3"`, one with
   `w:numRestart="eachPage"` over three pages, one with `w:pos="beneathText"`.
3. **`overrideTableStyleFontSizeAndJustification`** (293) - already honoured, and already
   covered by `PStyle12PtInTable*Test`, but **absent from the rules doc**. The follow-up
   batch should state the rule in §6 or §7 beside
   [§7hftblstyle](word-layout-rules.md#s7hftblstyle), which describes the same preprocess
   step without mentioning the setting that governs it.
4. **`differentiateMultirowTableHeaders`** (169). `w:tblHeader` becomes `fo:table-header`
   unconditionally; with the setting off Word repeats only the first row of a multi-row
   header set. A repeated header row is a whole line on every page of a long table.
   **Probe**: a two-row `w:tblHeader` on a table spanning three pages, both ways.
5. **`adjustLineHeightInTable`** (22) with **`doNotSnapToGridInCell`** (9). Both are about
   `w:sectPr/w:docGrid`, which docx4j does not implement at all - so the real gap is the
   document grid, and these two are only its switches. Worth recording as a known
   limitation rather than probing now.
6. **`spaceForUL`** (23) / **`ulTrailSpace`** (39) / **`underlineTabInNumList`** (12).
   All three are underline ink, not geometry: they change which pixels are underlined, not
   where a line breaks. Cheap to add if underline fidelity is ever scored; no probe is
   worth building for the line-parity harness.
7. **`allowTextAfterFloatingTableBreak`** (0). §6.8's positioned/floating table rules have
   no notion of a table breaking at all; relevant only once one does.
8. **`bordersDoNotSurroundHeader` / `bordersDoNotSurroundFooter`** (19). Blocked on
   `w:sectPr/w:pgBorders`, which is unrendered. 19 documents carry the flags, so at least
   19 carry page borders docx4j draws nothing for - **that** is the finding, and it is a
   rules-doc gap, not a settings one.
9. **`enableOpenTypeFeatures`** (291). docx4j's `+noliga` machinery (§5.5) already decides
   this per run from `w14:ligatures`; the document-level setting is the default those runs
   inherit, and reading it would let a document turn the whole thing off at once.
10. **`noPunctuationKerning`** (71) - one condition in `RunFontSelector.isKerned`.

### (c) Out of scope

**CJK and complex-script only** - correct behaviour needs a document grid, kinsoku tables
or a full East Asian line breaker, none of which docx4j has, and no corpus document
exercises them in a scored way: `useFELayout` (50), `balanceSingleByteDoubleByteWidth`
(14), `doNotWrapTextWithPunct` (9), `doNotUseEastAsianBreakRules` (9),
`useWord97LineBreakRules` (5), `useAltKinsokuLineBreakRules`, `lineWrapLikeWord6`,
`autoSpaceLikeWord95`, `displayHangulFixedWidth`, `applyBreakingRules`, `spaceForUL`,
`characterSpacingControl` (452, written by every Word file), `strictFirstAndLastChars`,
`noLineBreaksAfter`, `noLineBreaksBefore`.

**WordPerfect and Word-for-Mac emulation** - a document old enough to need these is older
than the corpus: `wpJustification`, `wpSpaceWidth`, `suppressTopSpacingWP`,
`truncateFontHeightsLikeWP6`, `mwSmallCaps`.

**N/A** - authoring, printing, proofing, privacy and UI switches, listed as such in §3.3.
`doNotLeaveBackslashAlone` (23) and `selectFldWithFirstOrLastChar` (8) have high corpus
counts and belong here: the first converts a backslash to a yen sign on a JIS code page and
the second is a text-selection behaviour; neither moves a line.

---

## 5. Where the reading lives

Built in 17.0.6, as sketched below and with the sketch's API almost unchanged.

`org.docx4j.model.CompatibilityOptions` sits beside `HyphenationSettings`:

```java
public final class CompatibilityOptions {

    public static CompatibilityOptions of(WordprocessingMLPackage pkg);
    public static CompatibilityOptions ofMode(int mode);        // for tests

    public int mode();                       // 11/12/14/15, 12 where absent
    public boolean is(Flag flag);            // the flag, or its mode's default
    public boolean isStated(Flag flag);      // whether the document says so itself
    public boolean setting(String name, boolean modeDefault);   // a w:compatSetting

    public enum Flag {
        GROW_AUTOFIT, USE_WORD2002_TABLE_STYLE_RULES, FORGET_LAST_TAB_ALIGNMENT,
        LAYOUT_RAW_TABLE_WIDTH, DO_NOT_BREAK_WRAPPED_TABLES, DO_NOT_EXPAND_SHIFT_RETURN,
        DO_NOT_AUTOFIT_CONSTRAINED_TABLES, AUTOFIT_TO_FIRST_FIXED_WIDTH_CELL,
        DO_NOT_USE_INDENT_AS_NUMBERING_TAB_STOP, NO_TAB_HANG_IND,
        ALLOW_SPACE_OF_SAME_STYLE_IN_TABLE, NO_LEADING,
        SPLIT_PG_BREAK_AND_PARA_MARK, SUPPRESS_SP_BF_AFTER_PG_BRK;

        public boolean defaultAt(int mode);  // §2's table, in one place
    }
}
```

* **The mode default table is one method.** Each `Flag` carries `onBelowMode` and
  `onFromMode` and `defaultAt` is `mode < onBelowMode || mode >= onFromMode`, so §2 above
  has exactly one implementation. Each constant's javadoc says where its default comes
  from, and marks an assumed one **assumed**.
* **The three states are kept.** A null child element means "not stated" and takes the mode
  default; `<w:growAutofit w:val="0"/>` is stated, is false, and beats it.
  `BooleanDefaultTrue.isVal()` is only asked once the element is known to be present.
* **A package with no settings part at all** states nothing, so its flags take Word 365's
  defaults (mode 15's). Its `mode()` is still 12, which is what
  `DocumentSettingsPart.getCompatibilityMode` answers and what the mode-keyed rules are
  measured against.
* **It rides `HyphenationSettings`'s route**: cached per conversion on
  `AbstractWmlConversionContext.getCompatibilityOptions()`, and passed to
  `WordLayoutFixups.apply` in place of the bare `int compatibilityMode` (the `int`
  overloads remain, and build an options object from the mode alone).
* **Flags the layout managers need travel as `docx4j:` attributes** - `no-tab-hang-ind` on
  `fo:root`, `justify-soft-return` on the paragraph's block - beside `docx4j:space-shrink`
  and the hyphenation settings. A flag read while the FO is written never reaches FOP.
* **`DocumentSettingsPart` keeps only the raw accessors**, as below.
* Unit tests: `CompatibilityOptionsTest` (docx4j-core-tests) for the resolution,
  `UseWord2002TableStyleRulesTest` for the preprocess step the flag would switch off, and
  `CompatFlagsTest` (docx4j-export-fo-tests) for the rules, in both exporter pathways.

### 5.1 What it replaced

Before 17.0.6 the mode was read in four places, each with its own try/catch and its own
default: `TableWriter.compatibilityMode` (returns 12 on failure), `AbstractFOExporter`
(passing it into `WordLayoutFixups.apply`), `WordLayoutFixups.lineBoxAttributes` and
`.mergePageBreakParagraphs` (taking it as an `int` parameter), and
`DocumentSettingsPart.getCompatibilityMode(pkg)` underneath them all. Adding fifty flags on
that pattern would have meant fifty lookups scattered across `XsltFOFunctions`,
`TableWriter`, `WordLayoutFixups` and the layout managers. `DocumentSettingsPart` keeps only
the raw accessors it had - `getCompatibilityMode`, `isDoNotUseHTMLParagraphAutoSpacing`, the
four hyphenation getters, `overrideTableStyleFontSizeAndJustification` and
`getWordCompatSetting`; the mode-defaulting logic does not belong in an OPC part.
