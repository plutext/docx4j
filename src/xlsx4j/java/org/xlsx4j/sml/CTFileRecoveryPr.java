/*
 *  Copyright 2010, Plutext Pty Ltd.
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


package org.xlsx4j.sml;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlType;


/**
 * <p>Java class for CT_FileRecoveryPr complex type.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
 * 
 * <pre>
 * &lt;complexType name="CT_FileRecoveryPr">
 *   &lt;complexContent>
 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
 *       &lt;attribute name="autoRecover" type="{http://www.w3.org/2001/XMLSchema}boolean" default="true" />
 *       &lt;attribute name="crashSave" type="{http://www.w3.org/2001/XMLSchema}boolean" default="false" />
 *       &lt;attribute name="dataExtractLoad" type="{http://www.w3.org/2001/XMLSchema}boolean" default="false" />
 *       &lt;attribute name="repairLoad" type="{http://www.w3.org/2001/XMLSchema}boolean" default="false" />
 *     &lt;/restriction>
 *   &lt;/complexContent>
 * &lt;/complexType>
 * </pre>
 * 
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "CT_FileRecoveryPr")
public class CTFileRecoveryPr {

    @XmlAttribute
    protected Boolean autoRecover;
    @XmlAttribute
    protected Boolean crashSave;
    @XmlAttribute
    protected Boolean dataExtractLoad;
    @XmlAttribute
    protected Boolean repairLoad;

    /**
     * Gets the value of the autoRecover property.
     * 
     * @return
     *     possible object is
     *     {@link Boolean }
     *     
     */
    public boolean isAutoRecover() {
        if (autoRecover == null) {
            return true;
        } else {
            return autoRecover;
        }
    }

    /**
     * Sets the value of the autoRecover property.
     * 
     * @param value
     *     allowed object is
     *     {@link Boolean }
     *     
     */
    public void setAutoRecover(Boolean value) {
        this.autoRecover = value;
    }

    /**
     * Gets the value of the crashSave property.
     * 
     * @return
     *     possible object is
     *     {@link Boolean }
     *     
     */
    public boolean isCrashSave() {
        if (crashSave == null) {
            return false;
        } else {
            return crashSave;
        }
    }

    /**
     * Sets the value of the crashSave property.
     * 
     * @param value
     *     allowed object is
     *     {@link Boolean }
     *     
     */
    public void setCrashSave(Boolean value) {
        this.crashSave = value;
    }

    /**
     * Gets the value of the dataExtractLoad property.
     * 
     * @return
     *     possible object is
     *     {@link Boolean }
     *     
     */
    public boolean isDataExtractLoad() {
        if (dataExtractLoad == null) {
            return false;
        } else {
            return dataExtractLoad;
        }
    }

    /**
     * Sets the value of the dataExtractLoad property.
     * 
     * @param value
     *     allowed object is
     *     {@link Boolean }
     *     
     */
    public void setDataExtractLoad(Boolean value) {
        this.dataExtractLoad = value;
    }

    /**
     * Gets the value of the repairLoad property.
     * 
     * @return
     *     possible object is
     *     {@link Boolean }
     *     
     */
    public boolean isRepairLoad() {
        if (repairLoad == null) {
            return false;
        } else {
            return repairLoad;
        }
    }

    /**
     * Sets the value of the repairLoad property.
     * 
     * @param value
     *     allowed object is
     *     {@link Boolean }
     *     
     */
    public void setRepairLoad(Boolean value) {
        this.repairLoad = value;
    }

}
