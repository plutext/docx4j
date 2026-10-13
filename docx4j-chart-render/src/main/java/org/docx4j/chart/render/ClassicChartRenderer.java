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

import java.awt.Dimension;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.image.BufferedImage;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import org.apache.batik.dom.GenericDOMImplementation;
import org.apache.batik.svggen.SVGGeneratorContext;
import org.apache.batik.svggen.SVGGraphics2D;
import org.docx4j.dml.CTShapeProperties;
import org.docx4j.dml.chart.CTChartSpace;
import org.docx4j.dml.chart.CTLayout;
import org.docx4j.model.chart.ChartAxis;
import org.docx4j.model.chart.ChartDataLabels;
import org.docx4j.model.chart.ChartFill;
import org.docx4j.model.chart.ChartFonts;
import org.docx4j.model.chart.ChartGroup;
import org.docx4j.model.chart.ChartModel;
import org.docx4j.model.chart.ChartRenderCheck;
import org.docx4j.model.chart.ChartRenderer;
import org.docx4j.model.chart.ChartSeries;
import org.docx4j.model.color.ThemeColors;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.w3c.dom.DOMImplementation;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

/**
 * docx4j's renderer of the classic chart types (CR-037): the chart laid out as Office lays it out
 * ({@link ChartLayout}), painted onto a {@code Graphics2D}, Batik's SVG generator's for SVG.
 *
 * <p>Draws the column and bar charts (clustered, stacked, 100%), line and area charts over
 * categories or dates, scatter charts (markers, straight or smoothed lines), pies and doughnuts,
 * with their data labels, axis titles, manual layouts, axis options, fills (solid, gradients,
 * patterns), lines (dashed) and shadows and glows; refuses (its box stays empty) the other kinds,
 * combinations, secondary axes and 3-D (CR-037 section 4.6), and draws simplified (saying so) a
 * picture fill, inner and preset shadows, reflections and soft edges.</p>
 *
 * @since 17.3.3 (CR-037)
 */
public class ClassicChartRenderer implements ChartRenderer {

	private static final Logger log = LoggerFactory.getLogger(ClassicChartRenderer.class);

	private static final String SVG_NS = "http://www.w3.org/2000/svg";

	/**
	 * The property which, set true, writes a chart's text in its SVG as glyph outlines rather
	 * than {@code <text>} (CR-037 D6): drawn alike whatever fonts the reader of the SVG has, but
	 * no longer text in a PDF (not searchable, not selectable) and larger.  False by default.
	 *
	 * @since 17.3.3
	 */
	public static final String TEXT_AS_SHAPES = "docx4j.chart.render.textAsShapes";

	public ClassicChartRenderer() {
	}

	@Override
	public ChartRenderCheck check(ChartModel chart, ThemeColors theme) {
		List<String> refused = new ArrayList<>();
		List<String> simplified = new ArrayList<>();
		if (chart == null) {
			refused.add("no chart");
			return new ChartRenderCheck(refused, simplified);
		}
		if (chart.getGroups().size() != 1) refused.add(chart.getGroups().size() + " chart groups (a combination)");
		for (ChartGroup g : chart.getGroups()) {
			switch (g.getKind()) {
			case BAR:
			case LINE:
			case AREA:
			case PIE:
			case DOUGHNUT:
			case SCATTER:
				break;
			default:
				refused.add("a " + g.getKind().value() + " chart");
			}
			for (ChartSeries s : g.getShownSeries()) {
				fill(s.getFill(), "a series' fill", simplified);
			}
		}
		int value = 0, category = 0;
		for (ChartAxis a : chart.getAxes()) {
			if (a.getKind() == ChartAxis.Kind.VALUE) value++;
			else if (a.getKind() == ChartAxis.Kind.CATEGORY || a.getKind() == ChartAxis.Kind.DATE) category++;
			else refused.add("a " + a.getKind() + " axis");
			if ("r".equals(a.getPosition()) || "t".equals(a.getPosition())) refused.add("an axis at the " + a.getPosition());
		}
		// a scatter chart's two axes are both value axes
		boolean scatter = chart.getGroups().stream().anyMatch(g -> g.getKind() == ChartGroup.Kind.SCATTER);
		if (scatter ? value > 2 || category > 0 : value > 1 || category > 1) refused.add("a secondary axis");
		CTChartSpace cs = chart.getSource();
		if (cs.getChart() != null) {
			if (cs.getChart().getView3D() != null && chart.getGroups().stream().anyMatch(g -> g.getKind().value().endsWith("3D"))) {
				refused.add("3-D");
			}
		}
		effects(cs.getSpPr(), "the chart area", simplified);
		for (ChartGroup g : chart.getGroups()) {
			for (ChartSeries s : g.getShownSeries()) {
				Object sp = null;
				try {
					sp = s.getSource().getClass().getMethod("getSpPr").invoke(s.getSource());
				} catch (ReflectiveOperationException e) {
					// none
				}
				if (sp instanceof CTShapeProperties) effects((CTShapeProperties) sp, "a series", simplified);
			}
		}
		return new ChartRenderCheck(dedupe(refused), dedupe(simplified));
	}

	private static boolean shown(ChartDataLabels l) {
		if (l == null || Boolean.TRUE.equals(l.getDeleted())) return false;
		return Boolean.TRUE.equals(l.getShowValue()) || Boolean.TRUE.equals(l.getShowCategoryName())
				|| Boolean.TRUE.equals(l.getShowSeriesName()) || Boolean.TRUE.equals(l.getShowPercent())
				|| Boolean.TRUE.equals(l.getShowBubbleSize());
	}

	private static boolean manual(CTLayout l) {
		return l != null && l.getManualLayout() != null;
	}

	private static void fill(ChartFill f, String what, List<String> simplified) {
		if (f == null) return;
		switch (f.getKind()) {
		case PICTURE:
			simplified.add(what + ": a picture drawn as grey");
			break;
		default:
		}
	}

	/** Effects drawn simplified or not at all (outer shadows and glows are drawn, as pictures, as Office's PDFs have them) */
	private static void effects(CTShapeProperties sp, String what, List<String> simplified) {
		if (sp == null || sp.getEffectLst() == null) return;
		org.docx4j.dml.CTEffectList e = sp.getEffectLst();
		if (e.getInnerShdw() != null) simplified.add(what + ": its inner shadow not drawn");
		if (e.getPrstShdw() != null) simplified.add(what + ": its preset shadow not drawn");
		if (e.getReflection() != null) simplified.add(what + ": its reflection not drawn");
		if (e.getSoftEdge() != null) simplified.add(what + ": its soft edges not drawn");
	}

	private static List<String> dedupe(List<String> l) {
		List<String> out = new ArrayList<>();
		for (String s : l) if (!out.contains(s)) out.add(s);
		return out;
	}

	/** The chart laid out (for the gate, which compares it with Office's layout) */
	Drawing layout(ChartModel chart, ThemeColors theme, ChartFonts fonts, double width, double height) {
		return ChartLayout.layout(chart, theme, fonts, width, height);
	}

	@Override
	public void draw(ChartModel chart, ThemeColors theme, ChartFonts fonts, double width, double height, Graphics2D g) {
		draw(chart, theme, fonts, width, height, g, false);
	}

	private void draw(ChartModel chart, ThemeColors theme, ChartFonts fonts, double width, double height, Graphics2D g, boolean raster) {
		if (!check(chart, theme).isDrawn()) return;
		g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
		g.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
		g.setRenderingHint(RenderingHints.KEY_FRACTIONALMETRICS, RenderingHints.VALUE_FRACTIONALMETRICS_ON);
		g.setRenderingHint(RenderingHints.KEY_STROKE_CONTROL, RenderingHints.VALUE_STROKE_PURE);
		layout(chart, theme, fonts, width, height).paint(g, raster);
	}

	/** The chart as a picture, its rectangles' edges on whole pixels as Excel's pictures have them */
	@Override
	public BufferedImage toImage(ChartModel chart, ThemeColors theme, ChartFonts fonts, double width, double height, double dpi) {
		if (!check(chart, theme).isDrawn()) return null;
		double scale = dpi / 72.0;
		BufferedImage image = new BufferedImage((int) Math.max(1, Math.ceil(width * scale)), (int) Math.max(1, Math.ceil(height * scale)),
				BufferedImage.TYPE_INT_ARGB);
		Graphics2D g = image.createGraphics();
		try {
			g.setColor(java.awt.Color.WHITE);
			g.fillRect(0, 0, image.getWidth(), image.getHeight());
			g.scale(scale, scale);
			draw(chart, theme, fonts, width, height, g, true);
		} finally {
			g.dispose();
		}
		return image;
	}

	@Override
	public Document toSvg(ChartModel chart, ThemeColors theme, ChartFonts fonts, double width, double height) {
		if (!check(chart, theme).isDrawn()) return null;
		try {
			DOMImplementation impl = GenericDOMImplementation.getDOMImplementation();
			Document doc = impl.createDocument(SVG_NS, "svg", null);
			SVGGeneratorContext ctx = SVGGeneratorContext.createDefault(doc);
			ctx.setComment(null);
			ctx.setPrecision(4);
			// DrawingML's gradients: any number of stops, linear or radial
			ctx.setExtensionHandler(new GradientHandler());
			// pictures (shadows, glows, a pattern's tile) as PNG data, by the JDK's own writer (Batik's
			// default needs batik-codec)
			ctx.setImageHandler(new org.apache.batik.svggen.ImageHandlerBase64Encoder() {
				@Override
				public void encodeImage(java.awt.image.RenderedImage image, java.io.OutputStream os)
						throws org.apache.batik.svggen.SVGGraphics2DIOException {
					try {
						javax.imageio.ImageIO.write(image, "png", os);
					} catch (java.io.IOException e) {
						throw new org.apache.batik.svggen.SVGGraphics2DIOException(e);
					}
				}
			});
			// text as text, under the families docx4j's mapping gives FOP; outlines on the
			// property (D6), as for a metafile's
			SVGGraphics2D g = new SVGGraphics2D(ctx, org.docx4j.Docx4jProperties.getProperty(TEXT_AS_SHAPES, false));
			g.setSVGCanvasSize(new Dimension((int) Math.max(1, Math.ceil(width)), (int) Math.max(1, Math.ceil(height))));
			draw(chart, theme, fonts, width, height, g);
			Element root = doc.getDocumentElement();
			g.getRoot(root);
			// one user unit a point, the frame filled exactly (as BatikMetafileSvgProvider's)
			root.setAttributeNS(null, "width", length(width) + "pt");
			root.setAttributeNS(null, "height", length(height) + "pt");
			root.setAttributeNS(null, "viewBox", "0 0 " + length(width) + " " + length(height));
			root.setAttributeNS(null, "preserveAspectRatio", "none");
			// each text is placed and split at its kerned pairs already (Drawing): a browser
			// kerns an SVG's text and makes ligatures by default, FOP doesn't, and neither should
			root.setAttributeNS(null, "style", "font-kerning: none; font-variant-ligatures: none");
			return doc;
		} catch (RuntimeException e) {
			log.warn("Could not draw the chart as SVG: " + e, e);
			return null;
		}
	}

	/** at most four decimals, no trailing zeros */
	private static String length(double v) {
		String s = String.format(Locale.ROOT, "%.4f", v);
		while (s.contains(".") && (s.endsWith("0") || s.endsWith("."))) s = s.substring(0, s.length() - 1);
		return s;
	}
}
