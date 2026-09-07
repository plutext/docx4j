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
package org.docx4j.convert.out.XSLFO;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.io.ByteArrayOutputStream;

import org.docx4j.Docx4J;
import org.docx4j.XmlUtils;
import org.docx4j.convert.out.FOSettings;
import org.docx4j.openpackaging.packages.WordprocessingMLPackage;
import org.docx4j.wml.Document;
import org.junit.Test;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

/**
 * A small-caps run's line is sized at the run's <em>declared</em> size (CR-001
 * &#xa7;5.7).
 *
 * <p>{@code w:smallCaps} has no XSL-FO equivalent, so docx4j upper-cases the text and
 * puts the originally-lower-case stretches in an {@code fo:inline} at 80% of the size.
 * Word scales the glyphs, not the line; FOP takes a line's ascent from the areas on it,
 * so where such an inline is a block's <b>only</b> content the line came out 80% high.
 * Measured on a corpus letterhead whose CONTACT block is nothing but a small-caps run,
 * Word puts it and the two cells beside it on one baseline (123.2) where docx4j split the
 * row into 120.3 / 122.0 / 122.0; the block's own {@code docx4j:baseline="9.38pt"} was
 * emitted but could not win against a measurable smaller area.
 *
 * <p>The span now says what it was scaled by ({@code docx4j:small-caps}), and
 * {@code WordLineLayoutManager} reads its height at the size the run declares.  Scaling
 * the height rather than skipping the area keeps a small-caps run correct where it shares
 * a line with others.
 *
 * @since 17.1.0
 */
public class SmallCapsLineHeightTest extends AbstractXSLFOTest {

	private static final String W = "xmlns:w=\"http://schemas.openxmlformats.org/wordprocessingml/2006/main\"";
	private static final String FO = "http://www.w3.org/1999/XSL/Format";
	private static final String DOCX4J = "http://docx4j.org/fop/word-layout";

	private static final String BODY =
			"<w:document " + W + "><w:body>"
			+ "<w:p><w:r><w:rPr><w:smallCaps/><w:sz w:val=\"20\"/></w:rPr>"
			+ "<w:t>Contact</w:t></w:r></w:p>"
			+ "<w:sectPr><w:pgSz w:w=\"11906\" w:h=\"16838\"/>"
			+ "<w:pgMar w:top=\"1440\" w:right=\"1440\" w:bottom=\"1440\" w:left=\"1440\"/>"
			+ "</w:sectPr></w:body></w:document>";

	private org.w3c.dom.Document fo(int flags) throws Exception {
		WordprocessingMLPackage pkg = WordprocessingMLPackage.createPackage();
		pkg.getMainDocumentPart().setJaxbElement((Document)XmlUtils.unmarshalString(BODY));
		FOSettings foSettings = Docx4J.createFOSettings();
		foSettings.setOpcPackage(pkg);
		foSettings.setApacheFopMime(FOSettings.INTERNAL_FO_MIME);
		ByteArrayOutputStream baos = new ByteArrayOutputStream();
		Docx4J.toFO(foSettings, baos, flags);
		return w3cDomDocumentFromByteArray(baos.toByteArray());
	}

	private void check(int flags) throws Exception {
		org.w3c.dom.Document doc = fo(flags);
		NodeList inlines = doc.getElementsByTagNameNS(FO, "inline");
		Element small = null;
		for (int i = 0; i < inlines.getLength(); i++) {
			Element in = (Element) inlines.item(i);
			if ("80%".equals(in.getAttribute("font-size"))) small = in;
		}
		org.junit.Assert.assertNotNull("no small-caps inline: " + XmlUtils.w3CDomNodeToString(doc), small);
		assertEquals("the span must say what it was scaled by, so the line can be sized"
				+ " at the run's declared size",
				"0.8", small.getAttributeNS(DOCX4J, "small-caps"));
		// and the plain hint is not left behind in the output
		assertTrue("the plain hint must be removed once promoted",
				small.getAttribute(org.docx4j.fonts.RunFontSelector.HINT_SMALL_CAPS).length() == 0);
	}

	@Test
	public void aSmallCapsSpanCarriesItsScaleVisitor() throws Exception {
		check(Docx4J.FLAG_NONE);
	}

	@Test
	public void aSmallCapsSpanCarriesItsScaleXslt() throws Exception {
		check(Docx4J.FLAG_EXPORT_PREFER_XSL);
	}
}
