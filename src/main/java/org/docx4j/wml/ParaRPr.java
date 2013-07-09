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

import org.jvnet.jaxb2_commons.ppp.Child;

import javax.xml.bind.Unmarshaller;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlTransient;
import javax.xml.bind.annotation.XmlType;


/**
 * <p>Java class for CT_ParaRPr complex type.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
 * 
 * <pre>
 * &lt;complexType name="CT_ParaRPr">
 *   &lt;complexContent>
 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
 *       &lt;sequence>
 *         &lt;group ref="{http://schemas.openxmlformats.org/wordprocessingml/2006/main}EG_ParaRPrTrackChanges" minOccurs="0"/>
 *         &lt;group ref="{http://schemas.openxmlformats.org/wordprocessingml/2006/main}EG_RPrBase" minOccurs="0"/>
 *         &lt;element name="rPrChange" type="{http://schemas.openxmlformats.org/wordprocessingml/2006/main}CT_ParaRPrChange" minOccurs="0"/>
 *       &lt;/sequence>
 *     &lt;/restriction>
 *   &lt;/complexContent>
 * &lt;/complexType>
 * </pre>
 * 
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "CT_ParaRPr", propOrder = {
    "ins",
    "del",
    "moveFrom",
    "moveTo",
    "rStyle",
    "rFonts",
    "b",
    "bCs",
    "i",
    "iCs",
    "caps",
    "smallCaps",
    "strike",
    "dstrike",
    "outline",
    "shadow",
    "emboss",
    "imprint",
    "noProof",
    "snapToGrid",
    "vanish",
    "webHidden",
    "color",
    "spacing",
    "w",
    "kern",
    "position",
    "sz",
    "szCs",
    "highlight",
    "u",
    "effect",
    "bdr",
    "shd",
    "fitText",
    "vertAlign",
    "rtl",
    "cs",
    "em",
    "lang",
    "eastAsianLayout",
    "specVanish",
    "oMath",
    "rPrChange"
})
public class ParaRPr implements Child
{

    protected CTTrackChange ins;
    protected CTTrackChange del;
    protected CTTrackChange moveFrom;
    protected CTTrackChange moveTo;
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
    protected ParaRPrChange rPrChange;
    @XmlTransient
    private Object parent;

    /**
     * Gets the value of the ins property.
     * 
     * @return
     *     possible object is
     *     {@link CTTrackChange }
     *     
     */
    public CTTrackChange getIns() {
        return ins;
    }

    /**
     * Sets the value of the ins property.
     * 
     * @param value
     *     allowed object is
     *     {@link CTTrackChange }
     *     
     */
    public void setIns(CTTrackChange value) {
        this.ins = value;
    }

    /**
     * Gets the value of the del property.
     * 
     * @return
     *     possible object is
     *     {@link CTTrackChange }
     *     
     */
    public CTTrackChange getDel() {
        return del;
    }

    /**
     * Sets the value of the del property.
     * 
     * @param value
     *     allowed object is
     *     {@link CTTrackChange }
     *     
     */
    public void setDel(CTTrackChange value) {
        this.del = value;
    }

    /**
     * Gets the value of the moveFrom property.
     * 
     * @return
     *     possible object is
     *     {@link CTTrackChange }
     *     
     */
    public CTTrackChange getMoveFrom() {
        return moveFrom;
    }

    /**
     * Sets the value of the moveFrom property.
     * 
     * @param value
     *     allowed object is
     *     {@link CTTrackChange }
     *     
     */
    public void setMoveFrom(CTTrackChange value) {
        this.moveFrom = value;
    }

    /**
     * Gets the value of the moveTo property.
     * 
     * @return
     *     possible object is
     *     {@link CTTrackChange }
     *     
     */
    public CTTrackChange getMoveTo() {
        return moveTo;
    }

    /**
     * Sets the value of the moveTo property.
     * 
     * @param value
     *     allowed object is
     *     {@link CTTrackChange }
     *     
     */
    public void setMoveTo(CTTrackChange value) {
        this.moveTo = value;
    }

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
     * Gets the value of the rPrChange property.
     * 
     * @return
     *     possible object is
     *     {@link ParaRPrChange }
     *     
     */
    public ParaRPrChange getRPrChange() {
        return rPrChange;
    }

    /**
     * Sets the value of the rPrChange property.
     * 
     * @param value
     *     allowed object is
     *     {@link ParaRPrChange }
     *     
     */
    public void setRPrChange(ParaRPrChange value) {
        this.rPrChange = value;
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
