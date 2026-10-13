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
 * The open chart renderer's frame, for a renderer of other charts (a chartex renderer: a
 * {@link org.docx4j.model.chart.ChartExRenderer}, CR-039): what docx4j-chart-render measured
 * against Office (CR-037), offered as functions and a drawing, without the renderer itself.
 *
 * <ul>
 * <li>{@link org.docx4j.chart.render.api.AxisScale}: Excel's automatic axis bounds and unit;</li>
 * <li>{@link org.docx4j.chart.render.api.TextStyle} and {@link org.docx4j.chart.render.api.TextWrap}:
 *     text measured in the face docx4j's font mapping gives it, and wrapped as Office wraps it;</li>
 * <li>{@link org.docx4j.chart.render.api.Fills}: a shape's {@code a:spPr} as fills, lines, dashes,
 *     shadows and glows, through the theme;</li>
 * <li>{@link org.docx4j.chart.render.api.ChartFrame}: the title's and legend's places, and the plot
 *     area they leave;</li>
 * <li>{@link org.docx4j.chart.render.api.Drawing} and {@link org.docx4j.chart.render.api.SvgWriter}:
 *     what is painted, in order, painted to a {@code Graphics2D} or written as SVG for docx4j's
 *     outputs.</li>
 * </ul>
 *
 * <p>Number formats are docx4j-core's (xlsx4j's port of POI's {@code DataFormatter}).  The
 * signatures are kept from 17.3.3; the numbers behind them improve as CR-037's open items close.</p>
 *
 * @since 17.3.3 (CR-039)
 */
package org.docx4j.chart.render.api;
