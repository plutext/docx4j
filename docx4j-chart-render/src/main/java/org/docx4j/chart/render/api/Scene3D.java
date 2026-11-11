/*
 *  Copyright 2026, Plutext Pty Ltd.
 *
 *  This file is part of docx4j.
 *
 *  docx4j is licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *
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
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.Shape;
import java.awt.geom.AffineTransform;
import java.awt.geom.PathIterator;
import java.awt.geom.Rectangle2D;
import java.awt.image.BufferedImage;
import java.util.ArrayList;
import java.util.Collections;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;

import org.docx4j.dml.CTBevel;
import org.docx4j.dml.CTCamera;
import org.docx4j.dml.CTScene3D;
import org.docx4j.dml.CTShape3D;
import org.docx4j.dml.STBevelPresetType;
import org.docx4j.dml.STLightRigDirection;
import org.docx4j.dml.STPresetCameraType;
import org.docx4j.model.color.DrawingMLColors;
import org.docx4j.model.color.ThemeColors;

/**
 * A 3-D drawing as Word draws it (CR-081 phase 5a): one picture with alpha, in place of the drawing, its geometry
 * Word's and its shading simple.
 *
 * <p>The geometry, as Word's PDFs read (CR-081-notes/phase5a-findings.md):</p>
 * <ul>
 * <li>The drawing's frame, points from its centre, y down and z towards the viewer. Its front face is at z = 0. A top
 * bevel cuts into it: the face is inset by the bevel's width, and slopes back to z = -h at the outline along the
 * bevel's profile. The extrusion goes on behind that, to z = -(h + depth), then a bottom bevel.</li>
 * <li>The camera turns it about its centre: by lon about the vertical axis (the right side comes forward), then by
 * lat about the horizontal (the top comes forward), then by rev in the picture's plane, counter-clockwise.</li>
 * <li>A perspective camera scales a point at depth z by K D / (D - z), where D = C / tan(fov / 2); C and K are
 * fitted (phase 5a findings section 1), and D doesn't depend on the drawing's size.</li>
 * <li>An oblique camera shears depth towards the corner it names; zoom scales about the centre.</li>
 * </ul>
 *
 * <p>The shading isn't Word's (phase 5b): a diffuse light from the light rig's direction, every material matte,
 * normalised so that a face turned to the viewer keeps its colour. Where it would show, {@link #SHADING} is reported.</p>
 *
 * @since 17.3.7 (CR-081 phase 5a)
 */
public final class Scene3D {

	/** A 3-D drawing drawn with its geometry and a simple shading, not Word's lights and materials (phase 5b) */
	public static final String SHADING = "render.effect.3d_shading";

	/** The perspective constant: D tan(fov / 2), points (phase 5a findings section 1) */
	static final double C = 452.85;
	/** The perspective's overall scale */
	static final double K = 1.0011;

	private Scene3D() {
	}

	/** The picture: non-premultiplied ARGB, placed in the drawing's frame (points from its box's top left) */
	public static final class Picture {
		private final BufferedImage image;
		private final Rectangle2D.Double place;

		Picture(BufferedImage image, Rectangle2D.Double place) {
			this.image = image;
			this.place = place;
		}

		public BufferedImage getImage() {
			return image;
		}

		public Rectangle2D.Double getPlace() {
			return place;
		}

		/** It, painted where it goes */
		public Effects.Painter painter() {
			return g -> g.drawImage(image, new AffineTransform(place.width / image.getWidth(), 0, 0, place.height / image.getHeight(),
					place.x, place.y), null);
		}
	}

	// ------------------------------------------------------------------------------------------------------ the camera

	/** A camera preset's turn and projection */
	static final class Preset {
		final boolean perspective;
		final double lat, lon, rev, fov;
		/** an oblique's shear of depth, x and y per point of depth behind the face (towards -z) */
		final double obliqueX, obliqueY;
		/** a legacy perspective's: its distance (0 for none) and its eye's offset, points */
		final double distance, eyeX, eyeY;

		Preset(boolean perspective, double lat, double lon, double rev, double fov, double obliqueX, double obliqueY, double distance,
				double eyeX, double eyeY) {
			this.perspective = perspective;
			this.lat = lat;
			this.lon = lon;
			this.rev = rev;
			this.fov = fov;
			this.obliqueX = obliqueX;
			this.obliqueY = obliqueY;
			this.distance = distance;
			this.eyeX = eyeX;
			this.eyeY = eyeY;
		}
	}

	private static final Map<STPresetCameraType, Preset> PRESETS = new EnumMap<STPresetCameraType, Preset>(STPresetCameraType.class);

	private static void parallel(STPresetCameraType t, double lat, double lon, double rev) {
		PRESETS.put(t, new Preset(false, lat, lon, rev, 0, 0, 0, 0, 0, 0));
	}

	private static void perspective(STPresetCameraType t, double lat, double lon, double rev, double fov) {
		PRESETS.put(t, new Preset(true, lat, lon, rev, fov, 0, 0, 0, 0, 0));
	}

	/** An oblique: parallel, its depth sheared towards the corner it names */
	private static void oblique(STPresetCameraType t, double x, double y) {
		PRESETS.put(t, new Preset(false, 0, 0, 0, 0, x, y, 0, 0, 0));
	}

	/** A legacy perspective: a camera at that distance, its eye moved towards the side it names */
	private static void eye(STPresetCameraType t, double x, double y, double distance) {
		PRESETS.put(t, new Preset(true, 0, 0, 0, 0, 0, 0, distance, x, y));
	}

	/*
	 * Fitted to every Word picture of each preset at once (phase 5a findings section 2): round 1's two flat shapes and
	 * round 2's shape extruded 36 pt, each silhouette against the hull of the model's box, as Scene3DTest scores it
	 * (phase5a-presets.tsv, written by fit_all.py; these lines are generated from it). A shear is per point of depth
	 * behind the face: 0.30 for an oblique, 0.50 for a legacy oblique. A legacy perspective's camera is about 720 pt away,
	 * its eye 174 pt across and 156 pt up or down towards the side it names. Every preset fits within 0.42 pt.
	 */
	static {
		oblique(STPresetCameraType.LEGACY_OBLIQUE_TOP_LEFT, -0.3602, -0.3613);
		oblique(STPresetCameraType.LEGACY_OBLIQUE_TOP, 0.0000, -0.5012);
		oblique(STPresetCameraType.LEGACY_OBLIQUE_TOP_RIGHT, 0.3603, -0.3612);
		oblique(STPresetCameraType.LEGACY_OBLIQUE_LEFT, -0.5003, 0.0000);
		oblique(STPresetCameraType.LEGACY_OBLIQUE_FRONT, 0.0000, 0.0000);
		oblique(STPresetCameraType.LEGACY_OBLIQUE_RIGHT, 0.5003, 0.0000);
		oblique(STPresetCameraType.LEGACY_OBLIQUE_BOTTOM_LEFT, -0.3604, 0.3595);
		oblique(STPresetCameraType.LEGACY_OBLIQUE_BOTTOM, 0.0000, 0.4995);
		oblique(STPresetCameraType.LEGACY_OBLIQUE_BOTTOM_RIGHT, 0.3605, 0.3594);
		eye(STPresetCameraType.LEGACY_PERSPECTIVE_TOP_LEFT, -174.0, -157.0, 719.6);
		eye(STPresetCameraType.LEGACY_PERSPECTIVE_TOP, 0.0, -156.9, 718.7);
		eye(STPresetCameraType.LEGACY_PERSPECTIVE_TOP_RIGHT, 174.1, -157.1, 720.6);
		eye(STPresetCameraType.LEGACY_PERSPECTIVE_LEFT, -174.5, 0.0, 723.7);
		eye(STPresetCameraType.LEGACY_PERSPECTIVE_FRONT, 0.0, 0.0, 700.0);
		eye(STPresetCameraType.LEGACY_PERSPECTIVE_RIGHT, 174.5, 0.0, 723.6);
		eye(STPresetCameraType.LEGACY_PERSPECTIVE_BOTTOM_LEFT, -174.3, 156.0, 721.8);
		eye(STPresetCameraType.LEGACY_PERSPECTIVE_BOTTOM, 0.0, 155.4, 717.3);
		eye(STPresetCameraType.LEGACY_PERSPECTIVE_BOTTOM_RIGHT, 173.8, 155.6, 718.4);
		parallel(STPresetCameraType.ORTHOGRAPHIC_FRONT, 0.000, 0.000, 0.000);
		parallel(STPresetCameraType.ISOMETRIC_TOP_UP, -35.353, -45.188, 60.093);
		parallel(STPresetCameraType.ISOMETRIC_TOP_DOWN, -35.363, 45.183, -60.093);
		parallel(STPresetCameraType.ISOMETRIC_BOTTOM_UP, 35.381, 45.203, 60.108);
		parallel(STPresetCameraType.ISOMETRIC_BOTTOM_DOWN, 35.373, -45.206, -60.107);
		parallel(STPresetCameraType.ISOMETRIC_LEFT_UP, -35.029, 44.679, 0.099);
		parallel(STPresetCameraType.ISOMETRIC_LEFT_DOWN, 35.034, 44.677, -0.096);
		parallel(STPresetCameraType.ISOMETRIC_RIGHT_UP, 35.030, -44.677, 0.100);
		parallel(STPresetCameraType.ISOMETRIC_RIGHT_DOWN, -35.029, -44.680, -0.096);
		parallel(STPresetCameraType.ISOMETRIC_OFF_AXIS_1_LEFT, 18.026, 63.850, -0.065);
		parallel(STPresetCameraType.ISOMETRIC_OFF_AXIS_1_RIGHT, 18.013, -25.826, 0.042);
		parallel(STPresetCameraType.ISOMETRIC_OFF_AXIS_1_TOP, -58.642, -53.256, 57.511);
		parallel(STPresetCameraType.ISOMETRIC_OFF_AXIS_2_LEFT, 17.984, 25.825, -0.049);
		parallel(STPresetCameraType.ISOMETRIC_OFF_AXIS_2_RIGHT, 18.034, -63.852, 0.057);
		parallel(STPresetCameraType.ISOMETRIC_OFF_AXIS_2_TOP, -58.639, 53.264, -57.517);
		parallel(STPresetCameraType.ISOMETRIC_OFF_AXIS_3_LEFT, -18.003, 63.848, 0.084);
		parallel(STPresetCameraType.ISOMETRIC_OFF_AXIS_3_RIGHT, -17.965, -25.833, -0.052);
		parallel(STPresetCameraType.ISOMETRIC_OFF_AXIS_3_BOTTOM, 58.646, -53.251, -57.510);
		parallel(STPresetCameraType.ISOMETRIC_OFF_AXIS_4_LEFT, -17.930, 25.832, 0.062);
		parallel(STPresetCameraType.ISOMETRIC_OFF_AXIS_4_RIGHT, -18.011, -63.849, -0.077);
		parallel(STPresetCameraType.ISOMETRIC_OFF_AXIS_4_BOTTOM, 58.638, 53.257, 57.511);
		oblique(STPresetCameraType.OBLIQUE_TOP_LEFT, -0.2136, -0.2145);
		oblique(STPresetCameraType.OBLIQUE_TOP, 0.0000, -0.3012);
		oblique(STPresetCameraType.OBLIQUE_TOP_RIGHT, 0.2138, -0.2144);
		oblique(STPresetCameraType.OBLIQUE_LEFT, -0.3003, 0.0000);
		oblique(STPresetCameraType.OBLIQUE_RIGHT, 0.3003, 0.0000);
		oblique(STPresetCameraType.OBLIQUE_BOTTOM_LEFT, -0.2135, 0.2130);
		oblique(STPresetCameraType.OBLIQUE_BOTTOM, 0.0000, 0.2995);
		oblique(STPresetCameraType.OBLIQUE_BOTTOM_RIGHT, 0.2139, 0.2127);
		perspective(STPresetCameraType.PERSPECTIVE_FRONT, 0.000, 0.000, 0.000, 45.00);
		perspective(STPresetCameraType.PERSPECTIVE_LEFT, 0.052, 19.776, 0.011, 44.49);
		perspective(STPresetCameraType.PERSPECTIVE_RIGHT, 0.051, -19.775, -0.012, 44.49);
		perspective(STPresetCameraType.PERSPECTIVE_ABOVE, -19.777, 0.063, -0.012, 43.45);
		perspective(STPresetCameraType.PERSPECTIVE_BELOW, 19.920, 0.061, 0.012, 44.37);
		perspective(STPresetCameraType.PERSPECTIVE_ABOVE_LEFT_FACING, 39.302, 14.255, -18.903, 45.43);
		perspective(STPresetCameraType.PERSPECTIVE_ABOVE_RIGHT_FACING, 39.303, -14.290, 18.889, 45.59);
		perspective(STPresetCameraType.PERSPECTIVE_CONTRASTING_LEFT_FACING, 10.432, 43.913, -3.549, 45.43);
		perspective(STPresetCameraType.PERSPECTIVE_CONTRASTING_RIGHT_FACING, 10.445, -43.917, 3.540, 45.46);
		perspective(STPresetCameraType.PERSPECTIVE_HEROIC_LEFT_FACING, -10.828, 14.020, 2.681, 44.10);
		perspective(STPresetCameraType.PERSPECTIVE_HEROIC_RIGHT_FACING, -10.802, -14.035, -2.684, 43.19);
		perspective(STPresetCameraType.PERSPECTIVE_HEROIC_EXTREME_LEFT_FACING, 8.082, 34.435, -2.927, 79.88);
		perspective(STPresetCameraType.PERSPECTIVE_HEROIC_EXTREME_RIGHT_FACING, 8.039, -34.427, 2.948, 79.62);
		perspective(STPresetCameraType.PERSPECTIVE_RELAXED, -50.312, 0.005, 0.000, 44.84);
		perspective(STPresetCameraType.PERSPECTIVE_RELAXED_MODERATELY, -35.042, 0.027, -0.010, 44.40);
	}

	/** The camera as Word places it: the preset's, its rot, fov and zoom over it */
	static final class Camera {
		final double lat, lon, rev, zoom, d;
		final boolean perspective;
		final double obliqueX, obliqueY, eyeX, eyeY;
		private final double cLon, sLon, cLat, sLat, cRev, sRev;

		Camera(CTCamera c) {
			Preset p = PRESETS.get(c == null || c.getPrst() == null ? STPresetCameraType.ORTHOGRAPHIC_FRONT : c.getPrst());
			if (p == null) p = PRESETS.get(STPresetCameraType.ORTHOGRAPHIC_FRONT);
			if (c != null && c.getRot() != null) {    // a rot overrides the preset's turn (fxc8-21, -22)
				lat = c.getRot().getLat() / 60000.0;
				lon = c.getRot().getLon() / 60000.0;
				rev = c.getRot().getRev() / 60000.0;
			} else {
				lat = p.lat;
				lon = p.lon;
				rev = p.rev;
			}
			double fov = c != null && c.getFov() != null ? c.getFov() / 60000.0 : p.fov;
			perspective = p.perspective && (fov > 0 || p.distance > 0);
			d = p.distance > 0 ? p.distance : perspective ? C / Math.tan(Math.toRadians(fov) / 2) : 0;
			eyeX = p.eyeX;
			eyeY = p.eyeY;
			zoom = c != null && c.getZoom() > 0 ? c.getZoom() / 100000.0 : 1;
			obliqueX = p.obliqueX;
			obliqueY = p.obliqueY;
			cLon = Math.cos(Math.toRadians(lon));
			sLon = Math.sin(Math.toRadians(lon));
			cLat = Math.cos(Math.toRadians(lat));
			sLat = Math.sin(Math.toRadians(lat));
			cRev = Math.cos(Math.toRadians(rev));
			sRev = Math.sin(Math.toRadians(rev));
		}

		/** A point turned into the camera's space: {x, y, z}, z towards the viewer */
		double[] turn(double x, double y, double z) {
			double x1 = x * cLon - z * sLon, z1 = x * sLon + z * cLon;
			double y2 = y * cLat + z1 * sLat, z2 = -y * sLat + z1 * cLat;
			return new double[] { x1 * cRev + y2 * sRev, -x1 * sRev + y2 * cRev, z2 };
		}

		/** A direction turned (a normal) */
		double[] turnVector(double x, double y, double z) {
			return turn(x, y, z);
		}

		/** A point projected: {X, Y, depth}, points from the frame's centre, depth larger nearer */
		double[] project(double x, double y, double z) {
			double[] t = turn(x, y, z);
			double X = t[0], Y = t[1];
			if (perspective) {
				double s = d / (d - t[2]);
				X = K * X * s + eyeX * (1 - s);     // a legacy perspective's depth goes towards its eye (0 for the others)
				Y = K * Y * s + eyeY * (1 - s);
			}
			X -= z * obliqueX;          // z is behind the face, negative: the back face moves towards the named corner
			Y -= z * obliqueY;
			return new double[] { X * zoom, Y * zoom, t[2] };
		}
	}

	// ----------------------------------------------------------------------------------------------------- the profiles

	/**
	 * The bevels' profiles: the height, as a fraction of the bevel's, at t = 0, 1/32 ... 1 of its width in from the
	 * outline, read from Word's pictures of each bevel at 24 by 24 pt seen edge on (phase 5a findings section 2,
	 * phase5a-bevels.tsv, written by bevel_profiles.py, these lines generated from it). They scale with the bevel's width
	 * and height (the circle at 12 by 24 and 24 by 12 pt reads within 0.02). What a silhouette shows is the profile's
	 * envelope: a groove inside it (divot, riblet) doesn't show, and is drawn as its envelope (phase 5b).
	 */
	private static final Map<STBevelPresetType, double[]> PROFILES = new EnumMap<STBevelPresetType, double[]>(STBevelPresetType.class);

	static {
		PROFILES.put(STBevelPresetType.RELAXED_INSET, new double[] { 0.000, 0.314, 0.433, 0.522, 0.597, 0.656, 0.715, 0.745, 0.790, 0.834, 0.864, 0.879, 0.909, 0.938, 0.953, 0.968, 0.983, 0.998, 0.998, 0.998, 0.998, 0.998, 0.998, 0.998, 0.998, 0.998, 0.998, 0.998, 0.998, 0.998, 0.998, 0.998, 1.000 });
		PROFILES.put(STBevelPresetType.CIRCLE, new double[] { 0.000, 0.240, 0.344, 0.418, 0.463, 0.522, 0.582, 0.611, 0.656, 0.686, 0.715, 0.745, 0.775, 0.790, 0.819, 0.834, 0.849, 0.879, 0.894, 0.909, 0.923, 0.923, 0.938, 0.953, 0.953, 0.968, 0.968, 0.983, 0.983, 0.983, 0.983, 0.983, 1.000 });
		PROFILES.put(STBevelPresetType.SLOPE, new double[] { 0.000, 0.285, 0.433, 0.597, 0.745, 0.849, 0.923, 0.953, 0.983, 0.983, 0.983, 0.998, 0.998, 0.998, 0.998, 0.998, 0.998, 0.998, 0.998, 0.998, 0.998, 0.998, 0.998, 0.998, 0.998, 0.998, 0.998, 0.998, 0.998, 0.998, 0.998, 0.998, 1.000 });
		PROFILES.put(STBevelPresetType.CROSS, new double[] { 0.000, 0.062, 0.091, 0.091, 0.091, 0.091, 0.091, 0.091, 0.091, 0.091, 0.091, 0.091, 0.091, 0.091, 0.106, 0.121, 0.181, 0.968, 0.983, 0.998, 0.998, 0.998, 0.998, 0.998, 0.998, 0.998, 0.998, 0.998, 0.998, 0.998, 0.998, 0.998, 1.000 });
		PROFILES.put(STBevelPresetType.ANGLE, new double[] { 0.000, 0.032, 0.062, 0.091, 0.121, 0.151, 0.181, 0.210, 0.240, 0.270, 0.314, 0.344, 0.374, 0.403, 0.433, 0.463, 0.493, 0.522, 0.552, 0.597, 0.626, 0.656, 0.686, 0.715, 0.745, 0.775, 0.805, 0.834, 0.864, 0.894, 0.938, 0.968, 1.000 });
		PROFILES.put(STBevelPresetType.SOFT_ROUND, new double[] { 0.000, 0.522, 0.715, 0.849, 0.953, 0.998, 0.998, 0.998, 0.998, 0.998, 0.998, 0.998, 0.998, 0.998, 0.998, 0.998, 0.998, 0.998, 0.998, 0.998, 0.998, 0.998, 0.998, 0.998, 0.998, 0.998, 0.998, 0.998, 0.998, 0.998, 0.998, 0.998, 1.000 });
		PROFILES.put(STBevelPresetType.CONVEX, new double[] { 0.000, 0.077, 0.091, 0.091, 0.091, 0.091, 0.106, 0.106, 0.121, 0.136, 0.151, 0.166, 0.181, 0.195, 0.225, 0.240, 0.270, 0.299, 0.329, 0.359, 0.389, 0.433, 0.463, 0.507, 0.552, 0.597, 0.641, 0.701, 0.790, 0.938, 0.983, 0.983, 1.000 });
		PROFILES.put(STBevelPresetType.COOL_SLANT, new double[] { 0.000, 0.225, 0.299, 0.374, 0.448, 0.507, 0.597, 0.656, 0.730, 0.790, 0.849, 0.879, 0.923, 0.938, 0.953, 0.968, 0.983, 0.983, 0.998, 0.998, 0.998, 0.998, 0.998, 0.998, 0.998, 0.998, 0.998, 0.998, 0.998, 0.998, 0.998, 0.998, 1.000 });
		PROFILES.put(STBevelPresetType.DIVOT, new double[] { 0.000, 0.240, 0.359, 0.448, 0.522, 0.582, 0.656, 0.701, 0.730, 0.775, 0.805, 0.819, 0.849, 0.864, 0.879, 0.894, 0.909, 0.909, 0.923, 0.923, 0.923, 0.923, 0.923, 0.923, 0.923, 0.923, 0.923, 0.923, 0.968, 0.983, 0.983, 0.983, 1.000 });
		PROFILES.put(STBevelPresetType.RIBLET, new double[] { 0.000, 0.210, 0.299, 0.359, 0.403, 0.463, 0.522, 0.567, 0.597, 0.641, 0.671, 0.715, 0.745, 0.775, 0.805, 0.834, 0.849, 0.864, 0.879, 0.894, 0.894, 0.909, 0.909, 0.909, 0.909, 0.909, 0.909, 0.909, 0.909, 0.923, 0.983, 0.983, 1.000 });
		PROFILES.put(STBevelPresetType.HARD_EDGE, new double[] { 0.000, 0.255, 0.403, 0.567, 0.715, 0.864, 0.968, 0.983, 0.998, 0.998, 0.998, 0.998, 0.998, 0.998, 0.998, 0.998, 0.998, 0.998, 0.998, 0.998, 0.998, 0.998, 0.998, 0.998, 0.998, 0.998, 0.998, 0.998, 0.998, 0.998, 0.998, 0.998, 1.000 });
		PROFILES.put(STBevelPresetType.ART_DECO, new double[] { 0.000, 0.136, 0.181, 0.225, 0.255, 0.270, 0.285, 0.299, 0.314, 0.314, 0.329, 0.433, 0.567, 0.626, 0.686, 0.730, 0.760, 0.805, 0.819, 0.849, 0.879, 0.894, 0.909, 0.923, 0.938, 0.953, 0.968, 0.968, 0.983, 0.983, 0.983, 0.983, 1.000 });
	}

	/** A bevel's profile at a fraction t of its width in from the outline (0 there, 1 at the face) */
	static double profile(STBevelPresetType p, double t) {
		double[] table = PROFILES.get(p == null ? STBevelPresetType.CIRCLE : p);
		if (table == null) table = PROFILES.get(STBevelPresetType.CIRCLE);
		t = Math.max(0, Math.min(1, t)) * (table.length - 1);
		int i = Math.min(table.length - 2, (int) Math.floor(t));
		double f = t - i;
		return table[i] * (1 - f) + table[i + 1] * f;
	}

	// ------------------------------------------------------------------------------------------------------- drawing

	/**
	 * The drawing in 3-D: its face (the drawing as painted, in its box (0, 0, w, h)) on its front, its outline extruded.
	 *
	 * @param face the drawing as painted
	 * @param outline what it paints, in its box: the outline extruded and bevelled
	 * @param w its box's width, points
	 * @param h its box's height, points
	 * @param sides the colour of its sides (the extrusion's), or null for the face's colour at the outline
	 * @param pixelsPerPoint the picture's resolution (Word's is 200 dpi)
	 * @return the picture, or null where nothing is painted
	 */
	public static Picture draw(Effects.Painter face, Shape outline, double w, double h, CTScene3D scene, CTShape3D sp, ThemeColors theme,
			double pixelsPerPoint, List<String> problems) {
		Camera cam = new Camera(scene == null ? null : scene.getCamera());
		double bevelW = 0, bevelH = 0, backW = 0, backH = 0;
		STBevelPresetType topProfile = null, backProfile = null;
		double depth = sp == null ? 0 : sp.getExtrusionH() / 12700.0, z0 = sp == null ? 0 : sp.getZ() / 12700.0;
		if (sp != null && sp.getBevelT() != null) {
			CTBevel b = sp.getBevelT();
			bevelW = b.getW() / 12700.0;
			bevelH = b.getH() / 12700.0;
			topProfile = b.getPrst();
		}
		if (sp != null && sp.getBevelB() != null) {
			CTBevel b = sp.getBevelB();
			backW = b.getW() / 12700.0;
			backH = b.getH() / 12700.0;
			backProfile = b.getPrst();
		}
		Color sideColour = sp != null && sp.getExtrusionClr() != null ? Fills.awt(DrawingMLColors.resolve(sp.getExtrusionClr(), theme)) : null;
		if (sp != null && sp.getContourW() > 0 && problems != null && !problems.contains(ShapeRenderer.FLAT_3D)) {
			problems.add(ShapeRenderer.FLAT_3D);   // the contour isn't drawn yet (phase 5a findings section 3)
		}
		double[] light = light(scene);

		// the face and the outline, sampled at the grid (one cell a pixel at the picture's resolution)
		double s = pixelsPerPoint;
		Rectangle2D ob = outline.getBounds2D();
		int gx0 = (int) Math.floor(ob.getMinX() * s) - 1, gy0 = (int) Math.floor(ob.getMinY() * s) - 1;
		int gw = (int) Math.ceil(ob.getMaxX() * s) + 2 - gx0, gh = (int) Math.ceil(ob.getMaxY() * s) + 2 - gy0;
		if (gw <= 2 || gh <= 2 || (long) gw * gh > 40_000_000L) return null;
		BufferedImage tex = new BufferedImage(gw, gh, BufferedImage.TYPE_INT_ARGB);
		Graphics2D g = tex.createGraphics();
		g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
		g.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BILINEAR);
		g.translate(-gx0, -gy0);
		g.scale(s, s);
		face.paint(g);
		g.dispose();
		BufferedImage maskImg = new BufferedImage(gw, gh, BufferedImage.TYPE_BYTE_GRAY);
		Graphics2D mg = maskImg.createGraphics();
		mg.translate(-gx0, -gy0);
		mg.scale(s, s);
		mg.setColor(Color.WHITE);
		mg.fill(outline);
		mg.dispose();
		boolean[] inside = new boolean[gw * gh];
		for (int y = 0; y < gh; y++) for (int x = 0; x < gw; x++) inside[y * gw + x] = (maskImg.getRaster().getSample(x, y, 0) & 0xff) >= 128;

		// the signed distance from the outline, points, positive inside (exact, Felzenszwalb and Huttenlocher's two passes):
		// the outline is half a cell beyond the last sample on its side, so each cell's distance to the nearest sample of
		// the other side, less half a cell
		double[] dist = distanceInside(inside, gw, gh);
		boolean[] outside = new boolean[inside.length];
		for (int i = 0; i < inside.length; i++) outside[i] = !inside[i];
		double[] away = distanceInside(outside, gw, gh);
		double[] sdf = new double[dist.length];
		for (int i = 0; i < dist.length; i++) {
			sdf[i] = inside[i] ? (dist[i] - 0.5) / s : -(away[i] - 0.5) / s;
			dist[i] = Math.max(0, sdf[i]);
		}

		// the frame's centre: the model's origin
		double cx = w / 2, cy = h / 2;
		Raster r = new Raster(cam, cx, cy, s);

		// the front: a height field over the grid
		double[] zf = new double[gw * gh];
		for (int i = 0; i < zf.length; i++) {
			if (!inside[i]) continue;
			zf[i] = bevelH > 0 && bevelW > 0 && dist[i] < bevelW ? -bevelH * (1 - profile(topProfile, dist[i] / bevelW)) : 0;
			zf[i] += z0;
		}
		for (int y = 0; y + 1 < gh; y++) {
			for (int x = 0; x + 1 < gw; x++) {
				int a = y * gw + x, b = a + 1, c = a + gw, dd = c + 1;
				if (!inside[a] && !inside[b] && !inside[c] && !inside[dd]) continue;
				double px = (x + gx0 + 0.5) / s - cx, py = (y + gy0 + 0.5) / s - cy, q = 1 / s;
				// a cell with a corner outside: that corner moved onto the outline, at the bevel's foot, so that the face
				// ends at the outline and the picture's supersampling alone antialiases it (an edge cell's partial alpha
				// had antialiased it twice: a two-pixel ramp, a corner rounder than Word's)
				double[] pa = onOutline(sdf, inside, gw, gh, x, y, px, py, s), pb = onOutline(sdf, inside, gw, gh, x + 1, y, px + q, py, s),
						pc = onOutline(sdf, inside, gw, gh, x, y + 1, px, py + q, s), pd = onOutline(sdf, inside, gw, gh, x + 1, y + 1, px + q, py + q, s);
				double foot = z0 - bevelH;
				double za = inside[a] ? zf[a] : foot, zb = inside[b] ? zf[b] : foot, zc = inside[c] ? zf[c] : foot, zd = inside[dd] ? zf[dd] : foot;
				// the normal from the height field's slope, where the cell is inside
				double zas = zAt(zf, inside, a, b, c, dd), zbs = zAt(zf, inside, b, a, dd, c), zcs = zAt(zf, inside, c, dd, a, b),
						zds = zAt(zf, inside, dd, c, b, a);
				double gxs = ((zbs - zas) + (zds - zcs)) / (2 * q), gys = ((zcs - zas) + (zds - zbs)) / (2 * q);
				double[] n = cam.turnVector(-gxs, -gys, 1);
				int colour = texel(tex, x, y, true);
				int shaded = shade(colour, n, light);
				r.quad(pa[0], pa[1], za, pb[0], pb[1], zb, pd[0], pd[1], zd, pc[0], pc[1], zc, shaded);
			}
		}

		// the sides: the outline extruded from the bevel's foot to the back's. Where a bevel's profile is steep at the
		// outline (softRound, slope), the grid's first sample, half a cell in, is far above its foot: the side starts
		// at that sample's height, so that the bevel reaches its foot at the outline, as Word's does (fxd4)
		double zTop = z0 - bevelH, zBack = z0 - bevelH - depth;
		double zEdge = bevelH > 0 && bevelW > 0 ? z0 - bevelH * (1 - profile(topProfile, 0.5 / s / bevelW)) : zTop;
		if (depth > 0 || backH > 0 || zEdge > zTop) {
			List<double[]> ring = rings(outline, 0.25);
			Color fallback = sideColour;
			for (double[] seg : ring) {
				double x1 = seg[0] - cx, y1 = seg[1] - cy, x2 = seg[2] - cx, y2 = seg[3] - cy;
				double ex = x2 - x1, ey = y2 - y1, len = Math.hypot(ex, ey);
				if (len == 0) continue;
				// the outward normal (the ring runs with the outline's own winding; flip where it points in)
				double nx = ey / len, ny = -ex / len;
				if (seg[4] < 0) {
					nx = -nx;
					ny = -ny;
				}
				int base = fallback != null ? fallback.getRGB() : texelNear(tex, (seg[0] + seg[2]) / 2 * s - gx0, (seg[1] + seg[3]) / 2 * s - gy0);
				int shaded = shade(base, cam.turnVector(nx, ny, 0), light);
				if (zEdge > zTop) {     // the bevel's foot: the face's colour, the bevel's slope there
					int foot = texelNear(tex, (seg[0] + seg[2]) / 2 * s - gx0, (seg[1] + seg[3]) / 2 * s - gy0);
					r.quad(x1, y1, zEdge, x2, y2, zEdge, x2, y2, zTop, x1, y1, zTop, shade(foot, cam.turnVector(nx, ny, 0), light));
				}
				if (depth > 0) r.quad(x1, y1, zTop, x2, y2, zTop, x2, y2, zBack, x1, y1, zBack, shaded);
			}
			// the back: its bevel, then its face, the sides' colour
			int back = fallback != null ? fallback.getRGB() : 0xff808080;
			for (int y = 0; y + 1 < gh && (depth > 0 || backH > 0); y++) {
				for (int x = 0; x + 1 < gw; x++) {
					int a = y * gw + x, b = a + 1, c = a + gw, dd = c + 1;
					if (!inside[a] || !inside[b] || !inside[c] || !inside[dd]) continue;
					double px = (x + gx0 + 0.5) / s - cx, py = (y + gy0 + 0.5) / s - cy, q = 1 / s;
					double za = zBack - backZ(dist[a], backW, backH, backProfile), zb = zBack - backZ(dist[b], backW, backH, backProfile),
							zc = zBack - backZ(dist[c], backW, backH, backProfile), zd = zBack - backZ(dist[dd], backW, backH, backProfile);
					double gxs = ((zb - za) + (zd - zc)) / (2 * q), gys = ((zc - za) + (zd - zb)) / (2 * q);
					r.quad(px, py, za, px + q, py, zb, px + q, py + q, zd, px, py + q, zc, shade(back, cam.turnVector(gxs, gys, -1), light));
				}
			}
		}
		if (problems != null && !problems.contains(SHADING)) problems.add(SHADING);
		return r.picture();
	}

	/**
	 * A grid sample's place, points from the frame's centre: where it is outside the outline, moved onto it along the
	 * signed distance's gradient
	 */
	private static double[] onOutline(double[] sdf, boolean[] inside, int w, int h, int x, int y, double px, double py, double s) {
		int i = y * w + x;
		if (inside[i]) return new double[] { px, py };
		double gx = sdf[y * w + Math.min(w - 1, x + 1)] - sdf[y * w + Math.max(0, x - 1)];
		double gy = sdf[Math.min(h - 1, y + 1) * w + x] - sdf[Math.max(0, y - 1) * w + x];
		double len = Math.hypot(gx, gy);
		if (len == 0) return new double[] { px, py };
		double step = -sdf[i];             // the distance to the outline, points, inwards along the gradient
		return new double[] { px + gx / len * step, py + gy / len * step };
	}

	/** The back bevel's depth behind the back face at a distance in from the outline */
	private static double backZ(double d, double w, double h, STBevelPresetType p) {
		return h > 0 && w > 0 ? (d < w ? h * profile(p, d / w) : h) : 0;
	}

	/** A corner's height: its own where it is inside, else the nearest inside corner's of the cell */
	private static double zAt(double[] z, boolean[] in, int self, int n1, int n2, int n3) {
		if (in[self]) return z[self];
		if (in[n1]) return z[n1];
		if (in[n2]) return z[n2];
		if (in[n3]) return z[n3];
		return 0;
	}

	private static int texel(BufferedImage tex, int x, int y, boolean any) {
		int best = 0, alpha = -1;
		for (int dy = 0; dy < 2; dy++) {
			for (int dx = 0; dx < 2; dx++) {
				int xx = Math.min(tex.getWidth() - 1, x + dx), yy = Math.min(tex.getHeight() - 1, y + dy);
				int c = tex.getRGB(xx, yy);
				if ((c >>> 24) > alpha) {
					alpha = c >>> 24;
					best = c;
				}
			}
		}
		// a face is opaque where the outline is: the face's colour over the outline's ground (alpha only at its edge)
		return any ? (best | 0xff000000) : best;
	}

	private static int texelNear(BufferedImage tex, double x, double y) {
		int bx = (int) Math.round(x), by = (int) Math.round(y);
		for (int rr = 0; rr < 4; rr++) {
			for (int dy = -rr; dy <= rr; dy++) {
				for (int dx = -rr; dx <= rr; dx++) {
					int xx = bx + dx, yy = by + dy;
					if (xx < 0 || yy < 0 || xx >= tex.getWidth() || yy >= tex.getHeight()) continue;
					int c = tex.getRGB(xx, yy);
					if ((c >>> 24) > 128) return c | 0xff000000;
				}
			}
		}
		return 0xff808080;
	}

	/** The light's direction in the camera's space, towards the light: the rig's direction, from in front */
	static double[] light(CTScene3D scene) {
		STLightRigDirection dir = scene != null && scene.getLightRig() != null && scene.getLightRig().getDir() != null
				? scene.getLightRig().getDir() : STLightRigDirection.T;
		double x = 0, y = 0;
		switch (dir) {
		case TL: x = -1; y = -1; break;
		case T: y = -1; break;
		case TR: x = 1; y = -1; break;
		case L: x = -1; break;
		case R: x = 1; break;
		case BL: x = -1; y = 1; break;
		case B: y = 1; break;
		case BR: x = 1; y = 1; break;
		default: break;
		}
		double z = 1.2, n = Math.sqrt(x * x + y * y + z * z);
		return new double[] { x / n, y / n, z / n };
	}

	/** The simple shading: diffuse, normalised so that a face turned to the viewer keeps its colour */
	static int shade(int argb, double[] n, double[] light) {
		double len = Math.sqrt(n[0] * n[0] + n[1] * n[1] + n[2] * n[2]);
		if (len == 0) return argb;
		double dot = (n[0] * light[0] + n[1] * light[1] + n[2] * light[2]) / len;
		double front = light[2];
		double f = 1 + 0.6 * (Math.max(0, dot) - front);
		f = Math.max(0.25, Math.min(1.35, f));
		int a = argb >>> 24, rr = (argb >> 16) & 0xff, gg = (argb >> 8) & 0xff, bb = argb & 0xff;
		return (a << 24) | (clamp(rr * f) << 16) | (clamp(gg * f) << 8) | clamp(bb * f);
	}

	private static int clamp(double v) {
		return (int) Math.max(0, Math.min(255, Math.round(v)));
	}

	/** The outline as segments {x1, y1, x2, y2, winding sign}, flattened to that many points */
	static List<double[]> rings(Shape outline, double flatness) {
		List<double[]> out = new ArrayList<double[]>();
		PathIterator it = outline.getPathIterator(null, flatness);
		double[] c = new double[6];
		double sx = 0, sy = 0, lx = 0, ly = 0;
		List<double[]> sub = new ArrayList<double[]>();
		while (!it.isDone()) {
			int t = it.currentSegment(c);
			if (t == PathIterator.SEG_MOVETO) {
				close(out, sub);
				sx = lx = c[0];
				sy = ly = c[1];
			} else if (t == PathIterator.SEG_LINETO) {
				sub.add(new double[] { lx, ly, c[0], c[1] });
				lx = c[0];
				ly = c[1];
			} else if (t == PathIterator.SEG_CLOSE) {
				if (lx != sx || ly != sy) sub.add(new double[] { lx, ly, sx, sy });
				lx = sx;
				ly = sy;
			}
			it.next();
		}
		close(out, sub);
		return out;
	}

	/** A subpath's segments, with the sign that turns their left-hand normal outward (by its signed area) */
	private static void close(List<double[]> out, List<double[]> sub) {
		if (sub.isEmpty()) return;
		double area = 0;
		for (double[] s : sub) area += s[0] * s[3] - s[2] * s[1];
		double sign = area >= 0 ? -1 : 1;    // y down: a positive signed area runs clockwise on the page
		for (double[] s : sub) out.add(new double[] { s[0], s[1], s[2], s[3], sign });
		sub.clear();
	}

	/** The Euclidean distance of each inside cell to the nearest outside one, in cells */
	static double[] distanceInside(boolean[] inside, int w, int h) {
		double inf = 1e20;
		double[] f = new double[w * h];
		for (int i = 0; i < f.length; i++) f[i] = inside[i] ? inf : 0;
		double[] col = new double[Math.max(w, h)], d = new double[Math.max(w, h)];
		for (int x = 0; x < w; x++) {
			for (int y = 0; y < h; y++) col[y] = f[y * w + x];
			edt1(col, h, d);
			for (int y = 0; y < h; y++) f[y * w + x] = d[y];
		}
		for (int y = 0; y < h; y++) {
			for (int x = 0; x < w; x++) col[x] = f[y * w + x];
			edt1(col, w, d);
			for (int x = 0; x < w; x++) f[y * w + x] = Math.sqrt(d[x]);
		}
		return f;
	}

	/** One dimension of the squared distance transform (Felzenszwalb and Huttenlocher) */
	private static void edt1(double[] f, int n, double[] d) {
		int[] v = new int[n];
		double[] z = new double[n + 1];
		int k = 0;
		v[0] = 0;
		z[0] = -1e30;
		z[1] = 1e30;
		for (int q = 1; q < n; q++) {
			double s = ((f[q] + q * q) - (f[v[k]] + v[k] * v[k])) / (2.0 * q - 2.0 * v[k]);
			while (s <= z[k]) {
				k--;
				s = ((f[q] + q * q) - (f[v[k]] + v[k] * v[k])) / (2.0 * q - 2.0 * v[k]);
			}
			k++;
			v[k] = q;
			z[k] = s;
			z[k + 1] = 1e30;
		}
		k = 0;
		for (int q = 0; q < n; q++) {
			while (z[k + 1] < q) k++;
			d[q] = (q - v[k]) * (q - v[k]) + f[v[k]];
		}
	}

	// ------------------------------------------------------------------------------------------------------ the raster

	/** Triangles z-buffered into a picture at twice its resolution, then averaged down */
	static final class Raster {
		private static final int SS = 2;
		private final Camera cam;
		private final double cx, cy, s;
		private final List<double[]> tris = new ArrayList<double[]>();
		private final List<Integer> colours = new ArrayList<Integer>();
		private double minX = Double.MAX_VALUE, minY = Double.MAX_VALUE, maxX = -Double.MAX_VALUE, maxY = -Double.MAX_VALUE;

		Raster(Camera cam, double cx, double cy, double s) {
			this.cam = cam;
			this.cx = cx;
			this.cy = cy;
			this.s = s;
		}

		void quad(double x1, double y1, double z1, double x2, double y2, double z2, double x3, double y3, double z3, double x4, double y4,
				double z4, int argb) {
			double[] a = cam.project(x1, y1, z1), b = cam.project(x2, y2, z2), c = cam.project(x3, y3, z3), d = cam.project(x4, y4, z4);
			add(a, b, c, argb);
			add(a, c, d, argb);
		}

		private void add(double[] a, double[] b, double[] c, int argb) {
			tris.add(new double[] { a[0], a[1], a[2], b[0], b[1], b[2], c[0], c[1], c[2] });
			colours.add(argb);
			for (double[] p : new double[][] { a, b, c }) {
				minX = Math.min(minX, p[0]);
				maxX = Math.max(maxX, p[0]);
				minY = Math.min(minY, p[1]);
				maxY = Math.max(maxY, p[1]);
			}
		}

		Picture picture() {
			if (tris.isEmpty()) return null;
			double ss = s * SS;
			int ox = (int) Math.floor(minX * s) - 1, oy = (int) Math.floor(minY * s) - 1;
			int w = (int) Math.ceil(maxX * s) + 1 - ox, h = (int) Math.ceil(maxY * s) + 1 - oy;
			int W = w * SS, H = h * SS;
			if ((long) W * H > 80_000_000L) return null;
			int[] px = new int[W * H];
			double[] zb = new double[W * H];
			java.util.Arrays.fill(zb, -Double.MAX_VALUE);
			for (int t = 0; t < tris.size(); t++) {
				double[] v = tris.get(t);
				int colour = colours.get(t);
				double ax = (v[0] * s - ox) * SS, ay = (v[1] * s - oy) * SS, bx = (v[3] * s - ox) * SS, by = (v[4] * s - oy) * SS,
						cx2 = (v[6] * s - ox) * SS, cy2 = (v[7] * s - oy) * SS;
				double area = (bx - ax) * (cy2 - ay) - (by - ay) * (cx2 - ax);
				if (Math.abs(area) < 1e-12) continue;
				int x0 = Math.max(0, (int) Math.floor(Math.min(ax, Math.min(bx, cx2)))), x1 = Math.min(W - 1, (int) Math.ceil(Math.max(ax, Math.max(bx, cx2))));
				int y0 = Math.max(0, (int) Math.floor(Math.min(ay, Math.min(by, cy2)))), y1 = Math.min(H - 1, (int) Math.ceil(Math.max(ay, Math.max(by, cy2))));
				for (int y = y0; y <= y1; y++) {
					double sy = y + 0.5;
					for (int x = x0; x <= x1; x++) {
						double sx = x + 0.5;
						double w0 = ((bx - sx) * (cy2 - sy) - (by - sy) * (cx2 - sx)) / area;
						double w1 = ((cx2 - sx) * (ay - sy) - (cy2 - sy) * (ax - sx)) / area;
						double w2 = 1 - w0 - w1;
						if (w0 < -1e-9 || w1 < -1e-9 || w2 < -1e-9) continue;
						double z = w0 * v[2] + w1 * v[5] + w2 * v[8];
						int i = y * W + x;
						// an edge cell's partial alpha is composited, not replaced: over what is behind it, under what is
						// in front (else the face's antialiased edge leaves a seam over the sides it stands in front of)
						if (z >= zb[i]) {
							px[i] = (colour >>> 24) == 255 || px[i] == 0 ? colour : over(colour, px[i]);
							zb[i] = z;
						} else if ((px[i] >>> 24) < 255) {
							px[i] = over(px[i], colour);
						}
					}
				}
			}
			// averaged down, alpha-weighted
			BufferedImage img = new BufferedImage(w, h, BufferedImage.TYPE_INT_ARGB);
			for (int y = 0; y < h; y++) {
				for (int x = 0; x < w; x++) {
					double a = 0, r = 0, g = 0, b = 0;
					for (int dy = 0; dy < SS; dy++) {
						for (int dx = 0; dx < SS; dx++) {
							int c = px[(y * SS + dy) * W + x * SS + dx];
							double al = (c >>> 24) / 255.0;
							a += al;
							r += al * ((c >> 16) & 0xff);
							g += al * ((c >> 8) & 0xff);
							b += al * (c & 0xff);
						}
					}
					if (a <= 0) continue;
					int A = clamp(a / (SS * SS) * 255);
					img.setRGB(x, y, (A << 24) | (clamp(r / a) << 16) | (clamp(g / a) << 8) | clamp(b / a));
				}
			}
			return new Picture(img, new Rectangle2D.Double(ox / s + cx, oy / s + cy, w / s, h / s));
		}
	}

	/** a over b, non-premultiplied ARGB */
	static int over(int a, int b) {
		double aa = (a >>> 24) / 255.0, ba = (b >>> 24) / 255.0;
		double oa = aa + ba * (1 - aa);
		if (oa <= 0) return 0;
		double r = (((a >> 16) & 0xff) * aa + ((b >> 16) & 0xff) * ba * (1 - aa)) / oa;
		double g = (((a >> 8) & 0xff) * aa + ((b >> 8) & 0xff) * ba * (1 - aa)) / oa;
		double bl = ((a & 0xff) * aa + (b & 0xff) * ba * (1 - aa)) / oa;
		return (clamp(oa * 255) << 24) | (clamp(r) << 16) | (clamp(g) << 8) | clamp(bl);
	}

	/** The cameras with a preset, for tests */
	static List<STPresetCameraType> presets() {
		return Collections.unmodifiableList(new ArrayList<STPresetCameraType>(PRESETS.keySet()));
	}
}
