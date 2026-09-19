# [MS-DOCX] schemas against docx4j's WordprocessingML binding

An inventory of the ten namespaces in the "Full XML Schemas" appendix of
[MS-DOCX] revision 23.0 (2026-08-18, from the specification's table of
contents on learn.microsoft.com,
https://learn.microsoft.com/en-us/openspecs/office_standards/ms-docx/b839fe1f-e1ca-4fa6-8c26-5954d0abbccd)
against what docx4j binds, admits in the main schema, or knows by prefix,
measured on VERSION_17_1_1 on 2026-09-20 (Jason asked, after the [MS-XLSX]
inventory, "identify any schema used within it which we don't yet support"),
and **updated the same day after CR-023** closed the gaps it found.
Purposes are from the appendix's own XSD text (small enough to read whole for
eight of the ten) and from section 2.2's "Extensions" pages, which say which
ISO element each extension hangs on.

[MS-DOCX] is a much smaller specification than [MS-XLSX]: ten namespaces
against forty-five, five parts against nine, and the bulk of Word's own
markup beyond ISO/IEC 29500 lives in other specifications - the drawing
namespaces (`wp14`, `wps`, `wpg`, `wpc`, `wpi`, [MS-ODRAWXML]), the comment
reactions part (`cr`, [MS-OREACTXML]) - which this inventory names only where
a Word document carries them beside the [MS-DOCX] ones.

## What docx4j binds today

The Word JAXB context (`org.docx4j.jaxb.Context.jc`) holds six of the ten as
packages: `org.docx4j.w14` (5.1), `org.docx4j.w15` (5.2), `org.docx4j.w15symex`
(5.3), `org.docx4j.w16cid` (5.4), `org.docx4j.w16` (5.5), `org.docx4j.w16cex`
(5.6, since CR-018); the main schema `xsd/wml/wml.xsd` references their
elements and attributes where Word writes them (w14's text effects and
OpenType groups in `rPr`, `paraId`/`textId` on `p` and `tr`, the sdtPr
extensions of w14 and w15, the settings elements, `w15:footnoteColumns`,
`w15:collapsed`). Four of the five parts of section 2.1 are typed
(`CommentsExtendedPart`, `PeoplePart`, `CommentsIdsPart`,
`CommentsExtensiblePart`); `stylesWithEffects` loads as a generic XML part
(a copy of the styles part, by the specification's own description).

Two states short of "bound", and one worse:

**After CR-023 (2026-09-20) none of these states has a member**: 5.7, 5.8
and 5.9 are admitted as typed properties of their hosts (attribute-only
schemas generate no package), 5.10 `cei` is bound (`org.docx4j.cei`), and
the seven references below are in `wml.xsd`. The measurement that found
them is kept as the record:

- **prefix known** (was): in `NamespacePrefixMappings` both ways, so a part's
  `mc:Ignorable` naming it is declared on save (no repair prompt), but the
  namespace's content is neither typed nor admitted: 5.7 `w16sdtdh`, 5.8
  `w16du`, 5.9 `w16sdtfl`. Each declares a single attribute.
- **absent** (was): 5.10 `cei` (new in this revision).
- **bound but not admitted** (was) - the state [MS-XLSX] did not have: the
  package exists, yet the main schema has no reference for the attribute or
  element where Word writes it, so it is **dropped on a load-and-save**
  (JAXB keeps no unknown attribute; `wml.xsd` has no `anyAttribute`).
  Measured with a probe that forces every part to unmarshal (an untouched
  part is written back from its bytes, which hides the loss):

  | attribute or element | host | file | before | after |
  |---|---|---|---|---|
  | `w15:restartNumberingAfterBreak` (5.2, section 2.2.10) | `w:abstractNum` | `numPicBullet-word2019-pict.docx` | 1 | 0 |
  | `w16cid:durableId` (5.4) | `w:num` | `tracked-changes-equations.docx` | 1 | 0 |
  | `w16du:dateUtc` (5.8) | `w:ins`, `w:del` | `tracked-changes-equations.docx` | 39 | 0 |

  Not measured for want of a file, but not referenced by `wml.xsd` either,
  so the same fate: `w14:noSpellErr` on `w:p` (section 2.2.4),
  `w16se:symEx` in `w:r` (2.2.11), `w16sdtdh:storeItemChecksum` on
  `w:dataBinding` (2.2.12), `w16sdtfl:formattingAllowed` on `w:sdtPr`.
  `w16cid:durableId` inside the `commentsIds` part survives (that part is
  typed); it is the copy on `w:num` that Word 365 writes which is lost.

## Occurrence

Measured on the repository's 74 Word documents (test resources, samples,
export-fo and markdown fixtures; 55 with a "Microsoft Office Word"
`app.xml`s) and, for the count of declaring and using files only, on the
fidelity corpus of 342 real documents on the share (not redistributable;
nothing from it is copied here).

| namespace | repository: declared / using | corpus: declared / using | what is used |
|---|---|---|---|
| 5.1 `w14` | 51 / 23 | 321 / 57 | `paraId`, `textId` (thousands), `docId`, `defaultImageDpi`, text effects (`srgbClr`, `textOutline`, ...) |
| 5.2 `w15` | 18 / 17 | 284 / 109 | `restartNumberingAfterBreak` (637 in the corpus), `docId`, `chartTrackingRefBased`, `commentEx`, `people` |
| 5.3 `w16se` | 17 / 0 | 193 / 0 | declared in every recent document, `symEx` never written |
| 5.4 `w16cid` | 17 / 8 | 148 / 0 | `durableId` on `w:num` and in `commentsIds` |
| 5.5 `w16` | 14 / 0 | 38 / 0 | `ext`/`extLst` types only, used through w16cex |
| 5.6 `w16cex` | 14 / 1 | 38 / 0 | `commentsExtensible`, `commentExtensible` with `dateUtc` |
| 5.7 `w16sdtdh` | 13 / 0 | 0 / 0 | |
| 5.8 `w16du` | 13 / 1 | 0 / 0 | `dateUtc` on `w:ins`/`w:del` (39 in one file) |
| 5.9 `w16sdtfl` | 13 / 0 | 0 / 0 | |
| 5.10 `cei` | 5 / 0 | 0 / 0 | declared by Word 365 (16.0000) documents of 2026, not in their `mc:Ignorable` |

The `mc:Ignorable` of a current Word 365 `document.xml` is
`w14 w15 w16se w16cid w16 w16cex w16sdtdh w16sdtfl w16du wp14`; the corpus,
older, stops at `w14 wp14 w15 w16se`. Beside the [MS-DOCX] namespaces the
repository's documents declare `wp14` (51 files, `anchorId`/`editId`,
`sizeRelH`), `wps`/`wpg`/`wpc`/`wpi` (bound: the
`org.docx4j.com.microsoft.schemas.office.word.x2010.*` packages) and, in one,
`cr` (comment reactions, [MS-OREACTXML], inside `commentsExtensible`'s
`extLst`; kept as the lax wildcard's DOM).

## The ten namespaces

`status`: bound / admitted / prefix / absent. "bound" is the package;
"admitted" is the main schema referencing the extension where Word writes it,
so a load-and-save keeps it.

| § | prefix | namespace (`http://schemas.microsoft.com/office/word/` + ...) | purpose | status |
|---|---|---|---|---|
| 5.1 | w14 | 2010/wordml | Word 2010: run text effects (glow, shadow, reflection, 3-D, gradient and pattern fills, outlines) and OpenType features (ligatures, number forms, stylistic sets), the `checkbox` and `entityPicker` content controls, conflict tracking (co-authoring), `paraId`/`textId` on paragraphs and rows, `docId`, image settings (`defaultImageDpi`, `discardImageEditingData`), `noSpellErr` | bound; admitted (`noSpellErr` since CR-023) |
| 5.2 | w15 | 2012/wordml | Word 2013: the `commentsEx` part (`paraIdParent`, `done`), the `people` part with presence info, `repeatingSection`/`repeatingSectionItem`, `appearance` and `color` of a content control, `dataBinding` (the w15 form, for repeating sections), `docId`, `footnoteColumns`, `chartTrackingRefBased`, `collapsed` headings, web-extension flags, `restartNumberingAfterBreak` on `abstractNum` | bound; admitted (`restartNumberingAfterBreak` since CR-023, which also added it to docx4j's 2012 copy of the schema) |
| 5.3 | w16se | 2015/wordml/symex | `symEx` in a run: a symbol with a font and a Unicode code point beyond the `w:sym` range (supplementary planes) | bound; admitted in `w:r` (CR-023) |
| 5.4 | w16cid | 2016/wordml/cid | the `commentsIds` part (`paraId` to `durableId`) and `durableId` where Word writes it on `w:num` | bound (part); admitted on `w:num` (CR-023, the decimal attribute group added to docx4j's copy) |
| 5.5 | w16 | 2018/wordml | `CT_Extension`/`CT_ExtensionList`: the `ext`/`extLst` mechanism the newer Word namespaces hang content on | bound |
| 5.6 | w16cex | 2018/wordml/cex | the `commentsExtensible` part: `durableId`, `dateUtc`, `intelligentPlaceholder` per comment, with an `extLst` (reactions, entity info) | bound (part, CR-018) |
| 5.7 | w16sdtdh | 2020/wordml/sdtdatahash | one attribute, `storeItemChecksum` on `w:dataBinding` (a hash of the custom XML store item a content control binds to) | **admitted (CR-023)** |
| 5.8 | w16du | 2023/wordml/word16du | one attribute, `dateUtc`: the UTC form of a revision's or comment's date, beside ISO's local `w:date` | **admitted (CR-023)**: `CTTrackChange.getDateUtc()` |
| 5.9 | w16sdtfl | 2024/wordml/sdtformatlock | one attribute, `formattingAllowed` on a content control's properties (whether formatting may be changed inside a locked control) | **admitted (CR-023)** |
| 5.10 | cei | 2026/wordml/cei | `commentEntityInfo` (`entityType`), an `ext` child in `commentsExtensible`: what kind of entity authored the comment - new in revision 23.0 (2026-08) | **bound (CR-023)**: `org.docx4j.cei` |

## The five parts (section 2.1)

| § | part | content type | docx4j |
|---|---|---|---|
| 2.1.1 | stylesWithEffects | `...wordprocessingml.stylesWithEffects+xml` | generic XML part (kept as-is; a copy of styles, by the specification) |
| 2.1.2 | commentsExtended | `...wordprocessingml.commentsExtended+xml` | `CommentsExtendedPart` (w15) |
| 2.1.3 | people | `...wordprocessingml.people+xml` | `PeoplePart` (w15) |
| 2.1.4 | commentsIds | `...wordprocessingml.commentsIds+xml` | `CommentsIdsPart` (w16cid) |
| 2.1.5 | commentsExtensible | `...wordprocessingml.commentsExtensible+xml` | `CommentsExtensiblePart` (w16cex, CR-018) |

Section 2.3's `compatSetting` names (`compatibilityMode`,
`overrideTableStyleFontSizeAndJustification`, `enableOpenTypeFeatures`,
`doNotFlipMirrorIndents`, `differentiateMultirowTableHeaders`,
`useWord2013TrackBottomHyphenation`, `allowTextAfterFloatingTableBreak`,
`allowHyphenationAtTrackBottom`, `usePre2018iOSMacLayout`) are values of ISO's
`w:compatSetting`, not a schema; docx4j reads them through
`DocumentSettingsPart` and writes the first six on `createPackage` (17.1.1).

## What this suggests, for a CR to decide

- **First, and small** - **implemented via CR-023 (17.1.1, 2026-09-20)**: the "bound but not admitted" attributes - a CR of
  six attribute references in `wml.xsd` (`w15:restartNumberingAfterBreak`
  on `CT_AbstractNum`, `w16cid:durableId` on `CT_Num`, `w16du:dateUtc` on
  `CT_TrackChange` and `CT_Markup`'s kin, `w14:noSpellErr` on `CT_P`,
  `w16sdtdh:storeItemChecksum` on `CT_DataBinding`,
  `w16sdtfl:formattingAllowed` on `CT_SdtPr`) and one element reference
  (`w16se:symEx` in `EG_RunInnerContent`), with three new attribute-only
  schemas (`w16sdtdh`, `w16du`, `w16sdtfl`) taken from their section pages -
  each generates no package (the [MS-XLSX] lesson), so it is the xsd files,
  the references and a regeneration. Every Word 365 document carries
  `dateUtc` on every tracked change and `restartNumberingAfterBreak` on
  every numbering definition, and docx4j drops both today; the regression
  is silent because Word rebuilds them. The [MS-XLSX] recipe applies
  (`docs/developer/adding-a-schema.md`), and the round-trip probe of this
  inventory (force every part to unmarshal, then count) is the test.
- **Second** - **implemented via CR-023**: `cei` (5.10) - one element inside `commentsExtensible`'s
  `extLst`, kept as DOM by the lax wildcard today; binding it types it, as
  CR-022 did for `extLst` content in workbooks. Small; with the first, or
  when a file shows it used.
- **Third, outside [MS-DOCX]**: the comment reactions part ([MS-OREACTXML],
  `cr`), also `extLst` content today, and the drawing namespaces of
  [MS-ODRAWXML] that docx4j already binds. Nothing here is lost; a typed
  reactions part is a feature request rather than a fidelity gap.
- `stylesWithEffects` as a typed part: no gain - the generic part
  round-trips it, and Word regenerates it from styles.

Each of these is a change request first (`docs/developer/adding-a-schema.md`,
step 0). The first item is the one with a measured loss.
