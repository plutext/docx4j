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
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import javax.xml.datatype.DatatypeConstants;
import javax.xml.datatype.XMLGregorianCalendar;

import org.docx4j.math.CTCtrlPr;
import org.docx4j.math.CTR;
import org.docx4j.openpackaging.parts.JaxbXmlPart;
import org.docx4j.wml.CTMathRunTrackChange;
import org.docx4j.wml.CTPPrChange;
import org.docx4j.wml.CTRPrChange;
import org.docx4j.wml.CTSectPrChange;
import org.docx4j.wml.CTTrackChange;
import org.docx4j.wml.P;
import org.docx4j.wml.ParaRPrChange;
import org.docx4j.wml.R;
import org.docx4j.wml.SectPr;
import org.docx4j.wml.Tbl;
import org.docx4j.wml.Tc;
import org.docx4j.wml.Tr;

/**
 * One tracked change, as Word's Review tab and Office JS's {@code Word.TrackedChange} present it
 * (CR-043): touching insertions, deletions or formatting changes by one author are one change,
 * whatever their dates; a move is one change from either half.
 *
 * <p>A view: it holds the markup it is over, and is valid until the next accept or reject on its
 * package (a paragraph join can remove the paragraph it holds), after which {@link #accept()} and
 * {@link #reject()} throw {@link IllegalStateException}. List again with
 * {@link TrackedChanges#in(org.docx4j.openpackaging.packages.WordprocessingMLPackage)}.</p>
 *
 * @since 17.3.3
 */
public final class TrackedChange {

	/** Office JS's {@code Word.ChangeTrackingState}. */
	public enum Type {
		ADDED, DELETED, FORMATTED
	}

	/** docx4j's finer kind of change. */
	public enum Kind {
		/** {@code w:ins} around runs, at any depth of run holder. */
		RUN_INSERTION,
		/** {@code w:del} around runs. */
		RUN_DELETION,
		/** {@code w:moveFrom} and {@code w:moveTo}, with their range markers: either half is the whole move. */
		MOVE,
		/** {@code w:pPr/w:rPr/w:ins}: an inserted paragraph mark. */
		PARAGRAPH_MARK_INSERTION,
		/** {@code w:pPr/w:rPr/w:del}: a deleted paragraph mark. */
		PARAGRAPH_MARK_DELETION,
		/** A run's {@code w:rPrChange}. */
		RUN_PROPERTIES,
		/** A paragraph mark's {@code w:rPrChange} ({@code w:pPr/w:rPr/w:rPrChange}). */
		PARAGRAPH_MARK_PROPERTIES,
		/** {@code w:pPrChange}. */
		PARAGRAPH_PROPERTIES,
		/** {@code w:trPr/w:ins}. */
		ROW_INSERTION,
		/** {@code w:trPr/w:del}. */
		ROW_DELETION,
		/** A field Word inserted: {@code w:ins} around its begin and instruction and around its end. */
		FIELD_INSERTION,
		/** A content control or custom XML element inserted ({@code w:customXmlInsRangeStart} around it). */
		CONTENT_CONTROL_INSERTION,
		/** An equation's structure ({@code m:sSup}, {@code m:acc}, ...) inserted: a {@code w:ins} in its {@code m:ctrlPr}. */
		MATH_STRUCTURE_INSERTION,
		/** An equation's structure deleted: a {@code w:del} in its {@code m:ctrlPr}. */
		MATH_STRUCTURE_DELETION,
		/** A group of insertions of different kinds (text, marks, rows, fields). */
		INSERTION,
		/** A group of deletions of different kinds. */
		DELETION,
		/** A group of run and mark formatting changes. */
		FORMATTING,
		/**
		 * A table's property records ({@code w:tblPrChange}, {@code w:tblGridChange},
		 * {@code w:trPrChange}, {@code w:tcPrChange}, {@code w:tblPrExChange}), which Word writes as
		 * one set: one change per table.
		 */
		TABLE_PROPERTIES,
		/** {@code w:sectPrChange}: a section's properties (the body's last section, or a paragraph's). */
		SECTION_PROPERTIES,
		/** {@code w:cellIns} (Word no longer writes it, but reads it). */
		CELL_INSERTION,
		/** {@code w:cellDel} (Word no longer writes it, but reads it). */
		CELL_DELETION
	}

	// --- what a change is over ---------------------------------------------------------------------

	/** A paragraph and where it stands. */
	static final class Para {
		final P p;
		final Object item;
		final List<Object> container;
		Para(P p, Object item, List<Object> container) {
			this.p = p;
			this.item = item;
			this.container = container;
		}
	}

	abstract static class Target {
		abstract CTTrackChange value();
	}

	/** {@code w:ins}, {@code w:del}, {@code w:moveFrom} or {@code w:moveTo} around run-level content. */
	static final class RunT extends Target {
		final Trees.Revision revision;
		final Object item;
		final List<Object> owner;
		final CTTrackChange value;
		RunT(Trees.Revision revision, Object item, List<Object> owner) {
			this.revision = revision;
			this.item = item;
			this.owner = owner;
			this.value = (CTTrackChange) Trees.unwrap(item);
		}
		CTTrackChange value() { return value; }
	}

	/** {@code w:pPr/w:rPr/w:ins} or {@code w:del}. */
	static final class MarkT extends Target {
		final boolean ins;
		final CTTrackChange value;
		MarkT(boolean ins, CTTrackChange value) {
			this.ins = ins;
			this.value = value;
		}
		CTTrackChange value() { return value; }
	}

	static final class RunPropsT extends Target {
		final R run;
		final CTRPrChange value;
		RunPropsT(R run, CTRPrChange value) {
			this.run = run;
			this.value = value;
		}
		CTTrackChange value() { return value; }
	}

	static final class ParaPropsT extends Target {
		final CTPPrChange value;
		ParaPropsT(CTPPrChange value) {
			this.value = value;
		}
		CTTrackChange value() { return value; }
	}

	static final class MarkPropsT extends Target {
		final ParaRPrChange value;
		MarkPropsT(ParaRPrChange value) {
			this.value = value;
		}
		CTTrackChange value() { return value; }
	}

	/** A table row's {@code w:trPr/w:ins} or {@code w:del}, with the revisions of its own cells. */
	static final class RowT extends Target {
		final boolean ins;
		final Tr tr;
		final Object item;
		final List<Object> owner;
		final CTTrackChange value;
		final List<TrackedChange> inner;
		final Object tblItem;
		final List<Object> tblOwner;
		RowT(boolean ins, Tr tr, Object item, List<Object> owner, CTTrackChange value, List<TrackedChange> inner,
				Object tblItem, List<Object> tblOwner) {
			this.ins = ins;
			this.tr = tr;
			this.item = item;
			this.owner = owner;
			this.value = value;
			this.inner = inner;
			this.tblItem = tblItem;
			this.tblOwner = tblOwner;
		}
		CTTrackChange value() { return value; }
	}

	enum GroupKind { INS, DEL, FORMAT }

	/** Touching pieces of one type by one author: Office JS's one change. */
	static final class GroupT extends Target {
		final GroupKind kind;
		final List<TrackedChange> pieces;
		GroupT(GroupKind kind, List<TrackedChange> pieces) {
			this.kind = kind;
			this.pieces = pieces;
		}
		CTTrackChange value() { return pieces.get(0).target.value(); }
	}

	/** A field Word inserted: the items of {@code owner} from the first {@code w:ins} to the last. */
	static final class FieldT extends Target {
		final List<Object> span;
		final List<Object> owner;
		final CTTrackChange value;
		FieldT(List<Object> span, List<Object> owner) {
			this.span = span;
			this.owner = owner;
			this.value = (CTTrackChange) Trees.unwrap(span.get(0));
		}
		CTTrackChange value() { return value; }
	}

	/** A content control or custom XML element inserted: its two pairs of {@code w:customXmlIns*} markers. */
	static final class HolderT extends Target {
		final Object holder;
		final List<Object> owner;
		final Object outerStart, outerEnd, innerEnd, innerStart;
		final CTTrackChange value;
		HolderT(Object holder, List<Object> owner, Object outerStart, Object outerEnd, Object innerEnd, Object innerStart) {
			this.holder = holder;
			this.owner = owner;
			this.outerStart = outerStart;
			this.outerEnd = outerEnd;
			this.innerEnd = innerEnd;
			this.innerStart = innerStart;
			this.value = (CTTrackChange) Trees.unwrap(outerStart);
		}
		CTTrackChange value() { return value; }
	}

	/** {@code w:ins} or {@code w:del} inside an {@code m:r}. */
	static final class MathRunT extends Target {
		final boolean ins;
		final CTR run;
		final Object item;
		final CTMathRunTrackChange value;
		final Object runItem;
		final List<Object> runOwner;
		MathRunT(boolean ins, CTR run, Object item, Object runItem, List<Object> runOwner) {
			this.ins = ins;
			this.run = run;
			this.item = item;
			this.value = (CTMathRunTrackChange) Trees.unwrap(item);
			this.runItem = runItem;
			this.runOwner = runOwner;
		}
		CTTrackChange value() { return value; }
	}

	/** A {@code w:ins} or {@code w:del} in an equation structure's {@code m:ctrlPr}. */
	static final class MathStructT extends Target {
		final boolean ins;
		final CTCtrlPr ctrlPr;
		final CTRPrChange value;
		/** The structure and the list it is in; null when the {@code m:ctrlPr} is an argument's own. */
		final Object structItem;
		final List<Object> structOwner;
		MathStructT(boolean ins, CTCtrlPr ctrlPr, Object structItem, List<Object> structOwner) {
			this.ins = ins;
			this.ctrlPr = ctrlPr;
			this.value = ins ? ctrlPr.getIns() : ctrlPr.getDel();
			this.structItem = structItem;
			this.structOwner = structOwner;
		}
		CTTrackChange value() { return value; }
	}

	/** A table's property records. */
	static final class TableT extends Target {
		final Tbl tbl;
		final CTTrackChange value;
		TableT(Tbl tbl, CTTrackChange value) {
			this.tbl = tbl;
			this.value = value;
		}
		CTTrackChange value() { return value; }
	}

	/** {@code w:sectPrChange}. */
	static final class SectionT extends Target {
		final SectPr sectPr;
		final CTSectPrChange value;
		final String text;
		SectionT(SectPr sectPr, String text) {
			this.sectPr = sectPr;
			this.value = sectPr.getSectPrChange();
			this.text = text;
		}
		CTTrackChange value() { return value; }
	}

	/** {@code w:cellIns} or {@code w:cellDel}. */
	static final class CellT extends Target {
		final boolean ins;
		final Tc tc;
		final Tr tr;
		final CTTrackChange value;
		CellT(boolean ins, Tc tc, Tr tr) {
			this.ins = ins;
			this.tc = tc;
			this.tr = tr;
			this.value = ins ? tc.getTcPr().getCellIns() : tc.getTcPr().getCellDel();
		}
		CTTrackChange value() { return value; }
	}

	// --- the view --------------------------------------------------------------------------------------

	final Target target;
	/** The paragraph the change is in; null for a row, a table's or a section's records, markup at block level. */
	final Para para;
	final Story story;
	/** The changes in a text box's {@code mc:Fallback} twin this one resolves too (CR-043 section 3.1). */
	final List<TrackedChange> twins = new ArrayList<TrackedChange>();
	/** The package's generation when this was listed; null for a piece resolved through its group. */
	Generation generation;

	TrackedChange(Target target, Para para, Story story) {
		this.target = target;
		this.para = para;
		this.story = story;
	}

	/**
	 * Where the change stands, for a range filter: the markup object its position is taken from (a
	 * {@code w:ins}, a mark's record, a run, a row, a table, a section's properties).
	 */
	Object anchor() {
		Target t = target;
		if (t instanceof RunT) return ((RunT) t).value;
		if (t instanceof RunPropsT) return ((RunPropsT) t).run;
		if (t instanceof RowT) return ((RowT) t).tr;
		if (t instanceof FieldT) return Trees.unwrap(((FieldT) t).span.get(0));
		if (t instanceof HolderT) return Trees.unwrap(((HolderT) t).holder);
		if (t instanceof MathStructT) return ((MathStructT) t).ctrlPr;
		if (t instanceof TableT) return ((TableT) t).tbl;
		if (t instanceof SectionT) return ((SectionT) t).sectPr;
		if (t instanceof CellT) return ((CellT) t).tc;
		if (t instanceof GroupT) return ((GroupT) t).pieces.get(0).anchor();
		return t.value();
	}

	/** Office JS's {@code Word.TrackedChange.type}. */
	public Type getType() {
		Target t = target;
		if (t instanceof RunT) {
			Trees.Revision r = ((RunT) t).revision;
			return r == Trees.Revision.INS || r == Trees.Revision.MOVE_TO ? Type.ADDED : Type.DELETED;
		}
		if (t instanceof MarkT) return ((MarkT) t).ins ? Type.ADDED : Type.DELETED;
		if (t instanceof RowT) return ((RowT) t).ins ? Type.ADDED : Type.DELETED;
		if (t instanceof GroupT) {
			GroupKind k = ((GroupT) t).kind;
			return k == GroupKind.INS ? Type.ADDED : k == GroupKind.DEL ? Type.DELETED : Type.FORMATTED;
		}
		if (t instanceof CellT) return ((CellT) t).ins ? Type.ADDED : Type.DELETED;
		if (t instanceof HolderT || t instanceof FieldT) return Type.ADDED;
		if (t instanceof MathRunT) return ((MathRunT) t).ins ? Type.ADDED : Type.DELETED;
		if (t instanceof MathStructT) return ((MathStructT) t).ins ? Type.ADDED : Type.DELETED;
		return Type.FORMATTED;
	}

	/** docx4j's finer kind; a group's is its pieces' when they are of one kind. */
	public Kind getKind() {
		Target t = target;
		if (t instanceof RunT) {
			switch (((RunT) t).revision) {
			case INS: return Kind.RUN_INSERTION;
			case DEL: return Kind.RUN_DELETION;
			default: return Kind.MOVE;
			}
		}
		if (t instanceof MarkT) return ((MarkT) t).ins ? Kind.PARAGRAPH_MARK_INSERTION : Kind.PARAGRAPH_MARK_DELETION;
		if (t instanceof RunPropsT) return Kind.RUN_PROPERTIES;
		if (t instanceof MarkPropsT) return Kind.PARAGRAPH_MARK_PROPERTIES;
		if (t instanceof ParaPropsT) return Kind.PARAGRAPH_PROPERTIES;
		if (t instanceof RowT) return ((RowT) t).ins ? Kind.ROW_INSERTION : Kind.ROW_DELETION;
		if (t instanceof FieldT) return Kind.FIELD_INSERTION;
		if (t instanceof HolderT) return Kind.CONTENT_CONTROL_INSERTION;
		if (t instanceof MathRunT) return ((MathRunT) t).ins ? Kind.RUN_INSERTION : Kind.RUN_DELETION;
		if (t instanceof MathStructT) return ((MathStructT) t).ins ? Kind.MATH_STRUCTURE_INSERTION : Kind.MATH_STRUCTURE_DELETION;
		if (t instanceof TableT) return Kind.TABLE_PROPERTIES;
		if (t instanceof SectionT) return Kind.SECTION_PROPERTIES;
		if (t instanceof CellT) return ((CellT) t).ins ? Kind.CELL_INSERTION : Kind.CELL_DELETION;
		GroupT g = (GroupT) t;
		Kind first = g.pieces.get(0).getKind();
		for (TrackedChange piece : g.pieces) {
			if (piece.getKind() != first) {
				return g.kind == GroupKind.INS ? Kind.INSERTION : g.kind == GroupKind.DEL ? Kind.DELETION : Kind.FORMATTING;
			}
		}
		return first;
	}

	/** {@code w:author}; a group's is its first piece's (one author by definition). */
	public String getAuthor() {
		return target.value() == null ? null : target.value().getAuthor();
	}

	/**
	 * When the change was made: {@code w16du:dateUtc} where present (Word 365 writes both); else
	 * {@code w:date}, which Word means as local wall-clock time whatever its {@code Z} says, read in
	 * this JVM's time zone (as Office JS reads it in the add-in's); an explicit non-zero offset is
	 * read as it says. A group's is its first piece's. Null when there is none (docx4j-diffx writes
	 * {@code w:date=""}).
	 */
	public Instant getDate() {
		return dateOf(target.value());
	}

	static Instant dateOf(CTTrackChange value) {
		if (value == null) return null;
		XMLGregorianCalendar utc = value.getDateUtc();
		if (utc != null && utc.getYear() != DatatypeConstants.FIELD_UNDEFINED) {
			XMLGregorianCalendar c = (XMLGregorianCalendar) utc.clone();
			if (c.getTimezone() == DatatypeConstants.FIELD_UNDEFINED) c.setTimezone(0);
			return c.toGregorianCalendar().toInstant();
		}
		XMLGregorianCalendar d = value.getDate();
		if (d == null || d.getYear() == DatatypeConstants.FIELD_UNDEFINED) return null;
		if (d.getTimezone() != DatatypeConstants.FIELD_UNDEFINED && d.getTimezone() != 0) {
			return d.toGregorianCalendar().toInstant();
		}
		LocalDateTime local = LocalDateTime.of(d.getYear(), field(d.getMonth(), 1), field(d.getDay(), 1),
				field(d.getHour(), 0), field(d.getMinute(), 0), field(d.getSecond(), 0));
		return local.atZone(ZoneId.systemDefault()).toInstant();
	}

	private static int field(int value, int fallback) {
		return value == DatatypeConstants.FIELD_UNDEFINED ? fallback : value;
	}

	/** The annotation id ({@code w:id}); a group's is its first piece's. */
	public BigInteger getId() {
		return target.value() == null ? null : target.value().getId();
	}

	/**
	 * The text the change covers, as Office JS gives it: a paragraph mark is {@code \r}, a row its
	 * cells a tab apart and then {@code \r\n}, a section's its text then {@code \f}; a deletion's is
	 * the deleted text; a field's is its result.
	 */
	public String getText() {
		return Texts.of(this);
	}

	/** The part the change is in: the main document part, a header, a footer, the notes, the comments. */
	public JaxbXmlPart<?> getPart() {
		return story.part;
	}

	/**
	 * The single changes this one groups, in document order; a change that is no group is its own one
	 * piece. A piece can be accepted or rejected on its own (a text box's piece then leaves its
	 * {@code mc:Fallback} twin to {@code acceptAll}).
	 */
	public List<TrackedChange> getPieces() {
		if (target instanceof GroupT) return Collections.unmodifiableList(((GroupT) target).pieces);
		return Collections.singletonList(this);
	}

	/**
	 * Keeps the change, as Word's Review tab does: an insertion stays and loses its markup, a
	 * deletion goes (a deleted paragraph mark joins its paragraph with the next), a formatting
	 * change's record is dropped; either half of a move keeps the whole move. What goes takes with it
	 * the notes it references, the bookmarks and comments touching it, and the parts nothing else
	 * references (CR-043 section 3.2).
	 *
	 * @throws IllegalStateException when the package has been changed by an accept or reject since this was listed
	 */
	public void accept() {
		resolve(true);
	}

	/**
	 * Puts back what was there: an insertion goes, a deletion stays and loses its markup, a
	 * formatting change's recorded properties come back; either half of a move rejects the whole move.
	 *
	 * @throws IllegalStateException when the package has been changed by an accept or reject since this was listed
	 */
	public void reject() {
		resolve(false);
	}

	private void resolve(boolean accept) {
		if (generation == null || !generation.isCurrent()) {
			throw new IllegalStateException("This tracked change was listed before the last accept or reject on its package: list again");
		}
		Removals removals = new Removals(story.pkg);
		Resolver.resolve(this, accept, removals);
		for (TrackedChange twin : twins) Resolver.resolve(twin, accept, removals);
		removals.flush();
		generation.advance();
	}

	@Override
	public String toString() {
		return getKind() + " by " + getAuthor() + " in " + (story.part == null ? "?" : story.part.getPartName().getName())
				+ ": \"" + getText().replace("\r", "\\r").replace("\n", "\\n").replace("\t", "\\t") + "\"";
	}
}
