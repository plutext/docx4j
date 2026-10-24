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

import org.docx4j.wml.CTBorder;
import org.docx4j.wml.CTShd;
import org.docx4j.wml.Color;
import org.docx4j.wml.STThemeColor;
import org.docx4j.wml.U;
import org.docx4j.wml.WordColorValues;

/**
 * WordprocessingML colours ({@code w:color}, {@code w:shd}, the borders, {@code w:u}) resolved as
 * Word draws them (CR-034 §11, P1).
 *
 * <ul>
 * <li>A theme reference ({@code w:themeColor}, {@code w:themeFill}) wins over the value written
 *     beside it ({@code w:val}, {@code w:color}, {@code w:fill}), which is a cache Word keeps: Word
 *     draws the theme colour even where the value disagrees (an older theme's, or anything else).</li>
 * <li>{@code w:themeTint} and {@code w:themeShade} change the HSL luminance: tint t,
 *     L' = L·t + (1 − t); shade t, L' = L·t, t being the byte over 255 (MS-OI29500 2.1.72).  Where
 *     both are present Word applies the tint (the same note).</li>
 * <li>Word's own rounding of that arithmetic is not reproduced exactly (within one unit per channel
 *     in all 7,700 tints and shades of four corpora), so where the written value is within one
 *     unit of the computed one - a current cache - Word's value is returned: exact for every
 *     attribute Word itself wrote against the document's theme.</li>
 * </ul>
 *
 * @since 17.3.3 (CR-034)
 */
public final class WordColors {

	private WordColors() {
	}

	/**
	 * The colour Word draws for a WordprocessingML colour's attributes.
	 *
	 * @param val the written value: RRGGBB, "auto", or null
	 * @param themeColor the theme reference, or null (or "none")
	 * @param themeTint a byte in hex ("99"), or null
	 * @param themeShade a byte in hex ("BF"), or null
	 * @return null for automatic (no theme reference and val "auto" or absent): the caller's default
	 */
	public static ResolvedColor resolve(String val, String themeColor, String themeTint, String themeShade,
			ThemeColors theme) {
		ResolvedColor written = ResolvedColor.ofHex(val);
		if (themeColor == null || "none".equals(themeColor) || theme == null) return written;
		// Word's value where it is a current cache: Word's own rounding, which the computation
		// below matches for Word 365 but not for every older version that wrote the file
		ResolvedColor base = theme.wordThemeColor(themeColor);
		if (base == null) return written;
		Integer tint = hexByte(themeTint), shade = hexByte(themeShade);
		ResolvedColor computed = tint != null ? luminance(base, tint / 255.0, true)
				: shade != null ? luminance(base, shade / 255.0, false) : base;
		return written != null && written.within(computed, 1) ? written : computed;
	}

	public static ResolvedColor resolve(String val, STThemeColor themeColor, String themeTint, String themeShade,
			ThemeColors theme) {
		return resolve(val, themeColor == null ? null : themeColor.value(), themeTint, themeShade, theme);
	}

	/**
	 * A colour value ({@code w:color/@w:val}, {@code w:u/@w:color}, {@code w:shd/@w:fill}, a border's
	 * {@code w:color}) as Word 365 reads it (CR-060 section 1, rule 3, and sections 12 and 13): surrounding
	 * whitespace trimmed, six hex digits in either case with an optional leading '#'; else a classic HTML colour
	 * name, in any case.
	 *
	 * @return six upper-case hex digits; null for {@code auto} and for a value Word rejects, which Word reads as
	 * automatic in a fill or a border colour, and as absent in a text or underline colour ({@link #stated})
	 * @since 17.3.5
	 */
	public static String normalise(String val) {
		return WordColorValues.normalise(val);
	}

	/**
	 * Whether a {@code w:color/@w:val} or {@code w:u/@w:color} value states a colour, as Word reads it (CR-060
	 * sections 12 and 13): one {@link #normalise} accepts, or exactly {@code auto} (automatic, which overrides a
	 * style's colour). Word reads any other value ({@code F00}, {@code bogus}, {@code ""}, {@code Auto},
	 * {@code " auto"}) as absent: a style's colour beneath still shows. (In a fill or a border colour it reads such
	 * a value as automatic instead.)
	 *
	 * @since 17.3.5
	 */
	public static boolean stated(String val) {
		return WordColorValues.stated(val);
	}

	/** A run's colour ({@code w:color}); null for automatic */
	public static ResolvedColor of(Color c, ThemeColors theme) {
		return c == null ? null : resolve(normalise(c.getVal()), c.getThemeColor(), c.getThemeTint(), c.getThemeShade(), theme);
	}

	/** A shading's fill ({@code w:fill}, {@code w:themeFill}); null for automatic */
	public static ResolvedColor fill(CTShd shd, ThemeColors theme) {
		return shd == null ? null
				: resolve(shd.getFill(), shd.getThemeFill(), shd.getThemeFillTint(), shd.getThemeFillShade(), theme);
	}

	/** A shading's pattern colour ({@code w:color}, {@code w:themeColor}); null for automatic */
	public static ResolvedColor pattern(CTShd shd, ThemeColors theme) {
		return shd == null ? null
				: resolve(shd.getColor(), shd.getThemeColor(), shd.getThemeTint(), shd.getThemeShade(), theme);
	}

	/** A border's colour; null for automatic */
	public static ResolvedColor of(CTBorder border, ThemeColors theme) {
		return border == null ? null
				: resolve(border.getColor(), border.getThemeColor(), border.getThemeTint(), border.getThemeShade(), theme);
	}

	/** An underline's colour; null for automatic (the text's colour) */
	public static ResolvedColor of(U u, ThemeColors theme) {
		return u == null ? null : resolve(normalise(u.getColor()), u.getThemeColor(), u.getThemeTint(), u.getThemeShade(), theme);
	}

	private static Integer hexByte(String s) {
		if (s == null || s.isEmpty()) return null;
		try {
			int v = Integer.parseInt(s, 16);
			return v >= 0 && v <= 255 ? v : null;
		} catch (NumberFormatException e) {
			return null;
		}
	}

	/**
	 * Word's themeTint (tint true) or themeShade on a colour: float HSL, the channels truncated.
	 * That is what Word 365 draws (CR-034 §11: the three P1 cases with no usable written value, all
	 * exact); the values older versions of Word wrote are within one unit of it (all 119 distinct
	 * ones in four corpora, 77 exact).
	 */
	static ResolvedColor luminance(ResolvedColor c, double t, boolean tint) {
		double[] hsl = ColorMath.rgbToHsl(c.getRed() / 255.0, c.getGreen() / 255.0, c.getBlue() / 255.0);
		double l = tint ? hsl[2] * t + (1 - t) : hsl[2] * t;
		double[] rgb = ColorMath.hslToRgb(hsl[0], hsl[1], ColorMath.clamp01(l));
		return ResolvedColor.rgb(truncate(rgb[0]), truncate(rgb[1]), truncate(rgb[2]));
	}

	private static int truncate(double v) {
		return (int) Math.floor(ColorMath.clamp01(v) * 255 + 1e-9);
	}
}
