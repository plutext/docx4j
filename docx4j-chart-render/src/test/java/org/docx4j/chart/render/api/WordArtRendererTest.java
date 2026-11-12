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
package org.docx4j.chart.render.api;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import java.awt.geom.Rectangle2D;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.util.ArrayList;
import java.util.List;

import org.docx4j.Docx4J;
import org.docx4j.XmlUtils;
import org.docx4j.com.microsoft.schemas.office.word.x2010.wordprocessingShape.CTWordprocessingShape;
import org.docx4j.convert.out.HTMLSettings;
import org.docx4j.dml.CTLineProperties;
import org.docx4j.dml.CTPresetTextShape;
import org.docx4j.dml.CTSolidColorFillProperties;
import org.docx4j.dml.STTextShapeType;
import org.docx4j.dml.wordprocessingDrawing.Anchor;
import org.docx4j.model.chart.ChartFonts;
import org.docx4j.model.color.ThemeColors;
import org.docx4j.model.shapes.ShapeGeometry;
import org.docx4j.model.shapes.ShapeOutlines;
import org.docx4j.model.shapes.TextEffects;
import org.docx4j.model.shapes.WordArtText;
import org.docx4j.openpackaging.packages.WordprocessingMLPackage;
import org.docx4j.wml.R;
import org.junit.Test;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

/**
 * CR-083 phases 1 and 2: WordArt read ({@link WordArtText}), its w14 effects as DrawingML ({@link TextEffects}), the
 * warp presets ({@link ShapeGeometry#textWarp}), and drawn ({@link WordArtRenderer}), in its own SVG and in the HTML
 * output. Phase 0's probes against Word are CR-083-notes/gate_wordart.py's; these hold the rules in place.
 */
public class WordArtRendererTest {

	private static final String NS = "xmlns:w=\"http://schemas.openxmlformats.org/wordprocessingml/2006/main\""
			+ " xmlns:wp=\"http://schemas.openxmlformats.org/drawingml/2006/wordprocessingDrawing\""
			+ " xmlns:a=\"http://schemas.openxmlformats.org/drawingml/2006/main\""
			+ " xmlns:w14=\"http://schemas.microsoft.com/office/word/2010/wordml\""
			+ " xmlns:wps=\"http://schemas.microsoft.com/office/word/2010/wordprocessingShape\"";
	private static final String SVG_NS = "http://www.w3.org/2000/svg";

	/** Word's own WordArt body properties, with this warp: wrap none, its text's size (spAutoFit) */
	private static String wordForm(String warp) {
		return "<wps:bodyPr rot=\"0\" vert=\"horz\" wrap=\"none\" lIns=\"91440\" tIns=\"45720\" rIns=\"91440\" bIns=\"45720\" fromWordArt=\"0\""
				+ " anchor=\"t\" anchorCtr=\"0\"><a:prstTxWarp prst=\"" + warp + "\"><a:avLst/></a:prstTxWarp><a:spAutoFit/></wps:bodyPr>";
	}

	/** A fixed box: wrap square, no autofit, this warp (or none) */
	private static String fixed(String warp) {
		return "<wps:bodyPr wrap=\"square\" lIns=\"91440\" tIns=\"45720\" rIns=\"91440\" bIns=\"45720\" anchor=\"t\">"
				+ (warp == null ? "" : "<a:prstTxWarp prst=\"" + warp + "\"><a:avLst/></a:prstTxWarp>") + "<a:noAutofit/></wps:bodyPr>";
	}

	private static String run(String rPr, String text) {
		return "<w:r><w:rPr><w:sz w:val=\"72\"/>" + rPr + "</w:rPr><w:t xml:space=\"preserve\">" + text + "</w:t></w:r>";
	}

	private static String para(String runs) {
		return "<w:p><w:pPr><w:spacing w:before=\"0\" w:after=\"0\" w:line=\"240\" w:lineRule=\"auto\"/><w:jc w:val=\"center\"/></w:pPr>" + runs
				+ "</w:p>";
	}

	/** A text box 288 x 144pt (Word's own WordArt stores 144 x 144 whatever its text), anchored on the page */
	private static String anchor(String content, String bodyPr) {
		return "<w:p><w:r><w:drawing><wp:anchor distT=\"0\" distB=\"0\" distL=\"0\" distR=\"0\" simplePos=\"0\" relativeHeight=\"1\" behindDoc=\"0\""
				+ " locked=\"0\" layoutInCell=\"1\" allowOverlap=\"1\"><wp:simplePos x=\"0\" y=\"0\"/>"
				+ "<wp:positionH relativeFrom=\"page\"><wp:posOffset>1828800</wp:posOffset></wp:positionH>"
				+ "<wp:positionV relativeFrom=\"page\"><wp:posOffset>3200400</wp:posOffset></wp:positionV>"
				+ "<wp:extent cx=\"3657600\" cy=\"1828800\"/><wp:wrapNone/><wp:docPr id=\"1\" name=\"Text Box 1\"/>"
				+ "<a:graphic><a:graphicData uri=\"http://schemas.microsoft.com/office/word/2010/wordprocessingShape\"><wps:wsp><wps:cNvSpPr txBox=\"1\"/>"
				+ "<wps:spPr><a:xfrm><a:off x=\"0\" y=\"0\"/><a:ext cx=\"3657600\" cy=\"1828800\"/></a:xfrm><a:prstGeom prst=\"rect\"><a:avLst/></a:prstGeom>"
				+ "<a:noFill/><a:ln><a:noFill/></a:ln></wps:spPr><wps:txbx><w:txbxContent>" + content + "</w:txbxContent></wps:txbx>" + bodyPr
				+ "</wps:wsp></a:graphicData></a:graphic></wp:anchor></w:drawing></w:r></w:p>";
	}

	private static WordprocessingMLPackage pkg(String body) throws Exception {
		WordprocessingMLPackage pkg = WordprocessingMLPackage.createPackage();
		pkg.getMainDocumentPart().setJaxbElement((org.docx4j.wml.Document) XmlUtils.unmarshalString("<w:document " + NS + "><w:body>" + body
				+ "</w:body></w:document>"));
		return pkg;
	}

	/** The first text box's shape */
	private static CTWordprocessingShape shape(WordprocessingMLPackage pkg) {
		org.docx4j.wml.P p = (org.docx4j.wml.P) pkg.getMainDocumentPart().getContent().get(0);
		R r = (R) p.getContent().get(0);
		org.docx4j.wml.Drawing d = (org.docx4j.wml.Drawing) XmlUtils.unwrap(r.getContent().get(0));
		Anchor a = (Anchor) d.getAnchorOrInline().get(0);
		return a.getGraphic().getGraphicData().getWordprocessingShape();
	}

	private static WordArtText read(String content, String bodyPr) throws Exception {
		WordprocessingMLPackage pkg = pkg(anchor(content, bodyPr));
		return WordArtText.of(shape(pkg), pkg);
	}

	private static WordArtRenderer.Drawn draw(String content, String bodyPr) throws Exception {
		WordprocessingMLPackage pkg = pkg(anchor(content, bodyPr));
		WordArtText art = WordArtText.of(shape(pkg), pkg);
		assertNotNull(art);
		return WordArtRenderer.draw(art, ChartFonts.of(pkg.getMainDocumentPart()), ThemeColors.of(pkg), 3657600, 1828800, null);
	}

	private static final String OUTLINE = "<w14:textOutline w14:w=\"12700\" w14:cap=\"flat\" w14:cmpd=\"sng\" w14:algn=\"ctr\"><w14:solidFill>"
			+ "<w14:srgbClr w14:val=\"FF0000\"/></w14:solidFill><w14:prstDash w14:val=\"solid\"/><w14:round/></w14:textOutline>";

	// ------------------------------------------------------------------ which text boxes

	@Test
	public void whichTextBoxesAreWordArt() throws Exception {
		String text = para(run("", "WordArt"));
		assertNotNull("a warp", read(text, fixed("textArchUp")));
		assertNotNull("Word's own form: textNoShape, wrap none", read(text, wordForm("textNoShape")));
		assertNotNull("an outline Word draws as paths", read(para(run(OUTLINE, "WordArt")), fixed(null)));
		assertNull("an ordinary text box", read(text, fixed(null)));
		assertNull("textNoShape in a fixed box: a shape's text, not WordArt (the corpora have 26)", read(text, fixed("textNoShape")));
		assertNull("a shadow alone: real text, left to the text box", read(para(run("<w14:shadow w14:blurRad=\"50800\" w14:dist=\"38100\""
				+ " w14:dir=\"2700000\" w14:sx=\"100000\" w14:sy=\"100000\" w14:kx=\"0\" w14:ky=\"0\" w14:algn=\"tl\"><w14:srgbClr w14:val=\"000000\"/>"
				+ "</w14:shadow>", "WordArt")), fixed(null)));
		assertNull("a table in it: left as text", read("<w:tbl><w:tblGrid><w:gridCol w:w=\"1000\"/></w:tblGrid><w:tr><w:tc>" + text
				+ "</w:tc></w:tr></w:tbl>" + text, fixed("textArchUp")));
	}

	@Test
	public void runsAreReadWithTheirEffects() throws Exception {
		WordArtText art = read(para(run("<w:b/>" + OUTLINE, "Word") + "<w:r><w:br/></w:r>" + run("<w:color w:val=\"0000FF\"/>", "Art")),
				fixed("textInflate"));
		List<WordArtText.Run> runs = art.getParagraphs().get(0).getRuns();
		assertEquals(3, runs.size());
		assertEquals("Word", runs.get(0).getText());
		assertTrue(runs.get(0).isBold());
		assertEquals(36, runs.get(0).getSize(), 1e-9);
		assertNotNull(runs.get(0).getOutline());
		assertTrue(runs.get(1).isBreak());
		assertEquals("0000FF", String.format("%02X%02X%02X", runs.get(2).getColour().getRed(), runs.get(2).getColour().getGreen(),
				runs.get(2).getColour().getBlue()));
		assertEquals("textInflate", art.getWarp());
		assertEquals("warped: outlines", WordArtText.Paint.PATHS, runs.get(2).paint(true));
		assertEquals("flat, solid: real text", WordArtText.Paint.TEXT, runs.get(2).paint(false));
		assertEquals("flat, outlined: outlines", WordArtText.Paint.PATHS, runs.get(0).paint(false));
	}

	@Test
	public void aFieldsCodeIsNotItsText() throws Exception {
		WordArtText art = read(para("<w:r><w:fldChar w:fldCharType=\"begin\"/></w:r><w:r><w:instrText xml:space=\"preserve\"> DATE </w:instrText></w:r>"
				+ "<w:r><w:fldChar w:fldCharType=\"separate\"/></w:r>" + run("", "today") + "<w:r><w:fldChar w:fldCharType=\"end\"/></w:r>"),
				fixed("textArchUp"));
		StringBuilder b = new StringBuilder();
		for (WordArtText.Run r : art.getParagraphs().get(0).getRuns()) b.append(r.getText());
		assertEquals("today", b.toString());
	}

	// ------------------------------------------------------------------ w14 as DrawingML

	/** w14:alpha is a transparency (phase 0: 40000 drawn at 60%); a:alpha an opacity */
	@Test
	public void w14AlphaIsATransparency() throws Exception {
		WordArtText art = read(para(run("<w14:textFill><w14:solidFill><w14:srgbClr w14:val=\"FF0000\"><w14:alpha w14:val=\"40000\"/></w14:srgbClr>"
				+ "</w14:solidFill></w14:textFill>", "WordArt")), fixed(null));
		assertNotNull("a fill with transparency is drawn as paths", art);
		CTSolidColorFillProperties fill = (CTSolidColorFillProperties) art.getParagraphs().get(0).getRuns().get(0).getFill();
		Object alpha = XmlUtils.unwrap(fill.getSrgbClr().getEGColorTransform().get(0));
		assertEquals(60000, ((org.docx4j.dml.CTPositiveFixedPercentage) alpha).getVal());
		assertTrue(TextEffects.hasAlpha(fill));
	}

	@Test
	public void anOutlineIsALine() throws Exception {
		WordArtText art = read(para(run(OUTLINE, "WordArt")), fixed(null));
		CTLineProperties ln = art.getParagraphs().get(0).getRuns().get(0).getOutline();
		assertEquals(Integer.valueOf(12700), ln.getW());
		assertEquals("flat", ln.getCap().value());
		assertNotNull(ln.getSolidFill().getSrgbClr());
		assertNotNull(ln.getRound());
	}

	// ------------------------------------------------------------------ the warp presets

	@Test
	public void theWarpPresetsByTheirPaths() {
		int one = 0, two = 0, more = 0;
		for (String name : org.docx4j.model.shapes.PresetGeometries.getTextWarps().keySet()) {
			CTPresetTextShape w = new CTPresetTextShape();
			w.setPrst(STTextShapeType.fromValue(name));
			ShapeOutlines o = ShapeGeometry.textWarp(w, 128, 44);
			assertNull(name + ": " + o.getDetail(), o.getProblem());
			int n = o.getOutlines().size();
			if ("textNoShape".equals(name)) assertEquals(0, n);
			else if (n == 1) one++;
			else if (n == 2) two++;
			else more++;
		}
		assertEquals("Follow Path: textArchUp, textArchDown, textCircle", 3, one);
		assertEquals(33, two);
		assertEquals("textButton, textDeflateInflate, textButtonPour, textDeflateInflateDeflate", 4, more);
	}

	/** Word reads textDeflate's ss as the height (phase 0: in a tall box 1.59 pt off read as min(w, h), 0.16 as the height) */
	@Test
	public void textDeflateTakesSsAsTheHeight() {
		CTPresetTextShape w = new CTPresetTextShape();
		w.setPrst(STTextShapeType.TEXT_DEFLATE);
		ShapeOutlines o = ShapeGeometry.textWarp(w, 100, 200);
		// the top path's middle dips by adj (18750) of ss: 37.5 pt of the height 200, where min(w, h) would give 18.75
		Rectangle2D top = o.getOutlines().get(0).getPath().getBounds2D();
		assertEquals(37.5, top.getMaxY(), 0.5);
	}

	// ------------------------------------------------------------------ drawn

	/** Word's own form: its box its text's (wrap none, spAutoFit), not the extent it stores; the text real text */
	@Test
	public void wordsFormIsItsTextsSize() throws Exception {
		WordArtRenderer.Drawn d = draw(para(run("", "WordArt")), wordForm("textNoShape"));
		assertTrue("narrower than the 288pt stored: " + d.getWidth(), d.getWidth() < 200 && d.getWidth() > 100);
		assertTrue("one line of 36pt with the insets: " + d.getHeight(), d.getHeight() > 40 && d.getHeight() < 60);
		Element text = texts(d).get(0);
		assertEquals("WordArt", text.getTextContent());
		assertFalse("real text, in its colour", "none".equals(text.getAttribute("fill")));
		assertTrue("nothing drawn as outlines", d.getDrawing().getItems().isEmpty());
	}

	/** A warp: the glyphs as paths over invisible text (D2), the block's ink between the paths (textPlain: the rectangle) */
	@Test
	public void aWarpIsOutlinesOverInvisibleText() throws Exception {
		WordArtRenderer.Drawn d = draw(para(run("", "WordArt")), fixed("textPlain"));
		Element text = texts(d).get(0);
		assertEquals("WordArt", text.getTextContent());
		assertEquals("none", text.getAttribute("fill"));
		Rectangle2D ink = null;
		for (Object o : d.getDrawing().getItems()) {
			if (o instanceof Drawing.Area && "wordart.fill".equals(((Drawing.Area) o).getRole())) {
				ink = WordArtWarps.tight(((Drawing.Area) o).getShape());
			}
		}
		assertNotNull(ink);
		// the text rectangle: the 288 x 144 box less the insets
		assertEquals(7.2, ink.getMinX(), 0.3);
		assertEquals(3.6, ink.getMinY(), 0.3);
		assertEquals(288 - 7.2, ink.getMaxX(), 0.3);
		assertEquals(144 - 3.6, ink.getMaxY(), 0.3);
	}

	/** No fill and no outline: nothing drawn, the text kept invisibly */
	@Test
	public void noFillNoOutlineDrawsNothing() throws Exception {
		WordArtRenderer.Drawn d = draw(para(run("<w14:textOutline w14:w=\"9525\"><w14:noFill/></w14:textOutline><w14:textFill><w14:noFill/></w14:textFill>",
				"WordArt")), fixed("textPlain"));
		assertTrue(d.getDrawing().getItems().isEmpty());
		assertEquals("none", texts(d).get(0).getAttribute("fill"));
	}

	/** Three lines on textDeflateInflate's two bands: the first two in the top band, the third in the bottom */
	@Test
	public void moreLinesThanBandsFillTheFirst() throws Exception {
		WordArtRenderer.Layout layout = new WordArtRenderer.Layout();
		for (int i = 0; i < 3; i++) {
			WordArtRenderer.Line l = new WordArtRenderer.Line();
			WordArtRenderer.Glyph g = new WordArtRenderer.Glyph();
			g.outline = new Rectangle2D.Double(0, 0, 1, 1);
			l.glyphs.add(g);
			layout.lines.add(l);
		}
		List<List<WordArtRenderer.Line>> bands = WordArtWarps.bands(layout.lines, 2);
		assertEquals(2, bands.get(0).size());
		assertEquals(1, bands.get(1).size());
		bands = WordArtWarps.bands(layout.lines.subList(0, 1), 3);
		assertEquals("one line: the first band", 1, bands.get(0).size());
		assertTrue(bands.get(1).isEmpty() && bands.get(2).isEmpty());
	}

	/** Follow Path's scale (phase 0's table, Jason's document): Word's own form 0.727; a long line in it 0.858; a wide box
	 *  more; an arch adjusted short much less */
	@Test
	public void followPathScales() {
		assertEquals(0.727, WordArtWarps.scale(143.1, 43.9, 127.92, 1), 0.002);
		assertEquals(0.858, WordArtWarps.scale(390, 44.1, 380, 1), 0.01);
		assertEquals(0.8506, WordArtWarps.scale(2.57 * 127.92, 136.6, 127.92, 1), 0.002);
		assertEquals(0.394, WordArtWarps.scale(71.1, 136.6, 127.92, 1), 0.01);
		assertEquals(0.727 * 0.823, WordArtWarps.scale(143.1, 43.9, 127.92, 2), 0.002);
	}

	/** A Follow Path WordArt: each glyph's invisible text where the glyph is, turned */
	@Test
	public void followPathTextIsWhereItsGlyphsAre() throws Exception {
		WordArtRenderer.Drawn d = draw(para(run("", "WordArt")), wordForm("textArchUp"));
		List<Element> t = texts(d);
		assertEquals("a span a glyph", 7, t.size());
		assertTrue(t.get(0).getAttribute("transform").startsWith("matrix("));
		assertFalse(d.getDrawing().getItems().isEmpty());
	}

	private static List<Element> texts(WordArtRenderer.Drawn d) {
		NodeList nl = d.toSvg().getElementsByTagNameNS(SVG_NS, "text");
		List<Element> out = new ArrayList<Element>();
		for (int i = 0; i < nl.getLength(); i++) out.add((Element) nl.item(i));
		return out;
	}

	// ------------------------------------------------------------------ HTML

	/** In HTML: an image of the box Word draws, its text in its SVG once, no text container */
	@Test
	public void inHtmlItIsItsDrawing() throws Exception {
		WordprocessingMLPackage pkg = pkg(anchor(para(run("", "WordArt")), wordForm("textInflate")) + "<w:p><w:r><w:t>after</w:t></w:r></w:p>");
		for (int flags : new int[] { Docx4J.FLAG_NONE, Docx4J.FLAG_EXPORT_PREFER_XSL }) {
			HTMLSettings settings = Docx4J.createHTMLSettings();
			settings.setOpcPackage(pkg);
			ByteArrayOutputStream baos = new ByteArrayOutputStream();
			Docx4J.toHTML(settings, baos, flags);
			String html = baos.toString("UTF-8").replaceFirst("<!DOCTYPE[^>]*>", "");
			org.w3c.dom.Document doc = XmlUtils.getNewDocumentBuilder().parse(new ByteArrayInputStream(html.getBytes("UTF-8")));
			Element box = null;
			NodeList nl = doc.getElementsByTagName("*");
			int containers = 0;
			for (int i = 0; i < nl.getLength(); i++) {
				Element e = (Element) nl.item(i);
				if ("docx4j-shape".equals(e.getAttribute("class"))) box = e;
				if ("docx4j-shape-text".equals(e.getAttribute("class"))) containers++;
			}
			assertNotNull(box);
			assertEquals("img", box.getAttribute("role"));
			assertEquals("WordArt", box.getAttribute("aria-label"));
			assertEquals(0, containers);
			assertEquals(1, box.getElementsByTagNameNS(SVG_NS, "svg").getLength());
			String all = doc.getDocumentElement().getTextContent();
			assertEquals("its text once", all.indexOf("WordArt"), all.lastIndexOf("WordArt"));
			assertTrue(all.contains("WordArt"));
		}
	}
}
