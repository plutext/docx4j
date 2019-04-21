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


package org.docx4j.vml.officedrawing;

import java.math.BigDecimal;
import javax.xml.bind.Unmarshaller;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlTransient;
import javax.xml.bind.annotation.XmlType;
import org.docx4j.vml.STExt;
import org.docx4j.vml.STFillType;
import org.docx4j.vml.STImageAspect;
import org.docx4j.vml.STStrokeArrowLength;
import org.docx4j.vml.STStrokeArrowType;
import org.docx4j.vml.STStrokeArrowWidth;
import org.docx4j.vml.STStrokeEndCap;
import org.docx4j.vml.STStrokeJoinStyle;
import org.docx4j.vml.STStrokeLineStyle;
import org.jvnet.jaxb2_commons.ppp.Child;


/**
 * <p>Java class for CT_StrokeChild complex type.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
 * 
 * <pre>
 * &lt;complexType name="CT_StrokeChild">
 *   &lt;complexContent>
 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
 *       &lt;attGroup ref="{urn:schemas-microsoft-com:vml}AG_Ext"/>
 *       &lt;attribute name="on" type="{urn:schemas-microsoft-com:office:office}ST_TrueFalse" />
 *       &lt;attribute name="weight" type="{http://www.w3.org/2001/XMLSchema}string" />
 *       &lt;attribute name="color" type="{urn:schemas-microsoft-com:office:office}ST_ColorType" />
 *       &lt;attribute name="color2" type="{urn:schemas-microsoft-com:office:office}ST_ColorType" />
 *       &lt;attribute name="opacity" type="{http://www.w3.org/2001/XMLSchema}string" />
 *       &lt;attribute name="linestyle" type="{urn:schemas-microsoft-com:vml}ST_StrokeLineStyle" />
 *       &lt;attribute name="miterlimit" type="{http://www.w3.org/2001/XMLSchema}decimal" />
 *       &lt;attribute name="joinstyle" type="{urn:schemas-microsoft-com:vml}ST_StrokeJoinStyle" />
 *       &lt;attribute name="endcap" type="{urn:schemas-microsoft-com:vml}ST_StrokeEndCap" />
 *       &lt;attribute name="dashstyle" type="{http://www.w3.org/2001/XMLSchema}string" />
 *       &lt;attribute name="insetpen" type="{urn:schemas-microsoft-com:office:office}ST_TrueFalse" />
 *       &lt;attribute name="filltype" type="{urn:schemas-microsoft-com:vml}ST_FillType" />
 *       &lt;attribute name="src" type="{http://www.w3.org/2001/XMLSchema}string" />
 *       &lt;attribute name="imageaspect" type="{urn:schemas-microsoft-com:vml}ST_ImageAspect" />
 *       &lt;attribute name="imagesize" type="{http://www.w3.org/2001/XMLSchema}string" />
 *       &lt;attribute name="imagealignshape" type="{urn:schemas-microsoft-com:office:office}ST_TrueFalse" />
 *       &lt;attribute name="startarrow" type="{urn:schemas-microsoft-com:vml}ST_StrokeArrowType" />
 *       &lt;attribute name="startarrowwidth" type="{urn:schemas-microsoft-com:vml}ST_StrokeArrowWidth" />
 *       &lt;attribute name="startarrowlength" type="{urn:schemas-microsoft-com:vml}ST_StrokeArrowLength" />
 *       &lt;attribute name="endarrow" type="{urn:schemas-microsoft-com:vml}ST_StrokeArrowType" />
 *       &lt;attribute name="endarrowwidth" type="{urn:schemas-microsoft-com:vml}ST_StrokeArrowWidth" />
 *       &lt;attribute name="endarrowlength" type="{urn:schemas-microsoft-com:vml}ST_StrokeArrowLength" />
 *       &lt;attribute ref="{urn:schemas-microsoft-com:office:office}href"/>
 *       &lt;attribute ref="{urn:schemas-microsoft-com:office:office}althref"/>
 *       &lt;attribute ref="{urn:schemas-microsoft-com:office:office}title"/>
 *       &lt;attribute ref="{urn:schemas-microsoft-com:office:office}forcedash"/>
 *     &lt;/restriction>
 *   &lt;/complexContent>
 * &lt;/complexType>
 * </pre>
 * 
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "CT_StrokeChild")
public class CTStrokeChild
    implements Child
{

    @XmlAttribute(name = "on")
    protected STTrueFalse on;
    @XmlAttribute(name = "weight")
    protected String weight;
    @XmlAttribute(name = "color")
    protected String color;
    @XmlAttribute(name = "color2")
    protected String color2;
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
    @XmlAttribute(name = "insetpen")
    protected STTrueFalse insetpen;
    @XmlAttribute(name = "filltype")
    protected STFillType filltype;
    @XmlAttribute(name = "src")
    protected String src;
    @XmlAttribute(name = "imageaspect")
    protected STImageAspect imageaspect;
    @XmlAttribute(name = "imagesize")
    protected String imagesize;
    @XmlAttribute(name = "imagealignshape")
    protected STTrueFalse imagealignshape;
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
    protected STTrueFalse forcedash;
    @XmlAttribute(name = "ext", namespace = "urn:schemas-microsoft-com:vml")
    protected STExt ext;
    @XmlTransient
    private Object parent;

    /**
     * Gets the value of the on property.
     * 
     * @return
     *     possible object is
     *     {@link STTrueFalse }
     *     
     */
    public STTrueFalse getOn() {
        return on;
    }

    /**
     * Sets the value of the on property.
     * 
     * @param value
     *     allowed object is
     *     {@link STTrueFalse }
     *     
     */
    public void setOn(STTrueFalse value) {
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
     * Gets the value of the insetpen property.
     * 
     * @return
     *     possible object is
     *     {@link STTrueFalse }
     *     
     */
    public STTrueFalse getInsetpen() {
        return insetpen;
    }

    /**
     * Sets the value of the insetpen property.
     * 
     * @param value
     *     allowed object is
     *     {@link STTrueFalse }
     *     
     */
    public void setInsetpen(STTrueFalse value) {
        this.insetpen = value;
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
     *     {@link STTrueFalse }
     *     
     */
    public STTrueFalse getImagealignshape() {
        return imagealignshape;
    }

    /**
     * Sets the value of the imagealignshape property.
     * 
     * @param value
     *     allowed object is
     *     {@link STTrueFalse }
     *     
     */
    public void setImagealignshape(STTrueFalse value) {
        this.imagealignshape = value;
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
     *     {@link STTrueFalse }
     *     
     */
    public STTrueFalse getForcedash() {
        return forcedash;
    }

    /**
     * Sets the value of the forcedash property.
     * 
     * @param value
     *     allowed object is
     *     {@link STTrueFalse }
     *     
     */
    public void setForcedash(STTrueFalse value) {
        this.forcedash = value;
    }

    /**
     * Gets the value of the ext property.
     * 
     * @return
     *     possible object is
     *     {@link STExt }
     *     
     */
    public STExt getExt() {
        return ext;
    }

    /**
     * Sets the value of the ext property.
     * 
     * @param value
     *     allowed object is
     *     {@link STExt }
     *     
     */
    public void setExt(STExt value) {
        this.ext = value;
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
