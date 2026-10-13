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
import org.docx4j.dml.chart.CTDLbl;
import org.docx4j.dml.chart.CTDLbls;
import org.docx4j.dml.chart.CTDPt;
import org.docx4j.dml.chart.CTDateAx;
import org.docx4j.dml.chart.CTLayout;
import org.docx4j.dml.chart.CTLayoutMode;
import org.docx4j.dml.chart.CTLegend;
import org.docx4j.dml.chart.CTManualLayout;
import org.docx4j.dml.chart.CTMarker;
import org.docx4j.dml.chart.CTTickMark;
import org.docx4j.dml.chart.CTTitle;
import org.docx4j.dml.chart.CTTx;
import org.docx4j.dml.chart.CTValAx;
import org.docx4j.dml.chart.STLayoutMode;
import org.docx4j.dml.chart.STLayoutTarget;
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
	/** A line series' legend key is at least this many times its line's width (r15-line-dashes: 15.75pt for 3.5pt; one width: provisional) */
	static final double LINE_KEY_PER_WIDTH = 4.5;
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
	/** A minor tick mark's (r15-ticks-minor: 2.13pt at 9pt; one probe) */
	static final double MINOR_TICK = 0.2367;
	/** Category labels above the plot stand this much higher than a mirror of those below (r15-catax-crosses-max, -high: 0.67pt at 9pt) */
	static final double ABOVE_SHIFT = 0.67;
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
	/**
	 * A data label's box is its text and its insets (its c:txPr's bodyPr, Excel's 3pt either side
	 * and 1.5pt above and below); it stands this far from what it labels: a bar's end or base, a
	 * marker's edge (R6: every label of the column, the stacked column and the line with markers,
	 * to 0.001pt)
	 */
	static final double LABEL_GAP = 3;
	/** A pie's label inside its end: where its box leaves the slice's middle ray, this far inside the arc (R6, R10's 258: 3.352pt, all eight) */
	static final double LABEL_PIE_IN = 3.352;
	/**
	 * A pie whose labels may go outside it (outEnd, bestFit), or a doughnut with labels, is smaller
	 * each side by this and this share of its side, whatever its labels (r6-pie-outEnd, -bestFit,
	 * r8-pie-labels-outside, r15-labels-doughnut: 181.035 to 155.656pt; p6-pie-data-labels and
	 * p6-doughnut-data-labels: 145.05 to 124.16pt; two sizes: provisional)
	 */
	static final double LABEL_PIE_ROOM = 1.395, LABEL_PIE_SHARE = 0.06239;
	/**
	 * A plot area placed by hand has its axes' labels this much further in from its left and bottom
	 * edges than Office's own plot area, and the top value label's half line below its top
	 * (r9-plot-outer, r9-plot-inner: 6.00pt, the inside to 0.001pt)
	 */
	static final double MANUAL_INSET = 6;
	/** How the category labels are set (R12) */
	static final int LEVEL = 0, WRAPPED = 1, TURNED_45 = 2, TURNED_90 = 3;
	/** An axis's labels' side: at the low end of the crossing axis, the high end, or beside its line within the plot (R15) */
	static final int LOW = 0, HIGH = 1, MID = 2;
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
		/** the space after each character, in points (DrawingML's spc), and capitals (cap="all") */
		final double spc;
		final boolean caps;

		Style(ChartFonts fonts, String typeface, double size, boolean bold, boolean italic, Color colour, int kern) {
			this(fonts, typeface, size, bold, italic, colour, kern, 0, false);
		}

		Style(ChartFonts fonts, String typeface, double size, boolean bold, boolean italic, Color colour, int kern, double spc, boolean caps) {
			this.spc = spc;
			this.caps = caps;
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

		/** A text as shown: in capitals where the style says */
		String shown(String s) {
			return caps ? s.toUpperCase(Locale.ROOT) : s;
		}

		/**
		 * A text's width: its kerned advances and the space after each character, the last's
		 * included (r10's styles 202, 209 and 258: the titles' boxes to 0.01pt; Office's spacing is
		 * DrawingML's spc after every character)
		 */
		double width(String s) {
			if (s.isEmpty()) return 0;
			String u = shown(s);
			return font.getStringBounds(u, FRC).getWidth() + u.length() * spc;
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
		/** its shape properties, for a gradient */
		CTShapeProperties sp;
		/** what c:legendEntry's idx names it by: its series' c:idx, or its point's index */
		long idx;

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

	/** A piece of rich text: its text in a style */
	static final class Seg {
		String text;
		final Style style;

		Seg(String text, Style style) {
			this.text = text;
			this.style = style;
		}
	}

	/** A line of rich text: its pieces, its width, its height and ascent (its tallest piece's) */
	static final class Line {
		final List<Seg> segs = new ArrayList<>();
		double width, line, ascent;
	}

	/**
	 * A text body's paragraphs, each its runs in their styles: the paragraph's defRPr over the
	 * parent, a run's rPr over that; a line break starts a new paragraph
	 */
	private List<List<Seg>> paragraphs(CTTextBody rich, Style parent) {
		List<List<Seg>> out = new ArrayList<>();
		for (CTTextParagraph p : rich.getP()) {
			Style ps = style(p.getPPr() == null ? null : p.getPPr().getDefRPr(), parent);
			List<Seg> para = new ArrayList<>();
			for (Object o : p.getEGTextRun()) {
				Object r = org.docx4j.XmlUtils.unwrap(o);
				if (r instanceof org.docx4j.dml.CTRegularTextRun) {
					org.docx4j.dml.CTRegularTextRun run = (org.docx4j.dml.CTRegularTextRun) r;
					if (run.getT() != null) para.add(new Seg(run.getT(), style(run.getRPr(), ps)));
				} else if (r instanceof org.docx4j.dml.CTTextField) {
					org.docx4j.dml.CTTextField fld = (org.docx4j.dml.CTTextField) r;
					if (fld.getT() != null) para.add(new Seg(fld.getT(), style(fld.getRPr(), ps)));
				} else if (r instanceof org.docx4j.dml.CTTextLineBreak) {
					out.add(para);
					para = new ArrayList<>();
				}
			}
			if (para.isEmpty()) para.add(new Seg("", ps));
			out.add(para);
		}
		return out;
	}

	/** The title's text: its rich text's runs, or the text it shows (a cell's, Office's own) in its style */
	private List<List<Seg>> titleText(CTTitle t) {
		Style base = titleBase(t);
		if (t.getTx() != null && t.getTx().getRich() != null) return paragraphs(t.getTx().getRich(), base);
		List<List<Seg>> out = new ArrayList<>();
		for (String p : model.getTitle().getShownText().split("\n", -1)) {
			List<Seg> para = new ArrayList<>();
			para.add(new Seg(p, base));
			out.add(para);
		}
		return out;
	}

	/**
	 * Paragraphs of runs wrapped to lines no wider than max, at spaces (a word wider than max on a
	 * line of its own; a run's word that goes on into the next run kept whole); each line's pieces
	 * of one style joined; text of spaces alone a line with nothing on it
	 */
	static List<Line> wrap(List<List<Seg>> paragraphs, double max) {
		List<Line> out = new ArrayList<>();
		for (List<Seg> para : paragraphs) {
			// the paragraph's words, each its pieces
			List<List<Seg>> words = new ArrayList<>();
			List<Seg> cur = new ArrayList<>();
			Style last = para.isEmpty() ? null : para.get(0).style;
			for (Seg sg : para) {
				last = sg.style;
				String t = sg.text;
				int start = 0;
				for (int k = 0; k <= t.length(); k++) {
					if (k < t.length() && t.charAt(k) != ' ') continue;
					if (k > start) cur.add(new Seg(t.substring(start, k), sg.style));
					if (k < t.length()) {
						words.add(cur);
						cur = new ArrayList<>();
					}
					start = k + 1;
				}
			}
			words.add(cur);
			Line line = null;
			for (List<Seg> word : words) {
				// spaces at either end, or more than one, are dropped (p6-pie-numbers' title of a space: no width)
				if (word.isEmpty()) continue;
				double ww = 0;
				for (Seg sg : word) ww += sg.style.width(sg.text);
				Style ws = word.isEmpty() ? last : word.get(0).style;
				if (line != null && line.width + ws.width(" ") + ww > max && !line.segs.isEmpty()) {
					out.add(line);
					line = null;
				}
				if (line == null) {
					line = new Line();
					line.line = ws.line;
					line.ascent = ws.ascent;
				} else {
					// the space before the word, on the line's last piece
					Seg prev = line.segs.isEmpty() ? null : line.segs.get(line.segs.size() - 1);
					if (prev != null) prev.text += " ";
					else line.segs.add(new Seg(" ", ws));
					line.width += ws.width(" ");
				}
				for (Seg sg : word) {
					Seg prev = line.segs.isEmpty() ? null : line.segs.get(line.segs.size() - 1);
					if (prev != null && prev.style == sg.style) prev.text += sg.text;
					else line.segs.add(new Seg(sg.text, sg.style));
					line.line = Math.max(line.line, sg.style.line);
					line.ascent = Math.max(line.ascent, sg.style.ascent);
				}
				line.width = 0;
				for (Seg sg : line.segs) line.width += sg.style.width(sg.text);
			}
			if (line == null) {
				line = new Line();
				line.line = last == null ? 0 : last.line;
				line.ascent = last == null ? 0 : last.ascent;
			}
			out.add(line);
		}
		return out;
	}

	/** A text body's first paragraph's defRPr, or null */
	private static CTTextCharacterProperties defRPr(CTTextBody tx) {
		if (tx == null || tx.getP().isEmpty() || tx.getP().get(0).getPPr() == null) return null;
		return tx.getP().get(0).getPPr().getDefRPr();
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
		double spc = p.getSpc() != null ? p.getSpc() / 100.0 : parent.spc;
		boolean caps = p.getCap() != null ? p.getCap() == org.docx4j.dml.STTextCapsType.ALL : parent.caps;
		return new Style(fonts, face, size, bold, italic, colour, kern, spc, caps);
	}

	/** An axis's labels' style: its c:txPr's, never kerned (see {@link Style}) */
	private Style axisStyle(ChartAxis ax) {
		Style s = style(ax == null ? null : txPr(ax.getSource()), base());
		return s.kern == 0 ? s : new Style(fonts, s.typeface, s.size, s.bold, s.italic, s.colour, 0, s.spc, s.caps);
	}

	/**
	 * The title's: a c:rich's first paragraph and run over its c:txPr; where none of those says a
	 * size, Office's is 1.8 times the chart's text (finding 4; r10-column-style-204, whose title's
	 * txPr says none: 18pt), and bold where none says
	 */
	private Style titleBase(CTTitle t) {
		Style b = base();
		boolean sized = false, weighted = false;
		List<CTTextCharacterProperties> chain = new ArrayList<>();
		chain.add(defRPr(t.getTxPr()));
		if (t.getTx() != null && t.getTx().getRich() != null) {
			CTTextBody rich = t.getTx().getRich();
			chain.add(defRPr(rich));
			if (!rich.getP().isEmpty()) {
				for (Object r : rich.getP().get(0).getEGTextRun()) {
					if (r instanceof org.docx4j.dml.CTRegularTextRun) {
						chain.add(((org.docx4j.dml.CTRegularTextRun) r).getRPr());
						break;
					}
				}
			}
		}
		for (CTTextCharacterProperties p : chain) {
			if (p == null) continue;
			sized |= p.getSz() != null;
			weighted |= p.isB() != null;
		}
		Style office = new Style(fonts, b.typeface, sized ? b.size : b.size * 1.8, weighted ? b.bold : true, b.italic, b.colour, b.kern, b.spc, b.caps);
		return style(t.getTxPr(), office);
	}

	/** A line of text in a style, from its baseline's start; width is the style's (spacing after its last character included) */
	private Drawing.Text text(Style s, String t, double x, double baseline, double width, double angle, String role) {
		// (a run ending in spaces is drawn to its last glyph: its spaces' width off, r15-title-runs)
		String drawn = t.replaceAll(" +$", "");
		if (drawn.length() < t.length()) width = s.width(drawn);
		return new Drawing.Text(s.shown(t), fonts.face(s.typeface), s.font, s.colour, x, baseline, drawn.isEmpty() ? width : width - s.spc, angle, role,
				s.spc);
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

	/** A shape's gradient fill (a:gradFill), its stops resolved through the theme; null for none */
	private Drawing.Gradient gradient(CTShapeProperties sp) {
		return sp == null ? null : gradient(sp.getGradFill());
	}

	private Drawing.Gradient gradient(org.docx4j.dml.CTGradientFillProperties g) {
		if (g == null || g.getGsLst() == null) return null;
		List<org.docx4j.dml.CTGradientStop> gs = new ArrayList<>(g.getGsLst().getGs());
		if (gs.isEmpty()) return null;
		gs.sort((a, b) -> Integer.compare(a.getPos(), b.getPos()));
		float[] stops = new float[gs.size()];
		Color[] colours = new Color[gs.size()];
		for (int i = 0; i < gs.size(); i++) {
			org.docx4j.dml.CTGradientStop st = gs.get(i);
			Object c = st.getSchemeClr() != null ? st.getSchemeClr() : st.getSrgbClr() != null ? st.getSrgbClr()
					: st.getPrstClr() != null ? st.getPrstClr() : st.getSysClr() != null ? st.getSysClr()
							: st.getHslClr() != null ? st.getHslClr() : st.getScrgbClr();
			colours[i] = awt(DrawingMLColors.resolve(c, theme));
			if (colours[i] == null) colours[i] = Color.GRAY;
			// strictly increasing, as AWT's gradients need
			float f = (float) Math.max(0, Math.min(1, st.getPos() / 100000.0));
			stops[i] = i > 0 && f <= stops[i - 1] ? Math.min(1f, stops[i - 1] + 1e-5f) : f;
		}
		if (stops.length > 1 && stops[stops.length - 1] <= stops[stops.length - 2]) {
			// stops crowded at the end: as they came, spread a little below it
			for (int i = stops.length - 2; i >= 0 && stops[i] >= stops[i + 1]; i--) stops[i] = stops[i + 1] - 1e-5f;
		}
		if (g.getPath() != null) {
			org.docx4j.dml.CTRelativeRect r = g.getPath().getFillToRect();
			double[] focus = r == null ? new double[] { 0.5, 0.5, 0.5, 0.5 }
					: new double[] { r.getL() / 100000.0, r.getT() / 100000.0, r.getR() / 100000.0, r.getB() / 100000.0 };
			return new Drawing.Gradient(stops, colours, false, 0, false, focus);
		}
		double angle = g.getLin() != null && g.getLin().getAng() != null ? g.getLin().getAng() / 60000.0 : 0;
		boolean scaled = g.getLin() != null && Boolean.TRUE.equals(g.getLin().isScaled());
		return new Drawing.Gradient(stops, colours, true, angle, scaled, null);
	}

	/** A shape's outer shadow and glow (a:effectLst), or null for none */
	private List<Drawing.Shadow> shadows(CTShapeProperties sp) {
		if (sp == null || sp.getEffectLst() == null) return null;
		List<Drawing.Shadow> out = new ArrayList<>();
		org.docx4j.dml.CTGlowEffect gl = sp.getEffectLst().getGlow();
		if (gl != null) {
			Object c = gl.getSchemeClr() != null ? gl.getSchemeClr() : gl.getSrgbClr() != null ? gl.getSrgbClr() : gl.getPrstClr() != null ? gl.getPrstClr()
					: gl.getSysClr() != null ? gl.getSysClr() : gl.getHslClr() != null ? gl.getHslClr() : gl.getScrgbClr();
			Drawing.Shadow g = new Drawing.Shadow();
			g.colour = awt(DrawingMLColors.resolve(c, theme));
			g.glow = gl.getRad() / 12700.0;
			if (g.colour != null && g.glow > 0) out.add(g);
		}
		org.docx4j.dml.CTOuterShadowEffect os = sp.getEffectLst().getOuterShdw();
		if (os != null) {
			Object c = os.getSchemeClr() != null ? os.getSchemeClr() : os.getSrgbClr() != null ? os.getSrgbClr() : os.getPrstClr() != null ? os.getPrstClr()
					: os.getSysClr() != null ? os.getSysClr() : os.getHslClr() != null ? os.getHslClr() : os.getScrgbClr();
			Drawing.Shadow sh = new Drawing.Shadow();
			sh.colour = awt(DrawingMLColors.resolve(c, theme));
			sh.blur = os.getBlurRad() / 12700.0;
			sh.dist = os.getDist() / 12700.0;
			sh.dir = os.getDir() / 60000.0;
			sh.sx = os.getSx() / 100000.0;
			sh.sy = os.getSy() / 100000.0;
			sh.kx = os.getKx() / 60000.0;
			sh.ky = os.getKy() / 60000.0;
			String al = os.getAlgn() == null ? "b" : os.getAlgn().value();
			sh.ax = al.endsWith("l") ? 0 : al.endsWith("r") ? 1 : 0.5;
			sh.ay = al.startsWith("t") ? 0 : al.startsWith("b") ? 1 : 0.5;
			if (sh.colour != null) out.add(sh);
		}
		return out.isEmpty() ? null : out;
	}

	/** A shape's pattern fill (a:pattFill), its colours resolved through the theme; null for none */
	private Drawing.Pattern pattern(CTShapeProperties sp) {
		if (sp == null || sp.getPattFill() == null) return null;
		org.docx4j.dml.CTPatternFillProperties pf = sp.getPattFill();
		Color fg = pf.getFgClr() == null ? Color.BLACK : awt(DrawingMLColors.resolve(pf.getFgClr(), theme));
		Color bg = pf.getBgClr() == null ? Color.WHITE : awt(DrawingMLColors.resolve(pf.getBgClr(), theme));
		String prst = pf.getPrst() == null ? "pct5" : pf.getPrst().value();
		return new Drawing.Pattern(Drawing.Pattern.preset(prst), fg == null ? Color.BLACK : fg, bg);
	}

	/** An area filled by a shape's properties: its solid fill, gradient or pattern, and its line */
	private Drawing.Area area(Shape shape, CTShapeProperties sp, Color fill, String role, Shape clip) {
		Drawing.Gradient gr = gradient(sp);
		Drawing.Pattern pt = gr == null ? pattern(sp) : null;
		Drawing.Area a = new Drawing.Area(shape, gr != null ? gr.colours[0] : pt != null ? pt.fg : fill, lineColour(sp), lineWidth(sp), role, clip);
		a.gradient = gr;
		a.pattern = pt;
		a.shadows = shadows(sp);
		a.dash = dashes(sp, a.lineWidth);
		return a;
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

	/** The plot area's fill and line (its c:spPr), over its inside (provisional: R15's r15-plot-area-fill) */
	private void plotAreaFill(Rectangle2D inside) {
		CTChartSpace cs = model.getSource();
		CTShapeProperties sp = cs.getChart() == null || cs.getChart().getPlotArea() == null ? null : cs.getChart().getPlotArea().getSpPr();
		if (sp == null) return;
		Drawing.Area a = area(inside, sp, fill(sp), "plotArea", null);
		if (a.fill != null || a.line != null) d.add(a);
	}

	/**
	 * A line's dashes (a:prstDash), in points: DrawingML's presets in multiples of the line's
	 * width (Office's: dash 4 3, sysDash 3 1, ...); null for a solid line
	 */
	private static float[] dashes(CTShapeProperties sp, double width) {
		if (sp == null || sp.getLn() == null || sp.getLn().getPrstDash() == null || sp.getLn().getPrstDash().getVal() == null) return null;
		float[] u;
		switch (sp.getLn().getPrstDash().getVal().value()) {
		case "dot": u = new float[] { 1, 3 }; break;
		case "dash": u = new float[] { 4, 3 }; break;
		case "lgDash": u = new float[] { 8, 3 }; break;
		case "dashDot": u = new float[] { 4, 3, 1, 3 }; break;
		case "lgDashDot": u = new float[] { 8, 3, 1, 3 }; break;
		case "lgDashDotDot": u = new float[] { 8, 3, 1, 3, 1, 3 }; break;
		case "sysDash": u = new float[] { 3, 1 }; break;
		case "sysDot": u = new float[] { 1, 1 }; break;
		case "sysDashDot": u = new float[] { 3, 1, 1, 1 }; break;
		case "sysDashDotDot": u = new float[] { 3, 1, 1, 1, 1, 1 }; break;
		default: return null;
		}
		for (int i = 0; i < u.length; i++) u[i] *= (float) Math.max(width, 0.1);
		return u;
	}

	/** A c:layout's manual layout, or null */
	private static CTManualLayout manualLayout(CTLayout l) {
		return l == null ? null : l.getManualLayout();
	}

	private static boolean edge(CTLayoutMode m) {
		return m != null && m.getVal() == STLayoutMode.EDGE;
	}

	/**
	 * A box placed by hand ({@code c:manualLayout}), from where Office would put it: x and y in edge
	 * mode a share of the chart from its top left (r9-title-moved, r9-legend-moved: to 0.001pt), in
	 * factor mode (the default) moved by that share from where Office puts it; w and h a share of
	 * the chart (factor), or in edge mode where its right and bottom edges are
	 */
	private Rectangle2D manual(CTManualLayout m, Rectangle2D dflt) {
		double x = dflt.getX(), y = dflt.getY(), bw = dflt.getWidth(), bh = dflt.getHeight();
		if (m.getX() != null) x = edge(m.getXMode()) ? m.getX().getVal() * w : x + m.getX().getVal() * w;
		if (m.getY() != null) y = edge(m.getYMode()) ? m.getY().getVal() * h : y + m.getY().getVal() * h;
		if (m.getW() != null) bw = edge(m.getWMode()) ? m.getW().getVal() * w - x : m.getW().getVal() * w;
		if (m.getH() != null) bh = edge(m.getHMode()) ? m.getH().getVal() * h - y : m.getH().getVal() * h;
		return new Rectangle2D.Double(x, y, Math.max(0, bw), Math.max(0, bh));
	}

	// ------------------------------------------------------------------ the frame

	private void layout() {
		CTChartSpace cs = model.getSource();
		double ix = INSET, iy = INSET, iw = w - 2 * INSET, ih = h - 2 * INSET;

		// the chart area
		Color areaFill = fill(cs.getSpPr());
		Drawing.Gradient areaGradient = gradient(cs.getSpPr());
		if (areaFill != null || areaGradient != null) {
			Drawing.Area a = new Drawing.Area(new Rectangle2D.Double(0, 0, w, h), areaGradient != null ? areaGradient.colours[0] : areaFill, null, 0,
					"chartArea");
			a.gradient = areaGradient;
			a.pattern = areaGradient == null ? pattern(cs.getSpPr()) : null;
			a.shadows = shadows(cs.getSpPr());
			d.add(a);
		}

		// the title
		double plotTop = iy + PLOT_EDGE;
		List<Drawing.Text> texts = new ArrayList<>();
		if (model.getTitle() != null && model.getTitle().getShownText() != null) {
			CTTitle t = model.getTitle().getSource();
			List<Line> lines = wrap(titleText(t), TITLE_MAX * w - 2 * TITLE_PAD_X);
			double tw = 0, th = 0;
			for (Line l : lines) {
				tw = Math.max(tw, l.width);
				th += l.line;
			}
			double bw = tw + 2 * TITLE_PAD_X, bh = th + 2 * TITLE_PAD_Y;
			double bx = ix + (iw - bw) / 2, by = iy + TITLE_TOP;
			// the plot area keeps the room below the title where Office would put it, wherever it is
			// placed by hand (r9-title-moved), or where it is over the plot area (overlay)
			if (!model.getTitle().isOverlay()) plotTop = by + bh + GAP;
			CTManualLayout tm = manualLayout(t.getLayout());
			if (tm != null) {
				Rectangle2D moved = manual(tm, new Rectangle2D.Double(bx, by, bw, bh));
				bx = moved.getX();
				by = moved.getY();
			}
			d.boxes.put("title", new Rectangle2D.Double(bx, by, bw, bh));
			double y = by + TITLE_PAD_Y;
			for (Line l : lines) {
				double x = bx + (bw - l.width) / 2;
				for (Seg sg : l.segs) {
					double sw = sg.style.width(sg.text);
					texts.add(text(sg.style, sg.text, x, y + l.ascent + 0.15, sw, 0, "title"));
					x += sw;
				}
				y += l.line;
			}
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
				double ew = 2 * (e.lineKey ? lineKey(e) : key) + s.width(e.name);
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
				// a legend over the plot area (overlay) takes no room from it
				if (model.getLegend().isOverlay()) {
					// nothing
				} else if ("b".equals(pos)) plotBottom = ly - GAP;
				else plotTop = ly + lh + GAP;
			} else {
				lw = maxW + 3 + 2 * LEGEND_SIDE;
				lh = entries.size() * rowH;
				lx = "l".equals(pos) ? ix + LEGEND_EDGE : ix + iw - LEGEND_EDGE - lw;
				// centred on the plot area; at the top right, below the title, the plot area below it
				ly = "tr".equals(pos) ? top : (plotTop + plotBottom) / 2 - lh / 2;
				for (int i = 0; i < entries.size(); i++) at.add(new double[] { lx + LEGEND_SIDE, ly + LEGEND_TOP + i * rowH, maxW + 3 });
				if (model.getLegend().isOverlay()) {
					// nothing
				} else if ("l".equals(pos)) plotLeft = lx + lw + GAP;
				else plotRight = lx - GAP;
				if ("tr".equals(pos) && !model.getLegend().isOverlay()) {
					plotTop = ly + lh + LEGEND_TR_BELOW;
					plotBottom -= LEGEND_TR_BOTTOM;
				}
			}
			// placed by hand: the plot area keeps the room Office would give the legend (r9-legend-moved);
			// sized, its entries in a grid of as many columns as hold the widest, each centred in its
			// cell, 0.75pt right and 0.055pt down (r9-legend-moved, r15-legend-right-sized)
			CTManualLayout lm = manualLayout(lg.getLayout());
			if (lm != null) {
				Rectangle2D moved = manual(lm, new Rectangle2D.Double(lx, ly, lw, lh));
				if (lm.getW() != null || lm.getH() != null) {
					int k = entries.size();
					double cellMax = horizontal ? maxW : maxW + 3;
					int cols = Math.max(1, Math.min(k, (int) Math.floor(moved.getWidth() / cellMax)));
					int rows = (int) Math.ceil(k / (double) cols);
					double cw = moved.getWidth() / cols, ch = moved.getHeight() / rows;
					at.clear();
					for (int i = 0; i < k; i++) {
						at.add(new double[] { moved.getX() + (i % cols) * cw + (cw - cellMax) / 2 + 0.75,
								moved.getY() + (i / cols) * ch + (ch - s.line) / 2 + 0.055, cellMax });
					}
				} else {
					for (double[] e : at) {
						e[0] += moved.getX() - lx;
						e[1] += moved.getY() - ly;
					}
				}
				lx = moved.getX();
				ly = moved.getY();
				lw = moved.getWidth();
				lh = moved.getHeight();
			}
			d.boxes.put("legend", new Rectangle2D.Double(lx, ly, lw, lh));
			for (int i = 0; i < entries.size(); i++) {
				Entry en = entries.get(i);
				double[] e = at.get(i);
				double kw = en.lineKey ? lineKey(en) : key;
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
					Drawing.Area ka = new Drawing.Area(new Rectangle2D.Double(kx, ky, key, key), en.fill, en.line, en.lineWidth, "legend.key." + i);
					ka.gradient = gradient(en.sp);
					ka.pattern = ka.gradient == null ? pattern(en.sp) : null;
					keys.add(ka);
				}
				double tx = e[0] + 2 * kw + (2.94 / 10.986 - LEGEND_KEY / 2) * s.line;
				texts.add(text(s, en.name, tx, e[1] + s.ascent * 0.956, s.width(en.name), 0,
						"legend.text." + i));
			}
		}

		List<Drawing.Text> labels = new ArrayList<>();
		if (pie) {
			pie(group, series, plotLeft, plotTop, plotRight, plotBottom);
		} else {
			// axis titles take the plot area's left and bottom edges, the plot area beside them
			AxisTitle left = null, bottom = null;
			for (ChartAxis a : model.getAxes()) {
				if (!group.getAxisIds().contains(a.getId()) || a.getTitle() == null || a.getTitle().getShownText() == null) continue;
				if ("l".equals(a.getPosition()) && left == null) left = axisTitle(a, true);
				else if ("b".equals(a.getPosition()) && bottom == null) bottom = axisTitle(a, false);
			}
			if (left != null) {
				left.box.setRect(plotLeft, 0, left.box.getWidth(), left.box.getHeight());
				plotLeft += left.box.getWidth();
				titleInsetLeft = true;
			}
			if (bottom != null) {
				bottom.box.setRect(0, plotBottom - bottom.box.getHeight(), bottom.box.getWidth(), bottom.box.getHeight());
				plotBottom -= bottom.box.getHeight();
				titleInsetBottom = true;
			}
			d.boxes.put("plotArea", new Rectangle2D.Double(plotLeft, plotTop, plotRight - plotLeft, plotBottom - plotTop));
			axes(group, series, plotLeft, plotTop, plotRight, plotBottom, labels);
			// each centred on the inside along its axis (its top no higher than the plot area's), beside a
			// plot area placed by hand (r15-plot-outer-axis-titles)
			Rectangle2D inside = d.boxes.get("inside"), pa = d.boxes.get("plotArea");
			if (left != null) {
				double top = Math.max(pa.getMinY(), inside.getCenterY() - left.box.getHeight() / 2);
				double x = manualPlot ? pa.getMinX() - left.box.getWidth() : left.box.getX();
				left.box.setRect(x, top, left.box.getWidth(), left.box.getHeight());
				placeAxisTitle(left, texts);
			}
			if (bottom != null) {
				double y = manualPlot ? pa.getMaxY() : bottom.box.getY();
				bottom.box.setRect(inside.getCenterX() - bottom.box.getWidth() / 2, y, bottom.box.getWidth(), bottom.box.getHeight());
				placeAxisTitle(bottom, texts);
			}
		}
		dataLabels(group, series);
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
			boolean lines = kind == ChartGroup.Kind.LINE || kind == ChartGroup.Kind.SCATTER;
			for (int i = 0; i < n; i++) {
				CTShapeProperties sp = pointSpPr(s, i);
				if (lines) {
					// a line's points each an entry, its key the series' line and marker (r15-vary-colors-line)
					CTShapeProperties ssp = spPr(s);
					Color c = ssp == null ? automatic(s.getIdx(), seriesCount()) : lineColour(ssp);
					Entry e = new Entry(categoryText(s.getCategories(), i, null), null, c, ssp == null ? 2.25 : lineWidth(ssp), c != null, marker(s));
					e.idx = i;
					out.add(e);
					continue;
				}
				Entry e = new Entry(categoryText(s.getCategories(), i, null), pointFill(s, i), lineColour(sp), lineWidth(sp), false, null);
				e.sp = sp;
				e.idx = i;
				out.add(e);
			}
			return undeleted(out);
		}
		boolean line = kind == ChartGroup.Kind.LINE || kind == ChartGroup.Kind.SCATTER;
		for (ChartSeries s : series) {
			String name = s.getShownName() == null ? "" : s.getShownName();
			CTShapeProperties sp = spPr(s);
			// a line series' key is its line, its marker over it; a scatter series with no line has a
			// square key, its marker in it (r1-scatter.pptx)
			Entry e = line ? new Entry(name, null, lineColour(sp), lineWidth(sp), lineColour(sp) != null, marker(s))
					: new Entry(name, seriesFill(s), lineColour(sp), lineWidth(sp), false, null);
			if (!line) e.sp = sp;
			e.idx = s.getIdx();
			out.add(e);
		}
		out = undeleted(out);
		// (its categories reversed, from the top down, the series in order: r15-bar-category-reversed)
		boolean catRev = false;
		for (ChartAxis a : model.getAxes()) catRev |= g.getAxisIds().contains(a.getId()) && a.getKind() != ChartAxis.Kind.VALUE && reversed(a);
		if (kind == ChartGroup.Kind.BAR && "bar".equals(g.getBarDirection()) && !stacked(g) && !catRev) Collections.reverse(out);
		else if (vertical && stacked(g) && (kind == ChartGroup.Kind.AREA || (kind == ChartGroup.Kind.BAR && !"bar".equals(g.getBarDirection())))) {
			Collections.reverse(out);
		}
		return out;
	}

	/** A line series' legend key's width: {@link #LINE_KEY}, or for a wide line {@link #LINE_KEY_PER_WIDTH} times its width */
	private static double lineKey(Entry e) {
		return Math.max(LINE_KEY, LINE_KEY_PER_WIDTH * e.lineWidth);
	}

	/** The entries the legend keeps: those its c:legendEntry elements don't delete */
	private List<Entry> undeleted(List<Entry> entries) {
		if (model.getLegend() == null || model.getLegend().getDeletedEntries().isEmpty()) return entries;
		List<Entry> out = new ArrayList<>();
		for (Entry e : entries) if (!model.getLegend().getDeletedEntries().contains(e.idx)) out.add(e);
		return out;
	}

	private static boolean stacked(ChartGroup g) {
		return "stacked".equals(g.getGrouping()) || "percentStacked".equals(g.getGrouping());
	}

	// ------------------------------------------------------------------ the plot, with axes

	/** Whether the value axis is a 100% chart's (set by axes() for its frame) */
	private boolean percentAxis;

	/** Whether the plot area is placed by hand (set by axes() for its frame): its labels in by {@link #MANUAL_INSET} more */
	private boolean manualPlot;

	/** Whether an axis title is at the plot area's left or bottom: the labels there in by {@link #MANUAL_INSET} more (R15) */
	private boolean titleInsetLeft, titleInsetBottom;

	/** An axis's title: its lines, its box (turned with its text), its Excel axis type (1 category or x, 2 value) */
	private static final class AxisTitle {
		List<Line> lines;
		Rectangle2D box = new Rectangle2D.Double();
		int turn;
		double textWidth;
		int type;
		CTShapeProperties sp;
		CTManualLayout manual;
	}

	/**
	 * An axis's title (R15): its rich text (Office's "Axis Title" where it has none) in its style,
	 * turned as its bodyPr says (an axis at the left's is turned up the page unless it says rot 0),
	 * wrapped at 0.8 of the chart's side it runs along, less its padding (r15-axis-title-long); its
	 * box its lines and the title's padding, 3pt along and 1.5pt across, turned with it
	 */
	private AxisTitle axisTitle(ChartAxis a, boolean leftSide) {
		CTTitle t = a.getTitle().getSource();
		AxisTitle at = new AxisTitle();
		Style st = style(t.getTxPr(), base());
		CTTextBody body = t.getTx() != null && t.getTx().getRich() != null ? t.getTx().getRich() : t.getTxPr();
		Integer rot = body != null && body.getBodyPr() != null ? body.getBodyPr().getRot() : null;
		at.turn = rot == null ? (leftSide ? -1 : 0) : rot == -5400000 ? -1 : rot == 5400000 ? 1 : 0;
		List<List<Seg>> paras;
		if (t.getTx() != null && t.getTx().getRich() != null) {
			paras = paragraphs(t.getTx().getRich(), st);
		} else {
			paras = new ArrayList<>();
			for (String p : a.getTitle().getShownText().split("\n", -1)) {
				List<Seg> para = new ArrayList<>();
				para.add(new Seg(p, st));
				paras.add(para);
			}
		}
		at.lines = wrap(paras, TITLE_MAX * (at.turn != 0 ? h : w) - 2 * TITLE_PAD_X);
		double tw = 0, th = 0;
		for (Line l : at.lines) {
			tw = Math.max(tw, l.width);
			th += l.line;
		}
		at.textWidth = tw;
		double along = tw + 2 * TITLE_PAD_X, across = th + 2 * TITLE_PAD_Y;
		at.box = at.turn != 0 ? new Rectangle2D.Double(0, 0, across, along) : new Rectangle2D.Double(0, 0, along, across);
		at.type = a.getKind() == ChartAxis.Kind.VALUE && !(leftSide == false && model.getGroups().get(0).getKind() == ChartGroup.Kind.SCATTER) ? 2 : 1;
		at.sp = t.getSpPr();
		at.manual = manualLayout(t.getLayout());
		return at;
	}

	/**
	 * An axis title's text drawn in its box, each line centred; placed by hand as the chart's title
	 * is, the plot area keeping its room (r15-axis-title-moved)
	 */
	private void placeAxisTitle(AxisTitle at, List<Drawing.Text> texts) {
		if (at.manual != null) {
			Rectangle2D moved = manual(at.manual, at.box);
			at.box = new Rectangle2D.Double(moved.getX(), moved.getY(), at.box.getWidth(), at.box.getHeight());
		}
		Rectangle2D b = at.box;
		d.boxes.put("axisTitle." + at.type, b);
		Drawing.Area ba = area(b, at.sp, fill(at.sp), "axisTitle." + at.type, null);
		if (ba.fill != null || ba.line != null) d.add(ba);
		double across = 0;
		for (Line l : at.lines) {
			double along = (at.textWidth - l.width) / 2;
			for (Seg sg : l.segs) {
				double sw = sg.style.width(sg.text);
				if (at.turn < 0) {
					texts.add(text(sg.style, sg.text, b.getX() + TITLE_PAD_Y + across + l.ascent + 0.15, b.getMaxY() - TITLE_PAD_X - along, sw, -90,
							"axisTitle." + at.type));
				} else if (at.turn > 0) {
					texts.add(text(sg.style, sg.text, b.getMaxX() - TITLE_PAD_Y - across - l.ascent - 0.15, b.getY() + TITLE_PAD_X + along, sw, 90,
							"axisTitle." + at.type));
				} else {
					texts.add(text(sg.style, sg.text, b.getX() + TITLE_PAD_X + along, b.getY() + TITLE_PAD_Y + across + l.ascent + 0.15, sw, 0,
							"axisTitle." + at.type));
				}
				along += sw;
			}
			across += l.line;
		}
	}

	/** What each point's data label is placed by: its bar, its point and marker, its slice */
	private static final class Anchor {
		final int s, i;
		/** a bar's box; a point's place (and its marker's half size); a slice's centre */
		double x, y, w, h, m;
		/** a bar along the value axis: where it starts and ends (y down a column, x along a bar) */
		double base, end;
		boolean bar, across, area, slice;
		/** a slice's inner and outer radius, its middle angle (degrees clockwise from twelve o'clock) */
		double inner, outer, mid;
		/** the value, for its label */
		double value;

		Anchor(int s, int i) {
			this.s = s;
			this.i = i;
		}
	}

	private final List<Anchor> anchors = new ArrayList<>();

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

		final ChartAxis cAx = catAx, vAx = valAx;
		final double fdmin = dmin, fdmax = dmax, fxmin = xmin, fxmax = xmax;
		final int fnCat = nCat;
		java.util.function.Function<double[], Frame> frameOf = b -> scatter
				? scatterFrame(vAx, cAx, valLabels, catLabels, vs, cst, fdmin, fdmax, setMin, setMax, fxmin, fxmax, valCode, series, b[0], b[1], b[2], b[3])
				: across ? barFrame(group, vAx, cAx, valLabels, catLabels, vs, cst, fdmin, fdmax, setMin, setMax, valCode, series, fnCat, b[0], b[1], b[2],
						b[3])
						: columnFrame(vAx, cAx, valLabels, catLabels, vs, cst, fdmin, fdmax, setMin, setMax, valCode, series, fnCat, b[0], b[1], b[2], b[3]);
		Frame f = frameOf.apply(new double[] { plotLeft, plotTop, plotRight, plotBottom });
		// the plot area placed by hand: its outer box, or its inside (r9-plot-outer, r9-plot-inner), the
		// other found around it; its labels then in from its edges by MANUAL_INSET more
		CTManualLayout pm = model.getSource().getChart() == null || model.getSource().getChart().getPlotArea() == null ? null
				: manualLayout(model.getSource().getChart().getPlotArea().getLayout());
		if (pm != null) {
			manualPlot = true;
			double[] o;
			if (pm.getLayoutTarget() != null && pm.getLayoutTarget().getVal() == STLayoutTarget.INNER) {
				Rectangle2D t = manual(pm, new Rectangle2D.Double(f.left, f.top, f.right - f.left, f.bottom - f.top));
				o = new double[] { t.getMinX(), t.getMinY(), t.getMaxX(), t.getMaxY() };
				// the labels' room depends on the scale, and the scale on the inside's length: a few rounds settle it
				for (int k = 0; k < 4; k++) {
					Frame g = frameOf.apply(o);
					o = new double[] { t.getMinX() - (g.left - o[0]), t.getMinY() - (g.top - o[1]), t.getMaxX() + (o[2] - g.right),
							t.getMaxY() + (o[3] - g.bottom) };
				}
			} else {
				Rectangle2D b = manual(pm, new Rectangle2D.Double(plotLeft, plotTop, plotRight - plotLeft, plotBottom - plotTop));
				o = new double[] { b.getMinX(), b.getMinY(), b.getMaxX(), b.getMaxY() };
			}
			f = frameOf.apply(o);
			d.boxes.put("plotArea", new Rectangle2D.Double(o[0], o[1], o[2] - o[0], o[3] - o[1]));
		}
		d.valueAxis = new double[] { f.scale.min, f.scale.max, f.scale.unit };
		if (f.xScale != null) d.xAxis = new double[] { f.xScale.min, f.xScale.max, f.xScale.unit };
		else if (f.dates != null) d.xAxis = f.dates;
		Rectangle2D inside = new Rectangle2D.Double(f.left, f.top, f.right - f.left, f.bottom - f.top);
		d.boxes.put("inside", inside);
		plotAreaFill(inside);

		// gridlines; the axes' lines, where each crosses the other; tick marks
		CTShapeProperties catSp = catAx == null ? null : spPr(catAx.getSource());
		CTShapeProperties valSp = valAx == null ? null : spPr(valAx.getSource());
		boolean catLine = catAx != null && !catAx.isDeleted() && lineColour(catSp) != null;
		boolean valLine = valAx != null && !valAx.isDeleted() && lineColour(valSp) != null;
		double cross = scatter ? (f.scale.log ? f.scale.min : Math.max(f.scale.min, Math.min(f.scale.max, 0))) : f.crossValue;
		double crossV = f.v(cross);
		// where the value axis crosses: a category axis's start (or end, or a category: Frame#crossPos), a scatter chart's x of zero
		double crossC = scatter ? f.x(f.xScale.log ? f.xScale.min : Math.max(f.xScale.min, Math.min(f.xScale.max, 0)))
				: !Double.isNaN(f.crossPos) ? f.crossPos : across ? f.bottom : f.left;
		// gridlines: the value axis's minor then major ones, a scatter chart's x axis's, the category
		// axis's at its tick marks (r10-column-style-213); none where an axis's line is drawn over them
		CTChartLines minor = valAx == null ? null : gridlines(valAx.getSource(), false);
		if (minor != null && !f.scale.log) {
			double unit = valAx.getMinorUnit() != null ? valAx.getMinorUnit() : f.scale.unit / 5;
			for (double v = f.scale.min; unit > 0 && v <= f.scale.max + unit * 1e-6; v += unit) {
				double p = f.v(v);
				boolean major = false;
				for (double m : f.values) major |= Math.abs(f.v(m) - p) < 0.01;
				if (major || (catLine && Math.abs(p - crossV) < 0.01)) continue;
				gridline(across ? new Line2D.Double(p, f.top, p, f.bottom) : new Line2D.Double(f.left, p, f.right, p), minor, "gridline.minor");
			}
		}
		CTChartLines grid = valAx == null ? null : gridlines(valAx.getSource(), true);
		if (grid != null) {
			for (double v : f.values) {
				double p = f.v(v);
				if (catLine && Math.abs(p - crossV) < 0.01) continue;
				gridline(across ? new Line2D.Double(p, f.top, p, f.bottom) : new Line2D.Double(f.left, p, f.right, p), grid, "gridline");
			}
		}
		CTChartLines xGrid = scatter ? gridlines(catAx.getSource(), true) : null;
		if (xGrid != null) {
			for (double v : f.xValues) {
				double p = f.x(v);
				if (valLine && Math.abs(p - crossC) < 0.01) continue;
				gridline(new Line2D.Double(p, f.top, p, f.bottom), xGrid, "gridline.x");
			}
		}
		CTChartLines catGrid = !scatter && catAx != null ? gridlines(catAx.getSource(), true) : null;
		if (catGrid != null) {
			for (double p : f.ticks) {
				if (valLine && Math.abs(p - crossC) < 0.01) continue;
				gridline(across ? new Line2D.Double(f.left, p, f.right, p) : new Line2D.Double(p, f.top, p, f.bottom), catGrid, "gridline.category");
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
			// minor tick marks at each category's middle, and at the axis's far end (r15-ticks-minor; one probe)
			String minorTick = scatter ? null : tickMark(catAx.getSource(), false);
			if (minorTick != null && f.categories.length > 0) {
				List<Double> at = new ArrayList<>();
				for (double c : f.categories) at.add(c);
				if (!f.ticks.isEmpty()) at.add(f.ticks.get(f.ticks.size() - 1));
				for (double p : at) {
					d.add(new Drawing.Area(tick(!across, p, crossV, minorTick, MINOR_TICK * cst.size), null, lineColour(catSp), lineWidth(catSp),
							"tick.category.minor"));
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
			// minor tick marks at the minor unit, the major ones' places too (r15-ticks-minor: 2.13pt at 9pt; one probe)
			String minorTick = tickMark(valAx.getSource(), false);
			if (minorTick != null && !f.scale.log) {
				double unit = valAx.getMinorUnit() != null ? valAx.getMinorUnit() : f.scale.unit / 5;
				for (double v = f.scale.min; unit > 0 && v <= f.scale.max + unit * 1e-6; v += unit) {
					d.add(new Drawing.Area(tick(across, f.v(v), crossC, minorTick, MINOR_TICK * vs.size), null, lineColour(valSp), lineWidth(valSp),
							"tick.value.minor"));
				}
			}
		}

		// the value labels: at the left right-aligned, centred on their gridlines; below, centred under them
		for (int k = 0; k < f.valueTexts.size(); k++) {
			String t = f.valueTexts.get(k);
			double p = f.v(f.values.get(k));
			if (across) labels.add(f.valSide == HIGH ? above(t, vs, p, f.top, "value." + k) : below(t, vs, p, f.bottom, "value." + k));
			else if (f.valSide == HIGH) labels.add(right(t, vs, p, f.right, "value." + k));
			else labels.add(left(t, vs, p, f.valSide == MID ? crossC : f.left, "value." + k));
		}
		for (int k = 0; k < f.xTexts.size(); k++) {
			labels.add(below(f.xTexts.get(k), cst, f.x(f.xValues.get(k)), f.bottom, "x." + k));
		}
		labels.addAll(f.categoryLabels);
	}

	/** A text right-aligned against the plot's inside, centred on y */
	private Drawing.Text left(String t, Style s, double y, double insideLeft, String role) {
		double tw = s.width(t);
		return text(s, t, insideLeft - VALUE_LABEL_GAP * s.line - tw, y + 0.289 * s.size + 0.2, tw, 0,
				role);
	}

	/** A text left-aligned against the plot's inside's right, centred on y (value labels at the right) */
	private Drawing.Text right(String t, Style s, double y, double insideRight, String role) {
		double tw = s.width(t);
		return text(s, t, insideRight + VALUE_LABEL_GAP * s.line, y + 0.289 * s.size + 0.2, tw, 0, role);
	}

	/** A text centred over x, above the axis at y (a bar chart's value labels at the top; provisional) */
	private Drawing.Text above(String t, Style s, double x, double y, String role) {
		double tw = s.width(t);
		return text(s, t, x - tw / 2, y - 0.61 * s.size - 0.45 - (s.line - s.ascent) - ABOVE_SHIFT, tw, 0, role);
	}

	/** A text centred under x, below the axis at y */
	private Drawing.Text below(String t, Style s, double x, double y, String role) {
		double tw = s.width(t);
		return text(s, t, x - tw / 2, y + 0.61 * s.size + 0.45 + s.ascent, tw, 0, role);
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
		/** the values' order reversed (maxMin): down a column chart, leftwards along a bar chart */
		boolean valReversed;
		/** the categories' order reversed (maxMin): from the right of a column chart, down a bar chart */
		boolean catReversed;
		/** the value where the category axis crosses the value axis */
		double crossValue;
		/** where the value axis's line is across the categories (x on a column chart, y on a bar chart); NaN: Office's own */
		double crossPos = Double.NaN;
		/** which side the value labels are on: LOW (left of a column chart, below a bar chart), HIGH, or MID (beside the axis's line) */
		int valSide = LOW;
		/** which side the category labels are on: LOW (below a column chart), HIGH (above), or MID (hanging from the axis's line) */
		int catSide = LOW;

		/** a value's place along the value axis */
		double v(double value) {
			double fr = scale.fraction(value);
			if (valReversed) fr = 1 - fr;
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
		double inset = manualPlot || titleInsetLeft ? MANUAL_INSET : 0, insetB = manualPlot || titleInsetBottom ? MANUAL_INSET : 0;
		f.top = plotTop + (valLabels ? manualPlot ? vs.line / 2 : Math.max(0, vs.line / 2 - VALUE_LABEL_TOP) : 0);
		f.valReversed = reversed(valAx);
		boolean catRev = reversed(catAx);
		f.catReversed = catRev;

		// the scale, chosen with one line of category labels.  The category labels take a band at
		// the plot's bottom (or top) only where their axis is there: within the plot they hang from
		// the axis's line wherever it crosses (R2, R15)
		boolean guessLow = catAx != null && catAx.getCrossesAt() != null ? setMin != null && catAx.getCrossesAt() <= setMin
				: catAx != null && "max".equals(catAx.getCrosses()) ? false : dmin >= 0 || (setMin != null && setMin >= 0);
		boolean guessHigh = catAx != null && catAx.getCrossesAt() == null && "max".equals(catAx.getCrosses());
		f.catSide = labelSide(catAx, guessLow, guessHigh, f.valReversed);
		double band1 = catLabels && f.catSide != MID ? band(cst, 1) + (f.catSide == LOW ? insetB : 0) : 0;
		double scaleTop = f.catSide == HIGH && catLabels ? plotTop : f.top;
		// (a maximum set can take the scale below zero, so this is decided again with the scale)
		f.scale = AxisScale.of(dmin, dmax, setMin, setMax, valAx == null ? null : valAx.getMajorUnit(), valAx == null ? null : valAx.getLogBase(),
				plotBottom - band1 - scaleTop, vs.line, percentAxis ? AxisScale.Bounds.PERCENT : AxisScale.Bounds.VALUE);
		f.crossValue = crossValue(catAx, f.scale);
		// (zero at the maximum, all the data negative, is within the plot: the labels hang from it, r2-negative)
		boolean explicitMax = catAx != null && (catAx.getCrossesAt() != null ? catAx.getCrossesAt() >= f.scale.max : "max".equals(catAx.getCrosses()));
		f.catSide = labelSide(catAx, f.crossValue <= f.scale.min, explicitMax && !(f.crossValue <= f.scale.min), f.valReversed);
		valueLabels(f, f.scale, valLabels, valCode, f.values, f.valueTexts);
		// the value labels: left of the plot, right of it (the value axis crossing after the last
		// category, or the categories reversed: r15-valax-crosses-max, r15-category-reversed), or
		// beside the axis's line within it, taking no room (r15-valax-crosses-at-3)
		double[] crossAt = { 1 };
		int vc = valueCrossing(valAx, nCat, crossAt);
		f.valSide = labelSide(valAx, vc < 0, vc > 0, catRev);
		double labelsW = widest(f.valueTexts, vs) + VALUE_LABEL_GAP * vs.line;
		f.left = valLabels && f.valSide == LOW ? plotLeft - VALUE_LABEL_OVERHANG + inset + labelsW : plotLeft;
		f.right = valLabels && f.valSide == HIGH ? plotRight + VALUE_LABEL_OVERHANG - labelsW : plotRight;
		boolean bottomAxis = f.catSide == LOW;

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
			// (from the plot area's edge less its labels' inset beside an axis title: r15-axis-titles-area)
			f.left = Math.max(f.left, plotLeft - inset + cst.width(catTexts.get(0)) / 2);
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
		double band = !catLabels || f.catSide == MID ? 0 : bandOf(mode, cst, lines, shownMax) + (bottomAxis ? insetB : 0);
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
		if (f.catSide == HIGH) {
			f.top = plotTop + band;
			f.bottom = Math.max(f.top, plotBottom);
		} else {
			f.bottom = Math.max(f.top, plotBottom - band);
		}
		f.catW = catW;
		if (dates) {
			LocalDate end = "months".equals(c.unit) ? c.start.plusMonths(c.span) : "years".equals(c.unit) ? c.start.plusYears(c.span) : c.start.plusDays(c.span);
			f.dates = new double[] { serial(c.start), serial(end), c.step };
		}
		// the categories from the left, or (reversed) from the right
		final double x0 = catRev ? f.right : f.left, dx = catRev ? -catW : catW;
		f.categories = new double[nCat];
		for (int i = 0; i < nCat; i++) f.categories[i] = x0 + (c.slot[i] + (midCat ? 0 : 0.5)) * dx;
		if (midCat) for (int k = 0; k <= c.span; k += Math.max(1, c.step)) f.ticks.add(x0 + k * dx);
		else for (int k = 0; k <= c.span + 1; k += Math.max(1, c.step)) f.ticks.add(x0 + k * dx);
		f.crossPos = vc == 0 ? x0 + (crossAt[0] - 1) * dx : (vc < 0) != catRev ? f.left : f.right;

		// the category labels, centred under their categories (turned: a turned 90 label's line
		// centred on its category, its end at the labels' top; a turned 45 label's end there); above
		// the plot where their axis is at its top, a line's height up from it (provisional)
		double crossV = f.catSide == LOW ? f.bottom : f.catSide == HIGH ? f.top : f.v(f.crossValue);
		for (int i = 0; i < catLines.size(); i++) {
			double cx = x0 + (c.labelSlot[i] + (midCat ? 0 : 0.5)) * dx;
			List<String> l = catLines.get(i);
			double top = f.catSide == HIGH ? crossV - (0.61 * cst.size + 0.45) - l.size() * cst.line - ABOVE_SHIFT : crossV + 0.61 * cst.size + 0.45;
			for (int j = 0; j < l.size(); j++) {
				String t = l.get(j);
				double tw = cst.width(t);
				if (mode == TURNED_90) {
					double bx = cx + (cst.ascent - (cst.line - cst.ascent)) / 2;
					f.categoryLabels.add(text(cst, t, bx, top + tw, tw, -90, "category." + i));
				} else if (mode == TURNED_45) {
					// the turned line's top corner on the labels' top, its end's middle over the
					// category's centre (Word's glyph outlines, R12: to 0.1pt)
					double ex = cx + (cst.ascent - (cst.line - cst.ascent)) / 2 * SIN45, ey = top + cst.ascent * SIN45;
					f.categoryLabels.add(text(cst, t, ex - tw * SIN45, ey + tw * SIN45, tw, -45,
							"category." + i));
				} else {
					double base = top + cst.ascent + j * cst.line;
					f.categoryLabels.add(text(cst, t, cx - tw / 2, base, tw, 0, "category." + i));
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
		f.catReversed = reversed(catAx);
		f.valReversed = reversed(valAx);
		Categories c = categories(series, catAx, nCat);
		List<String> catTexts = catLabels ? c.labels : new ArrayList<>();
		double overhead = VALUE_LABEL_GAP * cst.line - VALUE_LABEL_OVERHANG;
		double inset = manualPlot || titleInsetLeft ? MANUAL_INSET : 0, insetB = manualPlot || titleInsetBottom ? MANUAL_INSET : 0;
		f.left = catLabels ? plotLeft + widest(catTexts, cst) + overhead + inset : plotLeft;
		// the value labels below the plot, or above it where the value axis crosses at its top (the
		// categories from the top down: r15-bar-category-reversed)
		double[] crossAt = { 1 };
		int vc = valueCrossing(valAx, nCat, crossAt);
		f.valSide = labelSide(valAx, vc < 0, vc > 0, f.catReversed);
		f.top = plotTop + (valLabels && f.valSide == HIGH ? band(vs, 1) : 0);
		f.bottom = plotBottom - (valLabels && f.valSide == LOW ? band(vs, 1) + insetB : 0);
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
			f.left = plotLeft + w + overhead + inset;
			f.scale = acrossScale(dmin, dmax, setMin, setMax, valAx, valLabels, valCode, vs, plotLeft, f.left, plotRight, kind);
		}
		valueLabels(f, f.scale, valLabels, valCode, f.values, f.valueTexts);
		f.right = plotRight - (valLabels ? vs.width(format(f.scale.max, valCode)) / 2 : 0);
		// the category labels left of the plot, or (the category axis crossing within it: values of
		// either sign) beside the axis's line, taking no room; the first value label then kept within
		// the plot area (r15-labels-negative-bar)
		f.crossValue = crossValue(catAx, f.scale);
		boolean explicitMax = catAx != null && (catAx.getCrossesAt() != null ? catAx.getCrossesAt() >= f.scale.max : "max".equals(catAx.getCrosses()));
		f.catSide = labelSide(catAx, f.crossValue <= f.scale.min, explicitMax && !(f.crossValue <= f.scale.min), f.valReversed);
		if (f.catSide == MID) f.left = plotLeft + (valLabels ? vs.width(format(f.scale.min, valCode)) / 2 : 0);
		double slots = c.span + 1;
		f.catW = (f.bottom - f.top) / slots;
		final double y0 = f.catReversed ? f.top : f.bottom, dy = f.catReversed ? f.catW : -f.catW;
		f.categories = new double[nCat];
		for (int i = 0; i < nCat; i++) f.categories[i] = y0 + (c.slot[i] + 0.5) * dy;
		for (int k = 0; k <= c.span + 1; k++) f.ticks.add(y0 + k * dy);
		f.crossPos = vc == 0 ? y0 + (crossAt[0] - 1) * dy : (vc < 0) != f.catReversed ? f.bottom : f.top;
		for (int i = 0; i < catLines.size(); i++) {
			List<String> l = catLines.get(i);
			double y = y0 + (c.labelSlot[i] + 0.5) * dy - (l.size() - 1) * cst.line / 2;
			double at = f.catSide == MID ? f.v(f.crossValue) : f.left;
			for (int j = 0; j < l.size(); j++) f.categoryLabels.add(left(l.get(j), cst, y + j * cst.line, at, "category." + i));
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
		double inset = manualPlot || titleInsetLeft ? MANUAL_INSET : 0, insetB = manualPlot || titleInsetBottom ? MANUAL_INSET : 0;
		f.top = plotTop + (valLabels ? manualPlot ? vs.line / 2 : Math.max(0, vs.line / 2 - VALUE_LABEL_TOP) : 0);
		f.bottom = plotBottom - (xLabels ? band(xs, 1) + insetB : 0);
		f.scale = AxisScale.of(dmin, dmax, setMin, setMax, valAx == null ? null : valAx.getMajorUnit(), valAx == null ? null : valAx.getLogBase(),
				f.bottom - f.top, vs.line);
		valueLabels(f, f.scale, valLabels, valCode, f.values, f.valueTexts);
		f.left = valLabels ? plotLeft - VALUE_LABEL_OVERHANG + inset + widest(f.valueTexts, vs) + VALUE_LABEL_GAP * vs.line : plotLeft;
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

	private static boolean reversed(ChartAxis a) {
		return a != null && "maxMin".equals(a.getOrientation());
	}

	/**
	 * Where an axis crosses the value axis, in its values (an axis's c:crosses or c:crossesAt is
	 * where it crosses the other axis, in that axis's units: r15-catax-crosses-at-2, -max): its
	 * zero (autoZero, within the scale; a log scale's minimum), the scale's minimum or maximum, or a
	 * value
	 */
	private static double crossValue(ChartAxis a, AxisScale scale) {
		if (a != null && a.getCrossesAt() != null) return Math.max(scale.min, Math.min(scale.max, a.getCrossesAt()));
		String c = a == null ? null : a.getCrosses();
		if ("max".equals(c)) return scale.max;
		if ("min".equals(c) || scale.log) return scale.min;
		return Math.max(scale.min, Math.min(scale.max, 0));
	}

	/**
	 * The side an axis's labels are on (R15): low and high by c:tickLblPos, at that end of the axis
	 * it crosses (its order reversed, the other end); next to its line, the end its line is at, or
	 * MID where its line is within the plot
	 */
	private static int labelSide(ChartAxis a, boolean atLow, boolean atHigh, boolean otherReversed) {
		String tlp = a == null ? null : a.getTickLabelPosition();
		int side;
		if ("low".equals(tlp)) side = LOW;
		else if ("high".equals(tlp)) side = HIGH;
		else return atLow ? (otherReversed ? HIGH : LOW) : atHigh ? (otherReversed ? LOW : HIGH) : MID;
		return otherReversed ? 1 - side : side;
	}

	/**
	 * Where the value axis crosses the categories (its c:crosses or c:crossesAt, in categories):
	 * -1 at the first category's edge, 1 after the last, 0 between, the category's number in at
	 */
	private static int valueCrossing(ChartAxis valAx, int nCat, double[] at) {
		if (valAx != null && valAx.getCrossesAt() != null) {
			double n = valAx.getCrossesAt();
			if (n <= 1) return -1;
			if (n > nCat) return 1;
			at[0] = n;
			return 0;
		}
		return valAx != null && "max".equals(valAx.getCrosses()) ? 1 : -1;
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
			for (int i = 0; i < f.categories.length; i++) {
				if (Double.isNaN(to[s][i])) continue;
				double a = Double.isNaN(from[s][i]) ? crossV : f.v(from[s][i]), b = f.v(to[s][i]);
				double lo = Math.min(a, b), hi = Math.max(a, b);
				// the bars of a category from its start (a column's left, a bar's bottom)
				double offset = -f.catW / 2 + gapWidth * barW / 2 + s * barW * (1 - overlap);
				// (the categories reversed, the series within each too)
				Rectangle2D r = across ? new Rectangle2D.Double(lo, f.catReversed ? f.categories[i] + offset : f.categories[i] - offset - barW, hi - lo, barW)
						: new Rectangle2D.Double(f.catReversed ? f.categories[i] - offset - barW : f.categories[i] + offset, lo, barW, hi - lo);
				d.boxes.put("point." + s + "." + i, r);
				Anchor an = new Anchor(s, i);
				an.bar = true;
				an.across = across;
				an.x = r.getX();
				an.y = r.getY();
				an.w = r.getWidth();
				an.h = r.getHeight();
				an.base = a;
				an.end = b;
				an.value = labelValue(sr, i, false);
				anchors.add(an);
				// clipped to the plot's inside, as Office clips a bar past the axis's bounds (R2)
				if (hi - lo > 0) {
					Drawing.Area bar = area(r, pointSpPr(sr, i), pointFill(sr, i), "point." + s + "." + i, inside);
					// a negative value inverted: white, outlined in black (r15-invert-negative; no c14:invertSolidFillFmt)
					if (Boolean.TRUE.equals(sr.getInvertIfNegative()) && labelValue(sr, i, false) < 0) {
						bar = new Drawing.Area(r, Color.WHITE, Color.BLACK, 0.75, "point." + s + "." + i, inside);
					}
					d.add(bar);
				}
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
				if (!Double.isNaN(to[s][i])) {
					Anchor an = new Anchor(s, i);
					an.area = true;
					an.x = x;
					an.y = y;
					an.base = base;
					an.end = y;
					an.value = labelValue(sr, i, false);
					anchors.add(an);
				}
			}
			for (int i = nCat - 1; i >= 0; i--) {
				double base = Double.isNaN(from[s][i]) ? crossV : f.v(from[s][i]);
				p.lineTo(f.categories[i], base);
			}
			p.closePath();
			CTShapeProperties sp = spPr(sr);
			if (nCat > 0) d.add(area(p, sp, seriesFill(sr), "series." + s, inside));
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
			// the colours varied by point (one series): the legend lists the points, but the line is drawn a
			// piece from each point to the next, all in the series' colour, as its markers (r15-vary-colors-line)
			boolean vary = !scatter && model.getGroups().get(0).variesColors();
			List<double[]> run = new ArrayList<>();
			for (int i = 0; i < nCat; i++) {
				if (Double.isNaN(to[s][i])) {
					// a gap ends the line; spanned, the line goes on to the next point
					if (!"span".equals(blanks)) {
						if (vary) pieces(run, lc, lw, dashes(sp, lw), "series." + s);
						else line(run, smooth, lc, lw, dashes(sp, lw), "series." + s);
						run = new ArrayList<>();
					}
					continue;
				}
				double x = scatter ? f.x(xs[s][i]) : f.categories[i], y = f.v(to[s][i]);
				d.boxes.put("point." + s + "." + i, new Rectangle2D.Double(x, y, 0, 0));
				Anchor an = new Anchor(s, i);
				an.x = x;
				an.y = y;
				// a line's labels stand off as from a marker of its size, Office's 5 where it shows none (r15-labels-line)
				an.m = (m == null ? markerSize(sr) : m.size) / 2;
				an.value = labelValue(sr, i, scatter);
				anchors.add(an);
				run.add(new double[] { x, y, i });
				if (m != null) markers.addAll(marker(m, x, y, "point." + s + "." + i));
			}
			if (vary) pieces(run, lc, lw, dashes(sp, lw), "series." + s);
			else line(run, smooth, lc, lw, dashes(sp, lw), "series." + s);
		}
		for (Drawing.Area a : markers) d.add(a);
	}

	/** A run of a line's points drawn a piece from each point to the next */
	private void pieces(List<double[]> run, Color colour, double width, float[] dash, String role) {
		for (int k = 1; k < run.size(); k++) {
			List<double[]> piece = new ArrayList<>();
			piece.add(run.get(k - 1));
			piece.add(run.get(k));
			line(piece, false, colour, width, dash, role);
		}
	}

	/**
	 * A run of a line's points, straight from one to the next, or smoothed: a Bezier curve through
	 * them, each segment's control points a sixth of its neighbours' difference along (Catmull-Rom),
	 * the ends' a third of the chord (R14's smoothed line and scatter: Word's PDF has these control
	 * points, fitted to 0.006pt)
	 */
	private void line(List<double[]> run, boolean smooth, Color colour, double width, float[] dash, String role) {
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
		Drawing.Area a = new Drawing.Area(p, null, colour, width, role, null, true);
		a.dash = dash;
		d.add(a);
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

	/** A point's value as its label shows it: the series' own, not its share of a 100% stack or its place in a stack */
	private static double labelValue(ChartSeries s, int i, boolean scatter) {
		Double v = number(values(s, scatter), i);
		return v == null ? 0 : v;
	}

	/** A series' marker's size, shown or not (Office's 5 where it states none) */
	private static double markerSize(ChartSeries s) {
		Object o = get(s.getSource(), "getMarker");
		return o instanceof CTMarker && ((CTMarker) o).getSize() != null ? ((CTMarker) o).getSize().getVal() : 5;
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

	// ------------------------------------------------------------------ data labels

	/** A point's data label as its series' c:dLbls (over its group's) and its own c:dLbl say */
	private static final class LabelSpec {
		boolean deleted, value, category, series, percent, key;
		String position, separator, code;
		boolean linked = true;
		CTTextBody txPr;
		CTShapeProperties spPr;
		CTTx tx;
		/** the label moved by hand (its own c:dLbl's layout) */
		CTManualLayout manual;

		boolean shown() {
			return !deleted && (value || category || series || percent || tx != null);
		}

		void apply(CTDLbls l) {
			if (l == null) return;
			if (l.getDelete() != null) deleted = l.getDelete().isVal();
			if (l.getShowVal() != null) value = l.getShowVal().isVal();
			if (l.getShowCatName() != null) category = l.getShowCatName().isVal();
			if (l.getShowSerName() != null) series = l.getShowSerName().isVal();
			if (l.getShowPercent() != null) percent = l.getShowPercent().isVal();
			if (l.getShowLegendKey() != null) key = l.getShowLegendKey().isVal();
			if (l.getDLblPos() != null && l.getDLblPos().getVal() != null) position = l.getDLblPos().getVal().value();
			if (l.getSeparator() != null) separator = l.getSeparator();
			if (l.getNumFmt() != null) {
				code = l.getNumFmt().getFormatCode();
				linked = Boolean.TRUE.equals(l.getNumFmt().isSourceLinked());
			}
			if (l.getTxPr() != null) txPr = l.getTxPr();
			if (l.getSpPr() != null) spPr = l.getSpPr();
		}

		void apply(CTDLbl l) {
			if (l.getDelete() != null && l.getDelete().isVal()) {
				deleted = true;
				return;
			}
			deleted = false;
			if (l.getLayout() != null && l.getLayout().getManualLayout() != null) manual = l.getLayout().getManualLayout();
			if (l.getShowVal() != null) value = l.getShowVal().isVal();
			if (l.getShowCatName() != null) category = l.getShowCatName().isVal();
			if (l.getShowSerName() != null) series = l.getShowSerName().isVal();
			if (l.getShowPercent() != null) percent = l.getShowPercent().isVal();
			if (l.getShowLegendKey() != null) key = l.getShowLegendKey().isVal();
			if (l.getDLblPos() != null && l.getDLblPos().getVal() != null) position = l.getDLblPos().getVal().value();
			if (l.getSeparator() != null) separator = l.getSeparator();
			if (l.getNumFmt() != null) {
				code = l.getNumFmt().getFormatCode();
				linked = Boolean.TRUE.equals(l.getNumFmt().isSourceLinked());
			}
			if (l.getTxPr() != null) txPr = l.getTxPr();
			if (l.getSpPr() != null) spPr = l.getSpPr();
			if (l.getTx() != null && l.getTx().getRich() != null) tx = l.getTx();
		}
	}

	/** Point i's label of a series: the group's labels, the series', then the point's own */
	private LabelSpec labelSpec(ChartGroup g, ChartSeries s, int i) {
		LabelSpec l = new LabelSpec();
		Object gl = get(g.getSource(), "getDLbls"), sl = get(s.getSource(), "getDLbls");
		l.apply(gl instanceof CTDLbls ? (CTDLbls) gl : null);
		l.apply(sl instanceof CTDLbls ? (CTDLbls) sl : null);
		if (l.position == null && s.getDataLabels() != null) l.position = s.getDataLabels().getShownPosition();
		for (Object o : new Object[] { gl, sl }) {
			if (!(o instanceof CTDLbls)) continue;
			for (CTDLbl p : ((CTDLbls) o).getDLbl()) {
				if (p.getIdx() != null && p.getIdx().getVal() == i) l.apply(p);
			}
		}
		return l;
	}

	/**
	 * Each point's data label, placed by what it labels (R6): a bar's at its end (outEnd, inEnd),
	 * its base (inBase) or its middle (ctr), {@link #LABEL_GAP} from it; a point's above, below,
	 * left or right of its marker, or on it; a slice's at the middle of its ray (ctr), inside its
	 * arc (inEnd), outside it (outEnd, provisional) or, for bestFit, inside as inEnd (provisional)
	 */
	private void dataLabels(ChartGroup group, List<ChartSeries> series) {
		boolean scatter = group.getKind() == ChartGroup.Kind.SCATTER;
		boolean pie = group.getKind() == ChartGroup.Kind.PIE || group.getKind() == ChartGroup.Kind.DOUGHNUT;
		for (Anchor an : anchors) {
			if (an.s >= series.size()) continue;
			ChartSeries sr = series.get(an.s);
			LabelSpec ls = labelSpec(group, sr, an.i);
			if (!ls.shown()) continue;
			Style st = style(ls.txPr, base());
			String text;
			if (ls.tx != null) {
				text = richText(ls.tx.getRich());
			} else {
				List<String> parts = new ArrayList<>();
				if (ls.series) parts.add(sr.getShownName() == null ? "" : sr.getShownName());
				ChartData xv = sr.getXValues();
				if (ls.category) parts.add(scatter ? format(numberOr(xv, an.i), xv != null && xv.getFormatCode() != null ? xv.getFormatCode() : "General")
						: categoryText(sr.getCategories(), an.i, null));
				if (ls.value) {
					ChartData v = values(sr, scatter);
					String code = !ls.linked && ls.code != null ? ls.code : v != null && v.getFormatCode() != null ? v.getFormatCode() : "General";
					parts.add(format(an.value, code));
				}
				if (ls.percent) parts.add(format(percent(values(sr, scatter), an.i), "0%"));
				// Office separates the parts by ", " (r15-labels-value-category, -all-three, -pie-everything),
				// but a pie's category and percentage alone by a line (r8-pie-labels-outside; provisional)
				boolean catPercent = pie && ls.category && ls.percent && !ls.value && !ls.series;
				String sep = ls.separator != null ? ls.separator : catPercent ? "\n" : ", ";
				text = String.join(sep, parts);
			}
			List<String> lines = new ArrayList<>();
			for (String line : text.split("\r?\n", -1)) lines.add(line);
			double[] ins = insets(ls.txPr);
			double tw = 0;
			for (String line : lines) tw = Math.max(tw, st.width(line));
			// its legend key before the text: the legend's square, in a slot of two keys and 3pt (r15-labels-legend-key)
			double kw = ls.key ? LEGEND_KEY * st.line : 0, ks = ls.key ? 2 * kw + 3 : 0;
			double bw = tw + ks + ins[0] + ins[2], bh = lines.size() * st.line + ins[1] + ins[3];
			// turned a quarter (bodyPr rot -5400000: up the page, 5400000: down it), the box turned with
			// it and placed by the same rule (r10-column-style-202)
			int turn = rotation(ls.txPr);
			double[] at = labelPlace(an, ls.position == null ? "" : ls.position, turn == 0 ? bw : bh, turn == 0 ? bh : bw);
			Rectangle2D box = new Rectangle2D.Double(at[0], at[1], turn == 0 ? bw : bh, turn == 0 ? bh : bw);
			// moved by hand: in factor mode by shares of the chart from where it would be (r15-labels-pie-moved)
			if (ls.manual != null) box = manual(ls.manual, box);
			String role = "label." + an.s + "." + an.i;
			d.boxes.put(role, box);
			Drawing.Area ba = area(box, ls.spPr, fill(ls.spPr), role, null);
			if (ba.fill != null || ba.line != null) d.add(ba);
			if (ls.key && turn == 0) {
				Color kc = an.slice || group.variesColors() ? pointFill(sr, an.i) : seriesFill(sr);
				if (kc == null) kc = lineColour(spPr(sr));
				d.add(new Drawing.Area(new Rectangle2D.Double(box.getX() + ins[0] + kw / 2 + 0.75, box.getCenterY() - kw / 2 - 0.05, kw, kw), kc, null, 0,
						role + ".key"));
				box = new Rectangle2D.Double(box.getX() + ks, box.getY(), box.getWidth() - ks, box.getHeight());
			}
			for (int j = 0; j < lines.size(); j++) {
				String line = lines.get(j);
				double lw = st.width(line);
				if (turn < 0) {
					d.add(text(st, line, box.getX() + ins[1] + st.ascent + 0.15 + j * st.line, box.getMaxY() - ins[0] - (tw - lw) / 2, lw, -90, role));
				} else if (turn > 0) {
					d.add(text(st, line, box.getMaxX() - ins[1] - st.ascent - 0.15 - j * st.line, box.getY() + ins[0] + (tw - lw) / 2, lw, 90, role));
				} else {
					d.add(text(st, line, box.getX() + ins[0] + (tw - lw) / 2, box.getY() + ins[1] + st.ascent + 0.15 + j * st.line, lw, 0, role));
				}
			}
		}
	}

	/**
	 * A point's share of its series, as Office shows it in a label: whole percentages that add up
	 * to 100%, the difference given to the largest share (r10-pie-style-258: 8.2, 3.2, 1.4 and 1.2
	 * are 58%, 23%, 10% and 9%, not 59%)
	 */
	private static double percent(ChartData v, int i) {
		int n = count(v);
		double total = 0;
		for (int k = 0; k < n; k++) {
			Double x = number(v, k);
			if (x != null) total += Math.abs(x);
		}
		if (total == 0) return 0;
		long sum = 0;
		int largest = -1;
		double max = -1;
		for (int k = 0; k < n; k++) {
			Double x = number(v, k);
			if (x == null) continue;
			sum += Math.round(100 * Math.abs(x) / total);
			if (Math.abs(x) > max) {
				max = Math.abs(x);
				largest = k;
			}
		}
		Double x = number(v, i);
		if (x == null) return 0;
		long p = Math.round(100 * Math.abs(x) / total);
		if (i == largest) p += 100 - sum;
		return p / 100.0;
	}

	/** A label box's top left, by its anchor and position */
	private static double[] labelPlace(Anchor an, String pos, double bw, double bh) {
		double g = LABEL_GAP;
		if (an.bar) {
			// the value's direction along its axis: up a column (y falls), right along a bar
			double dir = Math.signum(an.end - an.base);
			if (!an.across) {
				double x = an.x + an.w / 2 - bw / 2, y;
				boolean up = dir <= 0;
				switch (pos) {
				case "inEnd":
					y = up ? an.end + g : an.end - g - bh;
					break;
				case "inBase":
					y = up ? an.base - g - bh : an.base + g;
					break;
				case "ctr":
					y = (an.end + an.base) / 2 - bh / 2;
					break;
				default: // outEnd
					y = up ? an.end - g - bh : an.end + g;
				}
				return new double[] { x, y };
			}
			double y = an.y + an.h / 2 - bh / 2, x;
			boolean right = dir >= 0;
			switch (pos) {
			case "inEnd":
				x = right ? an.end - g - bw : an.end + g;
				break;
			case "inBase":
				x = right ? an.base + g : an.base - g - bw;
				break;
			case "ctr":
				x = (an.end + an.base) / 2 - bw / 2;
				break;
			default:
				x = right ? an.end + g : an.end - g - bw;
			}
			return new double[] { x, y };
		}
		if (an.slice) {
			double t = Math.toRadians(an.mid), ux = Math.sin(t), uy = -Math.cos(t);
			// how far the box reaches from its centre along the ray
			double reach = Math.min(Math.abs(ux) < 1e-9 ? Double.MAX_VALUE : bw / 2 / Math.abs(ux), Math.abs(uy) < 1e-9 ? Double.MAX_VALUE : bh / 2 / Math.abs(uy));
			double dist;
			switch (pos) {
			case "ctr":
				dist = (an.inner + an.outer) / 2;
				break;
			case "outEnd":
				dist = an.outer + 3.95 + reach;
				break;
			default: // inEnd, and (provisional) bestFit
				dist = an.outer - LABEL_PIE_IN - reach;
			}
			return new double[] { an.x + dist * ux - bw / 2, an.y + dist * uy - bh / 2 };
		}
		if (an.area) {
			return new double[] { an.x - bw / 2, (an.end + an.base) / 2 - bh / 2 };
		}
		double r = an.m + g;
		switch (pos) {
		case "t":
			return new double[] { an.x - bw / 2, an.y - r - bh };
		case "b":
			return new double[] { an.x - bw / 2, an.y + r };
		case "l":
			return new double[] { an.x - r - bw, an.y - bh / 2 };
		case "ctr":
			return new double[] { an.x - bw / 2, an.y - bh / 2 };
		default: // r
			return new double[] { an.x + r, an.y - bh / 2 };
		}
	}

	/** A text body's quarter turn: -1 up the page (rot -5400000), 1 down it (5400000), else 0 */
	private static int rotation(CTTextBody tx) {
		if (tx == null || tx.getBodyPr() == null || tx.getBodyPr().getRot() == null) return 0;
		int rot = tx.getBodyPr().getRot();
		return rot == -5400000 ? -1 : rot == 5400000 ? 1 : 0;
	}

	/** A text body's insets (lIns, tIns, rIns, bIns), Office's chart text's 3pt and 1.5pt where it says none */
	private static double[] insets(CTTextBody tx) {
		double[] out = { 3, 1.5, 3, 1.5 };
		if (tx == null || tx.getBodyPr() == null) return out;
		org.docx4j.dml.CTTextBodyProperties b = tx.getBodyPr();
		if (b.getLIns() != null) out[0] = b.getLIns() / 12700.0;
		if (b.getTIns() != null) out[1] = b.getTIns() / 12700.0;
		if (b.getRIns() != null) out[2] = b.getRIns() / 12700.0;
		if (b.getBIns() != null) out[3] = b.getBIns() / 12700.0;
		return out;
	}

	/** A rich text body's text, its paragraphs on lines */
	private static String richText(CTTextBody rich) {
		StringBuilder b = new StringBuilder();
		for (CTTextParagraph p : rich.getP()) {
			if (b.length() > 0) b.append('\n');
			for (Object r : p.getEGTextRun()) {
				if (r instanceof org.docx4j.dml.CTRegularTextRun) b.append(((org.docx4j.dml.CTRegularTextRun) r).getT());
			}
		}
		return b.toString();
	}

	private static double numberOr(ChartData d, int i) {
		Double v = d == null ? null : d.getNumber(i);
		return v == null ? i + 1 : v;
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
		// placed by hand: the largest square of the box given, centred in it (r15-plot-pie)
		CTManualLayout pm = model.getSource().getChart() == null || model.getSource().getChart().getPlotArea() == null ? null
				: manualLayout(model.getSource().getChart().getPlotArea().getLayout());
		if (pm != null) {
			Rectangle2D b = manual(pm, new Rectangle2D.Double(plotLeft, plotTop, plotRight - plotLeft, plotBottom - plotTop));
			plotLeft = b.getMinX();
			plotTop = b.getMinY();
			plotRight = b.getMaxX();
			plotBottom = b.getMaxY();
		}
		double side = Math.max(0, Math.min(plotRight - plotLeft, plotBottom - plotTop));
		// labels that may go outside take room from the pie (LABEL_PIE_ROOM)
		if (!series.isEmpty()) {
			ChartSeries first = series.get(0);
			for (int i = 0, n = count(first.getValues()); i < n; i++) {
				LabelSpec ls = labelSpec(group, first, i);
				// (a doughnut's labels, wherever they are: r15-labels-doughnut)
				if (ls.shown() && ("outEnd".equals(ls.position) || "bestFit".equals(ls.position) || group.getKind() == ChartGroup.Kind.DOUGHNUT)) {
					side = Math.max(0, side - 2 * (LABEL_PIE_ROOM + LABEL_PIE_SHARE * side));
					break;
				}
			}
		}
		double px = plotLeft + (plotRight - plotLeft - side) / 2, py = plotTop + (plotBottom - plotTop - side) / 2;
		Rectangle2D plot = new Rectangle2D.Double(px, py, side, side);
		d.boxes.put("plotArea", plot);
		d.boxes.put("inside", plot);
		plotAreaFill(plot);
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
				Anchor an = new Anchor(s, i);
				an.slice = true;
				an.x = cx;
				an.y = cy;
				an.inner = inner;
				an.outer = outer;
				an.mid = (a + b) / 2;
				an.value = x;
				an.w = total;
				anchors.add(an);
				Rectangle2D box = new Rectangle2D.Double(at[1][0], at[1][1], 0, 0);
				for (double[] q : new double[][] { at[3], doughnut ? at[7] : at[5], doughnut ? at[9] : at[5] }) box.add(q[0], q[1]);
				d.boxes.put(key, box);
				d.add(area(shape, pointSpPr(sr, i), pointFill(sr, i), key, null));
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
		if (f.getKind() == ChartFill.Kind.NONE) return null;
		if (f.getKind() == ChartFill.Kind.GRADIENT) {
			Drawing.Gradient g = gradient(spPr(s));
			if (g != null) return g.colours[0];
		}
		if (f.getKind() == ChartFill.Kind.PATTERN) {
			Drawing.Pattern p = pattern(spPr(s));
			if (p != null) return p.fg;
		}
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

	/** An axis's major or minor gridlines, or null */
	private static CTChartLines gridlines(Object axis, boolean major) {
		if (axis instanceof CTValAx) return major ? ((CTValAx) axis).getMajorGridlines() : ((CTValAx) axis).getMinorGridlines();
		if (axis instanceof CTCatAx) return major ? ((CTCatAx) axis).getMajorGridlines() : ((CTCatAx) axis).getMinorGridlines();
		if (axis instanceof CTDateAx) return major ? ((CTDateAx) axis).getMajorGridlines() : ((CTDateAx) axis).getMinorGridlines();
		return null;
	}

	/** A gridline, in its line's colour or gradient (Word's PDF: a shading in the stroke, r10-column-style-213) */
	private void gridline(Line2D l, CTChartLines g, String role) {
		Drawing.Area a = line(l, g.getSpPr(), role);
		if (a != null) d.add(a);
	}

	/** A line drawn by a shape's a:ln: its colour or gradient, its width; null where it has none */
	private Drawing.Area line(Shape shape, CTShapeProperties sp, String role) {
		Color c = lineColour(sp);
		Drawing.Gradient gr = sp == null || sp.getLn() == null ? null : gradient(sp.getLn().getGradFill());
		if (c == null && gr == null) return null;
		Drawing.Area a = new Drawing.Area(shape, null, c != null ? c : gr.colours[0], lineWidth(sp), role);
		a.lineGradient = gr;
		a.dash = dashes(sp, a.lineWidth);
		return a;
	}

	/** An axis's major tick marks: out, in or cross; null for none */
	private static String tickMark(Object axis) {
		return tickMark(axis, true);
	}

	/** An axis's major or minor tick marks: out, in or cross; null for none */
	private static String tickMark(Object axis, boolean major) {
		Object o = get(axis, major ? "getMajorTickMark" : "getMinorTickMark");
		if (!(o instanceof CTTickMark) || ((CTTickMark) o).getVal() == null) return null;
		String v = ((CTTickMark) o).getVal().value();
		return "none".equals(v) ? null : v;
	}
}
