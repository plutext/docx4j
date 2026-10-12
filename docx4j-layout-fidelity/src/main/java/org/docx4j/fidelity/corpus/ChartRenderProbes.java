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
 * charts of CR-036 (an xlsx only).</p>
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

	private ChartRenderProbes sweep(String spec) {
		sweep = spec;
		return this;
	}

	private static ChartRenderProbes copy(String set, File xlsx, int width, int height, String note) {
		String name = xlsx.getName().replaceAll("\\.xlsx$", "");
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

	/** The chart's contents through the probe's tweaks */
	private void tweak(Chart chart) throws Exception {
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
