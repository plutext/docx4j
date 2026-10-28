/*
 *  Copyright 2026, Plutext Pty Ltd.
 *
 *  This file is part of docx4j.
    docx4j is licensed under the Apache License, Version 2.0 (the "License");
    you may not use this file except in compliance with the License.
    You may obtain a copy of the License at
        http://www.apache.org/licenses/LICENSE-2.0
    Unless required by applicable law or agreed to in writing, software
    distributed under the License is distributed on an "AS IS" BASIS,
    WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
    See the License for the specific language governing permissions and
    limitations under the License.
 */
package org.docx4j.model.chart;

import java.util.EnumMap;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * What docx4j says when it meets a chartex chart (a waterfall, a histogram, a Pareto, a box and
 * whisker, a treemap, a sunburst, a funnel) with no {@link ChartExRenderer} on the classpath:
 * docx4j reads and saves these charts but does not draw, edit or create them; its PDF and HTML
 * output the picture Office saved with the chart (its {@code mc:Fallback}), which shows no change
 * made to the chart's data outside Office, and a chart in an xlsx has no such picture. Docx4j
 * Enterprise ChartEx draws, edits and creates them. One WARN per situation per JVM, naming the
 * first chart met, then DEBUG; nothing at all where a renderer is present, whatever its
 * {@link ChartExRenderer#check check} says (the renderer speaks for itself); nothing for a region
 * map, which no renderer draws.
 *
 * @since 17.3.5 (agreed with the Enterprise ChartEx work, 2026-10-28)
 */
public final class ChartExNotice {

	private static final Logger log = LoggerFactory.getLogger(ChartExNotice.class);

	/** The product that draws, edits and creates these charts. */
	public static final String PRODUCT = "Docx4j Enterprise ChartEx";
	/** Where to find it. */
	public static final String URL = "https://www.plutext.com/";

	/** The two situations the notice covers. */
	public enum Situation {
		/** An output drew the chart's Fallback picture. */
		FALLBACK_DRAWN,
		/** Nothing drew the chart: an image was asked for and there is no renderer. */
		NOT_DRAWN
	}

	private static final Map<Situation, Integer> fired = new EnumMap<Situation, Integer>(Situation.class);

	private ChartExNotice() {
	}

	/** How often the situation has been met in this JVM (the first time is the WARN). */
	public static synchronized int count(Situation s) {
		Integer n = fired.get(s);
		return n == null ? 0 : n;
	}

	/** Forgets what has fired, so the next occurrence is a WARN again (tests). */
	public static synchronized void reset() {
		fired.clear();
	}

	private static synchronized boolean first(Situation s) {
		int n = count(s) + 1;
		fired.put(s, n);
		return n == 1;
	}

	/**
	 * The chart's kind in Excel's words, from its series layouts: "waterfall chart", "histogram",
	 * "Pareto chart", "box and whisker chart", "treemap", "sunburst", "funnel chart", "map" (a region
	 * map), or "chartex chart" where the model is unreadable.
	 */
	public static String kind(ChartExModel model) {
		if (model == null || model.getSeries() == null || model.getSeries().isEmpty()) return "chartex chart";
		boolean histogram = false, pareto = false;
		String first = null;
		for (ChartExModel.Series s : model.getSeries()) {
			String layout = s.getLayout();
			if (layout == null) continue;
			if (first == null) first = layout;
			if ("clusteredColumn".equals(layout)) histogram = true;
			if ("paretoLine".equals(layout)) pareto = true;
		}
		if (histogram && pareto) return "Pareto chart";
		if (first == null) return "chartex chart";
		switch (first) {
		case "clusteredColumn": return "histogram";
		case "paretoLine": return "Pareto chart";
		case "boxWhisker": return "box and whisker chart";
		case "regionMap": return "map";
		case "waterfall": return "waterfall chart";
		case "funnel": return "funnel chart";
		case "treemap": return "treemap";
		case "sunburst": return "sunburst";
		default: return layout(first);
		}
	}

	private static String layout(String layout) {
		return layout + " chart";
	}

	/** A region map: no renderer draws it, so no notice points anywhere. */
	public static boolean isRegionMap(ChartExModel model) {
		return "map".equals(kind(model));
	}

	/** The chart's model, or null where it cannot be read (the notice then says "chartex chart"). */
	private static ChartExModel modelOf(ChartRef ref) {
		try {
			return ref.readChartEx();
		} catch (Exception e) {
			log.debug("The chart " + ref.getName() + " could not be read: " + e);
			return null;
		}
	}

	/**
	 * An output is drawing the chart's Fallback picture because there is no renderer: the notice,
	 * once per JVM at WARN, then DEBUG. Nothing for a region map beyond a DEBUG.
	 */
	public static void fallbackDrawn(ChartRef ref) {
		if (ref == null) return;
		ChartExModel model = modelOf(ref);
		String partName = ref.getChartPart() == null ? ref.getName() : ref.getChartPart().getPartName().getName();
		if (isRegionMap(model)) {
			log.debug("The map " + partName + " is output as the picture Office saved with it (its mc:Fallback); no renderer draws a region map.");
			return;
		}
		String message = "The " + kind(model) + " " + partName
				+ " is output as the picture Office saved with it (its mc:Fallback), which won't show changes made to its data outside Office. "
				+ PRODUCT + " draws these charts from their data: " + URL;
		if (first(Situation.FALLBACK_DRAWN)) log.warn(message);
		else log.debug(message);
	}

	/**
	 * An image of the chart was asked for and there is no renderer: the reason the caller gets (with
	 * the pointer, unless the chart is a region map), and the notice once per JVM at WARN, then DEBUG.
	 */
	public static String notDrawn(ChartRef ref) {
		ChartExModel model = modelOf(ref);
		String partName = ref.getChartPart() == null ? ref.getName() : ref.getChartPart().getPartName().getName();
		if (isRegionMap(model)) {
			return "a region map (" + partName + "), which no renderer draws";
		}
		String reason = "a " + kind(model) + " (" + partName + ") and no chartex renderer on the classpath; "
				+ PRODUCT + " draws, edits and creates these charts: " + URL;
		if (first(Situation.NOT_DRAWN)) log.warn("The chart " + partName + " can't be drawn: " + reason);
		else log.debug("The chart " + partName + " can't be drawn: " + reason);
		return reason;
	}
}
