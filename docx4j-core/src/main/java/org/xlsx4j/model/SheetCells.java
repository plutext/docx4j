/*
 *  Copyright 2026, Plutext Pty Ltd.
 *
 *  This file is part of docx4j.
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */
package org.xlsx4j.model;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.docx4j.XmlUtils;
import org.docx4j.openpackaging.exceptions.Docx4JException;
import org.docx4j.openpackaging.exceptions.InvalidFormatException;
import org.docx4j.openpackaging.packages.SpreadsheetMLPackage;
import org.docx4j.openpackaging.parts.Part;
import org.docx4j.openpackaging.parts.SpreadsheetML.CalcChain;
import org.docx4j.openpackaging.parts.SpreadsheetML.SharedStrings;
import org.docx4j.openpackaging.parts.SpreadsheetML.Styles;
import org.docx4j.openpackaging.parts.SpreadsheetML.WorkbookPart;
import org.docx4j.openpackaging.parts.SpreadsheetML.WorksheetPart;
import org.docx4j.relationships.Relationship;
import org.xlsx4j.org.apache.poi.ss.usermodel.BuiltinFormats;
import org.xlsx4j.sml.CTCalcPr;
import org.xlsx4j.sml.CTNumFmt;
import org.xlsx4j.sml.CTNumFmts;
import org.xlsx4j.sml.CTRElt;
import org.xlsx4j.sml.CTRst;
import org.xlsx4j.sml.CTSheetDimension;
import org.xlsx4j.sml.CTSst;
import org.xlsx4j.sml.CTStylesheet;
import org.xlsx4j.sml.CTXf;
import org.xlsx4j.sml.CTXstringWhitespace;
import org.xlsx4j.sml.Cell;
import org.xlsx4j.sml.Row;
import org.xlsx4j.sml.STCellType;
import org.xlsx4j.sml.Sheet;
import org.xlsx4j.sml.Worksheet;

/**
 * The cells of one worksheet, to read and write as a chart's data needs them (CR-035 phase 4):
 * a cell found or created with its row, each in order; a number, a shared string or an error
 * written; a cell cleared; the cells of a block moved up or left over a deleted row or column.
 * Afterwards {@link #tidy()} sets what Excel keeps in step with the cells: the sheet's
 * {@code dimension}, each row's {@code spans}, the shared strings' counts.  Not a general cell
 * API: no formulas are written, and no styles made but a number format's.
 *
 * <p>A cell cleared loses its element, as Excel's does, unless it has a style, which it keeps.
 * A number is written in its shortest form (Excel writes some with 17 significant digits; both
 * read back as the same number).
 *
 * @since 17.3.3 (CR-035)
 */
public final class SheetCells {

	private final SpreadsheetMLPackage workbook;
	private final WorksheetPart part;
	private final String name;
	private Map<String, Integer> stringIndex;
	private boolean stringsChanged, formulaRemoved;

	public SheetCells(SpreadsheetMLPackage workbook, WorksheetPart part, String name) {
		this.workbook = workbook;
		this.part = part;
		this.name = name;
	}

	/** The cells of the sheet of that name; null where the workbook has none */
	public static SheetCells of(SpreadsheetMLPackage workbook, String sheetName) throws Docx4JException {
		WorkbookPart wb = workbook.getWorkbookPart();
		for (Sheet s : wb.getContents().getSheets().getSheet()) {
			if (!s.getName().equals(sheetName)) continue;
			Part p = wb.getRelationshipsPart().getPart(s.getId());
			return p instanceof WorksheetPart ? new SheetCells(workbook, (WorksheetPart) p, s.getName()) : null;
		}
		return null;
	}

	public String getName() {
		return name;
	}

	public WorksheetPart getPart() {
		return part;
	}

	public SpreadsheetMLPackage getWorkbook() {
		return workbook;
	}

	private List<Row> rows() throws Docx4JException {
		return part.getContents().getSheetData().getRow();
	}

	// ------------------------------------------------------------------ finding

	/** The row numbered r; null for none */
	public Row getRow(int r) throws Docx4JException {
		for (Row row : rows()) if (row.getR() != null && row.getR() == r) return row;
		return null;
	}

	/** The cell, column and row from 1; null where there is none */
	public Cell getCell(int column, int row) throws Docx4JException {
		Row r = getRow(row);
		if (r == null) return null;
		String n = CellRef.columnName(column) + row;
		for (Cell c : r.getC()) if (n.equals(c.getR())) return c;
		return null;
	}

	/** The cell, made (and its row) where there is none, each in its place */
	public Cell cell(int column, int row) throws Docx4JException {
		List<Row> rows = rows();
		Row r = null;
		int at = rows.size();
		for (int i = 0; i < rows.size(); i++) {
			Long n = rows.get(i).getR();
			if (n == null) continue;
			if (n == row) {
				r = rows.get(i);
				break;
			}
			if (n > row) {
				at = i;
				break;
			}
		}
		if (r == null) {
			r = new Row();
			r.setR((long) row);
			// as Excel's new row: the descent of the row before it, else the sheet's default descent
			Row before = at > 0 ? rows.get(at - 1) : null;
			if (before != null && before.getDyDescent() != null) r.setDyDescent(before.getDyDescent());
			else if (before == null && part.getContents().getSheetFormatPr() != null) {
				r.setDyDescent(part.getContents().getSheetFormatPr().getDyDescent());
			}
			r.setParent(part.getContents().getSheetData());
			rows.add(at, r);
		}
		String n = CellRef.columnName(column) + row;
		List<Cell> cells = r.getC();
		int i = 0;
		for (; i < cells.size(); i++) {
			Cell c = cells.get(i);
			if (n.equals(c.getR())) return c;
			int[] cr = CellRef.cellName(c.getR());
			if (cr != null && cr[0] > column) break;
		}
		Cell c = new Cell();
		c.setR(n);
		c.setParent(r);
		cells.add(i, c);
		return c;
	}

	// ------------------------------------------------------------------ reading

	/**
	 * A cell's value as text: a string's text, a number as the cell holds it, an error its code
	 * (#N/A), a boolean TRUE or FALSE; null for an empty cell
	 */
	public String getText(Cell c) throws Docx4JException {
		if (c == null) return null;
		STCellType t = c.getT();
		if (t == STCellType.S) {
			try {
				return text(sst().getSi().get(Integer.parseInt(c.getV().trim())));
			} catch (RuntimeException e) {
				return null;
			}
		}
		if (t == STCellType.INLINE_STR) return c.getIs() == null ? null : text(c.getIs());
		if (t == STCellType.B) return c.getV() == null ? null : "1".equals(c.getV().trim()) ? "TRUE" : "FALSE";
		return c.getV() == null || c.getV().isEmpty() ? null : c.getV();
	}

	private static String text(CTRst r) {
		if (r.getT() != null) return r.getT().getValue();
		StringBuilder sb = new StringBuilder();
		for (CTRElt e : r.getR()) if (e.getT() != null) sb.append(e.getT().getValue());
		return sb.toString();
	}

	/** Whether the cell holds a number (as against text, an error or a boolean) */
	public static boolean isNumber(Cell c) {
		if (c == null || c.getV() == null) return false;
		if (c.getT() != null && c.getT() != STCellType.N) return false;
		try {
			Double.parseDouble(c.getV().trim());
			return true;
		} catch (NumberFormatException e) {
			return false;
		}
	}

	public static boolean hasFormula(Cell c) {
		return c != null && c.getF() != null;
	}

	/** The cell's number format code: the workbook's own, else the built-in one; General for none */
	public String getFormatCode(Cell c) throws Docx4JException {
		if (c == null) return "General";
		Styles styles = workbook.getWorkbookPart().getStylesPart();
		if (styles == null) return "General";
		CTStylesheet ss = styles.getContents();
		if (ss.getCellXfs() == null || c.getS() >= ss.getCellXfs().getXf().size()) return "General";
		CTXf xf = ss.getCellXfs().getXf().get((int) c.getS());
		long id = xf.getNumFmtId() == null ? 0 : xf.getNumFmtId();
		if (ss.getNumFmts() != null) {
			for (CTNumFmt f : ss.getNumFmts().getNumFmt()) if (f.getNumFmtId() == id) return f.getFormatCode();
		}
		String b = BuiltinFormats.getBuiltinFormat((int) id);
		return b == null ? "General" : b;
	}

	// ------------------------------------------------------------------ writing

	/** A number into the cell: its formula, if any, goes */
	public Cell setNumber(int column, int row, double value) throws Docx4JException {
		Cell c = cell(column, row);
		clearValue(c);
		c.setV(numberText(value));
		return c;
	}

	/** Text into the cell, as a shared string */
	public Cell setString(int column, int row, String value) throws Docx4JException {
		Cell c = cell(column, row);
		clearValue(c);
		c.setT(STCellType.S);
		c.setV(String.valueOf(stringIndex(value)));
		return c;
	}

	/**
	 * The cell given a number format (a date's, for a chart's dates), its style otherwise kept: a
	 * built-in format by its number, as Excel writes it ({@code m/d/yyyy} 14, {@code d-mmm} 16),
	 * else the workbook's own format, made where it has none; a style made where none has the
	 * cell's with this format (the workbook's styles made where it has none)
	 */
	public Cell setNumberFormat(int column, int row, String formatCode) throws Docx4JException {
		CTStylesheet ss = stylesheet();
		long id = numFmtId(ss, formatCode);
		Cell c = cell(column, row);
		List<CTXf> xfs = ss.getCellXfs().getXf();
		CTXf base = c.getS() < xfs.size() ? xfs.get((int) c.getS()) : xfs.get(0);
		long current = base.getNumFmtId() == null ? 0 : base.getNumFmtId();
		if (current == id) return c;
		for (int i = 0; i < xfs.size(); i++) {
			CTXf x = xfs.get(i);
			if (x.getNumFmtId() != null && x.getNumFmtId() == id && same(x.getFontId(), base.getFontId())
					&& same(x.getFillId(), base.getFillId()) && same(x.getBorderId(), base.getBorderId())
					&& same(x.getXfId(), base.getXfId()) && x.getAlignment() == null && base.getAlignment() == null
					&& x.getProtection() == null && base.getProtection() == null) {
				c.setS((long) i);
				return c;
			}
		}
		// as Excel's: the cell's style with the format, applyNumberFormat set
		CTXf x = new CTXf();
		x.setNumFmtId(id);
		x.setFontId(base.getFontId() == null ? 0L : base.getFontId());
		x.setFillId(base.getFillId() == null ? 0L : base.getFillId());
		x.setBorderId(base.getBorderId() == null ? 0L : base.getBorderId());
		x.setXfId(base.getXfId() == null ? 0L : base.getXfId());
		x.setApplyNumberFormat(true);
		if (base.getAlignment() != null) x.setAlignment(base.getAlignment());
		x.setParent(ss.getCellXfs());
		xfs.add(x);
		ss.getCellXfs().setCount((long) xfs.size());
		c.setS((long) xfs.size() - 1);
		return c;
	}

	private static boolean same(Long a, Long b) {
		return (a == null ? 0 : a) == (b == null ? 0 : b);
	}

	/**
	 * A format code's number: Excel's built-in one, where Excel spells it so (the codes of
	 * Excel's dates as its charts write them, a hyphen escaped, included), else the workbook's own,
	 * made where it has none
	 */
	private static long numFmtId(CTStylesheet ss, String formatCode) {
		Long builtin = builtinFormat(formatCode);
		if (builtin != null) return builtin;
		long max = BuiltinFormats.FIRST_USER_DEFINED_FORMAT_INDEX - 1;
		if (ss.getNumFmts() != null) {
			for (CTNumFmt f : ss.getNumFmts().getNumFmt()) {
				if (formatCode.equals(f.getFormatCode())) return f.getNumFmtId();
				max = Math.max(max, f.getNumFmtId());
			}
		} else {
			ss.setNumFmts(new CTNumFmts());
		}
		CTNumFmt f = new CTNumFmt();
		f.setNumFmtId(max + 1);
		f.setFormatCode(formatCode);
		f.setParent(ss.getNumFmts());
		ss.getNumFmts().getNumFmt().add(f);
		ss.getNumFmts().setCount((long) ss.getNumFmts().getNumFmt().size());
		return max + 1;
	}

	/** A built-in format's number: Excel's spelling of 14 (m/d/yyyy), a chart's escaped hyphens */
	static Long builtinFormat(String formatCode) {
		if ("m/d/yyyy".equals(formatCode)) return 14L;
		int i = BuiltinFormats.getBuiltinFormat(formatCode.replace("\\-", "-"));
		return i < 0 ? null : (long) i;
	}

	/**
	 * A built-in format as Excel's charts write it (CR-036 phase 2: {@code m/d/yyyy} for 14,
	 * {@code d\-mmm} for 16, a hyphen escaped); a workbook's own format as it is
	 */
	public static String chartFormatCode(String formatCode) {
		Long builtin = builtinFormat(formatCode);
		if (builtin == null) return formatCode;
		if (builtin == 14) return "m/d/yyyy";
		return BuiltinFormats.getBuiltinFormat(builtin.intValue()).replace("-", "\\-");
	}

	/** The workbook's styles, made (as a new workbook of Excel's has them) where it has none */
	private CTStylesheet stylesheet() throws Docx4JException {
		Styles styles = workbook.getWorkbookPart().getStylesPart();
		if (styles == null) {
			try {
				styles = new Styles();
			} catch (InvalidFormatException e) {
				throw new Docx4JException(e.getMessage(), e);
			}
			try {
				styles.setJaxbElement((CTStylesheet) XmlUtils.unmarshalString(MINIMAL_STYLES, org.xlsx4j.jaxb.Context.jcSML, CTStylesheet.class));
			} catch (jakarta.xml.bind.JAXBException e) {
				throw new Docx4JException(e.getMessage(), e);
			}
			workbook.getWorkbookPart().addTargetPart(styles);
		}
		CTStylesheet ss = styles.getContents();
		if (ss.getCellXfs() == null || ss.getCellXfs().getXf().isEmpty()) {
			throw new IllegalStateException("the workbook's styles have no cellXfs");
		}
		return ss;
	}

	private static final String MINIMAL_STYLES = "<styleSheet xmlns=\"http://schemas.openxmlformats.org/spreadsheetml/2006/main\">"
			+ "<fonts count=\"1\"><font><sz val=\"11\"/><color theme=\"1\"/><name val=\"Calibri\"/><family val=\"2\"/>"
			+ "<scheme val=\"minor\"/></font></fonts>"
			+ "<fills count=\"2\"><fill><patternFill patternType=\"none\"/></fill><fill><patternFill patternType=\"gray125\"/></fill></fills>"
			+ "<borders count=\"1\"><border><left/><right/><top/><bottom/><diagonal/></border></borders>"
			+ "<cellStyleXfs count=\"1\"><xf numFmtId=\"0\" fontId=\"0\" fillId=\"0\" borderId=\"0\"/></cellStyleXfs>"
			+ "<cellXfs count=\"1\"><xf numFmtId=\"0\" fontId=\"0\" fillId=\"0\" borderId=\"0\" xfId=\"0\"/></cellXfs>"
			+ "<cellStyles count=\"1\"><cellStyle name=\"Normal\" xfId=\"0\" builtinId=\"0\"/></cellStyles>"
			+ "<dxfs count=\"0\"/><tableStyles count=\"0\" defaultTableStyle=\"TableStyleMedium2\" defaultPivotStyle=\"PivotStyleLight16\"/>"
			+ "</styleSheet>";

	/** An error value (#N/A, #DIV/0!, ...) into the cell, as a constant */
	public Cell setError(int column, int row, String code) throws Docx4JException {
		Cell c = cell(column, row);
		clearValue(c);
		c.setT(STCellType.E);
		c.setV(code);
		return c;
	}

	/** The cell emptied: its element removed, or kept with only its style where it has one */
	public void clear(int column, int row) throws Docx4JException {
		Cell c = getCell(column, row);
		if (c == null) return;
		clearValue(c);
		if (c.getS() == 0) remove(c);
	}

	private void clearValue(Cell c) {
		if (c.getF() != null) formulaRemoved = true;
		c.setF(null);
		c.setT(null);
		c.setV(null);
		c.setIs(null);
	}

	private void remove(Cell c) throws Docx4JException {
		int[] cr = CellRef.cellName(c.getR());
		Row r = cr == null ? null : getRow(cr[1]);
		if (r == null) return;
		r.getC().remove(c);
		// as Excel's: a row left with no cells goes, unless something about it is its own
		if (r.getC().isEmpty() && r.getHt() == null && !r.isCustomFormat() && !r.isHidden() && r.getOutlineLevel() == 0
				&& !r.isCollapsed()) {
			rows().remove(r);
		}
	}

	/** The text a number is written as: its shortest form, without an exponent where it can be */
	public static String numberText(double v) {
		if (Double.isNaN(v) || Double.isInfinite(v)) throw new IllegalArgumentException("not a number a cell can hold: " + v);
		if (v == Math.rint(v) && Math.abs(v) < 1e15) return String.valueOf((long) v);
		String s = Double.toString(v);
		double a = Math.abs(v);
		if (a >= 1e-5 && a < 1e15) return new BigDecimal(s).stripTrailingZeros().toPlainString();
		return s.replace(".0E", "E");
	}

	/**
	 * The cells of {@code block} from {@code row} down moved up a row, over the cells of that row,
	 * its last row emptied: as Excel's table loses a row.  Cells outside the block's columns stay.
	 */
	public void deleteRow(AreaRef block, int row) throws Docx4JException {
		for (int c = block.getFirst().getColumn(); c <= block.getLast().getColumn(); c++) {
			clear(c, row);
			for (int r = row + 1; r <= block.getLast().getRow(); r++) move(c, r, c, r - 1);
		}
	}

	/** The cells of {@code block} right of {@code column} moved left a column, as Excel's table loses a column */
	public void deleteColumn(AreaRef block, int column) throws Docx4JException {
		for (int r = block.getFirst().getRow(); r <= block.getLast().getRow(); r++) {
			clear(column, r);
			for (int c = column + 1; c <= block.getLast().getColumn(); c++) move(c, r, c - 1, r);
		}
	}

	/** One cell's value and style to another (empty) place */
	private void move(int fromColumn, int fromRow, int toColumn, int toRow) throws Docx4JException {
		Cell from = getCell(fromColumn, fromRow);
		if (from == null) return;
		Cell to = cell(toColumn, toRow);
		to.setS(from.getS() == 0 ? null : from.getS());
		to.setT(from.getT());
		to.setV(from.getV());
		to.setIs(from.getIs());
		to.setF(from.getF());
		from.setF(null);
		from.setS(null);
		clear(fromColumn, fromRow);
	}

	// ------------------------------------------------------------------ shared strings

	private CTSst sst() throws Docx4JException {
		SharedStrings ss = workbook.getWorkbookPart().getSharedStrings();
		if (ss == null) {
			try {
				ss = new SharedStrings();
			} catch (InvalidFormatException e) {
				throw new Docx4JException(e.getMessage(), e);
			}
			ss.setJaxbElement(new CTSst());
			workbook.getWorkbookPart().addTargetPart(ss);
		}
		return ss.getContents();
	}

	private int stringIndex(String value) throws Docx4JException {
		List<CTRst> si = sst().getSi();
		if (stringIndex == null) {
			stringIndex = new HashMap<String, Integer>();
			for (int i = 0; i < si.size(); i++) {
				CTRst r = si.get(i);
				if (r.getT() != null && r.getR().isEmpty() && r.getRPh().isEmpty()) stringIndex.putIfAbsent(r.getT().getValue(), i);
			}
		}
		Integer i = stringIndex.get(value);
		if (i != null) return i;
		CTRst r = new CTRst();
		CTXstringWhitespace t = new CTXstringWhitespace();
		t.setValue(value);
		if (!value.equals(value.trim()) || value.contains("\n")) t.setSpace("preserve");
		r.setT(t);
		si.add(r);
		stringIndex.put(value, si.size() - 1);
		stringsChanged = true;
		return si.size() - 1;
	}

	// ------------------------------------------------------------------ afterwards

	/**
	 * What Excel keeps in step with the cells: the sheet's {@code dimension} (its cells' extent),
	 * each row's {@code spans} where the sheet writes them (the extent of its block of 16 rows,
	 * as Excel writes it), the shared strings' {@code count} and {@code uniqueCount}; and, where a
	 * formula was overwritten, the workbook's calculation chain dropped (Excel rebuilds it).
	 */
	public void tidy() throws Docx4JException {
		Worksheet ws = part.getContents();
		int minC = Integer.MAX_VALUE, minR = Integer.MAX_VALUE, maxC = 0, maxR = 0;
		boolean spans = false;
		Map<Integer, int[]> blocks = new HashMap<Integer, int[]>();
		for (Row row : rows()) {
			if (!row.getSpans().isEmpty()) spans = true;
			for (Cell c : row.getC()) {
				int[] cr = CellRef.cellName(c.getR());
				if (cr == null) continue;
				minC = Math.min(minC, cr[0]);
				maxC = Math.max(maxC, cr[0]);
				minR = Math.min(minR, cr[1]);
				maxR = Math.max(maxR, cr[1]);
				int[] b = blocks.computeIfAbsent((cr[1] - 1) / 16, k -> new int[] { Integer.MAX_VALUE, 0 });
				b[0] = Math.min(b[0], cr[0]);
				b[1] = Math.max(b[1], cr[0]);
			}
		}
		if (ws.getDimension() == null) ws.setDimension(new CTSheetDimension());
		ws.getDimension().setRef(maxR == 0 ? "A1"
				: new AreaRef(new CellRef(null, minC, minR), new CellRef(null, maxC, maxR)).formatLocal());
		if (spans) {
			for (Row row : rows()) {
				if (row.getR() == null) continue;
				int[] b = blocks.get((int) ((row.getR() - 1) / 16));
				row.getSpans().clear();
				if (b != null) row.getSpans().add(b[0] + ":" + b[1]);
			}
		}
		if (stringsChanged) {
			CTSst sst = sst();
			sst.setUniqueCount((long) sst.getSi().size());
			sst.setCount(stringCells());
			stringsChanged = false;
		}
		if (formulaRemoved) {
			WorkbookPart wb = workbook.getWorkbookPart();
			for (Relationship r : new ArrayList<Relationship>(wb.getRelationshipsPart().getRelationships().getRelationship())) {
				Part p = wb.getRelationshipsPart().getPart(r);
				if (p instanceof CalcChain) wb.getRelationshipsPart().removePart(p.getPartName());
			}
			formulaRemoved = false;
		}
	}

	/** Cells that are shared strings, in every worksheet */
	private long stringCells() throws Docx4JException {
		long n = 0;
		for (WorksheetPart w : worksheets(workbook)) {
			for (Row row : w.getContents().getSheetData().getRow()) {
				for (Cell c : row.getC()) if (c.getT() == STCellType.S) n++;
			}
		}
		return n;
	}

	static List<WorksheetPart> worksheets(SpreadsheetMLPackage workbook) throws Docx4JException {
		List<WorksheetPart> out = new ArrayList<WorksheetPart>();
		WorkbookPart wb = workbook.getWorkbookPart();
		for (Sheet s : wb.getContents().getSheets().getSheet()) {
			Part p = wb.getRelationshipsPart().getPart(s.getId());
			if (p instanceof WorksheetPart) out.add((WorksheetPart) p);
		}
		return out;
	}

	/** Whether any worksheet of the workbook has a formula */
	public static boolean hasFormulas(SpreadsheetMLPackage workbook) throws Docx4JException {
		for (WorksheetPart w : worksheets(workbook)) {
			for (Row row : w.getContents().getSheetData().getRow()) {
				for (Cell c : row.getC()) if (c.getF() != null) return true;
			}
		}
		return false;
	}

	/** {@code calcPr/@fullCalcOnLoad}: Excel recalculates every formula as it opens the workbook */
	public static void setFullCalcOnLoad(SpreadsheetMLPackage workbook) throws Docx4JException {
		org.xlsx4j.sml.Workbook wb = workbook.getWorkbookPart().getContents();
		if (wb.getCalcPr() == null) wb.setCalcPr(new CTCalcPr());
		wb.getCalcPr().setFullCalcOnLoad(true);
	}
}
