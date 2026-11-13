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

import java.util.Locale;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * VML colours (ISO/IEC 29500-4 §19.1.2.5's and §19.1.2.21's colour attributes, VML's ST_ColorType), as six hex
 * digits: a name or hex spelling {@link org.docx4j.model.color.WordColors#normalise} reads; {@code #RGB};
 * {@code rgb(r, g, b)}; a colour followed by an index ({@code #FFFFFF [9]}: its first word); and a colour relative to
 * another ({@code fill darken(118)}, {@code fill lighten(200)}, {@code line}: the fill's or line's colour, darkened or
 * lightened by n/255), given that colour (CR-083 phase 4, findings §4 item 11). Shared with
 * {@link org.docx4j.model.images.PictureRecolour}'s reading of {@code v:imagedata} (CR-084 phase 3).
 *
 * @since 17.3.7
 */
public final class VmlColours {

	private VmlColours() {}

	private static final Pattern RGB = Pattern.compile("(?i)rgb\\(\\s*(\\d{1,3})\\s*,\\s*(\\d{1,3})\\s*,\\s*(\\d{1,3})\\s*\\)");
	private static final Pattern RELATIVE = Pattern.compile("(?i)(fill|line|shadow)(?:\\s+(darken|lighten)\\(\\s*(\\d{1,3})\\s*\\))?\\s*(\\[\\d+\\])?");

	/** A colour on its own; null for a relative one or what can't be read. */
	public static String rgb(String s) {
		return rgb(s, null);
	}

	/**
	 * A colour, as six hex digits.
	 *
	 * @param relativeTo the colour a relative one ({@code fill darken(n)}) is taken from, six hex digits, or null
	 * @return null where it can't be read, or is relative to no colour
	 */
	public static String rgb(String s, String relativeTo) {
		if (s == null) return null;
		String v = s.trim();
		Matcher m = RGB.matcher(v);
		if (m.lookingAt()) {
			int r = Integer.parseInt(m.group(1)), g = Integer.parseInt(m.group(2)), b = Integer.parseInt(m.group(3));
			if (r > 255 || g > 255 || b > 255) return null;
			return String.format("%02X%02X%02X", r, g, b);
		}
		Matcher rel = RELATIVE.matcher(v);
		if (rel.matches()) {
			if (relativeTo == null || !relativeTo.matches("[0-9A-Fa-f]{6}")) return null;
			if (rel.group(2) == null) return relativeTo.toUpperCase(Locale.ROOT);
			int n = Math.min(255, Integer.parseInt(rel.group(3)));
			boolean darken = "darken".equalsIgnoreCase(rel.group(2));
			StringBuilder out = new StringBuilder();
			for (int i = 0; i < 3; i++) {
				int c = Integer.parseInt(relativeTo.substring(2 * i, 2 * i + 2), 16);
				int d = darken ? Math.round(c * n / 255f) : 255 - Math.round((255 - c) * n / 255f);
				out.append(String.format("%02X", Math.max(0, Math.min(255, d))));
			}
			return out.toString();
		}
		v = v.split("\\s+")[0];
		if (v.matches("#?[0-9A-Fa-f]{3}")) {
			String d = v.startsWith("#") ? v.substring(1) : v;
			StringBuilder b = new StringBuilder();
			for (char c : d.toCharArray()) b.append(c).append(c);
			return b.toString().toUpperCase(Locale.ROOT);
		}
		String system = system(v);
		if (system != null) return system;
		return org.docx4j.model.color.WordColors.normalise(v);
	}

	/** The Windows system colours VML may name, at Windows' defaults. */
	private static String system(String v) {
		switch (v.toLowerCase(Locale.ROOT)) {
		case "windowtext": case "buttontext": case "captiontext": case "infotext": case "menutext": return "000000";
		case "window": case "buttonhighlight": case "highlighttext": case "menu": return "FFFFFF";
		case "buttonface": case "threedface": case "menubar": return "F0F0F0";
		case "buttonshadow": case "threedshadow": return "A0A0A0";
		case "graytext": return "6D6D6D";
		case "highlight": return "0078D7";
		case "infobackground": return "FFFFE1";
		default: return null;
		}
	}
}
