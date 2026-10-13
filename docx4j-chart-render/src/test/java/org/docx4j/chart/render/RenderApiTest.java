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
package org.docx4j.chart.render;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import java.awt.Color;
import java.awt.geom.Rectangle2D;
import java.io.ByteArrayOutputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;

import org.docx4j.XmlUtils;
import org.docx4j.chart.render.api.AxisScale;
import org.docx4j.chart.render.api.ChartFrame;
import org.docx4j.chart.render.api.Drawing;
import org.docx4j.chart.render.api.Fills;
import org.docx4j.chart.render.api.SvgWriter;
import org.docx4j.chart.render.api.TextStyle;
import org.docx4j.chart.render.api.TextWrap;
import org.docx4j.dml.CTShapeProperties;
import org.docx4j.model.chart.ChartFonts;
import org.docx4j.model.chart.ChartRef;
import org.docx4j.model.color.ThemeColors;
import org.junit.Test;
import org.w3c.dom.Document;

/**
 * CR-039 phase 2: a chart drawn through {@code org.docx4j.chart.render.api} alone, as a renderer
 * of other charts (a chartex renderer) would draw one: Excel's axis scale, text measured and
 * wrapped in the host's fonts, a shape's properties as fills, the title and legend placed and the
 * plot area left, the drawing written as SVG.  That the api is the open renderer's own is the
 * phase's identity gate (every probe's layout, SVG and picture unchanged by the move).
 */
public class RenderApiTest {

	private static final String SVG = "http://www.w3.org/2000/svg";

	@Test
	public void excelsAxisScale() {
		// 0 to 87 on a 200pt axis of 12pt lines: KB 214075's 0 to 100 (87 + 87/20 = 91.35, up to the unit), by 10
		AxisScale a = AxisScale.of(0, 87, null, null, null, null, 200, 12);
		assertEquals(0, a.getMin(), 0);
		assertEquals(100, a.getMax(), 0);
		assertEquals(10, a.getUnit(), 0);
		assertEquals(11, a.values().size());
		assertEquals(0.5, a.fraction(50), 1e-12);
		// a bound the file sets is kept
		assertEquals(40, AxisScale.of(0, 87, null, 40d, null, null, 200, 12).getMax(), 0);
	}

	@Test
	public void aChartDrawnThroughTheApi() throws Exception {
		ChartRef host = ChartRenderGateTest.chart("r1-column-clustered");
		ChartFonts fonts = ChartFonts.of(host.getHost());
		ThemeColors theme = host.getHostTheme();
		double w = 432, h = 252;

		// text in the host's minor font, measured as it is drawn
		TextStyle body = new TextStyle(fonts, "+mn-lt", 10, false, false, Color.BLACK, 0);
		TextStyle bold = new TextStyle(fonts, "+mn-lt", 14, true, false, Color.DARK_GRAY, 1200);
		assertTrue(body.width("Sales") > 0);
		assertTrue(bold.width("Sales") > body.width("Sales"));
		assertEquals(Arrays.asList("Monthly", "sales"), TextWrap.wrap("Monthly sales", body, body.width("Monthly ")));

		// a shape's a:spPr: solid red, a 2pt line
		CTShapeProperties sp = (CTShapeProperties) XmlUtils.unmarshalString(
				"<c:spPr xmlns:c=\"http://schemas.openxmlformats.org/drawingml/2006/chart\" xmlns:a=\"http://schemas.openxmlformats.org/drawingml/2006/main\">"
						+ "<a:solidFill><a:srgbClr val=\"FF0000\"/></a:solidFill><a:ln w=\"25400\"><a:solidFill><a:schemeClr val=\"accent1\"/></a:solidFill>"
						+ "<a:prstDash val=\"dash\"/></a:ln></c:spPr>",
				org.docx4j.jaxb.Context.jc, CTShapeProperties.class);
		Fills fills = new Fills(theme);
		assertEquals(Color.RED, fills.fill(sp));
		assertEquals(2, Fills.lineWidth(sp), 1e-9);
		assertEquals(0x4472C4, fills.lineColour(sp).getRGB() & 0xFFFFFF);

		// the title at the top, the legend at the bottom, the plot area between
		List<List<TextWrap.Seg>> title = Collections.singletonList(Collections.singletonList(new TextWrap.Seg("Monthly sales", bold)));
		List<TextWrap.Line> lines = TextWrap.wrap(title, ChartFrame.titleWidth(w));
		assertEquals(1, lines.size());
		Rectangle2D titleBox = ChartFrame.titleBox(w, lines);
		assertEquals(w / 2, titleBox.getCenterX(), 1e-9);
		double[] plot = ChartFrame.plotEdges(w, h);
		plot[1] = ChartFrame.belowTitle(titleBox);
		List<Double> widths = new ArrayList<>();
		for (String entry : new String[] { "North", "South" }) widths.add(2 * ChartFrame.key(body) + body.width(entry));
		ChartFrame.Legend legend = ChartFrame.legend(w, h, widths, body, "b", false, titleBox, plot);
		assertEquals(2, legend.getEntries().size());
		double[] left = legend.getPlot();
		assertTrue(left[3] < legend.getBox().getMinY());
		assertTrue(left[1] > titleBox.getMaxY());

		Drawing d = new Drawing(w, h);
		Rectangle2D inside = new Rectangle2D.Double(left[0], left[1], left[2] - left[0], left[3] - left[1]);
		Drawing.Area bar = fills.area(new Rectangle2D.Double(inside.getX() + 20, inside.getCenterY(), 40, inside.getMaxY() - inside.getCenterY()), sp,
				fills.fill(sp), "point.0.0", inside);
		assertNotNull(bar.getDash());
		d.add(bar);
		for (Drawing.Text t : ChartFrame.titleTexts(titleBox, lines)) d.add(t);
		for (int i = 0; i < widths.size(); i++) {
			double[] e = legend.getEntries().get(i);
			d.add(body.text(i == 0 ? "North" : "South", ChartFrame.textX(e, ChartFrame.key(body), body), ChartFrame.textBaseline(e, body),
					body.width("North"), 0, "legend.text." + i));
		}
		d.getBoxes().put("title", titleBox);
		assertNull(d.getValueAxis());

		Document svg = SvgWriter.toSvg(d);
		assertEquals("432pt", svg.getDocumentElement().getAttribute("width"));
		assertEquals("0 0 432 252", svg.getDocumentElement().getAttribute("viewBox"));
		ByteArrayOutputStream out = new ByteArrayOutputStream();
		XmlUtils.getTransformerFactory().newTransformer().transform(new DOMSource(svg), new StreamResult(out));
		String s = out.toString("UTF-8");
		// the title kerned (14pt, kern 1200): its runs split at the kerned pair, as Office's PDF has them
		StringBuilder shown = new StringBuilder();
		org.w3c.dom.NodeList texts = svg.getElementsByTagNameNS(SVG, "text");
		for (int i = 0; i < texts.getLength(); i++) {
			if (Math.abs(Double.parseDouble(((org.w3c.dom.Element) texts.item(i)).getAttribute("y")) - 20.98) < 0.01) {
				shown.append(texts.item(i).getTextContent());
			}
		}
		assertEquals("Monthly sales", shown.toString());
		assertTrue(s, s.contains(">North</text>"));
		assertTrue(s, s.contains("fill=\"red\""));
		// the line dashed (dash: 4 3 times its 2pt width), its colour the theme's accent 1
		assertTrue(s, s.contains("stroke-dasharray=\"8,6\""));
		assertTrue(s, s.contains("stroke=\"rgb(68,114,196)\""));
	}
}
