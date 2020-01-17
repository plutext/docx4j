package org.docx4j.model.datastorage;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;

import org.docx4j.model.datastorage.XPathEnhancerParser;
import org.junit.Test;

public class XPathEnhancerTest {

	private final static XPathConfiguration SIMPLE = new XPathConfiguration(
			"/prefix", 12);

	
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
		
	}

	@Test
	public void testComplicated() {

		// repeating basket
		XPathConfiguration COMPLICATED = new XPathConfiguration("/doc[17]/baskets/basket", 78);
		
		assertChanged(COMPLICATED, "/doc[17]/baskets/basket/hay", 
								   "/doc[17]/baskets/basket[78]/hay");
	}

	@Test
	public void testNoWildcard1() {

		// repeating properties
		XPathConfiguration WILDCARD = new XPathConfiguration(
				OpenDoPEHandler.getRepeatXpathBase("/yaml/components[1]/schemas[1]/foo[1]/properties[1]"), 78);
		
		assertChanged(WILDCARD, "/yaml/components[1]/schemas[1]/foo[1]/properties[1]", 
								"/yaml/components[1]/schemas[1]/foo[1]/properties[78][1]"); // not ideal, but ok
		
		assertChanged(WILDCARD, "/yaml/components[1]/schemas[1]/foo[1]/properties", 
								"/yaml/components[1]/schemas[1]/foo[1]/properties[78]");
		
	}	

	@Test
	public void testWildcardMiddle1() {

		// repeating properties
		XPathConfiguration WILDCARD = new XPathConfiguration(
				OpenDoPEHandler.getRepeatXpathBase("/yaml/components[1]/schemas[1]/*/properties[1]"), 78);
		
		assertChanged(WILDCARD, "/yaml/components[1]/schemas[1]/*/properties[1]", 
								"/yaml/components[1]/schemas[1]/*/properties[78][1]"); // not ideal, but ok
	}
	
	@Test
	public void testWildcardMiddle2() {

		// repeating properties
		XPathConfiguration WILDCARD = new XPathConfiguration(
				OpenDoPEHandler.getRepeatXpathBase("/yaml/components[1]/schemas[1]/*[1]/properties[1]"), 78);
		
		assertChanged(WILDCARD, "/yaml/components[1]/schemas[1]/*[1]/properties[1]", 
								"/yaml/components[1]/schemas[1]/*[1]/properties[78][1]"); // not ideal, but ok

	}

	@Test
	public void testWildcardEnd1() {

		// repeating wildcard
		XPathConfiguration WILDCARD = new XPathConfiguration(
				OpenDoPEHandler.getRepeatXpathBase("/yaml/components[1]/schemas[1]/*"), 78);

		assertChanged(WILDCARD, "/yaml/components[1]/schemas[1]/*", 
								"/yaml/components[1]/schemas[1]/*[78]"); 
		
		assertChanged(WILDCARD, "/yaml/components[1]/schemas[1]/*/properties[1]", 
								"/yaml/components[1]/schemas[1]/*[78]/properties[1]"); 
	}

	@Test
	public void testWildcardEnd2() {

		// repeating wildcard
		XPathConfiguration WILDCARD = new XPathConfiguration(
				OpenDoPEHandler.getRepeatXpathBase("/yaml/components[1]/schemas[1]/*[1]"), 78);

		assertChanged(WILDCARD, "/yaml/components[1]/schemas[1]/*[1]", 
								"/yaml/components[1]/schemas[1]/*[78][1]"); // not ideal, but ok
		
		assertChanged(WILDCARD, "/yaml/components[1]/schemas[1]/*[1]/properties[1]", 
								"/yaml/components[1]/schemas[1]/*[78][1]/properties[1]");  // not ideal, but ok
	}
	
	private void assertUnchanged(final XPathConfiguration config,
			final String xpath) {
		assertEquals(xpath, enhance(config, xpath));
	}

	private void assertChanged(final XPathConfiguration config,
			final String xpath, final String newVersion) {
		String enhanced = enhance(config, xpath);
		assertFalse(xpath.equals(enhanced));
		if (newVersion != null) {
//			System.out.println("Expected: " + newVersion);
//			System.out.println("Got     : " + enhanced);
			assertEquals(newVersion, enhanced);
		}
	}

	private String enhance(final XPathConfiguration config, final String xpath) {
		final String enhanced = XPathEnhancerParser.enhanceXPath(
				config.getPrefix(), config.getIndex(), xpath);
		return enhanced;
	}
}
