/*
 *  Copyright 2026, Plutext Pty Ltd.
 *
 *  This file is part of docx4j.

    docx4j is licensed under the Apache License, Version 2.0 (the "License");
    you may not use this file except in compliance with the License.
    You may obtain a copy of the License at

        http://www.apache.org/licenses/LICENSE-2.0

    Unless required by applicable law or agreed to in writing, software
    distributed under the License is distributed on an "AS IS" BASIS,
    WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
    See the License for the specific language governing permissions and
    limitations under the License.

 */
package org.docx4j.metafiles;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.awt.Font;

import org.docx4j.fonts.Mapper;
import org.docx4j.fonts.PhysicalFont;
import org.docx4j.fonts.PhysicalFonts;
import org.docx4j.org.apache.poi.common.usermodel.fonts.FontInfo;
import org.docx4j.org.apache.poi.sl.draw.Docx4jDrawFontManager;
import org.junit.Assume;
import org.junit.Before;
import org.junit.Test;

/**
 * A metafile's GDI face name resolves through the document's font mapper
 * (CR-001, non-embedded fonts; CR-011's pathway).
 *
 * <p>A metafile names its fonts the way the producing application did - "Calibri",
 * "Times New Roman" - and those are <em>document</em> font names. {@code PhysicalFonts} is
 * keyed by the names of the fonts actually installed, so it answers null for every one of
 * them; only the mapper knows that Calibri is drawn in Carlito. Without it the name
 * reached AWT unchanged, AWT did not know it, and the family degraded to {@code Dialog} -
 * which Batik wrote into the SVG as the {@code font-family} and FOP could not resolve, so
 * the picture's text was drawn in a base-14 font the PDF does not embed.</p>
 *
 * @since 17.2.0
 */
public class MetafileFontResolutionTest {

	/** a document font no machine has installed under that name */
	private static final String DOC_FONT = "Calibri";

	private Mapper mapper;

	@Before
	public void setUp() throws Exception {
		/* The mapper of a document which names the font: Mapper.get reads the mappings
		 * the package populated for its own fonts, so a bare mapper knows nothing - and
		 * a metafile's face is exactly a document font name. */
		org.docx4j.openpackaging.packages.WordprocessingMLPackage pkg =
				org.docx4j.openpackaging.packages.WordprocessingMLPackage.createPackage();
		pkg.getMainDocumentPart().setJaxbElement((org.docx4j.wml.Document)
				org.docx4j.XmlUtils.unmarshalString(
				"<w:document xmlns:w=\"http://schemas.openxmlformats.org/wordprocessingml/2006/main\">"
				+ "<w:body><w:p><w:r><w:rPr><w:rFonts w:ascii=\"" + DOC_FONT
				+ "\" w:hAnsi=\"" + DOC_FONT + "\"/></w:rPr><w:t>text</w:t></w:r></w:p>"
				+ "</w:body></w:document>"));
		mapper = pkg.getFontMapper();

		Assume.assumeTrue(DOC_FONT + " is unexpectedly installed under its own name",
				PhysicalFonts.get(DOC_FONT) == null);
		Assume.assumeTrue("the mapper has no substitute for " + DOC_FONT,
				mapper.get(DOC_FONT) != null);
	}

	/** POI's DrawFontManager reads rendering hints off the context, so it needs one. */
	private static java.awt.Graphics2D graphics() {
		return new java.awt.image.BufferedImage(1, 1,
				java.awt.image.BufferedImage.TYPE_INT_ARGB).createGraphics();
	}

	private static FontInfo face(String typeface) {
		return new FontInfo() {
			public String getTypeface() { return typeface; }
			public void setTypeface(String t) { }
			public org.docx4j.org.apache.poi.common.usermodel.fonts.FontCharset getCharset() {
				return org.docx4j.org.apache.poi.common.usermodel.fonts.FontCharset.ANSI;
			}
			public void setCharset(org.docx4j.org.apache.poi.common.usermodel.fonts.FontCharset c) { }
			public org.docx4j.org.apache.poi.common.usermodel.fonts.FontFamily getFamily() { return null; }
			public void setFamily(org.docx4j.org.apache.poi.common.usermodel.fonts.FontFamily f) { }
			public org.docx4j.org.apache.poi.common.usermodel.fonts.FontPitch getPitch() { return null; }
			public void setPitch(org.docx4j.org.apache.poi.common.usermodel.fonts.FontPitch p) { }
			public byte[] getPanose() { return null; }
			public void setPanose(byte[] p) { }
			public Integer getIndex() { return null; }
			public void setIndex(Integer i) { }
		};
	}

	/** Without a mapper, nothing resolves the document font name: the old behaviour. */
	@Test
	public void withoutAMapperTheFaceIsLeftAlone() {
		Docx4jDrawFontManager manager = new Docx4jDrawFontManager();
		FontInfo mapped = manager.getMappedFont(graphics(), face(DOC_FONT));
		assertEquals(DOC_FONT, mapped.getTypeface());
		assertEquals("AWT cannot resolve it, so this is the family Batik would write",
				Font.DIALOG, new Font(mapped.getTypeface(), Font.PLAIN, 12).getFamily());
	}

	/** With one, the face becomes a family AWT can resolve - and not Dialog. */
	@Test
	public void theFaceResolvesThroughTheMapper() {
		Docx4jDrawFontManager manager = new Docx4jDrawFontManager(mapper);
		FontInfo mapped = manager.getMappedFont(graphics(), face(DOC_FONT));

		assertNotNull(mapped);
		assertFalse("the metafile's own face name was handed on unchanged",
				DOC_FONT.equals(mapped.getTypeface()));
		assertFalse("the family Batik writes must not be AWT's fallback",
				Font.DIALOG.equals(new Font(mapped.getTypeface(), Font.PLAIN, 12).getFamily()));
	}

	/** And the AWT font the pathway draws with is that face, not Dialog. */
	@Test
	public void theAwtFontIsTheSubstituteFace() {
		Docx4jDrawFontManager manager = new Docx4jDrawFontManager(mapper);
		Font font = manager.createAWTFont(graphics(), face(DOC_FONT), 12, false, false);
		assertNotNull(font);
		assertFalse("drawn in AWT's fallback: " + font, Font.DIALOG.equals(font.getFamily()));

		PhysicalFont substitute = mapper.get(DOC_FONT);
		assertNotNull(substitute);
		assertEquals("the substitute's own family", substitute.getFamilyName(), font.getFamily());
	}

	/** Bold asks the mapper for the bold face, where it has one. */
	@Test
	public void boldTakesTheBoldFace() {
		PhysicalFont regular = mapper.get(DOC_FONT);
		PhysicalFont bold = mapper.getBoldForm(DOC_FONT, regular);
		Assume.assumeTrue("no separate bold face for " + DOC_FONT,
				bold != null && !bold.getName().equals(regular.getName()));

		Docx4jDrawFontManager manager = new Docx4jDrawFontManager(mapper);
		Font font = manager.createAWTFont(graphics(), face(DOC_FONT), 12, true, false);
		assertNotNull(font);
		assertTrue("the bold face draws it: " + font,
				font.isBold() || bold.getFamilyName().equals(font.getFamily()));
	}
}
