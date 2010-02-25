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
 * <p>Java class for SVG.missing-glyph.content complex type.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
 * 
 * <pre>
 * &lt;complexType name="SVG.missing-glyph.content">
 *   &lt;complexContent>
 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
 *       &lt;choice maxOccurs="unbounded" minOccurs="0">
 *         &lt;element ref="{http://www.w3.org/2000/svg}SVG.Description.class"/>
 *         &lt;element ref="{http://www.w3.org/2000/svg}SVG.Animation.class"/>
 *         &lt;element ref="{http://www.w3.org/2000/svg}SVG.Structure.class"/>
 *         &lt;element ref="{http://www.w3.org/2000/svg}SVG.Conditional.class"/>
 *         &lt;element ref="{http://www.w3.org/2000/svg}SVG.Image.class"/>
 *         &lt;element ref="{http://www.w3.org/2000/svg}SVG.Style.class"/>
 *         &lt;element ref="{http://www.w3.org/2000/svg}SVG.Shape.class"/>
 *         &lt;element ref="{http://www.w3.org/2000/svg}SVG.Text.class"/>
 *         &lt;element ref="{http://www.w3.org/2000/svg}SVG.Marker.class"/>
 *         &lt;element ref="{http://www.w3.org/2000/svg}SVG.ColorProfile.class"/>
 *         &lt;element ref="{http://www.w3.org/2000/svg}SVG.Gradient.class"/>
 *         &lt;element ref="{http://www.w3.org/2000/svg}SVG.Pattern.class"/>
 *         &lt;element ref="{http://www.w3.org/2000/svg}clipPath"/>
 *         &lt;element ref="{http://www.w3.org/2000/svg}SVG.Mask.class"/>
 *         &lt;element ref="{http://www.w3.org/2000/svg}SVG.Filter.class"/>
 *         &lt;element ref="{http://www.w3.org/2000/svg}cursor"/>
 *         &lt;element ref="{http://www.w3.org/2000/svg}SVG.Hyperlink.class"/>
 *         &lt;element ref="{http://www.w3.org/2000/svg}SVG.View.class"/>
 *         &lt;element ref="{http://www.w3.org/2000/svg}SVG.Script.class"/>
 *         &lt;element ref="{http://www.w3.org/2000/svg}SVG.Font.class"/>
 *       &lt;/choice>
 *     &lt;/restriction>
 *   &lt;/complexContent>
 * &lt;/complexType>
 * </pre>
 * 
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "SVG.missing-glyph.content", propOrder = {
    "svgDescriptionClassOrSVGAnimationClassOrSVGStructureClass"
})
public class SVGMissingGlyphContent {

    @XmlElementRefs({
        @XmlElementRef(name = "SVG.Mask.class", namespace = "http://www.w3.org/2000/svg", type = JAXBElement.class),
        @XmlElementRef(name = "SVG.Structure.class", namespace = "http://www.w3.org/2000/svg", type = JAXBElement.class),
        @XmlElementRef(name = "SVG.Shape.class", namespace = "http://www.w3.org/2000/svg", type = JAXBElement.class),
        @XmlElementRef(name = "clipPath", namespace = "http://www.w3.org/2000/svg", type = ClipPath.class),
        @XmlElementRef(name = "SVG.Hyperlink.class", namespace = "http://www.w3.org/2000/svg", type = JAXBElement.class),
        @XmlElementRef(name = "SVG.Animation.class", namespace = "http://www.w3.org/2000/svg", type = JAXBElement.class),
        @XmlElementRef(name = "SVG.Pattern.class", namespace = "http://www.w3.org/2000/svg", type = JAXBElement.class),
        @XmlElementRef(name = "SVG.Image.class", namespace = "http://www.w3.org/2000/svg", type = JAXBElement.class),
        @XmlElementRef(name = "SVG.Text.class", namespace = "http://www.w3.org/2000/svg", type = JAXBElement.class),
        @XmlElementRef(name = "SVG.Style.class", namespace = "http://www.w3.org/2000/svg", type = JAXBElement.class),
        @XmlElementRef(name = "SVG.Gradient.class", namespace = "http://www.w3.org/2000/svg", type = JAXBElement.class),
        @XmlElementRef(name = "SVG.Filter.class", namespace = "http://www.w3.org/2000/svg", type = JAXBElement.class),
        @XmlElementRef(name = "SVG.Conditional.class", namespace = "http://www.w3.org/2000/svg", type = JAXBElement.class),
        @XmlElementRef(name = "SVG.ColorProfile.class", namespace = "http://www.w3.org/2000/svg", type = JAXBElement.class),
        @XmlElementRef(name = "SVG.Script.class", namespace = "http://www.w3.org/2000/svg", type = JAXBElement.class),
        @XmlElementRef(name = "SVG.Font.class", namespace = "http://www.w3.org/2000/svg", type = JAXBElement.class),
        @XmlElementRef(name = "SVG.Description.class", namespace = "http://www.w3.org/2000/svg", type = JAXBElement.class),
        @XmlElementRef(name = "SVG.View.class", namespace = "http://www.w3.org/2000/svg", type = JAXBElement.class),
        @XmlElementRef(name = "cursor", namespace = "http://www.w3.org/2000/svg", type = Cursor.class),
        @XmlElementRef(name = "SVG.Marker.class", namespace = "http://www.w3.org/2000/svg", type = JAXBElement.class)
    })
    protected List<Object> svgDescriptionClassOrSVGAnimationClassOrSVGStructureClass;

    /**
     * Gets the value of the svgDescriptionClassOrSVGAnimationClassOrSVGStructureClass property.
     * 
     * <p>
     * This accessor method returns a reference to the live list,
     * not a snapshot. Therefore any modification you make to the
     * returned list will be present inside the JAXB object.
     * This is why there is not a <CODE>set</CODE> method for the svgDescriptionClassOrSVGAnimationClassOrSVGStructureClass property.
     * 
     * <p>
     * For example, to add a new item, do as follows:
     * <pre>
     *    getSVGDescriptionClassOrSVGAnimationClassOrSVGStructureClass().add(newItem);
     * </pre>
     * 
     * 
     * <p>
     * Objects of the following type(s) are allowed in the list
     * {@link JAXBElement }{@code <}{@link Animate }{@code >}
     * {@link JAXBElement }{@code <}{@link Object }{@code >}
     * {@link JAXBElement }{@code <}{@link SVGHyperlinkClass }{@code >}
     * {@link JAXBElement }{@code <}{@link Path }{@code >}
     * {@link JAXBElement }{@code <}{@link Object }{@code >}
     * {@link JAXBElement }{@code <}{@link Polygon }{@code >}
     * {@link JAXBElement }{@code <}{@link SVGMarkerClass }{@code >}
     * {@link JAXBElement }{@code <}{@link G }{@code >}
     * {@link JAXBElement }{@code <}{@link AnimateColor }{@code >}
     * {@link JAXBElement }{@code <}{@link SVGStyleClass }{@code >}
     * {@link JAXBElement }{@code <}{@link Object }{@code >}
     * {@link JAXBElement }{@code <}{@link SVGFilterClass }{@code >}
     * {@link JAXBElement }{@code <}{@link Defs }{@code >}
     * {@link JAXBElement }{@code <}{@link AltGlyphDef }{@code >}
     * {@link JAXBElement }{@code <}{@link RadialGradient }{@code >}
     * {@link JAXBElement }{@code <}{@link Object }{@code >}
     * {@link JAXBElement }{@code <}{@link SVGStyleClass }{@code >}
     * {@link JAXBElement }{@code <}{@link Rect }{@code >}
     * {@link JAXBElement }{@code <}{@link SVGUseClass }{@code >}
     * {@link ClipPath }
     * {@link JAXBElement }{@code <}{@link SVGPatternClass }{@code >}
     * {@link JAXBElement }{@code <}{@link SVGImageClass }{@code >}
     * {@link JAXBElement }{@code <}{@link SVGMaskClass }{@code >}
     * {@link JAXBElement }{@code <}{@link SVGFilterClass }{@code >}
     * {@link JAXBElement }{@code <}{@link SVGConditionalClass }{@code >}
     * {@link JAXBElement }{@code <}{@link LinearGradient }{@code >}
     * {@link JAXBElement }{@code <}{@link SVGViewClass }{@code >}
     * {@link JAXBElement }{@code <}{@link SVGMarkerClass }{@code >}
     * {@link JAXBElement }{@code <}{@link SVGPatternClass }{@code >}
     * {@link JAXBElement }{@code <}{@link Text }{@code >}
     * {@link JAXBElement }{@code <}{@link Svg }{@code >}
     * {@link JAXBElement }{@code <}{@link SVGMaskClass }{@code >}
     * {@link JAXBElement }{@code <}{@link SVGScriptClass }{@code >}
     * {@link JAXBElement }{@code <}{@link Circle }{@code >}
     * {@link JAXBElement }{@code <}{@link Metadata }{@code >}
     * {@link JAXBElement }{@code <}{@link Line }{@code >}
     * {@link JAXBElement }{@code <}{@link Ellipse }{@code >}
     * {@link JAXBElement }{@code <}{@link Desc }{@code >}
     * {@link JAXBElement }{@code <}{@link Polyline }{@code >}
     * {@link JAXBElement }{@code <}{@link SVGUseClass }{@code >}
     * {@link JAXBElement }{@code <}{@link SVGColorProfileClass }{@code >}
     * {@link JAXBElement }{@code <}{@link SVGScriptClass }{@code >}
     * {@link JAXBElement }{@code <}{@link Set }{@code >}
     * {@link JAXBElement }{@code <}{@link SVGViewClass }{@code >}
     * {@link JAXBElement }{@code <}{@link AnimateTransform }{@code >}
     * {@link JAXBElement }{@code <}{@link SVGColorProfileClass }{@code >}
     * {@link JAXBElement }{@code <}{@link FontFace }{@code >}
     * {@link JAXBElement }{@code <}{@link SVGImageClass }{@code >}
     * {@link JAXBElement }{@code <}{@link Object }{@code >}
     * {@link JAXBElement }{@code <}{@link SVGConditionalClass }{@code >}
     * {@link JAXBElement }{@code <}{@link Title }{@code >}
     * {@link JAXBElement }{@code <}{@link Object }{@code >}
     * {@link JAXBElement }{@code <}{@link Font }{@code >}
     * {@link JAXBElement }{@code <}{@link AnimateMotion }{@code >}
     * {@link JAXBElement }{@code <}{@link Object }{@code >}
     * {@link JAXBElement }{@code <}{@link SVGHyperlinkClass }{@code >}
     * {@link JAXBElement }{@code <}{@link Symbol }{@code >}
     * {@link Cursor }
     * 
     * 
     */
    public List<Object> getSVGDescriptionClassOrSVGAnimationClassOrSVGStructureClass() {
        if (svgDescriptionClassOrSVGAnimationClassOrSVGStructureClass == null) {
            svgDescriptionClassOrSVGAnimationClassOrSVGStructureClass = new ArrayList<Object>();
        }
        return this.svgDescriptionClassOrSVGAnimationClassOrSVGStructureClass;
    }

}
