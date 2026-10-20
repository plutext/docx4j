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
 * CR-051 section 10.3 item 3 and section 10.5 item 3: tracked changes inside a block-level and a
 * paragraph-level {@code mc:AlternateContent}, listed once from the branch Word reads, with the
 * other branches' changes as twins that resolve with them.  The trees are unmarshalled outside a
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
