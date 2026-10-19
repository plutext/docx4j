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
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.docx4j.XmlUtils;
import org.docx4j.openpackaging.packages.WordprocessingMLPackage;
import org.docx4j.openpackaging.parts.JaxbXmlPart;
import org.docx4j.openpackaging.parts.Part;
import org.junit.BeforeClass;
import org.junit.Test;

/** The text model's statements that the probe's token rows do not pin on their own (CR-046 phase 1). */
public class TextsTest {

	private static final String DEL = " w:id=\"1\" w:author=\"A\" w:date=\"2026-01-01T00:00:00Z\"";

	private static WordprocessingMLPackage probe;

	@BeforeClass
	public static void loadProbe() throws Exception {
		probe = WordprocessingMLPackage.load(probePath().toFile());
	}

	static Path probePath() {
		return ViewAgreementTest.resources().resolve("text-extraction/phase0/extraction-probe.docx");
	}

	private static String text(WordprocessingMLPackage pkg, TextOptions options) {
		return PlainText.of(Texts.of(pkg, options));
	}

	private static TextBlock blockWith(TextResult result, String token) {
		for (TextBlock b : result.blocks()) if (b.getText().contains(token)) return b;
		throw new AssertionError("no block holds " + token);
	}

	/** REQ-401: three views, accepted the default; current a synonym. */
	@Test
	public void viewsAndDefault() {
		assertEquals(TextView.ACCEPTED, new TextOptions().getView());
		assertEquals(TextView.ACCEPTED, TextView.of("current"));
		assertEquals(TextView.MARKUP, TextView.of("markup"));
		try {
			TextView.of("final");
			fail();
		} catch (IllegalArgumentException expected) {
			assertTrue(expected.getMessage().contains("REQ-401"));
		}
	}

	/** REQ-204: the package is unchanged by any view or scope. */
	@Test
	public void packageUnchanged() throws Exception {
		for (Part part : probe.getParts().getParts().values()) if (part instanceof JaxbXmlPart) ((JaxbXmlPart<?>) part).getJaxbElement();
		Map<String, String> before = snapshot(probe);
		assertTrue(before.containsKey("/word/document.xml") && before.containsKey("/word/styles.xml"));
		for (TextView view : TextView.values()) {
			Texts.of(probe, TextOptions.of(view).include(TextOptions.Include.HEADERS, TextOptions.Include.FOOTERS)
					.comments(true).hidden(true).fieldCodes(true).listLabels(true));
		}
		assertEquals(before, snapshot(probe));
	}

	private static Map<String, String> snapshot(WordprocessingMLPackage pkg) {
		Map<String, String> out = new HashMap<String, String>();
		for (Part part : pkg.getParts().getParts().values()) {
			// the WordprocessingML parts
			if (part instanceof JaxbXmlPart && part.getPartName().getName().startsWith("/word/")) {
				// namespace declarations aside: MOXy orders them differently from one marshal to the next
				out.put(part.getPartName().getName(), XmlUtils.marshaltoString(((JaxbXmlPart<?>) part).getJaxbElement(), true, true,
						((JaxbXmlPart<?>) part).getJAXBContext()).replaceAll("\\s+xmlns(:[\\w.-]+)?=\"[^\"]*\"", ""));
			}
		}
		return out;
	}

	/** REQ-204: a package with no styles part is not given one (asking for a property resolver would add it). */
	@Test
	public void noStylesPartAdded() throws Exception {
		WordprocessingMLPackage pkg = MiniDocx.body("<w:p><w:r><w:t>TEXT</w:t></w:r></w:p>").load();
		int parts = pkg.getParts().getParts().size();
		assertNull(pkg.getMainDocumentPart().getStyleDefinitionsPart(false));
		for (TextView view : TextView.values()) assertEquals("TEXT\n", text(pkg, TextOptions.of(view).listLabels(true)));
		assertNull(pkg.getMainDocumentPart().getStyleDefinitionsPart(false));
		assertEquals(parts, pkg.getParts().getParts().size());
	}

	/** REQ-203: a strict document extracts as its transitional equivalent (docx4j converts it on load). */
	@Test
	public void strictAsTransitional() throws Exception {
		WordprocessingMLPackage strict = WordprocessingMLPackage.load(ViewAgreementTest.resources().resolve("strict/strict-sample-docx.docx").toFile());
		String text = text(strict, new TextOptions());
		assertTrue(text.trim().length() > 20);
		ByteArrayOutputStream saved = new ByteArrayOutputStream();
		strict.save(saved);
		WordprocessingMLPackage transitional = WordprocessingMLPackage.load(new ByteArrayInputStream(saved.toByteArray()));
		assertEquals(text, text(transitional, new TextOptions()));
	}

	/** REQ-402, REQ-405: a deleted mark joins in accepted, under the first paragraph's address and paraId; two blocks otherwise. */
	@Test
	public void joinedParagraph() throws Exception {
		WordprocessingMLPackage pkg = MiniDocx.body("<w:p w14:paraId=\"0000000A\"><w:pPr><w:rPr><w:del" + DEL + "/></w:rPr></w:pPr><w:r><w:t>FIRST</w:t></w:r></w:p>"
				+ "<w:p w14:paraId=\"0000000B\"><w:pPr><w:pStyle w:val=\"Heading1\"/></w:pPr><w:r><w:t>SECOND</w:t></w:r></w:p>").load();
		TextResult accepted = Texts.of(pkg, new TextOptions());
		assertEquals(1, accepted.blocks().size());
		TextBlock joined = accepted.blocks().get(0);
		assertEquals("FIRSTSECOND", joined.getText());
		assertEquals("body/0", joined.getAddress());
		assertEquals("0000000A", joined.getParaId());
		assertEquals("Heading1", joined.getStyle()); // the joined paragraph keeps the second one's properties
		assertEquals(1, joined.getRevisions());
		assertEquals("FIRST\nSECOND\n", text(pkg, TextOptions.of(TextView.ORIGINAL)));
		assertEquals("FIRST\nSECOND\n", text(pkg, TextOptions.of(TextView.MARKUP))); // REQ-403: no structure resolved, no marker
	}

	/** REQ-601, REQ-602, REQ-603: run content mapped, nothing normalised or trimmed; a line per paragraph and break. */
	@Test
	public void characters() throws Exception {
		WordprocessingMLPackage pkg = MiniDocx.body("<w:p><w:r><w:t xml:space=\"preserve\">  lead</w:t><w:tab/><w:t>A</w:t><w:br/><w:t>B</w:t>"
				+ "<w:br w:type=\"page\"/><w:t>C</w:t><w:cr/><w:t>D</w:t><w:noBreakHyphen/><w:softHyphen/><w:sym w:font=\"Wingdings\" w:char=\"F0FC\"/>"
				+ "<w:ptab w:relativeTo=\"margin\" w:alignment=\"right\" w:leader=\"none\"/><w:t xml:space=\"preserve\">e\u0301 \u00A0trail  </w:t></w:r></w:p>"
				+ "<w:p/><w:p><w:r><w:t>last</w:t></w:r></w:p>").load();
		assertEquals("  lead\tA\nB\nC\nD\u2011\u00AD\uF0FC\te\u0301 \u00A0trail  \n\nlast\n", text(pkg, new TextOptions()));
	}

	/** REQ-505: codes excluded unless asked for; a field with no result contributes nothing, is a field with a null result, and warns. */
	@Test
	public void fields() throws Exception {
		String xml = "<w:p><w:r><w:t xml:space=\"preserve\">Page </w:t></w:r><w:r><w:fldChar w:fldCharType=\"begin\"/></w:r><w:r><w:instrText xml:space=\"preserve\"> PAGE </w:instrText></w:r>"
				+ "<w:r><w:fldChar w:fldCharType=\"end\"/></w:r></w:p>"
				+ "<w:p><w:r><w:fldChar w:fldCharType=\"begin\"/></w:r><w:r><w:instrText xml:space=\"preserve\"> QUOTE x </w:instrText></w:r><w:r><w:fldChar w:fldCharType=\"separate\"/></w:r>"
				+ "<w:r><w:t>RESULT</w:t></w:r><w:r><w:fldChar w:fldCharType=\"end\"/></w:r></w:p>"
				+ "<w:p><w:fldSimple w:instr=\" AUTHOR \"/></w:p>";
		WordprocessingMLPackage pkg = MiniDocx.body(xml).load();
		TextResult result = Texts.of(pkg, new TextOptions());
		assertEquals("Page \nRESULT\n\n", PlainText.of(result));
		TextField page = result.blocks().get(0).getFields().get(0);
		assertEquals(" PAGE ", page.getInstr());
		assertNull(page.getResult());
		assertEquals("RESULT", result.blocks().get(1).getFields().get(0).getResult());
		assertNull(result.blocks().get(2).getFields().get(0).getResult());
		List<String> warnings = new ArrayList<String>();
		for (TextWarning w : result.warnings()) warnings.add(w.toString());
		assertEquals(List.of("field.no_result at body/0", "field.no_result at body/2"), warnings);
		assertEquals("Page  PAGE \n QUOTE x RESULT\n AUTHOR \n", text(pkg, new TextOptions().fieldCodes(true)));
	}

	/** REQ-503, REQ-707: [n] in reference order; a custom mark gets no number but its note is read; note_references: false drops the inline marker only. */
	@Test
	public void notes() throws Exception {
		WordprocessingMLPackage pkg = MiniDocx.body("<w:p><w:r><w:t>A</w:t></w:r><w:r><w:footnoteReference w:id=\"7\"/></w:r>"
				+ "<w:r><w:t xml:space=\"preserve\"> B</w:t></w:r><w:r><w:footnoteReference w:customMarkFollows=\"1\" w:id=\"8\"/></w:r><w:r><w:t>*</w:t></w:r>"
				+ "<w:r><w:t xml:space=\"preserve\"> C</w:t></w:r><w:r><w:footnoteReference w:id=\"9\"/></w:r></w:p>")
				.footnotes("<w:footnote w:type=\"separator\" w:id=\"-1\"><w:p><w:r><w:separator/></w:r></w:p></w:footnote>"
						+ "<w:footnote w:id=\"9\"><w:p><w:r><w:footnoteRef/></w:r><w:r><w:t xml:space=\"preserve\"> NOTE_NINE</w:t></w:r></w:p></w:footnote>"
						+ "<w:footnote w:id=\"7\"><w:p><w:r><w:footnoteRef/></w:r><w:r><w:t xml:space=\"preserve\"> NOTE_SEVEN</w:t></w:r></w:p></w:footnote>"
						+ "<w:footnote w:id=\"8\"><w:p><w:r><w:t>* NOTE_EIGHT</w:t></w:r></w:p></w:footnote>").load();
		assertEquals("A[1] B* C[2]\n[1] NOTE_SEVEN\n* NOTE_EIGHT\n[2] NOTE_NINE\n", text(pkg, new TextOptions()));
		assertEquals("A B* C\n[1] NOTE_SEVEN\n* NOTE_EIGHT\n[2] NOTE_NINE\n", text(pkg, new TextOptions().noteReferences(false)));
		assertEquals("A B* C\n", text(pkg, new TextOptions().notes(false)));
		TextBlock note = blockWith(Texts.of(pkg, new TextOptions()), "NOTE_NINE");
		assertEquals(TextBlock.Story.FOOTNOTE, note.getStory());
		assertEquals(2, note.getNoteNumber());
		assertEquals("footnote:9/0", note.getAddress());
		assertFalse(note.getText().contains("[")); // the number is a field, not text (REQ-707)
	}

	/** REQ-501, REQ-606: the main story, then footnotes, then endnotes; headers, footers and comments only when included. */
	@Test
	public void storyOrder() {
		String text = text(probe, new TextOptions());
		int lastBody = text.indexOf("Case 54:");
		int footnote = text.indexOf("[1] TC17_NOTE_BASE");
		int endnote = text.indexOf("[1] EN27_NOTE_TEXT");
		assertTrue(lastBody >= 0 && footnote > lastBody && endnote > footnote);
		assertFalse(text.contains("HDR26_") || text.contains("FTR26_") || text.contains("{>>"));
	}

	/** REQ-502: per section, the headers Word displays before its body and the footers after; each once; no orphan. */
	@Test
	public void headersBySection() {
		String text = text(probe, new TextOptions().include(TextOptions.Include.HEADERS, TextOptions.Include.FOOTERS));
		int first = text.indexOf("HDR26_FIRST"), dflt = text.indexOf("HDR26_DEFAULT"), even = text.indexOf("HDR26_EVEN");
		int body = text.indexOf("Text extraction probe"), endOfSection1 = text.indexOf("The last paragraph of section 1.");
		int footer = text.indexOf("FTR26_DEFAULT"), section2 = text.indexOf("HDR26_SECTION2"), body2 = text.indexOf("The first paragraph of section 2.");
		assertTrue(0 <= first && first < dflt && dflt < even && even < body);
		assertTrue(endOfSection1 < footer && footer < section2 && section2 < body2);
		assertEquals(text.lastIndexOf("HDR26_EVEN"), even);
		assertFalse(text.contains("HDR26_ORPHAN"));
		// REQ-519: a header's block is header:<rId>/<n>, a footer's footer:<rId>/<n> (CR-050 phase 0 found the part's
		// content's toString() before them)
		TextResult result = Texts.of(probe, new TextOptions().include(TextOptions.Include.HEADERS, TextOptions.Include.FOOTERS));
		int stories = 0;
		for (TextBlock b : result.blocks()) {
			if (b.getStory() != TextBlock.Story.HEADER && b.getStory() != TextBlock.Story.FOOTER) continue;
			stories++;
			assertTrue(b.getAddress(), b.getAddress().matches("(header|footer):rId\\d+/\\d+"));
		}
		assertTrue(stories >= 5);
	}

	/** REQ-504: comments in markup at the end of their range, replies after them, resolved flagged; otherwise only under comments: true. */
	@Test
	public void comments() {
		TextResult markup = Texts.of(probe, TextOptions.of(TextView.MARKUP));
		assertTrue(PlainText.of(markup).contains("CMT28_ANCHOR{>>CMT28_COMMENT_TEXT<<}{>>CMT28_REPLY<<} and CMT28_RESOLVED_ANCHOR{>>CMT28_RESOLVED_COMMENT<<}"));
		TextComment resolved = null, reply = null;
		for (TextComment c : markup.comments().values()) {
			if (c.getText().contains("CMT28_RESOLVED_COMMENT")) resolved = c;
			if (c.getText().contains("CMT28_REPLY")) reply = c;
		}
		assertTrue(resolved.isResolved());
		assertEquals("0", reply.getParentId());
		assertFalse(PlainText.of(Texts.of(probe, new TextOptions())).contains("{>>"));
		assertTrue(PlainText.of(Texts.of(probe, new TextOptions().comments(true))).contains("{>>CMT28_COMMENT_TEXT<<}"));
	}

	/** REQ-507: hidden text and the hidden paragraph included and flagged under hidden: true. */
	@Test
	public void hiddenText() {
		TextResult result = Texts.of(probe, new TextOptions().hidden(true));
		TextBlock para = blockWith(result, "HID18_HIDDEN_PARA");
		assertTrue(para.isHidden());
		boolean flagged = false;
		for (TextRun r : blockWith(result, "HID18_VANISH").getRuns()) if (r.getText().contains("HID18_VANISH")) flagged = r.isHidden();
		assertTrue(flagged);
	}

	/** REQ-508: placeholder text included and flagged; placeholders: false leaves it out. */
	@Test
	public void placeholders() {
		assertTrue(blockWith(Texts.of(probe, new TextOptions()), "PLC22_PLACEHOLDER").isPlaceholder());
		assertFalse(text(probe, new TextOptions().placeholders(false)).contains("PLC22_PLACEHOLDER"));
	}

	/** REQ-510: no list labels by default; the numbering model's under list_labels: true. */
	@Test
	public void listLabels() {
		assertTrue(text(probe, new TextOptions()).contains("\nLST29_ITEM_A\n"));
		String labelled = text(probe, new TextOptions().listLabels(true));
		assertTrue(labelled.contains("\n1.\tLST29_ITEM_A\n2.\tLST29_ITEM_B\n3.\tLST29_ITEM_C\n"));
	}

	/** REQ-518: a box's blocks after its anchor, addressed through it; after the table for a box in a cell. */
	@Test
	public void textBoxes() {
		TextResult result = Texts.of(probe, new TextOptions());
		TextBlock box = blockWith(result, "TBX25_FLOAT");
		assertEquals(TextBlock.Story.TEXT_BOX, box.getStory());
		assertTrue(box.isTextbox());
		assertTrue(box.getAddress(), box.getAddress().matches("body/\\d+/textbox:0/0"));
		int at = result.blocks().indexOf(box);
		assertEquals(box.getAddress().replaceAll("/textbox:.*", ""), result.blocks().get(at - 1).getAddress());
		TextBlock inCell = blockWith(result, "TBX25_IN_CELL");
		TextBlock before = result.blocks().get(result.blocks().indexOf(inCell) - 1);
		assertEquals(TextBlock.Kind.TABLE, before.getKind());
		assertTrue(inCell.getAddress().startsWith(before.getAddress() + "/"));
	}

	/** REQ-702, REQ-706: CriticMarkup, the detail in the marker's opening; runs carry their revision. */
	@Test
	public void markup() {
		String type = text(probe, TextOptions.of(TextView.MARKUP));
		assertTrue(type.contains("TC9_BASE {++TC9_INSERTED ++}{--TC9_DELETED--}"));
		assertFalse(type.contains("{~~"));
		String author = text(probe, TextOptions.of(TextView.MARKUP).markupDetail(TextOptions.MarkupDetail.AUTHOR));
		assertTrue(author.contains("{++Probe: TC9_INSERTED ++}{--Probe: TC9_DELETED--}"));
		String date = text(probe, TextOptions.of(TextView.MARKUP).markupDetail(TextOptions.MarkupDetail.AUTHOR_DATE));
		assertTrue(date.contains("{++Probe, 2026-10-17T00:00:00Z: TC9_INSERTED ++}"));
		TextBlock block = blockWith(Texts.of(probe, TextOptions.of(TextView.MARKUP)), "TC9_INSERTED");
		TextRevision inserted = null;
		for (TextRun r : block.getRuns()) if (r.getText().contains("TC9_INSERTED")) inserted = r.getRevision();
		assertEquals(TextRevision.Type.INSERTION, inserted.getType());
		assertEquals("Probe", inserted.getAuthor());
		assertEquals("2026-10-17T00:00:00Z", inserted.getDate());
		assertEquals(2, block.getRevisions());
		for (TextRun r : blockWith(Texts.of(probe, new TextOptions()), "TC9_INSERTED").getRuns()) assertNull(r.getRevision());
	}

	/** REQ-801, REQ-802: warnings are metadata, with no document text. */
	@Test
	public void warningsCarryNoText() throws Exception {
		TextResult result = Texts.of(probe, TextOptions.of(TextView.MARKUP));
		assertFalse(result.warnings().isEmpty());
		for (TextWarning w : result.warnings()) {
			assertFalse(w.toString(), w.toString().matches(".*[A-Z]+[0-9]+_[A-Z_0-9]+.*"));
		}
		assertEquals(Boolean.FALSE, result.metadata().get("fields_update_on_open"));
	}

	/** REQ-1001: every WD option name and value accepted; anything else refused; the two values not implemented refused, naming REQ-1001. */
	@Test
	public void optionsByWdName() throws Exception {
		TextOptions defaults = new TextOptions();
		assertEquals(TextOptions.NAMES, new ArrayList<String>(defaults.toMap().keySet()));
		assertEquals(defaults.toMap(), TextOptions.of(defaults.toMap()).toMap());
		Map<String, Object> m = new HashMap<String, Object>();
		m.put("view", "current");
		m.put("include", "headers, footers comments");
		m.put("hidden", "true");
		m.put("markup_detail", "author_date");
		m.put("tables", "tsv");
		TextOptions o = TextOptions.of(m);
		assertEquals(TextView.ACCEPTED, o.getView());
		assertTrue(o.isComments() && o.isHidden());
		assertEquals(TextOptions.MarkupDetail.AUTHOR_DATE, o.getMarkupDetail());
		for (String[] bad : new String[][] { { "colour", "red" }, { "hidden", "maybe" }, { "fields", "recalculate" }, { "include", "sidebars" } }) {
			try {
				new TextOptions().set(bad[0], bad[1]);
				fail(bad[0]);
			} catch (IllegalArgumentException expected) {
				assertTrue(expected.getMessage().contains("REQ-1001"));
			}
		}
		// implemented in phase 2
		for (String[] now : new String[][] { { "fields", "update" }, { "embedded", "true" }, { "metadata", "true" }, { "alt_text", "true" },
				{ "include", "glossary" }, { "tables", "grid" }, { "page_markers", "true" }, { "layout", "indent" } }) {
			Texts.of(probe, new TextOptions().set(now[0], now[1]));
		}
		// not implemented by CR-046 (its section 2.6)
		for (String[] later : new String[][] { { "math", "unicodemath" }, { "symbols", "unicode" } }) {
			try {
				Texts.of(probe, new TextOptions().set(later[0], later[1]));
				fail(later[0]);
			} catch (UnsupportedOperationException expected) {
				assertTrue(expected.getMessage(), expected.getMessage().contains("REQ-1001") && expected.getMessage().contains(later[0] + ": " + later[1]));
			}
		}
	}
}
