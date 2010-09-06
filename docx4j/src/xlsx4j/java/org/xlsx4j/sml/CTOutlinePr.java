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
 * <p>Java class for CT_OutlinePr complex type.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
 * 
 * <pre>
 * &lt;complexType name="CT_OutlinePr">
 *   &lt;complexContent>
 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
 *       &lt;attribute name="applyStyles" type="{http://www.w3.org/2001/XMLSchema}boolean" default="false" />
 *       &lt;attribute name="summaryBelow" type="{http://www.w3.org/2001/XMLSchema}boolean" default="true" />
 *       &lt;attribute name="summaryRight" type="{http://www.w3.org/2001/XMLSchema}boolean" default="true" />
 *       &lt;attribute name="showOutlineSymbols" type="{http://www.w3.org/2001/XMLSchema}boolean" default="true" />
 *     &lt;/restriction>
 *   &lt;/complexContent>
 * &lt;/complexType>
 * </pre>
 * 
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "CT_OutlinePr")
public class CTOutlinePr {

    @XmlAttribute
    protected Boolean applyStyles;
    @XmlAttribute
    protected Boolean summaryBelow;
    @XmlAttribute
    protected Boolean summaryRight;
    @XmlAttribute
    protected Boolean showOutlineSymbols;

    /**
     * Gets the value of the applyStyles property.
     * 
     * @return
     *     possible object is
     *     {@link Boolean }
     *     
     */
    public boolean isApplyStyles() {
        if (applyStyles == null) {
            return false;
        } else {
            return applyStyles;
        }
    }

    /**
     * Sets the value of the applyStyles property.
     * 
     * @param value
     *     allowed object is
     *     {@link Boolean }
     *     
     */
    public void setApplyStyles(Boolean value) {
        this.applyStyles = value;
    }

    /**
     * Gets the value of the summaryBelow property.
     * 
     * @return
     *     possible object is
     *     {@link Boolean }
     *     
     */
    public boolean isSummaryBelow() {
        if (summaryBelow == null) {
            return true;
        } else {
            return summaryBelow;
        }
    }

    /**
     * Sets the value of the summaryBelow property.
     * 
     * @param value
     *     allowed object is
     *     {@link Boolean }
     *     
     */
    public void setSummaryBelow(Boolean value) {
        this.summaryBelow = value;
    }

    /**
     * Gets the value of the summaryRight property.
     * 
     * @return
     *     possible object is
     *     {@link Boolean }
     *     
     */
    public boolean isSummaryRight() {
        if (summaryRight == null) {
            return true;
        } else {
            return summaryRight;
        }
    }

    /**
     * Sets the value of the summaryRight property.
     * 
     * @param value
     *     allowed object is
     *     {@link Boolean }
     *     
     */
    public void setSummaryRight(Boolean value) {
        this.summaryRight = value;
    }

    /**
     * Gets the value of the showOutlineSymbols property.
     * 
     * @return
     *     possible object is
     *     {@link Boolean }
     *     
     */
    public boolean isShowOutlineSymbols() {
        if (showOutlineSymbols == null) {
            return true;
        } else {
            return showOutlineSymbols;
        }
    }

    /**
     * Sets the value of the showOutlineSymbols property.
     * 
     * @param value
     *     allowed object is
     *     {@link Boolean }
     *     
     */
    public void setShowOutlineSymbols(Boolean value) {
        this.showOutlineSymbols = value;
    }

}
