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
package org.docx4j.fonts;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotEquals;
import static org.junit.Assert.assertTrue;

import java.util.Collections;
import java.util.Set;

import org.docx4j.XmlUtils;
import org.docx4j.jaxb.Context;
import org.docx4j.openpackaging.parts.relationships.Namespaces;
import org.docx4j.wml.Fonts;
import org.junit.Test;

/**
 * The line-metrics alias a document declares for itself - a {@code w:altName} docx4j
 * resolved, or Word's answer for a font it cannot find - belongs to <b>that conversion</b>,
 * and must not answer for the next document in the same JVM.
 *
 * <p>It was a static map in {@code WordLineMetrics} until 17.2.0, never cleared, so it did:
 * measured over the three real-document corpora, 93 of 449 documents register at least one
 * alias (137 registrations, 116 distinct font names), and replaying the corpora in the
 * order the harness converts them found four documents reading an alias that another
 * document had left behind - one of them a sans picking up Cambria, a serif. A server
 * converting document after document is the case that breaks, and because the effect
 * depends on what ran before, it is hard to see.</p>
 *
 * @since 17.2.0
 */
public class DocumentAliasScopeTest {

	/** A name no machine has a family for, so only the altName can resolve it, and one
	 *  with no class word in it, so the name heuristic cannot claim it either. */
	private static final String MADE_UP = "Zqxjk Nonesuch LT 55";

	private static Fonts fontTable(String fontName, String altName) throws Exception {
		return (Fonts) XmlUtils.unmarshalString(
				"<w:fonts xmlns:w=\"" + Namespaces.NS_WORD12 + "\">"
				+ "<w:font w:name=\"" + fontName + "\">"
				+ (altName == null ? "" : "<w:altName w:val=\"" + altName + "\"/>")
				+ "<w:charset w:val=\"00\"/><w:family w:val=\"swiss\"/><w:pitch w:val=\"variable\"/>"
				+ "</w:font></w:fonts>", Context.jc, Fonts.class);
	}

	/** A mapper as WordprocessingMLPackage.setFontMapper builds one, for a document whose
	 *  only font is MADE_UP, with or without a w:altName. */
	private static Mapper conversion(String altName) throws Exception {
		Mapper mapper = new IdentityPlusMapper();
		Set<String> inUse = Collections.singleton(MADE_UP);
		Fonts fonts = fontTable(MADE_UP, altName);
		mapper.populateFontMappings(inUse, fonts);
		mapper.addMetricallyCompatibleSubstitutes();
		mapper.addAltNameSubstitutes(inUse, fonts);
		mapper.addClassBasedSubstitutes(inUse);
		mapper.addWordDefaultSubstitutes(inUse, fonts);
		return mapper;
	}

	/** The seam: the alias is the Mapper's, and another Mapper in the same JVM has none. */
	@Test
	public void anAliasDoesNotOutliveItsConversion() throws Exception {

		Mapper withAlias = conversion("Cambria");
		assertEquals("the document's own altName is in force for it",
				"Cambria", withAlias.lineMetricsFamily(MADE_UP));

		Mapper without = conversion(null);
		assertNotEquals("the next conversion must not see it",
				"Cambria", without.lineMetricsFamily(MADE_UP));
	}

	/** And the line box follows: the second document's is not the first's alias's. */
	@Test
	public void aSecondConversionDoesNotSeeTheFirstsLineBox() throws Exception {

		Mapper withAlias = conversion("Cambria");
		Mapper without = conversion(null);

		PhysicalFont pf = PhysicalFonts.get("Arimo Regular");
		double aliased = WordLineMetrics.get(withAlias.lineMetricsFamily(MADE_UP), pf).lineHeightFactor();
		double own = WordLineMetrics.get(without.lineMetricsFamily(MADE_UP), pf).lineHeightFactor();

		assertEquals("the first document takes Cambria's line box",
				WordLineMetrics.get("Cambria", pf).lineHeightFactor(), aliased, 0.0001);
		assertNotEquals("the second must not", aliased, own, 0.0001);
	}

	/**
	 * The reverse order, so that the scoping cannot be mistaken for "aliases are off":
	 * each conversion gets <em>its own</em> answer whichever ran first.
	 *
	 * <p>A document with no {@code w:altName} is not a document with no alias: this one's
	 * font is a family nothing knows, so Word's own default answers for it - Calibri, the
	 * entry's {@code w:family="swiss"} - and that is registered too
	 * ({@code addWordDefaultSubstitutes}).  Which is the point: both passes register, and
	 * both answers have to stay inside their own conversion.</p>
	 */
	@Test
	public void eachConversionKeepsItsOwnAliasInEitherOrder() throws Exception {

		Mapper first = conversion(null);
		assertEquals("Word's default for an unknown swiss family",
				"Calibri", first.lineMetricsFamily(MADE_UP));

		Mapper second = conversion("Cambria");
		assertEquals("the later document's own altName must still be honoured",
				"Cambria", second.lineMetricsFamily(MADE_UP));

		// and the first one is unchanged by the second having run
		assertEquals("Calibri", first.lineMetricsFamily(MADE_UP));
	}

	/** A font the table has metrics of its own for is never aliased away from them. */
	@Test
	public void aFamilyWithItsOwnMetricsIsNotAliased() {
		Mapper mapper = new IdentityPlusMapper();
		mapper.registerLineMetricsAlias("Cambria", "Calibri");
		assertEquals("Cambria", mapper.lineMetricsFamily("Cambria"));
		assertTrue(WordLineMetrics.hasTableEntry("Cambria"));
	}

	/** Nothing a document registers reaches the static table. */
	@Test
	public void theTableItselfIsUntouched() throws Exception {
		conversion("Cambria");
		assertFalse("the alias must not have become a table entry",
				WordLineMetrics.hasTableEntry(MADE_UP));
		assertFalse(WordLineMetrics.isTableFamily(MADE_UP));
	}

	/** The 17.1.0 entry point is kept for callers, and does nothing. */
	@Test
	@SuppressWarnings("deprecation")
	public void theDeprecatedStaticEntryPointIsANoOp() {
		WordLineMetrics.registerAlias(MADE_UP, "Cambria");
		assertFalse("it must not have registered anything",
				WordLineMetrics.hasTableEntry(MADE_UP));
		WordLineMetrics.registerAlias(null, null); // and tolerates nulls
	}
}
