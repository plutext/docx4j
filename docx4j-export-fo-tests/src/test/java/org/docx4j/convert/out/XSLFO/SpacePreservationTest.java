package org.docx4j.convert.out.XSLFO;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.io.ByteArrayOutputStream;
import java.util.ArrayList;
import java.util.List;

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
 * Word renders every space in a run of spaces; XSL-FO collapses a run to one
 * unless the block says white-space-collapse="false", so lines which used spaces
 * to line something up (typically after tabs) came out short - on one measured
 * real-document line, eight spaces were rendered as one, 21pt narrower than Word.
 *
 * <p>The property has to go on the fo:block: FOP takes it from the nearest
 * ancestor block, so setting it on the fo:inline holding the spaces does nothing.
 * white-space-treatment stays at its default, which is what keeps issue 369's
 * case covered: a space at a line boundary is glue, and FOP drops glue at the
 * start and end of each line, so a wrapped line starts flush and a run of spaces
 * at a line end hangs there - which is also what Word does.</p>
 *
 * @since 17.0.5
 */
public class SpacePreservationTest extends AbstractXSLFOTest {

	private static final String W = "xmlns:w=\"http://schemas.openxmlformats.org/wordprocessingml/2006/main\"";
	private static final String FO_NS = "http://www.w3.org/1999/XSL/Format";
	private static final String FONT = "<w:rPr><w:rFonts w:ascii=\"Liberation Serif\" w:hAnsi=\"Liberation Serif\"/></w:rPr>";

	/** Enough text to wrap on an A4 page with 1in margins. */
	private static final String LONG =
			"Lorem ipsum dolor sit amet consectetur adipiscing elit sed do eiusmod tempor "
			+ "incididunt ut labore et dolore magna aliqua enim ad minim veniam quis nostrud";

	private static WordprocessingMLPackage pkg(String text) throws Exception {
		WordprocessingMLPackage pkg = WordprocessingMLPackage.createPackage();
		pkg.getMainDocumentPart().setJaxbElement((Document)XmlUtils.unmarshalString(
				"<w:document " + W + "><w:body>"
				+ "<w:p><w:r>" + FONT + "<w:t xml:space=\"preserve\">" + text + "</w:t></w:r></w:p>"
				+ "</w:body></w:document>"));
		return pkg;
	}

	private static org.w3c.dom.Document fo(String text, int flags) throws Exception {
		FOSettings foSettings = Docx4J.createFOSettings();
		foSettings.setWmlPackage(pkg(text));
		foSettings.setApacheFopMime(FOSettings.INTERNAL_FO_MIME);
		ByteArrayOutputStream baos = new ByteArrayOutputStream();
		Docx4J.toFO(foSettings, baos, flags);
		return XmlUtils.getNewDocumentBuilder().parse(new java.io.ByteArrayInputStream(baos.toByteArray()));
	}

	// ---- the FO

	private void checkFo(int flags) throws Exception {
		org.w3c.dom.Document doc = fo("B  C", flags);
		NodeList blocks = doc.getElementsByTagNameNS(FO_NS, "block");
		assertTrue("no fo:block", blocks.getLength() > 0);
		Element block = (Element) blocks.item(0);
		assertEquals("white-space-collapse must be false, or the spaces are lost",
				"false", block.getAttribute("white-space-collapse"));
		// ... and white-space-treatment must NOT be preserve: that is what caused the
		// unwanted indent after a wrapped line (issue 369)
		assertEquals("", block.getAttribute("white-space-treatment"));
		// the run's spaces are still in the text, untouched
		assertTrue(block.getTextContent().contains("B  C"));
	}

	@Test
	public void visitor() throws Exception {
		checkFo(Docx4J.FLAG_NONE);
	}

	@Test
	public void xslt() throws Exception {
		checkFo(Docx4J.FLAG_EXPORT_PREFER_XSL);
	}

	// ---- what FOP then lays out

	/** The inline progression dimension of the first text area, in millipoints. */
	private int textIpd(String text) throws Exception {
		org.w3c.dom.Document at = areaTree(pkg(text), Docx4J.FLAG_NONE);
		NodeList texts = at.getElementsByTagName("text");
		assertTrue("no text area for " + text, texts.getLength() > 0);
		return Integer.parseInt(((Element) texts.item(0)).getAttribute("ipd"));
	}

	/** Every space is rendered at its width, not collapsed into one. */
	@Test
	public void aRunOfSpacesIsAsWideAsItsSpaces() throws Exception {
		int one = textIpd("B C");
		int two = textIpd("B  C");
		int space = two - one;
		assertTrue("a second space added no width: " + space, space > 0);
		assertEquals("eight spaces must be eight spaces wide",
				one + 7 * space, textIpd("B        C"));
	}

	/**
	 * Issue 369 / https://stackoverflow.com/questions/57475550 : the line a paragraph
	 * wraps onto must not begin with the space it wrapped at.
	 */
	@Test
	public void aWrappedLineStartsFlush() throws Exception {
		List<Node> starts = lineStarts(areaTree(pkg(LONG), Docx4J.FLAG_NONE));
		assertTrue("the probe paragraph did not wrap", starts.size() > 1);
		for (int i = 1; i < starts.size(); i++) {
			assertEquals("line " + (i + 1) + " starts with whitespace",
					"word", starts.get(i).getNodeName());
		}
	}

	/**
	 * And a run of spaces which straddles a line end hangs at the end of that line,
	 * as it does in Word, rather than indenting the next one.
	 */
	@Test
	public void aRunOfSpacesAtALineEndHangs() throws Exception {
		String straddling = LONG.replace(" et dolore ", "          dolore ");
		List<Node> starts = lineStarts(areaTree(pkg(straddling), Docx4J.FLAG_NONE));
		assertTrue("the probe paragraph did not wrap", starts.size() > 1);
		for (int i = 1; i < starts.size(); i++) {
			assertEquals("line " + (i + 1) + " starts with whitespace",
					"word", starts.get(i).getNodeName());
		}
	}

	/** The first &lt;word&gt; or &lt;space&gt; of each lineArea, in document order. */
	private static List<Node> lineStarts(org.w3c.dom.Document areaTree) {
		List<Node> out = new ArrayList<Node>();
		NodeList lines = areaTree.getElementsByTagName("lineArea");
		for (int i = 0; i < lines.getLength(); i++) {
			Node first = firstWordOrSpace(lines.item(i));
			if (first != null) out.add(first);
		}
		return out;
	}

	private static Node firstWordOrSpace(Node node) {
		for (Node n = node.getFirstChild(); n != null; n = n.getNextSibling()) {
			if (n.getNodeType() != Node.ELEMENT_NODE) continue;
			if ("word".equals(n.getNodeName()) || "space".equals(n.getNodeName())) return n;
			Node inner = firstWordOrSpace(n);
			if (inner != null) return inner;
		}
		return null;
	}

	/** A sanity check that the probe really is measuring something. */
	@Test
	public void theProbeParagraphWraps() throws Exception {
		assertNotNull(areaTree(pkg(LONG), Docx4J.FLAG_NONE));
		assertTrue(lineCount(areaTree(pkg(LONG), Docx4J.FLAG_NONE)) > 1);
	}
}
