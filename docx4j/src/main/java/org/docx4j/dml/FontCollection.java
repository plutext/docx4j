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
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlType;


/**
 * <p>Java class for CT_FontCollection complex type.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
 * 
 * <pre>
 * &lt;complexType name="CT_FontCollection">
 *   &lt;complexContent>
 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
 *       &lt;sequence>
 *         &lt;element name="latin" type="{http://schemas.openxmlformats.org/drawingml/2006/main}CT_TextFont"/>
 *         &lt;element name="ea" type="{http://schemas.openxmlformats.org/drawingml/2006/main}CT_TextFont"/>
 *         &lt;element name="cs" type="{http://schemas.openxmlformats.org/drawingml/2006/main}CT_TextFont"/>
 *         &lt;element name="font" maxOccurs="unbounded" minOccurs="0">
 *           &lt;complexType>
 *             &lt;complexContent>
 *               &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
 *                 &lt;attribute name="script" use="required" type="{http://www.w3.org/2001/XMLSchema}string" />
 *                 &lt;attribute name="typeface" use="required">
 *                   &lt;simpleType>
 *                     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}string">
 *                     &lt;/restriction>
 *                   &lt;/simpleType>
 *                 &lt;/attribute>
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
@XmlType(name = "CT_FontCollection", propOrder = {
    "latin",
    "ea",
    "cs",
    "font"
})
public class FontCollection {

    @XmlElement(required = true)
    protected TextFont latin;
    @XmlElement(required = true)
    protected TextFont ea;
    @XmlElement(required = true)
    protected TextFont cs;
    protected List<FontCollection.Font> font;

    /**
     * Gets the value of the latin property.
     * 
     * @return
     *     possible object is
     *     {@link TextFont }
     *     
     */
    public TextFont getLatin() {
        return latin;
    }

    /**
     * Sets the value of the latin property.
     * 
     * @param value
     *     allowed object is
     *     {@link TextFont }
     *     
     */
    public void setLatin(TextFont value) {
        this.latin = value;
    }

    /**
     * Gets the value of the ea property.
     * 
     * @return
     *     possible object is
     *     {@link TextFont }
     *     
     */
    public TextFont getEa() {
        return ea;
    }

    /**
     * Sets the value of the ea property.
     * 
     * @param value
     *     allowed object is
     *     {@link TextFont }
     *     
     */
    public void setEa(TextFont value) {
        this.ea = value;
    }

    /**
     * Gets the value of the cs property.
     * 
     * @return
     *     possible object is
     *     {@link TextFont }
     *     
     */
    public TextFont getCs() {
        return cs;
    }

    /**
     * Sets the value of the cs property.
     * 
     * @param value
     *     allowed object is
     *     {@link TextFont }
     *     
     */
    public void setCs(TextFont value) {
        this.cs = value;
    }

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
     * {@link FontCollection.Font }
     * 
     * 
     */
    public List<FontCollection.Font> getFont() {
        if (font == null) {
            font = new ArrayList<FontCollection.Font>();
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
     *       &lt;attribute name="script" use="required" type="{http://www.w3.org/2001/XMLSchema}string" />
     *       &lt;attribute name="typeface" use="required">
     *         &lt;simpleType>
     *           &lt;restriction base="{http://www.w3.org/2001/XMLSchema}string">
     *           &lt;/restriction>
     *         &lt;/simpleType>
     *       &lt;/attribute>
     *     &lt;/restriction>
     *   &lt;/complexContent>
     * &lt;/complexType>
     * </pre>
     * 
     * 
     */
    @XmlAccessorType(XmlAccessType.FIELD)
    @XmlType(name = "")
    public static class Font {

        @XmlAttribute(required = true)
        protected String script;
        @XmlAttribute(required = true)
        protected String typeface;

        /**
         * Gets the value of the script property.
         * 
         * @return
         *     possible object is
         *     {@link String }
         *     
         */
        public String getScript() {
            return script;
        }

        /**
         * Sets the value of the script property.
         * 
         * @param value
         *     allowed object is
         *     {@link String }
         *     
         */
        public void setScript(String value) {
            this.script = value;
        }

        /**
         * Gets the value of the typeface property.
         * 
         * @return
         *     possible object is
         *     {@link String }
         *     
         */
        public String getTypeface() {
            return typeface;
        }

        /**
         * Sets the value of the typeface property.
         * 
         * @param value
         *     allowed object is
         *     {@link String }
         *     
         */
        public void setTypeface(String value) {
            this.typeface = value;
        }

    }

}
