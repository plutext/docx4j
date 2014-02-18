package org.docx4j.fonts;

/**
 * Certain fonts have CJK names in a docx.
 * However, in Win 7 (English locale at least), the font names are in English.
 * So this class translates the docx CJK font name to a physical font name.
 *  
 * @author jharrop
 *
 */
public class CJKToEnglish {
	
	/**
	 * Convert a CJK font name (as used in docx) to an English font name
	 * (as used on Win 7, English locale at least).
	 * 
	 * @param fontName
	 * @return
	 */
	public static String toEnglish(String fontName) {
		
		// Special cases; there are more; see http://en.wikipedia.org/wiki/List_of_CJK_fonts
		if (fontName.equals("ＭＳ ゴシック")) {
//	        <a:font script="Jpan" typeface="ＭＳ ゴシック"/>
			return "MS Gothic";
		} else if (fontName.equals("ＭＳ 明朝")) {			
			return "MS Mincho";
		} else if (fontName.equals("맑은 고딕")) {
//	        <a:font script="Hang" typeface="맑은 고딕"/>
			return "Malgun Gothic";
		} else if (fontName.equals("宋体")) {
//	        <a:font script="Hans" typeface="宋体"/>
			return "SimSun"; 			
		} else if (fontName.equals("新細明體")) { 
			return "PMingLiU"; // according to http://zh.wikipedia.org/wiki/%E5%AE%8B%E4%BD%93
		} else {
			return null;
		}		
	}

}
