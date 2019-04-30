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


package org.docx4j.dml.wordprocessingDrawing;

import javax.xml.bind.Unmarshaller;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlTransient;
import javax.xml.bind.annotation.XmlType;
import org.docx4j.dml.CTNonVisualDrawingProps;
import org.docx4j.dml.CTNonVisualGraphicFrameProperties;
import org.docx4j.dml.CTPositiveSize2D;
import org.docx4j.dml.Graphic;
import org.jvnet.jaxb2_commons.ppp.Child;


/**
 * <p>Java class for CT_Inline complex type.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
 * 
 * <pre>
 * &lt;complexType name="CT_Inline"&gt;
 *   &lt;complexContent&gt;
 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType"&gt;
 *       &lt;sequence&gt;
 *         &lt;element name="extent" type="{http://schemas.openxmlformats.org/drawingml/2006/main}CT_PositiveSize2D"/&gt;
 *         &lt;element name="effectExtent" type="{http://schemas.openxmlformats.org/drawingml/2006/wordprocessingDrawing}CT_EffectExtent" minOccurs="0"/&gt;
 *         &lt;element name="docPr" type="{http://schemas.openxmlformats.org/drawingml/2006/main}CT_NonVisualDrawingProps"/&gt;
 *         &lt;element name="cNvGraphicFramePr" type="{http://schemas.openxmlformats.org/drawingml/2006/main}CT_NonVisualGraphicFrameProperties" minOccurs="0"/&gt;
 *         &lt;element ref="{http://schemas.openxmlformats.org/drawingml/2006/main}graphic"/&gt;
 *       &lt;/sequence&gt;
 *       &lt;attribute name="distT" type="{http://schemas.openxmlformats.org/drawingml/2006/wordprocessingDrawing}ST_WrapDistance" /&gt;
 *       &lt;attribute name="distB" type="{http://schemas.openxmlformats.org/drawingml/2006/wordprocessingDrawing}ST_WrapDistance" /&gt;
 *       &lt;attribute name="distL" type="{http://schemas.openxmlformats.org/drawingml/2006/wordprocessingDrawing}ST_WrapDistance" /&gt;
 *       &lt;attribute name="distR" type="{http://schemas.openxmlformats.org/drawingml/2006/wordprocessingDrawing}ST_WrapDistance" /&gt;
 *     &lt;/restriction&gt;
 *   &lt;/complexContent&gt;
 * &lt;/complexType&gt;
 * </pre>
 * 
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "CT_Inline", propOrder = {
    "extent",
    "effectExtent",
    "docPr",
    "cNvGraphicFramePr",
    "graphic"
})
public class Inline implements Child
{

    @XmlElement(required = true)
    protected CTPositiveSize2D extent;
    protected CTEffectExtent effectExtent;
    @XmlElement(required = true)
    protected CTNonVisualDrawingProps docPr;
    protected CTNonVisualGraphicFrameProperties cNvGraphicFramePr;
    @XmlElement(namespace = "http://schemas.openxmlformats.org/drawingml/2006/main", required = true)
    protected Graphic graphic;
    @XmlAttribute(name = "distT")
    protected Long distT;
    @XmlAttribute(name = "distB")
    protected Long distB;
    @XmlAttribute(name = "distL")
    protected Long distL;
    @XmlAttribute(name = "distR")
    protected Long distR;
    @XmlTransient
    private Object parent;

    /**
     * Gets the value of the extent property.
     * 
     * @return
     *     possible object is
     *     {@link CTPositiveSize2D }
     *     
     */
    public CTPositiveSize2D getExtent() {
        return extent;
    }

    /**
     * Sets the value of the extent property.
     * 
     * @param value
     *     allowed object is
     *     {@link CTPositiveSize2D }
     *     
     */
    public void setExtent(CTPositiveSize2D value) {
        this.extent = value;
    }

    /**
     * Gets the value of the effectExtent property.
     * 
     * @return
     *     possible object is
     *     {@link CTEffectExtent }
     *     
     */
    public CTEffectExtent getEffectExtent() {
        return effectExtent;
    }

    /**
     * Sets the value of the effectExtent property.
     * 
     * @param value
     *     allowed object is
     *     {@link CTEffectExtent }
     *     
     */
    public void setEffectExtent(CTEffectExtent value) {
        this.effectExtent = value;
    }

    /**
     * Gets the value of the docPr property.
     * 
     * @return
     *     possible object is
     *     {@link CTNonVisualDrawingProps }
     *     
     */
    public CTNonVisualDrawingProps getDocPr() {
        return docPr;
    }

    /**
     * Sets the value of the docPr property.
     * 
     * @param value
     *     allowed object is
     *     {@link CTNonVisualDrawingProps }
     *     
     */
    public void setDocPr(CTNonVisualDrawingProps value) {
        this.docPr = value;
    }

    /**
     * Gets the value of the cNvGraphicFramePr property.
     * 
     * @return
     *     possible object is
     *     {@link CTNonVisualGraphicFrameProperties }
     *     
     */
    public CTNonVisualGraphicFrameProperties getCNvGraphicFramePr() {
        return cNvGraphicFramePr;
    }

    /**
     * Sets the value of the cNvGraphicFramePr property.
     * 
     * @param value
     *     allowed object is
     *     {@link CTNonVisualGraphicFrameProperties }
     *     
     */
    public void setCNvGraphicFramePr(CTNonVisualGraphicFrameProperties value) {
        this.cNvGraphicFramePr = value;
    }

    /**
     * Gets the value of the graphic property.
     * 
     * @return
     *     possible object is
     *     {@link Graphic }
     *     
     */
    public Graphic getGraphic() {
        return graphic;
    }

    /**
     * Sets the value of the graphic property.
     * 
     * @param value
     *     allowed object is
     *     {@link Graphic }
     *     
     */
    public void setGraphic(Graphic value) {
        this.graphic = value;
    }

    /**
     * Gets the value of the distT property.
     * 
     * @return
     *     possible object is
     *     {@link Long }
     *     
     */
    public Long getDistT() {
        return distT;
    }

    /**
     * Sets the value of the distT property.
     * 
     * @param value
     *     allowed object is
     *     {@link Long }
     *     
     */
    public void setDistT(Long value) {
        this.distT = value;
    }

    /**
     * Gets the value of the distB property.
     * 
     * @return
     *     possible object is
     *     {@link Long }
     *     
     */
    public Long getDistB() {
        return distB;
    }

    /**
     * Sets the value of the distB property.
     * 
     * @param value
     *     allowed object is
     *     {@link Long }
     *     
     */
    public void setDistB(Long value) {
        this.distB = value;
    }

    /**
     * Gets the value of the distL property.
     * 
     * @return
     *     possible object is
     *     {@link Long }
     *     
     */
    public Long getDistL() {
        return distL;
    }

    /**
     * Sets the value of the distL property.
     * 
     * @param value
     *     allowed object is
     *     {@link Long }
     *     
     */
    public void setDistL(Long value) {
        this.distL = value;
    }

    /**
     * Gets the value of the distR property.
     * 
     * @return
     *     possible object is
     *     {@link Long }
     *     
     */
    public Long getDistR() {
        return distR;
    }

    /**
     * Sets the value of the distR property.
     * 
     * @param value
     *     allowed object is
     *     {@link Long }
     *     
     */
    public void setDistR(Long value) {
        this.distR = value;
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
