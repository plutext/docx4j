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
 * <p>Java class for CT_Caption complex type.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
 * 
 * <pre>
 * &lt;complexType name="CT_Caption">
 *   &lt;complexContent>
 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
 *       &lt;attribute name="name" use="required" type="{http://schemas.openxmlformats.org/wordprocessingml/2006/main}ST_String" />
 *       &lt;attribute name="pos" type="{http://schemas.openxmlformats.org/wordprocessingml/2006/main}ST_CaptionPos" />
 *       &lt;attribute name="chapNum" type="{http://www.w3.org/2001/XMLSchema}boolean" default="true" />
 *       &lt;attribute name="heading" type="{http://schemas.openxmlformats.org/wordprocessingml/2006/main}ST_DecimalNumber" />
 *       &lt;attribute name="noLabel" type="{http://www.w3.org/2001/XMLSchema}boolean" default="true" />
 *       &lt;attribute name="numFmt" type="{http://schemas.openxmlformats.org/wordprocessingml/2006/main}NumberFormat" />
 *       &lt;attribute name="sep" type="{http://schemas.openxmlformats.org/wordprocessingml/2006/main}ST_ChapterSep" />
 *     &lt;/restriction>
 *   &lt;/complexContent>
 * &lt;/complexType>
 * </pre>
 * 
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "CT_Caption")
public class CTCaption implements Child
{

    @XmlAttribute(name = "name", namespace = "http://schemas.openxmlformats.org/wordprocessingml/2006/main", required = true)
    protected String name;
    @XmlAttribute(name = "pos", namespace = "http://schemas.openxmlformats.org/wordprocessingml/2006/main")
    protected STCaptionPos pos;
    @XmlAttribute(name = "chapNum", namespace = "http://schemas.openxmlformats.org/wordprocessingml/2006/main")
    protected Boolean chapNum;
    @XmlAttribute(name = "heading", namespace = "http://schemas.openxmlformats.org/wordprocessingml/2006/main")
    protected BigInteger heading;
    @XmlAttribute(name = "noLabel", namespace = "http://schemas.openxmlformats.org/wordprocessingml/2006/main")
    protected Boolean noLabel;
    @XmlAttribute(name = "numFmt", namespace = "http://schemas.openxmlformats.org/wordprocessingml/2006/main")
    protected NumberFormat numFmt;
    @XmlAttribute(name = "sep", namespace = "http://schemas.openxmlformats.org/wordprocessingml/2006/main")
    protected STChapterSep sep;
    @XmlTransient
    private Object parent;

    /**
     * Gets the value of the name property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getName() {
        return name;
    }

    /**
     * Sets the value of the name property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setName(String value) {
        this.name = value;
    }

    /**
     * Gets the value of the pos property.
     * 
     * @return
     *     possible object is
     *     {@link STCaptionPos }
     *     
     */
    public STCaptionPos getPos() {
        return pos;
    }

    /**
     * Sets the value of the pos property.
     * 
     * @param value
     *     allowed object is
     *     {@link STCaptionPos }
     *     
     */
    public void setPos(STCaptionPos value) {
        this.pos = value;
    }

    /**
     * Gets the value of the chapNum property.
     * 
     * @return
     *     possible object is
     *     {@link Boolean }
     *     
     */
    public boolean isChapNum() {
        if (chapNum == null) {
            return true;
        } else {
            return chapNum;
        }
    }

    /**
     * Sets the value of the chapNum property.
     * 
     * @param value
     *     allowed object is
     *     {@link Boolean }
     *     
     */
    public void setChapNum(Boolean value) {
        this.chapNum = value;
    }

    /**
     * Gets the value of the heading property.
     * 
     * @return
     *     possible object is
     *     {@link BigInteger }
     *     
     */
    public BigInteger getHeading() {
        return heading;
    }

    /**
     * Sets the value of the heading property.
     * 
     * @param value
     *     allowed object is
     *     {@link BigInteger }
     *     
     */
    public void setHeading(BigInteger value) {
        this.heading = value;
    }

    /**
     * Gets the value of the noLabel property.
     * 
     * @return
     *     possible object is
     *     {@link Boolean }
     *     
     */
    public boolean isNoLabel() {
        if (noLabel == null) {
            return true;
        } else {
            return noLabel;
        }
    }

    /**
     * Sets the value of the noLabel property.
     * 
     * @param value
     *     allowed object is
     *     {@link Boolean }
     *     
     */
    public void setNoLabel(Boolean value) {
        this.noLabel = value;
    }

    /**
     * Gets the value of the numFmt property.
     * 
     * @return
     *     possible object is
     *     {@link NumberFormat }
     *     
     */
    public NumberFormat getNumFmt() {
        return numFmt;
    }

    /**
     * Sets the value of the numFmt property.
     * 
     * @param value
     *     allowed object is
     *     {@link NumberFormat }
     *     
     */
    public void setNumFmt(NumberFormat value) {
        this.numFmt = value;
    }

    /**
     * Gets the value of the sep property.
     * 
     * @return
     *     possible object is
     *     {@link STChapterSep }
     *     
     */
    public STChapterSep getSep() {
        return sep;
    }

    /**
     * Sets the value of the sep property.
     * 
     * @param value
     *     allowed object is
     *     {@link STChapterSep }
     *     
     */
    public void setSep(STChapterSep value) {
        this.sep = value;
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
