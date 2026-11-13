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

import java.awt.BasicStroke;
import java.awt.Font;
import java.awt.Shape;
import java.awt.font.FontRenderContext;
import java.awt.font.GlyphVector;
import java.awt.geom.AffineTransform;
import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;
import java.io.InputStream;
import java.net.URI;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.BiFunction;
import java.util.function.Function;

import org.docx4j.model.color.ThemeColors;
import org.docx4j.model.shapes.WordArtText;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.w3c.dom.Document;

/**
 * The text-effect painter the FO renderer fork calls for a body run with a {@code docx4j:text-effect} id (CR-083 phase
 * 5, D20; fop/CR-043): once per group (one run, one font, one line), under its text and over it, with the group's
 * glyphs (the font file's ids), their positions as the fork set them, the font, the frame and the decorations; it
 * returns an SVG document and where to draw it, and whether it drew the glyphs (the fork then draws the text
 * invisibly) and the decorations. The run's effects are found by the id in the conversion's table, as WordArt runs
 * ({@link WordArtText#bodyRun}); the drawing is {@link WordArtRenderer#drawBody}'s.
 *
 * <p>Its types are the JDK's only (the fork's {@code FOUserAgent.setTextEffectPainter} takes a {@code BiFunction} of
 * maps, set by reflection, so that docx4j still loads against Apache FOP); the keys are the fork's {@code
 * org.apache.fop.docx4j.TextEffectKeys}' strings, copied.</p>
 *
 * <p>A face whose glyph ids aren't the file's (a single-byte or Type 1 face: no ids given), or whose file can't be
 * read, isn't painted: the fork draws its text as it would without effects, and that is reported once.</p>
 *
 * @since 17.3.7
 */
public final class TextEffectPainter implements BiFunction<String, Map<String, Object>, Map<String, Object>> {

	private static final Logger log = LoggerFactory.getLogger(TextEffectPainter.class);

	// org.apache.fop.docx4j.TextEffectKeys (fop/CR-043, 2.12-docx4j.2), copied
	public static final String PHASE = "phase";
	public static final String PHASE_UNDER = "under";
	public static final String PHASE_OVER = "over";
	public static final String GLYPHS = "glyphs";
	public static final String X = "x";
	public static final String Y = "y";
	public static final String TEXT = "text";
	public static final String FONT_URI = "font-uri";
	public static final String FONT_NAME = "font-name";
	public static final String FONT_SIZE = "font-size";
	public static final String BOLD_SIMULATED = "bold-simulated";
	public static final String ITALIC_SHEAR = "italic-shear";
	public static final String RTL = "rtl";
	public static final String FRAME = "frame";
	public static final String TRAILING_SPACE = "trailing-space";
	public static final String TEXT_SPANS = "text-spans";
	public static final String UNDERLINE = "underline";
	public static final String OVERLINE = "overline";
	public static final String LINE_THROUGH = "line-through";
	public static final String SVG = "svg";
	public static final String RECT = "rect";
	public static final String GLYPHS_DRAWN = "glyphs-drawn";
	public static final String DECORATIONS_DRAWN = "decorations-drawn";

	/** A run's effects as the painter draws them: its paint and effects, and the colours they resolve through */
	public static final class Entry {
		final WordArtText.Run run;
		final ThemeColors theme;

		public Entry(WordArtText.Run run, ThemeColors theme) {
			this.run = run;
			this.theme = theme;
		}

		public WordArtText.Run getRun() {
			return run;
		}
	}

	private static final FontRenderContext FRC = new FontRenderContext(null, true, true);

	/** Faces read from their files, by file and name; a face that couldn't be read is held as null's stand-in */
	private static final Map<String, Font> FACES = new ConcurrentHashMap<String, Font>();
	private static final Font UNREADABLE = new Font(Font.SERIF, Font.PLAIN, 1);

	/** What has been reported at warn: each kind once a JVM */
	private static final Set<String> REPORTED = ConcurrentHashMap.newKeySet();

	private final Function<String, Entry> table;

	/** @param table the conversion's runs by their ids */
	public TextEffectPainter(Function<String, Entry> table) {
		this.table = table;
	}

	@Override
	public Map<String, Object> apply(String id, Map<String, Object> m) {
		Entry e = id == null ? null : table.apply(id);
		if (e == null || e.run == null) return null;
		boolean over = PHASE_OVER.equals(m.get(PHASE));
		if (over && (e.run.getEffects() == null || e.run.getEffects().getInnerShdw() == null)) return null;
		double size = number(m.get(FONT_SIZE), e.run.getSize());
		Shape glyphs = glyphs(m, size);
		if (glyphs == null) {
			report("a run's text effects not drawn: its face's glyphs couldn't be read (" + m.get(FONT_NAME) + ")");
			return null;
		}
		Rectangle2D frame = m.get(FRAME) instanceof Rectangle2D ? (Rectangle2D) m.get(FRAME) : glyphs.getBounds2D();
		boolean rtl = Boolean.TRUE.equals(m.get(RTL));
		// Word's gradient frame: the run's part with the space suppressed at the line's break and the last letter
		// spacing (phase 5 findings section 6.1: wb80-02, -11)
		double extra = number(m.get(TRAILING_SPACE), 0) + Math.max(0, e.run.getSpacing());
		Rectangle2D gradientFrame = extra > 0 ? new Rectangle2D.Double(rtl ? frame.getX() - extra : frame.getX(), frame.getY(),
				frame.getWidth() + extra, frame.getHeight()) : frame;
		List<Shape> decorations = decorations(m);
		WordArtRenderer.Body b = WordArtRenderer.drawBody(e.run, glyphs, thickening(m, glyphs, size), size, frame, gradientFrame,
				decorations, rtl, e.theme, over);
		for (String p : b.getProblems()) report(p + ": a run's text effect");
		Map<String, Object> out = new HashMap<String, Object>();
		Document svg = b.toSvg();
		if (svg != null) {
			out.put(SVG, svg);
			out.put(RECT, b.getBounds());
		}
		if (!over) {
			out.put(GLYPHS_DRAWN, b.isGlyphsDrawn());
			out.put(DECORATIONS_DRAWN, b.isDecorationsDrawn());
		}
		return out;
	}

	/**
	 * The group's glyphs as placed: the face read from its file, each glyph at the fork's origin (points from the
	 * group's start on its baseline, y down), a simulated italic sheared about the baseline, as the fork draws them
	 * (fop/CR-021; a simulated bold's thickening is {@link #thickening}); null where they can't be had
	 */
	static Shape glyphs(Map<String, Object> m, double size) {
		int[] ids = m.get(GLYPHS) instanceof int[] ? (int[]) m.get(GLYPHS) : null;
		double[] x = m.get(X) instanceof double[] ? (double[]) m.get(X) : null;
		double[] y = m.get(Y) instanceof double[] ? (double[]) m.get(Y) : null;
		if (ids == null || x == null || y == null || x.length < ids.length || y.length < ids.length) return null;
		Font face = face(m.get(FONT_URI) instanceof URI ? (URI) m.get(FONT_URI) : null, (String) m.get(FONT_NAME));
		if (face == null) return null;
		GlyphVector gv = face.deriveFont((float) size).createGlyphVector(FRC, ids);
		for (int i = 0; i < ids.length; i++) gv.setGlyphPosition(i, new Point2D.Double(x[i], y[i]));
		Shape s = gv.getOutline();
		double shear = number(m.get(ITALIC_SHEAR), 0);
		if (shear != 0) s = new AffineTransform(1, 0, -shear, 1, 0, 0).createTransformedShape(s);
		return s;
	}

	/** A simulated bold's thickening of the glyphs: their stroke at 1/35 em, mitred as the renderer strokes them; null
	 *  where the bold isn't simulated */
	static Shape thickening(Map<String, Object> m, Shape glyphs, double size) {
		if (!Boolean.TRUE.equals(m.get(BOLD_SIMULATED))) return null;
		return new BasicStroke((float) (size / 35), BasicStroke.CAP_BUTT, BasicStroke.JOIN_MITER).createStrokedShape(glyphs);
	}

	/** The face in a file (in a collection, the one of its name); null where it can't be read */
	static Font face(URI uri, String name) {
		if (uri == null) return null;
		String key = uri + "#" + name;
		Font f = FACES.computeIfAbsent(key, k -> {
			try (InputStream in = uri.toURL().openStream()) {
				Font[] all = Font.createFonts(in);
				if (all.length == 1 || name == null) return all[0];
				for (Font c : all) {
					if (name.equals(c.getFontName()) || name.equals(c.getPSName()) || name.equals(c.getFamily())) return c;
				}
				return all[0];
			} catch (Exception ex) {
				log.debug("Couldn't read " + uri + ": " + ex.getMessage());
				return UNREADABLE;
			}
		});
		return f == UNREADABLE ? null : f;
	}

	/** The underline's and strikes' rectangles over each of the group's text areas, where FOP would draw them */
	static List<Shape> decorations(Map<String, Object> m) {
		List<Shape> out = new ArrayList<Shape>();
		double[][] spans = m.get(TEXT_SPANS) instanceof double[][] ? (double[][]) m.get(TEXT_SPANS) : null;
		if (spans == null) return out;
		for (String k : new String[] { UNDERLINE, OVERLINE, LINE_THROUGH }) {
			if (!(m.get(k) instanceof double[])) continue;
			double[] line = (double[]) m.get(k);
			if (line.length < 2 || !(line[1] > 0)) continue;
			for (double[] sp : spans) {
				if (sp.length >= 2 && sp[1] > 0) out.add(new Rectangle2D.Double(sp[0], line[0] - line[1] / 2, sp[1], line[1]));
			}
		}
		return out;
	}

	private static double number(Object o, double otherwise) {
		return o instanceof Number ? ((Number) o).doubleValue() : otherwise;
	}

	private static void report(String what) {
		if (REPORTED.add(what)) {
			log.warn(WordArtText.BODY_EFFECT + ": " + what + " (reported once)");
		} else if (log.isDebugEnabled()) {
			log.debug(WordArtText.BODY_EFFECT + ": " + what);
		}
	}
}
