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
package org.docx4j.model.properties.run;

import java.util.ArrayList;
import java.util.List;

import org.docx4j.dml.CTGradientFillProperties;
import org.docx4j.dml.CTGradientStop;
import org.docx4j.dml.CTSolidColorFillProperties;
import org.docx4j.dml.CTTextCharacterProperties;
import org.docx4j.model.color.DrawingMLColors;
import org.docx4j.model.color.ResolvedColor;
import org.docx4j.model.shapes.GradientCurve;
import org.docx4j.model.shapes.TextEffects;
import org.docx4j.w14.CTFillTextEffect;
import org.docx4j.wml.RPr;
import org.w3c.dom.Element;

/**
 * A run's {@code w14:textFill} (CR-083 phase 5).
 *
 * <p>Word draws a run with a solid {@code w14:textFill} in the fill's colour, whatever its {@code w:color} (phase 0,
 * wb50-01), and one whose fill has a {@code w14:alpha} at that alpha (wb50-02: {@code w14:alpha} is a transparency,
 * 40000 drawn at 0.6). So the PDF output writes the fill's colour as the run's {@code color}, after
 * {@link FontColor}'s, which it replaces; with an alpha as "#RRGGBBAA", which FOP draws at that alpha (a fill with
 * no opacity left as 01: FOP draws 00 opaque). Word draws a transparent run as glyph paths over invisible text, so
 * the text stays real here where Word's isn't (a known departure, CR-083 D20). A gradient fill and no fill wait for the
 * FO renderer fork's text effects (CR-083 phase 5's painter): the PDF output keeps {@code w:color} for them, as it
 * did.</p>
 *
 * <p>In HTML (D23): a solid fill as {@code color}, with its alpha as rgba(); a gradient as the run's background
 * clipped to its text, a linear gradient at Word's angle (DrawingML's 0 degrees, left to right, is CSS's 90), a path
 * gradient as a radial one about its focus, a gradient of two stops at 0 and 100% by Word's curve
 * ({@link GradientCurve}, D24), any other evenly between its stops as Word blends it; no fill as transparent text
 * (with an outline, {@link TextOutline}, hollow; without, invisible, as Word draws it, wb50-04).</p>
 *
 * @since 17.3.7
 */
public class TextFill extends AbstractRunProperty {

	public final static String CSS_NAME = "color";
	public final static String FO_NAME = "color";

	/** For a gradient or no fill: the glyphs' fill, leaving {@code color} (an underline's) as it is */
	public final static String CSS_TEXT_FILL = "-webkit-text-fill-color";

	public TextFill(CTFillTextEffect fill) {
		this.setObject(fill);
	}

	private CTFillTextEffect fill() {
		return (CTFillTextEffect) this.getObject();
	}

	@Override
	public String getCssName() {
		CTFillTextEffect f = fill();
		if (f == null) return null;
		if (f.getSolidFill() != null) return CSS_NAME;
		if (f.getGradFill() != null || f.getNoFill() != null) return CSS_TEXT_FILL;
		return null;
	}

	/** The solid fill's colour, resolved through the theme; null where it has none or it doesn't resolve */
	public ResolvedColor solidColour() {
		CTFillTextEffect f = fill();
		if (f == null || f.getSolidFill() == null) return null;
		Object dml = TextEffects.fill(f);
		return dml instanceof CTSolidColorFillProperties ? DrawingMLColors.resolve(dml, getThemeColors()) : null;
	}

	@Override
	public String getCssProperty() {
		CTFillTextEffect f = fill();
		if (f == null) return CSS_NULL;
		if (f.getSolidFill() != null) {
			ResolvedColor c = solidColour();
			return c == null ? CSS_NULL : composeCss(CSS_NAME, TextEffectCss.css(c));
		}
		if (f.getNoFill() != null) {
			return composeCss(CSS_TEXT_FILL, "transparent");
		}
		if (f.getGradFill() != null) {
			Object dml = TextEffects.fill(f);
			String image = dml instanceof CTGradientFillProperties ? gradient((CTGradientFillProperties) dml) : null;
			if (image == null) return CSS_NULL;
			return composeCss(CSS_TEXT_FILL, "transparent") + composeCss("background-image", image)
					+ composeCss("-webkit-background-clip", "text") + composeCss("background-clip", "text");
		}
		return CSS_NULL;
	}

	/** The gradient as a CSS image, or null where its stops don't resolve */
	String gradient(CTGradientFillProperties g) {
		if (g.getGsLst() == null || g.getGsLst().getGs().isEmpty()) return null;
		List<CTGradientStop> gs = new ArrayList<CTGradientStop>(g.getGsLst().getGs());
		gs.sort((a, b) -> Integer.compare(a.getPos(), b.getPos()));
		List<String> stops = new ArrayList<String>();
		if (GradientCurve.applies(g)) {
			ResolvedColor a = stopColour(gs.get(0)), b = stopColour(gs.get(1));
			if (a == null || b == null) return null;
			int[] argb = GradientCurve.argb(argb(a), argb(b));
			for (int k = 0; k < argb.length; k++) {
				stops.add(css(argb[k]) + " " + TextEffectCss.number(100.0 * k / GradientCurve.STEPS, 2) + "%");
			}
		} else {
			for (CTGradientStop s : gs) {
				ResolvedColor c = stopColour(s);
				if (c == null) return null;
				stops.add(TextEffectCss.css(c) + " " + TextEffectCss.number(s.getPos() / 1000.0, 3) + "%");
			}
			if (stops.size() == 1) stops.add(stops.get(0));
		}
		String list = String.join(", ", stops);
		if (g.getPath() != null) {
			org.docx4j.dml.CTRelativeRect r = g.getPath().getFillToRect();
			double x = r == null ? 50 : (r.getL() + (100000 - r.getL() - r.getR()) / 2.0) / 1000.0;
			double y = r == null ? 50 : (r.getT() + (100000 - r.getT() - r.getB()) / 2.0) / 1000.0;
			String shape = g.getPath().getPath() != null && "circle".equals(g.getPath().getPath().value()) ? "circle" : "ellipse";
			if (!"circle".equals(shape)) TextEffectCss.report("a path gradient other than a circle, as a radial gradient");
			return "radial-gradient(" + shape + " farthest-corner at " + TextEffectCss.number(x, 3) + "% "
					+ TextEffectCss.number(y, 3) + "%, " + list + ")";
		}
		double ang = g.getLin() != null && g.getLin().getAng() != null ? g.getLin().getAng() / 60000.0 : 0;
		return "linear-gradient(" + TextEffectCss.number(ang + 90, 3) + "deg, " + list + ")";
	}

	private ResolvedColor stopColour(CTGradientStop s) {
		Object c = s.getSchemeClr() != null ? s.getSchemeClr() : s.getSrgbClr() != null ? s.getSrgbClr()
				: s.getPrstClr() != null ? s.getPrstClr() : s.getSysClr() != null ? s.getSysClr()
						: s.getHslClr() != null ? s.getHslClr() : s.getScrgbClr();
		return DrawingMLColors.resolve(c, getThemeColors());
	}

	private static int argb(ResolvedColor c) {
		int a = (int) Math.round(255.0 * c.getAlpha() / ResolvedColor.OPAQUE);
		return (a << 24) | (c.getRed() << 16) | (c.getGreen() << 8) | c.getBlue();
	}

	private static String css(int argb) {
		int a = (argb >>> 24) & 255;
		if (a == 255) return String.format("#%06X", argb & 0xFFFFFF);
		return "rgba(" + ((argb >> 16) & 255) + ", " + ((argb >> 8) & 255) + ", " + (argb & 255) + ", "
				+ TextEffectCss.number(a / 255.0, 3) + ")";
	}

	@Override
	public void setXslFO(Element foElement) {
		ResolvedColor c = solidColour();
		if (c != null) foElement.setAttribute(FO_NAME, TextEffectCss.fo(c));
	}

	@Override
	public void set(RPr rPr) {
		rPr.setTextFill(fill());
	}

	@Override
	public void set(CTTextCharacterProperties rPr) {
		// TODO
	}
}
