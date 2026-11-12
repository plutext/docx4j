/*
 *  Copyright 2026, Plutext Pty Ltd.
 *  This file is part of docx4j.
 */
import java.awt.Font;
import java.awt.font.FontRenderContext;
import java.awt.font.GlyphVector;
import java.awt.font.TextAttribute;
import java.awt.geom.FlatteningPathIterator;
import java.awt.geom.PathIterator;
import java.io.File;
import java.util.Collections;

/**
 * CR-083 phase 2 (the Follow Path round): each text's kerned glyph positions and ink, in ems, as docx4j's WordArt layout
 * gets them (java.awt's layoutGlyphVector with TextAttribute.KERNING, the font at 1000 pt).
 *
 * usage: java KernInk.java &lt;font file&gt; &lt;text&gt; ...  prints a line a text: text, TAB, the positions of
 * its glyphs and of its end (n + 1 numbers), TAB, the ink's top and bottom (y down from the baseline: the top negative)
 * and its left and right, from the outlines flattened (an outline's bounds would hold its curves' control points)
 */
public class KernInk {
	public static void main(String[] args) throws Exception {
		Font base = Font.createFont(Font.TRUETYPE_FONT, new File(args[0])).deriveFont(1000f);
		Font kerned = base.deriveFont(Collections.singletonMap(TextAttribute.KERNING, TextAttribute.KERNING_ON));
		FontRenderContext frc = new FontRenderContext(null, true, true);
		for (int i = 1; i < args.length; i++) {
			char[] cs = args[i].toCharArray();
			GlyphVector g = kerned.layoutGlyphVector(frc, cs, 0, cs.length, Font.LAYOUT_LEFT_TO_RIGHT);
			StringBuilder b = new StringBuilder(args[i]).append('\t');
			for (int j = 0; j <= g.getNumGlyphs(); j++) b.append(j == 0 ? "" : " ").append(g.getGlyphPosition(j).getX() / 1000);
			double x0 = Double.POSITIVE_INFINITY, y0 = x0, x1 = Double.NEGATIVE_INFINITY, y1 = x1;
			double[] c = new double[6];
			for (PathIterator it = new FlatteningPathIterator(g.getOutline().getPathIterator(null), 0.01, 16); !it.isDone(); it.next()) {
				if (it.currentSegment(c) == PathIterator.SEG_CLOSE) continue;
				x0 = Math.min(x0, c[0]);
				x1 = Math.max(x1, c[0]);
				y0 = Math.min(y0, c[1]);
				y1 = Math.max(y1, c[1]);
			}
			b.append('\t').append(y0 / 1000).append(' ').append(y1 / 1000).append(' ').append(x0 / 1000).append(' ').append(x1 / 1000);
			System.out.println(b);
		}
	}
}
