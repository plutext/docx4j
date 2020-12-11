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

import java.math.BigInteger;
import javax.xml.bind.Unmarshaller;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlTransient;
import javax.xml.bind.annotation.XmlType;
import org.docx4j.vml.CTFill;
import org.docx4j.vml.CTShadow;
import org.docx4j.vml.CTStroke;
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
 *       &lt;all minOccurs="0">
 *         &lt;element ref="{urn:schemas-microsoft-com:vml}fill" minOccurs="0"/>
 *         &lt;element ref="{urn:schemas-microsoft-com:vml}stroke" minOccurs="0"/>
 *         &lt;element ref="{urn:schemas-microsoft-com:vml}textbox" minOccurs="0"/>
 *         &lt;element ref="{urn:schemas-microsoft-com:vml}shadow" minOccurs="0"/>
 *         &lt;element ref="{urn:schemas-microsoft-com:office:office}skew" minOccurs="0"/>
 *         &lt;element ref="{urn:schemas-microsoft-com:office:office}extrusion" minOccurs="0"/>
 *         &lt;element ref="{urn:schemas-microsoft-com:office:office}callout" minOccurs="0"/>
 *         &lt;element ref="{urn:schemas-microsoft-com:office:office}lock" minOccurs="0"/>
 *         &lt;element name="colormru" type="{urn:schemas-microsoft-com:office:office}CT_ColorMru" minOccurs="0"/>
 *         &lt;element name="colormenu" type="{urn:schemas-microsoft-com:office:office}CT_ColorMenu" minOccurs="0"/>
 *       &lt;/all>
 *       &lt;attGroup ref="{urn:schemas-microsoft-com:vml}AG_Ext"/>
 *       &lt;attribute name="spidmax" type="{http://www.w3.org/2001/XMLSchema}integer" />
 *       &lt;attribute name="style" type="{http://www.w3.org/2001/XMLSchema}string" />
 *       &lt;attribute name="fill" type="{urn:schemas-microsoft-com:office:office}ST_TrueFalse" />
 *       &lt;attribute name="fillcolor" type="{urn:schemas-microsoft-com:office:office}ST_ColorType" />
 *       &lt;attribute name="stroke" type="{urn:schemas-microsoft-com:office:office}ST_TrueFalse" />
 *       &lt;attribute name="strokecolor" type="{urn:schemas-microsoft-com:office:office}ST_ColorType" />
 *       &lt;attribute name="allowincell" type="{urn:schemas-microsoft-com:office:office}ST_TrueFalse" />
 *     &lt;/restriction>
 *   &lt;/complexContent>
 * &lt;/complexType>
 * </pre>
 * 
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(namespace = "urn:schemas-microsoft-com:office:office", name = "CT_ShapeDefaults", propOrder = {

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
    protected CTSkew skew;
    protected CTExtrusion extrusion;
    protected CTCallout callout;
    protected CTLock lock;
    protected CTColorMru colormru;
    protected CTColorMenu colormenu;
    @XmlAttribute(name = "spidmax")
    protected BigInteger spidmax;
    @XmlAttribute(name = "style")
    protected String style;
    @XmlAttribute(name = "fill")
    protected STTrueFalse fillToggle;
    @XmlAttribute(name = "fillcolor")
    protected String fillcolor;
    @XmlAttribute(name = "stroke")
    protected STTrueFalse strokeToggle;
    @XmlAttribute(name = "strokecolor")
    protected String strokecolor;
    @XmlAttribute(name = "allowincell", namespace = "urn:schemas-microsoft-com:office:office")
    protected STTrueFalse allowincell;
    @XmlAttribute(name = "ext", namespace = "urn:schemas-microsoft-com:vml")
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
     * Callout
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
     * Shape Protections
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
     *     {@link CTColorMru }
     *     
     */
    public CTColorMru getColormru() {
        return colormru;
    }

    /**
     * Sets the value of the colormru property.
     * 
     * @param value
     *     allowed object is
     *     {@link CTColorMru }
     *     
     */
    public void setColormru(CTColorMru value) {
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
     * Gets the value of the fillToggle property.
     * 
     * @return
     *     possible object is
     *     {@link STTrueFalse }
     *     
     */
    public STTrueFalse getFillToggle() {
        return fillToggle;
    }

    /**
     * Sets the value of the fillToggle property.
     * 
     * @param value
     *     allowed object is
     *     {@link STTrueFalse }
     *     
     */
    public void setFillToggle(STTrueFalse value) {
        this.fillToggle = value;
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
     * Gets the value of the strokeToggle property.
     * 
     * @return
     *     possible object is
     *     {@link STTrueFalse }
     *     
     */
    public STTrueFalse getStrokeToggle() {
        return strokeToggle;
    }

    /**
     * Sets the value of the strokeToggle property.
     * 
     * @param value
     *     allowed object is
     *     {@link STTrueFalse }
     *     
     */
    public void setStrokeToggle(STTrueFalse value) {
        this.strokeToggle = value;
    }

    /**
     * Gets the value of the strokecolor property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getStrokecolor() {
        return strokecolor;
    }

    /**
     * Sets the value of the strokecolor property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setStrokecolor(String value) {
        this.strokecolor = value;
    }

    /**
     * Gets the value of the allowincell property.
     * 
     * @return
     *     possible object is
     *     {@link STTrueFalse }
     *     
     */
    public STTrueFalse getAllowincell() {
        return allowincell;
    }

    /**
     * Sets the value of the allowincell property.
     * 
     * @param value
     *     allowed object is
     *     {@link STTrueFalse }
     *     
     */
    public void setAllowincell(STTrueFalse value) {
        this.allowincell = value;
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
