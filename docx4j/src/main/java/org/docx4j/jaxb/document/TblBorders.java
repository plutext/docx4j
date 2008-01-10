/*
 *  Copyright 2007, Plutext Pty Ltd.
 *   
 *  This file is part of docx4j.

    docx4j is free software: you can use it, redistribute it and/or modify
    it under the terms of version 3 of the GNU Affero General Public License 
    as published by the Free Software Foundation.

    docx4j is distributed in the hope that it will be useful,
    but WITHOUT ANY WARRANTY; without even the implied warranty of
    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
    GNU Affero General Public License for more details.

    You should have received a copy of the GNU Affero General Public License   
    along with docx4j.  If not, see <http://www.fsf.org/licensing/licenses/>.
    
 */

package org.docx4j.jaxb.document;

import javax.xml.bind.Unmarshaller;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlTransient;
import javax.xml.bind.annotation.XmlType;
import org.jvnet.jaxb2_commons.ppp.Child;


/**
 * <p>Java class for CT_TblBorders complex type.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
 * 
 * <pre>
 * &lt;complexType name="CT_TblBorders">
 *   &lt;complexContent>
 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
 *       &lt;sequence>
 *         &lt;element ref="{http://schemas.openxmlformats.org/wordprocessingml/2006/main}top" minOccurs="0"/>
 *         &lt;element ref="{http://schemas.openxmlformats.org/wordprocessingml/2006/main}left" minOccurs="0"/>
 *         &lt;element ref="{http://schemas.openxmlformats.org/wordprocessingml/2006/main}bottom" minOccurs="0"/>
 *         &lt;element ref="{http://schemas.openxmlformats.org/wordprocessingml/2006/main}right" minOccurs="0"/>
 *         &lt;element ref="{http://schemas.openxmlformats.org/wordprocessingml/2006/main}insideH" minOccurs="0"/>
 *         &lt;element ref="{http://schemas.openxmlformats.org/wordprocessingml/2006/main}insideV" minOccurs="0"/>
 *       &lt;/sequence>
 *     &lt;/restriction>
 *   &lt;/complexContent>
 * &lt;/complexType>
 * </pre>
 * 
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "CT_TblBorders", propOrder = {
    "top",
    "left",
    "bottom",
    "right",
    "insideH",
    "insideV"
})
public class TblBorders
    implements Child
{

    protected Top top;
    protected Left left;
    protected Bottom bottom;
    protected Right right;
    protected InsideH insideH;
    protected InsideV insideV;
    @XmlTransient
    private Object parent;

    /**
     * Gets the value of the top property.
     * 
     * @return
     *     possible object is
     *     {@link Top }
     *     
     */
    public Top getTop() {
        return top;
    }

    /**
     * Sets the value of the top property.
     * 
     * @param value
     *     allowed object is
     *     {@link Top }
     *     
     */
    public void setTop(Top value) {
        this.top = value;
    }

    /**
     * Gets the value of the left property.
     * 
     * @return
     *     possible object is
     *     {@link Left }
     *     
     */
    public Left getLeft() {
        return left;
    }

    /**
     * Sets the value of the left property.
     * 
     * @param value
     *     allowed object is
     *     {@link Left }
     *     
     */
    public void setLeft(Left value) {
        this.left = value;
    }

    /**
     * Gets the value of the bottom property.
     * 
     * @return
     *     possible object is
     *     {@link Bottom }
     *     
     */
    public Bottom getBottom() {
        return bottom;
    }

    /**
     * Sets the value of the bottom property.
     * 
     * @param value
     *     allowed object is
     *     {@link Bottom }
     *     
     */
    public void setBottom(Bottom value) {
        this.bottom = value;
    }

    /**
     * Gets the value of the right property.
     * 
     * @return
     *     possible object is
     *     {@link Right }
     *     
     */
    public Right getRight() {
        return right;
    }

    /**
     * Sets the value of the right property.
     * 
     * @param value
     *     allowed object is
     *     {@link Right }
     *     
     */
    public void setRight(Right value) {
        this.right = value;
    }

    /**
     * Gets the value of the insideH property.
     * 
     * @return
     *     possible object is
     *     {@link InsideH }
     *     
     */
    public InsideH getInsideH() {
        return insideH;
    }

    /**
     * Sets the value of the insideH property.
     * 
     * @param value
     *     allowed object is
     *     {@link InsideH }
     *     
     */
    public void setInsideH(InsideH value) {
        this.insideH = value;
    }

    /**
     * Gets the value of the insideV property.
     * 
     * @return
     *     possible object is
     *     {@link InsideV }
     *     
     */
    public InsideV getInsideV() {
        return insideV;
    }

    /**
     * Sets the value of the insideV property.
     * 
     * @param value
     *     allowed object is
     *     {@link InsideV }
     *     
     */
    public void setInsideV(InsideV value) {
        this.insideV = value;
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
