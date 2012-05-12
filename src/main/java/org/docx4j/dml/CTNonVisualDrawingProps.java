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


package org.docx4j.dml;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlType;


/**
 * <p>Java class for CT_NonVisualDrawingProps complex type.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
 * 
 * <pre>
 * &lt;complexType name="CT_NonVisualDrawingProps">
 *   &lt;complexContent>
 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
 *       &lt;sequence>
 *         &lt;element name="hlinkClick" type="{http://schemas.openxmlformats.org/drawingml/2006/main}CT_Hyperlink" minOccurs="0"/>
 *         &lt;element name="hlinkHover" type="{http://schemas.openxmlformats.org/drawingml/2006/main}CT_Hyperlink" minOccurs="0"/>
 *         &lt;element name="extLst" type="{http://schemas.openxmlformats.org/drawingml/2006/main}CT_OfficeArtExtensionList" minOccurs="0"/>
 *       &lt;/sequence>
 *       &lt;attribute name="id" use="required" type="{http://schemas.openxmlformats.org/drawingml/2006/main}ST_DrawingElementId" />
 *       &lt;attribute name="name" use="required" type="{http://www.w3.org/2001/XMLSchema}string" />
 *       &lt;attribute name="descr" type="{http://www.w3.org/2001/XMLSchema}string" default="" />
 *       &lt;attribute name="hidden" type="{http://www.w3.org/2001/XMLSchema}boolean" default="false" />
 *     &lt;/restriction>
 *   &lt;/complexContent>
 * &lt;/complexType>
 * </pre>
 * 
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "CT_NonVisualDrawingProps", propOrder = {
    "hlinkClick",
    "hlinkHover",
    "extLst"
})
public class CTNonVisualDrawingProps {

    protected CTHyperlink hlinkClick;
    protected CTHyperlink hlinkHover;
    protected CTOfficeArtExtensionList extLst;
    @XmlAttribute(required = true)
    protected long id;
    @XmlAttribute(required = true)
    protected String name;
    @XmlAttribute
    protected String descr;
    @XmlAttribute
    protected Boolean hidden;

    /**
     * Gets the value of the hlinkClick property.
     * 
     * @return
     *     possible object is
     *     {@link CTHyperlink }
     *     
     */
    public CTHyperlink getHlinkClick() {
        return hlinkClick;
    }

    /**
     * Sets the value of the hlinkClick property.
     * 
     * @param value
     *     allowed object is
     *     {@link CTHyperlink }
     *     
     */
    public void setHlinkClick(CTHyperlink value) {
        this.hlinkClick = value;
    }

    /**
     * Gets the value of the hlinkHover property.
     * 
     * @return
     *     possible object is
     *     {@link CTHyperlink }
     *     
     */
    public CTHyperlink getHlinkHover() {
        return hlinkHover;
    }

    /**
     * Sets the value of the hlinkHover property.
     * 
     * @param value
     *     allowed object is
     *     {@link CTHyperlink }
     *     
     */
    public void setHlinkHover(CTHyperlink value) {
        this.hlinkHover = value;
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
     * Gets the value of the id property.
     * 
     */
    public long getId() {
        return id;
    }

    /**
     * Sets the value of the id property.
     * 
     */
    public void setId(long value) {
        this.id = value;
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
     * Gets the value of the descr property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getDescr() {
        if (descr == null) {
            return "";
        } else {
            return descr;
        }
    }

    /**
     * Sets the value of the descr property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setDescr(String value) {
        this.descr = value;
    }

    /**
     * Gets the value of the hidden property.
     * 
     * @return
     *     possible object is
     *     {@link Boolean }
     *     
     */
    public boolean isHidden() {
        if (hidden == null) {
            return false;
        } else {
            return hidden;
        }
    }

    /**
     * Sets the value of the hidden property.
     * 
     * @param value
     *     allowed object is
     *     {@link Boolean }
     *     
     */
    public void setHidden(Boolean value) {
        this.hidden = value;
    }

}
