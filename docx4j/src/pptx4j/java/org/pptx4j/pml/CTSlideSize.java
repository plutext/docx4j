/*
 *  Copyright 2010, Plutext Pty Ltd.
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


package org.pptx4j.pml;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlType;
import javax.xml.bind.annotation.adapters.CollapsedStringAdapter;
import javax.xml.bind.annotation.adapters.XmlJavaTypeAdapter;


/**
 * <p>Java class for CT_SlideSize complex type.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
 * 
 * <pre>
 * &lt;complexType name="CT_SlideSize">
 *   &lt;complexContent>
 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
 *       &lt;attribute name="cx" use="required" type="{http://schemas.openxmlformats.org/presentationml/2006/main}ST_SlideSizeCoordinate" />
 *       &lt;attribute name="cy" use="required" type="{http://schemas.openxmlformats.org/presentationml/2006/main}ST_SlideSizeCoordinate" />
 *       &lt;attribute name="type" type="{http://schemas.openxmlformats.org/presentationml/2006/main}ST_SlideSizeType" default="custom" />
 *     &lt;/restriction>
 *   &lt;/complexContent>
 * &lt;/complexType>
 * </pre>
 * 
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "CT_SlideSize")
public class CTSlideSize {

    @XmlAttribute(required = true)
    protected int cx;
    @XmlAttribute(required = true)
    protected int cy;
    @XmlAttribute
    @XmlJavaTypeAdapter(CollapsedStringAdapter.class)
    protected String type;

    /**
     * Gets the value of the cx property.
     * 
     */
    public int getCx() {
        return cx;
    }

    /**
     * Sets the value of the cx property.
     * 
     */
    public void setCx(int value) {
        this.cx = value;
    }

    /**
     * Gets the value of the cy property.
     * 
     */
    public int getCy() {
        return cy;
    }

    /**
     * Sets the value of the cy property.
     * 
     */
    public void setCy(int value) {
        this.cy = value;
    }

    /**
     * Gets the value of the type property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getType() {
        if (type == null) {
            return "custom";
        } else {
            return type;
        }
    }

    /**
     * Sets the value of the type property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setType(String value) {
        this.type = value;
    }

}
