package org.docx4j.convert.out.html;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.io.ByteArrayOutputStream;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.docx4j.Docx4J;
import org.docx4j.XmlUtils;
import org.docx4j.convert.out.HTMLSettings;
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
 * The span we generate for such a field has no w:t, so unless the font is set
 * on it explicitly, it is rendered in a different font to the surrounding text.
 *
 * @since 17.0.3
 */
public class FieldFontTest {

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

		String html = toHTML(wordMLPackage);

		// the surrounding text has a font ..
		String textFont = fontFamilyOfSpanContaining(html, "Page ");
		assertNotNull("no span for the surrounding text", textFont);
		assertTrue("no font-family on the surrounding text", textFont.length() > 0);

		// .. and so should the field results (in HTML, both PAGE and NUMPAGES are "1")
		int fieldSpans = 0;
		Matcher m = spanPattern("1").matcher(html);
		while (m.find()) {
			fieldSpans++;
			assertEquals("field result span has a different font to the surrounding text",
					textFont, fontFamily(m.group(1)));
		}
		assertEquals("expected a span for each of PAGE and NUMPAGES", 2, fieldSpans);
	}

	private static Pattern spanPattern(String content) {
		return Pattern.compile("<span[^>]*style=\"([^\"]*)\"[^>]*>" + Pattern.quote(content) + "</span>");
	}

	private static String fontFamilyOfSpanContaining(String html, String content) {
		Matcher m = spanPattern(content).matcher(html);
		return (m.find() ? fontFamily(m.group(1)) : null);
	}

	/** the font-family declaration in a style attribute, or "" if there is none */
	private static String fontFamily(String style) {
		Matcher m = Pattern.compile("font-family:\\s*([^;]*)").matcher(style);
		return (m.find() ? m.group(1).trim() : "");
	}

	private String toHTML(WordprocessingMLPackage wordMLPackage) throws Exception {

		HTMLSettings htmlSettings = Docx4J.createHTMLSettings();
		htmlSettings.setOpcPackage(wordMLPackage);

		ByteArrayOutputStream baos = new ByteArrayOutputStream();
		Docx4J.toHTML(htmlSettings, baos, Docx4J.FLAG_EXPORT_PREFER_XSL);
		return baos.toString("UTF-8");
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
