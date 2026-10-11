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

import org.xlsx4j.sml.CTColor;
import org.xlsx4j.sml.CTIndexedColors;
import org.xlsx4j.sml.CTRgbColor;

/**
 * SpreadsheetML colours ({@code x:color}, {@code x:fgColor}, {@code x:bgColor} and the font and
 * border colours, ISO/IEC 29500-1 §18.8) resolved as Excel draws them (CR-034 §11, P4).
 *
 * <ul>
 * <li>{@code theme}: Excel's index swaps the first two pairs (0 lt1, 1 dk1, 2 lt2, 3 dk2, then the
 *     accents and hyperlinks), as {@link ThemeColors#sheetThemeColor(int)} does.</li>
 * <li>{@code indexed}: the legacy 64-colour palette, or all 64 entries of the workbook's own
 *     {@code x:indexedColors}; 64 and 65 the system foreground and background (black, white).</li>
 * <li>{@code tint}, −1 to 1: the HLS luminance, L' = L·(1 + tint) below 0, L·(1 − tint) + HLSMAX·tint
 *     above, in Windows' integer HLS (HLSMAX 240), the luminance truncated after adding 0.02; a
 *     tint of 0 leaves the colour alone.  All 90 theme-and-tint cells measured exact.  (Not POI's
 *     per-channel rule.)</li>
 * </ul>
 *
 * @since 17.3.3 (CR-034)
 */
public final class SpreadsheetColors {

	private SpreadsheetColors() {
	}

	/**
	 * @param c the colour element
	 * @param theme the workbook's theme
	 * @param indexedColors the workbook's {@code x:indexedColors}, or null for the legacy palette
	 * @return null for {@code auto} or nothing stated: the caller's automatic colour
	 */
	public static ResolvedColor resolve(CTColor c, ThemeColors theme, CTIndexedColors indexedColors) {
		if (c == null || Boolean.TRUE.equals(c.isAuto())) return null;
		ResolvedColor base = null;
		if (c.getRgb() != null) {
			base = argb(c.getRgb());
		} else if (c.getTheme() != null) {
			base = theme == null ? null : theme.sheetThemeColor(c.getTheme().intValue());
		} else if (c.getIndexed() != null) {
			base = indexed(c.getIndexed().intValue(), indexedColors);
		}
		if (base == null) return null;
		double tint = c.getTint();
		return tint == 0 ? base : tint(base, tint);
	}

	/** An indexed colour: the workbook's palette for 0 to 63 where it has one, else the legacy one */
	public static ResolvedColor indexed(int i, CTIndexedColors indexedColors) {
		if (indexedColors != null && i >= 0 && i < 64) {
			List<CTRgbColor> custom = indexedColors.getRgbColor();
			if (i < custom.size() && custom.get(i).getRgb() != null) return argb(custom.get(i).getRgb());
		}
		return i >= 0 && i < ColorTables.INDEXED.length ? ResolvedColor.ofHex(ColorTables.INDEXED[i]) : null;
	}

	/** ARGB (4 bytes) or RGB (3 bytes); the alpha byte is not drawn by Excel and is ignored */
	private static ResolvedColor argb(byte[] b) {
		if (b.length == 4) return ResolvedColor.rgb(b[1] & 0xFF, b[2] & 0xFF, b[3] & 0xFF);
		if (b.length == 3) return ResolvedColor.rgb(b[0] & 0xFF, b[1] & 0xFF, b[2] & 0xFF);
		return null;
	}

	/**
	 * Excel's tint: Windows' integer HLS, the luminance truncated after adding 0.02.  Plain
	 * truncation is two cells out of 90 short (both at tint 0.3333, whose luminance comes to just
	 * under a whole number and which Excel rounds up); any tolerance from 0.01 to 0.05 gives all 90,
	 * presumably the precision Excel keeps a tint at.
	 */
	static ResolvedColor tint(ResolvedColor c, double tint) {
		int[] hls = ColorMath.rgbToHls240(c.getRed(), c.getGreen(), c.getBlue());
		double l = tint < 0 ? hls[1] * (1 + tint) : hls[1] * (1 - tint) + (ColorMath.HLSMAX - ColorMath.HLSMAX * (1 - tint));
		int li = (int) (l + 0.02);
		if (li < 0) li = 0;
		if (li > ColorMath.HLSMAX) li = ColorMath.HLSMAX;
		int[] rgb = ColorMath.hls240ToRgb(hls[0], li, hls[2]);
		return ResolvedColor.rgb(rgb[0], rgb[1], rgb[2]);
	}
}
