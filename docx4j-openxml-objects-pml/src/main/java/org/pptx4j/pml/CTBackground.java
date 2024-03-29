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

import jakarta.xml.bind.Unmarshaller;
import jakarta.xml.bind.annotation.XmlAccessType;
import jakarta.xml.bind.annotation.XmlAccessorType;
import jakarta.xml.bind.annotation.XmlAttribute;
import jakarta.xml.bind.annotation.XmlTransient;
import jakarta.xml.bind.annotation.XmlType;
import org.docx4j.dml.CTStyleMatrixReference;
import org.docx4j.dml.STBlackWhiteMode;

import org.jvnet.jaxb2_commons.ppp.Child;


/**
 * <p>Java class for CT_Background complex type.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
 * 
 * <pre>
 * &lt;complexType name="CT_Background"&gt;
 *   &lt;complexContent&gt;
 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType"&gt;
 *       &lt;sequence&gt;
 *         &lt;group ref="{http://schemas.openxmlformats.org/presentationml/2006/main}EG_Background"/&gt;
 *       &lt;/sequence&gt;
 *       &lt;attribute name="bwMode" type="{http://schemas.openxmlformats.org/drawingml/2006/main}ST_BlackWhiteMode" default="white" /&gt;
 *     &lt;/restriction&gt;
 *   &lt;/complexContent&gt;
 * &lt;/complexType&gt;
 * </pre>
 * 
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "CT_Background", propOrder = {
    "bgPr",
    "bgRef"
})
public class CTBackground implements Child
{

    protected CTBackgroundProperties bgPr;
    protected CTStyleMatrixReference bgRef;
    @XmlAttribute(name = "bwMode")
    protected STBlackWhiteMode bwMode;
    @XmlTransient
    private Object parent;

    /**
     * Gets the value of the bgPr property.
     * 
     * @return
     *     possible object is
     *     {@link CTBackgroundProperties }
     *     
     */
    public CTBackgroundProperties getBgPr() {
        return bgPr;
    }

    /**
     * Sets the value of the bgPr property.
     * 
     * @param value
     *     allowed object is
     *     {@link CTBackgroundProperties }
     *     
     */
    public void setBgPr(CTBackgroundProperties value) {
        this.bgPr = value;
    }

    /**
     * Gets the value of the bgRef property.
     * 
     * @return
     *     possible object is
     *     {@link CTStyleMatrixReference }
     *     
     */
    public CTStyleMatrixReference getBgRef() {
        return bgRef;
    }

    /**
     * Sets the value of the bgRef property.
     * 
     * @param value
     *     allowed object is
     *     {@link CTStyleMatrixReference }
     *     
     */
    public void setBgRef(CTStyleMatrixReference value) {
        this.bgRef = value;
    }

    /**
     * Gets the value of the bwMode property.
     * 
     * @return
     *     possible object is
     *     {@link STBlackWhiteMode }
     *     
     */
    public STBlackWhiteMode getBwMode() {
        if (bwMode == null) {
            return STBlackWhiteMode.WHITE;
        } else {
            return bwMode;
        }
    }

    /**
     * Sets the value of the bwMode property.
     * 
     * @param value
     *     allowed object is
     *     {@link STBlackWhiteMode }
     *     
     */
    public void setBwMode(STBlackWhiteMode value) {
        this.bwMode = value;
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
