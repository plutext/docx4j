package org.docx4j.convert.out.common.writer;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.io.ByteArrayOutputStream;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.docx4j.Docx4J;
import org.docx4j.XmlUtils;
import org.docx4j.convert.out.HTMLSettings;
import org.docx4j.openpackaging.packages.WordprocessingMLPackage;
import org.docx4j.wml.Document;
import org.docx4j.wml.Styles;
import org.junit.Test;

/**
 * The table writers' side of a table style's conditional formatting (issue #546): a
 * {@code w:tblHeader} under {@code firstRow} makes the row a header row (so it repeats
 * across pages, and is a {@code thead} here), band shading is painted, and the borders of a
 * condition are those of its region.  Checked on the HTML output, which both HTML exporters
 * and the FO exporter build through the same {@link AbstractTableWriter}; both HTML
 * pathways are run.
 *
 * @since 17.1.1
 */
public class TableStyleConditionalWriterTest {

	private static final String W = "xmlns:w=\"http://schemas.openxmlformats.org/wordprocessingml/2006/main\"";

	private static final String STYLES = "<w:styles " + W + ">"
			+ "<w:docDefaults><w:rPrDefault><w:rPr><w:sz w:val=\"20\"/></w:rPr></w:rPrDefault></w:docDefaults>"
			+ "<w:style w:type=\"paragraph\" w:default=\"1\" w:styleId=\"Normal\"><w:name w:val=\"Normal\"/></w:style>"
			+ "<w:style w:type=\"table\" w:default=\"1\" w:styleId=\"TableNormal\"><w:name w:val=\"Normal Table\"/></w:style>"
			// a Light List: the header a box with no rules between its cells, repeating;
			// odd bands shaded; the first column ruled on its right
			+ "<w:style w:type=\"table\" w:styleId=\"Cond\"><w:name w:val=\"Cond\"/><w:basedOn w:val=\"TableNormal\"/>"
			+ "<w:tblStylePr w:type=\"firstRow\"><w:rPr><w:b/></w:rPr><w:trPr><w:tblHeader/></w:trPr>"
			+ "<w:tcPr><w:tcBorders>"
			+ "<w:top w:val=\"single\" w:sz=\"8\" w:space=\"0\" w:color=\"4F81BD\"/>"
			+ "<w:left w:val=\"single\" w:sz=\"8\" w:space=\"0\" w:color=\"4F81BD\"/>"
			+ "<w:bottom w:val=\"single\" w:sz=\"8\" w:space=\"0\" w:color=\"4F81BD\"/>"
			+ "<w:right w:val=\"single\" w:sz=\"8\" w:space=\"0\" w:color=\"4F81BD\"/>"
			+ "<w:insideH w:val=\"nil\"/><w:insideV w:val=\"nil\"/>"
			+ "</w:tcBorders><w:shd w:val=\"clear\" w:color=\"auto\" w:fill=\"4F81BD\"/></w:tcPr></w:tblStylePr>"
			+ "<w:tblStylePr w:type=\"band1Horz\"><w:tcPr><w:shd w:val=\"clear\" w:color=\"auto\" w:fill=\"F2F2F2\"/></w:tcPr></w:tblStylePr>"
			+ "<w:tblStylePr w:type=\"firstCol\"><w:tcPr><w:tcBorders>"
			+ "<w:right w:val=\"double\" w:sz=\"6\" w:space=\"0\" w:color=\"008000\"/></w:tcBorders></w:tcPr></w:tblStylePr>"
			+ "</w:style>"
			+ "</w:styles>";

	private static String cell(String text) {
		return "<w:tc><w:tcPr><w:tcW w:w=\"1500\" w:type=\"dxa\"/></w:tcPr><w:p><w:r><w:t>" + text + "</w:t></w:r></w:p></w:tc>";
	}

	private static String cell(String tcPrExtra, String text) {
		return "<w:tc><w:tcPr><w:tcW w:w=\"1500\" w:type=\"dxa\"/>" + tcPrExtra + "</w:tcPr><w:p><w:r><w:t>" + text + "</w:t></w:r></w:p></w:tc>";
	}

	private static String table(String id, String tblLook, String rows) {
		return "<w:tbl><w:tblPr><w:tblStyle w:val=\"Cond\"/>" + tblLook + "</w:tblPr>"
				+ "<w:tblGrid><w:gridCol w:w=\"1500\"/><w:gridCol w:w=\"1500\"/><w:gridCol w:w=\"1500\"/></w:tblGrid>"
				+ rows + "</w:tbl><w:p><w:r><w:t>after " + id + "</w:t></w:r></w:p>";
	}

	private static String rows(String id, int n) {
		StringBuilder sb = new StringBuilder();
		for (int r = 0; r < n; r++) {
			sb.append("<w:tr>");
			for (int c = 0; c < 3; c++) sb.append(cell(id + " r" + r + "c" + c));
			sb.append("</w:tr>");
		}
		return sb.toString();
	}

	private static final String DOC = "<w:document " + W + "><w:body>"
			// T1: Word's default look, three rows
			+ table("T1", "<w:tblLook w:val=\"04A0\"/>", rows("T1", 3))
			// T2: the same table with every conditional format off
			+ table("T2", "<w:tblLook w:val=\"0600\" w:firstRow=\"0\" w:lastRow=\"0\" w:firstColumn=\"0\" w:lastColumn=\"0\" w:noHBand=\"1\" w:noVBand=\"1\"/>", rows("T2", 3))
			// T3: a header cell merged down into the body: the header cannot be repeated
			+ table("T3", "<w:tblLook w:val=\"04A0\"/>",
					"<w:tr>" + cell("<w:vMerge w:val=\"restart\"/>", "T3 r0c0") + cell("T3 r0c1") + cell("T3 r0c2") + "</w:tr>"
					+ "<w:tr>" + cell("<w:vMerge/>", "") + cell("T3 r1c1") + cell("T3 r1c2") + "</w:tr>"
					+ "<w:tr>" + cell("T3 r2c0") + cell("T3 r2c1") + cell("T3 r2c2") + "</w:tr>")
			+ "<w:sectPr><w:pgSz w:w=\"11906\" w:h=\"16838\"/><w:pgMar w:top=\"1440\" w:right=\"1440\" w:bottom=\"1440\" w:left=\"1440\" w:header=\"708\" w:footer=\"708\" w:gutter=\"0\"/></w:sectPr>"
			+ "</w:body></w:document>";

	private static String html(int flag) throws Exception {
		WordprocessingMLPackage pkg = WordprocessingMLPackage.createPackage();
		pkg.getMainDocumentPart().setContents((Document) XmlUtils.unmarshalString(DOC));
		pkg.getMainDocumentPart().getStyleDefinitionsPart()
				.setContents((Styles) XmlUtils.unmarshalString(STYLES));
		HTMLSettings settings = Docx4J.createHTMLSettings();
		settings.setOpcPackage(pkg);
		ByteArrayOutputStream baos = new ByteArrayOutputStream();
		Docx4J.toHTML(settings, baos, flag);
		return baos.toString("UTF-8");
	}

	/** The start tag of the td or th holding the text, or null. */
	private static String cellTag(String html, String text) {
		int at = html.indexOf(">" + text + "<");
		if (at < 0) at = html.indexOf(text);
		if (at < 0) return null;
		int td = html.lastIndexOf("<td", at);
		int th = html.lastIndexOf("<th", at);
		int start = Math.max(td, th);
		if (start < 0) return null;
		return html.substring(start, html.indexOf('>', start) + 1);
	}

	/** The section of the html holding the table which precedes "after ID". */
	private static String tableOf(String html, String id) {
		int end = html.indexOf("after " + id);
		assertTrue(id, end > 0);
		int start = html.lastIndexOf("<table", end);
		return html.substring(start, end);
	}

	private static boolean has(String tag, String cssRegex) {
		return Pattern.compile(cssRegex, Pattern.CASE_INSENSITIVE).matcher(tag).find();
	}

	private static final int[] FLAGS = { Docx4J.FLAG_EXPORT_PREFER_XSL, Docx4J.FLAG_EXPORT_PREFER_NONXSL };

	@Test
	public void firstRowWithTblHeaderIsAHeaderRow() throws Exception {
		for (int flag : FLAGS) {
			String html = html(flag);
			String t1 = tableOf(html, "T1");
			assertTrue("thead " + flag, t1.contains("<thead"));
			assertTrue("th " + flag, cellTag(t1, "T1 r0c1").startsWith("<th"));
			assertTrue("td " + flag, cellTag(t1, "T1 r1c1").startsWith("<td"));
			// the header paragraphs are bold, through the synthetic style's class
			Matcher m = Pattern.compile("Normal-Cond-firstRow-BR\\s*\\{[^}]*font-weight:\\s*bold").matcher(html);
			assertTrue("bold header class " + flag, m.find());
			// with the look off, nothing: no header, no bold
			String t2 = tableOf(html, "T2");
			assertFalse("no thead " + flag, t2.contains("<thead"));
			assertTrue(cellTag(t2, "T2 r0c1").startsWith("<td"));
			assertFalse(html.contains("Normal-Cond-firstRow-BR\" >T2"));
		}
	}

	@Test
	public void bandShadingIsPainted() throws Exception {
		for (int flag : FLAGS) {
			String html = html(flag);
			String t1 = tableOf(html, "T1");
			// row 1 is the first band: shaded; row 2 the second, which the style leaves alone
			assertTrue("band1 " + flag, has(cellTag(t1, "T1 r1c1"), "background-color:\\s*#?f2f2f2"));
			assertFalse("band2 " + flag, has(cellTag(t1, "T1 r2c1"), "f2f2f2"));
			// the header is filled too
			assertTrue("header fill " + flag, has(cellTag(t1, "T1 r0c1"), "background-color:\\s*#?4f81bd"));
			// and nothing under a look with banding off
			String t2 = tableOf(html, "T2");
			assertFalse(has(cellTag(t2, "T2 r1c1"), "f2f2f2"));
		}
	}

	@Test
	public void bordersAreTheRegionsNotTheCells() throws Exception {
		for (int flag : FLAGS) {
			String html = html(flag);
			String t1 = tableOf(html, "T1");
			// firstRow: left on the first header cell, right on the last, nil between
			String h0 = cellTag(t1, "T1 r0c0"), h1 = cellTag(t1, "T1 r0c1"), h2 = cellTag(t1, "T1 r0c2");
			assertTrue("h0 left " + flag, has(h0, "border-left-style:\\s*solid"));
			assertTrue("h0 top " + flag, has(h0, "border-top-style:\\s*solid"));
			assertTrue("h1 left nil " + flag, has(h1, "border-left-style:\\s*none"));
			assertTrue("h1 right nil " + flag, has(h1, "border-right-style:\\s*none"));
			assertTrue("h1 bottom " + flag, has(h1, "border-bottom-style:\\s*solid"));
			assertTrue("h2 right " + flag, has(h2, "border-right-style:\\s*solid"));
			assertTrue("h2 left nil " + flag, has(h2, "border-left-style:\\s*none"));
			// firstCol: the right rule on every first-column body cell, in the style's colour
			assertTrue("c0 right " + flag, has(cellTag(t1, "T1 r1c0"), "border-right-style:\\s*double"));
			assertTrue("c0 right colour " + flag, has(cellTag(t1, "T1 r2c0"), "border-right-color:\\s*#?008000"));
			// firstCol is applied before firstRow, so the corner takes the header's right (nil)
			assertTrue("corner " + flag, has(h0, "border-right-style:\\s*none"));
			assertFalse(has(cellTag(t1, "T1 r1c1"), "border-right-style:\\s*double"));
		}
	}

	@Test
	public void aHeaderCellSpanningIntoTheBodyGivesUpTheHeader() throws Exception {
		for (int flag : FLAGS) {
			String html = html(flag);
			String t3 = tableOf(html, "T3");
			assertFalse("no thead " + flag, t3.contains("<thead"));
			assertNotNull(cellTag(t3, "T3 r0c1"));
			assertTrue(cellTag(t3, "T3 r0c1").startsWith("<td"));
			// its formatting is still the header's
			assertTrue(has(cellTag(t3, "T3 r0c1"), "background-color:\\s*#?4f81bd"));
		}
	}
}
