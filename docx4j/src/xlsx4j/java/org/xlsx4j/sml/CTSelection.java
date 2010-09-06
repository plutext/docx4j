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

import java.util.ArrayList;
import java.util.List;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlSchemaType;
import javax.xml.bind.annotation.XmlType;


/**
 * <p>Java class for CT_Selection complex type.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
 * 
 * <pre>
 * &lt;complexType name="CT_Selection">
 *   &lt;complexContent>
 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
 *       &lt;attribute name="pane" type="{http://schemas.openxmlformats.org/spreadsheetml/2006/main}ST_Pane" default="topLeft" />
 *       &lt;attribute name="activeCell" type="{http://schemas.openxmlformats.org/spreadsheetml/2006/main}ST_CellRef" />
 *       &lt;attribute name="activeCellId" type="{http://www.w3.org/2001/XMLSchema}unsignedInt" default="0" />
 *       &lt;attribute name="sqref" type="{http://schemas.openxmlformats.org/spreadsheetml/2006/main}ST_Sqref" default="A1" />
 *     &lt;/restriction>
 *   &lt;/complexContent>
 * &lt;/complexType>
 * </pre>
 * 
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "CT_Selection")
public class CTSelection {

    @XmlAttribute
    protected STPane pane;
    @XmlAttribute
    protected String activeCell;
    @XmlAttribute
    @XmlSchemaType(name = "unsignedInt")
    protected Long activeCellId;
    @XmlAttribute
    protected List<String> sqref;

    /**
     * Gets the value of the pane property.
     * 
     * @return
     *     possible object is
     *     {@link STPane }
     *     
     */
    public STPane getPane() {
        if (pane == null) {
            return STPane.TOP_LEFT;
        } else {
            return pane;
        }
    }

    /**
     * Sets the value of the pane property.
     * 
     * @param value
     *     allowed object is
     *     {@link STPane }
     *     
     */
    public void setPane(STPane value) {
        this.pane = value;
    }

    /**
     * Gets the value of the activeCell property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getActiveCell() {
        return activeCell;
    }

    /**
     * Sets the value of the activeCell property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setActiveCell(String value) {
        this.activeCell = value;
    }

    /**
     * Gets the value of the activeCellId property.
     * 
     * @return
     *     possible object is
     *     {@link Long }
     *     
     */
    public long getActiveCellId() {
        if (activeCellId == null) {
            return  0L;
        } else {
            return activeCellId;
        }
    }

    /**
     * Sets the value of the activeCellId property.
     * 
     * @param value
     *     allowed object is
     *     {@link Long }
     *     
     */
    public void setActiveCellId(Long value) {
        this.activeCellId = value;
    }

    /**
     * Gets the value of the sqref property.
     * 
     * <p>
     * This accessor method returns a reference to the live list,
     * not a snapshot. Therefore any modification you make to the
     * returned list will be present inside the JAXB object.
     * This is why there is not a <CODE>set</CODE> method for the sqref property.
     * 
     * <p>
     * For example, to add a new item, do as follows:
     * <pre>
     *    getSqref().add(newItem);
     * </pre>
     * 
     * 
     * <p>
     * Objects of the following type(s) are allowed in the list
     * {@link String }
     * 
     * 
     */
    public List<String> getSqref() {
        if (sqref == null) {
            sqref = new ArrayList<String>();
        }
        return this.sqref;
    }

}
