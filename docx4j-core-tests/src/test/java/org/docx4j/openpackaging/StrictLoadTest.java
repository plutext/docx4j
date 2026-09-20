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
package org.docx4j.openpackaging;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.math.BigInteger;

import org.docx4j.openpackaging.packages.WordprocessingMLPackage;
import org.docx4j.wml.PPrBase;
import org.docx4j.wml.Styles;
import org.junit.Test;

/**
 * Strict OOXML (ISO 29500 strict, "purl.oclc.org" namespaces) documents as
 * Word saves them: they load, their points become the transitional integers,
 * and they round-trip.
 * <p>
 * Every strict document Word writes carries
 * {@code <w:spacing w:after="8pt" w:line="12.95pt"/>} in its docDefaults; until
 * 2026-09-21 the strict-to-transitional preprocessor
 * (org/docx4j/jaxb/mc-preprocessor.xslt) passed such values through and the
 * styles part failed to unmarshal ({@code NumberFormatException: "12.95pt"}),
 * so no strict document loaded at all.
 */
public class StrictLoadTest {

	static final String[] STRICT = {
			"strict/strict-sample-docx.docx",
			"strict/strict-chart.docx",
			"strict/strict-smartart.docx",
			"strict/strict-math.docx" };

	static WordprocessingMLPackage load(String name) throws Exception {
		InputStream is = StrictLoadTest.class.getClassLoader().getResourceAsStream(name);
		assertNotNull(name, is);
		return WordprocessingMLPackage.load(is);
	}

	@Test
	public void strictDocumentsLoadAndRoundTrip() throws Exception {
		for (String name : STRICT) {
			WordprocessingMLPackage pkg;
			try {
				pkg = load(name);
			} catch (Exception e) {
				throw new AssertionError(name + ": " + e, e);
			}
			assertTrue(name, pkg.isWasStrict());
			assertNotNull(name + " styles", pkg.getMainDocumentPart().getStyleDefinitionsPart().getContents());
			assertNotNull(name + " document", pkg.getMainDocumentPart().getContents());

			ByteArrayOutputStream baos = new ByteArrayOutputStream();
			pkg.save(baos);
			WordprocessingMLPackage again = WordprocessingMLPackage.load(new ByteArrayInputStream(baos.toByteArray()));
			assertNotNull(name + " reloaded", again.getMainDocumentPart().getContents());
		}
	}

	/** points to twips: Word's docDefaults after="8pt" line="12.95pt" are 160 and 259 */
	@Test
	public void docDefaultsSpacingInTwips() throws Exception {
		WordprocessingMLPackage pkg = load("strict/strict-smartart.docx");
		Styles styles = pkg.getMainDocumentPart().getStyleDefinitionsPart().getContents();
		PPrBase.Spacing spacing = styles.getDocDefaults().getPPrDefault().getPPr().getSpacing();
		assertEquals(BigInteger.valueOf(160), spacing.getAfter());
		assertEquals(BigInteger.valueOf(259), spacing.getLine());
	}

	/** no point value survives into the transitional parts (spacing, indents, table widths, tabs, settings) */
	@Test
	public void noPointsSurvive() throws Exception {
		java.util.regex.Pattern pt = java.util.regex.Pattern.compile("=\"[-0-9.]+pt\"");
		for (String name : STRICT) {
			WordprocessingMLPackage pkg = load(name);
			String[] xml = {
					pkg.getMainDocumentPart().getStyleDefinitionsPart().getXML(),
					pkg.getMainDocumentPart().getXML(),
					pkg.getMainDocumentPart().getDocumentSettingsPart().getXML() };
			for (String x : xml) {
				java.util.regex.Matcher m = pt.matcher(x);
				boolean found = m.find();
				assertTrue(name + ": " + (found ? m.group() : ""), !found);
			}
		}
	}

}
