/*
 *  Copyright 2007-2013, Plutext Pty Ltd.
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


package org.docx4j.wml; 

import java.util.ArrayList;
import java.util.List;

import javax.xml.bind.JAXBElement;
import javax.xml.bind.Unmarshaller;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlElementRef;
import javax.xml.bind.annotation.XmlElementRefs;
import javax.xml.bind.annotation.XmlTransient;
import javax.xml.bind.annotation.XmlType;

import org.docx4j.w14.CTFillTextEffect;
import org.docx4j.w14.CTGlow;
import org.docx4j.w14.CTProps3D;
import org.docx4j.w14.CTReflection;
import org.docx4j.w14.CTScene3D;
import org.docx4j.w14.CTShadow;
import org.docx4j.w14.CTTextOutlineEffect;

import org.docx4j.w14.CTLigatures;
import org.docx4j.w14.CTNumForm;
import org.docx4j.w14.CTNumSpacing;
import org.docx4j.w14.CTOnOff;
import org.docx4j.w14.CTStylisticSets;

import org.jvnet.jaxb2_commons.ppp.Child;


/**
 * <p>Java class for CT_RPrChange complex type.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
 * 
 * <pre>
 * &lt;complexType name="CT_RPrChange">
 *   &lt;complexContent>
 *     &lt;extension base="{http://schemas.openxmlformats.org/wordprocessingml/2006/main}CT_TrackChange">
 *       &lt;sequence>
 *         &lt;element name="rPr">
 *           &lt;complexType>
 *             &lt;complexContent>
 *               &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
 *                 &lt;sequence>
 *                   &lt;group ref="{http://schemas.openxmlformats.org/wordprocessingml/2006/main}EG_RPrBase" maxOccurs="unbounded" minOccurs="0"/>
 *                 &lt;/sequence>
 *               &lt;/restriction>
 *             &lt;/complexContent>
 *           &lt;/complexType>
 *         &lt;/element>
 *       &lt;/sequence>
 *     &lt;/extension>
 *   &lt;/complexContent>
 * &lt;/complexType>
 * </pre>
 * 
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "CT_RPrChange", propOrder = {
    "rPr"
})
public class CTRPrChange
    extends CTTrackChange
{

    @XmlElement(required = true)
    protected CTRPrChange.RPr rPr;

    /**
     * Gets the value of the rPr property.
     * 
     * @return
     *     possible object is
     *     {@link CTRPrChange.RPr }
     *     
     */
    public CTRPrChange.RPr getRPr() {
        return rPr;
    }

    /**
     * Sets the value of the rPr property.
     * 
     * @param value
     *     allowed object is
     *     {@link CTRPrChange.RPr }
     *     
     */
    public void setRPr(CTRPrChange.RPr value) {
        this.rPr = value;
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
     *         &lt;group ref="{http://schemas.openxmlformats.org/wordprocessingml/2006/main}EG_RPrBase" maxOccurs="unbounded" minOccurs="0"/>
     *       &lt;/sequence>
     *     &lt;/restriction>
     *   &lt;/complexContent>
     * &lt;/complexType>
     * </pre>
     * 
     * 
     */
    @XmlAccessorType(XmlAccessType.FIELD)
    @XmlType(name = "", propOrder = {
        "egrPrBase"
    })
    public static class RPr implements Child
    {

        @XmlElementRefs({
            @XmlElementRef(name = "bdr", namespace = "http://schemas.openxmlformats.org/wordprocessingml/2006/main", type = JAXBElement.class),
            @XmlElementRef(name = "snapToGrid", namespace = "http://schemas.openxmlformats.org/wordprocessingml/2006/main", type = JAXBElement.class),
            @XmlElementRef(name = "dstrike", namespace = "http://schemas.openxmlformats.org/wordprocessingml/2006/main", type = JAXBElement.class),
            @XmlElementRef(name = "position", namespace = "http://schemas.openxmlformats.org/wordprocessingml/2006/main", type = JAXBElement.class),
            @XmlElementRef(name = "fitText", namespace = "http://schemas.openxmlformats.org/wordprocessingml/2006/main", type = JAXBElement.class),
            @XmlElementRef(name = "emboss", namespace = "http://schemas.openxmlformats.org/wordprocessingml/2006/main", type = JAXBElement.class),
            @XmlElementRef(name = "cs", namespace = "http://schemas.openxmlformats.org/wordprocessingml/2006/main", type = JAXBElement.class),
            @XmlElementRef(name = "smallCaps", namespace = "http://schemas.openxmlformats.org/wordprocessingml/2006/main", type = JAXBElement.class),
            @XmlElementRef(name = "spacing", namespace = "http://schemas.openxmlformats.org/wordprocessingml/2006/main", type = JAXBElement.class),
            @XmlElementRef(name = "rFonts", namespace = "http://schemas.openxmlformats.org/wordprocessingml/2006/main", type = RFonts.class),
            @XmlElementRef(name = "em", namespace = "http://schemas.openxmlformats.org/wordprocessingml/2006/main", type = JAXBElement.class),
            @XmlElementRef(name = "outline", namespace = "http://schemas.openxmlformats.org/wordprocessingml/2006/main", type = JAXBElement.class),
            @XmlElementRef(name = "rtl", namespace = "http://schemas.openxmlformats.org/wordprocessingml/2006/main", type = JAXBElement.class),
            @XmlElementRef(name = "caps", namespace = "http://schemas.openxmlformats.org/wordprocessingml/2006/main", type = JAXBElement.class),
            @XmlElementRef(name = "vertAlign", namespace = "http://schemas.openxmlformats.org/wordprocessingml/2006/main", type = JAXBElement.class),
            @XmlElementRef(name = "color", namespace = "http://schemas.openxmlformats.org/wordprocessingml/2006/main", type = Color.class),
            @XmlElementRef(name = "eastAsianLayout", namespace = "http://schemas.openxmlformats.org/wordprocessingml/2006/main", type = JAXBElement.class),
            @XmlElementRef(name = "w", namespace = "http://schemas.openxmlformats.org/wordprocessingml/2006/main", type = JAXBElement.class),
            @XmlElementRef(name = "b", namespace = "http://schemas.openxmlformats.org/wordprocessingml/2006/main", type = JAXBElement.class),
            @XmlElementRef(name = "oMath", namespace = "http://schemas.openxmlformats.org/wordprocessingml/2006/main", type = JAXBElement.class),
            @XmlElementRef(name = "strike", namespace = "http://schemas.openxmlformats.org/wordprocessingml/2006/main", type = JAXBElement.class),
            @XmlElementRef(name = "szCs", namespace = "http://schemas.openxmlformats.org/wordprocessingml/2006/main", type = JAXBElement.class),
            @XmlElementRef(name = "lang", namespace = "http://schemas.openxmlformats.org/wordprocessingml/2006/main", type = JAXBElement.class),
            @XmlElementRef(name = "iCs", namespace = "http://schemas.openxmlformats.org/wordprocessingml/2006/main", type = JAXBElement.class),
            @XmlElementRef(name = "shadow", namespace = "http://schemas.openxmlformats.org/wordprocessingml/2006/main", type = JAXBElement.class),
            @XmlElementRef(name = "noProof", namespace = "http://schemas.openxmlformats.org/wordprocessingml/2006/main", type = JAXBElement.class),
            @XmlElementRef(name = "effect", namespace = "http://schemas.openxmlformats.org/wordprocessingml/2006/main", type = JAXBElement.class),
            @XmlElementRef(name = "rStyle", namespace = "http://schemas.openxmlformats.org/wordprocessingml/2006/main", type = RStyle.class),
            @XmlElementRef(name = "vanish", namespace = "http://schemas.openxmlformats.org/wordprocessingml/2006/main", type = JAXBElement.class),
            @XmlElementRef(name = "specVanish", namespace = "http://schemas.openxmlformats.org/wordprocessingml/2006/main", type = JAXBElement.class),
            @XmlElementRef(name = "webHidden", namespace = "http://schemas.openxmlformats.org/wordprocessingml/2006/main", type = JAXBElement.class),
            @XmlElementRef(name = "imprint", namespace = "http://schemas.openxmlformats.org/wordprocessingml/2006/main", type = JAXBElement.class),
            @XmlElementRef(name = "i", namespace = "http://schemas.openxmlformats.org/wordprocessingml/2006/main", type = JAXBElement.class),
            @XmlElementRef(name = "shd", namespace = "http://schemas.openxmlformats.org/wordprocessingml/2006/main", type = JAXBElement.class),
            @XmlElementRef(name = "highlight", namespace = "http://schemas.openxmlformats.org/wordprocessingml/2006/main", type = Highlight.class),
            @XmlElementRef(name = "kern", namespace = "http://schemas.openxmlformats.org/wordprocessingml/2006/main", type = JAXBElement.class),
            @XmlElementRef(name = "bCs", namespace = "http://schemas.openxmlformats.org/wordprocessingml/2006/main", type = JAXBElement.class),
            @XmlElementRef(name = "u", namespace = "http://schemas.openxmlformats.org/wordprocessingml/2006/main", type = U.class),
            @XmlElementRef(name = "sz", namespace = "http://schemas.openxmlformats.org/wordprocessingml/2006/main", type = JAXBElement.class),
            @XmlElementRef(name = "textFill", namespace = "http://schemas.microsoft.com/office/word/2010/wordml", type = JAXBElement.class),
            @XmlElementRef(name = "shadow", namespace = "http://schemas.microsoft.com/office/word/2010/wordml", type = JAXBElement.class),
            @XmlElementRef(name = "props3d", namespace = "http://schemas.microsoft.com/office/word/2010/wordml", type = JAXBElement.class),
            @XmlElementRef(name = "reflection", namespace = "http://schemas.microsoft.com/office/word/2010/wordml", type = JAXBElement.class),
            @XmlElementRef(name = "glow", namespace = "http://schemas.microsoft.com/office/word/2010/wordml", type = JAXBElement.class),
            @XmlElementRef(name = "textOutline", namespace = "http://schemas.microsoft.com/office/word/2010/wordml", type = JAXBElement.class),
            @XmlElementRef(name = "scene3d", namespace = "http://schemas.microsoft.com/office/word/2010/wordml", type = JAXBElement.class),
            @XmlElementRef(name = "ligatures", namespace = "http://schemas.microsoft.com/office/word/2010/wordml", type = JAXBElement.class),
            @XmlElementRef(name = "numForm", namespace = "http://schemas.microsoft.com/office/word/2010/wordml", type = JAXBElement.class),
            @XmlElementRef(name = "numSpacing", namespace = "http://schemas.microsoft.com/office/word/2010/wordml", type = JAXBElement.class),
            @XmlElementRef(name = "stylisticSets", namespace = "http://schemas.microsoft.com/office/word/2010/wordml", type = JAXBElement.class),
            @XmlElementRef(name = "cntxtAlts", namespace = "http://schemas.microsoft.com/office/word/2010/wordml", type = JAXBElement.class)
        })
        protected List<Object> egrPrBase  = new ArrayListWml<Object>(this);
        @XmlTransient
        private Object parent;

        /**
         * Gets the value of the egrPrBase property.
         * 
         * <p>
         * This accessor method returns a reference to the live list,
         * not a snapshot. Therefore any modification you make to the
         * returned list will be present inside the JAXB object.
         * This is why there is not a <CODE>set</CODE> method for the egrPrBase property.
         * 
         * <p>
         * For example, to add a new item, do as follows:
         * <pre>
         *    getEGRPrBase().add(newItem);
         * </pre>
         * 
         * 
         * <p>
         * Objects of the following type(s) are allowed in the list
         * {@link JAXBElement }{@code <}{@link BooleanDefaultTrue }{@code >}
         * {@link JAXBElement }{@code <}{@link CTBorder }{@code >}
         * {@link JAXBElement }{@code <}{@link BooleanDefaultTrue }{@code >}
         * {@link JAXBElement }{@code <}{@link CTSignedHpsMeasure }{@code >}
         * {@link JAXBElement }{@code <}{@link CTFitText }{@code >}
         * {@link JAXBElement }{@code <}{@link BooleanDefaultTrue }{@code >}
         * {@link JAXBElement }{@code <}{@link CTSignedTwipsMeasure }{@code >}
         * {@link JAXBElement }{@code <}{@link BooleanDefaultTrue }{@code >}
         * {@link JAXBElement }{@code <}{@link BooleanDefaultTrue }{@code >}
         * {@link RFonts }
         * {@link JAXBElement }{@code <}{@link CTEm }{@code >}
         * {@link JAXBElement }{@code <}{@link BooleanDefaultTrue }{@code >}
         * {@link JAXBElement }{@code <}{@link BooleanDefaultTrue }{@code >}
         * {@link JAXBElement }{@code <}{@link BooleanDefaultTrue }{@code >}
         * {@link Color }
         * {@link JAXBElement }{@code <}{@link CTVerticalAlignRun }{@code >}
         * {@link JAXBElement }{@code <}{@link CTEastAsianLayout }{@code >}
         * {@link JAXBElement }{@code <}{@link CTTextScale }{@code >}
         * {@link JAXBElement }{@code <}{@link BooleanDefaultTrue }{@code >}
         * {@link JAXBElement }{@code <}{@link BooleanDefaultTrue }{@code >}
         * {@link JAXBElement }{@code <}{@link BooleanDefaultTrue }{@code >}
         * {@link JAXBElement }{@code <}{@link HpsMeasure }{@code >}
         * {@link JAXBElement }{@code <}{@link BooleanDefaultTrue }{@code >}
         * {@link JAXBElement }{@code <}{@link CTLanguage }{@code >}
         * {@link JAXBElement }{@code <}{@link BooleanDefaultTrue }{@code >}
         * {@link JAXBElement }{@code <}{@link BooleanDefaultTrue }{@code >}
         * {@link JAXBElement }{@code <}{@link CTTextEffect }{@code >}
         * {@link JAXBElement }{@code <}{@link BooleanDefaultTrue }{@code >}
         * {@link RStyle }
         * {@link JAXBElement }{@code <}{@link BooleanDefaultTrue }{@code >}
         * {@link JAXBElement }{@code <}{@link BooleanDefaultTrue }{@code >}
         * {@link JAXBElement }{@code <}{@link BooleanDefaultTrue }{@code >}
         * {@link JAXBElement }{@code <}{@link BooleanDefaultTrue }{@code >}
         * {@link JAXBElement }{@code <}{@link CTShd }{@code >}
         * {@link JAXBElement }{@code <}{@link HpsMeasure }{@code >}
         * {@link Highlight }
         * {@link JAXBElement }{@code <}{@link BooleanDefaultTrue }{@code >}
         * {@link JAXBElement }{@code <}{@link HpsMeasure }{@code >}
         * {@link U }
	     * {@link JAXBElement }{@code <}{@link CTGlow }{@code >}
	     * {@link JAXBElement }{@code <}{@link CTShadow }{@code >}
	     * {@link JAXBElement }{@code <}{@link CTReflection }{@code >}
	     * {@link JAXBElement }{@code <}{@link CTTextOutlineEffect }{@code >}
	     * {@link JAXBElement }{@code <}{@link CTFillTextEffect }{@code >}
	     * {@link JAXBElement }{@code <}{@link CTScene3D }{@code >}
	     * {@link JAXBElement }{@code <}{@link CTProps3D }{@code >}
	     * {@link JAXBElement }{@code <}{@link CTLigatures }{@code >}
	     * {@link JAXBElement }{@code <}{@link CTNumSpacing }{@code >}77
	     * {@link JAXBElement }{@code <}{@link CTNumForm }{@code >}
	     * {@link JAXBElement }{@code <}{@link CTStylisticSets }{@code >}
	     * {@link JAXBElement }{@code <}{@link CTOnOff }{@code >}
         */
        public List<Object> getEGRPrBase() {
            if (egrPrBase == null) {
                egrPrBase  = new ArrayListWml<Object>(this);
            }
            return this.egrPrBase;
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

}
