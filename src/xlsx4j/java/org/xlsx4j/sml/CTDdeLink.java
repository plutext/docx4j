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
import javax.xml.bind.annotation.XmlTransient;
import javax.xml.bind.annotation.XmlType;
import org.jvnet.jaxb2_commons.ppp.Child;


/**
 * <p>Java class for CT_DdeLink complex type.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
 * 
 * <pre>
 * &lt;complexType name="CT_DdeLink">
 *   &lt;complexContent>
 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
 *       &lt;sequence>
 *         &lt;element name="ddeItems" type="{http://schemas.openxmlformats.org/spreadsheetml/2006/main}CT_DdeItems" minOccurs="0"/>
 *       &lt;/sequence>
 *       &lt;attribute name="ddeService" use="required" type="{http://schemas.openxmlformats.org/officeDocument/2006/sharedTypes}ST_Xstring" />
 *       &lt;attribute name="ddeTopic" use="required" type="{http://schemas.openxmlformats.org/officeDocument/2006/sharedTypes}ST_Xstring" />
 *     &lt;/restriction>
 *   &lt;/complexContent>
 * &lt;/complexType>
 * </pre>
 * 
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "CT_DdeLink", propOrder = {
    "ddeItems"
})
public class CTDdeLink implements Child
{

    protected CTDdeItems ddeItems;
    @XmlAttribute(name = "ddeService", required = true)
    protected String ddeService;
    @XmlAttribute(name = "ddeTopic", required = true)
    protected String ddeTopic;
    @XmlTransient
    private Object parent;

    /**
     * Gets the value of the ddeItems property.
     * 
     * @return
     *     possible object is
     *     {@link CTDdeItems }
     *     
     */
    public CTDdeItems getDdeItems() {
        return ddeItems;
    }

    /**
     * Sets the value of the ddeItems property.
     * 
     * @param value
     *     allowed object is
     *     {@link CTDdeItems }
     *     
     */
    public void setDdeItems(CTDdeItems value) {
        this.ddeItems = value;
    }

    /**
     * Gets the value of the ddeService property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getDdeService() {
        return ddeService;
    }

    /**
     * Sets the value of the ddeService property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setDdeService(String value) {
        this.ddeService = value;
    }

    /**
     * Gets the value of the ddeTopic property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getDdeTopic() {
        return ddeTopic;
    }

    /**
     * Sets the value of the ddeTopic property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setDdeTopic(String value) {
        this.ddeTopic = value;
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
