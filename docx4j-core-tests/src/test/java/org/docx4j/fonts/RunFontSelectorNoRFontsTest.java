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

import static org.docx4j.fonts.FontsTestSupport.*;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;

import java.util.Arrays;
import java.util.List;

import org.docx4j.openpackaging.packages.WordprocessingMLPackage;
import org.junit.Test;

/**
 * No w:rFonts anywhere: Times New Roman, Word's built-in default (CR-016 probe
 * fonts-missing-slots (a)), and the text still goes through the range dispatch (so a
 * non-Latin stretch gets its own span and the coverage pass), where until 17.1.1 the
 * whole text was set in the default font with no dispatch at all.
 */
public class RunFontSelectorNoRFontsTest {

	@Test
	public void timesNewRomanThroughTheDispatch() throws Exception {
		WordprocessingMLPackage pkg = packageWith(
				styles("<w:sz w:val=\"22\"/>", null), null, null,
				p(null, "Hello") + p(null, "Hello 日本 world"),
				"Times New Roman", SERIF);
		assertEquals(Arrays.asList(SERIF), families(pkg, 0));
		List<String> mixed = families(pkg, 1);
		assertFalse(mixed.isEmpty());
		assertEquals("the Latin stretch", SERIF, mixed.get(0));
		assertEquals("Times New Roman", xslFoSelector(pkg).getDefaultFont());
	}
}
