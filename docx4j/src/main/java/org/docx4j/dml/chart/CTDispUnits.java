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
 * <p>Java class for CT_DispUnits complex type.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
 * 
 * <pre>
 * &lt;complexType name="CT_DispUnits">
 *   &lt;complexContent>
 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
 *       &lt;sequence>
 *         &lt;choice>
 *           &lt;element name="custUnit" type="{http://schemas.openxmlformats.org/drawingml/2006/chart}CT_Double"/>
 *           &lt;element name="builtInUnit" type="{http://schemas.openxmlformats.org/drawingml/2006/chart}CT_BuiltInUnit"/>
 *         &lt;/choice>
 *         &lt;element name="dispUnitsLbl" type="{http://schemas.openxmlformats.org/drawingml/2006/chart}CT_DispUnitsLbl" minOccurs="0"/>
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
@XmlType(name = "CT_DispUnits", propOrder = {
    "custUnit",
    "builtInUnit",
    "dispUnitsLbl",
    "extLst"
})
public class CTDispUnits {

    protected CTDouble custUnit;
    protected CTBuiltInUnit builtInUnit;
    protected CTDispUnitsLbl dispUnitsLbl;
    protected CTExtensionList extLst;

    /**
     * Gets the value of the custUnit property.
     * 
     * @return
     *     possible object is
     *     {@link CTDouble }
     *     
     */
    public CTDouble getCustUnit() {
        return custUnit;
    }

    /**
     * Sets the value of the custUnit property.
     * 
     * @param value
     *     allowed object is
     *     {@link CTDouble }
     *     
     */
    public void setCustUnit(CTDouble value) {
        this.custUnit = value;
    }

    /**
     * Gets the value of the builtInUnit property.
     * 
     * @return
     *     possible object is
     *     {@link CTBuiltInUnit }
     *     
     */
    public CTBuiltInUnit getBuiltInUnit() {
        return builtInUnit;
    }

    /**
     * Sets the value of the builtInUnit property.
     * 
     * @param value
     *     allowed object is
     *     {@link CTBuiltInUnit }
     *     
     */
    public void setBuiltInUnit(CTBuiltInUnit value) {
        this.builtInUnit = value;
    }

    /**
     * Gets the value of the dispUnitsLbl property.
     * 
     * @return
     *     possible object is
     *     {@link CTDispUnitsLbl }
     *     
     */
    public CTDispUnitsLbl getDispUnitsLbl() {
        return dispUnitsLbl;
    }

    /**
     * Sets the value of the dispUnitsLbl property.
     * 
     * @param value
     *     allowed object is
     *     {@link CTDispUnitsLbl }
     *     
     */
    public void setDispUnitsLbl(CTDispUnitsLbl value) {
        this.dispUnitsLbl = value;
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
