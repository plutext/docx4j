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
import javax.xml.bind.annotation.XmlTransient;
import javax.xml.bind.annotation.XmlType;
import org.jvnet.jaxb2_commons.ppp.Child;


/**
 * <p>Java class for CT_GroupChrPr complex type.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
 * 
 * <pre>
 * &lt;complexType name="CT_GroupChrPr">
 *   &lt;complexContent>
 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
 *       &lt;sequence>
 *         &lt;element name="chr" type="{http://schemas.openxmlformats.org/officeDocument/2006/math}CT_Char" minOccurs="0"/>
 *         &lt;element name="pos" type="{http://schemas.openxmlformats.org/officeDocument/2006/math}CT_TopBot" minOccurs="0"/>
 *         &lt;element name="vertJc" type="{http://schemas.openxmlformats.org/officeDocument/2006/math}CT_TopBot" minOccurs="0"/>
 *         &lt;element name="ctrlPr" type="{http://schemas.openxmlformats.org/officeDocument/2006/math}CT_CtrlPr" minOccurs="0"/>
 *       &lt;/sequence>
 *     &lt;/restriction>
 *   &lt;/complexContent>
 * &lt;/complexType>
 * </pre>
 * 
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "CT_GroupChrPr", propOrder = {
    "chr",
    "pos",
    "vertJc",
    "ctrlPr"
})
public class CTGroupChrPr
    implements Child
{

    protected CTChar chr;
    protected CTTopBot pos;
    protected CTTopBot vertJc;
    protected CTCtrlPr ctrlPr;
    @XmlTransient
    private Object parent;

    /**
     * Gets the value of the chr property.
     * 
     * @return
     *     possible object is
     *     {@link CTChar }
     *     
     */
    public CTChar getChr() {
        return chr;
    }

    /**
     * Sets the value of the chr property.
     * 
     * @param value
     *     allowed object is
     *     {@link CTChar }
     *     
     */
    public void setChr(CTChar value) {
        this.chr = value;
    }

    /**
     * Gets the value of the pos property.
     * 
     * @return
     *     possible object is
     *     {@link CTTopBot }
     *     
     */
    public CTTopBot getPos() {
        return pos;
    }

    /**
     * Sets the value of the pos property.
     * 
     * @param value
     *     allowed object is
     *     {@link CTTopBot }
     *     
     */
    public void setPos(CTTopBot value) {
        this.pos = value;
    }

    /**
     * Gets the value of the vertJc property.
     * 
     * @return
     *     possible object is
     *     {@link CTTopBot }
     *     
     */
    public CTTopBot getVertJc() {
        return vertJc;
    }

    /**
     * Sets the value of the vertJc property.
     * 
     * @param value
     *     allowed object is
     *     {@link CTTopBot }
     *     
     */
    public void setVertJc(CTTopBot value) {
        this.vertJc = value;
    }

    /**
     * Gets the value of the ctrlPr property.
     * 
     * @return
     *     possible object is
     *     {@link CTCtrlPr }
     *     
     */
    public CTCtrlPr getCtrlPr() {
        return ctrlPr;
    }

    /**
     * Sets the value of the ctrlPr property.
     * 
     * @param value
     *     allowed object is
     *     {@link CTCtrlPr }
     *     
     */
    public void setCtrlPr(CTCtrlPr value) {
        this.ctrlPr = value;
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
