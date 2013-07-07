/* NOTICE: This file has been changed by Plutext Pty Ltd for use in docx4j.
 * The package name has been changed; there may also be other changes.
 * 
 * This notice is included to meet the condition in clause 4(b) of the License. 
 */

 /*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

/* $Id: FontInfo.java 721430 2008-11-28 11:13:12Z acumiskey $ */

package org.docx4j.fonts.fop.fonts;

import java.util.Collection;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * The FontInfo holds font information for the layout and rendering of a fo document.
 * This stores the list of available fonts that are setup by
 * the renderer. The font name can be retrieved for the
 * family style and weight.
 * <br>
 * Currently font supported font-variant small-caps is not
 * implemented.
 */
public class FontInfo {

    /** logging instance */
    protected static Logger log = LoggerFactory.getLogger(FontInfo.class);

    /** Map containing fonts that have been used */
    private Map/*<String,FontMetrics>*/ usedFonts = null; //(String = font key)

    /** look up a font-triplet to find a font-name */
    private Map/*<FontTriplet,String>*/ triplets = null; //(String = font key)

    /** look up a font-triplet to find its priority
     *  (only used inside addFontProperties()) */
    private Map/*<FontTriplet,Integer>*/ tripletPriorities = null; //Map<FontTriplet,Integer>

    /** look up a font-name to get a font (that implements FontMetrics at least) */
    private Map/*<String,FontMetrics>*/ fonts = null; //(String = font key)

    /**
     *  a collection of missing fonts; used to make sure the user gets
     *  a warning for a missing font only once (not every time the font is used)
     */
    private Set/*<FontTriplet>*/ loggedFontKeys = null;

    /** Cache for Font instances. */
    private Map/*<FontTriplet, Map>*/ fontInstanceCache = null;

    /** Event listener for font events */
    private FontEventListener eventListener = null;

    /**
     * Main constructor
     */
    public FontInfo() {
        this.triplets = new java.util.HashMap/*<FontTriplet, String>*/();
        this.tripletPriorities = new java.util.HashMap/*<FontTriplet, Integer>*/();
        this.fonts = new java.util.HashMap/*<String, FontMetrics>*/();
        this.usedFonts = new java.util.HashMap/*<String,FontMetrics>*/();
    }

    /**
     * Sets the font event listener that can be used to receive events about particular events
     * in this class.
     * @param listener the font event listener
     */
    public void setEventListener(FontEventListener listener) {
        this.eventListener = listener;
    }

    /**
     * Checks if the font setup is valid (At least the ultimate fallback font
     * must be registered.)
     * @return True if valid
     */
    public boolean isSetupValid() {
        //We're only called when font setup is done:
        tripletPriorities = null; // candidate for garbage collection
        return triplets.containsKey(Font.DEFAULT_FONT);
    }

    /**
     * Adds a new font triplet.
     * @param name internal key
     * @param family font family name
     * @param style font style (normal, italic, oblique...)
     * @param weight font weight
     */
    public void addFontProperties(String name, String family, String style, int weight) {
        addFontProperties(name, createFontKey(family, style, weight));
    }

    /**
     * Adds a series of new font triplets given an array of font family names.
     * @param name internal key
     * @param families an array of font family names
     * @param style font style (normal, italic, oblique...)
     * @param weight font weight
     */
    public void addFontProperties(String name, String[] families, String style, int weight) {
        for (int i = 0; i < families.length; i++) {
            addFontProperties(name, families[i], style, weight);
        }
    }

    /**
     * Adds a new font triplet.
     * @param internalFontKey internal font key
     * @param triplet the font triplet to associate with the internal key
     */
    public void addFontProperties(String internalFontKey, FontTriplet triplet) {
        /*
         * add the given family, style and weight as a lookup for the font
         * with the given name
         */
        if (log.isDebugEnabled()) {
            log.debug("Registering: " + triplet + " under " + internalFontKey);
        }
        String oldName = (String)triplets.get(triplet);
        int newPriority = triplet.getPriority();
        if (oldName != null) {
            int oldPriority = ((Integer)tripletPriorities.get(triplet)).intValue();
            if (oldPriority < newPriority) {
                logDuplicateFont(triplet, false, oldName, oldPriority,
                            internalFontKey, newPriority);
                return;
            } else {
                logDuplicateFont(triplet, true, oldName, oldPriority,
                            internalFontKey, newPriority);
            }
        }
        this.triplets.put(triplet, internalFontKey);
        this.tripletPriorities.put(triplet, new Integer(newPriority));
    }

    /**
     * Logger warning about duplicate font triplets.
     *
     * @param triplet the duplicate font triplet
     * @param replacing true iff the new font will replace the old one
     * @param oldKey the old internal font name
     * @param oldPriority the priority of the existing font mapping
     * @param newKey the new internal font name
     * @param newPriority the priority of the duplicate font mapping
     */
    private void logDuplicateFont(FontTriplet triplet, boolean replacing,
                                  String oldKey, int oldPriority,
                                  String newKey, int newPriority) {
        if (log.isDebugEnabled()) {
            log.debug(triplet
                    + (replacing ? ": Replacing " : ": Not replacing ")
                    + ((FontMetrics)fonts.get(triplets.get(triplet))).getFullName()
                    + " (priority=" + oldPriority + ") by "
                    + ((FontMetrics)fonts.get(newKey)).getFullName()
                    + " (priority=" + newPriority + ")");
        }
    }

    /**
     * Adds font metrics for a specific font.
     * @param internalFontKey internal key
     * @param metrics metrics to register
     */
    public void addMetrics(String internalFontKey, FontMetrics metrics) {
        // add the given metrics as a font with the given name

        if (metrics instanceof Typeface) {
            ((Typeface)metrics).setEventListener(this.eventListener);
        }
        this.fonts.put(internalFontKey, metrics);
    }

    /**
     * Lookup a font.
     * <br>
     * Locate the font name for a given family, style and weight.
     * The font name can then be used as a key as it is unique for
     * the associated document.
     * This also adds the font to the list of used fonts.
     * @param family font family
     * @param style font style
     * @param weight font weight
     * @param substitutable true if the font may be substituted with the
     *                  default font if not found
     * @return internal font triplet key
     */
    private FontTriplet fontLookup(String family, String style,
                             int weight, boolean substitutable) {
        if (log.isTraceEnabled()) {
            log.trace("Font lookup: " + family + " " + style + " " + weight
                    + (substitutable ? " substitutable" : ""));
        }

        FontTriplet startKey = createFontKey(family, style, weight);
        FontTriplet fontTriplet = startKey;
        // first try given parameters
        String internalFontKey = getInternalFontKey(fontTriplet);
        if (internalFontKey == null) {
            fontTriplet = fuzzyFontLookup(family, style, weight, startKey, substitutable);
        }

        if (fontTriplet != null) {
            if (fontTriplet != startKey) {
                notifyFontReplacement(startKey, fontTriplet);
            }
            return fontTriplet;
        } else {
            return null;
        }
    }

    private FontTriplet fuzzyFontLookup(String family, String style,
            int weight, FontTriplet startKey, boolean substitutable) {
        FontTriplet key;
        String internalFontKey = null;
        if (!family.equals(startKey.getName())) {
            key = createFontKey(family, style, weight);
            internalFontKey = getInternalFontKey(key);
            if (internalFontKey != null) {
                return key;
            }
        }

        // adjust weight, favouring normal or bold
        key = findAdjustWeight(family, style, weight);
        if (key != null) {
            internalFontKey = getInternalFontKey(key);
        }

        // return null if not found and not substitutable
        if (!substitutable && internalFontKey == null) {
            return null;
        }

        // only if the font may be substituted
        // fallback 1: try the same font-family and weight with default style
        if (internalFontKey == null && style != Font.STYLE_NORMAL) {
            key = createFontKey(family, Font.STYLE_NORMAL, weight);
            internalFontKey = getInternalFontKey(key);
        }

        if (internalFontKey == null && weight != Font.WEIGHT_NORMAL) {
            int diffWeight = (Font.WEIGHT_NORMAL - weight) / 100;
            int direction = diffWeight > 0 ? 1 : -1;
            int tryWeight = weight;
            while (tryWeight != Font.WEIGHT_NORMAL) {
                tryWeight += 100 * direction;
                key = createFontKey(family, style, weight);
                internalFontKey = getInternalFontKey(key);
                if (internalFontKey == null) {
                    key = createFontKey(family, Font.STYLE_NORMAL, weight);
                    internalFontKey = getInternalFontKey(key);
                }
                if (internalFontKey != null) {
                    break;
                }
            }
        }

        // fallback 2: try the same font-family with default style and weight
        /* obsolete: replaced by the loop above
        if (internalFontKey == null) {
            key = createFontKey(family, Font.STYLE_NORMAL, Font.WEIGHT_NORMAL);
            internalFontKey = getInternalFontKey(key);
        }*/

        // fallback 3: try any family with original style/weight
        if (internalFontKey == null) {
            return fuzzyFontLookup("any", style, weight, startKey, false);
        }

        // last resort: use default
        if (key == null && internalFontKey == null) {
            key = Font.DEFAULT_FONT;
            internalFontKey = getInternalFontKey(key);
        }

        if (internalFontKey != null) {
            return key;
        } else {
            return null;
        }
    }

    /**
     * Tells this class that the font with the given internal name has been used.
     * @param internalName the internal font name (F1, F2 etc.)
     */
    public void useFont(String internalName) {
        usedFonts.put(internalName, fonts.get(internalName));
    }

    private Map/*<FontTriplet,Map<Integer,Font>>*/ getFontInstanceCache() {
        if (fontInstanceCache == null) {
            fontInstanceCache = new java.util.HashMap/*<FontTriplet, Map<Integer,Font>>*/();
        }
        return fontInstanceCache;
    }

    /**
     * Retrieves a (possibly cached) Font instance based on a FontTriplet and a font size.
     *
     * @param triplet the font triplet designating the requested font
     * @param fontSize the font size
     * @return the requested Font instance
     */
    public Font getFontInstance(FontTriplet triplet, int fontSize) {
        Map/*<Integer,Font>*/ sizes
            = (Map/*<Integer,Font>*/)getFontInstanceCache().get(triplet);
        if (sizes == null) {
            sizes = new java.util.HashMap/*<Integer,Font>*/();
            getFontInstanceCache().put(triplet, sizes);
        }
        Integer size = new Integer(fontSize);
        Font font = (Font)sizes.get(size);
        if (font == null) {
            String fontKey = getInternalFontKey(triplet);
            useFont(fontKey);
            FontMetrics metrics = getMetricsFor(fontKey);
            font = new Font(fontKey, triplet, metrics, fontSize);
            sizes.put(size, font);
        }
        return font;
    }

    private List/*<FontTriplet>*/ getTripletsForName(String fontName) {
        List/*<FontTriplet>*/ matchedTriplets = new java.util.ArrayList/*<FontTriplet>*/();
        Iterator it = triplets.keySet().iterator();
        while (it.hasNext()) {
            FontTriplet triplet = (FontTriplet)it.next();
            String tripletName = triplet.getName();
            if (tripletName.toLowerCase().equals(fontName.toLowerCase())) {
                matchedTriplets.add(triplet);
            }
        }
        return matchedTriplets;
    }

    /**
     * Returns a suitable internal font given an AWT Font instance.
     *
     * @param awtFont the AWT font
     * @return a best matching internal Font
     */
    public Font getFontInstanceForAWTFont(java.awt.Font awtFont) {
        String awtFontName = awtFont.getName();
        String awtFontFamily = awtFont.getFamily();
        String awtFontStyle = awtFont.isItalic() ? Font.STYLE_ITALIC : Font.STYLE_NORMAL;
        int awtFontWeight = awtFont.isBold() ? Font.WEIGHT_BOLD : Font.WEIGHT_NORMAL;

        FontTriplet matchedTriplet = null;
        List/*<FontTriplet>*/ triplets = getTripletsForName(awtFontName);
        if (!triplets.isEmpty()) {
            Iterator it = triplets.iterator();
            while (it.hasNext()) {
                FontTriplet triplet = (FontTriplet)it.next();
                boolean styleMatched = triplet.getStyle().equals(awtFontStyle);
                boolean weightMatched = triplet.getWeight() == awtFontWeight;
                if (styleMatched && weightMatched) {
                    matchedTriplet = triplet;
                    break;
                }
            }
        }

        // not matched on font name so do a lookup using family
        if (matchedTriplet == null) {
            if (awtFontFamily.equals("sanserif")) {
                awtFontFamily = "sans-serif";
            }
            matchedTriplet = fontLookup(awtFontFamily, awtFontStyle, awtFontWeight);
        }
        int fontSize = Math.round(awtFont.getSize2D() * 1000);
        return getFontInstance(matchedTriplet, fontSize);
    }

    /**
     * Lookup a font.
     * <br>
     * Locate the font name for a given family, style and weight.
     * The font name can then be used as a key as it is unique for
     * the associated document.
     * This also adds the font to the list of used fonts.
     * @param family font family
     * @param style font style
     * @param weight font weight
     * @return the font triplet of the font chosen
     */
    public FontTriplet fontLookup(String family, String style,
                             int weight) {
        return fontLookup(family, style, weight, true);
    }

    private List/*<FontTriplet>*/ fontLookup(String[] families, String style,
            int weight, boolean substitutable) {
        List/*<FontTriplet>*/ matchingTriplets = new java.util.ArrayList/*<FontTriplet>*/();
        FontTriplet triplet = null;
        for (int i = 0; i < families.length; i++) {
            triplet = fontLookup(families[i], style, weight, substitutable);
            if (triplet != null) {
                matchingTriplets.add(triplet);
            }
        }
        return matchingTriplets;
    }

    /**
     * Looks up a set of fonts.
     * <br>
     * Locate the font name(s) for the given families, style and weight.
     * The font name(s) can then be used as a key as they are unique for
     * the associated document.
     * This also adds the fonts to the list of used fonts.
     * @param families  font families (priority list)
     * @param style     font style
     * @param weight    font weight
     * @return the set of font triplets of all supported and chosen font-families
     *          in the specified style and weight.
     */
    public FontTriplet[] fontLookup(String[] families, String style,
                             int weight) {
        if (families.length == 0) {
            throw new IllegalArgumentException("Specify at least one font family");
        }

        // try matching without substitutions
        List/*<FontTriplet>*/ matchedTriplets = fontLookup(families, style, weight, false);

        // if there are no matching font triplets found try with substitutions
        if (matchedTriplets.size() == 0) {
            matchedTriplets = fontLookup(families, style, weight, true);
        }

        // no matching font triplets found!
        if (matchedTriplets.size() == 0) {
            StringBuffer sb = new StringBuffer();
            for (int i = 0, c = families.length; i < c; i++) {
                if (i > 0) {
                    sb.append(", ");
                }
                sb.append(families[i]);
            }
            throw new IllegalStateException(
                        "fontLookup must return an array with at least one "
                        + "FontTriplet on the last call. Lookup: " + sb.toString());

        }
        FontTriplet[] fontTriplets = new FontTriplet[matchedTriplets.size()];
        matchedTriplets.toArray(fontTriplets);

        // found some matching fonts so return them
        return fontTriplets;
    }

    private Set/*<FontTriplet>*/ getLoggedFontKeys() {
        if (loggedFontKeys == null) {
            loggedFontKeys = new java.util.HashSet/*<FontTriplet>*/();
        }
        return loggedFontKeys;
    }

    private void notifyFontReplacement(FontTriplet replacedKey, FontTriplet newKey) {
        if (!getLoggedFontKeys().contains(replacedKey)) {
            getLoggedFontKeys().add(replacedKey);
            if (this.eventListener != null) {
                this.eventListener.fontSubstituted(this, replacedKey, newKey);
            } else {
                log.warn("Font '" + replacedKey + "' not found. "
                        + "Substituting with '" + newKey + "'.");
            }
        }
    }

    /**
     * Find a font with a given family and style by trying
     * different font weights according to the spec.
     * @param family font family
     * @param style font style
     * @param weight font weight
     * @return internal key
     */
    public FontTriplet findAdjustWeight(String family, String style,
                             int weight) {
        FontTriplet key = null;
        String f = null;
        int newWeight = weight;
        if (newWeight <= 400) {
            while (f == null && newWeight > 100) {
                newWeight -= 100;
                key = createFontKey(family, style, newWeight);
                f = getInternalFontKey(key);
            }
            newWeight = weight;
            while (f == null && newWeight < 400) {
                newWeight += 100;
                key = createFontKey(family, style, newWeight);
                f = getInternalFontKey(key);
            }
        } else if (newWeight == 500) {
            key = createFontKey(family, style, 400);
            f = getInternalFontKey(key);
        } else if (newWeight > 500) {
            while (f == null && newWeight < 1000) {
                newWeight += 100;
                key = createFontKey(family, style, newWeight);
                f = getInternalFontKey(key);
            }
            newWeight = weight;
            while (f == null && newWeight > 400) {
                newWeight -= 100;
                key = createFontKey(family, style, newWeight);
                f = getInternalFontKey(key);
            }
        }
        if (f == null && weight != 400) {
            key = createFontKey(family, style, 400);
            f = getInternalFontKey(key);
        }

        if (f != null) {
            return key;
        } else {
            return null;
        }
    }

    /**
     * Determines if a particular font is available.
     * @param family font family
     * @param style font style
     * @param weight font weight
     * @return True if available
     */
    public boolean hasFont(String family, String style, int weight) {
        FontTriplet key = createFontKey(family, style, weight);
        return this.triplets.containsKey(key);
    }

    /**
     * Returns the internal font key (F1, F2, F3 etc.) for a given triplet.
     * @param triplet the font triplet
     * @return the associated internal key or null, if not found
     */
    public String getInternalFontKey(FontTriplet triplet) {
        return (String)triplets.get(triplet);
    }

    /**
     * Creates a key from the given strings.
     * @param family font family
     * @param style font style
     * @param weight font weight
     * @return internal key
     */
    public static FontTriplet createFontKey(String family, String style,
                                       int weight) {
        return new FontTriplet(family, style, weight);
    }

    /**
     * Gets a Map of all registered fonts.
     * @return a read-only Map with font key/FontMetrics pairs
     */
    public Map/*<String,FontMetrics>*/ getFonts() {
        return java.util.Collections.unmodifiableMap(this.fonts);
    }

    /**
     * Gets a Map of all registered font triplets.
     * @return a Map with FontTriplet/font key pairs
     */
    public Map/*<FontTriplet,String>*/ getFontTriplets() {
        return this.triplets;
    }

    /**
     * This is used by the renderers to retrieve all the
     * fonts used in the document.
     * This is for embedded font or creating a list of used fonts.
     * @return a read-only Map with font key/FontMetrics pairs
     */
    public Map/*<String,FontMetrics>*/ getUsedFonts() {
        return this.usedFonts;
    }

    /**
     * Returns the FontMetrics for a particular font
     * @param fontName internal key
     * @return font metrics
     */
    public FontMetrics getMetricsFor(String fontName) {
        FontMetrics metrics = (FontMetrics)fonts.get(fontName);
        usedFonts.put(fontName, metrics);
        return metrics;
    }

    /**
     * Returns all font triplet matching the given font name.
     * @param fontName The font name we are looking for
     * @return A list of matching font triplets
     */
    public List/*<FontTriplet>*/ getTripletsFor(String fontName) {
        List/*<FontTriplet>*/ foundTriplets = new java.util.ArrayList();
        for (Iterator iter = triplets.entrySet().iterator(); iter.hasNext();) {
            Map.Entry tripletEntry = (Map.Entry) iter.next();
            if (fontName.equals((tripletEntry.getValue()))) {
                foundTriplets.add(tripletEntry.getKey());
            }
        }
        return foundTriplets;
    }

    /**
     * Returns the first triplet matching the given font name.
     * As there may be multiple triplets matching the font name
     * the result set is sorted first to guarantee consistent results.
     * @param fontName The font name we are looking for
     * @return The first triplet for the given font name
     */
    public FontTriplet getTripletFor(String fontName) {
        List/*<FontTriplet>*/ foundTriplets = getTripletsFor(fontName);
        if (foundTriplets.size() > 0) {
            Collections.sort(foundTriplets);
            return (FontTriplet)foundTriplets.get(0);
        }
        return null;
    }

    /**
     * Returns the font style for a particular font.
     * There may be multiple font styles matching this font. Only the first
     * found is returned. Searching is done on a sorted list to guarantee consistent
     * results.
     * @param fontName internal key
     * @return font style
     */
    public String getFontStyleFor(String fontName) {
        FontTriplet triplet = getTripletFor(fontName);
        if (triplet != null) {
            return triplet.getStyle();
        } else {
            return "";
        }
    }

    /**
     * Returns the font weight for a particular font.
     * There may be multiple font weights matching this font. Only the first
     * found is returned. Searching is done on a sorted list to guarantee consistent
     * results.
     * @param fontName internal key
     * @return font weight
     */
    public int getFontWeightFor(String fontName) {
        FontTriplet triplet = getTripletFor(fontName);
        if (triplet != null) {
            return triplet.getWeight();
        } else {
            return 0;
        }
    }

    /**
     * Diagnostic method for logging all registered fonts to System.out.
     */
    public void dumpAllTripletsToSystemOut() {
        System.out.print(toString());
    }

    /**
     * {@inheritDoc}
     */
    public String toString() {
        Collection entries = new java.util.TreeSet();
        Iterator iter = this.triplets.keySet().iterator();
        while (iter.hasNext()) {
            FontTriplet triplet = (FontTriplet)iter.next();
            String key = getInternalFontKey(triplet);
            FontMetrics metrics = getMetricsFor(key);
            entries.add(triplet.toString() + " -> " + key + " -> " + metrics.getFontName() + "\n");
        }
        StringBuffer stringBuffer = new StringBuffer();
        for (iter = entries.iterator(); iter.hasNext();) {
            stringBuffer.append(iter.next());
        }
        return stringBuffer.toString();
    }
}
