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

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import javax.xml.bind.JAXBElement;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElementRef;
import javax.xml.bind.annotation.XmlElementRefs;
import javax.xml.bind.annotation.XmlMixed;
import javax.xml.bind.annotation.XmlType;


/**
 * <p>Java class for SVG.textPath.content complex type.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
 * 
 * <pre>
 * &lt;complexType name="SVG.textPath.content">
 *   &lt;complexContent>
 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
 *       &lt;choice maxOccurs="unbounded" minOccurs="0">
 *         &lt;element ref="{http://www.w3.org/2000/svg}tspan"/>
 *         &lt;element ref="{http://www.w3.org/2000/svg}tref"/>
 *         &lt;element ref="{http://www.w3.org/2000/svg}altGlyph"/>
 *         &lt;element ref="{http://www.w3.org/2000/svg}animate"/>
 *         &lt;element ref="{http://www.w3.org/2000/svg}set"/>
 *         &lt;element ref="{http://www.w3.org/2000/svg}animateColor"/>
 *         &lt;element ref="{http://www.w3.org/2000/svg}SVG.Description.class"/>
 *         &lt;element ref="{http://www.w3.org/2000/svg}SVG.Hyperlink.class"/>
 *       &lt;/choice>
 *     &lt;/restriction>
 *   &lt;/complexContent>
 * &lt;/complexType>
 * </pre>
 * 
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "SVG.textPath.content", propOrder = {
    "content"
})
public class SVGTextPathContent {

    @XmlElementRefs({
        @XmlElementRef(name = "set", namespace = "http://www.w3.org/2000/svg", type = JAXBElement.class),
        @XmlElementRef(name = "animate", namespace = "http://www.w3.org/2000/svg", type = JAXBElement.class),
        @XmlElementRef(name = "SVG.Hyperlink.class", namespace = "http://www.w3.org/2000/svg", type = JAXBElement.class),
        @XmlElementRef(name = "SVG.Description.class", namespace = "http://www.w3.org/2000/svg", type = JAXBElement.class),
        @XmlElementRef(name = "tref", namespace = "http://www.w3.org/2000/svg", type = JAXBElement.class),
        @XmlElementRef(name = "animateColor", namespace = "http://www.w3.org/2000/svg", type = JAXBElement.class),
        @XmlElementRef(name = "altGlyph", namespace = "http://www.w3.org/2000/svg", type = JAXBElement.class),
        @XmlElementRef(name = "tspan", namespace = "http://www.w3.org/2000/svg", type = JAXBElement.class)
    })
    @XmlMixed
    protected List<Serializable> content;

    /**
     * Gets the value of the content property.
     * 
     * <p>
     * This accessor method returns a reference to the live list,
     * not a snapshot. Therefore any modification you make to the
     * returned list will be present inside the JAXB object.
     * This is why there is not a <CODE>set</CODE> method for the content property.
     * 
     * <p>
     * For example, to add a new item, do as follows:
     * <pre>
     *    getContent().add(newItem);
     * </pre>
     * 
     * 
     * <p>
     * Objects of the following type(s) are allowed in the list
     * {@link JAXBElement }{@code <}{@link Animate }{@code >}
     * {@link JAXBElement }{@code <}{@link SVGHyperlinkClass }{@code >}
     * {@link JAXBElement }{@code <}{@link Metadata }{@code >}
     * {@link JAXBElement }{@code <}{@link Title }{@code >}
     * {@link JAXBElement }{@code <}{@link Tref }{@code >}
     * {@link JAXBElement }{@code <}{@link AnimateColor }{@code >}
     * {@link JAXBElement }{@code <}{@link Desc }{@code >}
     * {@link JAXBElement }{@code <}{@link Set }{@code >}
     * {@link JAXBElement }{@code <}{@link Object }{@code >}
     * {@link JAXBElement }{@code <}{@link SVGHyperlinkClass }{@code >}
     * {@link String }
     * {@link JAXBElement }{@code <}{@link AltGlyph }{@code >}
     * {@link JAXBElement }{@code <}{@link Tspan }{@code >}
     * 
     * 
     */
    public List<Serializable> getContent() {
        if (content == null) {
            content = new ArrayList<Serializable>();
        }
        return this.content;
    }

}
