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

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlType;


/**
 * <p>Java class for CT_TLTimeCondition complex type.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
 * 
 * <pre>
 * &lt;complexType name="CT_TLTimeCondition">
 *   &lt;complexContent>
 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
 *       &lt;choice minOccurs="0">
 *         &lt;element name="tgtEl" type="{http://schemas.openxmlformats.org/presentationml/2006/main}CT_TLTimeTargetElement"/>
 *         &lt;element name="tn" type="{http://schemas.openxmlformats.org/presentationml/2006/main}CT_TLTriggerTimeNodeID"/>
 *         &lt;element name="rtn" type="{http://schemas.openxmlformats.org/presentationml/2006/main}CT_TLTriggerRuntimeNode"/>
 *       &lt;/choice>
 *       &lt;attribute name="evt" type="{http://schemas.openxmlformats.org/presentationml/2006/main}ST_TLTriggerEvent" />
 *       &lt;attribute name="delay" type="{http://schemas.openxmlformats.org/presentationml/2006/main}ST_TLTime" />
 *     &lt;/restriction>
 *   &lt;/complexContent>
 * &lt;/complexType>
 * </pre>
 * 
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "CT_TLTimeCondition", propOrder = {
    "tgtEl",
    "tn",
    "rtn"
})
public class CTTLTimeCondition {

    protected CTTLTimeTargetElement tgtEl;
    protected CTTLTriggerTimeNodeID tn;
    protected CTTLTriggerRuntimeNode rtn;
    @XmlAttribute(name = "evt")
    protected STTLTriggerEvent evt;
    @XmlAttribute(name = "delay")
    protected String delay;

    /**
     * Gets the value of the tgtEl property.
     * 
     * @return
     *     possible object is
     *     {@link CTTLTimeTargetElement }
     *     
     */
    public CTTLTimeTargetElement getTgtEl() {
        return tgtEl;
    }

    /**
     * Sets the value of the tgtEl property.
     * 
     * @param value
     *     allowed object is
     *     {@link CTTLTimeTargetElement }
     *     
     */
    public void setTgtEl(CTTLTimeTargetElement value) {
        this.tgtEl = value;
    }

    /**
     * Gets the value of the tn property.
     * 
     * @return
     *     possible object is
     *     {@link CTTLTriggerTimeNodeID }
     *     
     */
    public CTTLTriggerTimeNodeID getTn() {
        return tn;
    }

    /**
     * Sets the value of the tn property.
     * 
     * @param value
     *     allowed object is
     *     {@link CTTLTriggerTimeNodeID }
     *     
     */
    public void setTn(CTTLTriggerTimeNodeID value) {
        this.tn = value;
    }

    /**
     * Gets the value of the rtn property.
     * 
     * @return
     *     possible object is
     *     {@link CTTLTriggerRuntimeNode }
     *     
     */
    public CTTLTriggerRuntimeNode getRtn() {
        return rtn;
    }

    /**
     * Sets the value of the rtn property.
     * 
     * @param value
     *     allowed object is
     *     {@link CTTLTriggerRuntimeNode }
     *     
     */
    public void setRtn(CTTLTriggerRuntimeNode value) {
        this.rtn = value;
    }

    /**
     * Gets the value of the evt property.
     * 
     * @return
     *     possible object is
     *     {@link STTLTriggerEvent }
     *     
     */
    public STTLTriggerEvent getEvt() {
        return evt;
    }

    /**
     * Sets the value of the evt property.
     * 
     * @param value
     *     allowed object is
     *     {@link STTLTriggerEvent }
     *     
     */
    public void setEvt(STTLTriggerEvent value) {
        this.evt = value;
    }

    /**
     * Gets the value of the delay property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getDelay() {
        return delay;
    }

    /**
     * Sets the value of the delay property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setDelay(String value) {
        this.delay = value;
    }

}
