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


package org.docx4j.dml.chart;

import org.docx4j.dml.ArrayListDml;
import java.util.List;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlType;


/**
 * <p>Java class for CT_PivotSource complex type.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
 * 
 * <pre>
 * &lt;complexType name="CT_PivotSource">
 *   &lt;complexContent>
 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
 *       &lt;sequence>
 *         &lt;element name="name" type="{http://schemas.openxmlformats.org/drawingml/2006/chart}ST_Xstring"/>
 *         &lt;element name="fmtId" type="{http://schemas.openxmlformats.org/drawingml/2006/chart}CT_UnsignedInt"/>
 *         &lt;element name="extLst" type="{http://schemas.openxmlformats.org/drawingml/2006/chart}CT_ExtensionList" maxOccurs="unbounded" minOccurs="0"/>
 *       &lt;/sequence>
 *     &lt;/restriction>
 *   &lt;/complexContent>
 * &lt;/complexType>
 * </pre>
 * 
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "CT_PivotSource", propOrder = {
    "name",
    "fmtId",
    "extLst"
})
public class CTPivotSource {

    @XmlElement(required = true)
    protected String name;
    @XmlElement(required = true)
    protected CTUnsignedInt fmtId;
    protected List<CTExtensionList> extLst  = new ArrayListDml<CTExtensionList>(this);

    /**
     * Gets the value of the name property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getName() {
        return name;
    }

    /**
     * Sets the value of the name property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setName(String value) {
        this.name = value;
    }

    /**
     * Gets the value of the fmtId property.
     * 
     * @return
     *     possible object is
     *     {@link CTUnsignedInt }
     *     
     */
    public CTUnsignedInt getFmtId() {
        return fmtId;
    }

    /**
     * Sets the value of the fmtId property.
     * 
     * @param value
     *     allowed object is
     *     {@link CTUnsignedInt }
     *     
     */
    public void setFmtId(CTUnsignedInt value) {
        this.fmtId = value;
    }

    /**
     * Gets the value of the extLst property.
     * 
     * <p>
     * This accessor method returns a reference to the live list,
     * not a snapshot. Therefore any modification you make to the
     * returned list will be present inside the JAXB object.
     * This is why there is not a <CODE>set</CODE> method for the extLst property.
     * 
     * <p>
     * For example, to add a new item, do as follows:
     * <pre>
     *    getExtLst().add(newItem);
     * </pre>
     * 
     * 
     * <p>
     * Objects of the following type(s) are allowed in the list
     * {@link CTExtensionList }
     * 
     * 
     */
    public List<CTExtensionList> getExtLst() {
        if (extLst == null) {
            extLst = new ArrayListDml<CTExtensionList>(this);
        }
        return this.extLst;
    }

}
