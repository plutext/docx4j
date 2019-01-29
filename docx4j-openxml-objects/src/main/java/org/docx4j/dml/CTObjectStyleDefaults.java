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

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlType;


/**
 * <p>Java class for CT_ObjectStyleDefaults complex type.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
 * 
 * <pre>
 * &lt;complexType name="CT_ObjectStyleDefaults">
 *   &lt;complexContent>
 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
 *       &lt;sequence>
 *         &lt;element name="spDef" type="{http://schemas.openxmlformats.org/drawingml/2006/main}CT_DefaultShapeDefinition" minOccurs="0"/>
 *         &lt;element name="lnDef" type="{http://schemas.openxmlformats.org/drawingml/2006/main}CT_DefaultShapeDefinition" minOccurs="0"/>
 *         &lt;element name="txDef" type="{http://schemas.openxmlformats.org/drawingml/2006/main}CT_DefaultShapeDefinition" minOccurs="0"/>
 *         &lt;element name="extLst" type="{http://schemas.openxmlformats.org/drawingml/2006/main}CT_OfficeArtExtensionList" minOccurs="0"/>
 *       &lt;/sequence>
 *     &lt;/restriction>
 *   &lt;/complexContent>
 * &lt;/complexType>
 * </pre>
 * 
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "CT_ObjectStyleDefaults", propOrder = {
    "spDef",
    "lnDef",
    "txDef",
    "extLst"
})
public class CTObjectStyleDefaults {

    protected CTDefaultShapeDefinition spDef;
    protected CTDefaultShapeDefinition lnDef;
    protected CTDefaultShapeDefinition txDef;
    protected CTOfficeArtExtensionList extLst;

    /**
     * Gets the value of the spDef property.
     * 
     * @return
     *     possible object is
     *     {@link CTDefaultShapeDefinition }
     *     
     */
    public CTDefaultShapeDefinition getSpDef() {
        return spDef;
    }

    /**
     * Sets the value of the spDef property.
     * 
     * @param value
     *     allowed object is
     *     {@link CTDefaultShapeDefinition }
     *     
     */
    public void setSpDef(CTDefaultShapeDefinition value) {
        this.spDef = value;
    }

    /**
     * Gets the value of the lnDef property.
     * 
     * @return
     *     possible object is
     *     {@link CTDefaultShapeDefinition }
     *     
     */
    public CTDefaultShapeDefinition getLnDef() {
        return lnDef;
    }

    /**
     * Sets the value of the lnDef property.
     * 
     * @param value
     *     allowed object is
     *     {@link CTDefaultShapeDefinition }
     *     
     */
    public void setLnDef(CTDefaultShapeDefinition value) {
        this.lnDef = value;
    }

    /**
     * Gets the value of the txDef property.
     * 
     * @return
     *     possible object is
     *     {@link CTDefaultShapeDefinition }
     *     
     */
    public CTDefaultShapeDefinition getTxDef() {
        return txDef;
    }

    /**
     * Sets the value of the txDef property.
     * 
     * @param value
     *     allowed object is
     *     {@link CTDefaultShapeDefinition }
     *     
     */
    public void setTxDef(CTDefaultShapeDefinition value) {
        this.txDef = value;
    }

    /**
     * Gets the value of the extLst property.
     * 
     * @return
     *     possible object is
     *     {@link CTOfficeArtExtensionList }
     *     
     */
    public CTOfficeArtExtensionList getExtLst() {
        return extLst;
    }

    /**
     * Sets the value of the extLst property.
     * 
     * @param value
     *     allowed object is
     *     {@link CTOfficeArtExtensionList }
     *     
     */
    public void setExtLst(CTOfficeArtExtensionList value) {
        this.extLst = value;
    }

}
