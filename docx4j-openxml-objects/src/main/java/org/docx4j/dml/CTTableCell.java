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


package org.docx4j.dml;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlType;


/**
 * <p>Java class for CT_TableCell complex type.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
 * 
 * <pre>
 * &lt;complexType name="CT_TableCell">
 *   &lt;complexContent>
 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
 *       &lt;sequence>
 *         &lt;element name="txBody" type="{http://schemas.openxmlformats.org/drawingml/2006/main}CT_TextBody" minOccurs="0"/>
 *         &lt;element name="tcPr" type="{http://schemas.openxmlformats.org/drawingml/2006/main}CT_TableCellProperties" minOccurs="0"/>
 *         &lt;element name="extLst" type="{http://schemas.openxmlformats.org/drawingml/2006/main}CT_OfficeArtExtensionList" minOccurs="0"/>
 *       &lt;/sequence>
 *       &lt;attribute name="rowSpan" type="{http://www.w3.org/2001/XMLSchema}int" default="1" />
 *       &lt;attribute name="gridSpan" type="{http://www.w3.org/2001/XMLSchema}int" default="1" />
 *       &lt;attribute name="hMerge" type="{http://www.w3.org/2001/XMLSchema}boolean" default="false" />
 *       &lt;attribute name="vMerge" type="{http://www.w3.org/2001/XMLSchema}boolean" default="false" />
 *     &lt;/restriction>
 *   &lt;/complexContent>
 * &lt;/complexType>
 * </pre>
 * 
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "CT_TableCell", propOrder = {
    "txBody",
    "tcPr",
    "extLst"
})
public class CTTableCell {

    protected CTTextBody txBody;
    protected CTTableCellProperties tcPr;
    protected CTOfficeArtExtensionList extLst;
    @XmlAttribute
    protected Integer rowSpan;
    @XmlAttribute
    protected Integer gridSpan;
    @XmlAttribute
    protected Boolean hMerge;
    @XmlAttribute
    protected Boolean vMerge;

    /**
     * Gets the value of the txBody property.
     * 
     * @return
     *     possible object is
     *     {@link CTTextBody }
     *     
     */
    public CTTextBody getTxBody() {
        return txBody;
    }

    /**
     * Sets the value of the txBody property.
     * 
     * @param value
     *     allowed object is
     *     {@link CTTextBody }
     *     
     */
    public void setTxBody(CTTextBody value) {
        this.txBody = value;
    }

    /**
     * Gets the value of the tcPr property.
     * 
     * @return
     *     possible object is
     *     {@link CTTableCellProperties }
     *     
     */
    public CTTableCellProperties getTcPr() {
        return tcPr;
    }

    /**
     * Sets the value of the tcPr property.
     * 
     * @param value
     *     allowed object is
     *     {@link CTTableCellProperties }
     *     
     */
    public void setTcPr(CTTableCellProperties value) {
        this.tcPr = value;
    }

    /**
     * Gets the value of the extLst property.
     * 
     * @return
     *     possible object is
     *     {@link CTOfficeArtExtensionList }
     *     
     */
    public CTOfficeArtExtensionList getExtLst() {
        return extLst;
    }

    /**
     * Sets the value of the extLst property.
     * 
     * @param value
     *     allowed object is
     *     {@link CTOfficeArtExtensionList }
     *     
     */
    public void setExtLst(CTOfficeArtExtensionList value) {
        this.extLst = value;
    }

    /**
     * Gets the value of the rowSpan property.
     * 
     * @return
     *     possible object is
     *     {@link Integer }
     *     
     */
    public int getRowSpan() {
        if (rowSpan == null) {
            return  1;
        } else {
            return rowSpan;
        }
    }

    /**
     * Sets the value of the rowSpan property.
     * 
     * @param value
     *     allowed object is
     *     {@link Integer }
     *     
     */
    public void setRowSpan(Integer value) {
        this.rowSpan = value;
    }

    /**
     * Gets the value of the gridSpan property.
     * 
     * @return
     *     possible object is
     *     {@link Integer }
     *     
     */
    public int getGridSpan() {
        if (gridSpan == null) {
            return  1;
        } else {
            return gridSpan;
        }
    }

    /**
     * Sets the value of the gridSpan property.
     * 
     * @param value
     *     allowed object is
     *     {@link Integer }
     *     
     */
    public void setGridSpan(Integer value) {
        this.gridSpan = value;
    }

    /**
     * Gets the value of the hMerge property.
     * 
     * @return
     *     possible object is
     *     {@link Boolean }
     *     
     */
    public boolean isHMerge() {
        if (hMerge == null) {
            return false;
        } else {
            return hMerge;
        }
    }

    /**
     * Sets the value of the hMerge property.
     * 
     * @param value
     *     allowed object is
     *     {@link Boolean }
     *     
     */
    public void setHMerge(Boolean value) {
        this.hMerge = value;
    }

    /**
     * Gets the value of the vMerge property.
     * 
     * @return
     *     possible object is
     *     {@link Boolean }
     *     
     */
    public boolean isVMerge() {
        if (vMerge == null) {
            return false;
        } else {
            return vMerge;
        }
    }

    /**
     * Sets the value of the vMerge property.
     * 
     * @param value
     *     allowed object is
     *     {@link Boolean }
     *     
     */
    public void setVMerge(Boolean value) {
        this.vMerge = value;
    }

}
