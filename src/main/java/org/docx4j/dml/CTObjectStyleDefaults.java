/*
 *  Copyright 2007, Plutext Pty Ltd.
 *   
 *  This file is part of docx4j.

    docx4j is free software: you can use it, redistribute it and/or modify
    it under the terms of version 3 of the GNU Affero General Public License 
    as published by the Free Software Foundation.

    If you need the right to use it under a different license, please
    contact Plutext.

    docx4j is distributed in the hope that it will be useful,
    but WITHOUT ANY WARRANTY; without even the implied warranty of
    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
    GNU Affero General Public License for more details.

    You should have received a copy of the GNU Affero General Public License   
    along with docx4j.  If not, see <http://www.fsf.org/licensing/licenses/>.
    
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
