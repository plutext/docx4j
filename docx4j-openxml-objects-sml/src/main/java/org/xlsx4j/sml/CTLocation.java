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
 * <p>Java class for CT_Location complex type.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
 * 
 * <pre>
 * &lt;complexType name="CT_Location">
 *   &lt;complexContent>
 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
 *       &lt;attribute name="ref" use="required" type="{http://schemas.openxmlformats.org/spreadsheetml/2006/main}ST_Ref" />
 *       &lt;attribute name="firstHeaderRow" use="required" type="{http://www.w3.org/2001/XMLSchema}unsignedInt" />
 *       &lt;attribute name="firstDataRow" use="required" type="{http://www.w3.org/2001/XMLSchema}unsignedInt" />
 *       &lt;attribute name="firstDataCol" use="required" type="{http://www.w3.org/2001/XMLSchema}unsignedInt" />
 *       &lt;attribute name="rowPageCount" type="{http://www.w3.org/2001/XMLSchema}unsignedInt" default="0" />
 *       &lt;attribute name="colPageCount" type="{http://www.w3.org/2001/XMLSchema}unsignedInt" default="0" />
 *     &lt;/restriction>
 *   &lt;/complexContent>
 * &lt;/complexType>
 * </pre>
 * 
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "CT_Location")
public class CTLocation implements Child
{

    @XmlAttribute(name = "ref", required = true)
    protected String ref;
    @XmlAttribute(name = "firstHeaderRow", required = true)
    @XmlSchemaType(name = "unsignedInt")
    protected long firstHeaderRow;
    @XmlAttribute(name = "firstDataRow", required = true)
    @XmlSchemaType(name = "unsignedInt")
    protected long firstDataRow;
    @XmlAttribute(name = "firstDataCol", required = true)
    @XmlSchemaType(name = "unsignedInt")
    protected long firstDataCol;
    @XmlAttribute(name = "rowPageCount")
    @XmlSchemaType(name = "unsignedInt")
    protected Long rowPageCount;
    @XmlAttribute(name = "colPageCount")
    @XmlSchemaType(name = "unsignedInt")
    protected Long colPageCount;
    @XmlTransient
    private Object parent;

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
     * Gets the value of the firstHeaderRow property.
     * 
     */
    public long getFirstHeaderRow() {
        return firstHeaderRow;
    }

    /**
     * Sets the value of the firstHeaderRow property.
     * 
     */
    public void setFirstHeaderRow(long value) {
        this.firstHeaderRow = value;
    }

    /**
     * Gets the value of the firstDataRow property.
     * 
     */
    public long getFirstDataRow() {
        return firstDataRow;
    }

    /**
     * Sets the value of the firstDataRow property.
     * 
     */
    public void setFirstDataRow(long value) {
        this.firstDataRow = value;
    }

    /**
     * Gets the value of the firstDataCol property.
     * 
     */
    public long getFirstDataCol() {
        return firstDataCol;
    }

    /**
     * Sets the value of the firstDataCol property.
     * 
     */
    public void setFirstDataCol(long value) {
        this.firstDataCol = value;
    }

    /**
     * Gets the value of the rowPageCount property.
     * 
     * @return
     *     possible object is
     *     {@link Long }
     *     
     */
    public long getRowPageCount() {
        if (rowPageCount == null) {
            return  0L;
        } else {
            return rowPageCount;
        }
    }

    /**
     * Sets the value of the rowPageCount property.
     * 
     * @param value
     *     allowed object is
     *     {@link Long }
     *     
     */
    public void setRowPageCount(Long value) {
        this.rowPageCount = value;
    }

    /**
     * Gets the value of the colPageCount property.
     * 
     * @return
     *     possible object is
     *     {@link Long }
     *     
     */
    public long getColPageCount() {
        if (colPageCount == null) {
            return  0L;
        } else {
            return colPageCount;
        }
    }

    /**
     * Sets the value of the colPageCount property.
     * 
     * @param value
     *     allowed object is
     *     {@link Long }
     *     
     */
    public void setColPageCount(Long value) {
        this.colPageCount = value;
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
