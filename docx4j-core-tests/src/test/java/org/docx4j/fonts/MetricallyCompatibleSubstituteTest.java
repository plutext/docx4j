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

		assertSubstitute(mapper, "Calibri Light", "carlito");
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
	 * Arial Narrow maps to Liberation Sans Narrow, which is metric-compatible with it but
	 * which neither font jar carries; where the box has not installed it either, Arial
	 * Narrow is left unmapped on purpose (the condensed faces a Linux box does have are
	 * further from Arial Narrow's widths than the document default is, measured over the
	 * real-document corpus).
	 */
	@Test
	public void arialNarrowMapsOnlyToItsMetricTwin() throws Exception {
		Mapper mapper = mapper();
		if (PhysicalFonts.get("Arial Narrow") != null) return; // installed: identity
		if (PhysicalFonts.get("Liberation Sans Narrow") != null) {
			assertSubstitute(mapper, "Arial Narrow", "liberation sans narrow");
		} else {
			org.junit.Assert.assertNull("Arial Narrow should be left to the default fallback",
					mapper.get("Arial Narrow"));
		}
	}
}
