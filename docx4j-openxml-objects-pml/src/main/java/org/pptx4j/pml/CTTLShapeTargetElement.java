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
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlTransient;
import javax.xml.bind.annotation.XmlType;
import javax.xml.bind.annotation.adapters.CollapsedStringAdapter;
import javax.xml.bind.annotation.adapters.XmlJavaTypeAdapter;
import org.docx4j.dml.CTAnimationElementChoice;

import org.jvnet.jaxb2_commons.ppp.Child;


/**
 * <p>Java class for CT_TLShapeTargetElement complex type.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
 * 
 * <pre>
 * &lt;complexType name="CT_TLShapeTargetElement"&gt;
 *   &lt;complexContent&gt;
 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType"&gt;
 *       &lt;choice minOccurs="0"&gt;
 *         &lt;element name="bg" type="{http://schemas.openxmlformats.org/presentationml/2006/main}CT_Empty"/&gt;
 *         &lt;element name="subSp" type="{http://schemas.openxmlformats.org/presentationml/2006/main}CT_TLSubShapeId"/&gt;
 *         &lt;element name="oleChartEl" type="{http://schemas.openxmlformats.org/presentationml/2006/main}CT_TLOleChartTargetElement"/&gt;
 *         &lt;element name="txEl" type="{http://schemas.openxmlformats.org/presentationml/2006/main}CT_TLTextTargetElement"/&gt;
 *         &lt;element name="graphicEl" type="{http://schemas.openxmlformats.org/drawingml/2006/main}CT_AnimationElementChoice"/&gt;
 *       &lt;/choice&gt;
 *       &lt;attribute name="spid" use="required" type="{http://schemas.openxmlformats.org/drawingml/2006/main}ST_ShapeID" /&gt;
 *     &lt;/restriction&gt;
 *   &lt;/complexContent&gt;
 * &lt;/complexType&gt;
 * </pre>
 * 
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "CT_TLShapeTargetElement", propOrder = {
    "bg",
    "subSp",
    "oleChartEl",
    "txEl",
    "graphicEl"
})
public class CTTLShapeTargetElement implements Child
{

    protected CTEmpty bg;
    protected CTTLSubShapeId subSp;
    protected CTTLOleChartTargetElement oleChartEl;
    protected CTTLTextTargetElement txEl;
    protected CTAnimationElementChoice graphicEl;
    @XmlAttribute(name = "spid", required = true)
    @XmlJavaTypeAdapter(CollapsedStringAdapter.class)
    protected String spid;
    @XmlTransient
    private Object parent;

    /**
     * Gets the value of the bg property.
     * 
     * @return
     *     possible object is
     *     {@link CTEmpty }
     *     
     */
    public CTEmpty getBg() {
        return bg;
    }

    /**
     * Sets the value of the bg property.
     * 
     * @param value
     *     allowed object is
     *     {@link CTEmpty }
     *     
     */
    public void setBg(CTEmpty value) {
        this.bg = value;
    }

    /**
     * Gets the value of the subSp property.
     * 
     * @return
     *     possible object is
     *     {@link CTTLSubShapeId }
     *     
     */
    public CTTLSubShapeId getSubSp() {
        return subSp;
    }

    /**
     * Sets the value of the subSp property.
     * 
     * @param value
     *     allowed object is
     *     {@link CTTLSubShapeId }
     *     
     */
    public void setSubSp(CTTLSubShapeId value) {
        this.subSp = value;
    }

    /**
     * Gets the value of the oleChartEl property.
     * 
     * @return
     *     possible object is
     *     {@link CTTLOleChartTargetElement }
     *     
     */
    public CTTLOleChartTargetElement getOleChartEl() {
        return oleChartEl;
    }

    /**
     * Sets the value of the oleChartEl property.
     * 
     * @param value
     *     allowed object is
     *     {@link CTTLOleChartTargetElement }
     *     
     */
    public void setOleChartEl(CTTLOleChartTargetElement value) {
        this.oleChartEl = value;
    }

    /**
     * Gets the value of the txEl property.
     * 
     * @return
     *     possible object is
     *     {@link CTTLTextTargetElement }
     *     
     */
    public CTTLTextTargetElement getTxEl() {
        return txEl;
    }

    /**
     * Sets the value of the txEl property.
     * 
     * @param value
     *     allowed object is
     *     {@link CTTLTextTargetElement }
     *     
     */
    public void setTxEl(CTTLTextTargetElement value) {
        this.txEl = value;
    }

    /**
     * Gets the value of the graphicEl property.
     * 
     * @return
     *     possible object is
     *     {@link CTAnimationElementChoice }
     *     
     */
    public CTAnimationElementChoice getGraphicEl() {
        return graphicEl;
    }

    /**
     * Sets the value of the graphicEl property.
     * 
     * @param value
     *     allowed object is
     *     {@link CTAnimationElementChoice }
     *     
     */
    public void setGraphicEl(CTAnimationElementChoice value) {
        this.graphicEl = value;
    }

    /**
     * Gets the value of the spid property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getSpid() {
        return spid;
    }

    /**
     * Sets the value of the spid property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setSpid(String value) {
        this.spid = value;
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
