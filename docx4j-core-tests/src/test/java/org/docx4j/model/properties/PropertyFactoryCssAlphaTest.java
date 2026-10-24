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
package org.docx4j.model.properties;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import java.lang.reflect.Proxy;

import org.docx4j.model.properties.paragraph.PShading;
import org.docx4j.model.properties.run.RShading;
import org.docx4j.wml.CTShd;
import org.junit.Test;
import org.w3c.dom.css.CSSPrimitiveValue;
import org.w3c.dom.css.CSSValue;
import org.w3c.dom.css.RGBColor;

/**
 * A CSS background colour's opacity (ImportXHTML CR-006's finding, 2026-10-24): a CSS parser that reads rgba()
 * (openhtmltopdf from 1.1.100) gives docx4j the red, green and blue alone, so rgba(0,0,0,0), which browsers write for
 * transparent, became a black fill. Alpha 0 is now no fill, as "transparent" is; a partial alpha is blended over white.
 */
public class PropertyFactoryCssAlphaTest {

	/**
	 * A run's background as w:shd, not as a w:highlight (the test docx4j.properties turns highlights on; PropertyFactory
	 * reads the setting once, and surefire forks a JVM for each test class). In highlight mode an alpha of 0 is no
	 * highlight too; a blended colour is a highlight only where it is one of ST_HighlightColor's, as before.
	 */
	@org.junit.BeforeClass
	public static void runBackgroundsAsShading() {
		org.docx4j.Docx4jProperties.setProperty(
				"docx4j.model.properties.PropertyFactory.createPropertyFromCssName.background-color.useHighlightInRPr", "false");
	}

	/** A colour as a CSS parser hands it over: its text, and its red, green and blue (the W3C RGBColor has no alpha) */
	private static CSSValue colour(String text, int r, int g, int b) {
		RGBColor rgb = (RGBColor) Proxy.newProxyInstance(RGBColor.class.getClassLoader(), new Class<?>[] { RGBColor.class },
				(proxy, m, args) -> {
					switch (m.getName()) {
					case "getRed": return component(r);
					case "getGreen": return component(g);
					case "getBlue": return component(b);
					default: throw new UnsupportedOperationException(m.getName());
					}
				});
		return (CSSValue) Proxy.newProxyInstance(CSSPrimitiveValue.class.getClassLoader(), new Class<?>[] { CSSPrimitiveValue.class },
				(proxy, m, args) -> {
					switch (m.getName()) {
					case "getCssText": return text;
					case "getCssValueType": return CSSValue.CSS_PRIMITIVE_VALUE;
					case "getPrimitiveType": return CSSPrimitiveValue.CSS_RGBCOLOR;
					case "getRGBColorValue": return rgb;
					case "toString": return text;
					default: throw new UnsupportedOperationException(m.getName());
					}
				});
	}

	private static CSSPrimitiveValue component(int v) {
		return (CSSPrimitiveValue) Proxy.newProxyInstance(CSSPrimitiveValue.class.getClassLoader(), new Class<?>[] { CSSPrimitiveValue.class },
				(proxy, m, args) -> {
					if (m.getName().equals("getFloatValue")) return (float) v;
					throw new UnsupportedOperationException(m.getName());
				});
	}

	@Test
	public void theOpacityACssColourStates() {
		assertEquals(0, PropertyFactory.cssAlpha("rgba(0, 0, 0, 0)"), 1e-9);
		assertEquals(0.4, PropertyFactory.cssAlpha("rgba(255,255,0,0.4)"), 1e-9);
		assertEquals(0.4, PropertyFactory.cssAlpha("rgb(255 255 0 / 40%)"), 1e-9);
		assertEquals(0, PropertyFactory.cssAlpha("HSLA(60, 100%, 50%, 0)"), 1e-9);
		assertEquals(0x66 / 255.0, PropertyFactory.cssAlpha("#FFFF0066"), 1e-9);
		assertEquals(1, PropertyFactory.cssAlpha("#ff0"), 1e-9);
		assertEquals(1, PropertyFactory.cssAlpha("rgb(1, 2, 3)"), 1e-9);
		assertEquals(1, PropertyFactory.cssAlpha("yellow"), 1e-9);
		assertEquals("FFFF99", PropertyFactory.overWhite("FFFF00", 0.4));
	}

	@Test
	public void aRunsTransparentBackgroundIsNoFill() {
		assertNull(PropertyFactory.createPropertyFromCssName(RShading.CSS_NAME, colour("rgba(0, 0, 0, 0)", 0, 0, 0)));
		Property partial = PropertyFactory.createPropertyFromCssName(RShading.CSS_NAME, colour("rgba(255, 255, 0, 0.4)", 255, 255, 0));
		assertTrue(String.valueOf(partial), partial instanceof RShading);
		assertEquals("FFFF99", ((CTShd) partial.getObject()).getFill());
		Property opaque = PropertyFactory.createPropertyFromCssName(RShading.CSS_NAME, colour("rgb(255, 255, 0)", 255, 255, 0));
		assertEquals("FFFF00", ((CTShd) opaque.getObject()).getFill().toUpperCase());
	}

	@Test
	public void aParagraphsTransparentBackgroundIsNoFill() {
		assertNull(PropertyFactory.createPropertyFromCssNameForPPr(PShading.CSS_NAME, colour("rgba(0, 0, 0, 0)", 0, 0, 0)));
		Property partial = PropertyFactory.createPropertyFromCssNameForPPr(PShading.CSS_NAME, colour("rgba(255, 255, 0, 0.4)", 255, 255, 0));
		assertEquals("FFFF99", ((CTShd) partial.getObject()).getFill());
	}
}
