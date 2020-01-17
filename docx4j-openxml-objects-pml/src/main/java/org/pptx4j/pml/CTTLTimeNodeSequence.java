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
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlTransient;
import javax.xml.bind.annotation.XmlType;

import org.jvnet.jaxb2_commons.ppp.Child;


/**
 * <p>Java class for CT_TLTimeNodeSequence complex type.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
 * 
 * <pre>
 * &lt;complexType name="CT_TLTimeNodeSequence"&gt;
 *   &lt;complexContent&gt;
 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType"&gt;
 *       &lt;sequence&gt;
 *         &lt;element name="cTn" type="{http://schemas.openxmlformats.org/presentationml/2006/main}CT_TLCommonTimeNodeData"/&gt;
 *         &lt;element name="prevCondLst" type="{http://schemas.openxmlformats.org/presentationml/2006/main}CT_TLTimeConditionList" minOccurs="0"/&gt;
 *         &lt;element name="nextCondLst" type="{http://schemas.openxmlformats.org/presentationml/2006/main}CT_TLTimeConditionList" minOccurs="0"/&gt;
 *       &lt;/sequence&gt;
 *       &lt;attribute name="concurrent" type="{http://www.w3.org/2001/XMLSchema}boolean" /&gt;
 *       &lt;attribute name="prevAc" type="{http://schemas.openxmlformats.org/presentationml/2006/main}ST_TLPreviousActionType" /&gt;
 *       &lt;attribute name="nextAc" type="{http://schemas.openxmlformats.org/presentationml/2006/main}ST_TLNextActionType" /&gt;
 *     &lt;/restriction&gt;
 *   &lt;/complexContent&gt;
 * &lt;/complexType&gt;
 * </pre>
 * 
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "CT_TLTimeNodeSequence", propOrder = {
    "cTn",
    "prevCondLst",
    "nextCondLst"
})
public class CTTLTimeNodeSequence implements Child
{

    @XmlElement(required = true)
    protected CTTLCommonTimeNodeData cTn;
    protected CTTLTimeConditionList prevCondLst;
    protected CTTLTimeConditionList nextCondLst;
    @XmlAttribute(name = "concurrent")
    protected Boolean concurrent;
    @XmlAttribute(name = "prevAc")
    protected STTLPreviousActionType prevAc;
    @XmlAttribute(name = "nextAc")
    protected STTLNextActionType nextAc;
    @XmlTransient
    private Object parent;

    /**
     * Gets the value of the cTn property.
     * 
     * @return
     *     possible object is
     *     {@link CTTLCommonTimeNodeData }
     *     
     */
    public CTTLCommonTimeNodeData getCTn() {
        return cTn;
    }

    /**
     * Sets the value of the cTn property.
     * 
     * @param value
     *     allowed object is
     *     {@link CTTLCommonTimeNodeData }
     *     
     */
    public void setCTn(CTTLCommonTimeNodeData value) {
        this.cTn = value;
    }

    /**
     * Gets the value of the prevCondLst property.
     * 
     * @return
     *     possible object is
     *     {@link CTTLTimeConditionList }
     *     
     */
    public CTTLTimeConditionList getPrevCondLst() {
        return prevCondLst;
    }

    /**
     * Sets the value of the prevCondLst property.
     * 
     * @param value
     *     allowed object is
     *     {@link CTTLTimeConditionList }
     *     
     */
    public void setPrevCondLst(CTTLTimeConditionList value) {
        this.prevCondLst = value;
    }

    /**
     * Gets the value of the nextCondLst property.
     * 
     * @return
     *     possible object is
     *     {@link CTTLTimeConditionList }
     *     
     */
    public CTTLTimeConditionList getNextCondLst() {
        return nextCondLst;
    }

    /**
     * Sets the value of the nextCondLst property.
     * 
     * @param value
     *     allowed object is
     *     {@link CTTLTimeConditionList }
     *     
     */
    public void setNextCondLst(CTTLTimeConditionList value) {
        this.nextCondLst = value;
    }

    /**
     * Gets the value of the concurrent property.
     * 
     * @return
     *     possible object is
     *     {@link Boolean }
     *     
     */
    public Boolean isConcurrent() {
        return concurrent;
    }

    /**
     * Sets the value of the concurrent property.
     * 
     * @param value
     *     allowed object is
     *     {@link Boolean }
     *     
     */
    public void setConcurrent(Boolean value) {
        this.concurrent = value;
    }

    /**
     * Gets the value of the prevAc property.
     * 
     * @return
     *     possible object is
     *     {@link STTLPreviousActionType }
     *     
     */
    public STTLPreviousActionType getPrevAc() {
        return prevAc;
    }

    /**
     * Sets the value of the prevAc property.
     * 
     * @param value
     *     allowed object is
     *     {@link STTLPreviousActionType }
     *     
     */
    public void setPrevAc(STTLPreviousActionType value) {
        this.prevAc = value;
    }

    /**
     * Gets the value of the nextAc property.
     * 
     * @return
     *     possible object is
     *     {@link STTLNextActionType }
     *     
     */
    public STTLNextActionType getNextAc() {
        return nextAc;
    }

    /**
     * Sets the value of the nextAc property.
     * 
     * @param value
     *     allowed object is
     *     {@link STTLNextActionType }
     *     
     */
    public void setNextAc(STTLNextActionType value) {
        this.nextAc = value;
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
