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

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import jakarta.xml.bind.JAXBElement;

import org.docx4j.math.CTCtrlPr;
import org.docx4j.math.CTR;
import org.docx4j.mce.AlternateContent;
import org.docx4j.openpackaging.packages.WordprocessingMLPackage;
import org.docx4j.openpackaging.parts.Part;
import org.docx4j.openpackaging.parts.WordprocessingML.BinaryPart;
import org.docx4j.openpackaging.parts.WordprocessingML.FooterPart;
import org.docx4j.openpackaging.parts.WordprocessingML.HeaderPart;
import org.docx4j.openpackaging.parts.WordprocessingML.MainDocumentPart;
import org.docx4j.openpackaging.parts.relationships.RelationshipsPart;
import org.docx4j.wml.BooleanDefaultTrue;
import org.docx4j.wml.CTBookmark;
import org.docx4j.wml.CTFtnEdn;
import org.docx4j.wml.CTFtnEdnRef;
import org.docx4j.wml.CTMathRunTrackChange;
import org.docx4j.wml.CTRel;
import org.docx4j.wml.CTTrackChange;
import org.docx4j.wml.CTTxbxContent;
import org.docx4j.wml.CommentRangeEnd;
import org.docx4j.wml.CommentRangeStart;
import org.docx4j.wml.Comments;
import org.docx4j.wml.P;
import org.docx4j.wml.PPr;
import org.docx4j.wml.ParaRPr;
import org.docx4j.wml.R;
import org.docx4j.wml.RPr;
import org.docx4j.wml.STFtnEdn;
import org.docx4j.wml.SectPr;
import org.docx4j.wml.Tbl;
import org.docx4j.wml.Tc;
import org.docx4j.wml.Tr;

/**
 * A document described for comparison with Word's saves (CR-043 section 3.5): each story's
 * paragraphs - style, alignment, indent, list, the mark's revision - with their text in runs of one
 * formatting and one revision (adjacent runs merged, since Word resplits runs on save); tables,
 * notes, comments, text boxes, equations, bookmarks, and each section's effective headers and
 * footers. What Word's save normalises does not show: ids, dates, rsids, field codes (a field is
 * its result, so an accepted HYPERLINK field and Word's {@code w:hyperlink} read alike), and
 * whether a section's header reference is explicit or inherited.
 */
public final class DocumentDescription {

	private final StringBuilder out = new StringBuilder();
	/** Whether table, row, cell and section property records ({@code *PrChange}) show: CR-043 phase 2 resolves them. */
	private final boolean records;

	private DocumentDescription(boolean records) {
		this.records = records;
	}

	public static String of(WordprocessingMLPackage pkg) {
		return of(pkg, false);
	}

	public static String of(WordprocessingMLPackage pkg, boolean records) {
		DocumentDescription d = new DocumentDescription(records);
		d.describe(pkg);
		return d.out.toString();
	}

	private void describe(WordprocessingMLPackage pkg) {
		MainDocumentPart main = pkg.getMainDocumentPart();
		line(0, "== main");
		blocks(main.getJaxbElement().getBody().getContent(), 0);
		if (main.getFootnotesPart() != null && hasNotes(main.getFootnotesPart().getJaxbElement().getFootnote())) {
			line(0, "== footnotes");
			for (CTFtnEdn note : main.getFootnotesPart().getJaxbElement().getFootnote()) {
				if (note.getType() != null && note.getType() != STFtnEdn.NORMAL) continue;
				line(1, "-- note");
				blocks(note.getContent(), 1);
			}
		}
		if (main.getEndNotesPart() != null && hasNotes(main.getEndNotesPart().getJaxbElement().getEndnote())) {
			line(0, "== endnotes");
			for (CTFtnEdn note : main.getEndNotesPart().getJaxbElement().getEndnote()) {
				if (note.getType() != null && note.getType() != STFtnEdn.NORMAL) continue;
				line(1, "-- note");
				blocks(note.getContent(), 1);
			}
		}
		if (main.getCommentsPart() != null) {
			line(0, "== comments");
			// Word writes the comments in the order of their anchors: compared as a sorted set
			List<String> comments = new ArrayList<String>();
			for (Comments.Comment c : main.getCommentsPart().getJaxbElement().getComment()) {
				DocumentDescription inner = new DocumentDescription(records);
				inner.numIds = numIds;
				inner.line(1, "-- comment by " + c.getAuthor());
				inner.blocks(c.getContent(), 1);
				comments.add(inner.out.toString());
			}
			java.util.Collections.sort(comments);
			for (String c : comments) out.append(c);
		}
		headersAndFooters(pkg, main);
		int images = 0;
		for (Part part : pkg.getParts().getParts().values()) {
			if (part instanceof BinaryPart && part.getContentType() != null && part.getContentType().startsWith("image/")) images++;
		}
		line(0, "== image parts: " + images);
		// a dropped section's headers and footers go with it (C06, D7); the empty ones Word's save adds do not count
		int headers = 0, footers = 0;
		for (Part part : pkg.getParts().getParts().values()) {
			if (part instanceof HeaderPart && !empty(((HeaderPart) part).getJaxbElement().getContent())) headers++;
			if (part instanceof FooterPart && !empty(((FooterPart) part).getJaxbElement().getContent())) footers++;
		}
		line(0, "== header parts: " + headers + ", footer parts: " + footers);
	}

	private static boolean hasNotes(List<CTFtnEdn> notes) {
		for (CTFtnEdn note : notes) if (note.getType() == null || note.getType() == STFtnEdn.NORMAL) return true;
		return false;
	}

	/** True when a header or footer says nothing: Word's save writes empty ones for every section of a document with none. */
	private static boolean empty(List<Object> items) {
		for (Object o : items) {
			Object v = unwrap(o);
			if (!(v instanceof P)) return false;
			if (!((P) v).getContent().isEmpty()) {
				List<Seg> segs = new ArrayList<Seg>();
				new DocumentDescription(false).runLevel(((P) v).getContent(), "", segs);
				if (!segs.isEmpty()) return false;
			}
		}
		return true;
	}

	/** Each section's effective header and footer of each type, inherited references resolved as Word does. */
	private void headersAndFooters(WordprocessingMLPackage pkg, MainDocumentPart main) {
		List<SectPr> sections = new ArrayList<SectPr>();
		for (Object o : main.getJaxbElement().getBody().getContent()) {
			Object v = unwrap(o);
			if (v instanceof P && ((P) v).getPPr() != null && ((P) v).getPPr().getSectPr() != null) sections.add(((P) v).getPPr().getSectPr());
		}
		if (main.getJaxbElement().getBody().getSectPr() != null) sections.add(main.getJaxbElement().getBody().getSectPr());
		RelationshipsPart rels = main.getRelationshipsPart();
		Map<String, Part> effective = new LinkedHashMap<String, Part>();
		int n = 0;
		for (SectPr sectPr : sections) {
			n++;
			for (CTRel ref : sectPr.getEGHdrFtrReferences()) {
				String kind = (ref instanceof org.docx4j.wml.HeaderReference ? "header " : "footer ")
						+ (ref instanceof org.docx4j.wml.HeaderReference ? ((org.docx4j.wml.HeaderReference) ref).getType()
								: ((org.docx4j.wml.FooterReference) ref).getType());
				effective.put(kind, rels.getPart(ref.getId()));
			}
			if (records && sectPr.getSectPrChange() != null) line(0, "== section " + n + " sectPrChange");
			for (Map.Entry<String, Part> e : effective.entrySet()) {
				Part part = e.getValue();
				List<Object> content = part instanceof HeaderPart ? ((HeaderPart) part).getJaxbElement().getContent()
						: part instanceof FooterPart ? ((FooterPart) part).getJaxbElement().getContent() : new ArrayList<Object>();
				if (empty(content)) continue;
				line(0, "== section " + n + " " + e.getKey().toLowerCase());
				blocks(content, 1);
			}
		}
	}

	private void line(int depth, String s) {
		for (int i = 0; i < depth; i++) out.append("  ");
		out.append(s).append('\n');
	}

	private static Object unwrap(Object o) {
		return o instanceof JAXBElement ? ((JAXBElement<?>) o).getValue() : o;
	}

	private static String name(Object o) {
		return Trees.name(o);
	}

	// --- blocks --------------------------------------------------------------------------------

	private void blocks(List<Object> items, int depth) {
		for (Object o : items) {
			Object v = unwrap(o);
			if (v instanceof P) paragraph((P) v, depth);
			else if (v instanceof Tbl) table((Tbl) v, depth);
			else if (v instanceof org.docx4j.wml.SdtBlock) {
				line(depth, "SDT");
				blocks(((org.docx4j.wml.SdtBlock) v).getSdtContent().getContent(), depth + 1);
			} else if (Trees.revisionOf(o) != null || name(o).endsWith("RangeStart") || name(o).endsWith("RangeEnd")
					|| "bookmarkStart".equals(name(o)) || "bookmarkEnd".equals(name(o))) {
				List<Seg> segs = new ArrayList<Seg>();
				runLevel(java.util.Collections.singletonList(o), "", segs);
				line(depth, "BLOCK-LEVEL " + render(segs));
			} else if (Trees.blockItems(v) != null) {
				line(depth, v.getClass().getSimpleName());
				blocks(Trees.blockItems(v), depth + 1);
			}
		}
	}

	private void table(Tbl tbl, int depth) {
		line(depth, "TABLE" + (records && Lister.hasTableRecords(tbl) ? " records" : ""));
		for (Lister.Row row : Lister.rowsOf(tbl)) {
			Tr tr = row.tr;
			String rev = "";
			if (tr.getTrPr() != null && tr.getTrPr().getIns() != null) rev += " ins(" + tr.getTrPr().getIns().getAuthor() + ")";
			if (tr.getTrPr() != null && tr.getTrPr().getDel() != null) rev += " del(" + tr.getTrPr().getDel().getAuthor() + ")";
			line(depth + 1, "ROW" + rev);
			for (Lister.Row cell : Lister.cellsOf(tr)) {
				Tc tc = (Tc) unwrap(cell.item);
				String span = records && tc.getTcPr() != null && tc.getTcPr().getGridSpan() != null ? " span=" + tc.getTcPr().getGridSpan().getVal() : "";
				line(depth + 2, "CELL" + span);
				blocks(tc.getContent(), depth + 3);
			}
		}
	}

	private void paragraph(P p, int depth) {
		StringBuilder props = new StringBuilder("P");
		PPr pPr = p.getPPr();
		if (pPr != null) {
			if (pPr.getPStyle() != null) props.append(" style=").append(pPr.getPStyle().getVal());
			if (pPr.getJc() != null) props.append(" jc=").append(pPr.getJc().getVal());
			if (pPr.getInd() != null) {
				org.docx4j.wml.PPrBase.Ind ind = pPr.getInd();
				props.append(" ind=").append(ind.getLeft()).append('/').append(ind.getRight()).append('/')
						.append(ind.getHanging()).append('/').append(ind.getFirstLine());
			}
			if (pPr.getNumPr() != null) {
				props.append(" num=").append(pPr.getNumPr().getNumId() == null ? null : numId(pPr.getNumPr().getNumId().getVal()))
						.append('/').append(pPr.getNumPr().getIlvl() == null ? null : pPr.getNumPr().getIlvl().getVal());
			}
			if (pPr.getPPrChange() != null) props.append(" pPrChange(").append(pPr.getPPrChange().getAuthor()).append(')');
			if (pPr.getSectPr() != null) props.append(" sect");
			ParaRPr rPr = pPr.getRPr();
			if (rPr != null) {
				mark(props, "ins", rPr.getIns());
				mark(props, "del", rPr.getDel());
				mark(props, "moveFrom", rPr.getMoveFrom());
				mark(props, "moveTo", rPr.getMoveTo());
				if (rPr.getRPrChange() != null) props.append(" mark-rPrChange(").append(rPr.getRPrChange().getAuthor()).append(')');
			}
		}
		List<Seg> segs = new ArrayList<Seg>();
		runLevel(p.getContent(), "", segs);
		line(depth, props + " | " + render(segs));
	}

	/** Word renumbers lists on save: a list is named by the order it first appears in. */
	private Map<java.math.BigInteger, Integer> numIds = new LinkedHashMap<java.math.BigInteger, Integer>();

	private String numId(java.math.BigInteger id) {
		if (id == null || id.signum() == 0) return String.valueOf(id);
		Integer n = numIds.get(id);
		if (n == null) numIds.put(id, n = numIds.size() + 1);
		return "#" + n;
	}

	private static void mark(StringBuilder props, String what, CTTrackChange change) {
		if (change != null) props.append(" mark-").append(what).append('(').append(change.getAuthor()).append(')');
	}

	// --- run level -----------------------------------------------------------------------------

	/** A piece of text (or a marker) with its formatting and the revisions around it. */
	private static final class Seg {
		String text;
		final String format;
		final String context;
		final boolean marker;
		Seg(String text, String format, String context, boolean marker) {
			this.text = text;
			this.format = format;
			this.context = context;
			this.marker = marker;
		}
	}

	private static String render(List<Seg> segs) {
		List<Seg> merged = new ArrayList<Seg>();
		for (Seg s : segs) {
			Seg last = merged.isEmpty() ? null : merged.get(merged.size() - 1);
			if (last != null && !s.marker && !last.marker && last.format.equals(s.format) && last.context.equals(s.context)) {
				last.text += s.text;
			} else {
				merged.add(new Seg(s.text, s.format, s.context, s.marker));
			}
		}
		StringBuilder out = new StringBuilder();
		for (Seg s : merged) {
			if (s.marker) out.append(s.text);
			else out.append('\u00ab').append(s.text.replace("\t", "\\t").replace("\n", "\\n")).append('\u00bb').append(s.format).append(s.context.isEmpty() ? "" : "[" + s.context + "]");
		}
		return out.toString();
	}

	private void runLevel(List<Object> items, String context, List<Seg> out) {
		for (Object o : items) {
			Object v = unwrap(o);
			String n = name(o);
			Trees.Revision rev = Trees.revisionOf(o);
			if (rev != null) {
				CTTrackChange tc = (CTTrackChange) v;
				String c = (context.isEmpty() ? "" : context + ">") + rev.name().toLowerCase() + "(" + tc.getAuthor() + ")";
				runLevel(Trees.runItems(v), c, out);
				continue;
			}
			if (v instanceof R) {
				run((R) v, context, out);
				continue;
			}
			if (v instanceof CommentRangeStart) { out.add(new Seg("\u2045c", "", "", true)); continue; }
			if (v instanceof CommentRangeEnd) { out.add(new Seg("c\u2046", "", "", true)); continue; }
			if ("bookmarkStart".equals(n)) {
				String bn = ((CTBookmark) v).getName();
				if (bn != null && !bn.startsWith("_")) out.add(new Seg("\u2045b:" + bn, "", "", true));
				continue;
			}
			if ("bookmarkEnd".equals(n)) continue; // ids only: a start without its end shows as the start alone
			if (n.startsWith("moveFromRange") || n.startsWith("moveToRange") || n.startsWith("customXml")) {
				out.add(new Seg("\u2045" + n + "\u2046", "", "", true));
				continue;
			}
			if (v instanceof org.docx4j.wml.SdtRun) {
				out.add(new Seg("\u2045sdt", "", "", true));
				runLevel(((org.docx4j.wml.SdtRun) v).getSdtContent().getContent(), context, out);
				out.add(new Seg("sdt\u2046", "", "", true));
				continue;
			}
			if (Lister.isMath(v)) {
				out.add(new Seg("\u2045math:" + math(v, context) + "\u2046", "", "", true));
				continue;
			}
			List<Object> nested = Trees.runItems(v);
			if (nested != null) runLevel(nested, context, out);
		}
	}

	private void run(R r, String context, List<Seg> out) {
		String format = format(r.getRPr());
		for (Object o : r.getContent()) {
			Object v = unwrap(o);
			String n = name(o);
			String t = Trees.itemText(o);
			if (t != null) {
				out.add(new Seg(t, format, context, false));
			} else if (v instanceof CTFtnEdnRef) {
				out.add(new Seg("\u2045" + ("footnoteReference".equals(n) ? "fn" : "en") + "\u2046", "", context, true));
			} else if (v instanceof R.CommentReference) {
				out.add(new Seg("\u2045cref\u2046", "", context, true));
			} else if (v instanceof org.docx4j.wml.Drawing || v instanceof org.docx4j.wml.Pict || v instanceof AlternateContent
					|| v instanceof org.docx4j.wml.CTObject) {
				out.add(new Seg("\u2045" + drawing(v) + "\u2046", "", context, true));
			}
		}
	}

	/** A drawing: a picture, or the text boxes in it (every branch's). */
	private String drawing(Object v) {
		StringBuilder s = new StringBuilder();
		List<CTTxbxContent> boxes = new ArrayList<CTTxbxContent>();
		boolean[] picture = { false };
		Trees.walk(v, new Trees.Visitor() {
			@Override boolean enter(Object value, Object item) {
				if (value instanceof CTTxbxContent) { boxes.add((CTTxbxContent) value); return false; }
				if (value instanceof org.docx4j.dml.CTBlip || value instanceof org.docx4j.vml.CTImageData) picture[0] = true;
				return true;
			}
		});
		if (boxes.isEmpty()) return picture[0] ? "img" : "drawing";
		for (CTTxbxContent box : boxes) {
			DocumentDescription inner = new DocumentDescription(records);
			inner.blocks(box.getContent(), 0);
			s.append("txbx{").append(inner.out.toString().trim().replace("\n", " / ")).append('}');
		}
		return s.toString();
	}

	/** An equation: its text, structures by name, and the revisions in it. */
	private String math(Object root, String context) {
		StringBuilder s = new StringBuilder();
		Trees.walk(root, new Trees.Visitor() {
			@Override boolean enter(Object value, Object item) {
				if (value instanceof CTR) {
					for (Object c : ((CTR) value).getContent()) {
						Object cv = unwrap(c);
						if (cv instanceof CTMathRunTrackChange) {
							s.append('[').append(name(c)).append('(').append(((CTMathRunTrackChange) cv).getAuthor()).append("):");
							for (Object x : ((CTMathRunTrackChange) cv).getRest()) {
								String t = Trees.itemText(x);
								if (t != null) s.append(t);
							}
							s.append(']');
						} else {
							String t = Trees.itemText(c);
							if (t != null) s.append(t);
						}
					}
					return false;
				}
				if (value instanceof CTCtrlPr) {
					CTCtrlPr cp = (CTCtrlPr) value;
					if (cp.getIns() != null) s.append("[struct-ins]");
					if (cp.getDel() != null) s.append("[struct-del]");
					return false;
				}
				if (item instanceof JAXBElement && value.getClass().getPackage() == CTR.class.getPackage()) {
					String n = ((JAXBElement<?>) item).getName().getLocalPart();
					if (!n.equals("r") && !n.equals("oMath") && !n.endsWith("Pr")) s.append(n).append(':');
				}
				return true;
			}
		});
		return s.toString();
	}

	private static String format(RPr rPr) {
		if (rPr == null) return "";
		StringBuilder s = new StringBuilder();
		if (rPr.getRStyle() != null) s.append(" rStyle=").append(rPr.getRStyle().getVal());
		on(s, "b", rPr.getB());
		on(s, "i", rPr.getI());
		on(s, "strike", rPr.getStrike());
		on(s, "dstrike", rPr.getDstrike());
		on(s, "caps", rPr.getCaps());
		on(s, "smallCaps", rPr.getSmallCaps());
		on(s, "vanish", rPr.getVanish());
		if (rPr.getU() != null && rPr.getU().getVal() != null) s.append(" u=").append(rPr.getU().getVal().value());
		if (rPr.getColor() != null) s.append(" color=").append(rPr.getColor().getVal());
		if (rPr.getSz() != null) s.append(" sz=").append(rPr.getSz().getVal());
		if (rPr.getHighlight() != null) s.append(" highlight=").append(rPr.getHighlight().getVal());
		if (rPr.getVertAlign() != null) s.append(" vertAlign=").append(rPr.getVertAlign().getVal());
		if (rPr.getRPrChange() != null) s.append(" rPrChange(").append(rPr.getRPrChange().getAuthor()).append(')');
		return s.length() == 0 ? "" : "{" + s.toString().trim() + "}";
	}

	private static void on(StringBuilder s, String name, BooleanDefaultTrue b) {
		if (b != null && b.isVal()) s.append(' ').append(name);
	}
}
