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

package org.docx4j.fonts.fop.fonts.type1;

import java.util.HashMap;
import java.util.Map;

/**
 * Enumerates the linkplain http://unicode.org/Public/MAPPINGS/VENDORS/ADOBE/stdenc.txt for
 * characters  found in a Type1 font.
 */
public enum AdobeStandardEncoding {
    /** space character */
    space(0x0020, 0x20, "SPACE", "space"),
    /** space character */
    space_nobreak(0x00A0, 0x20, "NO-BREAK SPACE", "space"),
    /** exclamation mark */
    exclam(0x0021, 0x21, "EXCLAMATION MARK", "exclam"),
    /** quotation mark */
    quotedbl(0x0022, 0x22, "QUOTATION MARK", "quotedbl"),
    /** number sign */
    numersign(0x0023, 0x23, "NUMBER SIGN", "numbersign"),
    /** dollar character */
    dollar(0x0024, 0x24, "DOLLAR SIGN", "dollar"),
    /** percent character */
    percent(0x0025, 0x25, "PERCENT SIGN", "percent"),
    /** ampersand character */
    ampersand(0x0026, 0x26, "AMPERSAND", "ampersand"),
    /** right single quotation mark */
    quoteright(0x2019, 0x27, "RIGHT SINGLE QUOTATION MARK", "quoteright"),
    /** left parenthesis character */
    parenleft(0x0028, 0x28, "LEFT PARENTHESIS", "parenleft"),
    /** right parenthesis character */
    parenright(0x0029, 0x29, "RIGHT PARENTHESIS", "parenright"),
    /** asterisk character */
    asterisk(0x002A, 0x2A, "ASTERISK", "asterisk"),
    /** plus sign */
    plus(0x002B, 0x2B, "PLUS SIGN", "plus"),
    /** comma character */
    comma(0x002C, 0x2C, "COMMA", "comma"),
    /** hyphen-minus character */
    hyphen(0x002D, 0x2D, "HYPHEN-MINUS", "hyphen"),
    /** soft-hyphen character */
    hyphen_soft(0x00AD, 0x2D, "SOFT HYPHEN", "hyphen"),
    /** period character */
    period(0x002E, 0x2E, "FULL STOP", "period"),
    /** slash character */
    slash(0x002F, 0x2F, "SOLIDUS", "slash"),
    /** zero character */
    zero(0x0030, 0x30, "DIGIT ZERO", "zero"),
    /** one character */
    one(0x0031, 0x31, "DIGIT ONE", "one"),
    /** two character */
    two(0x0032, 0x32, "DIGIT TWO", "two"),
    /** three character */
    three(0x0033, 0x33, "DIGIT THREE", "three"),
    /** four character */
    four(0x0034, 0x34, "DIGIT FOUR", "four"),
    /** five character */
    five(0x0035, 0x35, "DIGIT FIVE", "five"),
    /** six character */
    six(0x0036, 0x36, "DIGIT SIX", "six"),
    /** seven character */
    seven(0x0037, 0x37, "DIGIT SEVEN", "seven"),
    /** eight character */
    eight(0x0038, 0x38, "DIGIT EIGHT", "eight"),
    /** nine character */
    nine(0x0039, 0x39, "DIGIT NINE", "nine"),
    /** colon character */
    colon(0x003A, 0x3A, "COLON", "colon"),
    /** semi-colon character */
    semicolon(0x003B, 0x3B, "SEMICOLON", "semicolon"),
    /** less character */
    less(0x003C, 0x3C, "LESS-THAN SIGN", "less"),
    /** equal character */
    equal(0x003D, 0x3D, "EQUALS SIGN", "equal"),
    /** greater character */
    greater(0x003E, 0x3E, "GREATER-THAN SIGN", "greater"),
    /** question character */
    question(0x003F, 0x3F, "QUESTION MARK", "question"),
    /** at character */
    at(0x0040, 0x40, "COMMERCIAL AT", "at"),
    /** A character */
    A(0x0041, 0x41, "LATIN CAPITAL LETTER A", "A"),
    /** B character */
    B(0x0042, 0x42, "LATIN CAPITAL LETTER B", "B"),
    /** C character */
    C(0x0043, 0x43, "LATIN CAPITAL LETTER C", "C"),
    /** D character */
    D(0x0044, 0x44, "LATIN CAPITAL LETTER D", "D"),
    /** E character */
    E(0x0045, 0x45, "LATIN CAPITAL LETTER E", "E"),
    /** F character */
    F(0x0046, 0x46, "LATIN CAPITAL LETTER F", "F"),
    /** G character */
    G(0x0047, 0x47, "LATIN CAPITAL LETTER G", "G"),
    /** H character */
    H(0x0048, 0x48, "LATIN CAPITAL LETTER H", "H"),
    /** I character */
    I(0x0049, 0x49, "LATIN CAPITAL LETTER I", "I"),
    /** J character */
    J(0x004A, 0x4A, "LATIN CAPITAL LETTER J", "J"),
    /** K character */
    K(0x004B, 0x4B, "LATIN CAPITAL LETTER K", "K"),
    /** L character */
    L(0x004C, 0x4C, "LATIN CAPITAL LETTER L", "L"),
    /** M character */
    M(0x004D, 0x4D, "LATIN CAPITAL LETTER M", "M"),
    /** N character */
    N(0x004E, 0x4E, "LATIN CAPITAL LETTER N", "N"),
    /** O character */
    O(0x004F, 0x4F, "LATIN CAPITAL LETTER O", "O"),
    /** P character */
    P(0x0050, 0x50, "LATIN CAPITAL LETTER P", "P"),
    /** Q character */
    Q(0x0051, 0x51, "LATIN CAPITAL LETTER Q", "Q"),
    /** R character */
    R(0x0052, 0x52, "LATIN CAPITAL LETTER R", "R"),
    /** S character */
    S(0x0053, 0x53, "LATIN CAPITAL LETTER S", "S"),
    /** T character */
    T(0x0054, 0x54, "LATIN CAPITAL LETTER T", "T"),
    /** U character */
    U(0x0055, 0x55, "LATIN CAPITAL LETTER U", "U"),
    /** V character */
    V(0x0056, 0x56, "LATIN CAPITAL LETTER V", "V"),
    /** W character */
    W(0x0057, 0x57, "LATIN CAPITAL LETTER W", "W"),
    /** X character */
    X(0x0058, 0x58, "LATIN CAPITAL LETTER X", "X"),
    /** Y character */
    Y(0x0059, 0x59, "LATIN CAPITAL LETTER Y", "Y"),
    /** Z character */
    Z(0x005A, 0x5A, "LATIN CAPITAL LETTER Z", "Z"),
    /** left bracket character */
    bracketleft(0x005B, 0x5B, "LEFT SQUARE BRACKET", "bracketleft"),
    /** back slash character */
    backslash(0x005C, 0x5C, "REVERSE SOLIDUS", "backslash"),
    /** bracket right character */
    bracketright(0x005D, 0x5D, "RIGHT SQUARE BRACKET", "bracketright"),
    /** circumflex character */
    asciicircum(0x005E, 0x5E, "CIRCUMFLEX ACCENT", "asciicircum"),
    /** under score character */
    underscore(0x005F, 0x5F, "LOW LINE", "underscore"),
    /** left single quotation character */
    quoteleft(0x2018, 0x60, "LEFT SINGLE QUOTATION MARK", "quoteleft"),
    /** a character */
    a(0x0061, 0x61, "LATIN SMALL LETTER A", "a"),
    /** b character */
    b(0x0062, 0x62, "LATIN SMALL LETTER B", "b"),
    /** c character */
    c(0x0063, 0x63, "LATIN SMALL LETTER C", "c"),
    /** d character */
    d(0x0064, 0x64, "LATIN SMALL LETTER D", "d"),
    /** e character */
    e(0x0065, 0x65, "LATIN SMALL LETTER E", "e"),
    /** f character */
    f(0x0066, 0x66, "LATIN SMALL LETTER F", "f"),
    /** g character */
    g(0x0067, 0x67, "LATIN SMALL LETTER G", "g"),
    /** h character */
    h(0x0068, 0x68, "LATIN SMALL LETTER H", "h"),
    /** i character */
    i(0x0069, 0x69, "LATIN SMALL LETTER I", "i"),
    /** j character */
    j(0x006A, 0x6A, "LATIN SMALL LETTER J", "j"),
    /** k character */
    k(0x006B, 0x6B, "LATIN SMALL LETTER K", "k"),
    /** l character */
    l(0x006C, 0x6C, "LATIN SMALL LETTER L", "l"),
    /** m character */
    m(0x006D, 0x6D, "LATIN SMALL LETTER M", "m"),
    /** n character */
    n(0x006E, 0x6E, "LATIN SMALL LETTER N", "n"),
    /** o character */
    o(0x006F, 0x6F, "LATIN SMALL LETTER O", "o"),
    /** p character */
    p(0x0070, 0x70, "LATIN SMALL LETTER P", "p"),
    /** q character */
    q(0x0071, 0x71, "LATIN SMALL LETTER Q", "q"),
    /** r character */
    r(0x0072, 0x72, "LATIN SMALL LETTER R", "r"),
    /** s character */
    s(0x0073, 0x73, "LATIN SMALL LETTER S", "s"),
    /** t character */
    t(0x0074, 0x74, "LATIN SMALL LETTER T", "t"),
    /** u character */
    u(0x0075, 0x75, "LATIN SMALL LETTER U", "u"),
    /** v character */
    v(0x0076, 0x76, "LATIN SMALL LETTER V", "v"),
    /** w character */
    w(0x0077, 0x77, "LATIN SMALL LETTER W", "w"),
    /** x character */
    x(0x0078, 0x78, "LATIN SMALL LETTER X", "x"),
    /** y character */
    y(0x0079, 0x79, "LATIN SMALL LETTER Y", "y"),
    /** z character */
    z(0x007A, 0x7A, "LATIN SMALL LETTER Z", "z"),
    /** left curly bracket character */
    braceleft(0x007B, 0x7B, "LEFT CURLY BRACKET", "braceleft"),
    /** vertical line character */
    bar(0x007C, 0x7C, "VERTICAL LINE", "bar"),
    /** right curly bracket character */
    braceright(0x007D, 0x7D, "RIGHT CURLY BRACKET", "braceright"),
    /** tilde character */
    asciitilde(0x007E, 0x7E, "TILDE", "asciitilde"),
    /** inverted exclamation mark */
    exclamdown(0x00A1, 0xA1, "INVERTED EXCLAMATION MARK", "exclamdown"),
    /** cent character */
    cent(0x00A2, 0xA2, "CENT SIGN", "cent"),
    /** sterling character */
    sterling(0x00A3, 0xA3, "POUND SIGN", "sterling"),
    /** fraction slash character */
    fraction(0x2044, 0xA4, "FRACTION SLASH", "fraction"),
    /** division slash character */
    fraction_division_slash(0x2215, 0xA4, "DIVISION SLASH", "fraction"),
    /** yen character */
    yen(0x00A5, 0xA5, "YEN SIGN", "yen"),
    /** florin character */
    florin(0x0192, 0xA6, "LATIN SMALL LETTER F WITH HOOK", "florin"),
    /** section sign character */
    section(0x00A7, 0xA7, "SECTION SIGN", "section"),
    /** currency sign character */
    currency(0x00A4, 0xA8, "CURRENCY SIGN", "currency"),
    /** apostrophe character */
    quotesingle(0x0027, 0xA9, "APOSTROPHE", "quotesingle"),
    /** double left quotation mark */
    quotedblleft(0x201C, 0xAA, "LEFT DOUBLE QUOTATION MARK", "quotedblleft"),
    /** left-pointing double angle quotation mark */
    guillemotleft(0x00AB, 0xAB, "LEFT-POINTING DOUBLE ANGLE QUOTATION MARK", "guillemotleft"),
    /** left-pointing  single quotation mark */
    guilsinglleft(0x2039, 0xAC, "SINGLE LEFT-POINTING ANGLE QUOTATION MARK", "guilsinglleft"),
    /** right-pointing  single quotation mark */
    guilsinglright(0x203A, 0xAD, "SINGLE RIGHT-POINTING ANGLE QUOTATION MARK", "guilsinglright"),
    /** fi ligature */
    fi(0xFB01, 0xAE, "LATIN SMALL LIGATURE FI", "fi"),
    /** fl ligature */
    fl(0xFB02, 0xAF, "LATIN SMALL LIGATURE FL", "fl"),
    /** en-dash character */
    endash(0x2013, 0xB1, "EN DASH", "endash"),
    /** dagger character */
    dagger(0x2020, 0xB2, "DAGGER", "dagger"),
    /** double dagger character */
    daggerdbl(0x2021, 0xB3, "DOUBLE DAGGER", "daggerdbl"),
    /** centered period character */
    periodcentered(0x00B7, 0xB4, "MIDDLE DOT", "periodcentered"),
    /** centered period character */
    periodcentered_bullet_operator(0x2219, 0xB4, "BULLET OPERATOR", "periodcentered"),
    /** paragraph character */
    paragraph(0x00B6, 0xB6, "PILCROW SIGN", "paragraph"),
    /** bullet character */
    bullet(0x2022, 0xB7, "BULLET", "bullet"),
    /** single low-9 quotation mark */
    quotesinglbase(0x201A, 0xB8, "SINGLE LOW-9 QUOTATION MARK", "quotesinglbase"),
    /** double low-9 quotation mark */
    quotedblbase(0x201E, 0xB9, "DOUBLE LOW-9 QUOTATION MARK", "quotedblbase"),
    /** right double quotation mark */
    quotedblright(0x201D, 0xBA, "RIGHT DOUBLE QUOTATION MARK", "quotedblright"),
    /** right-pointing double angle quotation mark */
    guillemotright(0x00BB, 0xBB, "RIGHT-POINTING DOUBLE ANGLE QUOTATION MARK", "guillemotright"),
    /** horizontal ellipsis character */
    ellipsis(0x2026, 0xBC, "HORIZONTAL ELLIPSIS", "ellipsis"),
    /** per-mille character */
    perthousand(0x2030, 0xBD, "PER MILLE SIGN", "perthousand"),
    /** inverted question mark */
    questiondown(0x00BF, 0xBF, "INVERTED QUESTION MARK", "questiondown"),
    /** grave accent character */
    grave(0x0060, 0xC1, "GRAVE ACCENT", "grave"),
    /** acute accent character */
    acute(0x00B4, 0xC2, "ACUTE ACCENT", "acute"),
    /** modifier letter circumflex accent character */
    circumflex(0x02C6, 0xC3, "MODIFIER LETTER CIRCUMFLEX ACCENT", "circumflex"),
    /** small tilde character */
    tilde(0x02DC, 0xC4, "SMALL TILDE", "tilde"),
    /** macron character */
    macron(0x00AF, 0xC5, "MACRON", "macron"),
    /** modifier letter macron character */
    macron_modifier_letter(0x02C9, 0xC5, "MODIFIER LETTER MACRON", "macron"),
    /** breve character */
    breve(0x02D8, 0xC6, "BREVE", "breve"),
    /** dot above character */
    dotaccent(0x02D9, 0xC7, "DOT ABOVE", "dotaccent"),
    /** diaeresis character */
    dieresis(0x00A8, 0xC8, "DIAERESIS", "dieresis"),
    /** ring above character */
    ring(0x02DA, 0xCA, "RING ABOVE", "ring"),
    /** cedilla character */
    cedilla(0x00B8, 0xCB, "CEDILLA", "cedilla"),
    /** double acute accent character */
    hungarumlaut(0x02DD, 0xCD, "DOUBLE ACUTE ACCENT", "hungarumlaut"),
    /** agonek character */
    ogonek(0x02DB, 0xCE, "OGONEK", "ogonek"),
    /** caron character */
    caron(0x02C7, 0xCF, "CARON", "caron"),
    /** emdash character */
    emdash(0x2014, 0xD0, "EM DASH", "emdash"),
    /** AE (capitalised) character */
    AE(0x00C6, 0xE1, "LATIN CAPITAL LETTER AE", "AE"),
    /** femenine ordinal indicator character */
    ordfeminine(0x00AA, 0xE3, "FEMININE ORDINAL INDICATOR", "ordfeminine"),
    /** capital letter L with stroke character */
    Lslash(0x0141, 0xE8, "LATIN CAPITAL LETTER L WITH STROKE", "Lslash"),
    /** capital letter O with stroke character */
    Oslash(0x00D8, 0xE9, "LATIN CAPITAL LETTER O WITH STROKE", "Oslash"),
    /** OE (capitalised) character */
    OE(0x0152, 0xEA, "LATIN CAPITAL LIGATURE OE", "OE"),
    /** masculine ordinal indicator character */
    ordmasculine(0x00BA, 0xEB, "MASCULINE ORDINAL INDICATOR", "ordmasculine"),
    /** ae (small) character */
    ae(0x00E6, 0xF1, "LATIN SMALL LETTER AE", "ae"),
    /** dotless i character */
    dotlessi(0x0131, 0xF5, "LATIN SMALL LETTER DOTLESS I", "dotlessi"),
    /** small letter l with stroke character */
    lslash(0x0142, 0xF8, "LATIN SMALL LETTER L WITH STROKE", "lslash"),
    /** small letter o with stroke character */
    oslash(0x00F8, 0xF9, "LATIN SMALL LETTER O WITH STROKE", "oslash"),
    /** oe (small) character */
    oe(0x0153, 0xFA, "LATIN SMALL LIGATURE OE", "oe"),
    /** small letter sharp s character */
    germandbls(0x00DF, 0xFB, "LATIN SMALL LETTER SHARP S", "germandbls");

    private final int unicodeIndex;
    private final int adobeCodePoint;
    private final String unicodeName;
    private final String adobeName;

    /** The name of the Adobe Standard Encoding as seen in an AFM file. */
    public static final String NAME = "AdobeStandardEncoding";

    private static final Map<String, AdobeStandardEncoding> CACHE
            = new HashMap<String, AdobeStandardEncoding>();
    static {
        for (AdobeStandardEncoding encoding : AdobeStandardEncoding.values()) {
            CACHE.put(encoding.getAdobeName(), encoding);
        }
    }

    private AdobeStandardEncoding(int unicodeIndex, int adobeCodePoint, String unicodeName,
            String adobeName) {
        this.unicodeIndex = unicodeIndex;
        this.adobeCodePoint = adobeCodePoint;
        this.unicodeName = unicodeName;
        this.adobeName = adobeName;
    }

    /**
     * The Unicode index of this character.
     *
     * @return the Unicode index
     */
    int getUnicodeIndex() {
        return unicodeIndex;
    }

    /**
     * The Adobe code point of this character.
     *
     * @return the Adobe code point
     */
    int getAdobeCodePoint() {
        return adobeCodePoint;
    }

    /**
     * The Unicode name for this character.
     *
     * @return the Unicode name
     */
    String getUnicodeName() {
        return unicodeName;
    }

    /**
     * The Adobe name for this character.
     *
     * @return the Adobe name
     */
    String getAdobeName() {
        return adobeName;
    }

    /**
     * Returns the code point of a Adobe standard encoded character given its name. If the name
     * cannot be found, -1 is returned.
     *
     * @param adobeName the name of the character
     * @return the Adobe code point
     */
    public static int getAdobeCodePoint(String adobeName) {
        AdobeStandardEncoding encoding = CACHE.get(adobeName);
        return encoding != null ? encoding.getAdobeCodePoint() : -1;
    }

    public static String getCharFromCodePoint(int codePoint) {
        return CACHE.values().stream().filter(encoding -> encoding.getAdobeCodePoint() == codePoint).findFirst().map(AdobeStandardEncoding::getAdobeName).orElse("");
    }

    public static char getUnicodeFromCodePoint(int codePoint) {
        for (AdobeStandardEncoding encoding : CACHE.values()) {
            if (encoding.getAdobeCodePoint() == codePoint) {
                return (char) encoding.getUnicodeIndex();
            }
        }
        return (char) -1;
    }
}
