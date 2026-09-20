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
	 * Check whether this PhysicalFont contains a glyph for a codepoint, including one
	 * outside the Basic Multilingual Plane.
	 *  
	 * @param physicalFont the font
	 * @param cp the code point
	 * @return whether the font has a glyph for it (false where the font can't be loaded)
	 * @throws ExecutionException where loading the typeface failed
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

		/* A no-break space is a space: the two differ in where a line may break, not in
		 * what is drawn, and FOP draws U+00A0 with the space glyph.  Several faces map
		 * only U+0020 - Selawik and Selawik Light have no U+00A0 at all - and taking that
		 * as "no glyph" sent a paragraph whose only content is a no-break space down the
		 * no-coverage path, where the FO gets no font-family and FOP draws it in its
		 * base-14 default: a font not embedded in the PDF (measured on a corpus document,
		 * CR-017 phase 5; the same reading that TextMeasurer.glyphWidthPt already gives a
		 * tab).  @since 17.2.0 */
		if (cp==0x00A0 && !hasRawCodepoint(t, cp, physicalFont.name)) cp = ' ';

		return hasRawCodepoint(t, cp, physicalFont.name);
	}

	/**
	 * Whether a glyph of this font would be reported to a PDF reader as a <b>CJK
	 * radical</b> rather than as the ideograph the document holds.
	 *
	 * <p>FOP takes a glyph's character from the first cmap segment which covers the glyph
	 * ({@code MultiByteFont.findCharacterFromGlyphIndex}: "if more than one correspondence
	 * exists, then the first one is returned"), and a CJK font maps a Kangxi radical and
	 * the ideograph it is the radical of to one glyph - in Source Han Sans CN, U+2F63 and
	 * U+751F are both glyph 18742.  The radical is the lower code point, so it is the one
	 * FOP finds.  This reports whether that can happen in this font: whether a glyph of the
	 * radical blocks (U+2E80-U+2FDF) is also reached from a code point outside them.</p>
	 *
	 * @param physicalFont the font
	 * @return whether its reverse lookup can take a radical for an ideograph
	 * @throws ExecutionException where loading the typeface failed
	 * @since 17.2.0
	 */
	public static boolean reverseLookupTakesACjkRadical(PhysicalFont physicalFont)
			throws ExecutionException {

		return reverseLookupTakesACjkRadical(typefaceOrWarn(physicalFont));
	}

	/** As above, for a typeface already in hand.  @since 17.2.0 */
	public static boolean reverseLookupTakesACjkRadical(Typeface t) {

		if (!(t instanceof MultiByteFont)) return false;   // no layout tables, no round trip
		org.docx4j.fonts.fop.fonts.CMapSegment[] cmap = ((MultiByteFont)t).getCMap();
		if (cmap==null) return false;

		java.util.List<org.docx4j.fonts.fop.fonts.CMapSegment> radicals
				= new java.util.ArrayList<org.docx4j.fonts.fop.fonts.CMapSegment>();
		for (org.docx4j.fonts.fop.fonts.CMapSegment seg : cmap) {
			if (seg.getUnicodeEnd() >= RADICALS_START && seg.getUnicodeStart() <= RADICALS_END) {
				radicals.add(seg);
			}
		}
		if (radicals.isEmpty()) return false;

		for (org.docx4j.fonts.fop.fonts.CMapSegment seg : cmap) {
			if (seg.getUnicodeEnd() >= RADICALS_START && seg.getUnicodeStart() <= RADICALS_END) continue;
			int start = seg.getGlyphStartIndex();
			int end = start + (seg.getUnicodeEnd() - seg.getUnicodeStart());
			for (org.docx4j.fonts.fop.fonts.CMapSegment r : radicals) {
				int rStart = r.getGlyphStartIndex();
				int rEnd = rStart + (r.getUnicodeEnd() - r.getUnicodeStart());
				if (start <= rEnd && rStart <= end) return true;
			}
		}
		return false;
	}

	/** CJK Radicals Supplement and Kangxi Radicals: the forms a CJK font draws with the
	 *  ideograph's own glyph.  @since 17.2.0 */
	private static final int RADICALS_START = 0x2E80;
	private static final int RADICALS_END = 0x2FDF;

	/** Whether the typeface itself maps this code point. */
	private static boolean hasRawCodepoint(Typeface t, int cp, String fontName) {

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
                    + ") not available in font " + fontName);
			
		}
		
		return exists;
	}
	
	
	private static Set<String> warnedAlready = ConcurrentHashMap.newKeySet();

	/**
	 * @deprecated since 17.2.0: this looks the name up in {@link PhysicalFonts}, where a
	 *             font embedded in the document, or a document font mapped to a
	 *             substitute of another name, is not.  Resolve the document font through
	 *             the package's {@link Mapper#get} and ask {@link #hasCodepoint(PhysicalFont, int)}.
	 */
	@Deprecated
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
