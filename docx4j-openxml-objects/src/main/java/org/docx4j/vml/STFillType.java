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


package org.docx4j.vml;

import jakarta.xml.bind.annotation.XmlEnum;
import jakarta.xml.bind.annotation.XmlEnumValue;
import jakarta.xml.bind.annotation.XmlType;


/**
 * <p>Java class for ST_FillType.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
 * <p>
 * <pre>
 * &lt;simpleType name="ST_FillType">
 *   &lt;restriction base="{http://www.w3.org/2001/XMLSchema}string">
 *     &lt;enumeration value="solid"/>
 *     &lt;enumeration value="gradient"/>
 *     &lt;enumeration value="gradientRadial"/>
 *     &lt;enumeration value="tile"/>
 *     &lt;enumeration value="pattern"/>
 *     &lt;enumeration value="frame"/>
 *   &lt;/restriction>
 * &lt;/simpleType>
 * </pre>
 * 
 */
@XmlType(name = "ST_FillType")
@XmlEnum
public enum STFillType {


    /**
     * Solid Fill
     * 
     */
    @XmlEnumValue("solid")
    SOLID("solid"),

    /**
     * Linear Gradient
     * 
     */
    @XmlEnumValue("gradient")
    GRADIENT("gradient"),

    /**
     * Radial Gradient
     * 
     */
    @XmlEnumValue("gradientRadial")
    GRADIENT_RADIAL("gradientRadial"),

    /**
     * Tiled Image
     * 
     */
    @XmlEnumValue("tile")
    TILE("tile"),

    /**
     * Image Pattern
     * 
     */
    @XmlEnumValue("pattern")
    PATTERN("pattern"),

    /**
     * Stretch Image to Fit
     * 
     */
    @XmlEnumValue("frame")
    FRAME("frame");
    private final String value;

    STFillType(String v) {
        value = v;
    }

    public String value() {
        return value;
    }

    public static STFillType fromValue(String v) {
        for (STFillType c: STFillType.values()) {
            if (c.value.equals(v)) {
                return c;
            }
        }
        throw new IllegalArgumentException(v);
    }

}
