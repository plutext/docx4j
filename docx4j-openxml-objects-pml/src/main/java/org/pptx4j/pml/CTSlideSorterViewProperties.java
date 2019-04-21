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
 * <p>Java class for CT_SlideSorterViewProperties complex type.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
 * 
 * <pre>
 * &lt;complexType name="CT_SlideSorterViewProperties">
 *   &lt;complexContent>
 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
 *       &lt;sequence>
 *         &lt;element name="cViewPr" type="{http://schemas.openxmlformats.org/presentationml/2006/main}CT_CommonViewProperties"/>
 *         &lt;element name="extLst" type="{http://schemas.openxmlformats.org/presentationml/2006/main}CT_ExtensionList" minOccurs="0"/>
 *       &lt;/sequence>
 *       &lt;attribute name="showFormatting" type="{http://www.w3.org/2001/XMLSchema}boolean" default="true" />
 *     &lt;/restriction>
 *   &lt;/complexContent>
 * &lt;/complexType>
 * </pre>
 * 
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "CT_SlideSorterViewProperties", propOrder = {
    "cViewPr",
    "extLst"
})
public class CTSlideSorterViewProperties {

    @XmlElement(required = true)
    protected CTCommonViewProperties cViewPr;
    protected CTExtensionList extLst;
    @XmlAttribute(name = "showFormatting")
    protected Boolean showFormatting;

    /**
     * Gets the value of the cViewPr property.
     * 
     * @return
     *     possible object is
     *     {@link CTCommonViewProperties }
     *     
     */
    public CTCommonViewProperties getCViewPr() {
        return cViewPr;
    }

    /**
     * Sets the value of the cViewPr property.
     * 
     * @param value
     *     allowed object is
     *     {@link CTCommonViewProperties }
     *     
     */
    public void setCViewPr(CTCommonViewProperties value) {
        this.cViewPr = value;
    }

    /**
     * Gets the value of the extLst property.
     * 
     * @return
     *     possible object is
     *     {@link CTExtensionList }
     *     
     */
    public CTExtensionList getExtLst() {
        return extLst;
    }

    /**
     * Sets the value of the extLst property.
     * 
     * @param value
     *     allowed object is
     *     {@link CTExtensionList }
     *     
     */
    public void setExtLst(CTExtensionList value) {
        this.extLst = value;
    }

    /**
     * Gets the value of the showFormatting property.
     * 
     * @return
     *     possible object is
     *     {@link Boolean }
     *     
     */
    public boolean isShowFormatting() {
        if (showFormatting == null) {
            return true;
        } else {
            return showFormatting;
        }
    }

    /**
     * Sets the value of the showFormatting property.
     * 
     * @param value
     *     allowed object is
     *     {@link Boolean }
     *     
     */
    public void setShowFormatting(Boolean value) {
        this.showFormatting = value;
    }

}
