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


package org.docx4j.dml.spreadsheetdrawing;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlType;


/**
 * <p>Java class for CT_AnchorClientData complex type.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
 * 
 * <pre>
 * &lt;complexType name="CT_AnchorClientData">
 *   &lt;complexContent>
 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
 *       &lt;attribute name="fLocksWithSheet" type="{http://www.w3.org/2001/XMLSchema}boolean" default="true" />
 *       &lt;attribute name="fPrintsWithSheet" type="{http://www.w3.org/2001/XMLSchema}boolean" default="true" />
 *     &lt;/restriction>
 *   &lt;/complexContent>
 * &lt;/complexType>
 * </pre>
 * 
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "CT_AnchorClientData")
public class CTAnchorClientData {

    @XmlAttribute
    protected Boolean fLocksWithSheet;
    @XmlAttribute
    protected Boolean fPrintsWithSheet;

    /**
     * Gets the value of the fLocksWithSheet property.
     * 
     * @return
     *     possible object is
     *     {@link Boolean }
     *     
     */
    public boolean isFLocksWithSheet() {
        if (fLocksWithSheet == null) {
            return true;
        } else {
            return fLocksWithSheet;
        }
    }

    /**
     * Sets the value of the fLocksWithSheet property.
     * 
     * @param value
     *     allowed object is
     *     {@link Boolean }
     *     
     */
    public void setFLocksWithSheet(Boolean value) {
        this.fLocksWithSheet = value;
    }

    /**
     * Gets the value of the fPrintsWithSheet property.
     * 
     * @return
     *     possible object is
     *     {@link Boolean }
     *     
     */
    public boolean isFPrintsWithSheet() {
        if (fPrintsWithSheet == null) {
            return true;
        } else {
            return fPrintsWithSheet;
        }
    }

    /**
     * Sets the value of the fPrintsWithSheet property.
     * 
     * @param value
     *     allowed object is
     *     {@link Boolean }
     *     
     */
    public void setFPrintsWithSheet(Boolean value) {
        this.fPrintsWithSheet = value;
    }

}
