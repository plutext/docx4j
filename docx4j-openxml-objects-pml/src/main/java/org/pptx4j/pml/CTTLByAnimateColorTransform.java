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

import jakarta.xml.bind.Unmarshaller;
import jakarta.xml.bind.annotation.XmlAccessType;
import jakarta.xml.bind.annotation.XmlAccessorType;
import jakarta.xml.bind.annotation.XmlTransient;
import jakarta.xml.bind.annotation.XmlType;

import org.jvnet.jaxb2_commons.ppp.Child;


/**
 * <p>Java class for CT_TLByAnimateColorTransform complex type.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
 * 
 * <pre>
 * &lt;complexType name="CT_TLByAnimateColorTransform"&gt;
 *   &lt;complexContent&gt;
 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType"&gt;
 *       &lt;choice&gt;
 *         &lt;element name="rgb" type="{http://schemas.openxmlformats.org/presentationml/2006/main}CT_TLByRgbColorTransform"/&gt;
 *         &lt;element name="hsl" type="{http://schemas.openxmlformats.org/presentationml/2006/main}CT_TLByHslColorTransform"/&gt;
 *       &lt;/choice&gt;
 *     &lt;/restriction&gt;
 *   &lt;/complexContent&gt;
 * &lt;/complexType&gt;
 * </pre>
 * 
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "CT_TLByAnimateColorTransform", propOrder = {
    "rgb",
    "hsl"
})
public class CTTLByAnimateColorTransform implements Child
{

    protected CTTLByRgbColorTransform rgb;
    protected CTTLByHslColorTransform hsl;
    @XmlTransient
    private Object parent;

    /**
     * Gets the value of the rgb property.
     * 
     * @return
     *     possible object is
     *     {@link CTTLByRgbColorTransform }
     *     
     */
    public CTTLByRgbColorTransform getRgb() {
        return rgb;
    }

    /**
     * Sets the value of the rgb property.
     * 
     * @param value
     *     allowed object is
     *     {@link CTTLByRgbColorTransform }
     *     
     */
    public void setRgb(CTTLByRgbColorTransform value) {
        this.rgb = value;
    }

    /**
     * Gets the value of the hsl property.
     * 
     * @return
     *     possible object is
     *     {@link CTTLByHslColorTransform }
     *     
     */
    public CTTLByHslColorTransform getHsl() {
        return hsl;
    }

    /**
     * Sets the value of the hsl property.
     * 
     * @param value
     *     allowed object is
     *     {@link CTTLByHslColorTransform }
     *     
     */
    public void setHsl(CTTLByHslColorTransform value) {
        this.hsl = value;
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
