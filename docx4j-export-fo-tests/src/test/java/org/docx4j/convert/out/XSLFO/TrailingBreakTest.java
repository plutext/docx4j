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
 * Word gives a line break at the end of a paragraph an empty line of its own
 * (measured, CR-001 §6.10); the FO carries a no-break space in the run's font
 * after the break so FOP makes that line.  Both FO pathways.
 */
public class TrailingBreakTest {

	private static final String W = "xmlns:w=\"http://schemas.openxmlformats.org/wordprocessingml/2006/main\"";
	private static final String FONT = "<w:rPr><w:rFonts w:ascii=\"Liberation Serif\" w:hAnsi=\"Liberation Serif\"/></w:rPr>";

	private static org.w3c.dom.Document fo(String runContent, int flags) throws Exception {
		WordprocessingMLPackage pkg = WordprocessingMLPackage.createPackage();
		pkg.getMainDocumentPart().setJaxbElement((Document)XmlUtils.unmarshalString(
				"<w:document " + W + "><w:body><w:p><w:r>" + FONT + runContent + "</w:r></w:p></w:body></w:document>"));
		FOSettings foSettings = Docx4J.createFOSettings();
		foSettings.setWmlPackage(pkg);
		foSettings.setApacheFopMime(FOSettings.INTERNAL_FO_MIME);
		ByteArrayOutputStream baos = new ByteArrayOutputStream();
		Docx4J.toFO(foSettings, baos, flags);
		return XmlUtils.getNewDocumentBuilder().parse(new java.io.ByteArrayInputStream(baos.toByteArray()));
	}

	/** inlines holding just a no-break space */
	private static int emptyLines(org.w3c.dom.Document doc) {
		int n = 0;
		NodeList nl = doc.getElementsByTagNameNS("http://www.w3.org/1999/XSL/Format", "inline");
		for (int i = 0; i < nl.getLength(); i++) {
			Element el = (Element) nl.item(i);
			if (" ".equals(el.getTextContent()) && el.getAttribute("font-family").length() > 0) n++;
		}
		return n;
	}

	private void check(int flags) throws Exception {
		assertEquals("a trailing break makes an empty line", 1, emptyLines(fo("<w:t>abc</w:t><w:br/>", flags)));
		assertEquals("text follows the break", 0, emptyLines(fo("<w:t>abc</w:t><w:br/><w:t>def</w:t>", flags)));
		assertEquals("a page break is not a line", 0, emptyLines(fo("<w:t>abc</w:t><w:br w:type=\"page\"/>", flags)));
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
