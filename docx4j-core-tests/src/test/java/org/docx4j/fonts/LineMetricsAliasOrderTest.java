package org.docx4j.fonts;

import static org.junit.Assert.assertEquals;

import org.junit.Test;

/**
 * The first line-metrics alias registered for a document font wins.
 *
 * <p>Two passes register one: {@link Mapper#addAltNameSubstitutes}, from the document's own
 * <code>w:altName</code>, and {@link Mapper#addWordDefaultSubstitutes}, from Word's answer
 * for a font it cannot find. The altName pass runs first and is the more specific answer -
 * the document is telling us which font its author had - where the default pass is a guess
 * of last resort. Until 17.1.1 the register did an unconditional put, so a later pass
 * silently replaced an earlier family's line box with Cambria's or Calibri's.</p>
 *
 * <p><b>No document of the three reference corpora reaches that today</b>, and the ordering
 * is fixed as a contract rather than for a measured gain: scanned over 454 documents, 52
 * have an altName-pass alias (65 names) and <b>not one</b> is replaced by a later pass,
 * because the passes are disjoint - the default pass only handles fonts still unmapped, and
 * a font whose alias the altName pass registered is one it mapped. The guard matters for any
 * future pass that registers a box for a font it does not itself map (CR-001 batch 43).</p>
 *
 * @since 17.1.1
 */
public class LineMetricsAliasOrderTest {

	/** a name no machine has a family for, and which no docx4j table knows */
	private static final String MADE_UP = "Docx4j Test Face Order 01";

	@Test
	public void theFirstRegistrationWins() {
		Mapper m = new IdentityPlusMapper();
		assertEquals("no alias yet: the name answers for itself",
				MADE_UP, m.lineMetricsFamily(MADE_UP));

		m.registerLineMetricsAlias(MADE_UP, "Meiryo");
		assertEquals("Meiryo", m.lineMetricsFamily(MADE_UP));

		// what addWordDefaultSubstitutes would do afterwards
		m.registerLineMetricsAlias(MADE_UP, "Calibri");
		assertEquals("the later, less specific answer must not replace the first",
				"Meiryo", m.lineMetricsFamily(MADE_UP));
	}

	/** A font with line metrics of its own is never aliased at all - unchanged behaviour. */
	@Test
	public void aFamilyWithItsOwnMetricsIsNotAliased() {
		Mapper m = new IdentityPlusMapper();
		m.registerLineMetricsAlias("Cambria", "Meiryo");
		assertEquals("Cambria", m.lineMetricsFamily("Cambria"));
	}

	/** Each Mapper is a conversion's own: one document's alias never reaches another. */
	@Test
	public void aliasesAreNotSharedBetweenMappers() {
		Mapper a = new IdentityPlusMapper();
		a.registerLineMetricsAlias(MADE_UP, "Meiryo");
		assertEquals("Meiryo", a.lineMetricsFamily(MADE_UP));
		assertEquals(MADE_UP, new IdentityPlusMapper().lineMetricsFamily(MADE_UP));
	}
}
