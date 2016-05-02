/*
 *  Copyright 2007-2013, Plutext Pty Ltd.
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


package org.docx4j.wml; 

import javax.xml.bind.Unmarshaller;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlTransient;
import javax.xml.bind.annotation.XmlType;

import org.jvnet.jaxb2_commons.ppp.Child;


/**
 * <p>Java class for CT_FtnProps complex type.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
 * 
 * <pre>
 * &lt;complexType name="CT_FtnProps">
 *   &lt;complexContent>
 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
 *       &lt;sequence>
 *         &lt;element name="pos" type="{http://schemas.openxmlformats.org/wordprocessingml/2006/main}CT_FtnPos" minOccurs="0"/>
 *         &lt;element name="numFmt" type="{http://schemas.openxmlformats.org/wordprocessingml/2006/main}CT_NumFmt" minOccurs="0"/>
 *         &lt;group ref="{http://schemas.openxmlformats.org/wordprocessingml/2006/main}EG_FtnEdnNumProps" minOccurs="0"/>
 *       &lt;/sequence>
 *     &lt;/restriction>
 *   &lt;/complexContent>
 * &lt;/complexType>
 * </pre>
 * 
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "CT_FtnProps", propOrder = {
    "pos",
    "numFmt",
    "numStart",
    "numRestart"
})
public class CTFtnProps implements Child
{

    protected CTFtnPos pos;
    protected NumFmt numFmt;
    protected org.docx4j.wml.CTEdnProps.NumStart numStart;
    protected CTNumRestart numRestart;
    @XmlTransient
    private Object parent;

    /**
     * Gets the value of the pos property.
     * 
     * @return
     *     possible object is
     *     {@link CTFtnPos }
     *     
     */
    public CTFtnPos getPos() {
        return pos;
    }

    /**
     * Sets the value of the pos property.
     * 
     * @param value
     *     allowed object is
     *     {@link CTFtnPos }
     *     
     */
    public void setPos(CTFtnPos value) {
        this.pos = value;
    }

    /**
     * Gets the value of the numFmt property.
     * 
     * @return
     *     possible object is
     *     {@link NumFmt }
     *     
     */
    public NumFmt getNumFmt() {
        return numFmt;
    }

    /**
     * Sets the value of the numFmt property.
     * 
     * @param value
     *     allowed object is
     *     {@link NumFmt }
     *     
     */
    public void setNumFmt(NumFmt value) {
        this.numFmt = value;
    }

    /**
     * Gets the value of the numStart property.
     * 
     * @return
     *     possible object is
     *     {@link org.docx4j.wml.CTEdnProps.NumStart }
     *     
     */
    public org.docx4j.wml.CTEdnProps.NumStart getNumStart() {
        return numStart;
    }

    /**
     * Sets the value of the numStart property.
     * 
     * @param value
     *     allowed object is
     *     {@link org.docx4j.wml.CTEdnProps.NumStart }
     *     
     */
    public void setNumStart(org.docx4j.wml.CTEdnProps.NumStart value) {
        this.numStart = value;
    }

    /**
     * Gets the value of the numRestart property.
     * 
     * @return
     *     possible object is
     *     {@link CTNumRestart }
     *     
     */
    public CTNumRestart getNumRestart() {
        return numRestart;
    }

    /**
     * Sets the value of the numRestart property.
     * 
     * @param value
     *     allowed object is
     *     {@link CTNumRestart }
     *     
     */
    public void setNumRestart(CTNumRestart value) {
        this.numRestart = value;
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
