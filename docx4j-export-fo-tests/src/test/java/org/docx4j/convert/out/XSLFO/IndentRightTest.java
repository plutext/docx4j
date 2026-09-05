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
 * w:ind/@w:right (and @w:end) was never emitted, so a paragraph indented from
 * both sides was laid out at the full column width and did not break where Word
 * breaks it (CR-001, real documents).  It becomes end-indent in FO.  Both
 * pathways; the property class is shared with HTML, where it is margin-right.
 */
public class IndentRightTest extends AbstractXSLFOTest {

	private static final String W = "xmlns:w=\"http://schemas.openxmlformats.org/wordprocessingml/2006/main\"";

	/** A4 portrait with 1in margins: a 451.3pt text column. */
	private static final String SECT_PR = "<w:sectPr><w:pgSz w:w=\"11906\" w:h=\"16838\"/>"
			+ "<w:pgMar w:top=\"1440\" w:right=\"1440\" w:bottom=\"1440\" w:left=\"1440\"/></w:sectPr>";

	private static final String TEXT =
			"Pachtvertrag zur Pacht einer Reitanlage mit Stallungen und Weiden auf unbestimmte Zeit";

	private static WordprocessingMLPackage pkg(String ind) throws Exception {
		WordprocessingMLPackage pkg = WordprocessingMLPackage.createPackage();
		pkg.getMainDocumentPart().setJaxbElement((Document)XmlUtils.unmarshalString(
				"<w:document " + W + "><w:body>"
				+ "<w:p><w:pPr>" + ind + "</w:pPr><w:r><w:t>" + TEXT + "</w:t></w:r></w:p>"
				+ SECT_PR + "</w:body></w:document>"));
		return pkg;
	}

	private static org.w3c.dom.Document fo(String ind, int flags) throws Exception {
		FOSettings foSettings = Docx4J.createFOSettings();
		foSettings.setWmlPackage(pkg(ind));
		foSettings.setApacheFopMime(FOSettings.INTERNAL_FO_MIME);
		ByteArrayOutputStream baos = new ByteArrayOutputStream();
		Docx4J.toFO(foSettings, baos, flags);
		return XmlUtils.getNewDocumentBuilder().parse(new java.io.ByteArrayInputStream(baos.toByteArray()));
	}

	private static Element paragraphBlock(org.w3c.dom.Document doc) {
		NodeList nl = doc.getElementsByTagNameNS("http://www.w3.org/1999/XSL/Format", "block");
		for (int i = 0; i < nl.getLength(); i++) {
			Element el = (Element) nl.item(i);
			if (el.getTextContent().startsWith("Pachtvertrag")) return el;
		}
		return null;
	}

	private void check(int flags) throws Exception {
		Element indented = paragraphBlock(fo("<w:ind w:left=\"2704\" w:right=\"2700\"/>", flags));
		assertNotNull(indented);
		assertEquals("135.2pt", indented.getAttribute("start-indent"));
		assertEquals("135pt", indented.getAttribute("end-indent"));

		// w:end is the strict-conformance spelling of w:right
		assertEquals("135pt",
				paragraphBlock(fo("<w:ind w:left=\"2704\" w:end=\"2700\"/>", flags)).getAttribute("end-indent"));

		// unchanged where there is no right indent
		Element plain = paragraphBlock(fo("<w:ind w:left=\"2704\"/>", flags));
		assertEquals("135.2pt", plain.getAttribute("start-indent"));
		assertEquals("", plain.getAttribute("end-indent"));
	}

	@Test
	public void visitor() throws Exception {
		check(Docx4J.FLAG_NONE);
	}

	@Test
	public void xslt() throws Exception {
		check(Docx4J.FLAG_EXPORT_PREFER_XSL);
	}

	/**
	 * And it takes effect: FOP wraps the indented paragraph into more lines than the
	 * same text at the full column width.
	 */
	@Test
	public void wrapsAtTheNarrowerWidth() throws Exception {
		int wide = lines(pkg("<w:ind w:left=\"2704\"/>"));
		int narrow = lines(pkg("<w:ind w:left=\"2704\" w:right=\"2700\"/>"));
		assertTrue("end-indent did not narrow the line: " + wide + " then " + narrow, narrow > wide);
	}

	private int lines(WordprocessingMLPackage pkg) throws Exception {
		return lineCount(areaTree(pkg, Docx4J.FLAG_NONE));
	}
}
