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

import javax.xml.bind.Unmarshaller;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlTransient;
import javax.xml.bind.annotation.XmlType;
import org.docx4j.dml.CTShapeProperties;
import org.docx4j.dml.CTTextBody;
import org.jvnet.jaxb2_commons.ppp.Child;


/**
 * <p>Java class for CT_CatAx complex type.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
 * 
 * <pre>
 * &lt;complexType name="CT_CatAx"&gt;
 *   &lt;complexContent&gt;
 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType"&gt;
 *       &lt;sequence&gt;
 *         &lt;group ref="{http://schemas.openxmlformats.org/drawingml/2006/chart}EG_AxShared"/&gt;
 *         &lt;element name="auto" type="{http://schemas.openxmlformats.org/drawingml/2006/chart}CT_Boolean" minOccurs="0"/&gt;
 *         &lt;element name="lblAlgn" type="{http://schemas.openxmlformats.org/drawingml/2006/chart}CT_LblAlgn" minOccurs="0"/&gt;
 *         &lt;element name="lblOffset" type="{http://schemas.openxmlformats.org/drawingml/2006/chart}CT_LblOffset" minOccurs="0"/&gt;
 *         &lt;element name="tickLblSkip" type="{http://schemas.openxmlformats.org/drawingml/2006/chart}CT_Skip" minOccurs="0"/&gt;
 *         &lt;element name="tickMarkSkip" type="{http://schemas.openxmlformats.org/drawingml/2006/chart}CT_Skip" minOccurs="0"/&gt;
 *         &lt;element name="noMultiLvlLbl" type="{http://schemas.openxmlformats.org/drawingml/2006/chart}CT_Boolean" minOccurs="0"/&gt;
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
@XmlType(name = "CT_CatAx", propOrder = {
    "axId",
    "scaling",
    "delete",
    "axPos",
    "majorGridlines",
    "minorGridlines",
    "title",
    "numFmt",
    "majorTickMark",
    "minorTickMark",
    "tickLblPos",
    "spPr",
    "txPr",
    "crossAx",
    "crosses",
    "crossesAt",
    "auto",
    "lblAlgn",
    "lblOffset",
    "tickLblSkip",
    "tickMarkSkip",
    "noMultiLvlLbl",
    "extLst"
})
public class CTCatAx implements Child
{

    @XmlElement(required = true)
    protected CTUnsignedInt axId;
    @XmlElement(required = true)
    protected CTScaling scaling;
    protected CTBoolean delete;
    @XmlElement(required = true)
    protected CTAxPos axPos;
    protected CTChartLines majorGridlines;
    protected CTChartLines minorGridlines;
    protected CTTitle title;
    protected CTNumFmt numFmt;
    protected CTTickMark majorTickMark;
    protected CTTickMark minorTickMark;
    protected CTTickLblPos tickLblPos;
    protected CTShapeProperties spPr;
    protected CTTextBody txPr;
    @XmlElement(required = true)
    protected CTUnsignedInt crossAx;
    protected CTCrosses crosses;
    protected CTDouble crossesAt;
    protected CTBoolean auto;
    protected CTLblAlgn lblAlgn;
    protected CTLblOffset lblOffset;
    protected CTSkip tickLblSkip;
    protected CTSkip tickMarkSkip;
    protected CTBoolean noMultiLvlLbl;
    protected CTExtensionList extLst;
    @XmlTransient
    private Object parent;

    /**
     * Gets the value of the axId property.
     * 
     * @return
     *     possible object is
     *     {@link CTUnsignedInt }
     *     
     */
    public CTUnsignedInt getAxId() {
        return axId;
    }

    /**
     * Sets the value of the axId property.
     * 
     * @param value
     *     allowed object is
     *     {@link CTUnsignedInt }
     *     
     */
    public void setAxId(CTUnsignedInt value) {
        this.axId = value;
    }

    /**
     * Gets the value of the scaling property.
     * 
     * @return
     *     possible object is
     *     {@link CTScaling }
     *     
     */
    public CTScaling getScaling() {
        return scaling;
    }

    /**
     * Sets the value of the scaling property.
     * 
     * @param value
     *     allowed object is
     *     {@link CTScaling }
     *     
     */
    public void setScaling(CTScaling value) {
        this.scaling = value;
    }

    /**
     * Gets the value of the delete property.
     * 
     * @return
     *     possible object is
     *     {@link CTBoolean }
     *     
     */
    public CTBoolean getDelete() {
        return delete;
    }

    /**
     * Sets the value of the delete property.
     * 
     * @param value
     *     allowed object is
     *     {@link CTBoolean }
     *     
     */
    public void setDelete(CTBoolean value) {
        this.delete = value;
    }

    /**
     * Gets the value of the axPos property.
     * 
     * @return
     *     possible object is
     *     {@link CTAxPos }
     *     
     */
    public CTAxPos getAxPos() {
        return axPos;
    }

    /**
     * Sets the value of the axPos property.
     * 
     * @param value
     *     allowed object is
     *     {@link CTAxPos }
     *     
     */
    public void setAxPos(CTAxPos value) {
        this.axPos = value;
    }

    /**
     * Gets the value of the majorGridlines property.
     * 
     * @return
     *     possible object is
     *     {@link CTChartLines }
     *     
     */
    public CTChartLines getMajorGridlines() {
        return majorGridlines;
    }

    /**
     * Sets the value of the majorGridlines property.
     * 
     * @param value
     *     allowed object is
     *     {@link CTChartLines }
     *     
     */
    public void setMajorGridlines(CTChartLines value) {
        this.majorGridlines = value;
    }

    /**
     * Gets the value of the minorGridlines property.
     * 
     * @return
     *     possible object is
     *     {@link CTChartLines }
     *     
     */
    public CTChartLines getMinorGridlines() {
        return minorGridlines;
    }

    /**
     * Sets the value of the minorGridlines property.
     * 
     * @param value
     *     allowed object is
     *     {@link CTChartLines }
     *     
     */
    public void setMinorGridlines(CTChartLines value) {
        this.minorGridlines = value;
    }

    /**
     * Gets the value of the title property.
     * 
     * @return
     *     possible object is
     *     {@link CTTitle }
     *     
     */
    public CTTitle getTitle() {
        return title;
    }

    /**
     * Sets the value of the title property.
     * 
     * @param value
     *     allowed object is
     *     {@link CTTitle }
     *     
     */
    public void setTitle(CTTitle value) {
        this.title = value;
    }

    /**
     * Gets the value of the numFmt property.
     * 
     * @return
     *     possible object is
     *     {@link CTNumFmt }
     *     
     */
    public CTNumFmt getNumFmt() {
        return numFmt;
    }

    /**
     * Sets the value of the numFmt property.
     * 
     * @param value
     *     allowed object is
     *     {@link CTNumFmt }
     *     
     */
    public void setNumFmt(CTNumFmt value) {
        this.numFmt = value;
    }

    /**
     * Gets the value of the majorTickMark property.
     * 
     * @return
     *     possible object is
     *     {@link CTTickMark }
     *     
     */
    public CTTickMark getMajorTickMark() {
        return majorTickMark;
    }

    /**
     * Sets the value of the majorTickMark property.
     * 
     * @param value
     *     allowed object is
     *     {@link CTTickMark }
     *     
     */
    public void setMajorTickMark(CTTickMark value) {
        this.majorTickMark = value;
    }

    /**
     * Gets the value of the minorTickMark property.
     * 
     * @return
     *     possible object is
     *     {@link CTTickMark }
     *     
     */
    public CTTickMark getMinorTickMark() {
        return minorTickMark;
    }

    /**
     * Sets the value of the minorTickMark property.
     * 
     * @param value
     *     allowed object is
     *     {@link CTTickMark }
     *     
     */
    public void setMinorTickMark(CTTickMark value) {
        this.minorTickMark = value;
    }

    /**
     * Gets the value of the tickLblPos property.
     * 
     * @return
     *     possible object is
     *     {@link CTTickLblPos }
     *     
     */
    public CTTickLblPos getTickLblPos() {
        return tickLblPos;
    }

    /**
     * Sets the value of the tickLblPos property.
     * 
     * @param value
     *     allowed object is
     *     {@link CTTickLblPos }
     *     
     */
    public void setTickLblPos(CTTickLblPos value) {
        this.tickLblPos = value;
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
     * Gets the value of the txPr property.
     * 
     * @return
     *     possible object is
     *     {@link CTTextBody }
     *     
     */
    public CTTextBody getTxPr() {
        return txPr;
    }

    /**
     * Sets the value of the txPr property.
     * 
     * @param value
     *     allowed object is
     *     {@link CTTextBody }
     *     
     */
    public void setTxPr(CTTextBody value) {
        this.txPr = value;
    }

    /**
     * Gets the value of the crossAx property.
     * 
     * @return
     *     possible object is
     *     {@link CTUnsignedInt }
     *     
     */
    public CTUnsignedInt getCrossAx() {
        return crossAx;
    }

    /**
     * Sets the value of the crossAx property.
     * 
     * @param value
     *     allowed object is
     *     {@link CTUnsignedInt }
     *     
     */
    public void setCrossAx(CTUnsignedInt value) {
        this.crossAx = value;
    }

    /**
     * Gets the value of the crosses property.
     * 
     * @return
     *     possible object is
     *     {@link CTCrosses }
     *     
     */
    public CTCrosses getCrosses() {
        return crosses;
    }

    /**
     * Sets the value of the crosses property.
     * 
     * @param value
     *     allowed object is
     *     {@link CTCrosses }
     *     
     */
    public void setCrosses(CTCrosses value) {
        this.crosses = value;
    }

    /**
     * Gets the value of the crossesAt property.
     * 
     * @return
     *     possible object is
     *     {@link CTDouble }
     *     
     */
    public CTDouble getCrossesAt() {
        return crossesAt;
    }

    /**
     * Sets the value of the crossesAt property.
     * 
     * @param value
     *     allowed object is
     *     {@link CTDouble }
     *     
     */
    public void setCrossesAt(CTDouble value) {
        this.crossesAt = value;
    }

    /**
     * Gets the value of the auto property.
     * 
     * @return
     *     possible object is
     *     {@link CTBoolean }
     *     
     */
    public CTBoolean getAuto() {
        return auto;
    }

    /**
     * Sets the value of the auto property.
     * 
     * @param value
     *     allowed object is
     *     {@link CTBoolean }
     *     
     */
    public void setAuto(CTBoolean value) {
        this.auto = value;
    }

    /**
     * Gets the value of the lblAlgn property.
     * 
     * @return
     *     possible object is
     *     {@link CTLblAlgn }
     *     
     */
    public CTLblAlgn getLblAlgn() {
        return lblAlgn;
    }

    /**
     * Sets the value of the lblAlgn property.
     * 
     * @param value
     *     allowed object is
     *     {@link CTLblAlgn }
     *     
     */
    public void setLblAlgn(CTLblAlgn value) {
        this.lblAlgn = value;
    }

    /**
     * Gets the value of the lblOffset property.
     * 
     * @return
     *     possible object is
     *     {@link CTLblOffset }
     *     
     */
    public CTLblOffset getLblOffset() {
        return lblOffset;
    }

    /**
     * Sets the value of the lblOffset property.
     * 
     * @param value
     *     allowed object is
     *     {@link CTLblOffset }
     *     
     */
    public void setLblOffset(CTLblOffset value) {
        this.lblOffset = value;
    }

    /**
     * Gets the value of the tickLblSkip property.
     * 
     * @return
     *     possible object is
     *     {@link CTSkip }
     *     
     */
    public CTSkip getTickLblSkip() {
        return tickLblSkip;
    }

    /**
     * Sets the value of the tickLblSkip property.
     * 
     * @param value
     *     allowed object is
     *     {@link CTSkip }
     *     
     */
    public void setTickLblSkip(CTSkip value) {
        this.tickLblSkip = value;
    }

    /**
     * Gets the value of the tickMarkSkip property.
     * 
     * @return
     *     possible object is
     *     {@link CTSkip }
     *     
     */
    public CTSkip getTickMarkSkip() {
        return tickMarkSkip;
    }

    /**
     * Sets the value of the tickMarkSkip property.
     * 
     * @param value
     *     allowed object is
     *     {@link CTSkip }
     *     
     */
    public void setTickMarkSkip(CTSkip value) {
        this.tickMarkSkip = value;
    }

    /**
     * Gets the value of the noMultiLvlLbl property.
     * 
     * @return
     *     possible object is
     *     {@link CTBoolean }
     *     
     */
    public CTBoolean getNoMultiLvlLbl() {
        return noMultiLvlLbl;
    }

    /**
     * Sets the value of the noMultiLvlLbl property.
     * 
     * @param value
     *     allowed object is
     *     {@link CTBoolean }
     *     
     */
    public void setNoMultiLvlLbl(CTBoolean value) {
        this.noMultiLvlLbl = value;
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
