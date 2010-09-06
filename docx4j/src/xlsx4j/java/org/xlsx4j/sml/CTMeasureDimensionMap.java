/*
 *  Copyright 2010, Plutext Pty Ltd.
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


package org.xlsx4j.sml;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlSchemaType;
import javax.xml.bind.annotation.XmlType;


/**
 * <p>Java class for CT_MeasureDimensionMap complex type.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
 * 
 * <pre>
 * &lt;complexType name="CT_MeasureDimensionMap">
 *   &lt;complexContent>
 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
 *       &lt;attribute name="measureGroup" type="{http://www.w3.org/2001/XMLSchema}unsignedInt" />
 *       &lt;attribute name="dimension" type="{http://www.w3.org/2001/XMLSchema}unsignedInt" />
 *     &lt;/restriction>
 *   &lt;/complexContent>
 * &lt;/complexType>
 * </pre>
 * 
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "CT_MeasureDimensionMap")
public class CTMeasureDimensionMap {

    @XmlAttribute
    @XmlSchemaType(name = "unsignedInt")
    protected Long measureGroup;
    @XmlAttribute
    @XmlSchemaType(name = "unsignedInt")
    protected Long dimension;

    /**
     * Gets the value of the measureGroup property.
     * 
     * @return
     *     possible object is
     *     {@link Long }
     *     
     */
    public Long getMeasureGroup() {
        return measureGroup;
    }

    /**
     * Sets the value of the measureGroup property.
     * 
     * @param value
     *     allowed object is
     *     {@link Long }
     *     
     */
    public void setMeasureGroup(Long value) {
        this.measureGroup = value;
    }

    /**
     * Gets the value of the dimension property.
     * 
     * @return
     *     possible object is
     *     {@link Long }
     *     
     */
    public Long getDimension() {
        return dimension;
    }

    /**
     * Sets the value of the dimension property.
     * 
     * @param value
     *     allowed object is
     *     {@link Long }
     *     
     */
    public void setDimension(Long value) {
        this.dimension = value;
    }

}
