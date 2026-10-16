package org.docx4j.fonts;

import static org.junit.Assert.assertTrue;

import java.io.ByteArrayOutputStream;

import org.docx4j.Docx4J;
import org.docx4j.XmlUtils;
import org.docx4j.convert.out.HTMLSettings;
import org.docx4j.openpackaging.packages.WordprocessingMLPackage;
import org.docx4j.wml.Document;
import org.junit.Assume;
import org.junit.Test;

/**
 * RunFontSelector asks whether the run's font has a glyph for the character.  The name
 * in w:rFonts is the name the *document* uses, so the question has to be put to the font
 * it is mapped to.  Until 17.0.3 it was put to PhysicalFonts.get(thatName), which finds
 * nothing where the document font is mapped to a substitute with a different name - and
 * nothing at all for a font embedded in the document, since those are deliberately kept
 * out of PhysicalFonts.  The answer was then "no glyph", and the character was rendered
 * in a substitute font it didn't need.
 *
 * @since 17.0.3
 */
public class RunFontSelectorMappedFontGlyphTest {

	/** U+2751; in the range where RunFontSelector goes looking for a substitute font
	 *  (as Word 2016 does), unless the run's own font has the character. */
	private static final String CHAR = "❑";

	private static final String DOC_FONT = "Some Font Not On This System";

	@Test
	public void testGlyphCheckUsesTheMappedFont() throws Exception {

		WordprocessingMLPackage wordMLPackage = WordprocessingMLPackage.createPackage();

		// NB the physical fonts are discovered in the Mapper's static initialiser,
		// so ask for the mapper before looking in PhysicalFonts
		Mapper fontMapper = wordMLPackage.getFontMapper();

		PhysicalFont dejaVu = PhysicalFonts.get("DejaVu Sans");
		Assume.assumeTrue("DejaVu Sans isn't installed", dejaVu != null);
		Assume.assumeTrue("DejaVu Sans has no " + CHAR, GlyphCheck.hasChar(dejaVu, CHAR.charAt(0)));
		Assume.assumeTrue("unexpectedly installed", PhysicalFonts.get(DOC_FONT) == null);

		wordMLPackage.getMainDocumentPart().setJaxbElement((Document)XmlUtils.unmarshalString(
				"<w:document xmlns:w=\"http://schemas.openxmlformats.org/wordprocessingml/2006/main\">"
				+ "<w:body><w:p><w:r><w:rPr>"
				+ "<w:rFonts w:ascii=\"" + DOC_FONT + "\" w:hAnsi=\"" + DOC_FONT + "\"/>"
				+ "</w:rPr><w:t>" + CHAR + "</w:t></w:r></w:p></w:body></w:document>"));

		/* The document's font isn't installed under that name - PhysicalFonts.get returns
		 * null for it, exactly as it does for a font embedded in the document - but it is
		 * mapped to one which has the character. */
		fontMapper.put(DOC_FONT, dejaVu);

		HTMLSettings htmlSettings = Docx4J.createHTMLSettings();
		htmlSettings.setOpcPackage(wordMLPackage);
		ByteArrayOutputStream baos = new ByteArrayOutputStream();
		Docx4J.toHTML(htmlSettings, baos, Docx4J.FLAG_EXPORT_PREFER_XSL);
		String html = baos.toString("UTF-8");

		assertTrue("the run's own font has the glyph, so it should have been used;"
				+ " instead the output was: " + html,
				java.util.regex.Pattern.compile("font-family:\\s*'"
						+ java.util.regex.Pattern.quote(dejaVu.getName()) + "'").matcher(html).find());
	}
}
