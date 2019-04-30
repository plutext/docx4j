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

import javax.xml.bind.Unmarshaller;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlSchemaType;
import javax.xml.bind.annotation.XmlTransient;
import javax.xml.bind.annotation.XmlType;
import org.docx4j.dml.CTOfficeArtExtensionList;
import org.jvnet.jaxb2_commons.ppp.Child;


/**
 * <p>Java class for CT_Cxn complex type.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
 * 
 * <pre>
 * &lt;complexType name="CT_Cxn"&gt;
 *   &lt;complexContent&gt;
 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType"&gt;
 *       &lt;sequence&gt;
 *         &lt;element name="extLst" type="{http://schemas.openxmlformats.org/drawingml/2006/main}CT_OfficeArtExtensionList" minOccurs="0"/&gt;
 *       &lt;/sequence&gt;
 *       &lt;attribute name="modelId" use="required" type="{http://schemas.openxmlformats.org/drawingml/2006/diagram}ST_ModelId" /&gt;
 *       &lt;attribute name="type" type="{http://schemas.openxmlformats.org/drawingml/2006/diagram}ST_CxnType" default="parOf" /&gt;
 *       &lt;attribute name="srcId" use="required" type="{http://schemas.openxmlformats.org/drawingml/2006/diagram}ST_ModelId" /&gt;
 *       &lt;attribute name="destId" use="required" type="{http://schemas.openxmlformats.org/drawingml/2006/diagram}ST_ModelId" /&gt;
 *       &lt;attribute name="srcOrd" use="required" type="{http://www.w3.org/2001/XMLSchema}unsignedInt" /&gt;
 *       &lt;attribute name="destOrd" use="required" type="{http://www.w3.org/2001/XMLSchema}unsignedInt" /&gt;
 *       &lt;attribute name="parTransId" type="{http://schemas.openxmlformats.org/drawingml/2006/diagram}ST_ModelId" default="0" /&gt;
 *       &lt;attribute name="sibTransId" type="{http://schemas.openxmlformats.org/drawingml/2006/diagram}ST_ModelId" default="0" /&gt;
 *       &lt;attribute name="presId" type="{http://www.w3.org/2001/XMLSchema}string" default="" /&gt;
 *     &lt;/restriction&gt;
 *   &lt;/complexContent&gt;
 * &lt;/complexType&gt;
 * </pre>
 * 
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "CT_Cxn", propOrder = {
    "extLst"
})
public class CTCxn implements Child
{

    protected CTOfficeArtExtensionList extLst;
    @XmlAttribute(name = "modelId", required = true)
    protected String modelId;
    @XmlAttribute(name = "type")
    protected STCxnType type;
    @XmlAttribute(name = "srcId", required = true)
    protected String srcId;
    @XmlAttribute(name = "destId", required = true)
    protected String destId;
    @XmlAttribute(name = "srcOrd", required = true)
    @XmlSchemaType(name = "unsignedInt")
    protected long srcOrd;
    @XmlAttribute(name = "destOrd", required = true)
    @XmlSchemaType(name = "unsignedInt")
    protected long destOrd;
    @XmlAttribute(name = "parTransId")
    protected String parTransId;
    @XmlAttribute(name = "sibTransId")
    protected String sibTransId;
    @XmlAttribute(name = "presId")
    protected String presId;
    @XmlTransient
    private Object parent;

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
