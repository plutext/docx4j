package org.docx4j.fonts;

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.fail;

import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

import org.docx4j.openpackaging.packages.WordprocessingMLPackage;
import org.junit.Test;

/**
 * A font the metrically-compatible table doesn't know is now mapped to a font of its
 * own class, rather than left to fall back to whatever the document's default font
 * maps to - which is a Times clone standing in for a sans as often as not (CR-001
 * cause C3).  The classes and the candidate lists come from FontSubstitutions.xml,
 * which {@link BestMatchingMapper} already consulted and which
 * {@link IdentityPlusMapper} - the default mapper - did not.
 *
 * @since 17.0.5
 */
public class ClassBasedSubstituteTest {

	private static Mapper mapper(String... documentFonts) throws Exception {
		WordprocessingMLPackage pkg = WordprocessingMLPackage.createPackage();
		Mapper mapper = new IdentityPlusMapper();
		pkg.setFontMapper(mapper);
		Set<String> fonts = new HashSet<String>();
		Collections.addAll(fonts, documentFonts);
		mapper.addClassBasedSubstitutes(fonts);
		return mapper;
	}

	private static void assertClass(String docFont, FontFallback.FontClass expected) throws Exception {

		if (PhysicalFonts.get(docFont)!=null) return; // installed: the identity mapping applies
		PhysicalFont pf = mapper(docFont).get(docFont);
		assertNotNull(docFont + " was left unmapped", pf);
		FontFallback.FontClass actual = FontFallback.classOf(pf.getName());
		if (actual!=expected) {
			fail(docFont + " mapped to " + pf.getName() + ", which is " + actual + ", not " + expected);
		}
	}

	/** the two the ledger names, plus one classified by FontSubstitutions.xml and one
	 *  which only the name heuristic can place */
	@Test
	public void sansStaysSans() throws Exception {
		assertClass("Tahoma", FontFallback.FontClass.SANS);
		assertClass("Century Gothic", FontFallback.FontClass.SANS);
		assertClass("HelveticaNeue LT 55 Roman", FontFallback.FontClass.SANS);
	}

	@Test
	public void serifStaysSerif() throws Exception {
		assertClass("Georgia", FontFallback.FontClass.SERIF);
		assertClass("Calisto MT", FontFallback.FontClass.SERIF);
		assertClass("Baskerville Old Face", FontFallback.FontClass.SERIF);
	}

	@Test
	public void monospaceStaysMonospace() throws Exception {
		assertClass("Consolas", FontFallback.FontClass.MONO);
		assertClass("Andale Mono", FontFallback.FontClass.MONO);
	}

	/**
	 * A condensed face is deliberately left unmapped: measured over a real-document
	 * corpus, the condensed faces a Linux box does have are further from Arial Narrow's
	 * widths than the document default is, so substituting one cost line parity.
	 */
	@Test
	public void condensedFacesAreLeftAlone() throws Exception {
		if (PhysicalFonts.get("Arial Narrow")!=null) return;
		// its metric twins, which addMetricallyCompatibleSubstitutes maps it to
		if (PhysicalFonts.get("Liberation Sans Narrow")!=null) return;
		if (PhysicalFonts.get("Nimbus Sans Narrow")!=null) return;
		assertNull("Arial Narrow should be left to the document default",
				mapper("Arial Narrow").get("Arial Narrow"));
	}

	/**
	 * Nor is a family whose stand-in is measurably further from its widths than the
	 * document default is: Arimo set a Lato document a page longer than the default did.
	 */
	@Test
	public void familiesMeasuredWorseOffAreLeftAlone() throws Exception {
		if (PhysicalFonts.get("Lato")!=null) return;
		assertNull("Lato should be left to the document default", mapper("Lato").get("Lato"));
		assertNull("Lato Light should be left to the document default",
				mapper("Lato Light").get("Lato Light"));
	}

	/**
	 * A PostScript name is one no system has a family for, so Word doesn't resolve it
	 * either and falls back to the document default; matching Word means doing the same.
	 * The plain family name is substituted as usual.
	 */
	@Test
	public void postScriptNamesAreLeftToTheDocumentDefault() throws Exception {
		assertNull("a PostScript name should be left to the document default",
				mapper("TimesNewRomanPS-BoldMT").get("TimesNewRomanPS-BoldMT"));
		assertNull("a PostScript name should be left to the document default",
				mapper("MyriadPro-Regular").get("MyriadPro-Regular"));
		if (PhysicalFonts.get("Myriad")==null) {
			assertNotNull("the plain family name should still be substituted",
					mapper("Myriad").get("Myriad"));
		}
	}

	/** A name whose only clue is that it ends in "Sans" is not grounds to substitute. */
	@Test
	public void aGenericNameIsNotGroundsToSubstitute() throws Exception {
		if (PhysicalFonts.get("Gill Sans")!=null) return;
		assertNull("Gill Sans should be left to the document default",
				mapper("Gill Sans").get("Gill Sans"));
		// but it is still recognised as a sans where the question is only what class it is
		org.junit.Assert.assertEquals(FontFallback.FontClass.SANS, FontFallback.classOf("Gill Sans"));
	}

	/** A symbol or decorative face has no stand-in "of its class"; leave it be. */
	@Test
	public void symbolFacesAreLeftAlone() throws Exception {
		if (PhysicalFonts.get("Wingdings")!=null) return;
		assertNull("Wingdings should not get a text font",
				mapper("Wingdings").get("Wingdings"));
	}

	/** BestMatchingMapper reaches its own conclusions; this step is not for it. */
	@Test
	public void bestMatchingMapperIsUnchanged() throws Exception {
		org.junit.Assert.assertFalse(new BestMatchingMapper().wantsClassBasedSubstitutes());
		org.junit.Assert.assertTrue(new IdentityPlusMapper().wantsClassBasedSubstitutes());
	}
}
