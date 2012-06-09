/*
 * Copyright 2012 Plutext Pty Ltd.
 * 
 * This file is part of docx4j.
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
 
package org.docx4j.dml;

import org.jvnet.jaxb2_commons.ppp.Child;

import java.util.ArrayList;
import java.util.List;
import javax.xml.bind.Unmarshaller;
import javax.xml.bind.annotation.*;


/**
 * <p>Java class for CT_Blip complex type.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
 * 
 * <pre>
 * &lt;complexType name="CT_Blip">
 *   &lt;complexContent>
 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
 *       &lt;sequence>
 *         &lt;choice maxOccurs="unbounded" minOccurs="0">
 *           &lt;element name="alphaBiLevel" type="{http://schemas.openxmlformats.org/drawingml/2006/main}CT_AlphaBiLevelEffect"/>
 *           &lt;element name="alphaCeiling" type="{http://schemas.openxmlformats.org/drawingml/2006/main}CT_AlphaCeilingEffect"/>
 *           &lt;element name="alphaFloor" type="{http://schemas.openxmlformats.org/drawingml/2006/main}CT_AlphaFloorEffect"/>
 *           &lt;element name="alphaInv" type="{http://schemas.openxmlformats.org/drawingml/2006/main}CT_AlphaInverseEffect"/>
 *           &lt;element name="alphaMod" type="{http://schemas.openxmlformats.org/drawingml/2006/main}CT_AlphaModulateEffect"/>
 *           &lt;element name="alphaModFix" type="{http://schemas.openxmlformats.org/drawingml/2006/main}CT_AlphaModulateFixedEffect"/>
 *           &lt;element name="alphaRepl" type="{http://schemas.openxmlformats.org/drawingml/2006/main}CT_AlphaReplaceEffect"/>
 *           &lt;element name="biLevel" type="{http://schemas.openxmlformats.org/drawingml/2006/main}CT_BiLevelEffect"/>
 *           &lt;element name="blur" type="{http://schemas.openxmlformats.org/drawingml/2006/main}CT_BlurEffect"/>
 *           &lt;element name="clrChange" type="{http://schemas.openxmlformats.org/drawingml/2006/main}CT_ColorChangeEffect"/>
 *           &lt;element name="clrRepl" type="{http://schemas.openxmlformats.org/drawingml/2006/main}CT_ColorReplaceEffect"/>
 *           &lt;element name="duotone" type="{http://schemas.openxmlformats.org/drawingml/2006/main}CT_DuotoneEffect"/>
 *           &lt;element name="fillOverlay" type="{http://schemas.openxmlformats.org/drawingml/2006/main}CT_FillOverlayEffect"/>
 *           &lt;element name="grayscl" type="{http://schemas.openxmlformats.org/drawingml/2006/main}CT_GrayscaleEffect"/>
 *           &lt;element name="hsl" type="{http://schemas.openxmlformats.org/drawingml/2006/main}CT_HSLEffect"/>
 *           &lt;element name="lum" type="{http://schemas.openxmlformats.org/drawingml/2006/main}CT_LuminanceEffect"/>
 *           &lt;element name="tint" type="{http://schemas.openxmlformats.org/drawingml/2006/main}CT_TintEffect"/>
 *         &lt;/choice>
 *         &lt;element name="extLst" type="{http://schemas.openxmlformats.org/drawingml/2006/main}CT_OfficeArtExtensionList" minOccurs="0"/>
 *       &lt;/sequence>
 *       &lt;attGroup ref="{http://schemas.openxmlformats.org/drawingml/2006/main}AG_Blob"/>
 *       &lt;attribute name="cstate" type="{http://schemas.openxmlformats.org/drawingml/2006/main}ST_BlipCompression" default="none" />
 *     &lt;/restriction>
 *   &lt;/complexContent>
 * &lt;/complexType>
 * </pre>
 * 
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "CT_Blip", propOrder = {
    "alphaBiLevelOrAlphaCeilingOrAlphaFloor",
    "extLst"
})
public class CTBlip implements Child {

    @XmlElements({
        @XmlElement(name = "fillOverlay", type = CTFillOverlayEffect.class),
        @XmlElement(name = "blur", type = CTBlurEffect.class),
        @XmlElement(name = "alphaModFix", type = CTAlphaModulateFixedEffect.class),
        @XmlElement(name = "tint", type = CTTintEffect.class),
        @XmlElement(name = "alphaInv", type = CTAlphaInverseEffect.class),
        @XmlElement(name = "hsl", type = CTHSLEffect.class),
        @XmlElement(name = "biLevel", type = CTBiLevelEffect.class),
        @XmlElement(name = "alphaCeiling", type = CTAlphaCeilingEffect.class),
        @XmlElement(name = "alphaBiLevel", type = CTAlphaBiLevelEffect.class),
        @XmlElement(name = "alphaFloor", type = CTAlphaFloorEffect.class),
        @XmlElement(name = "alphaRepl", type = CTAlphaReplaceEffect.class),
        @XmlElement(name = "duotone", type = CTDuotoneEffect.class),
        @XmlElement(name = "clrRepl", type = CTColorReplaceEffect.class),
        @XmlElement(name = "lum", type = CTLuminanceEffect.class),
        @XmlElement(name = "clrChange", type = CTColorChangeEffect.class),
        @XmlElement(name = "grayscl", type = CTGrayscaleEffect.class),
        @XmlElement(name = "alphaMod", type = CTAlphaModulateEffect.class)
    })
    protected List<Object> alphaBiLevelOrAlphaCeilingOrAlphaFloor;
    protected CTOfficeArtExtensionList extLst;
    @XmlAttribute
    protected STBlipCompression cstate;
    @XmlAttribute(namespace = "http://schemas.openxmlformats.org/officeDocument/2006/relationships")
    protected String embed;
    @XmlAttribute(namespace = "http://schemas.openxmlformats.org/officeDocument/2006/relationships")
    protected String link;
    @XmlTransient
    private Object parent;

    /**
     * Gets the value of the alphaBiLevelOrAlphaCeilingOrAlphaFloor property.
     * 
     * <p>
     * This accessor method returns a reference to the live list,
     * not a snapshot. Therefore any modification you make to the
     * returned list will be present inside the JAXB object.
     * This is why there is not a <CODE>set</CODE> method for the alphaBiLevelOrAlphaCeilingOrAlphaFloor property.
     * 
     * <p>
     * For example, to add a new item, do as follows:
     * <pre>
     *    getAlphaBiLevelOrAlphaCeilingOrAlphaFloor().add(newItem);
     * </pre>
     * 
     * 
     * <p>
     * Objects of the following type(s) are allowed in the list
     * {@link CTFillOverlayEffect }
     * {@link CTBlurEffect }
     * {@link CTAlphaModulateFixedEffect }
     * {@link CTTintEffect }
     * {@link CTAlphaInverseEffect }
     * {@link CTHSLEffect }
     * {@link CTBiLevelEffect }
     * {@link CTAlphaCeilingEffect }
     * {@link CTAlphaBiLevelEffect }
     * {@link CTAlphaFloorEffect }
     * {@link CTAlphaReplaceEffect }
     * {@link CTDuotoneEffect }
     * {@link CTColorReplaceEffect }
     * {@link CTLuminanceEffect }
     * {@link CTColorChangeEffect }
     * {@link CTGrayscaleEffect }
     * {@link CTAlphaModulateEffect }
     * 
     * 
     */
    public List<Object> getAlphaBiLevelOrAlphaCeilingOrAlphaFloor() {
        if (alphaBiLevelOrAlphaCeilingOrAlphaFloor == null) {
            alphaBiLevelOrAlphaCeilingOrAlphaFloor = new ArrayList<Object>();
        }
        return this.alphaBiLevelOrAlphaCeilingOrAlphaFloor;
    }

    /**
     * Gets the value of the extLst property.
     * 
     * @return
     *     possible object is
     *     {@link CTOfficeArtExtensionList }
     *     
     */
    public CTOfficeArtExtensionList getExtLst() {
        return extLst;
    }

    /**
     * Sets the value of the extLst property.
     * 
     * @param value
     *     allowed object is
     *     {@link CTOfficeArtExtensionList }
     *     
     */
    public void setExtLst(CTOfficeArtExtensionList value) {
        this.extLst = value;
    }

    /**
     * Gets the value of the cstate property.
     * 
     * @return
     *     possible object is
     *     {@link STBlipCompression }
     *     
     */
    public STBlipCompression getCstate() {
        if (cstate == null) {
            return STBlipCompression.NONE;
        } else {
            return cstate;
        }
    }

    /**
     * Sets the value of the cstate property.
     * 
     * @param value
     *     allowed object is
     *     {@link STBlipCompression }
     *     
     */
    public void setCstate(STBlipCompression value) {
        this.cstate = value;
    }

    /**
     * Embedded Picture Reference
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getEmbed() {
        if (embed == null) {
            return "";
        } else {
            return embed;
        }
    }

    /**
     * Sets the value of the embed property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setEmbed(String value) {
        this.embed = value;
    }

    /**
     * Linked Picture Reference
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getLink() {
        if (link == null) {
            return "";
        } else {
            return link;
        }
    }

    /**
     * Sets the value of the link property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setLink(String value) {
        this.link = value;
    }
   /**
     * Gets the parent object in the object tree representing the unmarshalled xml document.
     *
     * @return
     *     The parent object.
     */
    public Object getParent() {
        return this.parent;
    }

    public void setParent(Object parent) {
        this.parent = parent;
    }

    /**
     * This method is invoked by the JAXB implementation on each instance when unmarshalling completes.
     *
     * @param parent
     *     The parent object in the object tree.
     * @param unmarshaller
     *     The unmarshaller that generated the instance.
     */
    public void afterUnmarshal(Unmarshaller unmarshaller, Object parent) {
        setParent(parent);
    }
}
