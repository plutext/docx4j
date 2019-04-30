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
 * <p>Java class for CT_View3D complex type.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
 * 
 * <pre>
 * &lt;complexType name="CT_View3D"&gt;
 *   &lt;complexContent&gt;
 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType"&gt;
 *       &lt;sequence&gt;
 *         &lt;element name="rotX" type="{http://schemas.openxmlformats.org/drawingml/2006/chart}CT_RotX" minOccurs="0"/&gt;
 *         &lt;element name="hPercent" type="{http://schemas.openxmlformats.org/drawingml/2006/chart}CT_HPercent" minOccurs="0"/&gt;
 *         &lt;element name="rotY" type="{http://schemas.openxmlformats.org/drawingml/2006/chart}CT_RotY" minOccurs="0"/&gt;
 *         &lt;element name="depthPercent" type="{http://schemas.openxmlformats.org/drawingml/2006/chart}CT_DepthPercent" minOccurs="0"/&gt;
 *         &lt;element name="rAngAx" type="{http://schemas.openxmlformats.org/drawingml/2006/chart}CT_Boolean" minOccurs="0"/&gt;
 *         &lt;element name="perspective" type="{http://schemas.openxmlformats.org/drawingml/2006/chart}CT_Perspective" minOccurs="0"/&gt;
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
@XmlType(name = "CT_View3D", propOrder = {
    "rotX",
    "hPercent",
    "rotY",
    "depthPercent",
    "rAngAx",
    "perspective",
    "extLst"
})
public class CTView3D implements Child
{

    protected CTRotX rotX;
    protected CTHPercent hPercent;
    protected CTRotY rotY;
    protected CTDepthPercent depthPercent;
    protected CTBoolean rAngAx;
    protected CTPerspective perspective;
    protected CTExtensionList extLst;
    @XmlTransient
    private Object parent;

    /**
     * Gets the value of the rotX property.
     * 
     * @return
     *     possible object is
     *     {@link CTRotX }
     *     
     */
    public CTRotX getRotX() {
        return rotX;
    }

    /**
     * Sets the value of the rotX property.
     * 
     * @param value
     *     allowed object is
     *     {@link CTRotX }
     *     
     */
    public void setRotX(CTRotX value) {
        this.rotX = value;
    }

    /**
     * Gets the value of the hPercent property.
     * 
     * @return
     *     possible object is
     *     {@link CTHPercent }
     *     
     */
    public CTHPercent getHPercent() {
        return hPercent;
    }

    /**
     * Sets the value of the hPercent property.
     * 
     * @param value
     *     allowed object is
     *     {@link CTHPercent }
     *     
     */
    public void setHPercent(CTHPercent value) {
        this.hPercent = value;
    }

    /**
     * Gets the value of the rotY property.
     * 
     * @return
     *     possible object is
     *     {@link CTRotY }
     *     
     */
    public CTRotY getRotY() {
        return rotY;
    }

    /**
     * Sets the value of the rotY property.
     * 
     * @param value
     *     allowed object is
     *     {@link CTRotY }
     *     
     */
    public void setRotY(CTRotY value) {
        this.rotY = value;
    }

    /**
     * Gets the value of the depthPercent property.
     * 
     * @return
     *     possible object is
     *     {@link CTDepthPercent }
     *     
     */
    public CTDepthPercent getDepthPercent() {
        return depthPercent;
    }

    /**
     * Sets the value of the depthPercent property.
     * 
     * @param value
     *     allowed object is
     *     {@link CTDepthPercent }
     *     
     */
    public void setDepthPercent(CTDepthPercent value) {
        this.depthPercent = value;
    }

    /**
     * Gets the value of the rAngAx property.
     * 
     * @return
     *     possible object is
     *     {@link CTBoolean }
     *     
     */
    public CTBoolean getRAngAx() {
        return rAngAx;
    }

    /**
     * Sets the value of the rAngAx property.
     * 
     * @param value
     *     allowed object is
     *     {@link CTBoolean }
     *     
     */
    public void setRAngAx(CTBoolean value) {
        this.rAngAx = value;
    }

    /**
     * Gets the value of the perspective property.
     * 
     * @return
     *     possible object is
     *     {@link CTPerspective }
     *     
     */
    public CTPerspective getPerspective() {
        return perspective;
    }

    /**
     * Sets the value of the perspective property.
     * 
     * @param value
     *     allowed object is
     *     {@link CTPerspective }
     *     
     */
    public void setPerspective(CTPerspective value) {
        this.perspective = value;
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
