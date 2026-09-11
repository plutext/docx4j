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
package org.docx4j.model.pagination;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * Which page each paragraph of the main document part starts on, and where the
 * paragraphs that span pages break, as laid out by Apache FOP (CR-012).
 *
 * <p>Paragraphs are identified by a key: the paragraph's {@code w14:paraId} where it
 * has one, otherwise {@code P<n>} ({@code n} the paragraph's 1-based position in
 * document order, counting the paragraphs {@link Paginate} keys: the body's, including
 * those in tables, but not those in text boxes, headers, footers or notes).
 * {@link #getKeys()} lists them in document order.</p>
 *
 * <p>Pages are reported two ways.  The <em>page index</em> is the page's 1-based
 * position in the rendering, which is what a consumer showing page boundaries wants.
 * The <em>page number</em> is what the page displays (FOP's formatted number, so
 * {@code w:pgNumType} restarts and formats are honoured, as in a TOC); it is only
 * unique when the document never restarts its numbering, and it is the plain number
 * where the format is not decimal.</p>
 *
 * <p>A paragraph the layout did not place (hidden text, say, a paragraph the FO
 * preprocessing folded into its neighbour, or one joined to the paragraph before it
 * because that one's mark is deleted) has no page; the getters return null for it.</p>
 *
 * @since 17.1.1
 */
public final class PaginationMap {

	/** A page boundary inside a paragraph as the area tree reader saw it, before
	 *  {@link Paginate} resolves it to an offset in the paragraph's text. */
	static final class RawBreak {
		/** Which part of the paragraph (the preprocessing splits one at a page break inside it). */
		int part;
		/** Characters: into the part if a run anchor placed it, else all the paragraph's so far. */
		int offset;
		boolean absolute;
		/** How many of the run's line ends before the boundary ended in a hyphen: at each,
		 *  either the document's own (counted right) or one FOP added (counted one too
		 *  many), which the resolver settles against the text. */
		int hyphenEnds;
		/** The first characters FOP put on the new page's line, for the resolver to
		 *  settle the hyphen question by. */
		final StringBuilder lineStart = new StringBuilder();
	}

	private final List<String> keys = new ArrayList<String>();
	private final Map<String, Integer> pageIndexOf = new LinkedHashMap<String, Integer>();
	private final Map<String, Integer> lastPageIndexOf = new LinkedHashMap<String, Integer>();
	private final Map<String, Integer> pageOf = new LinkedHashMap<String, Integer>();
	private final Map<String, List<RawBreak>> rawBreaks = new LinkedHashMap<String, List<RawBreak>>();
	private final Map<String, int[]> breaksIn = new LinkedHashMap<String, int[]>();
	private int pageCount = 0;

	/** The paragraph keys, in document order (empty for a map not built by {@link Paginate}). */
	public List<String> getKeys() {
		return Collections.unmodifiableList(keys);
	}

	/** The 1-based index, in rendering order, of the page this paragraph starts on; null if
	 *  the layout did not place it. */
	public Integer getPageIndex(String key) {
		return pageIndexOf.get(key);
	}

	/** The 1-based index of the page this paragraph ends on (its start page unless it
	 *  spans pages); null if the layout did not place it. */
	public Integer getLastPageIndex(String key) {
		return lastPageIndexOf.get(key);
	}

	/** The number the page this paragraph starts on displays; null if the layout did not
	 *  place it. */
	public Integer getPage(String key) {
		return pageOf.get(key);
	}

	/** Whether the layout placed this paragraph. */
	public boolean contains(String key) {
		return pageIndexOf.containsKey(key);
	}

	/**
	 * For a paragraph that spans pages, the offsets in its text ({@link RunText}: the
	 * characters of its {@code w:t}s, one for a symbol or non-breaking hyphen, none for
	 * tabs, breaks and the rest) at which each later page begins, ascending; an empty
	 * array otherwise.  Their number is the number of page boundaries inside the
	 * paragraph.  Where a run spans the boundary the offset is inside it, at the first
	 * character of the new page's line (the space FOP dropped at the line end before it
	 * is stepped over by the writer); a hyphen FOP added at the line end is corrected for.
	 * In a run holding a field result the offset can be off by the difference between
	 * the cached result and what FOP printed.
	 *
	 * <p>For a map from {@link Paginate#parse} alone (no document to resolve against) the
	 * offsets are as the area tree reader counted them: relative to the paragraph part
	 * they are in, or, without a run anchor, the paragraph's characters so far.</p>
	 */
	public int[] getBreaks(String key) {
		int[] breaks = breaksIn.get(key);
		if (breaks != null) return breaks.clone();
		List<RawBreak> raw = rawBreaks.get(key);
		if (raw == null) return new int[0];
		breaks = new int[raw.size()];
		for (int i = 0; i < breaks.length; i++) breaks[i] = raw.get(i).offset;
		return breaks;
	}

	/** The keys of the paragraphs that span pages, in document order. */
	public List<String> getKeysWithBreaks() {
		List<String> ret = new ArrayList<String>();
		for (String key : (keys.isEmpty() ? rawBreaks.keySet() : keys)) {
			if (getBreaks(key).length > 0) ret.add(key);
		}
		return ret;
	}

	/** The number of pages in the rendering. */
	public int getPageCount() {
		return pageCount;
	}

	/** The pages, by index, as a read-only view: key to start page index. */
	public Map<String, Integer> getPageIndexMap() {
		return Collections.unmodifiableMap(pageIndexOf);
	}

	/** The displayed page numbers as a read-only view: key to start page number. */
	public Map<String, Integer> getPageMap() {
		return Collections.unmodifiableMap(pageOf);
	}

	// --- population: by the area tree handler and Paginate ---

	/** The first sighting of a paragraph. */
	void start(String key, int pageIndex, Integer pageNumber) {
		pageIndexOf.put(key, pageIndex);
		lastPageIndexOf.put(key, pageIndex);
		pageOf.put(key, pageNumber);
	}

	/** A later sighting, on a later page: the paragraph continues there. */
	void continueOn(String key, int pageIndex, RawBreak raw) {
		List<RawBreak> list = rawBreaks.get(key);
		if (list == null) {
			list = new ArrayList<RawBreak>();
			rawBreaks.put(key, list);
		}
		list.add(raw);
		lastPageIndexOf.put(key, pageIndex);
	}

	/** The boundaries as read, for {@link Paginate} to resolve. */
	List<RawBreak> raw(String key) {
		List<RawBreak> list = rawBreaks.get(key);
		return list == null ? Collections.<RawBreak>emptyList() : list;
	}

	/** The resolved boundaries: offsets in the paragraph's text. */
	void setBreaks(String key, int[] breaks) {
		breaksIn.put(key, breaks);
	}

	void setPageCount(int pageCount) {
		this.pageCount = pageCount;
	}

	void setKeys(List<String> keys) {
		this.keys.clear();
		this.keys.addAll(keys);
	}

	/** Drop the entries for paragraphs outside this set (text box paragraphs carry ids too,
	 *  but are not keyed). */
	void retainAll(Collection<String> wanted) {
		pageIndexOf.keySet().retainAll(wanted);
		lastPageIndexOf.keySet().retainAll(wanted);
		pageOf.keySet().retainAll(wanted);
		rawBreaks.keySet().retainAll(wanted);
		breaksIn.keySet().retainAll(wanted);
	}

	@Override
	public String toString() {
		StringBuilder sb = new StringBuilder("PaginationMap[pages=" + pageCount);
		for (String key : (keys.isEmpty() ? pageIndexOf.keySet() : keys)) {
			Integer index = pageIndexOf.get(key);
			sb.append(", ").append(key).append("=");
			if (index == null) {
				sb.append("?");
			} else {
				sb.append(index);
				int[] breaks = getBreaks(key);
				if (breaks.length > 0) {
					sb.append("-").append(lastPageIndexOf.get(key)).append("@");
					for (int i = 0; i < breaks.length; i++) sb.append(i == 0 ? "" : ",").append(breaks[i]);
				}
			}
		}
		return sb.append("]").toString();
	}
}
