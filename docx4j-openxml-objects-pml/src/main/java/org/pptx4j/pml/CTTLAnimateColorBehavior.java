/*
 *  Copyright 2010-2012, Plutext Pty Ltd.
 *   
 *  This file is part of pptx4j, a component of docx4j.

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
package org.pptx4j.pml;

import javax.xml.bind.Unmarshaller;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlTransient;
import javax.xml.bind.annotation.XmlType;
import org.docx4j.dml.CTColor;

import org.jvnet.jaxb2_commons.ppp.Child;


/**
 * <p>Java class for CT_TLAnimateColorBehavior complex type.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
 * 
 * <pre>
 * &lt;complexType name="CT_TLAnimateColorBehavior"&gt;
 *   &lt;complexContent&gt;
 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType"&gt;
 *       &lt;sequence&gt;
 *         &lt;element name="cBhvr" type="{http://schemas.openxmlformats.org/presentationml/2006/main}CT_TLCommonBehaviorData"/&gt;
 *         &lt;element name="by" type="{http://schemas.openxmlformats.org/presentationml/2006/main}CT_TLByAnimateColorTransform" minOccurs="0"/&gt;
 *         &lt;element name="from" type="{http://schemas.openxmlformats.org/drawingml/2006/main}CT_Color" minOccurs="0"/&gt;
 *         &lt;element name="to" type="{http://schemas.openxmlformats.org/drawingml/2006/main}CT_Color" minOccurs="0"/&gt;
 *       &lt;/sequence&gt;
 *       &lt;attribute name="clrSpc" type="{http://schemas.openxmlformats.org/presentationml/2006/main}ST_TLAnimateColorSpace" /&gt;
 *       &lt;attribute name="dir" type="{http://schemas.openxmlformats.org/presentationml/2006/main}ST_TLAnimateColorDirection" /&gt;
 *     &lt;/restriction&gt;
 *   &lt;/complexContent&gt;
 * &lt;/complexType&gt;
 * </pre>
 * 
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "CT_TLAnimateColorBehavior", propOrder = {
    "cBhvr",
    "by",
    "from",
    "to"
})
public class CTTLAnimateColorBehavior implements Child
{

    @XmlElement(required = true)
    protected CTTLCommonBehaviorData cBhvr;
    protected CTTLByAnimateColorTransform by;
    protected CTColor from;
    protected CTColor to;
    @XmlAttribute(name = "clrSpc")
    protected STTLAnimateColorSpace clrSpc;
    @XmlAttribute(name = "dir")
    protected STTLAnimateColorDirection dir;
    @XmlTransient
    private Object parent;

    /**
     * Gets the value of the cBhvr property.
     * 
     * @return
     *     possible object is
     *     {@link CTTLCommonBehaviorData }
     *     
     */
    public CTTLCommonBehaviorData getCBhvr() {
        return cBhvr;
    }

    /**
     * Sets the value of the cBhvr property.
     * 
     * @param value
     *     allowed object is
     *     {@link CTTLCommonBehaviorData }
     *     
     */
    public void setCBhvr(CTTLCommonBehaviorData value) {
        this.cBhvr = value;
    }

    /**
     * Gets the value of the by property.
     * 
     * @return
     *     possible object is
     *     {@link CTTLByAnimateColorTransform }
     *     
     */
    public CTTLByAnimateColorTransform getBy() {
        return by;
    }

    /**
     * Sets the value of the by property.
     * 
     * @param value
     *     allowed object is
     *     {@link CTTLByAnimateColorTransform }
     *     
     */
    public void setBy(CTTLByAnimateColorTransform value) {
        this.by = value;
    }

    /**
     * Gets the value of the from property.
     * 
     * @return
     *     possible object is
     *     {@link CTColor }
     *     
     */
    public CTColor getFrom() {
        return from;
    }

    /**
     * Sets the value of the from property.
     * 
     * @param value
     *     allowed object is
     *     {@link CTColor }
     *     
     */
    public void setFrom(CTColor value) {
        this.from = value;
    }

    /**
     * Gets the value of the to property.
     * 
     * @return
     *     possible object is
     *     {@link CTColor }
     *     
     */
    public CTColor getTo() {
        return to;
    }

    /**
     * Sets the value of the to property.
     * 
     * @param value
     *     allowed object is
     *     {@link CTColor }
     *     
     */
    public void setTo(CTColor value) {
        this.to = value;
    }

    /**
     * Gets the value of the clrSpc property.
     * 
     * @return
     *     possible object is
     *     {@link STTLAnimateColorSpace }
     *     
     */
    public STTLAnimateColorSpace getClrSpc() {
        return clrSpc;
    }

    /**
     * Sets the value of the clrSpc property.
     * 
     * @param value
     *     allowed object is
     *     {@link STTLAnimateColorSpace }
     *     
     */
    public void setClrSpc(STTLAnimateColorSpace value) {
        this.clrSpc = value;
    }

    /**
     * Gets the value of the dir property.
     * 
     * @return
     *     possible object is
     *     {@link STTLAnimateColorDirection }
     *     
     */
    public STTLAnimateColorDirection getDir() {
        return dir;
    }

    /**
     * Sets the value of the dir property.
     * 
     * @param value
     *     allowed object is
     *     {@link STTLAnimateColorDirection }
     *     
     */
    public void setDir(STTLAnimateColorDirection value) {
        this.dir = value;
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
