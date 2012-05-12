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

import javax.xml.bind.Unmarshaller;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlTransient;
import javax.xml.bind.annotation.XmlType;
import org.docx4j.vml.officedrawing.STConnectType;
import org.jvnet.jaxb2_commons.ppp.Child;


/**
 * <p>Java class for CT_Path complex type.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
 * 
 * <pre>
 * &lt;complexType name="CT_Path">
 *   &lt;complexContent>
 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
 *       &lt;attGroup ref="{urn:schemas-microsoft-com:vml}AG_Id"/>
 *       &lt;attribute name="v" type="{http://www.w3.org/2001/XMLSchema}string" />
 *       &lt;attribute name="limo" type="{http://www.w3.org/2001/XMLSchema}string" />
 *       &lt;attribute name="textboxrect" type="{http://www.w3.org/2001/XMLSchema}string" />
 *       &lt;attribute name="fillok" type="{urn:schemas-microsoft-com:vml}ST_TrueFalse" />
 *       &lt;attribute name="strokeok" type="{urn:schemas-microsoft-com:vml}ST_TrueFalse" />
 *       &lt;attribute name="shadowok" type="{urn:schemas-microsoft-com:vml}ST_TrueFalse" />
 *       &lt;attribute name="arrowok" type="{urn:schemas-microsoft-com:vml}ST_TrueFalse" />
 *       &lt;attribute name="gradientshapeok" type="{urn:schemas-microsoft-com:vml}ST_TrueFalse" />
 *       &lt;attribute name="textpathok" type="{urn:schemas-microsoft-com:vml}ST_TrueFalse" />
 *       &lt;attribute name="insetpenok" type="{urn:schemas-microsoft-com:vml}ST_TrueFalse" />
 *       &lt;attribute ref="{urn:schemas-microsoft-com:office:office}connecttype"/>
 *       &lt;attribute ref="{urn:schemas-microsoft-com:office:office}connectlocs"/>
 *       &lt;attribute ref="{urn:schemas-microsoft-com:office:office}connectangles"/>
 *       &lt;attribute ref="{urn:schemas-microsoft-com:office:office}extrusionok"/>
 *     &lt;/restriction>
 *   &lt;/complexContent>
 * &lt;/complexType>
 * </pre>
 * 
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "CT_Path")
public class CTPath implements Child
{

    @XmlAttribute(name = "v")
    protected String v;
    @XmlAttribute(name = "limo")
    protected String limo;
    @XmlAttribute(name = "textboxrect")
    protected String textboxrect;
    @XmlAttribute(name = "fillok")
    protected org.docx4j.vml.STTrueFalse fillok;
    @XmlAttribute(name = "strokeok")
    protected org.docx4j.vml.STTrueFalse strokeok;
    @XmlAttribute(name = "shadowok")
    protected org.docx4j.vml.STTrueFalse shadowok;
    @XmlAttribute(name = "arrowok")
    protected org.docx4j.vml.STTrueFalse arrowok;
    @XmlAttribute(name = "gradientshapeok")
    protected org.docx4j.vml.STTrueFalse gradientshapeok;
    @XmlAttribute(name = "textpathok")
    protected org.docx4j.vml.STTrueFalse textpathok;
    @XmlAttribute(name = "insetpenok")
    protected org.docx4j.vml.STTrueFalse insetpenok;
    @XmlAttribute(name = "connecttype", namespace = "urn:schemas-microsoft-com:office:office")
    protected STConnectType connecttype;
    @XmlAttribute(name = "connectlocs", namespace = "urn:schemas-microsoft-com:office:office")
    protected String connectlocs;
    @XmlAttribute(name = "connectangles", namespace = "urn:schemas-microsoft-com:office:office")
    protected String connectangles;
    @XmlAttribute(name = "extrusionok", namespace = "urn:schemas-microsoft-com:office:office")
    protected org.docx4j.vml.officedrawing.STTrueFalse extrusionok;
    @XmlAttribute(name = "id")
    protected String vmlId;
    @XmlTransient
    private Object parent;

    /**
     * Gets the value of the v property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getV() {
        return v;
    }

    /**
     * Sets the value of the v property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setV(String value) {
        this.v = value;
    }

    /**
     * Gets the value of the limo property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getLimo() {
        return limo;
    }

    /**
     * Sets the value of the limo property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setLimo(String value) {
        this.limo = value;
    }

    /**
     * Gets the value of the textboxrect property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getTextboxrect() {
        return textboxrect;
    }

    /**
     * Sets the value of the textboxrect property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setTextboxrect(String value) {
        this.textboxrect = value;
    }

    /**
     * Gets the value of the fillok property.
     * 
     * @return
     *     possible object is
     *     {@link org.docx4j.vml.STTrueFalse }
     *     
     */
    public org.docx4j.vml.STTrueFalse getFillok() {
        return fillok;
    }

    /**
     * Sets the value of the fillok property.
     * 
     * @param value
     *     allowed object is
     *     {@link org.docx4j.vml.STTrueFalse }
     *     
     */
    public void setFillok(org.docx4j.vml.STTrueFalse value) {
        this.fillok = value;
    }

    /**
     * Gets the value of the strokeok property.
     * 
     * @return
     *     possible object is
     *     {@link org.docx4j.vml.STTrueFalse }
     *     
     */
    public org.docx4j.vml.STTrueFalse getStrokeok() {
        return strokeok;
    }

    /**
     * Sets the value of the strokeok property.
     * 
     * @param value
     *     allowed object is
     *     {@link org.docx4j.vml.STTrueFalse }
     *     
     */
    public void setStrokeok(org.docx4j.vml.STTrueFalse value) {
        this.strokeok = value;
    }

    /**
     * Gets the value of the shadowok property.
     * 
     * @return
     *     possible object is
     *     {@link org.docx4j.vml.STTrueFalse }
     *     
     */
    public org.docx4j.vml.STTrueFalse getShadowok() {
        return shadowok;
    }

    /**
     * Sets the value of the shadowok property.
     * 
     * @param value
     *     allowed object is
     *     {@link org.docx4j.vml.STTrueFalse }
     *     
     */
    public void setShadowok(org.docx4j.vml.STTrueFalse value) {
        this.shadowok = value;
    }

    /**
     * Gets the value of the arrowok property.
     * 
     * @return
     *     possible object is
     *     {@link org.docx4j.vml.STTrueFalse }
     *     
     */
    public org.docx4j.vml.STTrueFalse getArrowok() {
        return arrowok;
    }

    /**
     * Sets the value of the arrowok property.
     * 
     * @param value
     *     allowed object is
     *     {@link org.docx4j.vml.STTrueFalse }
     *     
     */
    public void setArrowok(org.docx4j.vml.STTrueFalse value) {
        this.arrowok = value;
    }

    /**
     * Gets the value of the gradientshapeok property.
     * 
     * @return
     *     possible object is
     *     {@link org.docx4j.vml.STTrueFalse }
     *     
     */
    public org.docx4j.vml.STTrueFalse getGradientshapeok() {
        return gradientshapeok;
    }

    /**
     * Sets the value of the gradientshapeok property.
     * 
     * @param value
     *     allowed object is
     *     {@link org.docx4j.vml.STTrueFalse }
     *     
     */
    public void setGradientshapeok(org.docx4j.vml.STTrueFalse value) {
        this.gradientshapeok = value;
    }

    /**
     * Gets the value of the textpathok property.
     * 
     * @return
     *     possible object is
     *     {@link org.docx4j.vml.STTrueFalse }
     *     
     */
    public org.docx4j.vml.STTrueFalse getTextpathok() {
        return textpathok;
    }

    /**
     * Sets the value of the textpathok property.
     * 
     * @param value
     *     allowed object is
     *     {@link org.docx4j.vml.STTrueFalse }
     *     
     */
    public void setTextpathok(org.docx4j.vml.STTrueFalse value) {
        this.textpathok = value;
    }

    /**
     * Gets the value of the insetpenok property.
     * 
     * @return
     *     possible object is
     *     {@link org.docx4j.vml.STTrueFalse }
     *     
     */
    public org.docx4j.vml.STTrueFalse getInsetpenok() {
        return insetpenok;
    }

    /**
     * Sets the value of the insetpenok property.
     * 
     * @param value
     *     allowed object is
     *     {@link org.docx4j.vml.STTrueFalse }
     *     
     */
    public void setInsetpenok(org.docx4j.vml.STTrueFalse value) {
        this.insetpenok = value;
    }

    /**
     * Connection Point Type
     * 
     * @return
     *     possible object is
     *     {@link STConnectType }
     *     
     */
    public STConnectType getConnecttype() {
        return connecttype;
    }

    /**
     * Sets the value of the connecttype property.
     * 
     * @param value
     *     allowed object is
     *     {@link STConnectType }
     *     
     */
    public void setConnecttype(STConnectType value) {
        this.connecttype = value;
    }

    /**
     * Connection Points
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getConnectlocs() {
        return connectlocs;
    }

    /**
     * Sets the value of the connectlocs property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setConnectlocs(String value) {
        this.connectlocs = value;
    }

    /**
     * Connection Point Connect Angles
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getConnectangles() {
        return connectangles;
    }

    /**
     * Sets the value of the connectangles property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setConnectangles(String value) {
        this.connectangles = value;
    }

    /**
     * Extrusion Toggle
     * 
     * @return
     *     possible object is
     *     {@link org.docx4j.vml.officedrawing.STTrueFalse }
     *     
     */
    public org.docx4j.vml.officedrawing.STTrueFalse getExtrusionok() {
        return extrusionok;
    }

    /**
     * Sets the value of the extrusionok property.
     * 
     * @param value
     *     allowed object is
     *     {@link org.docx4j.vml.officedrawing.STTrueFalse }
     *     
     */
    public void setExtrusionok(org.docx4j.vml.officedrawing.STTrueFalse value) {
        this.extrusionok = value;
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
