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

import java.awt.geom.Dimension2D;
import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.OutputStream;

import javax.imageio.ImageIO;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.Transformer;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;

import org.docx4j.XmlUtils;
import org.docx4j.model.color.ThemeColors;
import org.docx4j.openpackaging.exceptions.Docx4JException;
import org.docx4j.org.apache.poi.util.Dimension2DDouble;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.w3c.dom.Document;

/**
 * A chart as a picture: SVG or PNG, drawn as Office draws it by the {@link ChartRenderer} on
 * the classpath (docx4j-chart-render's, for the classic chart types; CR-037).  The chart is
 * any a {@link ChartRef} names, in a docx, a pptx or an xlsx ({@link Charts#in} finds them);
 * its size is its anchor's unless one is given, its theme and fonts its host's.
 *
 * <pre>
 * for (ChartRef chart : Charts.in(pkg)) {
 *     if (ChartImages.check(chart).isDrawn()) {
 *         try (OutputStream out = new FileOutputStream(chart.getName() + ".svg")) {
 *             ChartImages.writeSvg(chart, out);
 *         }
 *     }
 * }
 * </pre>
 *
 * <p>Each method that draws throws where it can't: no renderer on the classpath (add
 * docx4j-chart-render), a chartex chart (waterfall, funnel, ...: not drawn by the open
 * renderer), a chart the renderer refuses (the reasons in the message, as {@link #check} gives
 * them), or a size unknown.  The PDF, HTML and pptx outputs draw charts through the same
 * renderer, and keep a chart's box empty where it can't be drawn ({@link ChartBox}).</p>
 *
 * @since 17.3.3 (CR-037 phase 3)
 */
public final class ChartImages {

	private static final Logger log = LoggerFactory.getLogger(ChartImages.class);

	private static final double EMU_PER_PT = 12700;

	private ChartImages() {
	}

	/**
	 * What the renderer would do with the chart: draw it, draw it simplified (a cosmetic
	 * feature not drawn as Office draws it, each named), or refuse it (a structural feature it
	 * doesn't draw, each named).  A chart is refused too where there is no renderer, or it is
	 * a chartex chart.
	 */
	public static ChartRenderCheck check(ChartRef chart) throws Docx4JException {
		String why = unrenderable(chart);
		if (why != null) return refused(why);
		return ChartRenderer.getRenderer().check(chart.read(), chart.getHostTheme());
	}

	/**
	 * The chart's size in points: its anchor's extent; for an xlsx two-cell anchor, the cells
	 * it spans as Excel lays them out on a screen at 100% (96 dots an inch; see
	 * {@link #getSize(ChartRef, double)}).
	 *
	 * @return null where the anchor gives no size
	 */
	public static Dimension2D getSize(ChartRef chart) {
		return getSize(chart, 96);
	}

	/**
	 * The chart's size in points as Office shows it on a screen of dpi dots an inch: an extent
	 * is the same at any; an xlsx two-cell anchor's cells are not, since Excel lays the sheet out
	 * in whole pixels of the screen, so a chart over columns of a set width is wider at 120 (a
	 * screen at 125%) than at 96 ({@link #getSize(ChartRef)}'s, a screen at 100%).  Rows are
	 * taken as the file gives them (Excel's at the screen that saved it), a height set by hand
	 * in whole pixels at dpi.
	 *
	 * @return null where the anchor gives no size
	 */
	public static Dimension2D getSize(ChartRef chart, double dpi) {
		if (chart == null) return null;
		if (chart.getWidth() != null && chart.getHeight() != null && chart.getWidth() > 0 && chart.getHeight() > 0) {
			return new Dimension2DDouble(chart.getWidth() / EMU_PER_PT, chart.getHeight() / EMU_PER_PT);
		}
		return ChartAnchorSize.of(chart, dpi);
	}

	/** The chart as an SVG document at its anchor's size (one user unit a point) */
	public static Document toSvg(ChartRef chart) throws Docx4JException {
		Dimension2D size = size(chart);
		return toSvg(chart, size.getWidth(), size.getHeight());
	}

	/**
	 * The chart as an SVG document, width by height points (one user unit a point), its text
	 * as text under the families docx4j's font mapping gives FOP
	 */
	public static Document toSvg(ChartRef chart, double widthPt, double heightPt) throws Docx4JException {
		Drawn d = drawable(chart);
		ChartFonts fonts = ChartFonts.of(chart.getHost());
		Document svg = d.renderer.toSvg(d.model, d.theme, fonts, widthPt, heightPt);
		if (svg == null) throw new Docx4JException("The chart " + chart.getName() + " couldn't be drawn as SVG");
		documentFaces(svg.getDocumentElement(), fonts);
		return svg;
	}

	/** The chart's SVG (at its anchor's size), written as UTF-8 */
	public static void writeSvg(ChartRef chart, OutputStream out) throws Docx4JException {
		write(toSvg(chart), out);
	}

	/** The chart's SVG, width by height points, written as UTF-8 */
	public static void writeSvg(ChartRef chart, double widthPt, double heightPt, OutputStream out) throws Docx4JException {
		write(toSvg(chart, widthPt, heightPt), out);
	}

	/** The chart as a picture at its anchor's size, dpi pixels an inch, on white */
	public static BufferedImage toImage(ChartRef chart, double dpi) throws Docx4JException {
		Dimension2D size = size(chart);
		return toImage(chart, size.getWidth(), size.getHeight(), dpi);
	}

	/** The chart as a picture, width by height points, dpi pixels an inch, on white */
	public static BufferedImage toImage(ChartRef chart, double widthPt, double heightPt, double dpi) throws Docx4JException {
		Drawn d = drawable(chart);
		BufferedImage image = d.renderer.toImage(d.model, d.theme, ChartFonts.of(chart.getHost()), widthPt, heightPt, dpi);
		if (image == null) throw new Docx4JException("The chart " + chart.getName() + " couldn't be drawn");
		return image;
	}

	/** The chart as PNG bytes at its anchor's size, dpi pixels an inch */
	public static byte[] toPng(ChartRef chart, double dpi) throws Docx4JException {
		ByteArrayOutputStream out = new ByteArrayOutputStream();
		writePng(chart, dpi, out);
		return out.toByteArray();
	}

	/** The chart as PNG at its anchor's size, dpi pixels an inch */
	public static void writePng(ChartRef chart, double dpi, OutputStream out) throws Docx4JException {
		png(toImage(chart, dpi), out);
	}

	/** The chart as PNG, width by height points, dpi pixels an inch */
	public static void writePng(ChartRef chart, double widthPt, double heightPt, double dpi, OutputStream out)
			throws Docx4JException {
		png(toImage(chart, widthPt, heightPt, dpi), out);
	}

	// ------------------------------------------------------------------ for the outputs

	/**
	 * The chart drawn as SVG for an output's box ({@link ChartBox}), or null where it can't be
	 * (no renderer, chartex, refused, an error), the reason logged: the box stays empty.
	 */
	static Document svg(ChartRef chart, double widthPt, double heightPt) {
		if (chart == null || ChartRenderer.getRenderer() == null) return null;
		String why = unrenderable(chart);
		if (why != null) {
			log.info("The chart " + chart.getName() + " keeps its box: " + why);
			return null;
		}
		try {
			ChartModel model = chart.read();
			ThemeColors theme = chart.getHostTheme();
			ChartRenderer renderer = ChartRenderer.getRenderer();
			ChartRenderCheck check = renderer.check(model, theme);
			if (!check.isDrawn()) {
				log.info("The chart " + chart.getName() + " keeps its box: " + check);
				return null;
			}
			if (check.isSimplified()) log.info("The chart " + chart.getName() + " is " + check);
			ChartFonts fonts = ChartFonts.of(chart.getHost());
			Document svg = renderer.toSvg(model, theme, fonts, widthPt, heightPt);
			if (svg != null) documentFaces(svg.getDocumentElement(), fonts);
			return svg;
		} catch (Exception e) {
			log.warn("The chart " + chart.getName() + " keeps its box: " + e.getMessage(), e);
			return null;
		}
	}

	/**
	 * The chart drawn as a picture for an output's box, or null where it can't be (as
	 * {@link #svg})
	 */
	static BufferedImage image(ChartRef chart, double widthPt, double heightPt, double dpi) {
		try {
			Drawn d = drawable(chart);
			return d.renderer.toImage(d.model, d.theme, ChartFonts.of(chart.getHost()), widthPt, heightPt, dpi);
		} catch (Exception e) {
			log.warn("The chart " + (chart == null ? "" : chart.getName()) + " couldn't be drawn as a picture: " + e.getMessage());
			return null;
		}
	}

	/**
	 * After each family the SVG names (the face docx4j's font mapping gives FOP: Carlito), the
	 * face the chart names that it stands for (Calibri), then the generic family of its class,
	 * as docx4j's HTML names a run's (sans-serif): FOP draws the first it has, a browser or
	 * another reader of the SVG the first it has, the first two at the widths the chart was laid
	 * out with.  (Chrome on Linux, for one, resolves neither Carlito nor Calibri by name.)
	 */
	private static void documentFaces(org.w3c.dom.Element e, ChartFonts fonts) {
		String family = e.getAttribute("font-family");
		if (!family.isEmpty()) {
			String awt = family.replace("'", "").replace("\"", "").trim();
			java.util.Set<String> faces = fonts.facesByFamily().get(awt);
			if (faces != null && !faces.isEmpty()) {
				StringBuilder b = new StringBuilder(family);
				org.docx4j.fonts.FontFallback.FontClass c = org.docx4j.fonts.FontFallback.FontClass.UNKNOWN;
				for (String f : new java.util.TreeSet<String>(faces)) {
					b.append(", '").append(f).append("'");
					if (c == org.docx4j.fonts.FontFallback.FontClass.UNKNOWN) c = org.docx4j.fonts.FontFallback.classOf(f);
				}
				if (c == org.docx4j.fonts.FontFallback.FontClass.UNKNOWN) c = org.docx4j.fonts.FontFallback.classOf(awt);
				String generic = c == org.docx4j.fonts.FontFallback.FontClass.SERIF ? "serif"
						: c == org.docx4j.fonts.FontFallback.FontClass.SANS ? "sans-serif"
						: c == org.docx4j.fonts.FontFallback.FontClass.MONO ? "monospace" : null;
				if (generic != null) b.append(", ").append(generic);
				e.setAttribute("font-family", b.toString());
			}
		}
		for (org.w3c.dom.Node n = e.getFirstChild(); n != null; n = n.getNextSibling()) {
			if (n instanceof org.w3c.dom.Element) documentFaces((org.w3c.dom.Element) n, fonts);
		}
	}

	// ------------------------------------------------------------------

	private static final class Drawn {
		final ChartRenderer renderer;
		final ChartModel model;
		final ThemeColors theme;

		Drawn(ChartRenderer renderer, ChartModel model, ThemeColors theme) {
			this.renderer = renderer;
			this.model = model;
			this.theme = theme;
		}
	}

	private static Drawn drawable(ChartRef chart) throws Docx4JException {
		String why = unrenderable(chart);
		if (why != null) throw new Docx4JException("The chart " + (chart == null ? "" : chart.getName()) + " can't be drawn: " + why);
		ChartRenderer renderer = ChartRenderer.getRenderer();
		ChartModel model = chart.read();
		ThemeColors theme = chart.getHostTheme();
		ChartRenderCheck check = renderer.check(model, theme);
		if (!check.isDrawn()) throw new Docx4JException("The chart " + chart.getName() + " can't be drawn: " + check);
		if (check.isSimplified()) log.info("The chart " + chart.getName() + " is " + check);
		return new Drawn(renderer, model, theme);
	}

	/** Why the chart can't be drawn by any renderer here, or null */
	private static String unrenderable(ChartRef chart) {
		if (chart == null) return "no chart";
		if (ChartRenderer.getRenderer() == null) return "no chart renderer on the classpath (add docx4j-chart-render)";
		if (chart.isChartEx()) return "a chartex chart (" + chart.getChartPart().getPartName() + "), which the open renderer doesn't draw";
		return null;
	}

	private static ChartRenderCheck refused(String why) {
		return new ChartRenderCheck(java.util.Collections.singletonList(why), null);
	}

	private static Dimension2D size(ChartRef chart) throws Docx4JException {
		Dimension2D size = getSize(chart);
		if (size == null) throw new Docx4JException("The chart " + (chart == null ? "" : chart.getName())
				+ " has no size in its anchor: give one");
		return size;
	}

	private static void write(Document svg, OutputStream out) throws Docx4JException {
		try {
			Transformer t = XmlUtils.getTransformerFactory().newTransformer();
			t.setOutputProperty(OutputKeys.ENCODING, "UTF-8");
			t.transform(new DOMSource(svg), new StreamResult(out));
		} catch (Exception e) {
			throw new Docx4JException("Couldn't write the chart's SVG: " + e.getMessage(), e);
		}
	}

	private static void png(BufferedImage image, OutputStream out) throws Docx4JException {
		try {
			ImageIO.write(image, "png", out);
		} catch (IOException e) {
			throw new Docx4JException("Couldn't write the chart's PNG: " + e.getMessage(), e);
		}
	}
}
