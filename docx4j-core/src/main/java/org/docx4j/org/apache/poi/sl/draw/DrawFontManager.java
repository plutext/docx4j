/* NOTICE: This file has been changed by Plutext Pty Ltd for use in docx4j.
 * The package name has been changed; there may also be other changes.
 *
 * This notice is included to meet the condition in clause 4(b) of the License.
 */

/*
 *  ====================================================================
 *    Licensed to the Apache Software Foundation (ASF) under one or more
 *    contributor license agreements.  See the NOTICE file distributed with
 *    this work for additional information regarding copyright ownership.
 *    The ASF licenses this file to You under the Apache License, Version 2.0
 *    (the "License"); you may not use this file except in compliance with
 *    the License.  You may obtain a copy of the License at
 *
 *        http://www.apache.org/licenses/LICENSE-2.0
 *
 *    Unless required by applicable law or agreed to in writing, software
 *    distributed under the License is distributed on an "AS IS" BASIS,
 *    WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *    See the License for the specific language governing permissions and
 *    limitations under the License.
 * ====================================================================
 */

package org.docx4j.org.apache.poi.sl.draw;

import java.awt.Font;
import java.awt.Graphics2D;

import org.docx4j.org.apache.poi.common.usermodel.fonts.FontInfo;
import org.docx4j.org.apache.poi.util.StringUtil;

/**
 * Manages fonts when rendering slides.
 *
 * Use this class to handle unknown / missing fonts or to substitute fonts
 */
public interface DrawFontManager {

    /**
     * select a font to be used to paint text
     *
     * @param graphics the graphics context to request additional rendering hints
     * @param fontInfo the font info object corresponding to the text run font
     *
     * @return the font to be used to paint text
     */
    FontInfo getMappedFont(Graphics2D graphics, FontInfo fontInfo);

    /**
     * In case the original font doesn't contain a glyph, use the
     * returned fallback font as an alternative
     *
     * @param graphics the graphics context to request additional rendering hints
     * @param fontInfo the font info object corresponding to the text run font
     *
     * @return the font to be used as a fallback for the original typeface
     */
    FontInfo getFallbackFont(Graphics2D graphics, FontInfo fontInfo);

    /**
     * Map text charset depending on font family.
     * <p>
     * Currently this only maps for wingdings and symbol font (into unicode private use area)
     * <p>
     * Depending if the requested font is installed in the system, tbe mapped string varies:<br>
     * If the font is registered into the graphics environment the characters are mapped to the
     * private use area. If the font is missing (and hence a AWT logical font is used), the
     * characters are mapped to the corresponding unicode characters
     *
     * @param graphics the graphics context to request additional rendering hints
     * @param fontInfo the font info object corresponding to the text run font
     * @param text the raw text
     *
     * @return String with mapped codepoints
     *
     * @see StringUtil#mapMsCodepointString(String)
     */
    String mapFontCharset(Graphics2D graphics, FontInfo fontInfo, String text);

    /**
     * Create an AWT font object with the given attributes
     *
     * @param graphics the graphics context to request additional rendering hints
     * @param fontInfo the font info object corresponding to the text run font
     * @param size the font size in points
     * @param bold {@code true} if the font is bold
     * @param italic {@code true} if the font is italic
     *
     * @return the AWT font object
     */
    Font createAWTFont(Graphics2D graphics, FontInfo fontInfo, double size, boolean bold, boolean italic);
}
