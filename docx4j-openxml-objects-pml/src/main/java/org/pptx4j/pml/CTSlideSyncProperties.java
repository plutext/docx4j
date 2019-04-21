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
import javax.xml.datatype.XMLGregorianCalendar;


/**
 * <p>Java class for CT_SlideSyncProperties complex type.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
 * 
 * <pre>
 * &lt;complexType name="CT_SlideSyncProperties">
 *   &lt;complexContent>
 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
 *       &lt;sequence>
 *         &lt;element name="extLst" type="{http://schemas.openxmlformats.org/presentationml/2006/main}CT_ExtensionList" minOccurs="0"/>
 *       &lt;/sequence>
 *       &lt;attribute name="serverSldId" use="required" type="{http://www.w3.org/2001/XMLSchema}string" />
 *       &lt;attribute name="serverSldModifiedTime" use="required" type="{http://www.w3.org/2001/XMLSchema}dateTime" />
 *       &lt;attribute name="clientInsertedTime" use="required" type="{http://www.w3.org/2001/XMLSchema}dateTime" />
 *     &lt;/restriction>
 *   &lt;/complexContent>
 * &lt;/complexType>
 * </pre>
 * 
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "CT_SlideSyncProperties", propOrder = {
    "extLst"
})
public class CTSlideSyncProperties {

    protected CTExtensionList extLst;
    @XmlAttribute(name = "serverSldId", required = true)
    protected String serverSldId;
    @XmlAttribute(name = "serverSldModifiedTime", required = true)
    @XmlSchemaType(name = "dateTime")
    protected XMLGregorianCalendar serverSldModifiedTime;
    @XmlAttribute(name = "clientInsertedTime", required = true)
    @XmlSchemaType(name = "dateTime")
    protected XMLGregorianCalendar clientInsertedTime;

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
     * Gets the value of the serverSldId property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getServerSldId() {
        return serverSldId;
    }

    /**
     * Sets the value of the serverSldId property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setServerSldId(String value) {
        this.serverSldId = value;
    }

    /**
     * Gets the value of the serverSldModifiedTime property.
     * 
     * @return
     *     possible object is
     *     {@link XMLGregorianCalendar }
     *     
     */
    public XMLGregorianCalendar getServerSldModifiedTime() {
        return serverSldModifiedTime;
    }

    /**
     * Sets the value of the serverSldModifiedTime property.
     * 
     * @param value
     *     allowed object is
     *     {@link XMLGregorianCalendar }
     *     
     */
    public void setServerSldModifiedTime(XMLGregorianCalendar value) {
        this.serverSldModifiedTime = value;
    }

    /**
     * Gets the value of the clientInsertedTime property.
     * 
     * @return
     *     possible object is
     *     {@link XMLGregorianCalendar }
     *     
     */
    public XMLGregorianCalendar getClientInsertedTime() {
        return clientInsertedTime;
    }

    /**
     * Sets the value of the clientInsertedTime property.
     * 
     * @param value
     *     allowed object is
     *     {@link XMLGregorianCalendar }
     *     
     */
    public void setClientInsertedTime(XMLGregorianCalendar value) {
        this.clientInsertedTime = value;
    }

}
