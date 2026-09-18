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
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.docx4j.convert.out.fopconf.Fonts;
import org.docx4j.fonts.IdentityPlusMapper;
import org.docx4j.fonts.Mapper;
import org.docx4j.fonts.PhysicalFont;
import org.docx4j.fonts.PhysicalFonts;
import org.docx4j.fonts.RunFontSelector;
import org.docx4j.convert.out.fopconf.Fop;
import org.junit.Assume;
import org.junit.Test;

/**
 * A face declares its typographic family name to FOP only where the family is its own.
 *
 * <p>Aptos Display's typographic family (name id 16) is "Aptos", so declared under it its
 * regular face is the triplet {@code Aptos, normal, 400} - the one Aptos Regular declares -
 * and FOP keeps whichever it was given last.  Measured: with both installed, a document
 * asking for Aptos was drawn in Aptos Display, and its every line broke short of Word's.</p>
 */
public class FopConfigFamilyTripletTest {

	@Test
	public void styleWordsAreStrippedAndOpticalNamesAreNot() {
		assertEquals("Aptos", FopConfigUtil.styleStripped("Aptos Bold Italic"));
		assertEquals("Carlito", FopConfigUtil.styleStripped("Carlito Regular"));
		assertEquals("Liberation Serif", FopConfigUtil.styleStripped("Liberation Serif Bold"));
		assertEquals("Aptos Display", FopConfigUtil.styleStripped("Aptos Display"));
		assertEquals("Aptos Display", FopConfigUtil.styleStripped("Aptos Display Bold"));
		assertEquals("a named weight is a face of its own", "Aptos Light", FopConfigUtil.styleStripped("Aptos Light"));
		assertEquals("Aptos", FopConfigUtil.styleStripped("Aptos+noliga".replace(RunFontSelector.NOLIGA_SUFFIX, "")));
	}

	@Test
	public void aFaceAnswersForItsOwnFamilyAndAVariantDoesNotWhereTheFamilyHasAFace() {
		new IdentityPlusMapper(); // discovers the physical fonts
		PhysicalFont sans = firstOf("Liberation Sans");
		PhysicalFont serif = firstOf("Liberation Serif");
		Assume.assumeTrue("Liberation Sans and Serif on the classpath", sans != null && serif != null);
		List<PhysicalFont> all = new ArrayList<PhysicalFont>(PhysicalFonts.getPhysicalFonts().values());
		// its own family: Liberation Sans Regular / Bold for "Liberation Sans"
		assertTrue(FopConfigUtil.familyIsThisFaces("Liberation Sans", sans, all));
		PhysicalFont sansBold = PhysicalFonts.get("Liberation Sans Bold");
		if (sansBold != null) assertTrue(FopConfigUtil.familyIsThisFaces("Liberation Sans", sansBold, all));
		// a variant whose id 16 named another family which has faces of its own: Aptos Display's shape
		assertFalse(FopConfigUtil.familyIsThisFaces("Liberation Serif", sans, all));
		// ... but where that family has no face at all, the variant may stand in for it
		List<PhysicalFont> withoutSerif = new ArrayList<PhysicalFont>();
		for (PhysicalFont pf : all) {
			if (!FopConfigUtil.styleStripped(pf.getName()).equalsIgnoreCase("Liberation Serif")) withoutSerif.add(pf);
		}
		assertTrue(FopConfigUtil.familyIsThisFaces("Liberation Serif", sans, withoutSerif));
	}

	/** Over every font this machine has: no configured face names, as its family, the
	 *  own name of another configured face. */
	@Test
	public void noFaceIsDeclaredUnderAnotherFacesFamily() throws Exception {
		Mapper mapper = new IdentityPlusMapper();
		Assume.assumeTrue("no physical fonts on this machine", !PhysicalFonts.getPhysicalFonts().isEmpty());
		Set<String> inUse = new HashSet<String>();
		for (PhysicalFont pf : PhysicalFonts.getPhysicalFonts().values()) {
			mapper.put(pf.getName(), pf);
			inUse.add(pf.getName());
		}
		Fop fop = FopConfigUtil.createConfigurationObject(mapper, inUse);
		// each entry's own name is that of the face in its file (a bold variant entry is
		// declared under its base face's name, so the first triplet does not say)
		Map<String, String> ownNameByUrl = new HashMap<String, String>();
		Set<String> ownNames = new HashSet<String>();
		for (PhysicalFont pf : PhysicalFonts.getPhysicalFonts().values()) {
			if (pf.getEmbeddedURI() == null) continue;
			String own = FopConfigUtil.styleStripped(PhysicalFonts.stripSuffixes(pf.getName()));
			ownNameByUrl.put(pf.getEmbeddedURI().toString(), own);
			ownNames.add(own.toLowerCase());
		}
		List<Fonts.Font> fonts = fop.getRenderers().getRenderer().get(0).getFonts().getFont();
		int families = 0;
		for (Fonts.Font f : fonts) {
			if (f.getFontTriplet().isEmpty()) continue;
			String own = ownNameByUrl.get(f.getEmbedUrl());
			if (own == null) continue;
			for (Fonts.Font.FontTriplet t : f.getFontTriplet()) {
				// the theft is of the family's REGULAR triplet; a bold or italic variant
				// declared under its base face's name is that face's own variant
				if (!"normal".equals(t.getStyle()) || !("normal".equals(t.getWeight()) || "400".equals(t.getWeight()))) continue;
				String family = FopConfigUtil.styleStripped(PhysicalFonts.stripSuffixes(t.getName()));
				if (family.equalsIgnoreCase(own)) continue;
				families++;
				assertFalse(f.getEmbedUrl().replaceAll(".*/", "") + " (" + own + ") is declared under " + t.getName()
						+ ", which is another face's own family", ownNames.contains(family.toLowerCase()));
			}
		}
		System.out.println("family triplets checked: " + families + " over " + fonts.size() + " entries");
	}

	private static PhysicalFont firstOf(String family) {
		PhysicalFont pf = PhysicalFonts.get(family);
		if (pf != null) return pf;
		pf = PhysicalFonts.get(family + " Regular");
		return pf;
	}
}
