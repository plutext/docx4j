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
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.io.ByteArrayInputStream;
import java.math.BigInteger;
import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

import org.docx4j.openpackaging.packages.WordprocessingMLPackage;
import org.docx4j.utils.ResourceUtils;
import org.junit.Test;

/**
 * CR-043's oracle: docx4j's Accept All and Reject All against Word's own, on the Word-made fixtures
 * in {@code revisions/} (their README says where each came from), compared structurally
 * ({@link DocumentDescription}). Each result is saved and reloaded first, so what is compared is
 * what docx4j writes.
 */
public class TrackedChangesWordTest {

	static WordprocessingMLPackage load(String resource) throws Exception {
		try (InputStream is = ResourceUtils.getResource(resource)) {
			return WordprocessingMLPackage.load(is);
		}
	}

	static WordprocessingMLPackage roundTrip(WordprocessingMLPackage pkg) throws Exception {
		ByteArrayOutputStream bos = new ByteArrayOutputStream();
		pkg.save(bos);
		return WordprocessingMLPackage.load(new ByteArrayInputStream(bos.toByteArray()));
	}

	/** The tracked file resolved by docx4j, against Word's resolution of it. */
	static void assertAll(String tracked, String golden, boolean accept) throws Exception {
		WordprocessingMLPackage pkg = load(tracked);
		if (accept) TrackedChanges.acceptAll(pkg);
		else TrackedChanges.rejectAll(pkg);
		// nothing is left, in any story or branch
		List<TrackedChange> left = new Lister(pkg, false).list();
		assertTrue(tracked + ": " + left, left.isEmpty());
		String ours = DocumentDescription.of(roundTrip(pkg));
		String word = DocumentDescription.of(load(golden));
		dump(tracked + (accept ? ".accept" : ".reject"), word, ours);
		assertEquals((accept ? "accept all of " : "reject all of ") + tracked, word, ours);
	}

	/** With {@code -Dcr043.out=<dir>}, both descriptions are written there for a diff when they differ. */
	static void dump(String name, String word, String ours) throws Exception {
		String dir = System.getProperty("cr043.out");
		if (dir == null || word.equals(ours)) return;
		String base = name.replaceAll("[^A-Za-z0-9.-]", "_");
		java.nio.file.Files.write(java.nio.file.Paths.get(dir, base + ".word.txt"), word.getBytes("UTF-8"));
		java.nio.file.Files.write(java.nio.file.Paths.get(dir, base + ".ours.txt"), ours.getBytes("UTF-8"));
	}

	/**
	 * Every Word-made fixture, accepted all and rejected all: nothing is left in any story or branch,
	 * and the result saves and loads again.
	 */
	@Test
	public void everyFixtureResolves() throws Exception {
		List<java.nio.file.Path> files = new ArrayList<java.nio.file.Path>();
		try (java.util.stream.Stream<java.nio.file.Path> walk = java.nio.file.Files.walk(java.nio.file.Paths.get("src/test/resources/revisions"))) {
			walk.filter(p -> p.toString().endsWith(".docx")).sorted().forEach(files::add);
		}
		assertTrue(files.size() > 100);
		for (java.nio.file.Path file : files) {
			for (boolean accept : new boolean[] { true, false }) {
				WordprocessingMLPackage pkg = WordprocessingMLPackage.load(file.toFile());
				try {
					if (accept) TrackedChanges.acceptAll(pkg);
					else TrackedChanges.rejectAll(pkg);
					List<TrackedChange> left = new Lister(pkg, false).list();
					assertTrue(file + ": " + left, left.isEmpty());
					roundTrip(pkg);
				} catch (RuntimeException e) {
					throw new AssertionError((accept ? "accept all of " : "reject all of ") + file, e);
				}
			}
		}
	}

	/**
	 * Every Word-made fixture's changes resolved one at a time, listing again after each, accepting
	 * and rejecting in turn: each resolution takes its change away (the listing shrinks), and the
	 * result saves and loads again.
	 */
	@Test
	public void everyChangeOneByOne() throws Exception {
		List<java.nio.file.Path> files = new ArrayList<java.nio.file.Path>();
		try (java.util.stream.Stream<java.nio.file.Path> walk = java.nio.file.Files.walk(java.nio.file.Paths.get("src/test/resources/revisions"))) {
			walk.filter(p -> p.toString().endsWith(".docx")).sorted().forEach(files::add);
		}
		for (java.nio.file.Path file : files) {
			WordprocessingMLPackage pkg = WordprocessingMLPackage.load(file.toFile());
			boolean accept = true;
			try {
				for (int round = 0; ; round++) {
					TrackedChange next = null;
					int resolvable = 0;
					for (TrackedChange change : TrackedChanges.in(pkg)) {
						resolvable++;
						if (next == null) next = change;
					}
					if (next == null) break;
					assertTrue(file + ": no end", round < 500);
					if (accept) next.accept();
					else next.reject();
					accept = !accept;
					int after = 0;
					after = TrackedChanges.in(pkg).size();
					assertTrue(file + ": " + next + " left " + after + " of " + resolvable, after < resolvable);
				}
				roundTrip(pkg);
			} catch (RuntimeException e) {
				throw new AssertionError("one by one in " + file, e);
			}
		}
	}

	@Test
	public void cr043ProbeAcceptAll() throws Exception {
		assertAll("revisions/cr043/cr043-tracked.docx", "revisions/cr043/cr043-accept-all.docx", true);
	}

	@Test
	public void cr043ProbeRejectAll() throws Exception {
		assertAll("revisions/cr043/cr043-tracked.docx", "revisions/cr043/cr043-reject-all.docx", false);
	}

	@Test
	public void equationsAcceptAll() throws Exception {
		assertAll("tracked-changes-equations.docx", "revisions/cr043/equations-accept-all.docx", true);
	}

	@Test
	public void equationsRejectAll() throws Exception {
		assertAll("tracked-changes-equations.docx", "revisions/cr043/equations-reject-all.docx", false);
	}

	@Test
	public void check18AcceptAll() throws Exception {
		assertAll("revisions/editor/revisions/revisions-word15.docx", "revisions/core-ts/check18/accept-all.docx", true);
	}

	@Test
	public void check18RejectAll() throws Exception {
		assertAll("revisions/editor/revisions/revisions-word15.docx", "revisions/core-ts/check18/reject-all.docx", false);
	}

	@Test
	public void reviewWord15Accepted() throws Exception {
		assertAll("revisions/editor/review/review-for-word.docx", "revisions/editor/review/review-for-word-word15-accepted.docx", true);
	}

	/**
	 * Word 2010 and Word 15 keep the comment on the inserted paragraph whose markers lie outside its
	 * {@code w:ins}, its range markers gone; Word 365 removes it (CR-043 section 15), as docx4j does. So the
	 * older Words' saves are compared without that comment, the one difference, found by its text (Word
	 * renumbers comments on save).
	 */
	static void assertAllButComment(String tracked, String golden, String comment) throws Exception {
		WordprocessingMLPackage pkg = load(tracked);
		TrackedChanges.rejectAll(pkg);
		String ours = DocumentDescription.of(roundTrip(pkg));
		WordprocessingMLPackage word = load(golden);
		BigInteger id = null;
		for (org.docx4j.wml.Comments.Comment c : word.getMainDocumentPart().getCommentsPart().getJaxbElement().getComment()) {
			org.docx4j.finders.ClassFinder texts = new org.docx4j.finders.ClassFinder(org.docx4j.wml.Text.class);
			new org.docx4j.TraversalUtil(c.getContent(), texts);
			StringBuilder sb = new StringBuilder();
			for (Object t : texts.results) sb.append(((org.docx4j.wml.Text) t).getValue());
			if (sb.toString().contains(comment)) id = c.getId();
		}
		assertTrue("no comment " + comment + " in " + golden, id != null);
		Removals older = new Removals(word);
		older.removeComment(id);
		older.flush();
		String theirs = DocumentDescription.of(roundTrip(word));
		dump(tracked + ".reject-older", theirs, ours);
		assertEquals("reject all of " + tracked + ", but the comment " + comment, theirs, ours);
	}

	@Test
	public void reviewWord15Rejected() throws Exception {
		assertAllButComment("revisions/editor/review/review-for-word.docx", "revisions/editor/review/review-for-word-word15-rejected.docx",
				"Changed from arrears, as agreed.");
	}

	/** Word 365 removes the comment whose range Reject All leaves empty, wherever its markers were (CR-043 section 15). */
	@Test
	public void reviewWord365Rejected() throws Exception {
		assertAll("revisions/editor/review/review-for-word.docx", "revisions/editor/review/review-for-word-word365-rejected.docx", false);
	}

	/** docx4j-python's artefact 8 (CR-043 section 15): a comment on an insertion's text, every marker outside the {@code w:ins}. */
	@Test
	public void python8AcceptAll() throws Exception {
		assertAll("revisions/python/artefact8/8-tracked-changes.docx", "revisions/python/artefact8/8-word-accepted.docx", true);
	}

	/** Rejected, the comment goes, and with the last comment its parts and {@code people.xml}. */
	@Test
	public void python8RejectAll() throws Exception {
		assertAll("revisions/python/artefact8/8-tracked-changes.docx", "revisions/python/artefact8/8-word-rejected.docx", false);
		WordprocessingMLPackage pkg = load("revisions/python/artefact8/8-tracked-changes.docx");
		TrackedChanges.rejectAll(pkg);
		WordprocessingMLPackage saved = roundTrip(pkg);
		org.docx4j.openpackaging.parts.WordprocessingML.MainDocumentPart main = saved.getMainDocumentPart();
		assertNull(main.getCommentsPart());
		assertNull(main.getCommentsExtendedPart());
		assertNull(main.getCommentsIdsPart());
		assertNull(main.getPeoplePart());
		for (String name : new String[] { "/word/comments.xml", "/word/commentsExtended.xml", "/word/commentsIds.xml", "/word/people.xml" }) {
			assertNull(name, saved.getParts().get(new org.docx4j.openpackaging.parts.PartName(name)));
		}
	}

	private static final String COMMENTS = "revisions/cr043/comments/";

	/** CR-043 U21 (Word 365, 21 October 2026): Accept All of a deletion whose comment's markers lie outside the {@code w:del}; the comment goes whole, a control comment stays. */
	@Test
	public void u21AcceptAll() throws Exception {
		assertAll(COMMENTS + "c43-u21-accept-all-deletion-comment-outside.docx", COMMENTS + "c43-u21-word365-accepted.docx", true);
	}

	/** U22: one insertion rejected removes the only comment while a tracked deletion is left: the comment parts go, {@code people.xml} stays. */
	@Test
	public void u22RejectOne() throws Exception {
		String tracked = COMMENTS + "c43-u22-reject-one-last-comment-revisions-left.docx";
		WordprocessingMLPackage pkg = load(tracked);
		TrackedChange insertion = null;
		for (TrackedChange c : TrackedChanges.in(pkg)) if (c.getType() == TrackedChange.Type.ADDED) insertion = c;
		assertTrue(tracked, insertion != null);
		insertion.reject();
		WordprocessingMLPackage saved = roundTrip(pkg);
		String ours = DocumentDescription.of(saved);
		String word = DocumentDescription.of(load(COMMENTS + "c43-u22-word365-rejected-one.docx"));
		dump(tracked + ".reject-one", word, ours);
		assertEquals("the insertion rejected in " + tracked, word, ours);
		assertNull(saved.getMainDocumentPart().getCommentsPart());
		assertTrue("people.xml stays while a tracked change is left", saved.getMainDocumentPart().getPeoplePart() != null);
		assertEquals(1, TrackedChanges.in(saved).size());
	}

	/** U23: Reject All of two inserted paragraphs commented whole removes the comment, its range start and end in different paragraphs; a control comment stays. */
	@Test
	public void u23RejectAll() throws Exception {
		assertAll(COMMENTS + "c43-u23-reject-all-two-paragraph-insertion-commented.docx", COMMENTS + "c43-u23-word365-rejected.docx", false);
		WordprocessingMLPackage pkg = load(COMMENTS + "c43-u23-reject-all-two-paragraph-insertion-commented.docx");
		TrackedChanges.rejectAll(pkg);
		assertEquals(1, roundTrip(pkg).getMainDocumentPart().getCommentsPart().getJaxbElement().getComment().size());
	}

	@Test
	public void reviewWord2010Accepted() throws Exception {
		assertAll("revisions/editor/review/review-for-word.docx", "revisions/editor/review/review-for-word-word2010-accepted.docx", true);
	}

	@Test
	public void reviewWord2010Rejected() throws Exception {
		assertAllButComment("revisions/editor/review/review-for-word.docx", "revisions/editor/review/review-for-word-word2010-rejected.docx",
				"Changed from arrears, as agreed.");
	}

	/** Check 28: bold over two runs, and two tables' records ({@code w:trPrChange} included). */
	@Test
	public void check28AcceptAll() throws Exception {
		assertAll("revisions/core-ts/check28/28a-changes.docx", "revisions/core-ts/check28/28a-accept-all.docx", true);
	}

	@Test
	public void check28RejectAll() throws Exception {
		assertAll("revisions/core-ts/check28/28a-changes.docx", "revisions/core-ts/check28/28a-reject-all.docx", false);
	}

	/** Check 26: a paragraph centred, and one indented, rejected: back to the input Word saved first. */
	@Test
	public void check26CentredRejected() throws Exception {
		assertAll("revisions/core-ts/check26/26a-centred.docx", "revisions/core-ts/check26/26-input.docx", false);
	}

	@Test
	public void check26IndentedRejected() throws Exception {
		assertAll("revisions/core-ts/check26/26d-indented.docx", "revisions/core-ts/check26/26-input.docx", false);
	}

	@Test
	public void check33WholeParagraphAccepted() throws Exception {
		assertAll("revisions/core-ts/check33/33a-deleted.docx", "revisions/core-ts/check33/33b-accepted.docx", true);
	}
}
