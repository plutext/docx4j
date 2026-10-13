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
package org.docx4j.fidelity.corpus;

import java.io.File;
import java.io.InputStream;
import java.io.PrintWriter;
import java.nio.charset.StandardCharsets;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Random;
import java.util.function.Consumer;
import java.util.function.UnaryOperator;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.docx4j.Docx4jProperties;
import org.docx4j.XmlUtils;
import org.docx4j.dml.chart.CTAreaChart;
import org.docx4j.dml.chart.STTickLblPos;
import org.docx4j.dml.chart.STCrosses;
import org.docx4j.dml.chart.CTTitle;
import org.docx4j.dml.chart.CTTickLblPos;
import org.docx4j.dml.chart.CTCrosses;
import org.docx4j.dml.chart.CTChartLines;
import org.docx4j.dml.chart.CTDLbl;
import org.docx4j.dml.chart.CTCatAx;
import org.docx4j.dml.chart.CTDateAx;
import org.docx4j.dml.chart.CTGrouping;
import org.docx4j.dml.chart.CTLineChart;
import org.docx4j.dml.chart.CTLineSer;
import org.docx4j.dml.chart.CTMarker;
import org.docx4j.dml.chart.CTScatterChart;
import org.docx4j.dml.chart.CTScatterSer;
import org.docx4j.dml.chart.CTTickMark;
import org.docx4j.dml.chart.CTTimeUnit;
import org.docx4j.dml.chart.STGrouping;
import org.docx4j.dml.chart.STMarkerStyle;
import org.docx4j.dml.chart.STScatterStyle;
import org.docx4j.dml.chart.STTickMark;
import org.docx4j.dml.chart.STTimeUnit;
import org.docx4j.dml.chart.CTAxisUnit;
import org.docx4j.dml.chart.CTBoolean;
import org.docx4j.dml.chart.CTChart;
import org.docx4j.dml.chart.CTChartSpace;
import org.docx4j.dml.chart.CTDLblPos;
import org.docx4j.dml.chart.CTDLbls;
import org.docx4j.dml.chart.CTDPt;
import org.docx4j.dml.chart.CTDispBlanksAs;
import org.docx4j.dml.chart.CTDouble;
import org.docx4j.dml.chart.CTDoughnutChart;
import org.docx4j.dml.chart.CTFirstSliceAng;
import org.docx4j.dml.chart.CTHoleSize;
import org.docx4j.dml.chart.CTLayout;
import org.docx4j.dml.chart.CTLayoutMode;
import org.docx4j.dml.chart.CTLayoutTarget;
import org.docx4j.dml.chart.CTLogBase;
import org.docx4j.dml.chart.CTManualLayout;
import org.docx4j.dml.chart.CTNumFmt;
import org.docx4j.dml.chart.CTPieChart;
import org.docx4j.dml.chart.CTPieSer;
import org.docx4j.dml.chart.CTPlotArea;
import org.docx4j.dml.chart.CTScaling;
import org.docx4j.dml.chart.CTUnsignedInt;
import org.docx4j.dml.chart.CTValAx;
import org.docx4j.dml.chart.STDLblPos;
import org.docx4j.dml.chart.STDispBlanksAs;
import org.docx4j.dml.chart.STLayoutMode;
import org.docx4j.dml.chart.STLayoutTarget;
import org.docx4j.jaxb.Context;
import org.docx4j.model.chart.ChartCreator;
import org.docx4j.model.chart.ChartDefinition;
import org.docx4j.model.chart.ChartRef;
import org.docx4j.model.chart.ChartSettings;
import org.docx4j.model.chart.ChartType;
import org.docx4j.model.chart.Charts;
import org.docx4j.model.chart.LegendPosition;
import org.docx4j.openpackaging.packages.PresentationMLPackage;
import org.docx4j.openpackaging.packages.SpreadsheetMLPackage;
import org.docx4j.openpackaging.packages.WordprocessingMLPackage;
import org.docx4j.openpackaging.parts.PartName;
import org.docx4j.openpackaging.parts.ThemePart;
import org.docx4j.openpackaging.parts.DrawingML.Chart;
import org.docx4j.openpackaging.parts.PresentationML.SlideLayoutPart;
import org.docx4j.openpackaging.parts.PresentationML.SlidePart;
import org.docx4j.openpackaging.parts.SpreadsheetML.WorksheetPart;
import org.docx4j.wml.P;
import org.docx4j.wml.R;
import org.docx4j.wml.SectPr;
import org.pptx4j.pml.CTGraphicalObjectFrame;
import org.xlsx4j.model.CellRef;

/**
 * CR-037 phase 0: the probes Office draws, so that docx4j's chart renderer can be measured
 * against Office's drawing as numbers (docs/developer/change-requests/CR-037-charts-drawn-svg-png-pdf-html.md,
 * section 4.1).  Each probe is one chart, made by docx4j ({@link ChartCreator}) at a fixed size,
 * as an xlsx (the chart beside its data, as Excel's <i>Insert Chart</i> makes it) and as a docx
 * (as Word's makes it), and for R1 as a pptx too; tools/office-chart-render-probes.ps1 then has
 * Excel dump each chart's layout and export its picture, and Word and PowerPoint save theirs as
 * PDF.
 *
 * <p>The sets: R1 the twelve types (Word's default data), one with no {@code c:txPr} and two in
 * the 2023 theme; R2 the value axis's scaling; R3 one chart at four sizes; R4 text (long
 * categories, a long title, eight series, text sizes, many categories); R5 number formats; R6
 * data labels at each position; R7 blanks; R8 pie and doughnut; R9 manual layouts; R10 Excel's
 * gallery styles (applied on the VM, {@code style} in the manifest); R11 (phase 1) the value
 * axis's unit against its length, one chart at many heights and a bar chart at many widths; R12
 * (phase 1) the category labels as the room for them shrinks; R13 (phase 1) how far labels turned
 * 45 degrees push the plot right; R14 (phase 2) what the other types add, sweeps, and Excel's own
 * charts of CR-036 (an xlsx only); R15 (phase 4) what the settings and the data add: axis titles,
 * data labels, manual layouts, legend entries, axis options, fills and effects, titles of several
 * runs, and Excel's own charts of CR-036's P2 and P6 settings (an xlsx only); R16 (phase 4) a
 * pie's labels outside it and at best fit, one label on a small slice turned round the pie; R17
 * (phase 4) best fit inside, one thing changed at a time.</p>
 *
 * <p>One theme for each probe (review A2): the docx's theme part (Office 2013's, Calibri and
 * accent 1 4472C4, unless the probe says 2023) is copied into the xlsx, which docx4j makes with
 * none, and into the pptx, so that Excel, Word and PowerPoint draw against the same scheme.  A
 * docx's page is the chart's size with a half-inch margin all round, the chart in the first
 * paragraph (no spacing), so it sits at 36pt, 36pt; a pptx's chart is at 72pt, 72pt.</p>
 *
 * <p>Writes into the directory given the probes and {@code probes.tsv} (name, set, size in
 * points, hosts, theme, Excel gallery style, the chart's place on the PDF page for each host,
 * what the probe asks, R14's sweep).  Ids come from a {@link Random} seeded by the probe's name, so the same
 * files are made each time.</p>
 *
 * <p>usage: {@code java -cp ... org.docx4j.fidelity.corpus.ChartRenderProbes <dir> [<CR-036 charts dir>]}</p>
 *
 * @since 17.3.3 (CR-037 phase 0)
 */
public final class ChartRenderProbes {

	/** Word's size of a chart it inserts, 6 by 3.5 inches, in points */
	private static final int W = 432, H = 252;

	/** A docx's chart: its top left on the page (the margins) */
	private static final int DOCX_AT = 36;

	/** A pptx's chart: its top left on the slide */
	private static final int PPTX_AT = 72;

	private static final String W_NS = " xmlns:w=\"http://schemas.openxmlformats.org/wordprocessingml/2006/main\"";

	private static final List<String> ROWS = new ArrayList<>();

	private final String name, set, note;
	private final ChartDefinition definition;
	private int w = W, h = H;
	private boolean pptx;
	private String theme = "2013";
	private int style;
	private UnaryOperator<CTChartSpace> tweak = c -> c;
	private UnaryOperator<String> xmlTweak = s -> s;
	/** R14: the chart resized step by step in Excel, "width:from:to:step" or "height:..." */
	private String sweep = "";
	/** R14: an Excel chart (CR-036's P1, P6) copied as it is, an xlsx only */
	private File copyOf;
	/** R14: the chart's colour and style parts removed (no colour style to cycle through) */
	private boolean noColourStyle;

	private ChartRenderProbes(String set, String name, ChartDefinition definition, String note) {
		this.set = set;
		this.name = name;
		this.definition = definition;
		this.note = note;
	}

	private static ChartRenderProbes probe(String set, String name, ChartDefinition d, String note) {
		return new ChartRenderProbes(set, name, d, note);
	}

	private ChartRenderProbes size(int width, int height) {
		w = width;
		h = height;
		return this;
	}

	private ChartRenderProbes withPptx() {
		pptx = true;
		return this;
	}

	private ChartRenderProbes theme(String t) {
		theme = t;
		return this;
	}

	private ChartRenderProbes style(int s) {
		style = s;
		return this;
	}

	private ChartRenderProbes noColourStyle() {
		noColourStyle = true;
		return this;
	}

	private ChartRenderProbes sweep(String spec) {
		sweep = spec;
		return this;
	}

	private static ChartRenderProbes copy(String set, File xlsx, int width, int height, String note) {
		return copy(set, xlsx, xlsx.getName().replaceAll("\\.xlsx$", ""), width, height, note);
	}

	private static ChartRenderProbes copy(String set, File xlsx, String name, int width, int height, String note) {
		ChartRenderProbes p = new ChartRenderProbes(set, name, null, note);
		p.copyOf = xlsx;
		return p.size(width, height);
	}

	/** A change to the chart made in each host, after ChartCreator */
	private ChartRenderProbes tweak(Consumer<CTChartSpace> t) {
		UnaryOperator<CTChartSpace> before = tweak;
		tweak = c -> {
			CTChartSpace d = before.apply(c);
			t.accept(d);
			return d;
		};
		return this;
	}

	/** A change to the chart's XML made in each host, after {@link #tweak} */
	private ChartRenderProbes xml(UnaryOperator<String> t) {
		UnaryOperator<String> before = xmlTweak;
		xmlTweak = s -> t.apply(before.apply(s));
		return this;
	}

	// ------------------------------------------------------------------ the data

	private static final String[] CATS = { "Category 1", "Category 2", "Category 3", "Category 4" };

	/** Word's default data for the type, as its Insert Chart writes it */
	static ChartDefinition words(ChartType t) {
		ChartDefinition d = ChartDefinition.of(t);
		switch (t) {
		case PIE:
		case DOUGHNUT:
			return d.categories("1st Qtr", "2nd Qtr", "3rd Qtr", "4th Qtr").series("Sales", 8.2, 3.2, 1.4, 1.2);
		case AREA:
			return d.dates("m/d/yyyy", LocalDate.of(2002, 5, 1), LocalDate.of(2002, 6, 1), LocalDate.of(2002, 7, 1),
					LocalDate.of(2002, 8, 1), LocalDate.of(2002, 9, 1)).series("Series 1", 32, 32, 28, 12, 15)
					.series("Series 2", 12, 12, 12, 21, 28);
		case SCATTER:
			return d.categoriesHeader("X-Values").xValues(0.7, 1.8, 2.6).series("Y-Values", 2.7, 3.2, 0.8);
		default:
			return d.categories(CATS).series("Series 1", 4.3, 2.5, 3.5, 4.5).series("Series 2", 2.4, 4.4, 1.8, 2.8)
					.series("Series 3", 2, 2, 3, 5);
		}
	}

	private static ChartDefinition column() {
		return words(ChartType.COLUMN_CLUSTERED);
	}

	/** One series over four text categories, a legend at the bottom (as Word's has one) */
	private static ChartDefinition one(ChartType t, double... v) {
		String[] cats = new String[v.length];
		for (int i = 0; i < v.length; i++) cats[i] = String.valueOf((char) ('A' + i));
		return ChartDefinition.of(t).categories(cats).series("Data", v).legend(LegendPosition.BOTTOM);
	}

	private static ChartDefinition categories(int n, String prefix, int series) {
		String[] cats = new String[n];
		for (int i = 0; i < n; i++) cats[i] = prefix + (i + 1);
		ChartDefinition d = ChartDefinition.of(ChartType.COLUMN_CLUSTERED).categories(cats);
		Random r = new Random(n * 31 + series);
		for (int s = 0; s < series; s++) {
			double[] v = new double[n];
			for (int i = 0; i < n; i++) v[i] = Math.round(10 + r.nextDouble() * 40) / 10.0;
			d.series("Series " + (s + 1), v);
		}
		return d;
	}

	// ------------------------------------------------------------------ the sets

	private static List<ChartRenderProbes> probes() {
		List<ChartRenderProbes> l = new ArrayList<>();

		// R1 the twelve types, Word's default data, each in an xlsx, a docx and a pptx
		for (ChartType t : ChartType.values()) {
			l.add(probe("R1", "r1-" + t.getTemplateName(), words(t), "the " + t.getTemplateName() + " chart, Word's default data").withPptx());
		}
		l.add(probe("R1", "r1-column-clustered-no-txpr", column(),
				"every c:txPr removed: the text Office draws where a chart names none (A5)").xml(ChartRenderProbes::noTxPr));
		l.add(probe("R1", "r1t23-column-clustered", column(), "the 2023 theme (Aptos, 156082)").theme("2023"));
		l.add(probe("R1", "r1t23-pie", words(ChartType.PIE), "the 2023 theme (Aptos, 156082)").theme("2023"));

		// R2 the value axis's scaling (KB 214075's rule as the hypothesis, A4)
		ChartType col = ChartType.COLUMN_CLUSTERED;
		l.add(probe("R2", "r2-0-1", one(col, 0.12, 0.45, 0.78, 0.93), "data 0.12 to 0.93"));
		l.add(probe("R2", "r2-0-100", one(col, 12, 45, 78, 93), "data 12 to 93"));
		l.add(probe("R2", "r2-max-5", one(col, 1, 2, 3, 5), "maximum 5, on a unit"));
		l.add(probe("R2", "r2-max-10", one(col, 2, 4, 8, 10), "maximum 10, on a unit"));
		l.add(probe("R2", "r2-negative", one(col, -12, -45, -78, -93), "all negative"));
		l.add(probe("R2", "r2-both-signs", one(col, -35, 12, 60, -8), "either sign"));
		l.add(probe("R2", "r2-95-100", one(col, 95.2, 97.1, 99.4, 96.3), "95 to 100: the minimum leaves zero"));
		l.add(probe("R2", "r2-49-60", one(col, 49, 52, 57, 60), "range 11 over maximum 60 (more than a sixth)"));
		l.add(probe("R2", "r2-50-60", one(col, 50, 52, 57, 60), "range 10 over maximum 60 (a sixth)"));
		l.add(probe("R2", "r2-51-60", one(col, 51, 52, 57, 60), "range 9 over maximum 60 (less than a sixth)"));
		l.add(probe("R2", "r2-1e6", one(col, 1.2e6, 3.4e6, 2.7e6, 4.9e6), "millions"));
		l.add(probe("R2", "r2-0001", one(col, 0.0012, 0.0034, 0.0027, 0.0049), "thousandths"));
		l.add(probe("R2", "r2-mixed", one(col, 1234, 5678, 910, 2345), "910 to 5678"));
		l.add(probe("R2", "r2-constant", one(col, 5, 5, 5, 5), "a constant series"));
		l.add(probe("R2", "r2-zero", one(col, 0, 0, 0, 0), "all zero"));
		l.add(probe("R2", "r2-log", one(col, 3, 40, 700, 9000), "a log axis, base 10")
				.tweak(c -> scaling(c).setLogBase(logBase(10))));
		l.add(probe("R2", "r2-min-set", one(col, 12, 45, 78, 93), "the minimum set to 20")
				.tweak(c -> scaling(c).setMin(dbl(20))));
		l.add(probe("R2", "r2-max-set", one(col, 12, 45, 78, 93), "the maximum set to 150")
				.tweak(c -> scaling(c).setMax(dbl(150))));
		l.add(probe("R2", "r2-unit-set", one(col, 12, 45, 78, 93), "the major unit set to 25")
				.tweak(c -> valAx(c).setMajorUnit(unit(25))));
		l.add(probe("R2", "r2-line", one(ChartType.LINE, 95.2, 97.1, 99.4, 96.3), "95 to 100 on a line chart"));
		l.add(probe("R2", "r2-bar", one(ChartType.BAR_CLUSTERED, 12, 45, 78, 93), "12 to 93 on a bar chart"));

		// R3 one chart at four sizes: label rotation, skipping, the legend wrapping
		int[][] sizes = { { 180, 108 }, { 288, 168 }, { 432, 252 }, { 576, 336 } };
		for (int[] s : sizes) {
			l.add(probe("R3", "r3-default-" + s[0] + "x" + s[1], column(), "Word's default column at " + s[0] + " by " + s[1] + "pt")
					.size(s[0], s[1]));
			l.add(probe("R3", "r3-eight-" + s[0] + "x" + s[1], categories(8, "Category ", 3),
					"eight categories, three series, at " + s[0] + " by " + s[1] + "pt").size(s[0], s[1]));
		}

		// R4 text
		l.add(probe("R4", "r4-long-categories", ChartDefinition.of(col)
				.categories("North-east region", "South-west region", "Central and eastern districts", "Far northern islands")
				.series("Series 1", 4.3, 2.5, 3.5, 4.5).series("Series 2", 2.4, 4.4, 1.8, 2.8), "long category names"));
		l.add(probe("R4", "r4-long-title", column().title(
				"Quarterly sales by region for the financial year ending June 2026, all products and services"),
				"a title longer than the chart is wide"));
		ChartDefinition eight = ChartDefinition.of(col).categories("A", "B", "C");
		for (int s = 1; s <= 8; s++) eight.series("Series " + s, s, s + 1, s + 2);
		l.add(probe("R4", "r4-eight-series", eight, "a legend of eight entries, at the bottom"));
		ChartDefinition eightRight = ChartDefinition.of(col).categories("A", "B", "C").legend(LegendPosition.RIGHT);
		for (int s = 1; s <= 8; s++) eightRight.series("Series " + s, s, s + 1, s + 2);
		l.add(probe("R4", "r4-eight-series-right", eightRight, "a legend of eight entries, at the right"));
		for (int sz : new int[] { 700, 1200, 1600 }) {
			l.add(probe("R4", "r4-text-" + sz / 100, column(), "the axes' and legend's text at " + sz / 100 + "pt (the title 14pt)")
					.xml(s -> textSize(s, sz)));
		}
		l.add(probe("R4", "r4-twenty-categories", categories(20, "Item ", 1).legend(LegendPosition.BOTTOM),
				"twenty categories: labels turned or skipped"));
		l.add(probe("R4", "r4-years", yearsDefinition(), "twelve years as categories (numbers)"));

		// R5 number formats, on the value axis and the data labels
		String[][] formats = { { "general", "General" }, { "0.0", "0.0" }, { "percent", "0%" }, { "currency", "\"$\"#,##0.00" },
				{ "thousands", "#,##0" }, { "date-mdy", "m/d/yyyy" }, { "date-dmmm", "d-mmm" } };
		for (String[] f : formats) {
			double[] v = f[1].contains("%") ? new double[] { 0.125, 0.25, 0.4, 0.6 }
					: f[1].contains("m") ? new double[] { 46023, 46030, 46040, 46050 } : new double[] { 1234.5, 2500.25, 1800, 3100.75 };
			l.add(probe("R5", "r5-" + f[0], one(col, v).valueAxis(a -> a.numberFormat(f[1])).dataLabels(true),
					"the value axis and the data labels in " + f[1]).tweak(c -> labelFormat(c, f[1])));
		}
		l.add(probe("R5", "r5-categories-dmmm", ChartDefinition.of(ChartType.LINE)
				.dates("d-mmm", LocalDate.of(2026, 1, 1), LocalDate.of(2026, 1, 8), LocalDate.of(2026, 1, 15), LocalDate.of(2026, 1, 22))
				.series("Data", 3, 5, 4, 6).legend(LegendPosition.BOTTOM), "date categories in d-mmm"));
		l.add(probe("R5", "r5-categories-mdyyyy", ChartDefinition.of(ChartType.LINE)
				.dates("m/d/yyyy", LocalDate.of(2026, 1, 1), LocalDate.of(2026, 1, 8), LocalDate.of(2026, 1, 15), LocalDate.of(2026, 1, 22))
				.series("Data", 3, 5, 4, 6).legend(LegendPosition.BOTTOM), "date categories in m/d/yyyy"));

		// R6 data labels at each position each type allows
		Object[][] positions = { { col, new STDLblPos[] { STDLblPos.CTR, STDLblPos.IN_END, STDLblPos.IN_BASE, STDLblPos.OUT_END } },
				{ ChartType.COLUMN_STACKED, new STDLblPos[] { STDLblPos.CTR, STDLblPos.IN_END, STDLblPos.IN_BASE } },
				{ ChartType.LINE_MARKERS, new STDLblPos[] { STDLblPos.T, STDLblPos.B, STDLblPos.L, STDLblPos.R, STDLblPos.CTR } },
				{ ChartType.PIE, new STDLblPos[] { STDLblPos.BEST_FIT, STDLblPos.OUT_END, STDLblPos.IN_END, STDLblPos.CTR } } };
		for (Object[] p : positions) {
			ChartType t = (ChartType) p[0];
			for (STDLblPos pos : (STDLblPos[]) p[1]) {
				l.add(probe("R6", "r6-" + t.getTemplateName() + "-" + pos.value(), words(t).dataLabels(true),
						"data labels at " + pos.value() + " on the " + t.getTemplateName()).tweak(c -> labelPosition(c, pos)));
			}
		}

		// R7 blanks
		for (ChartType t : new ChartType[] { ChartType.LINE_MARKERS, col }) {
			for (STDispBlanksAs b : STDispBlanksAs.values()) {
				l.add(probe("R7", "r7-" + t.getTemplateName() + "-" + b.value(), ChartDefinition.of(t).categories("A", "B", "C", "D", "E")
						.series("Data", Arrays.asList(3.0, 5.0, null, 4.0, 6.0)).legend(LegendPosition.BOTTOM),
						"a blank third point, dispBlanksAs " + b.value()).tweak(c -> blanks(c, b)));
			}
		}

		// R8 pie and doughnut
		ChartType pie = ChartType.PIE;
		l.add(probe("R8", "r8-pie-first-slice-90", words(pie), "first slice angle 90").tweak(c -> firstSlice(c, 90)));
		l.add(probe("R8", "r8-pie-explode-point", words(pie), "the second point exploded 20%").tweak(c -> explodePoint(c, 1, 20)));
		l.add(probe("R8", "r8-pie-explode-all", words(pie), "the series exploded 10%").tweak(c -> explodeSeries(c, 10)));
		ChartDefinition twelve = ChartDefinition.of(pie).categories("Jan", "Feb", "Mar", "Apr", "May", "Jun", "Jul", "Aug", "Sep",
				"Oct", "Nov", "Dec").series("Sales", 12, 11, 10, 9, 8, 7, 6, 5, 4, 3, 2, 1);
		l.add(probe("R8", "r8-pie-twelve", twelve, "twelve slices: the colour cycle's variations"));
		l.add(probe("R8", "r8-pie-labels-outside", words(pie).dataLabels(true),
				"labels outside: category name and percentage, outEnd, leader lines").tweak(c -> {
					labelPosition(c, STDLblPos.OUT_END);
					for (CTDLbls d : dLbls(c)) {
						d.setShowVal(bool(false));
						d.setShowCatName(bool(true));
						d.setShowPercent(bool(true));
					}
				}));
		l.add(probe("R8", "r8-doughnut-hole-75", words(ChartType.DOUGHNUT), "hole size 75").tweak(c -> holeSize(c, 75)));
		l.add(probe("R8", "r8-doughnut-first-slice-180", words(ChartType.DOUGHNUT), "first slice angle 180")
				.tweak(c -> firstSlice(c, 180)));

		// R9 manual layout
		l.add(probe("R9", "r9-title-moved", column(), "the title moved to x 0.05, y 0.02 (edge)")
				.tweak(c -> titleLayout(c, manual(null, 0.05, 0.02, null, null))));
		l.add(probe("R9", "r9-legend-moved", column(), "the legend at x 0.70, y 0.10, 0.25 by 0.30 (edge)")
				.tweak(c -> legendLayout(c, manual(null, 0.70, 0.10, 0.25, 0.30))));
		l.add(probe("R9", "r9-plot-inner", column(), "the plot area's inner box at x 0.15, y 0.20, 0.60 by 0.55")
				.tweak(c -> plotLayout(c, manual(STLayoutTarget.INNER, 0.15, 0.20, 0.60, 0.55))));
		l.add(probe("R9", "r9-plot-outer", column(), "the plot area's outer box at x 0.15, y 0.20, 0.60 by 0.55")
				.tweak(c -> plotLayout(c, manual(STLayoutTarget.OUTER, 0.15, 0.20, 0.60, 0.55))));

		// R10 Excel's gallery styles, applied by Excel and Word on the VM (style in the manifest)
		for (int s : new int[] { 202, 204, 207, 209, 213 }) {
			l.add(probe("R10", "r10-column-style-" + s, column(), "Excel's gallery style " + s + " applied by Office").style(s));
		}
		for (int s : new int[] { 254, 258 }) {
			l.add(probe("R10", "r10-pie-style-" + s, words(pie), "Excel's gallery style " + s + " applied by Office").style(s));
		}

		// R11 (CR-037 phase 1) the value axis's unit against the axis's length: one chart at many
		// heights (Word's column, eight categories, one series with no title or legend so the plot
		// has the height), the column's text at 14pt, and a bar chart (its value axis level) at
		// many widths
		int[] heights = { 72, 90, 108, 126, 144, 162, 180, 198, 216, 234, 252, 288, 324, 360, 432 };
		for (int h : heights) {
			l.add(probe("R11", "r11-default-h" + h, column(), "Word's column, 432 by " + h + "pt").size(W, h));
		}
		for (int h : heights) {
			l.add(probe("R11", "r11-eight-h" + h, categories(8, "Category ", 3), "eight categories, 432 by " + h + "pt").size(W, h));
		}
		for (int h : heights) {
			l.add(probe("R11", "r11-bare-h" + h, ChartDefinition.of(col).categories("A", "B", "C", "D").series("Data", 12, 45, 78, 93)
					.noTitle().noLegend(), "12 to 93, no title or legend, 432 by " + h + "pt").size(W, h));
		}
		for (int h : new int[] { 108, 144, 180, 216, 252, 288, 360, 432 }) {
			l.add(probe("R11", "r11-text14-h" + h, column(), "Word's column, its axes' and legend's text 14pt, 432 by " + h + "pt")
					.size(W, h).xml(x -> textSize(x, 1400)));
		}
		for (int w : new int[] { 144, 180, 216, 252, 288, 360, 432, 504, 576, 648 }) {
			l.add(probe("R11", "r11-bar-w" + w, words(ChartType.BAR_CLUSTERED), "Word's bar chart, " + w + " by 252pt").size(w, H));
		}

		// R12 (CR-037 phase 1) the category labels as the room for them shrinks: kept on a line,
		// wrapped, turned, cut short or overlapping (R3's small charts do each); by width, by
		// height, and labels of one word, which can't wrap
		for (int n : new int[] { 4, 6, 8, 12 }) {
			for (int w : new int[] { 144, 180, 216, 288, 360 }) {
				l.add(probe("R12", "r12-cats" + n + "-w" + w, categories(n, "Category ", 2), n + " categories, " + w + " by 252pt")
						.size(w, H));
			}
		}
		for (int n : new int[] { 4, 8 }) {
			int w = n == 4 ? 180 : 216;
			for (int h : new int[] { 108, 144, 180, 216 }) {
				l.add(probe("R12", "r12-cats" + n + "-w" + w + "-h" + h, categories(n, "Category ", 2),
						n + " categories, " + w + " by " + h + "pt").size(w, h));
			}
		}
		for (int w : new int[] { 180, 216, 288 }) {
			l.add(probe("R12", "r12-words-w" + w, ChartDefinition.of(col).categories("Northeastern", "Southwestern", "Mountainous",
					"Archipelagic").series("Series 1", 4.3, 2.5, 3.5, 4.5).series("Series 2", 2.4, 4.4, 1.8, 2.8),
					"one-word categories, " + w + " by 252pt").size(w, H));
		}

		// R13 (CR-037 phase 1) how far labels turned 45 degrees push the plot's inside right: eight
		// categories in 216pt (turned 45), one thing changed at a time
		String[] first = { "A", "Categ", "Category 1", "Category 1 and more", "Category one with a long name" };
		String[] firstNames = { "a", "categ", "category-1", "and-more", "long-name" };
		for (int i = 0; i < first.length; i++) {
			l.add(probe("R13", "r13-first-" + firstNames[i], turned(first[i], "Category 8", 8, 1), "the first label \"" + first[i]
					+ "\", turned 45 degrees, 216 by 252pt").size(216, H));
		}
		String[] last = { "H", "Category 8 with a long name" };
		String[] lastNames = { "h", "long-name" };
		for (int i = 0; i < last.length; i++) {
			l.add(probe("R13", "r13-last-" + lastNames[i], turned("Category 1", last[i], 8, 1), "the last label \"" + last[i]
					+ "\", turned 45 degrees, 216 by 252pt").size(216, H));
		}
		for (int n : new int[] { 6, 10 }) {
			l.add(probe("R13", "r13-n" + n + "-w216", turned("Category 1", "Category " + n, n, 1), n + " categories turned 45 degrees, 216 by 252pt")
					.size(216, H));
		}
		for (int w : new int[] { 240, 252 }) {
			l.add(probe("R13", "r13-n8-w" + w, turned("Category 1", "Category 8", 8, 1), "8 categories turned 45 degrees, " + w + " by 252pt")
					.size(w, H));
		}
		for (int scale : new int[] { 100, 10000 }) {
			l.add(probe("R13", "r13-values-x" + scale, turned("Category 1", "Category 8", 8, scale),
					"values times " + scale + " (wider value labels), turned 45 degrees, 216 by 252pt").size(216, H));
		}
		for (int sz : new int[] { 700, 1200 }) {
			l.add(probe("R13", "r13-text-" + sz / 100, turned("Category 1", "Category 8", 8, 1),
					"the axes' and legend's text at " + sz / 100 + "pt, turned 45 degrees, 216 by 252pt").size(216, H)
					.xml(x -> textSize(x, sz)));
		}
		r14(l);
		r15(l);
		r16(l);
		r17(l);
		return l;
	}

	/**
	 * R14 (CR-037 phase 2): the rules the other types add.  Sweeps (one chart resized step by step
	 * in Excel, its axes read at each size: a horizontal value axis's unit against its labels'
	 * width and size; the category labels' ladder on a text and a date axis); legends (a line
	 * series' key at three sizes, the positions not yet probed, the order of a vertical legend, a
	 * legend too wide for a row); pies at other shapes; markers, smoothed lines, a scatter chart's
	 * lines; date axes (gaps, weekdays, years, a major unit set, columns over dates); stacks with
	 * negative values, stacked lines and areas; tick marks; kerning pairs; axis number formats; and Excel's own
	 * charts of CR-036 (P1's twelve types and the date axis, P6's dates and shapes), copied as they
	 * are, an xlsx only
	 */
	private static void r14(List<ChartRenderProbes> l) {
		ChartType col = ChartType.COLUMN_CLUSTERED, bar = ChartType.BAR_CLUSTERED;
		String widths = "width:120:660:3";
		for (int scale : new int[] { 1, 100, 10000 }) {
			l.add(probe("R14", "r14-sweep-bar-x" + scale, scaled(words(bar), scale), "Word's bar chart, values times " + scale
					+ ", swept across widths 120 to 660pt").sweep(widths));
		}
		for (int sz : new int[] { 700, 1200 }) {
			l.add(probe("R14", "r14-sweep-bar-text-" + sz / 100, words(bar), "Word's bar chart, its text at " + sz / 100
					+ "pt, swept across widths").sweep(widths).xml(x -> textSize(x, sz)));
		}
		l.add(probe("R14", "r14-sweep-bar-x100-text-12", scaled(words(bar), 100), "Word's bar chart, values times 100, text 12pt, swept across widths")
				.sweep(widths).xml(x -> textSize(x, 1200)));
		l.add(probe("R14", "r14-sweep-bar-100", words(ChartType.BAR_STACKED_100), "the 100% bar chart (0% to 100%), swept across widths")
				.sweep(widths));
		l.add(probe("R14", "r14-sweep-scatter", words(ChartType.SCATTER), "Word's scatter chart (its x axis), swept across widths").sweep(widths));
		l.add(probe("R14", "r14-sweep-column-h", column(), "Word's column, swept across heights 60 to 420pt").sweep("height:60:420:2"));
		l.add(probe("R14", "r14-sweep-cats8", categories(8, "Category ", 2), "eight categories, swept across widths (the labels' ladder)")
				.sweep(widths));
		LocalDate[] days = new LocalDate[22];
		for (int i = 0; i < days.length; i++) days[i] = LocalDate.of(2026, 1, 1).plusDays(i);
		double[] dayValues = new double[days.length];
		for (int i = 0; i < days.length; i++) dayValues[i] = 3 + (i * 7 % 5);
		l.add(probe("R14", "r14-sweep-dates", ChartDefinition.of(ChartType.LINE).dates("d-mmm", days).series("Data", dayValues)
				.legend(LegendPosition.BOTTOM), "22 days on a date axis, swept across widths (its labels' ladder)").sweep(widths));

		// legends
		for (int sz : new int[] { 700, 1200, 1600 }) {
			l.add(probe("R14", "r14-legend-line-" + sz / 100, words(ChartType.LINE_MARKERS), "the line chart's legend (line keys) at " + sz / 100
					+ "pt").xml(x -> textSize(x, sz)));
		}
		for (LegendPosition pos : new LegendPosition[] { LegendPosition.TOP, LegendPosition.LEFT, LegendPosition.TOP_RIGHT }) {
			l.add(probe("R14", "r14-legend-" + pos.name().toLowerCase().replace('_', '-'), column().legend(pos), "the legend at "
					+ pos.name().toLowerCase().replace('_', ' ')));
		}
		for (ChartType t : new ChartType[] { ChartType.COLUMN_STACKED, bar, ChartType.BAR_STACKED, ChartType.AREA, ChartType.LINE }) {
			l.add(probe("R14", "r14-legend-right-" + t.getTemplateName(), words(t).legend(LegendPosition.RIGHT), "the " + t.getTemplateName()
					+ " chart's legend at the right: its entries' order"));
		}
		ChartDefinition eight = ChartDefinition.of(col).categories("A", "B", "C");
		for (int s = 1; s <= 8; s++) eight.series("Series " + s, s, s + 1, s + 2);
		l.add(probe("R14", "r14-legend-rows-8", eight, "eight entries at the bottom of a 288pt chart: more than a row holds").size(288, H));
		ChartDefinition five = ChartDefinition.of(col).categories("A", "B", "C");
		for (int s = 1; s <= 5; s++) five.series("Series " + s, s, s + 1, s + 2);
		l.add(probe("R14", "r14-legend-rows-5", five, "five entries at the bottom of a 216pt chart").size(216, H));

		// pies
		ChartType pie = ChartType.PIE;
		l.add(probe("R14", "r14-pie-wide", words(pie), "a pie in a wide chart, 432 by 144pt").size(W, 144));
		l.add(probe("R14", "r14-pie-tall", words(pie), "a pie in a tall chart, 216 by 288pt").size(216, 288));
		l.add(probe("R14", "r14-pie-legend-right", words(pie).legend(LegendPosition.RIGHT), "a pie, its legend at the right"));
		l.add(probe("R14", "r14-doughnut-two", words(ChartType.DOUGHNUT).series("Costs", 3.1, 2.2, 1.9, 0.8), "a doughnut of two series (rings)"));
		l.add(probe("R14", "r14-pie-explode-both", words(pie), "the series exploded 10% and the third point 25%").tweak(c -> {
			explodeSeries(c, 10);
			explodePoint(c, 2, 25);
		}));

		// markers and lines
		String[] symbols = { "circle", "square", "diamond", "triangle", "x", "star", "dash", "dot", "plus" };
		ChartDefinition marked = ChartDefinition.of(ChartType.LINE_MARKERS).categories("A", "B", "C", "D");
		for (int i = 0; i < symbols.length; i++) marked.series(symbols[i], i + 1, i + 1.5, i + 1.2, i + 1.8);
		l.add(probe("R14", "r14-markers-symbols", marked, "each marker symbol, size 7").tweak(c -> markers(c, symbols, 7)));
		l.add(probe("R14", "r14-markers-sizes", words(ChartType.LINE_MARKERS), "markers of sizes 3, 9 and 15")
				.tweak(c -> markerSizes(c, 3, 9, 15)));
		l.add(probe("R14", "r14-line-smooth", words(ChartType.LINE), "smoothed lines").tweak(c -> smooth(c)));
		l.add(probe("R14", "r14-scatter-lines", words(ChartType.SCATTER), "a scatter chart with its line shown").tweak(c -> scatterLine(c)));
		l.add(probe("R14", "r14-scatter-smooth", words(ChartType.SCATTER), "a scatter chart, its line shown and smoothed").tweak(c -> {
			scatterLine(c);
			smooth(c);
		}));

		// date axes
		l.add(probe("R14", "r14-dates-months-gap", ChartDefinition.of(ChartType.LINE).dates("mmm-yy", LocalDate.of(2026, 1, 1),
				LocalDate.of(2026, 2, 1), LocalDate.of(2026, 5, 1), LocalDate.of(2026, 6, 1), LocalDate.of(2026, 7, 1)).series("Data", 3, 5, 4, 6, 2)
				.legend(LegendPosition.BOTTOM), "months with a gap (March, April missing)"));
		List<LocalDate> weekdays = new ArrayList<>();
		for (LocalDate d = LocalDate.of(2026, 3, 2); weekdays.size() < 15; d = d.plusDays(1)) {
			if (d.getDayOfWeek().getValue() <= 5) weekdays.add(d);
		}
		double[] wv = new double[weekdays.size()];
		for (int i = 0; i < wv.length; i++) wv[i] = 2 + (i * 3 % 7);
		l.add(probe("R14", "r14-dates-weekdays", ChartDefinition.of(ChartType.LINE).dates("d-mmm", weekdays.toArray(new LocalDate[0]))
				.series("Data", wv).legend(LegendPosition.BOTTOM), "fifteen weekdays (weekends missing)"));
		LocalDate[] years = new LocalDate[7];
		for (int i = 0; i < years.length; i++) years[i] = LocalDate.of(2020 + i, 1, 1);
		l.add(probe("R14", "r14-dates-years", ChartDefinition.of(ChartType.LINE).dates("yyyy", years).series("Data", 3, 5, 4, 6, 2, 7, 5)
				.legend(LegendPosition.BOTTOM), "January 1st of seven years"));
		LocalDate[] months = new LocalDate[12];
		for (int i = 0; i < months.length; i++) months[i] = LocalDate.of(2026, 1 + i, 1);
		double[] mv = { 32, 30, 28, 25, 20, 18, 15, 17, 21, 24, 27, 31 };
		l.add(probe("R14", "r14-dates-major-2", ChartDefinition.of(ChartType.AREA).dates("mmm", months).series("Data", mv)
				.legend(LegendPosition.BOTTOM), "twelve months, the major unit 2 months").tweak(c -> dateMajor(c, 2, STTimeUnit.MONTHS)));
		l.add(probe("R14", "r14-dates-column", ChartDefinition.of(col).dates("mmm-yy", LocalDate.of(2026, 1, 1), LocalDate.of(2026, 2, 1),
				LocalDate.of(2026, 3, 1), LocalDate.of(2026, 4, 1), LocalDate.of(2026, 5, 1), LocalDate.of(2026, 6, 1)).series("Data", 3, 5, 4, 6, 2, 4)
				.legend(LegendPosition.BOTTOM), "columns over six months (a date axis)"));
		l.add(probe("R14", "r14-dates-long", ChartDefinition.of(ChartType.LINE).dates("d mmmm yyyy", days).series("Data", dayValues)
				.legend(LegendPosition.BOTTOM), "22 days in a long format"));

		// stacks
		ChartDefinition mixed = ChartDefinition.of(ChartType.COLUMN_STACKED).categories(CATS).series("Series 1", 4.3, -2.5, 3.5, 4.5)
				.series("Series 2", -2.4, 4.4, 1.8, -2.8).series("Series 3", 2, 2, -3, 5);
		l.add(probe("R14", "r14-stacked-negative", mixed, "a stacked column with negative values"));
		ChartDefinition mixed100 = ChartDefinition.of(ChartType.COLUMN_STACKED_100).categories(CATS).series("Series 1", 4.3, -2.5, 3.5, 4.5)
				.series("Series 2", -2.4, 4.4, 1.8, -2.8).series("Series 3", 2, 2, -3, 5);
		l.add(probe("R14", "r14-stacked-100-negative", mixed100, "a 100% stacked column with negative values"));
		l.add(probe("R14", "r14-line-stacked", words(ChartType.LINE_MARKERS), "a stacked line chart").tweak(c -> grouping(c, STGrouping.STACKED)));
		l.add(probe("R14", "r14-area-stacked", words(ChartType.AREA), "a stacked area chart").tweak(c -> grouping(c, STGrouping.STACKED)));
		l.add(probe("R14", "r14-area-stacked-100", words(ChartType.AREA), "a 100% stacked area chart")
				.tweak(c -> grouping(c, STGrouping.PERCENT_STACKED)));

		// tick marks, both axes, the value axis's line shown
		for (STTickMark t : new STTickMark[] { STTickMark.OUT, STTickMark.IN, STTickMark.CROSS }) {
			l.add(probe("R14", "r14-ticks-" + t.value(), column(), "tick marks " + t.value() + " on both axes, the value axis's line shown")
					.tweak(c -> ticks(c, t)));
		}

		// automatic colours: series with no fill of their own, Office's cycle through the colour
		// style (accents, then their variations), and without one; a pie's points with none
		ChartDefinition auto = ChartDefinition.of(col).categories("A", "B", "C");
		for (int s = 1; s <= 8; s++) auto.series("Series " + s, s, s + 1, s + 2);
		l.add(probe("R14", "r14-auto-colours", auto, "eight series with no fill of their own: the colour style's cycle")
				.tweak(c -> noFills(c)));
		ChartDefinition auto2 = ChartDefinition.of(col).categories("A", "B", "C");
		for (int s = 1; s <= 8; s++) auto2.series("Series " + s, s, s + 1, s + 2);
		l.add(probe("R14", "r14-auto-colours-no-style", auto2, "eight series with no fill of their own, no colour style part")
				.tweak(c -> noFills(c)).noColourStyle());
		ChartDefinition twelve = ChartDefinition.of(pie).categories("Jan", "Feb", "Mar", "Apr", "May", "Jun", "Jul", "Aug", "Sep",
				"Oct", "Nov", "Dec").series("Sales", 12, 11, 10, 9, 8, 7, 6, 5, 4, 3, 2, 1);
		l.add(probe("R14", "r14-auto-pie", twelve, "twelve slices with no fill of their own (varyColors)").tweak(c -> noFills(c)));

		// kerning: which of Carlito's pairs Office applies (r1-scatter's title: "Va" but not "Y-"),
		// each pair a category label at 14pt, measured one by one in Word's PDF
		String[] pairs = { "Y-", "T-", "-Y", "AV", "Va", "Ty", "To", "LT", "P.", "F,", "r.", "y,", "Yo", "AT", "LY", "f." };
		double[] pv = new double[pairs.length];
		for (int i = 0; i < pv.length; i++) pv[i] = 1 + i % 5;
		l.add(probe("R14", "r14-kerning", ChartDefinition.of(col).categories(pairs).series("Data", pv).legend(LegendPosition.BOTTOM),
				"kerning pairs as category labels at 14pt, 720 by 252pt").size(720, H).xml(x -> textSize(x, 1400)));

		// axis number formats (no data labels)
		String[][] formats = { { "percent", "0%" }, { "thousands", "#,##0" }, { "currency", "\"$\"#,##0.00" }, { "decimal", "0.0" } };
		for (String[] f : formats) {
			double[] v = f[1].contains("%") ? new double[] { 0.125, 0.25, 0.4, 0.6 } : new double[] { 1234.5, 2500.25, 1800, 3100.75 };
			l.add(probe("R14", "r14-format-" + f[0], one(col, v).valueAxis(a -> a.numberFormat(f[1])), "the value axis in " + f[1]));
		}

		// Excel's own charts (CR-036): P1's types, P6's dates and shapes, as Excel made them
		File[] files = authoring.listFiles();
		if (files == null) throw new IllegalStateException("no CR-036 charts in " + authoring);
		Arrays.sort(files);
		Pattern own = Pattern.compile("p1-.*|p6-.*-(dates|one-by-one|two-by-two|eight-series|eight-points|numbers|text|title-text)|p6-range-.*");
		for (File f : files) {
			String n = f.getName();
			if (!n.endsWith(".xlsx") || !own.matcher(n.replace(".xlsx", "")).matches()) continue;
			// Excel's chart at its size, 5 by 3 inches
			l.add(copy("R14", f, 360, 216, "Excel's own chart (CR-036 " + n.substring(0, 2).toUpperCase() + "), its layout"));
		}
	}

	/**
	 * R15 (CR-037 phase 4): what the settings and the data add.  Axis titles (each type, each axis,
	 * beside each legend, long, larger, level); data labels (the positions R6 left: bars, areas,
	 * scatter, doughnuts; their contents, separators, legend keys, a point's own, one deleted;
	 * negative values; a pie's best fit and outside labels over twelve slices, a label moved);
	 * manual layouts in factor mode and with right and bottom edges, a pie's, an axis title's;
	 * legend entries deleted, overlays; axis options (deleted, crossing at a value or the maximum,
	 * reversed, tick labels high, low and none, minor gridlines and tick marks); the chart area's
	 * and plot area's fills and lines, gradients, patterns, shadows, transparency, dashes; titles of
	 * several runs; invertIfNegative; varyColors on one series.  And Excel's own charts of CR-036's
	 * P2 and P6 settings (axis titles, data labels, number formats on every type), an xlsx only
	 */
	private static void r15(List<ChartRenderProbes> l) {
		ChartType col = ChartType.COLUMN_CLUSTERED, bar = ChartType.BAR_CLUSTERED, pie = ChartType.PIE;

		// axis titles
		l.add(probe("R15", "r15-axis-titles-column", column().categoryAxis(a -> a.title("Axis Title")).valueAxis(a -> a.title("Axis Title")),
				"both axis titles on the column chart"));
		for (ChartType t : new ChartType[] { bar, ChartType.LINE_MARKERS, ChartType.SCATTER, ChartType.AREA, ChartType.COLUMN_STACKED_100 }) {
			l.add(probe("R15", "r15-axis-titles-" + t.getTemplateName(), words(t).categoryAxis(a -> a.title("Axis Title"))
					.valueAxis(a -> a.title("Axis Title")), "both axis titles on the " + t.getTemplateName() + " chart"));
		}
		l.add(probe("R15", "r15-axis-title-value", column().valueAxis(a -> a.title("Units sold")), "the value axis's title alone"));
		l.add(probe("R15", "r15-axis-title-category", column().categoryAxis(a -> a.title("Quarter")), "the category axis's title alone"));
		for (LegendPosition pos : new LegendPosition[] { LegendPosition.RIGHT, LegendPosition.LEFT, LegendPosition.TOP }) {
			String n = pos.name().toLowerCase();
			l.add(probe("R15", "r15-axis-titles-legend-" + n, column().legend(pos).categoryAxis(a -> a.title("Axis Title"))
					.valueAxis(a -> a.title("Axis Title")), "both axis titles, the legend at the " + n));
		}
		l.add(probe("R15", "r15-axis-title-long", column().valueAxis(a -> a.title("Sales in thousands of dollars, all regions and all products"))
				.categoryAxis(a -> a.title("Quarters of the financial year ending June 2026, all regions and all products and services")),
				"axis titles longer than their axes"));
		l.add(probe("R15", "r15-axis-titles-14", column().categoryAxis(a -> a.title("Axis Title")).valueAxis(a -> a.title("Axis Title")),
				"both axis titles at 14pt").tweak(c -> axisTitleSize(c, 1400)));
		l.add(probe("R15", "r15-axis-title-level", column().valueAxis(a -> a.title("Units")), "the value axis's title set level (rot 0)")
				.tweak(c -> axisTitleRot(c, 0)));
		l.add(probe("R15", "r15-axis-titles-small", column().categoryAxis(a -> a.title("Axis Title")).valueAxis(a -> a.title("Axis Title")),
				"both axis titles at 288 by 168pt").size(288, 168));

		// data labels: the positions R6 left, on the other types (each type's default where none is said)
		for (STDLblPos pos : new STDLblPos[] { STDLblPos.OUT_END, STDLblPos.IN_END, STDLblPos.CTR, STDLblPos.IN_BASE }) {
			l.add(probe("R15", "r15-labels-bar-" + pos.value(), words(bar).dataLabels(true), "data labels at " + pos.value() + " on the bar chart")
					.tweak(c -> labelPosition(c, pos)));
		}
		for (ChartType t : new ChartType[] { ChartType.BAR_STACKED, ChartType.COLUMN_STACKED_100, ChartType.AREA, ChartType.LINE, ChartType.DOUGHNUT,
				ChartType.SCATTER }) {
			l.add(probe("R15", "r15-labels-" + t.getTemplateName(), words(t).dataLabels(true), "data labels on the " + t.getTemplateName()
					+ " chart, at its default place"));
		}
		l.add(probe("R15", "r15-labels-scatter-t", words(ChartType.SCATTER).dataLabels(true), "data labels above the scatter chart's points")
				.tweak(c -> labelPosition(c, STDLblPos.T)));
		l.add(probe("R15", "r15-labels-negative-outEnd", one(col, 4.3, -2.5, 3.5, -1.5).dataLabels(true), "data labels at outEnd on negative columns")
				.tweak(c -> labelPosition(c, STDLblPos.OUT_END)));
		l.add(probe("R15", "r15-labels-negative-inBase", one(col, 4.3, -2.5, 3.5, -1.5).dataLabels(true), "data labels at inBase on negative columns")
				.tweak(c -> labelPosition(c, STDLblPos.IN_BASE)));
		l.add(probe("R15", "r15-labels-negative-bar", one(bar, 4.3, -2.5, 3.5, -1.5).dataLabels(true), "data labels at outEnd on negative bars")
				.tweak(c -> labelPosition(c, STDLblPos.OUT_END)));
		// their contents, on one series
		ChartDefinition sales = one(col, 4.3, 2.5, 3.5, 4.5);
		l.add(probe("R15", "r15-labels-series-name", one(col, 4.3, 2.5, 3.5, 4.5).dataLabels(true), "data labels: the series' name")
				.tweak(c -> labelContent(c, false, false, true, false, false, null)));
		l.add(probe("R15", "r15-labels-category-name", one(col, 4.3, 2.5, 3.5, 4.5).dataLabels(true), "data labels: the category's name")
				.tweak(c -> labelContent(c, true, false, false, false, false, null)));
		l.add(probe("R15", "r15-labels-value-category", one(col, 4.3, 2.5, 3.5, 4.5).dataLabels(true), "data labels: category and value, Excel's separator")
				.tweak(c -> labelContent(c, true, true, false, false, false, null)));
		l.add(probe("R15", "r15-labels-separator-newline", one(col, 4.3, 2.5, 3.5, 4.5).dataLabels(true),
				"data labels: category and value on two lines").tweak(c -> labelContent(c, true, true, false, false, false, "\n")));
		l.add(probe("R15", "r15-labels-separator-semicolon", one(col, 4.3, 2.5, 3.5, 4.5).dataLabels(true),
				"data labels: category and value, separated by \"; \"").tweak(c -> labelContent(c, true, true, false, false, false, "; ")));
		l.add(probe("R15", "r15-labels-all-three", sales.dataLabels(true), "data labels: series, category and value")
				.tweak(c -> labelContent(c, true, true, true, false, false, null)));
		l.add(probe("R15", "r15-labels-legend-key", one(col, 4.3, 2.5, 3.5, 4.5).dataLabels(true), "data labels: the value with its legend key")
				.tweak(c -> labelContent(c, false, true, false, false, true, null)));
		l.add(probe("R15", "r15-labels-pie-everything", words(pie).dataLabels(true), "a pie's labels: category, value and percentage, best fit")
				.tweak(c -> labelContent(c, true, true, false, true, false, null)));
		l.add(probe("R15", "r15-labels-pie-percent", words(pie).dataLabels(true), "a pie's labels: the percentage alone, best fit")
				.tweak(c -> labelContent(c, false, false, false, true, false, null)));
		ChartDefinition twelve = ChartDefinition.of(pie).categories("Jan", "Feb", "Mar", "Apr", "May", "Jun", "Jul", "Aug", "Sep", "Oct", "Nov",
				"Dec").series("Sales", 40, 25, 12, 8, 5, 3, 2, 1.5, 1, 1, 0.5, 0.5);
		l.add(probe("R15", "r15-labels-pie-twelve-bestFit", twelve.dataLabels(true), "twelve slices, small ones last, labels at best fit"));
		ChartDefinition twelve2 = ChartDefinition.of(pie).categories("Jan", "Feb", "Mar", "Apr", "May", "Jun", "Jul", "Aug", "Sep", "Oct", "Nov",
				"Dec").series("Sales", 40, 25, 12, 8, 5, 3, 2, 1.5, 1, 1, 0.5, 0.5);
		l.add(probe("R15", "r15-labels-pie-twelve-outEnd", twelve2.dataLabels(true), "twelve slices, labels outside (outEnd)")
				.tweak(c -> labelPosition(c, STDLblPos.OUT_END)));
		l.add(probe("R15", "r15-labels-pie-moved", words(pie).dataLabels(true), "a pie's labels outside, the second moved out by hand (a leader line)")
				.tweak(c -> {
					labelPosition(c, STDLblPos.OUT_END);
					movedLabel(c, 1, 0.08, 0.06);
				}));
		l.add(probe("R15", "r15-labels-text-12", one(col, 4.3, 2.5, 3.5, 4.5).dataLabels(true), "data labels at 12pt")
				.tweak(c -> labelSize(c, 1200)));
		l.add(probe("R15", "r15-labels-one-point", one(col, 4.3, 2.5, 3.5, 4.5), "the third point's label alone").tweak(c -> onePointLabel(c, 2)));
		l.add(probe("R15", "r15-labels-one-deleted", one(col, 4.3, 2.5, 3.5, 4.5).dataLabels(true), "every point's label but the second's")
				.tweak(c -> deletedLabel(c, 1)));
		l.add(probe("R15", "r15-labels-boxed", one(col, 4.3, 2.5, 3.5, 4.5).dataLabels(true), "data labels bold, in a filled and outlined box")
				.tweak(c -> boxedLabels(c)));
		l.add(probe("R15", "r15-labels-linked-format", one(col, 1234.5, 2500.25, 1800, 3100.75).dataLabels(true),
				"the values' cells in #,##0.00, the labels and the axis linked to them").tweak(c -> linkedFormat(c, "#,##0.00")));

		// manual layouts: factor mode (from where Office would put it), right and bottom edges
		l.add(probe("R15", "r15-title-factor", column(), "the title moved by x 0.10, y 0.05 (factor)")
				.tweak(c -> titleLayout(c, manualModes(null, 0.10, 0.05, null, null, false, false))));
		l.add(probe("R15", "r15-legend-factor", column(), "the legend moved by x -0.20, y -0.10 (factor)")
				.tweak(c -> legendLayout(c, manualModes(null, -0.20, -0.10, null, null, false, false))));
		l.add(probe("R15", "r15-legend-right-sized", column().legend(LegendPosition.RIGHT), "the legend at the right, 0.20 by 0.50 (edge)")
				.tweak(c -> legendLayout(c, manual(null, 0.75, 0.25, 0.20, 0.50))));
		l.add(probe("R15", "r15-plot-inner-factor", column(), "the plot area's inner box moved by x 0.05, y 0.05, 0.50 by 0.50 (factor)")
				.tweak(c -> plotLayout(c, manualModes(STLayoutTarget.INNER, 0.05, 0.05, 0.50, 0.50, false, false))));
		l.add(probe("R15", "r15-plot-inner-right-bottom", column(), "the plot area's inner box from x 0.15, y 0.20 to its right 0.85, bottom 0.75 (edge)")
				.tweak(c -> plotLayout(c, manualModes(STLayoutTarget.INNER, 0.15, 0.20, 0.85, 0.75, true, true))));
		l.add(probe("R15", "r15-plot-pie", words(pie), "a pie's plot area at x 0.10, y 0.20, 0.40 by 0.70 (edge)")
				.tweak(c -> plotLayout(c, manual(STLayoutTarget.INNER, 0.10, 0.20, 0.40, 0.70))));
		l.add(probe("R15", "r15-plot-outer-axis-titles", column().categoryAxis(a -> a.title("Axis Title")).valueAxis(a -> a.title("Axis Title")),
				"axis titles, the plot area's outer box at x 0.15, y 0.20, 0.60 by 0.55").tweak(
						c -> plotLayout(c, manual(STLayoutTarget.OUTER, 0.15, 0.20, 0.60, 0.55))));
		l.add(probe("R15", "r15-axis-title-moved", column().valueAxis(a -> a.title("Axis Title")), "the value axis's title moved to x 0.02, y 0.30 (edge)")
				.tweak(c -> valAx(c).getTitle().setLayout(manual(null, 0.02, 0.30, null, null))));

		// legends
		l.add(probe("R15", "r15-legend-entry-deleted", column(), "the second series' legend entry deleted").tweak(c -> deleteLegendEntry(c, 1)));
		l.add(probe("R15", "r15-legend-entry-deleted-pie", words(pie), "the first slice's legend entry deleted").tweak(c -> deleteLegendEntry(c, 0)));
		l.add(probe("R15", "r15-legend-overlay", column().legend(LegendPosition.RIGHT), "the legend at the right over the plot area (overlay)")
				.tweak(c -> c.getChart().getLegend().setOverlay(bool(true))));
		l.add(probe("R15", "r15-title-overlay", column(), "the title over the plot area (overlay)")
				.tweak(c -> c.getChart().getTitle().setOverlay(bool(true))));

		// axis options
		ChartDefinition signs = one(col, 4.3, -2.5, 3.5, -1.5);
		l.add(probe("R15", "r15-value-axis-deleted", column(), "the value axis deleted").tweak(c -> valAx(c).setDelete(bool(true))));
		l.add(probe("R15", "r15-category-axis-deleted", column(), "the category axis deleted").tweak(c -> catAx(c).setDelete(bool(true))));
		l.add(probe("R15", "r15-catax-crosses-at-2", column(), "the category axis's crossesAt 2").tweak(c -> crossesAt(catAx(c), 2)));
		l.add(probe("R15", "r15-valax-crosses-at-3", column(), "the value axis's crossesAt 3").tweak(c -> crossesAt(valAx(c), 3)));
		l.add(probe("R15", "r15-catax-crosses-max", column(), "the category axis's crosses max").tweak(c -> crosses(catAx(c), STCrosses.MAX)));
		l.add(probe("R15", "r15-valax-crosses-max", column(), "the value axis's crosses max").tweak(c -> crosses(valAx(c), STCrosses.MAX)));
		l.add(probe("R15", "r15-category-reversed", column(), "the categories in reverse order").tweak(c -> reversed(catAx(c).getScaling())));
		l.add(probe("R15", "r15-value-reversed", column(), "the values in reverse order").tweak(c -> reversed(valAx(c).getScaling())));
		l.add(probe("R15", "r15-bar-category-reversed", words(bar), "a bar chart's categories in reverse order (from the top)")
				.tweak(c -> reversed(catAx(c).getScaling())));
		String[][] lbl = { { "catax-low", "low" }, { "catax-high", "high" }, { "catax-none", "none" }, { "valax-high", "high" }, { "valax-none", "none" } };
		for (String[] k : lbl) {
			l.add(probe("R15", "r15-ticklabels-" + k[0], one(col, 4.3, -2.5, 3.5, -1.5), "values of either sign, the " + k[0].substring(0, 5)
					+ "'s tick labels " + k[1]).tweak(c -> tickLabels(k[0].startsWith("cat") ? catAx(c) : valAx(c), STTickLblPos.fromValue(k[1]))));
		}
		l.add(probe("R15", "r15-gridlines-minor", column(), "the value axis's minor gridlines, the category axis's major ones").tweak(c -> {
			valAx(c).setMinorGridlines(gridlines("D9D9D9", 9525));
			catAx(c).setMajorGridlines(gridlines("BFBFBF", 9525));
		}));
		l.add(probe("R15", "r15-ticks-minor", column(), "minor tick marks out on both axes, the value axis's line shown").tweak(c -> {
			ticks(c, STTickMark.OUT);
			CTTickMark m = new CTTickMark();
			m.setVal(STTickMark.OUT);
			valAx(c).setMinorTickMark(m);
			CTTickMark m2 = new CTTickMark();
			m2.setVal(STTickMark.CROSS);
			catAx(c).setMinorTickMark(m2);
		}));
		l.add(probe("R15", "r15-no-gridlines", column(), "the value axis's gridlines removed").tweak(c -> valAx(c).setMajorGridlines(null)));

		// fills, lines and effects
		l.add(probe("R15", "r15-chart-area-fill", column(), "the chart area filled F2F2F2, a 2pt accent 1 border, rounded corners").tweak(c -> {
			c.setSpPr(frag(org.docx4j.dml.CTShapeProperties.class, "<a:solidFill><a:srgbClr val=\"F2F2F2\"/></a:solidFill>"
					+ "<a:ln w=\"25400\"><a:solidFill><a:schemeClr val=\"accent1\"/></a:solidFill></a:ln>"));
			c.setRoundedCorners(bool(true));
		}));
		l.add(probe("R15", "r15-plot-area-fill", column(), "the plot area filled DEEBF7, a 1pt grey border").tweak(c -> plot(c).setSpPr(frag(
				org.docx4j.dml.CTShapeProperties.class, "<a:solidFill><a:srgbClr val=\"DEEBF7\"/></a:solidFill>"
						+ "<a:ln w=\"12700\"><a:solidFill><a:srgbClr val=\"7F7F7F\"/></a:solidFill></a:ln>"))));
		l.add(probe("R15", "r15-chart-area-gradient", column(), "the chart area a linear gradient, top to bottom").tweak(c -> c.setSpPr(frag(
				org.docx4j.dml.CTShapeProperties.class, "<a:gradFill rotWithShape=\"1\"><a:gsLst><a:gs pos=\"0\"><a:srgbClr val=\"FFFFFF\"/></a:gs>"
						+ "<a:gs pos=\"100000\"><a:srgbClr val=\"BDD7EE\"/></a:gs></a:gsLst><a:lin ang=\"5400000\" scaled=\"0\"/></a:gradFill>"
						+ "<a:ln><a:noFill/></a:ln>"))));
		l.add(probe("R15", "r15-gradients", column(), "the series' fills: a linear gradient down, one of three stops across, a radial one")
				.tweak(c -> seriesFills(c,
						"<a:gradFill rotWithShape=\"1\"><a:gsLst><a:gs pos=\"0\"><a:schemeClr val=\"accent1\"><a:lumMod val=\"40000\"/><a:lumOff val=\"60000\"/>"
								+ "</a:schemeClr></a:gs><a:gs pos=\"100000\"><a:schemeClr val=\"accent1\"/></a:gs></a:gsLst><a:lin ang=\"5400000\" scaled=\"0\"/></a:gradFill>",
						"<a:gradFill rotWithShape=\"1\"><a:gsLst><a:gs pos=\"0\"><a:srgbClr val=\"FFC000\"/></a:gs><a:gs pos=\"50000\"><a:srgbClr val=\"ED7D31\"/>"
								+ "</a:gs><a:gs pos=\"100000\"><a:srgbClr val=\"C00000\"/></a:gs></a:gsLst><a:lin ang=\"0\" scaled=\"0\"/></a:gradFill>",
						"<a:gradFill rotWithShape=\"1\"><a:gsLst><a:gs pos=\"0\"><a:srgbClr val=\"FFFFFF\"/></a:gs><a:gs pos=\"100000\"><a:schemeClr val=\"accent3\"/>"
								+ "</a:gs></a:gsLst><a:path path=\"circle\"><a:fillToRect l=\"50000\" t=\"50000\" r=\"50000\" b=\"50000\"/></a:path></a:gradFill>")));
		l.add(probe("R15", "r15-patterns", column(), "the series' fills: patterns pct50, dkDnDiag, smCheck").tweak(c -> seriesFills(c,
				"<a:pattFill prst=\"pct50\"><a:fgClr><a:schemeClr val=\"accent1\"/></a:fgClr><a:bgClr><a:schemeClr val=\"bg1\"/></a:bgClr></a:pattFill>",
				"<a:pattFill prst=\"dkDnDiag\"><a:fgClr><a:schemeClr val=\"accent2\"/></a:fgClr><a:bgClr><a:schemeClr val=\"bg1\"/></a:bgClr></a:pattFill>",
				"<a:pattFill prst=\"smCheck\"><a:fgClr><a:schemeClr val=\"accent3\"/></a:fgClr><a:bgClr><a:srgbClr val=\"FFFF00\"/></a:bgClr></a:pattFill>")));
		l.add(probe("R15", "r15-shadows", column(), "the series' outer shadows (Excel's offset bottom preset)").tweak(c -> seriesEffects(c,
				"<a:outerShdw blurRad=\"57150\" dist=\"19050\" dir=\"5400000\" algn=\"ctr\" rotWithShape=\"0\"><a:srgbClr val=\"000000\"><a:alpha val=\"63000\"/>"
						+ "</a:srgbClr></a:outerShdw>")));
		l.add(probe("R15", "r15-transparent", column(), "the series' fills 50% transparent, overlapping by 50%").tweak(c -> {
			seriesFills(c, "<a:solidFill><a:schemeClr val=\"accent1\"><a:alpha val=\"50000\"/></a:schemeClr></a:solidFill>",
					"<a:solidFill><a:schemeClr val=\"accent2\"><a:alpha val=\"50000\"/></a:schemeClr></a:solidFill>",
					"<a:solidFill><a:schemeClr val=\"accent3\"><a:alpha val=\"50000\"/></a:schemeClr></a:solidFill>");
			org.docx4j.dml.chart.CTOverlap o = new org.docx4j.dml.chart.CTOverlap();
			o.setVal((byte) 50);
			((org.docx4j.dml.chart.CTBarChart) group(c)).setOverlap(o);
		}));
		l.add(probe("R15", "r15-bar-outlines", column(), "the series' bars outlined, 1.5pt, in dark blue").tweak(c -> seriesLines(c,
				"<a:ln w=\"19050\"><a:solidFill><a:srgbClr val=\"1F3864\"/></a:solidFill></a:ln>")));
		l.add(probe("R15", "r15-line-dashes", words(ChartType.LINE), "the lines dashed (sysDash, dash, lgDashDot), 3.5pt").tweak(c -> lineDashes(c,
				new String[] { "sysDash", "dash", "lgDashDot" }, 44450)));
		l.add(probe("R15", "r15-gridlines-dashed", column(), "the gridlines dashed and coloured").tweak(c -> valAx(c).setMajorGridlines(
				frag(CTChartLines.class, "<c:spPr><a:ln w=\"9525\"><a:solidFill><a:srgbClr val=\"FF0000\"/></a:solidFill><a:prstDash val=\"dash\"/></a:ln></c:spPr>"))));
		l.add(probe("R15", "r15-pie-gradient", words(pie), "a pie's points: radial gradients").tweak(c -> pointFills(c,
				"<a:gradFill rotWithShape=\"1\"><a:gsLst><a:gs pos=\"0\"><a:srgbClr val=\"FFFFFF\"/></a:gs><a:gs pos=\"100000\"><a:schemeClr val=\"accent%d\"/>"
						+ "</a:gs></a:gsLst><a:path path=\"circle\"><a:fillToRect l=\"50000\" t=\"50000\" r=\"50000\" b=\"50000\"/></a:path></a:gradFill>")));

		// titles
		l.add(probe("R15", "r15-title-runs", column().title("Sales 2026"), "a title of two runs, \"Sales \" bold and \"2026\" italic in C00000")
				.tweak(c -> titleRuns(c)));
		l.add(probe("R15", "r15-title-paragraphs", column().title("Sales 2026"), "a title of two paragraphs").tweak(c -> titleParagraph(c, "by quarter")));
		l.add(probe("R15", "r15-title-20", column().title("Sales 2026"), "a title at 20pt").tweak(c -> titleSize(c, 2000)));

		// series
		l.add(probe("R15", "r15-invert-negative", one(col, 4.3, -2.5, 3.5, -1.5), "invertIfNegative on a series of either sign")
				.tweak(c -> invertIfNegative(c)));
		l.add(probe("R15", "r15-vary-colors-column", one(col, 4.3, 2.5, 3.5, 4.5), "varyColors on a single series' columns")
				.tweak(c -> varyColors(c)));
		l.add(probe("R15", "r15-vary-colors-bar", one(bar, 4.3, 2.5, 3.5, 4.5), "varyColors on a single series' bars").tweak(c -> varyColors(c)));
		l.add(probe("R15", "r15-vary-colors-line", one(ChartType.LINE_MARKERS, 4.3, 2.5, 3.5, 4.5), "varyColors on a single series' line with markers")
				.tweak(c -> varyColors(c)));
		l.add(probe("R15", "r15-line-markers-only", words(ChartType.LINE_MARKERS), "lines with markers, the lines not drawn (noFill)")
				.tweak(c -> seriesLines(c, "<a:ln><a:noFill/></a:ln>")));

		// Excel's own charts (CR-036): P2's settings on the column, P6's axis titles, data labels and
		// number formats on the other types; and CR-035's chart with a series filtered out
		File[] files = authoring.listFiles();
		Arrays.sort(files);
		Pattern own = Pattern.compile("p2-.*|p6-.*-(axis-titles|data-labels|number-format)");
		for (File f : files) {
			String n = f.getName();
			if (!n.endsWith(".xlsx") || !own.matcher(n.replace(".xlsx", "")).matches()) continue;
			l.add(copy("R15", f, 360, 216, "Excel's own chart (CR-036 " + n.substring(0, 2).toUpperCase() + "), its layout"));
		}
		l.add(copy("R15", new File(authoring.getParentFile(), "p2-filtered.xlsx"), "c35-filtered", 360, 216,
				"Excel's own chart with a series filtered out (CR-035's p2-filtered), its layout"));
	}

	/**
	 * R16 (CR-037 phase 4): where Office puts a pie's label outside it (outEnd) and at best fit, its
	 * rule apart from its collision avoidance.  Each pie has one label, on its small first slice (10
	 * of 100), the slice turned by the first slice angle so that the label's angle goes round the
	 * pie by 15 degrees, its text the same throughout; then two other label sizes at eight angles;
	 * best fit over five slice widths (where it leaves the slice); and the pie's shrink for labels
	 * outside at four more chart shapes, and a doughnut's
	 */
	private static void r16(List<ChartRenderProbes> l) {
		ChartType pie = ChartType.PIE;
		for (String pos : new String[] { "outEnd", "bestFit" }) {
			for (int mid = 0; mid < 360; mid += 15) {
				final int first = Math.floorMod(mid - 18, 360);
				l.add(probe("R16", "r16-" + pos + "-a" + mid, twoSlices(10), "one label (10) at " + pos + ", its slice's middle at " + mid
						+ " degrees").tweak(c -> {
							oneLabel(c, 0, pos, false, true, false);
							firstSlice(c, first);
						}));
			}
		}
		for (int mid = 0; mid < 360; mid += 45) {
			final int first = Math.floorMod(mid - 18, 360);
			l.add(probe("R16", "r16-outEnd-two-lines-a" + mid, twoSlices(10), "one label of two lines (category, percentage) at outEnd, at "
					+ mid + " degrees").tweak(c -> {
						oneLabel(c, 0, "outEnd", true, false, true);
						firstSlice(c, first);
					}));
			l.add(probe("R16", "r16-outEnd-12pt-a" + mid, twoSlices(10), "one label (10) at 12pt at outEnd, at " + mid + " degrees").tweak(c -> {
				oneLabel(c, 0, "outEnd", false, true, false);
				labelSize(c, 1200);
				firstSlice(c, first);
			}));
		}
		// best fit: inside the slice while the label fits, outside (with a leader line?) where it doesn't
		for (int share : new int[] { 3, 6, 15, 25, 40 }) {
			final int first = Math.floorMod(90 - share * 360 / 200, 360);
			l.add(probe("R16", "r16-bestFit-share-" + share, twoSlices(share), "one label at bestFit on a slice of " + share
					+ "%, its middle at 90 degrees").tweak(c -> {
						oneLabel(c, 0, "bestFit", false, true, false);
						firstSlice(c, first);
					}));
		}
		l.add(probe("R16", "r16-bestFit-share-15-long", twoSlices(15), "one label at bestFit on a slice of 15%: category, value and percentage")
				.tweak(c -> {
					oneLabel(c, 0, "bestFit", true, true, true);
					firstSlice(c, Math.floorMod(90 - 27, 360));
				}));
		// the shrink for labels outside, at other chart shapes (r6-pie-outEnd at 432 by 252, p6 at 360 by 216)
		int[][] shapes = { { 288, 168 }, { 576, 336 }, { 432, 144 }, { 216, 288 } };
		for (int[] sh : shapes) {
			l.add(probe("R16", "r16-shrink-" + sh[0] + "x" + sh[1], words(pie).dataLabels(true), "Word's pie, labels at outEnd, " + sh[0] + " by "
					+ sh[1] + "pt").size(sh[0], sh[1]).tweak(c -> labelPosition(c, STDLblPos.OUT_END)));
		}
		l.add(probe("R16", "r16-shrink-doughnut-288x168", words(ChartType.DOUGHNUT).dataLabels(true), "Word's doughnut with labels, 288 by 168pt")
				.size(288, 168));
	}

	/**
	 * R17 (CR-037 phase 4, the review's design): where Office puts a pie's label inside its slice at
	 * best fit, one thing changed at a time.  The label shows the slice's category name, so its text
	 * stays the same while the slice changes: the slice's width from 2% to 50% with its middle at 90
	 * and at 0 degrees; the label's width ("1" to "10000", "1.5%", two words) on a 10% slice; its
	 * text size (6, 12, 18pt); the chart's height swept (labels read at each size); two adjacent
	 * labelled slices; an exploded slice; and a 25% slice turned round the pie by 30 degrees
	 */
	private static void r17(List<ChartRenderProbes> l) {
		for (int mid : new int[] { 90, 0 }) {
			for (int share = 2; share <= 50; share += 2) {
				final int first = Math.floorMod((int) Math.round(mid - 1.8 * share), 360);
				l.add(probe("R17", "r17-share-" + share + "-a" + mid, labelledSlice(share, "10"), "best fit, the label \"10\" on a " + share
						+ "% slice, its middle at " + mid + " degrees").tweak(c -> {
							oneLabel(c, 0, "bestFit", true, false, false);
							firstSlice(c, first);
						}));
			}
			final int first10 = Math.floorMod(mid - 18, 360);
			String[] texts = { "1", "100", "1000", "10000", "1.5%", "Alpha beta" };
			String[] names = { "1", "100", "1000", "10000", "pct", "words" };
			for (int k = 0; k < texts.length; k++) {
				l.add(probe("R17", "r17-text-" + names[k] + "-a" + mid, labelledSlice(10, texts[k]), "best fit, the label \"" + texts[k]
						+ "\" on a 10% slice, at " + mid + " degrees").tweak(c -> {
							oneLabel(c, 0, "bestFit", true, false, false);
							firstSlice(c, first10);
						}));
			}
			for (int sz : new int[] { 600, 1200, 1800 }) {
				l.add(probe("R17", "r17-size-" + sz / 100 + "-a" + mid, labelledSlice(10, "10"), "best fit, the label \"10\" at " + sz / 100
						+ "pt on a 10% slice, at " + mid + " degrees").tweak(c -> {
							oneLabel(c, 0, "bestFit", true, false, false);
							labelSize(c, sz);
							firstSlice(c, first10);
						}));
			}
		}
		l.add(probe("R17", "r17-sweep-height", labelledSlice(10, "10"), "best fit, the label \"10\" on a 10% slice at 90 degrees, swept across heights")
				.sweep("height:120:420:3").tweak(c -> {
					oneLabel(c, 0, "bestFit", true, false, false);
					firstSlice(c, 72);
				}));
		l.add(probe("R17", "r17-adjacent", ChartDefinition.of(ChartType.PIE).categories("10", "20", "Rest").series("Sales", 4, 4, 92).dataLabels(true),
				"best fit, two adjacent 4% slices labelled \"10\" and \"20\", around 90 degrees").tweak(c -> {
					oneLabel(c, 0, "bestFit", true, false, false);
					oneLabel(c, 1, "bestFit", true, false, false);
					firstSlice(c, 83);
				}));
		l.add(probe("R17", "r17-exploded", labelledSlice(10, "10"), "best fit, the label \"10\" on a 10% slice exploded 20%, at 90 degrees")
				.tweak(c -> {
					oneLabel(c, 0, "bestFit", true, false, false);
					explodePoint(c, 0, 20);
					firstSlice(c, 72);
				}));
		for (int mid = 0; mid < 360; mid += 30) {
			final int first = Math.floorMod(mid - 45, 360);
			l.add(probe("R17", "r17-wide-a" + mid, labelledSlice(25, "10"), "best fit, the label \"10\" on a 25% slice, its middle at " + mid
					+ " degrees").tweak(c -> {
						oneLabel(c, 0, "bestFit", true, false, false);
						firstSlice(c, first);
					}));
		}
	}

	/** A pie of two slices, the first share percent named as given, the rest "Rest", with Excel's data labels */
	private static ChartDefinition labelledSlice(int share, String name) {
		return ChartDefinition.of(ChartType.PIE).categories(name, "Rest").series("Sales", share, 100 - share).dataLabels(true);
	}

	/** A pie of two slices, the first share percent ("Alpha"), the rest ("Rest"), with Excel's data labels (each probe chooses what shows) */
	private static ChartDefinition twoSlices(int share) {
		return ChartDefinition.of(ChartType.PIE).categories("Alpha", "Rest").series("Sales", share, 100 - share).dataLabels(true);
	}

	/**
	 * One point's label alone: the series' labels (Excel's, from dataLabels) showing nothing, a c:dLbl
	 * for the point showing what is asked at the position (its style the series' labels')
	 */
	private static void oneLabel(CTChartSpace c, int idx, String pos, boolean category, boolean value, boolean percent) {
		CTDLbls d = dLbls(c).get(0);
		d.setShowVal(bool(false));
		d.setShowCatName(bool(false));
		d.setShowPercent(bool(false));
		d.setShowSerName(bool(false));
		CTDLbl one = frag(CTDLbl.class, "<c:idx val=\"" + idx + "\"/><c:dLblPos val=\"" + pos + "\"/><c:showLegendKey val=\"0\"/><c:showVal val=\""
				+ (value ? 1 : 0) + "\"/><c:showCatName val=\"" + (category ? 1 : 0) + "\"/><c:showSerName val=\"0\"/><c:showPercent val=\""
				+ (percent ? 1 : 0) + "\"/><c:showBubbleSize val=\"0\"/>");
		d.getDLbl().add(one);
	}

	private static final String NS = " xmlns:c=\"http://schemas.openxmlformats.org/drawingml/2006/chart\""
			+ " xmlns:a=\"http://schemas.openxmlformats.org/drawingml/2006/main\"";

	/** A fragment of a chart's XML as its JAXB type (its root element's name doesn't matter) */
	@SuppressWarnings("unchecked")
	private static <T> T frag(Class<T> type, String inner) {
		try {
			return (T) XmlUtils.unmarshalString("<c:x" + NS + ">" + inner + "</c:x>", Context.jc, type);
		} catch (Exception e) {
			throw new IllegalStateException(e);
		}
	}

	private static CTCatAx catAx(CTChartSpace c) {
		for (Object o : plot(c).getValAxOrCatAxOrDateAx()) if (o instanceof CTCatAx) return (CTCatAx) o;
		throw new IllegalStateException("no category axis");
	}

	/** The value axis of a chart with one, else (a scatter chart) its first */
	private static List<CTTitle> axisTitles(CTChartSpace c) {
		List<CTTitle> l = new ArrayList<>();
		for (Object o : plot(c).getValAxOrCatAxOrDateAx()) {
			CTTitle t = o instanceof CTValAx ? ((CTValAx) o).getTitle() : o instanceof CTCatAx ? ((CTCatAx) o).getTitle()
					: o instanceof CTDateAx ? ((CTDateAx) o).getTitle() : null;
			if (t != null) l.add(t);
		}
		return l;
	}

	/** Each axis title's text size (its rich text's paragraph defaults, and its txPr's) */
	private static void axisTitleSize(CTChartSpace c, int sz) {
		for (CTTitle t : axisTitles(c)) {
			for (org.docx4j.dml.CTTextBody b : new org.docx4j.dml.CTTextBody[] { t.getTx() == null ? null : t.getTx().getRich(), t.getTxPr() }) {
				if (b == null) continue;
				for (org.docx4j.dml.CTTextParagraph p : b.getP()) if (p.getPPr() != null && p.getPPr().getDefRPr() != null) p.getPPr().getDefRPr().setSz(sz);
			}
		}
	}

	/** The value axis's title's rotation (60000ths of a degree) */
	private static void axisTitleRot(CTChartSpace c, int rot) {
		CTTitle t = valAx(c).getTitle();
		for (org.docx4j.dml.CTTextBody b : new org.docx4j.dml.CTTextBody[] { t.getTx() == null ? null : t.getTx().getRich(), t.getTxPr() }) {
			if (b != null && b.getBodyPr() != null) b.getBodyPr().setRot(rot);
		}
	}

	/** Every series' labels showing these (the others off), and the separator where one is given */
	private static void labelContent(CTChartSpace c, boolean category, boolean value, boolean series, boolean percent, boolean key, String sep) {
		for (CTDLbls d : dLbls(c)) {
			d.setShowCatName(bool(category));
			d.setShowVal(bool(value));
			d.setShowSerName(bool(series));
			d.setShowPercent(bool(percent));
			d.setShowLegendKey(bool(key));
			if (sep != null) d.setSeparator(sep);
		}
	}

	private static void labelSize(CTChartSpace c, int sz) {
		for (CTDLbls d : dLbls(c)) {
			if (d.getTxPr() == null) continue;
			for (org.docx4j.dml.CTTextParagraph p : d.getTxPr().getP()) if (p.getPPr() != null && p.getPPr().getDefRPr() != null) p.getPPr().getDefRPr().setSz(sz);
		}
	}

	/** One point's label moved by hand (factor: from where Office would put it), the series' at outEnd */
	private static void movedLabel(CTChartSpace c, int idx, double x, double y) {
		CTDLbls d = dLbls(c).get(0);
		CTDLbl l = frag(CTDLbl.class, "<c:idx val=\"" + idx + "\"/><c:layout><c:manualLayout><c:x val=\"" + x + "\"/><c:y val=\"" + y
				+ "\"/></c:manualLayout></c:layout><c:dLblPos val=\"outEnd\"/><c:showLegendKey val=\"0\"/><c:showVal val=\"1\"/>"
				+ "<c:showCatName val=\"0\"/><c:showSerName val=\"0\"/><c:showPercent val=\"0\"/><c:showBubbleSize val=\"0\"/>");
		d.getDLbl().add(l);
		d.setShowLeaderLines(bool(true));
	}

	/** One point's label alone (a c:dLbl showing its value, the series' labels showing nothing) */
	private static void onePointLabel(CTChartSpace c, int idx) {
		for (Object g : plot(c).getAreaChartOrArea3DChartOrLineChart()) {
			Object sers = invoke(g, "getSer");
			if (!(sers instanceof List)) continue;
			for (Object s : (List<?>) sers) {
				CTDLbls d = frag(CTDLbls.class, "<c:dLbl><c:idx val=\"" + idx + "\"/><c:spPr><a:noFill/><a:ln><a:noFill/></a:ln></c:spPr>"
						+ "<c:dLblPos val=\"outEnd\"/><c:showLegendKey val=\"0\"/><c:showVal val=\"1\"/><c:showCatName val=\"0\"/><c:showSerName val=\"0\"/>"
						+ "<c:showPercent val=\"0\"/><c:showBubbleSize val=\"0\"/></c:dLbl><c:spPr><a:noFill/><a:ln><a:noFill/></a:ln></c:spPr>"
						+ "<c:showLegendKey val=\"0\"/><c:showVal val=\"0\"/><c:showCatName val=\"0\"/><c:showSerName val=\"0\"/><c:showPercent val=\"0\"/>"
						+ "<c:showBubbleSize val=\"0\"/>");
				try {
					s.getClass().getMethod("setDLbls", CTDLbls.class).invoke(s, d);
				} catch (ReflectiveOperationException e) {
					throw new IllegalStateException(e);
				}
			}
		}
	}

	private static void deletedLabel(CTChartSpace c, int idx) {
		CTDLbl l = frag(CTDLbl.class, "<c:idx val=\"" + idx + "\"/><c:delete val=\"1\"/>");
		dLbls(c).get(0).getDLbl().add(l);
	}

	/** The labels bold, in a box filled FFF2CC and outlined in BF9000 */
	private static void boxedLabels(CTChartSpace c) {
		for (CTDLbls d : dLbls(c)) {
			d.setSpPr(frag(org.docx4j.dml.CTShapeProperties.class, "<a:solidFill><a:srgbClr val=\"FFF2CC\"/></a:solidFill>"
					+ "<a:ln w=\"9525\"><a:solidFill><a:srgbClr val=\"BF9000\"/></a:solidFill></a:ln>"));
			for (org.docx4j.dml.CTTextParagraph p : d.getTxPr().getP()) if (p.getPPr() != null && p.getPPr().getDefRPr() != null) p.getPPr().getDefRPr().setB(true);
		}
	}

	/** The values' cache in a format, the labels' and the value axis's linked to it */
	private static void linkedFormat(CTChartSpace c, String code) {
		for (Object g : plot(c).getAreaChartOrArea3DChartOrLineChart()) {
			Object sers = invoke(g, "getSer");
			if (!(sers instanceof List)) continue;
			for (Object s : (List<?>) sers) {
				Object val = invoke(s, "getVal");
				org.docx4j.dml.chart.CTNumRef ref = (org.docx4j.dml.chart.CTNumRef) invoke(val, "getNumRef");
				ref.getNumCache().setFormatCode(code);
			}
		}
		for (CTDLbls d : dLbls(c)) {
			CTNumFmt f = new CTNumFmt();
			f.setFormatCode("General");
			f.setSourceLinked(true);
			d.setNumFmt(f);
		}
		CTNumFmt f = new CTNumFmt();
		f.setFormatCode("General");
		f.setSourceLinked(true);
		valAx(c).setNumFmt(f);
	}

	/** A manual layout, x and y in factor mode unless edge, w and h as the right and bottom edges where asked */
	private static CTLayout manualModes(STLayoutTarget target, Double x, Double y, Double w, Double h, boolean xyEdge, boolean whEdge) {
		CTLayout l = manual(target, x, y, w, h);
		CTManualLayout m = l.getManualLayout();
		if (!xyEdge) {
			m.setXMode(null);
			m.setYMode(null);
		}
		if (whEdge) {
			m.setWMode(edge());
			m.setHMode(edge());
		}
		return l;
	}

	private static void deleteLegendEntry(CTChartSpace c, int idx) {
		org.docx4j.dml.chart.CTLegendEntry e = frag(org.docx4j.dml.chart.CTLegendEntry.class, "<c:idx val=\"" + idx + "\"/><c:delete val=\"1\"/>");
		c.getChart().getLegend().getLegendEntry().add(e);
	}

	private static void crossesAt(Object axis, double v) {
		try {
			axis.getClass().getMethod("setCrosses", CTCrosses.class).invoke(axis, (Object) null);
			axis.getClass().getMethod("setCrossesAt", CTDouble.class).invoke(axis, dbl(v));
		} catch (ReflectiveOperationException e) {
			throw new IllegalStateException(e);
		}
	}

	private static void crosses(Object axis, STCrosses v) {
		CTCrosses x = new CTCrosses();
		x.setVal(v);
		try {
			axis.getClass().getMethod("setCrosses", CTCrosses.class).invoke(axis, x);
		} catch (ReflectiveOperationException e) {
			throw new IllegalStateException(e);
		}
	}

	private static void reversed(CTScaling s) {
		org.docx4j.dml.chart.CTOrientation o = new org.docx4j.dml.chart.CTOrientation();
		o.setVal(org.docx4j.dml.chart.STOrientation.MAX_MIN);
		s.setOrientation(o);
	}

	private static void tickLabels(Object axis, STTickLblPos pos) {
		CTTickLblPos p = new CTTickLblPos();
		p.setVal(pos);
		try {
			axis.getClass().getMethod("setTickLblPos", CTTickLblPos.class).invoke(axis, p);
		} catch (ReflectiveOperationException e) {
			throw new IllegalStateException(e);
		}
	}

	private static CTChartLines gridlines(String rgb, int w) {
		return frag(CTChartLines.class, "<c:spPr><a:ln w=\"" + w + "\"><a:solidFill><a:srgbClr val=\"" + rgb + "\"/></a:solidFill></a:ln></c:spPr>");
	}

	/** The group's series' spPr */
	private static List<org.docx4j.dml.CTShapeProperties> seriesSpPr(CTChartSpace c) {
		List<org.docx4j.dml.CTShapeProperties> l = new ArrayList<>();
		Object sers = invoke(group(c), "getSer");
		for (Object s : (List<?>) sers) {
			Object sp = invoke(s, "getSpPr");
			if (sp == null) {
				sp = new org.docx4j.dml.CTShapeProperties();
				try {
					s.getClass().getMethod("setSpPr", org.docx4j.dml.CTShapeProperties.class).invoke(s, sp);
				} catch (ReflectiveOperationException e) {
					throw new IllegalStateException(e);
				}
			}
			l.add((org.docx4j.dml.CTShapeProperties) sp);
		}
		return l;
	}

	/** Each series' fill, in turn, from a:solidFill, a:gradFill or a:pattFill XML (its line kept) */
	private static void seriesFills(CTChartSpace c, String... fills) {
		List<org.docx4j.dml.CTShapeProperties> l = seriesSpPr(c);
		for (int i = 0; i < l.size(); i++) {
			org.docx4j.dml.CTShapeProperties sp = l.get(i), f = frag(org.docx4j.dml.CTShapeProperties.class, fills[i % fills.length]);
			sp.setNoFill(null);
			sp.setSolidFill(f.getSolidFill());
			sp.setGradFill(f.getGradFill());
			sp.setPattFill(f.getPattFill());
		}
	}

	private static void seriesEffects(CTChartSpace c, String effects) {
		for (org.docx4j.dml.CTShapeProperties sp : seriesSpPr(c)) sp.setEffectLst(frag(org.docx4j.dml.CTEffectList.class, effects));
	}

	private static void seriesLines(CTChartSpace c, String ln) {
		for (org.docx4j.dml.CTShapeProperties sp : seriesSpPr(c)) sp.setLn(frag(org.docx4j.dml.CTShapeProperties.class, ln).getLn());
	}

	private static void lineDashes(CTChartSpace c, String[] dashes, int w) {
		List<org.docx4j.dml.CTShapeProperties> l = seriesSpPr(c);
		for (int i = 0; i < l.size(); i++) {
			org.docx4j.dml.CTLineProperties ln = l.get(i).getLn();
			ln.setW(w);
			ln.setPrstDash(frag(org.docx4j.dml.CTShapeProperties.class, "<a:ln><a:prstDash val=\"" + dashes[i % dashes.length] + "\"/></a:ln>")
					.getLn().getPrstDash());
		}
	}

	/** Each of a pie's points' fills (%d: the accent, 1 to 6) */
	private static void pointFills(CTChartSpace c, String fill) {
		List<CTDPt> pts = pieSeries(c).getDPt();
		for (int i = 0; i < pts.size(); i++) {
			org.docx4j.dml.CTShapeProperties sp = pts.get(i).getSpPr(),
					f = frag(org.docx4j.dml.CTShapeProperties.class, String.format(fill, i % 6 + 1));
			sp.setSolidFill(null);
			sp.setGradFill(f.getGradFill());
		}
	}

	/** The title's run split in two: "Sales " bold, "2026" italic in C00000 */
	private static void titleRuns(CTChartSpace c) {
		org.docx4j.dml.CTTextParagraph p = c.getChart().getTitle().getTx().getRich().getP().get(0);
		p.getEGTextRun().clear();
		p.getEGTextRun().add(frag(org.docx4j.dml.CTRegularTextRun.class, "<a:rPr lang=\"en-US\" b=\"1\"/><a:t>Sales </a:t>"));
		p.getEGTextRun().add(frag(org.docx4j.dml.CTRegularTextRun.class,
				"<a:rPr lang=\"en-US\" i=\"1\"><a:solidFill><a:srgbClr val=\"C00000\"/></a:solidFill></a:rPr><a:t>2026</a:t>"));
	}

	private static void titleParagraph(CTChartSpace c, String text) {
		org.docx4j.dml.CTTextBody rich = c.getChart().getTitle().getTx().getRich();
		org.docx4j.dml.CTTextParagraph p = XmlUtils.deepCopy(rich.getP().get(0), Context.jc);
		p.getEGTextRun().clear();
		p.getEGTextRun().add(frag(org.docx4j.dml.CTRegularTextRun.class, "<a:rPr lang=\"en-US\"/><a:t>" + text + "</a:t>"));
		rich.getP().add(p);
	}

	private static void titleSize(CTChartSpace c, int sz) {
		org.docx4j.dml.CTTextParagraph p = c.getChart().getTitle().getTx().getRich().getP().get(0);
		p.getPPr().getDefRPr().setSz(sz);
		for (Object r : p.getEGTextRun()) {
			if (r instanceof org.docx4j.dml.CTRegularTextRun && ((org.docx4j.dml.CTRegularTextRun) r).getRPr() != null) {
				((org.docx4j.dml.CTRegularTextRun) r).getRPr().setSz(sz);
			}
		}
	}

	private static void invertIfNegative(CTChartSpace c) {
		for (Object s : (List<?>) invoke(group(c), "getSer")) {
			try {
				s.getClass().getMethod("setInvertIfNegative", CTBoolean.class).invoke(s, bool(true));
			} catch (ReflectiveOperationException e) {
				throw new IllegalStateException(e);
			}
		}
	}

	private static void varyColors(CTChartSpace c) {
		try {
			Object g = group(c);
			g.getClass().getMethod("setVaryColors", CTBoolean.class).invoke(g, bool(true));
		} catch (ReflectiveOperationException e) {
			throw new IllegalStateException(e);
		}
	}

	/** Every series' and point's own shape properties removed */
	private static void noFills(CTChartSpace c) {
		for (Object g : plot(c).getAreaChartOrArea3DChartOrLineChart()) {
			Object sers = invoke(g, "getSer");
			if (!(sers instanceof List)) continue;
			for (Object ser : (List<?>) sers) {
				try {
					ser.getClass().getMethod("setSpPr", org.docx4j.dml.CTShapeProperties.class).invoke(ser, (Object) null);
				} catch (ReflectiveOperationException e) {
					throw new IllegalStateException(e);
				}
				Object dPts = invoke(ser, "getDPt");
				if (dPts instanceof List) ((List<?>) dPts).clear();
			}
		}
	}

	/** Word's default data times scale */
	private static ChartDefinition scaled(ChartDefinition words, double scale) {
		if (scale == 1) return words;
		return ChartDefinition.of(ChartType.BAR_CLUSTERED).categories(CATS).series("Series 1", 4.3 * scale, 2.5 * scale, 3.5 * scale, 4.5 * scale)
				.series("Series 2", 2.4 * scale, 4.4 * scale, 1.8 * scale, 2.8 * scale).series("Series 3", 2 * scale, 2 * scale, 3 * scale, 5 * scale);
	}

	/** Each series' marker: its symbol, the size */
	private static void markers(CTChartSpace c, String[] symbols, int size) {
		List<CTLineSer> sers = ((CTLineChart) group(c)).getSer();
		for (int i = 0; i < sers.size(); i++) {
			CTMarker m = sers.get(i).getMarker();
			m.getSymbol().setVal(STMarkerStyle.fromValue(symbols[i]));
			m.getSize().setVal((short) size);
		}
	}

	private static void markerSizes(CTChartSpace c, int... sizes) {
		List<CTLineSer> sers = ((CTLineChart) group(c)).getSer();
		for (int i = 0; i < sers.size(); i++) sers.get(i).getMarker().getSize().setVal((short) sizes[i % sizes.length]);
	}

	private static void smooth(CTChartSpace c) {
		Object g = group(c);
		Object sers = invoke(g, "getSer");
		for (Object s : (List<?>) sers) {
			Object b = invoke(s, "getSmooth");
			if (b instanceof CTBoolean) ((CTBoolean) b).setVal(true);
		}
		if (g instanceof CTScatterChart) ((CTScatterChart) g).getScatterStyle().setVal(STScatterStyle.SMOOTH_MARKER);
	}

	/** A scatter chart's series' line shown (Word's has it, but with no fill) */
	private static void scatterLine(CTChartSpace c) {
		for (CTScatterSer s : ((CTScatterChart) group(c)).getSer()) {
			org.docx4j.dml.CTLineProperties ln = s.getSpPr().getLn();
			ln.setNoFill(null);
			ln.setW(19050);
			ln.setSolidFill(s.getMarker().getSpPr().getSolidFill());
		}
	}

	private static void grouping(CTChartSpace c, STGrouping g) {
		Object group = group(c);
		CTGrouping v = new CTGrouping();
		v.setVal(g);
		if (group instanceof CTLineChart) ((CTLineChart) group).setGrouping(v);
		else ((CTAreaChart) group).setGrouping(v);
		if (group instanceof CTLineChart) {
			// Excel's stacked line has its series overlapping (as its template); nothing else to set
		}
	}

	private static void dateMajor(CTChartSpace c, double unit, STTimeUnit timeUnit) {
		for (Object o : plot(c).getValAxOrCatAxOrDateAx()) {
			if (!(o instanceof CTDateAx)) continue;
			CTDateAx d = (CTDateAx) o;
			d.setMajorUnit(unit(unit));
			CTTimeUnit t = new CTTimeUnit();
			t.setVal(timeUnit);
			d.setMajorTimeUnit(t);
			return;
		}
		throw new IllegalStateException("no date axis");
	}

	/** Tick marks on both axes, the value axis's line drawn as the category axis's is */
	private static void ticks(CTChartSpace c, STTickMark t) {
		org.docx4j.dml.CTShapeProperties line = null;
		for (Object o : plot(c).getValAxOrCatAxOrDateAx()) {
			if (o instanceof CTCatAx) line = ((CTCatAx) o).getSpPr();
		}
		for (Object o : plot(c).getValAxOrCatAxOrDateAx()) {
			CTTickMark m = new CTTickMark();
			m.setVal(t);
			if (o instanceof CTCatAx) ((CTCatAx) o).setMajorTickMark(m);
			if (o instanceof CTValAx) {
				((CTValAx) o).setMajorTickMark(m);
				((CTValAx) o).setSpPr(XmlUtils.deepCopy(line, Context.jc));
			}
		}
	}

	/** n categories ("Category 2" ... between the first and last given), two series, values 1 to 5 times scale */
	private static ChartDefinition turned(String first, String last, int n, double scale) {
		String[] cats = new String[n];
		for (int i = 0; i < n; i++) cats[i] = i == 0 ? first : i == n - 1 ? last : "Category " + (i + 1);
		ChartDefinition d = ChartDefinition.of(ChartType.COLUMN_CLUSTERED).categories(cats);
		Random r = new Random(n * 131 + 7);
		for (int s = 0; s < 2; s++) {
			double[] v = new double[n];
			for (int i = 0; i < n; i++) v[i] = Math.round(10 + r.nextDouble() * 40) / 10.0 * scale;
			d.series("Series " + (s + 1), v);
		}
		return d;
	}

	private static ChartDefinition yearsDefinition() {
		double[] years = new double[12];
		double[] v = new double[12];
		for (int i = 0; i < 12; i++) {
			years[i] = 2015 + i;
			v[i] = 3 + (i * 7 % 5);
		}
		return ChartDefinition.of(ChartType.COLUMN_CLUSTERED).categories(years).series("Data", v).legend(LegendPosition.BOTTOM);
	}

	// ------------------------------------------------------------------ the tweaks

	private static final Pattern TXPR = Pattern.compile("<c:txPr>.*?</c:txPr>", Pattern.DOTALL);

	private static String noTxPr(String xml) {
		return TXPR.matcher(xml).replaceAll("");
	}

	private static final Pattern SZ = Pattern.compile("(<a:defRPr[^>]*\\ssz=\")(\\d+)(\")");

	/** Every text size but the title's (1400) set to sz */
	private static String textSize(String xml, int sz) {
		Matcher m = SZ.matcher(xml);
		StringBuilder b = new StringBuilder();
		while (m.find()) m.appendReplacement(b, Matcher.quoteReplacement(m.group(1) + ("1400".equals(m.group(2)) ? "1400" : String.valueOf(sz)) + m.group(3)));
		m.appendTail(b);
		return b.toString();
	}

	private static CTPlotArea plot(CTChartSpace c) {
		return c.getChart().getPlotArea();
	}

	private static CTValAx valAx(CTChartSpace c) {
		for (Object o : plot(c).getValAxOrCatAxOrDateAx()) if (o instanceof CTValAx) return (CTValAx) o;
		throw new IllegalStateException("no value axis");
	}

	private static CTScaling scaling(CTChartSpace c) {
		return valAx(c).getScaling();
	}

	private static CTDouble dbl(double v) {
		CTDouble d = new CTDouble();
		d.setVal(v);
		return d;
	}

	private static CTLogBase logBase(double v) {
		CTLogBase d = new CTLogBase();
		d.setVal(v);
		return d;
	}

	private static CTAxisUnit unit(double v) {
		CTAxisUnit d = new CTAxisUnit();
		d.setVal(v);
		return d;
	}

	private static CTBoolean bool(boolean v) {
		CTBoolean b = new CTBoolean();
		b.setVal(v);
		return b;
	}

	/** Every series' data labels */
	private static List<CTDLbls> dLbls(CTChartSpace c) {
		List<CTDLbls> l = new ArrayList<>();
		for (Object g : plot(c).getAreaChartOrArea3DChartOrLineChart()) {
			Object sers = invoke(g, "getSer");
			if (!(sers instanceof List)) continue;
			for (Object s : (List<?>) sers) {
				Object d = invoke(s, "getDLbls");
				if (d instanceof CTDLbls) l.add((CTDLbls) d);
			}
		}
		if (l.isEmpty()) throw new IllegalStateException("no data labels");
		return l;
	}

	private static void labelPosition(CTChartSpace c, STDLblPos pos) {
		for (CTDLbls d : dLbls(c)) {
			CTDLblPos p = new CTDLblPos();
			p.setVal(pos);
			d.setDLblPos(p);
		}
	}

	private static void labelFormat(CTChartSpace c, String code) {
		for (CTDLbls d : dLbls(c)) {
			CTNumFmt f = new CTNumFmt();
			f.setFormatCode(code);
			f.setSourceLinked(false);
			d.setNumFmt(f);
		}
	}

	private static void blanks(CTChartSpace c, STDispBlanksAs b) {
		CTDispBlanksAs d = new CTDispBlanksAs();
		d.setVal(b);
		c.getChart().setDispBlanksAs(d);
	}

	private static Object group(CTChartSpace c) {
		return plot(c).getAreaChartOrArea3DChartOrLineChart().get(0);
	}

	private static void firstSlice(CTChartSpace c, int angle) {
		CTFirstSliceAng a = new CTFirstSliceAng();
		a.setVal(angle);
		Object g = group(c);
		if (g instanceof CTPieChart) ((CTPieChart) g).setFirstSliceAng(a);
		else ((CTDoughnutChart) g).setFirstSliceAng(a);
	}

	private static void holeSize(CTChartSpace c, int size) {
		CTHoleSize s = new CTHoleSize();
		s.setVal((short) size);
		((CTDoughnutChart) group(c)).setHoleSize(s);
	}

	private static CTPieSer pieSeries(CTChartSpace c) {
		return ((CTPieChart) group(c)).getSer().get(0);
	}

	private static void explodeSeries(CTChartSpace c, int percent) {
		CTUnsignedInt e = new CTUnsignedInt();
		e.setVal(percent);
		pieSeries(c).setExplosion(e);
	}

	private static void explodePoint(CTChartSpace c, int idx, int percent) {
		for (CTDPt p : pieSeries(c).getDPt()) {
			if (p.getIdx().getVal() == idx) {
				CTUnsignedInt e = new CTUnsignedInt();
				e.setVal(percent);
				p.setExplosion(e);
				return;
			}
		}
		throw new IllegalStateException("no point " + idx);
	}

	private static CTLayout manual(STLayoutTarget target, Double x, Double y, Double w, Double h) {
		CTManualLayout m = new CTManualLayout();
		if (target != null) {
			CTLayoutTarget t = new CTLayoutTarget();
			t.setVal(target);
			m.setLayoutTarget(t);
		}
		if (x != null) {
			m.setXMode(edge());
			m.setX(dbl(x));
		}
		if (y != null) {
			m.setYMode(edge());
			m.setY(dbl(y));
		}
		if (w != null) m.setW(dbl(w));
		if (h != null) m.setH(dbl(h));
		CTLayout l = new CTLayout();
		l.setManualLayout(m);
		return l;
	}

	private static CTLayoutMode edge() {
		CTLayoutMode m = new CTLayoutMode();
		m.setVal(STLayoutMode.EDGE);
		return m;
	}

	private static void titleLayout(CTChartSpace c, CTLayout l) {
		c.getChart().getTitle().setLayout(l);
	}

	private static void legendLayout(CTChartSpace c, CTLayout l) {
		c.getChart().getLegend().setLayout(l);
	}

	private static void plotLayout(CTChartSpace c, CTLayout l) {
		plot(c).setLayout(l);
	}

	private static Object invoke(Object o, String method) {
		try {
			return o.getClass().getMethod(method).invoke(o);
		} catch (NoSuchMethodException e) {
			return null;
		} catch (Exception e) {
			throw new IllegalStateException(e);
		}
	}

	// ------------------------------------------------------------------ making the files

	private static String themeXml(String theme) throws Exception {
		Docx4jProperties.DefaultTheme t = Docx4jProperties.DefaultTheme.of(theme);
		try (InputStream is = Docx4jProperties.class.getClassLoader().getResourceAsStream(t.resource())) {
			return new String(is.readAllBytes(), StandardCharsets.UTF_8);
		}
	}

	private static org.docx4j.dml.Theme themeOf(String theme) throws Exception {
		return (org.docx4j.dml.Theme) XmlUtils.unwrap(XmlUtils.unmarshalString(themeXml(theme), Context.jcThemePart));
	}

	/** The chart's contents through the probe's tweaks (and its colour and style parts gone, where asked) */
	private void tweak(Chart chart) throws Exception {
		if (noColourStyle) {
			List<PartName> gone = new ArrayList<>();
			for (org.docx4j.relationships.Relationship r : chart.getRelationshipsPart().getRelationships().getRelationship()) {
				org.docx4j.openpackaging.parts.Part p = chart.getRelationshipsPart().getPart(r);
				if (p instanceof org.docx4j.openpackaging.parts.DrawingML.ChartColorStylePart
						|| p instanceof org.docx4j.openpackaging.parts.DrawingML.ChartStylePart) {
					gone.add(p.getPartName());
				}
			}
			for (PartName n : gone) {
				chart.getRelationshipsPart().removePart(n);
				// (removePart leaves the part's content type override behind)
				chart.getPackage().getContentTypeManager().removeContentType(n);
			}
		}
		CTChartSpace c = tweak.apply(chart.getContents());
		String xml = XmlUtils.marshaltoString(c, true, false, chart.getJAXBContext());
		String changed = xmlTweak.apply(xml);
		if (!changed.equals(xml)) {
			c = (CTChartSpace) XmlUtils.unwrap(XmlUtils.unmarshalString(changed, chart.getJAXBContext()));
		}
		chart.setContents(c);
	}

	private void xlsx(File dir) throws Exception {
		SpreadsheetMLPackage pkg = SpreadsheetMLPackage.createPackage();
		ThemePart theme = new ThemePart(new PartName("/xl/theme/theme1.xml"));
		pkg.getWorkbookPart().addTargetPart(theme);
		theme.setContents(themeOf(this.theme));
		WorksheetPart sheet = pkg.createWorksheetPart(new PartName("/xl/worksheets/sheet1.xml"), "Sheet1", 1);
		ChartRef r = new ChartCreator(new Random(name.hashCode())).inSheet(sheet, definition, new CellRef(null, 1, 1), null);
		Chart chart = (Chart) r.getChartPart();
		tweak(chart);
		pkg.save(new File(dir, name + ".xlsx"));
	}

	private void docx(File dir) throws Exception {
		// createPackage's theme part is the property's
		Docx4jProperties.setProperty(Docx4jProperties.DEFAULT_THEME, theme);
		WordprocessingMLPackage pkg = WordprocessingMLPackage.createPackage();
		R run = new ChartCreator(new Random(name.hashCode())).inDocument(pkg, definition, w * 12700L, h * 12700L);
		P p = (P) XmlUtils.unmarshalString("<w:p" + W_NS + "><w:pPr><w:spacing w:before=\"0\" w:after=\"0\" w:line=\"240\" "
				+ "w:lineRule=\"auto\"/></w:pPr></w:p>");
		p.getContent().add(run);
		run.setParent(p);
		pkg.getMainDocumentPart().getContent().add(p);
		p.setParent(pkg.getMainDocumentPart().getJaxbElement().getBody());
		// the page the chart's size with a half-inch margin, and room below for the paragraph mark
		pkg.getMainDocumentPart().getJaxbElement().getBody().setSectPr((SectPr) XmlUtils.unmarshalString("<w:sectPr" + W_NS
				+ "><w:pgSz w:w=\"" + (w + 2 * DOCX_AT) * 20 + "\" w:h=\"" + (h + 4 * DOCX_AT) * 20 + "\"/><w:pgMar w:top=\""
				+ DOCX_AT * 20 + "\" w:right=\"" + DOCX_AT * 20 + "\" w:bottom=\"" + DOCX_AT * 20 + "\" w:left=\"" + DOCX_AT * 20
				+ "\" w:header=\"0\" w:footer=\"0\" w:gutter=\"0\"/></w:sectPr>"));
		tweak((Chart) Charts.in(pkg).get(0).getChartPart());
		pkg.save(new File(dir, name + ".docx"));
	}

	private void write(File dir) throws Exception {
		if (copyOf != null) {
			java.nio.file.Files.copy(copyOf.toPath(), new File(dir, name + ".xlsx").toPath(), java.nio.file.StandardCopyOption.REPLACE_EXISTING);
			ROWS.add(String.join("\t", name, set, String.valueOf(w), String.valueOf(h), "xlsx", "", "", "", "", note, ""));
			return;
		}
		xlsx(dir);
		docx(dir);
		if (pptx) pptx(dir);
		ROWS.add(String.join("\t", name, set, String.valueOf(w), String.valueOf(h), pptx ? "xlsx,docx,pptx" : "xlsx,docx", theme,
				style == 0 ? "" : String.valueOf(style), DOCX_AT + "," + DOCX_AT, pptx ? PPTX_AT + "," + PPTX_AT : "", note, sweep));
	}

	private void pptx(File dir) throws Exception {
		PresentationMLPackage pkg = PresentationMLPackage.createPackage();
		pkg.getMainPresentationPart().getThemePart().setContents(themeOf(theme));
		SlidePart slide = PresentationMLPackage.createSlidePart(pkg.getMainPresentationPart(),
				(SlideLayoutPart) pkg.getParts().get(new PartName("/ppt/slideLayouts/slideLayout1.xml")),
				new PartName("/ppt/slides/slide1.xml"));
		CTGraphicalObjectFrame f = new ChartCreator(new Random(name.hashCode())).inSlide(slide, definition, PPTX_AT * 12700L,
				PPTX_AT * 12700L, w * 12700L, h * 12700L);
		tweak((Chart) ChartRef.of(slide, f).getChartPart());
		pkg.save(new File(dir, name + ".pptx"));
	}

	/** CR-036's oracle charts (Excel's own: P1, P6), which R14 copies */
	private static File authoring = new File("../docx4j-core-tests/src/test/resources/charts/authoring");

	public static void main(String[] args) throws Exception {
		File dir = new File(args[0]);
		dir.mkdirs();
		if (args.length > 1) authoring = new File(args[1]);
		List<ChartRenderProbes> probes = probes();
		for (ChartRenderProbes p : probes) {
			try {
				p.write(dir);
			} catch (Exception e) {
				throw new IllegalStateException(p.name + ": " + e.getMessage(), e);
			}
		}
		try (PrintWriter out = new PrintWriter(new File(dir, "probes.tsv"), StandardCharsets.UTF_8)) {
			out.println("name\tset\twidth\theight\thosts\ttheme\tstyle\tdocx_at\tpptx_at\tnote\tsweep");
			for (String r : ROWS) out.println(r);
		}
		System.out.println("wrote " + ROWS.size() + " probes into " + dir);
	}
}
