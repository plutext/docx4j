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
package org.docx4j.model.color;

import java.util.List;

import jakarta.xml.bind.JAXBElement;

import org.docx4j.XmlUtils;
import org.docx4j.dml.CTBlipFillProperties;
import org.docx4j.dml.CTColor;
import org.docx4j.dml.CTFontReference;
import org.docx4j.dml.CTGradientFillProperties;
import org.docx4j.dml.CTGroupFillProperties;
import org.docx4j.dml.CTHslColor;
import org.docx4j.dml.CTLineProperties;
import org.docx4j.dml.CTNoFillProperties;
import org.docx4j.dml.CTPatternFillProperties;
import org.docx4j.dml.CTPresetColor;
import org.docx4j.dml.CTSRgbColor;
import org.docx4j.dml.CTScRgbColor;
import org.docx4j.dml.CTSchemeColor;
import org.docx4j.dml.CTSolidColorFillProperties;
import org.docx4j.dml.CTStyleMatrix;
import org.docx4j.dml.CTStyleMatrixReference;
import org.docx4j.dml.CTSystemColor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * DrawingML colours (ISO/IEC 29500-1 §20.1.2.3) resolved as Office draws them.  The six kinds:
 * {@code srgbClr}, {@code scrgbClr} (linear percentages), {@code hslClr}, {@code sysClr} (the
 * machine's colour by name, not {@code lastClr}), {@code prstClr} and {@code schemeClr} (through
 * {@link ThemeColors}).  Their transforms apply in document order, in the spaces Word and
 * PowerPoint use, measured on 403 cases each (CR-034 §11, P3):
 * <ul>
 * <li>{@code tint}, {@code shade}, {@code inv}, {@code red}/{@code green}/{@code blue} and their
 *     {@code Off}/{@code Mod}: linear RGB; {@code gamma} and {@code invGamma}: the sRGB transfer
 *     function each way;</li>
 * <li>{@code hue}, {@code sat}, {@code lum} and their {@code Off}/{@code Mod}, {@code comp}: HSL of
 *     the sRGB colour, the saturation not clamped (the channels are, afterwards); a colour whose last
 *     change is a luminance transform converted back as Foley and van Dam do and rounded half down,
 *     as Office rounds it (so {@code tx1} at {@code lumMod 50000 lumOff 50000} is 7F7F7F);</li>
 * <li>{@code gray}: Rec.709 luma of the sRGB channels;</li>
 * <li>{@code alpha}, {@code alphaOff}, {@code alphaMod}: the alpha.</li>
 * </ul>
 * MS-OI29500 says {@code lumOff} and {@code satOff} are not supported by Office; Office 365 applies
 * both as above in every case measured.  Known differences from Office: a saturation set on a grey
 * (Office gives a grey's hue about 40 degrees), and some hue, saturation and luminance cases one
 * unit off.
 *
 * @since 17.3.3 (CR-034)
 */
public final class DrawingMLColors {

	private static final Logger log = LoggerFactory.getLogger(DrawingMLColors.class);

	private DrawingMLColors() {
	}

	/**
	 * The colour a DrawingML colour element, or an element holding one, resolves to.
	 *
	 * @param colour one of the six kinds ({@code CTSchemeColor}, {@code CTSRgbColor}, ...), or a
	 *        holder of one: {@code CTColor} (a scheme slot), {@code CTSolidColorFillProperties},
	 *        {@code CTStyleMatrixReference}, {@code CTFontReference}; a JAXBElement is unwrapped
	 * @param theme the theme and colour map for {@code schemeClr}; may be null where there is none
	 * @return null where there is no colour, or a {@code schemeClr} cannot be resolved
	 */
	public static ResolvedColor resolve(Object colour, ThemeColors theme) {
		Object c = kind(XmlUtils.unwrap(colour));
		if (c == null) return null;
		ResolvedColor base = base(c, theme);
		if (base == null) return null;
		return transform(base, transforms(c));
	}

	/** The colour element inside a holder, or the element itself */
	private static Object kind(Object o) {
		if (o instanceof CTColor) {
			CTColor h = (CTColor) o;
			return first(h.getSrgbClr(), h.getSchemeClr(), h.getScrgbClr(), h.getHslClr(), h.getSysClr(), h.getPrstClr());
		}
		if (o instanceof CTSolidColorFillProperties) {
			CTSolidColorFillProperties h = (CTSolidColorFillProperties) o;
			return first(h.getSrgbClr(), h.getSchemeClr(), h.getScrgbClr(), h.getHslClr(), h.getSysClr(), h.getPrstClr());
		}
		if (o instanceof CTStyleMatrixReference) {
			CTStyleMatrixReference h = (CTStyleMatrixReference) o;
			return first(h.getSrgbClr(), h.getSchemeClr(), h.getScrgbClr(), h.getHslClr(), h.getSysClr(), h.getPrstClr());
		}
		if (o instanceof CTFontReference) {
			CTFontReference h = (CTFontReference) o;
			return first(h.getSrgbClr(), h.getSchemeClr(), h.getScrgbClr(), h.getHslClr(), h.getSysClr(), h.getPrstClr());
		}
		return o;
	}

	private static Object first(Object... os) {
		for (Object o : os) {
			if (o != null) return o;
		}
		return null;
	}

	private static ResolvedColor base(Object c, ThemeColors theme) {
		if (c instanceof CTSRgbColor) {
			return ResolvedColor.ofHex(((CTSRgbColor) c).getVal());
		}
		if (c instanceof CTSchemeColor) {
			CTSchemeColor s = (CTSchemeColor) c;
			ResolvedColor r = theme == null || s.getVal() == null ? null : theme.schemeColor(s.getVal().value());
			if (r == null) log.debug("Couldn't resolve schemeClr " + (s.getVal() == null ? null : s.getVal().value()));
			return r;
		}
		if (c instanceof CTScRgbColor) {
			CTScRgbColor s = (CTScRgbColor) c;
			return ResolvedColor.rgb(ColorMath.channel(ColorMath.toSrgb(s.getR() / 100000.0)),
					ColorMath.channel(ColorMath.toSrgb(s.getG() / 100000.0)), ColorMath.channel(ColorMath.toSrgb(s.getB() / 100000.0)));
		}
		if (c instanceof CTHslColor) {
			CTHslColor h = (CTHslColor) c;
			double[] rgb = ColorMath.hslToRgb(h.getHue() / 21600000.0, h.getSat() / 100000.0, h.getLum() / 100000.0);
			return ResolvedColor.rgb(ColorMath.channel(rgb[0]), ColorMath.channel(rgb[1]), ColorMath.channel(rgb[2]));
		}
		if (c instanceof CTSystemColor) {
			CTSystemColor s = (CTSystemColor) c;
			// Office draws the machine's colour for the name and ignores lastClr (CR-034 §11)
			String hex = s.getVal() == null ? null : ColorTables.SYSTEM.get(s.getVal());
			if (hex != null) return ResolvedColor.ofHex(hex);
			byte[] last = s.getLastClr();
			return last != null && last.length == 3 ? ResolvedColor.rgb(last[0] & 0xFF, last[1] & 0xFF, last[2] & 0xFF) : null;
		}
		if (c instanceof CTPresetColor) {
			CTPresetColor p = (CTPresetColor) c;
			return p.getVal() == null ? null : ResolvedColor.ofHex(ColorTables.PRESET.get(p.getVal().value()));
		}
		return null;
	}

	private static List<JAXBElement<?>> transforms(Object c) {
		if (c instanceof CTSRgbColor) return ((CTSRgbColor) c).getEGColorTransform();
		if (c instanceof CTSchemeColor) return ((CTSchemeColor) c).getEGColorTransform();
		if (c instanceof CTScRgbColor) return ((CTScRgbColor) c).getEGColorTransform();
		if (c instanceof CTHslColor) return ((CTHslColor) c).getEGColorTransform();
		if (c instanceof CTSystemColor) return ((CTSystemColor) c).getEGColorTransform();
		if (c instanceof CTPresetColor) return ((CTPresetColor) c).getEGColorTransform();
		return null;
	}

	/** The transforms' value, in thousandths of a percent or 60000ths of a degree; 0 for those without one */
	private static int value(Object t) {
		if (t instanceof org.docx4j.dml.CTPercentage) return ((org.docx4j.dml.CTPercentage) t).getVal();
		if (t instanceof org.docx4j.dml.CTPositivePercentage) return ((org.docx4j.dml.CTPositivePercentage) t).getVal();
		if (t instanceof org.docx4j.dml.CTPositiveFixedPercentage) return ((org.docx4j.dml.CTPositiveFixedPercentage) t).getVal();
		if (t instanceof org.docx4j.dml.CTFixedPercentage) return ((org.docx4j.dml.CTFixedPercentage) t).getVal();
		if (t instanceof org.docx4j.dml.CTAngle) return ((org.docx4j.dml.CTAngle) t).getVal();
		if (t instanceof org.docx4j.dml.CTPositiveFixedAngle) return ((org.docx4j.dml.CTPositiveFixedAngle) t).getVal();
		return 0;
	}

	/** A colour with transforms applied in document order */
	static ResolvedColor transform(ResolvedColor base, List<JAXBElement<?>> transforms) {
		if (transforms == null || transforms.isEmpty()) return base;
		double r = base.getRed() / 255.0, g = base.getGreen() / 255.0, b = base.getBlue() / 255.0;
		double alpha = base.getAlpha() / 100000.0;
		boolean lumLast = false;   // the last change to the colour a luminance transform: it rounds half down
		for (JAXBElement<?> t : transforms) {
			String name = t.getName().getLocalPart();
			double v = value(t.getValue()) / 100000.0;
			if (!name.startsWith("alpha")) lumLast = name.startsWith("lum");
			switch (name) {
			case "tint":
				r = ColorMath.toSrgb(1 - (1 - ColorMath.toLinear(r)) * v);
				g = ColorMath.toSrgb(1 - (1 - ColorMath.toLinear(g)) * v);
				b = ColorMath.toSrgb(1 - (1 - ColorMath.toLinear(b)) * v);
				break;
			case "shade":
				r = ColorMath.toSrgb(ColorMath.toLinear(r) * v);
				g = ColorMath.toSrgb(ColorMath.toLinear(g) * v);
				b = ColorMath.toSrgb(ColorMath.toLinear(b) * v);
				break;
			case "inv":
				r = ColorMath.toSrgb(1 - ColorMath.toLinear(r));
				g = ColorMath.toSrgb(1 - ColorMath.toLinear(g));
				b = ColorMath.toSrgb(1 - ColorMath.toLinear(b));
				break;
			case "gray": {
				double y = 0.2126 * r + 0.7152 * g + 0.0722 * b;
				r = g = b = ColorMath.clamp01(y);
				break;
			}
			case "gamma":
				r = ColorMath.toSrgb(r);
				g = ColorMath.toSrgb(g);
				b = ColorMath.toSrgb(b);
				break;
			case "invGamma":
				r = ColorMath.toLinear(r);
				g = ColorMath.toLinear(g);
				b = ColorMath.toLinear(b);
				break;
			case "red": case "redOff": case "redMod":
				r = channel(name, r, v);
				break;
			case "green": case "greenOff": case "greenMod":
				g = channel(name, g, v);
				break;
			case "blue": case "blueOff": case "blueMod":
				b = channel(name, b, v);
				break;
			case "alpha":
				alpha = v;
				break;
			case "alphaOff":
				alpha += v;
				break;
			case "alphaMod":
				alpha *= v;
				break;
			case "comp": case "hue": case "hueOff": case "hueMod":
			case "sat": case "satOff": case "satMod":
			case "lum": case "lumOff": case "lumMod": {
				double[] hsl = ColorMath.rgbToHsl(r, g, b);
				double h = hsl[0], s = hsl[1], l = hsl[2];
				int raw = value(t.getValue());
				switch (name) {
				case "comp": h += 0.5; break;
				case "hue": h = raw / 21600000.0; break;
				case "hueOff": h += raw / 21600000.0; break;
				case "hueMod": h *= v; break;
				case "sat": s = v; break;
				case "satOff": s += v; break;
				case "satMod": s *= v; break;
				case "lum": l = v; break;
				case "lumOff": l += v; break;
				// l x val / 100000 in this order, not l *= v: with hslToRgbFoley and channelHalfDown, which way an exact
				// half rounds follows this form's floating-point error, as Office's does (CR-034 §11, follow-up: measured)
				case "lumMod": l = l * raw / 100000.0; break;
				default: break;
				}
				h = h % 1;
				if (h < 0) h += 1;
				double[] rgb = lumLast ? ColorMath.hslToRgbFoley(h, s, ColorMath.clamp01(l)) : ColorMath.hslToRgb(h, s, ColorMath.clamp01(l));
				r = rgb[0];
				g = rgb[1];
				b = rgb[2];
				break;
			}
			default:
				log.debug("Ignoring an unknown colour transform " + name);
			}
		}
		if (lumLast) return ResolvedColor.rgb(ColorMath.channelHalfDown(r), ColorMath.channelHalfDown(g), ColorMath.channelHalfDown(b))
				.withAlpha((int) Math.round(ColorMath.clamp01(alpha) * 100000));
		return ResolvedColor.rgb(ColorMath.channel(r), ColorMath.channel(g), ColorMath.channel(b))
				.withAlpha((int) Math.round(ColorMath.clamp01(alpha) * 100000));
	}

	/** red, redOff, redMod (and green, blue): the channel in linear RGB */
	private static double channel(String name, double c, double v) {
		double l = ColorMath.toLinear(c);
		if (name.endsWith("Off")) l += v;
		else if (name.endsWith("Mod")) l *= v;
		else l = v;
		return ColorMath.toSrgb(l);
	}

	// ------------------------------------------------------------------ fills and style references

	/**
	 * A fill: {@code a:noFill}, {@code a:solidFill} (its colour resolved), or a gradient, pattern,
	 * picture or group fill reported by kind.
	 */
	public static StyleFill fill(Object fillProperties, ThemeColors theme) {
		Object f = XmlUtils.unwrap(fillProperties);
		if (f == null) return StyleFill.UNKNOWN;
		if (f instanceof CTNoFillProperties) return StyleFill.NONE;
		if (f instanceof CTSolidColorFillProperties) return new StyleFill(StyleFill.Kind.SOLID, resolve(f, theme), f);
		if (f instanceof CTGradientFillProperties) return new StyleFill(StyleFill.Kind.GRADIENT, null, f);
		if (f instanceof CTPatternFillProperties) return new StyleFill(StyleFill.Kind.PATTERN, null, f);
		if (f instanceof CTBlipFillProperties) return new StyleFill(StyleFill.Kind.PICTURE, null, f);
		if (f instanceof CTGroupFillProperties) return new StyleFill(StyleFill.Kind.GROUP, null, f);
		return StyleFill.UNKNOWN;
	}

	/** A line's fill: none, solid, gradient or pattern */
	public static StyleFill lineFill(CTLineProperties ln, ThemeColors theme) {
		if (ln == null) return StyleFill.UNKNOWN;
		if (ln.getNoFill() != null) return StyleFill.NONE;
		if (ln.getSolidFill() != null) return fill(ln.getSolidFill(), theme);
		if (ln.getGradFill() != null) return fill(ln.getGradFill(), theme);
		if (ln.getPattFill() != null) return fill(ln.getPattFill(), theme);
		return StyleFill.UNKNOWN;
	}

	/**
	 * A shape style's {@code a:fillRef}: index 0 is no fill, 1 to 999 the theme's
	 * {@code a:fillStyleLst}, 1001 and up its {@code a:bgFillStyleLst}, with {@code phClr} the
	 * reference's own colour.
	 */
	public static StyleFill fillReference(CTStyleMatrixReference ref, ThemeColors theme) {
		if (ref == null) return StyleFill.UNKNOWN;
		long idx = ref.getIdx();
		if (idx == 0) return StyleFill.NONE;
		CTStyleMatrix fmt = theme == null ? null : theme.getFormatScheme();
		if (fmt == null) return StyleFill.UNKNOWN;
		List<Object> list = idx >= 1001
				? (fmt.getBgFillStyleLst() == null ? null : fmt.getBgFillStyleLst().getEGFillProperties())
				: (fmt.getFillStyleLst() == null ? null : fmt.getFillStyleLst().getEGFillProperties());
		int i = (int) (idx >= 1001 ? idx - 1001 : idx - 1);
		if (list == null || i < 0 || i >= list.size()) return StyleFill.UNKNOWN;
		return fill(list.get(i), theme.withPlaceholder(resolve(ref, theme)));
	}

	/** A shape style's {@code a:lnRef}: index 0 is no line, else the theme's {@code a:lnStyleLst} entry's fill */
	public static StyleFill lineReference(CTStyleMatrixReference ref, ThemeColors theme) {
		if (ref == null) return StyleFill.UNKNOWN;
		long idx = ref.getIdx();
		if (idx == 0) return StyleFill.NONE;
		CTStyleMatrix fmt = theme == null ? null : theme.getFormatScheme();
		if (fmt == null || fmt.getLnStyleLst() == null) return StyleFill.UNKNOWN;
		List<CTLineProperties> list = fmt.getLnStyleLst().getLn();
		int i = (int) idx - 1;
		if (i < 0 || i >= list.size()) return StyleFill.UNKNOWN;
		return lineFill(list.get(i), theme.withPlaceholder(resolve(ref, theme)));
	}

	/** A shape style's {@code a:fontRef}: its colour, the text's where the text states none */
	public static ResolvedColor fontReference(CTFontReference ref, ThemeColors theme) {
		return ref == null ? null : resolve(ref, theme);
	}
}
