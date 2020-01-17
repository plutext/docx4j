/*
 *  Copyright 2009-2012, Plutext Pty Ltd.
 *   
 *  This file is part of pptx4j, a component of docx4j.

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
package org.docx4j.math;

import javax.xml.bind.Unmarshaller;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlTransient;
import javax.xml.bind.annotation.XmlType;
import org.jvnet.jaxb2_commons.ppp.Child;


/**
 * <p>Java class for CT_MathPr complex type.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
 * 
 * <pre>
 * &lt;complexType name="CT_MathPr">
 *   &lt;complexContent>
 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
 *       &lt;sequence>
 *         &lt;element name="mathFont" type="{http://schemas.openxmlformats.org/officeDocument/2006/math}CT_String" minOccurs="0"/>
 *         &lt;element name="brkBin" type="{http://schemas.openxmlformats.org/officeDocument/2006/math}CT_BreakBin" minOccurs="0"/>
 *         &lt;element name="brkBinSub" type="{http://schemas.openxmlformats.org/officeDocument/2006/math}CT_BreakBinSub" minOccurs="0"/>
 *         &lt;element name="smallFrac" type="{http://schemas.openxmlformats.org/officeDocument/2006/math}CT_OnOff" minOccurs="0"/>
 *         &lt;element name="dispDef" type="{http://schemas.openxmlformats.org/officeDocument/2006/math}CT_OnOff" minOccurs="0"/>
 *         &lt;element name="lMargin" type="{http://schemas.openxmlformats.org/officeDocument/2006/math}CT_TwipsMeasure" minOccurs="0"/>
 *         &lt;element name="rMargin" type="{http://schemas.openxmlformats.org/officeDocument/2006/math}CT_TwipsMeasure" minOccurs="0"/>
 *         &lt;element name="defJc" type="{http://schemas.openxmlformats.org/officeDocument/2006/math}CT_OMathJc" minOccurs="0"/>
 *         &lt;element name="preSp" type="{http://schemas.openxmlformats.org/officeDocument/2006/math}CT_TwipsMeasure" minOccurs="0"/>
 *         &lt;element name="postSp" type="{http://schemas.openxmlformats.org/officeDocument/2006/math}CT_TwipsMeasure" minOccurs="0"/>
 *         &lt;element name="interSp" type="{http://schemas.openxmlformats.org/officeDocument/2006/math}CT_TwipsMeasure" minOccurs="0"/>
 *         &lt;element name="intraSp" type="{http://schemas.openxmlformats.org/officeDocument/2006/math}CT_TwipsMeasure" minOccurs="0"/>
 *         &lt;choice minOccurs="0">
 *           &lt;element name="wrapIndent" type="{http://schemas.openxmlformats.org/officeDocument/2006/math}CT_TwipsMeasure"/>
 *           &lt;element name="wrapRight" type="{http://schemas.openxmlformats.org/officeDocument/2006/math}CT_OnOff"/>
 *         &lt;/choice>
 *         &lt;element name="intLim" type="{http://schemas.openxmlformats.org/officeDocument/2006/math}CT_LimLoc" minOccurs="0"/>
 *         &lt;element name="naryLim" type="{http://schemas.openxmlformats.org/officeDocument/2006/math}CT_LimLoc" minOccurs="0"/>
 *       &lt;/sequence>
 *     &lt;/restriction>
 *   &lt;/complexContent>
 * &lt;/complexType>
 * </pre>
 * 
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "CT_MathPr", propOrder = {
    "mathFont",
    "brkBin",
    "brkBinSub",
    "smallFrac",
    "dispDef",
    "lMargin",
    "rMargin",
    "defJc",
    "preSp",
    "postSp",
    "interSp",
    "intraSp",
    "wrapIndent",
    "wrapRight",
    "intLim",
    "naryLim"
})
public class CTMathPr
    implements Child
{

    protected CTString mathFont;
    protected CTBreakBin brkBin;
    protected CTBreakBinSub brkBinSub;
    protected CTOnOff smallFrac;
    protected CTOnOff dispDef;
    protected CTTwipsMeasure lMargin;
    protected CTTwipsMeasure rMargin;
    protected CTOMathJc defJc;
    protected CTTwipsMeasure preSp;
    protected CTTwipsMeasure postSp;
    protected CTTwipsMeasure interSp;
    protected CTTwipsMeasure intraSp;
    protected CTTwipsMeasure wrapIndent;
    protected CTOnOff wrapRight;
    protected CTLimLoc intLim;
    protected CTLimLoc naryLim;
    @XmlTransient
    private Object parent;

    /**
     * Gets the value of the mathFont property.
     * 
     * @return
     *     possible object is
     *     {@link CTString }
     *     
     */
    public CTString getMathFont() {
        return mathFont;
    }

    /**
     * Sets the value of the mathFont property.
     * 
     * @param value
     *     allowed object is
     *     {@link CTString }
     *     
     */
    public void setMathFont(CTString value) {
        this.mathFont = value;
    }

    /**
     * Gets the value of the brkBin property.
     * 
     * @return
     *     possible object is
     *     {@link CTBreakBin }
     *     
     */
    public CTBreakBin getBrkBin() {
        return brkBin;
    }

    /**
     * Sets the value of the brkBin property.
     * 
     * @param value
     *     allowed object is
     *     {@link CTBreakBin }
     *     
     */
    public void setBrkBin(CTBreakBin value) {
        this.brkBin = value;
    }

    /**
     * Gets the value of the brkBinSub property.
     * 
     * @return
     *     possible object is
     *     {@link CTBreakBinSub }
     *     
     */
    public CTBreakBinSub getBrkBinSub() {
        return brkBinSub;
    }

    /**
     * Sets the value of the brkBinSub property.
     * 
     * @param value
     *     allowed object is
     *     {@link CTBreakBinSub }
     *     
     */
    public void setBrkBinSub(CTBreakBinSub value) {
        this.brkBinSub = value;
    }

    /**
     * Gets the value of the smallFrac property.
     * 
     * @return
     *     possible object is
     *     {@link CTOnOff }
     *     
     */
    public CTOnOff getSmallFrac() {
        return smallFrac;
    }

    /**
     * Sets the value of the smallFrac property.
     * 
     * @param value
     *     allowed object is
     *     {@link CTOnOff }
     *     
     */
    public void setSmallFrac(CTOnOff value) {
        this.smallFrac = value;
    }

    /**
     * Gets the value of the dispDef property.
     * 
     * @return
     *     possible object is
     *     {@link CTOnOff }
     *     
     */
    public CTOnOff getDispDef() {
        return dispDef;
    }

    /**
     * Sets the value of the dispDef property.
     * 
     * @param value
     *     allowed object is
     *     {@link CTOnOff }
     *     
     */
    public void setDispDef(CTOnOff value) {
        this.dispDef = value;
    }

    /**
     * Gets the value of the lMargin property.
     * 
     * @return
     *     possible object is
     *     {@link CTTwipsMeasure }
     *     
     */
    public CTTwipsMeasure getLMargin() {
        return lMargin;
    }

    /**
     * Sets the value of the lMargin property.
     * 
     * @param value
     *     allowed object is
     *     {@link CTTwipsMeasure }
     *     
     */
    public void setLMargin(CTTwipsMeasure value) {
        this.lMargin = value;
    }

    /**
     * Gets the value of the rMargin property.
     * 
     * @return
     *     possible object is
     *     {@link CTTwipsMeasure }
     *     
     */
    public CTTwipsMeasure getRMargin() {
        return rMargin;
    }

    /**
     * Sets the value of the rMargin property.
     * 
     * @param value
     *     allowed object is
     *     {@link CTTwipsMeasure }
     *     
     */
    public void setRMargin(CTTwipsMeasure value) {
        this.rMargin = value;
    }

    /**
     * Gets the value of the defJc property.
     * 
     * @return
     *     possible object is
     *     {@link CTOMathJc }
     *     
     */
    public CTOMathJc getDefJc() {
        return defJc;
    }

    /**
     * Sets the value of the defJc property.
     * 
     * @param value
     *     allowed object is
     *     {@link CTOMathJc }
     *     
     */
    public void setDefJc(CTOMathJc value) {
        this.defJc = value;
    }

    /**
     * Gets the value of the preSp property.
     * 
     * @return
     *     possible object is
     *     {@link CTTwipsMeasure }
     *     
     */
    public CTTwipsMeasure getPreSp() {
        return preSp;
    }

    /**
     * Sets the value of the preSp property.
     * 
     * @param value
     *     allowed object is
     *     {@link CTTwipsMeasure }
     *     
     */
    public void setPreSp(CTTwipsMeasure value) {
        this.preSp = value;
    }

    /**
     * Gets the value of the postSp property.
     * 
     * @return
     *     possible object is
     *     {@link CTTwipsMeasure }
     *     
     */
    public CTTwipsMeasure getPostSp() {
        return postSp;
    }

    /**
     * Sets the value of the postSp property.
     * 
     * @param value
     *     allowed object is
     *     {@link CTTwipsMeasure }
     *     
     */
    public void setPostSp(CTTwipsMeasure value) {
        this.postSp = value;
    }

    /**
     * Gets the value of the interSp property.
     * 
     * @return
     *     possible object is
     *     {@link CTTwipsMeasure }
     *     
     */
    public CTTwipsMeasure getInterSp() {
        return interSp;
    }

    /**
     * Sets the value of the interSp property.
     * 
     * @param value
     *     allowed object is
     *     {@link CTTwipsMeasure }
     *     
     */
    public void setInterSp(CTTwipsMeasure value) {
        this.interSp = value;
    }

    /**
     * Gets the value of the intraSp property.
     * 
     * @return
     *     possible object is
     *     {@link CTTwipsMeasure }
     *     
     */
    public CTTwipsMeasure getIntraSp() {
        return intraSp;
    }

    /**
     * Sets the value of the intraSp property.
     * 
     * @param value
     *     allowed object is
     *     {@link CTTwipsMeasure }
     *     
     */
    public void setIntraSp(CTTwipsMeasure value) {
        this.intraSp = value;
    }

    /**
     * Gets the value of the wrapIndent property.
     * 
     * @return
     *     possible object is
     *     {@link CTTwipsMeasure }
     *     
     */
    public CTTwipsMeasure getWrapIndent() {
        return wrapIndent;
    }

    /**
     * Sets the value of the wrapIndent property.
     * 
     * @param value
     *     allowed object is
     *     {@link CTTwipsMeasure }
     *     
     */
    public void setWrapIndent(CTTwipsMeasure value) {
        this.wrapIndent = value;
    }

    /**
     * Gets the value of the wrapRight property.
     * 
     * @return
     *     possible object is
     *     {@link CTOnOff }
     *     
     */
    public CTOnOff getWrapRight() {
        return wrapRight;
    }

    /**
     * Sets the value of the wrapRight property.
     * 
     * @param value
     *     allowed object is
     *     {@link CTOnOff }
     *     
     */
    public void setWrapRight(CTOnOff value) {
        this.wrapRight = value;
    }

    /**
     * Gets the value of the intLim property.
     * 
     * @return
     *     possible object is
     *     {@link CTLimLoc }
     *     
     */
    public CTLimLoc getIntLim() {
        return intLim;
    }

    /**
     * Sets the value of the intLim property.
     * 
     * @param value
     *     allowed object is
     *     {@link CTLimLoc }
     *     
     */
    public void setIntLim(CTLimLoc value) {
        this.intLim = value;
    }

    /**
     * Gets the value of the naryLim property.
     * 
     * @return
     *     possible object is
     *     {@link CTLimLoc }
     *     
     */
    public CTLimLoc getNaryLim() {
        return naryLim;
    }

    /**
     * Sets the value of the naryLim property.
     * 
     * @param value
     *     allowed object is
     *     {@link CTLimLoc }
     *     
     */
    public void setNaryLim(CTLimLoc value) {
        this.naryLim = value;
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
