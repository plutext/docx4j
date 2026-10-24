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
package org.docx4j.wml;

import java.util.Collections;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

/**
 * A WordprocessingML colour value ({@code ST_HexColor}: {@code w:color/@w:val}, {@code w:u/@w:color},
 * {@code w:shd/@w:fill}, a border's {@code w:color}) as Word 365 reads it (CR-060 section 1 rule 3, sections 12 and
 * 13). Here, beside the generated classes, so that both their save adapters ({@link NormaliseOnSave}) and docx4j-core's
 * readers ({@code org.docx4j.model.color.WordColors}) use the one rule.
 *
 * @since 17.3.5
 */
public final class WordColorValues {

	private WordColorValues() {
	}

	/**
	 * The classic HTML colour names (Netscape's, 140 of them) to RRGGBB, lower-case: the names Word 365 reads in a
	 * colour (CR-060 D5). Measured in four rounds: the grays spelt with an a, but {@code lightgrey} with an e; no
	 * {@code grey} or other e spellings, no CSS 4 names, no system colours; Word's hex for the sixty-odd names it was
	 * given is these.
	 */
	private static final Map<String, String> HTML = table(
		"aliceblue", "F0F8FF", "antiquewhite", "FAEBD7", "aqua", "00FFFF", "aquamarine", "7FFFD4", "azure", "F0FFFF",
		"beige", "F5F5DC", "bisque", "FFE4C4", "black", "000000", "blanchedalmond", "FFEBCD", "blue", "0000FF",
		"blueviolet", "8A2BE2", "brown", "A52A2A", "burlywood", "DEB887", "cadetblue", "5F9EA0", "chartreuse", "7FFF00",
		"chocolate", "D2691E", "coral", "FF7F50", "cornflowerblue", "6495ED", "cornsilk", "FFF8DC", "crimson", "DC143C",
		"cyan", "00FFFF", "darkblue", "00008B", "darkcyan", "008B8B", "darkgoldenrod", "B8860B", "darkgray", "A9A9A9",
		"darkgreen", "006400", "darkkhaki", "BDB76B", "darkmagenta", "8B008B", "darkolivegreen", "556B2F",
		"darkorange", "FF8C00", "darkorchid", "9932CC", "darkred", "8B0000", "darksalmon", "E9967A",
		"darkseagreen", "8FBC8F", "darkslateblue", "483D8B", "darkslategray", "2F4F4F", "darkturquoise", "00CED1",
		"darkviolet", "9400D3", "deeppink", "FF1493", "deepskyblue", "00BFFF", "dimgray", "696969", "dodgerblue", "1E90FF",
		"firebrick", "B22222", "floralwhite", "FFFAF0", "forestgreen", "228B22", "fuchsia", "FF00FF",
		"gainsboro", "DCDCDC", "ghostwhite", "F8F8FF", "gold", "FFD700", "goldenrod", "DAA520", "gray", "808080",
		"green", "008000", "greenyellow", "ADFF2F", "honeydew", "F0FFF0", "hotpink", "FF69B4", "indianred", "CD5C5C",
		"indigo", "4B0082", "ivory", "FFFFF0", "khaki", "F0E68C", "lavender", "E6E6FA", "lavenderblush", "FFF0F5",
		"lawngreen", "7CFC00", "lemonchiffon", "FFFACD", "lightblue", "ADD8E6", "lightcoral", "F08080",
		"lightcyan", "E0FFFF", "lightgoldenrodyellow", "FAFAD2", "lightgreen", "90EE90", "lightgrey", "D3D3D3",
		"lightpink", "FFB6C1", "lightsalmon", "FFA07A", "lightseagreen", "20B2AA", "lightskyblue", "87CEFA",
		"lightslategray", "778899", "lightsteelblue", "B0C4DE", "lightyellow", "FFFFE0", "lime", "00FF00",
		"limegreen", "32CD32", "linen", "FAF0E6", "magenta", "FF00FF", "maroon", "800000", "mediumaquamarine", "66CDAA",
		"mediumblue", "0000CD", "mediumorchid", "BA55D3", "mediumpurple", "9370DB", "mediumseagreen", "3CB371",
		"mediumslateblue", "7B68EE", "mediumspringgreen", "00FA9A", "mediumturquoise", "48D1CC",
		"mediumvioletred", "C71585", "midnightblue", "191970", "mintcream", "F5FFFA", "mistyrose", "FFE4E1",
		"moccasin", "FFE4B5", "navajowhite", "FFDEAD", "navy", "000080", "oldlace", "FDF5E6", "olive", "808000",
		"olivedrab", "6B8E23", "orange", "FFA500", "orangered", "FF4500", "orchid", "DA70D6", "palegoldenrod", "EEE8AA",
		"palegreen", "98FB98", "paleturquoise", "AFEEEE", "palevioletred", "DB7093", "papayawhip", "FFEFD5",
		"peachpuff", "FFDAB9", "peru", "CD853F", "pink", "FFC0CB", "plum", "DDA0DD", "powderblue", "B0E0E6",
		"purple", "800080", "red", "FF0000", "rosybrown", "BC8F8F", "royalblue", "4169E1", "saddlebrown", "8B4513",
		"salmon", "FA8072", "sandybrown", "F4A460", "seagreen", "2E8B57", "seashell", "FFF5EE", "sienna", "A0522D",
		"silver", "C0C0C0", "skyblue", "87CEEB", "slateblue", "6A5ACD", "slategray", "708090", "snow", "FFFAFA",
		"springgreen", "00FF7F", "steelblue", "4682B4", "tan", "D2B48C", "teal", "008080", "thistle", "D8BFD8",
		"tomato", "FF6347", "turquoise", "40E0D0", "violet", "EE82EE", "wheat", "F5DEB3", "white", "FFFFFF",
		"whitesmoke", "F5F5F5", "yellow", "FFFF00", "yellowgreen", "9ACD32");

	/**
	 * A colour value as Word reads it: surrounding whitespace trimmed, six hex digits in either case with an optional
	 * leading '#'; else a classic HTML colour name, in any case.
	 *
	 * @return six upper-case hex digits; null for {@code auto} and for a value Word rejects, which Word reads as
	 * automatic in a fill or a border colour, and as absent in a text or underline colour ({@link #stated})
	 */
	public static String normalise(String val) {
		if (val == null) return null;
		String v = val.trim();
		String hex = v.startsWith("#") ? v.substring(1) : v;
		if (hex.length() == 6 && hex.chars().allMatch(ch -> Character.digit(ch, 16) >= 0)) {
			return hex.toUpperCase(Locale.ROOT);
		}
		return HTML.get(v.toLowerCase(Locale.ROOT));
	}

	/**
	 * Whether a {@code w:color/@w:val} or {@code w:u/@w:color} value states a colour, as Word reads it: one
	 * {@link #normalise} accepts, or exactly {@code auto} (automatic, which overrides a style's colour). Word reads any
	 * other value ({@code F00}, {@code bogus}, {@code ""}, {@code Auto}, {@code " auto"}) as absent: a style's colour
	 * beneath still shows. (In a fill or a border colour it reads such a value as automatic instead.)
	 */
	public static boolean stated(String val) {
		return "auto".equals(val) || normalise(val) != null;
	}

	private static Map<String, String> table(String... pairs) {
		Map<String, String> m = new HashMap<>();
		for (int i = 0; i < pairs.length; i += 2) {
			m.put(pairs[i], pairs[i + 1]);
		}
		return Collections.unmodifiableMap(m);
	}
}
