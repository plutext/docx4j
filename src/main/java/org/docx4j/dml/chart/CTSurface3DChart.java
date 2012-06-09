/*
 * Copyright 2012 Plutext Pty Ltd.
 * 
 * This file is part of docx4j.
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
 
package org.docx4j.dml.chart;

import java.util.ArrayList;
import java.util.List;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlType;


/**
 * <p>Java class for CT_Surface3DChart complex type.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
 * 
 * <pre>
 * &lt;complexType name="CT_Surface3DChart">
 *   &lt;complexContent>
 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
 *       &lt;sequence>
 *         &lt;group ref="{http://schemas.openxmlformats.org/drawingml/2006/chart}EG_SurfaceChartShared"/>
 *         &lt;element name="axId" type="{http://schemas.openxmlformats.org/drawingml/2006/chart}CT_UnsignedInt" maxOccurs="3" minOccurs="3"/>
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
@XmlType(name = "CT_Surface3DChart", propOrder = {
    "wireframe",
    "ser",
    "bandFmts",
    "axId",
    "extLst"
})
public class CTSurface3DChart implements ListSer {

    protected CTBoolean wireframe;
    protected List<CTSurfaceSer> ser;
    protected CTBandFmts bandFmts;
    @XmlElement(required = true)
    protected List<CTUnsignedInt> axId;
    protected CTExtensionList extLst;

    /**
     * Gets the value of the wireframe property.
     * 
     * @return
     *     possible object is
     *     {@link CTBoolean }
     *     
     */
    public CTBoolean getWireframe() {
        return wireframe;
    }

    /**
     * Sets the value of the wireframe property.
     * 
     * @param value
     *     allowed object is
     *     {@link CTBoolean }
     *     
     */
    public void setWireframe(CTBoolean value) {
        this.wireframe = value;
    }

    /**
     * Gets the value of the ser property.
     * 
     * <p>
     * This accessor method returns a reference to the live list,
     * not a snapshot. Therefore any modification you make to the
     * returned list will be present inside the JAXB object.
     * This is why there is not a <CODE>set</CODE> method for the ser property.
     * 
     * <p>
     * For example, to add a new item, do as follows:
     * <pre>
     *    getSer().add(newItem);
     * </pre>
     * 
     * 
     * <p>
     * Objects of the following type(s) are allowed in the list
     * {@link CTSurfaceSer }
     * 
     * 
     */
    public List<CTSurfaceSer> getSer() {
        if (ser == null) {
            ser = new ArrayList<CTSurfaceSer>();
        }
        return this.ser;
    }

    /**
     * Gets the value of the bandFmts property.
     * 
     * @return
     *     possible object is
     *     {@link CTBandFmts }
     *     
     */
    public CTBandFmts getBandFmts() {
        return bandFmts;
    }

    /**
     * Sets the value of the bandFmts property.
     * 
     * @param value
     *     allowed object is
     *     {@link CTBandFmts }
     *     
     */
    public void setBandFmts(CTBandFmts value) {
        this.bandFmts = value;
    }

    /**
     * Gets the value of the axId property.
     * 
     * <p>
     * This accessor method returns a reference to the live list,
     * not a snapshot. Therefore any modification you make to the
     * returned list will be present inside the JAXB object.
     * This is why there is not a <CODE>set</CODE> method for the axId property.
     * 
     * <p>
     * For example, to add a new item, do as follows:
     * <pre>
     *    getAxId().add(newItem);
     * </pre>
     * 
     * 
     * <p>
     * Objects of the following type(s) are allowed in the list
     * {@link CTUnsignedInt }
     * 
     * 
     */
    public List<CTUnsignedInt> getAxId() {
        if (axId == null) {
            axId = new ArrayList<CTUnsignedInt>();
        }
        return this.axId;
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
