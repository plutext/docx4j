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
import org.w3c.dom.NodeList;

/**
 * w:sectPr/w:vAlign - Word's Page Setup "Vertical alignment" - was ignored, so a title
 * page whose section asks for "center" was laid out at the top of the page.  Measured
 * against Word's own PDF of a 179 page specification whose title section carries
 * &lt;w:vAlign w:val="center"/&gt;: every line of page 1 was 112.5pt above Word's (Word
 * put its first line at y=275.9, docx4j at 163.4).
 *
 * <p>XSL 1.1's display-align on fo:region-body is the equivalent, and it costs nothing
 * on a full page, so it applies to the whole section as Word applies it.</p>
 *
 * @since 17.0.6
 */
public class SectionVerticalAlignTest extends AbstractXSLFOTest {

	private static final String W = "xmlns:w=\"http://schemas.openxmlformats.org/wordprocessingml/2006/main\"";
	private static final String FO = "http://www.w3.org/1999/XSL/Format";

	private static String body(String vAlign) {
		return "<w:document " + W + "><w:body>"
				+ "<w:p><w:r><w:t>Title</w:t></w:r></w:p>"
				+ "<w:sectPr>"
				+ (vAlign == null ? "" : "<w:vAlign w:val=\"" + vAlign + "\"/>")
				+ "<w:pgSz w:w=\"11906\" w:h=\"16838\"/>"
				+ "<w:pgMar w:top=\"1440\" w:right=\"1440\" w:bottom=\"1440\" w:left=\"1440\"/>"
				+ "</w:sectPr></w:body></w:document>";
	}

	private org.w3c.dom.Document fo(String vAlign, int flags) throws Exception {
		WordprocessingMLPackage pkg = WordprocessingMLPackage.createPackage();
		pkg.getMainDocumentPart().setJaxbElement((Document)XmlUtils.unmarshalString(body(vAlign)));
		FOSettings foSettings = Docx4J.createFOSettings();
		foSettings.setOpcPackage(pkg);
		foSettings.setApacheFopMime(FOSettings.INTERNAL_FO_MIME);
		ByteArrayOutputStream baos = new ByteArrayOutputStream();
		Docx4J.toFO(foSettings, baos, flags);
		return w3cDomDocumentFromByteArray(baos.toByteArray());
	}

	/** the display-align of every fo:region-body, joined */
	private String displayAlign(org.w3c.dom.Document doc) {
		NodeList bodies = doc.getElementsByTagNameNS(FO, "region-body");
		assertTrue("no region-body", bodies.getLength() > 0);
		StringBuilder sb = new StringBuilder();
		for (int i = 0; i < bodies.getLength(); i++) {
			sb.append(((Element)bodies.item(i)).getAttribute("display-align")).append('|');
		}
		return sb.toString();
	}

	private void check(int flags) throws Exception {
		assertEquals("|", displayAlign(fo(null, flags)));
		assertEquals("center|", displayAlign(fo("center", flags)));
		assertEquals("after|", displayAlign(fo("bottom", flags)));
		assertEquals("XSL-FO has no justified equivalent; centre is the closest",
				"center|", displayAlign(fo("both", flags)));
		assertEquals("top is the default", "|", displayAlign(fo("top", flags)));
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
