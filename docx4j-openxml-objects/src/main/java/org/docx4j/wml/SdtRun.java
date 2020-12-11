/*
 *  Copyright 2007-2013, Plutext Pty Ltd.
 *   
 *  This file is part of docx4j.

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


package org.docx4j.wml; 

import javax.xml.bind.Unmarshaller;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlTransient;
import javax.xml.bind.annotation.XmlType;

import org.jvnet.jaxb2_commons.ppp.Child;


/**
 * <p>Java class for CT_SdtRun complex type.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
 * 
 * <pre>
 * &lt;complexType name="CT_SdtRun">
 *   &lt;complexContent>
 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
 *       &lt;sequence>
 *         &lt;element name="sdtPr" type="{http://schemas.openxmlformats.org/wordprocessingml/2006/main}CT_SdtPr" minOccurs="0"/>
 *         &lt;element name="sdtEndPr" type="{http://schemas.openxmlformats.org/wordprocessingml/2006/main}CT_SdtEndPr" minOccurs="0"/>
 *         &lt;element name="sdtContent" type="{http://schemas.openxmlformats.org/wordprocessingml/2006/main}CT_SdtContentRun" minOccurs="0"/>
 *       &lt;/sequence>
 *     &lt;/restriction>
 *   &lt;/complexContent>
 * &lt;/complexType>
 * </pre>
 * 
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "CT_SdtRun", propOrder = {
    "sdtPr",
    "sdtEndPr",
    "sdtContent"
})
@XmlRootElement(name = "sdt")
public class SdtRun implements SdtElement, Child
{

    protected SdtPr sdtPr;
    protected CTSdtEndPr sdtEndPr;
    protected CTSdtContentRun sdtContent;
    @XmlTransient
    private Object parent;

    /**
     * Gets the value of the sdtPr property.
     * 
     * @return
     *     possible object is
     *     {@link SdtPr }
     *     
     */
    public SdtPr getSdtPr() {
        return sdtPr;
    }

    /**
     * Sets the value of the sdtPr property.
     * 
     * @param value
     *     allowed object is
     *     {@link SdtPr }
     *     
     */
    public void setSdtPr(SdtPr value) {
        this.sdtPr = value;
        value.setParent(this); // unmarshalling does this automatically; this helps user in other cases        
    }

    /**
     * Gets the value of the sdtEndPr property.
     * 
     * @return
     *     possible object is
     *     {@link CTSdtEndPr }
     *     
     */
    public CTSdtEndPr getSdtEndPr() {
        return sdtEndPr;
    }

    /**
     * Sets the value of the sdtEndPr property.
     * 
     * @param value
     *     allowed object is
     *     {@link CTSdtEndPr }
     *     
     */
    public void setSdtEndPr(CTSdtEndPr value) {
        this.sdtEndPr = value;
    }

    /**
     * Gets the value of the sdtContent property.
     * 
     * @return
     *     possible object is
     *     {@link CTSdtContentRun }
     *     
     */
    public SdtContent getSdtContent() {
        return sdtContent;
    }

//    public SdtContent getContent() {
//        return sdtContent;
//    }
    
    /**
     * Sets the value of the sdtContent property.
     * 
     * @param value
     *     allowed object is
     *     {@link CTSdtContentRun }
     *     
     */
    public void setSdtContent(SdtContent value) {
        this.sdtContent = (CTSdtContentRun)value;
        ((CTSdtContentRun)value).setParent(this); // unmarshalling does this automatically; this helps user in other cases        
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
