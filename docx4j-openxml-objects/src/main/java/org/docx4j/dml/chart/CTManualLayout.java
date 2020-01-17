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
import javax.xml.bind.annotation.XmlTransient;
import javax.xml.bind.annotation.XmlType;
import org.jvnet.jaxb2_commons.ppp.Child;


/**
 * <p>Java class for CT_ManualLayout complex type.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
 * 
 * <pre>
 * &lt;complexType name="CT_ManualLayout"&gt;
 *   &lt;complexContent&gt;
 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType"&gt;
 *       &lt;sequence&gt;
 *         &lt;element name="layoutTarget" type="{http://schemas.openxmlformats.org/drawingml/2006/chart}CT_LayoutTarget" minOccurs="0"/&gt;
 *         &lt;element name="xMode" type="{http://schemas.openxmlformats.org/drawingml/2006/chart}CT_LayoutMode" minOccurs="0"/&gt;
 *         &lt;element name="yMode" type="{http://schemas.openxmlformats.org/drawingml/2006/chart}CT_LayoutMode" minOccurs="0"/&gt;
 *         &lt;element name="wMode" type="{http://schemas.openxmlformats.org/drawingml/2006/chart}CT_LayoutMode" minOccurs="0"/&gt;
 *         &lt;element name="hMode" type="{http://schemas.openxmlformats.org/drawingml/2006/chart}CT_LayoutMode" minOccurs="0"/&gt;
 *         &lt;element name="x" type="{http://schemas.openxmlformats.org/drawingml/2006/chart}CT_Double" minOccurs="0"/&gt;
 *         &lt;element name="y" type="{http://schemas.openxmlformats.org/drawingml/2006/chart}CT_Double" minOccurs="0"/&gt;
 *         &lt;element name="w" type="{http://schemas.openxmlformats.org/drawingml/2006/chart}CT_Double" minOccurs="0"/&gt;
 *         &lt;element name="h" type="{http://schemas.openxmlformats.org/drawingml/2006/chart}CT_Double" minOccurs="0"/&gt;
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
@XmlType(name = "CT_ManualLayout", propOrder = {
    "layoutTarget",
    "xMode",
    "yMode",
    "wMode",
    "hMode",
    "x",
    "y",
    "w",
    "h",
    "extLst"
})
public class CTManualLayout implements Child
{

    protected CTLayoutTarget layoutTarget;
    protected CTLayoutMode xMode;
    protected CTLayoutMode yMode;
    protected CTLayoutMode wMode;
    protected CTLayoutMode hMode;
    protected CTDouble x;
    protected CTDouble y;
    protected CTDouble w;
    protected CTDouble h;
    protected CTExtensionList extLst;
    @XmlTransient
    private Object parent;

    /**
     * Gets the value of the layoutTarget property.
     * 
     * @return
     *     possible object is
     *     {@link CTLayoutTarget }
     *     
     */
    public CTLayoutTarget getLayoutTarget() {
        return layoutTarget;
    }

    /**
     * Sets the value of the layoutTarget property.
     * 
     * @param value
     *     allowed object is
     *     {@link CTLayoutTarget }
     *     
     */
    public void setLayoutTarget(CTLayoutTarget value) {
        this.layoutTarget = value;
    }

    /**
     * Gets the value of the xMode property.
     * 
     * @return
     *     possible object is
     *     {@link CTLayoutMode }
     *     
     */
    public CTLayoutMode getXMode() {
        return xMode;
    }

    /**
     * Sets the value of the xMode property.
     * 
     * @param value
     *     allowed object is
     *     {@link CTLayoutMode }
     *     
     */
    public void setXMode(CTLayoutMode value) {
        this.xMode = value;
    }

    /**
     * Gets the value of the yMode property.
     * 
     * @return
     *     possible object is
     *     {@link CTLayoutMode }
     *     
     */
    public CTLayoutMode getYMode() {
        return yMode;
    }

    /**
     * Sets the value of the yMode property.
     * 
     * @param value
     *     allowed object is
     *     {@link CTLayoutMode }
     *     
     */
    public void setYMode(CTLayoutMode value) {
        this.yMode = value;
    }

    /**
     * Gets the value of the wMode property.
     * 
     * @return
     *     possible object is
     *     {@link CTLayoutMode }
     *     
     */
    public CTLayoutMode getWMode() {
        return wMode;
    }

    /**
     * Sets the value of the wMode property.
     * 
     * @param value
     *     allowed object is
     *     {@link CTLayoutMode }
     *     
     */
    public void setWMode(CTLayoutMode value) {
        this.wMode = value;
    }

    /**
     * Gets the value of the hMode property.
     * 
     * @return
     *     possible object is
     *     {@link CTLayoutMode }
     *     
     */
    public CTLayoutMode getHMode() {
        return hMode;
    }

    /**
     * Sets the value of the hMode property.
     * 
     * @param value
     *     allowed object is
     *     {@link CTLayoutMode }
     *     
     */
    public void setHMode(CTLayoutMode value) {
        this.hMode = value;
    }

    /**
     * Gets the value of the x property.
     * 
     * @return
     *     possible object is
     *     {@link CTDouble }
     *     
     */
    public CTDouble getX() {
        return x;
    }

    /**
     * Sets the value of the x property.
     * 
     * @param value
     *     allowed object is
     *     {@link CTDouble }
     *     
     */
    public void setX(CTDouble value) {
        this.x = value;
    }

    /**
     * Gets the value of the y property.
     * 
     * @return
     *     possible object is
     *     {@link CTDouble }
     *     
     */
    public CTDouble getY() {
        return y;
    }

    /**
     * Sets the value of the y property.
     * 
     * @param value
     *     allowed object is
     *     {@link CTDouble }
     *     
     */
    public void setY(CTDouble value) {
        this.y = value;
    }

    /**
     * Gets the value of the w property.
     * 
     * @return
     *     possible object is
     *     {@link CTDouble }
     *     
     */
    public CTDouble getW() {
        return w;
    }

    /**
     * Sets the value of the w property.
     * 
     * @param value
     *     allowed object is
     *     {@link CTDouble }
     *     
     */
    public void setW(CTDouble value) {
        this.w = value;
    }

    /**
     * Gets the value of the h property.
     * 
     * @return
     *     possible object is
     *     {@link CTDouble }
     *     
     */
    public CTDouble getH() {
        return h;
    }

    /**
     * Sets the value of the h property.
     * 
     * @param value
     *     allowed object is
     *     {@link CTDouble }
     *     
     */
    public void setH(CTDouble value) {
        this.h = value;
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
