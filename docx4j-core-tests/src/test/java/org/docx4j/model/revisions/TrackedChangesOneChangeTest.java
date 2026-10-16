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
import static org.junit.Assert.assertNotNull;

import java.util.List;

import org.docx4j.openpackaging.packages.WordprocessingMLPackage;
import org.junit.Test;

/**
 * One change accepted or rejected, against Word's Review tab doing the same (Accept This Change,
 * Reject Change) on the Word-made fixtures: docx4j-core-ts's checks 18, 20 and 22, and Word's Reject
 * All of check 25's list changes. The change is the one the check's tester clicked in, found by its
 * text and type; the rest of the document keeps its revisions, which the comparison includes.
 */
public class TrackedChangesOneChangeTest {

	private static final String C18 = "revisions/core-ts/check18/";
	private static final String C20 = "revisions/core-ts/check20/";
	private static final String C22 = "revisions/core-ts/check22/";
	private static final String C25 = "revisions/core-ts/check25/";
	private static final String WORD15 = "revisions/editor/revisions/revisions-word15.docx";

	/** The first change listed whose text holds {@code needle}, of the given type. */
	static TrackedChange find(List<TrackedChange> changes, String needle, TrackedChange.Type type) {
		for (TrackedChange change : changes) {
			if (change.getType() == type && change.getText().contains(needle)) return change;
		}
		// a move's destination whose text was all deleted again (check 20 case 08) has no accepted text:
		// the tester clicked in its deleted words
		for (TrackedChange change : changes) {
			if (change.getType() == type && change.getKind() == TrackedChange.Kind.MOVE
					&& change.getText().isEmpty()) return change;
		}
		return null;
	}

	static void assertOne(String tracked, String golden, String needle, TrackedChange.Type type, boolean accept) throws Exception {
		WordprocessingMLPackage pkg = TrackedChangesWordTest.load(tracked);
		TrackedChange change = find(TrackedChanges.in(pkg), needle, type);
		assertNotNull("no " + type + " change holding \"" + needle + "\" in " + tracked, change);
		if (accept) change.accept();
		else change.reject();
		String ours = DocumentDescription.of(TrackedChangesWordTest.roundTrip(pkg));
		String word = DocumentDescription.of(TrackedChangesWordTest.load(golden));
		TrackedChangesWordTest.dump(tracked + (accept ? ".accept-one" : ".reject-one") + "." + golden.replaceAll(".*/", ""), word, ours);
		assertEquals((accept ? "accept " : "reject ") + change + " in " + tracked, word, ours);
	}

	// --- check 18: a move, from either half ---------------------------------------------------------------

	private static final String MOVED = "The second paragraph of the move";

	@Test
	public void check18AcceptDestination() throws Exception {
		assertOne(WORD15, C18 + "accept-destination.docx", MOVED, TrackedChange.Type.ADDED, true);
	}

	@Test
	public void check18AcceptSource() throws Exception {
		assertOne(WORD15, C18 + "accept-source.docx", MOVED, TrackedChange.Type.DELETED, true);
	}

	@Test
	public void check18RejectDestination() throws Exception {
		assertOne(WORD15, C18 + "reject-destination.docx", MOVED, TrackedChange.Type.ADDED, false);
	}

	@Test
	public void check18RejectSource() throws Exception {
		assertOne(WORD15, C18 + "reject-source.docx", MOVED, TrackedChange.Type.DELETED, false);
	}

	// --- check 20: edited and broken moves, resolved from the destination's "second" ---------------------------

	private static final String SECOND = "second";

	private static void check20(String name, boolean accept) throws Exception {
		assertOne(C20 + name + ".docx", C20 + name + (accept ? "-accept" : "-reject") + ".docx", SECOND, TrackedChange.Type.ADDED, accept);
	}

	@Test public void check20TypeInsideAccept() throws Exception { check20("01-type-inside-a", true); }
	@Test public void check20TypeInsideReject() throws Exception { check20("01-type-inside-a", false); }
	@Test public void check20TypeAtStartAccept() throws Exception { check20("03-type-at-start", true); }
	@Test public void check20TypeAtStartReject() throws Exception { check20("03-type-at-start", false); }
	@Test public void check20TypeAtEndAccept() throws Exception { check20("04-type-at-end", true); }
	@Test public void check20TypeAtEndReject() throws Exception { check20("04-type-at-end", false); }
	@Test public void check20DeletePartAAccept() throws Exception { check20("06-delete-part-a", true); }
	@Test public void check20DeletePartAReject() throws Exception { check20("06-delete-part-a", false); }
	@Test public void check20DeletePartBAccept() throws Exception { check20("07-delete-part-b", true); }
	@Test public void check20DeletePartBReject() throws Exception { check20("07-delete-part-b", false); }
	@Test public void check20DeleteAllAccept() throws Exception { check20("08-delete-all", true); }
	@Test public void check20DeleteAllReject() throws Exception { check20("08-delete-all", false); }
	@Test public void check20MoveAgainAccept() throws Exception { check20("10-move-again", true); }
	@Test public void check20MoveAgainReject() throws Exception { check20("10-move-again", false); }
	@Test public void check20PartnerMissingAccept() throws Exception { check20("12-partner-missing", true); }
	@Test public void check20PartnerMissingReject() throws Exception { check20("12-partner-missing", false); }
	@Test public void check20MoveToNoRangesAccept() throws Exception { check20("13-moveTo-no-ranges", true); }
	@Test public void check20MoveToNoRangesReject() throws Exception { check20("13-moveTo-no-ranges", false); }

	@Test
	public void check20MoveFromNoRangesAcceptSource() throws Exception {
		assertOne(C20 + "moveFrom-no-ranges.docx", C20 + "14-moveFrom-no-ranges-accept-input.docx", SECOND, TrackedChange.Type.DELETED, true);
	}

	@Test
	public void check20MoveFromNoRangesRejectSource() throws Exception {
		assertOne(C20 + "moveFrom-no-ranges.docx", C20 + "14-moveFrom-no-ranges-reject-input.docx", SECOND, TrackedChange.Type.DELETED, false);
	}

	@Test
	public void check20MoveFromNoRangesAcceptDestination() throws Exception {
		assertOne(C20 + "moveFrom-no-ranges.docx", C20 + "14-moveFrom-no-ranges-accept-dest-input.docx", SECOND, TrackedChange.Type.ADDED, true);
	}

	@Test
	public void check20MoveFromNoRangesRejectDestination() throws Exception {
		assertOne(C20 + "moveFrom-no-ranges.docx", C20 + "14-moveFrom-no-ranges-reject-dest-input.docx", SECOND, TrackedChange.Type.ADDED, false);
	}

	// --- check 22: a deleted paragraph and a deletion across a mark: one change each -----------------------------

	@Test
	public void check22AcceptWhole() throws Exception {
		assertOne(C22 + "22-deleted.docx", C22 + "22-accept-whole.docx", "Two.", TrackedChange.Type.DELETED, true);
	}

	@Test
	public void check22RejectWhole() throws Exception {
		assertOne(C22 + "22-deleted.docx", C22 + "22-reject-whole.docx", "Two.", TrackedChange.Type.DELETED, false);
	}

	@Test
	public void check22AcceptAcross() throws Exception {
		assertOne(C22 + "22-deleted.docx", C22 + "22-accept-across.docx", "six.", TrackedChange.Type.DELETED, true);
	}

	@Test
	public void check22RejectAcross() throws Exception {
		assertOne(C22 + "22-deleted.docx", C22 + "22-reject-across.docx", "six.", TrackedChange.Type.DELETED, false);
	}

	// --- check 25: a list change rejected, the level Word writes back -----------------------------------------

	@Test
	public void check25BulletsRejectAll() throws Exception {
		TrackedChangesWordTest.assertAll(C25 + "25a-bullets.docx", C25 + "25b-bullets-reject.docx", false);
	}

	@Test
	public void check25BulletDemoteRejectAll() throws Exception {
		TrackedChangesWordTest.assertAll(C25 + "25e-bullet-demote.docx", C25 + "25f-bullet-demote-reject.docx", false);
	}
}
