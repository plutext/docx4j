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

import static org.docx4j.fonts.FontsTestSupport.SANS;
import static org.docx4j.fonts.FontsTestSupport.p;
import static org.docx4j.fonts.FontsTestSupport.packageWith;
import static org.docx4j.fonts.FontsTestSupport.plain;
import static org.docx4j.fonts.FontsTestSupport.styles;
import static org.docx4j.fonts.FontsTestSupport.xslFoSelector;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import org.docx4j.XmlUtils;
import org.docx4j.convert.out.common.writer.SymbolUtils;
import org.docx4j.openpackaging.packages.WordprocessingMLPackage;
import org.docx4j.wml.P;
import org.docx4j.wml.R;
import org.docx4j.wml.Text;
import org.junit.Test;
import org.w3c.dom.DocumentFragment;
import org.w3c.dom.Element;
import org.w3c.dom.Node;

/**
 * A symbol-font character with no Unicode replacement is drawn as the missing-symbol box,
 * in a font which has that box (CR-001, non-embedded fonts).
 *
 * <p>The box is docx4j's own mark, not a character of the symbol font, and
 * {@code symbolSetAttribute} returned for it without setting a {@code font-family} at all.
 * {@code glyphFallback} then declined to look at the span - it returns early for one with
 * no family - so the box was drawn as notdef in whatever face the context supplied. For a
 * numbering label, which is an {@code fo:block} of its own outside the paragraph, that is
 * FOP's base-14 default: three corpus documents drew their bullets as a blank Helvetica
 * notdef, and named a font the PDF does not embed.</p>
 *
 * <p>The box is the right mark. Measured on Word's own renderings of those documents,
 * Symbol 0x7F and 0xFF are an open square (0.60 and 0.50 em), and U+2B1A, which SymbolMT
 * has no glyph for, Word draws as nothing at all - so a bullet would have been wrong for
 * all three.</p>
 *
 * @since 17.1.1
 */
public class RunFontSelectorMissingSymbolTest {

	/** Symbol 0x7F, which SymbolMapper has no Unicode replacement for; and 0xB7, which it has. */
	private static final String SYMBOL_UNMAPPED = "&#xF07F;";
	private static final String SYMBOL_BULLET = "&#xF0B7;";

	private static final int BOX = SymbolUtils.MISSING_SYMBOL.codePointAt(0);

	private static WordprocessingMLPackage pkg(String text) throws Exception {
		return packageWith(
				styles("<w:rFonts w:ascii=\"" + SANS + "\" w:hAnsi=\"" + SANS + "\"/><w:sz w:val=\"22\"/>",
						null),
				null, null,
				p("<w:rFonts w:ascii=\"Symbol\" w:hAnsi=\"Symbol\" w:cs=\"Symbol\"/>", text),
				SANS, SANS);
	}

	/** the spans the selector produced for the first paragraph's first run */
	private static Element[] spans(WordprocessingMLPackage pkg) throws Exception {
		P p = (P) pkg.getMainDocumentPart().getContent().get(0);
		R r = (R) p.getContent().get(0);
		Object o = xslFoSelector(pkg).fontSelector(p.getPPr(), r.getRPr(),
				(Text) XmlUtils.unwrap(r.getContent().get(0)));
		java.util.List<Element> out = new java.util.ArrayList<Element>();
		if (o instanceof DocumentFragment) {
			for (Node n = ((DocumentFragment) o).getFirstChild(); n != null; n = n.getNextSibling()) {
				if (n instanceof Element) out.add((Element) n);
			}
		}
		return out.toArray(new Element[out.size()]);
	}

	@Test
	public void theBoxIsWhatIsDrawn() throws Exception {
		Element[] spans = spans(pkg(SYMBOL_UNMAPPED));
		assertEquals("one span", 1, spans.length);
		assertEquals("Symbol 0x7F has no replacement, so the missing-symbol box is drawn",
				SymbolUtils.MISSING_SYMBOL, spans[0].getTextContent());
	}

	@Test
	public void theBoxIsDrawnInAFontWhichHasIt() throws Exception {
		Element[] spans = spans(pkg(SYMBOL_UNMAPPED));
		assertEquals("one span", 1, spans.length);

		String family = plain(spans[0].getAttribute("font-family"));
		assertNotNull(family);
		assertFalse("the missing-symbol box has no font-family, so it is drawn as notdef -"
				+ " and in a numbering label, in a base-14 font the PDF does not embed",
				family.isEmpty());

		PhysicalFont pf = PhysicalFonts.get(family);
		assertNotNull(family + " is not a physical font", pf);
		assertTrue(family + " cannot draw the missing-symbol box",
				FontFallback.covers(pf, new int[] { BOX }));
	}

	/** A symbol code which does have a replacement is unaffected: it is not the box, and
	 *  it keeps the face the symbol substitution chose for it. */
	@Test
	public void aMappedSymbolIsUntouched() throws Exception {
		Element[] spans = spans(pkg(SYMBOL_BULLET));
		assertEquals("one span", 1, spans.length);
		assertFalse("Symbol 0xB7 has a replacement, so it is not the missing-symbol box",
				SymbolUtils.MISSING_SYMBOL.equals(spans[0].getTextContent()));
		assertFalse("no font-family on a mapped symbol",
				spans[0].getAttribute("font-family").isEmpty());
	}
}
