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
import javax.xml.bind.annotation.XmlTransient;
import javax.xml.bind.annotation.XmlType;
import org.jvnet.jaxb2_commons.ppp.Child;


/**
 * <p>Java class for CT_TupleCache complex type.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
 * 
 * <pre>
 * &lt;complexType name="CT_TupleCache">
 *   &lt;complexContent>
 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
 *       &lt;sequence>
 *         &lt;element name="entries" type="{http://schemas.openxmlformats.org/spreadsheetml/2006/main}CT_PCDSDTCEntries" minOccurs="0"/>
 *         &lt;element name="sets" type="{http://schemas.openxmlformats.org/spreadsheetml/2006/main}CT_Sets" minOccurs="0"/>
 *         &lt;element name="queryCache" type="{http://schemas.openxmlformats.org/spreadsheetml/2006/main}CT_QueryCache" minOccurs="0"/>
 *         &lt;element name="serverFormats" type="{http://schemas.openxmlformats.org/spreadsheetml/2006/main}CT_ServerFormats" minOccurs="0"/>
 *         &lt;element name="extLst" type="{http://schemas.openxmlformats.org/spreadsheetml/2006/main}CT_ExtensionList" minOccurs="0"/>
 *       &lt;/sequence>
 *     &lt;/restriction>
 *   &lt;/complexContent>
 * &lt;/complexType>
 * </pre>
 * 
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "CT_TupleCache", propOrder = {
    "entries",
    "sets",
    "queryCache",
    "serverFormats",
    "extLst"
})
public class CTTupleCache implements Child
{

    protected CTPCDSDTCEntries entries;
    protected CTSets sets;
    protected CTQueryCache queryCache;
    protected CTServerFormats serverFormats;
    protected CTExtensionList extLst;
    @XmlTransient
    private Object parent;

    /**
     * Gets the value of the entries property.
     * 
     * @return
     *     possible object is
     *     {@link CTPCDSDTCEntries }
     *     
     */
    public CTPCDSDTCEntries getEntries() {
        return entries;
    }

    /**
     * Sets the value of the entries property.
     * 
     * @param value
     *     allowed object is
     *     {@link CTPCDSDTCEntries }
     *     
     */
    public void setEntries(CTPCDSDTCEntries value) {
        this.entries = value;
    }

    /**
     * Gets the value of the sets property.
     * 
     * @return
     *     possible object is
     *     {@link CTSets }
     *     
     */
    public CTSets getSets() {
        return sets;
    }

    /**
     * Sets the value of the sets property.
     * 
     * @param value
     *     allowed object is
     *     {@link CTSets }
     *     
     */
    public void setSets(CTSets value) {
        this.sets = value;
    }

    /**
     * Gets the value of the queryCache property.
     * 
     * @return
     *     possible object is
     *     {@link CTQueryCache }
     *     
     */
    public CTQueryCache getQueryCache() {
        return queryCache;
    }

    /**
     * Sets the value of the queryCache property.
     * 
     * @param value
     *     allowed object is
     *     {@link CTQueryCache }
     *     
     */
    public void setQueryCache(CTQueryCache value) {
        this.queryCache = value;
    }

    /**
     * Gets the value of the serverFormats property.
     * 
     * @return
     *     possible object is
     *     {@link CTServerFormats }
     *     
     */
    public CTServerFormats getServerFormats() {
        return serverFormats;
    }

    /**
     * Sets the value of the serverFormats property.
     * 
     * @param value
     *     allowed object is
     *     {@link CTServerFormats }
     *     
     */
    public void setServerFormats(CTServerFormats value) {
        this.serverFormats = value;
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
