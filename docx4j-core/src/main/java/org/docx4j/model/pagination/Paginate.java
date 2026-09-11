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

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.IdentityHashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;
import java.util.concurrent.ThreadLocalRandom;
import java.util.regex.Pattern;

import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;

import org.docx4j.Docx4J;
import org.docx4j.TraversalUtil;
import org.docx4j.XmlUtils;
import org.docx4j.convert.out.ConversionFeatures;
import org.docx4j.convert.out.FOSettings;
import org.docx4j.convert.out.FopReflective;
import org.docx4j.convert.out.common.Preprocess;
import org.docx4j.convert.out.common.preprocess.PageBreak;
import org.docx4j.model.CompatibilityOptions;
import org.docx4j.openpackaging.exceptions.Docx4JException;
import org.docx4j.openpackaging.packages.WordprocessingMLPackage;
import org.docx4j.openpackaging.parts.WordprocessingML.MainDocumentPart;
import org.docx4j.wml.Br;
import org.docx4j.wml.ContentAccessor;
import org.docx4j.wml.Document;
import org.docx4j.wml.ObjectFactory;
import org.docx4j.wml.P;
import org.docx4j.wml.R;
import org.docx4j.wml.RunDel;
import org.docx4j.wml.RunIns;
import org.docx4j.wml.RunTrackChange;
import org.docx4j.wml.STBrType;
import org.docx4j.wml.SdtElement;
import org.docx4j.wml.Text;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Lays a document out with Apache FOP (via docx4j-export-fo) to find which page each
 * paragraph starts on and where the paragraphs that span pages break, and writes the
 * result back as {@code w:lastRenderedPageBreak} markers, the way Word records the
 * pagination of its last rendering (ECMA-376 17.3.3.13).  See CR-012.
 *
 * <p>{@link #compute} is the query: a {@link PaginationMap} from paragraph key to pages
 * and break offsets.  {@link #applyLastRenderedPageBreaks} writes a map into the
 * document: every existing marker is removed, and one is written at the start of each
 * paragraph that begins a new page and, inside a paragraph that spans pages, at each
 * page boundary (splitting the run there).  {@link #paginate} does both.</p>
 *
 * <p>The keys are the paragraphs' {@code w14:paraId}s; a paragraph without one (or with
 * a duplicate) is keyed {@code P<n>} by document order, transiently for {@code compute},
 * while {@code paginate} and {@code applyLastRenderedPageBreaks} assign such paragraphs
 * an id by default (see {@link PaginateSettings#getWriteParaIds()}), so that a document
 * paginated once keys stably from then on.</p>
 *
 * <p>The layout is of the document as if its tracked changes were accepted
 * ({@code ConversionFeatures.PP_COMMON_ACCEPT_TRACKED_CHANGES}): deleted text takes no
 * space and is never marked; a paragraph whose mark is deleted is one paragraph with
 * the next, keyed by the first.  The pages are FOP's, with the fonts the package's font
 * mapper resolves, so they drift from Word's where the metrics differ; the markers are
 * advisory, as Word's own are.</p>
 *
 * <p>Needs docx4j-export-fo on the classpath (found reflectively, as the TOC generator
 * finds it); the XSL-FO is produced by the non-XSLT exporter.</p>
 *
 * @since 17.1.1
 */
public final class Paginate {

	private static Logger log = LoggerFactory.getLogger(Paginate.class);

	/** FOP's area tree output format (org.apache.fop.apps.MimeConstants.MIME_FOP_AREA_TREE). */
	public static final String MIME_FOP_AREA_TREE = "application/X-fop-areatree";

	/** A key Paginate assigns for a call: never a paraId, which is hex. */
	private static final Pattern TRANSIENT_KEY = Pattern.compile("P[0-9]+");

	private static final ObjectFactory factory = new ObjectFactory();

	private Paginate() {}

	/**
	 * Lay the document out and return which page each paragraph starts on.  The document
	 * is not changed (unless {@code settings.writeParaIds} is set).
	 *
	 * @param settings may be null
	 * @throws Docx4JException if docx4j-export-fo is not available, or the layout fails
	 */
	public static PaginationMap compute(WordprocessingMLPackage pkg, PaginateSettings settings)
			throws Docx4JException {

		if (settings == null) settings = new PaginateSettings();
		return compute(pkg, settings, settings.writeParaIds(false));
	}

	/**
	 * {@link #compute} then {@link #applyLastRenderedPageBreaks}: the document's
	 * {@code w:lastRenderedPageBreak} markers are rewritten from a fresh layout, and
	 * paragraphs lacking a {@code w14:paraId} are given one (unless the settings say not to).
	 *
	 * @param settings may be null
	 * @return the map the markers were written from
	 */
	public static PaginationMap paginate(WordprocessingMLPackage pkg, PaginateSettings settings)
			throws Docx4JException {

		if (settings == null) settings = new PaginateSettings();
		PaginationMap map = compute(pkg, settings, settings.writeParaIds(true));
		// ids, if wanted, were assigned before the layout
		applyLastRenderedPageBreaks(pkg, map, false, settings.lineBreaks());
		return map;
	}

	/**
	 * Remove every {@code w:lastRenderedPageBreak} from the main document part and write
	 * the map's page boundaries, assigning a {@code w14:paraId} to paragraphs lacking one.
	 *
	 * @return the number of markers written
	 * @throws IllegalArgumentException if the map was computed for a different document
	 *         state (its paragraphs no longer line up with the document's)
	 */
	public static int applyLastRenderedPageBreaks(WordprocessingMLPackage pkg, PaginationMap map) {
		return applyLastRenderedPageBreaks(pkg, map, true, true);
	}

	/** As {@link #applyLastRenderedPageBreaks(WordprocessingMLPackage, PaginationMap)}, with
	 *  the choice of assigning ids. */
	public static int applyLastRenderedPageBreaks(WordprocessingMLPackage pkg, PaginationMap map,
			boolean writeParaIds) {
		return applyLastRenderedPageBreaks(pkg, map, writeParaIds, true);
	}

	/**
	 * As {@link #applyLastRenderedPageBreaks(WordprocessingMLPackage, PaginationMap)}, with
	 * the choice of assigning ids and of markers inside paragraphs.
	 *
	 * <p>A marker goes at the start of a paragraph whose start page is later than the page
	 * the preceding paragraphs' content ended on, whatever caused the break: Word writes
	 * one after an explicit page break or section break too.  It is the first item of the
	 * paragraph's first run (a run inside a hyperlink, content control or insertion
	 * included; deleted runs are not rendered and are passed over); a paragraph with no
	 * run gets one to hold it.</p>
	 *
	 * <p>With {@code lineBreaks}, a paragraph that spans pages gets a marker at each page
	 * boundary ({@link PaginationMap#getBreaks}): before the first character of the new
	 * page's line, the run split there into two (the second, with a copy of the run
	 * properties, opening with the marker), or at the run boundary where the boundary
	 * falls on one.</p>
	 *
	 * <p>A paragraph that begins with a page break gets no marker at its start: Word lays
	 * the break out as the last line of the page before, so the run holding it starts
	 * there (the export moves such a break in front of the paragraph's block, which is
	 * why the map has the paragraph on the page after); with {@code lineBreaks} the text
	 * after the break gets the marker.  A paragraph holding nothing but page breaks is
	 * taken to end on the page before the one the map places it on, so the paragraph
	 * after it gets the marker, where Word puts it.</p>
	 *
	 * @since 17.1.1 (CR-012 phase 2)
	 */
	public static int applyLastRenderedPageBreaks(WordprocessingMLPackage pkg, PaginationMap map,
			boolean writeParaIds, boolean lineBreaks) {

		MainDocumentPart mdp = pkg.getMainDocumentPart();
		List<P> paragraphs = bodyParagraphs(mdp);
		List<String> keys = map.getKeys().isEmpty() ? keys(paragraphs) : map.getKeys();
		checkSameDocument(keys, keys(paragraphs));

		removeLastRenderedPageBreaks(mdp);
		if (writeParaIds) assignParaIds(mdp, paragraphs);

		int written = 0;
		Integer contentEnd = null;  // the last page any preceding paragraph's content reached
		Integer flowEnd = null;     // ... or its mark, which a page break moves a page on
		Set<P> joined = new HashSet<P>();
		for (int i = 0; i < paragraphs.size(); i++) {
			P p = paragraphs.get(i);
			if (joined.contains(p)) continue;
			LayoutParagraph lp = LayoutParagraph.of(paragraphs, i);
			joined.addAll(lp.members.subList(1, lp.members.size()));

			String key = keys.get(i);
			Integer start = map.getPageIndex(key);
			List<Object> runItems = lp.runItems();
			boolean leadingBreak = !runItems.isEmpty() && isPageBreak(runItems.get(0));
			boolean breakOnly = leadingBreak && allPageBreaks(runItems);

			if (start == null) {
				// not placed: a break-only paragraph whose break moved onto the next block
				// sits where the previous paragraph's mark ended
				if (breakOnly && flowEnd != null && contentEnd != null && flowEnd.intValue() > contentEnd.intValue()) {
					insertMarker(p);
					written++;
					contentEnd = flowEnd;
					flowEnd = Integer.valueOf(flowEnd.intValue() + 1);
				}
				continue;
			}

			boolean newPage = contentEnd != null && start.intValue() > contentEnd.intValue();
			if (newPage && !leadingBreak) {
				insertMarker(p);
				written++;
			} else if (newPage && leadingBreak && !breakOnly && lineBreaks) {
				if (placeAt(lp, 0)) written++;   // after the break, before the text
			} else if (breakOnly && contentEnd != null && start.intValue() - 1 > contentEnd.intValue()) {
				insertMarker(p);                 // its break run opens a page
				written++;
			}
			if (lineBreaks) {
				for (int offset : map.getBreaks(key)) {
					if (placeAt(lp, offset)) written++;
				}
			}

			int end = map.getLastPageIndex(key).intValue();
			if (breakOnly) {
				// its break is on the page before the one FOP placed it on; at the start
				// of the document FOP places it on page 1 and ignores the break
				contentEnd = max(contentEnd, Math.max(start.intValue() - 1, 1));
				flowEnd = max(flowEnd, end);
			} else {
				contentEnd = max(contentEnd, end);
				flowEnd = max(flowEnd, end);
			}
		}
		log.debug("wrote " + written + " w:lastRenderedPageBreak");
		return written;
	}

	private static Integer max(Integer a, int b) {
		return Integer.valueOf(a == null ? b : Math.max(a.intValue(), b));
	}

	/**
	 * Remove every {@code w:lastRenderedPageBreak} from the main document part (text boxes
	 * included), as Word's own re-rendering replaces them.
	 *
	 * @return the number removed
	 */
	public static int removeLastRenderedPageBreaks(MainDocumentPart mdp) {

		final int[] removed = { 0 };
		new TraversalUtil(mdp.getContent(), new TraversalUtil.CallbackImpl() {
			@Override
			public List<Object> apply(Object o) {
				if (o instanceof R) {
					Iterator<Object> it = ((R) o).getContent().iterator();
					while (it.hasNext()) {
						if (XmlUtils.unwrap(it.next()) instanceof R.LastRenderedPageBreak) {
							it.remove();
							removed[0]++;
						}
					}
				}
				return null;
			}
		});
		return removed[0];
	}

	/**
	 * Give each keyed paragraph of the main document part without a {@code w14:paraId} (or
	 * with one another paragraph already carries) an id: eight hex digits below 0x80000000,
	 * unique in the part, with {@code w14:textId} the same, as Word writes them (ECMA-376
	 * 17.3.1.20 plus [MS-DOCX]).  The {@code w14} namespace is added to the document's
	 * {@code mc:Ignorable}.
	 *
	 * @return the number of ids assigned
	 */
	public static int assignParaIds(MainDocumentPart mdp) {
		return assignParaIds(mdp, bodyParagraphs(mdp));
	}

	private static int assignParaIds(MainDocumentPart mdp, List<P> paragraphs) {

		// ids in use anywhere in the part (text box paragraphs included), not to be reused
		final Set<String> inUse = new HashSet<String>();
		new TraversalUtil(mdp.getContent(), new TraversalUtil.CallbackImpl() {
			@Override
			public List<Object> apply(Object o) {
				if (o instanceof P && ((P) o).getParaId() != null) inUse.add(((P) o).getParaId());
				return null;
			}
		});

		int assigned = 0;
		Set<String> seen = new HashSet<String>();
		for (P p : paragraphs) {
			String id = p.getParaId();
			if (id != null && !id.isEmpty() && seen.add(id)) continue;
			do {
				id = String.format("%08X", ThreadLocalRandom.current().nextInt(1, 0x7FFFFFFF));
			} while (inUse.contains(id));
			inUse.add(id);
			seen.add(id);
			p.setParaId(id);
			p.setTextId(id);
			assigned++;
		}

		if (assigned > 0) declareW14Ignorable(mdp);
		return assigned;
	}

	private static void declareW14Ignorable(MainDocumentPart mdp) {

		Document document = mdp.getJaxbElement();
		if (document == null) return;
		String ignorable = document.getIgnorable();
		if (ignorable == null || ignorable.trim().isEmpty()) {
			document.setIgnorable("w14");
		} else if (!Arrays.asList(ignorable.trim().split("\\s+")).contains("w14")) {
			document.setIgnorable(ignorable.trim() + " w14");
		}
	}

	/**
	 * The paragraphs {@link Paginate} keys, in document order: the main document part's,
	 * including those in tables and block-level content controls, but not those in text
	 * boxes (which are reached only through a run).
	 */
	public static List<P> bodyParagraphs(MainDocumentPart mdp) {

		final List<P> paragraphs = new ArrayList<P>();
		new TraversalUtil(mdp.getContent(), new TraversalUtil.CallbackImpl() {
			@Override
			public List<Object> apply(Object o) {
				if (o instanceof P) paragraphs.add((P) o);
				return null;
			}
			@Override
			public boolean shouldTraverse(Object o) {
				return !(o instanceof P);
			}
		});
		return paragraphs;
	}

	/**
	 * The keys of these paragraphs, in order: each one's {@code w14:paraId}, or
	 * {@code P<n>} (n its 1-based position) where it has none or the id was already used
	 * by an earlier paragraph.
	 */
	public static List<String> keys(List<P> paragraphs) {

		List<String> keys = new ArrayList<String>(paragraphs.size());
		Set<String> seen = new HashSet<String>();
		for (int i = 0; i < paragraphs.size(); i++) {
			String id = paragraphs.get(i).getParaId();
			keys.add(id != null && !id.isEmpty() && seen.add(id) ? id : "P" + (i + 1));
		}
		return keys;
	}

	// --- compute ---

	private static PaginationMap compute(WordprocessingMLPackage pkg, PaginateSettings settings,
			boolean writeParaIds) throws Docx4JException {

		if (!Docx4J.pdfViaFO()) {
			throw new Docx4JException("Paginate needs docx4j-export-fo on the classpath");
		}

		MainDocumentPart mdp = pkg.getMainDocumentPart();
		List<P> paragraphs = bodyParagraphs(mdp);
		if (writeParaIds) assignParaIds(mdp, paragraphs);
		List<String> keys = keys(paragraphs);

		// The exporter puts the paragraph's paraId in its fo:block id, so a paragraph
		// keyed P<n> carries that as its paraId for the duration of the layout
		Map<P, String> restore = new IdentityHashMap<P, String>();
		for (int i = 0; i < paragraphs.size(); i++) {
			P p = paragraphs.get(i);
			if (!keys.get(i).equals(p.getParaId())) {
				restore.put(p, p.getParaId());
				p.setParaId(keys.get(i));
			}
		}
		FOSettings foSettings = settings.getFoSettings();
		if (foSettings == null) foSettings = Docx4J.createFOSettings();
		byte[] areaTree;
		try {
			areaTree = layout(pkg, foSettings);
		} finally {
			for (Map.Entry<P, String> e : restore.entrySet()) {
				e.getKey().setParaId(e.getValue());
			}
		}

		PaginationMap map = parse(areaTree);
		map.retainAll(new HashSet<String>(keys));
		resolveBreaks(map, pkg, paragraphs, keys, foSettings.getFeatures());
		map.setKeys(keys);
		if (log.isDebugEnabled()) log.debug(map.toString());
		return map;
	}

	/** Lay the package out with FOP, returning its area tree XML. */
	private static byte[] layout(WordprocessingMLPackage pkg, FOSettings foSettings)
			throws Docx4JException {

		long start = System.currentTimeMillis();

		foSettings.setOpcPackage(pkg);
		foSettings.setApacheFopMime(MIME_FOP_AREA_TREE);
		foSettings.getFeatures().add(ConversionFeatures.PP_FO_PARAGRAPH_IDS);
		// the accepted view: deleted text takes no space (decision, CR-012 §3.1)
		foSettings.getFeatures().add(ConversionFeatures.PP_COMMON_ACCEPT_TRACKED_CHANGES);
		// as TocGenerator's page-number pass (which phase 3 folds into this one), so
		// that the two agree about where the pages fall
		foSettings.getFeatures().add(ConversionFeatures.PP_PDF_APACHEFOP_DISABLE_PAGEBREAK_LIST_ITEM);

		ByteArrayOutputStream os = new ByteArrayOutputStream();
		try {
			FopReflective.invokeFORendererApacheFOP(foSettings); // the FOUserAgent, with the fonts
		} catch (Exception e) {
			throw new Docx4JException("Paginate: can't configure Apache FOP; is docx4j-export-fo on the classpath? "
					+ e.getMessage(), e);
		}
		Docx4J.toFO(foSettings, os, Docx4J.FLAG_EXPORT_PREFER_NONXSL);

		if (log.isDebugEnabled()) {
			log.debug("Area tree in " + (System.currentTimeMillis() - start) + " ms");
		}
		return os.toByteArray();
	}

	/**
	 * Read FOP's area tree XML (its {@code application/X-fop-areatree} output for an FO
	 * document whose paragraph blocks carry ids, see
	 * {@link ConversionFeatures#PP_FO_PARAGRAPH_IDS}) into a map.  The map's key list is
	 * empty and its break offsets unresolved (see {@link PaginationMap#getBreaks}): the
	 * caller knows the paragraphs.
	 */
	public static PaginationMap parse(byte[] areaTree) throws Docx4JException {

		try {
			SAXParserFactory spf = SAXParserFactory.newInstance();
			SAXParser saxParser = spf.newSAXParser();
			PaginationAreaTreeHandler handler = new PaginationAreaTreeHandler();
			saxParser.parse(new ByteArrayInputStream(areaTree), handler);
			return handler.getMap();
		} catch (Exception e) {
			throw new Docx4JException("Paginate: can't read FOP's area tree: " + e.getMessage(), e);
		}
	}

	/**
	 * The boundaries the area tree reader recorded are relative to the part of the
	 * paragraph they are in (the preprocessing splits a paragraph at a page break inside
	 * it, and the exporter counts each part's runs from 0): replay the split to find each
	 * part's base offset, and correct for a hyphen FOP added at the line end.
	 */
	static void resolveBreaks(PaginationMap map, WordprocessingMLPackage pkg, List<P> paragraphs,
			List<String> keys, Set<String> features) {

		boolean movePageBreaks = features.contains(ConversionFeatures.PP_COMMON_MOVE_PAGEBREAK);
		boolean splitAtBreaks = movePageBreaks
				&& CompatibilityOptions.of(pkg).is(CompatibilityOptions.Flag.SPLIT_PG_BREAK_AND_PARA_MARK);
		boolean keepBreakLine = movePageBreaks && Preprocess.keepBreakLine(features);

		for (int i = 0; i < paragraphs.size(); i++) {
			String key = keys.get(i);
			List<PaginationMap.RawBreak> raws = map.raw(key);
			if (raws.isEmpty()) continue;

			LayoutParagraph lp = LayoutParagraph.of(paragraphs, i);
			List<Integer> bases = lp.partBases(splitAtBreaks, keepBreakLine);
			String text = lp.text();
			TreeSet<Integer> resolved = new TreeSet<Integer>();
			for (PaginationMap.RawBreak raw : raws) {
				int offset;
				if (raw.absolute) {
					offset = raw.offset;
				} else if (raw.part < bases.size()) {
					offset = bases.get(raw.part).intValue() + raw.offset;
				} else {
					log.warn("Paragraph " + key + ": the layout split it into more parts (" + (raw.part + 1)
							+ ") than its page breaks account for (" + bases.size() + "); a boundary in part "
							+ raw.part + " is dropped");
					continue;
				}
				if (raw.hyphenEnds > 0) {
					// each hyphenated line end was FOP's hyphen (one counted too many) or
					// the document's: the new page's first word says how many were FOP's
					String probe = firstWord(raw.lineStart.toString());
					for (int c = offset; probe.length() > 0 && c >= offset - raw.hyphenEnds; c--) {
						if (c >= 0 && c + probe.length() <= text.length() && text.startsWith(probe, c)) {
							offset = c;
							break;
						}
					}
				}
				offset = Math.max(0, Math.min(offset, text.length()));
				// FOP drops the space at a line end, so the count lands on it: the new page
				// begins at the next character, which is where the writer puts the marker
				while (offset < text.length() && text.charAt(offset) == ' ') offset++;
				if (offset == 0 || offset >= text.length()) continue; // nothing to mark inside
				resolved.add(Integer.valueOf(offset));
			}
			int[] breaks = new int[resolved.size()];
			int n = 0;
			for (Integer offset : resolved) breaks[n++] = offset.intValue();
			map.setBreaks(key, breaks);
		}
	}

	private static String firstWord(String lineStart) {
		String s = lineStart.trim();
		int space = s.indexOf(' ');
		return space < 0 ? s : s.substring(0, space);
	}

	// --- the paragraph as laid out ---

	/**
	 * A paragraph as the layout knows it: the document paragraph, joined with the ones
	 * after it while its mark is deleted (the accepted view; see AcceptTrackedChanges).
	 */
	static final class LayoutParagraph {

		final List<P> members = new ArrayList<P>();

		static LayoutParagraph of(List<P> paragraphs, int i) {
			LayoutParagraph lp = new LayoutParagraph();
			P p = paragraphs.get(i);
			lp.members.add(p);
			while (markDeleted(p) && i + 1 < paragraphs.size()
					&& paragraphs.get(i + 1).getParent() == p.getParent()) {
				p = paragraphs.get(++i);
				lp.members.add(p);
			}
			return lp;
		}

		private static boolean markDeleted(P p) {
			return p.getPPr() != null && p.getPPr().getRPr() != null && p.getPPr().getRPr().getDel() != null;
		}

		/** The block-level content, all members'. */
		List<Object> content() {
			if (members.size() == 1) return members.get(0).getContent();
			List<Object> all = new ArrayList<Object>();
			for (P p : members) all.addAll(p.getContent());
			return all;
		}

		List<R> runs() {
			return RunText.runs(content());
		}

		/** The runs' items, unwrapped, in order. */
		List<Object> runItems() {
			List<Object> items = new ArrayList<Object>();
			for (R r : runs()) {
				for (Object item : r.getContent()) items.add(XmlUtils.unwrap(item));
			}
			return items;
		}

		/** The text, in {@link RunText}'s model. */
		String text() {
			StringBuilder sb = new StringBuilder();
			for (R r : runs()) RunText.appendText(r, sb);
			return sb.toString();
		}

		/** The offset at which each part the preprocessing would split this paragraph
		 *  into starts; the first is 0. */
		List<Integer> partBases(boolean splitAtBreaks, boolean keepBreakLine) {

			List<Integer> bases = new ArrayList<Integer>();
			bases.add(Integer.valueOf(0));
			List<Object> content = content();
			P last = members.get(members.size() - 1);
			boolean breaksBefore = last.getPPr() != null && last.getPPr().getPageBreakBefore() != null
					&& last.getPPr().getPageBreakBefore().isVal();
			for (int[] at : PageBreak.splitPositions(content, breaksBefore, splitAtBreaks, keepBreakLine)) {
				int offset = 0;
				for (R r : RunText.runs(content.subList(0, at[0]))) offset += RunText.length(r);
				if (at.length > 1) {
					offset += RunText.length(((R) content.get(at[0])).getContent().subList(0, at[1]));
				}
				bases.add(Integer.valueOf(offset));
			}
			return bases;
		}
	}

	// --- apply ---

	/**
	 * The map's keys and the document's must name the same paragraphs, in the same order:
	 * a paragraph's paraId may have been assigned since (the map keyed it P<n>), but not
	 * changed, and none added or removed.
	 */
	private static void checkSameDocument(List<String> mapKeys, List<String> documentKeys) {

		boolean same = mapKeys.size() == documentKeys.size();
		for (int i = 0; same && i < mapKeys.size(); i++) {
			String a = mapKeys.get(i);
			String b = documentKeys.get(i);
			same = a.equals(b)
					|| TRANSIENT_KEY.matcher(a).matches()
					|| TRANSIENT_KEY.matcher(b).matches();
		}
		if (!same) {
			throw new IllegalArgumentException("The pagination map was computed for a different document state: "
					+ mapKeys.size() + " paragraphs keyed, the document has " + documentKeys.size()
					+ (mapKeys.size() == documentKeys.size() ? " and their ids differ" : ""));
		}
	}

	private static boolean isPageBreak(Object runItem) {
		return runItem instanceof Br && ((Br) runItem).getType() == STBrType.PAGE;
	}

	private static boolean allPageBreaks(List<Object> runItems) {
		for (Object item : runItems) {
			if (!isPageBreak(item)) return false;
		}
		return !runItems.isEmpty();
	}

	private static boolean isMarker(Object item) {
		return XmlUtils.unwrap(item) instanceof R.LastRenderedPageBreak;
	}

	private static Object newMarker(R r) {
		R.LastRenderedPageBreak marker = factory.createRLastRenderedPageBreak();
		marker.setParent(r);
		return factory.createRLastRenderedPageBreak(marker);
	}

	/** The marker as the first item of the paragraph's first (non-deleted) run. */
	private static void insertMarker(P p) {

		final R[] first = { null };
		new TraversalUtil(p.getContent(), new TraversalUtil.CallbackImpl() {
			@Override
			public List<Object> apply(Object o) {
				if (first[0] == null && o instanceof R) first[0] = (R) o;
				return null;
			}
			@Override
			public boolean shouldTraverse(Object o) {
				return first[0] == null && !(o instanceof R) && !(o instanceof RunDel);
			}
		});

		R r = first[0];
		if (r == null) {
			r = factory.createR();
			r.setParent(p);
			p.getContent().add(0, r);
		}
		if (!r.getContent().isEmpty() && isMarker(r.getContent().get(0))) return;
		r.getContent().add(0, newMarker(r));
	}

	/**
	 * A marker before the character at this offset of the paragraph's text, stepping over
	 * the spaces, tabs and breaks there first (FOP drops the space at a line end; Word
	 * puts the marker before the first character of the new line).  Splits the run where
	 * the offset falls inside a {@code w:t}.
	 *
	 * @return whether a marker was written (not where the offset is past the text, or a
	 *         marker is there already)
	 */
	private static boolean placeAt(LayoutParagraph lp, int offset) {

		List<R> runs = lp.runs();
		int pos = 0;
		int ri = 0, m = 0, k = 0;
		boolean found = false;
		outer:
		for (ri = 0; ri < runs.size(); ri++) {
			List<Object> items = runs.get(ri).getContent();
			for (m = 0; m < items.size(); m++) {
				int len = RunText.length(items.get(m));
				if (offset < pos + len || (offset == pos && len == 0)) {
					k = offset - pos;
					found = true;
					break outer;
				}
				if (offset == pos + len && len > 0 && m == items.size() - 1 && ri == runs.size() - 1) {
					return false; // the paragraph's end
				}
				pos += len;
			}
		}
		if (!found) {
			// past every item with characters: at the start of the next item, if any
			ri = -1;
			pos = 0;
			outer2:
			for (int i = 0; i < runs.size(); i++) {
				List<Object> items = runs.get(i).getContent();
				for (int j = 0; j < items.size(); j++) {
					if (pos >= offset) { ri = i; m = j; k = 0; found = true; break outer2; }
					pos += RunText.length(items.get(j));
				}
			}
			if (!found) return false;
		}

		// step over what ends a line: spaces, tabs, breaks
		while (true) {
			List<Object> items = runs.get(ri).getContent();
			if (m >= items.size()) {
				if (++ri >= runs.size()) return false;
				m = 0;
				k = 0;
				continue;
			}
			Object item = items.get(m);
			Object v = XmlUtils.unwrap(item);
			if (v instanceof Text && RunText.length(item) > 0) {
				String s = ((Text) v).getValue();
				while (k < s.length() && s.charAt(k) == ' ') k++;
				if (k < s.length()) break;
				m++;
				k = 0;
			} else if (RunText.isSkippable(item) || (RunText.length(item) == 0 && v instanceof Text)) {
				m++;
				k = 0;
			} else {
				break; // a symbol, a drawing, a field: the marker goes before it
			}
		}

		R r = runs.get(ri);
		List<Object> items = r.getContent();
		if (k == 0) {
			if (isMarker(items.get(m)) || (m > 0 && isMarker(items.get(m - 1)))) return false;
			items.add(m, newMarker(r));
			return true;
		}

		// inside a w:t: split the run there
		Text t = (Text) XmlUtils.unwrap(items.get(m));
		String s = t.getValue();
		t.setValue(s.substring(0, k));
		t.setSpace("preserve");
		Text tail = factory.createText();
		tail.setValue(s.substring(k));
		tail.setSpace("preserve");

		R second = factory.createR();
		if (r.getRPr() != null) {
			second.setRPr(XmlUtils.deepCopy(r.getRPr()));
			second.getRPr().setParent(second);
		}
		List<Object> siblings = siblingsOf(r, lp);
		if (siblings == null) {
			log.warn("Run has no parent list; the marker goes before its text instead of inside it");
			t.setValue(s);
			items.add(m, newMarker(r));
			return true;
		}
		List<Object> moved = new ArrayList<Object>(items.subList(m + 1, items.size()));
		while (items.size() > m + 1) items.remove(items.size() - 1);
		second.getContent().add(newMarker(second));
		tail.setParent(second);
		second.getContent().add(tail);
		for (Object o : moved) {
			Object v = XmlUtils.unwrap(o);
			if (v instanceof org.jvnet.jaxb.lang.Child) ((org.jvnet.jaxb.lang.Child) v).setParent(second);
			second.getContent().add(o);
		}
		int at = indexOfIdentity(siblings, r);
		second.setParent(r.getParent());
		siblings.add(at + 1, second);
		return true;
	}

	/** The list the run sits in: by its parent pointer, else by searching the paragraph. */
	private static List<Object> siblingsOf(R r, LayoutParagraph lp) {

		Object parent = r.getParent();
		List<Object> list = contentOf(parent);
		if (list != null && indexOfIdentity(list, r) >= 0) return list;
		for (P p : lp.members) {
			list = find(p.getContent(), r);
			if (list != null) return list;
		}
		return null;
	}

	private static List<Object> contentOf(Object container) {
		if (container instanceof RunIns) return ((RunIns) container).getCustomXmlOrSmartTagOrSdt();
		if (container instanceof RunTrackChange) return ((RunTrackChange) container).getAccOrBarOrBox();
		if (container instanceof ContentAccessor) return ((ContentAccessor) container).getContent();
		return null;
	}

	private static List<Object> find(List<Object> content, R r) {
		if (content == null) return null;
		if (indexOfIdentity(content, r) >= 0) return content;
		for (Object o : content) {
			Object v = XmlUtils.unwrap(o);
			if (v instanceof R || v instanceof RunDel) continue;
			List<Object> inner = (v instanceof SdtElement && ((SdtElement) v).getSdtContent() != null
					? ((SdtElement) v).getSdtContent().getContent() : contentOf(v));
			List<Object> hit = find(inner, r);
			if (hit != null) return hit;
		}
		return null;
	}

	private static int indexOfIdentity(List<Object> list, Object o) {
		for (int i = 0; i < list.size(); i++) {
			if (XmlUtils.unwrap(list.get(i)) == o) return i;
		}
		return -1;
	}

	/** Whether this key is one Paginate assigned for a call ({@code P<n>}), not a paraId. */
	public static boolean isTransientKey(String key) {
		return key != null && TRANSIENT_KEY.matcher(key).matches();
	}
}
