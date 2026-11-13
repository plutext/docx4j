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
import org.docx4j.dml.CTEffectList;
import org.docx4j.dml.CTGradientFillProperties;
import org.docx4j.dml.CTLineProperties;
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
import org.docx4j.model.shapes.TextEffects;
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
 * <li><b>Decorations</b>: a run's underline, strikes, highlight and shading are drawn on flat WordArt as the FO output
 * draws them, and a superscript or subscript is set as it sets them; on warped WordArt the decorations are reported
 * ({@link WordArtText#DECORATION}), how Word warps them not being measured.</li>
 * <li><b>Effects</b> (phase 3, phase 3 findings section 4): a run's shadow, glow and reflection are drawn as Word
 * draws them, pictures under its glyphs ({@link Effects#drawText}), an inner shadow over them: flat, each run's glyphs
 * on a line together, in its line box, at {@link TextEffects#scale}; warped, at their stated lengths, in the warped
 * glyphs' box, on Follow Path glyph by glyph (D14). They take room in the line as Word gives it, flat or warped
 * ({@link TextEffects#reach}): the line taller, its text lower.</li>
 * </ul>
 * 3-D (phase 6) isn't drawn yet, and is reported. A face with no font
 * file is drawn in AWT's fallback and reported ({@link WordArtText#FONT}); a warp whose outlines would pass
 * {@link TextWarp#MAX_POINTS} points is drawn unwarped ({@link WordArtText#OVER_LIMIT}).
 *
 * @since 17.3.7
 */
public final class WordArtRenderer {

	/** Glyphs are outlined at this size and scaled, so that no hinting reaches them, and measured at it */
	private static final float OUTLINE_SIZE = 1000f;

	private static final FontRenderContext FRC = new FontRenderContext(null, true, true);

	private static final String SVG_NS = "http://www.w3.org/2000/svg";

	/** AWT's logical families: a font made of a face with no file is one of them */
	private static final java.util.Set<String> LOGICAL = new java.util.HashSet<String>(java.util.Arrays.asList("Dialog", "DialogInput",
			"SansSerif", "Serif", "Monospaced"));

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
		/** what is drawn over the text: inner shadows */
		private final Drawing over;

		Drawn(Drawing drawing, List<Span> spans, double width, double height, Rectangle2D.Double bounds, ShapeOutlines outlines,
				List<String> problems, String text, Drawing over) {
			this.over = over;
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

		/** What is drawn over its text: the runs' inner shadows (phase 3) */
		public Drawing getOver() {
			return over;
		}

		/**
		 * As SVG, a user unit a point, its canvas {@link #getBounds()}: the paths (the effects' pictures among them), then
		 * the text, visible where Word keeps it as text, with no fill where Word draws it as outlines, then what is over
		 * it. Its families are those docx4j's font mapping gives FOP, then the document's faces, then the generic family,
		 * as a chart's.
		 */
		public Document toSvg() {
			Document doc = SvgWriter.toSvg(drawing, bounds);
			Element root = doc.getDocumentElement();
			for (Span s : spans) root.appendChild(s.element(doc));
			if (over != null && !over.getItems().isEmpty()) {
				Element top = SvgWriter.toSvg(over, bounds).getDocumentElement();
				for (org.w3c.dom.Node n = top.getFirstChild(); n != null; n = n.getNextSibling()) {
					// Batik's empty defs (its id would be the drawing's twice)
					if (n instanceof Element && "defs".equals(n.getLocalName()) && !n.hasChildNodes()) continue;
					root.appendChild(doc.importNode(n, true));
				}
			}
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
		StringBuilder b = new StringBuilder(quoted(font.getFamily()));
		if (face != null && !face.equalsIgnoreCase(font.getFamily())) b.append(", ").append(quoted(face));
		org.docx4j.fonts.FontFallback.FontClass c = org.docx4j.fonts.FontFallback.classOf(face == null ? font.getFamily() : face);
		if (c == org.docx4j.fonts.FontFallback.FontClass.UNKNOWN) c = org.docx4j.fonts.FontFallback.classOf(font.getFamily());
		String generic = c == org.docx4j.fonts.FontFallback.FontClass.SERIF ? "serif" : c == org.docx4j.fonts.FontFallback.FontClass.SANS ? "sans-serif"
				: c == org.docx4j.fonts.FontFallback.FontClass.MONO ? "monospace" : null;
		if (generic != null) b.append(", ").append(generic);
		return b.toString();
	}

	/**
	 * The face has no file, docx4j's mapping no substitute: AWT made the font of one of its logical families, which FOP
	 * doesn't draw the run's text in (a substitute docx4j maps, which FOP draws too, isn't a fallback)
	 */
	static boolean fellBack(Font font, String face) {
		return LOGICAL.contains(font.getFamily(Locale.ROOT)) && (face == null || !LOGICAL.contains(face));
	}

	/** A family as a CSS string: in single quotes, or double where it has one, a backslash before any quote of that kind */
	static String quoted(String family) {
		String q = family.indexOf('\'') < 0 ? "'" : "\"";
		return q + family.replace("\\", "\\\\").replace(q, "\\" + q) + q;
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
		/** the room its runs' effects take above and below it (phase 3) */
		double roomAbove, roomBelow;

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
		/** faces with no font file, drawn in AWT's fallback */
		final java.util.Set<String> unmapped = new java.util.LinkedHashSet<String>();
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
				shape(r, fonts, fontScale, kernAll, cache, glyphs, out.unmapped);
			}
			List<Line> lines = breakLines(p, glyphs, width);
			for (int i = 0; i < lines.size(); i++) {
				Line l = lines.get(i);
				l.para = p;
				l.first = i == 0;
				l.last = i == lines.size() - 1;
				metrics(l, mark != null ? mark : previous != null && !previous.glyphs.isEmpty() ? previous.glyphs.get(0).run : null, fontScale);
				space(l, lineScale);
				room(l, fontScale);
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
	private static void shape(WordArtText.Run r, ChartFonts fonts, double fontScale, boolean kernAll, Outlines cache, List<Glyph> out,
			java.util.Set<String> unmapped) {
		double size = r.getSize() * fontScale;
		Font base = fonts.font(r.getFace(), r.isBold(), r.isItalic(), 1);
		if (r.getText() != null && !r.getText().isEmpty() && fellBack(base, fonts.face(r.getFace()))) unmapped.add(fonts.face(r.getFace()));
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

	/**
	 * The line's ascent (with the font's leading above it, as Word sets a line: phase 3 findings section 4), descent and
	 * single height, Word's, for the largest of its runs (an empty line: its mark's)
	 */
	private static void metrics(Line l, WordArtText.Run mark, double fontScale) {
		double a = 0, d = 0, s = 0;
		boolean any = false;
		for (Glyph g : l.glyphs) {
			WordArtText.Run r = g.run;
			double size = r.getSize() * fontScale;
			a = Math.max(a, frameAscent(r) * size);
			d = Math.max(d, r.getDescent() * size);
			s = Math.max(s, r.getSingle() * size);
			any = true;
		}
		if (!any && mark != null) {
			double size = mark.getSize() * fontScale;
			a = frameAscent(mark) * size;
			d = mark.getDescent() * size;
			s = mark.getSingle() * size;
		}
		l.ascent = a;
		l.descent = d;
		l.single = Math.max(s, a + d);
	}

	/** The run's ascent with its font's leading above it, as Word sets its line, a fraction of its size */
	static double frameAscent(WordArtText.Run r) {
		return r.getAscent() + r.getLeading();
	}

	/**
	 * The room its runs' shadows, glows and reflections take ({@link TextEffects#reach}, each on its run's line box): the
	 * line taller by it, its baseline lower by what is above. Word gives it on the line before any warp, flat or warped
	 * alike (phase 3 findings section 4); with exact line spacing it isn't measured, and none is given.
	 */
	private static void room(Line l, double fontScale) {
		if ("exact".equals(l.para.getLineRule())) return;
		Map<WordArtText.Run, double[]> runs = new java.util.LinkedHashMap<WordArtText.Run, double[]>();
		for (Glyph g : l.glyphs) {
			if (g.lineBreak || g.run.getEffects() == null) continue;
			double[] x = runs.computeIfAbsent(g.run, r -> new double[] { Double.MAX_VALUE, -Double.MAX_VALUE });
			if (!g.space) {
				x[0] = Math.min(x[0], g.x);
				x[1] = Math.max(x[1], g.x + g.advance);
			}
		}
		if (runs.isEmpty()) return;
		double up = l.ascent, down = l.descent;
		for (Map.Entry<WordArtText.Run, double[]> e : runs.entrySet()) {
			WordArtText.Run r = e.getKey();
			double size = r.getSize() * fontScale;
			double[] x = e.getValue();
			double[] reach = TextEffects.reach(r.getEffects(), size, frameAscent(r) * size, r.getDescent() * size, x[1] > x[0] ? x[1] - x[0] : 0);
			up = Math.max(up, reach[0]);
			down = Math.max(down, reach[1]);
		}
		l.roomAbove = up - l.ascent;
		l.roomBelow = down - l.descent;
		l.height += l.roomAbove + l.roomBelow;
		l.baseline += l.roomAbove;
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
	 * @return null where it can't be drawn as WordArt and stays text: an extent of no width that its text doesn't size
	 *         ({@code wrap="none"}), or of no height that its lines don't ({@code a:spAutoFit})
	 */
	public static Drawn draw(WordArtText art, ChartFonts fonts, ThemeColors theme, long cx, long cy, ShapeRenderer.Pictures pictures) {
		CTWordprocessingShape wsp = art.getShape();
		CTTextBodyProperties body = art.getBodyPr();
		List<String> problems = new ArrayList<String>(art.getProblems());
		double[] in = ShapeText.withLine(ShapeText.bodyInsets(wsp), ShapeText.linePt(wsp));
		if (in == null) in = ShapeText.DEFAULT_INSETS.clone();
		boolean wrapNone = body != null && body.getWrap() == STTextWrappingType.NONE;
		boolean fit = body != null && body.getSpAutoFit() != null;
		// a box of no width (or height) that its text doesn't size: one glyph a line; left as text, as other text boxes are
		if (cx <= 0 && !wrapNone || cy <= 0 && !fit) return null;
		double fontScale = 1, lineScale = 1;
		if (body != null && body.getNormAutofit() != null) {
			fontScale = body.getNormAutofit().getFontScale() / 100000.0;
			lineScale = 1 - body.getNormAutofit().getLnSpcReduction() / 100000.0;
			if (!(fontScale > 0)) fontScale = 1;
		}
		boolean warpNamed = body != null && body.getPrstTxWarp() != null;
		double boxW = cx / 12700.0, boxH = cy / 12700.0;
		double avail = wrapNone ? Double.POSITIVE_INFINITY : Math.max(1, boxW - in[0] - in[2]);
		Layout layout;
		double w, h;
		ShapeRenderer.Drawn shape;
		double[] tin;
		Rectangle2D.Double rect;
		for (int pass = 0;; pass++) {
			layout = layout(art, fonts, avail, fontScale, lineScale, warpNamed);
			w = wrapNone ? layout.width + in[0] + in[2] : boxW;
			h = fit ? layout.height + in[1] + in[3] : boxH;

			// the shape, at the box Word draws
			long wEmu = Math.max(1, Math.round(w * 12700)), hEmu = Math.max(1, Math.round(h * 12700));
			shape = ShapeRenderer.draw(wsp.getSpPr(), wsp.getStyle(), theme, wEmu, hEmu, pictures, true,
					body != null && body.getFlatTx() != null);
			tin = ShapeText.textInsets(in, shape.getOutlines(), wsp, w, h);
			if (tin == null) tin = in;
			rect = new Rectangle2D.Double(tin[0], tin[1], Math.max(0, w - tin[0] - tin[2]), Math.max(0, h - tin[1] - tin[3]));
			// a geometry whose text rectangle is narrower than the box (an ellipse): the lines broken at its width, as the
			// text route breaks them (once: under spAutoFit the rectangle then follows the new height)
			if (wrapNone || pass > 0 || rect.width >= avail - 1e-6) break;
			avail = Math.max(1, rect.width);
		}
		for (String p : shape.getProblems()) if (!problems.contains(p)) problems.add(p);
		if (!layout.unmapped.isEmpty()) {
			if (!problems.contains(WordArtText.FONT)) problems.add(WordArtText.FONT);
		}

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

		// the runs painted, and their text; what is over the text
		List<Span> spans = new ArrayList<Span>();
		Drawing over = new Drawing(w, h);
		// flat effects are drawn within the box across and its text rectangle up and down (Word's clip)
		Rectangle2D glyphs = paint(layout, warped, theme, turn, drawing, over, spans, problems, new Rectangle2D.Double(0, rect.y, w, rect.height));
		bounds = union(bounds, glyphs);
		bounds = union(bounds, new Rectangle2D.Double(0, 0, w, h));

		StringBuilder text = new StringBuilder();
		for (Line l : layout.lines) {
			StringBuilder line = new StringBuilder();
			for (Glyph g : l.glyphs) if (g.chars != null) line.append(g.chars);
			// the space a wrapped line ends with is the one between the lines
			text.append(line.toString().stripTrailing()).append(l.last ? "\n" : " ");
		}
		return new Drawn(drawing, spans, w, h, rect(bounds), shape.getOutlines(), problems, text.toString().trim(), over);
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
		return AffineTransform.getTranslateInstance(l.x0 + g.x, baseline(l, g)).createTransformedShape(g.outline);
	}

	/** The glyph's baseline: its line's, raised by a superscript or lowered by a subscript */
	private static double baseline(Line l, Glyph g) {
		return l.top + l.baseline - g.run.getRise();
	}

	/**
	 * Each run's glyphs painted as Word paints them (D2), warped where there is a warp, its effects under them (and an
	 * inner shadow in over); its text as spans. The area its outlines and effects paint, or null.
	 */
	private static Rectangle2D paint(Layout layout, Warped warped, ThemeColors theme, AffineTransform turn, Drawing drawing, Drawing over,
			List<Span> spans, List<String> problems, Rectangle2D clip) {
		// the glyphs where Word draws them: unwarped, or by the warp
		Map<Glyph, Shape> drawn = new HashMap<Glyph, Shape>();
		for (Line l : layout.lines) {
			for (Glyph g : l.glyphs) {
				if (g.space || g.lineBreak || g.outline == null) continue;
				drawn.put(g, placed(l, g));
			}
		}
		Map<Glyph, double[]> followed = null;
		if (warped != null) {
			Map<Glyph, Shape> flat = new HashMap<Glyph, Shape>(drawn);
			try {
				followed = WordArtWarps.warp(layout, warped.name, warped.paths, warped.rect, drawn, problems);
			} catch (TextWarp.OverLimit e) {
				// too many points to warp: the text drawn as if it had no warp, reported
				drawn.clear();
				drawn.putAll(flat);
				followed = null;
				warped = null;
				if (!problems.contains(WordArtText.OVER_LIMIT)) problems.add(WordArtText.OVER_LIMIT);
			}
		}

		// a run's shading and highlight behind it, where the text isn't warped (as the FO output draws them)
		boolean decorations = false;
		for (Line l : layout.lines) for (Glyph g : l.glyphs) decorations |= g.run.hasDecoration();
		if (decorations && warped != null) {
			if (!problems.contains(WordArtText.DECORATION)) problems.add(WordArtText.DECORATION);
			decorations = false;
		}
		List<Stretch> stretches = decorations ? stretches(layout) : Collections.<Stretch>emptyList();
		Rectangle2D painted = null;
		for (Stretch st : stretches) {
			WordArtText.Run r = st.run;
			double size = st.font.getSize2D();
			Rectangle2D.Double behind = new Rectangle2D.Double(st.x0, st.baseline - r.getAscent() * size, st.x1 - st.x0,
					(r.getAscent() + r.getDescent()) * size);
			for (org.docx4j.model.color.ResolvedColor rc : new org.docx4j.model.color.ResolvedColor[] { r.getShading(), r.getHighlight() }) {
				Color c = Fills.awt(rc);
				if (c == null || st.x1 <= st.x0) continue;
				Shape sh = turn.createTransformedShape(behind);
				drawing.add(new Drawing.Area(sh, c, null, 0, "wordart.highlight"));
				painted = union(painted, sh.getBounds2D());
			}
		}

		// the runs' shadows, glows and reflections, under their glyphs (phase 3)
		painted = effects(layout, warped != null, followed != null, drawn, theme, turn, drawing, over, problems, painted,
				warped == null ? clip : null);

		// each run: its glyphs over all its lines, painted at once (a gradient spans the run)
		Map<WordArtText.Run, Path2D.Double> byRun = new java.util.LinkedHashMap<WordArtText.Run, Path2D.Double>();
		for (Line l : layout.lines) {
			for (Glyph g : l.glyphs) {
				Shape s = drawn.get(g);
				if (s == null) continue;
				byRun.computeIfAbsent(g.run, r -> new Path2D.Double(Path2D.WIND_NON_ZERO)).append(turn.createTransformedShape(s), false);
			}
		}
		Fills fills = new Fills(theme);
		for (Map.Entry<WordArtText.Run, Path2D.Double> e : byRun.entrySet()) {
			WordArtText.Run r = e.getKey();
			if (r.paint(warped != null) != WordArtText.Paint.PATHS) continue;
			Path2D.Double s = e.getValue();
			Object fill = r.getFill();
			boolean filled = false;
			if (fill == null) {
				Color c = Fills.awt(r.getColour());
				if (c != null) {
					drawing.add(new Drawing.Area(s, c, null, 0, "wordart.fill"));
					painted = union(painted, s.getBounds2D());
					filled = true;
				}
			} else if (fill instanceof CTSolidColorFillProperties) {
				Color c = Fills.awt(DrawingMLColors.resolve(fill, theme));
				if (c != null) {
					drawing.add(new Drawing.Area(s, c, null, 0, "wordart.fill"));
					painted = union(painted, s.getBounds2D());
					filled = true;
				}
			} else if (fill instanceof CTGradientFillProperties) {
				Drawing.Gradient gr = fills.gradient((CTGradientFillProperties) fill);
				if (gr != null) {
					Drawing.Area a = new Drawing.Area(s, gr.getColours()[0], null, 0, "wordart.fill");
					a.setGradient(gr);
					drawing.add(a);
					painted = union(painted, s.getBounds2D());
					filled = true;
				}
			} else if (!(fill instanceof CTNoFillProperties)) {
				Color c = Fills.awt(r.getColour());
				if (c != null) {
					drawing.add(new Drawing.Area(s, c, null, 0, "wordart.fill"));
					filled = true;
				}
			} else {
				filled = true; // no fill: nothing to draw
			}
			if (!filled && !problems.contains(WordArtText.FILL)) problems.add(WordArtText.FILL);
			CTLineProperties ln = r.getOutline();
			if (ln != null && ln.getNoFill() == null) {
				Rectangle2D stroked = ShapeRenderer.strokeText(drawing, s, ln, theme);
				if (stroked == null && (ln.getSolidFill() != null || ln.getGradFill() != null || ln.getPattFill() != null)
						&& !problems.contains(WordArtText.OUTLINE)) {
					problems.add(WordArtText.OUTLINE);
				}
				painted = union(painted, stroked);
			}
		}

		// a run's underline and strikes, where the text isn't warped (as the FO output draws them)
		for (Stretch st : stretches) {
			WordArtText.Run r = st.run;
			Color c = Fills.awt(r.getUnderlineColour() != null ? r.getUnderlineColour() : textColour(r, theme));
			Color text = Fills.awt(textColour(r, theme));
			java.awt.font.LineMetrics lm = st.font.getLineMetrics(st.chars.toString(), FRC);
			double size = st.font.getSize2D();
			double ut = lm.getUnderlineThickness() > 0 ? lm.getUnderlineThickness() : size * 0.05;
			double uo = lm.getUnderlineOffset() > 0 ? lm.getUnderlineOffset() : size * 0.1;
			double stt = lm.getStrikethroughThickness() > 0 ? lm.getStrikethroughThickness() : ut;
			double so = lm.getStrikethroughOffset() < 0 ? lm.getStrikethroughOffset() : -size * 0.25;
			List<double[]> bars = new ArrayList<double[]>();
			if (r.getUnderline() != null && c != null) {
				org.docx4j.wml.UnderlineEnumeration u = r.getUnderline();
				double t = heavy(u) ? 2 * ut : ut;
				for (double[] x : st.underlined) {
					bars.add(new double[] { x[0], x[1], st.baseline + uo - t / 2, t, c.getRGB() });
					if (u == org.docx4j.wml.UnderlineEnumeration.DOUBLE || u == org.docx4j.wml.UnderlineEnumeration.WAVY_DOUBLE) {
						bars.add(new double[] { x[0], x[1], st.baseline + uo + 1.5 * t, t, c.getRGB() });
					}
				}
			}
			if (text != null && r.isDoubleStrike()) {
				bars.add(new double[] { st.x0, st.x1, st.baseline + so - 1.5 * stt, stt, text.getRGB() });
				bars.add(new double[] { st.x0, st.x1, st.baseline + so + 0.5 * stt, stt, text.getRGB() });
			} else if (text != null && r.isStrike()) {
				bars.add(new double[] { st.x0, st.x1, st.baseline + so - stt / 2, stt, text.getRGB() });
			}
			for (double[] b : bars) {
				if (b[1] <= b[0]) continue;
				Shape sh = turn.createTransformedShape(new Rectangle2D.Double(b[0], b[2], b[1] - b[0], b[3]));
				drawing.add(new Drawing.Area(sh, new Color((int) b[4], true), null, 0, "wordart.line"));
				painted = union(painted, sh.getBounds2D());
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
				double scale = 1, x = l.x0 + g.x, y = baseline(l, g);
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

	// ------------------------------------------------------------------ effects (phase 3)

	/**
	 * Each unit's shadow, glow and reflection ({@link Effects#drawText}), as Word draws them (phase 3 findings section 4),
	 * added to the drawing before the glyphs, an inner shadow to over:
	 * <ul>
	 * <li>flat: a run's glyphs on a line, at {@link TextEffects#scale} (a reflection's blur at
	 * {@link TextEffects#reflectionScale}, moved {@link TextEffects#reflectionMove}), in the run's line box (its ascent
	 * box for the reflection);</li>
	 * <li>on two paths: a run's glyphs on a line, at the stated lengths, in their warped box;</li>
	 * <li>on Follow Path: each glyph, at the stated lengths, the reflection's frame the whole warped block (D14).</li>
	 * </ul>
	 * Word draws each unit's pictures just before its glyphs; here they are all before the glyphs (a Follow Path glyph's
	 * shadow doesn't fall on the glyph before it) and, for a run kept as text, before all the text. Flat, Word draws each
	 * picture within the box across and the text rectangle up and down (its PDF's clip paths): a shadow 12 pt to the right
	 * of 72 pt text, past the box's 7.2 pt inset, is cut at its right side (we12-32), a shadow scaled 150% too (we10-26);
	 * warped, within the page.
	 *
	 * <p>A WordArt's effects are drawn in at most {@link #MAX_EFFECT_PICTURES} pictures and {@link #MAX_EFFECT_PIXELS}
	 * pixels (Follow Path takes a picture a glyph an effect); past either none is drawn, and
	 * {@link Effects#OVER_LIMIT} is reported, as a warp past {@link TextWarp#MAX_POINTS} is drawn unwarped.</p>
	 *
	 * @param clip where flat effects are drawn within, in the box's frame; null for none
	 */
	private static Rectangle2D effects(Layout layout, boolean warped, boolean follow, Map<Glyph, Shape> drawn, ThemeColors theme,
			AffineTransform turn, Drawing drawing, Drawing over, List<String> problems, Rectangle2D painted, Rectangle2D clip) {
		Rectangle2D block = null;
		if (follow) for (Shape s : drawn.values()) block = union(block, s.getBounds2D());
		double cap = ShapeRenderer.maxPixelsPerPoint();
		// the units, line by line: a run's glyphs on a line
		List<Map<WordArtText.Run, List<Glyph>>> lines = new ArrayList<Map<WordArtText.Run, List<Glyph>>>();
		long pictures = 0;
		for (Line l : layout.lines) {
			Map<WordArtText.Run, List<Glyph>> units = new java.util.LinkedHashMap<WordArtText.Run, List<Glyph>>();
			for (Glyph g : l.glyphs) {
				if (g.run.getEffects() != null && drawn.containsKey(g)) units.computeIfAbsent(g.run, r -> new ArrayList<Glyph>()).add(g);
			}
			for (Map.Entry<WordArtText.Run, List<Glyph>> e : units.entrySet()) {
				pictures += (long) count(e.getKey().getEffects()) * (follow ? e.getValue().size() : 1);
			}
			lines.add(units);
		}
		if (pictures > MAX_EFFECT_PICTURES) {
			if (!problems.contains(Effects.OVER_LIMIT)) problems.add(Effects.OVER_LIMIT);
			return painted;
		}
		final Rectangle2D before = painted;
		List<Drawing.Picture> added = new ArrayList<Drawing.Picture>();
		for (int li = 0; li < layout.lines.size(); li++) {
			Line l = layout.lines.get(li);
			Map<WordArtText.Run, List<Glyph>> units = lines.get(li);
			for (Map.Entry<WordArtText.Run, List<Glyph>> e : units.entrySet()) {
				if (pixels(added) > MAX_EFFECT_PIXELS) {
					// past the budget: none of them drawn
					drawing.getItems().removeAll(added);
					over.getItems().removeAll(added);
					if (!problems.contains(Effects.OVER_LIMIT)) problems.add(Effects.OVER_LIMIT);
					return before;
				}
				WordArtText.Run r = e.getKey();
				List<Glyph> gs = e.getValue();
				if (!warped) {
					double size = gs.get(0).font.getSize2D();
					CTEffectList fx = TextEffects.scaled(r.getEffects(), TextEffects.scale(size), TextEffects.reflectionScale(size),
							TextEffects.REFLECTION_MOVE * r.getDescent() * size);
					Glyph first = gs.get(0), last = gs.get(gs.size() - 1);
					double base = baseline(l, first), asc = frameAscent(r) * size, desc = r.getDescent() * size;
					double x0 = l.x0 + first.x, width = Math.max(0, last.x + last.advance - first.x);
					Rectangle2D frame = new Rectangle2D.Double(x0, base - asc, width, asc + desc);
					Rectangle2D mirror = new Rectangle2D.Double(x0, base - asc, width, asc);
					painted = unit(fx, theme, frame, mirror, outlines(gs, drawn), r, turn, drawing, over, problems, painted, cap, clip, added);
				} else if (!follow) {
					Shape s = outlines(gs, drawn);
					Rectangle2D box = s.getBounds2D();
					painted = unit(TextEffects.scaled(r.getEffects(), 1, 1, 1), theme, box, box, s, r, turn, drawing, over, problems, painted, cap,
							null, added);
				} else {
					CTEffectList fx = TextEffects.scaled(r.getEffects(), 1, 1, 1);
					for (Glyph g : gs) {
						Shape s = drawn.get(g);
						painted = unit(fx, theme, s.getBounds2D(), block, s, r, turn, drawing, over, problems, painted, cap, null, added);
					}
				}
			}
		}
		if (pixels(added) > MAX_EFFECT_PIXELS) {
			drawing.getItems().removeAll(added);
			over.getItems().removeAll(added);
			if (!problems.contains(Effects.OVER_LIMIT)) problems.add(Effects.OVER_LIMIT);
			return before;
		}
		return painted;
	}

	/**
	 * At most this many effect pictures in a WordArt (a Follow Path glyph takes one an effect: 1,000 glyphs with three
	 * effects would take 3,000 pictures, a 15.7 MB SVG and half a gigabyte: the phase 3 review)
	 */
	public static final int MAX_EFFECT_PICTURES = 600;

	/** At most this many pixels in a WordArt's effect pictures: one picture's worth under CR-081's D10 */
	public static final long MAX_EFFECT_PIXELS = Effects.MAX_PIXELS;

	/** The pictures a unit's effects take: one an effect */
	private static int count(CTEffectList fx) {
		if (fx == null) return 0;
		return (fx.getOuterShdw() != null ? 1 : 0) + (fx.getInnerShdw() != null ? 1 : 0) + (fx.getGlow() != null ? 1 : 0)
				+ (fx.getReflection() != null ? 1 : 0);
	}

	private static long pixels(List<Drawing.Picture> pictures) {
		long n = 0;
		for (Drawing.Picture p : pictures) n += (long) p.getImage().getWidth() * p.getImage().getHeight();
		return n;
	}

	/** The glyphs' outlines where they are drawn, as one shape */
	private static Shape outlines(List<Glyph> glyphs, Map<Glyph, Shape> drawn) {
		Path2D.Double p = new Path2D.Double(Path2D.WIND_NON_ZERO);
		for (Glyph g : glyphs) p.append(drawn.get(g), false);
		return p;
	}

	/**
	 * One unit's effects, cast by its glyphs as the run paints them (its fill's transparency, its outline's width; a
	 * gradient as opaque), placed in the drawing (turned with the box: Word's turn with it, we19) and in over
	 */
	private static Rectangle2D unit(CTEffectList fx, ThemeColors theme, Rectangle2D frame, Rectangle2D mirror, Shape glyphs, WordArtText.Run r,
			AffineTransform turn, Drawing drawing, Drawing over, List<String> problems, Rectangle2D painted, double cap, Rectangle2D clip,
			List<Drawing.Picture> added) {
		if (fx == null) return painted;
		Color fill = silhouette(r, theme);
		java.awt.BasicStroke stroke = null;
		CTLineProperties ln = r.getOutline();
		if (ln != null && ln.getNoFill() == null) {
			float w = ln.getW() == null ? 0.75f : (float) (ln.getW() / 12700.0);
			if (w > 0) stroke = new java.awt.BasicStroke(w, java.awt.BasicStroke.CAP_BUTT, java.awt.BasicStroke.JOIN_ROUND);
		}
		// nothing painted, nothing cast (we15-09, -10); its room is taken still
		if (fill == null && stroke == null) return painted;
		Rectangle2D reach = glyphs.getBounds2D();
		if (stroke != null) reach = reach.createUnion(stroke.createStrokedShape(glyphs).getBounds2D());
		final java.awt.BasicStroke line = stroke;
		Effects.Painter painter = g -> {
			if (fill != null) {
				g.setColor(fill);
				g.fill(glyphs);
			}
			if (line != null) {
				g.setColor(Color.BLACK);
				g.setStroke(line);
				g.draw(glyphs);
			}
		};
		Effects.Result res;
		try {
			res = Effects.drawText(fx, theme, frame, mirror, reach, painter, cap);
		} catch (RuntimeException e) {
			if (!problems.contains(WordArtText.EFFECT)) problems.add(WordArtText.EFFECT);
			return painted;
		}
		for (String c : res.getProblems()) if (!problems.contains(c)) problems.add(c);
		// a shadow's or glow's colour that doesn't resolve: nothing drawn for it
		for (Object e : new Object[] { fx.getOuterShdw(), fx.getInnerShdw(), fx.getGlow() }) {
			if (e != null && Effects.colourOf(e, theme) == null && !problems.contains(WordArtText.EFFECT)) problems.add(WordArtText.EFFECT);
		}
		for (Effects.Picture p : res.getPictures()) {
			Drawing.Picture dp = turned(p, turn, p.getLayer() == Effects.Layer.OVER ? null : clip);
			(p.getLayer() == Effects.Layer.OVER ? over : drawing).add(dp);
			added.add(dp);
			// what is shown of it: within its clip
			Rectangle2D shown = dp.getClip() == null ? dp.getInto() : dp.getInto().createIntersection(dp.getClip().getBounds2D());
			if (shown.getWidth() > 0 && shown.getHeight() > 0) painted = union(painted, shown);
		}
		return painted;
	}

	/** What the run's glyphs cast with: black at its fill's opacity (a gradient's as opaque); null where it has no fill */
	private static Color silhouette(WordArtText.Run r, ThemeColors theme) {
		Object fill = r.getFill();
		if (fill instanceof CTNoFillProperties) return null;
		int alpha = 255;
		if (fill instanceof CTSolidColorFillProperties) {
			Color c = Fills.awt(DrawingMLColors.resolve(fill, theme));
			if (c == null) return null;
			alpha = c.getAlpha();
		}
		return new Color(0, 0, 0, alpha);
	}

	/** The picture where it goes, turned with the box (its image turned, on a grid of its own resolution), within clip */
	private static Drawing.Picture turned(Effects.Picture p, AffineTransform turn, Rectangle2D clip) {
		Rectangle2D.Double pl = p.getPlace();
		Shape cut = clip == null ? null : turn == null ? clip : turn.createTransformedShape(clip);
		if (turn == null || turn.isIdentity()) return new Drawing.Picture(p.getImage(), pl, cut);
		Rectangle2D into = turn.createTransformedShape(pl).getBounds2D();
		double ppt = p.getPixelsPerPoint();
		int w = Math.max(1, (int) Math.ceil(into.getWidth() * ppt)), h = Math.max(1, (int) Math.ceil(into.getHeight() * ppt));
		java.awt.image.BufferedImage im = new java.awt.image.BufferedImage(w, h, java.awt.image.BufferedImage.TYPE_INT_ARGB);
		java.awt.Graphics2D g = im.createGraphics();
		g.setRenderingHint(java.awt.RenderingHints.KEY_INTERPOLATION, java.awt.RenderingHints.VALUE_INTERPOLATION_BILINEAR);
		g.setRenderingHint(java.awt.RenderingHints.KEY_RENDERING, java.awt.RenderingHints.VALUE_RENDER_QUALITY);
		g.scale(ppt, ppt);
		g.translate(-into.getX(), -into.getY());
		g.transform(turn);
		g.drawImage(p.getImage(), new AffineTransform(pl.width / p.getImage().getWidth(), 0, 0, pl.height / p.getImage().getHeight(), pl.x, pl.y),
				null);
		g.dispose();
		return new Drawing.Picture(im, new Rectangle2D.Double(into.getX(), into.getY(), w / ppt, h / ppt), cut);
	}

	/**
	 * A stretch of one run's glyphs on a line, for its decorations: from its first glyph to its last, the line's trailing
	 * spaces left out; its baseline (raised or lowered); the spans its underline takes (its words alone for
	 * {@code w:u="words"})
	 */
	static final class Stretch {
		WordArtText.Run run;
		Font font;
		double baseline, x0, x1;
		final StringBuilder chars = new StringBuilder();
		final List<double[]> underlined = new ArrayList<double[]>();
	}

	/** The stretches of the runs that have a decoration, line by line (a run Word draws nothing of has none) */
	static List<Stretch> stretches(Layout layout) {
		List<Stretch> out = new ArrayList<Stretch>();
		for (Line l : layout.lines) {
			int last = -1;
			for (int i = l.glyphs.size() - 1; i >= 0; i--) {
				Glyph g = l.glyphs.get(i);
				if (!g.space && !g.lineBreak) {
					last = i;
					break;
				}
			}
			Stretch cur = null;
			boolean inWord = false;
			for (int i = 0; i <= last; i++) {
				Glyph g = l.glyphs.get(i);
				if (g.lineBreak || !g.run.hasDecoration() || g.run.paint(false) == WordArtText.Paint.NONE) {
					cur = null;
					continue;
				}
				double x = l.x0 + g.x;
				if (cur == null || cur.run != g.run || cur.font != g.font) {
					cur = new Stretch();
					cur.run = g.run;
					cur.font = g.font;
					cur.baseline = baseline(l, g);
					cur.x0 = x;
					out.add(cur);
					inWord = false;
				}
				cur.x1 = x + g.advance;
				if (g.chars != null) cur.chars.append(g.chars);
				boolean words = g.run.getUnderline() == org.docx4j.wml.UnderlineEnumeration.WORDS;
				if (!words) {
					if (cur.underlined.isEmpty()) cur.underlined.add(new double[] { cur.x0, cur.x1 });
					else cur.underlined.get(0)[1] = cur.x1;
				} else if (g.space) {
					inWord = false;
				} else {
					if (!inWord) cur.underlined.add(new double[] { x, x + g.advance });
					else cur.underlined.get(cur.underlined.size() - 1)[1] = x + g.advance;
					inWord = true;
				}
			}
		}
		for (Stretch st : out) if (st.chars.length() == 0) st.chars.append('x');
		return out;
	}

	/** The colour Word draws a run's text in: its solid fill's, else {@code w:color} */
	private static org.docx4j.model.color.ResolvedColor textColour(WordArtText.Run r, ThemeColors theme) {
		Object f = r.getFill();
		if (f instanceof CTSolidColorFillProperties) {
			org.docx4j.model.color.ResolvedColor c = DrawingMLColors.resolve(f, theme);
			if (c != null) return c;
		}
		return r.getColour();
	}

	/** An underline drawn twice as thick (the FO output's heavy kinds) */
	private static boolean heavy(org.docx4j.wml.UnderlineEnumeration u) {
		switch (u) {
			case THICK: case DOTTED_HEAVY: case DASHED_HEAVY: case DASH_LONG_HEAVY: case DASH_DOT_HEAVY: case DASH_DOT_DOT_HEAVY:
			case WAVY_HEAVY:
				return true;
			default:
				return false;
		}
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
