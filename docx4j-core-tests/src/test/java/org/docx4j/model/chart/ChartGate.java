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

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.docx4j.XmlUtils;
import org.docx4j.openpackaging.packages.OpcPackage;
import org.docx4j.openpackaging.packages.SpreadsheetMLPackage;
import org.docx4j.openpackaging.parts.Part;
import org.docx4j.openpackaging.parts.DrawingML.Chart;
import org.docx4j.openpackaging.parts.SpreadsheetML.TablePart;
import org.docx4j.openpackaging.parts.WordprocessingML.EmbeddedPackagePart;
import org.docx4j.relationships.Relationship;
import org.xlsx4j.model.SheetCells;
import org.xlsx4j.model.Tables;
import org.xlsx4j.sml.CTTable;
import org.xlsx4j.sml.CTTableColumn;
import org.xlsx4j.sml.Cell;
import org.xlsx4j.sml.Row;

/**
 * The comparison of CR-035's and CR-036's chart gates (CR-036 review A5): a chart part and the
 * workbook its data is in, each as a text that Office's file and docx4j's can be compared by.
 *
 * <ul>
 * <li>The chart part as docx4j marshals it (so both sides go through the same writer), each cached
 * number in one form (Excel writes 2.2 as 2.2000000000000002), and on request the random parts of
 * Office's ids set aside: the {@code c16:uniqueId} suffixes (Word gives every point's id a fresh
 * one when a pie's data is edited) and the axis ids.</li>
 * <li>A worksheet's dimension, rows (spans, descent) and cells (type, style, value by its text, so
 * the shared strings' order doesn't count; formulas on request).</li>
 * <li>A worksheet's tables: ref, autoFilter, columns (id, name; that each has a uid).</li>
 * </ul>
 * What docx4j's own writing changes in Office's XML (booleans as true/false, namespace
 * declarations) is the same on both sides, and is measured by each gate's no-edit control.
 */
final class ChartGate {

	private ChartGate() {
	}

	static OpcPackage load(String resource) throws Exception {
		try (InputStream is = ChartGate.class.getClassLoader().getResourceAsStream("charts/" + resource)) {
			if (is == null) throw new IllegalArgumentException("no fixture " + resource);
			return OpcPackage.load(is);
		}
	}

	/** A package saved and loaded again, as a reader of the saved file would see it */
	static OpcPackage roundTrip(OpcPackage pkg) throws Exception {
		ByteArrayOutputStream os = new ByteArrayOutputStream();
		pkg.save(os);
		return OpcPackage.load(new ByteArrayInputStream(os.toByteArray()));
	}

	/** The package's first chart part (by part name) */
	static Chart chart(OpcPackage pkg) {
		Chart first = null;
		for (Part p : pkg.getParts().getParts().values()) {
			if (p instanceof Chart && (first == null
					|| p.getPartName().getName().compareTo(first.getPartName().getName()) < 0)) {
				first = (Chart) p;
			}
		}
		return first;
	}

	/** The workbook a chart's data is in: its own package's, or a copy of the embedded one */
	static SpreadsheetMLPackage workbook(Chart c) throws Exception {
		if (c.getPackage() instanceof SpreadsheetMLPackage) return (SpreadsheetMLPackage) c.getPackage();
		Relationship r = c.getRelationshipsPart().getRelationshipByID(c.getContents().getExternalData().getId());
		return (SpreadsheetMLPackage) ((EmbeddedPackagePart) c.getRelationshipsPart().getPart(r)).extractPackage();
	}

	private static final Pattern V = Pattern.compile("<c:v>([^<]*)</c:v>");
	private static final Pattern UNIQUE_ID = Pattern.compile("(uniqueId[^>]*val=\"\\{[0-9A-Fa-f]{8})-[^}]*\\}");
	private static final Pattern AX_ID = Pattern.compile("<c:(axId|crossAx) val=\"\\d+\"/>");

	/** The chart part, each cached number in one form; Office's random ids aside where asked */
	static String chartXml(Chart c, boolean idsAside) throws Exception {
		String xml = XmlUtils.marshaltoString(c.getJaxbElement(), true, true);
		Matcher m = V.matcher(xml);
		StringBuffer sb = new StringBuffer();
		while (m.find()) {
			String v = m.group(1);
			try {
				v = Double.toString(Double.parseDouble(v));
			} catch (NumberFormatException e) {
				// text
			}
			m.appendReplacement(sb, Matcher.quoteReplacement("<c:v>" + v + "</c:v>"));
		}
		m.appendTail(sb);
		String out = sb.toString();
		if (idsAside) {
			out = UNIQUE_ID.matcher(out).replaceAll("$1-SUFFIX}");
			out = AX_ID.matcher(out).replaceAll("<c:$1 val=\"ID\"/>");
		}
		return out;
	}

	/** Sheet1's dimension, rows (spans) and cells (type, style, value by its text, formula on request) */
	static String cells(SpreadsheetMLPackage pkg, boolean formulas) throws Exception {
		return cells(pkg, formulas, true);
	}

	/**
	 * The same, the sheet's extent hints (its {@code dimension}, the rows' {@code spans}) left out
	 * where asked: Word's and PowerPoint's save of a chart's workbook leaves them at the larger
	 * extent after cells are removed (CR-036 phase 1), where Excel's narrows them
	 */
	static String cells(SpreadsheetMLPackage pkg, boolean formulas, boolean extentHints) throws Exception {
		SheetCells sc = SheetCells.of(pkg, "Sheet1");
		StringBuilder sb = new StringBuilder(extentHints ? "dimension " + sc.getPart().getContents().getDimension().getRef() + "\n" : "");
		for (Row r : sc.getPart().getContents().getSheetData().getRow()) {
			sb.append("row ").append(r.getR()).append(extentHints ? " spans " + r.getSpans() : "").append(" dyDescent ")
					.append(r.getDyDescent()).append('\n');
			for (Cell c : r.getC()) {
				String v = sc.getText(c);
				if (SheetCells.isNumber(c)) v = Double.toString(Double.parseDouble(v));
				sb.append("  ").append(c.getR()).append(" t=").append(c.getT() == null ? "n" : c.getT().value())
						.append(" s=").append(c.getS()).append(" v=").append(v);
				if (formulas && c.getF() != null) sb.append(" f=").append(c.getF().getValue());
				sb.append('\n');
			}
		}
		return sb.toString();
	}

	/** Sheet1's tables: ref, autoFilter, columns (id, name; a uid each) */
	static String table(SpreadsheetMLPackage pkg) throws Exception {
		SheetCells sc = SheetCells.of(pkg, "Sheet1");
		StringBuilder sb = new StringBuilder();
		for (TablePart t : Tables.of(sc.getPart())) {
			CTTable ct = t.getContents();
			sb.append(ct.getName()).append(' ').append(ct.getRef()).append(" autoFilter ")
					.append(ct.getAutoFilter() == null ? null : ct.getAutoFilter().getRef()).append(" count ")
					.append(ct.getTableColumns().getCount()).append('\n');
			for (CTTableColumn c : ct.getTableColumns().getTableColumn()) {
				sb.append("  ").append(c.getId()).append(" '").append(c.getName()).append("' uid ")
						.append(c.getUid() != null).append('\n');
			}
		}
		return sb.toString();
	}
}
