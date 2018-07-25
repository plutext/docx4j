package org.docx4j.unicode;


import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class UnicodeScriptDetector {

	public static void main(String[] args) {
		
System.out.println(percentage("חץ", Script.Hebrew));

	}
	
	
	public static boolean containsScript(String text, Script script) {

		String pattern = "\\p{Is" + script + "}";
		//System.out.println(pattern);
		Pattern p = Pattern.compile(pattern);
		Matcher m = p.matcher(text);	
		
		return m.find();
		
	}

	public static boolean isAll(String text, Script script) {

		// TODO skip punctuation, spaces?
		
		String pattern = "\\p{Is" + script + "}*";
		//System.out.println(pattern);
		Pattern p = Pattern.compile(pattern);
		Matcher m = p.matcher(text);	
		
		return m.matches();
		
	}

	public static int percentage(String text, Script script) {
		
		int count=0;
		int total = text.length();
		for( int i = 0; i<text.length(); i++) {
			
			// TODO skip punctuation, spaces?
			
			// TODO surrogates
			String glyph = text.charAt(i) + "";
			if (containsScript(glyph, script)) count++;
		}
		
		return Math.round(100*count/total);
	}
	
	public static boolean containsCJK(String text) {

		Pattern p = Pattern.compile("\\p{InHiragana}|\\p{InKatakana}|\\p{IsHan}|\\p{IsHangul}", Pattern.UNICODE_CASE);
		Matcher m = p.matcher(text);	
		
		return m.find();
		
	}

	// from https://gist.github.com/TheFinestArtist/2fd1b4aa1d4824fcbaef
    public static boolean hasKorean(CharSequence charSequence) {
        boolean hasKorean = false;
        for (char c : charSequence.toString().toCharArray()) {
            if (Character.UnicodeBlock.of(c) == Character.UnicodeBlock.HANGUL_JAMO
                    || Character.UnicodeBlock.of(c) == Character.UnicodeBlock.HANGUL_COMPATIBILITY_JAMO
                    || Character.UnicodeBlock.of(c) == Character.UnicodeBlock.HANGUL_SYLLABLES) {
                hasKorean = true;
                break;
            }
        }

        return hasKorean;
    }

    public static boolean hasJapanese(CharSequence charSequence) {
        boolean hasJapanese = false;
        for (char c : charSequence.toString().toCharArray()) {
            if (Character.UnicodeBlock.of(c) == Character.UnicodeBlock.CJK_UNIFIED_IDEOGRAPHS
                    || Character.UnicodeBlock.of(c) == Character.UnicodeBlock.HIRAGANA
                    || Character.UnicodeBlock.of(c) == Character.UnicodeBlock.KATAKANA
                    || Character.UnicodeBlock.of(c) == Character.UnicodeBlock.HALFWIDTH_AND_FULLWIDTH_FORMS
                    || Character.UnicodeBlock.of(c) == Character.UnicodeBlock.HALFWIDTH_AND_FULLWIDTH_FORMS
                    || Character.UnicodeBlock.of(c) == Character.UnicodeBlock.CJK_SYMBOLS_AND_PUNCTUATION) {
                hasJapanese = true;
                break;
            }
        }

        return hasJapanese;
    }	
//
//	public Script dominant(String text) {
//		
//	}
	
	
	
}
