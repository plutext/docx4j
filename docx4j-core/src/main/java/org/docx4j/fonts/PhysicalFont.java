/**
 * 
 */
package org.docx4j.fonts;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.URI;
import java.util.concurrent.ExecutionException;

import org.docx4j.fonts.fop.apps.io.InternalResourceResolver;
import org.docx4j.fonts.fop.fonts.EmbedFontInfo;
import org.docx4j.fonts.fop.fonts.LazyFont;
import org.docx4j.fonts.fop.fonts.Typeface;

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
	protected static Logger log = LoggerFactory.getLogger(PhysicalFont.class);		
	protected InternalResourceResolver fontResolver = null;
	protected volatile boolean loadTypefaceFailed = false;

	PhysicalFont(String name, EmbedFontInfo embedFontInfo, InternalResourceResolver fontResolver) {
		
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
		this.fontResolver = fontResolver;
		
    	setName(name);
    	
    	//familyName = embedFontInfo.
    	
//    	setName(fontInfo.getPostScriptName());
    	
		setEmbeddedURI(embedFontInfo.getEmbedURI());
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
			
	URI embeddedURI;
	public URI getEmbeddedURI() {
		return embeddedURI;
	}
	public void setEmbeddedURI(URI embeddedURI) {
		this.embeddedURI = embeddedURI;
	}
	
	org.docx4j.fonts.foray.font.format.Panose panose;
	public org.docx4j.fonts.foray.font.format.Panose getPanose() {
		return panose;
	}
	public void setPanose(org.docx4j.fonts.foray.font.format.Panose panose) {
		this.panose = panose;
	}

	/**
	 * The Typeface for this font, or null if it can't be loaded.
	 *
	 * Since 17.0.3, the Typeface is not held on this object; it lives in
	 * GlyphCheck's cache, so that it can be reclaimed.  A loaded Typeface
	 * is expensive (of the order of 100s of KB per font), and a PhysicalFont
	 * for a system font is held for the life of the JVM in the static map
	 * in PhysicalFonts, so retaining it here meant it was never freed.
	 */
	public Typeface getTypeface() {
		try {
			return GlyphCheck.getTypeface(this);
		} catch (ExecutionException e) {
			log.error(e.getMessage(), e);
			return null;
		}
	}

	/**
	 * Actually load the Typeface.  Use getTypeface(), so the result is cached.
	 */
	Typeface loadTypeface() {

		if (loadTypefaceFailed) return null;

		Typeface typeface = null;
		try {
			LazyFont lazyFont = new LazyFont(embedFontInfo, fontResolver, false); // TODO: useComplexScripts
			typeface = lazyFont.getRealFont();
		} catch (RuntimeException e) {
			// eg a font we can't parse; treat it as a font without glyphs,
			// rather than failing the conversion
			log.error("Couldn't load typeface for " + name + ": " + e.getMessage(), e);
		}
		loadTypefaceFailed = (typeface == null);
		return typeface;
	}
}
