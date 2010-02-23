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
 *       &lt;attribute name="cx" type="{http://www.w3.org/2001/XMLSchema}anySimpleType" />
 *       &lt;attribute name="cy" type="{http://www.w3.org/2001/XMLSchema}anySimpleType" />
 *       &lt;attribute name="r" type="{http://www.w3.org/2001/XMLSchema}anySimpleType" />
 *       &lt;attribute name="fx" type="{http://www.w3.org/2001/XMLSchema}anySimpleType" />
 *       &lt;attribute name="fy" type="{http://www.w3.org/2001/XMLSchema}anySimpleType" />
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
@XmlRootElement(name = "radialGradient")
public class RadialGradient {

    @XmlElements({
        @XmlElement(name = "metadata", type = Metadata.class),
        @XmlElement(name = "desc", type = Desc.class),
        @XmlElement(name = "title", type = Title.class)
    })
    protected List<Object> descOrTitleOrMetadata;
    @XmlElements({
        @XmlElement(name = "stop", type = Stop.class),
        @XmlElement(name = "animateTransform", type = AnimateTransform.class),
        @XmlElement(name = "set", type = Set.class),
        @XmlElement(name = "animate", type = Animate.class)
    })
    protected List<Object> stopOrAnimateOrSet;
    @XmlAttribute
    @XmlSchemaType(name = "anySimpleType")
    protected String cx;
    @XmlAttribute
    @XmlSchemaType(name = "anySimpleType")
    protected String cy;
    @XmlAttribute
    @XmlSchemaType(name = "anySimpleType")
    protected String r;
    @XmlAttribute
    @XmlSchemaType(name = "anySimpleType")
    protected String fx;
    @XmlAttribute
    @XmlSchemaType(name = "anySimpleType")
    protected String fy;
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
     * {@link Stop }
     * {@link AnimateTransform }
     * {@link Set }
     * {@link Animate }
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
     * Gets the value of the cx property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getCx() {
        return cx;
    }

    /**
     * Sets the value of the cx property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setCx(String value) {
        this.cx = value;
    }

    /**
     * Gets the value of the cy property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getCy() {
        return cy;
    }

    /**
     * Sets the value of the cy property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setCy(String value) {
        this.cy = value;
    }

    /**
     * Gets the value of the r property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getR() {
        return r;
    }

    /**
     * Sets the value of the r property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setR(String value) {
        this.r = value;
    }

    /**
     * Gets the value of the fx property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getFx() {
        return fx;
    }

    /**
     * Sets the value of the fx property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setFx(String value) {
        this.fx = value;
    }

    /**
     * Gets the value of the fy property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getFy() {
        return fy;
    }

    /**
     * Sets the value of the fy property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setFy(String value) {
        this.fy = value;
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
