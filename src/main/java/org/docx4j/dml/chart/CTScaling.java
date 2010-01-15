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


package org.docx4j.dml.chart;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlType;


/**
 * <p>Java class for CT_Scaling complex type.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
 * 
 * <pre>
 * &lt;complexType name="CT_Scaling">
 *   &lt;complexContent>
 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
 *       &lt;sequence>
 *         &lt;element name="logBase" type="{http://schemas.openxmlformats.org/drawingml/2006/chart}CT_LogBase" minOccurs="0"/>
 *         &lt;element name="orientation" type="{http://schemas.openxmlformats.org/drawingml/2006/chart}CT_Orientation" minOccurs="0"/>
 *         &lt;element name="max" type="{http://schemas.openxmlformats.org/drawingml/2006/chart}CT_Double" minOccurs="0"/>
 *         &lt;element name="min" type="{http://schemas.openxmlformats.org/drawingml/2006/chart}CT_Double" minOccurs="0"/>
 *         &lt;element name="extLst" type="{http://schemas.openxmlformats.org/drawingml/2006/chart}CT_ExtensionList" minOccurs="0"/>
 *       &lt;/sequence>
 *     &lt;/restriction>
 *   &lt;/complexContent>
 * &lt;/complexType>
 * </pre>
 * 
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "CT_Scaling", propOrder = {
    "logBase",
    "orientation",
    "max",
    "min",
    "extLst"
})
public class CTScaling {

    protected CTLogBase logBase;
    protected CTOrientation orientation;
    protected CTDouble max;
    protected CTDouble min;
    protected CTExtensionList extLst;

    /**
     * Gets the value of the logBase property.
     * 
     * @return
     *     possible object is
     *     {@link CTLogBase }
     *     
     */
    public CTLogBase getLogBase() {
        return logBase;
    }

    /**
     * Sets the value of the logBase property.
     * 
     * @param value
     *     allowed object is
     *     {@link CTLogBase }
     *     
     */
    public void setLogBase(CTLogBase value) {
        this.logBase = value;
    }

    /**
     * Gets the value of the orientation property.
     * 
     * @return
     *     possible object is
     *     {@link CTOrientation }
     *     
     */
    public CTOrientation getOrientation() {
        return orientation;
    }

    /**
     * Sets the value of the orientation property.
     * 
     * @param value
     *     allowed object is
     *     {@link CTOrientation }
     *     
     */
    public void setOrientation(CTOrientation value) {
        this.orientation = value;
    }

    /**
     * Gets the value of the max property.
     * 
     * @return
     *     possible object is
     *     {@link CTDouble }
     *     
     */
    public CTDouble getMax() {
        return max;
    }

    /**
     * Sets the value of the max property.
     * 
     * @param value
     *     allowed object is
     *     {@link CTDouble }
     *     
     */
    public void setMax(CTDouble value) {
        this.max = value;
    }

    /**
     * Gets the value of the min property.
     * 
     * @return
     *     possible object is
     *     {@link CTDouble }
     *     
     */
    public CTDouble getMin() {
        return min;
    }

    /**
     * Sets the value of the min property.
     * 
     * @param value
     *     allowed object is
     *     {@link CTDouble }
     *     
     */
    public void setMin(CTDouble value) {
        this.min = value;
    }

    /**
     * Gets the value of the extLst property.
     * 
     * @return
     *     possible object is
     *     {@link CTExtensionList }
     *     
     */
    public CTExtensionList getExtLst() {
        return extLst;
    }

    /**
     * Sets the value of the extLst property.
     * 
     * @param value
     *     allowed object is
     *     {@link CTExtensionList }
     *     
     */
    public void setExtLst(CTExtensionList value) {
        this.extLst = value;
    }

}
