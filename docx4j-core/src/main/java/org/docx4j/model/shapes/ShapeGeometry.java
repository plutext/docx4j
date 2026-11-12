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
package org.docx4j.model.shapes;

import java.awt.geom.AffineTransform;
import java.awt.geom.Path2D;
import java.awt.geom.PathIterator;
import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.docx4j.dml.CTCustomGeometry2D;
import org.docx4j.dml.CTGeomGuide;
import org.docx4j.dml.CTPresetGeometry2D;
import org.docx4j.dml.CTShapeProperties;
import org.docx4j.dml.STShapeType;
import org.docx4j.org.apache.poi.sl.draw.geom.Context;
import org.docx4j.org.apache.poi.sl.draw.geom.GuideIf;
import org.docx4j.org.apache.poi.sl.draw.geom.IAdjustableShape;
import org.docx4j.org.apache.poi.sl.draw.geom.PathIf;

/**
 * A DrawingML shape's geometry, preset ({@code a:prstGeom}) or custom ({@code a:custGeom}), evaluated at the shape's
 * size: its paths, its text rectangle and its connection sites (CR-080 phase 1). The evaluation is Apache POI's
 * geometry engine, repackaged as {@code org.docx4j.org.apache.poi.sl.draw.geom}, with Office's {@code sqrt} of the
 * absolute value ([MS-OI29500] 2.1.1311) and the built-in guides {@code wd12} and {@code hd10}, as Word evaluates them.
 * The presets are docx4j's own definitions ({@link PresetGeometries}).
 * <p>
 * The size is the shape's extent in EMU, and the results are in points from the shape's top left, y down. A path with
 * its own {@code w} and {@code h} is in those coordinates, scaled to the shape, and its guides read that {@code w} and
 * {@code h}, as POI has it; Word's reading of a guide there isn't measured.
 * <p>
 * A geometry that can't be evaluated, or is past a limit against hostile input (CR-080 D10: 1 MiB of text, 1,024
 * guides, 256 paths, 8,192 commands, an arc's sweep of 16 turns, a coordinate within 2<sup>31</sup> EMU), is given as
 * the shape's box, with {@link ShapeOutlines#getProblem()} saying why.
 *
 * @since 17.3.7
 */
public final class ShapeGeometry {

	private static final double EMU_PER_POINT = 12700;

	private static final IAdjustableShape NO_ADJUSTMENTS = name -> null;

	/** Each preset read once, from {@link PresetGeometries}: a {@link GeometryReader.Read}, or the problem as a String. */
	private static final Map<String, Object> PRESETS = new ConcurrentHashMap<String, Object>();

	private ShapeGeometry() {}

	/**
	 * The geometry of a shape's properties, its custom geometry where it has one, else its preset. A shape with neither
	 * has no paths, and its box as its text rectangle.
	 *
	 * @param cx the shape's width in EMU
	 * @param cy the shape's height in EMU
	 */
	public static ShapeOutlines outlines(CTShapeProperties spPr, long cx, long cy) {
		if (spPr != null && spPr.getCustGeom() != null) return outlines(spPr.getCustGeom(), cx, cy);
		if (spPr != null && spPr.getPrstGeom() != null) return outlines(spPr.getPrstGeom(), cx, cy);
		return new ShapeOutlines(Collections.<ShapeOutline>emptyList(), new Rectangle2D.Double(0, 0, cx / EMU_PER_POINT, cy / EMU_PER_POINT),
				Collections.<ShapeOutlines.ConnectionSite>emptyList());
	}

	/** A preset geometry, with its adjustments, at the shape's size in EMU. */
	public static ShapeOutlines outlines(CTPresetGeometry2D prstGeom, long cx, long cy) {
		STShapeType type = prstGeom.getPrst();
		if (type == null) return box(cx, cy, ShapeOutlines.UNKNOWN_PRESET, "a prstGeom with no prst");
		Object preset = preset(type.value());
		if (preset instanceof String) return box(cx, cy, ShapeOutlines.UNKNOWN_PRESET, (String) preset);
		final Map<String, GuideIf> adjustments;
		try {
			adjustments = GeometryReader.adjustments(prstGeom.getAvLst());
		} catch (GeometryReader.OverLimit e) {
			return box(cx, cy, ShapeOutlines.OVER_LIMIT, type.value() + "'s adjustments: " + e.getMessage());
		} catch (RuntimeException e) {
			return box(cx, cy, ShapeOutlines.BAD_GEOMETRY, type.value() + "'s adjustments: " + e.getMessage());
		}
		return evaluate((GeometryReader.Read) preset, adjustments.isEmpty() ? NO_ADJUSTMENTS : adjustments::get, cx, cy, type.value());
	}

	/** A custom geometry at the shape's size in EMU. */
	public static ShapeOutlines outlines(CTCustomGeometry2D custGeom, long cx, long cy) {
		final GeometryReader.Read read;
		try {
			read = GeometryReader.read(custGeom);
		} catch (GeometryReader.OverLimit e) {
			return box(cx, cy, ShapeOutlines.OVER_LIMIT, "a custGeom: " + e.getMessage());
		} catch (RuntimeException e) {
			return box(cx, cy, ShapeOutlines.BAD_GEOMETRY, "a custGeom: " + e.getMessage());
		}
		return evaluate(read, NO_ADJUSTMENTS, cx, cy, "a custGeom");
	}

	/** Each text warp read once: a {@link GeometryReader.Read}, or the problem as a String (CR-083). */
	private static final Map<String, Object> WARPS = new ConcurrentHashMap<String, Object>();

	/**
	 * The text warps whose {@code ss} Word reads as the height, not min(w, h) as the shipped definitions have it (CR-083
	 * phase 0, findings section 3: textDeflateBottom in a tall box from 3.90 to 0.38 pt, textDeflate from 1.59 to 0.16).
	 */
	static final java.util.Set<String> SS_IS_HEIGHT = Collections.unmodifiableSet(new java.util.HashSet<String>(
			java.util.Arrays.asList("textDeflate", "textDeflateBottom")));

	/**
	 * A text warp ({@code a:prstTxWarp}, CR-083 phase 2) evaluated on the text rectangle, w by h points, with its
	 * adjustments: its paths in points from the rectangle's top left, y down, in the definition's order (for a
	 * two-path warp, the top path then the bottom). Word evaluates a warp on the text rectangle (the box less its
	 * insets), not the box, and reads {@code ss} as the height in {@link #SS_IS_HEIGHT}. {@code textNoShape} has no paths.
	 * A warp that can't be evaluated is the rectangle, with {@link ShapeOutlines#getProblem()} saying why.
	 *
	 * @since 17.3.7
	 */
	public static ShapeOutlines textWarp(org.docx4j.dml.CTPresetTextShape prstTxWarp, double w, double h) {
		long cx = Math.round(w * EMU_PER_POINT), cy = Math.round(h * EMU_PER_POINT);
		org.docx4j.dml.STTextShapeType type = prstTxWarp == null ? null : prstTxWarp.getPrst();
		if (type == null) return box(cx, cy, ShapeOutlines.UNKNOWN_PRESET, "a prstTxWarp with no prst");
		Object warp = warp(type.value());
		if (warp instanceof String) return box(cx, cy, ShapeOutlines.UNKNOWN_PRESET, (String) warp);
		final Map<String, GuideIf> adjustments;
		try {
			adjustments = GeometryReader.adjustments(prstTxWarp.getAvLst());
		} catch (GeometryReader.OverLimit e) {
			return box(cx, cy, ShapeOutlines.OVER_LIMIT, type.value() + "'s adjustments: " + e.getMessage());
		} catch (RuntimeException e) {
			return box(cx, cy, ShapeOutlines.BAD_GEOMETRY, type.value() + "'s adjustments: " + e.getMessage());
		}
		return evaluate((GeometryReader.Read) warp, adjustments.isEmpty() ? NO_ADJUSTMENTS : adjustments::get, cx, cy, type.value());
	}

	private static Object warp(String name) {
		Object found = WARPS.get(name);
		if (found != null) return found;
		CTCustomGeometry2D definition = PresetGeometries.getTextWarps().get(name);
		if (definition == null) {
			found = "no definition of the text warp " + name;
		} else {
			try {
				if (SS_IS_HEIGHT.contains(name)) {
					definition = org.docx4j.XmlUtils.deepCopy(definition);
					if (definition.getGdLst() != null) {
						for (CTGeomGuide gd : definition.getGdLst().getGd()) {
							if (gd.getFmla() != null) gd.setFmla(gd.getFmla().replaceAll("\\bss\\b", "h"));
						}
					}
				}
				found = GeometryReader.read(definition);
			} catch (RuntimeException e) {
				found = "the text warp " + name + "'s definition: " + e.getMessage();
			}
		}
		WARPS.put(name, found);
		return found;
	}

	private static Object preset(String name) {
		Object found = PRESETS.get(name);
		if (found != null) return found;
		CTCustomGeometry2D definition = PresetGeometries.getInstance().get(name);
		if (definition == null) {
			found = "no definition of the preset " + name;
		} else {
			try {
				found = GeometryReader.read(definition);
			} catch (RuntimeException e) {
				found = "the preset " + name + "'s definition: " + e.getMessage();
			}
		}
		PRESETS.put(name, found);
		return found;
	}

	private static ShapeOutlines box(long cx, long cy, String problem, String detail) {
		return ShapeOutlines.box(cx / EMU_PER_POINT, cy / EMU_PER_POINT, problem, detail);
	}

	private static ShapeOutlines evaluate(GeometryReader.Read read, IAdjustableShape adjust, long cx, long cy, String name) {
		try {
			Context shape = new Context(read.geometry, new Rectangle2D.Double(0, 0, cx, cy), adjust);
			Map<String, Context> spaces = new HashMap<String, Context>();
			List<ShapeOutline> outlines = new ArrayList<ShapeOutline>();
			for (PathIf p : read.geometry) {
				// a path with its own w and h is in those coordinates, which its guides read as w and h (POI's reading);
				// one without is in the shape's, EMU
				boolean ownW = p.getW() > 0, ownH = p.getH() > 0;
				double w = ownW ? p.getW() : cx, h = ownH ? p.getH() : cy;
				Context ctx = shape;
				if (ownW || ownH) {
					ctx = spaces.computeIfAbsent(w + " " + h, k -> new Context(read.geometry, new Rectangle2D.Double(0, 0, w, h), adjust));
				}
				double toEmuX = ownW ? cx / w : 1, toEmuY = ownH ? cy / h : 1;
				Path2D.Double path = normalise(p.getPath(ctx));
				within(path, toEmuX, toEmuY);
				path.transform(AffineTransform.getScaleInstance(toEmuX / EMU_PER_POINT, toEmuY / EMU_PER_POINT));
				outlines.add(new ShapeOutline(path, GeometryReader.fill(p.getFill()), p.isStroked(), p.isExtrusionOk()));
			}
			Rectangle2D.Double text = new Rectangle2D.Double(0, 0, cx / EMU_PER_POINT, cy / EMU_PER_POINT);
			if (read.geometry.getTextBounds() != null) {
				Path2D.Double bounds = read.geometry.getTextBounds().getPath(shape);
				within(bounds, 1, 1);
				Rectangle2D b = bounds.getBounds2D();
				text = new Rectangle2D.Double(b.getX() / EMU_PER_POINT, b.getY() / EMU_PER_POINT, b.getWidth() / EMU_PER_POINT,
						b.getHeight() / EMU_PER_POINT);
			}
			List<ShapeOutlines.ConnectionSite> sites = new ArrayList<ShapeOutlines.ConnectionSite>();
			for (GeometryReader.Site s : read.sites) {
				double x = shape.getValue(s.x), y = shape.getValue(s.y);
				coordinate(x);
				coordinate(y);
				sites.add(new ShapeOutlines.ConnectionSite(new Point2D.Double(x / EMU_PER_POINT, y / EMU_PER_POINT), angle(shape, s.ang)));
			}
			return new ShapeOutlines(outlines, text, sites);
		} catch (GeometryReader.OverLimit e) {
			return box(cx, cy, ShapeOutlines.OVER_LIMIT, name + ": " + e.getMessage());
		} catch (RuntimeException e) {
			return box(cx, cy, ShapeOutlines.BAD_GEOMETRY, name + ": " + e);
		}
	}

	/**
	 * The path as Word draws it (CR-080 phase 2, from Word's PDFs of phase 0's presets):
	 * <ul>
	 * <li>A line of no length goes. The engine appends each arc with a line to its start, and a stroke's cap or join
	 * then formed round it ({@code rightBrace}, {@code moon}, the curved arrows).</li>
	 * <li>A close straight after a move, with more drawn after it, closes the subpath at its end: the callouts' accent
	 * bar is {@code moveTo}, {@code close}, {@code lnTo} in ECMA-376's definitions, and Word draws it as a closed
	 * line. A close after a drawing closes as it is.</li>
	 * </ul>
	 */
	static Path2D.Double normalise(Path2D.Double path) {
		Path2D.Double out = new Path2D.Double(path.getWindingRule());
		double[] c = new double[6];
		double px = 0, py = 0;
		boolean pendingClose = false, drawnSinceMove = false;
		for (PathIterator it = path.getPathIterator(null); !it.isDone(); it.next()) {
			int t = it.currentSegment(c);
			switch (t) {
				case PathIterator.SEG_MOVETO:
					if (pendingClose) out.closePath();
					pendingClose = false;
					drawnSinceMove = false;
					out.moveTo(c[0], c[1]);
					px = c[0];
					py = c[1];
					break;
				case PathIterator.SEG_LINETO:
					if (Math.abs(c[0] - px) < 1e-6 && Math.abs(c[1] - py) < 1e-6) break;
					out.lineTo(c[0], c[1]);
					drawnSinceMove = true;
					px = c[0];
					py = c[1];
					break;
				case PathIterator.SEG_QUADTO:
					out.quadTo(c[0], c[1], c[2], c[3]);
					drawnSinceMove = true;
					px = c[2];
					py = c[3];
					break;
				case PathIterator.SEG_CUBICTO:
					out.curveTo(c[0], c[1], c[2], c[3], c[4], c[5]);
					drawnSinceMove = true;
					px = c[4];
					py = c[5];
					break;
				case PathIterator.SEG_CLOSE:
					// a close straight after the move closes what follows it; one after a drawing closes as it is
					if (drawnSinceMove) {
						out.closePath();
						drawnSinceMove = false;
					} else {
						pendingClose = true;
					}
					break;
				default:
					break;
			}
		}
		if (pendingClose) out.closePath();
		return out;
	}

	/**
	 * A connection site's angle in degrees, or NaN where it names no guide: {@code curvedLeftArrow}'s {@code cd3}, which
	 * ECMA-376's definitions use and don't define. A site isn't drawn, so the shape is.
	 */
	private static double angle(Context shape, String ang) {
		double a;
		try {
			a = shape.getValue(ang);
		} catch (IllegalArgumentException undefined) {
			return Double.NaN;
		}
		return Double.isFinite(a) ? a / 60000 : Double.NaN;
	}

	/** Each coordinate of the path, in EMU in the shape's space, finite and within {@link GeometryReader#MAX_COORDINATE}. */
	private static void within(Path2D.Double path, double toEmuX, double toEmuY) {
		double[] c = new double[6];
		for (PathIterator it = path.getPathIterator(null); !it.isDone(); it.next()) {
			int n;
			switch (it.currentSegment(c)) {
				case PathIterator.SEG_MOVETO: case PathIterator.SEG_LINETO: n = 2; break;
				case PathIterator.SEG_QUADTO: n = 4; break;
				case PathIterator.SEG_CUBICTO: n = 6; break;
				default: n = 0;
			}
			for (int i = 0; i < n; i += 2) {
				coordinate(c[i] * toEmuX);
				coordinate(c[i + 1] * toEmuY);
			}
		}
	}

	private static void coordinate(double emu) {
		if (!(Math.abs(emu) <= GeometryReader.MAX_COORDINATE)) throw new GeometryReader.OverLimit("a coordinate of " + emu + " EMU");
	}
}
