/*
 * Copyright 2026, Plutext Pty Ltd.
 *
 * This file is part of docx4j.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.docx4j.fonts.fop.util;

import static org.junit.Assert.assertTrue;

import java.util.Collections;
import java.util.List;

import org.docx4j.Docx4jProperties;
import org.docx4j.convert.out.fopconf.Fonts;
import org.docx4j.convert.out.fopconf.Fop;
import org.docx4j.fonts.IdentityPlusMapper;
import org.docx4j.fonts.Mapper;
import org.docx4j.fonts.PhysicalFont;
import org.docx4j.fonts.PhysicalFonts;
import org.junit.Assume;
import org.junit.Test;

/**
 * The mapper's +nobold alias of a face (a family with no bold face of its own, every symbol font among
 * them) is the name the FO uses, so the FOP configuration must declare it whichever branch builds it.
 * With {@code docx4j.fonts.fop.util.FopConfigUtil.simulate-style=false} the plain branch declared only
 * the file's own name: issue #701, a document's Wingdings and Symbol bullets drawn as "#" in Times-Roman
 * ("Font Wingdings+nobold,normal,400 not found. Substituting with any"). @since 17.3.5
 */
public class FopConfigNoBoldAliasTest {

	private static final String SIMULATE = "docx4j.fonts.fop.util.FopConfigUtil.simulate-style";

	@Test
	public void theAliasIsDeclaredWithoutSimulateStyle() throws Exception {
		Mapper mapper = new IdentityPlusMapper();
		PhysicalFont regular = PhysicalFonts.get("Liberation Sans");
		if (regular == null) regular = PhysicalFonts.get("Liberation Sans Regular");
		Assume.assumeTrue("Liberation Sans on the classpath", regular != null);
		PhysicalFont alias = regular.noBoldFaceAlias();
		mapper.put("Test Light", alias);
		String was = Docx4jProperties.getProperty(SIMULATE);
		try {
			for (String simulate : new String[] { "false", "true" }) {
				Docx4jProperties.setProperty(SIMULATE, simulate);
				Fop fop = FopConfigUtil.createConfigurationObject(mapper, Collections.singleton("Test Light"));
				List<Fonts.Font> fonts = fop.getRenderers().getRenderer().get(0).getFonts().getFont();
				boolean declared = false;
				for (Fonts.Font f : fonts) {
					for (Fonts.Font.FontTriplet t : f.getFontTriplet()) {
						if (alias.getName().equals(t.getName()) && "normal".equals(t.getStyle()) && "normal".equals(t.getWeight())) {
							declared = true;
							assertTrue("on the regular file", regular.getEmbeddedURI().toString().equals(f.getEmbedUrl()));
						}
					}
				}
				assertTrue("simulate-style=" + simulate + ": (" + alias.getName() + ", normal, normal) not declared", declared);
			}
		} finally {
			if (was == null) Docx4jProperties.setProperty(SIMULATE, "true"); else Docx4jProperties.setProperty(SIMULATE, was);
		}
	}
}
