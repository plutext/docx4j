package org.docx4j.fonts.fop.util;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotEquals;
import static org.junit.Assert.assertSame;
import static org.junit.Assert.assertTrue;
import static org.junit.Assume.assumeNotNull;
import static org.junit.Assume.assumeTrue;

import org.docx4j.fonts.IdentityPlusMapper;
import org.docx4j.fonts.Mapper;
import org.docx4j.fonts.PhysicalFont;
import org.docx4j.fonts.PhysicalFonts;
import org.docx4j.fonts.TextMeasurer;
import org.junit.Test;

/**
 * {@link FopConfigUtil#renderedFace} answers with the face FOP will draw, so that text
 * measured before FOP sees the document - the table autofit pass - is measured in the
 * face it is set in.
 *
 * <p>The autofit sizer used to consult only {@code font-family}, which docx4j writes as
 * the regular face's name for all four faces of a family, so every bold cell was measured
 * in the regular face: an 8pt heading measured 79.6pt in DejaVu Sans where FOP draws it at
 * 89.1 in DejaVu Sans Bold, and the columns of a table with bold headings came out some
 * 11-13% narrow.</p>
 *
 * @since 17.1.1
 */
public class RenderedFaceTest {

	private static PhysicalFont installed(String name) throws Exception {
		PhysicalFonts.discoverPhysicalFonts();
		return PhysicalFonts.get(name);
	}

	private static String file(PhysicalFont pf) {
		return pf.getEmbeddedURI().toString().toLowerCase();
	}

	@Test
	public void regularIsItself() throws Exception {
		PhysicalFont regular = installed("Liberation Sans");
		assumeNotNull("Liberation Sans is not installed", regular);
		Mapper mapper = new IdentityPlusMapper();
		assertSame(regular, FopConfigUtil.renderedFace(mapper, regular, false, false));
		assertSame(regular, FopConfigUtil.renderedFace(null, regular, false, false));
		assertEquals(null, FopConfigUtil.renderedFace(mapper, null, true, true));
	}

	@Test
	public void liberationSansFourFaces() throws Exception {
		PhysicalFont regular = installed("Liberation Sans");
		assumeNotNull("Liberation Sans is not installed", regular);
		Mapper mapper = new IdentityPlusMapper();

		PhysicalFont bold = FopConfigUtil.renderedFace(mapper, regular, true, false);
		PhysicalFont italic = FopConfigUtil.renderedFace(mapper, regular, false, true);
		PhysicalFont boldItalic = FopConfigUtil.renderedFace(mapper, regular, true, true);

		assertNotEquals("bold resolved to the regular file", file(regular), file(bold));
		assertNotEquals("italic resolved to the regular file", file(regular), file(italic));
		assertNotEquals("bold italic resolved to the regular file", file(regular), file(boldItalic));
		assertNotEquals("bold italic resolved to the bold file", file(bold), file(boldItalic));
		assertNotEquals("bold italic resolved to the italic file", file(italic), file(boldItalic));
		assertTrue(file(bold), file(bold).contains("bold"));
		assertTrue(file(italic), file(italic).contains("italic"));
	}

	/** The measurement that found the defect: a bold heading is a tenth wider than the
	 *  regular face says. */
	@Test
	public void boldMeasuresWiderThanRegular() throws Exception {
		PhysicalFont regular = installed("DejaVu Sans");
		if (regular == null) regular = installed("Liberation Sans");
		assumeNotNull("neither DejaVu Sans nor Liberation Sans is installed", regular);
		PhysicalFont bold = FopConfigUtil.renderedFace(new IdentityPlusMapper(), regular, true, false);
		assumeTrue("no bold face for " + regular.getName(), bold != regular);

		double wRegular = TextMeasurer.widthPt("Demora Proyectada", regular, 8);
		double wBold = TextMeasurer.widthPt("Demora Proyectada", bold, 8);
		assertTrue(wRegular + " regular, " + wBold + " bold", wBold > wRegular * 1.05);
		assertTrue(wRegular + " regular, " + wBold + " bold", wBold < wRegular * 1.25);
	}

	/** A family which has no such face is drawn, and so measured, in the regular file. */
	@Test
	public void familyWithoutTheFaceFallsBackToRegular() throws Exception {
		PhysicalFonts.discoverPhysicalFonts();
		PhysicalFont symbol = PhysicalFonts.getSymbolFont();
		assumeNotNull("no Symbol substitute", symbol);
		Mapper mapper = new IdentityPlusMapper();
		assumeTrue("this Symbol substitute has a bold face", mapper.getBoldForm(symbol.getName(), symbol) == null);
		assertSame(symbol, FopConfigUtil.renderedFace(mapper, symbol, true, false));
		assertSame(symbol, FopConfigUtil.renderedFace(mapper, symbol, true, true));
	}
}
