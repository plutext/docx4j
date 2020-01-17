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
import javax.xml.bind.annotation.XmlTransient;
import javax.xml.bind.annotation.XmlType;

import org.jvnet.jaxb2_commons.ppp.Child;


/**
 * <p>Java class for CT_TblStylePr complex type.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
 * 
 * <pre>
 * &lt;complexType name="CT_TblStylePr">
 *   &lt;complexContent>
 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
 *       &lt;sequence>
 *         &lt;element name="pPr" type="{http://schemas.openxmlformats.org/wordprocessingml/2006/main}CT_PPr" minOccurs="0"/>
 *         &lt;element name="rPr" type="{http://schemas.openxmlformats.org/wordprocessingml/2006/main}CT_RPr" minOccurs="0"/>
 *         &lt;element name="tblPr" type="{http://schemas.openxmlformats.org/wordprocessingml/2006/main}CT_TblPrBase" minOccurs="0"/>
 *         &lt;element name="trPr" type="{http://schemas.openxmlformats.org/wordprocessingml/2006/main}CT_TrPr" minOccurs="0"/>
 *         &lt;element name="tcPr" type="{http://schemas.openxmlformats.org/wordprocessingml/2006/main}CT_TcPr" minOccurs="0"/>
 *       &lt;/sequence>
 *       &lt;attribute name="type" use="required" type="{http://schemas.openxmlformats.org/wordprocessingml/2006/main}ST_TblStyleOverrideType" />
 *     &lt;/restriction>
 *   &lt;/complexContent>
 * &lt;/complexType>
 * </pre>
 * 
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "CT_TblStylePr", propOrder = {
    "pPr",
    "rPr",
    "tblPr",
    "trPr",
    "tcPr"
})
public class CTTblStylePr implements Child
{

    protected PPr pPr;
    protected RPr rPr;
    protected CTTblPrBase tblPr;
    protected TrPr trPr;
    protected TcPr tcPr;
    @XmlAttribute(name = "type", namespace = "http://schemas.openxmlformats.org/wordprocessingml/2006/main", required = true)
    protected STTblStyleOverrideType type;
    @XmlTransient
    private Object parent;

    /**
     * Gets the value of the pPr property.
     * 
     * @return
     *     possible object is
     *     {@link PPr }
     *     
     */
    public PPr getPPr() {
        return pPr;
    }

    /**
     * Sets the value of the pPr property.
     * 
     * @param value
     *     allowed object is
     *     {@link PPr }
     *     
     */
    public void setPPr(PPr value) {
        this.pPr = value;
    }

    /**
     * Gets the value of the rPr property.
     * 
     * @return
     *     possible object is
     *     {@link RPr }
     *     
     */
    public RPr getRPr() {
        return rPr;
    }

    /**
     * Sets the value of the rPr property.
     * 
     * @param value
     *     allowed object is
     *     {@link RPr }
     *     
     */
    public void setRPr(RPr value) {
        this.rPr = value;
    }

    /**
     * Gets the value of the tblPr property.
     * 
     * @return
     *     possible object is
     *     {@link CTTblPrBase }
     *     
     */
    public CTTblPrBase getTblPr() {
        return tblPr;
    }

    /**
     * Sets the value of the tblPr property.
     * 
     * @param value
     *     allowed object is
     *     {@link CTTblPrBase }
     *     
     */
    public void setTblPr(CTTblPrBase value) {
        this.tblPr = value;
    }

    /**
     * Gets the value of the trPr property.
     * 
     * @return
     *     possible object is
     *     {@link TrPr }
     *     
     */
    public TrPr getTrPr() {
        return trPr;
    }

    /**
     * Sets the value of the trPr property.
     * 
     * @param value
     *     allowed object is
     *     {@link TrPr }
     *     
     */
    public void setTrPr(TrPr value) {
        this.trPr = value;
    }

    /**
     * Gets the value of the tcPr property.
     * 
     * @return
     *     possible object is
     *     {@link TcPr }
     *     
     */
    public TcPr getTcPr() {
        return tcPr;
    }

    /**
     * Sets the value of the tcPr property.
     * 
     * @param value
     *     allowed object is
     *     {@link TcPr }
     *     
     */
    public void setTcPr(TcPr value) {
        this.tcPr = value;
    }

    /**
     * Gets the value of the type property.
     * 
     * @return
     *     possible object is
     *     {@link STTblStyleOverrideType }
     *     
     */
    public STTblStyleOverrideType getType() {
        return type;
    }

    /**
     * Sets the value of the type property.
     * 
     * @param value
     *     allowed object is
     *     {@link STTblStyleOverrideType }
     *     
     */
    public void setType(STTblStyleOverrideType value) {
        this.type = value;
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
