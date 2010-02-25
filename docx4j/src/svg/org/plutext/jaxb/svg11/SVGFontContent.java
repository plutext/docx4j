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
import javax.xml.bind.JAXBElement;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlElementRef;
import javax.xml.bind.annotation.XmlElements;
import javax.xml.bind.annotation.XmlType;


/**
 * <p>Java class for SVG.font.content complex type.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
 * 
 * <pre>
 * &lt;complexType name="SVG.font.content">
 *   &lt;complexContent>
 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
 *       &lt;sequence>
 *         &lt;element ref="{http://www.w3.org/2000/svg}SVG.Description.class" maxOccurs="unbounded" minOccurs="0"/>
 *         &lt;element ref="{http://www.w3.org/2000/svg}font-face"/>
 *         &lt;element ref="{http://www.w3.org/2000/svg}missing-glyph"/>
 *         &lt;choice maxOccurs="unbounded" minOccurs="0">
 *           &lt;element ref="{http://www.w3.org/2000/svg}glyph"/>
 *           &lt;element ref="{http://www.w3.org/2000/svg}hkern"/>
 *           &lt;element ref="{http://www.w3.org/2000/svg}vkern"/>
 *         &lt;/choice>
 *       &lt;/sequence>
 *     &lt;/restriction>
 *   &lt;/complexContent>
 * &lt;/complexType>
 * </pre>
 * 
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "SVG.font.content", propOrder = {
    "svgDescriptionClass",
    "fontFace",
    "missingGlyph",
    "glyphOrHkernOrVkern"
})
public class SVGFontContent {

    @XmlElementRef(name = "SVG.Description.class", namespace = "http://www.w3.org/2000/svg", type = JAXBElement.class)
    protected List<JAXBElement<?>> svgDescriptionClass;
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

    /**
     * Gets the value of the svgDescriptionClass property.
     * 
     * <p>
     * This accessor method returns a reference to the live list,
     * not a snapshot. Therefore any modification you make to the
     * returned list will be present inside the JAXB object.
     * This is why there is not a <CODE>set</CODE> method for the svgDescriptionClass property.
     * 
     * <p>
     * For example, to add a new item, do as follows:
     * <pre>
     *    getSVGDescriptionClass().add(newItem);
     * </pre>
     * 
     * 
     * <p>
     * Objects of the following type(s) are allowed in the list
     * {@link JAXBElement }{@code <}{@link Desc }{@code >}
     * {@link JAXBElement }{@code <}{@link Metadata }{@code >}
     * {@link JAXBElement }{@code <}{@link Object }{@code >}
     * {@link JAXBElement }{@code <}{@link Title }{@code >}
     * 
     * 
     */
    public List<JAXBElement<?>> getSVGDescriptionClass() {
        if (svgDescriptionClass == null) {
            svgDescriptionClass = new ArrayList<JAXBElement<?>>();
        }
        return this.svgDescriptionClass;
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

}
