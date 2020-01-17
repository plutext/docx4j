/*
 *  Copyright 2010-2013, Plutext Pty Ltd.
 *   
 *  This file is part of xlsx4j, a component of docx4j.

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
package org.xlsx4j.sml;

import javax.xml.bind.Unmarshaller;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlTransient;
import javax.xml.bind.annotation.XmlType;
import org.jvnet.jaxb2_commons.ppp.Child;


/**
 * <p>Java class for CT_Border complex type.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
 * 
 * <pre>
 * &lt;complexType name="CT_Border">
 *   &lt;complexContent>
 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
 *       &lt;sequence>
 *         &lt;element name="start" type="{http://schemas.openxmlformats.org/spreadsheetml/2006/main}CT_BorderPr" minOccurs="0"/>
 *         &lt;element name="end" type="{http://schemas.openxmlformats.org/spreadsheetml/2006/main}CT_BorderPr" minOccurs="0"/>
 *         &lt;element name="left" type="{http://schemas.openxmlformats.org/spreadsheetml/2006/main}CT_BorderPr" minOccurs="0"/>
 *         &lt;element name="right" type="{http://schemas.openxmlformats.org/spreadsheetml/2006/main}CT_BorderPr" minOccurs="0"/>
 *         &lt;element name="top" type="{http://schemas.openxmlformats.org/spreadsheetml/2006/main}CT_BorderPr" minOccurs="0"/>
 *         &lt;element name="bottom" type="{http://schemas.openxmlformats.org/spreadsheetml/2006/main}CT_BorderPr" minOccurs="0"/>
 *         &lt;element name="diagonal" type="{http://schemas.openxmlformats.org/spreadsheetml/2006/main}CT_BorderPr" minOccurs="0"/>
 *         &lt;element name="vertical" type="{http://schemas.openxmlformats.org/spreadsheetml/2006/main}CT_BorderPr" minOccurs="0"/>
 *         &lt;element name="horizontal" type="{http://schemas.openxmlformats.org/spreadsheetml/2006/main}CT_BorderPr" minOccurs="0"/>
 *       &lt;/sequence>
 *       &lt;attribute name="diagonalUp" type="{http://www.w3.org/2001/XMLSchema}boolean" />
 *       &lt;attribute name="diagonalDown" type="{http://www.w3.org/2001/XMLSchema}boolean" />
 *       &lt;attribute name="outline" type="{http://www.w3.org/2001/XMLSchema}boolean" default="true" />
 *     &lt;/restriction>
 *   &lt;/complexContent>
 * &lt;/complexType>
 * </pre>
 * 
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "CT_Border", propOrder = {
    "start",
    "end",
    "left",
    "right",
    "top",
    "bottom",
    "diagonal",
    "vertical",
    "horizontal"
})
public class CTBorder implements Child
{

    protected CTBorderPr start;
    protected CTBorderPr end;
    protected CTBorderPr left;
    protected CTBorderPr right;
    protected CTBorderPr top;
    protected CTBorderPr bottom;
    protected CTBorderPr diagonal;
    protected CTBorderPr vertical;
    protected CTBorderPr horizontal;
    @XmlAttribute(name = "diagonalUp")
    protected Boolean diagonalUp;
    @XmlAttribute(name = "diagonalDown")
    protected Boolean diagonalDown;
    @XmlAttribute(name = "outline")
    protected Boolean outline;
    @XmlTransient
    private Object parent;

    /**
     * Gets the value of the start property.
     * 
     * @return
     *     possible object is
     *     {@link CTBorderPr }
     *     
     */
    public CTBorderPr getStart() {
        return start;
    }

    /**
     * Sets the value of the start property.
     * 
     * @param value
     *     allowed object is
     *     {@link CTBorderPr }
     *     
     */
    public void setStart(CTBorderPr value) {
        this.start = value;
    }

    /**
     * Gets the value of the end property.
     * 
     * @return
     *     possible object is
     *     {@link CTBorderPr }
     *     
     */
    public CTBorderPr getEnd() {
        return end;
    }

    /**
     * Sets the value of the end property.
     * 
     * @param value
     *     allowed object is
     *     {@link CTBorderPr }
     *     
     */
    public void setEnd(CTBorderPr value) {
        this.end = value;
    }

    /**
     * Gets the value of the left property.
     * 
     * @return
     *     possible object is
     *     {@link CTBorderPr }
     *     
     */
    public CTBorderPr getLeft() {
        return left;
    }

    /**
     * Sets the value of the left property.
     * 
     * @param value
     *     allowed object is
     *     {@link CTBorderPr }
     *     
     */
    public void setLeft(CTBorderPr value) {
        this.left = value;
    }

    /**
     * Gets the value of the right property.
     * 
     * @return
     *     possible object is
     *     {@link CTBorderPr }
     *     
     */
    public CTBorderPr getRight() {
        return right;
    }

    /**
     * Sets the value of the right property.
     * 
     * @param value
     *     allowed object is
     *     {@link CTBorderPr }
     *     
     */
    public void setRight(CTBorderPr value) {
        this.right = value;
    }

    /**
     * Gets the value of the top property.
     * 
     * @return
     *     possible object is
     *     {@link CTBorderPr }
     *     
     */
    public CTBorderPr getTop() {
        return top;
    }

    /**
     * Sets the value of the top property.
     * 
     * @param value
     *     allowed object is
     *     {@link CTBorderPr }
     *     
     */
    public void setTop(CTBorderPr value) {
        this.top = value;
    }

    /**
     * Gets the value of the bottom property.
     * 
     * @return
     *     possible object is
     *     {@link CTBorderPr }
     *     
     */
    public CTBorderPr getBottom() {
        return bottom;
    }

    /**
     * Sets the value of the bottom property.
     * 
     * @param value
     *     allowed object is
     *     {@link CTBorderPr }
     *     
     */
    public void setBottom(CTBorderPr value) {
        this.bottom = value;
    }

    /**
     * Gets the value of the diagonal property.
     * 
     * @return
     *     possible object is
     *     {@link CTBorderPr }
     *     
     */
    public CTBorderPr getDiagonal() {
        return diagonal;
    }

    /**
     * Sets the value of the diagonal property.
     * 
     * @param value
     *     allowed object is
     *     {@link CTBorderPr }
     *     
     */
    public void setDiagonal(CTBorderPr value) {
        this.diagonal = value;
    }

    /**
     * Gets the value of the vertical property.
     * 
     * @return
     *     possible object is
     *     {@link CTBorderPr }
     *     
     */
    public CTBorderPr getVertical() {
        return vertical;
    }

    /**
     * Sets the value of the vertical property.
     * 
     * @param value
     *     allowed object is
     *     {@link CTBorderPr }
     *     
     */
    public void setVertical(CTBorderPr value) {
        this.vertical = value;
    }

    /**
     * Gets the value of the horizontal property.
     * 
     * @return
     *     possible object is
     *     {@link CTBorderPr }
     *     
     */
    public CTBorderPr getHorizontal() {
        return horizontal;
    }

    /**
     * Sets the value of the horizontal property.
     * 
     * @param value
     *     allowed object is
     *     {@link CTBorderPr }
     *     
     */
    public void setHorizontal(CTBorderPr value) {
        this.horizontal = value;
    }

    /**
     * Gets the value of the diagonalUp property.
     * 
     * @return
     *     possible object is
     *     {@link Boolean }
     *     
     */
    public Boolean isDiagonalUp() {
        return diagonalUp;
    }

    /**
     * Sets the value of the diagonalUp property.
     * 
     * @param value
     *     allowed object is
     *     {@link Boolean }
     *     
     */
    public void setDiagonalUp(Boolean value) {
        this.diagonalUp = value;
    }

    /**
     * Gets the value of the diagonalDown property.
     * 
     * @return
     *     possible object is
     *     {@link Boolean }
     *     
     */
    public Boolean isDiagonalDown() {
        return diagonalDown;
    }

    /**
     * Sets the value of the diagonalDown property.
     * 
     * @param value
     *     allowed object is
     *     {@link Boolean }
     *     
     */
    public void setDiagonalDown(Boolean value) {
        this.diagonalDown = value;
    }

    /**
     * Gets the value of the outline property.
     * 
     * @return
     *     possible object is
     *     {@link Boolean }
     *     
     */
    public boolean isOutline() {
        if (outline == null) {
            return true;
        } else {
            return outline;
        }
    }

    /**
     * Sets the value of the outline property.
     * 
     * @param value
     *     allowed object is
     *     {@link Boolean }
     *     
     */
    public void setOutline(Boolean value) {
        this.outline = value;
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
