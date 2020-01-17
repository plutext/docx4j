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


package org.docx4j.dml.wordprocessingDrawing;

import javax.xml.bind.Unmarshaller;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlSchemaType;
import javax.xml.bind.annotation.XmlTransient;
import javax.xml.bind.annotation.XmlType;
import org.jvnet.jaxb2_commons.ppp.Child;


/**
 * <p>Java class for CT_PosV complex type.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
 * 
 * <pre>
 * &lt;complexType name="CT_PosV"&gt;
 *   &lt;complexContent&gt;
 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType"&gt;
 *       &lt;sequence&gt;
 *         &lt;choice&gt;
 *           &lt;element name="align" type="{http://schemas.openxmlformats.org/drawingml/2006/wordprocessingDrawing}ST_AlignV"/&gt;
 *           &lt;element name="posOffset" type="{http://schemas.openxmlformats.org/drawingml/2006/wordprocessingDrawing}ST_PositionOffset"/&gt;
 *         &lt;/choice&gt;
 *       &lt;/sequence&gt;
 *       &lt;attribute name="relativeFrom" use="required" type="{http://schemas.openxmlformats.org/drawingml/2006/wordprocessingDrawing}ST_RelFromV" /&gt;
 *     &lt;/restriction&gt;
 *   &lt;/complexContent&gt;
 * &lt;/complexType&gt;
 * </pre>
 * 
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "CT_PosV", propOrder = {
    "align",
    "posOffset"
})
public class CTPosV implements Child
{

    @XmlSchemaType(name = "token")
    protected STAlignV align;
    protected Integer posOffset;
    @XmlAttribute(name = "relativeFrom", required = true)
    protected STRelFromV relativeFrom;
    @XmlTransient
    private Object parent;

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
