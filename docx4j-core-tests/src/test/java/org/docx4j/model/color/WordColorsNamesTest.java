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
package org.docx4j.model.color;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;

import org.docx4j.wml.CTBorder;
import org.docx4j.wml.CTShd;
import org.docx4j.wml.Color;
import org.docx4j.wml.U;
import org.junit.Test;

/**
 * WordColors resolves every colour attribute as Word reads it (CR-060 rounds 3 to 7): a classic HTML name or any hex
 * spelling Word accepts, the run's and the underline's colour, a shading's fill and pattern colour, a border's colour
 * alike; a value Word rejects is automatic (null). Found by docx4j-core-ts: the fill, pattern and border resolved
 * only six hex digits.
 */
public class WordColorsNamesTest {

	@Test
	public void namesAndHexSpellingsInEveryColour() {
		Color c = new Color();
		c.setVal("red");
		assertEquals("FF0000", WordColors.of(c, null).toHex());
		U u = new U();
		u.setColor("Navy");
		assertEquals("000080", WordColors.of(u, null).toHex());
		CTShd shd = new CTShd();
		shd.setFill("Aqua");
		shd.setColor(" #ff0000 ");
		assertEquals("00FFFF", WordColors.fill(shd, null).toHex());
		assertEquals("FF0000", WordColors.pattern(shd, null).toHex());
		CTBorder border = new CTBorder();
		border.setColor("navy");
		assertEquals("000080", WordColors.of(border, null).toHex());
	}

	@Test
	public void aRejectedValueIsAutomatic() {
		CTShd shd = new CTShd();
		shd.setFill("F00");
		assertNull(WordColors.fill(shd, null));
		CTBorder border = new CTBorder();
		border.setColor("bogus");
		assertNull(WordColors.of(border, null));
		shd.setFill("auto");
		assertNull(WordColors.fill(shd, null));
	}
}
