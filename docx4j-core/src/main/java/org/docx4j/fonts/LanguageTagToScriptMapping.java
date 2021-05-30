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
	

	public static String getScriptForLanguageTag(String langTag) {
		
		String lang = langTag;
		int pos = lang.indexOf('-');
		if (pos>-1) {
			lang = lang.substring(0, pos);
		}
		
		log.debug("lang: " + lang);
		
		//    <a:font script="Jpan" typeface="ＭＳ 明朝"/>
		if (lang.equals("ja")) {
			return "Jpan";
		}
		
		//    <a:font script="Hang" typeface="맑은 고딕"/>
		if (lang.equals("ko")) {
			return "Hang";
		}

		if (lang.equals("zh")) {
		
			//    <a:font script="Hans" typeface="宋体"/>
			if (langTag.equals("zh-CN")
					|| langTag.equals("zh-SG")) {
				// Mainland China and Singapore both use simplified characters
				return "Hans";
			} else {
				//    <a:font script="Hant" typeface="新細明體"/>
				// Chinese (Traditional Han)
				return "Hant";				
			}
		}
		
		//    <a:font script="Arab" typeface="Arial"/>
		if (lang.equals("ar")) {
			return "Arab";
		}
		
		//    <a:font script="Hebr" typeface="Arial"/>
		if ("he,yi,iw".contains(lang)) {
			return "Hebr";
		}
		
		//    <a:font script="Thai" typeface="Cordia New"/>
		if (lang.equals("th")) {
			return "Thai";
		}
		
		//    <a:font script="Ethi" typeface="Nyala"/>
		if ("ti,bwo,eth,kxh,mdy".contains(lang)) {
			return "Ethi";
		}
		
		//    <a:font script="Beng" typeface="Vrinda"/>
		if ("bn,as,mni".contains(lang)) {
			return "Beng";
		}
		
		//    <a:font script="Gujr" typeface="Shruti"/>
		if (lang.equals("gu")) {
			return "Gujr";			
		}
		
		//    <a:font script="Khmr" typeface="DaunPenh"/>
		if (lang.equals("km")) {
			return "Khmr";
		}
		
		//    <a:font script="Knda" typeface="Tunga"/>
		if (lang.equals("kn")) {
			return "Knda";
		}
		
		//    <a:font script="Guru" typeface="Raavi"/>
		if (lang.equals("pa")) {
			return "Guru";
		}
		
		//    <a:font script="Cans" typeface="Euphemia"/>
		if (lang.equals("iu")) {
			return "Cans";
		}
		
		//    <a:font script="Cher" typeface="Plantagenet Cherokee"/>
		if (lang.equals("chr")) {
			return "Cher";
		}
		
		// <a:font script="Yiii" typeface="Microsoft Yi Baiti"/> omitted
		// see http://en.wikipedia.org/wiki/Yi_script
		
		//    <a:font script="Tibt" typeface="Microsoft Himalaya"/>
		if (lang.equals("bo")) {
			return "Tibt";
		}
		
		//    <a:font script="Thaa" typeface="MV Boli"/>
		if (lang.equals("dv")) {
			return "Thaa";
		}
		
		//    <a:font script="Deva" typeface="Mangal"/>
		if ("hi,ks,kok,mr,ne,sa,sd".contains(lang)) {
			return "Deva";
		}
		
		//    <a:font script="Telu" typeface="Gautami"/>
		if (lang.equals("te")) {
			return "Telu";
		}
		
		//    <a:font script="Taml" typeface="Latha"/>
		if (lang.equals("ta")) {
			return "Taml";
		}
		
		//    <a:font script="Syrc" typeface="Estrangelo Edessa"/>
		if (lang.equals("syr")) {
			return "Syrc";
		}
		
		//    <a:font script="Orya" typeface="Kalinga"/>
		if (lang.equals("or")) {
			return "Orya";
		}
		
		//    <a:font script="Mlym" typeface="Kartika"/>
		if (lang.equals("ml")) {
			return "Mlym";
		}
		
		//    <a:font script="Laoo" typeface="DokChampa"/>
		if (lang.equals("lo")) {
			return "Laoo";
		}
		
		//    <a:font script="Sinh" typeface="Iskoola Pota"/>
		if (lang.equals("si")) {
			return "Sinh";
		}
		
		//    <a:font script="Mong" typeface="Mongolian Baiti"/>
		
		//    <a:font script="Viet" typeface="Arial"/>
		if (lang.equals("vi")
				|| lang.equals("lha")
				|| lang.equals("nut")
				) {
			return "Viet";
		}
		
		//    <a:font script="Uigh" typeface="Microsoft Uighur"/>
		
		//    <a:font script="Geor" typeface="Sylfaen"/>
		if (lang.equals("ka")) {
			return "Geor";
		}
		
		return null;
	}
	
}
