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
 *         &lt;element ref="{http://www.w3.org/2000/svg}animateColor"/>
 *       &lt;/choice>
 *       &lt;attGroup ref="{http://www.w3.org/2000/svg}SVG.FilterPrimitiveWithIn.attrib"/>
 *       &lt;attribute name="flood-color" type="{http://www.w3.org/2001/XMLSchema}anySimpleType" />
 *       &lt;attribute name="flood-opacity" type="{http://www.w3.org/2001/XMLSchema}anySimpleType" />
 *     &lt;/restriction>
 *   &lt;/complexContent>
 * &lt;/complexType>
 * </pre>
 * 
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "", propOrder = {
    "animateOrSetOrAnimateColor"
})
@XmlRootElement(name = "feFlood")
public class FeFlood {

    @XmlElements({
        @XmlElement(name = "animateColor", type = AnimateColor.class),
        @XmlElement(name = "animate", type = Animate.class),
        @XmlElement(name = "set", type = Set.class)
    })
    protected List<Object> animateOrSetOrAnimateColor;
    @XmlAttribute(name = "flood-color")
    @XmlSchemaType(name = "anySimpleType")
    protected String floodColor;
    @XmlAttribute(name = "flood-opacity")
    @XmlSchemaType(name = "anySimpleType")
    protected String floodOpacity;
    @XmlAttribute
    @XmlSchemaType(name = "anySimpleType")
    protected String in;
    @XmlAttribute
    @XmlSchemaType(name = "anySimpleType")
    protected String x;
    @XmlAttribute
    @XmlSchemaType(name = "anySimpleType")
    protected String y;
    @XmlAttribute
    @XmlSchemaType(name = "anySimpleType")
    protected String width;
    @XmlAttribute
    @XmlSchemaType(name = "anySimpleType")
    protected String height;
    @XmlAttribute
    @XmlSchemaType(name = "anySimpleType")
    protected String result;

    /**
     * Gets the value of the animateOrSetOrAnimateColor property.
     * 
     * <p>
     * This accessor method returns a reference to the live list,
     * not a snapshot. Therefore any modification you make to the
     * returned list will be present inside the JAXB object.
     * This is why there is not a <CODE>set</CODE> method for the animateOrSetOrAnimateColor property.
     * 
     * <p>
     * For example, to add a new item, do as follows:
     * <pre>
     *    getAnimateOrSetOrAnimateColor().add(newItem);
     * </pre>
     * 
     * 
     * <p>
     * Objects of the following type(s) are allowed in the list
     * {@link AnimateColor }
     * {@link Animate }
     * {@link Set }
     * 
     * 
     */
    public List<Object> getAnimateOrSetOrAnimateColor() {
        if (animateOrSetOrAnimateColor == null) {
            animateOrSetOrAnimateColor = new ArrayList<Object>();
        }
        return this.animateOrSetOrAnimateColor;
    }

    /**
     * Gets the value of the floodColor property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getFloodColor() {
        return floodColor;
    }

    /**
     * Sets the value of the floodColor property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setFloodColor(String value) {
        this.floodColor = value;
    }

    /**
     * Gets the value of the floodOpacity property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getFloodOpacity() {
        return floodOpacity;
    }

    /**
     * Sets the value of the floodOpacity property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setFloodOpacity(String value) {
        this.floodOpacity = value;
    }

    /**
     * Gets the value of the in property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getIn() {
        return in;
    }

    /**
     * Sets the value of the in property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setIn(String value) {
        this.in = value;
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
     * Gets the value of the width property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getWidth() {
        return width;
    }

    /**
     * Sets the value of the width property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setWidth(String value) {
        this.width = value;
    }

    /**
     * Gets the value of the height property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getHeight() {
        return height;
    }

    /**
     * Sets the value of the height property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setHeight(String value) {
        this.height = value;
    }

    /**
     * Gets the value of the result property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getResult() {
        return result;
    }

    /**
     * Sets the value of the result property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setResult(String value) {
        this.result = value;
    }

}
