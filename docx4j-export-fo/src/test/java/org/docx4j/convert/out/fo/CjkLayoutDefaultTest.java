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
package org.docx4j.convert.out.fo;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import org.docx4j.Docx4jProperties;
import org.docx4j.fonts.fop.util.FopConfigUtil;
import org.junit.After;
import org.junit.Test;

/**
 * CR-020: a CJK font keeps its OpenType layout tables exactly when the FO renderer declares
 * {@code shared-glyph-tounicode} (fork CR-006), the capability under which the per-font
 * {@code advanced="false"} workaround for the Kangxi-radical text layer is unnecessary; the
 * property overrides the renderer's answer either way.  Gated 2026-10-02 on 2.11-docx4j.2.
 */
public class CjkLayoutDefaultTest {

	private static final String PROPERTY = "docx4j.convert.out.fo.cjkAdvancedFeatures";

	@After
	public void unset() {
		Docx4jProperties.getProperties().remove(PROPERTY);
	}

	@Test
	public void defaultFollowsTheRenderersCapability() {
		Docx4jProperties.getProperties().remove(PROPERTY);
		boolean capability = FopCapabilities.has(FopCapabilities.Capability.SHARED_GLYPH_TOUNICODE);
		assertEquals("docx4j-core's own probe agrees with FopCapabilities", capability, FopConfigUtil.keepsCjkLayoutTables());
		// the renderer this module is built against (2.11-docx4j.5; the capability since .2) declares it
		if (FopCapabilities.get().isDocx4jRenderer()) assertTrue(capability);
	}

	@Test
	public void propertyOverridesEitherWay() {
		Docx4jProperties.setProperty(PROPERTY, "false");
		assertFalse(FopConfigUtil.keepsCjkLayoutTables());
		Docx4jProperties.setProperty(PROPERTY, "true");
		assertTrue(FopConfigUtil.keepsCjkLayoutTables());
	}
}
