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

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import org.docx4j.XmlUtils;
import org.docx4j.dml.chart.CTChart;
import org.docx4j.dml.chart.CTChartSpace;
import org.docx4j.dml.chart.CTPlotArea;
import org.docx4j.mce.AlternateContent;
import org.docx4j.model.color.ResolvedColor;
import org.docx4j.model.color.ThemeColors;
import org.docx4j.openpackaging.exceptions.Docx4JException;
import org.docx4j.openpackaging.parts.DrawingML.Chart;

/**
 * A chart, read: what its part ({@code c:chartSpace}) says, and, where Office derives what it
 * shows, what Office shows, each named as such ({@code getTitle().getShownText()},
 * {@link ChartSeries#getShownName()}, {@link ChartGroup#variesColors()} ...).  It holds the JAXB
 * objects it read ({@code getSource()} throughout), so a caller can always go deeper.
 *
 * <p>Values come from the file's caches, which are what Office draws until the data is edited,
 * and all a docx or pptx guarantees.  Colours are resolved through the chart's theme, its host's
 * unless the chart has an override (CR-034); a series with no fill of its own is
 * {@link ChartFill.Kind#AUTOMATIC automatic}.</p>
 *
 * <p>What the file says is ISO/IEC 29500-1 §21.2; what Office shows starts from Office's own
 * notes on it, [MS-OI29500] 2.1.1428 to 2.1.1599, cited where used.  The design is
 * docs/developer/change-requests/CR-035-charts-box-read-model-describe-edit.md, §4.2.</p>
 *
 * <p>{@link #toJson()} is the model's JSON form, versioned ({@link #JSON_VERSION}), which
 * docx4j's TypeScript and Python ports compare against.</p>
 *
 * @since 17.3.3 (CR-035)
 */
public final class ChartModel {

	/** The version of {@link #toJson()}'s form */
	public static final int JSON_VERSION = 1;

	private final CTChartSpace chartSpace;
	private final Chart part;
	Integer style, c14Style;
	Boolean date1904, roundedCorners;
	String lang;
	ChartTitle title;
	Boolean autoTitleDeleted;
	ChartLegend legend;
	Boolean plotVisOnly;
	String dispBlanksAs;
	Boolean showDLblsOverMax;
	List<ChartGroup> groups;
	List<ChartAxis> axes;

	private ChartModel(CTChartSpace chartSpace, Chart part) {
		this.chartSpace = chartSpace;
		this.part = part;
	}

	/**
	 * A chart part, read in its host's theme.
	 *
	 * @param host the theme and colour map of the part the chart is placed in (a docx's, a
	 *        slide's, a workbook's); null for the chart's package's, as {@link ThemeColors#of(org.docx4j.openpackaging.parts.Part)}
	 *        gives it
	 */
	public static ChartModel read(Chart chart, ThemeColors host) throws Docx4JException {
		ThemeColors theme = host != null ? host : ThemeColors.of(chart);
		return read(chart.getContents(), chart, theme.forChart(chart));
	}

	/** A chart space read with the theme given (a chart override already applied) */
	public static ChartModel read(CTChartSpace cs, ThemeColors theme) {
		return read(cs, null, theme);
	}

	private static ChartModel read(CTChartSpace cs, Chart part, ThemeColors theme) {
		ChartModel m = new ChartModel(cs, part);
		m.date1904 = Vals.bool(cs.getDate1904());
		m.roundedCorners = Vals.bool(cs.getRoundedCorners());
		m.lang = cs.getLang() == null ? null : cs.getLang().getVal();
		readStyle(m, cs);
		CTChart chart = cs.getChart();
		List<ChartGroup> groups = new ArrayList<ChartGroup>();
		List<ChartAxis> axes = new ArrayList<ChartAxis>();
		if (chart != null) {
			m.autoTitleDeleted = Vals.bool(chart.getAutoTitleDeleted());
			m.legend = ChartLegend.of(chart.getLegend());
			m.plotVisOnly = Vals.bool(chart.getPlotVisOnly());
			m.dispBlanksAs = Vals.str(chart.getDispBlanksAs());
			m.showDLblsOverMax = Vals.bool(chart.getShowDLblsOverMax());
			CTPlotArea pa = chart.getPlotArea();
			if (pa != null) {
				for (Object o : pa.getAreaChartOrArea3DChartOrLineChart()) {
					ChartGroup g = ChartGroup.of(o, theme);
					if (g != null) {
						g.applyDispBlanksAs(m.dispBlanksAs);
						groups.add(g);
					}
				}
				for (Object o : pa.getValAxOrCatAxOrDateAx()) {
					ChartAxis a = ChartAxis.of(XmlUtils.unwrap(o));
					if (a != null) axes.add(a);
				}
			}
			m.groups = Collections.unmodifiableList(groups);
			m.axes = Collections.unmodifiableList(axes);
			m.title = ChartTitle.of(chart.getTitle(), defaultTitle(m));
		} else {
			m.groups = Collections.emptyList();
			m.axes = Collections.emptyList();
		}
		return m;
	}

	/**
	 * [MS-OI29500] 2.1.1561 (a): a title with no text shows a localized default ("Chart Title"),
	 * or, where the chart has a single series with a name, that name
	 */
	private static String defaultTitle(ChartModel m) {
		List<ChartSeries> shown = new ArrayList<ChartSeries>();
		for (ChartGroup g : m.groups) shown.addAll(g.getShownSeries());
		if (shown.size() == 1 && shown.get(0).getName() != null && shown.get(0).getName().getText(0) != null) {
			return shown.get(0).getName().getText(0);
		}
		return "Chart Title";
	}

	/** {@code c14:style} (in an mc:AlternateContent Choice) and {@code c:style} (beside it or its Fallback) */
	private static void readStyle(ChartModel m, CTChartSpace cs) {
		if (cs.getStyle() != null) m.style = (int) cs.getStyle().getVal();
		AlternateContent ac = cs.getAlternateContent();
		if (ac == null) return;
		for (AlternateContent.Choice c : ac.getChoice()) {
			for (Object o : c.getAny()) {
				Object u = XmlUtils.unwrap(o);
				if (u instanceof org.docx4j.dml.chart.x2007.CTStyle) m.c14Style = (int) ((org.docx4j.dml.chart.x2007.CTStyle) u).getVal();
			}
		}
		if (m.style == null && ac.getFallback() != null) {
			for (Object o : ac.getFallback().getAny()) {
				Object u = XmlUtils.unwrap(o);
				if (u instanceof org.docx4j.dml.chart.CTStyle) m.style = (int) ((org.docx4j.dml.chart.CTStyle) u).getVal();
			}
		}
		// [MS-ODRAWXML] 2.7.3.5: c:style is c14:style less 100
		if (m.style == null && m.c14Style != null) m.style = m.c14Style - 100;
	}

	// ------------------------------------------------------------------ what it holds

	/**
	 * The chart's style number, 1 to 48: {@code c:style}, or {@code c14:style} less 100 where only
	 * that is given; null for none
	 */
	public Integer getStyle() {
		return style;
	}

	/** {@code c14:style} (101 to 148, which Office prefers to {@code c:style}); null for none */
	public Integer getC14Style() {
		return c14Style;
	}

	/** {@code c:date1904} */
	public Boolean getDate1904() {
		return date1904;
	}

	/** {@code c:roundedCorners} */
	public Boolean getRoundedCorners() {
		return roundedCorners;
	}

	/** {@code c:lang} */
	public String getLang() {
		return lang;
	}

	/** The chart's title; null where the chart has no {@code c:title} */
	public ChartTitle getTitle() {
		return title;
	}

	/** {@code c:autoTitleDeleted}: applies only where the chart has no title ([MS-OI29500] 2.1.1431) */
	public Boolean getAutoTitleDeleted() {
		return autoTitleDeleted;
	}

	/** The legend; null for none */
	public ChartLegend getLegend() {
		return legend;
	}

	/** {@code c:plotVisOnly}: only visible cells are plotted */
	public Boolean getPlotVisOnly() {
		return plotVisOnly;
	}

	/** {@code c:dispBlanksAs} as stated; each group's {@link ChartGroup#getShownDispBlanksAs()} is what Office does */
	public String getDispBlanksAs() {
		return dispBlanksAs;
	}

	/** {@code c:showDLblsOverMax} */
	public Boolean getShowDLblsOverMax() {
		return showDLblsOverMax;
	}

	/** The chart groups, in document order: more than one in a combination chart */
	public List<ChartGroup> getGroups() {
		return groups;
	}

	/** The axes, in document order */
	public List<ChartAxis> getAxes() {
		return axes;
	}

	/** The axis with an id; null for none */
	public ChartAxis getAxis(long id) {
		for (ChartAxis a : axes) if (a.getId() == id) return a;
		return null;
	}

	/** The chart part; null where read from a chart space alone */
	public Chart getPart() {
		return part;
	}

	public CTChartSpace getSource() {
		return chartSpace;
	}

	// ------------------------------------------------------------------ JSON

	/**
	 * The model as JSON, its form versioned ({@link #JSON_VERSION}) and its members in a fixed
	 * order, so that two readings of one chart give the same text: the ports' oracle.
	 */
	public String toJson() {
		Json j = new Json();
		j.open(null);
		j.put("version", JSON_VERSION);
		j.put("style", style);
		j.put("c14Style", c14Style);
		j.put("date1904", date1904);
		j.put("roundedCorners", roundedCorners);
		j.put("lang", lang);
		title(j, "title", title);
		j.put("autoTitleDeleted", autoTitleDeleted);
		if (legend == null) {
			j.nul("legend");
		} else {
			j.open("legend");
			j.put("position", legend.getPosition());
			j.put("overlay", legend.isOverlay());
			j.openArray("deletedEntries");
			for (Long e : legend.getDeletedEntries()) j.put(null, e);
			j.closeArray();
			j.close();
		}
		j.put("plotVisOnly", plotVisOnly);
		j.put("dispBlanksAs", dispBlanksAs);
		j.put("showDLblsOverMax", showDLblsOverMax);
		j.openArray("groups");
		for (ChartGroup g : groups) group(j, g);
		j.closeArray();
		j.openArray("axes");
		for (ChartAxis a : axes) axis(j, a);
		j.closeArray();
		j.close();
		return j.toString();
	}

	private static void title(Json j, String name, ChartTitle t) {
		if (t == null) {
			j.nul(name);
			return;
		}
		j.open(name);
		j.put("text", t.getText());
		data(j, "reference", t.getReference());
		j.put("shownText", t.getShownText());
		j.put("overlay", t.isOverlay());
		j.close();
	}

	private static void group(Json j, ChartGroup g) {
		j.open(null);
		j.put("kind", g.getKind().value());
		j.put("barDirection", g.getBarDirection());
		j.put("grouping", g.getGrouping());
		j.put("varyColors", g.getVaryColors());
		j.put("variesColors", g.variesColors());
		j.put("shownDispBlanksAs", g.getShownDispBlanksAs());
		j.open("settings");
		for (Map.Entry<String, String> e : g.getSettings().entrySet()) {
			if ("true".equals(e.getValue()) || "false".equals(e.getValue())) j.put(e.getKey(), Boolean.valueOf(e.getValue()));
			else j.rawNumber(e.getKey(), e.getValue());
		}
		j.close();
		j.openArray("axisIds");
		for (Long id : g.getAxisIds()) j.put(null, id);
		j.closeArray();
		labels(j, "dataLabels", g.getDataLabels());
		j.openArray("series");
		for (ChartSeries s : g.getSeries()) series(j, s);
		j.closeArray();
		j.close();
	}

	private static void series(Json j, ChartSeries s) {
		j.open(null);
		j.put("idx", s.getIdx());
		j.put("order", s.getOrder());
		j.put("hidden", s.isHidden());
		data(j, "name", s.getName());
		j.put("shownName", s.getShownName());
		j.put("uniqueId", s.getUniqueId());
		fill(j, "fill", s.getFill());
		fill(j, "line", s.getLine());
		data(j, "categories", s.getCategories());
		data(j, "values", s.getValues());
		data(j, "xValues", s.getXValues());
		data(j, "yValues", s.getYValues());
		data(j, "bubbleSizes", s.getBubbleSizes());
		j.openArray("points");
		for (Map.Entry<Integer, ChartFill> e : s.getPointFills().entrySet()) {
			j.open(null);
			j.put("idx", e.getKey());
			fill(j, "fill", e.getValue());
			j.close();
		}
		j.closeArray();
		j.put("smooth", s.getSmooth());
		j.put("invertIfNegative", s.getInvertIfNegative());
		j.put("explosion", s.getExplosion());
		j.put("marker", s.getMarker());
		labels(j, "dataLabels", s.getDataLabels());
		j.close();
	}

	private static void data(Json j, String name, ChartData d) {
		if (d == null) {
			j.nul(name);
			return;
		}
		j.open(name);
		j.put("kind", kind(d.getKind()));
		j.put("reference", d.getReference());
		j.put("fullReference", d.getFullReference());
		j.put("count", d.getCount());
		j.put("formatCode", d.getFormatCode());
		int n = d.size();
		if (d.getLevels().size() > 1) {
			j.openArray("levels");
			for (Map<Integer, String> level : d.getLevels()) {
				j.openArray(null);
				for (int i = 0; i < n; i++) j.put(null, level.get(i));
				j.closeArray();
			}
			j.closeArray();
		} else {
			j.openArray("points");
			for (int i = 0; i < n; i++) {
				if (d.isNumeric()) j.rawNumber(null, d.getText(i));
				else j.put(null, d.getText(i));
			}
			j.closeArray();
		}
		j.close();
	}

	private static String kind(ChartData.Kind k) {
		switch (k) {
			case NUMBER_REFERENCE: return "numberReference";
			case NUMBER_LITERAL: return "numberLiteral";
			case STRING_REFERENCE: return "stringReference";
			case STRING_LITERAL: return "stringLiteral";
			case MULTI_LEVEL_REFERENCE: return "multiLevelReference";
			default: return "text";
		}
	}

	private static void fill(Json j, String name, ChartFill f) {
		j.open(name);
		j.put("kind", f.getKind().name().toLowerCase());
		ResolvedColor c = f.getColor();
		if (c != null) {
			j.put("color", c.toHex());
			if (!c.isOpaque()) j.put("alpha", c.getAlpha());
		}
		j.close();
	}

	private static void labels(Json j, String name, ChartDataLabels l) {
		if (l == null) {
			j.nul(name);
			return;
		}
		j.open(name);
		j.put("deleted", l.getDeleted());
		j.put("showValue", l.getShowValue());
		j.put("showCategoryName", l.getShowCategoryName());
		j.put("showSeriesName", l.getShowSeriesName());
		j.put("showPercent", l.getShowPercent());
		j.put("showBubbleSize", l.getShowBubbleSize());
		j.put("showLegendKey", l.getShowLegendKey());
		j.put("position", l.getPosition());
		j.put("shownPosition", l.getShownPosition());
		j.put("numberFormat", l.getNumberFormat());
		j.put("separator", l.getSeparator());
		j.close();
	}

	private static void axis(Json j, ChartAxis a) {
		j.open(null);
		j.put("id", a.getId());
		j.put("kind", a.getKind().name().toLowerCase());
		j.put("crossAxisId", a.getCrossAxisId());
		j.put("position", a.getPosition());
		j.put("deleted", a.isDeleted());
		j.put("orientation", a.getOrientation());
		j.put("min", a.getMin());
		j.put("max", a.getMax());
		j.put("logBase", a.getLogBase());
		j.put("majorUnit", a.getMajorUnit());
		j.put("minorUnit", a.getMinorUnit());
		j.put("baseTimeUnit", a.getBaseTimeUnit());
		j.put("numberFormat", a.getNumberFormat());
		j.put("numberFormatSourceLinked", a.getNumberFormatSourceLinked());
		j.put("crosses", a.getCrosses());
		j.put("crossesAt", a.getCrossesAt());
		j.put("crossBetween", a.getCrossBetween());
		j.put("majorTickMark", a.getMajorTickMark());
		j.put("minorTickMark", a.getMinorTickMark());
		j.put("tickLabelPosition", a.getTickLabelPosition());
		j.put("majorGridlines", a.hasMajorGridlines());
		j.put("minorGridlines", a.hasMinorGridlines());
		title(j, "title", a.getTitle());
		j.close();
	}
}
