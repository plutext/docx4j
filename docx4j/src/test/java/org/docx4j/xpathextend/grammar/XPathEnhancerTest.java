package org.docx4j.xpathextend.grammar;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;

import org.docx4j.model.datastorage.XPathEnhancerParser;
import org.junit.Test;

public class XPathEnhancerTest {

	private final static XPathConfiguration SIMPLE = new XPathConfiguration(
			"/prefix", 12);
	private final static XPathConfiguration COMPLICATED = new XPathConfiguration(
			"/doc[17]/baskets/basket", 78);

	@Test
	public void testExtensions() {

		assertUnchanged(SIMPLE, "/somewhere/else");
		assertUnchanged(SIMPLE, "/short");
		assertUnchanged(SIMPLE, "/");

		assertChanged(SIMPLE, "/prefix", "/prefix[12]");
		assertChanged(SIMPLE, "/prefix/further", "/prefix[12]/further");
		assertChanged(SIMPLE, "/prefix/further/@hand",
				"/prefix[12]/further/@hand");

		assertUnchanged(SIMPLE, "string(/prefi/*[5])='got'");
		assertChanged(
				SIMPLE,
				"string(/prefix/property) = 'wisdom' and boolean(/prefix/imaginary)",
				"string(/prefix[12]/property) = 'wisdom' and boolean(/prefix[12]/imaginary)");
		
		assertChanged(COMPLICATED, "/doc[17]/baskets/basket/hay", "/doc[17]/baskets/basket[78]/hay");
	}

	private void assertUnchanged(final XPathConfiguration config,
			final String xpath) {
		assertEquals(xpath, enhance(config, xpath));
	}

	private void assertChanged(final XPathConfiguration config,
			final String xpath, final String newVersion) {
		String enhanced = enhance(config, xpath);
		assertFalse(xpath.equals(enhanced));
		if (newVersion != null)
			assertEquals(newVersion, enhanced);
	}

	private String enhance(final XPathConfiguration config, final String xpath) {
		final String enhanced = XPathEnhancerParser.enhanceXPath(
				config.getPrefix(), config.getIndex(), xpath);
		return enhanced;
	}
}
