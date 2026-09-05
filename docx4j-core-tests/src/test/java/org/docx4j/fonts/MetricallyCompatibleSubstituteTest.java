package org.docx4j.fonts;

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import org.docx4j.openpackaging.packages.WordprocessingMLPackage;
import org.junit.Test;

/**
 * {@link Mapper#addMetricallyCompatibleSubstitutes()} covered only a handful of
 * fonts, so anything else the document named fell back to the document's default
 * font - a sans in a Times clone, or the other way about (CR-001: the first
 * divergence in a fifth of a real-document sample).  The table now covers the
 * common Microsoft faces, each with a substitute of its own class.
 *
 * <p>It also had the two secondary substitutes the wrong way round, so on a box
 * with Liberation but not Croscore, Times New Roman became a sans and Arial a
 * serif.</p>
 */
public class MetricallyCompatibleSubstituteTest {

	private static Mapper mapper() throws Exception {
		WordprocessingMLPackage pkg = WordprocessingMLPackage.createPackage();
		Mapper mapper = new IdentityPlusMapper();
		pkg.setFontMapper(mapper);
		return mapper;
	}

	/** the physical font this document font maps to, or null if the font is installed
	 *  (in which case the identity mapping applies and there is nothing to check) */
	private static String substituteFor(Mapper mapper, String docFont) {
		if (PhysicalFonts.get(docFont) != null) return null;
		PhysicalFont pf = mapper.get(docFont);
		assertNotNull(docFont + " left unmapped", pf);
		return pf.getName().toLowerCase();
	}

	private static void assertSubstitute(Mapper mapper, String docFont, String... anyOf) {
		String name = substituteFor(mapper, docFont);
		if (name == null) return;
		for (String candidate : anyOf) {
			if (name.contains(candidate)) return;
		}
		org.junit.Assert.fail(docFont + " mapped to " + name + ", expected one of " + String.join(", ", anyOf));
	}

	@Test
	public void serifsGetASerifAndSansGetASans() throws Exception {
		Mapper mapper = mapper();
		assertSubstitute(mapper, "Times New Roman", "tinos", "liberation serif");
		assertSubstitute(mapper, "Georgia", "tinos", "liberation serif");
		assertSubstitute(mapper, "Book Antiqua", "tinos", "liberation serif");
		assertSubstitute(mapper, "Garamond", "tinos", "liberation serif");

		assertSubstitute(mapper, "Arial", "arimo", "liberation sans");
		assertSubstitute(mapper, "Tahoma", "arimo", "liberation sans");
		assertSubstitute(mapper, "Verdana", "arimo", "liberation sans");
		assertSubstitute(mapper, "Segoe UI", "arimo", "liberation sans");
		assertSubstitute(mapper, "Helvetica", "arimo", "liberation sans");

		// crosextra clone where installed, else the Liberation sans (a build server
		// with only the Liberation jar has no Carlito)
		assertSubstitute(mapper, "Calibri Light", "carlito", "liberation sans");
		assertSubstitute(mapper, "Calibri", "carlito", "liberation sans");
		assertSubstitute(mapper, "Cambria", "caladea", "liberation serif");
	}

	/** The pair whose secondary substitutes were swapped: neither may become the other's class. */
	@Test
	public void secondarySubstitutesAreNotSwapped() throws Exception {
		Mapper mapper = mapper();
		String times = substituteFor(mapper, "Times New Roman");
		if (times != null) assertTrue("Times New Roman became a sans: " + times, !times.contains("sans"));
		String arial = substituteFor(mapper, "Arial");
		if (arial != null) assertTrue("Arial became a serif: " + arial, !arial.contains("serif"));
	}

	/**
	 * Century Gothic was drawn to ITC Avant Garde Gothic's widths, and URW Gothic (the
	 * Avant Garde clone in the URW base 35, ie the ghostscript fonts) matches it to the
	 * unit: measured against the Century Gothic a real document had Word embed, URW
	 * Gothic Book is 0.00% different over 6743 characters, and URW Gothic Demi likewise
	 * matches Century Gothic Bold.  Left to the class-based fallback it reached a
	 * Helvetica clone instead, 3.1% wider - enough to break a full line differently.
	 */
	@Test
	public void centuryGothicPrefersItsAvantGardeClone() throws Exception {
		Mapper mapper = mapper();
		if (PhysicalFonts.get("Century Gothic") != null) return; // installed: identity
		if (PhysicalFonts.get("URW Gothic") == null) return; // the URW base 35 is not installed here
		assertSubstitute(mapper, "Century Gothic", "urw gothic");
	}

	/**
	 * Arial Narrow maps to one of its metric twins: Liberation Sans Narrow, which
	 * neither font jar carries and which recent Liberation packages no longer ship, or
	 * Nimbus Sans Narrow, URW's Helvetica Narrow in the base 35 (the ghostscript
	 * fonts).  Nimbus Sans Narrow matches Arial Narrow's advances to within a unit per
	 * 1000 over letters, digits and punctuation - 0.02% mean, bold likewise, against
	 * 14% for Carlito and 22% for Arimo, which is where the fallback used to end up.
	 * Where neither twin is installed, Arial Narrow is left unmapped on purpose (the
	 * other condensed faces a Linux box has are further from its widths than the
	 * document default is, measured over the real-document corpus).
	 */
	@Test
	public void arialNarrowMapsOnlyToItsMetricTwin() throws Exception {
		Mapper mapper = mapper();
		if (PhysicalFonts.get("Arial Narrow") != null) return; // installed: identity
		if (PhysicalFonts.get("Liberation Sans Narrow") != null) {
			assertSubstitute(mapper, "Arial Narrow", "liberation sans narrow");
		} else if (PhysicalFonts.get("Nimbus Sans Narrow") != null) {
			assertSubstitute(mapper, "Arial Narrow", "nimbus sans narrow");
		} else {
			org.junit.Assert.assertNull("Arial Narrow should be left to the default fallback",
					mapper.get("Arial Narrow"));
		}
	}

	/**
	 * Segoe UI Light has no metric clone, but Arimo is the wrong shape for it: measured
	 * against the Segoe UI Light Word embeds, Arimo's advances are systematically 11.8%
	 * wider on letters, so every line breaks early.  Source Sans has no systematic bias
	 * (+0.4% mean signed), which is what line breaking cares about; Arimo stays the
	 * last resort.
	 */
	@Test
	public void segoeUiLightPrefersAFaceWithoutAWidthBias() throws Exception {
		Mapper mapper = mapper();
		if (PhysicalFonts.get("Segoe UI Light") != null) return; // installed: identity
		if (PhysicalFonts.get("Source Sans 3") != null) {
			assertSubstitute(mapper, "Segoe UI Light", "source sans 3");
		} else if (PhysicalFonts.get("Source Sans Pro") != null) {
			assertSubstitute(mapper, "Segoe UI Light", "source sans pro");
		} else {
			assertSubstitute(mapper, "Segoe UI Light", "arimo", "liberation sans");
		}
	}
}
