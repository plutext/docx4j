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

import org.docx4j.dml.CTLineProperties;
import org.docx4j.dml.CTTextCharacterProperties;
import org.docx4j.dml.STCompoundLine;
import org.docx4j.dml.STPresetLineDashVal;
import org.docx4j.model.color.DrawingMLColors;
import org.docx4j.model.color.ResolvedColor;
import org.docx4j.model.shapes.TextEffects;
import org.docx4j.w14.CTTextOutlineEffect;
import org.docx4j.wml.RPr;
import org.w3c.dom.Element;

/**
 * A run's {@code w14:textOutline} (CR-083 phase 5).
 *
 * <p>In HTML (D23) a solid outline is {@code -webkit-text-stroke} in its width and colour, centred on the glyphs'
 * edge over their fill as Word strokes it (a width of 0 as 0.25 pt, unmeasured); a gradient outline is reported and not
 * drawn, a compound or dashed one drawn as a single solid line and reported. The PDF output draws no outline until the
 * FO renderer fork's text effects (CR-083 phase 5's painter): nothing is written for it.</p>
 *
 * @since 17.3.7
 */
public class TextOutline extends AbstractRunProperty {

	public final static String CSS_NAME = "-webkit-text-stroke";

	/** The width CSS draws an outline of width 0 at, in points (Word's is unmeasured) */
	static final double HAIRLINE = 0.25;

	public TextOutline(CTTextOutlineEffect outline) {
		this.setObject(outline);
	}

	@Override
	public String getCssName() {
		return CSS_NAME;
	}

	@Override
	public String getCssProperty() {
		CTTextOutlineEffect o = (CTTextOutlineEffect) this.getObject();
		CTLineProperties ln = TextEffects.line(o);
		if (ln == null || ln.getNoFill() != null) return CSS_NULL;
		if (ln.getSolidFill() == null) {
			if (ln.getGradFill() != null || ln.getPattFill() != null) {
				TextEffectCss.report("a gradient or pattern text outline, not drawn");
			}
			return CSS_NULL;
		}
		ResolvedColor c = DrawingMLColors.resolve(ln.getSolidFill(), getThemeColors());
		if (c == null) return CSS_NULL;
		if (ln.getCmpd() != null && ln.getCmpd() != STCompoundLine.SNG) {
			TextEffectCss.report("a compound text outline, drawn as a single line");
		}
		if (ln.getPrstDash() != null && ln.getPrstDash().getVal() != null && ln.getPrstDash().getVal() != STPresetLineDashVal.SOLID) {
			TextEffectCss.report("a dashed text outline, drawn solid");
		}
		double w = ln.getW() == null ? 0 : ln.getW() / 12700.0;
		return composeCss(CSS_NAME, TextEffectCss.number(Math.max(w, HAIRLINE), 3) + "pt " + TextEffectCss.css(c));
	}

	@Override
	public void setXslFO(Element foElement) {
		// drawn by the FO renderer fork's text effects (CR-083 phase 5), not by an FO property
	}

	@Override
	public void set(RPr rPr) {
		rPr.setTextOutline((CTTextOutlineEffect) this.getObject());
	}

	@Override
	public void set(CTTextCharacterProperties rPr) {
		// TODO
	}
}
