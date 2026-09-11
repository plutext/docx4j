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

import java.util.ArrayDeque;
import java.util.Deque;
import java.util.HashMap;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.xml.sax.Attributes;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.DefaultHandler;

/**
 * Reads Apache FOP's area tree XML ({@code org.apache.fop.render.xml.XMLRenderer}) into a
 * {@link PaginationMap}: the page each paragraph's block first appears on, and, where a
 * block reappears on a later page, the number of characters its lines held before that.
 *
 * <p>The paragraphs are recognised by the {@code prod-id} trait on their block areas, which
 * FOP writes for an fo:block (or fo:list-block, or fo:block-container) that had an
 * {@code id}; the FO exporter puts {@link #FO_ID_PREFIX} + the paragraph key there when
 * {@code ConversionFeatures.PP_FO_PARAGRAPH_IDS} is on, and, where the preprocessing has
 * split a paragraph (at a page break inside it) so that its key would recur,
 * {@link #FO_ID_CONTINUATION} + a counter on the later parts, which this handler reads as
 * the same paragraph.</p>
 *
 * <p>Headers, footers and side regions ({@code regionBefore} and the like), footnotes and
 * before-floats are skipped.  Positioned blocks are not: the export wraps a table in an
 * absolutely positioned block-container as well as a text box, so the exporter, which
 * knows which is which, writes no id for a text box's paragraphs instead.</p>
 *
 * <p>Written against FOP 2.11's renderer (CR-012 §1.3): {@code pageViewport} with
 * {@code nr}, {@code formatted-nr} and {@code key}; {@code block} with {@code prod-id};
 * {@code lineArea} holding {@code text} with {@code word} and {@code space}.  A page
 * viewport without a number is treated as a foreign format and fails the parse, rather
 * than quietly producing an empty map.</p>
 *
 * @since 17.1.1
 */
public class PaginationAreaTreeHandler extends DefaultHandler {

	private static Logger log = LoggerFactory.getLogger(PaginationAreaTreeHandler.class);

	/** Prefix of the fo:block id (and so of the area's prod-id) of a keyed paragraph. */
	public static final String FO_ID_PREFIX = "p-";
	/** Separates the key from the counter on the id of a paragraph's later parts. */
	public static final String FO_ID_CONTINUATION = "~";

	private static final String PAGE_VIEWPORT = "pageViewport";
	private static final String BLOCK = "block";
	private static final String WORD = "word";
	private static final String SPACE = "space";

	private final PaginationMap map = new PaginationMap();

	/** Characters seen on the lines of each paragraph so far. */
	private final Map<String, Integer> chars = new HashMap<String, Integer>();

	private int depth = 0;
	/** The depth of the element whose subtree is being skipped, or -1. */
	private int skipDepth = -1;
	/** The open keyed blocks: their key and the depth at which they close. */
	private final Deque<Object[]> open = new ArrayDeque<Object[]>();
	/** Inside a word or space of a line, whose characters count. */
	private boolean inText = false;

	private int pageIndex = 0;
	private Integer pageNumber = null;

	public PaginationMap getMap() {
		return map;
	}

	@Override
	public void startElement(String uri, String localName, String qName, Attributes attributes)
			throws SAXException {

		depth++;
		if (skipDepth >= 0) return;

		if (PAGE_VIEWPORT.equals(qName)) {
			startPage(attributes);
			return;
		}
		if (isSkipped(qName)) {
			skipDepth = depth;
			return;
		}
		if (BLOCK.equals(qName)) {
			String prodId = attributes.getValue("prod-id");
			if (prodId != null && prodId.startsWith(FO_ID_PREFIX)) {
				String key = keyOf(prodId);
				open.push(new Object[] { key, Integer.valueOf(depth) });
				sighting(key);
			}
			return;
		}
		if ((WORD.equals(qName) || SPACE.equals(qName)) && !open.isEmpty()) {
			inText = true;
		}
	}

	@Override
	public void endElement(String uri, String localName, String qName) throws SAXException {

		if (skipDepth == depth) {
			skipDepth = -1;
		} else if (skipDepth < 0) {
			if (!open.isEmpty() && ((Integer) open.peek()[1]).intValue() == depth) {
				open.pop();
			}
			if (WORD.equals(qName) || SPACE.equals(qName)) {
				inText = false;
			}
		}
		depth--;
	}

	@Override
	public void characters(char[] ch, int start, int length) throws SAXException {

		if (!inText || skipDepth >= 0 || open.isEmpty()) return;
		String key = (String) open.peek()[0];
		Integer n = chars.get(key);
		chars.put(key, Integer.valueOf((n == null ? 0 : n.intValue()) + length));
	}

	private void startPage(Attributes attributes) throws SAXException {

		String nr = attributes.getValue("nr");
		if (nr == null) {
			throw new SAXException("pageViewport without nr (key " + attributes.getValue("key")
					+ "): not the FOP 2.11 area tree format this reader expects");
		}
		pageIndex++;
		map.setPageCount(pageIndex);

		Integer page = null;
		String formatted = attributes.getValue("formatted-nr");
		try {
			if (formatted != null) page = Integer.valueOf(formatted.trim());
		} catch (NumberFormatException e) {
			// roman or lettered page numbers: fall back to the plain number
		}
		if (page == null) {
			try {
				page = Integer.valueOf(nr.trim());
			} catch (NumberFormatException e) {
				throw new SAXException("pageViewport nr '" + nr + "' is not a number");
			}
		}
		pageNumber = page;
		// pages are laid out one at a time, so a page boundary closes every block; any
		// still open is a block that spans it, and its next sighting is the continuation
		open.clear();
		inText = false;
	}

	private static boolean isSkipped(String qName) {

		return "regionBefore".equals(qName) || "regionAfter".equals(qName)
				|| "regionStart".equals(qName) || "regionEnd".equals(qName)
				|| "footnote".equals(qName) || "beforeFloat".equals(qName);
	}

	/**
	 * The fo:block id the exporter writes for a paragraph: {@code p-<key>} for its first
	 * (normally only) block, {@code p-<key>~<n>} for the n-th later block of the same
	 * paragraph, which keeps the ids unique in the FO document, as FOP requires.
	 */
	public static String foId(String key, int occurrence) {
		return occurrence <= 0 ? FO_ID_PREFIX + key : FO_ID_PREFIX + key + FO_ID_CONTINUATION + occurrence;
	}

	/** The paragraph key in a prod-id: strip the prefix, and any continuation counter. */
	static String keyOf(String prodId) {
		String key = prodId.substring(FO_ID_PREFIX.length());
		int tilde = key.lastIndexOf(FO_ID_CONTINUATION);
		if (tilde > 0) {
			boolean counter = tilde < key.length() - 1;
			for (int i = tilde + 1; counter && i < key.length(); i++) {
				counter = Character.isDigit(key.charAt(i));
			}
			if (counter) key = key.substring(0, tilde);
		}
		return key;
	}

	private void sighting(String key) {

		if (pageIndex == 0) {
			log.warn("Block " + key + " outside any page; ignored");
			return;
		}
		Integer last = map.getLastPageIndex(key);
		if (last == null) {
			map.start(key, pageIndex, pageNumber);
		} else if (last.intValue() != pageIndex) {
			Integer n = chars.get(key);
			map.continueOn(key, pageIndex, n == null ? 0 : n.intValue());
		}
		// the same page again (a paragraph split across columns, or a block reopened
		// after a nested one): nothing to record
	}
}
