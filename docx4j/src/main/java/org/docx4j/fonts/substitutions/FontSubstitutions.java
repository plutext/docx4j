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
package org.docx4j.fonts.substitutions;

import java.util.ArrayList;
import java.util.List;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
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
 *         &lt;element name="replace" maxOccurs="unbounded">
 *           &lt;complexType>
 *             &lt;complexContent>
 *               &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
 *                 &lt;sequence>
 *                   &lt;element name="SubstFonts" type="{http://www.w3.org/2001/XMLSchema}string"/>
 *                   &lt;element name="SubstFontsMS" type="{http://www.w3.org/2001/XMLSchema}string" minOccurs="0"/>
 *                   &lt;element name="SubstFontsPS" type="{http://www.w3.org/2001/XMLSchema}string" minOccurs="0"/>
 *                   &lt;element name="SubstFontsHTML" type="{http://www.w3.org/2001/XMLSchema}string" minOccurs="0"/>
 *                   &lt;element name="FontWeight" type="{http://www.w3.org/2001/XMLSchema}string" minOccurs="0"/>
 *                   &lt;element name="FontWidth" type="{http://www.w3.org/2001/XMLSchema}string" minOccurs="0"/>
 *                   &lt;element name="FontType" type="{http://www.w3.org/2001/XMLSchema}string" minOccurs="0"/>
 *                 &lt;/sequence>
 *                 &lt;attribute name="name" use="required" type="{http://www.w3.org/2001/XMLSchema}string" />
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
    "replace"
})
@XmlRootElement(name = "FontSubstitutions")
public class FontSubstitutions {

    @XmlElement(required = true)
    protected List<FontSubstitutions.Replace> replace;

    /**
     * Gets the value of the replace property.
     * 
     * <p>
     * This accessor method returns a reference to the live list,
     * not a snapshot. Therefore any modification you make to the
     * returned list will be present inside the JAXB object.
     * This is why there is not a <CODE>set</CODE> method for the replace property.
     * 
     * <p>
     * For example, to add a new item, do as follows:
     * <pre>
     *    getReplace().add(newItem);
     * </pre>
     * 
     * 
     * <p>
     * Objects of the following type(s) are allowed in the list
     * {@link FontSubstitutions.Replace }
     * 
     * 
     */
    public List<FontSubstitutions.Replace> getReplace() {
        if (replace == null) {
            replace = new ArrayList<FontSubstitutions.Replace>();
        }
        return this.replace;
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
     *         &lt;element name="SubstFonts" type="{http://www.w3.org/2001/XMLSchema}string"/>
     *         &lt;element name="SubstFontsMS" type="{http://www.w3.org/2001/XMLSchema}string" minOccurs="0"/>
     *         &lt;element name="SubstFontsPS" type="{http://www.w3.org/2001/XMLSchema}string" minOccurs="0"/>
     *         &lt;element name="SubstFontsHTML" type="{http://www.w3.org/2001/XMLSchema}string" minOccurs="0"/>
     *         &lt;element name="FontWeight" type="{http://www.w3.org/2001/XMLSchema}string" minOccurs="0"/>
     *         &lt;element name="FontWidth" type="{http://www.w3.org/2001/XMLSchema}string" minOccurs="0"/>
     *         &lt;element name="FontType" type="{http://www.w3.org/2001/XMLSchema}string" minOccurs="0"/>
     *       &lt;/sequence>
     *       &lt;attribute name="name" use="required" type="{http://www.w3.org/2001/XMLSchema}string" />
     *     &lt;/restriction>
     *   &lt;/complexContent>
     * &lt;/complexType>
     * </pre>
     * 
     * 
     */
    @XmlAccessorType(XmlAccessType.FIELD)
    @XmlType(name = "", propOrder = {
        "substFonts",
        "substFontsMS",
        "substFontsPS",
        "substFontsHTML",
        "fontWeight",
        "fontWidth",
        "fontType"
    })
    public static class Replace {

        @XmlElement(name = "SubstFonts", required = true)
        protected String substFonts;
        @XmlElement(name = "SubstFontsMS")
        protected String substFontsMS;
        @XmlElement(name = "SubstFontsPS")
        protected String substFontsPS;
        @XmlElement(name = "SubstFontsHTML")
        protected String substFontsHTML;
        @XmlElement(name = "FontWeight")
        protected String fontWeight;
        @XmlElement(name = "FontWidth")
        protected String fontWidth;
        @XmlElement(name = "FontType")
        protected String fontType;
        @XmlAttribute(required = true)
        protected String name;

        /**
         * Gets the value of the substFonts property.
         * 
         * @return
         *     possible object is
         *     {@link String }
         *     
         */
        public String getSubstFonts() {
            return substFonts;
        }

        /**
         * Sets the value of the substFonts property.
         * 
         * @param value
         *     allowed object is
         *     {@link String }
         *     
         */
        public void setSubstFonts(String value) {
            this.substFonts = value;
        }

        /**
         * Gets the value of the substFontsMS property.
         * 
         * @return
         *     possible object is
         *     {@link String }
         *     
         */
        public String getSubstFontsMS() {
            return substFontsMS;
        }

        /**
         * Sets the value of the substFontsMS property.
         * 
         * @param value
         *     allowed object is
         *     {@link String }
         *     
         */
        public void setSubstFontsMS(String value) {
            this.substFontsMS = value;
        }

        /**
         * Gets the value of the substFontsPS property.
         * 
         * @return
         *     possible object is
         *     {@link String }
         *     
         */
        public String getSubstFontsPS() {
            return substFontsPS;
        }

        /**
         * Sets the value of the substFontsPS property.
         * 
         * @param value
         *     allowed object is
         *     {@link String }
         *     
         */
        public void setSubstFontsPS(String value) {
            this.substFontsPS = value;
        }

        /**
         * Gets the value of the substFontsHTML property.
         * 
         * @return
         *     possible object is
         *     {@link String }
         *     
         */
        public String getSubstFontsHTML() {
            return substFontsHTML;
        }

        /**
         * Sets the value of the substFontsHTML property.
         * 
         * @param value
         *     allowed object is
         *     {@link String }
         *     
         */
        public void setSubstFontsHTML(String value) {
            this.substFontsHTML = value;
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
         * Gets the value of the fontWidth property.
         * 
         * @return
         *     possible object is
         *     {@link String }
         *     
         */
        public String getFontWidth() {
            return fontWidth;
        }

        /**
         * Sets the value of the fontWidth property.
         * 
         * @param value
         *     allowed object is
         *     {@link String }
         *     
         */
        public void setFontWidth(String value) {
            this.fontWidth = value;
        }

        /**
         * Gets the value of the fontType property.
         * 
         * @return
         *     possible object is
         *     {@link String }
         *     
         */
        public String getFontType() {
            return fontType;
        }

        /**
         * Sets the value of the fontType property.
         * 
         * @param value
         *     allowed object is
         *     {@link String }
         *     
         */
        public void setFontType(String value) {
            this.fontType = value;
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

    }

}
