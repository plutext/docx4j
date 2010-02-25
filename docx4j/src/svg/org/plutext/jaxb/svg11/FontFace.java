/*
 *  Copyright 2010, Plutext Pty Ltd.
 *   
 *  This file is part of docx4j.

    docx4j is licensed under the Apache License, Version 2.0 (the "License"); 
    you may not use this file except in compliance with the License. 

    You may obtain a copy of the License at 

        http://www.apache.org/licenses/LICENSE-2.0 

    Unless required by applicable law or agreed to in writing, software 
    distributed under the License is distributed on an "AS IS" BASIS, 
    WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. 
    See the License for the specific language governing permissions and 
    limitations under the License.

 */


package org.plutext.jaxb.svg11;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlID;
import javax.xml.bind.annotation.XmlSchemaType;
import javax.xml.bind.annotation.XmlType;
import javax.xml.bind.annotation.adapters.CollapsedStringAdapter;
import javax.xml.bind.annotation.adapters.XmlJavaTypeAdapter;


/**
 * <p>Java class for anonymous complex type.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
 * 
 * <pre>
 * &lt;complexType>
 *   &lt;complexContent>
 *     &lt;extension base="{http://www.w3.org/2000/svg}SVG.font-face.content">
 *       &lt;attGroup ref="{http://www.w3.org/2000/svg}SVG.Core.attrib"/>
 *       &lt;attribute name="font-family" type="{http://www.w3.org/2001/XMLSchema}anySimpleType" />
 *       &lt;attribute name="font-style" type="{http://www.w3.org/2001/XMLSchema}anySimpleType" />
 *       &lt;attribute name="font-variant" type="{http://www.w3.org/2001/XMLSchema}anySimpleType" />
 *       &lt;attribute name="font-weight" type="{http://www.w3.org/2001/XMLSchema}anySimpleType" />
 *       &lt;attribute name="font-stretch" type="{http://www.w3.org/2001/XMLSchema}anySimpleType" />
 *       &lt;attribute name="font-size" type="{http://www.w3.org/2001/XMLSchema}anySimpleType" />
 *       &lt;attribute name="unicode-range" type="{http://www.w3.org/2001/XMLSchema}anySimpleType" />
 *       &lt;attribute name="units-per-em" type="{http://www.w3.org/2000/svg}Number.datatype" />
 *       &lt;attribute name="panose-1" type="{http://www.w3.org/2001/XMLSchema}anySimpleType" />
 *       &lt;attribute name="stemv" type="{http://www.w3.org/2000/svg}Number.datatype" />
 *       &lt;attribute name="stemh" type="{http://www.w3.org/2000/svg}Number.datatype" />
 *       &lt;attribute name="slope" type="{http://www.w3.org/2000/svg}Number.datatype" />
 *       &lt;attribute name="cap-height" type="{http://www.w3.org/2000/svg}Number.datatype" />
 *       &lt;attribute name="x-height" type="{http://www.w3.org/2000/svg}Number.datatype" />
 *       &lt;attribute name="accent-height" type="{http://www.w3.org/2000/svg}Number.datatype" />
 *       &lt;attribute name="ascent" type="{http://www.w3.org/2000/svg}Number.datatype" />
 *       &lt;attribute name="descent" type="{http://www.w3.org/2000/svg}Number.datatype" />
 *       &lt;attribute name="widths" type="{http://www.w3.org/2001/XMLSchema}anySimpleType" />
 *       &lt;attribute name="bbox" type="{http://www.w3.org/2001/XMLSchema}anySimpleType" />
 *       &lt;attribute name="ideographic" type="{http://www.w3.org/2000/svg}Number.datatype" />
 *       &lt;attribute name="alphabetic" type="{http://www.w3.org/2000/svg}Number.datatype" />
 *       &lt;attribute name="mathematical" type="{http://www.w3.org/2000/svg}Number.datatype" />
 *       &lt;attribute name="hanging" type="{http://www.w3.org/2000/svg}Number.datatype" />
 *       &lt;attribute name="v-ideographic" type="{http://www.w3.org/2000/svg}Number.datatype" />
 *       &lt;attribute name="v-alphabetic" type="{http://www.w3.org/2000/svg}Number.datatype" />
 *       &lt;attribute name="v-mathematical" type="{http://www.w3.org/2000/svg}Number.datatype" />
 *       &lt;attribute name="v-hanging" type="{http://www.w3.org/2000/svg}Number.datatype" />
 *       &lt;attribute name="underline-position" type="{http://www.w3.org/2000/svg}Number.datatype" />
 *       &lt;attribute name="underline-thickness" type="{http://www.w3.org/2000/svg}Number.datatype" />
 *       &lt;attribute name="strikethrough-position" type="{http://www.w3.org/2000/svg}Number.datatype" />
 *       &lt;attribute name="strikethrough-thickness" type="{http://www.w3.org/2000/svg}Number.datatype" />
 *       &lt;attribute name="overline-position" type="{http://www.w3.org/2000/svg}Number.datatype" />
 *       &lt;attribute name="overline-thickness" type="{http://www.w3.org/2000/svg}Number.datatype" />
 *     &lt;/extension>
 *   &lt;/complexContent>
 * &lt;/complexType>
 * </pre>
 * 
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "")
public class FontFace
    extends SVGFontFaceContent
{

    @XmlAttribute(name = "font-family")
    @XmlSchemaType(name = "anySimpleType")
    protected String fontFamily;
    @XmlAttribute(name = "font-style")
    @XmlSchemaType(name = "anySimpleType")
    protected String fontStyle;
    @XmlAttribute(name = "font-variant")
    @XmlSchemaType(name = "anySimpleType")
    protected String fontVariant;
    @XmlAttribute(name = "font-weight")
    @XmlSchemaType(name = "anySimpleType")
    protected String fontWeight;
    @XmlAttribute(name = "font-stretch")
    @XmlSchemaType(name = "anySimpleType")
    protected String fontStretch;
    @XmlAttribute(name = "font-size")
    @XmlSchemaType(name = "anySimpleType")
    protected String fontSize;
    @XmlAttribute(name = "unicode-range")
    @XmlSchemaType(name = "anySimpleType")
    protected String unicodeRange;
    @XmlAttribute(name = "units-per-em")
    protected String unitsPerEm;
    @XmlAttribute(name = "panose-1")
    @XmlSchemaType(name = "anySimpleType")
    protected String panose1;
    @XmlAttribute
    protected String stemv;
    @XmlAttribute
    protected String stemh;
    @XmlAttribute
    protected String slope;
    @XmlAttribute(name = "cap-height")
    protected String capHeight;
    @XmlAttribute(name = "x-height")
    protected String xHeight;
    @XmlAttribute(name = "accent-height")
    protected String accentHeight;
    @XmlAttribute
    protected String ascent;
    @XmlAttribute
    protected String descent;
    @XmlAttribute
    @XmlSchemaType(name = "anySimpleType")
    protected String widths;
    @XmlAttribute
    @XmlSchemaType(name = "anySimpleType")
    protected String bbox;
    @XmlAttribute
    protected String ideographic;
    @XmlAttribute
    protected String alphabetic;
    @XmlAttribute
    protected String mathematical;
    @XmlAttribute
    protected String hanging;
    @XmlAttribute(name = "v-ideographic")
    protected String vIdeographic;
    @XmlAttribute(name = "v-alphabetic")
    protected String vAlphabetic;
    @XmlAttribute(name = "v-mathematical")
    protected String vMathematical;
    @XmlAttribute(name = "v-hanging")
    protected String vHanging;
    @XmlAttribute(name = "underline-position")
    protected String underlinePosition;
    @XmlAttribute(name = "underline-thickness")
    protected String underlineThickness;
    @XmlAttribute(name = "strikethrough-position")
    protected String strikethroughPosition;
    @XmlAttribute(name = "strikethrough-thickness")
    protected String strikethroughThickness;
    @XmlAttribute(name = "overline-position")
    protected String overlinePosition;
    @XmlAttribute(name = "overline-thickness")
    protected String overlineThickness;
    @XmlAttribute
    @XmlJavaTypeAdapter(CollapsedStringAdapter.class)
    @XmlID
    @XmlSchemaType(name = "ID")
    protected String id;
    @XmlAttribute(namespace = "http://www.w3.org/XML/1998/namespace")
    protected String base;
    @XmlAttribute(namespace = "http://www.w3.org/XML/1998/namespace", required = true)
    @XmlJavaTypeAdapter(CollapsedStringAdapter.class)
    protected String space;
    @XmlAttribute(namespace = "http://www.w3.org/XML/1998/namespace")
    @XmlJavaTypeAdapter(CollapsedStringAdapter.class)
    protected String lang;

    /**
     * Gets the value of the fontFamily property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getFontFamily() {
        return fontFamily;
    }

    /**
     * Sets the value of the fontFamily property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setFontFamily(String value) {
        this.fontFamily = value;
    }

    /**
     * Gets the value of the fontStyle property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getFontStyle() {
        return fontStyle;
    }

    /**
     * Sets the value of the fontStyle property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setFontStyle(String value) {
        this.fontStyle = value;
    }

    /**
     * Gets the value of the fontVariant property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getFontVariant() {
        return fontVariant;
    }

    /**
     * Sets the value of the fontVariant property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setFontVariant(String value) {
        this.fontVariant = value;
    }

    /**
     * Gets the value of the fontWeight property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getFontWeight() {
        return fontWeight;
    }

    /**
     * Sets the value of the fontWeight property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setFontWeight(String value) {
        this.fontWeight = value;
    }

    /**
     * Gets the value of the fontStretch property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getFontStretch() {
        return fontStretch;
    }

    /**
     * Sets the value of the fontStretch property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setFontStretch(String value) {
        this.fontStretch = value;
    }

    /**
     * Gets the value of the fontSize property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getFontSize() {
        return fontSize;
    }

    /**
     * Sets the value of the fontSize property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setFontSize(String value) {
        this.fontSize = value;
    }

    /**
     * Gets the value of the unicodeRange property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getUnicodeRange() {
        return unicodeRange;
    }

    /**
     * Sets the value of the unicodeRange property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setUnicodeRange(String value) {
        this.unicodeRange = value;
    }

    /**
     * Gets the value of the unitsPerEm property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getUnitsPerEm() {
        return unitsPerEm;
    }

    /**
     * Sets the value of the unitsPerEm property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setUnitsPerEm(String value) {
        this.unitsPerEm = value;
    }

    /**
     * Gets the value of the panose1 property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getPanose1() {
        return panose1;
    }

    /**
     * Sets the value of the panose1 property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setPanose1(String value) {
        this.panose1 = value;
    }

    /**
     * Gets the value of the stemv property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getStemv() {
        return stemv;
    }

    /**
     * Sets the value of the stemv property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setStemv(String value) {
        this.stemv = value;
    }

    /**
     * Gets the value of the stemh property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getStemh() {
        return stemh;
    }

    /**
     * Sets the value of the stemh property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setStemh(String value) {
        this.stemh = value;
    }

    /**
     * Gets the value of the slope property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getSlope() {
        return slope;
    }

    /**
     * Sets the value of the slope property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setSlope(String value) {
        this.slope = value;
    }

    /**
     * Gets the value of the capHeight property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getCapHeight() {
        return capHeight;
    }

    /**
     * Sets the value of the capHeight property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setCapHeight(String value) {
        this.capHeight = value;
    }

    /**
     * Gets the value of the xHeight property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getXHeight() {
        return xHeight;
    }

    /**
     * Sets the value of the xHeight property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setXHeight(String value) {
        this.xHeight = value;
    }

    /**
     * Gets the value of the accentHeight property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getAccentHeight() {
        return accentHeight;
    }

    /**
     * Sets the value of the accentHeight property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setAccentHeight(String value) {
        this.accentHeight = value;
    }

    /**
     * Gets the value of the ascent property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getAscent() {
        return ascent;
    }

    /**
     * Sets the value of the ascent property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setAscent(String value) {
        this.ascent = value;
    }

    /**
     * Gets the value of the descent property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getDescent() {
        return descent;
    }

    /**
     * Sets the value of the descent property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setDescent(String value) {
        this.descent = value;
    }

    /**
     * Gets the value of the widths property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getWidths() {
        return widths;
    }

    /**
     * Sets the value of the widths property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setWidths(String value) {
        this.widths = value;
    }

    /**
     * Gets the value of the bbox property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getBbox() {
        return bbox;
    }

    /**
     * Sets the value of the bbox property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setBbox(String value) {
        this.bbox = value;
    }

    /**
     * Gets the value of the ideographic property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getIdeographic() {
        return ideographic;
    }

    /**
     * Sets the value of the ideographic property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setIdeographic(String value) {
        this.ideographic = value;
    }

    /**
     * Gets the value of the alphabetic property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getAlphabetic() {
        return alphabetic;
    }

    /**
     * Sets the value of the alphabetic property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setAlphabetic(String value) {
        this.alphabetic = value;
    }

    /**
     * Gets the value of the mathematical property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getMathematical() {
        return mathematical;
    }

    /**
     * Sets the value of the mathematical property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setMathematical(String value) {
        this.mathematical = value;
    }

    /**
     * Gets the value of the hanging property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getHanging() {
        return hanging;
    }

    /**
     * Sets the value of the hanging property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setHanging(String value) {
        this.hanging = value;
    }

    /**
     * Gets the value of the vIdeographic property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getVIdeographic() {
        return vIdeographic;
    }

    /**
     * Sets the value of the vIdeographic property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setVIdeographic(String value) {
        this.vIdeographic = value;
    }

    /**
     * Gets the value of the vAlphabetic property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getVAlphabetic() {
        return vAlphabetic;
    }

    /**
     * Sets the value of the vAlphabetic property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setVAlphabetic(String value) {
        this.vAlphabetic = value;
    }

    /**
     * Gets the value of the vMathematical property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getVMathematical() {
        return vMathematical;
    }

    /**
     * Sets the value of the vMathematical property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setVMathematical(String value) {
        this.vMathematical = value;
    }

    /**
     * Gets the value of the vHanging property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getVHanging() {
        return vHanging;
    }

    /**
     * Sets the value of the vHanging property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setVHanging(String value) {
        this.vHanging = value;
    }

    /**
     * Gets the value of the underlinePosition property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getUnderlinePosition() {
        return underlinePosition;
    }

    /**
     * Sets the value of the underlinePosition property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setUnderlinePosition(String value) {
        this.underlinePosition = value;
    }

    /**
     * Gets the value of the underlineThickness property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getUnderlineThickness() {
        return underlineThickness;
    }

    /**
     * Sets the value of the underlineThickness property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setUnderlineThickness(String value) {
        this.underlineThickness = value;
    }

    /**
     * Gets the value of the strikethroughPosition property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getStrikethroughPosition() {
        return strikethroughPosition;
    }

    /**
     * Sets the value of the strikethroughPosition property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setStrikethroughPosition(String value) {
        this.strikethroughPosition = value;
    }

    /**
     * Gets the value of the strikethroughThickness property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getStrikethroughThickness() {
        return strikethroughThickness;
    }

    /**
     * Sets the value of the strikethroughThickness property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setStrikethroughThickness(String value) {
        this.strikethroughThickness = value;
    }

    /**
     * Gets the value of the overlinePosition property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getOverlinePosition() {
        return overlinePosition;
    }

    /**
     * Sets the value of the overlinePosition property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setOverlinePosition(String value) {
        this.overlinePosition = value;
    }

    /**
     * Gets the value of the overlineThickness property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getOverlineThickness() {
        return overlineThickness;
    }

    /**
     * Sets the value of the overlineThickness property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setOverlineThickness(String value) {
        this.overlineThickness = value;
    }

    /**
     * Gets the value of the id property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getId() {
        return id;
    }

    /**
     * Sets the value of the id property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setId(String value) {
        this.id = value;
    }

    /**
     * Gets the value of the base property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getBase() {
        return base;
    }

    /**
     * Sets the value of the base property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setBase(String value) {
        this.base = value;
    }

    /**
     * Gets the value of the space property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getSpace() {
        return space;
    }

    /**
     * Sets the value of the space property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setSpace(String value) {
        this.space = value;
    }

    /**
     * Gets the value of the lang property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getLang() {
        return lang;
    }

    /**
     * Sets the value of the lang property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setLang(String value) {
        this.lang = value;
    }

}
