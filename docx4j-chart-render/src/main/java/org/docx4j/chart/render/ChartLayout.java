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
import java.awt.font.FontRenderContext;
import java.awt.font.LineMetrics;
import java.awt.geom.Line2D;
import java.awt.geom.Rectangle2D;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import org.docx4j.dml.CTLineProperties;
import org.docx4j.dml.CTShapeProperties;
import org.docx4j.dml.CTTextBody;
import org.docx4j.dml.CTTextCharacterProperties;
import org.docx4j.dml.CTTextParagraph;
import org.docx4j.dml.chart.CTCatAx;
import org.docx4j.dml.chart.CTChartLines;
import org.docx4j.dml.chart.CTChartSpace;
import org.docx4j.dml.chart.CTDateAx;
import org.docx4j.dml.chart.CTLegend;
import org.docx4j.dml.chart.CTTitle;
import org.docx4j.dml.chart.CTValAx;
import org.docx4j.model.chart.ChartAxis;
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
 * A chart laid out as Office lays it out (CR-037 phase 1): the clustered column's frame, by the
 * rules measured on Excel's layout of the render probes (§11).  Office reports a chart's
 * elements from an origin {@link #INSET} inside the chart's box; the constants are in that
 * frame unless they say otherwise.
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
	/** A legend key's side, a share of the legend's line; the key is inset half its side */
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
	/** The widest value label's left edge is this far left of the plot area's (6.53pt from the chart's edge) */
	static final double VALUE_LABEL_OVERHANG = 4.47;
	/** Between a value label's right edge and the plot's inside, a share of the label's line */
	static final double VALUE_LABEL_GAP = 0.7575;
	/** The top value label may stand this far above the plot area before the inside drops */
	static final double VALUE_LABEL_TOP = 6;

	private static final FontRenderContext FRC = new FontRenderContext(null, true, true);
	private static final double SIN45 = Math.sqrt(0.5);

	/**
	 * A text style: a chart's c:txPr, inherited from the chart space's, else Office's default.
	 * Text at or above its kern size (DrawingML's {@code kern}, hundredths of a point; Office's
	 * charts write 1200) is kerned, as Office kerns it (R4's long title: its width is Carlito's
	 * kerned advances, to 0.01pt)
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

	private final ChartModel model;
	private final ThemeColors theme;
	private final ChartFonts fonts;
	private final double w, h;
	private final Drawing d;
	private final DataFormatter formatter = new DataFormatter(Locale.US);

	private ChartLayout(ChartModel model, ThemeColors theme, ChartFonts fonts, double w, double h) {
		this.model = model;
		this.theme = theme;
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

	/** A shape's fill: solid only (phase 1); null for none */
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

	// ------------------------------------------------------------------ the layout

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
		List<ChartSeries> series = group.getShownSeries();
		double plotLeft = ix + PLOT_EDGE, plotRight = ix + iw - PLOT_EDGE, plotBottom = iy + ih - PLOT_EDGE;
		List<Drawing.Area> keys = new ArrayList<>();
		if (model.getLegend() != null && !series.isEmpty()) {
			CTLegend lg = model.getLegend().getSource();
			Style s = style(lg.getTxPr(), base());
			String pos = model.getLegend().getPosition() == null ? "r" : model.getLegend().getPosition();
			double key = LEGEND_KEY * s.line;
			double rowH = s.line + 5.97 - 0.0102 * s.size;
			List<String> names = new ArrayList<>();
			List<Double> widths = new ArrayList<>();
			double maxW = 0;
			for (ChartSeries sr : series) {
				String n = sr.getShownName() == null ? "" : sr.getShownName();
				names.add(n);
				double ew = 2 * key + s.width(n);
				widths.add(ew);
				maxW = Math.max(maxW, ew);
			}
			boolean horizontal = "b".equals(pos) || "t".equals(pos);
			double lx, ly, lw, lh;
			List<double[]> at = new ArrayList<>();
			if (horizontal) {
				int k = widths.size();
				double extra = 0;
				for (double ew : widths) extra += LEGEND_SLOT * ew;
				double pad = extra / (k + 1);
				lw = extra;
				for (double ew : widths) lw += ew;
				lh = rowH;
				lx = ix + (iw - lw) / 2;
				ly = "b".equals(pos) ? iy + ih - LEGEND_EDGE - lh : plotTop;
				double x = lx + pad;
				for (double ew : widths) {
					at.add(new double[] { x, ly + LEGEND_TOP, ew });
					x += ew + pad;
				}
				if ("b".equals(pos)) plotBottom = ly - GAP;
				else plotTop = ly + lh + GAP;
			} else {
				lw = maxW + 3 + 2 * LEGEND_SIDE;
				lh = series.size() * rowH;
				lx = "l".equals(pos) ? ix + LEGEND_EDGE : ix + iw - LEGEND_EDGE - lw;
				// centred on the plot area
				ly = (plotTop + plotBottom) / 2 - lh / 2;
				for (int i = 0; i < series.size(); i++) at.add(new double[] { lx + LEGEND_SIDE, ly + LEGEND_TOP + i * rowH, maxW + 3 });
				if ("l".equals(pos)) plotLeft = lx + lw + GAP;
				else plotRight = lx - GAP;
			}
			d.boxes.put("legend", new Rectangle2D.Double(lx, ly, lw, lh));
			for (int i = 0; i < series.size(); i++) {
				double[] e = at.get(i);
				d.boxes.put("legend.entry." + i, new Rectangle2D.Double(e[0], e[1], e[2], s.line));
				d.boxes.put("legend.key." + i, new Rectangle2D.Double(e[0] + key / 2, e[1] + key / 2, key, key));
				// drawn 0.75pt right and 0.055 of the text's size down of the box Office reports for it
				keys.add(new Drawing.Area(new Rectangle2D.Double(e[0] + key / 2 + 0.75, e[1] + key / 2 + 0.055 * s.size, key, key),
						seriesFill(series.get(i)), null, 0, "legend.key." + i));
				double tx = e[0] + key / 2 + key + 2.94 * s.line / 10.986;
				texts.add(new Drawing.Text(names.get(i), fonts.face(s.typeface), s.font, s.colour, tx, e[1] + s.ascent * 0.956, s.width(names.get(i)), 0,
						"legend.text." + i));
			}
		}
		d.boxes.put("plotArea", new Rectangle2D.Double(plotLeft, plotTop, plotRight - plotLeft, plotBottom - plotTop));

		// the axes
		ChartAxis catAx = null, valAx = null;
		for (ChartAxis a : model.getAxes()) {
			if (!group.getAxisIds().contains(a.getId())) continue;
			if (a.getKind() == ChartAxis.Kind.VALUE && valAx == null) valAx = a;
			else if (a.getKind() == ChartAxis.Kind.CATEGORY || a.getKind() == ChartAxis.Kind.DATE) catAx = a;
		}
		boolean valLabels = valAx != null && !valAx.isDeleted() && !"none".equals(valAx.getTickLabelPosition());
		boolean catLabels = catAx != null && !catAx.isDeleted() && !"none".equals(catAx.getTickLabelPosition());
		Style vs = style(valAx == null ? null : txPr(valAx.getSource()), base());
		Style cst = style(catAx == null ? null : txPr(catAx.getSource()), base());

		double insideTop = plotTop + (valLabels ? Math.max(0, vs.line / 2 - VALUE_LABEL_TOP) : 0);

		// the scale, chosen with one line of category labels
		double dmin = Double.POSITIVE_INFINITY, dmax = Double.NEGATIVE_INFINITY;
		int nCat = 0;
		for (ChartSeries sr : series) {
			ChartData v = sr.getValues();
			if (v == null) continue;
			nCat = Math.max(nCat, (int) (v.getCount() == null ? v.size() : v.getCount()));
			for (int i = 0; i < nCat; i++) {
				Double x = v.getNumber(i);
				if (x == null) continue;
				dmin = Math.min(dmin, x);
				dmax = Math.max(dmax, x);
			}
		}
		if (dmin == Double.POSITIVE_INFINITY) dmin = dmax = 0;
		// the category labels take a band at the plot's bottom only where their axis is there:
		// below it they hang from the axis's line wherever it crosses (R2)
		boolean bottomAxis = dmin >= 0 || (valAx != null && valAx.getMin() != null && valAx.getMin() >= 0);
		double band1 = catLabels && bottomAxis ? band(cst, 1) : 0;
		// (a maximum set can take the scale below zero, so this is decided again with the scale)
		AxisScale scale = AxisScale.of(dmin, dmax, valAx == null ? null : valAx.getMin(), valAx == null ? null : valAx.getMax(),
				valAx == null ? null : valAx.getMajorUnit(), valAx == null ? null : valAx.getLogBase(),
				plotBottom - band1 - insideTop, vs.line);
		d.valueAxis = new double[] { scale.min, scale.max, scale.unit };
		if (!scale.log && scale.min < 0) bottomAxis = false;

		// the value labels, and the inside's left
		List<Double> values = scale.values();
		List<String> valueTexts = new ArrayList<>();
		double maxLabel = 0;
		if (valLabels) {
			String code = numberFormat(valAx, series);
			for (double v : values) {
				String t = format(v, code);
				valueTexts.add(t);
				maxLabel = Math.max(maxLabel, vs.width(t));
			}
		}
		double insideLeft = valLabels ? plotLeft - VALUE_LABEL_OVERHANG + maxLabel + VALUE_LABEL_GAP * vs.line : plotLeft;
		double insideRight = plotRight;
		double catW = nCat == 0 ? 0 : (insideRight - insideLeft) / nCat;

		// the category labels: one line, else wrapped at the category's width, else turned 45 degrees
		List<String> catTexts = new ArrayList<>();
		if (catLabels && !series.isEmpty()) {
			ChartData cats = series.get(0).getCategories();
			for (int i = 0; i < nCat; i++) catTexts.add(categoryText(cats, i, catAx));
		}
		List<List<String>> catLines = new ArrayList<>();
		boolean turned = false;
		int lines = 1;
		double catMax = 0;
		for (String t : catTexts) catMax = Math.max(catMax, cst.width(t));
		if (catLabels && catMax > catW) {
			boolean wraps = true;
			for (String t : catTexts) {
				for (String word : t.split(" ")) if (cst.width(word) > catW) wraps = false;
			}
			turned = !wraps;
		}
		for (String t : catTexts) {
			List<String> l = turned || cst.width(t) <= catW ? java.util.Collections.singletonList(t) : wrap(t, cst, catW);
			catLines.add(l);
			lines = Math.max(lines, l.size());
		}
		double band = !catLabels || !bottomAxis ? 0 : turned ? (catMax + cst.line) * SIN45 + 0.634 * cst.size - 4.5 : band(cst, lines);
		double insideBottom = plotBottom - band;
		double insideH = insideBottom - insideTop;
		d.boxes.put("inside", new Rectangle2D.Double(insideLeft, insideTop, insideRight - insideLeft, insideH));

		// gridlines and the category axis's line, at the crossing
		double cross = scale.log ? scale.min : Math.max(scale.min, Math.min(scale.max, 0));
		double crossY = insideBottom - scale.fraction(cross) * insideH;
		CTChartLines grid = valAx == null ? null : gridlines(valAx.getSource());
		CTShapeProperties catSp = catAx == null ? null : spPr(catAx.getSource());
		boolean axisLine = catAx != null && !catAx.isDeleted() && lineColour(catSp) != null;
		if (grid != null) {
			Color gc = lineColour(grid.getSpPr());
			if (gc != null) {
				for (double v : values) {
					double y = insideBottom - scale.fraction(v) * insideH;
					if (axisLine && Math.abs(y - crossY) < 0.01) continue;
					d.add(new Drawing.Area(new Line2D.Double(insideLeft, y, insideRight, y), null, gc, lineWidth(grid.getSpPr()), "gridline"));
				}
			}
		}

		// the bars
		double gapWidth = number(group.getSettings().get("gapWidth"), 150) / 100.0;
		double overlap = number(group.getSettings().get("overlap"), 0) / 100.0;
		int n = series.size();
		double barW = catW / (n - (n - 1) * overlap + gapWidth);
		for (int s = 0; s < n; s++) {
			ChartSeries sr = series.get(s);
			Color c = seriesFill(sr);
			for (int i = 0; i < nCat; i++) {
				Double v = sr.getValues() == null ? null : sr.getValues().getNumber(i);
				if (v == null && "zero".equals(group.getShownDispBlanksAs())) v = 0.0;
				if (v == null) continue;
				double x = insideLeft + i * catW + gapWidth * barW / 2 + s * barW * (1 - overlap);
				double y = insideBottom - scale.fraction(v) * insideH;
				double top = Math.min(y, crossY), bottom = Math.max(y, crossY);
				Rectangle2D r = new Rectangle2D.Double(x, top, barW, bottom - top);
				d.boxes.put("point." + s + "." + i, r);
				// clipped to the plot's inside, as Office clips a bar past the axis's bounds (R2)
				if (bottom - top > 0) d.add(new Drawing.Area(r, c, null, 0, "point." + s + "." + i, d.boxes.get("inside")));
			}
		}
		if (axisLine) {
			d.add(new Drawing.Area(new Line2D.Double(insideLeft, crossY, insideRight, crossY), null, lineColour(catSp), lineWidth(catSp),
					"axis.category"));
		}
		for (Drawing.Area k : keys) d.add(k);

		// the value labels, right-aligned, centred on their gridlines
		for (int k = 0; k < valueTexts.size(); k++) {
			String t = valueTexts.get(k);
			double tw = vs.width(t);
			double y = insideBottom - scale.fraction(values.get(k)) * insideH;
			d.add(new Drawing.Text(t, fonts.face(vs.typeface), vs.font, vs.colour, insideLeft - VALUE_LABEL_GAP * vs.line - tw, y + 0.289 * vs.size + 0.2, tw, 0,
					"value." + k));
		}
		// the category labels, centred under their categories
		for (int i = 0; i < catLines.size(); i++) {
			double cx = insideLeft + (i + 0.5) * catW;
			List<String> l = catLines.get(i);
			for (int j = 0; j < l.size(); j++) {
				String t = l.get(j);
				double tw = cst.width(t);
				if (turned) {
					// the label's end at the category's centre, turned up 45 degrees
					double top = crossY + 0.61 * cst.size + 0.45;
					double ex = cx + cst.ascent * SIN45 / 2, ey = top + cst.ascent * SIN45 / 2;
					d.add(new Drawing.Text(t, fonts.face(cst.typeface), cst.font, cst.colour, ex - tw * SIN45, ey + tw * SIN45, tw, -45, "category." + i));
				} else {
					double base = crossY + 0.61 * cst.size + 0.45 + cst.ascent + j * cst.line;
					d.add(new Drawing.Text(t, fonts.face(cst.typeface), cst.font, cst.colour, cx - tw / 2, base, tw, 0, "category." + i));
				}
			}
		}
		for (Drawing.Text t : texts) d.add(t);

		// the chart area's border, last (as Word draws it)
		Color border = lineColour(cs.getSpPr());
		if (border != null) {
			double lw = lineWidth(cs.getSpPr());
			d.add(new Drawing.Area(new Rectangle2D.Double(0, 0, w, h), null, border, lw, "chartArea.border"));
		}
	}

	/** The height of the category labels' band, lines high (finding: 1.855 x size - 4.5 for one line) */
	private static double band(Style s, int lines) {
		return lines * s.line + 0.634 * s.size - 4.5;
	}

	private Color seriesFill(ChartSeries s) {
		ChartFill f = s.getFill();
		if (f != null && f.getColor() != null) return awt(f.getColor());
		f = s.getPointFills().get(0);
		if (f != null && f.getColor() != null) return awt(f.getColor());
		return Color.GRAY;
	}

	private static double number(String s, double dflt) {
		if (s == null) return dflt;
		try {
			return Double.parseDouble(s.replace("%", "").trim());
		} catch (NumberFormatException e) {
			return dflt;
		}
	}

	/** The value axis's number format: its own, or (linked to the source) the first series' values' */
	private static String numberFormat(ChartAxis ax, List<ChartSeries> series) {
		if (ax.getNumberFormat() != null && !Boolean.TRUE.equals(ax.getNumberFormatSourceLinked())) return ax.getNumberFormat();
		for (ChartSeries s : series) {
			if (s.getValues() != null && s.getValues().getFormatCode() != null) return s.getValues().getFormatCode();
		}
		return ax.getNumberFormat() == null ? "General" : ax.getNumberFormat();
	}

	private String format(double v, String code) {
		if (code == null || code.isEmpty()) code = "General";
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
		String code = ax.getNumberFormat() != null && !Boolean.TRUE.equals(ax.getNumberFormatSourceLinked()) ? ax.getNumberFormat()
				: cats.getFormatCode(i);
		return format(v, code);
	}

	/** Words wrapped to lines no wider than max (a word wider than max on a line of its own) */
	static List<String> wrap(String text, Style s, double max) {
		List<String> out = new ArrayList<>();
		String line = null;
		for (String word : text.split(" ")) {
			String next = line == null ? word : line + " " + word;
			if (line != null && s.width(next) > max) {
				out.add(line);
				line = word;
			} else {
				line = next;
			}
		}
		if (line != null) out.add(line);
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
}
