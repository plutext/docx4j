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
package org.docx4j.chart.render.api;

import java.awt.Dimension;
import java.awt.RenderingHints;
import java.util.Locale;

import org.apache.batik.dom.GenericDOMImplementation;
import org.apache.batik.svggen.SVGGeneratorContext;
import org.apache.batik.svggen.SVGGraphics2D;
import org.w3c.dom.DOMImplementation;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

/**
 * A {@link Drawing} as an SVG document, as docx4j's outputs want a chart's: one user unit a
 * point, the frame filled exactly; its text as text under the families docx4j's font mapping gives
 * FOP (CR-037 D6), or as outlines on {@link #TEXT_AS_SHAPES}; DrawingML's gradients (any number of
 * stops, linear or radial) as SVG gradients, which Batik's generator alone doesn't write; shadows,
 * glows and patterns' tiles as PNG pictures.
 *
 * @since 17.3.3 (CR-039: from docx4j-chart-render's internals, CR-037)
 */
public final class SvgWriter {

	private static final String SVG_NS = "http://www.w3.org/2000/svg";

	/**
	 * The property which, set true, writes a chart's text in its SVG as glyph outlines rather
	 * than {@code <text>} (CR-037 D6): drawn alike whatever fonts the reader of the SVG has, but
	 * no longer text in a PDF (not searchable, not selectable) and larger.  False by default.
	 */
	public static final String TEXT_AS_SHAPES = "docx4j.chart.render.textAsShapes";

	private SvgWriter() {
	}

	/** The drawing as SVG, its width by height points */
	public static Document toSvg(Drawing drawing) {
		double width = drawing.getWidth(), height = drawing.getHeight();
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
		g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
		g.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
		g.setRenderingHint(RenderingHints.KEY_FRACTIONALMETRICS, RenderingHints.VALUE_FRACTIONALMETRICS_ON);
		g.setRenderingHint(RenderingHints.KEY_STROKE_CONTROL, RenderingHints.VALUE_STROKE_PURE);
		drawing.paint(g, false);
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
	}

	/** at most four decimals, no trailing zeros */
	private static String length(double v) {
		String s = String.format(Locale.ROOT, "%.4f", v);
		while (s.contains(".") && (s.endsWith("0") || s.endsWith("."))) s = s.substring(0, s.length() - 1);
		return s;
	}
}
