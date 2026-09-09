package org.docx4j.fidelity.extract;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.util.ArrayList;
import java.util.List;

import org.junit.Test;

import org.docx4j.fidelity.extract.PdfLayout.Line;

/**
 * The line-number rule: Word's {@code w:lnNumType} numbers sit in the margin, left
 * of the document's text edge, flush right and increasing down the page; docx4j
 * cannot render them, so the harness drops them from the reference (a declared
 * floor, see the README) rather than losing every line of such a document.
 */
public class LineNumberNormaliseTest {

	/** A line as the extractor would assemble it: {@code numEnd} is the right edge of a
	 *  leading digit run, {@code rest} the x of the ink after it (NaN = number only). */
	private static Line line(int page, String text, double x0, double numEnd, double rest) {
		Line l = new Line();
		l.page = page;
		l.text = text;
		l.x0 = x0;
		l.x1 = x0 + 100;
		l.leadNumberEnd = numEnd;
		l.restX0 = rest;
		return l;
	}

	private static Line plain(int page, String text, double x0) {
		return line(page, text, x0, Double.NaN, Double.NaN);
	}

	private static PdfLayout layout(List<Line> lines) {
		PdfLayout out = new PdfLayout();
		out.lines.addAll(lines);
		return out;
	}

	private static List<String> texts(PdfLayout out) {
		List<String> texts = new ArrayList<>();
		for (Line l : out.lines) texts.add(l.text);
		return texts;
	}

	/** A body of prose at 85pt, so the document's edge is 85. */
	private static void body(List<Line> lines, int page, int n) {
		for (int i = 0; i < n; i++) lines.add(line(page, (i + 5) + " body text of the page", i + 5 < 10 ? 61.5 : 55.9, 67.1, 85.0));
	}

	@Test
	public void marginNumbersAreDroppedAsPrefixAndAsLines() {
		// Word's A4 page: numbers flush right at 67, text from 85; an empty numbered
		// paragraph is a number-only line; a footer page number is not in the zone
		List<Line> lines = new ArrayList<>();
		lines.add(line(0, "1 A centred title above the text", 61.5, 67.1, 159.5));
		lines.add(line(0, "2", 61.5, 67.1, Double.NaN));
		lines.add(plain(0, "An author line with no number of its own", 92.0));
		lines.add(line(0, "3 An affiliation", 61.5, 67.1, 85.0));
		lines.add(line(0, "10 Tenth line of the page", 55.9, 67.1, 85.0));
		lines.add(line(0, "11", 55.9, 67.1, Double.NaN));
		lines.add(line(0, "1", 504.4, 510.4, Double.NaN));
		body(lines, 1, 10);
		PdfLayout out = layout(lines);
		PdfLayoutExtractor.dropLineNumbers(out);
		assertEquals(List.of("A centred title above the text", "An author line with no number of its own",
				"An affiliation", "Tenth line of the page", "1"), texts(out).subList(0, 5));
		assertEquals(15, out.lines.size());
		assertEquals(159.5, out.lines.get(0).x0, 0.0);
		assertEquals(85.0, out.lines.get(2).x0, 0.0);
		assertEquals(504.4, out.lines.get(4).x0, 0.0);
		assertEquals("body text of the page", out.lines.get(5).text);
	}

	@Test
	public void tocEntryStartingWithItsRealNumberIsKept() {
		// the line number is the prefix; "1." after it is the heading's own number
		List<Line> lines = new ArrayList<>();
		lines.add(line(0, "9 1. Scope ...... 5", 31.9, 36.0, 54.0));
		lines.add(line(0, "10 2. Conformance ...... 5", 27.8, 36.0, 54.0));
		for (int i = 0; i < 5; i++) lines.add(plain(1, "prose", 54.0));
		PdfLayout out = layout(lines);
		PdfLayoutExtractor.dropLineNumbers(out);
		assertEquals(List.of("1. Scope ...... 5", "2. Conformance ...... 5"), texts(out).subList(0, 2));
	}

	/** Once the document has shown its numbers, a page with a single one loses it too. */
	@Test
	public void aShortLastPageIsStrippedOnceTheDocumentQualifies() {
		List<Line> lines = new ArrayList<>();
		body(lines, 0, 10);
		lines.add(line(1, "40 the last line", 55.9, 67.1, 85.0));
		PdfLayout out = layout(lines);
		PdfLayoutExtractor.dropLineNumbers(out);
		assertEquals("the last line", out.lines.get(10).text);
	}

	@Test
	public void numberedHeadingAtTheTextEdgeIsUntouched() {
		List<Line> lines = new ArrayList<>();
		lines.add(line(0, "1 Introduction", 72.0, 78.0, 90.0));
		lines.add(plain(0, "Body text at the margin.", 72.0));
		lines.add(line(0, "2 Scope", 72.0, 78.0, 90.0));
		PdfLayout out = layout(lines);
		PdfLayoutExtractor.dropLineNumbers(out);
		assertEquals(List.of("1 Introduction", "Body text at the margin.", "2 Scope"), texts(out));
	}

	/** A page that is one single-digit list: its labels are flush right by accident,
	 *  increase, and lie left of everything else on the page - and nothing in the
	 *  document shows two digit counts on one right edge. */
	@Test
	public void singleDigitListIsNotLineNumbers() {
		List<Line> lines = new ArrayList<>();
		for (int i = 1; i <= 9; i++) {
			lines.add(line(0, Integer.toString(i), 72.0, 77.5, Double.NaN));
			lines.add(plain(0, "item " + i + ", its text 0.5in in", 108.0));
		}
		PdfLayout out = layout(lines);
		PdfLayoutExtractor.dropLineNumbers(out);
		assertEquals(18, out.lines.size());
	}

	/** A list past 9 is left-aligned: 9 and 10 share a left edge, not a right one. */
	@Test
	public void leftAlignedListIsNotLineNumbers() {
		List<Line> lines = new ArrayList<>();
		for (int i = 8; i <= 12; i++) {
			lines.add(line(0, Integer.toString(i), 40.0, i < 10 ? 45.5 : 51.0, Double.NaN));
			lines.add(plain(0, "item " + i, 80.0));
		}
		PdfLayout out = layout(lines);
		PdfLayoutExtractor.dropLineNumbers(out);
		assertEquals(10, out.lines.size());
	}

	/** A right-aligned numeric column of a table, 1..24, flush right and increasing,
	 *  left of the document's dominant line start - but not left of the paragraph
	 *  above the table on the same page. */
	@Test
	public void rightAlignedTableColumnWithInkToItsLeftIsNotLineNumbers() {
		List<Line> lines = new ArrayList<>();
		lines.add(plain(0, "Sum of instalments", 70.8));
		for (int i = 1; i <= 24; i++) {
			lines.add(line(0, Integer.toString(i), i < 10 ? 95.1 : 91.0, 99.1, Double.NaN));
			lines.add(plain(0, "145,54", 142.6));
			lines.add(plain(0, "2017-01-27", 214.9));
		}
		PdfLayout out = layout(lines);
		PdfLayoutExtractor.dropLineNumbers(out);
		assertEquals(73, out.lines.size());
	}

	@Test
	public void numbersThatDoNotIncreaseAreNotLineNumbers() {
		List<Line> lines = new ArrayList<>();
		lines.add(line(0, "12", 40.0, 51.0, Double.NaN));
		lines.add(plain(0, "twelve", 80.0));
		lines.add(line(0, "9", 45.5, 51.0, Double.NaN));
		lines.add(plain(0, "nine", 80.0));
		PdfLayout out = layout(lines);
		PdfLayoutExtractor.dropLineNumbers(out);
		assertEquals(4, out.lines.size());
	}

	@Test
	public void emptyLayout() {
		PdfLayout out = new PdfLayout();
		PdfLayoutExtractor.dropLineNumbers(out);
		assertTrue(out.lines.isEmpty());
	}

	@Test
	public void textEdgeIsTheModeOfLineStartsAfterAnyLeadingNumber() {
		List<Line> lines = new ArrayList<>();
		lines.add(line(0, "1 first", 55.9, 67.1, 85.0));
		lines.add(line(0, "2 second", 55.9, 67.1, 85.02));
		lines.add(plain(0, "unnumbered", 84.98));
		lines.add(plain(0, "indented", 103.0));
		lines.add(line(0, "3", 55.9, 67.1, Double.NaN));
		assertEquals(85.0, PdfLayoutExtractor.textEdge(lines), 0.0);
		assertTrue(Double.isNaN(PdfLayoutExtractor.textEdge(new ArrayList<>())));
	}

	/** U+00A0, U+2007 and U+202F fold to a space; everything else is left alone. */
	@Test
	public void noBreakSpacesFold() {
		assertEquals("1 000 kg", Line.foldSpaces("1\u00a0000\u2007kg"));
		assertEquals("12 h", Line.foldSpaces("12\u202fh"));
		String plain = "nothing to fold";
		assertTrue(plain == Line.foldSpaces(plain));
	}
}
