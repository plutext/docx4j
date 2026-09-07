package org.docx4j.convert.out.XSLFO;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;

import org.docx4j.Docx4J;
import org.docx4j.XmlUtils;
import org.docx4j.convert.out.FOSettings;
import org.docx4j.jaxb.Context;
import org.docx4j.openpackaging.packages.WordprocessingMLPackage;
import org.docx4j.openpackaging.parts.relationships.Namespaces;
import org.docx4j.wml.CTSettings;
import org.docx4j.wml.Document;
import org.junit.Test;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

/**
 * A cell's last paragraph keeps its space-after at the cell bottom (&#xa7;3.5) - and Word
 * keeps it <b>below compatibility mode 15 too</b>, where docx4j pinned it only from mode
 * 15.  XSL-FO drops space at the end of a reference area, so without the pin the space
 * vanishes.
 *
 * <p>Measured on a mode-14 document whose cell paragraphs are styled with
 * {@code <w:spacing w:before="60" w:after="60"/>} (3pt each): Word's row pitch is
 * 119.6 -&gt; 137.6 -&gt; 155.6 = 18.0pt = 3 + 11.5 + 3, where ours was
 * 110.7 -&gt; 125.7 -&gt; 139.2 = 15.0 / 13.5, the space-before only, and the deficit grew
 * to -25.4pt by y=360 on page 1.  x matched to 0.3pt throughout.</p>
 *
 * @since 17.1.0
 */
public class CellBottomSpacingCompat14Test extends AbstractXSLFOTest {

	private static final String W = "xmlns:w=\"" + Namespaces.NS_WORD12 + "\"";
	private static final String FO = "http://www.w3.org/1999/XSL/Format";

	private static String row(String text, int afterTwips) {
		return "<w:tr><w:tc><w:tcPr><w:tcW w:w=\"9000\" w:type=\"dxa\"/></w:tcPr>"
				+ "<w:p><w:pPr><w:spacing w:before=\"60\" w:after=\"" + afterTwips + "\"/></w:pPr>"
				+ "<w:r><w:t>" + text + "</w:t></w:r></w:p></w:tc></w:tr>";
	}

	private static WordprocessingMLPackage pkg(int compatibilityMode) throws Exception {
		return pkg(compatibilityMode, 60);
	}

	private static WordprocessingMLPackage pkg(int compatibilityMode, int afterTwips) throws Exception {

		WordprocessingMLPackage pkg = WordprocessingMLPackage.createPackage();
		pkg.getMainDocumentPart().setJaxbElement((Document) XmlUtils.unmarshalString(
				"<w:document " + W + "><w:body>"
				+ "<w:tbl><w:tblPr><w:tblW w:w=\"9000\" w:type=\"dxa\"/></w:tblPr>"
				+ "<w:tblGrid><w:gridCol w:w=\"9000\"/></w:tblGrid>"
				+ row("row one", afterTwips) + row("row two", afterTwips) + row("row three", afterTwips)
				+ "</w:tbl>"
				+ "<w:p/>"
				+ "<w:sectPr><w:pgSz w:w=\"11906\" w:h=\"16838\"/>"
				+ "<w:pgMar w:top=\"1440\" w:right=\"1440\" w:bottom=\"1440\" w:left=\"1440\"/>"
				+ "</w:sectPr></w:body></w:document>"));

		pkg.getMainDocumentPart().getDocumentSettingsPart(true).setJaxbElement(
				(CTSettings) XmlUtils.unmarshalString(
					"<w:settings " + W + "><w:compat>"
					+ "<w:compatSetting w:name=\"compatibilityMode\""
					+ " w:uri=\"http://schemas.microsoft.com/office/word\""
					+ " w:val=\"" + compatibilityMode + "\"/>"
					+ "</w:compat></w:settings>", Context.jc, CTSettings.class));
		return pkg;
	}

	private static org.w3c.dom.Document fo(int compatibilityMode, int flags) throws Exception {
		FOSettings foSettings = Docx4J.createFOSettings();
		foSettings.setWmlPackage(pkg(compatibilityMode));
		foSettings.setApacheFopMime(FOSettings.INTERNAL_FO_MIME);
		ByteArrayOutputStream baos = new ByteArrayOutputStream();
		Docx4J.toFO(foSettings, baos, flags);
		return XmlUtils.getNewDocumentBuilder().parse(new ByteArrayInputStream(baos.toByteArray()));
	}

	private void theSpaceIsPinned(int compatibilityMode, int flags) throws Exception {

		NodeList cells = fo(compatibilityMode, flags).getElementsByTagNameNS(FO, "table-cell");
		assertEquals("three rows, one cell each", 3, cells.getLength());
		for (int i = 0; i < cells.getLength(); i++) {
			NodeList blocks = ((Element) cells.item(i)).getElementsByTagNameNS(FO, "block");
			assertTrue("no block in the cell", blocks.getLength() > 0);
			Element last = (Element) blocks.item(blocks.getLength() - 1);
			assertEquals("mode " + compatibilityMode + ": the cell's last paragraph keeps its"
					+ " space-after", "retain", last.getAttribute("space-after.conditionality"));
		}
	}

	@Test
	public void mode14Visitor() throws Exception {
		theSpaceIsPinned(14, Docx4J.FLAG_NONE);
	}

	@Test
	public void mode14Xslt() throws Exception {
		theSpaceIsPinned(14, Docx4J.FLAG_EXPORT_PREFER_XSL);
	}

	@Test
	public void mode15Visitor() throws Exception {
		theSpaceIsPinned(15, Docx4J.FLAG_NONE);
	}

	/** And it shows on the page: the three rows are 3 + line + 3 apart, not 3 + line. */
	/**
	 * And it shows on the page: measured in FOP's area tree, each cell paragraph is
	 * allocated its 3pt space-before <i>and</i> its 3pt space-after, so the row pitch is
	 * 21.442pt where the space-before alone gives 18.442 - the 3pt per row Word has.
	 */
	@Test
	public void theRowPitchIncludesIt() throws Exception {

		assertEquals("space-before only", 3000, spaceAllocatedPerCellParagraph(0));
		assertEquals("space-before and space-after", 6000, spaceAllocatedPerCellParagraph(60));
	}

	/** bpda - bpd of each cell paragraph's own block (the one carrying the spacing),
	 *  in millipoints; the three rows must agree. */
	private int spaceAllocatedPerCellParagraph(int afterTwips) throws Exception {

		org.w3c.dom.Document areaTree = areaTree(pkg(14, afterTwips), Docx4J.FLAG_NONE);
		NodeList blocks = areaTree.getElementsByTagName("block");
		int space = -1, found = 0;
		for (int i = 0; i < blocks.getLength(); i++) {
			Element b = (Element) blocks.item(i);
			if (!b.hasAttribute("space-before")) continue; // the paragraph's own block
			int extra = Integer.parseInt(b.getAttribute("bpda")) - Integer.parseInt(b.getAttribute("bpd"));
            if (space < 0) space = extra;
			assertEquals("the three rows should agree", space, extra);
			found++;
		}
		assertEquals("three cell paragraphs", 3, found);
		return space;
	}
}
