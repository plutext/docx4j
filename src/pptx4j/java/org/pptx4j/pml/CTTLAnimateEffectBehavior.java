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
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlType;


/**
 * <p>Java class for CT_TLAnimateEffectBehavior complex type.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
 * 
 * <pre>
 * &lt;complexType name="CT_TLAnimateEffectBehavior">
 *   &lt;complexContent>
 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
 *       &lt;sequence>
 *         &lt;element name="cBhvr" type="{http://schemas.openxmlformats.org/presentationml/2006/main}CT_TLCommonBehaviorData"/>
 *         &lt;element name="progress" type="{http://schemas.openxmlformats.org/presentationml/2006/main}CT_TLAnimVariant" minOccurs="0"/>
 *       &lt;/sequence>
 *       &lt;attribute name="transition" type="{http://schemas.openxmlformats.org/presentationml/2006/main}ST_TLAnimateEffectTransition" />
 *       &lt;attribute name="filter" type="{http://www.w3.org/2001/XMLSchema}string" />
 *       &lt;attribute name="prLst" type="{http://www.w3.org/2001/XMLSchema}string" />
 *     &lt;/restriction>
 *   &lt;/complexContent>
 * &lt;/complexType>
 * </pre>
 * 
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "CT_TLAnimateEffectBehavior", propOrder = {
    "cBhvr",
    "progress"
})
public class CTTLAnimateEffectBehavior {

    @XmlElement(required = true)
    protected CTTLCommonBehaviorData cBhvr;
    protected CTTLAnimVariant progress;
    @XmlAttribute(name = "transition")
    protected STTLAnimateEffectTransition transition;
    @XmlAttribute(name = "filter")
    protected String filter;
    @XmlAttribute(name = "prLst")
    protected String prLst;

    /**
     * Gets the value of the cBhvr property.
     * 
     * @return
     *     possible object is
     *     {@link CTTLCommonBehaviorData }
     *     
     */
    public CTTLCommonBehaviorData getCBhvr() {
        return cBhvr;
    }

    /**
     * Sets the value of the cBhvr property.
     * 
     * @param value
     *     allowed object is
     *     {@link CTTLCommonBehaviorData }
     *     
     */
    public void setCBhvr(CTTLCommonBehaviorData value) {
        this.cBhvr = value;
    }

    /**
     * Gets the value of the progress property.
     * 
     * @return
     *     possible object is
     *     {@link CTTLAnimVariant }
     *     
     */
    public CTTLAnimVariant getProgress() {
        return progress;
    }

    /**
     * Sets the value of the progress property.
     * 
     * @param value
     *     allowed object is
     *     {@link CTTLAnimVariant }
     *     
     */
    public void setProgress(CTTLAnimVariant value) {
        this.progress = value;
    }

    /**
     * Gets the value of the transition property.
     * 
     * @return
     *     possible object is
     *     {@link STTLAnimateEffectTransition }
     *     
     */
    public STTLAnimateEffectTransition getTransition() {
        return transition;
    }

    /**
     * Sets the value of the transition property.
     * 
     * @param value
     *     allowed object is
     *     {@link STTLAnimateEffectTransition }
     *     
     */
    public void setTransition(STTLAnimateEffectTransition value) {
        this.transition = value;
    }

    /**
     * Gets the value of the filter property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getFilter() {
        return filter;
    }

    /**
     * Sets the value of the filter property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setFilter(String value) {
        this.filter = value;
    }

    /**
     * Gets the value of the prLst property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getPrLst() {
        return prLst;
    }

    /**
     * Sets the value of the prLst property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setPrLst(String value) {
        this.prLst = value;
    }

}
