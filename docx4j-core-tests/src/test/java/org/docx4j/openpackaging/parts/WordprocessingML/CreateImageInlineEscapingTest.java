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
package org.docx4j.openpackaging.parts.WordprocessingML;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;

import java.util.Base64;

import org.docx4j.XmlUtils;
import org.docx4j.dml.wordprocessingDrawing.Inline;
import org.docx4j.openpackaging.packages.WordprocessingMLPackage;
import org.junit.Ignore;
import org.junit.Test;

/**
 * CR-057 phase 0: {@code createImageInline} takes alt text and a filename hint as any text (its Javadoc
 * says so) and must put them in the document as given. Measured 2026-10-23 on 17.3.4 (from
 * docx4j-ImportXHTML's CR-005): an ampersand fails the unmarshal, a quote injects an attribute. Red until
 * phase 1.
 */
public class CreateImageInlineEscapingTest {

	private static final byte[] PNG = Base64.getDecoder().decode(
			"iVBORw0KGgoAAAANSUhEUgAAAAEAAAABCAYAAAAfFcSJAAAADUlEQVR42mP8z8BQDwAEhQGAhKmMIQAAAABJRU5ErkJggg==");

	static Inline inline(String filenameHint, String altText) throws Exception {
		WordprocessingMLPackage pkg = WordprocessingMLPackage.createPackage();
		BinaryPartAbstractImage part = BinaryPartAbstractImage.createImagePart(pkg, pkg.getMainDocumentPart(), PNG);
		return part.createImageInline(filenameHint, altText, 1, 2, false);
	}

	@Test
	public void plainTextRoundTrips() throws Exception {
		Inline inline = inline("plain.png", "plain");
		assertEquals("plain", inline.getDocPr().getDescr());
		assertEquals("plain.png", inline.getDocPr().getName());
	}

	@Ignore("CR-057 phase 0: red until phase 1 (escaping in createImageInline)")
	@Test
	public void ampersandAndAngleBracketsRoundTrip() throws Exception {
		Inline inline = inline("P&L <2026>.png", "Profit & Loss, a < b");
		assertEquals("Profit & Loss, a < b", inline.getDocPr().getDescr());
		assertEquals("P&L <2026>.png", inline.getDocPr().getName());
		assertEquals("P&L <2026>.png", inline.getGraphic().getGraphicData().getPic().getNvPicPr().getCNvPr().getName());
	}

	@Ignore("CR-057 phase 0: red until phase 1 (escaping in createImageInline)")
	@Test
	public void quoteInjectsNoAttribute() throws Exception {
		Inline inline = inline("f.png", "x\" title=\"INJECTED");
		assertEquals("x\" title=\"INJECTED", inline.getDocPr().getDescr());
		String xml = XmlUtils.marshaltoString(inline, true, false, org.docx4j.jaxb.Context.jc,
				"http://schemas.openxmlformats.org/drawingml/2006/wordprocessingDrawing", "inline", Inline.class);
		assertFalse("an attribute injected\n" + xml, xml.contains("title=\"INJECTED\""));
	}
}
