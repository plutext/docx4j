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
package org.docx4j.model.table;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import org.docx4j.wml.Tbl;
import org.docx4j.wml.Tc;
import org.docx4j.wml.Tr;
import org.junit.Test;

/**
 * CR-066 D5: a cell width beyond Word's 16-bit storage is the caller's mistake (points or EMUs passed where twips
 * are wanted, a page width whose margins are wrong), so TblFactory.createTable refuses it with the range in the
 * message, where before it wrote w:tcW w:w="70000" and Word read 4464.
 */
public class TblFactoryRangeTest {

	@Test
	public void theWidestWordCellIsAccepted() {
		Tbl tbl = TblFactory.createTable(1, 2, TblFactory.MAX_CELL_WIDTH_TWIPS);
		assertEquals(32767, tbl.getTblGrid().getGridCol().get(0).getW().intValue());
		Tc tc = (Tc) ((Tr) tbl.getContent().get(0)).getContent().get(0);
		assertEquals(32767, tc.getTcPr().getTcW().getW().intValue());
		assertEquals(0, TblFactory.createTable(1, 1, 0).getTblGrid().getGridCol().get(0).getW().intValue());
	}

	@Test
	public void aWidthBeyondWordsRangeIsRefusedWithTheRangeInTheMessage() {
		try {
			TblFactory.createTable(1, 1, 70000);
			fail("70000 twips (48.6 in) must be refused");
		} catch (IllegalArgumentException e) {
			assertTrue(e.getMessage(), e.getMessage().contains("cellWidthTwips 70000"));
			assertTrue(e.getMessage(), e.getMessage().contains("0..32767"));
			assertTrue(e.getMessage(), e.getMessage().contains("16-bit"));
		}
		try {
			TblFactory.createTable(1, 1, 32768);
			fail("32768 is Word's -32768");
		} catch (IllegalArgumentException expected) {
		}
		try {
			TblFactory.createTable(1, 1, -1);
			fail("a negative width");
		} catch (IllegalArgumentException expected) {
		}
	}
}
