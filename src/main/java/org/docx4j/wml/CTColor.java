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
 * <p>Java class for CT_Color complex type.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
 * 
 * <pre>
 * &lt;complexType name="CT_Color">
 *   &lt;complexContent>
 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
 *       &lt;attribute name="val" use="required" type="{http://schemas.openxmlformats.org/wordprocessingml/2006/main}ST_HexColor" />
 *       &lt;attribute name="themeColor" type="{http://schemas.openxmlformats.org/wordprocessingml/2006/main}ST_ThemeColor" />
 *       &lt;attribute name="themeTint" type="{http://schemas.openxmlformats.org/wordprocessingml/2006/main}ST_UcharHexNumber" />
 *       &lt;attribute name="themeShade" type="{http://schemas.openxmlformats.org/wordprocessingml/2006/main}ST_UcharHexNumber" />
 *     &lt;/restriction>
 *   &lt;/complexContent>
 * &lt;/complexType>
 * </pre>
 * 
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "CT_Color")
public class CTColor implements Child
{

    @XmlAttribute(name = "val", namespace = "http://schemas.openxmlformats.org/wordprocessingml/2006/main", required = true)
    protected String val;
    @XmlAttribute(name = "themeColor", namespace = "http://schemas.openxmlformats.org/wordprocessingml/2006/main")
    protected STThemeColor themeColor;
    @XmlAttribute(name = "themeTint", namespace = "http://schemas.openxmlformats.org/wordprocessingml/2006/main")
    protected String themeTint;
    @XmlAttribute(name = "themeShade", namespace = "http://schemas.openxmlformats.org/wordprocessingml/2006/main")
    protected String themeShade;
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
