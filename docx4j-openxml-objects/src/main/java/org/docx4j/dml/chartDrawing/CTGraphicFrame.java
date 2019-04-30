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


package org.docx4j.dml.chartDrawing;

import javax.xml.bind.Unmarshaller;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlTransient;
import javax.xml.bind.annotation.XmlType;
import org.docx4j.dml.CTTransform2D;
import org.docx4j.dml.Graphic;
import org.jvnet.jaxb2_commons.ppp.Child;


/**
 * <p>Java class for CT_GraphicFrame complex type.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
 * 
 * <pre>
 * &lt;complexType name="CT_GraphicFrame"&gt;
 *   &lt;complexContent&gt;
 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType"&gt;
 *       &lt;sequence&gt;
 *         &lt;element name="nvGraphicFramePr" type="{http://schemas.openxmlformats.org/drawingml/2006/chartDrawing}CT_GraphicFrameNonVisual"/&gt;
 *         &lt;element name="xfrm" type="{http://schemas.openxmlformats.org/drawingml/2006/main}CT_Transform2D"/&gt;
 *         &lt;element ref="{http://schemas.openxmlformats.org/drawingml/2006/main}graphic"/&gt;
 *       &lt;/sequence&gt;
 *       &lt;attribute name="macro" type="{http://www.w3.org/2001/XMLSchema}string" /&gt;
 *       &lt;attribute name="fPublished" type="{http://www.w3.org/2001/XMLSchema}boolean" default="false" /&gt;
 *     &lt;/restriction&gt;
 *   &lt;/complexContent&gt;
 * &lt;/complexType&gt;
 * </pre>
 * 
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "CT_GraphicFrame", propOrder = {
    "nvGraphicFramePr",
    "xfrm",
    "graphic"
})
public class CTGraphicFrame implements Child
{

    @XmlElement(required = true)
    protected CTGraphicFrameNonVisual nvGraphicFramePr;
    @XmlElement(required = true)
    protected CTTransform2D xfrm;
    @XmlElement(namespace = "http://schemas.openxmlformats.org/drawingml/2006/main", required = true)
    protected Graphic graphic;
    @XmlAttribute(name = "macro")
    protected String macro;
    @XmlAttribute(name = "fPublished")
    protected Boolean fPublished;
    @XmlTransient
    private Object parent;

    /**
     * Gets the value of the nvGraphicFramePr property.
     * 
     * @return
     *     possible object is
     *     {@link CTGraphicFrameNonVisual }
     *     
     */
    public CTGraphicFrameNonVisual getNvGraphicFramePr() {
        return nvGraphicFramePr;
    }

    /**
     * Sets the value of the nvGraphicFramePr property.
     * 
     * @param value
     *     allowed object is
     *     {@link CTGraphicFrameNonVisual }
     *     
     */
    public void setNvGraphicFramePr(CTGraphicFrameNonVisual value) {
        this.nvGraphicFramePr = value;
    }

    /**
     * Gets the value of the xfrm property.
     * 
     * @return
     *     possible object is
     *     {@link CTTransform2D }
     *     
     */
    public CTTransform2D getXfrm() {
        return xfrm;
    }

    /**
     * Sets the value of the xfrm property.
     * 
     * @param value
     *     allowed object is
     *     {@link CTTransform2D }
     *     
     */
    public void setXfrm(CTTransform2D value) {
        this.xfrm = value;
    }

    /**
     * Graphical Object
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
     * Gets the value of the macro property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getMacro() {
        return macro;
    }

    /**
     * Sets the value of the macro property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setMacro(String value) {
        this.macro = value;
    }

    /**
     * Gets the value of the fPublished property.
     * 
     * @return
     *     possible object is
     *     {@link Boolean }
     *     
     */
    public boolean isFPublished() {
        if (fPublished == null) {
            return false;
        } else {
            return fPublished;
        }
    }

    /**
     * Sets the value of the fPublished property.
     * 
     * @param value
     *     allowed object is
     *     {@link Boolean }
     *     
     */
    public void setFPublished(Boolean value) {
        this.fPublished = value;
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
