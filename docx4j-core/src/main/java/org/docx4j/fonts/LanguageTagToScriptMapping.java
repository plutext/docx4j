package org.docx4j.fonts;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class LanguageTagToScriptMapping {

	protected static Logger log = LoggerFactory.getLogger(LanguageTagToScriptMapping.class);	
	
	/* The purpose of this class is to map an attribute value
	 * from w:themeFontLang in the settings part,  
	 * for example:
	 * 
	 *     <w:themeFontLang w:val="en-US" w:eastAsia="ko-KR"/>
	 * 
	 * to the value of @script in the theme part.
	 * 
	 *  <a:font script="Jpan" typeface="ＭＳ 明朝"/>
        <a:font script="Hang" typeface="맑은 고딕"/>
        <a:font script="Hans" typeface="宋体"/>
        <a:font script="Hant" typeface="新細明體"/>
        <a:font script="Arab" typeface="Arial"/>
        <a:font script="Hebr" typeface="Arial"/>
        <a:font script="Thai" typeface="Cordia New"/>
        <a:font script="Ethi" typeface="Nyala"/>
        <a:font script="Beng" typeface="Vrinda"/>
        <a:font script="Gujr" typeface="Shruti"/>
        <a:font script="Khmr" typeface="DaunPenh"/>
        <a:font script="Knda" typeface="Tunga"/>
        <a:font script="Guru" typeface="Raavi"/>
        <a:font script="Cans" typeface="Euphemia"/>
        <a:font script="Cher" typeface="Plantagenet Cherokee"/>
        <a:font script="Yiii" typeface="Microsoft Yi Baiti"/>
        <a:font script="Tibt" typeface="Microsoft Himalaya"/>
        <a:font script="Thaa" typeface="MV Boli"/>
        <a:font script="Deva" typeface="Mangal"/>
        <a:font script="Telu" typeface="Gautami"/>
        <a:font script="Taml" typeface="Latha"/>
        <a:font script="Syrc" typeface="Estrangelo Edessa"/>
        <a:font script="Orya" typeface="Kalinga"/>
        <a:font script="Mlym" typeface="Kartika"/>
        <a:font script="Laoo" typeface="DokChampa"/>
        <a:font script="Sinh" typeface="Iskoola Pota"/>
        <a:font script="Mong" typeface="Mongolian Baiti"/>
        <a:font script="Viet" typeface="Arial"/>
        <a:font script="Uigh" typeface="Microsoft Uighur"/>
        <a:font script="Geor" typeface="Sylfaen"/>
        
        See generally http://social.msdn.microsoft.com/Forums/en-US/ef7b4b55-9d38-4cd8-89f0-38389419c672/determining-font-script-from-majorminorfont-and-lang-and-text-in-drawingml
        and in particular the helpful xlsx kindly supplied by Todd Main.
        
	 */
	

	/** The script a primary language subtag selects in a theme's font list (the
	 *  {@code script} attribute of {@code a:font}), by exact subtag.  Until 17.1.1 this
	 *  was a substring test over comma-separated lists, so Estonian ({@code et}) matched
	 *  inside {@code eth} and took the Ethiopic face (Nyala), Mongolian ({@code mn})
	 *  inside {@code mni} the Bengali one, Wolof ({@code wo}) inside {@code bwo};
	 *  CR-016 probe fonts-theme-lang.  Chinese is decided on the region as well. */
	private static final java.util.Map<String, String> SCRIPT_BY_LANGUAGE;
	static {
		java.util.Map<String, String> m = new java.util.HashMap<String, String>();
		m.put("ja", "Jpan");
		m.put("ko", "Hang");
		m.put("ar", "Arab");
		for (String l : new String[] { "he", "yi", "iw" }) m.put(l, "Hebr");
		m.put("th", "Thai");
		for (String l : new String[] { "ti", "bwo", "eth", "kxh", "mdy" }) m.put(l, "Ethi");
		for (String l : new String[] { "bn", "as", "mni" }) m.put(l, "Beng");
		m.put("gu", "Gujr");
		m.put("km", "Khmr");
		m.put("kn", "Knda");
		m.put("pa", "Guru");
		m.put("iu", "Cans");
		m.put("chr", "Cher");
		// Yiii (Microsoft Yi Baiti) omitted; see http://en.wikipedia.org/wiki/Yi_script
		m.put("bo", "Tibt");
		m.put("dv", "Thaa");
		for (String l : new String[] { "hi", "ks", "kok", "mr", "ne", "sa", "sd" }) m.put(l, "Deva");
		m.put("te", "Telu");
		m.put("ta", "Taml");
		m.put("syr", "Syrc");
		m.put("or", "Orya");
		m.put("ml", "Mlym");
		m.put("lo", "Laoo");
		m.put("si", "Sinh");
		// Mong (Mongolian Baiti) and Uigh (Microsoft Uighur) omitted, as before
		for (String l : new String[] { "vi", "lha", "nut" }) m.put(l, "Viet");
		m.put("ka", "Geor");
		SCRIPT_BY_LANGUAGE = java.util.Collections.unmodifiableMap(m);
	}

	public static String getScriptForLanguageTag(String langTag) {
		
		if (langTag==null) return null;
		String lang = langTag.trim();
		int pos = lang.indexOf('-');
		if (pos<0) pos = lang.indexOf('_');
		if (pos>-1) {
			lang = lang.substring(0, pos);
		}
		lang = lang.toLowerCase();
		
		log.debug("lang: " + lang);
		
		if (lang.equals("zh")) {
			//    <a:font script="Hans" typeface="宋体"/>
			if (langTag.equalsIgnoreCase("zh-CN")
					|| langTag.equalsIgnoreCase("zh-SG")) {
				// Mainland China and Singapore both use simplified characters
				return "Hans";
			} else {
				//    <a:font script="Hant" typeface="新細明體"/>
				// Chinese (Traditional Han)
				return "Hant";				
			}
		}
		return SCRIPT_BY_LANGUAGE.get(lang);
	}
	
}
