/**
 * Word-style layout for docx4j's PDF output via the docx4j FO renderer (Enterprise CR-001 phase 5; CR-071).
 *
 * <p>Word breaks lines greedily: each line takes every word that fits and breaks at the last opportunity before
 * the first word that does not.  FOP uses Knuth-Plass total-fit, which optimises over the whole paragraph, so with
 * identical fonts and widths the two break about a quarter of ragged-right lines differently, and every later line
 * and page moves.  Word's line breaking, line placement, list items, citations, tabs and leaders, hyphenation zone
 * and GDI rounding are the docx4j FO renderer's, in its own layout managers (its hook {@code word-layout}); until
 * 17.3.6 this package carried a copy of FOP's layout managers for them.</p>
 *
 * <p>What stays here: {@link org.docx4j.fop.wordlayout.WordLayoutCustomizer}, which docx4j-export-fo loads through
 * ServiceLoader, turns the renderer's Word layout on and hands it the docx4j.convert.out.fo.wordLayout.* settings;
 * and {@link org.docx4j.fop.wordlayout.WordLayoutElementMapping}, the names of the attributes docx4j writes for it.
 * On by default; docx4j.convert.out.fo.wordLayout=false restores plain FOP layout, which is also what Apache FOP
 * gives.  See README-word-layout.md.</p>
 */
package org.docx4j.fop.wordlayout;
