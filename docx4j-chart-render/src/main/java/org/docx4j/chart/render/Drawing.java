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

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.Shape;
import java.awt.geom.AffineTransform;
import java.awt.geom.Rectangle2D;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * A chart laid out: what is painted, in painting order, in points from the chart's top left,
 * and the boxes Office's object model reports for the same chart (CR-037 §11: the plot area,
 * the title, the legend and its entries, each point, a pie's slices), so that a layout can be
 * compared with Excel's without painting it.
 */
final class Drawing {

	/** A filled or stroked shape: a bar, a gridline, the chart area, a legend key */
	static final class Area {
		final Shape shape;
		final Color fill;
		final Color line;
		final double lineWidth;
		final String role;
		/** what it is drawn within (a bar, the plot's inside: Office clips there), or null */
		final Shape clip;
		/** its line's ends and joins rounded (a line series'), else butt and mitred */
		final boolean round;

		Area(Shape shape, Color fill, Color line, double lineWidth, String role) {
			this(shape, fill, line, lineWidth, role, null, false);
		}

		Area(Shape shape, Color fill, Color line, double lineWidth, String role, Shape clip) {
			this(shape, fill, line, lineWidth, role, clip, false);
		}

		Area(Shape shape, Color fill, Color line, double lineWidth, String role, Shape clip, boolean round) {
			this.shape = shape;
			this.fill = fill;
			this.line = line;
			this.lineWidth = lineWidth;
			this.role = role;
			this.clip = clip;
			this.round = round;
		}
	}

	/** A line of text, from its baseline's start, turned by angle degrees (clockwise) about it */
	static final class Text {
		final String text;
		/** the face the chart names (Calibri), where {@link #font} is the file it is drawn in (Carlito) */
		final String face;
		final Font font;
		final Color colour;
		final double x;
		final double baseline;
		final double width;
		final double angle;
		final String role;

		Text(String text, String face, Font font, Color colour, double x, double baseline, double width, double angle, String role) {
			this.text = text;
			this.face = face;
			this.font = font;
			this.colour = colour;
			this.x = x;
			this.baseline = baseline;
			this.width = width;
			this.angle = angle;
			this.role = role;
		}
	}

	final double width;
	final double height;
	final List<Object> items = new ArrayList<>();

	/** The boxes Office reports, by name: title, legend, legend.entry.N, legend.key.N, plotArea, inside, point.S.P */
	final Map<String, Rectangle2D> boxes = new LinkedHashMap<>();

	/**
	 * A pie's or doughnut's slices, by point (point.S.P), at the places Office reports
	 * ({@code PieSliceLocation}, 1 to 9: the end's outer point, the arc's middle, the start's outer
	 * point, the start edge's middle, the centre, the end edge's middle, then a doughnut's inner
	 * start, inner arc's middle and inner end); null where a pie has none
	 */
	final Map<String, double[][]> slices = new LinkedHashMap<>();

	/** The value axis as laid out: min, max, major unit */
	double[] valueAxis;

	/** A scatter chart's x axis as laid out: min, max, major unit; a date axis's first and last dates and major unit */
	double[] xAxis;

	Drawing(double width, double height) {
		this.width = width;
		this.height = height;
	}

	void add(Area a) {
		items.add(a);
	}

	void add(Text t) {
		items.add(t);
	}

	/** A line across or down (a gridline, an axis) */
	private static boolean level(Shape s) {
		if (!(s instanceof java.awt.geom.Line2D)) return false;
		java.awt.geom.Line2D l = (java.awt.geom.Line2D) s;
		return l.getX1() == l.getX2() || l.getY1() == l.getY2();
	}

	/** Paints the items in order */
	void paint(Graphics2D g) {
		paint(g, false);
	}

	/**
	 * Paints the items in order; for a picture (raster), with a rectangle's edges on whole pixels,
	 * as Excel's pictures have them (a bar's edge is its colour or the background, not a blend)
	 */
	void paint(Graphics2D g, boolean raster) {
		for (Object o : items) {
			if (o instanceof Area) {
				Area a = (Area) o;
				Shape saved = g.getClip();
				Object aa = g.getRenderingHint(RenderingHints.KEY_ANTIALIASING);
				boolean crisp = raster && (a.shape instanceof Rectangle2D || level(a.shape));
				if (crisp) g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_OFF);
				if (a.clip != null) g.clip(a.clip);
				if (a.fill != null) {
					g.setColor(a.fill);
					g.fill(a.shape);
				}
				if (a.line != null && a.lineWidth > 0) {
					g.setColor(a.line);
					g.setStroke(a.round ? new BasicStroke((float) a.lineWidth, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND)
							: new BasicStroke((float) a.lineWidth, BasicStroke.CAP_BUTT, BasicStroke.JOIN_MITER));
					g.draw(a.shape);
				}
				if (a.clip != null) g.setClip(saved);
				if (crisp) g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, aa);
			} else {
				Text t = (Text) o;
				g.setColor(t.colour);
				g.setFont(t.font);
				if (t.angle == 0) {
					g.drawString(t.text, (float) t.x, (float) t.baseline);
				} else {
					AffineTransform saved = g.getTransform();
					g.rotate(Math.toRadians(t.angle), t.x, t.baseline);
					g.drawString(t.text, (float) t.x, (float) t.baseline);
					g.setTransform(saved);
				}
			}
		}
	}
}
