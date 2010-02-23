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
 *           &lt;element ref="{http://www.w3.org/2000/svg}animate"/>
 *           &lt;element ref="{http://www.w3.org/2000/svg}set"/>
 *           &lt;element ref="{http://www.w3.org/2000/svg}animateMotion"/>
 *           &lt;element ref="{http://www.w3.org/2000/svg}animateColor"/>
 *           &lt;element ref="{http://www.w3.org/2000/svg}animateTransform"/>
 *         &lt;/choice>
 *       &lt;/sequence>
 *       &lt;attribute name="points" use="required" type="{http://www.w3.org/2001/XMLSchema}anySimpleType" />
 *       &lt;attribute name="transform" type="{http://www.w3.org/2001/XMLSchema}anySimpleType" />
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
    "animateOrSetOrAnimateMotion"
})
@XmlRootElement(name = "polyline")
public class Polyline {

    @XmlElements({
        @XmlElement(name = "desc", type = Desc.class),
        @XmlElement(name = "title", type = Title.class),
        @XmlElement(name = "metadata", type = Metadata.class)
    })
    protected List<Object> descOrTitleOrMetadata;
    @XmlElements({
        @XmlElement(name = "animateColor", type = AnimateColor.class),
        @XmlElement(name = "animate", type = Animate.class),
        @XmlElement(name = "animateMotion", type = AnimateMotion.class),
        @XmlElement(name = "set", type = Set.class),
        @XmlElement(name = "animateTransform", type = AnimateTransform.class)
    })
    protected List<Object> animateOrSetOrAnimateMotion;
    @XmlAttribute(required = true)
    @XmlSchemaType(name = "anySimpleType")
    protected String points;
    @XmlAttribute
    @XmlSchemaType(name = "anySimpleType")
    protected String transform;

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
     * {@link Desc }
     * {@link Title }
     * {@link Metadata }
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
     * Gets the value of the animateOrSetOrAnimateMotion property.
     * 
     * <p>
     * This accessor method returns a reference to the live list,
     * not a snapshot. Therefore any modification you make to the
     * returned list will be present inside the JAXB object.
     * This is why there is not a <CODE>set</CODE> method for the animateOrSetOrAnimateMotion property.
     * 
     * <p>
     * For example, to add a new item, do as follows:
     * <pre>
     *    getAnimateOrSetOrAnimateMotion().add(newItem);
     * </pre>
     * 
     * 
     * <p>
     * Objects of the following type(s) are allowed in the list
     * {@link AnimateColor }
     * {@link Animate }
     * {@link AnimateMotion }
     * {@link Set }
     * {@link AnimateTransform }
     * 
     * 
     */
    public List<Object> getAnimateOrSetOrAnimateMotion() {
        if (animateOrSetOrAnimateMotion == null) {
            animateOrSetOrAnimateMotion = new ArrayList<Object>();
        }
        return this.animateOrSetOrAnimateMotion;
    }

    /**
     * Gets the value of the points property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getPoints() {
        return points;
    }

    /**
     * Sets the value of the points property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setPoints(String value) {
        this.points = value;
    }

    /**
     * Gets the value of the transform property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getTransform() {
        return transform;
    }

    /**
     * Sets the value of the transform property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setTransform(String value) {
        this.transform = value;
    }

}
