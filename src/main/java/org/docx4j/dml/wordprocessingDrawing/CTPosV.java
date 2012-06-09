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
 
package org.docx4j.dml.wordprocessingDrawing;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlType;


/**
 * <p>Java class for CT_PosV complex type.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
 * 
 * <pre>
 * &lt;complexType name="CT_PosV">
 *   &lt;complexContent>
 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
 *       &lt;sequence>
 *         &lt;choice>
 *           &lt;element name="align" type="{http://schemas.openxmlformats.org/drawingml/2006/wordprocessingDrawing}ST_AlignV"/>
 *           &lt;element name="posOffset" type="{http://schemas.openxmlformats.org/drawingml/2006/wordprocessingDrawing}ST_PositionOffset"/>
 *         &lt;/choice>
 *       &lt;/sequence>
 *       &lt;attribute name="relativeFrom" use="required" type="{http://schemas.openxmlformats.org/drawingml/2006/wordprocessingDrawing}ST_RelFromV" />
 *     &lt;/restriction>
 *   &lt;/complexContent>
 * &lt;/complexType>
 * </pre>
 * 
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "CT_PosV", propOrder = {
    "align",
    "posOffset"
})
public class CTPosV {

    protected STAlignV align;
    protected Integer posOffset;
    @XmlAttribute(required = true)
    protected STRelFromV relativeFrom;

    /**
     * Gets the value of the align property.
     * 
     * @return
     *     possible object is
     *     {@link STAlignV }
     *     
     */
    public STAlignV getAlign() {
        return align;
    }

    /**
     * Sets the value of the align property.
     * 
     * @param value
     *     allowed object is
     *     {@link STAlignV }
     *     
     */
    public void setAlign(STAlignV value) {
        this.align = value;
    }

    /**
     * Gets the value of the posOffset property.
     * 
     * @return
     *     possible object is
     *     {@link Integer }
     *     
     */
    public Integer getPosOffset() {
        return posOffset;
    }

    /**
     * Sets the value of the posOffset property.
     * 
     * @param value
     *     allowed object is
     *     {@link Integer }
     *     
     */
    public void setPosOffset(Integer value) {
        this.posOffset = value;
    }

    /**
     * Gets the value of the relativeFrom property.
     * 
     * @return
     *     possible object is
     *     {@link STRelFromV }
     *     
     */
    public STRelFromV getRelativeFrom() {
        return relativeFrom;
    }

    /**
     * Sets the value of the relativeFrom property.
     * 
     * @param value
     *     allowed object is
     *     {@link STRelFromV }
     *     
     */
    public void setRelativeFrom(STRelFromV value) {
        this.relativeFrom = value;
    }

}
