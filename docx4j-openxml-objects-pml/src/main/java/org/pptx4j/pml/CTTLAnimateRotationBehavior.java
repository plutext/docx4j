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
 * <p>Java class for CT_TLAnimateRotationBehavior complex type.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
 * 
 * <pre>
 * &lt;complexType name="CT_TLAnimateRotationBehavior">
 *   &lt;complexContent>
 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
 *       &lt;sequence>
 *         &lt;element name="cBhvr" type="{http://schemas.openxmlformats.org/presentationml/2006/main}CT_TLCommonBehaviorData"/>
 *       &lt;/sequence>
 *       &lt;attribute name="by" type="{http://schemas.openxmlformats.org/drawingml/2006/main}ST_Angle" />
 *       &lt;attribute name="from" type="{http://schemas.openxmlformats.org/drawingml/2006/main}ST_Angle" />
 *       &lt;attribute name="to" type="{http://schemas.openxmlformats.org/drawingml/2006/main}ST_Angle" />
 *     &lt;/restriction>
 *   &lt;/complexContent>
 * &lt;/complexType>
 * </pre>
 * 
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "CT_TLAnimateRotationBehavior", propOrder = {
    "cBhvr"
})
public class CTTLAnimateRotationBehavior {

    @XmlElement(required = true)
    protected CTTLCommonBehaviorData cBhvr;
    @XmlAttribute(name = "by")
    protected Integer by;
    @XmlAttribute(name = "from")
    protected Integer from;
    @XmlAttribute(name = "to")
    protected Integer to;

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
     * Gets the value of the by property.
     * 
     * @return
     *     possible object is
     *     {@link Integer }
     *     
     */
    public Integer getBy() {
        return by;
    }

    /**
     * Sets the value of the by property.
     * 
     * @param value
     *     allowed object is
     *     {@link Integer }
     *     
     */
    public void setBy(Integer value) {
        this.by = value;
    }

    /**
     * Gets the value of the from property.
     * 
     * @return
     *     possible object is
     *     {@link Integer }
     *     
     */
    public Integer getFrom() {
        return from;
    }

    /**
     * Sets the value of the from property.
     * 
     * @param value
     *     allowed object is
     *     {@link Integer }
     *     
     */
    public void setFrom(Integer value) {
        this.from = value;
    }

    /**
     * Gets the value of the to property.
     * 
     * @return
     *     possible object is
     *     {@link Integer }
     *     
     */
    public Integer getTo() {
        return to;
    }

    /**
     * Sets the value of the to property.
     * 
     * @param value
     *     allowed object is
     *     {@link Integer }
     *     
     */
    public void setTo(Integer value) {
        this.to = value;
    }

}
