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

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import org.docx4j.XmlUtils;
import org.docx4j.jaxb.Context;
import org.docx4j.model.color.ResolvedColor;
import org.docx4j.openpackaging.packages.WordprocessingMLPackage;
import org.docx4j.vml.CTFill;
import org.docx4j.vml.CTShadow;
import org.docx4j.vml.CTShape;
import org.docx4j.vml.CTShapetype;
import org.docx4j.vml.CTStroke;
import org.docx4j.vml.CTTextPath;
import org.docx4j.vml.STFillType;
import org.docx4j.vml.STShadowType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * A VML WordArt ({@code v:shape} with a {@code v:textpath}, CR-083 phase 4, D6) read as the model DrawingML WordArt is
 * drawn from ({@link WordArtText}): a paragraph for each line of the text path's {@code string}, a run each in the
 * {@code style}'s font, its {@code v:fill} and {@code v:stroke} as a DrawingML fill and outline, its {@code v:shadow} as
 * an {@code a:effectLst} (D19). Its shape's geometry, which warps it, is {@link VmlGeometry}'s from
 * {@link WordArtText#getVml()}.
 * <p>
 * A gradient as Word draws it (cr083-phase4's wv45, phase 4 findings §10): {@code color2} at the focus, {@code color}
 * at the ends, so with no focus from {@code color2} to {@code color}; a negative focus the colours swapped, the focus
 * taken from the far end; a {@code colors} list in place of the two; blended by Word's sigma curve in gamma-2.2 light
 * unless the {@code method} is {@code none} or {@code linear}. A radial gradient is drawn as a circle where Word's is a
 * rectangle. A {@code v:shadow} with no {@code on} is off (Part 4's default is on; Office's legacy WordArt writes one on
 * styles with no shadow); an emboss a double shadow, light above and to the left; a picture fill drawn in its colour
 * and reported, a compound line drawn single and reported, a perspective shadow not drawn and reported;
 * {@code v-text-spacing} not applied; the default face Arial and size 36 pt.
 *
 * @since 17.3.7
 */
public final class VmlWordArt {

	private static final Logger log = LoggerFactory.getLogger(VmlWordArt.class);

	/** A VML WordArt's geometry that can't be evaluated or is past a limit: nothing drawn */
	public static final String BAD_GEOMETRY = "render.wordart.bad_geometry";

	/** A VML shadow not drawn as Word draws it: perspective */
	public static final String SHADOW = "render.wordart.shadow";

	private static final String A = "http://schemas.openxmlformats.org/drawingml/2006/main";

	private VmlWordArt() {}

	/** The shape as WordArt, or null where it isn't one ({@link VmlShapes.Resolved#isTextPath()}). */
	public static WordArtText of(VmlShapes.Resolved r, WordprocessingMLPackage pkg) {
		if (r == null || !r.isTextPath()) return null;
		List<String> problems = new ArrayList<String>();
		Map<String, String> style = style(r.child(CTTextPath.class, CTTextPath::getStyle));
		String face = face(style);
		double size = VmlShapes.length(style.get("font-size"), 36);
		if (!(size > 0)) size = 36;
		String weight = style.getOrDefault("font-weight", "normal").toLowerCase(Locale.ROOT);
		boolean bold = weight.startsWith("bold") || weight.matches("[6-9]00");
		String slant = style.getOrDefault("font-style", "normal").toLowerCase(Locale.ROOT);
		boolean italic = slant.startsWith("italic") || slant.startsWith("oblique");
		boolean kerned = truthy(style.get("v-text-kern"));

		String fillRgb = fillColour(r);
		Object fill = fill(r, fillRgb, problems);
		org.docx4j.dml.CTLineProperties outline = outline(r, fillRgb, problems);
		// a VML shadow is a copy of the glyphs Word draws under them (shadows), not a DrawingML effect picture
		shadow(r, fillRgb, problems);
		org.docx4j.dml.CTEffectList effects = null;
		boolean threeD = r.has(org.docx4j.vml.officedrawing.CTExtrusion.class)
				&& isOn(r.child(org.docx4j.vml.officedrawing.CTExtrusion.class, org.docx4j.vml.officedrawing.CTExtrusion::getOn));
		if (threeD) problems.add(WordArtText.FLAT_3D);

		String align = align(style.get("v-text-align"));
		List<WordArtText.Paragraph> paragraphs = new ArrayList<WordArtText.Paragraph>();
		String text = r.child(CTTextPath.class, CTTextPath::getString);
		for (String line : text.split("\r\n|\r|\n", -1)) {
			WordArtText.Paragraph p = new WordArtText.Paragraph();
			p.align = align;
			WordArtText.Run run = new WordArtText.Run();
			run.text = line;
			run.mark = line.isEmpty();
			run.face = face;
			run.size = size;
			run.bold = bold;
			run.italic = italic;
			run.kerned = kerned;
			run.fill = fill;
			run.colour = fillRgb == null ? ResolvedColor.BLACK : ResolvedColor.ofHex(fillRgb);
			run.outline = outline;
			run.effects = effects;
			run.shadow = effects != null;
			run.threeD = threeD;
			run.fitted = true;
			metrics(run, pkg);
			p.runs.add(run);
			paragraphs.add(p);
		}
		return new WordArtText(null, r, paragraphs, problems, true);
	}

	/** Whether the text is stretched to the shape's box ({@code fitshape}) */
	public static boolean isFitShape(VmlShapes.Resolved r) {
		return isOn(r.child(CTTextPath.class, CTTextPath::getFitshape));
	}

	/** Whether the text is sized to fill its path ({@code fitpath}) */
	public static boolean isFitPath(VmlShapes.Resolved r) {
		return isOn(r.child(CTTextPath.class, CTTextPath::getFitpath));
	}

	/**
	 * The runs' paint as the geometry's sets give it: Word fills none of the text where a set says {@code nf}, and strokes
	 * none where a set says {@code ns} (cr083-phase4's wv43-28: one set nf, the other ns, nothing drawn).
	 */
	public static void paintBySets(WordArtText art, ShapeOutlines geometry) {
		if (art == null || geometry == null) return;
		boolean fill = true, stroke = true;
		for (ShapeOutline o : geometry.getOutlines()) {
			if (o.getFill() == org.docx4j.dml.STPathFillMode.NONE) fill = false;
			if (!o.isStroked()) stroke = false;
		}
		for (WordArtText.Paragraph p : art.getParagraphs()) {
			for (WordArtText.Run r : p.getRuns()) {
				if (!fill) r.fill = new org.docx4j.dml.CTNoFillProperties();
				if (!stroke) r.outline = null;
			}
		}
	}

	/** Whether the text path trims the room for ascenders and descenders ({@code trim}) */
	public static boolean isTrim(VmlShapes.Resolved r) {
		return isOn(r.child(CTTextPath.class, CTTextPath::getTrim));
	}

	// ------------------------------------------------------------------ the style

	/** A {@code style} attribute's properties, quotes taken off */
	static Map<String, String> style(String css) {
		Map<String, String> out = new LinkedHashMap<String, String>();
		if (css == null) return out;
		for (String decl : css.split(";")) {
			int colon = decl.indexOf(':');
			if (colon <= 0) continue;
			String name = decl.substring(0, colon).trim().toLowerCase(Locale.ROOT);
			String value = decl.substring(colon + 1).trim();
			out.put(name, value);
		}
		return out;
	}

	/** The first family of {@code font-family}, else of the {@code font} shorthand's last words; Arial */
	static String face(Map<String, String> style) {
		String fam = style.get("font-family");
		if (fam == null && style.containsKey("font")) {
			String font = style.get("font");
			java.util.regex.Matcher m = java.util.regex.Pattern.compile("\\d+(?:\\.\\d+)?(?:pt|px|in|cm|mm|pc)\\s+(.+)$").matcher(font);
			if (m.find()) fam = m.group(1);
			java.util.regex.Matcher s = java.util.regex.Pattern.compile("(\\d+(?:\\.\\d+)?(?:pt|px|in|cm|mm|pc))").matcher(font);
			if (s.find() && !style.containsKey("font-size")) style.put("font-size", s.group(1));
		}
		if (fam == null) return "Arial";
		String first = fam.split(",")[0].trim();
		if (first.length() >= 2 && (first.startsWith("\"") && first.endsWith("\"") || first.startsWith("'") && first.endsWith("'"))) {
			first = first.substring(1, first.length() - 1);
		}
		return first.isEmpty() ? "Arial" : first;
	}

	/** {@code v-text-align}, Office's default center ([MS-OI29500] 2.1.1812) */
	static String align(String v) {
		if (v == null) return "center";
		switch (v.trim().toLowerCase(Locale.ROOT)) {
		case "left": return "left";
		case "right": return "right";
		case "justify": return "both";
		case "letter-justify":
		case "stretch-justify": return "distribute";
		default: return "center";
		}
	}

	private static boolean truthy(String v) {
		return v != null && ("t".equalsIgnoreCase(v.trim()) || "true".equalsIgnoreCase(v.trim()));
	}

	private static boolean isOn(org.docx4j.vml.STTrueFalse v) {
		return VmlShapes.isTrue(v);
	}

	private static boolean isOn(org.docx4j.vml.officedrawing.STTrueFalse v) {
		return v == org.docx4j.vml.officedrawing.STTrueFalse.T || v == org.docx4j.vml.officedrawing.STTrueFalse.TRUE;
	}

	/** A VML number: plain, 65536ths ({@code 32768f}) or a percentage */
	static double number(String s, double dflt) {
		if (s == null || s.trim().isEmpty()) return dflt;
		String v = s.trim();
		try {
			if (v.endsWith("f")) return Double.parseDouble(v.substring(0, v.length() - 1)) / 65536d;
			if (v.endsWith("%")) return Double.parseDouble(v.substring(0, v.length() - 1)) / 100d;
			return Double.parseDouble(v);
		} catch (NumberFormatException e) {
			return dflt;
		}
	}

	private static void metrics(WordArtText.Run run, WordprocessingMLPackage pkg) {
		try {
			org.docx4j.fonts.PhysicalFont pf = pkg == null || pkg.getFontMapper() == null ? null : pkg.getFontMapper().get(run.face);
			org.docx4j.fonts.WordLineMetrics.Metrics m = org.docx4j.fonts.WordLineMetrics.get(run.face, pf);
			run.ascent = m.winAscent;
			run.descent = m.winDescent;
			run.single = m.lineHeightFactor();
			run.leading = m.eastAsian ? 0 : m.externalLeading;
			run.emDescent = Math.max(m.winDescent, m.fopDescent);
		} catch (RuntimeException e) {
			log.debug("No line metrics for " + run.face + ": " + e.getMessage());
		}
	}

	// ------------------------------------------------------------------ paint (D19)

	/** The fill's colour: {@code v:fill color}, else {@code fillcolor}, else VML's default, white */
	public static String fillColour(VmlShapes.Resolved r) {
		String c = r.child(CTFill.class, CTFill::getColor);
		if (c == null) c = r.attribute(CTShape::getFillcolor, CTShapetype::getFillcolor);
		String rgb = VmlColours.rgb(c == null ? "white" : c);
		return rgb == null ? "FFFFFF" : rgb;
	}

	/** {@code v:fill} as a DrawingML fill: none, solid, or a gradient */
	static Object fill(VmlShapes.Resolved r, String rgb, List<String> problems) {
		if (!r.isFilled()) return new org.docx4j.dml.CTNoFillProperties();
		double opacity = number(r.child(CTFill.class, CTFill::getOpacity), Double.NaN);
		if (Double.isNaN(opacity)) opacity = number(r.attribute(CTShape::getOpacity, CTShapetype::getOpacity), 1);
		STFillType type = r.child(CTFill.class, CTFill::getType);
		if (type == STFillType.GRADIENT || type == STFillType.GRADIENT_RADIAL) {
			String rgb2 = VmlColours.rgb(r.child(CTFill.class, CTFill::getColor2), rgb);
			if (rgb2 == null) rgb2 = "FFFFFF";
			double opacity2 = number(r.child(CTFill.class, CTFill::getOpacity2), opacity);
			double focus = number(r.child(CTFill.class, CTFill::getFocus), 0);
			if (Math.abs(focus) > 1) focus /= 100; // "50" as well as "50%"
			org.docx4j.vml.STFillMethod method = r.child(CTFill.class, CTFill::getMethod);
			boolean sigma = method != org.docx4j.vml.STFillMethod.NONE && method != org.docx4j.vml.STFillMethod.LINEAR;
			List<double[]> stops = stops(run(r.child(CTFill.class, CTFill::getColors), rgb, opacity, rgb2, opacity2), focus, sigma);
			StringBuilder x = new StringBuilder("<a:gradFill xmlns:a=\"" + A + "\" rotWithShape=\"1\"><a:gsLst>");
			for (double[] s : stops) {
				x.append("<a:gs pos=\"").append(Math.round(s[0] * 100000)).append("\">")
						.append(srgb(String.format("%06X", (int) s[1]), s[2])).append("</a:gs>");
			}
			x.append("</a:gsLst>");
			if (type == STFillType.GRADIENT_RADIAL) {
				x.append("<a:path path=\"circle\"><a:fillToRect l=\"50000\" t=\"50000\" r=\"50000\" b=\"50000\"/></a:path>");
			} else {
				java.math.BigDecimal angle = r.child(CTFill.class, CTFill::getAngle);
				double vml = angle == null ? 0 : angle.doubleValue();
				double ang = ((90 - vml) % 360 + 360) % 360; // 0 blends down (wv45-09), counterclockwise positive
				// the angle in the box's own square, as Word draws it (wv45-10: 45 degrees in a 2:1 box at 63.4 on the page)
				x.append("<a:lin ang=\"").append(Math.round(ang * 60000)).append("\" scaled=\"1\"/>");
			}
			x.append("</a:gradFill>");
			Object g = read(x.toString(), org.docx4j.dml.CTGradientFillProperties.class);
			if (g != null) return g;
		} else if (type == STFillType.TILE || type == STFillType.PATTERN || type == STFillType.FRAME) {
			// a picture fill: drawn in its colour, reported
			if (!problems.contains(WordArtText.FILL)) problems.add(WordArtText.FILL);
		}
		return read("<a:solidFill xmlns:a=\"" + A + "\">" + srgb(rgb, opacity) + "</a:solidFill>",
				org.docx4j.dml.CTSolidColorFillProperties.class);
	}

	/**
	 * Word's sigma blend, VML's default gradient method: {@code color2}'s weight at each sixteenth of the way from
	 * {@code color}, in gamma-2.2 light (cr083-phase4's wv45-09, red to blue: Word's 256 samples, red and blue summing
	 * to 1 within 0.005 at gamma 2.2; this table within 11 of 255 of every sample, the middle (187, 0, 186) where an
	 * even blend gives (128, 0, 128))
	 */
	private static final double[] SIGMA = { 0, .018, .0443, .0854, .1422, .2147, .2996, .3976, .5, .6024, .7004, .7853,
			.8578, .9146, .9557, .982, 1 };

	private static final double GAMMA = 2.2;

	/**
	 * A gradient's colours from {@code color} (0) to {@code color2} (1), each {position, rgb, opacity}: its
	 * {@code colors} list where it has one ("0 red;32768f yellow;1 blue", a position a fraction, {@code f} 65,536ths,
	 * wv45-15), held at its first and last colours to 0 and 1, the opacity from {@code opacity} to {@code opacity2}
	 */
	static List<double[]> run(String colors, String c1, double o1, String c2, double o2) {
		List<double[]> out = new ArrayList<double[]>();
		if (colors != null) {
			for (String e : colors.split(";")) {
				String t = e.trim();
				int sp = t.indexOf(' ');
				if (sp < 0) continue;
				double p = number(t.substring(0, sp), Double.NaN);
				String c = VmlColours.rgb(t.substring(sp + 1).trim());
				if (Double.isNaN(p) || c == null) continue;
				p = Math.max(0, Math.min(1, p));
				out.add(new double[] { p, Integer.parseInt(c, 16), o1 + (o2 - o1) * p });
			}
			out.sort((a, b) -> Double.compare(a[0], b[0]));
		}
		if (out.size() < 2) {
			out.clear();
			out.add(new double[] { 0, Integer.parseInt(c1, 16), o1 });
			out.add(new double[] { 1, Integer.parseInt(c2, 16), o2 });
			return out;
		}
		if (out.get(0)[0] > 0) out.add(0, new double[] { 0, out.get(0)[1], o1 });
		double[] last = out.get(out.size() - 1);
		if (last[0] < 1) out.add(new double[] { 1, last[1], o2 });
		return out;
	}

	/**
	 * A gradient's stops along its axis, each {position 0 to 1, rgb, opacity}, from its colours ({@link #run}) and
	 * its focus, as Word draws them (wv45-09 to -15): the run from {@code color} at the ends to {@code color2} at the
	 * focus, so with focus 0 from {@code color2} at the axis's start to {@code color} at its end, with 100% the
	 * reverse, with 50% {@code color} at both ends and {@code color2} in the middle, each half the whole run; a
	 * negative focus the colours swapped and the focus taken from the far end (-50% measured, -100% as Part 4 has it,
	 * as 100%); with {@code sigma} each step of the run blended by Word's curve ({@link #SIGMA}).
	 */
	static List<double[]> stops(List<double[]> run, double focus, boolean sigma) {
		List<double[]> r = run;
		double at = focus;
		if (focus < 0) {
			r = new ArrayList<double[]>();
			for (int i = run.size() - 1; i >= 0; i--) r.add(new double[] { 1 - run.get(i)[0], run.get(i)[1], run.get(i)[2] });
			at = 1 + focus;
		}
		at = Math.max(0, Math.min(1, at));
		if (sigma) r = sigma(r);
		List<double[]> out = new ArrayList<double[]>();
		if (at > 0) {
			for (double[] s : r) out.add(new double[] { at * s[0], s[1], s[2] });
		}
		if (at < 1) {
			for (int i = r.size() - 1; i >= 0; i--) {
				double[] s = r.get(i);
				double x = at + (1 - at) * (1 - s[0]);
				if (!out.isEmpty() && Math.abs(out.get(out.size() - 1)[0] - x) < 1e-9) continue;
				out.add(new double[] { x, s[1], s[2] });
			}
		}
		return out;
	}

	/** The run with each step blended by Word's sigma curve, at sixteenths */
	private static List<double[]> sigma(List<double[]> run) {
		List<double[]> out = new ArrayList<double[]>();
		for (int i = 0; i + 1 < run.size(); i++) {
			double[] a = run.get(i), b = run.get(i + 1);
			for (int k = i == 0 ? 0 : 1; k < SIGMA.length; k++) {
				double w = SIGMA[k];
				out.add(new double[] { a[0] + (b[0] - a[0]) * k / (SIGMA.length - 1), blend((int) a[1], (int) b[1], w),
						a[2] + (b[2] - a[2]) * w });
			}
		}
		return out;
	}

	/** Two colours blended in gamma-2.2 light, {@code w} of the second */
	static int blend(int a, int b, double w) {
		int rgb = 0;
		for (int shift = 16; shift >= 0; shift -= 8) {
			double ca = ((a >> shift) & 255) / 255.0, cb = ((b >> shift) & 255) / 255.0;
			double v = Math.pow((1 - w) * Math.pow(ca, GAMMA) + w * Math.pow(cb, GAMMA), 1 / GAMMA);
			rgb |= (int) Math.round(Math.max(0, Math.min(1, v)) * 255) << shift;
		}
		return rgb;
	}

	/** {@code v:stroke} as a DrawingML outline, or null where it isn't stroked */
	static org.docx4j.dml.CTLineProperties outline(VmlShapes.Resolved r, String fillRgb, List<String> problems) {
		if (!r.isStroked()) return null;
		String c = r.child(CTStroke.class, CTStroke::getColor);
		if (c == null) c = r.attribute(CTShape::getStrokecolor, CTShapetype::getStrokecolor);
		String rgb = VmlColours.rgb(c == null ? "black" : c, fillRgb);
		if (rgb == null) rgb = "000000";
		double opacity = number(r.child(CTStroke.class, CTStroke::getOpacity), 1);
		long w = Math.round(r.strokeWeightPt() * 12700);
		org.docx4j.vml.STStrokeLineStyle ls = r.child(CTStroke.class, CTStroke::getLinestyle);
		if (ls != null && ls != org.docx4j.vml.STStrokeLineStyle.SINGLE && !problems.contains(WordArtText.OUTLINE)) {
			problems.add(WordArtText.OUTLINE); // a compound line: drawn single
		}
		org.docx4j.vml.STStrokeJoinStyle join = r.child(CTStroke.class, CTStroke::getJoinstyle);
		org.docx4j.vml.STStrokeEndCap cap = r.child(CTStroke.class, CTStroke::getEndcap);
		String capAttr = cap == org.docx4j.vml.STStrokeEndCap.ROUND ? "rnd" : cap == org.docx4j.vml.STStrokeEndCap.SQUARE ? "sq" : "flat";
		StringBuilder x = new StringBuilder("<a:ln xmlns:a=\"" + A + "\" w=\"" + w + "\" cap=\"" + capAttr + "\">");
		x.append("<a:solidFill>").append(srgb(rgb, opacity)).append("</a:solidFill>");
		x.append("<a:prstDash val=\"").append(dash(r.child(CTStroke.class, CTStroke::getDashstyle))).append("\"/>");
		if (join == org.docx4j.vml.STStrokeJoinStyle.BEVEL) x.append("<a:bevel/>");
		else if (join == org.docx4j.vml.STStrokeJoinStyle.MITER) x.append("<a:miter lim=\"800000\"/>");
		else x.append("<a:round/>"); // VML's default
		x.append("</a:ln>");
		return (org.docx4j.dml.CTLineProperties) read(x.toString(), org.docx4j.dml.CTLineProperties.class);
	}

	/** VML's {@code dashstyle} as DrawingML's preset dash; solid for a custom one */
	static String dash(String v) {
		if (v == null) return "solid";
		switch (v.trim().toLowerCase(Locale.ROOT)) {
		case "shortdash": return "sysDash";
		case "shortdot": return "sysDot";
		case "shortdashdot": return "sysDashDot";
		case "shortdashdotdot": return "sysDashDotDot";
		case "dot": return "dot";
		case "dash": return "dash";
		case "longdash": return "lgDash";
		case "dashdot": return "dashDot";
		case "longdashdot": return "lgDashDot";
		case "longdashdotdot": return "lgDashDotDot";
		default: return "solid";
		}
	}

	/**
	 * {@code v:shadow} as Word draws it on VML WordArt (cr083-phase4's wv45): copies of the glyphs, filled, no stroke,
	 * under them, each {dx, dy, rgb, opacity} in the order drawn: for a double shadow the second ({@code offset2},
	 * {@code color2}) then the first; an offset a percentage of the shape's width and height; the defaults 2pt,2pt and
	 * gray. An emboss is a double shadow (wv45-35: a copy in #CBCBCB at -2pt,-2pt, then one in its colour at 2pt,2pt,
	 * then the glyphs; at its defaults only). A perspective (copies Word turns and lightens) none, reported in {@link #of}.
	 */
	public static java.util.List<double[]> shadows(VmlShapes.Resolved r, double w, double h) {
		java.util.List<double[]> out = new java.util.ArrayList<double[]>();
		if (r == null || !r.has(CTShadow.class) || !isOn(r.child(CTShadow.class, CTShadow::getOn))) return out;
		STShadowType type = r.child(CTShadow.class, CTShadow::getType);
		if (type == STShadowType.PERSPECTIVE) return out;
		String fillRgb = fillColour(r);
		double opacity = number(r.child(CTShadow.class, CTShadow::getOpacity), 1);
		if (type == STShadowType.DOUBLE || type == STShadowType.EMBOSS) {
			double[] o2 = offset(r.child(CTShadow.class, CTShadow::getOffset2), w, h, -2, -2);
			String c2 = VmlColours.rgb(r.child(CTShadow.class, CTShadow::getColor2), fillRgb);
			out.add(new double[] { o2[0], o2[1], Integer.parseInt(c2 == null ? "CBCBCB" : c2, 16), opacity });
		}
		double[] o = offset(r.child(CTShadow.class, CTShadow::getOffset), w, h, 2, 2);
		String c = VmlColours.rgb(r.child(CTShadow.class, CTShadow::getColor), fillRgb);
		out.add(new double[] { o[0], o[1], Integer.parseInt(c == null ? "808080" : c, 16), opacity });
		return out;
	}

	/** A shadow's offset: lengths, or percentages of the shape's width and height; the default where left out */
	static double[] offset(String v, double w, double h, double dx, double dy) {
		if (v == null || v.trim().isEmpty()) return new double[] { dx, dy };
		String[] p = v.split(",", -1);
		double[] out = { dx, dy };
		for (int i = 0; i < 2 && i < p.length; i++) {
			String s = p[i].trim();
			if (s.isEmpty()) { out[i] = 0; continue; }
			out[i] = s.endsWith("%") ? number(s, 0) * (i == 0 ? w : h) : VmlShapes.length(s, 0);
		}
		return out;
	}

	/** {@code v:shadow} reported where it isn't drawn as Word draws it: perspective ({@link #shadows} draws the rest) */
	static void shadow(VmlShapes.Resolved r, String fillRgb, List<String> problems) {
		if (!r.has(CTShadow.class) || !isOn(r.child(CTShadow.class, CTShadow::getOn))) return;
		STShadowType type = r.child(CTShadow.class, CTShadow::getType);
		if (type == STShadowType.PERSPECTIVE && !problems.contains(SHADOW)) problems.add(SHADOW);
	}

	/**
	 * Whether Word draws this WordArt not at all, whatever its text: its path holds a {@code qb} (cr083-phase4's wv43-27,
	 * cr083-phase4b's wv49-01 to -03: the control point inside the box or above it, on the top path or the bottom,
	 * nothing drawn, not even the shape's stroke; Word's save keeps the path). Unmeasured on VML other than WordArt.
	 */
	public static boolean isUndrawn(VmlShapes.Resolved r) {
		String p = r.getPath();
		return p != null && p.contains("qb");
	}

	/** Whether the text path's glyphs are all one height ({@code v-same-letter-heights}) */
	public static boolean isSameLetterHeights(VmlShapes.Resolved r) {
		String v = style(r.child(CTTextPath.class, CTTextPath::getStyle)).get("v-same-letter-heights");
		return truthy(v);
	}

	private static String srgb(String rgb, double opacity) {
		StringBuilder s = new StringBuilder("<a:srgbClr val=\"").append(rgb).append("\">");
		if (opacity < 1) s.append("<a:alpha val=\"").append(Math.max(0, Math.round(opacity * 100000))).append("\"/>");
		return s.append("</a:srgbClr>").toString();
	}

	private static Object read(String xml, Class<?> as) {
		try {
			return XmlUtils.unwrap(XmlUtils.unmarshalString(xml, Context.jc, as));
		} catch (Exception e) {
			log.warn("VML WordArt paint couldn't be read: " + e.getMessage());
			return null;
		}
	}
}
