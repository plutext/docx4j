package org.docx4j.fonts;

import static org.junit.Assert.assertFalse;

import org.junit.Test;

/**
 * Some families report one name for every face.  In the URW base 35 (the ghostscript
 * fonts most Linux boxes have), URW Gothic Book, Demi, Book Oblique and Demi Oblique
 * all call themselves "URW Gothic", and FOP's font detection reports each as upright
 * weight 400, so nothing but the file name tells them apart.  Registering them
 * last-one-wins made the family name mean whichever the file system handed over last:
 * on the box where this was found, "Nimbus Roman" was NimbusRoman-BoldItalic and "URW
 * Gothic" was URWGothic-DemiOblique, so a substitution which asked for the family got
 * a bold italic - and set text 3.5% wide of the font it was standing in for.
 *
 * @since 17.0.5
 */
public class PhysicalFontFamilyFaceTest {

	/** Families in that position, where the box has them. */
	private static final String[] SHARED_NAME_FAMILIES = {
		"URW Gothic", "URW Bookman", "Nimbus Roman", "Nimbus Sans", "Nimbus Mono PS", "C059", "P052" };

	@Test
	public void aFamilySharingOneNameRegistersItsPlainestFace() throws Exception {

		PhysicalFonts.discoverPhysicalFonts();
		for (String family : SHARED_NAME_FAMILIES) {
			PhysicalFont pf = PhysicalFonts.get(family);
			if (pf == null || pf.getEmbeddedURI() == null) continue; // not installed here
			String file = pf.getEmbeddedURI().toString().toLowerCase();
			file = file.substring(file.lastIndexOf('/') + 1);
			assertFalse(family + " is registered as an italic face: " + file,
					file.contains("italic") || file.contains("oblique"));
			assertFalse(family + " is registered as a bold face: " + file,
					file.contains("bold") || file.contains("demi") || file.contains("black"));
		}
	}
}
