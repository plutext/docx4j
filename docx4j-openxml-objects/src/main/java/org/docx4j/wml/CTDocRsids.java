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

import java.util.ArrayList;
import java.util.List;

import javax.xml.bind.Unmarshaller;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlTransient;
import javax.xml.bind.annotation.XmlType;

import org.jvnet.jaxb2_commons.ppp.Child;


/**
 * <p>Java class for CT_DocRsids complex type.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
 * 
 * <pre>
 * &lt;complexType name="CT_DocRsids">
 *   &lt;complexContent>
 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
 *       &lt;sequence>
 *         &lt;element name="rsidRoot" type="{http://schemas.openxmlformats.org/wordprocessingml/2006/main}CT_LongHexNumber" minOccurs="0"/>
 *         &lt;element name="rsid" type="{http://schemas.openxmlformats.org/wordprocessingml/2006/main}CT_LongHexNumber" maxOccurs="unbounded" minOccurs="0"/>
 *       &lt;/sequence>
 *     &lt;/restriction>
 *   &lt;/complexContent>
 * &lt;/complexType>
 * </pre>
 * 
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "CT_DocRsids", propOrder = {
    "rsidRoot",
    "rsid"
})
public class CTDocRsids implements Child
{

    protected CTLongHexNumber rsidRoot;
    protected List<CTLongHexNumber> rsid = new ArrayListWml<CTLongHexNumber>(this);
    @XmlTransient
    private Object parent;

    /**
     * Gets the value of the rsidRoot property.
     * 
     * @return
     *     possible object is
     *     {@link CTLongHexNumber }
     *     
     */
    public CTLongHexNumber getRsidRoot() {
        return rsidRoot;
    }

    /**
     * Sets the value of the rsidRoot property.
     * 
     * @param value
     *     allowed object is
     *     {@link CTLongHexNumber }
     *     
     */
    public void setRsidRoot(CTLongHexNumber value) {
        this.rsidRoot = value;
    }

    /**
     * Gets the value of the rsid property.
     * 
     * <p>
     * This accessor method returns a reference to the live list,
     * not a snapshot. Therefore any modification you make to the
     * returned list will be present inside the JAXB object.
     * This is why there is not a <CODE>set</CODE> method for the rsid property.
     * 
     * <p>
     * For example, to add a new item, do as follows:
     * <pre>
     *    getRsid().add(newItem);
     * </pre>
     * 
     * 
     * <p>
     * Objects of the following type(s) are allowed in the list
     * {@link CTLongHexNumber }
     * 
     * 
     */
    public List<CTLongHexNumber> getRsid() {
        if (rsid == null) {
            rsid = new ArrayListWml<CTLongHexNumber>(this);
        }
        return this.rsid;
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
