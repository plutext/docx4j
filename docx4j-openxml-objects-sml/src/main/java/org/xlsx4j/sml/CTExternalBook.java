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
 * <p>Java class for CT_ExternalBook complex type.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
 * 
 * <pre>
 * &lt;complexType name="CT_ExternalBook">
 *   &lt;complexContent>
 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
 *       &lt;sequence>
 *         &lt;element name="sheetNames" type="{http://schemas.openxmlformats.org/spreadsheetml/2006/main}CT_ExternalSheetNames" minOccurs="0"/>
 *         &lt;element name="definedNames" type="{http://schemas.openxmlformats.org/spreadsheetml/2006/main}CT_ExternalDefinedNames" minOccurs="0"/>
 *         &lt;element name="sheetDataSet" type="{http://schemas.openxmlformats.org/spreadsheetml/2006/main}CT_ExternalSheetDataSet" minOccurs="0"/>
 *       &lt;/sequence>
 *       &lt;attribute ref="{http://schemas.openxmlformats.org/officeDocument/2006/relationships}id use="required""/>
 *     &lt;/restriction>
 *   &lt;/complexContent>
 * &lt;/complexType>
 * </pre>
 * 
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "CT_ExternalBook", propOrder = {
    "sheetNames",
    "definedNames",
    "sheetDataSet"
})
public class CTExternalBook implements Child
{

    protected CTExternalSheetNames sheetNames;
    protected CTExternalDefinedNames definedNames;
    protected CTExternalSheetDataSet sheetDataSet;
    @XmlAttribute(name = "id", namespace = "http://schemas.openxmlformats.org/officeDocument/2006/relationships", required = true)
    protected String id;
    @XmlTransient
    private Object parent;

    /**
     * Gets the value of the sheetNames property.
     * 
     * @return
     *     possible object is
     *     {@link CTExternalSheetNames }
     *     
     */
    public CTExternalSheetNames getSheetNames() {
        return sheetNames;
    }

    /**
     * Sets the value of the sheetNames property.
     * 
     * @param value
     *     allowed object is
     *     {@link CTExternalSheetNames }
     *     
     */
    public void setSheetNames(CTExternalSheetNames value) {
        this.sheetNames = value;
    }

    /**
     * Gets the value of the definedNames property.
     * 
     * @return
     *     possible object is
     *     {@link CTExternalDefinedNames }
     *     
     */
    public CTExternalDefinedNames getDefinedNames() {
        return definedNames;
    }

    /**
     * Sets the value of the definedNames property.
     * 
     * @param value
     *     allowed object is
     *     {@link CTExternalDefinedNames }
     *     
     */
    public void setDefinedNames(CTExternalDefinedNames value) {
        this.definedNames = value;
    }

    /**
     * Gets the value of the sheetDataSet property.
     * 
     * @return
     *     possible object is
     *     {@link CTExternalSheetDataSet }
     *     
     */
    public CTExternalSheetDataSet getSheetDataSet() {
        return sheetDataSet;
    }

    /**
     * Sets the value of the sheetDataSet property.
     * 
     * @param value
     *     allowed object is
     *     {@link CTExternalSheetDataSet }
     *     
     */
    public void setSheetDataSet(CTExternalSheetDataSet value) {
        this.sheetDataSet = value;
    }

    /**
     * Gets the value of the id property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getId() {
        return id;
    }

    /**
     * Sets the value of the id property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setId(String value) {
        this.id = value;
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
