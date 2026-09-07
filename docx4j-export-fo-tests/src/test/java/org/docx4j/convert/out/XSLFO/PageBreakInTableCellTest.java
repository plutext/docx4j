package org.docx4j.convert.out.XSLFO;

import static org.junit.Assert.assertEquals;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;

import org.docx4j.Docx4J;
import org.docx4j.XmlUtils;
import org.docx4j.convert.out.FOSettings;
import org.docx4j.openpackaging.packages.WordprocessingMLPackage;
import org.docx4j.wml.Document;
import org.junit.Test;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

/**
 * A page break inside a table cell belongs to the table, not to the paragraph
 * (CR-001 &#xa7;3.3).
 *
 * <p>Word paginates on the paragraph property and not on the break run: a
 * {@code w:pageBreakBefore} on the paragraph which opens the table starts the table on
 * a new page, and a {@code w:br w:type="page"} anywhere in a cell is ignored outright.
 * Measured on the {@code page-break-in-cell} probe, which varies the break's position
 * and count one at a time - a single break at the head of the first cell's first
 * paragraph, two of them there, one in a later paragraph of the cell, one in a cell
 * which is not the first, and one in the second row: Word gives none of them a page and
 * its table shares a page with the paragraph introducing it in every case, so Word's
 * document is 7 pages where 17.1.0's was 8.  FOP otherwise breaks the table wherever it
 * finds a break-before in a cell, which put one line on a page of its own.</p>
 *
 * <p>Both FO pathways.</p>
 *
 * @since 17.1.0
 */
public class PageBreakInTableCellTest extends AbstractXSLFOTest {

	private static final String W = "xmlns:w=\"http://schemas.openxmlformats.org/wordprocessingml/2006/main\"";
	private static final String FO_NS = "http://www.w3.org/1999/XSL/Format";

	private static final String SECT_PR = "<w:sectPr><w:pgSz w:w=\"11906\" w:h=\"16838\"/>"
			+ "<w:pgMar w:top=\"1440\" w:right=\"1440\" w:bottom=\"1440\" w:left=\"1440\"/></w:sectPr>";

	private static final int[] FLAGS = { Docx4J.FLAG_NONE, Docx4J.FLAG_EXPORT_PREFER_XSL };

	private static String flagName(int flags) {
		return flags == Docx4J.FLAG_NONE ? "visitor" : "xslt";
	}

	/** A two-row, one-column table whose first cell's first paragraph carries the break. */
	private static WordprocessingMLPackage pkg(String firstParagraph) throws Exception {
		WordprocessingMLPackage pkg = WordprocessingMLPackage.createPackage();
		pkg.getMainDocumentPart().setJaxbElement((Document) XmlUtils.unmarshalString(
				"<w:document " + W + "><w:body>"
				+ "<w:p><w:r><w:t>Introducing the table</w:t></w:r></w:p>"
				+ "<w:tbl><w:tblPr><w:tblW w:w=\"0\" w:type=\"auto\"/></w:tblPr>"
				+ "<w:tblGrid><w:gridCol w:w=\"9026\"/></w:tblGrid>"
				+ "<w:tr><w:tc><w:tcPr><w:tcW w:w=\"0\" w:type=\"auto\"/></w:tcPr>"
				+ firstParagraph + "</w:tc></w:tr>"
				+ "<w:tr><w:tc><w:tcPr><w:tcW w:w=\"0\" w:type=\"auto\"/></w:tcPr>"
				+ "<w:p><w:r><w:t>Row two</w:t></w:r></w:p></w:tc></w:tr>"
				+ "</w:tbl>"
				+ "<w:p/>" + SECT_PR + "</w:body></w:document>"));
		return pkg;
	}

	private org.w3c.dom.Document fo(WordprocessingMLPackage pkg, int flags) throws Exception {
		FOSettings foSettings = Docx4J.createFOSettings();
		foSettings.setOpcPackage(pkg);
		foSettings.setApacheFopMime(FOSettings.INTERNAL_FO_MIME);
		ByteArrayOutputStream baos = new ByteArrayOutputStream();
		Docx4J.toFO(foSettings, baos, flags);
		return XmlUtils.getNewDocumentBuilder().parse(new ByteArrayInputStream(baos.toByteArray()));
	}

	/** How many elements of this name carry break-before="page". */
	private static int breaks(org.w3c.dom.Document doc, String localName) {
		NodeList nl = doc.getElementsByTagNameNS(FO_NS, localName);
		int n = 0;
		for (int i = 0; i < nl.getLength(); i++) {
			if ("page".equals(((Element) nl.item(i)).getAttribute("break-before"))) n++;
		}
		return n;
	}

	@Test
	public void aBreakRunInACellIsIgnored() throws Exception {
		String p = "<w:p><w:r><w:br w:type=\"page\"/><w:t>Row one</w:t></w:r></w:p>";
		for (int flags : FLAGS) {
			org.w3c.dom.Document doc = fo(pkg(p), flags);
			assertEquals(flagName(flags) + ": no block may keep the break",
					0, breaks(doc, "block"));
			assertEquals(flagName(flags) + ": and the table must not take it either",
					0, breaks(doc, "table"));
		}
	}

	@Test
	public void twoBreakRunsInACellAreIgnoredToo() throws Exception {
		String p = "<w:p><w:r><w:br w:type=\"page\"/><w:br w:type=\"page\"/>"
				+ "<w:t>Row one</w:t></w:r></w:p>";
		for (int flags : FLAGS) {
			org.w3c.dom.Document doc = fo(pkg(p), flags);
			assertEquals(flagName(flags) + ": no block may keep the breaks",
					0, breaks(doc, "block"));
			assertEquals(flagName(flags) + ": and the table must not take them",
					0, breaks(doc, "table"));
		}
	}

	@Test
	public void pageBreakBeforeOnTheOpeningParagraphMovesTheTable() throws Exception {
		String p = "<w:p><w:pPr><w:pageBreakBefore/></w:pPr>"
				+ "<w:r><w:t>Row one</w:t></w:r></w:p>";
		for (int flags : FLAGS) {
			org.w3c.dom.Document doc = fo(pkg(p), flags);
			assertEquals(flagName(flags) + ": the break belongs to the table",
					1, breaks(doc, "table"));
			assertEquals(flagName(flags) + ": and not to the cell's paragraph",
					0, breaks(doc, "block"));
		}
	}

	@Test
	public void pageBreakBeforeLowerDownTheTableIsDropped() throws Exception {
		String p = "<w:p><w:r><w:t>Row one</w:t></w:r></w:p>"
				+ "<w:p><w:pPr><w:pageBreakBefore/></w:pPr><w:r><w:t>and more</w:t></w:r></w:p>";
		for (int flags : FLAGS) {
			org.w3c.dom.Document doc = fo(pkg(p), flags);
			assertEquals(flagName(flags) + ": only a break which opens the table counts",
					0, breaks(doc, "table"));
			assertEquals(flagName(flags) + ": and the paragraph does not keep it",
					0, breaks(doc, "block"));
		}
	}
}
