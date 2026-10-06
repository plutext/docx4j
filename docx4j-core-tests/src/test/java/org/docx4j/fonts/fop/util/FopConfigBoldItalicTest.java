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
package org.docx4j.fonts.fop.util;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.util.Collections;
import java.util.List;

import org.docx4j.convert.out.fopconf.Fonts;
import org.docx4j.convert.out.fopconf.Fop;
import org.docx4j.fonts.IdentityPlusMapper;
import org.docx4j.fonts.Mapper;
import org.docx4j.fonts.PhysicalFont;
import org.docx4j.fonts.PhysicalFonts;
import org.junit.Assume;
import org.junit.Test;

/**
 * Bold italic in a family with an italic face but no bold one (Calibri Light) is declared on the
 * italic face where the renderer simulates styles per face (fork CR-021): Word draws it as the italic
 * face stroked at 1/35 em, unsheared (probe fonts-light-bold-italic; corpus document 2065).  Elsewhere
 * it stays on the regular face, which FOP shears and strokes: an italic face would be sheared again.
 */
public class FopConfigBoldItalicTest {

	@Test
	public void boldItalicIsOnTheItalicFaceOnlyWhereTheRendererSimulatesPerFace() throws Exception {
		Mapper mapper = new IdentityPlusMapper(); // discovers the physical fonts
		PhysicalFont regular = PhysicalFonts.get("Liberation Sans");
		if (regular == null) regular = PhysicalFonts.get("Liberation Sans Regular");
		Assume.assumeTrue("Liberation Sans on the classpath", regular != null);
		PhysicalFont italic = PhysicalFonts.getItalicForm(regular);
		Assume.assumeTrue("Liberation Sans Italic on the classpath", italic != null);
		PhysicalFont light = regular.noBoldFaceAlias(); // its italic and no bold, as Calibri Light
		mapper.put("Test Light", light);
		try {
			FopConfigUtil.rendererCapabilitiesForTest(Collections.singleton("simulate-style-per-face"));
			Fonts.Font onItalic = boldItalicEntry(mapper, light);
			assertEquals("on the italic face", italic.getEmbeddedURI().toString(), onItalic.getEmbedUrl());
			assertTrue("simulated, for FOP's stroke", onItalic.isSimulateStyle());

			FopConfigUtil.rendererCapabilitiesForTest(Collections.<String>emptySet());
			Fonts.Font onRegular = boldItalicEntry(mapper, light);
			assertEquals("on the regular face, without the capability", regular.getEmbeddedURI().toString(), onRegular.getEmbedUrl());

			assertEquals("the face TableWriter measures", italic.getEmbeddedURI(),
					rendered(mapper, light, true));
		} finally {
			FopConfigUtil.rendererCapabilitiesForTest(null);
		}
	}

	private static Object rendered(Mapper mapper, PhysicalFont light, boolean capability) {
		FopConfigUtil.rendererCapabilitiesForTest(capability ? Collections.singleton("simulate-style-per-face")
				: Collections.<String>emptySet());
		return FopConfigUtil.renderedFace(mapper, light, true, true).getEmbeddedURI();
	}

	/** The entry declaring (the alias's name, italic, bold). */
	private static Fonts.Font boldItalicEntry(Mapper mapper, PhysicalFont light) throws Exception {
		Fop fop = FopConfigUtil.createConfigurationObject(mapper, Collections.singleton("Test Light"));
		List<Fonts.Font> fonts = fop.getRenderers().getRenderer().get(0).getFonts().getFont();
		for (Fonts.Font f : fonts) {
			for (Fonts.Font.FontTriplet t : f.getFontTriplet()) {
				if (light.getName().equals(t.getName()) && "italic".equals(t.getStyle()) && "bold".equals(t.getWeight())) {
					return f;
				}
			}
		}
		assertNotNull("no (" + light.getName() + ", italic, bold) declared", null);
		return null;
	}
}
