/*
 *  Copyright 2007-2013, Plutext Pty Ltd.
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


package org.docx4j.wml; 

import java.math.BigInteger;

import javax.xml.bind.Unmarshaller;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlTransient;
import javax.xml.bind.annotation.XmlType;

import org.jvnet.jaxb2_commons.ppp.Child;


/**
 * <p>Java class for CT_FramePr complex type.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
 * 
 * <pre>
 * &lt;complexType name="CT_FramePr">
 *   &lt;complexContent>
 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
 *       &lt;attribute name="dropCap" type="{http://schemas.openxmlformats.org/wordprocessingml/2006/main}ST_DropCap" />
 *       &lt;attribute name="lines" type="{http://schemas.openxmlformats.org/wordprocessingml/2006/main}ST_DecimalNumber" />
 *       &lt;attribute name="w" type="{http://schemas.openxmlformats.org/wordprocessingml/2006/main}ST_TwipsMeasure" />
 *       &lt;attribute name="h" type="{http://schemas.openxmlformats.org/wordprocessingml/2006/main}ST_TwipsMeasure" />
 *       &lt;attribute name="vSpace" type="{http://schemas.openxmlformats.org/wordprocessingml/2006/main}ST_TwipsMeasure" />
 *       &lt;attribute name="hSpace" type="{http://schemas.openxmlformats.org/wordprocessingml/2006/main}ST_TwipsMeasure" />
 *       &lt;attribute name="wrap" type="{http://schemas.openxmlformats.org/wordprocessingml/2006/main}ST_Wrap" />
 *       &lt;attribute name="hAnchor" type="{http://schemas.openxmlformats.org/wordprocessingml/2006/main}ST_HAnchor" />
 *       &lt;attribute name="vAnchor" type="{http://schemas.openxmlformats.org/wordprocessingml/2006/main}ST_VAnchor" />
 *       &lt;attribute name="x" type="{http://schemas.openxmlformats.org/wordprocessingml/2006/main}ST_SignedTwipsMeasure" />
 *       &lt;attribute name="xAlign" type="{http://schemas.openxmlformats.org/wordprocessingml/2006/main}ST_XAlign" />
 *       &lt;attribute name="y" type="{http://schemas.openxmlformats.org/wordprocessingml/2006/main}ST_SignedTwipsMeasure" />
 *       &lt;attribute name="yAlign" type="{http://schemas.openxmlformats.org/wordprocessingml/2006/main}ST_YAlign" />
 *       &lt;attribute name="hRule" type="{http://schemas.openxmlformats.org/wordprocessingml/2006/main}ST_HeightRule" />
 *       &lt;attribute name="anchorLock" type="{http://www.w3.org/2001/XMLSchema}boolean" default="true" />
 *     &lt;/restriction>
 *   &lt;/complexContent>
 * &lt;/complexType>
 * </pre>
 * 
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "CT_FramePr")
public class CTFramePr implements Child
{

    @XmlAttribute(name = "dropCap", namespace = "http://schemas.openxmlformats.org/wordprocessingml/2006/main")
    protected STDropCap dropCap;
    @XmlAttribute(name = "lines", namespace = "http://schemas.openxmlformats.org/wordprocessingml/2006/main")
    protected BigInteger lines;
    @XmlAttribute(name = "w", namespace = "http://schemas.openxmlformats.org/wordprocessingml/2006/main")
    protected BigInteger w;
    @XmlAttribute(name = "h", namespace = "http://schemas.openxmlformats.org/wordprocessingml/2006/main")
    protected BigInteger h;
    @XmlAttribute(name = "vSpace", namespace = "http://schemas.openxmlformats.org/wordprocessingml/2006/main")
    protected BigInteger vSpace;
    @XmlAttribute(name = "hSpace", namespace = "http://schemas.openxmlformats.org/wordprocessingml/2006/main")
    protected BigInteger hSpace;
    @XmlAttribute(name = "wrap", namespace = "http://schemas.openxmlformats.org/wordprocessingml/2006/main")
    protected STWrap wrap;
    @XmlAttribute(name = "hAnchor", namespace = "http://schemas.openxmlformats.org/wordprocessingml/2006/main")
    protected STHAnchor hAnchor;
    @XmlAttribute(name = "vAnchor", namespace = "http://schemas.openxmlformats.org/wordprocessingml/2006/main")
    protected STVAnchor vAnchor;
    @XmlAttribute(name = "x", namespace = "http://schemas.openxmlformats.org/wordprocessingml/2006/main")
    protected BigInteger x;
    @XmlAttribute(name = "xAlign", namespace = "http://schemas.openxmlformats.org/wordprocessingml/2006/main")
    protected STXAlign xAlign;
    @XmlAttribute(name = "y", namespace = "http://schemas.openxmlformats.org/wordprocessingml/2006/main")
    protected BigInteger y;
    @XmlAttribute(name = "yAlign", namespace = "http://schemas.openxmlformats.org/wordprocessingml/2006/main")
    protected STYAlign yAlign;
    @XmlAttribute(name = "hRule", namespace = "http://schemas.openxmlformats.org/wordprocessingml/2006/main")
    protected STHeightRule hRule;
    @XmlAttribute(name = "anchorLock", namespace = "http://schemas.openxmlformats.org/wordprocessingml/2006/main")
    protected Boolean anchorLock;
    @XmlTransient
    private Object parent;

    /**
     * Gets the value of the dropCap property.
     * 
     * @return
     *     possible object is
     *     {@link STDropCap }
     *     
     */
    public STDropCap getDropCap() {
        return dropCap;
    }

    /**
     * Sets the value of the dropCap property.
     * 
     * @param value
     *     allowed object is
     *     {@link STDropCap }
     *     
     */
    public void setDropCap(STDropCap value) {
        this.dropCap = value;
    }

    /**
     * Gets the value of the lines property.
     * 
     * @return
     *     possible object is
     *     {@link BigInteger }
     *     
     */
    public BigInteger getLines() {
        return lines;
    }

    /**
     * Sets the value of the lines property.
     * 
     * @param value
     *     allowed object is
     *     {@link BigInteger }
     *     
     */
    public void setLines(BigInteger value) {
        this.lines = value;
    }

    /**
     * Gets the value of the w property.
     * 
     * @return
     *     possible object is
     *     {@link BigInteger }
     *     
     */
    public BigInteger getW() {
        return w;
    }

    /**
     * Sets the value of the w property.
     * 
     * @param value
     *     allowed object is
     *     {@link BigInteger }
     *     
     */
    public void setW(BigInteger value) {
        this.w = value;
    }

    /**
     * Gets the value of the h property.
     * 
     * @return
     *     possible object is
     *     {@link BigInteger }
     *     
     */
    public BigInteger getH() {
        return h;
    }

    /**
     * Sets the value of the h property.
     * 
     * @param value
     *     allowed object is
     *     {@link BigInteger }
     *     
     */
    public void setH(BigInteger value) {
        this.h = value;
    }

    /**
     * Gets the value of the vSpace property.
     * 
     * @return
     *     possible object is
     *     {@link BigInteger }
     *     
     */
    public BigInteger getVSpace() {
        return vSpace;
    }

    /**
     * Sets the value of the vSpace property.
     * 
     * @param value
     *     allowed object is
     *     {@link BigInteger }
     *     
     */
    public void setVSpace(BigInteger value) {
        this.vSpace = value;
    }

    /**
     * Gets the value of the hSpace property.
     * 
     * @return
     *     possible object is
     *     {@link BigInteger }
     *     
     */
    public BigInteger getHSpace() {
        return hSpace;
    }

    /**
     * Sets the value of the hSpace property.
     * 
     * @param value
     *     allowed object is
     *     {@link BigInteger }
     *     
     */
    public void setHSpace(BigInteger value) {
        this.hSpace = value;
    }

    /**
     * Gets the value of the wrap property.
     * 
     * @return
     *     possible object is
     *     {@link STWrap }
     *     
     */
    public STWrap getWrap() {
        return wrap;
    }

    /**
     * Sets the value of the wrap property.
     * 
     * @param value
     *     allowed object is
     *     {@link STWrap }
     *     
     */
    public void setWrap(STWrap value) {
        this.wrap = value;
    }

    /**
     * Gets the value of the hAnchor property.
     * 
     * @return
     *     possible object is
     *     {@link STHAnchor }
     *     
     */
    public STHAnchor getHAnchor() {
        return hAnchor;
    }

    /**
     * Sets the value of the hAnchor property.
     * 
     * @param value
     *     allowed object is
     *     {@link STHAnchor }
     *     
     */
    public void setHAnchor(STHAnchor value) {
        this.hAnchor = value;
    }

    /**
     * Gets the value of the vAnchor property.
     * 
     * @return
     *     possible object is
     *     {@link STVAnchor }
     *     
     */
    public STVAnchor getVAnchor() {
        return vAnchor;
    }

    /**
     * Sets the value of the vAnchor property.
     * 
     * @param value
     *     allowed object is
     *     {@link STVAnchor }
     *     
     */
    public void setVAnchor(STVAnchor value) {
        this.vAnchor = value;
    }

    /**
     * Gets the value of the x property.
     * 
     * @return
     *     possible object is
     *     {@link BigInteger }
     *     
     */
    public BigInteger getX() {
        return x;
    }

    /**
     * Sets the value of the x property.
     * 
     * @param value
     *     allowed object is
     *     {@link BigInteger }
     *     
     */
    public void setX(BigInteger value) {
        this.x = value;
    }

    /**
     * Gets the value of the xAlign property.
     * 
     * @return
     *     possible object is
     *     {@link STXAlign }
     *     
     */
    public STXAlign getXAlign() {
        return xAlign;
    }

    /**
     * Sets the value of the xAlign property.
     * 
     * @param value
     *     allowed object is
     *     {@link STXAlign }
     *     
     */
    public void setXAlign(STXAlign value) {
        this.xAlign = value;
    }

    /**
     * Gets the value of the y property.
     * 
     * @return
     *     possible object is
     *     {@link BigInteger }
     *     
     */
    public BigInteger getY() {
        return y;
    }

    /**
     * Sets the value of the y property.
     * 
     * @param value
     *     allowed object is
     *     {@link BigInteger }
     *     
     */
    public void setY(BigInteger value) {
        this.y = value;
    }

    /**
     * Gets the value of the yAlign property.
     * 
     * @return
     *     possible object is
     *     {@link STYAlign }
     *     
     */
    public STYAlign getYAlign() {
        return yAlign;
    }

    /**
     * Sets the value of the yAlign property.
     * 
     * @param value
     *     allowed object is
     *     {@link STYAlign }
     *     
     */
    public void setYAlign(STYAlign value) {
        this.yAlign = value;
    }

    /**
     * Gets the value of the hRule property.
     * 
     * @return
     *     possible object is
     *     {@link STHeightRule }
     *     
     */
    public STHeightRule getHRule() {
        return hRule;
    }

    /**
     * Sets the value of the hRule property.
     * 
     * @param value
     *     allowed object is
     *     {@link STHeightRule }
     *     
     */
    public void setHRule(STHeightRule value) {
        this.hRule = value;
    }

    /**
     * Gets the value of the anchorLock property.
     * 
     * @return
     *     possible object is
     *     {@link Boolean }
     *     
     */
    public boolean isAnchorLock() {
        if (anchorLock == null) {
            return true;
        } else {
            return anchorLock;
        }
    }

    /**
     * Sets the value of the anchorLock property.
     * 
     * @param value
     *     allowed object is
     *     {@link Boolean }
     *     
     */
    public void setAnchorLock(Boolean value) {
        this.anchorLock = value;
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
