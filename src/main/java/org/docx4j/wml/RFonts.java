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
 *       &lt;attribute name="hint" type="{http://schemas.openxmlformats.org/wordprocessingml/2006/main}ST_Hint" />
 *       &lt;attribute name="ascii" type="{http://schemas.openxmlformats.org/wordprocessingml/2006/main}ST_String" />
 *       &lt;attribute name="hAnsi" type="{http://schemas.openxmlformats.org/wordprocessingml/2006/main}ST_String" />
 *       &lt;attribute name="eastAsia" type="{http://schemas.openxmlformats.org/wordprocessingml/2006/main}ST_String" />
 *       &lt;attribute name="cs" type="{http://schemas.openxmlformats.org/wordprocessingml/2006/main}ST_String" />
 *       &lt;attribute name="asciiTheme" type="{http://schemas.openxmlformats.org/wordprocessingml/2006/main}ST_Theme" />
 *       &lt;attribute name="hAnsiTheme" type="{http://schemas.openxmlformats.org/wordprocessingml/2006/main}ST_Theme" />
 *       &lt;attribute name="eastAsiaTheme" type="{http://schemas.openxmlformats.org/wordprocessingml/2006/main}ST_Theme" />
 *       &lt;attribute name="cstheme" type="{http://schemas.openxmlformats.org/wordprocessingml/2006/main}ST_Theme" />
 *     &lt;/restriction>
 *   &lt;/complexContent>
 * &lt;/complexType>
 * </pre>
 * 
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "")
@XmlRootElement(name = "rFonts")
public class RFonts implements Child
{

    @XmlAttribute(name = "hint", namespace = "http://schemas.openxmlformats.org/wordprocessingml/2006/main")
    protected STHint hint;
    @XmlAttribute(name = "ascii", namespace = "http://schemas.openxmlformats.org/wordprocessingml/2006/main")
    protected String ascii;
    @XmlAttribute(name = "hAnsi", namespace = "http://schemas.openxmlformats.org/wordprocessingml/2006/main")
    protected String hAnsi;
    @XmlAttribute(name = "eastAsia", namespace = "http://schemas.openxmlformats.org/wordprocessingml/2006/main")
    protected String eastAsia;
    @XmlAttribute(name = "cs", namespace = "http://schemas.openxmlformats.org/wordprocessingml/2006/main")
    protected String cs;
    @XmlAttribute(name = "asciiTheme", namespace = "http://schemas.openxmlformats.org/wordprocessingml/2006/main")
    protected STTheme asciiTheme;
    @XmlAttribute(name = "hAnsiTheme", namespace = "http://schemas.openxmlformats.org/wordprocessingml/2006/main")
    protected STTheme hAnsiTheme;
    @XmlAttribute(name = "eastAsiaTheme", namespace = "http://schemas.openxmlformats.org/wordprocessingml/2006/main")
    protected STTheme eastAsiaTheme;
    @XmlAttribute(name = "cstheme", namespace = "http://schemas.openxmlformats.org/wordprocessingml/2006/main")
    protected STTheme cstheme;
    @XmlTransient
    private Object parent;

    /**
     * Gets the value of the hint property.
     * 
     * @return
     *     possible object is
     *     {@link STHint }
     *     
     */
    public STHint getHint() {
        return hint;
    }

    /**
     * Sets the value of the hint property.
     * 
     * @param value
     *     allowed object is
     *     {@link STHint }
     *     
     */
    public void setHint(STHint value) {
        this.hint = value;
    }

    /**
     * Gets the value of the ascii property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getAscii() {
        return ascii;
    }

    /**
     * Sets the value of the ascii property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setAscii(String value) {
        this.ascii = value;
    }

    /**
     * Gets the value of the hAnsi property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getHAnsi() {
        return hAnsi;
    }

    /**
     * Sets the value of the hAnsi property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setHAnsi(String value) {
        this.hAnsi = value;
    }

    /**
     * Gets the value of the eastAsia property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getEastAsia() {
        return eastAsia;
    }

    /**
     * Sets the value of the eastAsia property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setEastAsia(String value) {
        this.eastAsia = value;
    }

    /**
     * Gets the value of the cs property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getCs() {
        return cs;
    }

    /**
     * Sets the value of the cs property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setCs(String value) {
        this.cs = value;
    }

    /**
     * Gets the value of the asciiTheme property.
     * 
     * @return
     *     possible object is
     *     {@link STTheme }
     *     
     */
    public STTheme getAsciiTheme() {
        return asciiTheme;
    }

    /**
     * Sets the value of the asciiTheme property.
     * 
     * @param value
     *     allowed object is
     *     {@link STTheme }
     *     
     */
    public void setAsciiTheme(STTheme value) {
        this.asciiTheme = value;
    }

    /**
     * Gets the value of the hAnsiTheme property.
     * 
     * @return
     *     possible object is
     *     {@link STTheme }
     *     
     */
    public STTheme getHAnsiTheme() {
        return hAnsiTheme;
    }

    /**
     * Sets the value of the hAnsiTheme property.
     * 
     * @param value
     *     allowed object is
     *     {@link STTheme }
     *     
     */
    public void setHAnsiTheme(STTheme value) {
        this.hAnsiTheme = value;
    }

    /**
     * Gets the value of the eastAsiaTheme property.
     * 
     * @return
     *     possible object is
     *     {@link STTheme }
     *     
     */
    public STTheme getEastAsiaTheme() {
        return eastAsiaTheme;
    }

    /**
     * Sets the value of the eastAsiaTheme property.
     * 
     * @param value
     *     allowed object is
     *     {@link STTheme }
     *     
     */
    public void setEastAsiaTheme(STTheme value) {
        this.eastAsiaTheme = value;
    }

    /**
     * Gets the value of the cstheme property.
     * 
     * @return
     *     possible object is
     *     {@link STTheme }
     *     
     */
    public STTheme getCstheme() {
        return cstheme;
    }

    /**
     * Sets the value of the cstheme property.
     * 
     * @param value
     *     allowed object is
     *     {@link STTheme }
     *     
     */
    public void setCstheme(STTheme value) {
        this.cstheme = value;
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
