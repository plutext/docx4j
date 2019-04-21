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


package org.docx4j.dml.wordprocessingDrawing;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlType;


/**
 * <p>Java class for CT_EffectExtent complex type.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
 * 
 * <pre>
 * &lt;complexType name="CT_EffectExtent">
 *   &lt;complexContent>
 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
 *       &lt;attribute name="l" use="required" type="{http://schemas.openxmlformats.org/drawingml/2006/main}ST_Coordinate" />
 *       &lt;attribute name="t" use="required" type="{http://schemas.openxmlformats.org/drawingml/2006/main}ST_Coordinate" />
 *       &lt;attribute name="r" use="required" type="{http://schemas.openxmlformats.org/drawingml/2006/main}ST_Coordinate" />
 *       &lt;attribute name="b" use="required" type="{http://schemas.openxmlformats.org/drawingml/2006/main}ST_Coordinate" />
 *     &lt;/restriction>
 *   &lt;/complexContent>
 * &lt;/complexType>
 * </pre>
 * 
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "CT_EffectExtent")
public class CTEffectExtent {

    @XmlAttribute(required = true)
    protected long l;
    @XmlAttribute(required = true)
    protected long t;
    @XmlAttribute(required = true)
    protected long r;
    @XmlAttribute(required = true)
    protected long b;

    /**
     * Gets the value of the l property.
     * 
     */
    public long getL() {
        return l;
    }

    /**
     * Sets the value of the l property.
     * 
     */
    public void setL(long value) {
        this.l = value;
    }

    /**
     * Gets the value of the t property.
     * 
     */
    public long getT() {
        return t;
    }

    /**
     * Sets the value of the t property.
     * 
     */
    public void setT(long value) {
        this.t = value;
    }

    /**
     * Gets the value of the r property.
     * 
     */
    public long getR() {
        return r;
    }

    /**
     * Sets the value of the r property.
     * 
     */
    public void setR(long value) {
        this.r = value;
    }

    /**
     * Gets the value of the b property.
     * 
     */
    public long getB() {
        return b;
    }

    /**
     * Sets the value of the b property.
     * 
     */
    public void setB(long value) {
        this.b = value;
    }

}
