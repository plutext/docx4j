/*
 *  Copyright 2007, Plutext Pty Ltd.
 *   
 *  This file is part of docx4j.

    docx4j is free software: you can use it, redistribute it and/or modify
    it under the terms of version 3 of the GNU Affero General Public License 
    as published by the Free Software Foundation.

    docx4j is distributed in the hope that it will be useful,
    but WITHOUT ANY WARRANTY; without even the implied warranty of
    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
    GNU Affero General Public License for more details.

    You should have received a copy of the GNU Affero General Public License   
    along with docx4j.  If not, see <http://www.fsf.org/licensing/licenses/>.
    
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
 * <p>Java class for PageSz complex type.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
 * 
 * <pre>
 * &lt;complexType name="PageSz">
 *   &lt;complexContent>
 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
 *       &lt;attribute name="w" type="{http://schemas.openxmlformats.org/wordprocessingml/2006/main}ST_TwipsMeasure" />
 *       &lt;attribute name="h" type="{http://schemas.openxmlformats.org/wordprocessingml/2006/main}ST_TwipsMeasure" />
 *       &lt;attribute name="orient" type="{http://schemas.openxmlformats.org/wordprocessingml/2006/main}ST_PageOrientation" />
 *       &lt;attribute name="code" type="{http://www.w3.org/2001/XMLSchema}integer" />
 *     &lt;/restriction>
 *   &lt;/complexContent>
 * &lt;/complexType>
 * </pre>
 * 
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "PageSz")
public class PageSz
    implements Child
{

    @XmlAttribute(namespace = "http://schemas.openxmlformats.org/wordprocessingml/2006/main")
    protected BigInteger w;
    @XmlAttribute(namespace = "http://schemas.openxmlformats.org/wordprocessingml/2006/main")
    protected BigInteger h;
    @XmlAttribute(namespace = "http://schemas.openxmlformats.org/wordprocessingml/2006/main")
    protected STPageOrientation orient;
    @XmlAttribute(namespace = "http://schemas.openxmlformats.org/wordprocessingml/2006/main")
    protected BigInteger code;
    @XmlTransient
    private Object parent;

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
     * Gets the value of the orient property.
     * 
     * @return
     *     possible object is
     *     {@link STPageOrientation }
     *     
     */
    public STPageOrientation getOrient() {
        return orient;
    }

    /**
     * Sets the value of the orient property.
     * 
     * @param value
     *     allowed object is
     *     {@link STPageOrientation }
     *     
     */
    public void setOrient(STPageOrientation value) {
        this.orient = value;
    }

    /**
     * Gets the value of the code property.
     * 
     * @return
     *     possible object is
     *     {@link BigInteger }
     *     
     */
    public BigInteger getCode() {
        return code;
    }

    /**
     * Sets the value of the code property.
     * 
     * @param value
     *     allowed object is
     *     {@link BigInteger }
     *     
     */
    public void setCode(BigInteger value) {
        this.code = value;
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
