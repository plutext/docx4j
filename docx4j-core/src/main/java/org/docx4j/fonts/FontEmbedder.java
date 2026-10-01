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

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.util.Locale;
import java.util.UUID;

import org.docx4j.fonts.fop.fonts.truetype.FontFileReader;
import org.docx4j.fonts.fop.fonts.truetype.OFFontLoader;
import org.docx4j.fonts.fop.fonts.truetype.TTFFile;
import org.docx4j.openpackaging.exceptions.Docx4JException;
import org.docx4j.openpackaging.exceptions.InvalidFormatException;
import org.docx4j.openpackaging.packages.WordprocessingMLPackage;
import org.docx4j.openpackaging.parts.AbstractFontPart;
import org.docx4j.openpackaging.parts.Part;
import org.docx4j.openpackaging.parts.PartName;
import org.docx4j.openpackaging.parts.WordprocessingML.DocumentSettingsPart;
import org.docx4j.openpackaging.parts.WordprocessingML.FontTablePart;
import org.docx4j.openpackaging.parts.WordprocessingML.MainDocumentPart;
import org.docx4j.openpackaging.parts.WordprocessingML.ObfuscatedFontPart;
import org.docx4j.relationships.Relationship;
import org.docx4j.openpackaging.parts.relationships.RelationshipsPart;
import org.docx4j.wml.BooleanDefaultTrue;
import org.docx4j.wml.CTSettings;
import org.docx4j.wml.CTUcharHexNumber;
import org.docx4j.wml.FontFamily;
import org.docx4j.wml.FontPanose;
import org.docx4j.wml.FontPitch;
import org.docx4j.wml.FontRel;
import org.docx4j.wml.FontSig;
import org.docx4j.wml.Fonts;
import org.docx4j.wml.STPitch;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Embeds a TrueType font file in a docx the way Word does (CR-028), so that Word, on a
 * machine that lacks the font, draws the document in it.
 *
 * <pre>
 * FontEmbedder.embed(wordMLPackage, new File("NotoNaskhArabic-Regular.ttf"));         // regular
 * FontEmbedder.embed(wordMLPackage, boldFile, FontEmbedder.Style.BOLD);
 * FontEmbedder.embed(wordMLPackage, file, "Noto Naskh Arabic", FontEmbedder.Style.REGULAR);
 * </pre>
 *
 * <p>What it writes, each measured against Word 365 (CR-028 §5 phase 0, 2026-10-02):</p>
 * <ul>
 * <li>the font, obfuscated as ECMA-376 Part 4 §2.8.1 says, as {@code /word/fonts/fontN.odttf},
 *     related from the font table part with a fresh GUID as the {@code w:fontKey};</li>
 * <li>the {@code w:font} entry of that name, with {@code w:panose1}, {@code w:charset},
 *     {@code w:family}, {@code w:pitch} and {@code w:sig} computed from the font's OS/2 and
 *     post tables as Word computes them for its own entries, and the {@code w:embedRegular}
 *     (or Bold, Italic, BoldItalic) element.  The signature is what makes Word load the
 *     part: an entry without {@code w:sig} is ignored and the text drawn in a substitute.
 *     Which scripts Word will use the font for follows the signature's code page bits, which
 *     are copied from OS/2 as Word copies them;</li>
 * <li>{@code w:embedTrueTypeFonts} in the settings part.</li>
 * </ul>
 *
 * <p><b>The licence is the user's to answer for, not the flag's.</b> The OS/2 {@code fsType}
 * says what the font's vendor permits by default, and this class refuses what it refuses: a
 * restricted-licence font (bit 1) and a bitmap-embedding-only font (bit 9) throw
 * {@link RefusedException}; a preview-and-print font (bit 2) is embedded with a warning
 * that Word will open the document read-only.  The flag is not the licence: a font whose
 * flag permits embedding may still be licensed in a way that forbids distributing it inside
 * a document, and a font whose flag forbids it may have been licensed to you for exactly
 * that.  docx4j never overrides a refusal; it is the caller who knows the licence.</p>
 *
 * <p>Refused as well, because Word does not use them: CFF-flavoured OpenType ({@code OTTO}),
 * variable fonts ({@code fvar} table; untested in Word, so refused until measured), and
 * TrueType collections (extract the face first).  Word prefers an installed font of the
 * same name to the embedded one, so a name clash on the opening machine shows that machine's
 * font.</p>
 *
 * <p>The whole font is embedded.  Subsetting to the characters used (Word's
 * {@code w:saveSubsetFonts}) is CR-028 phase 2.</p>
 *
 * @since 17.3.0
 */
public final class FontEmbedder {

	private static final Logger log = LoggerFactory.getLogger(FontEmbedder.class);

	private FontEmbedder() {}

	/** Which of the four faces of a family the file is. */
	public enum Style { REGULAR, BOLD, ITALIC, BOLD_ITALIC }

	/** Why a font was not embedded. */
	public enum Reason {
		/** Not an sfnt font file at all. */
		NOT_A_FONT,
		/** CFF outlines ({@code OTTO}): Word embeds TrueType outlines only. */
		CFF_OUTLINES,
		/** A variable font ({@code fvar}): untested in Word, refused until measured. */
		VARIABLE_FONT,
		/** A TrueType collection ({@code ttcf}): extract the face to embed first. */
		COLLECTION,
		/** OS/2 fsType bit 1: the vendor permits no embedding. */
		RESTRICTED_LICENSE,
		/** OS/2 fsType bit 9: the vendor permits embedding bitmaps only. */
		BITMAP_EMBEDDING_ONLY
	}

	/** Thrown when a font is not embedded; {@link #getReason()} says why. */
	public static class RefusedException extends Docx4JException {
		private static final long serialVersionUID = 1L;
		private final Reason reason;
		private final String fontName;

		public RefusedException(Reason reason, String fontName, String message) {
			super(message);
			this.reason = reason;
			this.fontName = fontName;
		}

		public Reason getReason() { return reason; }

		/** The font's name as far as it was read, or the file name; may be null. */
		public String getFontName() { return fontName; }
	}

	/** OS/2 fsType bit 1: restricted licence embedding. */
	public static final int FSTYPE_RESTRICTED = 0x0002;
	/** OS/2 fsType bit 2: preview and print embedding (Word opens the document read-only). */
	public static final int FSTYPE_PREVIEW_AND_PRINT = 0x0004;
	/** OS/2 fsType bit 3: editable embedding. */
	public static final int FSTYPE_EDITABLE = 0x0008;
	/** OS/2 fsType bit 8: no subsetting. */
	public static final int FSTYPE_NO_SUBSETTING = 0x0100;
	/** OS/2 fsType bit 9: bitmap embedding only. */
	public static final int FSTYPE_BITMAP_ONLY = 0x0200;

	/**
	 * Embed the file as the family's regular face, under the family name the file declares.
	 * @return the font table entry written or updated
	 */
	public static Fonts.Font embed(WordprocessingMLPackage pkg, File fontFile) throws Docx4JException, IOException {
		return embed(pkg, fontFile, null, Style.REGULAR);
	}

	/** Embed the file as the given face, under the family name the file declares. */
	public static Fonts.Font embed(WordprocessingMLPackage pkg, File fontFile, Style style) throws Docx4JException, IOException {
		return embed(pkg, fontFile, null, style);
	}

	/**
	 * Embed the file as the given face of the named family.
	 * @param fontName the {@code w:font/@w:name} to write it under, which is the name runs
	 *   use in {@code w:rFonts}; null for the family name the file declares (name ID 1)
	 */
	public static Fonts.Font embed(WordprocessingMLPackage pkg, File fontFile, String fontName, Style style)
			throws Docx4JException, IOException {
		byte[] data = Files.readAllBytes(fontFile.toPath());
		return embed(pkg, data, fontName, style, fontFile.getName());
	}

	/** Embed font data already in memory. */
	public static Fonts.Font embed(WordprocessingMLPackage pkg, byte[] fontData, String fontName, Style style)
			throws Docx4JException, IOException {
		return embed(pkg, fontData, fontName, style, null);
	}

	private static Fonts.Font embed(WordprocessingMLPackage pkg, byte[] fontData, String fontName, Style style, String sourceName)
			throws Docx4JException, IOException {
		if (pkg == null) throw new IllegalArgumentException("package is null");
		if (fontData == null || fontData.length < 12) throw new RefusedException(Reason.NOT_A_FONT, sourceName, "not a font file: " + sourceName);
		if (style == null) style = Style.REGULAR;

		FontInfo info = describe(fontData, sourceName);
		info.checkLicence();
		String name = fontName != null ? fontName : info.getFamilyName();
		if (name == null || name.length() == 0) {
			throw new RefusedException(Reason.NOT_A_FONT, sourceName, "the font declares no family name; give one: " + sourceName);
		}

		MainDocumentPart mdp = pkg.getMainDocumentPart();

		// the settings flag
		DocumentSettingsPart dsp = mdp.getDocumentSettingsPart(true);
		CTSettings settings = dsp.getContents();
		if (settings == null) {
			settings = new CTSettings();
			dsp.setContents(settings);
		}
		if (settings.getEmbedTrueTypeFonts() == null) {
			settings.setEmbedTrueTypeFonts(new BooleanDefaultTrue());
		}

		// the font table part, created if the package has none
		FontTablePart ftp = mdp.getFontTablePart();
		if (ftp == null) {
			ftp = new FontTablePart();
			ftp.setJaxbElement(new Fonts());
			mdp.addTargetPart(ftp);
		}
		Fonts fonts = ftp.getContents();
		if (fonts == null) {
			fonts = new Fonts();
			ftp.setContents(fonts);
		}

		// the entry
		Fonts.Font font = null;
		for (Fonts.Font f : fonts.getFont()) {
			if (name.equalsIgnoreCase(f.getName())) {
				font = f;
				break;
			}
		}
		if (font == null) {
			font = new Fonts.Font();
			font.setName(name);
			fonts.getFont().add(font);
		}

		// an existing embedding of this face goes, part and all
		FontRel existing = getEmbed(font, style);
		if (existing != null && ftp.getRelationshipsPart() != null) {
			RelationshipsPart rp = ftp.getRelationshipsPart();
			Relationship old = rp.getRelationshipByID(existing.getId());
			if (old != null) {
				Part oldPart = rp.getPart(old);
				if (oldPart != null) {
					rp.removePart(oldPart.getPartName());
				} else {
					rp.removeRelationship(old);
				}
			}
			setEmbed(font, style, null);
		}

		// the part: a fresh key, the obfuscation, the next free name
		String fontKey = "{" + UUID.randomUUID().toString().toUpperCase(Locale.ROOT) + "}";
		byte[] obfuscated = AbstractFontPart.obfuscate(fontKey, fontData);
		ObfuscatedFontPart part = new ObfuscatedFontPart(nextPartName(pkg));
		part.setBinaryData(obfuscated);
		Relationship rel = ftp.addTargetPart(part);

		FontRel fontRel = new FontRel();
		fontRel.setId(rel.getId());
		fontRel.setFontKey(fontKey);
		setEmbed(font, style, fontRel);

		info.describe(font);

		log.info("Embedded " + name + " (" + style + ", " + fontData.length + " bytes, fsType 0x"
				+ Integer.toHexString(info.getFsType()) + ") as " + part.getPartName().getName());
		return font;
	}

	private static PartName nextPartName(WordprocessingMLPackage pkg) throws InvalidFormatException {
		for (int n = 1; ; n++) {
			PartName candidate = new PartName("/word/fonts/font" + n + ".odttf");
			if (pkg.getParts().get(candidate) == null) {
				return candidate;
			}
		}
	}

	private static FontRel getEmbed(Fonts.Font font, Style style) {
		switch (style) {
			case BOLD: return font.getEmbedBold();
			case ITALIC: return font.getEmbedItalic();
			case BOLD_ITALIC: return font.getEmbedBoldItalic();
			default: return font.getEmbedRegular();
		}
	}

	private static void setEmbed(Fonts.Font font, Style style, FontRel rel) {
		switch (style) {
			case BOLD: font.setEmbedBold(rel); break;
			case ITALIC: font.setEmbedItalic(rel); break;
			case BOLD_ITALIC: font.setEmbedBoldItalic(rel); break;
			default: font.setEmbedRegular(rel); break;
		}
	}

	/**
	 * Read what the font table entry needs from a font file, without embedding it.  Refuses
	 * (as {@link #embed} would) a non-sfnt file, CFF outlines, a variable font and a
	 * collection; the licence flags are read into the result, not enforced here.
	 */
	public static FontInfo describe(File fontFile) throws RefusedException, IOException {
		return describe(Files.readAllBytes(fontFile.toPath()), fontFile.getName());
	}

	/** As {@link #describe(File)}, for font data in memory. */
	public static FontInfo describe(byte[] fontData) throws RefusedException, IOException {
		return describe(fontData, null);
	}

	private static FontInfo describe(byte[] fontData, String sourceName) throws RefusedException, IOException {
		if (fontData == null || fontData.length < 12) throw new RefusedException(Reason.NOT_A_FONT, sourceName, "not a font file: " + sourceName);
		FontFileReader reader = new FontFileReader(new ByteArrayInputStream(fontData));
		String header = OFFontLoader.readHeader(reader);
		if ("OTTO".equals(header)) {
			throw new RefusedException(Reason.CFF_OUTLINES, sourceName,
					"CFF-flavoured OpenType (OTTO) is not embedded: Word uses TrueType outlines only (" + sourceName + ")");
		}
		if ("ttcf".equals(header)) {
			throw new RefusedException(Reason.COLLECTION, sourceName,
					"a TrueType collection is not embedded as a whole: extract the face first (" + sourceName + ")");
		}
		int sfnt = ((fontData[0] & 0xFF) << 24) | ((fontData[1] & 0xFF) << 16) | ((fontData[2] & 0xFF) << 8) | (fontData[3] & 0xFF);
		if (sfnt != 0x00010000 && !"true".equals(header)) {
			throw new RefusedException(Reason.NOT_A_FONT, sourceName, "not a TrueType font file (sfnt version 0x"
					+ Integer.toHexString(sfnt) + "): " + sourceName);
		}
		TTFFile ttf = new TTFFile(false, false);
		try {
			ttf.readFont(reader, header, (String) null);
		} catch (RuntimeException e) {
			throw new RefusedException(Reason.NOT_A_FONT, sourceName, "cannot read " + sourceName + ": " + e.getMessage());
		}
		if (ttf.hasTable("fvar")) {
			throw new RefusedException(Reason.VARIABLE_FONT, ttf.getLegacyFamilyName(),
					"a variable font (fvar) is not embedded: Word's handling of one is unmeasured (" + ttf.getLegacyFamilyName() + ")");
		}
		if (ttf.hasTable("CFF ") && !ttf.hasTable("glyf")) {
			throw new RefusedException(Reason.CFF_OUTLINES, ttf.getLegacyFamilyName(),
					"CFF outlines are not embedded: Word uses TrueType outlines only (" + ttf.getLegacyFamilyName() + ")");
		}
		return new FontInfo(ttf, sourceName);
	}

	/**
	 * The fields of a {@code w:font} entry as computed from a font file, by the rules Word's
	 * own entries show (CR-028 §5 phase 0: Calibri, Aptos, Traditional Arabic, Arabic
	 * Typesetting, Yu Gothic and Noto Naskh Arabic, as Word 365 wrote them).
	 */
	public static final class FontInfo {
		private final String familyName;
		private final String sourceName;
		private final int fsType;
		private final int familyClass;
		private final byte[] panose;
		private final long[] unicodeRanges;
		private final long[] codePageRanges;
		private final boolean fixedPitch;

		FontInfo(TTFFile ttf, String sourceName) {
			this.familyName = ttf.getLegacyFamilyName();
			this.sourceName = sourceName;
			this.fsType = ttf.getFsType() < 0 ? 0 : ttf.getFsType();
			this.familyClass = ttf.getFamilyClass();
			this.panose = ttf.getPanose() == null ? null : ttf.getPanose().getPanoseArray();
			this.unicodeRanges = ttf.getUnicodeRanges();
			this.codePageRanges = ttf.getCodePageRanges();
			this.fixedPitch = ttf.isFixedPitchFont();
		}

		/** For the rule tests: an entry's inputs given directly. */
		FontInfo(String familyName, int fsType, int familyClass, byte[] panose, long[] unicodeRanges, long[] codePageRanges, boolean fixedPitch) {
			this.familyName = familyName;
			this.sourceName = null;
			this.fsType = fsType;
			this.familyClass = familyClass;
			this.panose = panose;
			this.unicodeRanges = unicodeRanges;
			this.codePageRanges = codePageRanges;
			this.fixedPitch = fixedPitch;
		}

		/** name ID 1, the family name Word writes as {@code w:name}. */
		public String getFamilyName() { return familyName; }

		/** OS/2 fsType, whole. */
		public int getFsType() { return fsType; }

		/** Whether fsType bit 8 forbids subsetting (phase 2 reads it). */
		public boolean isSubsettingForbidden() { return (fsType & FSTYPE_NO_SUBSETTING) != 0; }

		/** The ten PANOSE bytes, or null if the font has none. */
		public byte[] getPanose() { return panose == null ? null : panose.clone(); }

		/** {@code w:panose1}'s value, 20 hex digits, or null if the font's PANOSE is all zero. */
		public String getPanoseHex() {
			if (panose == null) return null;
			boolean any = false;
			StringBuilder sb = new StringBuilder(20);
			for (byte b : panose) {
				any |= b != 0;
				sb.append(String.format("%02X", b & 0xFF));
			}
			return any ? sb.toString() : null;
		}

		/** {@code w:sig}'s usb0-3, 8 hex digits each: ulUnicodeRange1-4 as the font declares them. */
		public String[] getUsb() {
			return new String[] { hex8(unicodeRanges[0]), hex8(unicodeRanges[1]), hex8(unicodeRanges[2]), hex8(unicodeRanges[3]) };
		}

		/**
		 * {@code w:sig}'s csb0-1, 8 hex digits each.  csb0 is ulCodePageRange1 without bit 29
		 * (the Macintosh code page: Word drops it, Calibri's 200001FF written as 000001FF,
		 * Aptos's 2000019F as 0000019F); csb1 is 00000000 (the OEM code pages of
		 * ulCodePageRange2, which Word writes as zero for every font measured).
		 */
		public String[] getCsb() {
			return new String[] { hex8(codePageRanges[0] & ~0x20000000L), hex8(0) };
		}

		/**
		 * {@code w:charset}, from the code page bits: a double-byte code page first (932
		 * Shift-JIS 80, 936 GB2312 86, 949 Hangul 81, 950 Big5 88, 1361 Johab 82; Yu Gothic,
		 * whose bits include 1252, gets 80 from Word), then 1252 (00), then the single-byte
		 * code pages in bit order (1250 EE, 1251 CC, 1253 A1, 1254 A2, 1255 B1, 1256 B2,
		 * 1257 BA, 1258 A3, 874 DE), then Symbol (02), else 00.  Word's own value for a font
		 * that claims both 1252 and another code page follows the script it used the font
		 * for in the document (Arabic Typesetting, 1252 and 1256, gets B2); Word accepts
		 * either value for an embedded font (phase 0, e1 against e3).
		 */
		public String getCharset() {
			return charsetFor(codePageRanges[0]);
		}

		/**
		 * {@code w:family}: modern for a fixed-pitch font; else from OS/2 sFamilyClass when
		 * set (classes 1-5 and 7 roman, 8 swiss, 9 decorative, 10 script); else from PANOSE
		 * (family type 2: serif style 11-15 swiss, 2-10 roman, proportion 9 modern; type 3
		 * script; type 4 decorative); else auto.  Word: Calibri (class 8) swiss, Traditional
		 * Arabic (class 0, PANOSE 0202..) roman, Noto Naskh Arabic (020B..) swiss, Arabic
		 * Typesetting (0302..) script.
		 */
		public String getFamily() {
			return familyFor(familyClass, panose, fixedPitch);
		}

		/** {@code w:pitch}: fixed if post.isFixedPitch, else variable. */
		public STPitch getPitch() {
			return fixedPitch ? STPitch.FIXED : STPitch.VARIABLE;
		}

		/** Throws for what fsType forbids; logs what it restricts. */
		public void checkLicence() throws RefusedException {
			String who = familyName != null && familyName.length() > 0 ? familyName : sourceName;
			if ((fsType & FSTYPE_RESTRICTED) != 0) {
				throw new RefusedException(Reason.RESTRICTED_LICENSE, who, who + " is not embedded: its OS/2 fsType (0x"
						+ Integer.toHexString(fsType) + ") says the vendor permits no embedding. "
						+ "docx4j does not override that; if your licence does permit it, embed a copy whose flag says so.");
			}
			if ((fsType & FSTYPE_BITMAP_ONLY) != 0) {
				throw new RefusedException(Reason.BITMAP_EMBEDDING_ONLY, who, who + " is not embedded: its OS/2 fsType (0x"
						+ Integer.toHexString(fsType) + ") permits bitmap embedding only, and a docx embeds outlines.");
			}
			if ((fsType & FSTYPE_PREVIEW_AND_PRINT) != 0 && (fsType & FSTYPE_EDITABLE) == 0) {
				log.warn(who + ": fsType 0x" + Integer.toHexString(fsType)
						+ " permits preview and print embedding only; Word will open the document read-only.");
			}
		}

		/** Writes the computed fields onto the entry (name and embed elements untouched). */
		public void describe(Fonts.Font font) {
			String panoseHex = getPanoseHex();
			if (panoseHex != null) {
				FontPanose p = new FontPanose();
				p.setVal(panose.clone());
				font.setPanose1(p);
			} else {
				font.setPanose1(null);
			}
			CTUcharHexNumber charset = new CTUcharHexNumber();
			charset.setVal(getCharset());
			font.setCharset(charset);
			FontFamily family = new FontFamily();
			family.setVal(getFamily());
			font.setFamily(family);
			FontPitch pitch = new FontPitch();
			pitch.setVal(getPitch());
			font.setPitch(pitch);
			FontSig sig = new FontSig();
			String[] usb = getUsb();
			String[] csb = getCsb();
			sig.setUsb0(usb[0]);
			sig.setUsb1(usb[1]);
			sig.setUsb2(usb[2]);
			sig.setUsb3(usb[3]);
			sig.setCsb0(csb[0]);
			sig.setCsb1(csb[1]);
			font.setSig(sig);
		}

		static String hex8(long v) {
			return String.format("%08X", v & 0xFFFFFFFFL);
		}

		static String charsetFor(long csb0) {
			if ((csb0 & (1L << 17)) != 0) return "80"; // 932 Shift-JIS
			if ((csb0 & (1L << 18)) != 0) return "86"; // 936 GB2312
			if ((csb0 & (1L << 19)) != 0) return "81"; // 949 Hangul (Wansung)
			if ((csb0 & (1L << 20)) != 0) return "88"; // 950 Big5
			if ((csb0 & (1L << 21)) != 0) return "82"; // 1361 Johab
			if ((csb0 & 1L) != 0) return "00";         // 1252 Latin 1
			if ((csb0 & (1L << 1)) != 0) return "EE";  // 1250 Latin 2
			if ((csb0 & (1L << 2)) != 0) return "CC";  // 1251 Cyrillic
			if ((csb0 & (1L << 3)) != 0) return "A1";  // 1253 Greek
			if ((csb0 & (1L << 4)) != 0) return "A2";  // 1254 Turkish
			if ((csb0 & (1L << 5)) != 0) return "B1";  // 1255 Hebrew
			if ((csb0 & (1L << 6)) != 0) return "B2";  // 1256 Arabic
			if ((csb0 & (1L << 7)) != 0) return "BA";  // 1257 Baltic
			if ((csb0 & (1L << 8)) != 0) return "A3";  // 1258 Vietnamese
			if ((csb0 & (1L << 16)) != 0) return "DE"; // 874 Thai
			if ((csb0 & (1L << 31)) != 0) return "02"; // Symbol
			return "00";
		}

		static String familyFor(int familyClass, byte[] panose, boolean fixedPitch) {
			if (fixedPitch) return "modern";
			int cls = (familyClass >> 8) & 0xFF;
			switch (cls) {
				case 1: case 2: case 3: case 4: case 5: case 7: return "roman";
				case 8: return "swiss";
				case 9: return "decorative";
				case 10: return "script";
				default: break;
			}
			if (panose != null && panose.length >= 4) {
				int type = panose[0] & 0xFF;
				if (type == 2) {
					if ((panose[3] & 0xFF) == 9) return "modern";
					int serif = panose[1] & 0xFF;
					if (serif >= 11 && serif <= 15) return "swiss";
					if (serif >= 2 && serif <= 10) return "roman";
					return "auto";
				}
				if (type == 3) return "script";
				if (type == 4) return "decorative";
			}
			return "auto";
		}
	}
}
