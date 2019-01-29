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

import javax.xml.bind.Unmarshaller;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlTransient;
import javax.xml.bind.annotation.XmlType;

import org.jvnet.jaxb2_commons.ppp.Child;


/**
 * <p>Java class for CT_RubyPr complex type.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
 * 
 * <pre>
 * &lt;complexType name="CT_RubyPr">
 *   &lt;complexContent>
 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
 *       &lt;sequence>
 *         &lt;element name="rubyAlign" type="{http://schemas.openxmlformats.org/wordprocessingml/2006/main}CT_RubyAlign"/>
 *         &lt;element name="hps" type="{http://schemas.openxmlformats.org/wordprocessingml/2006/main}CT_HpsMeasure"/>
 *         &lt;element name="hpsRaise" type="{http://schemas.openxmlformats.org/wordprocessingml/2006/main}CT_HpsMeasure"/>
 *         &lt;element name="hpsBaseText" type="{http://schemas.openxmlformats.org/wordprocessingml/2006/main}CT_HpsMeasure"/>
 *         &lt;element name="lid" type="{http://schemas.openxmlformats.org/wordprocessingml/2006/main}CT_Lang"/>
 *         &lt;element name="dirty" type="{http://schemas.openxmlformats.org/wordprocessingml/2006/main}BooleanDefaultTrue" minOccurs="0"/>
 *       &lt;/sequence>
 *     &lt;/restriction>
 *   &lt;/complexContent>
 * &lt;/complexType>
 * </pre>
 * 
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "CT_RubyPr", propOrder = {
    "rubyAlign",
    "hps",
    "hpsRaise",
    "hpsBaseText",
    "lid",
    "dirty"
})
public class CTRubyPr implements Child
{

    @XmlElement(required = true)
    protected CTRubyAlign rubyAlign;
    @XmlElement(required = true)
    protected HpsMeasure hps;
    @XmlElement(required = true)
    protected HpsMeasure hpsRaise;
    @XmlElement(required = true)
    protected HpsMeasure hpsBaseText;
    @XmlElement(required = true)
    protected CTLang lid;
    protected BooleanDefaultTrue dirty;
    @XmlTransient
    private Object parent;

    /**
     * Gets the value of the rubyAlign property.
     * 
     * @return
     *     possible object is
     *     {@link CTRubyAlign }
     *     
     */
    public CTRubyAlign getRubyAlign() {
        return rubyAlign;
    }

    /**
     * Sets the value of the rubyAlign property.
     * 
     * @param value
     *     allowed object is
     *     {@link CTRubyAlign }
     *     
     */
    public void setRubyAlign(CTRubyAlign value) {
        this.rubyAlign = value;
    }

    /**
     * Gets the value of the hps property.
     * 
     * @return
     *     possible object is
     *     {@link HpsMeasure }
     *     
     */
    public HpsMeasure getHps() {
        return hps;
    }

    /**
     * Sets the value of the hps property.
     * 
     * @param value
     *     allowed object is
     *     {@link HpsMeasure }
     *     
     */
    public void setHps(HpsMeasure value) {
        this.hps = value;
    }

    /**
     * Gets the value of the hpsRaise property.
     * 
     * @return
     *     possible object is
     *     {@link HpsMeasure }
     *     
     */
    public HpsMeasure getHpsRaise() {
        return hpsRaise;
    }

    /**
     * Sets the value of the hpsRaise property.
     * 
     * @param value
     *     allowed object is
     *     {@link HpsMeasure }
     *     
     */
    public void setHpsRaise(HpsMeasure value) {
        this.hpsRaise = value;
    }

    /**
     * Gets the value of the hpsBaseText property.
     * 
     * @return
     *     possible object is
     *     {@link HpsMeasure }
     *     
     */
    public HpsMeasure getHpsBaseText() {
        return hpsBaseText;
    }

    /**
     * Sets the value of the hpsBaseText property.
     * 
     * @param value
     *     allowed object is
     *     {@link HpsMeasure }
     *     
     */
    public void setHpsBaseText(HpsMeasure value) {
        this.hpsBaseText = value;
    }

    /**
     * Gets the value of the lid property.
     * 
     * @return
     *     possible object is
     *     {@link CTLang }
     *     
     */
    public CTLang getLid() {
        return lid;
    }

    /**
     * Sets the value of the lid property.
     * 
     * @param value
     *     allowed object is
     *     {@link CTLang }
     *     
     */
    public void setLid(CTLang value) {
        this.lid = value;
    }

    /**
     * Gets the value of the dirty property.
     * 
     * @return
     *     possible object is
     *     {@link BooleanDefaultTrue }
     *     
     */
    public BooleanDefaultTrue getDirty() {
        return dirty;
    }

    /**
     * Sets the value of the dirty property.
     * 
     * @param value
     *     allowed object is
     *     {@link BooleanDefaultTrue }
     *     
     */
    public void setDirty(BooleanDefaultTrue value) {
        this.dirty = value;
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
