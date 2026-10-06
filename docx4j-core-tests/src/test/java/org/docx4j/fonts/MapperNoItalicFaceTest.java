/*
   Copyright 2026, Plutext Pty Ltd.

   This file is part of docx4j.

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
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import java.util.Arrays;
import java.util.HashSet;
import java.util.List;

import org.docx4j.convert.out.fopconf.Fonts;
import org.docx4j.convert.out.fopconf.Fop;
import org.docx4j.fonts.fop.util.FopConfigUtil;
import org.junit.Assume;
import org.junit.Test;

/**
 * A document font Word has no italic face for (Aptos Light) is sheared for italic, as Word draws
 * it, rather than set in its substitute family's real italic: probes fonts-light-bold-italic and
 * fonts-aptos-light-italic (Word's italic is Aptos-Light sheared 0.3333 and wraps as its upright
 * text; the golden VM's cloud fonts hold Aptos Light upright only).  Liberation Sans, which has an
 * italic face, stands in for the clone Akasia Light here.
 */
public class MapperNoItalicFaceTest {

	@Test
	public void aptosLightReportsNoItalicFaceAndOtherFontsKeepTheirs() throws Exception {
		Mapper mapper = new IdentityPlusMapper();
		PhysicalFont sans = PhysicalFonts.get("Liberation Sans");
		if (sans == null) sans = PhysicalFonts.get("Liberation Sans Regular");
		Assume.assumeTrue("Liberation Sans on the classpath", sans != null);
		Assume.assumeTrue("Liberation Sans Italic on the classpath", PhysicalFonts.getItalicForm(sans) != null);
		mapper.put("aptos light", sans);
		mapper.put("some other light", sans);

		mapper.addNoItalicFaceAliases(new HashSet<String>(Arrays.asList("Aptos Light", "Some Other Light")));

		PhysicalFont alias = mapper.get("Aptos Light");
		assertTrue("Aptos Light is aliased", alias.isNoItalicFace());
		assertNull("no italic face", mapper.getItalicForm("Aptos Light", alias));
		assertNull("no bold italic face", mapper.getBoldItalicForm("Aptos Light", alias));
		assertEquals("the same file", sans.getEmbeddedURI(), alias.getEmbeddedURI());
		assertEquals("found again under its own name", sans, PhysicalFonts.get(alias.getName()));
		assertNotNull("a font not on the measured list keeps its italic",
				mapper.getItalicForm("Some Other Light", mapper.get("Some Other Light")));

		// and FOP is told to shear the regular file for it
		Fop fop = FopConfigUtil.createConfigurationObject(mapper, new HashSet<String>(Arrays.asList("Aptos Light")));
		List<Fonts.Font> fonts = fop.getRenderers().getRenderer().get(0).getFonts().getFont();
		Fonts.Font italic = null;
		for (Fonts.Font f : fonts) {
			for (Fonts.Font.FontTriplet t : f.getFontTriplet()) {
				if (alias.getName().equals(t.getName()) && "italic".equals(t.getStyle()) && "normal".equals(t.getWeight())) italic = f;
			}
		}
		assertNotNull("(" + alias.getName() + ", italic, normal) declared", italic);
		assertEquals("on the regular file", sans.getEmbeddedURI().toString(), italic.getEmbedUrl());
		assertTrue("simulated: FOP shears it", italic.isSimulateStyle());
	}

	@Test
	public void theNoBoldAliasWrappedKeepsBothFlags() {
		new IdentityPlusMapper();
		PhysicalFont sans = PhysicalFonts.get("Liberation Sans");
		if (sans == null) sans = PhysicalFonts.get("Liberation Sans Regular");
		Assume.assumeTrue("Liberation Sans on the classpath", sans != null);
		PhysicalFont both = sans.noBoldFaceAlias().noItalicFaceAlias();
		assertTrue(both.isNoBoldFace());
		assertTrue(both.isNoItalicFace());
		assertNull(PhysicalFonts.getBoldForm(both));
		assertNull(PhysicalFonts.getItalicForm(both));
		assertEquals(sans, PhysicalFonts.get(both.getName()));
	}
}
