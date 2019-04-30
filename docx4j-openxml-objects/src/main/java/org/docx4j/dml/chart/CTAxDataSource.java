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
import javax.xml.bind.annotation.XmlTransient;
import javax.xml.bind.annotation.XmlType;
import org.jvnet.jaxb2_commons.ppp.Child;


/**
 * <p>Java class for CT_AxDataSource complex type.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
 * 
 * <pre>
 * &lt;complexType name="CT_AxDataSource"&gt;
 *   &lt;complexContent&gt;
 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType"&gt;
 *       &lt;sequence&gt;
 *         &lt;choice&gt;
 *           &lt;element name="multiLvlStrRef" type="{http://schemas.openxmlformats.org/drawingml/2006/chart}CT_MultiLvlStrRef"/&gt;
 *           &lt;element name="numRef" type="{http://schemas.openxmlformats.org/drawingml/2006/chart}CT_NumRef"/&gt;
 *           &lt;element name="numLit" type="{http://schemas.openxmlformats.org/drawingml/2006/chart}CT_NumData"/&gt;
 *           &lt;element name="strRef" type="{http://schemas.openxmlformats.org/drawingml/2006/chart}CT_StrRef"/&gt;
 *           &lt;element name="strLit" type="{http://schemas.openxmlformats.org/drawingml/2006/chart}CT_StrData"/&gt;
 *         &lt;/choice&gt;
 *       &lt;/sequence&gt;
 *     &lt;/restriction&gt;
 *   &lt;/complexContent&gt;
 * &lt;/complexType&gt;
 * </pre>
 * 
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "CT_AxDataSource", propOrder = {
    "multiLvlStrRef",
    "numRef",
    "numLit",
    "strRef",
    "strLit"
})
public class CTAxDataSource implements Child
{

    protected CTMultiLvlStrRef multiLvlStrRef;
    protected CTNumRef numRef;
    protected CTNumData numLit;
    protected CTStrRef strRef;
    protected CTStrData strLit;
    @XmlTransient
    private Object parent;

    /**
     * Gets the value of the multiLvlStrRef property.
     * 
     * @return
     *     possible object is
     *     {@link CTMultiLvlStrRef }
     *     
     */
    public CTMultiLvlStrRef getMultiLvlStrRef() {
        return multiLvlStrRef;
    }

    /**
     * Sets the value of the multiLvlStrRef property.
     * 
     * @param value
     *     allowed object is
     *     {@link CTMultiLvlStrRef }
     *     
     */
    public void setMultiLvlStrRef(CTMultiLvlStrRef value) {
        this.multiLvlStrRef = value;
    }

    /**
     * Gets the value of the numRef property.
     * 
     * @return
     *     possible object is
     *     {@link CTNumRef }
     *     
     */
    public CTNumRef getNumRef() {
        return numRef;
    }

    /**
     * Sets the value of the numRef property.
     * 
     * @param value
     *     allowed object is
     *     {@link CTNumRef }
     *     
     */
    public void setNumRef(CTNumRef value) {
        this.numRef = value;
    }

    /**
     * Gets the value of the numLit property.
     * 
     * @return
     *     possible object is
     *     {@link CTNumData }
     *     
     */
    public CTNumData getNumLit() {
        return numLit;
    }

    /**
     * Sets the value of the numLit property.
     * 
     * @param value
     *     allowed object is
     *     {@link CTNumData }
     *     
     */
    public void setNumLit(CTNumData value) {
        this.numLit = value;
    }

    /**
     * Gets the value of the strRef property.
     * 
     * @return
     *     possible object is
     *     {@link CTStrRef }
     *     
     */
    public CTStrRef getStrRef() {
        return strRef;
    }

    /**
     * Sets the value of the strRef property.
     * 
     * @param value
     *     allowed object is
     *     {@link CTStrRef }
     *     
     */
    public void setStrRef(CTStrRef value) {
        this.strRef = value;
    }

    /**
     * Gets the value of the strLit property.
     * 
     * @return
     *     possible object is
     *     {@link CTStrData }
     *     
     */
    public CTStrData getStrLit() {
        return strLit;
    }

    /**
     * Sets the value of the strLit property.
     * 
     * @param value
     *     allowed object is
     *     {@link CTStrData }
     *     
     */
    public void setStrLit(CTStrData value) {
        this.strLit = value;
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
