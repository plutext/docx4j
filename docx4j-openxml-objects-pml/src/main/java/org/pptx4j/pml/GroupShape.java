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

import java.util.ArrayList;
import java.util.List;
import javax.xml.bind.Unmarshaller;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlElements;
import javax.xml.bind.annotation.XmlTransient;
import javax.xml.bind.annotation.XmlType;
import org.docx4j.dml.CTGroupShapeProperties;
import org.docx4j.dml.CTNonVisualDrawingProps;
import org.docx4j.dml.CTNonVisualGroupDrawingShapeProps;
import org.docx4j.mce.AlternateContent;

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
 *         &lt;element name="nvGrpSpPr"&gt;
 *           &lt;complexType&gt;
 *             &lt;complexContent&gt;
 *               &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType"&gt;
 *                 &lt;sequence&gt;
 *                   &lt;element name="cNvPr" type="{http://schemas.openxmlformats.org/drawingml/2006/main}CT_NonVisualDrawingProps"/&gt;
 *                   &lt;element name="cNvGrpSpPr" type="{http://schemas.openxmlformats.org/drawingml/2006/main}CT_NonVisualGroupDrawingShapeProps"/&gt;
 *                   &lt;element name="nvPr" type="{http://schemas.openxmlformats.org/presentationml/2006/main}CT_ApplicationNonVisualDrawingProps"/&gt;
 *                 &lt;/sequence&gt;
 *               &lt;/restriction&gt;
 *             &lt;/complexContent&gt;
 *           &lt;/complexType&gt;
 *         &lt;/element&gt;
 *         &lt;element name="grpSpPr" type="{http://schemas.openxmlformats.org/drawingml/2006/main}CT_GroupShapeProperties"/&gt;
 *         &lt;choice maxOccurs="unbounded" minOccurs="0"&gt;
 *           &lt;element name="sp" type="{http://schemas.openxmlformats.org/presentationml/2006/main}CT_Shape"/&gt;
 *           &lt;element name="grpSp" type="{http://schemas.openxmlformats.org/presentationml/2006/main}CT_GroupShape"/&gt;
 *           &lt;element name="graphicFrame" type="{http://schemas.openxmlformats.org/presentationml/2006/main}CT_GraphicalObjectFrame"/&gt;
 *           &lt;element name="cxnSp" type="{http://schemas.openxmlformats.org/presentationml/2006/main}CT_Connector"/&gt;
 *           &lt;element name="pic" type="{http://schemas.openxmlformats.org/presentationml/2006/main}CT_Picture"/&gt;
 *           &lt;element name="contentPart" type="{http://schemas.openxmlformats.org/presentationml/2006/main}CT_Rel"/&gt;
 *         &lt;/choice&gt;
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
@XmlType(name = "CT_GroupShape", propOrder = {
    "nvGrpSpPr",
    "grpSpPr",
    "spOrGrpSpOrGraphicFrame",
    "extLst"
})
public class GroupShape implements Child
{

    @XmlElement(required = true)
    protected GroupShape.NvGrpSpPr nvGrpSpPr;
    @XmlElement(required = true)
    protected CTGroupShapeProperties grpSpPr;
    @XmlElements({
        @XmlElement(name = "sp", type = Shape.class),
        @XmlElement(name = "grpSp", type = GroupShape.class),
        @XmlElement(name = "graphicFrame", type = CTGraphicalObjectFrame.class),
        @XmlElement(name = "cxnSp", type = CxnSp.class),
        @XmlElement(name = "pic", type = Pic.class),
        @XmlElement(name = "contentPart", type = CTRel.class),
        @XmlElement(name = "AlternateContent", namespace = "http://schemas.openxmlformats.org/markup-compatibility/2006", type = AlternateContent.class)
    })
    protected List<Object> spOrGrpSpOrGraphicFrame;
    protected CTExtensionListModify extLst;
    @XmlTransient
    private Object parent;

    /**
     * Gets the value of the nvGrpSpPr property.
     * 
     * @return
     *     possible object is
     *     {@link GroupShape.NvGrpSpPr }
     *     
     */
    public GroupShape.NvGrpSpPr getNvGrpSpPr() {
        return nvGrpSpPr;
    }

    /**
     * Sets the value of the nvGrpSpPr property.
     * 
     * @param value
     *     allowed object is
     *     {@link GroupShape.NvGrpSpPr }
     *     
     */
    public void setNvGrpSpPr(GroupShape.NvGrpSpPr value) {
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
     * {@link Shape }
     * {@link GroupShape }
     * {@link CTGraphicalObjectFrame }
     * {@link CxnSp }
     * {@link Pic }
     * {@link CTRel }
     * {@link AlternateContent }
     * 
     * 
     */
    public List<Object> getSpOrGrpSpOrGraphicFrame() {
        if (spOrGrpSpOrGraphicFrame == null) {
            spOrGrpSpOrGraphicFrame = new ArrayList<Object>();
        }
        return this.spOrGrpSpOrGraphicFrame;
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
     *         &lt;element name="cNvGrpSpPr" type="{http://schemas.openxmlformats.org/drawingml/2006/main}CT_NonVisualGroupDrawingShapeProps"/&gt;
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
        "cNvGrpSpPr",
        "nvPr"
    })
    public static class NvGrpSpPr implements Child
    {

        @XmlElement(required = true)
        protected CTNonVisualDrawingProps cNvPr;
        @XmlElement(required = true)
        protected CTNonVisualGroupDrawingShapeProps cNvGrpSpPr;
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
         * Gets the value of the cNvGrpSpPr property.
         * 
         * @return
         *     possible object is
         *     {@link CTNonVisualGroupDrawingShapeProps }
         *     
         */
        public CTNonVisualGroupDrawingShapeProps getCNvGrpSpPr() {
            return cNvGrpSpPr;
        }

        /**
         * Sets the value of the cNvGrpSpPr property.
         * 
         * @param value
         *     allowed object is
         *     {@link CTNonVisualGroupDrawingShapeProps }
         *     
         */
        public void setCNvGrpSpPr(CTNonVisualGroupDrawingShapeProps value) {
            this.cNvGrpSpPr = value;
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
