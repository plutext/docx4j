/*
 *  Copyright 2010, Plutext Pty Ltd.
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


package org.plutext.jaxb.svg11;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlID;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlSchemaType;
import javax.xml.bind.annotation.XmlType;
import javax.xml.bind.annotation.adapters.CollapsedStringAdapter;
import javax.xml.bind.annotation.adapters.XmlJavaTypeAdapter;


/**
 * <p>Java class for anonymous complex type.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
 * 
 * <pre>
 * &lt;complexType>
 *   &lt;complexContent>
 *     &lt;extension base="{http://www.w3.org/2000/svg}SVG.feSpotLight.content">
 *       &lt;attGroup ref="{http://www.w3.org/2000/svg}SVG.Core.attrib"/>
 *       &lt;attribute name="x" type="{http://www.w3.org/2000/svg}Number.datatype" />
 *       &lt;attribute name="y" type="{http://www.w3.org/2000/svg}Number.datatype" />
 *       &lt;attribute name="z" type="{http://www.w3.org/2000/svg}Number.datatype" />
 *       &lt;attribute name="pointsAtX" type="{http://www.w3.org/2000/svg}Number.datatype" />
 *       &lt;attribute name="pointsAtY" type="{http://www.w3.org/2000/svg}Number.datatype" />
 *       &lt;attribute name="pointsAtZ" type="{http://www.w3.org/2000/svg}Number.datatype" />
 *       &lt;attribute name="specularExponent" type="{http://www.w3.org/2000/svg}Number.datatype" />
 *       &lt;attribute name="limitingConeAngle" type="{http://www.w3.org/2000/svg}Number.datatype" />
 *     &lt;/extension>
 *   &lt;/complexContent>
 * &lt;/complexType>
 * </pre>
 * 
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "")
@XmlRootElement(name = "feSpotLight")
public class FeSpotLight
    extends SVGFeSpotLightContent
{

    @XmlAttribute
    protected String x;
    @XmlAttribute
    protected String y;
    @XmlAttribute
    protected String z;
    @XmlAttribute
    protected String pointsAtX;
    @XmlAttribute
    protected String pointsAtY;
    @XmlAttribute
    protected String pointsAtZ;
    @XmlAttribute
    protected String specularExponent;
    @XmlAttribute
    protected String limitingConeAngle;
    @XmlAttribute
    @XmlJavaTypeAdapter(CollapsedStringAdapter.class)
    @XmlID
    @XmlSchemaType(name = "ID")
    protected String id;
    @XmlAttribute(namespace = "http://www.w3.org/XML/1998/namespace")
    protected String base;
    @XmlAttribute(namespace = "http://www.w3.org/XML/1998/namespace", required = true)
    @XmlJavaTypeAdapter(CollapsedStringAdapter.class)
    protected String space;
    @XmlAttribute(namespace = "http://www.w3.org/XML/1998/namespace")
    @XmlJavaTypeAdapter(CollapsedStringAdapter.class)
    protected String lang;

    /**
     * Gets the value of the x property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getX() {
        return x;
    }

    /**
     * Sets the value of the x property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setX(String value) {
        this.x = value;
    }

    /**
     * Gets the value of the y property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getY() {
        return y;
    }

    /**
     * Sets the value of the y property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setY(String value) {
        this.y = value;
    }

    /**
     * Gets the value of the z property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getZ() {
        return z;
    }

    /**
     * Sets the value of the z property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setZ(String value) {
        this.z = value;
    }

    /**
     * Gets the value of the pointsAtX property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getPointsAtX() {
        return pointsAtX;
    }

    /**
     * Sets the value of the pointsAtX property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setPointsAtX(String value) {
        this.pointsAtX = value;
    }

    /**
     * Gets the value of the pointsAtY property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getPointsAtY() {
        return pointsAtY;
    }

    /**
     * Sets the value of the pointsAtY property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setPointsAtY(String value) {
        this.pointsAtY = value;
    }

    /**
     * Gets the value of the pointsAtZ property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getPointsAtZ() {
        return pointsAtZ;
    }

    /**
     * Sets the value of the pointsAtZ property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setPointsAtZ(String value) {
        this.pointsAtZ = value;
    }

    /**
     * Gets the value of the specularExponent property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getSpecularExponent() {
        return specularExponent;
    }

    /**
     * Sets the value of the specularExponent property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setSpecularExponent(String value) {
        this.specularExponent = value;
    }

    /**
     * Gets the value of the limitingConeAngle property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getLimitingConeAngle() {
        return limitingConeAngle;
    }

    /**
     * Sets the value of the limitingConeAngle property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setLimitingConeAngle(String value) {
        this.limitingConeAngle = value;
    }

    /**
     * Gets the value of the id property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getId() {
        return id;
    }

    /**
     * Sets the value of the id property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setId(String value) {
        this.id = value;
    }

    /**
     * Gets the value of the base property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getBase() {
        return base;
    }

    /**
     * Sets the value of the base property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setBase(String value) {
        this.base = value;
    }

    /**
     * Gets the value of the space property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getSpace() {
        return space;
    }

    /**
     * Sets the value of the space property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setSpace(String value) {
        this.space = value;
    }

    /**
     * Gets the value of the lang property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getLang() {
        return lang;
    }

    /**
     * Sets the value of the lang property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setLang(String value) {
        this.lang = value;
    }

}
