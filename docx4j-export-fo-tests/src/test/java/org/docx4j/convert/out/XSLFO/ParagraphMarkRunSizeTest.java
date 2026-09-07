package org.docx4j.convert.out.XSLFO;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

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
 * A {@code w:r} with no {@code w:rPr} takes its size from the style chain, not from
 * whatever else the paragraph holds.
 *
 * <p>Such a run gets no {@code font-size} of its own - the XSLT pathway does not wrap it
 * in an {@code fo:inline} at all - so it inherits the block's.  The block starts with the
 * paragraph's effective size and then takes the size of the run owning most of its text
 * ({@code applyBlockLineHeight}, which is how a line gets Word's pitch), and until 17.1.0
 * the sizeless runs changed size with it.  Measured on a corpus header of
 * {@code [image][45 spaces][24pt text]} whose paragraph mark carries {@code w:sz="48"}:
 * the spaces were 45 x 5.42 = 244.1pt where Word's are 45 x 2.5 = 111.6, which wrapped
 * the heading and made 8 Word pages 10 of ours.
 *
 * <p>Both FO pathways.
 *
 * @since 17.1.0
 */
public class ParagraphMarkRunSizeTest {

	private static final String W = "xmlns:w=\"http://schemas.openxmlformats.org/wordprocessingml/2006/main\"";
	private static final String FO = "http://www.w3.org/1999/XSL/Format";

	private static final int[] FLAGS = { Docx4J.FLAG_NONE, Docx4J.FLAG_EXPORT_PREFER_XSL };

	private static org.w3c.dom.Document fo(String body, int flags) throws Exception {
		WordprocessingMLPackage pkg = WordprocessingMLPackage.createPackage();
		pkg.getMainDocumentPart().setJaxbElement((Document) XmlUtils.unmarshalString(
				"<w:document " + W + "><w:body>" + body + "</w:body></w:document>"));
		FOSettings foSettings = Docx4J.createFOSettings();
		foSettings.setWmlPackage(pkg);
		foSettings.setApacheFopMime(FOSettings.INTERNAL_FO_MIME);
		ByteArrayOutputStream baos = new ByteArrayOutputStream();
		Docx4J.toFO(foSettings, baos, flags);
		return XmlUtils.getNewDocumentBuilder().parse(new java.io.ByteArrayInputStream(baos.toByteArray()));
	}

	/** The block of the first paragraph that has text. */
	private static Element block(org.w3c.dom.Document doc, String containing) {
		NodeList nl = doc.getElementsByTagNameNS(FO, "block");
		for (int i = 0; i < nl.getLength(); i++) {
			Element b = (Element) nl.item(i);
			if (b.getTextContent()!=null && b.getTextContent().contains(containing)
					&& b.getElementsByTagNameNS(FO, "block").getLength()==0) {
				return b;
			}
		}
		return null;
	}

	/** The size in force where this text is painted: the nearest font-size at or above it. */
	private static String effectiveSize(Element block, String text) {
		Node n = find(block, text);
		assertTrue("no such text: " + text, n!=null);
		while (n!=null) {
			if (n instanceof Element) {
				String v = ((Element) n).getAttribute("font-size");
				if (v!=null && v.length()>0) return v;
			}
			n = n.getParentNode();
		}
		return "";
	}

	private static Node find(Node from, String text) {
		if (from.getNodeType()==Node.TEXT_NODE) {
			return from.getNodeValue()!=null && from.getNodeValue().contains(text) ? from : null;
		}
		NodeList nl = from.getChildNodes();
		for (int i = 0; i < nl.getLength(); i++) {
			Node hit = find(nl.item(i), text);
			if (hit!=null) return hit;
		}
		return null;
	}

	/**
	 * The paragraph mark is 24pt and the only run with a {@code w:rPr} is too, so the
	 * block takes 24pt - but the run with no {@code w:rPr} keeps the default 11pt.
	 */
	@Test
	public void aRunWithNoRPrKeepsTheStyleChainSize() throws Exception {
		String body = "<w:p><w:pPr><w:rPr><w:sz w:val=\"48\"/></w:rPr></w:pPr>"
				+ "<w:r><w:t xml:space=\"preserve\">plainplainplain</w:t></w:r>"
				+ "<w:r><w:rPr><w:sz w:val=\"48\"/></w:rPr><w:t>BIGBIGBIGBIGBIGBIGBIG</w:t></w:r></w:p>";
		for (int flag : FLAGS) {
			org.w3c.dom.Document doc = fo(body, flag);
			Element b = block(doc, "BIG");
			assertTrue("no block", b!=null);
			// the dominant run sizes the block's lines, as Word sizes them
			assertEquals("block", "24.0pt", b.getAttribute("font-size"));
			// but the sizeless run is not resized with it
			assertEquals("sizeless run", "11.0pt", effectiveSize(b, "plain"));
			assertEquals("sized run", "24.0pt", effectiveSize(b, "BIG"));
		}
	}

	/**
	 * An {@code fo:inline} which paints nothing - a bookmark anchor - gets no size of its
	 * own: FOP builds an empty inline area of whatever size it is given and takes the
	 * line's height from it, and three corpus documents fell from 1.000 when their
	 * anchors were pinned.
	 */
	@Test
	public void anEmptyAnchorIsNotPinned() throws Exception {
		String body = "<w:p><w:pPr><w:rPr><w:sz w:val=\"48\"/></w:rPr></w:pPr>"
				+ "<w:bookmarkStart w:id=\"1\" w:name=\"anchor\"/><w:bookmarkEnd w:id=\"1\"/>"
				+ "<w:r><w:t xml:space=\"preserve\">plainplainplain</w:t></w:r>"
				+ "<w:r><w:rPr><w:sz w:val=\"48\"/></w:rPr><w:t>BIGBIGBIGBIGBIGBIGBIG</w:t></w:r></w:p>";
		for (int flag : FLAGS) {
			org.w3c.dom.Document doc = fo(body, flag);
			NodeList nl = doc.getElementsByTagNameNS(FO, "inline");
			boolean sawAnchor = false;
			for (int i = 0; i < nl.getLength(); i++) {
				Element el = (Element) nl.item(i);
				if (el.getTextContent()!=null && el.getTextContent().length()>0) continue;
				sawAnchor = true;
				assertEquals("an empty inline was given a size", "", el.getAttribute("font-size"));
			}
			assertTrue("no empty inline in the FO", sawAnchor);
		}
	}

	/** Where every run is the same size there is nothing to pin, and no font-size is
	 *  added to the runs: the FO is unchanged from 17.0.5. */
	@Test
	public void aUniformParagraphIsUnchanged() throws Exception {
		String body = "<w:p><w:r><w:t>one</w:t></w:r><w:r><w:t xml:space=\"preserve\"> two</w:t></w:r></w:p>";
		for (int flag : FLAGS) {
			org.w3c.dom.Document doc = fo(body, flag);
			Element b = block(doc, "one");
			assertTrue("no block", b!=null);
			assertEquals("11.0pt", b.getAttribute("font-size"));
			assertEquals("11.0pt", effectiveSize(b, "two"));
			NodeList nl = b.getElementsByTagNameNS(FO, "inline");
			for (int i = 0; i < nl.getLength(); i++) {
				String v = ((Element) nl.item(i)).getAttribute("font-size");
				assertTrue("an unnecessary font-size was added: " + v,
						v==null || v.length()==0 || "11.0pt".equals(v));
			}
		}
	}
}
