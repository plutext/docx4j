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
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertSame;
import static org.junit.Assert.assertTrue;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import org.docx4j.XmlUtils;
import org.docx4j.wml.Fonts;
import org.junit.Assume;
import org.junit.BeforeClass;
import org.junit.Test;

/**
 * The one order of precedence both mappers follow (CR-016 phase 3), and the passes that
 * run after it: the altName chain, Word's default for an unknown family, the alias for a
 * family with no bold face.
 */
public class MapperPrecedenceTest {

	private static final String W = "http://schemas.openxmlformats.org/wordprocessingml/2006/main";
	private static final String SANS = "Liberation Sans";
	private static final String SERIF = "Liberation Serif";

	@BeforeClass
	public static void fonts() {
		new IdentityPlusMapper(); // discovery
		Assume.assumeTrue("Liberation not on the classpath", PhysicalFonts.get(SANS) != null && PhysicalFonts.get(SERIF) != null);
	}

	private static Fonts fontTable(String entries) throws Exception {
		return (Fonts) XmlUtils.unwrap(XmlUtils.unmarshalString(
				"<w:fonts xmlns:w=\"" + W + "\">" + entries + "</w:fonts>"));
	}

	private static Set<String> names(String... n) {
		return new HashSet<String>(Arrays.asList(n));
	}

	@Test
	public void theInstalledFontWinsOverTheEmbeddedOneInBothMappers() throws Exception {
		for (Mapper m : new Mapper[] { new IdentityPlusMapper(), new BestMatchingMapper() }) {
			// the document embeds a "Liberation Sans" which is really the Serif file
			m.registerRegularForm(SANS, PhysicalFonts.get(SERIF));
			m.populateFontMappings(names(SANS), fontTable(""));
			assertSame(m.getClass().getSimpleName(), PhysicalFonts.get(SANS), m.get(SANS));
		}
	}

	@Test
	public void theEmbeddedFormWhereTheMachineLacksTheFont() throws Exception {
		for (Mapper m : new Mapper[] { new IdentityPlusMapper(), new BestMatchingMapper() }) {
			m.registerBoldForm("Docx4j Embedded Only", PhysicalFonts.get(SERIF));
			m.populateFontMappings(names("Docx4j Embedded Only"), fontTable(""));
			assertSame(m.getClass().getSimpleName(), PhysicalFonts.get(SERIF), m.get("Docx4j Embedded Only"));
		}
	}

	@Test
	public void identityFaceOrderIsRegularBoldItalic() throws Exception {
		// a family with no plain face: bold before italic (until 17.1.1 italic won, and
		// upright text came out italic)
		PhysicalFonts.put("Docx4j Faceorder bold", PhysicalFonts.get(SANS));
		PhysicalFonts.put("Docx4j Faceorder italic", PhysicalFonts.get(SERIF));
		IdentityPlusMapper m = new IdentityPlusMapper();
		m.populateFontMappings(names("Docx4j Faceorder"), fontTable(""));
		assertSame(PhysicalFonts.get(SANS), m.get("Docx4j Faceorder"));
	}

	@Test
	public void altNameChain() throws Exception {
		IdentityPlusMapper m = new IdentityPlusMapper();
		Fonts table = fontTable(
				"<w:font w:name=\"Docx4j Alt A\"><w:altName w:val=\"Docx4j Alt B\"/></w:font>"
				+ "<w:font w:name=\"Docx4j Alt B\"><w:altName w:val=\"" + SERIF + "\"/></w:font>"
				+ "<w:font w:name=\"Docx4j Alt Loop\"><w:altName w:val=\"Docx4j Alt Loop\"/></w:font>");
		m.populateFontMappings(names("Docx4j Alt A", "Docx4j Alt Loop"), table);
		m.addAltNameSubstitutes(names("Docx4j Alt A", "Docx4j Alt Loop"), table);
		assertSame("two hops", PhysicalFonts.get(SERIF), m.get("Docx4j Alt A"));
		assertNull("a cycle resolves to nothing", m.get("Docx4j Alt Loop"));
	}

	@Test
	public void wordsDefaultForAnUnknownFamily() throws Exception {
		Fonts table = fontTable(
				"<w:font w:name=\"Docx4j Probe B\"><w:panose1 w:val=\"020B0604020202020204\"/><w:charset w:val=\"00\"/><w:family w:val=\"swiss\"/></w:font>"
				+ "<w:font w:name=\"Docx4j Probe C\"><w:charset w:val=\"00\"/><w:family w:val=\"roman\"/></w:font>"
				+ "<w:font w:name=\"Docx4j Probe E\"><w:altName w:val=\"Docx4j Probe X\"/><w:charset w:val=\"00\"/></w:font>"
				+ "<w:font w:name=\"Docx4j Probe F\"><w:altName w:val=\"Docx4j Probe Y\"/></w:font>"
				+ "<w:font w:name=\"Docx4j Probe Y\"><w:family w:val=\"swiss\"/></w:font>"
				+ "<w:font w:name=\"Docx4j Probe M\"><w:family w:val=\"modern\"/></w:font>");
		Map<String, Fonts.Font> t = Mapper.fontTable(table);
		// the golden fonts-unresolvable: (a) no entry Cambria, (b) swiss Calibri (panose
		// unread), (c) roman Cambria, (e) an entry without a family Calibri, (f) the
		// altName's family
		assertEquals("Cambria", Mapper.wordDefaultFor("Docx4j Probe A", t));
		assertEquals("Calibri", Mapper.wordDefaultFor("Docx4j Probe B", t));
		assertEquals("Cambria", Mapper.wordDefaultFor("Docx4j Probe C", t));
		assertEquals("Calibri", Mapper.wordDefaultFor("Docx4j Probe E", t));
		assertEquals("Calibri", Mapper.wordDefaultFor("Docx4j Probe F", t));
		assertEquals("Courier New", Mapper.wordDefaultFor("Docx4j Probe M", t));

		assertTrue(Mapper.isKnownFamily("Arial"));
		assertTrue(Mapper.isKnownFamily("Calibri Light"));
		assertTrue(Mapper.isKnownFamily("Trebuchet MS"));
		assertFalse(Mapper.isKnownFamily("Docx4j Probe B"));

		// through the mapper: the unknown family gets what Calibri / Cambria map to
		IdentityPlusMapper m = new IdentityPlusMapper();
		m.put("Calibri", PhysicalFonts.get(SANS));
		m.put("Cambria", PhysicalFonts.get(SERIF));
		Set<String> names = names("Docx4j Probe A", "Docx4j Probe B", "Docx4j Probe C", "Docx4j Probe E", "Arial Narrow");
		m.addWordDefaultSubstitutes(names, table);
		assertSame(PhysicalFonts.get(SERIF), m.get("Docx4j Probe A"));
		assertSame(PhysicalFonts.get(SANS), m.get("Docx4j Probe B"));
		assertSame(PhysicalFonts.get(SERIF), m.get("Docx4j Probe C"));
		assertSame(PhysicalFonts.get(SANS), m.get("Docx4j Probe E"));
		assertNull("a known family is not Word-defaulted", m.get("Arial Narrow"));
		// and the line box is the face Word uses
		assertEquals(WordLineMetrics.get("Cambria", null).lineHeightFactor(),
				WordLineMetrics.get("Docx4j Probe A", null).lineHeightFactor(), 0.0001);
	}

	@Test
	public void aFamilyWithNoBoldFace() throws Exception {
		assertTrue(Mapper.hasBoldFace("Calibri"));
		assertTrue(Mapper.hasBoldFace("Arial"));
		assertFalse("a weight-named family", Mapper.hasBoldFace("Calibri Light"));
		assertFalse(Mapper.hasBoldFace("Segoe UI Semibold"));
		assertFalse("the registry entry has no bold child", Mapper.hasBoldFace("Arial Black"));
		assertTrue(Mapper.hasBoldFace("Some Unknown Family"));

		IdentityPlusMapper m = new IdentityPlusMapper();
		m.put("Calibri Light", PhysicalFonts.get(SANS));
		m.put("Calibri", PhysicalFonts.get(SANS));
		Assume.assumeTrue("Liberation Sans Bold not installed", PhysicalFonts.getBoldForm(PhysicalFonts.get(SANS)) != null);
		m.addNoBoldFaceAliases(names("Calibri Light", "Calibri"));
		PhysicalFont alias = m.get("Calibri Light");
		assertTrue(alias.isNoBoldFace());
		assertEquals(SANS + PhysicalFont.NOBOLD_SUFFIX, alias.getName());
		assertNull("FOP is to synthesise the bold", PhysicalFonts.getBoldForm(alias));
		assertNull(PhysicalFonts.getBoldItalicForm(alias));
		assertSame("the alias's name resolves to the file's own entry", PhysicalFonts.get(SANS), PhysicalFonts.get(alias.getName()));
		// the FO layer stacks its suffixes ("+nobold+noliga"); every one is stripped (found
		// on the corpus: two Sylfaen documents lost their line boxes to the double suffix)
		assertSame(PhysicalFonts.get(SANS), PhysicalFonts.get(alias.getName() + RunFontSelector.NOLIGA_SUFFIX));
		assertSame(PhysicalFonts.get(SANS), PhysicalFonts.get(alias.getName() + RunFontSelector.KERNED_SUFFIX));
		assertSame("Calibri keeps its real bold", PhysicalFonts.get(SANS), m.get("Calibri"));
		assertFalse(m.get("Calibri").isNoBoldFace());
	}
}
