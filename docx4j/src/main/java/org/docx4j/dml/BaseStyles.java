/*
 *  Copyright 2007, Plutext Pty Ltd.
 *   
 *  This file is part of docx4j.

    docx4j is free software: you can use it, redistribute it and/or modify
    it under the terms of version 3 of the GNU Affero General Public License 
    as published by the Free Software Foundation.

    If you need the right to use it under a different license, please
    contact Plutext.

    docx4j is distributed in the hope that it will be useful,
    but WITHOUT ANY WARRANTY; without even the implied warranty of
    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
    GNU Affero General Public License for more details.

    You should have received a copy of the GNU Affero General Public License   
    along with docx4j.  If not, see <http://www.fsf.org/licensing/licenses/>.
    
 */

package org.docx4j.dml;

import java.util.ArrayList;
import java.util.List;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAnyElement;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlType;
import org.w3c.dom.Element;


/**
 * <p>Java class for CT_BaseStyles complex type.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
 * 
 * <pre>
 * &lt;complexType name="CT_BaseStyles">
 *   &lt;complexContent>
 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
 *       &lt;sequence>
 *         &lt;element name="clrScheme">
 *           &lt;complexType>
 *             &lt;complexContent>
 *               &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
 *                 &lt;sequence>
 *                   &lt;any/>
 *                 &lt;/sequence>
 *               &lt;/restriction>
 *             &lt;/complexContent>
 *           &lt;/complexType>
 *         &lt;/element>
 *         &lt;element name="fontScheme">
 *           &lt;complexType>
 *             &lt;complexContent>
 *               &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
 *                 &lt;sequence>
 *                   &lt;element name="majorFont" type="{http://schemas.openxmlformats.org/drawingml/2006/main}CT_FontCollection"/>
 *                   &lt;element name="minorFont" type="{http://schemas.openxmlformats.org/drawingml/2006/main}CT_FontCollection"/>
 *                   &lt;element name="extLst" minOccurs="0">
 *                     &lt;complexType>
 *                       &lt;complexContent>
 *                         &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
 *                           &lt;sequence>
 *                             &lt;any/>
 *                           &lt;/sequence>
 *                         &lt;/restriction>
 *                       &lt;/complexContent>
 *                     &lt;/complexType>
 *                   &lt;/element>
 *                 &lt;/sequence>
 *                 &lt;attribute name="name" use="required" type="{http://www.w3.org/2001/XMLSchema}string" />
 *               &lt;/restriction>
 *             &lt;/complexContent>
 *           &lt;/complexType>
 *         &lt;/element>
 *         &lt;element name="fmtScheme">
 *           &lt;complexType>
 *             &lt;complexContent>
 *               &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
 *                 &lt;sequence>
 *                   &lt;any/>
 *                 &lt;/sequence>
 *               &lt;/restriction>
 *             &lt;/complexContent>
 *           &lt;/complexType>
 *         &lt;/element>
 *         &lt;element name="extLst" minOccurs="0">
 *           &lt;complexType>
 *             &lt;complexContent>
 *               &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
 *                 &lt;sequence>
 *                   &lt;any/>
 *                 &lt;/sequence>
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
@XmlType(name = "CT_BaseStyles", propOrder = {
    "clrScheme",
    "fontScheme",
    "fmtScheme",
    "extLst"
})
public class BaseStyles {

    @XmlElement(required = true)
    protected BaseStyles.ClrScheme clrScheme;
    @XmlElement(required = true)
    protected BaseStyles.FontScheme fontScheme;
    @XmlElement(required = true)
    protected BaseStyles.FmtScheme fmtScheme;
    protected BaseStyles.ExtLst extLst;

    /**
     * Gets the value of the clrScheme property.
     * 
     * @return
     *     possible object is
     *     {@link BaseStyles.ClrScheme }
     *     
     */
    public BaseStyles.ClrScheme getClrScheme() {
        return clrScheme;
    }

    /**
     * Sets the value of the clrScheme property.
     * 
     * @param value
     *     allowed object is
     *     {@link BaseStyles.ClrScheme }
     *     
     */
    public void setClrScheme(BaseStyles.ClrScheme value) {
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
     *     {@link BaseStyles.FmtScheme }
     *     
     */
    public BaseStyles.FmtScheme getFmtScheme() {
        return fmtScheme;
    }

    /**
     * Sets the value of the fmtScheme property.
     * 
     * @param value
     *     allowed object is
     *     {@link BaseStyles.FmtScheme }
     *     
     */
    public void setFmtScheme(BaseStyles.FmtScheme value) {
        this.fmtScheme = value;
    }

    /**
     * Gets the value of the extLst property.
     * 
     * @return
     *     possible object is
     *     {@link BaseStyles.ExtLst }
     *     
     */
    public BaseStyles.ExtLst getExtLst() {
        return extLst;
    }

    /**
     * Sets the value of the extLst property.
     * 
     * @param value
     *     allowed object is
     *     {@link BaseStyles.ExtLst }
     *     
     */
    public void setExtLst(BaseStyles.ExtLst value) {
        this.extLst = value;
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
     *         &lt;any/>
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
        "any"
    })
    public static class ClrScheme {

        @XmlAnyElement
        protected List<Element> any;

        /**
         * Gets the value of the any property.
         * 
         * <p>
         * This accessor method returns a reference to the live list,
         * not a snapshot. Therefore any modification you make to the
         * returned list will be present inside the JAXB object.
         * This is why there is not a <CODE>set</CODE> method for the any property.
         * 
         * <p>
         * For example, to add a new item, do as follows:
         * <pre>
         *    getAny().add(newItem);
         * </pre>
         * 
         * 
         * <p>
         * Objects of the following type(s) are allowed in the list
         * {@link Element }
         * 
         * 
         */
        public List<Element> getAny() {
            if (any == null) {
                any = new ArrayList<Element>();
            }
            return this.any;
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
     *       &lt;sequence>
     *         &lt;any/>
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
        "any"
    })
    public static class ExtLst {

        @XmlAnyElement
        protected List<Element> any;

        /**
         * Gets the value of the any property.
         * 
         * <p>
         * This accessor method returns a reference to the live list,
         * not a snapshot. Therefore any modification you make to the
         * returned list will be present inside the JAXB object.
         * This is why there is not a <CODE>set</CODE> method for the any property.
         * 
         * <p>
         * For example, to add a new item, do as follows:
         * <pre>
         *    getAny().add(newItem);
         * </pre>
         * 
         * 
         * <p>
         * Objects of the following type(s) are allowed in the list
         * {@link Element }
         * 
         * 
         */
        public List<Element> getAny() {
            if (any == null) {
                any = new ArrayList<Element>();
            }
            return this.any;
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
     *       &lt;sequence>
     *         &lt;any/>
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
        "any"
    })
    public static class FmtScheme {

        @XmlAnyElement
        protected List<Element> any;

        /**
         * Gets the value of the any property.
         * 
         * <p>
         * This accessor method returns a reference to the live list,
         * not a snapshot. Therefore any modification you make to the
         * returned list will be present inside the JAXB object.
         * This is why there is not a <CODE>set</CODE> method for the any property.
         * 
         * <p>
         * For example, to add a new item, do as follows:
         * <pre>
         *    getAny().add(newItem);
         * </pre>
         * 
         * 
         * <p>
         * Objects of the following type(s) are allowed in the list
         * {@link Element }
         * 
         * 
         */
        public List<Element> getAny() {
            if (any == null) {
                any = new ArrayList<Element>();
            }
            return this.any;
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
     *       &lt;sequence>
     *         &lt;element name="majorFont" type="{http://schemas.openxmlformats.org/drawingml/2006/main}CT_FontCollection"/>
     *         &lt;element name="minorFont" type="{http://schemas.openxmlformats.org/drawingml/2006/main}CT_FontCollection"/>
     *         &lt;element name="extLst" minOccurs="0">
     *           &lt;complexType>
     *             &lt;complexContent>
     *               &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
     *                 &lt;sequence>
     *                   &lt;any/>
     *                 &lt;/sequence>
     *               &lt;/restriction>
     *             &lt;/complexContent>
     *           &lt;/complexType>
     *         &lt;/element>
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
        "majorFont",
        "minorFont",
        "extLst"
    })
    public static class FontScheme {

        @XmlElement(required = true)
        protected FontCollection majorFont;
        @XmlElement(required = true)
        protected FontCollection minorFont;
        protected BaseStyles.FontScheme.ExtLst extLst;
        @XmlAttribute(required = true)
        protected String name;

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
         *     {@link BaseStyles.FontScheme.ExtLst }
         *     
         */
        public BaseStyles.FontScheme.ExtLst getExtLst() {
            return extLst;
        }

        /**
         * Sets the value of the extLst property.
         * 
         * @param value
         *     allowed object is
         *     {@link BaseStyles.FontScheme.ExtLst }
         *     
         */
        public void setExtLst(BaseStyles.FontScheme.ExtLst value) {
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
         * <p>Java class for anonymous complex type.
         * 
         * <p>The following schema fragment specifies the expected content contained within this class.
         * 
         * <pre>
         * &lt;complexType>
         *   &lt;complexContent>
         *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
         *       &lt;sequence>
         *         &lt;any/>
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
            "any"
        })
        public static class ExtLst {

            @XmlAnyElement
            protected List<Element> any;

            /**
             * Gets the value of the any property.
             * 
             * <p>
             * This accessor method returns a reference to the live list,
             * not a snapshot. Therefore any modification you make to the
             * returned list will be present inside the JAXB object.
             * This is why there is not a <CODE>set</CODE> method for the any property.
             * 
             * <p>
             * For example, to add a new item, do as follows:
             * <pre>
             *    getAny().add(newItem);
             * </pre>
             * 
             * 
             * <p>
             * Objects of the following type(s) are allowed in the list
             * {@link Element }
             * 
             * 
             */
            public List<Element> getAny() {
                if (any == null) {
                    any = new ArrayList<Element>();
                }
                return this.any;
            }

        }

    }

}
