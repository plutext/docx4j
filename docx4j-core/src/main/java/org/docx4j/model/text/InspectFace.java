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
package org.docx4j.model.text;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;

import org.apache.xmlgraphics.fonts.Glyphs;

/**
 * An embedded face, as CR-050 D18's tests read it: its {@code cmap} (the Unicode and symbol
 * subtables, the first to map a code winning), which glyphs have an outline ({@code glyf}), their
 * advances ({@code hmtx}), their names ({@code post} format 2) and the single and multiple
 * substitutions of its {@code GSUB} features. TrueType outlines only: a face of another kind is not
 * read ({@link NotTrueType}). Only the package's own bytes are read, never an installed font.
 */
final class InspectFace {

	/** A face whose outlines are not TrueType's ({@code CFF}, a collection): its glyphs are not tested. */
	static final class NotTrueType extends IOException {
		private static final long serialVersionUID = 1L;
		NotTrueType(String what) {
			super(what);
		}
	}

	private final byte[] data;
	private final Map<String, int[]> tables = new HashMap<String, int[]>();
	final int unitsPerEm;
	private final int numGlyphs;
	/** code point to glyph */
	private final Map<Integer, Integer> cmap = new LinkedHashMap<Integer, Integer>();
	/** glyph to the code points mapping to it */
	private final Map<Integer, List<Integer>> codes = new HashMap<Integer, List<Integer>>();
	private final int[] advances;
	private final boolean[] outlines;
	/** the post table's names (format 2), unique as fontTools makes them; null for another format */
	private final String[] names;
	private Map<String, List<Integer>> features;
	/** Each glyph's substitutions, by the features asked for. */
	private final Map<String, List<Integer>> substituted = new HashMap<String, List<Integer>>();

	/**
	 * The code points a face's {@code cmap} subtables may map, together, at most: a hostile range costs
	 * no more. Nine times the most any of the layout-fidelity corpora's 97 embedded faces maps (28,737).
	 */
	private static final int CMAP_BUDGET = 0x40000;
	/** The {@code cmap} encoding records read, at most (a face has a handful). */
	private static final int CMAP_RECORDS = 64;
	/** The {@code GSUB} subtables one glyph's substitutions read, at most. */
	private static final int GSUB_BUDGET = 4096;

	private InspectFace(byte[] data) throws IOException {
		this.data = data;
		int version = u32(0);
		if (version == 0x4F54544F) throw new NotTrueType("CFF outlines (OTTO)");
		if (version == 0x74746366) throw new NotTrueType("a font collection (ttcf)");
		if (version != 0x00010000 && version != 0x74727565) throw new IOException("not a TrueType font");
		int n = u16(4);
		for (int i = 0; i < n; i++) {
			int rec = 12 + 16 * i;
			String tag = new String(data, rec, 4, java.nio.charset.StandardCharsets.ISO_8859_1);
			int offset = u32(rec + 8), length = u32(rec + 12);
			if (offset < 0 || length < 0 || (long) offset + length > data.length) throw new IOException("table " + tag + " out of range");
			tables.put(tag, new int[] { offset, length });
		}
		if (!tables.containsKey("glyf")) throw new NotTrueType("no glyf table");
		int head = table("head");
		unitsPerEm = u16(head + 18);
		int indexToLocFormat = s16(head + 50);
		numGlyphs = u16(table("maxp") + 4);
		outlines = new boolean[numGlyphs];
		int loca = table("loca"), glyf = table("glyf");
		for (int g = 0; g < numGlyphs; g++) {
			long start = indexToLocFormat == 0 ? 2L * u16(loca + 2 * g) : u32(loca + 4 * g) & 0xffffffffL;
			long end = indexToLocFormat == 0 ? 2L * u16(loca + 2 * g + 2) : u32(loca + 4 * g + 4) & 0xffffffffL;
			// no bytes is no outline; a simple glyph with no contours is none either; a composite (-1) has one
			outlines[g] = end > start && s16((int) (glyf + start)) != 0;
		}
		int hhea = table("hhea"), hmtx = table("hmtx");
		int metrics = u16(hhea + 34);
		advances = new int[numGlyphs];
		for (int g = 0; g < numGlyphs; g++) advances[g] = metrics == 0 ? 0 : u16(hmtx + 4 * Math.min(g, metrics - 1));
		readCmap();
		names = readPost();
	}

	/** A face read from its de-obfuscated bytes. */
	static InspectFace read(byte[] data) throws IOException {
		try {
			return new InspectFace(data);
		} catch (ArrayIndexOutOfBoundsException e) {
			throw new IOException("a truncated font", e);
		}
	}

	/** The glyph a code point maps to, or null; 0 is {@code .notdef}. */
	Integer glyph(int codePoint) {
		return cmap.get(codePoint);
	}

	boolean outline(int glyph) {
		return glyph >= 0 && glyph < numGlyphs && outlines[glyph];
	}

	int advance(int glyph) {
		return glyph >= 0 && glyph < numGlyphs ? advances[glyph] : 0;
	}

	/** The code points that map to a glyph, in the {@code cmap}'s order. */
	List<Integer> codes(int glyph) {
		List<Integer> l = codes.get(glyph);
		return l == null ? Collections.<Integer>emptyList() : l;
	}

	/** Whether the face names its glyphs ({@code post} format 2). */
	boolean hasNames() {
		return names != null;
	}

	/** A glyph's name ({@code post} format 2), or null. */
	String name(int glyph) {
		return names == null || glyph < 0 || glyph >= names.length ? null : names[glyph];
	}

	/** What the single (type 1) and multiple (type 2) substitutions of these features give for a glyph, directly. */
	List<Integer> substitutions(int glyph, Set<String> wanted) {
		String key = glyph + " " + new TreeSet<String>(wanted);
		List<Integer> cached = substituted.get(key);
		if (cached != null) return cached;
		List<Integer> out = new ArrayList<Integer>();
		substituted.put(key, out);
		if (!tables.containsKey("GSUB")) return out;
		int budget = GSUB_BUDGET;
		try {
			int gsub = table("GSUB");
			TreeSet<Integer> lookups = new TreeSet<Integer>();
			for (Map.Entry<String, List<Integer>> f : features(gsub).entrySet()) if (wanted.contains(f.getKey())) lookups.addAll(f.getValue());
			int lookupList = gsub + u16(gsub + 8);
			int count = u16(lookupList);
			for (int li : lookups) {
				if (li >= count) continue;
				int lookup = lookupList + u16(lookupList + 2 + 2 * li);
				int type = u16(lookup);
				int subtables = u16(lookup + 4);
				for (int s = 0; s < subtables && budget-- > 0; s++) {
					int st = lookup + u16(lookup + 6 + 2 * s);
					int t = type;
					if (t == 7) {
						t = u16(st + 2);
						st = st + u32(st + 4);
					}
					if (t == 1) single(st, glyph, out);
					else if (t == 2) multiple(st, glyph, out);
				}
			}
		} catch (IOException | RuntimeException e) {
			// a malformed table: no substitution is evidence
		}
		return out;
	}

	// --- cmap ---------------------------------------------------------------------------------------

	private void readCmap() throws IOException {
		int cmapTable = table("cmap");
		int n = Math.min(u16(cmapTable + 2), CMAP_RECORDS);
		int[] budget = { CMAP_BUDGET }; // the face's, whatever its records
		Set<Integer> read = new HashSet<Integer>();
		for (int i = 0; i < n && budget[0] > 0; i++) {
			int platform = u16(cmapTable + 4 + 8 * i), encoding = u16(cmapTable + 6 + 8 * i);
			int offset = cmapTable + u32(cmapTable + 8 + 8 * i);
			// fontTools' isUnicode() and the symbol subtable, as the oracle reads them
			boolean unicode = platform == 0 || platform == 3 && (encoding == 0 || encoding == 1 || encoding == 10);
			if (!unicode) continue;
			if (!read.add(offset)) continue; // a subtable two records share adds nothing the first did not
			int format = u16(offset);
			Map<Integer, Integer> sub = new LinkedHashMap<Integer, Integer>();
			if (format == 4) format4(offset, sub, budget);
			else if (format == 12) format12(offset, sub, budget);
			else if (format == 0) for (int c = 0; c < 256 && budget[0]-- > 0; c++) put(sub, c, data[offset + 6 + c] & 0xff);
			else if (format == 6) {
				int first = u16(offset + 6), count = u16(offset + 8);
				for (int c = 0; c < count && budget[0]-- > 0; c++) put(sub, first + c, u16(offset + 10 + 2 * c));
			}
			for (Map.Entry<Integer, Integer> e : sub.entrySet()) {
				if (cmap.containsKey(e.getKey())) continue;
				cmap.put(e.getKey(), e.getValue());
				codes.computeIfAbsent(e.getValue(), k -> new ArrayList<Integer>()).add(e.getKey());
			}
		}
	}

	private void put(Map<Integer, Integer> sub, int code, int glyph) {
		if (glyph != 0 && glyph < numGlyphs) sub.put(code, glyph);
	}

	private void format4(int offset, Map<Integer, Integer> sub, int[] budget) {
		int segX2 = u16(offset + 6);
		int ends = offset + 14, starts = ends + segX2 + 2, deltas = starts + segX2, ranges = deltas + segX2;
		for (int s = 0; s < segX2 / 2; s++) {
			int end = u16(ends + 2 * s), start = u16(starts + 2 * s), delta = s16(deltas + 2 * s), range = u16(ranges + 2 * s);
			for (int c = start; c <= end && c != 0xFFFF && budget[0]-- > 0; c++) {
				int g;
				if (range == 0) g = (c + delta) & 0xFFFF;
				else {
					int at = ranges + 2 * s + range + 2 * (c - start);
					g = u16(at);
					if (g != 0) g = (g + delta) & 0xFFFF;
				}
				put(sub, c, g);
			}
		}
	}

	private void format12(int offset, Map<Integer, Integer> sub, int[] budget) {
		int groups = u32(offset + 12);
		for (int i = 0; i < groups && budget[0] > 0; i++) {
			int start = u32(offset + 16 + 12 * i), end = u32(offset + 20 + 12 * i), g = u32(offset + 24 + 12 * i);
			if (start < 0 || end < start) continue;
			for (int c = start; c <= end && c <= 0x10FFFF && budget[0]-- > 0; c++) put(sub, c, g + (c - start));
		}
	}

	// --- post ---------------------------------------------------------------------------------------

	/** The standard Macintosh glyph order, which {@code post} format 2's indices below 258 name (fontTools' standardGlyphOrder). */
	private static final String[] MAC_GLYPH_NAMES = {
			".notdef", ".null", "nonmarkingreturn", "space", "exclam", "quotedbl", "numbersign", "dollar", "percent", "ampersand",
			"quotesingle", "parenleft", "parenright", "asterisk", "plus", "comma", "hyphen", "period", "slash", "zero",
			"one", "two", "three", "four", "five", "six", "seven", "eight", "nine", "colon",
			"semicolon", "less", "equal", "greater", "question", "at", "A", "B", "C", "D",
			"E", "F", "G", "H", "I", "J", "K", "L", "M", "N",
			"O", "P", "Q", "R", "S", "T", "U", "V", "W", "X",
			"Y", "Z", "bracketleft", "backslash", "bracketright", "asciicircum", "underscore", "grave", "a", "b",
			"c", "d", "e", "f", "g", "h", "i", "j", "k", "l",
			"m", "n", "o", "p", "q", "r", "s", "t", "u", "v",
			"w", "x", "y", "z", "braceleft", "bar", "braceright", "asciitilde", "Adieresis", "Aring",
			"Ccedilla", "Eacute", "Ntilde", "Odieresis", "Udieresis", "aacute", "agrave", "acircumflex", "adieresis", "atilde",
			"aring", "ccedilla", "eacute", "egrave", "ecircumflex", "edieresis", "iacute", "igrave", "icircumflex", "idieresis",
			"ntilde", "oacute", "ograve", "ocircumflex", "odieresis", "otilde", "uacute", "ugrave", "ucircumflex", "udieresis",
			"dagger", "degree", "cent", "sterling", "section", "bullet", "paragraph", "germandbls", "registered", "copyright",
			"trademark", "acute", "dieresis", "notequal", "AE", "Oslash", "infinity", "plusminus", "lessequal", "greaterequal",
			"yen", "mu", "partialdiff", "summation", "product", "pi", "integral", "ordfeminine", "ordmasculine", "Omega",
			"ae", "oslash", "questiondown", "exclamdown", "logicalnot", "radical", "florin", "approxequal", "Delta", "guillemotleft",
			"guillemotright", "ellipsis", "nonbreakingspace", "Agrave", "Atilde", "Otilde", "OE", "oe", "endash", "emdash",
			"quotedblleft", "quotedblright", "quoteleft", "quoteright", "divide", "lozenge", "ydieresis", "Ydieresis", "fraction", "currency",
			"guilsinglleft", "guilsinglright", "fi", "fl", "daggerdbl", "periodcentered", "quotesinglbase", "quotedblbase", "perthousand", "Acircumflex",
			"Ecircumflex", "Aacute", "Edieresis", "Egrave", "Iacute", "Icircumflex", "Idieresis", "Igrave", "Oacute", "Ocircumflex",
			"apple", "Ograve", "Uacute", "Ucircumflex", "Ugrave", "dotlessi", "circumflex", "tilde", "macron", "breve",
			"dotaccent", "ring", "cedilla", "hungarumlaut", "ogonek", "caron", "Lslash", "lslash", "Scaron", "scaron",
			"Zcaron", "zcaron", "brokenbar", "Eth", "eth", "Yacute", "yacute", "Thorn", "thorn", "minus",
			"multiply", "onesuperior", "twosuperior", "threesuperior", "onehalf", "onequarter", "threequarters", "franc", "Gbreve", "gbreve",
			"Idotaccent", "Scedilla", "scedilla", "Cacute", "cacute", "Ccaron", "ccaron", "dcroat"
	};


	private String[] readPost() {
		if (!tables.containsKey("post")) return null;
		int post = tables.get("post")[0];
		if (u32(post) != 0x00020000) return null;
		int n = u16(post + 32);
		int[] index = new int[n];
		for (int g = 0; g < n; g++) index[g] = u16(post + 34 + 2 * g);
		List<String> extra = new ArrayList<String>();
		int at = post + 34 + 2 * n, end = post + tables.get("post")[1];
		while (at < end) {
			int len = data[at] & 0xff;
			if (at + 1 + len > end) break;
			extra.add(new String(data, at + 1, len, java.nio.charset.StandardCharsets.ISO_8859_1));
			at += 1 + len;
		}
		String[] out = new String[numGlyphs];
		Map<String, Integer> all = new HashMap<String, Integer>();
		for (int g = 0; g < numGlyphs; g++) {
			String name = "";
			if (g < n) {
				int i = index[g];
				if (i < 258) name = MAC_GLYPH_NAMES[i];
				else if (i - 258 < extra.size()) name = extra.get(i - 258);
			}
			if (name.isEmpty()) name = String.format("glyph%05d", g);
			// fontTools' build_psNameMapping: a name met before gets #n
			if (all.containsKey(name)) {
				int k = all.get(name);
				while (all.containsKey(name + "#" + k)) k++;
				all.put(name, k + 1);
				name = name + "#" + k;
			}
			all.put(name, 1);
			out[g] = name;
		}
		return out;
	}

	/**
	 * The character a glyph name names, for D18's third test, as fontTools' {@code agl.toUnicode} reads
	 * it: the part before a full stop; an AGL name (the full list, xmlgraphics-commons'
	 * {@code glyphlist.txt}), a {@code uniXXXX} or a {@code uXXXX[XX]} value. Empty for a name of several
	 * components (a ligature's), of several characters, or none.
	 */
	static List<Integer> named(String name) {
		List<Integer> out = new ArrayList<Integer>();
		if (name == null) return out;
		int dot = name.indexOf('.');
		String base = dot >= 0 ? name.substring(0, dot) : name;
		if (base.isEmpty() || base.contains("_")) return out;
		String u = Glyphs.getUnicodeSequenceForGlyphName(base);
		if (u == null && base.matches("u[0-9A-F]{4,6}")) {
			int v = Integer.parseInt(base.substring(1), 16);
			if (v <= 0x10FFFF && !(v >= 0xD800 && v <= 0xDFFF)) u = new String(Character.toChars(v));
		}
		if (u != null && u.codePointCount(0, u.length()) == 1) out.add(u.codePointAt(0));
		return out;
	}

	// --- GSUB ---------------------------------------------------------------------------------------

	/** feature tag to lookup indices, from the DFLT and latn scripts' default language systems */
	private Map<String, List<Integer>> features(int gsub) {
		if (features != null) return features;
		Map<String, List<Integer>> out = new HashMap<String, List<Integer>>();
		int scriptList = gsub + u16(gsub + 4), featureList = gsub + u16(gsub + 6);
		int scripts = u16(scriptList);
		for (int i = 0; i < scripts; i++) {
			String tag = new String(data, scriptList + 2 + 6 * i, 4, java.nio.charset.StandardCharsets.ISO_8859_1);
			if (!tag.equals("DFLT") && !tag.equals("latn")) continue;
			int script = scriptList + u16(scriptList + 6 + 6 * i);
			int defaultLangSys = u16(script);
			if (defaultLangSys == 0) continue;
			int ls = script + defaultLangSys;
			int count = u16(ls + 4);
			for (int k = 0; k < count; k++) {
				int fi = u16(ls + 6 + 2 * k);
				if (fi >= u16(featureList)) continue;
				String ftag = new String(data, featureList + 2 + 6 * fi, 4, java.nio.charset.StandardCharsets.ISO_8859_1);
				int feature = featureList + u16(featureList + 6 + 6 * fi);
				int lookups = u16(feature + 2);
				List<Integer> l = out.computeIfAbsent(ftag, t -> new ArrayList<Integer>());
				for (int j = 0; j < lookups; j++) l.add(u16(feature + 4 + 2 * j));
			}
		}
		features = out;
		return out;
	}

	/** A glyph's index in a coverage table, or -1. */
	private int coverage(int cov, int glyph) {
		int format = u16(cov);
		if (format == 1) {
			int n = u16(cov + 2);
			for (int i = 0; i < n; i++) if (u16(cov + 4 + 2 * i) == glyph) return i;
		} else if (format == 2) {
			int n = u16(cov + 2);
			for (int i = 0; i < n; i++) {
				int start = u16(cov + 4 + 6 * i), end = u16(cov + 6 + 6 * i), index = u16(cov + 8 + 6 * i);
				if (glyph >= start && glyph <= end) return index + glyph - start;
			}
		}
		return -1;
	}

	private void single(int st, int glyph, List<Integer> out) {
		int format = u16(st);
		int i = coverage(st + u16(st + 2), glyph);
		if (i < 0) return;
		if (format == 1) out.add((glyph + s16(st + 4)) & 0xFFFF);
		else if (format == 2 && i < u16(st + 4)) out.add(u16(st + 6 + 2 * i));
	}

	private void multiple(int st, int glyph, List<Integer> out) {
		int i = coverage(st + u16(st + 2), glyph);
		if (i < 0 || i >= u16(st + 4)) return;
		int seq = st + u16(st + 6 + 2 * i);
		int n = u16(seq);
		for (int k = 0; k < n; k++) out.add(u16(seq + 2 + 2 * k));
	}

	// --- bytes --------------------------------------------------------------------------------------

	private int table(String tag) throws IOException {
		int[] t = tables.get(tag);
		if (t == null) throw new IOException("no " + tag + " table");
		return t[0];
	}

	private int u16(int at) {
		return ((data[at] & 0xff) << 8) | (data[at + 1] & 0xff);
	}

	private int s16(int at) {
		return (short) u16(at);
	}

	private int u32(int at) {
		return ((data[at] & 0xff) << 24) | ((data[at + 1] & 0xff) << 16) | ((data[at + 2] & 0xff) << 8) | (data[at + 3] & 0xff);
	}
}
