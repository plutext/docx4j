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
import javax.xml.bind.annotation.XmlTransient;
import javax.xml.bind.annotation.XmlType;

import org.jvnet.jaxb2_commons.ppp.Child;


/**
 * <p>Java class for CT_Shd complex type.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
 * 
 * <pre>
 * &lt;complexType name="CT_Shd">
 *   &lt;complexContent>
 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
 *       &lt;attribute name="val" use="required" type="{http://schemas.openxmlformats.org/wordprocessingml/2006/main}ST_Shd" />
 *       &lt;attribute name="color" type="{http://schemas.openxmlformats.org/wordprocessingml/2006/main}ST_HexColor" />
 *       &lt;attribute name="themeColor" type="{http://schemas.openxmlformats.org/wordprocessingml/2006/main}ST_ThemeColor" />
 *       &lt;attribute name="themeTint" type="{http://schemas.openxmlformats.org/wordprocessingml/2006/main}ST_UcharHexNumber" />
 *       &lt;attribute name="themeShade" type="{http://schemas.openxmlformats.org/wordprocessingml/2006/main}ST_UcharHexNumber" />
 *       &lt;attribute name="fill" type="{http://schemas.openxmlformats.org/wordprocessingml/2006/main}ST_HexColor" />
 *       &lt;attribute name="themeFill" type="{http://schemas.openxmlformats.org/wordprocessingml/2006/main}ST_ThemeColor" />
 *       &lt;attribute name="themeFillTint" type="{http://schemas.openxmlformats.org/wordprocessingml/2006/main}ST_UcharHexNumber" />
 *       &lt;attribute name="themeFillShade" type="{http://schemas.openxmlformats.org/wordprocessingml/2006/main}ST_UcharHexNumber" />
 *     &lt;/restriction>
 *   &lt;/complexContent>
 * &lt;/complexType>
 * </pre>
 * 
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "CT_Shd")
public class CTShd implements Child
{

    @XmlAttribute(name = "val", namespace = "http://schemas.openxmlformats.org/wordprocessingml/2006/main", required = true)
    protected STShd val;
    @XmlAttribute(name = "color", namespace = "http://schemas.openxmlformats.org/wordprocessingml/2006/main")
    protected String color;
    @XmlAttribute(name = "themeColor", namespace = "http://schemas.openxmlformats.org/wordprocessingml/2006/main")
    protected STThemeColor themeColor;
    @XmlAttribute(name = "themeTint", namespace = "http://schemas.openxmlformats.org/wordprocessingml/2006/main")
    protected String themeTint;
    @XmlAttribute(name = "themeShade", namespace = "http://schemas.openxmlformats.org/wordprocessingml/2006/main")
    protected String themeShade;
    @XmlAttribute(name = "fill", namespace = "http://schemas.openxmlformats.org/wordprocessingml/2006/main")
    protected String fill;
    @XmlAttribute(name = "themeFill", namespace = "http://schemas.openxmlformats.org/wordprocessingml/2006/main")
    protected STThemeColor themeFill;
    @XmlAttribute(name = "themeFillTint", namespace = "http://schemas.openxmlformats.org/wordprocessingml/2006/main")
    protected String themeFillTint;
    @XmlAttribute(name = "themeFillShade", namespace = "http://schemas.openxmlformats.org/wordprocessingml/2006/main")
    protected String themeFillShade;
    @XmlTransient
    private Object parent;

    /**
     * Gets the value of the val property.
     * 
     * @return
     *     possible object is
     *     {@link STShd }
     *     
     */
    public STShd getVal() {
        return val;
    }

    /**
     * Sets the value of the val property.
     * 
     * @param value
     *     allowed object is
     *     {@link STShd }
     *     
     */
    public void setVal(STShd value) {
        this.val = value;
    }

    /**
     * Gets the value of the color property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getColor() {
        return color;
    }

    /**
     * Sets the value of the color property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setColor(String value) {
        this.color = value;
    }

    /**
     * Gets the value of the themeColor property.
     * 
     * @return
     *     possible object is
     *     {@link STThemeColor }
     *     
     */
    public STThemeColor getThemeColor() {
        return themeColor;
    }

    /**
     * Sets the value of the themeColor property.
     * 
     * @param value
     *     allowed object is
     *     {@link STThemeColor }
     *     
     */
    public void setThemeColor(STThemeColor value) {
        this.themeColor = value;
    }

    /**
     * Gets the value of the themeTint property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getThemeTint() {
        return themeTint;
    }

    /**
     * Sets the value of the themeTint property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setThemeTint(String value) {
        this.themeTint = value;
    }

    /**
     * Gets the value of the themeShade property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getThemeShade() {
        return themeShade;
    }

    /**
     * Sets the value of the themeShade property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setThemeShade(String value) {
        this.themeShade = value;
    }

    /**
     * Gets the value of the fill property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getFill() {
        return fill;
    }

    /**
     * Sets the value of the fill property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setFill(String value) {
        this.fill = value;
    }

    /**
     * Gets the value of the themeFill property.
     * 
     * @return
     *     possible object is
     *     {@link STThemeColor }
     *     
     */
    public STThemeColor getThemeFill() {
        return themeFill;
    }

    /**
     * Sets the value of the themeFill property.
     * 
     * @param value
     *     allowed object is
     *     {@link STThemeColor }
     *     
     */
    public void setThemeFill(STThemeColor value) {
        this.themeFill = value;
    }

    /**
     * Gets the value of the themeFillTint property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getThemeFillTint() {
        return themeFillTint;
    }

    /**
     * Sets the value of the themeFillTint property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setThemeFillTint(String value) {
        this.themeFillTint = value;
    }

    /**
     * Gets the value of the themeFillShade property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getThemeFillShade() {
        return themeFillShade;
    }

    /**
     * Sets the value of the themeFillShade property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setThemeFillShade(String value) {
        this.themeFillShade = value;
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
