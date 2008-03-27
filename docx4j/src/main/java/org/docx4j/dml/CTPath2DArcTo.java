/*
 *  Copyright 2007, Plutext Pty Ltd.
 *   
 *  This file is part of docx4j.

    docx4j is free software: you can use it, redistribute it and/or modify
    it under the terms of version 3 of the GNU Affero General Public License 
    as published by the Free Software Foundation.

    If you need the right to use it under a different license, please
    contact Plutext.

    docx4j is distributed in the hope that it will be useful,
    but WITHOUT ANY WARRANTY; without even the implied warranty of
    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
    GNU Affero General Public License for more details.

    You should have received a copy of the GNU Affero General Public License   
    along with docx4j.  If not, see <http://www.fsf.org/licensing/licenses/>.
    
 */

package org.docx4j.dml;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlType;


/**
 * <p>Java class for CT_Path2DArcTo complex type.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
 * 
 * <pre>
 * &lt;complexType name="CT_Path2DArcTo">
 *   &lt;complexContent>
 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
 *       &lt;attribute name="wR" use="required" type="{http://schemas.openxmlformats.org/drawingml/2006/main}ST_AdjCoordinate" />
 *       &lt;attribute name="hR" use="required" type="{http://schemas.openxmlformats.org/drawingml/2006/main}ST_AdjCoordinate" />
 *       &lt;attribute name="stAng" use="required" type="{http://schemas.openxmlformats.org/drawingml/2006/main}ST_AdjAngle" />
 *       &lt;attribute name="swAng" use="required" type="{http://schemas.openxmlformats.org/drawingml/2006/main}ST_AdjAngle" />
 *     &lt;/restriction>
 *   &lt;/complexContent>
 * &lt;/complexType>
 * </pre>
 * 
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "CT_Path2DArcTo")
public class CTPath2DArcTo {

    @XmlAttribute(name = "wR", required = true)
    protected String wr;
    @XmlAttribute(name = "hR", required = true)
    protected String hr;
    @XmlAttribute(required = true)
    protected String stAng;
    @XmlAttribute(required = true)
    protected String swAng;

    /**
     * Gets the value of the wr property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getWR() {
        return wr;
    }

    /**
     * Sets the value of the wr property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setWR(String value) {
        this.wr = value;
    }

    /**
     * Gets the value of the hr property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getHR() {
        return hr;
    }

    /**
     * Sets the value of the hr property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setHR(String value) {
        this.hr = value;
    }

    /**
     * Gets the value of the stAng property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getStAng() {
        return stAng;
    }

    /**
     * Sets the value of the stAng property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setStAng(String value) {
        this.stAng = value;
    }

    /**
     * Gets the value of the swAng property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getSwAng() {
        return swAng;
    }

    /**
     * Sets the value of the swAng property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setSwAng(String value) {
        this.swAng = value;
    }

}
