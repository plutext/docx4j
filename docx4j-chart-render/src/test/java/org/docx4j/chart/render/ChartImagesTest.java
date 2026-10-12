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
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.awt.geom.Dimension2D;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.imageio.ImageIO;

import org.docx4j.Docx4J;
import org.docx4j.Docx4jProperties;
import org.docx4j.XmlUtils;
import org.docx4j.convert.out.HTMLSettings;
import org.docx4j.model.chart.ChartBox;
import org.docx4j.model.chart.ChartImages;
import org.docx4j.model.chart.ChartRef;
import org.docx4j.model.chart.ChartRenderCheck;
import org.docx4j.model.chart.Charts;
import org.docx4j.openpackaging.exceptions.Docx4JException;
import org.docx4j.openpackaging.packages.OpcPackage;
import org.docx4j.openpackaging.packages.PresentationMLPackage;
import org.docx4j.openpackaging.packages.WordprocessingMLPackage;
import org.junit.BeforeClass;
import org.junit.Test;
import org.pptx4j.convert.out.svginhtml.SvgExporter;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

/**
 * CR-037 phase 3: a chart drawn through {@link ChartImages} (SVG, PNG, the reasons a chart isn't
 * drawn), and in docx4j's HTML and pptx SVG-in-HTML outputs, on Office's own charts (Word's and
 * PowerPoint's Insert Chart, CR-036's P3 and P4) and Excel's.  The PDF output is
 * docx4j-export-fo-tests' {@code ChartBoxFoTest}; how close the drawings are to Office's is the
 * phase 3 gate (CR-037 section 11), run from docx4j-layout-fidelity's tools.
 */
public class ChartImagesTest {

	private static final String SVG = "http://www.w3.org/2000/svg";
	private static final int[] PATHWAYS = { Docx4J.FLAG_NONE, Docx4J.FLAG_EXPORT_PREFER_XSL };

	@BeforeClass
	public static void shortDate() {
		// the machine's short date Office drew P3's dates in (the gate's setting)
		Docx4jProperties.setProperty("docx4j.chart.render.shortDate", "d/mm/yyyy");
	}

	private static OpcPackage office(String name) throws Exception {
		try (InputStream is = ChartImagesTest.class.getClassLoader().getResourceAsStream(ChartRenderGateTest.DIR + "office/" + name)) {
			return OpcPackage.load(is);
		}
	}

	private static List<Element> texts(Document svg) {
		List<Element> l = new ArrayList<>();
		NodeList n = svg.getElementsByTagNameNS(SVG, "text");
		for (int i = 0; i < n.getLength(); i++) l.add((Element) n.item(i));
		return l;
	}

	@Test
	public void wordsChartAsSvgAndPng() throws Exception {
		ChartRef chart = Charts.in(office("p3-column-clustered.docx")).get(0);
		assertTrue(ChartImages.check(chart).toString(), ChartImages.check(chart).isDrawn());

		Dimension2D size = ChartImages.getSize(chart);
		assertEquals(432, size.getWidth(), 0.001);
		assertEquals(252, size.getHeight(), 0.001);

		Document svg = ChartImages.toSvg(chart);
		Element root = svg.getDocumentElement();
		assertEquals("432pt", root.getAttribute("width"));
		assertEquals("252pt", root.getAttribute("height"));
		assertEquals("0 0 432 252", root.getAttribute("viewBox"));
		// the layout places every run: a browser mustn't kern or make ligatures of its own
		assertTrue(root.getAttribute("style"), root.getAttribute("style").contains("font-kerning: none"));
		Set<String> strings = new HashSet<>();
		for (Element t : texts(svg)) strings.add(t.getTextContent());
		for (String s : new String[] { "Chart Title", "Category 1", "Category 4", "Series 1", "Series 3", "0", "6" }) {
			assertTrue(s + " in " + strings, strings.contains(s));
		}
		// Calibri as the face docx4j's mapper gives FOP, then the chart's own face, then its class
		NodeList g = svg.getElementsByTagNameNS(SVG, "g");
		boolean families = false;
		for (int i = 0; i < g.getLength(); i++) {
			if ("'Carlito', 'Calibri', sans-serif".equals(((Element) g.item(i)).getAttribute("font-family"))) families = true;
		}
		assertTrue("font-family 'Carlito', 'Calibri', sans-serif", families);

		ByteArrayOutputStream out = new ByteArrayOutputStream();
		ChartImages.writeSvg(chart, out);
		Document parsed = XmlUtils.getNewDocumentBuilder().parse(new ByteArrayInputStream(out.toByteArray()));
		assertEquals("svg", parsed.getDocumentElement().getLocalName());

		BufferedImage png = ImageIO.read(new ByteArrayInputStream(ChartImages.toPng(chart, 96)));
		assertEquals(576, png.getWidth());
		assertEquals(336, png.getHeight());
		// the plot's first bar, Word's accent 1 (4472C4), a quarter of the way in at two thirds down
		BufferedImage at72 = ChartImages.toImage(chart, 72);
		assertEquals(432, at72.getWidth());
		boolean accent1 = false;
		for (int x = 30; x < 120 && !accent1; x++) {
			accent1 = (at72.getRGB(x, 170) & 0xFFFFFF) == 0x4472C4;
		}
		assertTrue("a bar in 4472C4", accent1);
	}

	@Test
	public void textAsShapesOnTheProperty() throws Exception {
		ChartRef chart = Charts.in(office("p3-column-clustered.docx")).get(0);
		Document asText = ChartImages.toSvg(chart);
		int texts = texts(asText).size(), paths = asText.getElementsByTagNameNS(SVG, "path").getLength();
		assertTrue(texts > 10);
		String was = Docx4jProperties.getProperty(ClassicChartRenderer.TEXT_AS_SHAPES, "false");
		Docx4jProperties.setProperty(ClassicChartRenderer.TEXT_AS_SHAPES, "true");
		try {
			Document svg = ChartImages.toSvg(chart);
			assertEquals("no text", 0, texts(svg).size());
			// each text an outline
			assertEquals(paths + texts, svg.getElementsByTagNameNS(SVG, "path").getLength());
		} finally {
			Docx4jProperties.setProperty(ClassicChartRenderer.TEXT_AS_SHAPES, was);
		}
	}

	@Test
	public void aKernedTitleIsSplitAtItsKernedPairs() throws Exception {
		// "Y-Values" at 14pt is kerned; an SVG's text carries no kerning, so it is written as runs
		ChartRef chart = Charts.in(office("p3-scatter.docx")).get(0);
		Document svg = ChartImages.toSvg(chart);
		List<Element> title = new ArrayList<>();
		String y = null;
		for (Element t : texts(svg)) {
			if (t.getTextContent().startsWith("Y") && y == null && !"Y-Values".equals(t.getTextContent())) y = t.getAttribute("y");
			if (y != null && y.equals(t.getAttribute("y"))) title.add(t);
		}
		assertTrue("the title in runs", title.size() > 1);
		StringBuilder b = new StringBuilder();
		double x = -1;
		for (Element t : title) {
			b.append(t.getTextContent());
			double tx = Double.parseDouble(t.getAttribute("x"));
			assertTrue("runs left to right", tx > x);
			x = tx;
		}
		assertEquals("Y-Values", b.toString());
	}

	@Test
	public void aRefusedChartSaysWhy() throws Exception {
		ChartRef labels = ChartRenderGateTest.chart("r6-column-clustered-ctr");
		ChartRenderCheck k = ChartImages.check(labels);
		assertFalse(k.isDrawn());
		assertTrue(k.getRefused().toString(), k.getRefused().contains("data labels"));
		try {
			ChartImages.toSvg(labels);
			fail("a refused chart drawn");
		} catch (Docx4JException e) {
			assertTrue(e.getMessage(), e.getMessage().contains("data labels"));
		}
	}

	@Test
	public void chartexIsNotDrawnByTheOpenRenderer() throws Exception {
		OpcPackage pkg;
		try (InputStream is = ChartImagesTest.class.getClassLoader().getResourceAsStream("charts/waterfall.xlsx")) {
			pkg = OpcPackage.load(is);
		}
		boolean chartex = false;
		for (ChartRef c : Charts.in(pkg)) {
			if (!c.isChartEx()) continue;
			chartex = true;
			ChartRenderCheck k = ChartImages.check(c);
			assertFalse(k.isDrawn());
			assertTrue(k.toString(), k.getRefused().get(0).contains("chartex"));
		}
		assertTrue("waterfall.xlsx has a chartex chart", chartex);
	}

	@Test
	public void aWorkbooksChartIsSizedFromItsCells() throws Exception {
		// Excel's own chart (CR-036's P1): nine 64-pixel columns and seventeen 14.4pt rows and
		// 7.2pt, as Excel wrote the anchor for its 6 x 3.5 inch chart
		ChartRef chart = ChartRenderGateTest.chart("p1-area");
		assertTrue(ChartImages.check(chart).isDrawn());
		Dimension2D size = ChartImages.getSize(chart);
		assertNotNull(size);
		assertEquals(432, size.getWidth(), 0.01);
		assertEquals(252, size.getHeight(), 0.01);
		BufferedImage image = ChartImages.toImage(chart, 72);
		assertEquals(432, image.getWidth());
		assertEquals(252, image.getHeight());
	}

	@Test
	public void htmlHoldsEachChartDrawn() throws Exception {
		Pattern svgTag = Pattern.compile("<svg[^>]*>");
		Pattern id = Pattern.compile(" id=\"([^\"]*)\"");
		for (int flags : PATHWAYS) {
			String pathway = flags == Docx4J.FLAG_NONE ? "visitor: " : "XSLT: ";
			HTMLSettings settings = Docx4J.createHTMLSettings();
			settings.setOpcPackage(office("p3-two-charts.docx"));
			ByteArrayOutputStream baos = new ByteArrayOutputStream();
			Docx4J.toHTML(settings, baos, flags);
			String html = baos.toString("UTF-8");

			Matcher m = svgTag.matcher(html);
			int svgs = 0;
			while (m.find()) {
				svgs++;
				assertTrue(pathway + m.group(), m.group().contains("width=\"432\""));
				assertTrue(pathway + m.group(), m.group().contains("height=\"252\""));
				assertTrue(pathway + m.group(), m.group().contains("aria-label=\"Chart "));
			}
			assertEquals(pathway, 2, svgs);
			assertTrue(pathway, html.contains(">Category 1</text>"));
			// one document holds both: each chart's ids its own
			Set<String> ids = new HashSet<>();
			Matcher i = id.matcher(html);
			while (i.find()) assertTrue(pathway + "id " + i.group(1) + " twice", ids.add(i.group(1)));
		}
	}

	@Test
	public void htmlChartAsPng() throws Exception {
		String was = Docx4jProperties.getProperty(ChartBox.HTML_PNG, "false");
		Docx4jProperties.setProperty(ChartBox.HTML_PNG, "true");
		try {
			HTMLSettings settings = Docx4J.createHTMLSettings();
			settings.setOpcPackage(office("p3-pie.docx"));
			ByteArrayOutputStream baos = new ByteArrayOutputStream();
			Docx4J.toHTML(settings, baos, Docx4J.FLAG_NONE);
			String html = baos.toString("UTF-8");
			assertFalse(html.contains("<svg"));
			Matcher img = Pattern.compile("<img[^>]*src=\"data:image/png;base64,[^\"]+\"[^>]*>").matcher(html);
			assertTrue(html.length() > 2000 ? html.substring(0, 2000) : html, img.find());
			assertTrue(img.group(), img.group().contains("width=\"432\""));
			assertTrue(img.group(), img.group().contains("alt=\"Chart "));
		} finally {
			Docx4jProperties.setProperty(ChartBox.HTML_PNG, was);
		}
	}

	@Test
	public void pptxSvgInHtmlHoldsTheChartDrawnInItsFrame() throws Exception {
		PresentationMLPackage pml = (PresentationMLPackage) office("p4-column-clustered.pptx");
		String html = SvgExporter.svg(pml, pml.getMainPresentationPart().getSlide(0));
		assertFalse(html, html.contains("support for graphicFrame"));
		// the frame: 100pt in from the slide's top left, 432 x 252pt, at 96 dpi
		Matcher div = Pattern.compile("<div[^>]*width:576px; height:336px; left:133px; top:133px;[^>]*>\\s*<svg[^>]*>").matcher(html);
		assertTrue(html, div.find());
		assertTrue(div.group(), div.group().contains("width=\"576\""));
		assertTrue(div.group(), div.group().contains("aria-label=\"Chart "));
		assertTrue(html.contains(">Category 1</text>"));
		// PowerPoint's 2023 theme: Aptos, drawn as its metric clone (no generic family after it:
		// FontFallback doesn't know Aptos's class, as for docx4j's HTML of an Aptos run)
		assertTrue(html.contains("font-family=\"'Akasia', 'Aptos'"));
	}

	@Test
	public void wordPdfAndHtmlDrawTheSameChart() throws Exception {
		// the renderer is reached the same way whatever the output: through ChartRef, from the host
		WordprocessingMLPackage pkg = (WordprocessingMLPackage) office("p3-line.docx");
		ChartRef chart = Charts.in(pkg).get(0);
		ChartBox box = ChartBox.forChart(chart);
		assertTrue(box.isDrawn());
		assertEquals(432, box.getWidthPt(), 0.001);
	}
}
