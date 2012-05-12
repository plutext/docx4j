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
 * <p>Java class for CT_SheetPr complex type.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
 * 
 * <pre>
 * &lt;complexType name="CT_SheetPr">
 *   &lt;complexContent>
 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
 *       &lt;sequence>
 *         &lt;element name="tabColor" type="{http://schemas.openxmlformats.org/spreadsheetml/2006/main}CT_Color" minOccurs="0"/>
 *         &lt;element name="outlinePr" type="{http://schemas.openxmlformats.org/spreadsheetml/2006/main}CT_OutlinePr" minOccurs="0"/>
 *         &lt;element name="pageSetUpPr" type="{http://schemas.openxmlformats.org/spreadsheetml/2006/main}CT_PageSetUpPr" minOccurs="0"/>
 *       &lt;/sequence>
 *       &lt;attribute name="syncHorizontal" type="{http://www.w3.org/2001/XMLSchema}boolean" default="false" />
 *       &lt;attribute name="syncVertical" type="{http://www.w3.org/2001/XMLSchema}boolean" default="false" />
 *       &lt;attribute name="syncRef" type="{http://schemas.openxmlformats.org/spreadsheetml/2006/main}ST_Ref" />
 *       &lt;attribute name="transitionEvaluation" type="{http://www.w3.org/2001/XMLSchema}boolean" default="false" />
 *       &lt;attribute name="transitionEntry" type="{http://www.w3.org/2001/XMLSchema}boolean" default="false" />
 *       &lt;attribute name="published" type="{http://www.w3.org/2001/XMLSchema}boolean" default="true" />
 *       &lt;attribute name="codeName" type="{http://www.w3.org/2001/XMLSchema}string" />
 *       &lt;attribute name="filterMode" type="{http://www.w3.org/2001/XMLSchema}boolean" default="false" />
 *       &lt;attribute name="enableFormatConditionsCalculation" type="{http://www.w3.org/2001/XMLSchema}boolean" default="true" />
 *     &lt;/restriction>
 *   &lt;/complexContent>
 * &lt;/complexType>
 * </pre>
 * 
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "CT_SheetPr", propOrder = {
    "tabColor",
    "outlinePr",
    "pageSetUpPr"
})
public class CTSheetPr {

    protected CTColor tabColor;
    protected CTOutlinePr outlinePr;
    protected CTPageSetUpPr pageSetUpPr;
    @XmlAttribute
    protected Boolean syncHorizontal;
    @XmlAttribute
    protected Boolean syncVertical;
    @XmlAttribute
    protected String syncRef;
    @XmlAttribute
    protected Boolean transitionEvaluation;
    @XmlAttribute
    protected Boolean transitionEntry;
    @XmlAttribute
    protected Boolean published;
    @XmlAttribute
    protected String codeName;
    @XmlAttribute
    protected Boolean filterMode;
    @XmlAttribute
    protected Boolean enableFormatConditionsCalculation;

    /**
     * Gets the value of the tabColor property.
     * 
     * @return
     *     possible object is
     *     {@link CTColor }
     *     
     */
    public CTColor getTabColor() {
        return tabColor;
    }

    /**
     * Sets the value of the tabColor property.
     * 
     * @param value
     *     allowed object is
     *     {@link CTColor }
     *     
     */
    public void setTabColor(CTColor value) {
        this.tabColor = value;
    }

    /**
     * Gets the value of the outlinePr property.
     * 
     * @return
     *     possible object is
     *     {@link CTOutlinePr }
     *     
     */
    public CTOutlinePr getOutlinePr() {
        return outlinePr;
    }

    /**
     * Sets the value of the outlinePr property.
     * 
     * @param value
     *     allowed object is
     *     {@link CTOutlinePr }
     *     
     */
    public void setOutlinePr(CTOutlinePr value) {
        this.outlinePr = value;
    }

    /**
     * Gets the value of the pageSetUpPr property.
     * 
     * @return
     *     possible object is
     *     {@link CTPageSetUpPr }
     *     
     */
    public CTPageSetUpPr getPageSetUpPr() {
        return pageSetUpPr;
    }

    /**
     * Sets the value of the pageSetUpPr property.
     * 
     * @param value
     *     allowed object is
     *     {@link CTPageSetUpPr }
     *     
     */
    public void setPageSetUpPr(CTPageSetUpPr value) {
        this.pageSetUpPr = value;
    }

    /**
     * Gets the value of the syncHorizontal property.
     * 
     * @return
     *     possible object is
     *     {@link Boolean }
     *     
     */
    public boolean isSyncHorizontal() {
        if (syncHorizontal == null) {
            return false;
        } else {
            return syncHorizontal;
        }
    }

    /**
     * Sets the value of the syncHorizontal property.
     * 
     * @param value
     *     allowed object is
     *     {@link Boolean }
     *     
     */
    public void setSyncHorizontal(Boolean value) {
        this.syncHorizontal = value;
    }

    /**
     * Gets the value of the syncVertical property.
     * 
     * @return
     *     possible object is
     *     {@link Boolean }
     *     
     */
    public boolean isSyncVertical() {
        if (syncVertical == null) {
            return false;
        } else {
            return syncVertical;
        }
    }

    /**
     * Sets the value of the syncVertical property.
     * 
     * @param value
     *     allowed object is
     *     {@link Boolean }
     *     
     */
    public void setSyncVertical(Boolean value) {
        this.syncVertical = value;
    }

    /**
     * Gets the value of the syncRef property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getSyncRef() {
        return syncRef;
    }

    /**
     * Sets the value of the syncRef property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setSyncRef(String value) {
        this.syncRef = value;
    }

    /**
     * Gets the value of the transitionEvaluation property.
     * 
     * @return
     *     possible object is
     *     {@link Boolean }
     *     
     */
    public boolean isTransitionEvaluation() {
        if (transitionEvaluation == null) {
            return false;
        } else {
            return transitionEvaluation;
        }
    }

    /**
     * Sets the value of the transitionEvaluation property.
     * 
     * @param value
     *     allowed object is
     *     {@link Boolean }
     *     
     */
    public void setTransitionEvaluation(Boolean value) {
        this.transitionEvaluation = value;
    }

    /**
     * Gets the value of the transitionEntry property.
     * 
     * @return
     *     possible object is
     *     {@link Boolean }
     *     
     */
    public boolean isTransitionEntry() {
        if (transitionEntry == null) {
            return false;
        } else {
            return transitionEntry;
        }
    }

    /**
     * Sets the value of the transitionEntry property.
     * 
     * @param value
     *     allowed object is
     *     {@link Boolean }
     *     
     */
    public void setTransitionEntry(Boolean value) {
        this.transitionEntry = value;
    }

    /**
     * Gets the value of the published property.
     * 
     * @return
     *     possible object is
     *     {@link Boolean }
     *     
     */
    public boolean isPublished() {
        if (published == null) {
            return true;
        } else {
            return published;
        }
    }

    /**
     * Sets the value of the published property.
     * 
     * @param value
     *     allowed object is
     *     {@link Boolean }
     *     
     */
    public void setPublished(Boolean value) {
        this.published = value;
    }

    /**
     * Gets the value of the codeName property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getCodeName() {
        return codeName;
    }

    /**
     * Sets the value of the codeName property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setCodeName(String value) {
        this.codeName = value;
    }

    /**
     * Gets the value of the filterMode property.
     * 
     * @return
     *     possible object is
     *     {@link Boolean }
     *     
     */
    public boolean isFilterMode() {
        if (filterMode == null) {
            return false;
        } else {
            return filterMode;
        }
    }

    /**
     * Sets the value of the filterMode property.
     * 
     * @param value
     *     allowed object is
     *     {@link Boolean }
     *     
     */
    public void setFilterMode(Boolean value) {
        this.filterMode = value;
    }

    /**
     * Gets the value of the enableFormatConditionsCalculation property.
     * 
     * @return
     *     possible object is
     *     {@link Boolean }
     *     
     */
    public boolean isEnableFormatConditionsCalculation() {
        if (enableFormatConditionsCalculation == null) {
            return true;
        } else {
            return enableFormatConditionsCalculation;
        }
    }

    /**
     * Sets the value of the enableFormatConditionsCalculation property.
     * 
     * @param value
     *     allowed object is
     *     {@link Boolean }
     *     
     */
    public void setEnableFormatConditionsCalculation(Boolean value) {
        this.enableFormatConditionsCalculation = value;
    }

}
