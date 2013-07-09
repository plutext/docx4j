/*
 *  Copyright 2013, Plutext Pty Ltd.
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


package org.docx4j.w14;

import org.jvnet.jaxb2_commons.ppp.Child;

import java.util.ArrayList;
import java.util.List;
import javax.xml.bind.JAXBElement;
import javax.xml.bind.Unmarshaller;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElementRef;
import javax.xml.bind.annotation.XmlElementRefs;
import javax.xml.bind.annotation.XmlTransient;
import javax.xml.bind.annotation.XmlType;



/**
 * <p>Java class for CT_SchemeColor complex type.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
 * 
 * <pre>
 * &lt;complexType name="CT_SchemeColor">
 *   &lt;complexContent>
 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
 *       &lt;sequence>
 *         &lt;group ref="{http://schemas.microsoft.com/office/word/2010/wordml}EG_ColorTransform" maxOccurs="unbounded" minOccurs="0"/>
 *       &lt;/sequence>
 *       &lt;attribute name="val" use="required" type="{http://schemas.microsoft.com/office/word/2010/wordml}ST_SchemeColorVal" />
 *     &lt;/restriction>
 *   &lt;/complexContent>
 * &lt;/complexType>
 * </pre>
 * 
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "CT_SchemeColor", propOrder = {
    "egColorTransform"
})
public class CTSchemeColor implements Child
{

    @XmlElementRefs({
        @XmlElementRef(name = "shade", namespace = "http://schemas.microsoft.com/office/word/2010/wordml", type = JAXBElement.class),
        @XmlElementRef(name = "lumOff", namespace = "http://schemas.microsoft.com/office/word/2010/wordml", type = JAXBElement.class),
        @XmlElementRef(name = "tint", namespace = "http://schemas.microsoft.com/office/word/2010/wordml", type = JAXBElement.class),
        @XmlElementRef(name = "lumMod", namespace = "http://schemas.microsoft.com/office/word/2010/wordml", type = JAXBElement.class),
        @XmlElementRef(name = "sat", namespace = "http://schemas.microsoft.com/office/word/2010/wordml", type = JAXBElement.class),
        @XmlElementRef(name = "satMod", namespace = "http://schemas.microsoft.com/office/word/2010/wordml", type = JAXBElement.class),
        @XmlElementRef(name = "hueMod", namespace = "http://schemas.microsoft.com/office/word/2010/wordml", type = JAXBElement.class),
        @XmlElementRef(name = "satOff", namespace = "http://schemas.microsoft.com/office/word/2010/wordml", type = JAXBElement.class),
        @XmlElementRef(name = "alpha", namespace = "http://schemas.microsoft.com/office/word/2010/wordml", type = JAXBElement.class),
        @XmlElementRef(name = "lum", namespace = "http://schemas.microsoft.com/office/word/2010/wordml", type = JAXBElement.class)
    })
    protected List<JAXBElement<?>> egColorTransform;
    @XmlAttribute(name = "val", namespace = "http://schemas.microsoft.com/office/word/2010/wordml", required = true)
    protected STSchemeColorVal val;
    @XmlTransient
    private Object parent;

    /**
     * Gets the value of the egColorTransform property.
     * 
     * <p>
     * This accessor method returns a reference to the live list,
     * not a snapshot. Therefore any modification you make to the
     * returned list will be present inside the JAXB object.
     * This is why there is not a <CODE>set</CODE> method for the egColorTransform property.
     * 
     * <p>
     * For example, to add a new item, do as follows:
     * <pre>
     *    getEGColorTransform().add(newItem);
     * </pre>
     * 
     * 
     * <p>
     * Objects of the following type(s) are allowed in the list
     * {@link JAXBElement }{@code <}{@link CTPositiveFixedPercentage }{@code >}
     * {@link JAXBElement }{@code <}{@link CTPercentage }{@code >}
     * {@link JAXBElement }{@code <}{@link CTPositiveFixedPercentage }{@code >}
     * {@link JAXBElement }{@code <}{@link CTPercentage }{@code >}
     * {@link JAXBElement }{@code <}{@link CTPercentage }{@code >}
     * {@link JAXBElement }{@code <}{@link CTPositivePercentage }{@code >}
     * {@link JAXBElement }{@code <}{@link CTPercentage }{@code >}
     * {@link JAXBElement }{@code <}{@link CTPercentage }{@code >}
     * {@link JAXBElement }{@code <}{@link CTPositiveFixedPercentage }{@code >}
     * {@link JAXBElement }{@code <}{@link CTPercentage }{@code >}
     * 
     * 
     */
    public List<JAXBElement<?>> getEGColorTransform() {
        if (egColorTransform == null) {
            egColorTransform = new ArrayList<JAXBElement<?>>();
        }
        return this.egColorTransform;
    }

    /**
     * Gets the value of the val property.
     * 
     * @return
     *     possible object is
     *     {@link STSchemeColorVal }
     *     
     */
    public STSchemeColorVal getVal() {
        return val;
    }

    /**
     * Sets the value of the val property.
     * 
     * @param value
     *     allowed object is
     *     {@link STSchemeColorVal }
     *     
     */
    public void setVal(STSchemeColorVal value) {
        this.val = value;
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
