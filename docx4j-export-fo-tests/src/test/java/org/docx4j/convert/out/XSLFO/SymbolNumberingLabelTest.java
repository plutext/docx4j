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

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;

import java.io.ByteArrayOutputStream;

import org.docx4j.Docx4J;
import org.docx4j.XmlUtils;
import org.docx4j.convert.out.FOSettings;
import org.docx4j.openpackaging.packages.WordprocessingMLPackage;
import org.docx4j.openpackaging.parts.PartName;
import org.docx4j.openpackaging.parts.WordprocessingML.NumberingDefinitionsPart;
import org.docx4j.wml.Document;
import org.docx4j.wml.Numbering;
import org.junit.Test;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

/**
 * A bullet stated as a code point of a symbol font is drawn as the glyph, not as FOP's
 * {@code NOT_FOUND} box (CR-001 &#xa7;5.7).
 *
 * <p>{@code w:lvlText} U+F0B7 with the level's {@code w:rFonts} naming Symbol is the same
 * thing as {@code <w:sym w:font="Symbol" w:char="F0B7"/>}.  {@code RunFontSelector} maps
 * such a character to its Unicode equivalent and draws it in the substitute font that has
 * the glyph, which is why most of these bullets are right - but it is reached through the
 * run's {@code w:rFonts/@w:hAnsi}, and a numbering label's comes from the level's
 * {@code w:rPr}, which does not always survive to it.  Where it does not, the private-use
 * code point reached FOP unmapped, no installed face could draw it, and FOP painted
 * {@code #}: <b>923 lines of 116 of the three corpora's 449 documents</b> carried one,
 * and Word's own PDFs have none of them.
 *
 * <p>{@code XsltFOFunctions.symbolLabelFallback} maps a label which is still in the
 * private-use area after run font selection - which is exactly the case the symbol path
 * did not take - and gives it the substitute font.  Mapping every such label ahead of run
 * font selection instead was measured over the three corpora and is much worse: it
 * bypasses the mapping that works, turning correct bullets into the missing-symbol box,
 * and cost 0.025 of mean line parity on each corpus over 111 documents.
 *
 * @since 17.1.0
 */
public class SymbolNumberingLabelTest extends AbstractXSLFOTest {

	private static final String W = "xmlns:w=\"http://schemas.openxmlformats.org/wordprocessingml/2006/main\"";
	private static final String FO = "http://www.w3.org/1999/XSL/Format";

	/** numId 1: a Symbol bullet, U+F0B7 (the Symbol middle dot, Word's default bullet) */
	private static final String NUMBERING =
			"<w:numbering " + W + ">"
			+ "<w:abstractNum w:abstractNumId=\"0\"><w:multiLevelType w:val=\"hybridMultilevel\"/>"
			+ "<w:lvl w:ilvl=\"0\"><w:start w:val=\"1\"/><w:numFmt w:val=\"bullet\"/>"
			+ "<w:lvlText w:val=\"\uF0B7\"/><w:lvlJc w:val=\"left\"/>"
			+ "<w:pPr><w:ind w:left=\"720\" w:hanging=\"360\"/></w:pPr>"
			+ "<w:rPr><w:rFonts w:ascii=\"Symbol\" w:hAnsi=\"Symbol\" w:hint=\"default\"/></w:rPr>"
			+ "</w:lvl></w:abstractNum>"
			+ "<w:num w:numId=\"1\"><w:abstractNumId w:val=\"0\"/></w:num>"
			+ "</w:numbering>";

	private static final String BODY =
			"<w:document " + W + "><w:body>"
			+ "<w:p><w:pPr><w:numPr><w:ilvl w:val=\"0\"/><w:numId w:val=\"1\"/></w:numPr></w:pPr>"
			+ "<w:r><w:t>item</w:t></w:r></w:p>"
			+ "<w:sectPr><w:pgSz w:w=\"11906\" w:h=\"16838\"/>"
			+ "<w:pgMar w:top=\"1440\" w:right=\"1440\" w:bottom=\"1440\" w:left=\"1440\"/>"
			+ "</w:sectPr></w:body></w:document>";

	private org.w3c.dom.Document fo(int flags) throws Exception {
		WordprocessingMLPackage pkg = WordprocessingMLPackage.createPackage();
		NumberingDefinitionsPart ndp = new NumberingDefinitionsPart(new PartName("/word/numbering.xml"));
		ndp.setJaxbElement((Numbering) XmlUtils.unmarshalString(NUMBERING));
		pkg.getMainDocumentPart().addTargetPart(ndp);
		pkg.getMainDocumentPart().setJaxbElement((Document)XmlUtils.unmarshalString(BODY));
		FOSettings foSettings = Docx4J.createFOSettings();
		foSettings.setOpcPackage(pkg);
		foSettings.setApacheFopMime(FOSettings.INTERNAL_FO_MIME);
		ByteArrayOutputStream baos = new ByteArrayOutputStream();
		Docx4J.toFO(foSettings, baos, flags);
		return w3cDomDocumentFromByteArray(baos.toByteArray());
	}

	/** the text of the fo:list-item-label's block */
	private static String label(org.w3c.dom.Document doc) {
		NodeList labels = doc.getElementsByTagNameNS(FO, "list-item-label");
		assertFalse("no fo:list-item-label", labels.getLength() == 0);
		return ((Element) labels.item(0)).getTextContent();
	}

	private void check(int flags) throws Exception {
		String label = label(fo(flags));
		assertNotNull(label);
		assertFalse("the label must not reach FOP as an unmapped private-use code point"
				+ " (it paints # there): " + Integer.toHexString(label.codePointAt(0)),
				label.codePointAt(0) >= 0xF000 && label.codePointAt(0) <= 0xF8FF);
		// Symbol 0xB7 is the bullet, U+2022; the missing-symbol box is not an answer
		org.junit.Assert.assertEquals("Word draws Symbol 0xB7 as a bullet", "\u2022", label);
	}

	@Test
	public void aSymbolBulletIsMappedVisitor() throws Exception {
		check(Docx4J.FLAG_NONE);
	}

	@Test
	public void aSymbolBulletIsMappedXslt() throws Exception {
		check(Docx4J.FLAG_EXPORT_PREFER_XSL);
	}
}
