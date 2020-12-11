/*
 *  Copyright 2007-2008, Plutext Pty Ltd.
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


package org.docx4j.vml.officedrawing;

import javax.xml.bind.Unmarshaller;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlTransient;
import javax.xml.bind.annotation.XmlType;
import org.jvnet.jaxb2_commons.ppp.Child;


/**
 * <p>Java class for CT_OLEObject complex type.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
 * 
 * <pre>
 * &lt;complexType name="CT_OLEObject">
 *   &lt;complexContent>
 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
 *       &lt;sequence>
 *         &lt;element name="LinkType" type="{urn:schemas-microsoft-com:office:office}ST_OLELinkType" minOccurs="0"/>
 *         &lt;element name="LockedField" type="{urn:schemas-microsoft-com:office:office}ST_TrueFalseBlank" minOccurs="0"/>
 *         &lt;element name="FieldCodes" type="{http://www.w3.org/2001/XMLSchema}string" minOccurs="0"/>
 *       &lt;/sequence>
 *       &lt;attribute name="Type" type="{urn:schemas-microsoft-com:office:office}ST_OLEType" />
 *       &lt;attribute name="ProgID" type="{http://www.w3.org/2001/XMLSchema}string" />
 *       &lt;attribute name="ShapeID" type="{http://www.w3.org/2001/XMLSchema}string" />
 *       &lt;attribute name="DrawAspect" type="{urn:schemas-microsoft-com:office:office}ST_OLEDrawAspect" />
 *       &lt;attribute name="ObjectID" type="{http://www.w3.org/2001/XMLSchema}string" />
 *       &lt;attribute ref="{http://schemas.openxmlformats.org/officeDocument/2006/relationships}id"/>
 *       &lt;attribute name="UpdateMode" type="{urn:schemas-microsoft-com:office:office}ST_OLEUpdateMode" />
 *     &lt;/restriction>
 *   &lt;/complexContent>
 * &lt;/complexType>
 * </pre>
 * 
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "CT_OLEObject", propOrder = {
    "linkType",
    "lockedField",
    "fieldCodes"
})
public class CTOLEObject
    implements Child
{

    @XmlElement(name = "LinkType")
    protected STOLELinkType linkType;
    @XmlElement(name = "LockedField")
    protected String lockedField;
    @XmlElement(name = "FieldCodes")
    protected String fieldCodes;
    @XmlAttribute(name = "Type")
    protected STOLEType type;
    @XmlAttribute(name = "ProgID")
    protected String progID;
    @XmlAttribute(name = "ShapeID")
    protected String shapeID;
    @XmlAttribute(name = "DrawAspect")
    protected STOLEDrawAspect drawAspect;
    @XmlAttribute(name = "ObjectID")
    protected String objectID;
    @XmlAttribute(name = "id", namespace = "http://schemas.openxmlformats.org/officeDocument/2006/relationships")
    protected String id;
    @XmlAttribute(name = "UpdateMode")
    protected STOLEUpdateMode updateMode;
    @XmlTransient
    private Object parent;

    /**
     * Gets the value of the linkType property.
     * 
     * @return
     *     possible object is
     *     {@link STOLELinkType }
     *     
     */
    public STOLELinkType getLinkType() {
        return linkType;
    }

    /**
     * Sets the value of the linkType property.
     * 
     * @param value
     *     allowed object is
     *     {@link STOLELinkType }
     *     
     */
    public void setLinkType(STOLELinkType value) {
        this.linkType = value;
    }

    /**
     * Gets the value of the lockedField property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getLockedField() {
        return lockedField;
    }

    /**
     * Sets the value of the lockedField property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setLockedField(String value) {
        this.lockedField = value;
    }

    /**
     * Gets the value of the fieldCodes property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getFieldCodes() {
        return fieldCodes;
    }

    /**
     * Sets the value of the fieldCodes property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setFieldCodes(String value) {
        this.fieldCodes = value;
    }

    /**
     * Gets the value of the type property.
     * 
     * @return
     *     possible object is
     *     {@link STOLEType }
     *     
     */
    public STOLEType getType() {
        return type;
    }

    /**
     * Sets the value of the type property.
     * 
     * @param value
     *     allowed object is
     *     {@link STOLEType }
     *     
     */
    public void setType(STOLEType value) {
        this.type = value;
    }

    /**
     * Gets the value of the progID property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getProgID() {
        return progID;
    }

    /**
     * Sets the value of the progID property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setProgID(String value) {
        this.progID = value;
    }

    /**
     * Gets the value of the shapeID property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getShapeID() {
        return shapeID;
    }

    /**
     * Sets the value of the shapeID property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setShapeID(String value) {
        this.shapeID = value;
    }

    /**
     * Gets the value of the drawAspect property.
     * 
     * @return
     *     possible object is
     *     {@link STOLEDrawAspect }
     *     
     */
    public STOLEDrawAspect getDrawAspect() {
        return drawAspect;
    }

    /**
     * Sets the value of the drawAspect property.
     * 
     * @param value
     *     allowed object is
     *     {@link STOLEDrawAspect }
     *     
     */
    public void setDrawAspect(STOLEDrawAspect value) {
        this.drawAspect = value;
    }

    /**
     * Gets the value of the objectID property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getObjectID() {
        return objectID;
    }

    /**
     * Sets the value of the objectID property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setObjectID(String value) {
        this.objectID = value;
    }

    /**
     * Relationship
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
     * Gets the value of the updateMode property.
     * 
     * @return
     *     possible object is
     *     {@link STOLEUpdateMode }
     *     
     */
    public STOLEUpdateMode getUpdateMode() {
        return updateMode;
    }

    /**
     * Sets the value of the updateMode property.
     * 
     * @param value
     *     allowed object is
     *     {@link STOLEUpdateMode }
     *     
     */
    public void setUpdateMode(STOLEUpdateMode value) {
        this.updateMode = value;
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
