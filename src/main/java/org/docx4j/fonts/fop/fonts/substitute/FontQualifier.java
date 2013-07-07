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

/* $Id: FontQualifier.java 679326 2008-07-24 09:35:34Z vhennebert $ */

package org.docx4j.fonts.fop.fonts.substitute;

import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.docx4j.fonts.fop.fonts.Font;
import org.docx4j.fonts.fop.fonts.FontInfo;
import org.docx4j.fonts.fop.fonts.FontTriplet;
import org.docx4j.fonts.fop.fonts.FontUtil;

/**
 * Encapsulates a font substitution qualifier
 */
public class FontQualifier {

    /** logger instance */
    private static Logger log = LoggerFactory.getLogger(FontQualifier.class);

    /** font family attribute value */
    private AttributeValue fontFamilyAttributeValue = null;

    /** font style attribute value */
    private AttributeValue fontStyleAttributeValue = null;

    /** font weight attribute value */
    private AttributeValue fontWeightAttributeValue = null;

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
            for (Iterator it = fontWeightAttribute.iterator(); it.hasNext();) {
                Object weightObj = it.next();
                if (weightObj instanceof String) {
                    String weightString = ((String)weightObj).trim();
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
    protected List/*<FontTriplet>*/ match(FontInfo fontInfo) {
        AttributeValue fontFamilyValue = getFontFamily();
        AttributeValue weightValue = getFontWeight();
        AttributeValue styleValue = getFontStyle();

        List/*<FontTriplet>*/ matchingTriplets = new java.util.ArrayList/*<FontTriplet>*/();

        // try to find matching destination font triplet
        for (Iterator attrIt = fontFamilyValue.iterator(); attrIt.hasNext();) {
            String fontFamilyString = (String)attrIt.next();
            Map/*<FontTriplet>*/ triplets = (Map/*<FontTriplet>*/)fontInfo.getFontTriplets();
            if (triplets != null) {
                Set/*<FontTriplet>*/ tripletSet = triplets.keySet();
                for (Iterator/*<FontTriplet>*/ tripletIt = tripletSet.iterator();
                        tripletIt.hasNext();) {
                    FontTriplet triplet = (FontTriplet)tripletIt.next();
                    String fontName = triplet.getName();

                    // matched font family name
                    if (fontFamilyString.toLowerCase().equals(fontName.toLowerCase())) {

                        // try and match font weight
                        boolean weightMatched = false;
                        int fontWeight = triplet.getWeight();
                        for (Iterator weightIt = weightValue.iterator(); weightIt.hasNext();) {
                            Object weightObj = weightIt.next();
                            if (weightObj instanceof FontWeightRange) {
                                FontWeightRange intRange = (FontWeightRange)weightObj;
                                if (intRange.isWithinRange(fontWeight)) {
                                    weightMatched = true;
                                }
                            } else if (weightObj instanceof String) {
                                String fontWeightString = (String)weightObj;
                                int fontWeightValue = FontUtil.parseCSS2FontWeight(
                                        fontWeightString);
                                if (fontWeightValue == fontWeight) {
                                    weightMatched = true;
                                }
                            } else if (weightObj instanceof Integer) {
                                Integer fontWeightInteger = (Integer)weightObj;
                                int fontWeightValue = fontWeightInteger.intValue();
                                if (fontWeightValue == fontWeight) {
                                    weightMatched = true;
                                }
                            }
                        }

                        // try and match font style
                        boolean styleMatched = false;
                        String fontStyleString = triplet.getStyle();
                        for (Iterator styleIt = styleValue.iterator(); styleIt.hasNext();) {
                            String style = (String)styleIt.next();
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
        List/*<FontTriplet>*/ matchingTriplets = match(fontInfo);
        FontTriplet bestTriplet = null;
        if (matchingTriplets.size() == 1) {
            bestTriplet = (FontTriplet)matchingTriplets.get(0);
        } else {
            for (Iterator iterator = matchingTriplets.iterator(); iterator.hasNext();) {
                FontTriplet triplet = (FontTriplet)iterator.next();
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
    public List/*<FontTriplet>*/ getTriplets() {
        List/*<FontTriplet>*/ triplets = new java.util.ArrayList/*<FontTriplet>*/();

        AttributeValue fontFamilyValue = getFontFamily();
        for (Iterator fontFamilyIt = fontFamilyValue.iterator(); fontFamilyIt.hasNext();) {
            String name = (String)fontFamilyIt.next();

            AttributeValue styleValue = getFontStyle();
            for (Iterator styleIt = styleValue.iterator(); styleIt.hasNext();) {
                String style = (String)styleIt.next();

                AttributeValue weightValue = getFontWeight();
                for (Iterator weightIt = weightValue.iterator(); weightIt.hasNext();) {
                    Object weightObj = weightIt.next();

                    if (weightObj instanceof FontWeightRange) {
                        FontWeightRange fontWeightRange = (FontWeightRange)weightObj;
                        int[] weightRange = fontWeightRange.toArray();
                        for (int i = 0; i < weightRange.length; i++) {
                            triplets.add(new FontTriplet(name, style, weightRange[i]));
                        }
                    } else if (weightObj instanceof String) {
                        String weightString = (String)weightObj;
                        int weight = FontUtil.parseCSS2FontWeight(weightString);
                        triplets.add(new FontTriplet(name, style, weight));
                    } else if (weightObj instanceof Integer) {
                        Integer weightInteger = (Integer)weightObj;
                        int weight = weightInteger.intValue();
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
        String str = new String();
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
