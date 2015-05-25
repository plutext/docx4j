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


package org.docx4j.vml;

import java.math.BigDecimal;
import javax.xml.bind.Unmarshaller;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlTransient;
import javax.xml.bind.annotation.XmlType;
import org.docx4j.vml.officedrawing.CTStrokeChild;
import org.jvnet.jaxb2_commons.ppp.Child;


/**
 * <p>Java class for CT_Stroke complex type.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
 * 
 * <pre>
 * &lt;complexType name="CT_Stroke">
 *   &lt;complexContent>
 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
 *       &lt;sequence>
 *         &lt;element ref="{urn:schemas-microsoft-com:office:office}left" minOccurs="0"/>
 *         &lt;element ref="{urn:schemas-microsoft-com:office:office}top" minOccurs="0"/>
 *         &lt;element ref="{urn:schemas-microsoft-com:office:office}right" minOccurs="0"/>
 *         &lt;element ref="{urn:schemas-microsoft-com:office:office}bottom" minOccurs="0"/>
 *         &lt;element ref="{urn:schemas-microsoft-com:office:office}column" minOccurs="0"/>
 *       &lt;/sequence>
 *       &lt;attGroup ref="{urn:schemas-microsoft-com:vml}AG_Id"/>
 *       &lt;attGroup ref="{urn:schemas-microsoft-com:vml}AG_StrokeAttributes"/>
 *     &lt;/restriction>
 *   &lt;/complexContent>
 * &lt;/complexType>
 * </pre>
 * 
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "CT_Stroke", propOrder = {
    "left",
    "top",
    "right",
    "bottom",
    "column"
})
public class CTStroke implements Child
{

    @XmlElement(namespace = "urn:schemas-microsoft-com:office:office")
    protected CTStrokeChild left;
    @XmlElement(namespace = "urn:schemas-microsoft-com:office:office")
    protected CTStrokeChild top;
    @XmlElement(namespace = "urn:schemas-microsoft-com:office:office")
    protected CTStrokeChild right;
    @XmlElement(namespace = "urn:schemas-microsoft-com:office:office")
    protected CTStrokeChild bottom;
    @XmlElement(namespace = "urn:schemas-microsoft-com:office:office")
    protected CTStrokeChild column;
    @XmlAttribute(name = "id")
    protected String vmlId;
    @XmlAttribute(name = "on")
    protected org.docx4j.vml.STTrueFalse on;
    @XmlAttribute(name = "weight")
    protected String weight;
    @XmlAttribute(name = "color")
    protected String color;
    @XmlAttribute(name = "opacity")
    protected String opacity;
    @XmlAttribute(name = "linestyle")
    protected STStrokeLineStyle linestyle;
    @XmlAttribute(name = "miterlimit")
    protected String miterlimit;
    @XmlAttribute(name = "joinstyle")
    protected STStrokeJoinStyle joinstyle;
    @XmlAttribute(name = "endcap")
    protected STStrokeEndCap endcap;
    @XmlAttribute(name = "dashstyle")
    protected String dashstyle;
    @XmlAttribute(name = "filltype")
    protected STFillType filltype;
    @XmlAttribute(name = "src")
    protected String src;
    @XmlAttribute(name = "imageaspect")
    protected STImageAspect imageaspect;
    @XmlAttribute(name = "imagesize")
    protected String imagesize;
    @XmlAttribute(name = "imagealignshape")
    protected org.docx4j.vml.STTrueFalse imagealignshape;
    @XmlAttribute(name = "color2")
    protected String color2;
    @XmlAttribute(name = "startarrow")
    protected STStrokeArrowType startarrow;
    @XmlAttribute(name = "startarrowwidth")
    protected STStrokeArrowWidth startarrowwidth;
    @XmlAttribute(name = "startarrowlength")
    protected STStrokeArrowLength startarrowlength;
    @XmlAttribute(name = "endarrow")
    protected STStrokeArrowType endarrow;
    @XmlAttribute(name = "endarrowwidth")
    protected STStrokeArrowWidth endarrowwidth;
    @XmlAttribute(name = "endarrowlength")
    protected STStrokeArrowLength endarrowlength;
    @XmlAttribute(name = "href", namespace = "urn:schemas-microsoft-com:office:office")
    protected String href;
    @XmlAttribute(name = "althref", namespace = "urn:schemas-microsoft-com:office:office")
    protected String althref;
    @XmlAttribute(name = "title", namespace = "urn:schemas-microsoft-com:office:office")
    protected String title;
    @XmlAttribute(name = "forcedash", namespace = "urn:schemas-microsoft-com:office:office")
    protected org.docx4j.vml.officedrawing.STTrueFalse forcedash;
    @XmlAttribute(name = "id", namespace = "http://schemas.openxmlformats.org/officeDocument/2006/relationships")
    protected String id;
    @XmlAttribute(name = "insetpen")
    protected org.docx4j.vml.STTrueFalse insetpen;
    @XmlAttribute(name = "relid", namespace = "urn:schemas-microsoft-com:office:office")
    protected String relid;
    @XmlTransient
    private Object parent;

    /**
     * Gets the value of the left property.
     * 
     * @return
     *     possible object is
     *     {@link CTStrokeChild }
     *     
     */
    public CTStrokeChild getLeft() {
        return left;
    }

    /**
     * Sets the value of the left property.
     * 
     * @param value
     *     allowed object is
     *     {@link CTStrokeChild }
     *     
     */
    public void setLeft(CTStrokeChild value) {
        this.left = value;
    }

    /**
     * Gets the value of the top property.
     * 
     * @return
     *     possible object is
     *     {@link CTStrokeChild }
     *     
     */
    public CTStrokeChild getTop() {
        return top;
    }

    /**
     * Sets the value of the top property.
     * 
     * @param value
     *     allowed object is
     *     {@link CTStrokeChild }
     *     
     */
    public void setTop(CTStrokeChild value) {
        this.top = value;
    }

    /**
     * Gets the value of the right property.
     * 
     * @return
     *     possible object is
     *     {@link CTStrokeChild }
     *     
     */
    public CTStrokeChild getRight() {
        return right;
    }

    /**
     * Sets the value of the right property.
     * 
     * @param value
     *     allowed object is
     *     {@link CTStrokeChild }
     *     
     */
    public void setRight(CTStrokeChild value) {
        this.right = value;
    }

    /**
     * Gets the value of the bottom property.
     * 
     * @return
     *     possible object is
     *     {@link CTStrokeChild }
     *     
     */
    public CTStrokeChild getBottom() {
        return bottom;
    }

    /**
     * Sets the value of the bottom property.
     * 
     * @param value
     *     allowed object is
     *     {@link CTStrokeChild }
     *     
     */
    public void setBottom(CTStrokeChild value) {
        this.bottom = value;
    }

    /**
     * Gets the value of the column property.
     * 
     * @return
     *     possible object is
     *     {@link CTStrokeChild }
     *     
     */
    public CTStrokeChild getColumn() {
        return column;
    }

    /**
     * Sets the value of the column property.
     * 
     * @param value
     *     allowed object is
     *     {@link CTStrokeChild }
     *     
     */
    public void setColumn(CTStrokeChild value) {
        this.column = value;
    }

    /**
     * Gets the value of the vmlId property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getVmlId() {
        return vmlId;
    }

    /**
     * Sets the value of the vmlId property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setVmlId(String value) {
        this.vmlId = value;
    }

    /**
     * Gets the value of the on property.
     * 
     * @return
     *     possible object is
     *     {@link org.docx4j.vml.STTrueFalse }
     *     
     */
    public org.docx4j.vml.STTrueFalse getOn() {
        return on;
    }

    /**
     * Sets the value of the on property.
     * 
     * @param value
     *     allowed object is
     *     {@link org.docx4j.vml.STTrueFalse }
     *     
     */
    public void setOn(org.docx4j.vml.STTrueFalse value) {
        this.on = value;
    }

    /**
     * Gets the value of the weight property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getWeight() {
        return weight;
    }

    /**
     * Sets the value of the weight property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setWeight(String value) {
        this.weight = value;
    }

    /**
     * Gets the value of the color property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getColor() {
        return color;
    }

    /**
     * Sets the value of the color property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setColor(String value) {
        this.color = value;
    }

    /**
     * Gets the value of the opacity property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getOpacity() {
        return opacity;
    }

    /**
     * Sets the value of the opacity property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setOpacity(String value) {
        this.opacity = value;
    }

    /**
     * Gets the value of the linestyle property.
     * 
     * @return
     *     possible object is
     *     {@link STStrokeLineStyle }
     *     
     */
    public STStrokeLineStyle getLinestyle() {
        return linestyle;
    }

    /**
     * Sets the value of the linestyle property.
     * 
     * @param value
     *     allowed object is
     *     {@link STStrokeLineStyle }
     *     
     */
    public void setLinestyle(STStrokeLineStyle value) {
        this.linestyle = value;
    }

    /**
     * Gets the value of the miterlimit property.
     * 
     * @return
     *     possible object is
     *     {@link BigDecimal }
     *     
     */
    public String getMiterlimit() {
        return miterlimit;
    }

    /**
     * Sets the value of the miterlimit property.
     * 
     * @param value
     *     allowed object is
     *     {@link BigDecimal }
     *     
     */
    public void setMiterlimit(String value) {
        this.miterlimit = value;
    }

    /**
     * Gets the value of the joinstyle property.
     * 
     * @return
     *     possible object is
     *     {@link STStrokeJoinStyle }
     *     
     */
    public STStrokeJoinStyle getJoinstyle() {
        return joinstyle;
    }

    /**
     * Sets the value of the joinstyle property.
     * 
     * @param value
     *     allowed object is
     *     {@link STStrokeJoinStyle }
     *     
     */
    public void setJoinstyle(STStrokeJoinStyle value) {
        this.joinstyle = value;
    }

    /**
     * Gets the value of the endcap property.
     * 
     * @return
     *     possible object is
     *     {@link STStrokeEndCap }
     *     
     */
    public STStrokeEndCap getEndcap() {
        return endcap;
    }

    /**
     * Sets the value of the endcap property.
     * 
     * @param value
     *     allowed object is
     *     {@link STStrokeEndCap }
     *     
     */
    public void setEndcap(STStrokeEndCap value) {
        this.endcap = value;
    }

    /**
     * Gets the value of the dashstyle property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getDashstyle() {
        return dashstyle;
    }

    /**
     * Sets the value of the dashstyle property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setDashstyle(String value) {
        this.dashstyle = value;
    }

    /**
     * Gets the value of the filltype property.
     * 
     * @return
     *     possible object is
     *     {@link STFillType }
     *     
     */
    public STFillType getFilltype() {
        return filltype;
    }

    /**
     * Sets the value of the filltype property.
     * 
     * @param value
     *     allowed object is
     *     {@link STFillType }
     *     
     */
    public void setFilltype(STFillType value) {
        this.filltype = value;
    }

    /**
     * Gets the value of the src property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getSrc() {
        return src;
    }

    /**
     * Sets the value of the src property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setSrc(String value) {
        this.src = value;
    }

    /**
     * Gets the value of the imageaspect property.
     * 
     * @return
     *     possible object is
     *     {@link STImageAspect }
     *     
     */
    public STImageAspect getImageaspect() {
        return imageaspect;
    }

    /**
     * Sets the value of the imageaspect property.
     * 
     * @param value
     *     allowed object is
     *     {@link STImageAspect }
     *     
     */
    public void setImageaspect(STImageAspect value) {
        this.imageaspect = value;
    }

    /**
     * Gets the value of the imagesize property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getImagesize() {
        return imagesize;
    }

    /**
     * Sets the value of the imagesize property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setImagesize(String value) {
        this.imagesize = value;
    }

    /**
     * Gets the value of the imagealignshape property.
     * 
     * @return
     *     possible object is
     *     {@link org.docx4j.vml.STTrueFalse }
     *     
     */
    public org.docx4j.vml.STTrueFalse getImagealignshape() {
        return imagealignshape;
    }

    /**
     * Sets the value of the imagealignshape property.
     * 
     * @param value
     *     allowed object is
     *     {@link org.docx4j.vml.STTrueFalse }
     *     
     */
    public void setImagealignshape(org.docx4j.vml.STTrueFalse value) {
        this.imagealignshape = value;
    }

    /**
     * Gets the value of the color2 property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getColor2() {
        return color2;
    }

    /**
     * Sets the value of the color2 property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setColor2(String value) {
        this.color2 = value;
    }

    /**
     * Gets the value of the startarrow property.
     * 
     * @return
     *     possible object is
     *     {@link STStrokeArrowType }
     *     
     */
    public STStrokeArrowType getStartarrow() {
        return startarrow;
    }

    /**
     * Sets the value of the startarrow property.
     * 
     * @param value
     *     allowed object is
     *     {@link STStrokeArrowType }
     *     
     */
    public void setStartarrow(STStrokeArrowType value) {
        this.startarrow = value;
    }

    /**
     * Gets the value of the startarrowwidth property.
     * 
     * @return
     *     possible object is
     *     {@link STStrokeArrowWidth }
     *     
     */
    public STStrokeArrowWidth getStartarrowwidth() {
        return startarrowwidth;
    }

    /**
     * Sets the value of the startarrowwidth property.
     * 
     * @param value
     *     allowed object is
     *     {@link STStrokeArrowWidth }
     *     
     */
    public void setStartarrowwidth(STStrokeArrowWidth value) {
        this.startarrowwidth = value;
    }

    /**
     * Gets the value of the startarrowlength property.
     * 
     * @return
     *     possible object is
     *     {@link STStrokeArrowLength }
     *     
     */
    public STStrokeArrowLength getStartarrowlength() {
        return startarrowlength;
    }

    /**
     * Sets the value of the startarrowlength property.
     * 
     * @param value
     *     allowed object is
     *     {@link STStrokeArrowLength }
     *     
     */
    public void setStartarrowlength(STStrokeArrowLength value) {
        this.startarrowlength = value;
    }

    /**
     * Gets the value of the endarrow property.
     * 
     * @return
     *     possible object is
     *     {@link STStrokeArrowType }
     *     
     */
    public STStrokeArrowType getEndarrow() {
        return endarrow;
    }

    /**
     * Sets the value of the endarrow property.
     * 
     * @param value
     *     allowed object is
     *     {@link STStrokeArrowType }
     *     
     */
    public void setEndarrow(STStrokeArrowType value) {
        this.endarrow = value;
    }

    /**
     * Gets the value of the endarrowwidth property.
     * 
     * @return
     *     possible object is
     *     {@link STStrokeArrowWidth }
     *     
     */
    public STStrokeArrowWidth getEndarrowwidth() {
        return endarrowwidth;
    }

    /**
     * Sets the value of the endarrowwidth property.
     * 
     * @param value
     *     allowed object is
     *     {@link STStrokeArrowWidth }
     *     
     */
    public void setEndarrowwidth(STStrokeArrowWidth value) {
        this.endarrowwidth = value;
    }

    /**
     * Gets the value of the endarrowlength property.
     * 
     * @return
     *     possible object is
     *     {@link STStrokeArrowLength }
     *     
     */
    public STStrokeArrowLength getEndarrowlength() {
        return endarrowlength;
    }

    /**
     * Sets the value of the endarrowlength property.
     * 
     * @param value
     *     allowed object is
     *     {@link STStrokeArrowLength }
     *     
     */
    public void setEndarrowlength(STStrokeArrowLength value) {
        this.endarrowlength = value;
    }

    /**
     * Original Image Reference
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getHref() {
        return href;
    }

    /**
     * Sets the value of the href property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setHref(String value) {
        this.href = value;
    }

    /**
     * Alternate Image Reference
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getAlthref() {
        return althref;
    }

    /**
     * Sets the value of the althref property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setAlthref(String value) {
        this.althref = value;
    }

    /**
     * Stroke Title
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getTitle() {
        return title;
    }

    /**
     * Sets the value of the title property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setTitle(String value) {
        this.title = value;
    }

    /**
     * Force Dashed Outline
     * 
     * @return
     *     possible object is
     *     {@link org.docx4j.vml.officedrawing.STTrueFalse }
     *     
     */
    public org.docx4j.vml.officedrawing.STTrueFalse getForcedash() {
        return forcedash;
    }

    /**
     * Sets the value of the forcedash property.
     * 
     * @param value
     *     allowed object is
     *     {@link org.docx4j.vml.officedrawing.STTrueFalse }
     *     
     */
    public void setForcedash(org.docx4j.vml.officedrawing.STTrueFalse value) {
        this.forcedash = value;
    }

    /**
     * Relationship
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getId() {
        return id;
    }

    /**
     * Sets the value of the id property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setId(String value) {
        this.id = value;
    }

    /**
     * Gets the value of the insetpen property.
     * 
     * @return
     *     possible object is
     *     {@link org.docx4j.vml.STTrueFalse }
     *     
     */
    public org.docx4j.vml.STTrueFalse getInsetpen() {
        return insetpen;
    }

    /**
     * Sets the value of the insetpen property.
     * 
     * @param value
     *     allowed object is
     *     {@link org.docx4j.vml.STTrueFalse }
     *     
     */
    public void setInsetpen(org.docx4j.vml.STTrueFalse value) {
        this.insetpen = value;
    }

    /**
     * Relationship to Part
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getRelid() {
        return relid;
    }

    /**
     * Sets the value of the relid property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setRelid(String value) {
        this.relid = value;
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
