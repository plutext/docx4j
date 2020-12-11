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
 * <p>Java class for CT_DataConsolidate complex type.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
 * 
 * <pre>
 * &lt;complexType name="CT_DataConsolidate">
 *   &lt;complexContent>
 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
 *       &lt;sequence>
 *         &lt;element name="dataRefs" type="{http://schemas.openxmlformats.org/spreadsheetml/2006/main}CT_DataRefs" minOccurs="0"/>
 *       &lt;/sequence>
 *       &lt;attribute name="function" type="{http://schemas.openxmlformats.org/spreadsheetml/2006/main}ST_DataConsolidateFunction" default="sum" />
 *       &lt;attribute name="startLabels" type="{http://www.w3.org/2001/XMLSchema}boolean" default="false" />
 *       &lt;attribute name="leftLabels" type="{http://www.w3.org/2001/XMLSchema}boolean" default="false" />
 *       &lt;attribute name="topLabels" type="{http://www.w3.org/2001/XMLSchema}boolean" default="false" />
 *       &lt;attribute name="link" type="{http://www.w3.org/2001/XMLSchema}boolean" default="false" />
 *     &lt;/restriction>
 *   &lt;/complexContent>
 * &lt;/complexType>
 * </pre>
 * 
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "CT_DataConsolidate", propOrder = {
    "dataRefs"
})
public class CTDataConsolidate implements Child
{

    protected CTDataRefs dataRefs;
    @XmlAttribute(name = "function")
    protected STDataConsolidateFunction function;
    @XmlAttribute(name = "startLabels")
    protected Boolean startLabels;
    @XmlAttribute(name = "leftLabels")
    protected Boolean leftLabels;
    @XmlAttribute(name = "topLabels")
    protected Boolean topLabels;
    @XmlAttribute(name = "link")
    protected Boolean link;
    @XmlTransient
    private Object parent;

    /**
     * Gets the value of the dataRefs property.
     * 
     * @return
     *     possible object is
     *     {@link CTDataRefs }
     *     
     */
    public CTDataRefs getDataRefs() {
        return dataRefs;
    }

    /**
     * Sets the value of the dataRefs property.
     * 
     * @param value
     *     allowed object is
     *     {@link CTDataRefs }
     *     
     */
    public void setDataRefs(CTDataRefs value) {
        this.dataRefs = value;
    }

    /**
     * Gets the value of the function property.
     * 
     * @return
     *     possible object is
     *     {@link STDataConsolidateFunction }
     *     
     */
    public STDataConsolidateFunction getFunction() {
        if (function == null) {
            return STDataConsolidateFunction.SUM;
        } else {
            return function;
        }
    }

    /**
     * Sets the value of the function property.
     * 
     * @param value
     *     allowed object is
     *     {@link STDataConsolidateFunction }
     *     
     */
    public void setFunction(STDataConsolidateFunction value) {
        this.function = value;
    }

    /**
     * Gets the value of the startLabels property.
     * 
     * @return
     *     possible object is
     *     {@link Boolean }
     *     
     */
    public boolean isStartLabels() {
        if (startLabels == null) {
            return false;
        } else {
            return startLabels;
        }
    }

    /**
     * Sets the value of the startLabels property.
     * 
     * @param value
     *     allowed object is
     *     {@link Boolean }
     *     
     */
    public void setStartLabels(Boolean value) {
        this.startLabels = value;
    }

    /**
     * Gets the value of the leftLabels property.
     * 
     * @return
     *     possible object is
     *     {@link Boolean }
     *     
     */
    public boolean isLeftLabels() {
        if (leftLabels == null) {
            return false;
        } else {
            return leftLabels;
        }
    }

    /**
     * Sets the value of the leftLabels property.
     * 
     * @param value
     *     allowed object is
     *     {@link Boolean }
     *     
     */
    public void setLeftLabels(Boolean value) {
        this.leftLabels = value;
    }

    /**
     * Gets the value of the topLabels property.
     * 
     * @return
     *     possible object is
     *     {@link Boolean }
     *     
     */
    public boolean isTopLabels() {
        if (topLabels == null) {
            return false;
        } else {
            return topLabels;
        }
    }

    /**
     * Sets the value of the topLabels property.
     * 
     * @param value
     *     allowed object is
     *     {@link Boolean }
     *     
     */
    public void setTopLabels(Boolean value) {
        this.topLabels = value;
    }

    /**
     * Gets the value of the link property.
     * 
     * @return
     *     possible object is
     *     {@link Boolean }
     *     
     */
    public boolean isLink() {
        if (link == null) {
            return false;
        } else {
            return link;
        }
    }

    /**
     * Sets the value of the link property.
     * 
     * @param value
     *     allowed object is
     *     {@link Boolean }
     *     
     */
    public void setLink(Boolean value) {
        this.link = value;
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
