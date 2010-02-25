/*
 *  Copyright 2010, Plutext Pty Ltd.
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


package org.plutext.jaxb.svg11;

import java.util.ArrayList;
import java.util.List;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlElements;
import javax.xml.bind.annotation.XmlType;


/**
 * <p>Java class for SVG.feDiffuseLighting.content complex type.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
 * 
 * <pre>
 * &lt;complexType name="SVG.feDiffuseLighting.content">
 *   &lt;complexContent>
 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
 *       &lt;sequence>
 *         &lt;choice>
 *           &lt;element ref="{http://www.w3.org/2000/svg}feDistantLight"/>
 *           &lt;element ref="{http://www.w3.org/2000/svg}fePointLight"/>
 *           &lt;element ref="{http://www.w3.org/2000/svg}feSpotLight"/>
 *         &lt;/choice>
 *         &lt;choice maxOccurs="unbounded" minOccurs="0">
 *           &lt;element ref="{http://www.w3.org/2000/svg}animate"/>
 *           &lt;element ref="{http://www.w3.org/2000/svg}set"/>
 *           &lt;element ref="{http://www.w3.org/2000/svg}animateColor"/>
 *         &lt;/choice>
 *       &lt;/sequence>
 *     &lt;/restriction>
 *   &lt;/complexContent>
 * &lt;/complexType>
 * </pre>
 * 
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "SVG.feDiffuseLighting.content", propOrder = {
    "feDistantLight",
    "fePointLight",
    "feSpotLight",
    "animateOrSetOrAnimateColor"
})
public class SVGFeDiffuseLightingContent {

    protected FeDistantLight feDistantLight;
    protected FePointLight fePointLight;
    protected FeSpotLight feSpotLight;
    @XmlElements({
        @XmlElement(name = "animate", type = Animate.class),
        @XmlElement(name = "animateColor", type = AnimateColor.class),
        @XmlElement(name = "set", type = Set.class)
    })
    protected List<Object> animateOrSetOrAnimateColor;

    /**
     * Gets the value of the feDistantLight property.
     * 
     * @return
     *     possible object is
     *     {@link FeDistantLight }
     *     
     */
    public FeDistantLight getFeDistantLight() {
        return feDistantLight;
    }

    /**
     * Sets the value of the feDistantLight property.
     * 
     * @param value
     *     allowed object is
     *     {@link FeDistantLight }
     *     
     */
    public void setFeDistantLight(FeDistantLight value) {
        this.feDistantLight = value;
    }

    /**
     * Gets the value of the fePointLight property.
     * 
     * @return
     *     possible object is
     *     {@link FePointLight }
     *     
     */
    public FePointLight getFePointLight() {
        return fePointLight;
    }

    /**
     * Sets the value of the fePointLight property.
     * 
     * @param value
     *     allowed object is
     *     {@link FePointLight }
     *     
     */
    public void setFePointLight(FePointLight value) {
        this.fePointLight = value;
    }

    /**
     * Gets the value of the feSpotLight property.
     * 
     * @return
     *     possible object is
     *     {@link FeSpotLight }
     *     
     */
    public FeSpotLight getFeSpotLight() {
        return feSpotLight;
    }

    /**
     * Sets the value of the feSpotLight property.
     * 
     * @param value
     *     allowed object is
     *     {@link FeSpotLight }
     *     
     */
    public void setFeSpotLight(FeSpotLight value) {
        this.feSpotLight = value;
    }

    /**
     * Gets the value of the animateOrSetOrAnimateColor property.
     * 
     * <p>
     * This accessor method returns a reference to the live list,
     * not a snapshot. Therefore any modification you make to the
     * returned list will be present inside the JAXB object.
     * This is why there is not a <CODE>set</CODE> method for the animateOrSetOrAnimateColor property.
     * 
     * <p>
     * For example, to add a new item, do as follows:
     * <pre>
     *    getAnimateOrSetOrAnimateColor().add(newItem);
     * </pre>
     * 
     * 
     * <p>
     * Objects of the following type(s) are allowed in the list
     * {@link Animate }
     * {@link AnimateColor }
     * {@link Set }
     * 
     * 
     */
    public List<Object> getAnimateOrSetOrAnimateColor() {
        if (animateOrSetOrAnimateColor == null) {
            animateOrSetOrAnimateColor = new ArrayList<Object>();
        }
        return this.animateOrSetOrAnimateColor;
    }

}
