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

import java.awt.Color;
import java.awt.Font;
import java.awt.Shape;
import java.awt.font.FontRenderContext;
import java.awt.font.LineMetrics;
import java.awt.geom.Arc2D;
import java.awt.geom.Ellipse2D;
import java.awt.geom.Line2D;
import java.awt.geom.Path2D;
import java.awt.geom.Rectangle2D;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Locale;

import org.docx4j.Docx4jProperties;
import org.docx4j.dml.CTLineProperties;
import org.docx4j.dml.CTShapeProperties;
import org.docx4j.dml.CTTextBody;
import org.docx4j.dml.CTTextCharacterProperties;
import org.docx4j.dml.CTTextParagraph;
import org.docx4j.dml.chart.CTCatAx;
import org.docx4j.dml.chart.CTChartLines;
import org.docx4j.dml.chart.CTChartSpace;
import org.docx4j.dml.chart.CTDPt;
import org.docx4j.dml.chart.CTDateAx;
import org.docx4j.dml.chart.CTLegend;
import org.docx4j.dml.chart.CTMarker;
import org.docx4j.dml.chart.CTTickMark;
import org.docx4j.dml.chart.CTTitle;
import org.docx4j.dml.chart.CTValAx;
import org.docx4j.model.chart.ChartAxis;
import org.docx4j.model.chart.ChartColorStyles;
import org.docx4j.model.chart.ChartData;
import org.docx4j.model.chart.ChartFill;
import org.docx4j.model.chart.ChartFonts;
import org.docx4j.model.chart.ChartGroup;
import org.docx4j.model.chart.ChartModel;
import org.docx4j.model.chart.ChartSeries;
import org.docx4j.model.color.DrawingMLColors;
import org.docx4j.model.color.ResolvedColor;
import org.docx4j.model.color.ThemeColors;
import org.xlsx4j.org.apache.poi.ss.usermodel.DataFormatter;

/**
 * A chart laid out as Office lays it out (CR-037 phases 1 and 2): the frame (the title, the
 * legend, the plot area), then the plot of its kind: columns and bars (clustered, stacked, 100%),
 * lines and areas over categories or dates, a scatter chart's points over two value axes, a pie's
 * or doughnut's slices.  The rules are those measured on Excel's layout of the render probes (§11).
 * Office reports a chart's elements from an origin {@link #INSET} inside the chart's box; the
 * constants are in that frame unless they say otherwise.
 */
final class ChartLayout {

	/** The chart area's inner frame: Office's elements are placed from 4pt inside the chart's box */
	static final double INSET = 4;
	/** The title's top, in the inner frame */
	static final double TITLE_TOP = 2;
	/** The title's box: its text, 3pt either side, 1.5pt above and below */
	static final double TITLE_PAD_X = 3, TITLE_PAD_Y = 1.5;
	/** The title's lines are no wider than this share of the chart, less its padding (R4) */
	static final double TITLE_MAX = 0.8;
	/** Between the title, the plot area and the legend */
	static final double GAP = 11;
	/** The plot area from the inner frame's edges, where nothing else is beside it */
	static final double PLOT_EDGE = 7;
	/** The legend from the inner frame's edge it is at */
	static final double LEGEND_EDGE = 2;
	/** A legend key's side, a share of the legend's line; the key is inset half a key */
	static final double LEGEND_KEY = 0.45;
	/**
	 * A horizontal legend is its entries plus this share of each entry's width, spread evenly
	 * before, between and after them (R1, R2, R4: one, two, three and eight entries; Calibri and Aptos)
	 */
	static final double LEGEND_SLOT = 0.2;
	/** A vertical legend's entries are inset this much either side */
	static final double LEGEND_SIDE = 1.5;
	/** A legend's entries are this far below its top */
	static final double LEGEND_TOP = 3;
	/** A legend at the top (or top right) is this far below the title (r14-legend-top, r14-legend-top-right) */
	static final double LEGEND_BELOW_TITLE = 6;
	/**
	 * A legend at the top right: the plot area this far below it, and this much higher off the
	 * bottom (r14-legend-top-right, one probe: provisional)
	 */
	static final double LEGEND_TR_BELOW = 5, LEGEND_TR_BOTTOM = 6;
	/**
	 * A horizontal legend's entries too wide for {@link #LEGEND_MAX} of the chart go in rows, as many
	 * entries to each as the fewest rows allow, closed up; each row after the first adds a row's
	 * height less this (r14-legend-rows-8: eight entries in two rows of four; one probe: provisional)
	 */
	static final double LEGEND_ROW_LESS = 1.5;
	/** A horizontal legend is at most this share of the chart's width, its entries closing up (r11-bar-w144) */
	static final double LEGEND_MAX = 0.9;
	/** A line series' legend key is this wide (R1, at 9pt), its line drawn this many times as long, centred (Word's PDF) */
	static final double LINE_KEY = 12, LINE_KEY_DRAWN = 1.6;
	/** The widest value label's left edge is this far left of the plot area's (6.53pt from the chart's edge) */
	static final double VALUE_LABEL_OVERHANG = 4.47;
	/** Between a value label's right edge and the plot's inside, a share of the label's line */
	static final double VALUE_LABEL_GAP = 0.7575;
	/** The top value label may stand this far above the plot area before the inside drops */
	static final double VALUE_LABEL_TOP = 6;
	/**
	 * A horizontal value axis's unit (a bar chart's, a scatter chart's x axis) gives intervals of at
	 * least this share of its labels' text size across the whole plot area, the category labels'
	 * band included: Excel chooses it before it lays out either axis's labels.  R14's sweeps change
	 * unit at the same chart widths whatever the labels (1 to 60000, 0% to 100%, a scatter's x):
	 * 3.789 to 3.814 at 9pt, 3.761 to 3.833 at 7pt, 3.778 to 3.819 at 12pt.  Where the labels at
	 * that unit then overlap, the unit is doubled and the bounds kept (see {@link #acrossScale})
	 */
	static final double ACROSS = 3.8;
	/** See the push of labels turned 45 degrees in the layout: 10.94pt (R12, R13; 10.87 to 10.96) */
	static final double TURNED_PUSH = 10.93;
	/**
	 * A small plot's inside is at least this share of the plot area's height less the category band's
	 * gap, the labels' band taking the rest (r14-sweep-column-h, 72 to 92pt high, to 0.01pt; at 14pt
	 * r11-text14-h108, whose inside starts below the top value label's overhang; R11's and R12's
	 * "about 0.56 of the plot" was this near 20pt of plot)
	 */
	static final double CATEGORY_CAP = 0.5;
	/** A tick mark's length, a share of its axis's text size (r1-area: 2.85pt at 9pt in Word's PDF, 3.80 at 12pt in PowerPoint's) */
	static final double TICK = 0.3167;
	/**
	 * A date axis's labels, turned 45 degrees, are at least this many lines apart square to them,
	 * and turned 90 this many lines apart along the axis; where neither, an automatic major unit
	 * goes up to the next of 1, 2 and 7 days (r14-sweep-dates: 13.101 to 13.111pt at 9pt in both
	 * tests; R5's daily dates, 18.25pt a label, turned 90).  Text labels need a line (R12), and so
	 * do dates with a space in them, which turn 45 as text does (r14-dates-long's "1 January 2026"
	 * at 18.25pt a label; provisional)
	 */
	static final double DATE_ROOM = 1.193;
	/**
	 * A date label is wrapped at its hyphen only where it is at most this share wider than its room
	 * ("22-Jan", 23.80pt: wrapped from 23.03 to 23.14pt of room, turned 45 below; r14-sweep-dates,
	 * one label width: provisional)
	 */
	static final double DATE_WRAP = 1.031;
	/** How the category labels are set (R12) */
	static final int LEVEL = 0, WRAPPED = 1, TURNED_45 = 2, TURNED_90 = 3;
	/**
	 * Builtin number format 14, which Office shows in the machine's short date (the VM's: d/mm/yyyy,
	 * r1-area); docx4j shows it as the property {@code docx4j.chart.render.shortDate} says, by default as written
	 */
	static final String SHORT_DATE = "m/d/yyyy";

	private static final FontRenderContext FRC = new FontRenderContext(null, true, true);
	private static final double SIN45 = Math.sqrt(0.5);

	/**
	 * A text style: a chart's c:txPr, inherited from the chart space's, else Office's default.
	 * Text at or above its kern size (DrawingML's {@code kern}, hundredths of a point; Office's
	 * charts write 1200) is kerned, as Office kerns it (R4's long title: its width is Carlito's
	 * kerned advances, to 0.01pt); an axis's labels never are (r14-kerning: sixteen pairs as 14pt
	 * category labels, kern 1200, none kerned)
	 */
	static final class Style {
		final String typeface;
		final double size;
		final boolean bold;
		final boolean italic;
		final Color colour;
		final int kern;
		final Font font;
		final double ascent, line;

		Style(ChartFonts fonts, String typeface, double size, boolean bold, boolean italic, Color colour, int kern) {
			this.typeface = typeface;
			this.size = size;
			this.bold = bold;
			this.italic = italic;
			this.colour = colour;
			this.kern = kern;
			Font f = fonts.font(typeface, bold, italic, size);
			if (kern > 0 && size * 100 >= kern) {
				f = f.deriveFont(java.util.Collections.singletonMap(java.awt.font.TextAttribute.KERNING,
						java.awt.font.TextAttribute.KERNING_ON));
			}
			this.font = f;
			LineMetrics lm = font.getLineMetrics("Xg", FRC);
			this.ascent = lm.getAscent();
			this.line = lm.getAscent() + lm.getDescent() + lm.getLeading();
		}

		double width(String s) {
			return s.isEmpty() ? 0 : font.getStringBounds(s, FRC).getWidth();
		}
	}

	/** A legend entry: a series', or a point's where the colours vary by point */
	private static final class Entry {
		final String name;
		final Color fill, line;
		final double lineWidth;
		/** a line series' key: its line, and its marker over it */
		final boolean lineKey;
		final Marker marker;

		Entry(String name, Color fill, Color line, double lineWidth, boolean lineKey, Marker marker) {
			this.name = name;
			this.fill = fill;
			this.line = line;
			this.lineWidth = lineWidth;
			this.lineKey = lineKey;
			this.marker = marker;
		}
	}

	/** A series' marker: its symbol, size in points, fill and line */
	private static final class Marker {
		final String symbol;
		final double size;
		final Color fill, line;
		final double lineWidth;

		Marker(String symbol, double size, Color fill, Color line, double lineWidth) {
			this.symbol = symbol;
			this.size = size;
			this.fill = fill;
			this.line = line;
			this.lineWidth = lineWidth;
		}

		/** This marker no larger than a size (a legend key's: r14-legend-line-7, 3.84pt for 5) */
		Marker atMost(double max) {
			return size <= max ? this : new Marker(symbol, max, fill, line, lineWidth);
		}
	}

	private final ChartModel model;
	private final ThemeColors theme;
	private final ChartFonts fonts;
	private final double w, h;
	private final Drawing d;
	private final DataFormatter formatter = new DataFormatter(Locale.US);
	private final String shortDate = Docx4jProperties.getProperty("docx4j.chart.render.shortDate", SHORT_DATE);

	private ChartLayout(ChartModel model, ThemeColors theme, ChartFonts fonts, double w, double h) {
		this.model = model;
		// the chart's own theme override and colour map, as the read model resolves its fills
		this.theme = theme != null && model.getPart() != null ? theme.forChart(model.getPart()) : theme;
		this.fonts = fonts;
		this.w = w;
		this.h = h;
		this.d = new Drawing(w, h);
	}

	static Drawing layout(ChartModel model, ThemeColors theme, ChartFonts fonts, double w, double h) {
		ChartLayout l = new ChartLayout(model, theme, fonts, w, h);
		l.layout();
		return l.d;
	}

	// ------------------------------------------------------------------ styles

	private Style base;

	/**
	 * Office's default where nothing says (finding 4): 10pt in the theme's minor font, black;
	 * then the chart space's c:txPr
	 */
	private Style base() {
		if (base == null) {
			Style office = new Style(fonts, "+mn-lt", 10, false, false, Color.BLACK, 0);
			base = style(model.getSource().getTxPr(), office);
		}
		return base;
	}

	/** A c:txPr (its first paragraph's defRPr) over a parent style */
	private Style style(CTTextBody tx, Style parent) {
		CTTextCharacterProperties p = null;
		if (tx != null && !tx.getP().isEmpty()) {
			CTTextParagraph para = tx.getP().get(0);
			if (para.getPPr() != null) p = para.getPPr().getDefRPr();
		}
		return style(p, parent);
	}

	private Style style(CTTextCharacterProperties p, Style parent) {
		if (p == null) return parent;
		double size = p.getSz() != null ? p.getSz() / 100.0 : parent.size;
		boolean bold = p.isB() != null ? p.isB() : parent.bold;
		boolean italic = p.isI() != null ? p.isI() : parent.italic;
		String face = p.getLatin() != null && p.getLatin().getTypeface() != null ? p.getLatin().getTypeface() : parent.typeface;
		Color colour = parent.colour;
		if (p.getSolidFill() != null) {
			Color c = awt(DrawingMLColors.resolve(p.getSolidFill(), theme));
			if (c != null) colour = c;
		}
		int kern = p.getKern() != null ? p.getKern() : parent.kern;
		return new Style(fonts, face, size, bold, italic, colour, kern);
	}

	/** An axis's labels' style: its c:txPr's, never kerned (see {@link Style}) */
	private Style axisStyle(ChartAxis ax) {
		Style s = style(ax == null ? null : txPr(ax.getSource()), base());
		return s.kern == 0 ? s : new Style(fonts, s.typeface, s.size, s.bold, s.italic, s.colour, 0);
	}

	/** The title's: a c:rich's first paragraph and run over its c:txPr; Office's title is 1.8 times the text, bold */
	private Style titleStyle(CTTitle t) {
		Style b = base();
		Style office = model.getSource().getTxPr() == null && t.getTxPr() == null && (t.getTx() == null || t.getTx().getRich() == null)
				? new Style(fonts, b.typeface, b.size * 1.8, true, b.italic, b.colour, b.kern)
				: b;
		Style s = style(t.getTxPr(), office);
		if (t.getTx() != null && t.getTx().getRich() != null) {
			CTTextBody rich = t.getTx().getRich();
			s = style(rich, s);
			if (!rich.getP().isEmpty()) {
				for (Object r : rich.getP().get(0).getEGTextRun()) {
					if (r instanceof org.docx4j.dml.CTRegularTextRun) {
						s = style(((org.docx4j.dml.CTRegularTextRun) r).getRPr(), s);
						break;
					}
				}
			}
		}
		return s;
	}

	static Color awt(ResolvedColor c) {
		if (c == null) return null;
		return new Color(c.getRed(), c.getGreen(), c.getBlue(), (int) Math.round(255.0 * c.getAlpha() / ResolvedColor.OPAQUE));
	}

	/** A shape's fill: solid only (phases 1 and 2); null for none */
	private Color fill(CTShapeProperties sp) {
		if (sp == null || sp.getNoFill() != null) return null;
		if (sp.getSolidFill() != null) return awt(DrawingMLColors.resolve(sp.getSolidFill(), theme));
		return null;
	}

	private Color lineColour(CTShapeProperties sp) {
		CTLineProperties ln = sp == null ? null : sp.getLn();
		if (ln == null || ln.getNoFill() != null || ln.getSolidFill() == null) return null;
		return awt(DrawingMLColors.resolve(ln.getSolidFill(), theme));
	}

	private static double lineWidth(CTShapeProperties sp) {
		CTLineProperties ln = sp == null ? null : sp.getLn();
		return ln == null || ln.getW() == null ? 0.75 : ln.getW() / 12700.0;
	}

	// ------------------------------------------------------------------ the frame

	private void layout() {
		CTChartSpace cs = model.getSource();
		double ix = INSET, iy = INSET, iw = w - 2 * INSET, ih = h - 2 * INSET;

		// the chart area
		Color areaFill = fill(cs.getSpPr());
		if (areaFill != null) d.add(new Drawing.Area(new Rectangle2D.Double(0, 0, w, h), areaFill, null, 0, "chartArea"));

		// the title
		double plotTop = iy + PLOT_EDGE;
		List<Drawing.Text> texts = new ArrayList<>();
		if (model.getTitle() != null && model.getTitle().getShownText() != null) {
			CTTitle t = model.getTitle().getSource();
			Style s = titleStyle(t);
			List<String> lines = wrap(model.getTitle().getShownText(), s, TITLE_MAX * w - 2 * TITLE_PAD_X);
			double tw = 0;
			for (String l : lines) tw = Math.max(tw, s.width(l));
			double bw = tw + 2 * TITLE_PAD_X, bh = lines.size() * s.line + 2 * TITLE_PAD_Y;
			double bx = ix + (iw - bw) / 2, by = iy + TITLE_TOP;
			d.boxes.put("title", new Rectangle2D.Double(bx, by, bw, bh));
			for (int i = 0; i < lines.size(); i++) {
				String l = lines.get(i);
				double lw = s.width(l);
				texts.add(new Drawing.Text(l, fonts.face(s.typeface), s.font, s.colour, bx + (bw - lw) / 2, by + TITLE_PAD_Y + s.ascent + 0.15 + i * s.line, lw, 0,
						"title"));
			}
			plotTop = by + bh + GAP;
		}

		// the series drawn, and the legend
		ChartGroup group = model.getGroups().get(0);
		boolean pie = group.getKind() == ChartGroup.Kind.PIE || group.getKind() == ChartGroup.Kind.DOUGHNUT;
		List<ChartSeries> series = group.getShownSeries();
		double plotLeft = ix + PLOT_EDGE, plotRight = ix + iw - PLOT_EDGE, plotBottom = iy + ih - PLOT_EDGE;
		List<Drawing.Area> keys = new ArrayList<>();
		String pos = model.getLegend() == null || model.getLegend().getPosition() == null ? "r" : model.getLegend().getPosition();
		boolean horizontal = "b".equals(pos) || "t".equals(pos);
		List<Entry> entries = entries(group, series, !horizontal);
		if (model.getLegend() != null && !entries.isEmpty()) {
			CTLegend lg = model.getLegend().getSource();
			Style s = style(lg.getTxPr(), base());
			// at the top, or the top right, the legend is below the title (or at the frame's edge)
			double top = d.boxes.containsKey("title") ? d.boxes.get("title").getMaxY() + LEGEND_BELOW_TITLE : iy + LEGEND_EDGE;
			double key = LEGEND_KEY * s.line;
			double rowH = s.line + 5.97 - 0.0102 * s.size;
			List<Double> widths = new ArrayList<>();
			double maxW = 0;
			for (Entry e : entries) {
				double ew = 2 * (e.lineKey ? LINE_KEY : key) + s.width(e.name);
				widths.add(ew);
				maxW = Math.max(maxW, ew);
			}
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
				if ("b".equals(pos)) plotBottom = ly - GAP;
				else plotTop = ly + lh + GAP;
			} else {
				lw = maxW + 3 + 2 * LEGEND_SIDE;
				lh = entries.size() * rowH;
				lx = "l".equals(pos) ? ix + LEGEND_EDGE : ix + iw - LEGEND_EDGE - lw;
				// centred on the plot area; at the top right, below the title, the plot area below it
				ly = "tr".equals(pos) ? top : (plotTop + plotBottom) / 2 - lh / 2;
				for (int i = 0; i < entries.size(); i++) at.add(new double[] { lx + LEGEND_SIDE, ly + LEGEND_TOP + i * rowH, maxW + 3 });
				if ("l".equals(pos)) plotLeft = lx + lw + GAP;
				else plotRight = lx - GAP;
				if ("tr".equals(pos)) {
					plotTop = ly + lh + LEGEND_TR_BELOW;
					plotBottom -= LEGEND_TR_BOTTOM;
				}
			}
			d.boxes.put("legend", new Rectangle2D.Double(lx, ly, lw, lh));
			for (int i = 0; i < entries.size(); i++) {
				Entry en = entries.get(i);
				double[] e = at.get(i);
				double kw = en.lineKey ? LINE_KEY : key;
				d.boxes.put("legend.entry." + i, new Rectangle2D.Double(e[0], e[1], e[2], s.line));
				d.boxes.put("legend.key." + i, new Rectangle2D.Double(e[0] + kw / 2, e[1] + key / 2, kw, key));
				// drawn 0.75pt right and 0.055 of the text's size down of the box Office reports for it
				double kx = e[0] + kw / 2 + 0.75, ky = e[1] + key / 2 + 0.055 * s.size;
				if (en.lineKey || en.marker != null) {
					double cx = kx + kw / 2, cy = ky + key / 2;
					if (en.lineKey) {
						double half = LINE_KEY_DRAWN * kw / 2;
						keys.add(new Drawing.Area(new Line2D.Double(cx - half, cy, cx + half, cy), null, en.line, en.lineWidth, "legend.key." + i, null,
								true));
					}
					if (en.marker != null) keys.addAll(marker(en.marker.atMost(key), cx, cy, "legend.key." + i));
				} else {
					keys.add(new Drawing.Area(new Rectangle2D.Double(kx, ky, key, key), en.fill, en.line, en.lineWidth, "legend.key." + i));
				}
				double tx = e[0] + 2 * kw + (2.94 / 10.986 - LEGEND_KEY / 2) * s.line;
				texts.add(new Drawing.Text(en.name, fonts.face(s.typeface), s.font, s.colour, tx, e[1] + s.ascent * 0.956, s.width(en.name), 0,
						"legend.text." + i));
			}
		}

		List<Drawing.Text> labels = new ArrayList<>();
		if (pie) {
			pie(group, series, plotLeft, plotTop, plotRight, plotBottom);
		} else {
			d.boxes.put("plotArea", new Rectangle2D.Double(plotLeft, plotTop, plotRight - plotLeft, plotBottom - plotTop));
			axes(group, series, plotLeft, plotTop, plotRight, plotBottom, labels);
		}
		for (Drawing.Area k : keys) d.add(k);
		for (Drawing.Text t : labels) d.add(t);
		for (Drawing.Text t : texts) d.add(t);

		// the chart area's border, last (as Word draws it)
		Color border = lineColour(cs.getSpPr());
		if (border != null) {
			double lw = lineWidth(cs.getSpPr());
			d.add(new Drawing.Area(new Rectangle2D.Double(0, 0, w, h), null, border, lw, "chartArea.border"));
		}
	}

	/**
	 * The legend's entries: a series' each, or where the colours vary by point (a pie's, a
	 * doughnut's), the first series' points'.  A clustered bar chart lists its series from the
	 * last (r1-bar-clustered), as its bars run from the bottom up; so does a stacked column or area
	 * chart's legend at the side, as its stack does (r14-legend-right-column-stacked; a stacked bar's
	 * and a line's in order)
	 */
	private List<Entry> entries(ChartGroup g, List<ChartSeries> series, boolean vertical) {
		List<Entry> out = new ArrayList<>();
		if (series.isEmpty()) return out;
		ChartGroup.Kind kind = g.getKind();
		if (kind == ChartGroup.Kind.PIE || kind == ChartGroup.Kind.DOUGHNUT || g.variesColors()) {
			ChartSeries s = series.get(0);
			int n = count(s.getValues());
			for (int i = 0; i < n; i++) {
				CTShapeProperties sp = pointSpPr(s, i);
				out.add(new Entry(categoryText(s.getCategories(), i, null), pointFill(s, i), lineColour(sp), lineWidth(sp), false, null));
			}
			return out;
		}
		boolean line = kind == ChartGroup.Kind.LINE || kind == ChartGroup.Kind.SCATTER;
		for (ChartSeries s : series) {
			String name = s.getShownName() == null ? "" : s.getShownName();
			CTShapeProperties sp = spPr(s);
			// a line series' key is its line, its marker over it; a scatter series with no line has a
			// square key, its marker in it (r1-scatter.pptx)
			if (line) out.add(new Entry(name, null, lineColour(sp), lineWidth(sp), lineColour(sp) != null, marker(s)));
			else out.add(new Entry(name, seriesFill(s), lineColour(sp), lineWidth(sp), false, null));
		}
		if (kind == ChartGroup.Kind.BAR && "bar".equals(g.getBarDirection()) && !stacked(g)) Collections.reverse(out);
		else if (vertical && stacked(g) && (kind == ChartGroup.Kind.AREA || (kind == ChartGroup.Kind.BAR && !"bar".equals(g.getBarDirection())))) {
			Collections.reverse(out);
		}
		return out;
	}

	private static boolean stacked(ChartGroup g) {
		return "stacked".equals(g.getGrouping()) || "percentStacked".equals(g.getGrouping());
	}

	// ------------------------------------------------------------------ the plot, with axes

	/** Whether the value axis is a 100% chart's (set by axes() for its frame) */
	private boolean percentAxis;

	/** The axes' labels, places and scales, and the series drawn between them */
	private void axes(ChartGroup group, List<ChartSeries> series, double plotLeft, double plotTop, double plotRight, double plotBottom,
			List<Drawing.Text> labels) {
		ChartGroup.Kind kind = group.getKind();
		boolean scatter = kind == ChartGroup.Kind.SCATTER;
		// a bar chart's categories run up the left, its values along the bottom
		boolean across = kind == ChartGroup.Kind.BAR && "bar".equals(group.getBarDirection());
		boolean percent = "percentStacked".equals(group.getGrouping());
		boolean stacked = stacked(group);

		// the value axis; the category axis, or a scatter chart's x axis (a value axis too)
		ChartAxis catAx = null, valAx = null;
		for (ChartAxis a : model.getAxes()) {
			if (!group.getAxisIds().contains(a.getId())) continue;
			if (scatter) {
				if (a.getKind() != ChartAxis.Kind.VALUE) continue;
				boolean x = "b".equals(a.getPosition()) || "t".equals(a.getPosition());
				if (x && catAx == null) catAx = a;
				else if (!x && valAx == null) valAx = a;
			} else if (a.getKind() == ChartAxis.Kind.VALUE && valAx == null) valAx = a;
			else if (a.getKind() == ChartAxis.Kind.CATEGORY || a.getKind() == ChartAxis.Kind.DATE) catAx = a;
		}
		boolean valLabels = labelled(valAx), catLabels = labelled(catAx);
		Style vs = axisStyle(valAx);
		Style cst = axisStyle(catAx);

		// the values: each point's extent, from (NaN: the axis's crossing) to; NaN to: a blank not drawn
		int n = series.size(), nCat = 0;
		for (ChartSeries sr : series) nCat = Math.max(nCat, count(values(sr, scatter)));
		double[][] from = new double[n][nCat], to = new double[n][nCat], xs = new double[n][nCat];
		String blanks = group.getShownDispBlanksAs();
		for (int i = 0; i < nCat; i++) {
			double total = 0;
			if (percent) {
				for (ChartSeries sr : series) {
					Double v = number(values(sr, scatter), i);
					if (v != null) total += Math.abs(v);
				}
			}
			double pos = 0, neg = 0, run = 0;
			for (int s = 0; s < n; s++) {
				ChartSeries sr = series.get(s);
				Double v = number(values(sr, scatter), i);
				if (v == null && "zero".equals(blanks)) v = 0.0;
				xs[s][i] = i + 1;
				if (scatter && sr.getXValues() != null && sr.getXValues().isNumeric() && sr.getXValues().getNumber(i) != null) {
					xs[s][i] = sr.getXValues().getNumber(i);
				}
				if (v == null) {
					from[s][i] = to[s][i] = Double.NaN;
					continue;
				}
				double x = percent ? (total == 0 ? 0 : v / total) : v;
				if (!stacked) {
					from[s][i] = Double.NaN;
					to[s][i] = x;
				} else if (kind == ChartGroup.Kind.BAR) {
					// a bar stacks on the bars of its sign
					if (x >= 0) {
						from[s][i] = pos;
						to[s][i] = pos += x;
					} else {
						from[s][i] = neg;
						to[s][i] = neg += x;
					}
				} else {
					from[s][i] = run;
					to[s][i] = run += x;
				}
			}
		}
		double dmin = Double.POSITIVE_INFINITY, dmax = Double.NEGATIVE_INFINITY;
		double xmin = Double.POSITIVE_INFINITY, xmax = Double.NEGATIVE_INFINITY;
		for (int s = 0; s < n; s++) {
			for (int i = 0; i < nCat; i++) {
				if (Double.isNaN(to[s][i])) continue;
				dmin = Math.min(dmin, to[s][i]);
				dmax = Math.max(dmax, to[s][i]);
				xmin = Math.min(xmin, xs[s][i]);
				xmax = Math.max(xmax, xs[s][i]);
			}
		}
		if (dmin == Double.POSITIVE_INFINITY) dmin = dmax = xmin = xmax = 0;
		Double setMin = valAx == null ? null : valAx.getMin(), setMax = valAx == null ? null : valAx.getMax();
		// a 100% chart's axis: its shares out to the unit, within 100% either way (AxisScale)
		percentAxis = percent;
		String valCode = numberFormat(valAx, series, percent);

		Frame f = scatter ? scatterFrame(valAx, catAx, valLabels, catLabels, vs, cst, dmin, dmax, setMin, setMax, xmin, xmax, valCode, series,
				plotLeft, plotTop, plotRight, plotBottom)
				: across ? barFrame(group, valAx, catAx, valLabels, catLabels, vs, cst, dmin, dmax, setMin, setMax, valCode, series, nCat, plotLeft,
						plotTop, plotRight, plotBottom)
						: columnFrame(valAx, catAx, valLabels, catLabels, vs, cst, dmin, dmax, setMin, setMax, valCode, series, nCat, plotLeft,
								plotTop, plotRight, plotBottom);
		d.valueAxis = new double[] { f.scale.min, f.scale.max, f.scale.unit };
		if (f.xScale != null) d.xAxis = new double[] { f.xScale.min, f.xScale.max, f.xScale.unit };
		else if (f.dates != null) d.xAxis = f.dates;
		Rectangle2D inside = new Rectangle2D.Double(f.left, f.top, f.right - f.left, f.bottom - f.top);
		d.boxes.put("inside", inside);

		// gridlines; the axes' lines, where each crosses the other; tick marks
		CTShapeProperties catSp = catAx == null ? null : spPr(catAx.getSource());
		CTShapeProperties valSp = valAx == null ? null : spPr(valAx.getSource());
		boolean catLine = catAx != null && !catAx.isDeleted() && lineColour(catSp) != null;
		boolean valLine = valAx != null && !valAx.isDeleted() && lineColour(valSp) != null;
		double cross = f.scale.log ? f.scale.min : Math.max(f.scale.min, Math.min(f.scale.max, 0));
		double crossV = f.v(cross);
		// where the value axis crosses: a category axis's start, a scatter chart's x of zero
		double crossC = scatter ? f.x(f.xScale.log ? f.xScale.min : Math.max(f.xScale.min, Math.min(f.xScale.max, 0)))
				: across ? f.bottom : f.left;
		CTChartLines grid = valAx == null ? null : gridlines(valAx.getSource());
		if (grid != null && lineColour(grid.getSpPr()) != null) {
			Color gc = lineColour(grid.getSpPr());
			for (double v : f.values) {
				double p = f.v(v);
				if (catLine && Math.abs(p - crossV) < 0.01) continue;
				Line2D l = across ? new Line2D.Double(p, f.top, p, f.bottom) : new Line2D.Double(f.left, p, f.right, p);
				d.add(new Drawing.Area(l, null, gc, lineWidth(grid.getSpPr()), "gridline"));
			}
		}
		CTChartLines xGrid = scatter ? gridlines(catAx.getSource()) : null;
		if (xGrid != null && lineColour(xGrid.getSpPr()) != null) {
			Color gc = lineColour(xGrid.getSpPr());
			for (double v : f.xValues) {
				double p = f.x(v);
				if (valLine && Math.abs(p - crossC) < 0.01) continue;
				d.add(new Drawing.Area(new Line2D.Double(p, f.top, p, f.bottom), null, gc, lineWidth(xGrid.getSpPr()), "gridline.x"));
			}
		}

		// the series
		if (kind == ChartGroup.Kind.BAR) bars(group, series, f, from, to, crossV, across, inside);
		else if (kind == ChartGroup.Kind.AREA) areas(series, f, from, to, crossV, inside);
		else lines(series, f, from, to, xs, scatter, blanks, inside);

		if (catLine) {
			Line2D l = across ? new Line2D.Double(crossV, f.top, crossV, f.bottom) : new Line2D.Double(f.left, crossV, f.right, crossV);
			d.add(new Drawing.Area(l, null, lineColour(catSp), lineWidth(catSp), "axis.category"));
			String tick = tickMark(catAx.getSource());
			if (tick != null && !scatter) {
				for (double p : f.ticks) {
					d.add(new Drawing.Area(tick(!across, p, crossV, tick, TICK * cst.size), null, lineColour(catSp), lineWidth(catSp), "tick.category"));
				}
			}
		}
		if (valLine) {
			Line2D l = across ? new Line2D.Double(f.left, crossC, f.right, crossC) : new Line2D.Double(crossC, f.top, crossC, f.bottom);
			d.add(new Drawing.Area(l, null, lineColour(valSp), lineWidth(valSp), "axis.value"));
			String tick = tickMark(valAx.getSource());
			if (tick != null) {
				for (double v : f.values) {
					d.add(new Drawing.Area(tick(across, f.v(v), crossC, tick, TICK * vs.size), null, lineColour(valSp), lineWidth(valSp), "tick.value"));
				}
			}
		}

		// the value labels: at the left right-aligned, centred on their gridlines; below, centred under them
		for (int k = 0; k < f.valueTexts.size(); k++) {
			String t = f.valueTexts.get(k);
			double p = f.v(f.values.get(k));
			labels.add(across ? below(t, vs, p, f.bottom, "value." + k) : left(t, vs, p, f.left, "value." + k));
		}
		for (int k = 0; k < f.xTexts.size(); k++) {
			labels.add(below(f.xTexts.get(k), cst, f.x(f.xValues.get(k)), f.bottom, "x." + k));
		}
		labels.addAll(f.categoryLabels);
	}

	/** A text right-aligned against the plot's inside, centred on y */
	private Drawing.Text left(String t, Style s, double y, double insideLeft, String role) {
		double tw = s.width(t);
		return new Drawing.Text(t, fonts.face(s.typeface), s.font, s.colour, insideLeft - VALUE_LABEL_GAP * s.line - tw, y + 0.289 * s.size + 0.2, tw, 0,
				role);
	}

	/** A text centred under x, below the axis at y */
	private Drawing.Text below(String t, Style s, double x, double y, String role) {
		double tw = s.width(t);
		return new Drawing.Text(t, fonts.face(s.typeface), s.font, s.colour, x - tw / 2, y + 0.61 * s.size + 0.45 + s.ascent, tw, 0, role);
	}

	/**
	 * A tick mark at p along an axis whose line is at q: out (away from the plot: down from an
	 * axis along the bottom, left from one at the left), in, or across it
	 */
	private static Line2D tick(boolean alongBottom, double p, double q, String mark, double length) {
		double out = "in".equals(mark) ? 0 : length, in = "out".equals(mark) ? 0 : length;
		if (alongBottom) return new Line2D.Double(p, q - in, p, q + out);
		return new Line2D.Double(q - out, p, q + in, p);
	}

	/** The plot's inside, its axes' scales and labels, and the places of its values and categories */
	private static final class Frame {
		double left, top, right, bottom;
		AxisScale scale, xScale;
		/** the value axis along the bottom (a bar chart's) */
		boolean across;
		List<Double> values = new ArrayList<>(), xValues = new ArrayList<>();
		List<String> valueTexts = new ArrayList<>(), xTexts = new ArrayList<>();
		List<Drawing.Text> categoryLabels = new ArrayList<>();
		/** each category's place along its axis (its centre, or its point for midCat), in points */
		double[] categories = new double[0];
		/** a category's width along its axis */
		double catW;
		/** where the category axis's tick marks are */
		List<Double> ticks = new ArrayList<>();
		/** a date axis's first and last dates and its major unit in base units, as Excel reports them */
		double[] dates;

		/** a value's place along the value axis */
		double v(double value) {
			double fr = scale.fraction(value);
			return across ? left + fr * (right - left) : bottom - fr * (bottom - top);
		}

		/** a scatter chart's x value's place */
		double x(double value) {
			return left + xScale.fraction(value) * (right - left);
		}
	}

	/**
	 * A bar chart's category labels' band (from the plot area's edge to the inside) is at most this
	 * share of the plot area less half the last value label; wider labels are wrapped to fit, a word
	 * wider than the room broken where it reaches it (r14-sweep-bar-text-12 and -x100-text-12:
	 * 0.548 to 0.553 on 24 sizes; "Categor" / "y 4")
	 */
	static final double BAR_CATEGORY_SHARE = 0.55;

	/** A value axis's labels: the values and their texts */
	private void valueLabels(Frame f, AxisScale scale, boolean labelled, String code, List<Double> values, List<String> texts) {
		values.addAll(scale.values());
		if (labelled) for (double v : values) texts.add(format(v, code));
	}

	private double widest(List<String> texts, Style s) {
		double m = 0;
		for (String t : texts) m = Math.max(m, s.width(t));
		return m;
	}

	/**
	 * Columns, lines and areas: the value axis at the left, the categories along the bottom (phase
	 * 1's rules, R1 to R13; midCat and date axes, r1-area)
	 */
	private Frame columnFrame(ChartAxis valAx, ChartAxis catAx, boolean valLabels, boolean catLabels, Style vs, Style cst, double dmin,
			double dmax, Double setMin, Double setMax, String valCode, List<ChartSeries> series, int nCat, double plotLeft, double plotTop,
			double plotRight, double plotBottom) {
		Frame f = new Frame();
		f.top = plotTop + (valLabels ? Math.max(0, vs.line / 2 - VALUE_LABEL_TOP) : 0);

		// the scale, chosen with one line of category labels.  The category labels take a band at
		// the plot's bottom only where their axis is there: below it they hang from the axis's line
		// wherever it crosses (R2)
		boolean bottomAxis = dmin >= 0 || (setMin != null && setMin >= 0);
		double band1 = catLabels && bottomAxis ? band(cst, 1) : 0;
		// (a maximum set can take the scale below zero, so this is decided again with the scale)
		f.scale = AxisScale.of(dmin, dmax, setMin, setMax, valAx == null ? null : valAx.getMajorUnit(), valAx == null ? null : valAx.getLogBase(),
				plotBottom - band1 - f.top, vs.line, percentAxis ? AxisScale.Bounds.PERCENT : AxisScale.Bounds.VALUE);
		if (!f.scale.log && f.scale.min < 0) bottomAxis = false;
		valueLabels(f, f.scale, valLabels, valCode, f.values, f.valueTexts);
		f.left = valLabels ? plotLeft - VALUE_LABEL_OVERHANG + widest(f.valueTexts, vs) + VALUE_LABEL_GAP * vs.line : plotLeft;
		f.right = plotRight;

		// the categories' places: by category, or a date axis's by its base unit; between the
		// ticks (a category's centre) or on them (midCat: the points on the inside's edges)
		boolean midCat = valAx != null && "midCat".equals(valAx.getCrossBetween());
		Categories c = categories(series, catAx, nCat);
		double slots = midCat ? Math.max(1, c.span) : c.span + 1;

		// the category labels, as Office tries them (R12): on one line where each fits its
		// category, wrapped where each word does, turned 45 degrees where the category is a line /
		// sin 45 wide, turned 90 where it is a line wide; else every second label, every third ...
		List<String> catTexts = catLabels ? c.labels : new ArrayList<>();
		// labels centred on the inside's edges keep within the plot area (r1-area's dates)
		if (midCat && !catTexts.isEmpty()) {
			f.left = Math.max(f.left, plotLeft + cst.width(catTexts.get(0)) / 2);
			f.right = Math.min(f.right, plotRight - cst.width(catTexts.get(catTexts.size() - 1)) / 2);
		}
		double catW = (f.right - f.left) / slots;
		double catMax = 0, wordMax = 0;
		for (String t : catTexts) {
			catMax = Math.max(catMax, cst.width(t));
			for (String word : words(t)) wordMax = Math.max(wordMax, cst.width(word));
		}
		int nLabels = catTexts.size();
		int mode = LEVEL, every = 1;
		double labelRoom = catW * c.step;
		boolean dates = c.dates;
		// a date axis's labels each have the inside over their number: level, wrapped, turned 45 or
		// 90 ({@link #DATE_ROOM}), else an automatic major unit the next up (r14-sweep-dates)
		if (catLabels && dates && !midCat && nLabels > 0) {
			int[] units = c.autoMajor ? majorUnits(c.unit) : new int[] { c.step };
			for (int u = 0; u < units.length; u++) {
				if (c.autoMajor) dateLabels(c, units[u]);
				catTexts = c.labels;
				nLabels = catTexts.size();
				catMax = 0;
				wordMax = 0;
				for (String t : catTexts) {
					catMax = Math.max(catMax, cst.width(t));
					for (String word : words(t)) wordMax = Math.max(wordMax, cst.width(word));
				}
				labelRoom = (f.right - f.left) / nLabels;
				boolean spaced = false;
				for (String t : catTexts) spaced |= t.indexOf(' ') >= 0;
				if (catMax <= labelRoom) mode = LEVEL;
				else if (wordMax <= labelRoom && (spaced || catMax <= DATE_WRAP * labelRoom)) mode = WRAPPED;
				else if (spaced ? labelRoom >= cst.line / SIN45 : labelRoom * SIN45 >= DATE_ROOM * cst.line) mode = TURNED_45;
				else if (labelRoom >= DATE_ROOM * cst.line || u == units.length - 1) mode = TURNED_90;
				else continue;
				break;
			}
		} else if (catLabels && nLabels > 0) {
			for (every = 1; every <= nLabels; every++) {
				double room = every * labelRoom;
				if (catMax <= room) mode = LEVEL;
				else if (wordMax <= room) mode = WRAPPED;
				else if (!dates && room >= cst.line / SIN45) mode = TURNED_45;
				else if (room >= cst.line) mode = TURNED_90;
				else continue;
				break;
			}
			if (every > nLabels) every = nLabels;
		}
		List<List<String>> catLines = new ArrayList<>();
		int lines = 1;
		double shownMax = 0;
		for (int i = 0; i < nLabels; i++) {
			String t = catTexts.get(i);
			List<String> l = i % every != 0 ? java.util.Collections.<String>emptyList()
					: mode == WRAPPED && cst.width(t) > labelRoom * every ? wrap(t, cst, labelRoom * every) : java.util.Collections.singletonList(t);
			catLines.add(l);
			lines = Math.max(lines, l.size());
			if (!l.isEmpty()) shownMax = Math.max(shownMax, cst.width(t));
		}
		double band = !catLabels || !bottomAxis ? 0 : bandOf(mode, cst, lines, shownMax);
		// a small plot caps the band (R3, R11, R12, R14: CATEGORY_CAP of the plot area's height and the gap): wrapped
		// labels go back on one line, overlapping; turned ones are cut short to the band of one
		// line (at least a character, and "..."); a line too tall overlaps the gap below
		double cap = (1 - CATEGORY_CAP) * (plotBottom - plotTop) + band(cst, 0) - (f.top - plotTop);
		if (band > cap) {
			if (mode == WRAPPED) {
				mode = LEVEL;
				for (int i = 0; i < catLines.size(); i++) {
					if (!catLines.get(i).isEmpty()) catLines.set(i, java.util.Collections.singletonList(catTexts.get(i)));
				}
				lines = 1;
				band = band(cst, 1);
			} else if (mode == TURNED_45 || mode == TURNED_90) {
				double room = mode == TURNED_45 ? (band(cst, 1) - (0.634 * cst.size - 4.5)) / SIN45 - cst.line
						: band(cst, 1) - (0.634 * cst.size - 4.5);
				shownMax = 0;
				for (int i = 0; i < catLines.size(); i++) {
					if (catLines.get(i).isEmpty()) continue;
					String t = catTexts.get(i);
					int k = t.length();
					while (k > 1 && cst.width(t.substring(0, k)) > room) k--;
					shownMax = Math.max(shownMax, cst.width(t.substring(0, k)));
					if (k < t.length()) t = t.substring(0, k) + "…";
					catLines.set(i, java.util.Collections.singletonList(t));
				}
				band = bandOf(mode, cst, 1, shownMax);
			}
			if (band > cap) band = cap;
		}
		// labels turned 45 degrees push the plot's inside right where the first (as drawn) would
		// reach past the chart's left: to TURNED_PUSH + its width x sin 45 - the plot's right / (2n)
		// (R12, R13: on all 31 probes turned so, to 0.07pt; the category width taken as the right
		// edge over n, as if the plot began at the chart's edge)
		// and pull its right edge in where the last category, placed so, is nearer the plot's right
		// than a line x sin 45 (R12's every second label, r14-sweep-dates: on 16 sizes, to 0.05pt)
		if (mode == TURNED_45 && nLabels > 0 && !catLines.get(0).isEmpty()) {
			double right = f.right;
			double pushed = TURNED_PUSH + SIN45 * cst.width(catLines.get(0).get(0)) - right / (2 * slots);
			if (pushed > f.left) f.left = pushed;
			double pulled = SIN45 * cst.line - right / (2 * slots);
			if (pulled > 0) f.right = right - pulled;
			catW = (f.right - f.left) / slots;
		}
		// (the inside no less than nothing: r14-sweep-column-h below 74pt)
		f.bottom = Math.max(f.top, plotBottom - band);
		f.catW = catW;
		if (dates) {
			LocalDate end = "months".equals(c.unit) ? c.start.plusMonths(c.span) : "years".equals(c.unit) ? c.start.plusYears(c.span) : c.start.plusDays(c.span);
			f.dates = new double[] { serial(c.start), serial(end), c.step };
		}
		f.categories = new double[nCat];
		for (int i = 0; i < nCat; i++) f.categories[i] = f.left + (c.slot[i] + (midCat ? 0 : 0.5)) * catW;
		if (midCat) for (int k = 0; k <= c.span; k += Math.max(1, c.step)) f.ticks.add(f.left + k * catW);
		else for (int k = 0; k <= c.span + 1; k += Math.max(1, c.step)) f.ticks.add(f.left + k * catW);

		// the category labels, centred under their categories (turned: a turned 90 label's line
		// centred on its category, its end at the labels' top; a turned 45 label's end there)
		double crossV = f.v(f.scale.log ? f.scale.min : Math.max(f.scale.min, Math.min(f.scale.max, 0)));
		for (int i = 0; i < catLines.size(); i++) {
			double cx = f.left + (c.labelSlot[i] + (midCat ? 0 : 0.5)) * catW;
			List<String> l = catLines.get(i);
			double top = crossV + 0.61 * cst.size + 0.45;
			for (int j = 0; j < l.size(); j++) {
				String t = l.get(j);
				double tw = cst.width(t);
				if (mode == TURNED_90) {
					double bx = cx + (cst.ascent - (cst.line - cst.ascent)) / 2;
					f.categoryLabels.add(new Drawing.Text(t, fonts.face(cst.typeface), cst.font, cst.colour, bx, top + tw, tw, -90, "category." + i));
				} else if (mode == TURNED_45) {
					// the turned line's top corner on the labels' top, its end's middle over the
					// category's centre (Word's glyph outlines, R12: to 0.1pt)
					double ex = cx + (cst.ascent - (cst.line - cst.ascent)) / 2 * SIN45, ey = top + cst.ascent * SIN45;
					f.categoryLabels.add(new Drawing.Text(t, fonts.face(cst.typeface), cst.font, cst.colour, ex - tw * SIN45, ey + tw * SIN45, tw, -45,
							"category." + i));
				} else {
					double base = top + cst.ascent + j * cst.line;
					f.categoryLabels.add(new Drawing.Text(t, fonts.face(cst.typeface), cst.font, cst.colour, cx - tw / 2, base, tw, 0, "category." + i));
				}
			}
		}
		return f;
	}

	/**
	 * A bar chart: the categories up the left (from the bottom), their labels right-aligned as a
	 * column chart's value labels are; the values along the bottom, their labels centred under
	 * their ticks, the last kept within the plot area (r1-bar-*, R11's bar chart)
	 */
	private Frame barFrame(ChartGroup group, ChartAxis valAx, ChartAxis catAx, boolean valLabels, boolean catLabels, Style vs, Style cst,
			double dmin, double dmax, Double setMin, Double setMax, String valCode, List<ChartSeries> series, int nCat, double plotLeft,
			double plotTop, double plotRight, double plotBottom) {
		Frame f = new Frame();
		f.across = true;
		f.top = plotTop;
		Categories c = categories(series, catAx, nCat);
		List<String> catTexts = catLabels ? c.labels : new ArrayList<>();
		double overhead = VALUE_LABEL_GAP * cst.line - VALUE_LABEL_OVERHANG;
		f.left = catLabels ? plotLeft + widest(catTexts, cst) + overhead : plotLeft;
		f.bottom = plotBottom - (valLabels ? band(vs, 1) : 0);
		AxisScale.Bounds kind = percentAxis ? AxisScale.Bounds.PERCENT : AxisScale.Bounds.VALUE;
		f.scale = acrossScale(dmin, dmax, setMin, setMax, valAx, valLabels, valCode, vs, plotLeft, f.left, plotRight, kind);
		// labels too wide for their share of the plot area are wrapped to it, and the scale chosen again
		List<List<String>> catLines = new ArrayList<>();
		for (String t : catTexts) catLines.add(java.util.Collections.singletonList(t));
		double half = valLabels ? vs.width(format(f.scale.max, valCode)) / 2 : 0;
		double room = BAR_CATEGORY_SHARE * (plotRight - half - plotLeft) - overhead;
		if (catLabels && widest(catTexts, cst) > room) {
			double w = 0;
			for (int i = 0; i < catTexts.size(); i++) {
				List<String> l = wrapBroken(catTexts.get(i), cst, room);
				catLines.set(i, l);
				w = Math.max(w, widest(l, cst));
			}
			f.left = plotLeft + w + overhead;
			f.scale = acrossScale(dmin, dmax, setMin, setMax, valAx, valLabels, valCode, vs, plotLeft, f.left, plotRight, kind);
		}
		valueLabels(f, f.scale, valLabels, valCode, f.values, f.valueTexts);
		f.right = plotRight - (valLabels ? vs.width(format(f.scale.max, valCode)) / 2 : 0);
		double slots = c.span + 1;
		f.catW = (f.bottom - f.top) / slots;
		f.categories = new double[nCat];
		for (int i = 0; i < nCat; i++) f.categories[i] = f.bottom - (c.slot[i] + 0.5) * f.catW;
		for (int k = 0; k <= c.span + 1; k++) f.ticks.add(f.bottom - k * f.catW);
		for (int i = 0; i < catLines.size(); i++) {
			List<String> l = catLines.get(i);
			double y = f.bottom - (c.labelSlot[i] + 0.5) * f.catW - (l.size() - 1) * cst.line / 2;
			for (int j = 0; j < l.size(); j++) f.categoryLabels.add(left(l.get(j), cst, y + j * cst.line, f.left, "category." + i));
		}
		return f;
	}

	/**
	 * A scatter chart: the y axis at the left as a column chart's value axis, the x axis along the
	 * bottom as a bar chart's value axis (r1-scatter)
	 */
	private Frame scatterFrame(ChartAxis valAx, ChartAxis xAx, boolean valLabels, boolean xLabels, Style vs, Style xs, double dmin,
			double dmax, Double setMin, Double setMax, double xmin, double xmax, String valCode, List<ChartSeries> series, double plotLeft,
			double plotTop, double plotRight, double plotBottom) {
		Frame f = new Frame();
		f.top = plotTop + (valLabels ? Math.max(0, vs.line / 2 - VALUE_LABEL_TOP) : 0);
		f.bottom = plotBottom - (xLabels ? band(xs, 1) : 0);
		f.scale = AxisScale.of(dmin, dmax, setMin, setMax, valAx == null ? null : valAx.getMajorUnit(), valAx == null ? null : valAx.getLogBase(),
				f.bottom - f.top, vs.line);
		valueLabels(f, f.scale, valLabels, valCode, f.values, f.valueTexts);
		f.left = valLabels ? plotLeft - VALUE_LABEL_OVERHANG + widest(f.valueTexts, vs) + VALUE_LABEL_GAP * vs.line : plotLeft;
		String xCode = xAx == null ? "General" : numberFormat(xAx, series.isEmpty() ? null : series.get(0).getXValues());
		f.xScale = acrossScale(xmin, xmax, xAx == null ? null : xAx.getMin(), xAx == null ? null : xAx.getMax(), xAx, xLabels, xCode, xs,
				plotLeft, f.left, plotRight, AxisScale.Bounds.X);
		valueLabels(f, f.xScale, xLabels, xCode, f.xValues, f.xTexts);
		// the first label, centred on the inside's left edge, is kept within the plot area (p6-scatter-dates)
		if (xLabels && !f.xTexts.isEmpty()) f.left = Math.max(f.left, plotLeft + xs.width(f.xTexts.get(0)) / 2);
		f.right = plotRight - (xLabels ? xs.width(format(f.xScale.max, xCode)) / 2 : 0);
		return f;
	}

	/**
	 * A horizontal value axis's scale: the unit its plot area's width gives ({@link #ACROSS}), then
	 * doubled, the bounds kept, while its interval on the inside is less than its widest label (R14's
	 * sweeps: "60000", 22.90pt, at 9pt, 22.45 apart doubled and 23.45 not; "1000", 24.32pt, at 12pt,
	 * 22.24 doubled and 25.24 not); the inside's right edge comes in by half the label at the maximum
	 */
	private AxisScale acrossScale(double dmin, double dmax, Double setMin, Double setMax, ChartAxis ax, boolean labels, String code, Style s,
			double plotLeft, double left, double plotRight, AxisScale.Bounds kind) {
		AxisScale scale = AxisScale.of(dmin, dmax, setMin, setMax, ax == null ? null : ax.getMajorUnit(), ax == null ? null : ax.getLogBase(),
				(lo, hi, u, k) -> (plotRight - plotLeft) / k >= ACROSS * s.size, kind);
		if (!labels || scale.log || (ax != null && ax.getMajorUnit() != null)) return scale;
		double inside = plotRight - s.width(format(scale.max, code)) / 2 - left;
		for (int guard = 0; guard < 8; guard++) {
			List<Double> v = scale.values();
			double widest = 0;
			for (double x : v) widest = Math.max(widest, s.width(format(x, code)));
			if (v.size() <= 2 || inside * scale.unit / (scale.max - scale.min) >= widest) return scale;
			scale = scale.doubled();
		}
		return scale;
	}

	/** A category axis's categories: each one's slot (a date axis's: its base units from the first), and its labels */
	private static final class Categories {
		double[] slot;
		/** the last slot */
		int span;
		/** the labels, each at its slot; a date axis's at each major unit */
		List<String> labels = new ArrayList<>();
		double[] labelSlot;
		/** slots between labels (a date axis's major unit in base units) */
		int step = 1;
		/** a date axis's */
		boolean dates;
		/** a date axis's base unit, first date, labels' format, and whether its major unit is automatic */
		String unit;
		LocalDate start;
		String code;
		boolean autoMajor;
	}

	/** A date axis's major units Excel tries, in base units, finest first (r14-sweep-dates: 1, 2 and 7 days) */
	private static int[] majorUnits(String unit) {
		if ("months".equals(unit)) return new int[] { 1, 2, 3, 6, 12 };
		if ("years".equals(unit)) return new int[] { 1, 2, 5, 10, 20, 50, 100 };
		return new int[] { 1, 2, 7, 14 };
	}

	/** A date axis's labels at each major unit (step base units) from the first date */
	private void dateLabels(Categories c, int step) {
		c.step = step;
		c.labels.clear();
		List<Double> at = new ArrayList<>();
		for (int k = 0; k <= c.span; k += c.step) {
			at.add((double) k);
			LocalDate dt = "months".equals(c.unit) ? c.start.plusMonths(k) : "years".equals(c.unit) ? c.start.plusYears(k) : c.start.plusDays(k);
			c.labels.add(format(serial(dt), c.code));
		}
		c.labelSlot = new double[at.size()];
		for (int i = 0; i < at.size(); i++) c.labelSlot[i] = at.get(i);
	}

	private Categories categories(List<ChartSeries> series, ChartAxis ax, int nCat) {
		Categories c = new Categories();
		c.slot = new double[nCat];
		ChartData cats = series.isEmpty() ? null : series.get(0).getCategories();
		String unit = ax != null && ax.getKind() == ChartAxis.Kind.DATE && cats != null && cats.isNumeric() ? baseUnit(ax, cats, nCat) : null;
		if (unit == null) {
			for (int i = 0; i < nCat; i++) c.slot[i] = i;
			c.span = Math.max(0, nCat - 1);
			c.labelSlot = new double[nCat];
			for (int i = 0; i < nCat; i++) {
				c.labelSlot[i] = i;
				c.labels.add(categoryText(cats, i, ax));
			}
			return c;
		}
		// a date axis: the dates placed by their base unit; labelled at each major unit from the first
		c.dates = true;
		double first = Double.POSITIVE_INFINITY, last = Double.NEGATIVE_INFINITY;
		for (int i = 0; i < nCat; i++) {
			Double v = cats.getNumber(i);
			if (v == null) continue;
			first = Math.min(first, units(v, unit));
			last = Math.max(last, units(v, unit));
		}
		if (first == Double.POSITIVE_INFINITY) first = last = 0;
		for (int i = 0; i < nCat; i++) {
			Double v = cats.getNumber(i);
			c.slot[i] = v == null ? i : units(v, unit) - first;
		}
		c.span = (int) Math.round(last - first);
		String major = ax.getMajorTimeUnit() != null ? ax.getMajorTimeUnit() : unit;
		double step = ax.getMajorUnit() != null ? ax.getMajorUnit() : 1;
		c.unit = unit;
		c.code = ax.getNumberFormat() != null && !Boolean.TRUE.equals(ax.getNumberFormatSourceLinked()) ? ax.getNumberFormat()
				: cats.getFormatCode(0);
		c.start = null;
		for (int i = 0; i < nCat; i++) {
			Double v = cats.getNumber(i);
			if (v != null && units(v, unit) == first) {
				c.start = date(v);
				break;
			}
		}
		if (c.start == null) c.start = date(0);
		c.autoMajor = ax.getMajorUnit() == null;
		dateLabels(c, (int) Math.max(1, Math.round(step * ratio(major, unit))));
		return c;
	}

	/** A date axis's base unit: as written, or the largest of days, months and years the dates all fall on */
	private static String baseUnit(ChartAxis ax, ChartData cats, int n) {
		if (ax.getBaseTimeUnit() != null) return ax.getBaseTimeUnit();
		boolean sameDay = true, sameMonth = true;
		LocalDate first = null;
		for (int i = 0; i < n; i++) {
			Double v = cats.getNumber(i);
			if (v == null) continue;
			LocalDate dt = date(v);
			if (first == null) first = dt;
			if (dt.getDayOfMonth() != first.getDayOfMonth()) sameDay = false;
			if (dt.getMonthValue() != first.getMonthValue()) sameMonth = false;
		}
		return !sameDay ? "days" : !sameMonth ? "months" : "years";
	}

	private static double ratio(String major, String base) {
		double[] days = { 1, 30.4375, 365.25 };
		return days[index(major)] / days[index(base)];
	}

	private static int index(String unit) {
		return "years".equals(unit) ? 2 : "months".equals(unit) ? 1 : 0;
	}

	private static final LocalDate EPOCH = LocalDate.of(1899, 12, 30);

	static LocalDate date(double serial) {
		return EPOCH.plusDays((long) Math.floor(serial));
	}

	static double serial(LocalDate d) {
		return java.time.temporal.ChronoUnit.DAYS.between(EPOCH, d);
	}

	/** A date in base units (days, months or years since year 0) */
	private static double units(double serial, String unit) {
		LocalDate dt = date(serial);
		if ("months".equals(unit)) return dt.getYear() * 12 + dt.getMonthValue() - 1;
		if ("years".equals(unit)) return dt.getYear();
		return Math.floor(serial);
	}

	// ------------------------------------------------------------------ the series

	/** Columns or bars: clustered side by side (gap width, overlap), stacked on each other */
	private void bars(ChartGroup group, List<ChartSeries> series, Frame f, double[][] from, double[][] to, double crossV, boolean across,
			Rectangle2D inside) {
		double gapWidth = number(group.getSettings().get("gapWidth"), 150) / 100.0;
		double overlap = number(group.getSettings().get("overlap"), stacked(group) ? 100 : 0) / 100.0;
		int n = series.size();
		double barW = f.catW / (n - (n - 1) * overlap + gapWidth);
		for (int s = 0; s < n; s++) {
			ChartSeries sr = series.get(s);
			Color c = seriesFill(sr);
			for (int i = 0; i < f.categories.length; i++) {
				if (Double.isNaN(to[s][i])) continue;
				double a = Double.isNaN(from[s][i]) ? crossV : f.v(from[s][i]), b = f.v(to[s][i]);
				double lo = Math.min(a, b), hi = Math.max(a, b);
				// the bars of a category from its start (a column's left, a bar's bottom)
				double offset = -f.catW / 2 + gapWidth * barW / 2 + s * barW * (1 - overlap);
				Rectangle2D r = across ? new Rectangle2D.Double(lo, f.categories[i] - offset - barW, hi - lo, barW)
						: new Rectangle2D.Double(f.categories[i] + offset, lo, barW, hi - lo);
				d.boxes.put("point." + s + "." + i, r);
				// clipped to the plot's inside, as Office clips a bar past the axis's bounds (R2)
				if (hi - lo > 0) d.add(new Drawing.Area(r, c, null, 0, "point." + s + "." + i, inside));
			}
		}
	}

	/** Areas: each series' from the axis (or the series below, stacked) up to its values; points on the categories */
	private void areas(List<ChartSeries> series, Frame f, double[][] from, double[][] to, double crossV, Rectangle2D inside) {
		for (int s = 0; s < series.size(); s++) {
			ChartSeries sr = series.get(s);
			Path2D.Double p = new Path2D.Double();
			int nCat = f.categories.length;
			boolean started = false;
			for (int i = 0; i < nCat; i++) {
				double v = Double.isNaN(to[s][i]) ? 0 : to[s][i];
				double x = f.categories[i], y = f.v(v);
				if (!started) p.moveTo(x, y);
				else p.lineTo(x, y);
				started = true;
				double base = Double.isNaN(from[s][i]) ? crossV : f.v(from[s][i]);
				d.boxes.put("point." + s + "." + i, new Rectangle2D.Double(x, Math.min(y, base), 0, Math.abs(base - y)));
			}
			for (int i = nCat - 1; i >= 0; i--) {
				double base = Double.isNaN(from[s][i]) ? crossV : f.v(from[s][i]);
				p.lineTo(f.categories[i], base);
			}
			p.closePath();
			CTShapeProperties sp = spPr(sr);
			if (nCat > 0) d.add(new Drawing.Area(p, seriesFill(sr), lineColour(sp), lineWidth(sp), "series." + s, inside));
		}
	}

	/** Lines (each run of points without a gap a line), and markers over them; a scatter chart's too */
	private void lines(List<ChartSeries> series, Frame f, double[][] from, double[][] to, double[][] xs, boolean scatter, String blanks,
			Rectangle2D inside) {
		List<Drawing.Area> markers = new ArrayList<>();
		for (int s = 0; s < series.size(); s++) {
			ChartSeries sr = series.get(s);
			CTShapeProperties sp = spPr(sr);
			Color lc = lineColour(sp);
			double lw = lineWidth(sp);
			// a line series with no line of its own: the automatic colour, Office's 2.25pt (its template's)
			if (sp == null && !scatter) {
				lc = automatic(sr.getIdx(), seriesCount());
				lw = 2.25;
			}
			Marker m = marker(sr);
			boolean smooth = Boolean.TRUE.equals(sr.getSmooth());
			int nCat = to[s].length;
			List<double[]> run = new ArrayList<>();
			for (int i = 0; i < nCat; i++) {
				if (Double.isNaN(to[s][i])) {
					// a gap ends the line; spanned, the line goes on to the next point
					if (!"span".equals(blanks)) {
						line(run, smooth, lc, lw, "series." + s);
						run = new ArrayList<>();
					}
					continue;
				}
				double x = scatter ? f.x(xs[s][i]) : f.categories[i], y = f.v(to[s][i]);
				d.boxes.put("point." + s + "." + i, new Rectangle2D.Double(x, y, 0, 0));
				run.add(new double[] { x, y });
				if (m != null) markers.addAll(marker(m, x, y, "point." + s + "." + i));
			}
			line(run, smooth, lc, lw, "series." + s);
		}
		for (Drawing.Area a : markers) d.add(a);
	}

	/**
	 * A run of a line's points, straight from one to the next, or smoothed: a Bezier curve through
	 * them, each segment's control points a sixth of its neighbours' difference along (Catmull-Rom),
	 * the ends' a third of the chord (R14's smoothed line and scatter: Word's PDF has these control
	 * points, fitted to 0.006pt)
	 */
	private void line(List<double[]> run, boolean smooth, Color colour, double width, String role) {
		if (colour == null || run.size() < 2) return;
		Path2D.Double p = new Path2D.Double();
		p.moveTo(run.get(0)[0], run.get(0)[1]);
		for (int i = 1; i < run.size(); i++) {
			double[] b = run.get(i - 1), c = run.get(i);
			if (!smooth) {
				p.lineTo(c[0], c[1]);
				continue;
			}
			// beyond either end, the end reflected (Office's end tangents are a third of the chord, r14-scatter-smooth)
			double[] a = i >= 2 ? run.get(i - 2) : new double[] { 2 * b[0] - c[0], 2 * b[1] - c[1] };
			double[] e = i + 1 < run.size() ? run.get(i + 1) : new double[] { 2 * c[0] - b[0], 2 * c[1] - b[1] };
			p.curveTo(b[0] + (c[0] - a[0]) / 6, b[1] + (c[1] - a[1]) / 6, c[0] - (e[0] - b[0]) / 6, c[1] - (e[1] - b[1]) / 6, c[0], c[1]);
		}
		d.add(new Drawing.Area(p, null, colour, width, role, null, true));
	}

	/**
	 * A marker centred on x, y, its size across (R14's markers): an x, a star or a plus its strokes
	 * over a square filled and outlined; a dash a bar a fifth of its size high; a dot that bar half
	 * as long, from the point rightwards.  Word's PDF has them on its 0.24pt grid, 0.12 to 0.28
	 * smaller (2.88 for 3, 4.80 for 5, 6.72 for 7), PowerPoint's at their size (5.04 for 5)
	 */
	private static List<Drawing.Area> marker(Marker m, double x, double y, String role) {
		List<Drawing.Area> out = new ArrayList<>();
		double s = m.size, r = s / 2, R = r;
		switch (m.symbol) {
		case "square":
			out.add(new Drawing.Area(new Rectangle2D.Double(x - r, y - r, s, s), m.fill, m.line, m.lineWidth, role));
			break;
		case "diamond": {
			Path2D.Double p = new Path2D.Double();
			p.moveTo(x, y - r);
			p.lineTo(x + r, y);
			p.lineTo(x, y + r);
			p.lineTo(x - r, y);
			p.closePath();
			out.add(new Drawing.Area(p, m.fill, m.line, m.lineWidth, role));
			break;
		}
		case "triangle": {
			Path2D.Double p = new Path2D.Double();
			p.moveTo(x, y - r);
			p.lineTo(x + r, y + r);
			p.lineTo(x - r, y + r);
			p.closePath();
			out.add(new Drawing.Area(p, m.fill, m.line, m.lineWidth, role));
			break;
		}
		case "x":
		case "star":
		case "plus": {
			out.add(new Drawing.Area(new Rectangle2D.Double(x - r, y - r, s, s), m.fill, m.line, m.lineWidth, role));
			List<Line2D> strokes = new ArrayList<>();
			if (!"plus".equals(m.symbol)) {
				strokes.add(new Line2D.Double(x - R, y - R, x + R, y + R));
				strokes.add(new Line2D.Double(x + R, y - R, x - R, y + R));
			}
			if (!"x".equals(m.symbol)) strokes.add(new Line2D.Double(x, y - R, x, y + R));
			if ("plus".equals(m.symbol)) strokes.add(new Line2D.Double(x - R, y, x + R, y));
			// (each stroke a path of its own, filled and stroked, as Word's PDF has them)
			for (Line2D l : strokes) out.add(new Drawing.Area(l, m.fill, m.line, m.lineWidth, role));
			break;
		}
		case "dash":
			out.add(new Drawing.Area(new Rectangle2D.Double(x - r, y - s / 10, s, s / 5), m.fill, m.line,
					m.lineWidth, role));
			break;
		case "dot":
			out.add(new Drawing.Area(new Rectangle2D.Double(x, y - s / 10, r, s / 5), m.fill, m.line,
					m.lineWidth, role));
			break;
		default:
			out.add(new Drawing.Area(new Ellipse2D.Double(x - r, y - r, s, s), m.fill, m.line, m.lineWidth, role));
		}
		return out;
	}

	/** A series' marker: none where its symbol is none, or where it states none (the probes all state theirs) */
	private Marker marker(ChartSeries s) {
		Object o = get(s.getSource(), "getMarker");
		if (!(o instanceof CTMarker)) return null;
		CTMarker m = (CTMarker) o;
		String symbol = m.getSymbol() == null || m.getSymbol().getVal() == null ? "circle" : m.getSymbol().getVal().value();
		if ("none".equals(symbol)) return null;
		double size = m.getSize() == null ? 5 : m.getSize().getVal();
		CTShapeProperties sp = m.getSpPr();
		return new Marker(symbol, size, fill(sp), lineColour(sp), lineWidth(sp));
	}

	// ------------------------------------------------------------------ the plot, a pie

	/**
	 * A pie or doughnut: the largest circle the plot's space holds, centred in it (r1-pie); the
	 * slices clockwise from twelve o'clock and the first slice's angle, a doughnut's series in rings
	 * from the inside out, the hole its share of the radius.  An exploded slice is moved out along
	 * its middle by its explosion's share of the radius; a series' explosion shrinks the pie by
	 * 1 + its share, a point's alone doesn't (R8).  The boxes Office reports: each slice's places
	 * (Drawing#slices), and the box of its radii's ends (finding 7)
	 */
	private void pie(ChartGroup group, List<ChartSeries> series, double plotLeft, double plotTop, double plotRight, double plotBottom) {
		double side = Math.max(0, Math.min(plotRight - plotLeft, plotBottom - plotTop));
		double px = plotLeft + (plotRight - plotLeft - side) / 2, py = plotTop + (plotBottom - plotTop - side) / 2;
		Rectangle2D plot = new Rectangle2D.Double(px, py, side, side);
		d.boxes.put("plotArea", plot);
		d.boxes.put("inside", plot);
		boolean doughnut = group.getKind() == ChartGroup.Kind.DOUGHNUT;
		double r = side / 2, cx0 = px + r, cy0 = py + r;
		long seriesExplosion = 0;
		for (ChartSeries sr : series) if (sr.getExplosion() != null) seriesExplosion = Math.max(seriesExplosion, sr.getExplosion());
		r /= 1 + seriesExplosion / 100.0;
		double hole = doughnut ? number(group.getSettings().get("holeSize"), 50) / 100.0 : 0;
		double first = number(group.getSettings().get("firstSliceAng"), 0);
		int rings = series.size();
		double ringW = r * (1 - hole) / Math.max(1, rings);
		for (int s = 0; s < rings; s++) {
			ChartSeries sr = series.get(s);
			double inner = doughnut ? r * hole + s * ringW : 0, outer = doughnut ? inner + ringW : r;
			ChartData v = sr.getValues();
			int nCat = count(v);
			double total = 0;
			for (int i = 0; i < nCat; i++) {
				Double x = v.getNumber(i);
				if (x != null) total += Math.abs(x);
			}
			double a = first;
			for (int i = 0; i < nCat; i++) {
				Double x = v.getNumber(i);
				if (x == null || total == 0) continue;
				double sweep = Math.abs(x) / total * 360, b = a + sweep;
				Long explosion = pointExplosion(sr, i);
				if (explosion == null) explosion = sr.getExplosion();
				double[] off = at(0, 0, explosion == null ? 0 : explosion / 100.0 * r, (a + b) / 2);
				double cx = cx0 + off[0], cy = cy0 + off[1];
				Shape shape;
				if (!doughnut) {
					shape = new Arc2D.Double(cx - outer, cy - outer, 2 * outer, 2 * outer, 90 - a, -sweep, Arc2D.PIE);
				} else {
					Path2D.Double p = new Path2D.Double();
					p.append(new Arc2D.Double(cx - outer, cy - outer, 2 * outer, 2 * outer, 90 - a, -sweep, Arc2D.OPEN), false);
					p.append(new Arc2D.Double(cx - inner, cy - inner, 2 * inner, 2 * inner, 90 - b, sweep, Arc2D.OPEN), true);
					p.closePath();
					shape = p;
				}
				double[][] at = new double[10][];
				at[1] = at(cx, cy, outer, b);
				at[2] = at(cx, cy, outer, (a + b) / 2);
				at[3] = at(cx, cy, outer, a);
				at[4] = at(cx, cy, (outer + inner) / 2, a);
				at[5] = new double[] { cx, cy };
				at[6] = at(cx, cy, (outer + inner) / 2, b);
				if (doughnut) {
					at[7] = at(cx, cy, inner, a);
					at[8] = at(cx, cy, inner, (a + b) / 2);
					at[9] = at(cx, cy, inner, b);
				}
				String key = "point." + s + "." + i;
				d.slices.put(key, at);
				Rectangle2D box = new Rectangle2D.Double(at[1][0], at[1][1], 0, 0);
				for (double[] q : new double[][] { at[3], doughnut ? at[7] : at[5], doughnut ? at[9] : at[5] }) box.add(q[0], q[1]);
				d.boxes.put(key, box);
				CTShapeProperties sp = pointSpPr(sr, i);
				d.add(new Drawing.Area(shape, pointFill(sr, i), lineColour(sp), lineWidth(sp), key));
				a = b;
			}
		}
	}

	/** A point's own explosion (c:dPt), or null */
	@SuppressWarnings("unchecked")
	private static Long pointExplosion(ChartSeries s, int i) {
		Object dPts = get(s.getSource(), "getDPt");
		if (dPts instanceof List) {
			for (CTDPt p : (List<CTDPt>) dPts) {
				if (p.getIdx() != null && p.getIdx().getVal() == i && p.getExplosion() != null) return p.getExplosion().getVal();
			}
		}
		return null;
	}

	/** The point at a radius and an angle clockwise from twelve o'clock */
	private static double[] at(double cx, double cy, double radius, double degrees) {
		double t = Math.toRadians(degrees);
		return new double[] { cx + radius * Math.sin(t), cy - radius * Math.cos(t) };
	}

	// ------------------------------------------------------------------ helpers

	/** The height of the category labels' band, lines high (finding: 1.855 x size - 4.5 for one line) */
	private static double band(Style s, int lines) {
		return lines * s.line + 0.634 * s.size - 4.5;
	}

	/** The band for labels laid out so: turned, their widest's footprint (R3, R12) */
	private static double bandOf(int mode, Style s, int lines, double widest) {
		if (mode == TURNED_45) return (widest + s.line) * SIN45 + 0.634 * s.size - 4.5;
		if (mode == TURNED_90) return widest + 0.634 * s.size - 4.5;
		return band(s, lines);
	}

	/** The chart's series, as Office counts them for its automatic colours */
	private int seriesCount() {
		int n = 0;
		for (ChartGroup g : model.getGroups()) n += g.getSeries().size();
		return n;
	}

	/** A series' fill: its own, else (automatic) the colour Office shows for its index */
	private Color seriesFill(ChartSeries s) {
		ChartFill f = s.getFill();
		if (f != null && f.getColor() != null) return awt(f.getColor());
		if (f == null || f.isAutomatic()) return automatic(s.getIdx(), seriesCount());
		f = s.getPointFills().get(0);
		if (f != null && f.getColor() != null) return awt(f.getColor());
		return Color.GRAY;
	}

	/** A point's fill: its own (c:dPt), else where the colours vary by point the automatic colour for its index, else its series' */
	private Color pointFill(ChartSeries s, int i) {
		ChartFill f = s.getPointFills().get(i);
		if (f != null && f.getColor() != null) return awt(f.getColor());
		ChartGroup g = model.getGroups().get(0);
		boolean byPoint = g.getKind() == ChartGroup.Kind.PIE || g.getKind() == ChartGroup.Kind.DOUGHNUT || g.variesColors();
		if (byPoint && (f == null || f.isAutomatic()) && (s.getFill() == null || s.getFill().isAutomatic())) return automatic(i, count(s.getValues()));
		return seriesFill(s);
	}

	/**
	 * The colour Office shows for an index with no colour of its own, of a count (the chart's
	 * series, or a series' points): its legacy cycle of the accents, whatever the colour style
	 * (CR-037 R14; ChartColorStyles)
	 */
	private Color automatic(long idx, int count) {
		Color colour = awt(DrawingMLColors.resolve(ChartColorStyles.automatic(idx, count), theme));
		return colour == null ? Color.GRAY : colour;
	}

	/** A point's shape properties: its own (c:dPt), else its series' */
	@SuppressWarnings("unchecked")
	private static CTShapeProperties pointSpPr(ChartSeries s, int i) {
		Object dPts = get(s.getSource(), "getDPt");
		if (dPts instanceof List) {
			for (CTDPt p : (List<CTDPt>) dPts) {
				if (p.getIdx() != null && p.getIdx().getVal() == i && p.getSpPr() != null) return p.getSpPr();
			}
		}
		return spPr(s);
	}

	private static CTShapeProperties spPr(ChartSeries s) {
		Object o = get(s.getSource(), "getSpPr");
		return o instanceof CTShapeProperties ? (CTShapeProperties) o : null;
	}

	private static Object get(Object o, String getter) {
		if (o == null) return null;
		try {
			return o.getClass().getMethod(getter).invoke(o);
		} catch (ReflectiveOperationException e) {
			return null;
		}
	}

	private static ChartData values(ChartSeries s, boolean scatter) {
		return scatter && s.getYValues() != null ? s.getYValues() : s.getValues();
	}

	private static Double number(ChartData d, int i) {
		return d == null ? null : d.getNumber(i);
	}

	private static int count(ChartData v) {
		return v == null ? 0 : (int) (v.getCount() == null ? v.size() : v.getCount());
	}

	private static boolean labelled(ChartAxis a) {
		return a != null && !a.isDeleted() && !"none".equals(a.getTickLabelPosition());
	}

	private static double number(String s, double dflt) {
		if (s == null) return dflt;
		try {
			return Double.parseDouble(s.replace("%", "").trim());
		} catch (NumberFormatException e) {
			return dflt;
		}
	}

	/**
	 * The value axis's number format: its own, or (linked to the source) the first series'
	 * values'; a 100% chart's linked, 0%
	 */
	private static String numberFormat(ChartAxis ax, List<ChartSeries> series, boolean percent) {
		if (ax == null) return "General";
		if (ax.getNumberFormat() != null && !Boolean.TRUE.equals(ax.getNumberFormatSourceLinked())) return ax.getNumberFormat();
		if (percent) return "0%";
		for (ChartSeries s : series) {
			ChartData v = s.getYValues() != null ? s.getYValues() : s.getValues();
			if (v != null && v.getFormatCode() != null) return v.getFormatCode();
		}
		return ax.getNumberFormat() == null ? "General" : ax.getNumberFormat();
	}

	private static String numberFormat(ChartAxis ax, ChartData data) {
		if (ax.getNumberFormat() != null && !Boolean.TRUE.equals(ax.getNumberFormatSourceLinked())) return ax.getNumberFormat();
		if (data != null && data.getFormatCode() != null) return data.getFormatCode();
		return ax.getNumberFormat() == null ? "General" : ax.getNumberFormat();
	}

	private String format(double v, String code) {
		if (code == null || code.isEmpty()) code = "General";
		if (SHORT_DATE.equalsIgnoreCase(code)) code = shortDate;
		try {
			return formatter.formatRawCellContents(v, "General".equalsIgnoreCase(code) ? 0 : -1, code);
		} catch (RuntimeException e) {
			return String.valueOf(v);
		}
	}

	private String categoryText(ChartData cats, int i, ChartAxis ax) {
		if (cats == null) return String.valueOf(i + 1);
		if (!cats.isNumeric()) {
			String t = cats.getText(i);
			return t == null ? "" : t;
		}
		Double v = cats.getNumber(i);
		if (v == null) return "";
		String code = ax != null && ax.getNumberFormat() != null && !Boolean.TRUE.equals(ax.getNumberFormatSourceLinked()) ? ax.getNumberFormat()
				: cats.getFormatCode(i);
		return format(v, code);
	}

	/** The pieces a line can break between: at spaces, and after a hyphen ("22-Jan", r14-sweep-dates) */
	static List<String> words(String text) {
		List<String> out = new ArrayList<>();
		for (String word : text.split(" ")) {
			int from = 0;
			for (int i = word.indexOf('-'); i >= 0 && i < word.length() - 1; i = word.indexOf('-', i + 1)) {
				if (i == 0) continue;
				out.add(word.substring(from, i + 1));
				from = i + 1;
			}
			out.add(word.substring(from));
		}
		return out;
	}

	/**
	 * Words wrapped to lines no wider than max (a word wider than max on a line of its own); a
	 * hyphen's word breaks after it; text of spaces alone is a line with nothing on it (p6-pie-numbers'
	 * title, " ")
	 */
	static List<String> wrap(String text, Style s, double max) {
		List<String> out = new ArrayList<>();
		String line = null;
		for (String word : text.split(" ")) {
			List<String> parts = words(word);
			for (int p = 0; p < parts.size(); p++) {
				String part = parts.get(p);
				String next = line == null ? part : line + (p == 0 ? " " : "") + part;
				if (line != null && s.width(next) > max) {
					out.add(line);
					line = part;
				} else {
					line = next;
				}
			}
		}
		if (line != null) out.add(line);
		if (out.isEmpty()) out.add("");
		return out;
	}

	/** Words wrapped to lines no wider than max, a word wider than max broken where it reaches it */
	static List<String> wrapBroken(String text, Style s, double max) {
		List<String> out = new ArrayList<>();
		String line = null;
		for (String word : text.split(" ")) {
			String rest = word;
			while (!rest.isEmpty()) {
				String next = line == null ? rest : line + " " + rest;
				if (s.width(next) <= max) {
					line = next;
					rest = "";
				} else if (line != null) {
					out.add(line);
					line = null;
				} else {
					int k = rest.length();
					while (k > 1 && s.width(rest.substring(0, k)) > max) k--;
					out.add(rest.substring(0, k));
					rest = rest.substring(k);
				}
			}
		}
		if (line != null) out.add(line);
		if (out.isEmpty()) out.add("");
		return out;
	}

	private static CTTextBody txPr(Object axis) {
		if (axis instanceof CTValAx) return ((CTValAx) axis).getTxPr();
		if (axis instanceof CTCatAx) return ((CTCatAx) axis).getTxPr();
		if (axis instanceof CTDateAx) return ((CTDateAx) axis).getTxPr();
		return null;
	}

	private static CTShapeProperties spPr(Object axis) {
		if (axis instanceof CTValAx) return ((CTValAx) axis).getSpPr();
		if (axis instanceof CTCatAx) return ((CTCatAx) axis).getSpPr();
		if (axis instanceof CTDateAx) return ((CTDateAx) axis).getSpPr();
		return null;
	}

	private static CTChartLines gridlines(Object axis) {
		if (axis instanceof CTValAx) return ((CTValAx) axis).getMajorGridlines();
		return null;
	}

	/** An axis's major tick marks: out, in or cross; null for none */
	private static String tickMark(Object axis) {
		Object o = get(axis, "getMajorTickMark");
		if (!(o instanceof CTTickMark) || ((CTTickMark) o).getVal() == null) return null;
		String v = ((CTTickMark) o).getVal().value();
		return "none".equals(v) ? null : v;
	}
}
