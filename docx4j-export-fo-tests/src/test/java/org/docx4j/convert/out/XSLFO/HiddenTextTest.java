package org.docx4j.convert.out.XSLFO;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.io.ByteArrayOutputStream;

import org.docx4j.Docx4J;
import org.docx4j.Docx4jProperties;
import org.docx4j.XmlUtils;
import org.docx4j.convert.out.FOSettings;
import org.docx4j.convert.out.common.HiddenText;
import org.docx4j.openpackaging.packages.WordprocessingMLPackage;
import org.docx4j.wml.Document;
import org.junit.After;
import org.junit.Test;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

/**
 * w:vanish (hidden text) was rendered, which pushed everything after it down the
 * page - a web form artefact at the top of a real document moved every line on
 * page 1 down 14pt (CR-001).  Word prints nothing for it, and a paragraph whose
 * runs and paragraph mark are all hidden leaves no line at all.  Both FO
 * pathways; set docx4j.convert.out.printHiddenText to render it anyway.
 */
public class HiddenTextTest extends AbstractXSLFOTest {

	private static final String W = "xmlns:w=\"http://schemas.openxmlformats.org/wordprocessingml/2006/main\"";
	private static final String XSL_FO = "http://www.w3.org/1999/XSL/Format";

	private static final String BODY =
			  "<w:p><w:r><w:rPr><w:vanish/></w:rPr><w:t>Haut du formulaire</w:t></w:r>"
			+     "<w:r><w:t>visible tail</w:t></w:r></w:p>"
			// a wholly hidden paragraph: hidden run, hidden paragraph mark
			+ "<w:p><w:pPr><w:rPr><w:vanish/></w:rPr></w:pPr>"
			+     "<w:r><w:rPr><w:vanish/></w:rPr><w:t>Bas du formulaire</w:t></w:r></w:p>"
			// all runs hidden but the mark is not: Word still leaves the empty line
			+ "<w:p><w:r><w:rPr><w:vanish/></w:rPr><w:t>hidden only</w:t></w:r></w:p>"
			+ "<w:p><w:r><w:t>Title</w:t></w:r></w:p>";

	@After
	public void reset() {
		Docx4jProperties.setProperty(HiddenText.PROPERTY_NAME, false);
	}

	private static org.w3c.dom.Document fo(int flags) throws Exception {
		WordprocessingMLPackage pkg = WordprocessingMLPackage.createPackage();
		pkg.getMainDocumentPart().setJaxbElement((Document)XmlUtils.unmarshalString(
				"<w:document " + W + "><w:body>" + BODY + "</w:body></w:document>"));
		FOSettings foSettings = Docx4J.createFOSettings();
		foSettings.setWmlPackage(pkg);
		foSettings.setApacheFopMime(FOSettings.INTERNAL_FO_MIME);
		ByteArrayOutputStream baos = new ByteArrayOutputStream();
		Docx4J.toFO(foSettings, baos, flags);
		return XmlUtils.getNewDocumentBuilder().parse(new java.io.ByteArrayInputStream(baos.toByteArray()));
	}

	/** the fo:blocks which are paragraphs of the flow */
	private static int paragraphCount(org.w3c.dom.Document doc) {
		NodeList nl = doc.getElementsByTagNameNS(XSL_FO, "flow");
		int n = 0;
		for (int i = 0; i < nl.getLength(); i++) {
			NodeList children = nl.item(i).getChildNodes();
			for (int j = 0; j < children.getLength(); j++) {
				if (children.item(j) instanceof Element
						&& "block".equals(((Element) children.item(j)).getLocalName())) n++;
			}
		}
		return n;
	}

	private void check(int flags) throws Exception {
		org.w3c.dom.Document doc = fo(flags);
		String text = doc.getDocumentElement().getTextContent();
		assertFalse("hidden run rendered: " + text, text.contains("Haut du formulaire"));
		assertFalse("wholly hidden paragraph rendered", text.contains("Bas du formulaire"));
		assertFalse(text.contains("hidden only"));
		assertTrue("visible text lost", text.contains("visible tail"));
		assertTrue(text.contains("Title"));

		// four paragraphs, less the wholly hidden one; the one whose mark is visible stays
		assertEquals(3, paragraphCount(doc));
	}

	private void checkPrinted(int flags) throws Exception {
		Docx4jProperties.setProperty(HiddenText.PROPERTY_NAME, true);
		String text = fo(flags).getDocumentElement().getTextContent();
		assertTrue("printHiddenText did not restore it", text.contains("Haut du formulaire"));
		assertTrue(text.contains("Bas du formulaire"));
	}

	@Test
	public void visitor() throws Exception {
		check(Docx4J.FLAG_NONE);
	}

	@Test
	public void xslt() throws Exception {
		check(Docx4J.FLAG_EXPORT_PREFER_XSL);
	}

	@Test
	public void printedVisitor() throws Exception {
		checkPrinted(Docx4J.FLAG_NONE);
	}

	@Test
	public void printedXslt() throws Exception {
		checkPrinted(Docx4J.FLAG_EXPORT_PREFER_XSL);
	}
}
