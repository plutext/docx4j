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
package org.docx4j.model.table;

import java.math.BigInteger;
import java.util.EnumSet;
import java.util.IdentityHashMap;
import java.util.List;

import org.docx4j.TraversalUtil;
import org.docx4j.finders.TcFinder;
import org.docx4j.model.PropertyResolver;
import org.docx4j.model.table.TableStyleConditions.Look;
import org.docx4j.wml.CTTblPrBase;
import org.docx4j.wml.CTTrPrBase;
import org.docx4j.wml.PPr;
import org.docx4j.wml.STTblStyleOverrideType;
import org.docx4j.wml.Style;
import org.docx4j.wml.Tbl;
import org.docx4j.wml.Tc;
import org.docx4j.wml.Tr;

import jakarta.xml.bind.JAXBElement;

/**
 * What a table's paragraphs need to know about it to be formatted: the table style it resolves
 * to (its w:basedOn chain merged), the conditional formats its {@code w:tblLook} asks for, its
 * band sizes, and where each row and cell sits.  {@link #forParagraph} then gives the
 * {@link CellContext} of a paragraph in one of its cells.
 *
 * <p>A reading of the table's <em>content</em> (rows, spans, look), so it is built fresh for
 * each table by {@link PropertyResolver#tableContext(Tbl)} and held by the caller while it is
 * in the table; the resolver keeps none (a cached one would go stale when a row is added, and
 * would keep the table reachable for the package's life).  What it takes from the styles part,
 * the merged table style, is the resolver's cached {@link PropertyResolver#getTableStyleChain(String)}.</p>
 *
 * <p>The rows and cells are counted as {@code ParagraphStylesInTableFix} counted them until
 * 17.3.1: nested tables excluded, a cell's column its row's {@code w:gridBefore} plus the
 * spans before it.  See CR-030 &#xa7;4.2.</p>
 *
 * @since 17.3.1
 */
public final class TableContext {

	private final String tableStyleId;
	private final Style tableStyle;
	private final Look look;
	private final int rowBandSize;
	private final int colBandSize;
	private final int rowCount;
	private final int colCount;
	private final IdentityHashMap<Tr, Integer> rowIndex = new IdentityHashMap<Tr, Integer>();
	/** {first grid column, span} per cell */
	private final IdentityHashMap<Tc, int[]> cellColumns = new IdentityHashMap<Tc, int[]>();

	/**
	 * The context of a table.  Use {@link PropertyResolver#tableContext(Tbl)}.
	 *
	 * @param tbl the table
	 * @param resolver the package's resolver, for the table style
	 */
	public TableContext(Tbl tbl, PropertyResolver resolver) {

		this.tableStyleId = resolver.getTableStyleIdOf(tbl.getTblPr());
		this.tableStyle = resolver.getTableStyleChain(tableStyleId);

		// the table's own tblPr decides the look and band sizes; the style's is the fallback
		CTTblPrBase tblPr = tbl.getTblPr();
		CTTblPrBase stylePr = tableStyle.getTblPr();
		if (tblPr != null && tblPr.getTblLook() != null) {
			look = TableStyleConditions.look(tblPr);
		} else if (stylePr != null && stylePr.getTblLook() != null) {
			look = TableStyleConditions.look(stylePr);
		} else {
			look = Look.DEFAULT;
		}
		rowBandSize = (tblPr != null && tblPr.getTblStyleRowBandSize() != null)
				? TableStyleConditions.rowBandSize(tblPr) : TableStyleConditions.rowBandSize(stylePr);
		colBandSize = (tblPr != null && tblPr.getTblStyleColBandSize() != null)
				? TableStyleConditions.colBandSize(tblPr) : TableStyleConditions.colBandSize(stylePr);

		TableModel.TrFinder trFinder = new TableModel.TrFinder();
		new TraversalUtil(tbl, trFinder);
		List<Tr> rows = trFinder.getTrList();
		int cols = 0;
		for (int r = 0; r < rows.size(); r++) {
			Tr tr = rows.get(r);
			rowIndex.put(tr, Integer.valueOf(r));
			int c = gridBeforeOrAfter(tr, "gridBefore");
			TcFinder tcFinder = new TcFinder();
			new TraversalUtil(tr, tcFinder);
			for (Tc tc : tcFinder.tcList) {
				int span = 1;
				if (tc.getTcPr() != null && tc.getTcPr().getGridSpan() != null
						&& tc.getTcPr().getGridSpan().getVal() != null) {
					span = Math.max(1, tc.getTcPr().getGridSpan().getVal().intValue());
				}
				cellColumns.put(tc, new int[] { c, span });
				c += span;
			}
			c += gridBeforeOrAfter(tr, "gridAfter");
			if (c > cols) cols = c;
		}
		rowCount = rows.size();
		colCount = cols;
	}

	private static int gridBeforeOrAfter(Tr tr, String name) {
		if (tr.getTrPr() == null) return 0;
		for (JAXBElement<?> el : tr.getTrPr().getCnfStyleOrDivIdOrGridBefore()) {
			if (name.equals(el.getName().getLocalPart())) {
				Object v = el.getValue();
				BigInteger val = null;
				if (v instanceof CTTrPrBase.GridBefore) val = ((CTTrPrBase.GridBefore) v).getVal();
				else if (v instanceof CTTrPrBase.GridAfter) val = ((CTTrPrBase.GridAfter) v).getVal();
				return val == null ? 0 : Math.max(0, val.intValue());
			}
		}
		return 0;
	}

	/**
	 * The context of a paragraph in this table: the conditions its row, its cell and its own
	 * {@code w:cnfStyle} caches say it is under, or its position where they say nothing
	 * ({@link TableStyleConditions#resolve}).  A row or cell this table does not hold (a
	 * nested table's, or one added since this context was built) is under no condition.
	 *
	 * @param tr the paragraph's row
	 * @param tc the paragraph's cell
	 * @param pPr the paragraph's own w:pPr, for its w:cnfStyle; may be null
	 */
	public CellContext forParagraph(Tr tr, Tc tc, PPr pPr) {
		return new CellContext(tableStyleId, tableStyle, conditions(tr, tc, pPr));
	}

	/** The context of a cell, for its row and cell properties: as {@link #forParagraph}
	 *  without a paragraph's own w:cnfStyle. */
	public CellContext forCell(Tr tr, Tc tc) {
		return forParagraph(tr, tc, null);
	}

	private EnumSet<STTblStyleOverrideType> conditions(Tr tr, Tc tc, PPr pPr) {
		Integer r = tr == null ? null : rowIndex.get(tr);
		int[] cc = tc == null ? null : cellColumns.get(tc);
		if (r == null || cc == null) {
			return EnumSet.noneOf(STTblStyleOverrideType.class);
		}
		return TableStyleConditions.resolve(look, rowBandSize, colBandSize,
				r.intValue(), rowCount, cc[0], cc[1], colCount,
				TableStyleConditions.rowCnf(tr.getTrPr()),
				tc.getTcPr() == null ? null : tc.getTcPr().getCnfStyle(),
				pPr == null ? null : pPr.getCnfStyle());
	}

	/** The id of the table style the table resolves to, or null. */
	public String getTableStyleId() {
		return tableStyleId;
	}

	/** The table style's chain merged; shared, read it only. */
	public Style getTableStyle() {
		return tableStyle;
	}

	public Look getLook() {
		return look;
	}

	public int getRowBandSize() {
		return rowBandSize;
	}

	public int getColBandSize() {
		return colBandSize;
	}

	public int getRowCount() {
		return rowCount;
	}

	public int getColCount() {
		return colCount;
	}
}
