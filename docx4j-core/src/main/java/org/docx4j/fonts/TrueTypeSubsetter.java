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
package org.docx4j.fonts;

import java.io.ByteArrayOutputStream;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.BitSet;
import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.SortedMap;
import java.util.TreeMap;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Subsets a TrueType font to the characters a document uses, in the shape Word's own
 * embedded subsets have (CR-028 §5 phase 0, Word 365's re-save of e11, 2026-10-02):
 * <ul>
 * <li>the glyph count and every glyph id are kept: unused outlines are emptied, nothing is
 *     renumbered, so {@code hmtx}, {@code GSUB}, {@code GPOS}, {@code GDEF}, the hinting
 *     tables and every other per-glyph table stay as they were and the obfuscated part
 *     still shapes;</li>
 * <li>the outlines kept are the used characters' glyphs, everything {@code GSUB} can
 *     substitute for them (so Arabic forms and ligatures survive), and the components of
 *     any composite among them;</li>
 * <li>{@code cmap} is rebuilt for the used characters only (a (3,1) format 4 subtable, and
 *     a (3,10) format 12 one when a character is outside the BMP), so a character the
 *     subset lacks falls back visibly instead of drawing blank;</li>
 * <li>{@code post} becomes format 3 (the glyph names go); {@code DSIG} goes (a signature
 *     of the old bytes); every other table is copied unchanged.</li>
 * </ul>
 *
 * <p>The {@code GSUB} closure applies every lookup in the table, whatever feature or
 * script it belongs to, until nothing is added: a superset of what any shaper would
 * reach, which is the safe side for a document that must still shape.</p>
 *
 * <p>Used by {@link FontEmbedder}; a static {@link #subset(byte[], Collection)} for
 * anyone else.</p>
 *
 * @since 17.3.0
 */
public final class TrueTypeSubsetter {

	private static final Logger log = LoggerFactory.getLogger(TrueTypeSubsetter.class);

	/** The subset, with what it kept. */
	public static final class Subset {
		private final byte[] bytes;
		private final int numGlyphs;
		private final int glyphsKept;
		private final int charactersMapped;

		Subset(byte[] bytes, int numGlyphs, int glyphsKept, int charactersMapped) {
			this.bytes = bytes;
			this.numGlyphs = numGlyphs;
			this.glyphsKept = glyphsKept;
			this.charactersMapped = charactersMapped;
		}

		/** The subset font file. */
		public byte[] getBytes() { return bytes; }
		/** maxp.numGlyphs, unchanged from the input. */
		public int getNumGlyphs() { return numGlyphs; }
		/** Glyphs whose outlines were kept (.notdef included). */
		public int getGlyphsKept() { return glyphsKept; }
		/** Of the code points asked for, how many the font maps. */
		public int getCharactersMapped() { return charactersMapped; }
	}

	private final byte[] font;
	private final Map<String, int[]> tables = new LinkedHashMap<String, int[]>(); // tag -> {offset, length}
	private int numGlyphs;

	private TrueTypeSubsetter(byte[] font) {
		this.font = font;
	}

	/**
	 * Subset the font to these code points.
	 * @param font a TrueType font file (sfnt 0x00010000 or 'true'; not OTTO, not a collection)
	 * @param codePoints the characters to keep
	 * @throws IllegalArgumentException if the file is not a TrueType font with the tables this needs
	 */
	public static Subset subset(byte[] font, Collection<Integer> codePoints) {
		return new TrueTypeSubsetter(font).run(codePoints);
	}

	private Subset run(Collection<Integer> codePoints) {
		readDirectory();
		require("head"); require("maxp"); require("loca"); require("glyf"); require("cmap"); require("hmtx");
		numGlyphs = u16(table("maxp")[0] + 4);
		int indexToLocFormat = (short) u16(table("head")[0] + 50);
		int[] loca = readLoca(indexToLocFormat);

		// the characters' glyphs
		SortedMap<Integer, Integer> cmap = readCmap();
		BitSet keep = new BitSet(numGlyphs);
		keep.set(0);
		SortedMap<Integer, Integer> mapped = new TreeMap<Integer, Integer>();
		for (int cp : codePoints) {
			Integer gid = cmap.get(cp);
			if (gid != null && gid < numGlyphs) {
				keep.set(gid);
				mapped.put(cp, gid);
			}
		}

		// everything GSUB can make of them, then composite components, to a fixed point
		if (tables.containsKey("GSUB")) {
			try {
				gsubClosure(keep);
			} catch (RuntimeException e) {
				log.warn("GSUB closure failed, keeping every glyph GSUB names: " + e.getMessage());
				keep.set(0, numGlyphs);
			}
		}
		compositeClosure(keep, loca);

		// glyf and loca, rebuilt
		int glyfOffset = table("glyf")[0];
		ByteArrayOutputStream glyf = new ByteArrayOutputStream();
		int[] newLoca = new int[numGlyphs + 1];
		for (int g = 0; g < numGlyphs; g++) {
			newLoca[g] = glyf.size();
			if (keep.get(g)) {
				int len = loca[g + 1] - loca[g];
				if (len > 0) {
					glyf.write(font, glyfOffset + loca[g], len);
					while (glyf.size() % 4 != 0) glyf.write(0);
				}
			}
		}
		newLoca[numGlyphs] = glyf.size();
		byte[] locaBytes = writeLoca(newLoca, indexToLocFormat);

		// the new tables, in place of the old
		Map<String, byte[]> out = new TreeMap<String, byte[]>();
		for (Map.Entry<String, int[]> e : tables.entrySet()) {
			String tag = e.getKey();
			if (tag.equals("DSIG")) continue;
			int[] t = e.getValue();
			byte[] data = new byte[t[1]];
			System.arraycopy(font, t[0], data, 0, t[1]);
			out.put(tag, data);
		}
		out.put("glyf", glyf.toByteArray());
		out.put("loca", locaBytes);
		out.put("cmap", writeCmap(mapped));
		if (tables.containsKey("post")) out.put("post", writePost3());

		return new Subset(assemble(out), numGlyphs, keep.cardinality(), mapped.size());
	}

	// ------------------------------------------------------------------ reading

	private void readDirectory() {
		if (font.length < 12) throw new IllegalArgumentException("not a font file");
		int sfnt = s32(0);
		if (sfnt != 0x00010000 && sfnt != 0x74727565) {  // 'true'
			throw new IllegalArgumentException("not a TrueType font (sfnt 0x" + Integer.toHexString(sfnt) + ")");
		}
		int n = u16(4);
		for (int i = 0; i < n; i++) {
			int rec = 12 + 16 * i;
			String tag = new String(font, rec, 4, StandardCharsets.ISO_8859_1);
			int offset = s32(rec + 8);
			int length = s32(rec + 12);
			if (offset < 0 || length < 0 || offset + length > font.length) {
				throw new IllegalArgumentException("table " + tag + " outside the file");
			}
			tables.put(tag, new int[] { offset, length });
		}
	}

	private void require(String tag) {
		if (!tables.containsKey(tag)) throw new IllegalArgumentException("no " + tag + " table");
	}

	private int[] table(String tag) {
		return tables.get(tag);
	}

	private int[] readLoca(int indexToLocFormat) {
		int[] t = table("loca");
		int[] loca = new int[numGlyphs + 1];
		for (int i = 0; i <= numGlyphs; i++) {
			loca[i] = indexToLocFormat == 0 ? u16(t[0] + 2 * i) * 2 : s32(t[0] + 4 * i);
		}
		return loca;
	}

	/** code point -> glyph id, from the best Unicode subtable (format 12, else format 4). */
	private SortedMap<Integer, Integer> readCmap() {
		int[] t = table("cmap");
		int n = u16(t[0] + 2);
		int best4 = -1, best12 = -1;
		for (int i = 0; i < n; i++) {
			int rec = t[0] + 4 + 8 * i;
			int platform = u16(rec), encoding = u16(rec + 2), off = s32(rec + 4);
			int sub = t[0] + off;
			int format = u16(sub);
			boolean unicode = platform == 0 || (platform == 3 && (encoding == 1 || encoding == 10));
			if (!unicode) continue;
			if (format == 12 && (best12 < 0 || platform == 3)) best12 = sub;
			if (format == 4 && (best4 < 0 || platform == 3)) best4 = sub;
		}
		SortedMap<Integer, Integer> map = new TreeMap<Integer, Integer>();
		if (best4 >= 0) readCmap4(best4, map);
		if (best12 >= 0) readCmap12(best12, map);
		if (best4 < 0 && best12 < 0) throw new IllegalArgumentException("no Unicode cmap subtable");
		return map;
	}

	private void readCmap4(int sub, Map<Integer, Integer> map) {
		int segX2 = u16(sub + 6);
		int segs = segX2 / 2;
		int ends = sub + 14, starts = ends + segX2 + 2, deltas = starts + segX2, rangeOffs = deltas + segX2;
		for (int s = 0; s < segs; s++) {
			int end = u16(ends + 2 * s), start = u16(starts + 2 * s), delta = u16(deltas + 2 * s), ro = u16(rangeOffs + 2 * s);
			if (start > end) continue;
			for (int c = start; c <= end && c != 0xFFFF; c++) {
				int gid;
				if (ro == 0) {
					gid = (c + delta) & 0xFFFF;
				} else {
					int addr = rangeOffs + 2 * s + ro + 2 * (c - start);
					if (addr + 1 >= font.length) continue;
					gid = u16(addr);
					if (gid != 0) gid = (gid + delta) & 0xFFFF;
				}
				if (gid != 0) map.put(c, gid);
			}
		}
	}

	private void readCmap12(int sub, Map<Integer, Integer> map) {
		int groups = s32(sub + 12);
		for (int g = 0; g < groups; g++) {
			int rec = sub + 16 + 12 * g;
			int start = s32(rec), end = s32(rec + 4), gid = s32(rec + 8);
			if (start < 0 || end < start || end - start > 0x10FFFF) continue;
			for (int c = start; c <= end; c++) {
				if (gid + (c - start) != 0) map.put(c, gid + (c - start));
			}
		}
	}

	// ------------------------------------------------------------------ closures

	private void compositeClosure(BitSet keep, int[] loca) {
		int glyfOffset = table("glyf")[0];
		boolean changed = true;
		while (changed) {
			changed = false;
			for (int g = keep.nextSetBit(0); g >= 0; g = keep.nextSetBit(g + 1)) {
				int len = loca[g + 1] - loca[g];
				if (len < 10) continue;
				int p = glyfOffset + loca[g];
				if ((short) u16(p) >= 0) continue; // simple glyph
				p += 10;
				while (true) {
					int flags = u16(p), component = u16(p + 2);
					if (component < numGlyphs && !keep.get(component)) {
						keep.set(component);
						changed = true;
					}
					p += 4;
					p += (flags & 0x0001) != 0 ? 4 : 2;      // ARG_1_AND_2_ARE_WORDS
					if ((flags & 0x0008) != 0) p += 2;         // WE_HAVE_A_SCALE
					else if ((flags & 0x0040) != 0) p += 4;    // WE_HAVE_AN_X_AND_Y_SCALE
					else if ((flags & 0x0080) != 0) p += 8;    // WE_HAVE_A_TWO_BY_TWO
					if ((flags & 0x0020) == 0) break;          // MORE_COMPONENTS
				}
			}
		}
	}

	private int gsubBase;
	private int[] lookupOffsets;

	private void gsubClosure(BitSet keep) {
		int[] t = table("GSUB");
		gsubBase = t[0];
		int lookupList = gsubBase + u16(gsubBase + 8);
		int n = u16(lookupList);
		lookupOffsets = new int[n];
		for (int i = 0; i < n; i++) lookupOffsets[i] = lookupList + u16(lookupList + 2 + 2 * i);
		int before;
		do {
			before = keep.cardinality();
			for (int i = 0; i < n; i++) applyLookup(i, keep, 0);
		} while (keep.cardinality() != before);
	}

	private void applyLookup(int index, BitSet keep, int depth) {
		if (index < 0 || index >= lookupOffsets.length || depth > 8) return;
		int lookup = lookupOffsets[index];
		int type = u16(lookup);
		int subCount = u16(lookup + 4);
		for (int s = 0; s < subCount; s++) {
			int sub = lookup + u16(lookup + 6 + 2 * s);
			int subType = type;
			if (subType == 7) { // extension
				subType = u16(sub + 2);
				sub = sub + s32(sub + 4);
			}
			applySubtable(subType, sub, keep, depth);
		}
	}

	private void applySubtable(int type, int sub, BitSet keep, int depth) {
		int format = u16(sub);
		switch (type) {
		case 1: { // single
			int[] coverage = coverage(sub + u16(sub + 2));
			if (format == 1) {
				int delta = (short) u16(sub + 4);
				for (int g : coverage) if (keep.get(g)) setGlyph(keep, (g + delta) & 0xFFFF);
			} else {
				int count = u16(sub + 4);
				for (int i = 0; i < coverage.length && i < count; i++) {
					if (keep.get(coverage[i])) setGlyph(keep, u16(sub + 6 + 2 * i));
				}
			}
			break;
		}
		case 2: // multiple
		case 3: { // alternate
			int[] coverage = coverage(sub + u16(sub + 2));
			int count = u16(sub + 4);
			for (int i = 0; i < coverage.length && i < count; i++) {
				if (!keep.get(coverage[i])) continue;
				int seq = sub + u16(sub + 6 + 2 * i);
				int glyphCount = u16(seq);
				for (int j = 0; j < glyphCount; j++) setGlyph(keep, u16(seq + 2 + 2 * j));
			}
			break;
		}
		case 4: { // ligature
			int[] coverage = coverage(sub + u16(sub + 2));
			int setCount = u16(sub + 4);
			for (int i = 0; i < coverage.length && i < setCount; i++) {
				if (!keep.get(coverage[i])) continue;
				int ligSet = sub + u16(sub + 6 + 2 * i);
				int ligCount = u16(ligSet);
				for (int j = 0; j < ligCount; j++) {
					int lig = ligSet + u16(ligSet + 2 + 2 * j);
					int ligGlyph = u16(lig);
					int compCount = u16(lig + 2);
					boolean all = true;
					for (int k = 1; k < compCount && all; k++) {
						if (!keep.get(u16(lig + 4 + 2 * (k - 1)))) all = false;
					}
					if (all) setGlyph(keep, ligGlyph);
				}
			}
			break;
		}
		case 5: // context
		case 6: { // chaining context
			// conservative: when the input coverage meets the set, the nested lookups are
			// applied to the whole set
			if (!contextTouches(type, sub, format, keep)) break;
			for (int nested : nestedLookups(type, sub, format)) applyLookup(nested, keep, depth + 1);
			break;
		}
		case 8: { // reverse chaining single
			int[] coverage = coverage(sub + u16(sub + 2));
			int p = sub + 4;
			int backCount = u16(p); p += 2 + 2 * backCount;
			int aheadCount = u16(p); p += 2 + 2 * aheadCount;
			int count = u16(p); p += 2;
			for (int i = 0; i < coverage.length && i < count; i++) {
				if (keep.get(coverage[i])) setGlyph(keep, u16(p + 2 * i));
			}
			break;
		}
		default:
			break;
		}
	}

	private boolean contextTouches(int type, int sub, int format, BitSet keep) {
		if (format == 3) {
			int p = sub + 2;
			int inputCount;
			if (type == 6) {
				int backCount = u16(p); p += 2 + 2 * backCount;
				inputCount = u16(p); p += 2;
			} else {
				inputCount = u16(p); p += 4; // glyphCount, then substCount, then the coverages
			}
			for (int i = 0; i < inputCount; i++) {
				if (!covers(sub + u16(p + 2 * i), keep)) return false;
			}
			return true;
		}
		return covers(sub + u16(sub + 2), keep);
	}

	private boolean covers(int coverageOffset, BitSet keep) {
		for (int g : coverage(coverageOffset)) if (keep.get(g)) return true;
		return false;
	}

	/** Every lookup index a context subtable's rules name. */
	private List<Integer> nestedLookups(int type, int sub, int format) {
		List<Integer> out = new ArrayList<Integer>();
		if (format == 3) {
			int p = sub + 2;
			int records;
			if (type == 6) {
				int c = u16(p); p += 2 + 2 * c;
				int inputCount = u16(p); p += 2 + 2 * inputCount;
				c = u16(p); p += 2 + 2 * c;
				records = u16(p); p += 2;
			} else {
				int inputCount = u16(p);
				records = u16(p + 2);
				p += 4 + 2 * inputCount;
			}
			for (int r = 0; r < records; r++) out.add(u16(p + 4 * r + 2));
			return out;
		}
		// formats 1 and 2: rule sets of rules, each ending in substitution records
		int setCountPos = format == 1 ? sub + 4 : (type == 6 ? sub + 10 : sub + 6);
		int setCount = u16(setCountPos);
		for (int i = 0; i < setCount; i++) {
			int setOff = u16(setCountPos + 2 + 2 * i);
			if (setOff == 0) continue;
			int set = sub + setOff;
			int ruleCount = u16(set);
			for (int r = 0; r < ruleCount; r++) {
				int rule = set + u16(set + 2 + 2 * r);
				int p = rule;
				int inputCount;
				if (type == 6) {
					int back = u16(p); p += 2 + 2 * back;
					inputCount = u16(p); p += 2 + 2 * (inputCount - 1);
					int ahead = u16(p); p += 2 + 2 * ahead;
				} else {
					inputCount = u16(p); p += 2;
					int substCount = u16(p); p += 2;
					p += 2 * (inputCount - 1);
					for (int k = 0; k < substCount; k++) out.add(u16(p + 4 * k + 2));
					continue;
				}
				int substCount = u16(p); p += 2;
				for (int k = 0; k < substCount; k++) out.add(u16(p + 4 * k + 2));
			}
		}
		return out;
	}

	private int[] coverage(int off) {
		int format = u16(off);
		if (format == 1) {
			int count = u16(off + 2);
			int[] g = new int[count];
			for (int i = 0; i < count; i++) g[i] = u16(off + 4 + 2 * i);
			return g;
		}
		int ranges = u16(off + 2);
		List<Integer> g = new ArrayList<Integer>();
		for (int r = 0; r < ranges; r++) {
			int start = u16(off + 4 + 6 * r), end = u16(off + 6 + 6 * r);
			for (int x = start; x <= end && x - start < 65536; x++) g.add(x);
		}
		int[] a = new int[g.size()];
		for (int i = 0; i < a.length; i++) a[i] = g.get(i);
		return a;
	}

	private void setGlyph(BitSet keep, int g) {
		if (g >= 0 && g < numGlyphs) keep.set(g);
	}

	// ------------------------------------------------------------------ writing

	private byte[] writeLoca(int[] loca, int indexToLocFormat) {
		ByteArrayOutputStream bos = new ByteArrayOutputStream();
		for (int v : loca) {
			if (indexToLocFormat == 0) {
				put16(bos, v / 2);
			} else {
				put32(bos, v);
			}
		}
		return bos.toByteArray();
	}

	/** A (0,3)+(3,1) format 4 subtable for the BMP characters, and (0,4)+(3,10) format 12 when any is beyond it. */
	private byte[] writeCmap(SortedMap<Integer, Integer> mapped) {
		boolean supplementary = !mapped.isEmpty() && mapped.lastKey() > 0xFFFF;
		byte[] f4 = cmap4(mapped);
		byte[] f12 = supplementary ? cmap12(mapped) : null;
		int records = supplementary ? 4 : 2;
		ByteArrayOutputStream bos = new ByteArrayOutputStream();
		put16(bos, 0);
		put16(bos, records);
		int headerLen = 4 + 8 * records;
		int off4 = headerLen, off12 = headerLen + f4.length;
		put16(bos, 0); put16(bos, 3); put32(bos, off4);
		if (supplementary) { put16(bos, 0); put16(bos, 4); put32(bos, off12); }
		put16(bos, 3); put16(bos, 1); put32(bos, off4);
		if (supplementary) { put16(bos, 3); put16(bos, 10); put32(bos, off12); }
		bos.write(f4, 0, f4.length);
		if (supplementary) bos.write(f12, 0, f12.length);
		return bos.toByteArray();
	}

	private static byte[] cmap4(SortedMap<Integer, Integer> mapped) {
		// segments of consecutive code points; glyph ids through the glyphIdArray (no delta
		// tricks), plus the 0xFFFF terminator
		List<int[]> segs = new ArrayList<int[]>(); // {start, end}
		int start = -1, prev = -1;
		for (int cp : mapped.keySet()) {
			if (cp > 0xFFFE) break;
			if (start < 0) { start = cp; prev = cp; continue; }
			if (cp == prev + 1) { prev = cp; continue; }
			segs.add(new int[] { start, prev });
			start = cp; prev = cp;
		}
		if (start >= 0) segs.add(new int[] { start, prev });
		segs.add(new int[] { 0xFFFF, 0xFFFF });
		int segCount = segs.size();
		int glyphIdCount = 0;
		for (int[] s : segs) if (s[0] != 0xFFFF) glyphIdCount += s[1] - s[0] + 1;
		int length = 16 + 8 * segCount + 2 * glyphIdCount;
		ByteArrayOutputStream bos = new ByteArrayOutputStream();
		put16(bos, 4); put16(bos, length); put16(bos, 0);
		int segX2 = segCount * 2;
		int entrySelector = 31 - Integer.numberOfLeadingZeros(Math.max(1, segCount));
		int searchRange = 2 * (1 << entrySelector);
		put16(bos, segX2); put16(bos, searchRange); put16(bos, entrySelector); put16(bos, segX2 - searchRange);
		for (int[] s : segs) put16(bos, s[1]);
		put16(bos, 0);
		for (int[] s : segs) put16(bos, s[0]);
		for (int[] s : segs) put16(bos, s[0] == 0xFFFF ? 1 : 0); // idDelta
		int idx = 0;
		for (int i = 0; i < segCount; i++) {
			int[] s = segs.get(i);
			if (s[0] == 0xFFFF) { put16(bos, 0); continue; }
			// offset from this idRangeOffset slot to glyphIdArray[idx]
			int slotsAfter = segCount - i;
			put16(bos, 2 * slotsAfter + 2 * idx);
			idx += s[1] - s[0] + 1;
		}
		for (int[] s : segs) {
			if (s[0] == 0xFFFF) continue;
			for (int c = s[0]; c <= s[1]; c++) put16(bos, mapped.get(c));
		}
		return bos.toByteArray();
	}

	private static byte[] cmap12(SortedMap<Integer, Integer> mapped) {
		List<int[]> groups = new ArrayList<int[]>(); // {start, end, startGlyph}
		int[] cur = null;
		for (Map.Entry<Integer, Integer> e : mapped.entrySet()) {
			int cp = e.getKey(), gid = e.getValue();
			if (cur != null && cp == cur[1] + 1 && gid == cur[2] + (cp - cur[0])) {
				cur[1] = cp;
			} else {
				cur = new int[] { cp, cp, gid };
				groups.add(cur);
			}
		}
		ByteArrayOutputStream bos = new ByteArrayOutputStream();
		put16(bos, 12); put16(bos, 0); put32(bos, 16 + 12 * groups.size()); put32(bos, 0); put32(bos, groups.size());
		for (int[] g : groups) { put32(bos, g[0]); put32(bos, g[1]); put32(bos, g[2]); }
		return bos.toByteArray();
	}

	private byte[] writePost3() {
		int[] t = table("post");
		byte[] post = new byte[32];
		System.arraycopy(font, t[0], post, 0, Math.min(32, t[1]));
		post[0] = 0; post[1] = 3; post[2] = 0; post[3] = 0;
		return post;
	}

	private static byte[] assemble(Map<String, byte[]> tables) {
		int n = tables.size();
		int entrySelector = 31 - Integer.numberOfLeadingZeros(Math.max(1, n));
		int searchRange = 16 * (1 << entrySelector);
		ByteArrayOutputStream bos = new ByteArrayOutputStream();
		put32(bos, 0x00010000); put16(bos, n); put16(bos, searchRange); put16(bos, entrySelector); put16(bos, 16 * n - searchRange);
		int offset = 12 + 16 * n;
		int headOffset = -1;
		List<byte[]> datas = new ArrayList<byte[]>();
		for (Map.Entry<String, byte[]> e : tables.entrySet()) {
			byte[] data = e.getValue();
			if (e.getKey().equals("head")) {
				data = data.clone();
				data[8] = 0; data[9] = 0; data[10] = 0; data[11] = 0; // checkSumAdjustment
				headOffset = offset;
			}
			bos.write(e.getKey().getBytes(StandardCharsets.ISO_8859_1), 0, 4);
			put32(bos, (int) checksum(data, 0, data.length));
			put32(bos, offset);
			put32(bos, data.length);
			datas.add(data);
			offset += (data.length + 3) & ~3;
		}
		for (byte[] data : datas) {
			bos.write(data, 0, data.length);
			for (int pad = data.length; pad % 4 != 0; pad++) bos.write(0);
		}
		byte[] out = bos.toByteArray();
		if (headOffset >= 0) {
			long sum = checksum(out, 0, out.length);
			long adjust = (0xB1B0AFBAL - sum) & 0xFFFFFFFFL;
			out[headOffset + 8] = (byte) (adjust >> 24);
			out[headOffset + 9] = (byte) (adjust >> 16);
			out[headOffset + 10] = (byte) (adjust >> 8);
			out[headOffset + 11] = (byte) adjust;
		}
		return out;
	}

	static long checksum(byte[] b, int off, int len) {
		long sum = 0;
		for (int i = 0; i < len; i += 4) {
			long v = 0;
			for (int k = 0; k < 4; k++) {
				v = (v << 8) | (i + k < len ? (b[off + i + k] & 0xFF) : 0);
			}
			sum = (sum + v) & 0xFFFFFFFFL;
		}
		return sum;
	}

	private int u16(int p) {
		return ((font[p] & 0xFF) << 8) | (font[p + 1] & 0xFF);
	}

	private int s32(int p) {
		return ((font[p] & 0xFF) << 24) | ((font[p + 1] & 0xFF) << 16) | ((font[p + 2] & 0xFF) << 8) | (font[p + 3] & 0xFF);
	}

	private static void put16(ByteArrayOutputStream bos, int v) {
		bos.write((v >> 8) & 0xFF);
		bos.write(v & 0xFF);
	}

	private static void put32(ByteArrayOutputStream bos, int v) {
		bos.write((v >> 24) & 0xFF);
		bos.write((v >> 16) & 0xFF);
		bos.write((v >> 8) & 0xFF);
		bos.write(v & 0xFF);
	}
}
