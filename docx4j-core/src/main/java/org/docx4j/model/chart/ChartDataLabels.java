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

import org.docx4j.dml.chart.CTDLbls;

/**
 * A series' data labels ({@code c:dLbls}), the chart group's under the series' own: [MS-OI29500]
 * 2.1.1457 (b), the group's labels, then the series', then a single label's.  A setting neither
 * states is null.  The position Office uses where none is stated depends on the chart's kind
 * ([MS-OI29500] 2.1.1456 (b)): {@link #getShownPosition()}.
 *
 * @since 17.3.3 (CR-035)
 */
public final class ChartDataLabels {

	Boolean deleted;
	Boolean showValue, showCategoryName, showSeriesName, showPercent, showBubbleSize, showLegendKey;
	String position;
	String shownPosition;
	String numberFormat;
	Boolean numberFormatSourceLinked;
	String separator;

	private ChartDataLabels() {
	}

	/** The series' labels over the group's; null where neither has any */
	static ChartDataLabels of(CTDLbls group, CTDLbls series, ChartGroup.Kind kind, String grouping) {
		if (group == null && series == null) return null;
		ChartDataLabels l = new ChartDataLabels();
		for (CTDLbls d : new CTDLbls[] { group, series }) {
			if (d == null) continue;
			if (d.getDelete() != null) l.deleted = d.getDelete().isVal();
			if (d.getShowVal() != null) l.showValue = d.getShowVal().isVal();
			if (d.getShowCatName() != null) l.showCategoryName = d.getShowCatName().isVal();
			if (d.getShowSerName() != null) l.showSeriesName = d.getShowSerName().isVal();
			if (d.getShowPercent() != null) l.showPercent = d.getShowPercent().isVal();
			if (d.getShowBubbleSize() != null) l.showBubbleSize = d.getShowBubbleSize().isVal();
			if (d.getShowLegendKey() != null) l.showLegendKey = d.getShowLegendKey().isVal();
			if (d.getDLblPos() != null) l.position = Vals.str(d.getDLblPos());
			if (d.getNumFmt() != null) {
				l.numberFormat = d.getNumFmt().getFormatCode();
				l.numberFormatSourceLinked = d.getNumFmt().isSourceLinked();
			}
			if (d.getSeparator() != null) l.separator = d.getSeparator();
		}
		l.shownPosition = l.position != null ? l.position : defaultPosition(kind, grouping);
		return l;
	}

	/** [MS-OI29500] 2.1.1456 (b): the position Office uses where none is stated */
	static String defaultPosition(ChartGroup.Kind kind, String grouping) {
		switch (kind) {
			case PIE: case PIE_3D: case OF_PIE: return "bestFit";
			case AREA: case AREA_3D: case DOUGHNUT: return "ctr";
			case BAR: case BAR_3D:
				return "stacked".equals(grouping) || "percentStacked".equals(grouping) ? "ctr" : "outEnd";
			case RADAR: return "outEnd";
			case BUBBLE: case SCATTER: case STOCK: case LINE: case LINE_3D: return "r";
			default: return null;
		}
	}

	/** {@code c:delete}: the labels are removed */
	public Boolean getDeleted() {
		return deleted;
	}

	/** {@code c:showVal} */
	public Boolean getShowValue() {
		return showValue;
	}

	/** {@code c:showCatName} */
	public Boolean getShowCategoryName() {
		return showCategoryName;
	}

	/** {@code c:showSerName} */
	public Boolean getShowSeriesName() {
		return showSeriesName;
	}

	/** {@code c:showPercent}: pie, of-pie and doughnut charts only */
	public Boolean getShowPercent() {
		return showPercent;
	}

	/** {@code c:showBubbleSize}: bubble charts only */
	public Boolean getShowBubbleSize() {
		return showBubbleSize;
	}

	/** {@code c:showLegendKey} */
	public Boolean getShowLegendKey() {
		return showLegendKey;
	}

	/** {@code c:dLblPos} as stated; null where neither states it */
	public String getPosition() {
		return position;
	}

	/** The position Office uses: the stated one, or the kind's default */
	public String getShownPosition() {
		return shownPosition;
	}

	/** {@code c:numFmt/@formatCode} */
	public String getNumberFormat() {
		return numberFormat;
	}

	public Boolean getNumberFormatSourceLinked() {
		return numberFormatSourceLinked;
	}

	/** {@code c:separator} */
	public String getSeparator() {
		return separator;
	}
}
