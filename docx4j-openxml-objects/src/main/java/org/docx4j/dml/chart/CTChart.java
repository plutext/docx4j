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
import org.jvnet.jaxb2_commons.ppp.Child;


/**
 * <p>Java class for CT_Chart complex type.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
 * 
 * <pre>
 * &lt;complexType name="CT_Chart"&gt;
 *   &lt;complexContent&gt;
 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType"&gt;
 *       &lt;sequence&gt;
 *         &lt;element name="title" type="{http://schemas.openxmlformats.org/drawingml/2006/chart}CT_Title" minOccurs="0"/&gt;
 *         &lt;element name="autoTitleDeleted" type="{http://schemas.openxmlformats.org/drawingml/2006/chart}CT_Boolean" minOccurs="0"/&gt;
 *         &lt;element name="pivotFmts" type="{http://schemas.openxmlformats.org/drawingml/2006/chart}CT_PivotFmts" minOccurs="0"/&gt;
 *         &lt;element name="view3D" type="{http://schemas.openxmlformats.org/drawingml/2006/chart}CT_View3D" minOccurs="0"/&gt;
 *         &lt;element name="floor" type="{http://schemas.openxmlformats.org/drawingml/2006/chart}CT_Surface" minOccurs="0"/&gt;
 *         &lt;element name="sideWall" type="{http://schemas.openxmlformats.org/drawingml/2006/chart}CT_Surface" minOccurs="0"/&gt;
 *         &lt;element name="backWall" type="{http://schemas.openxmlformats.org/drawingml/2006/chart}CT_Surface" minOccurs="0"/&gt;
 *         &lt;element name="plotArea" type="{http://schemas.openxmlformats.org/drawingml/2006/chart}CT_PlotArea"/&gt;
 *         &lt;element name="legend" type="{http://schemas.openxmlformats.org/drawingml/2006/chart}CT_Legend" minOccurs="0"/&gt;
 *         &lt;element name="plotVisOnly" type="{http://schemas.openxmlformats.org/drawingml/2006/chart}CT_Boolean" minOccurs="0"/&gt;
 *         &lt;element name="dispBlanksAs" type="{http://schemas.openxmlformats.org/drawingml/2006/chart}CT_DispBlanksAs" minOccurs="0"/&gt;
 *         &lt;element name="showDLblsOverMax" type="{http://schemas.openxmlformats.org/drawingml/2006/chart}CT_Boolean" minOccurs="0"/&gt;
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
@XmlType(name = "CT_Chart", propOrder = {
    "title",
    "autoTitleDeleted",
    "pivotFmts",
    "view3D",
    "floor",
    "sideWall",
    "backWall",
    "plotArea",
    "legend",
    "plotVisOnly",
    "dispBlanksAs",
    "showDLblsOverMax",
    "extLst"
})
public class CTChart implements Child
{

    protected CTTitle title;
    protected CTBoolean autoTitleDeleted;
    protected CTPivotFmts pivotFmts;
    protected CTView3D view3D;
    protected CTSurface floor;
    protected CTSurface sideWall;
    protected CTSurface backWall;
    @XmlElement(required = true)
    protected CTPlotArea plotArea;
    protected CTLegend legend;
    protected CTBoolean plotVisOnly;
    protected CTDispBlanksAs dispBlanksAs;
    protected CTBoolean showDLblsOverMax;
    protected CTExtensionList extLst;
    @XmlTransient
    private Object parent;

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
     * Gets the value of the autoTitleDeleted property.
     * 
     * @return
     *     possible object is
     *     {@link CTBoolean }
     *     
     */
    public CTBoolean getAutoTitleDeleted() {
        return autoTitleDeleted;
    }

    /**
     * Sets the value of the autoTitleDeleted property.
     * 
     * @param value
     *     allowed object is
     *     {@link CTBoolean }
     *     
     */
    public void setAutoTitleDeleted(CTBoolean value) {
        this.autoTitleDeleted = value;
    }

    /**
     * Gets the value of the pivotFmts property.
     * 
     * @return
     *     possible object is
     *     {@link CTPivotFmts }
     *     
     */
    public CTPivotFmts getPivotFmts() {
        return pivotFmts;
    }

    /**
     * Sets the value of the pivotFmts property.
     * 
     * @param value
     *     allowed object is
     *     {@link CTPivotFmts }
     *     
     */
    public void setPivotFmts(CTPivotFmts value) {
        this.pivotFmts = value;
    }

    /**
     * Gets the value of the view3D property.
     * 
     * @return
     *     possible object is
     *     {@link CTView3D }
     *     
     */
    public CTView3D getView3D() {
        return view3D;
    }

    /**
     * Sets the value of the view3D property.
     * 
     * @param value
     *     allowed object is
     *     {@link CTView3D }
     *     
     */
    public void setView3D(CTView3D value) {
        this.view3D = value;
    }

    /**
     * Gets the value of the floor property.
     * 
     * @return
     *     possible object is
     *     {@link CTSurface }
     *     
     */
    public CTSurface getFloor() {
        return floor;
    }

    /**
     * Sets the value of the floor property.
     * 
     * @param value
     *     allowed object is
     *     {@link CTSurface }
     *     
     */
    public void setFloor(CTSurface value) {
        this.floor = value;
    }

    /**
     * Gets the value of the sideWall property.
     * 
     * @return
     *     possible object is
     *     {@link CTSurface }
     *     
     */
    public CTSurface getSideWall() {
        return sideWall;
    }

    /**
     * Sets the value of the sideWall property.
     * 
     * @param value
     *     allowed object is
     *     {@link CTSurface }
     *     
     */
    public void setSideWall(CTSurface value) {
        this.sideWall = value;
    }

    /**
     * Gets the value of the backWall property.
     * 
     * @return
     *     possible object is
     *     {@link CTSurface }
     *     
     */
    public CTSurface getBackWall() {
        return backWall;
    }

    /**
     * Sets the value of the backWall property.
     * 
     * @param value
     *     allowed object is
     *     {@link CTSurface }
     *     
     */
    public void setBackWall(CTSurface value) {
        this.backWall = value;
    }

    /**
     * Gets the value of the plotArea property.
     * 
     * @return
     *     possible object is
     *     {@link CTPlotArea }
     *     
     */
    public CTPlotArea getPlotArea() {
        return plotArea;
    }

    /**
     * Sets the value of the plotArea property.
     * 
     * @param value
     *     allowed object is
     *     {@link CTPlotArea }
     *     
     */
    public void setPlotArea(CTPlotArea value) {
        this.plotArea = value;
    }

    /**
     * Gets the value of the legend property.
     * 
     * @return
     *     possible object is
     *     {@link CTLegend }
     *     
     */
    public CTLegend getLegend() {
        return legend;
    }

    /**
     * Sets the value of the legend property.
     * 
     * @param value
     *     allowed object is
     *     {@link CTLegend }
     *     
     */
    public void setLegend(CTLegend value) {
        this.legend = value;
    }

    /**
     * Gets the value of the plotVisOnly property.
     * 
     * @return
     *     possible object is
     *     {@link CTBoolean }
     *     
     */
    public CTBoolean getPlotVisOnly() {
        return plotVisOnly;
    }

    /**
     * Sets the value of the plotVisOnly property.
     * 
     * @param value
     *     allowed object is
     *     {@link CTBoolean }
     *     
     */
    public void setPlotVisOnly(CTBoolean value) {
        this.plotVisOnly = value;
    }

    /**
     * Gets the value of the dispBlanksAs property.
     * 
     * @return
     *     possible object is
     *     {@link CTDispBlanksAs }
     *     
     */
    public CTDispBlanksAs getDispBlanksAs() {
        return dispBlanksAs;
    }

    /**
     * Sets the value of the dispBlanksAs property.
     * 
     * @param value
     *     allowed object is
     *     {@link CTDispBlanksAs }
     *     
     */
    public void setDispBlanksAs(CTDispBlanksAs value) {
        this.dispBlanksAs = value;
    }

    /**
     * Gets the value of the showDLblsOverMax property.
     * 
     * @return
     *     possible object is
     *     {@link CTBoolean }
     *     
     */
    public CTBoolean getShowDLblsOverMax() {
        return showDLblsOverMax;
    }

    /**
     * Sets the value of the showDLblsOverMax property.
     * 
     * @param value
     *     allowed object is
     *     {@link CTBoolean }
     *     
     */
    public void setShowDLblsOverMax(CTBoolean value) {
        this.showDLblsOverMax = value;
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
