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

import javax.xml.bind.annotation.XmlEnum;
import javax.xml.bind.annotation.XmlEnumValue;
import javax.xml.bind.annotation.XmlType;


/**
 * <p>Java class for ST_TextEffect.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
 * <p>
 * <pre>
 * &lt;simpleType name="ST_TextEffect">
 *   &lt;restriction base="{http://www.w3.org/2001/XMLSchema}string">
 *     &lt;enumeration value="blinkBackground"/>
 *     &lt;enumeration value="lights"/>
 *     &lt;enumeration value="antsBlack"/>
 *     &lt;enumeration value="antsRed"/>
 *     &lt;enumeration value="shimmer"/>
 *     &lt;enumeration value="sparkle"/>
 *     &lt;enumeration value="none"/>
 *   &lt;/restriction>
 * &lt;/simpleType>
 * </pre>
 * 
 */
@XmlType(name = "ST_TextEffect")
@XmlEnum
public enum STTextEffect {


    /**
     * Blinking Background
     * 						Animation
     * 
     */
    @XmlEnumValue("blinkBackground")
    BLINK_BACKGROUND("blinkBackground"),

    /**
     * Colored Lights
     * 						Animation
     * 
     */
    @XmlEnumValue("lights")
    LIGHTS("lights"),

    /**
     * Black Dashed Line
     * 						Animation
     * 
     */
    @XmlEnumValue("antsBlack")
    ANTS_BLACK("antsBlack"),

    /**
     * Marching Red Ants
     * 
     */
    @XmlEnumValue("antsRed")
    ANTS_RED("antsRed"),

    /**
     * Shimmer Animation
     * 
     */
    @XmlEnumValue("shimmer")
    SHIMMER("shimmer"),

    /**
     * Sparkling Lights
     * 						Animation
     * 
     */
    @XmlEnumValue("sparkle")
    SPARKLE("sparkle"),

    /**
     * No Animation
     * 
     */
    @XmlEnumValue("none")
    NONE("none");
    private final String value;

    STTextEffect(String v) {
        value = v;
    }

    public String value() {
        return value;
    }

    public static STTextEffect fromValue(String v) {
        for (STTextEffect c: STTextEffect.values()) {
            if (c.value.equals(v)) {
                return c;
            }
        }
        throw new IllegalArgumentException(v);
    }

}
