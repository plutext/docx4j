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


package org.docx4j.dml.diagram;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlType;
import org.docx4j.dml.CTBackgroundFormatting;
import org.docx4j.dml.CTOfficeArtExtensionList;
import org.docx4j.dml.CTWholeE2OFormatting;


/**
 * <p>Java class for CT_DataModel complex type.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
 * 
 * <pre>
 * &lt;complexType name="CT_DataModel">
 *   &lt;complexContent>
 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
 *       &lt;sequence>
 *         &lt;element name="ptLst" type="{http://schemas.openxmlformats.org/drawingml/2006/diagram}CT_PtList"/>
 *         &lt;element name="cxnLst" type="{http://schemas.openxmlformats.org/drawingml/2006/diagram}CT_CxnList" minOccurs="0"/>
 *         &lt;element name="bg" type="{http://schemas.openxmlformats.org/drawingml/2006/main}CT_BackgroundFormatting" minOccurs="0"/>
 *         &lt;element name="whole" type="{http://schemas.openxmlformats.org/drawingml/2006/main}CT_WholeE2oFormatting" minOccurs="0"/>
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
@XmlType(name = "CT_DataModel", propOrder = {
    "ptLst",
    "cxnLst",
    "bg",
    "whole",
    "extLst"
})
@XmlRootElement(name = "dataModel")
public class CTDataModel {

    @XmlElement(required = true)
    protected CTPtList ptLst;
    protected CTCxnList cxnLst;
    protected CTBackgroundFormatting bg;
    protected CTWholeE2OFormatting whole;
    protected CTOfficeArtExtensionList extLst;

    /**
     * Gets the value of the ptLst property.
     * 
     * @return
     *     possible object is
     *     {@link CTPtList }
     *     
     */
    public CTPtList getPtLst() {
        return ptLst;
    }

    /**
     * Sets the value of the ptLst property.
     * 
     * @param value
     *     allowed object is
     *     {@link CTPtList }
     *     
     */
    public void setPtLst(CTPtList value) {
        this.ptLst = value;
    }

    /**
     * Gets the value of the cxnLst property.
     * 
     * @return
     *     possible object is
     *     {@link CTCxnList }
     *     
     */
    public CTCxnList getCxnLst() {
        return cxnLst;
    }

    /**
     * Sets the value of the cxnLst property.
     * 
     * @param value
     *     allowed object is
     *     {@link CTCxnList }
     *     
     */
    public void setCxnLst(CTCxnList value) {
        this.cxnLst = value;
    }

    /**
     * Gets the value of the bg property.
     * 
     * @return
     *     possible object is
     *     {@link CTBackgroundFormatting }
     *     
     */
    public CTBackgroundFormatting getBg() {
        return bg;
    }

    /**
     * Sets the value of the bg property.
     * 
     * @param value
     *     allowed object is
     *     {@link CTBackgroundFormatting }
     *     
     */
    public void setBg(CTBackgroundFormatting value) {
        this.bg = value;
    }

    /**
     * Gets the value of the whole property.
     * 
     * @return
     *     possible object is
     *     {@link CTWholeE2OFormatting }
     *     
     */
    public CTWholeE2OFormatting getWhole() {
        return whole;
    }

    /**
     * Sets the value of the whole property.
     * 
     * @param value
     *     allowed object is
     *     {@link CTWholeE2OFormatting }
     *     
     */
    public void setWhole(CTWholeE2OFormatting value) {
        this.whole = value;
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
