/*
   Licensed to Plutext Pty Ltd under one or more contributor license agreements.

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
package org.docx4j.convert.out.fo;

import java.util.Map;

import org.docx4j.model.structure.PageDimensions;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

/**
 * The fo:block-container a text box's content is rendered in, and the geometry
 * hints WordLayoutFixups needs to place it where Word does.
 *
 * <p>A Word text box - VML (w:pict/v:shape/v:textbox) or DrawingML
 * (w:drawing/wp:anchor/../wps:wsp/wps:txbx) - is a positioned object: it carries
 * an explicit position relative to the paragraph, the column, the margin or the
 * page.  Until 17.0.5 the VML case was emitted as an fo:block-container with
 * position="absolute" (not an XSL-FO property; the FO 1.1 name is
 * absolute-position) and a top but no left, width or height, nested inside an
 * fo:inline - which is not block-level content - so FOP painted nothing at all
 * and letterheads built from text boxes were lost.  The DrawingML case never
 * reached the FO.</p>
 *
 * <p>Both now travel as hints on the container, which WordLayoutFixups turns
 * into the same absolutely positioned or space-reserving block-container it
 * builds for an anchored picture.  Unlike a picture, a wrapped text box is not
 * given to fo:float: a float discards the box's position (and FOP's side floats
 * are unreliable - see docx4j.convert.out.fo.pictures.float), so a wrapped box
 * reserves its height at the paragraph instead.  Text therefore does not flow
 * beside a text box.</p>
 *
 * @since 17.0.5
 */
public class FOTextBoxes {

	protected static Logger log = LoggerFactory.getLogger(FOTextBoxes.class);

	private static final String XSL_FO = "http://www.w3.org/1999/XSL/Format";

	/** Word's default text box insets (v:textbox/@inset), in points: left, top, right, bottom. */
	private static final double[] DEFAULT_INSET = { 7.2, 3.6, 7.2, 3.6 };

	private FOTextBoxes() {}

	/** True where WordLayoutFixups will be run, so hints on the FO are removed again. */
	public static boolean isEnabled() {
		return WordLayoutFixups.isEnabled();
	}

	/**
	 * The container a text box's converted content goes in.  It carries the hints
	 * WordLayoutFixups reads (and removes).
	 *
	 * @param kind "none" (behind or in front of the text), "square" (any wrapping
	 *             style: the box reserves its height) or "inline"
	 */
	public static Element createContainer(Document doc, String kind, double w, double h,
			double x, String y, double col, double ml, double[] inset) {

		Element container = doc.createElementNS(XSL_FO, "fo:block-container");
		double[] in = (inset==null) ? DEFAULT_INSET : inset;
		// Word's width and height are the box; the text inside is inset from them, so
		// the container's content box is that much smaller and its border box is Word's
		if (w > in[0] + in[2]) container.setAttribute("width", pt(w - in[0] - in[2]));
		if (h > in[1] + in[3]) container.setAttribute("height", pt(h - in[1] - in[3]));
		container.setAttribute("overflow", "visible");
		if (in[0] > 0) container.setAttribute("padding-left", pt(in[0]));
		if (in[1] > 0) container.setAttribute("padding-top", pt(in[1]));
		if (in[2] > 0) container.setAttribute("padding-right", pt(in[2]));
		if (in[3] > 0) container.setAttribute("padding-bottom", pt(in[3]));
		// indents are inherited and measured from the reference area, which the
		// container is: without this the content would be indented all over again
		container.setAttribute("start-indent", "0pt");
		container.setAttribute("end-indent", "0pt");
		if (!"inline".equals(kind)) {
			container.setAttribute(WordLayoutFixups.HINT_ANCHOR, kind);
			container.setAttribute(WordLayoutFixups.HINT_ANCHOR_W, pt(w));
			container.setAttribute(WordLayoutFixups.HINT_ANCHOR_H, pt(h));
			container.setAttribute(WordLayoutFixups.HINT_ANCHOR_X, pt(x));
			container.setAttribute(WordLayoutFixups.HINT_ANCHOR_Y, y);
			container.setAttribute(WordLayoutFixups.HINT_ANCHOR_COL, pt(col));
			container.setAttribute(WordLayoutFixups.HINT_ANCHOR_ML, pt(ml));
		}
		return container;
	}

	/** No inset: a VML picture's box is the picture. */
	private static final double[] NO_INSET = { 0, 0, 0, 0 };

	/**
	 * A v:shape's @style as a property map ("position:absolute;margin-left:-2.85pt;...").
	 * A property with no value, or with more colons than one, is skipped rather than
	 * throwing.
	 *
	 * @since 17.0.6
	 */
	public static Map<String, String> parseStyle(String style) {
		Map<String, String> map = new java.util.HashMap<String, String>();
		if (style==null) return map;
		for (String entry : style.split(";")) {
			int colon = entry.indexOf(':');
			if (colon <= 0 || colon == entry.length()-1) continue;
			map.put(entry.substring(0, colon).trim(), entry.substring(colon+1).trim());
		}
		return map;
	}

	/**
	 * Whether a v:shape's @style says Word positions it rather than laying it out in
	 * the line ("position:absolute", which is what Word writes for every floating
	 * picture and text box).
	 *
	 * @since 17.0.6
	 */
	public static boolean isPositioned(Map<String, String> props) {
		return "absolute".equals(props.get("position"))
				&& !"char".equals(props.get("mso-position-horizontal-relative"));
	}

	/**
	 * The container an absolutely positioned VML <em>picture</em>
	 * (w:pict/v:shape/v:imagedata) goes in, so that it is placed where Word places it
	 * instead of taking a line in the flow.  Same geometry as a VML text box, with no
	 * inset: the shape's box is the picture.
	 *
	 * @since 17.0.6
	 */
	public static Element createVmlPictureContainer(Document doc, Map<String, String> props,
			String wrapType, PageDimensions pd) {
		return createVmlContainer(doc, props, wrapType, NO_INSET, pd);
	}

	/**
	 * The container for a VML text box, from the v:shape's @style properties
	 * (the same properties Word writes for an anchored picture, in CSS form).
	 */
	public static Element createVmlContainer(Document doc, Map<String, String> props,
			String wrapType, double[] inset, PageDimensions pd) {

		double w = pts(props.get("width"), 0);
		double h = pts(props.get("height"), 0);
		double marginLeft = pts(props.get("margin-left"), 0);
		double marginTop = pts(props.get("margin-top"), 0);

		double colW = pd.getWritableWidthTwips() / 20d;
		double pageW = pd.getPgSz().getW().doubleValue() / 20d;
		double pageH = pd.getPgSz().getH().doubleValue() / 20d;
		double mL = pd.getPgMar().getLeft().doubleValue() / 20d;
		double mR = pd.getPgMar().getRight().doubleValue() / 20d;
		double mT = pd.getPgMar().getTop().doubleValue() / 20d;
		double mB = pd.getPgMar().getBottom().doubleValue() / 20d;

		// horizontal: the reference box in column coordinates, then align or offset
		String relH = props.get("mso-position-horizontal-relative");
		double refLeft = 0, refRight = colW;
		if ("page".equals(relH)) {
			refLeft = -mL; refRight = pageW - mL;
		} else if ("left-margin-area".equals(relH) || "inner-margin-area".equals(relH)) {
			refLeft = -mL; refRight = 0;
		} else if ("right-margin-area".equals(relH) || "outer-margin-area".equals(relH)) {
			refLeft = colW; refRight = colW + mR;
		} // text, column, margin: the column
		String posH = props.get("mso-position-horizontal");
		double x;
		if ("center".equals(posH)) {
			x = (refLeft + refRight - w) / 2;
		} else if ("right".equals(posH) || "outside".equals(posH)) {
			x = refRight - w;
		} else if ("left".equals(posH) || "inside".equals(posH)) {
			x = refLeft;
		} else {
			x = refLeft + marginLeft; // absolute, or none given
		}

		// vertical: from the paragraph's top, or from the page's top
		String relV = props.get("mso-position-vertical-relative");
		String posV = props.get("mso-position-vertical");
		String y;
		if (relV==null || "text".equals(relV) || "line".equals(relV)) {
			y = "p:" + fmt(marginTop);
		} else {
			double refTop = 0, refBottom = pageH;
			if ("margin".equals(relV)) {
				refTop = mT; refBottom = pageH - mB;
			} else if ("top-margin-area".equals(relV) || "inner-margin-area".equals(relV)) {
				refTop = 0; refBottom = mT;
			} else if ("bottom-margin-area".equals(relV) || "outer-margin-area".equals(relV)) {
				refTop = pageH - mB; refBottom = pageH;
			}
			double py;
			if ("center".equals(posV)) {
				py = (refTop + refBottom - h) / 2;
			} else if ("bottom".equals(posV) || "outside".equals(posV)) {
				py = refBottom - h;
			} else if ("top".equals(posV) || "inside".equals(posV)) {
				py = refTop;
			} else {
				py = refTop + marginTop; // absolute, or none given
			}
			y = "page:" + fmt(py);
		}

		return createContainer(doc, kind(wrapType), w, h, x, y, colW, mL, inset);
	}

	/** Word's wrapping styles, as the fixups know them. */
	private static String kind(String wrapType) {
		return ("none".equals(wrapType) || wrapType==null) ? "none" : "square";
	}

	/** The v:textbox/@inset ("0,0,0,0", "1mm,2pt,,") in points, or null for Word's default. */
	public static double[] inset(String inset) {
		if (inset==null || inset.trim().length()==0) return null;
		String[] parts = inset.split(",", -1);
		double[] result = new double[] { DEFAULT_INSET[0], DEFAULT_INSET[1], DEFAULT_INSET[2], DEFAULT_INSET[3] };
		for (int i = 0; i < 4 && i < parts.length; i++) {
			String v = parts[i].trim();
			if (v.length() > 0) result[i] = measure(v, result[i]);
		}
		return result;
	}

	/** A VML measurement (pt, in, mm, cm, pc, px or bare points) in points. */
	private static double measure(String v, double fallback) {
		try {
			if (v.endsWith("pt")) return Double.parseDouble(v.substring(0, v.length()-2));
			if (v.endsWith("in")) return Double.parseDouble(v.substring(0, v.length()-2)) * 72;
			if (v.endsWith("mm")) return Double.parseDouble(v.substring(0, v.length()-2)) * 72 / 25.4;
			if (v.endsWith("cm")) return Double.parseDouble(v.substring(0, v.length()-2)) * 720 / 25.4;
			if (v.endsWith("pc")) return Double.parseDouble(v.substring(0, v.length()-2)) * 12;
			if (v.endsWith("px")) return Double.parseDouble(v.substring(0, v.length()-2)) * 0.75;
			return Double.parseDouble(v);
		} catch (NumberFormatException e) {
			log.debug("Not a length: " + v);
			return fallback;
		}
	}

	static double pts(String v, double fallback) {
		return (v==null) ? fallback : measure(v.trim(), fallback);
	}

	static String pt(double v) {
		return fmt(v) + "pt";
	}

	static String fmt(double v) {
		String s = String.format(java.util.Locale.ROOT, "%.2f", v);
		if (s.endsWith("0")) s = s.substring(0, s.length()-1);
		if (s.endsWith("0")) s = s.substring(0, s.length()-1);
		if (s.endsWith(".")) s = s.substring(0, s.length()-1);
		if (s.equals("-0")) s = "0";
		return s;
	}
}
