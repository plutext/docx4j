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
package org.docx4j.jaxb;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import java.math.BigInteger;

import org.docx4j.XmlUtils;
import org.docx4j.wml.CTBorder;
import org.docx4j.wml.LenientInteger;
import org.docx4j.wml.PPr;
import org.docx4j.wml.RPr;
import org.docx4j.wml.SectPr;
import org.docx4j.wml.TcPr;
import org.junit.Test;

/**
 * CR-065 phase 2 (D2 to D4, D6): an integer attribute of WordprocessingML is read as Word 365 reads it, in the
 * JAXB binding itself ({@link LenientInteger}, bound by xsd/bindings.xjb to the integer simple types): a decimal is
 * truncated toward zero, the empty string is 0, and a form Word refuses drops the attribute with a WARN rather
 * than failing the part. Word's rule is CR-065 section 3 (four rounds, 65 cases).
 */
public class LenientIntegerTest {

	private static final String W = "http://schemas.openxmlformats.org/wordprocessingml/2006/main";

	// ---- the parse method, every measured form

	@Test
	public void wordsIntegerFormsReadAsIntegers() {
		assertEquals(120, parse("120"));
		assertEquals(-121, parse("-121"));
		assertEquals(240, parse("+240"));           // A2c: Word accepts a plus sign
		assertEquals(0, parse("0"));
		assertEquals(new BigInteger("7205001"), LenientInteger.parse("7205001")); // out of Word's 16-bit range, read as written (CR-066)
	}

	@Test
	public void aDecimalIsTruncatedTowardZero() {
		assertEquals(120, parse("120.5"));
		assertEquals(120, parse("120.4"));
		assertEquals(121, parse("121.5"));          // not half-even (the stylesheet's rounding gave 122)
		assertEquals(122, parse("122.5"));
		assertEquals(240, parse("240.0"));
		assertEquals(141, parse("141.99999999999994")); // not rounded up
		assertEquals(720, parse("720.5"));
		assertEquals(-120, parse("-120.5"));
		assertEquals(-121, parse("-121.5"));        // toward zero, not away from it
		assertEquals(0, parse("-0.5"));
		assertEquals(-1, parse("-1.5"));
		assertEquals(10206, parse("10206.0"));
		assertEquals(259, parse("259.2"));
		assertEquals(1, parse("1.8"));
		assertEquals(400, parse("400.7"));
		assertEquals(9001, parse("9001.5"));
	}

	@Test
	public void eitherDigitGroupMayBeEmptyButNotBoth() {
		assertEquals(240, parse("240."));           // A2a
		assertEquals(0, parse(".5"));               // A2b
		assertEquals(240, parse("+240.5"));         // A2d
		assertEquals(0, parse("-.5"));
		assertNull("a sign or dot with no digit is unmeasured and treated as refused", LenientInteger.parse("-"));
		assertNull(LenientInteger.parse("."));
		assertNull(LenientInteger.parse("+."));
	}

	@Test
	public void theEmptyStringIsZero() {
		assertEquals(0, parse(""));                 // A6, H60, H61
	}

	@Test
	public void aFormWordRefusesDropsTheAttribute() {
		assertNull(LenientInteger.parse(" 240.5 "));  // A2e: no trimming (round 4)
		assertNull(LenientInteger.parse(" 240 "));    // A2f
		assertNull(LenientInteger.parse("240,5"));    // A3
		assertNull(LenientInteger.parse("0x10"));     // A4
		assertNull(LenientInteger.parse("1.5e2"));    // A5
		assertNull(LenientInteger.parse("2.5e0"));
		assertNull(LenientInteger.parse("1e3"));
		assertNull(LenientInteger.parse("abc"));      // A7
		assertNull(LenientInteger.parse("1.2.3"));
		assertNull(LenientInteger.parse("12pt"));
		assertNull("only ASCII digits are Word's form", LenientInteger.parse("٣"));
		assertNull(LenientInteger.parse(null));
	}

	@Test
	public void printWritesTheValueAsHeld() {
		assertEquals("120", LenientInteger.print(BigInteger.valueOf(120)));
		assertEquals("-3959", LenientInteger.print(BigInteger.valueOf(-3959)));
		assertEquals("7205001", LenientInteger.print(BigInteger.valueOf(7205001)));
		assertNull(LenientInteger.print(null));
	}

	// ---- through the binding (Context.jc), the attribute types of CR-065 D4

	@Test
	public void paragraphPropertiesReadThroughTheBinding() throws Exception {
		PPr pPr = (PPr) XmlUtils.unmarshalString("<w:pPr xmlns:w=\"" + W + "\">"
				+ "<w:spacing w:before=\"120.5\" w:after=\"141.5\" w:line=\"259.2\" w:lineRule=\"auto\"/>"
				+ "<w:ind w:left=\"abc\" w:hanging=\"141.99999999999994\" w:firstLine=\"\"/>"
				+ "<w:tabs><w:tab w:val=\"left\" w:pos=\"1440.5\"/></w:tabs>"
				+ "</w:pPr>", Context.jc, PPr.class);
		assertEquals(120, pPr.getSpacing().getBefore().intValue());
		assertEquals("the stylesheet's half-even never fires now; Word truncates", 141, pPr.getSpacing().getAfter().intValue());
		assertEquals(259, pPr.getSpacing().getLine().intValue());
		assertNull("a form Word refuses drops the attribute, the part still reads", pPr.getInd().getLeft());
		assertEquals(141, pPr.getInd().getHanging().intValue());
		assertEquals(0, pPr.getInd().getFirstLine().intValue());
		assertEquals(1440, pPr.getTabs().getTab().get(0).getPos().intValue());
	}

	@Test
	public void runPropertiesBordersCellsAndSectionsReadThroughTheBinding() throws Exception {
		RPr rPr = (RPr) XmlUtils.unmarshalString("<w:rPr xmlns:w=\"" + W + "\">"
				+ "<w:spacing w:val=\"20.5\"/><w:w w:val=\"90.5\"/><w:position w:val=\"6.5\"/><w:sz w:val=\"22.5\"/><w:szCs w:val=\"22.5\"/>"
				+ "</w:rPr>", Context.jc, RPr.class);
		assertEquals(20, rPr.getSpacing().getVal().intValue());
		assertEquals(90, rPr.getW().getVal().intValue());
		assertEquals(6, rPr.getPosition().getVal().intValue());
		assertEquals("CT_HpsMeasure/@w:val, typed xsd:unsignedLong directly", 22, rPr.getSz().getVal().intValue());
		assertEquals(22, rPr.getSzCs().getVal().intValue());

		CTBorder top = (CTBorder) XmlUtils.unmarshalString("<w:top xmlns:w=\"" + W + "\" w:val=\"single\" w:sz=\"7.5\" w:space=\"1.8\" w:color=\"auto\"/>", Context.jc, CTBorder.class);
		assertEquals(7, top.getSz().intValue());
		assertEquals(1, top.getSpace().intValue());

		TcPr tcPr = (TcPr) XmlUtils.unmarshalString("<w:tcPr xmlns:w=\"" + W + "\"><w:tcW w:w=\"50.5\" w:type=\"pct\"/><w:gridSpan w:val=\"2.0\"/></w:tcPr>", Context.jc, TcPr.class);
		assertEquals(50, tcPr.getTcW().getW().intValue());
		assertEquals(2, tcPr.getGridSpan().getVal().intValue());

		SectPr sectPr = (SectPr) XmlUtils.unmarshalString("<w:sectPr xmlns:w=\"" + W + "\" w:rsidR=\"\">"
				+ "<w:pgSz w:w=\"11907.0\" w:h=\"16839.0\"/>"
				+ "<w:pgMar w:top=\"1133.5\" w:right=\"1440\" w:bottom=\"1440\" w:left=\"1440\" w:header=\"\" w:footer=\"\" w:gutter=\"\"/>"
				+ "<w:pgNumType w:start=\"3.9\"/></w:sectPr>", Context.jc, SectPr.class);
		assertEquals(11907, sectPr.getPgSz().getW().intValue());
		assertEquals(16839, sectPr.getPgSz().getH().intValue());
		assertEquals(1133, sectPr.getPgMar().getTop().intValue());
		assertEquals("SSRS's empty header is 0", 0, sectPr.getPgMar().getHeader().intValue());
		assertEquals(0, sectPr.getPgMar().getFooter().intValue());
		assertEquals(0, sectPr.getPgMar().getGutter().intValue());
		assertEquals(3, sectPr.getPgNumType().getStart().intValue());
		assertEquals("an rsid is a string, not an integer; untouched", "", sectPr.getRsidR());
	}

	@Test
	public void marshalWritesTheIntegerRead() throws Exception {
		PPr pPr = (PPr) XmlUtils.unmarshalString("<w:pPr xmlns:w=\"" + W + "\"><w:spacing w:before=\"120.5\"/><w:ind w:left=\"abc\" w:right=\"-121.5\"/></w:pPr>", Context.jc, PPr.class);
		String xml = XmlUtils.marshaltoString(pPr, true, false, Context.jc, W, "pPr", PPr.class);
		assertTrue(xml, xml.contains("w:before=\"120\""));
		assertTrue(xml, xml.contains("w:right=\"-121\""));
		assertTrue("the dropped attribute is not written: " + xml, !xml.contains("w:left"));
	}

	// ---- the report scope (D6)

	@Test
	public void aScopeCountsWhatWasReadLeniently() {
		LenientInteger.beginReport();
		LenientInteger.parse("120");          // not lenient, not counted
		LenientInteger.parse("120.5");
		LenientInteger.parse("-1.5");
		LenientInteger.parse("");
		LenientInteger.parse("abc");
		LenientInteger.beginReport();         // an inner scope (the preprocessor retry) is counted, not reported
		LenientInteger.parse("7.5");
		assertNull(LenientInteger.endReport());
		LenientInteger.Report r = LenientInteger.endReport();
		assertNotNull(r);
		assertEquals(3, r.getTruncated());
		assertEquals(1, r.getEmpty());
		assertEquals(1, r.getDropped());
		String summary = r.summary();
		assertTrue(summary, summary.contains("3 decimal integer attribute values truncated"));
		assertTrue(summary, summary.contains("1 empty read as 0"));
		assertTrue(summary, summary.contains("1 of a form Word refuses dropped"));
		assertTrue(summary, summary.contains("\"120.5\" -> 120"));
		assertTrue(summary, summary.contains("\"abc\" dropped"));
		assertNull("no scope is open now", LenientInteger.endReport());
	}

	private static int parse(String s) {
		BigInteger v = LenientInteger.parse(s);
		assertNotNull("expected a value for \"" + s + "\"", v);
		return v.intValue();
	}
}
