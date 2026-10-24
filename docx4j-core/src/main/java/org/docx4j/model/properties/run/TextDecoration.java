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

import org.docx4j.dml.CTTextCharacterProperties;
import org.docx4j.dml.STTextStrikeType;
import org.docx4j.dml.STTextUnderlineType;
import org.docx4j.model.color.ResolvedColor;
import org.docx4j.model.color.WordColors;
import org.docx4j.wml.BooleanDefaultTrue;
import org.docx4j.wml.RPr;
import org.docx4j.wml.U;
import org.docx4j.wml.UnderlineEnumeration;
import org.w3c.dom.Element;

/**
 * A run's underline, strike and double strike drawn together (CR-062): one set of CSS {@code text-decoration-*}
 * declarations, and one FO {@code text-decoration}, where each property used to write {@code text-decoration} over
 * the others, so a run both underlined and struck through lost its strike, and a double strike wasn't drawn.
 *
 * <p>CSS: {@code text-decoration-line} (underline, line-through, or none where a strike is stated off and nothing
 * is drawn); {@code -style} from the underline's style, the nearest CSS has (D1), or double for a double strike with
 * no underline; {@code -thickness} for the heavy styles; {@code -color} from the underline's colour, its theme
 * colour where it has one. CSS draws every line of an element in one style and colour, so with an underline and a
 * strike together, the underline's are used (D2). FO: {@code text-decoration} takes underline and line-through
 * together; XSL-FO has no decoration style or colour, so PDF draws a single line in the text's colour (D3).</p>
 *
 * @since 17.3.5
 */
public class TextDecoration extends AbstractRunProperty {

	public final static String CSS_NAME = "text-decoration";
	public final static String FO_NAME = "text-decoration";

	private final U u;
	private final BooleanDefaultTrue strike, dstrike;

	public TextDecoration(U u, BooleanDefaultTrue strike, BooleanDefaultTrue dstrike) {
		this.u = u;
		this.strike = strike;
		this.dstrike = dstrike;
		this.setObject(u);
	}

	/** Whether this run's properties say anything about decoration */
	public static boolean stated(U u, BooleanDefaultTrue strike, BooleanDefaultTrue dstrike) {
		return u != null || strike != null || dstrike != null;
	}

	@Override
	public String getCssName() {
		return CSS_NAME;
	}

	private boolean underlined() {
		return u != null && u.getVal() != null && u.getVal() != UnderlineEnumeration.NONE;
	}

	private boolean struck() {
		return (strike != null && strike.isVal()) || doubleStruck();
	}

	private boolean doubleStruck() {
		return dstrike != null && dstrike.isVal();
	}

	/** A strike stated off, which undoes one beneath (a toggled style's, or a class's) */
	private boolean strikeStatedOff() {
		return (strike != null && !strike.isVal()) || (dstrike != null && !dstrike.isVal());
	}

	private List<String> lines() {
		List<String> lines = new ArrayList<String>();
		if (underlined()) lines.add("underline");
		if (struck()) lines.add("line-through");
		return lines;
	}

	/** The CSS style of an underline (D1): the nearest CSS has; null for solid */
	static String cssStyle(UnderlineEnumeration val) {
		switch (val) {
		case DOUBLE: return "double";
		case DOTTED: case DOTTED_HEAVY: return "dotted";
		case DASH: case DASHED_HEAVY: case DASH_LONG: case DASH_LONG_HEAVY:
		case DOT_DASH: case DASH_DOT_HEAVY: case DOT_DOT_DASH: case DASH_DOT_DOT_HEAVY: return "dashed";
		case WAVE: case WAVY_HEAVY: case WAVY_DOUBLE: return "wavy";
		default: return null; // single, words (CSS can't leave the spaces out), thick
		}
	}

	static boolean heavy(UnderlineEnumeration val) {
		switch (val) {
		case THICK: case DOTTED_HEAVY: case DASHED_HEAVY: case DASH_LONG_HEAVY:
		case DASH_DOT_HEAVY: case DASH_DOT_DOT_HEAVY: case WAVY_HEAVY: return true;
		default: return false;
		}
	}

	/** The underline's colour as Word draws it, "#RRGGBB"; null for automatic (the text's) */
	private String underlineColour() {
		ResolvedColor c = u == null ? null : WordColors.of(u, getThemeColors());
		return c == null ? null : "#" + c.toHex();
	}

	@Override
	public String getCssProperty() {
		List<String> lines = lines();
		if (lines.isEmpty()) {
			StringBuilder css = new StringBuilder();
			if (strikeStatedOff()) css.append(composeCss("text-decoration-line", "none"));
			// a w:u stating only a colour colours the underline a character style draws (Word: CR-060 round 6)
			if (u != null && u.getVal() == null && underlineColour() != null) {
				css.append(composeCss("text-decoration-color", underlineColour()));
			}
			return css.length() == 0 ? CSS_NULL : css.toString();
		}
		StringBuilder css = new StringBuilder(composeCss("text-decoration-line", String.join(" ", lines)));
		if (underlined()) {
			String style = cssStyle(u.getVal());
			if (style != null) css.append(composeCss("text-decoration-style", style));
			if (heavy(u.getVal())) css.append(composeCss("text-decoration-thickness", "0.15em"));
			if (underlineColour() != null) css.append(composeCss("text-decoration-color", underlineColour()));
		} else if (doubleStruck()) {
			css.append(composeCss("text-decoration-style", "double"));
		}
		return css.toString();
	}

	@Override
	public void setXslFO(Element foElement) {
		List<String> lines = lines();
		if (!lines.isEmpty()) {
			foElement.setAttribute(FO_NAME, String.join(" ", lines));
		} else if (strikeStatedOff()) {
			foElement.setAttribute(FO_NAME, "none");
		}
	}

	@Override
	public void set(RPr rPr) {
		rPr.setU(u);
		rPr.setStrike(strike);
		rPr.setDstrike(dstrike);
	}

	@Override
	public void set(CTTextCharacterProperties rPr) {
		if (underlined()) rPr.setU(STTextUnderlineType.SNG);
		if (doubleStruck()) rPr.setStrike(STTextStrikeType.DBL_STRIKE);
		else if (struck()) rPr.setStrike(STTextStrikeType.SNG_STRIKE);
	}
}
