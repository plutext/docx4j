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
 * <p>Java class for CT_OfPieChart complex type.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
 * 
 * <pre>
 * &lt;complexType name="CT_OfPieChart"&gt;
 *   &lt;complexContent&gt;
 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType"&gt;
 *       &lt;sequence&gt;
 *         &lt;element name="ofPieType" type="{http://schemas.openxmlformats.org/drawingml/2006/chart}CT_OfPieType"/&gt;
 *         &lt;group ref="{http://schemas.openxmlformats.org/drawingml/2006/chart}EG_PieChartShared"/&gt;
 *         &lt;element name="gapWidth" type="{http://schemas.openxmlformats.org/drawingml/2006/chart}CT_GapAmount" minOccurs="0"/&gt;
 *         &lt;element name="splitType" type="{http://schemas.openxmlformats.org/drawingml/2006/chart}CT_SplitType" minOccurs="0"/&gt;
 *         &lt;element name="splitPos" type="{http://schemas.openxmlformats.org/drawingml/2006/chart}CT_Double" minOccurs="0"/&gt;
 *         &lt;element name="custSplit" type="{http://schemas.openxmlformats.org/drawingml/2006/chart}CT_CustSplit" minOccurs="0"/&gt;
 *         &lt;element name="secondPieSize" type="{http://schemas.openxmlformats.org/drawingml/2006/chart}CT_SecondPieSize" minOccurs="0"/&gt;
 *         &lt;element name="serLines" type="{http://schemas.openxmlformats.org/drawingml/2006/chart}CT_ChartLines" maxOccurs="unbounded" minOccurs="0"/&gt;
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
@XmlType(name = "CT_OfPieChart", propOrder = {
    "ofPieType",
    "varyColors",
    "ser",
    "dLbls",
    "gapWidth",
    "splitType",
    "splitPos",
    "custSplit",
    "secondPieSize",
    "serLines",
    "extLst"
})
public class CTOfPieChart implements Child
{

    @XmlElement(required = true)
    protected CTOfPieType ofPieType;
    protected CTBoolean varyColors;
    protected List<CTPieSer> ser = new ArrayListDml<CTPieSer>(this);
    protected CTDLbls dLbls;
    protected CTGapAmount gapWidth;
    protected CTSplitType splitType;
    protected CTDouble splitPos;
    protected CTCustSplit custSplit;
    protected CTSecondPieSize secondPieSize;
    protected List<CTChartLines> serLines  = new ArrayListDml<CTChartLines>(this);
    protected CTExtensionList extLst;
    @XmlTransient
    private Object parent;

    /**
     * Gets the value of the ofPieType property.
     * 
     * @return
     *     possible object is
     *     {@link CTOfPieType }
     *     
     */
    public CTOfPieType getOfPieType() {
        return ofPieType;
    }

    /**
     * Sets the value of the ofPieType property.
     * 
     * @param value
     *     allowed object is
     *     {@link CTOfPieType }
     *     
     */
    public void setOfPieType(CTOfPieType value) {
        this.ofPieType = value;
    }

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
     * Gets the value of the gapWidth property.
     * 
     * @return
     *     possible object is
     *     {@link CTGapAmount }
     *     
     */
    public CTGapAmount getGapWidth() {
        return gapWidth;
    }

    /**
     * Sets the value of the gapWidth property.
     * 
     * @param value
     *     allowed object is
     *     {@link CTGapAmount }
     *     
     */
    public void setGapWidth(CTGapAmount value) {
        this.gapWidth = value;
    }

    /**
     * Gets the value of the splitType property.
     * 
     * @return
     *     possible object is
     *     {@link CTSplitType }
     *     
     */
    public CTSplitType getSplitType() {
        return splitType;
    }

    /**
     * Sets the value of the splitType property.
     * 
     * @param value
     *     allowed object is
     *     {@link CTSplitType }
     *     
     */
    public void setSplitType(CTSplitType value) {
        this.splitType = value;
    }

    /**
     * Gets the value of the splitPos property.
     * 
     * @return
     *     possible object is
     *     {@link CTDouble }
     *     
     */
    public CTDouble getSplitPos() {
        return splitPos;
    }

    /**
     * Sets the value of the splitPos property.
     * 
     * @param value
     *     allowed object is
     *     {@link CTDouble }
     *     
     */
    public void setSplitPos(CTDouble value) {
        this.splitPos = value;
    }

    /**
     * Gets the value of the custSplit property.
     * 
     * @return
     *     possible object is
     *     {@link CTCustSplit }
     *     
     */
    public CTCustSplit getCustSplit() {
        return custSplit;
    }

    /**
     * Sets the value of the custSplit property.
     * 
     * @param value
     *     allowed object is
     *     {@link CTCustSplit }
     *     
     */
    public void setCustSplit(CTCustSplit value) {
        this.custSplit = value;
    }

    /**
     * Gets the value of the secondPieSize property.
     * 
     * @return
     *     possible object is
     *     {@link CTSecondPieSize }
     *     
     */
    public CTSecondPieSize getSecondPieSize() {
        return secondPieSize;
    }

    /**
     * Sets the value of the secondPieSize property.
     * 
     * @param value
     *     allowed object is
     *     {@link CTSecondPieSize }
     *     
     */
    public void setSecondPieSize(CTSecondPieSize value) {
        this.secondPieSize = value;
    }

    /**
     * Gets the value of the serLines property.
     * 
     * <p>
     * This accessor method returns a reference to the live list,
     * not a snapshot. Therefore any modification you make to the
     * returned list will be present inside the JAXB object.
     * This is why there is not a <CODE>set</CODE> method for the serLines property.
     * 
     * <p>
     * For example, to add a new item, do as follows:
     * <pre>
     *    getSerLines().add(newItem);
     * </pre>
     * 
     * 
     * <p>
     * Objects of the following type(s) are allowed in the list
     * {@link CTChartLines }
     * 
     * 
     */
    public List<CTChartLines> getSerLines() {
        if (serLines == null) {
            serLines = new ArrayListDml<CTChartLines>(this);
        }
        return this.serLines;
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
