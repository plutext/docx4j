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

import org.docx4j.dml.chart.CTLegend;
import org.docx4j.dml.chart.CTLegendEntry;

/**
 * A chart's legend ({@code c:legend}).
 *
 * @since 17.3.3 (CR-035)
 */
public final class ChartLegend {

	private final String position;
	private final boolean overlay;
	private final List<Long> deletedEntries;
	private final CTLegend source;

	private ChartLegend(String position, boolean overlay, List<Long> deletedEntries, CTLegend source) {
		this.position = position;
		this.overlay = overlay;
		this.deletedEntries = deletedEntries;
		this.source = source;
	}

	static ChartLegend of(CTLegend l) {
		if (l == null) return null;
		List<Long> deleted = new ArrayList<Long>();
		for (CTLegendEntry e : l.getLegendEntry()) {
			if (e.getIdx() != null && e.getDelete() != null && e.getDelete().isVal()) deleted.add(e.getIdx().getVal());
		}
		String pos = l.getLegendPos() == null ? "r" : Vals.str(l.getLegendPos());
		return new ChartLegend(pos == null ? "r" : pos, l.getOverlay() != null && l.getOverlay().isVal(),
				Collections.unmodifiableList(deleted), l);
	}

	/** {@code c:legendPos}: b, l, r, t or tr; r where not stated (its schema default) */
	public String getPosition() {
		return position;
	}

	/** {@code c:overlay}: laid over the plot area rather than beside it */
	public boolean isOverlay() {
		return overlay;
	}

	/** {@code c:legendEntry} with {@code c:delete}: the entries removed, by index */
	public List<Long> getDeletedEntries() {
		return deletedEntries;
	}

	public CTLegend getSource() {
		return source;
	}
}
