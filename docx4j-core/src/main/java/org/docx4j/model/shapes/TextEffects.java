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

import javax.xml.namespace.QName;

import jakarta.xml.bind.JAXBElement;

import org.docx4j.XmlUtils;
import org.docx4j.dml.CTEffectList;
import org.docx4j.dml.CTGlowEffect;
import org.docx4j.dml.CTInnerShadowEffect;
import org.docx4j.dml.CTLineProperties;
import org.docx4j.dml.CTNoFillProperties;
import org.docx4j.dml.CTOuterShadowEffect;
import org.docx4j.dml.CTReflectionEffect;
import org.docx4j.dml.CTSolidColorFillProperties;
import org.docx4j.jaxb.Context;
import org.docx4j.w14.CTFillTextEffect;
import org.docx4j.w14.CTGlow;
import org.docx4j.w14.CTReflection;
import org.docx4j.w14.CTShadow;
import org.docx4j.w14.CTTextOutlineEffect;
import org.docx4j.wml.RPr;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.w3c.dom.Attr;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;

/**
 * A run's Word 2010 text effects ({@code w14:textFill}, {@code w14:textOutline}; {@code w14:glow}, {@code w14:shadow},
 * {@code w14:reflection}) as the DrawingML they mirror, for
 * docx4j's DrawingML painting (CR-083): w14's fills and lines are DrawingML's elements in another namespace, with their
 * attributes qualified. One difference is measured (phase 0, findings section 1): {@code w14:alpha} is a
 * transparency, where DrawingML's {@code a:alpha} is an opacity (a solid fill with {@code w14:alpha w14:val="40000"}
 * is drawn at 60%), so it is converted to {@code a:alpha} of 100000 less its value.
 *
 * @since 17.3.7
 */
public final class TextEffects {

	private static final Logger log = LoggerFactory.getLogger(TextEffects.class);

	static final String W14 = "http://schemas.microsoft.com/office/word/2010/wordml";

	static final String A = "http://schemas.openxmlformats.org/drawingml/2006/main";

	private TextEffects() {}

	/**
	 * A {@code w14:textFill} as a DrawingML fill: CTNoFillProperties, CTSolidColorFillProperties or
	 * CTGradientFillProperties; null where it has none, or it can't be read
	 */
	public static Object fill(CTFillTextEffect fill) {
		if (fill == null) return null;
		if (fill.getNoFill() != null) return new CTNoFillProperties();
		try {
			if (fill.getSolidFill() != null) {
				return convert(new JAXBElement<org.docx4j.w14.CTSolidColorFillProperties>(new QName(W14, "solidFill"),
						org.docx4j.w14.CTSolidColorFillProperties.class, fill.getSolidFill()), CTSolidColorFillProperties.class);
			}
			if (fill.getGradFill() != null) {
				return convert(new JAXBElement<org.docx4j.w14.CTGradientFillProperties>(new QName(W14, "gradFill"),
						org.docx4j.w14.CTGradientFillProperties.class, fill.getGradFill()), org.docx4j.dml.CTGradientFillProperties.class);
			}
		} catch (Exception e) {
			log.warn("A w14:textFill couldn't be read: " + e.getMessage());
		}
		return null;
	}

	/** A {@code w14:textOutline} as DrawingML's {@code a:ln}; null where it can't be read */
	public static CTLineProperties line(CTTextOutlineEffect outline) {
		if (outline == null) return null;
		try {
			return convert(new JAXBElement<CTTextOutlineEffect>(new QName(W14, "textOutline"), CTTextOutlineEffect.class, outline),
					CTLineProperties.class);
		} catch (Exception e) {
			log.warn("A w14:textOutline couldn't be read: " + e.getMessage());
			return null;
		}
	}

	/** Whether a solid fill's colour is less than opaque */
	public static boolean hasAlpha(CTSolidColorFillProperties fill) {
		Object c = fill.getSrgbClr() != null ? fill.getSrgbClr() : fill.getSchemeClr() != null ? fill.getSchemeClr()
				: fill.getPrstClr() != null ? fill.getPrstClr() : fill.getSysClr() != null ? fill.getSysClr()
				: fill.getScrgbClr() != null ? fill.getScrgbClr() : fill.getHslClr();
		if (c == null) return false;
		try {
			java.lang.reflect.Method m = c.getClass().getMethod("getEGColorTransform");
			for (Object t : (java.util.List<?>) m.invoke(c)) {
				if (t instanceof JAXBElement && "alpha".equals(((JAXBElement<?>) t).getName().getLocalPart())) {
					Object v = ((JAXBElement<?>) t).getValue();
					if (v instanceof org.docx4j.dml.CTPositiveFixedPercentage && ((org.docx4j.dml.CTPositiveFixedPercentage) v).getVal() < 100000) return true;
				}
			}
		} catch (Exception e) {
			return false;
		}
		return false;
	}

	/**
	 * A run's {@code w14:glow}, {@code w14:shadow} and {@code w14:reflection} as a DrawingML {@code a:effectLst}
	 * ({@code a:glow}, {@code a:outerShdw} or {@code a:innerShdw}, {@code a:reflection}), their colours converted as a
	 * fill's ({@code w14:alpha} a transparency), their lengths as written; null where it has none, or none can be read
	 * (CR-083 phase 3).
	 *
	 * <p>A shadow with {@code sx} and {@code sy} 0 and {@code algn="none"} is an inner shadow: the gallery's style 15,
	 * which Word draws over the text (phase 3 findings section 1, D15); an inner shadow keeps its blur, distance and
	 * direction. {@code algn="none"}, which DrawingML has no value for, is read as {@code ctr} on any other shadow (a shadow
	 * with {@code sy} 0 and {@code algn="none"} takes no room, which it would about the default, {@code b}: phase 3
	 * findings section 4, we16-10), and as {@code b}, DrawingML's default, on a reflection, mirrored about the baseline as
	 * its room is given (a reflection with {@code algn="none"} isn't measured; about the middle it would lie on the text).</p>
	 *
	 * @since 17.3.7
	 */
	public static CTEffectList effects(RPr rPr) {
		return rPr == null ? null : effects(rPr.getGlow(), rPr.getShadow14(), rPr.getReflection());
	}

	/** As {@link #effects(RPr)}, from the three elements, any null */
	public static CTEffectList effects(CTGlow glow, CTShadow shadow, CTReflection reflection) {
		if (glow == null && shadow == null && reflection == null) return null;
		CTEffectList list = new CTEffectList();
		boolean any = false;
		try {
			if (glow != null) {
				list.setGlow(convert(new JAXBElement<CTGlow>(new QName(W14, "glow"), CTGlow.class, glow), CTGlowEffect.class, null,
						NO_ATTRIBUTES));
				any = true;
			}
		} catch (Exception e) {
			log.warn("A w14:glow couldn't be read: " + e.getMessage());
		}
		try {
			if (shadow != null) {
				JAXBElement<CTShadow> el = new JAXBElement<CTShadow>(new QName(W14, "shadow"), CTShadow.class, shadow);
				if (isInner(shadow)) {
					list.setInnerShdw(convert(el, CTInnerShadowEffect.class, "innerShdw", INNER_DROPS));
				} else {
					list.setOuterShdw(convert(el, CTOuterShadowEffect.class, "outerShdw", NONE_ALIGNMENT));
				}
				any = true;
			}
		} catch (Exception e) {
			log.warn("A w14:shadow couldn't be read: " + e.getMessage());
		}
		try {
			if (reflection != null) {
				list.setReflection(convert(new JAXBElement<CTReflection>(new QName(W14, "reflection"), CTReflection.class, reflection),
						CTReflectionEffect.class, null, REFLECTION_NONE));
				any = true;
			}
		} catch (Exception e) {
			log.warn("A w14:reflection couldn't be read: " + e.getMessage());
		}
		return any ? list : null;
	}

	/** Word's inner shadow: {@code sx} and {@code sy} 0, {@code algn="none"} (the gallery's style 15) */
	public static boolean isInner(CTShadow s) {
		return s != null && s.getSx() != null && s.getSx() == 0 && s.getSy() != null && s.getSy() == 0
				&& s.getAlgn() == org.docx4j.w14.STRectAlignment.NONE;
	}

	// ------------------------------------------------------------------ Word's scale and room (phase 3)

	/**
	 * The share of its stated lengths at which Word draws a flat run's shadow, glow and reflection blur, and by which it
	 * gives the line room: (size / 72)^0.7, whatever the face (phase 3 findings section 4: a sharp shadow 12 pt long drawn
	 * 3.4, 5.55, 7.4 and 12.0 pt long at 12, 24, 36 and 72 pt; Aptos, Calibri and Times New Roman alike). Measured from
	 * 12 to 72 pt.
	 */
	public static double scale(double size) {
		return size > 0 ? Math.pow(size / 72.0, 0.7) : 0;
	}

	/** A glow takes this share of its drawn radius as room above and below the line (phase 3 findings section 4) */
	public static final double GLOW_ROOM = 0.75;

	/**
	 * A flat run's reflection is moved its {@code dist} times this share of the run's descent (phase 3 findings section 4:
	 * 0.075 of the size for Aptos at 12 to 96 pt, whose descent is 0.282 of it; Calibri's and Times New Roman's moves at
	 * 36 pt within 1% of the same share of their descents)
	 */
	public static final double REFLECTION_MOVE = 0.2664;

	/** How far a flat run's reflection is moved, in points, for its dist in points and the run's descent in points */
	public static double reflectionMove(double dist, double descent) {
		return REFLECTION_MOVE * dist * descent;
	}

	/**
	 * A flat run's reflection's blur is drawn, and takes room, at this share of its stated radius: size / 72, not
	 * {@link #scale(double)} (phase 3 findings section 4: 2 pt of blur takes 0.35, 0.65, 1.0, 2.0 and 2.65 pt at 12, 24,
	 * 36, 72 and 96 pt)
	 */
	public static double reflectionScale(double size) {
		return size > 0 ? size / 72.0 : 0;
	}

	/**
	 * How far a run's effects reach from its baseline, as Word gives its line the room: {above, below} in points, at
	 * least the run's own ascent and descent (phase 3 findings section 4). Word works it out on the run's line box, its
	 * frame (its advance across, its ascent and descent up and down: not its ink), before any warp:
	 * <ul>
	 * <li>an outer shadow: the frame scaled and skewed about its {@code algn} point, moved by its {@code dist}, grown by
	 * its {@code blurRad}, the lengths at {@link #scale(double)} (a shadow scaled to nothing takes its room still, and
	 * draws nothing);</li>
	 * <li>a glow: the frame grown by {@link #GLOW_ROOM} of its radius at the scale;</li>
	 * <li>a reflection: below, its move ({@link #reflectionMove(double, double)}) and endPos of the ascent at its
	 * {@code sy}, and its blur at {@link #reflectionScale(double)}; above, the descent at its {@code sy}, mirrored, less
	 * its move (a reflection moved up past the line);</li>
	 * <li>an inner shadow: none.</li>
	 * </ul>
	 *
	 * @param ascent the run's ascent in points (Word's: {@code WordLineMetrics})
	 * @param descent its descent in points
	 * @param width its advance on the line, in points
	 */
	public static double[] reach(CTEffectList fx, double size, double ascent, double descent, double width) {
		double up = ascent, down = descent;
		if (fx == null) return new double[] { up, down };
		double s = scale(size);
		CTOuterShadowEffect sh = fx.getOuterShdw();
		if (sh != null) {
			// the frame, y down from the baseline, its corners scaled and skewed about the algn point, moved, grown
			double[] al = alignment(sh.getAlgn());
			double px = al[0] * width, py = -ascent + al[1] * (ascent + descent);
			double sx = sh.getSx() / 100000.0, sy = sh.getSy() / 100000.0;
			double tkx = Math.tan(Math.toRadians(sh.getKx() / 60000.0)), tky = Math.tan(Math.toRadians(sh.getKy() / 60000.0));
			double dir = Math.toRadians(sh.getDir() / 60000.0), dist = s * sh.getDist() / 12700.0, blur = s * sh.getBlurRad() / 12700.0;
			double dy = dist * Math.sin(dir);
			double top = Double.MAX_VALUE, bottom = -Double.MAX_VALUE;
			for (double[] c : new double[][] { { 0, -ascent }, { width, -ascent }, { 0, descent }, { width, descent } }) {
				double y = py + tky * (c[0] - px) + sy * (c[1] - py) + dy;
				top = Math.min(top, y);
				bottom = Math.max(bottom, y);
			}
			up = Math.max(up, blur - top);
			down = Math.max(down, bottom + blur);
		}
		CTGlowEffect gl = fx.getGlow();
		if (gl != null && gl.getRad() > 0) {
			double g = GLOW_ROOM * s * gl.getRad() / 12700.0;
			up = Math.max(up, ascent + g);
			down = Math.max(down, descent + g);
		}
		CTReflectionEffect rf = fx.getReflection();
		if (rf != null) {
			double move = reflectionMove(rf.getDist() / 12700.0, descent) * Math.sin(Math.toRadians(rf.getDir() / 60000.0));
			double sy = Math.abs(rf.getSy() / 100000.0), blur = reflectionScale(size) * rf.getBlurRad() / 12700.0;
			down = Math.max(down, move + sy * rf.getEndPos() / 100000.0 * ascent + blur);
			up = Math.max(up, sy * descent - move);
		}
		return new double[] { up, down };
	}

	/** An algn point's place in a box, 0 to 1 across and down (DrawingML's default b) */
	static double[] alignment(org.docx4j.dml.STRectAlignment a) {
		String v = a == null ? "b" : a.value();
		if (v.equals("ctr")) return new double[] { 0.5, 0.5 };
		return new double[] { v.endsWith("l") ? 0 : v.endsWith("r") ? 1 : 0.5, v.startsWith("t") ? 0 : v.startsWith("b") ? 1 : 0.5 };
	}

	/**
	 * A copy of the effects with their lengths as Word draws them: a shadow's and a glow's blurs, distances and radii
	 * times scale, a reflection's blur times reflectionScale and its distance times move. Flat text: {@link #scale(double)},
	 * {@link #reflectionScale(double)} and {@link #REFLECTION_MOVE} times the descent; warped text, which Word draws at
	 * the stated lengths (phase 3 findings section 4: at 12, 36 and 72 pt): 1, 1 and 1.
	 */
	public static CTEffectList scaled(CTEffectList fx, double scale, double reflectionScale, double move) {
		if (fx == null) return null;
		CTEffectList out = (CTEffectList) XmlUtils.deepCopy(fx);
		if (out.getOuterShdw() != null) {
			out.getOuterShdw().setBlurRad(Math.round(out.getOuterShdw().getBlurRad() * scale));
			out.getOuterShdw().setDist(Math.round(out.getOuterShdw().getDist() * scale));
		}
		if (out.getInnerShdw() != null) {
			out.getInnerShdw().setBlurRad(Math.round(out.getInnerShdw().getBlurRad() * scale));
			out.getInnerShdw().setDist(Math.round(out.getInnerShdw().getDist() * scale));
		}
		if (out.getGlow() != null) out.getGlow().setRad(Math.round(out.getGlow().getRad() * scale));
		if (out.getReflection() != null) {
			CTReflectionEffect r = out.getReflection();
			r.setBlurRad(Math.round(r.getBlurRad() * reflectionScale));
			r.setDist(Math.round(r.getDist() * move));
		}
		return out;
	}

	private static final java.util.Set<String> NO_ATTRIBUTES = java.util.Collections.emptySet();

	/** a:innerShdw has a blur, a distance and a direction only */
	private static final java.util.Set<String> INNER_DROPS = new java.util.HashSet<String>(java.util.Arrays.asList("sx", "sy", "kx", "ky", "algn"));

	/** algn="none" read as ctr (name=value:replacement) */
	private static final java.util.Set<String> NONE_ALIGNMENT = java.util.Collections.singleton("algn=none:ctr");

	/** algn="none" on a reflection read as b */
	private static final java.util.Set<String> REFLECTION_NONE = java.util.Collections.singleton("algn=none:b");

	/** The w14 element, marshalled, renamed into DrawingML's namespace and read back as the DrawingML class */
	private static <T> T convert(JAXBElement<?> w14, Class<T> as) throws Exception {
		return convert(w14, as, null, NO_ATTRIBUTES);
	}

	/**
	 * As {@link #convert(JAXBElement, Class)}, its root renamed (null: its own name) and its root's attributes in drops left
	 * out (a name, or name=value for that value alone) or replaced (name=value:replacement)
	 */
	private static <T> T convert(JAXBElement<?> w14, Class<T> as, String root, java.util.Set<String> drops) throws Exception {
		Document source = XmlUtils.marshaltoW3CDomDocument(w14, Context.jc);
		Document target = XmlUtils.getNewDocumentBuilder().newDocument();
		Element top = copy(source.getDocumentElement(), target, root);
		for (String d : drops) {
			int eq = d.indexOf('='), colon = d.indexOf(':');
			String name = eq < 0 ? d : d.substring(0, eq);
			String value = eq < 0 ? null : colon < 0 ? d.substring(eq + 1) : d.substring(eq + 1, colon);
			if (!top.hasAttribute(name) || value != null && !value.equals(top.getAttribute(name))) continue;
			if (colon < 0) top.removeAttribute(name);
			else top.setAttributeNS(null, name, d.substring(colon + 1));
		}
		target.appendChild(top);
		Object o = XmlUtils.unwrap(XmlUtils.unmarshal(target.getDocumentElement(), Context.jc, as));
		return as.cast(o);
	}

	private static Element copy(Element from, Document into) {
		return copy(from, into, null);
	}

	private static Element copy(Element from, Document into, String rename) {
		String ns = W14.equals(from.getNamespaceURI()) ? A : from.getNamespaceURI();
		Element to = into.createElementNS(ns, "a:" + (rename != null ? rename : from.getLocalName()));
		NamedNodeMap attrs = from.getAttributes();
		for (int i = 0; i < attrs.getLength(); i++) {
			Attr a = (Attr) attrs.item(i);
			if ("http://www.w3.org/2000/xmlns/".equals(a.getNamespaceURI())) continue;
			String value = a.getValue();
			// w14:alpha is a transparency; a:alpha an opacity
			if ("alpha".equals(from.getLocalName()) && "val".equals(a.getLocalName())) {
				try {
					value = Integer.toString(Math.max(0, 100000 - Integer.parseInt(value.trim())));
				} catch (NumberFormatException e) {
					// left as written
				}
			}
			to.setAttributeNS(null, a.getLocalName(), value);
		}
		for (Node c = from.getFirstChild(); c != null; c = c.getNextSibling()) {
			if (c instanceof Element) to.appendChild(copy((Element) c, into));
		}
		return to;
	}
}
