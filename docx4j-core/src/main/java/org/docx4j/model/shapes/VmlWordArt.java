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
 * Provisional until the phase's round (phase 4 findings §3) measures Word: a gradient's {@code angle} (0 taken as
 * blending down, positive counterclockwise), its {@code focus} (0 from colour to {@code color2}, 100 the reverse, 50
 * and -50 mirrored about the middle); a {@code v:shadow} with no {@code on} taken as off (Part 4's default is on,
 * Office's legacy WordArt writes one on styles with no shadow); a shadow's lengths in points; a picture fill drawn in
 * its colour and reported, a compound line drawn single and reported, a double, perspective or emboss shadow drawn
 * single (or, emboss, not) and reported; {@code v-text-spacing} not applied; the default face Arial and size 36 pt.
 *
 * @since 17.3.7
 */
public final class VmlWordArt {

	private static final Logger log = LoggerFactory.getLogger(VmlWordArt.class);

	/** A VML WordArt's geometry that can't be evaluated or is past a limit: nothing drawn */
	public static final String BAD_GEOMETRY = "render.wordart.bad_geometry";

	/** A VML shadow not drawn as Word draws it: double, perspective, emboss, or offset as a percentage */
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
		org.docx4j.dml.CTEffectList effects = shadow(r, fillRgb, problems);
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
		} catch (RuntimeException e) {
			log.debug("No line metrics for " + run.face + ": " + e.getMessage());
		}
	}

	// ------------------------------------------------------------------ paint (D19)

	/** The fill's colour: {@code v:fill color}, else {@code fillcolor}, else VML's default, white */
	static String fillColour(VmlShapes.Resolved r) {
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
			String[][] stops = stops(rgb, opacity, rgb2, opacity2, focus);
			StringBuilder x = new StringBuilder("<a:gradFill xmlns:a=\"" + A + "\" rotWithShape=\"1\"><a:gsLst>");
			for (String[] s : stops) x.append("<a:gs pos=\"").append(s[0]).append("\">").append(srgb(s[1], Double.parseDouble(s[2]))).append("</a:gs>");
			x.append("</a:gsLst>");
			if (type == STFillType.GRADIENT_RADIAL) {
				x.append("<a:path path=\"circle\"><a:fillToRect l=\"50000\" t=\"50000\" r=\"50000\" b=\"50000\"/></a:path>");
			} else {
				java.math.BigDecimal angle = r.child(CTFill.class, CTFill::getAngle);
				double vml = angle == null ? 0 : angle.doubleValue();
				double ang = ((90 - vml) % 360 + 360) % 360; // provisional: 0 blends down, counterclockwise positive
				x.append("<a:lin ang=\"").append(Math.round(ang * 60000)).append("\" scaled=\"0\"/>");
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

	/** A gradient's stops by its focus: {pos in 100,000ths, rgb, opacity} */
	private static String[][] stops(String c1, double o1, String c2, double o2, double focus) {
		if (Math.abs(focus) < 1e-9) return new String[][] { { "0", c1, "" + o1 }, { "100000", c2, "" + o2 } };
		if (Math.abs(focus - 1) < 1e-9) return new String[][] { { "0", c2, "" + o2 }, { "100000", c1, "" + o1 } };
		if (focus > 0) {
			long mid = Math.round(focus * 100000);
			return new String[][] { { "0", c1, "" + o1 }, { "" + mid, c2, "" + o2 }, { "100000", c1, "" + o1 } };
		}
		long mid = Math.round(Math.min(1, -focus) * 100000);
		return new String[][] { { "0", c2, "" + o2 }, { "" + mid, c1, "" + o1 }, { "100000", c2, "" + o2 } };
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

	/** {@code v:shadow} as an {@code a:effectLst} of one sharp outer shadow, or null where it is off */
	static org.docx4j.dml.CTEffectList shadow(VmlShapes.Resolved r, String fillRgb, List<String> problems) {
		if (!r.has(CTShadow.class) || !isOn(r.child(CTShadow.class, CTShadow::getOn))) return null;
		STShadowType type = r.child(CTShadow.class, CTShadow::getType);
		if (type == STShadowType.EMBOSS) {
			if (!problems.contains(SHADOW)) problems.add(SHADOW);
			return null;
		}
		if (type == STShadowType.DOUBLE || type == STShadowType.PERSPECTIVE) {
			if (!problems.contains(SHADOW)) problems.add(SHADOW); // drawn as a single shadow
		}
		String offset = r.child(CTShadow.class, CTShadow::getOffset);
		double dx = 2, dy = 2;
		if (offset != null) {
			String[] p = offset.split(",", -1);
			if (offset.contains("%")) {
				if (!problems.contains(SHADOW)) problems.add(SHADOW);
			} else {
				dx = VmlShapes.length(p.length > 0 && !p[0].trim().isEmpty() ? p[0] : "0", 0);
				dy = VmlShapes.length(p.length > 1 && !p[1].trim().isEmpty() ? p[1] : "0", 0);
			}
		}
		String rgb = VmlColours.rgb(r.child(CTShadow.class, CTShadow::getColor), fillRgb);
		if (rgb == null) rgb = "808080"; // VML's default, gray
		double opacity = number(r.child(CTShadow.class, CTShadow::getOpacity), 1);
		long dist = Math.round(Math.hypot(dx, dy) * 12700);
		double dir = Math.toDegrees(Math.atan2(dy, dx));
		if (dir < 0) dir += 360;
		String x = "<a:effectLst xmlns:a=\"" + A + "\"><a:outerShdw blurRad=\"0\" dist=\"" + dist + "\" dir=\""
				+ Math.round(dir * 60000) + "\" algn=\"ctr\" rotWithShape=\"0\">" + srgb(rgb, opacity) + "</a:outerShdw></a:effectLst>";
		return (org.docx4j.dml.CTEffectList) read(x, org.docx4j.dml.CTEffectList.class);
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
