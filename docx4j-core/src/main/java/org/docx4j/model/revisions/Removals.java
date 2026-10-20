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

import java.lang.reflect.Field;
import java.math.BigInteger;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.IdentityHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.function.Predicate;

import org.docx4j.openpackaging.packages.WordprocessingMLPackage;
import org.docx4j.openpackaging.parts.JaxbXmlPart;
import org.docx4j.openpackaging.parts.Part;
import org.docx4j.openpackaging.parts.PartName;
import org.docx4j.openpackaging.parts.WordprocessingML.CommentsExtendedPart;
import org.docx4j.openpackaging.parts.WordprocessingML.CommentsExtensiblePart;
import org.docx4j.openpackaging.parts.WordprocessingML.CommentsIdsPart;
import org.docx4j.openpackaging.parts.WordprocessingML.CommentsPart;
import org.docx4j.openpackaging.parts.WordprocessingML.EndnotesPart;
import org.docx4j.openpackaging.parts.WordprocessingML.FootnotesPart;
import org.docx4j.openpackaging.parts.WordprocessingML.MainDocumentPart;
import org.docx4j.openpackaging.parts.relationships.Namespaces;
import org.docx4j.openpackaging.parts.relationships.RelationshipsPart;
import org.docx4j.relationships.Relationship;
import org.docx4j.wml.CTFtnEdn;
import org.docx4j.wml.CTFtnEdnRef;
import org.docx4j.wml.CTMarkup;
import org.docx4j.wml.CommentRangeEnd;
import org.docx4j.wml.CommentRangeStart;
import org.docx4j.wml.Comments;
import org.docx4j.wml.P;
import org.docx4j.wml.R;
import org.docx4j.wml.STFtnEdn;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * What content removed by resolving takes with it (CR-043 section 3.2): the notes whose references
 * went (Word removes a note with its reference), the comments whose anchors went (with their
 * {@code commentsExtended}, {@code commentsIds} and {@code commentsExtensible} entries and their
 * replies), bookmarks with a start or an end in removed content (removed whole, C11), and the
 * relationships and parts nothing references any more (D7: images, charts, OLE objects, a dropped
 * section's headers and footers). Recorded as content goes, settled by {@link #flush()} once the
 * resolution is done, against what still stands in every story.
 */
final class Removals {

	private static Logger log = LoggerFactory.getLogger(Removals.class);

	private final WordprocessingMLPackage pkg;
	/**
	 * True for a conversion's working copy, which shares its content type manager, relationship
	 * objects and the parts it did not copy deep (the comments' extended parts among them) with the
	 * package it was copied from: nothing outside the story parts is touched, and a joined paragraph
	 * keeps the first paragraph's {@code w14:paraId} (a layout keyed by paragraph needs it: Paginate).
	 */
	final boolean conversion;

	private final Set<BigInteger> footnotes = new LinkedHashSet<BigInteger>();
	private final Set<BigInteger> endnotes = new LinkedHashSet<BigInteger>();
	/** Comments some of whose markup went: removed when nothing references them, else their orphan markers go. */
	private final Set<BigInteger> comments = new LinkedHashSet<BigInteger>();
	/** Comments to remove whole (their range held removed text, and is now empty). */
	private final Set<BigInteger> commentsWhole = new LinkedHashSet<BigInteger>();
	private final Map<Part, Set<BigInteger>> bookmarks = new IdentityHashMap<Part, Set<BigInteger>>();
	private final Map<Part, Set<String>> relationships = new IdentityHashMap<Part, Set<String>>();

	Removals(WordprocessingMLPackage pkg) {
		this(pkg, false);
	}

	Removals(WordprocessingMLPackage pkg, boolean conversion) {
		this.pkg = pkg;
		this.conversion = conversion;
	}

	/** Records what {@code subtree}, about to be removed from a story, references. */
	void discard(Story story, Object subtree) {
		discard(story.part, subtree);
	}

	private void discard(Part part, Object subtree) {
		Trees.walk(subtree, new Trees.Visitor() {
			@Override boolean enter(Object value, Object item) {
				String name = Trees.name(item);
				if (value instanceof CTFtnEdnRef) {
					if ("footnoteReference".equals(name)) footnotes.add(((CTFtnEdnRef) value).getId());
					else if ("endnoteReference".equals(name)) endnotes.add(((CTFtnEdnRef) value).getId());
				} else if (value instanceof R.CommentReference) {
					comments.add(((R.CommentReference) value).getId());
				} else if (value instanceof CommentRangeStart) {
					comments.add(((CommentRangeStart) value).getId());
				} else if (value instanceof CommentRangeEnd) {
					comments.add(((CommentRangeEnd) value).getId());
				} else if (("bookmarkStart".equals(name) || "bookmarkEnd".equals(name)) && value instanceof CTMarkup) {
					setOf(bookmarks, part).add(((CTMarkup) value).getId());
				}
				return true;
			}
			@Override void relationship(String id) {
				if (part != null && id != null) setOfStrings(relationships, part).add(id);
			}
		});
	}

	void removeComment(BigInteger id) {
		if (id != null) commentsWhole.add(id);
	}

	private static Set<BigInteger> setOf(Map<Part, Set<BigInteger>> map, Part part) {
		Set<BigInteger> set = map.get(part);
		if (set == null) map.put(part, set = new LinkedHashSet<BigInteger>());
		return set;
	}

	private static Set<String> setOfStrings(Map<Part, Set<String>> map, Part part) {
		Set<String> set = map.get(part);
		if (set == null) map.put(part, set = new LinkedHashSet<String>());
		return set;
	}

	private boolean pending() {
		return !footnotes.isEmpty() || !endnotes.isEmpty() || !comments.isEmpty() || !commentsWhole.isEmpty()
				|| !bookmarks.isEmpty() || !relationships.isEmpty();
	}

	/** Settles everything recorded; what that removes in turn (a note's own picture) is settled too. */
	void flush() {
		int rounds = 0;
		while (pending() && rounds++ < 20) {
			flushComments();
			flushNotes();
			flushBookmarks();
			flushRelationships();
		}
	}

	// --- the parts and their roots --------------------------------------------------------------------

	/** Every part whose content is a story's, with its root: main, headers, footers, notes, comments. */
	private List<JaxbXmlPart<?>> storyParts() {
		List<JaxbXmlPart<?>> out = new ArrayList<JaxbXmlPart<?>>();
		MainDocumentPart main = pkg.getMainDocumentPart();
		if (main == null) return out;
		out.add(main);
		for (Story story : new Lister(pkg, false).stories()) {
			if (!out.contains(story.part)) out.add(story.part);
		}
		return out;
	}

	// --- comments --------------------------------------------------------------------------------

	private void flushComments() {
		if (comments.isEmpty() && commentsWhole.isEmpty()) return;
		Set<BigInteger> candidates = new LinkedHashSet<BigInteger>(comments);
		Set<BigInteger> whole = new LinkedHashSet<BigInteger>(commentsWhole);
		comments.clear();
		commentsWhole.clear();
		List<JaxbXmlPart<?>> parts = storyParts();
		Set<BigInteger> references = new HashSet<BigInteger>();
		Set<BigInteger> starts = new HashSet<BigInteger>();
		Set<BigInteger> ends = new HashSet<BigInteger>();
		for (JaxbXmlPart<?> part : parts) {
			Trees.walk(part.getJaxbElement(), new Trees.Visitor() {
				@Override boolean enter(Object value, Object item) {
					if (value instanceof R.CommentReference) references.add(((R.CommentReference) value).getId());
					else if (value instanceof CommentRangeStart) starts.add(((CommentRangeStart) value).getId());
					else if (value instanceof CommentRangeEnd) ends.add(((CommentRangeEnd) value).getId());
					return true;
				}
			});
		}
		for (BigInteger id : candidates) {
			if (whole.contains(id)) continue;
			if (!references.contains(id)) {
				// its anchor is gone: Word keeps no comment without one (check 32)
				if (commentEntry(id) != null || starts.contains(id) || ends.contains(id)) whole.add(id);
			} else if (starts.contains(id) != ends.contains(id)) {
				// half a range: the half left goes, the comment stays on its reference
				for (JaxbXmlPart<?> part : parts) removeFrom(part.getJaxbElement(), item -> isCommentMarker(item, id), null);
			}
		}
		Set<BigInteger> done = new HashSet<BigInteger>();
		for (BigInteger id : whole) removeCommentWhole(id, parts, done);
		if (!done.isEmpty()) dropEmptyCommentParts();
	}

	/**
	 * With the last comment a resolution takes away, the comment parts go, as Word 365 removes them
	 * (docx4j-python's artefact 8 rejected, core-ts check 32): the comments part, its extended, ids and
	 * extensible parts, their relationships and content types; and the people part, where no tracked
	 * change is left to name an author (Word's removal was seen with none left).
	 */
	private void dropEmptyCommentParts() {
		if (conversion) return;
		MainDocumentPart main = pkg.getMainDocumentPart();
		CommentsPart commentsPart = main.getCommentsPart();
		if (commentsPart == null || commentsPart.getJaxbElement() == null || !commentsPart.getJaxbElement().getComment().isEmpty()) return;
		dropPart(main, commentsPart, Namespaces.COMMENTS);
		dropPart(main, main.getCommentsExtendedPart(), Namespaces.COMMENTS_EXTENDED);
		dropPart(main, main.getCommentsIdsPart(), Namespaces.COMMENTS_IDS);
		dropPart(main, main.getCommentsExtensiblePart(), Namespaces.COMMENTS_EXTENSIBLE);
		if (main.getPeoplePart() != null && new Lister(pkg, false).list().isEmpty()) {
			dropPart(main, main.getPeoplePart(), Namespaces.OFFICE_2011_PEOPLE);
		}
	}

	private void dropPart(MainDocumentPart main, Part part, String type) {
		if (part == null) return;
		RelationshipsPart rels = main.getRelationshipsPart(false);
		Relationship rel = rels == null ? null : rels.getRel(part.getPartName());
		if (rel != null) rels.removeRelationship(rel);
		main.setPartShortcut(null, type);
		removeIfUntargeted(part);
	}

	private static boolean isCommentMarker(Object item, BigInteger id) {
		Object v = Trees.unwrap(item);
		return (v instanceof CommentRangeStart && Objects.equals(((CommentRangeStart) v).getId(), id))
				|| (v instanceof CommentRangeEnd && Objects.equals(((CommentRangeEnd) v).getId(), id));
	}

	private static boolean isCommentReference(Object item, BigInteger id) {
		Object v = Trees.unwrap(item);
		return v instanceof R.CommentReference && Objects.equals(((R.CommentReference) v).getId(), id);
	}

	/** A run that holds the comment's reference and nothing else. */
	private static boolean isReferenceRun(Object item, BigInteger id) {
		Object v = Trees.unwrap(item);
		if (!(v instanceof R)) return false;
		boolean found = false;
		for (Object o : ((R) v).getContent()) {
			if (isCommentReference(o, id)) found = true;
			else return false;
		}
		return found;
	}

	private Comments.Comment commentEntry(BigInteger id) {
		CommentsPart part = pkg.getMainDocumentPart().getCommentsPart();
		if (part == null || part.getJaxbElement() == null) return null;
		for (Comments.Comment c : part.getJaxbElement().getComment()) if (Objects.equals(c.getId(), id)) return c;
		return null;
	}

	/**
	 * A comment taken away: its markers and reference run in every story, its entry, its entries in
	 * the extended parts, and its replies (CR-043 section 3.2, U3).
	 */
	private void removeCommentWhole(BigInteger id, List<JaxbXmlPart<?>> parts, Set<BigInteger> done) {
		if (!done.add(id)) return;
		for (JaxbXmlPart<?> part : parts) {
			removeFrom(part.getJaxbElement(), item -> isCommentMarker(item, id) || isReferenceRun(item, id) || isCommentReference(item, id), null);
		}
		MainDocumentPart main = pkg.getMainDocumentPart();
		CommentsPart commentsPart = main.getCommentsPart();
		Comments.Comment entry = commentEntry(id);
		if (entry == null) return;
		String paraId = Trees.lastParaId(entry.getContent());
		discard(commentsPart, entry.getContent());
		commentsPart.getJaxbElement().getComment().remove(entry);
		if (paraId == null || conversion) return;
		CommentsExtendedPart extended = main.getCommentsExtendedPart();
		if (extended != null && extended.getJaxbElement() != null) {
			List<org.docx4j.w15.CTCommentEx> list = extended.getJaxbElement().getCommentEx();
			List<String> replies = new ArrayList<String>();
			for (int i = list.size() - 1; i >= 0; i--) {
				org.docx4j.w15.CTCommentEx ex = list.get(i);
				if (paraId.equalsIgnoreCase(ex.getParaId())) list.remove(i);
				else if (paraId.equalsIgnoreCase(ex.getParaIdParent())) replies.add(ex.getParaId());
			}
			for (String reply : replies) {
				BigInteger replyId = commentIdByParaId(reply);
				if (replyId != null) removeCommentWhole(replyId, parts, done);
			}
		}
		CommentsIdsPart ids = main.getCommentsIdsPart();
		String durableId = null;
		if (ids != null && ids.getJaxbElement() != null) {
			List<org.docx4j.w16cid.CTCommentId> list = ids.getJaxbElement().getCommentId();
			for (int i = list.size() - 1; i >= 0; i--) {
				if (paraId.equalsIgnoreCase(list.get(i).getParaId())) {
					durableId = list.get(i).getDurableId();
					list.remove(i);
				}
			}
		}
		CommentsExtensiblePart extensible = main.getCommentsExtensiblePart();
		if (durableId != null && extensible != null && extensible.getJaxbElement() != null) {
			List<org.docx4j.w16cex.CTCommentExtensible> list = extensible.getJaxbElement().getCommentExtensible();
			for (int i = list.size() - 1; i >= 0; i--) {
				if (durableId.equalsIgnoreCase(list.get(i).getDurableId())) list.remove(i);
			}
		}
	}

	private BigInteger commentIdByParaId(String paraId) {
		CommentsPart part = pkg.getMainDocumentPart().getCommentsPart();
		if (part == null || part.getJaxbElement() == null) return null;
		for (Comments.Comment c : part.getJaxbElement().getComment()) {
			String last = Trees.lastParaId(c.getContent());
			if (paraId.equalsIgnoreCase(last)) return c.getId();
		}
		return null;
	}

	// --- notes ---------------------------------------------------------------------------------------

	/**
	 * A note whose reference went is removed from its part (Word: the editor's {@code review/}
	 * saves for footnotes, C12 and C12b for endnotes), unless a reference to it still stands
	 * somewhere (an accepted move keeps the reference its source had). Separators are never
	 * referenced and never go.
	 */
	private void flushNotes() {
		if (footnotes.isEmpty() && endnotes.isEmpty()) return;
		Set<BigInteger> fn = new LinkedHashSet<BigInteger>(footnotes);
		Set<BigInteger> en = new LinkedHashSet<BigInteger>(endnotes);
		footnotes.clear();
		endnotes.clear();
		Set<BigInteger> fnStanding = new HashSet<BigInteger>();
		Set<BigInteger> enStanding = new HashSet<BigInteger>();
		for (JaxbXmlPart<?> part : storyParts()) {
			Trees.walk(part.getJaxbElement(), new Trees.Visitor() {
				@Override boolean enter(Object value, Object item) {
					if (value instanceof CTFtnEdnRef) {
						String name = Trees.name(item);
						if ("footnoteReference".equals(name)) fnStanding.add(((CTFtnEdnRef) value).getId());
						else if ("endnoteReference".equals(name)) enStanding.add(((CTFtnEdnRef) value).getId());
					}
					return true;
				}
			});
		}
		MainDocumentPart main = pkg.getMainDocumentPart();
		FootnotesPart fp = main.getFootnotesPart();
		if (fp != null && fp.getJaxbElement() != null) prune(fp, fp.getJaxbElement().getFootnote(), fn, fnStanding);
		EndnotesPart ep = main.getEndNotesPart();
		if (ep != null && ep.getJaxbElement() != null) prune(ep, ep.getJaxbElement().getEndnote(), en, enStanding);
	}

	private void prune(Part part, List<CTFtnEdn> notes, Set<BigInteger> ids, Set<BigInteger> standing) {
		for (int i = notes.size() - 1; i >= 0; i--) {
			CTFtnEdn note = notes.get(i);
			if (note.getType() != null && note.getType() != STFtnEdn.NORMAL) continue;
			if (!ids.contains(note.getId()) || standing.contains(note.getId())) continue;
			discard(part, note.getContent());
			notes.remove(i);
		}
	}

	// --- bookmarks -----------------------------------------------------------------------------------

	/** A bookmark with its start or its end in removed content goes whole (C11, C11b). */
	private void flushBookmarks() {
		if (bookmarks.isEmpty()) return;
		Map<Part, Set<BigInteger>> todo = new IdentityHashMap<Part, Set<BigInteger>>(bookmarks);
		bookmarks.clear();
		for (Map.Entry<Part, Set<BigInteger>> e : todo.entrySet()) {
			if (!(e.getKey() instanceof JaxbXmlPart)) continue;
			Set<BigInteger> ids = e.getValue();
			removeFrom(((JaxbXmlPart<?>) e.getKey()).getJaxbElement(), item -> {
				String name = Trees.name(item);
				Object v = Trees.unwrap(item);
				return ("bookmarkStart".equals(name) || "bookmarkEnd".equals(name)) && v instanceof CTMarkup
						&& ids.contains(((CTMarkup) v).getId());
			}, null);
		}
	}

	// --- relationships and parts ---------------------------------------------------------------------

	/**
	 * A relationship no content of its part references any more is removed, and the part it
	 * targets with it when no relationship in the package targets that part (D7; C04, C05, C06):
	 * what the removed part relates to in turn goes when nothing else targets it.
	 */
	private void flushRelationships() {
		if (relationships.isEmpty()) return;
		if (conversion) {
			relationships.clear();
			return;
		}
		Map<Part, Set<String>> todo = new IdentityHashMap<Part, Set<String>>(relationships);
		relationships.clear();
		for (Map.Entry<Part, Set<String>> e : todo.entrySet()) {
			Part part = e.getKey();
			if (!(part instanceof JaxbXmlPart) || pkg.getParts().get(part.getPartName()) != part) continue;
			RelationshipsPart rels = part.getRelationshipsPart(false);
			if (rels == null) continue;
			Set<String> used = new HashSet<String>();
			Trees.walk(((JaxbXmlPart<?>) part).getJaxbElement(), new Trees.Visitor() {
				@Override void relationship(String id) {
					used.add(id);
				}
			});
			for (String id : e.getValue()) {
				if (used.contains(id)) continue;
				Relationship rel = rels.getRelationshipByID(id);
				if (rel == null) continue;
				Part target = "External".equals(rel.getTargetMode()) ? null : rels.getPart(rel);
				rels.removeRelationship(rel);
				if (target != null) removeIfUntargeted(target);
			}
		}
	}

	private void removeIfUntargeted(Part target) {
		if (isTargeted(target.getPartName())) return;
		RelationshipsPart own = target.getRelationshipsPart(false);
		List<Part> children = new ArrayList<Part>();
		if (own != null) {
			for (Relationship rel : new ArrayList<Relationship>(own.getRelationships().getRelationship())) {
				if ("External".equals(rel.getTargetMode())) continue;
				Part child = own.getPart(rel);
				if (child != null) children.add(child);
			}
		}
		if (log.isDebugEnabled()) log.debug("Removing " + target.getPartName().getName() + ": nothing references it now");
		pkg.getParts().remove(target.getPartName());
		if (own != null) pkg.getParts().remove(own.getPartName());
		pkg.getContentTypeManager().removeOverrideContentType(target.getPartName());
		for (Part child : children) removeIfUntargeted(child);
	}

	private boolean isTargeted(PartName name) {
		if (pkg.getRelationshipsPart() != null && pkg.getRelationshipsPart().getRel(name) != null) return true;
		for (Part part : new ArrayList<Part>(pkg.getParts().getParts().values())) {
			if (part instanceof RelationshipsPart) continue;
			RelationshipsPart rels = part.getRelationshipsPart(false);
			if (rels != null && rels.getRel(name) != null) return true;
		}
		return false;
	}

	// --- removing from every list ------------------------------------------------------------------------

	/**
	 * Removes the items {@code match} matches from every list under {@code root} (block, run and
	 * run-content lists, math's), recording what goes; returns how many went.
	 */
	int removeFrom(Object root, Predicate<Object> match, Part part) {
		int[] count = { 0 };
		removeFrom(root, match, part, count, 0);
		return count[0];
	}

	private void removeFrom(Object value, Predicate<Object> match, Part part, int[] count, int depth) {
		if (value == null || depth > 400) return;
		value = Trees.unwrap(value);
		if (value instanceof List) {
			@SuppressWarnings("unchecked")
			List<Object> list = (List<Object>) value;
			for (int i = list.size() - 1; i >= 0; i--) {
				if (match.test(list.get(i))) {
					if (part != null) discard(part, list.get(i));
					list.remove(i);
					count[0]++;
				}
			}
			for (Object item : new ArrayList<Object>(list)) removeFrom(item, match, part, count, depth + 1);
			return;
		}
		if (value == null || value instanceof String || value instanceof org.w3c.dom.Node) return;
		Package p = value.getClass().getPackage();
		if (p == null || !p.getName().startsWith("org.docx4j.")) return;
		for (Field f : Trees.fieldsOf(value.getClass())) {
			Object fv;
			try {
				fv = f.get(value);
			} catch (IllegalAccessException e) {
				continue;
			}
			if (fv != null && !(fv instanceof String)) removeFrom(fv, match, part, count, depth + 1);
		}
	}
}
