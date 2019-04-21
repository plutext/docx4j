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
 * <p>Java class for JcEnumeration.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
 * <p>
 * <pre>
 * &lt;simpleType name="JcEnumeration">
 *   &lt;restriction base="{http://www.w3.org/2001/XMLSchema}string">
 *     &lt;enumeration value="left"/>
 *     &lt;enumeration value="center"/>
 *     &lt;enumeration value="right"/>
 *     &lt;enumeration value="both"/>
 *     &lt;enumeration value="mediumKashida"/>
 *     &lt;enumeration value="distribute"/>
 *     &lt;enumeration value="numTab"/>
 *     &lt;enumeration value="highKashida"/>
 *     &lt;enumeration value="lowKashida"/>
 *     &lt;enumeration value="thaiDistribute"/>
 *   &lt;/restriction>
 * &lt;/simpleType>
 * </pre>
 * 
 */
@XmlType(name = "JcEnumeration")
@XmlEnum
public enum JcEnumeration {


    /**
     * Align Left
     * 
     */
    @XmlEnumValue("left")
    LEFT("left"),

    /**
     * Align Center
     * 
     */
    @XmlEnumValue("center")
    CENTER("center"),

    /**
     * Align Right
     * 
     */
    @XmlEnumValue("right")
    RIGHT("right"),

    /**
     * Justified
     * 
     */
    @XmlEnumValue("both")
    BOTH("both"),

    /**
     * Medium Kashida Length
     * 
     */
    @XmlEnumValue("mediumKashida")
    MEDIUM_KASHIDA("mediumKashida"),

    /**
     * Distribute All Characters Equally
     * 					
     * 
     */
    @XmlEnumValue("distribute")
    DISTRIBUTE("distribute"),

    /**
     * Align to List Tab
     * 
     */
    @XmlEnumValue("numTab")
    NUM_TAB("numTab"),

    /**
     * Widest Kashida Length
     * 
     */
    @XmlEnumValue("highKashida")
    HIGH_KASHIDA("highKashida"),

    /**
     * Low Kashida Length
     * 
     */
    @XmlEnumValue("lowKashida")
    LOW_KASHIDA("lowKashida"),

    /**
     * Thai Language Justification
     * 
     */
    @XmlEnumValue("thaiDistribute")
    THAI_DISTRIBUTE("thaiDistribute");
    private final String value;

    JcEnumeration(String v) {
        value = v;
    }

    public String value() {
        return value;
    }

    public static JcEnumeration fromValue(String v) {
        for (JcEnumeration c: JcEnumeration.values()) {
            if (c.value.equals(v)) {
                return c;
            }
        }
        throw new IllegalArgumentException(v);
    }

}
