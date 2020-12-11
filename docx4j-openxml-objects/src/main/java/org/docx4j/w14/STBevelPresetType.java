/*
 *  Copyright 2013, Plutext Pty Ltd.
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


package org.docx4j.w14;

import javax.xml.bind.annotation.XmlEnum;
import javax.xml.bind.annotation.XmlEnumValue;
import javax.xml.bind.annotation.XmlType;


/**
 * <p>Java class for ST_BevelPresetType.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
 * <p>
 * <pre>
 * &lt;simpleType name="ST_BevelPresetType">
 *   &lt;restriction base="{http://www.w3.org/2001/XMLSchema}token">
 *     &lt;enumeration value="relaxedInset"/>
 *     &lt;enumeration value="circle"/>
 *     &lt;enumeration value="slope"/>
 *     &lt;enumeration value="cross"/>
 *     &lt;enumeration value="angle"/>
 *     &lt;enumeration value="softRound"/>
 *     &lt;enumeration value="convex"/>
 *     &lt;enumeration value="coolSlant"/>
 *     &lt;enumeration value="divot"/>
 *     &lt;enumeration value="riblet"/>
 *     &lt;enumeration value="hardEdge"/>
 *     &lt;enumeration value="artDeco"/>
 *   &lt;/restriction>
 * &lt;/simpleType>
 * </pre>
 * 
 */
@XmlType(name = "ST_BevelPresetType")
@XmlEnum
public enum STBevelPresetType {

    @XmlEnumValue("relaxedInset")
    RELAXED_INSET("relaxedInset"),
    @XmlEnumValue("circle")
    CIRCLE("circle"),
    @XmlEnumValue("slope")
    SLOPE("slope"),
    @XmlEnumValue("cross")
    CROSS("cross"),
    @XmlEnumValue("angle")
    ANGLE("angle"),
    @XmlEnumValue("softRound")
    SOFT_ROUND("softRound"),
    @XmlEnumValue("convex")
    CONVEX("convex"),
    @XmlEnumValue("coolSlant")
    COOL_SLANT("coolSlant"),
    @XmlEnumValue("divot")
    DIVOT("divot"),
    @XmlEnumValue("riblet")
    RIBLET("riblet"),
    @XmlEnumValue("hardEdge")
    HARD_EDGE("hardEdge"),
    @XmlEnumValue("artDeco")
    ART_DECO("artDeco");
    private final String value;

    STBevelPresetType(String v) {
        value = v;
    }

    public String value() {
        return value;
    }

    public static STBevelPresetType fromValue(String v) {
        for (STBevelPresetType c: STBevelPresetType.values()) {
            if (c.value.equals(v)) {
                return c;
            }
        }
        throw new IllegalArgumentException(v);
    }

}
