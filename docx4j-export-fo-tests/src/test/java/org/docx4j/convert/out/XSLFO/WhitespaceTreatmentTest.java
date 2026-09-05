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
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

/**
 * Whitespace as Word paints it (CR-001 &#xa7;4.5), and where the property that keeps it
 * may be written.
 *
 * <p><b>Leading whitespace.</b> Word paints the spaces at the start of a paragraph -
 * documents use them to position text - while XSL-FO's default white-space-treatment,
 * ignore-if-surrounding-linefeed, deletes them: FOP's XMLWhiteSpaceHandler treats the
 * start of a block as "after a linefeed".  Measured against Word's own PDFs: a run whose
 * w:t is xml:space="preserve" with eighteen leading spaces at 13pt Times New Roman
 * started 58.8pt left of Word's line, and five leading spaces in a right-aligned cell
 * cost 19.96pt.</p>
 *
 * <p><b>But not by preserving whitespace on the block</b>, which also keeps the space at
 * every line-break opportunity: measured on a document whose first body paragraph is
 * justified and begins with ten literal spaces, Word's continuation lines all start at
 * x=113.3 where ours ran 119.0 / 117.0 / 117.9 / 120.0.  The whitespace itself becomes an
 * fo:leader of its measured width instead, as a leading tab already is.</p>
 *
 * <p><b>And where the property goes</b> when it is still needed (the empty-paragraph
 * placeholder).  white-space-treatment is an <em>inherited</em>
 * property which FOP reads from the nearest ancestor fo:block, so it has to be written on
 * the paragraph's block - but where that block is also a container (a paragraph whose
 * objects were lifted into positioned fo:block-containers), every block inside those
 * containers inherited it and kept its own leading whitespace.  Measured: one paragraph
 * enclosing forty positioned text boxes moved every continuation line inside them from
 * x=72.0 to 74.2, exactly one 8pt Arimo space, and the narrower measure re-broke the
 * text; 174 such blocks in 53 of 156 corpus documents.</p>
 *
 * @since 17.0.6
 */
public class WhitespaceTreatmentTest extends AbstractXSLFOTest {

	private static final String W = "xmlns:w=\"http://schemas.openxmlformats.org/wordprocessingml/2006/main\"";
	private static final String NS = W + " xmlns:v=\"urn:schemas-microsoft-com:vml\"";
	private static final String FO_NS = "http://www.w3.org/1999/XSL/Format";

	private static final String SECT_PR = "<w:sectPr><w:pgSz w:w=\"11906\" w:h=\"16838\"/>"
			+ "<w:pgMar w:top=\"1440\" w:right=\"1440\" w:bottom=\"1440\" w:left=\"1440\"/></w:sectPr>";

	/** A paragraph whose only content is five leading spaces and a word. */
	private static final String LEADING_SPACES =
			"<w:p><w:r><w:t xml:space=\"preserve\">     Indented by spaces</w:t></w:r></w:p>";

	/** The same word with no leading spaces, as the control. */
	private static final String NO_SPACES =
			"<w:p><w:r><w:t>Indented by spaces</w:t></w:r></w:p>";

	/** A paragraph whose runs are all lifted out: an absolutely positioned text box whose
	 *  own paragraph starts with a space.  The paragraph itself gets the empty-line
	 *  placeholder (&#xa7;2.5), and the box must not inherit its preserve. */
	private static final String BOX_ONLY_PARAGRAPH =
			"<w:p><w:r><w:pict><v:shape id=\"Text Box 2\" type=\"#_x0000_t202\" stroked=\"f\""
			+ " style=\"position:absolute;margin-left:100pt;margin-top:90pt;width:180pt;height:30pt;"
			+ "mso-position-horizontal:absolute;mso-position-horizontal-relative:text;"
			+ "mso-position-vertical:absolute;mso-position-vertical-relative:text\">"
			+ "<v:textbox inset=\"0,0,0,0\"><w:txbxContent>"
			+ "<w:p><w:r><w:t>Text box content</w:t></w:r></w:p>"
			+ "</w:txbxContent></v:textbox></v:shape></w:pict></w:r></w:p>";

	private static WordprocessingMLPackage pkg(String body) throws Exception {
		WordprocessingMLPackage pkg = WordprocessingMLPackage.createPackage();
		pkg.getMainDocumentPart().setJaxbElement((Document)XmlUtils.unmarshalString(
				"<w:document " + NS + "><w:body>" + body + SECT_PR + "</w:body></w:document>"));
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

	// ---------------------------------------------------------------- leading whitespace

	private void leadingSpacesArePreserved(int flags) throws Exception {
		NodeList blocks = fo(pkg(LEADING_SPACES), flags).getElementsByTagNameNS(FO_NS, "block");
		boolean found = false;
		for (int i = 0; i < blocks.getLength(); i++) {
			Element b = (Element) blocks.item(i);
			if (!b.getTextContent().contains("Indented by spaces")) continue;
			found = true;
			assertEquals("the leading spaces must be a leader, not text", "Indented by spaces",
					b.getTextContent());
			org.w3c.dom.NodeList leaders = b.getElementsByTagNameNS(FO_NS, "leader");
			assertEquals("one leader, of the width of the five spaces", 1, leaders.getLength());
			Element leader = (Element)leaders.item(0);
			assertEquals("space", leader.getAttribute("leader-pattern"));
			assertTrue("no width: " + leader.getAttribute("leader-length"),
					Double.parseDouble(leader.getAttribute("leader-length").replace("pt", "")) > 4);
			assertEquals("white-space-treatment=preserve also keeps the space at every"
					+ " line-break opportunity, so the block stays on the default",
					"", b.getAttribute("white-space-treatment"));
		}
		assertTrue("the paragraph was not found in the FO", found);
	}

	@Test
	public void leadingSpacesArePreservedVisitor() throws Exception {
		leadingSpacesArePreserved(Docx4J.FLAG_NONE);
	}

	@Test
	public void leadingSpacesArePreservedXslt() throws Exception {
		leadingSpacesArePreserved(Docx4J.FLAG_EXPORT_PREFER_XSL);
	}

	/** A paragraph with no leading whitespace keeps the XSL-FO default, which is what
	 *  makes a run of spaces at a line end hang there rather than indent the next line. */
	private void anOrdinaryParagraphIsUnchanged(int flags) throws Exception {
		NodeList blocks = fo(pkg(NO_SPACES), flags).getElementsByTagNameNS(FO_NS, "block");
		for (int i = 0; i < blocks.getLength(); i++) {
			Element b = (Element) blocks.item(i);
			if (!b.getTextContent().contains("Indented by spaces")) continue;
			assertEquals("no leading whitespace, so nothing to preserve",
					"", b.getAttribute("white-space-treatment"));
		}
	}

	@Test
	public void anOrdinaryParagraphIsUnchangedVisitor() throws Exception {
		anOrdinaryParagraphIsUnchanged(Docx4J.FLAG_NONE);
	}

	@Test
	public void anOrdinaryParagraphIsUnchangedXslt() throws Exception {
		anOrdinaryParagraphIsUnchanged(Docx4J.FLAG_EXPORT_PREFER_XSL);
	}

	/** Word paints them, so the line is five spaces wider than the same text without
	 *  them: measured in the area tree, which is what PDF gets. */
	@Test
	public void thePaintedLineIsWider() throws Exception {
		int with = firstLineWidth(areaTree(pkg(LEADING_SPACES), Docx4J.FLAG_NONE));
		int without = firstLineWidth(areaTree(pkg(NO_SPACES), Docx4J.FLAG_NONE));
		assertTrue("the leading spaces were dropped: " + with + " vs " + without + " millipoints",
				with - without > 4000);
	}

	/** The inline-progression dimension of everything on the first line - the leading
	 *  whitespace is a leader area, not a text area. */
	private static int firstLineWidth(org.w3c.dom.Document areaTree) {
		NodeList lines = areaTree.getElementsByTagName("lineArea");
		assertTrue("no lineArea", lines.getLength() > 0);
		return sumIpd((Element) lines.item(0));
	}

	private static int sumIpd(Element parent) {
		int total = 0;
		for (Node n = parent.getFirstChild(); n != null; n = n.getNextSibling()) {
			if (!(n instanceof Element)) continue;
			Element el = (Element) n;
			String ipd = el.getAttribute("ipd");
			if (ipd.length() > 0) {
				total += Integer.parseInt(ipd);
			} else {
				total += sumIpd(el);
			}
		}
		return total;
	}

	// ---------------------------------------------------------------- inheritance

	private void aContainerDoesNotPassPreserveOn(int flags) throws Exception {
		org.w3c.dom.Document doc = fo(pkg(BOX_ONLY_PARAGRAPH + LEADING_SPACES), flags);
		NodeList blocks = doc.getElementsByTagNameNS(FO_NS, "block");
		int containers = 0;
		for (int i = 0; i < blocks.getLength(); i++) {
			Element b = (Element) blocks.item(i);
			if (!"preserve".equals(b.getAttribute("white-space-treatment"))) continue;
			for (Node n = b.getFirstChild(); n != null; n = n.getNextSibling()) {
				if (!(n instanceof Element)) continue;
				Element child = (Element) n;
				if (!FO_NS.equals(child.getNamespaceURI())) continue;
				if (!"block-container".equals(child.getLocalName())
						&& !"float".equals(child.getLocalName())) continue;
				containers++;
				assertTrue("a positioned container inherits the paragraph's preserve,"
						+ " so every line inside it keeps its leading whitespace",
						child.getAttribute("white-space-treatment").length() > 0
							&& !"preserve".equals(child.getAttribute("white-space-treatment")));
			}
		}
		assertTrue("the text box was not lifted into a container", containers > 0);
	}

	@Test
	public void aContainerDoesNotPassPreserveOnVisitor() throws Exception {
		aContainerDoesNotPassPreserveOn(Docx4J.FLAG_NONE);
	}

	@Test
	public void aContainerDoesNotPassPreserveOnXslt() throws Exception {
		aContainerDoesNotPassPreserveOn(Docx4J.FLAG_EXPORT_PREFER_XSL);
	}
}
