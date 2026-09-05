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
 * Text boxes are painted, and where Word puts them (CR-001, real documents).
 *
 * <p>A VML text box (w:pict/v:shape/v:textbox) used to be emitted as a
 * block-container with position="absolute" - not an XSL-FO property - and a top
 * but no left, width or height, nested inside an fo:inline, so FOP painted
 * nothing and letterheads built from text boxes were simply lost.  A DrawingML
 * shape's text box (wps:wsp/wps:txbx) never reached the FO at all.  Both are now
 * placed as an anchored picture is, by WordLayoutFixups.</p>
 */
public class TextBoxLayoutTest extends AbstractXSLFOTest {

	private static final String W = "xmlns:w=\"http://schemas.openxmlformats.org/wordprocessingml/2006/main\"";
	private static final String NS = W
			+ " xmlns:v=\"urn:schemas-microsoft-com:vml\""
			+ " xmlns:w10=\"urn:schemas-microsoft-com:office:word\""
			+ " xmlns:wp=\"http://schemas.openxmlformats.org/drawingml/2006/wordprocessingDrawing\""
			+ " xmlns:a=\"http://schemas.openxmlformats.org/drawingml/2006/main\""
			+ " xmlns:wps=\"http://schemas.microsoft.com/office/word/2010/wordprocessingShape\"";
	private static final String FO_NS = "http://www.w3.org/1999/XSL/Format";

	/** A4 portrait, 1in margins. */
	private static final String SECT_PR = "<w:sectPr><w:pgSz w:w=\"11906\" w:h=\"16838\"/>"
			+ "<w:pgMar w:top=\"1440\" w:right=\"1440\" w:bottom=\"1440\" w:left=\"1440\"/></w:sectPr>";

	private static final String BOX_TEXT = "Text box content";

	/** A text box 90pt from the paragraph's top and 100pt in from the column. */
	private static final String VML_TEXT_BOX =
			"<w:p><w:r><w:pict><v:shape id=\"Text Box 2\" type=\"#_x0000_t202\" stroked=\"f\""
			+ " style=\"position:absolute;margin-left:100pt;margin-top:90pt;width:180pt;height:30pt;"
			+ "mso-position-horizontal:absolute;mso-position-horizontal-relative:text;"
			+ "mso-position-vertical:absolute;mso-position-vertical-relative:text\">"
			+ "<v:textbox inset=\"0,0,0,0\"><w:txbxContent>"
			+ "<w:p><w:r><w:t>" + BOX_TEXT + "</w:t></w:r></w:p>"
			+ "</w:txbxContent></v:textbox></v:shape></w:pict></w:r></w:p>";

	/** The same box as a DrawingML shape, positioned from the paragraph. */
	private static final String DML_TEXT_BOX =
			"<w:p><w:r><w:drawing><wp:anchor distT=\"0\" distB=\"0\" distL=\"0\" distR=\"0\""
			+ " simplePos=\"0\" relativeHeight=\"1\" behindDoc=\"0\" locked=\"0\""
			+ " layoutInCell=\"1\" allowOverlap=\"1\">"
			+ "<wp:simplePos x=\"0\" y=\"0\"/>"
			+ "<wp:positionH relativeFrom=\"column\"><wp:posOffset>1270000</wp:posOffset></wp:positionH>"
			+ "<wp:positionV relativeFrom=\"paragraph\"><wp:posOffset>1143000</wp:posOffset></wp:positionV>"
			+ "<wp:extent cx=\"2286000\" cy=\"381000\"/><wp:wrapNone/><wp:docPr id=\"1\" name=\"Text Box 1\"/>"
			+ "<a:graphic><a:graphicData uri=\"http://schemas.microsoft.com/office/word/2010/wordprocessingShape\">"
			+ "<wps:wsp><wps:cNvSpPr txBox=\"1\"/><wps:spPr/>"
			+ "<wps:txbx><w:txbxContent><w:p><w:r><w:t>" + BOX_TEXT + "</w:t></w:r></w:p></w:txbxContent></wps:txbx>"
			+ "<wps:bodyPr/></wps:wsp></a:graphicData></a:graphic></wp:anchor></w:drawing></w:r></w:p>";

	private static WordprocessingMLPackage pkg(String box) throws Exception {
		WordprocessingMLPackage pkg = WordprocessingMLPackage.createPackage();
		pkg.getMainDocumentPart().setJaxbElement((Document)XmlUtils.unmarshalString(
				"<w:document " + NS + "><w:body>"
				+ box
				+ "<w:p><w:r><w:t>body text</w:t></w:r></w:p>"
				+ SECT_PR + "</w:body></w:document>"));
		return pkg;
	}

	private static org.w3c.dom.Document fo(String box, int flags) throws Exception {
		FOSettings foSettings = Docx4J.createFOSettings();
		foSettings.setWmlPackage(pkg(box));
		foSettings.setApacheFopMime(FOSettings.INTERNAL_FO_MIME);
		ByteArrayOutputStream baos = new ByteArrayOutputStream();
		Docx4J.toFO(foSettings, baos, flags);
		return XmlUtils.getNewDocumentBuilder().parse(new ByteArrayInputStream(baos.toByteArray()));
	}

	/** The absolutely positioned container holding the box's text. */
	private static Element positionedBox(org.w3c.dom.Document doc) {
		NodeList nl = doc.getElementsByTagNameNS(FO_NS, "block-container");
		for (int i = 0; i < nl.getLength(); i++) {
			Element el = (Element)nl.item(i);
			if (el.getTextContent().contains(BOX_TEXT)
					&& el.getAttribute("absolute-position").length() > 0) return el;
		}
		return null;
	}

	private void placedAbsolutely(String box, int flags, String width, String height) throws Exception {
		org.w3c.dom.Document doc = fo(box, flags);
		Element container = positionedBox(doc);
		assertNotNull("the text box was not painted (no absolutely positioned container)", container);
		assertEquals("absolute", container.getAttribute("absolute-position"));
		assertEquals("100pt", container.getAttribute("left"));
		assertEquals("90pt", container.getAttribute("top"));
		// the box is 180 x 30pt; the container's content box is that less its insets
		assertEquals(width, container.getAttribute("width"));
		assertEquals(height, container.getAttribute("height"));
		// and it takes no space in the flow: the container it sits in is zero-height
		Element wrapper = (Element)container.getParentNode();
		assertEquals("0pt", wrapper.getAttribute("height"));
		// no non-FO position property survives
		assertEquals("", container.getAttribute("position"));
	}

	@Test
	public void vmlTextBoxVisitor() throws Exception {
		placedAbsolutely(VML_TEXT_BOX, Docx4J.FLAG_NONE, "180pt", "30pt");
	}

	@Test
	public void vmlTextBoxXslt() throws Exception {
		placedAbsolutely(VML_TEXT_BOX, Docx4J.FLAG_EXPORT_PREFER_XSL, "180pt", "30pt");
	}

	@Test
	public void drawingMLTextBoxVisitor() throws Exception {
		// no wps:bodyPr insets, so Word's defaults: 0.1in left and right, 0.05in top and bottom
		placedAbsolutely(DML_TEXT_BOX, Docx4J.FLAG_NONE, "165.6pt", "22.8pt");
	}

	@Test
	public void drawingMLTextBoxXslt() throws Exception {
		placedAbsolutely(DML_TEXT_BOX, Docx4J.FLAG_EXPORT_PREFER_XSL, "165.6pt", "22.8pt");
	}

	/** A box Word wraps text around that fills the column reserves its height where
	 *  Word puts it, rather than being given to fo:float (FOP's side floats throw on
	 *  documents in this corpus) or overlaying the text. */
	@Test
	public void aWideWrappedTextBoxReservesItsHeight() throws Exception {
		// 400pt of a 451.3pt column: Word has no room to flow text beside it
		String wrapped = VML_TEXT_BOX.replace("width:180pt", "width:400pt")
				.replace("</v:textbox>", "</v:textbox><w10:wrap type=\"square\"/>");
		org.w3c.dom.Document doc = fo(wrapped, Docx4J.FLAG_NONE);
		assertTrue("a wrapped text box should not be an fo:float",
				doc.getElementsByTagNameNS(FO_NS, "float").getLength() == 0);
		Element wrapper = null;
		NodeList nl = doc.getElementsByTagNameNS(FO_NS, "block-container");
		for (int i = 0; i < nl.getLength(); i++) {
			Element el = (Element)nl.item(i);
			if (el.getTextContent().contains(BOX_TEXT) && el.getAttribute("start-indent").equals("100pt")) {
				wrapper = el;
			}
		}
		assertNotNull("no container reserving the box's space", wrapper);
		assertEquals("30pt", wrapper.getAttribute("height"));
		assertEquals("90pt", wrapper.getAttribute("padding-top"));
		assertEquals("", wrapper.getAttribute("absolute-position"));
	}

	/** A narrower one is placed where Word puts it and takes no space: Word flows the
	 *  text beside it, and several such boxes side by side (a planner built from text
	 *  boxes) would otherwise cost a page each. */
	@Test
	public void aNarrowWrappedTextBoxIsPlacedWhereWordPutsIt() throws Exception {
		String wrapped = VML_TEXT_BOX.replace("</v:textbox>",
				"</v:textbox><w10:wrap type=\"square\"/>");
		Element container = positionedBox(fo(wrapped, Docx4J.FLAG_NONE));
		assertNotNull("the narrow wrapped box was not placed", container);
		assertEquals("100pt", container.getAttribute("left"));
		assertEquals("0pt", ((Element)container.getParentNode()).getAttribute("height"));
	}

	/** The text really is painted, at the box's position: 72pt page margin + 100pt. */
	@Test
	public void theTextIsPaintedWhereWordPutsIt() throws Exception {
		org.w3c.dom.Document areaTree = areaTree(pkg(VML_TEXT_BOX), Docx4J.FLAG_NONE);
		Element viewport = null;
		NodeList blocks = areaTree.getElementsByTagName("block");
		for (int i = 0; i < blocks.getLength(); i++) {
			Element block = (Element)blocks.item(i);
			if ("absolute".equals(block.getAttribute("positioning"))
					&& block.getTextContent().contains(BOX_TEXT)) viewport = block;
		}
		assertNotNull("the text box has no area in the rendered page", viewport);
		// in millipoints, from the column's left edge and the paragraph's top
		assertEquals("100000", viewport.getAttribute("left-position"));
		assertEquals("90000", viewport.getAttribute("top-position"));
		assertEquals("180000", viewport.getAttribute("ipd"));
		assertEquals("30000", viewport.getAttribute("bpd"));
	}
}
