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

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.docx4j.Docx4J;
import org.docx4j.convert.out.HTMLSettings;
import org.docx4j.dml.wordprocessingDrawing.Inline;
import org.docx4j.openpackaging.packages.OpcPackage;
import org.docx4j.openpackaging.packages.PresentationMLPackage;
import org.docx4j.openpackaging.packages.WordprocessingMLPackage;
import org.docx4j.openpackaging.parts.PresentationML.SlidePart;
import org.docx4j.openpackaging.parts.Part;
import org.docx4j.wml.Drawing;
import org.docx4j.wml.P;
import org.docx4j.wml.R;
import org.junit.Test;
import org.pptx4j.convert.out.svginhtml.SvgExporter;

import jakarta.xml.bind.JAXBElement;

/**
 * A chart keeps its space in the HTML outputs, and in pptx's SVG-in-HTML, until docx4j draws
 * charts (CR-035 phase 0).  Until 17.3.3 the visitor logged "Need to handle ...CTRelId" and the
 * XSLT "without pic:pic", and wrote nothing; pptx wrote a red "NOT IMPLEMENTED: support for
 * graphicFrame".  The FO side is in docx4j-export-fo-tests (ChartBoxFoTest).
 */
public class ChartBoxTest {

	private static final int[] PATHWAYS = { Docx4J.FLAG_NONE, Docx4J.FLAG_EXPORT_PREFER_XSL };

	private static WordprocessingMLPackage barChart() throws Exception {
		try (InputStream is = ChartBoxTest.class.getClassLoader().getResourceAsStream("charts/bar-chart.docx")) {
			return WordprocessingMLPackage.load(is);
		}
	}

	private static Inline inline(WordprocessingMLPackage pkg) {
		P p = (P) pkg.getMainDocumentPart().getContent().get(0);
		for (Object o : p.getContent()) {
			if (o instanceof R) {
				for (Object c : ((R) o).getContent()) {
					c = c instanceof JAXBElement ? ((JAXBElement<?>) c).getValue() : c;
					if (c instanceof Drawing) return (Inline) ((Drawing) c).getAnchorOrInline().get(0);
				}
			}
		}
		return null;
	}

	/** Word's Insert Chart default: 6 x 3.5 in, so 432 x 252pt, named "Chart 1" */
	@Test
	public void boxOfWordsDefaultChart() throws Exception {

		ChartBox box = ChartBox.of(inline(barChart()));
		assertNotNull(box);
		assertEquals(432, box.getWidthPt(), 0.001);
		assertEquals(252, box.getHeightPt(), 0.001);
		assertEquals("Chart 1", box.getAltText());
		assertFalse(box.isAnchored());
	}

	@Test
	public void htmlKeepsTheChartsSpace() throws Exception {

		Pattern svg = Pattern.compile("<svg[^>]*>");
		for (int flags : PATHWAYS) {
			String pathway = flags == Docx4J.FLAG_NONE ? "visitor: " : "XSLT: ";
			HTMLSettings settings = Docx4J.createHTMLSettings();
			settings.setOpcPackage(barChart());
			ByteArrayOutputStream baos = new ByteArrayOutputStream();
			Docx4J.toHTML(settings, baos, flags);
			String html = baos.toString("UTF-8");

			Matcher m = svg.matcher(html);
			assertTrue(pathway + "no svg in " + html, m.find());
			String tag = m.group();
			assertTrue(pathway + tag, tag.contains("width=\"432\""));
			assertTrue(pathway + tag, tag.contains("height=\"252\""));
			assertTrue(pathway + tag, tag.contains("aria-label=\"Chart 1\""));
			assertFalse(pathway + "a second svg", m.find());
			assertFalse(pathway, html.contains("NOT IMPLEMENTED"));
		}
	}

	/**
	 * A chartex chart in a docx: Word's mc:Fallback is a picture of the chart it drew, so by
	 * default (chartex's Choice not preferred) the output shows that picture, not a box.
	 */
	@Test
	public void chartexInWordDrawsItsFallbackPicture() throws Exception {

		for (int flags : PATHWAYS) {
			String pathway = flags == Docx4J.FLAG_NONE ? "visitor: " : "XSLT: ";
			HTMLSettings settings = Docx4J.createHTMLSettings();
			try (InputStream is = ChartBoxTest.class.getClassLoader().getResourceAsStream("charts/waterfall.docx")) {
				settings.setOpcPackage(WordprocessingMLPackage.load(is));
			}
			ByteArrayOutputStream baos = new ByteArrayOutputStream();
			Docx4J.toHTML(settings, baos, flags);
			String html = baos.toString("UTF-8");
			assertTrue(pathway + html, html.matches("(?s).*<img[^>]*width=\"432\"[^>]*>.*"));
			assertFalse(pathway, html.contains("<svg"));
		}
	}

	@Test
	public void pptxSvgKeepsTheChartsFrame() throws Exception {

		PresentationMLPackage pml;
		try (InputStream is = ChartBoxTest.class.getClassLoader().getResourceAsStream("loadAndSave.pptx")) {
			pml = (PresentationMLPackage) OpcPackage.load(is);
		}
		SlidePart withChart = null;
		for (Part p : pml.getParts().getParts().values()) {
			if (p instanceof SlidePart && p.getRelationshipsPart() != null
					&& p.getRelationshipsPart().getRelationshipByType(
							org.docx4j.openpackaging.parts.relationships.Namespaces.SPREADSHEETML_CHART) != null) {
				withChart = (SlidePart) p;
			}
		}
		assertNotNull("loadAndSave.pptx has a slide with a chart", withChart);

		String html = SvgExporter.svg(pml, withChart);
		assertFalse(html, html.contains("support for graphicFrame"));
		// the frame's p:xfrm: off 4868561,1553777, ext 6069914 x 4312708 EMU, at 96 dpi
		Matcher div = Pattern.compile("<div[^>]*width:637px; height:453px; left:511px; top:163px;[^>]*>").matcher(html);
		assertTrue(html, div.find());
		assertTrue(div.group(), div.group().contains("aria-label=\"Chart 3\""));
	}
}
