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

import javax.xml.bind.Unmarshaller;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlTransient;
import javax.xml.bind.annotation.XmlType;
import org.docx4j.dml.CTBlipFillProperties;
import org.docx4j.dml.CTNonVisualDrawingProps;
import org.docx4j.dml.CTNonVisualPictureProperties;
import org.docx4j.dml.CTShapeProperties;
import org.docx4j.dml.CTShapeStyle;

import org.jvnet.jaxb2_commons.ppp.Child;


/**
 * <p>Java class for CT_Picture complex type.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
 * 
 * <pre>
 * &lt;complexType name="CT_Picture"&gt;
 *   &lt;complexContent&gt;
 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType"&gt;
 *       &lt;sequence&gt;
 *         &lt;element name="nvPicPr"&gt;
 *           &lt;complexType&gt;
 *             &lt;complexContent&gt;
 *               &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType"&gt;
 *                 &lt;sequence&gt;
 *                   &lt;element name="cNvPr" type="{http://schemas.openxmlformats.org/drawingml/2006/main}CT_NonVisualDrawingProps"/&gt;
 *                   &lt;element name="cNvPicPr" type="{http://schemas.openxmlformats.org/drawingml/2006/main}CT_NonVisualPictureProperties"/&gt;
 *                   &lt;element name="nvPr" type="{http://schemas.openxmlformats.org/presentationml/2006/main}CT_ApplicationNonVisualDrawingProps"/&gt;
 *                 &lt;/sequence&gt;
 *               &lt;/restriction&gt;
 *             &lt;/complexContent&gt;
 *           &lt;/complexType&gt;
 *         &lt;/element&gt;
 *         &lt;element name="blipFill" type="{http://schemas.openxmlformats.org/drawingml/2006/main}CT_BlipFillProperties"/&gt;
 *         &lt;element name="spPr" type="{http://schemas.openxmlformats.org/drawingml/2006/main}CT_ShapeProperties"/&gt;
 *         &lt;element name="style" type="{http://schemas.openxmlformats.org/drawingml/2006/main}CT_ShapeStyle" minOccurs="0"/&gt;
 *         &lt;element name="extLst" type="{http://schemas.openxmlformats.org/presentationml/2006/main}CT_ExtensionListModify" minOccurs="0"/&gt;
 *       &lt;/sequence&gt;
 *     &lt;/restriction&gt;
 *   &lt;/complexContent&gt;
 * &lt;/complexType&gt;
 * </pre>
 * 
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "CT_Picture", propOrder = {
    "nvPicPr",
    "blipFill",
    "spPr",
    "style",
    "extLst"
})
public class Pic implements Child
{

    @XmlElement(required = true)
    protected Pic.NvPicPr nvPicPr;
    @XmlElement(required = true)
    protected CTBlipFillProperties blipFill;
    @XmlElement(required = true)
    protected CTShapeProperties spPr;
    protected CTShapeStyle style;
    protected CTExtensionListModify extLst;
    @XmlTransient
    private Object parent;

    /**
     * Gets the value of the nvPicPr property.
     * 
     * @return
     *     possible object is
     *     {@link Pic.NvPicPr }
     *     
     */
    public Pic.NvPicPr getNvPicPr() {
        return nvPicPr;
    }

    /**
     * Sets the value of the nvPicPr property.
     * 
     * @param value
     *     allowed object is
     *     {@link Pic.NvPicPr }
     *     
     */
    public void setNvPicPr(Pic.NvPicPr value) {
        this.nvPicPr = value;
    }

    /**
     * Gets the value of the blipFill property.
     * 
     * @return
     *     possible object is
     *     {@link CTBlipFillProperties }
     *     
     */
    public CTBlipFillProperties getBlipFill() {
        return blipFill;
    }

    /**
     * Sets the value of the blipFill property.
     * 
     * @param value
     *     allowed object is
     *     {@link CTBlipFillProperties }
     *     
     */
    public void setBlipFill(CTBlipFillProperties value) {
        this.blipFill = value;
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


    /**
     * <p>Java class for anonymous complex type.
     * 
     * <p>The following schema fragment specifies the expected content contained within this class.
     * 
     * <pre>
     * &lt;complexType&gt;
     *   &lt;complexContent&gt;
     *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType"&gt;
     *       &lt;sequence&gt;
     *         &lt;element name="cNvPr" type="{http://schemas.openxmlformats.org/drawingml/2006/main}CT_NonVisualDrawingProps"/&gt;
     *         &lt;element name="cNvPicPr" type="{http://schemas.openxmlformats.org/drawingml/2006/main}CT_NonVisualPictureProperties"/&gt;
     *         &lt;element name="nvPr" type="{http://schemas.openxmlformats.org/presentationml/2006/main}CT_ApplicationNonVisualDrawingProps"/&gt;
     *       &lt;/sequence&gt;
     *     &lt;/restriction&gt;
     *   &lt;/complexContent&gt;
     * &lt;/complexType&gt;
     * </pre>
     * 
     * 
     */
    @XmlAccessorType(XmlAccessType.FIELD)
    @XmlType(name = "", propOrder = {
        "cNvPr",
        "cNvPicPr",
        "nvPr"
    })
    public static class NvPicPr implements Child
    {

        @XmlElement(required = true)
        protected CTNonVisualDrawingProps cNvPr;
        @XmlElement(required = true)
        protected CTNonVisualPictureProperties cNvPicPr;
        @XmlElement(required = true)
        protected NvPr nvPr;
        @XmlTransient
        private Object parent;

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
         * Gets the value of the cNvPicPr property.
         * 
         * @return
         *     possible object is
         *     {@link CTNonVisualPictureProperties }
         *     
         */
        public CTNonVisualPictureProperties getCNvPicPr() {
            return cNvPicPr;
        }

        /**
         * Sets the value of the cNvPicPr property.
         * 
         * @param value
         *     allowed object is
         *     {@link CTNonVisualPictureProperties }
         *     
         */
        public void setCNvPicPr(CTNonVisualPictureProperties value) {
            this.cNvPicPr = value;
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

}
