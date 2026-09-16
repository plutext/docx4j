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

	/** 4.15pt at 9pt is 0.46 em and merges; a whole line pitch does not. */
	@Test
	public void piecesFurtherApartThanHalfAnEmDoNotMerge() {
		LayoutComparison.Result r = compare(refMerged(), candSplit(99.0, 167.3));
		assertEquals(1, r.matched);
		assertEquals(0, r.merged);
	}

	@Test
	public void piecesAtAnotherIndentDoNotMerge() {
		LayoutComparison.Result r = compare(refMerged(), candSplit(110.4, 185.3));
		assertEquals(1, r.matched);
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
