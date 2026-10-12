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
package org.docx4j.convert.out.XSLFO;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.pdfbox.Loader;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.text.PDFTextStripper;
import org.apache.pdfbox.text.TextPosition;
import org.docx4j.Docx4J;
import org.docx4j.XmlUtils;
import org.docx4j.convert.out.FOSettings;
import org.docx4j.dml.wordprocessingDrawing.Inline;
import org.docx4j.openpackaging.packages.WordprocessingMLPackage;
import org.docx4j.wml.Drawing;
import org.docx4j.wml.P;
import org.docx4j.wml.R;
import org.junit.Test;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

import jakarta.xml.bind.JAXBElement;

/**
 * A chart keeps its space in the FO and the PDF, in both pathways (CR-035 phase 0): an
 * {@code fo:instream-foreign-object} of the drawing's extent, inline in its line, or floated as
 * an anchored picture is; and it is drawn there, as SVG, by docx4j-chart-render (CR-037 phase 3),
 * the box left empty where no renderer draws it.  Until 17.3.3 the chart was dropped ("Need to
 * handle ...CTRelId"; the XSLT's "without pic:pic") and the text after it moved up into its place.
 */
public class ChartBoxFoTest extends AbstractXSLFOTest {

	private static final String FO = "http://www.w3.org/1999/XSL/Format";
	private static final String FOX = "http://xmlgraphics.apache.org/fop/extensions";
	private static final String SVG = "http://www.w3.org/2000/svg";
	private static final int[] PATHWAYS = { Docx4J.FLAG_NONE, Docx4J.FLAG_EXPORT_PREFER_XSL };

	private static String pathway(int flags) {
		return (flags == Docx4J.FLAG_NONE ? "visitor" : "XSLT") + ": ";
	}

	/**
	 * Word's default bar chart (6 x 3.5 in = 432 x 252pt, "Chart 1") between a paragraph BEFORE
	 * and a paragraph AFTER; anchored, the same chart as a top-and-bottom wp:anchor at its
	 * paragraph's top.
	 */
	private static WordprocessingMLPackage doc(boolean anchored) throws Exception {
		WordprocessingMLPackage pkg;
		try (InputStream is = ChartBoxFoTest.class.getClassLoader().getResourceAsStream("charts/bar-chart.docx")) {
			pkg = WordprocessingMLPackage.load(is);
		}
		List<Object> body = pkg.getMainDocumentPart().getContent();
		if (anchored) {
			Drawing drawing = drawing((P) body.get(0));
			Inline inline = (Inline) drawing.getAnchorOrInline().get(0);
			String rId = ((org.docx4j.dml.chart.CTRelId) XmlUtils.unwrap(
					inline.getGraphic().getGraphicData().getAny().get(0))).getId();
			Object anchor = XmlUtils.unwrap(XmlUtils.unmarshalString(anchorXml(rId)));
			drawing.getAnchorOrInline().set(0, anchor);
		}
		body.add(0, pkg.getMainDocumentPart().createParagraphOfText("BEFORE the chart."));
		body.add(pkg.getMainDocumentPart().createParagraphOfText("AFTER the chart."));
		return pkg;
	}

	private static Drawing drawing(P p) {
		for (Object o : p.getContent()) {
			if (o instanceof R) {
				for (Object c : ((R) o).getContent()) {
					c = c instanceof JAXBElement ? ((JAXBElement<?>) c).getValue() : c;
					if (c instanceof Drawing) return (Drawing) c;
				}
			}
		}
		return null;
	}

	private static String anchorXml(String rId) {
		return "<wp:anchor xmlns:wp=\"http://schemas.openxmlformats.org/drawingml/2006/wordprocessingDrawing\""
				+ " xmlns:a=\"http://schemas.openxmlformats.org/drawingml/2006/main\""
				+ " xmlns:c=\"http://schemas.openxmlformats.org/drawingml/2006/chart\""
				+ " xmlns:r=\"http://schemas.openxmlformats.org/officeDocument/2006/relationships\""
				+ " distT=\"0\" distB=\"0\" distL=\"114300\" distR=\"114300\" simplePos=\"0\" relativeHeight=\"251659264\""
				+ " behindDoc=\"0\" locked=\"0\" layoutInCell=\"1\" allowOverlap=\"1\">"
				+ "<wp:simplePos x=\"0\" y=\"0\"/>"
				+ "<wp:positionH relativeFrom=\"column\"><wp:posOffset>0</wp:posOffset></wp:positionH>"
				+ "<wp:positionV relativeFrom=\"paragraph\"><wp:posOffset>0</wp:posOffset></wp:positionV>"
				+ "<wp:extent cx=\"5486400\" cy=\"3200400\"/><wp:effectExtent l=\"0\" t=\"0\" r=\"0\" b=\"0\"/>"
				+ "<wp:wrapTopAndBottom/><wp:docPr id=\"1\" name=\"Chart 1\"/><wp:cNvGraphicFramePr/>"
				+ "<a:graphic><a:graphicData uri=\"http://schemas.openxmlformats.org/drawingml/2006/chart\">"
				+ "<c:chart r:id=\"" + rId + "\"/></a:graphicData></a:graphic></wp:anchor>";
	}

	private static org.w3c.dom.Document fo(WordprocessingMLPackage pkg, int flags) throws Exception {
		FOSettings foSettings = Docx4J.createFOSettings();
		foSettings.setOpcPackage(pkg);
		foSettings.setApacheFopMime(FOSettings.INTERNAL_FO_MIME);
		ByteArrayOutputStream baos = new ByteArrayOutputStream();
		Docx4J.toFO(foSettings, baos, flags);
		return XmlUtils.getNewDocumentBuilder().parse(new ByteArrayInputStream(baos.toByteArray()));
	}

	/** The y (from the page top, points) of the first line starting with each of BEFORE and AFTER */
	private static Map<String, Float> lineTops(WordprocessingMLPackage pkg, int flags) throws Exception {
		FOSettings foSettings = Docx4J.createFOSettings();
		foSettings.setOpcPackage(pkg);
		ByteArrayOutputStream baos = new ByteArrayOutputStream();
		Docx4J.toFO(foSettings, baos, flags);
		Map<String, Float> y = new HashMap<>();
		try (PDDocument d = Loader.loadPDF(baos.toByteArray())) {
			PDFTextStripper s = new PDFTextStripper() {
				@Override
				protected void writeString(String text, List<TextPosition> positions) {
					for (String word : new String[] { "BEFORE", "AFTER" }) {
						if (text.startsWith(word) && !y.containsKey(word)) {
							y.put(word, positions.get(0).getYDirAdj());
						}
					}
				}
			};
			s.getText(d);
		}
		return y;
	}

	/** The box's frame: an fo:instream-foreign-object of the extent, holding one SVG */
	private static Element boxSvg(org.w3c.dom.Document fo, int flags) {
		NodeList ifos = fo.getElementsByTagNameNS(FO, "instream-foreign-object");
		assertEquals(pathway(flags), 1, ifos.getLength());
		Element ifo = (Element) ifos.item(0);
		assertEquals(pathway(flags), "432pt", ifo.getAttribute("content-width"));
		assertEquals(pathway(flags), "252pt", ifo.getAttribute("content-height"));
		assertEquals(pathway(flags), "non-uniform", ifo.getAttribute("scaling"));
		assertEquals(pathway(flags), "Chart 1", ifo.getAttributeNS(FOX, "alt-text"));
		NodeList svg = ifo.getElementsByTagNameNS(SVG, "svg");
		assertEquals(pathway(flags), 1, svg.getLength());
		return (Element) svg.item(0);
	}

	@Test
	public void inlineChartIsDrawnInItsExtent() throws Exception {

		for (int flags : PATHWAYS) {
			Element svg = boxSvg(fo(doc(false), flags), flags);
			assertEquals(pathway(flags), "432pt", svg.getAttribute("width"));
			assertEquals(pathway(flags), "0 0 432 252", svg.getAttribute("viewBox"));
			List<String> texts = new java.util.ArrayList<>();
			NodeList t = svg.getElementsByTagNameNS(SVG, "text");
			for (int i = 0; i < t.getLength(); i++) texts.add(t.item(i).getTextContent());
			for (String s : new String[] { "Category 1", "Category 4", "Series 1", "Series 3" }) {
				assertTrue(pathway(flags) + s + " in " + texts, texts.contains(s));
			}
		}
	}

	@Test
	public void withoutARendererTheBoxStaysEmpty() throws Exception {

		org.docx4j.model.chart.ChartRenderer was = org.docx4j.model.chart.ChartRenderer.getRenderer();
		assertNotNull("docx4j-chart-render is export-fo's dependency", was);
		org.docx4j.model.chart.ChartRenderer.setRenderer(null);
		try {
			for (int flags : PATHWAYS) {
				Element svg = boxSvg(fo(doc(false), flags), flags);
				assertEquals(pathway(flags), 0, svg.getChildNodes().getLength());
			}
		} finally {
			org.docx4j.model.chart.ChartRenderer.setRenderer(was);
		}
	}

	@Test
	public void theChartsTextIsTextInThePdf() throws Exception {

		for (int flags : PATHWAYS) {
			FOSettings foSettings = Docx4J.createFOSettings();
			foSettings.setOpcPackage(doc(false));
			ByteArrayOutputStream baos = new ByteArrayOutputStream();
			Docx4J.toFO(foSettings, baos, flags);
			try (PDDocument d = Loader.loadPDF(baos.toByteArray())) {
				String text = new PDFTextStripper().getText(d);
				for (String s : new String[] { "Category 1", "Series 2" }) {
					assertTrue(pathway(flags) + s + " in " + text, text.contains(s));
				}
			}
		}
	}

	@Test
	public void textAfterAnInlineChartIsBelowIt() throws Exception {

		for (int flags : PATHWAYS) {
			Map<String, Float> y = lineTops(doc(false), flags);
			assertNotNull(pathway(flags) + y, y.get("BEFORE"));
			assertNotNull(pathway(flags) + y, y.get("AFTER"));
			float gap = y.get("AFTER") - y.get("BEFORE");
			// the chart's line is 252pt plus a little; before 17.3.3 the gap was one line
			assertTrue(pathway(flags) + "gap " + gap, gap >= 252 && gap < 300);
		}
	}

	@Test
	public void textAfterAnAnchoredChartIsBelowIt() throws Exception {

		for (int flags : PATHWAYS) {
			org.w3c.dom.Document fo = fo(doc(true), flags);
			NodeList ifos = fo.getElementsByTagNameNS(FO, "instream-foreign-object");
			assertEquals(pathway(flags), 1, ifos.getLength());
			// the anchor hints are consumed by WordLayoutFixups
			assertTrue(pathway(flags), ((Element) ifos.item(0)).getAttribute("docx4j-anchor").isEmpty());

			Map<String, Float> y = lineTops(doc(true), flags);
			float gap = y.get("AFTER") - y.get("BEFORE");
			assertTrue(pathway(flags) + "gap " + gap, gap >= 252 && gap < 300);
		}
	}
}
