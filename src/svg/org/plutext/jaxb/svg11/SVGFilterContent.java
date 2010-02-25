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
import javax.xml.bind.JAXBElement;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElementRef;
import javax.xml.bind.annotation.XmlElementRefs;
import javax.xml.bind.annotation.XmlType;


/**
 * <p>Java class for SVG.filter.content complex type.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
 * 
 * <pre>
 * &lt;complexType name="SVG.filter.content">
 *   &lt;complexContent>
 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
 *       &lt;sequence>
 *         &lt;element ref="{http://www.w3.org/2000/svg}SVG.Description.class" maxOccurs="unbounded" minOccurs="0"/>
 *         &lt;choice maxOccurs="unbounded" minOccurs="0">
 *           &lt;element ref="{http://www.w3.org/2000/svg}animate"/>
 *           &lt;element ref="{http://www.w3.org/2000/svg}set"/>
 *           &lt;element ref="{http://www.w3.org/2000/svg}SVG.FilterPrimitive.class"/>
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
@XmlType(name = "SVG.filter.content", propOrder = {
    "svgDescriptionClass",
    "animateOrSetOrSVGFilterPrimitiveClass"
})
public class SVGFilterContent {

    @XmlElementRef(name = "SVG.Description.class", namespace = "http://www.w3.org/2000/svg", type = JAXBElement.class)
    protected List<JAXBElement<?>> svgDescriptionClass;
    @XmlElementRefs({
        @XmlElementRef(name = "animate", namespace = "http://www.w3.org/2000/svg", type = JAXBElement.class),
        @XmlElementRef(name = "set", namespace = "http://www.w3.org/2000/svg", type = JAXBElement.class),
        @XmlElementRef(name = "SVG.FilterPrimitive.class", namespace = "http://www.w3.org/2000/svg", type = JAXBElement.class)
    })
    protected List<JAXBElement<?>> animateOrSetOrSVGFilterPrimitiveClass;

    /**
     * Gets the value of the svgDescriptionClass property.
     * 
     * <p>
     * This accessor method returns a reference to the live list,
     * not a snapshot. Therefore any modification you make to the
     * returned list will be present inside the JAXB object.
     * This is why there is not a <CODE>set</CODE> method for the svgDescriptionClass property.
     * 
     * <p>
     * For example, to add a new item, do as follows:
     * <pre>
     *    getSVGDescriptionClass().add(newItem);
     * </pre>
     * 
     * 
     * <p>
     * Objects of the following type(s) are allowed in the list
     * {@link JAXBElement }{@code <}{@link Desc }{@code >}
     * {@link JAXBElement }{@code <}{@link Metadata }{@code >}
     * {@link JAXBElement }{@code <}{@link Object }{@code >}
     * {@link JAXBElement }{@code <}{@link Title }{@code >}
     * 
     * 
     */
    public List<JAXBElement<?>> getSVGDescriptionClass() {
        if (svgDescriptionClass == null) {
            svgDescriptionClass = new ArrayList<JAXBElement<?>>();
        }
        return this.svgDescriptionClass;
    }

    /**
     * Gets the value of the animateOrSetOrSVGFilterPrimitiveClass property.
     * 
     * <p>
     * This accessor method returns a reference to the live list,
     * not a snapshot. Therefore any modification you make to the
     * returned list will be present inside the JAXB object.
     * This is why there is not a <CODE>set</CODE> method for the animateOrSetOrSVGFilterPrimitiveClass property.
     * 
     * <p>
     * For example, to add a new item, do as follows:
     * <pre>
     *    getAnimateOrSetOrSVGFilterPrimitiveClass().add(newItem);
     * </pre>
     * 
     * 
     * <p>
     * Objects of the following type(s) are allowed in the list
     * {@link JAXBElement }{@code <}{@link Animate }{@code >}
     * {@link JAXBElement }{@code <}{@link FeTurbulence }{@code >}
     * {@link JAXBElement }{@code <}{@link FeComposite }{@code >}
     * {@link JAXBElement }{@code <}{@link FeSpecularLighting }{@code >}
     * {@link JAXBElement }{@code <}{@link FeComponentTransfer }{@code >}
     * {@link JAXBElement }{@code <}{@link FeBlend }{@code >}
     * {@link JAXBElement }{@code <}{@link FeMorphology }{@code >}
     * {@link JAXBElement }{@code <}{@link FeConvolveMatrix }{@code >}
     * {@link JAXBElement }{@code <}{@link Object }{@code >}
     * {@link JAXBElement }{@code <}{@link FeDiffuseLighting }{@code >}
     * {@link JAXBElement }{@code <}{@link FeMerge }{@code >}
     * {@link JAXBElement }{@code <}{@link Set }{@code >}
     * {@link JAXBElement }{@code <}{@link FeImage }{@code >}
     * {@link JAXBElement }{@code <}{@link FeFlood }{@code >}
     * {@link JAXBElement }{@code <}{@link FeDisplacementMap }{@code >}
     * {@link JAXBElement }{@code <}{@link FeTile }{@code >}
     * {@link JAXBElement }{@code <}{@link FeOffset }{@code >}
     * {@link JAXBElement }{@code <}{@link FeColorMatrix }{@code >}
     * {@link JAXBElement }{@code <}{@link FeGaussianBlur }{@code >}
     * 
     * 
     */
    public List<JAXBElement<?>> getAnimateOrSetOrSVGFilterPrimitiveClass() {
        if (animateOrSetOrSVGFilterPrimitiveClass == null) {
            animateOrSetOrSVGFilterPrimitiveClass = new ArrayList<JAXBElement<?>>();
        }
        return this.animateOrSetOrSVGFilterPrimitiveClass;
    }

}
