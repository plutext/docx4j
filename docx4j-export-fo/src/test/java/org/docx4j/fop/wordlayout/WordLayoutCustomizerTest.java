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
package org.docx4j.fop.wordlayout;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import java.util.Map;

import org.docx4j.Docx4jProperties;
import org.junit.Test;

/** What docx4j hands the renderer's Word layout (CR-071 D3, D4). */
public class WordLayoutCustomizerTest {

	private static final String PENALTY = WordLayoutCustomizer.PROPERTY + ".keepChainPenalty";

	/** Every docx4j.convert.out.fo.wordLayout.* setting by its full name, a system property over docx4j.properties. */
	@Test
	public void settingsAreTheWordLayoutPropertiesSystemFirst() {
		String was = Docx4jProperties.getProperty(PENALTY);
		Docx4jProperties.setProperty(PENALTY, "800");
		System.setProperty(WordLayoutCustomizer.MAX_SPACE_SHRINK, "0.2");
		try {
			Map<String, String> m = WordLayoutCustomizer.settings();
			assertEquals("800", m.get(PENALTY));
			assertEquals("0.2", m.get(WordLayoutCustomizer.MAX_SPACE_SHRINK));
			System.setProperty(PENALTY, "700");
			assertEquals("the system property wins", "700", WordLayoutCustomizer.settings().get(PENALTY));
			assertFalse("the switch stays docx4j's", m.containsKey(WordLayoutCustomizer.PROPERTY));
			for (String k : m.keySet()) assertTrue(k, k.startsWith(WordLayoutCustomizer.PROPERTY + "."));
		} finally {
			System.clearProperty(WordLayoutCustomizer.MAX_SPACE_SHRINK);
			System.clearProperty(PENALTY);
			if (was == null) Docx4jProperties.getProperties().remove(PENALTY);
			else Docx4jProperties.setProperty(PENALTY, was);
		}
	}

	/** With neither a physical font nor a document font there is no Word metric, and the renderer takes the block's. */
	@Test
	public void ascentShareUnknownWithoutAFont() {
		assertNull(WordLayoutCustomizer.ASCENT_SHARE.apply(null, "No Such Font Anywhere"));
	}
}
