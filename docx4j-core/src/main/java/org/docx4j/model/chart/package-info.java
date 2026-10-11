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
/**
 * Charts in docx, pptx and xlsx: where they are, and what they say.
 *
 * <ul>
 * <li>{@link org.docx4j.model.chart.Charts#in} finds every chart a package places, in document
 *     order, as a {@link org.docx4j.model.chart.ChartRef}: its host part, anchor, extent, name and
 *     chart part, a chart in an {@code mc:AlternateContent} Choice (a chartex chart) included.</li>
 * <li>{@link org.docx4j.model.chart.ChartModel} reads a chart part: its groups (several in a
 *     combination chart), their series (those Excel's chart filter hides too, marked hidden), the
 *     series' data from the caches, keyed by point index, and their fills and lines with colours
 *     resolved in the chart's theme; the axes, the title and the legend.  Where Office derives
 *     what it shows (a title with no text, a series with no name, varyColors with several series,
 *     data label positions, how blank cells are drawn), it says what Office shows as well, from
 *     Office's own notes ([MS-OI29500]) and Office's renderings of the probes.</li>
 * <li>{@link org.docx4j.model.chart.ChartExModel} reads a chartex chart (waterfall, funnel ...)
 *     from its own caches.</li>
 * <li>{@link org.docx4j.model.chart.ChartCells} reads the same data from the cells, in the
 *     workbook or the workbook embedded with the chart, and reports where they and the caches
 *     differ.</li>
 * <li>Both models have a versioned JSON form ({@code toJson()}), which docx4j's TypeScript and
 *     Python ports compare against, and which describes a chart to a tool.</li>
 * <li>{@link org.docx4j.model.chart.ChartBox} keeps a chart's space in the PDF and HTML outputs.</li>
 * </ul>
 *
 * Drawing a chart, and writing one, are not here yet.  The design is
 * {@code docs/developer/change-requests/CR-035-charts-box-read-model-describe-edit.md}.
 *
 * @since 17.3.3
 */
package org.docx4j.model.chart;
