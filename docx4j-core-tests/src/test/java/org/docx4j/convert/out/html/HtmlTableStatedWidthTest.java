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
package org.docx4j.convert.out.html;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.io.ByteArrayOutputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.docx4j.Docx4J;
import org.docx4j.XmlUtils;
import org.docx4j.convert.out.HTMLSettings;
import org.docx4j.openpackaging.packages.WordprocessingMLPackage;
import org.docx4j.wml.Body;
import org.junit.Test;

/**
 * A table whose own {@code w:tblW} contradicts its {@code w:tblGrid} is written at that
 * width in HTML, its columns in the cells' proportions (CR-041 phase 2): the HTML writer
 * measures no content, so it takes the cells' widths scaled to the table's, which is what
 * Word's re-saves of the table-align probes show (F6, W1).  A grid that agrees, a wider
 * grid the cells do not repeat (F8), and a nested table's grid stand.  Both pathways.
 */
public class HtmlTableStatedWidthTest {

	private static final String W = "xmlns:w=\"http://schemas.openxmlformats.org/wordprocessingml/2006/main\"";

	private static final int[] FLAGS = { Docx4J.FLAG_NONE, Docx4J.FLAG_EXPORT_PREFER_XSL };

	private static String pathway(int flags) {
		return flags == Docx4J.FLAG_NONE ? "visitor: " : "XSLT: ";
	}

	private static String table(int tblW, int[] grid, int[] tcW) {
		StringBuilder sb = new StringBuilder("<w:tbl><w:tblPr><w:tblW w:w=\"" + tblW + "\" w:type=\"dxa\"/></w:tblPr><w:tblGrid>");
		for (int g : grid) sb.append("<w:gridCol w:w=\"" + g + "\"/>");
		sb.append("</w:tblGrid><w:tr>");
		for (int w : tcW) {
			sb.append("<w:tc><w:tcPr><w:tcW w:w=\"" + w + "\" w:type=\"dxa\"/></w:tcPr><w:p><w:r><w:t>x</w:t></w:r></w:p></w:tc>");
		}
		return sb.append("</w:tr></w:tbl>").toString();
	}

	private static String html(String body, int flags) throws Exception {
		WordprocessingMLPackage pkg = WordprocessingMLPackage.createPackage();
		pkg.getMainDocumentPart().getJaxbElement().setBody(
				(Body) XmlUtils.unwrap(XmlUtils.unmarshalString("<w:body " + W + ">" + body + "<w:p/></w:body>")));
		HTMLSettings settings = Docx4J.createHTMLSettings();
		settings.setOpcPackage(pkg);
		ByteArrayOutputStream os = new ByteArrayOutputStream();
		Docx4J.toHTML(settings, os, flags);
		return os.toString("UTF-8");
	}

	/** The n-th table's own CSS width, in twips. */
	private static double tableWidth(String html, int n) {
		String tag = tag(html, "<table", n);
		Matcher m = Pattern.compile("(?:^|[;\\s\"])width:\\s*([\\d.]+)(pt|in)").matcher(tag);
		assertTrue("no width: " + tag, m.find());
		return Double.parseDouble(m.group(1)) * ("in".equals(m.group(2)) ? 1440 : 20);
	}

	/** The n-th table's col widths, in per cent. */
	private static List<Double> colPercentages(String html, int n) {
		int at = indexOf(html, "<table", n);
		int end = html.indexOf("</colgroup>", at);
		List<Double> out = new ArrayList<>();
		Matcher m = Pattern.compile("<col[^>]*width:\\s*([\\d.]+)%").matcher(html.substring(at, end));
		while (m.find()) out.add(Double.parseDouble(m.group(1)));
		return out;
	}

	private static int indexOf(String html, String what, int n) {
		int at = -1;
		for (int i = 0; i <= n; i++) {
			at = html.indexOf(what, at + 1);
			assertTrue(what + " " + n, at >= 0);
		}
		return at;
	}

	private static String tag(String html, String what, int n) {
		int at = indexOf(html, what, n);
		return html.substring(at, html.indexOf('>', at));
	}

	/** F6: TblFactory's grid under a width set afterwards: the table's width, not 300pt. */
	@Test
	public void aGridItsWidthContradictsGivesWay() throws Exception {
		String body = table(9026, new int[] { 2000, 2000, 2000 }, new int[] { 2000, 2000, 2000 });
		for (int flags : FLAGS) {
			String html = html(body, flags);
			assertEquals(pathway(flags), 9026, tableWidth(html, 0), 1);
			for (double p : colPercentages(html, 0)) assertEquals(pathway(flags), 33.33, p, 0.02);
		}
	}

	/** W1: the cells' proportions (3000 / 2000 / 1000), not the grid's (1000 / 2000 / 3000). */
	@Test
	public void theCellsProportionsNotTheGrids() throws Exception {
		String body = table(9026, new int[] { 1000, 2000, 3000 }, new int[] { 3000, 2000, 1000 });
		for (int flags : FLAGS) {
			String html = html(body, flags);
			assertEquals(pathway(flags), 9026, tableWidth(html, 0), 1);
			List<Double> cols = colPercentages(html, 0);
			assertEquals(pathway(flags), 3, cols.size());
			assertEquals(pathway(flags), 50.0, cols.get(0), 0.02);
			assertEquals(pathway(flags), 33.34, cols.get(1), 0.02);
			assertEquals(pathway(flags), 16.66, cols.get(2), 0.02);
		}
	}

	/** F8: cells that agree with the width under a wider grid: the grid stands. */
	@Test
	public void aWiderGridTheCellsDoNotRepeatStands() throws Exception {
		String body = table(6000, new int[] { 3009, 3009, 3009 }, new int[] { 2000, 2000, 2000 });
		for (int flags : FLAGS) {
			assertEquals(pathway(flags), 9027, tableWidth(html(body, flags), 0), 1);
		}
	}

	/** A table nested in a cell keeps its grid (2 x 2000 under a 5000-twip w:tblW). */
	@Test
	public void aNestedTableKeepsItsGrid() throws Exception {
		String inner = table(5000, new int[] { 2000, 2000 }, new int[] { 2000, 2000 });
		String body = "<w:tbl><w:tblPr><w:tblW w:w=\"9026\" w:type=\"dxa\"/></w:tblPr><w:tblGrid><w:gridCol w:w=\"9026\"/></w:tblGrid>"
				+ "<w:tr><w:tc><w:tcPr><w:tcW w:w=\"9026\" w:type=\"dxa\"/></w:tcPr><w:p/>" + inner + "<w:p/></w:tc></w:tr></w:tbl>";
		for (int flags : FLAGS) {
			assertEquals(pathway(flags), 4000, tableWidth(html(body, flags), 1), 1);
		}
	}
}
