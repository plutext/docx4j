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
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlTransient;
import javax.xml.bind.annotation.XmlType;
import org.docx4j.dml.CTColorMapping;
import org.docx4j.dml.CTShapeProperties;
import org.docx4j.dml.CTTextBody;
import org.jvnet.jaxb2_commons.ppp.Child;


/**
 * <p>Java class for CT_ChartSpace complex type.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
 * 
 * <pre>
 * &lt;complexType name="CT_ChartSpace"&gt;
 *   &lt;complexContent&gt;
 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType"&gt;
 *       &lt;sequence&gt;
 *         &lt;element name="date1904" type="{http://schemas.openxmlformats.org/drawingml/2006/chart}CT_Boolean" minOccurs="0"/&gt;
 *         &lt;element name="lang" type="{http://schemas.openxmlformats.org/drawingml/2006/chart}CT_TextLanguageID" minOccurs="0"/&gt;
 *         &lt;element name="roundedCorners" type="{http://schemas.openxmlformats.org/drawingml/2006/chart}CT_Boolean" minOccurs="0"/&gt;
 *         &lt;element name="style" type="{http://schemas.openxmlformats.org/drawingml/2006/chart}CT_Style" minOccurs="0"/&gt;
 *         &lt;element name="clrMapOvr" type="{http://schemas.openxmlformats.org/drawingml/2006/main}CT_ColorMapping" minOccurs="0"/&gt;
 *         &lt;element name="pivotSource" type="{http://schemas.openxmlformats.org/drawingml/2006/chart}CT_PivotSource" minOccurs="0"/&gt;
 *         &lt;element name="protection" type="{http://schemas.openxmlformats.org/drawingml/2006/chart}CT_Protection" minOccurs="0"/&gt;
 *         &lt;element name="chart" type="{http://schemas.openxmlformats.org/drawingml/2006/chart}CT_Chart"/&gt;
 *         &lt;element name="spPr" type="{http://schemas.openxmlformats.org/drawingml/2006/main}CT_ShapeProperties" minOccurs="0"/&gt;
 *         &lt;element name="txPr" type="{http://schemas.openxmlformats.org/drawingml/2006/main}CT_TextBody" minOccurs="0"/&gt;
 *         &lt;element name="externalData" type="{http://schemas.openxmlformats.org/drawingml/2006/chart}CT_ExternalData" minOccurs="0"/&gt;
 *         &lt;element name="printSettings" type="{http://schemas.openxmlformats.org/drawingml/2006/chart}CT_PrintSettings" minOccurs="0"/&gt;
 *         &lt;element name="userShapes" type="{http://schemas.openxmlformats.org/drawingml/2006/chart}CT_RelId" minOccurs="0"/&gt;
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
@XmlType(name = "CT_ChartSpace", propOrder = {
    "date1904",
    "lang",
    "roundedCorners",
    "style",
    "clrMapOvr",
    "pivotSource",
    "protection",
    "chart",
    "spPr",
    "txPr",
    "externalData",
    "printSettings",
    "userShapes",
    "extLst"
})
@XmlRootElement(name = "chartSpace")
public class CTChartSpace implements Child
{
    protected CTBoolean date1904;
    protected CTTextLanguageID lang;
    protected CTBoolean roundedCorners;
    protected CTStyle style;
    protected CTColorMapping clrMapOvr;
    protected CTPivotSource pivotSource;
    protected CTProtection protection;
    @XmlElement(required = true)
    protected CTChart chart;
    protected CTShapeProperties spPr;
    protected CTTextBody txPr;
    protected CTExternalData externalData;
    protected CTPrintSettings printSettings;
    protected CTRelId userShapes;
    protected CTExtensionList extLst;
    @XmlTransient
    private Object parent;

    /**
     * Gets the value of the date1904 property.
     * 
     * @return
     *     possible object is
     *     {@link CTBoolean }
     *     
     */
    public CTBoolean getDate1904() {
        return date1904;
    }

    /**
     * Sets the value of the date1904 property.
     * 
     * @param value
     *     allowed object is
     *     {@link CTBoolean }
     *     
     */
    public void setDate1904(CTBoolean value) {
        this.date1904 = value;
    }

    /**
     * Gets the value of the lang property.
     * 
     * @return
     *     possible object is
     *     {@link CTTextLanguageID }
     *     
     */
    public CTTextLanguageID getLang() {
        return lang;
    }

    /**
     * Sets the value of the lang property.
     * 
     * @param value
     *     allowed object is
     *     {@link CTTextLanguageID }
     *     
     */
    public void setLang(CTTextLanguageID value) {
        this.lang = value;
    }

    /**
     * Gets the value of the roundedCorners property.
     * 
     * @return
     *     possible object is
     *     {@link CTBoolean }
     *     
     */
    public CTBoolean getRoundedCorners() {
        return roundedCorners;
    }

    /**
     * Sets the value of the roundedCorners property.
     * 
     * @param value
     *     allowed object is
     *     {@link CTBoolean }
     *     
     */
    public void setRoundedCorners(CTBoolean value) {
        this.roundedCorners = value;
    }

    /**
     * Gets the value of the style property.
     * 
     * @return
     *     possible object is
     *     {@link CTStyle }
     *     
     */
    public CTStyle getStyle() {
        return style;
    }

    /**
     * Sets the value of the style property.
     * 
     * @param value
     *     allowed object is
     *     {@link CTStyle }
     *     
     */
    public void setStyle(CTStyle value) {
        this.style = value;
    }

    /**
     * Gets the value of the clrMapOvr property.
     * 
     * @return
     *     possible object is
     *     {@link CTColorMapping }
     *     
     */
    public CTColorMapping getClrMapOvr() {
        return clrMapOvr;
    }

    /**
     * Sets the value of the clrMapOvr property.
     * 
     * @param value
     *     allowed object is
     *     {@link CTColorMapping }
     *     
     */
    public void setClrMapOvr(CTColorMapping value) {
        this.clrMapOvr = value;
    }

    /**
     * Gets the value of the pivotSource property.
     * 
     * @return
     *     possible object is
     *     {@link CTPivotSource }
     *     
     */
    public CTPivotSource getPivotSource() {
        return pivotSource;
    }

    /**
     * Sets the value of the pivotSource property.
     * 
     * @param value
     *     allowed object is
     *     {@link CTPivotSource }
     *     
     */
    public void setPivotSource(CTPivotSource value) {
        this.pivotSource = value;
    }

    /**
     * Gets the value of the protection property.
     * 
     * @return
     *     possible object is
     *     {@link CTProtection }
     *     
     */
    public CTProtection getProtection() {
        return protection;
    }

    /**
     * Sets the value of the protection property.
     * 
     * @param value
     *     allowed object is
     *     {@link CTProtection }
     *     
     */
    public void setProtection(CTProtection value) {
        this.protection = value;
    }

    /**
     * Gets the value of the chart property.
     * 
     * @return
     *     possible object is
     *     {@link CTChart }
     *     
     */
    public CTChart getChart() {
        return chart;
    }

    /**
     * Sets the value of the chart property.
     * 
     * @param value
     *     allowed object is
     *     {@link CTChart }
     *     
     */
    public void setChart(CTChart value) {
        this.chart = value;
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
     * Gets the value of the externalData property.
     * 
     * @return
     *     possible object is
     *     {@link CTExternalData }
     *     
     */
    public CTExternalData getExternalData() {
        return externalData;
    }

    /**
     * Sets the value of the externalData property.
     * 
     * @param value
     *     allowed object is
     *     {@link CTExternalData }
     *     
     */
    public void setExternalData(CTExternalData value) {
        this.externalData = value;
    }

    /**
     * Gets the value of the printSettings property.
     * 
     * @return
     *     possible object is
     *     {@link CTPrintSettings }
     *     
     */
    public CTPrintSettings getPrintSettings() {
        return printSettings;
    }

    /**
     * Sets the value of the printSettings property.
     * 
     * @param value
     *     allowed object is
     *     {@link CTPrintSettings }
     *     
     */
    public void setPrintSettings(CTPrintSettings value) {
        this.printSettings = value;
    }

    /**
     * Gets the value of the userShapes property.
     * 
     * @return
     *     possible object is
     *     {@link CTRelId }
     *     
     */
    public CTRelId getUserShapes() {
        return userShapes;
    }

    /**
     * Sets the value of the userShapes property.
     * 
     * @param value
     *     allowed object is
     *     {@link CTRelId }
     *     
     */
    public void setUserShapes(CTRelId value) {
        this.userShapes = value;
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
