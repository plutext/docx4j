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
package org.docx4j.model.styles;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import org.docx4j.XmlUtils;
import org.docx4j.jaxb.Context;
import org.docx4j.wml.CTFramePr;
import org.docx4j.wml.FramePrAccess;
import org.junit.Test;

/**
 * {@code w:framePr/@w:anchorLock} in style resolution, after CR-059 made an absent one read false: a
 * {@code w:framePr} with nothing set is empty, and one that omits {@code anchorLock} leaves the style's value
 * alone. Found by docx4j-python reading ac97b56c5 (2026-10-24): {@code isEmpty} and {@code apply} had called
 * {@code isAnchorLock()}, which the schema's old default made true for an absent attribute.
 */
public class StyleUtilFramePrAnchorLockTest {

	private static CTFramePr framePr(String attributes) throws Exception {
		String xml = "<w:framePr xmlns:w=\"http://schemas.openxmlformats.org/wordprocessingml/2006/main\" " + attributes + "/>";
		org.w3c.dom.Document d = XmlUtils.getNewDocumentBuilder().parse(new java.io.ByteArrayInputStream(xml.getBytes("UTF-8")));
		return (CTFramePr) XmlUtils.unmarshal(d.getDocumentElement(), Context.jc, CTFramePr.class);
	}

	@Test
	public void aFramePrWithNothingSetIsEmpty() throws Exception {
		assertTrue(StyleUtil.isEmpty(framePr("")));
		assertFalse(StyleUtil.isEmpty(framePr("w:anchorLock=\"1\"")));
		assertFalse(StyleUtil.isEmpty(framePr("w:anchorLock=\"0\"")));
		assertFalse(StyleUtil.isEmpty(framePr("w:w=\"3600\"")));
	}

	@Test
	public void anOmittedAnchorLockKeepsTheStyles() throws Exception {
		CTFramePr merged = StyleUtil.apply(framePr("w:w=\"3600\""), framePr("w:w=\"3629\" w:anchorLock=\"1\""));
		assertEquals(Boolean.TRUE, FramePrAccess.getAnchorLock(merged));
		assertEquals(3600, merged.getW().intValue());
	}

	@Test
	public void aStatedAnchorLockWins() throws Exception {
		assertEquals(Boolean.FALSE, FramePrAccess.getAnchorLock(StyleUtil.apply(framePr("w:w=\"1\" w:anchorLock=\"0\""), framePr("w:anchorLock=\"1\""))));
		assertEquals(Boolean.TRUE, FramePrAccess.getAnchorLock(StyleUtil.apply(framePr("w:w=\"1\" w:anchorLock=\"1\""), framePr("w:w=\"2\""))));
	}

	@Test
	public void neitherStatesItAndNoneIsWritten() throws Exception {
		assertNull(FramePrAccess.getAnchorLock(StyleUtil.apply(framePr("w:w=\"1\""), framePr("w:w=\"2\""))));
	}
}
