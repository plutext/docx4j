# What a golden measures: the basis, the fonts, and three classes of layout rule

Recorded 2026-09-18, after CR-001 batch 47 and the Aptos findings of the same day. This is
the frame the layout rules in `docx4j-export-fo/docs/word-layout-rules.md` were derived in,
stated so that a rule is read against the evidence that can support it.

## 1. The two facts every reading rests on

**The reference is Word's PDF of Word's own re-save, not of the input docx.** The golden is
cut from docx4j's re-save of the original, and `resaved` beside it is Word's save of that
package; Word's rendering is invariant under its own re-save (README, "Is Word's rendering
invariant under its own re-save?"), and the re-save normalises styles, numbering, the
`w:tblGrid` and `w:compat`, which takes docx4j's interpretation of ambiguous input out of the
comparison. The README says so ("What to score: the re-saved docx, not the corpus file").
One correction on our side: every batch baseline from b71 was in fact cut from the `corpus`
directory, not `resaved` - the batch 47 agent measured the two bases apart (`real` has 194
rows on one and 191 on the other, and two page-count discriminators) - and the two are not
comparable. The next re-baseline should be on `resaved`, and a scoreboard should say which
basis it is on.

**Word's PDF depends on the fonts Word had.** Word lays a document out in the fonts installed
on the machine that renders it, and a cloud font in the machine's `FontCache/CloudFonts`
folder counts as installed only while the "connected experiences" setting is on. Measured on
one document in one day: with the setting off Word set Aptos in Calibri and Aptos Display in
Arial Bold (the fallback for a family it treated as absent), and produced a 27-page PDF; with
it on Word set the body in Aptos, still put Aptos Display in Arial Bold, and produced a
different 27-page PDF whose every line broke elsewhere. Both are Word's output. Only the
second is Word's layout of the document's fonts.

## 2. Three classes of rule

A layout rule derived from a golden belongs to one of three classes, and the class says what
evidence can confirm or refute it.

**Class 1 - rules that hold whatever the fonts.** Keeps and widows, the band two anchored
drawings share, which tab stop a numbering tab reaches, toggle properties across style
levels, a shape wholly off the paper, the width of an unresolved page-number citation,
Word's 1/300-inch leader grid, when a table grid is recomputed. The rule's statement does
not mention a font. Its measured *effect* does: every one of them acts through line boxes
whose height and width come from fonts, so the same rule can gain on one machine's corpus
and lose on another's. Such a rule is settled on a probe, whose fonts are under our control
and present on both machines, and only confirmed on the corpora. That is how the batches
already work; this class exists to say why.

**Class 2 - rules that hold when docx4j has the fonts Word had.** The line box from the
font's own tables - usWin, or the typo box where the font sets USE_TYPO_METRICS, in an OS/2
table of any version, or 1.3 x the usWin box for an East Asian face - kerning, advance
widths, the leader step in the label's face at the label's size, the bold face's own extents.
Here docx4j can be exact, and a residue is a defect in docx4j. A class 2 rule is settled on a
probe whose fonts both machines have (the probe set's Liberation, Carlito and DejaVu faces,
or a Microsoft face on the VM supplied to docx4j through `-Dfidelity.fonts`), and a document
whose fonts docx4j lacks is not evidence for or against it.

**Class 3 - rules for when docx4j lacks a font Word had.** These have two targets which must
not be confused:

* *3a, approximating Word-with-the-font.* The document font's vertical metrics from
  `word-line-metrics.properties`, so that the line height follows the font Word laid out in
  even when a substitute draws the glyphs; a metric-compatible clone for the widths (Carlito,
  Caladea, Arimo, Tinos, Cousine); per-face width factors; the weight kept through a
  substitution. The target is what Word produced *with* the font. A golden cut on a machine
  that had the font measures this target.
* *3b, reproducing Word's own substitution.* Which face Word falls back to - Calibri for
  Aptos by the document's `w:altName`, Arial Bold for Aptos Display, Sylfaen for 9919's
  family - and what it does to the weight there. A golden cut on a machine that lacked the
  font measures only this. A 3b reading is evidence about Word, and it transfers to a user
  only if that user's Word substitutes the same way; it is not a rule for docx4j to
  implement, and it must not be taken as a class 2 or 3a measurement.

Where no metric-compatible clone exists (Aptos, Consolas, today), class 2 fidelity is
unreachable horizontally: 3a can make the line height right and cannot make the line breaks
right. Such a document is measured with the real faces supplied to the harness, or scored on
pagination and vertical position only. Scored on line parity against a Carlito render, it
measures the clone, not the exporter.

## 3. The corpus goldens are a mix of classes 2 and 3b, per document

The VM had most fonts and lacked some (9919's family, drawn in Sylfaen, is one). So one
scoreboard holds documents whose golden is Word's layout of their own fonts and documents
whose golden is Word's substitution, and a rule gated on the corpus mean is gated on both at
once. The class of each document is computable from what the harness already records:
`golden-manifest.properties` lists the faces Word embedded in each golden, and the docx's
`w:fontTable` lists the faces it names. A document is class 2 where every named family has
a face of its own in the golden, and class 3b where Word embedded something else for one of
them - and, separately, docx4j's side is 3a where a named family is absent here but was
present on the VM (`FontsAnalysis` already says which). That belongs in the scoreboard as a
column, so that a document's parity is read against its class and a 3b document is never
counted as evidence for a class 2 rule.

## 4. Machine state beyond fonts

Two settings of the rendering machine decide a golden as much as its fonts do, and the
manifest should record both: whether proofing tools for the document's languages were
installed (they decide hyphenation; the corpora were cut without, hence
`-Dfidelity.hyphenate=false`), and whether connected experiences were on (they decide
whether the cloud fonts counted). Whether the existing goldens were cut with the second on is
not recorded and should be checked before the next re-cut; a document whose golden shows a
substitution for a family that is in the cloud-font cache is the sign that it was off.

## 5. What follows for the programme

* Re-baseline on `resaved`, and label every scoreboard with its basis.
* Add the per-document class column (section 3) to `score`, from the two manifests.
* Record proofing and connected-experience state in the golden manifest at cut time; check
  the runner's setting.
* Gate class 1 and class 2 rules on probes; use the corpora to confirm, reading a mover
  against its class.
* Treat a 3b reading as a note about Word, never as a docx4j rule; keep 3a rules (the
  metrics table, the clones, the width factors) as the way to approach Word-with-the-font.
* For documents in families with no clone, supply the faces to the harness or score
  pagination only.
