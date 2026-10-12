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

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.function.Consumer;

/**
 * A chart to make (CR-036 phase 2), assembled before {@link ChartCreator} makes it from Office's
 * own parts: in the shape of POI's XDDF, a chart with its categories, title, legend and data
 * labels, and one or more <b>groups</b>, each a chart type, its series and its pair of axes.
 * {@link #series} and the axes' setters are shorthand for the first group, so that combination
 * charts and secondary axes can later add a group without changing what is here
 * ({@link ChartCreator} makes charts of one group for now).
 *
 * <pre>
 * ChartDefinition d = ChartDefinition.of(ChartType.COLUMN_CLUSTERED)
 *     .categories("North", "South")
 *     .series("2025", 1.0, 2.0)
 *     .series("2026", 3.0, 4.0)
 *     .title("Sales")                            // or noTitle(); left out: Office's own title
 *     .legend(LegendPosition.BOTTOM)             // or noLegend(); left out: the type's default
 *     .valueAxis(a -&gt; a.title("Units").numberFormat("0.0"))
 *     .dataLabels(true);
 * </pre>
 *
 * Categories are text, numbers, or dates (each written into its cell as Excel's serial number,
 * with a date format); for a scatter chart they are the x values ({@link #xValues}).  A value
 * that is null leaves its point empty.
 *
 * @since 17.3.3 (CR-036)
 */
public final class ChartDefinition {

	/** The date format Excel gives a date typed into a cell (en-US): m/d/yyyy, built-in 14 */
	public static final String DEFAULT_DATE_FORMAT = "m/d/yyyy";

	private final List<Group> groups = new ArrayList<Group>();
	private final List<Object> categories = new ArrayList<Object>();
	private String dateFormat = DEFAULT_DATE_FORMAT;
	private String categoriesHeader = " ";
	private String title;
	private boolean titleDeleted;
	private LegendPosition legend;
	private boolean legendDeleted;
	private boolean dataLabels;

	private ChartDefinition(ChartType type) {
		groups.add(new Group(type));
	}

	/** A chart of that type, its data to come */
	public static ChartDefinition of(ChartType type) {
		if (type == null) throw new IllegalArgumentException("no chart type");
		return new ChartDefinition(type);
	}

	/**
	 * A chart of its data in one call, as {@link ChartDataEditor#setData} takes it: categories
	 * (text, numbers or {@link LocalDate}s), the series' names, {@code values[series][point]}
	 * (null for an empty point)
	 */
	public static ChartDefinition fromData(ChartType type, List<?> categories, List<String> seriesNames, Double[][] values) {
		ChartDefinition d = of(type).categories(categories);
		for (int s = 0; s < seriesNames.size(); s++) {
			Double[] v = values != null && s < values.length && values[s] != null ? values[s] : new Double[0];
			d.series(seriesNames.get(s), Arrays.asList(v));
		}
		return d;
	}

	// ------------------------------------------------------------------ the data

	/** Text categories */
	public ChartDefinition categories(String... labels) {
		return categories(Arrays.asList(labels));
	}

	/** Categories that are numbers (a scatter chart's x values: {@link #xValues}) */
	public ChartDefinition categories(double... values) {
		List<Object> l = new ArrayList<Object>();
		for (double v : values) l.add(v);
		return categories(l);
	}

	/** Categories: all text, all numbers, or all {@link LocalDate}s (with {@link #dateFormat}) */
	public ChartDefinition categories(List<?> values) {
		Class<?> kind = null;
		List<Object> l = new ArrayList<Object>();
		for (Object o : values) {
			if (o == null) throw new IllegalArgumentException("a category is null");
			Object v = o instanceof Number ? (Object) ((Number) o).doubleValue() : o;
			if (!(v instanceof String || v instanceof Double || v instanceof LocalDate)) {
				throw new IllegalArgumentException("a category is text, a number or a LocalDate: " + o.getClass().getName());
			}
			if (kind != null && kind != v.getClass()) {
				throw new IllegalArgumentException("the categories are all text, all numbers, or all dates");
			}
			kind = v.getClass();
			l.add(v);
		}
		categories.clear();
		categories.addAll(l);
		return this;
	}

	/** Date categories, in this format (the cells' and the axis's) */
	public ChartDefinition dates(String formatCode, LocalDate... days) {
		dateFormat(formatCode);
		return categories(Arrays.asList(days));
	}

	/** The format of date categories: {@link #DEFAULT_DATE_FORMAT} unless given */
	public ChartDefinition dateFormat(String formatCode) {
		if (formatCode == null || formatCode.isEmpty()) throw new IllegalArgumentException("no date format");
		this.dateFormat = formatCode;
		return this;
	}

	/** A scatter chart's x values: its categories, as {@link ChartDataEditor#addCategory(double, Double...)} has them */
	public ChartDefinition xValues(double... x) {
		return categories(x);
	}

	/**
	 * The text over the categories in the data's header row: Word's own data has a space there
	 * (the default), Excel's table needing a name for every column; none (null or empty) leaves
	 * the table to name it, Column1
	 */
	public ChartDefinition categoriesHeader(String text) {
		this.categoriesHeader = text;
		return this;
	}

	/** A series of the first group */
	public ChartDefinition series(String name, double... values) {
		groups.get(0).series(name, values);
		return this;
	}

	/** A series of the first group, a null value an empty point */
	public ChartDefinition series(String name, List<Double> values) {
		groups.get(0).series(name, values);
		return this;
	}

	/**
	 * Another group: a chart type with its own series (and, later, its own axes).  For combination
	 * charts and secondary axes, which {@link ChartCreator} doesn't make yet.
	 */
	public ChartDefinition group(ChartType type, Consumer<Group> g) {
		Group n = new Group(type);
		g.accept(n);
		groups.add(n);
		return this;
	}

	// ------------------------------------------------------------------ the settings

	/** The title's text */
	public ChartDefinition title(String text) {
		if (text == null) throw new IllegalArgumentException("no title text: noTitle() removes the title");
		this.title = text;
		this.titleDeleted = false;
		return this;
	}

	/** No title */
	public ChartDefinition noTitle() {
		this.title = null;
		this.titleDeleted = true;
		return this;
	}

	/** The legend, at that side */
	public ChartDefinition legend(LegendPosition position) {
		if (position == null) throw new IllegalArgumentException("no legend position: noLegend() removes the legend");
		this.legend = position;
		this.legendDeleted = false;
		return this;
	}

	/** No legend */
	public ChartDefinition noLegend() {
		this.legend = null;
		this.legendDeleted = true;
		return this;
	}

	/** The first group's category axis (a scatter chart's x axis) */
	public ChartDefinition categoryAxis(Consumer<Axis> a) {
		groups.get(0).categoryAxis(a);
		return this;
	}

	/** The first group's value axis */
	public ChartDefinition valueAxis(Consumer<Axis> a) {
		groups.get(0).valueAxis(a);
		return this;
	}

	/** Every series' values shown on its points */
	public ChartDefinition dataLabels(boolean shown) {
		this.dataLabels = shown;
		return this;
	}

	// ------------------------------------------------------------------ reading it

	/** The first group's type */
	public ChartType getType() {
		return groups.get(0).getType();
	}

	public List<Group> getGroups() {
		return Collections.unmodifiableList(groups);
	}

	/** Text, Double or LocalDate */
	public List<Object> getCategories() {
		return Collections.unmodifiableList(categories);
	}

	public String getDateFormat() {
		return dateFormat;
	}

	public String getCategoriesHeader() {
		return categoriesHeader;
	}

	/** The title's text; null for Office's own title, or none ({@link #isTitleDeleted()}) */
	public String getTitle() {
		return title;
	}

	public boolean isTitleDeleted() {
		return titleDeleted;
	}

	/** The legend's side; null for the type's default, or none ({@link #isLegendDeleted()}) */
	public LegendPosition getLegend() {
		return legend;
	}

	public boolean isLegendDeleted() {
		return legendDeleted;
	}

	public boolean isDataLabels() {
		return dataLabels;
	}

	/** A chart type, its series and its pair of axes */
	public static final class Group {

		private final ChartType type;
		private final List<Series> series = new ArrayList<Series>();
		private final Axis categoryAxis = new Axis(), valueAxis = new Axis();

		Group(ChartType type) {
			if (type == null) throw new IllegalArgumentException("no chart type");
			this.type = type;
		}

		public Group series(String name, double... values) {
			List<Double> l = new ArrayList<Double>();
			for (double v : values) l.add(v);
			return series(name, l);
		}

		public Group series(String name, List<Double> values) {
			if (name == null) throw new IllegalArgumentException("a series needs a name");
			series.add(new Series(name, values));
			return this;
		}

		public Group categoryAxis(Consumer<Axis> a) {
			a.accept(categoryAxis);
			return this;
		}

		public Group valueAxis(Consumer<Axis> a) {
			a.accept(valueAxis);
			return this;
		}

		public ChartType getType() {
			return type;
		}

		public List<Series> getSeries() {
			return Collections.unmodifiableList(series);
		}

		public Axis getCategoryAxis() {
			return categoryAxis;
		}

		public Axis getValueAxis() {
			return valueAxis;
		}
	}

	/** A series: its name and values, a null value an empty point */
	public static final class Series {

		private final String name;
		private final List<Double> values;

		Series(String name, List<Double> values) {
			this.name = name;
			this.values = Collections.unmodifiableList(new ArrayList<Double>(values));
		}

		public String getName() {
			return name;
		}

		public List<Double> getValues() {
			return values;
		}
	}

	/** An axis' title and its labels' number format */
	public static final class Axis {

		private String title, numberFormat;

		/** The axis' title */
		public Axis title(String text) {
			this.title = text;
			return this;
		}

		/** Its labels' number format (not linked to the cells') */
		public Axis numberFormat(String formatCode) {
			this.numberFormat = formatCode;
			return this;
		}

		public String getTitle() {
			return title;
		}

		public String getNumberFormat() {
			return numberFormat;
		}
	}
}
