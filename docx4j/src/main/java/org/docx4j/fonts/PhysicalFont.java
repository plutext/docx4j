/**
 * 
 */
package org.docx4j.fonts;

import org.apache.fop.fonts.EmbedFontInfo;
import org.apache.log4j.Logger;

/**
 * This class represents a font which is
 * available on the system.
 * 
 * It essentially wraps fop's EmbedFontInfo,
 * but names it using the name from the
 * triplet.
 * 
 * However, it extends that with Panose
 * info.  TODO: use reflection, so that
 * things don't fail if the fop jar
 * doesn't include fontInfo.getPanose()
 * or fontInfo.getEmbedFile()
 * 
 * @author dev
 *
 */
public class PhysicalFont {

	protected static Logger log = Logger.getLogger(PhysicalFont.class);		
	
	PhysicalFont(String name, EmbedFontInfo embedFontInfo) {
		
		try {
			// Sanity check
			if (embedFontInfo.getPostScriptName()==null) {
				log.error("Not set!");
				//log.error(((org.apache.fop.fonts.FontTriplet)fontInfo.getFontTriplets().get(0)).getName());
			}
		} catch (Exception e1) {
			// NB getPanose() only exists in our patched FOP
			if (!loggedWarningAlready) {
				log.warn("Not using patched FOP; getPostScriptName() method missing.");
				loggedWarningAlready = true;
			}							
		}
		
		this.embedFontInfo = embedFontInfo;
		
    	setName(name);
    	
    	//familyName = embedFontInfo.
    	
//    	setName(fontInfo.getPostScriptName());
    	
		setEmbeddedFile(embedFontInfo.getEmbedFile());
    	try {
        	setPanose(embedFontInfo.getPanose());		
		} catch (Exception e) {
			// NB getPanose() only exists in our patched FOP
			if (!loggedWarningAlready) {
				log.warn("Not using patched FOP; getPanose() method missing.");
				loggedWarningAlready = true;
			}							
		}
	}
	
	private static boolean loggedWarningAlready = false;
	
	// postscript name eg 
	String name;
	public String getName() {
		return name;
	}
	public void setName(String name) {
		this.name = name;
	}
	
	private EmbedFontInfo embedFontInfo;
	public EmbedFontInfo getEmbedFontInfo() {
		return embedFontInfo;
	}
	
	// // For example: Times New Roman - note this is an array;
	// FOP doesn't ordinarily include it in EmbedFontInfo,
	// instead it makes a font triplet to represent it
//	String familyName;
//	public String getFamilyName() {
//		return familyName;
//	}
//	public void setFamilyName(String familyName) {
//		this.familyName = familyName;
//	}
			
	String embeddedFile;
	public String getEmbeddedFile() {
		return embeddedFile;
	}
	public void setEmbeddedFile(String embeddedFile) {
		this.embeddedFile = embeddedFile;
	}
	
	org.foray.font.format.Panose panose;
	public org.foray.font.format.Panose getPanose() {
		return panose;
	}
	public void setPanose(org.foray.font.format.Panose panose) {
		this.panose = panose;
	}

}