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

import static org.junit.Assert.assertEquals;

import java.lang.reflect.Proxy;

import org.docx4j.model.properties.PropertyFactory;
import org.docx4j.wml.BooleanDefaultTrue;
import org.junit.Test;
import org.w3c.dom.css.CSSPrimitiveValue;
import org.w3c.dom.css.CSSValue;

/**
 * A CSS font-weight to w:b: bold, bolder and a number above 400 are bold; anything else isn't.
 */
public class BoldFontWeightTest {

	/** A value as a CSS parser hands it over: just its text */
	private static CSSValue weight(String text) {
		return (CSSValue) Proxy.newProxyInstance(CSSPrimitiveValue.class.getClassLoader(), new Class<?>[] { CSSPrimitiveValue.class },
				(proxy, m, args) -> {
					switch (m.getName()) {
					case "getCssText": return text;
					case "toString": return text;
					default: throw new UnsupportedOperationException(m.getName());
					}
				});
	}

	private static boolean bold(String text) {
		Bold b = (Bold) PropertyFactory.createPropertyFromCssName(Bold.CSS_NAME, weight(text));
		return ((BooleanDefaultTrue) b.getObject()).isVal();
	}

	@Test
	public void keywords() {
		assertEquals(true, bold("bold"));
		assertEquals(true, bold("BOLD"));
		assertEquals(true, bold("bolder"));
		assertEquals(false, bold("normal"));
		assertEquals(false, bold("Normal"));
		assertEquals(false, bold("lighter"));
		assertEquals(false, bold("inherit"));
	}

	@Test
	public void numbers() {
		assertEquals(false, bold("100"));
		assertEquals(false, bold("400"));
		assertEquals(true, bold("401"));
		assertEquals(true, bold("700"));
		assertEquals(true, bold("900"));
		assertEquals(false, bold("650.5")); // not an integer
	}
}
