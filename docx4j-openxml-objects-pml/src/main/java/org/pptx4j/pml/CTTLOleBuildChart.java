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

import javax.xml.bind.Unmarshaller;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlSchemaType;
import javax.xml.bind.annotation.XmlTransient;
import javax.xml.bind.annotation.XmlType;
import javax.xml.bind.annotation.adapters.CollapsedStringAdapter;
import javax.xml.bind.annotation.adapters.XmlJavaTypeAdapter;

import org.jvnet.jaxb2_commons.ppp.Child;


/**
 * <p>Java class for CT_TLOleBuildChart complex type.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
 * 
 * <pre>
 * &lt;complexType name="CT_TLOleBuildChart"&gt;
 *   &lt;complexContent&gt;
 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType"&gt;
 *       &lt;attGroup ref="{http://schemas.openxmlformats.org/presentationml/2006/main}AG_TLBuild"/&gt;
 *       &lt;attribute name="bld" type="{http://schemas.openxmlformats.org/presentationml/2006/main}ST_TLOleChartBuildType" default="allAtOnce" /&gt;
 *       &lt;attribute name="animBg" type="{http://www.w3.org/2001/XMLSchema}boolean" default="true" /&gt;
 *     &lt;/restriction&gt;
 *   &lt;/complexContent&gt;
 * &lt;/complexType&gt;
 * </pre>
 * 
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "CT_TLOleBuildChart")
public class CTTLOleBuildChart implements Child
{

    @XmlAttribute(name = "bld")
    protected STTLOleChartBuildType bld;
    @XmlAttribute(name = "animBg")
    protected Boolean animBg;
    @XmlAttribute(name = "spid", required = true)
    @XmlJavaTypeAdapter(CollapsedStringAdapter.class)
    protected String spid;
    @XmlAttribute(name = "grpId", required = true)
    @XmlSchemaType(name = "unsignedInt")
    protected long grpId;
    @XmlAttribute(name = "uiExpand")
    protected Boolean uiExpand;
    @XmlTransient
    private Object parent;

    /**
     * Gets the value of the bld property.
     * 
     * @return
     *     possible object is
     *     {@link STTLOleChartBuildType }
     *     
     */
    public STTLOleChartBuildType getBld() {
        if (bld == null) {
            return STTLOleChartBuildType.ALL_AT_ONCE;
        } else {
            return bld;
        }
    }

    /**
     * Sets the value of the bld property.
     * 
     * @param value
     *     allowed object is
     *     {@link STTLOleChartBuildType }
     *     
     */
    public void setBld(STTLOleChartBuildType value) {
        this.bld = value;
    }

    /**
     * Gets the value of the animBg property.
     * 
     * @return
     *     possible object is
     *     {@link Boolean }
     *     
     */
    public boolean isAnimBg() {
        if (animBg == null) {
            return true;
        } else {
            return animBg;
        }
    }

    /**
     * Sets the value of the animBg property.
     * 
     * @param value
     *     allowed object is
     *     {@link Boolean }
     *     
     */
    public void setAnimBg(Boolean value) {
        this.animBg = value;
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
