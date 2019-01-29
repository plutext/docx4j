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
import javax.xml.bind.annotation.XmlSchemaType;
import javax.xml.bind.annotation.XmlType;
import javax.xml.bind.annotation.adapters.CollapsedStringAdapter;
import javax.xml.bind.annotation.adapters.XmlJavaTypeAdapter;


/**
 * <p>Java class for CT_TLBuildDiagram complex type.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
 * 
 * <pre>
 * &lt;complexType name="CT_TLBuildDiagram">
 *   &lt;complexContent>
 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
 *       &lt;attGroup ref="{http://schemas.openxmlformats.org/presentationml/2006/main}AG_TLBuild"/>
 *       &lt;attribute name="bld" type="{http://schemas.openxmlformats.org/presentationml/2006/main}ST_TLDiagramBuildType" default="whole" />
 *     &lt;/restriction>
 *   &lt;/complexContent>
 * &lt;/complexType>
 * </pre>
 * 
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "CT_TLBuildDiagram")
public class CTTLBuildDiagram {

    @XmlAttribute(name = "bld")
    protected STTLDiagramBuildType bld;
    @XmlAttribute(name = "spid", required = true)
    @XmlJavaTypeAdapter(CollapsedStringAdapter.class)
    protected String spid;
    @XmlAttribute(name = "grpId", required = true)
    @XmlSchemaType(name = "unsignedInt")
    protected long grpId;
    @XmlAttribute(name = "uiExpand")
    protected Boolean uiExpand;

    /**
     * Gets the value of the bld property.
     * 
     * @return
     *     possible object is
     *     {@link STTLDiagramBuildType }
     *     
     */
    public STTLDiagramBuildType getBld() {
        if (bld == null) {
            return STTLDiagramBuildType.WHOLE;
        } else {
            return bld;
        }
    }

    /**
     * Sets the value of the bld property.
     * 
     * @param value
     *     allowed object is
     *     {@link STTLDiagramBuildType }
     *     
     */
    public void setBld(STTLDiagramBuildType value) {
        this.bld = value;
    }

    /**
     * Gets the value of the spid property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getSpid() {
        return spid;
    }

    /**
     * Sets the value of the spid property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setSpid(String value) {
        this.spid = value;
    }

    /**
     * Gets the value of the grpId property.
     * 
     */
    public long getGrpId() {
        return grpId;
    }

    /**
     * Sets the value of the grpId property.
     * 
     */
    public void setGrpId(long value) {
        this.grpId = value;
    }

    /**
     * Gets the value of the uiExpand property.
     * 
     * @return
     *     possible object is
     *     {@link Boolean }
     *     
     */
    public boolean isUiExpand() {
        if (uiExpand == null) {
            return false;
        } else {
            return uiExpand;
        }
    }

    /**
     * Sets the value of the uiExpand property.
     * 
     * @param value
     *     allowed object is
     *     {@link Boolean }
     *     
     */
    public void setUiExpand(Boolean value) {
        this.uiExpand = value;
    }

}
