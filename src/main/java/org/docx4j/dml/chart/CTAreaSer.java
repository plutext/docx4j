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
 
package org.docx4j.dml.chart;

import java.util.ArrayList;
import java.util.List;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlType;
import org.docx4j.dml.CTShapeProperties;


/**
 * <p>Java class for CT_AreaSer complex type.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
 * 
 * <pre>
 * &lt;complexType name="CT_AreaSer">
 *   &lt;complexContent>
 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
 *       &lt;sequence>
 *         &lt;group ref="{http://schemas.openxmlformats.org/drawingml/2006/chart}EG_SerShared"/>
 *         &lt;element name="pictureOptions" type="{http://schemas.openxmlformats.org/drawingml/2006/chart}CT_PictureOptions" minOccurs="0"/>
 *         &lt;element name="dPt" type="{http://schemas.openxmlformats.org/drawingml/2006/chart}CT_DPt" maxOccurs="unbounded" minOccurs="0"/>
 *         &lt;element name="dLbls" type="{http://schemas.openxmlformats.org/drawingml/2006/chart}CT_DLbls" minOccurs="0"/>
 *         &lt;element name="trendline" type="{http://schemas.openxmlformats.org/drawingml/2006/chart}CT_Trendline" maxOccurs="unbounded" minOccurs="0"/>
 *         &lt;element name="errBars" type="{http://schemas.openxmlformats.org/drawingml/2006/chart}CT_ErrBars" maxOccurs="2" minOccurs="0"/>
 *         &lt;element name="cat" type="{http://schemas.openxmlformats.org/drawingml/2006/chart}CT_AxDataSource" minOccurs="0"/>
 *         &lt;element name="val" type="{http://schemas.openxmlformats.org/drawingml/2006/chart}CT_NumDataSource" minOccurs="0"/>
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
@XmlType(name = "CT_AreaSer", propOrder = {
    "idx",
    "order",
    "tx",
    "spPr",
    "pictureOptions",
    "dPt",
    "dLbls",
    "trendline",
    "errBars",
    "cat",
    "val",
    "extLst"
})
public class CTAreaSer implements SerContent {

    @XmlElement(required = true)
    protected CTUnsignedInt idx;
    @XmlElement(required = true)
    protected CTUnsignedInt order;
    protected CTSerTx tx;
    protected CTShapeProperties spPr;
    protected CTPictureOptions pictureOptions;
    protected List<CTDPt> dPt;
    protected CTDLbls dLbls;
    protected List<CTTrendline> trendline;
    protected List<CTErrBars> errBars;
    protected CTAxDataSource cat;
    protected CTNumDataSource val;
    protected CTExtensionList extLst;

    /**
     * Gets the value of the idx property.
     * 
     * @return
     *     possible object is
     *     {@link CTUnsignedInt }
     *     
     */
    public CTUnsignedInt getIdx() {
        return idx;
    }

    /**
     * Sets the value of the idx property.
     * 
     * @param value
     *     allowed object is
     *     {@link CTUnsignedInt }
     *     
     */
    public void setIdx(CTUnsignedInt value) {
        this.idx = value;
    }

    /**
     * Gets the value of the order property.
     * 
     * @return
     *     possible object is
     *     {@link CTUnsignedInt }
     *     
     */
    public CTUnsignedInt getOrder() {
        return order;
    }

    /**
     * Sets the value of the order property.
     * 
     * @param value
     *     allowed object is
     *     {@link CTUnsignedInt }
     *     
     */
    public void setOrder(CTUnsignedInt value) {
        this.order = value;
    }

    /**
     * Gets the value of the tx property.
     * 
     * @return
     *     possible object is
     *     {@link CTSerTx }
     *     
     */
    public CTSerTx getTx() {
        return tx;
    }

    /**
     * Sets the value of the tx property.
     * 
     * @param value
     *     allowed object is
     *     {@link CTSerTx }
     *     
     */
    public void setTx(CTSerTx value) {
        this.tx = value;
    }

    /**
     * Gets the value of the spPr property.
     * 
     * @return
     *     possible object is
     *     {@link CTShapeProperties }
     *     
     */
    public CTShapeProperties getSpPr() {
        return spPr;
    }

    /**
     * Sets the value of the spPr property.
     * 
     * @param value
     *     allowed object is
     *     {@link CTShapeProperties }
     *     
     */
    public void setSpPr(CTShapeProperties value) {
        this.spPr = value;
    }

    /**
     * Gets the value of the pictureOptions property.
     * 
     * @return
     *     possible object is
     *     {@link CTPictureOptions }
     *     
     */
    public CTPictureOptions getPictureOptions() {
        return pictureOptions;
    }

    /**
     * Sets the value of the pictureOptions property.
     * 
     * @param value
     *     allowed object is
     *     {@link CTPictureOptions }
     *     
     */
    public void setPictureOptions(CTPictureOptions value) {
        this.pictureOptions = value;
    }

    /**
     * Gets the value of the dPt property.
     * 
     * <p>
     * This accessor method returns a reference to the live list,
     * not a snapshot. Therefore any modification you make to the
     * returned list will be present inside the JAXB object.
     * This is why there is not a <CODE>set</CODE> method for the dPt property.
     * 
     * <p>
     * For example, to add a new item, do as follows:
     * <pre>
     *    getDPt().add(newItem);
     * </pre>
     * 
     * 
     * <p>
     * Objects of the following type(s) are allowed in the list
     * {@link CTDPt }
     * 
     * 
     */
    public List<CTDPt> getDPt() {
        if (dPt == null) {
            dPt = new ArrayList<CTDPt>();
        }
        return this.dPt;
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
     * Gets the value of the trendline property.
     * 
     * <p>
     * This accessor method returns a reference to the live list,
     * not a snapshot. Therefore any modification you make to the
     * returned list will be present inside the JAXB object.
     * This is why there is not a <CODE>set</CODE> method for the trendline property.
     * 
     * <p>
     * For example, to add a new item, do as follows:
     * <pre>
     *    getTrendline().add(newItem);
     * </pre>
     * 
     * 
     * <p>
     * Objects of the following type(s) are allowed in the list
     * {@link CTTrendline }
     * 
     * 
     */
    public List<CTTrendline> getTrendline() {
        if (trendline == null) {
            trendline = new ArrayList<CTTrendline>();
        }
        return this.trendline;
    }

    /**
     * Gets the value of the errBars property.
     * 
     * <p>
     * This accessor method returns a reference to the live list,
     * not a snapshot. Therefore any modification you make to the
     * returned list will be present inside the JAXB object.
     * This is why there is not a <CODE>set</CODE> method for the errBars property.
     * 
     * <p>
     * For example, to add a new item, do as follows:
     * <pre>
     *    getErrBars().add(newItem);
     * </pre>
     * 
     * 
     * <p>
     * Objects of the following type(s) are allowed in the list
     * {@link CTErrBars }
     * 
     * 
     */
    public List<CTErrBars> getErrBars() {
        if (errBars == null) {
            errBars = new ArrayList<CTErrBars>();
        }
        return this.errBars;
    }

    /**
     * Gets the value of the cat property.
     * 
     * @return
     *     possible object is
     *     {@link CTAxDataSource }
     *     
     */
    public CTAxDataSource getCat() {
        return cat;
    }

    /**
     * Sets the value of the cat property.
     * 
     * @param value
     *     allowed object is
     *     {@link CTAxDataSource }
     *     
     */
    public void setCat(CTAxDataSource value) {
        this.cat = value;
    }

    /**
     * Gets the value of the val property.
     * 
     * @return
     *     possible object is
     *     {@link CTNumDataSource }
     *     
     */
    public CTNumDataSource getVal() {
        return val;
    }

    /**
     * Sets the value of the val property.
     * 
     * @param value
     *     allowed object is
     *     {@link CTNumDataSource }
     *     
     */
    public void setVal(CTNumDataSource value) {
        this.val = value;
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
