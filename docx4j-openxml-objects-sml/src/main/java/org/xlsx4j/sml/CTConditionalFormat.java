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
import javax.xml.bind.annotation.XmlSchemaType;
import javax.xml.bind.annotation.XmlTransient;
import javax.xml.bind.annotation.XmlType;
import org.jvnet.jaxb2_commons.ppp.Child;


/**
 * <p>Java class for CT_ConditionalFormat complex type.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
 * 
 * <pre>
 * &lt;complexType name="CT_ConditionalFormat">
 *   &lt;complexContent>
 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
 *       &lt;sequence>
 *         &lt;element name="pivotAreas" type="{http://schemas.openxmlformats.org/spreadsheetml/2006/main}CT_PivotAreas"/>
 *         &lt;element name="extLst" type="{http://schemas.openxmlformats.org/spreadsheetml/2006/main}CT_ExtensionList" minOccurs="0"/>
 *       &lt;/sequence>
 *       &lt;attribute name="scope" type="{http://schemas.openxmlformats.org/spreadsheetml/2006/main}ST_Scope" default="selection" />
 *       &lt;attribute name="type" type="{http://schemas.openxmlformats.org/spreadsheetml/2006/main}ST_Type" default="none" />
 *       &lt;attribute name="priority" use="required" type="{http://www.w3.org/2001/XMLSchema}unsignedInt" />
 *     &lt;/restriction>
 *   &lt;/complexContent>
 * &lt;/complexType>
 * </pre>
 * 
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "CT_ConditionalFormat", propOrder = {
    "pivotAreas",
    "extLst"
})
public class CTConditionalFormat implements Child
{

    @XmlElement(required = true)
    protected CTPivotAreas pivotAreas;
    protected CTExtensionList extLst;
    @XmlAttribute(name = "scope")
    protected STScope scope;
    @XmlAttribute(name = "type")
    protected STType type;
    @XmlAttribute(name = "priority", required = true)
    @XmlSchemaType(name = "unsignedInt")
    protected long priority;
    @XmlTransient
    private Object parent;

    /**
     * Gets the value of the pivotAreas property.
     * 
     * @return
     *     possible object is
     *     {@link CTPivotAreas }
     *     
     */
    public CTPivotAreas getPivotAreas() {
        return pivotAreas;
    }

    /**
     * Sets the value of the pivotAreas property.
     * 
     * @param value
     *     allowed object is
     *     {@link CTPivotAreas }
     *     
     */
    public void setPivotAreas(CTPivotAreas value) {
        this.pivotAreas = value;
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
     * Gets the value of the scope property.
     * 
     * @return
     *     possible object is
     *     {@link STScope }
     *     
     */
    public STScope getScope() {
        if (scope == null) {
            return STScope.SELECTION;
        } else {
            return scope;
        }
    }

    /**
     * Sets the value of the scope property.
     * 
     * @param value
     *     allowed object is
     *     {@link STScope }
     *     
     */
    public void setScope(STScope value) {
        this.scope = value;
    }

    /**
     * Gets the value of the type property.
     * 
     * @return
     *     possible object is
     *     {@link STType }
     *     
     */
    public STType getType() {
        if (type == null) {
            return STType.NONE;
        } else {
            return type;
        }
    }

    /**
     * Sets the value of the type property.
     * 
     * @param value
     *     allowed object is
     *     {@link STType }
     *     
     */
    public void setType(STType value) {
        this.type = value;
    }

    /**
     * Gets the value of the priority property.
     * 
     */
    public long getPriority() {
        return priority;
    }

    /**
     * Sets the value of the priority property.
     * 
     */
    public void setPriority(long value) {
        this.priority = value;
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
