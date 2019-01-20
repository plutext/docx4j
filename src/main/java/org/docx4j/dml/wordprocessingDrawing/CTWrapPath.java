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


package org.docx4j.dml.wordprocessingDrawing;

import org.docx4j.dml.ArrayListDml;
import java.util.List;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlType;
import org.docx4j.dml.CTPoint2D;


/**
 * <p>Java class for CT_WrapPath complex type.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
 * 
 * <pre>
 * &lt;complexType name="CT_WrapPath">
 *   &lt;complexContent>
 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
 *       &lt;sequence>
 *         &lt;element name="start" type="{http://schemas.openxmlformats.org/drawingml/2006/main}CT_Point2D"/>
 *         &lt;element name="lineTo" type="{http://schemas.openxmlformats.org/drawingml/2006/main}CT_Point2D" maxOccurs="unbounded" minOccurs="2"/>
 *       &lt;/sequence>
 *       &lt;attribute name="edited" type="{http://www.w3.org/2001/XMLSchema}boolean" />
 *     &lt;/restriction>
 *   &lt;/complexContent>
 * &lt;/complexType>
 * </pre>
 * 
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "CT_WrapPath", propOrder = {
    "start",
    "lineTo"
})
public class CTWrapPath {

    @XmlElement(required = true)
    protected CTPoint2D start;
    @XmlElement(required = true)
    protected List<CTPoint2D> lineTo = new ArrayListDml<CTPoint2D>(this);
    @XmlAttribute
    protected Boolean edited;

    /**
     * Gets the value of the start property.
     * 
     * @return
     *     possible object is
     *     {@link CTPoint2D }
     *     
     */
    public CTPoint2D getStart() {
        return start;
    }

    /**
     * Sets the value of the start property.
     * 
     * @param value
     *     allowed object is
     *     {@link CTPoint2D }
     *     
     */
    public void setStart(CTPoint2D value) {
        this.start = value;
    }

    /**
     * Gets the value of the lineTo property.
     * 
     * <p>
     * This accessor method returns a reference to the live list,
     * not a snapshot. Therefore any modification you make to the
     * returned list will be present inside the JAXB object.
     * This is why there is not a <CODE>set</CODE> method for the lineTo property.
     * 
     * <p>
     * For example, to add a new item, do as follows:
     * <pre>
     *    getLineTo().add(newItem);
     * </pre>
     * 
     * 
     * <p>
     * Objects of the following type(s) are allowed in the list
     * {@link CTPoint2D }
     * 
     * 
     */
    public List<CTPoint2D> getLineTo() {
        if (lineTo == null) {
            lineTo = new ArrayListDml<CTPoint2D>(this);
        }
        return this.lineTo;
    }

    /**
     * Gets the value of the edited property.
     * 
     * @return
     *     possible object is
     *     {@link Boolean }
     *     
     */
    public Boolean isEdited() {
        return edited;
    }

    /**
     * Sets the value of the edited property.
     * 
     * @param value
     *     allowed object is
     *     {@link Boolean }
     *     
     */
    public void setEdited(Boolean value) {
        this.edited = value;
    }

}
