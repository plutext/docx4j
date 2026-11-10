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

import java.awt.geom.Path2D;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.docx4j.dml.CTAdjPoint2D;
import org.docx4j.dml.CTConnectionSite;
import org.docx4j.dml.CTCustomGeometry2D;
import org.docx4j.dml.CTGeomGuide;
import org.docx4j.dml.CTGeomGuideList;
import org.docx4j.dml.CTGeomRect;
import org.docx4j.dml.CTPath2D;
import org.docx4j.dml.CTPath2DArcTo;
import org.docx4j.dml.CTPath2DClose;
import org.docx4j.dml.CTPath2DCubicBezierTo;
import org.docx4j.dml.CTPath2DLineTo;
import org.docx4j.dml.CTPath2DMoveTo;
import org.docx4j.dml.CTPath2DQuadBezierTo;
import org.docx4j.dml.STPathFillMode;
import org.docx4j.org.apache.poi.sl.draw.geom.AdjustPoint;
import org.docx4j.org.apache.poi.sl.draw.geom.AdjustValue;
import org.docx4j.org.apache.poi.sl.draw.geom.ArcToCommand;
import org.docx4j.org.apache.poi.sl.draw.geom.ClosePathCommand;
import org.docx4j.org.apache.poi.sl.draw.geom.Context;
import org.docx4j.org.apache.poi.sl.draw.geom.CurveToCommand;
import org.docx4j.org.apache.poi.sl.draw.geom.CustomGeometry;
import org.docx4j.org.apache.poi.sl.draw.geom.Guide;
import org.docx4j.org.apache.poi.sl.draw.geom.GuideIf;
import org.docx4j.org.apache.poi.sl.draw.geom.LineToCommand;
import org.docx4j.org.apache.poi.sl.draw.geom.MoveToCommand;
import org.docx4j.org.apache.poi.sl.draw.geom.Path;
import org.docx4j.org.apache.poi.sl.draw.geom.QuadToCommand;
import org.docx4j.org.apache.poi.sl.usermodel.PaintStyle.PaintModifier;

/**
 * docx4j's JAXB geometry ({@code CTCustomGeometry2D}, a custom geometry's or a preset's definition) read into the
 * repackaged POI engine's {@link CustomGeometry}, within the limits of CR-080 D10.
 */
final class GeometryReader {

	/** Characters in the geometry's names, formulas and points: the 1 MiB of geometry XML of D10, counted once parsed. */
	static final int MAX_TEXT = 1 << 20;

	/** Adjust values and guides, a shape's adjustments included: each is evaluated once a coordinate space. */
	static final int MAX_GUIDES = 1024;

	static final int MAX_PATHS = 256;

	/** Path commands, over all of a geometry's paths. */
	static final int MAX_COMMANDS = 8192;

	/** An arc's sweep: 16 turns, in 60,000ths of a degree. */
	static final double MAX_SWEEP = 16 * 21600000d;

	/** A coordinate, in EMU in the shape's space: a 32-bit coordinate, beyond any page. */
	static final double MAX_COORDINATE = Integer.MAX_VALUE;

	/** A geometry past a limit. */
	static final class OverLimit extends RuntimeException {
		private static final long serialVersionUID = 1L;

		OverLimit(String what) {
			super(what, null, false, false);
		}
	}

	/** A geometry that can't be read as one: a missing or ill-formed part. */
	static final class Bad extends RuntimeException {
		private static final long serialVersionUID = 1L;

		Bad(String what) {
			super(what, null, false, false);
		}
	}

	/** A connection site's point and angle, as guide names or numbers, for the evaluation. */
	static final class Site {
		final String x, y, ang;

		Site(String x, String y, String ang) {
			this.x = x;
			this.y = y;
			this.ang = ang;
		}
	}

	/** A geometry read: POI's, with what it keeps to itself, the connection sites. Not changed after it is read. */
	static final class Read {
		final CustomGeometry geometry = new CustomGeometry();
		final List<Site> sites = new ArrayList<Site>();
	}

	/** An arc that refuses a sweep past {@link #MAX_SWEEP} before it is drawn (D10). */
	static final class BoundedArcTo extends ArcToCommand {
		@Override
		public void execute(Path2D.Double path, Context ctx) {
			double sweep = ctx.getValue(getSwAng());
			if (Double.isNaN(sweep) || Math.abs(sweep) > MAX_SWEEP) throw new OverLimit("an arc's sweep of " + sweep);
			super.execute(path, ctx);
		}
	}

	private int text, guides, commands;

	private GeometryReader() {}

	/** A custom geometry, or a preset's definition. */
	static Read read(CTCustomGeometry2D g) {
		return new GeometryReader().readGeometry(g);
	}

	/** A preset shape's adjustments ({@code a:prstGeom/a:avLst}), by name, to stand in for the definition's. */
	static Map<String, GuideIf> adjustments(CTGeomGuideList avLst) {
		Map<String, GuideIf> found = new HashMap<String, GuideIf>();
		if (avLst == null) return found;
		GeometryReader r = new GeometryReader();
		for (CTGeomGuide gd : avLst.getGd()) {
			Guide guide = new Guide();
			r.guide(gd, guide);
			found.put(guide.getName(), guide);
		}
		return found;
	}

	private Read readGeometry(CTCustomGeometry2D g) {
		Read read = new Read();
		CustomGeometry geom = read.geometry;
		if (g.getAvLst() != null) {
			for (CTGeomGuide gd : g.getAvLst().getGd()) {
				AdjustValue av = new AdjustValue();
				guide(gd, av);
				geom.addAdjustGuide(av);
			}
		}
		if (g.getGdLst() != null) {
			for (CTGeomGuide gd : g.getGdLst().getGd()) {
				Guide guide = new Guide();
				guide(gd, guide);
				geom.addGeomGuide(guide);
			}
		}
		if (g.getCxnLst() != null) {
			for (CTConnectionSite cxn : g.getCxnLst().getCxn()) {
				CTAdjPoint2D pos = cxn.getPos();
				if (pos == null) throw new Bad("a connection site with no position");
				read.sites.add(new Site(text(pos.getX(), "a connection site's x"), text(pos.getY(), "a connection site's y"),
						text(cxn.getAng(), "a connection site's angle")));
			}
		}
		CTGeomRect rect = g.getRect();
		if (rect != null) {
			geom.setTextBounds(text(rect.getL(), "the text rectangle's l"), text(rect.getT(), "the text rectangle's t"),
					text(rect.getR(), "the text rectangle's r"), text(rect.getB(), "the text rectangle's b"));
		}
		if (g.getPathLst() != null) {
			List<CTPath2D> paths = g.getPathLst().getPath();
			if (paths.size() > MAX_PATHS) throw new OverLimit(paths.size() + " paths");
			for (CTPath2D p : paths) geom.addPath(path(p));
		}
		return read;
	}

	private void guide(CTGeomGuide gd, Guide guide) {
		if (++guides > MAX_GUIDES) throw new OverLimit("more than " + MAX_GUIDES + " guides");
		guide.setName(text(gd.getName(), "a guide's name"));
		guide.setFmla(text(gd.getFmla(), "a guide's formula"));
	}

	private Path path(CTPath2D p) {
		Path path = new Path();
		// JAXB reads an absent w or h as 0; ShapeGeometry takes 0 or less as not stated, the path in the shape's coordinates
		path.setW(p.getW());
		path.setH(p.getH());
		path.setFill(fill(p.getFill()));
		path.setStroke(p.isStroke());
		path.setExtrusionOk(p.isExtrusionOk());
		for (Object o : p.getCloseOrMoveToOrLnTo()) {
			if (++commands > MAX_COMMANDS) throw new OverLimit("more than " + MAX_COMMANDS + " path commands");
			if (o instanceof CTPath2DMoveTo) {
				MoveToCommand cmd = new MoveToCommand();
				cmd.setPt(point(((CTPath2DMoveTo) o).getPt()));
				path.addCommand(cmd);
			} else if (o instanceof CTPath2DLineTo) {
				LineToCommand cmd = new LineToCommand();
				cmd.setPt(point(((CTPath2DLineTo) o).getPt()));
				path.addCommand(cmd);
			} else if (o instanceof CTPath2DArcTo) {
				CTPath2DArcTo a = (CTPath2DArcTo) o;
				BoundedArcTo cmd = new BoundedArcTo();
				cmd.setWR(text(a.getWR(), "an arc's wR"));
				cmd.setHR(text(a.getHR(), "an arc's hR"));
				cmd.setStAng(text(a.getStAng(), "an arc's stAng"));
				cmd.setSwAng(text(a.getSwAng(), "an arc's swAng"));
				path.addCommand(cmd);
			} else if (o instanceof CTPath2DQuadBezierTo) {
				List<CTAdjPoint2D> pts = ((CTPath2DQuadBezierTo) o).getPt();
				if (pts.size() != 2) throw new Bad("a quadBezTo with " + pts.size() + " points");
				QuadToCommand cmd = new QuadToCommand();
				cmd.setPt1(point(pts.get(0)));
				cmd.setPt2(point(pts.get(1)));
				path.addCommand(cmd);
			} else if (o instanceof CTPath2DCubicBezierTo) {
				List<CTAdjPoint2D> pts = ((CTPath2DCubicBezierTo) o).getPt();
				if (pts.size() != 3) throw new Bad("a cubicBezTo with " + pts.size() + " points");
				CurveToCommand cmd = new CurveToCommand();
				cmd.setPt1(point(pts.get(0)));
				cmd.setPt2(point(pts.get(1)));
				cmd.setPt3(point(pts.get(2)));
				path.addCommand(cmd);
			} else if (o instanceof CTPath2DClose) {
				path.addCommand(new ClosePathCommand());
			}
		}
		return path;
	}

	private AdjustPoint point(CTAdjPoint2D pt) {
		if (pt == null) throw new Bad("a path command with no point");
		AdjustPoint p = new AdjustPoint();
		p.setX(text(pt.getX(), "a point's x"));
		p.setY(text(pt.getY(), "a point's y"));
		return p;
	}

	private String text(String s, String what) {
		if (s == null) throw new Bad(what + " missing");
		text += s.length();
		if (text > MAX_TEXT) throw new OverLimit("more than " + MAX_TEXT + " characters of geometry");
		return s;
	}

	static PaintModifier fill(STPathFillMode mode) {
		if (mode == null) return PaintModifier.NORM;
		switch (mode) {
			case NONE: return PaintModifier.NONE;
			case LIGHTEN: return PaintModifier.LIGHTEN;
			case LIGHTEN_LESS: return PaintModifier.LIGHTEN_LESS;
			case DARKEN: return PaintModifier.DARKEN;
			case DARKEN_LESS: return PaintModifier.DARKEN_LESS;
			default: return PaintModifier.NORM;
		}
	}

	static STPathFillMode fill(PaintModifier mode) {
		if (mode == null) return STPathFillMode.NORM;
		switch (mode) {
			case NONE: return STPathFillMode.NONE;
			case LIGHTEN: return STPathFillMode.LIGHTEN;
			case LIGHTEN_LESS: return STPathFillMode.LIGHTEN_LESS;
			case DARKEN: return STPathFillMode.DARKEN;
			case DARKEN_LESS: return STPathFillMode.DARKEN_LESS;
			default: return STPathFillMode.NORM;
		}
	}
}
