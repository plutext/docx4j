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


package org.docx4j.dml.wordprocessingDrawing;

import javax.xml.bind.Unmarshaller;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlSchemaType;
import javax.xml.bind.annotation.XmlTransient;
import javax.xml.bind.annotation.XmlType;
import javax.xml.bind.annotation.adapters.HexBinaryAdapter;
import javax.xml.bind.annotation.adapters.XmlJavaTypeAdapter;
import org.docx4j.com.microsoft.schemas.office.word.x2010.wordprocessingDrawing.CTSizeRelH;
import org.docx4j.com.microsoft.schemas.office.word.x2010.wordprocessingDrawing.CTSizeRelV;
import org.docx4j.dml.CTNonVisualDrawingProps;
import org.docx4j.dml.CTNonVisualGraphicFrameProperties;
import org.docx4j.dml.CTPoint2D;
import org.docx4j.dml.CTPositiveSize2D;
import org.docx4j.dml.Graphic;
import org.jvnet.jaxb2_commons.ppp.Child;


/**
 * <p>Java class for CT_Anchor complex type.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
 * 
 * <pre>
 * &lt;complexType name="CT_Anchor"&gt;
 *   &lt;complexContent&gt;
 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType"&gt;
 *       &lt;sequence&gt;
 *         &lt;element name="simplePos" type="{http://schemas.openxmlformats.org/drawingml/2006/main}CT_Point2D"/&gt;
 *         &lt;element name="positionH" type="{http://schemas.openxmlformats.org/drawingml/2006/wordprocessingDrawing}CT_PosH"/&gt;
 *         &lt;element name="positionV" type="{http://schemas.openxmlformats.org/drawingml/2006/wordprocessingDrawing}CT_PosV"/&gt;
 *         &lt;element name="extent" type="{http://schemas.openxmlformats.org/drawingml/2006/main}CT_PositiveSize2D"/&gt;
 *         &lt;element name="effectExtent" type="{http://schemas.openxmlformats.org/drawingml/2006/wordprocessingDrawing}CT_EffectExtent" minOccurs="0"/&gt;
 *         &lt;group ref="{http://schemas.openxmlformats.org/drawingml/2006/wordprocessingDrawing}EG_WrapType"/&gt;
 *         &lt;element name="docPr" type="{http://schemas.openxmlformats.org/drawingml/2006/main}CT_NonVisualDrawingProps"/&gt;
 *         &lt;element name="cNvGraphicFramePr" type="{http://schemas.openxmlformats.org/drawingml/2006/main}CT_NonVisualGraphicFrameProperties" minOccurs="0"/&gt;
 *         &lt;element ref="{http://schemas.openxmlformats.org/drawingml/2006/main}graphic"/&gt;
 *         &lt;element ref="{http://schemas.microsoft.com/office/word/2010/wordprocessingDrawing}sizeRelH" minOccurs="0"/&gt;
 *         &lt;element ref="{http://schemas.microsoft.com/office/word/2010/wordprocessingDrawing}sizeRelV" minOccurs="0"/&gt;
 *       &lt;/sequence&gt;
 *       &lt;attribute name="distT" type="{http://schemas.openxmlformats.org/drawingml/2006/wordprocessingDrawing}ST_WrapDistance" /&gt;
 *       &lt;attribute name="distB" type="{http://schemas.openxmlformats.org/drawingml/2006/wordprocessingDrawing}ST_WrapDistance" /&gt;
 *       &lt;attribute name="distL" type="{http://schemas.openxmlformats.org/drawingml/2006/wordprocessingDrawing}ST_WrapDistance" /&gt;
 *       &lt;attribute name="distR" type="{http://schemas.openxmlformats.org/drawingml/2006/wordprocessingDrawing}ST_WrapDistance" /&gt;
 *       &lt;attribute name="simplePos" type="{http://www.w3.org/2001/XMLSchema}boolean" /&gt;
 *       &lt;attribute name="relativeHeight" use="required" type="{http://www.w3.org/2001/XMLSchema}unsignedInt" /&gt;
 *       &lt;attribute name="behindDoc" use="required" type="{http://www.w3.org/2001/XMLSchema}boolean" /&gt;
 *       &lt;attribute name="locked" use="required" type="{http://www.w3.org/2001/XMLSchema}boolean" /&gt;
 *       &lt;attribute name="layoutInCell" use="required" type="{http://www.w3.org/2001/XMLSchema}boolean" /&gt;
 *       &lt;attribute name="hidden" type="{http://www.w3.org/2001/XMLSchema}boolean" /&gt;
 *       &lt;attribute name="allowOverlap" use="required" type="{http://www.w3.org/2001/XMLSchema}boolean" /&gt;
 *       &lt;attribute ref="{http://schemas.microsoft.com/office/word/2010/wordprocessingDrawing}anchorId"/&gt;
 *       &lt;attribute ref="{http://schemas.microsoft.com/office/word/2010/wordprocessingDrawing}editId"/&gt;
 *     &lt;/restriction&gt;
 *   &lt;/complexContent&gt;
 * &lt;/complexType&gt;
 * </pre>
 * 
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "CT_Anchor", propOrder = {
    "simplePos",
    "positionH",
    "positionV",
    "extent",
    "effectExtent",
    "wrapNone",
    "wrapSquare",
    "wrapTight",
    "wrapThrough",
    "wrapTopAndBottom",
    "docPr",
    "cNvGraphicFramePr",
    "graphic",
    "sizeRelH",
    "sizeRelV"
})
public class Anchor implements Child
{

    @XmlElement(required = true)
    protected CTPoint2D simplePos;
    @XmlElement(required = true)
    protected CTPosH positionH;
    @XmlElement(required = true)
    protected CTPosV positionV;
    @XmlElement(required = true)
    protected CTPositiveSize2D extent;
    protected CTEffectExtent effectExtent;
    protected CTWrapNone wrapNone;
    protected CTWrapSquare wrapSquare;
    protected CTWrapTight wrapTight;
    protected CTWrapThrough wrapThrough;
    protected CTWrapTopBottom wrapTopAndBottom;
    @XmlElement(required = true)
    protected CTNonVisualDrawingProps docPr;
    protected CTNonVisualGraphicFrameProperties cNvGraphicFramePr;
    @XmlElement(namespace = "http://schemas.openxmlformats.org/drawingml/2006/main", required = true)
    protected Graphic graphic;
    @XmlElement(namespace = "http://schemas.microsoft.com/office/word/2010/wordprocessingDrawing")
    protected CTSizeRelH sizeRelH;
    @XmlElement(namespace = "http://schemas.microsoft.com/office/word/2010/wordprocessingDrawing")
    protected CTSizeRelV sizeRelV;
    @XmlAttribute(name = "distT")
    protected Long distT;
    @XmlAttribute(name = "distB")
    protected Long distB;
    @XmlAttribute(name = "distL")
    protected Long distL;
    @XmlAttribute(name = "distR")
    protected Long distR;
    @XmlAttribute(name = "simplePos")
    protected Boolean simplePosAttr;
    @XmlAttribute(name = "relativeHeight", required = true)
    @XmlSchemaType(name = "unsignedInt")
    protected long relativeHeight;
    @XmlAttribute(name = "behindDoc", required = true)
    protected boolean behindDoc;
    @XmlAttribute(name = "locked", required = true)
    protected boolean locked;
    @XmlAttribute(name = "layoutInCell", required = true)
    protected boolean layoutInCell;
    @XmlAttribute(name = "hidden")
    protected Boolean hidden;
    @XmlAttribute(name = "allowOverlap", required = true)
    protected boolean allowOverlap;
    @XmlAttribute(name = "anchorId", namespace = "http://schemas.microsoft.com/office/word/2010/wordprocessingDrawing")
    @XmlJavaTypeAdapter(HexBinaryAdapter.class)
    protected byte[] anchorId;
    @XmlAttribute(name = "editId", namespace = "http://schemas.microsoft.com/office/word/2010/wordprocessingDrawing")
    @XmlJavaTypeAdapter(HexBinaryAdapter.class)
    protected byte[] editId;
    @XmlTransient
    private Object parent;

    /**
     * Gets the value of the simplePos property.
     * 
     * @return
     *     possible object is
     *     {@link CTPoint2D }
     *     
     */
    public CTPoint2D getSimplePos() {
        return simplePos;
    }

    /**
     * Sets the value of the simplePos property.
     * 
     * @param value
     *     allowed object is
     *     {@link CTPoint2D }
     *     
     */
    public void setSimplePos(CTPoint2D value) {
        this.simplePos = value;
    }

    /**
     * Gets the value of the positionH property.
     * 
     * @return
     *     possible object is
     *     {@link CTPosH }
     *     
     */
    public CTPosH getPositionH() {
        return positionH;
    }

    /**
     * Sets the value of the positionH property.
     * 
     * @param value
     *     allowed object is
     *     {@link CTPosH }
     *     
     */
    public void setPositionH(CTPosH value) {
        this.positionH = value;
    }

    /**
     * Gets the value of the positionV property.
     * 
     * @return
     *     possible object is
     *     {@link CTPosV }
     *     
     */
    public CTPosV getPositionV() {
        return positionV;
    }

    /**
     * Sets the value of the positionV property.
     * 
     * @param value
     *     allowed object is
     *     {@link CTPosV }
     *     
     */
    public void setPositionV(CTPosV value) {
        this.positionV = value;
    }

    /**
     * Gets the value of the extent property.
     * 
     * @return
     *     possible object is
     *     {@link CTPositiveSize2D }
     *     
     */
    public CTPositiveSize2D getExtent() {
        return extent;
    }

    /**
     * Sets the value of the extent property.
     * 
     * @param value
     *     allowed object is
     *     {@link CTPositiveSize2D }
     *     
     */
    public void setExtent(CTPositiveSize2D value) {
        this.extent = value;
    }

    /**
     * Gets the value of the effectExtent property.
     * 
     * @return
     *     possible object is
     *     {@link CTEffectExtent }
     *     
     */
    public CTEffectExtent getEffectExtent() {
        return effectExtent;
    }

    /**
     * Sets the value of the effectExtent property.
     * 
     * @param value
     *     allowed object is
     *     {@link CTEffectExtent }
     *     
     */
    public void setEffectExtent(CTEffectExtent value) {
        this.effectExtent = value;
    }

    /**
     * Gets the value of the wrapNone property.
     * 
     * @return
     *     possible object is
     *     {@link CTWrapNone }
     *     
     */
    public CTWrapNone getWrapNone() {
        return wrapNone;
    }

    /**
     * Sets the value of the wrapNone property.
     * 
     * @param value
     *     allowed object is
     *     {@link CTWrapNone }
     *     
     */
    public void setWrapNone(CTWrapNone value) {
        this.wrapNone = value;
    }

    /**
     * Gets the value of the wrapSquare property.
     * 
     * @return
     *     possible object is
     *     {@link CTWrapSquare }
     *     
     */
    public CTWrapSquare getWrapSquare() {
        return wrapSquare;
    }

    /**
     * Sets the value of the wrapSquare property.
     * 
     * @param value
     *     allowed object is
     *     {@link CTWrapSquare }
     *     
     */
    public void setWrapSquare(CTWrapSquare value) {
        this.wrapSquare = value;
    }

    /**
     * Gets the value of the wrapTight property.
     * 
     * @return
     *     possible object is
     *     {@link CTWrapTight }
     *     
     */
    public CTWrapTight getWrapTight() {
        return wrapTight;
    }

    /**
     * Sets the value of the wrapTight property.
     * 
     * @param value
     *     allowed object is
     *     {@link CTWrapTight }
     *     
     */
    public void setWrapTight(CTWrapTight value) {
        this.wrapTight = value;
    }

    /**
     * Gets the value of the wrapThrough property.
     * 
     * @return
     *     possible object is
     *     {@link CTWrapThrough }
     *     
     */
    public CTWrapThrough getWrapThrough() {
        return wrapThrough;
    }

    /**
     * Sets the value of the wrapThrough property.
     * 
     * @param value
     *     allowed object is
     *     {@link CTWrapThrough }
     *     
     */
    public void setWrapThrough(CTWrapThrough value) {
        this.wrapThrough = value;
    }

    /**
     * Gets the value of the wrapTopAndBottom property.
     * 
     * @return
     *     possible object is
     *     {@link CTWrapTopBottom }
     *     
     */
    public CTWrapTopBottom getWrapTopAndBottom() {
        return wrapTopAndBottom;
    }

    /**
     * Sets the value of the wrapTopAndBottom property.
     * 
     * @param value
     *     allowed object is
     *     {@link CTWrapTopBottom }
     *     
     */
    public void setWrapTopAndBottom(CTWrapTopBottom value) {
        this.wrapTopAndBottom = value;
    }

    /**
     * Gets the value of the docPr property.
     * 
     * @return
     *     possible object is
     *     {@link CTNonVisualDrawingProps }
     *     
     */
    public CTNonVisualDrawingProps getDocPr() {
        return docPr;
    }

    /**
     * Sets the value of the docPr property.
     * 
     * @param value
     *     allowed object is
     *     {@link CTNonVisualDrawingProps }
     *     
     */
    public void setDocPr(CTNonVisualDrawingProps value) {
        this.docPr = value;
    }

    /**
     * Gets the value of the cNvGraphicFramePr property.
     * 
     * @return
     *     possible object is
     *     {@link CTNonVisualGraphicFrameProperties }
     *     
     */
    public CTNonVisualGraphicFrameProperties getCNvGraphicFramePr() {
        return cNvGraphicFramePr;
    }

    /**
     * Sets the value of the cNvGraphicFramePr property.
     * 
     * @param value
     *     allowed object is
     *     {@link CTNonVisualGraphicFrameProperties }
     *     
     */
    public void setCNvGraphicFramePr(CTNonVisualGraphicFrameProperties value) {
        this.cNvGraphicFramePr = value;
    }

    /**
     * Gets the value of the graphic property.
     * 
     * @return
     *     possible object is
     *     {@link Graphic }
     *     
     */
    public Graphic getGraphic() {
        return graphic;
    }

    /**
     * Sets the value of the graphic property.
     * 
     * @param value
     *     allowed object is
     *     {@link Graphic }
     *     
     */
    public void setGraphic(Graphic value) {
        this.graphic = value;
    }

    /**
     * Gets the value of the sizeRelH property.
     * 
     * @return
     *     possible object is
     *     {@link CTSizeRelH }
     *     
     */
    public CTSizeRelH getSizeRelH() {
        return sizeRelH;
    }

    /**
     * Sets the value of the sizeRelH property.
     * 
     * @param value
     *     allowed object is
     *     {@link CTSizeRelH }
     *     
     */
    public void setSizeRelH(CTSizeRelH value) {
        this.sizeRelH = value;
    }

    /**
     * Gets the value of the sizeRelV property.
     * 
     * @return
     *     possible object is
     *     {@link CTSizeRelV }
     *     
     */
    public CTSizeRelV getSizeRelV() {
        return sizeRelV;
    }

    /**
     * Sets the value of the sizeRelV property.
     * 
     * @param value
     *     allowed object is
     *     {@link CTSizeRelV }
     *     
     */
    public void setSizeRelV(CTSizeRelV value) {
        this.sizeRelV = value;
    }

    /**
     * Gets the value of the distT property.
     * 
     * @return
     *     possible object is
     *     {@link Long }
     *     
     */
    public Long getDistT() {
        return distT;
    }

    /**
     * Sets the value of the distT property.
     * 
     * @param value
     *     allowed object is
     *     {@link Long }
     *     
     */
    public void setDistT(Long value) {
        this.distT = value;
    }

    /**
     * Gets the value of the distB property.
     * 
     * @return
     *     possible object is
     *     {@link Long }
     *     
     */
    public Long getDistB() {
        return distB;
    }

    /**
     * Sets the value of the distB property.
     * 
     * @param value
     *     allowed object is
     *     {@link Long }
     *     
     */
    public void setDistB(Long value) {
        this.distB = value;
    }

    /**
     * Gets the value of the distL property.
     * 
     * @return
     *     possible object is
     *     {@link Long }
     *     
     */
    public Long getDistL() {
        return distL;
    }

    /**
     * Sets the value of the distL property.
     * 
     * @param value
     *     allowed object is
     *     {@link Long }
     *     
     */
    public void setDistL(Long value) {
        this.distL = value;
    }

    /**
     * Gets the value of the distR property.
     * 
     * @return
     *     possible object is
     *     {@link Long }
     *     
     */
    public Long getDistR() {
        return distR;
    }

    /**
     * Sets the value of the distR property.
     * 
     * @param value
     *     allowed object is
     *     {@link Long }
     *     
     */
    public void setDistR(Long value) {
        this.distR = value;
    }

    /**
     * Gets the value of the simplePosAttr property.
     * 
     * @return
     *     possible object is
     *     {@link Boolean }
     *     
     */
    public Boolean isSimplePosAttr() {
        return simplePosAttr;
    }

    /**
     * Sets the value of the simplePosAttr property.
     * 
     * @param value
     *     allowed object is
     *     {@link Boolean }
     *     
     */
    public void setSimplePosAttr(Boolean value) {
        this.simplePosAttr = value;
    }

    /**
     * Gets the value of the relativeHeight property.
     * 
     */
    public long getRelativeHeight() {
        return relativeHeight;
    }

    /**
     * Sets the value of the relativeHeight property.
     * 
     */
    public void setRelativeHeight(long value) {
        this.relativeHeight = value;
    }

    /**
     * Gets the value of the behindDoc property.
     * 
     */
    public boolean isBehindDoc() {
        return behindDoc;
    }

    /**
     * Sets the value of the behindDoc property.
     * 
     */
    public void setBehindDoc(boolean value) {
        this.behindDoc = value;
    }

    /**
     * Gets the value of the locked property.
     * 
     */
    public boolean isLocked() {
        return locked;
    }

    /**
     * Sets the value of the locked property.
     * 
     */
    public void setLocked(boolean value) {
        this.locked = value;
    }

    /**
     * Gets the value of the layoutInCell property.
     * 
     */
    public boolean isLayoutInCell() {
        return layoutInCell;
    }

    /**
     * Sets the value of the layoutInCell property.
     * 
     */
    public void setLayoutInCell(boolean value) {
        this.layoutInCell = value;
    }

    /**
     * Gets the value of the hidden property.
     * 
     * @return
     *     possible object is
     *     {@link Boolean }
     *     
     */
    public Boolean isHidden() {
        return hidden;
    }

    /**
     * Sets the value of the hidden property.
     * 
     * @param value
     *     allowed object is
     *     {@link Boolean }
     *     
     */
    public void setHidden(Boolean value) {
        this.hidden = value;
    }

    /**
     * Gets the value of the allowOverlap property.
     * 
     */
    public boolean isAllowOverlap() {
        return allowOverlap;
    }

    /**
     * Sets the value of the allowOverlap property.
     * 
     */
    public void setAllowOverlap(boolean value) {
        this.allowOverlap = value;
    }

    /**
     * Gets the value of the anchorId property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public byte[] getAnchorId() {
        return anchorId;
    }

    /**
     * Sets the value of the anchorId property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setAnchorId(byte[] value) {
        this.anchorId = value;
    }

    /**
     * Gets the value of the editId property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public byte[] getEditId() {
        return editId;
    }

    /**
     * Sets the value of the editId property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setEditId(byte[] value) {
        this.editId = value;
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
