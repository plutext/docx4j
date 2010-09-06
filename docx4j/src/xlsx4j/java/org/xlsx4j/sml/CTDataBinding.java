/*
 *  Copyright 2010, Plutext Pty Ltd.
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


package org.xlsx4j.sml;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAnyElement;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlSchemaType;
import javax.xml.bind.annotation.XmlType;


/**
 * <p>Java class for CT_DataBinding complex type.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
 * 
 * <pre>
 * &lt;complexType name="CT_DataBinding">
 *   &lt;complexContent>
 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
 *       &lt;sequence>
 *         &lt;any/>
 *       &lt;/sequence>
 *       &lt;attribute name="DataBindingName" type="{http://www.w3.org/2001/XMLSchema}string" />
 *       &lt;attribute name="FileBinding" type="{http://www.w3.org/2001/XMLSchema}boolean" />
 *       &lt;attribute name="ConnectionID" type="{http://www.w3.org/2001/XMLSchema}unsignedInt" />
 *       &lt;attribute name="FileBindingName" type="{http://www.w3.org/2001/XMLSchema}string" />
 *       &lt;attribute name="DataBindingLoadMode" use="required" type="{http://www.w3.org/2001/XMLSchema}unsignedInt" />
 *     &lt;/restriction>
 *   &lt;/complexContent>
 * &lt;/complexType>
 * </pre>
 * 
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "CT_DataBinding", propOrder = {
    "any"
})
public class CTDataBinding {

    @XmlAnyElement(lax = true)
    protected Object any;
    @XmlAttribute(name = "DataBindingName")
    protected String dataBindingName;
    @XmlAttribute(name = "FileBinding")
    protected Boolean fileBinding;
    @XmlAttribute(name = "ConnectionID")
    @XmlSchemaType(name = "unsignedInt")
    protected Long connectionID;
    @XmlAttribute(name = "FileBindingName")
    protected String fileBindingName;
    @XmlAttribute(name = "DataBindingLoadMode", required = true)
    @XmlSchemaType(name = "unsignedInt")
    protected long dataBindingLoadMode;

    /**
     * Gets the value of the any property.
     * 
     * @return
     *     possible object is
     *     {@link Object }
     *     
     */
    public Object getAny() {
        return any;
    }

    /**
     * Sets the value of the any property.
     * 
     * @param value
     *     allowed object is
     *     {@link Object }
     *     
     */
    public void setAny(Object value) {
        this.any = value;
    }

    /**
     * Gets the value of the dataBindingName property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getDataBindingName() {
        return dataBindingName;
    }

    /**
     * Sets the value of the dataBindingName property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setDataBindingName(String value) {
        this.dataBindingName = value;
    }

    /**
     * Gets the value of the fileBinding property.
     * 
     * @return
     *     possible object is
     *     {@link Boolean }
     *     
     */
    public Boolean isFileBinding() {
        return fileBinding;
    }

    /**
     * Sets the value of the fileBinding property.
     * 
     * @param value
     *     allowed object is
     *     {@link Boolean }
     *     
     */
    public void setFileBinding(Boolean value) {
        this.fileBinding = value;
    }

    /**
     * Gets the value of the connectionID property.
     * 
     * @return
     *     possible object is
     *     {@link Long }
     *     
     */
    public Long getConnectionID() {
        return connectionID;
    }

    /**
     * Sets the value of the connectionID property.
     * 
     * @param value
     *     allowed object is
     *     {@link Long }
     *     
     */
    public void setConnectionID(Long value) {
        this.connectionID = value;
    }

    /**
     * Gets the value of the fileBindingName property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getFileBindingName() {
        return fileBindingName;
    }

    /**
     * Sets the value of the fileBindingName property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setFileBindingName(String value) {
        this.fileBindingName = value;
    }

    /**
     * Gets the value of the dataBindingLoadMode property.
     * 
     */
    public long getDataBindingLoadMode() {
        return dataBindingLoadMode;
    }

    /**
     * Sets the value of the dataBindingLoadMode property.
     * 
     */
    public void setDataBindingLoadMode(long value) {
        this.dataBindingLoadMode = value;
    }

}
