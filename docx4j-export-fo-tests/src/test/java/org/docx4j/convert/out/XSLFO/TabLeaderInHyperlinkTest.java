package org.docx4j.convert.out.XSLFO;

import static org.junit.Assert.assertTrue;

import java.io.ByteArrayOutputStream;

import org.docx4j.Docx4J;
import org.docx4j.XmlUtils;
import org.docx4j.convert.out.FOSettings;
import org.docx4j.openpackaging.packages.WordprocessingMLPackage;
import org.docx4j.wml.Document;
import org.junit.Test;

/** A w:tab inside a hyperlink (a TOC entry) keeps its paragraph's dot leader (Getting Started guide). */
public class TabLeaderInHyperlinkTest {

	private static final String W = "xmlns:w=\"http://schemas.openxmlformats.org/wordprocessingml/2006/main\"";

	private static String fo(int flags) throws Exception {
		WordprocessingMLPackage pkg = WordprocessingMLPackage.createPackage();
		pkg.getMainDocumentPart().setJaxbElement((Document)XmlUtils.unmarshalString(
				"<w:document " + W + "><w:body><w:p>"
				+ "<w:pPr><w:tabs><w:tab w:val=\"right\" w:leader=\"dot\" w:pos=\"9350\"/></w:tabs></w:pPr>"
				+ "<w:hyperlink w:anchor=\"_Toc1\"><w:r><w:t>What is docx4j?</w:t></w:r><w:r><w:tab/></w:r><w:r><w:t>4</w:t></w:r></w:hyperlink>"
				+ "</w:p></w:body></w:document>"));
		FOSettings foSettings = Docx4J.createFOSettings();
		foSettings.setWmlPackage(pkg);
		foSettings.setApacheFopMime(FOSettings.INTERNAL_FO_MIME);
		ByteArrayOutputStream baos = new ByteArrayOutputStream();
		Docx4J.toFO(foSettings, baos, flags);
		return baos.toString("UTF-8");
	}

	@Test
	public void visitorPathway() throws Exception {
		assertTrue(fo(Docx4J.FLAG_NONE).contains("leader-pattern=\"dots\""));
	}

	@Test
	public void xsltPathway() throws Exception {
		assertTrue(fo(Docx4J.FLAG_EXPORT_PREFER_XSL).contains("leader-pattern=\"dots\""));
	}
}
