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


package org.docx4j.vml.officedrawing;

import javax.xml.bind.annotation.XmlEnum;
import javax.xml.bind.annotation.XmlEnumValue;
import javax.xml.bind.annotation.XmlType;


/**
 * <p>Java class for ST_FillType.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
 * <p>
 * <pre>
 * &lt;simpleType name="ST_FillType">
 *   &lt;restriction base="{http://www.w3.org/2001/XMLSchema}string">
 *     &lt;enumeration value="gradientCenter"/>
 *     &lt;enumeration value="solid"/>
 *     &lt;enumeration value="pattern"/>
 *     &lt;enumeration value="tile"/>
 *     &lt;enumeration value="frame"/>
 *     &lt;enumeration value="gradientUnscaled"/>
 *     &lt;enumeration value="gradientRadial"/>
 *     &lt;enumeration value="gradient"/>
 *     &lt;enumeration value="background"/>
 *   &lt;/restriction>
 * &lt;/simpleType>
 * </pre>
 * 
 */
@XmlType(name = "ST_FillType")
@XmlEnum
public enum STFillType {


    /**
     * Centered Radial Gradient
     * 
     */
    @XmlEnumValue("gradientCenter")
    GRADIENT_CENTER("gradientCenter"),

    /**
     * Solid Fill
     * 
     */
    @XmlEnumValue("solid")
    SOLID("solid"),

    /**
     * Image Pattern
     * 
     */
    @XmlEnumValue("pattern")
    PATTERN("pattern"),

    /**
     * Tiled Image
     * 
     */
    @XmlEnumValue("tile")
    TILE("tile"),

    /**
     * Stretch Image to Fit
     * 
     */
    @XmlEnumValue("frame")
    FRAME("frame"),

    /**
     * Unscaled Gradient
     * 
     */
    @XmlEnumValue("gradientUnscaled")
    GRADIENT_UNSCALED("gradientUnscaled"),

    /**
     * Radial Gradient
     * 
     */
    @XmlEnumValue("gradientRadial")
    GRADIENT_RADIAL("gradientRadial"),

    /**
     * Linear Gradient
     * 
     */
    @XmlEnumValue("gradient")
    GRADIENT("gradient"),

    /**
     * Use Background Fill
     * 
     */
    @XmlEnumValue("background")
    BACKGROUND("background");
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
