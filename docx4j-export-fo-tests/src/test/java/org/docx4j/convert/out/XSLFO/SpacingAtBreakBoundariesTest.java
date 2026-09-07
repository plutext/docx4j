package org.docx4j.convert.out.XSLFO;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

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
 * A place XSL-FO drops a space Word keeps (CR-001 &#xa7;3).
 *
 * <ul>
 * <li><b>A hard page break inside a numbered paragraph.</b> The break reaches the block
 * inside the {@code fo:list-item-body}, so FOP laid the list block's space-before down on
 * the page the break leaves.  Measured on a document whose {@code Heading1} carries
 * {@code <w:spacing w:before="360" w:after="240"/>} and whose first run is a page break:
 * Word's heading is at y=85.0 (the 56.7pt top margin, 18pt of space-before, its ascent)
 * where docx4j's block top was the top margin exactly, and every line of that page carried
 * the -18.2.</li>
 * </ul>
 *
 * @since 17.1.0
 */
public class SpacingAtBreakBoundariesTest extends AbstractXSLFOTest {

	private static final String W = "xmlns:w=\"http://schemas.openxmlformats.org/wordprocessingml/2006/main\"";
	private static final String FO = "http://www.w3.org/1999/XSL/Format";

	private static final String NUMBERING = "<w:numbering " + W + ">"
			+ "<w:abstractNum w:abstractNumId=\"0\"><w:lvl w:ilvl=\"0\"><w:start w:val=\"1\"/>"
			+ "<w:numFmt w:val=\"decimal\"/><w:lvlText w:val=\"%1\"/><w:lvlJc w:val=\"left\"/>"
			+ "<w:pPr><w:ind w:left=\"567\" w:hanging=\"567\"/></w:pPr></w:lvl></w:abstractNum>"
			+ "<w:num w:numId=\"1\"><w:abstractNumId w:val=\"0\"/></w:num></w:numbering>";

	private static org.w3c.dom.Document fo(WordprocessingMLPackage pkg, int flags) throws Exception {
		FOSettings foSettings = Docx4J.createFOSettings();
		foSettings.setWmlPackage(pkg);
		foSettings.setApacheFopMime(FOSettings.INTERNAL_FO_MIME);
		ByteArrayOutputStream baos = new ByteArrayOutputStream();
		Docx4J.toFO(foSettings, baos, flags);
		return XmlUtils.getNewDocumentBuilder().parse(new ByteArrayInputStream(baos.toByteArray()));
	}

	/** A numbered heading whose first run is a page break, with 18pt of space-before. */
	private static WordprocessingMLPackage numberedPageBreakPkg() throws Exception {
		WordprocessingMLPackage pkg = WordprocessingMLPackage.createPackage();
		org.docx4j.openpackaging.parts.WordprocessingML.NumberingDefinitionsPart ndp
				= new org.docx4j.openpackaging.parts.WordprocessingML.NumberingDefinitionsPart();
		ndp.setJaxbElement((org.docx4j.wml.Numbering)XmlUtils.unmarshalString(NUMBERING));
		pkg.getMainDocumentPart().addTargetPart(ndp);
		pkg.getMainDocumentPart().setJaxbElement((Document)XmlUtils.unmarshalString(
				"<w:document " + W + "><w:body>"
				+ "<w:p><w:r><w:t>before the break</w:t></w:r></w:p>"
				+ "<w:p><w:pPr><w:numPr><w:ilvl w:val=\"0\"/><w:numId w:val=\"1\"/></w:numPr>"
				+ "<w:spacing w:before=\"360\" w:after=\"240\"/></w:pPr>"
				+ "<w:r><w:br w:type=\"page\"/></w:r><w:r><w:t>Heading on the new page</w:t></w:r></w:p>"
				+ "<w:sectPr><w:pgSz w:w=\"11906\" w:h=\"16838\"/>"
				+ "<w:pgMar w:top=\"1134\" w:right=\"1134\" w:bottom=\"1134\" w:left=\"1134\"/></w:sectPr>"
				+ "</w:body></w:document>"));
		return pkg;
	}

	private void checkNumbered(int flags) throws Exception {
		org.w3c.dom.Document doc = fo(numberedPageBreakPkg(), flags);
		NodeList lists = doc.getElementsByTagNameNS(FO, "list-block");
		assertEquals(1, lists.getLength());
		Element list = (Element) lists.item(0);
		assertEquals("the break belongs to the list block", "page", list.getAttribute("break-before"));
		assertEquals("18pt", list.getAttribute("space-before"));
		assertEquals("retain", list.getAttribute("space-before.conditionality"));
		// and nothing inside the item still carries it
		NodeList blocks = doc.getElementsByTagNameNS(FO, "block");
		for (int i = 0; i < blocks.getLength(); i++) {
			assertEquals("", ((Element) blocks.item(i)).getAttribute("break-before").replace("auto", ""));
		}
	}

	@Test
	public void numberedPageBreakVisitor() throws Exception {
		checkNumbered(Docx4J.FLAG_NONE);
	}

	@Test
	public void numberedPageBreakXslt() throws Exception {
		checkNumbered(Docx4J.FLAG_EXPORT_PREFER_XSL);
	}

}
