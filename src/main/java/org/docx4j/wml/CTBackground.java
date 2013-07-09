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
import javax.xml.bind.annotation.XmlType;


/**
 * <p>Java class for CT_Background complex type.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
 * 
 * <pre>
 * &lt;complexType name="CT_Background">
 *   &lt;complexContent>
 *     &lt;extension base="{http://schemas.openxmlformats.org/wordprocessingml/2006/main}CT_PictureBase">
 *       &lt;attribute name="color" type="{http://schemas.openxmlformats.org/wordprocessingml/2006/main}ST_HexColor" />
 *       &lt;attribute name="themeColor" type="{http://schemas.openxmlformats.org/wordprocessingml/2006/main}ST_ThemeColor" />
 *       &lt;attribute name="themeTint" type="{http://schemas.openxmlformats.org/wordprocessingml/2006/main}ST_UcharHexNumber" />
 *       &lt;attribute name="themeShade" type="{http://schemas.openxmlformats.org/wordprocessingml/2006/main}ST_UcharHexNumber" />
 *     &lt;/extension>
 *   &lt;/complexContent>
 * &lt;/complexType>
 * </pre>
 * 
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "CT_Background")
public class CTBackground
    extends CTPictureBase
{

    @XmlAttribute(name = "color", namespace = "http://schemas.openxmlformats.org/wordprocessingml/2006/main")
    protected String color;
    @XmlAttribute(name = "themeColor", namespace = "http://schemas.openxmlformats.org/wordprocessingml/2006/main")
    protected STThemeColor themeColor;
    @XmlAttribute(name = "themeTint", namespace = "http://schemas.openxmlformats.org/wordprocessingml/2006/main")
    protected String themeTint;
    @XmlAttribute(name = "themeShade", namespace = "http://schemas.openxmlformats.org/wordprocessingml/2006/main")
    protected String themeShade;

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
