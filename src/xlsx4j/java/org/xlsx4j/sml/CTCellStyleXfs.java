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
import javax.xml.bind.annotation.XmlSchemaType;
import javax.xml.bind.annotation.XmlTransient;
import javax.xml.bind.annotation.XmlType;
import org.jvnet.jaxb2_commons.ppp.Child;


/**
 * <p>Java class for CT_CellStyleXfs complex type.
 * 
 * <p>This element contains the master formatting records (xf's) which define the formatting for all named cell styles in this workbook. Master formatting records reference individual elements of formatting (e.g., number format, font definitions, cell fills, etc) by specifying a zero-based index into those collections. Master formatting records also specify whether to apply or ignore particular aspects of formatting, for example whether to apply a border or not.

A cell can have both direct formatting (e.g., bold) and a cell style (e.g., Explanatory) applied to it. Therefore, both the cell style xf records and cell xf records must be read to understand the full set of formatting applied to a cell.

[Example: This example shows 4 master formatting records, each defining formatting for a named cell style (expressed in the cellStyles collection). Note that 0th record does not express any "apply" attributes, while the other records do express "apply" attribute values. For example, the last record specifies that number format, alignment, and protection formatting will not be applied to the cell, even when that information is specified in related formatting records.

<cellStyleXfs count="4">
  <xf numFmtId="0" fontId="0" fillId="0" borderId="0"/>

  <xf numFmtId="0" fontId="2" fillId="0" borderId="0" applyNumberFormat="0" 
    applyFill="0" applyBorder="0" applyAlignment="0" applyProtection="0"/>

  <xf numFmtId="0" fontId="3" fillId="0" borderId="1" applyNumberFormat="0" 
    applyFill="0" applyAlignment="0" applyProtection="0"/>

  <xf numFmtId="0" fontId="4" fillId="2" borderId="2" applyNumberFormat="0"
    applyAlignment="0" applyProtection="0"/>
</cellStyleXfs>
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
 * 
 * <pre>
 * &lt;complexType name="CT_CellStyleXfs">
 *   &lt;complexContent>
 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
 *       &lt;sequence>
 *         &lt;element name="xf" type="{http://schemas.openxmlformats.org/spreadsheetml/2006/main}CT_Xf" maxOccurs="unbounded"/>
 *       &lt;/sequence>
 *       &lt;attribute name="count" type="{http://www.w3.org/2001/XMLSchema}unsignedInt" />
 *     &lt;/restriction>
 *   &lt;/complexContent>
 * &lt;/complexType>
 * </pre>
 * 
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "CT_CellStyleXfs", propOrder = {
    "xf"
})
public class CTCellStyleXfs implements Child
{

    @XmlElement(required = true)
    protected List<CTXf> xf;
    @XmlAttribute(name = "count")
    @XmlSchemaType(name = "unsignedInt")
    protected Long count;
    @XmlTransient
    private Object parent;

    /**
     * Gets the value of the xf property.
     * 
     * <p>
     * This accessor method returns a reference to the live list,
     * not a snapshot. Therefore any modification you make to the
     * returned list will be present inside the JAXB object.
     * This is why there is not a <CODE>set</CODE> method for the xf property.
     * 
     * <p>
     * For example, to add a new item, do as follows:
     * <pre>
     *    getXf().add(newItem);
     * </pre>
     * 
     * 
     * <p>
     * Objects of the following type(s) are allowed in the list
     * {@link CTXf }
     * 
     * 
     */
    public List<CTXf> getXf() {
        if (xf == null) {
            xf = new ArrayList<CTXf>();
        }
        return this.xf;
    }

    /**
     * Gets the value of the count property.
     * 
     * @return
     *     possible object is
     *     {@link Long }
     *     
     */
    public Long getCount() {
        return count;
    }

    /**
     * Sets the value of the count property.
     * 
     * @param value
     *     allowed object is
     *     {@link Long }
     *     
     */
    public void setCount(Long value) {
        this.count = value;
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
