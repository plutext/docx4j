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
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.IdentityHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.xml.datatype.DatatypeConfigurationException;
import javax.xml.datatype.DatatypeConstants;
import javax.xml.datatype.DatatypeFactory;
import javax.xml.datatype.XMLGregorianCalendar;

import org.docx4j.XmlUtils;
import org.docx4j.jaxb.McPurpose;
import org.docx4j.jaxb.McSelection;
import org.docx4j.jaxb.McSplice;
import org.docx4j.mce.AlternateContent;
import org.docx4j.model.PropertyResolver;
import org.docx4j.model.revisions.ManualRedlineReport.Kind;
import org.docx4j.model.revisions.ManualRedlineReport.Reason;
import org.docx4j.model.revisions.ManualRedlineReport.Type;
import org.docx4j.model.text.TextOptions;
import org.docx4j.model.text.TextView;
import org.docx4j.model.text.Texts;
import org.docx4j.openpackaging.packages.WordprocessingMLPackage;
import org.docx4j.openpackaging.parts.JaxbXmlPart;
import org.docx4j.wml.BooleanDefaultTrue;
import org.docx4j.wml.CTMarkup;
import org.docx4j.wml.CTSimpleField;
import org.docx4j.wml.CTTrackChange;
import org.docx4j.wml.CTTxbxContent;
import org.docx4j.wml.CommentRangeEnd;
import org.docx4j.wml.CommentRangeStart;
import org.docx4j.wml.Comments;
import org.docx4j.wml.DelText;
import org.docx4j.wml.FldChar;
import org.docx4j.wml.P;
import org.docx4j.wml.PPr;
import org.docx4j.wml.PPrBase;
import org.docx4j.wml.ParaRPr;
import org.docx4j.wml.ProofErr;
import org.docx4j.wml.R;
import org.docx4j.wml.RPr;
import org.docx4j.wml.RStyle;
import org.docx4j.wml.RunDel;
import org.docx4j.wml.RunIns;
import org.docx4j.wml.STFldCharType;
import org.docx4j.wml.Text;
import org.docx4j.wml.UnderlineEnumeration;

/**
 * Manual redline to tracked changes (CR-068): an explicit, opt-in conversion, never a view. A manual
 * redline is a document whose author struck the old words through by hand instead of tracking the
 * change; the file holds formatting, not revisions, so Word's Accept All and Reject All leave the
 * struck text where it is. {@link #toTrackedChanges} turns every run struck by its own run properties
 * ({@code w:strike} or {@code w:dstrike}) into a tracked deletion by the author {@code manual redline},
 * the strike removed (D6), so that Accept All drops the old words, Reject All gives them back as plain
 * text, and every reader of the document, the text model's three views among them, follows.
 *
 * <ul>
 * <li><b>Where</b> (D2): the main document, headers and footers, footnotes and endnotes, and in each
 * the tables, content controls, hyperlinks and text boxes (every {@code mc:AlternateContent} branch,
 * converted alike); the comments only under {@link ManualRedlineOptions#comments} (their struck runs
 * reported otherwise); a range or parts where the options name them.</li>
 * <li><b>The opt-ins</b> ({@link ManualRedlineOptions}, CR-068 section 3.3): strike from a style; a
 * run's own underline, or a colour the caller names, as a tracked insertion, the mark removed (D7);
 * a run both struck and underlined is a deletion. In a comment every converted run keeps its mark
 * (D14).</li>
 * <li><b>Left alone and reported</b> (D10): a run inside {@code w:ins}, {@code w:del},
 * {@code w:moveFrom} or {@code w:moveTo}, or whose strike is itself a tracked formatting change; a
 * field's runs, {@code begin} to {@code end}, and a {@code w:fldSimple}'s; math; a run struck only
 * by a style, without the opt-in (D5).</li>
 * <li><b>Grouped</b> (D8): adjacent converted runs of one paragraph go in one {@code w:del} (or
 * {@code w:ins}), as Word groups its own; a spelling marker between them does not part them.</li>
 * <li><b>The separating space</b> (D13; {@link ManualRedlineOptions#separatorSpace}): a struck word
 * with whitespace (or the paragraph's start) before it and a space after takes that space with it;
 * one that ends the paragraph or meets punctuation takes the space before it, so that Accept All
 * leaves one space, as Word's own deletion of a word does in the main story (CR-068 section 8.4). An
 * insertion takes its space alike, so that Reject All leaves one; the one space between a deletion
 * and the insertion replacing it is written inserted and deleted ({@code w:ins} holding
 * {@code w:del}), so that neither outcome keeps it.</li>
 * <li><b>The paragraph mark</b> (D9 as revisited): a paragraph whose mark is struck and whose every
 * run converts has its mark deleted too, so that Accept All joins it to the next paragraph, as Word's
 * own deletion of the paragraph does; not a cell's last paragraph, one before a table or anything
 * else that is not a paragraph, nor one ending a section, which Word will not delete (reported).</li>
 * <li><b>Ids, author, date</b> (D3, D4): each change a fresh {@code w:id} above the document's
 * largest annotation id; the date as Word writes it, {@code w:date} the wall clock of the package's
 * zone with a {@code Z} and {@code w16du:dateUtc} the instant, {@code w16du} made ignorable.</li>
 * <li>{@code w:trackRevisions} is not touched (D12). A second conversion changes nothing.</li>
 * </ul>
 *
 * <pre>
 * ManualRedlineReport r = ManualRedline.preview(pkg);          // what it would do; nothing changed
 * ManualRedlineReport r = ManualRedline.toTrackedChanges(pkg); // then TrackedChanges.acceptAll(pkg), or save
 * </pre>
 *
 * Word 365's Accept All and Reject All of the converted file are the oracle (CR-068 sections 8, 11
 * and 12, the probe in docx4j-core-tests' {@code revisions/cr068}).
 *
 * @since 17.3.5
 */
public final class ManualRedline {

	private ManualRedline() {}

	/** Converts with the default options. */
	public static ManualRedlineReport toTrackedChanges(WordprocessingMLPackage pkg) {
		return toTrackedChanges(pkg, null);
	}

	/** Converts the package in place; returns what was converted and what was left. */
	public static ManualRedlineReport toTrackedChanges(WordprocessingMLPackage pkg, ManualRedlineOptions options) {
		if (pkg == null) throw new IllegalArgumentException("No package");
		return new Conversion(pkg, options == null ? new ManualRedlineOptions() : options, false).run();
	}

	/** {@link #preview(WordprocessingMLPackage, ManualRedlineOptions)} with the default options. */
	public static ManualRedlineReport preview(WordprocessingMLPackage pkg) {
		return preview(pkg, null);
	}

	/**
	 * The report a conversion would give: the same walk, every decision made, nothing written. The
	 * package is not changed.
	 */
	public static ManualRedlineReport preview(WordprocessingMLPackage pkg, ManualRedlineOptions options) {
		if (pkg == null) throw new IllegalArgumentException("No package");
		return new Conversion(pkg, options == null ? new ManualRedlineOptions() : options, true).run();
	}

	/** Where a run stands with respect to the tracked changes and fields around it. */
	private enum Ctx {
		PLAIN,
		/** In {@code w:ins} or {@code w:moveTo}: shown, not converted. */
		INSERTED,
		/** In {@code w:del} or {@code w:moveFrom}: not shown. */
		DELETED,
		/** In a {@code w:fldSimple}: shown, not converted. */
		SIMPLE_FIELD
	}

	/** What the conversion makes of a run. */
	private enum Role { NONE, DELETE, INSERT }

	/** A run, a marker or an equation of a paragraph's run-level content, in document order. */
	private static final class Seg {
		R run;
		Object item;
		List<Object> owner;
		String text = "";
		Ctx ctx = Ctx.PLAIN;
		boolean inField;
		Role role = Role.NONE;
		Kind kind;
		/** For a style strike: which of strike (1) and double strike (2) the styles set. */
		int styleStrike;
		/** Converted: its text is deleted, or inserted (a space between a deletion and its insertion is both). */
		boolean deleted;
		boolean inserted;
		boolean marker;
		boolean proofErr;
		boolean opaque;

		/** Shown once every change is accepted. */
		boolean shownAccepted() {
			return !marker && !text.isEmpty() && ctx != Ctx.DELETED && !deleted;
		}

		/** Shown once every change is rejected. */
		boolean shownOriginal() {
			return !marker && !text.isEmpty() && ctx != Ctx.INSERTED && !inserted;
		}
	}

	/** Adjacent runs of one content list that one {@code w:del} (or {@code w:ins}) holds. */
	private static final class Group {
		final List<Seg> segs = new ArrayList<Seg>();
		final List<Object> owner;
		final boolean insertion;
		CTTrackChange wrapper;
		boolean paired;
		Group(Seg first, boolean insertion) {
			segs.add(first);
			owner = first.owner;
			this.insertion = insertion;
		}
		Seg first() {
			return segs.get(0);
		}
		Seg last() {
			return segs.get(segs.size() - 1);
		}
		String text() {
			StringBuilder sb = new StringBuilder();
			for (Seg s : segs) sb.append(s.text);
			return sb.toString();
		}
		/** The report's kind: the runs' own where they agree. */
		Kind kind() {
			Kind k = first().kind;
			for (Seg s : segs) if (s.kind != k) return insertion ? Kind.UNDERLINE : Kind.STRIKE;
			return k;
		}
	}

	/** A text box found in a run, and whether it is a twin (an {@code mc:AlternateContent} branch after the first holding boxes). */
	private static final class Box {
		final CTTxbxContent content;
		final boolean twin;
		Box(CTTxbxContent content, boolean twin) {
			this.content = content;
			this.twin = twin;
		}
	}

	/** What never makes a run worth converting on its own. */
	private static final Set<String> NOT_CONTENT = new HashSet<String>(Arrays.asList("commentReference", "annotationRef",
			"footnoteRef", "endnoteRef", "separator", "continuationSeparator", "lastRenderedPageBreak"));

	/** Punctuation a struck word meets that makes the space before it the one to take (D13). */
	private static final String PUNCTUATION = ".,;:!?)]}”’»";

	private static boolean on(BooleanDefaultTrue b) {
		return b != null && b.isVal();
	}

	/** A run's own underline: a {@code w:u} naming a pattern other than {@code none} ({@code w:u} with none inherits). */
	private static boolean underlined(RPr rPr) {
		return rPr != null && rPr.getU() != null && rPr.getU().getVal() != null && rPr.getU().getVal() != UnderlineEnumeration.NONE;
	}

	private static final class Conversion {
		final WordprocessingMLPackage pkg;
		final ManualRedlineOptions options;
		final ManualRedlineReport report;
		/** {@link ManualRedline#preview}: every decision made, nothing written. */
		final boolean dry;
		Map<Object, String> addresses = Collections.emptyMap();
		/** What the options' range holds (a control's content, a bookmark's extent); null for no range. */
		Set<Object> range;
		BigInteger nextId;
		Instant at;
		XMLGregorianCalendar date;
		XMLGregorianCalendar dateUtc;
		/** The part roots a dated change was written in, to be given {@code w16du} in {@code mc:Ignorable}. */
		final Map<Object, Boolean> dated = new IdentityHashMap<Object, Boolean>();
		/** Which strikes a run style and paragraph style set (1 strike, 2 double strike), by "rStyle|pStyle". */
		final Map<String, Integer> styleStrikes = new HashMap<String, Integer>();

		Conversion(WordprocessingMLPackage pkg, ManualRedlineOptions options, boolean dry) {
			this.pkg = pkg;
			this.options = options;
			this.dry = dry;
			this.report = new ManualRedlineReport(dry);
		}

		ManualRedlineReport run() {
			try {
				addresses = Texts.addresses(pkg, TextOptions.of(TextView.ACCEPTED).notes(true).comments(true)
						.include(TextOptions.Include.HEADERS, TextOptions.Include.FOOTERS));
			} catch (RuntimeException e) {
				// the addresses are the report's; the conversion goes on without them
			}
			range = range();
			List<Story> stories = new Lister(pkg, false).stories();
			nextId = maxId(stories).add(BigInteger.ONE);
			dates();
			for (Story story : stories) {
				if (options.getParts() != null && !options.getParts().contains(story.part)) continue;
				boolean comment = story.rootOwner instanceof Comments.Comment;
				new Walk(story.part, comment, comment && !options.isComments(), false, null).blocks(story.root);
			}
			if (!dry) {
				for (Object root : dated.keySet()) McSplice.addIgnorable(root, Collections.singleton("w16du"));
				if (options.isAuditProperty()) {
					pkg.addDocPropsCustomPart();
					pkg.getDocPropsCustomPart().setProperty("docx4j:manualRedline", (at == null ? "no date" : at.toString()) + "; " + options);
				}
			}
			return report;
		}

		/** The options' range, read before anything is converted, with {@link TrackedChangeFilter}'s semantics. */
		private Set<Object> range() {
			if (options.getWithin() == null && options.getWithinBookmark() == null) return null;
			TrackedChangeFilter filter = new TrackedChangeFilter();
			if (options.getWithin() != null) filter.within(options.getWithin());
			else filter.withinBookmark(options.getWithinBookmark());
			return filter.range(pkg);
		}

		private boolean inRange(Object v) {
			return range == null || range.contains(v);
		}

		/** The largest annotation id in the story parts: tracked changes, comments, bookmarks and the other ranges. */
		private BigInteger maxId(List<Story> stories) {
			BigInteger[] max = { BigInteger.ZERO };
			Map<Object, Boolean> roots = new IdentityHashMap<Object, Boolean>();
			for (Story story : stories) roots.put(story.part.getJaxbElement(), true);
			for (Object root : roots.keySet()) {
				Trees.walk(root, new Trees.Visitor() {
					@Override boolean enter(Object value, Object item) {
						BigInteger id = null;
						if (value instanceof CTMarkup) id = ((CTMarkup) value).getId();
						else if (value instanceof CommentRangeStart) id = ((CommentRangeStart) value).getId();
						else if (value instanceof CommentRangeEnd) id = ((CommentRangeEnd) value).getId();
						else if (value instanceof R.CommentReference) id = ((R.CommentReference) value).getId();
						if (id != null && id.compareTo(max[0]) > 0) max[0] = id;
						return true;
					}
				});
			}
			return max[0];
		}

		/** The date written (D4): the wall clock of the package's zone in {@code w:date}, which Word shows, and the instant in {@code w16du:dateUtc}. */
		private void dates() {
			at = options.isDateSet() ? options.getDate() : Instant.now();
			if (at == null) return;
			at = at.truncatedTo(ChronoUnit.SECONDS);
			DatatypeFactory factory;
			try {
				factory = DatatypeFactory.newInstance();
			} catch (DatatypeConfigurationException e) {
				throw new IllegalStateException(e);
			}
			date = calendar(factory, LocalDateTime.ofInstant(at, pkg.effectiveDateTimeZone()));
			dateUtc = calendar(factory, LocalDateTime.ofInstant(at, ZoneOffset.UTC));
		}

		private static XMLGregorianCalendar calendar(DatatypeFactory factory, LocalDateTime t) {
			return factory.newXMLGregorianCalendar(t.getYear(), t.getMonthValue(), t.getDayOfMonth(), t.getHour(), t.getMinute(),
					t.getSecond(), DatatypeConstants.FIELD_UNDEFINED, 0);
		}

		/** A tracked change's id, author and date, the next id taken. */
		private <T extends CTTrackChange> T stamp(T change, JaxbXmlPart<?> part) {
			change.setId(nextId);
			nextId = nextId.add(BigInteger.ONE);
			change.setAuthor(options.getAuthor());
			if (date != null) {
				change.setDate((XMLGregorianCalendar) date.clone());
				change.setDateUtc((XMLGregorianCalendar) dateUtc.clone());
				dated.put(part.getJaxbElement(), true);
			}
			return change;
		}

		/** Which strikes a run's character style and its paragraph's style set, with the document defaults (1 strike, 2 double strike). */
		private int styleStrike(RPr own, P p) {
			String rStyle = own != null && own.getRStyle() != null ? own.getRStyle().getVal() : null;
			String pStyle = p != null && p.getPPr() != null && p.getPPr().getPStyle() != null ? p.getPPr().getPStyle().getVal() : null;
			String key = rStyle + "|" + pStyle;
			Integer known = styleStrikes.get(key);
			if (known != null) return known;
			int struck = 0;
			try {
				PropertyResolver resolver = pkg.getMainDocumentPart().getPropertyResolver();
				RPr express = null;
				if (rStyle != null) {
					express = new RPr();
					RStyle rs = new RStyle();
					rs.setVal(rStyle);
					express.setRStyle(rs);
				}
				PPr pPr = null;
				if (pStyle != null) {
					pPr = new PPr();
					PPrBase.PStyle ps = new PPrBase.PStyle();
					ps.setVal(pStyle);
					pPr.setPStyle(ps);
				}
				RPr effective = resolver.getEffectiveRPr(express, pPr);
				if (effective != null && on(effective.getStrike())) struck |= 1;
				if (effective != null && on(effective.getDstrike())) struck |= 2;
			} catch (Exception e) {
				struck = 0;
			}
			styleStrikes.put(key, struck);
			return struck;
		}

		/** One story, or one text box in it: its paragraphs in document order, a field's depth carried from one to the next. */
		private final class Walk {
			final JaxbXmlPart<?> part;
			/** The comments story: converted runs keep their marks (D14). */
			final boolean comments;
			/** Struck runs reported, nothing converted: the comments story without the opt-in (D2). */
			final boolean reportOnly;
			/** A text box's second branch: converted as the first, not reported again. */
			final boolean twin;
			/** The address of the paragraph a text box is in, for a box paragraph the text model gives none. */
			final String outer;
			int fieldDepth;
			P paragraph;

			Walk(JaxbXmlPart<?> part, boolean comments, boolean reportOnly, boolean twin, String outer) {
				this.part = part;
				this.comments = comments;
				this.reportOnly = reportOnly;
				this.twin = twin;
				this.outer = outer;
			}

			private String partName() {
				return part.getPartName().getName();
			}

			private String addressOf(P p) {
				String a = addresses.get(p);
				return a != null ? a : outer;
			}

			private void left(Reason reason, String address, String text) {
				if (!twin) report.add(new ManualRedlineReport.Left(reason, partName(), address, text));
			}

			private void changed(Kind kind, Type type, boolean own, String address, String text, int runs) {
				if (!twin) report.add(new ManualRedlineReport.Change(kind, type, own, partName(), address, text, runs));
			}

			void blocks(List<Object> items) {
				for (Object item : new ArrayList<Object>(items)) {
					Object v = Trees.unwrap(item);
					if (v instanceof P) {
						paragraph((P) v, items);
						continue;
					}
					if (v instanceof AlternateContent) {
						alternateBlocks((AlternateContent) v);
						continue;
					}
					if (Trees.revisionOf(item) != null) {
						// run-level markup standing at block level: tracked content
						reportStruck(v, Reason.IN_TRACKED_CHANGE, null);
						continue;
					}
					List<Object> children = Trees.blockItems(v);
					if (children != null) blocks(children);
				}
			}

			/** Every branch of a block-level {@code mc:AlternateContent}: the one the text model reads as this story, the others as twins. */
			private void alternateBlocks(AlternateContent ac) {
				Object selected = McSelection.selectedBranch(McPurpose.TEXT, ac);
				for (Object branch : branches(ac)) {
					List<Object> items = McSplice.contentOf(branch);
					if (items == null) continue;
					if (branch == selected) blocks(items);
					else new Walk(part, comments, reportOnly, true, outer).blocks(items);
				}
			}

			private void paragraph(P p, List<Object> container) {
				P outerParagraph = paragraph;
				paragraph = p;
				String address = addressOf(p);
				List<Seg> segs = new ArrayList<Seg>();
				List<Box> boxes = new ArrayList<Box>();
				collect(p.getContent(), Ctx.PLAIN, segs, boxes, address);
				if (!reportOnly) {
					List<Group> groups = groups(segs);
					for (Group g : groups) convert(g, address);
					if (options.isSeparatorSpace()) separators(groups, segs, address);
					mark(p, container, segs, groups, address);
				}
				paragraph = outerParagraph;
				for (Box box : boxes) new Walk(part, comments, reportOnly, twin || box.twin, address).blocks(box.content.getContent());
			}

			private void collect(List<Object> items, Ctx ctx, List<Seg> segs, List<Box> boxes, String address) {
				if (items == null) return;
				for (Object item : items) {
					Object v = Trees.unwrap(item);
					if (v instanceof R) {
						run((R) v, item, items, ctx, segs, boxes, address);
						continue;
					}
					Trees.Revision revision = Trees.revisionOf(item);
					if (revision != null) {
						boolean shown = revision == Trees.Revision.INS || revision == Trees.Revision.MOVE_TO;
						collect(Trees.runItems(v), ctx == Ctx.DELETED || !shown ? Ctx.DELETED : Ctx.INSERTED, segs, boxes, address);
						continue;
					}
					if (v instanceof CTSimpleField) {
						collect(((CTSimpleField) v).getContent(), ctx == Ctx.PLAIN ? Ctx.SIMPLE_FIELD : ctx, segs, boxes, address);
						continue;
					}
					if (Lister.isMath(v)) {
						Seg s = new Seg();
						s.item = item;
						s.owner = items;
						s.ctx = ctx;
						s.opaque = true;
						s.text = Trees.text(item, Trees.View.ACCEPTED);
						segs.add(s);
						if (!reportOnly) reportStruck(v, Reason.MATH, address);
						continue;
					}
					if (v instanceof AlternateContent) {
						alternateRuns((AlternateContent) v, ctx, segs, boxes, address);
						continue;
					}
					List<Object> nested = Trees.runItems(v);
					if (nested != null) {
						// a hyperlink, an inline content control, a smart tag, custom XML, w:dir, w:bdo
						collect(nested, ctx, segs, boxes, address);
						continue;
					}
					Seg marker = new Seg();
					marker.item = item;
					marker.owner = items;
					marker.ctx = ctx;
					marker.marker = true;
					marker.proofErr = v instanceof ProofErr;
					segs.add(marker);
				}
			}

			/** A paragraph-level {@code mc:AlternateContent}: the branch the text model reads, in the paragraph; the others converted apart, as twins. */
			private void alternateRuns(AlternateContent ac, Ctx ctx, List<Seg> segs, List<Box> boxes, String address) {
				Object selected = McSelection.selectedBranch(McPurpose.TEXT, ac);
				for (Object branch : branches(ac)) {
					List<Object> items = McSplice.contentOf(branch);
					if (items == null) continue;
					if (branch == selected) {
						collect(items, ctx, segs, boxes, address);
					} else {
						Walk twinWalk = new Walk(part, comments, reportOnly, true, address);
						twinWalk.paragraph = paragraph;
						List<Seg> twinSegs = new ArrayList<Seg>();
						List<Box> twinBoxes = new ArrayList<Box>();
						twinWalk.collect(items, ctx, twinSegs, twinBoxes, address);
						if (!reportOnly) for (Group g : twinWalk.groups(twinSegs)) twinWalk.convert(g, address);
						for (Box box : twinBoxes) new Walk(part, comments, reportOnly, true, address).blocks(box.content.getContent());
					}
				}
			}

			private void run(R r, Object item, List<Object> owner, Ctx ctx, List<Seg> segs, List<Box> boxes, String address) {
				boolean fieldBefore = fieldDepth > 0;
				boolean fieldChar = false;
				for (Object o : r.getContent()) {
					Object ov = Trees.unwrap(o);
					if (ov instanceof FldChar) {
						fieldChar = true;
						STFldCharType type = ((FldChar) ov).getFldCharType();
						if (type == STFldCharType.BEGIN) fieldDepth++;
						else if (type == STFldCharType.END && fieldDepth > 0) fieldDepth--;
					}
				}
				Seg s = new Seg();
				s.run = r;
				s.item = item;
				s.owner = owner;
				s.ctx = ctx;
				s.inField = fieldBefore || fieldChar || fieldDepth > 0;
				s.text = runText(r);
				segs.add(s);
				findBoxes(r, boxes);
				if (!inRange(r)) return;

				RPr rPr = r.getRPr();
				BooleanDefaultTrue strike = rPr == null ? null : rPr.getStrike();
				BooleanDefaultTrue dstrike = rPr == null ? null : rPr.getDstrike();
				Reason blocked = ctx == Ctx.INSERTED || ctx == Ctx.DELETED ? Reason.IN_TRACKED_CHANGE
						: ctx == Ctx.SIMPLE_FIELD || s.inField ? Reason.FIELD
						: rPr != null && rPr.getRPrChange() != null ? Reason.IN_TRACKED_CHANGE : null;
				if (on(strike) || on(dstrike)) {
					if (reportOnly) left(Reason.COMMENT, address, s.text);
					else if (blocked != null) left(blocked, address, s.text);
					else if (hasContent(r)) {
						s.role = Role.DELETE;
						s.kind = on(strike) ? Kind.STRIKE : Kind.DOUBLE_STRIKE;
						if (options.isUnderlineAsInsertion() && underlined(rPr) && !twin) report.strikeAndUnderline();
					}
					return;
				}
				if (reportOnly) return;
				if (strike == null && dstrike == null && ctx != Ctx.DELETED && !s.text.isEmpty()) {
					int styles = styleStrike(rPr, paragraph);
					if (styles != 0) {
						if (!options.isStyleStrike()) left(Reason.STYLE_STRIKE, address, s.text);
						else if (blocked != null) left(blocked, address, s.text);
						else if (hasContent(r)) {
							s.role = Role.DELETE;
							s.kind = Kind.STYLE_STRIKE;
							s.styleStrike = styles;
						}
						return;
					}
				}
				if (blocked != null || !hasContent(r)) return;
				if (options.isUnderlineAsInsertion() && underlined(rPr)) {
					s.role = Role.INSERT;
					s.kind = Kind.UNDERLINE;
				} else if (!options.getColours().isEmpty() && rPr != null && rPr.getColor() != null && rPr.getColor().getVal() != null
						&& rPr.getColor().getThemeColor() == null && options.getColours().contains(rPr.getColor().getVal().toUpperCase(java.util.Locale.ROOT))
						&& !underlined(rPr) && rPr.getHighlight() == null && rPr.getShd() == null) {
					s.role = Role.INSERT;
					s.kind = Kind.COLOUR;
				}
			}

			/**
			 * Adjacent runs of one role in one content list, in document order, a spelling marker between
			 * them no matter (D8); a deletion and an insertion side by side are two.
			 */
			private List<Group> groups(List<Seg> segs) {
				List<Group> out = new ArrayList<Group>();
				Group current = null;
				for (Seg s : segs) {
					if (s.role != Role.NONE) {
						boolean insertion = s.role == Role.INSERT;
						if (current != null && current.insertion == insertion && current.owner == s.owner && adjacent(current.last(), s)) {
							current.segs.add(s);
						} else {
							out.add(current = new Group(s, insertion));
						}
					} else if (!s.proofErr) {
						current = null;
					}
				}
				return out;
			}

			private boolean adjacent(Seg a, Seg b) {
				int i = Trees.indexOf(a.owner, a.item);
				int j = Trees.indexOf(b.owner, b.item);
				if (i < 0 || j <= i) return false;
				for (int k = i + 1; k < j; k++) if (!(Trees.unwrap(a.owner.get(k)) instanceof ProofErr)) return false;
				return true;
			}

			/**
			 * A group's runs into one {@code w:del} (their text become deleted text) or {@code w:ins}, the
			 * mark that made them removed (D6, D7), except in a comment (D14).
			 */
			private void convert(Group g, String address) {
				for (Seg s : g.segs) {
					if (g.insertion) s.inserted = true;
					else s.deleted = true;
				}
				changed(g.kind(), g.insertion ? Type.INSERTION : Type.DELETION, true, address, g.text(), g.segs.size());
				if (dry) return;
				List<Object> owner = g.owner;
				int from = Trees.indexOf(owner, g.first().item);
				int to = Trees.indexOf(owner, g.last().item);
				if (from < 0 || to < from) return;
				Object parent = Trees.parentOf(g.first().item);
				List<Object> moved = new ArrayList<Object>(owner.subList(from, to + 1));
				owner.subList(from, to + 1).clear();
				CTTrackChange wrapper;
				List<Object> content;
				if (g.insertion) {
					RunIns ins = stamp(new RunIns(), part);
					content = ins.getCustomXmlOrSmartTagOrSdt();
					wrapper = ins;
				} else {
					RunDel del = stamp(new RunDel(), part);
					content = del.getCustomXmlOrSmartTagOrSdt();
					wrapper = del;
				}
				content.addAll(moved);
				Trees.setParents(moved, wrapper);
				wrapper.setParent(parent);
				owner.add(from, wrapper);
				g.wrapper = wrapper;
				if (!comments) for (Seg s : g.segs) unmark(s);
				if (!g.insertion) Resolver.deleteText(moved);
			}

			/** D13 for every group: first the one space between a deletion and its insertion, then each group's own. */
			private void separators(List<Group> groups, List<Seg> segs, String address) {
				for (int k = 0; k + 1 < groups.size(); k++) {
					Group a = groups.get(k);
					Group b = groups.get(k + 1);
					if (a.insertion == b.insertion || a.paired || b.paired) continue;
					Seg between = onlyBetween(segs, a, b);
					if (between != null && plain(between) && " ".equals(between.text) && onlyText(between.run)) {
						pairSpace(between, address);
						a.paired = true;
						b.paired = true;
					}
				}
				for (Group g : groups) if (!g.paired) separator(g, segs, address);
			}

			/** The one segment with text between two groups, markers and empty runs aside; null where there is none or more. */
			private Seg onlyBetween(List<Seg> segs, Group a, Group b) {
				Seg found = null;
				for (int i = segs.indexOf(a.last()) + 1; i < segs.indexOf(b.first()); i++) {
					Seg s = segs.get(i);
					if (s.marker || (s.text.isEmpty() && !s.opaque)) continue;
					if (found != null) return null;
					found = s;
				}
				return found;
			}

			/** The space between a deletion and the insertion replacing it, written inserted and deleted: gone in either outcome. */
			private void pairSpace(Seg s, String address) {
				s.deleted = true;
				s.inserted = true;
				changed(Kind.SEPARATOR_SPACE, Type.BOTH, true, address, " ", 1);
				if (dry) return;
				int at = Trees.indexOf(s.owner, s.item);
				Object parent = Trees.parentOf(s.item);
				s.owner.remove(at);
				RunIns ins = stamp(new RunIns(), part);
				RunDel del = stamp(new RunDel(), part);
				del.getCustomXmlOrSmartTagOrSdt().add(s.item);
				Trees.setParent(s.item, del);
				Resolver.deleteText(Collections.singletonList(s.item));
				ins.getCustomXmlOrSmartTagOrSdt().add(del);
				del.setParent(ins);
				ins.setParent(parent);
				s.owner.add(at, ins);
			}

			/**
			 * D13: a deletion takes the space that separated it, by the text once everything is accepted;
			 * an insertion by the text once everything is rejected, so that each outcome keeps one space.
			 */
			private void separator(Group g, List<Seg> segs, String address) {
				String text = g.text();
				if (text.isEmpty() || Character.isWhitespace(text.charAt(0)) || Character.isWhitespace(text.charAt(text.length() - 1))) return;
				boolean accepted = !g.insertion;
				Seg prev = neighbour(segs, segs.indexOf(g.first()) - 1, -1, accepted);
				Seg next = neighbour(segs, segs.indexOf(g.last()) + 1, 1, accepted);
				char before = prev == null ? 0 : prev.text.charAt(prev.text.length() - 1);
				char after = next == null ? 0 : next.text.charAt(0);
				if ((prev == null || Character.isWhitespace(before)) && next != null && after == ' ' && plain(next)) {
					take(g, next, true, address);
				} else if ((next == null || PUNCTUATION.indexOf(after) >= 0) && prev != null && before == ' ' && plain(prev)) {
					take(g, prev, false, address);
				}
			}

			/** The nearest segment with text shown in the outcome, from {@code i} in direction {@code step}; null at the paragraph's edge. */
			private Seg neighbour(List<Seg> segs, int i, int step, boolean accepted) {
				for (; i >= 0 && i < segs.size(); i += step) {
					Seg s = segs.get(i);
					if (accepted ? s.shownAccepted() : s.shownOriginal()) return s;
				}
				return null;
			}

			/** A run a space can be taken from: plain text, not tracked, not converted, not a field's. */
			private boolean plain(Seg s) {
				return s.run != null && s.ctx == Ctx.PLAIN && !s.inField && !s.opaque && !s.deleted && !s.inserted;
			}

			/**
			 * The space at the start of the run after a group ({@code first}) or the end of the run before
			 * it, into the group's change where the run is beside it, else a change of its own there.
			 */
			private void take(Group g, Seg from, boolean first, String address) {
				List<Object> content = from.run.getContent();
				int at = -1;
				if (first) {
					for (int i = 0; i < content.size() && at < 0; i++) if (Trees.itemText(content.get(i)) != null) at = i;
				} else {
					for (int i = content.size() - 1; i >= 0 && at < 0; i--) if (Trees.itemText(content.get(i)) != null) at = i;
				}
				if (at < 0 || !isT(content.get(at))) return;
				Text t = (Text) Trees.unwrap(content.get(at));
				String v = t.getValue();
				if (v == null || !(first ? v.startsWith(" ") : v.endsWith(" "))) return;
				int runAt = Trees.indexOf(from.owner, from.item);
				int edge = dry ? Trees.indexOf(g.owner, first ? g.last().item : g.first().item) : Trees.indexOf(g.owner, g.wrapper);
				boolean beside = from.owner == g.owner && runAt == edge + (first ? 1 : -1);
				from.text = first ? from.text.substring(1) : from.text.substring(0, from.text.length() - 1);
				changed(Kind.SEPARATOR_SPACE, g.insertion ? Type.INSERTION : Type.DELETION, !beside, address, " ", 1);
				if (dry) return;
				trim(content, at, t, first ? v.substring(1) : v.substring(0, v.length() - 1));
				R space = spaceRun(from.run, !g.insertion);
				if (beside) {
					List<Object> into = wrapperContent(g.wrapper);
					if (first) into.add(space);
					else into.add(0, space);
					space.setParent(g.wrapper);
				} else {
					CTTrackChange own = g.insertion ? stamp(new RunIns(), part) : stamp(new RunDel(), part);
					wrapperContent(own).add(space);
					space.setParent(own);
					own.setParent(Trees.parentOf(from.item));
					from.owner.add(first ? runAt : runAt + 1, own);
				}
				if (from.run.getContent().isEmpty()) {
					int i = Trees.indexOf(from.owner, from.item);
					if (i >= 0) from.owner.remove(i);
				}
			}

			/** D9 as revisited: a struck mark whose paragraph converted whole, deleted too where Word would join the paragraph to the next. */
			private void mark(P p, List<Object> container, List<Seg> segs, List<Group> groups, String address) {
				PPr pPr = p.getPPr();
				ParaRPr mark = pPr == null ? null : pPr.getRPr();
				if (mark == null || !(on(mark.getStrike()) || on(mark.getDstrike())) || mark.getIns() != null || mark.getDel() != null) return;
				if (groups.isEmpty() || !inRange(p)) return;
				for (Seg s : segs) if (s.shownAccepted()) return;
				int at = Trees.indexOf(container, p);
				boolean joinable = pPr.getSectPr() == null && at >= 0 && at + 1 < container.size()
						&& Trees.unwrap(container.get(at + 1)) instanceof P;
				if (!joinable) {
					left(Reason.PARAGRAPH_MARK, address, "");
					return;
				}
				changed(Kind.PARAGRAPH_MARK, Type.DELETION, true, address, "", 0);
				if (dry) return;
				CTTrackChange del = stamp(new CTTrackChange(), part);
				del.setParent(mark);
				mark.setDel(del);
				if (!comments) {
					mark.setStrike(null);
					mark.setDstrike(null);
				}
			}

			/** Every struck run under a node, reported once with the node's text. */
			private void reportStruck(Object node, Reason reason, String address) {
				boolean[] struck = { false };
				Trees.walk(node, new Trees.Visitor() {
					@Override boolean enter(Object value, Object item) {
						if (value instanceof RPr && (on(((RPr) value).getStrike()) || on(((RPr) value).getDstrike()))) struck[0] = true;
						return !struck[0];
					}
				});
				if (struck[0]) left(reason, address, Trees.text(node, Trees.View.ACCEPTED));
			}

			/** The text boxes in a run; in {@code mc:AlternateContent}, the first branch's, the other branches' as twins. */
			private void findBoxes(R run, List<Box> boxes) {
				Trees.walk(run, new Trees.Visitor() {
					@Override boolean enter(Object value, Object item) {
						if (value instanceof AlternateContent) {
							List<List<CTTxbxContent>> found = new ArrayList<List<CTTxbxContent>>();
							for (Object branch : branches((AlternateContent) value)) found.add(boxesIn(branch));
							List<CTTxbxContent> primary = null;
							for (List<CTTxbxContent> b : found) if (primary == null && !b.isEmpty()) primary = b;
							for (List<CTTxbxContent> b : found) for (CTTxbxContent box : b) boxes.add(new Box(box, b != primary));
							return false;
						}
						if (value instanceof CTTxbxContent) {
							boxes.add(new Box((CTTxbxContent) value, false));
							return false;
						}
						return true;
					}
				});
			}
		}
	}

	private static List<Object> branches(AlternateContent ac) {
		List<Object> out = new ArrayList<Object>(ac.getChoice());
		if (ac.getFallback() != null) out.add(ac.getFallback());
		return out;
	}

	private static List<CTTxbxContent> boxesIn(Object branch) {
		List<CTTxbxContent> out = new ArrayList<CTTxbxContent>();
		Trees.walk(branch, new Trees.Visitor() {
			@Override boolean enter(Object value, Object item) {
				if (value instanceof CTTxbxContent) {
					out.add((CTTxbxContent) value);
					return false;
				}
				return true;
			}
		});
		return out;
	}

	private static List<Object> wrapperContent(CTTrackChange wrapper) {
		return wrapper instanceof RunIns ? ((RunIns) wrapper).getCustomXmlOrSmartTagOrSdt() : ((RunDel) wrapper).getCustomXmlOrSmartTagOrSdt();
	}

	/** A run's text, Office JS's way: its text, deleted text, tabs and breaks. */
	private static String runText(R r) {
		StringBuilder sb = new StringBuilder();
		for (Object o : r.getContent()) {
			String s = Trees.itemText(o);
			if (s != null) sb.append(s);
		}
		return sb.toString();
	}

	/** Whether a run holds anything a change is about: not only a comment reference, a note's own mark or a rendering hint. */
	private static boolean hasContent(R r) {
		for (Object o : r.getContent()) if (!NOT_CONTENT.contains(Trees.name(o))) return true;
		return false;
	}

	/** Whether a run's text is held by one {@code w:t} alone. */
	private static boolean onlyText(R r) {
		int texts = 0;
		for (Object o : r.getContent()) {
			if (Trees.itemText(o) == null) continue;
			if (!isT(o)) return false;
			texts++;
		}
		return texts == 1;
	}

	private static boolean isT(Object item) {
		return Trees.unwrap(item) instanceof Text && "t".equals(Trees.name(item));
	}

	/**
	 * The mark that made a run a change, removed (D6, D7): the strike of a deletion, or the style's
	 * strike turned off by the run's own property; the underline or colour of an insertion.
	 */
	private static void unmark(Seg s) {
		R r = s.run;
		RPr rPr = r.getRPr();
		if (s.kind == Kind.STYLE_STRIKE) {
			if (rPr == null) {
				rPr = new RPr();
				r.setRPr(rPr);
				rPr.setParent(r);
			}
			if ((s.styleStrike & 1) != 0) rPr.setStrike(off());
			if ((s.styleStrike & 2) != 0) rPr.setDstrike(off());
			return;
		}
		if (rPr == null) return;
		if (s.kind == Kind.UNDERLINE) {
			rPr.setU(null);
		} else if (s.kind == Kind.COLOUR) {
			rPr.setColor(null);
		} else {
			rPr.setStrike(null);
			rPr.setDstrike(null);
		}
		if (Trees.isEmpty(rPr)) r.setRPr(null);
	}

	private static BooleanDefaultTrue off() {
		BooleanDefaultTrue b = new BooleanDefaultTrue();
		b.setVal(false);
		return b;
	}

	/** A {@code w:t}'s text shortened, or the {@code w:t} removed where nothing is left. */
	private static void trim(List<Object> content, int at, Text t, String rest) {
		if (rest.isEmpty()) {
			content.remove(at);
		} else {
			t.setValue(rest);
			if (Character.isWhitespace(rest.charAt(0)) || Character.isWhitespace(rest.charAt(rest.length() - 1))) t.setSpace("preserve");
		}
	}

	/** A run holding one space, deleted text or text, formatted as the run it came from. */
	private static R spaceRun(R from, boolean deleted) {
		R space = new R();
		if (from.getRPr() != null) {
			RPr rPr = XmlUtils.deepCopy(from.getRPr());
			rPr.setRPrChange(null);
			space.setRPr(rPr);
			rPr.setParent(space);
		}
		if (deleted) {
			DelText d = new DelText();
			d.setValue(" ");
			d.setSpace("preserve");
			d.setParent(space);
			space.getContent().add(d);
		} else {
			Text t = new Text();
			t.setValue(" ");
			t.setSpace("preserve");
			t.setParent(space);
			space.getContent().add(new org.docx4j.wml.ObjectFactory().createRT(t));
		}
		return space;
	}
}
