/*
 *  Copyright 2026, Plutext Pty Ltd.
 *
 *  This file is part of docx4j.

    docx4j is licensed under the Apache License, Version 2.0 (the "License");
    you may not use this file except in compliance with the License.
    You may obtain a copy of the License at

        http://www.apache.org/licenses/LICENSE-2.0

    Unless required by applicable law or agreed to in writing, software
    distributed under the License is distributed on an "AS IS" BASIS,
    WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
    See the License for the specific language governing permissions and
    limitations under the License.

 */
package org.docx4j.fidelity.compare;

import static org.junit.Assert.assertEquals;

import org.junit.Test;

import org.docx4j.fidelity.extract.PdfLayout;

/**
 * The merging pass: a line one extractor read as one pairs with the two the other
 * read in the same place, and refuses to when any of its four guards fails.
 *
 * <p>The shape is a corpus document's list bullet: Word puts the bullet on its text's
 * baseline and reads {@code "&#x2022; Physical access"} as one line, we set the bullet
 * 4.15pt above at 9pt and read it as two.
 */
public class LayoutComparisonMergeTest {

	private static PdfLayout.Line line(int page, double y, double x0, double x1, String text) {
		PdfLayout.Line l = new PdfLayout.Line();
		l.page = page;
		l.y = y;
		l.x0 = x0;
		l.x1 = x1;
		l.size = 9;
		l.text = text;
		return l;
	}

	/** One page of lines. */
	private static PdfLayout layout(PdfLayout.Line... lines) {
		PdfLayout out = new PdfLayout();
		out.pageWidths.add(595.3);
		out.pageHeights.add(841.9);
		for (PdfLayout.Line l : lines) out.lines.add(l);
		return out;
	}

	/** An anchor pair, identical on both sides, so the page map has evidence to work from. */
	private static PdfLayout.Line anchor(double y) {
		return line(0, y, 71.0, 130.0, "Prerequisites");
	}

	private static LayoutComparison.Result compare(PdfLayout.Line[] ref, PdfLayout.Line[] cand) {
		return LayoutComparison.compare("t", layout(ref), layout(cand));
	}

	private static PdfLayout.Line[] refMerged() {
		return new PdfLayout.Line[] { anchor(100), line(0, 110.4, 167.1, 515.0, "• Physical access") };
	}

	private static PdfLayout.Line[] candSplit(double bulletY, double bulletX) {
		return new PdfLayout.Line[] { anchor(100), line(0, bulletY, bulletX, bulletX + 5.3, "•"),
				line(0, 114.6, 185.3, 458.8, "Physical access") };
	}

	@Test
	public void referenceLinePairsWithTheTwoWeRead() {
		LayoutComparison.Result r = compare(refMerged(), candSplit(110.4, 167.3));
		assertEquals(2, r.refLines);
		assertEquals(3, r.candLines);
		assertEquals("both reference lines pair", 2, r.matched);
		assertEquals(1, r.merged);
		assertEquals(0, r.refOnly.size());
	}

	@Test
	public void candidateLinePairsWithTheTwoWordRead() {
		LayoutComparison.Result r = compare(candSplit(110.4, 167.3), refMerged());
		assertEquals(3, r.refLines);
		assertEquals("the merged candidate line is worth the two reference lines it holds", 3, r.matched);
		assertEquals(1, r.merged);
	}

	/** 4.15pt at 9pt is 0.46 em and merges; 15.6pt, more than a line pitch, does not. */
	@Test
	public void piecesMoreThanALinePitchApartDoNotMerge() {
		LayoutComparison.Result r = compare(refMerged(), candSplit(99.0, 167.3));
		assertEquals(1, r.matched);
		assertEquals(0, r.merged);
	}

	/**
	 * The bound that keeps the pass from hiding a displacement of nearly a whole line:
	 * a corpus check-box glyph we paint 9.86pt (0.99 em) above the label Word paints it
	 * beside is <b>stacked</b> - the label begins to the left of the glyph's own right
	 * edge - so it takes the half-em bound and does not merge, although a row that far
	 * apart would.
	 */
	@Test
	public void stackedPiecesTakeTheStackedBoundEvenWhenARowThatFarApartWouldMerge() {
		PdfLayout.Line[] ref = { anchor(100), line(0, 110.0, 67.71, 156.50, "x 9.00-9.15") };
		PdfLayout.Line[] cand = { anchor(100), line(0, 110.0, 67.71, 74.38, "x"),
				line(0, 119.86, 66.75, 156.50, "9.00-9.15") };
		LayoutComparison.Result r = compare(ref, cand);
		assertEquals("0.99 em apart and overlapping: stacked, so refused", 1, r.matched);
		assertEquals(0, r.merged);
	}

	@Test
	public void piecesAtAnotherIndentDoNotMerge() {
		LayoutComparison.Result r = compare(refMerged(), candSplit(110.4, 185.3));
		assertEquals(1, r.matched);
		assertEquals(0, r.merged);
	}

	/** A row of cells, each beginning after the one before it ends. */
	private static PdfLayout.Line[] rowOf(int n, double[] xs, double[] ys) {
		PdfLayout.Line[] out = new PdfLayout.Line[n + 1];
		out[0] = anchor(100);
		for (int i = 0; i < n; i++) {
			out[i + 1] = line(0, ys[i], xs[i], xs[i] + 38.5, "9.00-9.15");
		}
		return out;
	}

	private static PdfLayout.Line[] rowAsOneLine(int n) {
		StringBuilder t = new StringBuilder("9.00-9.15");
		for (int i = 1; i < n; i++) t.append(" 9.00-9.15");
		return new PdfLayout.Line[] { anchor(100), line(0, 121.78, 86.42, 681.13, t.toString()) };
	}

	/**
	 * The five-cell row of {@code 14_en-AU_tbl_174}'s page 1, which Word reads as one
	 * line at x 86.42..681.13 and our extractor reads as five: it needs
	 * {@code MERGE_MAX} of at least five, and its baselines span 2.25pt.
	 */
	@Test
	public void aFiveCellRowMerges() {
		double[] xs = { 86.25, 223.90, 358.20, 506.45, 642.05 };
		double[] ys = { 121.78, 123.79, 122.47, 123.79, 121.54 };
		LayoutComparison.Result r = compare(rowAsOneLine(5), rowOf(5, xs, ys));
		assertEquals(2, r.refLines);
		assertEquals(6, r.candLines);
		assertEquals("both reference lines pair", 2, r.matched);
		assertEquals(1, r.merged);
	}

	/** Six, the widest row measured on the corpora - a rating scale on
	 *  {@code 16_fr-CA_sdt_num_tbl_3640}, a six-cell heading on
	 *  {@code 12_en-US_sdt_fields1_num_tbl_4957} - merges. */
	@Test
	public void aSixCellRowMerges() {
		LayoutComparison.Result r = compare(rowAsOneLine(6), evenRow(6));
		assertEquals(2, r.matched);
		assertEquals(1, r.merged);
	}

	/** And the pass stops there: seven pieces are more than one line may claim, which is
	 *  what keeps the per-glyph letter-spaced diagram of
	 *  {@code 15_es-AR_sdt_num_tbl_12301} - single digits on one baseline, seven to
	 *  twenty of them - out of a pass meant for a row. */
	@Test
	public void aSevenCellRowDoesNotMerge() {
		LayoutComparison.Result r = compare(rowAsOneLine(7), evenRow(7));
		assertEquals(1, r.matched);
		assertEquals(0, r.merged);
	}

	/** n cells 60pt apart, alternating 2pt in baseline. */
	private static PdfLayout.Line[] evenRow(int n) {
		double[] xs = new double[n], ys = new double[n];
		for (int i = 0; i < n; i++) {
			xs[i] = 86.25 + i * 60.0;
			ys[i] = 121.78 + (i % 2) * 2.0;
		}
		return rowOf(n, xs, ys);
	}

	/**
	 * A row's cells share no baseline: two cells of one row 0.97 em apart - the
	 * letterhead of {@code 14_en-US_tbl_2564} and {@code 14_en-US_tbl_10224} - merge,
	 * where the same two stacked would not.
	 */
	@Test
	public void rowCellsNearlyAnEmApartMerge() {
		double[] xs = { 74.22, 419.34 };
		double[] ys = { 100.00, 107.29 };
		PdfLayout.Line[] cand = rowOf(2, xs, ys);
		cand[1].size = 7.5;
		cand[2].size = 7.5;
		PdfLayout.Line[] ref = { anchor(100), line(0, 100.0, 74.22, 510.60, "9.00-9.15 9.00-9.15") };
		LayoutComparison.Result r = compare(ref, cand);
		assertEquals("7.29pt at 7.5pt is 0.97 em, and they are a row", 2, r.matched);
		assertEquals(1, r.merged);
	}

	/** A full line pitch apart is refused however the cells sit. */
	@Test
	public void rowCellsAFullLinePitchApartDoNotMerge() {
		double[] xs = { 74.22, 419.34 };
		double[] ys = { 100.00, 109.00 };
		PdfLayout.Line[] cand = rowOf(2, xs, ys);
		cand[1].size = 7.5;
		cand[2].size = 7.5;
		PdfLayout.Line[] ref = { anchor(100), line(0, 100.0, 74.22, 510.60, "9.00-9.15 9.00-9.15") };
		LayoutComparison.Result r = compare(ref, cand);
		assertEquals("9.00pt at 7.5pt is 1.2 em", 1, r.matched);
		assertEquals(0, r.merged);
	}

	@Test
	public void piecesOnAnotherPageDoNotMerge() {
		PdfLayout cand = layout(anchor(100));
		PdfLayout.Line bullet = line(1, 110.4, 167.3, 172.6, "•");
		PdfLayout.Line text = line(1, 114.6, 185.3, 458.8, "Physical access");
		cand.pageWidths.add(595.3);
		cand.pageHeights.add(841.9);
		cand.lines.add(bullet);
		cand.lines.add(text);
		LayoutComparison.Result r = LayoutComparison.compare("t", layout(refMerged()), cand);
		assertEquals(1, r.matched);
		assertEquals(0, r.merged);
	}

	@Test
	public void textWhichDoesNotConcatenateDoesNotMerge() {
		PdfLayout.Line[] cand = candSplit(110.4, 167.3);
		cand[2].text = "Physical access to the equipment room";
		LayoutComparison.Result r = compare(refMerged(), cand);
		assertEquals(1, r.matched);
		assertEquals(0, r.merged);
	}
}
