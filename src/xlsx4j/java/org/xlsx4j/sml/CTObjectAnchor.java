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
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlTransient;
import javax.xml.bind.annotation.XmlType;
import org.docx4j.dml.spreadsheetdrawing.CTMarker;
import org.jvnet.jaxb2_commons.ppp.Child;


/**
 * <p>Java class for CT_ObjectAnchor complex type.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
 * 
 * <pre>
 * &lt;complexType name="CT_ObjectAnchor">
 *   &lt;complexContent>
 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
 *       &lt;sequence>
 *         &lt;element ref="{http://schemas.openxmlformats.org/drawingml/2006/spreadsheetDrawing}from"/>
 *         &lt;element ref="{http://schemas.openxmlformats.org/drawingml/2006/spreadsheetDrawing}to"/>
 *       &lt;/sequence>
 *       &lt;attribute name="moveWithCells" type="{http://www.w3.org/2001/XMLSchema}boolean" default="false" />
 *       &lt;attribute name="sizeWithCells" type="{http://www.w3.org/2001/XMLSchema}boolean" default="false" />
 *     &lt;/restriction>
 *   &lt;/complexContent>
 * &lt;/complexType>
 * </pre>
 * 
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "CT_ObjectAnchor", propOrder = {
    "from",
    "to"
})
public class CTObjectAnchor implements Child
{

    @XmlElement(namespace = "http://schemas.openxmlformats.org/drawingml/2006/spreadsheetDrawing", required = true)
    protected CTMarker from;
    @XmlElement(namespace = "http://schemas.openxmlformats.org/drawingml/2006/spreadsheetDrawing", required = true)
    protected CTMarker to;
    @XmlAttribute(name = "moveWithCells")
    protected Boolean moveWithCells;
    @XmlAttribute(name = "sizeWithCells")
    protected Boolean sizeWithCells;
    @XmlTransient
    private Object parent;

    /**
     * Gets the value of the from property.
     * 
     * @return
     *     possible object is
     *     {@link CTMarker }
     *     
     */
    public CTMarker getFrom() {
        return from;
    }

    /**
     * Sets the value of the from property.
     * 
     * @param value
     *     allowed object is
     *     {@link CTMarker }
     *     
     */
    public void setFrom(CTMarker value) {
        this.from = value;
    }

    /**
     * Gets the value of the to property.
     * 
     * @return
     *     possible object is
     *     {@link CTMarker }
     *     
     */
    public CTMarker getTo() {
        return to;
    }

    /**
     * Sets the value of the to property.
     * 
     * @param value
     *     allowed object is
     *     {@link CTMarker }
     *     
     */
    public void setTo(CTMarker value) {
        this.to = value;
    }

    /**
     * Gets the value of the moveWithCells property.
     * 
     * @return
     *     possible object is
     *     {@link Boolean }
     *     
     */
    public boolean isMoveWithCells() {
        if (moveWithCells == null) {
            return false;
        } else {
            return moveWithCells;
        }
    }

    /**
     * Sets the value of the moveWithCells property.
     * 
     * @param value
     *     allowed object is
     *     {@link Boolean }
     *     
     */
    public void setMoveWithCells(Boolean value) {
        this.moveWithCells = value;
    }

    /**
     * Gets the value of the sizeWithCells property.
     * 
     * @return
     *     possible object is
     *     {@link Boolean }
     *     
     */
    public boolean isSizeWithCells() {
        if (sizeWithCells == null) {
            return false;
        } else {
            return sizeWithCells;
        }
    }

    /**
     * Sets the value of the sizeWithCells property.
     * 
     * @param value
     *     allowed object is
     *     {@link Boolean }
     *     
     */
    public void setSizeWithCells(Boolean value) {
        this.sizeWithCells = value;
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
