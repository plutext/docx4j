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

import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.util.Map;
import java.util.TreeMap;

import org.docx4j.fop.wordlayout.WordLayoutElementMapping;
import org.junit.Assume;
import org.junit.Test;

/**
 * The attributes docx4j writes for the renderer's Word layout are the ones the renderer registers (CR-071 section
 * 4): docx4j's {@link WordLayoutElementMapping} and the docx4j FO renderer's
 * {@code org.apache.fop.layoutmgr.word.WordLayoutElementMapping} name the same namespace and attributes.
 */
public class WordLayoutAttributesTest {

	@Test
	public void docx4jAndTheRendererNameTheSameAttributes() throws Exception {
		Assume.assumeTrue("the renderer carries no Word layout", FopCapabilities.has(FopCapabilities.Capability.WORD_LAYOUT));
		Map<String, Object> renderer = constants(Class.forName("org.apache.fop.layoutmgr.word.WordLayoutElementMapping"));
		assertEquals(renderer, constants(WordLayoutElementMapping.class));
	}

	private static Map<String, Object> constants(Class<?> c) throws IllegalAccessException {
		Map<String, Object> m = new TreeMap<>();
		for (Field f : c.getDeclaredFields()) {      // not ElementMapping's own DEFAULT, which the renderer's inherits
			int mod = f.getModifiers();
			if (Modifier.isPublic(mod) && Modifier.isStatic(mod) && Modifier.isFinal(mod) && f.getType() == String.class)
				m.put(f.getName(), f.get(null));
		}
		return m;
	}
}
