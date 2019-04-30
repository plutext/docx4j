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


package org.docx4j.dml.chartDrawing;

import javax.xml.bind.Unmarshaller;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlTransient;
import javax.xml.bind.annotation.XmlType;
import org.docx4j.dml.CTPositiveSize2D;
import org.jvnet.jaxb2_commons.ppp.Child;


/**
 * <p>Java class for CT_AbsSizeAnchor complex type.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
 * 
 * <pre>
 * &lt;complexType name="CT_AbsSizeAnchor"&gt;
 *   &lt;complexContent&gt;
 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType"&gt;
 *       &lt;sequence&gt;
 *         &lt;element name="from" type="{http://schemas.openxmlformats.org/drawingml/2006/chartDrawing}CT_Marker"/&gt;
 *         &lt;element name="ext" type="{http://schemas.openxmlformats.org/drawingml/2006/main}CT_PositiveSize2D"/&gt;
 *         &lt;group ref="{http://schemas.openxmlformats.org/drawingml/2006/chartDrawing}EG_ObjectChoices"/&gt;
 *       &lt;/sequence&gt;
 *     &lt;/restriction&gt;
 *   &lt;/complexContent&gt;
 * &lt;/complexType&gt;
 * </pre>
 * 
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "CT_AbsSizeAnchor", propOrder = {
    "from",
    "ext",
    "sp",
    "grpSp",
    "graphicFrame",
    "cxnSp",
    "pic"
})
public class CTAbsSizeAnchor implements Child
{

    @XmlElement(required = true)
    protected CTMarker from;
    @XmlElement(required = true)
    protected CTPositiveSize2D ext;
    protected CTShape sp;
    protected CTGroupShape grpSp;
    protected CTGraphicFrame graphicFrame;
    protected CTConnector cxnSp;
    protected CTPicture pic;
    @XmlTransient
    private Object parent;

    /**
     * Gets the value of the from property.
     * 
     * @return
     *     possible object is
     *     {@link CTMarker }
     *     
     */
    public CTMarker getFrom() {
        return from;
    }

    /**
     * Sets the value of the from property.
     * 
     * @param value
     *     allowed object is
     *     {@link CTMarker }
     *     
     */
    public void setFrom(CTMarker value) {
        this.from = value;
    }

    /**
     * Gets the value of the ext property.
     * 
     * @return
     *     possible object is
     *     {@link CTPositiveSize2D }
     *     
     */
    public CTPositiveSize2D getExt() {
        return ext;
    }

    /**
     * Sets the value of the ext property.
     * 
     * @param value
     *     allowed object is
     *     {@link CTPositiveSize2D }
     *     
     */
    public void setExt(CTPositiveSize2D value) {
        this.ext = value;
    }

    /**
     * Gets the value of the sp property.
     * 
     * @return
     *     possible object is
     *     {@link CTShape }
     *     
     */
    public CTShape getSp() {
        return sp;
    }

    /**
     * Sets the value of the sp property.
     * 
     * @param value
     *     allowed object is
     *     {@link CTShape }
     *     
     */
    public void setSp(CTShape value) {
        this.sp = value;
    }

    /**
     * Gets the value of the grpSp property.
     * 
     * @return
     *     possible object is
     *     {@link CTGroupShape }
     *     
     */
    public CTGroupShape getGrpSp() {
        return grpSp;
    }

    /**
     * Sets the value of the grpSp property.
     * 
     * @param value
     *     allowed object is
     *     {@link CTGroupShape }
     *     
     */
    public void setGrpSp(CTGroupShape value) {
        this.grpSp = value;
    }

    /**
     * Gets the value of the graphicFrame property.
     * 
     * @return
     *     possible object is
     *     {@link CTGraphicFrame }
     *     
     */
    public CTGraphicFrame getGraphicFrame() {
        return graphicFrame;
    }

    /**
     * Sets the value of the graphicFrame property.
     * 
     * @param value
     *     allowed object is
     *     {@link CTGraphicFrame }
     *     
     */
    public void setGraphicFrame(CTGraphicFrame value) {
        this.graphicFrame = value;
    }

    /**
     * Gets the value of the cxnSp property.
     * 
     * @return
     *     possible object is
     *     {@link CTConnector }
     *     
     */
    public CTConnector getCxnSp() {
        return cxnSp;
    }

    /**
     * Sets the value of the cxnSp property.
     * 
     * @param value
     *     allowed object is
     *     {@link CTConnector }
     *     
     */
    public void setCxnSp(CTConnector value) {
        this.cxnSp = value;
    }

    /**
     * Gets the value of the pic property.
     * 
     * @return
     *     possible object is
     *     {@link CTPicture }
     *     
     */
    public CTPicture getPic() {
        return pic;
    }

    /**
     * Sets the value of the pic property.
     * 
     * @param value
     *     allowed object is
     *     {@link CTPicture }
     *     
     */
    public void setPic(CTPicture value) {
        this.pic = value;
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
