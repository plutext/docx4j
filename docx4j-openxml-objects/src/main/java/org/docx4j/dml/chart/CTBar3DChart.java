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
 * <p>Java class for CT_Bar3DChart complex type.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
 * 
 * <pre>
 * &lt;complexType name="CT_Bar3DChart">
 *   &lt;complexContent>
 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
 *       &lt;sequence>
 *         &lt;group ref="{http://schemas.openxmlformats.org/drawingml/2006/chart}EG_BarChartShared"/>
 *         &lt;element name="gapWidth" type="{http://schemas.openxmlformats.org/drawingml/2006/chart}CT_GapAmount" minOccurs="0"/>
 *         &lt;element name="gapDepth" type="{http://schemas.openxmlformats.org/drawingml/2006/chart}CT_GapAmount" minOccurs="0"/>
 *         &lt;element name="shape" type="{http://schemas.openxmlformats.org/drawingml/2006/chart}CT_Shape" minOccurs="0"/>
 *         &lt;element name="axId" type="{http://schemas.openxmlformats.org/drawingml/2006/chart}CT_UnsignedInt" maxOccurs="3" minOccurs="2"/>
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
@XmlType(name = "CT_Bar3DChart", propOrder = {
    "barDir",
    "grouping",
    "varyColors",
    "ser",
    "dLbls",
    "gapWidth",
    "gapDepth",
    "shape",
    "axId",
    "extLst"
})
public class CTBar3DChart implements ListSer {

    @XmlElement(required = true)
    protected CTBarDir barDir;
    protected CTBarGrouping grouping;
    protected CTBoolean varyColors;
    protected List<CTBarSer> ser = new ArrayListDml<CTBarSer>(this);
    protected CTDLbls dLbls;
    protected CTGapAmount gapWidth;
    protected CTGapAmount gapDepth;
    protected CTShape shape;
    @XmlElement(required = true)
    protected List<CTUnsignedInt> axId = new ArrayListDml<CTUnsignedInt>(this);
    protected CTExtensionList extLst;

    /**
     * Gets the value of the barDir property.
     * 
     * @return
     *     possible object is
     *     {@link CTBarDir }
     *     
     */
    public CTBarDir getBarDir() {
        return barDir;
    }

    /**
     * Sets the value of the barDir property.
     * 
     * @param value
     *     allowed object is
     *     {@link CTBarDir }
     *     
     */
    public void setBarDir(CTBarDir value) {
        this.barDir = value;
    }

    /**
     * Gets the value of the grouping property.
     * 
     * @return
     *     possible object is
     *     {@link CTBarGrouping }
     *     
     */
    public CTBarGrouping getGrouping() {
        return grouping;
    }

    /**
     * Sets the value of the grouping property.
     * 
     * @param value
     *     allowed object is
     *     {@link CTBarGrouping }
     *     
     */
    public void setGrouping(CTBarGrouping value) {
        this.grouping = value;
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
     * {@link CTBarSer }
     * 
     * 
     */
    public List<CTBarSer> getSer() {
        if (ser == null) {
            ser = new ArrayListDml<CTBarSer>(this);
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
     * Gets the value of the gapDepth property.
     * 
     * @return
     *     possible object is
     *     {@link CTGapAmount }
     *     
     */
    public CTGapAmount getGapDepth() {
        return gapDepth;
    }

    /**
     * Sets the value of the gapDepth property.
     * 
     * @param value
     *     allowed object is
     *     {@link CTGapAmount }
     *     
     */
    public void setGapDepth(CTGapAmount value) {
        this.gapDepth = value;
    }

    /**
     * Gets the value of the shape property.
     * 
     * @return
     *     possible object is
     *     {@link CTShape }
     *     
     */
    public CTShape getShape() {
        return shape;
    }

    /**
     * Sets the value of the shape property.
     * 
     * @param value
     *     allowed object is
     *     {@link CTShape }
     *     
     */
    public void setShape(CTShape value) {
        this.shape = value;
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

}
