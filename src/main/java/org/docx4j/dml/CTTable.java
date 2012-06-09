/*
 * Copyright 2012 Plutext Pty Ltd.
 * 
 * This file is part of docx4j.
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
 
package org.docx4j.dml;

import java.util.ArrayList;
import java.util.List;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlType;


/**
 * <p>Java class for CT_Table complex type.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
 * 
 * <pre>
 * &lt;complexType name="CT_Table">
 *   &lt;complexContent>
 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
 *       &lt;sequence>
 *         &lt;element name="tblPr" type="{http://schemas.openxmlformats.org/drawingml/2006/main}CT_TableProperties" minOccurs="0"/>
 *         &lt;element name="tblGrid" type="{http://schemas.openxmlformats.org/drawingml/2006/main}CT_TableGrid"/>
 *         &lt;element name="tr" type="{http://schemas.openxmlformats.org/drawingml/2006/main}CT_TableRow" maxOccurs="unbounded" minOccurs="0"/>
 *       &lt;/sequence>
 *     &lt;/restriction>
 *   &lt;/complexContent>
 * &lt;/complexType>
 * </pre>
 * 
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "CT_Table", propOrder = {
    "tblPr",
    "tblGrid",
    "tr"
})
public class CTTable {

    protected CTTableProperties tblPr;
    @XmlElement(required = true)
    protected CTTableGrid tblGrid;
    protected List<CTTableRow> tr;

    /**
     * Gets the value of the tblPr property.
     * 
     * @return
     *     possible object is
     *     {@link CTTableProperties }
     *     
     */
    public CTTableProperties getTblPr() {
        return tblPr;
    }

    /**
     * Sets the value of the tblPr property.
     * 
     * @param value
     *     allowed object is
     *     {@link CTTableProperties }
     *     
     */
    public void setTblPr(CTTableProperties value) {
        this.tblPr = value;
    }

    /**
     * Gets the value of the tblGrid property.
     * 
     * @return
     *     possible object is
     *     {@link CTTableGrid }
     *     
     */
    public CTTableGrid getTblGrid() {
        return tblGrid;
    }

    /**
     * Sets the value of the tblGrid property.
     * 
     * @param value
     *     allowed object is
     *     {@link CTTableGrid }
     *     
     */
    public void setTblGrid(CTTableGrid value) {
        this.tblGrid = value;
    }

    /**
     * Gets the value of the tr property.
     * 
     * <p>
     * This accessor method returns a reference to the live list,
     * not a snapshot. Therefore any modification you make to the
     * returned list will be present inside the JAXB object.
     * This is why there is not a <CODE>set</CODE> method for the tr property.
     * 
     * <p>
     * For example, to add a new item, do as follows:
     * <pre>
     *    getTr().add(newItem);
     * </pre>
     * 
     * 
     * <p>
     * Objects of the following type(s) are allowed in the list
     * {@link CTTableRow }
     * 
     * 
     */
    public List<CTTableRow> getTr() {
        if (tr == null) {
            tr = new ArrayList<CTTableRow>();
        }
        return this.tr;
    }

}
