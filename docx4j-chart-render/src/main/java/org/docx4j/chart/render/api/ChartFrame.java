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

import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;
import java.util.ArrayList;
import java.util.List;

/**
 * Where Office puts a chart's title and legend, and so where the plot area goes (CR-037's
 * measurements, R1 to R17): the title centred at the top, its lines wrapped to a share of the
 * chart; the legend at the top, bottom, left, right or top right, its entries in rows or a
 * column; the plot area in what they leave.  These are a classic chart's rules (a {@code c:title},
 * a {@code c:legend}); a chartex chart's {@code cx:title} and {@code cx:legend} also take an
 * alignment, which these don't.
 *
 * <p>Sizes are points, from the chart's top left.  The plot area's edges are an array: left, top,
 * right, bottom.</p>
 *
 * @since 17.3.3 (CR-039: from docx4j-chart-render's internals, CR-037)
 */
public final class ChartFrame {

	/** The chart area's inner frame: Office's elements are placed from 4pt inside the chart's box */
	public static final double INSET = 4;
	/** The title's top, in the inner frame */
	public static final double TITLE_TOP = 2;
	/** The title's box: its text, 3pt either side, 1.5pt above and below */
	public static final double TITLE_PAD_X = 3, TITLE_PAD_Y = 1.5;
	/** The title's lines are no wider than this share of the chart, less its padding (R4) */
	public static final double TITLE_MAX = 0.8;
	/** Between the title, the plot area and the legend */
	public static final double GAP = 11;
	/** The plot area from the inner frame's edges, where nothing else is beside it */
	public static final double PLOT_EDGE = 7;
	/** The legend from the inner frame's edge it is at */
	public static final double LEGEND_EDGE = 2;
	/** A legend key's side, a share of the legend's line; the key is inset half a key */
	public static final double LEGEND_KEY = 0.45;
	/**
	 * A horizontal legend is its entries plus this share of each entry's width, spread evenly
	 * before, between and after them (R1, R2, R4: one, two, three and eight entries; Calibri and Aptos)
	 */
	public static final double LEGEND_SLOT = 0.2;
	/** A vertical legend's entries are inset this much either side */
	public static final double LEGEND_SIDE = 1.5;
	/** A legend's entries are this far below its top */
	public static final double LEGEND_TOP = 3;
	/** A legend at the top (or top right) is this far below the title (r14-legend-top, r14-legend-top-right) */
	public static final double LEGEND_BELOW_TITLE = 6;
	/**
	 * A legend at the top right: the plot area this far below it, and this much higher off the
	 * bottom (r14-legend-top-right, one probe: provisional)
	 */
	public static final double LEGEND_TR_BELOW = 5, LEGEND_TR_BOTTOM = 6;
	/**
	 * A horizontal legend's entries too wide for {@link #LEGEND_MAX} of the chart go in rows, as many
	 * entries to each as the fewest rows allow, closed up; each row after the first adds a row's
	 * height less this (r14-legend-rows-8: eight entries in two rows of four; one probe: provisional)
	 */
	public static final double LEGEND_ROW_LESS = 1.5;
	/** A horizontal legend is at most this share of the chart's width, its entries closing up (r11-bar-w144) */
	public static final double LEGEND_MAX = 0.9;

	private ChartFrame() {
	}

	/** The plot area's edges where nothing takes room from it: left, top, right, bottom */
	public static double[] plotEdges(double w, double h) {
		double ix = INSET, iy = INSET, iw = w - 2 * INSET, ih = h - 2 * INSET;
		return new double[] { ix + PLOT_EDGE, iy + PLOT_EDGE, ix + iw - PLOT_EDGE, iy + ih - PLOT_EDGE };
	}

	// ------------------------------------------------------------------ the title

	/** How wide a title's lines may be ({@link TextWrap#wrap(List, double)} them to it) */
	public static double titleWidth(double w) {
		return TITLE_MAX * w - 2 * TITLE_PAD_X;
	}

	/** The title's box where Office puts it: its lines, padded, centred at the chart's top */
	public static Rectangle2D titleBox(double w, List<TextWrap.Line> lines) {
		double ix = INSET, iy = INSET, iw = w - 2 * INSET;
		double tw = 0, th = 0;
		for (TextWrap.Line l : lines) {
			tw = Math.max(tw, l.getWidth());
			th += l.getLineHeight();
		}
		double bw = tw + 2 * TITLE_PAD_X, bh = th + 2 * TITLE_PAD_Y;
		return new Rectangle2D.Double(ix + (iw - bw) / 2, iy + TITLE_TOP, bw, bh);
	}

	/**
	 * The plot area's top below a title's box where Office puts it ({@link #titleBox}): kept so
	 * where the title is placed by hand (r9-title-moved); not taken where the title is over the
	 * plot area (overlay)
	 */
	public static double belowTitle(Rectangle2D titleBox) {
		return titleBox.getY() + titleBox.getHeight() + GAP;
	}

	/** The title's lines in its box, each centred, its pieces one after another in their styles */
	public static List<Drawing.Text> titleTexts(Rectangle2D box, List<TextWrap.Line> lines) {
		List<Drawing.Text> texts = new ArrayList<>();
		double bx = box.getX(), bw = box.getWidth();
		double y = box.getY() + TITLE_PAD_Y;
		for (TextWrap.Line l : lines) {
			double x = bx + (bw - l.getWidth()) / 2;
			for (TextWrap.Seg sg : l.getSegs()) {
				double sw = sg.getStyle().width(sg.getText());
				texts.add(sg.getStyle().text(sg.getText(), x, y + l.getAscent() + 0.15, sw, 0, "title"));
				x += sw;
			}
			y += l.getLineHeight();
		}
		return texts;
	}

	// ------------------------------------------------------------------ the legend

	/** A legend's key's side for its text's style (a line key is as high, and wider) */
	public static double key(TextStyle s) {
		return LEGEND_KEY * s.getLineHeight();
	}

	/** A legend laid out: its box, its entries' places, and the plot area's edges it leaves */
	public static final class Legend {
		private final Rectangle2D box;
		private final List<double[]> entries;
		private final double[] plot;
		private final double cellWidth;

		Legend(Rectangle2D box, List<double[]> entries, double[] plot, double cellWidth) {
			this.box = box;
			this.entries = entries;
			this.plot = plot;
			this.cellWidth = cellWidth;
		}

		/** The box Office reports for the legend */
		public Rectangle2D getBox() {
			return box;
		}

		/** Each entry's place: its left, its top and its width (the widest's: Excel reports each at it) */
		public List<double[]> getEntries() {
			return entries;
		}

		/** The plot area's edges the legend leaves: left, top, right, bottom */
		public double[] getPlot() {
			return plot;
		}

		/** An entry's cell, where the legend is sized by hand ({@link ChartFrame#grid}) */
		public double getCellWidth() {
			return cellWidth;
		}
	}

	/**
	 * The legend where Office puts it: at the top or bottom ({@code t}, {@code b}) its entries in
	 * rows, each spaced by its own width, centred across the chart; at the left or right ({@code l},
	 * {@code r}) in a column centred on the plot area; at the top right ({@code tr}) in a column below
	 * the title, the plot area below it
	 *
	 * @param widths each entry's width: twice its key's, and its text's
	 * @param overlay the legend over the plot area, taking no room from it
	 * @param title the title's box as placed, or null for none
	 * @param plot the plot area's edges so far (below the title): left, top, right, bottom
	 */
	public static Legend legend(double w, double h, List<Double> widths, TextStyle s, String pos, boolean overlay, Rectangle2D title,
			double[] plot) {
		double ix = INSET, iy = INSET, iw = w - 2 * INSET, ih = h - 2 * INSET;
		double plotLeft = plot[0], plotTop = plot[1], plotRight = plot[2], plotBottom = plot[3];
		boolean horizontal = "b".equals(pos) || "t".equals(pos);
		// at the top, or the top right, the legend is below the title (or at the frame's edge)
		double top = title != null ? title.getMaxY() + LEGEND_BELOW_TITLE : iy + LEGEND_EDGE;
		double rowH = s.getLineHeight() + 5.97 - 0.0102 * s.getSize();
		double maxW = 0;
		for (double ew : widths) maxW = Math.max(maxW, ew);
		double lx, ly, lw, lh;
		List<double[]> at = new ArrayList<>();
		if (horizontal) {
			// each entry spaced by its own width, but reported (Excel) at the widest's (r1-pie)
			int k = widths.size();
			double sum = 0;
			for (double ew : widths) sum += ew;
			int rows = Math.max(1, (int) Math.ceil(sum / (LEGEND_MAX * w) - 1e-9));
			int perRow = (int) Math.ceil(k / (double) rows);
			rows = (int) Math.ceil(k / (double) perRow);
			if (rows == 1) {
				lw = Math.min((1 + LEGEND_SLOT) * sum, Math.max(sum, LEGEND_MAX * w));
			} else {
				lw = 0;
				for (int r = 0; r < rows; r++) {
					double rw = 0;
					for (int i = r * perRow; i < Math.min(k, (r + 1) * perRow); i++) rw += widths.get(i);
					lw = Math.max(lw, rw);
				}
			}
			double pad = rows == 1 ? (lw - sum) / (k + 1) : 0;
			lh = rows * rowH - (rows - 1) * LEGEND_ROW_LESS;
			lx = ix + (iw - lw) / 2;
			ly = "b".equals(pos) ? iy + ih - LEGEND_EDGE - lh : top;
			for (int r = 0; r < rows; r++) {
				double x = lx + pad;
				for (int i = r * perRow; i < Math.min(k, (r + 1) * perRow); i++) {
					at.add(new double[] { x, ly + LEGEND_TOP + r * rowH, maxW });
					x += widths.get(i) + pad;
				}
			}
			// a legend over the plot area (overlay) takes no room from it
			if (overlay) {
				// nothing
			} else if ("b".equals(pos)) plotBottom = ly - GAP;
			else plotTop = ly + lh + GAP;
		} else {
			lw = maxW + 3 + 2 * LEGEND_SIDE;
			lh = widths.size() * rowH;
			lx = "l".equals(pos) ? ix + LEGEND_EDGE : ix + iw - LEGEND_EDGE - lw;
			// centred on the plot area; at the top right, below the title, the plot area below it
			ly = "tr".equals(pos) ? top : (plotTop + plotBottom) / 2 - lh / 2;
			for (int i = 0; i < widths.size(); i++) at.add(new double[] { lx + LEGEND_SIDE, ly + LEGEND_TOP + i * rowH, maxW + 3 });
			if (overlay) {
				// nothing
			} else if ("l".equals(pos)) plotLeft = lx + lw + GAP;
			else plotRight = lx - GAP;
			if ("tr".equals(pos) && !overlay) {
				plotTop = ly + lh + LEGEND_TR_BELOW;
				plotBottom -= LEGEND_TR_BOTTOM;
			}
		}
		return new Legend(new Rectangle2D.Double(lx, ly, lw, lh), at, new double[] { plotLeft, plotTop, plotRight, plotBottom },
				horizontal ? maxW : maxW + 3);
	}

	/**
	 * A legend sized by hand: its k entries in a grid of as many columns as hold the widest, each
	 * centred in its cell, 0.75pt right and 0.055pt down (r9-legend-moved, r15-legend-right-sized)
	 *
	 * @param cellWidth {@link Legend#getCellWidth()}
	 */
	public static List<double[]> grid(Rectangle2D box, int k, double cellWidth, TextStyle s) {
		int cols = Math.max(1, Math.min(k, (int) Math.floor(box.getWidth() / cellWidth)));
		int rows = (int) Math.ceil(k / (double) cols);
		double cw = box.getWidth() / cols, ch = box.getHeight() / rows;
		List<double[]> at = new ArrayList<>();
		for (int i = 0; i < k; i++) {
			at.add(new double[] { box.getX() + (i % cols) * cw + (cw - cellWidth) / 2 + 0.75,
					box.getY() + (i / cols) * ch + (ch - s.getLineHeight()) / 2 + 0.055, cellWidth });
		}
		return at;
	}

	/** The box Office reports for an entry: its place's, a line high */
	public static Rectangle2D entryBox(double[] entry, TextStyle s) {
		return new Rectangle2D.Double(entry[0], entry[1], entry[2], s.getLineHeight());
	}

	/** The box Office reports for an entry's key, keyWidth wide (a line key's, else {@link #key}) */
	public static Rectangle2D keyBox(double[] entry, double keyWidth, TextStyle s) {
		double key = key(s);
		return new Rectangle2D.Double(entry[0] + keyWidth / 2, entry[1] + key / 2, keyWidth, key);
	}

	/** Where an entry's key is drawn: its top left, 0.75pt right and 0.055 of the text's size down of {@link #keyBox} */
	public static Point2D keyAt(double[] entry, double keyWidth, TextStyle s) {
		double key = key(s);
		return new Point2D.Double(entry[0] + keyWidth / 2 + 0.75, entry[1] + key / 2 + 0.055 * s.getSize());
	}

	/** Where an entry's text starts */
	public static double textX(double[] entry, double keyWidth, TextStyle s) {
		return entry[0] + 2 * keyWidth + (2.94 / 10.986 - LEGEND_KEY / 2) * s.getLineHeight();
	}

	/** An entry's text's baseline */
	public static double textBaseline(double[] entry, TextStyle s) {
		return entry[1] + s.getAscent() * 0.956;
	}
}
