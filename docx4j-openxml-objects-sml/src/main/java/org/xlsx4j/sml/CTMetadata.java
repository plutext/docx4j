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

import java.util.ArrayList;
import java.util.List;
import javax.xml.bind.Unmarshaller;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlTransient;
import javax.xml.bind.annotation.XmlType;
import org.jvnet.jaxb2_commons.ppp.Child;


/**
 * <p>Java class for CT_Metadata complex type.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
 * 
 * <pre>
 * &lt;complexType name="CT_Metadata">
 *   &lt;complexContent>
 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
 *       &lt;sequence>
 *         &lt;element name="metadataTypes" type="{http://schemas.openxmlformats.org/spreadsheetml/2006/main}CT_MetadataTypes" minOccurs="0"/>
 *         &lt;element name="metadataStrings" type="{http://schemas.openxmlformats.org/spreadsheetml/2006/main}CT_MetadataStrings" minOccurs="0"/>
 *         &lt;element name="mdxMetadata" type="{http://schemas.openxmlformats.org/spreadsheetml/2006/main}CT_MdxMetadata" minOccurs="0"/>
 *         &lt;element name="futureMetadata" type="{http://schemas.openxmlformats.org/spreadsheetml/2006/main}CT_FutureMetadata" maxOccurs="unbounded" minOccurs="0"/>
 *         &lt;element name="cellMetadata" type="{http://schemas.openxmlformats.org/spreadsheetml/2006/main}CT_MetadataBlocks" minOccurs="0"/>
 *         &lt;element name="valueMetadata" type="{http://schemas.openxmlformats.org/spreadsheetml/2006/main}CT_MetadataBlocks" minOccurs="0"/>
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
@XmlType(name = "CT_Metadata", propOrder = {
    "metadataTypes",
    "metadataStrings",
    "mdxMetadata",
    "futureMetadata",
    "cellMetadata",
    "valueMetadata",
    "extLst"
})
public class CTMetadata implements Child
{

    protected CTMetadataTypes metadataTypes;
    protected CTMetadataStrings metadataStrings;
    protected CTMdxMetadata mdxMetadata;
    protected List<CTFutureMetadata> futureMetadata;
    protected CTMetadataBlocks cellMetadata;
    protected CTMetadataBlocks valueMetadata;
    protected CTExtensionList extLst;
    @XmlTransient
    private Object parent;

    /**
     * Gets the value of the metadataTypes property.
     * 
     * @return
     *     possible object is
     *     {@link CTMetadataTypes }
     *     
     */
    public CTMetadataTypes getMetadataTypes() {
        return metadataTypes;
    }

    /**
     * Sets the value of the metadataTypes property.
     * 
     * @param value
     *     allowed object is
     *     {@link CTMetadataTypes }
     *     
     */
    public void setMetadataTypes(CTMetadataTypes value) {
        this.metadataTypes = value;
    }

    /**
     * Gets the value of the metadataStrings property.
     * 
     * @return
     *     possible object is
     *     {@link CTMetadataStrings }
     *     
     */
    public CTMetadataStrings getMetadataStrings() {
        return metadataStrings;
    }

    /**
     * Sets the value of the metadataStrings property.
     * 
     * @param value
     *     allowed object is
     *     {@link CTMetadataStrings }
     *     
     */
    public void setMetadataStrings(CTMetadataStrings value) {
        this.metadataStrings = value;
    }

    /**
     * Gets the value of the mdxMetadata property.
     * 
     * @return
     *     possible object is
     *     {@link CTMdxMetadata }
     *     
     */
    public CTMdxMetadata getMdxMetadata() {
        return mdxMetadata;
    }

    /**
     * Sets the value of the mdxMetadata property.
     * 
     * @param value
     *     allowed object is
     *     {@link CTMdxMetadata }
     *     
     */
    public void setMdxMetadata(CTMdxMetadata value) {
        this.mdxMetadata = value;
    }

    /**
     * Gets the value of the futureMetadata property.
     * 
     * <p>
     * This accessor method returns a reference to the live list,
     * not a snapshot. Therefore any modification you make to the
     * returned list will be present inside the JAXB object.
     * This is why there is not a <CODE>set</CODE> method for the futureMetadata property.
     * 
     * <p>
     * For example, to add a new item, do as follows:
     * <pre>
     *    getFutureMetadata().add(newItem);
     * </pre>
     * 
     * 
     * <p>
     * Objects of the following type(s) are allowed in the list
     * {@link CTFutureMetadata }
     * 
     * 
     */
    public List<CTFutureMetadata> getFutureMetadata() {
        if (futureMetadata == null) {
            futureMetadata = new ArrayList<CTFutureMetadata>();
        }
        return this.futureMetadata;
    }

    /**
     * Gets the value of the cellMetadata property.
     * 
     * @return
     *     possible object is
     *     {@link CTMetadataBlocks }
     *     
     */
    public CTMetadataBlocks getCellMetadata() {
        return cellMetadata;
    }

    /**
     * Sets the value of the cellMetadata property.
     * 
     * @param value
     *     allowed object is
     *     {@link CTMetadataBlocks }
     *     
     */
    public void setCellMetadata(CTMetadataBlocks value) {
        this.cellMetadata = value;
    }

    /**
     * Gets the value of the valueMetadata property.
     * 
     * @return
     *     possible object is
     *     {@link CTMetadataBlocks }
     *     
     */
    public CTMetadataBlocks getValueMetadata() {
        return valueMetadata;
    }

    /**
     * Sets the value of the valueMetadata property.
     * 
     * @param value
     *     allowed object is
     *     {@link CTMetadataBlocks }
     *     
     */
    public void setValueMetadata(CTMetadataBlocks value) {
        this.valueMetadata = value;
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
