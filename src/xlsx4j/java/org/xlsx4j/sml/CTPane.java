/*
 *  Copyright 2010-2013, Plutext Pty Ltd.
 *   
 *  This file is part of xlsx4j, a component of docx4j.

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
package org.xlsx4j.sml;

import javax.xml.bind.Unmarshaller;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlTransient;
import javax.xml.bind.annotation.XmlType;
import org.jvnet.jaxb2_commons.ppp.Child;


/**
 * <p>Java class for CT_Pane complex type.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
 * 
 * <pre>
 * &lt;complexType name="CT_Pane">
 *   &lt;complexContent>
 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
 *       &lt;attribute name="xSplit" type="{http://www.w3.org/2001/XMLSchema}double" default="0" />
 *       &lt;attribute name="ySplit" type="{http://www.w3.org/2001/XMLSchema}double" default="0" />
 *       &lt;attribute name="topLeftCell" type="{http://schemas.openxmlformats.org/spreadsheetml/2006/main}ST_CellRef" />
 *       &lt;attribute name="activePane" type="{http://schemas.openxmlformats.org/spreadsheetml/2006/main}ST_Pane" default="topLeft" />
 *       &lt;attribute name="state" type="{http://schemas.openxmlformats.org/spreadsheetml/2006/main}ST_PaneState" default="split" />
 *     &lt;/restriction>
 *   &lt;/complexContent>
 * &lt;/complexType>
 * </pre>
 * 
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "CT_Pane")
public class CTPane implements Child
{

    @XmlAttribute(name = "xSplit")
    protected Double xSplit;
    @XmlAttribute(name = "ySplit")
    protected Double ySplit;
    @XmlAttribute(name = "topLeftCell")
    protected String topLeftCell;
    @XmlAttribute(name = "activePane")
    protected STPane activePane;
    @XmlAttribute(name = "state")
    protected STPaneState state;
    @XmlTransient
    private Object parent;

    /**
     * Gets the value of the xSplit property.
     * 
     * @return
     *     possible object is
     *     {@link Double }
     *     
     */
    public double getXSplit() {
        if (xSplit == null) {
            return  0.0D;
        } else {
            return xSplit;
        }
    }

    /**
     * Sets the value of the xSplit property.
     * 
     * @param value
     *     allowed object is
     *     {@link Double }
     *     
     */
    public void setXSplit(Double value) {
        this.xSplit = value;
    }

    /**
     * Gets the value of the ySplit property.
     * 
     * @return
     *     possible object is
     *     {@link Double }
     *     
     */
    public double getYSplit() {
        if (ySplit == null) {
            return  0.0D;
        } else {
            return ySplit;
        }
    }

    /**
     * Sets the value of the ySplit property.
     * 
     * @param value
     *     allowed object is
     *     {@link Double }
     *     
     */
    public void setYSplit(Double value) {
        this.ySplit = value;
    }

    /**
     * Gets the value of the topLeftCell property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getTopLeftCell() {
        return topLeftCell;
    }

    /**
     * Sets the value of the topLeftCell property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setTopLeftCell(String value) {
        this.topLeftCell = value;
    }

    /**
     * Gets the value of the activePane property.
     * 
     * @return
     *     possible object is
     *     {@link STPane }
     *     
     */
    public STPane getActivePane() {
        if (activePane == null) {
            return STPane.TOP_LEFT;
        } else {
            return activePane;
        }
    }

    /**
     * Sets the value of the activePane property.
     * 
     * @param value
     *     allowed object is
     *     {@link STPane }
     *     
     */
    public void setActivePane(STPane value) {
        this.activePane = value;
    }

    /**
     * Gets the value of the state property.
     * 
     * @return
     *     possible object is
     *     {@link STPaneState }
     *     
     */
    public STPaneState getState() {
        if (state == null) {
            return STPaneState.SPLIT;
        } else {
            return state;
        }
    }

    /**
     * Sets the value of the state property.
     * 
     * @param value
     *     allowed object is
     *     {@link STPaneState }
     *     
     */
    public void setState(STPaneState value) {
        this.state = value;
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
