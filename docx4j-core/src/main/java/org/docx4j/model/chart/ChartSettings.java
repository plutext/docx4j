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

import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

import org.docx4j.XmlUtils;
import org.docx4j.com.microsoft.schemas.office.drawing.x2012.chartStyle.CTChartStyle;
import org.docx4j.com.microsoft.schemas.office.drawing.x2012.chartStyle.CTFontReference;
import org.docx4j.com.microsoft.schemas.office.drawing.x2012.chartStyle.CTStyleEntry;
import org.docx4j.dml.STFontCollectionIndex;
import org.docx4j.dml.CTEffectList;
import org.docx4j.dml.CTLineProperties;
import org.docx4j.dml.CTNoFillProperties;
import org.docx4j.dml.CTRegularTextRun;
import org.docx4j.dml.CTShapeProperties;
import org.docx4j.dml.CTSolidColorFillProperties;
import org.docx4j.dml.CTTextBody;
import org.docx4j.dml.CTTextBodyProperties;
import org.docx4j.dml.CTTextCharacterProperties;
import org.docx4j.dml.CTTextListStyle;
import org.docx4j.dml.CTTextParagraph;
import org.docx4j.dml.CTTextParagraphProperties;
import org.docx4j.dml.CTTextShapeAutofit;
import org.docx4j.dml.STTextAnchoringType;
import org.docx4j.dml.STTextStrikeType;
import org.docx4j.dml.STTextUnderlineType;
import org.docx4j.dml.STTextVertOverflowType;
import org.docx4j.dml.STTextVerticalType;
import org.docx4j.dml.STTextWrappingType;
import org.docx4j.dml.TextFont;
import org.docx4j.dml.chart.CTAxPos;
import org.docx4j.dml.chart.CTBoolean;
import org.docx4j.dml.chart.CTChart;
import org.docx4j.dml.chart.CTChartLines;
import org.docx4j.dml.chart.CTDLbls;
import org.docx4j.dml.chart.CTExtension;
import org.docx4j.dml.chart.CTExtensionList;
import org.docx4j.dml.chart.CTLegend;
import org.docx4j.dml.chart.CTLegendPos;
import org.docx4j.dml.chart.CTNumFmt;
import org.docx4j.dml.chart.CTPlotArea;
import org.docx4j.dml.chart.CTTitle;
import org.docx4j.dml.chart.CTTx;
import org.docx4j.dml.chart.CTUnsignedInt;
import org.docx4j.dml.chart.STAxPos;
import org.docx4j.dml.chart.STLegendPos;
import org.docx4j.jaxb.Context;
import org.docx4j.openpackaging.exceptions.Docx4JException;
import org.docx4j.openpackaging.parts.Part;
import org.docx4j.openpackaging.parts.DrawingML.Chart;
import org.docx4j.openpackaging.parts.DrawingML.ChartStylePart;
import org.docx4j.relationships.Relationship;

/**
 * A chart's title, legend, axis titles, number format and data labels set as Excel sets them
 * (CR-036 phase 2, review A3): on any chart, made by {@link ChartCreator} or loaded, so that a
 * filled template's title is set as its data is.  What each writes is what Excel wrote for the
 * same setting on each type (CR-036's P2 and P6 probes); its text properties and lines come from
 * the chart's own style part ({@code cs:title}, {@code cs:axisTitle}, {@code cs:legend},
 * {@code cs:dataLabel}, {@code cs:leaderLine}), as Excel's do, and from Excel's default style
 * where a chart has none.
 *
 * <ul>
 * <li>A title's text: {@code c:tx/c:rich}, a run in the title's own text properties; no title:
 * {@code c:title} removed, {@code autoTitleDeleted} 1.</li>
 * <li>The legend's side: {@code c:legendPos} alone; no legend: {@code c:legend} removed.</li>
 * <li>An axis title: {@code c:title} in the axis, its text turned for an axis at the left or
 * right; a number format: the axis' {@code c:numFmt}, not linked to the cells.</li>
 * <li>Data labels: each series' {@code c:dLbls} showing its values (a pie's and a doughnut's
 * with leader lines).</li>
 * </ul>
 *
 * @since 17.3.3 (CR-036)
 */
public final class ChartSettings {

	/** An axis of the chart's first group, by its role */
	public enum Axis {
		/** The categories' axis (a scatter chart's x axis; a bar chart's is at the left) */
		CATEGORY,
		/** The values' axis */
		VALUE
	}

	private static final String LEADER_LINES_URI = "{CE6537A1-D6FC-4f65-9D91-7224C49458BB}";

	private ChartSettings() {
	}

	// ------------------------------------------------------------------ the title

	/**
	 * The chart's title, with this text; null leaves the title for Office to word (a series'
	 * name, or "Chart Title")
	 */
	public static void setTitle(Chart chart, String text) throws Docx4JException {
		CTChart c = chart.getContents().getChart();
		CTStyleEntry entry = style(chart).getTitle();
		CTTitle t = c.getTitle();
		if (t == null) {
			t = title(entry, 0, lang(chart));
			t.setParent(c);
			c.setTitle(t);
		}
		c.setAutoTitleDeleted(bool(false));
		setText(t, text, lang(chart));
	}

	/** No title: Office's own not shown either ({@code autoTitleDeleted}) */
	public static void removeTitle(Chart chart) throws Docx4JException {
		CTChart c = chart.getContents().getChart();
		c.setTitle(null);
		c.setAutoTitleDeleted(bool(true));
	}

	// ------------------------------------------------------------------ the legend

	/** The legend at that side, made where the chart has none */
	public static void setLegend(Chart chart, LegendPosition position) throws Docx4JException {
		if (position == null) throw new IllegalArgumentException("no legend position: removeLegend removes it");
		CTChart c = chart.getContents().getChart();
		CTLegend l = c.getLegend();
		if (l == null) {
			l = new CTLegend();
			l.setOverlay(bool(false));
			l.setSpPr(noFill());
			l.setTxPr(textBody(style(chart).getLegend(), 0, false, lang(chart)));
			l.setParent(c);
			c.setLegend(l);
		}
		CTLegendPos p = new CTLegendPos();
		p.setVal(STLegendPos.fromValue(position.getVal()));
		p.setParent(l);
		l.setLegendPos(p);
	}

	public static void removeLegend(Chart chart) throws Docx4JException {
		chart.getContents().getChart().setLegend(null);
	}

	// ------------------------------------------------------------------ axes

	/** An axis' title, with this text; null removes it */
	public static void setAxisTitle(Chart chart, Axis axis, String text) throws Docx4JException {
		Object ax = axis(chart, axis);
		if (text == null) {
			invoke(ax, "setTitle", CTTitle.class, null);
			return;
		}
		CTTitle t = (CTTitle) ChartSeries.get(ax, "getTitle");
		if (t == null) {
			CTAxPos pos = (CTAxPos) ChartSeries.get(ax, "getAxPos");
			boolean upright = pos != null && (pos.getVal() == STAxPos.L || pos.getVal() == STAxPos.R);
			t = title(style(chart).getAxisTitle(), upright ? -5400000 : 0, lang(chart));
			t.setParent(ax);
			invoke(ax, "setTitle", CTTitle.class, t);
		}
		setText(t, text, lang(chart));
	}

	/** An axis' labels' number format, not linked to the cells'; null links it to them again */
	public static void setNumberFormat(Chart chart, Axis axis, String formatCode) throws Docx4JException {
		Object ax = axis(chart, axis);
		CTNumFmt f = new CTNumFmt();
		f.setFormatCode(formatCode == null ? "General" : formatCode);
		f.setSourceLinked(formatCode == null);
		f.setParent(ax);
		invoke(ax, "setNumFmt", CTNumFmt.class, f);
	}

	// ------------------------------------------------------------------ data labels

	/** Every series' values shown on its points, or no series' */
	public static void setDataLabels(Chart chart, boolean shown) throws Docx4JException {
		CTChartStyle style = style(chart);
		String lang = lang(chart);
		for (Object g : groups(chart)) {
			boolean pie = g instanceof org.docx4j.dml.chart.CTPieChart || g instanceof org.docx4j.dml.chart.CTDoughnutChart
					|| g instanceof org.docx4j.dml.chart.CTOfPieChart || g instanceof org.docx4j.dml.chart.CTPie3DChart;
			Object sers = ChartSeries.get(g, "getSer");
			if (!(sers instanceof List)) continue;
			for (Object s : (List<?>) sers) {
				CTDLbls d = shown ? dataLabels(style, pie, lang) : null;
				if (d != null) d.setParent(s);
				invoke(s, "setDLbls", CTDLbls.class, d);
			}
		}
	}

	/** A series' data labels as Excel's <i>Add Data Labels</i> writes them, showing its values */
	private static CTDLbls dataLabels(CTChartStyle style, boolean pie, String lang) {
		CTDLbls d = new CTDLbls();
		d.setSpPr(noFill());
		d.setTxPr(textBody(style.getDataLabel(), 0, true, lang));
		d.setShowLegendKey(bool(false));
		d.setShowVal(bool(true));
		d.setShowCatName(bool(false));
		d.setShowSerName(bool(false));
		d.setShowPercent(bool(false));
		d.setShowBubbleSize(bool(false));
		CTChartLines leader = new CTChartLines();
		leader.setSpPr(lines(style.getLeaderLine()));
		CTExtension e = new CTExtension();
		e.setUri(LEADER_LINES_URI);
		if (pie) {
			// a pie's: its leader lines c:'s own, the c15 extension left empty
			d.setShowLeaderLines(bool(true));
			leader.setParent(d);
			d.setLeaderLines(leader);
		} else {
			d.setShowLeaderLines(bool(false));
			org.docx4j.com.microsoft.schemas.office.drawing.x2012.chart.ObjectFactory c15 = new org.docx4j.com.microsoft.schemas.office.drawing.x2012.chart.ObjectFactory();
			e.getAny().add(c15.createShowLeaderLines(bool(true)));
			e.getAny().add(c15.createLeaderLines(leader));
		}
		CTExtensionList l = new CTExtensionList();
		e.setParent(l);
		l.getExt().add(e);
		l.setParent(d);
		d.setExtLst(l);
		return d;
	}

	// ------------------------------------------------------------------ what the settings are made of

	/** A title (the chart's or an axis'): no fill, no line, its text properties the style entry's */
	private static CTTitle title(CTStyleEntry entry, int rot, String lang) {
		CTTitle t = new CTTitle();
		t.setOverlay(bool(false));
		t.setSpPr(noFill());
		CTTextBody tx = textBody(entry, rot, false, lang);
		tx.setParent(t);
		t.setTxPr(tx);
		return t;
	}

	/**
	 * A title's text: {@code c:tx/c:rich}, its body and paragraph properties its text properties',
	 * one run; null removes it
	 */
	private static void setText(CTTitle t, String text, String lang) {
		if (text == null) {
			t.setTx(null);
			return;
		}
		CTTextBody txPr = t.getTxPr();
		CTTextBody rich = new CTTextBody();
		rich.setBodyPr(txPr != null && txPr.getBodyPr() != null ? (CTTextBodyProperties) txPr.getBodyPr().copy() : new CTTextBodyProperties());
		rich.setLstStyle(new CTTextListStyle());
		CTTextParagraph p = new CTTextParagraph();
		if (txPr != null && !txPr.getP().isEmpty() && txPr.getP().get(0).getPPr() != null) {
			p.setPPr((CTTextParagraphProperties) txPr.getP().get(0).getPPr().copy());
		}
		CTRegularTextRun r = new CTRegularTextRun();
		CTTextCharacterProperties rPr = new CTTextCharacterProperties();
		rPr.setLang(lang);
		r.setRPr(rPr);
		r.setT(text);
		r.setParent(p);
		p.getEGTextRun().add(r);
		p.setParent(rich);
		rich.getP().add(p);
		CTTx tx = new CTTx();
		rich.setParent(tx);
		tx.setRich(rich);
		tx.setParent(t);
		t.setTx(tx);
	}

	/**
	 * Text properties from a style entry, as Excel writes them into the chart: the entry's
	 * {@code defRPr} with Excel's defaults where it is silent (not bold, not italic, no underline,
	 * no strike, baseline 0), its font reference's colour as a fill and its fonts as the theme's
	 * ({@code +mn-lt} ...); one empty paragraph.  {@code label}: a data label's body (insets,
	 * shape fitted to its text).
	 */
	static CTTextBody textBody(CTStyleEntry entry, int rot, boolean label, String lang) {
		CTTextBodyProperties b = new CTTextBodyProperties();
		b.setRot(rot);
		b.setSpcFirstLastPara(true);
		b.setVertOverflow(STTextVertOverflowType.ELLIPSIS);
		b.setVert(STTextVerticalType.HORZ);
		b.setWrap(STTextWrappingType.SQUARE);
		if (label) {
			b.setLIns(38100);
			b.setTIns(19050);
			b.setRIns(38100);
			b.setBIns(19050);
			b.setSpAutoFit(new CTTextShapeAutofit());
		}
		b.setAnchor(STTextAnchoringType.CTR);
		b.setAnchorCtr(true);
		CTTextCharacterProperties d = entry != null && entry.getDefRPr() != null
				? (CTTextCharacterProperties) entry.getDefRPr().copy() : new CTTextCharacterProperties();
		if (d.isB() == null) d.setB(false);
		if (d.isI() == null) d.setI(false);
		if (d.getU() == null) d.setU(STTextUnderlineType.NONE);
		if (d.getStrike() == null) d.setStrike(STTextStrikeType.NO_STRIKE);
		if (d.getBaseline() == null) d.setBaseline(0);
		CTFontReference f = entry == null ? null : entry.getFontRef();
		if (f != null) {
			CTSolidColorFillProperties fill = new CTSolidColorFillProperties();
			if (f.getSchemeClr() != null) fill.setSchemeClr((org.docx4j.dml.CTSchemeColor) f.getSchemeClr().copy());
			else if (f.getSrgbClr() != null) fill.setSrgbClr((org.docx4j.dml.CTSRgbColor) f.getSrgbClr().copy());
			else if (f.getScrgbClr() != null) fill.setScrgbClr((org.docx4j.dml.CTScRgbColor) f.getScrgbClr().copy());
			else if (f.getHslClr() != null) fill.setHslClr((org.docx4j.dml.CTHslColor) f.getHslClr().copy());
			else if (f.getSysClr() != null) fill.setSysClr((org.docx4j.dml.CTSystemColor) f.getSysClr().copy());
			else if (f.getPrstClr() != null) fill.setPrstClr((org.docx4j.dml.CTPresetColor) f.getPrstClr().copy());
			if (fill.getSchemeClr() != null || fill.getSrgbClr() != null || fill.getScrgbClr() != null
					|| fill.getHslClr() != null || fill.getSysClr() != null || fill.getPrstClr() != null) {
				d.setSolidFill(fill);
			}
			String m = f.getIdx() == STFontCollectionIndex.MAJOR ? "+mj-" : "+mn-";
			if (f.getIdx() != STFontCollectionIndex.NONE) {
				d.setLatin(font(m + "lt"));
				d.setEa(font(m + "ea"));
				d.setCs(font(m + "cs"));
			}
		}
		CTTextParagraphProperties pPr = new CTTextParagraphProperties();
		pPr.setDefRPr(d);
		CTTextParagraph p = new CTTextParagraph();
		p.setPPr(pPr);
		CTTextCharacterProperties end = new CTTextCharacterProperties();
		end.setLang(lang);
		p.setEndParaRPr(end);
		CTTextBody t = new CTTextBody();
		t.setBodyPr(b);
		t.setLstStyle(new CTTextListStyle());
		t.getP().add(p);
		return t;
	}

	private static TextFont font(String typeface) {
		TextFont f = new TextFont();
		f.setTypeface(typeface);
		return f;
	}

	/** No fill, no line, no effects: a title's, a legend's or a data label's own shape */
	static CTShapeProperties noFill() {
		CTShapeProperties sp = new CTShapeProperties();
		sp.setNoFill(new CTNoFillProperties());
		CTLineProperties ln = new CTLineProperties();
		ln.setNoFill(new CTNoFillProperties());
		sp.setLn(ln);
		sp.setEffectLst(new CTEffectList());
		return sp;
	}

	/** A style entry's lines (a leader line's), with no effects */
	private static CTShapeProperties lines(CTStyleEntry entry) {
		CTShapeProperties sp = entry != null && entry.getSpPr() != null ? (CTShapeProperties) entry.getSpPr().copy()
				: new CTShapeProperties();
		if (sp.getEffectLst() == null) sp.setEffectLst(new CTEffectList());
		return sp;
	}

	static CTBoolean bool(boolean v) {
		CTBoolean b = new CTBoolean();
		b.setVal(v);
		return b;
	}

	/** The language of the chart's text: its {@code c:lang}, else en-US */
	private static String lang(Chart chart) throws Docx4JException {
		org.docx4j.dml.chart.CTTextLanguageID l = chart.getContents().getLang();
		return l == null || l.getVal() == null ? "en-US" : l.getVal();
	}

	/** The chart's style part's entries; Excel's default style's where it has none */
	static CTChartStyle style(Chart chart) throws Docx4JException {
		if (chart.getRelationshipsPart() != null) {
			for (Relationship r : chart.getRelationshipsPart().getRelationships().getRelationship()) {
				Part p = chart.getRelationshipsPart().getPart(r);
				if (p instanceof ChartStylePart) return ((ChartStylePart) p).getContents();
			}
		}
		return defaultStyle();
	}

	private static CTChartStyle defaultStyle;

	private static synchronized CTChartStyle defaultStyle() throws Docx4JException {
		if (defaultStyle == null) {
			try (InputStream is = ChartSettings.class.getResourceAsStream("templates/style-201.xml")) {
				defaultStyle = (CTChartStyle) XmlUtils.unwrap(XmlUtils.unmarshal(is, Context.jc));
			} catch (Exception e) {
				throw new Docx4JException("Excel's default chart style couldn't be read", e);
			}
		}
		return defaultStyle;
	}

	/** The chart's groups (barChart, lineChart ...) */
	static List<Object> groups(Chart chart) throws Docx4JException {
		List<Object> out = new ArrayList<Object>();
		CTPlotArea pa = chart.getContents().getChart() == null ? null : chart.getContents().getChart().getPlotArea();
		if (pa == null) return out;
		for (Object o : pa.getAreaChartOrArea3DChartOrLineChart()) out.add(XmlUtils.unwrap(o));
		return out;
	}

	/** The first group's axis of that role: its first {@code c:axId} the category axis', its second the value axis' */
	@SuppressWarnings("unchecked")
	static Object axis(Chart chart, Axis axis) throws Docx4JException {
		List<Object> gs = groups(chart);
		Object ids = gs.isEmpty() ? null : ChartSeries.get(gs.get(0), "getAxId");
		if (!(ids instanceof List) || ((List<?>) ids).size() < 2) throw new IllegalStateException("the chart has no axes");
		long id = ((List<CTUnsignedInt>) ids).get(axis == Axis.CATEGORY ? 0 : 1).getVal();
		for (Object o : chart.getContents().getChart().getPlotArea().getValAxOrCatAxOrDateAx()) {
			Object a = XmlUtils.unwrap(o);
			CTUnsignedInt ax = (CTUnsignedInt) ChartSeries.get(a, "getAxId");
			if (ax != null && ax.getVal() == id) return a;
		}
		throw new IllegalStateException("the chart's axis " + id + " is missing");
	}

	static void invoke(Object o, String setter, Class<?> type, Object value) {
		try {
			o.getClass().getMethod(setter, type).invoke(o, value);
		} catch (ReflectiveOperationException e) {
			throw new IllegalStateException(o.getClass().getSimpleName() + "." + setter, e);
		}
	}
}
