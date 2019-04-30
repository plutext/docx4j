/*
 *  Copyright 2010-2012, Plutext Pty Ltd.
 *   
 *  This file is part of pptx4j, a component of docx4j.

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
package org.pptx4j.pml;

import javax.xml.bind.Unmarshaller;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlTransient;
import javax.xml.bind.annotation.XmlType;
import org.docx4j.dml.CTColor;

import org.jvnet.jaxb2_commons.ppp.Child;


/**
 * <p>Java class for CT_TLAnimVariant complex type.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
 * 
 * <pre>
 * &lt;complexType name="CT_TLAnimVariant"&gt;
 *   &lt;complexContent&gt;
 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType"&gt;
 *       &lt;choice&gt;
 *         &lt;element name="boolVal" type="{http://schemas.openxmlformats.org/presentationml/2006/main}CT_TLAnimVariantBooleanVal"/&gt;
 *         &lt;element name="intVal" type="{http://schemas.openxmlformats.org/presentationml/2006/main}CT_TLAnimVariantIntegerVal"/&gt;
 *         &lt;element name="fltVal" type="{http://schemas.openxmlformats.org/presentationml/2006/main}CT_TLAnimVariantFloatVal"/&gt;
 *         &lt;element name="strVal" type="{http://schemas.openxmlformats.org/presentationml/2006/main}CT_TLAnimVariantStringVal"/&gt;
 *         &lt;element name="clrVal" type="{http://schemas.openxmlformats.org/drawingml/2006/main}CT_Color"/&gt;
 *       &lt;/choice&gt;
 *     &lt;/restriction&gt;
 *   &lt;/complexContent&gt;
 * &lt;/complexType&gt;
 * </pre>
 * 
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "CT_TLAnimVariant", propOrder = {
    "boolVal",
    "intVal",
    "fltVal",
    "strVal",
    "clrVal"
})
public class CTTLAnimVariant implements Child
{

    protected CTTLAnimVariantBooleanVal boolVal;
    protected CTTLAnimVariantIntegerVal intVal;
    protected CTTLAnimVariantFloatVal fltVal;
    protected CTTLAnimVariantStringVal strVal;
    protected CTColor clrVal;
    @XmlTransient
    private Object parent;

    /**
     * Gets the value of the boolVal property.
     * 
     * @return
     *     possible object is
     *     {@link CTTLAnimVariantBooleanVal }
     *     
     */
    public CTTLAnimVariantBooleanVal getBoolVal() {
        return boolVal;
    }

    /**
     * Sets the value of the boolVal property.
     * 
     * @param value
     *     allowed object is
     *     {@link CTTLAnimVariantBooleanVal }
     *     
     */
    public void setBoolVal(CTTLAnimVariantBooleanVal value) {
        this.boolVal = value;
    }

    /**
     * Gets the value of the intVal property.
     * 
     * @return
     *     possible object is
     *     {@link CTTLAnimVariantIntegerVal }
     *     
     */
    public CTTLAnimVariantIntegerVal getIntVal() {
        return intVal;
    }

    /**
     * Sets the value of the intVal property.
     * 
     * @param value
     *     allowed object is
     *     {@link CTTLAnimVariantIntegerVal }
     *     
     */
    public void setIntVal(CTTLAnimVariantIntegerVal value) {
        this.intVal = value;
    }

    /**
     * Gets the value of the fltVal property.
     * 
     * @return
     *     possible object is
     *     {@link CTTLAnimVariantFloatVal }
     *     
     */
    public CTTLAnimVariantFloatVal getFltVal() {
        return fltVal;
    }

    /**
     * Sets the value of the fltVal property.
     * 
     * @param value
     *     allowed object is
     *     {@link CTTLAnimVariantFloatVal }
     *     
     */
    public void setFltVal(CTTLAnimVariantFloatVal value) {
        this.fltVal = value;
    }

    /**
     * Gets the value of the strVal property.
     * 
     * @return
     *     possible object is
     *     {@link CTTLAnimVariantStringVal }
     *     
     */
    public CTTLAnimVariantStringVal getStrVal() {
        return strVal;
    }

    /**
     * Sets the value of the strVal property.
     * 
     * @param value
     *     allowed object is
     *     {@link CTTLAnimVariantStringVal }
     *     
     */
    public void setStrVal(CTTLAnimVariantStringVal value) {
        this.strVal = value;
    }

    /**
     * Gets the value of the clrVal property.
     * 
     * @return
     *     possible object is
     *     {@link CTColor }
     *     
     */
    public CTColor getClrVal() {
        return clrVal;
    }

    /**
     * Sets the value of the clrVal property.
     * 
     * @param value
     *     allowed object is
     *     {@link CTColor }
     *     
     */
    public void setClrVal(CTColor value) {
        this.clrVal = value;
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
