package org.docx4j.convert.out.fo;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.io.ByteArrayOutputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.docx4j.Docx4J;
import org.docx4j.Docx4jProperties;
import org.docx4j.XmlUtils;
import org.docx4j.convert.out.FOSettings;
import org.docx4j.openpackaging.packages.WordprocessingMLPackage;
import org.docx4j.wml.Document;
import org.junit.Test;

/**
 * The autofit column sizer measures a column's minimum at the line manager's break
 * opportunities, not at white space: a URL is measured to its {@code ?} and its
 * hyphens, where the line manager (and Word) break it, so the column beside it is
 * not starved.  Measured on a corpus template: a 117-character URL measured whole
 * gave its column 286pt where Word gives 216, and the column beside it 44pt where
 * Word gives 77, which broke a word there a letter to a line.
 *
 * @since 17.1.1
 */
public class AutofitBreakOpportunityTest {

	private static final String URL =
			"http://www.example.com/content/getDocument.aspx?key=AB-12-CD-34-EF-56-GH-78-IJ-90-KL-12-MN-34#&doc=1";

	private static String documentXML(String first, String second) {
		return "<w:document xmlns:w=\"http://schemas.openxmlformats.org/wordprocessingml/2006/main\">"
				+ "<w:body>"
				+ "<w:tbl>"
				+ "<w:tblPr><w:tblW w:w=\"0\" w:type=\"auto\"/></w:tblPr>"
				+ "<w:tblGrid><w:gridCol w:w=\"4680\"/><w:gridCol w:w=\"4680\"/></w:tblGrid>"
				+ "<w:tr>" + cell(first) + cell(second) + "</w:tr>"
				+ "</w:tbl>"
				+ "<w:p/>"
				+ "<w:sectPr><w:pgSz w:w=\"12240\" w:h=\"15840\"/>"
				+ "<w:pgMar w:top=\"1440\" w:right=\"1440\" w:bottom=\"1440\" w:left=\"1440\" w:header=\"720\" w:footer=\"720\" w:gutter=\"0\"/>"
				+ "</w:sectPr>"
				+ "</w:body></w:document>";
	}

	private static String cell(String text) {
		return "<w:tc><w:tcPr><w:tcW w:w=\"0\" w:type=\"auto\"/></w:tcPr>"
				+ "<w:p><w:r><w:rPr><w:rFonts w:ascii=\"Times New Roman\" w:hAnsi=\"Times New Roman\"/><w:sz w:val=\"24\"/></w:rPr>"
				+ "<w:t xml:space=\"preserve\">" + text.replace("&", "&amp;") + "</w:t></w:r></w:p></w:tc>";
	}

	private static List<Double> columnWidthsPt(String fo) {
		List<Double> out = new ArrayList<>();
		Matcher m = Pattern.compile("column-width=\"([0-9.]+)pt\"").matcher(fo);
		while (m.find()) out.add(Double.parseDouble(m.group(1)));
		return out;
	}

	private static List<Double> columns(String first, String second, boolean atBreaks) throws Exception {
		Docx4jProperties.setProperty(TableWriter.MINIMUM_AT_BREAK_OPPORTUNITIES, Boolean.valueOf(atBreaks));
		try {
			WordprocessingMLPackage pkg = WordprocessingMLPackage.createPackage();
			pkg.getMainDocumentPart().setJaxbElement((Document) XmlUtils.unmarshalString(documentXML(first, second)));
			FOSettings settings = new FOSettings(pkg);
			settings.setApacheFopMime(FOSettings.INTERNAL_FO_MIME);
			ByteArrayOutputStream baos = new ByteArrayOutputStream();
			Docx4J.toFO(settings, baos, Docx4J.FLAG_NONE);
			List<Double> widths = columnWidthsPt(baos.toString("UTF-8"));
			assertEquals(2, widths.size());
			return widths;
		} finally {
			Docx4jProperties.setProperty(TableWriter.MINIMUM_AT_BREAK_OPPORTUNITIES, Boolean.TRUE);
		}
	}

	/**
	 * Measured whole, the URL's minimum exceeds the 468pt text column on its own, the
	 * table is squeezed and the prose column is left too narrow for its widest word,
	 * "Conclusions" (59pt at 12pt, plus 10.8pt of cell margins).  Measured to its break
	 * opportunities the URL's widest unit is its host and path, which fits, and the
	 * prose column keeps at least its word.
	 */
	@Test
	public void aUrlColumnNoLongerStarvesItsNeighbour() throws Exception {
		String prose = "Conclusions Conclusions Conclusions";
		List<Double> whole = columns(URL, prose, false);
		List<Double> atBreaks = columns(URL, prose, true);
		assertTrue("URL column measured whole: " + whole, whole.get(0) > 400);
		assertTrue("prose column starved: " + whole, whole.get(1) < 65);
		assertTrue("URL column at its break opportunities: " + atBreaks, atBreaks.get(0) < whole.get(0));
		assertTrue("prose column holds its widest word: " + atBreaks, atBreaks.get(1) >= 69);
		assertEquals(468, atBreaks.get(0) + atBreaks.get(1), 0.5);
	}

	/** Plain words have no break opportunity inside them, so nothing changes. */
	@Test
	public void plainWordsAreMeasuredAsBefore() throws Exception {
		String a = "Personal Protective Equipment", b = "Quantity on hand";
		assertEquals(columns(a, b, false), columns(a, b, true));
	}

	/** A hyphenated compound may break after each hyphen, so it does not hold a column
	 *  to its whole width; a run of ideographs may break between any two. */
	@Test
	public void hyphensAndIdeographsAreBreakOpportunities() throws Exception {
		String prose = "Conclusions Conclusions Conclusions";
		String compound = "state-of-the-art-nineteenth-century-machine-readable-cross-platform-hyphenated-compound";
		List<Double> whole = columns(compound, prose, false);
		List<Double> atBreaks = columns(compound, prose, true);
		assertTrue(atBreaks.get(0) < whole.get(0));
		assertTrue(atBreaks.get(1) > whole.get(1));
		String ideographs = "日本語の文章日本語の文章日本語の文章日本語の文章日本語の文章日本語の文章日本語の文章日本語の文章日本語の文章日本語の文章";
		whole = columns(ideographs, prose, false);
		atBreaks = columns(ideographs, prose, true);
		assertTrue(atBreaks.get(0) < whole.get(0));
	}
}
