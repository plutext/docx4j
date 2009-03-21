/**
 * 
 */
package org.docx4j.fonts;

import org.apache.fop.fonts.EmbedFontInfo;

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

	
	PhysicalFont(String name, EmbedFontInfo fontInfo) {
		
		// Sanity check
		if (fontInfo.getPostScriptName()==null) {
			SubstituterImplPanose.log.error("Not set!");
			//log.error(((org.apache.fop.fonts.FontTriplet)fontInfo.getFontTriplets().get(0)).getName());
		}
		
    	setName(name);
//    	setName(fontInfo.getPostScriptName());
    	setEmbeddedFile(fontInfo.getEmbedFile());
    	setPanose(fontInfo.getPanose());		
	}
	
	// For example, Times New Roman Bold
	// What exactly to call it?
	// Note that we don't use this name anywhere that matters
	// - iText uses the font family name,
	// with magic appendages 'Bold', 'Italic', 'BoldItalic'
	// as necessary
	// Nevertheless, lets refer to it here by its
	// postscript name, since that is pretty well defined
	String name;
	public String getName() {
		return name;
	}
	public void setName(String name) {
		this.name = name;
	}
	
	// // For example: Times New Roman
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
	
	org.apache.fop.fonts.Panose panose;
	public org.apache.fop.fonts.Panose getPanose() {
		return panose;
	}
	public void setPanose(org.apache.fop.fonts.Panose panose) {
		this.panose = panose;
	}

}