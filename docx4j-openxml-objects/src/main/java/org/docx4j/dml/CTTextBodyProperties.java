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
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlTransient;
import javax.xml.bind.annotation.XmlType;
import org.jvnet.jaxb2_commons.ppp.Child;


/**
 * <p>Java class for CT_TextBodyProperties complex type.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
 * 
 * <pre>
 * &lt;complexType name="CT_TextBodyProperties"&gt;
 *   &lt;complexContent&gt;
 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType"&gt;
 *       &lt;sequence&gt;
 *         &lt;element name="prstTxWarp" type="{http://schemas.openxmlformats.org/drawingml/2006/main}CT_PresetTextShape" minOccurs="0"/&gt;
 *         &lt;group ref="{http://schemas.openxmlformats.org/drawingml/2006/main}EG_TextAutofit" minOccurs="0"/&gt;
 *         &lt;element name="scene3d" type="{http://schemas.openxmlformats.org/drawingml/2006/main}CT_Scene3D" minOccurs="0"/&gt;
 *         &lt;group ref="{http://schemas.openxmlformats.org/drawingml/2006/main}EG_Text3D" minOccurs="0"/&gt;
 *         &lt;element name="extLst" type="{http://schemas.openxmlformats.org/drawingml/2006/main}CT_OfficeArtExtensionList" minOccurs="0"/&gt;
 *       &lt;/sequence&gt;
 *       &lt;attribute name="rot" type="{http://schemas.openxmlformats.org/drawingml/2006/main}ST_Angle" /&gt;
 *       &lt;attribute name="spcFirstLastPara" type="{http://www.w3.org/2001/XMLSchema}boolean" /&gt;
 *       &lt;attribute name="vertOverflow" type="{http://schemas.openxmlformats.org/drawingml/2006/main}ST_TextVertOverflowType" /&gt;
 *       &lt;attribute name="horzOverflow" type="{http://schemas.openxmlformats.org/drawingml/2006/main}ST_TextHorzOverflowType" /&gt;
 *       &lt;attribute name="vert" type="{http://schemas.openxmlformats.org/drawingml/2006/main}ST_TextVerticalType" /&gt;
 *       &lt;attribute name="wrap" type="{http://schemas.openxmlformats.org/drawingml/2006/main}ST_TextWrappingType" /&gt;
 *       &lt;attribute name="lIns" type="{http://schemas.openxmlformats.org/drawingml/2006/main}ST_Coordinate32" /&gt;
 *       &lt;attribute name="tIns" type="{http://schemas.openxmlformats.org/drawingml/2006/main}ST_Coordinate32" /&gt;
 *       &lt;attribute name="rIns" type="{http://schemas.openxmlformats.org/drawingml/2006/main}ST_Coordinate32" /&gt;
 *       &lt;attribute name="bIns" type="{http://schemas.openxmlformats.org/drawingml/2006/main}ST_Coordinate32" /&gt;
 *       &lt;attribute name="numCol" type="{http://schemas.openxmlformats.org/drawingml/2006/main}ST_TextColumnCount" /&gt;
 *       &lt;attribute name="spcCol" type="{http://schemas.openxmlformats.org/drawingml/2006/main}ST_PositiveCoordinate32" /&gt;
 *       &lt;attribute name="rtlCol" type="{http://www.w3.org/2001/XMLSchema}boolean" /&gt;
 *       &lt;attribute name="fromWordArt" type="{http://www.w3.org/2001/XMLSchema}boolean" /&gt;
 *       &lt;attribute name="anchor" type="{http://schemas.openxmlformats.org/drawingml/2006/main}ST_TextAnchoringType" /&gt;
 *       &lt;attribute name="anchorCtr" type="{http://www.w3.org/2001/XMLSchema}boolean" /&gt;
 *       &lt;attribute name="forceAA" type="{http://www.w3.org/2001/XMLSchema}boolean" /&gt;
 *       &lt;attribute name="upright" type="{http://www.w3.org/2001/XMLSchema}boolean" default="false" /&gt;
 *       &lt;attribute name="compatLnSpc" type="{http://www.w3.org/2001/XMLSchema}boolean" /&gt;
 *     &lt;/restriction&gt;
 *   &lt;/complexContent&gt;
 * &lt;/complexType&gt;
 * </pre>
 * 
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "CT_TextBodyProperties", propOrder = {
    "prstTxWarp",
    "noAutofit",
    "normAutofit",
    "spAutoFit",
    "scene3D",
    "sp3D",
    "flatTx",
    "extLst"
})
public class CTTextBodyProperties implements Child
{

    protected CTPresetTextShape prstTxWarp;
    protected CTTextNoAutofit noAutofit;
    protected CTTextNormalAutofit normAutofit;
    protected CTTextShapeAutofit spAutoFit;
    @XmlElement(name = "scene3d")
    protected CTScene3D scene3D;
    @XmlElement(name = "sp3d")
    protected CTShape3D sp3D;
    protected CTFlatText flatTx;
    protected CTOfficeArtExtensionList extLst;
    @XmlAttribute(name = "rot")
    protected Integer rot;
    @XmlAttribute(name = "spcFirstLastPara")
    protected Boolean spcFirstLastPara;
    @XmlAttribute(name = "vertOverflow")
    protected STTextVertOverflowType vertOverflow;
    @XmlAttribute(name = "horzOverflow")
    protected STTextHorzOverflowType horzOverflow;
    @XmlAttribute(name = "vert")
    protected STTextVerticalType vert;
    @XmlAttribute(name = "wrap")
    protected STTextWrappingType wrap;
    @XmlAttribute(name = "lIns")
    protected Integer lIns;
    @XmlAttribute(name = "tIns")
    protected Integer tIns;
    @XmlAttribute(name = "rIns")
    protected Integer rIns;
    @XmlAttribute(name = "bIns")
    protected Integer bIns;
    @XmlAttribute(name = "numCol")
    protected Integer numCol;
    @XmlAttribute(name = "spcCol")
    protected Integer spcCol;
    @XmlAttribute(name = "rtlCol")
    protected Boolean rtlCol;
    @XmlAttribute(name = "fromWordArt")
    protected Boolean fromWordArt;
    @XmlAttribute(name = "anchor")
    protected STTextAnchoringType anchor;
    @XmlAttribute(name = "anchorCtr")
    protected Boolean anchorCtr;
    @XmlAttribute(name = "forceAA")
    protected Boolean forceAA;
    @XmlAttribute(name = "upright")
    protected Boolean upright;
    @XmlAttribute(name = "compatLnSpc")
    protected Boolean compatLnSpc;
    @XmlTransient
    private Object parent;

    /**
     * Gets the value of the prstTxWarp property.
     * 
     * @return
     *     possible object is
     *     {@link CTPresetTextShape }
     *     
     */
    public CTPresetTextShape getPrstTxWarp() {
        return prstTxWarp;
    }

    /**
     * Sets the value of the prstTxWarp property.
     * 
     * @param value
     *     allowed object is
     *     {@link CTPresetTextShape }
     *     
     */
    public void setPrstTxWarp(CTPresetTextShape value) {
        this.prstTxWarp = value;
    }

    /**
     * Gets the value of the noAutofit property.
     * 
     * @return
     *     possible object is
     *     {@link CTTextNoAutofit }
     *     
     */
    public CTTextNoAutofit getNoAutofit() {
        return noAutofit;
    }

    /**
     * Sets the value of the noAutofit property.
     * 
     * @param value
     *     allowed object is
     *     {@link CTTextNoAutofit }
     *     
     */
    public void setNoAutofit(CTTextNoAutofit value) {
        this.noAutofit = value;
    }

    /**
     * Gets the value of the normAutofit property.
     * 
     * @return
     *     possible object is
     *     {@link CTTextNormalAutofit }
     *     
     */
    public CTTextNormalAutofit getNormAutofit() {
        return normAutofit;
    }

    /**
     * Sets the value of the normAutofit property.
     * 
     * @param value
     *     allowed object is
     *     {@link CTTextNormalAutofit }
     *     
     */
    public void setNormAutofit(CTTextNormalAutofit value) {
        this.normAutofit = value;
    }

    /**
     * Gets the value of the spAutoFit property.
     * 
     * @return
     *     possible object is
     *     {@link CTTextShapeAutofit }
     *     
     */
    public CTTextShapeAutofit getSpAutoFit() {
        return spAutoFit;
    }

    /**
     * Sets the value of the spAutoFit property.
     * 
     * @param value
     *     allowed object is
     *     {@link CTTextShapeAutofit }
     *     
     */
    public void setSpAutoFit(CTTextShapeAutofit value) {
        this.spAutoFit = value;
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
     * Gets the value of the sp3D property.
     * 
     * @return
     *     possible object is
     *     {@link CTShape3D }
     *     
     */
    public CTShape3D getSp3D() {
        return sp3D;
    }

    /**
     * Sets the value of the sp3D property.
     * 
     * @param value
     *     allowed object is
     *     {@link CTShape3D }
     *     
     */
    public void setSp3D(CTShape3D value) {
        this.sp3D = value;
    }

    /**
     * Gets the value of the flatTx property.
     * 
     * @return
     *     possible object is
     *     {@link CTFlatText }
     *     
     */
    public CTFlatText getFlatTx() {
        return flatTx;
    }

    /**
     * Sets the value of the flatTx property.
     * 
     * @param value
     *     allowed object is
     *     {@link CTFlatText }
     *     
     */
    public void setFlatTx(CTFlatText value) {
        this.flatTx = value;
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
     * Gets the value of the rot property.
     * 
     * @return
     *     possible object is
     *     {@link Integer }
     *     
     */
    public Integer getRot() {
        return rot;
    }

    /**
     * Sets the value of the rot property.
     * 
     * @param value
     *     allowed object is
     *     {@link Integer }
     *     
     */
    public void setRot(Integer value) {
        this.rot = value;
    }

    /**
     * Gets the value of the spcFirstLastPara property.
     * 
     * @return
     *     possible object is
     *     {@link Boolean }
     *     
     */
    public Boolean isSpcFirstLastPara() {
        return spcFirstLastPara;
    }

    /**
     * Sets the value of the spcFirstLastPara property.
     * 
     * @param value
     *     allowed object is
     *     {@link Boolean }
     *     
     */
    public void setSpcFirstLastPara(Boolean value) {
        this.spcFirstLastPara = value;
    }

    /**
     * Gets the value of the vertOverflow property.
     * 
     * @return
     *     possible object is
     *     {@link STTextVertOverflowType }
     *     
     */
    public STTextVertOverflowType getVertOverflow() {
        return vertOverflow;
    }

    /**
     * Sets the value of the vertOverflow property.
     * 
     * @param value
     *     allowed object is
     *     {@link STTextVertOverflowType }
     *     
     */
    public void setVertOverflow(STTextVertOverflowType value) {
        this.vertOverflow = value;
    }

    /**
     * Gets the value of the horzOverflow property.
     * 
     * @return
     *     possible object is
     *     {@link STTextHorzOverflowType }
     *     
     */
    public STTextHorzOverflowType getHorzOverflow() {
        return horzOverflow;
    }

    /**
     * Sets the value of the horzOverflow property.
     * 
     * @param value
     *     allowed object is
     *     {@link STTextHorzOverflowType }
     *     
     */
    public void setHorzOverflow(STTextHorzOverflowType value) {
        this.horzOverflow = value;
    }

    /**
     * Gets the value of the vert property.
     * 
     * @return
     *     possible object is
     *     {@link STTextVerticalType }
     *     
     */
    public STTextVerticalType getVert() {
        return vert;
    }

    /**
     * Sets the value of the vert property.
     * 
     * @param value
     *     allowed object is
     *     {@link STTextVerticalType }
     *     
     */
    public void setVert(STTextVerticalType value) {
        this.vert = value;
    }

    /**
     * Gets the value of the wrap property.
     * 
     * @return
     *     possible object is
     *     {@link STTextWrappingType }
     *     
     */
    public STTextWrappingType getWrap() {
        return wrap;
    }

    /**
     * Sets the value of the wrap property.
     * 
     * @param value
     *     allowed object is
     *     {@link STTextWrappingType }
     *     
     */
    public void setWrap(STTextWrappingType value) {
        this.wrap = value;
    }

    /**
     * Gets the value of the lIns property.
     * 
     * @return
     *     possible object is
     *     {@link Integer }
     *     
     */
    public Integer getLIns() {
        return lIns;
    }

    /**
     * Sets the value of the lIns property.
     * 
     * @param value
     *     allowed object is
     *     {@link Integer }
     *     
     */
    public void setLIns(Integer value) {
        this.lIns = value;
    }

    /**
     * Gets the value of the tIns property.
     * 
     * @return
     *     possible object is
     *     {@link Integer }
     *     
     */
    public Integer getTIns() {
        return tIns;
    }

    /**
     * Sets the value of the tIns property.
     * 
     * @param value
     *     allowed object is
     *     {@link Integer }
     *     
     */
    public void setTIns(Integer value) {
        this.tIns = value;
    }

    /**
     * Gets the value of the rIns property.
     * 
     * @return
     *     possible object is
     *     {@link Integer }
     *     
     */
    public Integer getRIns() {
        return rIns;
    }

    /**
     * Sets the value of the rIns property.
     * 
     * @param value
     *     allowed object is
     *     {@link Integer }
     *     
     */
    public void setRIns(Integer value) {
        this.rIns = value;
    }

    /**
     * Gets the value of the bIns property.
     * 
     * @return
     *     possible object is
     *     {@link Integer }
     *     
     */
    public Integer getBIns() {
        return bIns;
    }

    /**
     * Sets the value of the bIns property.
     * 
     * @param value
     *     allowed object is
     *     {@link Integer }
     *     
     */
    public void setBIns(Integer value) {
        this.bIns = value;
    }

    /**
     * Gets the value of the numCol property.
     * 
     * @return
     *     possible object is
     *     {@link Integer }
     *     
     */
    public Integer getNumCol() {
        return numCol;
    }

    /**
     * Sets the value of the numCol property.
     * 
     * @param value
     *     allowed object is
     *     {@link Integer }
     *     
     */
    public void setNumCol(Integer value) {
        this.numCol = value;
    }

    /**
     * Gets the value of the spcCol property.
     * 
     * @return
     *     possible object is
     *     {@link Integer }
     *     
     */
    public Integer getSpcCol() {
        return spcCol;
    }

    /**
     * Sets the value of the spcCol property.
     * 
     * @param value
     *     allowed object is
     *     {@link Integer }
     *     
     */
    public void setSpcCol(Integer value) {
        this.spcCol = value;
    }

    /**
     * Gets the value of the rtlCol property.
     * 
     * @return
     *     possible object is
     *     {@link Boolean }
     *     
     */
    public Boolean isRtlCol() {
        return rtlCol;
    }

    /**
     * Sets the value of the rtlCol property.
     * 
     * @param value
     *     allowed object is
     *     {@link Boolean }
     *     
     */
    public void setRtlCol(Boolean value) {
        this.rtlCol = value;
    }

    /**
     * Gets the value of the fromWordArt property.
     * 
     * @return
     *     possible object is
     *     {@link Boolean }
     *     
     */
    public Boolean isFromWordArt() {
        return fromWordArt;
    }

    /**
     * Sets the value of the fromWordArt property.
     * 
     * @param value
     *     allowed object is
     *     {@link Boolean }
     *     
     */
    public void setFromWordArt(Boolean value) {
        this.fromWordArt = value;
    }

    /**
     * Gets the value of the anchor property.
     * 
     * @return
     *     possible object is
     *     {@link STTextAnchoringType }
     *     
     */
    public STTextAnchoringType getAnchor() {
        return anchor;
    }

    /**
     * Sets the value of the anchor property.
     * 
     * @param value
     *     allowed object is
     *     {@link STTextAnchoringType }
     *     
     */
    public void setAnchor(STTextAnchoringType value) {
        this.anchor = value;
    }

    /**
     * Gets the value of the anchorCtr property.
     * 
     * @return
     *     possible object is
     *     {@link Boolean }
     *     
     */
    public Boolean isAnchorCtr() {
        return anchorCtr;
    }

    /**
     * Sets the value of the anchorCtr property.
     * 
     * @param value
     *     allowed object is
     *     {@link Boolean }
     *     
     */
    public void setAnchorCtr(Boolean value) {
        this.anchorCtr = value;
    }

    /**
     * Gets the value of the forceAA property.
     * 
     * @return
     *     possible object is
     *     {@link Boolean }
     *     
     */
    public Boolean isForceAA() {
        return forceAA;
    }

    /**
     * Sets the value of the forceAA property.
     * 
     * @param value
     *     allowed object is
     *     {@link Boolean }
     *     
     */
    public void setForceAA(Boolean value) {
        this.forceAA = value;
    }

    /**
     * Gets the value of the upright property.
     * 
     * @return
     *     possible object is
     *     {@link Boolean }
     *     
     */
    public boolean isUpright() {
        if (upright == null) {
            return false;
        } else {
            return upright;
        }
    }

    /**
     * Sets the value of the upright property.
     * 
     * @param value
     *     allowed object is
     *     {@link Boolean }
     *     
     */
    public void setUpright(Boolean value) {
        this.upright = value;
    }

    /**
     * Gets the value of the compatLnSpc property.
     * 
     * @return
     *     possible object is
     *     {@link Boolean }
     *     
     */
    public Boolean isCompatLnSpc() {
        return compatLnSpc;
    }

    /**
     * Sets the value of the compatLnSpc property.
     * 
     * @param value
     *     allowed object is
     *     {@link Boolean }
     *     
     */
    public void setCompatLnSpc(Boolean value) {
        this.compatLnSpc = value;
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
