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
import javax.xml.bind.annotation.XmlTransient;
import javax.xml.bind.annotation.XmlType;
import org.jvnet.jaxb2_commons.ppp.Child;


/**
 * <p>Java class for CT_TextParagraphProperties complex type.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
 * 
 * <pre>
 * &lt;complexType name="CT_TextParagraphProperties"&gt;
 *   &lt;complexContent&gt;
 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType"&gt;
 *       &lt;sequence&gt;
 *         &lt;element name="lnSpc" type="{http://schemas.openxmlformats.org/drawingml/2006/main}CT_TextSpacing" minOccurs="0"/&gt;
 *         &lt;element name="spcBef" type="{http://schemas.openxmlformats.org/drawingml/2006/main}CT_TextSpacing" minOccurs="0"/&gt;
 *         &lt;element name="spcAft" type="{http://schemas.openxmlformats.org/drawingml/2006/main}CT_TextSpacing" minOccurs="0"/&gt;
 *         &lt;group ref="{http://schemas.openxmlformats.org/drawingml/2006/main}EG_TextBulletColor" minOccurs="0"/&gt;
 *         &lt;group ref="{http://schemas.openxmlformats.org/drawingml/2006/main}EG_TextBulletSize" minOccurs="0"/&gt;
 *         &lt;group ref="{http://schemas.openxmlformats.org/drawingml/2006/main}EG_TextBulletTypeface" minOccurs="0"/&gt;
 *         &lt;group ref="{http://schemas.openxmlformats.org/drawingml/2006/main}EG_TextBullet" minOccurs="0"/&gt;
 *         &lt;element name="tabLst" type="{http://schemas.openxmlformats.org/drawingml/2006/main}CT_TextTabStopList" minOccurs="0"/&gt;
 *         &lt;element name="defRPr" type="{http://schemas.openxmlformats.org/drawingml/2006/main}CT_TextCharacterProperties" minOccurs="0"/&gt;
 *         &lt;element name="extLst" type="{http://schemas.openxmlformats.org/drawingml/2006/main}CT_OfficeArtExtensionList" minOccurs="0"/&gt;
 *       &lt;/sequence&gt;
 *       &lt;attribute name="marL" type="{http://schemas.openxmlformats.org/drawingml/2006/main}ST_TextMargin" /&gt;
 *       &lt;attribute name="marR" type="{http://schemas.openxmlformats.org/drawingml/2006/main}ST_TextMargin" /&gt;
 *       &lt;attribute name="lvl" type="{http://schemas.openxmlformats.org/drawingml/2006/main}ST_TextIndentLevelType" /&gt;
 *       &lt;attribute name="indent" type="{http://schemas.openxmlformats.org/drawingml/2006/main}ST_TextIndent" /&gt;
 *       &lt;attribute name="algn" type="{http://schemas.openxmlformats.org/drawingml/2006/main}ST_TextAlignType" /&gt;
 *       &lt;attribute name="defTabSz" type="{http://schemas.openxmlformats.org/drawingml/2006/main}ST_Coordinate32" /&gt;
 *       &lt;attribute name="rtl" type="{http://www.w3.org/2001/XMLSchema}boolean" /&gt;
 *       &lt;attribute name="eaLnBrk" type="{http://www.w3.org/2001/XMLSchema}boolean" /&gt;
 *       &lt;attribute name="fontAlgn" type="{http://schemas.openxmlformats.org/drawingml/2006/main}ST_TextFontAlignType" /&gt;
 *       &lt;attribute name="latinLnBrk" type="{http://www.w3.org/2001/XMLSchema}boolean" /&gt;
 *       &lt;attribute name="hangingPunct" type="{http://www.w3.org/2001/XMLSchema}boolean" /&gt;
 *     &lt;/restriction&gt;
 *   &lt;/complexContent&gt;
 * &lt;/complexType&gt;
 * </pre>
 * 
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "CT_TextParagraphProperties", propOrder = {
    "lnSpc",
    "spcBef",
    "spcAft",
    "buClrTx",
    "buClr",
    "buSzTx",
    "buSzPct",
    "buSzPts",
    "buFontTx",
    "buFont",
    "buNone",
    "buAutoNum",
    "buChar",
    "buBlip",
    "tabLst",
    "defRPr",
    "extLst"
})
public class CTTextParagraphProperties implements Child
{

    protected CTTextSpacing lnSpc;
    protected CTTextSpacing spcBef;
    protected CTTextSpacing spcAft;
    protected CTTextBulletColorFollowText buClrTx;
    protected CTColor buClr;
    protected CTTextBulletSizeFollowText buSzTx;
    protected CTTextBulletSizePercent buSzPct;
    protected CTTextBulletSizePoint buSzPts;
    protected CTTextBulletTypefaceFollowText buFontTx;
    protected TextFont buFont;
    protected CTTextNoBullet buNone;
    protected CTTextAutonumberBullet buAutoNum;
    protected CTTextCharBullet buChar;
    protected CTTextBlipBullet buBlip;
    protected CTTextTabStopList tabLst;
    protected CTTextCharacterProperties defRPr;
    protected CTOfficeArtExtensionList extLst;
    @XmlAttribute(name = "marL")
    protected Integer marL;
    @XmlAttribute(name = "marR")
    protected Integer marR;
    @XmlAttribute(name = "lvl")
    protected Integer lvl;
    @XmlAttribute(name = "indent")
    protected Integer indent;
    @XmlAttribute(name = "algn")
    protected STTextAlignType algn;
    @XmlAttribute(name = "defTabSz")
    protected Integer defTabSz;
    @XmlAttribute(name = "rtl")
    protected Boolean rtl;
    @XmlAttribute(name = "eaLnBrk")
    protected Boolean eaLnBrk;
    @XmlAttribute(name = "fontAlgn")
    protected STTextFontAlignType fontAlgn;
    @XmlAttribute(name = "latinLnBrk")
    protected Boolean latinLnBrk;
    @XmlAttribute(name = "hangingPunct")
    protected Boolean hangingPunct;
    @XmlTransient
    private Object parent;

    /**
     * Gets the value of the lnSpc property.
     * 
     * @return
     *     possible object is
     *     {@link CTTextSpacing }
     *     
     */
    public CTTextSpacing getLnSpc() {
        return lnSpc;
    }

    /**
     * Sets the value of the lnSpc property.
     * 
     * @param value
     *     allowed object is
     *     {@link CTTextSpacing }
     *     
     */
    public void setLnSpc(CTTextSpacing value) {
        this.lnSpc = value;
    }

    /**
     * Gets the value of the spcBef property.
     * 
     * @return
     *     possible object is
     *     {@link CTTextSpacing }
     *     
     */
    public CTTextSpacing getSpcBef() {
        return spcBef;
    }

    /**
     * Sets the value of the spcBef property.
     * 
     * @param value
     *     allowed object is
     *     {@link CTTextSpacing }
     *     
     */
    public void setSpcBef(CTTextSpacing value) {
        this.spcBef = value;
    }

    /**
     * Gets the value of the spcAft property.
     * 
     * @return
     *     possible object is
     *     {@link CTTextSpacing }
     *     
     */
    public CTTextSpacing getSpcAft() {
        return spcAft;
    }

    /**
     * Sets the value of the spcAft property.
     * 
     * @param value
     *     allowed object is
     *     {@link CTTextSpacing }
     *     
     */
    public void setSpcAft(CTTextSpacing value) {
        this.spcAft = value;
    }

    /**
     * Gets the value of the buClrTx property.
     * 
     * @return
     *     possible object is
     *     {@link CTTextBulletColorFollowText }
     *     
     */
    public CTTextBulletColorFollowText getBuClrTx() {
        return buClrTx;
    }

    /**
     * Sets the value of the buClrTx property.
     * 
     * @param value
     *     allowed object is
     *     {@link CTTextBulletColorFollowText }
     *     
     */
    public void setBuClrTx(CTTextBulletColorFollowText value) {
        this.buClrTx = value;
    }

    /**
     * Gets the value of the buClr property.
     * 
     * @return
     *     possible object is
     *     {@link CTColor }
     *     
     */
    public CTColor getBuClr() {
        return buClr;
    }

    /**
     * Sets the value of the buClr property.
     * 
     * @param value
     *     allowed object is
     *     {@link CTColor }
     *     
     */
    public void setBuClr(CTColor value) {
        this.buClr = value;
    }

    /**
     * Gets the value of the buSzTx property.
     * 
     * @return
     *     possible object is
     *     {@link CTTextBulletSizeFollowText }
     *     
     */
    public CTTextBulletSizeFollowText getBuSzTx() {
        return buSzTx;
    }

    /**
     * Sets the value of the buSzTx property.
     * 
     * @param value
     *     allowed object is
     *     {@link CTTextBulletSizeFollowText }
     *     
     */
    public void setBuSzTx(CTTextBulletSizeFollowText value) {
        this.buSzTx = value;
    }

    /**
     * Gets the value of the buSzPct property.
     * 
     * @return
     *     possible object is
     *     {@link CTTextBulletSizePercent }
     *     
     */
    public CTTextBulletSizePercent getBuSzPct() {
        return buSzPct;
    }

    /**
     * Sets the value of the buSzPct property.
     * 
     * @param value
     *     allowed object is
     *     {@link CTTextBulletSizePercent }
     *     
     */
    public void setBuSzPct(CTTextBulletSizePercent value) {
        this.buSzPct = value;
    }

    /**
     * Gets the value of the buSzPts property.
     * 
     * @return
     *     possible object is
     *     {@link CTTextBulletSizePoint }
     *     
     */
    public CTTextBulletSizePoint getBuSzPts() {
        return buSzPts;
    }

    /**
     * Sets the value of the buSzPts property.
     * 
     * @param value
     *     allowed object is
     *     {@link CTTextBulletSizePoint }
     *     
     */
    public void setBuSzPts(CTTextBulletSizePoint value) {
        this.buSzPts = value;
    }

    /**
     * Gets the value of the buFontTx property.
     * 
     * @return
     *     possible object is
     *     {@link CTTextBulletTypefaceFollowText }
     *     
     */
    public CTTextBulletTypefaceFollowText getBuFontTx() {
        return buFontTx;
    }

    /**
     * Sets the value of the buFontTx property.
     * 
     * @param value
     *     allowed object is
     *     {@link CTTextBulletTypefaceFollowText }
     *     
     */
    public void setBuFontTx(CTTextBulletTypefaceFollowText value) {
        this.buFontTx = value;
    }

    /**
     * Gets the value of the buFont property.
     * 
     * @return
     *     possible object is
     *     {@link TextFont }
     *     
     */
    public TextFont getBuFont() {
        return buFont;
    }

    /**
     * Sets the value of the buFont property.
     * 
     * @param value
     *     allowed object is
     *     {@link TextFont }
     *     
     */
    public void setBuFont(TextFont value) {
        this.buFont = value;
    }

    /**
     * Gets the value of the buNone property.
     * 
     * @return
     *     possible object is
     *     {@link CTTextNoBullet }
     *     
     */
    public CTTextNoBullet getBuNone() {
        return buNone;
    }

    /**
     * Sets the value of the buNone property.
     * 
     * @param value
     *     allowed object is
     *     {@link CTTextNoBullet }
     *     
     */
    public void setBuNone(CTTextNoBullet value) {
        this.buNone = value;
    }

    /**
     * Gets the value of the buAutoNum property.
     * 
     * @return
     *     possible object is
     *     {@link CTTextAutonumberBullet }
     *     
     */
    public CTTextAutonumberBullet getBuAutoNum() {
        return buAutoNum;
    }

    /**
     * Sets the value of the buAutoNum property.
     * 
     * @param value
     *     allowed object is
     *     {@link CTTextAutonumberBullet }
     *     
     */
    public void setBuAutoNum(CTTextAutonumberBullet value) {
        this.buAutoNum = value;
    }

    /**
     * Gets the value of the buChar property.
     * 
     * @return
     *     possible object is
     *     {@link CTTextCharBullet }
     *     
     */
    public CTTextCharBullet getBuChar() {
        return buChar;
    }

    /**
     * Sets the value of the buChar property.
     * 
     * @param value
     *     allowed object is
     *     {@link CTTextCharBullet }
     *     
     */
    public void setBuChar(CTTextCharBullet value) {
        this.buChar = value;
    }

    /**
     * Gets the value of the buBlip property.
     * 
     * @return
     *     possible object is
     *     {@link CTTextBlipBullet }
     *     
     */
    public CTTextBlipBullet getBuBlip() {
        return buBlip;
    }

    /**
     * Sets the value of the buBlip property.
     * 
     * @param value
     *     allowed object is
     *     {@link CTTextBlipBullet }
     *     
     */
    public void setBuBlip(CTTextBlipBullet value) {
        this.buBlip = value;
    }

    /**
     * Gets the value of the tabLst property.
     * 
     * @return
     *     possible object is
     *     {@link CTTextTabStopList }
     *     
     */
    public CTTextTabStopList getTabLst() {
        return tabLst;
    }

    /**
     * Sets the value of the tabLst property.
     * 
     * @param value
     *     allowed object is
     *     {@link CTTextTabStopList }
     *     
     */
    public void setTabLst(CTTextTabStopList value) {
        this.tabLst = value;
    }

    /**
     * Gets the value of the defRPr property.
     * 
     * @return
     *     possible object is
     *     {@link CTTextCharacterProperties }
     *     
     */
    public CTTextCharacterProperties getDefRPr() {
        return defRPr;
    }

    /**
     * Sets the value of the defRPr property.
     * 
     * @param value
     *     allowed object is
     *     {@link CTTextCharacterProperties }
     *     
     */
    public void setDefRPr(CTTextCharacterProperties value) {
        this.defRPr = value;
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
     * Gets the value of the marL property.
     * 
     * @return
     *     possible object is
     *     {@link Integer }
     *     
     */
    public Integer getMarL() {
        return marL;
    }

    /**
     * Sets the value of the marL property.
     * 
     * @param value
     *     allowed object is
     *     {@link Integer }
     *     
     */
    public void setMarL(Integer value) {
        this.marL = value;
    }

    /**
     * Gets the value of the marR property.
     * 
     * @return
     *     possible object is
     *     {@link Integer }
     *     
     */
    public Integer getMarR() {
        return marR;
    }

    /**
     * Sets the value of the marR property.
     * 
     * @param value
     *     allowed object is
     *     {@link Integer }
     *     
     */
    public void setMarR(Integer value) {
        this.marR = value;
    }

    /**
     * Gets the value of the lvl property.
     * 
     * @return
     *     possible object is
     *     {@link Integer }
     *     
     */
    public Integer getLvl() {
        return lvl;
    }

    /**
     * Sets the value of the lvl property.
     * 
     * @param value
     *     allowed object is
     *     {@link Integer }
     *     
     */
    public void setLvl(Integer value) {
        this.lvl = value;
    }

    /**
     * Gets the value of the indent property.
     * 
     * @return
     *     possible object is
     *     {@link Integer }
     *     
     */
    public Integer getIndent() {
        return indent;
    }

    /**
     * Sets the value of the indent property.
     * 
     * @param value
     *     allowed object is
     *     {@link Integer }
     *     
     */
    public void setIndent(Integer value) {
        this.indent = value;
    }

    /**
     * Gets the value of the algn property.
     * 
     * @return
     *     possible object is
     *     {@link STTextAlignType }
     *     
     */
    public STTextAlignType getAlgn() {
        return algn;
    }

    /**
     * Sets the value of the algn property.
     * 
     * @param value
     *     allowed object is
     *     {@link STTextAlignType }
     *     
     */
    public void setAlgn(STTextAlignType value) {
        this.algn = value;
    }

    /**
     * Gets the value of the defTabSz property.
     * 
     * @return
     *     possible object is
     *     {@link Integer }
     *     
     */
    public Integer getDefTabSz() {
        return defTabSz;
    }

    /**
     * Sets the value of the defTabSz property.
     * 
     * @param value
     *     allowed object is
     *     {@link Integer }
     *     
     */
    public void setDefTabSz(Integer value) {
        this.defTabSz = value;
    }

    /**
     * Gets the value of the rtl property.
     * 
     * @return
     *     possible object is
     *     {@link Boolean }
     *     
     */
    public Boolean isRtl() {
        return rtl;
    }

    /**
     * Sets the value of the rtl property.
     * 
     * @param value
     *     allowed object is
     *     {@link Boolean }
     *     
     */
    public void setRtl(Boolean value) {
        this.rtl = value;
    }

    /**
     * Gets the value of the eaLnBrk property.
     * 
     * @return
     *     possible object is
     *     {@link Boolean }
     *     
     */
    public Boolean isEaLnBrk() {
        return eaLnBrk;
    }

    /**
     * Sets the value of the eaLnBrk property.
     * 
     * @param value
     *     allowed object is
     *     {@link Boolean }
     *     
     */
    public void setEaLnBrk(Boolean value) {
        this.eaLnBrk = value;
    }

    /**
     * Gets the value of the fontAlgn property.
     * 
     * @return
     *     possible object is
     *     {@link STTextFontAlignType }
     *     
     */
    public STTextFontAlignType getFontAlgn() {
        return fontAlgn;
    }

    /**
     * Sets the value of the fontAlgn property.
     * 
     * @param value
     *     allowed object is
     *     {@link STTextFontAlignType }
     *     
     */
    public void setFontAlgn(STTextFontAlignType value) {
        this.fontAlgn = value;
    }

    /**
     * Gets the value of the latinLnBrk property.
     * 
     * @return
     *     possible object is
     *     {@link Boolean }
     *     
     */
    public Boolean isLatinLnBrk() {
        return latinLnBrk;
    }

    /**
     * Sets the value of the latinLnBrk property.
     * 
     * @param value
     *     allowed object is
     *     {@link Boolean }
     *     
     */
    public void setLatinLnBrk(Boolean value) {
        this.latinLnBrk = value;
    }

    /**
     * Gets the value of the hangingPunct property.
     * 
     * @return
     *     possible object is
     *     {@link Boolean }
     *     
     */
    public Boolean isHangingPunct() {
        return hangingPunct;
    }

    /**
     * Sets the value of the hangingPunct property.
     * 
     * @param value
     *     allowed object is
     *     {@link Boolean }
     *     
     */
    public void setHangingPunct(Boolean value) {
        this.hangingPunct = value;
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
