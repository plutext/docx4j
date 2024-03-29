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

import jakarta.xml.bind.Unmarshaller;
import jakarta.xml.bind.annotation.XmlAccessType;
import jakarta.xml.bind.annotation.XmlAccessorType;
import jakarta.xml.bind.annotation.XmlAttribute;
import jakarta.xml.bind.annotation.XmlElement;
import jakarta.xml.bind.annotation.XmlTransient;
import jakarta.xml.bind.annotation.XmlType;

import org.jvnet.jaxb2_commons.ppp.Child;


/**
 * <p>Java class for CT_TLAnimateBehavior complex type.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
 * 
 * <pre>
 * &lt;complexType name="CT_TLAnimateBehavior"&gt;
 *   &lt;complexContent&gt;
 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType"&gt;
 *       &lt;sequence&gt;
 *         &lt;element name="cBhvr" type="{http://schemas.openxmlformats.org/presentationml/2006/main}CT_TLCommonBehaviorData"/&gt;
 *         &lt;element name="tavLst" type="{http://schemas.openxmlformats.org/presentationml/2006/main}CT_TLTimeAnimateValueList" minOccurs="0"/&gt;
 *       &lt;/sequence&gt;
 *       &lt;attribute name="by" type="{http://www.w3.org/2001/XMLSchema}string" /&gt;
 *       &lt;attribute name="from" type="{http://www.w3.org/2001/XMLSchema}string" /&gt;
 *       &lt;attribute name="to" type="{http://www.w3.org/2001/XMLSchema}string" /&gt;
 *       &lt;attribute name="calcmode" type="{http://schemas.openxmlformats.org/presentationml/2006/main}ST_TLAnimateBehaviorCalcMode" /&gt;
 *       &lt;attribute name="valueType" type="{http://schemas.openxmlformats.org/presentationml/2006/main}ST_TLAnimateBehaviorValueType" /&gt;
 *     &lt;/restriction&gt;
 *   &lt;/complexContent&gt;
 * &lt;/complexType&gt;
 * </pre>
 * 
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "CT_TLAnimateBehavior", propOrder = {
    "cBhvr",
    "tavLst"
})
public class CTTLAnimateBehavior implements Child
{

    @XmlElement(required = true)
    protected CTTLCommonBehaviorData cBhvr;
    protected CTTLTimeAnimateValueList tavLst;
    @XmlAttribute(name = "by")
    protected String by;
    @XmlAttribute(name = "from")
    protected String from;
    @XmlAttribute(name = "to")
    protected String to;
    @XmlAttribute(name = "calcmode")
    protected STTLAnimateBehaviorCalcMode calcmode;
    @XmlAttribute(name = "valueType")
    protected STTLAnimateBehaviorValueType valueType;
    @XmlTransient
    private Object parent;

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
     * Gets the value of the tavLst property.
     * 
     * @return
     *     possible object is
     *     {@link CTTLTimeAnimateValueList }
     *     
     */
    public CTTLTimeAnimateValueList getTavLst() {
        return tavLst;
    }

    /**
     * Sets the value of the tavLst property.
     * 
     * @param value
     *     allowed object is
     *     {@link CTTLTimeAnimateValueList }
     *     
     */
    public void setTavLst(CTTLTimeAnimateValueList value) {
        this.tavLst = value;
    }

    /**
     * Gets the value of the by property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getBy() {
        return by;
    }

    /**
     * Sets the value of the by property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setBy(String value) {
        this.by = value;
    }

    /**
     * Gets the value of the from property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getFrom() {
        return from;
    }

    /**
     * Sets the value of the from property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setFrom(String value) {
        this.from = value;
    }

    /**
     * Gets the value of the to property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getTo() {
        return to;
    }

    /**
     * Sets the value of the to property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setTo(String value) {
        this.to = value;
    }

    /**
     * Gets the value of the calcmode property.
     * 
     * @return
     *     possible object is
     *     {@link STTLAnimateBehaviorCalcMode }
     *     
     */
    public STTLAnimateBehaviorCalcMode getCalcmode() {
        return calcmode;
    }

    /**
     * Sets the value of the calcmode property.
     * 
     * @param value
     *     allowed object is
     *     {@link STTLAnimateBehaviorCalcMode }
     *     
     */
    public void setCalcmode(STTLAnimateBehaviorCalcMode value) {
        this.calcmode = value;
    }

    /**
     * Gets the value of the valueType property.
     * 
     * @return
     *     possible object is
     *     {@link STTLAnimateBehaviorValueType }
     *     
     */
    public STTLAnimateBehaviorValueType getValueType() {
        return valueType;
    }

    /**
     * Sets the value of the valueType property.
     * 
     * @param value
     *     allowed object is
     *     {@link STTLAnimateBehaviorValueType }
     *     
     */
    public void setValueType(STTLAnimateBehaviorValueType value) {
        this.valueType = value;
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
