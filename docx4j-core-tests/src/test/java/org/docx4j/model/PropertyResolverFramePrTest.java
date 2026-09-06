/*
   Licensed to Plutext Pty Ltd under one or more contributor license agreements.

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
package org.docx4j.model;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

import org.docx4j.XmlUtils;
import org.docx4j.openpackaging.packages.WordprocessingMLPackage;
import org.docx4j.wml.PPr;
import org.docx4j.wml.STHAnchor;
import org.docx4j.wml.STVAnchor;
import org.docx4j.wml.Styles;
import org.junit.Test;

/**
 * {@code w:pPr/w:framePr} (a Word text frame) through the property resolver.
 *
 * <p>Word inherits a frame's attributes one at a time: a paragraph whose own
 * {@code w:framePr} states only a width keeps the anchors and the position its style
 * states.  Measured on a Word letterhead whose "Adresse" style carries
 * {@code w:framePr w:w="3629" w:vAnchor="page" w:hAnchor="page" w:x="1362" w:y="2042"}
 * and whose paragraphs carry only {@code w:framePr w:w="3600"}: Word draws them at
 * (68.1pt, 102.1pt) - the style's x and y - in a box 180pt (3600 twips) wide.</p>
 *
 * @since 17.0.6
 */
public class PropertyResolverFramePrTest {

	private static final String STYLES =
			"<w:styles xmlns:w=\"http://schemas.openxmlformats.org/wordprocessingml/2006/main\">"
			+ "<w:style w:type=\"paragraph\" w:default=\"1\" w:styleId=\"Normal\">"
			+   "<w:name w:val=\"Normal\"/>"
			+ "</w:style>"
			+ "<w:style w:type=\"paragraph\" w:styleId=\"Adresse\">"
			+   "<w:name w:val=\"Adresse\"/><w:basedOn w:val=\"Normal\"/>"
			+   "<w:pPr><w:framePr w:w=\"3629\" w:h=\"2427\" w:wrap=\"notBeside\""
			+       " w:vAnchor=\"page\" w:hAnchor=\"page\" w:x=\"1362\" w:y=\"2042\"/></w:pPr>"
			+ "</w:style>"
			+ "</w:styles>";

	private static PPr effective(String pPrXml) throws Exception {
		Styles styles = (Styles) XmlUtils.unmarshalString(STYLES);
		WordprocessingMLPackage pkg = PropertyResolverTestUtils.createdPkgWithStyles(styles);
		PPr pPr = (PPr) XmlUtils.unmarshalString(pPrXml);
		return new PropertyResolver(pkg).getEffectivePPr(pPr);
	}

	/** A paragraph whose only direct formatting is the frame: hasDirectPPrFormatting has
	 *  to see it, or applyPPr is never called and the frame is lost. */
	@Test
	public void frameIsDirectFormatting() throws Exception {
		PPr effective = effective(
				"<w:pPr xmlns:w=\"http://schemas.openxmlformats.org/wordprocessingml/2006/main\">"
				+ "<w:framePr w:w=\"2926\" w:h=\"748\" w:hRule=\"exact\" w:wrap=\"notBeside\""
				+ " w:vAnchor=\"page\" w:hAnchor=\"page\" w:x=\"8563\" w:y=\"1702\"/>"
				+ "</w:pPr>");
		assertNotNull("w:framePr survived getEffectivePPr", effective.getFramePr());
		assertEquals(8563, effective.getFramePr().getX().intValue());
		assertEquals(1702, effective.getFramePr().getY().intValue());
		assertEquals(2926, effective.getFramePr().getW().intValue());
		assertEquals(STVAnchor.PAGE, effective.getFramePr().getVAnchor());
	}

	/** The paragraph's width, the style's anchors and position. */
	@Test
	public void frameAttributesInheritSingly() throws Exception {
		PPr effective = effective(
				"<w:pPr xmlns:w=\"http://schemas.openxmlformats.org/wordprocessingml/2006/main\">"
				+ "<w:pStyle w:val=\"Adresse\"/>"
				+ "<w:framePr w:w=\"3600\" w:wrap=\"notBeside\"/>"
				+ "</w:pPr>");
		assertNotNull(effective.getFramePr());
		assertEquals("the paragraph's own width", 3600, effective.getFramePr().getW().intValue());
		assertEquals("the style's x", 1362, effective.getFramePr().getX().intValue());
		assertEquals("the style's y", 2042, effective.getFramePr().getY().intValue());
		assertEquals(STVAnchor.PAGE, effective.getFramePr().getVAnchor());
		assertEquals(STHAnchor.PAGE, effective.getFramePr().getHAnchor());
	}

	/** A style's frame applies to every paragraph of that style. */
	@Test
	public void frameFromTheStyleAlone() throws Exception {
		PPr effective = effective(
				"<w:pPr xmlns:w=\"http://schemas.openxmlformats.org/wordprocessingml/2006/main\">"
				+ "<w:pStyle w:val=\"Adresse\"/>"
				+ "</w:pPr>");
		assertNotNull(effective.getFramePr());
		assertEquals(3629, effective.getFramePr().getW().intValue());
		assertEquals(STHAnchor.PAGE, effective.getFramePr().getHAnchor());
	}
}
