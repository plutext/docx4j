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

/* $Id: CustomFont.java 721430 2008-11-28 11:13:12Z acumiskey $ */

/* NOTICE: This file has been changed by Plutext Pty Ltd for use in docx4j.
 * 
 * This notice is included to meet the condition in clause 4(b) of the License. 
 */

package org.docx4j.fonts.fop.fonts;

import java.io.IOException;
import java.util.Collections;
import java.util.Map;
import java.util.Set;

import javax.xml.transform.Source;

import org.docx4j.fonts.foray.font.format.Panose;


/**
 * Abstract base class for custom fonts loaded from files, for example.
 */
public abstract class CustomFont extends Typeface
            implements FontDescriptor, MutableFont {

    private String fontName = null;
    private String fullName = null;
    private Set familyNames = null; //Set<String>
    private String fontSubName = null;
    private String embedFileName = null;
    private String embedResourceName = null;
    private FontResolver resolver = null;

    private int capHeight = 0;
    private int xHeight = 0;
    private int ascender = 0;
    private int descender = 0;
    private int[] fontBBox = {0, 0, 0, 0};
    private int flags = 4;
    private int weight = 0; //0 means unknown weight
    private int stemV = 0;
    private int italicAngle = 0;
    private int missingWidth = 0;
    private FontType fontType = FontType.TYPE1;
    private int firstChar = 0;
    private int lastChar = 255;

    private Map kerning;

    private boolean useKerning = true;

    private boolean isEmbeddable = true;

	public boolean isEmbeddable() {
		return isEmbeddable;
	}

	public void setEmbeddable(boolean isEmbeddable) {
		this.isEmbeddable = isEmbeddable;
	}

	private Panose panose = null;

	public Panose getPanose() {
		return panose;
	}

	public void setPanose(Panose panose) {
		this.panose = panose;
	}
    
    /** {@inheritDoc} */
    public String getFontName() {
        return fontName;
    }

    /** {@inheritDoc} */
    public String getEmbedFontName() {
        return getFontName();
    }

    /** {@inheritDoc} */
    public String getFullName() {
        return fullName;
    }

    /**
     * Returns the font family names.
     * @return the font family names (a Set of Strings)
     */
    public Set getFamilyNames() {
        return Collections.unmodifiableSet(this.familyNames);
    }

    /**
     * Returns the font family name stripped of whitespace.
     * @return the stripped font family
     * @see FontUtil#stripWhiteSpace(String)
     */
    public String getStrippedFontName() {
        return FontUtil.stripWhiteSpace(getFontName());
    }

    /**
     * Returns font's subfamily name.
     * @return the font's subfamily name
     */
    public String getFontSubName() {
        return fontSubName;
    }

    /**
     * Returns an URI representing an embeddable font file. The URI will often
     * be a filename or an URL.
     * @return URI to an embeddable font file or null if not available.
     */
    public String getEmbedFileName() {
        return embedFileName;
    }

    /**
     * Returns a Source representing an embeddable font file.
     * @return Source for an embeddable font file
     * @throws IOException if embedFileName is not null but Source is not found
     */
    public Source getEmbedFileSource() throws IOException {
        Source result = null;
        if (resolver != null && embedFileName != null) {
            result = resolver.resolve(embedFileName);
            if (result == null) {
                throw new IOException("Unable to resolve Source '"
                        + embedFileName + "' for embedded font");
            }
        }
        return result;
    }

    /**
     * Returns the lookup name to an embeddable font file available as a
     * resource.
     * (todo) Remove this method, this should be done using a resource: URI.
     * @return the lookup name
     */
    public String getEmbedResourceName() {
        return embedResourceName;
    }

    /**
     * {@inheritDoc}
     */
    public int getAscender() {
        return ascender;
    }

    /**
     * {@inheritDoc}
     */
    public int getDescender() {
        return descender;
    }

    /**
     * {@inheritDoc}
     */
    public int getCapHeight() {
        return capHeight;
    }

    /**
     * {@inheritDoc}
     */
    public int getAscender(int size) {
        return size * ascender;
    }

    /**
     * {@inheritDoc}
     */
    public int getDescender(int size) {
        return size * descender;
    }

    /**
     * {@inheritDoc}
     */
    public int getCapHeight(int size) {
        return size * capHeight;
    }

    /**
     * {@inheritDoc}
     */
    public int getXHeight(int size) {
        return size * xHeight;
    }

    /**
     * {@inheritDoc}
     */
    public int[] getFontBBox() {
        return fontBBox;
    }

    /** {@inheritDoc} */
    public int getFlags() {
        return flags;
    }

    /** {@inheritDoc} */
    public boolean isSymbolicFont() {
        return ((getFlags() & 4) != 0) || "ZapfDingbatsEncoding".equals(getEncodingName());
        //Note: The check for ZapfDingbats is necessary as the PFM does not reliably indicate
        //if a font is symbolic.
    }

    /**
     * Returns the font weight (100, 200...800, 900). This value may be different from the
     * one that was actually used to register the font.
     * @return the font weight (or 0 if the font weight is unknown)
     */
    public int getWeight() {
        return this.weight;
    }

    /**
     * {@inheritDoc}
     */
    public int getStemV() {
        return stemV;
    }

    /**
     * {@inheritDoc}
     */
    public int getItalicAngle() {
        return italicAngle;
    }

    /**
     * Returns the width to be used when no width is available.
     * @return a character width
     */
    public int getMissingWidth() {
        return missingWidth;
    }

    /**
     * {@inheritDoc}
     */
    public FontType getFontType() {
        return fontType;
    }

    /**
     * Returns the index of the first character defined in this font.
     * @return the index of the first character
     */
    public int getFirstChar() {
        return firstChar;
    }

    /**
     * Returns the index of the last character defined in this font.
     * @return the index of the last character
     */
    public int getLastChar() {
        return lastChar;
    }

    /**
     * Used to determine if kerning is enabled.
     * @return True if kerning is enabled.
     */
    public boolean isKerningEnabled() {
        return useKerning;
    }

    /**
     * {@inheritDoc}
     */
    public final boolean hasKerningInfo() {
        return (isKerningEnabled() && (kerning != null) && !kerning.isEmpty());
    }

    /**
     * {@inheritDoc}
     */
    public final Map getKerningInfo() {
        if (hasKerningInfo()) {
            return kerning;
        } else {
            return java.util.Collections.EMPTY_MAP;
        }
    }

    /* ---- MutableFont interface ---- */

    /** {@inheritDoc} */
    public void setFontName(String name) {
        this.fontName = name;
    }

    /** {@inheritDoc} */
    public void setFullName(String name) {
        this.fullName = name;
    }

    /** {@inheritDoc} */
    public void setFamilyNames(Set names) {
        this.familyNames = new java.util.HashSet(names);
    }

    /**
     * Sets the font's subfamily name.
     * @param subFamilyName the subfamily name of the font
     */
    public void setFontSubFamilyName(String subFamilyName) {
        this.fontSubName = subFamilyName;
    }

    /**
     * {@inheritDoc}
     */
    public void setEmbedFileName(String path) {
        this.embedFileName = path;
    }

    /**
     * {@inheritDoc}
     */
    public void setEmbedResourceName(String name) {
        this.embedResourceName = name;
    }

    /**
     * {@inheritDoc}
     */
    public void setCapHeight(int capHeight) {
        this.capHeight = capHeight;
    }

    /**
     * Returns the XHeight value of the font.
     * @param xHeight the XHeight value
     */
    public void setXHeight(int xHeight) {
        this.xHeight = xHeight;
    }

    /**
     * {@inheritDoc}
     */
    public void setAscender(int ascender) {
        this.ascender = ascender;
    }

    /**
     * {@inheritDoc}
     */
    public void setDescender(int descender) {
        this.descender = descender;
    }

    /**
     * {@inheritDoc}
     */
    public void setFontBBox(int[] bbox) {
        this.fontBBox = bbox;
    }

    /**
     * {@inheritDoc}
     */
    public void setFlags(int flags) {
        this.flags = flags;
    }

    /**
     * Sets the font weight. Valid values are 100, 200...800, 900.
     * @param weight the font weight
     */
    public void setWeight(int weight) {
        weight = (weight / 100) * 100;
        weight = Math.max(100, weight);
        weight = Math.min(900, weight);
        this.weight = weight;
    }

    /**
     * {@inheritDoc}
     */
    public void setStemV(int stemV) {
        this.stemV = stemV;
    }

    /**
     * {@inheritDoc}
     */
    public void setItalicAngle(int italicAngle) {
        this.italicAngle = italicAngle;
    }

    /**
     * {@inheritDoc}
     */
    public void setMissingWidth(int width) {
        this.missingWidth = width;
    }

    /**
     * {@inheritDoc}
     */
    public void setFontType(FontType fontType) {
        this.fontType = fontType;
    }

    /**
     * {@inheritDoc}
     */
    public void setFirstChar(int index) {
        this.firstChar = index;
    }

    /**
     * {@inheritDoc}
     */
    public void setLastChar(int index) {
        this.lastChar = index;
    }

    /**
     * {@inheritDoc}
     */
    public void setKerningEnabled(boolean enabled) {
        this.useKerning = enabled;
    }

    /**
     * Sets the font resolver. Needed for URI resolution.
     * @param resolver the font resolver
     */
    public void setResolver(FontResolver resolver) {
        this.resolver = resolver;
    }

    /** {@inheritDoc} */
    public void putKerningEntry(Integer key, Map value) {
        if (kerning == null) {
            kerning = new java.util.HashMap();
        }
        this.kerning.put(key, value);
    }

    /**
     * Replaces the existing kerning map with a new one.
     * @param kerningMap the kerning map (Map<Integer, Map<Integer, Integer>, the integers are
     *                          character codes)
     */
    public void replaceKerningMap(Map kerningMap) {
        if (kerningMap == null) {
            this.kerning = Collections.EMPTY_MAP;
        } else {
            this.kerning = kerningMap;
        }
    }

}
