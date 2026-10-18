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

import java.util.Collections;

import org.docx4j.openpackaging.packages.WordprocessingMLPackage;
import org.junit.Test;

/**
 * A table of contents under the {@code toc} option (WD REQ-520, CR-046 D12): the result of a TOC
 * field, left out by default in every serialisation, nothing of the field read, a paragraph with
 * nothing else in it no paragraph; kept under {@code toc: true}.
 */
public class TextsTocTest {

	private static String run(String t) {
		return "<w:r><w:t xml:space=\"preserve\">" + t + "</w:t></w:r>";
	}

	private static String field(String instr, String result) {
		return "<w:r><w:fldChar w:fldCharType=\"begin\"/></w:r>"
				+ "<w:r><w:instrText xml:space=\"preserve\"> " + instr + " </w:instrText></w:r>"
				+ "<w:r><w:fldChar w:fldCharType=\"separate\"/></w:r>" + result
				+ "<w:r><w:fldChar w:fldCharType=\"end\"/></w:r>";
	}

	/** A TOC entry as Word writes it: a hyperlink to the heading's bookmark, with a PAGEREF to it. */
	private static String entry(String bookmark, String title, String page) {
		return "<w:hyperlink w:anchor=\"" + bookmark + "\">" + run(title) + "<w:r><w:tab/></w:r>"
				+ field("PAGEREF " + bookmark + " \\h", run(page)) + "</w:hyperlink>";
	}

	/** Word's shape: a heading, the field begun in the first entry's paragraph, ended in a paragraph of its own. */
	private static final String TOC = "<w:sdt><w:sdtPr><w:docPartObj><w:docPartGallery w:val=\"Table of Contents\"/>"
			+ "<w:docPartUnique/></w:docPartObj></w:sdtPr><w:sdtContent>"
			+ "<w:p><w:pPr><w:pStyle w:val=\"TOCHeading\"/></w:pPr>" + run("Contents") + "</w:p>"
			+ "<w:p><w:pPr><w:pStyle w:val=\"TOC1\"/></w:pPr>"
			+ "<w:r><w:fldChar w:fldCharType=\"begin\"/></w:r><w:r><w:instrText xml:space=\"preserve\"> TOC \\o \"1-3\" \\h \\z \\u </w:instrText></w:r>"
			+ "<w:r><w:fldChar w:fldCharType=\"separate\"/></w:r>" + entry("_Toc1", "Introduction", "1") + "</w:p>"
			+ "<w:p><w:pPr><w:pStyle w:val=\"TOC1\"/></w:pPr>" + entry("_Toc2", "Method", "2") + "</w:p>"
			+ "<w:p><w:r><w:fldChar w:fldCharType=\"end\"/></w:r></w:p>"
			+ "</w:sdtContent></w:sdt>";

	private static final String BODY = "<w:p><w:pPr><w:pStyle w:val=\"Heading1\"/></w:pPr><w:bookmarkStart w:id=\"1\" w:name=\"_Toc1\"/>"
			+ run("Introduction") + "<w:bookmarkEnd w:id=\"1\"/></w:p>"
			+ "<w:p>" + run("Dated ") + field("DATE", run("today")) + "</w:p>";

	private static WordprocessingMLPackage pkg(String body) throws Exception {
		return MiniDocx.body(body).load();
	}

	private static String text(WordprocessingMLPackage pkg, TextOptions options) {
		return PlainText.of(Texts.of(pkg, options));
	}

	@Test
	public void leftOutByDefault() throws Exception {
		WordprocessingMLPackage pkg = pkg(TOC + BODY);
		assertFalse(new TextOptions().isToc());
		TextResult r = Texts.of(pkg, new TextOptions());
		// the heading stays (it isn't the field's); the entries and the end mark's paragraph are no lines
		assertEquals("Contents\nIntroduction\nDated today\n", PlainText.of(r));
		assertTrue("no field.no_result for the TOC", r.warnings().isEmpty());
		for (TextBlock b : r.blocks()) {
			for (TextField f : b.getFields()) assertFalse(f.getInstr(), f.getInstr().contains("TOC") || f.getInstr().contains("PAGEREF"));
		}
	}

	@Test
	public void keptUnderToc() throws Exception {
		WordprocessingMLPackage pkg = pkg(TOC + BODY);
		assertEquals("Contents\nIntroduction\t1\nMethod\t2\n\nIntroduction\nDated today\n",
				text(pkg, new TextOptions().toc(true)));
		assertEquals(text(pkg, new TextOptions().toc(true)), text(pkg, TextOptions.of(Collections.singletonMap("toc", "true"))));
		assertEquals(Boolean.FALSE, new TextOptions().toMap().get("toc"));
	}

	/** Under field_codes, nothing of the TOC field is read, its code included; other fields' codes are. */
	@Test
	public void fieldCodes() throws Exception {
		WordprocessingMLPackage pkg = pkg(TOC + BODY);
		String text = text(pkg, new TextOptions().fieldCodes(true));
		assertEquals("Contents\nIntroduction\nDated  DATE today\n", text);
		String kept = text(pkg, new TextOptions().fieldCodes(true).toc(true));
		assertTrue(kept, kept.contains(" TOC \\o \"1-3\" \\h \\z \\u Introduction\t PAGEREF _Toc1 \\h 1"));
	}

	/** A table of figures is a TOC field too; a simple field's is left out as a complex one's. */
	@Test
	public void tableOfFiguresAndSimpleField() throws Exception {
		WordprocessingMLPackage pkg = pkg("<w:p>" + run("Figures") + "</w:p>"
				+ "<w:p><w:fldSimple w:instr=\" TOC \\h \\z \\c &quot;Figure&quot; \">" + run("Figure 1: A") + "</w:fldSimple></w:p>"
				+ "<w:p>" + field("TOC \\c \"Table\"", run("Table 1: B")) + "</w:p>"
				+ "<w:p>" + run("After") + "</w:p>");
		assertEquals("Figures\nAfter\n", text(pkg, new TextOptions()));
		assertEquals("Figures\nFigure 1: A\nTable 1: B\nAfter\n", text(pkg, new TextOptions().toc(true)));
	}

	/** A table of contents whose field was unlinked is the document's text, whatever its styles. */
	@Test
	public void unlinkedTocIsText() throws Exception {
		WordprocessingMLPackage pkg = pkg("<w:p><w:pPr><w:pStyle w:val=\"TOC1\"/></w:pPr>" + run("Introduction\t1") + "</w:p>");
		assertEquals("Introduction\t1\n", text(pkg, new TextOptions()));
	}

	/** In every view: text before the field in its first paragraph stays, and so does a revision's mark outside it. */
	@Test
	public void textBesideTheFieldAndTheViews() throws Exception {
		WordprocessingMLPackage pkg = pkg("<w:p>" + run("See: ") + field("TOC \\o", run("Entry\t3")) + run(" (end)") + "</w:p>"
				+ "<w:p>" + field("TOC \\o", "<w:ins w:id=\"1\" w:author=\"A\"><w:r><w:t>Added entry</w:t></w:r></w:ins>") + "</w:p>"
				+ "<w:p>" + run("Body") + "</w:p>");
		for (TextView view : TextView.values()) {
			assertEquals(view.toString(), "See:  (end)\nBody\n", text(pkg, TextOptions.of(view)));
		}
		// under field_codes too, and the text either side of the field is one run, as it is without codes
		for (TextOptions options : new TextOptions[] { new TextOptions(), new TextOptions().fieldCodes(true) }) {
			TextBlock first = Texts.of(pkg, options).blocks().get(0);
			assertEquals(options.toString(), 1, first.getRuns().size());
			assertEquals("See:  (end)", first.getRuns().get(0).getText());
		}
	}
}
