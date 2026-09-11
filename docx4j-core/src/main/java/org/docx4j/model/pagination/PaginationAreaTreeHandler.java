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
 * block reappears on a later page, where in the paragraph's text the new page begins.
 *
 * <p>The paragraphs are recognised by the {@code prod-id} trait on their block areas, which
 * FOP writes for an fo:block (or fo:list-block, or fo:block-container) that had an
 * {@code id}; the FO exporter puts {@link #FO_ID_PREFIX} + the paragraph key there when
 * {@code ConversionFeatures.PP_FO_PARAGRAPH_IDS} is on, and, where the preprocessing has
 * split a paragraph (at a page break inside it) so that its key would recur,
 * {@link #FO_ID_CONTINUATION} + a counter on the later parts, which this handler reads as
 * the same paragraph.</p>
 *
 * <p>The runs are recognised the same way: their inline areas carry
 * {@code r-<paragraph id>-<offset>}, the offset being where in the paragraph's text
 * ({@link RunText}) the run starts.  A page boundary inside a paragraph is placed at the
 * first run anchor met on the new page: its offset, plus the characters FOP had already
 * put on lines for that run (a run that spans the boundary reappears with the same
 * anchor, once per line), plus one for each of those lines, since FOP drops the space
 * (or the tab, or the wrapping break, which the text model counts as one) at every line
 * end, except where the line ended in a hyphen.  A hyphen at a line end is either the
 * document's, counted right, or one FOP added, counted one too many: how many such
 * line ends there were, and the first characters of the new page's line, are recorded
 * for {@link Paginate} to settle against the text.  So FOP's own additions to the text
 * (list labels, note numbers) never shift the answer by more than they occupy of the one
 * run they are in.  Where the paragraph continues without a run anchor, the count of all
 * its characters so far is the fallback.</p>
 *
 * <p>Headers, footers and side regions ({@code regionBefore} and the like), footnotes and
 * before-floats are skipped.  Positioned blocks are not: the export wraps a table in an
 * absolutely positioned block-container as well as a text box, so the exporter, which
 * knows which is which, writes no id for a text box's paragraphs instead.</p>
 *
 * <p>Written against FOP 2.11's renderer (CR-012 §1.3): {@code pageViewport} with
 * {@code nr}, {@code formatted-nr} and {@code key}; {@code block} and
 * {@code inlineparent} with {@code prod-id}; {@code lineArea} holding {@code text} with
 * {@code word} and {@code space}.  A page viewport without a number is treated as a
 * foreign format and fails the parse, rather than quietly producing an empty map.</p>
 *
 * @since 17.1.1
 */
public class PaginationAreaTreeHandler extends DefaultHandler {

	private static Logger log = LoggerFactory.getLogger(PaginationAreaTreeHandler.class);

	/** Prefix of the fo:block id (and so of the area's prod-id) of a keyed paragraph. */
	public static final String FO_ID_PREFIX = "p-";
	/** Prefix of the fo:inline id of a run: {@code r-<paragraph id without p->-<offset>}. */
	public static final String FO_RUN_ID_PREFIX = "r-";
	/** Separates the key from the counter on the id of a paragraph's later parts. */
	public static final String FO_ID_CONTINUATION = "~";

	private static final String PAGE_VIEWPORT = "pageViewport";
	private static final String BLOCK = "block";
	private static final String INLINE_PARENT = "inlineparent";
	private static final String WORD = "word";
	private static final String SPACE = "space";

	private final PaginationMap map = new PaginationMap();

	/** Characters seen on the lines of each paragraph so far, all parts. */
	private final Map<String, Integer> chars = new HashMap<String, Integer>();
	/** Characters seen under each run anchor (its prod-id) so far. */
	private final Map<String, Integer> charsByAnchor = new HashMap<String, Integer>();
	/** Lines each run anchor has appeared on so far. */
	private final Map<String, Integer> linesByAnchor = new HashMap<String, Integer>();
	/** Of those lines, how many ended in a hyphen. */
	private final Map<String, Integer> hyphenEndsByAnchor = new HashMap<String, Integer>();
	/** The last word seen under each run anchor. */
	private final Map<String, String> lastWordByAnchor = new HashMap<String, String>();
	/** Boundaries whose new page's first line is being captured, by paragraph key. */
	private final Map<String, PaginationMap.RawBreak> capturing = new HashMap<String, PaginationMap.RawBreak>();
	private static final int LINE_START_CHARS = 48;
	/** Paragraphs continuing on this page whose first run anchor is still to come. */
	private final Map<String, PaginationMap.RawBreak> pending = new HashMap<String, PaginationMap.RawBreak>();

	private int depth = 0;
	/** The depth of the element whose subtree is being skipped, or -1. */
	private int skipDepth = -1;
	/** The open keyed blocks: their key and the depth at which they close. */
	private final Deque<Object[]> open = new ArrayDeque<Object[]>();
	/** The open run anchors: their prod-id and the depth at which they close. */
	private final Deque<Object[]> openAnchors = new ArrayDeque<Object[]>();
	/** Inside a word or space of a line, whose characters count. */
	private boolean inText = false;
	private boolean inWord = false;
	private boolean spaceHadChars = false;
	private final StringBuilder word = new StringBuilder();

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
				sighting(key, partOf(prodId));
			}
			return;
		}
		if (INLINE_PARENT.equals(qName)) {
			String prodId = attributes.getValue("prod-id");
			if (prodId != null && prodId.startsWith(FO_RUN_ID_PREFIX) && !open.isEmpty()) {
				openAnchors.push(new Object[] { prodId, Integer.valueOf(depth) });
				anchorSighting(prodId);
			}
			return;
		}
		if ((WORD.equals(qName) || SPACE.equals(qName)) && !open.isEmpty()) {
			inText = true;
			inWord = WORD.equals(qName);
			spaceHadChars = false;
			word.setLength(0);
		}
	}

	@Override
	public void endElement(String uri, String localName, String qName) throws SAXException {

		if (skipDepth == depth) {
			skipDepth = -1;
		} else if (skipDepth < 0) {
			if (!open.isEmpty() && ((Integer) open.peek()[1]).intValue() == depth) {
				String key = (String) open.pop()[0];
				pending.remove(key); // no run anchor followed: the fallback stands
			}
			if (!openAnchors.isEmpty() && ((Integer) openAnchors.peek()[1]).intValue() == depth) {
				openAnchors.pop();
			}
			if (WORD.equals(qName) || SPACE.equals(qName)) {
				if (inWord && !openAnchors.isEmpty()) {
					lastWordByAnchor.put((String) openAnchors.peek()[0], word.toString());
				}
				if (!inWord && !spaceHadChars && inText) {
					// an empty space area: a tab, one character in the text model
					count(1, " ");
				}
				inText = false;
				inWord = false;
			}
			if ("lineArea".equals(qName) && !open.isEmpty()) {
				capturing.remove((String) open.peek()[0]); // the new page's first line is read
			}
		}
		depth--;
	}

	@Override
	public void characters(char[] ch, int start, int length) throws SAXException {

		if (!inText || skipDepth >= 0 || open.isEmpty()) return;
		if (!inWord) spaceHadChars = true;
		if (inWord) word.append(ch, start, length);
		count(length, new String(ch, start, length));
	}

	/** Characters on a line: to the paragraph, to the innermost run anchor, and to a
	 *  boundary's line start being captured. */
	private void count(int length, String text) {

		String key = (String) open.peek()[0];
		Integer n = chars.get(key);
		chars.put(key, Integer.valueOf((n == null ? 0 : n.intValue()) + length));
		if (!openAnchors.isEmpty()) {
			String anchor = (String) openAnchors.peek()[0];
			Integer a = charsByAnchor.get(anchor);
			charsByAnchor.put(anchor, Integer.valueOf((a == null ? 0 : a.intValue()) + length));
		}
		PaginationMap.RawBreak raw = capturing.get(key);
		if (raw != null && raw.lineStart.length() < LINE_START_CHARS) raw.lineStart.append(text);
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
		openAnchors.clear();
		pending.clear();
		capturing.clear();
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

	/** The paragraph key in a block's prod-id: strip the prefix, and any continuation counter. */
	static String keyOf(String prodId) {
		return stripContinuation(prodId.substring(FO_ID_PREFIX.length()));
	}

	/** The continuation counter in a block's prod-id (0 for the first part). */
	static int partOf(String prodId) {
		return continuationOf(prodId.substring(FO_ID_PREFIX.length()));
	}

	private static String stripContinuation(String key) {
		int tilde = key.lastIndexOf(FO_ID_CONTINUATION);
		return (tilde > 0 && isCounter(key, tilde)) ? key.substring(0, tilde) : key;
	}

	private static int continuationOf(String key) {
		int tilde = key.lastIndexOf(FO_ID_CONTINUATION);
		return (tilde > 0 && isCounter(key, tilde)) ? Integer.parseInt(key.substring(tilde + 1)) : 0;
	}

	private static boolean isCounter(String key, int tilde) {
		if (tilde >= key.length() - 1) return false;
		for (int i = tilde + 1; i < key.length(); i++) {
			if (!Character.isDigit(key.charAt(i))) return false;
		}
		return true;
	}

	private void sighting(String key, int part) {

		if (pageIndex == 0) {
			log.warn("Block " + key + " outside any page; ignored");
			return;
		}
		Integer last = map.getLastPageIndex(key);
		if (last == null) {
			map.start(key, pageIndex, pageNumber);
		} else if (last.intValue() != pageIndex) {
			Integer n = chars.get(key);
			PaginationMap.RawBreak raw = new PaginationMap.RawBreak();
			raw.part = part;
			raw.offset = (n == null ? 0 : n.intValue());
			raw.absolute = true; // until a run anchor says better
			map.continueOn(key, pageIndex, raw);
			pending.put(key, raw);
		}
		// the same page again (a paragraph split across columns, or a block reopened
		// after a nested one): nothing to record
	}

	/** A run's inline on a line: one more line for it; and the paragraph continuing
	 *  here has its boundary placed. */
	private void anchorSighting(String prodId) {

		Integer lines = linesByAnchor.get(prodId);
		int linesBefore = (lines == null ? 0 : lines.intValue());
		Integer h = hyphenEndsByAnchor.get(prodId);
		int hyphenEnds = (h == null ? 0 : h.intValue());
		if (linesBefore > 0) {
			// the run's previous line ended: in a hyphen, or with a space dropped
			String lastWord = lastWordByAnchor.get(prodId);
			if (lastWord != null && lastWord.length() > 0 && isHyphen(lastWord.charAt(lastWord.length() - 1))) {
				hyphenEnds++;
				hyphenEndsByAnchor.put(prodId, Integer.valueOf(hyphenEnds));
			}
		}
		linesByAnchor.put(prodId, Integer.valueOf(linesBefore + 1));

		String key = (String) open.peek()[0];
		PaginationMap.RawBreak raw = pending.remove(key);
		if (raw == null) return;

		// r-<key>[~part]-<offset>[.<n>]
		String body = prodId.substring(FO_RUN_ID_PREFIX.length());
		int dash = body.lastIndexOf('-');
		if (dash <= 0) return;
		String number = body.substring(dash + 1);
		int dot = number.indexOf('.');
		if (dot >= 0) number = number.substring(0, dot); // a later run at the same offset
		int offset;
		try {
			offset = Integer.parseInt(number);
		} catch (NumberFormatException e) {
			return;
		}
		String paragraphId = body.substring(0, dash);
		Integer seen = charsByAnchor.get(prodId); // before this page's characters are added

		raw.part = continuationOf(paragraphId);
		raw.offset = offset + (seen == null ? 0 : seen.intValue()) + linesBefore - hyphenEnds;
		raw.absolute = false;
		raw.hyphenEnds = hyphenEnds;
		capturing.put(key, raw);
	}

	private static boolean isHyphen(char c) {
		return c == '-' || c == '‐' || c == '­';
	}
}
