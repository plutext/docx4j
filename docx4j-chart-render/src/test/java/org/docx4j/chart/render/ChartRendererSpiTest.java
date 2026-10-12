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
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import java.awt.image.BufferedImage;

import org.docx4j.model.chart.ChartFonts;
import org.docx4j.model.chart.ChartRef;
import org.docx4j.model.chart.ChartRenderCheck;
import org.docx4j.model.chart.ChartRenderer;
import org.junit.Test;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

/**
 * The renderer through docx4j-core's SPI (CR-037, review A8: found by ServiceLoader on the module
 * path), its check (A1), and its SVG (text as text, D6) and picture.
 */
public class ChartRendererSpiTest {

	@Test
	public void foundThroughTheSpi() {
		ChartRenderer r = ChartRenderer.getRenderer();
		assertNotNull("no ChartRenderer found", r);
		assertEquals(ClassicChartRenderer.class, r.getClass());
	}

	@Test
	public void checkDrawsTheTwelveTypesAndRefusesWhatPhaseTwoDoesNot() throws Exception {
		ChartRenderer r = new ClassicChartRenderer();
		for (String t : new String[] { "column-clustered", "column-stacked", "column-stacked-100", "bar-clustered", "bar-stacked",
				"bar-stacked-100", "line", "line-markers", "pie", "doughnut", "area", "scatter" }) {
			ChartRef c = ChartRenderGateTest.chart("r1-" + t);
			ChartRenderCheck k = r.check(c.read(), c.getHostTheme());
			assertTrue(t + ": " + k, k.isDrawn());
			assertFalse(t + ": " + k, k.isSimplified());
		}
		// smoothed lines, as Office smooths them (R14)
		for (String t : new String[] { "r14-line-smooth", "r14-scatter-smooth" }) {
			ChartRef c = ChartRenderGateTest.chart(t);
			assertTrue(t, r.check(c.read(), c.getHostTheme()).isDrawn());
		}
		ChartRef labels = ChartRenderGateTest.chart("r6-column-clustered-ctr");
		ChartRenderCheck l = r.check(labels.read(), labels.getHostTheme());
		assertFalse(l.isDrawn());
		assertTrue(l.getRefused().toString(), l.getRefused().contains("data labels"));
		ChartRef moved = ChartRenderGateTest.chart("r9-title-moved");
		assertTrue(r.check(moved.read(), moved.getHostTheme()).getRefused().contains("the title placed by hand"));
		// a refused chart: no SVG, no picture
		assertNull(r.toSvg(labels.read(), labels.getHostTheme(), ChartFonts.of(labels.getHost()), 432, 252));
		assertNull(r.toImage(labels.read(), labels.getHostTheme(), ChartFonts.of(labels.getHost()), 432, 252, 72));
	}

	@Test
	public void svgKeepsTextAsTextInTheMappedFamily() throws Exception {
		ChartRef c = ChartRenderGateTest.chart("r1-column-clustered");
		Document svg = new ClassicChartRenderer().toSvg(c.read(), c.getHostTheme(), ChartFonts.of(c.getHost()), 432, 252);
		assertNotNull(svg);
		Element root = svg.getDocumentElement();
		assertEquals("432pt", root.getAttribute("width"));
		assertEquals("0 0 432 252", root.getAttribute("viewBox"));
		NodeList texts = svg.getElementsByTagNameNS("http://www.w3.org/2000/svg", "text");
		StringBuilder all = new StringBuilder();
		boolean carlito = false;
		for (int i = 0; i < texts.getLength(); i++) {
			all.append(texts.item(i).getTextContent()).append('|');
			Element e = (Element) texts.item(i);
			for (org.w3c.dom.Node n = e; n instanceof Element; n = n.getParentNode()) {
				if (((Element) n).getAttribute("font-family").contains("Carlito")) carlito = true;
			}
		}
		assertTrue(all.toString(), all.toString().contains("Chart Title|"));
		assertTrue(all.toString(), all.toString().contains("Category 4|"));
		assertTrue("Calibri drawn as Carlito, the family FOP is given", carlito);
	}

	@Test
	public void picture() throws Exception {
		ChartRef c = ChartRenderGateTest.chart("r1-column-clustered");
		BufferedImage im = ChartRenderer.getRenderer().toImage(c.read(), c.getHostTheme(), ChartFonts.of(c.getHost()), 432, 252, 120);
		assertEquals(720, im.getWidth());
		assertEquals(420, im.getHeight());
		// the first series' bar, accent 1, under the first category
		assertEquals(0x4472C4, im.getRGB(80, 250) & 0xFFFFFF);
	}
}
