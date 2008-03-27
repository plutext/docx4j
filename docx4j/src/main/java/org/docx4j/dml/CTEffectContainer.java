/*
 *  Copyright 2007, Plutext Pty Ltd.
 *   
 *  This file is part of docx4j.

    docx4j is free software: you can use it, redistribute it and/or modify
    it under the terms of version 3 of the GNU Affero General Public License 
    as published by the Free Software Foundation.

    If you need the right to use it under a different license, please
    contact Plutext.

    docx4j is distributed in the hope that it will be useful,
    but WITHOUT ANY WARRANTY; without even the implied warranty of
    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
    GNU Affero General Public License for more details.

    You should have received a copy of the GNU Affero General Public License   
    along with docx4j.  If not, see <http://www.fsf.org/licensing/licenses/>.
    
 */

package org.docx4j.dml;

import java.util.ArrayList;
import java.util.List;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlElements;
import javax.xml.bind.annotation.XmlSchemaType;
import javax.xml.bind.annotation.XmlType;
import javax.xml.bind.annotation.adapters.CollapsedStringAdapter;
import javax.xml.bind.annotation.adapters.XmlJavaTypeAdapter;


/**
 * <p>Java class for CT_EffectContainer complex type.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
 * 
 * <pre>
 * &lt;complexType name="CT_EffectContainer">
 *   &lt;complexContent>
 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
 *       &lt;group ref="{http://schemas.openxmlformats.org/drawingml/2006/main}EG_Effect" maxOccurs="unbounded" minOccurs="0"/>
 *       &lt;attribute name="type" type="{http://schemas.openxmlformats.org/drawingml/2006/main}ST_EffectContainerType" default="sib" />
 *       &lt;attribute name="name" type="{http://www.w3.org/2001/XMLSchema}token" />
 *     &lt;/restriction>
 *   &lt;/complexContent>
 * &lt;/complexType>
 * </pre>
 * 
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "CT_EffectContainer", propOrder = {
    "egEffect"
})
public class CTEffectContainer {

    @XmlElements({
        @XmlElement(name = "duotone", type = CTDuotoneEffect.class),
        @XmlElement(name = "lum", type = CTLuminanceEffect.class),
        @XmlElement(name = "blend", type = CTBlendEffect.class),
        @XmlElement(name = "effect", type = CTEffectReference.class),
        @XmlElement(name = "innerShdw", type = CTInnerShadowEffect.class),
        @XmlElement(name = "alphaBiLevel", type = CTAlphaBiLevelEffect.class),
        @XmlElement(name = "reflection", type = CTReflectionEffect.class),
        @XmlElement(name = "fillOverlay", type = CTFillOverlayEffect.class),
        @XmlElement(name = "softEdge", type = CTSoftEdgesEffect.class),
        @XmlElement(name = "fill", type = CTFillEffect.class),
        @XmlElement(name = "alphaRepl", type = CTAlphaReplaceEffect.class),
        @XmlElement(name = "tint", type = CTTintEffect.class),
        @XmlElement(name = "prstShdw", type = CTPresetShadowEffect.class),
        @XmlElement(name = "alphaFloor", type = CTAlphaFloorEffect.class),
        @XmlElement(name = "cont", type = CTEffectContainer.class),
        @XmlElement(name = "biLevel", type = CTBiLevelEffect.class),
        @XmlElement(name = "alphaCeiling", type = CTAlphaCeilingEffect.class),
        @XmlElement(name = "clrChange", type = CTColorChangeEffect.class),
        @XmlElement(name = "alphaInv", type = CTAlphaInverseEffect.class),
        @XmlElement(name = "alphaMod", type = CTAlphaModulateEffect.class),
        @XmlElement(name = "clrRepl", type = CTColorReplaceEffect.class),
        @XmlElement(name = "grayscl", type = CTGrayscaleEffect.class),
        @XmlElement(name = "xfrm", type = CTTransformEffect.class),
        @XmlElement(name = "relOff", type = CTRelativeOffsetEffect.class),
        @XmlElement(name = "hsl", type = CTHSLEffect.class),
        @XmlElement(name = "outerShdw", type = CTOuterShadowEffect.class),
        @XmlElement(name = "alphaModFix", type = CTAlphaModulateFixedEffect.class),
        @XmlElement(name = "blur", type = CTBlurEffect.class),
        @XmlElement(name = "alphaOutset", type = CTAlphaOutsetEffect.class),
        @XmlElement(name = "glow", type = CTGlowEffect.class)
    })
    protected List<Object> egEffect;
    @XmlAttribute
    protected STEffectContainerType type;
    @XmlAttribute
    @XmlJavaTypeAdapter(CollapsedStringAdapter.class)
    @XmlSchemaType(name = "token")
    protected String name;

    /**
     * Gets the value of the egEffect property.
     * 
     * <p>
     * This accessor method returns a reference to the live list,
     * not a snapshot. Therefore any modification you make to the
     * returned list will be present inside the JAXB object.
     * This is why there is not a <CODE>set</CODE> method for the egEffect property.
     * 
     * <p>
     * For example, to add a new item, do as follows:
     * <pre>
     *    getEGEffect().add(newItem);
     * </pre>
     * 
     * 
     * <p>
     * Objects of the following type(s) are allowed in the list
     * {@link CTDuotoneEffect }
     * {@link CTLuminanceEffect }
     * {@link CTBlendEffect }
     * {@link CTEffectReference }
     * {@link CTInnerShadowEffect }
     * {@link CTAlphaBiLevelEffect }
     * {@link CTReflectionEffect }
     * {@link CTFillOverlayEffect }
     * {@link CTSoftEdgesEffect }
     * {@link CTFillEffect }
     * {@link CTAlphaReplaceEffect }
     * {@link CTTintEffect }
     * {@link CTPresetShadowEffect }
     * {@link CTAlphaFloorEffect }
     * {@link CTEffectContainer }
     * {@link CTBiLevelEffect }
     * {@link CTAlphaCeilingEffect }
     * {@link CTColorChangeEffect }
     * {@link CTAlphaInverseEffect }
     * {@link CTAlphaModulateEffect }
     * {@link CTColorReplaceEffect }
     * {@link CTGrayscaleEffect }
     * {@link CTTransformEffect }
     * {@link CTRelativeOffsetEffect }
     * {@link CTHSLEffect }
     * {@link CTOuterShadowEffect }
     * {@link CTAlphaModulateFixedEffect }
     * {@link CTBlurEffect }
     * {@link CTAlphaOutsetEffect }
     * {@link CTGlowEffect }
     * 
     * 
     */
    public List<Object> getEGEffect() {
        if (egEffect == null) {
            egEffect = new ArrayList<Object>();
        }
        return this.egEffect;
    }

    /**
     * Gets the value of the type property.
     * 
     * @return
     *     possible object is
     *     {@link STEffectContainerType }
     *     
     */
    public STEffectContainerType getType() {
        if (type == null) {
            return STEffectContainerType.SIB;
        } else {
            return type;
        }
    }

    /**
     * Sets the value of the type property.
     * 
     * @param value
     *     allowed object is
     *     {@link STEffectContainerType }
     *     
     */
    public void setType(STEffectContainerType value) {
        this.type = value;
    }

    /**
     * Gets the value of the name property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getName() {
        return name;
    }

    /**
     * Sets the value of the name property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setName(String value) {
        this.name = value;
    }

}
