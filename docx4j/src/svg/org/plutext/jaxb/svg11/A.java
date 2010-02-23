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
import javax.xml.bind.annotation.XmlElementRef;
import javax.xml.bind.annotation.XmlElementRefs;
import javax.xml.bind.annotation.XmlMixed;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlSchemaType;
import javax.xml.bind.annotation.XmlType;
import javax.xml.bind.annotation.adapters.CollapsedStringAdapter;
import javax.xml.bind.annotation.adapters.XmlJavaTypeAdapter;


/**
 * <p>Java class for anonymous complex type.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
 * 
 * <pre>
 * &lt;complexType>
 *   &lt;complexContent>
 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
 *       &lt;choice maxOccurs="unbounded" minOccurs="0">
 *         &lt;element ref="{http://www.w3.org/2000/svg}desc"/>
 *         &lt;element ref="{http://www.w3.org/2000/svg}title"/>
 *         &lt;element ref="{http://www.w3.org/2000/svg}metadata"/>
 *         &lt;element ref="{http://www.w3.org/2000/svg}animate"/>
 *         &lt;element ref="{http://www.w3.org/2000/svg}set"/>
 *         &lt;element ref="{http://www.w3.org/2000/svg}animateMotion"/>
 *         &lt;element ref="{http://www.w3.org/2000/svg}animateColor"/>
 *         &lt;element ref="{http://www.w3.org/2000/svg}animateTransform"/>
 *         &lt;element ref="{http://www.w3.org/2000/svg}svg"/>
 *         &lt;element ref="{http://www.w3.org/2000/svg}g"/>
 *         &lt;element ref="{http://www.w3.org/2000/svg}defs"/>
 *         &lt;element ref="{http://www.w3.org/2000/svg}symbol"/>
 *         &lt;element ref="{http://www.w3.org/2000/svg}use"/>
 *         &lt;element ref="{http://www.w3.org/2000/svg}switch"/>
 *         &lt;element ref="{http://www.w3.org/2000/svg}image"/>
 *         &lt;element ref="{http://www.w3.org/2000/svg}style"/>
 *         &lt;element ref="{http://www.w3.org/2000/svg}path"/>
 *         &lt;element ref="{http://www.w3.org/2000/svg}rect"/>
 *         &lt;element ref="{http://www.w3.org/2000/svg}circle"/>
 *         &lt;element ref="{http://www.w3.org/2000/svg}line"/>
 *         &lt;element ref="{http://www.w3.org/2000/svg}ellipse"/>
 *         &lt;element ref="{http://www.w3.org/2000/svg}polyline"/>
 *         &lt;element ref="{http://www.w3.org/2000/svg}polygon"/>
 *         &lt;element ref="{http://www.w3.org/2000/svg}text"/>
 *         &lt;element ref="{http://www.w3.org/2000/svg}altGlyphDef"/>
 *         &lt;element ref="{http://www.w3.org/2000/svg}marker"/>
 *         &lt;element ref="{http://www.w3.org/2000/svg}color-profile"/>
 *         &lt;element ref="{http://www.w3.org/2000/svg}linearGradient"/>
 *         &lt;element ref="{http://www.w3.org/2000/svg}radialGradient"/>
 *         &lt;element ref="{http://www.w3.org/2000/svg}pattern"/>
 *         &lt;element ref="{http://www.w3.org/2000/svg}clipPath"/>
 *         &lt;element ref="{http://www.w3.org/2000/svg}mask"/>
 *         &lt;element ref="{http://www.w3.org/2000/svg}filter"/>
 *         &lt;element ref="{http://www.w3.org/2000/svg}cursor"/>
 *         &lt;element ref="{http://www.w3.org/2000/svg}a"/>
 *         &lt;element ref="{http://www.w3.org/2000/svg}view"/>
 *         &lt;element ref="{http://www.w3.org/2000/svg}script"/>
 *         &lt;element ref="{http://www.w3.org/2000/svg}font"/>
 *         &lt;element ref="{http://www.w3.org/2000/svg}font-face"/>
 *       &lt;/choice>
 *       &lt;attGroup ref="{http://www.w3.org/2000/svg}SVG.Presentation.attrib"/>
 *       &lt;attribute name="transform" type="{http://www.w3.org/2001/XMLSchema}anySimpleType" />
 *       &lt;attribute name="target" type="{http://www.w3.org/2001/XMLSchema}NMTOKEN" />
 *     &lt;/restriction>
 *   &lt;/complexContent>
 * &lt;/complexType>
 * </pre>
 * 
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "", propOrder = {
    "content"
})
@XmlRootElement(name = "a")
public class A {

    @XmlElementRefs({
        @XmlElementRef(name = "font-face", namespace = "http://www.w3.org/2000/svg", type = FontFace.class),
        @XmlElementRef(name = "radialGradient", namespace = "http://www.w3.org/2000/svg", type = RadialGradient.class),
        @XmlElementRef(name = "metadata", namespace = "http://www.w3.org/2000/svg", type = Metadata.class),
        @XmlElementRef(name = "animateTransform", namespace = "http://www.w3.org/2000/svg", type = AnimateTransform.class),
        @XmlElementRef(name = "style", namespace = "http://www.w3.org/2000/svg", type = Style.class),
        @XmlElementRef(name = "view", namespace = "http://www.w3.org/2000/svg", type = View.class),
        @XmlElementRef(name = "line", namespace = "http://www.w3.org/2000/svg", type = Line.class),
        @XmlElementRef(name = "switch", namespace = "http://www.w3.org/2000/svg", type = Switch.class),
        @XmlElementRef(name = "a", namespace = "http://www.w3.org/2000/svg", type = A.class),
        @XmlElementRef(name = "symbol", namespace = "http://www.w3.org/2000/svg", type = Symbol.class),
        @XmlElementRef(name = "clipPath", namespace = "http://www.w3.org/2000/svg", type = ClipPath.class),
        @XmlElementRef(name = "path", namespace = "http://www.w3.org/2000/svg", type = Path.class),
        @XmlElementRef(name = "desc", namespace = "http://www.w3.org/2000/svg", type = Desc.class),
        @XmlElementRef(name = "color-profile", namespace = "http://www.w3.org/2000/svg", type = ColorProfile.class),
        @XmlElementRef(name = "image", namespace = "http://www.w3.org/2000/svg", type = Image.class),
        @XmlElementRef(name = "ellipse", namespace = "http://www.w3.org/2000/svg", type = Ellipse.class),
        @XmlElementRef(name = "animateColor", namespace = "http://www.w3.org/2000/svg", type = AnimateColor.class),
        @XmlElementRef(name = "rect", namespace = "http://www.w3.org/2000/svg", type = Rect.class),
        @XmlElementRef(name = "title", namespace = "http://www.w3.org/2000/svg", type = Title.class),
        @XmlElementRef(name = "script", namespace = "http://www.w3.org/2000/svg", type = Script.class),
        @XmlElementRef(name = "altGlyphDef", namespace = "http://www.w3.org/2000/svg", type = AltGlyphDef.class),
        @XmlElementRef(name = "pattern", namespace = "http://www.w3.org/2000/svg", type = Pattern.class),
        @XmlElementRef(name = "cursor", namespace = "http://www.w3.org/2000/svg", type = Cursor.class),
        @XmlElementRef(name = "defs", namespace = "http://www.w3.org/2000/svg", type = Defs.class),
        @XmlElementRef(name = "font", namespace = "http://www.w3.org/2000/svg", type = Font.class),
        @XmlElementRef(name = "polyline", namespace = "http://www.w3.org/2000/svg", type = Polyline.class),
        @XmlElementRef(name = "animate", namespace = "http://www.w3.org/2000/svg", type = Animate.class),
        @XmlElementRef(name = "linearGradient", namespace = "http://www.w3.org/2000/svg", type = LinearGradient.class),
        @XmlElementRef(name = "mask", namespace = "http://www.w3.org/2000/svg", type = Mask.class),
        @XmlElementRef(name = "use", namespace = "http://www.w3.org/2000/svg", type = Use.class),
        @XmlElementRef(name = "filter", namespace = "http://www.w3.org/2000/svg", type = Filter.class),
        @XmlElementRef(name = "polygon", namespace = "http://www.w3.org/2000/svg", type = Polygon.class),
        @XmlElementRef(name = "set", namespace = "http://www.w3.org/2000/svg", type = Set.class),
        @XmlElementRef(name = "circle", namespace = "http://www.w3.org/2000/svg", type = Circle.class),
        @XmlElementRef(name = "marker", namespace = "http://www.w3.org/2000/svg", type = Marker.class),
        @XmlElementRef(name = "animateMotion", namespace = "http://www.w3.org/2000/svg", type = AnimateMotion.class),
        @XmlElementRef(name = "g", namespace = "http://www.w3.org/2000/svg", type = G.class),
        @XmlElementRef(name = "svg", namespace = "http://www.w3.org/2000/svg", type = Svg.class),
        @XmlElementRef(name = "text", namespace = "http://www.w3.org/2000/svg", type = Text.class)
    })
    @XmlMixed
    protected List<Object> content;
    @XmlAttribute
    @XmlSchemaType(name = "anySimpleType")
    protected String transform;
    @XmlAttribute
    @XmlJavaTypeAdapter(CollapsedStringAdapter.class)
    @XmlSchemaType(name = "NMTOKEN")
    protected String target;
    @XmlAttribute(name = "flood-color")
    @XmlSchemaType(name = "anySimpleType")
    protected String floodColor;
    @XmlAttribute(name = "flood-opacity")
    @XmlSchemaType(name = "anySimpleType")
    protected String floodOpacity;
    @XmlAttribute(name = "lighting-color")
    @XmlSchemaType(name = "anySimpleType")
    protected String lightingColor;

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
     * {@link FontFace }
     * {@link RadialGradient }
     * {@link Metadata }
     * {@link AnimateTransform }
     * {@link Style }
     * {@link Line }
     * {@link View }
     * {@link Switch }
     * {@link A }
     * {@link Path }
     * {@link ClipPath }
     * {@link Symbol }
     * {@link Desc }
     * {@link ColorProfile }
     * {@link Image }
     * {@link Ellipse }
     * {@link AnimateColor }
     * {@link Rect }
     * {@link Script }
     * {@link Title }
     * {@link String }
     * {@link Pattern }
     * {@link AltGlyphDef }
     * {@link Cursor }
     * {@link Defs }
     * {@link Font }
     * {@link Animate }
     * {@link Polyline }
     * {@link LinearGradient }
     * {@link Mask }
     * {@link Filter }
     * {@link Use }
     * {@link Polygon }
     * {@link Circle }
     * {@link Set }
     * {@link Marker }
     * {@link G }
     * {@link AnimateMotion }
     * {@link Svg }
     * {@link Text }
     * 
     * 
     */
    public List<Object> getContent() {
        if (content == null) {
            content = new ArrayList<Object>();
        }
        return this.content;
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
     * Gets the value of the target property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getTarget() {
        return target;
    }

    /**
     * Sets the value of the target property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setTarget(String value) {
        this.target = value;
    }

    /**
     * Gets the value of the floodColor property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getFloodColor() {
        return floodColor;
    }

    /**
     * Sets the value of the floodColor property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setFloodColor(String value) {
        this.floodColor = value;
    }

    /**
     * Gets the value of the floodOpacity property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getFloodOpacity() {
        return floodOpacity;
    }

    /**
     * Sets the value of the floodOpacity property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setFloodOpacity(String value) {
        this.floodOpacity = value;
    }

    /**
     * Gets the value of the lightingColor property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getLightingColor() {
        return lightingColor;
    }

    /**
     * Sets the value of the lightingColor property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setLightingColor(String value) {
        this.lightingColor = value;
    }

}
