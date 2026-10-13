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
 * Draws a chartex chart (Office 2016's waterfall, histogram, Pareto, box and whisker, treemap,
 * sunburst, funnel: a {@code cx:chartSpace}) as Office draws it, to a {@link Graphics2D}, to SVG,
 * to a picture (CR-039).  The same shape as {@link ChartRenderer}, on a {@link ChartExModel}, and
 * found separately through {@link ServiceLoader}, so that a provider of one never competes with a
 * provider of the other: docx4j-chart-render provides {@link ChartRenderer} (the classic chart
 * types); a chartex renderer (the Enterprise jar's, the charts plan's D1 (d)) provides this.
 * docx4j's own jars provide none, and a chartex chart's {@code mc:Fallback} is what the outputs
 * draw ({@link ChartExBranch}).
 *
 * <p>Sizes are points; a chart is drawn with its top left at the origin.  The theme is the
 * chart's host's ({@link ChartRef#getHostTheme()}), the fonts its host's
 * ({@link ChartFonts#of(org.docx4j.openpackaging.parts.Part)}).  {@link ChartExModel#getPart()}
 * leads to the chart's workbook, where an xlsx's chart has no caches.</p>
 *
 * <p>{@link #check} decides what the outputs draw: where it says a chart {@link ChartRenderCheck#isDrawn()
 * is drawn}, they draw it in place of its Fallback ({@link ChartExBranch}).</p>
 *
 * @since 17.3.3 (CR-039)
 */
public interface ChartExRenderer {

	/**
	 * What this renderer would do with the chart: refuse it, draw it simplified, or draw it.  A
	 * chart refused is drawn by the outputs as its Fallback.
	 */
	ChartRenderCheck check(ChartExModel chart, ThemeColors theme);

	/** The chart drawn, its top left at the origin, width by height points; a refused chart draws nothing */
	void draw(ChartExModel chart, ThemeColors theme, ChartFonts fonts, double width, double height, Graphics2D g);

	/**
	 * The chart as an SVG document, width by height points (one user unit a point), its text as
	 * text under the families docx4j's font mapping gives FOP; null where it is refused
	 */
	Document toSvg(ChartExModel chart, ThemeColors theme, ChartFonts fonts, double width, double height);

	/** The chart as a picture, dpi pixels an inch, on white; null where it is refused */
	default BufferedImage toImage(ChartExModel chart, ThemeColors theme, ChartFonts fonts, double width, double height,
			double dpi) {
		if (!check(chart, theme).isDrawn()) return null;
		return Renderers.picture(width, height, dpi, g -> draw(chart, theme, fonts, width, height, g));
	}

	// ------------------------------------------------------------------ lookup

	/** cache: looked up once */
	final class Holder {
		private Holder() {
		}

		static final Logger log = LoggerFactory.getLogger(ChartExRenderer.class);
		static volatile ChartExRenderer renderer;
		static volatile boolean resolved;
	}

	/** The chartex renderer on the classpath, or null; {@link #setRenderer} overrides it */
	static ChartExRenderer getRenderer() {
		if (!Holder.resolved) {
			synchronized (Holder.class) {
				if (!Holder.resolved) {
					ChartExRenderer found = Renderers.first(ChartExRenderer.class, Holder.log);
					if (found == null) {
						Holder.log.debug("No ChartExRenderer on the classpath; chartex charts are drawn as their Fallback.");
					}
					Holder.renderer = found;
					Holder.resolved = true;
				}
			}
		}
		return Holder.renderer;
	}

	/** Overrides the discovered renderer; null turns chartex drawing off */
	static void setRenderer(ChartExRenderer r) {
		synchronized (Holder.class) {
			Holder.renderer = r;
			Holder.resolved = true;
		}
	}
}
