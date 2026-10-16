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
import org.docx4j.fonts.PhysicalFont;
import org.docx4j.fonts.PhysicalFonts;
import org.docx4j.fonts.GlyphCheck;
import org.docx4j.fonts.IdentityPlusMapper;
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
import org.junit.Assume;
import org.junit.BeforeClass;

/**
 * The runs of a PAGE or NUMPAGES field in a footer specify a font (w:rFonts).
 * The span we generate for such a field has no w:t, so unless the font is set
 * on it explicitly, it is rendered in a different font to the surrounding text.
 *
 * @since 17.0.3
 */
public class FieldFontTest {

	/** The font the test document asks for.  Chosen from what is actually installed:
	 *  these tests are about the field's font matching the surrounding text's, and a
	 *  font which resolves to nothing gets no font-family at all in html output (see
	 *  RunFontSelector.getCssProperty), which would fail them for the wrong reason. */
	private static String FONT;

	@BeforeClass
	public static void chooseAFontWhichExists() throws Exception {

		new IdentityPlusMapper();  // triggers discovery of the physical fonts

		PhysicalFont pf = firstUsable("Courier New", "DejaVu Sans Mono", "Liberation Mono",
				"DejaVu Sans", "Liberation Sans", "Arimo Regular", "Noto Sans Regular");
		if (pf==null) {
			for (PhysicalFont candidate : PhysicalFonts.getPhysicalFonts().values()) {
				if (canRenderTheTestText(candidate)) { pf = candidate; break; }
			}
		}
		Assume.assumeTrue("no usable physical font on this machine", pf!=null);
		FONT = pf.getName();
	}

	private static PhysicalFont firstUsable(String... names) throws Exception {
		for (String name : names) {
			PhysicalFont pf = PhysicalFonts.get(name);
			if (canRenderTheTestText(pf)) return pf;
		}
		return null;
	}

	/** It must have the characters this test uses, or RunFontSelector may not choose it. */
	private static boolean canRenderTheTestText(PhysicalFont pf) throws Exception {
		if (pf==null || pf.getName()==null) return false;
		for (char c : "Page of 1".toCharArray()) {
			if (!GlyphCheck.hasCodepoint(pf, c)) return false;
		}
		return true;
	}

	/** Where the font comes from in the test package. */
	private enum FontSource { DIRECT_RFONTS, PARAGRAPH_STYLE, CHARACTER_STYLE }

	@Test
	public void testPageFieldFont() throws Exception {

		assertFieldFontMatchesText(createPkg(FontSource.DIRECT_RFONTS));
	}

	/** Word doesn't stamp the style's font on each run; it is inherited.  So the
	 *  usual case is that the field's runs have no w:rFonts at all, and the font has
	 *  to be resolved from the paragraph style. */
	@Test
	public void testPageFieldFontFromParagraphStyle() throws Exception {

		assertFieldFontMatchesText(createPkg(FontSource.PARAGRAPH_STYLE));
	}

	/** Word's footer galleries (in some locales) apply a character style to the
	 *  field's runs ("page number"), rather than direct w:rFonts.  The font then
	 *  has to be resolved from the character style. */
	@Test
	public void testPageFieldFontFromCharacterStyle() throws Exception {

		assertFieldFontMatchesText(createPkg(FontSource.CHARACTER_STYLE));
	}

	private void assertFieldFontMatchesText(WordprocessingMLPackage wordMLPackage) throws Exception {

		String html = toHTML(wordMLPackage);

		// the surrounding text has a font ..
		String textFont = fontFamilyOfSpanContaining(html, "Page ");
		assertNotNull("no span for the surrounding text", textFont);
		assertTrue("no font-family on the surrounding text", textFont.length() > 0);

		// .. namely FONT's physical font - not the document default (were the style
		// being ignored, text and field would still match, on the default font)
		PhysicalFont expected = wordMLPackage.getFontMapper().get(FONT);
		assertNotNull("no physical font for " + FONT + " on this machine", expected);
		assertEquals("surrounding text isn't in " + FONT, "'" + expected.getName() + "'", textFont);

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
	 * @param fontSource where the font comes from: w:rFonts on each run; a paragraph
	 *        style (with no direct formatting at all); or a character style on each
	 *        run (as Word's footer galleries produce in some locales)
	 */
	private WordprocessingMLPackage createPkg(FontSource fontSource) throws Exception {

		WordprocessingMLPackage wordMLPackage = WordprocessingMLPackage.createPackage();
		ObjectFactory factory = Context.getWmlObjectFactory();

		/* Add the styles first: PropertyResolver snapshots the styles part when it is
		 * created (initialiseLiveStyles), and addParagraphOfText creates it - so a style
		 * added afterwards is invisible, and the run would silently fall back to the
		 * document default instead of testing what it says it tests. */
		if (fontSource == FontSource.PARAGRAPH_STYLE) {
			addStyle(wordMLPackage,
					"<w:style xmlns:w=\"" + Namespaces.NS_WORD12 + "\" w:type=\"paragraph\" w:styleId=\"MyFooter\">"
					+ "<w:name w:val=\"My Footer\"/>"
					+ "<w:rPr><w:rFonts w:ascii=\"" + FONT + "\" w:hAnsi=\"" + FONT + "\"/></w:rPr>"
					+ "</w:style>");
		} else if (fontSource == FontSource.CHARACTER_STYLE) {
			addStyle(wordMLPackage,
					"<w:style xmlns:w=\"" + Namespaces.NS_WORD12 + "\" w:type=\"character\" w:styleId=\"PageNumber\">"
					+ "<w:name w:val=\"page number\"/>"
					+ "<w:rPr><w:rFonts w:ascii=\"" + FONT + "\" w:hAnsi=\"" + FONT + "\"/></w:rPr>"
					+ "</w:style>");
		}

		wordMLPackage.getMainDocumentPart().addParagraphOfText("Hello world");

		String ftrXml = "<w:ftr xmlns:w=\"" + Namespaces.NS_WORD12 + "\">"
				+ "<w:p>"
				+ (fontSource == FontSource.PARAGRAPH_STYLE ? "<w:pPr><w:pStyle w:val=\"MyFooter\"/></w:pPr>" : "")
				+ r(fontSource, "<w:t xml:space=\"preserve\">Page </w:t>")
				+ complexField(fontSource, "PAGE", "1")
				+ r(fontSource, "<w:t xml:space=\"preserve\"> of </w:t>")
				+ complexField(fontSource, "NUMPAGES", "1")
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

	private static void addStyle(WordprocessingMLPackage wordMLPackage, String styleXml) throws Exception {
		wordMLPackage.getMainDocumentPart().getStyleDefinitionsPart().getJaxbElement().getStyle().add(
				(Style)XmlUtils.unmarshalString(styleXml, Context.jc, Style.class));
	}

	private static String r(FontSource fontSource, String inner) {
		String rPr = "";
		if (fontSource == FontSource.DIRECT_RFONTS) {
			rPr = "<w:rPr><w:rFonts w:ascii=\"" + FONT + "\" w:hAnsi=\"" + FONT + "\"/></w:rPr>";
		} else if (fontSource == FontSource.CHARACTER_STYLE) {
			rPr = "<w:rPr><w:rStyle w:val=\"PageNumber\"/></w:rPr>";
		}
		return "<w:r>" + rPr + inner + "</w:r>";
	}

	private static String complexField(FontSource fontSource, String instr, String cachedResult) {
		return r(fontSource, "<w:fldChar w:fldCharType=\"begin\"/>")
			 + r(fontSource, "<w:instrText xml:space=\"preserve\"> " + instr + " </w:instrText>")
			 + r(fontSource, "<w:fldChar w:fldCharType=\"separate\"/>")
			 + r(fontSource, "<w:t>" + cachedResult + "</w:t>")
			 + r(fontSource, "<w:fldChar w:fldCharType=\"end\"/>");
	}
}
