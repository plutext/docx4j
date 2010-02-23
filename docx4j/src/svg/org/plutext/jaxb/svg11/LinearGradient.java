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
 *       &lt;sequence>
 *         &lt;choice maxOccurs="unbounded" minOccurs="0">
 *           &lt;element ref="{http://www.w3.org/2000/svg}desc"/>
 *           &lt;element ref="{http://www.w3.org/2000/svg}title"/>
 *           &lt;element ref="{http://www.w3.org/2000/svg}metadata"/>
 *         &lt;/choice>
 *         &lt;choice maxOccurs="unbounded" minOccurs="0">
 *           &lt;element ref="{http://www.w3.org/2000/svg}stop"/>
 *           &lt;element ref="{http://www.w3.org/2000/svg}animate"/>
 *           &lt;element ref="{http://www.w3.org/2000/svg}set"/>
 *           &lt;element ref="{http://www.w3.org/2000/svg}animateTransform"/>
 *         &lt;/choice>
 *       &lt;/sequence>
 *       &lt;attribute name="x1" type="{http://www.w3.org/2001/XMLSchema}anySimpleType" />
 *       &lt;attribute name="y1" type="{http://www.w3.org/2001/XMLSchema}anySimpleType" />
 *       &lt;attribute name="x2" type="{http://www.w3.org/2001/XMLSchema}anySimpleType" />
 *       &lt;attribute name="y2" type="{http://www.w3.org/2001/XMLSchema}anySimpleType" />
 *       &lt;attribute name="gradientUnits">
 *         &lt;simpleType>
 *           &lt;restriction base="{http://www.w3.org/2001/XMLSchema}string">
 *             &lt;enumeration value="userSpaceOnUse"/>
 *             &lt;enumeration value="objectBoundingBox"/>
 *           &lt;/restriction>
 *         &lt;/simpleType>
 *       &lt;/attribute>
 *       &lt;attribute name="gradientTransform" type="{http://www.w3.org/2001/XMLSchema}anySimpleType" />
 *       &lt;attribute name="spreadMethod">
 *         &lt;simpleType>
 *           &lt;restriction base="{http://www.w3.org/2001/XMLSchema}string">
 *             &lt;enumeration value="pad"/>
 *             &lt;enumeration value="reflect"/>
 *             &lt;enumeration value="repeat"/>
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
@XmlType(name = "", propOrder = {
    "descOrTitleOrMetadata",
    "stopOrAnimateOrSet"
})
@XmlRootElement(name = "linearGradient")
public class LinearGradient {

    @XmlElements({
        @XmlElement(name = "metadata", type = Metadata.class),
        @XmlElement(name = "desc", type = Desc.class),
        @XmlElement(name = "title", type = Title.class)
    })
    protected List<Object> descOrTitleOrMetadata;
    @XmlElements({
        @XmlElement(name = "set", type = Set.class),
        @XmlElement(name = "animate", type = Animate.class),
        @XmlElement(name = "animateTransform", type = AnimateTransform.class),
        @XmlElement(name = "stop", type = Stop.class)
    })
    protected List<Object> stopOrAnimateOrSet;
    @XmlAttribute
    @XmlSchemaType(name = "anySimpleType")
    protected String x1;
    @XmlAttribute
    @XmlSchemaType(name = "anySimpleType")
    protected String y1;
    @XmlAttribute
    @XmlSchemaType(name = "anySimpleType")
    protected String x2;
    @XmlAttribute
    @XmlSchemaType(name = "anySimpleType")
    protected String y2;
    @XmlAttribute
    protected String gradientUnits;
    @XmlAttribute
    @XmlSchemaType(name = "anySimpleType")
    protected String gradientTransform;
    @XmlAttribute
    protected String spreadMethod;

    /**
     * Gets the value of the descOrTitleOrMetadata property.
     * 
     * <p>
     * This accessor method returns a reference to the live list,
     * not a snapshot. Therefore any modification you make to the
     * returned list will be present inside the JAXB object.
     * This is why there is not a <CODE>set</CODE> method for the descOrTitleOrMetadata property.
     * 
     * <p>
     * For example, to add a new item, do as follows:
     * <pre>
     *    getDescOrTitleOrMetadata().add(newItem);
     * </pre>
     * 
     * 
     * <p>
     * Objects of the following type(s) are allowed in the list
     * {@link Metadata }
     * {@link Desc }
     * {@link Title }
     * 
     * 
     */
    public List<Object> getDescOrTitleOrMetadata() {
        if (descOrTitleOrMetadata == null) {
            descOrTitleOrMetadata = new ArrayList<Object>();
        }
        return this.descOrTitleOrMetadata;
    }

    /**
     * Gets the value of the stopOrAnimateOrSet property.
     * 
     * <p>
     * This accessor method returns a reference to the live list,
     * not a snapshot. Therefore any modification you make to the
     * returned list will be present inside the JAXB object.
     * This is why there is not a <CODE>set</CODE> method for the stopOrAnimateOrSet property.
     * 
     * <p>
     * For example, to add a new item, do as follows:
     * <pre>
     *    getStopOrAnimateOrSet().add(newItem);
     * </pre>
     * 
     * 
     * <p>
     * Objects of the following type(s) are allowed in the list
     * {@link Set }
     * {@link Animate }
     * {@link AnimateTransform }
     * {@link Stop }
     * 
     * 
     */
    public List<Object> getStopOrAnimateOrSet() {
        if (stopOrAnimateOrSet == null) {
            stopOrAnimateOrSet = new ArrayList<Object>();
        }
        return this.stopOrAnimateOrSet;
    }

    /**
     * Gets the value of the x1 property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getX1() {
        return x1;
    }

    /**
     * Sets the value of the x1 property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setX1(String value) {
        this.x1 = value;
    }

    /**
     * Gets the value of the y1 property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getY1() {
        return y1;
    }

    /**
     * Sets the value of the y1 property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setY1(String value) {
        this.y1 = value;
    }

    /**
     * Gets the value of the x2 property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getX2() {
        return x2;
    }

    /**
     * Sets the value of the x2 property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setX2(String value) {
        this.x2 = value;
    }

    /**
     * Gets the value of the y2 property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getY2() {
        return y2;
    }

    /**
     * Sets the value of the y2 property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setY2(String value) {
        this.y2 = value;
    }

    /**
     * Gets the value of the gradientUnits property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getGradientUnits() {
        return gradientUnits;
    }

    /**
     * Sets the value of the gradientUnits property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setGradientUnits(String value) {
        this.gradientUnits = value;
    }

    /**
     * Gets the value of the gradientTransform property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getGradientTransform() {
        return gradientTransform;
    }

    /**
     * Sets the value of the gradientTransform property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setGradientTransform(String value) {
        this.gradientTransform = value;
    }

    /**
     * Gets the value of the spreadMethod property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getSpreadMethod() {
        return spreadMethod;
    }

    /**
     * Sets the value of the spreadMethod property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setSpreadMethod(String value) {
        this.spreadMethod = value;
    }

}
