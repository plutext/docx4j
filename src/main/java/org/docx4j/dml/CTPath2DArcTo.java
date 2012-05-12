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
