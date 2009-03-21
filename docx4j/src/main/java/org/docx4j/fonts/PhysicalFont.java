/**
 * 
 */
package org.docx4j.fonts;

import org.apache.fop.fonts.EmbedFontInfo;
import org.apache.log4j.Logger;
import org.docx4j.convert.out.pdf.viaXSLFO.Conversion;

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
		
		// Sanity check
		if (embedFontInfo.getPostScriptName()==null) {
			log.error("Not set!");
			//log.error(((org.apache.fop.fonts.FontTriplet)fontInfo.getFontTriplets().get(0)).getName());
		}
		
		this.embedFontInfo = embedFontInfo;
		
    	setName(name);
//    	setName(fontInfo.getPostScriptName());
    	setEmbeddedFile(embedFontInfo.getEmbedFile());
    	setPanose(embedFontInfo.getPanose());		
	}
	
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