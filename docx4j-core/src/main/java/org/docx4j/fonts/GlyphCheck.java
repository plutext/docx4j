/**
 * 
 */
package org.docx4j.fonts;

import java.util.Optional;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
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
	
	/**
	 * The loaded Typeface for a PhysicalFont.  This cache is the only thing which
	 * holds a Typeface; PhysicalFont doesn't.
	 *
	 * weakKeys, so that the fonts embedded in a docx (which get a PhysicalFont each,
	 * per document, deliberately not in the static map in PhysicalFonts) aren't
	 * retained once the package is discarded.
	 *
	 * softValues, so that the JVM can reclaim a Typeface (of the order of 100s of KB
	 * for a system font, more for a CJK TTC) if it needs the memory.  Before 17.0.3,
	 * maximumSize alone didn't bound anything, since PhysicalFont held the Typeface
	 * as well, and eviction therefore freed nothing.
	 */
	private static LoadingCache<PhysicalFont, Optional<Typeface>> cache = CacheBuilder.newBuilder()
		       .maximumSize(1000)
		       .weakKeys()
		       .softValues()
		       .build(new CacheLoader<PhysicalFont, Optional<Typeface>>() {
		             public Optional<Typeface> load(PhysicalFont key)  {

		            	 return Optional.ofNullable(key.loadTypeface());
		               }
		             });

	/**
	 * The Typeface for this PhysicalFont, loading it if necessary, or null if it
	 * can't be loaded.
	 *
	 * @since 17.0.3
	 */
	public static Typeface getTypeface(PhysicalFont physicalFont) throws ExecutionException {

		return cache.get(physicalFont).orElse(null);
	}

	/**
	 * The Typeface, or null (having warned once for this font).
	 */
	private static Typeface typefaceOrWarn(PhysicalFont physicalFont) throws ExecutionException {

		Typeface t = getTypeface(physicalFont);
		if (t==null
				&& warnedAlready.add(String.valueOf(physicalFont.name))) {
			log.warn("Couldn't load typeface for " + physicalFont.name);
		}
		return t;
	}


	public static boolean hasChar(PhysicalFont physicalFont, char c) throws ExecutionException {
		
		Typeface t = typefaceOrWarn(physicalFont);
		if (t==null) return false;

		boolean exists = t.hasChar(c);

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
		
		Typeface t = typefaceOrWarn(physicalFont);
		if (t==null) return false;

		if (t instanceof MultiByteFont) {
			MultiByteFont mbf = (MultiByteFont)t;
			return mbf.hasCodePoint(cp);
		}
		if (log.isDebugEnabled()) {
			log.debug("Not a MultiByteFont");
		}
		boolean exists = t.hasChar( (char)cp);

		if (log.isInfoEnabled() 
				&& !exists) {
			
            log.info("Glyph " + (int) cp + " (0x"
                    + Integer.toHexString(cp) 
                    + ") not available in font " + physicalFont.name);
			
		}
		
		return exists;
	}
	
	
	private static Set<String> warnedAlready = ConcurrentHashMap.newKeySet();

	public static boolean hasChar(String fontName, char c) throws ExecutionException {

		PhysicalFont pf = PhysicalFonts.get(fontName);
		if (pf==null) {
			if (warnedAlready.add(fontName)) {
				log.warn("Couldn't get font " + fontName);
			}
			return false;
		}
		
		return hasChar(pf, c);
	}
	
}
