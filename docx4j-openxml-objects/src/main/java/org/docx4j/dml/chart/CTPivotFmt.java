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

import javax.xml.bind.Unmarshaller;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlTransient;
import javax.xml.bind.annotation.XmlType;
import org.docx4j.dml.CTShapeProperties;
import org.docx4j.dml.CTTextBody;
import org.jvnet.jaxb2_commons.ppp.Child;


/**
 * <p>Java class for CT_PivotFmt complex type.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
 * 
 * <pre>
 * &lt;complexType name="CT_PivotFmt"&gt;
 *   &lt;complexContent&gt;
 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType"&gt;
 *       &lt;sequence&gt;
 *         &lt;element name="idx" type="{http://schemas.openxmlformats.org/drawingml/2006/chart}CT_UnsignedInt"/&gt;
 *         &lt;element name="spPr" type="{http://schemas.openxmlformats.org/drawingml/2006/main}CT_ShapeProperties" minOccurs="0"/&gt;
 *         &lt;element name="txPr" type="{http://schemas.openxmlformats.org/drawingml/2006/main}CT_TextBody" minOccurs="0"/&gt;
 *         &lt;element name="marker" type="{http://schemas.openxmlformats.org/drawingml/2006/chart}CT_Marker" minOccurs="0"/&gt;
 *         &lt;element name="dLbl" type="{http://schemas.openxmlformats.org/drawingml/2006/chart}CT_DLbl" minOccurs="0"/&gt;
 *         &lt;element name="extLst" type="{http://schemas.openxmlformats.org/drawingml/2006/chart}CT_ExtensionList" minOccurs="0"/&gt;
 *       &lt;/sequence&gt;
 *     &lt;/restriction&gt;
 *   &lt;/complexContent&gt;
 * &lt;/complexType&gt;
 * </pre>
 * 
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "CT_PivotFmt", propOrder = {
    "idx",
    "spPr",
    "txPr",
    "marker",
    "dLbl",
    "extLst"
})
public class CTPivotFmt implements Child
{

    @XmlElement(required = true)
    protected CTUnsignedInt idx;
    protected CTShapeProperties spPr;
    protected CTTextBody txPr;
    protected CTMarker marker;
    protected CTDLbl dLbl;
    protected CTExtensionList extLst;
    @XmlTransient
    private Object parent;

    /**
     * Gets the value of the idx property.
     * 
     * @return
     *     possible object is
     *     {@link CTUnsignedInt }
     *     
     */
    public CTUnsignedInt getIdx() {
        return idx;
    }

    /**
     * Sets the value of the idx property.
     * 
     * @param value
     *     allowed object is
     *     {@link CTUnsignedInt }
     *     
     */
    public void setIdx(CTUnsignedInt value) {
        this.idx = value;
    }

    /**
     * Gets the value of the spPr property.
     * 
     * @return
     *     possible object is
     *     {@link CTShapeProperties }
     *     
     */
    public CTShapeProperties getSpPr() {
        return spPr;
    }

    /**
     * Sets the value of the spPr property.
     * 
     * @param value
     *     allowed object is
     *     {@link CTShapeProperties }
     *     
     */
    public void setSpPr(CTShapeProperties value) {
        this.spPr = value;
    }

    /**
     * Gets the value of the txPr property.
     * 
     * @return
     *     possible object is
     *     {@link CTTextBody }
     *     
     */
    public CTTextBody getTxPr() {
        return txPr;
    }

    /**
     * Sets the value of the txPr property.
     * 
     * @param value
     *     allowed object is
     *     {@link CTTextBody }
     *     
     */
    public void setTxPr(CTTextBody value) {
        this.txPr = value;
    }

    /**
     * Gets the value of the marker property.
     * 
     * @return
     *     possible object is
     *     {@link CTMarker }
     *     
     */
    public CTMarker getMarker() {
        return marker;
    }

    /**
     * Sets the value of the marker property.
     * 
     * @param value
     *     allowed object is
     *     {@link CTMarker }
     *     
     */
    public void setMarker(CTMarker value) {
        this.marker = value;
    }

    /**
     * Gets the value of the dLbl property.
     * 
     * @return
     *     possible object is
     *     {@link CTDLbl }
     *     
     */
    public CTDLbl getDLbl() {
        return dLbl;
    }

    /**
     * Sets the value of the dLbl property.
     * 
     * @param value
     *     allowed object is
     *     {@link CTDLbl }
     *     
     */
    public void setDLbl(CTDLbl value) {
        this.dLbl = value;
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
