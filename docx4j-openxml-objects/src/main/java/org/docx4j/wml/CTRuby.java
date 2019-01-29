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
 * <p>Java class for CT_Ruby complex type.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
 * 
 * <pre>
 * &lt;complexType name="CT_Ruby">
 *   &lt;complexContent>
 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
 *       &lt;sequence>
 *         &lt;element name="rubyPr" type="{http://schemas.openxmlformats.org/wordprocessingml/2006/main}CT_RubyPr"/>
 *         &lt;element name="rt" type="{http://schemas.openxmlformats.org/wordprocessingml/2006/main}CT_RubyContent"/>
 *         &lt;element name="rubyBase" type="{http://schemas.openxmlformats.org/wordprocessingml/2006/main}CT_RubyContent"/>
 *       &lt;/sequence>
 *     &lt;/restriction>
 *   &lt;/complexContent>
 * &lt;/complexType>
 * </pre>
 * 
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "CT_Ruby", propOrder = {
    "rubyPr",
    "rt",
    "rubyBase"
})
public class CTRuby implements Child
{

    @XmlElement(required = true)
    protected CTRubyPr rubyPr;
    @XmlElement(required = true)
    protected CTRubyContent rt;
    @XmlElement(required = true)
    protected CTRubyContent rubyBase;
    @XmlTransient
    private Object parent;

    /**
     * Gets the value of the rubyPr property.
     * 
     * @return
     *     possible object is
     *     {@link CTRubyPr }
     *     
     */
    public CTRubyPr getRubyPr() {
        return rubyPr;
    }

    /**
     * Sets the value of the rubyPr property.
     * 
     * @param value
     *     allowed object is
     *     {@link CTRubyPr }
     *     
     */
    public void setRubyPr(CTRubyPr value) {
        this.rubyPr = value;
    }

    /**
     * Gets the value of the rt property.
     * 
     * @return
     *     possible object is
     *     {@link CTRubyContent }
     *     
     */
    public CTRubyContent getRt() {
        return rt;
    }

    /**
     * Sets the value of the rt property.
     * 
     * @param value
     *     allowed object is
     *     {@link CTRubyContent }
     *     
     */
    public void setRt(CTRubyContent value) {
        this.rt = value;
    }

    /**
     * Gets the value of the rubyBase property.
     * 
     * @return
     *     possible object is
     *     {@link CTRubyContent }
     *     
     */
    public CTRubyContent getRubyBase() {
        return rubyBase;
    }

    /**
     * Sets the value of the rubyBase property.
     * 
     * @param value
     *     allowed object is
     *     {@link CTRubyContent }
     *     
     */
    public void setRubyBase(CTRubyContent value) {
        this.rubyBase = value;
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
