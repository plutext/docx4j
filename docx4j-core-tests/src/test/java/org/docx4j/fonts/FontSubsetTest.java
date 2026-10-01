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

import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.math.BigInteger;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.SortedSet;
import java.util.TreeSet;

import org.docx4j.fonts.FontEmbedder.EmbedPolicy;
import org.docx4j.fonts.FontEmbedder.Style;
import org.docx4j.fonts.fop.fonts.truetype.FontFileReader;
import org.docx4j.fonts.fop.fonts.truetype.OFFontLoader;
import org.docx4j.fonts.fop.fonts.truetype.TTFFile;
import org.docx4j.jaxb.Context;
import org.docx4j.openpackaging.packages.WordprocessingMLPackage;
import org.docx4j.openpackaging.parts.PartName;
import org.docx4j.openpackaging.parts.WordprocessingML.MainDocumentPart;
import org.docx4j.openpackaging.parts.WordprocessingML.ObfuscatedFontPart;
import org.docx4j.utils.ResourceUtils;
import org.docx4j.wml.BooleanDefaultTrue;
import org.docx4j.wml.CTLanguage;
import org.docx4j.wml.CTSettings;
import org.docx4j.wml.Fonts;
import org.docx4j.wml.HpsMeasure;
import org.docx4j.wml.ObjectFactory;
import org.docx4j.wml.P;
import org.docx4j.wml.PPr;
import org.docx4j.wml.R;
import org.docx4j.wml.RFonts;
import org.docx4j.wml.RPr;
import org.docx4j.wml.Text;
import org.junit.Test;

/**
 * CR-028 phase 2: {@link TrueTypeSubsetter} cuts a subset in the shape of Word's own
 * embedded subsets (glyph count and ids kept, GSUB closure, cmap pruned, post 3), and
 * {@link FontEmbedder}'s {@link EmbedPolicy} decides whole or subset as Word's
 * {@code w:saveSubsetFonts} and 32-character rule do.
 *
 * <p>The closure's lower bound is fontTools' own for the same text in the same font
 * ({@code pyftsubset --retain-gids --layout-features='*'}, 2026-10-02): 18 glyphs for
 * "Hamburg بسم" in Noto Naskh Arabic, ids 0 14 15 16 19 32 33 34 35 72 73 74 75 76 77
 * 288 323 616.  Ours applies every lookup regardless of feature, so it may keep more,
 * never fewer.</p>
 */
public class FontSubsetTest {

	private static final String NOTO = "fonts/noto-naskh-arabic/NotoNaskhArabic-Regular.ttf";
	private static final String FONT = "Noto Naskh Arabic";
	private static final String ARABIC = "بسم"; // bism
	private static final String TEXT = "Hamburg " + ARABIC;
	private static final int[] FONTTOOLS_CLOSURE = { 0, 14, 15, 16, 19, 32, 33, 34, 35, 72, 73, 74, 75, 76, 77, 288, 323, 616 };

	private static byte[] noto() throws Exception {
		try (InputStream is = ResourceUtils.getResource(NOTO)) {
			ByteArrayOutputStream bos = new ByteArrayOutputStream();
			byte[] buf = new byte[65536];
			int n;
			while ((n = is.read(buf)) > 0) bos.write(buf, 0, n);
			return bos.toByteArray();
		}
	}

	private static SortedSet<Integer> codePoints(String s) {
		SortedSet<Integer> cps = new TreeSet<Integer>();
		s.codePoints().forEach(cps::add);
		return cps;
	}

	// ---------------------------------------------------------------- the subsetter

	@Test
	public void subsetHasWordsShape() throws Exception {
		byte[] font = noto();
		TrueTypeSubsetter.Subset sub = TrueTypeSubsetter.subset(font, codePoints(TEXT));
		byte[] out = sub.getBytes();
		Sfnt in = new Sfnt(font), s = new Sfnt(out);

		// glyph count and ids kept; hmtx and the layout tables untouched
		assertEquals(1415, sub.getNumGlyphs());
		assertEquals(in.u16("maxp", 4), s.u16("maxp", 4));
		assertArrayEquals(in.table("hmtx"), s.table("hmtx"));
		assertArrayEquals(in.table("GSUB"), s.table("GSUB"));
		assertArrayEquals(in.table("GPOS"), s.table("GPOS"));
		assertArrayEquals(in.table("GDEF"), s.table("GDEF"));
		assertArrayEquals(in.table("OS/2"), s.table("OS/2"));
		assertArrayEquals(in.table("name"), s.table("name"));
		assertArrayEquals(in.table("hhea"), s.table("hhea"));
		for (String hint : new String[] { "cvt ", "fpgm", "prep", "gasp" }) assertArrayEquals(hint, in.table(hint), s.table(hint));
		assertNull(s.tables.get("DSIG"));

		// outlines: fontTools' closure at least, nothing absurd, and the rest empty
		int[] loca = s.loca();
		assertEquals(1416, loca.length);
		int nonEmpty = 0;
		for (int g = 0; g < 1415; g++) if (loca[g + 1] > loca[g]) nonEmpty++;
		// kept glyphs include the space, which has no outline in the original either
		assertTrue(nonEmpty + " non-empty of " + sub.getGlyphsKept() + " kept", nonEmpty <= sub.getGlyphsKept() && nonEmpty >= FONTTOOLS_CLOSURE.length - 1);
		for (int g : FONTTOOLS_CLOSURE) assertTrue("glyph " + g + " kept", loca[g + 1] > loca[g]);
		assertTrue("closure of " + nonEmpty + " glyphs is a subset, not the font", nonEmpty < 200);
		// a used glyph's bytes are the original's
		int[] inLoca = in.loca();
		int g = 616; // beh
		byte[] a = Arrays.copyOfRange(in.table("glyf"), inLoca[g], inLoca[g + 1]);
		byte[] b = Arrays.copyOfRange(s.table("glyf"), loca[g], loca[g] + (inLoca[g + 1] - inLoca[g]));
		assertArrayEquals(a, b);
		assertTrue("smaller: " + out.length + " of " + font.length, out.length < font.length / 3);

		// cmap pruned to the characters asked for that the font has
		assertEquals(4, sub.getCharactersMapped()); // space, beh, seen, meem; the Latin letters are not in Noto
		Map<Integer, Integer> cmap = s.cmap4();
		assertEquals(4, cmap.size());
		assertEquals(Integer.valueOf(616), cmap.get(0x0628));
		assertEquals(Integer.valueOf(32), cmap.get(0x0633));
		assertEquals(Integer.valueOf(72), cmap.get(0x0645));
		assertEquals(Integer.valueOf(3), cmap.get(0x0020));
		assertNull(cmap.get((int) 'H'));
		assertNull(cmap.get(0x0627)); // alef, not asked for

		// post format 3
		assertEquals(0x00030000, s.s32("post", 0));
		assertEquals(32, s.table("post").length);

		// checksums: the file sums to the magic, every table to its directory entry
		assertEquals(0xB1B0AFBAL, TrueTypeSubsetter.checksum(out, 0, out.length));
		for (Map.Entry<String, int[]> e : s.tables.entrySet()) {
			if (e.getKey().equals("head")) continue;
			assertEquals(e.getKey(), e.getValue()[2] & 0xFFFFFFFFL, TrueTypeSubsetter.checksum(out, e.getValue()[0], e.getValue()[1]));
		}

		// and docx4j's own loader reads it
		FontFileReader reader = new FontFileReader(new ByteArrayInputStream(out));
		TTFFile ttf = new TTFFile(false, false);
		ttf.readFont(reader, OFFontLoader.readHeader(reader), (String) null);
		assertEquals("Noto Naskh Arabic", ttf.getLegacyFamilyName());
	}

	@Test
	public void subsetOfNothingKeepsNotdefOnly() throws Exception {
		TrueTypeSubsetter.Subset sub = TrueTypeSubsetter.subset(noto(), new TreeSet<Integer>());
		assertEquals(0, sub.getCharactersMapped());
		assertTrue(sub.getGlyphsKept() >= 1);
		Sfnt s = new Sfnt(sub.getBytes());
		assertEquals(0, s.cmap4().size());
	}

	@Test
	public void gsubClosureReachesTheArabicForms() throws Exception {
		// beh alone: its initial, medial and final forms come from GSUB, not the cmap
		TrueTypeSubsetter.Subset one = TrueTypeSubsetter.subset(noto(), codePoints("ب"));
		assertEquals(1, one.getCharactersMapped());
		assertTrue("forms kept: " + one.getGlyphsKept(), one.getGlyphsKept() >= 4);
	}

	// ---------------------------------------------------------------- the policy

	@Test
	public void asWordEmbedsWholeWithoutSaveSubsetFonts() throws Exception {
		byte[] font = noto();
		WordprocessingMLPackage pkg = doc(TEXT);
		Fonts.Font f = FontEmbedder.embed(pkg, font, FONT, Style.REGULAR, EmbedPolicy.AS_WORD);
		assertFalse(subsetted(pkg));
		assertEquals(font.length, part(pkg).getBytes().length);
		assertNull(settings(pkg).getSaveSubsetFonts());
	}

	@Test
	public void asWordSubsetsUnder32WithSaveSubsetFonts() throws Exception {
		byte[] font = noto();
		WordprocessingMLPackage pkg = doc(TEXT);
		settings(pkg).setSaveSubsetFonts(new BooleanDefaultTrue());
		Fonts.Font f = FontEmbedder.embed(pkg, font, FONT, Style.REGULAR, EmbedPolicy.AS_WORD);
		assertTrue(subsetted(pkg));
		assertTrue(part(pkg).getBytes().length < font.length / 3);
		assertNotNull("left as found", settings(pkg).getSaveSubsetFonts());
		// what the walk counted
		assertEquals(codePoints(TEXT), FontEmbedder.codePointsUsed(pkg, FONT, Style.REGULAR));
	}

	@Test
	public void asWordEmbedsWholeAt32OrMore() throws Exception {
		byte[] font = noto();
		// 32 distinct characters: the Arabic letters alef..yeh and digits
		StringBuilder sb = new StringBuilder();
		for (int c = 0x0627; sb.codePointCount(0, sb.length()) < 32; c++) sb.appendCodePoint(c);
		WordprocessingMLPackage pkg = doc(sb.toString());
		settings(pkg).setSaveSubsetFonts(new BooleanDefaultTrue());
		assertEquals(32, FontEmbedder.codePointsUsed(pkg, FONT, Style.REGULAR).size());
		Fonts.Font f = FontEmbedder.embed(pkg, font, FONT, Style.REGULAR, EmbedPolicy.AS_WORD);
		assertFalse(subsetted(pkg));
		assertEquals(font.length, part(pkg).getBytes().length);
	}

	@Test
	public void subsetPolicySetsTheFlagAndSubsetsWhateverTheCount() throws Exception {
		byte[] font = noto();
		StringBuilder sb = new StringBuilder();
		for (int c = 0x0627; c < 0x0627 + 40; c++) sb.appendCodePoint(c);
		WordprocessingMLPackage pkg = doc(sb.toString());
		Fonts.Font f = FontEmbedder.embed(pkg, font, FONT, Style.REGULAR, EmbedPolicy.SUBSET);
		assertTrue(subsetted(pkg));
		assertTrue(settings(pkg).getSaveSubsetFonts().isVal());
		assertTrue(part(pkg).getBytes().length < font.length);
	}

	@Test
	public void wholePolicyClearsTheFlag() throws Exception {
		byte[] font = noto();
		WordprocessingMLPackage pkg = doc(TEXT);
		settings(pkg).setSaveSubsetFonts(new BooleanDefaultTrue());
		Fonts.Font f = FontEmbedder.embed(pkg, font, FONT, Style.REGULAR, EmbedPolicy.WHOLE);
		assertFalse(subsetted(pkg));
		assertNull(settings(pkg).getSaveSubsetFonts());
		assertEquals(font.length, part(pkg).getBytes().length);
	}

	@Test
	public void subsetPolicyWithNoTextEmbedsWhole() throws Exception {
		byte[] font = noto();
		WordprocessingMLPackage pkg = WordprocessingMLPackage.createPackage();
		Fonts.Font f = FontEmbedder.embed(pkg, font, FONT, Style.REGULAR, EmbedPolicy.SUBSET);
		assertFalse(subsetted(pkg));
		assertEquals(font.length, part(pkg).getBytes().length);
	}

	@Test
	public void fsTypeBit8ForbidsSubsettingWhateverThePolicy() throws Exception {
		byte[] font = FontEmbedderTest.withFsType(noto(), FontEmbedder.FSTYPE_NO_SUBSETTING | FontEmbedder.FSTYPE_EDITABLE);
		WordprocessingMLPackage pkg = doc(TEXT);
		Fonts.Font f = FontEmbedder.embed(pkg, font, FONT, Style.REGULAR, EmbedPolicy.SUBSET);
		assertFalse(subsetted(pkg));
		assertEquals(font.length, part(pkg).getBytes().length);
	}

	@Test
	public void subsetRoundTripsAndReadsBack() throws Exception {
		WordprocessingMLPackage pkg = doc(TEXT);
		FontEmbedder.embed(pkg, noto(), FONT, Style.REGULAR, EmbedPolicy.SUBSET);
		byte[] stored = part(pkg).getBytes().clone();
		ByteArrayOutputStream bos = new ByteArrayOutputStream();
		pkg.save(bos);
		WordprocessingMLPackage loaded = WordprocessingMLPackage.load(new ByteArrayInputStream(bos.toByteArray()));
		Fonts.Font back = loaded.getMainDocumentPart().getFontTablePart().getContents().getFont().stream()
				.filter(x -> FONT.equals(x.getName())).findFirst().get();
		assertTrue(subsetted(loaded));
		assertArrayEquals(stored, part(loaded).getBytes());
		loaded.getMainDocumentPart().getFontTablePart().processEmbeddings(new IdentityPlusMapper());
		ObfuscatedFontPart p = (ObfuscatedFontPart) loaded.getMainDocumentPart().getFontTablePart()
				.getRelationshipsPart().getPart(back.getEmbedRegular().getId());
		assertNotNull(p.getF());
		assertTrue(p.getF().length() > 0);
		loaded.getMainDocumentPart().getFontTablePart().deleteEmbeddedFontTempFiles();
	}

	// ---------------------------------------------------------------- helpers

	/** A package with one paragraph of the text in the font (ascii, hAnsi and cs slots). */
	static WordprocessingMLPackage doc(String text) throws Exception {
		WordprocessingMLPackage pkg = WordprocessingMLPackage.createPackage();
		ObjectFactory f = Context.getWmlObjectFactory();
		P p = f.createP();
		PPr ppr = f.createPPr();
		p.setPPr(ppr);
		R r = f.createR();
		RPr rpr = f.createRPr();
		RFonts rf = f.createRFonts();
		rf.setAscii(FONT); rf.setHAnsi(FONT); rf.setCs(FONT);
		rpr.setRFonts(rf);
		HpsMeasure sz = f.createHpsMeasure(); sz.setVal(BigInteger.valueOf(24)); rpr.setSz(sz); rpr.setSzCs(sz);
		CTLanguage lang = f.createCTLanguage(); lang.setBidi("ar-SA"); rpr.setLang(lang);
		r.setRPr(rpr);
		Text t = f.createText(); t.setValue(text); t.setSpace("preserve");
		r.getContent().add(t);
		p.getContent().add(r);
		pkg.getMainDocumentPart().getContent().add(p);
		return pkg;
	}

	/** Whether the font table marks the embedding subsetted.  (FontRel.isSubsetted() answers true for an
	 *  absent attribute, the binding's ST_OnOff default, so the marshalled attribute is what is read; the
	 *  binding writes the plain Boolean as "true", where Word writes "1" - both ST_OnOff.) */
	static boolean subsetted(WordprocessingMLPackage pkg) throws Exception {
		String xml = pkg.getMainDocumentPart().getFontTablePart().getXML();
		return xml.contains("w:subsetted=\"1\"") || xml.contains("w:subsetted=\"true\"");
	}

	static CTSettings settings(WordprocessingMLPackage pkg) throws Exception {
		MainDocumentPart mdp = pkg.getMainDocumentPart();
		return mdp.getDocumentSettingsPart(true).getContents();
	}

	static ObfuscatedFontPart part(WordprocessingMLPackage pkg) throws Exception {
		return (ObfuscatedFontPart) pkg.getParts().get(new PartName("/word/fonts/font1.odttf"));
	}

	/** A minimal sfnt reader for the assertions. */
	static final class Sfnt {
		final byte[] b;
		final Map<String, int[]> tables = new LinkedHashMap<String, int[]>(); // offset, length, checksum

		Sfnt(byte[] b) {
			this.b = b;
			int n = u16(4);
			for (int i = 0; i < n; i++) {
				int rec = 12 + 16 * i;
				tables.put(new String(b, rec, 4, StandardCharsets.ISO_8859_1), new int[] { s32(rec + 8), s32(rec + 12), s32(rec + 4) });
			}
		}

		int u16(int p) { return ((b[p] & 0xFF) << 8) | (b[p + 1] & 0xFF); }
		int s32(int p) { return ((b[p] & 0xFF) << 24) | ((b[p + 1] & 0xFF) << 16) | ((b[p + 2] & 0xFF) << 8) | (b[p + 3] & 0xFF); }
		int u16(String tag, int off) { return u16(tables.get(tag)[0] + off); }
		int s32(String tag, int off) { return s32(tables.get(tag)[0] + off); }

		byte[] table(String tag) {
			int[] t = tables.get(tag);
			assertNotNull(tag, t);
			return Arrays.copyOfRange(b, t[0], t[0] + t[1]);
		}

		int[] loca() {
			int numGlyphs = u16("maxp", 4);
			int fmt = (short) u16("head", 50);
			int base = tables.get("loca")[0];
			int[] loca = new int[numGlyphs + 1];
			for (int i = 0; i <= numGlyphs; i++) loca[i] = fmt == 0 ? u16(base + 2 * i) * 2 : s32(base + 4 * i);
			return loca;
		}

		/** The (3,1) format 4 subtable, read the way a consumer reads it. */
		Map<Integer, Integer> cmap4() {
			int base = tables.get("cmap")[0];
			int n = u16(base + 2);
			int sub = -1;
			for (int i = 0; i < n; i++) {
				int rec = base + 4 + 8 * i;
				if (u16(rec) == 3 && u16(rec + 2) == 1) sub = base + s32(rec + 4);
			}
			assertTrue("(3,1) subtable", sub >= 0);
			assertEquals(4, u16(sub));
			Map<Integer, Integer> map = new LinkedHashMap<Integer, Integer>();
			int segX2 = u16(sub + 6), segs = segX2 / 2;
			int ends = sub + 14, starts = ends + segX2 + 2, deltas = starts + segX2, ros = deltas + segX2;
			for (int s = 0; s < segs; s++) {
				int end = u16(ends + 2 * s), start = u16(starts + 2 * s), delta = u16(deltas + 2 * s), ro = u16(ros + 2 * s);
				for (int c = start; c <= end && c != 0xFFFF; c++) {
					int gid = ro == 0 ? (c + delta) & 0xFFFF : u16(ros + 2 * s + ro + 2 * (c - start));
					if (gid != 0 && ro != 0) gid = (gid + delta) & 0xFFFF;
					if (gid != 0) map.put(c, gid);
				}
			}
			return map;
		}
	}
}
