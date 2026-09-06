package org.docx4j.convert.out.XSLFO;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.io.ByteArrayOutputStream;

import org.docx4j.Docx4J;
import org.docx4j.UnitsOfMeasurement;
import org.docx4j.XmlUtils;
import org.docx4j.convert.out.FOSettings;
import org.docx4j.openpackaging.packages.WordprocessingMLPackage;
import org.docx4j.openpackaging.parts.PartName;
import org.docx4j.openpackaging.parts.WordprocessingML.NumberingDefinitionsPart;
import org.docx4j.openpackaging.parts.relationships.Namespaces;
import org.docx4j.wml.Document;
import org.docx4j.wml.Numbering;
import org.junit.Test;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

/**
 * Word keeps a list label's natural width: where it is wider than the level's hanging
 * indent the text goes to the first tab stop past it, rather than being overprinted.
 *
 * <p>Measured (CR-001 &#xa7;2.8) on a document whose paragraphs are
 * <code>&lt;w:ind w:left="40" w:hanging="6"/&gt;</code> with a
 * <code>&lt;w:tab w:val="left" w:pos="358"/&gt;</code> and a <code>(%1)</code> label: Word
 * draws "(" at x=56.66, "2" at 60.02, ")" at 65.54, a space at 68.90 and the text's "M" at
 * <b>72.98</b> - the 358tw stop - where our 0.3pt label column produced
 * "(M2i)tverpachtet".</p>
 *
 * @since 17.0.6
 */
public class ListLabelWiderThanHangingTest extends AbstractXSLFOTest {

	private static final String W = "xmlns:w=\"" + Namespaces.NS_WORD12 + "\"";

	private static String numbering(int hangingTwips) {
		return "<w:numbering " + W + ">"
				+ "<w:abstractNum w:abstractNumId=\"0\"><w:lvl w:ilvl=\"0\">"
				+ "<w:start w:val=\"1\"/><w:numFmt w:val=\"decimal\"/><w:lvlText w:val=\"(%1)\"/>"
				+ "<w:lvlJc w:val=\"left\"/>"
				+ "<w:pPr><w:ind w:left=\"40\" w:hanging=\"" + hangingTwips + "\"/></w:pPr>"
				+ "</w:lvl></w:abstractNum>"
				+ "<w:num w:numId=\"1\"><w:abstractNumId w:val=\"0\"/></w:num>"
				+ "</w:numbering>";
	}

	private static org.w3c.dom.Document fo(int hangingTwips, int flags) throws Exception {
		WordprocessingMLPackage pkg = WordprocessingMLPackage.createPackage();
		NumberingDefinitionsPart ndp = new NumberingDefinitionsPart(new PartName("/word/numbering.xml"));
		ndp.setJaxbElement((Numbering) XmlUtils.unmarshalString(numbering(hangingTwips)));
		pkg.getMainDocumentPart().addTargetPart(ndp);
		pkg.getMainDocumentPart().setJaxbElement((Document) XmlUtils.unmarshalString(
				"<w:document " + W + "><w:body>"
				+ "<w:p><w:pPr>"
				+ "<w:numPr><w:ilvl w:val=\"0\"/><w:numId w:val=\"1\"/></w:numPr>"
				+ "<w:tabs><w:tab w:val=\"left\" w:pos=\"358\"/></w:tabs>"
				+ "<w:ind w:left=\"40\" w:hanging=\"" + hangingTwips + "\"/>"
				+ "</w:pPr><w:r><w:t>Mitverpachtet</w:t></w:r></w:p>"
				+ "</w:body></w:document>"));
		FOSettings foSettings = Docx4J.createFOSettings();
		foSettings.setWmlPackage(pkg);
		foSettings.setApacheFopMime(FOSettings.INTERNAL_FO_MIME);
		ByteArrayOutputStream baos = new ByteArrayOutputStream();
		Docx4J.toFO(foSettings, baos, flags);
		return XmlUtils.getNewDocumentBuilder().parse(new java.io.ByteArrayInputStream(baos.toByteArray()));
	}

	private static Element listBlock(org.w3c.dom.Document doc) {
		NodeList nl = doc.getElementsByTagNameNS("http://www.w3.org/1999/XSL/Format", "list-block");
		return nl.getLength() == 0 ? null : (Element) nl.item(0);
	}

	private static double pts(String length) {
		assertTrue("expected points, got " + length, length.endsWith("pt"));
		return Double.parseDouble(length.substring(0, length.length() - 2));
	}

	private void check(int flags) throws Exception {

		// a 6tw (0.3pt) hanging indent cannot hold "(1)": the text goes to the 358tw stop,
		// which is 358 - (40 - 6) = 324tw = 16.2pt past the number's position
		Element narrow = listBlock(fo(6, flags));
		assertNotNull("no list-block", narrow);
		assertEquals("the number's position: left - hanging",
				(40 - 6) / 20d, pts(narrow.getAttribute("start-indent")), 0.05);
		assertEquals("the label column reaches the next tab stop",
				324 / 20d, pts(narrow.getAttribute("provisional-distance-between-starts")), 0.05);

		// a hanging indent the label does fit inside is used as it stands
		Element wide = listBlock(fo(720, flags));
		assertNotNull("no list-block", wide);
		assertEquals(720 / 20d, pts(wide.getAttribute("provisional-distance-between-starts")), 0.05);
	}

	@Test
	public void visitorPathway() throws Exception {
		check(Docx4J.FLAG_NONE);
	}

	@Test
	public void xsltPathway() throws Exception {
		check(Docx4J.FLAG_EXPORT_PREFER_XSL);
	}
}
