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

/* $Id$ */

package org.docx4j.fonts.fop.fonts.substitute;

import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.docx4j.fonts.fop.fonts.Font;
import org.docx4j.fonts.fop.fonts.FontInfo;
import org.docx4j.fonts.fop.fonts.FontTriplet;
import org.docx4j.fonts.fop.fonts.FontUtil;

/**
 * Encapsulates a font substitution qualifier
 */
public class FontQualifier {

    /** logger instance */
    private static Log log = LogFactory.getLog(FontQualifier.class);

    /** font family attribute value */
    private AttributeValue fontFamilyAttributeValue;

    /** font style attribute value */
    private AttributeValue fontStyleAttributeValue;

    /** font weight attribute value */
    private AttributeValue fontWeightAttributeValue;

    /**
     * Default constructor
     */
    public FontQualifier() {
    }

    /**
     * Sets the font family
     * @param fontFamily the font family
     */
    public void setFontFamily(String fontFamily) {
        AttributeValue fontFamilyAttribute = AttributeValue.valueOf(fontFamily);
        if (fontFamilyAttribute == null) {
            log.error("Invalid font-family value '" + fontFamily + "'");
            return;
        }
        this.fontFamilyAttributeValue = fontFamilyAttribute;
    }

    /**
     * Sets the font style
     * @param fontStyle the font style
     */
    public void setFontStyle(String fontStyle) {
        AttributeValue fontStyleAttribute = AttributeValue.valueOf(fontStyle);
        if (fontStyleAttribute != null) {
            this.fontStyleAttributeValue = fontStyleAttribute;
        }
    }

    /**
     * Sets the font weight
     * @param fontWeight the font weight
     */
    public void setFontWeight(String fontWeight) {
        AttributeValue fontWeightAttribute = AttributeValue.valueOf(fontWeight);
        if (fontWeightAttribute != null) {
            for (Object weightObj : fontWeightAttribute) {
                if (weightObj instanceof String) {
                    String weightString = ((String) weightObj).trim();
                    try {
                        FontUtil.parseCSS2FontWeight(weightString);
                    } catch (IllegalArgumentException ex) {
                        log.error("Invalid font-weight value '" + weightString + "'");
                        return;
                    }
                }
            }
            this.fontWeightAttributeValue = fontWeightAttribute;
        }
    }

    /**
     * @return the font family attribute
     */
    public AttributeValue getFontFamily() {
        return this.fontFamilyAttributeValue;
    }

    /**
     * @return the font style attribute
     */
    public AttributeValue getFontStyle() {
        if (fontStyleAttributeValue == null) {
            return AttributeValue.valueOf(Font.STYLE_NORMAL);
        }
        return this.fontStyleAttributeValue;
    }

    /**
     * @return the font weight attribute
     */
    public AttributeValue getFontWeight() {
        if (fontWeightAttributeValue == null) {
            return AttributeValue.valueOf(Integer.toString(Font.WEIGHT_NORMAL));
        }
        return this.fontWeightAttributeValue;
    }

    /**
     * @return true if this rule has a font weight
     */
    public boolean hasFontWeight() {
        return this.fontWeightAttributeValue != null;
    }

    /**
     * @return true if this rule has a font style
     */
    public boolean hasFontStyle() {
        return this.fontStyleAttributeValue != null;
    }

    /**
     * Returns a list of matching font triplet found in a given font info
     *
     * @param fontInfo the font info
     * @return a list of matching font triplets
     */
    protected List<FontTriplet> match(FontInfo fontInfo) {
        AttributeValue fontFamilyValue = getFontFamily();
        AttributeValue weightValue = getFontWeight();
        AttributeValue styleValue = getFontStyle();

        List<FontTriplet> matchingTriplets = new java.util.ArrayList<FontTriplet>();

        // try to find matching destination font triplet
        for (Object aFontFamilyValue : fontFamilyValue) {
            String fontFamilyString = (String) aFontFamilyValue;
            Map<FontTriplet, String> triplets = fontInfo.getFontTriplets();
            if (triplets != null) {
                Set<FontTriplet> tripletSet = triplets.keySet();
                for (Object aTripletSet : tripletSet) {
                    FontTriplet triplet = (FontTriplet) aTripletSet;
                    String fontName = triplet.getName();

                    // matched font family name
                    if (fontFamilyString.toLowerCase().equals(fontName.toLowerCase())) {

                        // try and match font weight
                        boolean weightMatched = false;
                        int fontWeight = triplet.getWeight();
                        for (Object weightObj : weightValue) {
                            if (weightObj instanceof FontWeightRange) {
                                FontWeightRange intRange = (FontWeightRange) weightObj;
                                if (intRange.isWithinRange(fontWeight)) {
                                    weightMatched = true;
                                }
                            } else if (weightObj instanceof String) {
                                String fontWeightString = (String) weightObj;
                                int fontWeightValue = FontUtil.parseCSS2FontWeight(
                                        fontWeightString);
                                if (fontWeightValue == fontWeight) {
                                    weightMatched = true;
                                }
                            } else if (weightObj instanceof Integer) {
                                Integer fontWeightInteger = (Integer) weightObj;
                                int fontWeightValue = fontWeightInteger;
                                if (fontWeightValue == fontWeight) {
                                    weightMatched = true;
                                }
                            }
                        }

                        // try and match font style
                        boolean styleMatched = false;
                        String fontStyleString = triplet.getStyle();
                        for (Object aStyleValue : styleValue) {
                            String style = (String) aStyleValue;
                            if (fontStyleString.equals(style)) {
                                styleMatched = true;
                            }
                        }

                        if (weightMatched && styleMatched) {
                            matchingTriplets.add(triplet);
                        }
                    }
                }
            }
        }
        return matchingTriplets;
    }

    /**
     * Returns the highest priority matching font triplet found in a given font info
     * @param fontInfo the font info
     * @return the highest priority matching font triplet
     */
    protected FontTriplet bestMatch(FontInfo fontInfo) {
        List<FontTriplet> matchingTriplets = match(fontInfo);
        FontTriplet bestTriplet = null;
        if (matchingTriplets.size() == 1) {
            bestTriplet = matchingTriplets.get(0);
        } else {
            for (Object matchingTriplet : matchingTriplets) {
                FontTriplet triplet = (FontTriplet) matchingTriplet;
                if (bestTriplet == null) {
                    bestTriplet = triplet;
                } else {
                    int priority = triplet.getPriority();
                    if (priority < bestTriplet.getPriority()) {
                        bestTriplet = triplet;
                    }
                }
            }
        }
        return bestTriplet;
    }

    /**
     * @return a list of font triplets matching this qualifier
     */
    public List<FontTriplet> getTriplets() {
        List<FontTriplet> triplets = new java.util.ArrayList<FontTriplet>();

        AttributeValue fontFamilyValue = getFontFamily();
        for (Object aFontFamilyValue : fontFamilyValue) {
            String name = (String) aFontFamilyValue;

            AttributeValue styleValue = getFontStyle();
            for (Object aStyleValue : styleValue) {
                String style = (String) aStyleValue;

                AttributeValue weightValue = getFontWeight();
                for (Object weightObj : weightValue) {
                    if (weightObj instanceof FontWeightRange) {
                        FontWeightRange fontWeightRange = (FontWeightRange) weightObj;
                        int[] weightRange = fontWeightRange.toArray();
                        for (int aWeightRange : weightRange) {
                            triplets.add(new FontTriplet(name, style, aWeightRange));
                        }
                    } else if (weightObj instanceof String) {
                        String weightString = (String) weightObj;
                        int weight = FontUtil.parseCSS2FontWeight(weightString);
                        triplets.add(new FontTriplet(name, style, weight));
                    } else if (weightObj instanceof Integer) {
                        Integer weightInteger = (Integer) weightObj;
                        int weight = weightInteger;
                        triplets.add(new FontTriplet(name, style, weight));
                    }
                }
            }
        }
        return triplets;
    }

    /**
     * {@inheritDoc}
     */
    public String toString() {
        String str = "";
        if (fontFamilyAttributeValue != null) {
            str += "font-family=" + fontFamilyAttributeValue;
        }
        if (fontStyleAttributeValue != null) {
            if (str.length() > 0) {
                str += ", ";
            }
            str += "font-style=" + fontStyleAttributeValue;
        }
        if (fontWeightAttributeValue != null) {
            if (str.length() > 0) {
                str += ", ";
            }
            str += "font-weight=" + fontWeightAttributeValue;
        }
        return str;
    }
}
