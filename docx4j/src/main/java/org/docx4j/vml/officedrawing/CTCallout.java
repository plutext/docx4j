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
 *       &lt;attribute ref="{urn:schemas-microsoft-com:vml}ext"/>
 *       &lt;attribute name="on" type="{urn:schemas-microsoft-com:office:office}ST_TrueFalse" />
 *       &lt;attribute name="type" type="{http://www.w3.org/2001/XMLSchema}string" />
 *       &lt;attribute name="gap" type="{http://www.w3.org/2001/XMLSchema}string" />
 *       &lt;attribute name="angle" type="{urn:schemas-microsoft-com:office:office}ST_Angle" />
 *       &lt;attribute name="dropauto" type="{urn:schemas-microsoft-com:office:office}ST_TrueFalse" />
 *       &lt;attribute name="drop" type="{urn:schemas-microsoft-com:office:office}ST_CalloutDrop" />
 *       &lt;attribute name="distance" type="{http://www.w3.org/2001/XMLSchema}string" />
 *       &lt;attribute name="lengthspecified" type="{urn:schemas-microsoft-com:office:office}ST_TrueFalse" default="f" />
 *       &lt;attribute name="length" type="{http://www.w3.org/2001/XMLSchema}float" />
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
public class CTCallout
    implements Child
{

    @XmlAttribute(namespace = "urn:schemas-microsoft-com:vml")
    protected STExt ext;
    @XmlAttribute
    protected String on;
    @XmlAttribute
    protected String type;
    @XmlAttribute
    protected String gap;
    @XmlAttribute
    protected String angle;
    @XmlAttribute
    protected String dropauto;
    @XmlAttribute
    protected String drop;
    @XmlAttribute
    protected String distance;
    @XmlAttribute
    protected String lengthspecified;
    @XmlAttribute
    protected Float length;
    @XmlAttribute
    protected String accentbar;
    @XmlAttribute
    protected String textborder;
    @XmlAttribute
    protected String minusx;
    @XmlAttribute
    protected String minusy;
    @XmlTransient
    private Object parent;

    /**
     * Gets the value of the ext property.
     * 
     * @return
     *     possible object is
     *     {@link STExt }
     *     
     */
    public STExt getExt() {
        if (ext == null) {
            return STExt.VIEW;
        } else {
            return ext;
        }
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
     *     {@link String }
     *     
     */
    public String getDropauto() {
        return dropauto;
    }

    /**
     * Sets the value of the dropauto property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setDropauto(String value) {
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
     *     {@link String }
     *     
     */
    public String getLengthspecified() {
        if (lengthspecified == null) {
            return "f";
        } else {
            return lengthspecified;
        }
    }

    /**
     * Sets the value of the lengthspecified property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setLengthspecified(String value) {
        this.lengthspecified = value;
    }

    /**
     * Gets the value of the length property.
     * 
     * @return
     *     possible object is
     *     {@link Float }
     *     
     */
    public Float getLength() {
        return length;
    }

    /**
     * Sets the value of the length property.
     * 
     * @param value
     *     allowed object is
     *     {@link Float }
     *     
     */
    public void setLength(Float value) {
        this.length = value;
    }

    /**
     * Gets the value of the accentbar property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getAccentbar() {
        return accentbar;
    }

    /**
     * Sets the value of the accentbar property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setAccentbar(String value) {
        this.accentbar = value;
    }

    /**
     * Gets the value of the textborder property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getTextborder() {
        return textborder;
    }

    /**
     * Sets the value of the textborder property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setTextborder(String value) {
        this.textborder = value;
    }

    /**
     * Gets the value of the minusx property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getMinusx() {
        return minusx;
    }

    /**
     * Sets the value of the minusx property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setMinusx(String value) {
        this.minusx = value;
    }

    /**
     * Gets the value of the minusy property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getMinusy() {
        return minusy;
    }

    /**
     * Sets the value of the minusy property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setMinusy(String value) {
        this.minusy = value;
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
