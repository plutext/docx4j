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
 * <p>Java class for CT_TblPPr complex type.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
 * 
 * <pre>
 * &lt;complexType name="CT_TblPPr">
 *   &lt;complexContent>
 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
 *       &lt;attribute name="leftFromText" type="{http://schemas.openxmlformats.org/wordprocessingml/2006/main}ST_TwipsMeasure" />
 *       &lt;attribute name="rightFromText" type="{http://schemas.openxmlformats.org/wordprocessingml/2006/main}ST_TwipsMeasure" />
 *       &lt;attribute name="topFromText" type="{http://schemas.openxmlformats.org/wordprocessingml/2006/main}ST_TwipsMeasure" />
 *       &lt;attribute name="bottomFromText" type="{http://schemas.openxmlformats.org/wordprocessingml/2006/main}ST_TwipsMeasure" />
 *       &lt;attribute name="vertAnchor" type="{http://schemas.openxmlformats.org/wordprocessingml/2006/main}ST_VAnchor" />
 *       &lt;attribute name="horzAnchor" type="{http://schemas.openxmlformats.org/wordprocessingml/2006/main}ST_HAnchor" />
 *       &lt;attribute name="tblpXSpec" type="{http://schemas.openxmlformats.org/wordprocessingml/2006/main}ST_XAlign" />
 *       &lt;attribute name="tblpX" type="{http://schemas.openxmlformats.org/wordprocessingml/2006/main}ST_SignedTwipsMeasure" />
 *       &lt;attribute name="tblpYSpec" type="{http://schemas.openxmlformats.org/wordprocessingml/2006/main}ST_YAlign" />
 *       &lt;attribute name="tblpY" type="{http://schemas.openxmlformats.org/wordprocessingml/2006/main}ST_SignedTwipsMeasure" />
 *     &lt;/restriction>
 *   &lt;/complexContent>
 * &lt;/complexType>
 * </pre>
 * 
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "CT_TblPPr")
public class CTTblPPr implements Child
{

    @XmlAttribute(name = "leftFromText", namespace = "http://schemas.openxmlformats.org/wordprocessingml/2006/main")
    protected BigInteger leftFromText;
    @XmlAttribute(name = "rightFromText", namespace = "http://schemas.openxmlformats.org/wordprocessingml/2006/main")
    protected BigInteger rightFromText;
    @XmlAttribute(name = "topFromText", namespace = "http://schemas.openxmlformats.org/wordprocessingml/2006/main")
    protected BigInteger topFromText;
    @XmlAttribute(name = "bottomFromText", namespace = "http://schemas.openxmlformats.org/wordprocessingml/2006/main")
    protected BigInteger bottomFromText;
    @XmlAttribute(name = "vertAnchor", namespace = "http://schemas.openxmlformats.org/wordprocessingml/2006/main")
    protected STVAnchor vertAnchor;
    @XmlAttribute(name = "horzAnchor", namespace = "http://schemas.openxmlformats.org/wordprocessingml/2006/main")
    protected STHAnchor horzAnchor;
    @XmlAttribute(name = "tblpXSpec", namespace = "http://schemas.openxmlformats.org/wordprocessingml/2006/main")
    protected STXAlign tblpXSpec;
    @XmlAttribute(name = "tblpX", namespace = "http://schemas.openxmlformats.org/wordprocessingml/2006/main")
    protected BigInteger tblpX;
    @XmlAttribute(name = "tblpYSpec", namespace = "http://schemas.openxmlformats.org/wordprocessingml/2006/main")
    protected STYAlign tblpYSpec;
    @XmlAttribute(name = "tblpY", namespace = "http://schemas.openxmlformats.org/wordprocessingml/2006/main")
    protected BigInteger tblpY;
    @XmlTransient
    private Object parent;

    /**
     * Gets the value of the leftFromText property.
     * 
     * @return
     *     possible object is
     *     {@link BigInteger }
     *     
     */
    public BigInteger getLeftFromText() {
        return leftFromText;
    }

    /**
     * Sets the value of the leftFromText property.
     * 
     * @param value
     *     allowed object is
     *     {@link BigInteger }
     *     
     */
    public void setLeftFromText(BigInteger value) {
        this.leftFromText = value;
    }

    /**
     * Gets the value of the rightFromText property.
     * 
     * @return
     *     possible object is
     *     {@link BigInteger }
     *     
     */
    public BigInteger getRightFromText() {
        return rightFromText;
    }

    /**
     * Sets the value of the rightFromText property.
     * 
     * @param value
     *     allowed object is
     *     {@link BigInteger }
     *     
     */
    public void setRightFromText(BigInteger value) {
        this.rightFromText = value;
    }

    /**
     * Gets the value of the topFromText property.
     * 
     * @return
     *     possible object is
     *     {@link BigInteger }
     *     
     */
    public BigInteger getTopFromText() {
        return topFromText;
    }

    /**
     * Sets the value of the topFromText property.
     * 
     * @param value
     *     allowed object is
     *     {@link BigInteger }
     *     
     */
    public void setTopFromText(BigInteger value) {
        this.topFromText = value;
    }

    /**
     * Gets the value of the bottomFromText property.
     * 
     * @return
     *     possible object is
     *     {@link BigInteger }
     *     
     */
    public BigInteger getBottomFromText() {
        return bottomFromText;
    }

    /**
     * Sets the value of the bottomFromText property.
     * 
     * @param value
     *     allowed object is
     *     {@link BigInteger }
     *     
     */
    public void setBottomFromText(BigInteger value) {
        this.bottomFromText = value;
    }

    /**
     * Gets the value of the vertAnchor property.
     * 
     * @return
     *     possible object is
     *     {@link STVAnchor }
     *     
     */
    public STVAnchor getVertAnchor() {
        return vertAnchor;
    }

    /**
     * Sets the value of the vertAnchor property.
     * 
     * @param value
     *     allowed object is
     *     {@link STVAnchor }
     *     
     */
    public void setVertAnchor(STVAnchor value) {
        this.vertAnchor = value;
    }

    /**
     * Gets the value of the horzAnchor property.
     * 
     * @return
     *     possible object is
     *     {@link STHAnchor }
     *     
     */
    public STHAnchor getHorzAnchor() {
        return horzAnchor;
    }

    /**
     * Sets the value of the horzAnchor property.
     * 
     * @param value
     *     allowed object is
     *     {@link STHAnchor }
     *     
     */
    public void setHorzAnchor(STHAnchor value) {
        this.horzAnchor = value;
    }

    /**
     * Gets the value of the tblpXSpec property.
     * 
     * @return
     *     possible object is
     *     {@link STXAlign }
     *     
     */
    public STXAlign getTblpXSpec() {
        return tblpXSpec;
    }

    /**
     * Sets the value of the tblpXSpec property.
     * 
     * @param value
     *     allowed object is
     *     {@link STXAlign }
     *     
     */
    public void setTblpXSpec(STXAlign value) {
        this.tblpXSpec = value;
    }

    /**
     * Gets the value of the tblpX property.
     * 
     * @return
     *     possible object is
     *     {@link BigInteger }
     *     
     */
    public BigInteger getTblpX() {
        return tblpX;
    }

    /**
     * Sets the value of the tblpX property.
     * 
     * @param value
     *     allowed object is
     *     {@link BigInteger }
     *     
     */
    public void setTblpX(BigInteger value) {
        this.tblpX = value;
    }

    /**
     * Gets the value of the tblpYSpec property.
     * 
     * @return
     *     possible object is
     *     {@link STYAlign }
     *     
     */
    public STYAlign getTblpYSpec() {
        return tblpYSpec;
    }

    /**
     * Sets the value of the tblpYSpec property.
     * 
     * @param value
     *     allowed object is
     *     {@link STYAlign }
     *     
     */
    public void setTblpYSpec(STYAlign value) {
        this.tblpYSpec = value;
    }

    /**
     * Gets the value of the tblpY property.
     * 
     * @return
     *     possible object is
     *     {@link BigInteger }
     *     
     */
    public BigInteger getTblpY() {
        return tblpY;
    }

    /**
     * Sets the value of the tblpY property.
     * 
     * @param value
     *     allowed object is
     *     {@link BigInteger }
     *     
     */
    public void setTblpY(BigInteger value) {
        this.tblpY = value;
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
