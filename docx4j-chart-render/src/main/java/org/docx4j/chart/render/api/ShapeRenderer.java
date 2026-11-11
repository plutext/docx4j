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

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Shape;
import java.awt.Stroke;
import java.awt.geom.AffineTransform;
import java.awt.geom.Area;
import java.awt.geom.Ellipse2D;
import java.awt.geom.FlatteningPathIterator;
import java.awt.geom.Path2D;
import java.awt.geom.PathIterator;
import java.awt.geom.Rectangle2D;
import java.awt.image.BufferedImage;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.docx4j.dml.CTBlipFillProperties;
import org.docx4j.dml.CTEffectList;
import org.docx4j.dml.CTGradientFillProperties;
import org.docx4j.dml.CTLineEndProperties;
import org.docx4j.dml.CTLineProperties;
import org.docx4j.dml.CTPatternFillProperties;
import org.docx4j.dml.CTRelativeRect;
import org.docx4j.dml.CTShapeProperties;
import org.docx4j.dml.CTShapeStyle;
import org.docx4j.dml.CTSolidColorFillProperties;
import org.docx4j.dml.CTGroupShapeProperties;
import org.docx4j.dml.CTGroupTransform2D;
import org.docx4j.dml.CTPresetGeometry2D;
import org.docx4j.dml.CTTransform2D;
import org.docx4j.dml.STShapeType;
import org.docx4j.dml.picture.Pic;
import org.docx4j.com.microsoft.schemas.office.word.x2010.wordprocessingCanvas.CTWordprocessingCanvas;
import org.docx4j.com.microsoft.schemas.office.word.x2010.wordprocessingGroup.CTWordprocessingGroup;
import org.docx4j.com.microsoft.schemas.office.word.x2010.wordprocessingShape.CTWordprocessingShape;
import org.docx4j.dml.STPathFillMode;
import org.docx4j.model.color.DrawingMLColors;
import org.docx4j.model.color.ResolvedColor;
import org.docx4j.model.color.StyleFill;
import org.docx4j.model.color.ThemeColors;
import org.docx4j.model.shapes.ShapeGeometry;
import org.docx4j.model.shapes.ShapeOutline;
import org.docx4j.model.shapes.ShapeOutlines;
import org.w3c.dom.Document;

/**
 * A DrawingML shape ({@code wps:wsp}'s {@code wps:spPr} and {@code wps:style}) drawn as Word draws it in a PDF
 * (CR-080 phase 2): its geometry ({@link ShapeGeometry}), its fill and its line, the shape's own where it states them
 * and its style's references through the theme otherwise.
 * <ul>
 * <li>Fills: solid, with alpha; gradients; the preset patterns; pictures, stretched or tiled. A path's fill mode
 * lightens or darkens the fill as Word does: {@code lighten} 40% toward white, {@code lightenLess} 20%,
 * {@code darken} to 60%, {@code darkenLess} to 80.7%, measured on 4472C4.</li>
 * <li>Lines: Word's width (0 is a 0.14pt hairline), dashes in multiples of the width (with a round cap each dash a
 * width shorter and each gap a width longer), caps (flat where none is stated), joins (round where none is stated;
 * {@code a:miter}'s limit isn't used: Word's is 10), compound lines and inside lines as filled bands, and arrowheads,
 * their sizes 2, 3 and 5 times the larger of the width and 2pt.</li>
 * <li>The shape's own turn and flips ({@code a:xfrm}), about its box's centre.</li>
 * </ul>
 * Effects are CR-081's. A geometry that can't be drawn is its box, with the code its {@link ShapeOutlines} gives.
 *
 * @since 17.3.7
 */
public final class ShapeRenderer {

	/** A line of width 0, as Word draws it, in points */
	public static final double HAIRLINE = 0.14;

	/** Gives the picture a picture fill names by its relationship, or null where there is none */
	public interface Pictures {
		BufferedImage picture(String relationshipId);
	}

	/** A shape drawn: its drawing, the box it paints (from the shape's top left, in points), its geometry. */
	public static final class Drawn {
		private final Drawing drawing;
		private final Rectangle2D.Double bounds;
		private final ShapeOutlines outlines;
		private final List<String> problems;
		private final List<Placed> children;

		Drawn(Drawing drawing, Rectangle2D.Double bounds, ShapeOutlines outlines, List<String> problems) {
			this(drawing, bounds, outlines, problems, Collections.<Placed>emptyList());
		}

		Drawn(Drawing drawing, Rectangle2D.Double bounds, ShapeOutlines outlines, List<String> problems, List<Placed> children) {
			this.drawing = drawing;
			this.bounds = bounds;
			this.outlines = outlines;
			this.problems = Collections.unmodifiableList(problems);
			this.children = Collections.unmodifiableList(children);
		}

		/** A group's or a canvas's shapes and pictures where they are drawn, in document order; none for a shape */
		public List<Placed> getChildren() {
			return children;
		}

		/** What is painted, in the shape's frame: points from its box's top left, y down */
		public Drawing getDrawing() {
			return drawing;
		}

		/** All it paints, its line and arrowheads included, in the shape's frame; it can reach past the box */
		public Rectangle2D.Double getBounds() {
			return bounds;
		}

		/** The geometry, its text rectangle included; a group's or a canvas's is its box */
		public ShapeOutlines getOutlines() {
			return outlines;
		}

		/** The gaps (stable codes, CR-080 D9): the geometry's, and a picture fill whose picture can't be had */
		public List<String> getProblems() {
			return problems;
		}

		/** Nothing is painted */
		public boolean isEmpty() {
			return drawing.getItems().isEmpty();
		}

		/** As SVG, its canvas {@link #getBounds()}, so that (0, 0) is the box's top left */
		public Document toSvg() {
			return SvgWriter.toSvg(drawing, bounds);
		}
	}

	/**
	 * A shape or picture in a group or a canvas, where it is drawn: its box in the drawing's frame (points from its top
	 * left, y down), before its turn and flips, which are about the box's centre, flipped then turned, as a shape's own
	 * are. They are the group's composed with its own, down the tree, as Word draws them (CR-080 phase 0, fp91): a
	 * child's box is mapped from its group's child space ({@code a:chOff}, {@code a:chExt}) to the group's box, its
	 * centre then turned and flipped with the group, and its turns added, or taken from the group's under one flip. A
	 * box is scaled, its drawing not: Word keeps a child's line width and its right angles under a group that halves
	 * its width.
	 */
	public static final class Placed {
		private final Object child;
		private final Rectangle2D.Double box;
		private final double rotation;
		private final boolean flipH, flipV;
		private final ShapeOutlines outlines;

		Placed(Object child, Rectangle2D.Double box, double rotation, boolean flipH, boolean flipV, ShapeOutlines outlines) {
			this.child = child;
			this.box = box;
			this.rotation = rotation;
			this.flipH = flipH;
			this.flipV = flipV;
			this.outlines = outlines;
		}

		/** The {@link CTWordprocessingShape} or {@link Pic} */
		public Object getChild() {
			return child;
		}

		/** Its box in the frame, before its turn and flips */
		public Rectangle2D.Double getBox() {
			return box;
		}

		/** Its turn, clockwise, in degrees */
		public double getRotation() {
			return rotation;
		}

		public boolean isFlipH() {
			return flipH;
		}

		public boolean isFlipV() {
			return flipV;
		}

		/** Its geometry at its box's size, its text rectangle from the box's top left */
		public ShapeOutlines getOutlines() {
			return outlines;
		}
	}

	/** A picture fill whose picture couldn't be had: drawn as no fill */
	public static final String PICTURE_MISSING = "render.shape.picture_missing";

	/** A group's or a canvas's child that isn't drawn: a graphic frame (a chart, a table), ink (contentPart) */
	public static final String CHILD_NOT_DRAWN = "render.shape.child_not_drawn";

	private ShapeRenderer() {}

	/**
	 * The shape drawn at its size.
	 *
	 * @param spPr the shape's properties
	 * @param style the shape's style, or null
	 * @param theme its colours and format scheme, the document's
	 * @param cx its width in EMU
	 * @param cy its height in EMU
	 * @param pictures gives a picture fill's picture, or null where there are none
	 */
	public static Drawn draw(CTShapeProperties spPr, CTShapeStyle style, ThemeColors theme, long cx, long cy, Pictures pictures) {
		return draw(spPr, style, theme, cx, cy, pictures, false);
	}

	/**
	 * The shape drawn at its size, with its effects (CR-081 phase 3).
	 *
	 * @param text the shape shows text: Word's text casts the shape's effects too, which docx4j doesn't draw, so where
	 *        they would show it is reported ({@link #TEXT_EFFECTS})
	 * @since 17.3.7 (CR-081 phase 3)
	 */
	public static Drawn draw(CTShapeProperties spPr, CTShapeStyle style, ThemeColors theme, long cx, long cy, Pictures pictures, boolean text) {
		ShapeOutlines outlines = ShapeGeometry.outlines(spPr, cx, cy);
		List<String> problems = new ArrayList<String>();
		if (outlines.getProblem() != null) problems.add(outlines.getProblem());
		double w = cx / 12700.0, h = cy / 12700.0;
		Drawing drawing = new Drawing(w, h);
		Rectangle2D bounds = paintShape(drawing, spPr, style, theme, outlines, w, h, turn(spPr == null ? null : spPr.getXfrm(), w, h), pictures,
				null, problems);
		bounds = effects(drawing, 0, spPr, style, theme, null, new Rectangle2D.Double(0, 0, w, h), text, bounds, problems);
		return new Drawn(drawing, bounds == null ? new Rectangle2D.Double(0, 0, w, h) : rect(bounds), outlines, problems);
	}

	/**
	 * A shape's fills, then its lines, path by path, as Word paints them, into the drawing: its geometry at its size (w,
	 * h, points), placed by the transform (its turn, and for a group's child where the group puts it); a:grpFill takes
	 * the group's fill, null where there is none. The area painted, or null.
	 */
	private static Rectangle2D paintShape(Drawing drawing, CTShapeProperties spPr, CTShapeStyle style, ThemeColors theme, ShapeOutlines outlines,
			double w, double h, AffineTransform turn, Pictures pictures, Fill groupFill, List<String> problems) {
		Fill fill = fill(spPr, style, theme, groupFill);
		String preset = spPr != null && spPr.getPrstGeom() != null && spPr.getPrstGeom().getPrst() != null ? spPr.getPrstGeom().getPrst().value() : null;
		Line line = line(spPr, style, theme, preset);
		Rectangle2D bounds = null;

		// the fills, then the lines, path by path, as Word paints them
		for (ShapeOutline o : outlines.getOutlines()) {
			if (fill == null || o.getFill() == STPathFillMode.NONE) continue;
			// Word fills by the even-odd rule (its PDF's f*), and a path of no area (line) not at all
			Path2D.Double evenOdd = new Path2D.Double(Path2D.WIND_EVEN_ODD);
			evenOdd.append(o.getPath(), false);
			Shape s = turn.createTransformedShape(evenOdd);
			if (new Area(s).isEmpty()) continue;
			Rectangle2D painted = fill.paint(drawing, s, o.getFill(), turn.createTransformedShape(new Rectangle2D.Double(0, 0, w, h)), pictures,
					problems);
			bounds = union(bounds, painted);
		}
		if (line != null) {
			List<ShapeOutline> stroked = new ArrayList<ShapeOutline>();
			for (ShapeOutline o : outlines.getOutlines()) if (o.isStroked()) stroked.add(o);
			for (int i = 0; i < stroked.size(); i++) {
				Path2D.Double p = stroked.get(i).getPath();
				bounds = union(bounds, line.paint(drawing, p, turn, i == 0, i == stroked.size() - 1));
			}
		}
		return bounds;
	}

	// ------------------------------------------------------------------ a picture's frame (CR-081 phase 2)

	/**
	 * A picture's frame ({@code pic:spPr}), as Word draws it round the picture (CR-081 phase 2): the outline its geometry
	 * gives ({@code a:prstGeom}, {@code a:custGeom}), which clips the picture and the fill behind it; the fill behind it
	 * (a {@code a:solidFill} or another fill, in the outline, under the picture: it shows through a transparent picture);
	 * and its line ({@code a:ln}), wholly outside the picture, where a shape's is centred on its outline. Word strokes it
	 * on the outline moved half the line's width outward, its corners mitred and its arcs offset (fs10-14: a
	 * round2DiagRect's 18pt corners stroked at 21.5pt, its square corners square), so the line's inner edge is the
	 * picture's; a compound line is rings from the outline outward (thickThin 3 : 1 : 1, fs10-07), their corners the
	 * line's join. An open path's line is centred on it, as a shape's.
	 */
	public static final class PictureFrame {
		private final Shape outline;
		private final boolean box;
		private final Drawing behind, line;
		private Drawing shadow;
		private Rectangle2D.Double shadowBounds;
		private final Rectangle2D.Double behindBounds, lineBounds;
		private final List<String> problems;

		PictureFrame(Shape outline, boolean box, Drawing behind, Rectangle2D behindBounds, Drawing line, Rectangle2D lineBounds, List<String> problems) {
			this.outline = outline;
			this.box = box;
			this.behind = behind;
			this.behindBounds = behindBounds == null ? null : rect(behindBounds);
			this.line = line;
			this.lineBounds = lineBounds == null ? null : rect(lineBounds);
			this.problems = Collections.unmodifiableList(problems);
		}

		/** The outline the picture and the fill behind it are clipped to, points from the box's top left, y down */
		public Shape getOutline() {
			return outline;
		}

		/** The outline is the picture's box: nothing of the picture is clipped */
		public boolean isBox() {
			return box;
		}

		/** The fill behind the picture, in its outline; null where there is none */
		public Drawing getBehind() {
			return behind;
		}

		/** What the fill behind paints; null where there is none */
		public Rectangle2D.Double getBehindBounds() {
			return behindBounds;
		}

		/** The line round the picture; null where there is none */
		public Drawing getLine() {
			return line;
		}

		/** What the line paints, past the box; null where there is none */
		public Rectangle2D.Double getLineBounds() {
			return lineBounds;
		}

		/** The geometry's gaps (render.shape.*: it is then the box) */
		public List<String> getProblems() {
			return problems;
		}

		/** Nothing is drawn round the picture, and nothing of it is clipped */
		public boolean isPlain() {
			return box && behind == null && line == null && shadow == null;
		}

		/** Its preset shadow ({@code a:prstShdw}: vector fills under it, phase 4); null where there is none */
		public Drawing getShadow() {
			return shadow;
		}

		/** What the preset shadow paints; null where there is none */
		public Rectangle2D.Double getShadowBounds() {
			return shadowBounds;
		}
	}

	/**
	 * A picture's frame at its size (CR-081 phase 2): its outline, the fill behind it and its line, from its
	 * {@code pic:spPr} (its own; a picture has no style).
	 *
	 * @param cx its width in EMU
	 * @param cy its height in EMU
	 */
	public static PictureFrame pictureFrame(CTShapeProperties spPr, ThemeColors theme, long cx, long cy) {
		double w = cx / 12700.0, h = cy / 12700.0;
		List<String> problems = new ArrayList<String>();
		Rectangle2D.Double box = new Rectangle2D.Double(0, 0, w, h);
		boolean geometry = spPr != null && (spPr.getPrstGeom() != null || spPr.getCustGeom() != null);
		ShapeOutlines outlines = geometry ? ShapeGeometry.outlines(spPr, cx, cy) : null;
		if (outlines != null && outlines.getProblem() != null) problems.add(outlines.getProblem());
		// the outline: the filled paths, each by the even-odd rule, as a shape's fill (a picture with no geometry, or none
		// that fills, is its box)
		Area outline = new Area();
		if (outlines != null) {
			for (ShapeOutline o : outlines.getOutlines()) {
				if (o.getFill() == STPathFillMode.NONE) continue;
				Path2D.Double evenOdd = new Path2D.Double(Path2D.WIND_EVEN_ODD);
				evenOdd.append(o.getPath(), false);
				outline.add(new Area(evenOdd));
			}
		}
		if (outline.isEmpty()) outline = new Area(box);
		Rectangle2D ob = outline.getBounds2D();
		boolean isBox = outline.isRectangular() && Math.abs(ob.getX()) < 1e-6 && Math.abs(ob.getY()) < 1e-6 && Math.abs(ob.getWidth() - w) < 1e-6
				&& Math.abs(ob.getHeight() - h) < 1e-6;
		Shape clip = isBox ? box : outline;

		// the fill behind: the fill in the outline
		Drawing behind = null;
		Rectangle2D behindBounds = null;
		Fill fill = spPr == null || spPr.getBlipFill() != null ? null : fill(spPr, null, theme, null);
		if (fill != null) {
			Drawing d = new Drawing(w, h);
			behindBounds = fill.paint(d, clip, null, box, null, problems);
			if (behindBounds != null && !d.getItems().isEmpty()) behind = d;
		}

		// the line, wholly outside
		Drawing lined = null;
		Rectangle2D lineBounds = null;
		String preset = spPr != null && spPr.getPrstGeom() != null && spPr.getPrstGeom().getPrst() != null ? spPr.getPrstGeom().getPrst().value() : null;
		Line line = line(spPr, null, theme, preset == null ? "rect" : preset);
		if (line != null) {
			Drawing d = new Drawing(w, h);
			List<Path2D.Double> stroked = new ArrayList<Path2D.Double>();
			if (outlines != null && !outlines.getOutlines().isEmpty()) {
				for (ShapeOutline o : outlines.getOutlines()) if (o.isStroked()) stroked.add(o.getPath());
			} else {
				stroked.add(new Path2D.Double(box));
			}
			AffineTransform none = new AffineTransform();
			for (int i = 0; i < stroked.size(); i++) {
				lineBounds = union(lineBounds, line.paintOutside(d, stroked.get(i), none, i == 0, i == stroked.size() - 1));
			}
			if (lineBounds != null && !d.getItems().isEmpty()) lined = d;
		}
		PictureFrame frame = new PictureFrame(clip, isBox, behind, behindBounds, lined, lineBounds, problems);
		// a preset shadow: the outline, with its line, moved and shaped as the preset says (phase 4)
		if (spPr != null && spPr.getEffectLst() != null && spPr.getEffectLst().getPrstShdw() != null) {
			Area cast = new Area(clip);
			if (lined != null) cast.add(new Area(outline(lined.getItems())));
			Rectangle2D drawnBox = lineBounds == null ? box : box.createUnion(lineBounds);
			Drawing d = new Drawing(w, h);
			Rectangle2D sb = null;
			for (Effects.Fill f : Effects.presetShadow(spPr.getEffectLst().getPrstShdw(), theme, cast, drawnBox)) {
				d.add(new Drawing.Area(f.getShape(), f.getColour(), null, 0, "shape.shadow"));
				sb = union(sb, f.getShape().getBounds2D());
			}
			if (sb != null) {
				frame.shadow = d;
				frame.shadowBounds = rect(sb);
			}
		}
		return frame;
	}

	/**
	 * A closed path moved d outward, its corners mitred and its arcs offset, as Word strokes a picture's line on it:
	 * the boundary of the path's inside grown by d. A rectangle stays one, from its bottom left as Word's re.
	 */
	static Path2D.Double outward(Path2D.Double path, double d) {
		Path2D.Double re = asRe(path);
		if (re != path) {
			// rectangles: each grown
			Path2D.Double out = new Path2D.Double(path.getWindingRule());
			double[] c = new double[6];
			List<double[]> pts = new ArrayList<double[]>();
			for (PathIterator it = re.getPathIterator(null); !it.isDone(); it.next()) {
				int t = it.currentSegment(c);
				if (t == PathIterator.SEG_MOVETO || t == PathIterator.SEG_LINETO) pts.add(new double[] { c[0], c[1] });
				if (t == PathIterator.SEG_CLOSE && pts.size() == 4) {
					double l = Math.min(pts.get(0)[0], pts.get(2)[0]) - d, r = Math.max(pts.get(0)[0], pts.get(2)[0]) + d;
					double top = Math.min(pts.get(0)[1], pts.get(2)[1]) - d, b = Math.max(pts.get(0)[1], pts.get(2)[1]) + d;
					out.moveTo(l, b);
					out.lineTo(r, b);
					out.lineTo(r, top);
					out.lineTo(l, top);
					out.closePath();
					pts.clear();
				}
			}
			return out;
		}
		Area grown = new Area(path);
		grown.add(new Area(new BasicStroke((float) (2 * d), BasicStroke.CAP_BUTT, BasicStroke.JOIN_MITER, 10f).createStrokedShape(path)));
		return new Path2D.Double(grown);
	}

	// ------------------------------------------------------------------ a shape's effects (CR-081 phase 3)

	/**
	 * A shape's text casts its effects in Word (fxr80-10, -11: its outer shadow, a picture of its own under the
	 * shape's fill); docx4j's text is the output's, not the drawing's, so it casts none. Reported where that shows:
	 * a shadow or glow under a shape with no opaque fill, a reflection under any (D9).
	 */
	public static final String TEXT_EFFECTS = "render.effect.shape_text";

	/** A 3-D drawing (a bevel, an extrusion, a turned camera) drawn flat (D9): its contour isn't drawn yet, or its 3-D
	 *  picture couldn't be made (CR-081 phase 5a draws the rest, {@link Scene3D}) */
	public static final String FLAT_3D = "render.effect.flat_3d";

	/** An effect list given as a DAG ({@code a:effectDag}), not drawn (CR-081 §9) */
	public static final String EFFECT_DAG = "render.effect.effect_dag";

	/** A group's own effects ({@code wpg:grpSpPr/a:effectLst}), not drawn: not measured (its shapes' are drawn) */
	public static final String GROUP_EFFECTS = "render.effect.group";

	/** Reports a group's own effects, which aren't drawn */
	private static void groupEffects(CTGroupShapeProperties gp, List<String> problems) {
		CTEffectList l = gp == null ? null : gp.getEffectLst();
		if (l != null && (l.getOuterShdw() != null || l.getInnerShdw() != null || l.getGlow() != null || l.getSoftEdge() != null
				|| l.getReflection() != null || l.getPrstShdw() != null) && !problems.contains(GROUP_EFFECTS)) {
			problems.add(GROUP_EFFECTS);
		}
	}

	/** The effects a shape draws, the theme its colours resolve in, and its 3-D scene and shape where it is 3-D */
	static final class EffectsOf {
		final CTEffectList list;
		final ThemeColors theme;
		final boolean flat3d;
		final org.docx4j.dml.CTScene3D scene;
		final org.docx4j.dml.CTShape3D shape3d;

		EffectsOf(CTEffectList list, ThemeColors theme, org.docx4j.dml.CTScene3D scene, org.docx4j.dml.CTShape3D shape3d) {
			this.list = list;
			this.theme = theme;
			this.scene = scene;
			this.shape3d = shape3d;
			this.flat3d = is3d(scene, shape3d);
		}
	}

	/**
	 * The shape's effects: its spPr's a:effectLst where it states one (an empty one is none, over its style's:
	 * fxr81-05, fxr82-05), else its style's a:effectRef, the theme's effect style idx (1-based; 0 none), its colours
	 * in the reference's colour for phClr (fxr81-04, fxr83-02: a custom theme's glow in its accent); null where there
	 * are none
	 */
	static EffectsOf effectsOf(CTShapeProperties sp, CTShapeStyle style, ThemeColors theme, List<String> problems) {
		org.docx4j.dml.CTScene3D scene = sp == null ? null : sp.getScene3D();
		org.docx4j.dml.CTShape3D shape3d = sp == null ? null : sp.getSp3D();
		// the style's effect style, through the theme: its effects, and its 3-D where the spPr states none
		org.docx4j.dml.CTEffectStyleItem item = null;
		ThemeColors itemTheme = theme;
		if (style != null && style.getEffectRef() != null && theme != null && theme.getFormatScheme() != null
				&& theme.getFormatScheme().getEffectStyleLst() != null) {
			long idx = style.getEffectRef().getIdx();
			List<org.docx4j.dml.CTEffectStyleItem> items = theme.getFormatScheme().getEffectStyleLst().getEffectStyle();
			if (idx > 0 && idx <= items.size()) {
				item = items.get((int) idx - 1);
				itemTheme = theme.withPlaceholder(DrawingMLColors.resolve(style.getEffectRef(), theme));
			}
		}
		if (item != null) {
			if (scene == null) scene = item.getScene3D();
			if (shape3d == null) shape3d = item.getSp3D();
		}
		boolean three = is3d(scene, shape3d);
		ThemeColors threeTheme = sp != null && (sp.getScene3D() != null || sp.getSp3D() != null) ? theme : itemTheme;
		if (sp != null && sp.getEffectLst() != null) return new EffectsOf(sp.getEffectLst(), theme, scene, shape3d);
		if (sp != null && sp.getEffectDag() != null) {
			if (problems != null && !problems.contains(EFFECT_DAG)) problems.add(EFFECT_DAG);
			return three ? new EffectsOf(null, threeTheme, scene, shape3d) : null;
		}
		if (item != null) {
			if (item.getEffectDag() != null && item.getEffectLst() == null && problems != null && !problems.contains(EFFECT_DAG)) {
				problems.add(EFFECT_DAG);
			}
			return new EffectsOf(item.getEffectLst(), itemTheme, scene, shape3d);
		}
		return three ? new EffectsOf(null, threeTheme, scene, shape3d) : null;
	}

	/** Whether a scene and shape are 3-D as Word draws them: a bevel, an extrusion or a contour, or a camera off the front */
	public static boolean is3d(org.docx4j.dml.CTScene3D scene, org.docx4j.dml.CTShape3D shape) {
		if (shape != null && (shape.getBevelT() != null || shape.getBevelB() != null || shape.getExtrusionH() > 0 || shape.getContourW() > 0)) return true;
		if (scene != null && scene.getCamera() != null) {
			org.docx4j.dml.CTCamera c = scene.getCamera();
			if (c.getPrst() != null && c.getPrst() != org.docx4j.dml.STPresetCameraType.ORTHOGRAPHIC_FRONT) return true;
			if (c.getRot() != null && (c.getRot().getLat() != 0 || c.getRot().getLon() != 0 || c.getRot().getRev() != 0)) return true;
		}
		return false;
	}

	/**
	 * The effects of the shape whose items are the drawing's from {@code from} on (its fills, then its lines and
	 * arrowheads, as {@link #paintShape} adds them), drawn round them as Word draws a shape's (CR-081 phase 3): its
	 * reflection, outer shadow and glow under it, its inner shadow over its fill and under its line (as a picture's,
	 * phase 2), or the shape with its soft edge in its place (fxr80-07). Each follows the shape's silhouette, its fill
	 * and line as painted (fxr80-02 to -04: an ellipse, a star, an arrow; fxr80-09: an unfilled shape's shadow is its
	 * line's); a shadow's algn point is on the box with the line. The area painted, effects included.
	 *
	 * @param box the shape's box in the drawing's frame, before its turn (a turned shape's effects are cast unturned)
	 */
	static Rectangle2D effects(Drawing drawing, int from, CTShapeProperties sp, CTShapeStyle style, ThemeColors theme, Fill groupFill,
			Rectangle2D.Double box, boolean text, Rectangle2D bounds, List<String> problems) {
		EffectsOf of = effectsOf(sp, style, theme, problems);
		if (of == null) return bounds;
		// a 3-D shape: its drawing as painted is the face of one picture, which takes its place (CR-081 phase 5a); its
		// effects are cast by that picture, within its bounds
		Rectangle2D.Double threeLocal = null;
		if (of.flat3d && from < drawing.getItems().size()) {
			List<Object> painted = new ArrayList<Object>(drawing.getItems().subList(from, drawing.getItems().size()));
			Drawing face = new Drawing(drawing.getWidth(), drawing.getHeight());
			face.getItems().addAll(painted);
			final double fx0 = box.x, fy0 = box.y;
			java.awt.Shape faceOutline = java.awt.geom.AffineTransform.getTranslateInstance(-fx0, -fy0).createTransformedShape(outline(painted));
			double ppt = Effects.BASE_PIXELS_PER_POINT, cap = maxPixelsPerPoint();
			if (cap > 0) ppt = Math.min(ppt, cap);
			Scene3D.Picture three = Scene3D.draw(g -> {
				g.translate(-fx0, -fy0);
				face.paint(g);
				g.translate(fx0, fy0);
			}, faceOutline, box.width, box.height, of.scene, of.shape3d, of.theme, ppt, problems);
			if (three != null) {
				Rectangle2D.Double pl = three.getPlace();
				Drawing.Picture dp = new Drawing.Picture(three.getImage(), new Rectangle2D.Double(pl.x + fx0, pl.y + fy0, pl.width, pl.height), null);
				drawing.getItems().subList(from, drawing.getItems().size()).clear();
				drawing.getItems().add(dp);
				bounds = union(bounds, dp.getInto());
				threeLocal = pl;
			} else if (!problems.contains(FLAT_3D)) {
				problems.add(FLAT_3D);
			}
		}
		CTEffectList list = of.list;
		if (list == null || list.getOuterShdw() == null && list.getInnerShdw() == null && (list.getGlow() == null || list.getGlow().getRad() <= 0)
				&& (list.getSoftEdge() == null || list.getSoftEdge().getRad() <= 0) && list.getReflection() == null && list.getPrstShdw() == null) {
			return bounds;
		}
		if (text && !problems.contains(TEXT_EFFECTS)) {
			Fill f = fill(sp, style, theme, groupFill);
			boolean covered = opaque(f);
			if (list.getReflection() != null || !covered && (list.getOuterShdw() != null || list.getGlow() != null)) problems.add(TEXT_EFFECTS);
		}
		List<Object> items = drawing.getItems();
		if (from >= items.size()) return bounds; // nothing painted, nothing cast (fxr80-10: Word's is its text's)
		// the shape's items: its fills (and picture fills) the body, its lines and arrowheads over them
		List<Object> mine = new ArrayList<Object>(items.subList(from, items.size()));
		Drawing body = new Drawing(drawing.getWidth(), drawing.getHeight()), line = new Drawing(drawing.getWidth(), drawing.getHeight());
		Rectangle2D lineBounds = null;
		for (Object o : mine) {
			boolean isLine = o instanceof Drawing.Area && ("shape.line".equals(((Drawing.Area) o).getRole()) || "shape.arrowhead".equals(((Drawing.Area) o).getRole()));
			(isLine ? line : body).getItems().add(o);
			if (isLine) lineBounds = union(lineBounds, painted((Drawing.Area) o));
		}
		final double x = box.x, y = box.y;
		Effects.Painter bodyPainter = g -> {
			g.translate(-x, -y);
			body.paint(g);
			g.translate(x, y);
		};
		Effects.Painter linePainter = line.getItems().isEmpty() ? null : g -> {
			g.translate(-x, -y);
			line.paint(g);
			g.translate(x, y);
		};
		Rectangle2D localLine = lineBounds == null ? null
				: new Rectangle2D.Double(lineBounds.getX() - x, lineBounds.getY() - y, lineBounds.getWidth(), lineBounds.getHeight());
		if (threeLocal != null) {     // the effects' extent: the 3-D picture, which may reach past the box
			linePainter = g -> {
			};
			localLine = threeLocal;
		}
		// a preset shadow: vector fills of the shape's outline, its line stroked with it (phase 4)
		final Drawing preset = new Drawing(drawing.getWidth(), drawing.getHeight());
		Rectangle2D presetBounds = null;
		if (list.getPrstShdw() != null) {
			Rectangle2D drawn = lineBounds == null ? box : box.createUnion(lineBounds);
			for (Effects.Fill f : Effects.presetShadow(list.getPrstShdw(), of.theme, outline(mine), drawn)) {
				preset.add(new Drawing.Area(f.getShape(), f.getColour(), null, 0, "shape.shadow"));
				presetBounds = union(presetBounds, f.getShape().getBounds2D());
			}
		}
		Effects.Painter presetPainter = preset.getItems().isEmpty() ? null : g -> {
			g.translate(-x, -y);
			preset.paint(g);
			g.translate(x, y);
		};
		Rectangle2D localPreset = presetBounds == null ? null
				: new Rectangle2D.Double(presetBounds.getX() - x, presetBounds.getY() - y, presetBounds.getWidth(), presetBounds.getHeight());
		Effects.Result r = Effects.draw(list, of.theme, box.width, box.height, bodyPainter, linePainter, localLine, presetPainter, localPreset,
				maxPixelsPerPoint());
		for (String code : r.getProblems()) if (!problems.contains(code)) problems.add(code);
		if (presetBounds != null) bounds = union(bounds, presetBounds);
		if (r.isEmpty() && preset.getItems().isEmpty()) return bounds;
		List<Object> reflected = new ArrayList<Object>(), under = new ArrayList<Object>(), over = new ArrayList<Object>(),
				replace = new ArrayList<Object>();
		for (Effects.Picture p : r.getPictures()) {
			Rectangle2D.Double pl = p.getPlace();
			Drawing.Picture dp = new Drawing.Picture(p.getImage(), new Rectangle2D.Double(pl.x + x, pl.y + y, pl.width, pl.height), null);
			(p.getLayer() == Effects.Layer.UNDER ? "reflection".equals(p.getEffect()) ? reflected : under
					: p.getLayer() == Effects.Layer.OVER ? over : replace).add(dp);
			bounds = union(bounds, dp.getInto());
		}
		// Word's order: the reflection, the preset shadow, then the outer shadow and glow (fxp4-01 to -03)
		List<Object> out = new ArrayList<Object>(reflected);
		out.addAll(preset.getItems());
		out.addAll(under);
		if (!replace.isEmpty()) {
			out.addAll(replace);
		} else {
			out.addAll(body.getItems());
			out.addAll(over);
			out.addAll(line.getItems());
		}
		items.subList(from, items.size()).clear();
		items.addAll(out);
		return bounds;
	}

	/** The outline of what a drawing paints: its fills' shapes and its strokes' outlines, one area */
	public static java.awt.Shape outline(Drawing drawing) {
		return outline(drawing.getItems());
	}

	/** The outline of what these items paint: their fills' shapes and their strokes' outlines, one area */
	static java.awt.Shape outline(List<Object> items) {
		Area a = new Area();
		for (Object o : items) {
			if (o instanceof Drawing.Area) {
				Drawing.Area d = (Drawing.Area) o;
				if (d.getFill() != null || d.getGradient() != null || d.getPattern() != null) a.add(new Area(d.getShape()));
				if (d.getLine() != null) {
					java.awt.Stroke st = d.getStroke() != null ? d.getStroke() : new BasicStroke((float) Math.max(d.getLineWidth(), HAIRLINE));
					a.add(new Area(st.createStrokedShape(d.getShape())));
				}
			} else if (o instanceof Drawing.Picture) {
				Drawing.Picture p = (Drawing.Picture) o;
				Area in = new Area(p.getInto());
				if (p.getClip() != null) in.intersect(new Area(p.getClip()));
				a.add(in);
			}
		}
		return a;
	}

	/** What an area paints: its stroke's outline, or its shape */
	private static Rectangle2D painted(Drawing.Area a) {
		if (a.getLine() != null && a.getStroke() != null) return a.getStroke().createStrokedShape(a.getShape()).getBounds2D();
		if (a.getLine() != null && a.getLineWidth() > 0) return new BasicStroke((float) a.getLineWidth()).createStrokedShape(a.getShape()).getBounds2D();
		return a.getShape().getBounds2D();
	}

	/** Whether a fill hides what is under it: a solid colour, a gradient or a pattern with no alpha (14372's gradient) */
	private static boolean opaque(Fill f) {
		if (f == null) return false;
		if (f.fill instanceof CTSolidColorFillProperties) {
			Color c = Fills.awt(DrawingMLColors.resolve(f.fill, f.fills.getTheme()));
			return c != null && c.getAlpha() >= 255;
		}
		if (f.fill instanceof CTGradientFillProperties) {
			Drawing.Gradient g = f.fills.gradient((CTGradientFillProperties) f.fill);
			if (g == null) return false;
			for (Color c : g.getColours()) if (c == null || c.getAlpha() < 255) return false;
			return true;
		}
		if (f.fill instanceof CTPatternFillProperties) {
			CTShapeProperties holder = new CTShapeProperties();
			holder.setPattFill((CTPatternFillProperties) f.fill);
			Drawing.Pattern p = f.fills.pattern(holder);
			return p != null && p.getForeground() != null && p.getBackground() != null && p.getForeground().getAlpha() >= 255
					&& p.getBackground().getAlpha() >= 255;
		}
		return false;
	}

	/** docx4j.convert.out.effects.pixelsPerPoint, an upper bound on an effect picture's resolution (D3); 0 for none */
	private static double maxPixelsPerPoint() {
		String bound = org.docx4j.Docx4jProperties.getProperty("docx4j.convert.out.effects.pixelsPerPoint");
		if (bound == null || bound.trim().isEmpty()) return 0;
		try {
			return Double.parseDouble(bound.trim());
		} catch (NumberFormatException e) {
			return 0;
		}
	}

	// ------------------------------------------------------------------ groups and canvases

	/**
	 * A group ({@code wpg:wgp}) drawn at its size: its shapes and pictures, nested groups' too, into one drawing in the
	 * group's frame, each where Word draws it (see {@link Placed}). The group's own turn and flips apply, about its box.
	 *
	 * @param cx its width in EMU, its anchor's extent
	 * @param cy its height in EMU
	 * @since 17.3.7 (CR-080 phase 3)
	 */
	public static Drawn drawGroup(CTWordprocessingGroup group, ThemeColors theme, long cx, long cy, Pictures pictures) {
		double w = cx / 12700.0, h = cy / 12700.0;
		Walk walk = new Walk(new Drawing(w, h), theme, pictures);
		CTGroupShapeProperties gp = group == null ? null : group.getGrpSpPr();
		CTGroupTransform2D x = gp == null ? null : gp.getXfrm();
		Frame top = new Frame(0, 0, w, h, x == null ? 0 : x.getRot() / 60000.0, x != null && x.isFlipH(), x != null && x.isFlipV());
		top.childSpace(x);
		top.fill = groupFill(gp, null, theme);
		groupEffects(gp, walk.problems);
		if (group != null) walk.children(top, group.getWspOrGrpSpOrGraphicFrame());
		return walk.drawn(w, h);
	}

	/**
	 * A drawing canvas ({@code wpc:wpc}) drawn at its size: its background ({@code wpc:bg}) as a fill of its box and its
	 * outline ({@code wpc:whole}'s line), then its shapes, pictures and groups, in the canvas's own coordinates, as Word
	 * draws them (CR-080 phase 0, fp92).
	 *
	 * @since 17.3.7 (CR-080 phase 3)
	 */
	public static Drawn drawCanvas(CTWordprocessingCanvas canvas, ThemeColors theme, long cx, long cy, Pictures pictures) {
		double w = cx / 12700.0, h = cy / 12700.0;
		Walk walk = new Walk(new Drawing(w, h), theme, pictures);
		if (canvas != null && (canvas.getBg() != null || canvas.getWhole() != null)) {
			CTShapeProperties box = rectangle();
			if (canvas.getBg() != null) {
				box.setNoFill(canvas.getBg().getNoFill());
				box.setSolidFill(canvas.getBg().getSolidFill());
				box.setGradFill(canvas.getBg().getGradFill());
				box.setPattFill(canvas.getBg().getPattFill());
				box.setBlipFill(canvas.getBg().getBlipFill());
			}
			if (canvas.getWhole() != null) box.setLn(canvas.getWhole().getLn());
			ShapeOutlines o = ShapeGeometry.outlines(box, cx, cy);
			walk.bounds = union(walk.bounds, paintShape(walk.drawing, box, null, theme, o, w, h, new AffineTransform(), pictures, null, walk.problems));
		}
		Frame top = new Frame(0, 0, w, h, 0, false, false);
		if (canvas != null) walk.children(top, canvas.getWspOrPicOrContentPart());
		return walk.drawn(w, h);
	}

	/** A group's box in the drawing's frame (points), its turn and flips, the child space it maps from, its fill */
	private static final class Frame {
		final double x, y, w, h, rotation;
		final boolean flipH, flipV;
		double chX, chY, kx = 1 / 12700.0, ky = 1 / 12700.0;
		Fill fill;

		Frame(double x, double y, double w, double h, double rotation, boolean flipH, boolean flipV) {
			this.x = x;
			this.y = y;
			this.w = w;
			this.h = h;
			this.rotation = rotation;
			this.flipH = flipH;
			this.flipV = flipV;
		}

		/** Its children's coordinates: chOff and chExt mapped to its box; without them, its off and ext (no scaling) */
		void childSpace(CTGroupTransform2D x) {
			if (x == null) return;
			long ox = x.getChOff() != null ? x.getChOff().getX() : x.getOff() != null ? x.getOff().getX() : 0;
			long oy = x.getChOff() != null ? x.getChOff().getY() : x.getOff() != null ? x.getOff().getY() : 0;
			long ex = x.getChExt() != null ? x.getChExt().getCx() : x.getExt() != null ? x.getExt().getCx() : 0;
			long ey = x.getChExt() != null ? x.getChExt().getCy() : x.getExt() != null ? x.getExt().getCy() : 0;
			chX = ox;
			chY = oy;
			if (ex > 0) kx = w / ex;
			if (ey > 0) ky = h / ey;
		}

		/** Its turn and flips about its box's centre */
		AffineTransform turn() {
			return ShapeRenderer.turn(x, y, w, h, rotation, flipH, flipV);
		}

		/** A child at off, ext (EMU, in this group's child space), turned and flipped: where it is drawn */
		Frame place(long offX, long offY, long extX, long extY, double rot, boolean fh, boolean fv) {
			double bw = extX * kx, bh = extY * ky;
			double bx = x + (offX - chX) * kx, by = y + (offY - chY) * ky;
			java.awt.geom.Point2D c = turn().transform(new java.awt.geom.Point2D.Double(bx + bw / 2, by + bh / 2), null);
			// R(g) F(g) R(c) F(c): one flip in the group reverses the child's turn
			double r = flipH != flipV ? rotation - rot : rotation + rot;
			return new Frame(c.getX() - bw / 2, c.getY() - bh / 2, bw, bh, r, flipH != fh, flipV != fv);
		}
	}

	/** A walk down a group or canvas, painting as it goes */
	private static final class Walk {
		final Drawing drawing;
		final ThemeColors theme;
		final Pictures pictures;
		final List<String> problems = new ArrayList<String>();
		final List<Placed> placed = new ArrayList<Placed>();
		Rectangle2D bounds;

		Walk(Drawing drawing, ThemeColors theme, Pictures pictures) {
			this.drawing = drawing;
			this.theme = theme;
			this.pictures = pictures;
		}

		void children(Frame parent, List<Object> children) {
			for (Object o : children) {
				Object c = o instanceof jakarta.xml.bind.JAXBElement ? ((jakarta.xml.bind.JAXBElement<?>) o).getValue() : o;
				if (c instanceof CTWordprocessingShape) {
					CTWordprocessingShape wsp = (CTWordprocessingShape) c;
					shape(parent, c, wsp.getSpPr(), wsp.getStyle());
				} else if (c instanceof Pic) {
					Pic pic = (Pic) c;
					CTShapeProperties sp = pictureAsShape(pic);
					shape(parent, c, sp, null);
				} else if (c instanceof CTWordprocessingGroup) {
					CTWordprocessingGroup g = (CTWordprocessingGroup) c;
					CTGroupTransform2D x = g.getGrpSpPr() == null ? null : g.getGrpSpPr().getXfrm();
					Frame f = x == null ? parent.place(0, 0, 0, 0, 0, false, false)
							: parent.place(x.getOff() == null ? 0 : x.getOff().getX(), x.getOff() == null ? 0 : x.getOff().getY(),
									x.getExt() == null ? 0 : x.getExt().getCx(), x.getExt() == null ? 0 : x.getExt().getCy(), x.getRot() / 60000.0,
									x.isFlipH(), x.isFlipV());
					f.childSpace(x);
					f.fill = groupFill(g.getGrpSpPr(), parent.fill, theme);
					groupEffects(g.getGrpSpPr(), problems);
					children(f, g.getWspOrGrpSpOrGraphicFrame());
				} else {
					if (!problems.contains(CHILD_NOT_DRAWN)) problems.add(CHILD_NOT_DRAWN);
				}
			}
		}

		void shape(Frame parent, Object child, CTShapeProperties sp, CTShapeStyle style) {
			CTTransform2D x = sp == null ? null : sp.getXfrm();
			if (x == null || x.getExt() == null) return; // a child with no box has nowhere to be drawn
			Frame f = parent.place(x.getOff() == null ? 0 : x.getOff().getX(), x.getOff() == null ? 0 : x.getOff().getY(), x.getExt().getCx(),
					x.getExt().getCy(), x.getRot() / 60000.0, x.isFlipH(), x.isFlipV());
			long cx = Math.round(f.w * 12700), cy = Math.round(f.h * 12700);
			ShapeOutlines outlines = ShapeGeometry.outlines(sp, cx, cy);
			if (outlines.getProblem() != null && !problems.contains(outlines.getProblem())) problems.add(outlines.getProblem());
			AffineTransform at = turn(f.x, f.y, f.w, f.h, f.rotation, f.flipH, f.flipV);
			at.translate(f.x, f.y);
			int from = drawing.getItems().size();
			Rectangle2D painted = paintShape(drawing, sp, style, theme, outlines, f.w, f.h, at, pictures, parent.fill, problems);
			// its effects, round its own drawing (CR-081 phase 3); a picture's own spPr's
			CTShapeProperties own = child instanceof Pic ? ((Pic) child).getSpPr() : sp;
			painted = effects(drawing, from, own, style, theme, parent.fill, new Rectangle2D.Double(f.x, f.y, f.w, f.h),
					child instanceof CTWordprocessingShape && org.docx4j.model.shapes.ShapeText.hasText((CTWordprocessingShape) child), painted, problems);
			bounds = union(bounds, painted);
			placed.add(new Placed(child, new Rectangle2D.Double(f.x, f.y, f.w, f.h), f.rotation, f.flipH, f.flipV, outlines));
		}

		Drawn drawn(double w, double h) {
			return new Drawn(drawing, bounds == null ? new Rectangle2D.Double(0, 0, w, h) : rect(bounds),
					ShapeGeometry.outlines((CTShapeProperties) null, Math.round(w * 12700), Math.round(h * 12700)), problems, placed);
		}
	}

	/** A picture as a shape whose fill is the picture: its geometry (a rectangle where it states none) and its line */
	private static CTShapeProperties pictureAsShape(Pic pic) {
		CTShapeProperties own = pic.getSpPr();
		CTShapeProperties sp = own != null && (own.getPrstGeom() != null || own.getCustGeom() != null) ? shallow(own) : rectangle();
		if (own != null) {
			sp.setXfrm(own.getXfrm());
			sp.setLn(own.getLn());
		}
		sp.setNoFill(null);
		sp.setSolidFill(null);
		sp.setGradFill(null);
		sp.setPattFill(null);
		sp.setGrpFill(null);
		sp.setBlipFill(pic.getBlipFill());
		return sp;
	}

	private static CTShapeProperties shallow(CTShapeProperties own) {
		CTShapeProperties sp = new CTShapeProperties();
		sp.setPrstGeom(own.getPrstGeom());
		sp.setCustGeom(own.getCustGeom());
		return sp;
	}

	private static CTShapeProperties rectangle() {
		CTShapeProperties sp = new CTShapeProperties();
		CTPresetGeometry2D g = new CTPresetGeometry2D();
		g.setPrst(STShapeType.RECT);
		sp.setPrstGeom(g);
		return sp;
	}

	/** A group's fill, for its children's a:grpFill: its own, else (a:grpFill, or none stated) its group's */
	private static Fill groupFill(CTGroupShapeProperties gp, Fill parent, ThemeColors theme) {
		if (gp == null) return parent;
		if (gp.getNoFill() != null) return null;
		Object own = gp.getSolidFill() != null ? gp.getSolidFill() : gp.getGradFill() != null ? gp.getGradFill()
				: gp.getPattFill() != null ? gp.getPattFill() : gp.getBlipFill() != null ? gp.getBlipFill() : null;
		return own != null ? new Fill(own, new Fills(theme)) : parent;
	}

	private static Rectangle2D.Double rect(Rectangle2D r) {
		return new Rectangle2D.Double(r.getX(), r.getY(), r.getWidth(), r.getHeight());
	}

	private static Rectangle2D union(Rectangle2D a, Rectangle2D b) {
		if (b == null || b.isEmpty() && b.getWidth() == 0 && b.getHeight() == 0) return a;
		if (a == null) return b.getBounds2D();
		Rectangle2D u = a.getBounds2D();
		Rectangle2D.union(u, b, u);
		return u;
	}

	/** A turn and flips about the centre of the box (x, y, w, h): flipped, then turned */
	static AffineTransform turn(double x, double y, double w, double h, double degrees, boolean flipH, boolean flipV) {
		AffineTransform at = new AffineTransform();
		if (degrees == 0 && !flipH && !flipV) return at;
		at.translate(x + w / 2, y + h / 2);
		if (degrees != 0) at.rotate(Math.toRadians(degrees));
		at.scale(flipH ? -1 : 1, flipV ? -1 : 1);
		at.translate(-(x + w / 2), -(y + h / 2));
		return at;
	}

	/** The shape's turn and flips about its box's centre: flipped, then turned */
	static AffineTransform turn(CTTransform2D xfrm, double w, double h) {
		AffineTransform at = new AffineTransform();
		if (xfrm == null || (xfrm.getRot() == 0 && !xfrm.isFlipH() && !xfrm.isFlipV())) return at;
		at.translate(w / 2, h / 2);
		if (xfrm.getRot() != 0) at.rotate(Math.toRadians(xfrm.getRot() / 60000.0));
		at.scale(xfrm.isFlipH() ? -1 : 1, xfrm.isFlipV() ? -1 : 1);
		at.translate(-w / 2, -h / 2);
		return at;
	}

	// ------------------------------------------------------------------ the fill

	/** A shape's fill: its own where it states one, else its style's fillRef, with the theme that resolves it */
	private static final class Fill {
		final Object fill;
		final Fills fills;

		Fill(Object fill, Fills fills) {
			this.fill = fill;
			this.fills = fills;
		}

		/** The area painted; null where nothing is */
		Rectangle2D paint(Drawing d, Shape s, STPathFillMode mode, Shape box, Pictures pictures, List<String> problems) {
			if (fill instanceof CTSolidColorFillProperties) {
				Color c = Fills.awt(DrawingMLColors.resolve(fill, fills.getTheme()));
				if (c == null) return null;
				d.add(new Drawing.Area(s, modify(c, mode), null, 0, "shape.fill"));
			} else if (fill instanceof CTGradientFillProperties) {
				Drawing.Gradient g = fills.gradient((CTGradientFillProperties) fill);
				if (g == null) return null;
				Color[] cs = g.getColours().clone();
				for (int i = 0; i < cs.length; i++) cs[i] = modify(cs[i], mode);
				Drawing.Area a = new Drawing.Area(s, cs[0], null, 0, "shape.fill");
				a.setGradient(new Drawing.Gradient(g.getStops(), cs, g.isLinear(), g.getAngle(), g.isScaled(), g.getFocus()));
				d.add(a);
			} else if (fill instanceof CTPatternFillProperties) {
				CTShapeProperties holder = new CTShapeProperties();
				holder.setPattFill((CTPatternFillProperties) fill);
				Drawing.Pattern p = fills.pattern(holder);
				Drawing.Area a = new Drawing.Area(s, modify(p.getForeground(), mode), null, 0, "shape.fill");
				a.setPattern(new Drawing.Pattern(p.getTile(), modify(p.getForeground(), mode), modify(p.getBackground(), mode)));
				d.add(a);
			} else if (fill instanceof CTBlipFillProperties) {
				if (!picture(d, s, box.getBounds2D(), (CTBlipFillProperties) fill, pictures)) {
					if (!problems.contains(PICTURE_MISSING)) problems.add(PICTURE_MISSING);
					return null;
				}
			} else {
				return null;
			}
			return s.getBounds2D();
		}
	}

	private static Fill fill(CTShapeProperties sp, CTShapeStyle style, ThemeColors theme, Fill groupFill) {
		if (sp != null) {
			if (sp.getNoFill() != null) return null;
			Object own = sp.getSolidFill() != null ? sp.getSolidFill() : sp.getGradFill() != null ? sp.getGradFill()
					: sp.getPattFill() != null ? sp.getPattFill() : sp.getBlipFill() != null ? sp.getBlipFill() : null;
			if (own != null) return new Fill(own, new Fills(theme));
			// a:grpFill is its group's (CR-080 phase 3, fp91-014)
			if (sp.getGrpFill() != null) return groupFill;
		}
		if (style == null || style.getFillRef() == null) return null;
		StyleFill sf = DrawingMLColors.fillReference(style.getFillRef(), theme);
		if (sf.getFill() == null) return null;
		return new Fill(sf.getFill(), new Fills(theme.withPlaceholder(DrawingMLColors.resolve(style.getFillRef(), theme))));
	}

	/** The fill mode's lightening or darkening, as Word's (CR-080 phase 0, on 4472C4) */
	static Color modify(Color c, STPathFillMode mode) {
		if (c == null || mode == null) return c;
		double f;
		boolean lighten;
		switch (mode) {
			case LIGHTEN: f = 0.4; lighten = true; break;
			case LIGHTEN_LESS: f = 0.2; lighten = true; break;
			case DARKEN: f = 0.6; lighten = false; break;
			case DARKEN_LESS: f = 0.807; lighten = false; break;
			default: return c;
		}
		int[] v = { c.getRed(), c.getGreen(), c.getBlue() };
		for (int i = 0; i < 3; i++) v[i] = (int) Math.round(lighten ? v[i] + f * (255 - v[i]) : v[i] * f);
		return new Color(v[0], v[1], v[2], c.getAlpha());
	}

	/** A picture fill, stretched (into its fillRect, cropped by its srcRect) or tiled; false where there is no picture */
	private static boolean picture(Drawing d, Shape s, Rectangle2D box, CTBlipFillProperties bf, Pictures pictures) {
		String id = bf.getBlip() == null ? null : bf.getBlip().getEmbed();
		BufferedImage img = id == null || pictures == null ? null : pictures.picture(id);
		if (img == null) return false;
		img = crop(img, bf.getSrcRect());
		if (bf.getTile() != null) {
			org.docx4j.dml.CTTileInfoProperties t = bf.getTile();
			// the picture's own size, at 96 dpi where it says nothing, scaled
			double tw = img.getWidth() * 72.0 / 96 * (t.getSx() == null ? 1 : t.getSx() / 100000.0);
			double th = img.getHeight() * 72.0 / 96 * (t.getSy() == null ? 1 : t.getSy() / 100000.0);
			String flip = t.getFlip() == null ? "none" : t.getFlip().value();
			BufferedImage tile = flip(img, flip);
			double cellW = flip.contains("x") ? 2 * tw : tw, cellH = flip.contains("y") ? 2 * th : th;
			String algn = t.getAlgn() == null ? "tl" : t.getAlgn().value();
			double x = box.getX() + (algn.endsWith("r") ? box.getWidth() - tw : algn.endsWith("l") ? 0 : (box.getWidth() - tw) / 2);
			double y = box.getY() + (algn.startsWith("b") ? box.getHeight() - th : algn.startsWith("t") ? 0 : (box.getHeight() - th) / 2);
			x += t.getTx() == null ? 0 : t.getTx() / 12700.0;
			y += t.getTy() == null ? 0 : t.getTy() / 12700.0;
			Drawing.Area a = new Drawing.Area(s, null, null, 0, "shape.fill");
			a.setPaint(new java.awt.TexturePaint(tile, new Rectangle2D.Double(x, y, cellW, cellH)));
			d.add(a);
			return true;
		}
		CTRelativeRect fr = bf.getStretch() == null ? null : bf.getStretch().getFillRect();
		Rectangle2D into = box;
		if (fr != null) {
			double bw = box.getWidth(), bh = box.getHeight();
			into = new Rectangle2D.Double(box.getX() + bw * fr.getL() / 100000.0, box.getY() + bh * fr.getT() / 100000.0,
					bw * (1 - (fr.getL() + fr.getR()) / 100000.0), bh * (1 - (fr.getT() + fr.getB()) / 100000.0));
		}
		d.add(new Drawing.Picture(img, into, s));
		return true;
	}

	private static BufferedImage crop(BufferedImage img, CTRelativeRect src) {
		if (src == null || (src.getL() == 0 && src.getT() == 0 && src.getR() == 0 && src.getB() == 0)) return img;
		int w = img.getWidth(), h = img.getHeight();
		int x0 = clamp((int) Math.round(w * src.getL() / 100000.0), 0, w - 1), y0 = clamp((int) Math.round(h * src.getT() / 100000.0), 0, h - 1);
		int x1 = clamp((int) Math.round(w * (1 - src.getR() / 100000.0)), x0 + 1, w), y1 = clamp((int) Math.round(h * (1 - src.getB() / 100000.0)), y0 + 1, h);
		return img.getSubimage(x0, y0, x1 - x0, y1 - y0);
	}

	private static int clamp(int v, int lo, int hi) {
		return Math.max(lo, Math.min(hi, v));
	}

	/** A tile flipped as {@code a:tile/@flip} has it: the picture beside its mirror image across, down or both */
	private static BufferedImage flip(BufferedImage img, String flip) {
		if ("none".equals(flip)) return img;
		boolean fx = flip.contains("x"), fy = flip.contains("y");
		int w = img.getWidth(), h = img.getHeight();
		BufferedImage out = new BufferedImage(fx ? 2 * w : w, fy ? 2 * h : h, BufferedImage.TYPE_INT_ARGB);
		java.awt.Graphics2D g = out.createGraphics();
		g.drawImage(img, 0, 0, null);
		if (fx) g.drawImage(img, 2 * w, 0, -w, h, null);
		if (fy) g.drawImage(img, 0, 2 * h, w, -h, null);
		if (fx && fy) g.drawImage(img, 2 * w, 2 * h, -w, -h, null);
		g.dispose();
		return out;
	}

	// ------------------------------------------------------------------ the line

	/** A shape's line, its own a:ln over its style's lnRef, attribute by attribute (CR-080 phase 0: fp50-012) */
	private static final class Line {
		Object paint; // the line's fill: solid, gradient or pattern
		Fills fills;
		double width;
		String cap = "flat", cmpd = "sng", algn = "ctr", dash;
		int join = BasicStroke.JOIN_ROUND;
		List<org.docx4j.dml.CTDashStop> custDash;
		CTLineEndProperties head, tail;

		/** Paints a stroked path, with its arrowheads where it is the first or the last; the area painted */
		Rectangle2D paint(Drawing d, Path2D.Double path, AffineTransform turn, boolean first, boolean last) {
			Color colour = paint instanceof CTSolidColorFillProperties ? Fills.awt(DrawingMLColors.resolve(paint, fills.getTheme())) : null;
			Drawing.Gradient gradient = paint instanceof CTGradientFillProperties ? fills.gradient((CTGradientFillProperties) paint) : null;
			Drawing.Pattern pattern = null;
			if (paint instanceof CTPatternFillProperties) {
				CTShapeProperties holder = new CTShapeProperties();
				holder.setPattFill((CTPatternFillProperties) paint);
				pattern = fills.pattern(holder);
			}
			if (colour == null && gradient != null) colour = gradient.getColours()[0];
			if (colour == null && pattern != null) colour = pattern.getForeground();
			if (colour == null) return null;

			boolean open = isOpen(path);
			// a rectangle as Word's PDF draws it, an re from its bottom left, so that its dashes start there
			Shape shape = turn.createTransformedShape(asRe(path));

			// arrowheads on the open ends of the first and last stroked paths, the line drawn up to them
			List<Shape> heads = new ArrayList<Shape>();
			if (open && (first && head != null || last && tail != null)) {
				List<double[]> pts = flatten(shape);
				if (pts.size() >= 2) {
					double headBack = 0, tailBack = 0;
					if (first && end(head) != null) {
						double[] dir = direction(pts, true, length(head, width));
						Head hd = head(head, width, pts.get(0), dir);
						if (hd != null) {
							heads.add(hd.shape);
							headBack = hd.back;
						}
					}
					if (last && end(tail) != null) {
						double[] dir = direction(pts, false, length(tail, width));
						Head hd = head(tail, width, pts.get(pts.size() - 1), dir);
						if (hd != null) {
							heads.add(hd.shape);
							tailBack = hd.back;
						}
					}
					if (headBack > 0 || tailBack > 0) shape = trim(pts, headBack, tailBack);
				}
			}

			return stroke(d, shape, open, heads, false);
		}

		/**
		 * Paints a picture's line (CR-081 phase 2): on a closed path, wholly outside it, stroked on the path moved half
		 * its width outward, a compound line as rings from the path outward; on an open one, as a shape's. The area
		 * painted.
		 */
		Rectangle2D paintOutside(Drawing d, Path2D.Double path, AffineTransform turn, boolean first, boolean last) {
			if (isOpen(path)) return paint(d, path, turn, first, last);
			double w = width <= 0 ? HAIRLINE : width;
			if (!"sng".equals(cmpd)) {
				return stroke(d, turn.createTransformedShape(path), false, Collections.<Shape>emptyList(), true);
			}
			return stroke(d, turn.createTransformedShape(asRe(outward(path, w / 2))), false, Collections.<Shape>emptyList(), false);
		}

		/** The line on the shape, and its heads; outside: a compound line's rings from the shape outward, at its join */
		private Rectangle2D stroke(Drawing d, Shape shape, boolean open, List<Shape> heads, boolean outside) {
			Color colour = paint instanceof CTSolidColorFillProperties ? Fills.awt(DrawingMLColors.resolve(paint, fills.getTheme())) : null;
			Drawing.Gradient gradient = paint instanceof CTGradientFillProperties ? fills.gradient((CTGradientFillProperties) paint) : null;
			Drawing.Pattern pattern = null;
			if (paint instanceof CTPatternFillProperties) {
				CTShapeProperties holder = new CTShapeProperties();
				holder.setPattFill((CTPatternFillProperties) paint);
				pattern = fills.pattern(holder);
			}
			if (colour == null && gradient != null) colour = gradient.getColours()[0];
			if (colour == null && pattern != null) colour = pattern.getForeground();
			if (colour == null) return null;
			Rectangle2D painted;
			double w = width <= 0 ? HAIRLINE : width;
			boolean compound = !"sng".equals(cmpd), inside = "in".equals(algn) && !open && !outside;
			if (compound || inside) {
				// Word draws these as filled bands (phase 0: fp30-056 to -059, -061); a picture's all outside
				Area band = outside ? band(shape, w, false, cmpd, false, true, join) : band(shape, w, open, compound ? cmpd : "sng", inside);
				Drawing.Area a = new Drawing.Area(band, colour, null, 0, "shape.line");
				if (gradient != null) a.setGradient(gradient);
				else if (pattern != null) a.setPattern(pattern);
				d.add(a);
				painted = band.getBounds2D();
			} else {
				Stroke st = stroke(w);
				Drawing.Area a = new Drawing.Area(shape, null, colour, w, "shape.line");
				a.setStroke(st);
				if (gradient != null) a.setLineGradient(gradient);
				else if (pattern != null) a.setLinePattern(pattern);
				d.add(a);
				painted = st.createStrokedShape(shape).getBounds2D();
			}
			for (Shape hd : heads) {
				Drawing.Area a = new Drawing.Area(hd, colour, null, 0, "shape.arrowhead");
				d.add(a);
				painted = union(painted, hd.getBounds2D());
			}
			return painted;
		}

		Stroke stroke(double w) {
			int c = "rnd".equals(cap) ? BasicStroke.CAP_ROUND : "sq".equals(cap) ? BasicStroke.CAP_SQUARE : BasicStroke.CAP_BUTT;
			float[] dashes = dashes(w);
			// a:miter's lim isn't used: Word's PDF has the default limit, 10 (phase 0: fp30-052, -053)
			return dashes == null ? new BasicStroke((float) w, c, join, 10f) : new BasicStroke((float) w, c, join, 10f, dashes, 0f);
		}

		/** The dashes in points: the preset's or custDash's, in multiples of the width; with a round cap, each dash a width shorter */
		float[] dashes(double w) {
			float[] u = null;
			if (custDash != null && !custDash.isEmpty()) {
				u = new float[custDash.size() * 2];
				for (int i = 0; i < custDash.size(); i++) {
					u[2 * i] = custDash.get(i).getD() / 100000f;
					u[2 * i + 1] = custDash.get(i).getSp() / 100000f;
				}
			} else if (dash != null) {
				CTShapeProperties holder = new CTShapeProperties();
				CTLineProperties ln = new CTLineProperties();
				org.docx4j.dml.CTPresetLineDashProperties pd = new org.docx4j.dml.CTPresetLineDashProperties();
				pd.setVal(org.docx4j.dml.STPresetLineDashVal.fromValue(dash));
				ln.setPrstDash(pd);
				holder.setLn(ln);
				float[] f = Fills.dashes(holder, 1);
				u = f;
			}
			if (u == null) return null;
			float[] out = new float[u.length];
			// a round cap reaches half a width past each end of a dash: Word takes that off the dash (fp30-033 to
			// -043); a square cap's dashes aren't measured, and are left as the preset's
			boolean round = "rnd".equals(cap);
			for (int i = 0; i < u.length; i++) {
				double v = u[i] * w;
				if (round) v += (i % 2 == 0) ? -w : w;
				out[i] = (float) Math.max(i % 2 == 0 ? 0.001 : 0, v);
			}
			return out;
		}
	}

	/**
	 * The presets Word strokes with a mitred join where no join is stated (phase 0: fp10, fp11, 31 of 187); the rest
	 * it rounds. Office's legacy shape types differ so: rightArrow is mitred and leftArrow round.
	 */
	static final java.util.Set<String> MITRED = new java.util.HashSet<String>(java.util.Arrays.asList("bentUpArrow", "chevron", "cube",
			"downArrow", "ellipseRibbon", "ellipseRibbon2", "leftArrowCallout", "leftRightArrowCallout", "leftUpArrow", "mathEqual", "mathMinus",
			"mathMultiply", "mathNotEqual", "mathPlus", "moon", "notchedRightArrow", "plus", "quadArrowCallout", "rightArrow", "star10", "star12",
			"star16", "star24", "star32", "star4", "star5", "star8", "stripedRightArrow", "upArrow", "upDownArrow", "upDownArrowCallout"));

	private static Line line(CTShapeProperties sp, CTShapeStyle style, ThemeColors theme, String preset) {
		CTLineProperties own = sp == null ? null : sp.getLn();
		CTLineProperties base = null;
		ThemeColors baseTheme = theme;
		if (style != null && style.getLnRef() != null) {
			long idx = style.getLnRef().getIdx();
			org.docx4j.dml.CTStyleMatrix fmt = theme == null ? null : theme.getFormatScheme();
			if (idx > 0 && fmt != null && fmt.getLnStyleLst() != null && idx <= fmt.getLnStyleLst().getLn().size()) {
				base = fmt.getLnStyleLst().getLn().get((int) idx - 1);
				baseTheme = theme.withPlaceholder(DrawingMLColors.resolve(style.getLnRef(), theme));
			}
		}
		if (own == null && base == null) return null;
		Line l = new Line();
		if (preset != null && MITRED.contains(preset)) l.join = BasicStroke.JOIN_MITER;
		// the line's fill: the shape's own, else the style's
		Object ownPaint = own == null ? null : own.getNoFill() != null ? own.getNoFill() : own.getSolidFill() != null ? own.getSolidFill()
				: own.getGradFill() != null ? own.getGradFill() : own.getPattFill();
		if (ownPaint instanceof org.docx4j.dml.CTNoFillProperties) return null;
		if (ownPaint != null) {
			l.paint = ownPaint;
			l.fills = new Fills(theme);
		} else if (base != null) {
			Object bp = base.getNoFill() != null ? null : base.getSolidFill() != null ? base.getSolidFill()
					: base.getGradFill() != null ? base.getGradFill() : base.getPattFill();
			if (bp == null) return null;
			l.paint = bp;
			l.fills = new Fills(baseTheme);
		} else {
			return null;
		}
		Integer w = own != null && own.getW() != null ? own.getW() : base != null ? base.getW() : null;
		l.width = w == null ? 0.75 : w / 12700.0;
		CTLineProperties[] order = { base, own };
		for (CTLineProperties p : order) {
			if (p == null) continue;
			if (p.getCap() != null) l.cap = p.getCap().value();
			if (p.getCmpd() != null) l.cmpd = p.getCmpd().value();
			if (p.getAlgn() != null) l.algn = p.getAlgn().value();
			if (p.getPrstDash() != null && p.getPrstDash().getVal() != null) {
				l.dash = p.getPrstDash().getVal().value();
				l.custDash = null;
			}
			if (p.getCustDash() != null) {
				l.custDash = p.getCustDash().getDs();
				l.dash = null;
			}
			if (p.getRound() != null) l.join = BasicStroke.JOIN_ROUND;
			if (p.getBevel() != null) l.join = BasicStroke.JOIN_BEVEL;
			if (p.getMiter() != null) l.join = BasicStroke.JOIN_MITER;
			if (p.getHeadEnd() != null) l.head = p.getHeadEnd();
			if (p.getTailEnd() != null) l.tail = p.getTailEnd();
		}
		return l;
	}

	// ------------------------------------------------------------------ compound and inside lines

	/**
	 * A compound or inside line as the band Word fills, its offsets from the path in widths, outward positive
	 * (phase 0, at 9pt): dbl 1:1:1, thickThin 3:1:1 thick outside, thinThick 1:1:3, tri 1:1:2:1:1, and an inside line
	 * all of its width inside. An open path has no inside: there the bands are as on a closed path's outside, mirrored.
	 */
	static Area band(Shape shape, double w, boolean open, String cmpd, boolean inside) {
		return band(shape, w, open, cmpd, inside, false, BasicStroke.JOIN_ROUND);
	}

	/**
	 * As {@link #band(Shape, double, boolean, String, boolean)}; outside: the whole width outside a closed path (a
	 * picture's line, CR-081 phase 2: fs10-07's thickThin 18pt is rings 18 to 7.2pt and 3.6 to 0pt out), the outer
	 * corners at the join given (a shape's are round, CR-080 phase 0; a picture's mitred line has square ones)
	 */
	static Area band(Shape shape, double w, boolean open, String cmpd, boolean inside, boolean outside, int join) {
		double[][] bands;
		switch (cmpd) {
			case "dbl": bands = new double[][] { { 0.5, 1.0 / 6 }, { -1.0 / 6, -0.5 } }; break;
			case "thickThin": bands = new double[][] { { 0.5, -0.1 }, { -0.3, -0.5 } }; break;
			case "thinThick": bands = new double[][] { { 0.5, 0.3 }, { 0.1, -0.5 } }; break;
			case "tri": bands = new double[][] { { 0.5, 1.0 / 3 }, { 1.0 / 6, -1.0 / 6 }, { -1.0 / 3, -0.5 } }; break;
			default: bands = new double[][] { { 0.5, -0.5 } };
		}
		if (inside || outside && !open) {
			// the whole width inside the path, or outside it
			double by = inside ? -0.5 : 0.5;
			for (double[] b : bands) {
				b[0] = b[0] + by;
				b[1] = b[1] + by;
			}
		}
		Area interior = open ? null : new Area(shape);
		Area out = new Area();
		for (double[] b : bands) {
			double outer = b[0] * w, inner = b[1] * w;
			out.add(open ? openBand(shape, Math.max(Math.abs(outer), Math.abs(inner)), Math.min(Math.abs(outer), Math.abs(inner)), outer * inner < 0, join)
					: closedBand(shape, interior, outer, inner, join));
		}
		return out;
	}

	/** The points at signed distance between inner and outer (outward positive) from a closed path */
	private static Area closedBand(Shape shape, Area interior, double outer, double inner, int join) {
		Area band = new Area();
		if (outer > 0) {
			Area ring = within(shape, outer, join);
			if (inner > 0) ring.subtract(within(shape, inner, join));
			ring.subtract(interior);
			band.add(ring);
		}
		if (inner < 0) {
			Area ring = within(shape, -inner, join);
			if (outer < 0) ring.subtract(within(shape, -outer, join));
			ring.intersect(interior);
			band.add(ring);
		}
		return band;
	}

	private static Area openBand(Shape shape, double far, double near, boolean across, int join) {
		Area ring = within(shape, far, join);
		if (!across && near > 0) ring.subtract(within(shape, near, join));
		return ring;
	}

	/**
	 * The points within d of the path: a stroke of width 2d at the join (round for a shape's, as Word's bands' outer
	 * corners are round; mitred, at Word's limit of 10, for a picture's mitred line)
	 */
	private static Area within(Shape shape, double d, int join) {
		if (d <= 0) return new Area();
		return new Area(new BasicStroke((float) (2 * d), BasicStroke.CAP_BUTT, join, 10f).createStrokedShape(shape));
	}

	// ------------------------------------------------------------------ arrowheads

	private static String end(CTLineEndProperties e) {
		if (e == null || e.getType() == null) return null;
		String t = e.getType().value();
		return "none".equals(t) ? null : t;
	}

	private static final class Head {
		final Shape shape;
		/** how far back from the end the line stops */
		final double back;

		Head(Shape shape, double back) {
			this.shape = shape;
			this.back = back;
		}
	}

	/** The units of an arrowhead's width and length: sm 2, med 3, lg 5 */
	private static double units(Object v) {
		String s = v == null ? "med" : v instanceof org.docx4j.dml.STLineEndWidth ? ((org.docx4j.dml.STLineEndWidth) v).value()
				: ((org.docx4j.dml.STLineEndLength) v).value();
		return "sm".equals(s) ? 2 : "lg".equals(s) ? 5 : 3;
	}

	/**
	 * An arrowhead at the end of a line, pointing along dir (a unit vector toward the end), as Word draws it (phase 0:
	 * fp31): with s the larger of the line's width and 2pt, its length L and width W are 2, 3 or 5 s. A triangle's tip
	 * is the end, the line stopping s/2 inside its base; a stealth's notch is s, 2s or 3s back, where the line stops; a
	 * diamond and an oval are centred on the end, where the line stops; an arrow is an open V of the line's width, its
	 * outer tip the end, its arms round-ended, 2s, 3s or 4.5s back and 1.25s, 1.75s or 2.5s across from its apex.
	 */
	static Head head(CTLineEndProperties e, double lineWidth, double[] end, double[] dir) {
		String type = end(e);
		if (type == null) return null;
		double s = Math.max(lineWidth, 2);
		double lu = units(e.getLen()), L = lu * s, W = units(e.getW()) * s;
		double ex = end[0], ey = end[1], ux = dir[0], uy = dir[1], nx = -uy, ny = ux;
		Path2D.Double p = new Path2D.Double();
		switch (type) {
			case "triangle":
				p.moveTo(ex, ey);
				p.lineTo(ex - L * ux + W / 2 * nx, ey - L * uy + W / 2 * ny);
				p.lineTo(ex - L * ux - W / 2 * nx, ey - L * uy - W / 2 * ny);
				p.closePath();
				return new Head(p, L - s / 2);
			case "stealth": {
				double notch = (lu == 2 ? 1 : lu == 3 ? 2 : 3) * s;
				p.moveTo(ex, ey);
				p.lineTo(ex - L * ux + W / 2 * nx, ey - L * uy + W / 2 * ny);
				p.lineTo(ex - notch * ux, ey - notch * uy);
				p.lineTo(ex - L * ux - W / 2 * nx, ey - L * uy - W / 2 * ny);
				p.closePath();
				return new Head(p, notch);
			}
			case "diamond":
				p.moveTo(ex + L / 2 * ux, ey + L / 2 * uy);
				p.lineTo(ex + W / 2 * nx, ey + W / 2 * ny);
				p.lineTo(ex - L / 2 * ux, ey - L / 2 * uy);
				p.lineTo(ex - W / 2 * nx, ey - W / 2 * ny);
				p.closePath();
				return new Head(p, 0);
			case "oval": {
				Shape o = new Ellipse2D.Double(-L / 2, -W / 2, L, W);
				AffineTransform at = new AffineTransform(ux, uy, nx, ny, ex, ey);
				return new Head(at.createTransformedShape(o), 0);
			}
			case "arrow": {
				/* An open V, its arms the line's width with round ends, its apex mitred, as Word draws it (phase 0: fp31-037
				 * to -045, -058 to -060). From the V's apex, each arm's end is 2s, 3s or 4.5s back and 1.25s, 1.75s or 2.5s
				 * across, for sm, med and lg; the apex is where the arms' outer edges meet at the end; the line stops at
				 * the apex. */
				double lw = Math.max(lineWidth, HAIRLINE), wu = units(e.getW());
				double back = (lu == 2 ? 2 : lu == 3 ? 3 : 4.5) * s, across = (wu == 2 ? 1.25 : wu == 3 ? 1.75 : 2.5) * s;
				double apex = lw / 2 / Math.sin(Math.atan2(across, back));
				double ax = ex - apex * ux, ay = ey - apex * uy;
				Path2D.Double v = new Path2D.Double();
				v.moveTo(ax - back * ux + across * nx, ay - back * uy + across * ny);
				v.lineTo(ax, ay);
				v.lineTo(ax - back * ux - across * nx, ay - back * uy - across * ny);
				Shape sh = new BasicStroke((float) lw, BasicStroke.CAP_ROUND, BasicStroke.JOIN_MITER, 10f).createStrokedShape(v);
				return new Head(sh, apex);
			}
			default:
				return null;
		}
	}

	/** The path flattened to points, for the ends' directions and trimming */
	private static List<double[]> flatten(Shape s) {
		List<double[]> pts = new ArrayList<double[]>();
		double[] c = new double[6];
		for (PathIterator it = new FlatteningPathIterator(s.getPathIterator(null), 0.01, 12); !it.isDone(); it.next()) {
			int t = it.currentSegment(c);
			if (t == PathIterator.SEG_MOVETO && !pts.isEmpty()) break; // the first subpath only
			if (t == PathIterator.SEG_MOVETO || t == PathIterator.SEG_LINETO) {
				double[] last = pts.isEmpty() ? null : pts.get(pts.size() - 1);
				if (last == null || Math.hypot(c[0] - last[0], c[1] - last[1]) > 1e-9) pts.add(new double[] { c[0], c[1] });
			}
		}
		return pts;
	}

	/** An arrowhead's length in points: 2, 3 or 5 times the larger of the line's width and 2pt */
	private static double length(CTLineEndProperties e, double lineWidth) {
		return units(e.getLen()) * Math.max(lineWidth, 2);
	}

	/**
	 * The unit direction toward an end, along the chord from the point the head's length back along the path, so that
	 * on a curve the head's base sits on it (phase 0: fp31-068, -069)
	 */
	private static double[] direction(List<double[]> pts, boolean start, double length) {
		List<double[]> p = new ArrayList<double[]>(pts);
		if (start) Collections.reverse(p);
		double[] end = p.get(p.size() - 1), from = p.get(p.size() - 2);
		double left = length;
		for (int i = p.size() - 1; i > 0; i--) {
			double[] a = p.get(i - 1), b = p.get(i);
			double seg = Math.hypot(b[0] - a[0], b[1] - a[1]);
			if (seg >= left) {
				double t = seg == 0 ? 0 : left / seg;
				from = new double[] { b[0] + (a[0] - b[0]) * t, b[1] + (a[1] - b[1]) * t };
				break;
			}
			left -= seg;
			from = a;
		}
		double dx = end[0] - from[0], dy = end[1] - from[1], len = Math.hypot(dx, dy);
		return len == 0 ? new double[] { 1, 0 } : new double[] { dx / len, dy / len };
	}

	/** The polyline with headBack taken off its start and tailBack off its end */
	private static Shape trim(List<double[]> pts, double headBack, double tailBack) {
		List<double[]> p = new ArrayList<double[]>(pts);
		if (headBack > 0) p = cut(p, headBack);
		if (tailBack > 0) {
			Collections.reverse(p);
			p = cut(p, tailBack);
			Collections.reverse(p);
		}
		Path2D.Double out = new Path2D.Double();
		for (int i = 0; i < p.size(); i++) {
			if (i == 0) out.moveTo(p.get(i)[0], p.get(i)[1]);
			else out.lineTo(p.get(i)[0], p.get(i)[1]);
		}
		return out;
	}

	private static List<double[]> cut(List<double[]> p, double d) {
		double left = d;
		int i = 0;
		while (i < p.size() - 1) {
			double[] a = p.get(i), b = p.get(i + 1);
			double len = Math.hypot(b[0] - a[0], b[1] - a[1]);
			if (len >= left) {
				double t = len == 0 ? 0 : left / len;
				List<double[]> out = new ArrayList<double[]>();
				out.add(new double[] { a[0] + (b[0] - a[0]) * t, a[1] + (b[1] - a[1]) * t });
				out.addAll(p.subList(i + 1, p.size()));
				return out;
			}
			left -= len;
			i++;
		}
		// shorter than the cut: a point at its end
		List<double[]> out = new ArrayList<double[]>();
		out.add(p.get(p.size() - 1));
		out.add(p.get(p.size() - 1));
		return out;
	}

	/**
	 * A path whose every subpath is an axis-aligned rectangle (four corners and a close), each from its bottom left,
	 * then right, up and back: the order of the PDF's re, which Word draws a rectangle with. Any other path as it is.
	 */
	static Path2D.Double asRe(Path2D.Double path) {
		List<double[]> pts = new ArrayList<double[]>();
		Path2D.Double out = new Path2D.Double(path.getWindingRule());
		double[] c = new double[6];
		boolean closed = false;
		for (PathIterator it = path.getPathIterator(null); !it.isDone(); it.next()) {
			int t = it.currentSegment(c);
			if (t == PathIterator.SEG_MOVETO) {
				if (!pts.isEmpty() && !rect(pts, closed, out)) return path;
				pts.clear();
				closed = false;
				pts.add(new double[] { c[0], c[1] });
			} else if (t == PathIterator.SEG_LINETO) {
				pts.add(new double[] { c[0], c[1] });
			} else if (t == PathIterator.SEG_CLOSE) {
				closed = true;
			} else {
				return path;
			}
		}
		if (pts.isEmpty() || !rect(pts, closed, out)) return path;
		return out;
	}

	private static boolean rect(List<double[]> pts, boolean closed, Path2D.Double out) {
		List<double[]> p = new ArrayList<double[]>(pts);
		if (p.size() == 5 && p.get(4)[0] == p.get(0)[0] && p.get(4)[1] == p.get(0)[1]) p.remove(4);
		if (!closed || p.size() != 4) return false;
		double l = Double.MAX_VALUE, r = -Double.MAX_VALUE, t = Double.MAX_VALUE, b = -Double.MAX_VALUE;
		for (double[] q : p) {
			l = Math.min(l, q[0]);
			r = Math.max(r, q[0]);
			t = Math.min(t, q[1]);
			b = Math.max(b, q[1]);
		}
		for (int i = 0; i < 4; i++) {
			double[] a = p.get(i), n = p.get((i + 1) % 4);
			if (a[0] != l && a[0] != r || a[1] != t && a[1] != b) return false;
			if (a[0] != n[0] && a[1] != n[1]) return false; // a diagonal
		}
		if (l == r || t == b) return false;
		out.moveTo(l, b);
		out.lineTo(r, b);
		out.lineTo(r, t);
		out.lineTo(l, t);
		out.closePath();
		return true;
	}

	/** Whether the path's first subpath is left open (a line, an arc) */
	private static boolean isOpen(Shape s) {
		double[] c = new double[6];
		boolean started = false;
		for (PathIterator it = s.getPathIterator(null); !it.isDone(); it.next()) {
			int t = it.currentSegment(c);
			if (t == PathIterator.SEG_MOVETO) {
				if (started) return true;
				started = true;
			} else if (t == PathIterator.SEG_CLOSE) {
				return false;
			}
		}
		return true;
	}
}
