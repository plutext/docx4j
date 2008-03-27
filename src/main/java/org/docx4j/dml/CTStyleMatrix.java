/*
 *  Copyright 2007, Plutext Pty Ltd.
 *   
 *  This file is part of docx4j.

    docx4j is free software: you can use it, redistribute it and/or modify
    it under the terms of version 3 of the GNU Affero General Public License 
    as published by the Free Software Foundation.

    If you need the right to use it under a different license, please
    contact Plutext.

    docx4j is distributed in the hope that it will be useful,
    but WITHOUT ANY WARRANTY; without even the implied warranty of
    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
    GNU Affero General Public License for more details.

    You should have received a copy of the GNU Affero General Public License   
    along with docx4j.  If not, see <http://www.fsf.org/licensing/licenses/>.
    
 */

package org.docx4j.dml;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlType;


/**
 * <p>Java class for CT_StyleMatrix complex type.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
 * 
 * <pre>
 * &lt;complexType name="CT_StyleMatrix">
 *   &lt;complexContent>
 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
 *       &lt;sequence>
 *         &lt;element name="fillStyleLst" type="{http://schemas.openxmlformats.org/drawingml/2006/main}CT_FillStyleList"/>
 *         &lt;element name="lnStyleLst" type="{http://schemas.openxmlformats.org/drawingml/2006/main}CT_LineStyleList"/>
 *         &lt;element name="effectStyleLst" type="{http://schemas.openxmlformats.org/drawingml/2006/main}CT_EffectStyleList"/>
 *         &lt;element name="bgFillStyleLst" type="{http://schemas.openxmlformats.org/drawingml/2006/main}CT_BackgroundFillStyleList"/>
 *       &lt;/sequence>
 *       &lt;attribute name="name" type="{http://www.w3.org/2001/XMLSchema}string" default="" />
 *     &lt;/restriction>
 *   &lt;/complexContent>
 * &lt;/complexType>
 * </pre>
 * 
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "CT_StyleMatrix", propOrder = {
    "fillStyleLst",
    "lnStyleLst",
    "effectStyleLst",
    "bgFillStyleLst"
})
public class CTStyleMatrix {

    @XmlElement(required = true)
    protected CTFillStyleList fillStyleLst;
    @XmlElement(required = true)
    protected CTLineStyleList lnStyleLst;
    @XmlElement(required = true)
    protected CTEffectStyleList effectStyleLst;
    @XmlElement(required = true)
    protected CTBackgroundFillStyleList bgFillStyleLst;
    @XmlAttribute
    protected String name;

    /**
     * Gets the value of the fillStyleLst property.
     * 
     * @return
     *     possible object is
     *     {@link CTFillStyleList }
     *     
     */
    public CTFillStyleList getFillStyleLst() {
        return fillStyleLst;
    }

    /**
     * Sets the value of the fillStyleLst property.
     * 
     * @param value
     *     allowed object is
     *     {@link CTFillStyleList }
     *     
     */
    public void setFillStyleLst(CTFillStyleList value) {
        this.fillStyleLst = value;
    }

    /**
     * Gets the value of the lnStyleLst property.
     * 
     * @return
     *     possible object is
     *     {@link CTLineStyleList }
     *     
     */
    public CTLineStyleList getLnStyleLst() {
        return lnStyleLst;
    }

    /**
     * Sets the value of the lnStyleLst property.
     * 
     * @param value
     *     allowed object is
     *     {@link CTLineStyleList }
     *     
     */
    public void setLnStyleLst(CTLineStyleList value) {
        this.lnStyleLst = value;
    }

    /**
     * Gets the value of the effectStyleLst property.
     * 
     * @return
     *     possible object is
     *     {@link CTEffectStyleList }
     *     
     */
    public CTEffectStyleList getEffectStyleLst() {
        return effectStyleLst;
    }

    /**
     * Sets the value of the effectStyleLst property.
     * 
     * @param value
     *     allowed object is
     *     {@link CTEffectStyleList }
     *     
     */
    public void setEffectStyleLst(CTEffectStyleList value) {
        this.effectStyleLst = value;
    }

    /**
     * Gets the value of the bgFillStyleLst property.
     * 
     * @return
     *     possible object is
     *     {@link CTBackgroundFillStyleList }
     *     
     */
    public CTBackgroundFillStyleList getBgFillStyleLst() {
        return bgFillStyleLst;
    }

    /**
     * Sets the value of the bgFillStyleLst property.
     * 
     * @param value
     *     allowed object is
     *     {@link CTBackgroundFillStyleList }
     *     
     */
    public void setBgFillStyleLst(CTBackgroundFillStyleList value) {
        this.bgFillStyleLst = value;
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
        if (name == null) {
            return "";
        } else {
            return name;
        }
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

}
