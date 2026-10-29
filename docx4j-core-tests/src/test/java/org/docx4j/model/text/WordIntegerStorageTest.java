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
package org.docx4j.model.text;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import java.math.BigInteger;
import java.util.List;
import java.util.Map;

import javax.xml.parsers.DocumentBuilderFactory;

import org.junit.Test;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

/**
 * {@code word-integer-storage.tsv} against Word's saves (CR-066 sections 3.1 and 3.4, rounds 5 and 6 of the
 * probe, Word 365, 2026-10-26): for each measured attribute, the written value and what Word's save held, so the
 * table is tied to the evidence and not to the CR's prose. A row of the form {@code kept} is a value inside the
 * range (no finding).
 */
public class WordIntegerStorageTest {

	private static final String W = "http://schemas.openxmlformats.org/wordprocessingml/2006/main";
	private static final String KEPT = null;

	/** element, attribute, context attribute (or ""), its value, written, Word's reading (null: kept). */
	private static final String[][] EVIDENCE = {
		// round 5 P1: w:spacing/@w:before, uint16
		{ "spacing", "before", "", "", "32767", KEPT }, { "spacing", "before", "", "", "32768", KEPT },
		{ "spacing", "before", "", "", "65535", KEPT }, { "spacing", "before", "", "", "65536", "0" },
		{ "spacing", "before", "", "", "65537", "1" }, { "spacing", "before", "", "", "1205003", "25355" },
		{ "spacing", "before", "", "", "-32769", "32767" },
		// round 6 A: w:spacing/@w:after as before
		{ "spacing", "after", "", "", "32768", KEPT }, { "spacing", "after", "", "", "-32768", "32768" },
		{ "spacing", "after", "", "", "7205001", "61577" }, { "spacing", "after", "", "", "1205003", "25355" },
		// round 5 P2: w:ind/@w:left, int16
		{ "ind", "left", "", "", "32767", KEPT }, { "ind", "left", "", "", "32768", "-32768" },
		{ "ind", "left", "", "", "65535", "-1" }, { "ind", "left", "", "", "65536", "0" },
		{ "ind", "left", "", "", "65537", "1" }, { "ind", "left", "", "", "-1", KEPT },
		{ "ind", "left", "", "", "-32768", KEPT }, { "ind", "left", "", "", "-32769", "32767" },
		{ "ind", "left", "", "", "7205001", "-3959" },
		// round 6 A: right, start, end
		{ "ind", "right", "", "", "32768", "-32768" }, { "ind", "right", "", "", "7205001", "-3959" },
		{ "ind", "start", "", "", "32767", KEPT }, { "ind", "start", "", "", "32768", "-32768" },
		{ "ind", "end", "", "", "65536", "0" },
		// round 5 P3: line under auto, plain int16
		{ "spacing", "line", "lineRule", "auto", "32768", "-32768" }, { "spacing", "line", "", "", "7205001", "-3959" },
		// round 5 P4: line under exact, the sign is the rule
		{ "spacing", "line", "lineRule", "exact", "32768", KEPT }, { "spacing", "line", "lineRule", "exact", "65535", "atLeast 1" },
		{ "spacing", "line", "lineRule", "exact", "65536", "atLeast 0" }, { "spacing", "line", "lineRule", "exact", "65537", "exact 1" },
		{ "spacing", "line", "lineRule", "exact", "-1", "atLeast 1" }, { "spacing", "line", "lineRule", "exact", "-32768", "exact 32768" },
		{ "spacing", "line", "lineRule", "exact", "-32769", "exact 32767" }, { "spacing", "line", "lineRule", "exact", "7205001", "atLeast 3959" },
		// round 6 A: line under atLeast
		{ "spacing", "line", "lineRule", "atLeast", "32768", "exact 32768" }, { "spacing", "line", "lineRule", "atLeast", "65535", "exact 1" },
		{ "spacing", "line", "lineRule", "atLeast", "65536", "atLeast 0" }, { "spacing", "line", "lineRule", "atLeast", "-1", "exact 1" },
		{ "spacing", "line", "lineRule", "atLeast", "-32768", "exact 32768" }, { "spacing", "line", "lineRule", "atLeast", "-32769", "atLeast 32767" },
		{ "spacing", "line", "lineRule", "atLeast", "7205001", "exact 3959" },
		// round 5 P5: a tab stop, int16 then clamped to 22 inches
		{ "tab", "pos", "", "", "32767", "31680" }, { "tab", "pos", "", "", "32768", "-31680" },
		{ "tab", "pos", "", "", "65535", "-1" }, { "tab", "pos", "", "", "-32768", "-31680" },
		{ "tab", "pos", "", "", "-32769", "31680" }, { "tab", "pos", "", "", "7205001", "-3959" },
		{ "tab", "pos", "", "", "31680", KEPT },
		// round 5 B1, B2: a border's sz (8 bits) and space (5 bits)
		{ "top", "sz", "", "", "49", KEPT }, { "top", "sz", "", "", "255", KEPT }, { "top", "sz", "", "", "256", "0" },
		{ "top", "sz", "", "", "32767", "255" }, { "top", "sz", "", "", "32768", "0" }, { "top", "sz", "", "", "65537", "1" },
		{ "top", "sz", "", "", "-1", "255" }, { "top", "sz", "", "", "-32769", "255" },
		{ "top", "space", "", "", "31", KEPT }, { "top", "space", "", "", "32", "0" }, { "top", "space", "", "", "255", "31" },
		{ "top", "space", "", "", "256", "0" }, { "top", "space", "", "", "65537", "1" }, { "top", "space", "", "", "-1", "31" },
		{ "top", "space", "", "", "-32769", "31" },
		// round 6 R: w:bdr, and the cell, table and page border families alike
		{ "bdr", "sz", "", "", "256", "0" }, { "bdr", "space", "", "", "32", "0" }, { "insideH", "sz", "", "", "32767", "255" },
		{ "tl2br", "space", "", "", "255", "31" }, { "between", "sz", "", "", "-1", "255" },
		// round 5 R1: w:sz, uint16, no UI clamp
		{ "sz", "val", "", "", "3277", KEPT }, { "sz", "val", "", "", "32768", KEPT }, { "sz", "val", "", "", "65535", KEPT },
		{ "sz", "val", "", "", "65536", "0" }, { "sz", "val", "", "", "65537", "1" }, { "sz", "val", "", "", "-1", "65535" },
		{ "sz", "val", "", "", "-32769", "32767" },
		// round 6 R: szCs, kern as sz
		{ "szCs", "val", "", "", "65536", "0" }, { "kern", "val", "", "", "-32768", "32768" }, { "kern", "val", "", "", "7205001", "61577" },
		// round 5 R2: w:position, int16, no UI clamp
		{ "position", "val", "", "", "3169", KEPT }, { "position", "val", "", "", "32768", "-32768" },
		{ "position", "val", "", "", "-32769", "32767" },
		// round 5 R3: a run's w:spacing, int16, no UI clamp
		{ "spacing", "val", "", "", "31681", KEPT }, { "spacing", "val", "", "", "32768", "-32768" },
		// round 5 R4: w:w (text scale), uint16, no clamp at 600
		{ "w", "val", "", "", "601", KEPT }, { "w", "val", "", "", "65535", KEPT }, { "w", "val", "", "", "65536", "0" },
		{ "w", "val", "", "", "-1", "65535" }, { "w", "val", "", "", "-32769", "32767" },
		// round 6 R: w:fitText kept as written
		{ "fitText", "val", "", "", "7205001", KEPT }, { "fitText", "val", "", "", "-32769", KEPT },
		// round 5 T1 to T5: TblWidth under dxa and pct, int16; auto and nil unchecked
		{ "tcW", "w", "type", "dxa", "32768", "-32768" }, { "tcW", "w", "type", "dxa", "65535", "-1" },
		{ "tcW", "w", "type", "dxa", "65536", "0" }, { "tcW", "w", "type", "dxa", "-32769", "32767" },
		{ "tcW", "w", "type", "dxa", "7205001", "-3959" }, { "tcW", "w", "type", "dxa", "70000", "4464" },
		{ "tblW", "w", "type", "pct", "5001", KEPT }, { "tblW", "w", "type", "pct", "32767", KEPT },
		{ "tblW", "w", "type", "pct", "65536", "0" }, { "tblInd", "w", "type", "dxa", "7205001", "-3959" },
		{ "tblW", "w", "type", "auto", "70000", KEPT }, { "tcW", "w", "type", "nil", "70000", KEPT },
		{ "tblCellSpacing", "w", "type", "dxa", "32768", "-32768" }, { "top", "w", "type", "dxa", "65535", "-1" },
		// round 5 T6: trHeight with no rule, the sign is the rule
		{ "trHeight", "val", "", "", "32767", KEPT }, { "trHeight", "val", "", "", "32768", "exact 32768" },
		{ "trHeight", "val", "", "", "65535", "exact 1" }, { "trHeight", "val", "", "", "65536", "dropped" },
		{ "trHeight", "val", "", "", "65537", "atLeast 1" }, { "trHeight", "val", "", "", "-1", "exact 1" },
		{ "trHeight", "val", "", "", "-32769", "atLeast 32767" }, { "trHeight", "val", "", "", "1205003", "atLeast 25355" },
		// round 6 T: trHeight under exact and under auto
		{ "trHeight", "val", "hRule", "exact", "32768", KEPT }, { "trHeight", "val", "hRule", "exact", "65535", "atLeast 1" },
		{ "trHeight", "val", "hRule", "exact", "-1", "atLeast 1" }, { "trHeight", "val", "hRule", "exact", "-32768", "exact 32768" },
		{ "trHeight", "val", "hRule", "exact", "-32769", "exact 32767" }, { "trHeight", "val", "hRule", "exact", "7205001", "atLeast 3959" },
		{ "trHeight", "val", "hRule", "exact", "65537", "exact 1" }, { "trHeight", "val", "hRule", "auto", "7205001", KEPT },
		// the grid: unmeasured, the certain range only
		{ "gridCol", "w", "", "", "65535", KEPT }, { "gridCol", "w", "", "", "65536", "changed" }, { "gridCol", "w", "", "", "-32769", "changed" },
		// round 6 P: tblpPr
		{ "tblpPr", "tblpX", "", "", "32768", "-32768" }, { "tblpPr", "tblpX", "", "", "-32768", KEPT },
		{ "tblpPr", "leftFromText", "", "", "65536", "0" }, { "tblpPr", "topFromText", "", "", "7205001", "-3959" },
		// round 5 S1, S2: the page size's clamps
		{ "pgSz", "w", "", "", "31681", KEPT }, { "pgSz", "w", "", "", "32767", "31681" }, { "pgSz", "w", "", "", "32768", "31681" },
		{ "pgSz", "w", "", "", "65535", "31681" }, { "pgSz", "w", "", "", "-1", "31681" }, { "pgSz", "w", "", "", "-32769", "31681" },
		{ "pgSz", "w", "", "", "65536", "144" }, { "pgSz", "w", "", "", "65537", "144" }, { "pgSz", "w", "", "", "1205003", "25355" },
		{ "pgSz", "h", "", "", "65535", KEPT }, { "pgSz", "h", "", "", "65536", "144" }, { "pgSz", "h", "", "", "65537", "144" },
		{ "pgSz", "h", "", "", "1205003", "25355" },
		// round 5 S3, round 6 S: the margins
		{ "pgMar", "top", "", "", "32768", "-32768" }, { "pgMar", "top", "", "", "65535", "-1" }, { "pgMar", "top", "", "", "-1", KEPT },
		{ "pgMar", "top", "", "", "-32769", "32767" }, { "pgMar", "top", "", "", "1205003", "25355" },
		{ "pgMar", "bottom", "", "", "32768", "-32768" }, { "pgMar", "bottom", "", "", "7205001", "-3959" },
		{ "pgMar", "left", "", "", "32768", KEPT }, { "pgMar", "left", "", "", "65535", KEPT }, { "pgMar", "left", "", "", "-32768", "32768" },
		{ "pgMar", "gutter", "", "", "7205001", "61577" },
		// round 6 S: columns
		{ "cols", "space", "", "", "32768", "-32768" }, { "cols", "space", "", "", "-1", KEPT }, { "cols", "space", "", "", "7205001", "-3959" },
		{ "col", "w", "", "", "31681", KEPT }, { "col", "w", "", "", "32767", KEPT }, { "col", "w", "", "", "32768", "computed" },
		{ "col", "w", "", "", "65535", "computed" }, { "col", "w", "", "", "-1", "computed" }, { "col", "w", "", "", "7205001", "computed" },
		{ "col", "w", "", "", "65536", "0" }, { "col", "w", "", "", "65537", "1" }, { "col", "w", "", "", "-32769", "32767" },
		{ "col", "space", "", "", "32768", "-1" }, { "col", "space", "", "", "65535", "-1" }, { "col", "space", "", "", "-1", KEPT },
		{ "col", "space", "", "", "-32768", "-1" }, { "col", "space", "", "", "7205001", "-1" }, { "col", "space", "", "", "-32769", "32767" },
		{ "col", "space", "", "", "1205003", "25355" },
		// round 6 S: line numbers
		{ "lnNumType", "distance", "", "", "32768", "-32768" }, { "lnNumType", "distance", "", "", "-1", KEPT },
		{ "lnNumType", "distance", "", "", "7205001", "-3959" },
		// round 5 S4: the page number start, int32
		{ "pgNumType", "start", "", "", "65536", KEPT }, { "pgNumType", "start", "", "", "2147483647", KEPT },
		{ "pgNumType", "start", "", "", "2147483648", "-2147483648" }, { "pgNumType", "start", "", "", "4294967295", "-1" },
		{ "pgNumType", "start", "", "", "4294967296", "0" }, { "pgNumType", "start", "", "", "-1", KEPT },
		// round 6 A: firstLine and hanging, one int16 field
		{ "ind", "firstLine", "", "", "32767", KEPT }, { "ind", "firstLine", "", "", "32768", "hanging 32768" },
		{ "ind", "firstLine", "", "", "65535", "hanging 1" }, { "ind", "firstLine", "", "", "65536", "firstLine 0" },
		{ "ind", "firstLine", "", "", "-1", "hanging 1" }, { "ind", "firstLine", "", "", "-32768", "hanging 32768" },
		{ "ind", "firstLine", "", "", "-32769", "firstLine 32767" }, { "ind", "firstLine", "", "", "7205001", "hanging 3959" },
		{ "ind", "firstLine", "", "", "1205003", "firstLine 25355" },
		{ "ind", "hanging", "", "", "32768", KEPT }, { "ind", "hanging", "", "", "65535", "firstLine 1" },
		{ "ind", "hanging", "", "", "65536", "firstLine 0" }, { "ind", "hanging", "", "", "-1", "firstLine 1" },
		{ "ind", "hanging", "", "", "-32768", "hanging 32768" }, { "ind", "hanging", "", "", "-32769", "hanging 32767" },
		{ "ind", "hanging", "", "", "7205001", "firstLine 3959" },
		// round 6 F: the frame
		{ "framePr", "w", "", "", "32768", "-32768" }, { "framePr", "w", "", "", "65535", "-1" }, { "framePr", "w", "", "", "-1", KEPT },
		{ "framePr", "w", "", "", "7205001", "-3959" }, { "framePr", "x", "", "", "65536", "0" }, { "framePr", "hSpace", "", "", "32768", "-32768" },
		{ "framePr", "h", "", "", "32767", KEPT }, { "framePr", "h", "", "", "32768", "h=0 hRule dropped" },
		{ "framePr", "h", "", "", "65535", "h=32767 hRule dropped" }, { "framePr", "h", "", "", "-1", "h=32767 hRule dropped" },
		{ "framePr", "h", "", "", "65537", "h=1" }, { "framePr", "h", "", "", "-32769", "h=32767" },
		{ "framePr", "h", "", "", "7205001", "h=28809 hRule dropped" },
		// round 6 D, round 2: the settings
		{ "defaultTabStop", "val", "", "", "65535", KEPT }, { "defaultTabStop", "val", "", "", "65536", "0" },
		{ "defaultTabStop", "val", "", "", "-1", "65535" }, { "defaultTabStop", "val", "", "", "-32768", "32768" },
		{ "defaultTabStop", "val", "", "", "7205001", "61577" }, { "defaultTabStop", "val", "", "", "7205002", "61578" },
		{ "hyphenationZone", "val", "", "", "65537", "1" },
		// round 6 N: w:legacy is dropped whatever its values
		{ "legacy", "legacySpace", "", "", "7205001", KEPT },
		// CR-065: Word's lexical reading of the text first (a fraction truncated, a bare sign 0)
		{ "ind", "left", "", "", "32768.9", "-32768" }, { "ind", "left", "", "", "-", KEPT }, { "ind", "left", "", "", "x", KEPT },
	};

	private static Element element(String local, String contextName, String contextValue) throws Exception {
		DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
		dbf.setNamespaceAware(true);
		Document doc = dbf.newDocumentBuilder().newDocument();
		Element e = doc.createElementNS(W, "w:" + local);
		if (!contextName.isEmpty()) e.setAttributeNS(W, "w:" + contextName, contextValue);
		return e;
	}

	@Test
	public void theTableAgreesWithWordsSaves() throws Exception {
		for (String[] row : EVIDENCE) {
			String summary = InspectRanges.summary(row[0], row[1], element(row[0], row[2], row[3]), row[4]);
			String what = "w:" + row[0] + "/@w:" + row[1] + (row[2].isEmpty() ? "" : " " + row[2] + "=" + row[3]) + " = " + row[4];
			if (row[5] == KEPT) {
				assertNull(what + " is kept by Word, so no finding, but: " + summary, summary);
			} else {
				assertNotNull(what + ": Word reads " + row[5] + ", so a finding", summary);
				assertTrue(what + ": " + summary, summary.endsWith("; word_reads=" + row[5]));
				assertTrue(what + ": " + summary, summary.startsWith("attribute=w:" + row[0] + "/@w:" + row[1] + "; "));
				assertTrue(what + ": " + summary, summary.contains("; value=" + row[4] + "; storage="));
			}
		}
	}

	@Test
	public void theSummaryCarriesTheSharedFieldsInOrder() throws Exception {
		assertEquals("attribute=w:ind/@w:left; value=7205001; storage=int16; range=-32768..32767; word_reads=-3959",
				InspectRanges.summary("ind", "left", element("ind", "", ""), "7205001"));
		assertEquals("attribute=w:tcW/@w:w; type=dxa; value=70000; storage=int16; range=-32768..32767; word_reads=4464",
				InspectRanges.summary("tcW", "w", element("tcW", "type", "dxa"), "70000"));
		assertEquals("attribute=w:top/@w:sz; value=256; storage=8 bits; range=0..255; word_reads=0",
				InspectRanges.summary("top", "sz", element("top", "", ""), "256"));
		assertEquals("attribute=w:gridCol/@w:w; value=70000; storage=16 bits; range=-32768..65535; word_reads=changed",
				InspectRanges.summary("gridCol", "w", element("gridCol", "", ""), "70000"));
		assertEquals("attribute=w:framePr/@w:h; value=7205001; storage=15 bits; range=0..32767; word_reads=h=28809 hRule dropped",
				InspectRanges.summary("framePr", "h", element("framePr", "", ""), "7205001"));
	}

	@Test
	public void everyRowParsesAndEveryStorageIsKnown() {
		Map<String, List<InspectRanges.Rule>> rules = InspectRanges.rules();
		assertTrue(rules.size() >= 90);
		for (Map.Entry<String, List<InspectRanges.Rule>> e : rules.entrySet()) {
			for (InspectRanges.Rule r : e.getValue()) {
				if (r.storage.equals("ignored")) continue;
				assertNotNull(e.getKey() + " has no range", r.lo);
				assertTrue(e.getKey() + ": " + r.range, r.lo.compareTo(r.hi) < 0);
				// a value beyond each end has a reading
				assertNotNull(InspectRanges.reading(r, r.hi.add(BigInteger.ONE)));
				assertNotNull(InspectRanges.reading(r, r.lo.subtract(BigInteger.ONE)));
				assertNotNull(InspectRanges.storage(r.storage));
			}
		}
		// the attributes of CR-066 sections 3.1 and 3.4 are all there
		for (String key : new String[] { "spacing/before", "spacing/after", "spacing/line", "spacing/val", "ind/left", "ind/right",
				"ind/start", "ind/end", "ind/firstLine", "ind/hanging", "tab/pos", "framePr/w", "framePr/h", "position/val", "w/val",
				"sz/val", "szCs/val", "kern/val", "fitText/val", "top/sz", "top/space", "bdr/sz", "tblW/w", "tcW/w", "tblInd/w",
				"tblCellSpacing/w", "top/w", "trHeight/val", "gridCol/w", "tblpPr/tblpX", "tblpPr/leftFromText", "pgSz/w", "pgSz/h",
				"pgMar/top", "pgMar/left", "cols/space", "col/w", "col/space", "lnNumType/distance", "pgNumType/start",
				"defaultTabStop/val", "hyphenationZone/val", "legacy/legacySpace" }) {
			assertTrue(key, rules.containsKey(key));
		}
	}
}
