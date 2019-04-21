/*
 *  Copyright 2010-2012, Plutext Pty Ltd.
 *   
 *  This file is part of pptx4j, a component of docx4j.

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
package org.pptx4j.pml;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlType;
import org.docx4j.dml.CTNonVisualDrawingProps;
import org.docx4j.dml.CTNonVisualDrawingShapeProps;
import org.docx4j.dml.CTShapeProperties;
import org.docx4j.dml.CTShapeStyle;
import org.docx4j.dml.CTTextBody;


/**
 * <p>Java class for CT_Shape complex type.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
 * 
 * <pre>
 * &lt;complexType name="CT_Shape">
 *   &lt;complexContent>
 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
 *       &lt;sequence>
 *         &lt;element name="nvSpPr">
 *           &lt;complexType>
 *             &lt;complexContent>
 *               &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
 *                 &lt;sequence>
 *                   &lt;element name="cNvPr" type="{http://schemas.openxmlformats.org/drawingml/2006/main}CT_NonVisualDrawingProps"/>
 *                   &lt;element name="cNvSpPr" type="{http://schemas.openxmlformats.org/drawingml/2006/main}CT_NonVisualDrawingShapeProps"/>
 *                   &lt;element name="nvPr" type="{http://schemas.openxmlformats.org/presentationml/2006/main}CT_ApplicationNonVisualDrawingProps"/>
 *                 &lt;/sequence>
 *               &lt;/restriction>
 *             &lt;/complexContent>
 *           &lt;/complexType>
 *         &lt;/element>
 *         &lt;element name="spPr" type="{http://schemas.openxmlformats.org/drawingml/2006/main}CT_ShapeProperties"/>
 *         &lt;element name="style" type="{http://schemas.openxmlformats.org/drawingml/2006/main}CT_ShapeStyle" minOccurs="0"/>
 *         &lt;element name="txBody" type="{http://schemas.openxmlformats.org/drawingml/2006/main}CT_TextBody" minOccurs="0"/>
 *         &lt;element name="extLst" type="{http://schemas.openxmlformats.org/presentationml/2006/main}CT_ExtensionListModify" minOccurs="0"/>
 *       &lt;/sequence>
 *       &lt;attribute name="useBgFill" type="{http://www.w3.org/2001/XMLSchema}boolean" default="false" />
 *     &lt;/restriction>
 *   &lt;/complexContent>
 * &lt;/complexType>
 * </pre>
 * 
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "CT_Shape", propOrder = {
    "nvSpPr",
    "spPr",
    "style",
    "txBody",
    "extLst"
})
@XmlRootElement(name="sp")
public class Shape {

    @XmlElement(required = true)
    protected Shape.NvSpPr nvSpPr;
    @XmlElement(required = true)
    protected CTShapeProperties spPr;
    protected CTShapeStyle style;
    protected CTTextBody txBody;
    protected CTExtensionListModify extLst;
    @XmlAttribute(name = "useBgFill")
    protected Boolean useBgFill;

    /**
     * Gets the value of the nvSpPr property.
     * 
     * @return
     *     possible object is
     *     {@link Shape.NvSpPr }
     *     
     */
    public Shape.NvSpPr getNvSpPr() {
        return nvSpPr;
    }

    /**
     * Sets the value of the nvSpPr property.
     * 
     * @param value
     *     allowed object is
     *     {@link Shape.NvSpPr }
     *     
     */
    public void setNvSpPr(Shape.NvSpPr value) {
        this.nvSpPr = value;
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
     * Gets the value of the style property.
     * 
     * @return
     *     possible object is
     *     {@link CTShapeStyle }
     *     
     */
    public CTShapeStyle getStyle() {
        return style;
    }

    /**
     * Sets the value of the style property.
     * 
     * @param value
     *     allowed object is
     *     {@link CTShapeStyle }
     *     
     */
    public void setStyle(CTShapeStyle value) {
        this.style = value;
    }

    /**
     * Gets the value of the txBody property.
     * 
     * @return
     *     possible object is
     *     {@link CTTextBody }
     *     
     */
    public CTTextBody getTxBody() {
        return txBody;
    }

    /**
     * Sets the value of the txBody property.
     * 
     * @param value
     *     allowed object is
     *     {@link CTTextBody }
     *     
     */
    public void setTxBody(CTTextBody value) {
        this.txBody = value;
    }

    /**
     * Gets the value of the extLst property.
     * 
     * @return
     *     possible object is
     *     {@link CTExtensionListModify }
     *     
     */
    public CTExtensionListModify getExtLst() {
        return extLst;
    }

    /**
     * Sets the value of the extLst property.
     * 
     * @param value
     *     allowed object is
     *     {@link CTExtensionListModify }
     *     
     */
    public void setExtLst(CTExtensionListModify value) {
        this.extLst = value;
    }

    /**
     * Gets the value of the useBgFill property.
     * 
     * @return
     *     possible object is
     *     {@link Boolean }
     *     
     */
    public boolean isUseBgFill() {
        if (useBgFill == null) {
            return false;
        } else {
            return useBgFill;
        }
    }

    /**
     * Sets the value of the useBgFill property.
     * 
     * @param value
     *     allowed object is
     *     {@link Boolean }
     *     
     */
    public void setUseBgFill(Boolean value) {
        this.useBgFill = value;
    }


    /**
     * <p>Java class for anonymous complex type.
     * 
     * <p>The following schema fragment specifies the expected content contained within this class.
     * 
     * <pre>
     * &lt;complexType>
     *   &lt;complexContent>
     *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
     *       &lt;sequence>
     *         &lt;element name="cNvPr" type="{http://schemas.openxmlformats.org/drawingml/2006/main}CT_NonVisualDrawingProps"/>
     *         &lt;element name="cNvSpPr" type="{http://schemas.openxmlformats.org/drawingml/2006/main}CT_NonVisualDrawingShapeProps"/>
     *         &lt;element name="nvPr" type="{http://schemas.openxmlformats.org/presentationml/2006/main}CT_ApplicationNonVisualDrawingProps"/>
     *       &lt;/sequence>
     *     &lt;/restriction>
     *   &lt;/complexContent>
     * &lt;/complexType>
     * </pre>
     * 
     * 
     */
    @XmlAccessorType(XmlAccessType.FIELD)
    @XmlType(name = "", propOrder = {
        "cNvPr",
        "cNvSpPr",
        "nvPr"
    })
    public static class NvSpPr {

        @XmlElement(required = true)
        protected CTNonVisualDrawingProps cNvPr;
        @XmlElement(required = true)
        protected CTNonVisualDrawingShapeProps cNvSpPr;
        @XmlElement(required = true)
        protected NvPr nvPr;

        /**
         * Gets the value of the cNvPr property.
         * 
         * @return
         *     possible object is
         *     {@link CTNonVisualDrawingProps }
         *     
         */
        public CTNonVisualDrawingProps getCNvPr() {
            return cNvPr;
        }

        /**
         * Sets the value of the cNvPr property.
         * 
         * @param value
         *     allowed object is
         *     {@link CTNonVisualDrawingProps }
         *     
         */
        public void setCNvPr(CTNonVisualDrawingProps value) {
            this.cNvPr = value;
        }

        /**
         * Gets the value of the cNvSpPr property.
         * 
         * @return
         *     possible object is
         *     {@link CTNonVisualDrawingShapeProps }
         *     
         */
        public CTNonVisualDrawingShapeProps getCNvSpPr() {
            return cNvSpPr;
        }

        /**
         * Sets the value of the cNvSpPr property.
         * 
         * @param value
         *     allowed object is
         *     {@link CTNonVisualDrawingShapeProps }
         *     
         */
        public void setCNvSpPr(CTNonVisualDrawingShapeProps value) {
            this.cNvSpPr = value;
        }

        /**
         * Gets the value of the nvPr property.
         * 
         * @return
         *     possible object is
         *     {@link NvPr }
         *     
         */
        public NvPr getNvPr() {
            return nvPr;
        }

        /**
         * Sets the value of the nvPr property.
         * 
         * @param value
         *     allowed object is
         *     {@link NvPr }
         *     
         */
        public void setNvPr(NvPr value) {
            this.nvPr = value;
        }

    }

}
