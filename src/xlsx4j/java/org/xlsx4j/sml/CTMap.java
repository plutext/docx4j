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
 * <p>Java class for CT_Map complex type.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
 * 
 * <pre>
 * &lt;complexType name="CT_Map">
 *   &lt;complexContent>
 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
 *       &lt;sequence>
 *         &lt;element name="DataBinding" type="{http://schemas.openxmlformats.org/spreadsheetml/2006/main}CT_DataBinding" minOccurs="0"/>
 *       &lt;/sequence>
 *       &lt;attribute name="ID" use="required" type="{http://www.w3.org/2001/XMLSchema}unsignedInt" />
 *       &lt;attribute name="Name" use="required" type="{http://www.w3.org/2001/XMLSchema}string" />
 *       &lt;attribute name="RootElement" use="required" type="{http://www.w3.org/2001/XMLSchema}string" />
 *       &lt;attribute name="SchemaID" use="required" type="{http://www.w3.org/2001/XMLSchema}string" />
 *       &lt;attribute name="ShowImportExportValidationErrors" use="required" type="{http://www.w3.org/2001/XMLSchema}boolean" />
 *       &lt;attribute name="AutoFit" use="required" type="{http://www.w3.org/2001/XMLSchema}boolean" />
 *       &lt;attribute name="Append" use="required" type="{http://www.w3.org/2001/XMLSchema}boolean" />
 *       &lt;attribute name="PreserveSortAFLayout" use="required" type="{http://www.w3.org/2001/XMLSchema}boolean" />
 *       &lt;attribute name="PreserveFormat" use="required" type="{http://www.w3.org/2001/XMLSchema}boolean" />
 *     &lt;/restriction>
 *   &lt;/complexContent>
 * &lt;/complexType>
 * </pre>
 * 
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "CT_Map", propOrder = {
    "dataBinding"
})
public class CTMap implements Child
{

    @XmlElement(name = "DataBinding")
    protected CTDataBinding dataBinding;
    @XmlAttribute(name = "ID", required = true)
    @XmlSchemaType(name = "unsignedInt")
    protected long id;
    @XmlAttribute(name = "Name", required = true)
    protected String name;
    @XmlAttribute(name = "RootElement", required = true)
    protected String rootElement;
    @XmlAttribute(name = "SchemaID", required = true)
    protected String schemaID;
    @XmlAttribute(name = "ShowImportExportValidationErrors", required = true)
    protected boolean showImportExportValidationErrors;
    @XmlAttribute(name = "AutoFit", required = true)
    protected boolean autoFit;
    @XmlAttribute(name = "Append", required = true)
    protected boolean append;
    @XmlAttribute(name = "PreserveSortAFLayout", required = true)
    protected boolean preserveSortAFLayout;
    @XmlAttribute(name = "PreserveFormat", required = true)
    protected boolean preserveFormat;
    @XmlTransient
    private Object parent;

    /**
     * Gets the value of the dataBinding property.
     * 
     * @return
     *     possible object is
     *     {@link CTDataBinding }
     *     
     */
    public CTDataBinding getDataBinding() {
        return dataBinding;
    }

    /**
     * Sets the value of the dataBinding property.
     * 
     * @param value
     *     allowed object is
     *     {@link CTDataBinding }
     *     
     */
    public void setDataBinding(CTDataBinding value) {
        this.dataBinding = value;
    }

    /**
     * Gets the value of the id property.
     * 
     */
    public long getID() {
        return id;
    }

    /**
     * Sets the value of the id property.
     * 
     */
    public void setID(long value) {
        this.id = value;
    }

    /**
     * Gets the value of the name property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getName() {
        return name;
    }

    /**
     * Sets the value of the name property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setName(String value) {
        this.name = value;
    }

    /**
     * Gets the value of the rootElement property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getRootElement() {
        return rootElement;
    }

    /**
     * Sets the value of the rootElement property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setRootElement(String value) {
        this.rootElement = value;
    }

    /**
     * Gets the value of the schemaID property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getSchemaID() {
        return schemaID;
    }

    /**
     * Sets the value of the schemaID property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setSchemaID(String value) {
        this.schemaID = value;
    }

    /**
     * Gets the value of the showImportExportValidationErrors property.
     * 
     */
    public boolean isShowImportExportValidationErrors() {
        return showImportExportValidationErrors;
    }

    /**
     * Sets the value of the showImportExportValidationErrors property.
     * 
     */
    public void setShowImportExportValidationErrors(boolean value) {
        this.showImportExportValidationErrors = value;
    }

    /**
     * Gets the value of the autoFit property.
     * 
     */
    public boolean isAutoFit() {
        return autoFit;
    }

    /**
     * Sets the value of the autoFit property.
     * 
     */
    public void setAutoFit(boolean value) {
        this.autoFit = value;
    }

    /**
     * Gets the value of the append property.
     * 
     */
    public boolean isAppend() {
        return append;
    }

    /**
     * Sets the value of the append property.
     * 
     */
    public void setAppend(boolean value) {
        this.append = value;
    }

    /**
     * Gets the value of the preserveSortAFLayout property.
     * 
     */
    public boolean isPreserveSortAFLayout() {
        return preserveSortAFLayout;
    }

    /**
     * Sets the value of the preserveSortAFLayout property.
     * 
     */
    public void setPreserveSortAFLayout(boolean value) {
        this.preserveSortAFLayout = value;
    }

    /**
     * Gets the value of the preserveFormat property.
     * 
     */
    public boolean isPreserveFormat() {
        return preserveFormat;
    }

    /**
     * Sets the value of the preserveFormat property.
     * 
     */
    public void setPreserveFormat(boolean value) {
        this.preserveFormat = value;
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
