package org.docx4j.convert.out.fo;

import static org.junit.Assert.assertEquals;

import java.io.ByteArrayOutputStream;
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
 * w:hideMark: a cell whose last paragraph paints nothing takes no line for it, as Word
 * sizes the row.  See {@link TableWriter#HIDE_MARK} and word-layout-rules.md §6.
 *
 * @since 17.1.1
 */
public class HideMarkTest {

	@After
	public void restore() {
		Docx4jProperties.setProperty(TableWriter.HIDE_MARK, "true");
	}

	private static String tc(boolean hide, String... paragraphs) {
		StringBuilder sb = new StringBuilder("<w:tc><w:tcPr><w:tcW w:w=\"4000\" w:type=\"dxa\"/>" + (hide ? "<w:hideMark/>" : "") + "</w:tcPr>");
		for (String p : paragraphs) sb.append(p);
		return sb.append("</w:tc>").toString();
	}

	private static final String EMPTY = "<w:p><w:pPr><w:rPr><w:sz w:val=\"24\"/></w:rPr></w:pPr></w:p>";
	private static final String TEXT = "<w:p><w:r><w:t>text</w:t></w:r></w:p>";

	private static String convert(String cell) throws Exception {
		String xml = "<w:document xmlns:w=\"http://schemas.openxmlformats.org/wordprocessingml/2006/main\"><w:body>"
				+ "<w:tbl><w:tblPr><w:tblW w:w=\"0\" w:type=\"auto\"/></w:tblPr><w:tblGrid><w:gridCol w:w=\"4000\"/></w:tblGrid>"
				+ "<w:tr>" + cell + "</w:tr></w:tbl><w:p><w:r><w:t>after</w:t></w:r></w:p>"
				+ "<w:sectPr><w:pgSz w:w=\"12240\" w:h=\"15840\"/>"
				+ "<w:pgMar w:top=\"1440\" w:right=\"1440\" w:bottom=\"1440\" w:left=\"1440\" w:header=\"720\" w:footer=\"720\" w:gutter=\"0\"/>"
				+ "</w:sectPr></w:body></w:document>";
		WordprocessingMLPackage pkg = WordprocessingMLPackage.createPackage();
		pkg.getMainDocumentPart().setJaxbElement((Document) XmlUtils.unmarshalString(xml));
		FOSettings settings = new FOSettings(pkg);
		settings.setApacheFopMime(FOSettings.INTERNAL_FO_MIME);
		ByteArrayOutputStream baos = new ByteArrayOutputStream();
		Docx4J.toFO(settings, baos, Docx4J.FLAG_NONE);
		return baos.toString("UTF-8");
	}

	/** The number of blocks with a line-height inside the first table cell. */
	private static int linedBlocksInCell(String fo) {
		int a = fo.indexOf("<fo:table-cell"); int b = fo.indexOf("</fo:table-cell>", a);
		String cell = fo.substring(a, b);
		int n = 0;
		Matcher m = Pattern.compile("<(?:fo:)?block\\b[^>]*line-height=").matcher(cell);
		while (m.find()) n++;
		return n;
	}

	@Test
	public void anEmptyHideMarkCellTakesNoLine() throws Exception {
		assertEquals(0, linedBlocksInCell(convert(tc(true, EMPTY))));
	}

	@Test
	public void withoutTheFlagTheMarkHasItsLine() throws Exception {
		assertEquals(1, linedBlocksInCell(convert(tc(false, EMPTY))));
	}

	@Test
	public void onlyTheLastParagraphIsTheMark() throws Exception {
		// text then an empty paragraph: the empty one is the mark and goes; the text stays
		assertEquals(1, linedBlocksInCell(convert(tc(true, TEXT, EMPTY))));
		// two empty paragraphs: the first keeps its line
		assertEquals(1, linedBlocksInCell(convert(tc(true, EMPTY, EMPTY))));
		// text last: nothing to hide
		assertEquals(1, linedBlocksInCell(convert(tc(true, TEXT))));
	}

	@Test
	public void thePropertyTurnsItOff() throws Exception {
		Docx4jProperties.setProperty(TableWriter.HIDE_MARK, "false");
		assertEquals(1, linedBlocksInCell(convert(tc(true, EMPTY))));
	}
}
