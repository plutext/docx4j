/*
   Licensed to Plutext Pty Ltd under one or more contributor license agreements.

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
package org.docx4j.model.table;

import java.math.BigInteger;
import java.util.ArrayList;
import java.util.EnumSet;
import java.util.List;
import java.util.Set;

import jakarta.xml.bind.JAXBElement;

import org.docx4j.jaxb.Context;
import org.docx4j.model.styles.StyleUtil;
import org.docx4j.wml.CTCnf;
import org.docx4j.wml.CTTblLook;
import org.docx4j.wml.CTTblPrBase;
import org.docx4j.wml.CTTblStylePr;
import org.docx4j.wml.PPr;
import org.docx4j.wml.RPr;
import org.docx4j.sharedtypes.STOnOff;
import org.docx4j.wml.STTblStyleOverrideType;
import org.docx4j.wml.Style;
import org.docx4j.wml.TcPr;
import org.docx4j.wml.TrPr;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * A table style's <em>conditional formatting</em> ({@code w:tblStylePr}, ECMA-376-1
 * &#xa7;17.7.6): which of its conditions a given row, cell or paragraph is under, and
 * which of the style's {@code w:tblStylePr} entries therefore apply to it, in the order
 * they are to be applied.
 *
 * <p>Three things decide it.</p>
 * <ul>
 * <li>{@code w:tblPr/w:tblLook} on the table says which conditional formats the table
 *     asks for at all: first row, last row, first column, last column, horizontal and
 *     vertical banding ({@link Look}).  Word writes it both as the six attributes and as
 *     the legacy {@code w:val} bitmask; older documents carry only the bitmask.</li>
 * <li>{@code w:cnfStyle} on {@code w:tr}, {@code w:tc} and {@code w:p} is Word's cache of
 *     the conditions the element is under, a twelve-bit string
 *     (&#xa7;17.18.6, {@link #fromCnf(String)}).  Word writes the row bits on the row,
 *     the column bits on the cell and the row bits again on the paragraph, and never
 *     the corner bits, so the three are combined and the corners derived.</li>
 * <li>Where there is no cache the conditions follow from the position: the band
 *     arithmetic of {@link #atPosition}, which honours {@code w:tblStyleRowBandSize} /
 *     {@code w:tblStyleColBandSize} and leaves the first row and column out of the bands
 *     when the look gives them a condition of their own.</li>
 * </ul>
 *
 * <p>The conditions are then applied in the order &#xa7;17.7.6 fixes, later overriding
 * earlier ({@link #PRECEDENCE}): the whole table, then vertical banding, horizontal
 * banding, first column, last column, first row, last row, and the four corner cells.
 * The lot sits below the paragraph's own style and below direct formatting, which is
 * where {@code ParagraphStylesInTableFix} and the table writers put it.</p>
 *
 * @since 17.1.1
 */
public final class TableStyleConditions {

	private static final Logger log = LoggerFactory.getLogger(TableStyleConditions.class);

	private TableStyleConditions() {}

	/**
	 * The order in which a table style's conditional formats are applied to a cell, later
	 * overriding earlier (ECMA-376-1 &#xa7;17.7.6).
	 */
	public static final STTblStyleOverrideType[] PRECEDENCE = {
			STTblStyleOverrideType.WHOLE_TABLE,
			STTblStyleOverrideType.BAND_1_VERT, STTblStyleOverrideType.BAND_2_VERT,
			STTblStyleOverrideType.BAND_1_HORZ, STTblStyleOverrideType.BAND_2_HORZ,
			STTblStyleOverrideType.FIRST_COL, STTblStyleOverrideType.LAST_COL,
			STTblStyleOverrideType.FIRST_ROW, STTblStyleOverrideType.LAST_ROW,
			STTblStyleOverrideType.NW_CELL, STTblStyleOverrideType.NE_CELL,
			STTblStyleOverrideType.SW_CELL, STTblStyleOverrideType.SE_CELL };

	/** The conditions {@code w:cnfStyle} carries on a row: the row-axis bits. */
	private static final EnumSet<STTblStyleOverrideType> ROW_AXIS = EnumSet.of(
			STTblStyleOverrideType.FIRST_ROW, STTblStyleOverrideType.LAST_ROW,
			STTblStyleOverrideType.BAND_1_HORZ, STTblStyleOverrideType.BAND_2_HORZ);

	/** The conditions {@code w:cnfStyle} carries on a cell: the column-axis bits. */
	private static final EnumSet<STTblStyleOverrideType> COLUMN_AXIS = EnumSet.of(
			STTblStyleOverrideType.FIRST_COL, STTblStyleOverrideType.LAST_COL,
			STTblStyleOverrideType.BAND_1_VERT, STTblStyleOverrideType.BAND_2_VERT);

	/**
	 * Which conditional formats a table asks for: {@code w:tblPr/w:tblLook}, resolved.
	 */
	public static final class Look {

		public final boolean firstRow;
		public final boolean lastRow;
		public final boolean firstColumn;
		public final boolean lastColumn;
		/** Horizontal (row) banding is on: {@code w:noHBand} is off. */
		public final boolean hBand;
		/** Vertical (column) banding is on: {@code w:noVBand} is off. */
		public final boolean vBand;

		public Look(boolean firstRow, boolean lastRow, boolean firstColumn, boolean lastColumn,
				boolean hBand, boolean vBand) {
			this.firstRow = firstRow;
			this.lastRow = lastRow;
			this.firstColumn = firstColumn;
			this.lastColumn = lastColumn;
			this.hBand = hBand;
			this.vBand = vBand;
		}

		/**
		 * What Word assumes where a table states no {@code w:tblLook} at all: its own
		 * default, {@code 04A0} - first row, first column and row banding on, column
		 * banding off - which is also what Word's Table Style Options show for a fresh
		 * table.  (The three real-document corpora hold one document whose tables have no
		 * {@code w:tblLook}, and none of its tables uses a table style, so this has no
		 * corpus measurement behind it.)
		 */
		public static final Look DEFAULT = new Look(true, false, true, false, true, false);

		/** No conditional formatting at all: {@code w:tblLook w:val="0000"} with banding off. */
		public static final Look NONE = new Look(false, false, false, false, false, false);

		/**
		 * The look a {@code w:tblLook} states.  The six attributes (ECMA-376) win where any
		 * of them is present; otherwise the legacy {@code w:val} bitmask (0x0020 firstRow,
		 * 0x0040 lastRow, 0x0080 firstColumn, 0x0100 lastColumn, 0x0200 noHBand, 0x0400
		 * noVBand); a null or empty element is {@link #DEFAULT}.
		 */
		public static Look of(CTTblLook tblLook) {
			if (tblLook == null) return DEFAULT;
			if (tblLook.getFirstRow() != null || tblLook.getLastRow() != null
					|| tblLook.getFirstColumn() != null || tblLook.getLastColumn() != null
					|| tblLook.getNoHBand() != null || tblLook.getNoVBand() != null) {
				return new Look(on(tblLook.getFirstRow()), on(tblLook.getLastRow()),
						on(tblLook.getFirstColumn()), on(tblLook.getLastColumn()),
						!on(tblLook.getNoHBand()), !on(tblLook.getNoVBand()));
			}
			String val = tblLook.getVal();
			if (val == null || val.trim().length() == 0) return DEFAULT;
			try {
				int bits = Integer.parseInt(val.trim(), 16);
				return new Look((bits & 0x0020) != 0, (bits & 0x0040) != 0,
						(bits & 0x0080) != 0, (bits & 0x0100) != 0,
						(bits & 0x0200) == 0, (bits & 0x0400) == 0);
			} catch (NumberFormatException e) {
				log.warn("Unreadable w:tblLook/@w:val '" + val + "'; using Word's default");
				return DEFAULT;
			}
		}

		private static boolean on(STOnOff v) {
			return v != null && (v == STOnOff.ONE || v == STOnOff.TRUE || v == STOnOff.ON);
		}

		/** Whether this look lets the condition through at all. */
		public boolean allows(STTblStyleOverrideType t) {
			switch (t) {
				case WHOLE_TABLE: return true;
				case FIRST_ROW: return firstRow;
				case LAST_ROW: return lastRow;
				case FIRST_COL: return firstColumn;
				case LAST_COL: return lastColumn;
				case BAND_1_HORZ: case BAND_2_HORZ: return hBand;
				case BAND_1_VERT: case BAND_2_VERT: return vBand;
				case NW_CELL: return firstRow && firstColumn;
				case NE_CELL: return firstRow && lastColumn;
				case SW_CELL: return lastRow && firstColumn;
				case SE_CELL: return lastRow && lastColumn;
				default: return false;
			}
		}

		@Override
		public String toString() {
			return "Look[firstRow=" + firstRow + " lastRow=" + lastRow + " firstColumn=" + firstColumn
					+ " lastColumn=" + lastColumn + " hBand=" + hBand + " vBand=" + vBand + "]";
		}
	}

	/** The look the table's (effective) {@code w:tblPr} states; {@link Look#DEFAULT} where none. */
	public static Look look(CTTblPrBase tblPr) {
		return Look.of(tblPr == null ? null : tblPr.getTblLook());
	}

	/** {@code w:tblStyleRowBandSize}, at least 1 (the default). */
	public static int rowBandSize(CTTblPrBase tblPr) {
		if (tblPr != null && tblPr.getTblStyleRowBandSize() != null) {
			return atLeastOne(tblPr.getTblStyleRowBandSize().getVal());
		}
		return 1;
	}

	/** {@code w:tblStyleColBandSize}, at least 1 (the default). */
	public static int colBandSize(CTTblPrBase tblPr) {
		if (tblPr != null && tblPr.getTblStyleColBandSize() != null) {
			return atLeastOne(tblPr.getTblStyleColBandSize().getVal());
		}
		return 1;
	}

	private static int atLeastOne(BigInteger v) {
		return (v == null || v.intValue() < 1) ? 1 : v.intValue();
	}

	/**
	 * The conditions a {@code w:cnfStyle} bitmask names (ECMA-376-1 &#xa7;17.18.6): twelve
	 * characters, in order firstRow, lastRow, firstColumn, lastColumn, oddVBand (band1Vert),
	 * evenVBand (band2Vert), oddHBand (band1Horz), evenHBand (band2Horz), then the four
	 * corners nw, ne, sw, se.  A short string is read as far as it goes; a null one is the
	 * empty set.
	 */
	public static EnumSet<STTblStyleOverrideType> fromCnf(String val) {
		EnumSet<STTblStyleOverrideType> out = EnumSet.noneOf(STTblStyleOverrideType.class);
		if (val == null) return out;
		String s = val.trim();
		STTblStyleOverrideType[] bits = {
				STTblStyleOverrideType.FIRST_ROW, STTblStyleOverrideType.LAST_ROW,
				STTblStyleOverrideType.FIRST_COL, STTblStyleOverrideType.LAST_COL,
				STTblStyleOverrideType.BAND_1_VERT, STTblStyleOverrideType.BAND_2_VERT,
				STTblStyleOverrideType.BAND_1_HORZ, STTblStyleOverrideType.BAND_2_HORZ,
				STTblStyleOverrideType.NW_CELL, STTblStyleOverrideType.NE_CELL,
				STTblStyleOverrideType.SW_CELL, STTblStyleOverrideType.SE_CELL };
		for (int i = 0; i < bits.length && i < s.length(); i++) {
			if (s.charAt(i) == '1') out.add(bits[i]);
		}
		return out;
	}

	/** As {@link #fromCnf(String)}, for the element; the empty set where there is none. */
	public static EnumSet<STTblStyleOverrideType> fromCnf(CTCnf cnf) {
		return fromCnf(cnf == null ? null : cnf.getVal());
	}

	/** The {@code w:cnfStyle} of a row, or null where the row states none. */
	public static CTCnf rowCnf(TrPr trPr) {
		if (trPr == null) return null;
		for (JAXBElement<?> el : trPr.getCnfStyleOrDivIdOrGridBefore()) {
			if ("cnfStyle".equals(el.getName().getLocalPart()) && el.getValue() instanceof CTCnf) {
				return (CTCnf) el.getValue();
			}
		}
		return null;
	}

	/**
	 * The conditions a cell is under by its position alone, with no {@code w:cnfStyle} to
	 * go by.
	 *
	 * <p>Banding counts from the first row (column) which is not under a condition of its
	 * own: where the look has {@code firstRow} on, row 1 is the first banded row, so with
	 * a band size of 1 the rows alternate band1Horz, band2Horz, ... from row 1; a last row
	 * (column) the look gives its own condition is left out of the bands too.  A band is
	 * {@code w:tblStyleRowBandSize} rows deep.  A cell spanning several columns is placed
	 * by its first column, and is in the last column where its span reaches it.</p>
	 *
	 * @param row       0-based row index
	 * @param rowCount  rows in the table
	 * @param col       0-based grid column the cell starts in
	 * @param colSpan   grid columns the cell covers (at least 1)
	 * @param colCount  grid columns in the table
	 */
	public static EnumSet<STTblStyleOverrideType> atPosition(Look look, int rowBandSize, int colBandSize,
			int row, int rowCount, int col, int colSpan, int colCount) {

		EnumSet<STTblStyleOverrideType> out = EnumSet.noneOf(STTblStyleOverrideType.class);
		if (look == null) look = Look.DEFAULT;
		boolean first = look.firstRow && row == 0;
		boolean last = look.lastRow && rowCount > 0 && row == rowCount - 1;
		if (first) out.add(STTblStyleOverrideType.FIRST_ROW);
		if (last) out.add(STTblStyleOverrideType.LAST_ROW);
		if (look.hBand && !first && !last) {
			int offset = look.firstRow ? 1 : 0;
			int band = (row - offset) / Math.max(1, rowBandSize);
			out.add(band % 2 == 0 ? STTblStyleOverrideType.BAND_1_HORZ : STTblStyleOverrideType.BAND_2_HORZ);
		}
		int span = Math.max(1, colSpan);
		boolean firstCol = look.firstColumn && col == 0;
		boolean lastCol = look.lastColumn && colCount > 0 && col + span - 1 >= colCount - 1;
		if (firstCol) out.add(STTblStyleOverrideType.FIRST_COL);
		if (lastCol) out.add(STTblStyleOverrideType.LAST_COL);
		if (look.vBand && !firstCol && !lastCol) {
			int offset = look.firstColumn ? 1 : 0;
			int band = (col - offset) / Math.max(1, colBandSize);
			out.add(band % 2 == 0 ? STTblStyleOverrideType.BAND_1_VERT : STTblStyleOverrideType.BAND_2_VERT);
		}
		addCorners(out);
		return out;
	}

	/**
	 * The conditions a cell (or a paragraph in it) is under: the {@code w:cnfStyle} caches
	 * where the document has them, the position where it has not, gated by the look.
	 *
	 * <p>Word writes the row-axis bits on the {@code w:tr}, the column-axis bits on the
	 * {@code w:tc} and the row-axis bits again on each {@code w:p} - measured over the
	 * three corpora, where the 5308 cell caches are all column bits and the 3392 row caches
	 * and 1737 non-empty paragraph caches all row bits - and it never writes the corner
	 * bits.  So the row's cache, where there is one, decides the row axis, the cell's the
	 * column axis, a paragraph's bits are added on whichever axis they name, and the
	 * corners are derived from the result.  An axis with no cache falls back to the
	 * arithmetic of {@link #atPosition}.  Every bit is then gated by the look, so a stale
	 * cache cannot switch on a format the table has turned off.</p>
	 *
	 * @param rowCnf   the row's {@code w:cnfStyle}, or null
	 * @param cellCnf  the cell's, or null
	 * @param paraCnf  the paragraph's, or null (a row or cell asks with null here)
	 */
	public static EnumSet<STTblStyleOverrideType> resolve(Look look, int rowBandSize, int colBandSize,
			int row, int rowCount, int col, int colSpan, int colCount,
			CTCnf rowCnf, CTCnf cellCnf, CTCnf paraCnf) {

		if (look == null) look = Look.DEFAULT;
		EnumSet<STTblStyleOverrideType> computed = atPosition(look, rowBandSize, colBandSize,
				row, rowCount, col, colSpan, colCount);
		EnumSet<STTblStyleOverrideType> para = fromCnf(paraCnf);

		EnumSet<STTblStyleOverrideType> out = EnumSet.noneOf(STTblStyleOverrideType.class);

		// row axis
		EnumSet<STTblStyleOverrideType> rowBits = fromCnf(rowCnf);
		rowBits.addAll(para);
		rowBits.retainAll(ROW_AXIS);
		if (rowCnf != null || (paraCnf != null && !rowBits.isEmpty())) {
			out.addAll(rowBits);
		} else {
			EnumSet<STTblStyleOverrideType> c = EnumSet.copyOf(computed);
			c.retainAll(ROW_AXIS);
			out.addAll(c);
		}

		// column axis
		EnumSet<STTblStyleOverrideType> colBits = fromCnf(cellCnf);
		colBits.addAll(para);
		colBits.retainAll(COLUMN_AXIS);
		if (cellCnf != null || (paraCnf != null && !colBits.isEmpty())) {
			out.addAll(colBits);
		} else {
			EnumSet<STTblStyleOverrideType> c = EnumSet.copyOf(computed);
			c.retainAll(COLUMN_AXIS);
			out.addAll(c);
		}

		gate(out, look);
		addCorners(out);
		return out;
	}

	/** Remove the conditions the look does not allow (in place), and return the set. */
	public static EnumSet<STTblStyleOverrideType> gate(EnumSet<STTblStyleOverrideType> conditions, Look look) {
		if (look == null) look = Look.DEFAULT;
		List<STTblStyleOverrideType> drop = new ArrayList<STTblStyleOverrideType>();
		for (STTblStyleOverrideType t : conditions) {
			if (!look.allows(t)) drop.add(t);
		}
		conditions.removeAll(drop);
		return conditions;
	}

	/** Add the corner conditions the row and column conditions imply (in place). */
	private static void addCorners(EnumSet<STTblStyleOverrideType> s) {
		boolean fr = s.contains(STTblStyleOverrideType.FIRST_ROW);
		boolean lr = s.contains(STTblStyleOverrideType.LAST_ROW);
		boolean fc = s.contains(STTblStyleOverrideType.FIRST_COL);
		boolean lc = s.contains(STTblStyleOverrideType.LAST_COL);
		if (fr && fc) s.add(STTblStyleOverrideType.NW_CELL);
		if (fr && lc) s.add(STTblStyleOverrideType.NE_CELL);
		if (lr && fc) s.add(STTblStyleOverrideType.SW_CELL);
		if (lr && lc) s.add(STTblStyleOverrideType.SE_CELL);
	}

	/**
	 * The style's {@code w:tblStylePr} entry for a condition, or null.  Pass the
	 * <em>effective</em> table style (the {@code w:basedOn} chain merged, as
	 * {@code PropertyResolver.getEffectiveTableStyle} does) to see inherited conditions.
	 */
	public static CTTblStylePr lookup(Style tableStyle, STTblStyleOverrideType type) {
		if (tableStyle == null || tableStyle.getTblStylePr() == null) return null;
		for (CTTblStylePr pr : tableStyle.getTblStylePr()) {
			if (pr != null && pr.getType() == type) return pr;
		}
		return null;
	}

	/**
	 * The style's {@code w:tblStylePr} entries which apply under these conditions, in the
	 * order they are to be applied ({@link #PRECEDENCE}); a {@code wholeTable} entry, where
	 * the style has one, always applies and comes first.
	 */
	public static List<CTTblStylePr> applicable(Style tableStyle, Set<STTblStyleOverrideType> conditions) {
		List<CTTblStylePr> out = new ArrayList<CTTblStylePr>();
		if (tableStyle == null || tableStyle.getTblStylePr() == null || tableStyle.getTblStylePr().isEmpty()) {
			return out;
		}
		for (STTblStyleOverrideType t : PRECEDENCE) {
			if (t != STTblStyleOverrideType.WHOLE_TABLE && (conditions == null || !conditions.contains(t))) continue;
			CTTblStylePr pr = lookup(tableStyle, t);
			if (pr != null) out.add(pr);
		}
		return out;
	}

	/**
	 * The paragraph properties the applicable conditions contribute, merged in precedence
	 * order into a new object; null where none of them has any.
	 */
	public static PPr conditionalPPr(List<CTTblStylePr> applicable) {
		PPr out = null;
		for (CTTblStylePr pr : applicable) {
			if (pr.getPPr() != null && !StyleUtil.isEmpty(pr.getPPr())) {
				out = StyleUtil.apply(pr.getPPr(), out);
			}
		}
		return out;
	}

	/** As {@link #conditionalPPr}, for the run properties. */
	public static RPr conditionalRPr(List<CTTblStylePr> applicable) {
		RPr out = null;
		for (CTTblStylePr pr : applicable) {
			if (pr.getRPr() != null && !StyleUtil.isEmpty(pr.getRPr())) {
				out = StyleUtil.apply(pr.getRPr(), out);
			}
		}
		return out;
	}

	/** As {@link #conditionalPPr}, for the row properties. */
	public static TrPr conditionalTrPr(List<CTTblStylePr> applicable) {
		TrPr out = null;
		for (CTTblStylePr pr : applicable) {
			if (pr.getTrPr() != null && !StyleUtil.isEmpty(pr.getTrPr())) {
				if (out == null) out = Context.getWmlObjectFactory().createTrPr();
				out = StyleUtil.apply(pr.getTrPr(), out);
			}
		}
		return out;
	}

	/** As {@link #conditionalPPr}, for the cell properties. */
	public static TcPr conditionalTcPr(List<CTTblStylePr> applicable) {
		TcPr out = null;
		for (CTTblStylePr pr : applicable) {
			if (pr.getTcPr() != null && !StyleUtil.isEmpty(pr.getTcPr())) {
				out = StyleUtil.apply(pr.getTcPr(), out);
			}
		}
		return out;
	}

	/** Whether the entry has any paragraph or run formatting to contribute. */
	public static boolean formatsText(CTTblStylePr pr) {
		return pr != null
				&& ((pr.getPPr() != null && !StyleUtil.isEmpty(pr.getPPr()))
						|| (pr.getRPr() != null && !StyleUtil.isEmpty(pr.getRPr())));
	}

	/**
	 * A short name for a set of conditions, in precedence order - {@code firstCol-firstRow-nwCell}
	 * - for use in a synthetic style id; the empty string for none.
	 */
	public static String key(Set<STTblStyleOverrideType> conditions) {
		if (conditions == null || conditions.isEmpty()) return "";
		StringBuilder sb = new StringBuilder();
		for (STTblStyleOverrideType t : PRECEDENCE) {
			if (t == STTblStyleOverrideType.WHOLE_TABLE || !conditions.contains(t)) continue;
			if (sb.length() > 0) sb.append('-');
			sb.append(t.value());
		}
		return sb.toString();
	}

	/**
	 * Whether the row's own {@code w:trPr} says {@code w:tblHeader}.
	 */
	public static boolean hasTblHeader(TrPr trPr) {
		if (trPr == null) return false;
		for (JAXBElement<?> el : trPr.getCnfStyleOrDivIdOrGridBefore()) {
			if ("tblHeader".equals(el.getName().getLocalPart())
					&& el.getValue() instanceof org.docx4j.wml.BooleanDefaultTrue) {
				return ((org.docx4j.wml.BooleanDefaultTrue) el.getValue()).isVal();
			}
		}
		return false;
	}
}
