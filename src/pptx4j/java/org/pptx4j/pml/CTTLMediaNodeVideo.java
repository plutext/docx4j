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
 * <p>Java class for CT_TLMediaNodeVideo complex type.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
 * 
 * <pre>
 * &lt;complexType name="CT_TLMediaNodeVideo">
 *   &lt;complexContent>
 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
 *       &lt;sequence>
 *         &lt;element name="cMediaNode" type="{http://schemas.openxmlformats.org/presentationml/2006/main}CT_TLCommonMediaNodeData"/>
 *       &lt;/sequence>
 *       &lt;attribute name="fullScrn" type="{http://www.w3.org/2001/XMLSchema}boolean" default="false" />
 *     &lt;/restriction>
 *   &lt;/complexContent>
 * &lt;/complexType>
 * </pre>
 * 
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "CT_TLMediaNodeVideo", propOrder = {
    "cMediaNode"
})
public class CTTLMediaNodeVideo {

    @XmlElement(required = true)
    protected CTTLCommonMediaNodeData cMediaNode;
    @XmlAttribute(name = "fullScrn")
    protected Boolean fullScrn;

    /**
     * Gets the value of the cMediaNode property.
     * 
     * @return
     *     possible object is
     *     {@link CTTLCommonMediaNodeData }
     *     
     */
    public CTTLCommonMediaNodeData getCMediaNode() {
        return cMediaNode;
    }

    /**
     * Sets the value of the cMediaNode property.
     * 
     * @param value
     *     allowed object is
     *     {@link CTTLCommonMediaNodeData }
     *     
     */
    public void setCMediaNode(CTTLCommonMediaNodeData value) {
        this.cMediaNode = value;
    }

    /**
     * Gets the value of the fullScrn property.
     * 
     * @return
     *     possible object is
     *     {@link Boolean }
     *     
     */
    public boolean isFullScrn() {
        if (fullScrn == null) {
            return false;
        } else {
            return fullScrn;
        }
    }

    /**
     * Sets the value of the fullScrn property.
     * 
     * @param value
     *     allowed object is
     *     {@link Boolean }
     *     
     */
    public void setFullScrn(Boolean value) {
        this.fullScrn = value;
    }

}
