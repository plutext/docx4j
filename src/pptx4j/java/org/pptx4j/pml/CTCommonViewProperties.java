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
import javax.xml.bind.annotation.XmlType;
import org.docx4j.dml.CTPoint2D;
import org.docx4j.dml.CTScale2D;


/**
 * <p>Java class for CT_CommonViewProperties complex type.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
 * 
 * <pre>
 * &lt;complexType name="CT_CommonViewProperties">
 *   &lt;complexContent>
 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
 *       &lt;sequence>
 *         &lt;element name="scale" type="{http://schemas.openxmlformats.org/drawingml/2006/main}CT_Scale2D"/>
 *         &lt;element name="origin" type="{http://schemas.openxmlformats.org/drawingml/2006/main}CT_Point2D"/>
 *       &lt;/sequence>
 *       &lt;attribute name="varScale" type="{http://www.w3.org/2001/XMLSchema}boolean" default="false" />
 *     &lt;/restriction>
 *   &lt;/complexContent>
 * &lt;/complexType>
 * </pre>
 * 
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "CT_CommonViewProperties", propOrder = {
    "scale",
    "origin"
})
public class CTCommonViewProperties {

    @XmlElement(required = true)
    protected CTScale2D scale;
    @XmlElement(required = true)
    protected CTPoint2D origin;
    @XmlAttribute(name = "varScale")
    protected Boolean varScale;

    /**
     * Gets the value of the scale property.
     * 
     * @return
     *     possible object is
     *     {@link CTScale2D }
     *     
     */
    public CTScale2D getScale() {
        return scale;
    }

    /**
     * Sets the value of the scale property.
     * 
     * @param value
     *     allowed object is
     *     {@link CTScale2D }
     *     
     */
    public void setScale(CTScale2D value) {
        this.scale = value;
    }

    /**
     * Gets the value of the origin property.
     * 
     * @return
     *     possible object is
     *     {@link CTPoint2D }
     *     
     */
    public CTPoint2D getOrigin() {
        return origin;
    }

    /**
     * Sets the value of the origin property.
     * 
     * @param value
     *     allowed object is
     *     {@link CTPoint2D }
     *     
     */
    public void setOrigin(CTPoint2D value) {
        this.origin = value;
    }

    /**
     * Gets the value of the varScale property.
     * 
     * @return
     *     possible object is
     *     {@link Boolean }
     *     
     */
    public boolean isVarScale() {
        if (varScale == null) {
            return false;
        } else {
            return varScale;
        }
    }

    /**
     * Sets the value of the varScale property.
     * 
     * @param value
     *     allowed object is
     *     {@link Boolean }
     *     
     */
    public void setVarScale(Boolean value) {
        this.varScale = value;
    }

}
