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


package org.docx4j.dml;

import javax.xml.bind.Unmarshaller;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlTransient;
import javax.xml.bind.annotation.XmlType;
import org.jvnet.jaxb2_commons.ppp.Child;


/**
 * <p>Java class for CT_EffectList complex type.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
 * 
 * <pre>
 * &lt;complexType name="CT_EffectList"&gt;
 *   &lt;complexContent&gt;
 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType"&gt;
 *       &lt;sequence&gt;
 *         &lt;element name="blur" type="{http://schemas.openxmlformats.org/drawingml/2006/main}CT_BlurEffect" minOccurs="0"/&gt;
 *         &lt;element name="fillOverlay" type="{http://schemas.openxmlformats.org/drawingml/2006/main}CT_FillOverlayEffect" minOccurs="0"/&gt;
 *         &lt;element name="glow" type="{http://schemas.openxmlformats.org/drawingml/2006/main}CT_GlowEffect" minOccurs="0"/&gt;
 *         &lt;element name="innerShdw" type="{http://schemas.openxmlformats.org/drawingml/2006/main}CT_InnerShadowEffect" minOccurs="0"/&gt;
 *         &lt;element name="outerShdw" type="{http://schemas.openxmlformats.org/drawingml/2006/main}CT_OuterShadowEffect" minOccurs="0"/&gt;
 *         &lt;element name="prstShdw" type="{http://schemas.openxmlformats.org/drawingml/2006/main}CT_PresetShadowEffect" minOccurs="0"/&gt;
 *         &lt;element name="reflection" type="{http://schemas.openxmlformats.org/drawingml/2006/main}CT_ReflectionEffect" minOccurs="0"/&gt;
 *         &lt;element name="softEdge" type="{http://schemas.openxmlformats.org/drawingml/2006/main}CT_SoftEdgesEffect" minOccurs="0"/&gt;
 *       &lt;/sequence&gt;
 *     &lt;/restriction&gt;
 *   &lt;/complexContent&gt;
 * &lt;/complexType&gt;
 * </pre>
 * 
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "CT_EffectList", propOrder = {
    "blur",
    "fillOverlay",
    "glow",
    "innerShdw",
    "outerShdw",
    "prstShdw",
    "reflection",
    "softEdge"
})
public class CTEffectList implements Child
{

    protected CTBlurEffect blur;
    protected CTFillOverlayEffect fillOverlay;
    protected CTGlowEffect glow;
    protected CTInnerShadowEffect innerShdw;
    protected CTOuterShadowEffect outerShdw;
    protected CTPresetShadowEffect prstShdw;
    protected CTReflectionEffect reflection;
    protected CTSoftEdgesEffect softEdge;
    @XmlTransient
    private Object parent;

    /**
     * Gets the value of the blur property.
     * 
     * @return
     *     possible object is
     *     {@link CTBlurEffect }
     *     
     */
    public CTBlurEffect getBlur() {
        return blur;
    }

    /**
     * Sets the value of the blur property.
     * 
     * @param value
     *     allowed object is
     *     {@link CTBlurEffect }
     *     
     */
    public void setBlur(CTBlurEffect value) {
        this.blur = value;
    }

    /**
     * Gets the value of the fillOverlay property.
     * 
     * @return
     *     possible object is
     *     {@link CTFillOverlayEffect }
     *     
     */
    public CTFillOverlayEffect getFillOverlay() {
        return fillOverlay;
    }

    /**
     * Sets the value of the fillOverlay property.
     * 
     * @param value
     *     allowed object is
     *     {@link CTFillOverlayEffect }
     *     
     */
    public void setFillOverlay(CTFillOverlayEffect value) {
        this.fillOverlay = value;
    }

    /**
     * Gets the value of the glow property.
     * 
     * @return
     *     possible object is
     *     {@link CTGlowEffect }
     *     
     */
    public CTGlowEffect getGlow() {
        return glow;
    }

    /**
     * Sets the value of the glow property.
     * 
     * @param value
     *     allowed object is
     *     {@link CTGlowEffect }
     *     
     */
    public void setGlow(CTGlowEffect value) {
        this.glow = value;
    }

    /**
     * Gets the value of the innerShdw property.
     * 
     * @return
     *     possible object is
     *     {@link CTInnerShadowEffect }
     *     
     */
    public CTInnerShadowEffect getInnerShdw() {
        return innerShdw;
    }

    /**
     * Sets the value of the innerShdw property.
     * 
     * @param value
     *     allowed object is
     *     {@link CTInnerShadowEffect }
     *     
     */
    public void setInnerShdw(CTInnerShadowEffect value) {
        this.innerShdw = value;
    }

    /**
     * Gets the value of the outerShdw property.
     * 
     * @return
     *     possible object is
     *     {@link CTOuterShadowEffect }
     *     
     */
    public CTOuterShadowEffect getOuterShdw() {
        return outerShdw;
    }

    /**
     * Sets the value of the outerShdw property.
     * 
     * @param value
     *     allowed object is
     *     {@link CTOuterShadowEffect }
     *     
     */
    public void setOuterShdw(CTOuterShadowEffect value) {
        this.outerShdw = value;
    }

    /**
     * Gets the value of the prstShdw property.
     * 
     * @return
     *     possible object is
     *     {@link CTPresetShadowEffect }
     *     
     */
    public CTPresetShadowEffect getPrstShdw() {
        return prstShdw;
    }

    /**
     * Sets the value of the prstShdw property.
     * 
     * @param value
     *     allowed object is
     *     {@link CTPresetShadowEffect }
     *     
     */
    public void setPrstShdw(CTPresetShadowEffect value) {
        this.prstShdw = value;
    }

    /**
     * Gets the value of the reflection property.
     * 
     * @return
     *     possible object is
     *     {@link CTReflectionEffect }
     *     
     */
    public CTReflectionEffect getReflection() {
        return reflection;
    }

    /**
     * Sets the value of the reflection property.
     * 
     * @param value
     *     allowed object is
     *     {@link CTReflectionEffect }
     *     
     */
    public void setReflection(CTReflectionEffect value) {
        this.reflection = value;
    }

    /**
     * Gets the value of the softEdge property.
     * 
     * @return
     *     possible object is
     *     {@link CTSoftEdgesEffect }
     *     
     */
    public CTSoftEdgesEffect getSoftEdge() {
        return softEdge;
    }

    /**
     * Sets the value of the softEdge property.
     * 
     * @param value
     *     allowed object is
     *     {@link CTSoftEdgesEffect }
     *     
     */
    public void setSoftEdge(CTSoftEdgesEffect value) {
        this.softEdge = value;
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
