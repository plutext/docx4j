package org.docx4j.fonts;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import java.util.Collections;
import java.util.Set;

import org.docx4j.XmlUtils;
import org.docx4j.jaxb.Context;
import org.docx4j.openpackaging.packages.WordprocessingMLPackage;
import org.docx4j.openpackaging.parts.WordprocessingML.FontTablePart;
import org.docx4j.openpackaging.parts.relationships.Namespaces;
import org.docx4j.wml.Document;
import org.docx4j.wml.Fonts;
import org.junit.Test;

/**
 * {@code w:altName} in {@code word/fontTable.xml} (ECMA-376 17.8.3.1): the name of the
 * font to use where the one the document asks for is not available.  That is the author's
 * own answer to a missing font, and Word takes it - measured on a document whose Normal
 * style is "HelveticaNeue LT 55 Roman" with {@code <w:altName w:val="Times New Roman"/>},
 * where Word's PDF embeds TimesNewRomanPSMT while docx4j reached the theme's minorHAnsi
 * 2194 times, making our lines 1.072 x Word's over 140 matched lines.
 *
 * <p>Consulted after the metrically compatible substitutes (a clone of the document's own
 * font is a better answer) and before the class-based ones (which are only a guess from
 * the name); CR-001 &#xa7;5.1.</p>
 *
 * @since 17.0.6
 */
public class AltNameSubstituteTest {

	private static final String W = "xmlns:w=\"http://schemas.openxmlformats.org/wordprocessingml/2006/main\"";

	/** a name no machine has a family for, so nothing but the altName can resolve it */
	private static final String MADE_UP = "Docx4j Test Face LT 55";
	/** a second such name, for the case with no altName (the metrics alias is a static map) */
	private static final String MADE_UP_2 = "Docx4j Test Face LT 56";

	private static Fonts fontTable(String fontName, String altName) throws Exception {
		return (Fonts) XmlUtils.unmarshalString(
				"<w:fonts xmlns:w=\"" + Namespaces.NS_WORD12 + "\">"
				+ "<w:font w:name=\"" + fontName + "\">"
				+ (altName == null ? "" : "<w:altName w:val=\"" + altName + "\"/>")
				+ "<w:charset w:val=\"00\"/><w:family w:val=\"swiss\"/><w:pitch w:val=\"variable\"/>"
				+ "</w:font></w:fonts>", Context.jc, Fonts.class);
	}

	/** the mapper as WordprocessingMLPackage.setFontMapper builds it, up to the altName step */
	private static Mapper mapper(String documentFont, String altName) throws Exception {
		Mapper mapper = new IdentityPlusMapper();
		Set<String> inUse = Collections.singleton(documentFont);
		Fonts fonts = fontTable(documentFont, altName);
		mapper.populateFontMappings(inUse, fonts);
		mapper.addMetricallyCompatibleSubstitutes();
		mapper.addAltNameSubstitutes(inUse, fonts);
		return mapper;
	}

	/** The made-up family resolves to whatever Arial resolves to on this machine. */
	@Test
	public void altNameIsUsed() throws Exception {

		Mapper mapper = mapper(MADE_UP, "Arial");

		PhysicalFont arial = mapper.get("Arial");
		if (arial == null) arial = PhysicalFonts.get("Arial");
		assertNotNull("no mapping for Arial itself; cannot test the alias", arial);

		PhysicalFont mapped = mapper.get(MADE_UP);
		assertNotNull("w:altName was not consulted: " + MADE_UP + " is unmapped", mapped);
		assertEquals("w:altName should resolve exactly as its own name does",
				arial.getName(), mapped.getName());
	}

	/**
	 * And Word takes the vertical metrics of the font it actually uses, so the alias is
	 * registered with WordLineMetrics too (&#xa7;2.7): line heights follow Arial's
	 * 1.150 em rather than the physical substitute's own (Arimo's is 1.432).
	 */
	@Test
	public void altNameSuppliesTheLineMetrics() throws Exception {

		mapper(MADE_UP, "Arial");

		assertTrue("the altName should give " + MADE_UP + " Arial's Word line metrics",
				WordLineMetrics.hasTableEntry(MADE_UP));

		PhysicalFont pf = PhysicalFonts.get("Arimo Regular");
		assertEquals("Arial's single-spacing factor",
				WordLineMetrics.get("Arial", pf).lineHeightFactor(),
				WordLineMetrics.get(MADE_UP, pf).lineHeightFactor(), 0.0001);
	}

	/** With no altName there is nothing to resolve, and no metrics alias either. */
	@Test
	public void noAltNameLeavesItAlone() throws Exception {

		Mapper mapper = mapper(MADE_UP_2, null);

		assertNull("nothing should have mapped " + MADE_UP_2 + " at this stage",
				mapper.get(MADE_UP_2));
		assertFalse("no w:altName, so no WordLineMetrics alias",
				WordLineMetrics.hasTableEntry(MADE_UP_2));
	}

	/** And the step is wired into the package's font mapper setup. */
	@Test
	public void thePackageConsultsIt() throws Exception {

		WordprocessingMLPackage pkg = WordprocessingMLPackage.createPackage();
		pkg.getMainDocumentPart().setJaxbElement((Document) XmlUtils.unmarshalString(
				"<w:document " + W + "><w:body><w:p><w:r>"
				+ "<w:rPr><w:rFonts w:ascii=\"" + MADE_UP + "\" w:hAnsi=\"" + MADE_UP + "\"/></w:rPr>"
				+ "<w:t>Hello</w:t></w:r></w:p></w:body></w:document>"));

		FontTablePart ftp = new FontTablePart();
		ftp.setJaxbElement(fontTable(MADE_UP, "Arial"));
		pkg.getMainDocumentPart().addTargetPart(ftp);

		pkg.setFontMapper(new IdentityPlusMapper());

		PhysicalFont arial = pkg.getFontMapper().get("Arial");
		if (arial == null) arial = PhysicalFonts.get("Arial");
		assertNotNull("no mapping for Arial itself; cannot test the alias", arial);
		assertNotNull("the package's font mapper did not consult w:altName",
				pkg.getFontMapper().get(MADE_UP));
		assertEquals(arial.getName(), pkg.getFontMapper().get(MADE_UP).getName());
	}
}
