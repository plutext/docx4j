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
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.List;

import org.docx4j.XmlUtils;
import org.docx4j.jaxb.Context;
import org.docx4j.openpackaging.packages.WordprocessingMLPackage;
import org.docx4j.openpackaging.parts.WordprocessingML.CommentsExtendedPart;
import org.docx4j.openpackaging.parts.WordprocessingML.CommentsExtensiblePart;
import org.docx4j.openpackaging.parts.WordprocessingML.CommentsIdsPart;
import org.docx4j.openpackaging.parts.WordprocessingML.CommentsPart;
import org.docx4j.openpackaging.parts.relationships.Namespaces;
import org.docx4j.wml.Body;
import org.docx4j.wml.Comments;
import org.docx4j.wml.Document;
import org.docx4j.wml.P;
import org.junit.Test;

/**
 * CR-043's API contract and the rules no Word-made fixture holds (CR-043 section 11's unmeasured
 * list), on handwritten markup; and two of the Word-made fixtures read for what the listing says.
 */
public class TrackedChangesTest {

	static final String W = "http://schemas.openxmlformats.org/wordprocessingml/2006/main";
	static final String NS = "xmlns:w=\"" + W + "\" xmlns:r=\"http://schemas.openxmlformats.org/officeDocument/2006/relationships\""
			+ " xmlns:w14=\"http://schemas.microsoft.com/office/word/2010/wordml\""
			+ " xmlns:w16du=\"http://schemas.microsoft.com/office/word/2023/wordml/word16du\"";

	/** A package whose body is {@code bodyXml} (the content of {@code w:body}). */
	static WordprocessingMLPackage withBody(String bodyXml) throws Exception {
		WordprocessingMLPackage pkg = WordprocessingMLPackage.createPackage();
		Document doc = (Document) XmlUtils.unmarshalString("<w:document " + NS + "><w:body>" + bodyXml + "</w:body></w:document>");
		pkg.getMainDocumentPart().setJaxbElement(doc);
		return pkg;
	}

	static List<P> paragraphs(WordprocessingMLPackage pkg) {
		List<P> out = new ArrayList<P>();
		for (Object o : pkg.getMainDocumentPart().getJaxbElement().getBody().getContent()) {
			if (XmlUtils.unwrap(o) instanceof P) out.add((P) XmlUtils.unwrap(o));
		}
		return out;
	}

	static String bodyXml(WordprocessingMLPackage pkg) {
		return XmlUtils.marshaltoString(pkg.getMainDocumentPart().getJaxbElement(), true, false);
	}

	private static String ins(int id, String author, String text) {
		return "<w:ins w:id=\"" + id + "\" w:author=\"" + author + "\"><w:r><w:t xml:space=\"preserve\">" + text + "</w:t></w:r></w:ins>";
	}

	private static String del(int id, String author, String text) {
		return "<w:del w:id=\"" + id + "\" w:author=\"" + author + "\"><w:r><w:delText xml:space=\"preserve\">" + text + "</w:delText></w:r></w:del>";
	}

	private static String run(String text) {
		return "<w:r><w:t xml:space=\"preserve\">" + text + "</w:t></w:r>";
	}

	// --- listing --------------------------------------------------------------------------------------

	/** Check 22: a deleted paragraph is one change, a lone deleted mark one, a deletion across a mark one. */
	@Test
	public void deletionsThatTouchAreOneChange() throws Exception {
		WordprocessingMLPackage pkg = TrackedChangesWordTest.load("revisions/core-ts/check22/22-deleted.docx");
		List<String> texts = new ArrayList<String>();
		for (TrackedChange change : TrackedChanges.in(pkg)) {
			assertEquals(TrackedChange.Type.DELETED, change.getType());
			texts.add(change.getText());
		}
		assertEquals("[Two.\r, \r, six.\rSeven ]", texts.toString());
	}

	/** Check 23: another author splits a group; a deletion and an insertion are two changes. */
	@Test
	public void anotherAuthorOrTypeSplits() throws Exception {
		WordprocessingMLPackage pkg = withBody("<w:p>" + del(1, "A", "one ") + del(2, "B", "two ") + ins(3, "B", "three ") + ins(4, "B", "four") + "</w:p>");
		List<TrackedChange> changes = TrackedChanges.in(pkg);
		assertEquals(3, changes.size());
		assertEquals("one ", changes.get(0).getText());
		assertEquals("two ", changes.get(1).getText());
		assertEquals("three four", changes.get(2).getText());
		assertEquals(2, changes.get(2).getPieces().size());
		assertEquals(TrackedChange.Kind.RUN_INSERTION, changes.get(2).getKind());
	}

	/** The equations of CR-010's fixture list as Word counts them: 12. */
	@Test
	public void equationsListAsWordCounts() throws Exception {
		WordprocessingMLPackage pkg = TrackedChangesWordTest.load("tracked-changes-equations.docx");
		assertEquals(12, TrackedChanges.in(pkg).size());
	}

	/** A text box's changes are listed once (from mc:Choice); accepting one resolves its mc:Fallback twin (C22). */
	@Test
	public void textBoxTwinResolvesWithItsChange() throws Exception {
		WordprocessingMLPackage pkg = TrackedChangesWordTest.load("revisions/cr043/cr043-tracked.docx");
		int listed = 0;
		TrackedChange inserted = null;
		for (TrackedChange change : TrackedChanges.in(pkg)) {
			if (change.getText().contains("C22 inserted")) {
				listed++;
				inserted = change;
			}
		}
		assertEquals(1, listed);
		assertEquals(1, inserted.twins.size());
		inserted.accept();
		String xml = bodyXml(pkg);
		int at = xml.indexOf("C22 inserted");
		assertTrue(at > 0);
		assertTrue("both branches keep the text", xml.indexOf("C22 inserted", at + 1) > 0);
		for (TrackedChange change : TrackedChanges.in(pkg)) assertFalse(change.getText().contains("C22 inserted"));
		// and the Fallback's insertion went with it
		List<TrackedChange> all = new Lister(pkg, false).list();
		for (TrackedChange change : all) assertFalse(change.toString(), change.getText().contains("C22 inserted"));
	}

	// --- dates --------------------------------------------------------------------------------------

	@Test
	public void dateUtcIsPreferred() throws Exception {
		WordprocessingMLPackage pkg = withBody("<w:p><w:ins w:id=\"1\" w:author=\"A\" w:date=\"2026-01-01T10:00:00Z\" w16du:dateUtc=\"2026-01-01T00:00:00Z\">"
				+ run("x") + "</w:ins></w:p>");
		assertEquals(Instant.parse("2026-01-01T00:00:00Z"), TrackedChanges.in(pkg).get(0).getDate());
	}

	/** Without {@code w16du:dateUtc}, {@code w:date} is local wall-clock time, its Z notwithstanding (check 23). */
	@Test
	public void wordDateIsLocalTime() throws Exception {
		WordprocessingMLPackage pkg = withBody("<w:p><w:ins w:id=\"1\" w:author=\"A\" w:date=\"2026-01-01T10:00:00Z\">" + run("x") + "</w:ins></w:p>");
		assertEquals(LocalDateTime.of(2026, 1, 1, 10, 0).atZone(ZoneId.systemDefault()).toInstant(), TrackedChanges.in(pkg).get(0).getDate());
	}

	@Test
	public void noDateIsNull() throws Exception {
		WordprocessingMLPackage pkg = withBody("<w:p><w:ins w:id=\"1\" w:author=\"A\">" + run("x") + "</w:ins></w:p>");
		assertNull(TrackedChanges.in(pkg).get(0).getDate());
	}

	// --- the contract -----------------------------------------------------------------------------------

	@Test
	public void aViewIsStaleAfterTheNextAcceptOrReject() throws Exception {
		WordprocessingMLPackage pkg = withBody("<w:p>" + ins(1, "A", "one") + run(" plain ") + ins(2, "A", "two") + "</w:p>");
		List<TrackedChange> changes = TrackedChanges.in(pkg);
		assertEquals(2, changes.size());
		changes.get(1).accept();
		try {
			changes.get(0).accept();
			fail("a stale view accepted");
		} catch (IllegalStateException expected) {
			// list again
		}
		List<TrackedChange> again = TrackedChanges.in(pkg);
		assertEquals(1, again.size());
		again.get(0).reject();
		assertEquals(" plain two", TextOf.paragraph(paragraphs(pkg).get(0)));
	}

	/** A group's piece is a view of its own: rejected alone, it leaves the rest of the group pending. */
	@Test
	public void aPieceResolvesOnItsOwn() throws Exception {
		WordprocessingMLPackage pkg = withBody("<w:p>" + run("a ") + ins(1, "A", "one ") + ins(2, "A", "two") + "</w:p>");
		List<TrackedChange> changes = TrackedChanges.in(pkg);
		assertEquals(1, changes.size());
		assertEquals(2, changes.get(0).getPieces().size());
		changes.get(0).getPieces().get(0).reject();
		List<TrackedChange> left = TrackedChanges.in(pkg);
		assertEquals(1, left.size());
		assertEquals("two", left.get(0).getText());
		assertEquals("a two", TextOf.paragraph(paragraphs(pkg).get(0)));
	}

	/** Phase 1 lists table and section records but leaves them: unresolved in the report. */
	@Test
	public void recordsArePhase2() throws Exception {
		WordprocessingMLPackage pkg = withBody("<w:tbl><w:tblPr><w:tblW w:w=\"0\" w:type=\"auto\"/>"
				+ "<w:tblPrChange w:id=\"1\" w:author=\"A\"><w:tblPr><w:tblW w:w=\"5000\" w:type=\"pct\"/></w:tblPr></w:tblPrChange></w:tblPr>"
				+ "<w:tblGrid><w:gridCol w:w=\"1000\"/></w:tblGrid><w:tr><w:tc><w:p>" + run("cell") + "</w:p></w:tc></w:tr></w:tbl>"
				+ "<w:p>" + ins(2, "A", "text") + "</w:p>");
		List<TrackedChange> changes = TrackedChanges.in(pkg);
		assertEquals(2, changes.size());
		TrackedChange table = changes.get(0);
		assertEquals(TrackedChange.Kind.TABLE_PROPERTIES, table.getKind());
		assertFalse(table.isResolvable());
		try {
			table.accept();
			fail("phase 2's kind resolved");
		} catch (UnsupportedOperationException expected) {
			// phase 2
		}
		TrackedChangesReport report = TrackedChanges.report(pkg);
		assertEquals(2, report.getTotal());
		assertEquals(1, report.getUnresolved());
		assertEquals(1, TrackedChanges.acceptAll(pkg));
		List<TrackedChange> left = TrackedChanges.in(pkg);
		assertEquals(1, left.size());
		assertEquals(TrackedChange.Kind.TABLE_PROPERTIES, left.get(0).getKind());
		String json = TrackedChanges.report(pkg).toJson();
		assertTrue(json, json.startsWith("{\"total\":1,\"unresolved\":1,\"byKind\":{\"TABLE_PROPERTIES\":1},\"unresolvedKinds\":[\"TABLE_PROPERTIES\"],\"byAuthor\":{\"A\":1},\"byPart\":{\"/word/document.xml\":1}}"));
	}

	// --- the unmeasured rules (CR-043 section 11) ------------------------------------------------------------

	/** U1: a deleted mark with no paragraph after it in its list is dropped, with no join; nor across a table. */
	@Test
	public void noJoinAcrossAContainerBoundary() throws Exception {
		String deletedMark = "<w:pPr><w:rPr><w:del w:id=\"%d\" w:author=\"A\"/></w:rPr></w:pPr>";
		WordprocessingMLPackage pkg = withBody(
				"<w:p>" + String.format(deletedMark, 1) + run("before a table") + "</w:p>"
				+ "<w:tbl><w:tblGrid><w:gridCol w:w=\"1000\"/></w:tblGrid><w:tr><w:tc><w:p>" + String.format(deletedMark, 2) + run("a cell's last") + "</w:p></w:tc></w:tr></w:tbl>"
				+ "<w:p>" + String.format(deletedMark, 3) + run("the story's last") + "</w:p>"
				+ "<w:sectPr/>");
		assertEquals(3, TrackedChanges.acceptAll(pkg));
		assertEquals(2, paragraphs(pkg).size());
		assertEquals(0, TrackedChanges.in(pkg).size());
		assertFalse(bodyXml(pkg).contains("w:del "));
		Body body = pkg.getMainDocumentPart().getJaxbElement().getBody();
		assertNotNull(body.getSectPr());
	}

	/** A deleted mark joins: the result keeps the next paragraph's properties and paraId (C10b), and markers between go into it. */
	@Test
	public void aJoinKeepsTheNextParagraph() throws Exception {
		WordprocessingMLPackage pkg = withBody(
				"<w:p w14:paraId=\"00000001\"><w:pPr><w:pStyle w:val=\"Heading1\"/><w:rPr><w:del w:id=\"1\" w:author=\"A\"/></w:rPr></w:pPr>" + run("first ") + "</w:p>"
				+ "<w:bookmarkEnd w:id=\"9\"/>"
				+ "<w:p w14:paraId=\"00000002\"><w:pPr><w:jc w:val=\"right\"/></w:pPr>" + run("second") + "</w:p>");
		TrackedChanges.acceptAll(pkg);
		List<P> ps = paragraphs(pkg);
		assertEquals(1, ps.size());
		assertEquals("00000002", ps.get(0).getParaId());
		assertNull(ps.get(0).getPPr().getPStyle());
		assertEquals("first second", TextOf.paragraph(ps.get(0)));
		assertEquals(1, pkg.getMainDocumentPart().getJaxbElement().getBody().getContent().size());
	}

	/** U2: a field straddling a revision's edge - its end deleted - resolves as runs. */
	@Test
	public void aFieldStraddlingAnEdgeResolvesAsRuns() throws Exception {
		WordprocessingMLPackage pkg = withBody("<w:p><w:r><w:fldChar w:fldCharType=\"begin\"/></w:r><w:r><w:instrText> PAGE </w:instrText></w:r>"
				+ "<w:r><w:fldChar w:fldCharType=\"separate\"/></w:r>"
				+ "<w:del w:id=\"1\" w:author=\"A\"><w:r><w:delText>1</w:delText></w:r><w:r><w:fldChar w:fldCharType=\"end\"/></w:r><w:r><w:delText> after</w:delText></w:r></w:del></w:p>");
		List<TrackedChange> changes = TrackedChanges.in(pkg);
		assertEquals(1, changes.size());
		assertEquals(TrackedChange.Kind.RUN_DELETION, changes.get(0).getKind());
		WordprocessingMLPackage rejected = withBody(bodyOf(pkg));
		TrackedChanges.rejectAll(rejected);
		assertTrue(bodyXml(rejected).contains("fldCharType=\"end\""));
		TrackedChanges.acceptAll(pkg);
		assertFalse(bodyXml(pkg).contains("fldCharType=\"end\""));
	}

	private static String bodyOf(WordprocessingMLPackage pkg) {
		String xml = bodyXml(pkg);
		return xml.substring(xml.indexOf("<w:body>") + 8, xml.lastIndexOf("</w:body>"));
	}

	/** A bookmark with an end in removed content goes whole (C11); one around removed content goes too (C11b). */
	@Test
	public void bookmarksTouchingRemovedContentGo() throws Exception {
		WordprocessingMLPackage pkg = withBody("<w:p>" + run("a ") + "<w:bookmarkStart w:id=\"10\" w:name=\"half\"/>" + run("b ")
				+ "<w:del w:id=\"1\" w:author=\"A\"><w:r><w:delText>c </w:delText></w:r><w:bookmarkEnd w:id=\"10\"/></w:del>" + run("d ")
				+ "<w:bookmarkStart w:id=\"11\" w:name=\"around\"/>" + ins(2, "A", "e") + "<w:bookmarkEnd w:id=\"11\"/>"
				+ "<w:bookmarkStart w:id=\"12\" w:name=\"point\"/><w:bookmarkEnd w:id=\"12\"/></w:p>");
		WordprocessingMLPackage rejected = withBody(bodyOf(pkg));
		TrackedChanges.acceptAll(pkg);
		String xml = bodyXml(pkg);
		assertFalse(xml, xml.contains("half"));
		assertTrue(xml, xml.contains("around"));
		assertTrue(xml, xml.contains("point"));
		TrackedChanges.rejectAll(rejected);
		xml = bodyXml(rejected);
		assertTrue(xml, xml.contains("half"));
		assertFalse(xml, xml.contains("around"));
		assertFalse(xml, xml.contains("w:id=\"11\""));
		assertTrue(xml, xml.contains("point"));
	}

	/** U4 in part: an external relationship (a hyperlink's) in a deleted row goes with it. */
	@Test
	public void aHyperlinksRelationshipGoesWithIt() throws Exception {
		WordprocessingMLPackage pkg = WordprocessingMLPackage.createPackage();
		org.docx4j.relationships.Relationship rel = new org.docx4j.relationships.ObjectFactory().createRelationship();
		rel.setType(Namespaces.HYPERLINK);
		rel.setTarget("https://www.docx4java.org/");
		rel.setTargetMode("External");
		pkg.getMainDocumentPart().getRelationshipsPart().addRelationship(rel);
		String id = rel.getId();
		Document doc = (Document) XmlUtils.unmarshalString("<w:document " + NS + "><w:body><w:p>" + run("kept") + "</w:p>"
				+ "<w:tbl><w:tblGrid><w:gridCol w:w=\"1000\"/></w:tblGrid>"
				+ "<w:tr><w:tc><w:p>" + run("row one") + "</w:p></w:tc></w:tr>"
				+ "<w:tr><w:trPr><w:del w:id=\"1\" w:author=\"A\"/></w:trPr><w:tc><w:p><w:hyperlink r:id=\"" + id + "\">"
				+ "<w:del w:id=\"2\" w:author=\"A\"><w:r><w:delText>link</w:delText></w:r></w:del></w:hyperlink></w:p></w:tc></w:tr></w:tbl>"
				+ "<w:p/></w:body></w:document>");
		pkg.getMainDocumentPart().setJaxbElement(doc);
		assertNotNull(pkg.getMainDocumentPart().getRelationshipsPart().getRelationshipByID(id));
		TrackedChanges.acceptAll(pkg);
		assertTrue(bodyXml(pkg).contains("row one"));
		assertNull(pkg.getMainDocumentPart().getRelationshipsPart().getRelationshipByID(id));
	}

	/** U3: a removed comment's entries in commentsExtended, commentsIds and commentsExtensible go, and its reply with it. */
	@Test
	public void aRemovedCommentTakesItsRepliesAndEntries() throws Exception {
		WordprocessingMLPackage pkg = withBody("<w:p>" + run("before ")
				+ "<w:ins w:id=\"1\" w:author=\"A\"><w:commentRangeStart w:id=\"20\"/><w:commentRangeStart w:id=\"21\"/><w:r><w:t>commented</w:t></w:r></w:ins>"
				+ "<w:commentRangeEnd w:id=\"20\"/><w:commentRangeEnd w:id=\"21\"/>"
				+ "<w:r><w:commentReference w:id=\"20\"/></w:r><w:r><w:commentReference w:id=\"21\"/></w:r>"
				+ run(" after") + "</w:p>");
		CommentsPart comments = new CommentsPart();
		comments.setJaxbElement((Comments) XmlUtils.unmarshalString("<w:comments " + NS + ">"
				+ "<w:comment w:id=\"20\" w:author=\"A\"><w:p w14:paraId=\"0000000A\">" + run("the comment") + "</w:p></w:comment>"
				+ "<w:comment w:id=\"21\" w:author=\"B\"><w:p w14:paraId=\"0000000B\">" + run("the reply") + "</w:p></w:comment>"
				+ "</w:comments>"));
		pkg.getMainDocumentPart().addTargetPart(comments);
		String w15 = "xmlns:w15=\"http://schemas.microsoft.com/office/word/2012/wordml\"";
		CommentsExtendedPart extended = new CommentsExtendedPart();
		extended.setJaxbElement((org.docx4j.w15.CTCommentsEx) XmlUtils.unmarshalString("<w15:commentsEx " + w15 + ">"
				+ "<w15:commentEx w15:paraId=\"0000000A\" w15:done=\"0\"/><w15:commentEx w15:paraId=\"0000000B\" w15:paraIdParent=\"0000000A\" w15:done=\"0\"/>"
				+ "</w15:commentsEx>", Context.jc, org.docx4j.w15.CTCommentsEx.class));
		pkg.getMainDocumentPart().addTargetPart(extended);
		String cid = "xmlns:w16cid=\"http://schemas.microsoft.com/office/word/2016/wordml/cid\"";
		CommentsIdsPart ids = new CommentsIdsPart();
		ids.setJaxbElement((org.docx4j.w16cid.CTCommentsIds) XmlUtils.unmarshalString("<w16cid:commentsIds " + cid + ">"
				+ "<w16cid:commentId w16cid:paraId=\"0000000A\" w16cid:durableId=\"1000000A\"/><w16cid:commentId w16cid:paraId=\"0000000B\" w16cid:durableId=\"1000000B\"/>"
				+ "</w16cid:commentsIds>", Context.jc, org.docx4j.w16cid.CTCommentsIds.class));
		pkg.getMainDocumentPart().addTargetPart(ids);
		String cex = "xmlns:w16cex=\"http://schemas.microsoft.com/office/word/2018/wordml/cex\"";
		CommentsExtensiblePart extensible = new CommentsExtensiblePart();
		extensible.setJaxbElement((org.docx4j.w16cex.CTCommentsExtensible) XmlUtils.unmarshalString("<w16cex:commentsExtensible " + cex + ">"
				+ "<w16cex:commentExtensible w16cex:durableId=\"1000000A\"/><w16cex:commentExtensible w16cex:durableId=\"1000000B\"/>"
				+ "</w16cex:commentsExtensible>", Context.jc, org.docx4j.w16cex.CTCommentsExtensible.class));
		pkg.getMainDocumentPart().addTargetPart(extensible);

		TrackedChanges.rejectAll(pkg);
		assertEquals("before  after", TextOf.paragraph(paragraphs(pkg).get(0)));
		assertFalse(bodyXml(pkg).contains("comment"));
		assertEquals(0, comments.getJaxbElement().getComment().size());
		assertEquals(0, extended.getJaxbElement().getCommentEx().size());
		assertEquals(0, ids.getJaxbElement().getCommentId().size());
		assertEquals(0, extensible.getJaxbElement().getCommentExtensible().size());
	}

	/** An inserted equation structure rejected goes whole; accepted, its control keeps the recorded run properties. */
	@Test
	public void mathStructures() throws Exception {
		WordprocessingMLPackage pkg = TrackedChangesWordTest.load("tracked-changes-equations.docx");
		TrackedChange inserted = TrackedChanges.in(pkg).get(1);
		assertTrue(inserted.getText(), inserted.getText().endsWith("s-1."));
		boolean struct = false;
		for (TrackedChange piece : inserted.getPieces()) struct |= piece.getKind() == TrackedChange.Kind.MATH_STRUCTURE_INSERTION;
		assertTrue(struct);
		inserted.accept();
		String xml = bodyXml(pkg);
		int sSup = xml.indexOf("<m:sSup>");
		assertTrue(sSup > 0);
		String ctrlPr = xml.substring(xml.indexOf("<m:ctrlPr>", sSup), xml.indexOf("</m:ctrlPr>", sSup));
		assertFalse(ctrlPr, ctrlPr.contains("w:ins"));
		assertTrue(ctrlPr, ctrlPr.contains("Cambria Math"));

		WordprocessingMLPackage rejected = TrackedChangesWordTest.load("tracked-changes-equations.docx");
		TrackedChanges.in(rejected).get(1).reject();
		xml = bodyXml(rejected);
		assertTrue(xml.indexOf("<m:sSup>") > xml.indexOf("Air density") || xml.indexOf("<m:sSup>") < 0);
	}

	/** The paragraph text of a {@link P}, accepted view. */
	static final class TextOf {
		static String paragraph(P p) {
			return Trees.paragraphText(p, Trees.View.ACCEPTED);
		}
	}
}
