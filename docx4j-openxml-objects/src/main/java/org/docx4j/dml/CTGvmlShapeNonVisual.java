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
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlTransient;
import javax.xml.bind.annotation.XmlType;
import org.jvnet.jaxb2_commons.ppp.Child;


/**
 * <p>Java class for CT_GvmlShapeNonVisual complex type.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
 * 
 * <pre>
 * &lt;complexType name="CT_GvmlShapeNonVisual"&gt;
 *   &lt;complexContent&gt;
 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType"&gt;
 *       &lt;sequence&gt;
 *         &lt;element name="cNvPr" type="{http://schemas.openxmlformats.org/drawingml/2006/main}CT_NonVisualDrawingProps"/&gt;
 *         &lt;element name="cNvSpPr" type="{http://schemas.openxmlformats.org/drawingml/2006/main}CT_NonVisualDrawingShapeProps"/&gt;
 *       &lt;/sequence&gt;
 *     &lt;/restriction&gt;
 *   &lt;/complexContent&gt;
 * &lt;/complexType&gt;
 * </pre>
 * 
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "CT_GvmlShapeNonVisual", propOrder = {
    "cNvPr",
    "cNvSpPr"
})
public class CTGvmlShapeNonVisual implements Child
{

    @XmlElement(required = true)
    protected CTNonVisualDrawingProps cNvPr;
    @XmlElement(required = true)
    protected CTNonVisualDrawingShapeProps cNvSpPr;
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
