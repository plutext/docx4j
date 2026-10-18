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

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.io.File;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.docx4j.XmlUtils;
import org.docx4j.dml.chart.CTChartSpace;
import org.docx4j.jaxb.Context;
import org.docx4j.openpackaging.packages.OpcPackage;
import org.junit.Test;

/**
 * A chart's description (CR-047): its kind in Excel's names, the axes Office shows with their
 * roles and titles, the series on secondary axes, and the note text readers write; and its JSON
 * goldens ({@code charts/description/}), which docx4j's ports compare against.
 *
 * <p>To write the goldens afresh (after a deliberate change), run this test with
 * {@code -Ddocx4j.chartDescriptionGoldens.write=<the charts/description directory of
 * src/test/resources>} and review the diff.</p>
 */
public class ChartDescriptionTest {

	private static ChartDescription describe(String fixture) throws Exception {
		return ChartDescription.of(ChartModelTest.model(fixture));
	}

	private static List<String> roles(ChartDescription d) {
		List<String> out = new ArrayList<String>();
		for (ChartDescription.Axis a : d.getAxes()) out.add(a.getRole() + "@" + a.getPosition() + "=" + a.getTitle());
		return out;
	}

	/** A clustered bar with both axis titles at Office's default */
	@Test
	public void barWithDefaultAxisTitles() throws Exception {
		ChartDescription d = describe("bar-chart.xlsx");
		assertEquals("clustered bar", d.getKind());
		assertEquals(Arrays.asList("category axis@l=Axis Title", "value axis@b=Axis Title"), roles(d));
		assertEquals("[chart: clustered bar; category axis: Axis Title; value axis: Axis Title]", d.getNote());
		assertEquals("barChart", d.getGroups().get(0).getElement());
		assertEquals("bar", d.getGroups().get(0).getBarDirection());
		// no c:title: no title, and not in the note
		assertEquals("[chart: clustered bar]", describe("bar-chart.docx").getNote());
	}

	/** A clustered column and a line on secondary axes, the secondary category axis deleted */
	@Test
	public void comboOnSecondaryAxes() throws Exception {
		ChartDescription d = describe("p2-combo-secondary.xlsx");
		assertEquals("clustered column and line", d.getKind());
		assertFalse(d.getGroups().get(0).isSecondary());
		assertTrue(d.getGroups().get(1).isSecondary());
		assertEquals(Arrays.asList("Share"), d.getSecondarySeries());
		assertEquals(Arrays.asList("category axis@b=null", "value axis@l=null", "secondary value axis@r=null"), roles(d));
		assertEquals("[chart: clustered column and line; secondary axis: Share]", d.getNote());
	}

	/** A 3-D column's depth axis; a pie's none; a scatter's x and y axes */
	@Test
	public void axisRoles() throws Exception {
		assertEquals(Arrays.asList("category axis@b=null", "value axis@l=null", "depth axis@b=null"), roles(describe("p2-3d-column.xlsx")));
		assertEquals("3-D column", describe("p2-3d-column.xlsx").getKind());
		assertTrue(describe("pie-chart.xlsx").getAxes().isEmpty());
		assertEquals("[chart: pie]", describe("pie-chart.xlsx").getNote());
		assertEquals(Arrays.asList("x axis@b=null", "y axis@l=null"), roles(describe("p2-scatter.xlsx")));
		assertEquals(Arrays.asList("x axis@b=null", "y axis@l=null"), roles(describe("p2-bubble.xlsx")));
		// a date axis is a category axis
		assertEquals("category axis@b=null", roles(describe("p2-date-axis.xlsx")).get(0));
	}

	/** The text extraction probe's chart (its case 34), as WD 0.8's Appendix A expects it */
	@Test
	public void probeChart() throws Exception {
		List<ChartRef> refs;
		try (InputStream is = getClass().getClassLoader().getResourceAsStream("text-extraction/phase0/extraction-probe.docx")) {
			refs = Charts.in(OpcPackage.load(is));
		}
		assertEquals(1, refs.size());
		ChartDescription d = ChartDescription.of(refs.get(0).read());
		assertEquals("[chart: clustered column; value axis: CHT34_AXIS_TITLE]", d.getNote());
		assertEquals("CHT34_TITLE", d.getTable().getTitle());
	}

	/** Chartex: the first series' layout; a hidden series not among the group's */
	@Test
	public void chartex() throws Exception {
		ChartDescription d = ChartDescription.of(ChartsTest.refs("waterfall.docx").get(0).readChartEx());
		assertEquals("[chart: waterfall]", d.getNote());
		assertEquals(Arrays.asList("category axis@null=null", "value axis@null=null"), roles(d));
		ChartRef xlsx = null;
		for (ChartRef r : ChartsTest.refs("waterfall.xlsx")) if (r.isChartEx()) xlsx = r;
		ChartDescription x = ChartDescription.of(xlsx.readChartEx());
		assertEquals(Arrays.asList("Series 1"), x.getGroups().get(0).getSeries());
		assertEquals("waterfall", x.getGroups().get(0).getElement());
		assertEquals("histogram", ChartDescription.layoutName("clusteredColumn"));
		assertEquals("box and whisker", ChartDescription.layoutName("boxWhisker"));
		assertEquals("map", ChartDescription.layoutName("regionMap"));
		assertEquals("funnel", ChartDescription.layoutName("funnel"));
	}

	/** Chartex axes' titles (Office's default where a title states no text), a hidden axis left out unless titled; a Pareto chart */
	@Test
	public void chartexAxisTitlesAndPareto() throws Exception {
		ChartExModel m = pareto("<cx:axis id=\"0\"><cx:catScaling/><cx:title><cx:tx><cx:txData><cx:v>Bins</cx:v></cx:txData></cx:tx></cx:title></cx:axis>"
				+ "<cx:axis id=\"1\"><cx:valScaling/><cx:title/></cx:axis>"
				+ "<cx:axis id=\"2\"><cx:valScaling/></cx:axis>"
				+ "<cx:axis id=\"3\" hidden=\"1\"><cx:valScaling/></cx:axis>");
		assertEquals(4, m.getAxes().size());
		assertEquals("Bins", m.getAxes().get(0).getTitleText());
		assertEquals("Axis Title", m.getAxes().get(1).getShownTitle());
		assertEquals(null, m.getAxes().get(2).getShownTitle());
		assertTrue(m.getAxes().get(3).isHidden());
		ChartDescription d = ChartDescription.of(m);
		assertEquals("pareto", d.getKind());
		assertEquals(Arrays.asList("category axis@null=Bins", "value axis@null=Axis Title", "secondary value axis@null=null"), roles(d));
		assertEquals("[chart: pareto; category axis: Bins; value axis: Axis Title]", d.getNote());
		// a hidden axis's title is kept, as Excel shows a deleted axis's; a hidden value axis still takes its role
		d = ChartDescription.of(pareto("<cx:axis id=\"0\"><cx:catScaling/></cx:axis>"
				+ "<cx:axis id=\"1\" hidden=\"1\"><cx:valScaling/></cx:axis>"
				+ "<cx:axis id=\"2\" hidden=\"1\"><cx:valScaling/><cx:title><cx:tx><cx:txData><cx:v>Percent</cx:v></cx:txData></cx:tx></cx:title></cx:axis>"));
		assertEquals(Arrays.asList("category axis@null=null", "secondary value axis@null=Percent"), roles(d));
		assertEquals("[chart: pareto; secondary value axis: Percent]", d.getNote());
	}

	private static ChartExModel pareto(String axes) throws Exception {
		String xml = "<cx:chartSpace xmlns:cx=\"http://schemas.microsoft.com/office/drawing/2014/chartex\"><cx:chart><cx:plotArea><cx:plotAreaRegion>"
				+ "<cx:series layoutId=\"clusteredColumn\"><cx:tx><cx:txData><cx:v>Scores</cx:v></cx:txData></cx:tx></cx:series>"
				+ "<cx:series layoutId=\"paretoLine\"/></cx:plotAreaRegion>"
				+ axes
				+ "</cx:plotArea></cx:chart></cx:chartSpace>";
		return ChartExModel.read((org.docx4j.com.microsoft.schemas.office.drawing.x2014.chartex.CTChartSpace)
				XmlUtils.unwrap(XmlUtils.unmarshalString(xml, Context.jc)));
	}

	/**
	 * A deleted axis's title still shows: Excel draws CR047_DELETED_AXIS_TITLE, and "Axis Title" for a
	 * title with no text, where the deleted value axis would be (CR-047's Office check, 18 October 2026)
	 */
	@Test
	public void deletedAxisTitleShows() throws Exception {
		ChartDescription d = describe("cr047-deleted-axis-titled.xlsx");
		assertEquals(Arrays.asList("category axis@l=CR047_SHOWN_AXIS_TITLE", "value axis@b=CR047_DELETED_AXIS_TITLE"), roles(d));
		assertEquals("[chart: clustered bar; category axis: CR047_SHOWN_AXIS_TITLE; value axis: CR047_DELETED_AXIS_TITLE]", d.getNote());
		assertEquals("[chart: clustered bar; category axis: CR047_SHOWN_AXIS_TITLE; value axis: Axis Title]",
				describe("cr047-deleted-axis-default-title.xlsx").getNote());
	}

	/**
	 * A hidden chartex axis's title still shows, as a deleted axis's does: Excel draws
	 * CR047_HIDDEN_AXIS_TITLE, and "Axis Title" for a title with no text, at the left of its Pareto chart,
	 * and "Axis Title" at the right for the shown axis's (CR-047's Office check, second round)
	 */
	@Test
	public void hiddenChartexAxisTitleShows() throws Exception {
		ChartDescription d = describeEx("cr047-pareto-hidden-axis-titled.xlsx");
		assertEquals(Arrays.asList("category axis@null=CR047_SHOWN_AXIS_TITLE", "value axis@null=CR047_HIDDEN_AXIS_TITLE",
				"secondary value axis@null=null"), roles(d));
		assertEquals("[chart: pareto; category axis: CR047_SHOWN_AXIS_TITLE; value axis: CR047_HIDDEN_AXIS_TITLE]", d.getNote());
		assertEquals("[chart: pareto; category axis: CR047_SHOWN_AXIS_TITLE; value axis: Axis Title; secondary value axis: Axis Title]",
				describeEx("cr047-pareto-hidden-axis-default-title.xlsx").getNote());
		// Excel's own removal of both value axes (Chart Elements, one after the other): hidden="1", untitled, left out
		d = describeEx("cr047-pareto-excel-deleted.xlsx");
		assertEquals(Arrays.asList("category axis@null=null"), roles(d));
		assertEquals("[chart: pareto]", d.getNote());
	}

	private static ChartDescription describeEx(String fixture) throws Exception {
		for (ChartRef r : ChartsTest.refs(fixture)) if (r.isChartEx()) return ChartDescription.of(r.readChartEx());
		throw new AssertionError(fixture + ": no chartex chart");
	}

	/** Every chart fixture's kind, in Excel's names (CR-047 section 2.2) */
	@Test
	public void kindsOfTheFixtures() throws Exception {
		Map<String, String> expected = new LinkedHashMap<String, String>();
		expected.put("clustered-column.xlsx", "clustered column");
		expected.put("stacked-column.xlsx", "stacked column");
		expected.put("line-chart.xlsx", "line");
		expected.put("p2-area.xlsx", "area");
		expected.put("p2-bar-of-pie.xlsx", "bar of pie");
		expected.put("p2-doughnut.xlsx", "doughnut");
		expected.put("p2-radar.xlsx", "radar");
		expected.put("p2-stock.xlsx", "stock");
		expected.put("p2-surface.xlsx", "3-D surface");
		expected.put("p2-scatter.xlsx", "scatter");
		expected.put("p2-bubble.xlsx", "bubble");
		for (Map.Entry<String, String> e : expected.entrySet()) assertEquals(e.getKey(), e.getValue(), describe(e.getKey()).getKind());
	}

	private static ChartDescription group(String element) throws Exception {
		String xml = "<c:chartSpace xmlns:c=\"http://schemas.openxmlformats.org/drawingml/2006/chart\"><c:chart><c:plotArea>"
				+ element + "</c:plotArea></c:chart></c:chartSpace>";
		CTChartSpace cs = (CTChartSpace) XmlUtils.unwrap(XmlUtils.unmarshalString(xml, Context.jc));
		return ChartDescription.of(ChartModel.read(cs, null));
	}

	/** The kinds no fixture has, from their elements */
	@Test
	public void kindsOfTheElements() throws Exception {
		assertEquals("100% stacked bar", group("<c:barChart><c:barDir val=\"bar\"/><c:grouping val=\"percentStacked\"/></c:barChart>").getKind());
		assertEquals("column", group("<c:barChart><c:barDir val=\"col\"/><c:grouping val=\"standard\"/></c:barChart>").getKind());
		assertEquals("3-D clustered column", group("<c:bar3DChart><c:barDir val=\"col\"/><c:grouping val=\"clustered\"/></c:bar3DChart>").getKind());
		assertEquals("stacked line", group("<c:lineChart><c:grouping val=\"stacked\"/></c:lineChart>").getKind());
		assertEquals("3-D line", group("<c:line3DChart><c:grouping val=\"standard\"/></c:line3DChart>").getKind());
		assertEquals("3-D 100% stacked area", group("<c:area3DChart><c:grouping val=\"percentStacked\"/></c:area3DChart>").getKind());
		assertEquals("3-D pie", group("<c:pie3DChart/>").getKind());
		assertEquals("pie of pie", group("<c:ofPieChart><c:ofPieType val=\"pie\"/></c:ofPieChart>").getKind());
		assertEquals("filled radar", group("<c:radarChart><c:radarStyle val=\"filled\"/></c:radarChart>").getKind());
		assertEquals("wireframe contour", group("<c:surfaceChart><c:wireframe val=\"1\"/></c:surfaceChart>").getKind());
		assertEquals("wireframe 3-D surface", group("<c:surface3DChart><c:wireframe val=\"1\"/></c:surface3DChart>").getKind());
		assertEquals("contour", group("<c:surfaceChart><c:wireframe val=\"0\"/></c:surfaceChart>").getKind());
	}

	private static String golden(String fixture, String partName) {
		return fixture + partName.replace('/', '_') + ".json";
	}

	/** The JSON goldens: every chart part of the chart fixtures, chartex included */
	@Test
	public void jsonGoldens() throws Exception {
		String write = System.getProperty("docx4j.chartDescriptionGoldens.write");
		List<String> fixtures = new ArrayList<String>(Arrays.asList(ChartModelTest.FIXTURES));
		fixtures.add("chartex/manual-bincount-6.xlsx");
		List<String> missing = new ArrayList<String>();
		int n = 0;
		for (String fixture : fixtures) {
			for (ChartRef r : ChartsTest.refs(fixture)) {
				ChartDescription d = r.isChartEx() ? ChartDescription.of(r.readChartEx()) : ChartDescription.of(r.read());
				String name = golden(fixture.replace("chartex/", ""), r.getChartPart().getPartName().getName());
				if (write != null) {
					Files.write(new File(write, name).toPath(), d.toJson().getBytes(StandardCharsets.UTF_8));
					continue;
				}
				try (InputStream is = getClass().getClassLoader().getResourceAsStream("charts/description/" + name)) {
					if (is == null) {
						missing.add(name);
						continue;
					}
					assertEquals(name, new String(is.readAllBytes(), StandardCharsets.UTF_8), d.toJson());
				}
				n++;
			}
		}
		assertTrue("no golden for " + missing, missing.isEmpty() || write != null);
		if (write == null) assertEquals("charts described", 33, n);
	}

}
