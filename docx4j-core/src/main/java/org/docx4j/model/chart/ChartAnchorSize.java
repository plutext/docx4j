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
package org.docx4j.model.chart;

import java.awt.Font;
import java.awt.font.FontRenderContext;
import java.awt.geom.Dimension2D;
import java.util.HashMap;
import java.util.Map;

import jakarta.xml.bind.JAXBElement;

import org.docx4j.XmlUtils;
import org.docx4j.dml.spreadsheetdrawing.CTMarker;
import org.docx4j.dml.spreadsheetdrawing.CTTwoCellAnchor;
import org.docx4j.openpackaging.packages.OpcPackage;
import org.docx4j.openpackaging.parts.Part;
import org.docx4j.openpackaging.parts.SpreadsheetML.Styles;
import org.docx4j.openpackaging.parts.SpreadsheetML.WorkbookPart;
import org.docx4j.openpackaging.parts.SpreadsheetML.WorksheetPart;
import org.docx4j.openpackaging.parts.relationships.RelationshipsPart;
import org.docx4j.org.apache.poi.util.Dimension2DDouble;
import org.docx4j.relationships.Relationship;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.xlsx4j.sml.CTFont;
import org.xlsx4j.sml.CTFontName;
import org.xlsx4j.sml.CTFontSize;
import org.xlsx4j.sml.CTSheetFormatPr;
import org.xlsx4j.sml.Col;
import org.xlsx4j.sml.Cols;
import org.xlsx4j.sml.Row;
import org.xlsx4j.sml.Worksheet;

/**
 * The size of a chart an xlsx two-cell anchor places: the cells from its {@code xdr:from} to
 * its {@code xdr:to}, plus and less their offsets.  A column's width is in characters of the
 * workbook's Normal font's widest digit, to whole pixels (ECMA-376 Part 1, 18.3.1.13), a
 * column with none the sheet's default ({@code defaultColWidth}, else {@code baseColWidth}
 * digits and 5 pixels, rounded up to 8 pixels as Excel does); a row's height its own, else the
 * sheet's default; hidden ones nothing.
 *
 * <p>Excel lays the sheet out in pixels of the screen it shows it on, so the size is given for
 * a number of dots an inch, 96 (a screen at 100%) unless asked.  Measured (CR-037 phase 3, Excel
 * 365, ten probes on screens at 96 and 120): the width exactly Excel's on all ten at 120 and nine
 * at 96, the tenth GDI's hinting (Calibri 14 at 19 pixels has 10-pixel digits, its advances 9.6).
 * A height set by hand is Excel's at both, in whole pixels.  A row's height that is not set by
 * hand Excel takes from the Normal font at the screen (Aptos Narrow 11: 15pt at 96, 14.4 at
 * 120), which the file can't say: it is taken as the file gives it, Excel's at the screen that
 * saved it.</p>
 *
 * @since 17.3.3 (CR-037 phase 3)
 */
final class ChartAnchorSize {

	private static final Logger log = LoggerFactory.getLogger(ChartAnchorSize.class);

	private static final double EMU_PER_PT = 12700;
	private static final double EMU_PER_INCH = 914400;

	private ChartAnchorSize() {
	}

	/**
	 * At dpi pixels an inch (96: ECMA-376's, a screen at 100%)
	 *
	 * @return null where the chart's anchor isn't a two-cell anchor, or its sheet isn't found
	 */
	static Dimension2D of(ChartRef chart, double dpi) {
		Object a = XmlUtils.unwrap(chart.getAnchor());
		if (!(a instanceof CTTwoCellAnchor)) return null;
		CTMarker from = ((CTTwoCellAnchor) a).getFrom(), to = ((CTTwoCellAnchor) a).getTo();
		if (from == null || to == null) return null;
		WorksheetPart sheet = sheetOf(chart.getHost());
		if (sheet == null) {
			log.warn("The sheet of the drawing " + chart.getHost().getPartName() + " isn't found");
			return null;
		}
		try {
			Worksheet ws = sheet.getContents();
			int mdw = maxDigitWidth(chart.getHost(), dpi);
			// in EMUs, whole numbers as the anchor's offsets are
			double emuPerPx = EMU_PER_INCH / dpi;
			long w = to.getColOff() - from.getColOff();
			for (int c = from.getCol(); c < to.getCol(); c++) w += Math.round(columnPx(ws, c, mdw) * emuPerPx);
			Map<Long, Row> rows = rows(ws);
			long h = to.getRowOff() - from.getRowOff();
			for (int r = from.getRow(); r < to.getRow(); r++) h += Math.round(rowPt(ws, rows.get((long) r + 1), dpi) * EMU_PER_PT);
			return w > 0 && h > 0 ? new Dimension2DDouble(w / EMU_PER_PT, h / EMU_PER_PT) : null;
		} catch (Exception e) {
			log.warn("Couldn't size the chart " + chart.getName() + " from its cells: " + e.getMessage());
			return null;
		}
	}

	/** The worksheet whose drawing the part is */
	private static WorksheetPart sheetOf(Part drawing) {
		OpcPackage pkg = drawing.getPackage();
		if (pkg == null) return null;
		for (Part p : pkg.getParts().getParts().values()) {
			if (!(p instanceof WorksheetPart)) continue;
			RelationshipsPart rp = p.getRelationshipsPart();
			if (rp == null) continue;
			for (Relationship r : rp.getRelationships().getRelationship()) {
				if (rp.getPart(r) == drawing) return (WorksheetPart) p;
			}
		}
		return null;
	}

	/** column c (from 0), in pixels */
	private static double columnPx(Worksheet ws, int c, int mdw) {
		long n = c + 1;
		for (Cols cols : ws.getCols()) {
			for (Col col : cols.getCol()) {
				if (n < col.getMin() || n > col.getMax()) continue;
				if (col.isHidden()) return 0;
				if (col.getWidth() != null) return widthPx(col.getWidth(), mdw);
			}
		}
		CTSheetFormatPr f = ws.getSheetFormatPr();
		if (f != null && f.getDefaultColWidth() != null) return widthPx(f.getDefaultColWidth(), mdw);
		long base = f == null ? 8 : f.getBaseColWidth();
		long px = base * mdw + 5;
		return 8 * ((px + 7) / 8);
	}

	/** a width in characters (padding included), in whole pixels (ECMA-376 18.3.1.13) */
	private static double widthPx(double chars, int mdw) {
		return Math.floor((256 * chars + Math.floor(128.0 / mdw)) / 256 * mdw);
	}

	private static Map<Long, Row> rows(Worksheet ws) {
		Map<Long, Row> m = new HashMap<>();
		if (ws.getSheetData() == null) return m;
		for (Row r : ws.getSheetData().getRow()) {
			if (r.getR() != null) m.put(r.getR(), r);
		}
		return m;
	}

	/**
	 * A row's height: its own, else the sheet's default, as the file gives them (Excel's at the
	 * screen that saved it); a height set by hand ({@code customHeight}) in whole pixels of the
	 * screen, down, as Excel draws it (20pt: 19.5 at 96, 19.8 at 120)
	 */
	private static double rowPt(Worksheet ws, Row row, double dpi) {
		if (row != null && row.isHidden()) return 0;
		if (row != null && row.getHt() != null) return row.isCustomHeight() ? pixels(row.getHt(), dpi) : row.getHt();
		CTSheetFormatPr f = ws.getSheetFormatPr();
		if (f != null && f.isZeroHeight()) return 0;
		if (f == null || !(f.getDefaultRowHeight() > 0)) return 15;
		return f.isCustomHeight() ? pixels(f.getDefaultRowHeight(), dpi) : f.getDefaultRowHeight();
	}

	/** points, down to whole pixels at dpi */
	private static double pixels(double pt, double dpi) {
		return Math.floor(pt * dpi / 72 + 1e-9) * 72 / dpi;
	}

	/**
	 * The widest digit of the workbook's Normal font (its first), in whole pixels at dpi: 7 at 96
	 * for Calibri 11 and for Aptos Narrow 11, 9 at 120 (measured: Excel's columns at 120)
	 */
	private static int maxDigitWidth(Part drawing, double dpi) {
		String face = "Calibri";
		double size = 11;
		try {
			OpcPackage pkg = drawing.getPackage();
			WorkbookPart wb = pkg instanceof org.docx4j.openpackaging.packages.SpreadsheetMLPackage
					? ((org.docx4j.openpackaging.packages.SpreadsheetMLPackage) pkg).getWorkbookPart() : null;
			Styles styles = wb == null ? null : wb.getStylesPart();
			if (styles != null && styles.getJaxbElement().getFonts() != null
					&& !styles.getJaxbElement().getFonts().getFont().isEmpty()) {
				CTFont font = styles.getJaxbElement().getFonts().getFont().get(0);
				for (JAXBElement<?> e : font.getNameOrCharsetOrFamily()) {
					Object v = e.getValue();
					if (v instanceof CTFontName && ((CTFontName) v).getVal() != null) face = ((CTFontName) v).getVal();
					else if (v instanceof CTFontSize) size = ((CTFontSize) v).getVal();
				}
			}
			Font f = ChartFonts.of(drawing).font(face, false, false, size * dpi / 72);
			FontRenderContext frc = new FontRenderContext(null, true, true);
			double widest = 0;
			for (char d = '0'; d <= '9'; d++) {
				widest = Math.max(widest, f.getStringBounds(String.valueOf(d), frc).getWidth());
			}
			int mdw = (int) Math.round(widest);
			return mdw > 0 ? mdw : 7;
		} catch (Exception e) {
			log.debug("The Normal font's digits unmeasured, so 7 pixels: " + e.getMessage());
			return 7;
		}
	}
}
