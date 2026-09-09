package org.docx4j.convert.out.fo;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
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
import org.junit.After;
import org.junit.Test;

/**
 * A table row whose first paragraph carries {@code w:keepNext} is written with
 * {@code keep-with-next="always"} on the {@code fo:table-row} itself, which is the keep
 * FOP propagates out of the table (a keep on the cells' blocks is dropped at the last
 * row).  See {@link TableWriter#applyTableRowCustomAttributes} and word-layout-rules.md
 * §3.
 *
 * @since 17.1.1
 */
public class TableRowKeepWithNextTest {

	private static final String W = "xmlns:w=\"http://schemas.openxmlformats.org/wordprocessingml/2006/main\"";

	@After
	public void restore() {
		Docx4jProperties.setProperty(TableWriter.ROW_KEEP_WITH_NEXT, "true");
	}

	private static String doc(String body) {
		return "<w:document " + W + "><w:body>" + body
				+ "<w:p><w:r><w:t>after the table</w:t></w:r></w:p>"
				+ "<w:sectPr><w:pgSz w:w=\"12240\" w:h=\"15840\"/>"
				+ "<w:pgMar w:top=\"1440\" w:right=\"1440\" w:bottom=\"1440\" w:left=\"1440\" w:header=\"720\" w:footer=\"720\" w:gutter=\"0\"/>"
				+ "</w:sectPr></w:body></w:document>";
	}

	private static String p(String text, boolean keepNext) {
		return "<w:p>" + (keepNext ? "<w:pPr><w:keepNext/></w:pPr>" : "")
				+ (text == null ? "" : "<w:r><w:t>" + text + "</w:t></w:r>") + "</w:p>";
	}

	private static String tc(String... paragraphs) {
		StringBuilder sb = new StringBuilder("<w:tc><w:tcPr><w:tcW w:w=\"4500\" w:type=\"dxa\"/></w:tcPr>");
		for (String each : paragraphs) sb.append(each);
		return sb.append("</w:tc>").toString();
	}

	private static String tr(String trPr, String... cells) {
		StringBuilder sb = new StringBuilder("<w:tr>");
		if (trPr != null) sb.append("<w:trPr>").append(trPr).append("</w:trPr>");
		for (String each : cells) sb.append(each);
		return sb.append("</w:tr>").toString();
	}

	private static String tbl(String... rows) {
		StringBuilder sb = new StringBuilder("<w:tbl><w:tblPr><w:tblW w:w=\"0\" w:type=\"auto\"/></w:tblPr>"
				+ "<w:tblGrid><w:gridCol w:w=\"4500\"/><w:gridCol w:w=\"4500\"/></w:tblGrid>");
		for (String each : rows) sb.append(each);
		return sb.append("</w:tbl>").toString();
	}

	/** Two cells, every paragraph keeping with next. */
	private static String keptRow(String label) {
		return tr(null, tc(p(label + " one", true)), tc(p(label + " two", true)));
	}

	private static String convert(String body) throws Exception {
		WordprocessingMLPackage pkg = WordprocessingMLPackage.createPackage();
		pkg.getMainDocumentPart().setJaxbElement((Document) XmlUtils.unmarshalString(doc(body)));
		FOSettings settings = new FOSettings(pkg);
		settings.setApacheFopMime(FOSettings.INTERNAL_FO_MIME);
		ByteArrayOutputStream baos = new ByteArrayOutputStream();
		Docx4J.toFO(settings, baos, Docx4J.FLAG_NONE);
		return baos.toString("UTF-8");
	}

	/** Each fo:table-row start tag, in document order. */
	private static List<String> rows(String fo) {
		List<String> out = new ArrayList<>();
		Matcher m = Pattern.compile("<(?:fo:)?table-row[^>]*>").matcher(fo);
		while (m.find()) out.add(m.group());
		return out;
	}

	private static boolean kept(String rowTag) {
		return rowTag.contains("keep-with-next=\"always\"");
	}

	@Test
	public void everyRowWhoseParagraphsAllKeepIsKept() throws Exception {
		List<String> rows = rows(convert(tbl(keptRow("r1"), keptRow("r2"), keptRow("r3"))));
		assertEquals(3, rows.size());
		for (String row : rows) assertTrue(row, kept(row));
	}

	@Test
	public void aRowWithoutKeepNextIsNot() throws Exception {
		List<String> rows = rows(convert(tbl(keptRow("r1"),
				tr(null, tc(p("r2 one", false)), tc(p("r2 two", false))))));
		assertEquals(2, rows.size());
		assertTrue(kept(rows.get(0)));
		assertFalse(rows.get(1), kept(rows.get(1)));
	}

	/** Word consults the first paragraph of the row's first cell and nothing else
	 *  (the table-row-keepnext probe: R2 kept, R3 and R4 not). */
	@Test
	public void theFirstParagraphOfTheFirstCellDecides() throws Exception {
		List<String> rows = rows(convert(tbl(
				tr(null, tc(p("first cell keeps", true)), tc(p("second does not", false))),
				tr(null, tc(p("first does not", false)), tc(p("second keeps", true))),
				tr(null, tc(p("first para no", false), p("second para yes", true)), tc(p("keeps", true))),
				tr(null, tc(p("first keeps", true), p("second does not", false)), tc(p("does not", false))))));
		assertEquals(4, rows.size());
		assertTrue(rows.get(0), kept(rows.get(0)));
		assertFalse(rows.get(1), kept(rows.get(1)));
		assertFalse(rows.get(2), kept(rows.get(2)));
		assertTrue(rows.get(3), kept(rows.get(3)));
	}

	/** An empty paragraph with w:keepNext is a paragraph like any other. */
	@Test
	public void anEmptyKeepingParagraphCounts() throws Exception {
		List<String> rows = rows(convert(tbl(tr(null, tc(p(null, true)), tc(p("text", true))))));
		assertEquals(1, rows.size());
		assertTrue(kept(rows.get(0)));
	}

	/** A table nested in a one-row table: the outer cell's first block is the nested
	 *  table, which keeps by its own first row, so the outer row keeps. */
	@Test
	public void aNestedTableKeepsThroughItsCell() throws Exception {
		String nested = tbl(keptRow("n1"), keptRow("n2"));
		String outer = "<w:tbl><w:tblPr><w:tblW w:w=\"0\" w:type=\"auto\"/></w:tblPr>"
				+ "<w:tblGrid><w:gridCol w:w=\"9000\"/></w:tblGrid>"
				+ "<w:tr><w:tc><w:tcPr><w:tcW w:w=\"9000\" w:type=\"dxa\"/></w:tcPr>" + nested + p(null, true) + "</w:tc></w:tr>"
				+ "</w:tbl>";
		List<String> rows = rows(convert(outer));
		assertEquals(3, rows.size());
		assertTrue("outer row: " + rows.get(0), kept(rows.get(0)));
		assertTrue(kept(rows.get(1)));
		assertTrue(kept(rows.get(2)));

		// the nested table's FIRST row does not keep: neither does the outer row
		String nestedLoose = tbl(tr(null, tc(p("n1 one", false)), tc(p("n1 two", false))), keptRow("n2"));
		String outerLoose = outer.replace(nested, nestedLoose);
		rows = rows(convert(outerLoose));
		assertEquals(3, rows.size());
		assertFalse("outer row: " + rows.get(0), kept(rows.get(0)));
	}

	/** A repeated header row keeps with the row after it by construction. */
	@Test
	public void aHeaderRowIsNotMarked() throws Exception {
		String fo = convert(tbl(tr("<w:tblHeader/>", tc(p("h one", true)), tc(p("h two", true))), keptRow("r2"), keptRow("r3")));
		assertTrue(fo.contains("<fo:table-header"));
		List<String> rows = rows(fo);
		assertEquals(3, rows.size());
		assertFalse(rows.get(0), kept(rows.get(0)));
		assertTrue(kept(rows.get(1)));
		assertTrue(kept(rows.get(2)));
	}

	@Test
	public void thePropertyTurnsItOff() throws Exception {
		Docx4jProperties.setProperty(TableWriter.ROW_KEEP_WITH_NEXT, "false");
		List<String> rows = rows(convert(tbl(keptRow("r1"), keptRow("r2"))));
		assertEquals(2, rows.size());
		for (String row : rows) assertFalse(row, kept(row));
	}
}
