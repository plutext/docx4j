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


package org.docx4j.dml.spreadsheetdrawing;

import javax.xml.bind.Unmarshaller;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlTransient;
import javax.xml.bind.annotation.XmlType;
import org.jvnet.jaxb2_commons.ppp.Child;


/**
 * <p>Java class for CT_TwoCellAnchor complex type.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
 * 
 * <pre>
 * &lt;complexType name="CT_TwoCellAnchor"&gt;
 *   &lt;complexContent&gt;
 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType"&gt;
 *       &lt;sequence&gt;
 *         &lt;element name="from" type="{http://schemas.openxmlformats.org/drawingml/2006/spreadsheetDrawing}CT_Marker"/&gt;
 *         &lt;element name="to" type="{http://schemas.openxmlformats.org/drawingml/2006/spreadsheetDrawing}CT_Marker"/&gt;
 *         &lt;group ref="{http://schemas.openxmlformats.org/drawingml/2006/spreadsheetDrawing}EG_ObjectChoices"/&gt;
 *         &lt;element name="clientData" type="{http://schemas.openxmlformats.org/drawingml/2006/spreadsheetDrawing}CT_AnchorClientData"/&gt;
 *       &lt;/sequence&gt;
 *       &lt;attribute name="editAs" type="{http://schemas.openxmlformats.org/drawingml/2006/spreadsheetDrawing}ST_EditAs" default="twoCell" /&gt;
 *     &lt;/restriction&gt;
 *   &lt;/complexContent&gt;
 * &lt;/complexType&gt;
 * </pre>
 * 
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "CT_TwoCellAnchor", propOrder = {
    "from",
    "to",
    "sp",
    "grpSp",
    "graphicFrame",
    "cxnSp",
    "pic",
    "clientData"
})
public class CTTwoCellAnchor implements Child
{

    @XmlElement(required = true)
    protected CTMarker from;
    @XmlElement(required = true)
    protected CTMarker to;
    protected CTShape sp;
    protected CTGroupShape grpSp;
    protected CTGraphicalObjectFrame graphicFrame;
    protected CTConnector cxnSp;
    protected CTPicture pic;
    @XmlElement(required = true)
    protected CTAnchorClientData clientData;
    @XmlAttribute(name = "editAs")
    protected STEditAs editAs;
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
     * Gets the value of the to property.
     * 
     * @return
     *     possible object is
     *     {@link CTMarker }
     *     
     */
    public CTMarker getTo() {
        return to;
    }

    /**
     * Sets the value of the to property.
     * 
     * @param value
     *     allowed object is
     *     {@link CTMarker }
     *     
     */
    public void setTo(CTMarker value) {
        this.to = value;
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
     *     {@link CTGraphicalObjectFrame }
     *     
     */
    public CTGraphicalObjectFrame getGraphicFrame() {
        return graphicFrame;
    }

    /**
     * Sets the value of the graphicFrame property.
     * 
     * @param value
     *     allowed object is
     *     {@link CTGraphicalObjectFrame }
     *     
     */
    public void setGraphicFrame(CTGraphicalObjectFrame value) {
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
     * Gets the value of the clientData property.
     * 
     * @return
     *     possible object is
     *     {@link CTAnchorClientData }
     *     
     */
    public CTAnchorClientData getClientData() {
        return clientData;
    }

    /**
     * Sets the value of the clientData property.
     * 
     * @param value
     *     allowed object is
     *     {@link CTAnchorClientData }
     *     
     */
    public void setClientData(CTAnchorClientData value) {
        this.clientData = value;
    }

    /**
     * Gets the value of the editAs property.
     * 
     * @return
     *     possible object is
     *     {@link STEditAs }
     *     
     */
    public STEditAs getEditAs() {
        if (editAs == null) {
            return STEditAs.TWO_CELL;
        } else {
            return editAs;
        }
    }

    /**
     * Sets the value of the editAs property.
     * 
     * @param value
     *     allowed object is
     *     {@link STEditAs }
     *     
     */
    public void setEditAs(STEditAs value) {
        this.editAs = value;
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
