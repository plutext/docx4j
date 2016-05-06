package org.docx4j.anon;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import org.docx4j.openpackaging.parts.Part;
import org.docx4j.wml.CTLanguage;

public class AnonymizeResult {

	public boolean hasGreek = false;
	public boolean hasCyrillic = false;
	public boolean hasHebrew = false;
	public boolean hasArabic = false;
	public boolean hasHiragana = false;
	public boolean hasKatakana = false;
	public boolean hasCJK = false;
	
//    // Useful to capture this for analysis purposes
//	CTLanguage themeFontLang = null;	
//	
//	/**
//	 * What language is this docx?
//	 * @return
//	 */
//	public CTLanguage getLang() {
//		return themeFontLang;
//	}
//	
//	String mostPopularLang;
//	public String getMostPopularLang() {
//		return mostPopularLang;
//	}
//	
//	boolean rtl = false;
//
//	public boolean isRtl() {
//		return rtl;
//	}

	/**
	 * Return parts which are potentially unsafe
	 * 
	 * @return
	 */
	public HashSet<Part> getUnsafeParts() {
		return unsafeParts;
	}
	HashSet<Part> unsafeParts;

	/**
	 * Return objects which are potentially unsafe, by story part
	 * 
	 * @return
	 */
	public HashMap<Part, Set<Object>> getUnsafeObjectsByPart() {
		return unsafeObjectsByPart;
	}
	HashMap<Part, Set<Object>> unsafeObjectsByPart = new HashMap<Part, Set<Object>>(); 

	boolean anyUnsafeObjects = false;

	/**
	 * Convenience object to record whether any story part contains an unsafe object.
	 * If so, use getUnsafeObjectsByPart() 
	 */
	public boolean hasAnyUnsafeObjects() {
		return anyUnsafeObjects;
	}

	/**
	 * Return misc interesting objects, by story part, for further consideration
	 * @return
	 */
	public HashMap<Part, Set<Object>> getInventoryObjectsByPart() {
		return inventoryObjectsByPart;
	}
	HashMap<Part, Set<Object>> inventoryObjectsByPart = new HashMap<Part, Set<Object>>(); 

	/**
	 * Whether this docx contains VML
	 * 
	 * @return
	 */
	public boolean containsVML() {
		return containsVML;
	}
	boolean containsVML = false;
	
	HashSet<String> fieldsPresent = null;
	
	public HashSet<String> getFieldsPresent() {
		return fieldsPresent;
	}

	/**
	 * @return
	 */
	public boolean isOK() {
		
		return ((unsafeParts.size()==0)
				&& (anyUnsafeObjects == false));
		
	}
	
}
