# CR-023: Word's extension attributes kept on a round trip - `dateUtc`, `restartNumberingAfterBreak`, `durableId`, `noSpellErr`, `storeItemChecksum`, `formattingAllowed`, `symEx`, and `cei` bound

Status: PROPOSED 2026-09-20 (Jason Harrop: "please write the CR", after the
[MS-DOCX] inventory of `docs/developer/ms-docx-schema-inventory.md` measured
three of these attributes dropped on a load-and-save). Written by
`docs/developer/adding-a-schema.md` step 0: the recipe followed, departures
named in §13. Drafted with Claude Fable 5.1. Owner: Jason Harrop.

Scope: every [MS-DOCX] 23.0 extension that docx4j's Word schema does not
admit where Word writes it, so that a document loaded and saved through
docx4j keeps it - six attributes and one element on ISO types, three of them
in namespaces docx4j knows only by prefix (§5.7 `w16sdtdh`, §5.8 `w16du`,
§5.9 `w16sdtfl`, one attribute each, taken as schemas), and the one namespace
the appendix added in revision 23.0 (§5.10 `cei`, `commentEntityInfo`).
After it, every namespace of [MS-DOCX]'s appendix is bound and admitted. Not
in scope: the comment reactions part (`cr`, [MS-OREACTXML], `extLst` content
that round-trips as DOM), the drawing namespaces of [MS-ODRAWXML] (bound
already), and a typed `stylesWithEffects` part (the generic part
round-trips it).

## 1. The schemas and a file exercising them (recipe step 1)

**Source**: [MS-DOCX] revision 23.0 (2026-08-18), the HTML section pages of
appendix 5 (the recipe's preferred source; strip the pages' non-breaking
spaces, as the recipe now says):

| § | prefix | namespace (`.../office/word/` + ...) | what it declares | host, per [MS-DOCX] 2.2 and Word's output |
|---|---|---|---|---|
| 5.1 | w14 | 2010/wordml | (bound) `noSpellErr` attribute, already in `xsd/wml/w14_word_2010_wordml.xsd` | `w:p` (2.2.4) |
| 5.2 | w15 | 2012/wordml | (bound) `restartNumberingAfterBreak` attribute, already in `w15_word_2012_wordml.xsd` | `w:abstractNum` (2.2.10) |
| 5.3 | w16se | 2015/wordml/symex | (bound) `symEx` element, `xsd/wml/w15symex.xsd` | inside `w:r`, beside `w:sym` (2.2.11) |
| 5.4 | w16cid | 2016/wordml/cid | (bound) `durableId`: the `decimaldurableId` attribute group (2.8.2.2, `ST_DecimalNumber`), in `w16cid.xsd` | `w:num` (Word 365 writes it there; the `commentsIds` part's hex form is typed already) |
| 5.7 | w16sdtdh | 2020/wordml/sdtdatahash | `storeItemChecksum` (`ST_String`, a base64 CRC of the bound custom XML part) - **new file** | `w:dataBinding` (2.2.12), so also `w15:dataBinding`, which is the same type |
| 5.8 | w16du | 2023/wordml/word16du | `dateUtc` (`xsd:dateTime`, a tracked change's date in UTC) - **new file** | every tracked change: `w:ins`, `w:del`, `w:rPrChange`, `w:pPrChange`, ... and `w:comment` - all extend `CT_TrackChange` |
| 5.9 | w16sdtfl | 2024/wordml/sdtformatlock | `formattingAllowed` (`ST_OnOff`, with `contentLocked`/`sdtContentLocked`) - **new file** | `w:sdtPr` (2.13) |
| 5.10 | cei | 2026/wordml/cei | `commentEntityInfo` (`entityType`, an `extLst`) - **new file** | an `ext` child of `w16cex:commentExtensible`'s `extLst` (2.14) |

**Occurrence** (the inventory, 2026-09-20): every Word 365 document
declares all of these prefixes (`mc:Ignorable="w14 w15 w16se w16cid w16
w16cex w16sdtdh w16sdtfl w16du wp14"`, plus `xmlns:cei` since 2026). Used
in the repository's documents: `w16du:dateUtc` on 25 `w:ins` and 14 `w:del`
of one file, `w15:restartNumberingAfterBreak` on the `abstractNum` of eight,
`w16cid:durableId` on the `num` of seven; in the corpus of 342 real
documents `restartNumberingAfterBreak` 637 times. `noSpellErr`, `symEx`,
`storeItemChecksum`, `formattingAllowed` and `commentEntityInfo`: in none.

**Measured loss** (a load, every JAXB part forced to unmarshal, a save):

| attribute | file | before | after |
|---|---|---|---|
| `w16du:dateUtc` | `tracked-changes-equations.docx` | 39 | 0 |
| `w15:restartNumberingAfterBreak` | `numPicBullet-word2019-pict.docx` | 1 | 0 |
| `w16cid:durableId` on `w:num` | `tracked-changes-equations.docx` | 1 | 0 |

Silent, because Word rebuilds all three on its next save; but a document
that passes through docx4j between two co-authors loses the UTC dates of
its revisions, and its numbering restarts differently after a section
break if the flag was on.

**Files exercising them**: for the three measured, the repository's own
(above). For the other five, no file exists; Jason makes one in Word 365
(ten minutes) and it is measured in phase 0 before anything is admitted
(CR-021's rule: admit what Office writes, confirmed on a file):

1. **symEx**: *Insert > Symbol > More Symbols*, font Segoe UI Emoji (or any
   font with supplementary-plane glyphs), pick a character above U+FFFF
   (an emoji), Insert. Word writes `w:sym` for the BMP and `w16se:symEx`
   for these.
2. **storeItemChecksum**: *Insert > Quick Parts > Document Property > Title*
   (a content control bound to the core-properties custom XML), type a
   title. Word writes `w:dataBinding` with `w16sdtdh:storeItemChecksum`.
3. **formattingAllowed**: *Developer > Rich Text Content Control*, then
   *Properties*: tick "Contents cannot be edited"; if the dialog offers an
   "allow formatting" choice, tick it. Measure whether Word writes
   `w16sdtfl:formattingAllowed`; if not, note it and the attribute is
   admitted on the specification's word alone (a departure, recorded).
4. **noSpellErr**: type a paragraph, run *Review > Spelling* to completion.
   Word sets `w14:noSpellErr="1"` on checked paragraphs in some builds;
   measure, same fallback as 3.
5. **commentEntityInfo**: add a comment (*Review > New Comment*); if the
   build writes `cei:commentEntityInfo` for a human comment it shows in
   `commentsExtensible.xml`; if only Copilot-authored comments carry it,
   note it and the schema is bound on the page's word.
6. *File > Save As*, `cr023-word-extensions.docx`, to the share's
   `fidelity/cr023/`; no personal data beyond the author name.

## 2. Where they join the tree, and their dependencies (recipe step 2)

`xsd/wml/` (the Word tree; the other Word extension schemas live there):
`w16sdtdh.xsd`, `w16du.xsd`, `w16sdtfl.xsd`, `cei.xsd`, each importing
`wml.xsd` for the `w12:` types it uses (`ST_String`, `ST_OnOff`; `cei`
imports `w16.xsd` for `CT_ExtensionList`). `xsd/ROOT.xsd` imports the four
beside its `w16cid`/`w16`/`w16cex`/`w15symex` imports. **`wml.xsd` itself
imports** `w16sdtdh`, `w16du`, `w16sdtfl` (it already imports w14, w15 and
- to be checked in phase 1 - w16se and w16cid), because the attribute and
element references below sit in it; a circular import (the small file
imports `wml.xsd` back for `w12:` types) is what `w16cid.xsd` does already.

No dependency crosses a JAXB context: everything is `jc` (Word). Nothing is
needed in the PresentationML or SpreadsheetML contexts.

**The host side** - the references `wml.xsd` gains, each where Word writes
the extension (the type names as ECMA-376 has them; docx4j's file renames
some classes through `jaxb:class`, phase 1 finds the types by their
element):

| host type | reference | why there |
|---|---|---|
| `CT_TrackChange` (the base of `w:ins`, `w:del`, the `*PrChange`s, `w:comment`; sixteen extensions) | `<xsd:attribute ref="w16du:dateUtc"/>` | one reference covers every tracked change; measured on `w:ins`/`w:del` |
| `CT_AbstractNum` | `<xsd:attribute ref="w15:restartNumberingAfterBreak"/>` | 2.2.10; measured |
| `CT_Num` | `<xsd:attributeGroup ref="w16cid:decimaldurableId"/>` | Word writes the decimal form (`w16cid:durableId="1187672177"`); measured |
| `CT_P` | `<xsd:attribute ref="w14:noSpellErr"/>` | 2.2.4 (beside the existing `w14:AG_Parids`) |
| `CT_DataBinding` | `<xsd:attribute ref="w16sdtdh:storeItemChecksum"/>` | 2.2.12; `w15:dataBinding` shares the type |
| `CT_SdtPr` | `<xsd:attribute ref="w16sdtfl:formattingAllowed"/>` | 2.13 |
| `EG_RunInnerContent` | `<xsd:element ref="w16se:symEx" minOccurs="0"/>` after `sym` | 2.2.11; a run's content list gains a `CTSymEx` member |

`cei:commentEntityInfo` needs no host reference: it sits in
`w16cex:commentExtensible/extLst/ext`, a lax wildcard (`w16:CT_Extension`),
so once the package exists in `jc` it unmarshals typed, as CR-022's `extLst`
content did - the one behaviour change a caller could notice (§13).

## 3. The xsd annotations (recipe step 3)

Each new file: the JAXB namespaces, `jaxb:version="3.0"`, the package
annotation; no root-element annotation (nothing here is a part root); the
header citing the section page and the fetch date. Packages, by the Word
tree's convention (`org.docx4j.<prefix>`, as `w16cex`, `w16cid`):

| namespace | package | generated? |
|---|---|---|
| 2020/wordml/sdtdatahash | `org.docx4j.w16sdtdh` | no - attribute only (the [MS-XLSX] finding; recipe step 4's note) |
| 2023/wordml/word16du | `org.docx4j.w16du` | no - attribute only |
| 2024/wordml/sdtformatlock | `org.docx4j.w16sdtfl` | no - attribute only |
| 2026/wordml/cei | `org.docx4j.cei` | yes: `CTCommentEntityInfo`, `ObjectFactory` (`createCommentEntityInfo`) |

The three attribute-only annotations are kept for consistency with the
tree, and so that a later element in one of those namespaces lands in a
named package; they change nothing in the build.

`w16du:dateUtc` is `xsd:dateTime`, which XJC binds as
`XMLGregorianCalendar` - the same type docx4j already gives ISO's `w:date`
on every tracked change (`CTTrackChange.getDate()`) and w16cex's `dateUtc`
on a comment, so `getDateUtc()` matches its neighbours. Kept as XJC gives
it (decided with Jason, 2026-09-20: no issue with the type in the tree).

## 4 to 6. Regeneration, module-info, contexts (recipe steps 4 to 6)

`mvn clean install -DskipTests -Dgpg.skip=true -pl docx4j-generated-objects,docx4j-core`;
one `exports`/`opens` pair (`org.docx4j.cei`) in the generated-objects
`module-info.java`; `org.docx4j.cei` appended to `Context.jcString` (and its
commented twin) - the Word context only. The attribute-only packages have
nothing to export or list (recipe step 4's paragraph).

## 7. The prefix table (recipe step 7)

Present already, both ways: w14, w15, w16se, w16cid, w16, w16cex, w16sdtdh,
w16du, w16sdtfl. To add, beside them: `cei`
(`http://schemas.microsoft.com/office/word/2026/wordml/cei`), which is
Word's own prefix. objects-ts and the Python port keep copies (§11).

## 8 and 9. Parts and their registration (recipe steps 8 and 9)

None: no namespace here defines a part.

## 10. Tests (recipe step 10)

In `docx4j-core-tests`, `org.docx4j.wml.WordExtensionAttributesTest`:

- the round-trip probe of the inventory as a test: load, force every
  `JaxbXmlPart` to unmarshal, save, count - `w16du:dateUtc` 39 → 39 on
  `tracked-changes-equations.docx`, `w15:restartNumberingAfterBreak` 1 → 1
  and `w16cid:durableId` 1 → 1 on `numPicBullet-word2019-pict.docx` (and
  the OpenDoPE ones);
- the typed accessors: `CTTrackChange.getDateUtc()` on an `ins`,
  `CTAbstractNum.getRestartNumberingAfterBreak()` (or whatever XJC names
  the `w15` attribute), `CTNum.getDurableId()`;
- on Jason's file (phase 0), whichever of `symEx` (a `CTSymEx` in the run's
  content), `storeItemChecksum`, `formattingAllowed`, `noSpellErr` and
  `commentEntityInfo` (typed inside `commentExtensible`'s `extLst`) Word
  wrote, read, kept through a save, and the `mc:Ignorable` of `document.xml`
  complete on the re-save;
- `LoadAndSaveTests` and the whole `docx4j-core-tests` suite;
  `docx4j-export-fo-tests` once, since `CT_P` and `CT_TrackChange` are on
  every exporter's path (a new attribute on `P` should cost nothing, but
  the FO suite is the check that no exporter's copy of a paragraph broke);
- the re-save opens in Word 365 without a repair prompt (Jason).

## 11. Follow-through and hand-offs (recipe step 11)

CHANGELOG under "Schema (CR-023 ...)" for 17.1.1 (or the release Jason
names); this CR per phase; `docs/developer/ms-docx-schema-inventory.md`
updated (the "bound but not admitted" state emptied, `cei` bound, "what
this suggests" first item implemented); objects-ts told the four xsd files,
the seven `wml.xsd` references, the package and the prefix entry (it
regenerates once from the commit); the Python port told (its generation is
copy-based); core-ts told, since its Word round-trip parity would otherwise
show docx4j keeping what the port drops.

## 12. Phases and gates

0. **Jason's file and its measurement**: `cr023-word-extensions.docx` made
   as §1 says; the survey of which of the five unmeasured extensions Word
   wrote and on which element; the inventory's probe run on it. No code.
   Gate: the host list of §2 confirmed or amended (an extension Word did
   not write is admitted on the specification's word, recorded as such).
1. **The schemas, references, regeneration, context, prefix, tests**: one
   commit. Gate: the three measured losses closed (the probe test), the
   accessors typed, core suite green, export-fo suite green, Word opens the
   re-save.
2. **Follow-through**: CHANGELOG, inventory, this CR closed, the hand-offs.

## 13. Risks and departures, in one place

- **Attribute-only schemas generate no package** (recipe steps 5 and 6 do
  not apply to three of the four files): named here as the recipe now
  says.
- **`w16cid:durableId` on `w:num` is the decimal form** (2.8.2.2, an
  attribute group), not the hex `ST_LongHexNumber` global attribute
  (2.8.2.1) the `commentsIds` part uses: the reference is to the group, so
  the accessor is a `BigInteger`, not a `String`.
- **`dateUtc` as `XMLGregorianCalendar`** (§3): consistent with `w:date`;
  decided.
- **API growth only**: seven new accessors on existing classes, one new
  content-list member type (`CTSymEx` in `R.getContent()`); nothing
  removed or renamed. A caller walking a run's content with an exhaustive
  `instanceof` chain sees a new class.
- **`extLst` content becomes typed** for `cei` (a `JAXBElement<CTCommentEntityInfo>`
  where a DOM `Element` was): no docx4j code reads that `extLst` (CR-018's
  part is new); recorded in the CHANGELOG for callers.
- **Extensions Word may not write on demand** (`noSpellErr`, `formattingAllowed`,
  `commentEntityInfo`): admitted on the specification's word if phase 0
  cannot produce them; the accessors are then untested by a fixture and
  say so in their test.
- **MOXy**: `jc` grows by one package; the core suite's context check.

## 14. Effort (rough)

Phase 0 an hour (Jason's file, the survey); phase 1 half a day (four small
files, seven references, a regeneration, one test class, two suites);
phase 2 an hour.

## 15. References

- [MS-DOCX] 23.0: §2.2.4, §2.2.10, §2.2.11, §2.2.12 (hosts), §2.6.2.2
  (`noSpellErr`), §2.7.1.1 (`symEx`), §2.8.2.2 (decimal `durableId`),
  §2.11.2.1, §2.12.2.1, §2.13.2.1, §2.14.3.1; §5.7 to §5.10 (schemas).
- `docs/developer/ms-docx-schema-inventory.md` (the measurement),
  `docs/developer/adding-a-schema.md` (the recipe), CR-018 (w16cex, the
  precedent in this tree), CR-022 (the attribute-only finding, the
  probe pattern).

## 16. Phase 0: Jason's file and its measurement (2026-09-20)

`cr023-word-extensions.docx` (share, `fidelity/cr023/`; Word 365, `AppVersion`
16.0000; 22 parts including `commentsExtended`, `commentsIds`,
`commentsExtensible`, `people` and a glossary), made by §1's steps, every
part surveyed for the seven extensions. **Word wrote none of the five
unmeasured ones**, each for a reason the file shows:

| extension | what Word did instead |
|---|---|
| `w16se:symEx` | the two supplementary-plane characters went into the run as text (`<w:t>🀀🐼</w:t>`, surrogate pairs), not as `w:sym` or `symEx`; *Insert > Symbol* of an astral character is plain text in this build |
| `w16sdtdh:storeItemChecksum` | the Title control's `w:dataBinding` (to the core-properties store item) carries `xpath` and `storeItemID` only; the checksum is evidently for user custom XML parts, not the package's own core properties |
| `w16sdtfl:formattingAllowed` | the locked control has `w:lock w:val="contentLocked"` and nothing else; the Properties dialog offered no formatting choice ("there was no allow-formatting choice", Jason's note in the document) |
| `w14:noSpellErr` | the checked paragraph has no attribute; this build does not persist the spelling state per paragraph |
| `cei:commentEntityInfo` | the comment's `commentExtensible` has `durableId` and `dateUtc` and no `extLst`; and this build does not even declare `xmlns:cei` (the repository's five 2026 documents that declare it came from another build) |

Every `mc:Ignorable` in the file is `w14 w15 w16se w16cid w16 w16cex
w16sdtdh w16sdtfl w16du wp14` (`commentsExtensible.xml` adds `cr`), so all
three attribute-only prefixes are declared on every part even though
nothing uses them - which is why the prefix table already had them. No
tracked change and no numbering in the file, so `dateUtc`,
`restartNumberingAfterBreak` and `durableId` are exercised by the
repository's own documents, as §1 measured.

**Gate of phase 0 met, with the fallback**: the host list of §2 stands as
written; the five unmeasured extensions are admitted **on the specification's
word** (§13), their accessors covered by unit tests that build the objects
and marshal them, not by a fixture; the three measured ones by the
round-trip probe on the existing fixtures. The file is not committed (it
exercises nothing the repository's documents do not) and stays on the share
as this phase's record. Phase 1 may proceed at Jason's go.

## 17. Phase 1: the schemas, references, regeneration, context, prefix, tests (2026-09-20, at Jason's go)

Coded:

- `xsd/wml/w16sdtdh.xsd`, `w16du.xsd`, `w16sdtfl.xsd`, `cei.xsd`, each from
  its [MS-DOCX] 23.0 section page (headers cite the page; the pages'
  non-breaking spaces replaced; packages `org.docx4j.w16sdtdh`, `.w16du`,
  `.w16sdtfl`, `.cei`). `xsd/ROOT.xsd` imports the four.
- **Two older copies were behind the current revision** (found by the
  regeneration, "Cannot resolve the name"): `w15_word_2012_wordml.xsd` (from
  the 2012 text) lacked `restartNumberingAfterBreak`, and `w16cid.xsd` lacked
  the `decimaldurableId` attribute group and the global `durableId`
  attribute. Both added from the 23.0 pages, recorded in each file. The
  inventory's "bound" for 5.2 and 5.4 was true of the packages, not of every
  declaration.
- `wml.xsd`: imports of `w16se`, `w16cid`, `w16sdtdh`, `w16du`, `w16sdtfl`
  (the first two had been imported only by `ROOT.xsd`), and the seven
  references of §2: `w16du:dateUtc` on `CT_TrackChange` (so on its sixteen
  extensions - every `ins`, `del`, `*PrChange`, `comment`, the math run
  changes of CR-010 included), `w15:restartNumberingAfterBreak` on
  `abstractNum`'s type, `w16cid:decimaldurableId` on `num`'s,
  `w14:noSpellErr` on `CT_P`, `w16sdtdh:storeItemChecksum` on
  `CT_DataBinding`, `w16sdtfl:formattingAllowed` on `CT_SdtPr`,
  `w16se:symEx` in `EG_RunInnerContent` after `sym`.
- Generated: `CTTrackChange.getDateUtc()` (`XMLGregorianCalendar`),
  `Numbering.AbstractNum.getRestartNumberingAfterBreak()` (`String`, as
  docx4j binds `ST_OnOff`), `Numbering.Num.getDurableId()` (`BigInteger`),
  `P.getNoSpellErr()`, `CTDataBinding.getStoreItemChecksum()`,
  `SdtPr.getFormattingAllowed()`, `org.docx4j.w15symex.CTSymEx` as a member
  of `R.getContent()` (and of the math run track change's), and package
  `org.docx4j.cei` (`CTCommentEntityInfo`, `ObjectFactory`). The three
  attribute-only packages generate nothing, as §3 said.
- `module-info.java`: `org.docx4j.cei` exported and opened; `Context.jc`:
  `org.docx4j.cei` appended; `NamespacePrefixMappings`: `cei` both ways.
- `org.docx4j.wml.WordExtensionAttributesTest` (4 tests): the round trip
  (every part forced to unmarshal) keeping `dateUtc` 39 → 39 and `durableId`
  1 → 1 on `tracked-changes-equations.docx` with every `mc:Ignorable` prefix
  of `document.xml` and `numbering.xml` declared on the re-save, and
  `restartNumberingAfterBreak` 1 → 1 on `numPicBullet-word2019-pict.docx`;
  the typed accessors on those files (39 changes each with a `dateUtc`
  ending in `Z`, the numbering flag "0", a positive durable id); and the
  four extensions admitted on the specification's word (§16) built,
  marshalled with Word's prefixes and, for `symEx` and `commentEntityInfo`,
  unmarshalled back into the typed classes.
- CHANGELOG under "Schema (CR-023 ...)".

**Re-saves for the Word check** (share, `fidelity/cr023/`):
`tracked-changes-equations-cr023-resave.docx` (the 39 UTC dates now kept)
and `cr023-word-extensions-cr023-resave.docx` (Jason's phase 0 file through
docx4j). Jason: open both in Word 365; the gate's last row is "no repair
prompt, the tracked changes and comments intact".

**Gate:**

| step | result |
|---|---|
| docx4j-generated-objects + docx4j-core clean install | BUILD SUCCESS; installed jar md5 = target jar's |
| `WordExtensionAttributesTest` | 4 tests, 0 failures |
| docx4j-core-tests, the whole suite | 1218 tests, 0 failures, 11 skipped (2026-09-20) |
| docx4j-export-fo-tests, the whole suite (`CT_P` and `CT_TrackChange` are on every exporter's path; export-fo clean-installed against the new core first) | 669 tests, 0 failures, 8 skipped (2026-09-20) |
| Word 365 opens the two re-saves | Jason |

