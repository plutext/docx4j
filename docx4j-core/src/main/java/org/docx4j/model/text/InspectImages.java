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

import java.io.ByteArrayInputStream;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.EnumSet;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.docx4j.org.apache.poi.common.usermodel.fonts.FontCharset;
import org.docx4j.org.apache.poi.hemf.record.emf.HemfRecord;
import org.docx4j.org.apache.poi.hemf.record.emf.HemfText;
import org.docx4j.org.apache.poi.hemf.usermodel.HemfPicture;
import org.docx4j.org.apache.poi.hwmf.record.HwmfDraw;
import org.docx4j.org.apache.poi.hwmf.record.HwmfMisc;
import org.docx4j.org.apache.poi.hwmf.record.HwmfRecord;
import org.docx4j.org.apache.poi.hwmf.record.HwmfRecordType;
import org.docx4j.org.apache.poi.hwmf.record.HwmfText;
import org.docx4j.org.apache.poi.hwmf.usermodel.HwmfPicture;
import org.w3c.dom.Element;

/**
 * The text an image part draws as characters (CR-050 D21, decided (c)): an SVG's {@code <text>}
 * elements, and a WMF or EMF metafile's text records. Extraction reads neither (WD section 1,
 * REQ-514), so the inspector reports each as a finding of its part, {@code image.svg_text} or
 * {@code image.metafile_text}, once per part, with the text as its excerpt.
 *
 * <p><b>An SVG:</b> each {@code <text>} element in document order is one line, its characters
 * being every descendant text node's ({@code <tspan>} and {@code <textPath>} included), with white
 * space collapsed as SVG's default {@code xml:space} collapses it (runs of white space to one space,
 * leading and trailing white space dropped); an element left empty by that is left out. Whether the
 * text is painted where its picture shows it (its {@code viewBox}, paint, opacity, {@code display},
 * a {@code <text>} in {@code <defs>} no {@code <use>} draws) is not judged (the limitation
 * {@code image.text}).
 *
 * <p><b>A metafile:</b> each text record in record order is one line: a WMF's {@code META_TEXTOUT}
 * and {@code META_EXTTEXTOUT}, an EMF's {@code EMR_EXTTEXTOUTA} and {@code EMR_EXTTEXTOUTW}, decoded
 * as the record says: a W record as UTF-16, the others in the character set of the font selected
 * into the device context when the record is played (the object table tracked as GDI keeps it: a
 * WMF object takes the lowest free slot, an EMF object the slot its record names; a stock font is
 * ANSI). A record whose text is all white space is left out. Left out, and declared under
 * {@code image.text}: a record drawn with a font of the SYMBOL character set (its bytes are the
 * face's glyph codes, not characters); an {@code ExtTextOut} record carrying glyph indices
 * ({@code ETO_GLYPH_INDEX}); {@code EMR_POLYTEXTOUT} and EMF+ {@code DrawString} records, which
 * the decoder does not implement (an Office-made EMF+ file is usually dual, so its GDI records
 * carry the same text). A metafile the decoder cannot parse is a skipped entry, {@code unreadable}.
 *
 * <p>The decoder is CR-011's, Apache POI's HWMF and HEMF code as docx4j repackages it.
 *
 * @since 17.3.4 (CR-050 phase 3)
 */
final class InspectImages {

	static final String SVG = "http://www.w3.org/2000/svg";

	private InspectImages() {}

	/** True for the content types of the two metafile formats. */
	static boolean isMetafile(String contentType) {
		if (contentType == null) return false;
		String t = contentType.toLowerCase();
		return t.equals("image/x-emf") || t.equals("image/x-wmf") || t.equals("image/emf") || t.equals("image/wmf");
	}

	/** True for an SVG image part's content type. */
	static boolean isSvg(String contentType) {
		return contentType != null && contentType.toLowerCase().equals("image/svg+xml");
	}

	// --- SVG --------------------------------------------------------------------------------------

	/** The lines of an SVG's text, one per {@code <text>} element with characters, in document order. */
	static List<String> svgText(Element root) {
		List<String> out = new ArrayList<String>();
		if (root == null) return out;
		for (Element t : InspectXml.iter(root, SVG, "text")) {
			String s = collapse(t.getTextContent());
			if (!s.isEmpty()) out.add(s);
		}
		return out;
	}

	/** White space collapsed as SVG's default {@code xml:space} does: runs to one space, the ends dropped. */
	static String collapse(String s) {
		StringBuilder b = new StringBuilder();
		boolean space = false;
		for (int i = 0; i < s.length(); i++) {
			char c = s.charAt(i);
			if (c == ' ' || c == '\t' || c == '\n' || c == '\r') {
				space = true;
			} else {
				if (space && b.length() > 0) b.append(' ');
				space = false;
				b.append(c);
			}
		}
		return b.toString();
	}

	// --- metafiles --------------------------------------------------------------------------------

	private static final Charset UTF_16LE = Charset.forName("UTF-16LE");
	private static final Charset ANSI = Charset.forName("windows-1252");
	/** MS-WMF's object records, each of which takes a slot of the object table. */
	private static final EnumSet<HwmfRecordType> WMF_OBJECT_RECORDS = EnumSet.of(HwmfRecordType.createBrushIndirect,
			HwmfRecordType.createFontIndirect, HwmfRecordType.createPalette, HwmfRecordType.createPatternBrush,
			HwmfRecordType.createPenIndirect, HwmfRecordType.createRegion, HwmfRecordType.dibCreatePatternBrush);
	/** MS-EMF's stock fonts (OEM_FIXED_FONT to DEFAULT_GUI_FONT), selected with the high bit set: ANSI. */
	private static final int[] STOCK_FONTS = { 10, 11, 12, 13, 14, 16, 17 };
	/**
	 * A LOGFONT character set to the Windows code page GDI decodes it with (the WD's Pictures rule): a
	 * fixed table, the same in {@code inspect_oracle.py}, so the implementations agree whatever the locale.
	 * DEFAULT and OEM, which GDI resolves by the system locale, are windows-1252 here on purpose. The CJK sets
	 * take Windows' pages (932, 936, 950), supersets of Shift_JIS, GB2312 and Big5: NEC row 13's circled
	 * digits, for one, are in cp932 and not in Shift_JIS. SYMBOL (2) is not here: its records are left out.
	 */
	private static final Map<Integer, Charset> CHARSETS = new HashMap<>();
	static {
		String[][] t = { { "0", "windows-1252" }, { "1", "windows-1252" }, { "255", "windows-1252" }, { "77", "x-MacRoman" },
				{ "128", "windows-31j" }, { "129", "x-windows-949" }, { "130", "x-Johab" }, { "134", "GBK" }, { "136", "x-windows-950" },
				{ "161", "windows-1253" }, { "162", "windows-1254" }, { "163", "windows-1258" }, { "177", "windows-1255" },
				{ "178", "windows-1256" }, { "186", "windows-1257" }, { "204", "windows-1251" }, { "222", "x-windows-874" },
				{ "238", "windows-1250" } };
		for (String[] e : t) CHARSETS.put(Integer.valueOf(e[0]), Charset.forName(e[1]));
	}

	/** The code page of a font's character set, windows-1252 for one the table does not name. */
	private static Charset codePage(FontCharset fc) {
		return fc == null ? ANSI : CHARSETS.getOrDefault(fc.getNativeId(), ANSI);
	}

	/**
	 * The lines of a metafile's text, one per text record with characters, in record order; a WMF's
	 * where the bytes begin with a WMF header (placeable or not), else an EMF's.
	 *
	 * @throws Exception where the decoder cannot parse the file (the caller skips the part as unreadable)
	 */
	static List<String> metafileText(byte[] data) throws Exception {
		return isWmf(data) ? wmfText(data) : emfText(data);
	}

	/** A placeable WMF's key, or a WMF header's type (1 or 2) and its size in words (9). */
	static boolean isWmf(byte[] d) {
		if (d.length < 4) return false;
		if ((d[0] & 0xff) == 0xd7 && (d[1] & 0xff) == 0xcd && (d[2] & 0xff) == 0xc6 && (d[3] & 0xff) == 0x9a) return true;
		return (d[0] == 1 || d[0] == 2) && d[1] == 0 && d[2] == 9 && d[3] == 0;
	}

	static List<String> wmfText(byte[] data) throws Exception {
		List<String> out = new ArrayList<String>();
		HwmfPicture wmf = new HwmfPicture(new ByteArrayInputStream(data));
		// the GDI object table: each created object takes the lowest free slot (HwmfGraphics.addObjectTableEntry)
		List<HwmfRecord> table = new ArrayList<HwmfRecord>();
		Charset charset = ANSI;
		boolean symbol = false;
		for (HwmfRecord r : wmf.getRecords()) {
			if (WMF_OBJECT_RECORDS.contains(r.getWmfRecordType())) {
				int slot = table.indexOf(null);
				if (slot < 0) table.add(r);
				else table.set(slot, r);
			} else if (r instanceof HwmfDraw.WmfSelectObject) {
				int i = ((HwmfDraw.WmfSelectObject) r).getObjectIndex();
				HwmfRecord e = i >= 0 && i < table.size() ? table.get(i) : null;
				if (e instanceof HwmfText.WmfCreateFontIndirect) {
					FontCharset fc = ((HwmfText.WmfCreateFontIndirect) e).getFont().getCharset();
					symbol = fc == FontCharset.SYMBOL;
					charset = codePage(fc);
				}
			} else if (r instanceof HwmfMisc.WmfDeleteObject) {
				int i = ((HwmfMisc.WmfDeleteObject) r).getObjectIndex();
				if (i >= 0 && i < table.size()) table.set(i, null);
			} else if (r instanceof HwmfText.WmfExtTextOut) {
				HwmfText.WmfExtTextOut t = (HwmfText.WmfExtTextOut) r;
				if (!symbol && !t.getOptions().isGlyphIndex()) line(out, t.getText(charset));
			} else if (r instanceof HwmfText.WmfTextOut) {
				if (!symbol) line(out, ((HwmfText.WmfTextOut) r).getText(charset));
			}
		}
		return out;
	}

	static List<String> emfText(byte[] data) throws Exception {
		List<String> out = new ArrayList<String>();
		HemfPicture emf = new HemfPicture(new ByteArrayInputStream(data));
		// the EMF object table: each created object takes the slot its record names; a stock object (the high bit) is ANSI
		Map<Integer, HemfRecord> table = new HashMap<Integer, HemfRecord>();
		Charset charset = ANSI;
		boolean symbol = false;
		for (HemfRecord r : emf.getRecords()) {
			if (r instanceof HemfText.EmfExtCreateFontIndirectW) {
				table.put(((HemfText.EmfExtCreateFontIndirectW) r).getFontIdx(), r);
			} else if (r instanceof HwmfDraw.WmfSelectObject) {
				int i = ((HwmfDraw.WmfSelectObject) r).getObjectIndex();
				if (i < 0) { // a stock object: a stock font is ANSI, another kind selects no font
					for (int f : STOCK_FONTS) if ((i & 0x7fffffff) == f) { symbol = false; charset = ANSI; }
				} else if (table.get(i) instanceof HemfText.EmfExtCreateFontIndirectW) {
					FontCharset fc = ((HemfText.EmfExtCreateFontIndirectW) table.get(i)).getFont().getCharset();
					symbol = fc == FontCharset.SYMBOL;
					charset = codePage(fc);
				}
			} else if (r instanceof HwmfMisc.WmfDeleteObject) {
				table.remove(((HwmfMisc.WmfDeleteObject) r).getObjectIndex());
			} else if (r instanceof HemfText.EmfExtTextOutW) {
				HemfText.EmfExtTextOutW t = (HemfText.EmfExtTextOutW) r;
				if (!t.getOptions().isGlyphIndex()) line(out, t.getText(UTF_16LE));
			} else if (r instanceof HemfText.EmfExtTextOutA) {
				HemfText.EmfExtTextOutA t = (HemfText.EmfExtTextOutA) r;
				if (!symbol && !t.getOptions().isGlyphIndex()) line(out, t.getText(charset));
			}
		}
		return out;
	}

	/** A record's text as a line: trailing NULs (a record's padding, decoded) dropped; all white space is no line. */
	private static void line(List<String> out, String s) {
		if (s == null) return;
		int end = s.length();
		while (end > 0 && s.charAt(end - 1) == 0) end--;
		s = s.substring(0, end);
		for (int i = 0; i < s.length(); i++) {
			if (!Character.isWhitespace(s.charAt(i))) {
				out.add(s);
				return;
			}
		}
	}
}
