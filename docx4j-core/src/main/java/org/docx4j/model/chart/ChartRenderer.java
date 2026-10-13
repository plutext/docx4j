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

import java.awt.Graphics2D;
import java.awt.image.BufferedImage;
import java.util.ServiceLoader;

import org.docx4j.model.color.ThemeColors;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.w3c.dom.Document;

/**
 * Draws a chart as Office draws it (CR-037): to a {@link Graphics2D}, to SVG, to a picture.
 * Found through {@link ServiceLoader}, as {@code MetafileSvgProvider} is: docx4j-chart-render
 * provides the classic chart types (review A7: on the read model, so that a chart can be drawn
 * from a {@link ChartModel} without its package); the Enterprise jar, chartex (the charts plan's
 * D1 (d)), through its own interface, {@link ChartExRenderer}, so that the two never compete
 * (CR-039).  docx4j-core on its own has none, and the outputs keep a chart's box empty
 * ({@link ChartBox}).
 *
 * <p>Sizes are points; a chart is drawn with its top left at the origin.  The theme is the
 * chart's host's ({@link ChartRef#getHostTheme()}), the fonts its host's
 * ({@link ChartFonts#of(org.docx4j.openpackaging.parts.Part)}).</p>
 *
 * @since 17.3.3 (CR-037)
 */
public interface ChartRenderer {

	/**
	 * What this renderer would do with the chart: refuse it (a structural feature it doesn't
	 * draw), draw it simplified (a cosmetic one), or draw it (review A1)
	 */
	ChartRenderCheck check(ChartModel chart, ThemeColors theme);

	/** The chart drawn, its top left at the origin, width by height points; a refused chart draws nothing */
	void draw(ChartModel chart, ThemeColors theme, ChartFonts fonts, double width, double height, Graphics2D g);

	/**
	 * The chart as an SVG document, width by height points (one user unit a point), its text as
	 * text under the families docx4j's font mapping gives FOP (D6); null where it is refused
	 */
	Document toSvg(ChartModel chart, ThemeColors theme, ChartFonts fonts, double width, double height);

	/** The chart as a picture, dpi pixels an inch, on white; null where it is refused */
	default BufferedImage toImage(ChartModel chart, ThemeColors theme, ChartFonts fonts, double width, double height,
			double dpi) {
		if (!check(chart, theme).isDrawn()) return null;
		return Renderers.picture(width, height, dpi, g -> draw(chart, theme, fonts, width, height, g));
	}

	// ------------------------------------------------------------------ lookup

	/** cache: looked up once */
	final class Holder {
		private Holder() {
		}

		static final Logger log = LoggerFactory.getLogger(ChartRenderer.class);
		static volatile ChartRenderer renderer;
		static volatile boolean resolved;
	}

	/** The renderer on the classpath (docx4j-chart-render's), or null; {@link #setRenderer} overrides it */
	static ChartRenderer getRenderer() {
		if (!Holder.resolved) {
			synchronized (Holder.class) {
				if (!Holder.resolved) {
					ChartRenderer found = Renderers.first(ChartRenderer.class, Holder.log);
					if (found == null) {
						Holder.log.debug("No ChartRenderer on the classpath; charts keep their box (add docx4j-chart-render).");
					}
					Holder.renderer = found;
					Holder.resolved = true;
				}
			}
		}
		return Holder.renderer;
	}

	/** Overrides the discovered renderer; null turns chart drawing off */
	static void setRenderer(ChartRenderer r) {
		synchronized (Holder.class) {
			Holder.renderer = r;
			Holder.resolved = true;
		}
	}
}
