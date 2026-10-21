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
import static org.junit.Assert.assertTrue;

import java.util.List;

import org.docx4j.XmlUtils;
import org.docx4j.openpackaging.packages.WordprocessingMLPackage;
import org.docx4j.wml.Document;
import org.junit.Test;

/**
 * CR-051 section 10.3 item 3 and section 10.5 item 3, and phase 2: tracked changes inside a
 * block-level, a paragraph-level, a row-level and a cell-level {@code mc:AlternateContent}, listed
 * once from the branch Word reads, with the other branches' changes as twins that resolve with them.  The trees are unmarshalled outside a
 * part, so the elements stay whole in either {@code McBlockLevel} mode (CR-051 section 3.4).
 */
public class TrackedChangesAlternateContentTest {

	private static final String NS = TrackedChangesTest.NS + " xmlns:mc=\"http://schemas.openxmlformats.org/markup-compatibility/2006\"";

	/** As {@link TrackedChangesTest#withBody}, with the mc namespace declared. */
	private static WordprocessingMLPackage withBody(String bodyXml) throws Exception {
		WordprocessingMLPackage pkg = WordprocessingMLPackage.createPackage();
		Document doc = (Document) XmlUtils.unmarshalString("<w:document " + NS + "><w:body>" + bodyXml + "</w:body></w:document>");
		pkg.getMainDocumentPart().setJaxbElement(doc);
		return pkg;
	}

	private static String ins(int id, String text) {
		return "<w:ins w:id=\"" + id + "\" w:author=\"A\"><w:r><w:t xml:space=\"preserve\">" + text + "</w:t></w:r></w:ins>";
	}

	private static String del(int id, String text) {
		return "<w:del w:id=\"" + id + "\" w:author=\"A\"><w:r><w:delText xml:space=\"preserve\">" + text + "</w:delText></w:r></w:del>";
	}

	/** A block-level element, an insertion and a deletion in each branch's paragraph. */
	private static final String BLOCK = "<w:p><w:r><w:t>before</w:t></w:r></w:p>"
			+ "<mc:AlternateContent><mc:Choice Requires=\"w14\"><w:p><w:r><w:t>C </w:t></w:r>" + ins(1, "INSERTED") + del(2, "DELETED") + "</w:p></mc:Choice>"
			+ "<mc:Fallback><w:p><w:r><w:t>F </w:t></w:r>" + ins(3, "INSERTED") + del(4, "DELETED") + "</w:p></mc:Fallback></mc:AlternateContent>"
			+ "<w:p><w:r><w:t>after</w:t></w:r></w:p>";

	/** A paragraph-level element, the same changes in each branch. */
	private static final String PARA = "<w:p><w:r><w:t>x </w:t></w:r>"
			+ "<mc:AlternateContent><mc:Choice Requires=\"w14\"><w:r><w:t>C </w:t></w:r>" + ins(1, "INSERTED") + del(2, "DELETED") + "</mc:Choice>"
			+ "<mc:Fallback><w:r><w:t>F </w:t></w:r>" + ins(3, "INSERTED") + del(4, "DELETED") + "</mc:Fallback></mc:AlternateContent></w:p>";

	private static final String TBL_PROPS = "<w:tblPr/><w:tblGrid><w:gridCol w:w=\"1000\"/></w:tblGrid>";

	/** A row-level element among a table's rows (CR-051 phase 2), the same changes in each branch's row. */
	private static final String ROW = "<w:tbl>" + TBL_PROPS
			+ "<mc:AlternateContent><mc:Choice Requires=\"w14\"><w:tr><w:tc><w:p><w:r><w:t>C </w:t></w:r>" + ins(1, "INSERTED") + del(2, "DELETED") + "</w:p></w:tc></w:tr></mc:Choice>"
			+ "<mc:Fallback><w:tr><w:tc><w:p><w:r><w:t>F </w:t></w:r>" + ins(3, "INSERTED") + del(4, "DELETED") + "</w:p></w:tc></w:tr></mc:Fallback></mc:AlternateContent>"
			+ "</w:tbl>";

	/** A cell-level element among a row's cells, the same changes in each branch's cell. */
	private static final String CELL = "<w:tbl>" + TBL_PROPS + "<w:tr>"
			+ "<mc:AlternateContent><mc:Choice Requires=\"w14\"><w:tc><w:p><w:r><w:t>C </w:t></w:r>" + ins(1, "INSERTED") + del(2, "DELETED") + "</w:p></w:tc></mc:Choice>"
			+ "<mc:Fallback><w:tc><w:p><w:r><w:t>F </w:t></w:r>" + ins(3, "INSERTED") + del(4, "DELETED") + "</w:p></w:tc></mc:Fallback></mc:AlternateContent>"
			+ "</w:tr></w:tbl>";

	private static String deletedRow(int id, String text) {
		return "<w:tr><w:trPr><w:del w:id=\"" + id + "\" w:author=\"A\"/></w:trPr><w:tc><w:p><w:r><w:delText>" + text + "</w:delText></w:r></w:p></w:tc></w:tr>";
	}

	private static String plainRow(String text) {
		return "<w:tr><w:tc><w:p><w:r><w:t>" + text + "</w:t></w:r></w:p></w:tc></w:tr>";
	}

	private static List<TrackedChange> listed(WordprocessingMLPackage pkg) {
		return TrackedChanges.in(pkg);
	}

	private static void listedOnceWithTwins(String body) throws Exception {
		WordprocessingMLPackage pkg = withBody(body);
		List<TrackedChange> changes = listed(pkg);
		assertEquals(changes.toString(), 2, changes.size());
		for (TrackedChange change : changes) {
			assertEquals(change.toString(), 1, change.twins.size());
			assertEquals(change.getKind(), change.twins.get(0).getKind());
		}
		assertTrue(changes.get(0).getText().contains("INSERTED"));
		assertTrue(changes.get(1).getText().contains("DELETED"));
		// every branch's changes are listed for acceptAll
		assertEquals(4, new Lister(pkg, false).list().size());
	}

	@Test
	public void blockLevelChangesAreListedOnceWithTwins() throws Exception {
		listedOnceWithTwins(BLOCK);
	}

	@Test
	public void paragraphLevelChangesAreListedOnceWithTwins() throws Exception {
		listedOnceWithTwins(PARA);
	}

	private static void acceptingResolvesTheTwin(String body) throws Exception {
		WordprocessingMLPackage pkg = withBody(body);
		listed(pkg).get(0).accept();
		String xml = TrackedChangesTest.bodyXml(pkg);
		assertFalse(xml, xml.contains("w:ins "));
		assertEquals("the insertion stays in both branches", 2, count(xml, "INSERTED"));
		assertEquals("the deletions are still listed, once", 1, listed(pkg).size());
		listed(pkg).get(0).reject();
		xml = TrackedChangesTest.bodyXml(pkg);
		assertFalse(xml, xml.contains("w:del "));
		assertEquals("the deleted text is back in both branches", 2, count(xml, "DELETED"));
		assertEquals(0, listed(pkg).size());
		assertEquals(0, new Lister(pkg, false).list().size());
	}

	@Test
	public void acceptingABlockLevelChangeResolvesItsTwin() throws Exception {
		acceptingResolvesTheTwin(BLOCK);
	}

	@Test
	public void acceptingAParagraphLevelChangeResolvesItsTwin() throws Exception {
		acceptingResolvesTheTwin(PARA);
	}

	private static void acceptAllResolvesEveryBranch(String body) throws Exception {
		WordprocessingMLPackage pkg = withBody(body);
		assertEquals(2, TrackedChanges.acceptAll(pkg));
		String xml = TrackedChangesTest.bodyXml(pkg);
		assertFalse(xml, xml.contains("w:ins ") || xml.contains("w:del "));
		assertEquals(2, count(xml, "INSERTED"));
		assertEquals(0, count(xml, "DELETED"));
		assertTrue("the element is still whole", xml.contains("<mc:Choice") && xml.contains("<mc:Fallback"));
	}

	@Test
	public void acceptAllResolvesEveryBranchAtBlockLevel() throws Exception {
		acceptAllResolvesEveryBranch(BLOCK);
	}

	@Test
	public void acceptAllResolvesEveryBranchAtParagraphLevel() throws Exception {
		acceptAllResolvesEveryBranch(PARA);
	}

	@Test
	public void rowLevelChangesAreListedOnceWithTwins() throws Exception {
		listedOnceWithTwins(ROW);
	}

	@Test
	public void cellLevelChangesAreListedOnceWithTwins() throws Exception {
		listedOnceWithTwins(CELL);
	}

	@Test
	public void acceptingARowLevelChangeResolvesItsTwin() throws Exception {
		acceptingResolvesTheTwin(ROW);
	}

	@Test
	public void acceptingACellLevelChangeResolvesItsTwin() throws Exception {
		acceptingResolvesTheTwin(CELL);
	}

	@Test
	public void acceptAllResolvesEveryBranchAtRowLevel() throws Exception {
		acceptAllResolvesEveryBranch(ROW);
	}

	@Test
	public void acceptAllResolvesEveryBranchAtCellLevel() throws Exception {
		acceptAllResolvesEveryBranch(CELL);
	}

	/**
	 * A deleted row in each branch of a row-level element: listed once, with its twin; accepted, both
	 * rows go, and the table with them, no branch having a row left (core-ts CR-013 section 7.3's shape).
	 */
	@Test
	public void acceptingADeletedRowInEveryBranchRemovesTheTable() throws Exception {
		WordprocessingMLPackage pkg = withBody("<w:p><w:r><w:t>before</w:t></w:r></w:p><w:tbl>" + TBL_PROPS
				+ "<mc:AlternateContent><mc:Choice Requires=\"w14\">" + deletedRow(1, "C") + "</mc:Choice><mc:Fallback>" + deletedRow(2, "F") + "</mc:Fallback></mc:AlternateContent></w:tbl>");
		List<TrackedChange> changes = listed(pkg);
		assertEquals(changes.toString(), 1, changes.size());
		assertEquals(1, changes.get(0).twins.size());
		changes.get(0).accept();
		String xml = TrackedChangesTest.bodyXml(pkg);
		assertFalse(xml, xml.contains("<w:tbl") || xml.contains("<w:tr"));
		assertTrue(xml, xml.contains("before"));
	}

	/** A deleted row in the branch Word reads, a plain row in the Fallback: accepted, the table stays, since a branch still holds a row. */
	@Test
	public void acceptingADeletedRowKeepsATableWhoseOtherBranchHasARow() throws Exception {
		WordprocessingMLPackage pkg = withBody("<w:tbl>" + TBL_PROPS
				+ "<mc:AlternateContent><mc:Choice Requires=\"w14\">" + deletedRow(1, "C") + "</mc:Choice><mc:Fallback>" + plainRow("F") + "</mc:Fallback></mc:AlternateContent></w:tbl>");
		List<TrackedChange> changes = listed(pkg);
		assertEquals(changes.toString(), 1, changes.size());
		assertEquals(0, changes.get(0).twins.size());
		changes.get(0).accept();
		String xml = TrackedChangesTest.bodyXml(pkg);
		assertTrue(xml, xml.contains("<w:tbl") && xml.contains("<mc:Fallback>") && xml.contains("<w:tr"));
		assertFalse(xml, xml.contains("<mc:Choice Requires=\"w14\"><w:tr"));
		assertTrue(xml, xml.contains(">F<"));
	}

	private static String cell(String text, String tcPr) {
		return "<w:tc><w:tcPr>" + tcPr + "</w:tcPr><w:p><w:r><w:t>" + text + "</w:t></w:r></w:p></w:tc>";
	}

	private static String deletedCell(int id, String text) {
		return cell(text, "<w:tcW w:w=\"1000\" w:type=\"dxa\"/><w:cellDel w:id=\"" + id + "\" w:author=\"A\"/>");
	}

	private static String plainCell(String text) {
		return cell(text, "<w:tcW w:w=\"1000\" w:type=\"dxa\"/>");
	}

	/**
	 * A deleted cell in each branch of a cell-level element, each branch with a plain cell beside it
	 * (docx4j-python's finding, its CR-012 section 10): accepted, both branches lose their cell and each
	 * branch's own neighbour is widened, once.
	 */
	@Test
	public void acceptingADeletedCellResolvesItsTwinInItsOwnBranch() throws Exception {
		WordprocessingMLPackage pkg = withBody("<w:tbl>" + TBL_PROPS + "<w:tr>"
				+ "<mc:AlternateContent><mc:Choice Requires=\"w14\">" + deletedCell(1, "C") + plainCell("Ckeep") + "</mc:Choice>"
				+ "<mc:Fallback>" + deletedCell(2, "F") + plainCell("Fkeep") + "</mc:Fallback></mc:AlternateContent></w:tr></w:tbl>");
		List<TrackedChange> changes = listed(pkg);
		assertEquals(changes.toString(), 1, changes.size());
		assertEquals(1, changes.get(0).twins.size());
		changes.get(0).accept();
		String xml = TrackedChangesTest.bodyXml(pkg);
		assertFalse(xml, xml.contains("cellDel") || xml.contains(">C<") || xml.contains(">F<"));
		assertTrue(xml, xml.contains("Ckeep") && xml.contains("Fkeep"));
		assertEquals("each branch's neighbour widened once", 2, count(xml, "w:gridSpan w:val=\"2\""));
		assertEquals(2, count(xml, "w:tcW w:w=\"2000\""));
		assertEquals(0, listed(pkg).size());
	}

	/** Each branch holding the deleted cell alone, a plain cell beside the element in the row: the row's neighbour is widened once, not once per branch. */
	@Test
	public void acceptingADeletedCellAloneInEachBranchWidensTheRowsNeighbourOnce() throws Exception {
		WordprocessingMLPackage pkg = withBody("<w:tbl>" + TBL_PROPS + "<w:tr>" + plainCell("keep")
				+ "<mc:AlternateContent><mc:Choice Requires=\"w14\">" + deletedCell(1, "C") + "</mc:Choice>"
				+ "<mc:Fallback>" + deletedCell(2, "F") + "</mc:Fallback></mc:AlternateContent></w:tr></w:tbl>");
		listed(pkg).get(0).accept();
		String xml = TrackedChangesTest.bodyXml(pkg);
		assertFalse(xml, xml.contains("cellDel") || xml.contains(">C<") || xml.contains(">F<"));
		assertEquals("the row's neighbour widened once", 1, count(xml, "w:gridSpan w:val=\"2\""));
		assertEquals(1, count(xml, "w:tcW w:w=\"2000\""));
		assertTrue("the element stays, its branches now empty of cells", xml.contains("<mc:Choice") && xml.contains("<mc:Fallback"));
	}

	private static String changedCell(int id, String text) {
		return cell(text, "<w:tcW w:w=\"1000\" w:type=\"dxa\"/><w:shd w:val=\"clear\" w:fill=\"FF0000\"/>"
				+ "<w:tcPrChange w:id=\"" + id + "\" w:author=\"A\"><w:tcPr><w:tcW w:w=\"1000\" w:type=\"dxa\"/></w:tcPr></w:tcPrChange>");
	}

	/**
	 * A {@code w:tcPrChange} in each branch's cell of a cell-level element (docx4j-python's second
	 * finding, its CR-012 section 10.10): the table's one property record is listed once; accepted,
	 * every branch's record goes; rejected, every branch's cell gets its recorded properties back.
	 */
	@Test
	public void resolvingATablePropertyRecordReachesEveryBranchsCell() throws Exception {
		String body = "<w:tbl>" + TBL_PROPS + "<w:tr>"
				+ "<mc:AlternateContent><mc:Choice Requires=\"w14\">" + changedCell(1, "C") + "</mc:Choice>"
				+ "<mc:Fallback>" + changedCell(2, "F") + "</mc:Fallback></mc:AlternateContent></w:tr></w:tbl>";
		WordprocessingMLPackage pkg = withBody(body);
		assertEquals(listed(pkg).toString(), 1, listed(pkg).size());
		TrackedChanges.acceptAll(pkg);
		String xml = TrackedChangesTest.bodyXml(pkg);
		assertEquals(xml, 0, count(xml, "tcPrChange"));
		assertEquals("accepted: both branches keep the shading", 2, count(xml, "w:fill=\"FF0000\""));
		pkg = withBody(body);
		TrackedChanges.rejectAll(pkg);
		xml = TrackedChangesTest.bodyXml(pkg);
		assertEquals(xml, 0, count(xml, "tcPrChange"));
		assertEquals("rejected: both branches lose the shading", 0, count(xml, "w:fill=\"FF0000\""));
		assertEquals(2, count(xml, "<w:tcW w:w=\"1000\""));
	}

	/** A {@code w:trPrChange} on the row in the branch Word does not read of a row-level element: accepted, the record goes. */
	@Test
	public void acceptingATablePropertyRecordReachesARowInAnotherBranch() throws Exception {
		String changedRow = "<w:tr><w:trPr><w:cantSplit/><w:trPrChange w:id=\"1\" w:author=\"A\"><w:trPr/></w:trPrChange></w:trPr>"
				+ plainCell("F") + "</w:tr>";
		WordprocessingMLPackage pkg = withBody("<w:tbl>" + TBL_PROPS
				+ "<mc:AlternateContent><mc:Choice Requires=\"w14\">" + plainRow("C") + "</mc:Choice><mc:Fallback>" + changedRow + "</mc:Fallback></mc:AlternateContent></w:tbl>");
		assertEquals(listed(pkg).toString(), 1, listed(pkg).size());
		TrackedChanges.acceptAll(pkg);
		String xml = TrackedChangesTest.bodyXml(pkg);
		assertEquals(xml, 0, count(xml, "trPrChange"));
		assertTrue(xml, xml.contains("<w:cantSplit/>"));
		assertEquals(0, listed(pkg).size());
	}

	/** A whole-table change's text is what Word shows: the rows of the branch it reads, not every branch's (docx4j-python, its CR-012 section 10.11). */
	@Test
	public void aTableChangesTextReadsTheBranchWordReads() throws Exception {
		WordprocessingMLPackage pkg = withBody("<w:tbl><w:tblPr><w:jc w:val=\"center\"/><w:tblPrChange w:id=\"1\" w:author=\"A\"><w:tblPr/></w:tblPrChange></w:tblPr>"
				+ "<w:tblGrid><w:gridCol w:w=\"1000\"/></w:tblGrid>"
				+ "<mc:AlternateContent><mc:Choice Requires=\"w14\">" + plainRow("C") + "</mc:Choice><mc:Fallback>" + plainRow("F") + "</mc:Fallback></mc:AlternateContent></w:tbl>");
		List<TrackedChange> changes = listed(pkg);
		assertEquals(changes.toString(), 1, changes.size());
		String text = changes.get(0).getText();
		assertTrue(text, text.contains("C"));
		assertFalse(text, text.contains("F"));
	}

	private static String spanned(String text, int span) {
		return cell(text, "<w:tcW w:w=\"" + (500 * span) + "\" w:type=\"dxa\"/><w:gridSpan w:val=\"" + span + "\"/>");
	}

	/**
	 * A rejected grid change collapsed over a row with a cell-level element (core-ts CR-013 phase 3's
	 * shape): a cell outside the element is in every branch's layout, so the new spans are counted from
	 * the old ones along every layout before any is written. Restored grid 6 x 500, current 3 x 1000;
	 * row 1 X (span 2) then Choice C (span 4) | Fallback F (span 4); row 2 a, b, c (span 2 each):
	 * rejected, the grid is 3 x 1000, X, a, b and c span 1 (no w:gridSpan), C and F span 2.
	 */
	@Test
	public void rejectingAGridChangeCountsEveryBranchsSpansFromTheOldOnes() throws Exception {
		String currentGrid = "<w:gridCol w:w=\"1000\"/><w:gridCol w:w=\"1000\"/><w:gridCol w:w=\"1000\"/>";
		String oldGrid = "<w:gridCol w:w=\"500\"/><w:gridCol w:w=\"500\"/><w:gridCol w:w=\"500\"/><w:gridCol w:w=\"500\"/><w:gridCol w:w=\"500\"/><w:gridCol w:w=\"500\"/>";
		WordprocessingMLPackage pkg = withBody("<w:tbl><w:tblPr><w:jc w:val=\"center\"/><w:tblPrChange w:id=\"1\" w:author=\"A\"><w:tblPr/></w:tblPrChange></w:tblPr>"
				+ "<w:tblGrid>" + currentGrid + "<w:tblGridChange w:id=\"2\"><w:tblGrid>" + oldGrid + "</w:tblGrid></w:tblGridChange></w:tblGrid>"
				+ "<w:tr>" + spanned("X", 2) + "<mc:AlternateContent><mc:Choice Requires=\"w14\">" + spanned("C", 4) + "</mc:Choice><mc:Fallback>" + spanned("F", 4) + "</mc:Fallback></mc:AlternateContent></w:tr>"
				+ "<w:tr>" + spanned("a", 2) + spanned("b", 2) + spanned("c", 2) + "</w:tr></w:tbl>");
		TrackedChanges.rejectAll(pkg);
		String xml = TrackedChangesTest.bodyXml(pkg);
		assertEquals(xml, 3, count(xml, "<w:gridCol "));
		assertEquals(xml, 3, count(xml, "<w:gridCol w:w=\"1000\"/>"));
		assertEquals("C and F span 2, the others none", 2, count(xml, "<w:gridSpan "));
		assertEquals(xml, 2, count(xml, "<w:gridSpan w:val=\"2\"/>"));
		assertFalse(xml, xml.contains("gridAfter") || xml.contains("gridBefore"));
	}

	/** The text model's view of a cell reads the branch Word reads (Texts.cellText through Trees.blockItems). */
	@Test
	public void acceptedViewReadsTheBranchWordReads() throws Exception {
		WordprocessingMLPackage pkg = withBody("<w:tbl><w:tblPr/><w:tblGrid><w:gridCol w:w=\"1000\"/></w:tblGrid><w:tr><w:tc>" + BLOCK + "</w:tc></w:tr></w:tbl>");
		TrackedChange change = listed(pkg).get(0);
		assertTrue(change.toString(), change.getText().contains("INSERTED"));
		assertEquals(1, change.twins.size());
	}

	private static int count(String s, String token) {
		int n = 0;
		for (int i = s.indexOf(token); i >= 0; i = s.indexOf(token, i + 1)) n++;
		return n;
	}
}
