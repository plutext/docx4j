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
import org.docx4j.dml.CTShapeProperties;
import org.jvnet.jaxb2_commons.ppp.Child;


/**
 * <p>Java class for CT_BubbleSer complex type.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
 * 
 * <pre>
 * &lt;complexType name="CT_BubbleSer"&gt;
 *   &lt;complexContent&gt;
 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType"&gt;
 *       &lt;sequence&gt;
 *         &lt;group ref="{http://schemas.openxmlformats.org/drawingml/2006/chart}EG_SerShared"/&gt;
 *         &lt;element name="invertIfNegative" type="{http://schemas.openxmlformats.org/drawingml/2006/chart}CT_Boolean" minOccurs="0"/&gt;
 *         &lt;element name="dPt" type="{http://schemas.openxmlformats.org/drawingml/2006/chart}CT_DPt" maxOccurs="unbounded" minOccurs="0"/&gt;
 *         &lt;element name="dLbls" type="{http://schemas.openxmlformats.org/drawingml/2006/chart}CT_DLbls" minOccurs="0"/&gt;
 *         &lt;element name="trendline" type="{http://schemas.openxmlformats.org/drawingml/2006/chart}CT_Trendline" maxOccurs="unbounded" minOccurs="0"/&gt;
 *         &lt;element name="errBars" type="{http://schemas.openxmlformats.org/drawingml/2006/chart}CT_ErrBars" maxOccurs="2" minOccurs="0"/&gt;
 *         &lt;element name="xVal" type="{http://schemas.openxmlformats.org/drawingml/2006/chart}CT_AxDataSource" minOccurs="0"/&gt;
 *         &lt;element name="yVal" type="{http://schemas.openxmlformats.org/drawingml/2006/chart}CT_NumDataSource" minOccurs="0"/&gt;
 *         &lt;element name="bubbleSize" type="{http://schemas.openxmlformats.org/drawingml/2006/chart}CT_NumDataSource" minOccurs="0"/&gt;
 *         &lt;element name="bubble3D" type="{http://schemas.openxmlformats.org/drawingml/2006/chart}CT_Boolean" minOccurs="0"/&gt;
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
@XmlType(name = "CT_BubbleSer", propOrder = {
    "idx",
    "order",
    "tx",
    "spPr",
    "invertIfNegative",
    "dPt",
    "dLbls",
    "trendline",
    "errBars",
    "xVal",
    "yVal",
    "bubbleSize",
    "bubble3D",
    "extLst"
})
public class CTBubbleSer implements Child
{

    @XmlElement(required = true)
    protected CTUnsignedInt idx;
    @XmlElement(required = true)
    protected CTUnsignedInt order;
    protected CTSerTx tx;
    protected CTShapeProperties spPr;
    protected CTBoolean invertIfNegative;
    protected List<CTDPt> dPt = new ArrayListDml<CTDPt>(this);
    protected CTDLbls dLbls;
    protected List<CTTrendline> trendline = new ArrayListDml<CTTrendline>(this);
    protected List<CTErrBars> errBars = new ArrayListDml<CTErrBars>(this);
    protected CTAxDataSource xVal;
    protected CTNumDataSource yVal;
    protected CTNumDataSource bubbleSize;
    protected CTBoolean bubble3D;
    protected CTExtensionList extLst;
    @XmlTransient
    private Object parent;

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
     * Gets the value of the invertIfNegative property.
     * 
     * @return
     *     possible object is
     *     {@link CTBoolean }
     *     
     */
    public CTBoolean getInvertIfNegative() {
        return invertIfNegative;
    }

    /**
     * Sets the value of the invertIfNegative property.
     * 
     * @param value
     *     allowed object is
     *     {@link CTBoolean }
     *     
     */
    public void setInvertIfNegative(CTBoolean value) {
        this.invertIfNegative = value;
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
            dPt = new ArrayListDml<CTDPt>(this);
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
            trendline = new ArrayListDml<CTTrendline>(this);
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
            errBars = new ArrayListDml<CTErrBars>(this);
        }
        return this.errBars;
    }

    /**
     * Gets the value of the xVal property.
     * 
     * @return
     *     possible object is
     *     {@link CTAxDataSource }
     *     
     */
    public CTAxDataSource getXVal() {
        return xVal;
    }

    /**
     * Sets the value of the xVal property.
     * 
     * @param value
     *     allowed object is
     *     {@link CTAxDataSource }
     *     
     */
    public void setXVal(CTAxDataSource value) {
        this.xVal = value;
    }

    /**
     * Gets the value of the yVal property.
     * 
     * @return
     *     possible object is
     *     {@link CTNumDataSource }
     *     
     */
    public CTNumDataSource getYVal() {
        return yVal;
    }

    /**
     * Sets the value of the yVal property.
     * 
     * @param value
     *     allowed object is
     *     {@link CTNumDataSource }
     *     
     */
    public void setYVal(CTNumDataSource value) {
        this.yVal = value;
    }

    /**
     * Gets the value of the bubbleSize property.
     * 
     * @return
     *     possible object is
     *     {@link CTNumDataSource }
     *     
     */
    public CTNumDataSource getBubbleSize() {
        return bubbleSize;
    }

    /**
     * Sets the value of the bubbleSize property.
     * 
     * @param value
     *     allowed object is
     *     {@link CTNumDataSource }
     *     
     */
    public void setBubbleSize(CTNumDataSource value) {
        this.bubbleSize = value;
    }

    /**
     * Gets the value of the bubble3D property.
     * 
     * @return
     *     possible object is
     *     {@link CTBoolean }
     *     
     */
    public CTBoolean getBubble3D() {
        return bubble3D;
    }

    /**
     * Sets the value of the bubble3D property.
     * 
     * @param value
     *     allowed object is
     *     {@link CTBoolean }
     *     
     */
    public void setBubble3D(CTBoolean value) {
        this.bubble3D = value;
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
