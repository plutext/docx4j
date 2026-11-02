# The clause-numbers probe (CR-074 phase 0)

What Word shows for a numbered paragraph as a cross-reference, and what its own Insert Cross-reference writes: the
oracle for CR-074's full-context label (the clause, `clause:2.01(a)`) and for REF's paragraph-number switches. Built
by `build_clause_numbers.py` (run from this directory: `python3 build_clause_numbers.py .`; a rebuild is
byte-identical); Word driven by `clause-numbers-probe.ps1` (`RUN.txt`).

## cn-1-labels.docx

Every numbered paragraph starts with its id (L6), and the id is bookmarked `cn_<id>`. The REF fields over the
bookmarks hold a stale `?` until Word updates them:

- **field lines** after the lists, one a numbered paragraph (`F-L6`): `\w`, `\n`, `\r`, `\w \t`, `\n \t`, `\r \t`;
  for L6, L9, A3 and A7 also `\p`, `\r \p`, `\w \d "-"` and `\w \h`. A field line is not numbered and comes after
  every list, so its `\r` is relative context from outside the lists;
- **"r:" fields inside numbered paragraphs**: `\r` from that paragraph to others of its case (L8, L9, L11 and L12 to
  each of L1 to L12), and L9 also `\w` to three (is `\w` the same wherever it stands?); O2 cites three of case L
  (relative context from another list); F0, before the lists, cites five of L.

| Case | Lists | Asks |
|---|---|---|
| L (L1-L13) | 1. / 1.01 (`%1.%2`, decimalZero) / (a) / (i) | the legal shape: is (a) under 2.01 `2.01(a)` in full context; what is dropped (the period of `1.`); relative context between siblings, cousins and levels; L13 in a table cell |
| O (O1-O7) | 1. / a. / i., each level its own counter | the join where no level shows the levels above, each ending in a period (`1.a.i`? `1ai`?) |
| P (P1-P7) | 1) / a) / i) | the same with a parenthesis |
| H (H1-H7) | 1. / 1.1. / 1.1.1. | levels that show the levels above already: no repetition? the last period? |
| A (A1-A8) | Article I / Section 1.01 (`w:isLgl`) / (a), each level linked to a heading style | style-linked levels; words in the level text (`Article III`), and `\t` (numbers only) |
| Y (Y1-Y4) | 1. / 1.1 through a list style (`w:numStyleLink`) | the list-style form |
| G (G1-G5) | I. / 1.1 (`w:isLgl`) / (a) | whether legal numbering of level 1 carries into a deeper level's context; G5 skips level 1 |
| S (S1-S5) | case L's levels, levels skipped | the context of a level whose parent level never appeared |
| R (R1-R5) | 1. / (a), restarted by a second `w:num` (start override) | two paragraphs showing `1.` and `1(a)`: the lookup's ambiguity |
| M (M1-M5) | Article I / 1. (`%2.`, no `%1`) / (a) | a level that does not show the level above, under words |
| N (N1-N8) | a bullet; Step 1: / a.; NOTE (format none); an empty level text; 1 / 1-1 / A | not numbers: what `\w` gives for a bullet, a label, an empty level; a colon; no punctuation |
| T (T1-T5) | 1. / (a), T2 a tracked deleted paragraph, T3 a tracked inserted one | which view Word counts in (all markup, then no markup, then the original) |
| E1-E3 | bookmarks | E1 on a paragraph not numbered; E2 empty, before L10's id; E3 from L6's text into L7's |

## cn-2-insert.docx

The same lists, no bookmarks or fields. The script has Word insert its own cross-references (Reference type
"Numbered item"): X1 to L6, L9, L11, A3, A6, A7, R4, M3, N2, G3, S2 and Y2 in full, no and relative context as
hyperlinks; X2 full context not a hyperlink; X3 relative with above/below; X4 full context with numbers separated by
`-`; X5 the paragraph text; X6 a second reference to L6; and inside L9 and L11, relative context to their neighbours.
What it answers: the field code Word writes for each choice (the switches, their order, `\h`, any `\* MERGEFORMAT`),
the hidden `_Ref` bookmark (its name, where it starts and ends, whether a second reference reuses it), and the result
Word stores.

## Results (Jason's run, 3 November 2026, Word 365 build 16.0.20326)

All steps ran; `Fields.Update` returned 0, every one of the 655 fields has a result, and the run is in
`clause-numbers-log.txt` (each paragraph's drawn label, the dialog's numbered items, every field's code and result,
the T views, Word's insertions and bookmarks). Word's saves: `cn-1-labels-word365.docx`, `cn-2-insert-word365.docx`.

**Labels.** Word draws the same label as docx4j for all 79 paragraphs (`ListFormat.ListString`), S4's `1.02` included.

**Full context (`\w`).** A paragraph's full context is its own label (the level text, counters filled in) after
its parent's full context. The parent is the nearest earlier paragraph of the same list (`w:abstractNum`) at a
shallower level. A level whose text already shows a level above (`%1.%2`) has no parent part. A level skipped in the
document contributes nothing. Then:

- a period ending a part is dropped when the next part starts with `(`, and kept before a letter (`1.a`); before a
  digit or other punctuation it is unmeasured;
- one trailing period at the end is dropped;
- other characters (`)`, `:`, `-`) are kept.

| Shape | Word's `\w` |
|---|---|
| 1. / 1.01 / (a) / (i) | `2.01(a)`, `2.01(a)(i)`; `2.02`; `3` |
| 1. / a. / i. | `1.a`, `1.a.i`, `2.a` |
| 1) / a) / i) | `1)a)`, `1)a)i)`, `2)a)` |
| 1. / 1.1. / 1.1.1. | `1.1`, `1.1.1` (no repetition) |
| Article I / Section 1.01 (isLgl, style-linked) / (a) | `Article I`, `Section 1.01`, `Section 1.01(a)` |
| list style 1. / 1.1 | `1`, `1.1` |
| I. / 1.1 (isLgl) / (a) | `I`, `1.1`, `1.1(a)`; G5 (level 1 skipped under II.) `II(a)` |
| skipped levels | S2 `1(a)`, S3 `1(a)(i)`, S5 (level 2 skipped under 1.02) `1.02(i)` |
| restarted | R1 and R4 both `1`, R2 and R5 both `1(a)` |
| Article I / 1. (`%2.`) / (a) | `Article I1`, `Article I1(a)`: joined with nothing between |
| Step 1: / a. | `Step 1:`, `Step 1:a` |
| 1 / 1-1 / A | `1-1`, `1-1A` |
| a bullet | the bullet character (U+F0B7) |
| NOTE (format none) | `NOTE` |
| empty level text (format none) | `0` |
| a bookmark on a paragraph not numbered (E1) | `0` |
| an empty bookmark (E2); one from L6 into L7 (E3) | the paragraph the bookmark starts in: `2.02`; `2.01(a)` |

**No context (`\n`)**: the paragraph's own part, with the trailing period dropped (`(a)`, `2.01`, `Section 1.01`,
`a`, `1.1.1`, `Step 1:`); a bullet gives its character, an empty level `0`.

**Relative context (`\r`)**: the full context's parts, without the leading parts that belong to paragraphs which
are ancestors of the reference's position (the paragraph itself is always shown). The position is the field's
own paragraph when that paragraph is in the target's list; otherwise it is the last paragraph of the target's list
before the field. With no such paragraph, nothing is dropped. The parts are matched by paragraph, not by value: R2
cited after R5 is `1(a)`, though R4 shows `1.` too. Examples: in L9 (2.01(b)), L6 is `(a)`, L7 `(a)(i)`, L11
`2.02(a)` and L5 `2.01`. After all the lists, O7 is `a`, S5 `(i)` and S2 `(a)`, while L6 is still `2.01(a)` (L's last
paragraphs are under 3.). Before any list, every reference is full.

**`\t`**: the level texts' words go, with their spaces; the numbers and the punctuation stay. `Article I` gives `I`,
`Section 1.01(a)` gives `1.01(a)`, `Article I1(a)` gives `I1(a)` and `Step 1:` gives `1:`. An empty result is `0`
(a bullet, `NOTE`).

**`\p`** appends ` above` or ` below`: `2.01(a) above`. **`\d "-"`** puts the separator between the parts:
`2.01-(a)`, `Section 1.01-(a)`. **`\h`** changes nothing in the result.

**Tracked changes (case T).** The results are the accepted view's in all three views (all markup, no markup,
original): T4 is `2`, so the deleted paragraph T2 is not counted. T2's bookmark resolves to the paragraph its
deleted mark merges into, T3: `1(a)`.

**Word's Insert Cross-reference (cn-2)**:

- *Bookmark.* A hidden `_Ref` followed by nine digits (`_Ref244594912`, numbered in sequence). It runs from the
  start of the paragraph's text to its end, without the label or the mark.
- *Reuse.* A later reference to the same paragraph reuses that bookmark while it still spans the whole text. L9's
  did not after text was inserted into L9, so Word made a second one.
- *Field code.* ` REF _Ref244594912 \w \h ` (likewise `\n \h`, `\r \h`, `\r \p \h`, `\w \h \d "-"`), with
  no `\* MERGEFORMAT`. As a hyperlink it is a complex field (`w:fldChar`); without `\h` it is a
  `w:fldSimple`.
- *Result.* Word stores the result, computed at the insertion point (relative context included).
- *Dialog list.* `GetCrossReferenceItems` lists each paragraph by its own label, indented by level (`    (a) L6
  level 2`).
