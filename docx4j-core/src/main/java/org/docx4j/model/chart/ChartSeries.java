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

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

import org.docx4j.XmlUtils;
import org.docx4j.dml.CTShapeProperties;
import org.docx4j.dml.chart.CTAxDataSource;
import org.docx4j.dml.chart.CTBoolean;
import org.docx4j.dml.chart.CTDLbls;
import org.docx4j.dml.chart.CTDPt;
import org.docx4j.dml.chart.CTExtension;
import org.docx4j.dml.chart.CTExtensionList;
import org.docx4j.dml.chart.CTMarker;
import org.docx4j.dml.chart.CTNumDataSource;
import org.docx4j.dml.chart.CTSerTx;
import org.docx4j.dml.chart.CTUnsignedInt;
import org.docx4j.model.color.ThemeColors;
import org.w3c.dom.Element;

/**
 * A series ({@code c:ser} of any of the eight kinds), as one model.  Its data are read from the
 * file's caches ({@link ChartData}), keyed by point index.  A series hidden with Excel's chart
 * filter (a {@code c15:filtered...Series}) is read too, marked {@link #isHidden() hidden}: it
 * keeps its {@code idx} and {@code order}, which the shown series share a sequence with.
 *
 * @since 17.3.3 (CR-035)
 */
public final class ChartSeries {

	private static final String UNIQUE_ID_URI = "{C3380CC4-5D6E-409C-BE32-E72D297353CC}";

	long idx, order;
	ChartData name;
	String shownName;
	ChartData categories, values, xValues, yValues, bubbleSizes;
	boolean hidden;
	String uniqueId;
	ChartFill fill, line;
	Map<Integer, ChartFill> pointFills = Collections.emptyMap();
	Boolean smooth, invertIfNegative;
	Long explosion;
	String marker;
	ChartDataLabels dataLabels;
	private final Object source;

	private ChartSeries(Object source) {
		this.source = source;
	}

	static ChartSeries of(Object ser, boolean hidden, CTDLbls groupLabels, ChartGroup.Kind kind, String grouping,
			ThemeColors theme) {
		ChartSeries s = new ChartSeries(ser);
		s.hidden = hidden;
		Long idx = Vals.unsigned((CTUnsignedInt) get(ser, "getIdx"));
		Long order = Vals.unsigned((CTUnsignedInt) get(ser, "getOrder"));
		s.idx = idx == null ? 0 : idx;
		s.order = order == null ? s.idx : order;
		s.name = ChartData.of((CTSerTx) get(ser, "getTx"));
		String named = s.name == null ? null : s.name.getText(0);
		// [MS-OI29500] 2.1.1565: a series with no c:tx is "Series" and its idx plus one
		s.shownName = named != null ? named : "Series" + (s.idx + 1);
		s.categories = ChartData.of((CTAxDataSource) get(ser, "getCat"));
		s.values = ChartData.of((CTNumDataSource) get(ser, "getVal"));
		s.xValues = ChartData.of((CTAxDataSource) get(ser, "getXVal"));
		s.yValues = ChartData.of((CTNumDataSource) get(ser, "getYVal"));
		s.bubbleSizes = ChartData.of((CTNumDataSource) get(ser, "getBubbleSize"));
		CTShapeProperties spPr = (CTShapeProperties) get(ser, "getSpPr");
		s.fill = ChartFill.fill(spPr, theme);
		s.line = ChartFill.line(spPr, theme);
		@SuppressWarnings("unchecked")
		List<CTDPt> dPts = (List<CTDPt>) get(ser, "getDPt");
		if (dPts != null && !dPts.isEmpty()) {
			Map<Integer, ChartFill> m = new TreeMap<Integer, ChartFill>();
			for (CTDPt pt : dPts) {
				if (pt.getIdx() != null) m.put((int) pt.getIdx().getVal(), ChartFill.fill(pt.getSpPr(), theme));
			}
			s.pointFills = Collections.unmodifiableMap(m);
		}
		s.smooth = Vals.bool((CTBoolean) get(ser, "getSmooth"));
		s.invertIfNegative = Vals.bool((CTBoolean) get(ser, "getInvertIfNegative"));
		s.explosion = Vals.unsigned((CTUnsignedInt) get(ser, "getExplosion"));
		CTMarker m = (CTMarker) get(ser, "getMarker");
		s.marker = m == null ? null : Vals.str(m.getSymbol());
		s.dataLabels = ChartDataLabels.of(groupLabels, (CTDLbls) get(ser, "getDLbls"), kind, grouping);
		s.uniqueId = uniqueId((CTExtensionList) get(ser, "getExtLst"));
		return s;
	}

	/** {@code c16:uniqueId}, which docx4j does not bind: read from the element */
	private static String uniqueId(CTExtensionList extLst) {
		if (extLst == null) return null;
		for (CTExtension e : extLst.getExt()) {
			if (!UNIQUE_ID_URI.equalsIgnoreCase(e.getUri())) continue;
			for (Object o : e.getAny()) {
				Object u = XmlUtils.unwrap(o);
				if (u instanceof Element && "uniqueId".equals(((Element) u).getLocalName())) {
					return ((Element) u).getAttribute("val");
				}
				Object v = get(u, "getVal");
				if (v instanceof String) return (String) v;
			}
		}
		return null;
	}

	/** A getter the series class may not have (each kind has its own set); null for none */
	static Object get(Object o, String getter) {
		if (o == null) return null;
		try {
			return o.getClass().getMethod(getter).invoke(o);
		} catch (NoSuchMethodException e) {
			return null;
		} catch (ReflectiveOperationException e) {
			return null;
		}
	}

	/** {@code c:idx}: the series' index, which numbers its automatic colour and default name */
	public long getIdx() {
		return idx;
	}

	/** {@code c:order}: the series' place in the plot */
	public long getOrder() {
		return order;
	}

	/** {@code c:tx}: the name as the file has it, a cell or text; null for none */
	public ChartData getName() {
		return name;
	}

	/** The name Office shows: the series' own, or "Series" and its index plus one */
	public String getShownName() {
		return shownName;
	}

	/** {@code c:cat}; null for none (Office then numbers the categories 1, 2, 3 ...) */
	public ChartData getCategories() {
		return categories;
	}

	/** {@code c:val} */
	public ChartData getValues() {
		return values;
	}

	/** {@code c:xVal}: scatter and bubble charts */
	public ChartData getXValues() {
		return xValues;
	}

	/** {@code c:yVal}: scatter and bubble charts */
	public ChartData getYValues() {
		return yValues;
	}

	/** {@code c:bubbleSize}: bubble charts */
	public ChartData getBubbleSizes() {
		return bubbleSizes;
	}

	/** Hidden with Excel's chart filter: read from a {@code c15:filtered...Series} */
	public boolean isHidden() {
		return hidden;
	}

	/** {@code c16:uniqueId}; null for none */
	public String getUniqueId() {
		return uniqueId;
	}

	/** The series' fill: automatic where its {@code c:spPr} states none */
	public ChartFill getFill() {
		return fill;
	}

	/** The series' line: automatic where its {@code c:spPr} states none */
	public ChartFill getLine() {
		return line;
	}

	/** {@code c:dPt}: the points with formatting of their own, by index */
	public Map<Integer, ChartFill> getPointFills() {
		return pointFills;
	}

	/** {@code c:smooth}: line and scatter series */
	public Boolean getSmooth() {
		return smooth;
	}

	/** {@code c:invertIfNegative}: bar series (ignored in bubble charts) */
	public Boolean getInvertIfNegative() {
		return invertIfNegative;
	}

	/** {@code c:explosion}: pie series */
	public Long getExplosion() {
		return explosion;
	}

	/** {@code c:marker/c:symbol}: line, scatter and radar series */
	public String getMarker() {
		return marker;
	}

	/** The series' data labels over its group's; null for none */
	public ChartDataLabels getDataLabels() {
		return dataLabels;
	}

	/** The JAXB series: a CTBarSer, CTLineSer, CTPieSer, CTAreaSer, CTScatterSer, CTRadarSer, CTBubbleSer or CTSurfaceSer */
	public Object getSource() {
		return source;
	}
}
