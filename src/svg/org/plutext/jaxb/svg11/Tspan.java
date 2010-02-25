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

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlID;
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
 *     &lt;extension base="{http://www.w3.org/2000/svg}SVG.tspan.content">
 *       &lt;attGroup ref="{http://www.w3.org/2000/svg}SVG.Color.attrib"/>
 *       &lt;attGroup ref="{http://www.w3.org/2000/svg}SVG.GraphicalEvents.attrib"/>
 *       &lt;attGroup ref="{http://www.w3.org/2000/svg}SVG.Paint.attrib"/>
 *       &lt;attGroup ref="{http://www.w3.org/2000/svg}SVG.Style.attrib"/>
 *       &lt;attGroup ref="{http://www.w3.org/2000/svg}SVG.Font.attrib"/>
 *       &lt;attGroup ref="{http://www.w3.org/2000/svg}SVG.Filter.attrib"/>
 *       &lt;attGroup ref="{http://www.w3.org/2000/svg}SVG.Core.attrib"/>
 *       &lt;attGroup ref="{http://www.w3.org/2000/svg}SVG.External.attrib"/>
 *       &lt;attGroup ref="{http://www.w3.org/2000/svg}SVG.Graphics.attrib"/>
 *       &lt;attGroup ref="{http://www.w3.org/2000/svg}SVG.Clip.attrib"/>
 *       &lt;attGroup ref="{http://www.w3.org/2000/svg}SVG.Conditional.attrib"/>
 *       &lt;attGroup ref="{http://www.w3.org/2000/svg}SVG.TextContent.attrib"/>
 *       &lt;attGroup ref="{http://www.w3.org/2000/svg}SVG.Cursor.attrib"/>
 *       &lt;attGroup ref="{http://www.w3.org/2000/svg}SVG.Opacity.attrib"/>
 *       &lt;attGroup ref="{http://www.w3.org/2000/svg}SVG.Mask.attrib"/>
 *       &lt;attribute name="x" type="{http://www.w3.org/2000/svg}Coordinates.datatype" />
 *       &lt;attribute name="y" type="{http://www.w3.org/2000/svg}Coordinates.datatype" />
 *       &lt;attribute name="dx" type="{http://www.w3.org/2000/svg}Lengths.datatype" />
 *       &lt;attribute name="dy" type="{http://www.w3.org/2000/svg}Lengths.datatype" />
 *       &lt;attribute name="rotate" type="{http://www.w3.org/2000/svg}Numbers.datatype" />
 *       &lt;attribute name="textLength" type="{http://www.w3.org/2000/svg}Length.datatype" />
 *       &lt;attribute name="lengthAdjust">
 *         &lt;simpleType>
 *           &lt;restriction base="{http://www.w3.org/2001/XMLSchema}token">
 *             &lt;enumeration value="spacing"/>
 *             &lt;enumeration value="spacingAndGlyphs"/>
 *           &lt;/restriction>
 *         &lt;/simpleType>
 *       &lt;/attribute>
 *     &lt;/extension>
 *   &lt;/complexContent>
 * &lt;/complexType>
 * </pre>
 * 
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "")
public class Tspan
    extends SVGTspanContent
{

    @XmlAttribute
    protected String x;
    @XmlAttribute
    protected String y;
    @XmlAttribute
    protected String dx;
    @XmlAttribute
    protected String dy;
    @XmlAttribute
    protected String rotate;
    @XmlAttribute
    protected String textLength;
    @XmlAttribute
    @XmlJavaTypeAdapter(CollapsedStringAdapter.class)
    protected String lengthAdjust;
    @XmlAttribute(name = "color-interpolation")
    @XmlJavaTypeAdapter(CollapsedStringAdapter.class)
    protected String colorInterpolation;
    @XmlAttribute(name = "color-rendering")
    @XmlJavaTypeAdapter(CollapsedStringAdapter.class)
    protected String colorRendering;
    @XmlAttribute
    protected String color;
    @XmlAttribute
    protected String onload;
    @XmlAttribute
    protected String onmousemove;
    @XmlAttribute
    protected String onfocusin;
    @XmlAttribute
    protected String onfocusout;
    @XmlAttribute
    protected String onmousedown;
    @XmlAttribute
    protected String onmouseover;
    @XmlAttribute
    protected String onmouseout;
    @XmlAttribute
    protected String onmouseup;
    @XmlAttribute
    protected String onclick;
    @XmlAttribute
    protected String onactivate;
    @XmlAttribute
    protected String stroke;
    @XmlAttribute
    protected String fill;
    @XmlAttribute(name = "stroke-linejoin")
    @XmlJavaTypeAdapter(CollapsedStringAdapter.class)
    protected String strokeLinejoin;
    @XmlAttribute(name = "stroke-width")
    protected String strokeWidth;
    @XmlAttribute(name = "stroke-dashoffset")
    protected String strokeDashoffset;
    @XmlAttribute(name = "stroke-dasharray")
    protected String strokeDasharray;
    @XmlAttribute(name = "stroke-miterlimit")
    protected String strokeMiterlimit;
    @XmlAttribute(name = "fill-rule")
    protected ClipFillRuleDatatype fillRule;
    @XmlAttribute(name = "stroke-linecap")
    @XmlJavaTypeAdapter(CollapsedStringAdapter.class)
    protected String strokeLinecap;
    @XmlAttribute
    protected String style;
    @XmlAttribute(name = "class")
    protected String clazz;
    @XmlAttribute(name = "font-family")
    protected String fontFamily;
    @XmlAttribute(name = "font-size")
    protected String fontSize;
    @XmlAttribute(name = "font-size-adjust")
    protected String fontSizeAdjust;
    @XmlAttribute(name = "font-stretch")
    @XmlJavaTypeAdapter(CollapsedStringAdapter.class)
    protected String fontStretch;
    @XmlAttribute(name = "font-style")
    @XmlJavaTypeAdapter(CollapsedStringAdapter.class)
    protected String fontStyle;
    @XmlAttribute(name = "font-variant")
    @XmlJavaTypeAdapter(CollapsedStringAdapter.class)
    protected String fontVariant;
    @XmlAttribute(name = "font-weight")
    @XmlJavaTypeAdapter(CollapsedStringAdapter.class)
    protected String fontWeight;
    @XmlAttribute
    protected String filter;
    @XmlAttribute
    @XmlJavaTypeAdapter(CollapsedStringAdapter.class)
    @XmlID
    @XmlSchemaType(name = "ID")
    protected String id;
    @XmlAttribute(namespace = "http://www.w3.org/XML/1998/namespace")
    protected String base;
    @XmlAttribute(namespace = "http://www.w3.org/XML/1998/namespace", required = true)
    @XmlJavaTypeAdapter(CollapsedStringAdapter.class)
    protected String space;
    @XmlAttribute(namespace = "http://www.w3.org/XML/1998/namespace")
    @XmlJavaTypeAdapter(CollapsedStringAdapter.class)
    protected String lang;
    @XmlAttribute
    protected BooleanDatatype externalResourcesRequired;
    @XmlAttribute
    @XmlJavaTypeAdapter(CollapsedStringAdapter.class)
    protected String visibility;
    @XmlAttribute(name = "pointer-events")
    @XmlJavaTypeAdapter(CollapsedStringAdapter.class)
    protected String pointerEvents;
    @XmlAttribute(name = "text-rendering")
    @XmlJavaTypeAdapter(CollapsedStringAdapter.class)
    protected String textRendering;
    @XmlAttribute
    @XmlJavaTypeAdapter(CollapsedStringAdapter.class)
    protected String display;
    @XmlAttribute(name = "image-rendering")
    @XmlJavaTypeAdapter(CollapsedStringAdapter.class)
    protected String imageRendering;
    @XmlAttribute(name = "shape-rendering")
    @XmlJavaTypeAdapter(CollapsedStringAdapter.class)
    protected String shapeRendering;
    @XmlAttribute(name = "clip-path")
    protected String clipPath;
    @XmlAttribute(name = "clip-rule")
    protected ClipFillRuleDatatype clipRule;
    @XmlAttribute
    protected String requiredFeatures;
    @XmlAttribute
    protected String requiredExtensions;
    @XmlAttribute
    protected String systemLanguage;
    @XmlAttribute(name = "alignment-baseline")
    @XmlJavaTypeAdapter(CollapsedStringAdapter.class)
    protected String alignmentBaseline;
    @XmlAttribute(name = "baseline-shift")
    protected String baselineShift;
    @XmlAttribute
    @XmlJavaTypeAdapter(CollapsedStringAdapter.class)
    protected String direction;
    @XmlAttribute(name = "dominant-baseline")
    @XmlJavaTypeAdapter(CollapsedStringAdapter.class)
    protected String dominantBaseline;
    @XmlAttribute(name = "glyph-orientation-horizontal")
    protected String glyphOrientationHorizontal;
    @XmlAttribute(name = "glyph-orientation-vertical")
    protected String glyphOrientationVertical;
    @XmlAttribute
    protected String kerning;
    @XmlAttribute(name = "letter-spacing")
    protected String letterSpacing;
    @XmlAttribute(name = "text-anchor")
    @XmlJavaTypeAdapter(CollapsedStringAdapter.class)
    protected String textAnchor;
    @XmlAttribute(name = "text-decoration")
    protected String textDecoration;
    @XmlAttribute(name = "unicode-bidi")
    @XmlJavaTypeAdapter(CollapsedStringAdapter.class)
    protected String unicodeBidi;
    @XmlAttribute(name = "word-spacing")
    protected String wordSpacing;
    @XmlAttribute
    protected String cursor;
    @XmlAttribute(name = "stroke-opacity")
    protected String strokeOpacity;
    @XmlAttribute(name = "fill-opacity")
    protected String fillOpacity;
    @XmlAttribute
    protected String opacity;
    @XmlAttribute
    protected String mask;

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
     * Gets the value of the dx property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getDx() {
        return dx;
    }

    /**
     * Sets the value of the dx property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setDx(String value) {
        this.dx = value;
    }

    /**
     * Gets the value of the dy property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getDy() {
        return dy;
    }

    /**
     * Sets the value of the dy property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setDy(String value) {
        this.dy = value;
    }

    /**
     * Gets the value of the rotate property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getRotate() {
        return rotate;
    }

    /**
     * Sets the value of the rotate property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setRotate(String value) {
        this.rotate = value;
    }

    /**
     * Gets the value of the textLength property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getTextLength() {
        return textLength;
    }

    /**
     * Sets the value of the textLength property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setTextLength(String value) {
        this.textLength = value;
    }

    /**
     * Gets the value of the lengthAdjust property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getLengthAdjust() {
        return lengthAdjust;
    }

    /**
     * Sets the value of the lengthAdjust property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setLengthAdjust(String value) {
        this.lengthAdjust = value;
    }

    /**
     * Gets the value of the colorInterpolation property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getColorInterpolation() {
        return colorInterpolation;
    }

    /**
     * Sets the value of the colorInterpolation property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setColorInterpolation(String value) {
        this.colorInterpolation = value;
    }

    /**
     * Gets the value of the colorRendering property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getColorRendering() {
        return colorRendering;
    }

    /**
     * Sets the value of the colorRendering property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setColorRendering(String value) {
        this.colorRendering = value;
    }

    /**
     * Gets the value of the color property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getColor() {
        return color;
    }

    /**
     * Sets the value of the color property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setColor(String value) {
        this.color = value;
    }

    /**
     * Gets the value of the onload property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getOnload() {
        return onload;
    }

    /**
     * Sets the value of the onload property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setOnload(String value) {
        this.onload = value;
    }

    /**
     * Gets the value of the onmousemove property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getOnmousemove() {
        return onmousemove;
    }

    /**
     * Sets the value of the onmousemove property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setOnmousemove(String value) {
        this.onmousemove = value;
    }

    /**
     * Gets the value of the onfocusin property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getOnfocusin() {
        return onfocusin;
    }

    /**
     * Sets the value of the onfocusin property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setOnfocusin(String value) {
        this.onfocusin = value;
    }

    /**
     * Gets the value of the onfocusout property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getOnfocusout() {
        return onfocusout;
    }

    /**
     * Sets the value of the onfocusout property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setOnfocusout(String value) {
        this.onfocusout = value;
    }

    /**
     * Gets the value of the onmousedown property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getOnmousedown() {
        return onmousedown;
    }

    /**
     * Sets the value of the onmousedown property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setOnmousedown(String value) {
        this.onmousedown = value;
    }

    /**
     * Gets the value of the onmouseover property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getOnmouseover() {
        return onmouseover;
    }

    /**
     * Sets the value of the onmouseover property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setOnmouseover(String value) {
        this.onmouseover = value;
    }

    /**
     * Gets the value of the onmouseout property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getOnmouseout() {
        return onmouseout;
    }

    /**
     * Sets the value of the onmouseout property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setOnmouseout(String value) {
        this.onmouseout = value;
    }

    /**
     * Gets the value of the onmouseup property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getOnmouseup() {
        return onmouseup;
    }

    /**
     * Sets the value of the onmouseup property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setOnmouseup(String value) {
        this.onmouseup = value;
    }

    /**
     * Gets the value of the onclick property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getOnclick() {
        return onclick;
    }

    /**
     * Sets the value of the onclick property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setOnclick(String value) {
        this.onclick = value;
    }

    /**
     * Gets the value of the onactivate property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getOnactivate() {
        return onactivate;
    }

    /**
     * Sets the value of the onactivate property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setOnactivate(String value) {
        this.onactivate = value;
    }

    /**
     * Gets the value of the stroke property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getStroke() {
        return stroke;
    }

    /**
     * Sets the value of the stroke property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setStroke(String value) {
        this.stroke = value;
    }

    /**
     * Gets the value of the fill property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getFill() {
        return fill;
    }

    /**
     * Sets the value of the fill property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setFill(String value) {
        this.fill = value;
    }

    /**
     * Gets the value of the strokeLinejoin property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getStrokeLinejoin() {
        return strokeLinejoin;
    }

    /**
     * Sets the value of the strokeLinejoin property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setStrokeLinejoin(String value) {
        this.strokeLinejoin = value;
    }

    /**
     * Gets the value of the strokeWidth property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getStrokeWidth() {
        return strokeWidth;
    }

    /**
     * Sets the value of the strokeWidth property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setStrokeWidth(String value) {
        this.strokeWidth = value;
    }

    /**
     * Gets the value of the strokeDashoffset property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getStrokeDashoffset() {
        return strokeDashoffset;
    }

    /**
     * Sets the value of the strokeDashoffset property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setStrokeDashoffset(String value) {
        this.strokeDashoffset = value;
    }

    /**
     * Gets the value of the strokeDasharray property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getStrokeDasharray() {
        return strokeDasharray;
    }

    /**
     * Sets the value of the strokeDasharray property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setStrokeDasharray(String value) {
        this.strokeDasharray = value;
    }

    /**
     * Gets the value of the strokeMiterlimit property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getStrokeMiterlimit() {
        return strokeMiterlimit;
    }

    /**
     * Sets the value of the strokeMiterlimit property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setStrokeMiterlimit(String value) {
        this.strokeMiterlimit = value;
    }

    /**
     * Gets the value of the fillRule property.
     * 
     * @return
     *     possible object is
     *     {@link ClipFillRuleDatatype }
     *     
     */
    public ClipFillRuleDatatype getFillRule() {
        return fillRule;
    }

    /**
     * Sets the value of the fillRule property.
     * 
     * @param value
     *     allowed object is
     *     {@link ClipFillRuleDatatype }
     *     
     */
    public void setFillRule(ClipFillRuleDatatype value) {
        this.fillRule = value;
    }

    /**
     * Gets the value of the strokeLinecap property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getStrokeLinecap() {
        return strokeLinecap;
    }

    /**
     * Sets the value of the strokeLinecap property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setStrokeLinecap(String value) {
        this.strokeLinecap = value;
    }

    /**
     * Gets the value of the style property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getStyle() {
        return style;
    }

    /**
     * Sets the value of the style property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setStyle(String value) {
        this.style = value;
    }

    /**
     * Gets the value of the clazz property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getClazz() {
        return clazz;
    }

    /**
     * Sets the value of the clazz property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setClazz(String value) {
        this.clazz = value;
    }

    /**
     * Gets the value of the fontFamily property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getFontFamily() {
        return fontFamily;
    }

    /**
     * Sets the value of the fontFamily property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setFontFamily(String value) {
        this.fontFamily = value;
    }

    /**
     * Gets the value of the fontSize property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getFontSize() {
        return fontSize;
    }

    /**
     * Sets the value of the fontSize property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setFontSize(String value) {
        this.fontSize = value;
    }

    /**
     * Gets the value of the fontSizeAdjust property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getFontSizeAdjust() {
        return fontSizeAdjust;
    }

    /**
     * Sets the value of the fontSizeAdjust property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setFontSizeAdjust(String value) {
        this.fontSizeAdjust = value;
    }

    /**
     * Gets the value of the fontStretch property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getFontStretch() {
        return fontStretch;
    }

    /**
     * Sets the value of the fontStretch property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setFontStretch(String value) {
        this.fontStretch = value;
    }

    /**
     * Gets the value of the fontStyle property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getFontStyle() {
        return fontStyle;
    }

    /**
     * Sets the value of the fontStyle property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setFontStyle(String value) {
        this.fontStyle = value;
    }

    /**
     * Gets the value of the fontVariant property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getFontVariant() {
        return fontVariant;
    }

    /**
     * Sets the value of the fontVariant property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setFontVariant(String value) {
        this.fontVariant = value;
    }

    /**
     * Gets the value of the fontWeight property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getFontWeight() {
        return fontWeight;
    }

    /**
     * Sets the value of the fontWeight property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setFontWeight(String value) {
        this.fontWeight = value;
    }

    /**
     * Gets the value of the filter property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getFilter() {
        return filter;
    }

    /**
     * Sets the value of the filter property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setFilter(String value) {
        this.filter = value;
    }

    /**
     * Gets the value of the id property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getId() {
        return id;
    }

    /**
     * Sets the value of the id property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setId(String value) {
        this.id = value;
    }

    /**
     * Gets the value of the base property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getBase() {
        return base;
    }

    /**
     * Sets the value of the base property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setBase(String value) {
        this.base = value;
    }

    /**
     * Gets the value of the space property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getSpace() {
        return space;
    }

    /**
     * Sets the value of the space property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setSpace(String value) {
        this.space = value;
    }

    /**
     * Gets the value of the lang property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getLang() {
        return lang;
    }

    /**
     * Sets the value of the lang property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setLang(String value) {
        this.lang = value;
    }

    /**
     * Gets the value of the externalResourcesRequired property.
     * 
     * @return
     *     possible object is
     *     {@link BooleanDatatype }
     *     
     */
    public BooleanDatatype getExternalResourcesRequired() {
        return externalResourcesRequired;
    }

    /**
     * Sets the value of the externalResourcesRequired property.
     * 
     * @param value
     *     allowed object is
     *     {@link BooleanDatatype }
     *     
     */
    public void setExternalResourcesRequired(BooleanDatatype value) {
        this.externalResourcesRequired = value;
    }

    /**
     * Gets the value of the visibility property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getVisibility() {
        return visibility;
    }

    /**
     * Sets the value of the visibility property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setVisibility(String value) {
        this.visibility = value;
    }

    /**
     * Gets the value of the pointerEvents property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getPointerEvents() {
        return pointerEvents;
    }

    /**
     * Sets the value of the pointerEvents property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setPointerEvents(String value) {
        this.pointerEvents = value;
    }

    /**
     * Gets the value of the textRendering property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getTextRendering() {
        return textRendering;
    }

    /**
     * Sets the value of the textRendering property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setTextRendering(String value) {
        this.textRendering = value;
    }

    /**
     * Gets the value of the display property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getDisplay() {
        return display;
    }

    /**
     * Sets the value of the display property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setDisplay(String value) {
        this.display = value;
    }

    /**
     * Gets the value of the imageRendering property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getImageRendering() {
        return imageRendering;
    }

    /**
     * Sets the value of the imageRendering property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setImageRendering(String value) {
        this.imageRendering = value;
    }

    /**
     * Gets the value of the shapeRendering property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getShapeRendering() {
        return shapeRendering;
    }

    /**
     * Sets the value of the shapeRendering property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setShapeRendering(String value) {
        this.shapeRendering = value;
    }

    /**
     * Gets the value of the clipPath property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getClipPath() {
        return clipPath;
    }

    /**
     * Sets the value of the clipPath property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setClipPath(String value) {
        this.clipPath = value;
    }

    /**
     * Gets the value of the clipRule property.
     * 
     * @return
     *     possible object is
     *     {@link ClipFillRuleDatatype }
     *     
     */
    public ClipFillRuleDatatype getClipRule() {
        return clipRule;
    }

    /**
     * Sets the value of the clipRule property.
     * 
     * @param value
     *     allowed object is
     *     {@link ClipFillRuleDatatype }
     *     
     */
    public void setClipRule(ClipFillRuleDatatype value) {
        this.clipRule = value;
    }

    /**
     * Gets the value of the requiredFeatures property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getRequiredFeatures() {
        return requiredFeatures;
    }

    /**
     * Sets the value of the requiredFeatures property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setRequiredFeatures(String value) {
        this.requiredFeatures = value;
    }

    /**
     * Gets the value of the requiredExtensions property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getRequiredExtensions() {
        return requiredExtensions;
    }

    /**
     * Sets the value of the requiredExtensions property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setRequiredExtensions(String value) {
        this.requiredExtensions = value;
    }

    /**
     * Gets the value of the systemLanguage property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getSystemLanguage() {
        return systemLanguage;
    }

    /**
     * Sets the value of the systemLanguage property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setSystemLanguage(String value) {
        this.systemLanguage = value;
    }

    /**
     * Gets the value of the alignmentBaseline property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getAlignmentBaseline() {
        return alignmentBaseline;
    }

    /**
     * Sets the value of the alignmentBaseline property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setAlignmentBaseline(String value) {
        this.alignmentBaseline = value;
    }

    /**
     * Gets the value of the baselineShift property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getBaselineShift() {
        return baselineShift;
    }

    /**
     * Sets the value of the baselineShift property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setBaselineShift(String value) {
        this.baselineShift = value;
    }

    /**
     * Gets the value of the direction property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getDirection() {
        return direction;
    }

    /**
     * Sets the value of the direction property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setDirection(String value) {
        this.direction = value;
    }

    /**
     * Gets the value of the dominantBaseline property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getDominantBaseline() {
        return dominantBaseline;
    }

    /**
     * Sets the value of the dominantBaseline property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setDominantBaseline(String value) {
        this.dominantBaseline = value;
    }

    /**
     * Gets the value of the glyphOrientationHorizontal property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getGlyphOrientationHorizontal() {
        return glyphOrientationHorizontal;
    }

    /**
     * Sets the value of the glyphOrientationHorizontal property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setGlyphOrientationHorizontal(String value) {
        this.glyphOrientationHorizontal = value;
    }

    /**
     * Gets the value of the glyphOrientationVertical property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getGlyphOrientationVertical() {
        return glyphOrientationVertical;
    }

    /**
     * Sets the value of the glyphOrientationVertical property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setGlyphOrientationVertical(String value) {
        this.glyphOrientationVertical = value;
    }

    /**
     * Gets the value of the kerning property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getKerning() {
        return kerning;
    }

    /**
     * Sets the value of the kerning property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setKerning(String value) {
        this.kerning = value;
    }

    /**
     * Gets the value of the letterSpacing property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getLetterSpacing() {
        return letterSpacing;
    }

    /**
     * Sets the value of the letterSpacing property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setLetterSpacing(String value) {
        this.letterSpacing = value;
    }

    /**
     * Gets the value of the textAnchor property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getTextAnchor() {
        return textAnchor;
    }

    /**
     * Sets the value of the textAnchor property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setTextAnchor(String value) {
        this.textAnchor = value;
    }

    /**
     * Gets the value of the textDecoration property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getTextDecoration() {
        return textDecoration;
    }

    /**
     * Sets the value of the textDecoration property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setTextDecoration(String value) {
        this.textDecoration = value;
    }

    /**
     * Gets the value of the unicodeBidi property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getUnicodeBidi() {
        return unicodeBidi;
    }

    /**
     * Sets the value of the unicodeBidi property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setUnicodeBidi(String value) {
        this.unicodeBidi = value;
    }

    /**
     * Gets the value of the wordSpacing property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getWordSpacing() {
        return wordSpacing;
    }

    /**
     * Sets the value of the wordSpacing property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setWordSpacing(String value) {
        this.wordSpacing = value;
    }

    /**
     * Gets the value of the cursor property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getCursor() {
        return cursor;
    }

    /**
     * Sets the value of the cursor property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setCursor(String value) {
        this.cursor = value;
    }

    /**
     * Gets the value of the strokeOpacity property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getStrokeOpacity() {
        return strokeOpacity;
    }

    /**
     * Sets the value of the strokeOpacity property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setStrokeOpacity(String value) {
        this.strokeOpacity = value;
    }

    /**
     * Gets the value of the fillOpacity property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getFillOpacity() {
        return fillOpacity;
    }

    /**
     * Sets the value of the fillOpacity property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setFillOpacity(String value) {
        this.fillOpacity = value;
    }

    /**
     * Gets the value of the opacity property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getOpacity() {
        return opacity;
    }

    /**
     * Sets the value of the opacity property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setOpacity(String value) {
        this.opacity = value;
    }

    /**
     * Gets the value of the mask property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getMask() {
        return mask;
    }

    /**
     * Sets the value of the mask property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setMask(String value) {
        this.mask = value;
    }

}
