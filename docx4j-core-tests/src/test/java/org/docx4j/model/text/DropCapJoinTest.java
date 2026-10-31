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
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.docx4j.XmlUtils;
import org.docx4j.openpackaging.packages.WordprocessingMLPackage;
import org.docx4j.utils.ResourceUtils;
import org.docx4j.wml.Document;
import org.docx4j.wml.Style;
import org.junit.Test;

/**
 * WD REQ-521 (CR-069 phase 2): a drop cap's frame paragraph ({@code w:framePr/@w:dropCap}) holding at most one word is
 * joined to the paragraph after it, as the page reads, with the warning {@code paragraph.drop_cap_joined} at the frame's
 * address; {@code drop_caps: asis} (the {@code word-text} preset's) leaves the two paragraphs, as Word's text save writes
 * them (the {@code dpp} suite's {@code bisect/C02}, 31 October 2026).
 */
public class DropCapJoinTest {

	static final String W = "http://schemas.openxmlformats.org/wordprocessingml/2006/main";

	static final String FRAME = "<w:pPr><w:framePr w:dropCap=\"drop\" w:lines=\"3\" w:wrap=\"around\" w:vAnchor=\"text\" w:hAnchor=\"text\"/></w:pPr>";

	/** Word's drop cap: one block, the second paragraph's address and properties; the warning at the frame's. */
	@Test
	public void wordsDropCapIsJoinedToItsWord() throws Exception {
		TextResult r = Texts.of(load("text-extraction/dpp/bisect/C02.docx"), new TextOptions());
		assertEquals("[body/0, body/2, body/3]", addresses(r));
		TextBlock joined = r.blocks().get(1);
		assertTrue(joined.getText(), joined.getText().startsWith("The C02_ modern Recreational Boating"));
		assertEquals("[The C02_ modern Recreational Boating Safety Program traces its inception to the Federal Boat Safety Act of 1971.]",
				texts(joined.getRuns()));
		assertEquals(1, r.warnings().size());
		TextWarning w = r.warnings().get(0);
		assertEquals(TextWarning.DROP_CAP_JOINED, w.getKind());
		assertEquals("paragraph.drop_cap_joined", w.getKind());
		assertEquals("body/1", w.getAddress());
		assertEquals("/word/document.xml", w.getSelector());
		assertTrue(PlainText.of(r).contains("\nThe C02_ modern Recreational"));
		// every view joins it
		for (TextView view : TextView.values()) {
			TextResult v = Texts.of(load("text-extraction/dpp/bisect/C02.docx"), TextOptions.of(view));
			assertEquals(view.toString(), "[body/0, body/2, body/3]", addresses(v));
		}
	}

	/** drop_caps: asis, and the word-text preset, which sets it: the two paragraphs as the package holds them, no warning. */
	@Test
	public void asisLeavesTheTwoParagraphs() throws Exception {
		for (TextOptions o : new TextOptions[] { new TextOptions().dropCaps(TextOptions.DropCaps.ASIS),
				TextOptions.of(Collections.singletonMap("drop_caps", "asis")), new TextOptions().profile(TextOptions.Profile.WORD_TEXT) }) {
			TextResult r = Texts.of(load("text-extraction/dpp/bisect/C02.docx"), o);
			assertEquals("[body/0, body/1, body/2, body/3]", addresses(r));
			assertEquals("T", r.blocks().get(1).getText());
			assertTrue(r.warnings().isEmpty());
			assertTrue(PlainText.of(r).contains("\nT\nhe C02_ modern"));
		}
		// a caller's drop_caps overrides the preset's, before or after it
		TextOptions join = new TextOptions().dropCaps(TextOptions.DropCaps.JOIN).profile(TextOptions.Profile.WORD_TEXT);
		assertEquals(TextOptions.DropCaps.JOIN, join.getDropCaps());
		assertEquals("[body/0, body/2, body/3]", addresses(Texts.of(load("text-extraction/dpp/bisect/C02.docx"), join)));
	}

	/** The shapes REQ-521 leaves as they are: more than a word, no paragraph next, a frame that is no drop cap, a section's end. */
	@Test
	public void theShapesNotJoined() throws Exception {
		String next = "<w:p><w:r><w:t>next</w:t></w:r></w:p>";
		String[] bodies = {
				// two words
				"<w:p>" + FRAME + "<w:r><w:t>Once upon</w:t></w:r></w:p>" + next,
				// a table next
				"<w:p>" + FRAME + "<w:r><w:t>T</w:t></w:r></w:p><w:tbl><w:tr><w:tc><w:p><w:r><w:t>cell</w:t></w:r></w:p></w:tc></w:tr></w:tbl>" + next,
				// the end of the story
				next + "<w:p>" + FRAME + "<w:r><w:t>T</w:t></w:r></w:p>",
				// a frame with no drop cap
				"<w:p><w:pPr><w:framePr w:dropCap=\"none\" w:wrap=\"around\"/></w:pPr><w:r><w:t>T</w:t></w:r></w:p>" + next,
				"<w:p><w:pPr><w:framePr w:wrap=\"around\"/></w:pPr><w:r><w:t>T</w:t></w:r></w:p>" + next,
				// the frame ends a section: the paragraph next is in another
				"<w:p><w:pPr><w:framePr w:dropCap=\"drop\"/><w:sectPr/></w:pPr><w:r><w:t>T</w:t></w:r></w:p>" + next,
				// a block-level control next
				"<w:p>" + FRAME + "<w:r><w:t>T</w:t></w:r></w:p><w:sdt><w:sdtContent>" + next + "</w:sdtContent></w:sdt>",
		};
		for (String body : bodies) {
			TextResult r = Texts.of(document(body), new TextOptions());
			assertEquals(body, 2 + (body.contains("<w:tbl>") ? 1 : 0), r.blocks().size());
			assertTrue(body, r.warnings().isEmpty());
		}
		// the end of a cell
		TextResult cell = Texts.of(document("<w:tbl><w:tr><w:tc><w:p>" + FRAME + "<w:r><w:t>T</w:t></w:r></w:p></w:tc></w:tr></w:tbl>" + next),
				new TextOptions());
		assertTrue(cell.warnings().isEmpty());
	}

	/** dropCap margin, a style's frame, a bookmark between, a cell's paragraphs, an empty frame: each joined. */
	@Test
	public void theShapesJoined() throws Exception {
		String word = "<w:p><w:r><w:t xml:space=\"preserve\">he end</w:t></w:r></w:p>";
		String[] bodies = {
				"<w:p><w:pPr><w:framePr w:dropCap=\"margin\" w:lines=\"3\"/></w:pPr><w:r><w:t>T</w:t></w:r></w:p>" + word,
				"<w:p><w:pPr><w:pStyle w:val=\"DropCap\"/></w:pPr><w:r><w:t>T</w:t></w:r></w:p>" + word,
				"<w:p>" + FRAME + "<w:r><w:t>T</w:t></w:r></w:p><w:bookmarkStart w:id=\"0\" w:name=\"b\"/><w:bookmarkEnd w:id=\"0\"/>" + word,
				"<w:p>" + FRAME + "<w:r><w:t>T</w:t></w:r><w:bookmarkStart w:id=\"1\" w:name=\"c\"/></w:p>" + word,
		};
		for (String body : bodies) {
			WordprocessingMLPackage pkg = document(body);
			if (body.contains("DropCap")) style(pkg, "<w:style xmlns:w=\"" + W + "\" w:type=\"paragraph\" w:styleId=\"DropCap\">"
					+ "<w:name w:val=\"Drop Cap\"/><w:pPr><w:framePr w:dropCap=\"drop\" w:lines=\"3\"/></w:pPr></w:style>");
			TextResult r = Texts.of(pkg, new TextOptions());
			assertEquals(body, 1, r.blocks().size());
			assertEquals(body, "The end", r.blocks().get(0).getText());
			assertEquals(body, "body/" + (body.contains("w:bookmarkStart w:id=\"0\"") ? 3 : 1), r.blocks().get(0).getAddress());
			assertEquals(body, 1, r.warnings().size());
			assertEquals(body, "body/0", r.warnings().get(0).getAddress());
		}
		// in a cell, the paragraph next in the cell takes it; the cell's address
		TextResult cell = Texts.of(document("<w:tbl><w:tr><w:tc><w:p>" + FRAME + "<w:r><w:t>T</w:t></w:r></w:p>" + word + "</w:tc></w:tr></w:tbl>"),
				new TextOptions());
		assertEquals("The end", cell.blocks().get(0).getText());
		assertEquals("body/0/0/0/1", cell.blocks().get(0).getRows().get(0).getCells().get(0).getBlocks().get(0).getAddress());
		assertEquals("body/0/0/0/0", cell.warnings().get(0).getAddress());
		// a frame with no text (a picture's, say): at most one word, so joined
		TextResult empty = Texts.of(document("<w:p>" + FRAME + "</w:p>" + word), new TextOptions());
		assertEquals("[body/1]", addresses(empty));
		assertEquals(1, empty.warnings().size());
	}

	/**
	 * A frame whose mark the view deletes is one paragraph with the next already (CR-043's join, at the first
	 * paragraph's address): no drop-cap join and no warning; where the view keeps the mark, the drop cap is joined.
	 */
	@Test
	public void aDeletedMarkJoinsWithoutTheWarning() throws Exception {
		String body = "<w:p><w:pPr><w:framePr w:dropCap=\"drop\" w:lines=\"3\"/><w:rPr><w:del w:id=\"1\" w:author=\"A\""
				+ " w:date=\"2026-10-31T00:00:00Z\"/></w:rPr></w:pPr><w:r><w:t>T</w:t></w:r></w:p>"
				+ "<w:p><w:r><w:t xml:space=\"preserve\">he end</w:t></w:r></w:p>";
		TextResult accepted = Texts.of(document(body), TextOptions.of(TextView.ACCEPTED));
		assertEquals("[body/0]", addresses(accepted));
		assertEquals("The end", accepted.blocks().get(0).getText());
		assertTrue(accepted.warnings().isEmpty());
		TextResult original = Texts.of(document(body), TextOptions.of(TextView.ORIGINAL));
		assertEquals("[body/1]", addresses(original));
		assertEquals("The end", original.blocks().get(0).getText());
		assertEquals(1, original.warnings().size());
	}

	/**
	 * The letter's run is merged into its word's where the block model's formatting agrees, as runs merge throughout (the
	 * block model has no size); a bold or a cased letter stays a run of its own. The joined block's properties are the
	 * second paragraph's.
	 */
	@Test
	public void theLetterAndItsWord() throws Exception {
		String word = "<w:p><w:pPr><w:jc w:val=\"center\"/></w:pPr><w:r><w:t xml:space=\"preserve\">he end</w:t></w:r></w:p>";
		TextResult plain = Texts.of(document("<w:p>" + FRAME + "<w:r><w:rPr><w:sz w:val=\"80\"/></w:rPr><w:t>T</w:t></w:r></w:p>" + word),
				new TextOptions());
		assertEquals("[The end]", texts(plain.blocks().get(0).getRuns()));
		assertEquals("center", plain.blocks().get(0).getAlignment());
		TextResult bold = Texts.of(document("<w:p>" + FRAME + "<w:r><w:rPr><w:b/></w:rPr><w:t>T</w:t></w:r></w:p>" + word),
				new TextOptions());
		List<TextRun> runs = bold.blocks().get(0).getRuns();
		assertEquals("[T, he end]", texts(runs));
		assertTrue(runs.get(0).isBold());
		assertFalse(runs.get(1).isBold());
		TextResult caps = Texts.of(document("<w:p>" + FRAME + "<w:r><w:rPr><w:caps/></w:rPr><w:t>t</w:t></w:r></w:p>" + word),
				new TextOptions());
		assertEquals("The end\n", PlainText.of(caps));
	}

	/** The inspector's record: what is found in the frame is at the joined block's address, the one the text has. */
	@Test
	public void theInspectorFindsTheFrameInTheJoinedBlock() throws Exception {
		WordprocessingMLPackage pkg = document("<w:p>" + FRAME + "<w:r><w:rPr><w:color w:val=\"FFFFFF\"/></w:rPr><w:t>T</w:t></w:r></w:p>"
				+ "<w:p><w:r><w:t xml:space=\"preserve\">he end</w:t></w:r></w:p>");
		List<String> found = new ArrayList<String>();
		for (InspectFinding f : Inspect.of(pkg).findings())
			if (f.getKind().equals(InspectFinding.TEXT_NEAR_BACKGROUND)) found.add(f.getAddress());
		assertEquals("[body/1]", found.toString());
	}

	/** drop_caps by name, its default, its place in WD section 10's order and the word-text preset's value. */
	@Test
	public void theDropCapsOption() {
		assertEquals(TextOptions.DropCaps.JOIN, new TextOptions().getDropCaps());
		assertEquals("join", new TextOptions().toMap().get("drop_caps"));
		assertEquals(TextOptions.DropCaps.ASIS, TextOptions.of(Collections.singletonMap("drop_caps", "ASIS")).getDropCaps());
		assertEquals(TextOptions.DropCaps.ASIS, new TextOptions().profile(TextOptions.Profile.WORD_TEXT).getDropCaps());
		assertEquals(TextOptions.DropCaps.ASIS, new TextOptions().dropCaps(TextOptions.DropCaps.ASIS).copy().getDropCaps());
		assertEquals(TextOptions.NAMES.indexOf("toc") + 1, TextOptions.NAMES.indexOf("drop_caps"));
		try {
			TextOptions.of(Collections.singletonMap("drop_caps", "split"));
			throw new AssertionError("drop_caps: split was accepted");
		} catch (IllegalArgumentException expected) {
			assertTrue(expected.getMessage(), expected.getMessage().contains("drop_caps"));
		}
	}

	private static WordprocessingMLPackage load(String resource) throws Exception {
		try (InputStream is = ResourceUtils.getResource(resource)) {
			return WordprocessingMLPackage.load(is);
		}
	}

	private static WordprocessingMLPackage document(String body) throws Exception {
		WordprocessingMLPackage pkg = WordprocessingMLPackage.createPackage();
		pkg.getMainDocumentPart().setJaxbElement((Document) XmlUtils.unwrap(
				XmlUtils.unmarshalString("<w:document xmlns:w=\"" + W + "\"><w:body>" + body + "</w:body></w:document>")));
		return pkg;
	}

	private static void style(WordprocessingMLPackage pkg, String xml) throws Exception {
		pkg.getMainDocumentPart().getStyleDefinitionsPart().getJaxbElement().getStyle().add((Style) XmlUtils.unmarshalString(xml));
		pkg.getMainDocumentPart().getPropertyResolver().refresh();
	}

	private static String addresses(TextResult r) {
		List<String> out = new ArrayList<String>();
		for (TextBlock b : r.blocks()) out.add(b.getAddress());
		return out.toString();
	}

	private static String texts(List<TextRun> runs) {
		List<String> out = new ArrayList<String>();
		for (TextRun t : runs) out.add(t.getText());
		return out.toString();
	}
}
