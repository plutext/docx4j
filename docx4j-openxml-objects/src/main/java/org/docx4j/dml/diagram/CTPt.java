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
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlType;
import org.docx4j.dml.CTOfficeArtExtensionList;
import org.docx4j.dml.CTShapeProperties;
import org.docx4j.dml.CTTextBody;


/**
 * <p>Java class for CT_Pt complex type.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
 * 
 * <pre>
 * &lt;complexType name="CT_Pt">
 *   &lt;complexContent>
 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
 *       &lt;sequence>
 *         &lt;element name="prSet" type="{http://schemas.openxmlformats.org/drawingml/2006/diagram}CT_ElemPropSet" minOccurs="0"/>
 *         &lt;element name="spPr" type="{http://schemas.openxmlformats.org/drawingml/2006/main}CT_ShapeProperties" minOccurs="0"/>
 *         &lt;element name="t" type="{http://schemas.openxmlformats.org/drawingml/2006/main}CT_TextBody" minOccurs="0"/>
 *         &lt;element name="extLst" type="{http://schemas.openxmlformats.org/drawingml/2006/main}CT_OfficeArtExtensionList" minOccurs="0"/>
 *       &lt;/sequence>
 *       &lt;attribute name="modelId" use="required" type="{http://schemas.openxmlformats.org/drawingml/2006/diagram}ST_ModelId" />
 *       &lt;attribute name="type" type="{http://schemas.openxmlformats.org/drawingml/2006/diagram}ST_PtType" default="node" />
 *       &lt;attribute name="cxnId" type="{http://schemas.openxmlformats.org/drawingml/2006/diagram}ST_ModelId" default="0" />
 *     &lt;/restriction>
 *   &lt;/complexContent>
 * &lt;/complexType>
 * </pre>
 * 
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "CT_Pt", propOrder = {
    "prSet",
    "spPr",
    "t",
    "extLst"
})
public class CTPt {

    protected CTElemPropSet prSet;
    protected CTShapeProperties spPr;
    protected CTTextBody t;
    protected CTOfficeArtExtensionList extLst;
    @XmlAttribute(required = true)
    protected String modelId;
    @XmlAttribute
    protected STPtType type;
    @XmlAttribute
    protected String cxnId;

    /**
     * Gets the value of the prSet property.
     * 
     * @return
     *     possible object is
     *     {@link CTElemPropSet }
     *     
     */
    public CTElemPropSet getPrSet() {
        return prSet;
    }

    /**
     * Sets the value of the prSet property.
     * 
     * @param value
     *     allowed object is
     *     {@link CTElemPropSet }
     *     
     */
    public void setPrSet(CTElemPropSet value) {
        this.prSet = value;
    }

    /**
     * Gets the value of the spPr property.
     * 
     * @return
     *     possible object is
     *     {@link CTShapeProperties }
     *     
     */
    public CTShapeProperties getSpPr() {
        return spPr;
    }

    /**
     * Sets the value of the spPr property.
     * 
     * @param value
     *     allowed object is
     *     {@link CTShapeProperties }
     *     
     */
    public void setSpPr(CTShapeProperties value) {
        this.spPr = value;
    }

    /**
     * Gets the value of the t property.
     * 
     * @return
     *     possible object is
     *     {@link CTTextBody }
     *     
     */
    public CTTextBody getT() {
        return t;
    }

    /**
     * Sets the value of the t property.
     * 
     * @param value
     *     allowed object is
     *     {@link CTTextBody }
     *     
     */
    public void setT(CTTextBody value) {
        this.t = value;
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

    /**
     * Gets the value of the modelId property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getModelId() {
        return modelId;
    }

    /**
     * Sets the value of the modelId property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setModelId(String value) {
        this.modelId = value;
    }

    /**
     * Gets the value of the type property.
     * 
     * @return
     *     possible object is
     *     {@link STPtType }
     *     
     */
    public STPtType getType() {
        if (type == null) {
            return STPtType.NODE;
        } else {
            return type;
        }
    }

    /**
     * Sets the value of the type property.
     * 
     * @param value
     *     allowed object is
     *     {@link STPtType }
     *     
     */
    public void setType(STPtType value) {
        this.type = value;
    }

    /**
     * Gets the value of the cxnId property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getCxnId() {
        if (cxnId == null) {
            return "0";
        } else {
            return cxnId;
        }
    }

    /**
     * Sets the value of the cxnId property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setCxnId(String value) {
        this.cxnId = value;
    }

}
