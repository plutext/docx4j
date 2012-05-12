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

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlType;


/**
 * <p>Java class for CT_ColorScheme complex type.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
 * 
 * <pre>
 * &lt;complexType name="CT_ColorScheme">
 *   &lt;complexContent>
 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
 *       &lt;sequence>
 *         &lt;element name="dk1" type="{http://schemas.openxmlformats.org/drawingml/2006/main}CT_Color"/>
 *         &lt;element name="lt1" type="{http://schemas.openxmlformats.org/drawingml/2006/main}CT_Color"/>
 *         &lt;element name="dk2" type="{http://schemas.openxmlformats.org/drawingml/2006/main}CT_Color"/>
 *         &lt;element name="lt2" type="{http://schemas.openxmlformats.org/drawingml/2006/main}CT_Color"/>
 *         &lt;element name="accent1" type="{http://schemas.openxmlformats.org/drawingml/2006/main}CT_Color"/>
 *         &lt;element name="accent2" type="{http://schemas.openxmlformats.org/drawingml/2006/main}CT_Color"/>
 *         &lt;element name="accent3" type="{http://schemas.openxmlformats.org/drawingml/2006/main}CT_Color"/>
 *         &lt;element name="accent4" type="{http://schemas.openxmlformats.org/drawingml/2006/main}CT_Color"/>
 *         &lt;element name="accent5" type="{http://schemas.openxmlformats.org/drawingml/2006/main}CT_Color"/>
 *         &lt;element name="accent6" type="{http://schemas.openxmlformats.org/drawingml/2006/main}CT_Color"/>
 *         &lt;element name="hlink" type="{http://schemas.openxmlformats.org/drawingml/2006/main}CT_Color"/>
 *         &lt;element name="folHlink" type="{http://schemas.openxmlformats.org/drawingml/2006/main}CT_Color"/>
 *         &lt;element name="extLst" type="{http://schemas.openxmlformats.org/drawingml/2006/main}CT_OfficeArtExtensionList" minOccurs="0"/>
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
@XmlType(name = "CT_ColorScheme", propOrder = {
    "dk1",
    "lt1",
    "dk2",
    "lt2",
    "accent1",
    "accent2",
    "accent3",
    "accent4",
    "accent5",
    "accent6",
    "hlink",
    "folHlink",
    "extLst"
})
public class CTColorScheme {

    @XmlElement(required = true)
    protected CTColor dk1;
    @XmlElement(required = true)
    protected CTColor lt1;
    @XmlElement(required = true)
    protected CTColor dk2;
    @XmlElement(required = true)
    protected CTColor lt2;
    @XmlElement(required = true)
    protected CTColor accent1;
    @XmlElement(required = true)
    protected CTColor accent2;
    @XmlElement(required = true)
    protected CTColor accent3;
    @XmlElement(required = true)
    protected CTColor accent4;
    @XmlElement(required = true)
    protected CTColor accent5;
    @XmlElement(required = true)
    protected CTColor accent6;
    @XmlElement(required = true)
    protected CTColor hlink;
    @XmlElement(required = true)
    protected CTColor folHlink;
    protected CTOfficeArtExtensionList extLst;
    @XmlAttribute(required = true)
    protected String name;

    /**
     * Gets the value of the dk1 property.
     * 
     * @return
     *     possible object is
     *     {@link CTColor }
     *     
     */
    public CTColor getDk1() {
        return dk1;
    }

    /**
     * Sets the value of the dk1 property.
     * 
     * @param value
     *     allowed object is
     *     {@link CTColor }
     *     
     */
    public void setDk1(CTColor value) {
        this.dk1 = value;
    }

    /**
     * Gets the value of the lt1 property.
     * 
     * @return
     *     possible object is
     *     {@link CTColor }
     *     
     */
    public CTColor getLt1() {
        return lt1;
    }

    /**
     * Sets the value of the lt1 property.
     * 
     * @param value
     *     allowed object is
     *     {@link CTColor }
     *     
     */
    public void setLt1(CTColor value) {
        this.lt1 = value;
    }

    /**
     * Gets the value of the dk2 property.
     * 
     * @return
     *     possible object is
     *     {@link CTColor }
     *     
     */
    public CTColor getDk2() {
        return dk2;
    }

    /**
     * Sets the value of the dk2 property.
     * 
     * @param value
     *     allowed object is
     *     {@link CTColor }
     *     
     */
    public void setDk2(CTColor value) {
        this.dk2 = value;
    }

    /**
     * Gets the value of the lt2 property.
     * 
     * @return
     *     possible object is
     *     {@link CTColor }
     *     
     */
    public CTColor getLt2() {
        return lt2;
    }

    /**
     * Sets the value of the lt2 property.
     * 
     * @param value
     *     allowed object is
     *     {@link CTColor }
     *     
     */
    public void setLt2(CTColor value) {
        this.lt2 = value;
    }

    /**
     * Gets the value of the accent1 property.
     * 
     * @return
     *     possible object is
     *     {@link CTColor }
     *     
     */
    public CTColor getAccent1() {
        return accent1;
    }

    /**
     * Sets the value of the accent1 property.
     * 
     * @param value
     *     allowed object is
     *     {@link CTColor }
     *     
     */
    public void setAccent1(CTColor value) {
        this.accent1 = value;
    }

    /**
     * Gets the value of the accent2 property.
     * 
     * @return
     *     possible object is
     *     {@link CTColor }
     *     
     */
    public CTColor getAccent2() {
        return accent2;
    }

    /**
     * Sets the value of the accent2 property.
     * 
     * @param value
     *     allowed object is
     *     {@link CTColor }
     *     
     */
    public void setAccent2(CTColor value) {
        this.accent2 = value;
    }

    /**
     * Gets the value of the accent3 property.
     * 
     * @return
     *     possible object is
     *     {@link CTColor }
     *     
     */
    public CTColor getAccent3() {
        return accent3;
    }

    /**
     * Sets the value of the accent3 property.
     * 
     * @param value
     *     allowed object is
     *     {@link CTColor }
     *     
     */
    public void setAccent3(CTColor value) {
        this.accent3 = value;
    }

    /**
     * Gets the value of the accent4 property.
     * 
     * @return
     *     possible object is
     *     {@link CTColor }
     *     
     */
    public CTColor getAccent4() {
        return accent4;
    }

    /**
     * Sets the value of the accent4 property.
     * 
     * @param value
     *     allowed object is
     *     {@link CTColor }
     *     
     */
    public void setAccent4(CTColor value) {
        this.accent4 = value;
    }

    /**
     * Gets the value of the accent5 property.
     * 
     * @return
     *     possible object is
     *     {@link CTColor }
     *     
     */
    public CTColor getAccent5() {
        return accent5;
    }

    /**
     * Sets the value of the accent5 property.
     * 
     * @param value
     *     allowed object is
     *     {@link CTColor }
     *     
     */
    public void setAccent5(CTColor value) {
        this.accent5 = value;
    }

    /**
     * Gets the value of the accent6 property.
     * 
     * @return
     *     possible object is
     *     {@link CTColor }
     *     
     */
    public CTColor getAccent6() {
        return accent6;
    }

    /**
     * Sets the value of the accent6 property.
     * 
     * @param value
     *     allowed object is
     *     {@link CTColor }
     *     
     */
    public void setAccent6(CTColor value) {
        this.accent6 = value;
    }

    /**
     * Gets the value of the hlink property.
     * 
     * @return
     *     possible object is
     *     {@link CTColor }
     *     
     */
    public CTColor getHlink() {
        return hlink;
    }

    /**
     * Sets the value of the hlink property.
     * 
     * @param value
     *     allowed object is
     *     {@link CTColor }
     *     
     */
    public void setHlink(CTColor value) {
        this.hlink = value;
    }

    /**
     * Gets the value of the folHlink property.
     * 
     * @return
     *     possible object is
     *     {@link CTColor }
     *     
     */
    public CTColor getFolHlink() {
        return folHlink;
    }

    /**
     * Sets the value of the folHlink property.
     * 
     * @param value
     *     allowed object is
     *     {@link CTColor }
     *     
     */
    public void setFolHlink(CTColor value) {
        this.folHlink = value;
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

}
