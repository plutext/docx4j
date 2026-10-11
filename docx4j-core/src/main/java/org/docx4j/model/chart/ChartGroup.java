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
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.docx4j.XmlUtils;
import org.docx4j.dml.chart.CTArea3DChart;
import org.docx4j.dml.chart.CTAreaChart;
import org.docx4j.dml.chart.CTBar3DChart;
import org.docx4j.dml.chart.CTBarChart;
import org.docx4j.dml.chart.CTBoolean;
import org.docx4j.dml.chart.CTBubbleChart;
import org.docx4j.dml.chart.CTDLbls;
import org.docx4j.dml.chart.CTDoughnutChart;
import org.docx4j.dml.chart.CTExtension;
import org.docx4j.dml.chart.CTExtensionList;
import org.docx4j.dml.chart.CTLine3DChart;
import org.docx4j.dml.chart.CTLineChart;
import org.docx4j.dml.chart.CTOfPieChart;
import org.docx4j.dml.chart.CTPie3DChart;
import org.docx4j.dml.chart.CTPieChart;
import org.docx4j.dml.chart.CTRadarChart;
import org.docx4j.dml.chart.CTScatterChart;
import org.docx4j.dml.chart.CTStockChart;
import org.docx4j.dml.chart.CTSurface3DChart;
import org.docx4j.dml.chart.CTSurfaceChart;
import org.docx4j.dml.chart.CTUnsignedInt;
import org.docx4j.model.color.ThemeColors;

/**
 * A chart group: one {@code c:barChart}, {@code c:lineChart} ... of a plot area, with its series
 * and the axes it is drawn against.  A chart with several groups is a combination chart; groups
 * on different axes put a series on a secondary axis.
 *
 * <p>What Office makes of a group, beside what it states ([MS-OI29500]): {@link #variesColors()}
 * ({@code varyColors} is ignored on area, 3-D area and stock charts, on filled radar charts, and
 * wherever the group has more than one series, 2.1.1571); {@link #getShownSeries()} (a pie chart
 * shows its first series only, 2.1.1519); a scatter chart's style as Office reads it
 * ({@link #getSettings()}, {@code scatterStyle}: smooth is read as smoothMarker, line and marker
 * as lineMarker, 2.1.1594).</p>
 *
 * @since 17.3.3 (CR-035)
 */
public final class ChartGroup {

	public enum Kind {
		AREA("area"), AREA_3D("area3D"), BAR("bar"), BAR_3D("bar3D"), BUBBLE("bubble"), DOUGHNUT("doughnut"),
		LINE("line"), LINE_3D("line3D"), OF_PIE("ofPie"), PIE("pie"), PIE_3D("pie3D"), RADAR("radar"),
		SCATTER("scatter"), STOCK("stock"), SURFACE("surface"), SURFACE_3D("surface3D");

		private final String name;

		Kind(String name) {
			this.name = name;
		}

		/** The element's name less "Chart": bar, line3D, ofPie ... */
		public String value() {
			return name;
		}

		static Kind of(Object group) {
			if (group instanceof CTAreaChart) return AREA;
			if (group instanceof CTArea3DChart) return AREA_3D;
			if (group instanceof CTBarChart) return BAR;
			if (group instanceof CTBar3DChart) return BAR_3D;
			if (group instanceof CTBubbleChart) return BUBBLE;
			if (group instanceof CTDoughnutChart) return DOUGHNUT;
			if (group instanceof CTLineChart) return LINE;
			if (group instanceof CTLine3DChart) return LINE_3D;
			if (group instanceof CTOfPieChart) return OF_PIE;
			if (group instanceof CTPieChart) return PIE;
			if (group instanceof CTPie3DChart) return PIE_3D;
			if (group instanceof CTRadarChart) return RADAR;
			if (group instanceof CTScatterChart) return SCATTER;
			if (group instanceof CTStockChart) return STOCK;
			if (group instanceof CTSurfaceChart) return SURFACE;
			if (group instanceof CTSurface3DChart) return SURFACE_3D;
			return null;
		}
	}

	/** The settings a group of some kind has, read where present: the element's name, its getter */
	private static final String[][] SETTINGS = {
			{ "gapWidth", "getGapWidth" }, { "overlap", "getOverlap" }, { "gapDepth", "getGapDepth" },
			{ "shape", "getShape" }, { "holeSize", "getHoleSize" }, { "firstSliceAng", "getFirstSliceAng" },
			{ "ofPieType", "getOfPieType" }, { "splitType", "getSplitType" }, { "splitPos", "getSplitPos" },
			{ "secondPieSize", "getSecondPieSize" }, { "scatterStyle", "getScatterStyle" },
			{ "radarStyle", "getRadarStyle" }, { "bubbleScale", "getBubbleScale" },
			{ "showNegBubbles", "getShowNegBubbles" }, { "sizeRepresents", "getSizeRepresents" },
			{ "wireframe", "getWireframe" }, { "marker", "getMarker" }, { "smooth", "getSmooth" },
	};

	private final Kind kind;
	private final Object source;
	String barDirection;
	String grouping;
	Boolean varyColors;
	Map<String, String> settings;
	List<Long> axisIds;
	List<ChartSeries> series;
	ChartDataLabels dataLabels;
	String shownDispBlanksAs;

	private ChartGroup(Kind kind, Object source) {
		this.kind = kind;
		this.source = source;
	}

	/** A group from its JAXB object; null for anything else */
	static ChartGroup of(Object o, ThemeColors theme) {
		Object g = XmlUtils.unwrap(o);
		Kind kind = Kind.of(g);
		if (kind == null) return null;
		ChartGroup cg = new ChartGroup(kind, g);
		cg.barDirection = Vals.str(ChartSeries.get(g, "getBarDir"));
		cg.grouping = Vals.str(ChartSeries.get(g, "getGrouping"));
		cg.varyColors = Vals.bool((CTBoolean) ChartSeries.get(g, "getVaryColors"));
		Map<String, String> settings = new LinkedHashMap<String, String>();
		for (String[] s : SETTINGS) {
			Object v = ChartSeries.get(g, s[1]);
			if (v == null) continue;
			String val = v instanceof CTBoolean ? String.valueOf(((CTBoolean) v).isVal()) : Vals.str(v);
			if (val == null) continue;
			if (s[0].equals("scatterStyle")) {
				// [MS-OI29500] 2.1.1594: Office reads smooth as smoothMarker, line and marker as lineMarker
				if (val.equals("smooth")) val = "smoothMarker";
				else if (val.equals("line") || val.equals("marker")) val = "lineMarker";
			}
			settings.put(s[0], val);
		}
		cg.settings = Collections.unmodifiableMap(settings);
		List<Long> ids = new ArrayList<Long>();
		@SuppressWarnings("unchecked")
		List<CTUnsignedInt> axIds = (List<CTUnsignedInt>) ChartSeries.get(g, "getAxId");
		if (axIds != null) for (CTUnsignedInt id : axIds) ids.add(id.getVal());
		cg.axisIds = Collections.unmodifiableList(ids);
		CTDLbls groupLabels = (CTDLbls) ChartSeries.get(g, "getDLbls");
		cg.dataLabels = ChartDataLabels.of(groupLabels, null, kind, cg.grouping);
		List<ChartSeries> series = new ArrayList<ChartSeries>();
		@SuppressWarnings("unchecked")
		List<Object> sers = (List<Object>) ChartSeries.get(g, "getSer");
		if (sers != null) {
			for (Object s : sers) series.add(ChartSeries.of(s, false, groupLabels, kind, cg.grouping, theme));
		}
		// the series filtered out with Excel's chart filter, in the group's extLst
		CTExtensionList extLst = (CTExtensionList) ChartSeries.get(g, "getExtLst");
		if (extLst != null) {
			for (CTExtension e : extLst.getExt()) {
				for (Object a : e.getAny()) {
					Object u = XmlUtils.unwrap(a);
					if (u != null && u.getClass().getSimpleName().startsWith("CTFiltered")
							&& u.getClass().getSimpleName().endsWith("Ser")) {
						Object s = ChartSeries.get(u, "getSer");
						if (s != null) series.add(ChartSeries.of(s, true, groupLabels, kind, cg.grouping, theme));
					}
				}
			}
		}
		series.sort((a, b) -> Long.compare(a.order, b.order));
		cg.series = Collections.unmodifiableList(series);
		return cg;
	}

	public Kind getKind() {
		return kind;
	}

	/** {@code c:barDir}: bar (horizontal) or col; bar charts only */
	public String getBarDirection() {
		return barDirection;
	}

	/**
	 * {@code c:grouping}: for bar charts clustered, stacked, percentStacked or standard (which
	 * Office draws as clustered in 2-D, [MS-OI29500] 2.1.1581); for line and area charts standard,
	 * stacked or percentStacked
	 */
	public String getGrouping() {
		return grouping;
	}

	/** {@code c:varyColors} as stated; null where not */
	public Boolean getVaryColors() {
		return varyColors;
	}

	/**
	 * Whether Office varies the colours point by point: {@code varyColors} stated true, on a kind
	 * that honours it, with a single series shown
	 */
	public boolean variesColors() {
		if (varyColors == null || !varyColors) return false;
		if (kind == Kind.AREA || kind == Kind.AREA_3D || kind == Kind.STOCK) return false;
		if (kind == Kind.RADAR && "filled".equals(settings.get("radarStyle"))) return false;
		return getShownSeries().size() <= 1;
	}

	/**
	 * The kind's own settings that the file states, by element name (gapWidth, overlap, holeSize,
	 * firstSliceAng, scatterStyle, radarStyle, ofPieType, ...), as written but for scatterStyle,
	 * which is as Office reads it.  A line chart's {@code marker} is reported though Office ignores
	 * it ([MS-OI29500] 2.1.1495).
	 */
	public Map<String, String> getSettings() {
		return settings;
	}

	/** {@code c:axId}: the axes the group is drawn against; none for pie and doughnut charts */
	public List<Long> getAxisIds() {
		return axisIds;
	}

	/** Every series, shown and filtered out, in their order ({@code c:order}) */
	public List<ChartSeries> getSeries() {
		return series;
	}

	/** The series Office draws: not filtered out, and for a pie chart the first only */
	public List<ChartSeries> getShownSeries() {
		List<ChartSeries> shown = new ArrayList<ChartSeries>();
		for (ChartSeries s : series) {
			if (s.isHidden()) continue;
			shown.add(s);
			if (kind == Kind.PIE || kind == Kind.PIE_3D) break;
		}
		return shown;
	}

	/** The group's own data labels ({@code c:dLbls}); each series' are on the series */
	public ChartDataLabels getDataLabels() {
		return dataLabels;
	}

	/**
	 * How Office draws a blank cell in this group: the chart's {@code c:dispBlanksAs} where the
	 * kind supports it, else the kind's default ([MS-OI29500] 2.1.1452); where the chart states
	 * none, the kind's default (not measured).
	 */
	public String getShownDispBlanksAs() {
		return shownDispBlanksAs;
	}

	/** [MS-OI29500] 2.1.1452: the methods a kind supports, its default first */
	static String[] blanks(Kind kind, String grouping) {
		boolean standard = grouping == null || "standard".equals(grouping);
		switch (kind) {
			case BAR: case BAR_3D: case PIE: case PIE_3D: case DOUGHNUT: case LINE_3D:
				return new String[] { "gap", "zero" };
			case AREA: return standard ? new String[] { "gap", "zero" } : new String[] { "zero" };
			case AREA_3D: return new String[] { "zero" };
			case LINE: return standard ? new String[] { "gap", "span", "zero" } : new String[] { "zero" };
			case BUBBLE: case SCATTER: case RADAR: return new String[] { "gap", "span", "zero" };
			default: return null;   // of-pie, stock and surface: the note does not say
		}
	}

	void applyDispBlanksAs(String stated) {
		String[] supported = blanks(kind, grouping);
		if (supported == null) {
			shownDispBlanksAs = stated;
			return;
		}
		shownDispBlanksAs = supported[0];
		if (stated != null) {
			for (String s : supported) if (s.equals(stated)) shownDispBlanksAs = stated;
		}
	}

	/** The JAXB group: a CTBarChart, CTLineChart ... */
	public Object getSource() {
		return source;
	}
}
