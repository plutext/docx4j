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

	/**
	 * Phase 3: a run's glow, shadow and reflection as an a:effectLst, the colours' w14:alpha as a:alpha's opacity and the
	 * theme colours' transforms kept; the gallery's style 15 (sx and sy 0, algn none) an inner shadow, which keeps only
	 * its blur, distance and direction; algn none on an outer shadow read as ctr
	 */
	@Test
	public void runEffectsAreAnEffectList() throws Exception {
		String glow = "<w14:glow w14:rad=\"63500\"><w14:schemeClr w14:val=\"accent2\"><w14:lumMod w14:val=\"75000\"/>"
				+ "<w14:alpha w14:val=\"60000\"/></w14:schemeClr></w14:glow>";
		String shadow = "<w14:shadow w14:blurRad=\"50800\" w14:dist=\"38100\" w14:dir=\"2700000\" w14:sx=\"50000\" w14:sy=\"150000\" w14:kx=\"0\""
				+ " w14:ky=\"0\" w14:algn=\"ctr\"><w14:srgbClr w14:val=\"000000\"><w14:alpha w14:val=\"60000\"/></w14:srgbClr></w14:shadow>";
		String reflection = "<w14:reflection w14:blurRad=\"6350\" w14:stA=\"55000\" w14:stPos=\"0\" w14:endA=\"300\" w14:endPos=\"45500\""
				+ " w14:dist=\"50800\" w14:dir=\"5400000\" w14:fadeDir=\"5400000\" w14:sx=\"100000\" w14:sy=\"-100000\" w14:kx=\"0\" w14:ky=\"0\""
				+ " w14:algn=\"bl\"/>";
		WordArtText art = read(para(run(glow + shadow + reflection, "WordArt")), wordForm("textNoShape"));
		WordArtText.Run r = art.getParagraphs().get(0).getRuns().get(0);
		org.docx4j.dml.CTEffectList fx = r.getEffects();
		assertNotNull(fx);
		assertEquals(63500, fx.getGlow().getRad());
		org.docx4j.dml.CTSchemeColor sc = fx.getGlow().getSchemeClr();
		assertEquals("accent2", sc.getVal().value());
		assertEquals("the transforms in order", 2, sc.getEGColorTransform().size());
		assertEquals(75000, ((org.docx4j.dml.CTPercentage) XmlUtils.unwrap(sc.getEGColorTransform().get(0))).getVal());
		assertEquals("w14:alpha 60000: opacity 40%", 40000,
				((org.docx4j.dml.CTPositiveFixedPercentage) XmlUtils.unwrap(sc.getEGColorTransform().get(1))).getVal());
		org.docx4j.dml.CTOuterShadowEffect o = fx.getOuterShdw();
		assertNotNull(o);
		assertNull(fx.getInnerShdw());
		assertEquals(50800, o.getBlurRad());
		assertEquals(38100, o.getDist());
		assertEquals(2700000, o.getDir());
		assertEquals(50000, o.getSx());
		assertEquals(150000, o.getSy());
		assertEquals(org.docx4j.dml.STRectAlignment.CTR, o.getAlgn());
		org.docx4j.dml.CTReflectionEffect rf = fx.getReflection();
		assertEquals(55000, rf.getStA());
		assertEquals(45500, rf.getEndPos());
		assertEquals(50800, rf.getDist());
		assertEquals(-100000, rf.getSy());
		assertEquals(org.docx4j.dml.STRectAlignment.BL, rf.getAlgn());

		String inner = "<w14:shadow w14:blurRad=\"63500\" w14:dist=\"50800\" w14:dir=\"13500000\" w14:sx=\"0\" w14:sy=\"0\" w14:kx=\"0\" w14:ky=\"0\""
				+ " w14:algn=\"none\"><w14:srgbClr w14:val=\"000000\"><w14:alpha w14:val=\"50000\"/></w14:srgbClr></w14:shadow>";
		fx = read(para(run(inner, "WordArt")), wordForm("textNoShape")).getParagraphs().get(0).getRuns().get(0).getEffects();
		assertNull(fx.getOuterShdw());
		org.docx4j.dml.CTInnerShadowEffect is = fx.getInnerShdw();
		assertNotNull("style 15: an inner shadow", is);
		assertEquals(63500, is.getBlurRad());
		assertEquals(50800, is.getDist());
		assertEquals(13500000, is.getDir());

		String none = inner.replace("w14:sx=\"0\" w14:sy=\"0\"", "w14:sx=\"100000\" w14:sy=\"100000\"");
		fx = read(para(run(none, "WordArt")), wordForm("textNoShape")).getParagraphs().get(0).getRuns().get(0).getEffects();
		assertNotNull("algn none at 100%: an outer shadow", fx.getOuterShdw());
		assertEquals("algn none read as ctr (we16-10)", org.docx4j.dml.STRectAlignment.CTR, fx.getOuterShdw().getAlgn());

		assertNull("no effects", read(para(run(OUTLINE, "WordArt")), fixed(null)).getParagraphs().get(0).getRuns().get(0).getEffects());
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

	/**
	 * Follow Path's scale, D / (D + 2T), against Word's sizes in the Follow Path round (Aptos 36 pt; the text rectangle,
	 * the ink unscaled): "WordArt" (ink 23.98 pt) in Word's own form, in a square box and in a wide one; "wave" (17.74);
	 * two lines (their inks added); "_____" (1.66), hardly scaled
	 */
	@Test
	public void followPathScales() {
		assertEquals(26.18 / 36, WordArtWarps.pathScale(127.9, 35.8, 23.98), 0.002);
		assertEquals(27.00 / 36, WordArtWarps.pathScale(127.9, 143.8, 23.98), 0.002);
		assertEquals(30.86 / 36, WordArtWarps.pathScale(287.8, 143.8, 23.98), 0.002);
		assertEquals(28.87 / 36, WordArtWarps.pathScale(79.8, 143.8, 17.74), 0.002);
		assertEquals(27.00 / 36, WordArtWarps.pathScale(287.8, 143.8, 2 * 23.98), 0.002);
		assertEquals(35.18 / 36, WordArtWarps.pathScale(82.8, 143.8, 1.66), 0.002);
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

	// ------------------------------------------------------------------ the review of phases 1 and 2

	/** Drawn with an extent of this size */
	private static WordArtRenderer.Drawn draw(String content, String bodyPr, long cx, long cy) throws Exception {
		WordprocessingMLPackage pkg = pkg(anchor(content, bodyPr));
		WordArtText art = WordArtText.of(shape(pkg), pkg);
		assertNotNull(art);
		return WordArtRenderer.draw(art, ChartFonts.of(pkg.getMainDocumentPart()), ThemeColors.of(pkg), cx, cy, null);
	}

	private static List<Drawing.Area> areas(WordArtRenderer.Drawn d, String role) {
		List<Drawing.Area> out = new ArrayList<Drawing.Area>();
		for (Object o : d.getDrawing().getItems()) {
			if (o instanceof Drawing.Area && role.equals(((Drawing.Area) o).getRole())) out.add((Drawing.Area) o);
		}
		return out;
	}

	/**
	 * A run's underline, strike, highlight and superscript (the review's finding 1): drawn on flat WordArt as the FO
	 * output draws them, where the line layout drew none; on a warp, reported and not drawn
	 */
	@Test
	public void decorationsAreDrawnOnFlatWordArt() throws Exception {
		String runs = run("<w:u w:val=\"single\"/>", "Under") + run("<w:strike/>", "Struck") + run("<w:highlight w:val=\"yellow\"/>", "Lit");
		WordArtRenderer.Drawn d = draw(para(runs), wordForm("textNoShape"));
		List<Drawing.Area> lines = areas(d, "wordart.line");
		assertEquals("an underline and a strike", 2, lines.size());
		List<Drawing.Area> lit = areas(d, "wordart.highlight");
		assertEquals(1, lit.size());
		assertEquals(new java.awt.Color(255, 255, 0), lit.get(0).getFill());
		double baseline = Double.parseDouble(texts(d).get(0).getAttribute("y"));
		Rectangle2D under = lines.get(0).getShape().getBounds2D(), struck = lines.get(1).getShape().getBounds2D();
		assertTrue("the underline below the baseline: " + under, under.getMinY() > baseline);
		assertTrue("the strike above it: " + struck, struck.getMaxY() < baseline);
		assertTrue("the highlight behind the text", d.getDrawing().getItems().indexOf(lit.get(0)) < d.getDrawing().getItems().indexOf(lines.get(0)));
		assertFalse(d.getProblems().contains(WordArtText.DECORATION));

		// a superscript: 65% of the size, raised 0.36 of it, as the FO output sets it
		d = draw(para(run("", "x") + run("<w:vertAlign w:val=\"superscript\"/>", "2")), wordForm("textNoShape"));
		List<Element> t = texts(d);
		assertEquals(2, t.size());
		assertEquals(36 * 0.65, Double.parseDouble(t.get(1).getAttribute("font-size")), 1e-6);
		assertEquals(36 * 0.36, Double.parseDouble(t.get(0).getAttribute("y")) - Double.parseDouble(t.get(1).getAttribute("y")), 1e-6);

		// warped: reported, not drawn (how Word warps them is not measured)
		d = draw(para(run("<w:u w:val=\"single\"/>", "WordArt")), fixed("textInflate"));
		assertTrue(d.getProblems().contains(WordArtText.DECORATION));
		assertTrue(areas(d, "wordart.line").isEmpty());
	}

	/** w:u="words": the words underlined, not the spaces between them */
	@Test
	public void wordsUnderlinesTheWordsAlone() throws Exception {
		WordArtRenderer.Drawn d = draw(para(run("<w:u w:val=\"words\"/>", "Word Art")), wordForm("textNoShape"));
		assertEquals(2, areas(d, "wordart.line").size());
	}

	/** The warp's points capped (finding 2): a piece a hundredth of the glyph's size past 50 pt; past the limit, unwarped */
	@Test
	public void aWarpPastTheLimitIsDrawnUnwarped() throws Exception {
		assertEquals(0.5, TextWarp.segment(36), 1e-9);
		assertEquals(4, TextWarp.segment(400), 1e-9);
		int was = TextWarp.maxPoints;
		try {
			TextWarp.maxPoints = 100;
			WordArtRenderer.Drawn d = draw(para(run("", "WordArt")), fixed("textInflate"));
			assertTrue(d.getProblems().contains(WordArtText.OVER_LIMIT));
			assertFalse("drawn as if it had no warp: real text", "none".equals(texts(d).get(0).getAttribute("fill")));
		} finally {
			TextWarp.maxPoints = was;
		}
		WordArtRenderer.Drawn d = draw(para(run("", "WordArt")), fixed("textInflate"));
		assertFalse(d.getProblems().contains(WordArtText.OVER_LIMIT));
	}

	/** An empty paragraph is a line as high as its mark (finding 3): it was no line at all */
	@Test
	public void anEmptyParagraphIsItsMarksLine() throws Exception {
		String text = para(run("", "WordArt"));
		String empty = "<w:p><w:pPr><w:spacing w:before=\"0\" w:after=\"0\" w:line=\"240\" w:lineRule=\"auto\"/><w:rPr><w:sz w:val=\"40\"/></w:rPr></w:pPr></w:p>";
		double without = draw(text, wordForm("textNoShape")).getHeight();
		double with = draw(empty + text, wordForm("textNoShape")).getHeight();
		assertTrue("a 20 pt mark's line: " + (with - without), with - without > 20 && with - without < 30);
		WordArtText art = read(empty + text, wordForm("textNoShape"));
		assertTrue(art.getParagraphs().get(0).getRuns().get(0).isMark());
	}

	/** The lines are broken at the width of the text rectangle the geometry gives (finding 4): an ellipse's is narrower */
	@Test
	public void linesBreakAtTheGeometrysTextRectangle() throws Exception {
		String words = para(run(OUTLINE, "WordArt drawn in a box that is shaped as an ellipse"));
		int inRect = lines(draw(words, fixed(null)));
		WordprocessingMLPackage pkg = pkg(anchor(words, fixed(null)).replace("prst=\"rect\"", "prst=\"ellipse\""));
		WordArtText art = WordArtText.of(shape(pkg), pkg);
		int inEllipse = lines(WordArtRenderer.draw(art, ChartFonts.of(pkg.getMainDocumentPart()), ThemeColors.of(pkg), 3657600, 1828800, null));
		assertTrue(inRect + " lines in the rectangle, " + inEllipse + " in the ellipse", inEllipse > inRect);
	}

	/** The lines of invisible text, by their baselines */
	private static int lines(WordArtRenderer.Drawn d) {
		java.util.Set<String> ys = new java.util.HashSet<String>();
		for (Element e : texts(d)) ys.add(e.getAttribute("y"));
		return ys.size();
	}

	/** The alternative text has no double space where a line wraps (finding 5) */
	@Test
	public void theTextHasOneSpaceWhereALineWraps() throws Exception {
		WordArtRenderer.Drawn d = draw(para(run("", "Hello World Hello World")), fixed("textInflate"));
		assertTrue(lines(d) > 1);
		assertEquals("Hello World Hello World", d.getText());
	}

	/** What isn't drawn is reported (finding 6): a gradient with no stops, a face with no font file and no substitute */
	@Test
	public void whatIsNotDrawnIsReported() throws Exception {
		WordArtRenderer.Drawn d = draw(para(run("<w14:textFill><w14:gradFill><w14:gsLst/><w14:lin w14:ang=\"0\" w14:scaled=\"0\"/></w14:gradFill>"
				+ "</w14:textFill>", "WordArt")), fixed("textInflate"));
		assertTrue(d.getProblems().toString(), d.getProblems().contains(WordArtText.FILL));
		// AWT's own fallback (no file, and docx4j's mapping found no substitute, which FOP would draw too)
		assertTrue(WordArtRenderer.fellBack(new java.awt.Font("No Such Face 4711", java.awt.Font.PLAIN, 12), "No Such Face 4711"));
		assertFalse(WordArtRenderer.fellBack(new java.awt.Font("Serif", java.awt.Font.PLAIN, 12), "Serif"));
	}

	/** A family with a quote in its name is quoted with the other kind (finding 7) */
	@Test
	public void aQuoteInAFamilyIsQuoted() {
		assertEquals("'Aptos'", WordArtRenderer.quoted("Aptos"));
		assertEquals("\"O'Brien Sans\"", WordArtRenderer.quoted("O'Brien Sans"));
		assertEquals("\"Both'\\\"\"", WordArtRenderer.quoted("Both'\""));
	}

	/** A style changed after a first conversion counts (finding 8): the styles aren't cached */
	@Test
	public void aStyleChangedLaterCounts() throws Exception {
		WordprocessingMLPackage pkg = pkg(anchor(para("<w:r><w:rPr><w:rStyle w:val=\"Fancy\"/><w:sz w:val=\"72\"/></w:rPr><w:t>WordArt</w:t></w:r>"),
				fixed(null)));
		org.docx4j.wml.Styles styles = pkg.getMainDocumentPart().getStyleDefinitionsPart().getContents();
		org.docx4j.wml.Style fancy = (org.docx4j.wml.Style) XmlUtils.unmarshalString("<w:style " + NS + " w:type=\"character\" w:styleId=\"Fancy\">"
				+ "<w:name w:val=\"Fancy\"/><w:rPr><w:b/></w:rPr></w:style>");
		styles.getStyle().add(fancy);
		assertNull("no effect: text", WordArtText.of(shape(pkg), pkg));
		fancy.setRPr(((org.docx4j.wml.Style) XmlUtils.unmarshalString("<w:style " + NS + " w:type=\"character\" w:styleId=\"Fancy\"><w:rPr>"
				+ OUTLINE + "</w:rPr></w:style>")).getRPr());
		assertNotNull("its outline now", WordArtText.of(shape(pkg), pkg));
	}

	/** A box of no width its text doesn't size stays text (finding 9); Word's own form sizes itself */
	@Test
	public void aBoxOfNoWidthStaysText() throws Exception {
		assertNull(draw(para(run(OUTLINE, "WordArt")), fixed(null), 0, 1828800));
		assertNotNull(draw(para(run("", "WordArt")), wordForm("textNoShape"), 0, 0));
	}

	// ------------------------------------------------------------------ phase 3: shadow, glow and reflection

	/** w14:shadow, lengths in points, its direction in degrees, black at w14:alpha 60000 */
	private static String shadow(double blur, double dist, double dir, int sx, int sy, String algn) {
		return "<w14:shadow w14:blurRad=\"" + Math.round(blur * 12700) + "\" w14:dist=\"" + Math.round(dist * 12700) + "\" w14:dir=\""
				+ Math.round(dir * 60000) + "\" w14:sx=\"" + sx + "\" w14:sy=\"" + sy + "\" w14:kx=\"0\" w14:ky=\"0\" w14:algn=\"" + algn + "\">"
				+ "<w14:srgbClr w14:val=\"000000\"><w14:alpha w14:val=\"60000\"/></w14:srgbClr></w14:shadow>";
	}

	private static String glow(double rad) {
		return "<w14:glow w14:rad=\"" + Math.round(rad * 12700) + "\"><w14:srgbClr w14:val=\"ED7D31\"><w14:alpha w14:val=\"60000\"/></w14:srgbClr></w14:glow>";
	}

	private static String reflection(double blur, double dist, double dir, int sy) {
		return "<w14:reflection w14:blurRad=\"" + Math.round(blur * 12700) + "\" w14:stA=\"55000\" w14:stPos=\"0\" w14:endA=\"300\""
				+ " w14:endPos=\"45500\" w14:dist=\"" + Math.round(dist * 12700) + "\" w14:dir=\"" + Math.round(dir * 60000) + "\" w14:fadeDir=\"5400000\""
				+ " w14:sx=\"100000\" w14:sy=\"" + sy + "\" w14:kx=\"0\" w14:ky=\"0\" w14:algn=\"bl\"/>";
	}

	/** Aptos' typographic box, which Word sets its lines by */
	private static final double ASC = 1923 / 2048.0, DESC = 577 / 2048.0;

	/** The room TextEffects.reach gives "WordArt" in Aptos at this size, above and below added */
	private static double room(String fx, double size) throws Exception {
		WordArtText.Run r = read(para(run(fx, "WordArt")), wordForm("textNoShape")).getParagraphs().get(0).getRuns().get(0);
		double[] reach = TextEffects.reach(r.getEffects(), size, ASC * size, DESC * size, 127.9 * size / 36);
		return reach[0] + reach[1] - (ASC + DESC) * size;
	}

	/**
	 * Word's room, measured (round cr083-phase3, phase 3 findings section 4): how much taller each probe's spAutoFit box
	 * is than one with no effect, "WordArt" in Aptos; Word sizes the box in twentieths of a point
	 */
	@Test
	public void theRoomIsWords() throws Exception {
		double t = 0.03;
		assertEquals("we10-05 shadow blur 8", 9.85, room(shadow(8, 0, 90, 100000, 100000, "tl"), 36), t);
		assertEquals("we10-11 shadow 6 pt down", 3.70, room(shadow(0, 6, 90, 100000, 100000, "tl"), 36), t);
		assertEquals("we10-21 shadow blur 4, 3 pt up", 4.90, room(shadow(4, 3, 270, 100000, 100000, "tl"), 36), t);
		assertEquals("we10-27 shadow scaled 150% about ctr", 21.95, room(shadow(0, 0, 90, 150000, 150000, "ctr"), 36), t);
		assertEquals("we11-08 glow 25", 23.10, room(glow(25), 36), t);
		assertEquals("we11-09 reflection", 5.25, room(reflection(0, 0, 90, -100000), 36), t);
		assertEquals("we11-13 reflection 4 pt down", 16.05, room(reflection(0, 4, 90, -100000), 36), t);
		assertEquals("we11-18 reflection sy -50%, 4 pt down", 8.60, room(reflection(0.5, 4, 90, -50000), 36), t);
		assertEquals("we11-29 reflection 16 pt up", 19.55, room(reflection(0, 16, 270, -100000), 36), t);
		assertEquals("we16-03 the inner shadow: none", 0, room(shadow(5, 4, 225, 0, 0, "none"), 36), 1e-9);
		assertEquals("we16-12 a shadow scaled to nothing about tl", 4.80, room(shadow(5, 4, 225, 0, 0, "tl"), 36), t);
		assertEquals("we12-29 at 12 pt", 3.40, room(shadow(0, 12, 90, 100000, 100000, "tl"), 12), t);
		assertEquals("we12-33 at 72 pt", 12.0, room(shadow(0, 12, 90, 100000, 100000, "tl"), 72), t);
		assertEquals("we21-01 at 96 pt", 14.66, room(shadow(0, 12, 90, 100000, 100000, "tl"), 96), t);
		assertEquals("across: none", 0, room(shadow(0, 12, 0, 100000, 100000, "tl"), 72), 1e-9);
		assertEquals(0.616, TextEffects.scale(36), 0.001);
	}

	/** Which of the root's children holds the node */
	private static int top(org.w3c.dom.Document svg, org.w3c.dom.Node n) {
		while (n.getParentNode() != svg.getDocumentElement()) n = n.getParentNode();
		int i = 0;
		for (org.w3c.dom.Node c = svg.getDocumentElement().getFirstChild(); c != n; c = c.getNextSibling()) i++;
		return i;
	}

	private static List<Drawing.Picture> pictures(Drawing d) {
		List<Drawing.Picture> out = new ArrayList<Drawing.Picture>();
		for (Object o : d.getItems()) if (o instanceof Drawing.Picture) out.add((Drawing.Picture) o);
		return out;
	}

	/**
	 * Drawn: flat, a picture a run on a line, under the text, the box taller by the room and the text lower by what is
	 * above; an inner shadow over the text, taking none; warped, the same room; on Follow Path a picture a glyph (D14)
	 */
	@Test
	public void effectsAreDrawnWithTheirRoom() throws Exception {
		WordArtRenderer.Drawn plain = draw(para(run("", "WordArt")), wordForm("textNoShape"));
		WordArtRenderer.Drawn blurred = draw(para(run(shadow(8, 0, 90, 100000, 100000, "tl"), "WordArt")), wordForm("textNoShape"));
		assertTrue(pictures(plain.getDrawing()).isEmpty());
		assertEquals("a picture under the text", 1, pictures(blurred.getDrawing()).size());
		WordArtText.Run r = read(para(run(shadow(8, 0, 90, 100000, 100000, "tl"), "WordArt")), wordForm("textNoShape")).getParagraphs().get(0)
				.getRuns().get(0);
		double size = r.getSize(), asc = (r.getAscent() + r.getLeading()) * size, desc = r.getDescent() * size;
		double[] reach = TextEffects.reach(r.getEffects(), size, asc, desc, 100);
		assertEquals("the box taller by the room", reach[0] - asc + reach[1] - desc, blurred.getHeight() - plain.getHeight(), 1e-6);
		double y0 = Double.parseDouble(texts(plain).get(0).getAttribute("y")), y1 = Double.parseDouble(texts(blurred).get(0).getAttribute("y"));
		assertEquals("the text lower by the room above (the SVG to 4 places)", reach[0] - asc, y1 - y0, 1e-3);
		assertTrue(blurred.getProblems().toString(), blurred.getProblems().isEmpty());

		WordArtRenderer.Drawn inner = draw(para(run(shadow(5, 4, 225, 0, 0, "none"), "WordArt")), wordForm("textNoShape"));
		assertTrue("an inner shadow is over the text", pictures(inner.getDrawing()).isEmpty());
		assertEquals(1, pictures(inner.getOver()).size());
		assertEquals("no room", plain.getHeight(), inner.getHeight(), 1e-6);
		org.w3c.dom.Document svg = inner.toSvg();
		NodeList images = svg.getElementsByTagNameNS(SVG_NS, "image");
		assertEquals(1, images.getLength());
		NodeList t = svg.getElementsByTagNameNS(SVG_NS, "text");
		assertTrue("after the text", top(svg, images.item(0)) > top(svg, t.item(0)));

		WordArtRenderer.Drawn arch = draw(para(run(glow(5), "WordArt")), wordForm("textArchUp"));
		assertEquals("Follow Path: a picture a glyph", 7, pictures(arch.getDrawing()).size());
		WordArtRenderer.Drawn wave = draw(para(run(glow(5), "Word") + run(glow(5), "Art")), wordForm("textWave1"));
		assertEquals("two paths: a picture a run", 2, pictures(wave.getDrawing()).size());
		WordArtRenderer.Drawn flat = draw(para(run(glow(5), "Word") + run(glow(5), "Art")), wordForm("textNoShape"));
		assertEquals("warped, the same room", flat.getHeight(), wave.getHeight(), 1e-6);
		for (Drawing.Picture p : pictures(flat.getDrawing())) assertNotNull("flat: within the box and its text rectangle", p.getClip());
		for (Drawing.Picture p : pictures(wave.getDrawing())) assertNull("warped: not cut", p.getClip());
	}
	// ------------------------------------------------------------------ phase 3: the review's findings

	/** The picture's alpha, 0 to 1, row by row from the top, each row's greatest */
	private static double[] rows(Drawing.Picture p) {
		java.awt.image.BufferedImage im = p.getImage();
		double[] out = new double[im.getHeight()];
		for (int y = 0; y < im.getHeight(); y++) for (int x = 0; x < im.getWidth(); x++) out[y] = Math.max(out[y], (im.getRGB(x, y) >>> 24) / 255.0);
		return out;
	}

	private static double peak(Drawing.Picture p) {
		double m = 0;
		for (double v : rows(p)) m = Math.max(m, v);
		return m;
	}

	/** Where the picture's alpha starts, in the drawing's frame: its first row with any */
	private static double firstRow(Drawing.Picture p) {
		double[] r = rows(p);
		for (int y = 0; y < r.length; y++) if (r[y] > 0.01) return p.getInto().getY() + y * p.getInto().getHeight() / r.length;
		return Double.NaN;
	}

	private static double baselineOf(WordArtRenderer.Drawn d) {
		return Double.parseDouble(texts(d).get(0).getAttribute("y"));
	}

	/**
	 * A reflection's fade starts half its move, at the reflection's scale, before its first edge: Word's we11-18 (sy -50%,
	 * 4 pt down) peaks as we11-13 (sy -100%) does, 0.376 (the review's finding 1: 0.251 before)
	 */
	@Test
	public void aReflectionFadesAlikeAtAnySy() throws Exception {
		Drawing.Picture full = pictures(draw(para(run(reflection(0, 4, 90, -100000), "WordArt")), wordForm("textNoShape")).getDrawing()).get(0);
		Drawing.Picture half = pictures(draw(para(run(reflection(0, 4, 90, -50000), "WordArt")), wordForm("textNoShape")).getDrawing()).get(0);
		assertEquals(peak(full), peak(half), 0.02);
	}

	/** A reflection is mirrored about the baseline, and of a descender nothing is drawn above it (we11-33) */
	@Test
	public void aReflectionIsCutAtTheBaseline() throws Exception {
		WordArtRenderer.Drawn d = draw(para(run(reflection(0, 0, 90, -100000), "Typography")), wordForm("textNoShape"));
		assertTrue(firstRow(pictures(d.getDrawing()).get(0)) >= baselineOf(d) - 0.5);
		// algn none on a reflection read as b, mirrored about the baseline as its room is given (the review's finding 2)
		WordArtRenderer.Drawn none = draw(para(run(reflection(0, 0, 90, -100000).replace("w14:algn=\"bl\"", "w14:algn=\"none\""), "WordArt")),
				wordForm("textNoShape"));
		assertTrue(firstRow(pictures(none.getDrawing()).get(0)) >= baselineOf(none) - 0.5);
	}

	/**
	 * Flat pictures are clipped to the box across and its text rectangle up and down, and the drawing's bounds take in
	 * only what is shown (finding 4); a turned box's clip is turned with it
	 */
	@Test
	public void flatPicturesAreClipped() throws Exception {
		WordArtRenderer.Drawn d = draw(para(run(reflection(0, 4, 90, -100000), "WordArt")), wordForm("textNoShape"));
		Drawing.Picture p = pictures(d.getDrawing()).get(0);
		Rectangle2D clip = p.getClip().getBounds2D();
		assertEquals(0, clip.getX(), 1e-9);
		assertEquals(d.getWidth(), clip.getWidth(), 1e-9);
		assertTrue("the bounds the box's: " + d.getBounds() + " for " + d.getHeight(), d.getBounds().getHeight() <= d.getHeight() + 1e-3);

		WordprocessingMLPackage pkg = pkg(anchor(para(run(shadow(0, 6, 90, 100000, 100000, "tl"), "WordArt")), wordForm("textNoShape"))
				.replace("<a:xfrm>", "<a:xfrm rot=\"1800000\">"));
		WordArtRenderer.Drawn turned = WordArtRenderer.draw(WordArtText.of(shape(pkg), pkg), ChartFonts.of(pkg.getMainDocumentPart()),
				ThemeColors.of(pkg), 3657600, 1828800, null);
		java.awt.Shape cut = pictures(turned.getDrawing()).get(0).getClip();
		assertNotNull(cut);
		assertTrue("turned with the box", !(cut instanceof Rectangle2D));
	}

	/** A wholly transparent effect draws nothing (we17-06; finding 5); a colour that doesn't resolve is reported (finding 8) */
	@Test
	public void effectsThatDrawNothing() throws Exception {
		String clear = "<w14:glow w14:rad=\"101600\"><w14:srgbClr w14:val=\"ED7D31\"><w14:alpha w14:val=\"100000\"/></w14:srgbClr></w14:glow>";
		WordArtRenderer.Drawn d = draw(para(run(clear, "WordArt")), wordForm("textNoShape"));
		assertTrue(pictures(d.getDrawing()).isEmpty());
		assertTrue(d.getProblems().toString(), d.getProblems().isEmpty());
		String placeholder = "<w14:glow w14:rad=\"101600\"><w14:schemeClr w14:val=\"phClr\"/></w14:glow>";
		WordArtRenderer.Drawn ph = draw(para(run(placeholder, "WordArt")), wordForm("textNoShape"));
		assertTrue(pictures(ph.getDrawing()).isEmpty());
		assertTrue(ph.getProblems().toString(), ph.getProblems().contains(WordArtText.EFFECT));
	}

	/** Follow Path takes a picture a glyph an effect: past MAX_EFFECT_PICTURES none is drawn, and it is reported (finding 3) */
	@Test
	public void manyPicturesAreNotDrawn() throws Exception {
		StringBuilder text = new StringBuilder();
		for (int i = 0; i < 60; i++) text.append("WordArt ");
		long t0 = System.nanoTime();
		WordArtRenderer.Drawn d = draw(para(run(glow(5) + shadow(4, 3, 45, 100000, 100000, "tl"), text.toString().trim())), wordForm("textArchUp"));
		assertTrue(pictures(d.getDrawing()).isEmpty());
		assertTrue(d.getProblems().toString(), d.getProblems().contains(Effects.OVER_LIMIT));
		assertTrue("left out before drawing", (System.nanoTime() - t0) / 1e9 < 30);
		WordArtRenderer.Drawn few = draw(para(run(glow(5) + shadow(4, 3, 45, 100000, 100000, "tl"), "WordArt")), wordForm("textArchUp"));
		assertEquals(14, pictures(few.getDrawing()).size());
	}

	private static String gradFill(String... stops) {
		StringBuilder gs = new StringBuilder();
		for (int i = 0; i < stops.length; i += 2) {
			gs.append("<w14:gs w14:pos=\"" + stops[i] + "\"><w14:srgbClr w14:val=\"" + stops[i + 1] + "\"/></w14:gs>");
		}
		return "<w14:textFill><w14:gradFill><w14:gsLst>" + gs + "</w14:gsLst><w14:lin w14:ang=\"0\" w14:scaled=\"0\"/>"
				+ "</w14:gradFill></w14:textFill>";
	}

	/**
	 * A gradient of two stops at 0 and 100% blended by Word's curve, any other evenly between its stops (CR-083 D24;
	 * phase 5 findings section 1.2: wb50-13, red to blue (238, 0, 106) a quarter of the way; wb50-15, three stops)
	 */
	@Test
	public void aTwoStopGradientTakesWordsCurve() throws Exception {
		WordArtRenderer.Drawn d = draw(para(run(gradFill("0", "FF0000", "100000", "0000FF"), "WordArt")), fixed(null));
		Drawing.Gradient g = areas(d, "wordart.fill").get(0).getGradient();
		assertEquals(17, g.getColours().length);
		assertEquals(0.25f, g.getStops()[4], 1e-6);
		assertEquals(new java.awt.Color(0xEE, 0, 0x69), g.getColours()[4]);
		assertEquals(new java.awt.Color(0xBA, 0, 0xBA), g.getColours()[8]);
		WordArtRenderer.Drawn three = draw(para(run(gradFill("0", "FF0000", "50000", "FFFF00", "100000", "0000FF"), "WordArt")), fixed(null));
		assertEquals(3, areas(three, "wordart.fill").get(0).getGradient().getColours().length);
		WordArtRenderer.Drawn inside = draw(para(run(gradFill("21000", "FF0000", "88000", "0000FF"), "WordArt")), fixed(null));
		assertEquals(2, areas(inside, "wordart.fill").get(0).getGradient().getColours().length);
	}

	/**
	 * On flat WordArt a gradient spans the run's frame, its advance across and its font's ascent to descent down, not
	 * its ink (wa30-14 and wg10-06 to -08: Word's axis at 90 degrees 43.9 pt at 36 pt, the ink's 23.9)
	 */
	@Test
	public void aGradientSpansTheRunsFrame() throws Exception {
		String g90 = gradFill("0", "FF0000", "100000", "0000FF").replace("w14:ang=\"0\"", "w14:ang=\"5400000\"");
		WordArtRenderer.Drawn d = draw(para(run(g90, "wave")), fixed(null));
		Drawing.Area a = areas(d, "wordart.fill").get(0);
		java.awt.geom.Rectangle2D frame = a.getGradientFrame();
		assertNotNull(frame);
		java.awt.geom.Rectangle2D ink = a.getShape().getBounds2D();
		// "wave" has no ascender or descender: its ink well inside the frame, which is about 1.2 em at 36 pt
		assertTrue(frame.getHeight() > 40 && frame.getHeight() < 48);
		assertTrue(ink.getHeight() < 25);
		assertTrue(frame.getMinY() < ink.getMinY() - 5 && frame.getMaxY() > ink.getMaxY() + 5);
		// a warp's frame isn't measured: the ink, as before
		WordArtRenderer.Drawn w = draw(para(run(g90, "wave")), fixed("textInflate"));
		assertNull(areas(w, "wordart.fill").get(0).getGradientFrame());
	}
}
