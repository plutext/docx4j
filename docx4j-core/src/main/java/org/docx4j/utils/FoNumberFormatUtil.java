package org.docx4j.utils;

public class FoNumberFormatUtil {
	
    // constants
    public static final String FO_PAGENUMBER_DECIMAL    = "1";       // '1'
    public static final String FO_PAGENUMBER_LOWERALPHA = "a";    // 'a'
    public static final String FO_PAGENUMBER_UPPERALPHA = "A";    // 'A'
    public static final String FO_PAGENUMBER_LOWERROMAN = "i";    // 'i'
    public static final String FO_PAGENUMBER_UPPERROMAN = "I";    // 'I'

	
	/** Format a page number the way fo would do it, taken from PageNumberGenerator
	 *  of Apache-Fop 1.0
	 * 
	 * @param pageNumber to be formatted
	 * @param foFormat fo format name
	 * @return formatted page number (or null if pageNumber < 0)
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
}
