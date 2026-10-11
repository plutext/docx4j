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

import org.docx4j.dml.chart.CTCatAx;
import org.docx4j.dml.chart.CTChartLines;
import org.docx4j.dml.chart.CTDateAx;
import org.docx4j.dml.chart.CTNumFmt;
import org.docx4j.dml.chart.CTScaling;
import org.docx4j.dml.chart.CTSerAx;
import org.docx4j.dml.chart.CTValAx;

/**
 * An axis ({@code c:catAx}, {@code c:valAx}, {@code c:dateAx}, {@code c:serAx}) as the file
 * states it.  A value the file does not state is null, except where noted; the chart groups
 * name their axes by {@link #getId()}.
 *
 * @since 17.3.3 (CR-035)
 */
public final class ChartAxis {

	public enum Kind {
		/** {@code c:catAx} */
		CATEGORY,
		/** {@code c:valAx} */
		VALUE,
		/** {@code c:dateAx} */
		DATE,
		/** {@code c:serAx}: a 3-D chart's depth axis */
		SERIES
	}

	private final Kind kind;
	private final Object source;
	long id;
	Long crossAxisId;
	String position;
	boolean deleted;
	String orientation;
	Double min, max, logBase;
	Double majorUnit, minorUnit;
	String baseTimeUnit, majorTimeUnit, minorTimeUnit;
	String numberFormat;
	Boolean numberFormatSourceLinked;
	String crosses;
	Double crossesAt;
	String crossBetween;
	String majorTickMark, minorTickMark, tickLabelPosition;
	boolean majorGridlines, minorGridlines;
	ChartTitle title;

	private ChartAxis(Kind kind, Object source) {
		this.kind = kind;
		this.source = source;
	}

	/** An axis from its JAXB object; null for anything else */
	static ChartAxis of(Object o) {
		if (o instanceof CTCatAx) {
			CTCatAx a = (CTCatAx) o;
			ChartAxis x = new ChartAxis(Kind.CATEGORY, a);
			common(x, Vals.unsigned(a.getAxId()), a.getScaling(), Vals.bool(a.getDelete()), Vals.str(a.getAxPos()),
					a.getMajorGridlines(), a.getMinorGridlines(), a.getNumFmt(), Vals.str(a.getMajorTickMark()),
					Vals.str(a.getMinorTickMark()), Vals.str(a.getTickLblPos()), Vals.unsigned(a.getCrossAx()),
					Vals.str(a.getCrosses()), Vals.dbl(a.getCrossesAt()));
			x.title = ChartTitle.of(a.getTitle(), "Axis Title");
			return x;
		}
		if (o instanceof CTValAx) {
			CTValAx a = (CTValAx) o;
			ChartAxis x = new ChartAxis(Kind.VALUE, a);
			common(x, Vals.unsigned(a.getAxId()), a.getScaling(), Vals.bool(a.getDelete()), Vals.str(a.getAxPos()),
					a.getMajorGridlines(), a.getMinorGridlines(), a.getNumFmt(), Vals.str(a.getMajorTickMark()),
					Vals.str(a.getMinorTickMark()), Vals.str(a.getTickLblPos()), Vals.unsigned(a.getCrossAx()),
					Vals.str(a.getCrosses()), Vals.dbl(a.getCrossesAt()));
			x.crossBetween = Vals.str(a.getCrossBetween());
			x.majorUnit = Vals.num(a.getMajorUnit());
			x.minorUnit = Vals.num(a.getMinorUnit());
			x.title = ChartTitle.of(a.getTitle(), "Axis Title");
			return x;
		}
		if (o instanceof CTDateAx) {
			CTDateAx a = (CTDateAx) o;
			ChartAxis x = new ChartAxis(Kind.DATE, a);
			common(x, Vals.unsigned(a.getAxId()), a.getScaling(), Vals.bool(a.getDelete()), Vals.str(a.getAxPos()),
					a.getMajorGridlines(), a.getMinorGridlines(), a.getNumFmt(), Vals.str(a.getMajorTickMark()),
					Vals.str(a.getMinorTickMark()), Vals.str(a.getTickLblPos()), Vals.unsigned(a.getCrossAx()),
					Vals.str(a.getCrosses()), Vals.dbl(a.getCrossesAt()));
			x.majorUnit = Vals.num(a.getMajorUnit());
			x.minorUnit = Vals.num(a.getMinorUnit());
			x.baseTimeUnit = Vals.str(a.getBaseTimeUnit());
			x.majorTimeUnit = Vals.str(a.getMajorTimeUnit());
			x.minorTimeUnit = Vals.str(a.getMinorTimeUnit());
			x.title = ChartTitle.of(a.getTitle(), "Axis Title");
			return x;
		}
		if (o instanceof CTSerAx) {
			CTSerAx a = (CTSerAx) o;
			ChartAxis x = new ChartAxis(Kind.SERIES, a);
			common(x, Vals.unsigned(a.getAxId()), a.getScaling(), Vals.bool(a.getDelete()), Vals.str(a.getAxPos()),
					a.getMajorGridlines(), a.getMinorGridlines(), a.getNumFmt(), Vals.str(a.getMajorTickMark()),
					Vals.str(a.getMinorTickMark()), Vals.str(a.getTickLblPos()), Vals.unsigned(a.getCrossAx()),
					Vals.str(a.getCrosses()), Vals.dbl(a.getCrossesAt()));
			x.title = ChartTitle.of(a.getTitle(), "Axis Title");
			return x;
		}
		return null;
	}

	private static void common(ChartAxis x, Long id, CTScaling scaling, Boolean deleted, String position,
			CTChartLines major, CTChartLines minor, CTNumFmt numFmt, String majorTick, String minorTick,
			String tickLabels, Long crossAx, String crosses, Double crossesAt) {
		x.id = id == null ? 0 : id;
		if (scaling != null) {
			x.orientation = Vals.str(scaling.getOrientation());
			x.min = Vals.dbl(scaling.getMin());
			x.max = Vals.dbl(scaling.getMax());
			x.logBase = Vals.num(scaling.getLogBase());
		}
		x.deleted = deleted != null && deleted;
		x.position = position;
		x.majorGridlines = major != null;
		x.minorGridlines = minor != null;
		if (numFmt != null) {
			x.numberFormat = numFmt.getFormatCode();
			x.numberFormatSourceLinked = numFmt.isSourceLinked();
		}
		x.majorTickMark = majorTick;
		x.minorTickMark = minorTick;
		x.tickLabelPosition = tickLabels;
		x.crossAxisId = crossAx;
		x.crosses = crosses;
		x.crossesAt = crossesAt;
	}

	public Kind getKind() {
		return kind;
	}

	/** {@code c:axId} */
	public long getId() {
		return id;
	}

	/** {@code c:crossAx}: the axis this one crosses */
	public Long getCrossAxisId() {
		return crossAxisId;
	}

	/** {@code c:axPos}: b, l, r or t */
	public String getPosition() {
		return position;
	}

	/** {@code c:delete}: the axis is not drawn (its scale still applies); false where not stated */
	public boolean isDeleted() {
		return deleted;
	}

	/** {@code c:scaling/c:orientation}: minMax or maxMin */
	public String getOrientation() {
		return orientation;
	}

	/** {@code c:scaling/c:min}; null for automatic */
	public Double getMin() {
		return min;
	}

	/** {@code c:scaling/c:max}; null for automatic */
	public Double getMax() {
		return max;
	}

	/** {@code c:scaling/c:logBase}; null for a linear scale */
	public Double getLogBase() {
		return logBase;
	}

	/** {@code c:majorUnit}; null for automatic */
	public Double getMajorUnit() {
		return majorUnit;
	}

	/** {@code c:minorUnit}; null for automatic */
	public Double getMinorUnit() {
		return minorUnit;
	}

	/** a date axis' {@code c:baseTimeUnit}: days, months or years */
	public String getBaseTimeUnit() {
		return baseTimeUnit;
	}

	public String getMajorTimeUnit() {
		return majorTimeUnit;
	}

	public String getMinorTimeUnit() {
		return minorTimeUnit;
	}

	/** {@code c:numFmt/@formatCode} */
	public String getNumberFormat() {
		return numberFormat;
	}

	/** {@code c:numFmt/@sourceLinked}: the format is the data's, not the axis' own */
	public Boolean getNumberFormatSourceLinked() {
		return numberFormatSourceLinked;
	}

	/** {@code c:crosses}: autoZero, min or max; null where {@link #getCrossesAt()} says instead */
	public String getCrosses() {
		return crosses;
	}

	/** {@code c:crossesAt}: the value at which the crossing axis crosses this one */
	public Double getCrossesAt() {
		return crossesAt;
	}

	/** a value axis' {@code c:crossBetween}: between or midCat */
	public String getCrossBetween() {
		return crossBetween;
	}

	/** {@code c:majorTickMark}: cross, in, none or out */
	public String getMajorTickMark() {
		return majorTickMark;
	}

	public String getMinorTickMark() {
		return minorTickMark;
	}

	/** {@code c:tickLblPos}: high, low, nextTo or none */
	public String getTickLabelPosition() {
		return tickLabelPosition;
	}

	/** Whether {@code c:majorGridlines} is there */
	public boolean hasMajorGridlines() {
		return majorGridlines;
	}

	public boolean hasMinorGridlines() {
		return minorGridlines;
	}

	/** The axis' title; null for none */
	public ChartTitle getTitle() {
		return title;
	}

	/** The JAXB axis: a CTCatAx, CTValAx, CTDateAx or CTSerAx */
	public Object getSource() {
		return source;
	}
}
