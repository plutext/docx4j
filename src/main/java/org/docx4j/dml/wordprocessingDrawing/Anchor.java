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
import javax.xml.bind.annotation.*;

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
 * &lt;complexType name="CT_Anchor">
 *   &lt;complexContent>
 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
 *       &lt;sequence>
 *         &lt;element name="simplePos" type="{http://schemas.openxmlformats.org/drawingml/2006/main}CT_Point2D"/>
 *         &lt;element name="positionH" type="{http://schemas.openxmlformats.org/drawingml/2006/wordprocessingDrawing}CT_PosH"/>
 *         &lt;element name="positionV" type="{http://schemas.openxmlformats.org/drawingml/2006/wordprocessingDrawing}CT_PosV"/>
 *         &lt;element name="extent" type="{http://schemas.openxmlformats.org/drawingml/2006/main}CT_PositiveSize2D"/>
 *         &lt;element name="effectExtent" type="{http://schemas.openxmlformats.org/drawingml/2006/wordprocessingDrawing}CT_EffectExtent" minOccurs="0"/>
 *         &lt;group ref="{http://schemas.openxmlformats.org/drawingml/2006/wordprocessingDrawing}EG_WrapType"/>
 *         &lt;element name="docPr" type="{http://schemas.openxmlformats.org/drawingml/2006/main}CT_NonVisualDrawingProps"/>
 *         &lt;element name="cNvGraphicFramePr" type="{http://schemas.openxmlformats.org/drawingml/2006/main}CT_NonVisualGraphicFrameProperties" minOccurs="0"/>
 *         &lt;element ref="{http://schemas.openxmlformats.org/drawingml/2006/main}graphic"/>
 *       &lt;/sequence>
 *       &lt;attribute name="distT" type="{http://schemas.openxmlformats.org/drawingml/2006/wordprocessingDrawing}ST_WrapDistance" />
 *       &lt;attribute name="distB" type="{http://schemas.openxmlformats.org/drawingml/2006/wordprocessingDrawing}ST_WrapDistance" />
 *       &lt;attribute name="distL" type="{http://schemas.openxmlformats.org/drawingml/2006/wordprocessingDrawing}ST_WrapDistance" />
 *       &lt;attribute name="distR" type="{http://schemas.openxmlformats.org/drawingml/2006/wordprocessingDrawing}ST_WrapDistance" />
 *       &lt;attribute name="simplePos" type="{http://www.w3.org/2001/XMLSchema}boolean" />
 *       &lt;attribute name="relativeHeight" use="required" type="{http://www.w3.org/2001/XMLSchema}unsignedInt" />
 *       &lt;attribute name="behindDoc" use="required" type="{http://www.w3.org/2001/XMLSchema}boolean" />
 *       &lt;attribute name="locked" use="required" type="{http://www.w3.org/2001/XMLSchema}boolean" />
 *       &lt;attribute name="layoutInCell" use="required" type="{http://www.w3.org/2001/XMLSchema}boolean" />
 *       &lt;attribute name="hidden" type="{http://www.w3.org/2001/XMLSchema}boolean" />
 *       &lt;attribute name="allowOverlap" use="required" type="{http://www.w3.org/2001/XMLSchema}boolean" />
 *     &lt;/restriction>
 *   &lt;/complexContent>
 * &lt;/complexType>
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
    "graphic"
})
public class Anchor implements Child {

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
    @XmlAttribute
    protected Long distT;
    @XmlAttribute
    protected Long distB;
    @XmlAttribute
    protected Long distL;
    @XmlAttribute
    protected Long distR;
    @XmlAttribute(name = "simplePos")
    protected Boolean simplePosAttr;
    @XmlAttribute(required = true)
    @XmlSchemaType(name = "unsignedInt")
    protected long relativeHeight;
    @XmlAttribute(required = true)
    protected boolean behindDoc;
    @XmlAttribute(required = true)
    protected boolean locked;
    @XmlAttribute(required = true)
    protected boolean layoutInCell;
    @XmlAttribute
    protected Boolean hidden;
    @XmlAttribute(required = true)
    protected boolean allowOverlap;
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
