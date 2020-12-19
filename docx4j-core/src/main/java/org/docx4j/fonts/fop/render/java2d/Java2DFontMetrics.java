/* NOTICE: This file has been changed by Plutext Pty Ltd for use in docx4j.
 * The package name has been changed; there may also be other changes.
 * 
 * This notice is included to meet the condition in clause 4(b) of the License. 
 */
/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

/* $Id$ */

package org.docx4j.fonts.fop.render.java2d;

// Java
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.font.LineMetrics;
import java.awt.font.TextAttribute;
import java.awt.font.TextLayout;
import java.awt.geom.Rectangle2D;
import java.awt.image.BufferedImage;
import java.util.Map;

/**
 * This is a FontMetrics to be used  for AWT  rendering.
 * It  instanciates a font, depening on family and style
 * values. The java.awt.FontMetrics for this font is then
 * created to be used for the actual measurement.
 * Since layout is word by word and since it is expected that
 * two subsequent words often share the same style, the
 * Font and FontMetrics is buffered and only changed if needed.
 * <p>
 * Since FontState and FontInfo multiply all factors by
 * size, we assume a "standard" font of FONT_SIZE.
 */
public class Java2DFontMetrics {

    /**
     * Font size standard used for metric measurements
     */
    public static final int FONT_SIZE = 1;

    /**
     * This factor multiplies the calculated values to scale
     * to FOP internal measurements
     */
    public static final int FONT_FACTOR = (1000 * 1000) / FONT_SIZE;

    /**
     * The width of all 256 character, if requested
     */
    private int[] width;

    /**
     * The typical height of a small cap latter (often derived from "x", value in mpt)
     */
    private int xHeight;

    /**
     * The highest point of the font above the baseline (usually derived from "d", value in mpt)
     */
    private int ascender;

    /**
     * The lowest point of the font under the baseline (usually derived from "p", value in mpt)
     */
    private int descender;

    /**
     * Buffered font.
     * f1 is bufferd for metric measurements during layout.
     * fSized is buffered for display purposes
     */
    private Font f1;    // , fSized;

    /**
     * The family type of the font last used
     */
    private String family = "";

    /**
     * The style of the font last used
     */
    private int style;

    /**
     * The size of the font last used
     */
    private float size;

    /**
     * The FontMetrics object used to calculate character width etc.
     */
    private FontMetrics fmt;

    /** A LineMetrics to access high-resolution metrics information. */
    private LineMetrics lineMetrics;

    /**
     * Temp graphics object needed to get the font metrics
     */
    private final Graphics2D graphics;

    /**
     * Creates a Graphics2D object for the sole purpose of getting font metrics.
     * @return a Graphics2D object
     */
    private static Graphics2D createFontMetricsGraphics2D() {
        BufferedImage fontImage = new BufferedImage(100, 100,
                BufferedImage.TYPE_INT_RGB);
        Graphics2D graphics2D = fontImage.createGraphics();
        //The next line is important to get accurate font metrics!
        graphics2D.setRenderingHint(RenderingHints.KEY_FRACTIONALMETRICS,
                RenderingHints.VALUE_FRACTIONALMETRICS_ON);
        return graphics2D;
    }

    /**
     * Constructs a new Font-metrics.
     */
    public Java2DFontMetrics() {
        this.graphics = createFontMetricsGraphics2D();
    }

    /**
     * Determines the font's maximum ascent of the Font described by the current
     * FontMetrics object
     * @param family font family (java name) to use
     * @param style font style (java def.) to use
     * @param size font size
     * @return ascent in milliponts
     */
    public int getMaxAscent(String family, int style, int size) {
        setFont(family, style, size);
        return Math.round(lineMetrics.getAscent() * FONT_FACTOR);
    }

    /**
     * Determines the font ascent of the Font described by this
     * FontMetrics object
     * @param family font family (java name) to use
     * @param style font style (java def.) to use
     * @param size font size
     * @return ascent in milliponts
     */
    public int getAscender(String family, int style, int size) {
        setFont(family, style, size);
        return ascender * 1000;

        // workaround for sun bug on FontMetrics.getAscent()
        // http://developer.java.sun.com/developer/bugParade/bugs/4399887.html
        /*
         * Bug 4399887 has status Closed, not a bug.  The comments on the bug
         * are:
         * The submitter is incorrectly assuming that the string he has used
         * is displaying characters which represent those with the maximum
         * ascent in the font. If (for example) the unicode character
         * \u00c1 which is the A-acute character used in many European
         * languages is placed in the bodu of the "Wrong" string it can be
         * seen that the JDK is exactly correct in its determination of the
         * ascent of the font.
         * If the bounds of a particular string are interesting then the
         * Rectangle FontMetrics.getStringBounds(..) method can be called.
         * The y value of the rectangle is the offset from the origin
         * (baseline) apparently needed by the sample test program
         *
         * xxxxx@xxxxx 2001-05-15
         */
        /* I don't think this is right.
        int realAscent = fmt.getAscent()
                         - (fmt.getDescent() + fmt.getLeading());
        return FONT_FACTOR * realAscent;
        */
    }


    /**
     * The size of a capital letter measured from the font's baseline
     * @param family font family
     * @param style font style
     * @param size font size
     * @return capital height in millipoints
     */
    public int getCapHeight(String family, int style, int size) {
        // currently just gets Ascent value but maybe should use
        // getMaxAcent() at some stage
        return getAscender(family, style, size);
    }

    /**
     * Determines the font descent of the Font described by this
     * FontMetrics object
     * @param family font family (jave name) to use
     * @param style font style (jave def.) to use
     * @param size font size
     * @return descent in milliponts
     */
    public int getDescender(String family, int style, int size) {
        setFont(family, style, size);
        return descender * 1000;
    }

    /**
     * Determines the typical font height of a small cap letter
     * FontMetrics object
     * @param family font family (jave name) to use
     * @param style font style (jave def.) to use
     * @param size font size
     * @return font height in milliponts
     */
    public int getXHeight(String family, int style, int size) {
        setFont(family, style, size);
        return xHeight * 1000;
    }

    public int getUnderlinePosition(String family, int style, int size) {
        setFont(family, style, size);
        return -Math.round(lineMetrics.getUnderlineOffset());
    }

    public int getUnderlineThickness(String family, int style, int size) {
        setFont(family, style, size);
        return Math.round(lineMetrics.getUnderlineThickness());
    }

    public int getStrikeoutPosition(String family, int style, int size) {
        setFont(family, style, size);
        return -Math.round(lineMetrics.getStrikethroughOffset());
    }

    public int getStrikeoutThickness(String family, int style, int size) {
        setFont(family, style, size);
        return Math.round(lineMetrics.getStrikethroughThickness());
    }

    /**
     * Returns width (in 1/1000ths of point size) of character at
     * code point i
     * @param  i the character for which to get the width
     * @param family font family (jave name) to use
     * @param style font style (jave def.) to use
     * @param size font size
     * @return character width in millipoints
     */
    public int width(int i, String family, int style, int size) {
        int w;
        setFont(family, style, size);
        w = internalCharWidth(i) * 1000;
        return w;
    }

    private int internalCharWidth(int i) {
        //w = (int)(fmt.charWidth(i) * 1000); //Not accurate enough!
        char[] ch = {(char)i};
        Rectangle2D rect = fmt.getStringBounds(ch, 0, 1, this.graphics);
        return (int)Math.round(rect.getWidth() * 1000);
    }

    /**
     * Return widths (in 1/1000ths of point size) of all
     * characters
     * @param family font family (jave name) to use
     * @param style font style (jave def.) to use
     * @param size font size
     * @return array of character widths in millipoints
     */
    public int[] getWidths(String family, int style, int size) {
        int i;

        if (width == null) {
            width = new int[256];
        }
        setFont(family, style, size);
        for (i = 0; i < 256; i++) {
            width[i] = 1000 * internalCharWidth(i);
        }
        return width;
    }

    private Font getBaseFont(String family, int style, float size) {
        Map atts = new java.util.HashMap();
        atts.put(TextAttribute.FAMILY, family);
        if ((style & Font.BOLD) != 0) {
            atts.put(TextAttribute.WEIGHT, TextAttribute.WEIGHT_BOLD);
        }
        if ((style & Font.ITALIC) != 0) {
            atts.put(TextAttribute.POSTURE, TextAttribute.POSTURE_OBLIQUE);
        }
        atts.put(TextAttribute.SIZE, size); //size in pt
        return new Font(atts);
    }

    /**
     * Checks whether the font  for which values are
     * requested is the one used immediately before or
     * whether it is a new one
     * @param family font family (jave name) to use
     * @param style font style (jave def.) to use
     * @param size font size
     * @return true if the font was changed, false otherwise
     */
    private boolean setFont(String family, int style, int size) {
        boolean changed = false;
        float s = size / 1000f;

        if (f1 == null) {
            f1 = getBaseFont(family, style, s);
            fmt = graphics.getFontMetrics(f1);
            changed = true;
        } else {
            if ((this.style != style) || !this.family.equals(family)
                    || this.size != s) {
                if (family.equals(this.family)) {
                    f1 = f1.deriveFont(style, s);
                } else {
                    f1 = getBaseFont(family, style, s);
                }
                fmt = graphics.getFontMetrics(f1);
                changed = true;
            }
            // else the font is unchanged from last time
        }
        if (changed) {
            //x-Height
            TextLayout layout = new TextLayout("x", f1, graphics.getFontRenderContext());
            Rectangle2D rect = layout.getBounds();
            xHeight = (int)Math.round(-rect.getY() * 1000);

            //PostScript-compatible ascent
            layout = new TextLayout("d", f1, graphics.getFontRenderContext());
            rect = layout.getBounds();
            ascender = (int)Math.round(-rect.getY() * 1000);

            //PostScript-compatible descent
            layout = new TextLayout("p", f1, graphics.getFontRenderContext());
            rect = layout.getBounds();
            descender = (int)Math.round((rect.getY() + rect.getHeight()) * -1000);

            //Alternative way to get metrics but the ascender is again wrong for our purposes
            lineMetrics = f1.getLineMetrics("", graphics.getFontRenderContext());
        }
        // save the family and style for later comparison
        this.family = family;
        this.style = style;
        this.size = s;
        return changed;
    }


    /**
     * Returns a java.awt.Font instance for the desired
     * family, style and size type.
     * This is here, so that the font-mapping
     * of FOP-defined fonts to java-fonts can be done
     * in one place and does not need to occur in
     * AWTFontRenderer.
     * @param family font family (jave name) to use
     * @param style font style (jave def.) to use
     * @param size font size
     * @return font with the desired characeristics.
     */
    public java.awt.Font getFont(String family, int style, int size) {
        setFont(family, style, size);
        return f1;
        /*
         * if( setFont(family,style, size) ) fSized = null;
         * if( fSized == null ||  this.size != size ) {
         * fSized = f1.deriveFont( size / 1000f );
         * }
         * this.size = size;
         * return fSized;
         */
    }

    /**
     * Indicates whether the font contains a particular character/glyph.
     * @param family font family (jave name) to use
     * @param style font style (jave def.) to use
     * @param size font size
     * @param c the glyph to check
     * @return true if the character is supported
     */
    public boolean hasChar(String family, int style, int size, char c) {
        setFont(family, style, size);
        return f1.canDisplay(c);
    }

}






