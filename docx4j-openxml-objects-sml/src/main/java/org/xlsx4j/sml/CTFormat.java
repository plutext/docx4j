/*
 *  Copyright 2010-2013, Plutext Pty Ltd.
 *   
 *  This file is part of xlsx4j, a component of docx4j.

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

import javax.xml.bind.Unmarshaller;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlTransient;
import javax.xml.bind.annotation.XmlType;
import org.jvnet.jaxb2_commons.ppp.Child;


/**
 * <p>Java class for CT_Format complex type.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
 * 
 * <pre>
 * &lt;complexType name="CT_Format">
 *   &lt;complexContent>
 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
 *       &lt;sequence>
 *         &lt;element name="pivotArea" type="{http://schemas.openxmlformats.org/spreadsheetml/2006/main}CT_PivotArea"/>
 *         &lt;element name="extLst" type="{http://schemas.openxmlformats.org/spreadsheetml/2006/main}CT_ExtensionList" minOccurs="0"/>
 *       &lt;/sequence>
 *       &lt;attribute name="action" type="{http://schemas.openxmlformats.org/spreadsheetml/2006/main}ST_FormatAction" default="formatting" />
 *       &lt;attribute name="dxfId" type="{http://schemas.openxmlformats.org/spreadsheetml/2006/main}ST_DxfId" />
 *     &lt;/restriction>
 *   &lt;/complexContent>
 * &lt;/complexType>
 * </pre>
 * 
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "CT_Format", propOrder = {
    "pivotArea",
    "extLst"
})
public class CTFormat implements Child
{

    @XmlElement(required = true)
    protected CTPivotArea pivotArea;
    protected CTExtensionList extLst;
    @XmlAttribute(name = "action")
    protected STFormatAction action;
    @XmlAttribute(name = "dxfId")
    protected Long dxfId;
    @XmlTransient
    private Object parent;

    /**
     * Gets the value of the pivotArea property.
     * 
     * @return
     *     possible object is
     *     {@link CTPivotArea }
     *     
     */
    public CTPivotArea getPivotArea() {
        return pivotArea;
    }

    /**
     * Sets the value of the pivotArea property.
     * 
     * @param value
     *     allowed object is
     *     {@link CTPivotArea }
     *     
     */
    public void setPivotArea(CTPivotArea value) {
        this.pivotArea = value;
    }

    /**
     * Gets the value of the extLst property.
     * 
     * @return
     *     possible object is
     *     {@link CTExtensionList }
     *     
     */
    public CTExtensionList getExtLst() {
        return extLst;
    }

    /**
     * Sets the value of the extLst property.
     * 
     * @param value
     *     allowed object is
     *     {@link CTExtensionList }
     *     
     */
    public void setExtLst(CTExtensionList value) {
        this.extLst = value;
    }

    /**
     * Gets the value of the action property.
     * 
     * @return
     *     possible object is
     *     {@link STFormatAction }
     *     
     */
    public STFormatAction getAction() {
        if (action == null) {
            return STFormatAction.FORMATTING;
        } else {
            return action;
        }
    }

    /**
     * Sets the value of the action property.
     * 
     * @param value
     *     allowed object is
     *     {@link STFormatAction }
     *     
     */
    public void setAction(STFormatAction value) {
        this.action = value;
    }

    /**
     * Gets the value of the dxfId property.
     * 
     * @return
     *     possible object is
     *     {@link Long }
     *     
     */
    public Long getDxfId() {
        return dxfId;
    }

    /**
     * Sets the value of the dxfId property.
     * 
     * @param value
     *     allowed object is
     *     {@link Long }
     *     
     */
    public void setDxfId(Long value) {
        this.dxfId = value;
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
