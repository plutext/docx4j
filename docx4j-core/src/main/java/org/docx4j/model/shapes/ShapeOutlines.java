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
import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;
import java.util.Collections;
import java.util.List;

import org.docx4j.dml.STPathFillMode;

/**
 * A shape's geometry evaluated at its size ({@link ShapeGeometry}): its paths, its text rectangle and its connection
 * sites, in points from the shape's top left, y down (CR-080 phase 1).
 * <p>
 * A geometry that can't be drawn as defined is given as the shape's box, filled and stroked, with the box as its text
 * rectangle, and {@link #getProblem()} names why with a stable code (CR-080 D9), for the caller to report.
 *
 * @since 17.3.7
 */
public final class ShapeOutlines {

	/** A {@code prstGeom} whose {@code prst} names no preset docx4j has the definition of. */
	public static final String UNKNOWN_PRESET = "render.shape.unknown_preset";

	/**
	 * A geometry that can't be evaluated: a guide or point that names no guide, an unknown formula, a command with the
	 * wrong number of points, or a path command with no current point.
	 */
	public static final String BAD_GEOMETRY = "render.shape.bad_geometry";

	/** A geometry past one of {@link ShapeGeometry}'s limits against hostile input (CR-080 D10). */
	public static final String OVER_LIMIT = "render.shape.over_limit";

	/** One connection site ({@code a:cxn}): where a connector may attach, and the angle it leaves at. */
	public static final class ConnectionSite {
		private final Point2D.Double position;
		private final double angle;

		ConnectionSite(Point2D.Double position, double angle) {
			this.position = position;
			this.angle = angle;
		}

		/** In points from the shape's top left, y down. */
		public Point2D.Double getPosition() {
			return position;
		}

		/**
		 * In degrees, clockwise from the positive x axis, as {@code ang} gives it; NaN where {@code ang} names no guide, as
		 * {@code curvedLeftArrow}'s {@code cd3} does in ECMA-376's definitions.
		 */
		public double getAngle() {
			return angle;
		}
	}

	private final List<ShapeOutline> outlines;
	private final Rectangle2D.Double textRectangle;
	private final List<ConnectionSite> connectionSites;
	private final String problem;
	private final String detail;

	ShapeOutlines(List<ShapeOutline> outlines, Rectangle2D.Double textRectangle, List<ConnectionSite> connectionSites) {
		this(outlines, textRectangle, connectionSites, null, null);
	}

	private ShapeOutlines(List<ShapeOutline> outlines, Rectangle2D.Double textRectangle, List<ConnectionSite> connectionSites,
			String problem, String detail) {
		this.outlines = Collections.unmodifiableList(outlines);
		this.textRectangle = textRectangle;
		this.connectionSites = Collections.unmodifiableList(connectionSites);
		this.problem = problem;
		this.detail = detail;
	}

	/** The shape's box in place of a geometry that can't be drawn, with the code and what it found. */
	static ShapeOutlines box(double widthPt, double heightPt, String problem, String detail) {
		Path2D.Double box = new Path2D.Double(new Rectangle2D.Double(0, 0, widthPt, heightPt));
		return new ShapeOutlines(Collections.singletonList(new ShapeOutline(box, STPathFillMode.NORM, true, true)),
				new Rectangle2D.Double(0, 0, widthPt, heightPt), Collections.<ConnectionSite>emptyList(), problem, detail);
	}

	/** The paths, in document order. Empty for a shape with no geometry. */
	public List<ShapeOutline> getOutlines() {
		return outlines;
	}

	/** The geometry's text rectangle ({@code a:rect}), or the shape's box where the geometry gives none. */
	public Rectangle2D.Double getTextRectangle() {
		return textRectangle;
	}

	/** The connection sites ({@code a:cxnLst}), in document order. */
	public List<ConnectionSite> getConnectionSites() {
		return connectionSites;
	}

	/**
	 * Null where the geometry was evaluated as defined; otherwise {@link #UNKNOWN_PRESET}, {@link #BAD_GEOMETRY} or
	 * {@link #OVER_LIMIT}, and the outlines are the shape's box.
	 */
	public String getProblem() {
		return problem;
	}

	/** What the problem was found in, for a message; null where there is none. */
	public String getDetail() {
		return detail;
	}
}
