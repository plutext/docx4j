/*
 *  Copyright 2007, Plutext Pty Ltd.
 *   
 *  This file is part of docx4j.

    docx4j is free software: you can use it, redistribute it and/or modify
    it under the terms of version 3 of the GNU Affero General Public License 
    as published by the Free Software Foundation.

    If you need the right to use it under a different license, please
    contact Plutext.

    docx4j is distributed in the hope that it will be useful,
    but WITHOUT ANY WARRANTY; without even the implied warranty of
    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
    GNU Affero General Public License for more details.

    You should have received a copy of the GNU Affero General Public License   
    along with docx4j.  If not, see <http://www.fsf.org/licensing/licenses/>.
    
 */

package org.docx4j.dml;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlType;
import javax.xml.bind.annotation.adapters.CollapsedStringAdapter;
import javax.xml.bind.annotation.adapters.XmlJavaTypeAdapter;


/**
 * <p>Java class for CT_XYAdjustHandle complex type.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
 * 
 * <pre>
 * &lt;complexType name="CT_XYAdjustHandle">
 *   &lt;complexContent>
 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
 *       &lt;sequence>
 *         &lt;element name="pos" type="{http://schemas.openxmlformats.org/drawingml/2006/main}CT_AdjPoint2D"/>
 *       &lt;/sequence>
 *       &lt;attribute name="gdRefX" type="{http://schemas.openxmlformats.org/drawingml/2006/main}ST_GeomGuideName" />
 *       &lt;attribute name="minX" type="{http://schemas.openxmlformats.org/drawingml/2006/main}ST_AdjCoordinate" />
 *       &lt;attribute name="maxX" type="{http://schemas.openxmlformats.org/drawingml/2006/main}ST_AdjCoordinate" />
 *       &lt;attribute name="gdRefY" type="{http://schemas.openxmlformats.org/drawingml/2006/main}ST_GeomGuideName" />
 *       &lt;attribute name="minY" type="{http://schemas.openxmlformats.org/drawingml/2006/main}ST_AdjCoordinate" />
 *       &lt;attribute name="maxY" type="{http://schemas.openxmlformats.org/drawingml/2006/main}ST_AdjCoordinate" />
 *     &lt;/restriction>
 *   &lt;/complexContent>
 * &lt;/complexType>
 * </pre>
 * 
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "CT_XYAdjustHandle", propOrder = {
    "pos"
})
public class CTXYAdjustHandle {

    @XmlElement(required = true)
    protected CTAdjPoint2D pos;
    @XmlAttribute
    @XmlJavaTypeAdapter(CollapsedStringAdapter.class)
    protected String gdRefX;
    @XmlAttribute
    protected String minX;
    @XmlAttribute
    protected String maxX;
    @XmlAttribute
    @XmlJavaTypeAdapter(CollapsedStringAdapter.class)
    protected String gdRefY;
    @XmlAttribute
    protected String minY;
    @XmlAttribute
    protected String maxY;

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

}
