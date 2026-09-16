package org.docx4j.fonts;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.Set;

import org.docx4j.XmlUtils;
import org.docx4j.jaxb.Context;
import org.docx4j.openpackaging.parts.relationships.Namespaces;
import org.docx4j.wml.Fonts;
import org.junit.Test;

/**
 * Where a document's <code>w:altName</code> chain passes through a family the line-metrics
 * table flags <b>East Asian</b> and this machine does not have, the line box is that
 * family's - 1.3 x its usWin box - even though the chain goes on to find the face that
 * renders the text.
 *
 * <p>Measured (CR-001 batch 43): a corpus document whose table-of-contents entries name a
 * font no machine has, with an alternate of Meiryo which Word resolved and drew (its golden
 * embeds Meiryo, Meiryo-Bold and Meiryo-Italic). Word's pitch on those entries is 22.44pt
 * at 10pt with <code>w:line="276"</code> auto; Meiryo's row is
 * <code>2048;2171;901;2171;-901;0;1</code>, so 1.5000 x 1.3 x 1.15 x 10 = <b>22.43</b>.
 * docx4j drew the substitute with Calibri's box and got 14.04, the 60% error ledger4
 * measured.</p>
 *
 * <p><b>East Asian only.</b> Batch 42 &#xa7;28.1 measured ten documents whose Word-resolved
 * altName ends at a Latin family, and in every one the line box already agrees with Word to
 * within 1.5% - two of them at 1.0000 line parity and page-exact - so taking the chain
 * end's box for a Latin family can only break them.</p>
 *
 * @since 17.1.1
 */
public class AltNameEastAsianLineBoxTest {

	/** names no machine has a family for, so only the altName chain can resolve them */
	private static final String MADE_UP = "Docx4j Test Face CJK 01";
	private static final String MADE_UP_LATIN = "Docx4j Test Face Latin 01";

	private static Fonts fontTable(String... nameThenAlt) throws Exception {
		StringBuilder sb = new StringBuilder("<w:fonts xmlns:w=\"" + Namespaces.NS_WORD12 + "\">");
		for (int i = 0; i < nameThenAlt.length; i += 2) {
			sb.append("<w:font w:name=\"").append(nameThenAlt[i]).append("\">")
					.append("<w:altName w:val=\"").append(nameThenAlt[i + 1]).append("\"/>")
					.append("<w:charset w:val=\"00\"/><w:family w:val=\"swiss\"/>")
					.append("<w:pitch w:val=\"variable\"/></w:font>");
		}
		return (Fonts) XmlUtils.unmarshalString(sb.append("</w:fonts>").toString(),
				Context.jc, Fonts.class);
	}

	private static Mapper mapper(Set<String> inUse, Fonts fonts) throws Exception {
		Mapper mapper = new IdentityPlusMapper();
		mapper.populateFontMappings(inUse, fonts);
		mapper.addMetricallyCompatibleSubstitutes();
		mapper.addAltNameSubstitutes(inUse, fonts);
		return mapper;
	}

	/**
	 * The chain is "made-up face" -&gt; Meiryo -&gt; Arial: Meiryo is East Asian and absent
	 * here, so it gives the line box, while Arial gives the face that renders the text.
	 */
	@Test
	public void eastAsianHopGivesTheLineBox() throws Exception {
		org.junit.Assume.assumeTrue("Meiryo is installed here, so the chain stops at it",
				PhysicalFonts.get("Meiryo") == null);
		Set<String> inUse = Collections.singleton(MADE_UP);
		Fonts fonts = fontTable(MADE_UP, "Meiryo", "Meiryo", "Arial");
		Mapper mapper = mapper(inUse, fonts);

		assertNotNull("the chain did not resolve to a face at all", mapper.get(MADE_UP));
		assertEquals("the line box is the East Asian hop's, not the face that renders it",
				"Meiryo", mapper.lineMetricsFamily(MADE_UP));

		// 1.5000 usWin x 1.3 = 1.9500, and w:line="276" auto x 1.15 -> 22.43pt at 10pt
		WordLineMetrics.Metrics m = WordLineMetrics.get(mapper.lineMetricsFamily(MADE_UP), null);
		assertTrue(m.eastAsian);
		assertEquals(1.9500, m.lineHeightFactor(), 1e-4);
		org.docx4j.wml.PPrBase.Spacing auto276 = new org.docx4j.wml.ObjectFactory().createPPrBaseSpacing();
		auto276.setLine(java.math.BigInteger.valueOf(276));
		auto276.setLineRule(org.docx4j.wml.STLineSpacingRule.AUTO);
		assertEquals(22.43, WordLineMetrics.lineHeightPt(
				mapper.lineMetricsFamily(MADE_UP), null, 10, auto276), 0.05);
	}

	/**
	 * A Latin chain end is left where it was: the alias is the family the chain
	 * <em>resolved</em> at, which is what 17.1.1 already did, and batch 42 &#xa7;28.1's ten
	 * documents say that is right.
	 */
	@Test
	public void latinHopIsNotPreferred() throws Exception {
		org.junit.Assume.assumeTrue("Century is installed here",
				PhysicalFonts.get("Century") == null);
		Set<String> inUse = new LinkedHashSet<String>();
		inUse.add(MADE_UP_LATIN);
		Fonts fonts = fontTable(MADE_UP_LATIN, "Century", "Century", "Arial");
		Mapper mapper = mapper(inUse, fonts);

		assertNotNull(mapper.get(MADE_UP_LATIN));
		assertEquals("a Latin hop this machine lacks must not take over the line box",
				"Arial", mapper.lineMetricsFamily(MADE_UP_LATIN));
	}
}
