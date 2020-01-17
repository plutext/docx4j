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
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlTransient;
import javax.xml.bind.annotation.XmlType;

import org.jvnet.jaxb2_commons.ppp.Child;


/**
 * <p>Java class for CT_TLSetBehavior complex type.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
 * 
 * <pre>
 * &lt;complexType name="CT_TLSetBehavior"&gt;
 *   &lt;complexContent&gt;
 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType"&gt;
 *       &lt;sequence&gt;
 *         &lt;element name="cBhvr" type="{http://schemas.openxmlformats.org/presentationml/2006/main}CT_TLCommonBehaviorData"/&gt;
 *         &lt;element name="to" type="{http://schemas.openxmlformats.org/presentationml/2006/main}CT_TLAnimVariant" minOccurs="0"/&gt;
 *       &lt;/sequence&gt;
 *     &lt;/restriction&gt;
 *   &lt;/complexContent&gt;
 * &lt;/complexType&gt;
 * </pre>
 * 
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "CT_TLSetBehavior", propOrder = {
    "cBhvr",
    "to"
})
public class CTTLSetBehavior implements Child
{

    @XmlElement(required = true)
    protected CTTLCommonBehaviorData cBhvr;
    protected CTTLAnimVariant to;
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
     * Gets the value of the to property.
     * 
     * @return
     *     possible object is
     *     {@link CTTLAnimVariant }
     *     
     */
    public CTTLAnimVariant getTo() {
        return to;
    }

    /**
     * Sets the value of the to property.
     * 
     * @param value
     *     allowed object is
     *     {@link CTTLAnimVariant }
     *     
     */
    public void setTo(CTTLAnimVariant value) {
        this.to = value;
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
