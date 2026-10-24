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
package org.docx4j.model.properties.run;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import org.docx4j.Docx4jProperties;
import org.docx4j.openpackaging.packages.WordprocessingMLPackage;
import org.junit.Test;

/**
 * CR-061's opt-out (D1, D2): docx4j.save.normalise=false, or a package's setNormaliseOnSave(false), writes colours
 * as they were read and keeps the highlight, vertAlign and em Word repairs away (CR-060's D3); a package's setting
 * wins over the property.
 */
public class ColourNormalFormOptOutTest {

	@Test
	public void aPackageThatOptsOutKeepsWhatItRead() throws Exception {
		WordprocessingMLPackage pkg = RunPropertiesWord365Test.load("colour-hex.docx");
		pkg.setNormaliseOnSave(false);
		String xml = ColourNormalFormTest.saved(pkg, "word/document.xml");
		assertTrue(ColourNormalFormTest.run(xml, "X1_ff0000"), ColourNormalFormTest.run(xml, "X1_ff0000").contains("w:val=\"ff0000\""));
		assertTrue(ColourNormalFormTest.run(xml, "X3_F00"), ColourNormalFormTest.run(xml, "X3_F00").contains("w:val=\"F00\""));
		WordprocessingMLPackage enums = RunPropertiesWord365Test.load("props-enum-missing.docx");
		enums.setNormaliseOnSave(false);
		String e = ColourNormalFormTest.saved(enums, "word/document.xml");
		assertTrue(ColourNormalFormTest.run(e, "M1_HIGHLIGHT_BARE"), ColourNormalFormTest.run(e, "M1_HIGHLIGHT_BARE").contains("<w:highlight/>"));
	}

	@Test
	public void thePropertyOffKeepsWhatItRead() throws Exception {
		String before = Docx4jProperties.getProperty("docx4j.save.normalise");
		Docx4jProperties.setProperty("docx4j.save.normalise", "false");
		try {
			String xml = ColourNormalFormTest.saved(RunPropertiesWord365Test.load("colour-case.docx"), "word/document.xml");
			assertTrue(ColourNormalFormTest.run(xml, "K4_Red"), ColourNormalFormTest.run(xml, "K4_Red").contains("w:val=\"Red\""));
			WordprocessingMLPackage on = RunPropertiesWord365Test.load("colour-case.docx");
			on.setNormaliseOnSave(true); // the package's setting wins
			String onXml = ColourNormalFormTest.saved(on, "word/document.xml");
			assertTrue(ColourNormalFormTest.run(onXml, "K4_Red"), ColourNormalFormTest.run(onXml, "K4_Red").contains("w:val=\"FF0000\""));
		} finally {
			Docx4jProperties.setProperty("docx4j.save.normalise", before == null ? "true" : before);
		}
		String xml = ColourNormalFormTest.saved(RunPropertiesWord365Test.load("colour-case.docx"), "word/document.xml");
		assertFalse(ColourNormalFormTest.run(xml, "K4_Red"), ColourNormalFormTest.run(xml, "K4_Red").contains("w:val=\"Red\""));
	}
}
