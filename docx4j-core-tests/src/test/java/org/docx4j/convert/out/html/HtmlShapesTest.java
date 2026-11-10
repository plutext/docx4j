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
package org.docx4j.convert.out.html;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import java.io.ByteArrayOutputStream;

import org.docx4j.Docx4J;
import org.docx4j.XmlUtils;
import org.docx4j.convert.out.HTMLSettings;
import org.docx4j.model.shapes.ShapeDrawer;
import org.docx4j.openpackaging.packages.WordprocessingMLPackage;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.junit.Test;

/**
 * A DrawingML shape in HTML with no {@link ShapeDrawer} on the classpath (docx4j-core alone, no docx4j-chart-render):
 * its box, its text where Word lays it out, nothing drawn (CR-080 phase 5); and the box whole inside its paragraph
 * when an HTML parser reads the output, as a browser does, its text's paragraphs being spans.
 */
public class HtmlShapesTest {

	private static final String NS = "xmlns:w=\"http://schemas.openxmlformats.org/wordprocessingml/2006/main\""
			+ " xmlns:wp=\"http://schemas.openxmlformats.org/drawingml/2006/wordprocessingDrawing\""
			+ " xmlns:a=\"http://schemas.openxmlformats.org/drawingml/2006/main\""
			+ " xmlns:wps=\"http://schemas.microsoft.com/office/word/2010/wordprocessingShape\"";
	private static final int[] BOTH = { Docx4J.FLAG_NONE, Docx4J.FLAG_EXPORT_PREFER_XSL };

	private static String shape(String text) {
		return "<w:r><w:drawing><wp:inline distT=\"0\" distB=\"0\" distL=\"0\" distR=\"0\"><wp:extent cx=\"2286000\" cy=\"1143000\"/>"
				+ "<wp:docPr id=\"1\" name=\"Shape 1\"/><a:graphic><a:graphicData uri=\"http://schemas.microsoft.com/office/word/2010/wordprocessingShape\">"
				+ "<wps:wsp><wps:cNvSpPr/><wps:spPr><a:xfrm><a:off x=\"0\" y=\"0\"/><a:ext cx=\"2286000\" cy=\"1143000\"/></a:xfrm>"
				+ "<a:prstGeom prst=\"rect\"><a:avLst/></a:prstGeom><a:solidFill><a:srgbClr val=\"FF0000\"/></a:solidFill></wps:spPr>"
				+ (text == null ? "" : "<wps:txbx><w:txbxContent><w:p><w:r><w:t>" + text + "</w:t></w:r></w:p><w:p><w:r><w:t>SECOND</w:t></w:r></w:p>"
						+ "</w:txbxContent></wps:txbx>")
				+ "<wps:bodyPr anchor=\"b\"/></wps:wsp></a:graphicData></a:graphic></wp:inline></w:drawing></w:r>";
	}

	private static String html(String body, int flags) throws Exception {
		WordprocessingMLPackage pkg = WordprocessingMLPackage.createPackage();
		pkg.getMainDocumentPart().setJaxbElement((org.docx4j.wml.Document) XmlUtils.unmarshalString("<w:document " + NS + "><w:body>" + body
				+ "</w:body></w:document>"));
		HTMLSettings settings = Docx4J.createHTMLSettings();
		settings.setOpcPackage(pkg);
		ByteArrayOutputStream baos = new ByteArrayOutputStream();
		Docx4J.toHTML(settings, baos, flags);
		return baos.toString("UTF-8");
	}

	private static String route(int flags) {
		return flags == Docx4J.FLAG_NONE ? "visitor" : "XSLT";
	}

	@Test
	public void noDrawerHere() {
		assertNull("docx4j-core-tests has no docx4j-chart-render", ShapeDrawer.getDrawer());
	}

	/** With nothing to draw it, a shape's text is still where Word lays it out: its box, its text at the foot */
	@Test
	public void aShapesTextIsPlacedWithNothingDrawn() throws Exception {
		for (int flags : BOTH) {
			org.jsoup.nodes.Document doc = Jsoup.parse(html("<w:p><w:r><w:t>before</w:t></w:r>" + shape("IN THE SHAPE") + "</w:p>", flags));
			Elements boxes = doc.select("span.docx4j-shape");
			assertEquals(route(flags), 1, boxes.size());
			assertEquals(route(flags), 0, boxes.first().select("svg").size());
			Element text = boxes.first().selectFirst("span.docx4j-shape-text");
			assertTrue(route(flags), text != null && text.text().contains("IN THE SHAPE"));
			assertTrue(route(flags), text.attr("style").contains("justify-content:flex-end"));
		}
	}

	/** A browser's HTML parser keeps the box, and both of its text's paragraphs, inside the anchor's paragraph: the
	 *  text's paragraphs are spans, where a nested p would end the outer p and spill the text out of the box */
	@Test
	public void anHtmlParserKeepsTheBoxWholeInItsParagraph() throws Exception {
		for (int flags : BOTH) {
			org.jsoup.nodes.Document doc = Jsoup.parse(html("<w:p><w:r><w:t>before</w:t></w:r>" + shape("IN THE SHAPE")
					+ "<w:r><w:t>after</w:t></w:r></w:p>", flags));
			Elements texts = doc.select("p > span.docx4j-shape > span.docx4j-shape-text");
			assertEquals(route(flags), 1, texts.size());
			assertTrue(route(flags), texts.first().text().contains("IN THE SHAPE") && texts.first().text().contains("SECOND"));
			Element p = texts.first().parent().parent();
			assertTrue(route(flags) + ": the paragraph goes on after the box", p.text().contains("after"));
		}
	}

	/** A run whose only content is a shape, the run with a character style (as Word's runs have, DefaultParagraphFont
	 *  in a real document): the run's span keeps the box whole inside it, where a run's single child span used to be
	 *  folded into the run's own span, the box's class, role and label lost (9775: 9 boxes, none kept) */
	@Test
	public void aStyledRunKeepsItsShapesBox() throws Exception {
		for (int flags : BOTH) {
			WordprocessingMLPackage pkg = WordprocessingMLPackage.createPackage();
			pkg.getMainDocumentPart().getStyleDefinitionsPart().getJaxbElement().getStyle().add((org.docx4j.wml.Style) XmlUtils.unmarshalString(
					"<w:style " + NS + " w:type=\"character\" w:styleId=\"RunStyle\"><w:name w:val=\"Run Style\"/><w:rPr><w:b/></w:rPr></w:style>"));
			String run = shape(null).replace("<w:r><w:drawing>", "<w:r><w:rPr><w:rStyle w:val=\"RunStyle\"/></w:rPr><w:drawing>");
			pkg.getMainDocumentPart().setJaxbElement((org.docx4j.wml.Document) XmlUtils.unmarshalString("<w:document " + NS + "><w:body><w:p>" + run
					+ "</w:p></w:body></w:document>"));
			HTMLSettings settings = Docx4J.createHTMLSettings();
			settings.setOpcPackage(pkg);
			ByteArrayOutputStream baos = new ByteArrayOutputStream();
			Docx4J.toHTML(settings, baos, flags);
			org.jsoup.nodes.Document doc = Jsoup.parse(baos.toString("UTF-8"));
			Element box = doc.selectFirst("span.docx4j-shape");
			assertTrue(route(flags) + ": " + doc.body().html(), box != null);
			assertEquals(route(flags), "img", box.attr("role"));
			assertEquals(route(flags), "Shape 1", box.attr("aria-label"));
			assertTrue(route(flags) + ": inside the run's span", box.parent().hasClass("RunStyle"));
		}
	}

	/** A shape with no text and nothing to draw it, in its line, keeps its space: an empty box, an image */
	@Test
	public void aShapeWithNoTextKeepsItsSpace() throws Exception {
		for (int flags : BOTH) {
			org.jsoup.nodes.Document doc = Jsoup.parse(html("<w:p>" + shape(null) + "</w:p>", flags));
			Element box = doc.selectFirst("span.docx4j-shape");
			assertTrue(route(flags), box != null);
			assertEquals(route(flags), "img", box.attr("role"));
			assertEquals(route(flags), "Shape 1", box.attr("aria-label"));
		}
	}
}
