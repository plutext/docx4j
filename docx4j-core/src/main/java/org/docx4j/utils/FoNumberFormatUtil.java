package org.docx4j.utils;

public class FoNumberFormatUtil {
	
    // constants
    public static final String FO_PAGENUMBER_DECIMAL    = "1";       // '1'
    public static final String FO_PAGENUMBER_LOWERALPHA = "a";    // 'a'
    public static final String FO_PAGENUMBER_UPPERALPHA = "A";    // 'A'
    public static final String FO_PAGENUMBER_LOWERROMAN = "i";    // 'i'
    public static final String FO_PAGENUMBER_UPPERROMAN = "I";    // 'I'

	
    // digit based format tokens; the last character is the digit one of the target
    // script, any preceding zeroes give the minimum number of digits
    public static final String FO_PAGENUMBER_THAI         = "\u0E51";  // THAI DIGIT ONE
    public static final String FO_PAGENUMBER_DEVANAGARI   = "\u0967";  // DEVANAGARI DIGIT ONE
    public static final String FO_PAGENUMBER_FULLWIDTH    = "\uFF11";  // FULLWIDTH DIGIT ONE
    public static final String FO_PAGENUMBER_DECIMAL_ZERO = "01";      // zero padded to two digits

	
	/** Format a page number the way fo would do it, taken from PageNumberGenerator
	 *  of Apache-Fop 1.0
	 * 
	 * <p>Any decimal digit format token is honoured, following the xsl:number
	 * format-token semantics: the last character of the token is the digit one of
	 * the target script (Latin, Thai, Devanagari, fullwidth ...), and any preceding
	 * zeroes give the minimum number of digits, so the token "01" yields
	 * 01, 02, ... 09, 10.
	 * 
	 * @param pageNumber to be formatted
	 * @param foFormat fo format name
	 * @return formatted page number (or null if pageNumber < 0)
	 * @since 17.0.3 non Latin digits and zero padded tokens
	 */
	public static String format(int pageNumber, String foFormat) {
	String ret = null;
		if (pageNumber > -1) {
			if (foFormat == null) foFormat = "1"; //default
			if (pageNumber == 1) {
				//shortcut for html, for 1 the foFormat and the result are the same
				ret = foFormat;
			}
			else {
		        if (FO_PAGENUMBER_DECIMAL.equals(foFormat)) {
		        	//formatting with leading zeroes omitted
		            ret = Integer.toString(pageNumber);
		        } else if (isDigitFormatToken(foFormat)) {
		            ret = makeDigits(pageNumber, foFormat);
		        } else if (FO_PAGENUMBER_LOWERROMAN.equals(foFormat) ||
		        		  FO_PAGENUMBER_UPPERROMAN.equals(foFormat)) {
		            ret = makeRoman(pageNumber);
		            if (FO_PAGENUMBER_UPPERROMAN.equals(foFormat)) {
		                ret = ret.toUpperCase();
		            }
		        } else {
		            // alphabetic
		            ret = makeAlpha(pageNumber);
		            if (FO_PAGENUMBER_UPPERALPHA.equals(foFormat)) {
		                ret = ret.toUpperCase();
		            }
		        }
			}
		}
		return ret;
	}

    private static String makeRoman(int num) {
        int[] arabic = {
            1000, 900, 500, 400, 100, 90, 50, 40, 10, 9, 5, 4, 1
        };
        String[] roman = {
            "m", "cm", "d", "cd", "c", "xc", "l", "xl", "x", "ix", "v", "iv",
            "i"
        };

        int i = 0;
        StringBuffer romanNumber = new StringBuffer();

        while (num > 0) {
            while (num >= arabic[i]) {
                num = num - arabic[i];
                romanNumber.append(roman[i]);
            }
            i = i + 1;
        }
        return romanNumber.toString();
    }

    private static String makeAlpha(int num) {
        String letters = "abcdefghijklmnopqrstuvwxyz";
        StringBuffer alphaNumber = new StringBuffer();

        int base = 26;
        int rem = 0;

        num--;
        if (num < base) {
            alphaNumber.append(letters.charAt(num));
        } else {
            while (num >= base) {
                rem = num % base;
                alphaNumber.append(letters.charAt(rem));
                num = num / base;
            }
            alphaNumber.append(letters.charAt(num - 1));
        }
        return alphaNumber.reverse().toString();
    }

    /** Is this a decimal digit format token, ie a string of decimal digits in which
     *  the last digit has the value 1 and any preceding digits have the value 0?
     */
    private static boolean isDigitFormatToken(String foFormat) {
        int len = foFormat.length();
        if (len == 0) {
            return false;
        }
        for (int i = 0; i < len; i++) {
            char c = foFormat.charAt(i);
            if (Character.isHighSurrogate(c) || Character.isLowSurrogate(c)) {
                // supplementary plane digits are not supported here
                return false;
            }
            int value = Character.digit(c, 10);
            if (value != (i == len - 1 ? 1 : 0)) {
                return false;
            }
        }
        return true;
    }

    /** Render num using the digits of the script the format token is written in,
     *  left padded with that script's zero to the length of the token.
     */
    private static String makeDigits(int num, String foFormat) {
        char zero = (char)(foFormat.charAt(foFormat.length() - 1) - 1);
        String decimal = Integer.toString(num);
        StringBuilder result = new StringBuilder();
        for (int i = decimal.length(); i < foFormat.length(); i++) {
            result.append(zero);
        }
        for (int i = 0; i < decimal.length(); i++) {
            result.append((char)(zero + (decimal.charAt(i) - '0')));
        }
        return result.toString();
    }
}
