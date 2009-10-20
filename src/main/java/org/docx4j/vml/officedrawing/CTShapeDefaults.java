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

import java.math.BigInteger;
import javax.xml.bind.Unmarshaller;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlTransient;
import javax.xml.bind.annotation.XmlType;
import org.docx4j.vml.CTFill;
import org.docx4j.vml.CTImageData;
import org.docx4j.vml.CTShadow;
import org.docx4j.vml.CTStroke;
import org.docx4j.vml.CTTextPath;
import org.docx4j.vml.CTTextbox;
import org.docx4j.vml.STExt;
import org.jvnet.jaxb2_commons.ppp.Child;


/**
 * <p>Java class for CT_ShapeDefaults complex type.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
 * 
 * <pre>
 * &lt;complexType name="CT_ShapeDefaults">
 *   &lt;complexContent>
 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
 *       &lt;sequence>
 *         &lt;element ref="{urn:schemas-microsoft-com:vml}fill" minOccurs="0"/>
 *         &lt;element ref="{urn:schemas-microsoft-com:vml}stroke" minOccurs="0"/>
 *         &lt;element ref="{urn:schemas-microsoft-com:vml}textbox" minOccurs="0"/>
 *         &lt;element ref="{urn:schemas-microsoft-com:vml}shadow" minOccurs="0"/>
 *         &lt;element ref="{urn:schemas-microsoft-com:vml}textpath" minOccurs="0"/>
 *         &lt;element ref="{urn:schemas-microsoft-com:vml}imagedata" minOccurs="0"/>
 *         &lt;element ref="{urn:schemas-microsoft-com:office:office}skew" minOccurs="0"/>
 *         &lt;element ref="{urn:schemas-microsoft-com:office:office}extrusion" minOccurs="0"/>
 *         &lt;element ref="{urn:schemas-microsoft-com:office:office}callout" minOccurs="0"/>
 *         &lt;element ref="{urn:schemas-microsoft-com:office:office}lock" minOccurs="0"/>
 *         &lt;element name="colormru" type="{urn:schemas-microsoft-com:office:office}CT_ColorMRU" minOccurs="0"/>
 *         &lt;element name="colormenu" type="{urn:schemas-microsoft-com:office:office}CT_ColorMenu" minOccurs="0"/>
 *       &lt;/sequence>
 *       &lt;attribute name="spidmax" type="{http://www.w3.org/2001/XMLSchema}integer" />
 *       &lt;attribute name="style" type="{http://www.w3.org/2001/XMLSchema}string" />
 *       &lt;attribute name="fill" type="{urn:schemas-microsoft-com:office:office}ST_TrueFalse" />
 *       &lt;attribute name="fillcolor" type="{http://www.w3.org/2001/XMLSchema}string" />
 *       &lt;attribute ref="{urn:schemas-microsoft-com:vml}ext"/>
 *     &lt;/restriction>
 *   &lt;/complexContent>
 * &lt;/complexType>
 * </pre>
 * 
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(namespace = "urn:schemas-microsoft-com:office:office",
		 name = "CT_ShapeDefaults", propOrder = {
    "fill",
    "stroke",
    "textbox",
    "shadow",
    "textpath",
    "imagedata",
    "skew",
    "extrusion",
    "callout",
    "lock",
    "colormru",
    "colormenu"
})
public class CTShapeDefaults
    implements Child
{

    @XmlElement(namespace = "urn:schemas-microsoft-com:vml")
    protected CTFill fill;
    @XmlElement(namespace = "urn:schemas-microsoft-com:vml")
    protected CTStroke stroke;
    @XmlElement(namespace = "urn:schemas-microsoft-com:vml")
    protected CTTextbox textbox;
    @XmlElement(namespace = "urn:schemas-microsoft-com:vml")
    protected CTShadow shadow;
    @XmlElement(namespace = "urn:schemas-microsoft-com:vml")
    protected CTTextPath textpath;
    @XmlElement(namespace = "urn:schemas-microsoft-com:vml")
    protected CTImageData imagedata;
    protected CTSkew skew;
    protected CTExtrusion extrusion;
    protected CTCallout callout;
    protected CTLock lock;
    protected CTColorMRU colormru;
    protected CTColorMenu colormenu;
    @XmlAttribute
    protected BigInteger spidmax;
    @XmlAttribute
    protected String style;
    @XmlAttribute(name = "fill")
    protected String fillAttr;
    @XmlAttribute
    protected String fillcolor;
    @XmlAttribute(namespace = "urn:schemas-microsoft-com:vml")
    protected STExt ext;
    @XmlTransient
    private Object parent;

    /**
     * Gets the value of the fill property.
     * 
     * @return
     *     possible object is
     *     {@link CTFill }
     *     
     */
    public CTFill getFill() {
        return fill;
    }

    /**
     * Sets the value of the fill property.
     * 
     * @param value
     *     allowed object is
     *     {@link CTFill }
     *     
     */
    public void setFill(CTFill value) {
        this.fill = value;
    }

    /**
     * Gets the value of the stroke property.
     * 
     * @return
     *     possible object is
     *     {@link CTStroke }
     *     
     */
    public CTStroke getStroke() {
        return stroke;
    }

    /**
     * Sets the value of the stroke property.
     * 
     * @param value
     *     allowed object is
     *     {@link CTStroke }
     *     
     */
    public void setStroke(CTStroke value) {
        this.stroke = value;
    }

    /**
     * Gets the value of the textbox property.
     * 
     * @return
     *     possible object is
     *     {@link CTTextbox }
     *     
     */
    public CTTextbox getTextbox() {
        return textbox;
    }

    /**
     * Sets the value of the textbox property.
     * 
     * @param value
     *     allowed object is
     *     {@link CTTextbox }
     *     
     */
    public void setTextbox(CTTextbox value) {
        this.textbox = value;
    }

    /**
     * Gets the value of the shadow property.
     * 
     * @return
     *     possible object is
     *     {@link CTShadow }
     *     
     */
    public CTShadow getShadow() {
        return shadow;
    }

    /**
     * Sets the value of the shadow property.
     * 
     * @param value
     *     allowed object is
     *     {@link CTShadow }
     *     
     */
    public void setShadow(CTShadow value) {
        this.shadow = value;
    }

    /**
     * Gets the value of the textpath property.
     * 
     * @return
     *     possible object is
     *     {@link CTTextPath }
     *     
     */
    public CTTextPath getTextpath() {
        return textpath;
    }

    /**
     * Sets the value of the textpath property.
     * 
     * @param value
     *     allowed object is
     *     {@link CTTextPath }
     *     
     */
    public void setTextpath(CTTextPath value) {
        this.textpath = value;
    }

    /**
     * Gets the value of the imagedata property.
     * 
     * @return
     *     possible object is
     *     {@link CTImageData }
     *     
     */
    public CTImageData getImagedata() {
        return imagedata;
    }

    /**
     * Sets the value of the imagedata property.
     * 
     * @param value
     *     allowed object is
     *     {@link CTImageData }
     *     
     */
    public void setImagedata(CTImageData value) {
        this.imagedata = value;
    }

    /**
     * Gets the value of the skew property.
     * 
     * @return
     *     possible object is
     *     {@link CTSkew }
     *     
     */
    public CTSkew getSkew() {
        return skew;
    }

    /**
     * Sets the value of the skew property.
     * 
     * @param value
     *     allowed object is
     *     {@link CTSkew }
     *     
     */
    public void setSkew(CTSkew value) {
        this.skew = value;
    }

    /**
     * Gets the value of the extrusion property.
     * 
     * @return
     *     possible object is
     *     {@link CTExtrusion }
     *     
     */
    public CTExtrusion getExtrusion() {
        return extrusion;
    }

    /**
     * Sets the value of the extrusion property.
     * 
     * @param value
     *     allowed object is
     *     {@link CTExtrusion }
     *     
     */
    public void setExtrusion(CTExtrusion value) {
        this.extrusion = value;
    }

    /**
     * Gets the value of the callout property.
     * 
     * @return
     *     possible object is
     *     {@link CTCallout }
     *     
     */
    public CTCallout getCallout() {
        return callout;
    }

    /**
     * Sets the value of the callout property.
     * 
     * @param value
     *     allowed object is
     *     {@link CTCallout }
     *     
     */
    public void setCallout(CTCallout value) {
        this.callout = value;
    }

    /**
     * Gets the value of the lock property.
     * 
     * @return
     *     possible object is
     *     {@link CTLock }
     *     
     */
    public CTLock getLock() {
        return lock;
    }

    /**
     * Sets the value of the lock property.
     * 
     * @param value
     *     allowed object is
     *     {@link CTLock }
     *     
     */
    public void setLock(CTLock value) {
        this.lock = value;
    }

    /**
     * Gets the value of the colormru property.
     * 
     * @return
     *     possible object is
     *     {@link CTColorMRU }
     *     
     */
    public CTColorMRU getColormru() {
        return colormru;
    }

    /**
     * Sets the value of the colormru property.
     * 
     * @param value
     *     allowed object is
     *     {@link CTColorMRU }
     *     
     */
    public void setColormru(CTColorMRU value) {
        this.colormru = value;
    }

    /**
     * Gets the value of the colormenu property.
     * 
     * @return
     *     possible object is
     *     {@link CTColorMenu }
     *     
     */
    public CTColorMenu getColormenu() {
        return colormenu;
    }

    /**
     * Sets the value of the colormenu property.
     * 
     * @param value
     *     allowed object is
     *     {@link CTColorMenu }
     *     
     */
    public void setColormenu(CTColorMenu value) {
        this.colormenu = value;
    }

    /**
     * Gets the value of the spidmax property.
     * 
     * @return
     *     possible object is
     *     {@link BigInteger }
     *     
     */
    public BigInteger getSpidmax() {
        return spidmax;
    }

    /**
     * Sets the value of the spidmax property.
     * 
     * @param value
     *     allowed object is
     *     {@link BigInteger }
     *     
     */
    public void setSpidmax(BigInteger value) {
        this.spidmax = value;
    }

    /**
     * Gets the value of the style property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getStyle() {
        return style;
    }

    /**
     * Sets the value of the style property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setStyle(String value) {
        this.style = value;
    }

    /**
     * Gets the value of the fillAttr property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getFillAttr() {
        return fillAttr;
    }

    /**
     * Sets the value of the fillAttr property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setFillAttr(String value) {
        this.fillAttr = value;
    }

    /**
     * Gets the value of the fillcolor property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getFillcolor() {
        return fillcolor;
    }

    /**
     * Sets the value of the fillcolor property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setFillcolor(String value) {
        this.fillcolor = value;
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
