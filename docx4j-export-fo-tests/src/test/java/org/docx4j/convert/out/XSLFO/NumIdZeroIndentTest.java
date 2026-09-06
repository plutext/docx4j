package org.docx4j.convert.out.XSLFO;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

import java.io.ByteArrayOutputStream;

import org.docx4j.Docx4J;
import org.docx4j.XmlUtils;
import org.docx4j.convert.out.FOSettings;
import org.docx4j.openpackaging.packages.WordprocessingMLPackage;
import org.docx4j.openpackaging.parts.PartName;
import org.docx4j.openpackaging.parts.WordprocessingML.NumberingDefinitionsPart;
import org.docx4j.openpackaging.parts.relationships.Namespaces;
import org.docx4j.wml.Document;
import org.docx4j.wml.Numbering;
import org.docx4j.wml.Styles;
import org.junit.Test;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

/**
 * <code>w:numId w:val="0"</code> takes the paragraph out of the list (ECMA-376 17.9.18),
 * so neither the level's label nor its <code>w:ind</code> applies - only the paragraph's
 * own.  docx4j dropped the label but kept the level's hanging indent.
 *
 * <p>Measured (CR-001 &#xa7;2.8) on a heading styled with a style whose numbering carries a
 * 57.6pt hanging indent and which is overridden with
 * <code>&lt;w:numPr&gt;&lt;w:ilvl w:val="0"/&gt;&lt;w:numId w:val="0"/&gt;&lt;/w:numPr&gt;</code>
 * and <code>&lt;w:ind w:left="720"/&gt;</code>: Word draws it at x=78.5..541.2 where
 * docx4j drew the same 462pt of text at 20.9..483.1, exactly 57.6pt left, on ten
 * paragraphs.</p>
 *
 * @since 17.0.6
 */
public class NumIdZeroIndentTest extends AbstractXSLFOTest {

	private static final String W = "xmlns:w=\"" + Namespaces.NS_WORD12 + "\"";

	/** numId 1 -> abstract 0, whose only level indents 1440 with a 1152tw (57.6pt) hanging */
	private static final String NUMBERING =
			"<w:numbering " + W + ">"
			+ "<w:abstractNum w:abstractNumId=\"0\"><w:lvl w:ilvl=\"0\">"
			+ "<w:start w:val=\"1\"/><w:numFmt w:val=\"decimal\"/><w:lvlText w:val=\"%1.\"/>"
			+ "<w:lvlJc w:val=\"left\"/>"
			+ "<w:pPr><w:ind w:left=\"1440\" w:hanging=\"1152\"/></w:pPr>"
			+ "</w:lvl></w:abstractNum>"
			+ "<w:num w:numId=\"1\"><w:abstractNumId w:val=\"0\"/></w:num>"
			+ "</w:numbering>";

	private static final String STYLES =
			"<w:styles " + W + ">"
			+ "<w:style w:type=\"paragraph\" w:styleId=\"Numbered\"><w:name w:val=\"Numbered\"/>"
			+ "<w:pPr><w:numPr><w:ilvl w:val=\"0\"/><w:numId w:val=\"1\"/></w:numPr></w:pPr>"
			+ "</w:style></w:styles>";

	private static org.w3c.dom.Document fo(String pPr, int flags) throws Exception {
		WordprocessingMLPackage pkg = WordprocessingMLPackage.createPackage();
		pkg.getMainDocumentPart().getStyleDefinitionsPart().setJaxbElement(
				(Styles) XmlUtils.unmarshalString(STYLES));
		NumberingDefinitionsPart ndp = new NumberingDefinitionsPart(new PartName("/word/numbering.xml"));
		ndp.setJaxbElement((Numbering) XmlUtils.unmarshalString(NUMBERING));
		pkg.getMainDocumentPart().addTargetPart(ndp);
		pkg.getMainDocumentPart().setJaxbElement((Document) XmlUtils.unmarshalString(
				"<w:document " + W + "><w:body>"
				+ "<w:p><w:pPr><w:pStyle w:val=\"Numbered\"/>" + pPr + "</w:pPr>"
				+ "<w:r><w:t>heading</w:t></w:r></w:p>"
				+ "</w:body></w:document>"));
		FOSettings foSettings = Docx4J.createFOSettings();
		foSettings.setWmlPackage(pkg);
		foSettings.setApacheFopMime(FOSettings.INTERNAL_FO_MIME);
		ByteArrayOutputStream baos = new ByteArrayOutputStream();
		Docx4J.toFO(foSettings, baos, flags);
		return XmlUtils.getNewDocumentBuilder().parse(new java.io.ByteArrayInputStream(baos.toByteArray()));
	}

	/** the block (or list-block) holding the heading */
	private static Element blockFor(org.w3c.dom.Document doc, String text) {
		NodeList nl = doc.getElementsByTagNameNS("http://www.w3.org/1999/XSL/Format", "*");
		for (int i = 0; i < nl.getLength(); i++) {
			Element el = (Element) nl.item(i);
			if (!"block".equals(el.getLocalName()) && !"list-block".equals(el.getLocalName())) continue;
			if (el.getAttribute("start-indent").length() > 0 && el.getTextContent().contains(text)) return el;
		}
		return null;
	}

	private void check(int flags) throws Exception {

		Element numbered = blockFor(fo("", flags), "heading");
		assertNotNull("numbered paragraph", numbered);
		assertEquals("the level's number position: 1440 - 1152 twips",
				"14.4pt", numbered.getAttribute("start-indent"));

		Element notNumbered = blockFor(fo(
				"<w:numPr><w:ilvl w:val=\"0\"/><w:numId w:val=\"0\"/></w:numPr>"
				+ "<w:ind w:left=\"720\"/>", flags), "heading");
		assertNotNull("un-numbered paragraph", notNumbered);
		assertEquals("numId 0 leaves only the paragraph's own w:ind left=720",
				"36pt", notNumbered.getAttribute("start-indent"));
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
