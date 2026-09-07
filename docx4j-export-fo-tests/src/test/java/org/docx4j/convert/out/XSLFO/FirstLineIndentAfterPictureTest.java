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
 * A paragraph which begins with an anchored object keeps its first-line indent (CR-001).
 *
 * <p>The wrapper such an object is put in is block-level, so FOP puts the inline content
 * after it into an anonymous block, which starts at the block's start-indent whatever
 * {@code text-indent} says.  Measured on a corpus document whose "Dear Sir," paragraph is
 * {@code <w:ind w:left="60" w:firstLine="360"/>} and begins with a {@code w:pict}: Word
 * draws the text at x=21.1 (3pt + 18pt) and docx4j drew it at x=3.0.  The indent is now an
 * {@code fo:leader} of its width at the head of the inline content.</p>
 *
 * @since 17.1.0
 */
public class FirstLineIndentAfterPictureTest extends AbstractXSLFOTest {

	private static final String W = "xmlns:w=\"http://schemas.openxmlformats.org/wordprocessingml/2006/main\"";
	private static final String V = "xmlns:v=\"urn:schemas-microsoft-com:vml\"";
	private static final String FO = "http://www.w3.org/1999/XSL/Format";

	private static final String SECT_PR = "<w:sectPr><w:pgSz w:w=\"11906\" w:h=\"16838\"/>"
			+ "<w:pgMar w:top=\"1440\" w:right=\"1440\" w:bottom=\"1440\" w:left=\"1440\"/></w:sectPr>";

	/** an absolutely positioned VML text box, which is put in a wrapper of its own */
	private static final String PICT = "<w:r><w:pict " + V + ">"
			+ "<v:shape style=\"position:absolute;margin-left:10pt;margin-top:5pt;width:40pt;height:20pt\">"
			+ "<v:textbox><w:txbxContent><w:p><w:r><w:t>box</w:t></w:r></w:p></w:txbxContent></v:textbox>"
			+ "</v:shape></w:pict></w:r>";

	private static org.w3c.dom.Document fo(String body, int flags) throws Exception {
		WordprocessingMLPackage pkg = WordprocessingMLPackage.createPackage();
		pkg.getMainDocumentPart().setJaxbElement((Document)XmlUtils.unmarshalString(
				"<w:document " + W + "><w:body>" + body + SECT_PR + "</w:body></w:document>"));
		FOSettings foSettings = Docx4J.createFOSettings();
		foSettings.setWmlPackage(pkg);
		foSettings.setApacheFopMime(FOSettings.INTERNAL_FO_MIME);
		ByteArrayOutputStream baos = new ByteArrayOutputStream();
		Docx4J.toFO(foSettings, baos, flags);
		return XmlUtils.getNewDocumentBuilder().parse(new ByteArrayInputStream(baos.toByteArray()));
	}

	private static Element paragraph(org.w3c.dom.Document doc) {
		NodeList flows = doc.getElementsByTagNameNS(FO, "flow");
		for (Node n = flows.item(0).getFirstChild(); n != null; n = n.getNextSibling()) {
			if (n instanceof Element && "block".equals(((Element)n).getLocalName())) return (Element)n;
		}
		throw new AssertionError("no paragraph block");
	}

	@Test
	public void indentReservedByALeader() throws Exception {
		String body = "<w:p><w:pPr><w:ind w:left=\"60\" w:firstLine=\"360\"/></w:pPr>"
				+ PICT + "<w:r><w:t>Dear Sir,</w:t></w:r></w:p>";
		for (int flags : new int[] { Docx4J.FLAG_NONE, Docx4J.FLAG_EXPORT_PREFER_XSL }) {
			Element block = paragraph(fo(body, flags));
			assertEquals("3pt", block.getAttribute("start-indent"));
			// the property is taken off, so the following lines are not indented too
			assertEquals("", block.getAttribute("text-indent"));
			// a leader of its width stands at the head of the inline content, after the
			// object's own (block-level) wrapper
			Element leader = null;
			for (Node n = block.getFirstChild(); n != null; n = n.getNextSibling()) {
				if (n instanceof Element && "leader".equals(((Element)n).getLocalName())) {
					leader = (Element)n;
					break;
				}
			}
			assertTrue("no leader", leader != null);
			assertEquals("18pt", leader.getAttribute("leader-length"));
		}
	}

	/** A paragraph with no leading block-level child is untouched. */
	@Test
	public void plainParagraphKeepsItsTextIndent() throws Exception {
		String body = "<w:p><w:pPr><w:ind w:left=\"60\" w:firstLine=\"360\"/></w:pPr>"
				+ "<w:r><w:t>Dear Sir,</w:t></w:r></w:p>";
		for (int flags : new int[] { Docx4J.FLAG_NONE, Docx4J.FLAG_EXPORT_PREFER_XSL }) {
			Element block = paragraph(fo(body, flags));
			assertEquals("18pt", block.getAttribute("text-indent"));
			assertEquals(0, block.getElementsByTagNameNS(FO, "leader").getLength());
		}
	}

}
