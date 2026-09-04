package org.docx4j.convert.out.XSLFO;

import static org.junit.Assert.assertEquals;

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
 * w:keepLines (Word's "keep lines together", on every built-in heading style)
 * becomes keep-together.within-page="always", so a heading broken over two lines
 * by a w:br is not split across a page (CR-001 §6.10: the Getting Started guide's
 * "Selecting your insertion/editing point; / accessing JAXB nodes via XPath").
 * Both FO pathways.
 */
public class KeepLinesTest {

	private static final String W = "xmlns:w=\"http://schemas.openxmlformats.org/wordprocessingml/2006/main\"";

	private static org.w3c.dom.Document fo(String pPr, int flags) throws Exception {
		WordprocessingMLPackage pkg = WordprocessingMLPackage.createPackage();
		pkg.getMainDocumentPart().setJaxbElement((Document)XmlUtils.unmarshalString(
				"<w:document " + W + "><w:body><w:p><w:pPr>" + pPr + "</w:pPr>"
				+ "<w:r><w:t xml:space=\"preserve\">first line; </w:t><w:br/><w:t>second line</w:t></w:r></w:p>"
				+ "<w:p><w:r><w:t>after</w:t></w:r></w:p></w:body></w:document>"));
		FOSettings foSettings = Docx4J.createFOSettings();
		foSettings.setWmlPackage(pkg);
		foSettings.setApacheFopMime(FOSettings.INTERNAL_FO_MIME);
		ByteArrayOutputStream baos = new ByteArrayOutputStream();
		Docx4J.toFO(foSettings, baos, flags);
		return XmlUtils.getNewDocumentBuilder().parse(new java.io.ByteArrayInputStream(baos.toByteArray()));
	}

	/** the paragraph block holding the text */
	private static Element block(org.w3c.dom.Document doc, String text) {
		NodeList nl = doc.getElementsByTagNameNS("http://www.w3.org/1999/XSL/Format", "block");
		Element found = null;
		for (int i = 0; i < nl.getLength(); i++) {
			Element el = (Element) nl.item(i);
			if (el.getTextContent().contains(text)) found = found == null ? el : found; // outermost
		}
		return found;
	}

	private void check(int flags) throws Exception {
		assertEquals("always", block(fo("<w:keepLines/>", flags), "first line").getAttribute("keep-together.within-page"));
		assertEquals("", block(fo("", flags), "first line").getAttribute("keep-together.within-page"));
		assertEquals("", block(fo("<w:keepLines w:val=\"false\"/>", flags), "first line").getAttribute("keep-together.within-page"));
		assertEquals("keepNext alone is keep-with-next", "always", block(fo("<w:keepNext/>", flags), "first line").getAttribute("keep-with-next"));
	}

	@Test
	public void visitor() throws Exception {
		check(Docx4J.FLAG_NONE);
	}

	@Test
	public void xslt() throws Exception {
		check(Docx4J.FLAG_EXPORT_PREFER_XSL);
	}
}
