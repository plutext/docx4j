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

/**
 * Where the last paragraph of the body carries its own w:sectPr, the section the
 * document-level w:sectPr describes has no content: Word renders nothing for it, but
 * docx4j wrote a page-sequence with an empty fo:flow, which FOP rejects ("fo:flow" is
 * missing child elements), aborting the export.
 *
 * @since 17.0.5
 */
public class EmptyFinalSectionTest extends AbstractXSLFOTest {

	private static final String W = "xmlns:w=\"http://schemas.openxmlformats.org/wordprocessingml/2006/main\"";
	private static final String FO = "http://www.w3.org/1999/XSL/Format";

	private static final String SECT_PR =
			"<w:sectPr><w:pgSz w:w=\"11906\" w:h=\"16838\"/>"
			+ "<w:pgMar w:top=\"1440\" w:right=\"1440\" w:bottom=\"1440\" w:left=\"1440\"/></w:sectPr>";

	private static WordprocessingMLPackage pkg() throws Exception {
		WordprocessingMLPackage pkg = WordprocessingMLPackage.createPackage();
		pkg.getMainDocumentPart().setJaxbElement((Document)XmlUtils.unmarshalString(
				"<w:document " + W + "><w:body>"
				+ "<w:p><w:r><w:t>first</w:t></w:r></w:p>"
				+ "<w:p><w:pPr>" + SECT_PR + "</w:pPr><w:r><w:t>last, and it ends the section</w:t></w:r></w:p>"
				+ SECT_PR
				+ "</w:body></w:document>"));
		return pkg;
	}

	private void check(int flags) throws Exception {

		FOSettings foSettings = Docx4J.createFOSettings();
		foSettings.setWmlPackage(pkg());
		foSettings.setApacheFopMime(FOSettings.INTERNAL_FO_MIME);
		ByteArrayOutputStream baos = new ByteArrayOutputStream();
		Docx4J.toFO(foSettings, baos, flags);

		org.w3c.dom.Document doc = w3cDomDocumentFromByteArray(baos.toByteArray());
		assertEquals("no page-sequence for the section with no content",
				1, doc.getElementsByTagNameNS(FO, "page-sequence").getLength());
		assertTrue(new String(baos.toByteArray(), "UTF-8").contains("ends the section"));

		// FOP must accept it
		foSettings = Docx4J.createFOSettings();
		foSettings.setOpcPackage(pkg()); // setOpcPackage, so FOP gets a font configuration
		Docx4J.toFO(foSettings, new ByteArrayOutputStream(), flags);
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
