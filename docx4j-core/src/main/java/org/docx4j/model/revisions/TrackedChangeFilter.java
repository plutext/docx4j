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

import java.math.BigInteger;
import java.time.Instant;
import java.util.Arrays;
import java.util.Collections;
import java.util.EnumSet;
import java.util.HashSet;
import java.util.IdentityHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;

import org.docx4j.openpackaging.packages.WordprocessingMLPackage;
import org.docx4j.openpackaging.parts.Part;
import org.docx4j.wml.CTBookmark;
import org.docx4j.wml.CTMarkup;
import org.docx4j.wml.P;
import org.docx4j.wml.PPr;
import org.docx4j.wml.ParaRPr;
import org.docx4j.wml.SdtElement;

/**
 * Which tracked changes {@link TrackedChanges#acceptAll(WordprocessingMLPackage, TrackedChangeFilter)}
 * and {@code rejectAll} resolve (CR-043 section 3.1): by author, type, kind, date, part, and range
 * (a content control's content, or a bookmark's extent). Every condition set must hold; a condition
 * not set holds for all. The conditions apply to each single change: of a group (touching changes of
 * one author and type) only the pieces that match are resolved.
 *
 * <pre>
 * TrackedChanges.acceptAll(pkg, new TrackedChangeFilter().authors("Author A").types(TrackedChange.Type.ADDED));
 * </pre>
 *
 * @since 17.3.3
 */
public final class TrackedChangeFilter {

	private Set<String> authors;
	private Set<TrackedChange.Type> types;
	private Set<TrackedChange.Kind> kinds;
	private Instant from, to;
	private Set<Part> parts;
	private SdtElement control;
	private String bookmark;

	/** Changes by these authors ({@code w:author}). */
	public TrackedChangeFilter authors(String... authors) {
		this.authors = new HashSet<String>(Arrays.asList(authors));
		return this;
	}

	/** Changes of these types. */
	public TrackedChangeFilter types(TrackedChange.Type... types) {
		this.types = types.length == 0 ? EnumSet.noneOf(TrackedChange.Type.class) : EnumSet.copyOf(Arrays.asList(types));
		return this;
	}

	/** Changes of these kinds (a single change's kind: {@code RUN_INSERTION}, {@code PARAGRAPH_MARK_DELETION}, ...). */
	public TrackedChangeFilter kinds(TrackedChange.Kind... kinds) {
		this.kinds = kinds.length == 0 ? EnumSet.noneOf(TrackedChange.Kind.class) : EnumSet.copyOf(Arrays.asList(kinds));
		return this;
	}

	/**
	 * Changes made at or after {@code from} and before {@code to} ({@link TrackedChange#getDate()});
	 * either may be null for no bound. A change with no date matches no date range.
	 */
	public TrackedChangeFilter between(Instant from, Instant to) {
		this.from = from;
		this.to = to;
		return this;
	}

	/** Changes in these parts (the main document part, a header, the footnotes, ...). */
	public TrackedChangeFilter parts(Part... parts) {
		Set<Part> set = Collections.newSetFromMap(new IdentityHashMap<Part, Boolean>());
		set.addAll(Arrays.asList(parts));
		this.parts = set;
		return this;
	}

	/** Changes inside this content control's content (the control as it stands in the package). */
	public TrackedChangeFilter within(SdtElement control) {
		this.control = control;
		this.bookmark = null;
		return this;
	}

	/** Changes between the start and the end of the bookmark of this name, in whichever story it is in. */
	public TrackedChangeFilter withinBookmark(String name) {
		this.bookmark = name;
		this.control = null;
		return this;
	}

	// --- matching ----------------------------------------------------------------------------------

	/** The range, read once from the package before anything is resolved: the markup objects inside it. */
	Set<Object> range(WordprocessingMLPackage pkg) {
		if (control == null && bookmark == null) return null;
		Set<Object> inside = Collections.newSetFromMap(new IdentityHashMap<Object, Boolean>());
		if (control != null) {
			Trees.walk(control.getSdtContent(), new Trees.Visitor() {
				@Override boolean enter(Object value, Object item) {
					inside.add(value);
					return true;
				}
			});
			return inside;
		}
		for (Story story : new Lister(pkg, false).stories()) {
			BookmarkWalk walk = new BookmarkWalk(bookmark, inside);
			walk.list(story.root);
			if (walk.found) return inside;
		}
		throw new IllegalArgumentException("No bookmark named " + bookmark);
	}

	/** True when the single change {@code piece} meets every condition set. */
	boolean matches(TrackedChange piece, Set<Object> range) {
		if (authors != null && !authors.contains(piece.getAuthor())) return false;
		if (types != null && !types.contains(piece.getType())) return false;
		if (kinds != null && !kinds.contains(piece.getKind())) return false;
		if (from != null || to != null) {
			Instant date = piece.getDate();
			if (date == null) return false;
			if (from != null && date.isBefore(from)) return false;
			if (to != null && !date.isBefore(to)) return false;
		}
		if (parts != null && !parts.contains(piece.getPart())) return false;
		if (range != null && !range.contains(piece.anchor())) return false;
		return true;
	}

	/**
	 * A story walked in document order, a paragraph's mark after its content, noting what stands
	 * between a bookmark's start and its end. The bookmark's ids are matched by its start's.
	 */
	private static final class BookmarkWalk {
		final String name;
		final Set<Object> inside;
		BigInteger id;
		boolean open, found;

		BookmarkWalk(String name, Set<Object> inside) {
			this.name = name;
			this.inside = inside;
		}

		void list(List<Object> items) {
			for (Object item : items) node(item);
		}

		void node(Object item) {
			Object top = Trees.unwrap(item);
			if (top instanceof P) {
				paragraph((P) top);
				return;
			}
			Trees.walk(item, new Trees.Visitor() {
				@Override boolean enter(Object value, Object it) {
					String n = Trees.name(it);
					if ("bookmarkStart".equals(n) && value instanceof CTBookmark && !found && Objects.equals(((CTBookmark) value).getName(), name)) {
						id = ((CTBookmark) value).getId();
						open = true;
						found = true;
						return false;
					}
					if ("bookmarkEnd".equals(n) && value instanceof CTMarkup && open && Objects.equals(((CTMarkup) value).getId(), id)) {
						open = false;
						return false;
					}
					if (value instanceof P) {
						paragraph((P) value);
						return false;
					}
					if (open) inside.add(value);
					return true;
				}
			});
		}

		/** A paragraph: its properties' record first, then its content, then its mark. */
		void paragraph(P p) {
			PPr pPr = p.getPPr();
			if (open && pPr != null && pPr.getPPrChange() != null) inside.add(pPr.getPPrChange());
			list(p.getContent());
			mark(p);
		}

		void mark(P p) {
			if (!open) return;
			inside.add(p);
			PPr pPr = p.getPPr();
			if (pPr == null) return;
			ParaRPr rPr = pPr.getRPr();
			if (rPr != null) {
				inside.add(rPr);
				if (rPr.getIns() != null) inside.add(rPr.getIns());
				if (rPr.getDel() != null) inside.add(rPr.getDel());
				if (rPr.getRPrChange() != null) inside.add(rPr.getRPrChange());
			}
			if (pPr.getSectPr() != null) inside.add(pPr.getSectPr());
		}
	}
}
