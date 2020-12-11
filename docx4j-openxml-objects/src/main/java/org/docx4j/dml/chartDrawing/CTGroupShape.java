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

import org.docx4j.dml.ArrayListDml;
import java.util.List;
import javax.xml.bind.Unmarshaller;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlElements;
import javax.xml.bind.annotation.XmlTransient;
import javax.xml.bind.annotation.XmlType;
import org.docx4j.dml.CTGroupShapeProperties;
import org.jvnet.jaxb2_commons.ppp.Child;


/**
 * <p>Java class for CT_GroupShape complex type.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
 * 
 * <pre>
 * &lt;complexType name="CT_GroupShape"&gt;
 *   &lt;complexContent&gt;
 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType"&gt;
 *       &lt;sequence&gt;
 *         &lt;element name="nvGrpSpPr" type="{http://schemas.openxmlformats.org/drawingml/2006/chartDrawing}CT_GroupShapeNonVisual"/&gt;
 *         &lt;element name="grpSpPr" type="{http://schemas.openxmlformats.org/drawingml/2006/main}CT_GroupShapeProperties"/&gt;
 *         &lt;choice maxOccurs="unbounded" minOccurs="0"&gt;
 *           &lt;element name="sp" type="{http://schemas.openxmlformats.org/drawingml/2006/chartDrawing}CT_Shape"/&gt;
 *           &lt;element name="grpSp" type="{http://schemas.openxmlformats.org/drawingml/2006/chartDrawing}CT_GroupShape"/&gt;
 *           &lt;element name="graphicFrame" type="{http://schemas.openxmlformats.org/drawingml/2006/chartDrawing}CT_GraphicFrame"/&gt;
 *           &lt;element name="cxnSp" type="{http://schemas.openxmlformats.org/drawingml/2006/chartDrawing}CT_Connector"/&gt;
 *           &lt;element name="pic" type="{http://schemas.openxmlformats.org/drawingml/2006/chartDrawing}CT_Picture"/&gt;
 *         &lt;/choice&gt;
 *       &lt;/sequence&gt;
 *     &lt;/restriction&gt;
 *   &lt;/complexContent&gt;
 * &lt;/complexType&gt;
 * </pre>
 * 
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "CT_GroupShape", propOrder = {
    "nvGrpSpPr",
    "grpSpPr",
    "spOrGrpSpOrGraphicFrame"
})
public class CTGroupShape implements Child
{

    @XmlElement(required = true)
    protected CTGroupShapeNonVisual nvGrpSpPr;
    @XmlElement(required = true)
    protected CTGroupShapeProperties grpSpPr;
    @XmlElements({
        @XmlElement(name = "sp", type = CTShape.class),
        @XmlElement(name = "grpSp", type = CTGroupShape.class),
        @XmlElement(name = "graphicFrame", type = CTGraphicFrame.class),
        @XmlElement(name = "cxnSp", type = CTConnector.class),
        @XmlElement(name = "pic", type = CTPicture.class)
    })
    protected List<Object> spOrGrpSpOrGraphicFrame = new ArrayListDml<Object>(this);

    @XmlTransient
    private Object parent;

    /**
     * Gets the value of the nvGrpSpPr property.
     * 
     * @return
     *     possible object is
     *     {@link CTGroupShapeNonVisual }
     *     
     */
    public CTGroupShapeNonVisual getNvGrpSpPr() {
        return nvGrpSpPr;
    }

    /**
     * Sets the value of the nvGrpSpPr property.
     * 
     * @param value
     *     allowed object is
     *     {@link CTGroupShapeNonVisual }
     *     
     */
    public void setNvGrpSpPr(CTGroupShapeNonVisual value) {
        this.nvGrpSpPr = value;
    }

    /**
     * Gets the value of the grpSpPr property.
     * 
     * @return
     *     possible object is
     *     {@link CTGroupShapeProperties }
     *     
     */
    public CTGroupShapeProperties getGrpSpPr() {
        return grpSpPr;
    }

    /**
     * Sets the value of the grpSpPr property.
     * 
     * @param value
     *     allowed object is
     *     {@link CTGroupShapeProperties }
     *     
     */
    public void setGrpSpPr(CTGroupShapeProperties value) {
        this.grpSpPr = value;
    }

    /**
     * Gets the value of the spOrGrpSpOrGraphicFrame property.
     * 
     * <p>
     * This accessor method returns a reference to the live list,
     * not a snapshot. Therefore any modification you make to the
     * returned list will be present inside the JAXB object.
     * This is why there is not a <CODE>set</CODE> method for the spOrGrpSpOrGraphicFrame property.
     * 
     * <p>
     * For example, to add a new item, do as follows:
     * <pre>
     *    getSpOrGrpSpOrGraphicFrame().add(newItem);
     * </pre>
     * 
     * 
     * <p>
     * Objects of the following type(s) are allowed in the list
     * {@link CTShape }
     * {@link CTGroupShape }
     * {@link CTGraphicFrame }
     * {@link CTConnector }
     * {@link CTPicture }
     * 
     * 
     */
    public List<Object> getSpOrGrpSpOrGraphicFrame() {
        if (spOrGrpSpOrGraphicFrame == null) {
            spOrGrpSpOrGraphicFrame = new ArrayListDml<Object>(this);
        }
        return this.spOrGrpSpOrGraphicFrame;
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
