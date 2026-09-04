package org.docx4j.model.properties;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import java.math.BigInteger;
import java.util.List;

import org.docx4j.XmlUtils;
import org.docx4j.model.properties.paragraph.SpaceAfter;
import org.docx4j.model.properties.paragraph.SpaceBefore;
import org.docx4j.model.properties.paragraph.WidowControl;
import org.docx4j.openpackaging.packages.WordprocessingMLPackage;
import org.docx4j.openpackaging.parts.WordprocessingML.DocumentSettingsPart;
import org.docx4j.wml.BooleanDefaultTrue;
import org.docx4j.wml.CTCompat;
import org.docx4j.wml.ObjectFactory;
import org.docx4j.wml.PPr;
import org.docx4j.wml.PPrBase;
import org.junit.Test;
import org.w3c.dom.Element;

/** Word's HTML auto spacing and widow control, as PropertyFactory now maps them (17.0.5). */
public class ParagraphSpacingPropertiesTest {

	private static final ObjectFactory F = new ObjectFactory();

	private static PPr pPr(Integer before, Integer after, boolean autoBefore, boolean autoAfter) {
		PPr pPr = F.createPPr();
		PPrBase.Spacing sp = F.createPPrBaseSpacing();
		if (before != null) sp.setBefore(BigInteger.valueOf(before));
		if (after != null) sp.setAfter(BigInteger.valueOf(after));
		if (autoBefore) sp.setBeforeAutospacing(Boolean.TRUE);
		if (autoAfter) sp.setAfterAutospacing(Boolean.TRUE);
		pPr.setSpacing(sp);
		return pPr;
	}

	private static Element fo(WordprocessingMLPackage pkg, PPr pPr) {
		List<Property> props = PropertyFactory.createProperties(pkg, pPr);
		Element el = XmlUtils.getNewDocumentBuilder().newDocument().createElementNS("http://www.w3.org/1999/XSL/Format", "fo:block");
		for (Property p : props) p.setXslFO(el);
		return el;
	}

	@Test
	public void autoSpacingIs14pt() throws Exception {
		WordprocessingMLPackage pkg = WordprocessingMLPackage.createPackage();
		Element el = fo(pkg, pPr(100, 100, true, true));
		assertEquals("14pt", el.getAttribute("space-before"));
		assertEquals("14pt", el.getAttribute("space-after"));
		// auto on one side only
		el = fo(pkg, pPr(100, 100, false, true));
		assertEquals("5pt", el.getAttribute("space-before"));
		assertEquals("14pt", el.getAttribute("space-after"));
	}

	@Test
	public void compatSettingDisablesAutoSpacing() throws Exception {
		WordprocessingMLPackage pkg = WordprocessingMLPackage.createPackage();
		DocumentSettingsPart dsp = pkg.getMainDocumentPart().getDocumentSettingsPart();
		CTCompat compat = dsp.getContents().getCompat();
		if (compat == null) {
			compat = F.createCTCompat();
			dsp.getContents().setCompat(compat);
		}
		compat.setDoNotUseHTMLParagraphAutoSpacing(new BooleanDefaultTrue());
		assertTrue(DocumentSettingsPart.isDoNotUseHTMLParagraphAutoSpacing(pkg));
		Element el = fo(pkg, pPr(100, 100, true, true));
		assertEquals("5pt", el.getAttribute("space-before"));
		assertEquals("5pt", el.getAttribute("space-after"));
	}

	@Test
	public void compatibilityModeDefaultsTo12() throws Exception {
		WordprocessingMLPackage pkg = WordprocessingMLPackage.createPackage();
		assertEquals(12, DocumentSettingsPart.getCompatibilityMode(pkg));
		pkg.getMainDocumentPart().getDocumentSettingsPart().setWordCompatSetting("compatibilityMode", "15");
		assertEquals(15, DocumentSettingsPart.getCompatibilityMode(pkg));
	}

	@Test
	public void widowControlOffOnly() throws Exception {
		WordprocessingMLPackage pkg = WordprocessingMLPackage.createPackage();
		PPr on = F.createPPr();
		on.setWidowControl(new BooleanDefaultTrue());
		Element el = fo(pkg, on);
		assertEquals("on is the FO default; nothing emitted", "", el.getAttribute("widows"));

		PPr off = F.createPPr();
		BooleanDefaultTrue b = new BooleanDefaultTrue();
		b.setVal(Boolean.FALSE);
		off.setWidowControl(b);
		el = fo(pkg, off);
		assertEquals("1", el.getAttribute("widows"));
		assertEquals("1", el.getAttribute("orphans"));
	}

	@Test
	public void losslessTwips() {
		assertEquals("1in", org.docx4j.UnitsOfMeasurement.twipToBest(1440));
		assertEquals("12pt", org.docx4j.UnitsOfMeasurement.twipToBest(240));
		assertEquals("35.4pt", org.docx4j.UnitsOfMeasurement.twipToBest(708));
		assertEquals("0.05pt", org.docx4j.UnitsOfMeasurement.twipToBest(1));
		assertNull(null);
		assertTrue(new SpaceBefore(BigInteger.valueOf(708)) != null && new SpaceAfter(BigInteger.ZERO) != null
				&& new WidowControl(new BooleanDefaultTrue()) != null);
	}
}
