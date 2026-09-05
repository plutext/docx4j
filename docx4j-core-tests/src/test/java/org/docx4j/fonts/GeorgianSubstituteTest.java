package org.docx4j.fonts;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

import org.docx4j.openpackaging.packages.WordprocessingMLPackage;
import org.junit.Assume;
import org.junit.BeforeClass;
import org.junit.Test;

/**
 * Sylfaen's Georgian (CR-001 &#xa7;5.2).
 *
 * <p>Sylfaen is not installed on a typical Linux box, so its Georgian goes through the
 * glyph-aware pass, which took the first covering face of the document font's class -
 * DejaVu Serif.  Measured against Word's own PDFs: our Georgian lines are 8.6% wide (a
 * line Word ends at x=525.5 ran to 546.6, 21pt past its right edge) and every one of them
 * re-breaks; over 82 exact-match lines of a second document the ratio Word/ours is 0.876.
 * DejaVu Serif Condensed measures 0.900 - the only Georgian-covering face within 3% -
 * where Noto Serif Georgian is 0.999 of DejaVu Serif, i.e. no better.</p>
 *
 * @since 17.0.6
 */
public class GeorgianSubstituteTest {

	@BeforeClass
	public static void discoverFonts() throws Exception {
		WordprocessingMLPackage.createPackage().getFontMapper();
	}

	private static final String GEORGIAN = "გამარჯობა";

	@Test
	public void sylfaensGeorgianTakesTheCondensedFace() throws Exception {

		PhysicalFont condensed = PhysicalFonts.get("DejaVu Serif Condensed");
		Assume.assumeNotNull(condensed);
		Assume.assumeTrue("the condensed face here has no Georgian",
				FontFallback.covers(condensed, GEORGIAN.codePoints().toArray()));

		PhysicalFont chosen = FontFallback.selectCovering("Sylfaen", GEORGIAN.codePoints().toArray());
		assertNotNull(chosen);
		assertEquals("DejaVu Serif Condensed", chosen.getName());
	}

	/** Only Sylfaen, and only its Georgian: nothing else changes. */
	@Test
	public void otherFontsAreUnaffected() throws Exception {

		PhysicalFont chosen = FontFallback.selectCovering("Times New Roman", GEORGIAN.codePoints().toArray());
		Assume.assumeNotNull(chosen);
		org.junit.Assert.assertNotEquals("DejaVu Serif Condensed", chosen.getName());
	}
}
