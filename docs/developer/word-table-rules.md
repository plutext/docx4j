# Word's table rules

What Word requires of a table's markup beyond the schema, as measured, and where docx4j
enforces each rule. A file that breaks one opens in Word as "unreadable content"; Word offers
a repair, and the repair's shape is recorded here too, since docx4j's own fixes follow it.

Measurements are Word 365 (Jason's machine, `Office 2016` share), on probe files docx4j wrote.
A rule nobody has measured is marked so. Add a row, with its probe and date, when a new one is
measured; a port's or the editor's finding goes here as well, with the repository named.

## 1. A cell holds at least one block

`w:tc` holds one or more block-level elements (`w:p`, `w:tbl`, a block-level `w:sdt` or
`w:customXml`, an `mc:AlternateContent`). The schema says so (`CT_Tc`, `EG_BlockLevelElts`
`minOccurs="1"`); range markup alone (`w:bookmarkEnd` and the like) does not count.

## 2. A cell's last block, as Word reads it, is a paragraph

Word's own rule, beyond the schema. The cell's last block must be a `w:p`. Word finds that
last block **as it reads the cell**:

- it looks through block-level content controls (`w:sdt`), one or two deep, so a control whose
  content ends in a paragraph satisfies the rule, and one whose content ends in a table does not;
- it reads one branch of an `mc:AlternateContent` (the Choice whose `Requires` it understands,
  else the Fallback) and applies the rule to that branch's last block; **the branches it does not
  read are not checked**.

A cell ending in a nested table is the usual way to break it.

Measured 2026-10-21 (one probe, the editor's file) and 2026-10-22 (twelve probes, listed below).

| Probe (the cell's content; "nested" is a two-cell table) | Word |
|---|---|
| 01 nested, then `w:p` | opens |
| 02 nested, nothing after | unreadable |
| 03 nested, then `w:sdt` > `w:p` | opens |
| 04 nested, then `w:sdt` > `w:sdt` > `w:p` | opens |
| 05 nested, then `w:sdt` > nested | unreadable |
| 06 nested, then `mc` (Choice `w14`: `w:p`; Fallback: `w:p`) | opens |
| 07 nested, then `mc` (Choice unknown prefix: `w:p`; Fallback: `w:p`), the Fallback read | opens |
| 08 nested, then `mc` (Choice `w14`: `w:p`; Fallback: nested), the Fallback not read | opens |
| 09 nested, then `mc` (Choice `w14`: nested; Fallback: `w:p`), the Choice read | unreadable |
| 10 `w:sdt` > `w:p` alone | opens |
| 11 `mc` (`w:p`; `w:p`) alone | opens |
| 12 `w:sdt` > nested alone | unreadable |

**Word's repair:** an empty `w:p` appended to the cell, **after** any wrapper and never inside it
(probe 12's repair keeps the `w:sdt`, with an empty `w:sdtEndPr`, and puts the paragraph after
it). Where the unwrapping left two tables adjacent (probes 05 and 09), Word merged them into one
table of two rows and dropped the control or the element (an `mc:AlternateContent` is resolved
to the branch read on any save).

**A related edit-time rule:** Word refuses to delete a cell's last paragraph mark, and a mark
before a table (CR-043, probes C08 and C09), so no Word file records such a deletion.

**Where docx4j enforces it:**

- `org.docx4j.model.table.TableRules`: `endsWithParagraph` decides as Word reads (through
  controls and custom XML, the read branch of an `mc:AlternateContent` by the text model's
  selection, CR-051 section 10.3); `endWithParagraph(Tc)` does Word's repair, once, and nothing
  when the rule holds. New code that builds or rewrites cells calls it before saving.
- `UnequalColumns` (the one-row table a section of unequal columns becomes in PDF output) ends
  each column's cell with it.
- OpenDoPE: `OpenDoPEIntegrity.xslt` and `RemovalHandler.xslt` each have a `w:tc` template and a
  `last-block-is-p` named template doing the same in XSLT 1.0 (an `mc:AlternateContent` counts
  there only when every branch ends in a paragraph, since the stylesheet does not know which branch
  Word reads; the spare paragraph that can give is harmless). Until 17.3.4 both looked for a
  paragraph anywhere in the cell, so a cell ending in a control holding a table passed, and the
  integrity step padded every control in a cell with no paragraph inside it.
- `OpenDoPEHandler.emptyKeepingTableShape` empties a cell control to a cell holding one paragraph
  (CR-051 phase 2).

Tests: `TableRulesTest` (the twelve probes), `WordTableRulesOpenDoPETest`, `UnequalColumnsTest`.

**Elsewhere in the family:** the TypeScript editor's check (ED-005 section 12.109) walks the cell's
last child through block-level controls and appends the paragraph after the control, since
2026-10-22; a block-level `mc:AlternateContent` in a cell is opaque to it until its ED-009 phase
P9 projects the read branch. Probes 10 and 12, with 12's repair, are its fixtures.

## 3. A row holds at least one cell

`w:tr` must hold a `w:tc`, wherever that cell stands: directly, in a cell-level control or custom
XML element, or in a branch of a cell-level `mc:AlternateContent`. The schema allows zero
(`EG_ContentCellContent` `minOccurs="0"`); Word does not.

**Not measured on a probe here**; the rule is docx4j's working rule of long standing
(`RemovalHandler.xslt` removes a row left with no cell, and `OpenDoPEIntegrity.xslt` gives a
row-level control with no cell one; CR-051 phase 2 made both count cells in every branch). A
probe would be a `w:tr` holding only `w:trPr`.

## 4. A table may have no rows

`w:tbl` with `w:tblPr` and `w:tblGrid` and no `w:tr` opens. The schema allows it
(`EG_ContentRowContent` `minOccurs="0"`). docx4j's revisions model removes a table only when no
branch of a row-level `mc:AlternateContent` has a row left (CR-051 phase 2), on this reading.

**Not measured on a probe here**; recorded as the working rule.

## Changing this file

When a rule is measured or corrected, update the row and the date, and tell the sessions whose
code follows it: the TypeScript editor (ED-005), docx4j-core-ts and docx4j-python (which treat
docx4j as their behavioural oracle), and the portfolio registry if a CR's status changes.
