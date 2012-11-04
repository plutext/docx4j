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
 * <p>Java class for CT_TLAnimateBehavior complex type.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
 * 
 * <pre>
 * &lt;complexType name="CT_TLAnimateBehavior">
 *   &lt;complexContent>
 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
 *       &lt;sequence>
 *         &lt;element name="cBhvr" type="{http://schemas.openxmlformats.org/presentationml/2006/main}CT_TLCommonBehaviorData"/>
 *         &lt;element name="tavLst" type="{http://schemas.openxmlformats.org/presentationml/2006/main}CT_TLTimeAnimateValueList" minOccurs="0"/>
 *       &lt;/sequence>
 *       &lt;attribute name="by" type="{http://www.w3.org/2001/XMLSchema}string" />
 *       &lt;attribute name="from" type="{http://www.w3.org/2001/XMLSchema}string" />
 *       &lt;attribute name="to" type="{http://www.w3.org/2001/XMLSchema}string" />
 *       &lt;attribute name="calcmode" type="{http://schemas.openxmlformats.org/presentationml/2006/main}ST_TLAnimateBehaviorCalcMode" />
 *       &lt;attribute name="valueType" type="{http://schemas.openxmlformats.org/presentationml/2006/main}ST_TLAnimateBehaviorValueType" />
 *     &lt;/restriction>
 *   &lt;/complexContent>
 * &lt;/complexType>
 * </pre>
 * 
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "CT_TLAnimateBehavior", propOrder = {
    "cBhvr",
    "tavLst"
})
public class CTTLAnimateBehavior {

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

}
