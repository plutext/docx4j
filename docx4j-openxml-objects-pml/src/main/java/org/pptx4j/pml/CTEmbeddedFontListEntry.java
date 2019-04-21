/*
 *  Copyright 2010-2012, Plutext Pty Ltd.
 *   
 *  This file is part of pptx4j, a component of docx4j.

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
package org.pptx4j.pml;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlType;
import org.docx4j.dml.TextFont;


/**
 * <p>Java class for CT_EmbeddedFontListEntry complex type.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
 * 
 * <pre>
 * &lt;complexType name="CT_EmbeddedFontListEntry">
 *   &lt;complexContent>
 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
 *       &lt;sequence>
 *         &lt;element name="font" type="{http://schemas.openxmlformats.org/drawingml/2006/main}CT_TextFont"/>
 *         &lt;element name="regular" type="{http://schemas.openxmlformats.org/presentationml/2006/main}CT_EmbeddedFontDataId" minOccurs="0"/>
 *         &lt;element name="bold" type="{http://schemas.openxmlformats.org/presentationml/2006/main}CT_EmbeddedFontDataId" minOccurs="0"/>
 *         &lt;element name="italic" type="{http://schemas.openxmlformats.org/presentationml/2006/main}CT_EmbeddedFontDataId" minOccurs="0"/>
 *         &lt;element name="boldItalic" type="{http://schemas.openxmlformats.org/presentationml/2006/main}CT_EmbeddedFontDataId" minOccurs="0"/>
 *       &lt;/sequence>
 *     &lt;/restriction>
 *   &lt;/complexContent>
 * &lt;/complexType>
 * </pre>
 * 
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "CT_EmbeddedFontListEntry", propOrder = {
    "font",
    "regular",
    "bold",
    "italic",
    "boldItalic"
})
public class CTEmbeddedFontListEntry {

    @XmlElement(required = true)
    protected TextFont font;
    protected CTEmbeddedFontDataId regular;
    protected CTEmbeddedFontDataId bold;
    protected CTEmbeddedFontDataId italic;
    protected CTEmbeddedFontDataId boldItalic;

    /**
     * Gets the value of the font property.
     * 
     * @return
     *     possible object is
     *     {@link TextFont }
     *     
     */
    public TextFont getFont() {
        return font;
    }

    /**
     * Sets the value of the font property.
     * 
     * @param value
     *     allowed object is
     *     {@link TextFont }
     *     
     */
    public void setFont(TextFont value) {
        this.font = value;
    }

    /**
     * Gets the value of the regular property.
     * 
     * @return
     *     possible object is
     *     {@link CTEmbeddedFontDataId }
     *     
     */
    public CTEmbeddedFontDataId getRegular() {
        return regular;
    }

    /**
     * Sets the value of the regular property.
     * 
     * @param value
     *     allowed object is
     *     {@link CTEmbeddedFontDataId }
     *     
     */
    public void setRegular(CTEmbeddedFontDataId value) {
        this.regular = value;
    }

    /**
     * Gets the value of the bold property.
     * 
     * @return
     *     possible object is
     *     {@link CTEmbeddedFontDataId }
     *     
     */
    public CTEmbeddedFontDataId getBold() {
        return bold;
    }

    /**
     * Sets the value of the bold property.
     * 
     * @param value
     *     allowed object is
     *     {@link CTEmbeddedFontDataId }
     *     
     */
    public void setBold(CTEmbeddedFontDataId value) {
        this.bold = value;
    }

    /**
     * Gets the value of the italic property.
     * 
     * @return
     *     possible object is
     *     {@link CTEmbeddedFontDataId }
     *     
     */
    public CTEmbeddedFontDataId getItalic() {
        return italic;
    }

    /**
     * Sets the value of the italic property.
     * 
     * @param value
     *     allowed object is
     *     {@link CTEmbeddedFontDataId }
     *     
     */
    public void setItalic(CTEmbeddedFontDataId value) {
        this.italic = value;
    }

    /**
     * Gets the value of the boldItalic property.
     * 
     * @return
     *     possible object is
     *     {@link CTEmbeddedFontDataId }
     *     
     */
    public CTEmbeddedFontDataId getBoldItalic() {
        return boldItalic;
    }

    /**
     * Sets the value of the boldItalic property.
     * 
     * @param value
     *     allowed object is
     *     {@link CTEmbeddedFontDataId }
     *     
     */
    public void setBoldItalic(CTEmbeddedFontDataId value) {
        this.boldItalic = value;
    }

}
