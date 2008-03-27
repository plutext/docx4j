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
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlType;


/**
 * <p>Java class for CT_ShapeStyle complex type.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
 * 
 * <pre>
 * &lt;complexType name="CT_ShapeStyle">
 *   &lt;complexContent>
 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
 *       &lt;sequence>
 *         &lt;element name="lnRef" type="{http://schemas.openxmlformats.org/drawingml/2006/main}CT_StyleMatrixReference"/>
 *         &lt;element name="fillRef" type="{http://schemas.openxmlformats.org/drawingml/2006/main}CT_StyleMatrixReference"/>
 *         &lt;element name="effectRef" type="{http://schemas.openxmlformats.org/drawingml/2006/main}CT_StyleMatrixReference"/>
 *         &lt;element name="fontRef" type="{http://schemas.openxmlformats.org/drawingml/2006/main}CT_FontReference"/>
 *       &lt;/sequence>
 *     &lt;/restriction>
 *   &lt;/complexContent>
 * &lt;/complexType>
 * </pre>
 * 
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "CT_ShapeStyle", propOrder = {
    "lnRef",
    "fillRef",
    "effectRef",
    "fontRef"
})
public class CTShapeStyle {

    @XmlElement(required = true)
    protected CTStyleMatrixReference lnRef;
    @XmlElement(required = true)
    protected CTStyleMatrixReference fillRef;
    @XmlElement(required = true)
    protected CTStyleMatrixReference effectRef;
    @XmlElement(required = true)
    protected CTFontReference fontRef;

    /**
     * Gets the value of the lnRef property.
     * 
     * @return
     *     possible object is
     *     {@link CTStyleMatrixReference }
     *     
     */
    public CTStyleMatrixReference getLnRef() {
        return lnRef;
    }

    /**
     * Sets the value of the lnRef property.
     * 
     * @param value
     *     allowed object is
     *     {@link CTStyleMatrixReference }
     *     
     */
    public void setLnRef(CTStyleMatrixReference value) {
        this.lnRef = value;
    }

    /**
     * Gets the value of the fillRef property.
     * 
     * @return
     *     possible object is
     *     {@link CTStyleMatrixReference }
     *     
     */
    public CTStyleMatrixReference getFillRef() {
        return fillRef;
    }

    /**
     * Sets the value of the fillRef property.
     * 
     * @param value
     *     allowed object is
     *     {@link CTStyleMatrixReference }
     *     
     */
    public void setFillRef(CTStyleMatrixReference value) {
        this.fillRef = value;
    }

    /**
     * Gets the value of the effectRef property.
     * 
     * @return
     *     possible object is
     *     {@link CTStyleMatrixReference }
     *     
     */
    public CTStyleMatrixReference getEffectRef() {
        return effectRef;
    }

    /**
     * Sets the value of the effectRef property.
     * 
     * @param value
     *     allowed object is
     *     {@link CTStyleMatrixReference }
     *     
     */
    public void setEffectRef(CTStyleMatrixReference value) {
        this.effectRef = value;
    }

    /**
     * Gets the value of the fontRef property.
     * 
     * @return
     *     possible object is
     *     {@link CTFontReference }
     *     
     */
    public CTFontReference getFontRef() {
        return fontRef;
    }

    /**
     * Sets the value of the fontRef property.
     * 
     * @param value
     *     allowed object is
     *     {@link CTFontReference }
     *     
     */
    public void setFontRef(CTFontReference value) {
        this.fontRef = value;
    }

}
