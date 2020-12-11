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

import javax.xml.bind.Unmarshaller;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlTransient;
import javax.xml.bind.annotation.XmlType;
import org.docx4j.dml.CTOfficeArtExtensionList;
import org.jvnet.jaxb2_commons.ppp.Child;


/**
 * <p>Java class for CT_Shape complex type.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
 * 
 * <pre>
 * &lt;complexType name="CT_Shape"&gt;
 *   &lt;complexContent&gt;
 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType"&gt;
 *       &lt;sequence&gt;
 *         &lt;element name="adjLst" type="{http://schemas.openxmlformats.org/drawingml/2006/diagram}CT_AdjLst" minOccurs="0"/&gt;
 *         &lt;element name="extLst" type="{http://schemas.openxmlformats.org/drawingml/2006/main}CT_OfficeArtExtensionList" minOccurs="0"/&gt;
 *       &lt;/sequence&gt;
 *       &lt;attribute name="rot" type="{http://www.w3.org/2001/XMLSchema}double" default="0" /&gt;
 *       &lt;attribute name="type" type="{http://schemas.openxmlformats.org/drawingml/2006/diagram}ST_LayoutShapeType" default="none" /&gt;
 *       &lt;attribute ref="{http://schemas.openxmlformats.org/officeDocument/2006/relationships}blip"/&gt;
 *       &lt;attribute name="zOrderOff" type="{http://www.w3.org/2001/XMLSchema}int" default="0" /&gt;
 *       &lt;attribute name="hideGeom" type="{http://www.w3.org/2001/XMLSchema}boolean" default="false" /&gt;
 *       &lt;attribute name="lkTxEntry" type="{http://www.w3.org/2001/XMLSchema}boolean" default="false" /&gt;
 *       &lt;attribute name="blipPhldr" type="{http://www.w3.org/2001/XMLSchema}boolean" default="false" /&gt;
 *     &lt;/restriction&gt;
 *   &lt;/complexContent&gt;
 * &lt;/complexType&gt;
 * </pre>
 * 
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "CT_Shape", propOrder = {
    "adjLst",
    "extLst"
})
public class CTShape implements Child
{

    protected CTAdjLst adjLst;
    protected CTOfficeArtExtensionList extLst;
    @XmlAttribute(name = "rot")
    protected Double rot;
    @XmlAttribute(name = "type")
    protected String type;
    @XmlAttribute(name = "blip", namespace = "http://schemas.openxmlformats.org/officeDocument/2006/relationships")
    protected String blip;
    @XmlAttribute(name = "zOrderOff")
    protected Integer zOrderOff;
    @XmlAttribute(name = "hideGeom")
    protected Boolean hideGeom;
    @XmlAttribute(name = "lkTxEntry")
    protected Boolean lkTxEntry;
    @XmlAttribute(name = "blipPhldr")
    protected Boolean blipPhldr;
    @XmlTransient
    private Object parent;

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

    /**
     * Gets the parent object in the object tree representing the unmarshalled xml document.
     * 
     * @return
     *     The parent object.
     */
    public Object getParent() {
        return this.parent;
    }

    public void setParent(Object parent) {
        this.parent = parent;
    }

    /**
     * This method is invoked by the JAXB implementation on each instance when unmarshalling completes.
     * 
     * @param parent
     *     The parent object in the object tree.
     * @param unmarshaller
     *     The unmarshaller that generated the instance.
     */
    public void afterUnmarshal(Unmarshaller unmarshaller, Object parent) {
        setParent(parent);
    }

}
