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

import javax.xml.bind.Unmarshaller;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlTransient;
import javax.xml.bind.annotation.XmlType;
import org.jvnet.jaxb2_commons.ppp.Child;


/**
 * <p>Java class for CT_StyleMatrix complex type.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
 * 
 * <pre>
 * &lt;complexType name="CT_StyleMatrix"&gt;
 *   &lt;complexContent&gt;
 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType"&gt;
 *       &lt;sequence&gt;
 *         &lt;element name="fillStyleLst" type="{http://schemas.openxmlformats.org/drawingml/2006/main}CT_FillStyleList"/&gt;
 *         &lt;element name="lnStyleLst" type="{http://schemas.openxmlformats.org/drawingml/2006/main}CT_LineStyleList"/&gt;
 *         &lt;element name="effectStyleLst" type="{http://schemas.openxmlformats.org/drawingml/2006/main}CT_EffectStyleList"/&gt;
 *         &lt;element name="bgFillStyleLst" type="{http://schemas.openxmlformats.org/drawingml/2006/main}CT_BackgroundFillStyleList"/&gt;
 *       &lt;/sequence&gt;
 *       &lt;attribute name="name" type="{http://www.w3.org/2001/XMLSchema}string" default="" /&gt;
 *     &lt;/restriction&gt;
 *   &lt;/complexContent&gt;
 * &lt;/complexType&gt;
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
public class CTStyleMatrix implements Child
{

    @XmlElement(required = true)
    protected CTFillStyleList fillStyleLst;
    @XmlElement(required = true)
    protected CTLineStyleList lnStyleLst;
    @XmlElement(required = true)
    protected CTEffectStyleList effectStyleLst;
    @XmlElement(required = true)
    protected CTBackgroundFillStyleList bgFillStyleLst;
    @XmlAttribute(name = "name")
    protected String name;
    @XmlTransient
    private Object parent;

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
