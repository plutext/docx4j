package org.docx4j.convert.out.XSLFO;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

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
 * A centred or right-aligned numbered paragraph: Word lays the number out <b>on the line
 * with the text</b>, and centres (or right-aligns) the two together (CR-001 &#xa7;2.8).
 *
 * <p>Measured on a centred TOC entry in a 4920tw cell: Word's "1." at x=129.4 and the
 * entry's text at 147.5 - the level's 18pt hanging indent apart - with the whole first
 * line centred on 198.6, the centre of the cell.  docx4j's fo:list-block pinned the label
 * at the cell's content edge, x=90.0, and centred the text alone: the three entries'
 * labels were 39.4, 26.0 and 77.8pt left of Word's.  A right-aligned Cyrillic heading in a
 * second document had its label 152.1pt left of Word's.</p>
 *
 * @since 17.1.0
 */
public class InlineNumberLabelTest extends AbstractXSLFOTest {

	private static final String W = "xmlns:w=\"http://schemas.openxmlformats.org/wordprocessingml/2006/main\"";
	private static final String FO = "http://www.w3.org/1999/XSL/Format";

	private static final String SECT_PR = "<w:sectPr><w:pgSz w:w=\"11906\" w:h=\"16838\"/>"
			+ "<w:pgMar w:top=\"1440\" w:right=\"1440\" w:bottom=\"1440\" w:left=\"1440\"/></w:sectPr>";

	/** decimal "%1." at 360/360 twips, so the number position is 0 and the gap 18pt */
	private static final String NUMBERING = "<w:numbering " + W + ">"
			+ "<w:abstractNum w:abstractNumId=\"0\"><w:lvl w:ilvl=\"0\"><w:start w:val=\"1\"/>"
			+ "<w:numFmt w:val=\"decimal\"/><w:lvlText w:val=\"%1.\"/><w:lvlJc w:val=\"left\"/>"
			+ "<w:pPr><w:ind w:left=\"360\" w:hanging=\"360\"/></w:pPr></w:lvl></w:abstractNum>"
			+ "<w:num w:numId=\"1\"><w:abstractNumId w:val=\"0\"/></w:num>"
			+ "</w:numbering>";

	private static String item(String jc) {
		return "<w:p><w:pPr><w:numPr><w:ilvl w:val=\"0\"/><w:numId w:val=\"1\"/></w:numPr>"
				+ (jc.length()==0 ? "" : "<w:jc w:val=\"" + jc + "\"/>")
				+ "</w:pPr><w:r><w:t>GENERAL PROVISIONS</w:t></w:r></w:p>";
	}

	private static org.w3c.dom.Document fo(String body, int flags) throws Exception {
		WordprocessingMLPackage pkg = WordprocessingMLPackage.createPackage();
		org.docx4j.openpackaging.parts.WordprocessingML.NumberingDefinitionsPart ndp
				= new org.docx4j.openpackaging.parts.WordprocessingML.NumberingDefinitionsPart();
		ndp.setJaxbElement((org.docx4j.wml.Numbering)XmlUtils.unmarshalString(NUMBERING));
		pkg.getMainDocumentPart().addTargetPart(ndp);
		pkg.getMainDocumentPart().setJaxbElement((Document)XmlUtils.unmarshalString(
				"<w:document " + W + "><w:body>" + body + SECT_PR + "</w:body></w:document>"));
		FOSettings foSettings = Docx4J.createFOSettings();
		foSettings.setWmlPackage(pkg);
		foSettings.setApacheFopMime(FOSettings.INTERNAL_FO_MIME);
		ByteArrayOutputStream baos = new ByteArrayOutputStream();
		Docx4J.toFO(foSettings, baos, flags);
		return XmlUtils.getNewDocumentBuilder().parse(new ByteArrayInputStream(baos.toByteArray()));
	}

	private static Element only(org.w3c.dom.Document doc, String localName) {
		NodeList nl = doc.getElementsByTagNameNS(FO, localName);
		assertEquals("one fo:" + localName + " expected", 1, nl.getLength());
		return (Element) nl.item(0);
	}

	private void inline(String jc, int flags) throws Exception {

		org.w3c.dom.Document doc = fo(item(jc), flags);

		// no list block: the number is part of the line
		assertEquals(0, doc.getElementsByTagNameNS(FO, "list-block").getLength());

		Element block = only(doc, "block");
		assertEquals(jc.equals("center") ? "center" : "right", block.getAttribute("text-align"));
		// the block begins where the number does - the level's left less its hanging
		// indent - and its first line is not indented again
		assertEquals("0in", block.getAttribute("start-indent"));
		assertEquals("0in", block.getAttribute("text-indent"));

		// label, the w:suff tab, then the paragraph's own content
		Element label = (Element) block.getFirstChild();
		assertEquals("inline", label.getLocalName());
		assertEquals("1.", label.getTextContent());
		Element leader = (Element) label.getNextSibling();
		assertEquals("leader", leader.getLocalName());
		// 18pt from the number's position to the text's, less the label's own width
		double gap = Double.parseDouble(leader.getAttribute("leader-length").replace("pt", ""));
		assertTrue("gap " + gap, gap > 5 && gap < 18);
		assertTrue(block.getTextContent().contains("GENERAL PROVISIONS"));
	}

	@Test
	public void centredVisitor() throws Exception {
		inline("center", Docx4J.FLAG_NONE);
	}

	@Test
	public void centredXslt() throws Exception {
		inline("center", Docx4J.FLAG_EXPORT_PREFER_XSL);
	}

	@Test
	public void rightAligned() throws Exception {
		inline("right", Docx4J.FLAG_NONE);
		inline("right", Docx4J.FLAG_EXPORT_PREFER_XSL);
	}

	/** A left-aligned or justified list keeps the hanging-indent geometry. */
	@Test
	public void leftAndJustifiedKeepTheListBlock() throws Exception {
		for (int flags : new int[] { Docx4J.FLAG_NONE, Docx4J.FLAG_EXPORT_PREFER_XSL }) {
			for (String jc : new String[] { "", "left", "both" }) {
				org.w3c.dom.Document doc = fo(item(jc), flags);
				assertEquals("jc=" + jc, 1, doc.getElementsByTagNameNS(FO, "list-block").getLength());
			}
		}
	}

}
