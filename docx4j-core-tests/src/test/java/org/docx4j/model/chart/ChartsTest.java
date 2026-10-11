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

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import java.io.File;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.util.List;

import org.docx4j.openpackaging.packages.OpcPackage;
import org.junit.Test;

/**
 * Finding placed charts ({@link Charts#in}) in docx, pptx and xlsx, and reading the chartex ones
 * ({@link ChartExModel}), on the chart fixtures.  The chartex JSON goldens are written like
 * {@link ChartModelTest}'s ({@code -Ddocx4j.chartGoldens.write=...}).
 */
public class ChartsTest {

	static List<ChartRef> refs(String fixture) throws Exception {
		try (InputStream is = ChartsTest.class.getClassLoader().getResourceAsStream("charts/" + fixture)) {
			return Charts.in(OpcPackage.load(is));
		}
	}

	@Test
	public void docxInline() throws Exception {
		List<ChartRef> refs = refs("clustered-column.docx");
		assertEquals(1, refs.size());
		ChartRef r = refs.get(0);
		assertEquals("/word/document.xml", r.getHost().getPartName().getName());
		assertEquals("/word/charts/chart1.xml", r.getChartPart().getPartName().getName());
		assertEquals("Chart 1", r.getName());
		assertEquals(Long.valueOf(5486400), r.getWidth());   // 6 x 3.5 in
		assertEquals(Long.valueOf(3200400), r.getHeight());
		assertFalse(r.isInChoice());
		assertEquals("4472C4", r.read().getGroups().get(0).getSeries().get(0).getFill().getColor().toHex());
	}

	/** Word, PowerPoint and Excel each put a chartex chart in an mc:AlternateContent Choice */
	@Test
	public void chartexInAChoice() throws Exception {
		for (String f : new String[] { "waterfall.docx", "waterfall.pptx" }) {
			List<ChartRef> refs = refs(f);
			assertEquals(f, 1, refs.size());
			assertTrue(f, refs.get(0).isChartEx());
			assertTrue(f, refs.get(0).isInChoice());
			assertNull(f, refs.get(0).read());
		}
		assertEquals("Chart 5", refs("waterfall.pptx").get(0).getName());
		List<ChartRef> xlsx = refs("waterfall.xlsx");   // the line chart of a first attempt, then the waterfall
		assertEquals(2, xlsx.size());
		assertFalse(xlsx.get(0).isChartEx());
		assertTrue(xlsx.get(1).isChartEx());
		assertTrue(xlsx.get(1).isInChoice());
	}

	/** A chart pasted into PowerPoint: its slide is the host, and its theme the slide's */
	@Test
	public void pptxSlide() throws Exception {
		ChartRef r = refs("p2-pasted.pptx").get(0);
		assertEquals("/ppt/slides/slide1.xml", r.getHost().getPartName().getName());
		assertEquals("156082", r.read().getGroups().get(0).getSeries().get(0).getFill().getColor().toHex());
	}

	/** A chartsheet: its drawing part holds the chart, on an absolute anchor with an extent */
	@Test
	public void chartsheet() throws Exception {
		ChartRef r = refs("p2-chartsheet.xlsx").get(0);
		assertEquals("/xl/drawings/drawing1.xml", r.getHost().getPartName().getName());
		assertTrue(r.getWidth() > 0 && r.getHeight() > 0);
	}

	@Test
	public void waterfall() throws Exception {
		ChartExModel m = refs("waterfall.docx").get(0).readChartEx();
		assertEquals("Chart Title", m.getShownTitle());
		ChartExModel.Series s = m.getSeries().get(0);
		assertEquals("waterfall", s.getLayout());
		assertEquals("Series1", s.getName());
		assertEquals(3, s.getSubtotals().size());
		ChartExModel.Dimension val = s.getData().get(1);
		assertEquals("val", val.getType());
		assertEquals("-40", val.getLevels().get(0).get(3));
		// Excel writes no caches: the formulas name hidden defined names
		ChartExModel x = refs("waterfall.xlsx").get(1).readChartEx();
		assertTrue(x.getSeries().get(0).getData().get(1).getFormula().startsWith("_xlchart.v1."));
		assertTrue(x.getSeries().get(0).getData().get(1).getLevels().isEmpty());
		assertTrue(x.getSeries().get(1).isHidden());
	}

	@Test
	public void chartexGoldens() throws Exception {
		String write = System.getProperty("docx4j.chartGoldens.write");
		for (String f : new String[] { "waterfall.docx", "waterfall.pptx", "waterfall.xlsx" }) {
			for (ChartRef r : refs(f)) {
				if (!r.isChartEx()) continue;
				String json = r.readChartEx().toJson();
				String name = f + r.getChartPart().getPartName().getName().replace('/', '_') + ".json";
				if (write != null) {
					Files.write(new File(write, name).toPath(), json.getBytes(StandardCharsets.UTF_8));
					continue;
				}
				try (InputStream is = getClass().getClassLoader().getResourceAsStream("charts/json/" + name)) {
					assertTrue("no golden " + name, is != null);
					assertEquals(name, new String(is.readAllBytes(), StandardCharsets.UTF_8), json);
				}
			}
		}
	}
}
