/*
 *  Copyright 2007-2008, Plutext Pty Ltd.
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


package org.docx4j.dml;

import javax.xml.bind.Unmarshaller;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlTransient;
import javax.xml.bind.annotation.XmlType;
import org.jvnet.jaxb2_commons.ppp.Child;


/**
 * <p>Java class for CT_TableStyleTextStyle complex type.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
 * 
 * <pre>
 * &lt;complexType name="CT_TableStyleTextStyle"&gt;
 *   &lt;complexContent&gt;
 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType"&gt;
 *       &lt;sequence&gt;
 *         &lt;group ref="{http://schemas.openxmlformats.org/drawingml/2006/main}EG_ThemeableFontStyles" minOccurs="0"/&gt;
 *         &lt;group ref="{http://schemas.openxmlformats.org/drawingml/2006/main}EG_ColorChoice" minOccurs="0"/&gt;
 *         &lt;element name="extLst" type="{http://schemas.openxmlformats.org/drawingml/2006/main}CT_OfficeArtExtensionList" minOccurs="0"/&gt;
 *       &lt;/sequence&gt;
 *       &lt;attribute name="b" type="{http://schemas.openxmlformats.org/drawingml/2006/main}ST_OnOffStyleType" default="def" /&gt;
 *       &lt;attribute name="i" type="{http://schemas.openxmlformats.org/drawingml/2006/main}ST_OnOffStyleType" default="def" /&gt;
 *     &lt;/restriction&gt;
 *   &lt;/complexContent&gt;
 * &lt;/complexType&gt;
 * </pre>
 * 
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "CT_TableStyleTextStyle", propOrder = {
    "font",
    "fontRef",
    "scrgbClr",
    "srgbClr",
    "hslClr",
    "sysClr",
    "schemeClr",
    "prstClr",
    "extLst"
})
public class CTTableStyleTextStyle implements Child
{

    protected FontCollection font;
    protected CTFontReference fontRef;
    protected CTScRgbColor scrgbClr;
    protected CTSRgbColor srgbClr;
    protected CTHslColor hslClr;
    protected CTSystemColor sysClr;
    protected CTSchemeColor schemeClr;
    protected CTPresetColor prstClr;
    protected CTOfficeArtExtensionList extLst;
    @XmlAttribute(name = "b")
    protected STOnOffStyleType b;
    @XmlAttribute(name = "i")
    protected STOnOffStyleType i;
    @XmlTransient
    private Object parent;

    /**
     * Gets the value of the font property.
     * 
     * @return
     *     possible object is
     *     {@link FontCollection }
     *     
     */
    public FontCollection getFont() {
        return font;
    }

    /**
     * Sets the value of the font property.
     * 
     * @param value
     *     allowed object is
     *     {@link FontCollection }
     *     
     */
    public void setFont(FontCollection value) {
        this.font = value;
    }

    /**
     * Gets the value of the fontRef property.
     * 
     * @return
     *     possible object is
     *     {@link CTFontReference }
     *     
     */
    public CTFontReference getFontRef() {
        return fontRef;
    }

    /**
     * Sets the value of the fontRef property.
     * 
     * @param value
     *     allowed object is
     *     {@link CTFontReference }
     *     
     */
    public void setFontRef(CTFontReference value) {
        this.fontRef = value;
    }

    /**
     * Gets the value of the scrgbClr property.
     * 
     * @return
     *     possible object is
     *     {@link CTScRgbColor }
     *     
     */
    public CTScRgbColor getScrgbClr() {
        return scrgbClr;
    }

    /**
     * Sets the value of the scrgbClr property.
     * 
     * @param value
     *     allowed object is
     *     {@link CTScRgbColor }
     *     
     */
    public void setScrgbClr(CTScRgbColor value) {
        this.scrgbClr = value;
    }

    /**
     * Gets the value of the srgbClr property.
     * 
     * @return
     *     possible object is
     *     {@link CTSRgbColor }
     *     
     */
    public CTSRgbColor getSrgbClr() {
        return srgbClr;
    }

    /**
     * Sets the value of the srgbClr property.
     * 
     * @param value
     *     allowed object is
     *     {@link CTSRgbColor }
     *     
     */
    public void setSrgbClr(CTSRgbColor value) {
        this.srgbClr = value;
    }

    /**
     * Gets the value of the hslClr property.
     * 
     * @return
     *     possible object is
     *     {@link CTHslColor }
     *     
     */
    public CTHslColor getHslClr() {
        return hslClr;
    }

    /**
     * Sets the value of the hslClr property.
     * 
     * @param value
     *     allowed object is
     *     {@link CTHslColor }
     *     
     */
    public void setHslClr(CTHslColor value) {
        this.hslClr = value;
    }

    /**
     * Gets the value of the sysClr property.
     * 
     * @return
     *     possible object is
     *     {@link CTSystemColor }
     *     
     */
    public CTSystemColor getSysClr() {
        return sysClr;
    }

    /**
     * Sets the value of the sysClr property.
     * 
     * @param value
     *     allowed object is
     *     {@link CTSystemColor }
     *     
     */
    public void setSysClr(CTSystemColor value) {
        this.sysClr = value;
    }

    /**
     * Gets the value of the schemeClr property.
     * 
     * @return
     *     possible object is
     *     {@link CTSchemeColor }
     *     
     */
    public CTSchemeColor getSchemeClr() {
        return schemeClr;
    }

    /**
     * Sets the value of the schemeClr property.
     * 
     * @param value
     *     allowed object is
     *     {@link CTSchemeColor }
     *     
     */
    public void setSchemeClr(CTSchemeColor value) {
        this.schemeClr = value;
    }

    /**
     * Gets the value of the prstClr property.
     * 
     * @return
     *     possible object is
     *     {@link CTPresetColor }
     *     
     */
    public CTPresetColor getPrstClr() {
        return prstClr;
    }

    /**
     * Sets the value of the prstClr property.
     * 
     * @param value
     *     allowed object is
     *     {@link CTPresetColor }
     *     
     */
    public void setPrstClr(CTPresetColor value) {
        this.prstClr = value;
    }

    /**
     * Gets the value of the extLst property.
     * 
     * @return
     *     possible object is
     *     {@link CTOfficeArtExtensionList }
     *     
     */
    public CTOfficeArtExtensionList getExtLst() {
        return extLst;
    }

    /**
     * Sets the value of the extLst property.
     * 
     * @param value
     *     allowed object is
     *     {@link CTOfficeArtExtensionList }
     *     
     */
    public void setExtLst(CTOfficeArtExtensionList value) {
        this.extLst = value;
    }

    /**
     * Gets the value of the b property.
     * 
     * @return
     *     possible object is
     *     {@link STOnOffStyleType }
     *     
     */
    public STOnOffStyleType getB() {
        if (b == null) {
            return STOnOffStyleType.DEF;
        } else {
            return b;
        }
    }

    /**
     * Sets the value of the b property.
     * 
     * @param value
     *     allowed object is
     *     {@link STOnOffStyleType }
     *     
     */
    public void setB(STOnOffStyleType value) {
        this.b = value;
    }

    /**
     * Gets the value of the i property.
     * 
     * @return
     *     possible object is
     *     {@link STOnOffStyleType }
     *     
     */
    public STOnOffStyleType getI() {
        if (i == null) {
            return STOnOffStyleType.DEF;
        } else {
            return i;
        }
    }

    /**
     * Sets the value of the i property.
     * 
     * @param value
     *     allowed object is
     *     {@link STOnOffStyleType }
     *     
     */
    public void setI(STOnOffStyleType value) {
        this.i = value;
    }

    /**
     * Gets the parent object in the object tree representing the unmarshalled xml document.
     * 
     * @return
     *     The parent object.
     */
    public Object getParent() {
        return this.parent;
    }

    public void setParent(Object parent) {
        this.parent = parent;
    }

    /**
     * This method is invoked by the JAXB implementation on each instance when unmarshalling completes.
     * 
     * @param parent
     *     The parent object in the object tree.
     * @param unmarshaller
     *     The unmarshaller that generated the instance.
     */
    public void afterUnmarshal(Unmarshaller unmarshaller, Object parent) {
        setParent(parent);
    }

}
