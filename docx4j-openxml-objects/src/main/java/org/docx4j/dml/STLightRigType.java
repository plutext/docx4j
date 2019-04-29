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

import javax.xml.bind.annotation.XmlEnum;
import javax.xml.bind.annotation.XmlEnumValue;
import javax.xml.bind.annotation.XmlType;


/**
 * <p>Java class for ST_LightRigType.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
 * <p>
 * <pre>
 * &lt;simpleType name="ST_LightRigType"&gt;
 *   &lt;restriction base="{http://www.w3.org/2001/XMLSchema}token"&gt;
 *     &lt;enumeration value="legacyFlat1"/&gt;
 *     &lt;enumeration value="legacyFlat2"/&gt;
 *     &lt;enumeration value="legacyFlat3"/&gt;
 *     &lt;enumeration value="legacyFlat4"/&gt;
 *     &lt;enumeration value="legacyNormal1"/&gt;
 *     &lt;enumeration value="legacyNormal2"/&gt;
 *     &lt;enumeration value="legacyNormal3"/&gt;
 *     &lt;enumeration value="legacyNormal4"/&gt;
 *     &lt;enumeration value="legacyHarsh1"/&gt;
 *     &lt;enumeration value="legacyHarsh2"/&gt;
 *     &lt;enumeration value="legacyHarsh3"/&gt;
 *     &lt;enumeration value="legacyHarsh4"/&gt;
 *     &lt;enumeration value="threePt"/&gt;
 *     &lt;enumeration value="balanced"/&gt;
 *     &lt;enumeration value="soft"/&gt;
 *     &lt;enumeration value="harsh"/&gt;
 *     &lt;enumeration value="flood"/&gt;
 *     &lt;enumeration value="contrasting"/&gt;
 *     &lt;enumeration value="morning"/&gt;
 *     &lt;enumeration value="sunrise"/&gt;
 *     &lt;enumeration value="sunset"/&gt;
 *     &lt;enumeration value="chilly"/&gt;
 *     &lt;enumeration value="freezing"/&gt;
 *     &lt;enumeration value="flat"/&gt;
 *     &lt;enumeration value="twoPt"/&gt;
 *     &lt;enumeration value="glow"/&gt;
 *     &lt;enumeration value="brightRoom"/&gt;
 *   &lt;/restriction&gt;
 * &lt;/simpleType&gt;
 * </pre>
 * 
 */
@XmlType(name = "ST_LightRigType")
@XmlEnum
public enum STLightRigType {


    /**
     * Legacy Flat 1
     * 
     */
    @XmlEnumValue("legacyFlat1")
    LEGACY_FLAT_1("legacyFlat1"),

    /**
     * Legacy Flat 2
     * 
     */
    @XmlEnumValue("legacyFlat2")
    LEGACY_FLAT_2("legacyFlat2"),

    /**
     * Legacy Flat 3
     * 
     */
    @XmlEnumValue("legacyFlat3")
    LEGACY_FLAT_3("legacyFlat3"),

    /**
     * Legacy Flat 4
     * 
     */
    @XmlEnumValue("legacyFlat4")
    LEGACY_FLAT_4("legacyFlat4"),

    /**
     * Legacy Normal 1
     * 
     */
    @XmlEnumValue("legacyNormal1")
    LEGACY_NORMAL_1("legacyNormal1"),

    /**
     * Legacy Normal 2
     * 
     */
    @XmlEnumValue("legacyNormal2")
    LEGACY_NORMAL_2("legacyNormal2"),

    /**
     * Legacy Normal 3
     * 
     */
    @XmlEnumValue("legacyNormal3")
    LEGACY_NORMAL_3("legacyNormal3"),

    /**
     * Legacy Normal 4
     * 
     */
    @XmlEnumValue("legacyNormal4")
    LEGACY_NORMAL_4("legacyNormal4"),

    /**
     * Legacy Harsh 1
     * 
     */
    @XmlEnumValue("legacyHarsh1")
    LEGACY_HARSH_1("legacyHarsh1"),

    /**
     * Legacy Harsh 2
     * 
     */
    @XmlEnumValue("legacyHarsh2")
    LEGACY_HARSH_2("legacyHarsh2"),

    /**
     * Legacy Harsh 3
     * 
     */
    @XmlEnumValue("legacyHarsh3")
    LEGACY_HARSH_3("legacyHarsh3"),

    /**
     * Legacy Harsh 4
     * 
     */
    @XmlEnumValue("legacyHarsh4")
    LEGACY_HARSH_4("legacyHarsh4"),

    /**
     * Three Point
     * 
     */
    @XmlEnumValue("threePt")
    THREE_PT("threePt"),

    /**
     * Light Rig Enum ( Balanced )
     * 
     */
    @XmlEnumValue("balanced")
    BALANCED("balanced"),

    /**
     * Soft
     * 
     */
    @XmlEnumValue("soft")
    SOFT("soft"),

    /**
     * Harsh
     * 
     */
    @XmlEnumValue("harsh")
    HARSH("harsh"),

    /**
     * Flood
     * 
     */
    @XmlEnumValue("flood")
    FLOOD("flood"),

    /**
     * Contrasting
     * 
     */
    @XmlEnumValue("contrasting")
    CONTRASTING("contrasting"),

    /**
     * Morning
     * 
     */
    @XmlEnumValue("morning")
    MORNING("morning"),

    /**
     * Sunrise
     * 
     */
    @XmlEnumValue("sunrise")
    SUNRISE("sunrise"),

    /**
     * Sunset
     * 
     */
    @XmlEnumValue("sunset")
    SUNSET("sunset"),

    /**
     * Chilly
     * 
     */
    @XmlEnumValue("chilly")
    CHILLY("chilly"),

    /**
     * Freezing
     * 
     */
    @XmlEnumValue("freezing")
    FREEZING("freezing"),

    /**
     * Flat
     * 
     */
    @XmlEnumValue("flat")
    FLAT("flat"),

    /**
     * Two Point
     * 
     */
    @XmlEnumValue("twoPt")
    TWO_PT("twoPt"),

    /**
     * Glow
     * 
     */
    @XmlEnumValue("glow")
    GLOW("glow"),

    /**
     * Bright Room
     * 
     */
    @XmlEnumValue("brightRoom")
    BRIGHT_ROOM("brightRoom");
    private final String value;

    STLightRigType(String v) {
        value = v;
    }

    public String value() {
        return value;
    }

    public static STLightRigType fromValue(String v) {
        for (STLightRigType c: STLightRigType.values()) {
            if (c.value.equals(v)) {
                return c;
            }
        }
        throw new IllegalArgumentException(v);
    }

}
