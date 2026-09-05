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
package org.docx4j.convert.out.common.wrappers;

import java.math.BigInteger;
import java.util.ArrayList;
import java.util.List;

import org.docx4j.XmlUtils;
import org.docx4j.jaxb.Context;
import org.docx4j.wml.CTColumn;
import org.docx4j.wml.CTColumns;
import org.docx4j.wml.CTTblCellMar;
import org.docx4j.wml.CTTblLayoutType;
import org.docx4j.wml.ObjectFactory;
import org.docx4j.wml.P;
import org.docx4j.wml.R;
import org.docx4j.wml.STBrType;
import org.docx4j.wml.STTblLayoutType;
import org.docx4j.wml.SectPr;
import org.docx4j.wml.Tbl;
import org.docx4j.wml.TblGrid;
import org.docx4j.wml.TblGridCol;
import org.docx4j.wml.TblPr;
import org.docx4j.wml.TblWidth;
import org.docx4j.wml.Tc;
import org.docx4j.wml.TcPr;
import org.docx4j.wml.Tr;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * A section whose {@code w:cols} declares columns of <b>different widths</b>
 * ({@code w:equalWidth="0"} with {@code w:col} children), rendered as a one-row table
 * whose cells are the columns.
 *
 * <p>XSL-FO's region-body columns are all the same width, so until 17.0.6 such a section
 * was laid out as equal columns with the first column's gap - measured, a certificate
 * whose columns are 157 and 318pt with a 24pt gap came out as two 237.5pt columns, so
 * every line of the second column started 80pt left of Word's and broke differently.  A
 * one-row {@code fo:table} gets the widths and the gaps exactly right; what it cannot do
 * is let the content flow from one column into the next, so the transformation is only
 * made where the document itself says where the columns divide - a {@code w:br
 * w:type="column"} for each boundary. That is how Word's own documents are written
 * (thirteen of the eighteen unequal-column documents across the two corpora carry exactly
 * one column break per two-column stretch); the rest are left as equal columns.</p>
 *
 * <p>The table takes no borders, no cell margins and no indent, so the only geometry it
 * adds is the columns themselves: a spacer column carries each {@code w:col/@w:space}.</p>
 *
 * @since 17.0.6
 */
class UnequalColumns {

	private static final Logger log = LoggerFactory.getLogger(UnequalColumns.class);

	/** Columns within this ratio of each other are Word's own rounding of equal columns
	 *  (4716/4715, 4680/4860 and the like in the corpora), and are left to the region
	 *  body, whose balancing is what Word does with them. */
	private static final double EQUAL_ENOUGH = 1.05;

	private static final ObjectFactory F = Context.getWmlObjectFactory();

	private UnequalColumns() {}

	/**
	 * @param content the section's (or merged part's) content
	 * @param sectPr the section properties whose w:cols may declare unequal columns
	 * @return the content as a single one-row table, or null to leave it alone
	 */
	static List<Object> asOneRowTable(List<Object> content, SectPr sectPr) {

		int[][] columns = unequalColumns(sectPr);
		if (columns == null || content.isEmpty()) return null;
		int[] widths = columns[0], spaces = columns[1];

		List<List<Object>> groups = split(content, widths.length);
		if (groups == null) return null;

		Tbl tbl = F.createTbl();
		tbl.setTblPr(tblPr(total(widths, spaces)));
		TblGrid grid = F.createTblGrid();
		Tr tr = F.createTr();
		for (int i = 0; i < widths.length; i++) {
			grid.getGridCol().add(gridCol(widths[i]));
			tr.getContent().add(cell(widths[i], groups.get(i)));
			if (i + 1 < widths.length && spaces[i] > 0) {
				grid.getGridCol().add(gridCol(spaces[i]));
				tr.getContent().add(cell(spaces[i], null));
			}
		}
		tbl.setTblGrid(grid);
		tbl.getContent().add(tr);
		if (log.isDebugEnabled()) {
			log.debug("unequal columns rendered as a one-row table: " + widths.length + " columns");
		}
		List<Object> result = new ArrayList<Object>(1);
		result.add(tbl);
		return result;
	}

	/**
	 * @return { the w:col widths, the w:col spaces } in twips, or null where the section
	 *         has no unequal columns worth the transformation
	 */
	private static int[][] unequalColumns(SectPr sectPr) {
		if (sectPr == null) return null;
		CTColumns cols = sectPr.getCols();
		if (cols == null || cols.isEqualWidth()) return null;
		List<CTColumn> declared = cols.getCol();
		if (declared == null || declared.size() < 2) return null;
		if (cols.getNum() != null && cols.getNum().intValue() != declared.size()) return null;
		int[] widths = new int[declared.size()];
		int[] spaces = new int[declared.size()];
		int min = Integer.MAX_VALUE, max = 0;
		for (int i = 0; i < declared.size(); i++) {
			if (declared.get(i).getW() == null) return null;
			widths[i] = declared.get(i).getW().intValue();
			if (widths[i] <= 0) return null;
			spaces[i] = declared.get(i).getSpace() == null ? 0 : declared.get(i).getSpace().intValue();
			min = Math.min(min, widths[i]);
			max = Math.max(max, widths[i]);
		}
		if (max <= min * EQUAL_ENOUGH) return null;
		return new int[][] { widths, spaces };
	}

	/**
	 * Split the content into as many groups as there are columns, at the paragraphs
	 * carrying a column break.
	 *
	 * @return null where the content does not carry exactly one break per boundary, or
	 *         where a group would be empty
	 */
	private static List<List<Object>> split(List<Object> content, int columns) {
		List<List<Object>> groups = new ArrayList<List<Object>>();
		List<Object> current = new ArrayList<Object>();
		for (Object o : content) {
			Object unwrapped = XmlUtils.unwrap(o);
			if (!(unwrapped instanceof P) || columnBreaks((P) unwrapped) == 0) {
				current.add(o);
				continue;
			}
			P p = (P) unwrapped;
			if (columnBreaks(p) > 1) return null; // more than one column starts here
			if (groups.size() + 1 >= columns) return null; // more breaks than boundaries
			// Word divides the paragraph at the break: what precedes it ends the column,
			// what follows it opens the next (measured on a letterhead whose address
			// block is one paragraph with the break in the middle of it)
			P before = XmlUtils.deepCopy(p);
			P after = XmlUtils.deepCopy(p);
			truncateAtColumnBreak(before, true);
			truncateAtColumnBreak(after, false);
			if (rendersSomething(before)) current.add(before);
			groups.add(current);
			current = new ArrayList<Object>();
			if (rendersSomething(after)) current.add(after);
		}
		groups.add(current);
		if (groups.size() != columns) return null;
		for (List<Object> group : groups) if (group.isEmpty()) return null;
		return groups;
	}

	private static int columnBreaks(P p) {
		int n = 0;
		for (Object child : p.getContent()) {
			Object o = XmlUtils.unwrap(child);
			if (!(o instanceof R)) continue;
			for (Object rChild : ((R) o).getContent()) {
				if (isColumnBreak(XmlUtils.unwrap(rChild))) n++;
			}
		}
		return n;
	}

	private static boolean isColumnBreak(Object o) {
		return o instanceof org.docx4j.wml.Br && STBrType.COLUMN.equals(((org.docx4j.wml.Br) o).getType());
	}

	/**
	 * Cut the paragraph at its column break, keeping what precedes it or what follows
	 * it; the break itself goes either way (it is the division, and left in it would
	 * take a line of its own).  The paragraph is a copy: the document is converted more
	 * than once, so nothing of it may be modified.
	 */
	private static void truncateAtColumnBreak(P p, boolean keepBefore) {
		List<Object> content = p.getContent();
		int runIndex = -1, breakIndex = -1;
		for (int i = 0; i < content.size() && runIndex < 0; i++) {
			Object o = XmlUtils.unwrap(content.get(i));
			if (!(o instanceof R)) continue;
			List<Object> runContent = ((R) o).getContent();
			for (int j = 0; j < runContent.size(); j++) {
				if (isColumnBreak(XmlUtils.unwrap(runContent.get(j)))) {
					runIndex = i;
					breakIndex = j;
					break;
				}
			}
		}
		if (runIndex < 0) return;
		R run = (R) XmlUtils.unwrap(content.get(runIndex));
		List<Object> runContent = run.getContent();
		if (keepBefore) {
			while (runContent.size() > breakIndex) runContent.remove(runContent.size() - 1);
			while (content.size() > runIndex + 1) content.remove(content.size() - 1);
		} else {
			for (int j = breakIndex; j >= 0; j--) {
				if (!(XmlUtils.unwrap(runContent.get(j)) instanceof org.docx4j.wml.RPr)) runContent.remove(j);
			}
			for (int i = runIndex - 1; i >= 0; i--) {
				if (!(XmlUtils.unwrap(content.get(i)) instanceof org.docx4j.wml.PPr)) content.remove(i);
			}
		}
	}

	/** Whether the paragraph has a run with something in it, or any other child that
	 *  renders (a hyperlink, an sdt, a bookmark is not one). */
	private static boolean rendersSomething(P p) {
		for (Object child : p.getContent()) {
			Object o = XmlUtils.unwrap(child);
			if (o instanceof org.docx4j.wml.PPr) continue;
			if (!(o instanceof R)) return true;
			for (Object rChild : ((R) o).getContent()) {
				if (!(XmlUtils.unwrap(rChild) instanceof org.docx4j.wml.RPr)) return true;
			}
		}
		return false;
	}

	private static int total(int[] widths, int[] spaces) {
		int sum = 0;
		for (int i = 0; i < widths.length; i++) {
			sum += widths[i];
			if (i + 1 < widths.length) sum += spaces[i];
		}
		return sum;
	}

	private static TblPr tblPr(int widthTwips) {
		TblPr tblPr = F.createTblPr();
		tblPr.setTblW(width(widthTwips));
		tblPr.setTblInd(width(0));
		CTTblLayoutType layout = F.createCTTblLayoutType();
		layout.setType(STTblLayoutType.FIXED);
		tblPr.setTblLayout(layout);
		CTTblCellMar margins = F.createCTTblCellMar();
		margins.setLeft(width(0));
		margins.setRight(width(0));
		margins.setTop(width(0));
		margins.setBottom(width(0));
		tblPr.setTblCellMar(margins);
		return tblPr;
	}

	private static TblWidth width(int twips) {
		TblWidth w = F.createTblWidth();
		w.setType("dxa");
		w.setW(BigInteger.valueOf(twips));
		return w;
	}

	private static TblGridCol gridCol(int twips) {
		TblGridCol col = F.createTblGridCol();
		col.setW(BigInteger.valueOf(twips));
		return col;
	}

	/** @param content null for a spacer column, which still needs a paragraph in it */
	private static Tc cell(int twips, List<Object> content) {
		Tc tc = F.createTc();
		TcPr tcPr = F.createTcPr();
		tcPr.setTcW(width(twips));
		tc.setTcPr(tcPr);
		if (content == null || content.isEmpty()) {
			tc.getContent().add(F.createP());
		} else {
			tc.getContent().addAll(content);
			if (!(XmlUtils.unwrap(content.get(content.size() - 1)) instanceof P)) {
				tc.getContent().add(F.createP()); // a cell must end with a paragraph
			}
		}
		return tc;
	}
}
