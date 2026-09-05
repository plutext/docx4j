package org.docx4j.convert.out.XSLFO;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

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
 * A right aligned w:ptab becomes an fo:leader whose length stretches to the end
 * of the line.  Its <em>optimum</em> length used to be 100% - the whole
 * reference area - which is what FOP measures the line with, so the line was
 * always over-full and FOP broke it at the nearest opportunities, in the middle
 * of the text on either side of the tab (CR-001, a real document's header).  The
 * optimum is now the minimum; the leader still stretches to 100% because the
 * block carries text-align-last="justify".  Both FO pathways.
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
		assertEquals("12pt", leader.getAttribute("leader-length.minimum"));
		assertEquals("12pt", leader.getAttribute("leader-length.optimum"));
		assertEquals("100%", leader.getAttribute("leader-length.maximum"));

		assertNotNull("the leader needs text-align-last to stretch", blockWithAlignLast(fo(flags)));
	}

	/** the paragraph block, which must justify its last line for the leader to stretch */
	private static Element blockWithAlignLast(org.w3c.dom.Document doc) {
		NodeList nl = doc.getElementsByTagNameNS("http://www.w3.org/1999/XSL/Format", "block");
		for (int i = 0; i < nl.getLength(); i++) {
			Element el = (Element) nl.item(i);
			if ("justify".equals(el.getAttribute("text-align-last"))) return el;
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
}
