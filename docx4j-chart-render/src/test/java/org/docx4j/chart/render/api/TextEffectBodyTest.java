/*
 *  Copyright 2026, Plutext Pty Ltd.
 *
 *  This file is part of docx4j.
 */
package org.docx4j.chart.render.api;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import java.awt.Shape;
import java.awt.geom.Rectangle2D;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.docx4j.XmlUtils;
import org.docx4j.model.color.ThemeColors;
import org.docx4j.model.shapes.WordArtText;
import org.docx4j.openpackaging.packages.WordprocessingMLPackage;
import org.docx4j.wml.RPr;
import org.junit.Test;

/**
 * A body run's part on a line painted for the FO renderer fork's text-effect painter (CR-083 phase 5, step 2):
 * {@link WordArtRenderer#drawBody} and {@link TextEffectPainter}'s handling of the fork's map. Word's rules are phase 5
 * findings section 6's (cr083-phase5).
 */
public class TextEffectBodyTest {

	private static final String NS = "xmlns:w=\"http://schemas.openxmlformats.org/wordprocessingml/2006/main\" "
			+ "xmlns:w14=\"http://schemas.microsoft.com/office/word/2010/wordml\"";

	private static final String GRADIENT = "<w14:textFill><w14:gradFill><w14:gsLst>"
			+ "<w14:gs w14:pos=\"0\"><w14:srgbClr w14:val=\"FF0000\"/></w14:gs>"
			+ "<w14:gs w14:pos=\"100000\"><w14:srgbClr w14:val=\"0000FF\"/></w14:gs></w14:gsLst>"
			+ "<w14:lin w14:ang=\"0\" w14:scaled=\"0\"/></w14:gradFill></w14:textFill>";

	private static final String GLOW = "<w14:glow w14:rad=\"63500\"><w14:srgbClr w14:val=\"ED7D31\"><w14:alpha w14:val=\"60000\"/>"
			+ "</w14:srgbClr></w14:glow>";

	private static final String INNER = "<w14:shadow w14:blurRad=\"63500\" w14:dist=\"50800\" w14:dir=\"13500000\" w14:sx=\"0\" "
			+ "w14:sy=\"0\" w14:kx=\"0\" w14:ky=\"0\" w14:algn=\"none\"><w14:srgbClr w14:val=\"000000\"><w14:alpha w14:val=\"50000\"/>"
			+ "</w14:srgbClr></w14:shadow>";

	private static final String OUTLINE = "<w14:textOutline w14:w=\"12700\" w14:cap=\"flat\" w14:cmpd=\"sng\" w14:algn=\"ctr\">"
			+ "<w14:solidFill><w14:srgbClr w14:val=\"C00000\"/></w14:solidFill><w14:prstDash w14:val=\"solid\"/><w14:round/>"
			+ "</w14:textOutline>";

	private static WordprocessingMLPackage pkg;

	private static WordArtText.Run run(String fx) throws Exception {
		if (pkg == null) pkg = WordprocessingMLPackage.createPackage();
		RPr rPr = (RPr) XmlUtils.unmarshalString("<w:rPr " + NS + "><w:rFonts w:ascii=\"Aptos\" w:hAnsi=\"Aptos\"/>"
				+ "<w:color w:val=\"000000\"/><w:sz w:val=\"48\"/>" + fx + "</w:rPr>");
		assertTrue(fx, fx.isEmpty() || WordArtText.hasBodyEffect(rPr));
		return WordArtText.bodyRun(pkg, rPr);
	}

	private static ThemeColors theme() {
		return ThemeColors.of(pkg);
	}

	/** Two glyph boxes standing on the baseline: points from the group's start, y down */
	private static Shape glyphs() {
		java.awt.geom.Path2D.Double p = new java.awt.geom.Path2D.Double();
		p.append(new Rectangle2D.Double(1, -17, 12, 17), false);
		p.append(new Rectangle2D.Double(15, -17, 12, 17), false);
		return p;
	}

	private static final Rectangle2D FRAME = new Rectangle2D.Double(0, -23, 30, 29.3);

	private static List<Drawing.Area> areas(Drawing d, String role) {
		List<Drawing.Area> out = new ArrayList<Drawing.Area>();
		for (Object o : d.getItems()) if (o instanceof Drawing.Area && role.equals(((Drawing.Area) o).getRole())) out.add((Drawing.Area) o);
		return out;
	}

	private static int pictures(Drawing d) {
		int n = 0;
		for (Object o : d.getItems()) if (o instanceof Drawing.Picture) n++;
		return n;
	}

	/** A gradient: the glyphs as paths over the gradient frame, by Word's curve; the text then drawn invisibly */
	@Test
	public void aGradientRunIsPaintedAsPathsOverItsFrame() throws Exception {
		Rectangle2D gradientFrame = new Rectangle2D.Double(0, -23, 35, 29.3);
		WordArtRenderer.Body b = WordArtRenderer.drawBody(run(GRADIENT), glyphs(), 24, FRAME, gradientFrame,
				Collections.<Shape>emptyList(), false, theme(), false);
		assertTrue(b.isGlyphsDrawn());
		assertFalse(b.isDecorationsDrawn());
		Drawing.Area a = areas(b.getDrawing(), "wordart.fill").get(0);
		assertEquals(gradientFrame, a.getGradientFrame());
		assertEquals(17, a.getGradient().getColours().length);
		assertEquals(0, a.getGradient().getAngle(), 1e-9);
		assertNotNull(b.toSvg());
		// a right-to-left run's gradient starts at its right (wb82-28)
		WordArtRenderer.Body rtl = WordArtRenderer.drawBody(run(GRADIENT), glyphs(), 24, FRAME, gradientFrame,
				Collections.<Shape>emptyList(), true, theme(), false);
		assertEquals(180, areas(rtl.getDrawing(), "wordart.fill").get(0).getGradient().getAngle(), 1e-9);
	}

	/** A glow on an opaque run: its picture under real text, the glyphs left to the renderer */
	@Test
	public void aGlowIsAPictureUnderRealText() throws Exception {
		WordArtRenderer.Body b = WordArtRenderer.drawBody(run(GLOW), glyphs(), 24, FRAME, FRAME, null, false, theme(), false);
		assertFalse(b.isGlyphsDrawn());
		assertEquals(1, pictures(b.getDrawing()));
		assertTrue(areas(b.getDrawing(), "wordart.fill").isEmpty());
		assertTrue(b.getBounds().contains(new Rectangle2D.Double(1, -17, 26, 17)));
		// nothing over the text
		assertNull(WordArtRenderer.drawBody(run(GLOW), glyphs(), 24, FRAME, FRAME, null, false, theme(), true).toSvg());
	}

	/** No fill and no outline: nothing drawn, the text invisible (wb50-04) */
	@Test
	public void noFillAndNoOutlineDrawsNothing() throws Exception {
		WordArtRenderer.Body b = WordArtRenderer.drawBody(run("<w14:textFill><w14:noFill/></w14:textFill>"), glyphs(), 24, FRAME,
				FRAME, null, false, theme(), false);
		assertTrue(b.isGlyphsDrawn());
		assertNull(b.toSvg());
	}

	/** An inner shadow is drawn over the text, in the call after it */
	@Test
	public void anInnerShadowIsOverTheText() throws Exception {
		WordArtRenderer.Body under = WordArtRenderer.drawBody(run(INNER), glyphs(), 24, FRAME, FRAME, null, false, theme(), false);
		assertEquals(0, pictures(under.getDrawing()));
		WordArtRenderer.Body over = WordArtRenderer.drawBody(run(INNER), glyphs(), 24, FRAME, FRAME, null, false, theme(), true);
		assertEquals(1, pictures(over.getDrawing()));
		assertNotNull(over.toSvg());
	}

	/** An outlined run's underline painted with its fill and outline (wb80-17); a simulated bold's thickening filled
	 *  apart from the glyphs, not outlined (corpus 4102: a union of the two split every curve) */
	@Test
	public void decorationsAndABoldThickeningArePaintedWithTheGlyphs() throws Exception {
		List<Shape> lines = new ArrayList<Shape>();
		lines.add(new Rectangle2D.Double(0, 2, 30, 1.2));
		Shape thick = new java.awt.BasicStroke(0.7f).createStrokedShape(glyphs());
		WordArtRenderer.Body b = WordArtRenderer.drawBody(run(OUTLINE), glyphs(), thick, 24, FRAME, FRAME, lines, false, theme(), false);
		assertTrue(b.isGlyphsDrawn());
		assertTrue(b.isDecorationsDrawn());
		// the thickening, the glyphs and the underline filled
		assertEquals(3, areas(b.getDrawing(), "wordart.fill").size());
	}

	/** The painter: nothing for an id it doesn't know or a face it can't read; over only for an inner shadow */
	@Test
	public void thePainterAnswersOnlyWhatItDraws() throws Exception {
		Map<String, TextEffectPainter.Entry> table = new HashMap<String, TextEffectPainter.Entry>();
		table.put("fx1", new TextEffectPainter.Entry(run(GLOW), theme()));
		TextEffectPainter p = new TextEffectPainter(table::get);
		Map<String, Object> m = new HashMap<String, Object>();
		m.put(TextEffectPainter.PHASE, TextEffectPainter.PHASE_UNDER);
		m.put(TextEffectPainter.GLYPHS, new int[] { 3, 4 });
		m.put(TextEffectPainter.X, new double[] { 0, 10 });
		m.put(TextEffectPainter.Y, new double[] { 0, 0 });
		m.put(TextEffectPainter.FONT_SIZE, 24.0);
		assertNull(p.apply("fx9", m)); // not in the table
		assertNull(p.apply("fx1", m)); // no font file: not painted, reported
		m.put(TextEffectPainter.PHASE, TextEffectPainter.PHASE_OVER);
		assertNull(p.apply("fx1", m)); // a glow has nothing over the text
	}

	/** The underline and strike over each of the group's text areas, where FOP draws them */
	@Test
	public void decorationsAreTheGroupsSpans() {
		Map<String, Object> m = new HashMap<String, Object>();
		m.put(TextEffectPainter.TEXT_SPANS, new double[][] { { 0, 40 }, { 60, 30 } });
		m.put(TextEffectPainter.UNDERLINE, new double[] { 2.5, 1.0 });
		List<Shape> d = TextEffectPainter.decorations(m);
		assertEquals(2, d.size());
		assertEquals(new Rectangle2D.Double(0, 2, 40, 1), d.get(0).getBounds2D());
		assertEquals(new Rectangle2D.Double(60, 2, 30, 1), d.get(1).getBounds2D());
	}
}
