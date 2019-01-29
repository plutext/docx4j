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

import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlTransient;

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



/**
 * A common superclass for RPr and ParaRPr.
 * 
 * For these common fields to be listed in propOrder, it has to be
 * marked @XmlTransient 
 * see further http://stackoverflow.com/questions/12471674/declare-proporder-over-extended-class
 * and http://stackoverflow.com/questions/6790168/can-should-i-list-inherited-properties-for-a-jaxb-mapped-bean-in-the-proporder
 * 
 * @author jharrop
 * @since 3.2.0
 */
@XmlTransient  
public abstract class RPrAbstract
{

    protected RStyle rStyle;
    protected RFonts rFonts;
    protected BooleanDefaultTrue b;
    protected BooleanDefaultTrue bCs;
    protected BooleanDefaultTrue i;
    protected BooleanDefaultTrue iCs;
    protected BooleanDefaultTrue caps;
    protected BooleanDefaultTrue smallCaps;
    protected BooleanDefaultTrue strike;
    protected BooleanDefaultTrue dstrike;
    protected BooleanDefaultTrue outline;
    protected BooleanDefaultTrue shadow;
    protected BooleanDefaultTrue emboss;
    protected BooleanDefaultTrue imprint;
    protected BooleanDefaultTrue noProof;
    protected BooleanDefaultTrue snapToGrid;
    protected BooleanDefaultTrue vanish;
    protected BooleanDefaultTrue webHidden;
    protected Color color;
    protected CTSignedTwipsMeasure spacing;
    protected CTTextScale w;
    protected HpsMeasure kern;
    protected CTSignedHpsMeasure position;
    protected HpsMeasure sz;
    protected HpsMeasure szCs;
    protected Highlight highlight;
    protected U u;
    protected CTTextEffect effect;
    protected CTBorder bdr;
    protected CTShd shd;
    protected CTFitText fitText;
    protected CTVerticalAlignRun vertAlign;
    protected BooleanDefaultTrue rtl;
    protected BooleanDefaultTrue cs;
    protected CTEm em;
    protected CTLanguage lang;
    protected CTEastAsianLayout eastAsianLayout;
    protected BooleanDefaultTrue specVanish;
    protected BooleanDefaultTrue oMath;
    @XmlElement(namespace = "http://schemas.microsoft.com/office/word/2010/wordml")
    protected CTGlow glow;
    @XmlElement(name = "shadow", namespace = "http://schemas.microsoft.com/office/word/2010/wordml")
    protected CTShadow shadow14;
    @XmlElement(namespace = "http://schemas.microsoft.com/office/word/2010/wordml")
    protected CTReflection reflection;
    @XmlElement(namespace = "http://schemas.microsoft.com/office/word/2010/wordml")
    protected CTTextOutlineEffect textOutline;
    @XmlElement(namespace = "http://schemas.microsoft.com/office/word/2010/wordml")
    protected CTFillTextEffect textFill;
    @XmlElement(name = "scene3d", namespace = "http://schemas.microsoft.com/office/word/2010/wordml")
    protected CTScene3D scene3D;
    @XmlElement(name = "props3d", namespace = "http://schemas.microsoft.com/office/word/2010/wordml")
    protected CTProps3D props3D;
    @XmlElement(namespace = "http://schemas.microsoft.com/office/word/2010/wordml")
    protected CTLigatures ligatures;
    @XmlElement(namespace = "http://schemas.microsoft.com/office/word/2010/wordml")
    protected CTNumForm numForm;
    @XmlElement(namespace = "http://schemas.microsoft.com/office/word/2010/wordml")
    protected CTNumSpacing numSpacing;
    @XmlElement(namespace = "http://schemas.microsoft.com/office/word/2010/wordml")
    protected CTStylisticSets stylisticSets;
    @XmlElement(namespace = "http://schemas.microsoft.com/office/word/2010/wordml")
    protected CTOnOff cntxtAlts;
    protected CTRPrChange rPrChange;

    /**
     * Gets the value of the rStyle property.
     * 
     * @return
     *     possible object is
     *     {@link RStyle }
     *     
     */
    public RStyle getRStyle() {
        return rStyle;
    }

    /**
     * Sets the value of the rStyle property.
     * 
     * @param value
     *     allowed object is
     *     {@link RStyle }
     *     
     */
    public void setRStyle(RStyle value) {
        this.rStyle = value;
    }

    /**
     * Gets the value of the rFonts property.
     * 
     * @return
     *     possible object is
     *     {@link RFonts }
     *     
     */
    public RFonts getRFonts() {
        return rFonts;
    }

    /**
     * Sets the value of the rFonts property.
     * 
     * @param value
     *     allowed object is
     *     {@link RFonts }
     *     
     */
    public void setRFonts(RFonts value) {
        this.rFonts = value;
    }

    /**
     * Gets the value of the b property.
     * 
     * @return
     *     possible object is
     *     {@link BooleanDefaultTrue }
     *     
     */
    public BooleanDefaultTrue getB() {
        return b;
    }

    /**
     * Sets the value of the b property.
     * 
     * @param value
     *     allowed object is
     *     {@link BooleanDefaultTrue }
     *     
     */
    public void setB(BooleanDefaultTrue value) {
        this.b = value;
    }

    /**
     * Gets the value of the bCs property.
     * 
     * @return
     *     possible object is
     *     {@link BooleanDefaultTrue }
     *     
     */
    public BooleanDefaultTrue getBCs() {
        return bCs;
    }

    /**
     * Sets the value of the bCs property.
     * 
     * @param value
     *     allowed object is
     *     {@link BooleanDefaultTrue }
     *     
     */
    public void setBCs(BooleanDefaultTrue value) {
        this.bCs = value;
    }

    /**
     * Gets the value of the i property.
     * 
     * @return
     *     possible object is
     *     {@link BooleanDefaultTrue }
     *     
     */
    public BooleanDefaultTrue getI() {
        return i;
    }

    /**
     * Sets the value of the i property.
     * 
     * @param value
     *     allowed object is
     *     {@link BooleanDefaultTrue }
     *     
     */
    public void setI(BooleanDefaultTrue value) {
        this.i = value;
    }

    /**
     * Gets the value of the iCs property.
     * 
     * @return
     *     possible object is
     *     {@link BooleanDefaultTrue }
     *     
     */
    public BooleanDefaultTrue getICs() {
        return iCs;
    }

    /**
     * Sets the value of the iCs property.
     * 
     * @param value
     *     allowed object is
     *     {@link BooleanDefaultTrue }
     *     
     */
    public void setICs(BooleanDefaultTrue value) {
        this.iCs = value;
    }

    /**
     * Gets the value of the caps property.
     * 
     * @return
     *     possible object is
     *     {@link BooleanDefaultTrue }
     *     
     */
    public BooleanDefaultTrue getCaps() {
        return caps;
    }

    /**
     * Sets the value of the caps property.
     * 
     * @param value
     *     allowed object is
     *     {@link BooleanDefaultTrue }
     *     
     */
    public void setCaps(BooleanDefaultTrue value) {
        this.caps = value;
    }

    /**
     * Gets the value of the smallCaps property.
     * 
     * @return
     *     possible object is
     *     {@link BooleanDefaultTrue }
     *     
     */
    public BooleanDefaultTrue getSmallCaps() {
        return smallCaps;
    }

    /**
     * Sets the value of the smallCaps property.
     * 
     * @param value
     *     allowed object is
     *     {@link BooleanDefaultTrue }
     *     
     */
    public void setSmallCaps(BooleanDefaultTrue value) {
        this.smallCaps = value;
    }

    /**
     * Gets the value of the strike property.
     * 
     * @return
     *     possible object is
     *     {@link BooleanDefaultTrue }
     *     
     */
    public BooleanDefaultTrue getStrike() {
        return strike;
    }

    /**
     * Sets the value of the strike property.
     * 
     * @param value
     *     allowed object is
     *     {@link BooleanDefaultTrue }
     *     
     */
    public void setStrike(BooleanDefaultTrue value) {
        this.strike = value;
    }

    /**
     * Gets the value of the dstrike property.
     * 
     * @return
     *     possible object is
     *     {@link BooleanDefaultTrue }
     *     
     */
    public BooleanDefaultTrue getDstrike() {
        return dstrike;
    }

    /**
     * Sets the value of the dstrike property.
     * 
     * @param value
     *     allowed object is
     *     {@link BooleanDefaultTrue }
     *     
     */
    public void setDstrike(BooleanDefaultTrue value) {
        this.dstrike = value;
    }

    /**
     * Gets the value of the outline property.
     * 
     * @return
     *     possible object is
     *     {@link BooleanDefaultTrue }
     *     
     */
    public BooleanDefaultTrue getOutline() {
        return outline;
    }

    /**
     * Sets the value of the outline property.
     * 
     * @param value
     *     allowed object is
     *     {@link BooleanDefaultTrue }
     *     
     */
    public void setOutline(BooleanDefaultTrue value) {
        this.outline = value;
    }

    /**
     * Gets the value of the shadow property.
     * 
     * @return
     *     possible object is
     *     {@link BooleanDefaultTrue }
     *     
     */
    public BooleanDefaultTrue getShadow() {
        return shadow;
    }

    /**
     * Sets the value of the shadow property.
     * 
     * @param value
     *     allowed object is
     *     {@link BooleanDefaultTrue }
     *     
     */
    public void setShadow(BooleanDefaultTrue value) {
        this.shadow = value;
    }

    /**
     * Gets the value of the emboss property.
     * 
     * @return
     *     possible object is
     *     {@link BooleanDefaultTrue }
     *     
     */
    public BooleanDefaultTrue getEmboss() {
        return emboss;
    }

    /**
     * Sets the value of the emboss property.
     * 
     * @param value
     *     allowed object is
     *     {@link BooleanDefaultTrue }
     *     
     */
    public void setEmboss(BooleanDefaultTrue value) {
        this.emboss = value;
    }

    /**
     * Gets the value of the imprint property.
     * 
     * @return
     *     possible object is
     *     {@link BooleanDefaultTrue }
     *     
     */
    public BooleanDefaultTrue getImprint() {
        return imprint;
    }

    /**
     * Sets the value of the imprint property.
     * 
     * @param value
     *     allowed object is
     *     {@link BooleanDefaultTrue }
     *     
     */
    public void setImprint(BooleanDefaultTrue value) {
        this.imprint = value;
    }

    /**
     * Gets the value of the noProof property.
     * 
     * @return
     *     possible object is
     *     {@link BooleanDefaultTrue }
     *     
     */
    public BooleanDefaultTrue getNoProof() {
        return noProof;
    }

    /**
     * Sets the value of the noProof property.
     * 
     * @param value
     *     allowed object is
     *     {@link BooleanDefaultTrue }
     *     
     */
    public void setNoProof(BooleanDefaultTrue value) {
        this.noProof = value;
    }

    /**
     * Gets the value of the snapToGrid property.
     * 
     * @return
     *     possible object is
     *     {@link BooleanDefaultTrue }
     *     
     */
    public BooleanDefaultTrue getSnapToGrid() {
        return snapToGrid;
    }

    /**
     * Sets the value of the snapToGrid property.
     * 
     * @param value
     *     allowed object is
     *     {@link BooleanDefaultTrue }
     *     
     */
    public void setSnapToGrid(BooleanDefaultTrue value) {
        this.snapToGrid = value;
    }

    /**
     * Gets the value of the vanish property.
     * 
     * @return
     *     possible object is
     *     {@link BooleanDefaultTrue }
     *     
     */
    public BooleanDefaultTrue getVanish() {
        return vanish;
    }

    /**
     * Sets the value of the vanish property.
     * 
     * @param value
     *     allowed object is
     *     {@link BooleanDefaultTrue }
     *     
     */
    public void setVanish(BooleanDefaultTrue value) {
        this.vanish = value;
    }

    /**
     * Gets the value of the webHidden property.
     * 
     * @return
     *     possible object is
     *     {@link BooleanDefaultTrue }
     *     
     */
    public BooleanDefaultTrue getWebHidden() {
        return webHidden;
    }

    /**
     * Sets the value of the webHidden property.
     * 
     * @param value
     *     allowed object is
     *     {@link BooleanDefaultTrue }
     *     
     */
    public void setWebHidden(BooleanDefaultTrue value) {
        this.webHidden = value;
    }

    /**
     * Gets the value of the color property.
     * 
     * @return
     *     possible object is
     *     {@link Color }
     *     
     */
    public Color getColor() {
        return color;
    }

    /**
     * Sets the value of the color property.
     * 
     * @param value
     *     allowed object is
     *     {@link Color }
     *     
     */
    public void setColor(Color value) {
        this.color = value;
    }

    /**
     * Gets the value of the spacing property.
     * 
     * @return
     *     possible object is
     *     {@link CTSignedTwipsMeasure }
     *     
     */
    public CTSignedTwipsMeasure getSpacing() {
        return spacing;
    }

    /**
     * Sets the value of the spacing property.
     * 
     * @param value
     *     allowed object is
     *     {@link CTSignedTwipsMeasure }
     *     
     */
    public void setSpacing(CTSignedTwipsMeasure value) {
        this.spacing = value;
    }

    /**
     * Gets the value of the w property.
     * 
     * @return
     *     possible object is
     *     {@link CTTextScale }
     *     
     */
    public CTTextScale getW() {
        return w;
    }

    /**
     * Sets the value of the w property.
     * 
     * @param value
     *     allowed object is
     *     {@link CTTextScale }
     *     
     */
    public void setW(CTTextScale value) {
        this.w = value;
    }

    /**
     * Gets the value of the kern property.
     * 
     * @return
     *     possible object is
     *     {@link HpsMeasure }
     *     
     */
    public HpsMeasure getKern() {
        return kern;
    }

    /**
     * Sets the value of the kern property.
     * 
     * @param value
     *     allowed object is
     *     {@link HpsMeasure }
     *     
     */
    public void setKern(HpsMeasure value) {
        this.kern = value;
    }

    /**
     * Gets the value of the position property.
     * 
     * @return
     *     possible object is
     *     {@link CTSignedHpsMeasure }
     *     
     */
    public CTSignedHpsMeasure getPosition() {
        return position;
    }

    /**
     * Sets the value of the position property.
     * 
     * @param value
     *     allowed object is
     *     {@link CTSignedHpsMeasure }
     *     
     */
    public void setPosition(CTSignedHpsMeasure value) {
        this.position = value;
    }

    /**
     * Gets the value of the sz property.
     * 
     * @return
     *     possible object is
     *     {@link HpsMeasure }
     *     
     */
    public HpsMeasure getSz() {
        return sz;
    }

    /**
     * Sets the value of the sz property.
     * 
     * @param value
     *     allowed object is
     *     {@link HpsMeasure }
     *     
     */
    public void setSz(HpsMeasure value) {
        this.sz = value;
    }

    /**
     * Gets the value of the szCs property.
     * 
     * @return
     *     possible object is
     *     {@link HpsMeasure }
     *     
     */
    public HpsMeasure getSzCs() {
        return szCs;
    }

    /**
     * Sets the value of the szCs property.
     * 
     * @param value
     *     allowed object is
     *     {@link HpsMeasure }
     *     
     */
    public void setSzCs(HpsMeasure value) {
        this.szCs = value;
    }

    /**
     * Gets the value of the highlight property.
     * 
     * @return
     *     possible object is
     *     {@link Highlight }
     *     
     */
    public Highlight getHighlight() {
        return highlight;
    }

    /**
     * Sets the value of the highlight property.
     * 
     * @param value
     *     allowed object is
     *     {@link Highlight }
     *     
     */
    public void setHighlight(Highlight value) {
        this.highlight = value;
    }

    /**
     * Gets the value of the u property.
     * 
     * @return
     *     possible object is
     *     {@link U }
     *     
     */
    public U getU() {
        return u;
    }

    /**
     * Sets the value of the u property.
     * 
     * @param value
     *     allowed object is
     *     {@link U }
     *     
     */
    public void setU(U value) {
        this.u = value;
    }

    /**
     * Gets the value of the effect property.
     * 
     * @return
     *     possible object is
     *     {@link CTTextEffect }
     *     
     */
    public CTTextEffect getEffect() {
        return effect;
    }

    /**
     * Sets the value of the effect property.
     * 
     * @param value
     *     allowed object is
     *     {@link CTTextEffect }
     *     
     */
    public void setEffect(CTTextEffect value) {
        this.effect = value;
    }

    /**
     * Gets the value of the bdr property.
     * 
     * @return
     *     possible object is
     *     {@link CTBorder }
     *     
     */
    public CTBorder getBdr() {
        return bdr;
    }

    /**
     * Sets the value of the bdr property.
     * 
     * @param value
     *     allowed object is
     *     {@link CTBorder }
     *     
     */
    public void setBdr(CTBorder value) {
        this.bdr = value;
    }

    /**
     * Gets the value of the shd property.
     * 
     * @return
     *     possible object is
     *     {@link CTShd }
     *     
     */
    public CTShd getShd() {
        return shd;
    }

    /**
     * Sets the value of the shd property.
     * 
     * @param value
     *     allowed object is
     *     {@link CTShd }
     *     
     */
    public void setShd(CTShd value) {
        this.shd = value;
    }

    /**
     * Gets the value of the fitText property.
     * 
     * @return
     *     possible object is
     *     {@link CTFitText }
     *     
     */
    public CTFitText getFitText() {
        return fitText;
    }

    /**
     * Sets the value of the fitText property.
     * 
     * @param value
     *     allowed object is
     *     {@link CTFitText }
     *     
     */
    public void setFitText(CTFitText value) {
        this.fitText = value;
    }

    /**
     * Gets the value of the vertAlign property.
     * 
     * @return
     *     possible object is
     *     {@link CTVerticalAlignRun }
     *     
     */
    public CTVerticalAlignRun getVertAlign() {
        return vertAlign;
    }

    /**
     * Sets the value of the vertAlign property.
     * 
     * @param value
     *     allowed object is
     *     {@link CTVerticalAlignRun }
     *     
     */
    public void setVertAlign(CTVerticalAlignRun value) {
        this.vertAlign = value;
    }

    /**
     * Gets the value of the rtl property.
     * 
     * @return
     *     possible object is
     *     {@link BooleanDefaultTrue }
     *     
     */
    public BooleanDefaultTrue getRtl() {
        return rtl;
    }

    /**
     * Sets the value of the rtl property.
     * 
     * @param value
     *     allowed object is
     *     {@link BooleanDefaultTrue }
     *     
     */
    public void setRtl(BooleanDefaultTrue value) {
        this.rtl = value;
    }

    /**
     * Gets the value of the cs property.
     * 
     * @return
     *     possible object is
     *     {@link BooleanDefaultTrue }
     *     
     */
    public BooleanDefaultTrue getCs() {
        return cs;
    }

    /**
     * Sets the value of the cs property.
     * 
     * @param value
     *     allowed object is
     *     {@link BooleanDefaultTrue }
     *     
     */
    public void setCs(BooleanDefaultTrue value) {
        this.cs = value;
    }

    /**
     * Gets the value of the em property.
     * 
     * @return
     *     possible object is
     *     {@link CTEm }
     *     
     */
    public CTEm getEm() {
        return em;
    }

    /**
     * Sets the value of the em property.
     * 
     * @param value
     *     allowed object is
     *     {@link CTEm }
     *     
     */
    public void setEm(CTEm value) {
        this.em = value;
    }

    /**
     * Gets the value of the lang property.
     * 
     * @return
     *     possible object is
     *     {@link CTLanguage }
     *     
     */
    public CTLanguage getLang() {
        return lang;
    }

    /**
     * Sets the value of the lang property.
     * 
     * @param value
     *     allowed object is
     *     {@link CTLanguage }
     *     
     */
    public void setLang(CTLanguage value) {
        this.lang = value;
    }

    /**
     * Gets the value of the eastAsianLayout property.
     * 
     * @return
     *     possible object is
     *     {@link CTEastAsianLayout }
     *     
     */
    public CTEastAsianLayout getEastAsianLayout() {
        return eastAsianLayout;
    }

    /**
     * Sets the value of the eastAsianLayout property.
     * 
     * @param value
     *     allowed object is
     *     {@link CTEastAsianLayout }
     *     
     */
    public void setEastAsianLayout(CTEastAsianLayout value) {
        this.eastAsianLayout = value;
    }

    /**
     * Gets the value of the specVanish property.
     * 
     * @return
     *     possible object is
     *     {@link BooleanDefaultTrue }
     *     
     */
    public BooleanDefaultTrue getSpecVanish() {
        return specVanish;
    }

    /**
     * Sets the value of the specVanish property.
     * 
     * @param value
     *     allowed object is
     *     {@link BooleanDefaultTrue }
     *     
     */
    public void setSpecVanish(BooleanDefaultTrue value) {
        this.specVanish = value;
    }

    /**
     * Gets the value of the oMath property.
     * 
     * @return
     *     possible object is
     *     {@link BooleanDefaultTrue }
     *     
     */
    public BooleanDefaultTrue getOMath() {
        return oMath;
    }

    /**
     * Sets the value of the oMath property.
     * 
     * @param value
     *     allowed object is
     *     {@link BooleanDefaultTrue }
     *     
     */
    public void setOMath(BooleanDefaultTrue value) {
        this.oMath = value;
    }

    /**
     * Gets the value of the glow property.
     * 
     * @return
     *     possible object is
     *     {@link CTGlow }
     *     
     */
    public CTGlow getGlow() {
        return glow;
    }

    /**
     * Sets the value of the glow property.
     * 
     * @param value
     *     allowed object is
     *     {@link CTGlow }
     *     
     */
    public void setGlow(CTGlow value) {
        this.glow = value;
    }

    /**
     * Gets the value of the shadow14 property.
     * 
     * @return
     *     possible object is
     *     {@link CTShadow }
     *     
     */
    public CTShadow getShadow14() {
        return shadow14;
    }

    /**
     * Sets the value of the shadow14 property.
     * 
     * @param value
     *     allowed object is
     *     {@link CTShadow }
     *     
     */
    public void setShadow14(CTShadow value) {
        this.shadow14 = value;
    }

    /**
     * Gets the value of the reflection property.
     * 
     * @return
     *     possible object is
     *     {@link CTReflection }
     *     
     */
    public CTReflection getReflection() {
        return reflection;
    }

    /**
     * Sets the value of the reflection property.
     * 
     * @param value
     *     allowed object is
     *     {@link CTReflection }
     *     
     */
    public void setReflection(CTReflection value) {
        this.reflection = value;
    }

    /**
     * Gets the value of the textOutline property.
     * 
     * @return
     *     possible object is
     *     {@link CTTextOutlineEffect }
     *     
     */
    public CTTextOutlineEffect getTextOutline() {
        return textOutline;
    }

    /**
     * Sets the value of the textOutline property.
     * 
     * @param value
     *     allowed object is
     *     {@link CTTextOutlineEffect }
     *     
     */
    public void setTextOutline(CTTextOutlineEffect value) {
        this.textOutline = value;
    }

    /**
     * Gets the value of the textFill property.
     * 
     * @return
     *     possible object is
     *     {@link CTFillTextEffect }
     *     
     */
    public CTFillTextEffect getTextFill() {
        return textFill;
    }

    /**
     * Sets the value of the textFill property.
     * 
     * @param value
     *     allowed object is
     *     {@link CTFillTextEffect }
     *     
     */
    public void setTextFill(CTFillTextEffect value) {
        this.textFill = value;
    }

    /**
     * Gets the value of the scene3D property.
     * 
     * @return
     *     possible object is
     *     {@link CTScene3D }
     *     
     */
    public CTScene3D getScene3D() {
        return scene3D;
    }

    /**
     * Sets the value of the scene3D property.
     * 
     * @param value
     *     allowed object is
     *     {@link CTScene3D }
     *     
     */
    public void setScene3D(CTScene3D value) {
        this.scene3D = value;
    }

    /**
     * Gets the value of the props3D property.
     * 
     * @return
     *     possible object is
     *     {@link CTProps3D }
     *     
     */
    public CTProps3D getProps3D() {
        return props3D;
    }

    /**
     * Sets the value of the props3D property.
     * 
     * @param value
     *     allowed object is
     *     {@link CTProps3D }
     *     
     */
    public void setProps3D(CTProps3D value) {
        this.props3D = value;
    }
    
    /**
     * Gets the value of the ligatures property.
     * 
     * @return
     *     possible object is
     *     {@link CTLigatures }
     *     
     */
    public CTLigatures getLigatures() {
        return ligatures;
    }

    /**
     * Sets the value of the ligatures property.
     * 
     * @param value
     *     allowed object is
     *     {@link CTLigatures }
     *     
     */
    public void setLigatures(CTLigatures value) {
        this.ligatures = value;
    }

    /**
     * Gets the value of the numForm property.
     * 
     * @return
     *     possible object is
     *     {@link CTNumForm }
     *     
     */
    public CTNumForm getNumForm() {
        return numForm;
    }

    /**
     * Sets the value of the numForm property.
     * 
     * @param value
     *     allowed object is
     *     {@link CTNumForm }
     *     
     */
    public void setNumForm(CTNumForm value) {
        this.numForm = value;
    }

    /**
     * Gets the value of the numSpacing property.
     * 
     * @return
     *     possible object is
     *     {@link CTNumSpacing }
     *     
     */
    public CTNumSpacing getNumSpacing() {
        return numSpacing;
    }

    /**
     * Sets the value of the numSpacing property.
     * 
     * @param value
     *     allowed object is
     *     {@link CTNumSpacing }
     *     
     */
    public void setNumSpacing(CTNumSpacing value) {
        this.numSpacing = value;
    }

    /**
     * Gets the value of the stylisticSets property.
     * 
     * @return
     *     possible object is
     *     {@link CTStylisticSets }
     *     
     */
    public CTStylisticSets getStylisticSets() {
        return stylisticSets;
    }

    /**
     * Sets the value of the stylisticSets property.
     * 
     * @param value
     *     allowed object is
     *     {@link CTStylisticSets }
     *     
     */
    public void setStylisticSets(CTStylisticSets value) {
        this.stylisticSets = value;
    }

    /**
     * Gets the value of the cntxtAlts property.
     * 
     * @return
     *     possible object is
     *     {@link CTOnOff }
     *     
     */
    public CTOnOff getCntxtAlts() {
        return cntxtAlts;
    }

    /**
     * Sets the value of the cntxtAlts property.
     * 
     * @param value
     *     allowed object is
     *     {@link CTOnOff }
     *     
     */
    public void setCntxtAlts(CTOnOff value) {
        this.cntxtAlts = value;
    }    
    
    /**
     * Gets the value of the rPrChange property.
     * 
     * @return
     *     possible object is
     *     {@link CTRPrChange }
     *     
     */
    public CTRPrChange getRPrChange() {
        return rPrChange;
    }

    /**
     * Sets the value of the rPrChange property.
     * 
     * @param value
     *     allowed object is
     *     {@link CTRPrChange }
     *     
     */
    public void setRPrChange(CTRPrChange value) {
        this.rPrChange = value;
    }


}
