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
package org.docx4j.chart.render.api;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import java.awt.geom.Rectangle2D;
import java.util.ArrayList;
import java.util.List;

import org.docx4j.TraversalUtil;
import org.docx4j.XmlUtils;
import org.docx4j.model.chart.ChartFonts;
import org.docx4j.model.color.ThemeColors;
import org.docx4j.model.shapes.VmlGeometry;
import org.docx4j.model.shapes.VmlShapes;
import org.docx4j.model.shapes.VmlWordArt;
import org.docx4j.model.shapes.WordArtText;
import org.docx4j.openpackaging.packages.WordprocessingMLPackage;
import org.docx4j.vml.CTShape;
import org.junit.Test;

/**
 * VML WordArt drawn (CR-083 phase 4): the text stretched onto its shapetype's text paths, by its ink with
 * {@code trim} (phase 0's wv20-42: Word's ink is the box), else by an em box; its paint; its turn; what draws nothing.
 */
public class VmlWordArtRendererTest {

	private static final String NS = "xmlns:w=\"http://schemas.openxmlformats.org/wordprocessingml/2006/main\" "
			+ "xmlns:v=\"urn:schemas-microsoft-com:vml\" xmlns:o=\"urn:schemas-microsoft-com:office:office\" "
			+ "xmlns:w10=\"urn:schemas-microsoft-com:office:word\"";

	private static final String FORMULAS = "<v:formulas><v:f eqn=\"sum #0 0 10800\"/><v:f eqn=\"prod #0 2 1\"/>"
			+ "<v:f eqn=\"sum 21600 0 @1\"/><v:f eqn=\"sum 0 0 @2\"/><v:f eqn=\"sum 21600 0 @3\"/><v:f eqn=\"if @0 @3 0\"/>"
			+ "<v:f eqn=\"if @0 21600 @1\"/><v:f eqn=\"if @0 0 @2\"/><v:f eqn=\"if @0 @4 21600\"/><v:f eqn=\"mid @5 @6\"/>"
			+ "<v:f eqn=\"mid @8 @5\"/><v:f eqn=\"mid @7 @8\"/><v:f eqn=\"mid @6 @7\"/><v:f eqn=\"sum @6 0 @5\"/></v:formulas>";

	/** Shapetype 136 as Word writes it, with another path if given */
	private static String type(String path) {
		return "<v:shapetype id=\"_x0000_t136\" coordsize=\"21600,21600\" o:spt=\"136\" adj=\"10800\" path=\"" + path + "\">"
				+ FORMULAS + "<v:path textpathok=\"t\"/><v:textpath on=\"t\" fitshape=\"t\"/></v:shapetype>";
	}

	private static final String P136 = "m@7,l@8,m@5,21600l@6,21600e";

	private static String shape(String attrs, String textpath, String children) {
		return "<v:shape id=\"WordArt1\" type=\"#_x0000_t136\" style=\"position:absolute;margin-left:144pt;margin-top:252pt;"
				+ "width:288pt;height:144pt;z-index:1;mso-position-horizontal-relative:page;mso-position-vertical-relative:page\" "
				+ attrs + ">" + children + "<v:textpath style=\"font-family:&quot;Arial&quot;;font-size:36pt;v-text-kern:t\" "
				+ textpath + " string=\"WATERMARK\"/></v:shape>";
	}

	private static final class Case {
		WordprocessingMLPackage pkg;
		VmlShapes.Resolved resolved;
		WordArtText art;
	}

	private static Case read(String type, String shape) throws Exception {
		Case c = new Case();
		c.pkg = WordprocessingMLPackage.createPackage();
		Object hdr = XmlUtils.unmarshalString("<w:hdr " + NS + "><w:p><w:r><w:pict>" + type + shape + "</w:pict></w:r></w:p></w:hdr>");
		final List<CTShape> found = new ArrayList<CTShape>();
		new TraversalUtil(hdr, new TraversalUtil.CallbackImpl() {
			@Override
			public List<Object> apply(Object o) {
				Object u = XmlUtils.unwrap(o);
				if (u instanceof CTShape && !found.contains(u)) found.add((CTShape) u);
				return null;
			}
		});
		c.resolved = VmlShapes.resolve(found.get(0), hdr);
		c.art = VmlWordArt.of(c.resolved, c.pkg);
		return c;
	}

	private static WordArtRenderer.Drawn draw(Case c, double rotation, boolean withText) {
		return WordArtRenderer.drawVml(c.art, VmlGeometry.outlines(c.resolved.definition(), 288, 144),
				ChartFonts.of(c.pkg.getMainDocumentPart()), ThemeColors.of(c.pkg), 288, 144, rotation, false, false, withText);
	}

	private static List<Drawing.Area> fills(WordArtRenderer.Drawn d) {
		List<Drawing.Area> out = new ArrayList<Drawing.Area>();
		for (Object o : d.getDrawing().getItems()) {
			if (o instanceof Drawing.Area && "wordart.fill".equals(((Drawing.Area) o).getRole())) out.add((Drawing.Area) o);
		}
		return out;
	}

	private static List<org.w3c.dom.Element> texts(WordArtRenderer.Drawn d) {
		List<org.w3c.dom.Element> out = new ArrayList<org.w3c.dom.Element>();
		org.w3c.dom.NodeList n = d.toSvg().getElementsByTagNameNS("http://www.w3.org/2000/svg", "text");
		for (int i = 0; i < n.getLength(); i++) out.add((org.w3c.dom.Element) n.item(i));
		return out;
	}

	private static Rectangle2D ink(WordArtRenderer.Drawn d) {
		Rectangle2D r = null;
		for (Drawing.Area a : fills(d)) r = r == null ? a.getShape().getBounds2D() : r.createUnion(a.getShape().getBounds2D());
		return r;
	}

	@Test
	public void withTrimTheInkIsTheBox() throws Exception {
		// phase 0's wv20-42: Word's ink runs from the box's left to its right and from its top to its foot
		Case c = read(type(P136), shape("fillcolor=\"black\" stroked=\"f\"", "trim=\"t\" fitpath=\"t\"", ""));
		assertNotNull(c.art);
		WordArtRenderer.Drawn d = draw(c, 0, true);
		Rectangle2D ink = ink(d);
		assertNotNull(ink);
		assertEquals(0, ink.getMinX(), 0.5);
		assertEquals(288, ink.getMaxX(), 0.5);
		assertEquals(0, ink.getMinY(), 0.5);
		assertEquals(144, ink.getMaxY(), 0.5);
		List<org.w3c.dom.Element> texts = texts(d);
		assertFalse(texts.isEmpty()); // the text again, invisibly (D2)
		assertEquals("none", texts.get(0).getAttribute("fill"));
	}

	@Test
	public void withoutTrimAnEmBoxIsMapped() throws Exception {
		// phase 0's wv30-02: capitals take the box's cap height of an em, from 0.061 em below its top
		Case c = read(type(P136), shape("fillcolor=\"black\" stroked=\"f\"", "", ""));
		Rectangle2D ink = ink(draw(c, 0, true));
		// across, the advance (its side bearings kept: the ink within the box); down, an em box
		assertTrue("within the box across: " + ink, ink.getMinX() > -0.5 && ink.getMaxX() < 288.5);
		assertTrue("the capitals' tops below the box's top: " + ink, ink.getMinY() > 2);
		assertTrue("the baseline above the box's foot: " + ink, ink.getMaxY() < 120);
	}

	@Test
	public void theShapetypesPathWarpsTheText() throws Exception {
		// wv20-42's slanted top line: the text's top falls from the box's top on the left to its middle on the right
		Case c = read(type("m0,0l21600,10800m0,21600l21600,21600e"), shape("fillcolor=\"black\" stroked=\"f\"", "trim=\"t\"", ""));
		WordArtRenderer.Drawn d = draw(c, 0, true);
		List<Drawing.Area> glyphs = fills(d);
		assertEquals(1, glyphs.size()); // a run's glyphs painted at once
		Rectangle2D ink = ink(d);
		assertEquals(0, ink.getMinY(), 0.5);
		// the glyph paths' right edge, at the box's right, starts down near the middle
		java.awt.geom.Area right = new java.awt.geom.Area(glyphs.get(0).getShape());
		right.intersect(new java.awt.geom.Area(new Rectangle2D.Double(280, 0, 8, 144)));
		assertTrue(right.getBounds2D().getMinY() > 60);
	}

	@Test
	public void aTurnedWatermarkHasNoTextInAHeader() throws Exception {
		Case c = read(type(P136), shape("fillcolor=\"silver\" stroked=\"f\"", "", "<v:fill opacity=\".5\"/>"));
		WordArtRenderer.Drawn d = draw(c, 315, false);
		assertTrue(texts(d).isEmpty()); // D17: none in a header or footer
		Rectangle2D ink = ink(d);
		assertTrue("turned past the box: " + ink, ink.getHeight() > 144);
		Drawing.Area a = fills(d).get(0);
		assertEquals(0xC0, a.getFill().getRed());
		assertEquals(128, a.getFill().getAlpha(), 1);
	}

	@Test
	public void legacyWordArtIsOutlinedUnlessItSaysNot() throws Exception {
		Case c = read(type(P136), shape("fillcolor=\"blue\" strokecolor=\"blue\"", "trim=\"t\"", "<v:stroke joinstyle=\"miter\"/>"));
		WordArtRenderer.Drawn d = draw(c, 0, true);
		assertTrue(d.getDrawing().getItems().size() > fills(d).size());
		assertNotNull(c.art.getParagraphs().get(0).getRuns().get(0).getOutline());
		// a shadow with no on is off (wv40: Word draws none)
		Case s = read(type(P136), shape("fillcolor=\"blue\"", "", "<v:shadow color=\"#868686\"/>"));
		assertTrue(VmlWordArt.shadows(s.resolved, 288, 144).isEmpty());
		// on: a copy of the glyphs under them, moved by its offset (cr083-phase4's wv45), not an effect picture
		Case on = read(type(P136), shape("fillcolor=\"blue\" stroked=\"f\"", "trim=\"t\"",
				"<v:shadow on=\"t\" color=\"#868686\" offset=\"3pt,4pt\"/>"));
		assertNull(on.art.getParagraphs().get(0).getRuns().get(0).getEffects());
		WordArtRenderer.Drawn d2 = draw(on, 0, true);
		Rectangle2D shadow = null;
		for (Object o : d2.getDrawing().getItems()) {
			if (o instanceof Drawing.Area && "wordart.shadow".equals(((Drawing.Area) o).getRole())) {
				Rectangle2D b = ((Drawing.Area) o).getShape().getBounds2D();
				shadow = shadow == null ? b : shadow.createUnion(b);
			}
		}
		assertNotNull(shadow);
		assertEquals(ink(d2).getMinX() + 3, shadow.getMinX(), 0.01);
		assertEquals(ink(d2).getMinY() + 4, shadow.getMinY(), 0.01);
		// a percentage of the shape's size; a double shadow's second first
		Case pct = read(type(P136), shape("fillcolor=\"blue\"", "", "<v:shadow on=\"t\" offset=\"10%,10%\"/>"));
		assertEquals(28.8, VmlWordArt.shadows(pct.resolved, 288, 144).get(0)[0], 1e-9);
		assertEquals(14.4, VmlWordArt.shadows(pct.resolved, 288, 144).get(0)[1], 1e-9);
		Case dbl = read(type(P136), shape("fillcolor=\"blue\"", "",
				"<v:shadow on=\"t\" type=\"double\" color2=\"red\" offset=\"3pt,3pt\" offset2=\"6pt,6pt\"/>"));
		assertEquals(2, VmlWordArt.shadows(dbl.resolved, 288, 144).size());
		assertEquals(6, VmlWordArt.shadows(dbl.resolved, 288, 144).get(0)[0], 1e-9);
	}

	/**
	 * One text path (Word's Arch Up, o:spt 144, an arc over the top): the text along it, sized to its whole length, the
	 * line 0.22 em above its baseline on the path (wv40: Word's ink 103.7 to 461.8 across for a box of 144 to 432, and
	 * 40 pt above it), whatever fitshape says
	 */
	@Test
	public void onePathTheTextGoesAlongIt() throws Exception {
		String arch = "<v:shapetype id=\"_x0000_t136\" coordsize=\"21600,21600\" o:spt=\"144\">"
				+ "<v:path textpathok=\"t\"/><v:textpath on=\"t\" fitpath=\"t\"/></v:shapetype>";
		Case c = read(arch.replace("o:spt=\"144\">", "o:spt=\"144\" path=\"al10800,10800,10800,10800,11796480,-11796480e\">"),
				shape("", "fitshape=\"t\" trim=\"t\"", "").replace("string=\"WATERMARK\"", "string=\"WordArt\""));
		Rectangle2D ink = ink(draw(c, 0, true));
		assertTrue("wider than the box: " + ink, ink.getMinX() < -20 && ink.getMaxX() > 308);
		assertTrue("above the box: " + ink, ink.getMinY() < -20);
		assertTrue("down to the arc's ends: " + ink, ink.getMaxY() > 60 && ink.getMaxY() < 90);
	}

	/** Turned and flipped: the turn first, then the mirror (cr083-phase4's wv46-12) */
	@Test
	public void aTurnIsMirroredAfterItIsTurned() {
		java.awt.geom.Point2D.Double p = new java.awt.geom.Point2D.Double();
		WordArtRenderer.vmlTurn(30, true, false, 288, 144).transform(new java.awt.geom.Point2D.Double(288, 72), p);
		// the box's right middle turned 30 clockwise about the centre, then mirrored across it
		assertEquals(144 - 144 * Math.cos(Math.toRadians(30)), p.x, 1e-9);
		assertEquals(72 + 144 * Math.sin(Math.toRadians(30)), p.y, 1e-9);
	}

	@Test
	public void noPathDrawsNothing() throws Exception {
		// phase 0's wv20-01 to -40: a shapetype with only its o:spt
		Case c = read("<v:shapetype id=\"_x0000_t136\" coordsize=\"21600,21600\" o:spt=\"136\"><v:path textpathok=\"t\"/>"
				+ "<v:textpath on=\"t\" fitshape=\"t\"/></v:shapetype>", shape("fillcolor=\"black\"", "", ""));
		assertNotNull(c.art);
		WordArtRenderer.Drawn d = draw(c, 0, true);
		assertTrue(d.isEmpty());
		assertTrue(d.getProblems().isEmpty());
	}

	/** qb anywhere in the path: Word draws nothing, wherever its control point (cr083-phase4b's wv49-01 to -03) */
	@Test
	public void aQuadraticBezierDrawsNothing() throws Exception {
		for (String path : new String[] { "m0,5400qb10800,0,21600,5400m0,21600l21600,21600e",
				"m0,0l21600,0m0,21600qb10800,16200,21600,21600e", "m0,5400qb10800,-5400,21600,5400m0,21600l21600,21600e" }) {
			Case c = read(type(path), shape("fillcolor=\"black\"", "trim=\"t\"", ""));
			WordArtRenderer.Drawn d = draw(c, 0, true);
			assertTrue(path, d.isEmpty());
			assertTrue(path, d.getProblems().isEmpty());
		}
	}

	/**
	 * v-same-letter-heights: every glyph the box's height, an x-height letter too; across, the advance from the pen
	 * (cr083-phase4b's wv49-13 "Gyp ax", cr083-phase4's wv44-26)
	 */
	@Test
	public void sameLetterHeightsMakesEveryGlyphTheBoxsHeight() throws Exception {
		String same = shape("fillcolor=\"black\" stroked=\"f\"", "trim=\"t\" fitpath=\"t\"", "")
				.replace("v-text-kern:t", "v-text-kern:t;v-same-letter-heights:t").replace("WATERMARK", "Gyp ax");
		Rectangle2D x = lastGlyph(draw(read(type(P136), same), 0, true));
		assertEquals("the x reaches the box's top: " + x, 0, x.getMinY(), 0.5);
		assertEquals("and its foot: " + x, 144, x.getMaxY(), 0.5);
		Rectangle2D ink = ink(draw(read(type(P136), same), 0, true));
		assertTrue("the G's side bearing kept: " + ink, ink.getMinX() > 1);
		// without it, the x is short of both
		Rectangle2D plain = lastGlyph(draw(read(type(P136), same.replace(";v-same-letter-heights:t", "")), 0, true));
		assertTrue(plain.toString(), plain.getMinY() > 20 && plain.getMaxY() < 124);
	}

	/** The ink in the box's last 14 pt: the x of "Gyp ax" */
	private static Rectangle2D lastGlyph(WordArtRenderer.Drawn d) {
		java.awt.geom.Area a = new java.awt.geom.Area();
		for (Drawing.Area f : fills(d)) a.add(new java.awt.geom.Area(f.getShape()));
		a.intersect(new java.awt.geom.Area(new Rectangle2D.Double(274, -10, 14, 164)));
		return a.getBounds2D();
	}

	/**
	 * A turned shape aligned down its reference: its ink, not its box, aligned (centred: wv30-01; at the bottom:
	 * cr083-phase4b's wv50-01); placed at an offset, turned about the box's centre alone
	 */
	@Test
	public void aTurnedShapesInkIsWhatIsAligned() throws Exception {
		Case c = read(type(P136), shape("fillcolor=\"silver\" stroked=\"f\"", "", ""));
		Watermark g = new Watermark(c);
		Rectangle2D bottom = ink(g.draw("bottom")), centre = ink(g.draw("center")), top = ink(g.draw("top")),
				offset = ink(g.draw(null));
		// a watermark's box, 468 by 117
		assertEquals(117, bottom.getMaxY(), 0.01);
		assertEquals(58.5, centre.getCenterY(), 0.01);
		assertEquals(0, top.getMinY(), 0.01);
		assertEquals("across, the turn about the centre alone", offset.getCenterX(), bottom.getCenterX(), 0.01);
		assertTrue(offset.toString(), Math.abs(offset.getCenterY() - 58.5) > 1);
	}

	/** A turned watermark's drawing, aligned down as given */
	private static final class Watermark {
		final Case c;

		Watermark(Case c) {
			this.c = c;
		}

		WordArtRenderer.Drawn draw(String aligned) {
			return WordArtRenderer.drawVml(c.art, VmlGeometry.outlines(c.resolved.definition(), 468, 117),
					ChartFonts.of(c.pkg.getMainDocumentPart()), ThemeColors.of(c.pkg), 468, 117, 315, false, false, false, aligned);
		}
	}

	/** A gradient fill's stops, each "pos rgb" */
	private static List<String> stops(String fill) throws Exception {
		Case c = read(type(P136), shape("fillcolor=\"red\"", "", "<v:fill type=\"gradient\" color2=\"blue\" " + fill + "/>"));
		List<String> out = new ArrayList<String>();
		Object g = c.art.getParagraphs().get(0).getRuns().get(0).getFill();
		for (org.docx4j.dml.CTGradientStop s : ((org.docx4j.dml.CTGradientFillProperties) g).getGsLst().getGs()) {
			out.add(s.getPos() + " " + s.getSrgbClr().getVal());
		}
		return out;
	}

	/**
	 * A gradient as Word draws it (cr083-phase4's wv45-09 to -15): color2 at the focus, color at the ends, so with no
	 * focus from color2 to color; blended by Word's sigma curve in gamma-2.2 light unless its method is none
	 */
	@Test
	public void aGradientRunsFromColor2AtItsFocusToColorAtItsEnds() throws Exception {
		List<String> plain = stops("");
		assertEquals(17, plain.size());
		assertEquals("0 0000FF", plain.get(0));
		assertEquals("100000 FF0000", plain.get(16));
		// the middle Word's (187, 0, 186), where an even blend gives (128, 0, 128)
		assertEquals("50000 BA00BA", plain.get(8));
		List<String> hundred = stops("focus=\"100%\"");
		assertEquals("0 FF0000", hundred.get(0));
		assertEquals("100000 0000FF", hundred.get(16));
		// 50%: color at both ends, color2 in the middle, each half the whole curve; -50% the colours swapped
		List<String> fifty = stops("focus=\"50%\"");
		assertEquals(33, fifty.size());
		assertEquals("0 FF0000", fifty.get(0));
		assertEquals("50000 0000FF", fifty.get(16));
		assertEquals("100000 FF0000", fifty.get(32));
		assertEquals("25000 BA00BA", fifty.get(8));
		List<String> minus = stops("focus=\"-50%\"");
		assertEquals("0 0000FF", minus.get(0));
		assertEquals("50000 FF0000", minus.get(16));
		assertEquals("100000 0000FF", minus.get(32));
		// a colours list, with method none: its colours unblended, from the far end (wv45-15)
		assertEquals(java.util.Arrays.asList("0 0000FF", "50000 FFFF00", "100000 FF0000"),
				stops("colors=\"0 red;32768f yellow;1 blue\" method=\"none\""));
	}

	/** An emboss is a double shadow: a light copy above and to the left, then its colour below and to the right (wv45-35) */
	@Test
	public void anEmbossIsADoubleShadow() throws Exception {
		Case c = read(type(P136), shape("fillcolor=\"#336699\"", "", "<v:shadow on=\"t\" type=\"emboss\" color=\"#868686\"/>"));
		List<double[]> s = VmlWordArt.shadows(c.resolved, 288, 144);
		assertEquals(2, s.size());
		assertEquals(-2, s.get(0)[0], 1e-9);
		assertEquals(-2, s.get(0)[1], 1e-9);
		assertEquals(0xCBCBCB, (int) s.get(0)[2]);
		assertEquals(2, s.get(1)[0], 1e-9);
		assertEquals(2, s.get(1)[1], 1e-9);
		assertEquals(0x868686, (int) s.get(1)[2]);
		assertFalse(draw(c, 0, true).getProblems().contains(VmlWordArt.SHADOW));
		// a perspective shadow still none, reported
		Case p = read(type(P136), shape("fillcolor=\"#336699\"", "", "<v:shadow on=\"t\" type=\"perspective\"/>"));
		assertTrue(VmlWordArt.shadows(p.resolved, 288, 144).isEmpty());
		assertTrue(p.art.getProblems().contains(VmlWordArt.SHADOW));
	}

	@Test
	public void aFormulaThatCantBeReadDrawsNothingAndIsReported() throws Exception {
		Case c = read(type(P136).replace("sum #0 0 10800", "frobnicate #0 0 10800"), shape("fillcolor=\"black\"", "", ""));
		WordArtRenderer.Drawn d = draw(c, 0, true);
		assertTrue(d.isEmpty());
		assertTrue(d.getProblems().contains(VmlWordArt.BAD_GEOMETRY));
	}

	/** In HTML, both routes: the SVG the PDF has, in its line (CR-083 phase 4) */
	@Test
	public void inHtmlItIsItsDrawing() throws Exception {
		WordprocessingMLPackage pkg = WordprocessingMLPackage.createPackage();
		pkg.getMainDocumentPart().setJaxbElement((org.docx4j.wml.Document) XmlUtils.unmarshalString("<w:document " + NS + "><w:body>"
				+ "<w:p><w:r><w:pict>" + type(P136) + shape("fillcolor=\"black\" stroked=\"f\"", "trim=\"t\"", "")
				+ "</w:pict></w:r></w:p><w:p><w:r><w:t>after</w:t></w:r></w:p></w:body></w:document>"));
		for (int flags : new int[] { org.docx4j.Docx4J.FLAG_NONE, org.docx4j.Docx4J.FLAG_EXPORT_PREFER_XSL }) {
			org.docx4j.convert.out.HTMLSettings settings = org.docx4j.Docx4J.createHTMLSettings();
			settings.setOpcPackage(pkg);
			java.io.ByteArrayOutputStream baos = new java.io.ByteArrayOutputStream();
			org.docx4j.Docx4J.toHTML(settings, baos, flags);
			String html = baos.toString("UTF-8").replaceFirst("<!DOCTYPE[^>]*>", "");
			org.w3c.dom.Document doc = XmlUtils.getNewDocumentBuilder().parse(new java.io.ByteArrayInputStream(html.getBytes("UTF-8")));
			org.w3c.dom.NodeList svgs = doc.getElementsByTagNameNS("http://www.w3.org/2000/svg", "svg");
			assertEquals("flags " + flags, 1, svgs.getLength());
			org.w3c.dom.Element svg = (org.w3c.dom.Element) svgs.item(0);
			assertTrue("flags " + flags, svg.getElementsByTagNameNS("http://www.w3.org/2000/svg", "path").getLength() > 0);
			assertEquals("flags " + flags, "0 0 288 144", svg.getAttribute("viewBox"));
			assertTrue("flags " + flags, html.contains("after"));
		}
	}

	@Test
	public void theTextPathsStyle() throws Exception {
		Case c = read(type(P136), shape("fillcolor=\"black\"", "", "").replace("font-family:&quot;Arial&quot;;font-size:36pt;v-text-kern:t",
				"font-family:&quot;Arial Black&quot;;font-size:8pt;font-weight:bold;font-style:italic;v-text-align:left")
				.replace("string=\"WATERMARK\"", "string=\"ONE&#xA;TWO\""));
		assertEquals(2, c.art.getParagraphs().size());
		WordArtText.Run r = c.art.getParagraphs().get(0).getRuns().get(0);
		assertEquals("Arial Black", r.getFace());
		assertEquals(8, r.getSize(), 1e-9);
		assertTrue(r.isBold());
		assertTrue(r.isItalic());
		assertFalse(r.isKerned());
		assertTrue(r.isFitted());
		assertEquals("left", c.art.getParagraphs().get(0).getAlign());
		// Office's default alignment: center
		Case d = read(type(P136), shape("fillcolor=\"black\"", "", ""));
		assertEquals("center", d.art.getParagraphs().get(0).getAlign());
	}
}
