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

/**
 * The chart types {@link ChartCreator} makes (CR-036 phase 2), as Excel names them in its
 * {@code XlChartType} ({@code COLUMN_CLUSTERED} for {@code xlColumnClustered}, ...): each made from
 * Excel's own chart of that type, its chart, style and colour parts as Excel's <i>Insert Chart</i>
 * wrote them (CR-036 phase 0, P1).  Combination charts, secondary axes, 3-D, stacked lines and
 * areas, and smooth scatter come later.
 *
 * @since 17.3.3 (CR-036)
 * To draw, edit or create Office 2016's chart types (waterfall, histogram, Pareto, box and whisker,
 * treemap, sunburst, funnel), see Docx4j Enterprise ChartEx, https://www.plutext.com/ ({@link ChartExNotice}).
 */
public enum ChartType {

	COLUMN_CLUSTERED("column-clustered", 51, 201),
	COLUMN_STACKED("column-stacked", 52, 297),
	COLUMN_STACKED_100("column-stacked-100", 53, 297),
	BAR_CLUSTERED("bar-clustered", 57, 216),
	BAR_STACKED("bar-stacked", 58, 297),
	BAR_STACKED_100("bar-stacked-100", 59, 297),
	LINE("line", 4, 227),
	LINE_MARKERS("line-markers", 65, 332),
	PIE("pie", 5, 251),
	DOUGHNUT("doughnut", -4120, 251),
	AREA("area", 1, 276),
	/** Markers only; its categories are the x values */
	SCATTER("scatter", -4169, 240);

	private final String template;
	private final int excelType, style;

	ChartType(String template, int excelType, int style) {
		this.template = template;
		this.excelType = excelType;
		this.style = style;
	}

	/** The name of its template (and of the probes' files: p1-column-clustered.xlsx, ...) */
	public String getTemplateName() {
		return template;
	}

	/** Excel's {@code XlChartType} number */
	public int getExcelType() {
		return excelType;
	}

	/** The id of its chart style ({@code cs:chartStyle/@id}): Excel's default for the type */
	public int getStyleId() {
		return style;
	}

	/** Whether it has axes (all but pie and doughnut) */
	public boolean hasAxes() {
		return this != PIE && this != DOUGHNUT;
	}

	/** Whether each point has its own colour ({@code c:dPt}): pie and doughnut */
	public boolean variesColours() {
		return this == PIE || this == DOUGHNUT;
	}
}
