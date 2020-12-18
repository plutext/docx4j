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

package org.docx4j.fonts.fop.complexscripts.util;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

// CSOFF: LineLengthCheck

/**
 * <p>Implementation of Number to String Conversion algorithm specified by
 * XSL Transformations (XSLT) Version 2.0, W3C Recommendation, 23 January 2007.</p>
 *
 * <p>This algorithm differs from that specified in XSLT 1.0 in the following
 * ways:</p>
 * <ul>
 * <li>input numbers are greater than or equal to zero rather than greater than zero;</li>
 * <li>introduces format tokens { w, W, Ww };</li>
 * <li>introduces ordinal parameter to generate ordinal numbers;</li>
 * </ul>
 *
 * <p>Implementation Defaults and Limitations</p>
 * <ul>
 * <li>If language parameter is unspecified (null or empty string), then the value
 * of DEFAULT_LANGUAGE is used, which is defined below as "eng" (English).</li>
 * <li>Only English, French, and Spanish word numerals are supported, and only if less than one trillion (1,000,000,000,000).</li>
 * <li>Ordinal word numerals are supported for French and Spanish only when less than or equal to ten (10).</li>
 * </ul>
 *
 * <p>Implementation Notes</p>
 * <ul>
 * <li>In order to handle format tokens outside the Unicode BMP, all processing is
 * done in Unicode Scalar Values represented with Integer and Integer[]
 * types. Without affecting behavior, this may be subsequently optimized to
 * use int and int[] types.</li>
 * <li>In order to communicate various sub-parameters, including ordinalization, a <em>features</em>
 * is employed, which consists of comma separated name and optional value tokens, where name and value
 * are separated by an equals '=' sign.</li>
 * <li>Ordinal numbers are selected by specifying a word based format token in combination with a  'ordinal' feature with no value, in which case
 * the features 'male' and 'female' may be used to specify gender for gender sensitive languages. For example, the feature string "ordinal,female"
 * selects female ordinals.</li>
 * </ul>
 *
 * <p>This work was originally authored by Glenn Adams (gadams@apache.org).</p>
 */
public class NumberConverter {

    /** alphabetical  */
    public static final int LETTER_VALUE_ALPHABETIC = 1;
    /** traditional  */
    public static final int LETTER_VALUE_TRADITIONAL = 2;

    /** no token type */
    private static final int TOKEN_NONE = 0;
    /** alhphanumeric token type */
    private static final int TOKEN_ALPHANUMERIC = 1;
    /** nonalphanumeric token type */
    private static final int TOKEN_NONALPHANUMERIC = 2;
    /** default token */
    private static final Integer[] DEFAULT_TOKEN = new Integer[] { (int) '1' };
    /** default separator */
    private static final Integer[] DEFAULT_SEPARATOR = new Integer[] { (int) '.' };
    /** default language */
    private static final String DEFAULT_LANGUAGE = "eng";

    /** prefix token */
    private Integer[] prefix;
    /** suffix token */
    private Integer[] suffix;
    /** sequence of tokens, as parsed from format */
    private Integer[][] tokens;
    /** sequence of separators, as parsed from format */
    private Integer[][] separators;
    /** grouping separator */
    private int groupingSeparator;
    /** grouping size */
    private int groupingSize;
    /** letter value */
    private int letterValue;
    /** letter value system */
    private String features;
    /** language */
    private String language;
    /** country */
    private String country;

    /**
     * Construct parameterized number converter.
     * @param format format for the page number (may be null or empty, which is treated as null)
     * @param groupingSeparator grouping separator (if zero, then no grouping separator applies)
     * @param groupingSize grouping size (if zero or negative, then no grouping size applies)
     * @param letterValue letter value (must be one of the above letter value enumeration values)
     * @param features features (feature sub-parameters)
     * @param language (may be null or empty, which is treated as null)
     * @param country (may be null or empty, which is treated as null)
     * @throws IllegalArgumentException if format is not a valid UTF-16 string (e.g., has unpaired surrogate)
     */
    public NumberConverter(String format, int groupingSeparator, int groupingSize, int letterValue, String features, String language, String country)
        throws IllegalArgumentException {
        this.groupingSeparator = groupingSeparator;
        this.groupingSize = groupingSize;
        this.letterValue = letterValue;
        this.features = features;
        this.language = (language != null) ? language.toLowerCase() : null;
        this.country = (country != null) ? country.toLowerCase() : null;
        parseFormatTokens(format);
    }

    /**
     * Convert a number to string according to conversion parameters.
     * @param number number to conver
     * @return string representing converted number
     */
    public String convert(long number) {
        List<Long> numbers = new ArrayList<Long>();
        numbers.add(number);
        return convert(numbers);
    }

    /**
     * Convert list of numbers to string according to conversion parameters.
     * @param numbers list of numbers to convert
     * @return string representing converted list of numbers
     */
    public String convert(List<Long> numbers) {
        List<Integer> scalars = new ArrayList<Integer>();
        if (prefix != null) {
            appendScalars(scalars, prefix);
        }
        convertNumbers(scalars, numbers);
        if (suffix != null) {
            appendScalars(scalars, suffix);
        }
        return scalarsToString(scalars);
    }

    private void parseFormatTokens(String format) throws IllegalArgumentException {
        List<Integer[]> tokens = new ArrayList<Integer[]>();
        List<Integer[]> separators = new ArrayList<Integer[]>();
        if ((format == null) || (format.length() == 0)) {
            format = "1";
        }
        int tokenType = TOKEN_NONE;
        List<Integer> token = new ArrayList<Integer>();
        Integer[] ca = UTF32.toUTF32(format, 0, true);
        for (Integer c : ca) {
            int tokenTypeNew = isAlphaNumeric(c) ? TOKEN_ALPHANUMERIC : TOKEN_NONALPHANUMERIC;
            if (tokenTypeNew != tokenType) {
                if (token.size() > 0) {
                    if (tokenType == TOKEN_ALPHANUMERIC) {
                        tokens.add(token.toArray(new Integer[token.size()]));
                    } else {
                        separators.add(token.toArray(new Integer[token.size()]));
                    }
                    token.clear();
                }
                tokenType = tokenTypeNew;
            }
            token.add(c);
        }
        if (token.size() > 0) {
            if (tokenType == TOKEN_ALPHANUMERIC) {
                tokens.add(token.toArray(new Integer [ token.size() ]));
            } else {
                separators.add(token.toArray(new Integer [ token.size() ]));
            }
        }
        if (!separators.isEmpty()) {
            this.prefix = separators.remove(0);
        }
        if (!separators.isEmpty()) {
            this.suffix = separators.remove(separators.size() - 1);
        }
        this.separators = separators.toArray(new Integer [ separators.size() ] []);
        this.tokens = tokens.toArray(new Integer [ tokens.size() ] []);
    }

    private static boolean isAlphaNumeric(int c) {
        switch (Character.getType(c)) {
        case Character.DECIMAL_DIGIT_NUMBER:    // Nd
        case Character.LETTER_NUMBER:           // Nl
        case Character.OTHER_NUMBER:            // No
        case Character.UPPERCASE_LETTER:        // Lu
        case Character.LOWERCASE_LETTER:        // Ll
        case Character.TITLECASE_LETTER:        // Lt
        case Character.MODIFIER_LETTER:         // Lm
        case Character.OTHER_LETTER:            // Lo
            return true;
        default:
            return false;
        }
    }

    private void convertNumbers(List<Integer> scalars, List<Long> numbers) {
        Integer[] tknLast = DEFAULT_TOKEN;
        int   tknIndex = 0;
        int   tknCount = tokens.length;
        int   sepIndex = 0;
        int   sepCount = separators.length;
        int   numIndex = 0;
        for (Long number : numbers) {
            Integer[] sep = null;
            Integer[] tkn;
            if (tknIndex < tknCount) {
                if (numIndex > 0) {
                    if (sepIndex < sepCount) {
                        sep = separators [ sepIndex++ ];
                    } else {
                        sep = DEFAULT_SEPARATOR;
                    }
                }
                tkn = tokens [ tknIndex++ ];
            } else {
                tkn = tknLast;
            }
            appendScalars(scalars, convertNumber(number, sep, tkn));
            tknLast = tkn;
            numIndex++;
        }
    }

    private Integer[] convertNumber(long number, Integer[] separator, Integer[] token) {
        List<Integer> sl = new ArrayList<Integer>();
        if (separator != null) {
            appendScalars(sl, separator);
        }
        if (token != null) {
            appendScalars(sl, formatNumber(number, token));
        }
        return sl.toArray(new Integer [ sl.size() ]);
    }

    private Integer[] formatNumber(long number, Integer[] token) {
        Integer[] fn = null;
        assert token.length > 0;
        if (number < 0) {
            throw new IllegalArgumentException("number must be non-negative");
        } else if (token.length == 1) {
            int s = token[0];
            switch (s) {
            case (int) '1':
                fn = formatNumberAsDecimal(number, (int) '1', 1);
            break;
            case (int) 'W':
            case (int) 'w':
                fn = formatNumberAsWord(number, (s == (int) 'W') ? Character.UPPERCASE_LETTER : Character.LOWERCASE_LETTER);
            break;
            case (int) 'A': // handled as numeric sequence
            case (int) 'a': // handled as numeric sequence
            case (int) 'I': // handled as numeric special
            case (int) 'i': // handled as numeric special
            default:
                if (isStartOfDecimalSequence(s)) {
                    fn = formatNumberAsDecimal(number, s, 1);
                } else if (isStartOfAlphabeticSequence(s)) {
                    fn = formatNumberAsSequence(number, s, getSequenceBase(s), null);
                } else if (isStartOfNumericSpecial(s)) {
                    fn = formatNumberAsSpecial(number, s);
                } else {
                    fn = null;
                }
                break;
            }
        } else if ((token.length == 2) && (token[0] == (int) 'W') && (token[1] == (int) 'w')) {
            fn = formatNumberAsWord(number, Character.TITLECASE_LETTER);
        } else if (isPaddedOne(token)) {
            int s = token[token.length - 1];
            fn = formatNumberAsDecimal(number, s, token.length);
        } else {
            throw new IllegalArgumentException("invalid format token: \"" + UTF32.fromUTF32(token) + "\"");
        }
        if (fn == null) {
            fn = formatNumber(number, DEFAULT_TOKEN);
        }
        assert fn != null;
        return fn;
    }

    /**
     * Format NUMBER as decimal using characters denoting digits that start at ONE,
     * adding one or more (zero) padding characters as needed to fill out field WIDTH.
     * @param number to be formatted
     * @param one unicode scalar value denoting numeric value 1
     * @param width non-negative integer denoting field width of number, possible including padding
     * @return formatted number as array of unicode scalars
     */
    private Integer[] formatNumberAsDecimal(long number, int one, int width) {
        assert Character.getNumericValue(one) == 1;
        assert Character.getNumericValue(one - 1) == 0;
        assert Character.getNumericValue(one + 8) == 9;
        List<Integer> sl = new ArrayList<Integer>();
        int zero = one - 1;
        while (number > 0) {
            long digit = number % 10;
            sl.add(0, zero + (int) digit);
            number = number / 10;
        }
        while (width > sl.size()) {
            sl.add(0, zero);
        }
        if ((groupingSize != 0) && (groupingSeparator != 0)) {
            sl = performGrouping(sl, groupingSize, groupingSeparator);
        }
        return sl.toArray(new Integer [ sl.size() ]);
    }

    private static List<Integer> performGrouping(List<Integer> sl, int groupingSize, int groupingSeparator) {
        assert groupingSize > 0;
        assert groupingSeparator != 0;
        if (sl.size() > groupingSize) {
            List<Integer> gl = new ArrayList<Integer>();
            for (int i = 0, n = sl.size(), g = 0; i < n; i++) {
                int k = n - i - 1;
                if (g == groupingSize) {
                    gl.add(0, groupingSeparator);
                    g = 1;
                } else {
                    g++;
                }
                gl.add(0, sl.get(k));
            }
            return gl;
        } else {
            return sl;
        }
    }


    /**
     * Format NUMBER as using sequence of characters that start at ONE, and
     * having BASE radix.
     * @param number to be formatted
     * @param one unicode scalar value denoting start of sequence (numeric value 1)
     * @param base number of elements in sequence
     * @param map if non-null, then maps sequences indices to unicode scalars
     * @return formatted number as array of unicode scalars
     */
    private Integer[] formatNumberAsSequence(long number, int one, int base, int[] map) {
        assert base > 1;
        assert (map == null) || (map.length >= base);
        List<Integer> sl = new ArrayList<Integer>();
        if (number == 0) {
            return null;
        } else {
            long n = number;
            while (n > 0) {
                int d = (int) ((n - 1) % (long) base);
                int s = (map != null) ? map [ d ] : (one + d);
                sl.add(0, s);
                n = (n - 1) / base;
            }
            return sl.toArray(new Integer [ sl.size() ]);
        }
    }

    /**
     * Format NUMBER as using special system that starts at ONE.
     * @param number to be formatted
     * @param one unicode scalar value denoting start of system (numeric value 1)
     * @return formatted number as array of unicode scalars
     */
    private Integer[] formatNumberAsSpecial(long number, int one) {
        SpecialNumberFormatter f = getSpecialFormatter(one, letterValue, features, language, country);
        if (f != null) {
            return f.format(number, one, letterValue, features, language, country);
        } else {
            return null;
        }
    }

    /**
     * Format NUMBER as word according to TYPE, which must be either
     * Character.UPPERCASE_LETTER, Character.LOWERCASE_LETTER, or
     * Character.TITLECASE_LETTER. Makes use of this.language to
     * determine language of word.
     * @param number to be formatted
     * @param caseType unicode character type for case conversion
     * @return formatted number as array of unicode scalars
     */
    private Integer[] formatNumberAsWord(long number, int caseType) {
        SpecialNumberFormatter f = null;
        if (isLanguage("eng")) {
            f = new EnglishNumberAsWordFormatter(caseType);
        } else if (isLanguage("spa")) {
            f = new SpanishNumberAsWordFormatter(caseType);
        } else if (isLanguage("fra")) {
            f = new FrenchNumberAsWordFormatter(caseType);
        } else {
            f = new EnglishNumberAsWordFormatter(caseType);
        }
        return f.format(number, 0, letterValue, features, language, country);
    }

    private boolean isLanguage(String iso3Code) {
        if (language == null) {
            return false;
        } else if (language.equals(iso3Code)) {
            return true;
        } else {
            return isSameLanguage(iso3Code, language);
        }
    }

    private static String[][] equivalentLanguages = {
        { "eng", "en" },
        { "fra", "fre", "fr" },
        { "spa", "es" },
    };

    private static boolean isSameLanguage(String i3c, String lc) {
        for (String[] el : equivalentLanguages) {
            assert el.length >= 2;
            if (el[0].equals(i3c)) {
                for (String anEl : el) {
                    if (anEl.equals(lc)) {
                        return true;
                    }
                }
                return false;
            }
        }
        return false;
    }

    private static boolean hasFeature(String features, String feature) {
        if (features != null) {
            assert feature != null;
            assert feature.length() != 0;
            String[] fa = features.split(",");
            for (String f : fa) {
                String[] fp = f.split("=");
                assert fp.length > 0;
                String   fn = fp[0];
                String   fv = (fp.length > 1) ? fp[1] : "";
                if (fn.equals(feature)) {
                    return true;
                }
            }
        }
        return false;
    }

    /* not yet used
    private static String getFeatureValue ( String features, String feature ) {
        if ( features != null ) {
            assert feature != null;
            assert feature.length() != 0;
            String[] fa = features.split(",");
            for ( String f : fa ) {
                String[] fp = f.split("=");
                assert fp.length > 0;
                String   fn = fp[0];
                String   fv = ( fp.length > 1 ) ? fp[1] : "";
                if ( fn.equals ( feature ) ) {
                    return fv;
                }
            }
        }
        return "";
    }
    */

    private static void appendScalars(List<Integer> scalars, Integer[] sa) {
        Collections.addAll(scalars, sa);
    }

    private static String scalarsToString(List<Integer> scalars) {
        Integer[] sa = scalars.toArray(new Integer [ scalars.size() ]);
        return UTF32.fromUTF32(sa);
    }

    private static boolean isPaddedOne(Integer[] token) {
        if (getDecimalValue(token [ token.length - 1 ]) != 1) {
            return false;
        } else {
            for (int i = 0, n = token.length - 1; i < n; i++) {
                if (getDecimalValue(token [ i ]) != 0) {
                    return false;
                }
            }
            return true;
        }
    }

    private static int getDecimalValue(Integer scalar) {
        int s = scalar;
        if (Character.getType(s) == Character.DECIMAL_DIGIT_NUMBER) {
            return Character.getNumericValue(s);
        } else {
            return -1;
        }
    }

    private static boolean isStartOfDecimalSequence(int s) {
        return (Character.getNumericValue(s) == 1)
            && (Character.getNumericValue(s - 1) == 0)
            && (Character.getNumericValue(s + 8) == 9);
    }

    private static int[][] supportedAlphabeticSequences = {
        { 'A', 26 },            // A...Z
        { 'a', 26 },            // a...z
    };

    private static boolean isStartOfAlphabeticSequence(int s) {
        for (int[] ss : supportedAlphabeticSequences) {
            assert ss.length >= 2;
            if (ss[0] == s) {
                return true;
            }
        }
        return false;
    }

    private static int getSequenceBase(int s) {
        for (int[] ss : supportedAlphabeticSequences) {
            assert ss.length >= 2;
            if (ss[0] == s) {
                return ss[1];
            }
        }
        return 0;
    }

    private static int[][] supportedSpecials = {
        { 'I' },                // latin - uppercase roman numerals
        { 'i' },                // latin - lowercase roman numerals
        { '\u0391' },           // greek - uppercase isopsephry numerals
        { '\u03B1' },           // greek - lowercase isopsephry numerals
        { '\u05D0' },           // hebrew - gematria numerals
        { '\u0623' },           // arabic - abjadi numberals
        { '\u0627' },           // arabic - either abjadi or hijai alphabetic sequence
        { '\u0E01' },           // thai - default alphabetic sequence
        { '\u3042' },           // kana - hiragana (gojuon) - default alphabetic sequence
        { '\u3044' },           // kana - hiragana (iroha)
        { '\u30A2' },           // kana - katakana (gojuon) - default alphabetic sequence
        { '\u30A4' },           // kana - katakana (iroha)
    };

    private static boolean isStartOfNumericSpecial(int s) {
        for (int[] ss : supportedSpecials) {
            assert ss.length >= 1;
            if (ss[0] == s) {
                return true;
            }
        }
        return false;
    }

    private SpecialNumberFormatter getSpecialFormatter(int one, int letterValue, String features, String language, String country) {
        if (one == (int) 'I') {
            return new RomanNumeralsFormatter();
        } else if (one == (int) 'i') {
            return new RomanNumeralsFormatter();
        } else if (one == (int) '\u0391') {
            return new IsopsephryNumeralsFormatter();
        } else if (one == (int) '\u03B1') {
            return new IsopsephryNumeralsFormatter();
        } else if (one == (int) '\u05D0') {
            return new GematriaNumeralsFormatter();
        } else if (one == (int) '\u0623') {
            return new ArabicNumeralsFormatter();
        } else if (one == (int) '\u0627') {
            return new ArabicNumeralsFormatter();
        } else if (one == (int) '\u0E01') {
            return new ThaiNumeralsFormatter();
        } else if (one == (int) '\u3042') {
            return new KanaNumeralsFormatter();
        } else if (one == (int) '\u3044') {
            return new KanaNumeralsFormatter();
        } else if (one == (int) '\u30A2') {
            return new KanaNumeralsFormatter();
        } else if (one == (int) '\u30A4') {
            return new KanaNumeralsFormatter();
        } else {
            return null;
        }
    }

    private static Integer[] toUpperCase(Integer[] sa) {
        assert sa != null;
        for (int i = 0, n = sa.length; i < n; i++) {
            Integer s = sa [ i ];
            sa [ i ] = Character.toUpperCase(s);
        }
        return sa;
    }

    private static Integer[] toLowerCase(Integer[] sa) {
        assert sa != null;
        for (int i = 0, n = sa.length; i < n; i++) {
            Integer s = sa [ i ];
            sa [ i ] = Character.toLowerCase(s);
        }
        return sa;
    }

    /* not yet used
    private static Integer[] toTitleCase ( Integer[] sa ) {
        assert sa != null;
        if ( sa.length > 0 ) {
            sa [ 0 ] = Character.toTitleCase ( sa [ 0 ] );
        }
        return sa;
    }
    */

    private static List<String> convertWordCase(List<String> words, int caseType) {
        List<String> wl = new ArrayList<String>();
        for (String w : words) {
            wl.add(convertWordCase(w, caseType));
        }
        return wl;
    }

    private static String convertWordCase(String word, int caseType) {
        if (caseType == Character.UPPERCASE_LETTER) {
            return word.toUpperCase();
        } else if (caseType == Character.LOWERCASE_LETTER) {
            return word.toLowerCase();
        } else if (caseType == Character.TITLECASE_LETTER) {
            StringBuffer sb = new StringBuffer();
            for (int i = 0, n = word.length(); i < n; i++) {
                String s = word.substring(i, i + 1);
                if (i == 0) {
                    sb.append(s.toUpperCase());
                } else {
                    sb.append(s.toLowerCase());
                }
            }
            return sb.toString();
        } else {
            return word;
        }
    }

    private static String joinWords(List<String> words, String separator) {
        StringBuffer sb = new StringBuffer();
        for (String w : words) {
            if (sb.length() > 0) {
                sb.append(separator);
            }
            sb.append(w);
        }
        return sb.toString();
    }

    /**
     * Special number formatter.
     */
    interface SpecialNumberFormatter {
        /**
         * Format number with special numeral system.
         * @param number to be formatted
         * @param one unicode scalar value denoting numeric value 1
         * @param letterValue letter value (must be one of the above letter value enumeration values)
         * @param features features (feature sub-parameters)
         * @param language denotes applicable language
         * @param country denotes applicable country
         * @return formatted number as array of unicode scalars
         */
        Integer[] format(long number, int one, int letterValue, String features, String language, String country);
    }

    /**
     * English Word Numerals
     */
    private static String[] englishWordOnes = { "zero", "one", "two", "three", "four", "five", "six", "seven", "eight", "nine" };
    private static String[] englishWordTeens = { "ten", "eleven", "twelve", "thirteen", "fourteen", "fifteen", "sixteen", "seventeen", "eighteen", "nineteen" };
    private static String[] englishWordTens = { "", "ten", "twenty", "thirty", "forty", "fifty", "sixty", "seventy", "eighty", "ninety" };
    private static String[] englishWordOthers = { "hundred", "thousand", "million", "billion" };
    private static String[] englishWordOnesOrd = { "none", "first", "second", "third", "fourth", "fifth", "sixth", "seventh", "eighth", "ninth" };
    private static String[] englishWordTeensOrd = { "tenth", "eleventh", "twelfth", "thirteenth", "fourteenth", "fifteenth", "sixteenth", "seventeenth", "eighteenth", "nineteenth" };
    private static String[] englishWordTensOrd = { "", "tenth", "twentieth", "thirtieth", "fortieth", "fiftieth", "sixtieth", "seventieth", "eightieth", "ninetith" };
    private static String[] englishWordOthersOrd = { "hundredth", "thousandth", "millionth", "billionth" };
    private static class EnglishNumberAsWordFormatter implements SpecialNumberFormatter {
        private int caseType = Character.UPPERCASE_LETTER;
        EnglishNumberAsWordFormatter(int caseType) {
            this.caseType = caseType;
        }
        public Integer[] format(long number, int one, int letterValue, String features, String language, String country) {
            List<String> wl = new ArrayList<String>();
            if (number >= 1000000000000L) {
                return null;
            } else {
                boolean ordinal = hasFeature(features, "ordinal");
                if (number == 0) {
                    wl.add(englishWordOnes [ 0 ]);
                } else if (ordinal && (number < 10)) {
                    wl.add(englishWordOnesOrd [ (int) number ]);
                } else {
                    int ones = (int) (number % 1000);
                    int thousands = (int) ((number / 1000) % 1000);
                    int millions = (int) ((number / 1000000) % 1000);
                    int billions = (int) ((number / 1000000000) % 1000);
                    if (billions > 0) {
                        wl = formatOnesInThousand(wl, billions);
                        if (ordinal && ((number % 1000000000) == 0)) {
                            wl.add(englishWordOthersOrd[3]);
                        } else {
                            wl.add(englishWordOthers[3]);
                        }
                    }
                    if (millions > 0) {
                        wl = formatOnesInThousand(wl, millions);
                        if (ordinal && ((number % 1000000) == 0)) {
                            wl.add(englishWordOthersOrd[2]);
                        } else {
                            wl.add(englishWordOthers[2]);
                        }
                    }
                    if (thousands > 0) {
                        wl = formatOnesInThousand(wl, thousands);
                        if (ordinal && ((number % 1000) == 0)) {
                            wl.add(englishWordOthersOrd[1]);
                        } else {
                            wl.add(englishWordOthers[1]);
                        }
                    }
                    if (ones > 0) {
                        wl = formatOnesInThousand(wl, ones, ordinal);
                    }
                }
                wl = convertWordCase(wl, caseType);
                return UTF32.toUTF32(joinWords(wl, " "), 0, true);
            }
        }
        private List<String> formatOnesInThousand(List<String> wl, int number) {
            return formatOnesInThousand(wl, number, false);
        }
        private List<String> formatOnesInThousand(List<String> wl, int number, boolean ordinal) {
            assert number < 1000;
            int ones = number % 10;
            int tens = (number / 10) % 10;
            int hundreds = (number / 100) % 10;
            if (hundreds > 0) {
                wl.add(englishWordOnes [ hundreds ]);
                if (ordinal && ((number % 100) == 0)) {
                    wl.add(englishWordOthersOrd[0]);
                } else {
                    wl.add(englishWordOthers[0]);
                }
            }
            if (tens > 0) {
                if (tens == 1) {
                    if (ordinal) {
                        wl.add(englishWordTeensOrd [ ones ]);
                    } else {
                        wl.add(englishWordTeens [ ones ]);
                    }
                } else {
                    if (ordinal && (ones == 0)) {
                        wl.add(englishWordTensOrd [ tens ]);
                    } else {
                        wl.add(englishWordTens [ tens ]);
                    }
                    if (ones > 0) {
                        if (ordinal) {
                            wl.add(englishWordOnesOrd [ ones ]);
                        } else {
                            wl.add(englishWordOnes [ ones ]);
                        }
                    }
                }
            } else if (ones > 0) {
                if (ordinal) {
                    wl.add(englishWordOnesOrd [ ones ]);
                } else {
                    wl.add(englishWordOnes [ ones ]);
                }
            }
            return wl;
        }
    }

    /**
     * French Word Numerals
     */
    private static String[] frenchWordOnes = { "z\u00e9ro", "un", "deux", "trois", "quatre", "cinq", "six", "sept", "huit", "neuf" };
    private static String[] frenchWordTeens = { "dix", "onze", "douze", "treize", "quatorze", "quinze", "seize", "dix-sept", "dix-huit", "dix-neuf" };
    private static String[] frenchWordTens = { "", "dix", "vingt", "trente", "quarante", "cinquante", "soixante", "soixante-dix", "quatre-vingt", "quatre-vingt-dix" };
    private static String[] frenchWordOthers = { "cent", "cents", "mille", "million", "millions", "milliard", "milliards" };
    private static String[] frenchWordOnesOrdMale = { "premier", "deuxi\u00e8me", "troisi\u00e8me", "quatri\u00e8me", "cinqui\u00e8me", "sixi\u00e8me", "septi\u00e8me", "huiti\u00e8me", "neuvi\u00e8me", "dixi\u00e8me" };
    private static String[] frenchWordOnesOrdFemale = { "premi\u00e8re", "deuxi\u00e8me", "troisi\u00e8me", "quatri\u00e8me", "cinqui\u00e8me", "sixi\u00e8me", "septi\u00e8me", "huiti\u00e8me", "neuvi\u00e8me", "dixi\u00e8me" };
    private static class FrenchNumberAsWordFormatter implements SpecialNumberFormatter {
        private int caseType = Character.UPPERCASE_LETTER;
        FrenchNumberAsWordFormatter(int caseType) {
            this.caseType = caseType;
        }
        public Integer[] format(long number, int one, int letterValue, String features, String language, String country) {
            List<String> wl = new ArrayList<String>();
            if (number >= 1000000000000L) {
                return null;
            } else {
                boolean ordinal = hasFeature(features, "ordinal");
                if (number == 0) {
                    wl.add(frenchWordOnes [ 0 ]);
                } else if (ordinal && (number <= 10)) {
                    boolean female = hasFeature(features, "female");
                    if (female) {
                        wl.add(frenchWordOnesOrdFemale [ (int) number ]);
                    } else {
                        wl.add(frenchWordOnesOrdMale [ (int) number ]);
                    }
                } else {
                    int ones = (int) (number % 1000);
                    int thousands = (int) ((number / 1000) % 1000);
                    int millions = (int) ((number / 1000000) % 1000);
                    int billions = (int) ((number / 1000000000) % 1000);
                    if (billions > 0) {
                        wl = formatOnesInThousand(wl, billions);
                        if (billions == 1) {
                            wl.add(frenchWordOthers[5]);
                        } else {
                            wl.add(frenchWordOthers[6]);
                        }
                    }
                    if (millions > 0) {
                        wl = formatOnesInThousand(wl, millions);
                        if (millions == 1) {
                            wl.add(frenchWordOthers[3]);
                        } else {
                            wl.add(frenchWordOthers[4]);
                        }
                    }
                    if (thousands > 0) {
                        if (thousands > 1) {
                            wl = formatOnesInThousand(wl, thousands);
                        }
                        wl.add(frenchWordOthers[2]);
                    }
                    if (ones > 0) {
                        wl = formatOnesInThousand(wl, ones);
                    }
                }
                wl = convertWordCase(wl, caseType);
                return UTF32.toUTF32(joinWords(wl, " "), 0, true);
            }
        }
        private List<String> formatOnesInThousand(List<String> wl, int number) {
            assert number < 1000;
            int ones = number % 10;
            int tens = (number / 10) % 10;
            int hundreds = (number / 100) % 10;
            if (hundreds > 0) {
                if (hundreds > 1) {
                    wl.add(frenchWordOnes [ hundreds ]);
                }
                if ((hundreds > 1) && (tens == 0) && (ones == 0)) {
                    wl.add(frenchWordOthers[1]);
                } else {
                    wl.add(frenchWordOthers[0]);
                }
            }
            if (tens > 0) {
                if (tens == 1) {
                    wl.add(frenchWordTeens [ ones ]);
                } else if (tens < 7) {
                    if (ones == 1) {
                        wl.add(frenchWordTens [ tens ]);
                        wl.add("et");
                        wl.add(frenchWordOnes [ ones ]);
                    } else {
                        StringBuffer sb = new StringBuffer();
                        sb.append(frenchWordTens [ tens ]);
                        if (ones > 0) {
                            sb.append('-');
                            sb.append(frenchWordOnes [ ones ]);
                        }
                        wl.add(sb.toString());
                    }
                } else if (tens == 7) {
                    if (ones == 1) {
                        wl.add(frenchWordTens [ 6 ]);
                        wl.add("et");
                        wl.add(frenchWordTeens [ ones ]);
                    } else {
                        StringBuffer sb = new StringBuffer();
                        sb.append(frenchWordTens [ 6 ]);
                        sb.append('-');
                        sb.append(frenchWordTeens [ ones ]);
                        wl.add(sb.toString());
                    }
                } else if (tens == 8) {
                    StringBuffer sb = new StringBuffer();
                    sb.append(frenchWordTens [ tens ]);
                    if (ones > 0) {
                        sb.append('-');
                        sb.append(frenchWordOnes [ ones ]);
                    } else {
                        sb.append('s');
                    }
                    wl.add(sb.toString());
                } else if (tens == 9) {
                    StringBuffer sb = new StringBuffer();
                    sb.append(frenchWordTens [ 8 ]);
                    sb.append('-');
                    sb.append(frenchWordTeens [ ones ]);
                    wl.add(sb.toString());
                }
            } else if (ones > 0) {
                wl.add(frenchWordOnes [ ones ]);
            }
            return wl;
        }
    }

    /**
     * Spanish Word Numerals
     */
    private static String[] spanishWordOnes = { "cero", "uno", "dos", "tres", "cuatro", "cinco", "seise", "siete", "ocho", "nueve" };
    private static String[] spanishWordTeens = { "diez", "once", "doce", "trece", "catorce", "quince", "diecis\u00e9is", "diecisiete", "dieciocho", "diecinueve" };
    private static String[] spanishWordTweens = { "veinte", "veintiuno", "veintid\u00f3s", "veintitr\u00e9s", "veinticuatro", "veinticinco", "veintis\u00e9is", "veintisiete", "veintiocho", "veintinueve" };
    private static String[] spanishWordTens = { "", "diez", "veinte", "treinta", "cuarenta", "cincuenta", "sesenta", "setenta", "ochenta", "noventa" };
    private static String[] spanishWordHundreds = { "", "ciento", "doscientos", "trescientos", "cuatrocientos", "quinientos", "seiscientos", "setecientos", "ochocientos", "novecientos" };
    private static String[] spanishWordOthers = { "un", "cien", "mil", "mill\u00f3n", "millones" };
    private static String[] spanishWordOnesOrdMale = { "ninguno", "primero", "segundo", "tercero", "cuarto", "quinto", "sexto", "s\u00e9ptimo", "octavo", "novento", "d\u00e9cimo" };
    private static String[] spanishWordOnesOrdFemale = { "ninguna", "primera", "segunda", "tercera", "cuarta", "quinta", "sexta", "s\u00e9ptima", "octava", "noventa", "d\u00e9cima" };
    private static class SpanishNumberAsWordFormatter implements SpecialNumberFormatter {
        private int caseType = Character.UPPERCASE_LETTER;
        SpanishNumberAsWordFormatter(int caseType) {
            this.caseType = caseType;
        }
        public Integer[] format(long number, int one, int letterValue, String features, String language, String country) {
            List<String> wl = new ArrayList<String>();
            if (number >= 1000000000000L) {
                return null;
            } else {
                boolean ordinal = hasFeature(features, "ordinal");
                if (number == 0) {
                    wl.add(spanishWordOnes [ 0 ]);
                } else if (ordinal && (number <= 10)) {
                    boolean female = hasFeature(features, "female");
                    if (female) {
                        wl.add(spanishWordOnesOrdFemale [ (int) number ]);
                    } else {
                        wl.add(spanishWordOnesOrdMale [ (int) number ]);
                    }
                } else {
                    int ones = (int) (number % 1000);
                    int thousands = (int) ((number / 1000) % 1000);
                    int millions = (int) ((number / 1000000) % 1000);
                    int billions = (int) ((number / 1000000000) % 1000);
                    if (billions > 0) {
                        if (billions > 1) {
                            wl = formatOnesInThousand(wl, billions);
                        }
                        wl.add(spanishWordOthers[2]);
                        wl.add(spanishWordOthers[4]);
                    }
                    if (millions > 0) {
                        if (millions == 1) {
                            wl.add(spanishWordOthers[0]);
                        } else {
                            wl = formatOnesInThousand(wl, millions);
                        }
                        if (millions > 1) {
                            wl.add(spanishWordOthers[4]);
                        } else {
                            wl.add(spanishWordOthers[3]);
                        }
                    }
                    if (thousands > 0) {
                        if (thousands > 1) {
                            wl = formatOnesInThousand(wl, thousands);
                        }
                        wl.add(spanishWordOthers[2]);
                    }
                    if (ones > 0) {
                        wl = formatOnesInThousand(wl, ones);
                    }
                }
                wl = convertWordCase(wl, caseType);
                return UTF32.toUTF32(joinWords(wl, " "), 0, true);
            }
        }
        private List<String> formatOnesInThousand(List<String> wl, int number) {
            assert number < 1000;
            int ones = number % 10;
            int tens = (number / 10) % 10;
            int hundreds = (number / 100) % 10;
            if (hundreds > 0) {
                if ((hundreds == 1) && (tens == 0) && (ones == 0)) {
                    wl.add(spanishWordOthers[1]);
                } else {
                    wl.add(spanishWordHundreds [ hundreds ]);
                }
            }
            if (tens > 0) {
                if (tens == 1) {
                    wl.add(spanishWordTeens [ ones ]);
                } else if (tens == 2) {
                    wl.add(spanishWordTweens [ ones ]);
                } else {
                    wl.add(spanishWordTens [ tens ]);
                    if (ones > 0) {
                        wl.add("y");
                        wl.add(spanishWordOnes [ ones ]);
                    }
                }
            } else if (ones > 0) {
                wl.add(spanishWordOnes [ ones ]);
            }
            return wl;
        }
    }

    /**
     * Roman (Latin) Numerals
     */
    private static int[] romanMapping = {
        100000,
        90000,
        50000,
        40000,
        10000,
        9000,
        5000,
        4000,
        1000,
        900,
        500,
        400,
        100,
        90,
        50,
        40,
        10,
        9,
        8,
        7,
        6,
        5,
        4,
        3,
        2,
        1
    };
    private static String[] romanStandardForms = {
        null,
        null,
        null,
        null,
        null,
        null,
        null,
        null,
        "m",
        "cm",
        "d",
        "cd",
        "c",
        "xc",
        "l",
        "xl",
        "x",
        "ix",
        null,
        null,
        null,
        "v",
        "iv",
        null,
        null,
        "i"
    };
    private static String[] romanLargeForms = {
        "\u2188",
        "\u2182\u2188",
        "\u2187",
        "\u2182\u2187",
        "\u2182",
        "\u2180\u2182",
        "\u2181",
        "\u2180\u2181",
        "m",
        "cm",
        "d",
        "cd",
        "c",
        "xc",
        "l",
        "xl",
        "x",
        "ix",
        null,
        null,
        null,
        "v",
        "iv",
        null,
        null,
        "i"
    };
    private static String[] romanNumberForms = {
        "\u2188",
        "\u2182\u2188",
        "\u2187",
        "\u2182\u2187",
        "\u2182",
        "\u2180\u2182",
        "\u2181",
        "\u2180\u2181",
        "\u216F",
        "\u216D\u216F",
        "\u216E",
        "\u216D\u216E",
        "\u216D",
        "\u2169\u216D",
        "\u216C",
        "\u2169\u216C",
        "\u2169",
        "\u2168",
        "\u2167",
        "\u2166",
        "\u2165",
        "\u2164",
        "\u2163",
        "\u2162",
        "\u2161",
        "\u2160"
    };
    private static class RomanNumeralsFormatter implements SpecialNumberFormatter {
        public Integer[] format(long number, int one, int letterValue, String features, String language, String country) {
            List<Integer> sl = new ArrayList<Integer>();
            if (number == 0) {
                return null;
            } else {
                String[] forms;
                int maxNumber;
                if (hasFeature(features, "unicode-number-forms")) {
                    forms = romanNumberForms;
                    maxNumber = 199999;
                } else if (hasFeature(features, "large")) {
                    forms = romanLargeForms;
                    maxNumber = 199999;
                } else {
                    forms = romanStandardForms;
                    maxNumber = 4999;
                }
                if (number > maxNumber) {
                    return null;
                } else {
                    while (number > 0) {
                        for (int i = 0, n = romanMapping.length; i < n; i++) {
                            int d = romanMapping [ i ];
                            if ((number >= d) && (forms [ i ] != null)) {
                                appendScalars(sl, UTF32.toUTF32(forms [ i ], 0, true));
                                number = number - d;
                                break;
                            }
                        }
                    }
                    if (one == (int) 'I') {
                        return toUpperCase(sl.toArray(new Integer [ sl.size() ]));
                    } else if (one == (int) 'i') {
                        return toLowerCase(sl.toArray(new Integer [ sl.size() ]));
                    } else {
                        return null;
                    }
                }
            }
        }
    }

    /**
     * Isopsephry (Greek) Numerals
     */
    private static class IsopsephryNumeralsFormatter implements SpecialNumberFormatter {
        public Integer[] format(long number, int one, int letterValue, String features, String language, String country) {
            return null;
        }
    }

    /**
     * Gematria (Hebrew) Numerals
     */
    private static int[] hebrewGematriaAlphabeticMap = {
        // ones
        0x05D0, // ALEF
        0x05D1, // BET
        0x05D2, // GIMEL
        0x05D3, // DALET
        0x05D4, // HE
        0x05D5, // VAV
        0x05D6, // ZAYIN
        0x05D7, // HET
        0x05D8, // TET
        // tens
        0x05D9, // YOD
        0x05DB, // KAF
        0x05DC, // LAMED
        0x05DE, // MEM
        0x05E0, // NUN
        0x05E1, // SAMEKH
        0x05E2, // AYIN
        0x05E4, // PE
        0x05E6, // TSADHI
        // hundreds
        0x05E7, // QOF
        0x05E8, // RESH
        0x05E9, // SHIN
        0x05EA, // TAV
        0x05DA, // FINAL KAF
        0x05DD, // FINAL MEM
        0x05DF, // FINAL NUN
        0x05E3, // FINAL PE
        0x05E5, // FINAL TSADHI
    };
    private class GematriaNumeralsFormatter implements SpecialNumberFormatter {
        public Integer[] format(long number, int one, int letterValue, String features, String language, String country) {
            if (one == 0x05D0) {
                if (letterValue == LETTER_VALUE_ALPHABETIC) {
                    return formatNumberAsSequence(number, one, hebrewGematriaAlphabeticMap.length, hebrewGematriaAlphabeticMap);
                } else if (letterValue == LETTER_VALUE_TRADITIONAL) {
                    if ((number == 0) || (number > 1999)) {
                        return null;
                    } else {
                        return formatAsGematriaNumber(number, features, language, country);
                    }
                } else {
                    return null;
                }
            } else {
                return null;
            }
        }
        private Integer[] formatAsGematriaNumber(long number, String features, String language, String country) {
            List<Integer> sl = new ArrayList<Integer>();
            assert hebrewGematriaAlphabeticMap.length == 27;
            assert hebrewGematriaAlphabeticMap[0] == 0x05D0;  // ALEF
            assert hebrewGematriaAlphabeticMap[21] == 0x05EA; // TAV
            assert number != 0;
            assert number < 2000;
            int[] map = hebrewGematriaAlphabeticMap;
            int thousands = (int) ((number / 1000) % 10);
            int hundreds = (int) ((number / 100) % 10);
            int tens = (int) ((number / 10) % 10);
            int ones = (int) ((number / 1) % 10);
            if (thousands > 0) {
                sl.add(map [ 0 + (thousands - 1) ]);
                sl.add(0x05F3);
            }
            if (hundreds > 0) {
                if (hundreds < 5) {
                    sl.add(map [ 18 + (hundreds - 1) ]);
                } else if (hundreds < 9) {
                    sl.add(map [ 18 + (4 - 1) ]);
                    sl.add(0x05F4);
                    sl.add(map [ 18 + (hundreds - 5) ]);
                } else if (hundreds == 9) {
                    sl.add(map [ 18 + (4 - 1) ]);
                    sl.add(map [ 18 + (4 - 1) ]);
                    sl.add(0x05F4);
                    sl.add(map [ 18 + (hundreds - 9) ]);
                }
                assert hundreds < 10;
            }
            if (number == 15) {
                sl.add(map [ 9 - 1]);
                sl.add(0x05F4);
                sl.add(map [ 6 - 1]);
            } else if (number == 16) {
                sl.add(map [ 9 - 1 ]);
                sl.add(0x05F4);
                sl.add(map [ 7 - 1 ]);
            } else {
                if (tens > 0) {
                    assert tens < 10;
                    sl.add(map [ 9 + (tens - 1) ]);
                }
                if (ones > 0) {
                    assert ones < 10;
                    sl.add(map [ 0 + (ones - 1) ]);
                }
            }
            return sl.toArray(new Integer [ sl.size() ]);
        }
    }

    /**
     * Arabic Numerals
     */
    private static int[] arabicAbjadiAlphabeticMap = {
        // ones
        0x0623, // ALEF WITH HAMZA ABOVE
        0x0628, // BEH
        0x062C, // JEEM
        0x062F, // DAL
        0x0647, // HEH
        0x0648, // WAW
        0x0632, // ZAIN
        0x062D, // HAH
        0x0637, // TAH
        // tens
        0x0649, // ALEF MAQSURA
        0x0643, // KAF
        0x0644, // LAM
        0x0645, // MEEM
        0x0646, // NOON
        0x0633, // SEEN
        0x0639, // AIN
        0x0641, // FEH
        0x0635, // SAD
        // hundreds
        0x0642, // QAF
        0x0631, // REH
        0x0634, // SHEEN
        0x062A, // TEH
        0x062B, // THEH
        0x062E, // KHAH
        0x0630, // THAL
        0x0636, // DAD
        0x0638, // ZAH
        // thousands
        0x063A, // GHAIN
    };
    private static int[] arabicHijaiAlphabeticMap = {
        0x0623, // ALEF WITH HAMZA ABOVE
        0x0628, // BEH
        0x062A, // TEH
        0x062B, // THEH
        0x062C, // JEEM
        0x062D, // HAH
        0x062E, // KHAH
        0x062F, // DAL
        0x0630, // THAL
        0x0631, // REH
        0x0632, // ZAIN
        0x0633, // SEEN
        0x0634, // SHEEN
        0x0635, // SAD
        0x0636, // DAD
        0x0637, // TAH
        0x0638, // ZAH
        0x0639, // AIN
        0x063A, // GHAIN
        0x0641, // FEH
        0x0642, // QAF
        0x0643, // KAF
        0x0644, // LAM
        0x0645, // MEEM
        0x0646, // NOON
        0x0647, // HEH
        0x0648, // WAW
        0x0649, // ALEF MAQSURA
    };
    private class ArabicNumeralsFormatter implements SpecialNumberFormatter {
        public Integer[] format(long number, int one, int letterValue, String features, String language, String country) {
            if (one == 0x0627) {
                int[] map;
                if (letterValue == LETTER_VALUE_TRADITIONAL) {
                    map = arabicAbjadiAlphabeticMap;
                } else if (letterValue == LETTER_VALUE_ALPHABETIC) {
                    map = arabicHijaiAlphabeticMap;
                } else {
                    map = arabicAbjadiAlphabeticMap;
                }
                return formatNumberAsSequence(number, one, map.length, map);
            } else if (one == 0x0623) {
                if ((number == 0) || (number > 1999)) {
                    return null;
                } else {
                    return formatAsAbjadiNumber(number, features, language, country);
                }
            } else {
                return null;
            }
        }
        private Integer[] formatAsAbjadiNumber(long number, String features, String language, String country) {
            List<Integer> sl = new ArrayList<Integer>();
            assert arabicAbjadiAlphabeticMap.length == 28;
            assert arabicAbjadiAlphabeticMap[0] == 0x0623;  // ALEF WITH HAMZA ABOVE
            assert arabicAbjadiAlphabeticMap[27] == 0x063A; // GHAIN
            assert number != 0;
            assert number < 2000;
            int[] map = arabicAbjadiAlphabeticMap;
            int thousands = (int) ((number / 1000) % 10);
            int hundreds = (int) ((number / 100) % 10);
            int tens = (int) ((number / 10) % 10);
            int ones = (int) ((number / 1) % 10);
            if (thousands > 0) {
                assert thousands < 2;
                sl.add(map [ 27 + (thousands - 1) ]);
            }
            if (hundreds > 0) {
                assert thousands < 10;
                sl.add(map [ 18 + (hundreds - 1) ]);
            }
            if (tens > 0) {
                assert tens < 10;
                sl.add(map [ 9 + (tens - 1) ]);
            }
            if (ones > 0) {
                assert ones < 10;
                sl.add(map [ 0 + (ones - 1) ]);
            }
            return sl.toArray(new Integer [ sl.size() ]);
        }
    }

    /**
     * Kana (Japanese) Numerals
     */
    private static int[] hiraganaGojuonAlphabeticMap = {
        0x3042, // A
        0x3044, // I
        0x3046, // U
        0x3048, // E
        0x304A, // O
        0x304B, // KA
        0x304D, // KI
        0x304F, // KU
        0x3051, // KE
        0x3053, // KO
        0x3055, // SA
        0x3057, // SI
        0x3059, // SU
        0x305B, // SE
        0x305D, // SO
        0x305F, // TA
        0x3061, // TI
        0x3064, // TU
        0x3066, // TE
        0x3068, // TO
        0x306A, // NA
        0x306B, // NI
        0x306C, // NU
        0x306D, // NE
        0x306E, // NO
        0x306F, // HA
        0x3072, // HI
        0x3075, // HU
        0x3078, // HE
        0x307B, // HO
        0x307E, // MA
        0x307F, // MI
        0x3080, // MU
        0x3081, // ME
        0x3082, // MO
        0x3084, // YA
        0x3086, // YU
        0x3088, // YO
        0x3089, // RA
        0x308A, // RI
        0x308B, // RU
        0x308C, // RE
        0x308D, // RO
        0x308F, // WA
        0x3090, // WI
        0x3091, // WE
        0x3092, // WO
        0x3093, // N
    };
    private static int[] katakanaGojuonAlphabeticMap = {
        0x30A2, // A
        0x30A4, // I
        0x30A6, // U
        0x30A8, // E
        0x30AA, // O
        0x30AB, // KA
        0x30AD, // KI
        0x30AF, // KU
        0x30B1, // KE
        0x30B3, // KO
        0x30B5, // SA
        0x30B7, // SI
        0x30B9, // SU
        0x30BB, // SE
        0x30BD, // SO
        0x30BF, // TA
        0x30C1, // TI
        0x30C4, // TU
        0x30C6, // TE
        0x30C8, // TO
        0x30CA, // NA
        0x30CB, // NI
        0x30CC, // NU
        0x30CD, // NE
        0x30CE, // NO
        0x30CF, // HA
        0x30D2, // HI
        0x30D5, // HU
        0x30D8, // HE
        0x30DB, // HO
        0x30DE, // MA
        0x30DF, // MI
        0x30E0, // MU
        0x30E1, // ME
        0x30E2, // MO
        0x30E4, // YA
        0x30E6, // YU
        0x30E8, // YO
        0x30E9, // RA
        0x30EA, // RI
        0x30EB, // RU
        0x30EC, // RE
        0x30ED, // RO
        0x30EF, // WA
        0x30F0, // WI
        0x30F1, // WE
        0x30F2, // WO
        0x30F3, // N
    };
    private class KanaNumeralsFormatter implements SpecialNumberFormatter {
        public Integer[] format(long number, int one, int letterValue, String features, String language, String country) {
            if ((one == 0x3042) && (letterValue == LETTER_VALUE_ALPHABETIC)) {
                return formatNumberAsSequence(number, one, hiraganaGojuonAlphabeticMap.length, hiraganaGojuonAlphabeticMap);
            } else if ((one == 0x30A2) && (letterValue == LETTER_VALUE_ALPHABETIC)) {
                return formatNumberAsSequence(number, one, katakanaGojuonAlphabeticMap.length, katakanaGojuonAlphabeticMap);
            } else {
                return null;
            }
        }
    }

    /**
     * Thai Numerals
     */
    private static int[] thaiAlphabeticMap = {
        0x0E01,
        0x0E02,
        0x0E03,
        0x0E04,
        0x0E05,
        0x0E06,
        0x0E07,
        0x0E08,
        0x0E09,
        0x0E0A,
        0x0E0B,
        0x0E0C,
        0x0E0D,
        0x0E0E,
        0x0E0F,
        0x0E10,
        0x0E11,
        0x0E12,
        0x0E13,
        0x0E14,
        0x0E15,
        0x0E16,
        0x0E17,
        0x0E18,
        0x0E19,
        0x0E1A,
        0x0E1B,
        0x0E1C,
        0x0E1D,
        0x0E1E,
        0x0E1F,
        0x0E20,
        0x0E21,
        0x0E22,
        0x0E23,
        // 0x0E24, // RU - not used in modern sequence
        0x0E25,
        // 0x0E26, // LU - not used in modern sequence
        0x0E27,
        0x0E28,
        0x0E29,
        0x0E2A,
        0x0E2B,
        0x0E2C,
        0x0E2D,
        0x0E2E,
    };
    private class ThaiNumeralsFormatter implements SpecialNumberFormatter {
        public Integer[] format(long number, int one, int letterValue, String features, String language, String country) {
            if ((one == 0x0E01) && (letterValue == LETTER_VALUE_ALPHABETIC)) {
                return formatNumberAsSequence(number, one, thaiAlphabeticMap.length, thaiAlphabeticMap);
            } else {
                return null;
            }
        }
    }

}
