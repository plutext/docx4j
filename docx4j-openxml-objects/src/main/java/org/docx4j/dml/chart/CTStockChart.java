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
import javax.xml.bind.Unmarshaller;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlTransient;
import javax.xml.bind.annotation.XmlType;
import org.jvnet.jaxb2_commons.ppp.Child;


/**
 * <p>Java class for CT_StockChart complex type.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
 * 
 * <pre>
 * &lt;complexType name="CT_StockChart"&gt;
 *   &lt;complexContent&gt;
 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType"&gt;
 *       &lt;sequence&gt;
 *         &lt;element name="ser" type="{http://schemas.openxmlformats.org/drawingml/2006/chart}CT_LineSer" maxOccurs="4" minOccurs="3"/&gt;
 *         &lt;element name="dLbls" type="{http://schemas.openxmlformats.org/drawingml/2006/chart}CT_DLbls" minOccurs="0"/&gt;
 *         &lt;element name="dropLines" type="{http://schemas.openxmlformats.org/drawingml/2006/chart}CT_ChartLines" minOccurs="0"/&gt;
 *         &lt;element name="hiLowLines" type="{http://schemas.openxmlformats.org/drawingml/2006/chart}CT_ChartLines" minOccurs="0"/&gt;
 *         &lt;element name="upDownBars" type="{http://schemas.openxmlformats.org/drawingml/2006/chart}CT_UpDownBars" minOccurs="0"/&gt;
 *         &lt;element name="axId" type="{http://schemas.openxmlformats.org/drawingml/2006/chart}CT_UnsignedInt" maxOccurs="2" minOccurs="2"/&gt;
 *         &lt;element name="extLst" type="{http://schemas.openxmlformats.org/drawingml/2006/chart}CT_ExtensionList" minOccurs="0"/&gt;
 *       &lt;/sequence&gt;
 *     &lt;/restriction&gt;
 *   &lt;/complexContent&gt;
 * &lt;/complexType&gt;
 * </pre>
 * 
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "CT_StockChart", propOrder = {
    "ser",
    "dLbls",
    "dropLines",
    "hiLowLines",
    "upDownBars",
    "axId",
    "extLst"
})
public class CTStockChart implements Child
{

    @XmlElement(required = true)
    protected List<CTLineSer> ser = new ArrayListDml<CTLineSer>(this);
    protected CTDLbls dLbls;
    protected CTChartLines dropLines;
    protected CTChartLines hiLowLines;
    protected CTUpDownBars upDownBars;
    @XmlElement(required = true)
    protected List<CTUnsignedInt> axId = new ArrayListDml<CTUnsignedInt>(this);
    protected CTExtensionList extLst;
    @XmlTransient
    private Object parent;

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
     * {@link CTLineSer }
     * 
     * 
     */
    public List<CTLineSer> getSer() {
        if (ser == null) {
            ser = new ArrayListDml<CTLineSer>(this);
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
     * Gets the value of the dropLines property.
     * 
     * @return
     *     possible object is
     *     {@link CTChartLines }
     *     
     */
    public CTChartLines getDropLines() {
        return dropLines;
    }

    /**
     * Sets the value of the dropLines property.
     * 
     * @param value
     *     allowed object is
     *     {@link CTChartLines }
     *     
     */
    public void setDropLines(CTChartLines value) {
        this.dropLines = value;
    }

    /**
     * Gets the value of the hiLowLines property.
     * 
     * @return
     *     possible object is
     *     {@link CTChartLines }
     *     
     */
    public CTChartLines getHiLowLines() {
        return hiLowLines;
    }

    /**
     * Sets the value of the hiLowLines property.
     * 
     * @param value
     *     allowed object is
     *     {@link CTChartLines }
     *     
     */
    public void setHiLowLines(CTChartLines value) {
        this.hiLowLines = value;
    }

    /**
     * Gets the value of the upDownBars property.
     * 
     * @return
     *     possible object is
     *     {@link CTUpDownBars }
     *     
     */
    public CTUpDownBars getUpDownBars() {
        return upDownBars;
    }

    /**
     * Sets the value of the upDownBars property.
     * 
     * @param value
     *     allowed object is
     *     {@link CTUpDownBars }
     *     
     */
    public void setUpDownBars(CTUpDownBars value) {
        this.upDownBars = value;
    }

    /**
     * Gets the value of the axId property.
     * 
     * <p>
     * This accessor method returns a reference to the live list,
     * not a snapshot. Therefore any modification you make to the
     * returned list will be present inside the JAXB object.
     * This is why there is not a <CODE>set</CODE> method for the axId property.
     * 
     * <p>
     * For example, to add a new item, do as follows:
     * <pre>
     *    getAxId().add(newItem);
     * </pre>
     * 
     * 
     * <p>
     * Objects of the following type(s) are allowed in the list
     * {@link CTUnsignedInt }
     * 
     * 
     */
    public List<CTUnsignedInt> getAxId() {
        if (axId == null) {
            axId = new ArrayListDml<CTUnsignedInt>(this);
        }
        return this.axId;
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
