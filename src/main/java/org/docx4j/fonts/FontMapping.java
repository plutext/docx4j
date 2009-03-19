/**
 * 
 */
package org.docx4j.fonts;

public class FontMapping {

	FontMapping() {}
	
	FontMapping(String documentFont, PhysicalFont physicalFont) {
		
		this.documentFont = documentFont;
		
		this.physicalFont = physicalFont;
		
	}
	
	// The document font.  This, normalised, is the
	// key under which this font mapping can be found
	String documentFont;
	public String getDocumentFont() {
		return documentFont;
	}
	public void setDocumentFont(String documentFont) {
		this.documentFont = documentFont;
	}

	PhysicalFont physicalFont;
	public PhysicalFont getPhysicalFont() {
		return physicalFont;
	}
	public void setPhysicalFont(PhysicalFont physicalFont) {
		this.physicalFont = physicalFont;
	}

}