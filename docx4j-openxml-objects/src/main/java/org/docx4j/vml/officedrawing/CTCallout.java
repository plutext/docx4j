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

import javax.xml.bind.Unmarshaller;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlTransient;
import javax.xml.bind.annotation.XmlType;
import org.docx4j.vml.STExt;
import org.jvnet.jaxb2_commons.ppp.Child;


/**
 * <p>Java class for CT_Callout complex type.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
 * 
 * <pre>
 * &lt;complexType name="CT_Callout">
 *   &lt;complexContent>
 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
 *       &lt;attGroup ref="{urn:schemas-microsoft-com:vml}AG_Ext"/>
 *       &lt;attribute name="on" type="{urn:schemas-microsoft-com:office:office}ST_TrueFalse" />
 *       &lt;attribute name="type" type="{http://www.w3.org/2001/XMLSchema}string" />
 *       &lt;attribute name="gap" type="{http://www.w3.org/2001/XMLSchema}string" />
 *       &lt;attribute name="angle" type="{urn:schemas-microsoft-com:office:office}ST_Angle" />
 *       &lt;attribute name="dropauto" type="{urn:schemas-microsoft-com:office:office}ST_TrueFalse" />
 *       &lt;attribute name="drop" type="{urn:schemas-microsoft-com:office:office}ST_CalloutDrop" />
 *       &lt;attribute name="distance" type="{http://www.w3.org/2001/XMLSchema}string" />
 *       &lt;attribute name="lengthspecified" type="{urn:schemas-microsoft-com:office:office}ST_TrueFalse" default="f" />
 *       &lt;attribute name="length" type="{http://www.w3.org/2001/XMLSchema}string" />
 *       &lt;attribute name="accentbar" type="{urn:schemas-microsoft-com:office:office}ST_TrueFalse" />
 *       &lt;attribute name="textborder" type="{urn:schemas-microsoft-com:office:office}ST_TrueFalse" />
 *       &lt;attribute name="minusx" type="{urn:schemas-microsoft-com:office:office}ST_TrueFalse" />
 *       &lt;attribute name="minusy" type="{urn:schemas-microsoft-com:office:office}ST_TrueFalse" />
 *     &lt;/restriction>
 *   &lt;/complexContent>
 * &lt;/complexType>
 * </pre>
 * 
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "CT_Callout")
public class CTCallout implements Child
{

    @XmlAttribute(name = "on")
    protected STTrueFalse on;
    @XmlAttribute(name = "type")
    protected String type;
    @XmlAttribute(name = "gap")
    protected String gap;
    @XmlAttribute(name = "angle")
    protected String angle;
    @XmlAttribute(name = "dropauto")
    protected STTrueFalse dropauto;
    @XmlAttribute(name = "drop")
    protected String drop;
    @XmlAttribute(name = "distance")
    protected String distance;
    @XmlAttribute(name = "lengthspecified")
    protected STTrueFalse lengthspecified;
    @XmlAttribute(name = "length")
    protected String length;
    @XmlAttribute(name = "accentbar")
    protected STTrueFalse accentbar;
    @XmlAttribute(name = "textborder")
    protected STTrueFalse textborder;
    @XmlAttribute(name = "minusx")
    protected STTrueFalse minusx;
    @XmlAttribute(name = "minusy")
    protected STTrueFalse minusy;
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
     * Gets the value of the type property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getType() {
        return type;
    }

    /**
     * Sets the value of the type property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setType(String value) {
        this.type = value;
    }

    /**
     * Gets the value of the gap property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getGap() {
        return gap;
    }

    /**
     * Sets the value of the gap property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setGap(String value) {
        this.gap = value;
    }

    /**
     * Gets the value of the angle property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getAngle() {
        return angle;
    }

    /**
     * Sets the value of the angle property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setAngle(String value) {
        this.angle = value;
    }

    /**
     * Gets the value of the dropauto property.
     * 
     * @return
     *     possible object is
     *     {@link STTrueFalse }
     *     
     */
    public STTrueFalse getDropauto() {
        return dropauto;
    }

    /**
     * Sets the value of the dropauto property.
     * 
     * @param value
     *     allowed object is
     *     {@link STTrueFalse }
     *     
     */
    public void setDropauto(STTrueFalse value) {
        this.dropauto = value;
    }

    /**
     * Gets the value of the drop property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getDrop() {
        return drop;
    }

    /**
     * Sets the value of the drop property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setDrop(String value) {
        this.drop = value;
    }

    /**
     * Gets the value of the distance property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getDistance() {
        return distance;
    }

    /**
     * Sets the value of the distance property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setDistance(String value) {
        this.distance = value;
    }

    /**
     * Gets the value of the lengthspecified property.
     * 
     * @return
     *     possible object is
     *     {@link STTrueFalse }
     *     
     */
    public STTrueFalse getLengthspecified() {
        if (lengthspecified == null) {
            return STTrueFalse.F;
        } else {
            return lengthspecified;
        }
    }

    /**
     * Sets the value of the lengthspecified property.
     * 
     * @param value
     *     allowed object is
     *     {@link STTrueFalse }
     *     
     */
    public void setLengthspecified(STTrueFalse value) {
        this.lengthspecified = value;
    }

    /**
     * Gets the value of the length property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getLength() {
        return length;
    }

    /**
     * Sets the value of the length property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setLength(String value) {
        this.length = value;
    }

    /**
     * Gets the value of the accentbar property.
     * 
     * @return
     *     possible object is
     *     {@link STTrueFalse }
     *     
     */
    public STTrueFalse getAccentbar() {
        return accentbar;
    }

    /**
     * Sets the value of the accentbar property.
     * 
     * @param value
     *     allowed object is
     *     {@link STTrueFalse }
     *     
     */
    public void setAccentbar(STTrueFalse value) {
        this.accentbar = value;
    }

    /**
     * Gets the value of the textborder property.
     * 
     * @return
     *     possible object is
     *     {@link STTrueFalse }
     *     
     */
    public STTrueFalse getTextborder() {
        return textborder;
    }

    /**
     * Sets the value of the textborder property.
     * 
     * @param value
     *     allowed object is
     *     {@link STTrueFalse }
     *     
     */
    public void setTextborder(STTrueFalse value) {
        this.textborder = value;
    }

    /**
     * Gets the value of the minusx property.
     * 
     * @return
     *     possible object is
     *     {@link STTrueFalse }
     *     
     */
    public STTrueFalse getMinusx() {
        return minusx;
    }

    /**
     * Sets the value of the minusx property.
     * 
     * @param value
     *     allowed object is
     *     {@link STTrueFalse }
     *     
     */
    public void setMinusx(STTrueFalse value) {
        this.minusx = value;
    }

    /**
     * Gets the value of the minusy property.
     * 
     * @return
     *     possible object is
     *     {@link STTrueFalse }
     *     
     */
    public STTrueFalse getMinusy() {
        return minusy;
    }

    /**
     * Sets the value of the minusy property.
     * 
     * @param value
     *     allowed object is
     *     {@link STTrueFalse }
     *     
     */
    public void setMinusy(STTrueFalse value) {
        this.minusy = value;
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
