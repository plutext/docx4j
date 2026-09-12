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

import org.docx4j.XmlUtils;
import org.docx4j.openpackaging.packages.WordprocessingMLPackage;
import org.docx4j.wml.RPr;
import org.junit.Test;

/**
 * {@link RunFontSelector#documentFontFor}: the document font the selector picks for one
 * character, answered without a visitor (CR-016 phase 4; for docx4j-docx-anon, which
 * until 17.1.1 ran the selector in the DISCOVERY mode for this).
 */
public class RunFontSelectorDocumentFontForTest {

	private static final String W = FontsTestSupport.W;

	private static RunFontSelector selector() throws Exception {
		WordprocessingMLPackage pkg = FontsTestSupport.packageWith(
				FontsTestSupport.styles("<w:rFonts w:ascii=\"Liberation Serif\" w:hAnsi=\"Liberation Serif\" w:eastAsia=\"Noto Sans CJK JP\" w:cs=\"Liberation Sans\"/>", null),
				null, null, FontsTestSupport.p(null, "x"));
		return FontsTestSupport.xslFoSelector(pkg);
	}

	private static RPr rPr(String inner) throws Exception {
		return (RPr) XmlUtils.unwrap(XmlUtils.unmarshalString("<w:rPr xmlns:w=\"" + W + "\">" + inner + "</w:rPr>"));
	}

	@Test
	public void theRangeTable() throws Exception {
		RunFontSelector rfs = selector();
		assertEquals("Liberation Serif", rfs.documentFontFor(null, null, 'a'));
		assertEquals("Noto Sans CJK JP", rfs.documentFontFor(null, null, 0x65E5)); // 日
	}

	@Test
	public void theComplexScriptFontWhenCsIsOn() throws Exception {
		RunFontSelector rfs = selector();
		assertEquals("Liberation Sans", rfs.documentFontFor(null, rPr("<w:cs/>"), 'a'));
		assertEquals("cs off by value", "Liberation Serif", rfs.documentFontFor(null, rPr("<w:cs w:val=\"0\"/>"), 'a'));
		// the reference resolves to nothing (no theme part, no cs face) and there is no w:cs: by range
		assertEquals("Liberation Serif", rfs.documentFontFor(null, rPr("<w:rFonts w:cstheme=\"minorBidi\" w:cs=\"\"/><w:cs/>"), 'a'));
	}

	@Test
	public void theSymbolFontByItsCanonicalName() throws Exception {
		RunFontSelector rfs = selector();
		assertEquals("Wingdings", rfs.documentFontFor(null, rPr("<w:rFonts w:ascii=\"wingdings\" w:hAnsi=\"wingdings\"/>"), 'a'));
	}

	@Test
	public void thePreambleRule() throws Exception {
		RunFontSelector rfs = selector();
		RPr tnr = rPr("<w:rFonts w:ascii=\"Arial\" w:hAnsi=\"Arial\" w:eastAsia=\"Times New Roman\"/>");
		assertEquals("Arial", rfs.documentFontFor(null, tnr, 'a'));
		assertEquals("the whole run, East Asian text included", "Arial", rfs.documentFontFor(null, tnr, 0x65E5));
	}
}
