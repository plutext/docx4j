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
package org.docx4j.model.revisions;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.util.Arrays;
import java.util.List;

import org.docx4j.XmlUtils;
import org.docx4j.openpackaging.packages.WordprocessingMLPackage;
import org.docx4j.wml.Comments;
import org.docx4j.wml.P;
import org.docx4j.wml.R;
import org.docx4j.wml.RPr;
import org.docx4j.wml.RunDel;
import org.docx4j.wml.RunIns;
import org.docx4j.wml.SdtElement;
import org.junit.Test;

/**
 * CR-068 phase 2: the opt-ins of {@link ManualRedlineOptions} on the probe's two bases
 * ({@code revisions/cr068}, {@link ManualRedlineProbeBuilder}) and on small cases: style strike,
 * underline and colour as insertions, the comments (D14), the separating space between a deletion
 * and its insertion, the range, the parts, the audit property.
 */
public class ManualRedlineOptInsTest {

	static final String W = ManualRedlineTest.W;

	static ManualRedlineOptions all() {
		return ManualRedlineProbeBuilder.phase2();
	}

	static WordprocessingMLPackage second() throws Exception {
		WordprocessingMLPackage pkg = TrackedChangesWordTest.load(ManualRedlineTest.DIR + "manual-redline-2.docx");
		pkg.setDateTimeZone(ManualRedlineTest.SYDNEY);
		return pkg;
	}

	static List<String> lines(String text) {
		return Arrays.asList(text.split("\n"));
	}

	static void contains(List<String> lines, String... expected) {
		for (String e : expected) assertTrue(e + " in\n" + lines, lines.contains(e));
	}

	// ---------------------------------------------------------------- the probe

	@Test
	public void theFirstBase() throws Exception {
		WordprocessingMLPackage pkg = ManualRedlineTest.base();
		ManualRedlineReport r = ManualRedline.toTrackedChanges(pkg, all());
		assertEquals(26, r.getDeletions());
		assertEquals(15, r.getInsertions());
		assertEquals(28, r.getRuns());
		assertEquals("{in_tracked_change=1, field=2}", r.getLeftByReason().toString());
		List<String> accepted = lines(ManualRedlineTest.accepted(pkg));
		List<String> rejected = lines(ManualRedlineTest.rejected(pkg));
		contains(accepted, "M01 Rate: 1.335 per cent.", "M05 then plain words.", "M12 The party shall promptly notify the other.",
				"M13 Fee: EUR 450 a month.", "M17 Party: Acme Holdings Pty Ltd.", "[1] M10 footnote: the rate is 1.335.");
		contains(rejected, "M01 Rate: 1.316 per cent.", "M02 cell:\t1.316", "Widget\t$40.00", "M05 style-struck words then plain words.",
				"M12 The party shall notify the other.", "M13 Fee: EUR 500 a month.", "M17 Party: Acme Pty Ltd.",
				"[1] M10 footnote: the rate is 1.316.");
		for (String line : accepted) assertFalse("two spaces: " + line, line.contains("  "));
		for (String line : rejected) if (!line.startsWith("M07")) assertFalse("two spaces: " + line, line.contains("  "));
	}

	@Test
	public void theSecondBase() throws Exception {
		WordprocessingMLPackage pkg = second();
		ManualRedlineReport r = ManualRedline.toTrackedChanges(pkg, all());
		assertEquals(1, r.getStrikeAndUnderline());
		assertTrue(r.getLeft().isEmpty());
		contains(lines(ManualRedlineTest.accepted(pkg)), "N01 The party shall promptly notify the other.", "N02 Fee: EUR 450 a month.",
				"N03 Term: two years.", "N04 Note: urgent item.", "N05 Colour: blue words stay.", "N06 Underline: no underline stays.",
				"N07 Style: then plain.", "N08 Remove the last word", "N09 Replace the old.");
		contains(lines(ManualRedlineTest.rejected(pkg)), "N01 The party shall notify the other.", "N02 Fee: EUR 500 a month.",
				"N03 Term: one year.", "N07 Style: double struck by its style then plain.", "N08 Remove the last word here",
				"N09 Replace the old term.");
		// N07: the style's double strike turned off on the run itself (D6), so that Reject All shows it plain
		R n07 = firstRunWith(pkg, "double struck by its style");
		assertNotNull(n07.getRPr().getDstrike());
		assertFalse(n07.getRPr().getDstrike().isVal());
		assertEquals("DoubleStruck", n07.getRPr().getRStyle().getVal());
		// N02: the run both struck and underlined is a deletion, its underline kept with it
		R n02 = firstRunWith(pkg, "EUR 500");
		assertTrue(n02.getParent() instanceof RunDel);
		assertNotNull(n02.getRPr().getU());
		assertEquals(null, n02.getRPr().getStrike());
	}

	/** The space between a deletion and the insertion replacing it is inserted and deleted: in neither outcome. */
	@Test
	public void theSpaceBetweenADeletionAndItsInsertion() throws Exception {
		WordprocessingMLPackage pkg = ManualRedlineTest.base();
		ManualRedline.toTrackedChanges(pkg, all());
		P m01 = (P) pkg.getMainDocumentPart().getContent().get(1);
		String xml = XmlUtils.marshaltoString(m01, true, false);
		assertTrue(xml, xml.matches("(?s).*</w:del><w:ins [^>]*><w:del [^>]*><w:r><w:delText xml:space=\"preserve\"> </w:delText></w:r></w:del></w:ins><w:ins .*"));
	}

	/** D14: in a comment the converted runs keep their strike and underline, which is all Word's comment card shows. */
	@Test
	public void commentsKeepTheirMarks() throws Exception {
		WordprocessingMLPackage pkg = second();
		ManualRedline.toTrackedChanges(pkg, all());
		Comments.Comment comment = pkg.getMainDocumentPart().getCommentsPart().getJaxbElement().getComment().get(0);
		R withdrawn = firstRunWith(comment, "withdrawn");
		assertTrue(withdrawn.getParent() instanceof RunDel);
		assertTrue(withdrawn.getRPr().getStrike().isVal());
		R added = firstRunWith(comment, "added");
		assertTrue(added.getParent() instanceof RunIns);
		assertNotNull(added.getRPr().getU());
		// elsewhere the marks go (D6, D7)
		R n01 = firstRunWith(pkg, "promptly");
		assertTrue(n01.getParent() instanceof RunIns);
		assertEquals(null, n01.getRPr());
	}

	@Test
	public void idempotentAndPreviewed() throws Exception {
		WordprocessingMLPackage pkg = second();
		ManualRedlineReport p = ManualRedline.preview(pkg, all());
		ManualRedlineReport r = ManualRedline.toTrackedChanges(pkg, all());
		assertEquals(r.toJson(), p.toJson().replace("\"preview\":true", "\"preview\":false"));
		ManualRedlineReport again = ManualRedline.toTrackedChanges(pkg, all());
		assertEquals(0, again.getDeletions());
		assertEquals(0, again.getInsertions());
	}

	// ---------------------------------------------------------------- small cases

	private static WordprocessingMLPackage doc(String... paragraphs) throws Exception {
		WordprocessingMLPackage pkg = WordprocessingMLPackage.createPackage();
		for (String p : paragraphs) pkg.getMainDocumentPart().getContent().add(XmlUtils.unmarshalString("<w:p xmlns:w='" + W + "'>" + p + "</w:p>"));
		return pkg;
	}

	private static String run(String rPr, String text) {
		return "<w:r>" + (rPr.isEmpty() ? "" : "<w:rPr>" + rPr + "</w:rPr>") + "<w:t xml:space='preserve'>" + text + "</w:t></w:r>";
	}

	@Test
	public void colours() throws Exception {
		WordprocessingMLPackage pkg = doc(run("", "a ") + run("<w:color w:val='ff0000'/>", "red") + run("", " b ")
				+ run("<w:color w:val='00B050'/>", "green") + run("", " c ") + run("<w:color w:val='auto'/>", "auto") + run("", " end"));
		ManualRedlineReport r = ManualRedline.toTrackedChanges(pkg, ManualRedlineTest.options().colourAsInsertion("FF0000", "00b050"));
		assertEquals("{colour=2, separator_space=2}", r.getByKind().toString());
		assertEquals("a b c auto end\n", ManualRedlineTest.rejected(pkg));
		try {
			new ManualRedlineOptions().colourAsInsertion("auto");
			assertTrue("auto refused", false);
		} catch (IllegalArgumentException expected) {
			// the caller names a colour; auto is none
		}
	}

	@Test
	public void withinABookmark() throws Exception {
		WordprocessingMLPackage pkg = doc(run("<w:strike/>", "before"),
				"<w:bookmarkStart w:id='7' w:name='Clause2'/>" + run("<w:strike/>", "inside") + "<w:bookmarkEnd w:id='7'/>" + run("<w:strike/>", "after"));
		ManualRedlineReport r = ManualRedline.toTrackedChanges(pkg, ManualRedlineTest.options().withinBookmark("Clause2"));
		assertEquals(1, r.getDeletions());
		assertEquals("inside", r.getChanges().get(0).getText());
		assertEquals("before\nafter\n", ManualRedlineTest.accepted(pkg));
	}

	@Test
	public void withinAControl() throws Exception {
		WordprocessingMLPackage pkg = ManualRedlineTest.base();
		SdtElement m17 = (SdtElement) XmlUtils.unwrap(((P) pkg.getMainDocumentPart().getContent().get(19)).getContent().get(1));
		ManualRedlineReport r = ManualRedline.toTrackedChanges(pkg, ManualRedlineTest.options().within(m17).underlineAsInsertion(true));
		assertEquals("{strike=1, underline=1, separator_space=1}", r.getByKind().toString());
		assertEquals("body/19", r.getChanges().get(0).getAddress());
	}

	@Test
	public void parts() throws Exception {
		WordprocessingMLPackage pkg = ManualRedlineTest.base();
		ManualRedlineReport r = ManualRedline.toTrackedChanges(pkg,
				ManualRedlineTest.options().parts(pkg.getMainDocumentPart().getFootnotesPart()));
		assertEquals("{/word/footnotes.xml=2}", r.getByPart().toString());
	}

	@Test
	public void auditProperty() throws Exception {
		WordprocessingMLPackage pkg = doc(run("<w:strike/>", "gone"));
		ManualRedline.toTrackedChanges(pkg, ManualRedlineTest.options().auditProperty(true));
		String value = pkg.getDocPropsCustomPart().getProperty("docx4j:manualRedline").getLpwstr();
		assertEquals("2026-10-31T00:00:00Z; author=manual redline, date=2026-10-31T00:00:00Z, separatorSpace=true, auditProperty=true", value);
		// a preview records nothing
		WordprocessingMLPackage other = doc(run("<w:strike/>", "gone"));
		ManualRedline.preview(other, ManualRedlineTest.options().auditProperty(true));
		assertTrue(other.getDocPropsCustomPart() == null || other.getDocPropsCustomPart().getProperty("docx4j:manualRedline") == null);
	}

	// ---------------------------------------------------------------- helpers

	private static R firstRunWith(WordprocessingMLPackage pkg, String text) {
		return firstRunWith(pkg.getMainDocumentPart().getJaxbElement(), text);
	}

	private static R firstRunWith(Object root, String text) {
		R[] found = { null };
		Trees.walk(root, new Trees.Visitor() {
			@Override boolean enter(Object value, Object item) {
				if (found[0] == null && value instanceof R && Trees.text(value, Trees.View.ORIGINAL).contains(text)) found[0] = (R) value;
				return found[0] == null;
			}
		});
		return found[0];
	}

	static RPr rPr(R r) {
		return r.getRPr();
	}
}
