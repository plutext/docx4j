/*
 * Copyright 2012 Plutext Pty Ltd.
 * 
 * This file is part of docx4j.
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
 
package org.docx4j.dml.diagram;

import java.util.ArrayList;
import java.util.List;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlType;
import org.docx4j.dml.CTOfficeArtExtensionList;


/**
 * <p>Java class for CT_DiagramDefinition complex type.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
 * 
 * <pre>
 * &lt;complexType name="CT_DiagramDefinition">
 *   &lt;complexContent>
 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
 *       &lt;sequence>
 *         &lt;element name="title" type="{http://schemas.openxmlformats.org/drawingml/2006/diagram}CT_Name" maxOccurs="unbounded" minOccurs="0"/>
 *         &lt;element name="desc" type="{http://schemas.openxmlformats.org/drawingml/2006/diagram}CT_Description" maxOccurs="unbounded" minOccurs="0"/>
 *         &lt;element name="catLst" type="{http://schemas.openxmlformats.org/drawingml/2006/diagram}CT_Categories" minOccurs="0"/>
 *         &lt;element name="sampData" type="{http://schemas.openxmlformats.org/drawingml/2006/diagram}CT_SampleData" minOccurs="0"/>
 *         &lt;element name="styleData" type="{http://schemas.openxmlformats.org/drawingml/2006/diagram}CT_SampleData" minOccurs="0"/>
 *         &lt;element name="clrData" type="{http://schemas.openxmlformats.org/drawingml/2006/diagram}CT_SampleData" minOccurs="0"/>
 *         &lt;element name="layoutNode" type="{http://schemas.openxmlformats.org/drawingml/2006/diagram}CT_LayoutNode"/>
 *         &lt;element name="extLst" type="{http://schemas.openxmlformats.org/drawingml/2006/main}CT_OfficeArtExtensionList" minOccurs="0"/>
 *       &lt;/sequence>
 *       &lt;attribute name="uniqueId" type="{http://www.w3.org/2001/XMLSchema}string" default="" />
 *       &lt;attribute name="minVer" type="{http://www.w3.org/2001/XMLSchema}string" default="http://schemas.openxmlformats.org/drawingml/2006/diagram" />
 *       &lt;attribute name="defStyle" type="{http://www.w3.org/2001/XMLSchema}string" default="" />
 *     &lt;/restriction>
 *   &lt;/complexContent>
 * &lt;/complexType>
 * </pre>
 * 
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "CT_DiagramDefinition", propOrder = {
    "title",
    "desc",
    "catLst",
    "sampData",
    "styleData",
    "clrData",
    "layoutNode",
    "extLst"
})
@XmlRootElement(name = "layoutDef")
public class CTDiagramDefinition {

    protected List<CTName> title;
    protected List<CTDescription> desc;
    protected CTCategories catLst;
    protected CTSampleData sampData;
    protected CTSampleData styleData;
    protected CTSampleData clrData;
    @XmlElement(required = true)
    protected CTLayoutNode layoutNode;
    protected CTOfficeArtExtensionList extLst;
    @XmlAttribute
    protected String uniqueId;
    @XmlAttribute
    protected String minVer;
    @XmlAttribute
    protected String defStyle;

    /**
     * Gets the value of the title property.
     * 
     * <p>
     * This accessor method returns a reference to the live list,
     * not a snapshot. Therefore any modification you make to the
     * returned list will be present inside the JAXB object.
     * This is why there is not a <CODE>set</CODE> method for the title property.
     * 
     * <p>
     * For example, to add a new item, do as follows:
     * <pre>
     *    getTitle().add(newItem);
     * </pre>
     * 
     * 
     * <p>
     * Objects of the following type(s) are allowed in the list
     * {@link CTName }
     * 
     * 
     */
    public List<CTName> getTitle() {
        if (title == null) {
            title = new ArrayList<CTName>();
        }
        return this.title;
    }

    /**
     * Gets the value of the desc property.
     * 
     * <p>
     * This accessor method returns a reference to the live list,
     * not a snapshot. Therefore any modification you make to the
     * returned list will be present inside the JAXB object.
     * This is why there is not a <CODE>set</CODE> method for the desc property.
     * 
     * <p>
     * For example, to add a new item, do as follows:
     * <pre>
     *    getDesc().add(newItem);
     * </pre>
     * 
     * 
     * <p>
     * Objects of the following type(s) are allowed in the list
     * {@link CTDescription }
     * 
     * 
     */
    public List<CTDescription> getDesc() {
        if (desc == null) {
            desc = new ArrayList<CTDescription>();
        }
        return this.desc;
    }

    /**
     * Gets the value of the catLst property.
     * 
     * @return
     *     possible object is
     *     {@link CTCategories }
     *     
     */
    public CTCategories getCatLst() {
        return catLst;
    }

    /**
     * Sets the value of the catLst property.
     * 
     * @param value
     *     allowed object is
     *     {@link CTCategories }
     *     
     */
    public void setCatLst(CTCategories value) {
        this.catLst = value;
    }

    /**
     * Gets the value of the sampData property.
     * 
     * @return
     *     possible object is
     *     {@link CTSampleData }
     *     
     */
    public CTSampleData getSampData() {
        return sampData;
    }

    /**
     * Sets the value of the sampData property.
     * 
     * @param value
     *     allowed object is
     *     {@link CTSampleData }
     *     
     */
    public void setSampData(CTSampleData value) {
        this.sampData = value;
    }

    /**
     * Gets the value of the styleData property.
     * 
     * @return
     *     possible object is
     *     {@link CTSampleData }
     *     
     */
    public CTSampleData getStyleData() {
        return styleData;
    }

    /**
     * Sets the value of the styleData property.
     * 
     * @param value
     *     allowed object is
     *     {@link CTSampleData }
     *     
     */
    public void setStyleData(CTSampleData value) {
        this.styleData = value;
    }

    /**
     * Gets the value of the clrData property.
     * 
     * @return
     *     possible object is
     *     {@link CTSampleData }
     *     
     */
    public CTSampleData getClrData() {
        return clrData;
    }

    /**
     * Sets the value of the clrData property.
     * 
     * @param value
     *     allowed object is
     *     {@link CTSampleData }
     *     
     */
    public void setClrData(CTSampleData value) {
        this.clrData = value;
    }

    /**
     * Gets the value of the layoutNode property.
     * 
     * @return
     *     possible object is
     *     {@link CTLayoutNode }
     *     
     */
    public CTLayoutNode getLayoutNode() {
        return layoutNode;
    }

    /**
     * Sets the value of the layoutNode property.
     * 
     * @param value
     *     allowed object is
     *     {@link CTLayoutNode }
     *     
     */
    public void setLayoutNode(CTLayoutNode value) {
        this.layoutNode = value;
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
     * Gets the value of the uniqueId property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getUniqueId() {
        if (uniqueId == null) {
            return "";
        } else {
            return uniqueId;
        }
    }

    /**
     * Sets the value of the uniqueId property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setUniqueId(String value) {
        this.uniqueId = value;
    }

    /**
     * Gets the value of the minVer property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getMinVer() {
        if (minVer == null) {
            return "http://schemas.openxmlformats.org/drawingml/2006/diagram";
        } else {
            return minVer;
        }
    }

    /**
     * Sets the value of the minVer property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setMinVer(String value) {
        this.minVer = value;
    }

    /**
     * Gets the value of the defStyle property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getDefStyle() {
        if (defStyle == null) {
            return "";
        } else {
            return defStyle;
        }
    }

    /**
     * Sets the value of the defStyle property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setDefStyle(String value) {
        this.defStyle = value;
    }

}
