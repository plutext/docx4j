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

import org.docx4j.dml.ArrayListDml;
import java.util.List;
import javax.xml.bind.Unmarshaller;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlElements;
import javax.xml.bind.annotation.XmlTransient;
import javax.xml.bind.annotation.XmlType;
import org.jvnet.jaxb2_commons.ppp.Child;


/**
 * <p>Java class for CT_Path2D complex type.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
 * 
 * <pre>
 * &lt;complexType name="CT_Path2D"&gt;
 *   &lt;complexContent&gt;
 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType"&gt;
 *       &lt;choice maxOccurs="unbounded" minOccurs="0"&gt;
 *         &lt;element name="close" type="{http://schemas.openxmlformats.org/drawingml/2006/main}CT_Path2DClose"/&gt;
 *         &lt;element name="moveTo" type="{http://schemas.openxmlformats.org/drawingml/2006/main}CT_Path2DMoveTo"/&gt;
 *         &lt;element name="lnTo" type="{http://schemas.openxmlformats.org/drawingml/2006/main}CT_Path2DLineTo"/&gt;
 *         &lt;element name="arcTo" type="{http://schemas.openxmlformats.org/drawingml/2006/main}CT_Path2DArcTo"/&gt;
 *         &lt;element name="quadBezTo" type="{http://schemas.openxmlformats.org/drawingml/2006/main}CT_Path2DQuadBezierTo"/&gt;
 *         &lt;element name="cubicBezTo" type="{http://schemas.openxmlformats.org/drawingml/2006/main}CT_Path2DCubicBezierTo"/&gt;
 *       &lt;/choice&gt;
 *       &lt;attribute name="w" type="{http://schemas.openxmlformats.org/drawingml/2006/main}ST_PositiveCoordinate" default="0" /&gt;
 *       &lt;attribute name="h" type="{http://schemas.openxmlformats.org/drawingml/2006/main}ST_PositiveCoordinate" default="0" /&gt;
 *       &lt;attribute name="fill" type="{http://schemas.openxmlformats.org/drawingml/2006/main}ST_PathFillMode" default="norm" /&gt;
 *       &lt;attribute name="stroke" type="{http://www.w3.org/2001/XMLSchema}boolean" default="true" /&gt;
 *       &lt;attribute name="extrusionOk" type="{http://www.w3.org/2001/XMLSchema}boolean" default="true" /&gt;
 *     &lt;/restriction&gt;
 *   &lt;/complexContent&gt;
 * &lt;/complexType&gt;
 * </pre>
 * 
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "CT_Path2D", propOrder = {
    "closeOrMoveToOrLnTo"
})
public class CTPath2D implements Child
{

    @XmlElements({
        @XmlElement(name = "close", type = CTPath2DClose.class),
        @XmlElement(name = "moveTo", type = CTPath2DMoveTo.class),
        @XmlElement(name = "lnTo", type = CTPath2DLineTo.class),
        @XmlElement(name = "arcTo", type = CTPath2DArcTo.class),
        @XmlElement(name = "quadBezTo", type = CTPath2DQuadBezierTo.class),
        @XmlElement(name = "cubicBezTo", type = CTPath2DCubicBezierTo.class)
    })
    protected List<Object> closeOrMoveToOrLnTo = new ArrayListDml<Object>(this);

    @XmlAttribute(name = "w")
    protected Long w;
    @XmlAttribute(name = "h")
    protected Long h;
    @XmlAttribute(name = "fill")
    protected STPathFillMode fill;
    @XmlAttribute(name = "stroke")
    protected Boolean stroke;
    @XmlAttribute(name = "extrusionOk")
    protected Boolean extrusionOk;
    @XmlTransient
    private Object parent;

    /**
     * Gets the value of the closeOrMoveToOrLnTo property.
     * 
     * <p>
     * This accessor method returns a reference to the live list,
     * not a snapshot. Therefore any modification you make to the
     * returned list will be present inside the JAXB object.
     * This is why there is not a <CODE>set</CODE> method for the closeOrMoveToOrLnTo property.
     * 
     * <p>
     * For example, to add a new item, do as follows:
     * <pre>
     *    getCloseOrMoveToOrLnTo().add(newItem);
     * </pre>
     * 
     * 
     * <p>
     * Objects of the following type(s) are allowed in the list
     * {@link CTPath2DClose }
     * {@link CTPath2DMoveTo }
     * {@link CTPath2DLineTo }
     * {@link CTPath2DArcTo }
     * {@link CTPath2DQuadBezierTo }
     * {@link CTPath2DCubicBezierTo }
     * 
     * 
     */
    public List<Object> getCloseOrMoveToOrLnTo() {
        if (closeOrMoveToOrLnTo == null) {
            closeOrMoveToOrLnTo = new ArrayListDml<Object>(this);
        }
        return this.closeOrMoveToOrLnTo;
    }

    /**
     * Gets the value of the w property.
     * 
     * @return
     *     possible object is
     *     {@link Long }
     *     
     */
    public long getW() {
        if (w == null) {
            return  0L;
        } else {
            return w;
        }
    }

    /**
     * Sets the value of the w property.
     * 
     * @param value
     *     allowed object is
     *     {@link Long }
     *     
     */
    public void setW(Long value) {
        this.w = value;
    }

    /**
     * Gets the value of the h property.
     * 
     * @return
     *     possible object is
     *     {@link Long }
     *     
     */
    public long getH() {
        if (h == null) {
            return  0L;
        } else {
            return h;
        }
    }

    /**
     * Sets the value of the h property.
     * 
     * @param value
     *     allowed object is
     *     {@link Long }
     *     
     */
    public void setH(Long value) {
        this.h = value;
    }

    /**
     * Gets the value of the fill property.
     * 
     * @return
     *     possible object is
     *     {@link STPathFillMode }
     *     
     */
    public STPathFillMode getFill() {
        if (fill == null) {
            return STPathFillMode.NORM;
        } else {
            return fill;
        }
    }

    /**
     * Sets the value of the fill property.
     * 
     * @param value
     *     allowed object is
     *     {@link STPathFillMode }
     *     
     */
    public void setFill(STPathFillMode value) {
        this.fill = value;
    }

    /**
     * Gets the value of the stroke property.
     * 
     * @return
     *     possible object is
     *     {@link Boolean }
     *     
     */
    public boolean isStroke() {
        if (stroke == null) {
            return true;
        } else {
            return stroke;
        }
    }

    /**
     * Sets the value of the stroke property.
     * 
     * @param value
     *     allowed object is
     *     {@link Boolean }
     *     
     */
    public void setStroke(Boolean value) {
        this.stroke = value;
    }

    /**
     * Gets the value of the extrusionOk property.
     * 
     * @return
     *     possible object is
     *     {@link Boolean }
     *     
     */
    public boolean isExtrusionOk() {
        if (extrusionOk == null) {
            return true;
        } else {
            return extrusionOk;
        }
    }

    /**
     * Sets the value of the extrusionOk property.
     * 
     * @param value
     *     allowed object is
     *     {@link Boolean }
     *     
     */
    public void setExtrusionOk(Boolean value) {
        this.extrusionOk = value;
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
