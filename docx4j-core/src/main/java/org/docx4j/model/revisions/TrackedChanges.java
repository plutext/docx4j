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

import java.util.ArrayList;
import java.util.List;

import org.docx4j.openpackaging.packages.WordprocessingMLPackage;
import org.docx4j.openpackaging.parts.JaxbXmlPart;

/**
 * Listing, accepting and rejecting a document's tracked changes, in every story: the main
 * document, headers, footers, footnotes, endnotes and comments, and inside content controls, text
 * boxes (both branches of {@code mc:AlternateContent}) and equations (CR-043). Word 365's own
 * Accept All and Reject All are the oracle; the rules, and where each comes from, are CR-043's
 * section 3.2.
 *
 * <pre>
 * for (TrackedChange change : TrackedChanges.in(pkg)) System.out.println(change);
 * TrackedChanges.acceptAll(pkg);   // or rejectAll
 * </pre>
 *
 * <p>This release lists table, grid, row, cell and section property records
 * ({@code w:tblPrChange}, {@code w:sectPrChange}, ...) but does not yet resolve them (CR-043 phase 2):
 * {@code acceptAll} and {@code rejectAll} leave them, and {@link #report} counts them as
 * unresolved.</p>
 *
 * @since 17.3.3
 */
public final class TrackedChanges {

	private TrackedChanges() {}

	/**
	 * Every tracked change, story by story (main document, footnotes, endnotes, comments, headers
	 * and footers; a text box's after the story it is in), each in document order, grouped as Office
	 * JS groups them. A text box's changes are listed once, from its {@code mc:Choice}; resolving one
	 * resolves its {@code mc:Fallback} twin too. The views are valid until the next accept or reject
	 * on the package.
	 */
	public static List<TrackedChange> in(WordprocessingMLPackage pkg) {
		List<TrackedChange> changes = new Lister(pkg, true).list();
		Generation generation = Generation.of(pkg);
		for (TrackedChange change : changes) stamp(change, generation);
		return changes;
	}

	/** A change, and the pieces it groups (each a view that can be resolved on its own), valid from now. */
	private static void stamp(TrackedChange change, Generation generation) {
		change.generation = generation;
		if (change.target instanceof TrackedChange.GroupT) {
			for (TrackedChange piece : ((TrackedChange.GroupT) change.target).pieces) stamp(piece, generation);
		}
	}

	/** Accepts every change this release resolves; returns how many {@link #in} listed. */
	public static int acceptAll(WordprocessingMLPackage pkg) {
		return resolveAll(pkg, true);
	}

	/** Rejects every change this release resolves; returns how many {@link #in} listed. */
	public static int rejectAll(WordprocessingMLPackage pkg) {
		return resolveAll(pkg, false);
	}

	/** Counts by kind, author and part; the kinds this release does not resolve counted as unresolved. */
	public static TrackedChangesReport report(WordprocessingMLPackage pkg) {
		return new TrackedChangesReport(in(pkg));
	}

	/**
	 * Last change first, so that a paragraph join never disturbs a change still to do; then the list
	 * is read again and the pass repeated until nothing is left, since resolving a move can carry a
	 * deletion nested in it out as a change of its own, and resolving a broken move leaves plain
	 * {@code w:ins}/{@code w:del} (core-ts {@code Body.resolveAll}); then what is never listed is swept.
	 * Both branches of a text box's {@code mc:AlternateContent} are walked.
	 */
	private static int resolveAll(WordprocessingMLPackage pkg, boolean accept) {
		int count = resolvable(new Lister(pkg, true).list()).size();
		Removals removals = new Removals(pkg);
		List<TrackedChange> changes = resolvable(new Lister(pkg, false).list());
		int stalled = 0;
		for (int round = 0; !changes.isEmpty() && round < 20 && stalled < 2; round++) {
			for (int i = changes.size() - 1; i >= 0; i--) Resolver.resolve(changes.get(i), accept, removals);
			List<TrackedChange> left = resolvable(new Lister(pkg, false).list());
			stalled = left.size() >= changes.size() ? stalled + 1 : 0;
			changes = left;
		}
		sweep(pkg, removals);
		removals.flush();
		Generation.of(pkg).advance();
		return count;
	}

	private static List<TrackedChange> resolvable(List<TrackedChange> changes) {
		List<TrackedChange> out = new ArrayList<TrackedChange>();
		for (TrackedChange change : changes) if (change.isResolvable()) out.add(change);
		return out;
	}

	/**
	 * What is never listed: a {@code w:customXmlInsRangeStart} or {@code End} left without the form
	 * of an inserted control marks nothing that can be found, and goes either way (core-ts
	 * {@code resolveUnlisted}).
	 */
	private static void sweep(WordprocessingMLPackage pkg, Removals removals) {
		List<JaxbXmlPart<?>> done = new ArrayList<JaxbXmlPart<?>>();
		for (Story story : new Lister(pkg, false).stories()) {
			if (done.contains(story.part)) continue;
			done.add(story.part);
			removals.removeFrom(story.part.getJaxbElement(), item -> {
				String name = Trees.name(item);
				return "customXmlInsRangeStart".equals(name) || "customXmlInsRangeEnd".equals(name);
			}, null);
		}
	}
}
