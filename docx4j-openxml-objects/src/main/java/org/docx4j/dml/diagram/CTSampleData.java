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


package org.docx4j.dml.diagram;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlType;


/**
 * <p>Java class for CT_SampleData complex type.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
 * 
 * <pre>
 * &lt;complexType name="CT_SampleData">
 *   &lt;complexContent>
 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
 *       &lt;sequence>
 *         &lt;element name="dataModel" type="{http://schemas.openxmlformats.org/drawingml/2006/diagram}CT_DataModel" minOccurs="0"/>
 *       &lt;/sequence>
 *       &lt;attribute name="useDef" type="{http://www.w3.org/2001/XMLSchema}boolean" default="false" />
 *     &lt;/restriction>
 *   &lt;/complexContent>
 * &lt;/complexType>
 * </pre>
 * 
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "CT_SampleData", propOrder = {
    "dataModel"
})
public class CTSampleData {

    protected CTDataModel dataModel;
    @XmlAttribute
    protected Boolean useDef;

    /**
     * Gets the value of the dataModel property.
     * 
     * @return
     *     possible object is
     *     {@link CTDataModel }
     *     
     */
    public CTDataModel getDataModel() {
        return dataModel;
    }

    /**
     * Sets the value of the dataModel property.
     * 
     * @param value
     *     allowed object is
     *     {@link CTDataModel }
     *     
     */
    public void setDataModel(CTDataModel value) {
        this.dataModel = value;
    }

    /**
     * Gets the value of the useDef property.
     * 
     * @return
     *     possible object is
     *     {@link Boolean }
     *     
     */
    public boolean isUseDef() {
        if (useDef == null) {
            return false;
        } else {
            return useDef;
        }
    }

    /**
     * Sets the value of the useDef property.
     * 
     * @param value
     *     allowed object is
     *     {@link Boolean }
     *     
     */
    public void setUseDef(Boolean value) {
        this.useDef = value;
    }

}
