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
package org.docx4j.chart.render;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

import java.awt.geom.Dimension2D;
import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;

import org.docx4j.model.chart.ChartImages;
import org.docx4j.model.chart.ChartRef;
import org.docx4j.model.chart.Charts;
import org.docx4j.openpackaging.packages.OpcPackage;
import org.junit.Test;

/**
 * The size of a chart an xlsx two-cell anchor places, against Excel's (CR-037 phase 3):
 * {@code charts/sizes/}, Excel's own column chart with one thing changed in each
 * ({@code probes.tsv}; docx4j-layout-fidelity's {@code tools/chart_size_probes.py}), and the size
 * Excel 365 reported for each on the VM's screen at 120 dots an inch ({@code excel-sizes.tsv}) and
 * at 96 ({@code excel-sizes-96.tsv}), from {@code tools/excel-chart-size-check.ps1}, 2026-10-13.
 */
public class ChartAnchorSizeTest {

	/** Excel's sizes that docx4j doesn't give, with why */
	private static final Map<String, String> EXCEL_ONLY = new HashMap<>();
	static {
		// a row's height not set by hand: Excel's from the Normal font at the screen, the file's
		// as Excel saved it (at 120); the probes' font changed by hand left it at Aptos Narrow's
		EXCEL_ONLY.put("120 s4-calibri-14 height", "Calibri 14's row 18pt, the file's 14.4");
		EXCEL_ONLY.put("120 s5-arial-10 height", "Arial 10's row 13.2pt, the file's 14.4");
		for (String n : new String[] { "s0-base", "s1-widths", "s2-heights", "s3-hidden-column", "s4-calibri-14", "s5-arial-10",
				"s6-default-width", "s7-base-width", "s9-offsets" }) {
			EXCEL_ONLY.put("96 " + n + " height", "the font's row at 96 (Aptos Narrow 11: 15pt), the file's at 120 (14.4)");
		}
		// GDI's hinting: Calibri 14 at 96 is set at 19 pixels, where its digits are 10 pixels wide
		// (its advances 9.6); docx4j measures 9.46 and rounds to 9 (at 120, 12 either way)
		EXCEL_ONLY.put("96 s4-calibri-14 width", "GDI's hinted digits");
	}

	private static String text(String path) throws Exception {
		try (InputStream is = ChartAnchorSizeTest.class.getClassLoader().getResourceAsStream(path)) {
			return new String(is.readAllBytes(), java.nio.charset.StandardCharsets.UTF_8);
		}
	}

	private static ChartRef chart(String name) throws Exception {
		try (InputStream is = ChartAnchorSizeTest.class.getClassLoader().getResourceAsStream("charts/sizes/" + name + ".xlsx")) {
			return Charts.in(OpcPackage.load(is)).get(0);
		}
	}

	/** @return how many of Excel's widths and heights docx4j gives, exactly */
	private static int compare(String tsv, int dpi) throws Exception {
		String[] rows = text("charts/sizes/" + tsv).split("\r?\n");
		assertEquals("ten probes", 11, rows.length);
		int same = 0;
		for (int i = 1; i < rows.length; i++) {
			String[] r = rows[i].split("\t");
			Dimension2D size = ChartImages.getSize(chart(r[0]), dpi);
			assertNotNull(r[0], size);
			if (!EXCEL_ONLY.containsKey(dpi + " " + r[0] + " width")) {
				assertEquals(dpi + " " + r[0] + " width", Double.parseDouble(r[1]), size.getWidth(), 0.01);
				same++;
			}
			if (!EXCEL_ONLY.containsKey(dpi + " " + r[0] + " height")) {
				assertEquals(dpi + " " + r[0] + " height", Double.parseDouble(r[2]), size.getHeight(), 0.01);
				same++;
			}
		}
		return same;
	}

	@Test
	public void excelsSizesOnAScreenAt120() throws Exception {
		// every width, and every height but the two whose font was changed by hand
		assertEquals(18, compare("excel-sizes.tsv", 120));
	}

	@Test
	public void excelsSizesOnAScreenAt96() throws Exception {
		// every width but Calibri 14's; the height set by hand (s8, in whole pixels: 19.5pt rows)
		assertEquals(10, compare("excel-sizes-96.tsv", 96));
	}

	@Test
	public void aScreenAt100IsTheDefault() throws Exception {
		Dimension2D base = ChartImages.getSize(chart("s0-base"));
		assertEquals(432, base.getWidth(), 0.001);
		// the rows as Excel saved them, at 120 (14.4pt), not as Excel lays them out at 96 (15)
		assertEquals(252, base.getHeight(), 0.001);
		assertEquals(521.25, ChartImages.getSize(chart("s1-widths")).getWidth(), 0.001);
	}
}
