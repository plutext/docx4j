/*
 *  Copyright 2009-2012, Plutext Pty Ltd.
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
package org.docx4j.math;

import javax.xml.bind.Unmarshaller;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlTransient;
import javax.xml.bind.annotation.XmlType;
import org.docx4j.wml.CTRPrChange;
import org.docx4j.wml.RPr;
import org.jvnet.jaxb2_commons.ppp.Child;


/**
 * <p>Java class for CT_CtrlPr complex type.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
 * 
 * <pre>
 * &lt;complexType name="CT_CtrlPr">
 *   &lt;complexContent>
 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
 *       &lt;sequence>
 *         &lt;group ref="{http://schemas.openxmlformats.org/wordprocessingml/2006/main}EG_RPrMath" minOccurs="0"/>
 *       &lt;/sequence>
 *     &lt;/restriction>
 *   &lt;/complexContent>
 * &lt;/complexType>
 * </pre>
 * 
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "CT_CtrlPr", propOrder = {
    "rPr",
    "ins",
    "del"
})
public class CTCtrlPr
    implements Child
{

    @XmlElement(namespace = "http://schemas.openxmlformats.org/wordprocessingml/2006/main")
    protected RPr rPr;
    @XmlElement(namespace = "http://schemas.openxmlformats.org/wordprocessingml/2006/main")
    protected CTRPrChange ins;
    @XmlElement(namespace = "http://schemas.openxmlformats.org/wordprocessingml/2006/main")
    protected CTRPrChange del;
    @XmlTransient
    private Object parent;

    /**
     * Gets the value of the rPr property.
     * 
     * @return
     *     possible object is
     *     {@link RPr }
     *     
     */
    public RPr getRPr() {
        return rPr;
    }

    /**
     * Sets the value of the rPr property.
     * 
     * @param value
     *     allowed object is
     *     {@link RPr }
     *     
     */
    public void setRPr(RPr value) {
        this.rPr = value;
    }

    /**
     * Gets the value of the ins property.
     * 
     * @return
     *     possible object is
     *     {@link CTRPrChange }
     *     
     */
    public CTRPrChange getIns() {
        return ins;
    }

    /**
     * Sets the value of the ins property.
     * 
     * @param value
     *     allowed object is
     *     {@link CTRPrChange }
     *     
     */
    public void setIns(CTRPrChange value) {
        this.ins = value;
    }

    /**
     * Gets the value of the del property.
     * 
     * @return
     *     possible object is
     *     {@link CTRPrChange }
     *     
     */
    public CTRPrChange getDel() {
        return del;
    }

    /**
     * Sets the value of the del property.
     * 
     * @param value
     *     allowed object is
     *     {@link CTRPrChange }
     *     
     */
    public void setDel(CTRPrChange value) {
        this.del = value;
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
