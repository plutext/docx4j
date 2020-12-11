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
 * <p>Java class for CT_OleObject complex type.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
 * 
 * <pre>
 * &lt;complexType name="CT_OleObject">
 *   &lt;complexContent>
 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
 *       &lt;sequence>
 *         &lt;element name="objectPr" type="{http://schemas.openxmlformats.org/spreadsheetml/2006/main}CT_ObjectPr" minOccurs="0"/>
 *       &lt;/sequence>
 *       &lt;attribute name="progId" type="{http://www.w3.org/2001/XMLSchema}string" />
 *       &lt;attribute name="dvAspect" type="{http://schemas.openxmlformats.org/spreadsheetml/2006/main}ST_DvAspect" default="DVASPECT_CONTENT" />
 *       &lt;attribute name="link" type="{http://schemas.openxmlformats.org/officeDocument/2006/sharedTypes}ST_Xstring" />
 *       &lt;attribute name="oleUpdate" type="{http://schemas.openxmlformats.org/spreadsheetml/2006/main}ST_OleUpdate" />
 *       &lt;attribute name="autoLoad" type="{http://www.w3.org/2001/XMLSchema}boolean" default="false" />
 *       &lt;attribute name="shapeId" use="required" type="{http://www.w3.org/2001/XMLSchema}unsignedInt" />
 *       &lt;attribute ref="{http://schemas.openxmlformats.org/officeDocument/2006/relationships}id"/>
 *     &lt;/restriction>
 *   &lt;/complexContent>
 * &lt;/complexType>
 * </pre>
 * 
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "CT_OleObject", propOrder = {
    "objectPr"
})
public class CTOleObject implements Child
{

    protected CTObjectPr objectPr;
    @XmlAttribute(name = "progId")
    protected String progId;
    @XmlAttribute(name = "dvAspect")
    protected STDvAspect dvAspect;
    @XmlAttribute(name = "link")
    protected String link;
    @XmlAttribute(name = "oleUpdate")
    protected STOleUpdate oleUpdate;
    @XmlAttribute(name = "autoLoad")
    protected Boolean autoLoad;
    @XmlAttribute(name = "shapeId", required = true)
    @XmlSchemaType(name = "unsignedInt")
    protected long shapeId;
    @XmlAttribute(name = "id", namespace = "http://schemas.openxmlformats.org/officeDocument/2006/relationships")
    protected String id;
    @XmlTransient
    private Object parent;

    /**
     * Gets the value of the objectPr property.
     * 
     * @return
     *     possible object is
     *     {@link CTObjectPr }
     *     
     */
    public CTObjectPr getObjectPr() {
        return objectPr;
    }

    /**
     * Sets the value of the objectPr property.
     * 
     * @param value
     *     allowed object is
     *     {@link CTObjectPr }
     *     
     */
    public void setObjectPr(CTObjectPr value) {
        this.objectPr = value;
    }

    /**
     * Gets the value of the progId property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getProgId() {
        return progId;
    }

    /**
     * Sets the value of the progId property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setProgId(String value) {
        this.progId = value;
    }

    /**
     * Gets the value of the dvAspect property.
     * 
     * @return
     *     possible object is
     *     {@link STDvAspect }
     *     
     */
    public STDvAspect getDvAspect() {
        if (dvAspect == null) {
            return STDvAspect.DVASPECT_CONTENT;
        } else {
            return dvAspect;
        }
    }

    /**
     * Sets the value of the dvAspect property.
     * 
     * @param value
     *     allowed object is
     *     {@link STDvAspect }
     *     
     */
    public void setDvAspect(STDvAspect value) {
        this.dvAspect = value;
    }

    /**
     * Gets the value of the link property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getLink() {
        return link;
    }

    /**
     * Sets the value of the link property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setLink(String value) {
        this.link = value;
    }

    /**
     * Gets the value of the oleUpdate property.
     * 
     * @return
     *     possible object is
     *     {@link STOleUpdate }
     *     
     */
    public STOleUpdate getOleUpdate() {
        return oleUpdate;
    }

    /**
     * Sets the value of the oleUpdate property.
     * 
     * @param value
     *     allowed object is
     *     {@link STOleUpdate }
     *     
     */
    public void setOleUpdate(STOleUpdate value) {
        this.oleUpdate = value;
    }

    /**
     * Gets the value of the autoLoad property.
     * 
     * @return
     *     possible object is
     *     {@link Boolean }
     *     
     */
    public boolean isAutoLoad() {
        if (autoLoad == null) {
            return false;
        } else {
            return autoLoad;
        }
    }

    /**
     * Sets the value of the autoLoad property.
     * 
     * @param value
     *     allowed object is
     *     {@link Boolean }
     *     
     */
    public void setAutoLoad(Boolean value) {
        this.autoLoad = value;
    }

    /**
     * Gets the value of the shapeId property.
     * 
     */
    public long getShapeId() {
        return shapeId;
    }

    /**
     * Sets the value of the shapeId property.
     * 
     */
    public void setShapeId(long value) {
        this.shapeId = value;
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
