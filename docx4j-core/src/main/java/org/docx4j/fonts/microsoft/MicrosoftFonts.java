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

package org.docx4j.fonts.microsoft;

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
 *         &lt;element name="font" maxOccurs="unbounded">
 *           &lt;complexType>
 *             &lt;complexContent>
 *               &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
 *                 &lt;sequence minOccurs="0">
 *                   &lt;element name="bold" minOccurs="0">
 *                     &lt;complexType>
 *                       &lt;complexContent>
 *                         &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
 *                           &lt;attribute name="filename" use="required" type="{http://www.w3.org/2001/XMLSchema}string" />
 *                           &lt;attribute name="mac" type="{http://www.w3.org/2001/XMLSchema}string" />
 *                         &lt;/restriction>
 *                       &lt;/complexContent>
 *                     &lt;/complexType>
 *                   &lt;/element>
 *                   &lt;element name="italic" minOccurs="0">
 *                     &lt;complexType>
 *                       &lt;complexContent>
 *                         &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
 *                           &lt;attribute name="filename" use="required" type="{http://www.w3.org/2001/XMLSchema}string" />
 *                           &lt;attribute name="mac" type="{http://www.w3.org/2001/XMLSchema}string" />
 *                         &lt;/restriction>
 *                       &lt;/complexContent>
 *                     &lt;/complexType>
 *                   &lt;/element>
 *                   &lt;element name="bolditalic" minOccurs="0">
 *                     &lt;complexType>
 *                       &lt;complexContent>
 *                         &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
 *                           &lt;attribute name="filename" use="required" type="{http://www.w3.org/2001/XMLSchema}string" />
 *                           &lt;attribute name="mac" type="{http://www.w3.org/2001/XMLSchema}string" />
 *                         &lt;/restriction>
 *                       &lt;/complexContent>
 *                     &lt;/complexType>
 *                   &lt;/element>
 *                 &lt;/sequence>
 *                 &lt;attribute name="name" use="required" type="{http://www.w3.org/2001/XMLSchema}string" />
 *                 &lt;attribute name="filename" use="required" type="{http://www.w3.org/2001/XMLSchema}string" />
 *                 &lt;attribute name="mac" type="{http://www.w3.org/2001/XMLSchema}string" />
 *                 &lt;attribute name="coreWebFont" type="{http://www.w3.org/2001/XMLSchema}boolean" />
 *                 &lt;attribute name="clearTypeCollection" type="{http://www.w3.org/2001/XMLSchema}boolean" />
 *                 &lt;attribute name="secondary" type="{http://www.w3.org/2001/XMLSchema}boolean" />
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
@XmlRootElement(name = "MicrosoftFonts")
public class MicrosoftFonts {

    @XmlElement(required = true)
    protected List<MicrosoftFonts.Font> font;

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
     * {@link MicrosoftFonts.Font }
     * 
     * 
     */
    public List<MicrosoftFonts.Font> getFont() {
        if (font == null) {
            font = new ArrayList<MicrosoftFonts.Font>();
        }
        return this.font;
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
     *       &lt;sequence minOccurs="0">
     *         &lt;element name="bold" minOccurs="0">
     *           &lt;complexType>
     *             &lt;complexContent>
     *               &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
     *                 &lt;attribute name="filename" use="required" type="{http://www.w3.org/2001/XMLSchema}string" />
     *                 &lt;attribute name="mac" type="{http://www.w3.org/2001/XMLSchema}string" />
     *               &lt;/restriction>
     *             &lt;/complexContent>
     *           &lt;/complexType>
     *         &lt;/element>
     *         &lt;element name="italic" minOccurs="0">
     *           &lt;complexType>
     *             &lt;complexContent>
     *               &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
     *                 &lt;attribute name="filename" use="required" type="{http://www.w3.org/2001/XMLSchema}string" />
     *                 &lt;attribute name="mac" type="{http://www.w3.org/2001/XMLSchema}string" />
     *               &lt;/restriction>
     *             &lt;/complexContent>
     *           &lt;/complexType>
     *         &lt;/element>
     *         &lt;element name="bolditalic" minOccurs="0">
     *           &lt;complexType>
     *             &lt;complexContent>
     *               &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
     *                 &lt;attribute name="filename" use="required" type="{http://www.w3.org/2001/XMLSchema}string" />
     *                 &lt;attribute name="mac" type="{http://www.w3.org/2001/XMLSchema}string" />
     *               &lt;/restriction>
     *             &lt;/complexContent>
     *           &lt;/complexType>
     *         &lt;/element>
     *       &lt;/sequence>
     *       &lt;attribute name="name" use="required" type="{http://www.w3.org/2001/XMLSchema}string" />
     *       &lt;attribute name="filename" use="required" type="{http://www.w3.org/2001/XMLSchema}string" />
     *       &lt;attribute name="mac" type="{http://www.w3.org/2001/XMLSchema}string" />
     *       &lt;attribute name="coreWebFont" type="{http://www.w3.org/2001/XMLSchema}boolean" />
     *       &lt;attribute name="clearTypeCollection" type="{http://www.w3.org/2001/XMLSchema}boolean" />
     *       &lt;attribute name="secondary" type="{http://www.w3.org/2001/XMLSchema}boolean" />
     *     &lt;/restriction>
     *   &lt;/complexContent>
     * &lt;/complexType>
     * </pre>
     * 
     * 
     */
    @XmlAccessorType(XmlAccessType.FIELD)
    @XmlType(name = "", propOrder = {
        "bold",
        "italic",
        "bolditalic"
    })
    public static class Font {

        protected MicrosoftFonts.Font.Bold bold;
        protected MicrosoftFonts.Font.Italic italic;
        protected MicrosoftFonts.Font.Bolditalic bolditalic;
        @XmlAttribute(name = "name", required = true)
        protected String name;
        @XmlAttribute(name = "filename", required = true)
        protected String filename;
        @XmlAttribute(name = "mac")
        protected String mac;
        @XmlAttribute(name = "coreWebFont")
        protected Boolean coreWebFont;
        @XmlAttribute(name = "clearTypeCollection")
        protected Boolean clearTypeCollection;
        @XmlAttribute(name = "secondary")
        protected Boolean secondary;

        /**
         * Gets the value of the bold property.
         * 
         * @return
         *     possible object is
         *     {@link MicrosoftFonts.Font.Bold }
         *     
         */
        public MicrosoftFonts.Font.Bold getBold() {
            return bold;
        }

        /**
         * Sets the value of the bold property.
         * 
         * @param value
         *     allowed object is
         *     {@link MicrosoftFonts.Font.Bold }
         *     
         */
        public void setBold(MicrosoftFonts.Font.Bold value) {
            this.bold = value;
        }

        /**
         * Gets the value of the italic property.
         * 
         * @return
         *     possible object is
         *     {@link MicrosoftFonts.Font.Italic }
         *     
         */
        public MicrosoftFonts.Font.Italic getItalic() {
            return italic;
        }

        /**
         * Sets the value of the italic property.
         * 
         * @param value
         *     allowed object is
         *     {@link MicrosoftFonts.Font.Italic }
         *     
         */
        public void setItalic(MicrosoftFonts.Font.Italic value) {
            this.italic = value;
        }

        /**
         * Gets the value of the bolditalic property.
         * 
         * @return
         *     possible object is
         *     {@link MicrosoftFonts.Font.Bolditalic }
         *     
         */
        public MicrosoftFonts.Font.Bolditalic getBolditalic() {
            return bolditalic;
        }

        /**
         * Sets the value of the bolditalic property.
         * 
         * @param value
         *     allowed object is
         *     {@link MicrosoftFonts.Font.Bolditalic }
         *     
         */
        public void setBolditalic(MicrosoftFonts.Font.Bolditalic value) {
            this.bolditalic = value;
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
         * Gets the value of the filename property.
         * 
         * @return
         *     possible object is
         *     {@link String }
         *     
         */
        public String getFilename() {
            return filename;
        }

        /**
         * Sets the value of the filename property.
         * 
         * @param value
         *     allowed object is
         *     {@link String }
         *     
         */
        public void setFilename(String value) {
            this.filename = value;
        }

        /**
         * Gets the value of the mac property.
         * 
         * @return
         *     possible object is
         *     {@link String }
         *     
         */
        public String getMac() {
            return mac;
        }

        /**
         * Sets the value of the mac property.
         * 
         * @param value
         *     allowed object is
         *     {@link String }
         *     
         */
        public void setMac(String value) {
            this.mac = value;
        }

        /**
         * Gets the value of the coreWebFont property.
         * 
         * @return
         *     possible object is
         *     {@link Boolean }
         *     
         */
        public Boolean isCoreWebFont() {
            return coreWebFont;
        }

        /**
         * Sets the value of the coreWebFont property.
         * 
         * @param value
         *     allowed object is
         *     {@link Boolean }
         *     
         */
        public void setCoreWebFont(Boolean value) {
            this.coreWebFont = value;
        }

        /**
         * Gets the value of the clearTypeCollection property.
         * 
         * @return
         *     possible object is
         *     {@link Boolean }
         *     
         */
        public Boolean isClearTypeCollection() {
            return clearTypeCollection;
        }

        /**
         * Sets the value of the clearTypeCollection property.
         * 
         * @param value
         *     allowed object is
         *     {@link Boolean }
         *     
         */
        public void setClearTypeCollection(Boolean value) {
            this.clearTypeCollection = value;
        }

        /**
         * Gets the value of the secondary property.
         * 
         * @return
         *     possible object is
         *     {@link Boolean }
         *     
         */
        public Boolean isSecondary() {
            return secondary;
        }

        /**
         * Sets the value of the secondary property.
         * 
         * @param value
         *     allowed object is
         *     {@link Boolean }
         *     
         */
        public void setSecondary(Boolean value) {
            this.secondary = value;
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
         *       &lt;attribute name="filename" use="required" type="{http://www.w3.org/2001/XMLSchema}string" />
         *       &lt;attribute name="mac" type="{http://www.w3.org/2001/XMLSchema}string" />
         *     &lt;/restriction>
         *   &lt;/complexContent>
         * &lt;/complexType>
         * </pre>
         * 
         * 
         */
        @XmlAccessorType(XmlAccessType.FIELD)
        @XmlType(name = "")
        public static class Bold {

            @XmlAttribute(name = "filename", required = true)
            protected String filename;
            @XmlAttribute(name = "mac")
            protected String mac;

            /**
             * Gets the value of the filename property.
             * 
             * @return
             *     possible object is
             *     {@link String }
             *     
             */
            public String getFilename() {
                return filename;
            }

            /**
             * Sets the value of the filename property.
             * 
             * @param value
             *     allowed object is
             *     {@link String }
             *     
             */
            public void setFilename(String value) {
                this.filename = value;
            }

            /**
             * Gets the value of the mac property.
             * 
             * @return
             *     possible object is
             *     {@link String }
             *     
             */
            public String getMac() {
                return mac;
            }

            /**
             * Sets the value of the mac property.
             * 
             * @param value
             *     allowed object is
             *     {@link String }
             *     
             */
            public void setMac(String value) {
                this.mac = value;
            }

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
         *       &lt;attribute name="filename" use="required" type="{http://www.w3.org/2001/XMLSchema}string" />
         *       &lt;attribute name="mac" type="{http://www.w3.org/2001/XMLSchema}string" />
         *     &lt;/restriction>
         *   &lt;/complexContent>
         * &lt;/complexType>
         * </pre>
         * 
         * 
         */
        @XmlAccessorType(XmlAccessType.FIELD)
        @XmlType(name = "")
        public static class Bolditalic {

            @XmlAttribute(name = "filename", required = true)
            protected String filename;
            @XmlAttribute(name = "mac")
            protected String mac;

            /**
             * Gets the value of the filename property.
             * 
             * @return
             *     possible object is
             *     {@link String }
             *     
             */
            public String getFilename() {
                return filename;
            }

            /**
             * Sets the value of the filename property.
             * 
             * @param value
             *     allowed object is
             *     {@link String }
             *     
             */
            public void setFilename(String value) {
                this.filename = value;
            }

            /**
             * Gets the value of the mac property.
             * 
             * @return
             *     possible object is
             *     {@link String }
             *     
             */
            public String getMac() {
                return mac;
            }

            /**
             * Sets the value of the mac property.
             * 
             * @param value
             *     allowed object is
             *     {@link String }
             *     
             */
            public void setMac(String value) {
                this.mac = value;
            }

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
         *       &lt;attribute name="filename" use="required" type="{http://www.w3.org/2001/XMLSchema}string" />
         *       &lt;attribute name="mac" type="{http://www.w3.org/2001/XMLSchema}string" />
         *     &lt;/restriction>
         *   &lt;/complexContent>
         * &lt;/complexType>
         * </pre>
         * 
         * 
         */
        @XmlAccessorType(XmlAccessType.FIELD)
        @XmlType(name = "")
        public static class Italic {

            @XmlAttribute(name = "filename", required = true)
            protected String filename;
            @XmlAttribute(name = "mac")
            protected String mac;

            /**
             * Gets the value of the filename property.
             * 
             * @return
             *     possible object is
             *     {@link String }
             *     
             */
            public String getFilename() {
                return filename;
            }

            /**
             * Sets the value of the filename property.
             * 
             * @param value
             *     allowed object is
             *     {@link String }
             *     
             */
            public void setFilename(String value) {
                this.filename = value;
            }

            /**
             * Gets the value of the mac property.
             * 
             * @return
             *     possible object is
             *     {@link String }
             *     
             */
            public String getMac() {
                return mac;
            }

            /**
             * Sets the value of the mac property.
             * 
             * @param value
             *     allowed object is
             *     {@link String }
             *     
             */
            public void setMac(String value) {
                this.mac = value;
            }

        }

    }

}
