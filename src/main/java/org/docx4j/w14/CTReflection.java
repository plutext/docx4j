/*
 *  Copyright 2013, Plutext Pty Ltd.
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


package org.docx4j.w14; 

import org.jvnet.jaxb2_commons.ppp.Child;

import javax.xml.bind.Unmarshaller;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlTransient;
import javax.xml.bind.annotation.XmlType;



/**
 * <p>Java class for CT_Reflection complex type.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
 * 
 * <pre>
 * &lt;complexType name="CT_Reflection">
 *   &lt;complexContent>
 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
 *       &lt;attribute name="blurRad" type="{http://schemas.openxmlformats.org/drawingml/2006/main}ST_PositiveCoordinate" />
 *       &lt;attribute name="stA" type="{http://schemas.openxmlformats.org/drawingml/2006/main}ST_PositiveFixedPercentage" />
 *       &lt;attribute name="stPos" type="{http://schemas.openxmlformats.org/drawingml/2006/main}ST_PositiveFixedPercentage" />
 *       &lt;attribute name="endA" type="{http://schemas.openxmlformats.org/drawingml/2006/main}ST_PositiveFixedPercentage" />
 *       &lt;attribute name="endPos" type="{http://schemas.openxmlformats.org/drawingml/2006/main}ST_PositiveFixedPercentage" />
 *       &lt;attribute name="dist" type="{http://schemas.openxmlformats.org/drawingml/2006/main}ST_PositiveCoordinate" />
 *       &lt;attribute name="dir" type="{http://schemas.openxmlformats.org/drawingml/2006/main}ST_PositiveFixedAngle" />
 *       &lt;attribute name="fadeDir" type="{http://schemas.openxmlformats.org/drawingml/2006/main}ST_PositiveFixedAngle" />
 *       &lt;attribute name="sx" type="{http://schemas.openxmlformats.org/drawingml/2006/main}ST_Percentage" />
 *       &lt;attribute name="sy" type="{http://schemas.openxmlformats.org/drawingml/2006/main}ST_Percentage" />
 *       &lt;attribute name="kx" type="{http://schemas.openxmlformats.org/drawingml/2006/main}ST_FixedAngle" />
 *       &lt;attribute name="ky" type="{http://schemas.openxmlformats.org/drawingml/2006/main}ST_FixedAngle" />
 *       &lt;attribute name="algn" type="{http://schemas.microsoft.com/office/word/2010/wordml}ST_RectAlignment" />
 *     &lt;/restriction>
 *   &lt;/complexContent>
 * &lt;/complexType>
 * </pre>
 * 
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "CT_Reflection")
public class CTReflection implements Child
{

    @XmlAttribute(name = "blurRad", namespace = "http://schemas.microsoft.com/office/word/2010/wordml")
    protected Long blurRad;
    @XmlAttribute(name = "stA", namespace = "http://schemas.microsoft.com/office/word/2010/wordml")
    protected Integer stA;
    @XmlAttribute(name = "stPos", namespace = "http://schemas.microsoft.com/office/word/2010/wordml")
    protected Integer stPos;
    @XmlAttribute(name = "endA", namespace = "http://schemas.microsoft.com/office/word/2010/wordml")
    protected Integer endA;
    @XmlAttribute(name = "endPos", namespace = "http://schemas.microsoft.com/office/word/2010/wordml")
    protected Integer endPos;
    @XmlAttribute(name = "dist", namespace = "http://schemas.microsoft.com/office/word/2010/wordml")
    protected Long dist;
    @XmlAttribute(name = "dir", namespace = "http://schemas.microsoft.com/office/word/2010/wordml")
    protected Integer dir;
    @XmlAttribute(name = "fadeDir", namespace = "http://schemas.microsoft.com/office/word/2010/wordml")
    protected Integer fadeDir;
    @XmlAttribute(name = "sx", namespace = "http://schemas.microsoft.com/office/word/2010/wordml")
    protected Integer sx;
    @XmlAttribute(name = "sy", namespace = "http://schemas.microsoft.com/office/word/2010/wordml")
    protected Integer sy;
    @XmlAttribute(name = "kx", namespace = "http://schemas.microsoft.com/office/word/2010/wordml")
    protected Integer kx;
    @XmlAttribute(name = "ky", namespace = "http://schemas.microsoft.com/office/word/2010/wordml")
    protected Integer ky;
    @XmlAttribute(name = "algn", namespace = "http://schemas.microsoft.com/office/word/2010/wordml")
    protected STRectAlignment algn;
    @XmlTransient
    private Object parent;

    /**
     * Gets the value of the blurRad property.
     * 
     * @return
     *     possible object is
     *     {@link Long }
     *     
     */
    public Long getBlurRad() {
        return blurRad;
    }

    /**
     * Sets the value of the blurRad property.
     * 
     * @param value
     *     allowed object is
     *     {@link Long }
     *     
     */
    public void setBlurRad(Long value) {
        this.blurRad = value;
    }

    /**
     * Gets the value of the stA property.
     * 
     * @return
     *     possible object is
     *     {@link Integer }
     *     
     */
    public Integer getStA() {
        return stA;
    }

    /**
     * Sets the value of the stA property.
     * 
     * @param value
     *     allowed object is
     *     {@link Integer }
     *     
     */
    public void setStA(Integer value) {
        this.stA = value;
    }

    /**
     * Gets the value of the stPos property.
     * 
     * @return
     *     possible object is
     *     {@link Integer }
     *     
     */
    public Integer getStPos() {
        return stPos;
    }

    /**
     * Sets the value of the stPos property.
     * 
     * @param value
     *     allowed object is
     *     {@link Integer }
     *     
     */
    public void setStPos(Integer value) {
        this.stPos = value;
    }

    /**
     * Gets the value of the endA property.
     * 
     * @return
     *     possible object is
     *     {@link Integer }
     *     
     */
    public Integer getEndA() {
        return endA;
    }

    /**
     * Sets the value of the endA property.
     * 
     * @param value
     *     allowed object is
     *     {@link Integer }
     *     
     */
    public void setEndA(Integer value) {
        this.endA = value;
    }

    /**
     * Gets the value of the endPos property.
     * 
     * @return
     *     possible object is
     *     {@link Integer }
     *     
     */
    public Integer getEndPos() {
        return endPos;
    }

    /**
     * Sets the value of the endPos property.
     * 
     * @param value
     *     allowed object is
     *     {@link Integer }
     *     
     */
    public void setEndPos(Integer value) {
        this.endPos = value;
    }

    /**
     * Gets the value of the dist property.
     * 
     * @return
     *     possible object is
     *     {@link Long }
     *     
     */
    public Long getDist() {
        return dist;
    }

    /**
     * Sets the value of the dist property.
     * 
     * @param value
     *     allowed object is
     *     {@link Long }
     *     
     */
    public void setDist(Long value) {
        this.dist = value;
    }

    /**
     * Gets the value of the dir property.
     * 
     * @return
     *     possible object is
     *     {@link Integer }
     *     
     */
    public Integer getDir() {
        return dir;
    }

    /**
     * Sets the value of the dir property.
     * 
     * @param value
     *     allowed object is
     *     {@link Integer }
     *     
     */
    public void setDir(Integer value) {
        this.dir = value;
    }

    /**
     * Gets the value of the fadeDir property.
     * 
     * @return
     *     possible object is
     *     {@link Integer }
     *     
     */
    public Integer getFadeDir() {
        return fadeDir;
    }

    /**
     * Sets the value of the fadeDir property.
     * 
     * @param value
     *     allowed object is
     *     {@link Integer }
     *     
     */
    public void setFadeDir(Integer value) {
        this.fadeDir = value;
    }

    /**
     * Gets the value of the sx property.
     * 
     * @return
     *     possible object is
     *     {@link Integer }
     *     
     */
    public Integer getSx() {
        return sx;
    }

    /**
     * Sets the value of the sx property.
     * 
     * @param value
     *     allowed object is
     *     {@link Integer }
     *     
     */
    public void setSx(Integer value) {
        this.sx = value;
    }

    /**
     * Gets the value of the sy property.
     * 
     * @return
     *     possible object is
     *     {@link Integer }
     *     
     */
    public Integer getSy() {
        return sy;
    }

    /**
     * Sets the value of the sy property.
     * 
     * @param value
     *     allowed object is
     *     {@link Integer }
     *     
     */
    public void setSy(Integer value) {
        this.sy = value;
    }

    /**
     * Gets the value of the kx property.
     * 
     * @return
     *     possible object is
     *     {@link Integer }
     *     
     */
    public Integer getKx() {
        return kx;
    }

    /**
     * Sets the value of the kx property.
     * 
     * @param value
     *     allowed object is
     *     {@link Integer }
     *     
     */
    public void setKx(Integer value) {
        this.kx = value;
    }

    /**
     * Gets the value of the ky property.
     * 
     * @return
     *     possible object is
     *     {@link Integer }
     *     
     */
    public Integer getKy() {
        return ky;
    }

    /**
     * Sets the value of the ky property.
     * 
     * @param value
     *     allowed object is
     *     {@link Integer }
     *     
     */
    public void setKy(Integer value) {
        this.ky = value;
    }

    /**
     * Gets the value of the algn property.
     * 
     * @return
     *     possible object is
     *     {@link STRectAlignment }
     *     
     */
    public STRectAlignment getAlgn() {
        return algn;
    }

    /**
     * Sets the value of the algn property.
     * 
     * @param value
     *     allowed object is
     *     {@link STRectAlignment }
     *     
     */
    public void setAlgn(STRectAlignment value) {
        this.algn = value;
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
