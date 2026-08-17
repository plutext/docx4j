package org.docx4j.convert.out.XSLFO;

import static org.junit.Assert.assertTrue;

import java.io.ByteArrayOutputStream;

import org.docx4j.Docx4J;
import org.docx4j.XmlUtils;
import org.docx4j.convert.out.FOSettings;
import org.docx4j.jaxb.Context;
import org.docx4j.openpackaging.packages.WordprocessingMLPackage;
import org.docx4j.openpackaging.parts.PartName;
import org.docx4j.openpackaging.parts.WordprocessingML.FooterPart;
import org.docx4j.openpackaging.parts.relationships.Namespaces;
import org.docx4j.relationships.Relationship;
import org.docx4j.wml.FooterReference;
import org.docx4j.wml.Ftr;
import org.docx4j.wml.HdrFtrRef;
import org.docx4j.wml.ObjectFactory;
import org.docx4j.wml.SectPr;
import org.docx4j.wml.Style;
import org.junit.Test;

/**
 * The runs of a PAGE or NUMPAGES field in a footer specify a font (w:rFonts).
 * Since we generate fo:page-number/fo:page-number-citation-last for those
 * fields (there is no w:t to hang the font off), the font has to be set
 * explicitly; otherwise the page numbers are rendered in FOP's default font,
 * whilst the surrounding text is in the specified font.
 *
 * @since 17.0.3
 */
public class FieldFontTest extends AbstractXSLFOTest {

	private static final String FONT = "Courier New";

	@Test
	public void testPageFieldFont() throws Exception {

		assertFieldFontMatchesText(createPkg(true));
	}

	/** Word doesn't stamp the style's font on each run; it is inherited.  So the
	 *  usual case is that the field's runs have no w:rFonts at all, and the font has
	 *  to be resolved from the paragraph style. */
	@Test
	public void testPageFieldFontFromParagraphStyle() throws Exception {

		assertFieldFontMatchesText(createPkg(false));
	}

	private void assertFieldFontMatchesText(WordprocessingMLPackage wordMLPackage) throws Exception {

		byte[] fo = toFO(wordMLPackage);

		org.w3c.dom.Document doc = w3cDomDocumentFromByteArray(fo);

		// the surrounding text has a font ..
		assertTrue("no font-family on the surrounding text",
				isPresent(doc, "//fo:inline[starts-with(text(),'Page')][@font-family]"));

		// .. and so should the PAGE field
		assertTrue("no font-family on fo:page-number",
				isAbsent(doc, "//fo:page-number[not(@font-family)]"));
		assertTrue("fo:page-number font-family doesn't match the surrounding text",
				isPresent(doc, "//fo:page-number[@font-family = //fo:inline[starts-with(text(),'Page')]/@font-family]"));

		// NUMPAGES: the fo:page-number-citation-last is wrapped in an fo:wrapper,
		// which is what carries the inherited properties
		assertTrue("no font-family on the fo:wrapper of fo:page-number-citation-last",
				isAbsent(doc, "//fo:wrapper[fo:page-number-citation-last][not(@font-family)]"));
		assertTrue("fo:page-number-citation-last font-family doesn't match the surrounding text",
				isPresent(doc, "//fo:wrapper[fo:page-number-citation-last]"
						+ "[@font-family = //fo:inline[starts-with(text(),'Page')]/@font-family]"));
	}

	private byte[] toFO(WordprocessingMLPackage wordMLPackage) throws Exception {

		FOSettings foSettings = Docx4J.createFOSettings();
		foSettings.setWmlPackage(wordMLPackage);
		foSettings.setApacheFopMime(FOSettings.INTERNAL_FO_MIME);

		ByteArrayOutputStream baos = new ByteArrayOutputStream();
		Docx4J.toFO(foSettings, baos, Docx4J.FLAG_EXPORT_PREFER_XSL);
		return baos.toByteArray();
	}

	/** "Page { PAGE } of { NUMPAGES }" in a footer, in FONT.
	 *
	 * @param direct true for w:rFonts on each run; false for a paragraph style
	 *        which specifies the font, with no direct formatting at all
	 */
	private WordprocessingMLPackage createPkg(boolean direct) throws Exception {

		WordprocessingMLPackage wordMLPackage = WordprocessingMLPackage.createPackage();
		ObjectFactory factory = Context.getWmlObjectFactory();

		wordMLPackage.getMainDocumentPart().addParagraphOfText("Hello world");

		if (!direct) {
			wordMLPackage.getMainDocumentPart().getStyleDefinitionsPart().getJaxbElement().getStyle().add(
					(Style)XmlUtils.unmarshalString(
						"<w:style xmlns:w=\"" + Namespaces.NS_WORD12 + "\" w:type=\"paragraph\" w:styleId=\"MyFooter\">"
						+ "<w:name w:val=\"My Footer\"/>"
						+ "<w:rPr><w:rFonts w:ascii=\"" + FONT + "\" w:hAnsi=\"" + FONT + "\"/></w:rPr>"
						+ "</w:style>", Context.jc, Style.class));
		}

		String ftrXml = "<w:ftr xmlns:w=\"" + Namespaces.NS_WORD12 + "\">"
				+ "<w:p>"
				+ (direct ? "" : "<w:pPr><w:pStyle w:val=\"MyFooter\"/></w:pPr>")
				+ r(direct, "<w:t xml:space=\"preserve\">Page </w:t>")
				+ complexField(direct, "PAGE", "1")
				+ r(direct, "<w:t xml:space=\"preserve\"> of </w:t>")
				+ complexField(direct, "NUMPAGES", "1")
				+ "</w:p></w:ftr>";

		FooterPart footerPart = new FooterPart(new PartName("/word/footer1.xml"));
		footerPart.setPackage(wordMLPackage);
		footerPart.setJaxbElement((Ftr)XmlUtils.unmarshalString(ftrXml, Context.jc, Ftr.class));
		Relationship rel = wordMLPackage.getMainDocumentPart().addTargetPart(footerPart);

		SectPr sectPr = wordMLPackage.getMainDocumentPart().getJaxbElement().getBody().getSectPr();
		if (sectPr == null) {
			sectPr = factory.createSectPr();
			wordMLPackage.getMainDocumentPart().getJaxbElement().getBody().setSectPr(sectPr);
		}
		FooterReference footerReference = factory.createFooterReference();
		footerReference.setId(rel.getId());
		footerReference.setType(HdrFtrRef.DEFAULT);
		sectPr.getEGHdrFtrReferences().add(footerReference);

		return wordMLPackage;
	}

	private static String r(boolean direct, String inner) {
		return "<w:r>"
				+ (direct ? "<w:rPr><w:rFonts w:ascii=\"" + FONT + "\" w:hAnsi=\"" + FONT + "\"/></w:rPr>" : "")
				+ inner + "</w:r>";
	}

	private static String complexField(boolean direct, String instr, String cachedResult) {
		return r(direct, "<w:fldChar w:fldCharType=\"begin\"/>")
			 + r(direct, "<w:instrText xml:space=\"preserve\"> " + instr + " </w:instrText>")
			 + r(direct, "<w:fldChar w:fldCharType=\"separate\"/>")
			 + r(direct, "<w:t>" + cachedResult + "</w:t>")
			 + r(direct, "<w:fldChar w:fldCharType=\"end\"/>");
	}
}
