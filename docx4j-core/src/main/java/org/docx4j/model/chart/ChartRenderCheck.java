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

/**
 * What a {@link ChartRenderer} would do with a chart (CR-037, review A1): a chart with a
 * <em>structural</em> feature the renderer doesn't draw (a chart kind, an axis kind, a shape of
 * data, 3-D, a secondary axis, a combination) is refused, and the outputs keep its box empty,
 * since a chart drawn wrong is worse than a box; one whose only gaps are <em>cosmetic</em> (an
 * effect, a gradient, a pattern, a picture fill) is drawn simplified, each simplification named.
 * The reasons are for the API and the MCP tool to report.
 *
 * @since 17.3.3 (CR-037)
 */
public final class ChartRenderCheck {

	private final List<String> refused;
	private final List<String> simplified;

	/**
	 * @param refused    why the chart is not drawn: each structural feature not drawn (none: drawn)
	 * @param simplified what is drawn simplified: each cosmetic feature not drawn as Office draws it
	 */
	public ChartRenderCheck(List<String> refused, List<String> simplified) {
		this.refused = Collections.unmodifiableList(new ArrayList<String>(refused == null ? Collections.<String>emptyList() : refused));
		this.simplified = Collections.unmodifiableList(new ArrayList<String>(simplified == null ? Collections.<String>emptyList() : simplified));
	}

	/** Whether the chart is drawn (perhaps simplified) */
	public boolean isDrawn() {
		return refused.isEmpty();
	}

	/** Whether the chart is drawn, but simplified */
	public boolean isSimplified() {
		return isDrawn() && !simplified.isEmpty();
	}

	/** Why the chart is not drawn; empty where it is */
	public List<String> getRefused() {
		return refused;
	}

	/** What is drawn simplified */
	public List<String> getSimplified() {
		return simplified;
	}

	@Override
	public String toString() {
		if (!isDrawn()) return "refused: " + String.join("; ", refused);
		return simplified.isEmpty() ? "drawn" : "drawn simplified: " + String.join("; ", simplified);
	}
}
