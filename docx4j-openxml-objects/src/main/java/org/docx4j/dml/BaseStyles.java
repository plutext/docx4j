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
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlTransient;
import javax.xml.bind.annotation.XmlType;
import org.jvnet.jaxb2_commons.ppp.Child;


/**
 * <p>Java class for CT_BaseStyles complex type.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
 * 
 * <pre>
 * &lt;complexType name="CT_BaseStyles"&gt;
 *   &lt;complexContent&gt;
 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType"&gt;
 *       &lt;sequence&gt;
 *         &lt;element name="clrScheme" type="{http://schemas.openxmlformats.org/drawingml/2006/main}CT_ColorScheme"/&gt;
 *         &lt;element name="fontScheme"&gt;
 *           &lt;complexType&gt;
 *             &lt;complexContent&gt;
 *               &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType"&gt;
 *                 &lt;sequence&gt;
 *                   &lt;element name="majorFont" type="{http://schemas.openxmlformats.org/drawingml/2006/main}CT_FontCollection"/&gt;
 *                   &lt;element name="minorFont" type="{http://schemas.openxmlformats.org/drawingml/2006/main}CT_FontCollection"/&gt;
 *                   &lt;element name="extLst" type="{http://schemas.openxmlformats.org/drawingml/2006/main}CT_OfficeArtExtensionList" minOccurs="0"/&gt;
 *                 &lt;/sequence&gt;
 *                 &lt;attribute name="name" use="required" type="{http://www.w3.org/2001/XMLSchema}string" /&gt;
 *               &lt;/restriction&gt;
 *             &lt;/complexContent&gt;
 *           &lt;/complexType&gt;
 *         &lt;/element&gt;
 *         &lt;element name="fmtScheme" type="{http://schemas.openxmlformats.org/drawingml/2006/main}CT_StyleMatrix"/&gt;
 *         &lt;element name="extLst" type="{http://schemas.openxmlformats.org/drawingml/2006/main}CT_OfficeArtExtensionList" minOccurs="0"/&gt;
 *       &lt;/sequence&gt;
 *     &lt;/restriction&gt;
 *   &lt;/complexContent&gt;
 * &lt;/complexType&gt;
 * </pre>
 * 
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "CT_BaseStyles", propOrder = {
    "clrScheme",
    "fontScheme",
    "fmtScheme",
    "extLst"
})
public class BaseStyles implements Child
{

    @XmlElement(required = true)
    protected CTColorScheme clrScheme;
    @XmlElement(required = true)
    protected BaseStyles.FontScheme fontScheme;
    @XmlElement(required = true)
    protected CTStyleMatrix fmtScheme;
    protected CTOfficeArtExtensionList extLst;
    @XmlTransient
    private Object parent;

    /**
     * Gets the value of the clrScheme property.
     * 
     * @return
     *     possible object is
     *     {@link CTColorScheme }
     *     
     */
    public CTColorScheme getClrScheme() {
        return clrScheme;
    }

    /**
     * Sets the value of the clrScheme property.
     * 
     * @param value
     *     allowed object is
     *     {@link CTColorScheme }
     *     
     */
    public void setClrScheme(CTColorScheme value) {
        this.clrScheme = value;
    }

    /**
     * Gets the value of the fontScheme property.
     * 
     * @return
     *     possible object is
     *     {@link BaseStyles.FontScheme }
     *     
     */
    public BaseStyles.FontScheme getFontScheme() {
        return fontScheme;
    }

    /**
     * Sets the value of the fontScheme property.
     * 
     * @param value
     *     allowed object is
     *     {@link BaseStyles.FontScheme }
     *     
     */
    public void setFontScheme(BaseStyles.FontScheme value) {
        this.fontScheme = value;
    }

    /**
     * Gets the value of the fmtScheme property.
     * 
     * @return
     *     possible object is
     *     {@link CTStyleMatrix }
     *     
     */
    public CTStyleMatrix getFmtScheme() {
        return fmtScheme;
    }

    /**
     * Sets the value of the fmtScheme property.
     * 
     * @param value
     *     allowed object is
     *     {@link CTStyleMatrix }
     *     
     */
    public void setFmtScheme(CTStyleMatrix value) {
        this.fmtScheme = value;
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


    /**
     * <p>Java class for anonymous complex type.
     * 
     * <p>The following schema fragment specifies the expected content contained within this class.
     * 
     * <pre>
     * &lt;complexType&gt;
     *   &lt;complexContent&gt;
     *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType"&gt;
     *       &lt;sequence&gt;
     *         &lt;element name="majorFont" type="{http://schemas.openxmlformats.org/drawingml/2006/main}CT_FontCollection"/&gt;
     *         &lt;element name="minorFont" type="{http://schemas.openxmlformats.org/drawingml/2006/main}CT_FontCollection"/&gt;
     *         &lt;element name="extLst" type="{http://schemas.openxmlformats.org/drawingml/2006/main}CT_OfficeArtExtensionList" minOccurs="0"/&gt;
     *       &lt;/sequence&gt;
     *       &lt;attribute name="name" use="required" type="{http://www.w3.org/2001/XMLSchema}string" /&gt;
     *     &lt;/restriction&gt;
     *   &lt;/complexContent&gt;
     * &lt;/complexType&gt;
     * </pre>
     * 
     * 
     */
    @XmlAccessorType(XmlAccessType.FIELD)
    @XmlType(name = "", propOrder = {
        "majorFont",
        "minorFont",
        "extLst"
    })
    public static class FontScheme implements Child
    {

        @XmlElement(required = true)
        protected FontCollection majorFont;
        @XmlElement(required = true)
        protected FontCollection minorFont;
        protected CTOfficeArtExtensionList extLst;
        @XmlAttribute(name = "name", required = true)
        protected String name;
        @XmlTransient
        private Object parent;

        /**
         * Gets the value of the majorFont property.
         * 
         * @return
         *     possible object is
         *     {@link FontCollection }
         *     
         */
        public FontCollection getMajorFont() {
            return majorFont;
        }

        /**
         * Sets the value of the majorFont property.
         * 
         * @param value
         *     allowed object is
         *     {@link FontCollection }
         *     
         */
        public void setMajorFont(FontCollection value) {
            this.majorFont = value;
        }

        /**
         * Gets the value of the minorFont property.
         * 
         * @return
         *     possible object is
         *     {@link FontCollection }
         *     
         */
        public FontCollection getMinorFont() {
            return minorFont;
        }

        /**
         * Sets the value of the minorFont property.
         * 
         * @param value
         *     allowed object is
         *     {@link FontCollection }
         *     
         */
        public void setMinorFont(FontCollection value) {
            this.minorFont = value;
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
         * Gets the value of the name property.
         * 
         * @return
         *     possible object is
         *     {@link String }
         *     
         */
        public String getName() {
            return name;
        }

        /**
         * Sets the value of the name property.
         * 
         * @param value
         *     allowed object is
         *     {@link String }
         *     
         */
        public void setName(String value) {
            this.name = value;
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

}
