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
 * <p>Java class for CT_TLCommandBehavior complex type.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
 * 
 * <pre>
 * &lt;complexType name="CT_TLCommandBehavior">
 *   &lt;complexContent>
 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
 *       &lt;sequence>
 *         &lt;element name="cBhvr" type="{http://schemas.openxmlformats.org/presentationml/2006/main}CT_TLCommonBehaviorData"/>
 *       &lt;/sequence>
 *       &lt;attribute name="type" type="{http://schemas.openxmlformats.org/presentationml/2006/main}ST_TLCommandType" />
 *       &lt;attribute name="cmd" type="{http://www.w3.org/2001/XMLSchema}string" />
 *     &lt;/restriction>
 *   &lt;/complexContent>
 * &lt;/complexType>
 * </pre>
 * 
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "CT_TLCommandBehavior", propOrder = {
    "cBhvr"
})
public class CTTLCommandBehavior {

    @XmlElement(required = true)
    protected CTTLCommonBehaviorData cBhvr;
    @XmlAttribute(name = "type")
    protected STTLCommandType type;
    @XmlAttribute(name = "cmd")
    protected String cmd;

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
     * Gets the value of the type property.
     * 
     * @return
     *     possible object is
     *     {@link STTLCommandType }
     *     
     */
    public STTLCommandType getType() {
        return type;
    }

    /**
     * Sets the value of the type property.
     * 
     * @param value
     *     allowed object is
     *     {@link STTLCommandType }
     *     
     */
    public void setType(STTLCommandType value) {
        this.type = value;
    }

    /**
     * Gets the value of the cmd property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getCmd() {
        return cmd;
    }

    /**
     * Sets the value of the cmd property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setCmd(String value) {
        this.cmd = value;
    }

}
