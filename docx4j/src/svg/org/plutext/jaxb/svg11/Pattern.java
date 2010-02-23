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
 *       &lt;attribute name="x" type="{http://www.w3.org/2001/XMLSchema}anySimpleType" />
 *       &lt;attribute name="y" type="{http://www.w3.org/2001/XMLSchema}anySimpleType" />
 *       &lt;attribute name="width" type="{http://www.w3.org/2001/XMLSchema}anySimpleType" />
 *       &lt;attribute name="height" type="{http://www.w3.org/2001/XMLSchema}anySimpleType" />
 *       &lt;attribute name="patternUnits">
 *         &lt;simpleType>
 *           &lt;restriction base="{http://www.w3.org/2001/XMLSchema}string">
 *             &lt;enumeration value="userSpaceOnUse"/>
 *             &lt;enumeration value="objectBoundingBox"/>
 *           &lt;/restriction>
 *         &lt;/simpleType>
 *       &lt;/attribute>
 *       &lt;attribute name="patternContentUnits">
 *         &lt;simpleType>
 *           &lt;restriction base="{http://www.w3.org/2001/XMLSchema}string">
 *             &lt;enumeration value="userSpaceOnUse"/>
 *             &lt;enumeration value="objectBoundingBox"/>
 *           &lt;/restriction>
 *         &lt;/simpleType>
 *       &lt;/attribute>
 *       &lt;attribute name="patternTransform" type="{http://www.w3.org/2001/XMLSchema}anySimpleType" />
 *       &lt;attribute name="viewBox" type="{http://www.w3.org/2001/XMLSchema}anySimpleType" />
 *       &lt;attribute name="preserveAspectRatio" type="{http://www.w3.org/2001/XMLSchema}anySimpleType" default="xMidYMid meet" />
 *     &lt;/restriction>
 *   &lt;/complexContent>
 * &lt;/complexType>
 * </pre>
 * 
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "", propOrder = {
    "descOrTitleOrMetadata"
})
@XmlRootElement(name = "pattern")
public class Pattern {

    @XmlElements({
        @XmlElement(name = "font-face", type = FontFace.class),
        @XmlElement(name = "animateTransform", type = AnimateTransform.class),
        @XmlElement(name = "cursor", type = Cursor.class),
        @XmlElement(name = "script", type = Script.class),
        @XmlElement(name = "marker", type = Marker.class),
        @XmlElement(name = "line", type = Line.class),
        @XmlElement(name = "pattern", type = Pattern.class),
        @XmlElement(name = "altGlyphDef", type = AltGlyphDef.class),
        @XmlElement(name = "mask", type = Mask.class),
        @XmlElement(name = "svg", type = Svg.class),
        @XmlElement(name = "symbol", type = Symbol.class),
        @XmlElement(name = "circle", type = Circle.class),
        @XmlElement(name = "ellipse", type = Ellipse.class),
        @XmlElement(name = "set", type = Set.class),
        @XmlElement(name = "desc", type = Desc.class),
        @XmlElement(name = "use", type = Use.class),
        @XmlElement(name = "switch", type = Switch.class),
        @XmlElement(name = "polyline", type = Polyline.class),
        @XmlElement(name = "rect", type = Rect.class),
        @XmlElement(name = "filter", type = Filter.class),
        @XmlElement(name = "g", type = G.class),
        @XmlElement(name = "style", type = Style.class),
        @XmlElement(name = "path", type = Path.class),
        @XmlElement(name = "polygon", type = Polygon.class),
        @XmlElement(name = "metadata", type = Metadata.class),
        @XmlElement(name = "text", type = Text.class),
        @XmlElement(name = "linearGradient", type = LinearGradient.class),
        @XmlElement(name = "defs", type = Defs.class),
        @XmlElement(name = "title", type = Title.class),
        @XmlElement(name = "radialGradient", type = RadialGradient.class),
        @XmlElement(name = "image", type = Image.class),
        @XmlElement(name = "color-profile", type = ColorProfile.class),
        @XmlElement(name = "font", type = Font.class),
        @XmlElement(name = "clipPath", type = ClipPath.class),
        @XmlElement(name = "animate", type = Animate.class),
        @XmlElement(name = "a", type = A.class),
        @XmlElement(name = "animateMotion", type = AnimateMotion.class),
        @XmlElement(name = "animateColor", type = AnimateColor.class),
        @XmlElement(name = "view", type = View.class)
    })
    protected List<Object> descOrTitleOrMetadata;
    @XmlAttribute
    @XmlSchemaType(name = "anySimpleType")
    protected String x;
    @XmlAttribute
    @XmlSchemaType(name = "anySimpleType")
    protected String y;
    @XmlAttribute
    @XmlSchemaType(name = "anySimpleType")
    protected String width;
    @XmlAttribute
    @XmlSchemaType(name = "anySimpleType")
    protected String height;
    @XmlAttribute
    protected String patternUnits;
    @XmlAttribute
    protected String patternContentUnits;
    @XmlAttribute
    @XmlSchemaType(name = "anySimpleType")
    protected String patternTransform;
    @XmlAttribute
    @XmlSchemaType(name = "anySimpleType")
    protected String viewBox;
    @XmlAttribute
    @XmlSchemaType(name = "anySimpleType")
    protected String preserveAspectRatio;
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
     * {@link FontFace }
     * {@link AnimateTransform }
     * {@link Cursor }
     * {@link Script }
     * {@link Marker }
     * {@link Line }
     * {@link Pattern }
     * {@link AltGlyphDef }
     * {@link Mask }
     * {@link Svg }
     * {@link Symbol }
     * {@link Circle }
     * {@link Ellipse }
     * {@link Set }
     * {@link Desc }
     * {@link Use }
     * {@link Switch }
     * {@link Polyline }
     * {@link Rect }
     * {@link Filter }
     * {@link G }
     * {@link Style }
     * {@link Path }
     * {@link Polygon }
     * {@link Metadata }
     * {@link Text }
     * {@link LinearGradient }
     * {@link Defs }
     * {@link Title }
     * {@link RadialGradient }
     * {@link Image }
     * {@link ColorProfile }
     * {@link Font }
     * {@link ClipPath }
     * {@link Animate }
     * {@link A }
     * {@link AnimateMotion }
     * {@link AnimateColor }
     * {@link View }
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
     * Gets the value of the x property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getX() {
        return x;
    }

    /**
     * Sets the value of the x property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setX(String value) {
        this.x = value;
    }

    /**
     * Gets the value of the y property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getY() {
        return y;
    }

    /**
     * Sets the value of the y property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setY(String value) {
        this.y = value;
    }

    /**
     * Gets the value of the width property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getWidth() {
        return width;
    }

    /**
     * Sets the value of the width property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setWidth(String value) {
        this.width = value;
    }

    /**
     * Gets the value of the height property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getHeight() {
        return height;
    }

    /**
     * Sets the value of the height property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setHeight(String value) {
        this.height = value;
    }

    /**
     * Gets the value of the patternUnits property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getPatternUnits() {
        return patternUnits;
    }

    /**
     * Sets the value of the patternUnits property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setPatternUnits(String value) {
        this.patternUnits = value;
    }

    /**
     * Gets the value of the patternContentUnits property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getPatternContentUnits() {
        return patternContentUnits;
    }

    /**
     * Sets the value of the patternContentUnits property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setPatternContentUnits(String value) {
        this.patternContentUnits = value;
    }

    /**
     * Gets the value of the patternTransform property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getPatternTransform() {
        return patternTransform;
    }

    /**
     * Sets the value of the patternTransform property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setPatternTransform(String value) {
        this.patternTransform = value;
    }

    /**
     * Gets the value of the viewBox property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getViewBox() {
        return viewBox;
    }

    /**
     * Sets the value of the viewBox property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setViewBox(String value) {
        this.viewBox = value;
    }

    /**
     * Gets the value of the preserveAspectRatio property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getPreserveAspectRatio() {
        if (preserveAspectRatio == null) {
            return "xMidYMid meet";
        } else {
            return preserveAspectRatio;
        }
    }

    /**
     * Sets the value of the preserveAspectRatio property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setPreserveAspectRatio(String value) {
        this.preserveAspectRatio = value;
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
