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
import javax.xml.bind.annotation.XmlSchemaType;
import javax.xml.bind.annotation.XmlTransient;
import javax.xml.bind.annotation.XmlType;
import org.jvnet.jaxb2_commons.ppp.Child;


/**
 * <p>Java class for CT_CacheSource complex type.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
 * 
 * <pre>
 * &lt;complexType name="CT_CacheSource">
 *   &lt;complexContent>
 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
 *       &lt;choice minOccurs="0">
 *         &lt;element name="worksheetSource" type="{http://schemas.openxmlformats.org/spreadsheetml/2006/main}CT_WorksheetSource"/>
 *         &lt;element name="consolidation" type="{http://schemas.openxmlformats.org/spreadsheetml/2006/main}CT_Consolidation"/>
 *         &lt;element name="extLst" type="{http://schemas.openxmlformats.org/spreadsheetml/2006/main}CT_ExtensionList" minOccurs="0"/>
 *       &lt;/choice>
 *       &lt;attribute name="type" use="required" type="{http://schemas.openxmlformats.org/spreadsheetml/2006/main}ST_SourceType" />
 *       &lt;attribute name="connectionId" type="{http://www.w3.org/2001/XMLSchema}unsignedInt" default="0" />
 *     &lt;/restriction>
 *   &lt;/complexContent>
 * &lt;/complexType>
 * </pre>
 * 
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "CT_CacheSource", propOrder = {
    "worksheetSource",
    "consolidation",
    "extLst"
})
public class CTCacheSource implements Child
{

    protected CTWorksheetSource worksheetSource;
    protected CTConsolidation consolidation;
    protected CTExtensionList extLst;
    @XmlAttribute(name = "type", required = true)
    protected STSourceType type;
    @XmlAttribute(name = "connectionId")
    @XmlSchemaType(name = "unsignedInt")
    protected Long connectionId;
    @XmlTransient
    private Object parent;

    /**
     * Gets the value of the worksheetSource property.
     * 
     * @return
     *     possible object is
     *     {@link CTWorksheetSource }
     *     
     */
    public CTWorksheetSource getWorksheetSource() {
        return worksheetSource;
    }

    /**
     * Sets the value of the worksheetSource property.
     * 
     * @param value
     *     allowed object is
     *     {@link CTWorksheetSource }
     *     
     */
    public void setWorksheetSource(CTWorksheetSource value) {
        this.worksheetSource = value;
    }

    /**
     * Gets the value of the consolidation property.
     * 
     * @return
     *     possible object is
     *     {@link CTConsolidation }
     *     
     */
    public CTConsolidation getConsolidation() {
        return consolidation;
    }

    /**
     * Sets the value of the consolidation property.
     * 
     * @param value
     *     allowed object is
     *     {@link CTConsolidation }
     *     
     */
    public void setConsolidation(CTConsolidation value) {
        this.consolidation = value;
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
     * Gets the value of the type property.
     * 
     * @return
     *     possible object is
     *     {@link STSourceType }
     *     
     */
    public STSourceType getType() {
        return type;
    }

    /**
     * Sets the value of the type property.
     * 
     * @param value
     *     allowed object is
     *     {@link STSourceType }
     *     
     */
    public void setType(STSourceType value) {
        this.type = value;
    }

    /**
     * Gets the value of the connectionId property.
     * 
     * @return
     *     possible object is
     *     {@link Long }
     *     
     */
    public long getConnectionId() {
        if (connectionId == null) {
            return  0L;
        } else {
            return connectionId;
        }
    }

    /**
     * Sets the value of the connectionId property.
     * 
     * @param value
     *     allowed object is
     *     {@link Long }
     *     
     */
    public void setConnectionId(Long value) {
        this.connectionId = value;
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
