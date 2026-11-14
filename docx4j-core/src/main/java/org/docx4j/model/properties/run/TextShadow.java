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

import org.docx4j.dml.CTEffectList;
import org.docx4j.dml.CTGlowEffect;
import org.docx4j.dml.CTOuterShadowEffect;
import org.docx4j.dml.CTTextCharacterProperties;
import org.docx4j.model.color.DrawingMLColors;
import org.docx4j.model.color.ResolvedColor;
import org.docx4j.model.shapes.TextEffects;
import org.docx4j.wml.RPr;
import org.w3c.dom.Element;

/**
 * A run's {@code w14:shadow} and {@code w14:glow}, and the report of its {@code w14:reflection} (CR-083 phase 5).
 *
 * <p>In HTML (D23) as {@code text-shadow}, at the lengths Word draws a flat run's effects: its stated lengths at
 * {@link TextEffects#scale(double)} of the run's size (phase 3 findings section 4). A shadow is its offset (its
 * {@code dist} along {@code dir}) and its blur (Word's is a Gaussian of sigma {@code blurRad} / 3, CSS's of half its
 * radius: a radius of 2/3 {@code blurRad}); a glow is a text-shadow with no offset whose radius is the glow's (CSS has
 * no spread, where Word grows the glyphs by 0.44 of the radius before a blur of sigma radius / 6: the halo reaches as
 * far, fainter at the glyphs' edge). Where the run states no size, the lengths are written in em at Word's scale for
 * 11 pt, so a run of another size scales them evenly where Word's scale isn't even. A shadow scaled or skewed is drawn
 * as its plain offset, an inner shadow and a reflection aren't drawn: each reported. No room is given (D23). The PDF
 * output draws none of them until the FO renderer fork's text effects (CR-083 phase 5's painter).</p>
 *
 * @since 17.3.7
 */
public class TextShadow extends AbstractRunProperty {

	public final static String CSS_NAME = "text-shadow";

	/** Word's default size, at which the lengths are scaled where the run states none */
	static final double DEFAULT_SIZE = 11;

	/** @param rPr the run's properties: its glow, shadow and reflection, and its size */
	public TextShadow(RPr rPr) {
		this.setObject(rPr);
	}

	@Override
	public String getCssName() {
		return CSS_NAME;
	}

	@Override
	public String getCssProperty() {
		RPr rPr = (RPr) this.getObject();
		CTEffectList fx = TextEffects.effects(rPr);
		if (fx == null) return CSS_NULL;
		// "no reflection" and "no shadow" as numbering levels write them (every length and opacity 0) aren't reported
		if (rPr.getReflection() != null && !org.docx4j.model.shapes.WordArtText.drawsNothing(rPr.getReflection())) {
			TextEffectCss.report("a reflection on body text, not drawn");
		}
		if (rPr.getShadow14() != null && fx.getInnerShdw() != null && !org.docx4j.model.shapes.WordArtText.drawsNothing(rPr.getShadow14())) {
			TextEffectCss.report("an inner shadow on body text, not drawn");
		}
		boolean known = rPr.getSz() != null && rPr.getSz().getVal() != null && rPr.getSz().getVal().signum() > 0;
		double size = known ? rPr.getSz().getVal().doubleValue() / 2 : DEFAULT_SIZE;
		double s = TextEffects.scale(size);
		List<String> shadows = new ArrayList<String>();
		CTOuterShadowEffect sh = fx.getOuterShdw();
		if (sh != null) {
			ResolvedColor c = DrawingMLColors.resolve(first(sh.getSrgbClr(), sh.getSchemeClr(), sh.getScrgbClr(),
					sh.getHslClr(), sh.getSysClr(), sh.getPrstClr()), getThemeColors());
			if (c != null) {
				if (sh.getSx() != 100000 || sh.getSy() != 100000 || sh.getKx() != 0 || sh.getKy() != 0) {
					TextEffectCss.report("a shadow scaled or skewed, drawn as its plain offset");
				}
				double dist = sh.getDist() / 12700.0 * s, dir = Math.toRadians(sh.getDir() / 60000.0);
				double blur = sh.getBlurRad() / 12700.0 * s * 2 / 3;
				shadows.add(length(dist * Math.cos(dir), known, size) + " " + length(dist * Math.sin(dir), known, size) + " "
						+ length(blur, known, size) + " " + TextEffectCss.css(c));
			}
		}
		CTGlowEffect gl = fx.getGlow();
		if (gl != null && gl.getRad() > 0) {
			ResolvedColor c = DrawingMLColors.resolve(first(gl.getSrgbClr(), gl.getSchemeClr(), gl.getScrgbClr(),
					gl.getHslClr(), gl.getSysClr(), gl.getPrstClr()), getThemeColors());
			if (c != null) {
				shadows.add("0 0 " + length(gl.getRad() / 12700.0 * s, known, size) + " " + TextEffectCss.css(c));
			}
		}
		return shadows.isEmpty() ? CSS_NULL : composeCss(CSS_NAME, String.join(", ", shadows));
	}

	/** A length in points, as pt where the run's size is known, else in em of the size it was scaled at */
	private static String length(double pt, boolean known, double size) {
		if (Math.abs(pt) < 0.0005) return "0";
		return known ? TextEffectCss.number(pt, 3) + "pt" : TextEffectCss.number(pt / size, 4) + "em";
	}

	private static Object first(Object... os) {
		for (Object o : os) {
			if (o != null) return o;
		}
		return null;
	}

	@Override
	public void setXslFO(Element foElement) {
		// drawn by the FO renderer fork's text effects (CR-083 phase 5), not by an FO property
	}

	@Override
	public void set(RPr rPr) {
		RPr from = (RPr) this.getObject();
		rPr.setGlow(from.getGlow());
		rPr.setShadow14(from.getShadow14());
		rPr.setReflection(from.getReflection());
	}

	@Override
	public void set(CTTextCharacterProperties rPr) {
		// TODO
	}
}
