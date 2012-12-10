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
 * <p>Java class for CT_BoxPr complex type.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
 * 
 * <pre>
 * &lt;complexType name="CT_BoxPr">
 *   &lt;complexContent>
 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
 *       &lt;sequence>
 *         &lt;element name="opEmu" type="{http://schemas.openxmlformats.org/officeDocument/2006/math}CT_OnOff" minOccurs="0"/>
 *         &lt;element name="noBreak" type="{http://schemas.openxmlformats.org/officeDocument/2006/math}CT_OnOff" minOccurs="0"/>
 *         &lt;element name="diff" type="{http://schemas.openxmlformats.org/officeDocument/2006/math}CT_OnOff" minOccurs="0"/>
 *         &lt;element name="brk" type="{http://schemas.openxmlformats.org/officeDocument/2006/math}CT_ManualBreak" minOccurs="0"/>
 *         &lt;element name="aln" type="{http://schemas.openxmlformats.org/officeDocument/2006/math}CT_OnOff" minOccurs="0"/>
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
@XmlType(name = "CT_BoxPr", propOrder = {
    "opEmu",
    "noBreak",
    "diff",
    "brk",
    "aln",
    "ctrlPr"
})
public class CTBoxPr
    implements Child
{

    protected CTOnOff opEmu;
    protected CTOnOff noBreak;
    protected CTOnOff diff;
    protected CTManualBreak brk;
    protected CTOnOff aln;
    protected CTCtrlPr ctrlPr;
    @XmlTransient
    private Object parent;

    /**
     * Gets the value of the opEmu property.
     * 
     * @return
     *     possible object is
     *     {@link CTOnOff }
     *     
     */
    public CTOnOff getOpEmu() {
        return opEmu;
    }

    /**
     * Sets the value of the opEmu property.
     * 
     * @param value
     *     allowed object is
     *     {@link CTOnOff }
     *     
     */
    public void setOpEmu(CTOnOff value) {
        this.opEmu = value;
    }

    /**
     * Gets the value of the noBreak property.
     * 
     * @return
     *     possible object is
     *     {@link CTOnOff }
     *     
     */
    public CTOnOff getNoBreak() {
        return noBreak;
    }

    /**
     * Sets the value of the noBreak property.
     * 
     * @param value
     *     allowed object is
     *     {@link CTOnOff }
     *     
     */
    public void setNoBreak(CTOnOff value) {
        this.noBreak = value;
    }

    /**
     * Gets the value of the diff property.
     * 
     * @return
     *     possible object is
     *     {@link CTOnOff }
     *     
     */
    public CTOnOff getDiff() {
        return diff;
    }

    /**
     * Sets the value of the diff property.
     * 
     * @param value
     *     allowed object is
     *     {@link CTOnOff }
     *     
     */
    public void setDiff(CTOnOff value) {
        this.diff = value;
    }

    /**
     * Gets the value of the brk property.
     * 
     * @return
     *     possible object is
     *     {@link CTManualBreak }
     *     
     */
    public CTManualBreak getBrk() {
        return brk;
    }

    /**
     * Sets the value of the brk property.
     * 
     * @param value
     *     allowed object is
     *     {@link CTManualBreak }
     *     
     */
    public void setBrk(CTManualBreak value) {
        this.brk = value;
    }

    /**
     * Gets the value of the aln property.
     * 
     * @return
     *     possible object is
     *     {@link CTOnOff }
     *     
     */
    public CTOnOff getAln() {
        return aln;
    }

    /**
     * Sets the value of the aln property.
     * 
     * @param value
     *     allowed object is
     *     {@link CTOnOff }
     *     
     */
    public void setAln(CTOnOff value) {
        this.aln = value;
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
