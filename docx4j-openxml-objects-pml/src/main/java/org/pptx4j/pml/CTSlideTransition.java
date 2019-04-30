/*
 *  Copyright 2010-2012, Plutext Pty Ltd.
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
package org.pptx4j.pml;

import javax.xml.bind.Unmarshaller;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlSchemaType;
import javax.xml.bind.annotation.XmlTransient;
import javax.xml.bind.annotation.XmlType;

import org.jvnet.jaxb2_commons.ppp.Child;


/**
 * <p>Java class for CT_SlideTransition complex type.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
 * 
 * <pre>
 * &lt;complexType name="CT_SlideTransition"&gt;
 *   &lt;complexContent&gt;
 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType"&gt;
 *       &lt;sequence&gt;
 *         &lt;choice minOccurs="0"&gt;
 *           &lt;element name="blinds" type="{http://schemas.openxmlformats.org/presentationml/2006/main}CT_OrientationTransition"/&gt;
 *           &lt;element name="checker" type="{http://schemas.openxmlformats.org/presentationml/2006/main}CT_OrientationTransition"/&gt;
 *           &lt;element name="circle" type="{http://schemas.openxmlformats.org/presentationml/2006/main}CT_Empty"/&gt;
 *           &lt;element name="dissolve" type="{http://schemas.openxmlformats.org/presentationml/2006/main}CT_Empty"/&gt;
 *           &lt;element name="comb" type="{http://schemas.openxmlformats.org/presentationml/2006/main}CT_OrientationTransition"/&gt;
 *           &lt;element name="cover" type="{http://schemas.openxmlformats.org/presentationml/2006/main}CT_EightDirectionTransition"/&gt;
 *           &lt;element name="cut" type="{http://schemas.openxmlformats.org/presentationml/2006/main}CT_OptionalBlackTransition"/&gt;
 *           &lt;element name="diamond" type="{http://schemas.openxmlformats.org/presentationml/2006/main}CT_Empty"/&gt;
 *           &lt;element name="fade" type="{http://schemas.openxmlformats.org/presentationml/2006/main}CT_OptionalBlackTransition"/&gt;
 *           &lt;element name="newsflash" type="{http://schemas.openxmlformats.org/presentationml/2006/main}CT_Empty"/&gt;
 *           &lt;element name="plus" type="{http://schemas.openxmlformats.org/presentationml/2006/main}CT_Empty"/&gt;
 *           &lt;element name="pull" type="{http://schemas.openxmlformats.org/presentationml/2006/main}CT_EightDirectionTransition"/&gt;
 *           &lt;element name="push" type="{http://schemas.openxmlformats.org/presentationml/2006/main}CT_SideDirectionTransition"/&gt;
 *           &lt;element name="random" type="{http://schemas.openxmlformats.org/presentationml/2006/main}CT_Empty"/&gt;
 *           &lt;element name="randomBar" type="{http://schemas.openxmlformats.org/presentationml/2006/main}CT_OrientationTransition"/&gt;
 *           &lt;element name="split" type="{http://schemas.openxmlformats.org/presentationml/2006/main}CT_SplitTransition"/&gt;
 *           &lt;element name="strips" type="{http://schemas.openxmlformats.org/presentationml/2006/main}CT_CornerDirectionTransition"/&gt;
 *           &lt;element name="wedge" type="{http://schemas.openxmlformats.org/presentationml/2006/main}CT_Empty"/&gt;
 *           &lt;element name="wheel" type="{http://schemas.openxmlformats.org/presentationml/2006/main}CT_WheelTransition"/&gt;
 *           &lt;element name="wipe" type="{http://schemas.openxmlformats.org/presentationml/2006/main}CT_SideDirectionTransition"/&gt;
 *           &lt;element name="zoom" type="{http://schemas.openxmlformats.org/presentationml/2006/main}CT_InOutTransition"/&gt;
 *         &lt;/choice&gt;
 *         &lt;element name="sndAc" type="{http://schemas.openxmlformats.org/presentationml/2006/main}CT_TransitionSoundAction" minOccurs="0"/&gt;
 *         &lt;element name="extLst" type="{http://schemas.openxmlformats.org/presentationml/2006/main}CT_ExtensionListModify" minOccurs="0"/&gt;
 *       &lt;/sequence&gt;
 *       &lt;attribute name="spd" type="{http://schemas.openxmlformats.org/presentationml/2006/main}ST_TransitionSpeed" default="fast" /&gt;
 *       &lt;attribute name="advClick" type="{http://www.w3.org/2001/XMLSchema}boolean" default="true" /&gt;
 *       &lt;attribute name="advTm" type="{http://www.w3.org/2001/XMLSchema}unsignedInt" /&gt;
 *     &lt;/restriction&gt;
 *   &lt;/complexContent&gt;
 * &lt;/complexType&gt;
 * </pre>
 * 
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "CT_SlideTransition", propOrder = {
    "blinds",
    "checker",
    "circle",
    "dissolve",
    "comb",
    "cover",
    "cut",
    "diamond",
    "fade",
    "newsflash",
    "plus",
    "pull",
    "push",
    "random",
    "randomBar",
    "split",
    "strips",
    "wedge",
    "wheel",
    "wipe",
    "zoom",
    "sndAc",
    "extLst"
})
public class CTSlideTransition implements Child
{

    protected CTOrientationTransition blinds;
    protected CTOrientationTransition checker;
    protected CTEmpty circle;
    protected CTEmpty dissolve;
    protected CTOrientationTransition comb;
    protected CTEightDirectionTransition cover;
    protected CTOptionalBlackTransition cut;
    protected CTEmpty diamond;
    protected CTOptionalBlackTransition fade;
    protected CTEmpty newsflash;
    protected CTEmpty plus;
    protected CTEightDirectionTransition pull;
    protected CTSideDirectionTransition push;
    protected CTEmpty random;
    protected CTOrientationTransition randomBar;
    protected CTSplitTransition split;
    protected CTCornerDirectionTransition strips;
    protected CTEmpty wedge;
    protected CTWheelTransition wheel;
    protected CTSideDirectionTransition wipe;
    protected CTInOutTransition zoom;
    protected CTTransitionSoundAction sndAc;
    protected CTExtensionListModify extLst;
    @XmlAttribute(name = "spd")
    protected STTransitionSpeed spd;
    @XmlAttribute(name = "advClick")
    protected Boolean advClick;
    @XmlAttribute(name = "advTm")
    @XmlSchemaType(name = "unsignedInt")
    protected Long advTm;
    @XmlTransient
    private Object parent;

    /**
     * Gets the value of the blinds property.
     * 
     * @return
     *     possible object is
     *     {@link CTOrientationTransition }
     *     
     */
    public CTOrientationTransition getBlinds() {
        return blinds;
    }

    /**
     * Sets the value of the blinds property.
     * 
     * @param value
     *     allowed object is
     *     {@link CTOrientationTransition }
     *     
     */
    public void setBlinds(CTOrientationTransition value) {
        this.blinds = value;
    }

    /**
     * Gets the value of the checker property.
     * 
     * @return
     *     possible object is
     *     {@link CTOrientationTransition }
     *     
     */
    public CTOrientationTransition getChecker() {
        return checker;
    }

    /**
     * Sets the value of the checker property.
     * 
     * @param value
     *     allowed object is
     *     {@link CTOrientationTransition }
     *     
     */
    public void setChecker(CTOrientationTransition value) {
        this.checker = value;
    }

    /**
     * Gets the value of the circle property.
     * 
     * @return
     *     possible object is
     *     {@link CTEmpty }
     *     
     */
    public CTEmpty getCircle() {
        return circle;
    }

    /**
     * Sets the value of the circle property.
     * 
     * @param value
     *     allowed object is
     *     {@link CTEmpty }
     *     
     */
    public void setCircle(CTEmpty value) {
        this.circle = value;
    }

    /**
     * Gets the value of the dissolve property.
     * 
     * @return
     *     possible object is
     *     {@link CTEmpty }
     *     
     */
    public CTEmpty getDissolve() {
        return dissolve;
    }

    /**
     * Sets the value of the dissolve property.
     * 
     * @param value
     *     allowed object is
     *     {@link CTEmpty }
     *     
     */
    public void setDissolve(CTEmpty value) {
        this.dissolve = value;
    }

    /**
     * Gets the value of the comb property.
     * 
     * @return
     *     possible object is
     *     {@link CTOrientationTransition }
     *     
     */
    public CTOrientationTransition getComb() {
        return comb;
    }

    /**
     * Sets the value of the comb property.
     * 
     * @param value
     *     allowed object is
     *     {@link CTOrientationTransition }
     *     
     */
    public void setComb(CTOrientationTransition value) {
        this.comb = value;
    }

    /**
     * Gets the value of the cover property.
     * 
     * @return
     *     possible object is
     *     {@link CTEightDirectionTransition }
     *     
     */
    public CTEightDirectionTransition getCover() {
        return cover;
    }

    /**
     * Sets the value of the cover property.
     * 
     * @param value
     *     allowed object is
     *     {@link CTEightDirectionTransition }
     *     
     */
    public void setCover(CTEightDirectionTransition value) {
        this.cover = value;
    }

    /**
     * Gets the value of the cut property.
     * 
     * @return
     *     possible object is
     *     {@link CTOptionalBlackTransition }
     *     
     */
    public CTOptionalBlackTransition getCut() {
        return cut;
    }

    /**
     * Sets the value of the cut property.
     * 
     * @param value
     *     allowed object is
     *     {@link CTOptionalBlackTransition }
     *     
     */
    public void setCut(CTOptionalBlackTransition value) {
        this.cut = value;
    }

    /**
     * Gets the value of the diamond property.
     * 
     * @return
     *     possible object is
     *     {@link CTEmpty }
     *     
     */
    public CTEmpty getDiamond() {
        return diamond;
    }

    /**
     * Sets the value of the diamond property.
     * 
     * @param value
     *     allowed object is
     *     {@link CTEmpty }
     *     
     */
    public void setDiamond(CTEmpty value) {
        this.diamond = value;
    }

    /**
     * Gets the value of the fade property.
     * 
     * @return
     *     possible object is
     *     {@link CTOptionalBlackTransition }
     *     
     */
    public CTOptionalBlackTransition getFade() {
        return fade;
    }

    /**
     * Sets the value of the fade property.
     * 
     * @param value
     *     allowed object is
     *     {@link CTOptionalBlackTransition }
     *     
     */
    public void setFade(CTOptionalBlackTransition value) {
        this.fade = value;
    }

    /**
     * Gets the value of the newsflash property.
     * 
     * @return
     *     possible object is
     *     {@link CTEmpty }
     *     
     */
    public CTEmpty getNewsflash() {
        return newsflash;
    }

    /**
     * Sets the value of the newsflash property.
     * 
     * @param value
     *     allowed object is
     *     {@link CTEmpty }
     *     
     */
    public void setNewsflash(CTEmpty value) {
        this.newsflash = value;
    }

    /**
     * Gets the value of the plus property.
     * 
     * @return
     *     possible object is
     *     {@link CTEmpty }
     *     
     */
    public CTEmpty getPlus() {
        return plus;
    }

    /**
     * Sets the value of the plus property.
     * 
     * @param value
     *     allowed object is
     *     {@link CTEmpty }
     *     
     */
    public void setPlus(CTEmpty value) {
        this.plus = value;
    }

    /**
     * Gets the value of the pull property.
     * 
     * @return
     *     possible object is
     *     {@link CTEightDirectionTransition }
     *     
     */
    public CTEightDirectionTransition getPull() {
        return pull;
    }

    /**
     * Sets the value of the pull property.
     * 
     * @param value
     *     allowed object is
     *     {@link CTEightDirectionTransition }
     *     
     */
    public void setPull(CTEightDirectionTransition value) {
        this.pull = value;
    }

    /**
     * Gets the value of the push property.
     * 
     * @return
     *     possible object is
     *     {@link CTSideDirectionTransition }
     *     
     */
    public CTSideDirectionTransition getPush() {
        return push;
    }

    /**
     * Sets the value of the push property.
     * 
     * @param value
     *     allowed object is
     *     {@link CTSideDirectionTransition }
     *     
     */
    public void setPush(CTSideDirectionTransition value) {
        this.push = value;
    }

    /**
     * Gets the value of the random property.
     * 
     * @return
     *     possible object is
     *     {@link CTEmpty }
     *     
     */
    public CTEmpty getRandom() {
        return random;
    }

    /**
     * Sets the value of the random property.
     * 
     * @param value
     *     allowed object is
     *     {@link CTEmpty }
     *     
     */
    public void setRandom(CTEmpty value) {
        this.random = value;
    }

    /**
     * Gets the value of the randomBar property.
     * 
     * @return
     *     possible object is
     *     {@link CTOrientationTransition }
     *     
     */
    public CTOrientationTransition getRandomBar() {
        return randomBar;
    }

    /**
     * Sets the value of the randomBar property.
     * 
     * @param value
     *     allowed object is
     *     {@link CTOrientationTransition }
     *     
     */
    public void setRandomBar(CTOrientationTransition value) {
        this.randomBar = value;
    }

    /**
     * Gets the value of the split property.
     * 
     * @return
     *     possible object is
     *     {@link CTSplitTransition }
     *     
     */
    public CTSplitTransition getSplit() {
        return split;
    }

    /**
     * Sets the value of the split property.
     * 
     * @param value
     *     allowed object is
     *     {@link CTSplitTransition }
     *     
     */
    public void setSplit(CTSplitTransition value) {
        this.split = value;
    }

    /**
     * Gets the value of the strips property.
     * 
     * @return
     *     possible object is
     *     {@link CTCornerDirectionTransition }
     *     
     */
    public CTCornerDirectionTransition getStrips() {
        return strips;
    }

    /**
     * Sets the value of the strips property.
     * 
     * @param value
     *     allowed object is
     *     {@link CTCornerDirectionTransition }
     *     
     */
    public void setStrips(CTCornerDirectionTransition value) {
        this.strips = value;
    }

    /**
     * Gets the value of the wedge property.
     * 
     * @return
     *     possible object is
     *     {@link CTEmpty }
     *     
     */
    public CTEmpty getWedge() {
        return wedge;
    }

    /**
     * Sets the value of the wedge property.
     * 
     * @param value
     *     allowed object is
     *     {@link CTEmpty }
     *     
     */
    public void setWedge(CTEmpty value) {
        this.wedge = value;
    }

    /**
     * Gets the value of the wheel property.
     * 
     * @return
     *     possible object is
     *     {@link CTWheelTransition }
     *     
     */
    public CTWheelTransition getWheel() {
        return wheel;
    }

    /**
     * Sets the value of the wheel property.
     * 
     * @param value
     *     allowed object is
     *     {@link CTWheelTransition }
     *     
     */
    public void setWheel(CTWheelTransition value) {
        this.wheel = value;
    }

    /**
     * Gets the value of the wipe property.
     * 
     * @return
     *     possible object is
     *     {@link CTSideDirectionTransition }
     *     
     */
    public CTSideDirectionTransition getWipe() {
        return wipe;
    }

    /**
     * Sets the value of the wipe property.
     * 
     * @param value
     *     allowed object is
     *     {@link CTSideDirectionTransition }
     *     
     */
    public void setWipe(CTSideDirectionTransition value) {
        this.wipe = value;
    }

    /**
     * Gets the value of the zoom property.
     * 
     * @return
     *     possible object is
     *     {@link CTInOutTransition }
     *     
     */
    public CTInOutTransition getZoom() {
        return zoom;
    }

    /**
     * Sets the value of the zoom property.
     * 
     * @param value
     *     allowed object is
     *     {@link CTInOutTransition }
     *     
     */
    public void setZoom(CTInOutTransition value) {
        this.zoom = value;
    }

    /**
     * Gets the value of the sndAc property.
     * 
     * @return
     *     possible object is
     *     {@link CTTransitionSoundAction }
     *     
     */
    public CTTransitionSoundAction getSndAc() {
        return sndAc;
    }

    /**
     * Sets the value of the sndAc property.
     * 
     * @param value
     *     allowed object is
     *     {@link CTTransitionSoundAction }
     *     
     */
    public void setSndAc(CTTransitionSoundAction value) {
        this.sndAc = value;
    }

    /**
     * Gets the value of the extLst property.
     * 
     * @return
     *     possible object is
     *     {@link CTExtensionListModify }
     *     
     */
    public CTExtensionListModify getExtLst() {
        return extLst;
    }

    /**
     * Sets the value of the extLst property.
     * 
     * @param value
     *     allowed object is
     *     {@link CTExtensionListModify }
     *     
     */
    public void setExtLst(CTExtensionListModify value) {
        this.extLst = value;
    }

    /**
     * Gets the value of the spd property.
     * 
     * @return
     *     possible object is
     *     {@link STTransitionSpeed }
     *     
     */
    public STTransitionSpeed getSpd() {
        if (spd == null) {
            return STTransitionSpeed.FAST;
        } else {
            return spd;
        }
    }

    /**
     * Sets the value of the spd property.
     * 
     * @param value
     *     allowed object is
     *     {@link STTransitionSpeed }
     *     
     */
    public void setSpd(STTransitionSpeed value) {
        this.spd = value;
    }

    /**
     * Gets the value of the advClick property.
     * 
     * @return
     *     possible object is
     *     {@link Boolean }
     *     
     */
    public boolean isAdvClick() {
        if (advClick == null) {
            return true;
        } else {
            return advClick;
        }
    }

    /**
     * Sets the value of the advClick property.
     * 
     * @param value
     *     allowed object is
     *     {@link Boolean }
     *     
     */
    public void setAdvClick(Boolean value) {
        this.advClick = value;
    }

    /**
     * Gets the value of the advTm property.
     * 
     * @return
     *     possible object is
     *     {@link Long }
     *     
     */
    public Long getAdvTm() {
        return advTm;
    }

    /**
     * Sets the value of the advTm property.
     * 
     * @param value
     *     allowed object is
     *     {@link Long }
     *     
     */
    public void setAdvTm(Long value) {
        this.advTm = value;
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
