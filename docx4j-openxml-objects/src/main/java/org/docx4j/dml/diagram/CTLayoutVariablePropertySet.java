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


package org.docx4j.dml.diagram;

import javax.xml.bind.Unmarshaller;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlTransient;
import javax.xml.bind.annotation.XmlType;
import org.jvnet.jaxb2_commons.ppp.Child;


/**
 * <p>Java class for CT_LayoutVariablePropertySet complex type.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
 * 
 * <pre>
 * &lt;complexType name="CT_LayoutVariablePropertySet"&gt;
 *   &lt;complexContent&gt;
 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType"&gt;
 *       &lt;sequence&gt;
 *         &lt;element name="orgChart" type="{http://schemas.openxmlformats.org/drawingml/2006/diagram}CT_OrgChart" minOccurs="0"/&gt;
 *         &lt;element name="chMax" type="{http://schemas.openxmlformats.org/drawingml/2006/diagram}CT_ChildMax" minOccurs="0"/&gt;
 *         &lt;element name="chPref" type="{http://schemas.openxmlformats.org/drawingml/2006/diagram}CT_ChildPref" minOccurs="0"/&gt;
 *         &lt;element name="bulletEnabled" type="{http://schemas.openxmlformats.org/drawingml/2006/diagram}CT_BulletEnabled" minOccurs="0"/&gt;
 *         &lt;element name="dir" type="{http://schemas.openxmlformats.org/drawingml/2006/diagram}CT_Direction" minOccurs="0"/&gt;
 *         &lt;element name="hierBranch" type="{http://schemas.openxmlformats.org/drawingml/2006/diagram}CT_HierBranchStyle" minOccurs="0"/&gt;
 *         &lt;element name="animOne" type="{http://schemas.openxmlformats.org/drawingml/2006/diagram}CT_AnimOne" minOccurs="0"/&gt;
 *         &lt;element name="animLvl" type="{http://schemas.openxmlformats.org/drawingml/2006/diagram}CT_AnimLvl" minOccurs="0"/&gt;
 *         &lt;element name="resizeHandles" type="{http://schemas.openxmlformats.org/drawingml/2006/diagram}CT_ResizeHandles" minOccurs="0"/&gt;
 *       &lt;/sequence&gt;
 *     &lt;/restriction&gt;
 *   &lt;/complexContent&gt;
 * &lt;/complexType&gt;
 * </pre>
 * 
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "CT_LayoutVariablePropertySet", propOrder = {
    "orgChart",
    "chMax",
    "chPref",
    "bulletEnabled",
    "dir",
    "hierBranch",
    "animOne",
    "animLvl",
    "resizeHandles"
})
public class CTLayoutVariablePropertySet implements Child
{

    protected CTOrgChart orgChart;
    protected CTChildMax chMax;
    protected CTChildPref chPref;
    protected CTBulletEnabled bulletEnabled;
    protected CTDirection dir;
    protected CTHierBranchStyle hierBranch;
    protected CTAnimOne animOne;
    protected CTAnimLvl animLvl;
    protected CTResizeHandles resizeHandles;
    @XmlTransient
    private Object parent;

    /**
     * Gets the value of the orgChart property.
     * 
     * @return
     *     possible object is
     *     {@link CTOrgChart }
     *     
     */
    public CTOrgChart getOrgChart() {
        return orgChart;
    }

    /**
     * Sets the value of the orgChart property.
     * 
     * @param value
     *     allowed object is
     *     {@link CTOrgChart }
     *     
     */
    public void setOrgChart(CTOrgChart value) {
        this.orgChart = value;
    }

    /**
     * Gets the value of the chMax property.
     * 
     * @return
     *     possible object is
     *     {@link CTChildMax }
     *     
     */
    public CTChildMax getChMax() {
        return chMax;
    }

    /**
     * Sets the value of the chMax property.
     * 
     * @param value
     *     allowed object is
     *     {@link CTChildMax }
     *     
     */
    public void setChMax(CTChildMax value) {
        this.chMax = value;
    }

    /**
     * Gets the value of the chPref property.
     * 
     * @return
     *     possible object is
     *     {@link CTChildPref }
     *     
     */
    public CTChildPref getChPref() {
        return chPref;
    }

    /**
     * Sets the value of the chPref property.
     * 
     * @param value
     *     allowed object is
     *     {@link CTChildPref }
     *     
     */
    public void setChPref(CTChildPref value) {
        this.chPref = value;
    }

    /**
     * Gets the value of the bulletEnabled property.
     * 
     * @return
     *     possible object is
     *     {@link CTBulletEnabled }
     *     
     */
    public CTBulletEnabled getBulletEnabled() {
        return bulletEnabled;
    }

    /**
     * Sets the value of the bulletEnabled property.
     * 
     * @param value
     *     allowed object is
     *     {@link CTBulletEnabled }
     *     
     */
    public void setBulletEnabled(CTBulletEnabled value) {
        this.bulletEnabled = value;
    }

    /**
     * Gets the value of the dir property.
     * 
     * @return
     *     possible object is
     *     {@link CTDirection }
     *     
     */
    public CTDirection getDir() {
        return dir;
    }

    /**
     * Sets the value of the dir property.
     * 
     * @param value
     *     allowed object is
     *     {@link CTDirection }
     *     
     */
    public void setDir(CTDirection value) {
        this.dir = value;
    }

    /**
     * Gets the value of the hierBranch property.
     * 
     * @return
     *     possible object is
     *     {@link CTHierBranchStyle }
     *     
     */
    public CTHierBranchStyle getHierBranch() {
        return hierBranch;
    }

    /**
     * Sets the value of the hierBranch property.
     * 
     * @param value
     *     allowed object is
     *     {@link CTHierBranchStyle }
     *     
     */
    public void setHierBranch(CTHierBranchStyle value) {
        this.hierBranch = value;
    }

    /**
     * Gets the value of the animOne property.
     * 
     * @return
     *     possible object is
     *     {@link CTAnimOne }
     *     
     */
    public CTAnimOne getAnimOne() {
        return animOne;
    }

    /**
     * Sets the value of the animOne property.
     * 
     * @param value
     *     allowed object is
     *     {@link CTAnimOne }
     *     
     */
    public void setAnimOne(CTAnimOne value) {
        this.animOne = value;
    }

    /**
     * Gets the value of the animLvl property.
     * 
     * @return
     *     possible object is
     *     {@link CTAnimLvl }
     *     
     */
    public CTAnimLvl getAnimLvl() {
        return animLvl;
    }

    /**
     * Sets the value of the animLvl property.
     * 
     * @param value
     *     allowed object is
     *     {@link CTAnimLvl }
     *     
     */
    public void setAnimLvl(CTAnimLvl value) {
        this.animLvl = value;
    }

    /**
     * Gets the value of the resizeHandles property.
     * 
     * @return
     *     possible object is
     *     {@link CTResizeHandles }
     *     
     */
    public CTResizeHandles getResizeHandles() {
        return resizeHandles;
    }

    /**
     * Sets the value of the resizeHandles property.
     * 
     * @param value
     *     allowed object is
     *     {@link CTResizeHandles }
     *     
     */
    public void setResizeHandles(CTResizeHandles value) {
        this.resizeHandles = value;
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
