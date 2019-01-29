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
 * <p>Java class for CT_PieChart complex type.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
 * 
 * <pre>
 * &lt;complexType name="CT_PieChart">
 *   &lt;complexContent>
 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
 *       &lt;sequence>
 *         &lt;group ref="{http://schemas.openxmlformats.org/drawingml/2006/chart}EG_PieChartShared"/>
 *         &lt;element name="firstSliceAng" type="{http://schemas.openxmlformats.org/drawingml/2006/chart}CT_FirstSliceAng" minOccurs="0"/>
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
@XmlType(name = "CT_PieChart", propOrder = {
    "varyColors",
    "ser",
    "dLbls",
    "firstSliceAng",
    "extLst"
})
public class CTPieChart implements ListSer {

    protected CTBoolean varyColors;
    protected List<CTPieSer> ser  = new ArrayListDml<CTPieSer>(this);
    protected CTDLbls dLbls;
    protected CTFirstSliceAng firstSliceAng;
    protected CTExtensionList extLst;

    /**
     * Gets the value of the varyColors property.
     * 
     * @return
     *     possible object is
     *     {@link CTBoolean }
     *     
     */
    public CTBoolean getVaryColors() {
        return varyColors;
    }

    /**
     * Sets the value of the varyColors property.
     * 
     * @param value
     *     allowed object is
     *     {@link CTBoolean }
     *     
     */
    public void setVaryColors(CTBoolean value) {
        this.varyColors = value;
    }

    /**
     * Gets the value of the ser property.
     * 
     * <p>
     * This accessor method returns a reference to the live list,
     * not a snapshot. Therefore any modification you make to the
     * returned list will be present inside the JAXB object.
     * This is why there is not a <CODE>set</CODE> method for the ser property.
     * 
     * <p>
     * For example, to add a new item, do as follows:
     * <pre>
     *    getSer().add(newItem);
     * </pre>
     * 
     * 
     * <p>
     * Objects of the following type(s) are allowed in the list
     * {@link CTPieSer }
     * 
     * 
     */
    public List<CTPieSer> getSer() {
        if (ser == null) {
            ser = new ArrayListDml<CTPieSer>(this);
        }
        return this.ser;
    }

    /**
     * Gets the value of the dLbls property.
     * 
     * @return
     *     possible object is
     *     {@link CTDLbls }
     *     
     */
    public CTDLbls getDLbls() {
        return dLbls;
    }

    /**
     * Sets the value of the dLbls property.
     * 
     * @param value
     *     allowed object is
     *     {@link CTDLbls }
     *     
     */
    public void setDLbls(CTDLbls value) {
        this.dLbls = value;
    }

    /**
     * Gets the value of the firstSliceAng property.
     * 
     * @return
     *     possible object is
     *     {@link CTFirstSliceAng }
     *     
     */
    public CTFirstSliceAng getFirstSliceAng() {
        return firstSliceAng;
    }

    /**
     * Sets the value of the firstSliceAng property.
     * 
     * @param value
     *     allowed object is
     *     {@link CTFirstSliceAng }
     *     
     */
    public void setFirstSliceAng(CTFirstSliceAng value) {
        this.firstSliceAng = value;
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
