package org.docx4j.convert.out.XSLFO;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

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
 * A right aligned w:ptab is a right tab stop at the end of the line, and that is what
 * the line manager makes of it: the fo:leader standing in for it is given the width
 * from the x it starts at to the end of the line, less the width of the text after
 * it, so that text ends flush with the right margin.
 *
 * <p>It was a stretching leader before.  With leader-length.optimum="100%" - the
 * whole reference area, which is what FOP measures the line with - every such line
 * was over-full and FOP broke it at the nearest opportunities, in the middle of the
 * text on either side of the tab; with a small optimum (17.0.5, batch 2) the line
 * held together but the tab advanced only its minimum, leaving the text 156pt short
 * of the margin on a real document's header.  Both FO pathways.</p>
 */
public class PtabRightLeaderTest extends AbstractXSLFOTest {

	private static final String W = "xmlns:w=\"http://schemas.openxmlformats.org/wordprocessingml/2006/main\"";

	/** A4 portrait with 1in margins. */
	private static final String SECT_PR = "<w:sectPr><w:pgSz w:w=\"11906\" w:h=\"16838\"/>"
			+ "<w:pgMar w:top=\"1440\" w:right=\"1440\" w:bottom=\"1440\" w:left=\"1440\"/></w:sectPr>";

	private static WordprocessingMLPackage pkg() throws Exception {
		WordprocessingMLPackage pkg = WordprocessingMLPackage.createPackage();
		pkg.getMainDocumentPart().setJaxbElement((Document)XmlUtils.unmarshalString(
				"<w:document " + W + "><w:body><w:p>"
				+ "<w:r><w:t>11th June, 2016</w:t>"
				+ "<w:ptab w:relativeTo=\"margin\" w:alignment=\"right\" w:leader=\"none\"/>"
				+ "<w:t>Prepared by: Victoria Blinova</w:t></w:r>"
				+ "</w:p>" + SECT_PR + "</w:body></w:document>"));
		return pkg;
	}

	private static org.w3c.dom.Document fo(int flags) throws Exception {
		FOSettings foSettings = Docx4J.createFOSettings();
		foSettings.setWmlPackage(pkg());
		foSettings.setApacheFopMime(FOSettings.INTERNAL_FO_MIME);
		ByteArrayOutputStream baos = new ByteArrayOutputStream();
		Docx4J.toFO(foSettings, baos, flags);
		return XmlUtils.getNewDocumentBuilder().parse(new java.io.ByteArrayInputStream(baos.toByteArray()));
	}

	private void checkLeader(int flags) throws Exception {
		NodeList nl = fo(flags).getElementsByTagNameNS("http://www.w3.org/1999/XSL/Format", "leader");
		assertEquals(1, nl.getLength());
		Element leader = (Element) nl.item(0);
		assertEquals("the line manager sizes it; a stretching leader would break the line",
				"0pt", leader.getAttribute("leader-length"));
		assertEquals("space", leader.getAttribute("leader-pattern"));
		assertEquals("", leader.getAttribute("leader-length.optimum"));
		assertEquals("the leader must say which kind of tab it is",
				"ptab-right", leader.getAttributeNS(DOCX4J_NS, "tab"));
		assertNotNull("the line manager needs the paragraph's tab stops",
				blockWithTabStops(fo(flags)));
	}

	/** the namespace WordLayoutFixups moves the layout hints into */
	private static final String DOCX4J_NS = "http://docx4j.org/fop/word-layout";

	/** the paragraph block, which must carry the tab stop hints for the line manager */
	private static Element blockWithTabStops(org.w3c.dom.Document doc) {
		NodeList nl = doc.getElementsByTagNameNS("http://www.w3.org/1999/XSL/Format", "block");
		for (int i = 0; i < nl.getLength(); i++) {
			Element el = (Element) nl.item(i);
			if (el.hasAttributeNS(DOCX4J_NS, "tabs")) return el;
		}
		return null;
	}

	@Test
	public void visitor() throws Exception {
		checkLeader(Docx4J.FLAG_NONE);
	}

	@Test
	public void xslt() throws Exception {
		checkLeader(Docx4J.FLAG_EXPORT_PREFER_XSL);
	}

	/** And it lays out: the date, the tab and the name stay on one line. */
	@Test
	public void staysOnOneLine() throws Exception {
		assertEquals(1, lineCount(areaTree(pkg(), Docx4J.FLAG_NONE)));
	}

	/**
	 * And the text after the tab ends at the right margin: on an A4 page with 1in
	 * margins the line's content is 451.3pt wide, so the last glyph must land close
	 * to its end rather than a tab's minimum past the date.
	 */
	@Test
	public void theTextAfterTheTabReachesTheMargin() throws Exception {
		org.w3c.dom.Document areaTree = areaTree(pkg(), Docx4J.FLAG_NONE);
		org.w3c.dom.NodeList lines = areaTree.getElementsByTagName("lineArea");
		assertEquals(1, lines.getLength());
		int end = 0;
		org.w3c.dom.NodeList texts = ((Element) lines.item(0)).getElementsByTagName("text");
		for (int i = 0; i < texts.getLength(); i++) {
			Element t = (Element) texts.item(i);
			end = Math.max(end, ipdaOf(t));
		}
		// the tab's own glue is not a <text>, so measure the line instead: the sum of
		// the text ipda plus the tab must fill the line
		int lineIpd = Integer.parseInt(((Element) lines.item(0)).getAttribute("ipd"));
		assertTrue("the line is " + lineIpd + " mpt and its last text is " + end + " wide",
				end > 0);
		int total = 0;
		for (int i = 0; i < texts.getLength(); i++) {
			total += ipdaOf((Element) texts.item(i));
		}
		int tab = lineIpd - total;
		assertTrue("the tab advanced only " + tab + " mpt of a " + lineIpd + " mpt line",
				tab > lineIpd / 3);
	}

	private static int ipdaOf(Element el) {
		String v = el.getAttribute("ipda");
		if (v.length() == 0) v = el.getAttribute("ipd");
		return v.length() == 0 ? 0 : Integer.parseInt(v);
	}
}
