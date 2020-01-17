/*
 *  Copyright 2007-2008, Plutext Pty Ltd.
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


package org.docx4j.dml;

import javax.xml.bind.Unmarshaller;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlTransient;
import javax.xml.bind.annotation.XmlType;
import javax.xml.bind.annotation.adapters.CollapsedStringAdapter;
import javax.xml.bind.annotation.adapters.XmlJavaTypeAdapter;
import org.jvnet.jaxb2_commons.ppp.Child;


/**
 * <p>Java class for CT_XYAdjustHandle complex type.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
 * 
 * <pre>
 * &lt;complexType name="CT_XYAdjustHandle"&gt;
 *   &lt;complexContent&gt;
 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType"&gt;
 *       &lt;sequence&gt;
 *         &lt;element name="pos" type="{http://schemas.openxmlformats.org/drawingml/2006/main}CT_AdjPoint2D"/&gt;
 *       &lt;/sequence&gt;
 *       &lt;attribute name="gdRefX" type="{http://schemas.openxmlformats.org/drawingml/2006/main}ST_GeomGuideName" /&gt;
 *       &lt;attribute name="minX" type="{http://schemas.openxmlformats.org/drawingml/2006/main}ST_AdjCoordinate" /&gt;
 *       &lt;attribute name="maxX" type="{http://schemas.openxmlformats.org/drawingml/2006/main}ST_AdjCoordinate" /&gt;
 *       &lt;attribute name="gdRefY" type="{http://schemas.openxmlformats.org/drawingml/2006/main}ST_GeomGuideName" /&gt;
 *       &lt;attribute name="minY" type="{http://schemas.openxmlformats.org/drawingml/2006/main}ST_AdjCoordinate" /&gt;
 *       &lt;attribute name="maxY" type="{http://schemas.openxmlformats.org/drawingml/2006/main}ST_AdjCoordinate" /&gt;
 *     &lt;/restriction&gt;
 *   &lt;/complexContent&gt;
 * &lt;/complexType&gt;
 * </pre>
 * 
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "CT_XYAdjustHandle", propOrder = {
    "pos"
})
public class CTXYAdjustHandle implements Child
{

    @XmlElement(required = true)
    protected CTAdjPoint2D pos;
    @XmlAttribute(name = "gdRefX")
    @XmlJavaTypeAdapter(CollapsedStringAdapter.class)
    protected String gdRefX;
    @XmlAttribute(name = "minX")
    protected String minX;
    @XmlAttribute(name = "maxX")
    protected String maxX;
    @XmlAttribute(name = "gdRefY")
    @XmlJavaTypeAdapter(CollapsedStringAdapter.class)
    protected String gdRefY;
    @XmlAttribute(name = "minY")
    protected String minY;
    @XmlAttribute(name = "maxY")
    protected String maxY;
    @XmlTransient
    private Object parent;

    /**
     * Gets the value of the pos property.
     * 
     * @return
     *     possible object is
     *     {@link CTAdjPoint2D }
     *     
     */
    public CTAdjPoint2D getPos() {
        return pos;
    }

    /**
     * Sets the value of the pos property.
     * 
     * @param value
     *     allowed object is
     *     {@link CTAdjPoint2D }
     *     
     */
    public void setPos(CTAdjPoint2D value) {
        this.pos = value;
    }

    /**
     * Gets the value of the gdRefX property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getGdRefX() {
        return gdRefX;
    }

    /**
     * Sets the value of the gdRefX property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setGdRefX(String value) {
        this.gdRefX = value;
    }

    /**
     * Gets the value of the minX property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getMinX() {
        return minX;
    }

    /**
     * Sets the value of the minX property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setMinX(String value) {
        this.minX = value;
    }

    /**
     * Gets the value of the maxX property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getMaxX() {
        return maxX;
    }

    /**
     * Sets the value of the maxX property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setMaxX(String value) {
        this.maxX = value;
    }

    /**
     * Gets the value of the gdRefY property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getGdRefY() {
        return gdRefY;
    }

    /**
     * Sets the value of the gdRefY property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setGdRefY(String value) {
        this.gdRefY = value;
    }

    /**
     * Gets the value of the minY property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getMinY() {
        return minY;
    }

    /**
     * Sets the value of the minY property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setMinY(String value) {
        this.minY = value;
    }

    /**
     * Gets the value of the maxY property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getMaxY() {
        return maxY;
    }

    /**
     * Sets the value of the maxY property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setMaxY(String value) {
        this.maxY = value;
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
