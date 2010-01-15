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
 * <p>Java class for CT_LineEndProperties complex type.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
 * 
 * <pre>
 * &lt;complexType name="CT_LineEndProperties">
 *   &lt;complexContent>
 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
 *       &lt;attribute name="type" type="{http://schemas.openxmlformats.org/drawingml/2006/main}ST_LineEndType" />
 *       &lt;attribute name="w" type="{http://schemas.openxmlformats.org/drawingml/2006/main}ST_LineEndWidth" />
 *       &lt;attribute name="len" type="{http://schemas.openxmlformats.org/drawingml/2006/main}ST_LineEndLength" />
 *     &lt;/restriction>
 *   &lt;/complexContent>
 * &lt;/complexType>
 * </pre>
 * 
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "CT_LineEndProperties")
public class CTLineEndProperties {

    @XmlAttribute
    protected STLineEndType type;
    @XmlAttribute
    protected STLineEndWidth w;
    @XmlAttribute
    protected STLineEndLength len;

    /**
     * Gets the value of the type property.
     * 
     * @return
     *     possible object is
     *     {@link STLineEndType }
     *     
     */
    public STLineEndType getType() {
        return type;
    }

    /**
     * Sets the value of the type property.
     * 
     * @param value
     *     allowed object is
     *     {@link STLineEndType }
     *     
     */
    public void setType(STLineEndType value) {
        this.type = value;
    }

    /**
     * Gets the value of the w property.
     * 
     * @return
     *     possible object is
     *     {@link STLineEndWidth }
     *     
     */
    public STLineEndWidth getW() {
        return w;
    }

    /**
     * Sets the value of the w property.
     * 
     * @param value
     *     allowed object is
     *     {@link STLineEndWidth }
     *     
     */
    public void setW(STLineEndWidth value) {
        this.w = value;
    }

    /**
     * Gets the value of the len property.
     * 
     * @return
     *     possible object is
     *     {@link STLineEndLength }
     *     
     */
    public STLineEndLength getLen() {
        return len;
    }

    /**
     * Sets the value of the len property.
     * 
     * @param value
     *     allowed object is
     *     {@link STLineEndLength }
     *     
     */
    public void setLen(STLineEndLength value) {
        this.len = value;
    }

}
