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
import javax.xml.bind.annotation.XmlSchemaType;
import javax.xml.bind.annotation.XmlType;


/**
 * <p>Java class for CT_WheelTransition complex type.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
 * 
 * <pre>
 * &lt;complexType name="CT_WheelTransition">
 *   &lt;complexContent>
 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
 *       &lt;attribute name="spokes" type="{http://www.w3.org/2001/XMLSchema}unsignedInt" default="4" />
 *     &lt;/restriction>
 *   &lt;/complexContent>
 * &lt;/complexType>
 * </pre>
 * 
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "CT_WheelTransition")
public class CTWheelTransition {

    @XmlAttribute(name = "spokes")
    @XmlSchemaType(name = "unsignedInt")
    protected Long spokes;

    /**
     * Gets the value of the spokes property.
     * 
     * @return
     *     possible object is
     *     {@link Long }
     *     
     */
    public long getSpokes() {
        if (spokes == null) {
            return  4L;
        } else {
            return spokes;
        }
    }

    /**
     * Sets the value of the spokes property.
     * 
     * @param value
     *     allowed object is
     *     {@link Long }
     *     
     */
    public void setSpokes(Long value) {
        this.spokes = value;
    }

}
