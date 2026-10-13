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
package org.docx4j.chart.render.api;

import java.awt.Color;
import java.awt.Font;
import java.awt.font.FontRenderContext;
import java.awt.font.LineMetrics;
import java.util.Locale;

import org.docx4j.dml.CTTextBody;
import org.docx4j.dml.CTTextCharacterProperties;
import org.docx4j.dml.CTTextParagraph;
import org.docx4j.model.chart.ChartFonts;
import org.docx4j.model.color.DrawingMLColors;
import org.docx4j.model.color.ThemeColors;

/**
 * A text style: a chart's c:txPr, inherited from the chart space's, else Office's default.
 * Text at or above its kern size (DrawingML's {@code kern}, hundredths of a point; Office's
 * charts write 1200) is kerned, as Office kerns it (R4's long title: its width is Carlito's
 * kerned advances, to 0.01pt); an axis's labels never are (r14-kerning: sixteen pairs as 14pt
 * category labels, kern 1200, none kerned).
 *
 * <p>A style measures text in the font docx4j's mapping gives its face ({@link ChartFonts}), as
 * the chart is drawn in it; {@link #with(CTTextCharacterProperties, ThemeColors)} lays a
 * DrawingML run's properties over it (a chartex's {@code cx:txPr} is the same {@code a:txPr}).</p>
 *
 * @since 17.3.3 (CR-039: from docx4j-chart-render's internals, CR-037)
 */
public final class TextStyle {

	private static final FontRenderContext FRC = new FontRenderContext(null, true, true);

	private final ChartFonts fonts;
	private final String typeface;
	private final double size;
	private final boolean bold;
	private final boolean italic;
	private final Color colour;
	private final int kern;
	private final Font font;
	private final double ascent, line;
	/** the space after each character, in points (DrawingML's spc), and capitals (cap="all") */
	private final double spc;
	private final boolean caps;

	public TextStyle(ChartFonts fonts, String typeface, double size, boolean bold, boolean italic, Color colour, int kern) {
		this(fonts, typeface, size, bold, italic, colour, kern, 0, false);
	}

	/**
	 * @param typeface the face the chart names, or a theme's ({@code +mn-lt})
	 * @param kern DrawingML's {@code kern}: the size, in hundredths of a point, from which text is kerned (0: never)
	 * @param spc the space after each character, in points
	 * @param caps in capitals ({@code cap="all"})
	 */
	public TextStyle(ChartFonts fonts, String typeface, double size, boolean bold, boolean italic, Color colour, int kern, double spc, boolean caps) {
		this.fonts = fonts;
		this.spc = spc;
		this.caps = caps;
		this.typeface = typeface;
		this.size = size;
		this.bold = bold;
		this.italic = italic;
		this.colour = colour;
		this.kern = kern;
		Font f = fonts.font(typeface, bold, italic, size);
		if (kern > 0 && size * 100 >= kern) {
			f = f.deriveFont(java.util.Collections.singletonMap(java.awt.font.TextAttribute.KERNING,
					java.awt.font.TextAttribute.KERNING_ON));
		}
		this.font = f;
		LineMetrics lm = font.getLineMetrics("Xg", FRC);
		this.ascent = lm.getAscent();
		this.line = lm.getAscent() + lm.getDescent() + lm.getLeading();
	}

	/** The fonts the style was made with */
	public ChartFonts getFonts() {
		return fonts;
	}

	/** The face the chart names, or a theme's ({@code +mn-lt}) */
	public String getTypeface() {
		return typeface;
	}

	/** Its size, in points */
	public double getSize() {
		return size;
	}

	public boolean isBold() {
		return bold;
	}

	public boolean isItalic() {
		return italic;
	}

	public Color getColour() {
		return colour;
	}

	/** DrawingML's {@code kern}: the size, in hundredths of a point, from which text is kerned (0: never) */
	public int getKern() {
		return kern;
	}

	/** The font it is measured and drawn in: the face docx4j's mapping gives it, kerned where it is */
	public Font getFont() {
		return font;
	}

	/** The font's ascent, in points */
	public double getAscent() {
		return ascent;
	}

	/** A line's height: the font's ascent, descent and leading, in points */
	public double getLineHeight() {
		return line;
	}

	/** The space after each character, in points (DrawingML's spc) */
	public double getSpacing() {
		return spc;
	}

	/** In capitals ({@code cap="all"}) */
	public boolean isCaps() {
		return caps;
	}

	/** A text as shown: in capitals where the style says */
	public String shown(String s) {
		return caps ? s.toUpperCase(Locale.ROOT) : s;
	}

	/**
	 * A text's width: its kerned advances and the space after each character, the last's
	 * included (r10's styles 202, 209 and 258: the titles' boxes to 0.01pt; Office's spacing is
	 * DrawingML's spc after every character)
	 */
	public double width(String s) {
		if (s.isEmpty()) return 0;
		String u = shown(s);
		return font.getStringBounds(u, FRC).getWidth() + u.length() * spc;
	}

	/** This style, never kerned (an axis's labels') */
	public TextStyle unkerned() {
		return kern == 0 ? this : new TextStyle(fonts, typeface, size, bold, italic, colour, 0, spc, caps);
	}

	/** A text body's first paragraph's defRPr, or null */
	public static CTTextCharacterProperties defRPr(CTTextBody tx) {
		if (tx == null || tx.getP().isEmpty() || tx.getP().get(0).getPPr() == null) return null;
		return tx.getP().get(0).getPPr().getDefRPr();
	}

	/** A c:txPr (its first paragraph's defRPr) over this style */
	public TextStyle with(CTTextBody tx, ThemeColors theme) {
		CTTextCharacterProperties p = null;
		if (tx != null && !tx.getP().isEmpty()) {
			CTTextParagraph para = tx.getP().get(0);
			if (para.getPPr() != null) p = para.getPPr().getDefRPr();
		}
		return with(p, theme);
	}

	/** A run's properties over this style, its colour resolved through the theme */
	public TextStyle with(CTTextCharacterProperties p, ThemeColors theme) {
		if (p == null) return this;
		double size = p.getSz() != null ? p.getSz() / 100.0 : this.size;
		boolean bold = p.isB() != null ? p.isB() : this.bold;
		boolean italic = p.isI() != null ? p.isI() : this.italic;
		String face = p.getLatin() != null && p.getLatin().getTypeface() != null ? p.getLatin().getTypeface() : this.typeface;
		Color colour = this.colour;
		if (p.getSolidFill() != null) {
			Color c = Fills.awt(DrawingMLColors.resolve(p.getSolidFill(), theme));
			if (c != null) colour = c;
		}
		int kern = p.getKern() != null ? p.getKern() : this.kern;
		double spc = p.getSpc() != null ? p.getSpc() / 100.0 : this.spc;
		boolean caps = p.getCap() != null ? p.getCap() == org.docx4j.dml.STTextCapsType.ALL : this.caps;
		return new TextStyle(fonts, face, size, bold, italic, colour, kern, spc, caps);
	}

	/**
	 * A line of text in this style, from its baseline's start; width is the style's (spacing
	 * after its last character included)
	 */
	public Drawing.Text text(String t, double x, double baseline, double width, double angle, String role) {
		// (a run ending in spaces is drawn to its last glyph: its spaces' width off, r15-title-runs)
		String drawn = t.replaceAll(" +$", "");
		if (drawn.length() < t.length()) width = width(drawn);
		return new Drawing.Text(shown(t), fonts.face(typeface), font, colour, x, baseline, drawn.isEmpty() ? width : width - spc, angle, role,
				spc);
	}
}
