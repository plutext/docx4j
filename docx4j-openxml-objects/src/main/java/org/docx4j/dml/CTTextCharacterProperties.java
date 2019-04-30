/*
 *  Copyright 2007-2008, Plutext Pty Ltd.
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


package org.docx4j.dml;

import javax.xml.bind.Unmarshaller;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlSchemaType;
import javax.xml.bind.annotation.XmlTransient;
import javax.xml.bind.annotation.XmlType;
import org.jvnet.jaxb2_commons.ppp.Child;


/**
 * <p>Java class for CT_TextCharacterProperties complex type.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
 * 
 * <pre>
 * &lt;complexType name="CT_TextCharacterProperties"&gt;
 *   &lt;complexContent&gt;
 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType"&gt;
 *       &lt;sequence&gt;
 *         &lt;element name="ln" type="{http://schemas.openxmlformats.org/drawingml/2006/main}CT_LineProperties" minOccurs="0"/&gt;
 *         &lt;group ref="{http://schemas.openxmlformats.org/drawingml/2006/main}EG_FillProperties" minOccurs="0"/&gt;
 *         &lt;group ref="{http://schemas.openxmlformats.org/drawingml/2006/main}EG_EffectProperties" minOccurs="0"/&gt;
 *         &lt;element name="highlight" type="{http://schemas.openxmlformats.org/drawingml/2006/main}CT_Color" minOccurs="0"/&gt;
 *         &lt;group ref="{http://schemas.openxmlformats.org/drawingml/2006/main}EG_TextUnderlineLine" minOccurs="0"/&gt;
 *         &lt;group ref="{http://schemas.openxmlformats.org/drawingml/2006/main}EG_TextUnderlineFill" minOccurs="0"/&gt;
 *         &lt;element name="latin" type="{http://schemas.openxmlformats.org/drawingml/2006/main}CT_TextFont" minOccurs="0"/&gt;
 *         &lt;element name="ea" type="{http://schemas.openxmlformats.org/drawingml/2006/main}CT_TextFont" minOccurs="0"/&gt;
 *         &lt;element name="cs" type="{http://schemas.openxmlformats.org/drawingml/2006/main}CT_TextFont" minOccurs="0"/&gt;
 *         &lt;element name="sym" type="{http://schemas.openxmlformats.org/drawingml/2006/main}CT_TextFont" minOccurs="0"/&gt;
 *         &lt;element name="hlinkClick" type="{http://schemas.openxmlformats.org/drawingml/2006/main}CT_Hyperlink" minOccurs="0"/&gt;
 *         &lt;element name="hlinkMouseOver" type="{http://schemas.openxmlformats.org/drawingml/2006/main}CT_Hyperlink" minOccurs="0"/&gt;
 *         &lt;element name="extLst" type="{http://schemas.openxmlformats.org/drawingml/2006/main}CT_OfficeArtExtensionList" minOccurs="0"/&gt;
 *       &lt;/sequence&gt;
 *       &lt;attribute name="kumimoji" type="{http://www.w3.org/2001/XMLSchema}boolean" /&gt;
 *       &lt;attribute name="lang" type="{http://schemas.openxmlformats.org/drawingml/2006/main}ST_TextLanguageID" /&gt;
 *       &lt;attribute name="altLang" type="{http://schemas.openxmlformats.org/drawingml/2006/main}ST_TextLanguageID" /&gt;
 *       &lt;attribute name="sz" type="{http://schemas.openxmlformats.org/drawingml/2006/main}ST_TextFontSize" /&gt;
 *       &lt;attribute name="b" type="{http://www.w3.org/2001/XMLSchema}boolean" /&gt;
 *       &lt;attribute name="i" type="{http://www.w3.org/2001/XMLSchema}boolean" /&gt;
 *       &lt;attribute name="u" type="{http://schemas.openxmlformats.org/drawingml/2006/main}ST_TextUnderlineType" /&gt;
 *       &lt;attribute name="strike" type="{http://schemas.openxmlformats.org/drawingml/2006/main}ST_TextStrikeType" /&gt;
 *       &lt;attribute name="kern" type="{http://schemas.openxmlformats.org/drawingml/2006/main}ST_TextNonNegativePoint" /&gt;
 *       &lt;attribute name="cap" type="{http://schemas.openxmlformats.org/drawingml/2006/main}ST_TextCapsType" /&gt;
 *       &lt;attribute name="spc" type="{http://schemas.openxmlformats.org/drawingml/2006/main}ST_TextPoint" /&gt;
 *       &lt;attribute name="normalizeH" type="{http://www.w3.org/2001/XMLSchema}boolean" /&gt;
 *       &lt;attribute name="baseline" type="{http://schemas.openxmlformats.org/drawingml/2006/main}ST_Percentage" /&gt;
 *       &lt;attribute name="noProof" type="{http://www.w3.org/2001/XMLSchema}boolean" /&gt;
 *       &lt;attribute name="dirty" type="{http://www.w3.org/2001/XMLSchema}boolean" default="true" /&gt;
 *       &lt;attribute name="err" type="{http://www.w3.org/2001/XMLSchema}boolean" default="false" /&gt;
 *       &lt;attribute name="smtClean" type="{http://www.w3.org/2001/XMLSchema}boolean" default="true" /&gt;
 *       &lt;attribute name="smtId" type="{http://www.w3.org/2001/XMLSchema}unsignedInt" default="0" /&gt;
 *       &lt;attribute name="bmk" type="{http://www.w3.org/2001/XMLSchema}string" /&gt;
 *     &lt;/restriction&gt;
 *   &lt;/complexContent&gt;
 * &lt;/complexType&gt;
 * </pre>
 * 
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "CT_TextCharacterProperties", propOrder = {
    "ln",
    "noFill",
    "solidFill",
    "gradFill",
    "blipFill",
    "pattFill",
    "grpFill",
    "effectLst",
    "effectDag",
    "highlight",
    "uLnTx",
    "uLn",
    "uFillTx",
    "uFill",
    "latin",
    "ea",
    "cs",
    "sym",
    "hlinkClick",
    "hlinkMouseOver",
    "extLst"
})
public class CTTextCharacterProperties implements Child
{

    protected CTLineProperties ln;
    protected CTNoFillProperties noFill;
    protected CTSolidColorFillProperties solidFill;
    protected CTGradientFillProperties gradFill;
    protected CTBlipFillProperties blipFill;
    protected CTPatternFillProperties pattFill;
    protected CTGroupFillProperties grpFill;
    protected CTEffectList effectLst;
    protected CTEffectContainer effectDag;
    protected CTColor highlight;
    protected CTTextUnderlineLineFollowText uLnTx;
    protected CTLineProperties uLn;
    protected CTTextUnderlineFillFollowText uFillTx;
    protected CTTextUnderlineFillGroupWrapper uFill;
    protected TextFont latin;
    protected TextFont ea;
    protected TextFont cs;
    protected TextFont sym;
    protected CTHyperlink hlinkClick;
    protected CTHyperlink hlinkMouseOver;
    protected CTOfficeArtExtensionList extLst;
    @XmlAttribute(name = "kumimoji")
    protected Boolean kumimoji;
    @XmlAttribute(name = "lang")
    protected String lang;
    @XmlAttribute(name = "altLang")
    protected String altLang;
    @XmlAttribute(name = "sz")
    protected Integer sz;
    @XmlAttribute(name = "b")
    protected Boolean b;
    @XmlAttribute(name = "i")
    protected Boolean i;
    @XmlAttribute(name = "u")
    protected STTextUnderlineType u;
    @XmlAttribute(name = "strike")
    protected STTextStrikeType strike;
    @XmlAttribute(name = "kern")
    protected Integer kern;
    @XmlAttribute(name = "cap")
    protected STTextCapsType cap;
    @XmlAttribute(name = "spc")
    protected Integer spc;
    @XmlAttribute(name = "normalizeH")
    protected Boolean normalizeH;
    @XmlAttribute(name = "baseline")
    protected Integer baseline;
    @XmlAttribute(name = "noProof")
    protected Boolean noProof;
    @XmlAttribute(name = "dirty")
    protected Boolean dirty;
    @XmlAttribute(name = "err")
    protected Boolean err;
    @XmlAttribute(name = "smtClean")
    protected Boolean smtClean;
    @XmlAttribute(name = "smtId")
    @XmlSchemaType(name = "unsignedInt")
    protected Long smtId;
    @XmlAttribute(name = "bmk")
    protected String bmk;
    @XmlTransient
    private Object parent;

    /**
     * Gets the value of the ln property.
     * 
     * @return
     *     possible object is
     *     {@link CTLineProperties }
     *     
     */
    public CTLineProperties getLn() {
        return ln;
    }

    /**
     * Sets the value of the ln property.
     * 
     * @param value
     *     allowed object is
     *     {@link CTLineProperties }
     *     
     */
    public void setLn(CTLineProperties value) {
        this.ln = value;
    }

    /**
     * Gets the value of the noFill property.
     * 
     * @return
     *     possible object is
     *     {@link CTNoFillProperties }
     *     
     */
    public CTNoFillProperties getNoFill() {
        return noFill;
    }

    /**
     * Sets the value of the noFill property.
     * 
     * @param value
     *     allowed object is
     *     {@link CTNoFillProperties }
     *     
     */
    public void setNoFill(CTNoFillProperties value) {
        this.noFill = value;
    }

    /**
     * Gets the value of the solidFill property.
     * 
     * @return
     *     possible object is
     *     {@link CTSolidColorFillProperties }
     *     
     */
    public CTSolidColorFillProperties getSolidFill() {
        return solidFill;
    }

    /**
     * Sets the value of the solidFill property.
     * 
     * @param value
     *     allowed object is
     *     {@link CTSolidColorFillProperties }
     *     
     */
    public void setSolidFill(CTSolidColorFillProperties value) {
        this.solidFill = value;
    }

    /**
     * Gets the value of the gradFill property.
     * 
     * @return
     *     possible object is
     *     {@link CTGradientFillProperties }
     *     
     */
    public CTGradientFillProperties getGradFill() {
        return gradFill;
    }

    /**
     * Sets the value of the gradFill property.
     * 
     * @param value
     *     allowed object is
     *     {@link CTGradientFillProperties }
     *     
     */
    public void setGradFill(CTGradientFillProperties value) {
        this.gradFill = value;
    }

    /**
     * Gets the value of the blipFill property.
     * 
     * @return
     *     possible object is
     *     {@link CTBlipFillProperties }
     *     
     */
    public CTBlipFillProperties getBlipFill() {
        return blipFill;
    }

    /**
     * Sets the value of the blipFill property.
     * 
     * @param value
     *     allowed object is
     *     {@link CTBlipFillProperties }
     *     
     */
    public void setBlipFill(CTBlipFillProperties value) {
        this.blipFill = value;
    }

    /**
     * Gets the value of the pattFill property.
     * 
     * @return
     *     possible object is
     *     {@link CTPatternFillProperties }
     *     
     */
    public CTPatternFillProperties getPattFill() {
        return pattFill;
    }

    /**
     * Sets the value of the pattFill property.
     * 
     * @param value
     *     allowed object is
     *     {@link CTPatternFillProperties }
     *     
     */
    public void setPattFill(CTPatternFillProperties value) {
        this.pattFill = value;
    }

    /**
     * Gets the value of the grpFill property.
     * 
     * @return
     *     possible object is
     *     {@link CTGroupFillProperties }
     *     
     */
    public CTGroupFillProperties getGrpFill() {
        return grpFill;
    }

    /**
     * Sets the value of the grpFill property.
     * 
     * @param value
     *     allowed object is
     *     {@link CTGroupFillProperties }
     *     
     */
    public void setGrpFill(CTGroupFillProperties value) {
        this.grpFill = value;
    }

    /**
     * Gets the value of the effectLst property.
     * 
     * @return
     *     possible object is
     *     {@link CTEffectList }
     *     
     */
    public CTEffectList getEffectLst() {
        return effectLst;
    }

    /**
     * Sets the value of the effectLst property.
     * 
     * @param value
     *     allowed object is
     *     {@link CTEffectList }
     *     
     */
    public void setEffectLst(CTEffectList value) {
        this.effectLst = value;
    }

    /**
     * Gets the value of the effectDag property.
     * 
     * @return
     *     possible object is
     *     {@link CTEffectContainer }
     *     
     */
    public CTEffectContainer getEffectDag() {
        return effectDag;
    }

    /**
     * Sets the value of the effectDag property.
     * 
     * @param value
     *     allowed object is
     *     {@link CTEffectContainer }
     *     
     */
    public void setEffectDag(CTEffectContainer value) {
        this.effectDag = value;
    }

    /**
     * Gets the value of the highlight property.
     * 
     * @return
     *     possible object is
     *     {@link CTColor }
     *     
     */
    public CTColor getHighlight() {
        return highlight;
    }

    /**
     * Sets the value of the highlight property.
     * 
     * @param value
     *     allowed object is
     *     {@link CTColor }
     *     
     */
    public void setHighlight(CTColor value) {
        this.highlight = value;
    }

    /**
     * Gets the value of the uLnTx property.
     * 
     * @return
     *     possible object is
     *     {@link CTTextUnderlineLineFollowText }
     *     
     */
    public CTTextUnderlineLineFollowText getULnTx() {
        return uLnTx;
    }

    /**
     * Sets the value of the uLnTx property.
     * 
     * @param value
     *     allowed object is
     *     {@link CTTextUnderlineLineFollowText }
     *     
     */
    public void setULnTx(CTTextUnderlineLineFollowText value) {
        this.uLnTx = value;
    }

    /**
     * Gets the value of the uLn property.
     * 
     * @return
     *     possible object is
     *     {@link CTLineProperties }
     *     
     */
    public CTLineProperties getULn() {
        return uLn;
    }

    /**
     * Sets the value of the uLn property.
     * 
     * @param value
     *     allowed object is
     *     {@link CTLineProperties }
     *     
     */
    public void setULn(CTLineProperties value) {
        this.uLn = value;
    }

    /**
     * Gets the value of the uFillTx property.
     * 
     * @return
     *     possible object is
     *     {@link CTTextUnderlineFillFollowText }
     *     
     */
    public CTTextUnderlineFillFollowText getUFillTx() {
        return uFillTx;
    }

    /**
     * Sets the value of the uFillTx property.
     * 
     * @param value
     *     allowed object is
     *     {@link CTTextUnderlineFillFollowText }
     *     
     */
    public void setUFillTx(CTTextUnderlineFillFollowText value) {
        this.uFillTx = value;
    }

    /**
     * Gets the value of the uFill property.
     * 
     * @return
     *     possible object is
     *     {@link CTTextUnderlineFillGroupWrapper }
     *     
     */
    public CTTextUnderlineFillGroupWrapper getUFill() {
        return uFill;
    }

    /**
     * Sets the value of the uFill property.
     * 
     * @param value
     *     allowed object is
     *     {@link CTTextUnderlineFillGroupWrapper }
     *     
     */
    public void setUFill(CTTextUnderlineFillGroupWrapper value) {
        this.uFill = value;
    }

    /**
     * Gets the value of the latin property.
     * 
     * @return
     *     possible object is
     *     {@link TextFont }
     *     
     */
    public TextFont getLatin() {
        return latin;
    }

    /**
     * Sets the value of the latin property.
     * 
     * @param value
     *     allowed object is
     *     {@link TextFont }
     *     
     */
    public void setLatin(TextFont value) {
        this.latin = value;
    }

    /**
     * Gets the value of the ea property.
     * 
     * @return
     *     possible object is
     *     {@link TextFont }
     *     
     */
    public TextFont getEa() {
        return ea;
    }

    /**
     * Sets the value of the ea property.
     * 
     * @param value
     *     allowed object is
     *     {@link TextFont }
     *     
     */
    public void setEa(TextFont value) {
        this.ea = value;
    }

    /**
     * Gets the value of the cs property.
     * 
     * @return
     *     possible object is
     *     {@link TextFont }
     *     
     */
    public TextFont getCs() {
        return cs;
    }

    /**
     * Sets the value of the cs property.
     * 
     * @param value
     *     allowed object is
     *     {@link TextFont }
     *     
     */
    public void setCs(TextFont value) {
        this.cs = value;
    }

    /**
     * Gets the value of the sym property.
     * 
     * @return
     *     possible object is
     *     {@link TextFont }
     *     
     */
    public TextFont getSym() {
        return sym;
    }

    /**
     * Sets the value of the sym property.
     * 
     * @param value
     *     allowed object is
     *     {@link TextFont }
     *     
     */
    public void setSym(TextFont value) {
        this.sym = value;
    }

    /**
     * Gets the value of the hlinkClick property.
     * 
     * @return
     *     possible object is
     *     {@link CTHyperlink }
     *     
     */
    public CTHyperlink getHlinkClick() {
        return hlinkClick;
    }

    /**
     * Sets the value of the hlinkClick property.
     * 
     * @param value
     *     allowed object is
     *     {@link CTHyperlink }
     *     
     */
    public void setHlinkClick(CTHyperlink value) {
        this.hlinkClick = value;
    }

    /**
     * Gets the value of the hlinkMouseOver property.
     * 
     * @return
     *     possible object is
     *     {@link CTHyperlink }
     *     
     */
    public CTHyperlink getHlinkMouseOver() {
        return hlinkMouseOver;
    }

    /**
     * Sets the value of the hlinkMouseOver property.
     * 
     * @param value
     *     allowed object is
     *     {@link CTHyperlink }
     *     
     */
    public void setHlinkMouseOver(CTHyperlink value) {
        this.hlinkMouseOver = value;
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
     * Gets the value of the kumimoji property.
     * 
     * @return
     *     possible object is
     *     {@link Boolean }
     *     
     */
    public Boolean isKumimoji() {
        return kumimoji;
    }

    /**
     * Sets the value of the kumimoji property.
     * 
     * @param value
     *     allowed object is
     *     {@link Boolean }
     *     
     */
    public void setKumimoji(Boolean value) {
        this.kumimoji = value;
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
     * Gets the value of the altLang property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getAltLang() {
        return altLang;
    }

    /**
     * Sets the value of the altLang property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setAltLang(String value) {
        this.altLang = value;
    }

    /**
     * Gets the value of the sz property.
     * 
     * @return
     *     possible object is
     *     {@link Integer }
     *     
     */
    public Integer getSz() {
        return sz;
    }

    /**
     * Sets the value of the sz property.
     * 
     * @param value
     *     allowed object is
     *     {@link Integer }
     *     
     */
    public void setSz(Integer value) {
        this.sz = value;
    }

    /**
     * Gets the value of the b property.
     * 
     * @return
     *     possible object is
     *     {@link Boolean }
     *     
     */
    public Boolean isB() {
        return b;
    }

    /**
     * Sets the value of the b property.
     * 
     * @param value
     *     allowed object is
     *     {@link Boolean }
     *     
     */
    public void setB(Boolean value) {
        this.b = value;
    }

    /**
     * Gets the value of the i property.
     * 
     * @return
     *     possible object is
     *     {@link Boolean }
     *     
     */
    public Boolean isI() {
        return i;
    }

    /**
     * Sets the value of the i property.
     * 
     * @param value
     *     allowed object is
     *     {@link Boolean }
     *     
     */
    public void setI(Boolean value) {
        this.i = value;
    }

    /**
     * Gets the value of the u property.
     * 
     * @return
     *     possible object is
     *     {@link STTextUnderlineType }
     *     
     */
    public STTextUnderlineType getU() {
        return u;
    }

    /**
     * Sets the value of the u property.
     * 
     * @param value
     *     allowed object is
     *     {@link STTextUnderlineType }
     *     
     */
    public void setU(STTextUnderlineType value) {
        this.u = value;
    }

    /**
     * Gets the value of the strike property.
     * 
     * @return
     *     possible object is
     *     {@link STTextStrikeType }
     *     
     */
    public STTextStrikeType getStrike() {
        return strike;
    }

    /**
     * Sets the value of the strike property.
     * 
     * @param value
     *     allowed object is
     *     {@link STTextStrikeType }
     *     
     */
    public void setStrike(STTextStrikeType value) {
        this.strike = value;
    }

    /**
     * Gets the value of the kern property.
     * 
     * @return
     *     possible object is
     *     {@link Integer }
     *     
     */
    public Integer getKern() {
        return kern;
    }

    /**
     * Sets the value of the kern property.
     * 
     * @param value
     *     allowed object is
     *     {@link Integer }
     *     
     */
    public void setKern(Integer value) {
        this.kern = value;
    }

    /**
     * Gets the value of the cap property.
     * 
     * @return
     *     possible object is
     *     {@link STTextCapsType }
     *     
     */
    public STTextCapsType getCap() {
        return cap;
    }

    /**
     * Sets the value of the cap property.
     * 
     * @param value
     *     allowed object is
     *     {@link STTextCapsType }
     *     
     */
    public void setCap(STTextCapsType value) {
        this.cap = value;
    }

    /**
     * Gets the value of the spc property.
     * 
     * @return
     *     possible object is
     *     {@link Integer }
     *     
     */
    public Integer getSpc() {
        return spc;
    }

    /**
     * Sets the value of the spc property.
     * 
     * @param value
     *     allowed object is
     *     {@link Integer }
     *     
     */
    public void setSpc(Integer value) {
        this.spc = value;
    }

    /**
     * Gets the value of the normalizeH property.
     * 
     * @return
     *     possible object is
     *     {@link Boolean }
     *     
     */
    public Boolean isNormalizeH() {
        return normalizeH;
    }

    /**
     * Sets the value of the normalizeH property.
     * 
     * @param value
     *     allowed object is
     *     {@link Boolean }
     *     
     */
    public void setNormalizeH(Boolean value) {
        this.normalizeH = value;
    }

    /**
     * Gets the value of the baseline property.
     * 
     * @return
     *     possible object is
     *     {@link Integer }
     *     
     */
    public Integer getBaseline() {
        return baseline;
    }

    /**
     * Sets the value of the baseline property.
     * 
     * @param value
     *     allowed object is
     *     {@link Integer }
     *     
     */
    public void setBaseline(Integer value) {
        this.baseline = value;
    }

    /**
     * Gets the value of the noProof property.
     * 
     * @return
     *     possible object is
     *     {@link Boolean }
     *     
     */
    public Boolean isNoProof() {
        return noProof;
    }

    /**
     * Sets the value of the noProof property.
     * 
     * @param value
     *     allowed object is
     *     {@link Boolean }
     *     
     */
    public void setNoProof(Boolean value) {
        this.noProof = value;
    }

    /**
     * Gets the value of the dirty property.
     * 
     * @return
     *     possible object is
     *     {@link Boolean }
     *     
     */
    public boolean isDirty() {
        if (dirty == null) {
            return true;
        } else {
            return dirty;
        }
    }

    /**
     * Sets the value of the dirty property.
     * 
     * @param value
     *     allowed object is
     *     {@link Boolean }
     *     
     */
    public void setDirty(Boolean value) {
        this.dirty = value;
    }

    /**
     * Gets the value of the err property.
     * 
     * @return
     *     possible object is
     *     {@link Boolean }
     *     
     */
    public boolean isErr() {
        if (err == null) {
            return false;
        } else {
            return err;
        }
    }

    /**
     * Sets the value of the err property.
     * 
     * @param value
     *     allowed object is
     *     {@link Boolean }
     *     
     */
    public void setErr(Boolean value) {
        this.err = value;
    }

    /**
     * Gets the value of the smtClean property.
     * 
     * @return
     *     possible object is
     *     {@link Boolean }
     *     
     */
    public boolean isSmtClean() {
        if (smtClean == null) {
            return true;
        } else {
            return smtClean;
        }
    }

    /**
     * Sets the value of the smtClean property.
     * 
     * @param value
     *     allowed object is
     *     {@link Boolean }
     *     
     */
    public void setSmtClean(Boolean value) {
        this.smtClean = value;
    }

    /**
     * Gets the value of the smtId property.
     * 
     * @return
     *     possible object is
     *     {@link Long }
     *     
     */
    public long getSmtId() {
        if (smtId == null) {
            return  0L;
        } else {
            return smtId;
        }
    }

    /**
     * Sets the value of the smtId property.
     * 
     * @param value
     *     allowed object is
     *     {@link Long }
     *     
     */
    public void setSmtId(Long value) {
        this.smtId = value;
    }

    /**
     * Gets the value of the bmk property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getBmk() {
        return bmk;
    }

    /**
     * Sets the value of the bmk property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setBmk(String value) {
        this.bmk = value;
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
