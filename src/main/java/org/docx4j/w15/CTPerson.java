/*
 *  Copyright 2013, Plutext Pty Ltd.
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


package org.docx4j.w15; 

import org.jvnet.jaxb2_commons.ppp.Child;

import javax.xml.bind.Unmarshaller;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlTransient;
import javax.xml.bind.annotation.XmlType;



/**
 * <p>Java class for CT_Person complex type.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
 * 
 * <pre>
 * &lt;complexType name="CT_Person">
 *   &lt;complexContent>
 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
 *       &lt;sequence>
 *         &lt;element name="presenceInfo" type="{http://schemas.microsoft.com/office/word/2012/wordml}CT_PresenceInfo" minOccurs="0"/>
 *       &lt;/sequence>
 *       &lt;attribute name="author" use="required" type="{http://schemas.openxmlformats.org/wordprocessingml/2006/main}ST_String" />
 *       &lt;attribute name="contact" use="required" type="{http://schemas.openxmlformats.org/wordprocessingml/2006/main}ST_String" />
 *     &lt;/restriction>
 *   &lt;/complexContent>
 * &lt;/complexType>
 * </pre>
 * 
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "CT_Person", propOrder = {
    "presenceInfo"
})
public class CTPerson implements Child
{

    protected CTPresenceInfo presenceInfo;
    @XmlAttribute(name = "author", namespace = "http://schemas.microsoft.com/office/word/2012/wordml", required = true)
    protected String author;
    @XmlAttribute(name = "contact", namespace = "http://schemas.microsoft.com/office/word/2012/wordml", required = true)
    protected String contact;
    @XmlTransient
    private Object parent;

    /**
     * Gets the value of the presenceInfo property.
     * 
     * @return
     *     possible object is
     *     {@link CTPresenceInfo }
     *     
     */
    public CTPresenceInfo getPresenceInfo() {
        return presenceInfo;
    }

    /**
     * Sets the value of the presenceInfo property.
     * 
     * @param value
     *     allowed object is
     *     {@link CTPresenceInfo }
     *     
     */
    public void setPresenceInfo(CTPresenceInfo value) {
        this.presenceInfo = value;
    }

    /**
     * Gets the value of the author property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getAuthor() {
        return author;
    }

    /**
     * Sets the value of the author property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setAuthor(String value) {
        this.author = value;
    }

    /**
     * Gets the value of the contact property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getContact() {
        return contact;
    }

    /**
     * Sets the value of the contact property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setContact(String value) {
        this.contact = value;
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
