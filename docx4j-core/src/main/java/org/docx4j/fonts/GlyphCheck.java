/**
 * 
 */
package org.docx4j.fonts;

import java.util.HashSet;
import java.util.concurrent.ExecutionException;

import org.docx4j.com.google.common.cache.CacheBuilder;
import org.docx4j.com.google.common.cache.CacheLoader;
import org.docx4j.com.google.common.cache.LoadingCache;
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
