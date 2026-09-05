package org.docx4j.convert.out.XSLFO;

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.io.ByteArrayOutputStream;

import org.docx4j.Docx4J;
import org.docx4j.XmlUtils;
import org.docx4j.convert.out.FOSettings;
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
import org.docx4j.wml.CTPageNumber;
import org.docx4j.wml.FooterReference;
import org.docx4j.wml.NumberFormat;
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
 * Since we generate fo:page-number/fo:page-number-citation-last for those
 * fields (there is no w:t to hang the font off), the font has to be set
 * explicitly; otherwise the page numbers are rendered in FOP's default font,
 * whilst the surrounding text is in the specified font.
 *
 * @since 17.0.3
 */
public class FieldFontTest extends AbstractXSLFOTest {

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

	/** w:pgNumType w:fmt="thaiNumbers": the fo:page-sequence should ask for Thai
	 *  digits, rather than falling back to Latin ones.
	 *
	 *  The font is resolved for the digit which will actually be rendered, and FONT
	 *  has no Thai digits.  Giving FOP a font which can't render the character
	 *  produces .notdef, not a fallback, so the fo:page-number must end up either with
	 *  no font at all (XsltCommonFunctions.fontCanRender drops it) or, since 17.0.5,
	 *  with the Thai-capable font the glyph-aware fallback found for it.  The
	 *  surrounding text, which FONT can render, keeps it. */
	@Test
	public void testThaiPageNumberFormat() throws Exception {

		// the point of the test is that FONT itself can't render the Thai digit
		Assume.assumeFalse(FONT + " has Thai digits, so nothing would have to be done",
				GlyphCheck.hasCodepoint(PhysicalFonts.get(FONT), '\u0E51'));

		org.w3c.dom.Document doc = w3cDomDocumentFromByteArray(toFO(createThaiPkg()));

		assertTrue("fo:page-sequence doesn't ask for Thai digits",
				isPresent(doc, "//fo:page-sequence[@format='\u0E51']"));  // ๑, Thai digit one
		assertTrue("fo:page-number kept a font which has no Thai digits",
				isAbsent(doc, "//fo:page-number[@font-family='" + FONT + "']"));
		for (org.w3c.dom.Element pageNumber : pageNumbersWithAFont(doc)) {
			String family = pageNumber.getAttribute("font-family");
			PhysicalFont pf = PhysicalFonts.get(family);
			assertNotNull("fo:page-number names " + family + ", which is not installed", pf);
			assertTrue("fo:page-number was given " + family + ", which has no Thai digits",
					GlyphCheck.hasCodepoint(pf, '\u0E51'));
		}
		assertTrue("the surrounding text lost its font",
				isPresent(doc, "//fo:inline[starts-with(text(),'Page')][@font-family]"));
	}

	/** the fo:page-number elements which carry a font-family */
	private java.util.List<org.w3c.dom.Element> pageNumbersWithAFont(org.w3c.dom.Document doc) {
		java.util.List<org.w3c.dom.Element> result = new java.util.ArrayList<org.w3c.dom.Element>();
		org.w3c.dom.NodeList nl = doc.getElementsByTagNameNS(
				"http://www.w3.org/1999/XSL/Format", "page-number");
		for (int i=0; i<nl.getLength(); i++) {
			org.w3c.dom.Element el = (org.w3c.dom.Element)nl.item(i);
			if (el.getAttribute("font-family").length()>0) result.add(el);
		}
		return result;
	}

	/** .. and FOP accepts the resulting format token (the render completes). */
	@Test
	public void testThaiPageNumberFormatRenders() throws Exception {

		FOSettings foSettings = Docx4J.createFOSettings();
		// setOpcPackage, not setWmlPackage: rendering needs the fopConfig it creates
		foSettings.setOpcPackage(createThaiPkg());

		ByteArrayOutputStream baos = new ByteArrayOutputStream();
		Docx4J.toFO(foSettings, baos, Docx4J.FLAG_EXPORT_PREFER_XSL);
		assertTrue("no PDF produced", baos.size() > 0);
	}

	private WordprocessingMLPackage createThaiPkg() throws Exception {

		WordprocessingMLPackage wordMLPackage = createPkg(FontSource.DIRECT_RFONTS);
		CTPageNumber pageNumber = Context.getWmlObjectFactory().createCTPageNumber();
		pageNumber.setFmt(NumberFormat.THAI_NUMBERS);
		wordMLPackage.getMainDocumentPart().getJaxbElement().getBody().getSectPr().setPgNumType(pageNumber);
		return wordMLPackage;
	}

	private void assertFieldFontMatchesText(WordprocessingMLPackage wordMLPackage) throws Exception {

		byte[] fo = toFO(wordMLPackage);

		org.w3c.dom.Document doc = w3cDomDocumentFromByteArray(fo);

		/* What the fonts resolve to depends on what is installed, so say so when this
		 * fails: the assertions below are about the relationship between the field's
		 * font and the surrounding text's, not about any particular font. */
		String diagnostics = "\n  " + FONT + " maps to " + wordMLPackage.getFontMapper().get(FONT)
				+ "\n  FO was:\n" + new String(fo, "UTF-8");

		// the surrounding text has a font ..
		assertTrue("no font-family on the surrounding text" + diagnostics,
				isPresent(doc, "//fo:inline[starts-with(text(),'Page')][@font-family]"));

		// .. namely FONT's physical font - not the document default (were the style
		// being ignored, text and field would still match, on the default font)
		PhysicalFont expected = wordMLPackage.getFontMapper().get(FONT);
		assertNotNull("no physical font for " + FONT + " on this machine", expected);
		// starts-with, since the family name may carry a variant suffix (the kerned or
		// the no-ligature twin FopConfigUtil declares); it is the same physical font
		assertTrue("surrounding text isn't in " + FONT + diagnostics,
				isPresent(doc, "//fo:inline[starts-with(text(),'Page')]"
						+ "[starts-with(@font-family,'" + expected.getName() + "')]"));

		// .. and so should the PAGE field
		assertTrue("no font-family on fo:page-number" + diagnostics,
				isAbsent(doc, "//fo:page-number[not(@font-family)]"));
		assertTrue("fo:page-number font-family doesn't match the surrounding text" + diagnostics,
				isPresent(doc, "//fo:page-number[@font-family = //fo:inline[starts-with(text(),'Page')]/@font-family]"));

		// NUMPAGES: the fo:page-number-citation-last is wrapped in an fo:wrapper,
		// which is what carries the inherited properties
		assertTrue("no font-family on the fo:wrapper of fo:page-number-citation-last" + diagnostics,
				isAbsent(doc, "//fo:wrapper[fo:page-number-citation-last][not(@font-family)]"));
		assertTrue("fo:page-number-citation-last font-family doesn't match the surrounding text" + diagnostics,
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
