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
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.apache.pdfbox.Loader;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.text.PDFTextStripper;
import org.apache.pdfbox.text.TextPosition;
import org.docx4j.Docx4J;
import org.docx4j.Docx4jProperties;
import org.docx4j.XmlUtils;
import org.docx4j.fop.wordlayout.WordLayoutCustomizer;
import org.docx4j.openpackaging.packages.WordprocessingMLPackage;
import org.docx4j.wml.Document;
import org.junit.After;
import org.junit.Test;

/**
 * What a {@code w:tab} puts in the PDF's text layer: the space Word writes for its
 * advance, and, where its stop has a leader, the leader's own characters.
 *
 * <p>Word draws every leader as characters, in the paragraph mark's font and size: the
 * {@code tab-leader-kinds} golden gives {@code dot} a full stop, {@code middleDot} U+00B7,
 * {@code hyphen} a hyphen, and <b>both</b> {@code underscore} and {@code heavy} an
 * underscore, and its page carries no stroked or filled path at all.  XSL FO has no
 * repeating glyph but the dot, so docx4j asked FOP for a <em>rule</em> for the other three
 * - a drawn line of the right length in the right place, with nothing in the text layer at
 * all (CR-001 batch 45).</p>
 *
 * <p>Word writes the tab itself as one space glyph in the paragraph mark's font, in a text
 * object of its own, and one at each end of a leader run - read out of the content streams,
 * {@code BT /F2 11.04 Tf 1 0 0 1 136.13 718.99 Tm [( )] TJ ET} between
 * {@code P02 left none} and {@code after none}, and 154.87 and 564.22 around a corpus
 * entry's underscore run.  docx4j's stream is continuous, so it writes one too
 * ({@code WordLayoutCustomizer.tabSpaces}).</p>
 *
 * <p>The test renders the same document with the rules off and on and asserts both halves:
 * the text layer gains the characters, and <b>no ink glyph of the text moves</b>.</p>
 *
 * @since 17.1.1
 */
public class TabLeaderTextLayerTest {

	private static final String W = "xmlns:w=\"http://schemas.openxmlformats.org/wordprocessingml/2006/main\"";

	private static final String SECT_PR = "<w:sectPr><w:pgSz w:w=\"11906\" w:h=\"16838\"/>"
			+ "<w:pgMar w:top=\"1440\" w:right=\"1440\" w:bottom=\"1440\" w:left=\"1440\"/></w:sectPr>";

	private static final String BEFORE = "before";
	private static final String AFTER = "after";

	/** An explicit face, so that the widths this test's leader is phased against do not
	 *  move with {@code docx4j.fonts.defaultTheme}: the runs carried no w:rFonts and took
	 *  the document default, which was Calibri until 17.1.1 made it Aptos.  In Arimo (the
	 *  Aptos substitute) "before" is wide enough that the leader opens exactly on Word's
	 *  grid, the phase is 0, and the lead space this test is about is correctly not
	 *  written - a true answer to a question the test did not mean to ask. */
	private static final String FONT = "<w:rPr><w:rFonts w:ascii=\"Calibri\" w:hAnsi=\"Calibri\"/></w:rPr>";

	/** one paragraph: text, a tab to a stop with this leader, text.  The stop is
	 *  left-aligned, so that a dot leader is laid out against it by the line manager
	 *  rather than taking the stretching leader a table-of-contents entry takes
	 *  ({@code XsltFOFunctions.isTocDotLeader}, which wants a <b>right</b> dot stop). */
	private static String paragraph(String leader) {
		return "<w:p><w:pPr><w:tabs><w:tab w:val=\"left\""
				+ (leader == null ? "" : " w:leader=\"" + leader + "\"")
				+ " w:pos=\"9000\"/></w:tabs></w:pPr>"
				+ "<w:r>" + FONT + "<w:t>" + BEFORE + "</w:t></w:r>"
				+ "<w:r>" + FONT + "<w:tab/><w:t>" + AFTER + "</w:t></w:r></w:p>";
	}

	@After
	public void restore() {
		Docx4jProperties.getProperties().remove(WordLayoutCustomizer.LEADER_CHARACTERS);
		Docx4jProperties.getProperties().remove(WordLayoutCustomizer.TAB_SPACES);
	}

	private static byte[] pdf(String leader) throws Exception {
		WordprocessingMLPackage pkg = WordprocessingMLPackage.createPackage();
		pkg.getMainDocumentPart().setJaxbElement((Document) XmlUtils.unmarshalString(
				"<w:document " + W + "><w:body>" + paragraph(leader) + SECT_PR
				+ "</w:body></w:document>"));
		ByteArrayOutputStream baos = new ByteArrayOutputStream();
		Docx4J.toPDF(pkg, baos);
		return baos.toByteArray();
	}

	private static List<TextPosition> glyphs(byte[] pdf) throws Exception {
		final List<TextPosition> out = new ArrayList<TextPosition>();
		try (PDDocument doc = Loader.loadPDF(pdf)) {
			PDFTextStripper stripper = new PDFTextStripper() {
				@Override
				protected void writeString(String text, List<TextPosition> positions) throws IOException {
					out.addAll(positions);
				}
			};
			stripper.setSortByPosition(true);
			stripper.getText(doc);
		}
		return out;
	}

	private static String text(List<TextPosition> glyphs) {
		StringBuilder b = new StringBuilder();
		for (TextPosition tp : glyphs) b.append(tp.getUnicode());
		return b.toString();
	}

	/** the glyphs of the paragraph's own words, which may not move */
	private static List<TextPosition> words(List<TextPosition> glyphs) {
		List<TextPosition> out = new ArrayList<TextPosition>();
		for (TextPosition tp : glyphs) {
			String u = tp.getUnicode();
			if (u == null || u.trim().length() == 0) continue;
			if ("_".equals(u) || "-".equals(u) || ".".equals(u)) continue;
			out.add(tp);
		}
		return out;
	}

	private static void sameWords(List<TextPosition> a, List<TextPosition> b) {
		assertEquals("the same word glyphs", text(words(a)), text(words(b)));
		for (int i = 0; i < words(a).size(); i++) {
			TextPosition x = words(a).get(i), y = words(b).get(i);
			assertEquals("glyph " + i + " (" + x.getUnicode() + ") x",
					x.getXDirAdj(), y.getXDirAdj(), 0.001);
			assertEquals("glyph " + i + " (" + x.getUnicode() + ") y",
					x.getYDirAdj(), y.getYDirAdj(), 0.001);
		}
	}

	@Test
	public void anUnderscoreLeaderIsWrittenAsItsCharacters() throws Exception {

		Docx4jProperties.setProperty(WordLayoutCustomizer.LEADER_CHARACTERS, false);
		Docx4jProperties.setProperty(WordLayoutCustomizer.TAB_SPACES, false);
		List<TextPosition> rule = glyphs(pdf("underscore"));

		Docx4jProperties.setProperty(WordLayoutCustomizer.LEADER_CHARACTERS, true);
		List<TextPosition> chars = glyphs(pdf("underscore"));

		assertEquals("a rule puts nothing in the text layer", BEFORE + AFTER, text(rule));
		assertTrue("the leader's characters: " + text(chars),
				text(chars).startsWith(BEFORE + "_") && text(chars).endsWith("_" + AFTER));
		sameWords(rule, chars);
	}

	/**
	 * Every other kind is its own character too, and the {@code tab-leader-kinds} golden
	 * says which: a hyphen for {@code hyphen}, U+00B7 for {@code middleDot}, and an
	 * <b>underscore</b> for {@code heavy} - Word draws that one as characters like the
	 * rest, and its page carries no stroked or filled path at all.
	 */
	@Test
	public void everyOtherKindIsItsOwnCharacter() throws Exception {
		Docx4jProperties.setProperty(WordLayoutCustomizer.TAB_SPACES, false);
		Docx4jProperties.setProperty(WordLayoutCustomizer.LEADER_CHARACTERS, true);
		assertTrue(text(glyphs(pdf("hyphen"))).startsWith(BEFORE + "-"));
		assertTrue(text(glyphs(pdf("middleDot"))).startsWith(BEFORE + "\u00b7"));
		assertTrue("heavy is an underscore run: " + text(glyphs(pdf("heavy"))),
				text(glyphs(pdf("heavy"))).startsWith(BEFORE + "_"));
	}

	/** With the rule off, heavy falls back to the drawn rule, which puts nothing in the
	 *  text layer; that is the path {@code LBP.ruleArea} still serves. */
	@Test
	public void withTheRuleOffALeaderIsADrawnRule() throws Exception {
		Docx4jProperties.setProperty(WordLayoutCustomizer.TAB_SPACES, false);
		Docx4jProperties.setProperty(WordLayoutCustomizer.LEADER_CHARACTERS, false);
		assertEquals(BEFORE + AFTER, text(glyphs(pdf("heavy"))));
		assertEquals(BEFORE + AFTER, text(glyphs(pdf("underscore"))));
	}

	/** a tab which reaches a stop with no leader carries a space of its whole advance */
	@Test
	public void aTabWithNoLeaderIsASpace() throws Exception {

		Docx4jProperties.setProperty(WordLayoutCustomizer.TAB_SPACES, false);
		List<TextPosition> jump = glyphs(pdf(null));

		Docx4jProperties.setProperty(WordLayoutCustomizer.TAB_SPACES, true);
		List<TextPosition> space = glyphs(pdf(null));

		assertEquals("the tab was a jump", BEFORE + AFTER, text(jump));
		assertEquals("the tab is a space", BEFORE + " " + AFTER, text(space));
		sameWords(jump, space);
	}

	/** and a leader run has one at each end, as Word's has */
	@Test
	public void aLeaderRunHasASpaceAtEachEnd() throws Exception {

		Docx4jProperties.setProperty(WordLayoutCustomizer.LEADER_CHARACTERS, true);
		Docx4jProperties.setProperty(WordLayoutCustomizer.TAB_SPACES, false);
		List<TextPosition> abutted = glyphs(pdf("dot"));

		Docx4jProperties.setProperty(WordLayoutCustomizer.TAB_SPACES, true);
		List<TextPosition> spaced = glyphs(pdf("dot"));

		assertTrue("the run abutted its text: " + text(abutted),
				text(abutted).startsWith(BEFORE + ".") && text(abutted).endsWith("." + AFTER));
		assertTrue("a space at each end: " + text(spaced),
				text(spaced).startsWith(BEFORE + " .") && text(spaced).endsWith(". " + AFTER));
		sameWords(abutted, spaced);
	}

}
