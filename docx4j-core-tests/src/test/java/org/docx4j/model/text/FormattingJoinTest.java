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

import static org.docx4j.model.text.InspectTest.of;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import java.util.ArrayList;
import java.util.List;

import org.docx4j.model.text.InspectTest.Doc;
import org.docx4j.openpackaging.packages.WordprocessingMLPackage;
import org.junit.Test;

/**
 * {@code text.formatting_join} (WD 9.2; CR-069 phase 4): a struck or raised or lowered run meeting its neighbour at a
 * digit|digit boundary with no separator, the two differing in that formatting, so that the plain text forms a number the
 * page does not show. Unseen in plain text, info in Markdown and the block model, which show the parts apart. Word's
 * probe (dpp C03, C04, C06, C07), then the boundaries one at a time.
 */
public class FormattingJoinTest {

	private static final String K = InspectFinding.TEXT_FORMATTING_JOIN;
	private static final String STRIKE = "<w:strike/>";
	private static final String SUP = "<w:vertAlign w:val='superscript'/>";
	private static final String SUB = "<w:vertAlign w:val='subscript'/>";

	private static String r(String rPr, String text) {
		return "<w:r>" + (rPr.isEmpty() ? "" : "<w:rPr>" + rPr + "</w:rPr>") + "<w:t xml:space='preserve'>" + text + "</w:t></w:r>";
	}

	private static WordprocessingMLPackage doc(String... paragraphs) throws Exception {
		Doc d = new Doc();
		for (String p : paragraphs) d.p(p);
		return d.done();
	}

	/** The excerpts of a paragraph's findings. */
	private static List<String> joins(String... paragraphs) throws Exception {
		List<String> out = new ArrayList<String>();
		for (InspectFinding f : of(Inspect.of(doc(paragraphs)), K)) out.add(f.getExcerpt());
		return out;
	}

	private static List<String> list(String... s) {
		return java.util.Arrays.asList(s);
	}

	/** Word's probe: C04 (struck then underlined in a cell) and C07 (a superscript 3) fire; C03 (a space) and C06 (letters) do not. */
	@Test
	public void theProbesCases() throws Exception {
		WordprocessingMLPackage pkg = WordprocessingMLPackage.load(
				ViewAgreementTest.resources().resolve("text-extraction/dpp/dpp-forks-word365.docx").toFile());
		List<InspectFinding> found = of(Inspect.of(pkg), K);
		assertEquals(found.toString(), 2, found.size());
		InspectFinding c04 = found.get(0), c07 = found.get(1);
		// the joined token: from the struck run's start to the underlined one's end, in the cell
		assertEquals("1.3161.335", c04.getExcerpt());
		assertEquals("/w:document[1]/w:body[1]/w:tbl[1]/w:tr[1]/w:tc[2]/w:p[1]/w:r[1]", c04.getLocation().getPath());
		assertEquals(Integer.valueOf(0), c04.getLocation().getStart());
		assertEquals("/w:document[1]/w:body[1]/w:tbl[1]/w:tr[1]/w:tc[2]/w:p[1]/w:r[2]", c04.getLocation().getEndPath());
		assertEquals(Integer.valueOf(5), c04.getLocation().getEnd());
		assertEquals("body/4/0/1/0", c04.getAddress());
		// from 48,210 in "C07_ Revenue 48,210" to the superscript's end
		assertEquals("48,2103", c07.getExcerpt());
		assertEquals(Integer.valueOf(13), c07.getLocation().getStart());
		assertEquals(Integer.valueOf(1), c07.getLocation().getEnd());
		assertEquals("body/7", c07.getAddress());
		for (InspectFinding f : found) {
			assertEquals(Visible.DIFFERENTLY, f.getVisible());
			assertNull(f.getSubject());
			assertEquals(Reveal.Form.INCLUDED, f.getReveal().getForm());
			assertEquals(Group.UNSEEN, f.group(new TextOptions(), Inspection.Format.PLAIN, null));
			assertEquals(Group.INFO, f.group(new TextOptions(), Inspection.Format.MARKDOWN, null));
			assertEquals(Group.INFO, f.group(new TextOptions(), Inspection.Format.BLOCKS, null));
			assertTrue(InspectionReport.line(f), InspectionReport.line(f).contains("(a number the plain text fuses"));
		}
	}

	/** Struck once or twice, raised, lowered, raised beside lowered: each a join; the same formatting either side, none. */
	@Test
	public void theFormattingThatSetsARunApart() throws Exception {
		assertEquals(list("1.3161.335"), joins(r(STRIKE, "1.316") + r("", "1.335")));
		assertEquals(list("1.3161.335"), joins(r("<w:dstrike/>", "1.316") + r("", "1.335")));
		assertEquals(list("1.3161.335"), joins(r("", "1.316") + r(STRIKE, "1.335")));
		assertEquals(list("48,2103"), joins(r("", "Revenue 48,210") + r(SUP, "3") + r("", " in the period.")));
		assertEquals(list("C102"), joins(r("", "C10") + r(SUB, "2")));
		assertEquals(list("12"), joins(r(SUP, "1") + r(SUB, "2")));
		// both struck, both raised, a struck run turned off by its own w:val
		assertEquals(list(), joins(r(STRIKE, "1.316") + r(STRIKE, "1.335")));
		assertEquals(list(), joins(r(SUP, "1") + r(SUP, "2")));
		assertEquals(list(), joins(r("<w:strike w:val='0'/>", "1.316") + r("", "1.335")));
		// the formatting from a character style
		Doc d = new Doc();
		d.p(r("<w:rStyle w:val='Struck'/>", "1.316") + r("", "1.335"));
		WordprocessingMLPackage pkg = d.done();
		pkg.getMainDocumentPart().getStyleDefinitionsPart().getJaxbElement().getStyle().add((org.docx4j.wml.Style) org.docx4j.XmlUtils.unmarshalString(
				"<w:style" + InspectTest.NS + " w:type='character' w:styleId='Struck'><w:name w:val='Struck'/><w:rPr><w:strike/></w:rPr></w:style>"));
		assertEquals(1, of(Inspect.of(pkg), K).size());
	}

	/** Only a digit either side (D6): a letter's boundary is the same in every reading, so no join. */
	@Test
	public void aLetterIsNoJoin() throws Exception {
		assertEquals(list(), joins(r("", "H") + r(SUB, "2") + r("", "O and x") + r(SUP, "2") + r("", " on the 1") + r(SUP, "st") + r("", " of May")));
		assertEquals(list(), joins(r("", "page 1") + r(STRIKE, "a")));
		// a digit in another script is a digit
		assertEquals(list("\u0661\u0662"), joins(r("", "\u0661") + r(SUP, "\u0662")));
	}

	/** A space or a separator between the two, written in the plain text, is no join; a field's characters are not one. */
	@Test
	public void whatSeparatesTheRuns() throws Exception {
		assertEquals(list(), joins(r(STRIKE, "1.316") + r("", " 1.335")));
		assertEquals(list(), joins(r(STRIKE, "1.316 ") + r("", "1.335")));
		assertEquals(list(), joins(r(STRIKE, "1.316") + "<w:r><w:tab/></w:r>" + r("", "1.335")));
		assertEquals(list(), joins("<w:r><w:rPr><w:strike/></w:rPr><w:t>1.316</w:t><w:tab/></w:r>" + r("", "1.335")));
		assertEquals(list(), joins(r(STRIKE, "1.316") + "<w:r><w:br/></w:r>" + r("", "1.335")));
		assertEquals(list(), joins(r("", "48,210") + "<w:r><w:rPr>" + SUP + "</w:rPr><w:footnoteReference w:id='1'/></w:r>" + r(SUP, "3")));
		assertEquals(list(), joins(r("", "48,210") + "<m:oMath xmlns:m='http://schemas.openxmlformats.org/officeDocument/2006/math'><m:r><m:t>x</m:t></m:r></m:oMath>" + r(SUP, "3")));
		// a field's characters and instruction write nothing: its result joins
		assertEquals(list("48,2103"), joins(r("", "48,210") + "<w:r><w:fldChar w:fldCharType='begin'/></w:r><w:r><w:instrText> QUOTE 3 </w:instrText></w:r>"
				+ "<w:r><w:fldChar w:fldCharType='separate'/></w:r>" + r(SUP, "3") + "<w:r><w:fldChar w:fldCharType='end'/></w:r>"));
		// a bookmark between is nothing
		assertEquals(list("48,2103"), joins(r("", "48,210") + "<w:bookmarkStart w:id='0' w:name='b'/>" + r(SUP, "3") + "<w:bookmarkEnd w:id='0'/>"));
	}

	/** A hidden run and a deleted one leave no text, so their neighbours meet; neither is itself a side of a join. */
	@Test
	public void hiddenAndDeletedRunsLeaveNoText() throws Exception {
		assertEquals(list("1.3161.335"), joins(r(STRIKE, "1.316") + r("<w:vanish/>", " note ") + r("", "1.335")));
		assertEquals(list(), joins(r("", "1.316") + r("<w:vanish/>" + STRIKE, "9")));
		assertEquals(list("1.3161.335"), joins(r(STRIKE, "1.316")
				+ "<w:del w:id='1' w:author='A'><w:r><w:delText xml:space='preserve'> was </w:delText></w:r></w:del>" + r("", "1.335")));
		assertEquals(list(), joins("<w:del w:id='1' w:author='A'><w:r><w:rPr><w:strike/></w:rPr><w:delText>1.316</w:delText></w:r></w:del>" + r("", "1.335")));
		// an inserted run is a side, out of the original view's text
		List<InspectFinding> inserted = of(Inspect.of(doc(r("", "48,210") + "<w:ins w:id='1' w:author='A'>" + r(SUP, "3") + "</w:ins>")), K);
		assertEquals(1, inserted.size());
		assertEquals(Group.UNSEEN, inserted.get(0).group(new TextOptions(), Inspection.Format.PLAIN, null));
		assertEquals(Group.OMITTED, inserted.get(0).group(TextOptions.of(TextView.ORIGINAL), Inspection.Format.PLAIN, null));
	}

	/** One finding a token, its range the whole token, across every run it spans. */
	@Test
	public void oneFindingAToken() throws Exception {
		List<InspectFinding> found = of(Inspect.of(doc(r("", "sum 48,21") + r("", "0") + r(SUP, "3") + r("", "5 in all"))), K);
		assertEquals(1, found.size());
		InspectFinding f = found.get(0);
		assertEquals("48,21035", f.getExcerpt());
		assertEquals("/w:document[1]/w:body[1]/w:p[1]/w:r[1]", f.getLocation().getPath());
		assertEquals(Integer.valueOf(4), f.getLocation().getStart());
		assertEquals("/w:document[1]/w:body[1]/w:p[1]/w:r[4]", f.getLocation().getEndPath());
		assertEquals(Integer.valueOf(1), f.getLocation().getEnd());
		// two tokens in a paragraph, two findings
		assertEquals(list("12", "34"), joins(r("", "1") + r(SUP, "2") + r("", " and 3") + r(SUB, "4")));
	}

	/** The counts by format (REQ-903): unseen in plain text, info in Markdown and the block model. */
	@Test
	public void theCountsByFormat() throws Exception {
		WordprocessingMLPackage pkg = doc(r("", "Revenue 48,210") + r(SUP, "3"));
		TextResult result = Texts.of(pkg, new TextOptions().inspect(true));
		Inspection none = Inspect.of(doc(r("", "Revenue 48,2103")));
		Inspection.Counts base = none.counts(new TextOptions(), Inspection.Format.PLAIN);
		assertEquals(base.getUnseen() + 1, result.counts(Inspection.Format.PLAIN).getUnseen());
		assertEquals(base.getInfo(), result.counts(Inspection.Format.PLAIN).getInfo());
		Inspection.Counts md = none.counts(new TextOptions(), Inspection.Format.MARKDOWN);
		assertEquals(md.getUnseen(), result.counts(Inspection.Format.MARKDOWN).getUnseen());
		assertEquals(md.getInfo() + 1, result.counts(Inspection.Format.MARKDOWN).getInfo());
		assertEquals(none.counts(new TextOptions(), Inspection.Format.BLOCKS).getInfo() + 1, result.counts(Inspection.Format.BLOCKS).getInfo());
	}

	/** w:dstrike is struck in the text model since 17.3.5, so Markdown's ~~ shows a double-struck part apart. */
	@Test
	public void aDoubleStruckRunIsStruck() throws Exception {
		TextResult result = Texts.of(doc(r("<w:dstrike/>", "1.316") + r("", "1.335")), new TextOptions());
		List<TextRun> runs = result.blocks().get(0).getRuns();
		assertEquals(2, runs.size());
		assertTrue(runs.get(0).isStrike());
		assertEquals(false, runs.get(1).isStrike());
	}
}
