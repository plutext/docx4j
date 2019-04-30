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
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlTransient;
import javax.xml.bind.annotation.XmlType;
import org.jvnet.jaxb2_commons.ppp.Child;


/**
 * <p>Java class for CT_ShapeStyle complex type.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
 * 
 * <pre>
 * &lt;complexType name="CT_ShapeStyle"&gt;
 *   &lt;complexContent&gt;
 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType"&gt;
 *       &lt;sequence&gt;
 *         &lt;element name="lnRef" type="{http://schemas.openxmlformats.org/drawingml/2006/main}CT_StyleMatrixReference"/&gt;
 *         &lt;element name="fillRef" type="{http://schemas.openxmlformats.org/drawingml/2006/main}CT_StyleMatrixReference"/&gt;
 *         &lt;element name="effectRef" type="{http://schemas.openxmlformats.org/drawingml/2006/main}CT_StyleMatrixReference"/&gt;
 *         &lt;element name="fontRef" type="{http://schemas.openxmlformats.org/drawingml/2006/main}CT_FontReference"/&gt;
 *       &lt;/sequence&gt;
 *     &lt;/restriction&gt;
 *   &lt;/complexContent&gt;
 * &lt;/complexType&gt;
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
public class CTShapeStyle implements Child
{

    @XmlElement(required = true)
    protected CTStyleMatrixReference lnRef;
    @XmlElement(required = true)
    protected CTStyleMatrixReference fillRef;
    @XmlElement(required = true)
    protected CTStyleMatrixReference effectRef;
    @XmlElement(required = true)
    protected CTFontReference fontRef;
    @XmlTransient
    private Object parent;

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
