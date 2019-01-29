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

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlType;


/**
 * <p>Java class for CT_TLByAnimateColorTransform complex type.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
 * 
 * <pre>
 * &lt;complexType name="CT_TLByAnimateColorTransform">
 *   &lt;complexContent>
 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
 *       &lt;choice>
 *         &lt;element name="rgb" type="{http://schemas.openxmlformats.org/presentationml/2006/main}CT_TLByRgbColorTransform"/>
 *         &lt;element name="hsl" type="{http://schemas.openxmlformats.org/presentationml/2006/main}CT_TLByHslColorTransform"/>
 *       &lt;/choice>
 *     &lt;/restriction>
 *   &lt;/complexContent>
 * &lt;/complexType>
 * </pre>
 * 
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "CT_TLByAnimateColorTransform", propOrder = {
    "rgb",
    "hsl"
})
public class CTTLByAnimateColorTransform {

    protected CTTLByRgbColorTransform rgb;
    protected CTTLByHslColorTransform hsl;

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

}
