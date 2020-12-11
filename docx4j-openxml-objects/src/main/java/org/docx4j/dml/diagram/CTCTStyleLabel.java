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


package org.docx4j.dml.diagram;

import javax.xml.bind.Unmarshaller;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlTransient;
import javax.xml.bind.annotation.XmlType;
import org.docx4j.dml.CTOfficeArtExtensionList;
import org.jvnet.jaxb2_commons.ppp.Child;


/**
 * <p>Java class for CT_CTStyleLabel complex type.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
 * 
 * <pre>
 * &lt;complexType name="CT_CTStyleLabel"&gt;
 *   &lt;complexContent&gt;
 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType"&gt;
 *       &lt;sequence&gt;
 *         &lt;element name="fillClrLst" type="{http://schemas.openxmlformats.org/drawingml/2006/diagram}CT_Colors" minOccurs="0"/&gt;
 *         &lt;element name="linClrLst" type="{http://schemas.openxmlformats.org/drawingml/2006/diagram}CT_Colors" minOccurs="0"/&gt;
 *         &lt;element name="effectClrLst" type="{http://schemas.openxmlformats.org/drawingml/2006/diagram}CT_Colors" minOccurs="0"/&gt;
 *         &lt;element name="txLinClrLst" type="{http://schemas.openxmlformats.org/drawingml/2006/diagram}CT_Colors" minOccurs="0"/&gt;
 *         &lt;element name="txFillClrLst" type="{http://schemas.openxmlformats.org/drawingml/2006/diagram}CT_Colors" minOccurs="0"/&gt;
 *         &lt;element name="txEffectClrLst" type="{http://schemas.openxmlformats.org/drawingml/2006/diagram}CT_Colors" minOccurs="0"/&gt;
 *         &lt;element name="extLst" type="{http://schemas.openxmlformats.org/drawingml/2006/main}CT_OfficeArtExtensionList" minOccurs="0"/&gt;
 *       &lt;/sequence&gt;
 *       &lt;attribute name="name" use="required" type="{http://www.w3.org/2001/XMLSchema}string" /&gt;
 *     &lt;/restriction&gt;
 *   &lt;/complexContent&gt;
 * &lt;/complexType&gt;
 * </pre>
 * 
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "CT_CTStyleLabel", propOrder = {
    "fillClrLst",
    "linClrLst",
    "effectClrLst",
    "txLinClrLst",
    "txFillClrLst",
    "txEffectClrLst",
    "extLst"
})
public class CTCTStyleLabel implements Child
{

    protected CTColors fillClrLst;
    protected CTColors linClrLst;
    protected CTColors effectClrLst;
    protected CTColors txLinClrLst;
    protected CTColors txFillClrLst;
    protected CTColors txEffectClrLst;
    protected CTOfficeArtExtensionList extLst;
    @XmlAttribute(name = "name", required = true)
    protected String name;
    @XmlTransient
    private Object parent;

    /**
     * Gets the value of the fillClrLst property.
     * 
     * @return
     *     possible object is
     *     {@link CTColors }
     *     
     */
    public CTColors getFillClrLst() {
        return fillClrLst;
    }

    /**
     * Sets the value of the fillClrLst property.
     * 
     * @param value
     *     allowed object is
     *     {@link CTColors }
     *     
     */
    public void setFillClrLst(CTColors value) {
        this.fillClrLst = value;
    }

    /**
     * Gets the value of the linClrLst property.
     * 
     * @return
     *     possible object is
     *     {@link CTColors }
     *     
     */
    public CTColors getLinClrLst() {
        return linClrLst;
    }

    /**
     * Sets the value of the linClrLst property.
     * 
     * @param value
     *     allowed object is
     *     {@link CTColors }
     *     
     */
    public void setLinClrLst(CTColors value) {
        this.linClrLst = value;
    }

    /**
     * Gets the value of the effectClrLst property.
     * 
     * @return
     *     possible object is
     *     {@link CTColors }
     *     
     */
    public CTColors getEffectClrLst() {
        return effectClrLst;
    }

    /**
     * Sets the value of the effectClrLst property.
     * 
     * @param value
     *     allowed object is
     *     {@link CTColors }
     *     
     */
    public void setEffectClrLst(CTColors value) {
        this.effectClrLst = value;
    }

    /**
     * Gets the value of the txLinClrLst property.
     * 
     * @return
     *     possible object is
     *     {@link CTColors }
     *     
     */
    public CTColors getTxLinClrLst() {
        return txLinClrLst;
    }

    /**
     * Sets the value of the txLinClrLst property.
     * 
     * @param value
     *     allowed object is
     *     {@link CTColors }
     *     
     */
    public void setTxLinClrLst(CTColors value) {
        this.txLinClrLst = value;
    }

    /**
     * Gets the value of the txFillClrLst property.
     * 
     * @return
     *     possible object is
     *     {@link CTColors }
     *     
     */
    public CTColors getTxFillClrLst() {
        return txFillClrLst;
    }

    /**
     * Sets the value of the txFillClrLst property.
     * 
     * @param value
     *     allowed object is
     *     {@link CTColors }
     *     
     */
    public void setTxFillClrLst(CTColors value) {
        this.txFillClrLst = value;
    }

    /**
     * Gets the value of the txEffectClrLst property.
     * 
     * @return
     *     possible object is
     *     {@link CTColors }
     *     
     */
    public CTColors getTxEffectClrLst() {
        return txEffectClrLst;
    }

    /**
     * Sets the value of the txEffectClrLst property.
     * 
     * @param value
     *     allowed object is
     *     {@link CTColors }
     *     
     */
    public void setTxEffectClrLst(CTColors value) {
        this.txEffectClrLst = value;
    }

    /**
     * Gets the value of the extLst property.
     * 
     * @return
     *     possible object is
     *     {@link CTOfficeArtExtensionList }
     *     
     */
    public CTOfficeArtExtensionList getExtLst() {
        return extLst;
    }

    /**
     * Sets the value of the extLst property.
     * 
     * @param value
     *     allowed object is
     *     {@link CTOfficeArtExtensionList }
     *     
     */
    public void setExtLst(CTOfficeArtExtensionList value) {
        this.extLst = value;
    }

    /**
     * Gets the value of the name property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getName() {
        return name;
    }

    /**
     * Sets the value of the name property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setName(String value) {
        this.name = value;
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
