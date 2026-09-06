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
	 * A {@code w:vAnchor="text"} frame is drawn at its anchor paragraph's position
	 * offset by {@code w:x}/{@code w:y}, with the text that follows running beside it:
	 * an {@code fo:float} holding a one-row table whose columns are the offset from the
	 * column edge, the frame's {@code w:w}, and the {@code w:hSpace} gap to the text
	 * (&#xa7;9.5, the machinery &#xa7;6.8 uses for a text-anchored floating table).
	 *
	 * <p>The column here is 451.3pt (A4 less 72pt margins): {@code w:w=2880} is 144pt of
	 * it, at {@code w:x=720} = 36pt, {@code w:hSpace=180} = 9pt.</p>
	 */
	private static final String TEXT_ANCHORED =
			"<w:framePr w:w=\"2880\" w:hSpace=\"180\" w:vSpace=\"120\" w:wrap=\"around\""
			+ " w:vAnchor=\"text\" w:hAnchor=\"text\" w:x=\"720\" w:y=\"240\"/>";

	private void textAnchoredIsAFloat(int flags) throws Exception {
		org.w3c.dom.Document fo = fo(pkg(TEXT_ANCHORED, 1), flags);
		assertEquals("no positioned container for a text-anchored frame", null, absoluteContainer(fo));
		Element fl = theFloat(fo);
		assertNotNull("the frame was left in the flow", fl);
		assertEquals("the nearer edge", "left", fl.getAttribute("float"));
		Element block = (Element) fl.getElementsByTagNameNS(FO, "block").item(0);
		assertEquals("w:y is padding above the frame", 12.0, pt(block.getAttribute("padding-top")), 0.05);
		assertEquals("w:vSpace below it", 6.0, pt(block.getAttribute("padding-bottom")), 0.05);
		Element band = (Element) fl.getElementsByTagNameNS(FO, "table").item(0);
		assertNotNull("no band table", band);
		assertEquals("the band: w:x + w:w + w:hSpace", 189.0, pt(band.getAttribute("width")), 0.05);
		NodeList columns = band.getElementsByTagNameNS(FO, "table-column");
		assertEquals("three columns", 3, columns.getLength());
		assertEquals("w:x", 36.0, pt(((Element) columns.item(0)).getAttribute("column-width")), 0.05);
		assertEquals("w:w", 144.0, pt(((Element) columns.item(1)).getAttribute("column-width")), 0.05);
		assertEquals("w:hSpace", 9.0, pt(((Element) columns.item(2)).getAttribute("column-width")), 0.05);
		// the float anchors inside the paragraph that follows the frame, which is where
		// FOP has a line to hang it on
		Element anchor = (Element) fl.getParentNode();
		assertEquals("fo:block", "block", anchor.getLocalName());
		assertTrue("the float is not at the paragraph after the frame",
				anchor.getTextContent().contains("after the frame"));
	}

	@Test
	public void textAnchoredIsAFloatVisitor() throws Exception {
		textAnchoredIsAFloat(Docx4J.FLAG_NONE);
	}

	@Test
	public void textAnchoredIsAFloatXslt() throws Exception {
		textAnchoredIsAFloat(Docx4J.FLAG_EXPORT_PREFER_XSL);
	}

	/** A frame nearer the right edge of the column floats to it. */
	@Test
	public void textAnchoredFloatsToTheNearerEdge() throws Exception {
		Element fl = theFloat(fo(pkg(TEXT_ANCHORED.replace("w:x=\"720\"", "w:x=\"6000\""), 1),
				Docx4J.FLAG_NONE));
		assertNotNull(fl);
		assertEquals("right", fl.getAttribute("float"));
		Element band = (Element) fl.getElementsByTagNameNS(FO, "table").item(0);
		// w:hSpace + w:w + what is left of the column: 9 + 144 + (451.3 - 300 - 144)
		assertEquals(160.3, pt(band.getAttribute("width")), 0.05);
	}

	/**
	 * A frame with no {@code w:w} fills the rest of the measure, so nothing runs beside
	 * it: it stays in the flow.  That is the shape of all 499 frames of the corpus
	 * document that is this rule's acid test.
	 */
	@Test
	public void textAnchoredWithNoWidthStaysInTheFlow() throws Exception {
		org.w3c.dom.Document fo = fo(pkg(
				"<w:framePr w:hSpace=\"141\" w:wrap=\"around\" w:hAnchor=\"text\""
				+ " w:vAnchor=\"text\" w:x=\"108\" w:y=\"1\"/>", 1), Docx4J.FLAG_NONE);
		assertEquals("no float", null, theFloat(fo));
		assertEquals("no positioned container", null, absoluteContainer(fo));
	}

	/** A frame over 60% of the column has no useful measure beside it: in the flow. */
	@Test
	public void textAnchoredTooWideStaysInTheFlow() throws Exception {
		assertEquals(null, theFloat(fo(pkg(TEXT_ANCHORED.replace("w:w=\"2880\"", "w:w=\"7000\""), 1),
				Docx4J.FLAG_NONE)));
	}

	/** w:wrap="notBeside" says no text may run beside the frame; in the flow it already
	 *  reserves its own band. */
	@Test
	public void textAnchoredNotBesideStaysInTheFlow() throws Exception {
		assertEquals(null, theFloat(fo(pkg(
				TEXT_ANCHORED.replace("w:wrap=\"around\"", "w:wrap=\"notBeside\""), 1),
				Docx4J.FLAG_NONE)));
	}

	/** The float holds the frame's own paragraphs, and they are out of the flow. */
	@Test
	public void theFrameLeavesTheFlow() throws Exception {
		org.w3c.dom.Document fo = fo(pkg(TEXT_ANCHORED, 2), Docx4J.FLAG_NONE);
		Element fl = theFloat(fo);
		assertNotNull(fl);
		assertTrue("both paragraphs went into the one float",
				fl.getTextContent().contains("frame line 1") && fl.getTextContent().contains("frame line 2"));
		Element flow = (Element) fo.getElementsByTagNameNS(FO, "flow").item(0);
		int inFlow = 0;
		for (org.w3c.dom.Node n = flow.getFirstChild(); n != null; n = n.getNextSibling()) {
			if (n instanceof Element && "block".equals(((Element) n).getLocalName())) inFlow++;
		}
		assertEquals("only the two unframed paragraphs are left at flow level", 2, inFlow);
	}

	/** FOP lays the float out, and the lines beside it are held off the frame. */
	@Test
	public void theFloatNarrowsTheLinesBesideIt() throws Exception {
		StringBuilder body = new StringBuilder("<w:p><w:r><w:t>before the frame</w:t></w:r></w:p>");
		body.append("<w:p><w:pPr>").append(TEXT_ANCHORED).append("</w:pPr>")
			.append("<w:r><w:t>in the frame</w:t></w:r></w:p>");
		for (int i = 0; i < 8; i++) {
			body.append("<w:p><w:r><w:t>Lorem ipsum dolor sit amet, consectetur adipiscing"
					+ " elit, sed do eiusmod tempor incididunt ut labore et dolore magna"
					+ " aliqua. Ut enim ad minim veniam, quis nostrud.</w:t></w:r></w:p>");
		}
		WordprocessingMLPackage pkg = WordprocessingMLPackage.createPackage();
		pkg.getMainDocumentPart().setJaxbElement((Document) XmlUtils.unmarshalString(
				"<w:document " + W + "><w:body>" + body + SECT_PR + "</w:body></w:document>"));
		org.w3c.dom.Document areaTree = areaTree(pkg, Docx4J.FLAG_NONE);
		assertTrue("nothing was laid out", lineCount(areaTree) > 10);
		// FOP narrows a line beside a side float by taking the float's ipd off it (it
		// does not serialise the float's own areas): the 451.3pt column less the 189pt
		// band the frame reserves is 262.3pt
		int narrowed = 0, full = 0;
		NodeList lines = areaTree.getElementsByTagName("lineArea");
		for (int i = 0; i < lines.getLength(); i++) {
			String ipd = ((Element) lines.item(i)).getAttribute("ipd");
			if ("262300".equals(ipd)) narrowed++;
			else if ("451300".equals(ipd)) full++;
		}
		assertTrue("no line was narrowed by the frame's float", narrowed > 0);
		assertTrue("every line was narrowed", full > 0);
	}

	/**
	 * {@code w:dropCap="drop"}: the framed paragraph is the cap, and the first
	 * {@code w:lines} lines of the paragraph that follows run beside it.  Word writes
	 * the enlarged size into the run itself, so what is reproduced here is the band -
	 * the cap's own advance by {@code w:lines} lines of the following paragraph's pitch.
	 */
	@Test
	public void dropCapIsAFloat() throws Exception {
		Element fl = theFloat(dropCap("drop"));
		assertNotNull("the drop cap was left in the flow", fl);
		assertEquals("left", fl.getAttribute("float"));
		assertNotNull("no band table", fl.getElementsByTagNameNS(FO, "table").item(0));
		assertTrue("the cap is not in the float", fl.getTextContent().contains("D"));
		Element cap = null;
		NodeList blocks = fl.getElementsByTagNameNS(FO, "block");
		for (int i = 0; i < blocks.getLength(); i++) {
			if (((Element) blocks.item(i)).getTextContent().startsWith("D")) cap = (Element) blocks.item(i);
		}
		assertNotNull(cap);
		assertTrue("the cap's band is not three lines deep",
				pt(cap.getAttribute("line-height")) > 30);
	}

	/** {@code w:dropCap="margin"} hangs the cap in the margin: it takes no space. */
	@Test
	public void dropCapInTheMarginIsPositioned() throws Exception {
		org.w3c.dom.Document fo = dropCap("margin");
		assertEquals("a margin drop cap is not a float", null, theFloat(fo));
		Element abs = absoluteContainer(fo);
		assertNotNull("the drop cap was left in the flow", abs);
		assertTrue("the cap is not hung to the left of the column",
				pt(abs.getAttribute("left")) < 0);
	}

	private static org.w3c.dom.Document dropCap(String kind) throws Exception {
		StringBuilder body = new StringBuilder();
		body.append("<w:p><w:pPr><w:framePr w:dropCap=\"").append(kind)
			.append("\" w:lines=\"3\" w:hSpace=\"142\" w:wrap=\"around\" w:vAnchor=\"text\""
					+ " w:hAnchor=\"text\"/></w:pPr>")
			.append("<w:r><w:rPr><w:sz w:val=\"116\"/></w:rPr><w:t>D</w:t></w:r></w:p>");
		body.append("<w:p><w:r><w:t>rop caps set the paragraph's first letter into the"
				+ " text beside it, spanning three lines of the paragraph that"
				+ " follows.</w:t></w:r></w:p>");
		WordprocessingMLPackage pkg = WordprocessingMLPackage.createPackage();
		pkg.getMainDocumentPart().setJaxbElement((Document) XmlUtils.unmarshalString(
				"<w:document " + W + "><w:body>" + body + SECT_PR + "</w:body></w:document>"));
		return fo(pkg, Docx4J.FLAG_NONE);
	}

	private static Element theFloat(org.w3c.dom.Document fo) {
		NodeList floats = fo.getElementsByTagNameNS(FO, "float");
		return floats.getLength() == 0 ? null : (Element) floats.item(0);
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

	/**
	 * {@code w:wrap="notBeside"} lets no text run beside the frame, so Word's flow steps
	 * over the frame's band and resumes below it.  The band is an invisible copy of the
	 * frame's own blocks, left where they were: FOP honours {@code visibility="hidden"},
	 * so the area keeps its size and paints nothing, which reserves exactly the height
	 * the frame occupies where {@code w:h} cannot (w:hRule="auto" is the common case).
	 */
	private void notBesideReservesItsBand(int flags) throws Exception {
		org.w3c.dom.Document fo = fo(pkg(PAGE_ANCHORED, 1), flags);
		assertNotNull(absoluteContainer(fo));
		assertEquals("one invisible copy in the flow", 1, hiddenCopies(fo));
	}

	@Test
	public void notBesideReservesItsBandVisitor() throws Exception {
		notBesideReservesItsBand(Docx4J.FLAG_NONE);
	}

	@Test
	public void notBesideReservesItsBandXslt() throws Exception {
		notBesideReservesItsBand(Docx4J.FLAG_EXPORT_PREFER_XSL);
	}

	/** w:wrap="around" does let text run beside the frame, so nothing is reserved. */
	@Test
	public void aroundReservesNothing() throws Exception {
		org.w3c.dom.Document fo = fo(pkg(
				PAGE_ANCHORED.replace("w:wrap=\"notBeside\"", "w:wrap=\"around\""), 1),
				Docx4J.FLAG_NONE);
		assertNotNull(absoluteContainer(fo));
		assertEquals("nothing reserved", 0, hiddenCopies(fo));
	}

	/** Two frames at the same w:y share one band: Word steps over the pair once. */
	@Test
	public void framesAtOneAnchorShareTheirBand() throws Exception {
		StringBuilder body = new StringBuilder();
		body.append("<w:p><w:pPr>").append(PAGE_ANCHORED).append("</w:pPr>")
			.append("<w:r><w:t>left of the pair</w:t></w:r></w:p>");
		// the same w:y, a different w:x: a second frame in the same band
		body.append("<w:p><w:pPr>")
			.append(PAGE_ANCHORED.replace("w:x=\"8563\"", "w:x=\"1362\""))
			.append("</w:pPr><w:r><w:t>right of the pair</w:t></w:r></w:p>");
		WordprocessingMLPackage pkg = WordprocessingMLPackage.createPackage();
		pkg.getMainDocumentPart().setJaxbElement((Document) XmlUtils.unmarshalString(
				"<w:document " + W + "><w:body>" + body + SECT_PR + "</w:body></w:document>"));
		org.w3c.dom.Document fo = fo(pkg, Docx4J.FLAG_NONE);
		assertEquals("both frames positioned", 2, positionedContainers(fo));
		assertEquals("but only one band reserved", 1, hiddenCopies(fo));
	}

	/** fo:blocks left in the flow with visibility="hidden": the reserved bands. */
	private static int hiddenCopies(org.w3c.dom.Document fo) {
		int n = 0;
		NodeList blocks = fo.getElementsByTagNameNS(FO, "block");
		for (int i = 0; i < blocks.getLength(); i++) {
			if ("hidden".equals(((Element) blocks.item(i)).getAttribute("visibility"))) n++;
		}
		return n;
	}

	private static int positionedContainers(org.w3c.dom.Document fo) {
		int n = 0;
		NodeList containers = fo.getElementsByTagNameNS(FO, "block-container");
		for (int i = 0; i < containers.getLength(); i++) {
			if (((Element) containers.item(i)).getAttribute("absolute-position").length() > 0) n++;
		}
		return n;
	}
}
