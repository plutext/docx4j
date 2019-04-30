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
 * <p>Java class for CT_DLbl complex type.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
 * 
 * <pre>
 * &lt;complexType name="CT_DLbl"&gt;
 *   &lt;complexContent&gt;
 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType"&gt;
 *       &lt;sequence&gt;
 *         &lt;element name="idx" type="{http://schemas.openxmlformats.org/drawingml/2006/chart}CT_UnsignedInt"/&gt;
 *         &lt;choice&gt;
 *           &lt;element name="delete" type="{http://schemas.openxmlformats.org/drawingml/2006/chart}CT_Boolean"/&gt;
 *           &lt;group ref="{http://schemas.openxmlformats.org/drawingml/2006/chart}Group_DLbl"/&gt;
 *         &lt;/choice&gt;
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
@XmlType(name = "CT_DLbl", propOrder = {
    "idx",
    "delete",
    "layout",
    "tx",
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
    "extLst"
})
public class CTDLbl implements Child
{

    @XmlElement(required = true)
    protected CTUnsignedInt idx;
    protected CTBoolean delete;
    protected CTLayout layout;
    protected CTTx tx;
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
     * Gets the value of the layout property.
     * 
     * @return
     *     possible object is
     *     {@link CTLayout }
     *     
     */
    public CTLayout getLayout() {
        return layout;
    }

    /**
     * Sets the value of the layout property.
     * 
     * @param value
     *     allowed object is
     *     {@link CTLayout }
     *     
     */
    public void setLayout(CTLayout value) {
        this.layout = value;
    }

    /**
     * Gets the value of the tx property.
     * 
     * @return
     *     possible object is
     *     {@link CTTx }
     *     
     */
    public CTTx getTx() {
        return tx;
    }

    /**
     * Sets the value of the tx property.
     * 
     * @param value
     *     allowed object is
     *     {@link CTTx }
     *     
     */
    public void setTx(CTTx value) {
        this.tx = value;
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
