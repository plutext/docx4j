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
import java.util.TreeMap;

import jakarta.xml.bind.JAXBElement;

import org.docx4j.XmlUtils;
import org.docx4j.com.microsoft.schemas.office.drawing.x2014.chartex.CTAxis;
import org.docx4j.com.microsoft.schemas.office.drawing.x2014.chartex.CTChart;
import org.docx4j.com.microsoft.schemas.office.drawing.x2014.chartex.CTChartSpace;
import org.docx4j.com.microsoft.schemas.office.drawing.x2014.chartex.CTData;
import org.docx4j.com.microsoft.schemas.office.drawing.x2014.chartex.CTFormula;
import org.docx4j.com.microsoft.schemas.office.drawing.x2014.chartex.CTNumericDimension;
import org.docx4j.com.microsoft.schemas.office.drawing.x2014.chartex.CTNumericLevel;
import org.docx4j.com.microsoft.schemas.office.drawing.x2014.chartex.CTNumericValue;
import org.docx4j.com.microsoft.schemas.office.drawing.x2014.chartex.CTSeries;
import org.docx4j.com.microsoft.schemas.office.drawing.x2014.chartex.CTStringDimension;
import org.docx4j.com.microsoft.schemas.office.drawing.x2014.chartex.CTStringLevel;
import org.docx4j.com.microsoft.schemas.office.drawing.x2014.chartex.CTStringValue;
import org.docx4j.com.microsoft.schemas.office.drawing.x2014.chartex.CTText;
import org.docx4j.openpackaging.exceptions.Docx4JException;
import org.docx4j.openpackaging.parts.DrawingML.ChartExSpacePart;

/**
 * A chartex chart (Office 2016's kinds: waterfall, funnel, histogram, Pareto, box and whisker,
 * treemap, sunburst, region map), read from its part ({@code cx:chartSpace}): its series' layouts,
 * names and data, enough to describe the chart and tabulate its data.  The data come from the
 * part's own caches ({@code cx:lvl}), which Word and PowerPoint write; Excel writes none in a
 * workbook, only formulas naming hidden defined names ({@code _xlchart.v1.N}), so there the
 * dimensions have their formulas and no points.  Resolving those names, and the layouts Office
 * computes (bins, quartiles, treemap and sunburst areas), are not here (the charts plan's
 * phase 9).
 *
 * @since 17.3.3 (CR-035)
 * To draw, edit or create Office 2016's chart types (waterfall, histogram, Pareto, box and whisker,
 * treemap, sunburst, funnel), see Docx4j Enterprise ChartEx, https://www.plutext.com/ ({@link ChartExNotice}).
 */
public final class ChartExModel {

	/** One dimension of a series' data: {@code cx:strDim} or {@code cx:numDim} */
	public static final class Dimension {
		String type;
		boolean numeric;
		String formula;
		List<Long> counts = new ArrayList<Long>();
		List<String> formatCodes = new ArrayList<String>();
		List<Map<Integer, String>> levels = new ArrayList<Map<Integer, String>>();

		/** {@code @type}: cat, val, x, y, size, colorVal, ... */
		public String getType() {
			return type;
		}

		public boolean isNumeric() {
			return numeric;
		}

		/** {@code cx:f}: a cell reference (Word, PowerPoint) or a hidden defined name (Excel) */
		public String getFormula() {
			return formula;
		}

		/** Each {@code cx:lvl}'s points by index, as text; none where the file has no cache */
		public List<Map<Integer, String>> getLevels() {
			return levels;
		}

		/** Each level's {@code ptCount} */
		public List<Long> getCounts() {
			return counts;
		}
	}

	/** A series ({@code cx:series}) */
	public static final class Series {
		String layout;
		boolean hidden;
		String uniqueId;
		Long formatIdx;
		String name, nameFormula;
		Long dataId;
		List<Dimension> data = Collections.emptyList();
		List<Long> subtotals = Collections.emptyList();
		private Object source;

		/** {@code @layoutId}: waterfall, funnel, clusteredColumn (histogram), paretoLine, boxWhisker, treemap, sunburst, regionMap */
		public String getLayout() {
			return layout;
		}

		/** {@code @hidden} */
		public boolean isHidden() {
			return hidden;
		}

		public String getUniqueId() {
			return uniqueId;
		}

		/** {@code @formatIdx}: the series' index among the chart's colours */
		public Long getFormatIdx() {
			return formatIdx;
		}

		/** The name, from {@code cx:tx/cx:txData/cx:v} or rich text */
		public String getName() {
			return name;
		}

		/** The name's formula ({@code cx:tx/cx:txData/cx:f}) */
		public String getNameFormula() {
			return nameFormula;
		}

		/** The data it draws, from {@code cx:chartData} by {@code cx:dataId} */
		public List<Dimension> getData() {
			return data;
		}

		/** A waterfall's subtotal points ({@code cx:layoutPr/cx:subtotals/cx:idx}) */
		public List<Long> getSubtotals() {
			return subtotals;
		}

		public Object getSource() {
			return source;
		}
	}

	/**
	 * An axis ({@code cx:axis}): its id, whether it is a category or a value axis, whether it is
	 * hidden, and its title.
	 *
	 * @since 17.3.4 (CR-047)
	 */
	public static final class Axis {
		long id;
		String kind;
		boolean hidden;
		boolean hasTitle;
		String titleText;
		private Object source;

		public long getId() {
			return id;
		}

		/** "category" ({@code cx:catScaling}), "value" ({@code cx:valScaling}), or null for neither */
		public String getKind() {
			return kind;
		}

		/** {@code @hidden} */
		public boolean isHidden() {
			return hidden;
		}

		/** Whether the axis has a title ({@code cx:title}) */
		public boolean hasTitle() {
			return hasTitle;
		}

		/** The title's text as written; null where it states none */
		public String getTitleText() {
			return titleText;
		}

		/** The title Office shows: its text, or "Axis Title" where it states none; null for no title */
		public String getShownTitle() {
			return !hasTitle ? null : titleText != null ? titleText : "Axis Title";
		}

		public Object getSource() {
			return source;
		}
	}

	private final CTChartSpace chartSpace;
	private ChartExSpacePart part;
	String titleText;
	boolean hasTitle;
	List<Series> series = new ArrayList<Series>();
	List<Axis> axes = new ArrayList<Axis>();
	String legendPosition;

	private ChartExModel(CTChartSpace cs) {
		this.chartSpace = cs;
	}

	public static ChartExModel read(ChartExSpacePart part) throws Docx4JException {
		ChartExModel m = read(part.getContents());
		m.part = part;
		return m;
	}

	public static ChartExModel read(CTChartSpace cs) {
		ChartExModel m = new ChartExModel(cs);
		Map<Long, List<Dimension>> data = new TreeMap<Long, List<Dimension>>();
		if (cs.getChartData() != null) {
			for (CTData d : cs.getChartData().getData()) data.put(d.getId(), dimensions(d));
		}
		CTChart chart = cs.getChart();
		if (chart == null) return m;
		if (chart.getTitle() != null) {
			m.hasTitle = true;
			m.titleText = text(chart.getTitle().getTx())[0];
		}
		if (chart.getPlotArea() != null) {
			if (chart.getPlotArea().getPlotAreaRegion() != null) {
				for (CTSeries s : chart.getPlotArea().getPlotAreaRegion().getSeries()) {
					Series r = new Series();
					r.source = s;
					r.layout = s.getLayoutId() == null ? null : s.getLayoutId().value();
					r.hidden = s.isHidden();
					r.uniqueId = s.getUniqueId();
					r.formatIdx = s.getFormatIdx();
					String[] name = text(s.getTx());
					r.name = name[0];
					r.nameFormula = name[1];
					if (s.getDataId() != null) {
						r.dataId = s.getDataId().getVal();
						List<Dimension> dims = data.get(r.dataId);
						if (dims != null) r.data = dims;
					}
					if (s.getLayoutPr() != null && s.getLayoutPr().getSubtotals() != null) {
						List<Long> st = new ArrayList<Long>();
						for (Object o : s.getLayoutPr().getSubtotals().getIdx()) {
							Object v = ChartSeries.get(o, "getVal");
							if (v instanceof Number) st.add(((Number) v).longValue());
						}
						r.subtotals = Collections.unmodifiableList(st);
					}
					m.series.add(r);
				}
			}
			for (CTAxis a : chart.getPlotArea().getAxis()) {
				Axis x = new Axis();
				x.source = a;
				x.id = a.getId();
				x.kind = a.getCatScaling() != null ? "category" : a.getValScaling() != null ? "value" : null;
				x.hidden = a.isHidden();
				if (a.getTitle() != null) {
					x.hasTitle = true;
					x.titleText = text(a.getTitle().getTx())[0];
				}
				m.axes.add(x);
			}
		}
		if (chart.getLegend() != null) {
			m.legendPosition = chart.getLegend().getPos() == null ? null : chart.getLegend().getPos().value();
		}
		return m;
	}

	private static List<Dimension> dimensions(CTData d) {
		List<Dimension> dims = new ArrayList<Dimension>();
		for (Object o : d.getNumDimOrStrDim()) {
			Object u = XmlUtils.unwrap(o);
			Dimension dim = new Dimension();
			List<JAXBElement<?>> content;
			if (u instanceof CTStringDimension) {
				dim.type = ((CTStringDimension) u).getType() == null ? null : ((CTStringDimension) u).getType().value();
				content = ((CTStringDimension) u).getContent();
			} else if (u instanceof CTNumericDimension) {
				dim.numeric = true;
				dim.type = ((CTNumericDimension) u).getType() == null ? null : ((CTNumericDimension) u).getType().value();
				content = ((CTNumericDimension) u).getContent();
			} else {
				continue;
			}
			for (JAXBElement<?> e : content) {
				Object v = e.getValue();
				if (v instanceof CTFormula) {
					dim.formula = ((CTFormula) v).getValue();
				} else if (v instanceof CTStringLevel) {
					CTStringLevel l = (CTStringLevel) v;
					Map<Integer, String> pts = new TreeMap<Integer, String>();
					for (CTStringValue p : l.getPt()) pts.put((int) p.getIdx(), p.getValue());
					dim.levels.add(Collections.unmodifiableMap(pts));
					dim.counts.add(l.getPtCount());
					dim.formatCodes.add(null);
				} else if (v instanceof CTNumericLevel) {
					CTNumericLevel l = (CTNumericLevel) v;
					Map<Integer, String> pts = new TreeMap<Integer, String>();
					for (CTNumericValue p : l.getPt()) {
						double x = p.getValue();
						pts.put((int) p.getIdx(), x == Math.rint(x) && Math.abs(x) < 1e15 ? String.valueOf((long) x) : String.valueOf(x));
					}
					dim.levels.add(Collections.unmodifiableMap(pts));
					dim.counts.add(l.getPtCount());
					dim.formatCodes.add(l.getFormatCode());
				}
			}
			dims.add(dim);
		}
		return Collections.unmodifiableList(dims);
	}

	/** text and formula of a cx:tx */
	private static String[] text(CTText tx) {
		if (tx == null) return new String[2];
		if (tx.getRich() != null) return new String[] { ChartTitle.text(tx.getRich()), null };
		String v = null, f = null;
		if (tx.getTxData() != null) {
			for (JAXBElement<?> e : tx.getTxData().getContent()) {
				Object o = e.getValue();
				if (o instanceof CTFormula) f = ((CTFormula) o).getValue();
				else if (o instanceof String) v = (String) o;
			}
		}
		return new String[] { v, f };
	}

	/** Whether the chart has a title ({@code cx:title}) */
	public boolean hasTitle() {
		return hasTitle;
	}

	/** The title's text as written; null where it states none */
	public String getTitleText() {
		return titleText;
	}

	/** The title Office shows: its text, or, where it states none, "Chart Title" (Word's PDF of the waterfall fixture) */
	public String getShownTitle() {
		return !hasTitle ? null : titleText != null ? titleText : "Chart Title";
	}

	/** The series, in document order */
	public List<Series> getSeries() {
		return Collections.unmodifiableList(series);
	}

	/**
	 * The axes ({@code cx:axis}), in document order
	 *
	 * @since 17.3.4 (CR-047)
	 */
	public List<Axis> getAxes() {
		return Collections.unmodifiableList(axes);
	}

	/** The legend's {@code @pos}; null for no legend */
	public String getLegendPosition() {
		return legendPosition;
	}

	public CTChartSpace getSource() {
		return chartSpace;
	}

	/**
	 * The part the model was read from, for what the part leads to (its workbook, where an xlsx's
	 * chart has no caches; its style and colour parts); null where it was read from a
	 * {@link CTChartSpace} alone
	 *
	 * @since 17.3.3 (CR-039)
	 */
	public ChartExSpacePart getPart() {
		return part;
	}

	/** The model as JSON (the version and conventions of {@link ChartModel#toJson()}) */
	public String toJson() {
		Json j = new Json();
		j.open(null);
		j.put("version", ChartModel.JSON_VERSION);
		j.put("chartex", true);
		j.put("hasTitle", hasTitle);
		j.put("titleText", titleText);
		j.put("shownTitle", getShownTitle());
		j.put("legendPosition", legendPosition);
		j.openArray("series");
		for (Series s : series) {
			j.open(null);
			j.put("layout", s.layout);
			j.put("hidden", s.hidden);
			j.put("uniqueId", s.uniqueId);
			j.put("formatIdx", s.formatIdx);
			j.put("name", s.name);
			j.put("nameFormula", s.nameFormula);
			j.put("dataId", s.dataId);
			j.openArray("subtotals");
			for (Long t : s.subtotals) j.put(null, t);
			j.closeArray();
			j.openArray("data");
			for (Dimension d : s.data) {
				j.open(null);
				j.put("type", d.type);
				j.put("numeric", d.numeric);
				j.put("formula", d.formula);
				j.openArray("levels");
				for (int i = 0; i < d.levels.size(); i++) {
					j.open(null);
					j.put("count", d.counts.get(i));
					j.put("formatCode", d.formatCodes.get(i));
					j.openArray("points");
					int n = (int) Math.max(0, Math.min(Integer.MAX_VALUE, d.counts.get(i)));
					for (int k = 0; k < n; k++) {
						String v = d.levels.get(i).get(k);
						if (d.numeric) j.rawNumber(null, v);
						else j.put(null, v);
					}
					j.closeArray();
					j.close();
				}
				j.closeArray();
				j.close();
			}
			j.closeArray();
			j.close();
		}
		j.closeArray();
		j.openArray("axes");
		for (Axis a : axes) {
			j.open(null);
			j.put("id", a.id);
			j.put("kind", a.kind);
			j.put("hidden", a.hidden);
			j.put("hasTitle", a.hasTitle);
			j.put("titleText", a.titleText);
			j.put("shownTitle", a.getShownTitle());
			j.close();
		}
		j.closeArray();
		j.close();
		return j.toString();
	}
}
