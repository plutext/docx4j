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
import javax.xml.bind.annotation.XmlType;
import org.docx4j.dml.CTEmbeddedWAVAudioFile;


/**
 * <p>Java class for CT_TLTimeTargetElement complex type.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
 * 
 * <pre>
 * &lt;complexType name="CT_TLTimeTargetElement">
 *   &lt;complexContent>
 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
 *       &lt;choice>
 *         &lt;element name="sldTgt" type="{http://schemas.openxmlformats.org/presentationml/2006/main}CT_Empty"/>
 *         &lt;element name="sndTgt" type="{http://schemas.openxmlformats.org/drawingml/2006/main}CT_EmbeddedWAVAudioFile"/>
 *         &lt;element name="spTgt" type="{http://schemas.openxmlformats.org/presentationml/2006/main}CT_TLShapeTargetElement"/>
 *         &lt;element name="inkTgt" type="{http://schemas.openxmlformats.org/presentationml/2006/main}CT_TLSubShapeId"/>
 *       &lt;/choice>
 *     &lt;/restriction>
 *   &lt;/complexContent>
 * &lt;/complexType>
 * </pre>
 * 
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "CT_TLTimeTargetElement", propOrder = {
    "sldTgt",
    "sndTgt",
    "spTgt",
    "inkTgt"
})
public class CTTLTimeTargetElement {

    protected CTEmpty sldTgt;
    protected CTEmbeddedWAVAudioFile sndTgt;
    protected CTTLShapeTargetElement spTgt;
    protected CTTLSubShapeId inkTgt;

    /**
     * Gets the value of the sldTgt property.
     * 
     * @return
     *     possible object is
     *     {@link CTEmpty }
     *     
     */
    public CTEmpty getSldTgt() {
        return sldTgt;
    }

    /**
     * Sets the value of the sldTgt property.
     * 
     * @param value
     *     allowed object is
     *     {@link CTEmpty }
     *     
     */
    public void setSldTgt(CTEmpty value) {
        this.sldTgt = value;
    }

    /**
     * Gets the value of the sndTgt property.
     * 
     * @return
     *     possible object is
     *     {@link CTEmbeddedWAVAudioFile }
     *     
     */
    public CTEmbeddedWAVAudioFile getSndTgt() {
        return sndTgt;
    }

    /**
     * Sets the value of the sndTgt property.
     * 
     * @param value
     *     allowed object is
     *     {@link CTEmbeddedWAVAudioFile }
     *     
     */
    public void setSndTgt(CTEmbeddedWAVAudioFile value) {
        this.sndTgt = value;
    }

    /**
     * Gets the value of the spTgt property.
     * 
     * @return
     *     possible object is
     *     {@link CTTLShapeTargetElement }
     *     
     */
    public CTTLShapeTargetElement getSpTgt() {
        return spTgt;
    }

    /**
     * Sets the value of the spTgt property.
     * 
     * @param value
     *     allowed object is
     *     {@link CTTLShapeTargetElement }
     *     
     */
    public void setSpTgt(CTTLShapeTargetElement value) {
        this.spTgt = value;
    }

    /**
     * Gets the value of the inkTgt property.
     * 
     * @return
     *     possible object is
     *     {@link CTTLSubShapeId }
     *     
     */
    public CTTLSubShapeId getInkTgt() {
        return inkTgt;
    }

    /**
     * Sets the value of the inkTgt property.
     * 
     * @param value
     *     allowed object is
     *     {@link CTTLSubShapeId }
     *     
     */
    public void setInkTgt(CTTLSubShapeId value) {
        this.inkTgt = value;
    }

}
