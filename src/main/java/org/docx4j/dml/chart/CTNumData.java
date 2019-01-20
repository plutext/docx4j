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
import javax.xml.bind.annotation.XmlType;


/**
 * <p>Java class for CT_NumData complex type.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
 * 
 * <pre>
 * &lt;complexType name="CT_NumData">
 *   &lt;complexContent>
 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
 *       &lt;sequence>
 *         &lt;element name="formatCode" type="{http://schemas.openxmlformats.org/drawingml/2006/chart}ST_Xstring" minOccurs="0"/>
 *         &lt;element name="ptCount" type="{http://schemas.openxmlformats.org/drawingml/2006/chart}CT_UnsignedInt" minOccurs="0"/>
 *         &lt;element name="pt" type="{http://schemas.openxmlformats.org/drawingml/2006/chart}CT_NumVal" maxOccurs="unbounded" minOccurs="0"/>
 *         &lt;element name="extLst" type="{http://schemas.openxmlformats.org/drawingml/2006/chart}CT_ExtensionList" minOccurs="0"/>
 *       &lt;/sequence>
 *     &lt;/restriction>
 *   &lt;/complexContent>
 * &lt;/complexType>
 * </pre>
 * 
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "CT_NumData", propOrder = {
    "formatCode",
    "ptCount",
    "pt",
    "extLst"
})
public class CTNumData {

    protected String formatCode;
    protected CTUnsignedInt ptCount;
    protected List<CTNumVal> pt  = new ArrayListDml<CTNumVal>(this);
    protected CTExtensionList extLst;

    /**
     * Gets the value of the formatCode property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getFormatCode() {
        return formatCode;
    }

    /**
     * Sets the value of the formatCode property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setFormatCode(String value) {
        this.formatCode = value;
    }

    /**
     * Gets the value of the ptCount property.
     * 
     * @return
     *     possible object is
     *     {@link CTUnsignedInt }
     *     
     */
    public CTUnsignedInt getPtCount() {
        return ptCount;
    }

    /**
     * Sets the value of the ptCount property.
     * 
     * @param value
     *     allowed object is
     *     {@link CTUnsignedInt }
     *     
     */
    public void setPtCount(CTUnsignedInt value) {
        this.ptCount = value;
    }

    /**
     * Gets the value of the pt property.
     * 
     * <p>
     * This accessor method returns a reference to the live list,
     * not a snapshot. Therefore any modification you make to the
     * returned list will be present inside the JAXB object.
     * This is why there is not a <CODE>set</CODE> method for the pt property.
     * 
     * <p>
     * For example, to add a new item, do as follows:
     * <pre>
     *    getPt().add(newItem);
     * </pre>
     * 
     * 
     * <p>
     * Objects of the following type(s) are allowed in the list
     * {@link CTNumVal }
     * 
     * 
     */
    public List<CTNumVal> getPt() {
        if (pt == null) {
            pt = new ArrayListDml<CTNumVal>(this);
        }
        return this.pt;
    }

    /**
     * Gets the value of the extLst property.
     * 
     * @return
     *     possible object is
     *     {@link CTExtensionList }
     *     
     */
    public CTExtensionList getExtLst() {
        return extLst;
    }

    /**
     * Sets the value of the extLst property.
     * 
     * @param value
     *     allowed object is
     *     {@link CTExtensionList }
     *     
     */
    public void setExtLst(CTExtensionList value) {
        this.extLst = value;
    }

}
