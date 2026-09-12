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
import static org.junit.Assert.assertNull;

import org.junit.Test;

/** A theme font is chosen by the exact primary language subtag, never by a substring
 *  of some other language's (CR-016: Estonian took the Ethiopic face). */
public class LanguageTagToScriptMappingTest {

	@Test
	public void exactSubtags() {
		assertEquals("Jpan", LanguageTagToScriptMapping.getScriptForLanguageTag("ja-JP"));
		assertEquals("Hang", LanguageTagToScriptMapping.getScriptForLanguageTag("ko-KR"));
		assertEquals("Hans", LanguageTagToScriptMapping.getScriptForLanguageTag("zh-CN"));
		assertEquals("Hans", LanguageTagToScriptMapping.getScriptForLanguageTag("zh-SG"));
		assertEquals("Hant", LanguageTagToScriptMapping.getScriptForLanguageTag("zh-TW"));
		assertEquals("Hebr", LanguageTagToScriptMapping.getScriptForLanguageTag("he-IL"));
		assertEquals("Ethi", LanguageTagToScriptMapping.getScriptForLanguageTag("ti-ET"));
		assertEquals("Beng", LanguageTagToScriptMapping.getScriptForLanguageTag("bn-IN"));
		assertEquals("Deva", LanguageTagToScriptMapping.getScriptForLanguageTag("hi-IN"));
		assertEquals("Deva", LanguageTagToScriptMapping.getScriptForLanguageTag("kok"));
		assertEquals("Viet", LanguageTagToScriptMapping.getScriptForLanguageTag("vi-VN"));
		assertEquals("Geor", LanguageTagToScriptMapping.getScriptForLanguageTag("ka-GE"));
	}

	@Test
	public void noSubstringMatches() {
		assertNull("Estonian is not Ethiopic", LanguageTagToScriptMapping.getScriptForLanguageTag("et-EE"));
		assertNull("Mongolian is not Bengali", LanguageTagToScriptMapping.getScriptForLanguageTag("mn-MN"));
		assertNull("Wolof is not Ethiopic", LanguageTagToScriptMapping.getScriptForLanguageTag("wo-SN"));
		assertNull(LanguageTagToScriptMapping.getScriptForLanguageTag("en-US"));
		assertNull(LanguageTagToScriptMapping.getScriptForLanguageTag("es-ES"));
		assertNull(LanguageTagToScriptMapping.getScriptForLanguageTag("i"));
		assertNull(LanguageTagToScriptMapping.getScriptForLanguageTag(null));
	}
}
