/*
   Licensed to Plutext Pty Ltd under one or more contributor license agreements.

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
 * <p>A paragraph the layout did not place (hidden text, say, or a paragraph the FO
 * preprocessing folded into its neighbour) has no page; the getters return null for it.</p>
 *
 * @since 17.1.1
 */
public final class PaginationMap {

	private final List<String> keys = new ArrayList<String>();
	private final Map<String, Integer> pageIndexOf = new LinkedHashMap<String, Integer>();
	private final Map<String, Integer> lastPageIndexOf = new LinkedHashMap<String, Integer>();
	private final Map<String, Integer> pageOf = new LinkedHashMap<String, Integer>();
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
	 * For a paragraph that spans pages, the character offsets in its text at which each
	 * later page begins, ascending; an empty array otherwise.
	 *
	 * <p>The offsets count the characters FOP put on the lines before the break, after its
	 * own hyphenation and with tabs and leaders expanded, so in a paragraph with tabs,
	 * fields or images they are approximate (phase 2 of CR-012 reconciles them against the
	 * paragraph's own text).  Their number is exact: it is the number of page boundaries
	 * inside the paragraph.</p>
	 */
	public int[] getBreaks(String key) {
		int[] breaks = breaksIn.get(key);
		return breaks == null ? new int[0] : breaks.clone();
	}

	/** The keys of the paragraphs that span pages, in document order. */
	public List<String> getKeysWithBreaks() {
		List<String> ret = new ArrayList<String>();
		for (String key : (keys.isEmpty() ? breaksIn.keySet() : keys)) {
			int[] breaks = breaksIn.get(key);
			if (breaks != null && breaks.length > 0) ret.add(key);
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

	/** A later sighting, on a later page: the paragraph continues there from this offset. */
	void continueOn(String key, int pageIndex, int offset) {
		int[] old = breaksIn.get(key);
		int[] breaks = new int[old == null ? 1 : old.length + 1];
		if (old != null) System.arraycopy(old, 0, breaks, 0, old.length);
		breaks[breaks.length - 1] = offset;
		breaksIn.put(key, breaks);
		lastPageIndexOf.put(key, pageIndex);
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
				int[] breaks = breaksIn.get(key);
				if (breaks != null && breaks.length > 0) {
					sb.append("-").append(lastPageIndexOf.get(key));
				}
			}
		}
		return sb.append("]").toString();
	}
}
