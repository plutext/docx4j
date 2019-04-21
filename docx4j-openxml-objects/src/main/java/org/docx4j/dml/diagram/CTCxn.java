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
import javax.xml.bind.annotation.XmlSchemaType;
import javax.xml.bind.annotation.XmlType;
import org.docx4j.dml.CTOfficeArtExtensionList;


/**
 * <p>Java class for CT_Cxn complex type.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
 * 
 * <pre>
 * &lt;complexType name="CT_Cxn">
 *   &lt;complexContent>
 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
 *       &lt;sequence>
 *         &lt;element name="extLst" type="{http://schemas.openxmlformats.org/drawingml/2006/main}CT_OfficeArtExtensionList" minOccurs="0"/>
 *       &lt;/sequence>
 *       &lt;attribute name="modelId" use="required" type="{http://schemas.openxmlformats.org/drawingml/2006/diagram}ST_ModelId" />
 *       &lt;attribute name="type" type="{http://schemas.openxmlformats.org/drawingml/2006/diagram}ST_CxnType" default="parOf" />
 *       &lt;attribute name="srcId" use="required" type="{http://schemas.openxmlformats.org/drawingml/2006/diagram}ST_ModelId" />
 *       &lt;attribute name="destId" use="required" type="{http://schemas.openxmlformats.org/drawingml/2006/diagram}ST_ModelId" />
 *       &lt;attribute name="srcOrd" use="required" type="{http://www.w3.org/2001/XMLSchema}unsignedInt" />
 *       &lt;attribute name="destOrd" use="required" type="{http://www.w3.org/2001/XMLSchema}unsignedInt" />
 *       &lt;attribute name="parTransId" type="{http://schemas.openxmlformats.org/drawingml/2006/diagram}ST_ModelId" default="0" />
 *       &lt;attribute name="sibTransId" type="{http://schemas.openxmlformats.org/drawingml/2006/diagram}ST_ModelId" default="0" />
 *       &lt;attribute name="presId" type="{http://www.w3.org/2001/XMLSchema}string" default="" />
 *     &lt;/restriction>
 *   &lt;/complexContent>
 * &lt;/complexType>
 * </pre>
 * 
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "CT_Cxn", propOrder = {
    "extLst"
})
public class CTCxn {

    protected CTOfficeArtExtensionList extLst;
    @XmlAttribute(required = true)
    protected String modelId;
    @XmlAttribute
    protected STCxnType type;
    @XmlAttribute(required = true)
    protected String srcId;
    @XmlAttribute(required = true)
    protected String destId;
    @XmlAttribute(required = true)
    @XmlSchemaType(name = "unsignedInt")
    protected long srcOrd;
    @XmlAttribute(required = true)
    @XmlSchemaType(name = "unsignedInt")
    protected long destOrd;
    @XmlAttribute
    protected String parTransId;
    @XmlAttribute
    protected String sibTransId;
    @XmlAttribute
    protected String presId;

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
     *     {@link STCxnType }
     *     
     */
    public STCxnType getType() {
        if (type == null) {
            return STCxnType.PAR_OF;
        } else {
            return type;
        }
    }

    /**
     * Sets the value of the type property.
     * 
     * @param value
     *     allowed object is
     *     {@link STCxnType }
     *     
     */
    public void setType(STCxnType value) {
        this.type = value;
    }

    /**
     * Gets the value of the srcId property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getSrcId() {
        return srcId;
    }

    /**
     * Sets the value of the srcId property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setSrcId(String value) {
        this.srcId = value;
    }

    /**
     * Gets the value of the destId property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getDestId() {
        return destId;
    }

    /**
     * Sets the value of the destId property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setDestId(String value) {
        this.destId = value;
    }

    /**
     * Gets the value of the srcOrd property.
     * 
     */
    public long getSrcOrd() {
        return srcOrd;
    }

    /**
     * Sets the value of the srcOrd property.
     * 
     */
    public void setSrcOrd(long value) {
        this.srcOrd = value;
    }

    /**
     * Gets the value of the destOrd property.
     * 
     */
    public long getDestOrd() {
        return destOrd;
    }

    /**
     * Sets the value of the destOrd property.
     * 
     */
    public void setDestOrd(long value) {
        this.destOrd = value;
    }

    /**
     * Gets the value of the parTransId property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getParTransId() {
        if (parTransId == null) {
            return "0";
        } else {
            return parTransId;
        }
    }

    /**
     * Sets the value of the parTransId property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setParTransId(String value) {
        this.parTransId = value;
    }

    /**
     * Gets the value of the sibTransId property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getSibTransId() {
        if (sibTransId == null) {
            return "0";
        } else {
            return sibTransId;
        }
    }

    /**
     * Sets the value of the sibTransId property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setSibTransId(String value) {
        this.sibTransId = value;
    }

    /**
     * Gets the value of the presId property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getPresId() {
        if (presId == null) {
            return "";
        } else {
            return presId;
        }
    }

    /**
     * Sets the value of the presId property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setPresId(String value) {
        this.presId = value;
    }

}
