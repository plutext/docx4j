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
import org.docx4j.openpackaging.exceptions.Docx4JException;
import org.docx4j.openpackaging.packages.WordprocessingMLPackage;
import org.docx4j.openpackaging.parts.WordprocessingML.MainDocumentPart;
import org.docx4j.wml.Br;
import org.docx4j.wml.Document;
import org.docx4j.wml.ObjectFactory;
import org.docx4j.wml.P;
import org.docx4j.wml.R;
import org.docx4j.wml.RunDel;
import org.docx4j.wml.STBrType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Lays a document out with Apache FOP (via docx4j-export-fo) to find which page each
 * paragraph starts on, and writes the result back as {@code w:lastRenderedPageBreak}
 * markers, the way Word records the pagination of its last rendering (ECMA-376
 * 17.3.3.13).  See CR-012.
 *
 * <p>{@link #compute} is the query: a {@link PaginationMap} from paragraph key to page.
 * {@link #applyLastRenderedPageBreaks} writes a map into the document: every existing
 * marker is removed, and one is written at the start of each paragraph that begins a
 * new page.  {@link #paginate} does both.</p>
 *
 * <p>The keys are the paragraphs' {@code w14:paraId}s; a paragraph without one (or with
 * a duplicate) is keyed {@code P<n>} by document order, transiently for {@code compute},
 * while {@code paginate} and {@code applyLastRenderedPageBreaks} assign such paragraphs
 * an id by default (see {@link PaginateSettings#getWriteParaIds()}), so that a document
 * paginated once keys stably from then on.</p>
 *
 * <p>The pages are FOP's, with the fonts the package's font mapper resolves, so they
 * drift from Word's where the metrics differ; the markers are advisory, as Word's own
 * are.  This phase writes markers at paragraph boundaries only: a paragraph that spans
 * pages is reported in the map ({@link PaginationMap#getBreaks}), but the marker inside
 * it is CR-012 phase 2.</p>
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
		applyLastRenderedPageBreaks(pkg, map, false); // ids, if wanted, were assigned before the layout
		return map;
	}

	/**
	 * Remove every {@code w:lastRenderedPageBreak} from the main document part and write one
	 * at the start of each paragraph that begins a new page in the map, assigning a
	 * {@code w14:paraId} to paragraphs lacking one.
	 *
	 * @return the number of markers written
	 * @throws IllegalArgumentException if the map was computed for a different document
	 *         state (its paragraphs no longer line up with the document's)
	 */
	public static int applyLastRenderedPageBreaks(WordprocessingMLPackage pkg, PaginationMap map) {
		return applyLastRenderedPageBreaks(pkg, map, true);
	}

	/**
	 * As {@link #applyLastRenderedPageBreaks(WordprocessingMLPackage, PaginationMap)}, with
	 * the choice of assigning ids.
	 *
	 * <p>A marker goes at the start of a paragraph whose start page is later than the page
	 * the preceding paragraphs' content ended on, whatever caused the break: Word writes
	 * one after an explicit page break or section break too.  It is the first item of the
	 * paragraph's first run (a run inside a hyperlink, content control or insertion
	 * included; deleted runs are not rendered and are passed over); a paragraph with no
	 * run gets one to hold it.  The paragraph's page in the map does not change.</p>
	 *
	 * <p>A paragraph that begins with a page break gets no marker: Word lays the break
	 * out as the last line of the page before, so the run holding it starts there (the
	 * export moves such a break in front of the paragraph's block, which is why the map
	 * has the paragraph on the page after).  A paragraph holding nothing but page breaks
	 * is taken to end on the page before the one the map places it on, so the paragraph
	 * after it gets the marker, where Word puts it.</p>
	 */
	public static int applyLastRenderedPageBreaks(WordprocessingMLPackage pkg, PaginationMap map,
			boolean writeParaIds) {

		MainDocumentPart mdp = pkg.getMainDocumentPart();
		List<P> paragraphs = bodyParagraphs(mdp);
		List<String> keys = map.getKeys().isEmpty() ? keys(paragraphs) : map.getKeys();
		checkSameDocument(keys, keys(paragraphs));

		removeLastRenderedPageBreaks(mdp);
		if (writeParaIds) assignParaIds(mdp, paragraphs);

		int written = 0;
		Integer pageSoFar = null;  // the last page any preceding paragraph's content reached
		for (int i = 0; i < paragraphs.size(); i++) {
			String key = keys.get(i);
			Integer start = map.getPageIndex(key);
			if (start == null) continue;  // not placed by the layout (a break-only paragraph
			                              // whose break moved onto the next block, say)
			P p = paragraphs.get(i);
			List<Object> runItems = runItems(p);
			boolean leadingBreak = !runItems.isEmpty() && isPageBreak(runItems.get(0));
			if (!leadingBreak && pageSoFar != null && start.intValue() > pageSoFar.intValue()) {
				insertMarker(p);
				written++;
			}
			int end = map.getLastPageIndex(key).intValue();
			if (leadingBreak && allPageBreaks(runItems)) end = start.intValue() - 1;
			pageSoFar = Integer.valueOf(pageSoFar == null ? end : Math.max(pageSoFar.intValue(), end));
		}
		log.debug("wrote " + written + " w:lastRenderedPageBreak");
		return written;
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
		byte[] areaTree;
		try {
			areaTree = layout(pkg, settings);
		} finally {
			for (Map.Entry<P, String> e : restore.entrySet()) {
				e.getKey().setParaId(e.getValue());
			}
		}

		PaginationMap map = parse(areaTree);
		map.retainAll(new HashSet<String>(keys));
		map.setKeys(keys);
		if (log.isDebugEnabled()) log.debug(map.toString());
		return map;
	}

	/** Lay the package out with FOP, returning its area tree XML. */
	private static byte[] layout(WordprocessingMLPackage pkg, PaginateSettings settings)
			throws Docx4JException {

		long start = System.currentTimeMillis();

		FOSettings foSettings = settings.getFoSettings();
		if (foSettings == null) foSettings = Docx4J.createFOSettings();
		foSettings.setOpcPackage(pkg);
		foSettings.setApacheFopMime(MIME_FOP_AREA_TREE);
		foSettings.getFeatures().add(ConversionFeatures.PP_FO_PARAGRAPH_IDS);
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
	 * empty: the caller knows the paragraphs.
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

	/** The items of the paragraph's (non-deleted) runs, in order, unwrapped. */
	private static List<Object> runItems(P p) {

		final List<Object> items = new ArrayList<Object>();
		new TraversalUtil(p.getContent(), new TraversalUtil.CallbackImpl() {
			@Override
			public List<Object> apply(Object o) {
				if (o instanceof R) {
					for (Object item : ((R) o).getContent()) items.add(XmlUtils.unwrap(item));
				}
				return null;
			}
			@Override
			public boolean shouldTraverse(Object o) {
				return !(o instanceof R) && !(o instanceof RunDel);
			}
		});
		return items;
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
		R.LastRenderedPageBreak marker = factory.createRLastRenderedPageBreak();
		marker.setParent(r);
		r.getContent().add(0, factory.createRLastRenderedPageBreak(marker));
	}

	/** Whether this key is one Paginate assigned for a call ({@code P<n>}), not a paraId. */
	public static boolean isTransientKey(String key) {
		return key != null && TRANSIENT_KEY.matcher(key).matches();
	}
}
