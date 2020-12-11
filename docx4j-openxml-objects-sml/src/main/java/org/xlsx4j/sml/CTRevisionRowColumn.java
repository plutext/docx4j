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

import java.util.ArrayList;
import java.util.List;
import javax.xml.bind.Unmarshaller;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlElements;
import javax.xml.bind.annotation.XmlSchemaType;
import javax.xml.bind.annotation.XmlTransient;
import javax.xml.bind.annotation.XmlType;
import org.jvnet.jaxb2_commons.ppp.Child;


/**
 * <p>Java class for CT_RevisionRowColumn complex type.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
 * 
 * <pre>
 * &lt;complexType name="CT_RevisionRowColumn">
 *   &lt;complexContent>
 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
 *       &lt;choice maxOccurs="unbounded" minOccurs="0">
 *         &lt;element name="undo" type="{http://schemas.openxmlformats.org/spreadsheetml/2006/main}CT_UndoInfo" maxOccurs="unbounded" minOccurs="0"/>
 *         &lt;element name="rcc" type="{http://schemas.openxmlformats.org/spreadsheetml/2006/main}CT_RevisionCellChange" maxOccurs="unbounded" minOccurs="0"/>
 *         &lt;element name="rfmt" type="{http://schemas.openxmlformats.org/spreadsheetml/2006/main}CT_RevisionFormatting" maxOccurs="unbounded" minOccurs="0"/>
 *       &lt;/choice>
 *       &lt;attGroup ref="{http://schemas.openxmlformats.org/spreadsheetml/2006/main}AG_RevData"/>
 *       &lt;attribute name="sId" use="required" type="{http://www.w3.org/2001/XMLSchema}unsignedInt" />
 *       &lt;attribute name="eol" type="{http://www.w3.org/2001/XMLSchema}boolean" default="false" />
 *       &lt;attribute name="ref" use="required" type="{http://schemas.openxmlformats.org/spreadsheetml/2006/main}ST_Ref" />
 *       &lt;attribute name="action" use="required" type="{http://schemas.openxmlformats.org/spreadsheetml/2006/main}ST_rwColActionType" />
 *       &lt;attribute name="edge" type="{http://www.w3.org/2001/XMLSchema}boolean" default="false" />
 *     &lt;/restriction>
 *   &lt;/complexContent>
 * &lt;/complexType>
 * </pre>
 * 
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "CT_RevisionRowColumn", propOrder = {
    "undoOrRccOrRfmt"
})
public class CTRevisionRowColumn implements Child
{

    @XmlElements({
        @XmlElement(name = "undo", type = CTUndoInfo.class),
        @XmlElement(name = "rcc", type = CTRevisionCellChange.class),
        @XmlElement(name = "rfmt", type = CTRevisionFormatting.class)
    })
    protected List<Object> undoOrRccOrRfmt;
    @XmlAttribute(name = "sId", required = true)
    @XmlSchemaType(name = "unsignedInt")
    protected long sId;
    @XmlAttribute(name = "eol")
    protected Boolean eol;
    @XmlAttribute(name = "ref", required = true)
    protected String ref;
    @XmlAttribute(name = "action", required = true)
    protected STRwColActionType action;
    @XmlAttribute(name = "edge")
    protected Boolean edge;
    @XmlAttribute(name = "rId", required = true)
    @XmlSchemaType(name = "unsignedInt")
    protected long rId;
    @XmlAttribute(name = "ua")
    protected Boolean ua;
    @XmlAttribute(name = "ra")
    protected Boolean ra;
    @XmlTransient
    private Object parent;

    /**
     * Gets the value of the undoOrRccOrRfmt property.
     * 
     * <p>
     * This accessor method returns a reference to the live list,
     * not a snapshot. Therefore any modification you make to the
     * returned list will be present inside the JAXB object.
     * This is why there is not a <CODE>set</CODE> method for the undoOrRccOrRfmt property.
     * 
     * <p>
     * For example, to add a new item, do as follows:
     * <pre>
     *    getUndoOrRccOrRfmt().add(newItem);
     * </pre>
     * 
     * 
     * <p>
     * Objects of the following type(s) are allowed in the list
     * {@link CTUndoInfo }
     * {@link CTRevisionCellChange }
     * {@link CTRevisionFormatting }
     * 
     * 
     */
    public List<Object> getUndoOrRccOrRfmt() {
        if (undoOrRccOrRfmt == null) {
            undoOrRccOrRfmt = new ArrayList<Object>();
        }
        return this.undoOrRccOrRfmt;
    }

    /**
     * Gets the value of the sId property.
     * 
     */
    public long getSId() {
        return sId;
    }

    /**
     * Sets the value of the sId property.
     * 
     */
    public void setSId(long value) {
        this.sId = value;
    }

    /**
     * Gets the value of the eol property.
     * 
     * @return
     *     possible object is
     *     {@link Boolean }
     *     
     */
    public boolean isEol() {
        if (eol == null) {
            return false;
        } else {
            return eol;
        }
    }

    /**
     * Sets the value of the eol property.
     * 
     * @param value
     *     allowed object is
     *     {@link Boolean }
     *     
     */
    public void setEol(Boolean value) {
        this.eol = value;
    }

    /**
     * Gets the value of the ref property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getRef() {
        return ref;
    }

    /**
     * Sets the value of the ref property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setRef(String value) {
        this.ref = value;
    }

    /**
     * Gets the value of the action property.
     * 
     * @return
     *     possible object is
     *     {@link STRwColActionType }
     *     
     */
    public STRwColActionType getAction() {
        return action;
    }

    /**
     * Sets the value of the action property.
     * 
     * @param value
     *     allowed object is
     *     {@link STRwColActionType }
     *     
     */
    public void setAction(STRwColActionType value) {
        this.action = value;
    }

    /**
     * Gets the value of the edge property.
     * 
     * @return
     *     possible object is
     *     {@link Boolean }
     *     
     */
    public boolean isEdge() {
        if (edge == null) {
            return false;
        } else {
            return edge;
        }
    }

    /**
     * Sets the value of the edge property.
     * 
     * @param value
     *     allowed object is
     *     {@link Boolean }
     *     
     */
    public void setEdge(Boolean value) {
        this.edge = value;
    }

    /**
     * Gets the value of the rId property.
     * 
     */
    public long getRId() {
        return rId;
    }

    /**
     * Sets the value of the rId property.
     * 
     */
    public void setRId(long value) {
        this.rId = value;
    }

    /**
     * Gets the value of the ua property.
     * 
     * @return
     *     possible object is
     *     {@link Boolean }
     *     
     */
    public boolean isUa() {
        if (ua == null) {
            return false;
        } else {
            return ua;
        }
    }

    /**
     * Sets the value of the ua property.
     * 
     * @param value
     *     allowed object is
     *     {@link Boolean }
     *     
     */
    public void setUa(Boolean value) {
        this.ua = value;
    }

    /**
     * Gets the value of the ra property.
     * 
     * @return
     *     possible object is
     *     {@link Boolean }
     *     
     */
    public boolean isRa() {
        if (ra == null) {
            return false;
        } else {
            return ra;
        }
    }

    /**
     * Sets the value of the ra property.
     * 
     * @param value
     *     allowed object is
     *     {@link Boolean }
     *     
     */
    public void setRa(Boolean value) {
        this.ra = value;
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
