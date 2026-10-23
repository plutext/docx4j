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

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

/**
 * The fixed colour tables: DrawingML's preset colours ({@code a:prstClr}), Windows' default system
 * colours ({@code a:sysClr}, which Office draws as the machine's colour, not {@code lastClr}: CR-034
 * §11), SpreadsheetML's legacy indexed palette, and the HTML colour names Word reads in a {@code w:color}
 * (CR-060).
 *
 * <p>The preset and system values are Apache POI's {@code org.apache.poi.sl.usermodel.PresetColor}
 * (trunk ae62bb5116, Apache-2.0), keyed by the names docx4j's schema gives
 * {@code ST_PresetColorVal} and {@code ST_SystemColorVal}.  The indexed palette is Excel 365's, read
 * from its PDF of CR-034's probe tc-p4-spreadsheet.xlsx (2026-10-12): entries 0 to 63, then 64 and
 * 65, the system foreground and background.</p>
 *
 * @since 17.3.3 (CR-034)
 */
final class ColorTables {

	private ColorTables() {
	}

	/** a:prstClr names to RRGGBB */
	static final Map<String, String> PRESET = table(
			"aliceBlue", "F0F8FF", "antiqueWhite", "FAEBD7", "aqua", "00FFFF",
			"aquamarine", "7FFFD4", "azure", "F0FFFF", "beige", "F5F5DC",
			"bisque", "FFE4C4", "black", "000000", "blanchedAlmond", "FFEBCD",
			"blue", "0000FF", "blueViolet", "8A2BE2", "brown", "A52A2A",
			"burlyWood", "DEB887", "cadetBlue", "5F9EA0", "chartreuse", "7FFF00",
			"chocolate", "D2691E", "coral", "FF7F50", "cornflowerBlue", "6495ED",
			"cornsilk", "FFF8DC", "crimson", "DC143C", "cyan", "00FFFF",
			"dkBlue", "00008B", "dkCyan", "008B8B", "dkGoldenrod", "B8860B",
			"dkGray", "A9A9A9", "dkGreen", "006400", "dkKhaki", "BDB76B",
			"dkMagenta", "8B008B", "dkOliveGreen", "556B2F", "dkOrange", "FF8C00",
			"dkOrchid", "9932CC", "dkRed", "8B0000", "dkSalmon", "E9967A",
			"dkSeaGreen", "8FBC8B", "dkSlateBlue", "483D8B", "dkSlateGray", "2F4F4F",
			"dkTurquoise", "00CED1", "dkViolet", "9400D3", "deepPink", "FF1493",
			"deepSkyBlue", "00BFFF", "dimGray", "696969", "dodgerBlue", "1E90FF",
			"firebrick", "B22222", "floralWhite", "FFFAF0", "forestGreen", "228B22",
			"fuchsia", "FF00FF", "gainsboro", "DCDCDC", "ghostWhite", "F8F8FF",
			"gold", "FFD700", "goldenrod", "DAA520", "gray", "808080",
			"green", "008000", "greenYellow", "ADFF2F", "honeydew", "F0FFF0",
			"hotPink", "FF69B4", "indianRed", "CD5C5C", "indigo", "4B0082",
			"ivory", "FFFFF0", "khaki", "F0E68C", "lavender", "E6E6FA",
			"lavenderBlush", "FFF0F5", "lawnGreen", "7CFC00", "lemonChiffon", "FFFACD",
			"ltBlue", "ADD8E6", "ltCoral", "F08080", "ltCyan", "E0FFFF",
			"ltGoldenrodYellow", "FAFA78", "ltGray", "D3D3D3", "ltGreen", "90EE90",
			"ltPink", "FFB6C1", "ltSalmon", "FFA07A", "ltSeaGreen", "20B2AA",
			"ltSkyBlue", "87CEFA", "ltSlateGray", "778899", "ltSteelBlue", "B0C4DE",
			"ltYellow", "FFFFE0", "lime", "00FF00", "limeGreen", "32CD32",
			"linen", "FAF0E6", "magenta", "FF00FF", "maroon", "800000",
			"medAquamarine", "66CDAA", "medBlue", "0000CD", "medOrchid", "BA55D3",
			"medPurple", "9370DB", "medSeaGreen", "3CB371", "medSlateBlue", "7B68EE",
			"medSpringGreen", "00FA9A", "medTurquoise", "48D1CC", "medVioletRed", "C71585",
			"midnightBlue", "191970", "mintCream", "F5FFFA", "mistyRose", "FFE4E1",
			"moccasin", "FFE4B5", "navajoWhite", "FFDEAD", "navy", "000080",
			"oldLace", "FDF5E6", "olive", "808000", "oliveDrab", "6B8E23",
			"orange", "FFA500", "orangeRed", "FF4500", "orchid", "DA70D6",
			"paleGoldenrod", "EEE8AA", "paleGreen", "98FB98", "paleTurquoise", "AFEEEE",
			"paleVioletRed", "DB7093", "papayaWhip", "FFEFD5", "peachPuff", "FFDAB9",
			"peru", "CD853F", "pink", "FFC0CB", "plum", "DDA0DD",
			"powderBlue", "B0E0E6", "purple", "800080", "red", "FF0000",
			"rosyBrown", "BC8F8F", "royalBlue", "4169E1", "saddleBrown", "8B4513",
			"salmon", "FA8072", "sandyBrown", "F4A460", "seaGreen", "2E8B57",
			"seaShell", "FFF5EE", "sienna", "A0522D", "silver", "C0C0C0",
			"skyBlue", "87CEEB", "slateBlue", "6A5ACD", "slateGray", "708090",
			"snow", "FFFAFA", "springGreen", "00FF7F", "steelBlue", "4682B4",
			"tan", "D2B48C", "teal", "008080", "thistle", "D8BFD8",
			"tomato", "FF6347", "turquoise", "40E0D0", "violet", "EE82EE",
			"wheat", "F5DEB3", "white", "FFFFFF", "whiteSmoke", "F5F5F5",
			"yellow", "FFFF00", "yellowGreen", "9ACD32");

	/**
	 * The classic HTML colour names (Netscape's, 140 of them) to RRGGBB, lower-case: the names Word 365 reads in a
	 * {@code w:color} (CR-060 section 1, rule 3; D5). Measured in four rounds: the grays spelt with an a, but
	 * {@code lightgrey} with an e; no {@code grey} or other e spellings, no CSS 4 names, no system colours; Word's
	 * hex for the sixty-odd names it was given is these.
	 */
	static final Map<String, String> HTML = table(
			"aliceblue", "F0F8FF", "antiquewhite", "FAEBD7", "aqua", "00FFFF", "aquamarine", "7FFFD4", "azure", "F0FFFF",
			"beige", "F5F5DC", "bisque", "FFE4C4", "black", "000000", "blanchedalmond", "FFEBCD", "blue", "0000FF",
			"blueviolet", "8A2BE2", "brown", "A52A2A", "burlywood", "DEB887", "cadetblue", "5F9EA0", "chartreuse", "7FFF00",
			"chocolate", "D2691E", "coral", "FF7F50", "cornflowerblue", "6495ED", "cornsilk", "FFF8DC", "crimson", "DC143C",
			"cyan", "00FFFF", "darkblue", "00008B", "darkcyan", "008B8B", "darkgoldenrod", "B8860B", "darkgray", "A9A9A9",
			"darkgreen", "006400", "darkkhaki", "BDB76B", "darkmagenta", "8B008B", "darkolivegreen", "556B2F",
			"darkorange", "FF8C00", "darkorchid", "9932CC", "darkred", "8B0000", "darksalmon", "E9967A",
			"darkseagreen", "8FBC8F", "darkslateblue", "483D8B", "darkslategray", "2F4F4F", "darkturquoise", "00CED1",
			"darkviolet", "9400D3", "deeppink", "FF1493", "deepskyblue", "00BFFF", "dimgray", "696969",
			"dodgerblue", "1E90FF", "firebrick", "B22222", "floralwhite", "FFFAF0", "forestgreen", "228B22",
			"fuchsia", "FF00FF", "gainsboro", "DCDCDC", "ghostwhite", "F8F8FF", "gold", "FFD700", "goldenrod", "DAA520",
			"gray", "808080", "green", "008000", "greenyellow", "ADFF2F", "honeydew", "F0FFF0", "hotpink", "FF69B4",
			"indianred", "CD5C5C", "indigo", "4B0082", "ivory", "FFFFF0", "khaki", "F0E68C", "lavender", "E6E6FA",
			"lavenderblush", "FFF0F5", "lawngreen", "7CFC00", "lemonchiffon", "FFFACD", "lightblue", "ADD8E6",
			"lightcoral", "F08080", "lightcyan", "E0FFFF", "lightgoldenrodyellow", "FAFAD2", "lightgreen", "90EE90",
			"lightgrey", "D3D3D3", "lightpink", "FFB6C1", "lightsalmon", "FFA07A", "lightseagreen", "20B2AA",
			"lightskyblue", "87CEFA", "lightslategray", "778899", "lightsteelblue", "B0C4DE", "lightyellow", "FFFFE0",
			"lime", "00FF00", "limegreen", "32CD32", "linen", "FAF0E6", "magenta", "FF00FF", "maroon", "800000",
			"mediumaquamarine", "66CDAA", "mediumblue", "0000CD", "mediumorchid", "BA55D3", "mediumpurple", "9370DB",
			"mediumseagreen", "3CB371", "mediumslateblue", "7B68EE", "mediumspringgreen", "00FA9A",
			"mediumturquoise", "48D1CC", "mediumvioletred", "C71585", "midnightblue", "191970", "mintcream", "F5FFFA",
			"mistyrose", "FFE4E1", "moccasin", "FFE4B5", "navajowhite", "FFDEAD", "navy", "000080", "oldlace", "FDF5E6",
			"olive", "808000", "olivedrab", "6B8E23", "orange", "FFA500", "orangered", "FF4500", "orchid", "DA70D6",
			"palegoldenrod", "EEE8AA", "palegreen", "98FB98", "paleturquoise", "AFEEEE", "palevioletred", "DB7093",
			"papayawhip", "FFEFD5", "peachpuff", "FFDAB9", "peru", "CD853F", "pink", "FFC0CB", "plum", "DDA0DD",
			"powderblue", "B0E0E6", "purple", "800080", "red", "FF0000", "rosybrown", "BC8F8F", "royalblue", "4169E1",
			"saddlebrown", "8B4513", "salmon", "FA8072", "sandybrown", "F4A460", "seagreen", "2E8B57", "seashell", "FFF5EE",
			"sienna", "A0522D", "silver", "C0C0C0", "skyblue", "87CEEB", "slateblue", "6A5ACD", "slategray", "708090",
			"snow", "FFFAFA", "springgreen", "00FF7F", "steelblue", "4682B4", "tan", "D2B48C", "teal", "008080",
			"thistle", "D8BFD8", "tomato", "FF6347", "turquoise", "40E0D0", "violet", "EE82EE", "wheat", "F5DEB3",
			"white", "FFFFFF", "whitesmoke", "F5F5F5", "yellow", "FFFF00", "yellowgreen", "9ACD32");

	/** a:sysClr names to Windows' default RRGGBB */
	static final Map<String, String> SYSTEM = table(
			"scrollBar", "C8C8C8", "background", "000000", "activeCaption", "99B4D1",
			"inactiveCaption", "BFCDDB", "menu", "F0F0F0", "window", "FFFFFF",
			"windowFrame", "646464", "menuText", "000000", "windowText", "000000",
			"captionText", "000000", "activeBorder", "B4B4B4", "inactiveBorder", "F4F7FC",
			"appWorkspace", "ABABAB", "highlight", "3399FF", "highlightText", "FFFFFF",
			"btnFace", "F0F0F0", "btnShadow", "696969", "grayText", "6D6D6D",
			"btnText", "000000", "inactiveCaptionText", "000000", "btnHighlight", "E3E3E3",
			"3dDkShadow", "000000", "3dLight", "E3E3E3", "infoText", "000000",
			"infoBk", "FFFFE1", "hotLight", "0066CC", "gradientActiveCaption", "B9D1EA",
			"gradientInactiveCaption", "D7E4F2", "menuHighlight", "3399FF", "menuBar", "F0F0F0");

	/** SpreadsheetML indexed colours 0 to 65 */
	static final String[] INDEXED = {
		"000000", "FFFFFF", "FF0000", "00FF00", "0000FF", "FFFF00", "FF00FF", "00FFFF",
		"000000", "FFFFFF", "FF0000", "00FF00", "0000FF", "FFFF00", "FF00FF", "00FFFF",
		"800000", "008000", "000080", "808000", "800080", "008080", "C0C0C0", "808080",
		"9999FF", "993366", "FFFFCC", "CCFFFF", "660066", "FF8080", "0066CC", "CCCCFF",
		"000080", "FF00FF", "FFFF00", "00FFFF", "800080", "800000", "008080", "0000FF",
		"00CCFF", "CCFFFF", "CCFFCC", "FFFF99", "99CCFF", "FF99CC", "CC99FF", "FFCC99",
		"3366FF", "33CCCC", "99CC00", "FFCC00", "FF9900", "FF6600", "666699", "969696",
		"003366", "339966", "003300", "333300", "993300", "993366", "333399", "333333",
		"000000", "FFFFFF"
	};

	private static Map<String, String> table(String... pairs) {
		Map<String, String> m = new HashMap<>();
		for (int i = 0; i < pairs.length; i += 2) {
			m.put(pairs[i], pairs[i + 1]);
		}
		return Collections.unmodifiableMap(m);
	}
}
