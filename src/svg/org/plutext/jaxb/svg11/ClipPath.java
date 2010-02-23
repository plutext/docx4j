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
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlElements;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlSchemaType;
import javax.xml.bind.annotation.XmlType;


/**
 * <p>Java class for anonymous complex type.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
 * 
 * <pre>
 * &lt;complexType>
 *   &lt;complexContent>
 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
 *       &lt;sequence>
 *         &lt;choice maxOccurs="unbounded" minOccurs="0">
 *           &lt;element ref="{http://www.w3.org/2000/svg}desc"/>
 *           &lt;element ref="{http://www.w3.org/2000/svg}title"/>
 *           &lt;element ref="{http://www.w3.org/2000/svg}metadata"/>
 *         &lt;/choice>
 *         &lt;choice maxOccurs="unbounded" minOccurs="0">
 *           &lt;element ref="{http://www.w3.org/2000/svg}animate"/>
 *           &lt;element ref="{http://www.w3.org/2000/svg}set"/>
 *           &lt;element ref="{http://www.w3.org/2000/svg}animateMotion"/>
 *           &lt;element ref="{http://www.w3.org/2000/svg}animateColor"/>
 *           &lt;element ref="{http://www.w3.org/2000/svg}animateTransform"/>
 *           &lt;element ref="{http://www.w3.org/2000/svg}use"/>
 *           &lt;element ref="{http://www.w3.org/2000/svg}path"/>
 *           &lt;element ref="{http://www.w3.org/2000/svg}rect"/>
 *           &lt;element ref="{http://www.w3.org/2000/svg}circle"/>
 *           &lt;element ref="{http://www.w3.org/2000/svg}line"/>
 *           &lt;element ref="{http://www.w3.org/2000/svg}ellipse"/>
 *           &lt;element ref="{http://www.w3.org/2000/svg}polyline"/>
 *           &lt;element ref="{http://www.w3.org/2000/svg}polygon"/>
 *           &lt;element ref="{http://www.w3.org/2000/svg}text"/>
 *           &lt;element ref="{http://www.w3.org/2000/svg}altGlyphDef"/>
 *         &lt;/choice>
 *       &lt;/sequence>
 *       &lt;attribute name="transform" type="{http://www.w3.org/2001/XMLSchema}anySimpleType" />
 *       &lt;attribute name="clipPathUnits">
 *         &lt;simpleType>
 *           &lt;restriction base="{http://www.w3.org/2001/XMLSchema}string">
 *             &lt;enumeration value="userSpaceOnUse"/>
 *             &lt;enumeration value="objectBoundingBox"/>
 *           &lt;/restriction>
 *         &lt;/simpleType>
 *       &lt;/attribute>
 *     &lt;/restriction>
 *   &lt;/complexContent>
 * &lt;/complexType>
 * </pre>
 * 
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "", propOrder = {
    "descOrTitleOrMetadata",
    "animateOrSetOrAnimateMotion"
})
@XmlRootElement(name = "clipPath")
public class ClipPath {

    @XmlElements({
        @XmlElement(name = "metadata", type = Metadata.class),
        @XmlElement(name = "title", type = Title.class),
        @XmlElement(name = "desc", type = Desc.class)
    })
    protected List<Object> descOrTitleOrMetadata;
    @XmlElements({
        @XmlElement(name = "rect", type = Rect.class),
        @XmlElement(name = "altGlyphDef", type = AltGlyphDef.class),
        @XmlElement(name = "use", type = Use.class),
        @XmlElement(name = "line", type = Line.class),
        @XmlElement(name = "animateMotion", type = AnimateMotion.class),
        @XmlElement(name = "circle", type = Circle.class),
        @XmlElement(name = "polyline", type = Polyline.class),
        @XmlElement(name = "animateTransform", type = AnimateTransform.class),
        @XmlElement(name = "set", type = Set.class),
        @XmlElement(name = "text", type = Text.class),
        @XmlElement(name = "animate", type = Animate.class),
        @XmlElement(name = "animateColor", type = AnimateColor.class),
        @XmlElement(name = "polygon", type = Polygon.class),
        @XmlElement(name = "path", type = Path.class),
        @XmlElement(name = "ellipse", type = Ellipse.class)
    })
    protected List<Object> animateOrSetOrAnimateMotion;
    @XmlAttribute
    @XmlSchemaType(name = "anySimpleType")
    protected String transform;
    @XmlAttribute
    protected String clipPathUnits;

    /**
     * Gets the value of the descOrTitleOrMetadata property.
     * 
     * <p>
     * This accessor method returns a reference to the live list,
     * not a snapshot. Therefore any modification you make to the
     * returned list will be present inside the JAXB object.
     * This is why there is not a <CODE>set</CODE> method for the descOrTitleOrMetadata property.
     * 
     * <p>
     * For example, to add a new item, do as follows:
     * <pre>
     *    getDescOrTitleOrMetadata().add(newItem);
     * </pre>
     * 
     * 
     * <p>
     * Objects of the following type(s) are allowed in the list
     * {@link Metadata }
     * {@link Title }
     * {@link Desc }
     * 
     * 
     */
    public List<Object> getDescOrTitleOrMetadata() {
        if (descOrTitleOrMetadata == null) {
            descOrTitleOrMetadata = new ArrayList<Object>();
        }
        return this.descOrTitleOrMetadata;
    }

    /**
     * Gets the value of the animateOrSetOrAnimateMotion property.
     * 
     * <p>
     * This accessor method returns a reference to the live list,
     * not a snapshot. Therefore any modification you make to the
     * returned list will be present inside the JAXB object.
     * This is why there is not a <CODE>set</CODE> method for the animateOrSetOrAnimateMotion property.
     * 
     * <p>
     * For example, to add a new item, do as follows:
     * <pre>
     *    getAnimateOrSetOrAnimateMotion().add(newItem);
     * </pre>
     * 
     * 
     * <p>
     * Objects of the following type(s) are allowed in the list
     * {@link Rect }
     * {@link AltGlyphDef }
     * {@link Use }
     * {@link Line }
     * {@link AnimateMotion }
     * {@link Circle }
     * {@link Polyline }
     * {@link AnimateTransform }
     * {@link Set }
     * {@link Text }
     * {@link Animate }
     * {@link AnimateColor }
     * {@link Polygon }
     * {@link Path }
     * {@link Ellipse }
     * 
     * 
     */
    public List<Object> getAnimateOrSetOrAnimateMotion() {
        if (animateOrSetOrAnimateMotion == null) {
            animateOrSetOrAnimateMotion = new ArrayList<Object>();
        }
        return this.animateOrSetOrAnimateMotion;
    }

    /**
     * Gets the value of the transform property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getTransform() {
        return transform;
    }

    /**
     * Sets the value of the transform property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setTransform(String value) {
        this.transform = value;
    }

    /**
     * Gets the value of the clipPathUnits property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getClipPathUnits() {
        return clipPathUnits;
    }

    /**
     * Sets the value of the clipPathUnits property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setClipPathUnits(String value) {
        this.clipPathUnits = value;
    }

}
