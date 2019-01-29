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
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlTransient;
import javax.xml.bind.annotation.XmlType;

import org.jvnet.jaxb2_commons.ppp.Child;


/**
 * <p>Java class for CT_FldChar complex type.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
 * 
 * <pre>
 * &lt;complexType name="CT_FldChar">
 *   &lt;complexContent>
 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
 *       &lt;choice>
 *         &lt;element name="fldData" type="{http://schemas.openxmlformats.org/wordprocessingml/2006/main}CT_Text" minOccurs="0"/>
 *         &lt;element name="ffData" type="{http://schemas.openxmlformats.org/wordprocessingml/2006/main}CT_FFData" minOccurs="0"/>
 *         &lt;element name="numberingChange" type="{http://schemas.openxmlformats.org/wordprocessingml/2006/main}CT_TrackChangeNumbering" minOccurs="0"/>
 *       &lt;/choice>
 *       &lt;attribute name="fldCharType" use="required" type="{http://schemas.openxmlformats.org/wordprocessingml/2006/main}ST_FldCharType" />
 *       &lt;attribute name="fldLock" type="{http://www.w3.org/2001/XMLSchema}boolean" default="true" />
 *       &lt;attribute name="dirty" type="{http://www.w3.org/2001/XMLSchema}boolean" default="true" />
 *     &lt;/restriction>
 *   &lt;/complexContent>
 * &lt;/complexType>
 * </pre>
 * 
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "CT_FldChar", propOrder = {
    "fldData",
    "ffData",
    "numberingChange"
})
@XmlRootElement(name = "fldChar")
public class FldChar
    implements Child
{

    protected Text fldData;
    protected CTFFData ffData;
    protected CTTrackChangeNumbering numberingChange;
    @XmlAttribute(name = "fldCharType", namespace = "http://schemas.openxmlformats.org/wordprocessingml/2006/main", required = true)
    protected STFldCharType fldCharType;
    @XmlAttribute(name = "fldLock", namespace = "http://schemas.openxmlformats.org/wordprocessingml/2006/main")
    protected Boolean fldLock;
    @XmlAttribute(name = "dirty", namespace = "http://schemas.openxmlformats.org/wordprocessingml/2006/main")
    protected Boolean dirty;
    @XmlTransient
    private Object parent;

    /**
     * Gets the value of the fldData property.
     * 
     * @return
     *     possible object is
     *     {@link Text }
     *     
     */
    public Text getFldData() {
        return fldData;
    }

    /**
     * Sets the value of the fldData property.
     * 
     * @param value
     *     allowed object is
     *     {@link Text }
     *     
     */
    public void setFldData(Text value) {
        this.fldData = value;
    }

    /**
     * Gets the value of the ffData property.
     * 
     * @return
     *     possible object is
     *     {@link CTFFData }
     *     
     */
    public CTFFData getFfData() {
        return ffData;
    }

    /**
     * Sets the value of the ffData property.
     * 
     * @param value
     *     allowed object is
     *     {@link CTFFData }
     *     
     */
    public void setFfData(CTFFData value) {
        this.ffData = value;
    }

    /**
     * Gets the value of the numberingChange property.
     * 
     * @return
     *     possible object is
     *     {@link CTTrackChangeNumbering }
     *     
     */
    public CTTrackChangeNumbering getNumberingChange() {
        return numberingChange;
    }

    /**
     * Sets the value of the numberingChange property.
     * 
     * @param value
     *     allowed object is
     *     {@link CTTrackChangeNumbering }
     *     
     */
    public void setNumberingChange(CTTrackChangeNumbering value) {
        this.numberingChange = value;
    }

    /**
     * Gets the value of the fldCharType property.
     * 
     * @return
     *     possible object is
     *     {@link STFldCharType }
     *     
     */
    public STFldCharType getFldCharType() {
        return fldCharType;
    }

    /**
     * Sets the value of the fldCharType property.
     * 
     * @param value
     *     allowed object is
     *     {@link STFldCharType }
     *     
     */
    public void setFldCharType(STFldCharType value) {
        this.fldCharType = value;
    }

    /**
     * Gets the value of the fldLock property.
     * 
     * @return
     *     possible object is
     *     {@link Boolean }
     *     
     */
    public boolean isFldLock() {
        if (fldLock == null) {
            return true;
        } else {
            return fldLock;
        }
    }

    /**
     * Sets the value of the fldLock property.
     * 
     * @param value
     *     allowed object is
     *     {@link Boolean }
     *     
     */
    public void setFldLock(Boolean value) {
        this.fldLock = value;
    }

    /**
     * Gets the value of the dirty property.
     * 
     * @return
     *     possible object is
     *     {@link Boolean }
     *     
     */
    public boolean isDirty() {
        if (dirty == null) {
            return true;
        } else {
            return dirty;
        }
    }

    /**
     * Sets the value of the dirty property.
     * 
     * @param value
     *     allowed object is
     *     {@link Boolean }
     *     
     */
    public void setDirty(Boolean value) {
        this.dirty = value;
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
