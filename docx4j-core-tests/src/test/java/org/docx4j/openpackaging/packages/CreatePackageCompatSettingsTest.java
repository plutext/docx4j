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
package org.docx4j.openpackaging.packages;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;

import java.util.ArrayList;
import java.util.List;

import org.docx4j.openpackaging.parts.WordprocessingML.DocumentSettingsPart;
import org.docx4j.wml.CTCompatSetting;
import org.junit.Test;

/**
 * A package docx4j creates carries the compat settings Word 365 writes for a new
 * document, compatibilityMode 15 first, so Word opens it as a current document rather
 * than in compatibility mode (measured on Word 365's save of a docx4j-created package,
 * 2026-09-19; asked by the docx4j-core-ts acceptance run).
 */
public class CreatePackageCompatSettingsTest {

	@Test
	public void createdPackageCarriesWord365CompatSettingsInWordsOrder() throws Exception {
		WordprocessingMLPackage pkg = WordprocessingMLPackage.createPackage();
		DocumentSettingsPart dsp = pkg.getMainDocumentPart().getDocumentSettingsPart();
		assertEquals(15, dsp.getCompatibilityMode());
		List<String> names = new ArrayList<String>();
		for (CTCompatSetting cs : dsp.getContents().getCompat().getCompatSetting()) {
			assertEquals("http://schemas.microsoft.com/office/word", cs.getUri());
			names.add(cs.getName() + "=" + cs.getVal());
		}
		assertEquals(java.util.Arrays.asList(
				"compatibilityMode=15",
				"overrideTableStyleFontSizeAndJustification=1",
				"enableOpenTypeFeatures=1",
				"doNotFlipMirrorIndents=1",
				"differentiateMultirowTableHeaders=1",
				"useWord2013TrackBottomHyphenation=1"), names);
		assertNull("locale-dependent, so not written", dsp.getContents().getThemeFontLang());
	}
}
