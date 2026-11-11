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
import org.docx4j.dml.CTLightRig;
import org.docx4j.dml.CTSphereCoords;
import org.docx4j.dml.STBevelPresetType;
import org.docx4j.dml.STLightRigDirection;
import org.docx4j.dml.STLightRigType;
import org.docx4j.dml.STPresetCameraType;
import org.docx4j.dml.STPresetMaterialType;
import org.docx4j.model.color.DrawingMLColors;
import org.docx4j.model.color.ThemeColors;

/**
 * A 3-D drawing as Word draws it (CR-081 phase 5a): one picture with alpha, in place of the drawing, its geometry
 * Word's, and its shading Word's for the light rigs and materials of Word's picture styles (phase 5b-1).
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
 * <p>The shading, as Word's pictures read (CR-081-notes/phase5b-light.tsv, phase5b2-findings.md): each light rig four
 * (freezing and brightRoom five) directional lights that turn with the drawing, each with a diffuse and a highlight's
 * colour, and an ambient; each material its terms as {@link Material}'s. All 27 rigs and the materials but
 * legacyWireframe are fitted; that one, and none, are drawn as warmMatte, and {@link #SHADING} is reported.</p>
 *
 * @since 17.3.7 (CR-081 phase 5a)
 */
public final class Scene3D {

	/** A 3-D drawing whose material isn't Word's (legacyWireframe, none): drawn with Word's geometry and warmMatte, not
	 *  its own */
	public static final String SHADING = "render.effect.3d_shading";

	/**
	 * A 3-D shape's text drawn flat: under a camera other than orthographicFront, without a:flatTx, Word turns the
	 * text into its 3-D picture with the face (phase 5a findings section 5); docx4j's text is the output's
	 */
	public static final String TEXT_3D = "render.effect.3d_text";

	/** Whether Word turns a 3-D shape's text with it under this scene: any camera but orthographicFront unturned */
	public static boolean turnsText(CTScene3D scene) {
		CTCamera c = scene == null ? null : scene.getCamera();
		if (c == null) return false;
		if (c.getPrst() != null && c.getPrst() != STPresetCameraType.ORTHOGRAPHIC_FRONT) return true;
		return c.getRot() != null && (c.getRot().getLat() != 0 || c.getRot().getLon() != 0 || c.getRot().getRev() != 0);
	}

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

		/** A picture placed in the drawing's frame: an effect picture to be drawn in the drawing's plane */
		public static Picture of(BufferedImage image, Rectangle2D place) {
			return new Picture(image, new Rectangle2D.Double(place.getX(), place.getY(), place.getWidth(), place.getHeight()));
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
	 * envelope: a groove inside it doesn't show. hardEdge's and relaxedInset's are their true profiles, 65 heights,
	 * grooves and all, read from Word's shading (phase 5b findings section 6, invert_profiles.py): their faces sit below
	 * their ridges. The others' grooves (divot, riblet) are drawn as their envelopes (phase 5b-2).
	 */
	private static final Map<STBevelPresetType, double[]> PROFILES = new EnumMap<STBevelPresetType, double[]>(STBevelPresetType.class);

	static {
		PROFILES.put(STBevelPresetType.RELAXED_INSET, new double[] { 0.000, 0.133, 0.237, 0.315, 0.378, 0.432, 0.479, 0.520, 0.558, 0.593, 0.626, 0.655, 0.683, 0.709, 0.735, 0.758, 0.780, 0.800, 0.819, 0.838, 0.855, 0.871, 0.886, 0.900, 0.913, 0.926, 0.938, 0.950, 0.961, 0.971, 0.981, 0.990, 0.998, 0.993, 0.972, 0.951, 0.930, 0.909, 0.888, 0.867, 0.846, 0.826, 0.806, 0.787, 0.769, 0.752, 0.735, 0.719, 0.704, 0.689, 0.676, 0.664, 0.653, 0.644, 0.636, 0.629, 0.623, 0.618, 0.614, 0.610, 0.608, 0.606, 0.604, 0.603, 0.603 });
		PROFILES.put(STBevelPresetType.CIRCLE, new double[] { 0.000, 0.240, 0.344, 0.418, 0.463, 0.522, 0.582, 0.611, 0.656, 0.686, 0.715, 0.745, 0.775, 0.790, 0.819, 0.834, 0.849, 0.879, 0.894, 0.909, 0.923, 0.923, 0.938, 0.953, 0.953, 0.968, 0.968, 0.983, 0.983, 0.983, 0.983, 0.983, 1.000 });
		PROFILES.put(STBevelPresetType.SLOPE, new double[] { 0.000, 0.285, 0.433, 0.597, 0.745, 0.849, 0.923, 0.953, 0.983, 0.983, 0.983, 0.998, 0.998, 0.998, 0.998, 0.998, 0.998, 0.998, 0.998, 0.998, 0.998, 0.998, 0.998, 0.998, 0.998, 0.998, 0.998, 0.998, 0.998, 0.998, 0.998, 0.998, 1.000 });
		PROFILES.put(STBevelPresetType.CROSS, new double[] { 0.000, 0.062, 0.091, 0.091, 0.091, 0.091, 0.091, 0.091, 0.091, 0.091, 0.091, 0.091, 0.091, 0.091, 0.106, 0.121, 0.181, 0.968, 0.983, 0.998, 0.998, 0.998, 0.998, 0.998, 0.998, 0.998, 0.998, 0.998, 0.998, 0.998, 0.998, 0.998, 1.000 });
		PROFILES.put(STBevelPresetType.ANGLE, new double[] { 0.000, 0.032, 0.062, 0.091, 0.121, 0.151, 0.181, 0.210, 0.240, 0.270, 0.314, 0.344, 0.374, 0.403, 0.433, 0.463, 0.493, 0.522, 0.552, 0.597, 0.626, 0.656, 0.686, 0.715, 0.745, 0.775, 0.805, 0.834, 0.864, 0.894, 0.938, 0.968, 1.000 });
		PROFILES.put(STBevelPresetType.SOFT_ROUND, new double[] { 0.000, 0.522, 0.715, 0.849, 0.953, 0.998, 0.998, 0.998, 0.998, 0.998, 0.998, 0.998, 0.998, 0.998, 0.998, 0.998, 0.998, 0.998, 0.998, 0.998, 0.998, 0.998, 0.998, 0.998, 0.998, 0.998, 0.998, 0.998, 0.998, 0.998, 0.998, 0.998, 1.000 });
		PROFILES.put(STBevelPresetType.CONVEX, new double[] { 0.000, 0.077, 0.091, 0.091, 0.091, 0.091, 0.106, 0.106, 0.121, 0.136, 0.151, 0.166, 0.181, 0.195, 0.225, 0.240, 0.270, 0.299, 0.329, 0.359, 0.389, 0.433, 0.463, 0.507, 0.552, 0.597, 0.641, 0.701, 0.790, 0.938, 0.983, 0.983, 1.000 });
		PROFILES.put(STBevelPresetType.COOL_SLANT, new double[] { 0.000, 0.225, 0.299, 0.374, 0.448, 0.507, 0.597, 0.656, 0.730, 0.790, 0.849, 0.879, 0.923, 0.938, 0.953, 0.968, 0.983, 0.983, 0.998, 0.998, 0.998, 0.998, 0.998, 0.998, 0.998, 0.998, 0.998, 0.998, 0.998, 0.998, 0.998, 0.998, 1.000 });
		PROFILES.put(STBevelPresetType.DIVOT, new double[] { 0.000, 0.240, 0.359, 0.448, 0.522, 0.582, 0.656, 0.701, 0.730, 0.775, 0.805, 0.819, 0.849, 0.864, 0.879, 0.894, 0.909, 0.909, 0.923, 0.923, 0.923, 0.923, 0.923, 0.923, 0.923, 0.923, 0.923, 0.923, 0.968, 0.983, 0.983, 0.983, 1.000 });
		PROFILES.put(STBevelPresetType.RIBLET, new double[] { 0.000, 0.210, 0.299, 0.359, 0.403, 0.463, 0.522, 0.567, 0.597, 0.641, 0.671, 0.715, 0.745, 0.775, 0.805, 0.834, 0.849, 0.864, 0.879, 0.894, 0.894, 0.909, 0.909, 0.909, 0.909, 0.909, 0.909, 0.909, 0.909, 0.923, 0.983, 0.983, 1.000 });
		PROFILES.put(STBevelPresetType.HARD_EDGE, new double[] { 0.000, 0.111, 0.214, 0.301, 0.383, 0.465, 0.547, 0.629, 0.711, 0.793, 0.875, 0.949, 0.980, 0.990, 0.995, 0.998, 0.999, 1.000, 1.000, 1.000, 1.000, 1.000, 1.000, 1.000, 1.000, 1.000, 1.000, 1.000, 1.000, 1.000, 1.000, 1.000, 1.000, 1.000, 1.000, 1.000, 1.000, 1.000, 1.000, 1.000, 0.998, 0.993, 0.976, 0.947, 0.922, 0.905, 0.890, 0.876, 0.865, 0.856, 0.848, 0.840, 0.834, 0.828, 0.823, 0.818, 0.815, 0.811, 0.808, 0.806, 0.803, 0.802, 0.801, 0.800, 0.800 });
		PROFILES.put(STBevelPresetType.ART_DECO, new double[] { 0.000, 0.136, 0.181, 0.225, 0.255, 0.270, 0.285, 0.299, 0.314, 0.314, 0.329, 0.433, 0.567, 0.626, 0.686, 0.730, 0.760, 0.805, 0.819, 0.849, 0.879, 0.894, 0.909, 0.923, 0.938, 0.953, 0.968, 0.968, 0.983, 0.983, 0.983, 0.983, 1.000 });
	}

	/**
	 * What Word's shading shows of the other presets' profiles, read from it as hardEdge's and relaxedInset's were (phase
	 * 5b-2, round cr081-phase5b2-r1's fxh5 at 24 by 12 pt; invert_profiles.py b2): 65 heights, scaled to their highest.
	 * Word lights a preset at its mesh's vertices and blends between, so its shading shows normals blended across the
	 * facets, a longer, gentler rise than the silhouette's (slope: the silhouette is at its height a third of the way in;
	 * the shading rises over most of the width). The silhouette keeps {@link #PROFILES}; the shading takes its normal
	 * from these. Against Word's at 24 by 12 and 6 by 3 pt they are 1.1 to 5.0 / 255 where PROFILES were 2.3 to 16.6;
	 * cross, whose cliff its shading can't show, keeps PROFILES.
	 */
	private static final Map<STBevelPresetType, double[]> SHADES = new EnumMap<STBevelPresetType, double[]>(STBevelPresetType.class);

	static {
		SHADES.put(STBevelPresetType.ANGLE, new double[] { 0.000, 0.016, 0.031, 0.047, 0.063, 0.079, 0.094, 0.110, 0.126, 0.141, 0.157, 0.173, 0.189, 0.204, 0.220, 0.236, 0.252, 0.267, 0.283, 0.299, 0.314, 0.330, 0.346, 0.362, 0.377, 0.393, 0.409, 0.424, 0.440, 0.456, 0.472, 0.487, 0.503, 0.519, 0.534, 0.550, 0.566, 0.582, 0.597, 0.613, 0.629, 0.645, 0.660, 0.676, 0.692, 0.707, 0.723, 0.739, 0.755, 0.770, 0.786, 0.802, 0.817, 0.833, 0.849, 0.865, 0.880, 0.896, 0.912, 0.927, 0.943, 0.959, 0.975, 0.990, 1.000 });
		SHADES.put(STBevelPresetType.ART_DECO, new double[] { 0.000, 0.056, 0.103, 0.136, 0.163, 0.185, 0.203, 0.220, 0.237, 0.252, 0.263, 0.271, 0.279, 0.286, 0.293, 0.299, 0.304, 0.308, 0.312, 0.314, 0.316, 0.326, 0.381, 0.461, 0.517, 0.562, 0.599, 0.632, 0.661, 0.686, 0.710, 0.732, 0.752, 0.771, 0.788, 0.804, 0.820, 0.835, 0.849, 0.862, 0.874, 0.885, 0.895, 0.905, 0.914, 0.923, 0.931, 0.939, 0.946, 0.953, 0.959, 0.965, 0.970, 0.975, 0.979, 0.983, 0.987, 0.990, 0.993, 0.995, 0.997, 0.998, 0.999, 1.000, 1.000 });
		SHADES.put(STBevelPresetType.CONVEX, new double[] { 0.000, 0.020, 0.037, 0.047, 0.051, 0.054, 0.056, 0.056, 0.057, 0.058, 0.060, 0.062, 0.065, 0.069, 0.073, 0.077, 0.082, 0.087, 0.093, 0.099, 0.106, 0.113, 0.121, 0.129, 0.138, 0.147, 0.157, 0.168, 0.179, 0.190, 0.202, 0.215, 0.227, 0.240, 0.254, 0.268, 0.282, 0.297, 0.312, 0.328, 0.344, 0.361, 0.378, 0.396, 0.415, 0.434, 0.454, 0.475, 0.497, 0.520, 0.543, 0.568, 0.595, 0.623, 0.653, 0.687, 0.727, 0.789, 0.930, 0.972, 0.986, 0.992, 0.996, 0.999, 1.000 });
		SHADES.put(STBevelPresetType.COOL_SLANT, new double[] { 0.000, 0.071, 0.134, 0.184, 0.231, 0.274, 0.316, 0.356, 0.396, 0.435, 0.474, 0.512, 0.550, 0.587, 0.625, 0.662, 0.699, 0.736, 0.771, 0.802, 0.829, 0.853, 0.874, 0.893, 0.911, 0.927, 0.943, 0.955, 0.964, 0.972, 0.980, 0.985, 0.990, 0.994, 0.996, 0.998, 0.999, 1.000, 1.000, 1.000, 1.000, 1.000, 1.000, 1.000, 1.000, 1.000, 1.000, 1.000, 1.000, 1.000, 1.000, 1.000, 1.000, 1.000, 1.000, 1.000, 1.000, 1.000, 1.000, 1.000, 1.000, 1.000, 1.000, 1.000, 1.000 });
		SHADES.put(STBevelPresetType.DIVOT, new double[] { 0.000, 0.102, 0.193, 0.267, 0.331, 0.385, 0.434, 0.479, 0.520, 0.556, 0.591, 0.623, 0.651, 0.678, 0.702, 0.723, 0.744, 0.762, 0.780, 0.797, 0.814, 0.830, 0.846, 0.861, 0.873, 0.883, 0.893, 0.903, 0.912, 0.919, 0.926, 0.932, 0.938, 0.943, 0.946, 0.944, 0.923, 0.920, 0.920, 0.920, 0.920, 0.920, 0.920, 0.920, 0.920, 0.920, 0.920, 0.920, 0.920, 0.920, 0.920, 0.920, 0.920, 0.920, 0.922, 0.943, 0.965, 0.976, 0.984, 0.989, 0.993, 0.996, 0.998, 1.000, 1.000 });
		SHADES.put(STBevelPresetType.RIBLET, new double[] { 0.000, 0.081, 0.151, 0.205, 0.251, 0.291, 0.327, 0.361, 0.393, 0.422, 0.450, 0.476, 0.502, 0.525, 0.548, 0.570, 0.592, 0.612, 0.632, 0.652, 0.671, 0.689, 0.707, 0.724, 0.741, 0.758, 0.774, 0.789, 0.804, 0.819, 0.833, 0.846, 0.857, 0.867, 0.876, 0.884, 0.891, 0.896, 0.901, 0.905, 0.908, 0.910, 0.912, 0.913, 0.914, 0.914, 0.915, 0.915, 0.915, 0.915, 0.915, 0.915, 0.915, 0.915, 0.915, 0.915, 0.915, 0.918, 0.927, 0.951, 0.980, 0.991, 0.996, 0.999, 1.000 });
		SHADES.put(STBevelPresetType.SLOPE, new double[] { 0.000, 0.113, 0.213, 0.299, 0.385, 0.470, 0.556, 0.642, 0.727, 0.800, 0.853, 0.892, 0.921, 0.943, 0.959, 0.972, 0.980, 0.987, 0.991, 0.994, 0.996, 0.998, 0.999, 1.000, 1.000, 1.000, 1.000, 1.000, 1.000, 1.000, 1.000, 1.000, 1.000, 1.000, 0.999, 0.998, 0.996, 0.994, 0.991, 0.986, 0.980, 0.971, 0.957, 0.940, 0.917, 0.886, 0.845, 0.789, 0.715, 0.630, 0.544, 0.459, 0.373, 0.287, 0.201, 0.116, 0.034, -0.016, -0.044, -0.064, -0.079, -0.088, -0.093, -0.096, -0.097 });
		SHADES.put(STBevelPresetType.SOFT_ROUND, new double[] { 0.000, 0.223, 0.415, 0.557, 0.675, 0.772, 0.850, 0.915, 0.961, 0.993, 1.000, 0.998, 0.991, 0.970, 0.944, 0.914, 0.882, 0.849, 0.816, 0.783, 0.750, 0.719, 0.689, 0.660, 0.633, 0.607, 0.582, 0.558, 0.535, 0.514, 0.493, 0.474, 0.455, 0.437, 0.419, 0.402, 0.386, 0.371, 0.358, 0.345, 0.333, 0.322, 0.311, 0.301, 0.292, 0.283, 0.275, 0.267, 0.260, 0.254, 0.248, 0.242, 0.237, 0.232, 0.228, 0.224, 0.221, 0.218, 0.216, 0.214, 0.212, 0.211, 0.210, 0.209, 0.209 });
	}

	/** The slope, height over width, of a preset's shading profile at t, or NaN where it has none */
	static double shadeSlope(STBevelPresetType p, double t) {
		double[] table = p == null ? null : SHADES.get(p);
		if (table == null) return Double.NaN;
		int n = table.length - 1;
		double x = Math.max(0, Math.min(1, t)) * n;
		int i = Math.min(n - 1, (int) Math.floor(x));
		return (table[i + 1] - table[i]) * n;
	}

	/**
	 * A bevel's profile at a fraction t of its width in from the outline (0 there, 1 at the face). The circle is the
	 * quarter circle (its table reads as one within its pixels), whose slope its shading needs: the table's steps would
	 * band it (phase 5b-1)
	 */
	static double profile(STBevelPresetType p, double t) {
		if (p == null || p == STBevelPresetType.CIRCLE) {
			double u = 1 - Math.max(0, Math.min(1, t));
			return Math.sqrt(1 - u * u);
		}
		double[] table = PROFILES.get(p);
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
		return draw(face, outline, w, h, scene, sp, theme, pixelsPerPoint, problems, Collections.<Picture>emptyList());
	}

	/**
	 * The drawing in 3-D, with its effects under it in its plane: Word casts a 3-D drawing's shadow, glow and reflection
	 * as the flat drawing's, in the drawing's plane, and projects them with it (phase 5a findings section 5). Each is
	 * drawn as a picture on that plane behind the drawing (behind its back, where it has depth), z-buffered, so that the
	 * drawing hides what is behind it; all in the one picture.
	 *
	 * @param under the flat drawing's effect pictures, in Word's order (the reflection, the preset shadow, the outer
	 *        shadow, the glow), each placed in the drawing's frame
	 */
	public static Picture draw(Effects.Painter face, Shape outline, double w, double h, CTScene3D scene, CTShape3D sp, ThemeColors theme,
			double pixelsPerPoint, List<String> problems, List<Picture> under) {
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
		// the contour (phase 5a findings section 7): a rim of half its width round the sides, and the face's outer
		// half-width in its colour where there is no top bevel
		double rim = sp == null ? 0 : sp.getContourW() / 12700.0 / 2;
		Color contourColour = rim > 0 && sp.getContourClr() != null ? Fills.awt(DrawingMLColors.resolve(sp.getContourClr(), theme)) : null;
		Lighting light = lighting(scene, sp, cam);
		int cuts = bevelH > 0 && bevelW > 0 && (topProfile == null || topProfile == STBevelPresetType.CIRCLE) ? cuts(bevelW, bevelH) : 0;

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
		boolean layered = light.material.translucent();
		Raster r = new Raster(cam, cx, cy, s, layered);

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
				if (layered && n[2] < 0) continue;     // a translucent drawing's cap facing away isn't drawn
				int colour = texel(tex, x, y, true);
				if (rim > 0 && bevelH == 0 && contourColour != null && minDist(dist, inside, a, b, c, dd) < rim) colour = contourColour.getRGB();
				// a circle top bevel is lit as Word lights it: at its profile's cuts, the colour blended between them
				// (phase 5b findings section 5)
				double gl = Math.hypot(gxs, gys), dCell = meanInside(dist, inside, a, b, c, dd);
				int shaded;
				double shadeSlope = bevelH > 0 && dCell < bevelW ? shadeSlope(topProfile, dCell / bevelW) : Double.NaN;
				if (cuts > 0 && dCell < bevelW && gl > 1e-9) {
					shaded = blended(colour, gxs / gl, gys / gl, dCell / bevelW, cuts, bevelW, bevelH, cam, light);
				} else if (!Double.isNaN(shadeSlope)) {
					// the preset's shading profile, along the distance's gradient (its direction in)
					double ddx = ((dist[b] - dist[a]) + (dist[dd] - dist[c])) / (2 * q), ddy = ((dist[c] - dist[a]) + (dist[dd] - dist[b])) / (2 * q);
					double dl = Math.hypot(ddx, ddy), sl = shadeSlope * bevelH / bevelW;
					shaded = dl > 1e-9 ? shade(colour, cam.turnVector(-sl * ddx / dl, -sl * ddy / dl, 1), light) : shade(colour, n, light);
				} else {
					shaded = shade(colour, n, light);
				}
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
				// the outward normal (y down: the ring's left-hand normal, turned by its winding), as the contour's rim's
				double nx = -ey / len, ny = ex / len;
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
					double[] nb = cam.turnVector(gxs, gys, -1);
					if (layered && nb[2] < 0) continue;
					r.quad(px, py, za, px + q, py, zb, px + q, py + q, zd, px, py + q, zc, shade(back, nb, light));
				}
			}
		}
		// the contour's rim: the sides from the top bevel's foot to the back, widened by the rim with rounded ends, its
		// front no nearer than the face (fxf2: 6 pt edge on spans depth 0 to 6, its bulge half the width at its middle)
		if (rim > 0) {
			double zA = Math.min(z0 - bevelH, z0 - rim), zB = zA - depth;
			int colour = contourColour != null ? contourColour.getRGB() : sideColour != null ? sideColour.getRGB() : 0xff808080;
			contourRim(r, cam, light, rings(outline, 0.25), cx, cy, zA, zB, rim, colour);
		}
		// the effects, in the drawing's plane, behind its back
		double zUnder = z0 - bevelH - depth - backH - Math.max(0, 2 * rim - bevelH) - 0.02;
		for (Picture e : under) {
			BufferedImage im = e.getImage();
			Rectangle2D.Double pl = e.getPlace();
			int iw = im.getWidth(), ih = im.getHeight();
			double ex = pl.width / iw, ey = pl.height / ih;
			for (int y = 0; y < ih; y++) {
				for (int x = 0; x < iw; x++) {
					int c = im.getRGB(x, y);
					if ((c >>> 24) == 0) continue;
					double px = pl.x + x * ex - cx, py = pl.y + y * ey - cy;
					r.quad(px, py, zUnder, px + ex, py, zUnder, px + ex, py + ey, zUnder, px, py + ey, zUnder, c);
				}
			}
		}
		if (light.approximate && problems != null && !problems.contains(SHADING)) problems.add(SHADING);
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

	/** The least distance in from the outline at a cell's corners that are inside */
	private static double minDist(double[] dist, boolean[] inside, int... cells) {
		double m = Double.MAX_VALUE;
		for (int c : cells) if (inside[c]) m = Math.min(m, dist[c]);
		return m == Double.MAX_VALUE ? 0 : m;
	}

	/**
	 * The contour's rim: the outline swept, offset outwards along its normal by a profile from its front (on the outline,
	 * at zA + rim) round a quarter circle to rim out at zA, straight to zB, and round a quarter circle back to the outline
	 * at zB - rim; its corners rounded (the normal turned between a vertex's two segments).
	 */
	private static void contourRim(Raster r, Camera cam, Lighting light, List<double[]> segments, double cx, double cy, double zA,
			double zB, double rim, int colour) {
		// the profile: (out, z, normal out, normal z), each end the measured shape: Word's 6 and 12 pt contours seen edge
		// on (fxf2-01, -02), their reach out by their depth from the rim's middle, both in half-widths, agree: 1 at 0, 0.88
		// at 0.25, 0.77 at 0.45, 0.66 at 0.6, 0.55 at 0.75, 0.44 at 0.87, and a blunt end at 0.9
		double[] t = { 0, 0.25, 0.45, 0.6, 0.75, 0.87, 0.9, 0.9 }, o = { 1, 0.88, 0.77, 0.66, 0.55, 0.44, 0.44, 0 };
		List<double[]> prof = new ArrayList<double[]>();
		for (int k = t.length - 1; k >= 0; k--) prof.add(end(t, o, k, rim, zA, 1));          // the front end, out to its middle
		for (int k = 0; k < t.length; k++) prof.add(end(t, o, k, rim, zB, -1));              // the back, from its middle
		// the sweep's stations: (x, y, normal x, normal y), each segment's ends, and at each vertex the normal turned from the
		// one segment's to the next's
		List<double[]> stations = new ArrayList<double[]>();
		double[] prevN = null, first = null;
		for (int i = 0; i < segments.size(); i++) {
			double[] seg = segments.get(i);
			double ex = seg[2] - seg[0], ey = seg[3] - seg[1], len = Math.hypot(ex, ey);
			if (len == 0) continue;
			// the outward normal (y down: the ring's left-hand normal, turned by its winding)
			double nx = -ey / len, ny = ex / len;
			if (seg[4] < 0) {
				nx = -nx;
				ny = -ny;
			}
			if (prevN != null) turn(stations, seg[0], seg[1], prevN, new double[] { nx, ny });
			stations.add(new double[] { seg[0], seg[1], nx, ny });
			stations.add(new double[] { seg[2], seg[3], nx, ny });
			prevN = new double[] { nx, ny };
			if (first == null) first = new double[] { seg[0], seg[1], nx, ny };
		}
		if (first != null && prevN != null) turn(stations, first[0], first[1], prevN, new double[] { first[2], first[3] });
		for (int i = 0; i + 1 < stations.size(); i++) {
			double[] a = stations.get(i), b = stations.get(i + 1);
			if (Math.hypot(a[0] - b[0], a[1] - b[1]) > 2 && (a[2] != b[2] || a[3] != b[3])) continue;    // across subpaths
			for (int k = 0; k + 1 < prof.size(); k++) {
				double[] p0 = prof.get(k), p1 = prof.get(k + 1);
				double nzx = (a[2] + b[2]) / 2 * (p0[2] + p1[2]) / 2, nzy = (a[3] + b[3]) / 2 * (p0[2] + p1[2]) / 2, nzz = (p0[3] + p1[3]) / 2;
				int shaded = shade(colour, cam.turnVector(nzx, nzy, nzz), light);
				r.quad(a[0] + a[2] * p0[0] - cx, a[1] + a[3] * p0[0] - cy, p0[1], b[0] + b[2] * p0[0] - cx, b[1] + b[3] * p0[0] - cy, p0[1],
						b[0] + b[2] * p1[0] - cx, b[1] + b[3] * p1[0] - cy, p1[1], a[0] + a[2] * p1[0] - cx, a[1] + a[3] * p1[0] - cy, p1[1], shaded);
			}
		}
	}

	/** A point of a rim's end: its reach out, its depth (from z, towards the face for side 1, the back for -1), its normal */
	private static double[] end(double[] t, double[] o, int k, double rim, double z, int side) {
		double dt = k + 1 < t.length ? t[k + 1] - t[k] : t[k] - t[k - 1], d0 = k + 1 < t.length ? o[k + 1] - o[k] : o[k] - o[k - 1];
		double nz = -d0, nout = dt == 0 ? 0 : dt;
		double len = Math.hypot(nz, nout);
		if (len == 0 || dt == 0) {
			nz = 1;
			nout = 0;
			len = 1;
		}
		return new double[] { rim * o[k], z + side * rim * t[k], nout / len, side * Math.abs(nz) / len };
	}

	/** Stations at a vertex, the normal turned from one segment's to the next's, a step a fifteen degrees */
	private static void turn(List<double[]> stations, double x, double y, double[] from, double[] to) {
		double a0 = Math.atan2(from[1], from[0]), a1 = Math.atan2(to[1], to[0]);
		double d = a1 - a0;
		while (d > Math.PI) d -= 2 * Math.PI;
		while (d < -Math.PI) d += 2 * Math.PI;
		int steps = (int) Math.ceil(Math.abs(d) / Math.toRadians(15));
		for (int k = 1; k < steps; k++) {
			double a = a0 + d * k / steps;
			stations.add(new double[] { x, y, Math.cos(a), Math.sin(a) });
		}
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

	/**
	 * Word's light rigs, fitted to its pictures (CR-081 phase 5b-1: phase5b-findings.md, phase5b-light.tsv, by
	 * fit_light_model.py from cr081-phase5b-r1 to -r3, the four rigs under the three materials on a deep circle bevel,
	 * front on, turned and from behind; the mean error against Word's 1.64 / 255): the ambient, then each light's
	 * {x, y, z, diffuse, highlight}, in the drawing's own space (x right, y down, z out of its face; towards the light)
	 * for dir t and no rot. The lights turn with the drawing: the camera turns round a lit drawing. The other 23 (phase
	 * 5b-2: phase5b2-findings.md, by fit_rigs_b2.py from cr081-phase5b2-r1's fxh1 to fxh3, each rig under warmMatte and
	 * matte from five views) are coloured: the ambient {r, g, b}, then each light's {x, y, z, diffuse r, g, b, highlight
	 * r, g, b}; four lights, five for freezing and brightRoom, whose fits a fifth improves (worst 6.22 to 3.75 / 255, and
	 * mean 3.66 to 2.50).
	 */
	private static final Map<STLightRigType, double[][]> RIGS = new EnumMap<STLightRigType, double[][]>(STLightRigType.class);

	/** Word's materials */
	private static final Map<STPresetMaterialType, Material> MATERIALS = new EnumMap<STPresetMaterialType, Material>(STPresetMaterialType.class);

	/**
	 * A material (phase 5b findings, and phase 5b-2's: phase5b2-findings.md, by fit_materials_b2.py and
	 * fit_translucent_b2.py from cr081-phase5b2-r1's fxh4, the grey ellipse under threePt from four views):
	 * <pre>
	 * colour = min(255, (c (A + own + diffuse sum D_i max(0, n.L_i)) + 255 wash) max(0, n.v)^edge
	 *                   + 255 highlight sum S_i max(0, R_i.v)^exponent + 255 rim (1 - max(0, n.v))^rimExponent)
	 * </pre>
	 * R_i Phong's reflection, or Blinn's half vector for the legacy materials (legacyPlastic 2.78 / 255 against Phong's
	 * 6.35); a translucent one's alpha alpha0 + alpha1 |n.v| a layer, clamped. A material's own light was read under threePt
	 * only: whether it is its own or scales the rig's ambient isn't measured.
	 */
	static final class Material {
		final double own, diffuse, wash, edge, highlight, exponent, rim, rimExponent, alpha0, alpha1;
		final boolean blinn;

		Material(double own, double diffuse, double wash, double edge, double highlight, double exponent, double rim, double rimExponent,
				boolean blinn) {
			this(own, diffuse, wash, edge, highlight, exponent, rim, rimExponent, blinn, 1, 0);
		}

		Material(double own, double diffuse, double wash, double edge, double highlight, double exponent, double rim, double rimExponent,
				boolean blinn, double alpha0, double alpha1) {
			this.own = own;
			this.diffuse = diffuse;
			this.wash = wash;
			this.edge = edge;
			this.highlight = highlight;
			this.exponent = exponent;
			this.rim = rim;
			this.rimExponent = rimExponent;
			this.blinn = blinn;
			this.alpha0 = alpha0;
			this.alpha1 = alpha1;
		}

		/**
		 * Word draws a translucent drawing's caps (its face and back, with their bevels) only where they face the viewer, and
		 * its sides from both sides, a side seen from inside lit as facing the viewer (phase 5b-2's translucency: the rules
		 * against Word's alpha, 1.46 and 1.59 / 255 where the nearest surface alone is 14.69 and 19.66)
		 */
		boolean translucent() {
			return alpha0 != 1 || alpha1 != 0;
		}

		/** 5b-1's: the rig's light, and a Phong highlight */
		static Material phong(double highlight, double exponent) {
			return new Material(0, 1, 0, 0, highlight, exponent, 0, 1, false);
		}
	}

	static {
		RIGS.put(STLightRigType.TWO_PT, new double[][] { { 0.2485 }, { -0.4122, 0.5038, 0.7591, 0.6103, 0.1507 },
				{ 0.2360, -0.8998, 0.3669, 0.2894, 0.0877 }, { -0.4026, 0.5680, 0.7178, 0.2236, 0.0926 } });
		RIGS.put(STLightRigType.THREE_PT, new double[][] { { 0.0290 }, { 0.2569, 0.8469, 0.4656, 0.4061, 0.1436 },
				{ -0.2586, -0.6973, 0.6685, 0.2922, 0.2037 }, { -0.2710, -0.5821, 0.7666, 0.8084, 0.1326 },
				{ -0.2706, 0.6229, -0.7340, 1.0032, 0.2085 } });
		RIGS.put(STLightRigType.CONTRASTING, new double[][] { { 1.0002 }, { 0.9841, 0.0482, -0.1709, 0.0015, 0.0323 },
				{ 0.9992, -0.0117, 0.0393, 0.0007, 0.2553 }, { -1.0000, 0.0036, 0.0053, 0.0027, 0.2725 },
				{ -0.9060, 0.0471, 0.4207, 0.0017, 0.0151 } });
		RIGS.put(STLightRigType.SOFT, new double[][] { { 0.4287 }, { 0.0347, -0.0918, 0.9952, 0.3839, 0.0075 },
				{ -0.3997, -0.9150, -0.0555, 0.3321, 0.0084 }, { 0.2447, -0.6879, 0.6833, 0.1711, 0.2199 },
				{ 0.9771, -0.2100, 0.0340, 0.2694, 0 } });
		// legacyFlat1: mean 1.27, worst 1.66 / 255
		RIGS.put(STLightRigType.LEGACY_FLAT_1, new double[][] { { 0.2995, 0.2995, 0.2995 },
				{ -0.5130, 0.7787, 0.3612, 0.3075, 0.3075, 0.3075, 0.0014, 0.0014, 0.0014 },
				{ 0.9377, 0.0206, 0.3468, 0.3252, 0.3252, 0.3252, 0.0000, 0.0000, 0.0000 },
				{ -0.0058, 0.0162, 0.9999, 0.8317, 0.8317, 0.8317, 0.1692, 0.1692, 0.1692 },
				{ -0.4547, -0.8258, 0.3335, 0.3242, 0.3242, 0.3242, 0.0101, 0.0101, 0.0101 } });
		// legacyFlat2: mean 1.06, worst 1.67 / 255
		RIGS.put(STLightRigType.LEGACY_FLAT_2, new double[][] { { 0.4188, 0.4188, 0.4188 },
				{ -0.6406, -0.7543, 0.1438, 0.4944, 0.4944, 0.4944, 0.1669, 0.1669, 0.1669 },
				{ 0.8865, -0.0761, -0.4564, 0.2435, 0.2435, 0.2435, 0.0000, 0.0000, 0.0000 },
				{ 0.2849, -0.6587, 0.6964, 0.2418, 0.2418, 0.2418, 0.0000, 0.0000, 0.0000 },
				{ 0.7114, 0.6296, 0.3123, 0.2498, 0.2498, 0.2498, 0.0012, 0.0012, 0.0012 } });
		// legacyFlat3: mean 0.77, worst 1.13 / 255
		RIGS.put(STLightRigType.LEGACY_FLAT_3, new double[][] { { 0.4833, 0.4833, 0.4833 },
				{ -0.9733, 0.0070, 0.2293, 0.4005, 0.4005, 0.4005, 0.1614, 0.1614, 0.1614 },
				{ 0.9867, 0.0220, -0.1614, 0.2978, 0.2978, 0.2978, 0.0021, 0.0021, 0.0021 },
				{ 0.2802, 0.4772, 0.8329, 0.1793, 0.1793, 0.1793, 0.0000, 0.0000, 0.0000 },
				{ 0.3744, -0.8689, 0.3239, 0.1152, 0.1152, 0.1152, 0.0059, 0.0059, 0.0059 } });
		// legacyFlat4: mean 1.97, worst 2.89 / 255
		RIGS.put(STLightRigType.LEGACY_FLAT_4, new double[][] { { 0.5200, 0.5200, 0.5200 },
				{ 0.4940, 0.3199, 0.8085, 0.1210, 0.1210, 0.1210, 0.0000, 0.0000, 0.0000 },
				{ 0.9566, -0.2906, 0.0205, 0.2875, 0.2875, 0.2875, 0.0034, 0.0034, 0.0034 },
				{ -0.6945, 0.6956, 0.1835, 0.0002, 0.0002, 0.0002, 0.1435, 0.1435, 0.1435 },
				{ -0.4926, 0.8465, 0.2021, 0.4779, 0.4779, 0.4779, 0.0037, 0.0037, 0.0037 } });
		// legacyNormal1: mean 0.90, worst 1.21 / 255
		RIGS.put(STLightRigType.LEGACY_NORMAL_1, new double[][] { { 0.1422, 0.1422, 0.1422 },
				{ -0.6525, 0.6278, 0.4245, 0.2049, 0.2049, 0.2049, 0.0010, 0.0010, 0.0010 },
				{ 0.8925, 0.2302, 0.3878, 0.2037, 0.2037, 0.2037, 0.0000, 0.0000, 0.0000 },
				{ 0.0003, 0.0071, 1.0000, 0.7997, 0.7997, 0.7997, 0.1969, 0.1969, 0.1969 },
				{ -0.2392, -0.8954, 0.3756, 0.2105, 0.2105, 0.2105, 0.0078, 0.0078, 0.0078 } });
		// legacyNormal2: mean 0.89, worst 1.38 / 255
		RIGS.put(STLightRigType.LEGACY_NORMAL_2, new double[][] { { 0.2233, 0.2233, 0.2233 },
				{ -0.6747, -0.7257, 0.1351, 0.6082, 0.6082, 0.6082, 0.1938, 0.1938, 0.1938 },
				{ 0.8696, -0.0872, -0.4860, 0.1496, 0.1496, 0.1496, 0.0000, 0.0000, 0.0000 },
				{ 0.2696, -0.6525, 0.7082, 0.1600, 0.1600, 0.1600, 0.0000, 0.0001, 0.0000 },
				{ 0.7326, 0.6266, 0.2660, 0.1599, 0.1599, 0.1599, 0.0015, 0.0015, 0.0015 } });
		// legacyNormal3: mean 0.72, worst 1.12 / 255
		RIGS.put(STLightRigType.LEGACY_NORMAL_3, new double[][] { { 0.2713, 0.2713, 0.2713 },
				{ -0.9760, 0.0073, 0.2175, 0.5491, 0.5491, 0.5491, 0.1913, 0.1913, 0.1913 },
				{ 0.9894, 0.0296, -0.1423, 0.1793, 0.1793, 0.1793, 0.0004, 0.0004, 0.0004 },
				{ 0.3237, 0.4448, 0.8351, 0.1034, 0.1034, 0.1034, 0.0000, 0.0000, 0.0000 },
				{ 0.3939, -0.8647, 0.3117, 0.0669, 0.0669, 0.0669, 0.0030, 0.0030, 0.0030 } });
		// legacyNormal4: mean 1.03, worst 1.48 / 255
		RIGS.put(STLightRigType.LEGACY_NORMAL_4, new double[][] { { 0.2356, 0.2356, 0.2356 },
				{ 0.9089, -0.2529, -0.3316, 0.1813, 0.1813, 0.1813, 0.0000, 0.0000, 0.0000 },
				{ 0.4398, -0.4188, 0.7945, 0.1158, 0.1158, 0.1158, 0.0036, 0.0035, 0.0035 },
				{ -0.6611, 0.7423, 0.1090, 0.6095, 0.6095, 0.6095, 0.1981, 0.1981, 0.1981 },
				{ 0.4714, 0.7377, 0.4832, 0.1299, 0.1299, 0.1299, 0.0001, 0.0000, 0.0001 } });
		// legacyHarsh1: mean 0.69, worst 1.02 / 255
		RIGS.put(STLightRigType.LEGACY_HARSH_1, new double[][] { { 0.0450, 0.0450, 0.0450 },
				{ -0.1650, -0.4964, 0.8523, 0.0029, 0.0029, 0.0029, 0.0041, 0.0041, 0.0041 },
				{ 0.3196, -0.6023, 0.7316, 0.0000, 0.0000, 0.0000, 0.0034, 0.0034, 0.0034 },
				{ -0.0023, -0.0001, 1.0000, 1.0115, 1.0115, 1.0115, 0.2292, 0.2292, 0.2292 },
				{ -0.1251, 0.4137, 0.9018, 0.0005, 0.0005, 0.0005, 0.0028, 0.0028, 0.0028 } });
		// legacyHarsh2: mean 0.71, worst 1.12 / 255
		RIGS.put(STLightRigType.LEGACY_HARSH_2, new double[][] { { 0.0461, 0.0461, 0.0461 },
				{ -0.7199, -0.6646, 0.2000, 0.2525, 0.2525, 0.2525, 0.0618, 0.0618, 0.0618 },
				{ -0.6959, -0.7089, 0.1152, 0.5411, 0.5411, 0.5411, 0.1640, 0.1640, 0.1640 },
				{ 0.9778, 0.0252, 0.2081, 0.2211, 0.2211, 0.2211, 0.0001, 0.0001, 0.0001 },
				{ 0.3086, -0.9411, -0.1384, 0.0091, 0.0091, 0.0091, 0.0045, 0.0045, 0.0045 } });
		// legacyHarsh3: mean 0.82, worst 1.16 / 255
		RIGS.put(STLightRigType.LEGACY_HARSH_3, new double[][] { { 0.0487, 0.0487, 0.0487 },
				{ -0.9772, -0.0259, 0.2108, 0.3525, 0.3525, 0.3525, 0.0687, 0.0687, 0.0687 },
				{ -0.9631, 0.0123, 0.2689, 0.1349, 0.1349, 0.1349, 0.0723, 0.0723, 0.0723 },
				{ -0.9892, 0.0181, 0.1457, 0.3097, 0.3097, 0.3097, 0.0923, 0.0923, 0.0923 },
				{ 0.9775, 0.0029, 0.2109, 0.2218, 0.2218, 0.2218, 0.0000, 0.0003, 0.0001 } });
		// legacyHarsh4: mean 0.71, worst 1.08 / 255
		RIGS.put(STLightRigType.LEGACY_HARSH_4, new double[][] { { 0.0474, 0.0474, 0.0474 },
				{ -0.7636, 0.6137, 0.2007, 0.1179, 0.1179, 0.1179, 0.0000, 0.0000, 0.0000 },
				{ -0.6480, 0.7606, 0.0401, 0.1398, 0.1398, 0.1398, 0.0091, 0.0091, 0.0091 },
				{ -0.6998, 0.6990, 0.1473, 0.5436, 0.5436, 0.5436, 0.2141, 0.2141, 0.2141 },
				{ 0.9784, -0.0058, 0.2068, 0.2227, 0.2227, 0.2227, 0.0002, 0.0002, 0.0002 } });
		// balanced: mean 1.24, worst 1.87 / 255
		RIGS.put(STLightRigType.BALANCED, new double[][] { { 0.1323, 0.1323, 0.1323 },
				{ -0.4093, 0.5322, 0.7411, 1.0405, 1.0405, 1.0405, 0.3046, 0.3046, 0.3046 },
				{ 0.7547, 0.3425, -0.5596, 0.0029, 0.0029, 0.0029, 0.0372, 0.0372, 0.0372 },
				{ 0.7702, 0.0714, -0.6338, 0.4772, 0.4772, 0.4772, 0.1163, 0.1163, 0.1163 },
				{ 0.3453, -0.9373, 0.0470, 0.9914, 0.9914, 0.9914, 0.2825, 0.2825, 0.2825 } });
		// harsh: mean 1.26, worst 1.81 / 255
		RIGS.put(STLightRigType.HARSH, new double[][] { { 0.2659, 0.2659, 0.2659 },
				{ -0.7119, -0.6353, 0.2993, 0.5735, 0.5735, 0.5735, 0.1313, 0.1313, 0.1313 },
				{ -0.4183, 0.6639, 0.6199, 0.0439, 0.0439, 0.0439, 0.0061, 0.0061, 0.0061 },
				{ -0.7616, -0.5369, 0.3629, 0.3061, 0.3061, 0.3061, 0.1193, 0.1193, 0.1193 },
				{ -0.6846, 0.6638, 0.3010, 0.8488, 0.8488, 0.8488, 0.2449, 0.2449, 0.2449 } });
		// flood: mean 1.75, worst 2.47 / 255
		RIGS.put(STLightRigType.FLOOD, new double[][] { { 0.1257, 0.1257, 0.1257 },
				{ -0.9567, -0.1859, 0.2239, 0.2682, 0.2682, 0.2682, 0.1077, 0.1077, 0.1077 },
				{ 0.1249, -0.8991, 0.4196, 0.5557, 0.5557, 0.5557, 0.1519, 0.1519, 0.1519 },
				{ -0.7672, 0.5645, 0.3046, 1.0892, 1.0892, 1.0892, 0.3191, 0.3191, 0.3191 },
				{ -0.9611, -0.2438, 0.1297, 0.8097, 0.8097, 0.8097, 0.2116, 0.2116, 0.2116 } });
		// morning: mean 2.18, worst 4.31 / 255
		RIGS.put(STLightRigType.MORNING, new double[][] { { 0.0223, 0.0195, 0.0258 },
				{ -0.4497, -0.2611, 0.8542, 0.4326, 0.4304, 0.3568, 0.0000, 0.0000, 0.0001 },
				{ -0.6809, 0.6607, 0.3160, 1.5276, 1.4664, 1.3779, 0.1337, 0.1317, 0.1225 },
				{ 0.9824, -0.1697, -0.0783, 0.4280, 0.4074, 0.3785, 0.1645, 0.1570, 0.1307 },
				{ 0.9564, -0.1881, -0.2233, 0.4483, 0.4046, 0.3081, 0.1164, 0.1053, 0.0946 } });
		// sunrise: mean 1.64, worst 2.27 / 255
		RIGS.put(STLightRigType.SUNRISE, new double[][] { { 0.0120, 0.0113, 0.0019 },
				{ -0.6850, 0.6904, 0.2327, 0.9249, 0.8725, 0.7406, 0.1741, 0.1676, 0.1328 },
				{ 0.9711, -0.1780, -0.1587, 1.4873, 1.2931, 0.9784, 0.0007, 0.0009, 0.0024 },
				{ -0.6566, 0.5887, 0.4715, 0.5625, 0.4554, 0.4224, 0.0001, 0.0000, 0.0001 },
				{ -0.4588, -0.2845, 0.8418, 0.4105, 0.4206, 0.3474, 0.0004, 0.0006, 0.0005 } });
		// sunset: mean 1.64, worst 2.05 / 255
		RIGS.put(STLightRigType.SUNSET, new double[][] { { 0.0202, 0.0201, 0.0174 },
				{ -0.4208, 0.1547, 0.8939, 0.1443, 0.2598, 0.1938, 0.0000, 0.0001, 0.0003 },
				{ 0.8972, -0.4203, -0.1359, 0.7213, 0.6432, 0.3633, 0.0086, 0.0012, 0.0013 },
				{ -0.6922, 0.6369, 0.3393, 1.4678, 0.7604, 0.6529, 0.1355, 0.0374, 0.0306 },
				{ -0.0894, 0.1027, 0.9907, 0.2159, 0.1714, 0.1194, 0.0031, 0.0000, 0.0021 } });
		// chilly: mean 1.35, worst 1.94 / 255
		RIGS.put(STLightRigType.CHILLY, new double[][] { { 0.1061, 0.1140, 0.1106 },
				{ -0.7150, 0.6626, 0.2230, 0.4551, 0.4615, 0.4951, 0.0743, 0.0794, 0.0807 },
				{ 0.7984, -0.5792, -0.1648, 0.4069, 0.4318, 0.4690, 0.1175, 0.1271, 0.1403 },
				{ -0.6099, 0.6635, 0.4333, 0.8828, 0.8658, 0.9693, 0.0000, 0.0000, 0.0000 },
				{ -0.1408, -0.3585, 0.9228, 0.4414, 0.4331, 0.4381, 0.0021, 0.0015, 0.0009 } });
		// freezing: mean 2.10, worst 3.75 / 255
		RIGS.put(STLightRigType.FREEZING, new double[][] { { 0.0039, 0.0099, 0.0155 },
				{ -0.6805, 0.6656, 0.3064, 1.1447, 1.1621, 1.5129, 0.1116, 0.1211, 0.1399 },
				{ 0.9943, -0.1068, 0.0063, 0.1393, 0.1423, 0.1741, 0.1094, 0.1337, 0.1567 },
				{ 0.9756, -0.1193, -0.1844, 0.4104, 0.5183, 0.6135, 0.0527, 0.0585, 0.0669 },
				{ -0.4696, -0.2540, 0.8455, 0.3709, 0.4513, 0.4652, 0.0002, 0.0008, 0.0002 },
				{ 0.9466, -0.2619, -0.1881, 0.4142, 0.5170, 0.5563, 0.1471, 0.1874, 0.2105 } });
		// flat: mean 2.01, worst 6.71 / 255
		RIGS.put(STLightRigType.FLAT, new double[][] { { 0.9980, 0.9964, 0.9952 },
				{ 0.8214, 0.2268, -0.5233, 0.0000, 0.0099, 0.0008, 0.1438, 0.1912, 0.2383 },
				{ -0.7336, 0.6530, 0.1880, 0.0223, 0.0272, 0.0314, 1.0464, 1.0424, 1.0390 },
				{ -0.1614, -0.9526, 0.2577, 0.0128, 0.0175, 0.0231, 0.2184, 0.2155, 0.2125 },
				{ 0.8636, -0.0343, -0.5030, 0.0037, 0.0064, 0.0056, 0.5049, 0.6066, 0.6713 } });
		// glow: mean 1.50, worst 2.72 / 255
		RIGS.put(STLightRigType.GLOW, new double[][] { { 0.9949, 0.9949, 0.9949 },
				{ -0.9987, 0.0047, 0.0517, 0.0459, 0.0459, 0.0459, 0.2382, 0.2382, 0.2382 },
				{ -0.9584, -0.0147, -0.2851, 0.0171, 0.0171, 0.0171, 0.0378, 0.0378, 0.0378 },
				{ -1.0000, -0.0009, 0.0034, 0.9259, 0.9259, 0.9259, 0.0239, 0.0239, 0.0239 },
				{ 1.0000, 0.0020, 0.0027, 0.6963, 0.6963, 0.6963, 0.1967, 0.1967, 0.1967 } });
		// brightRoom: mean 2.50, worst 3.35 / 255
		RIGS.put(STLightRigType.BRIGHT_ROOM, new double[][] { { 1.0162, 1.0163, 1.0162 },
				{ -0.4810, 0.8421, -0.2441, 0.5692, 0.5691, 0.5678, 0.0000, 0.0004, 0.0018 },
				{ -0.1836, 0.8169, 0.5468, 0.0002, 0.0000, 0.0000, 0.2899, 0.2898, 0.2897 },
				{ 0.9914, -0.0162, -0.1296, 0.9795, 0.9794, 0.9790, 0.0000, 0.0002, 0.0009 },
				{ -0.9950, -0.0956, -0.0287, 1.0075, 1.0074, 1.0070, 0.0004, 0.0003, 0.0003 },
				{ -0.3151, -0.8445, -0.4330, 0.4254, 0.4250, 0.4256, 0.0000, 0.0001, 0.0004 } });
		MATERIALS.put(STPresetMaterialType.MATTE, Material.phong(0, 1));
		MATERIALS.put(STPresetMaterialType.WARM_MATTE, Material.phong(1, 8.02));
		MATERIALS.put(STPresetMaterialType.PLASTIC, Material.phong(1.9135, 11.47));
		// phase 5b-2, under threePt (mean / worst against Word's four views); own = (ka - 1) x threePt's ambient 0.0290
		MATERIALS.put(STPresetMaterialType.LEGACY_MATTE, new Material(0, 1.0079, -0.0013, 0.0055, 0, 1, 0, 1, false));                 // 1.35 / 1.98
		MATERIALS.put(STPresetMaterialType.LEGACY_PLASTIC, new Material(0.0242, 1.0312, -0.0201, 0.0055, 3.3159, 32.46, 0, 1, true));   // 2.78 / 3.52
		MATERIALS.put(STPresetMaterialType.LEGACY_METAL, new Material(-0.0127, 0.6917, -0.0072, 0.0232, 1.6212, 32.54, 0.0060, 0.5, true)); // 1.76 / 2.26
		MATERIALS.put(STPresetMaterialType.METAL, new Material(0.0142, 1.0176, -0.0029, 0.1965, 3.9463, 12.27, 0.7361, 2.9826, false)); // 4.09 / 4.56
		MATERIALS.put(STPresetMaterialType.POWDER, new Material(0.1238, 1.7703, -0.0735, 0.6495, 0.7699, 3.795, 0.8310, 1.5646, false)); // 3.09 / 4.93
		MATERIALS.put(STPresetMaterialType.DK_EDGE, new Material(0.0491, 1.0753, -0.0480, 0.5975, 2.9125, 31.47, 0.0504, 0.5, false)); // 3.77 / 4.51
		MATERIALS.put(STPresetMaterialType.SOFT_EDGE, new Material(0.0333, 1.0146, -0.0188, 0.0387, 2.8905, 31.59, 0.6520, 4.01, false)); // 3.09 / 3.28
		MATERIALS.put(STPresetMaterialType.FLAT, new Material(0.6362, 0.0161, 0.1531, 0.4982, 1.9940, 39.58, 0.1605, 0.5758, false));  // 2.45 / 3.16
		MATERIALS.put(STPresetMaterialType.SOFTMETAL, new Material(0.0119, 0.9796, 0.0055, 0, 2.5347, 7.909, 0, 1, false));             // 3.21 / 3.88
		// translucent: alpha a0 + a1 |n.v| a layer (mean / worst alpha, and over a white page)
		MATERIALS.put(STPresetMaterialType.CLEAR, new Material(-0.0290, 1.2107, -0.0873, 0.3984, 3.7928, 19.22, 0.1625, 0.5, false, 1, -0.9));       // 1.46 / 2.13; 4.25 / 4.99
		MATERIALS.put(STPresetMaterialType.TRANSLUCENT_POWDER, new Material(0.1463, 1.0417, -0.0175, 0, 1.3410, 3.021, 0.8107, 2.4769, false, -0.01, 0.71)); // 1.45 / 2.30; 5.00 / 7.82
	}

	/** A drawing's lights in the camera's space, and its material */
	static final class Lighting {
		/** the rig's ambient, {r, g, b} */
		final double[] ambient;
		/** {x, y, z, diffuse r, g, b, highlight r, g, b} */
		final double[][] lights;
		final Material material;
		/** The rig or the material isn't one fitted */
		final boolean approximate;

		Lighting(double[] ambient, double[][] lights, Material material, boolean approximate) {
			this.ambient = ambient;
			this.lights = lights;
			this.material = material;
			this.approximate = approximate;
		}
	}

	/**
	 * The drawing's lighting: its rig's lights turned about the drawing's z by its direction, from t (tl turns them 45
	 * degrees anticlockwise on the page, fxg1-15; the other directions by the same rule, not measured), then by its
	 * rotation's rev, clockwise (fxg1-13, -14: rev 120 degrees), after its rot's lat and lon; then with the drawing, into the camera's space
	 * (phase 5b findings section 2); its material's highlight
	 */
	static Lighting lighting(CTScene3D scene, CTShape3D sp, Camera cam) {
		CTLightRig rig = scene != null ? scene.getLightRig() : null;
		STLightRigType type = rig != null && rig.getRig() != null ? rig.getRig() : STLightRigType.THREE_PT;
		boolean approximate = !RIGS.containsKey(type);
		double[][] def = RIGS.containsKey(type) ? RIGS.get(type) : RIGS.get(STLightRigType.THREE_PT);
		STPresetMaterialType mt = sp != null ? sp.getPrstMaterial() : STPresetMaterialType.WARM_MATTE;
		Material mat = MATERIALS.get(mt);
		if (mat == null) {
			approximate = true;
			mat = MATERIALS.get(STPresetMaterialType.WARM_MATTE);
		}
		double turn = 0;
		STLightRigDirection dir = rig != null && rig.getDir() != null ? rig.getDir() : STLightRigDirection.T;
		switch (dir) {
		case TL: turn = -45; break;
		case L: turn = -90; break;
		case BL: turn = -135; break;
		case B: turn = 180; break;
		case BR: turn = 135; break;
		case R: turn = 90; break;
		case TR: turn = 45; break;
		default: break;
		}
		CTSphereCoords rot = rig != null ? rig.getRot() : null;
		double lat = 0, lon = 0;
		if (rot != null) {
			turn += rot.getRev() / 60000.0;
			lat = Math.toRadians(rot.getLat() / 60000.0);
			lon = Math.toRadians(rot.getLon() / 60000.0);
		}
		double c = Math.cos(Math.toRadians(turn)), sn = Math.sin(Math.toRadians(turn));
		double cl = Math.cos(lat), sl = Math.sin(lat), co = Math.cos(lon), so = Math.sin(lon);
		double[][] lights = new double[def.length - 1][];
		for (int i = 1; i < def.length; i++) {
			double[] d = def[i];
			// the rig's rot: lat turns it about x, then lon about y (phase 5b-2, fxh5-25 to -29: lat 30 and 330, lon 30
			// and 330, and both, within 2.2 / 255), then dir and rev about z
			double x0 = d[0], y0 = d[1] * cl - d[2] * sl, z0 = d[1] * sl + d[2] * cl;
			double x1 = x0 * co + z0 * so, z1 = -x0 * so + z0 * co;
			double[] t = cam.turnVector(x1 * c - y0 * sn, x1 * sn + y0 * c, z1);
			lights[i - 1] = d.length == 9 ? new double[] { t[0], t[1], t[2], d[3], d[4], d[5], d[6], d[7], d[8] }
					: new double[] { t[0], t[1], t[2], d[3], d[3], d[3], d[4], d[4], d[4] };
		}
		double[] ambient = def[0].length == 3 ? def[0] : new double[] { def[0][0], def[0][0], def[0][0] };
		return new Lighting(ambient, lights, mat, approximate);
	}

	/**
	 * How many pieces Word cuts a circle bevel's quarter circle into, of equal angle, to light it at the cuts (phase 5b
	 * findings section 5, round cr081-phase5b-r5): by its width, 2 up to 2 pt (1.8, 2), 4 from 3 to 7.5 pt, 8 from 8 to
	 * 24 pt, and more for 36 pt (16 taken). Where between 2 and 3, and between 24 and 36 pt, the count doubles isn't
	 * measured: 2.5 and 30 are taken.
	 */
	static int cuts(double w, double h) {
		return w < 2.5 ? 2 : w < 8 ? 4 : w < 30 ? 8 : 16;
	}

	/**
	 * A circle bevel's colour at a fraction t of its width in from the outline, its slope inwards (ux, uy): lit at
	 * the cuts either side, of the quarter circle's angle phi (t = 1 - cos phi), and blended by t between them, as
	 * Word's (Gouraud's) is
	 */
	static int blended(int argb, double ux, double uy, double t, int cuts, double w, double h, Camera cam, Lighting light) {
		double phi = Math.acos(Math.max(-1, Math.min(1, 1 - Math.max(0, Math.min(1, t))))), step = Math.PI / 2 / cuts;
		int k = Math.min(cuts - 1, (int) Math.floor(phi / step));
		double p0 = k * step, p1 = (k + 1) * step, t0 = 1 - Math.cos(p0), t1 = 1 - Math.cos(p1);
		double f = t1 > t0 ? (t - t0) / (t1 - t0) : 0;
		int c0 = shade(argb, cam.turnVector(-ux * h * Math.cos(p0), -uy * h * Math.cos(p0), w * Math.sin(p0)), light);
		int c1 = shade(argb, cam.turnVector(-ux * h * Math.cos(p1), -uy * h * Math.cos(p1), w * Math.sin(p1)), light);
		int out = clamp((c0 >>> 24) * (1 - f) + (c1 >>> 24) * f) << 24;     // a translucent material's alpha, as its colour
		for (int sh = 0; sh <= 16; sh += 8) {
			double v = ((c0 >> sh) & 0xff) * (1 - f) + ((c1 >> sh) & 0xff) * f;
			out |= clamp(v) << sh;
		}
		return out;
	}

	/** The mean distance in of a cell's corners that are inside */
	private static double meanInside(double[] dist, boolean[] inside, int a, int b, int c, int d) {
		double sum = 0;
		int n = 0;
		for (int i : new int[] { a, b, c, d }) {
			if (!inside[i]) continue;
			sum += dist[i];
			n++;
		}
		return n == 0 ? 0 : sum / n;
	}

	/**
	 * Word's shading: the colour times the ambient and each light's diffuse share, then each light's highlight added
	 * (Phong's: its reflection's with the view, (0, 0, 1); Blinn's half vector fits Word's turned views less well, 2.51 /
	 * 255 against 1.64, but the legacy materials' better), clamped; the material's terms as {@link Material}'s
	 */
	static int shade(int argb, double[] n, Lighting l) {
		double len = Math.sqrt(n[0] * n[0] + n[1] * n[1] + n[2] * n[2]);
		if (len == 0) return argb;
		double nx = n[0] / len, ny = n[1] / len, nz = n[2] / len;
		Material m = l.material;
		if (m.translucent() && nz < 0) {     // a side seen from inside: lit as facing the viewer
			nx = -nx;
			ny = -ny;
			nz = -nz;
		}
		double[] diffuse = { l.ambient[0] + m.own, l.ambient[1] + m.own, l.ambient[2] + m.own }, spec = new double[3];
		for (double[] li : l.lights) {
			double d = nx * li[0] + ny * li[1] + nz * li[2];
			if (d <= 0) continue;
			for (int k = 0; k < 3; k++) diffuse[k] += m.diffuse * li[3 + k] * d;
			if (m.highlight <= 0) continue;
			double rv;
			if (m.blinn) {
				double hx = li[0], hy = li[1], hz = li[2] + 1, hl = Math.sqrt(hx * hx + hy * hy + hz * hz);
				rv = hl > 0 ? (nx * hx + ny * hy + nz * hz) / hl : 0;
			} else {
				rv = 2 * d * nz - li[2];
			}
			if (rv <= 0) continue;
			double p = Math.pow(rv, m.exponent);
			for (int k = 0; k < 3; k++) spec[k] += li[6 + k] * p;
		}
		double facing = Math.max(0, nz), edge = m.edge > 0 ? Math.pow(facing, m.edge) : 1;
		double rim = m.rim > 0 ? 255 * m.rim * Math.pow(1 - facing, m.rimExponent) : 0;
		int a = argb >>> 24;
		if (m.alpha1 != 0 || m.alpha0 != 1) a = clamp(a * Math.max(0, Math.min(1, m.alpha0 + m.alpha1 * nz)));
		int out = a << 24;
		for (int k = 0, sh = 16; k < 3; k++, sh -= 8) {
			double c = (argb >> sh) & 0xff;
			out |= clamp((c * diffuse[k] + 255 * m.wash) * edge + 255 * m.highlight * spec[k] + rim) << sh;
		}
		return out;
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
		/** Translucent surfaces: one surface's triangles meeting at a sample replace, not composite, each other */
		private final boolean layered;

		Raster(Camera cam, double cx, double cy, double s) {
			this(cam, cx, cy, s, false);
		}

		Raster(Camera cam, double cx, double cy, double s, boolean layered) {
			this.cam = cam;
			this.cx = cx;
			this.cy = cy;
			this.s = s;
			this.layered = layered;
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
						if (layered && Math.abs(z - zb[i]) < 1e-6) {
							px[i] = colour;
						} else if (z >= zb[i]) {
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
