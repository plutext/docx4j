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
 * <p>Java class for CT_WebPublishItem complex type.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
 * 
 * <pre>
 * &lt;complexType name="CT_WebPublishItem">
 *   &lt;complexContent>
 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
 *       &lt;attribute name="id" use="required" type="{http://www.w3.org/2001/XMLSchema}unsignedInt" />
 *       &lt;attribute name="divId" use="required" type="{http://schemas.openxmlformats.org/officeDocument/2006/sharedTypes}ST_Xstring" />
 *       &lt;attribute name="sourceType" use="required" type="{http://schemas.openxmlformats.org/spreadsheetml/2006/main}ST_WebSourceType" />
 *       &lt;attribute name="sourceRef" type="{http://schemas.openxmlformats.org/spreadsheetml/2006/main}ST_Ref" />
 *       &lt;attribute name="sourceObject" type="{http://schemas.openxmlformats.org/officeDocument/2006/sharedTypes}ST_Xstring" />
 *       &lt;attribute name="destinationFile" use="required" type="{http://schemas.openxmlformats.org/officeDocument/2006/sharedTypes}ST_Xstring" />
 *       &lt;attribute name="title" type="{http://schemas.openxmlformats.org/officeDocument/2006/sharedTypes}ST_Xstring" />
 *       &lt;attribute name="autoRepublish" type="{http://www.w3.org/2001/XMLSchema}boolean" default="false" />
 *     &lt;/restriction>
 *   &lt;/complexContent>
 * &lt;/complexType>
 * </pre>
 * 
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "CT_WebPublishItem")
public class CTWebPublishItem implements Child
{

    @XmlAttribute(name = "id", required = true)
    @XmlSchemaType(name = "unsignedInt")
    protected long id;
    @XmlAttribute(name = "divId", required = true)
    protected String divId;
    @XmlAttribute(name = "sourceType", required = true)
    protected STWebSourceType sourceType;
    @XmlAttribute(name = "sourceRef")
    protected String sourceRef;
    @XmlAttribute(name = "sourceObject")
    protected String sourceObject;
    @XmlAttribute(name = "destinationFile", required = true)
    protected String destinationFile;
    @XmlAttribute(name = "title")
    protected String title;
    @XmlAttribute(name = "autoRepublish")
    protected Boolean autoRepublish;
    @XmlTransient
    private Object parent;

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
     * Gets the value of the sourceType property.
     * 
     * @return
     *     possible object is
     *     {@link STWebSourceType }
     *     
     */
    public STWebSourceType getSourceType() {
        return sourceType;
    }

    /**
     * Sets the value of the sourceType property.
     * 
     * @param value
     *     allowed object is
     *     {@link STWebSourceType }
     *     
     */
    public void setSourceType(STWebSourceType value) {
        this.sourceType = value;
    }

    /**
     * Gets the value of the sourceRef property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getSourceRef() {
        return sourceRef;
    }

    /**
     * Sets the value of the sourceRef property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setSourceRef(String value) {
        this.sourceRef = value;
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
