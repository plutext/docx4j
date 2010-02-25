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
 * <p>Java class for SVG.switch.content complex type.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
 * 
 * <pre>
 * &lt;complexType name="SVG.switch.content">
 *   &lt;complexContent>
 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
 *       &lt;sequence>
 *         &lt;element ref="{http://www.w3.org/2000/svg}SVG.Description.class" maxOccurs="unbounded" minOccurs="0"/>
 *         &lt;choice maxOccurs="unbounded" minOccurs="0">
 *           &lt;element ref="{http://www.w3.org/2000/svg}svg"/>
 *           &lt;element ref="{http://www.w3.org/2000/svg}g"/>
 *           &lt;element ref="{http://www.w3.org/2000/svg}use"/>
 *           &lt;element ref="{http://www.w3.org/2000/svg}text"/>
 *           &lt;element ref="{http://www.w3.org/2000/svg}SVG.Animation.class"/>
 *           &lt;element ref="{http://www.w3.org/2000/svg}SVG.Conditional.class"/>
 *           &lt;element ref="{http://www.w3.org/2000/svg}SVG.Image.class"/>
 *           &lt;element ref="{http://www.w3.org/2000/svg}SVG.Shape.class"/>
 *           &lt;element ref="{http://www.w3.org/2000/svg}SVG.Hyperlink.class"/>
 *           &lt;element ref="{http://www.w3.org/2000/svg}foreignObject"/>
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
@XmlType(name = "SVG.switch.content", propOrder = {
    "svgDescriptionClass",
    "svgOrGOrUse"
})
public class SVGSwitchContent {

    @XmlElementRef(name = "SVG.Description.class", namespace = "http://www.w3.org/2000/svg", type = JAXBElement.class)
    protected List<JAXBElement<?>> svgDescriptionClass;
    @XmlElementRefs({
        @XmlElementRef(name = "svg", namespace = "http://www.w3.org/2000/svg", type = JAXBElement.class),
        @XmlElementRef(name = "SVG.Shape.class", namespace = "http://www.w3.org/2000/svg", type = JAXBElement.class),
        @XmlElementRef(name = "SVG.Hyperlink.class", namespace = "http://www.w3.org/2000/svg", type = JAXBElement.class),
        @XmlElementRef(name = "foreignObject", namespace = "http://www.w3.org/2000/svg", type = ForeignObject.class),
        @XmlElementRef(name = "SVG.Animation.class", namespace = "http://www.w3.org/2000/svg", type = JAXBElement.class),
        @XmlElementRef(name = "SVG.Image.class", namespace = "http://www.w3.org/2000/svg", type = JAXBElement.class),
        @XmlElementRef(name = "g", namespace = "http://www.w3.org/2000/svg", type = JAXBElement.class),
        @XmlElementRef(name = "use", namespace = "http://www.w3.org/2000/svg", type = JAXBElement.class),
        @XmlElementRef(name = "text", namespace = "http://www.w3.org/2000/svg", type = JAXBElement.class),
        @XmlElementRef(name = "SVG.Conditional.class", namespace = "http://www.w3.org/2000/svg", type = JAXBElement.class)
    })
    protected List<Object> svgOrGOrUse;

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
     * Gets the value of the svgOrGOrUse property.
     * 
     * <p>
     * This accessor method returns a reference to the live list,
     * not a snapshot. Therefore any modification you make to the
     * returned list will be present inside the JAXB object.
     * This is why there is not a <CODE>set</CODE> method for the svgOrGOrUse property.
     * 
     * <p>
     * For example, to add a new item, do as follows:
     * <pre>
     *    getSvgOrGOrUse().add(newItem);
     * </pre>
     * 
     * 
     * <p>
     * Objects of the following type(s) are allowed in the list
     * {@link JAXBElement }{@code <}{@link Svg }{@code >}
     * {@link JAXBElement }{@code <}{@link Animate }{@code >}
     * {@link JAXBElement }{@code <}{@link SVGHyperlinkClass }{@code >}
     * {@link JAXBElement }{@code <}{@link Path }{@code >}
     * {@link JAXBElement }{@code <}{@link Circle }{@code >}
     * {@link JAXBElement }{@code <}{@link Object }{@code >}
     * {@link JAXBElement }{@code <}{@link Polygon }{@code >}
     * {@link JAXBElement }{@code <}{@link G }{@code >}
     * {@link JAXBElement }{@code <}{@link Line }{@code >}
     * {@link JAXBElement }{@code <}{@link Ellipse }{@code >}
     * {@link JAXBElement }{@code <}{@link AnimateColor }{@code >}
     * {@link JAXBElement }{@code <}{@link Polyline }{@code >}
     * {@link JAXBElement }{@code <}{@link Set }{@code >}
     * {@link JAXBElement }{@code <}{@link AnimateTransform }{@code >}
     * {@link JAXBElement }{@code <}{@link Rect }{@code >}
     * {@link JAXBElement }{@code <}{@link SVGUseClass }{@code >}
     * {@link JAXBElement }{@code <}{@link SVGImageClass }{@code >}
     * {@link JAXBElement }{@code <}{@link Object }{@code >}
     * {@link ForeignObject }
     * {@link JAXBElement }{@code <}{@link SVGConditionalClass }{@code >}
     * {@link JAXBElement }{@code <}{@link SVGImageClass }{@code >}
     * {@link JAXBElement }{@code <}{@link SVGConditionalClass }{@code >}
     * {@link JAXBElement }{@code <}{@link AnimateMotion }{@code >}
     * {@link JAXBElement }{@code <}{@link SVGHyperlinkClass }{@code >}
     * {@link JAXBElement }{@code <}{@link Text }{@code >}
     * 
     * 
     */
    public List<Object> getSvgOrGOrUse() {
        if (svgOrGOrUse == null) {
            svgOrGOrUse = new ArrayList<Object>();
        }
        return this.svgOrGOrUse;
    }

}
