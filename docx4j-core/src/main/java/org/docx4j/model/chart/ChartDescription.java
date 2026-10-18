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

import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

/**
 * A chart put into words beside its {@link ChartTable} (CR-047): what kind of chart it is, the
 * titles of the axes Office shows, and which series are on secondary axes. The text model (WD
 * REQ-511) and the Markdown exporter write its {@link #getNote() note} before the chart's title, so
 * that a reader without the picture knows how to take the numbers.
 *
 * <ul>
 * <li><b>The kind</b> is Excel's English name for each group's chart type, in group order, joined
 * by "and": clustered column, stacked bar, 100% stacked area, 3-D column, line, pie, bar of pie,
 * doughnut, scatter, bubble, filled radar, stock, 3-D surface, wireframe contour; for chartex, the
 * first series' layout (waterfall, funnel, histogram, box and whisker, sunburst, treemap, map), and
 * pareto for a histogram with its Pareto line. Markers, smoothing and scatter styles are not
 * named.</li>
 * <li><b>The axes</b> are the ones Office shows, or shows the title of, in reading order, each
 * with its role: category axis, value axis, secondary value axis, secondary category axis, depth
 * axis; for scatter and bubble charts, x axis and y axis, then secondary x axis and secondary y
 * axis. An axis's title is the one Office shows, its default "Axis Title" included; an axis with no
 * {@code c:title} has none. A deleted axis ({@code c:delete}; for chartex, a hidden
 * {@code cx:axis}) is left out unless it has a title: Excel still shows a deleted axis's title
 * (CR-047's Office check). Pie, doughnut and of-pie charts have no axes.</li>
 * <li><b>A group is on the secondary axes</b> when its value axis is not the first group's value
 * axis; its shown series are the secondary series.</li>
 * </ul>
 *
 * @since 17.3.4 (CR-047)
 */
public final class ChartDescription {

	/** A chart group put into words. */
	public static final class Group {
		final String kind, element, barDirection, grouping;
		final List<String> series;
		final boolean secondary;

		Group(String kind, String element, String barDirection, String grouping, List<String> series, boolean secondary) {
			this.kind = kind;
			this.element = element;
			this.barDirection = barDirection;
			this.grouping = grouping;
			this.series = Collections.unmodifiableList(series);
			this.secondary = secondary;
		}

		/** Excel's name for the group's kind: "clustered column", "line", "waterfall" ... */
		public String getKind() { return kind; }
		/** The element: {@code barChart}, {@code line3DChart} ...; for chartex, the first series' {@code layoutId} */
		public String getElement() { return element; }
		/** {@code c:barDir} (bar or col) for a bar chart; else null */
		public String getBarDirection() { return barDirection; }
		/** {@code c:grouping} as stated; null where the kind has none */
		public String getGrouping() { return grouping; }
		/** The shown series' names, as Office shows them */
		public List<String> getSeries() { return series; }
		/** Whether the group is on the secondary axes */
		public boolean isSecondary() { return secondary; }
	}

	/** An axis Office shows. */
	public static final class Axis {
		final String role, position, title;

		Axis(String role, String position, String title) {
			this.role = role;
			this.position = position;
			this.title = title;
		}

		/** category axis, value axis, secondary value axis, secondary category axis, depth axis; x axis, y axis ... */
		public String getRole() { return role; }
		/** {@code c:axPos}: b, l, r or t; null for chartex */
		public String getPosition() { return position; }
		/** The title Office shows, its default "Axis Title" included; null for an axis with none */
		public String getTitle() { return title; }
	}

	private final String kind;
	private final List<Group> groups;
	private final List<Axis> axes;
	private final List<String> secondarySeries;
	private final ChartTable table;

	private ChartDescription(String kind, List<Group> groups, List<Axis> axes, List<String> secondarySeries, ChartTable table) {
		this.kind = kind;
		this.groups = Collections.unmodifiableList(groups);
		this.axes = Collections.unmodifiableList(axes);
		this.secondarySeries = Collections.unmodifiableList(secondarySeries);
		this.table = table;
	}

	// ------------------------------------------------------------------ a chart

	public static ChartDescription of(ChartModel m) {
		List<ChartGroup> chartGroups = m.getGroups();
		ChartGroup first = chartGroups.isEmpty() ? null : chartGroups.get(0);
		boolean xy = first != null && (first.getKind() == ChartGroup.Kind.SCATTER || first.getKind() == ChartGroup.Kind.BUBBLE);
		ChartAxis primaryValue = first == null ? null : valueAxis(m, first, xy);

		List<Group> groups = new ArrayList<Group>();
		List<String> secondarySeries = new ArrayList<String>();
		List<String> kinds = new ArrayList<String>();
		ChartGroup secondaryGroup = null;
		for (ChartGroup g : chartGroups) {
			ChartAxis value = valueAxis(m, g, xy);
			boolean secondary = g != first && value != null && value != primaryValue;
			if (secondary && secondaryGroup == null) secondaryGroup = g;
			List<String> names = new ArrayList<String>();
			for (ChartSeries s : g.getShownSeries()) names.add(s.getShownName());
			if (secondary) secondarySeries.addAll(names);
			String name = name(g);
			kinds.add(name);
			groups.add(new Group(name, g.getKind().value() + "Chart", g.getBarDirection(), g.getGrouping(), names, secondary));
		}

		List<Axis> axes = new ArrayList<Axis>();
		if (first != null) {
			Set<ChartAxis> primary = axesOf(m, first);
			Set<ChartAxis> secondary = new LinkedHashSet<ChartAxis>();
			if (secondaryGroup != null) for (ChartAxis a : axesOf(m, secondaryGroup)) if (!primary.contains(a)) secondary.add(a);
			if (xy) {
				ChartAxis x = xAxis(primary), y = other(primary, x, ChartAxis.Kind.VALUE);
				add(axes, "x axis", x);
				add(axes, "y axis", y);
				ChartAxis x2 = xAxis(secondary), y2 = other(secondary, x2, ChartAxis.Kind.VALUE);
				add(axes, "secondary x axis", x2);
				add(axes, "secondary y axis", y2);
			} else {
				add(axes, "category axis", category(primary));
				add(axes, "value axis", ofKind(primary, ChartAxis.Kind.VALUE));
				add(axes, "secondary value axis", ofKind(secondary, ChartAxis.Kind.VALUE));
				add(axes, "secondary category axis", category(secondary));
				ChartAxis depth = ofKind(primary, ChartAxis.Kind.SERIES);
				add(axes, "depth axis", depth != null ? depth : ofKind(secondary, ChartAxis.Kind.SERIES));
			}
		}
		return new ChartDescription(String.join(" and ", kinds), groups, axes, secondarySeries, ChartTable.of(m));
	}

	/** Excel's English name for a group's kind (CR-047 section 2.2). */
	static String name(ChartGroup g) {
		String grouping = g.getGrouping();
		switch (g.getKind()) {
		case BAR:
		case BAR_3D: {
			String base = "bar".equals(g.getBarDirection()) ? "bar" : "column";
			String stacking = grouping == null || "clustered".equals(grouping) ? "clustered " : "stacked".equals(grouping) ? "stacked "
					: "percentStacked".equals(grouping) ? "100% stacked " : "";
			return (g.getKind() == ChartGroup.Kind.BAR_3D ? "3-D " : "") + stacking + base;
		}
		case LINE:
			return stacked(grouping) + "line";
		case LINE_3D:
			return "3-D line";
		case AREA:
			return stacked(grouping) + "area";
		case AREA_3D:
			return "3-D " + stacked(grouping) + "area";
		case PIE:
			return "pie";
		case PIE_3D:
			return "3-D pie";
		case OF_PIE:
			return "bar".equals(g.getSettings().get("ofPieType")) ? "bar of pie" : "pie of pie";
		case DOUGHNUT:
			return "doughnut";
		case SCATTER:
			return "scatter";
		case BUBBLE:
			return "bubble";
		case RADAR:
			return "filled".equals(g.getSettings().get("radarStyle")) ? "filled radar" : "radar";
		case STOCK:
			return "stock";
		case SURFACE:
			return "true".equals(g.getSettings().get("wireframe")) ? "wireframe contour" : "contour";
		case SURFACE_3D:
			return "true".equals(g.getSettings().get("wireframe")) ? "wireframe 3-D surface" : "3-D surface";
		default:
			return g.getKind().value();
		}
	}

	private static String stacked(String grouping) {
		return "stacked".equals(grouping) ? "stacked " : "percentStacked".equals(grouping) ? "100% stacked " : "";
	}

	/** The group's axes, in its {@code c:axId} order. */
	private static Set<ChartAxis> axesOf(ChartModel m, ChartGroup g) {
		Set<ChartAxis> out = new LinkedHashSet<ChartAxis>();
		for (Long id : g.getAxisIds()) {
			ChartAxis a = m.getAxis(id);
			if (a != null) out.add(a);
		}
		return out;
	}

	/** A group's value axis: its first {@code c:valAx}; for scatter and bubble, the one at the left or right (y). */
	private static ChartAxis valueAxis(ChartModel m, ChartGroup g, boolean xy) {
		Set<ChartAxis> axes = axesOf(m, g);
		if (xy) {
			ChartAxis x = xAxis(axes);
			return other(axes, x, ChartAxis.Kind.VALUE);
		}
		return ofKind(axes, ChartAxis.Kind.VALUE);
	}

	private static ChartAxis ofKind(Set<ChartAxis> axes, ChartAxis.Kind kind) {
		for (ChartAxis a : axes) if (a.getKind() == kind) return a;
		return null;
	}

	private static ChartAxis category(Set<ChartAxis> axes) {
		for (ChartAxis a : axes) if (a.getKind() == ChartAxis.Kind.CATEGORY || a.getKind() == ChartAxis.Kind.DATE) return a;
		return null;
	}

	/** Of a scatter or bubble chart's two value axes, the one at the bottom or top; else the first. */
	private static ChartAxis xAxis(Set<ChartAxis> axes) {
		ChartAxis first = null;
		for (ChartAxis a : axes) {
			if (a.getKind() != ChartAxis.Kind.VALUE) continue;
			if ("b".equals(a.getPosition()) || "t".equals(a.getPosition())) return a;
			if (first == null) first = a;
		}
		return first;
	}

	private static ChartAxis other(Set<ChartAxis> axes, ChartAxis not, ChartAxis.Kind kind) {
		for (ChartAxis a : axes) if (a != not && a.getKind() == kind) return a;
		return null;
	}

	/** An axis Office shows, or shows the title of: a deleted axis's title still shows. */
	private static void add(List<Axis> axes, String role, ChartAxis a) {
		if (a == null || a.isDeleted() && a.getTitle() == null) return;
		axes.add(new Axis(role, a.getPosition(), a.getTitle() == null ? null : a.getTitle().getShownText()));
	}

	// ------------------------------------------------------------------ a chartex chart

	public static ChartDescription of(ChartExModel m) {
		List<String> names = new ArrayList<String>();
		String firstLayout = null;
		boolean histogram = false, paretoLine = false;
		for (ChartExModel.Series s : m.getSeries()) {
			if (firstLayout == null) firstLayout = s.getLayout();
			if ("clusteredColumn".equals(s.getLayout())) histogram = true;
			if ("paretoLine".equals(s.getLayout())) paretoLine = true;
			if (!s.isHidden()) names.add(s.getName());
		}
		String kind = histogram && paretoLine ? "pareto" : layoutName(firstLayout);
		List<Group> groups = new ArrayList<Group>();
		if (firstLayout != null) groups.add(new Group(kind, firstLayout, null, null, names, false));
		List<Axis> axes = new ArrayList<Axis>();
		int values = 0;
		for (ChartExModel.Axis a : m.getAxes()) {
			String role;
			if ("category".equals(a.getKind())) role = "category axis";
			else if ("value".equals(a.getKind())) role = values++ == 0 ? "value axis" : "secondary value axis";
			else continue;
			if (a.isHidden() && !a.hasTitle()) continue;
			axes.add(new Axis(role, null, a.getShownTitle()));
		}
		return new ChartDescription(kind, groups, axes, new ArrayList<String>(), ChartTable.of(m));
	}

	/** Excel's English name for a chartex series layout. */
	static String layoutName(String layout) {
		if (layout == null) return "chart";
		switch (layout) {
		case "clusteredColumn": return "histogram";
		case "paretoLine": return "pareto";
		case "boxWhisker": return "box and whisker";
		case "regionMap": return "map";
		default: return layout; // waterfall, funnel, sunburst, treemap
		}
	}

	// ------------------------------------------------------------------ reading it

	/** The kind: each group's, in group order, joined by "and" */
	public String getKind() {
		return kind;
	}

	/** The groups, in document order */
	public List<Group> getGroups() {
		return groups;
	}

	/** The axes Office shows, in reading order */
	public List<Axis> getAxes() {
		return axes;
	}

	/** The series on secondary axes, by their shown names; empty for most charts */
	public List<String> getSecondarySeries() {
		return secondarySeries;
	}

	/** The chart's table, {@link ChartTable#of(ChartModel)}'s */
	public ChartTable getTable() {
		return table;
	}

	/**
	 * The bracket note a text reader writes before the chart's title (WD REQ-511, REQ-707):
	 * {@code [chart: <kind>; <role>: <title>; ...; secondary axis: <series>, ...]}, an axis only where
	 * it has a title; the titles and series names are document text inside the extractor's brackets.
	 */
	public String getNote() {
		StringBuilder sb = new StringBuilder("[chart: ").append(kind);
		for (Axis a : axes) if (a.title != null) sb.append("; ").append(a.role).append(": ").append(a.title);
		if (!secondarySeries.isEmpty()) sb.append("; secondary axis: ").append(String.join(", ", secondarySeries));
		return sb.append(']').toString();
	}

	/**
	 * The description as JSON, for the ports' goldens (CR-047 section 2.5), its members in a fixed
	 * order and in docx4j's camelCase ({@code secondarySeries}; the block model's JSON, in
	 * docx4j-mcp, spells it as the WD does). The table is {@link ChartTable}'s, and not repeated.
	 */
	public String toJson() {
		Json j = new Json();
		j.open(null);
		j.put("kind", kind);
		j.openArray("groups");
		for (Group g : groups) {
			j.open(null);
			j.put("kind", g.kind);
			j.put("element", g.element);
			j.put("barDirection", g.barDirection);
			j.put("grouping", g.grouping);
			j.openArray("series");
			for (String s : g.series) j.put(null, s);
			j.closeArray();
			j.put("secondary", g.secondary);
			j.close();
		}
		j.closeArray();
		j.openArray("axes");
		for (Axis a : axes) {
			j.open(null);
			j.put("role", a.role);
			j.put("position", a.position);
			j.put("title", a.title);
			j.close();
		}
		j.closeArray();
		j.openArray("secondarySeries");
		for (String s : secondarySeries) j.put(null, s);
		j.closeArray();
		j.put("note", getNote());
		j.close();
		return j.toString();
	}

	@Override
	public String toString() {
		return getNote();
	}
}
