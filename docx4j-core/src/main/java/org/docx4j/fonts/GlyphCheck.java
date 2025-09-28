/**
 * 
 */
package org.docx4j.fonts;

import java.util.HashSet;
import java.util.concurrent.ExecutionException;

import org.docx4j.com.google.common.cache.CacheBuilder;
import org.docx4j.com.google.common.cache.CacheLoader;
import org.docx4j.com.google.common.cache.LoadingCache;
import org.docx4j.fonts.fop.fonts.MultiByteFont;
import org.docx4j.fonts.fop.fonts.Typeface;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Check whether a PhysicalFont contains glyph sought.
 * 
 * @author jharrop
 *
 */
public class GlyphCheck {
	
	protected static Logger log = LoggerFactory.getLogger(GlyphCheck.class);	
	
	private static LoadingCache<PhysicalFont, Typeface> cache = CacheBuilder.newBuilder()
		       .maximumSize(1000)
		       .build(new CacheLoader<PhysicalFont, Typeface>() {
		             public Typeface load(PhysicalFont key)  {
		            	 
		            	 return key.getTypeface();
		               }
		             });

	
	public static boolean hasChar(PhysicalFont physicalFont, char c) throws ExecutionException {
		
		boolean exists = cache.get(physicalFont).hasChar(c);
		
		if (log.isInfoEnabled() 
				&& !exists) {
			
            log.info("Glyph " + (int) c + " (0x"
                    + Integer.toHexString(c) 
                    + ") not available in font " + physicalFont.name);
			
		}
		
		return exists;
	}

	/**
	 * Check whether this PhysicalFont contains a glyph for a codepoint outside the Basic Multilingual Plane.
	 *  
	 * @param physicalFont
	 * @param cp
	 * @return
	 * @throws ExecutionException
	 * @since 11.5.6
	 */
	public static boolean hasCodepoint(PhysicalFont physicalFont, int cp) throws ExecutionException {
		
		/*
		 * The Java char data type is a 16-bit type, meaning it can only represent Unicode characters 
		 * from U+0000 to U+FFFF. These are known as the Basic Multilingual Plane (BMP) characters.

			The '🕸' character, for example, is outside of this range. To represent such a character, 
			Java uses a surrogate pair, which is a sequence of two char values that 
			combine to represent a single Unicode code point.
		 */
		
		Typeface t = cache.get(physicalFont);
		if (t instanceof MultiByteFont) {
			MultiByteFont mbf = (MultiByteFont)t;
			return mbf.hasCodePoint(cp);
		}
		if (log.isDebugEnabled()) {
			log.debug("Not a MultiByteFont");
		}
		boolean exists = cache.get(physicalFont).hasChar( (char)cp);
		
		if (log.isInfoEnabled() 
				&& !exists) {
			
            log.info("Glyph " + (int) cp + " (0x"
                    + Integer.toHexString(cp) 
                    + ") not available in font " + physicalFont.name);
			
		}
		
		return exists;
	}
	
	
	private static HashSet<String> warnedAlready = new HashSet<String>();

	public static boolean hasChar(String fontName, char c) throws ExecutionException {
		
		PhysicalFont pf = PhysicalFonts.get(fontName);
		if (pf==null) {
			if (!warnedAlready.contains(fontName)) {
				log.warn("Couldn't get font " + fontName);
				warnedAlready.add(fontName);
			}
			return false;
		}
		
		return hasChar(pf, c);
	}
	
}
