/*
 *  Copyright 2007-2008, Plutext Pty Ltd.
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


package org.docx4j.dml;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlType;


/**
 * <p>Java class for CT_NonVisualConnectorProperties complex type.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
 * 
 * <pre>
 * &lt;complexType name="CT_NonVisualConnectorProperties">
 *   &lt;complexContent>
 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
 *       &lt;sequence>
 *         &lt;element name="cxnSpLocks" type="{http://schemas.openxmlformats.org/drawingml/2006/main}CT_ConnectorLocking" minOccurs="0"/>
 *         &lt;element name="stCxn" type="{http://schemas.openxmlformats.org/drawingml/2006/main}CT_Connection" minOccurs="0"/>
 *         &lt;element name="endCxn" type="{http://schemas.openxmlformats.org/drawingml/2006/main}CT_Connection" minOccurs="0"/>
 *         &lt;element name="extLst" type="{http://schemas.openxmlformats.org/drawingml/2006/main}CT_OfficeArtExtensionList" minOccurs="0"/>
 *       &lt;/sequence>
 *     &lt;/restriction>
 *   &lt;/complexContent>
 * &lt;/complexType>
 * </pre>
 * 
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "CT_NonVisualConnectorProperties", propOrder = {
    "cxnSpLocks",
    "stCxn",
    "endCxn",
    "extLst"
})
public class CTNonVisualConnectorProperties {

    protected CTConnectorLocking cxnSpLocks;
    protected CTConnection stCxn;
    protected CTConnection endCxn;
    protected CTOfficeArtExtensionList extLst;

    /**
     * Gets the value of the cxnSpLocks property.
     * 
     * @return
     *     possible object is
     *     {@link CTConnectorLocking }
     *     
     */
    public CTConnectorLocking getCxnSpLocks() {
        return cxnSpLocks;
    }

    /**
     * Sets the value of the cxnSpLocks property.
     * 
     * @param value
     *     allowed object is
     *     {@link CTConnectorLocking }
     *     
     */
    public void setCxnSpLocks(CTConnectorLocking value) {
        this.cxnSpLocks = value;
    }

    /**
     * Gets the value of the stCxn property.
     * 
     * @return
     *     possible object is
     *     {@link CTConnection }
     *     
     */
    public CTConnection getStCxn() {
        return stCxn;
    }

    /**
     * Sets the value of the stCxn property.
     * 
     * @param value
     *     allowed object is
     *     {@link CTConnection }
     *     
     */
    public void setStCxn(CTConnection value) {
        this.stCxn = value;
    }

    /**
     * Gets the value of the endCxn property.
     * 
     * @return
     *     possible object is
     *     {@link CTConnection }
     *     
     */
    public CTConnection getEndCxn() {
        return endCxn;
    }

    /**
     * Sets the value of the endCxn property.
     * 
     * @param value
     *     allowed object is
     *     {@link CTConnection }
     *     
     */
    public void setEndCxn(CTConnection value) {
        this.endCxn = value;
    }

    /**
     * Gets the value of the extLst property.
     * 
     * @return
     *     possible object is
     *     {@link CTOfficeArtExtensionList }
     *     
     */
    public CTOfficeArtExtensionList getExtLst() {
        return extLst;
    }

    /**
     * Sets the value of the extLst property.
     * 
     * @param value
     *     allowed object is
     *     {@link CTOfficeArtExtensionList }
     *     
     */
    public void setExtLst(CTOfficeArtExtensionList value) {
        this.extLst = value;
    }

}
