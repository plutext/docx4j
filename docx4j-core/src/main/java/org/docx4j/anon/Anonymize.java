package org.docx4j.anon;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import jakarta.xml.bind.JAXBElement;

import org.docx4j.TraversalUtil;
import org.docx4j.anon.AnonymizeResult.Action;
import org.docx4j.anon.JaxbGraphWalker.Visitor;
import org.docx4j.anon.PartsAnalyzer.Treatment;
import org.docx4j.openpackaging.exceptions.Docx4JException;
import org.docx4j.openpackaging.exceptions.InvalidFormatException;
import org.docx4j.openpackaging.packages.OpcPackage;
import org.docx4j.openpackaging.packages.PresentationMLPackage;
import org.docx4j.openpackaging.packages.SpreadsheetMLPackage;
import org.docx4j.openpackaging.packages.WordprocessingMLPackage;
import org.docx4j.openpackaging.parts.JaxbXmlPart;
import org.docx4j.openpackaging.parts.Part;
import org.docx4j.openpackaging.parts.PartName;
import org.docx4j.openpackaging.parts.XmlPart;
import org.docx4j.openpackaging.parts.WordprocessingML.FooterPart;
import org.docx4j.openpackaging.parts.WordprocessingML.HeaderPart;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Anonymises a docx (or, since 17.2.1, a pptx or xlsx) in place: the text is scrambled,
 * identities, references and metadata scrubbed, media replaced, and whatever
 * cannot be made clean removed (STRICT, the default) or kept and reported (KEEP).
 * <p>
 * The guarantee (CR-019): a document this returns with {@code result.isClean()}
 * contains no text from the original in any text-bearing part (scrambled per
 * Unicode range); no author, initials, user id or presence information; no
 * external target or field-instruction argument; no metadata beyond what the
 * package needs to open; no media but placeholder pixels; no embedded object,
 * chart data, diagram text, macro, custom XML or altChunk. What it keeps, by
 * design, is the document's structure - paragraphs, runs, tables, sections,
 * styles, numbering, fonts, sizes, the shape of fields and content controls -
 * because that is what a layout, a corruption or a converter bug lives in.
 * <p>
 * One thing the structure does not keep is the conformance class: docx4j reads a
 * strict (ISO/IEC 29500 Strict) package by converting each part to the
 * transitional namespaces, and never writes strict, so an anonymised strict
 * document is a transitional one. Nothing else about it changes, but a bug which
 * is the strict dialect's own will not reproduce from the anonymised copy - ask
 * for the original in that case.
 * <p>
 * Usage:
 * <pre>
 * WordprocessingMLPackage pkg = WordprocessingMLPackage.load(in);
 * AnonymizeResult result = new Anonymize(pkg).go();
 * if (result.isClean()) pkg.save(out);
 * System.out.println(result.toJson());
 * </pre>
 * Set a font mapper on the package first if the non-Latin replacement characters
 * should be chosen from the glyphs the document's fonts have (the CLI does).
 * <p>
 * For a pptx (CR-019 phase 2) the same walk covers the slides, layouts, masters,
 * notes and handout master, the presentation (section names, custom shows, the
 * modify verifier, the embedded-font list), legacy and 2018 comments with their
 * authors, tags, table styles, charts and diagrams; media, OLE objects, ActiveX,
 * ink, embedded fonts, VBA and the thumbnail go as in a docx. A pptx has no
 * styles part and no font mapper, so non-Latin replacement characters stay in the
 * character's Unicode block with no glyph check.
 * <p>
 * For an xlsx (CR-019 phase 3) a third visitor, {@link SpreadsheetScrubber},
 * covers the workbook, sheets, shared strings, styles, tables, comments (legacy
 * and threaded), connections, query tables and external links: strings are
 * scrambled consistently (a table column still matches its header), numbers have
 * their digits randomised ({@link #setKeepNumbers(boolean)} keeps them, decision
 * 3), formulas are rewritten to compute over the result ({@link SmlFormulas}),
 * sheets become {@code Sheet<n>} and defined names {@code n_<hash>}; pivot tables
 * and caches, slicers, timelines and the data model are removed (their cells stay
 * as values) whatever the mode; OLE, ActiveX, printer settings and the rich-value
 * parts go in STRICT.
 */
public class Anonymize {

	private static Logger log = LoggerFactory.getLogger(Anonymize.class);

	/** What to do with a part the tool cannot make clean. */
	public enum Mode {
		/** remove it (with its relationships and the markup that pointed at it); the default */
		STRICT,
		/** keep it, report it, and never call the result clean */
		KEEP
	}

	private final OpcPackage pkg;
	private final Mode mode;
	private boolean verify = true;
	private boolean keepNumbers = false;

	ScrambleText latinizer = null;
	MarkupScrubber markupScrubber = null;
	SpreadsheetScrubber spreadsheetScrubber = null;
	DmlVmlAnalyzer dmlVmlAnalyzer = null;
	Names names = null;

	AnonymizeResult result;

	public Anonymize(WordprocessingMLPackage wordMLPackage) {
		this((OpcPackage) wordMLPackage, Mode.STRICT);
	}

	/**
	 * @since 17.2.0
	 */
	public Anonymize(WordprocessingMLPackage wordMLPackage, Mode mode) {
		this((OpcPackage) wordMLPackage, mode);
	}

	/**
	 * @since 17.2.1
	 */
	public Anonymize(PresentationMLPackage pmlPackage, Mode mode) {
		this((OpcPackage) pmlPackage, mode);
	}

	/**
	 * @since 17.2.1
	 */
	public Anonymize(SpreadsheetMLPackage smlPackage, Mode mode) {
		this((OpcPackage) smlPackage, mode);
	}

	/**
	 * A docx, pptx or xlsx (as {@link OpcPackage#load(java.io.File)} returns it).
	 *
	 * @throws IllegalArgumentException for a package of another kind
	 * @since 17.2.1
	 */
	public Anonymize(OpcPackage pkg, Mode mode) {
		if (!(pkg instanceof WordprocessingMLPackage) && !(pkg instanceof PresentationMLPackage)
				&& !(pkg instanceof SpreadsheetMLPackage)) {
			throw new IllegalArgumentException("only docx, pptx and xlsx are supported: "
					+ (pkg == null ? "null" : pkg.getClass().getSimpleName()));
		}
		this.pkg = pkg;
		this.mode = mode;
		result = new AnonymizeResult();
	}

	/**
	 * STRICT.
	 *
	 * @since 17.2.1
	 */
	public Anonymize(OpcPackage pkg) {
		this(pkg, Mode.STRICT);
	}

	/**
	 * Whether {@link #go()} extracts the tokens of the document before and after
	 * and checks that none survived ({@link Verify}). On by default; it costs a
	 * marshal of every part twice.
	 *
	 * @since 17.2.0
	 */
	public void setVerify(boolean verify) {
		this.verify = verify;
	}

	/**
	 * Whether a workbook's numbers (cell values, formula constants, filter and
	 * input-cell values) keep their digits. Off by default: numbers are usually
	 * the confidential part of a spreadsheet, and a layout or corruption bug rarely
	 * depends on their values (CR-019 decision 3); on for the bug that does (a
	 * number format, a formula result). No effect on a docx or pptx.
	 *
	 * @since 17.2.1
	 */
	public void setKeepNumbers(boolean keepNumbers) {
		this.keepNumbers = keepNumbers;
	}

	public AnonymizeResult go() throws Docx4JException {

		result.mode = mode;

		// a workbook's vmlDrawing parts have an <xml> root the VML binding does not know: swap
		// each for a DOM part before anything reads it (once, or every reader logs the failure)
		for (Part p : new ArrayList<Part>(pkg.getParts().getParts().values())) {
			if (p instanceof org.docx4j.openpackaging.parts.VMLPart && !readable((JaxbXmlPart<?>) p)) {
				vmlAsDom(p);
			}
		}

		Verify.Extraction before = verify ? Verify.extract(pkg) : null;

		names = new Names(pkg);

		// the visitors first: the text scrambler's font selection reads the font table and
		// its embedded fonts, which STRICT is about to remove
		latinizer = new ScrambleText(pkg, names);
		markupScrubber = new MarkupScrubber(names, mode == Mode.STRICT, result.notes::add);
		if (pkg instanceof SpreadsheetMLPackage) {
			latinizer.setKeepNumbers(keepNumbers);
			spreadsheetScrubber = new SpreadsheetScrubber(latinizer, names, mode == Mode.STRICT, result.notes::add,
					markupScrubber.objectPreviews);
			// a chart's c:f in a workbook keeps pointing at its (scrambled) cells
			latinizer.setFormulas(spreadsheetScrubber.formulas()::scrub);
		}

		// the inventory (fields present, VML, objects of interest), read before the scramble;
		// it walks the WordprocessingML story parts, so a pptx has none
		if (pkg instanceof WordprocessingMLPackage) {
			detectDmlVmlContent();
		}

		// what each part is
		Map<Part, Treatment> treatments = new LinkedHashMap<Part, Treatment>();
		for (Entry<PartName, Part> entry : pkg.getParts().getParts().entrySet()) {
			treatments.put(entry.getValue(), PartsAnalyzer.classify(entry.getValue()));
		}

		// remove what goes: always-removed parts, and in STRICT mode what cannot be made clean
		removeParts(treatments);

		// metadata
		MetadataScrubber metadata = new MetadataScrubber(pkg);
		metadata.scrubDocProps();
		for (Entry<Part, Treatment> e : treatments.entrySet()) {
			if (e.getValue() == Treatment.METADATA && pkg.getParts().get(e.getKey().getPartName()) != null) {
				result.record(e.getKey(), Action.CLEARED, "descriptive properties, identities and dates cleared");
			}
		}

		// text, identities, references, embedded-object markup: one walk over every JAXB part
		walkParts(treatments);

		// relationships to outside the package
		result.externalTargetsReplaced = metadata.scrubExternalTargets();

		// media
		replaceMedia(treatments);

		result.authorsRenamed = names.authorCount();
		result.hasGreek = latinizer.hasGreek;
		result.hasCyrillic = latinizer.hasCyrillic;
		result.hasHebrew = latinizer.hasHebrew;
		result.hasArabic = latinizer.hasArabic;
		result.hasHiragana = latinizer.hasHiragana;
		result.hasKatakana = latinizer.hasKatakana;
		result.hasCJK = latinizer.hasCJK;

		if (before != null) {
			Verify.Extraction after = Verify.extract(pkg);
			result.leaks.addAll(Verify.compare(before, after));
			result.verified = result.leaks.isEmpty();
		}

		return result;
	}

	private void removeParts(Map<Part, Treatment> treatments) {

		for (Entry<Part, Treatment> e : treatments.entrySet()) {
			Part p = e.getKey();
			if (pkg.getParts().get(p.getPartName()) == null) continue; // went with an earlier removal

			if (e.getValue() == Treatment.REMOVE) {
				List<PartName> removed = MediaReplacer.removePart(pkg, p);
				result.record(p, Action.REMOVED, "always removed: " + p.getClass().getSimpleName());
				recordCascade(removed, p);

			} else if (e.getValue() == Treatment.UNSCRUBBABLE) {
				result.unsafeParts.add(p);
				if (mode == Mode.STRICT) {
					List<PartName> removed = MediaReplacer.removePart(pkg, p);
					result.record(p, Action.REMOVED, "cannot be made clean: " + p.getClass().getSimpleName());
					recordCascade(removed, p);
				} else {
					result.record(p, Action.KEPT_UNSAFE, "cannot be made clean: " + p.getClass().getSimpleName());
				}
			}
		}
	}

	private void recordCascade(List<PartName> removed, Part p) {
		for (PartName name : removed) {
			if (!name.equals(p.getPartName())) {
				result.record(name.getName(), "", Action.REMOVED, "target of " + p.getPartName().getName());
			}
		}
	}

	private void walkParts(Map<Part, Treatment> treatments) throws Docx4JException {

		final Visitor composite = new Visitor() {
			@Override
			public JaxbGraphWalker.Action visit(Object o, JAXBElement<?> wrapper) {
				JaxbGraphWalker.Action a = latinizer.visit(o, wrapper);
				JaxbGraphWalker.Action b = markupScrubber.visit(o, wrapper);
				JaxbGraphWalker.Action c = spreadsheetScrubber == null ? JaxbGraphWalker.Action.CONTINUE : spreadsheetScrubber.visit(o, wrapper);
				if (a == JaxbGraphWalker.Action.REMOVE || b == JaxbGraphWalker.Action.REMOVE || c == JaxbGraphWalker.Action.REMOVE) return JaxbGraphWalker.Action.REMOVE;
				if (a == JaxbGraphWalker.Action.REPLACE || b == JaxbGraphWalker.Action.REPLACE) return JaxbGraphWalker.Action.REPLACE;
				if (a == JaxbGraphWalker.Action.SKIP_CHILDREN || b == JaxbGraphWalker.Action.SKIP_CHILDREN || c == JaxbGraphWalker.Action.SKIP_CHILDREN) return JaxbGraphWalker.Action.SKIP_CHILDREN;
				return JaxbGraphWalker.Action.CONTINUE;
			}
			@Override
			public Object replacement() {
				return markupScrubber.replacement();
			}
		};

		for (Entry<Part, Treatment> e : treatments.entrySet()) {
			Part p = e.getKey();
			Treatment t = e.getValue();
			if (pkg.getParts().get(p.getPartName()) == null) continue; // removed
			if (t == Treatment.METADATA || t == Treatment.REPLACE_IMAGE) continue;
			if (!(p instanceof JaxbXmlPart)) {
				if (t == Treatment.SCRUB && p instanceof XmlPart) {
					// a DOM part the table names: the 2018 PowerPoint comments and authors
					scrubDom((XmlPart) p);
				} else if (t == Treatment.SAFE) {
					result.record(p, Action.KEPT, "structure");
				}
				continue;
			}
			JaxbXmlPart<?> jp = (JaxbXmlPart<?>) p;
			Object contents;
			try {
				contents = jp.getContents();
			} catch (Exception ex) {
				log.warn(p.getPartName().getName() + " could not be read: " + ex);
				contents = null;
			}
			if (contents == null) {
				result.unsafeParts.add(p);
				if (mode == Mode.STRICT) {
					MediaReplacer.removePart(pkg, p);
					result.record(p, Action.REMOVED, "could not be read, so could not be scrubbed");
				} else {
					result.record(p, Action.KEPT_UNSAFE, "could not be read, so could not be scrubbed");
				}
				continue;
			}
			log.debug("Scrubbing " + p.getPartName().getName());
			latinizer.latinText = null;
			markupScrubber.setCurrentPart(p);
			if (spreadsheetScrubber != null) spreadsheetScrubber.setCurrentPart(p);
			new JaxbGraphWalker(composite).walk(contents);
			if (contents instanceof org.xlsx4j.sml.CTConnections && ((org.xlsx4j.sml.CTConnections) contents).getConnection().isEmpty()) {
				// every connection was a data-model or Power Query one: an empty part is no use
				MediaReplacer.removePart(pkg, p);
				result.record(p, Action.REMOVED, "no connection left after the data-model and Power Query ones");
				continue;
			}
			if (t == Treatment.SCRUB) {
				result.record(p, Action.SCRUBBED, null);
			} else if (t == Treatment.SAFE) {
				result.record(p, Action.KEPT, "structure (walked)");
			}
			// an UNSCRUBBABLE JAXB part kept in KEEP mode was recorded as KEPT_UNSAFE already;
			// the walk scrubbed what it recognised in it, but it is not vouched for
		}
	}

	private void scrubDom(XmlPart p) {
		org.w3c.dom.Document doc;
		try {
			doc = p.getDocument();
		} catch (Exception ex) {
			log.warn(p.getPartName().getName() + " could not be read: " + ex);
			doc = null;
		}
		if (doc == null) {
			result.unsafeParts.add(p);
			if (mode == Mode.STRICT) {
				MediaReplacer.removePart(pkg, p);
				result.record(p, Action.REMOVED, "could not be read, so could not be scrubbed");
			} else {
				result.record(p, Action.KEPT_UNSAFE, "could not be read, so could not be scrubbed");
			}
			return;
		}
		log.debug("Scrubbing (DOM) " + p.getPartName().getName());
		if (PartsAnalyzer.isVml(p)) {
			new VmlDomScrubber(latinizer).scrub(doc.getDocumentElement());
			result.record(p, Action.SCRUBBED, "VML drawing (as DOM): shape text, names and links scrambled, control formulas rewritten");
		} else {
			new ModernCommentsScrubber(names, latinizer).scrub(doc.getDocumentElement());
			result.record(p, Action.SCRUBBED, "2018 comments: authors and persons renamed, dates fixed, text scrambled");
		}
	}

	private static boolean readable(JaxbXmlPart<?> p) {
		try {
			return p.getContents() != null;
		} catch (Exception e) {
			log.warn(p.getPartName().getName() + " could not be read as JAXB: " + e);
			return false;
		}
	}

	/**
	 * The part's bytes from the package's source store, as a DOM part in the JAXB
	 * part's place (same name, content type, relationships); null if the bytes
	 * cannot be had.
	 */
	private XmlPart vmlAsDom(Part p) {
		try {
			if (pkg.getSourcePartStore() == null) return null;
			byte[] bytes;
			try (java.io.InputStream is = pkg.getSourcePartStore().loadPart(p.getPartName().getName().substring(1))) {
				if (is == null) return null;
				bytes = is.readAllBytes();
			}
			org.docx4j.openpackaging.parts.DefaultXmlPart dom = new org.docx4j.openpackaging.parts.DefaultXmlPart(p.getPartName());
			dom.setContentType(new org.docx4j.openpackaging.contenttype.ContentType(p.getContentType()));
			dom.setRelationshipType(p.getRelationshipType());
			dom.setDocument(new java.io.ByteArrayInputStream(bytes));
			dom.setPackage(pkg);
			if (p.getRelationshipsPart() != null) {
				dom.setRelationships(p.getRelationshipsPart());
				p.getRelationshipsPart().setSourceP(dom);
			}
			pkg.getParts().remove(p.getPartName());
			pkg.getParts().put(dom);
			return dom;
		} catch (Exception e) {
			log.warn(p.getPartName().getName() + " could not be read as DOM either: " + e);
			return null;
		}
	}

	private void replaceMedia(Map<Part, Treatment> treatments) throws Docx4JException {
		MediaReplacer media = new MediaReplacer(pkg, markupScrubber.objectPreviews);
		for (Entry<Part, Treatment> e : treatments.entrySet()) {
			Part p = e.getKey();
			if (e.getValue() != Treatment.REPLACE_IMAGE) continue;
			if (pkg.getParts().get(p.getPartName()) == null) continue; // went with a removed part
			result.record(p, Action.REPLACED, media.replace(p));
		}
	}

	// ---- the inventory walk (pre-17.2.0), kept for its report

	private void detectDmlVmlContent() throws InvalidFormatException {

		dmlVmlAnalyzer = new DmlVmlAnalyzer();

		WordprocessingMLPackage pkg = (WordprocessingMLPackage) this.pkg;
		detectDmlVml(pkg.getMainDocumentPart());

		for (Entry<PartName, Part> entry : new ArrayList<Entry<PartName, Part>>(pkg.getParts().getParts().entrySet())) {
			Part p = entry.getValue();
			if (p instanceof HeaderPart) {
				detectDmlVml((HeaderPart) p);
			}
			if (p instanceof FooterPart) {
				detectDmlVml((FooterPart) p);
			}
		}

		if (pkg.getMainDocumentPart().getFootnotesPart() != null) {
			detectDmlVml(pkg.getMainDocumentPart().getFootnotesPart());
		}
		if (pkg.getMainDocumentPart().getEndNotesPart() != null) {
			detectDmlVml(pkg.getMainDocumentPart().getEndNotesPart());
		}
		if (pkg.getMainDocumentPart().getCommentsPart() != null) {
			detectDmlVml(pkg.getMainDocumentPart().getCommentsPart());
		}
	}

	public void detectDmlVml(JaxbXmlPart p) {

		log.debug("Inspecting " + p.getPartName().getName());

		Object contents;
		try {
			contents = p.getContents();
		} catch (Docx4JException e) {
			log.warn(p.getPartName().getName() + " could not be read: " + e);
			return;
		}

		dmlVmlAnalyzer.reinit();
		dmlVmlAnalyzer.setPart(p);
		// CR-021: ALL - a shape in any branch must be inspected
		new TraversalUtil(contents, dmlVmlAnalyzer, org.docx4j.jaxb.McMode.ALL);

		result.unsafeObjectsByPart.put(p, dmlVmlAnalyzer.unsafeObjects);
		if (dmlVmlAnalyzer.unsafeObjects.size() > 0) {
			result.anyUnsafeObjects = true;
		}
		result.inventoryObjectsByPart.put(p, dmlVmlAnalyzer.inventoryObjects);

		if (!result.containsVML) {
			result.containsVML = dmlVmlAnalyzer.containsVML;
		}

		result.fieldsPresent.addAll(dmlVmlAnalyzer.fieldsPresent);
	}

}
