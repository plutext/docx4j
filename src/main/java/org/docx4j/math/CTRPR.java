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
 * <p>Java class for CT_RPR complex type.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
 * 
 * <pre>
 * &lt;complexType name="CT_RPR">
 *   &lt;complexContent>
 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
 *       &lt;sequence>
 *         &lt;element name="lit" type="{http://schemas.openxmlformats.org/officeDocument/2006/math}CT_OnOff" minOccurs="0"/>
 *         &lt;choice>
 *           &lt;element name="nor" type="{http://schemas.openxmlformats.org/officeDocument/2006/math}CT_OnOff" minOccurs="0"/>
 *           &lt;sequence>
 *             &lt;group ref="{http://schemas.openxmlformats.org/officeDocument/2006/math}EG_ScriptStyle"/>
 *           &lt;/sequence>
 *         &lt;/choice>
 *         &lt;element name="brk" type="{http://schemas.openxmlformats.org/officeDocument/2006/math}CT_ManualBreak" minOccurs="0"/>
 *         &lt;element name="aln" type="{http://schemas.openxmlformats.org/officeDocument/2006/math}CT_OnOff" minOccurs="0"/>
 *       &lt;/sequence>
 *     &lt;/restriction>
 *   &lt;/complexContent>
 * &lt;/complexType>
 * </pre>
 * 
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "CT_RPR", propOrder = {
    "lit",
    "nor",
    "scr",
    "sty",
    "brk",
    "aln"
})
public class CTRPR
    implements Child
{

    protected CTOnOff lit;
    protected CTOnOff nor;
    protected CTScript scr;
    protected CTStyle sty;
    protected CTManualBreak brk;
    protected CTOnOff aln;
    @XmlTransient
    private Object parent;

    /**
     * Gets the value of the lit property.
     * 
     * @return
     *     possible object is
     *     {@link CTOnOff }
     *     
     */
    public CTOnOff getLit() {
        return lit;
    }

    /**
     * Sets the value of the lit property.
     * 
     * @param value
     *     allowed object is
     *     {@link CTOnOff }
     *     
     */
    public void setLit(CTOnOff value) {
        this.lit = value;
    }

    /**
     * Gets the value of the nor property.
     * 
     * @return
     *     possible object is
     *     {@link CTOnOff }
     *     
     */
    public CTOnOff getNor() {
        return nor;
    }

    /**
     * Sets the value of the nor property.
     * 
     * @param value
     *     allowed object is
     *     {@link CTOnOff }
     *     
     */
    public void setNor(CTOnOff value) {
        this.nor = value;
    }

    /**
     * Gets the value of the scr property.
     * 
     * @return
     *     possible object is
     *     {@link CTScript }
     *     
     */
    public CTScript getScr() {
        return scr;
    }

    /**
     * Sets the value of the scr property.
     * 
     * @param value
     *     allowed object is
     *     {@link CTScript }
     *     
     */
    public void setScr(CTScript value) {
        this.scr = value;
    }

    /**
     * Gets the value of the sty property.
     * 
     * @return
     *     possible object is
     *     {@link CTStyle }
     *     
     */
    public CTStyle getSty() {
        return sty;
    }

    /**
     * Sets the value of the sty property.
     * 
     * @param value
     *     allowed object is
     *     {@link CTStyle }
     *     
     */
    public void setSty(CTStyle value) {
        this.sty = value;
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
