package org.docx4j.convert.out.XSLFO;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
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
 * {@code w:pPr/w:framePr}: Word's positioned text frames (&#xa7;9.5).
 *
 * <p>Measured against Word's own PDF of a corpus document whose frame is
 * {@code w:w=2926 w:h=748 w:hRule=exact w:vAnchor=page w:hAnchor=page w:x=8563
 * w:y=1702}: Word draws its text at x=428.3 y=95.3, i.e. the box at 428.15 / 85.1 from
 * the page's top left corner - {@code w:x} and {@code w:y} exactly - where docx4j drew it
 * in the flow at x=68.1 y=81.2.</p>
 *
 * @since 17.0.6
 */
public class FramePrTest extends AbstractXSLFOTest {

	/** The rule is off by default (see WordLayoutFixups.positionFrames); these tests are
	 *  about what it does when it is on. */
	@org.junit.Before
	public void on() {
		org.docx4j.Docx4jProperties.setProperty("docx4j.convert.out.fo.frames.position", true);
	}

	@org.junit.After
	public void off() {
		org.docx4j.Docx4jProperties.setProperty("docx4j.convert.out.fo.frames.position", false);
	}

	private static final String W = "xmlns:w=\"http://schemas.openxmlformats.org/wordprocessingml/2006/main\"";
	private static final String FO = "http://www.w3.org/1999/XSL/Format";

	private static final String SECT_PR = "<w:sectPr><w:pgSz w:w=\"11906\" w:h=\"16838\"/>"
			+ "<w:pgMar w:top=\"1440\" w:right=\"1440\" w:bottom=\"1440\" w:left=\"1440\"/></w:sectPr>";

	private static WordprocessingMLPackage pkg(String framePr, int paragraphs) throws Exception {
		StringBuilder body = new StringBuilder("<w:p><w:r><w:t>before the frame</w:t></w:r></w:p>");
		for (int i = 0; i < paragraphs; i++) {
			body.append("<w:p><w:pPr>").append(framePr).append("</w:pPr>")
				.append("<w:r><w:t>frame line ").append(i + 1).append("</w:t></w:r></w:p>");
		}
		body.append("<w:p><w:r><w:t>after the frame</w:t></w:r></w:p>");
		WordprocessingMLPackage pkg = WordprocessingMLPackage.createPackage();
		pkg.getMainDocumentPart().setJaxbElement((Document) XmlUtils.unmarshalString(
				"<w:document " + W + "><w:body>" + body + SECT_PR + "</w:body></w:document>"));
		return pkg;
	}

	private static org.w3c.dom.Document fo(WordprocessingMLPackage pkg, int flags) throws Exception {
		FOSettings foSettings = Docx4J.createFOSettings();
		foSettings.setWmlPackage(pkg);
		foSettings.setApacheFopMime(FOSettings.INTERNAL_FO_MIME);
		ByteArrayOutputStream baos = new ByteArrayOutputStream();
		Docx4J.toFO(foSettings, baos, flags);
		return XmlUtils.getNewDocumentBuilder().parse(new ByteArrayInputStream(baos.toByteArray()));
	}

	private static Element absoluteContainer(org.w3c.dom.Document fo) {
		NodeList containers = fo.getElementsByTagNameNS(FO, "block-container");
		for (int i = 0; i < containers.getLength(); i++) {
			Element c = (Element) containers.item(i);
			if (c.getAttribute("absolute-position").length() > 0) return c;
		}
		return null;
	}

	private static double pt(String v) {
		assertTrue("no value", v != null && v.length() > 0);
		if (v.endsWith("pt")) v = v.substring(0, v.length() - 2);
		if (v.endsWith("in")) return Double.parseDouble(v.substring(0, v.length() - 2)) * 72;
		if (v.endsWith("mm")) return Double.parseDouble(v.substring(0, v.length() - 2)) * 72 / 25.4;
		return Double.parseDouble(v);
	}

	private static final String PAGE_ANCHORED =
			"<w:framePr w:w=\"2926\" w:h=\"748\" w:hRule=\"exact\" w:wrap=\"notBeside\""
			+ " w:vAnchor=\"page\" w:hAnchor=\"page\" w:x=\"8563\" w:y=\"1702\"/>";

	/** A page-anchored frame lands at w:x / w:y from the page's own corner. */
	private void pageAnchored(int flags) throws Exception {
		Element abs = absoluteContainer(fo(pkg(PAGE_ANCHORED, 1), flags));
		assertNotNull("the frame was left in the flow", abs);
		assertEquals("absolute-position", "fixed", abs.getAttribute("absolute-position"));
		assertEquals("left", 428.15, pt(abs.getAttribute("left")), 0.05);
		assertEquals("top", 85.1, pt(abs.getAttribute("top")), 0.05);
		assertEquals("width", 146.3, pt(abs.getAttribute("width")), 0.05);
		assertEquals("height (w:hRule exact)", 37.4, pt(abs.getAttribute("height")), 0.05);
	}

	@Test
	public void pageAnchoredVisitor() throws Exception {
		pageAnchored(Docx4J.FLAG_NONE);
	}

	@Test
	public void pageAnchoredXslt() throws Exception {
		pageAnchored(Docx4J.FLAG_EXPORT_PREFER_XSL);
	}

	/** Consecutive paragraphs carrying the same w:framePr are one frame. */
	@Test
	public void consecutiveParagraphsAreOneFrame() throws Exception {
		org.w3c.dom.Document fo = fo(pkg(PAGE_ANCHORED, 3), Docx4J.FLAG_NONE);
		Element abs = absoluteContainer(fo);
		assertNotNull(abs);
		assertEquals("three paragraphs, one frame", 3,
				abs.getElementsByTagNameNS(FO, "block").getLength());
		int positioned = 0;
		NodeList containers = fo.getElementsByTagNameNS(FO, "block-container");
		for (int i = 0; i < containers.getLength(); i++) {
			if (((Element) containers.item(i)).getAttribute("absolute-position").length() > 0) positioned++;
		}
		assertEquals("one positioned container", 1, positioned);
	}

	/**
	 * A margin-anchored frame is measured from the text margin: w:x 1440 on a 72pt
	 * margin is 144pt from the page's left edge.
	 */
	@Test
	public void marginAnchored() throws Exception {
		Element abs = absoluteContainer(fo(pkg(
				"<w:framePr w:w=\"2880\" w:vAnchor=\"margin\" w:hAnchor=\"margin\""
				+ " w:x=\"1440\" w:y=\"720\"/>", 1), Docx4J.FLAG_NONE));
		assertNotNull("the frame was left in the flow", abs);
		assertEquals("left", 144.0, pt(abs.getAttribute("left")), 0.05);
		assertEquals("top", 108.0, pt(abs.getAttribute("top")), 0.05);
	}

	/**
	 * A {@code w:vAnchor="text"} frame is positioned against the paragraph it belongs to
	 * and Word wraps the body text around it: it is left in the flow, since moving it
	 * into a container of its own width was measured a clear loss (see
	 * {@code WordLayoutFixups.positionFrame}).
	 */
	@Test
	public void textAnchoredIsLeftInTheFlow() throws Exception {
		org.w3c.dom.Document fo = fo(pkg(
				"<w:framePr w:w=\"5197\" w:vAnchor=\"text\" w:hAnchor=\"page\" w:x=\"4170\" w:y=\"48\"/>",
				1), Docx4J.FLAG_NONE);
		assertEquals("no positioned container for a text-anchored frame", null, absoluteContainer(fo));
	}

	/** The hint never reaches FOP. */
	@Test
	public void theHintIsStripped() throws Exception {
		org.w3c.dom.Document fo = fo(pkg(PAGE_ANCHORED, 1), Docx4J.FLAG_NONE);
		NodeList blocks = fo.getElementsByTagNameNS(FO, "block");
		for (int i = 0; i < blocks.getLength(); i++) {
			assertEquals("docx4j-frame left on the FO", "",
					((Element) blocks.item(i)).getAttribute("docx4j-frame"));
		}
	}
}
