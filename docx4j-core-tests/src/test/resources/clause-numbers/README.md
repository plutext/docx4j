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

## Results

Not yet run (staged on the share 3 November 2026 for Jason's run). Word's saves, `cn-1-labels-word365.docx` and
`cn-2-insert-word365.docx`, and `clause-numbers-log.txt` come back here; the table of Word's readings goes here.

docx4j 17.3.6-SNAPSHOT's list labels for cn-1 (the text model, `list_labels: true`), read before the run: L 1. /
1.01 / 1.02 / 2. / 2.01 / (a) / (i) / (ii) / (b) / 2.02 / (a) / 3.; A Article I / Section 1.01 / (a) / (b) / Article
II / Section 2.01 / (a) / Section 2.02; S 1. / (a) / (i) / 1.02 / (i); R 1. / (a) / 2. then 1. / (a); N4 NOTE, N5 none;
T counted in the accepted view (T2 not counted: T1 1., T3 (a), T4 2.). Each label is followed by the level's suffix
(a tab).
