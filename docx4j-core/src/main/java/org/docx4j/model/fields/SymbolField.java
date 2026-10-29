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
package org.docx4j.model.fields;

import java.math.BigInteger;
import java.nio.charset.Charset;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.Locale;
import java.util.Set;

import org.docx4j.jaxb.Context;
import org.docx4j.wml.BooleanDefaultTrue;
import org.docx4j.wml.HpsMeasure;
import org.docx4j.wml.R;
import org.docx4j.wml.RFonts;
import org.docx4j.wml.RPr;
import org.docx4j.wml.Text;

/**
 * A SYMBOL field as Word paints it (measured against Word 365, CR-067 section 14.4): the character whose code is the
 * argument (decimal, or {@code 0x} hexadecimal), in the font {@code \f} names at the size {@code \s} gives in points.
 * Without the {@code u} switch the code is a character code of the font, 0 to 255 (Windows-1252 for a text font; for a
 * symbol-encoded font such as Symbol or Wingdings the glyph at that code, which Word writes as {@code w:sym} and paints
 * from the font's private-use range, U+F000 and the code); with the {@code u} switch it is a Unicode code point. A code Word
 * cannot read (none, not a number, over 255 without the {@code u} switch) paints {@link #ERROR} in bold. {@code \a} (ANSI) is
 * the default; {@code \h} (no line-height change) changes nothing visible. Word writes no result for the field; the
 * updater writes this one.
 *
 * @since 17.3.5 (CR-067 phase 3)
 */
public final class SymbolField {

	/** What Word paints for a code it cannot read (bold). */
	public static final String ERROR = "###";

	/** The fonts Word treats as symbol-encoded: their codes are glyphs, kept in the private-use range. */
	public static final Set<String> SYMBOL_FONTS = Collections.unmodifiableSet(new HashSet<String>(Arrays.asList(
			"symbol", "wingdings", "wingdings 2", "wingdings 3", "webdings", "mt extra", "marlett", "bookshelf symbol 7",
			"monotype sorts", "zapf dingbats")));

	private final String text;
	private final String font;
	private final Integer halfPoints;
	private final boolean error;
	private final boolean symbolFont;

	private SymbolField(String text, String font, Integer halfPoints, boolean error, boolean symbolFont) {
		this.text = text;
		this.font = font;
		this.halfPoints = halfPoints;
		this.error = error;
		this.symbolFont = symbolFont;
	}

	/** Whether the font is one Word treats as symbol-encoded. */
	public static boolean isSymbolFont(String font) {
		return font != null && SYMBOL_FONTS.contains(font.trim().toLowerCase(Locale.ROOT));
	}

	/** The field as Word paints it. */
	public static SymbolField of(FieldInstruction instr) {
		String font = instr.getSwitchValue("\\f");
		if (font != null && font.trim().isEmpty()) font = null;
		Integer halfPoints = null;
		String size = instr.getSwitchValue("\\s");
		if (size != null) {
			try {
				halfPoints = Integer.valueOf(Math.round(Float.parseFloat(size.trim()) * 2));
			} catch (NumberFormatException e) {
				// no size
			}
		}
		boolean symbolFont = isSymbolFont(font);
		Integer code = codeOf(instr.getArgument());
		boolean unicode = instr.hasSwitch("\\u");
		if (code == null || code < 0 || (!unicode && code > 255) || (unicode && !Character.isValidCodePoint(code))) {
			return new SymbolField(ERROR, font, halfPoints, true, symbolFont);
		}
		String text;
		if (unicode) {
			text = new String(Character.toChars(code));
		} else if (symbolFont) {
			text = new String(Character.toChars(0xF000 + code));
		} else {
			text = new String(new byte[] { (byte) code.intValue() }, Charset.forName("windows-1252"));
		}
		return new SymbolField(text, font, halfPoints, false, symbolFont);
	}

	private static Integer codeOf(String argument) {
		if (argument == null) return null;
		String s = argument.trim();
		try {
			if (s.regionMatches(true, 0, "0x", 0, 2)) return Integer.valueOf(s.substring(2), 16);
			return Integer.valueOf(s);
		} catch (NumberFormatException e) {
			return null;
		}
	}

	/** The character (a private-use one for a symbol font), or {@link #ERROR}. */
	public String getText() {
		return text;
	}

	/** The font {@code \f} names, or null. */
	public String getFont() {
		return font;
	}

	/** The size {@code \s} gives, in half-points, or null. */
	public Integer getHalfPoints() {
		return halfPoints;
	}

	/** Whether Word paints {@link #ERROR} for the field. */
	public boolean isError() {
		return error;
	}

	/** Whether the font is symbol-encoded (the result is a {@code w:sym}). */
	public boolean isSymbolFont() {
		return symbolFont;
	}

	/**
	 * Writes the field's result into the run as Word would paint it: a {@code w:sym} for a symbol font, else the text;
	 * the font and the size on the run; bold for the error text.
	 */
	public void apply(R run) {
		if (run == null) return;
		run.getContent().clear();
		if (symbolFont && !error) {
			R.Sym sym = Context.getWmlObjectFactory().createRSym();
			sym.setFont(font);
			sym.setChar(String.format(Locale.ROOT, "%04X", text.codePointAt(0)));
			run.getContent().add(sym);
		} else {
			Text t = Context.getWmlObjectFactory().createText();
			t.setValue(text);
			run.getContent().add(Context.getWmlObjectFactory().createRT(t));
		}
		if (font == null && halfPoints == null && !error) return;
		RPr rPr = run.getRPr() == null ? Context.getWmlObjectFactory().createRPr() : run.getRPr();
		if (font != null) {
			RFonts fonts = rPr.getRFonts() == null ? Context.getWmlObjectFactory().createRFonts() : rPr.getRFonts();
			fonts.setAscii(font);
			fonts.setHAnsi(font);
			fonts.setCs(font);
			rPr.setRFonts(fonts);
		}
		if (halfPoints != null) {
			HpsMeasure sz = Context.getWmlObjectFactory().createHpsMeasure();
			sz.setVal(BigInteger.valueOf(halfPoints));
			rPr.setSz(sz);
			HpsMeasure szCs = Context.getWmlObjectFactory().createHpsMeasure();
			szCs.setVal(BigInteger.valueOf(halfPoints));
			rPr.setSzCs(szCs);
		}
		if (error) {
			BooleanDefaultTrue b = Context.getWmlObjectFactory().createBooleanDefaultTrue();
			rPr.setB(b);
		}
		run.setRPr(rPr);
	}
}
