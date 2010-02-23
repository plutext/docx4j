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
 *       &lt;attribute name="type" use="required">
 *         &lt;simpleType>
 *           &lt;restriction base="{http://www.w3.org/2001/XMLSchema}string">
 *             &lt;enumeration value="identity"/>
 *             &lt;enumeration value="table"/>
 *             &lt;enumeration value="discrete"/>
 *             &lt;enumeration value="linear"/>
 *             &lt;enumeration value="gamma"/>
 *           &lt;/restriction>
 *         &lt;/simpleType>
 *       &lt;/attribute>
 *       &lt;attribute name="tableValues" type="{http://www.w3.org/2001/XMLSchema}anySimpleType" />
 *       &lt;attribute name="slope" type="{http://www.w3.org/2001/XMLSchema}anySimpleType" />
 *       &lt;attribute name="intercept" type="{http://www.w3.org/2001/XMLSchema}anySimpleType" />
 *       &lt;attribute name="amplitude" type="{http://www.w3.org/2001/XMLSchema}anySimpleType" />
 *       &lt;attribute name="exponent" type="{http://www.w3.org/2001/XMLSchema}anySimpleType" />
 *       &lt;attribute name="offset" type="{http://www.w3.org/2001/XMLSchema}anySimpleType" />
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
@XmlRootElement(name = "feFuncA")
public class FeFuncA {

    @XmlElements({
        @XmlElement(name = "animate", type = Animate.class),
        @XmlElement(name = "set", type = Set.class)
    })
    protected List<Object> animateOrSet;
    @XmlAttribute(required = true)
    protected String type;
    @XmlAttribute
    @XmlSchemaType(name = "anySimpleType")
    protected String tableValues;
    @XmlAttribute
    @XmlSchemaType(name = "anySimpleType")
    protected String slope;
    @XmlAttribute
    @XmlSchemaType(name = "anySimpleType")
    protected String intercept;
    @XmlAttribute
    @XmlSchemaType(name = "anySimpleType")
    protected String amplitude;
    @XmlAttribute
    @XmlSchemaType(name = "anySimpleType")
    protected String exponent;
    @XmlAttribute
    @XmlSchemaType(name = "anySimpleType")
    protected String offset;

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
     * Gets the value of the type property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getType() {
        return type;
    }

    /**
     * Sets the value of the type property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setType(String value) {
        this.type = value;
    }

    /**
     * Gets the value of the tableValues property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getTableValues() {
        return tableValues;
    }

    /**
     * Sets the value of the tableValues property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setTableValues(String value) {
        this.tableValues = value;
    }

    /**
     * Gets the value of the slope property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getSlope() {
        return slope;
    }

    /**
     * Sets the value of the slope property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setSlope(String value) {
        this.slope = value;
    }

    /**
     * Gets the value of the intercept property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getIntercept() {
        return intercept;
    }

    /**
     * Sets the value of the intercept property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setIntercept(String value) {
        this.intercept = value;
    }

    /**
     * Gets the value of the amplitude property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getAmplitude() {
        return amplitude;
    }

    /**
     * Sets the value of the amplitude property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setAmplitude(String value) {
        this.amplitude = value;
    }

    /**
     * Gets the value of the exponent property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getExponent() {
        return exponent;
    }

    /**
     * Sets the value of the exponent property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setExponent(String value) {
        this.exponent = value;
    }

    /**
     * Gets the value of the offset property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getOffset() {
        return offset;
    }

    /**
     * Sets the value of the offset property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setOffset(String value) {
        this.offset = value;
    }

}
