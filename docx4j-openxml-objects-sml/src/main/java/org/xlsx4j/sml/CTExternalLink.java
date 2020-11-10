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
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlTransient;
import javax.xml.bind.annotation.XmlType;
import org.jvnet.jaxb2_commons.ppp.Child;


/**
 * <p>Java class for CT_ExternalLink complex type.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
 * 
 * <pre>
 * &lt;complexType name="CT_ExternalLink">
 *   &lt;complexContent>
 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
 *       &lt;sequence>
 *         &lt;choice>
 *           &lt;element name="externalBook" type="{http://schemas.openxmlformats.org/spreadsheetml/2006/main}CT_ExternalBook" minOccurs="0"/>
 *           &lt;element name="ddeLink" type="{http://schemas.openxmlformats.org/spreadsheetml/2006/main}CT_DdeLink" minOccurs="0"/>
 *           &lt;element name="oleLink" type="{http://schemas.openxmlformats.org/spreadsheetml/2006/main}CT_OleLink" minOccurs="0"/>
 *         &lt;/choice>
 *         &lt;element name="extLst" type="{http://schemas.openxmlformats.org/spreadsheetml/2006/main}CT_ExtensionList" minOccurs="0"/>
 *       &lt;/sequence>
 *     &lt;/restriction>
 *   &lt;/complexContent>
 * &lt;/complexType>
 * </pre>
 * 
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "CT_ExternalLink", propOrder = {
    "externalBook",
    "ddeLink",
    "oleLink",
    "extLst"
})
@XmlRootElement(name = "externalLink")
public class CTExternalLink implements Child
{

    protected CTExternalBook externalBook;
    protected CTDdeLink ddeLink;
    protected CTOleLink oleLink;
    protected CTExtensionList extLst;
    @XmlTransient
    private Object parent;

    /**
     * Gets the value of the externalBook property.
     * 
     * @return
     *     possible object is
     *     {@link CTExternalBook }
     *     
     */
    public CTExternalBook getExternalBook() {
        return externalBook;
    }

    /**
     * Sets the value of the externalBook property.
     * 
     * @param value
     *     allowed object is
     *     {@link CTExternalBook }
     *     
     */
    public void setExternalBook(CTExternalBook value) {
        this.externalBook = value;
    }

    /**
     * Gets the value of the ddeLink property.
     * 
     * @return
     *     possible object is
     *     {@link CTDdeLink }
     *     
     */
    public CTDdeLink getDdeLink() {
        return ddeLink;
    }

    /**
     * Sets the value of the ddeLink property.
     * 
     * @param value
     *     allowed object is
     *     {@link CTDdeLink }
     *     
     */
    public void setDdeLink(CTDdeLink value) {
        this.ddeLink = value;
    }

    /**
     * Gets the value of the oleLink property.
     * 
     * @return
     *     possible object is
     *     {@link CTOleLink }
     *     
     */
    public CTOleLink getOleLink() {
        return oleLink;
    }

    /**
     * Sets the value of the oleLink property.
     * 
     * @param value
     *     allowed object is
     *     {@link CTOleLink }
     *     
     */
    public void setOleLink(CTOleLink value) {
        this.oleLink = value;
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
