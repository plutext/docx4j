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
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlSchemaType;
import javax.xml.bind.annotation.XmlType;


/**
 * <p>Java class for CT_WebPublishObject complex type.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
 * 
 * <pre>
 * &lt;complexType name="CT_WebPublishObject">
 *   &lt;complexContent>
 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
 *       &lt;attribute name="id" use="required" type="{http://www.w3.org/2001/XMLSchema}unsignedInt" />
 *       &lt;attribute name="divId" use="required" type="{http://schemas.openxmlformats.org/spreadsheetml/2006/main}ST_Xstring" />
 *       &lt;attribute name="sourceObject" type="{http://schemas.openxmlformats.org/spreadsheetml/2006/main}ST_Xstring" />
 *       &lt;attribute name="destinationFile" use="required" type="{http://schemas.openxmlformats.org/spreadsheetml/2006/main}ST_Xstring" />
 *       &lt;attribute name="title" type="{http://schemas.openxmlformats.org/spreadsheetml/2006/main}ST_Xstring" />
 *       &lt;attribute name="autoRepublish" type="{http://www.w3.org/2001/XMLSchema}boolean" default="false" />
 *     &lt;/restriction>
 *   &lt;/complexContent>
 * &lt;/complexType>
 * </pre>
 * 
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "CT_WebPublishObject")
public class CTWebPublishObject {

    @XmlAttribute(required = true)
    @XmlSchemaType(name = "unsignedInt")
    protected long id;
    @XmlAttribute(required = true)
    protected String divId;
    @XmlAttribute
    protected String sourceObject;
    @XmlAttribute(required = true)
    protected String destinationFile;
    @XmlAttribute
    protected String title;
    @XmlAttribute
    protected Boolean autoRepublish;

    /**
     * Gets the value of the id property.
     * 
     */
    public long getId() {
        return id;
    }

    /**
     * Sets the value of the id property.
     * 
     */
    public void setId(long value) {
        this.id = value;
    }

    /**
     * Gets the value of the divId property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getDivId() {
        return divId;
    }

    /**
     * Sets the value of the divId property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setDivId(String value) {
        this.divId = value;
    }

    /**
     * Gets the value of the sourceObject property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getSourceObject() {
        return sourceObject;
    }

    /**
     * Sets the value of the sourceObject property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setSourceObject(String value) {
        this.sourceObject = value;
    }

    /**
     * Gets the value of the destinationFile property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getDestinationFile() {
        return destinationFile;
    }

    /**
     * Sets the value of the destinationFile property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setDestinationFile(String value) {
        this.destinationFile = value;
    }

    /**
     * Gets the value of the title property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getTitle() {
        return title;
    }

    /**
     * Sets the value of the title property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setTitle(String value) {
        this.title = value;
    }

    /**
     * Gets the value of the autoRepublish property.
     * 
     * @return
     *     possible object is
     *     {@link Boolean }
     *     
     */
    public boolean isAutoRepublish() {
        if (autoRepublish == null) {
            return false;
        } else {
            return autoRepublish;
        }
    }

    /**
     * Sets the value of the autoRepublish property.
     * 
     * @param value
     *     allowed object is
     *     {@link Boolean }
     *     
     */
    public void setAutoRepublish(Boolean value) {
        this.autoRepublish = value;
    }

}
