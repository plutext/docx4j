/**
 * Word-style line breaking for docx4j's PDF output via Apache FOP (CR-001
 * Phase 5).
 *
 * Word breaks lines greedily: each line takes every word that fits and breaks
 * at the last opportunity before the first word that does not.  FOP uses
 * Knuth-Plass total-fit, which optimises over the whole paragraph, so with
 * identical fonts and widths the two break about a quarter of ragged-right
 * lines differently, and every later line and page moves.
 *
 * FOP exposes no first-fit option and its line manager's breaking algorithm
 * is a private inner class, so this package carries a copy of FOP 2.11's
 * {@code LineLayoutManager} ({@link org.docx4j.fop.wordlayout.WordLineLayoutManager},
 * Apache License 2.0) whose breaking loop is greedy, a
 * {@link org.docx4j.fop.wordlayout.WordBlockLayoutManager} that creates it, and
 * a {@link org.docx4j.fop.wordlayout.WordLayoutManagerMaker} that FOP is given
 * through {@code FopFactoryBuilder.setLayoutManagerMakerOverride}.  The copy
 * is tied to FOP 2.11 internals and must be re-derived when FOP is upgraded;
 * the class comment says what was changed.
 *
 * These classes are part of docx4j-export-fo (there is no separate jar) and
 * are on by default: docx4j-export-fo loads
 * {@link org.docx4j.fop.wordlayout.WordLayoutCustomizer} through ServiceLoader.
 * docx4j.convert.out.fo.wordLayout=false restores plain FOP layout.
 * See README-word-layout.md.
 */
package org.docx4j.fop.wordlayout;
