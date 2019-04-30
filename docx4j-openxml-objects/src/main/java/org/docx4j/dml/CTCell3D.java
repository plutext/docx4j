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

import javax.xml.bind.Unmarshaller;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlTransient;
import javax.xml.bind.annotation.XmlType;
import org.jvnet.jaxb2_commons.ppp.Child;


/**
 * <p>Java class for CT_Cell3D complex type.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
 * 
 * <pre>
 * &lt;complexType name="CT_Cell3D"&gt;
 *   &lt;complexContent&gt;
 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType"&gt;
 *       &lt;sequence&gt;
 *         &lt;element name="bevel" type="{http://schemas.openxmlformats.org/drawingml/2006/main}CT_Bevel"/&gt;
 *         &lt;element name="lightRig" type="{http://schemas.openxmlformats.org/drawingml/2006/main}CT_LightRig" minOccurs="0"/&gt;
 *         &lt;element name="extLst" type="{http://schemas.openxmlformats.org/drawingml/2006/main}CT_OfficeArtExtensionList" minOccurs="0"/&gt;
 *       &lt;/sequence&gt;
 *       &lt;attribute name="prstMaterial" type="{http://schemas.openxmlformats.org/drawingml/2006/main}ST_PresetMaterialType" default="plastic" /&gt;
 *     &lt;/restriction&gt;
 *   &lt;/complexContent&gt;
 * &lt;/complexType&gt;
 * </pre>
 * 
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "CT_Cell3D", propOrder = {
    "bevel",
    "lightRig",
    "extLst"
})
public class CTCell3D implements Child
{

    @XmlElement(required = true)
    protected CTBevel bevel;
    protected CTLightRig lightRig;
    protected CTOfficeArtExtensionList extLst;
    @XmlAttribute(name = "prstMaterial")
    protected STPresetMaterialType prstMaterial;
    @XmlTransient
    private Object parent;

    /**
     * Gets the value of the bevel property.
     * 
     * @return
     *     possible object is
     *     {@link CTBevel }
     *     
     */
    public CTBevel getBevel() {
        return bevel;
    }

    /**
     * Sets the value of the bevel property.
     * 
     * @param value
     *     allowed object is
     *     {@link CTBevel }
     *     
     */
    public void setBevel(CTBevel value) {
        this.bevel = value;
    }

    /**
     * Gets the value of the lightRig property.
     * 
     * @return
     *     possible object is
     *     {@link CTLightRig }
     *     
     */
    public CTLightRig getLightRig() {
        return lightRig;
    }

    /**
     * Sets the value of the lightRig property.
     * 
     * @param value
     *     allowed object is
     *     {@link CTLightRig }
     *     
     */
    public void setLightRig(CTLightRig value) {
        this.lightRig = value;
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
     * Gets the value of the prstMaterial property.
     * 
     * @return
     *     possible object is
     *     {@link STPresetMaterialType }
     *     
     */
    public STPresetMaterialType getPrstMaterial() {
        if (prstMaterial == null) {
            return STPresetMaterialType.PLASTIC;
        } else {
            return prstMaterial;
        }
    }

    /**
     * Sets the value of the prstMaterial property.
     * 
     * @param value
     *     allowed object is
     *     {@link STPresetMaterialType }
     *     
     */
    public void setPrstMaterial(STPresetMaterialType value) {
        this.prstMaterial = value;
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
