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

/* $Id: FontUtil.java 721430 2008-11-28 11:13:12Z acumiskey $ */

package org.docx4j.fonts.fop.fonts;


/**
 * Font utilities.
 */
public class FontUtil {

    /**
     * Parses an CSS2 (SVG and XSL-FO) font weight (normal, bold, 100-900) to
     * an integer.
     * See http://www.w3.org/TR/REC-CSS2/fonts.html#propdef-font-weight
     * TODO: Implement "lighter" and "bolder".
     * @param text the font weight to parse
     * @return an integer between 100 and 900 (100, 200, 300...)
     */
    public static int parseCSS2FontWeight(String text) {
        int weight = 400;
        try {
            weight = Integer.parseInt(text);
            weight = ((int)weight / 100) * 100;
            weight = Math.max(weight, 100);
            weight = Math.min(weight, 900);
        } catch (NumberFormatException nfe) {
            //weight is no number, so convert symbolic name to number
            if (text.equals("normal")) {
                weight = 400;
            } else if (text.equals("bold")) {
                weight = 700;
            } else {
                throw new IllegalArgumentException(
                    "Illegal value for font weight: '"
                    + text
                    + "'. Use one of: 100, 200, 300, "
                    + "400, 500, 600, 700, 800, 900, "
                    + "normal (=400), bold (=700)");
            }
        }
        return weight;
    }

    /**
     * Removes all white space from a string (used primarily for font names)
     * @param str the string
     * @return the processed result
     */
    public static String stripWhiteSpace(String str) {
        if (str != null) {
            StringBuffer stringBuffer = new StringBuffer(str.length());
            for (int i = 0, strLen = str.length(); i < strLen; i++) {
                final char ch = str.charAt(i);
                if (ch != ' ' && ch != '\r' && ch != '\n' && ch != '\t') {
                    stringBuffer.append(ch);
                }
            }
            return stringBuffer.toString();
        }
        return str;
    }

    /** font constituent names which identify a font as being of "italic" style */
    private static final String[] ITALIC_WORDS = {
        Font.STYLE_ITALIC, Font.STYLE_OBLIQUE, Font.STYLE_INCLINED
    };

    /** font constituent names which identify a font as being of "light" weight */
    private static final String[] LIGHT_WORDS = {"light"};
    /** font constituent names which identify a font as being of "medium" weight */
    private static final String[] MEDIUM_WORDS = {"medium"};
    /** font constituent names which identify a font as being of "demi/semi" weight */
    private static final String[] DEMI_WORDS = {"demi", "semi"};
    /** font constituent names which identify a font as being of "bold" weight */
    private static final String[] BOLD_WORDS = {"bold"};
    /** font constituent names which identify a font as being of "extra bold" weight */
    private static final String[] EXTRA_BOLD_WORDS = {"extrabold", "extra bold", "black",
        "heavy", "ultra", "super"};

    /**
     * Guesses the font style of a font using its name.
     * @param fontName the font name
     * @return "normal" or "italic"
     */
    public static String guessStyle(String fontName) {
        if (fontName != null) {
            for (int i = 0; i < ITALIC_WORDS.length; i++) {
                if (fontName.indexOf(ITALIC_WORDS[i]) != -1) {
                    return Font.STYLE_ITALIC;
                }
            }
        }
        return Font.STYLE_NORMAL;
    }

    /**
     * Guesses the font weight of a font using its name.
     * @param fontName the font name
     * @return an integer between 100 and 900
     */
    public static int guessWeight(String fontName) {
        // weight
        int weight = Font.WEIGHT_NORMAL;

        for (int i = 0; i < BOLD_WORDS.length; i++) {
            if (fontName.indexOf(BOLD_WORDS[i]) != -1) {
                weight = Font.WEIGHT_BOLD;
                break;
            }
        }
        for (int i = 0; i < MEDIUM_WORDS.length; i++) {
            if (fontName.indexOf(MEDIUM_WORDS[i]) != -1) {
                weight = Font.WEIGHT_NORMAL + 100; //500
                break;
            }
        }
        //Search for "semi/demi" before "light", but after "bold"
        //(normally semi/demi-bold is meant, but it can also be semi/demi-light)
        for (int i = 0; i < DEMI_WORDS.length; i++) {
            if (fontName.indexOf(DEMI_WORDS[i]) != -1) {
                weight = Font.WEIGHT_BOLD - 100; //600
                break;
            }
        }
        for (int i = 0; i < EXTRA_BOLD_WORDS.length; i++) {
            if (fontName.indexOf(EXTRA_BOLD_WORDS[i]) != -1) {
                weight = Font.WEIGHT_EXTRA_BOLD;
                break;
            }
        }
        for (int i = 0; i < LIGHT_WORDS.length; i++) {
            if (fontName.indexOf(LIGHT_WORDS[i]) != -1) {
                weight = Font.WEIGHT_LIGHT;
                break;
            }
        }
        return weight;
    }
}
