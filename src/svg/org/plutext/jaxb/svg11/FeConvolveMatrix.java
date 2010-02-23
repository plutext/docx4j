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
 *       &lt;attGroup ref="{http://www.w3.org/2000/svg}SVG.FilterPrimitiveWithIn.attrib"/>
 *       &lt;attribute name="order" use="required" type="{http://www.w3.org/2001/XMLSchema}anySimpleType" />
 *       &lt;attribute name="kernelMatrix" use="required" type="{http://www.w3.org/2001/XMLSchema}anySimpleType" />
 *       &lt;attribute name="divisor" type="{http://www.w3.org/2001/XMLSchema}anySimpleType" />
 *       &lt;attribute name="bias" type="{http://www.w3.org/2001/XMLSchema}anySimpleType" />
 *       &lt;attribute name="targetX" type="{http://www.w3.org/2001/XMLSchema}anySimpleType" />
 *       &lt;attribute name="targetY" type="{http://www.w3.org/2001/XMLSchema}anySimpleType" />
 *       &lt;attribute name="edgeMode" default="duplicate">
 *         &lt;simpleType>
 *           &lt;restriction base="{http://www.w3.org/2001/XMLSchema}string">
 *             &lt;enumeration value="duplicate"/>
 *             &lt;enumeration value="wrap"/>
 *             &lt;enumeration value="none"/>
 *           &lt;/restriction>
 *         &lt;/simpleType>
 *       &lt;/attribute>
 *       &lt;attribute name="kernelUnitLength" type="{http://www.w3.org/2001/XMLSchema}anySimpleType" />
 *       &lt;attribute name="preserveAlpha">
 *         &lt;simpleType>
 *           &lt;restriction base="{http://www.w3.org/2001/XMLSchema}string">
 *             &lt;enumeration value="false"/>
 *             &lt;enumeration value="true"/>
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
    "animateOrSet"
})
@XmlRootElement(name = "feConvolveMatrix")
public class FeConvolveMatrix {

    @XmlElements({
        @XmlElement(name = "set", type = Set.class),
        @XmlElement(name = "animate", type = Animate.class)
    })
    protected List<Object> animateOrSet;
    @XmlAttribute(required = true)
    @XmlSchemaType(name = "anySimpleType")
    protected String order;
    @XmlAttribute(required = true)
    @XmlSchemaType(name = "anySimpleType")
    protected String kernelMatrix;
    @XmlAttribute
    @XmlSchemaType(name = "anySimpleType")
    protected String divisor;
    @XmlAttribute
    @XmlSchemaType(name = "anySimpleType")
    protected String bias;
    @XmlAttribute
    @XmlSchemaType(name = "anySimpleType")
    protected String targetX;
    @XmlAttribute
    @XmlSchemaType(name = "anySimpleType")
    protected String targetY;
    @XmlAttribute
    protected String edgeMode;
    @XmlAttribute
    @XmlSchemaType(name = "anySimpleType")
    protected String kernelUnitLength;
    @XmlAttribute
    protected String preserveAlpha;
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
     * {@link Set }
     * {@link Animate }
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
     * Gets the value of the order property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getOrder() {
        return order;
    }

    /**
     * Sets the value of the order property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setOrder(String value) {
        this.order = value;
    }

    /**
     * Gets the value of the kernelMatrix property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getKernelMatrix() {
        return kernelMatrix;
    }

    /**
     * Sets the value of the kernelMatrix property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setKernelMatrix(String value) {
        this.kernelMatrix = value;
    }

    /**
     * Gets the value of the divisor property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getDivisor() {
        return divisor;
    }

    /**
     * Sets the value of the divisor property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setDivisor(String value) {
        this.divisor = value;
    }

    /**
     * Gets the value of the bias property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getBias() {
        return bias;
    }

    /**
     * Sets the value of the bias property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setBias(String value) {
        this.bias = value;
    }

    /**
     * Gets the value of the targetX property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getTargetX() {
        return targetX;
    }

    /**
     * Sets the value of the targetX property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setTargetX(String value) {
        this.targetX = value;
    }

    /**
     * Gets the value of the targetY property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getTargetY() {
        return targetY;
    }

    /**
     * Sets the value of the targetY property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setTargetY(String value) {
        this.targetY = value;
    }

    /**
     * Gets the value of the edgeMode property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getEdgeMode() {
        if (edgeMode == null) {
            return "duplicate";
        } else {
            return edgeMode;
        }
    }

    /**
     * Sets the value of the edgeMode property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setEdgeMode(String value) {
        this.edgeMode = value;
    }

    /**
     * Gets the value of the kernelUnitLength property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getKernelUnitLength() {
        return kernelUnitLength;
    }

    /**
     * Sets the value of the kernelUnitLength property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setKernelUnitLength(String value) {
        this.kernelUnitLength = value;
    }

    /**
     * Gets the value of the preserveAlpha property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getPreserveAlpha() {
        return preserveAlpha;
    }

    /**
     * Sets the value of the preserveAlpha property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setPreserveAlpha(String value) {
        this.preserveAlpha = value;
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
