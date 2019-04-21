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
 * <p>Java class for CT_PageNumber complex type.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
 * 
 * <pre>
 * &lt;complexType name="CT_PageNumber">
 *   &lt;complexContent>
 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
 *       &lt;attribute name="fmt" type="{http://schemas.openxmlformats.org/wordprocessingml/2006/main}NumberFormat" />
 *       &lt;attribute name="start" type="{http://schemas.openxmlformats.org/wordprocessingml/2006/main}ST_DecimalNumber" />
 *       &lt;attribute name="chapStyle" type="{http://schemas.openxmlformats.org/wordprocessingml/2006/main}ST_DecimalNumber" />
 *       &lt;attribute name="chapSep" type="{http://schemas.openxmlformats.org/wordprocessingml/2006/main}ST_ChapterSep" />
 *     &lt;/restriction>
 *   &lt;/complexContent>
 * &lt;/complexType>
 * </pre>
 * 
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "CT_PageNumber")
public class CTPageNumber implements Child
{

    @XmlAttribute(name = "fmt", namespace = "http://schemas.openxmlformats.org/wordprocessingml/2006/main")
    protected NumberFormat fmt;
    @XmlAttribute(name = "start", namespace = "http://schemas.openxmlformats.org/wordprocessingml/2006/main")
    protected BigInteger start;
    @XmlAttribute(name = "chapStyle", namespace = "http://schemas.openxmlformats.org/wordprocessingml/2006/main")
    protected BigInteger chapStyle;
    @XmlAttribute(name = "chapSep", namespace = "http://schemas.openxmlformats.org/wordprocessingml/2006/main")
    protected STChapterSep chapSep;
    @XmlTransient
    private Object parent;

    /**
     * Gets the value of the fmt property.
     * 
     * @return
     *     possible object is
     *     {@link NumberFormat }
     *     
     */
    public NumberFormat getFmt() {
        return fmt;
    }

    /**
     * Sets the value of the fmt property.
     * 
     * @param value
     *     allowed object is
     *     {@link NumberFormat }
     *     
     */
    public void setFmt(NumberFormat value) {
        this.fmt = value;
    }

    /**
     * Gets the value of the start property.
     * 
     * @return
     *     possible object is
     *     {@link BigInteger }
     *     
     */
    public BigInteger getStart() {
        return start;
    }

    /**
     * Sets the value of the start property.
     * 
     * @param value
     *     allowed object is
     *     {@link BigInteger }
     *     
     */
    public void setStart(BigInteger value) {
        this.start = value;
    }

    /**
     * Gets the value of the chapStyle property.
     * 
     * @return
     *     possible object is
     *     {@link BigInteger }
     *     
     */
    public BigInteger getChapStyle() {
        return chapStyle;
    }

    /**
     * Sets the value of the chapStyle property.
     * 
     * @param value
     *     allowed object is
     *     {@link BigInteger }
     *     
     */
    public void setChapStyle(BigInteger value) {
        this.chapStyle = value;
    }

    /**
     * Gets the value of the chapSep property.
     * 
     * @return
     *     possible object is
     *     {@link STChapterSep }
     *     
     */
    public STChapterSep getChapSep() {
        return chapSep;
    }

    /**
     * Sets the value of the chapSep property.
     * 
     * @param value
     *     allowed object is
     *     {@link STChapterSep }
     *     
     */
    public void setChapSep(STChapterSep value) {
        this.chapSep = value;
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
