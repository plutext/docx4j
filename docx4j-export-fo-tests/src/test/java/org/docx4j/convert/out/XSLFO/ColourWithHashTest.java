package org.docx4j.convert.out.XSLFO;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.io.ByteArrayOutputStream;
import java.nio.charset.StandardCharsets;

import org.docx4j.Docx4J;
import org.docx4j.XmlUtils;
import org.docx4j.convert.out.FOSettings;
import org.docx4j.openpackaging.packages.WordprocessingMLPackage;
import org.docx4j.wml.Document;
import org.junit.Test;

/**
 * Word tolerates a colour written with the "#" already in it (w:color w:val="#0000FF").
 * docx4j used to add another, and FOP threw parsing "##0000FF", aborting the export.
 *
 * @since 17.0.5
 */
public class ColourWithHashTest {

	private static final String W = "xmlns:w=\"http://schemas.openxmlformats.org/wordprocessingml/2006/main\"";

	private static String fo(int flags) throws Exception {
		WordprocessingMLPackage pkg = WordprocessingMLPackage.createPackage();
		pkg.getMainDocumentPart().setJaxbElement((Document)XmlUtils.unmarshalString(
				"<w:document " + W + "><w:body>"
				+ "<w:p><w:r><w:rPr><w:color w:val=\"#0000FF\"/>"
				+ "  <w:shd w:val=\"clear\" w:fill=\"#FFFF00\"/></w:rPr><w:t>hashed</w:t></w:r></w:p>"
				+ "<w:p><w:r><w:rPr><w:color w:val=\"008000\"/></w:rPr><w:t>plain</w:t></w:r></w:p>"
				+ "</w:body></w:document>"));
		FOSettings foSettings = Docx4J.createFOSettings();
		foSettings.setWmlPackage(pkg);
		foSettings.setApacheFopMime(FOSettings.INTERNAL_FO_MIME);
		ByteArrayOutputStream baos = new ByteArrayOutputStream();
		Docx4J.toFO(foSettings, baos, flags);
		return new String(baos.toByteArray(), StandardCharsets.UTF_8);
	}

	private void check(int flags) throws Exception {
		String fo = fo(flags);
		assertFalse("a colour must not end up with two #:\n" + fo, fo.contains("##"));
		assertTrue(fo.contains("#0000FF"));
		assertTrue(fo.contains("#FFFF00"));
		assertTrue("a colour without the # still gets one", fo.contains("#008000"));

		// and FOP accepts it
		FOSettings foSettings = Docx4J.createFOSettings();
		WordprocessingMLPackage pkg = WordprocessingMLPackage.createPackage();
		pkg.getMainDocumentPart().setJaxbElement((Document)XmlUtils.unmarshalString(
				"<w:document " + W + "><w:body><w:p><w:r><w:rPr><w:color w:val=\"#0000FF\"/></w:rPr>"
				+ "<w:t>hashed</w:t></w:r></w:p></w:body></w:document>"));
		foSettings.setOpcPackage(pkg); // setOpcPackage, so FOP gets a font configuration
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
