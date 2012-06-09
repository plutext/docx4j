/*
 * Copyright 2012 Plutext Pty Ltd.
 * 
 * This file is part of docx4j.
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
 
package org.docx4j.dml.spreadsheetdrawing;

import java.util.ArrayList;
import java.util.List;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlElements;
import javax.xml.bind.annotation.XmlType;
import org.docx4j.dml.CTGroupShapeProperties;


/**
 * <p>Java class for CT_GroupShape complex type.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
 * 
 * <pre>
 * &lt;complexType name="CT_GroupShape">
 *   &lt;complexContent>
 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
 *       &lt;sequence>
 *         &lt;element name="nvGrpSpPr" type="{http://schemas.openxmlformats.org/drawingml/2006/spreadsheetDrawing}CT_GroupShapeNonVisual"/>
 *         &lt;element name="grpSpPr" type="{http://schemas.openxmlformats.org/drawingml/2006/main}CT_GroupShapeProperties"/>
 *         &lt;choice maxOccurs="unbounded" minOccurs="0">
 *           &lt;element name="sp" type="{http://schemas.openxmlformats.org/drawingml/2006/spreadsheetDrawing}CT_Shape"/>
 *           &lt;element name="grpSp" type="{http://schemas.openxmlformats.org/drawingml/2006/spreadsheetDrawing}CT_GroupShape"/>
 *           &lt;element name="graphicFrame" type="{http://schemas.openxmlformats.org/drawingml/2006/spreadsheetDrawing}CT_GraphicalObjectFrame"/>
 *           &lt;element name="cxnSp" type="{http://schemas.openxmlformats.org/drawingml/2006/spreadsheetDrawing}CT_Connector"/>
 *           &lt;element name="pic" type="{http://schemas.openxmlformats.org/drawingml/2006/spreadsheetDrawing}CT_Picture"/>
 *         &lt;/choice>
 *       &lt;/sequence>
 *     &lt;/restriction>
 *   &lt;/complexContent>
 * &lt;/complexType>
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
public class CTGroupShape {

    @XmlElement(required = true)
    protected CTGroupShapeNonVisual nvGrpSpPr;
    @XmlElement(required = true)
    protected CTGroupShapeProperties grpSpPr;
    @XmlElements({
        @XmlElement(name = "pic", type = CTPicture.class),
        @XmlElement(name = "sp", type = CTShape.class),
        @XmlElement(name = "graphicFrame", type = CTGraphicalObjectFrame.class),
        @XmlElement(name = "cxnSp", type = CTConnector.class),
        @XmlElement(name = "grpSp", type = CTGroupShape.class)
    })
    protected List<Object> spOrGrpSpOrGraphicFrame;

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
     * {@link CTPicture }
     * {@link CTShape }
     * {@link CTGraphicalObjectFrame }
     * {@link CTConnector }
     * {@link CTGroupShape }
     * 
     * 
     */
    public List<Object> getSpOrGrpSpOrGraphicFrame() {
        if (spOrGrpSpOrGraphicFrame == null) {
            spOrGrpSpOrGraphicFrame = new ArrayList<Object>();
        }
        return this.spOrGrpSpOrGraphicFrame;
    }

}
