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

import java.util.ArrayList;
import java.util.List;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlElements;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlSchemaType;
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
 *       &lt;choice maxOccurs="unbounded" minOccurs="0">
 *         &lt;element ref="{http://www.w3.org/2000/svg}animate"/>
 *         &lt;element ref="{http://www.w3.org/2000/svg}set"/>
 *       &lt;/choice>
 *       &lt;attribute name="x" type="{http://www.w3.org/2001/XMLSchema}anySimpleType" />
 *       &lt;attribute name="y" type="{http://www.w3.org/2001/XMLSchema}anySimpleType" />
 *       &lt;attribute name="z" type="{http://www.w3.org/2001/XMLSchema}anySimpleType" />
 *       &lt;attribute name="pointsAtX" type="{http://www.w3.org/2001/XMLSchema}anySimpleType" />
 *       &lt;attribute name="pointsAtY" type="{http://www.w3.org/2001/XMLSchema}anySimpleType" />
 *       &lt;attribute name="pointsAtZ" type="{http://www.w3.org/2001/XMLSchema}anySimpleType" />
 *       &lt;attribute name="specularExponent" type="{http://www.w3.org/2001/XMLSchema}anySimpleType" />
 *       &lt;attribute name="limitingConeAngle" type="{http://www.w3.org/2001/XMLSchema}anySimpleType" />
 *     &lt;/restriction>
 *   &lt;/complexContent>
 * &lt;/complexType>
 * </pre>
 * 
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "", propOrder = {
    "animateOrSet"
})
@XmlRootElement(name = "feSpotLight")
public class FeSpotLight {

    @XmlElements({
        @XmlElement(name = "animate", type = Animate.class),
        @XmlElement(name = "set", type = Set.class)
    })
    protected List<Object> animateOrSet;
    @XmlAttribute
    @XmlSchemaType(name = "anySimpleType")
    protected String x;
    @XmlAttribute
    @XmlSchemaType(name = "anySimpleType")
    protected String y;
    @XmlAttribute
    @XmlSchemaType(name = "anySimpleType")
    protected String z;
    @XmlAttribute
    @XmlSchemaType(name = "anySimpleType")
    protected String pointsAtX;
    @XmlAttribute
    @XmlSchemaType(name = "anySimpleType")
    protected String pointsAtY;
    @XmlAttribute
    @XmlSchemaType(name = "anySimpleType")
    protected String pointsAtZ;
    @XmlAttribute
    @XmlSchemaType(name = "anySimpleType")
    protected String specularExponent;
    @XmlAttribute
    @XmlSchemaType(name = "anySimpleType")
    protected String limitingConeAngle;

    /**
     * Gets the value of the animateOrSet property.
     * 
     * <p>
     * This accessor method returns a reference to the live list,
     * not a snapshot. Therefore any modification you make to the
     * returned list will be present inside the JAXB object.
     * This is why there is not a <CODE>set</CODE> method for the animateOrSet property.
     * 
     * <p>
     * For example, to add a new item, do as follows:
     * <pre>
     *    getAnimateOrSet().add(newItem);
     * </pre>
     * 
     * 
     * <p>
     * Objects of the following type(s) are allowed in the list
     * {@link Animate }
     * {@link Set }
     * 
     * 
     */
    public List<Object> getAnimateOrSet() {
        if (animateOrSet == null) {
            animateOrSet = new ArrayList<Object>();
        }
        return this.animateOrSet;
    }

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

}
