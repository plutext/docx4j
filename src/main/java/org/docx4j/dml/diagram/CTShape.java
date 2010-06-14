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
import org.docx4j.dml.CTOfficeArtExtensionList;


/**
 * <p>Java class for CT_Shape complex type.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
 * 
 * <pre>
 * &lt;complexType name="CT_Shape">
 *   &lt;complexContent>
 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
 *       &lt;sequence>
 *         &lt;element name="adjLst" type="{http://schemas.openxmlformats.org/drawingml/2006/diagram}CT_AdjLst" minOccurs="0"/>
 *         &lt;element name="extLst" type="{http://schemas.openxmlformats.org/drawingml/2006/main}CT_OfficeArtExtensionList" minOccurs="0"/>
 *       &lt;/sequence>
 *       &lt;attribute name="rot" type="{http://www.w3.org/2001/XMLSchema}double" default="0" />
 *       &lt;attribute name="type" type="{http://schemas.openxmlformats.org/drawingml/2006/diagram}ST_LayoutShapeType" default="none" />
 *       &lt;attribute ref="{http://schemas.openxmlformats.org/officeDocument/2006/relationships}blip"/>
 *       &lt;attribute name="zOrderOff" type="{http://www.w3.org/2001/XMLSchema}int" default="0" />
 *       &lt;attribute name="hideGeom" type="{http://www.w3.org/2001/XMLSchema}boolean" default="false" />
 *       &lt;attribute name="lkTxEntry" type="{http://www.w3.org/2001/XMLSchema}boolean" default="false" />
 *       &lt;attribute name="blipPhldr" type="{http://www.w3.org/2001/XMLSchema}boolean" default="false" />
 *     &lt;/restriction>
 *   &lt;/complexContent>
 * &lt;/complexType>
 * </pre>
 * 
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(namespace="http://schemas.openxmlformats.org/drawingml/2006/diagram", name = "CT_Shape", propOrder = {
    "adjLst",
    "extLst"
})
public class CTShape {

    protected CTAdjLst adjLst;
    protected CTOfficeArtExtensionList extLst;
    @XmlAttribute
    protected Double rot;
    @XmlAttribute
    protected String type;
    @XmlAttribute(namespace = "http://schemas.openxmlformats.org/officeDocument/2006/relationships")
    protected String blip;
    @XmlAttribute
    protected Integer zOrderOff;
    @XmlAttribute
    protected Boolean hideGeom;
    @XmlAttribute
    protected Boolean lkTxEntry;
    @XmlAttribute
    protected Boolean blipPhldr;

    /**
     * Gets the value of the adjLst property.
     * 
     * @return
     *     possible object is
     *     {@link CTAdjLst }
     *     
     */
    public CTAdjLst getAdjLst() {
        return adjLst;
    }

    /**
     * Sets the value of the adjLst property.
     * 
     * @param value
     *     allowed object is
     *     {@link CTAdjLst }
     *     
     */
    public void setAdjLst(CTAdjLst value) {
        this.adjLst = value;
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

    /**
     * Gets the value of the rot property.
     * 
     * @return
     *     possible object is
     *     {@link Double }
     *     
     */
    public double getRot() {
        if (rot == null) {
            return  0.0D;
        } else {
            return rot;
        }
    }

    /**
     * Sets the value of the rot property.
     * 
     * @param value
     *     allowed object is
     *     {@link Double }
     *     
     */
    public void setRot(Double value) {
        this.rot = value;
    }

    /**
     * Gets the value of the type property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getType() {
        if (type == null) {
            return "none";
        } else {
            return type;
        }
    }

    /**
     * Sets the value of the type property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setType(String value) {
        this.type = value;
    }

    /**
     * 
     * 					Relationship to Image Part
     * 				
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getBlip() {
        if (blip == null) {
            return "";
        } else {
            return blip;
        }
    }

    /**
     * Sets the value of the blip property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setBlip(String value) {
        this.blip = value;
    }

    /**
     * Gets the value of the zOrderOff property.
     * 
     * @return
     *     possible object is
     *     {@link Integer }
     *     
     */
    public int getZOrderOff() {
        if (zOrderOff == null) {
            return  0;
        } else {
            return zOrderOff;
        }
    }

    /**
     * Sets the value of the zOrderOff property.
     * 
     * @param value
     *     allowed object is
     *     {@link Integer }
     *     
     */
    public void setZOrderOff(Integer value) {
        this.zOrderOff = value;
    }

    /**
     * Gets the value of the hideGeom property.
     * 
     * @return
     *     possible object is
     *     {@link Boolean }
     *     
     */
    public boolean isHideGeom() {
        if (hideGeom == null) {
            return false;
        } else {
            return hideGeom;
        }
    }

    /**
     * Sets the value of the hideGeom property.
     * 
     * @param value
     *     allowed object is
     *     {@link Boolean }
     *     
     */
    public void setHideGeom(Boolean value) {
        this.hideGeom = value;
    }

    /**
     * Gets the value of the lkTxEntry property.
     * 
     * @return
     *     possible object is
     *     {@link Boolean }
     *     
     */
    public boolean isLkTxEntry() {
        if (lkTxEntry == null) {
            return false;
        } else {
            return lkTxEntry;
        }
    }

    /**
     * Sets the value of the lkTxEntry property.
     * 
     * @param value
     *     allowed object is
     *     {@link Boolean }
     *     
     */
    public void setLkTxEntry(Boolean value) {
        this.lkTxEntry = value;
    }

    /**
     * Gets the value of the blipPhldr property.
     * 
     * @return
     *     possible object is
     *     {@link Boolean }
     *     
     */
    public boolean isBlipPhldr() {
        if (blipPhldr == null) {
            return false;
        } else {
            return blipPhldr;
        }
    }

    /**
     * Sets the value of the blipPhldr property.
     * 
     * @param value
     *     allowed object is
     *     {@link Boolean }
     *     
     */
    public void setBlipPhldr(Boolean value) {
        this.blipPhldr = value;
    }

}
