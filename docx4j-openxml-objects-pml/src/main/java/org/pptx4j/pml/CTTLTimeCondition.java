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
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlTransient;
import javax.xml.bind.annotation.XmlType;

import org.jvnet.jaxb2_commons.ppp.Child;


/**
 * <p>Java class for CT_TLTimeCondition complex type.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
 * 
 * <pre>
 * &lt;complexType name="CT_TLTimeCondition"&gt;
 *   &lt;complexContent&gt;
 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType"&gt;
 *       &lt;choice minOccurs="0"&gt;
 *         &lt;element name="tgtEl" type="{http://schemas.openxmlformats.org/presentationml/2006/main}CT_TLTimeTargetElement"/&gt;
 *         &lt;element name="tn" type="{http://schemas.openxmlformats.org/presentationml/2006/main}CT_TLTriggerTimeNodeID"/&gt;
 *         &lt;element name="rtn" type="{http://schemas.openxmlformats.org/presentationml/2006/main}CT_TLTriggerRuntimeNode"/&gt;
 *       &lt;/choice&gt;
 *       &lt;attribute name="evt" type="{http://schemas.openxmlformats.org/presentationml/2006/main}ST_TLTriggerEvent" /&gt;
 *       &lt;attribute name="delay" type="{http://schemas.openxmlformats.org/presentationml/2006/main}ST_TLTime" /&gt;
 *     &lt;/restriction&gt;
 *   &lt;/complexContent&gt;
 * &lt;/complexType&gt;
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
public class CTTLTimeCondition implements Child
{

    protected CTTLTimeTargetElement tgtEl;
    protected CTTLTriggerTimeNodeID tn;
    protected CTTLTriggerRuntimeNode rtn;
    @XmlAttribute(name = "evt")
    protected STTLTriggerEvent evt;
    @XmlAttribute(name = "delay")
    protected String delay;
    @XmlTransient
    private Object parent;

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
