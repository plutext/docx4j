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
 * The line-pitch statistic: the cells of a table row are consecutive extracted lines, and
 * the gap between them is not a line pitch however small or large it is.
 *
 * @see LayoutComparison#linePitch
 */
public class LinePitchTest {

	private static PdfLayout.Line line(int page, double y, double x0, double x1) {
		PdfLayout.Line l = new PdfLayout.Line();
		l.page = page;
		l.y = y;
		l.x0 = x0;
		l.x1 = x1;
		l.size = 10;
		l.text = "9.00-9.15";
		return l;
	}

	private static PdfLayout layout(PdfLayout.Line... lines) {
		PdfLayout out = new PdfLayout();
		out.pageWidths.add(595.3);
		out.pageHeights.add(841.9);
		for (PdfLayout.Line l : lines) out.lines.add(l);
		return out;
	}

	/** A single column: every consecutive pair is stacked, so every gap counts. */
	@Test
	public void aColumnOfLinesIsItsOwnPitch() {
		PdfLayout l = layout(line(0, 100, 72, 300), line(0, 112, 72, 300), line(0, 124, 72, 300),
				line(0, 136, 72, 300));
		assertEquals(12.0, LayoutComparison.linePitch(l), 0.001);
	}

	/**
	 * Three rows of three cells at a 12pt pitch.  Six of the eight consecutive pairs are
	 * cells of one row, so the median over all of them is the <b>cell</b> gap; over the
	 * stacked pairs it is the row pitch.
	 */
	@Test
	public void theCellsOfARowAreNotAPitch() {
		PdfLayout l = layout(
				line(0, 100.00, 72, 150), line(0, 100.25, 200, 280), line(0, 100.50, 330, 400),
				line(0, 112.00, 72, 150), line(0, 112.25, 200, 280), line(0, 112.50, 330, 400),
				line(0, 124.00, 72, 150), line(0, 124.25, 200, 280), line(0, 124.50, 330, 400));
		assertEquals("11.50 from the last cell of a row to the first of the next",
				11.5, LayoutComparison.linePitch(l), 0.001);
	}

	/**
	 * The exclusion is the geometry and not a threshold: a row whose cells sit well over
	 * a point apart - which is what a document with a tall cell beside a short one does,
	 * on Word's side as well as ours - is still a row.
	 */
	@Test
	public void aRowWhoseCellsAreFarApartIsStillARow() {
		PdfLayout l = layout(
				line(0, 100.0, 72, 150), line(0, 106.0, 200, 280),
				line(0, 120.0, 72, 150), line(0, 126.0, 200, 280),
				line(0, 140.0, 72, 150), line(0, 146.0, 200, 280));
		assertEquals("14.00 between the rows, not the 6.00 inside them",
				14.0, LayoutComparison.linePitch(l), 0.001);
	}

	/** A pair that straddles a page break is no pitch either. */
	@Test
	public void aPageBreakIsNotAPitch() {
		PdfLayout l = layout(line(0, 700, 72, 300), line(0, 712, 72, 300));
		PdfLayout.Line onward = line(1, 100, 72, 300);
		l.pageWidths.add(595.3);
		l.pageHeights.add(841.9);
		l.lines.add(onward);
		assertEquals(12.0, LayoutComparison.linePitch(l), 0.001);
	}

	/** A cell that begins exactly where the one before it ends is beside it, not below. */
	@Test
	public void cellsThatTouchAreStillSideBySide() {
		PdfLayout l = layout(line(0, 100.0, 72, 150), line(0, 101.0, 150, 280),
				line(0, 112.0, 72, 150));
		assertEquals(11.0, LayoutComparison.linePitch(l), 0.001);
	}

	/** No stacked pair at all, and the statistic says nothing rather than something false. */
	@Test
	public void oneRowAloneHasNoPitch() {
		PdfLayout l = layout(line(0, 100.0, 72, 150), line(0, 100.25, 200, 280));
		assertEquals(0.0, LayoutComparison.linePitch(l), 0.001);
	}
}
