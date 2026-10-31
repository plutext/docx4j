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
import org.docx4j.wml.STFldCharType;
import org.docx4j.wml.Text;

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
 * converted alike); not the comments, whose struck runs are reported.</li>
 * <li><b>Left alone and reported</b> (D10): a run inside {@code w:ins}, {@code w:del},
 * {@code w:moveFrom} or {@code w:moveTo}, or whose strike is itself a tracked formatting change; a
 * field's runs, {@code begin} to {@code end}, and a {@code w:fldSimple}'s; math; a run struck only
 * by a style (D5).</li>
 * <li><b>Grouped</b> (D8): adjacent converted runs of one paragraph go in one {@code w:del}, as Word
 * groups its own deletion; a spelling marker between them does not part them.</li>
 * <li><b>The separating space</b> (D13; {@link ManualRedlineOptions#separatorSpace}): a struck word
 * with whitespace (or the paragraph's start) before it and a space after takes that space with it;
 * one that ends the paragraph or meets punctuation takes the space before it, so that Accept All
 * leaves one space, as Word's own deletion of a word does in the main story (CR-068 section 8.4).</li>
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
 * Word 365's Accept All and Reject All of the converted file are the oracle (CR-068 section 8, the
 * probe in docx4j-core-tests' {@code revisions/cr068}).
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

	/** The report a conversion would give, from a copy of the package; the package is not changed. */
	public static ManualRedlineReport preview(WordprocessingMLPackage pkg, ManualRedlineOptions options) {
		if (pkg == null) throw new IllegalArgumentException("No package");
		WordprocessingMLPackage copy = (WordprocessingMLPackage) pkg.clone();
		copy.setDateTimeZone(pkg.getDateTimeZone());
		return new Conversion(copy, options == null ? new ManualRedlineOptions() : options, true).run();
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

	/** A run, a marker or an equation of a paragraph's run-level content, in document order. */
	private static final class Seg {
		R run;
		Object item;
		List<Object> owner;
		String text = "";
		Ctx ctx = Ctx.PLAIN;
		boolean inField;
		boolean convertible;
		Kind kind;
		boolean converted;
		boolean marker;
		boolean proofErr;
		boolean opaque;

		boolean shown() {
			return ctx != Ctx.DELETED && !converted && !marker && !text.isEmpty();
		}
	}

	/** Adjacent converted runs of one content list, one {@code w:del}. */
	private static final class Group {
		final List<Seg> segs = new ArrayList<Seg>();
		final List<Object> owner;
		RunDel del;
		Group(Seg first) {
			segs.add(first);
			owner = first.owner;
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

	private static final class Conversion {
		final WordprocessingMLPackage pkg;
		final ManualRedlineOptions options;
		final ManualRedlineReport report;
		Map<Object, String> addresses = Collections.emptyMap();
		BigInteger nextId;
		XMLGregorianCalendar date;
		XMLGregorianCalendar dateUtc;
		/** The part roots a dated change was written in, to be given {@code w16du} in {@code mc:Ignorable}. */
		final Map<Object, Boolean> dated = new IdentityHashMap<Object, Boolean>();
		/** Whether a run style and paragraph style strike, by "rStyle|pStyle". */
		final Map<String, Boolean> styleStrike = new HashMap<String, Boolean>();

		Conversion(WordprocessingMLPackage pkg, ManualRedlineOptions options, boolean preview) {
			this.pkg = pkg;
			this.options = options;
			this.report = new ManualRedlineReport(preview);
		}

		ManualRedlineReport run() {
			try {
				addresses = Texts.addresses(pkg, TextOptions.of(TextView.ACCEPTED).notes(true).comments(true)
						.include(TextOptions.Include.HEADERS, TextOptions.Include.FOOTERS));
			} catch (RuntimeException e) {
				// the addresses are the report's; the conversion goes on without them
			}
			List<Story> stories = new Lister(pkg, false).stories();
			nextId = maxId(stories).add(BigInteger.ONE);
			dates();
			for (Story story : stories) {
				new Walk(story.part, story.rootOwner instanceof Comments.Comment, false, null).blocks(story.root);
			}
			for (Object root : dated.keySet()) McSplice.addIgnorable(root, Collections.singleton("w16du"));
			return report;
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
			Instant at = options.isDateSet() ? options.getDate() : Instant.now();
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

		/** Whether a run style and a paragraph style, with the document defaults, strike (D5's style strike). */
		private boolean styleStruck(RPr own, P p) {
			String rStyle = own != null && own.getRStyle() != null ? own.getRStyle().getVal() : null;
			String pStyle = p.getPPr() != null && p.getPPr().getPStyle() != null ? p.getPPr().getPStyle().getVal() : null;
			String key = rStyle + "|" + pStyle;
			Boolean known = styleStrike.get(key);
			if (known != null) return known;
			boolean struck = false;
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
				struck = effective != null && (on(effective.getStrike()) || on(effective.getDstrike()));
			} catch (Exception e) {
				struck = false;
			}
			styleStrike.put(key, struck);
			return struck;
		}

		/** One story, or one text box in it: its paragraphs in document order, a field's depth carried from one to the next. */
		private final class Walk {
			final JaxbXmlPart<?> part;
			/** The comments story: struck runs reported, nothing converted (D2). */
			final boolean comments;
			/** A text box's second branch: converted as the first, not reported again. */
			final boolean twin;
			/** The address of the paragraph a text box is in, for a box paragraph the text model gives none. */
			final String outer;
			int fieldDepth;
			P paragraph;

			Walk(JaxbXmlPart<?> part, boolean comments, boolean twin, String outer) {
				this.part = part;
				this.comments = comments;
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
					else new Walk(part, comments, true, outer).blocks(items);
				}
			}

			private void paragraph(P p, List<Object> container) {
				P outerParagraph = paragraph;
				paragraph = p;
				String address = addressOf(p);
				List<Seg> segs = new ArrayList<Seg>();
				List<Box> boxes = new ArrayList<Box>();
				collect(p.getContent(), Ctx.PLAIN, segs, boxes, address);
				if (!comments) {
					List<Group> groups = groups(segs);
					for (Group g : groups) convert(g, address);
					if (options.isSeparatorSpace()) for (Group g : groups) separator(g, segs, address);
					mark(p, container, segs, groups, address);
				}
				paragraph = outerParagraph;
				for (Box box : boxes) new Walk(part, comments, twin || box.twin, address).blocks(box.content.getContent());
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
						reportStruck(v, Reason.MATH, address);
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
						Walk twinWalk = new Walk(part, comments, true, address);
						twinWalk.paragraph = paragraph;
						List<Seg> twinSegs = new ArrayList<Seg>();
						List<Box> twinBoxes = new ArrayList<Box>();
						twinWalk.collect(items, ctx, twinSegs, twinBoxes, address);
						if (!comments) for (Group g : twinWalk.groups(twinSegs)) twinWalk.convert(g, address);
						for (Box box : twinBoxes) new Walk(part, comments, true, address).blocks(box.content.getContent());
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

				RPr rPr = r.getRPr();
				BooleanDefaultTrue strike = rPr == null ? null : rPr.getStrike();
				BooleanDefaultTrue dstrike = rPr == null ? null : rPr.getDstrike();
				if (on(strike) || on(dstrike)) {
					Reason why = comments ? Reason.COMMENT
							: ctx == Ctx.INSERTED || ctx == Ctx.DELETED ? Reason.IN_TRACKED_CHANGE
							: ctx == Ctx.SIMPLE_FIELD || s.inField ? Reason.FIELD
							: rPr.getRPrChange() != null ? Reason.IN_TRACKED_CHANGE : null;
					if (why != null) {
						left(why, address, s.text);
					} else if (hasContent(r)) {
						s.convertible = true;
						s.kind = on(strike) ? Kind.STRIKE : Kind.DOUBLE_STRIKE;
					}
				} else if (!comments && strike == null && dstrike == null && ctx != Ctx.DELETED && !s.text.isEmpty()
						&& styleStruck(rPr, paragraph)) {
					left(Reason.STYLE_STRIKE, address, s.text);
				}
			}

			/** Adjacent convertible runs of one content list, a spelling marker between them no matter (D8). */
			private List<Group> groups(List<Seg> segs) {
				List<Group> out = new ArrayList<Group>();
				Group current = null;
				for (Seg s : segs) {
					if (s.convertible) {
						if (current != null && current.owner == s.owner && adjacent(current.last(), s)) current.segs.add(s);
						else out.add(current = new Group(s));
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

			/** A group's runs into one {@code w:del}: their strike removed (D6), their text become deleted text. */
			private void convert(Group g, String address) {
				List<Object> owner = g.owner;
				int from = Trees.indexOf(owner, g.first().item);
				int to = Trees.indexOf(owner, g.last().item);
				if (from < 0 || to < from) return;
				Object parent = Trees.parentOf(g.first().item);
				List<Object> moved = new ArrayList<Object>(owner.subList(from, to + 1));
				owner.subList(from, to + 1).clear();
				RunDel del = stamp(new RunDel(), part);
				del.getCustomXmlOrSmartTagOrSdt().addAll(moved);
				Trees.setParents(moved, del);
				del.setParent(parent);
				owner.add(from, del);
				g.del = del;
				boolean dstrikeOnly = true;
				for (Seg s : g.segs) {
					unstrike(s.run);
					s.converted = true;
					if (s.kind != Kind.DOUBLE_STRIKE) dstrikeOnly = false;
				}
				Resolver.deleteText(moved);
				if (!twin) report.add(new ManualRedlineReport.Change(dstrikeOnly ? Kind.DOUBLE_STRIKE : Kind.STRIKE, partName(),
						address, g.text(), g.segs.size(), true));
			}

			/** D13: the space that separated a struck word from its neighbours, into the deletion. */
			private void separator(Group g, List<Seg> segs, String address) {
				String text = g.text();
				if (text.isEmpty() || Character.isWhitespace(text.charAt(0)) || Character.isWhitespace(text.charAt(text.length() - 1))) return;
				Seg prev = neighbour(segs, segs.indexOf(g.first()) - 1, -1);
				Seg next = neighbour(segs, segs.indexOf(g.last()) + 1, 1);
				char before = prev == null ? 0 : prev.text.charAt(prev.text.length() - 1);
				char after = next == null ? 0 : next.text.charAt(0);
				if ((prev == null || Character.isWhitespace(before)) && next != null && after == ' ' && plain(next)) {
					takeFirst(g, next, address);
				} else if ((next == null || PUNCTUATION.indexOf(after) >= 0) && prev != null && before == ' ' && plain(prev)) {
					takeLast(g, prev, address);
				}
			}

			/** The nearest segment shown with text, from {@code i} in direction {@code step}; null at the paragraph's edge. */
			private Seg neighbour(List<Seg> segs, int i, int step) {
				for (; i >= 0 && i < segs.size(); i += step) if (segs.get(i).shown()) return segs.get(i);
				return null;
			}

			/** A run a space can be taken from: plain text, not tracked, not a field's. */
			private boolean plain(Seg s) {
				return s.run != null && s.ctx == Ctx.PLAIN && !s.inField && !s.opaque;
			}

			/** The leading space of the run after a deletion, into it (or a deletion of its own where the run is not beside it). */
			private void takeFirst(Group g, Seg next, String address) {
				List<Object> content = next.run.getContent();
				int at = -1;
				for (int i = 0; i < content.size() && at < 0; i++) if (Trees.itemText(content.get(i)) != null) at = i;
				if (at < 0 || !isT(content.get(at))) return;
				Text t = (Text) Trees.unwrap(content.get(at));
				if (t.getValue() == null || !t.getValue().startsWith(" ")) return;
				trim(content, at, t, t.getValue().substring(1));
				next.text = next.text.substring(1);
				R space = spaceRun(next.run);
				int runAt = Trees.indexOf(next.owner, next.item);
				boolean beside = next.owner == g.owner && runAt == Trees.indexOf(g.owner, g.del) + 1;
				if (beside) {
					g.del.getCustomXmlOrSmartTagOrSdt().add(space);
					space.setParent(g.del);
				} else {
					next.owner.add(runAt, ownDeletion(space, Trees.parentOf(next.item)));
				}
				if (isEmptyRun(next.run)) removeItem(next);
				if (!twin) report.add(new ManualRedlineReport.Change(Kind.SEPARATOR_SPACE, partName(), address, " ", 1, !beside));
			}

			/** The trailing space of the run before a deletion, into it (or a deletion of its own where the run is not beside it). */
			private void takeLast(Group g, Seg prev, String address) {
				List<Object> content = prev.run.getContent();
				int at = -1;
				for (int i = content.size() - 1; i >= 0 && at < 0; i--) if (Trees.itemText(content.get(i)) != null) at = i;
				if (at < 0 || !isT(content.get(at))) return;
				Text t = (Text) Trees.unwrap(content.get(at));
				String v = t.getValue();
				if (v == null || !v.endsWith(" ")) return;
				trim(content, at, t, v.substring(0, v.length() - 1));
				prev.text = prev.text.substring(0, prev.text.length() - 1);
				R space = spaceRun(prev.run);
				int runAt = Trees.indexOf(prev.owner, prev.item);
				boolean beside = prev.owner == g.owner && runAt == Trees.indexOf(g.owner, g.del) - 1;
				if (beside) {
					g.del.getCustomXmlOrSmartTagOrSdt().add(0, space);
					space.setParent(g.del);
				} else {
					prev.owner.add(runAt + 1, ownDeletion(space, Trees.parentOf(prev.item)));
				}
				if (isEmptyRun(prev.run)) removeItem(prev);
				if (!twin) report.add(new ManualRedlineReport.Change(Kind.SEPARATOR_SPACE, partName(), address, " ", 1, !beside));
			}

			private RunDel ownDeletion(R space, Object parent) {
				RunDel del = stamp(new RunDel(), part);
				del.getCustomXmlOrSmartTagOrSdt().add(space);
				space.setParent(del);
				del.setParent(parent);
				return del;
			}

			/** D9 as revisited: a struck mark whose paragraph converted whole, deleted too where Word would join the paragraph to the next. */
			private void mark(P p, List<Object> container, List<Seg> segs, List<Group> groups, String address) {
				PPr pPr = p.getPPr();
				ParaRPr mark = pPr == null ? null : pPr.getRPr();
				if (mark == null || !(on(mark.getStrike()) || on(mark.getDstrike())) || mark.getIns() != null || mark.getDel() != null) return;
				if (groups.isEmpty()) return;
				for (Seg s : segs) if (s.shown()) return;
				int at = Trees.indexOf(container, p);
				boolean joinable = pPr.getSectPr() == null && at >= 0 && at + 1 < container.size()
						&& Trees.unwrap(container.get(at + 1)) instanceof P;
				if (!joinable) {
					left(Reason.PARAGRAPH_MARK, address, "");
					return;
				}
				CTTrackChange del = stamp(new CTTrackChange(), part);
				del.setParent(mark);
				mark.setDel(del);
				mark.setStrike(null);
				mark.setDstrike(null);
				if (!twin) report.add(new ManualRedlineReport.Change(Kind.PARAGRAPH_MARK, partName(), address, "", 0, true));
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

	/** A run's text, Office JS's way: its text, deleted text, tabs and breaks. */
	private static String runText(R r) {
		StringBuilder sb = new StringBuilder();
		for (Object o : r.getContent()) {
			String s = Trees.itemText(o);
			if (s != null) sb.append(s);
		}
		return sb.toString();
	}

	/** Whether a run holds anything a deletion is about: not only a comment reference, a note's own mark or a rendering hint. */
	private static boolean hasContent(R r) {
		for (Object o : r.getContent()) if (!NOT_CONTENT.contains(Trees.name(o))) return true;
		return false;
	}

	private static boolean isT(Object item) {
		return Trees.unwrap(item) instanceof Text && "t".equals(Trees.name(item));
	}

	private static void unstrike(R r) {
		RPr rPr = r.getRPr();
		if (rPr == null) return;
		rPr.setStrike(null);
		rPr.setDstrike(null);
		if (Trees.isEmpty(rPr)) r.setRPr(null);
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

	/** A run holding one deleted space, formatted as the run it came from. */
	private static R spaceRun(R from) {
		R space = new R();
		if (from.getRPr() != null) {
			RPr rPr = XmlUtils.deepCopy(from.getRPr());
			rPr.setRPrChange(null);
			space.setRPr(rPr);
			rPr.setParent(space);
		}
		DelText d = new DelText();
		d.setValue(" ");
		d.setSpace("preserve");
		d.setParent(space);
		space.getContent().add(d);
		return space;
	}

	private static boolean isEmptyRun(R r) {
		return r.getContent().isEmpty();
	}

	private static void removeItem(Seg s) {
		int i = Trees.indexOf(s.owner, s.item);
		if (i >= 0) s.owner.remove(i);
	}
}
