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
import static org.junit.Assert.fail;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.InputStream;
import java.nio.file.Files;
import java.util.Arrays;

import org.docx4j.anon.Anonymize;
import org.docx4j.anon.AnonymizeResult;
import org.docx4j.fonts.FontEmbedder.FontInfo;
import org.docx4j.fonts.FontEmbedder.Reason;
import org.docx4j.fonts.FontEmbedder.RefusedException;
import org.docx4j.fonts.FontEmbedder.Style;
import org.docx4j.openpackaging.packages.WordprocessingMLPackage;
import org.docx4j.openpackaging.parts.Part;
import org.docx4j.openpackaging.parts.PartName;
import org.docx4j.openpackaging.parts.WordprocessingML.FontTablePart;
import org.docx4j.openpackaging.parts.WordprocessingML.ObfuscatedFontPart;
import org.docx4j.utils.ResourceUtils;
import org.docx4j.wml.Fonts;
import org.docx4j.wml.STPitch;
import org.junit.Test;

/**
 * CR-028 phase 1: {@link FontEmbedder} writes what Word needs before it draws in an
 * embedded font, and what Word writes for its own entries.  The expected values are Word
 * 365's own, from its re-saves of the phase 0 probes (CR-028 §5, 2026-10-02): the Noto
 * Naskh Arabic entry Word wrote when the font was installed, and the OS/2 fields of the
 * Microsoft fonts it embedded, against the entries it gave them.
 *
 * <p>The fixture is Noto Naskh Arabic Regular (SIL Open Font License 1.1, OFL.txt beside
 * it), the font the phase 0 probes embedded.</p>
 */
public class FontEmbedderTest {

	private static final String NOTO = "fonts/noto-naskh-arabic/NotoNaskhArabic-Regular.ttf";

	private static byte[] noto() throws Exception {
		try (InputStream is = ResourceUtils.getResource(NOTO)) {
			ByteArrayOutputStream bos = new ByteArrayOutputStream();
			byte[] buf = new byte[65536];
			int n;
			while ((n = is.read(buf)) > 0) bos.write(buf, 0, n);
			return bos.toByteArray();
		}
	}

	// ---------------------------------------------------------------- the entry, against Word's own

	/** Word's entry for Noto Naskh Arabic, written with the font installed (w0-word-embeds-noto.docx):
	 *  charset B2, family swiss, pitch variable, sig 80002003 82002042 00000008 00000000 / 00000040 00000000. */
	@Test
	public void notoEntryMatchesWords() throws Exception {
		FontInfo info = FontEmbedder.describe(noto());
		assertEquals("Noto Naskh Arabic", info.getFamilyName());
		assertEquals("B2", info.getCharset());
		assertEquals("swiss", info.getFamily());
		assertEquals(STPitch.VARIABLE, info.getPitch());
		assertArrayEquals(new String[] { "80002003", "82002042", "00000008", "00000000" }, info.getUsb());
		assertArrayEquals(new String[] { "00000040", "00000000" }, info.getCsb());
		// Word omitted the panose for this font; docx4j writes the OS/2 value (phase 0: harmless, e4 drew)
		assertEquals("020B0502040504020204", info.getPanoseHex());
		assertEquals(0, info.getFsType());
		assertFalse(info.isSubsettingForbidden());
	}

	/** The rules against the Microsoft fonts Word embedded whole in its re-saves: OS/2 fields
	 *  read from the de-obfuscated parts, entries as Word wrote them. */
	@Test
	public void rulesMatchWordsEntriesForItsOwnFonts() {
		// Calibri: sFamilyClass 0x0800, PANOSE 020F0502020204030204, csb 200001FF/00000000 -> swiss, 00, 000001FF 00000000
		FontInfo calibri = new FontInfo("Calibri", 8, 0x0800, hex("020F0502020204030204"),
				new long[] { 0xE4002EFFL, 0xC000247BL, 9, 0 }, new long[] { 0x200001FFL, 0 }, false);
		assertEquals("swiss", calibri.getFamily());
		assertEquals("00", calibri.getCharset());
		assertArrayEquals(new String[] { "000001FF", "00000000" }, calibri.getCsb());
		assertArrayEquals(new String[] { "E4002EFF", "C000247B", "00000009", "00000000" }, calibri.getUsb());
		assertEquals(STPitch.VARIABLE, calibri.getPitch());

		// Aptos: class 0, PANOSE 020B0004020202020204, csb 2000019F -> swiss, 00, 0000019F
		FontInfo aptos = new FontInfo("Aptos", 8, 0, hex("020B0004020202020204"),
				new long[] { 0x20000287L, 3, 0, 0 }, new long[] { 0x2000019FL, 0 }, false);
		assertEquals("swiss", aptos.getFamily());
		assertEquals("00", aptos.getCharset());
		assertArrayEquals(new String[] { "0000019F", "00000000" }, aptos.getCsb());

		// Traditional Arabic: class 0, PANOSE 02020603050405020304, csb 00000041/20080000 -> roman; csb1 zeroed.
		// Word wrote charset B2 (the script it used the font for); the code-page rule says 00 (1252 claimed)
		FontInfo trad = new FontInfo("Traditional Arabic", 8, 0, hex("02020603050405020304"),
				new long[] { 0x2003, 0x80000000L, 8, 0 }, new long[] { 0x41, 0x20080000L }, false);
		assertEquals("roman", trad.getFamily());
		assertArrayEquals(new String[] { "00000041", "00000000" }, trad.getCsb());
		assertEquals("00", trad.getCharset());

		// Arabic Typesetting: PANOSE 03020402040406030203 -> script
		FontInfo at = new FontInfo("Arabic Typesetting", 8, 0, hex("03020402040406030203"),
				new long[] { 0x80002007L, 0x80000000L, 8, 0 }, new long[] { 0xD3, 0x20080000L }, false);
		assertEquals("script", at.getFamily());
		assertArrayEquals(new String[] { "000000D3", "00000000" }, at.getCsb());

		// Yu Gothic: csb 0002009F (932 Shift-JIS as well as 1252) -> Word's charset 80
		FontInfo yu = new FontInfo("Yu Gothic Light", 8, 0, hex("020B0300000000000000"),
				new long[] { 0xE00002FFL, 0x2AC7FDFFL, 0x16, 0 }, new long[] { 0x0002009FL, 0 }, false);
		assertEquals("80", yu.getCharset());
		assertEquals("swiss", yu.getFamily());

		// a fixed-pitch font is modern whatever its PANOSE (Consolas, Courier New in Word's tables)
		FontInfo mono = new FontInfo("Hack", 0, 0, hex("020B0609030202020204"),
				new long[] { 0xA50006EFL, 0x1000B8FBL, 0x20, 0 }, new long[] { 0x2000019FL, 0xDFD70000L }, true);
		assertEquals("modern", mono.getFamily());
		assertEquals(STPitch.FIXED, mono.getPitch());
		assertArrayEquals(new String[] { "0000019F", "00000000" }, mono.getCsb());

		// no code pages at all: 00
		assertEquals("00", FontInfo.charsetFor(0));
		assertEquals("02", FontInfo.charsetFor(0x80000000L));
	}

	// ---------------------------------------------------------------- the round trip

	@Test
	public void embedSaveLoadExtractsTheSameBytes() throws Exception {
		byte[] font = noto();
		WordprocessingMLPackage pkg = WordprocessingMLPackage.createPackage();
		pkg.getMainDocumentPart().addParagraphOfText("Hello");
		Fonts.Font entry = FontEmbedder.embed(pkg, font, null, Style.REGULAR);
		assertEquals("Noto Naskh Arabic", entry.getName());
		assertNotNull(entry.getEmbedRegular());
		assertTrue(entry.getEmbedRegular().getFontKey().matches("\\{[0-9A-F-]{36}\\}"));
		assertNotNull(entry.getSig());
		assertEquals("B2", entry.getCharset().getVal());
		assertEquals("swiss", entry.getFamily().getVal());
		assertEquals(STPitch.VARIABLE, entry.getPitch().getVal());
		assertNotNull(pkg.getMainDocumentPart().getDocumentSettingsPart().getContents().getEmbedTrueTypeFonts());

		Part part = pkg.getParts().get(new PartName("/word/fonts/font1.odttf"));
		assertTrue(part instanceof ObfuscatedFontPart);
		// obfuscated: the first 32 bytes differ, the rest do not
		byte[] stored = ((ObfuscatedFontPart) part).getBytes();
		assertEquals(font.length, stored.length);
		assertFalse(Arrays.equals(Arrays.copyOf(font, 32), Arrays.copyOf(stored, 32)));
		assertArrayEquals(Arrays.copyOfRange(font, 32, font.length), Arrays.copyOfRange(stored, 32, stored.length));

		ByteArrayOutputStream bos = new ByteArrayOutputStream();
		pkg.save(bos);
		WordprocessingMLPackage loaded = WordprocessingMLPackage.load(new ByteArrayInputStream(bos.toByteArray()));
		FontTablePart ftp = loaded.getMainDocumentPart().getFontTablePart();
		Fonts.Font back = ftp.getContents().getFont().stream().filter(f -> "Noto Naskh Arabic".equals(f.getName())).findFirst().get();
		assertEquals(entry.getEmbedRegular().getFontKey(), back.getEmbedRegular().getFontKey());
		assertEquals("80002003", back.getSig().getUsb0());
		assertEquals("00000040", back.getSig().getCsb0());
		assertEquals("00000000", back.getSig().getCsb1());
		assertEquals("020B0502040504020204", hexOf(back.getPanose1().getVal()));

		// the read path (what PDF output uses) gives the file back byte for byte
		ftp.processEmbeddings(new IdentityPlusMapper());
		ObfuscatedFontPart loadedPart = (ObfuscatedFontPart) ftp.getRelationshipsPart().getPart(back.getEmbedRegular().getId());
		File extracted = loadedPart.getF();
		assertNotNull(extracted);
		assertArrayEquals(font, Files.readAllBytes(extracted.toPath()));
		ftp.deleteEmbeddedFontTempFiles();
	}

	@Test
	public void secondEmbedOfTheSameFaceReplacesThePart() throws Exception {
		byte[] font = noto();
		WordprocessingMLPackage pkg = WordprocessingMLPackage.createPackage();
		Fonts.Font first = FontEmbedder.embed(pkg, font, "Probe Naskh", Style.REGULAR);
		String key1 = first.getEmbedRegular().getFontKey();
		byte[] stored1 = ((ObfuscatedFontPart) pkg.getParts().get(new PartName("/word/fonts/font1.odttf"))).getBytes().clone();
		Fonts.Font again = FontEmbedder.embed(pkg, font, "Probe Naskh", Style.REGULAR);
		assertTrue(first == again);
		assertFalse(key1.equals(again.getEmbedRegular().getFontKey()));
		// the old part went, and the new one took the first free name: one part, one relationship
		assertEquals(1, pkg.getParts().getParts().keySet().stream().filter(n -> n.getName().startsWith("/word/fonts/")).count());
		assertEquals(1, pkg.getMainDocumentPart().getFontTablePart().getRelationshipsPart().size());
		ObfuscatedFontPart part2 = (ObfuscatedFontPart) pkg.getMainDocumentPart().getFontTablePart()
				.getRelationshipsPart().getPart(again.getEmbedRegular().getId());
		assertNotNull(part2);
		assertFalse("obfuscated with the new key", Arrays.equals(Arrays.copyOf(stored1, 32), Arrays.copyOf(part2.getBytes(), 32)));
		// one entry of that name
		long n = pkg.getMainDocumentPart().getFontTablePart().getContents().getFont().stream()
				.filter(f -> "Probe Naskh".equals(f.getName())).count();
		assertEquals(1, n);

		// a second face of the same family is a second part
		FontEmbedder.embed(pkg, font, "Probe Naskh", Style.BOLD);
		assertNotNull(again.getEmbedBold());
		assertNotNull(again.getEmbedRegular());
		assertEquals(2, pkg.getMainDocumentPart().getFontTablePart().getRelationshipsPart().size());
	}

	@Test
	public void anonymiserRemovesWhatEmbedderAdds() throws Exception {
		WordprocessingMLPackage pkg = WordprocessingMLPackage.createPackage();
		pkg.getMainDocumentPart().addParagraphOfText("Hello");
		FontEmbedder.embed(pkg, noto(), null, Style.REGULAR);
		assertNotNull(pkg.getParts().get(new PartName("/word/fonts/font1.odttf")));
		AnonymizeResult r = new Anonymize(pkg).go();
		assertTrue(r.summary(), r.isClean());
		assertNull(pkg.getParts().get(new PartName("/word/fonts/font1.odttf")));
		String fontTable = pkg.getMainDocumentPart().getFontTablePart().getXML();
		assertFalse(fontTable, fontTable.contains("embedRegular"));
	}

	// ---------------------------------------------------------------- refusals

	@Test
	public void restrictedLicenceRefuses() throws Exception {
		byte[] font = withFsType(noto(), FontEmbedder.FSTYPE_RESTRICTED);
		assertEquals(FontEmbedder.FSTYPE_RESTRICTED, FontEmbedder.describe(font).getFsType());
		assertRefused(font, Reason.RESTRICTED_LICENSE);
	}

	@Test
	public void bitmapOnlyRefuses() throws Exception {
		assertRefused(withFsType(noto(), FontEmbedder.FSTYPE_BITMAP_ONLY | FontEmbedder.FSTYPE_EDITABLE), Reason.BITMAP_EMBEDDING_ONLY);
	}

	@Test
	public void previewAndPrintEmbedsWithAWarning() throws Exception {
		WordprocessingMLPackage pkg = WordprocessingMLPackage.createPackage();
		Fonts.Font f = FontEmbedder.embed(pkg, withFsType(noto(), FontEmbedder.FSTYPE_PREVIEW_AND_PRINT), null, Style.REGULAR);
		assertNotNull(f.getEmbedRegular());
	}

	@Test
	public void noSubsettingBitIsReadNotEnforced() throws Exception {
		byte[] font = withFsType(noto(), FontEmbedder.FSTYPE_NO_SUBSETTING | FontEmbedder.FSTYPE_EDITABLE);
		assertTrue(FontEmbedder.describe(font).isSubsettingForbidden());
		WordprocessingMLPackage pkg = WordprocessingMLPackage.createPackage();
		assertNotNull(FontEmbedder.embed(pkg, font, null, Style.REGULAR).getEmbedRegular());
	}

	@Test
	public void cffRefuses() throws Exception {
		byte[] otto = noto();
		otto[0] = 'O'; otto[1] = 'T'; otto[2] = 'T'; otto[3] = 'O';
		assertRefused(otto, Reason.CFF_OUTLINES);
	}

	@Test
	public void collectionRefuses() throws Exception {
		byte[] ttc = noto();
		ttc[0] = 't'; ttc[1] = 't'; ttc[2] = 'c'; ttc[3] = 'f';
		assertRefused(ttc, Reason.COLLECTION);
	}

	@Test
	public void variableFontRefuses() throws Exception {
		assertRefused(withTableRenamed(noto(), "GPOS", "fvar"), Reason.VARIABLE_FONT);
	}

	@Test
	public void junkRefuses() throws Exception {
		assertRefused("not a font at all, just bytes".getBytes("US-ASCII"), Reason.NOT_A_FONT);
	}

	private static void assertRefused(byte[] font, Reason reason) throws Exception {
		WordprocessingMLPackage pkg = WordprocessingMLPackage.createPackage();
		try {
			FontEmbedder.embed(pkg, font, null, Style.REGULAR);
			fail("expected " + reason);
		} catch (RefusedException e) {
			assertEquals(e.getMessage(), reason, e.getReason());
		}
		assertNull("nothing was added", pkg.getParts().get(new PartName("/word/fonts/font1.odttf")));
		assertNull(pkg.getMainDocumentPart().getFontTablePart());
	}

	// ---------------------------------------------------------------- sfnt helpers

	/** A copy of the font with OS/2 fsType set. */
	static byte[] withFsType(byte[] font, int fsType) {
		byte[] copy = font.clone();
		int os2 = tableOffset(copy, "OS/2");
		copy[os2 + 8] = (byte) (fsType >> 8);
		copy[os2 + 9] = (byte) fsType;
		return copy;
	}

	/** A copy of the font with one table-directory tag renamed (the data is untouched). */
	static byte[] withTableRenamed(byte[] font, String from, String to) {
		byte[] copy = font.clone();
		int n = ((copy[4] & 0xFF) << 8) | (copy[5] & 0xFF);
		for (int i = 0; i < n; i++) {
			int rec = 12 + 16 * i;
			if (new String(copy, rec, 4, java.nio.charset.StandardCharsets.ISO_8859_1).equals(from)) {
				System.arraycopy(to.getBytes(java.nio.charset.StandardCharsets.ISO_8859_1), 0, copy, rec, 4);
				return copy;
			}
		}
		throw new IllegalArgumentException("no table " + from);
	}

	static int tableOffset(byte[] font, String tag) {
		int n = ((font[4] & 0xFF) << 8) | (font[5] & 0xFF);
		for (int i = 0; i < n; i++) {
			int rec = 12 + 16 * i;
			if (new String(font, rec, 4, java.nio.charset.StandardCharsets.ISO_8859_1).equals(tag)) {
				return ((font[rec + 8] & 0xFF) << 24) | ((font[rec + 9] & 0xFF) << 16) | ((font[rec + 10] & 0xFF) << 8) | (font[rec + 11] & 0xFF);
			}
		}
		throw new IllegalArgumentException("no table " + tag);
	}

	static byte[] hex(String s) {
		byte[] b = new byte[s.length() / 2];
		for (int i = 0; i < b.length; i++) b[i] = (byte) Integer.parseInt(s.substring(2 * i, 2 * i + 2), 16);
		return b;
	}

	static String hexOf(byte[] b) {
		StringBuilder sb = new StringBuilder();
		for (byte x : b) sb.append(String.format("%02X", x & 0xFF));
		return sb.toString();
	}
}
