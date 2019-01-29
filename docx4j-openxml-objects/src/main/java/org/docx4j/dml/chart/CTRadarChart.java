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

import org.docx4j.dml.ArrayListDml;
import java.util.List;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlType;


/**
 * <p>Java class for CT_RadarChart complex type.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
 * 
 * <pre>
 * &lt;complexType name="CT_RadarChart">
 *   &lt;complexContent>
 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
 *       &lt;sequence>
 *         &lt;element name="radarStyle" type="{http://schemas.openxmlformats.org/drawingml/2006/chart}CT_RadarStyle"/>
 *         &lt;element name="varyColors" type="{http://schemas.openxmlformats.org/drawingml/2006/chart}CT_Boolean" minOccurs="0"/>
 *         &lt;element name="ser" type="{http://schemas.openxmlformats.org/drawingml/2006/chart}CT_RadarSer" maxOccurs="unbounded" minOccurs="0"/>
 *         &lt;element name="dLbls" type="{http://schemas.openxmlformats.org/drawingml/2006/chart}CT_DLbls" minOccurs="0"/>
 *         &lt;element name="axId" type="{http://schemas.openxmlformats.org/drawingml/2006/chart}CT_UnsignedInt" maxOccurs="2" minOccurs="2"/>
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
@XmlType(name = "CT_RadarChart", propOrder = {
    "radarStyle",
    "varyColors",
    "ser",
    "dLbls",
    "axId",
    "extLst"
})
public class CTRadarChart implements ListSer {

    @XmlElement(required = true)
    protected CTRadarStyle radarStyle;
    protected CTBoolean varyColors;
    protected List<CTRadarSer> ser = new ArrayListDml<CTRadarSer>(this);
    protected CTDLbls dLbls;
    @XmlElement(required = true)
    protected List<CTUnsignedInt> axId = new ArrayListDml<CTUnsignedInt>(this);
    protected CTExtensionList extLst;

    /**
     * Gets the value of the radarStyle property.
     * 
     * @return
     *     possible object is
     *     {@link CTRadarStyle }
     *     
     */
    public CTRadarStyle getRadarStyle() {
        return radarStyle;
    }

    /**
     * Sets the value of the radarStyle property.
     * 
     * @param value
     *     allowed object is
     *     {@link CTRadarStyle }
     *     
     */
    public void setRadarStyle(CTRadarStyle value) {
        this.radarStyle = value;
    }

    /**
     * Gets the value of the varyColors property.
     * 
     * @return
     *     possible object is
     *     {@link CTBoolean }
     *     
     */
    public CTBoolean getVaryColors() {
        return varyColors;
    }

    /**
     * Sets the value of the varyColors property.
     * 
     * @param value
     *     allowed object is
     *     {@link CTBoolean }
     *     
     */
    public void setVaryColors(CTBoolean value) {
        this.varyColors = value;
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
     * {@link CTRadarSer }
     * 
     * 
     */
    public List<CTRadarSer> getSer() {
        if (ser == null) {
            ser = new ArrayListDml<CTRadarSer>(this);
        }
        return this.ser;
    }

    /**
     * Gets the value of the dLbls property.
     * 
     * @return
     *     possible object is
     *     {@link CTDLbls }
     *     
     */
    public CTDLbls getDLbls() {
        return dLbls;
    }

    /**
     * Sets the value of the dLbls property.
     * 
     * @param value
     *     allowed object is
     *     {@link CTDLbls }
     *     
     */
    public void setDLbls(CTDLbls value) {
        this.dLbls = value;
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
            axId = new ArrayListDml<CTUnsignedInt>(this);
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
