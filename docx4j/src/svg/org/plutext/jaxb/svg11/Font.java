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

import java.util.ArrayList;
import java.util.List;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlElements;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlSchemaType;
import javax.xml.bind.annotation.XmlType;


/**
 * <p>Java class for anonymous complex type.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
 * 
 * <pre>
 * &lt;complexType>
 *   &lt;complexContent>
 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
 *       &lt;sequence>
 *         &lt;choice maxOccurs="unbounded" minOccurs="0">
 *           &lt;element ref="{http://www.w3.org/2000/svg}desc"/>
 *           &lt;element ref="{http://www.w3.org/2000/svg}title"/>
 *           &lt;element ref="{http://www.w3.org/2000/svg}metadata"/>
 *         &lt;/choice>
 *         &lt;element ref="{http://www.w3.org/2000/svg}font-face"/>
 *         &lt;element ref="{http://www.w3.org/2000/svg}missing-glyph"/>
 *         &lt;choice maxOccurs="unbounded" minOccurs="0">
 *           &lt;element ref="{http://www.w3.org/2000/svg}glyph"/>
 *           &lt;element ref="{http://www.w3.org/2000/svg}hkern"/>
 *           &lt;element ref="{http://www.w3.org/2000/svg}vkern"/>
 *         &lt;/choice>
 *       &lt;/sequence>
 *       &lt;attGroup ref="{http://www.w3.org/2000/svg}SVG.Presentation.attrib"/>
 *       &lt;attribute name="horiz-origin-x" type="{http://www.w3.org/2001/XMLSchema}anySimpleType" />
 *       &lt;attribute name="horiz-origin-y" type="{http://www.w3.org/2001/XMLSchema}anySimpleType" />
 *       &lt;attribute name="horiz-adv-x" use="required" type="{http://www.w3.org/2001/XMLSchema}anySimpleType" />
 *       &lt;attribute name="vert-origin-x" type="{http://www.w3.org/2001/XMLSchema}anySimpleType" />
 *       &lt;attribute name="vert-origin-y" type="{http://www.w3.org/2001/XMLSchema}anySimpleType" />
 *       &lt;attribute name="vert-adv-y" type="{http://www.w3.org/2001/XMLSchema}anySimpleType" />
 *     &lt;/restriction>
 *   &lt;/complexContent>
 * &lt;/complexType>
 * </pre>
 * 
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "", propOrder = {
    "descOrTitleOrMetadata",
    "fontFace",
    "missingGlyph",
    "glyphOrHkernOrVkern"
})
@XmlRootElement(name = "font")
public class Font {

    @XmlElements({
        @XmlElement(name = "metadata", type = Metadata.class),
        @XmlElement(name = "desc", type = Desc.class),
        @XmlElement(name = "title", type = Title.class)
    })
    protected List<Object> descOrTitleOrMetadata;
    @XmlElement(name = "font-face", required = true)
    protected FontFace fontFace;
    @XmlElement(name = "missing-glyph", required = true)
    protected MissingGlyph missingGlyph;
    @XmlElements({
        @XmlElement(name = "hkern", type = Hkern.class),
        @XmlElement(name = "vkern", type = Vkern.class),
        @XmlElement(name = "glyph", type = Glyph.class)
    })
    protected List<Object> glyphOrHkernOrVkern;
    @XmlAttribute(name = "horiz-origin-x")
    @XmlSchemaType(name = "anySimpleType")
    protected String horizOriginX;
    @XmlAttribute(name = "horiz-origin-y")
    @XmlSchemaType(name = "anySimpleType")
    protected String horizOriginY;
    @XmlAttribute(name = "horiz-adv-x", required = true)
    @XmlSchemaType(name = "anySimpleType")
    protected String horizAdvX;
    @XmlAttribute(name = "vert-origin-x")
    @XmlSchemaType(name = "anySimpleType")
    protected String vertOriginX;
    @XmlAttribute(name = "vert-origin-y")
    @XmlSchemaType(name = "anySimpleType")
    protected String vertOriginY;
    @XmlAttribute(name = "vert-adv-y")
    @XmlSchemaType(name = "anySimpleType")
    protected String vertAdvY;
    @XmlAttribute(name = "flood-color")
    @XmlSchemaType(name = "anySimpleType")
    protected String floodColor;
    @XmlAttribute(name = "flood-opacity")
    @XmlSchemaType(name = "anySimpleType")
    protected String floodOpacity;
    @XmlAttribute(name = "lighting-color")
    @XmlSchemaType(name = "anySimpleType")
    protected String lightingColor;

    /**
     * Gets the value of the descOrTitleOrMetadata property.
     * 
     * <p>
     * This accessor method returns a reference to the live list,
     * not a snapshot. Therefore any modification you make to the
     * returned list will be present inside the JAXB object.
     * This is why there is not a <CODE>set</CODE> method for the descOrTitleOrMetadata property.
     * 
     * <p>
     * For example, to add a new item, do as follows:
     * <pre>
     *    getDescOrTitleOrMetadata().add(newItem);
     * </pre>
     * 
     * 
     * <p>
     * Objects of the following type(s) are allowed in the list
     * {@link Metadata }
     * {@link Desc }
     * {@link Title }
     * 
     * 
     */
    public List<Object> getDescOrTitleOrMetadata() {
        if (descOrTitleOrMetadata == null) {
            descOrTitleOrMetadata = new ArrayList<Object>();
        }
        return this.descOrTitleOrMetadata;
    }

    /**
     * Gets the value of the fontFace property.
     * 
     * @return
     *     possible object is
     *     {@link FontFace }
     *     
     */
    public FontFace getFontFace() {
        return fontFace;
    }

    /**
     * Sets the value of the fontFace property.
     * 
     * @param value
     *     allowed object is
     *     {@link FontFace }
     *     
     */
    public void setFontFace(FontFace value) {
        this.fontFace = value;
    }

    /**
     * Gets the value of the missingGlyph property.
     * 
     * @return
     *     possible object is
     *     {@link MissingGlyph }
     *     
     */
    public MissingGlyph getMissingGlyph() {
        return missingGlyph;
    }

    /**
     * Sets the value of the missingGlyph property.
     * 
     * @param value
     *     allowed object is
     *     {@link MissingGlyph }
     *     
     */
    public void setMissingGlyph(MissingGlyph value) {
        this.missingGlyph = value;
    }

    /**
     * Gets the value of the glyphOrHkernOrVkern property.
     * 
     * <p>
     * This accessor method returns a reference to the live list,
     * not a snapshot. Therefore any modification you make to the
     * returned list will be present inside the JAXB object.
     * This is why there is not a <CODE>set</CODE> method for the glyphOrHkernOrVkern property.
     * 
     * <p>
     * For example, to add a new item, do as follows:
     * <pre>
     *    getGlyphOrHkernOrVkern().add(newItem);
     * </pre>
     * 
     * 
     * <p>
     * Objects of the following type(s) are allowed in the list
     * {@link Hkern }
     * {@link Vkern }
     * {@link Glyph }
     * 
     * 
     */
    public List<Object> getGlyphOrHkernOrVkern() {
        if (glyphOrHkernOrVkern == null) {
            glyphOrHkernOrVkern = new ArrayList<Object>();
        }
        return this.glyphOrHkernOrVkern;
    }

    /**
     * Gets the value of the horizOriginX property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getHorizOriginX() {
        return horizOriginX;
    }

    /**
     * Sets the value of the horizOriginX property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setHorizOriginX(String value) {
        this.horizOriginX = value;
    }

    /**
     * Gets the value of the horizOriginY property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getHorizOriginY() {
        return horizOriginY;
    }

    /**
     * Sets the value of the horizOriginY property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setHorizOriginY(String value) {
        this.horizOriginY = value;
    }

    /**
     * Gets the value of the horizAdvX property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getHorizAdvX() {
        return horizAdvX;
    }

    /**
     * Sets the value of the horizAdvX property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setHorizAdvX(String value) {
        this.horizAdvX = value;
    }

    /**
     * Gets the value of the vertOriginX property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getVertOriginX() {
        return vertOriginX;
    }

    /**
     * Sets the value of the vertOriginX property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setVertOriginX(String value) {
        this.vertOriginX = value;
    }

    /**
     * Gets the value of the vertOriginY property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getVertOriginY() {
        return vertOriginY;
    }

    /**
     * Sets the value of the vertOriginY property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setVertOriginY(String value) {
        this.vertOriginY = value;
    }

    /**
     * Gets the value of the vertAdvY property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getVertAdvY() {
        return vertAdvY;
    }

    /**
     * Sets the value of the vertAdvY property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setVertAdvY(String value) {
        this.vertAdvY = value;
    }

    /**
     * Gets the value of the floodColor property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getFloodColor() {
        return floodColor;
    }

    /**
     * Sets the value of the floodColor property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setFloodColor(String value) {
        this.floodColor = value;
    }

    /**
     * Gets the value of the floodOpacity property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getFloodOpacity() {
        return floodOpacity;
    }

    /**
     * Sets the value of the floodOpacity property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setFloodOpacity(String value) {
        this.floodOpacity = value;
    }

    /**
     * Gets the value of the lightingColor property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getLightingColor() {
        return lightingColor;
    }

    /**
     * Sets the value of the lightingColor property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setLightingColor(String value) {
        this.lightingColor = value;
    }

}
