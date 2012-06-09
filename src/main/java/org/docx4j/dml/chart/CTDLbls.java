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
import javax.xml.bind.annotation.XmlType;
import org.docx4j.dml.CTShapeProperties;
import org.docx4j.dml.CTTextBody;


/**
 * <p>Java class for CT_DLbls complex type.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
 * 
 * <pre>
 * &lt;complexType name="CT_DLbls">
 *   &lt;complexContent>
 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
 *       &lt;sequence>
 *         &lt;element name="dLbl" type="{http://schemas.openxmlformats.org/drawingml/2006/chart}CT_DLbl" maxOccurs="unbounded" minOccurs="0"/>
 *         &lt;choice>
 *           &lt;element name="delete" type="{http://schemas.openxmlformats.org/drawingml/2006/chart}CT_Boolean"/>
 *           &lt;group ref="{http://schemas.openxmlformats.org/drawingml/2006/chart}Group_DLbls"/>
 *         &lt;/choice>
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
@XmlType(name = "CT_DLbls", propOrder = {
    "dLbl",
    "delete",
    "numFmt",
    "spPr",
    "txPr",
    "dLblPos",
    "showLegendKey",
    "showVal",
    "showCatName",
    "showSerName",
    "showPercent",
    "showBubbleSize",
    "separator",
    "showLeaderLines",
    "leaderLines",
    "extLst"
})
public class CTDLbls {

    protected List<CTDLbl> dLbl;
    protected CTBoolean delete;
    protected CTNumFmt numFmt;
    protected CTShapeProperties spPr;
    protected CTTextBody txPr;
    protected CTDLblPos dLblPos;
    protected CTBoolean showLegendKey;
    protected CTBoolean showVal;
    protected CTBoolean showCatName;
    protected CTBoolean showSerName;
    protected CTBoolean showPercent;
    protected CTBoolean showBubbleSize;
    protected String separator;
    protected CTBoolean showLeaderLines;
    protected CTChartLines leaderLines;
    protected CTExtensionList extLst;

    /**
     * Gets the value of the dLbl property.
     * 
     * <p>
     * This accessor method returns a reference to the live list,
     * not a snapshot. Therefore any modification you make to the
     * returned list will be present inside the JAXB object.
     * This is why there is not a <CODE>set</CODE> method for the dLbl property.
     * 
     * <p>
     * For example, to add a new item, do as follows:
     * <pre>
     *    getDLbl().add(newItem);
     * </pre>
     * 
     * 
     * <p>
     * Objects of the following type(s) are allowed in the list
     * {@link CTDLbl }
     * 
     * 
     */
    public List<CTDLbl> getDLbl() {
        if (dLbl == null) {
            dLbl = new ArrayList<CTDLbl>();
        }
        return this.dLbl;
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
     * Gets the value of the dLblPos property.
     * 
     * @return
     *     possible object is
     *     {@link CTDLblPos }
     *     
     */
    public CTDLblPos getDLblPos() {
        return dLblPos;
    }

    /**
     * Sets the value of the dLblPos property.
     * 
     * @param value
     *     allowed object is
     *     {@link CTDLblPos }
     *     
     */
    public void setDLblPos(CTDLblPos value) {
        this.dLblPos = value;
    }

    /**
     * Gets the value of the showLegendKey property.
     * 
     * @return
     *     possible object is
     *     {@link CTBoolean }
     *     
     */
    public CTBoolean getShowLegendKey() {
        return showLegendKey;
    }

    /**
     * Sets the value of the showLegendKey property.
     * 
     * @param value
     *     allowed object is
     *     {@link CTBoolean }
     *     
     */
    public void setShowLegendKey(CTBoolean value) {
        this.showLegendKey = value;
    }

    /**
     * Gets the value of the showVal property.
     * 
     * @return
     *     possible object is
     *     {@link CTBoolean }
     *     
     */
    public CTBoolean getShowVal() {
        return showVal;
    }

    /**
     * Sets the value of the showVal property.
     * 
     * @param value
     *     allowed object is
     *     {@link CTBoolean }
     *     
     */
    public void setShowVal(CTBoolean value) {
        this.showVal = value;
    }

    /**
     * Gets the value of the showCatName property.
     * 
     * @return
     *     possible object is
     *     {@link CTBoolean }
     *     
     */
    public CTBoolean getShowCatName() {
        return showCatName;
    }

    /**
     * Sets the value of the showCatName property.
     * 
     * @param value
     *     allowed object is
     *     {@link CTBoolean }
     *     
     */
    public void setShowCatName(CTBoolean value) {
        this.showCatName = value;
    }

    /**
     * Gets the value of the showSerName property.
     * 
     * @return
     *     possible object is
     *     {@link CTBoolean }
     *     
     */
    public CTBoolean getShowSerName() {
        return showSerName;
    }

    /**
     * Sets the value of the showSerName property.
     * 
     * @param value
     *     allowed object is
     *     {@link CTBoolean }
     *     
     */
    public void setShowSerName(CTBoolean value) {
        this.showSerName = value;
    }

    /**
     * Gets the value of the showPercent property.
     * 
     * @return
     *     possible object is
     *     {@link CTBoolean }
     *     
     */
    public CTBoolean getShowPercent() {
        return showPercent;
    }

    /**
     * Sets the value of the showPercent property.
     * 
     * @param value
     *     allowed object is
     *     {@link CTBoolean }
     *     
     */
    public void setShowPercent(CTBoolean value) {
        this.showPercent = value;
    }

    /**
     * Gets the value of the showBubbleSize property.
     * 
     * @return
     *     possible object is
     *     {@link CTBoolean }
     *     
     */
    public CTBoolean getShowBubbleSize() {
        return showBubbleSize;
    }

    /**
     * Sets the value of the showBubbleSize property.
     * 
     * @param value
     *     allowed object is
     *     {@link CTBoolean }
     *     
     */
    public void setShowBubbleSize(CTBoolean value) {
        this.showBubbleSize = value;
    }

    /**
     * Gets the value of the separator property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getSeparator() {
        return separator;
    }

    /**
     * Sets the value of the separator property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setSeparator(String value) {
        this.separator = value;
    }

    /**
     * Gets the value of the showLeaderLines property.
     * 
     * @return
     *     possible object is
     *     {@link CTBoolean }
     *     
     */
    public CTBoolean getShowLeaderLines() {
        return showLeaderLines;
    }

    /**
     * Sets the value of the showLeaderLines property.
     * 
     * @param value
     *     allowed object is
     *     {@link CTBoolean }
     *     
     */
    public void setShowLeaderLines(CTBoolean value) {
        this.showLeaderLines = value;
    }

    /**
     * Gets the value of the leaderLines property.
     * 
     * @return
     *     possible object is
     *     {@link CTChartLines }
     *     
     */
    public CTChartLines getLeaderLines() {
        return leaderLines;
    }

    /**
     * Sets the value of the leaderLines property.
     * 
     * @param value
     *     allowed object is
     *     {@link CTChartLines }
     *     
     */
    public void setLeaderLines(CTChartLines value) {
        this.leaderLines = value;
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
