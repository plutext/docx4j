/*
 *  Copyright 2007-2013, Plutext Pty Ltd.
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


package org.docx4j.wml; 

import java.util.ArrayList;
import java.util.List;

import javax.xml.bind.Unmarshaller;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlTransient;
import javax.xml.bind.annotation.XmlType;

import org.jvnet.jaxb2_commons.ppp.Child;


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
 *         &lt;element name="font" maxOccurs="unbounded" minOccurs="0">
 *           &lt;complexType>
 *             &lt;complexContent>
 *               &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
 *                 &lt;sequence>
 *                   &lt;element name="altName" minOccurs="0">
 *                     &lt;complexType>
 *                       &lt;complexContent>
 *                         &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
 *                           &lt;attribute name="val" type="{http://www.w3.org/2001/XMLSchema}string" />
 *                         &lt;/restriction>
 *                       &lt;/complexContent>
 *                     &lt;/complexType>
 *                   &lt;/element>
 *                   &lt;element name="panose1" type="{http://schemas.openxmlformats.org/wordprocessingml/2006/main}CT_Panose" minOccurs="0"/>
 *                   &lt;element name="charset" type="{http://schemas.openxmlformats.org/wordprocessingml/2006/main}CT_UcharHexNumber" minOccurs="0"/>
 *                   &lt;element name="family" type="{http://schemas.openxmlformats.org/wordprocessingml/2006/main}CT_FontFamily" minOccurs="0"/>
 *                   &lt;element name="notTrueType" type="{http://schemas.openxmlformats.org/wordprocessingml/2006/main}BooleanDefaultTrue" minOccurs="0"/>
 *                   &lt;element name="pitch" type="{http://schemas.openxmlformats.org/wordprocessingml/2006/main}CT_Pitch" minOccurs="0"/>
 *                   &lt;element name="sig" type="{http://schemas.openxmlformats.org/wordprocessingml/2006/main}CT_FontSig" minOccurs="0"/>
 *                   &lt;element name="embedRegular" type="{http://schemas.openxmlformats.org/wordprocessingml/2006/main}CT_FontRel" minOccurs="0"/>
 *                   &lt;element name="embedBold" type="{http://schemas.openxmlformats.org/wordprocessingml/2006/main}CT_FontRel" minOccurs="0"/>
 *                   &lt;element name="embedItalic" type="{http://schemas.openxmlformats.org/wordprocessingml/2006/main}CT_FontRel" minOccurs="0"/>
 *                   &lt;element name="embedBoldItalic" type="{http://schemas.openxmlformats.org/wordprocessingml/2006/main}CT_FontRel" minOccurs="0"/>
 *                 &lt;/sequence>
 *                 &lt;attribute name="name" use="required" type="{http://schemas.openxmlformats.org/wordprocessingml/2006/main}ST_String" />
 *               &lt;/restriction>
 *             &lt;/complexContent>
 *           &lt;/complexType>
 *         &lt;/element>
 *       &lt;/sequence>
 *     &lt;/restriction>
 *   &lt;/complexContent>
 * &lt;/complexType>
 * </pre>
 * 
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "", propOrder = {
    "font"
})
@XmlRootElement(name = "fonts")
public class Fonts implements Child
{

    protected List<Fonts.Font> font = new ArrayListWml<Fonts.Font>(this);
    @XmlTransient
    private Object parent;

    /**
     * Gets the value of the font property.
     * 
     * <p>
     * This accessor method returns a reference to the live list,
     * not a snapshot. Therefore any modification you make to the
     * returned list will be present inside the JAXB object.
     * This is why there is not a <CODE>set</CODE> method for the font property.
     * 
     * <p>
     * For example, to add a new item, do as follows:
     * <pre>
     *    getFont().add(newItem);
     * </pre>
     * 
     * 
     * <p>
     * Objects of the following type(s) are allowed in the list
     * {@link Fonts.Font }
     * 
     * 
     */
    public List<Fonts.Font> getFont() {
        if (font == null) {
            font = new ArrayListWml<Fonts.Font>(this);
        }
        return this.font;
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
     * &lt;complexType>
     *   &lt;complexContent>
     *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
     *       &lt;sequence>
     *         &lt;element name="altName" minOccurs="0">
     *           &lt;complexType>
     *             &lt;complexContent>
     *               &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
     *                 &lt;attribute name="val" type="{http://www.w3.org/2001/XMLSchema}string" />
     *               &lt;/restriction>
     *             &lt;/complexContent>
     *           &lt;/complexType>
     *         &lt;/element>
     *         &lt;element name="panose1" type="{http://schemas.openxmlformats.org/wordprocessingml/2006/main}CT_Panose" minOccurs="0"/>
     *         &lt;element name="charset" type="{http://schemas.openxmlformats.org/wordprocessingml/2006/main}CT_UcharHexNumber" minOccurs="0"/>
     *         &lt;element name="family" type="{http://schemas.openxmlformats.org/wordprocessingml/2006/main}CT_FontFamily" minOccurs="0"/>
     *         &lt;element name="notTrueType" type="{http://schemas.openxmlformats.org/wordprocessingml/2006/main}BooleanDefaultTrue" minOccurs="0"/>
     *         &lt;element name="pitch" type="{http://schemas.openxmlformats.org/wordprocessingml/2006/main}CT_Pitch" minOccurs="0"/>
     *         &lt;element name="sig" type="{http://schemas.openxmlformats.org/wordprocessingml/2006/main}CT_FontSig" minOccurs="0"/>
     *         &lt;element name="embedRegular" type="{http://schemas.openxmlformats.org/wordprocessingml/2006/main}CT_FontRel" minOccurs="0"/>
     *         &lt;element name="embedBold" type="{http://schemas.openxmlformats.org/wordprocessingml/2006/main}CT_FontRel" minOccurs="0"/>
     *         &lt;element name="embedItalic" type="{http://schemas.openxmlformats.org/wordprocessingml/2006/main}CT_FontRel" minOccurs="0"/>
     *         &lt;element name="embedBoldItalic" type="{http://schemas.openxmlformats.org/wordprocessingml/2006/main}CT_FontRel" minOccurs="0"/>
     *       &lt;/sequence>
     *       &lt;attribute name="name" use="required" type="{http://schemas.openxmlformats.org/wordprocessingml/2006/main}ST_String" />
     *     &lt;/restriction>
     *   &lt;/complexContent>
     * &lt;/complexType>
     * </pre>
     * 
     * 
     */
    @XmlAccessorType(XmlAccessType.FIELD)
    @XmlType(name = "", propOrder = {
        "altName",
        "panose1",
        "charset",
        "family",
        "notTrueType",
        "pitch",
        "sig",
        "embedRegular",
        "embedBold",
        "embedItalic",
        "embedBoldItalic"
    })
    public static class Font implements Child
    {

        protected Fonts.Font.AltName altName;
        protected FontPanose panose1;
        protected CTUcharHexNumber charset;
        protected FontFamily family;
        protected BooleanDefaultTrue notTrueType;
        protected FontPitch pitch;
        protected FontSig sig;
        protected FontRel embedRegular;
        protected FontRel embedBold;
        protected FontRel embedItalic;
        protected FontRel embedBoldItalic;
        @XmlAttribute(name = "name", namespace = "http://schemas.openxmlformats.org/wordprocessingml/2006/main", required = true)
        protected String name;
        @XmlTransient
        private Object parent;

        /**
         * Gets the value of the altName property.
         * 
         * @return
         *     possible object is
         *     {@link Fonts.Font.AltName }
         *     
         */
        public Fonts.Font.AltName getAltName() {
            return altName;
        }

        /**
         * Sets the value of the altName property.
         * 
         * @param value
         *     allowed object is
         *     {@link Fonts.Font.AltName }
         *     
         */
        public void setAltName(Fonts.Font.AltName value) {
            this.altName = value;
        }

        /**
         * Gets the value of the panose1 property.
         * 
         * @return
         *     possible object is
         *     {@link FontPanose }
         *     
         */
        public FontPanose getPanose1() {
            return panose1;
        }

        /**
         * Sets the value of the panose1 property.
         * 
         * @param value
         *     allowed object is
         *     {@link FontPanose }
         *     
         */
        public void setPanose1(FontPanose value) {
            this.panose1 = value;
        }

        /**
         * Gets the value of the charset property.
         * 
         * @return
         *     possible object is
         *     {@link CTUcharHexNumber }
         *     
         */
        public CTUcharHexNumber getCharset() {
            return charset;
        }

        /**
         * Sets the value of the charset property.
         * 
         * @param value
         *     allowed object is
         *     {@link CTUcharHexNumber }
         *     
         */
        public void setCharset(CTUcharHexNumber value) {
            this.charset = value;
        }

        /**
         * Gets the value of the family property.
         * 
         * @return
         *     possible object is
         *     {@link FontFamily }
         *     
         */
        public FontFamily getFamily() {
            return family;
        }

        /**
         * Sets the value of the family property.
         * 
         * @param value
         *     allowed object is
         *     {@link FontFamily }
         *     
         */
        public void setFamily(FontFamily value) {
            this.family = value;
        }

        /**
         * Gets the value of the notTrueType property.
         * 
         * @return
         *     possible object is
         *     {@link BooleanDefaultTrue }
         *     
         */
        public BooleanDefaultTrue getNotTrueType() {
            return notTrueType;
        }

        /**
         * Sets the value of the notTrueType property.
         * 
         * @param value
         *     allowed object is
         *     {@link BooleanDefaultTrue }
         *     
         */
        public void setNotTrueType(BooleanDefaultTrue value) {
            this.notTrueType = value;
        }

        /**
         * Gets the value of the pitch property.
         * 
         * @return
         *     possible object is
         *     {@link FontPitch }
         *     
         */
        public FontPitch getPitch() {
            return pitch;
        }

        /**
         * Sets the value of the pitch property.
         * 
         * @param value
         *     allowed object is
         *     {@link FontPitch }
         *     
         */
        public void setPitch(FontPitch value) {
            this.pitch = value;
        }

        /**
         * Gets the value of the sig property.
         * 
         * @return
         *     possible object is
         *     {@link FontSig }
         *     
         */
        public FontSig getSig() {
            return sig;
        }

        /**
         * Sets the value of the sig property.
         * 
         * @param value
         *     allowed object is
         *     {@link FontSig }
         *     
         */
        public void setSig(FontSig value) {
            this.sig = value;
        }

        /**
         * Gets the value of the embedRegular property.
         * 
         * @return
         *     possible object is
         *     {@link FontRel }
         *     
         */
        public FontRel getEmbedRegular() {
            return embedRegular;
        }

        /**
         * Sets the value of the embedRegular property.
         * 
         * @param value
         *     allowed object is
         *     {@link FontRel }
         *     
         */
        public void setEmbedRegular(FontRel value) {
            this.embedRegular = value;
        }

        /**
         * Gets the value of the embedBold property.
         * 
         * @return
         *     possible object is
         *     {@link FontRel }
         *     
         */
        public FontRel getEmbedBold() {
            return embedBold;
        }

        /**
         * Sets the value of the embedBold property.
         * 
         * @param value
         *     allowed object is
         *     {@link FontRel }
         *     
         */
        public void setEmbedBold(FontRel value) {
            this.embedBold = value;
        }

        /**
         * Gets the value of the embedItalic property.
         * 
         * @return
         *     possible object is
         *     {@link FontRel }
         *     
         */
        public FontRel getEmbedItalic() {
            return embedItalic;
        }

        /**
         * Sets the value of the embedItalic property.
         * 
         * @param value
         *     allowed object is
         *     {@link FontRel }
         *     
         */
        public void setEmbedItalic(FontRel value) {
            this.embedItalic = value;
        }

        /**
         * Gets the value of the embedBoldItalic property.
         * 
         * @return
         *     possible object is
         *     {@link FontRel }
         *     
         */
        public FontRel getEmbedBoldItalic() {
            return embedBoldItalic;
        }

        /**
         * Sets the value of the embedBoldItalic property.
         * 
         * @param value
         *     allowed object is
         *     {@link FontRel }
         *     
         */
        public void setEmbedBoldItalic(FontRel value) {
            this.embedBoldItalic = value;
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


        /**
         * <p>Java class for anonymous complex type.
         * 
         * <p>The following schema fragment specifies the expected content contained within this class.
         * 
         * <pre>
         * &lt;complexType>
         *   &lt;complexContent>
         *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
         *       &lt;attribute name="val" type="{http://www.w3.org/2001/XMLSchema}string" />
         *     &lt;/restriction>
         *   &lt;/complexContent>
         * &lt;/complexType>
         * </pre>
         * 
         * 
         */
        @XmlAccessorType(XmlAccessType.FIELD)
        @XmlType(name = "")
        public static class AltName implements Child
        {

            @XmlAttribute(name = "val", namespace = "http://schemas.openxmlformats.org/wordprocessingml/2006/main")
            protected String val;
            @XmlTransient
            private Object parent;

            /**
             * Gets the value of the val property.
             * 
             * @return
             *     possible object is
             *     {@link String }
             *     
             */
            public String getVal() {
                return val;
            }

            /**
             * Sets the value of the val property.
             * 
             * @param value
             *     allowed object is
             *     {@link String }
             *     
             */
            public void setVal(String value) {
                this.val = value;
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

}
