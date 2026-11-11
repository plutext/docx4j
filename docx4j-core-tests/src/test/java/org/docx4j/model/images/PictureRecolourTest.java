/*
 * Copyright 2026, Plutext Pty Ltd.
 *
 * This file is part of docx4j.
 *
 * docx4j is licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 *
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.docx4j.model.images;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;

import org.docx4j.XmlUtils;
import org.docx4j.dml.picture.Pic;
import org.junit.Test;

/**
 * A picture's colour adjustments ({@code a:blip}'s children) aren't drawn, and are reported as
 * {@code render.effect.blip_recolour} (CR-081 D9 and phase 6; drawing them is CR-084): the first one is named.
 */
public class PictureRecolourTest {

	private static final String NS = " xmlns:pic=\"http://schemas.openxmlformats.org/drawingml/2006/picture\""
			+ " xmlns:a=\"http://schemas.openxmlformats.org/drawingml/2006/main\""
			+ " xmlns:r=\"http://schemas.openxmlformats.org/officeDocument/2006/relationships\"";

	private static Pic pic(String blipChildren) throws Exception {
		String xml = "<pic:pic" + NS + "><pic:nvPicPr><pic:cNvPr id=\"1\" name=\"p\"/><pic:cNvPicPr/></pic:nvPicPr>"
				+ "<pic:blipFill><a:blip r:embed=\"rId1\">" + blipChildren + "</a:blip><a:stretch><a:fillRect/></a:stretch></pic:blipFill>"
				+ "<pic:spPr><a:xfrm><a:off x=\"0\" y=\"0\"/><a:ext cx=\"914400\" cy=\"914400\"/></a:xfrm>"
				+ "<a:prstGeom prst=\"rect\"><a:avLst/></a:prstGeom></pic:spPr></pic:pic>";
		return (Pic) XmlUtils.unwrap(XmlUtils.unmarshalString(xml));
	}

	@Test
	public void aPictureWithoutAdjustmentsIsNotReported() throws Exception {
		assertNull(PictureEffects.recoloured(pic("")));
		assertNull(PictureEffects.recoloured(null));
	}

	@Test
	public void theFirstAdjustmentIsNamed() throws Exception {
		assertEquals("grayscl", PictureEffects.recoloured(pic("<a:grayscl/>")));
		assertEquals("alphaModFix", PictureEffects.recoloured(pic("<a:alphaModFix amt=\"50000\"/><a:lum bright=\"20000\"/>")));
		assertEquals("duotone", PictureEffects.recoloured(pic("<a:duotone><a:prstClr val=\"black\"/><a:srgbClr val=\"FFFFFF\"/></a:duotone>")));
	}
}
