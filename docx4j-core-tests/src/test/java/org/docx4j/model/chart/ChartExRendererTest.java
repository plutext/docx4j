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
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertSame;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.awt.Graphics2D;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import javax.imageio.ImageIO;

import org.docx4j.Docx4J;
import org.docx4j.Docx4jProperties;
import org.docx4j.TraversalUtil;
import org.docx4j.XmlUtils;
import org.docx4j.convert.out.HTMLSettings;
import org.docx4j.jaxb.McMode;
import org.docx4j.jaxb.McSelection;
import org.docx4j.mce.AlternateContent;
import org.docx4j.model.color.ThemeColors;
import org.docx4j.openpackaging.exceptions.Docx4JException;
import org.docx4j.openpackaging.packages.OpcPackage;
import org.docx4j.openpackaging.packages.PresentationMLPackage;
import org.docx4j.openpackaging.packages.SpreadsheetMLPackage;
import org.docx4j.openpackaging.packages.WordprocessingMLPackage;
import org.docx4j.openpackaging.parts.DrawingML.ChartExSpacePart;
import org.docx4j.openpackaging.parts.PresentationML.SlidePart;
import org.junit.After;
import org.junit.Test;
import org.pptx4j.convert.out.svginhtml.SvgExporter;
import org.slf4j.LoggerFactory;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

/**
 * CR-039 phase 1: a chartex chart drawn by a {@link ChartExRenderer}, found apart from
 * {@link ChartRenderer}; {@link ChartImages} dispatching on the chart's kind; and the outputs taking
 * a chartex chart's {@code mc:Choice} where the renderer draws the chart ({@link ChartExBranch}),
 * its Fallback picture otherwise, on Word's and PowerPoint's own waterfall (CR-035 phase 0's
 * fixtures).  The PDF output is docx4j-export-fo-tests' {@code ChartBoxFoTest}.
 */
public class ChartExRendererTest {

	private static final String SVG = "http://www.w3.org/2000/svg";
	private static final int[] PATHWAYS = { Docx4J.FLAG_NONE, Docx4J.FLAG_EXPORT_PREFER_XSL };

	/** A renderer that draws a chart as a box marked with its first series' layout, or refuses every chart */
	static final class TestRenderer implements ChartExRenderer {
		final boolean draws;
		final List<ChartExModel> checked = new ArrayList<>();

		TestRenderer(boolean draws) {
			this.draws = draws;
		}

		@Override
		public ChartRenderCheck check(ChartExModel chart, ThemeColors theme) {
			checked.add(chart);
			return new ChartRenderCheck(draws ? null : Collections.singletonList("the test refuses it"), null);
		}

		@Override
		public void draw(ChartExModel chart, ThemeColors theme, ChartFonts fonts, double width, double height, Graphics2D g) {
			g.setColor(java.awt.Color.RED);
			g.fill(new java.awt.geom.Rectangle2D.Double(0, 0, width, height));
		}

		@Override
		public Document toSvg(ChartExModel chart, ThemeColors theme, ChartFonts fonts, double width, double height) {
			if (!draws) return null;
			Document d = XmlUtils.getNewDocumentBuilder().newDocument();
			Element svg = d.createElementNS(SVG, "svg");
			svg.setAttribute("width", width + "pt");
			svg.setAttribute("height", height + "pt");
			d.appendChild(svg);
			Element text = d.createElementNS(SVG, "text");
			text.setAttribute("id", "cr039");
			text.setTextContent("drawn " + chart.getSeries().get(0).getLayout());
			svg.appendChild(text);
			return d;
		}
	}

	@After
	public void reset() {
		ChartExRenderer.setRenderer(null);
		Docx4jProperties.setProperty(ChartExBranch.PROPERTY, "auto");
	}

	private static OpcPackage load(String name) throws Exception {
		try (InputStream is = ChartExRendererTest.class.getClassLoader().getResourceAsStream("charts/" + name)) {
			return OpcPackage.load(is);
		}
	}

	private static ChartRef chartEx(OpcPackage pkg) throws Exception {
		for (ChartRef c : Charts.in(pkg)) {
			if (c.isChartEx()) return c;
		}
		fail("no chartex chart");
		return null;
	}

	/** The waterfall docx's mc:AlternateContent: its Choice holds the cx:chart, its Fallback Word's picture of it */
	private static AlternateContent alternateContent(WordprocessingMLPackage pkg) {
		List<AlternateContent> found = new ArrayList<>();
		new TraversalUtil(pkg.getMainDocumentPart().getContent(), new TraversalUtil.CallbackImpl() {
			@Override
			public List<Object> apply(Object o) {
				if (o instanceof AlternateContent) found.add((AlternateContent) o);
				return null;
			}
		}, McMode.ALL);
		assertEquals(1, found.size());
		return found.get(0);
	}

	private static String html(WordprocessingMLPackage pkg, int flags) throws Exception {
		HTMLSettings settings = Docx4J.createHTMLSettings();
		settings.setOpcPackage(pkg);
		ByteArrayOutputStream baos = new ByteArrayOutputStream();
		Docx4J.toHTML(settings, baos, flags);
		return baos.toString("UTF-8");
	}

	private static String pathway(int flags) {
		return (flags == Docx4J.FLAG_NONE ? "visitor" : "XSLT") + ": ";
	}

	@Test
	public void docx4jHasNoChartExRenderer() {
		// docx4j-core and its tests provide none: the lookup is ChartRenderer's, apart from it
		assertNull(Renderers.first(ChartExRenderer.class, LoggerFactory.getLogger(ChartExRendererTest.class)));
	}

	@Test
	public void chartImagesSendsAChartexChartToTheChartExRenderer() throws Exception {
		ChartRef c = chartEx(load("waterfall.docx"));

		ChartRenderCheck none = ChartImages.check(c);
		assertFalse(none.isDrawn());
		assertTrue(none.toString(), none.getRefused().get(0).contains("no chartex renderer"));

		TestRenderer r = new TestRenderer(true);
		ChartExRenderer.setRenderer(r);
		assertTrue(ChartImages.check(c).isDrawn());
		Document svg = ChartImages.toSvg(c);
		assertEquals("drawn waterfall", svg.getElementsByTagNameNS(SVG, "text").item(0).getTextContent());
		assertEquals("432.0pt", svg.getDocumentElement().getAttribute("width"));
		BufferedImage png = ImageIO.read(new ByteArrayInputStream(ChartImages.toPng(c, 72)));
		assertEquals(432, png.getWidth());
		assertEquals(252, png.getHeight());
		assertEquals(0xFF0000, png.getRGB(200, 100) & 0xFFFFFF);
		// the model the renderer is given knows its part, and so its workbook
		ChartExModel m = r.checked.get(0);
		assertSame(c.getChartPart(), m.getPart());

		ChartExRenderer.setRenderer(new TestRenderer(false));
		ChartRenderCheck refused = ChartImages.check(c);
		assertFalse(refused.isDrawn());
		assertEquals("the test refuses it", refused.getRefused().get(0));
		try {
			ChartImages.toSvg(c);
			fail("a refused chart is drawn");
		} catch (Docx4JException e) {
			assertTrue(e.getMessage(), e.getMessage().contains("the test refuses it"));
		}
	}

	@Test
	public void anXlsxsChartexChartReachesItsWorkbook() throws Exception {
		OpcPackage pkg = load("waterfall.xlsx");
		ChartRef c = chartEx(pkg);
		TestRenderer r = new TestRenderer(true);
		ChartExRenderer.setRenderer(r);
		assertNotNull(ChartImages.toSvg(c, 432, 252));
		assertTrue(r.checked.get(0).getPart().getPackage() instanceof SpreadsheetMLPackage);
		ChartExSpacePart part = (ChartExSpacePart) c.getChartPart();
		assertNull("read from the chart space alone", ChartExModel.read(part.getContents()).getPart());
	}

	@Test
	public void theChoiceIsTakenWhereTheRendererDrawsTheChart() throws Exception {
		WordprocessingMLPackage pkg = (WordprocessingMLPackage) load("waterfall.docx");
		AlternateContent ac = alternateContent(pkg);
		org.docx4j.openpackaging.parts.Part host = pkg.getMainDocumentPart();
		Object choice = ac.getChoice().get(0), fallback = ac.getFallback();

		// no chartex renderer: the Fallback, as before
		assertSame(fallback, McSelection.selectedBranch(ac, host));

		ChartExRenderer.setRenderer(new TestRenderer(true));
		assertSame(choice, McSelection.selectedBranch(ac, host));
		// a walk that draws nothing (no part) takes the Fallback
		assertSame(fallback, McSelection.selectedBranch(ac));
		assertSame(fallback, McSelection.selectedBranch(ac, null));

		Docx4jProperties.setProperty(ChartExBranch.PROPERTY, "fallback");
		assertSame(fallback, McSelection.selectedBranch(ac, host));

		ChartExRenderer.setRenderer(new TestRenderer(false));
		Docx4jProperties.setProperty(ChartExBranch.PROPERTY, "auto");
		assertSame("the renderer refuses the chart", fallback, McSelection.selectedBranch(ac, host));

		ChartExRenderer.setRenderer(null);
		Docx4jProperties.setProperty(ChartExBranch.PROPERTY, "choice");
		assertSame("forced, with no renderer", choice, McSelection.selectedBranch(ac, host));

		Docx4jProperties.setProperty(ChartExBranch.PROPERTY, "sometimes");
		assertSame("not a value: auto", fallback, McSelection.selectedBranch(ac, host));
	}

	@Test
	public void htmlDrawsTheChoiceWhereTheRendererDrawsTheChart() throws Exception {
		for (int flags : PATHWAYS) {
			// no renderer: Word's picture of the chart (ChartBoxTest.chartexInWordDrawsItsFallbackPicture)
			String html = html((WordprocessingMLPackage) load("waterfall.docx"), flags);
			assertFalse(pathway(flags), html.contains("<svg"));

			ChartExRenderer.setRenderer(new TestRenderer(true));
			html = html((WordprocessingMLPackage) load("waterfall.docx"), flags);
			assertTrue(pathway(flags) + html, html.contains(">drawn waterfall</text>"));
			assertFalse(pathway(flags) + "the Fallback's picture too", html.matches("(?s).*<img[^>]*width=\"432\"[^>]*>.*"));

			ChartExRenderer.setRenderer(new TestRenderer(false));
			html = html((WordprocessingMLPackage) load("waterfall.docx"), flags);
			assertFalse(pathway(flags), html.contains("drawn waterfall"));
			assertTrue(pathway(flags) + html, html.matches("(?s).*<img[^>]*width=\"432\"[^>]*>.*"));
			ChartExRenderer.setRenderer(null);
		}
	}

	@Test
	public void pptxSvgInHtmlDrawsTheChoiceWhereTheRendererDrawsTheChart() throws Exception {
		PresentationMLPackage pml = (PresentationMLPackage) load("waterfall.pptx");
		SlidePart slide = pml.getMainPresentationPart().getSlide(0);
		String html = SvgExporter.svg(pml, slide);
		assertFalse(html.contains("drawn waterfall"));

		ChartExRenderer.setRenderer(new TestRenderer(true));
		html = SvgExporter.svg(pml, slide);
		assertTrue(html, html.contains(">drawn waterfall</text>"));
	}
}
