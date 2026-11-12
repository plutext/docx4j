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

import java.awt.Color;
import java.awt.Font;
import java.awt.Shape;
import java.awt.font.FontRenderContext;
import java.awt.font.GlyphVector;
import java.awt.font.TextAttribute;
import java.awt.geom.AffineTransform;
import java.awt.geom.Path2D;
import java.awt.geom.Rectangle2D;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import org.docx4j.com.microsoft.schemas.office.word.x2010.wordprocessingShape.CTWordprocessingShape;
import org.docx4j.dml.CTGradientFillProperties;
import org.docx4j.dml.CTNoFillProperties;
import org.docx4j.dml.CTSolidColorFillProperties;
import org.docx4j.dml.CTTextBodyProperties;
import org.docx4j.dml.STTextWrappingType;
import org.docx4j.model.chart.ChartFonts;
import org.docx4j.model.color.DrawingMLColors;
import org.docx4j.model.color.ThemeColors;
import org.docx4j.model.shapes.ShapeGeometry;
import org.docx4j.model.shapes.ShapeOutline;
import org.docx4j.model.shapes.ShapeOutlines;
import org.docx4j.model.shapes.ShapeText;
import org.docx4j.model.shapes.WordArtText;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

/**
 * A text box drawn as WordArt (CR-083 phases 1 and 2): its text laid out in lines with the font files docx4j's font
 * mapping gives FOP (D4, {@link ChartFonts}), its box sized as Word sizes it, the glyphs warped where its body
 * properties name a warp, and each run painted as Word's PDF paints it (D2).
 * <ul>
 * <li><b>The box</b> (phase 0, findings section 2): {@code wrap="none"} makes it as wide as its widest line, leading
 * spaces counted and trailing ones not, with its insets and half its line; {@code a:spAutoFit} as tall as its lines.
 * Otherwise it keeps its extent, and the lines are broken at its text rectangle's width.</li>
 * <li><b>Lines</b>: Word's line heights for each run's font ({@code WordLineMetrics}), the paragraph's spacing, indents
 * and alignment. Text in a box with an {@code a:prstTxWarp} is kerned, as Word kerns it whatever {@code w:kern} says
 * (phase 1: "WordArt" in Aptos 36 pt, its box 142.6 pt wide, "Wo" 0.90 pt closer); other text as {@code w:kern} says.</li>
 * <li><b>Paint</b> (D2): a run Word keeps as text is SVG text in its fill's colour; a run Word draws as glyph outlines
 * (warped, outlined, a gradient, a fill with transparency) is its glyphs as paths, filled and stroked with its
 * {@code w14:textFill} and {@code w14:textOutline} as DrawingML, with its text again as SVG text with no fill, which
 * FOP writes in PDF text rendering mode 3: found by a search and copied, not seen. A run with no fill and no outline
 * draws nothing, its text kept invisibly.</li>
 * <li><b>Warps</b> (phase 2, {@link TextWarp}): two paths map the block's ink between them (section 20.1.9.19); one path
 * (Follow Path) sets each glyph on it, turned to it, the text scaled; more than two give each line a band.</li>
 * </ul>
 * Shadows, glows and reflections (phase 3) and 3-D (phase 6) aren't drawn yet, and are reported.
 *
 * @since 17.3.7
 */
public final class WordArtRenderer {

	/** Glyphs are outlined at this size and scaled, so that no hinting reaches them, and measured at it */
	private static final float OUTLINE_SIZE = 1000f;

	private static final FontRenderContext FRC = new FontRenderContext(null, true, true);

	private static final String SVG_NS = "http://www.w3.org/2000/svg";

	private WordArtRenderer() {}

	// ------------------------------------------------------------------ the result

	/** A WordArt drawn: its shape and its text, in the frame of the box Word draws (points from its top left, y down) */
	public static final class Drawn {
		private final Drawing drawing;
		private final List<Span> spans;
		private final double width, height;
		private final Rectangle2D.Double bounds;
		private final ShapeOutlines outlines;
		private final List<String> problems;
		private final String text;

		Drawn(Drawing drawing, List<Span> spans, double width, double height, Rectangle2D.Double bounds, ShapeOutlines outlines,
				List<String> problems, String text) {
			this.drawing = drawing;
			this.spans = spans;
			this.width = width;
			this.height = height;
			this.bounds = bounds;
			this.outlines = outlines;
			this.problems = Collections.unmodifiableList(problems);
			this.text = text;
		}

		/** The box's width as Word draws it: the extent's, or its text's where it is {@code wrap="none"} */
		public double getWidth() {
			return width;
		}

		/** The box's height as Word draws it: the extent's, or its lines' where it is {@code a:spAutoFit} */
		public double getHeight() {
			return height;
		}

		/** All it paints, the box included */
		public Rectangle2D.Double getBounds() {
			return bounds;
		}

		/** The shape's geometry at the box drawn */
		public ShapeOutlines getOutlines() {
			return outlines;
		}

		/** The gaps (stable codes, D9): render.wordart.*, and the shape's own render.shape.* */
		public List<String> getProblems() {
			return problems;
		}

		/** Its text, the lines joined by a space: for a picture's alternative text */
		public String getText() {
			return text;
		}

		/** The paths painted (the shape, then the glyphs Word draws as outlines) */
		public Drawing getDrawing() {
			return drawing;
		}

		/** Nothing is painted and there is no text */
		public boolean isEmpty() {
			return drawing.getItems().isEmpty() && spans.isEmpty();
		}

		/**
		 * As SVG, a user unit a point, its canvas {@link #getBounds()}: the paths, then the text, visible where Word keeps
		 * it as text, with no fill where Word draws it as outlines. Its families are those docx4j's font mapping gives FOP,
		 * then the document's faces, then the generic family, as a chart's.
		 */
		public Document toSvg() {
			Document doc = SvgWriter.toSvg(drawing, bounds);
			Element root = doc.getDocumentElement();
			for (Span s : spans) root.appendChild(s.element(doc));
			return doc;
		}
	}

	/** A line of a run's text in the SVG: real (its colour) or invisible (no fill) */
	static final class Span {
		final StringBuilder text = new StringBuilder();
		final List<Double> xs = new ArrayList<Double>();
		double y;
		Font font;
		String face;
		double size;
		/** null: invisible */
		Color fill;
		AffineTransform transform;

		Element element(Document doc) {
			Element t = doc.createElementNS(SVG_NS, "text");
			StringBuilder x = new StringBuilder();
			for (Double d : xs) x.append(x.length() == 0 ? "" : " ").append(num(d));
			t.setAttributeNS(null, "x", x.toString());
			t.setAttributeNS(null, "y", num(y));
			t.setAttributeNS(null, "font-family", families(font, face));
			t.setAttributeNS(null, "font-size", num(size));
			if (font.isBold()) t.setAttributeNS(null, "font-weight", "bold");
			if (font.isItalic()) t.setAttributeNS(null, "font-style", "italic");
			if (fill == null) {
				t.setAttributeNS(null, "fill", "none");
			} else {
				t.setAttributeNS(null, "fill", String.format(Locale.ROOT, "#%02x%02x%02x", fill.getRed(), fill.getGreen(), fill.getBlue()));
				if (fill.getAlpha() < 255) t.setAttributeNS(null, "fill-opacity", num(fill.getAlpha() / 255.0));
			}
			t.setAttributeNS(null, "stroke", "none");
			t.setAttributeNS("http://www.w3.org/XML/1998/namespace", "xml:space", "preserve");
			if (transform != null && !transform.isIdentity()) {
				double[] m = new double[6];
				transform.getMatrix(m);
				t.setAttributeNS(null, "transform", "matrix(" + num(m[0]) + " " + num(m[1]) + " " + num(m[2]) + " " + num(m[3]) + " "
						+ num(m[4]) + " " + num(m[5]) + ")");
			}
			t.setTextContent(text.toString());
			return t;
		}
	}

	/**
	 * The family the font's file is (the one docx4j's mapping gives FOP: Akasia), the face the document names (Aptos)
	 * and the generic family of its class: FOP draws the first it has, a browser the first it has
	 */
	static String families(Font font, String face) {
		StringBuilder b = new StringBuilder("'").append(font.getFamily()).append("'");
		if (face != null && !face.equalsIgnoreCase(font.getFamily())) b.append(", '").append(face).append("'");
		org.docx4j.fonts.FontFallback.FontClass c = org.docx4j.fonts.FontFallback.classOf(face == null ? font.getFamily() : face);
		if (c == org.docx4j.fonts.FontFallback.FontClass.UNKNOWN) c = org.docx4j.fonts.FontFallback.classOf(font.getFamily());
		String generic = c == org.docx4j.fonts.FontFallback.FontClass.SERIF ? "serif" : c == org.docx4j.fonts.FontFallback.FontClass.SANS ? "sans-serif"
				: c == org.docx4j.fonts.FontFallback.FontClass.MONO ? "monospace" : null;
		if (generic != null) b.append(", ").append(generic);
		return b.toString();
	}

	// ------------------------------------------------------------------ layout

	/** A glyph laid out: its run, its place on its line (from the line's start, on its baseline), its outline at origin */
	static final class Glyph {
		WordArtText.Run run;
		Font font;
		int code;
		double x, advance, scale;
		String chars;
		boolean space, lineBreak;
		/** its outline at the run's size, its origin at (0, 0) */
		Shape outline;
	}

	/** A line: its glyphs, its height and baseline, where it starts */
	static final class Line {
		final List<Glyph> glyphs = new ArrayList<Glyph>();
		WordArtText.Paragraph para;
		boolean first, last, forced;
		double top, height, baseline, x0, extraPerSpace;
		double ascent, descent, single;

		/** Its natural width: to its last glyph's advance, trailing spaces left out */
		double width() {
			for (int i = glyphs.size() - 1; i >= 0; i--) {
				Glyph g = glyphs.get(i);
				if (!g.space && !g.lineBreak) return g.x + g.advance;
			}
			return 0;
		}

		int spaces() {
			int n = 0, last = -1;
			for (int i = glyphs.size() - 1; i >= 0; i--) {
				if (!glyphs.get(i).space) {
					last = i;
					break;
				}
			}
			for (int i = 0; i < last; i++) if (glyphs.get(i).space) n++;
			return n;
		}
	}

	/** The lines laid out and their block's size */
	static final class Layout {
		final List<Line> lines = new ArrayList<Line>();
		double width, height;
	}

	/** The outlines of glyphs, by font and glyph code, at {@link #OUTLINE_SIZE} */
	private static final class Outlines {
		final Map<Font, Map<Integer, Shape>> cache = new HashMap<Font, Map<Integer, Shape>>();

		Shape outline(Font big, int code) {
			Map<Integer, Shape> m = cache.computeIfAbsent(big, k -> new HashMap<Integer, Shape>());
			return m.computeIfAbsent(code, c -> big.createGlyphVector(FRC, new int[] { c }).getGlyphOutline(0));
		}
	}

	/** The paragraphs laid out in lines no wider than width (infinite: broken only where they break) */
	static Layout layout(WordArtText art, ChartFonts fonts, double width, double fontScale, double lineScale, boolean kernAll) {
		Layout out = new Layout();
		Outlines cache = new Outlines();
		double y = 0;
		Line previous = null;
		for (WordArtText.Paragraph p : art.getParagraphs()) {
			List<Glyph> glyphs = new ArrayList<Glyph>();
			WordArtText.Run mark = null;
			for (WordArtText.Run r : p.getRuns()) {
				mark = r;
				if (r.isBreak()) {
					Glyph g = new Glyph();
					g.run = r;
					g.lineBreak = true;
					glyphs.add(g);
					continue;
				}
				shape(r, fonts, fontScale, kernAll, cache, glyphs);
			}
			List<Line> lines = breakLines(p, glyphs, width);
			for (int i = 0; i < lines.size(); i++) {
				Line l = lines.get(i);
				l.para = p;
				l.first = i == 0;
				l.last = i == lines.size() - 1;
				metrics(l, mark != null ? mark : previous != null && !previous.glyphs.isEmpty() ? previous.glyphs.get(0).run : null, fontScale);
				space(l, lineScale);
				if (l.first) y += p.getSpaceBefore();
				l.top = y;
				y += l.height;
				if (l.last) y += p.getSpaceAfter();
				double w = p.getIndLeft() + (l.first ? p.getIndFirst() : 0) + l.width() + p.getIndRight();
				out.width = Math.max(out.width, w);
				out.lines.add(l);
				previous = l;
			}
		}
		out.height = y;
		return out;
	}

	/** A run's glyphs, measured at {@link #OUTLINE_SIZE} and scaled to its size */
	private static void shape(WordArtText.Run r, ChartFonts fonts, double fontScale, boolean kernAll, Outlines cache, List<Glyph> out) {
		double size = r.getSize() * fontScale;
		Font base = fonts.font(r.getFace(), r.isBold(), r.isItalic(), 1);
		Font big = base.deriveFont(OUTLINE_SIZE);
		if (kernAll || r.isKerned()) big = big.deriveFont(Collections.singletonMap(TextAttribute.KERNING, TextAttribute.KERNING_ON));
		Font at = big.deriveFont((float) size);
		char[] cs = r.getText().toCharArray();
		if (cs.length == 0) return;
		GlyphVector gv = big.layoutGlyphVector(FRC, cs, 0, cs.length, Font.LAYOUT_LEFT_TO_RIGHT);
		double k = size / OUTLINE_SIZE;
		int n = gv.getNumGlyphs();
		boolean oneToOne = n == cs.length;
		for (int i = 0; i < n; i++) {
			Glyph g = new Glyph();
			g.run = r;
			g.font = at;
			g.code = gv.getGlyphCode(i);
			g.scale = k;
			int ci = oneToOne ? i : gv.getGlyphCharIndex(i);
			int next = oneToOne ? i + 1 : (i + 1 < n ? gv.getGlyphCharIndex(i + 1) : cs.length);
			g.chars = ci >= 0 && next > ci && next <= cs.length ? new String(cs, ci, next - ci) : (ci >= 0 && ci < cs.length ? String.valueOf(cs[ci]) : "");
			g.space = g.chars.trim().isEmpty() && !g.chars.isEmpty() && g.chars.indexOf(' ') < 0;
			g.advance = (gv.getGlyphPosition(i + 1).getX() - gv.getGlyphPosition(i).getX()) * k + r.getSpacing();
			Shape o = cache.outline(big, g.code);
			g.outline = AffineTransform.getScaleInstance(k, k).createTransformedShape(o);
			out.add(g);
		}
	}

	/** The glyphs broken into lines: at a break, and before a word that would pass the width */
	private static List<Line> breakLines(WordArtText.Paragraph p, List<Glyph> glyphs, double width) {
		List<Line> lines = new ArrayList<Line>();
		Line cur = new Line();
		double x = 0;
		int n = glyphs.size();
		int i = 0;
		while (i < n) {
			Glyph g = glyphs.get(i);
			if (g.lineBreak) {
				cur.forced = true;
				lines.add(cur);
				cur = new Line();
				x = 0;
				i++;
				continue;
			}
			int j = i;
			while (j < n && !glyphs.get(j).lineBreak && !glyphs.get(j).space) j++;
			int k = j;
			while (k < n && !glyphs.get(k).lineBreak && glyphs.get(k).space) k++;
			double word = 0;
			for (int m = i; m < j; m++) word += glyphs.get(m).advance;
			double avail = width - p.getIndLeft() - p.getIndRight() - (lines.isEmpty() ? p.getIndFirst() : 0);
			if (!cur.glyphs.isEmpty() && x + word > avail + 1e-6) {
				lines.add(cur);
				cur = new Line();
				x = 0;
				avail = width - p.getIndLeft() - p.getIndRight();
			}
			if (cur.glyphs.isEmpty() && word > avail + 1e-6 && i < j) {
				// a word wider than the line: broken between its characters
				for (int m = i; m < j; m++) {
					Glyph c = glyphs.get(m);
					if (!cur.glyphs.isEmpty() && x + c.advance > avail + 1e-6) {
						lines.add(cur);
						cur = new Line();
						x = 0;
						avail = width - p.getIndLeft() - p.getIndRight();
					}
					c.x = x;
					cur.glyphs.add(c);
					x += c.advance;
				}
				i = j;
				continue;
			}
			for (int m = i; m < k; m++) {
				Glyph c = glyphs.get(m);
				c.x = x;
				cur.glyphs.add(c);
				x += c.advance;
			}
			i = k;
		}
		lines.add(cur);
		return lines;
	}

	/** The line's ascent, descent and single height, Word's, for the largest of its runs (an empty line: its mark's) */
	private static void metrics(Line l, WordArtText.Run mark, double fontScale) {
		double a = 0, d = 0, s = 0;
		boolean any = false;
		for (Glyph g : l.glyphs) {
			WordArtText.Run r = g.run;
			double size = r.getSize() * fontScale;
			a = Math.max(a, r.getAscent() * size);
			d = Math.max(d, r.getDescent() * size);
			s = Math.max(s, r.getSingle() * size);
			any = true;
		}
		if (!any && mark != null) {
			double size = mark.getSize() * fontScale;
			a = mark.getAscent() * size;
			d = mark.getDescent() * size;
			s = mark.getSingle() * size;
		}
		l.ascent = a;
		l.descent = d;
		l.single = Math.max(s, a + d);
	}

	/** The line's height and baseline by its paragraph's line spacing, as the FO output sets Word's */
	private static void space(Line l, double lineScale) {
		WordArtText.Paragraph p = l.para;
		double single = l.single;
		switch (p.getLineRule()) {
			case "exact":
				l.height = p.getLine();
				l.baseline = l.height * l.ascent / Math.max(1e-9, l.ascent + l.descent);
				break;
			case "atLeast":
				l.height = Math.max(single, p.getLine());
				l.baseline = l.height - single + l.ascent;
				break;
			default:
				double lh = single * p.getLine() / 240 * lineScale;
				l.height = lh;
				l.baseline = lh >= single ? l.ascent : Math.max(0, l.ascent - (single - lh));
		}
	}

	// ------------------------------------------------------------------ drawing

	/**
	 * The text box drawn as WordArt: its shape at the box Word draws, then its text.
	 *
	 * @param cx the extent's width in EMU (the stored one: Word's own WordArt stores 144 by 144 pt, whatever its text)
	 * @param cy the extent's height in EMU
	 */
	public static Drawn draw(WordArtText art, ChartFonts fonts, ThemeColors theme, long cx, long cy, ShapeRenderer.Pictures pictures) {
		CTWordprocessingShape wsp = art.getShape();
		CTTextBodyProperties body = art.getBodyPr();
		List<String> problems = new ArrayList<String>(art.getProblems());
		double[] in = ShapeText.withLine(ShapeText.bodyInsets(wsp), ShapeText.linePt(wsp));
		if (in == null) in = ShapeText.DEFAULT_INSETS.clone();
		boolean wrapNone = body != null && body.getWrap() == STTextWrappingType.NONE;
		boolean fit = body != null && body.getSpAutoFit() != null;
		double fontScale = 1, lineScale = 1;
		if (body != null && body.getNormAutofit() != null) {
			fontScale = body.getNormAutofit().getFontScale() / 100000.0;
			lineScale = 1 - body.getNormAutofit().getLnSpcReduction() / 100000.0;
			if (!(fontScale > 0)) fontScale = 1;
		}
		boolean warpNamed = body != null && body.getPrstTxWarp() != null;
		double boxW = cx / 12700.0, boxH = cy / 12700.0;
		double avail = wrapNone ? Double.POSITIVE_INFINITY : Math.max(1, boxW - in[0] - in[2]);
		Layout layout = layout(art, fonts, avail, fontScale, lineScale, warpNamed);
		double w = wrapNone ? layout.width + in[0] + in[2] : boxW;
		double h = fit ? layout.height + in[1] + in[3] : boxH;

		// the shape, at the box Word draws
		long wEmu = Math.max(1, Math.round(w * 12700)), hEmu = Math.max(1, Math.round(h * 12700));
		ShapeRenderer.Drawn shape = ShapeRenderer.draw(wsp.getSpPr(), wsp.getStyle(), theme, wEmu, hEmu, pictures, true,
				body != null && body.getFlatTx() != null);
		for (String p : shape.getProblems()) if (!problems.contains(p)) problems.add(p);
		double[] tin = ShapeText.textInsets(in, shape.getOutlines(), wsp, w, h);
		if (tin == null) tin = in;
		Rectangle2D.Double rect = new Rectangle2D.Double(tin[0], tin[1], Math.max(0, w - tin[0] - tin[2]), Math.max(0, h - tin[1] - tin[3]));

		// the lines placed in the text rectangle: aligned, anchored
		String anchor = ShapeText.anchor(wsp);
		double block = layout.height;
		double y0 = rect.y + ("ctr".equals(anchor) ? (rect.height - block) / 2 : "b".equals(anchor) ? rect.height - block : 0);
		for (Line l : layout.lines) place(l, rect, y0);

		Drawing drawing = new Drawing(w, h);
		for (Object o : shape.getDrawing().getItems()) {
			if (o instanceof Drawing.Area) drawing.add((Drawing.Area) o);
			else if (o instanceof Drawing.Picture) drawing.add((Drawing.Picture) o);
			else if (o instanceof Drawing.Text) drawing.add((Drawing.Text) o);
		}
		Rectangle2D bounds = shape.getBounds();
		AffineTransform turn = ShapeRenderer.turn(wsp.getSpPr() == null ? null : wsp.getSpPr().getXfrm(), w, h);

		// the warp
		String warp = art.getWarp();
		Warped warped = null;
		if (warp != null) {
			ShapeOutlines paths = ShapeGeometry.textWarp(body.getPrstTxWarp(), rect.width, rect.height);
			if (paths.getProblem() != null || paths.getOutlines().isEmpty()) {
				problems.add(WordArtText.UNKNOWN_PRESET);
			} else {
				List<TextWarp.Measured> measured = new ArrayList<TextWarp.Measured>();
				AffineTransform at = AffineTransform.getTranslateInstance(rect.x, rect.y);
				for (ShapeOutline o : paths.getOutlines()) measured.add(new TextWarp.Measured(at.createTransformedShape(o.getPath())));
				warped = new Warped(warp, measured, rect);
			}
		}

		// the runs painted, and their text
		List<Span> spans = new ArrayList<Span>();
		Rectangle2D glyphs = paint(layout, warped, theme, turn, drawing, spans, problems);
		bounds = union(bounds, glyphs);
		bounds = union(bounds, new Rectangle2D.Double(0, 0, w, h));

		StringBuilder text = new StringBuilder();
		for (Line l : layout.lines) {
			for (Glyph g : l.glyphs) if (g.chars != null) text.append(g.chars);
			text.append(l.last ? "\n" : " ");
		}
		return new Drawn(drawing, spans, w, h, rect(bounds), shape.getOutlines(), problems, text.toString().trim());
	}

	/** The line set in the rectangle: its top, its start by its paragraph's alignment, its spaces stretched if justified */
	private static void place(Line l, Rectangle2D.Double rect, double y0) {
		WordArtText.Paragraph p = l.para;
		double start = p.getIndLeft() + (l.first ? p.getIndFirst() : 0);
		double avail = rect.width - start - p.getIndRight();
		double width = l.width();
		String align = p.getAlign();
		double x = start;
		if ("center".equals(align)) x = start + (avail - width) / 2;
		else if ("right".equals(align)) x = start + avail - width;
		else if (("both".equals(align) && !l.last && !l.forced || "distribute".equals(align)) && l.spaces() > 0 && avail > width) {
			l.extraPerSpace = (avail - width) / l.spaces();
		}
		l.x0 = rect.x + x;
		l.top += y0;
		if (l.extraPerSpace > 0) {
			double shift = 0;
			for (Glyph g : l.glyphs) {
				g.x += shift;
				if (g.space) shift += l.extraPerSpace;
			}
		}
	}

	/** A warp evaluated on the text rectangle */
	private static final class Warped {
		final String name;
		final List<TextWarp.Measured> paths;
		final Rectangle2D.Double rect;

		Warped(String name, List<TextWarp.Measured> paths, Rectangle2D.Double rect) {
			this.name = name;
			this.paths = paths;
			this.rect = rect;
		}
	}

	/** The glyph's outline where it is laid out, unwarped: its line's start and baseline */
	private static Shape placed(Line l, Glyph g) {
		return AffineTransform.getTranslateInstance(l.x0 + g.x, l.top + l.baseline).createTransformedShape(g.outline);
	}

	/**
	 * Each run's glyphs painted as Word paints them (D2), warped where there is a warp; its text as spans. The area its
	 * outlines paint, or null.
	 */
	private static Rectangle2D paint(Layout layout, Warped warped, ThemeColors theme, AffineTransform turn, Drawing drawing, List<Span> spans,
			List<String> problems) {
		// the glyphs where Word draws them: unwarped, or by the warp
		Map<Glyph, Shape> drawn = new HashMap<Glyph, Shape>();
		for (Line l : layout.lines) {
			for (Glyph g : l.glyphs) {
				if (g.space || g.lineBreak || g.outline == null) continue;
				drawn.put(g, placed(l, g));
			}
		}
		Map<Glyph, double[]> followed = null;
		if (warped != null) followed = WordArtWarps.warp(layout, warped.name, warped.paths, warped.rect, drawn, problems);

		// each run: its glyphs over all its lines, painted at once (a gradient spans the run)
		Map<WordArtText.Run, Path2D.Double> byRun = new java.util.LinkedHashMap<WordArtText.Run, Path2D.Double>();
		for (Line l : layout.lines) {
			for (Glyph g : l.glyphs) {
				Shape s = drawn.get(g);
				if (s == null) continue;
				byRun.computeIfAbsent(g.run, r -> new Path2D.Double(Path2D.WIND_NON_ZERO)).append(turn.createTransformedShape(s), false);
			}
		}
		Rectangle2D painted = null;
		Fills fills = new Fills(theme);
		for (Map.Entry<WordArtText.Run, Path2D.Double> e : byRun.entrySet()) {
			WordArtText.Run r = e.getKey();
			if (r.paint(warped != null) != WordArtText.Paint.PATHS) continue;
			Path2D.Double s = e.getValue();
			Object fill = r.getFill();
			if (fill == null) {
				Color c = Fills.awt(r.getColour());
				if (c != null) {
					drawing.add(new Drawing.Area(s, c, null, 0, "wordart.fill"));
					painted = union(painted, s.getBounds2D());
				}
			} else if (fill instanceof CTSolidColorFillProperties) {
				Color c = Fills.awt(DrawingMLColors.resolve(fill, theme));
				if (c != null) {
					drawing.add(new Drawing.Area(s, c, null, 0, "wordart.fill"));
					painted = union(painted, s.getBounds2D());
				}
			} else if (fill instanceof CTGradientFillProperties) {
				Drawing.Gradient gr = fills.gradient((CTGradientFillProperties) fill);
				if (gr != null) {
					Drawing.Area a = new Drawing.Area(s, gr.getColours()[0], null, 0, "wordart.fill");
					a.setGradient(gr);
					drawing.add(a);
					painted = union(painted, s.getBounds2D());
				}
			} else if (!(fill instanceof CTNoFillProperties)) {
				Color c = Fills.awt(r.getColour());
				if (c != null) drawing.add(new Drawing.Area(s, c, null, 0, "wordart.fill"));
			}
			if (r.getOutline() != null && r.getOutline().getNoFill() == null) {
				painted = union(painted, ShapeRenderer.strokeText(drawing, s, r.getOutline(), theme));
			}
		}

		// the text: real where Word keeps it, invisible where it draws outlines, at the unwarped layout (Follow Path: scaled)
		for (Line l : layout.lines) {
			Span span = null;
			for (Glyph g : l.glyphs) {
				if (g.lineBreak || g.chars == null || g.chars.isEmpty()) continue;
				WordArtText.Paint how = g.run.paint(warped != null);
				Color fill = null;
				if (how == WordArtText.Paint.TEXT) {
					Object f = g.run.getFill();
					fill = f instanceof CTSolidColorFillProperties ? Fills.awt(DrawingMLColors.resolve(f, theme)) : Fills.awt(g.run.getColour());
					if (fill == null) fill = Color.BLACK;
				}
				if (followed != null && followed.containsKey(g)) {
					// Follow Path: Word's invisible text is each glyph's own, where the glyph is, turned and scaled
					double[] f = followed.get(g);
					Span one = new Span();
					one.font = g.font;
					one.face = g.run.getFace();
					one.size = g.font.getSize2D() * f[0];
					one.fill = fill;
					one.y = 0;
					AffineTransform t = new AffineTransform(turn);
					t.translate(f[1], f[2]);
					t.rotate(f[3]);
					one.transform = t;
					one.text.append(g.chars);
					one.xs.add(0.0);
					spans.add(one);
					span = null;
					continue;
				}
				double scale = 1, x = l.x0 + g.x, y = l.top + l.baseline;
				if (span == null || span.font != g.font || Math.abs(span.size - g.font.getSize2D()) > 1e-9
						|| !same(span.fill, fill) || Math.abs(span.y - y) > 1e-6) {
					span = new Span();
					span.font = g.font;
					span.face = g.run.getFace();
					span.size = g.font.getSize2D() * scale;
					span.fill = fill;
					span.y = y;
					span.transform = turn;
					spans.add(span);
				}
				for (int i = 0; i < g.chars.length(); i++) {
					span.text.append(g.chars.charAt(i));
					span.xs.add(x + (i == 0 ? 0 : g.advance * scale * i / g.chars.length()));
				}
			}
		}
		// spans of spaces alone carry nothing
		spans.removeIf(s -> s.text.toString().trim().isEmpty());
		return painted;
	}

	private static boolean same(Color a, Color b) {
		return a == null ? b == null : a.equals(b);
	}

	static Rectangle2D union(Rectangle2D a, Rectangle2D b) {
		if (a == null) return b;
		if (b == null) return a;
		return a.createUnion(b);
	}

	private static Rectangle2D.Double rect(Rectangle2D r) {
		return new Rectangle2D.Double(r.getX(), r.getY(), r.getWidth(), r.getHeight());
	}

	static String num(double v) {
		String s = String.format(Locale.ROOT, "%.4f", v);
		while (s.contains(".") && (s.endsWith("0") || s.endsWith("."))) s = s.substring(0, s.length() - 1);
		return "-0".equals(s) ? "0" : s;
	}
}
