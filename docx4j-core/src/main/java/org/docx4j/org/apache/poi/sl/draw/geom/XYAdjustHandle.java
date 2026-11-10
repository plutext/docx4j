/* NOTICE: This file has been changed by Plutext Pty Ltd for use in docx4j.
 * The package name has been changed; there may also be other changes.
 *
 * This notice is included to meet the condition in clause 4(b) of the License.
 */

/* ====================================================================
   Licensed to the Apache Software Foundation (ASF) under one or more
   contributor license agreements.  See the NOTICE file distributed with
   this work for additional information regarding copyright ownership.
   The ASF licenses this file to You under the Apache License, Version 2.0
   (the "License"); you may not use this file except in compliance with
   the License.  You may obtain a copy of the License at

       http://www.apache.org/licenses/LICENSE-2.0

   Unless required by applicable law or agreed to in writing, software
   distributed under the License is distributed on an "AS IS" BASIS,
   WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
   See the License for the specific language governing permissions and
   limitations under the License.
==================================================================== */

package org.docx4j.org.apache.poi.sl.draw.geom;

import java.util.Objects;

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
// @XmlAccessorType(XmlAccessType.FIELD)
// @XmlType(name = "CT_XYAdjustHandle", propOrder = {"pos"})
public final class XYAdjustHandle implements AdjustHandle {

    // @XmlElement(required = true)
    private AdjustPoint pos;
    // @XmlAttribute(name = "gdRefX")
    // @XmlJavaTypeAdapter(CollapsedStringAdapter.class)
    private String gdRefX;
    // @XmlAttribute(name = "minX")
    private String minX;
    // @XmlAttribute(name = "maxX")
    private String maxX;
    // @XmlAttribute(name = "gdRefY")
    // @XmlJavaTypeAdapter(CollapsedStringAdapter.class)
    private String gdRefY;
    // @XmlAttribute(name = "minY")
    private String minY;
    // @XmlAttribute(name = "maxY")
    private String maxY;

    /**
     * Gets the value of the pos property.
     */
    public AdjustPoint getPos() {
        return pos;
    }

    /**
     * Sets the value of the pos property.
     */
    public void setPos(AdjustPoint value) {
        this.pos = value;
    }

    public boolean isSetPos() {
        return (this.pos!= null);
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

    public boolean isSetGdRefX() {
        return (this.gdRefX!= null);
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

    public boolean isSetMinX() {
        return (this.minX!= null);
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

    public boolean isSetMaxX() {
        return (this.maxX!= null);
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

    public boolean isSetGdRefY() {
        return (this.gdRefY!= null);
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

    public boolean isSetMinY() {
        return (this.minY!= null);
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

    public boolean isSetMaxY() {
        return (this.maxY!= null);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof XYAdjustHandle)) return false;
        XYAdjustHandle that = (XYAdjustHandle) o;
        return Objects.equals(pos, that.pos) &&
                Objects.equals(gdRefX, that.gdRefX) &&
                Objects.equals(minX, that.minX) &&
                Objects.equals(maxX, that.maxX) &&
                Objects.equals(gdRefY, that.gdRefY) &&
                Objects.equals(minY, that.minY) &&
                Objects.equals(maxY, that.maxY);
    }

    @Override
    public int hashCode() {
        return Objects.hash(pos, gdRefX, minX, maxX, gdRefY, minY, maxY);
    }
}
