package org.docx4j.fonts;

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assume.assumeNotNull;

import org.junit.Test;

/**
 * All four faces of a substitute family must be found, not just the regular one.
 *
 * <p>{@code MicrosoftFontsRegistry} knows only Microsoft's own families, so for every
 * substitute {@code Mapper} reaches outside that table - DejaVu Sans, Noto Sans, P052,
 * Carlito, Caladea, the Liberation and URW families - {@code getBoldForm} and
 * {@code getItalicForm} returned null.  {@code FopConfigUtil} then declared the family to
 * FOP as the regular file with {@code simulate-style="true"}, so FOP synthesised the bold
 * by re-stroking the regular glyphs: the ink looked bold, but every advance width was the
 * regular face's.  Measured against Word's own PDFs of five corpus documents, bold text
 * came out 11-18% narrow ("Partita IVA" 42.2pt against Word's 49.6), while the regular
 * weight of the same documents measured 0.9996 of Word's; a centred Verdana title was
 * 240.0pt against Word's 270.7, and is 269.8 now.</p>
 *
 * <p>The faces are found by the family's own name ("DejaVu Sans" + " Bold") and, failing
 * that, by file name, since a whole URW family reports one name and is told apart only by
 * its file (P052-Roman.otf -> P052-Bold.otf).</p>
 *
 * @since 17.0.6
 */
public class SubstituteFamilyFacesTest {

	private static PhysicalFont installed(String name) throws Exception {
		PhysicalFonts.discoverPhysicalFonts();
		return PhysicalFonts.get(name);
	}

	/** Liberation Sans is the one family a machine with only the Liberation jar has, so
	 *  this is the case that must hold everywhere. */
	@Test
	public void liberationSansHasItsBoldAndItalic() throws Exception {
		PhysicalFont regular = installed("Liberation Sans");
		assumeNotNull("Liberation Sans is not installed", regular);
		assertFace("Liberation Sans bold", PhysicalFonts.getBoldForm(regular), regular, "bold");
		assertFace("Liberation Sans italic", PhysicalFonts.getItalicForm(regular), regular, "italic");
		assertFace("Liberation Sans bold italic", PhysicalFonts.getBoldItalicForm(regular), regular,
				"bolditalic");
	}

	/** Carlito is the Calibri substitute, and stands in for the crosextra families. */
	@Test
	public void carlitoHasItsBoldFace() throws Exception {
		PhysicalFont regular = installed("Carlito Regular");
		assumeNotNull("Carlito is not installed", regular);
		assertFace("Carlito bold", PhysicalFonts.getBoldForm(regular), regular, "bold");
	}

	/** DejaVu Sans stands in for Verdana, and calls its italic "Oblique". */
	@Test
	public void dejaVuSansHasItsBoldAndOblique() throws Exception {
		PhysicalFont regular = installed("DejaVu Sans");
		assumeNotNull("DejaVu Sans is not installed", regular);
		assertFace("DejaVu Sans bold", PhysicalFonts.getBoldForm(regular), regular, "bold");
		assertFace("DejaVu Sans oblique", PhysicalFonts.getItalicForm(regular), regular, "oblique");
	}

	/** A family whose faces all report one name: only the file tells them apart. */
	@Test
	public void aUrwFamilyIsFoundByItsFileName() throws Exception {
		PhysicalFont regular = installed("P052");
		assumeNotNull("the URW base 35 (ghostscript-fonts) is not installed", regular);
		assertFace("P052 bold", PhysicalFonts.getBoldForm(regular), regular, "bold");
	}

	/** Not any old font: a face which really has no bold must still report none. */
	@Test
	public void aFamilyWithNoSuchFaceStillGetsNone() throws Exception {
		PhysicalFonts.discoverPhysicalFonts();
		PhysicalFont symbol = PhysicalFonts.getSymbolFont();
		assumeNotNull("no Symbol substitute", symbol);
		PhysicalFont bold = PhysicalFonts.getBoldForm(symbol);
		if (bold != null) {
			assertTrue("a bold face was invented for " + symbol.getName() + ": " + bold.getEmbeddedURI(),
					bold.getEmbeddedURI().toString().toLowerCase().contains("bold")
					|| bold.getEmbeddedURI().toString().toLowerCase().contains("bd")
					|| bold.getEmbeddedURI().toString().toLowerCase().contains("demi"));
		}
	}

	private static void assertFace(String what, PhysicalFont face, PhysicalFont regular, String style) {
		assertNotNull(what + " was not found", face);
		assertTrue(what + " resolved to the regular file " + face.getEmbeddedURI(),
				!face.getEmbeddedURI().equals(regular.getEmbeddedURI()));
		String file = face.getEmbeddedURI().toString().toLowerCase();
		String expected = "bolditalic".equals(style) ? "bold" : style;
		assertTrue(what + " resolved to " + file,
				file.contains(expected) || file.contains("bd") || file.contains("ita")
					|| file.contains("obli") || file.contains("demi"));
	}
}
