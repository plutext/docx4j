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
package org.docx4j.fonts;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.io.File;
import java.math.BigInteger;

import org.docx4j.XmlUtils;
import org.docx4j.fonts.fop.util.FopConfigUtil;
import org.docx4j.jaxb.Context;
import org.docx4j.openpackaging.packages.WordprocessingMLPackage;
import org.docx4j.openpackaging.parts.WordprocessingML.FontTablePart;
import org.docx4j.wml.Fonts;
import org.docx4j.wml.HpsMeasure;
import org.docx4j.wml.ObjectFactory;
import org.docx4j.wml.P;
import org.docx4j.wml.PPr;
import org.docx4j.wml.PPrBase;
import org.docx4j.wml.R;
import org.docx4j.wml.RFonts;
import org.docx4j.wml.RPr;
import org.docx4j.wml.STLineSpacingRule;
import org.docx4j.wml.Text;
import org.junit.Assume;
import org.junit.Test;
import org.w3c.dom.DocumentFragment;
import org.w3c.dom.Element;
import org.w3c.dom.Node;

/**
 * A run in a font embedded in the document gets its line height from the embedded
 * file's own metrics (CR-016 gap 7).  Until 17.1.1 the line-height pass looked the
 * physical font up by name in PhysicalFonts, where an embedded font never is, so a
 * corporate font embedded in a document - exactly the case the Microsoft table cannot
 * know - got the 1.2 fallback; likewise the no-ligature twin.
 *
 * <p>FontEmbedded.docx embeds Calibri; the test renames the font-table entry to a name
 * neither the table nor this machine knows, so that the embedded file is the only
 * source of its metrics.</p>
 */
public class EmbeddedFontMetricsTest {

	private static final String DOC_FONT = "Corporate Sans";
	private static final File SAMPLE = new File(System.getProperty("user.dir"), "../docx4j-samples-docx4j/sample-docs/FontEmbedded.docx");

	@Test
	public void embeddedFontsOwnMetricsSizeTheLine() throws Exception {
		Assume.assumeTrue("sample document not found: " + SAMPLE, SAMPLE.exists());
		Assume.assumeTrue(DOC_FONT + " is unexpectedly installed", PhysicalFonts.get(DOC_FONT)==null);
		WordprocessingMLPackage pkg = WordprocessingMLPackage.load(SAMPLE);

		FontTablePart fontTable = pkg.getMainDocumentPart().getFontTablePart();
		boolean renamed = false;
		for (Fonts.Font font : fontTable.getJaxbElement().getFont()) {
			if ("Calibri".equals(font.getName()) && font.getEmbedRegular()!=null) {
				font.setName(DOC_FONT);
				renamed = true;
			}
		}
		assertTrue("FontEmbedded.docx no longer embeds Calibri", renamed);

		ObjectFactory factory = Context.getWmlObjectFactory();
		P p = (P) pkg.getMainDocumentPart().getContent().get(1);
		R r = (R) p.getContent().get(0);
		RPr rPr = factory.createRPr();
		RFonts rFonts = factory.createRFonts();
		rFonts.setAscii(DOC_FONT);
		rFonts.setHAnsi(DOC_FONT);
		rPr.setRFonts(rFonts);
		HpsMeasure sz = factory.createHpsMeasure();
		sz.setVal(BigInteger.valueOf(22));
		rPr.setSz(sz);
		r.setRPr(rPr);
		PPr pPr = factory.createPPr();
		PPrBase.Spacing spacing = factory.createPPrBaseSpacing();
		spacing.setLine(BigInteger.valueOf(240));
		spacing.setLineRule(STLineSpacingRule.AUTO);
		pPr.setSpacing(spacing);
		p.setPPr(pPr);

		Mapper mapper = pkg.getFontMapper(); // extracts the embedded fonts and maps the document's names
		PhysicalFont embedded = mapper.get(DOC_FONT);
		assertNotNull(DOC_FONT + " not mapped to the embedded font", embedded);
		assertTrue(DOC_FONT + " mapped to an installed font, not the embedded one", mapper.isEmbedded(DOC_FONT));
		Assume.assumeTrue("the embedded file's metrics are unreadable", !WordLineMetrics.get(embedded).fallback);

		String expected = WordLineMetrics.lineHeightPtString(DOC_FONT, embedded, 11.0, spacing);
		String fallback = WordLineMetrics.lineHeightPtString(DOC_FONT, null, 11.0, spacing);
		assertNotEquals("the embedded file's metrics happen to equal the fallback, so the test cannot tell", fallback, expected);

		RunFontSelector rfs = FontsTestSupport.xslFoSelector(pkg);
		Object o = rfs.fontSelector(p.getPPr(), r.getRPr(), (Text) XmlUtils.unwrap(r.getContent().get(0)));
		assertTrue(String.valueOf(o), o instanceof DocumentFragment);
		Element span = null;
		for (Node n = ((DocumentFragment) o).getFirstChild(); n!=null; n = n.getNextSibling()) {
			if (n instanceof Element) { span = (Element) n; break; }
		}
		assertNotNull("no inline", span);
		assertEquals("line-height of a run in the embedded font", expected, span.getAttribute("line-height"));

		// and the no-ligature twin, which needs the same lookup
		if (FopConfigUtil.isTrueTypeFlavoured(embedded.getEmbeddedURI().toString())) {
			assertTrue("no +noliga twin for the embedded TrueType font: " + span.getAttribute("font-family"),
					span.getAttribute("font-family").endsWith(RunFontSelector.NOLIGA_SUFFIX));
		}
	}
}
