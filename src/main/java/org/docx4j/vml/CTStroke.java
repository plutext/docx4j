/*
 *  Copyright 2007-2009, Plutext Pty Ltd.
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
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import javax.xml.bind.Unmarshaller;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAnyAttribute;
import javax.xml.bind.annotation.XmlAnyElement;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlTransient;
import javax.xml.bind.annotation.XmlType;
import javax.xml.namespace.QName;
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
 *         &lt;any/>
 *       &lt;/sequence>
 *       &lt;attribute name="id" type="{http://www.w3.org/2001/XMLSchema}string" />
 *       &lt;attribute name="on" type="{urn:schemas-microsoft-com:vml}ST_TrueFalse" />
 *       &lt;attribute name="weight" type="{http://www.w3.org/2001/XMLSchema}string" />
 *       &lt;attribute name="color" type="{http://www.w3.org/2001/XMLSchema}string" />
 *       &lt;attribute name="opacity" type="{http://www.w3.org/2001/XMLSchema}string" />
 *       &lt;attribute name="linestyle" type="{urn:schemas-microsoft-com:vml}ST_StrokeLineStyle" default="single" />
 *       &lt;attribute name="miterlimit" type="{http://www.w3.org/2001/XMLSchema}decimal" />
 *       &lt;attribute name="joinstyle" type="{urn:schemas-microsoft-com:vml}ST_StrokeJoinStyle" default="round" />
 *       &lt;attribute name="endcap" type="{urn:schemas-microsoft-com:vml}ST_StrokeEndCap" default="flat" />
 *       &lt;attribute name="dashstyle" type="{http://www.w3.org/2001/XMLSchema}string" default="solid" />
 *       &lt;attribute name="filltype" type="{urn:schemas-microsoft-com:vml}ST_FillType" default="solid" />
 *       &lt;attribute name="src" type="{http://www.w3.org/2001/XMLSchema}string" />
 *       &lt;attribute name="imageaspect" type="{urn:schemas-microsoft-com:vml}ST_ImageAspect" default="ignore" />
 *       &lt;attribute name="imagesize" type="{http://www.w3.org/2001/XMLSchema}string" default="auto" />
 *       &lt;attribute name="imagealignshape" type="{urn:schemas-microsoft-com:vml}ST_TrueFalse" />
 *       &lt;attribute name="color2" type="{http://www.w3.org/2001/XMLSchema}string" />
 *       &lt;attribute name="startarrow" type="{urn:schemas-microsoft-com:vml}ST_ArrowType" default="none" />
 *       &lt;attribute name="startarrowwidth" type="{urn:schemas-microsoft-com:vml}ST_ArrowWidth" default="medium" />
 *       &lt;attribute name="startarrowlength" type="{urn:schemas-microsoft-com:vml}ST_StrokeArrowLength" default="medium" />
 *       &lt;attribute name="endarrow" type="{urn:schemas-microsoft-com:vml}ST_ArrowType" />
 *       &lt;attribute name="endarrowwidth" type="{urn:schemas-microsoft-com:vml}ST_ArrowWidth" />
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
@XmlType(name = "CT_Stroke", propOrder = {
    "any"
})
public class CTStroke
    implements Child
{

    @XmlAnyElement(lax = true)
    protected List<Object> any;
    @XmlAttribute
    protected String id;
    @XmlAttribute
    protected String on;
    @XmlAttribute
    protected String weight;
    @XmlAttribute
    protected String color;
    @XmlAttribute
    protected String opacity;
    @XmlAttribute
    protected STStrokeLineStyle linestyle;
    @XmlAttribute
    protected BigDecimal miterlimit;
    @XmlAttribute
    protected STStrokeJoinStyle joinstyle;
    @XmlAttribute
    protected STStrokeEndCap endcap;
    @XmlAttribute
    protected String dashstyle;
    @XmlAttribute
    protected STFillType filltype;
    @XmlAttribute
    protected String src;
    @XmlAttribute
    protected STImageAspect imageaspect;
    @XmlAttribute
    protected String imagesize;
    @XmlAttribute
    protected String imagealignshape;
    @XmlAttribute
    protected String color2;
    @XmlAttribute
    protected STArrowType startarrow;
    @XmlAttribute
    protected STArrowWidth startarrowwidth;
    @XmlAttribute
    protected STStrokeArrowLength startarrowlength;
    @XmlAttribute
    protected STArrowType endarrow;
    @XmlAttribute
    protected STArrowWidth endarrowwidth;
    @XmlAttribute
    protected STStrokeArrowLength endarrowlength;
    @XmlAttribute(namespace = "urn:schemas-microsoft-com:office:office")
    protected String href;
    @XmlAttribute(namespace = "urn:schemas-microsoft-com:office:office")
    protected String althref;
    @XmlAttribute(namespace = "urn:schemas-microsoft-com:office:office")
    protected String title;
    @XmlAttribute(namespace = "urn:schemas-microsoft-com:office:office")
    protected String forcedash;
    @XmlAnyAttribute
    private Map<QName, String> otherAttributes = new HashMap<QName, String>();
    @XmlTransient
    private Object parent;

    /**
     * Gets the value of the any property.
     * 
     * <p>
     * This accessor method returns a reference to the live list,
     * not a snapshot. Therefore any modification you make to the
     * returned list will be present inside the JAXB object.
     * This is why there is not a <CODE>set</CODE> method for the any property.
     * 
     * <p>
     * For example, to add a new item, do as follows:
     * <pre>
     *    getAny().add(newItem);
     * </pre>
     * 
     * 
     * <p>
     * Objects of the following type(s) are allowed in the list
     * {@link Object }
     * 
     * 
     */
    public List<Object> getAny() {
        if (any == null) {
            any = new ArrayList<Object>();
        }
        return this.any;
    }

    /**
     * Gets the value of the id property.
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
     * Gets the value of the on property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getOn() {
        return on;
    }

    /**
     * Sets the value of the on property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setOn(String value) {
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
        if (linestyle == null) {
            return STStrokeLineStyle.SINGLE;
        } else {
            return linestyle;
        }
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
    public BigDecimal getMiterlimit() {
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
    public void setMiterlimit(BigDecimal value) {
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
        if (joinstyle == null) {
            return STStrokeJoinStyle.ROUND;
        } else {
            return joinstyle;
        }
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
        if (endcap == null) {
            return STStrokeEndCap.FLAT;
        } else {
            return endcap;
        }
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
        if (dashstyle == null) {
            return "solid";
        } else {
            return dashstyle;
        }
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
        if (filltype == null) {
            return STFillType.SOLID;
        } else {
            return filltype;
        }
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
        if (imageaspect == null) {
            return STImageAspect.IGNORE;
        } else {
            return imageaspect;
        }
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
        if (imagesize == null) {
            return "auto";
        } else {
            return imagesize;
        }
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
     *     {@link String }
     *     
     */
    public String getImagealignshape() {
        return imagealignshape;
    }

    /**
     * Sets the value of the imagealignshape property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setImagealignshape(String value) {
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
     *     {@link STArrowType }
     *     
     */
    public STArrowType getStartarrow() {
        if (startarrow == null) {
            return STArrowType.NONE;
        } else {
            return startarrow;
        }
    }

    /**
     * Sets the value of the startarrow property.
     * 
     * @param value
     *     allowed object is
     *     {@link STArrowType }
     *     
     */
    public void setStartarrow(STArrowType value) {
        this.startarrow = value;
    }

    /**
     * Gets the value of the startarrowwidth property.
     * 
     * @return
     *     possible object is
     *     {@link STArrowWidth }
     *     
     */
    public STArrowWidth getStartarrowwidth() {
        if (startarrowwidth == null) {
            return STArrowWidth.MEDIUM;
        } else {
            return startarrowwidth;
        }
    }

    /**
     * Sets the value of the startarrowwidth property.
     * 
     * @param value
     *     allowed object is
     *     {@link STArrowWidth }
     *     
     */
    public void setStartarrowwidth(STArrowWidth value) {
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
        if (startarrowlength == null) {
            return STStrokeArrowLength.MEDIUM;
        } else {
            return startarrowlength;
        }
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
     *     {@link STArrowType }
     *     
     */
    public STArrowType getEndarrow() {
        return endarrow;
    }

    /**
     * Sets the value of the endarrow property.
     * 
     * @param value
     *     allowed object is
     *     {@link STArrowType }
     *     
     */
    public void setEndarrow(STArrowType value) {
        this.endarrow = value;
    }

    /**
     * Gets the value of the endarrowwidth property.
     * 
     * @return
     *     possible object is
     *     {@link STArrowWidth }
     *     
     */
    public STArrowWidth getEndarrowwidth() {
        return endarrowwidth;
    }

    /**
     * Sets the value of the endarrowwidth property.
     * 
     * @param value
     *     allowed object is
     *     {@link STArrowWidth }
     *     
     */
    public void setEndarrowwidth(STArrowWidth value) {
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
     * Gets the value of the href property.
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
     * Gets the value of the althref property.
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
     * Gets the value of the title property.
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
     * Gets the value of the forcedash property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getForcedash() {
        return forcedash;
    }

    /**
     * Sets the value of the forcedash property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setForcedash(String value) {
        this.forcedash = value;
    }

    /**
     * Gets a map that contains attributes that aren't bound to any typed property on this class.
     * 
     * <p>
     * the map is keyed by the name of the attribute and 
     * the value is the string value of the attribute.
     * 
     * the map returned by this method is live, and you can add new attribute
     * by updating the map directly. Because of this design, there's no setter.
     * 
     * 
     * @return
     *     always non-null
     */
    public Map<QName, String> getOtherAttributes() {
        return otherAttributes;
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
