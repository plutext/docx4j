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
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.docx4j.TextUtils;
import org.docx4j.XmlUtils;
import org.docx4j.openpackaging.packages.WordprocessingMLPackage;
import org.docx4j.openpackaging.parts.Part;
import org.docx4j.openpackaging.parts.WordprocessingML.HeaderPart;
import org.docx4j.openpackaging.parts.WordprocessingML.MainDocumentPart;
import org.docx4j.utils.ResourceUtils;
import org.docx4j.wml.CTBorder;
import org.docx4j.wml.P;
import org.docx4j.wml.PPr;
import org.docx4j.wml.R;
import org.docx4j.wml.RPr;
import org.docx4j.wml.SectPr;
import org.docx4j.wml.Style;
import org.docx4j.wml.Tbl;
import org.docx4j.wml.Tc;
import org.docx4j.wml.Tr;
import org.junit.BeforeClass;
import org.junit.Test;

/**
 * CR-065 phase 2 against phase 0's probe: {@code cr065-decimals-probe.docx} is the all-cases file Word 365 was
 * given (section 3; Word refused it whole for the forms in family A, then answered the families one by one). After
 * phase 2 docx4j reads all four of its parts, and every value equals Word's reading from its saves: a decimal
 * truncated toward zero, the empty string 0, and the forms Word refuses dropped (null). Before phase 2 the main
 * part, the styles part, the settings part and the header all failed to unmarshal.
 */
public class DecimalAttributesProbeTest {

	private static WordprocessingMLPackage pkg;
	private static MainDocumentPart mdp;
	/** case id -> the paragraph (A to F, I, Z), the table (G) or the section properties (H) that carries the value */
	private static final Map<String, Object> CASES = new LinkedHashMap<>();

	@BeforeClass
	public static void load() throws Exception {
		pkg = WordprocessingMLPackage.load(ResourceUtils.getResource("cr065-decimals-probe.docx"));
		mdp = pkg.getMainDocumentPart();
		assertNotNull("the main part reads", mdp.getJaxbElement());
		List<Object> content = mdp.getContent();
		for (int i = 0; i < content.size(); i++) {
			Object o = XmlUtils.unwrap(content.get(i));
			if (!(o instanceof P)) continue;
			String text = TextUtils.getText(o);
			if (text.length() < 4 || !Character.isUpperCase(text.charAt(0)) || !Character.isDigit(text.charAt(1)) || !Character.isDigit(text.charAt(2))) continue;
			String id = text.substring(0, 3);
			if (id.startsWith("G")) CASES.put(id, XmlUtils.unwrap(content.get(i + 1)));
			else if (id.startsWith("H")) CASES.put(id, ((P) XmlUtils.unwrap(content.get(i + 1))).getPPr().getSectPr());
			else CASES.put(id, o);
		}
		assertEquals("65 labelled cases", 65, CASES.size());
	}

	@Test
	public void familyAspacingBeforeTheDecimalSetAndTheOddForms() {
		assertBefore("A01", 120); assertBefore("A02", 120); assertBefore("A03", 121); assertBefore("A04", 122);
		assertBefore("A05", 240); assertBefore("A06", 141);
		assertBefore("A07", 240);   // "240."
		assertBefore("A08", 0);     // ".5"
		assertBefore("A09", 240);   // "+240"
		assertBefore("A10", 240);   // "+240.5"
		assertBefore("A11", null);  // " 240.5 " refused by Word, dropped here
		assertBefore("A12", null);  // "240,5"
		assertBefore("A13", null);  // "0x10"
		assertBefore("A14", null);  // "1.5e2"
		assertBefore("A15", null);  // "2.5e0"
		assertBefore("A16", null);  // "1e3"
		assertBefore("A17", 0);     // ""
		assertBefore("A18", null);  // "abc"
		assertBefore("A19", null);  // " 240 "
		assertBefore("Z65", 120);
	}

	@Test
	public void familyBindLeftTruncatesTowardZero() {
		assertEquals(720, left("B20")); assertEquals(720, left("B21")); assertEquals(721, left("B22"));
		assertEquals(-120, left("B23")); assertEquals(-121, left("B24")); assertEquals(0, left("B25"));
		assertEquals(-1, left("B26")); assertEquals(10206, left("B27"));
	}

	@Test
	public void familyCtheStylesheetsListedAttributesNowReadAsWordDoes() {
		PPr c28 = pPr("C28"), c29 = pPr("C29"), c30 = pPr("C30"), c31 = pPr("C31"), c32 = pPr("C32");
		assertEquals("141.5: Word 141 (the stylesheet gave 142)", 141, c28.getSpacing().getAfter().intValue());
		assertEquals(120, c29.getSpacing().getAfter().intValue());
		assertEquals(259, c30.getSpacing().getLine().intValue());
		assertEquals(240, c31.getSpacing().getLine().intValue());
		assertEquals("241.5: Word 241 (the stylesheet gave 242)", 241, c32.getSpacing().getLine().intValue());
		assertEquals("141.99999999999994: Word 141 (the stylesheet gave 142)", 141, pPr("C33").getInd().getHanging().intValue());
		assertEquals(12, pPr("C34").getInd().getFirstLine().intValue());
	}

	@Test
	public void familyDborders() {
		assertEquals(7, pPr("D35").getPBdr().getTop().getSz().intValue());
		assertEquals("1.8: Word 1 (the stylesheet gave 2)", 1, pPr("D36").getPBdr().getTop().getSpace().intValue());
		assertEquals(7, pPr("D37").getPBdr().getBottom().getSpace().intValue());
		CTBorder between = pPr("D38").getPBdr().getBetween();
		assertEquals("w:between was never on the stylesheet's list", 1, between.getSpace().intValue());
	}

	@Test
	public void familyEtabs() {
		assertEquals(1440, pPr("E39").getTabs().getTab().get(0).getPos().intValue());
		assertEquals(720, pPr("E40").getTabs().getTab().get(0).getPos().intValue());
	}

	@Test
	public void familyFrunProperties() {
		assertEquals(22, rPr("F41").getSz().getVal().intValue());
		assertEquals(90, rPr("F42").getW().getVal().intValue());
		assertEquals(20, rPr("F43").getSpacing().getVal().intValue());
		assertEquals(6, rPr("F44").getPosition().getVal().intValue());
	}

	@Test
	public void familyGtables() {
		assertEquals(9000, tbl("G45").getTblPr().getTblW().getW().intValue());
		assertEquals("9001.5: Word 9001 (the stylesheet gave 9002)", 9001, tbl("G46").getTblPr().getTblW().getW().intValue());
		assertEquals(10206, tbl("G47").getTblPr().getTblW().getW().intValue());
		assertEquals(100, tbl("G48").getTblPr().getTblW().getW().intValue());
		assertEquals(10, tbl("G49").getTblPr().getTblInd().getW().intValue());
		assertEquals(4500, tbl("G50").getTblGrid().getGridCol().get(0).getW().intValue());
		assertEquals(5705, tbl("G50").getTblGrid().getGridCol().get(1).getW().intValue());
		assertEquals(1234, tbl("G51").getTblGrid().getGridCol().get(0).getW().intValue());
		assertEquals("400.7: Word 400 (the stylesheet gave 401)", 400, trHeight("G52"));
		assertEquals(300, trHeight("G53"));
		assertEquals(50, firstTc("G54").getTcPr().getTcW().getW().intValue());
		assertEquals(50, tbl("G55").getTblPr().getTblW().getW().intValue());
	}

	@Test
	public void familyHsections() {
		SectPr h56 = sectPr("H56"), h57 = sectPr("H57");
		assertEquals(11907, h56.getPgSz().getW().intValue()); assertEquals(16839, h56.getPgSz().getH().intValue());
		assertEquals("11907.5: Word 11907 (the stylesheet gave 11908)", 11907, h57.getPgSz().getW().intValue());
		assertEquals(16838, h57.getPgSz().getH().intValue());
		assertEquals(1133, sectPr("H58").getPgMar().getTop().intValue());
		assertEquals(1440, sectPr("H59").getPgMar().getTop().intValue());
		SectPr h60 = sectPr("H60");
		assertEquals("SSRS's header=\"\" is 0", 0, h60.getPgMar().getHeader().intValue());
		assertEquals(0, h60.getPgMar().getFooter().intValue());
		assertEquals(0, h60.getPgMar().getGutter().intValue());
		assertEquals("an empty rsid is a string and stays (CR-065 D5: a separate question)", "", h60.getRsidR());
		assertEquals(0, sectPr("H61").getPgMar().getTop().intValue());
	}

	@Test
	public void familyItheOtherPartsRead() {
		Style probe = mdp.getStyleDefinitionsPart().getStyleById("ProbeStyle");
		assertNotNull("styles.xml reads", probe);
		assertEquals(720, probe.getPPr().getInd().getLeft().intValue());
		assertEquals(720, mdp.getDocumentSettingsPart().getJaxbElement().getDefaultTabStop().getVal().intValue());
		HeaderPart header = null;
		for (Part part : pkg.getParts().getParts().values()) if (part instanceof HeaderPart) header = (HeaderPart) part;
		assertNotNull(header);
		P hp = (P) XmlUtils.unwrap(header.getContent().get(0));
		assertEquals(120, hp.getPPr().getSpacing().getBefore().intValue());
	}

	@Test
	public void aSaveWritesTheIntegersRead() throws Exception {
		String xml = XmlUtils.marshaltoString(mdp.getJaxbElement(), true, false);
		assertTrue(xml.contains("w:before=\"121\""));
		// the labels quote the values as text (w:spacing/@w:before="abc"); an attribute is preceded by a space
		assertTrue("the dropped attributes are not written", !xml.contains(" w:before=\"abc\"") && !xml.contains(" w:before=\"0x10\""));
		assertTrue(xml.contains(" w:before=\"240\""));
		assertTrue(xml.contains("w:w=\"9001\" w:type=\"dxa\"") || xml.contains("w:type=\"dxa\" w:w=\"9001\""));
	}

	// ---- helpers

	private static void assertBefore(String id, Integer expected) {
		BigInteger v = pPr(id).getSpacing().getBefore();
		if (expected == null) assertNull(id + " drops the attribute", v);
		else { assertNotNull(id, v); assertEquals(id, expected.intValue(), v.intValue()); }
	}
	private static int left(String id) { return pPr(id).getInd().getLeft().intValue(); }
	private static PPr pPr(String id) { return ((P) CASES.get(id)).getPPr(); }
	private static RPr rPr(String id) {
		for (Object o : ((P) CASES.get(id)).getContent()) {
			Object u = XmlUtils.unwrap(o);
			if (u instanceof R && ((R) u).getRPr() != null) return ((R) u).getRPr();
		}
		throw new AssertionError(id + ": no run with properties");
	}
	private static Tbl tbl(String id) { return (Tbl) CASES.get(id); }
	private static Tr firstTr(String id) { return (Tr) XmlUtils.unwrap(tbl(id).getContent().get(0)); }
	private static Tc firstTc(String id) { return (Tc) XmlUtils.unwrap(firstTr(id).getContent().get(0)); }
	private static int trHeight(String id) {
		for (jakarta.xml.bind.JAXBElement<?> e : firstTr(id).getTrPr().getCnfStyleOrDivIdOrGridBefore()) {
			if (e.getValue() instanceof org.docx4j.wml.CTHeight) return ((org.docx4j.wml.CTHeight) e.getValue()).getVal().intValue();
		}
		throw new AssertionError(id + ": no trHeight");
	}
	private static SectPr sectPr(String id) { return (SectPr) CASES.get(id); }
}
