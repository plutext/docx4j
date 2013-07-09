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
 * <p>Java class for ST_LightRigType.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
 * <p>
 * <pre>
 * &lt;simpleType name="ST_LightRigType">
 *   &lt;restriction base="{http://www.w3.org/2001/XMLSchema}token">
 *     &lt;enumeration value="legacyFlat1"/>
 *     &lt;enumeration value="legacyFlat2"/>
 *     &lt;enumeration value="legacyFlat3"/>
 *     &lt;enumeration value="legacyFlat4"/>
 *     &lt;enumeration value="legacyNormal1"/>
 *     &lt;enumeration value="legacyNormal2"/>
 *     &lt;enumeration value="legacyNormal3"/>
 *     &lt;enumeration value="legacyNormal4"/>
 *     &lt;enumeration value="legacyHarsh1"/>
 *     &lt;enumeration value="legacyHarsh2"/>
 *     &lt;enumeration value="legacyHarsh3"/>
 *     &lt;enumeration value="legacyHarsh4"/>
 *     &lt;enumeration value="threePt"/>
 *     &lt;enumeration value="balanced"/>
 *     &lt;enumeration value="soft"/>
 *     &lt;enumeration value="harsh"/>
 *     &lt;enumeration value="flood"/>
 *     &lt;enumeration value="contrasting"/>
 *     &lt;enumeration value="morning"/>
 *     &lt;enumeration value="sunrise"/>
 *     &lt;enumeration value="sunset"/>
 *     &lt;enumeration value="chilly"/>
 *     &lt;enumeration value="freezing"/>
 *     &lt;enumeration value="flat"/>
 *     &lt;enumeration value="twoPt"/>
 *     &lt;enumeration value="glow"/>
 *     &lt;enumeration value="brightRoom"/>
 *   &lt;/restriction>
 * &lt;/simpleType>
 * </pre>
 * 
 */
@XmlType(name = "ST_LightRigType")
@XmlEnum
public enum STLightRigType {

    @XmlEnumValue("legacyFlat1")
    LEGACY_FLAT_1("legacyFlat1"),
    @XmlEnumValue("legacyFlat2")
    LEGACY_FLAT_2("legacyFlat2"),
    @XmlEnumValue("legacyFlat3")
    LEGACY_FLAT_3("legacyFlat3"),
    @XmlEnumValue("legacyFlat4")
    LEGACY_FLAT_4("legacyFlat4"),
    @XmlEnumValue("legacyNormal1")
    LEGACY_NORMAL_1("legacyNormal1"),
    @XmlEnumValue("legacyNormal2")
    LEGACY_NORMAL_2("legacyNormal2"),
    @XmlEnumValue("legacyNormal3")
    LEGACY_NORMAL_3("legacyNormal3"),
    @XmlEnumValue("legacyNormal4")
    LEGACY_NORMAL_4("legacyNormal4"),
    @XmlEnumValue("legacyHarsh1")
    LEGACY_HARSH_1("legacyHarsh1"),
    @XmlEnumValue("legacyHarsh2")
    LEGACY_HARSH_2("legacyHarsh2"),
    @XmlEnumValue("legacyHarsh3")
    LEGACY_HARSH_3("legacyHarsh3"),
    @XmlEnumValue("legacyHarsh4")
    LEGACY_HARSH_4("legacyHarsh4"),
    @XmlEnumValue("threePt")
    THREE_PT("threePt"),
    @XmlEnumValue("balanced")
    BALANCED("balanced"),
    @XmlEnumValue("soft")
    SOFT("soft"),
    @XmlEnumValue("harsh")
    HARSH("harsh"),
    @XmlEnumValue("flood")
    FLOOD("flood"),
    @XmlEnumValue("contrasting")
    CONTRASTING("contrasting"),
    @XmlEnumValue("morning")
    MORNING("morning"),
    @XmlEnumValue("sunrise")
    SUNRISE("sunrise"),
    @XmlEnumValue("sunset")
    SUNSET("sunset"),
    @XmlEnumValue("chilly")
    CHILLY("chilly"),
    @XmlEnumValue("freezing")
    FREEZING("freezing"),
    @XmlEnumValue("flat")
    FLAT("flat"),
    @XmlEnumValue("twoPt")
    TWO_PT("twoPt"),
    @XmlEnumValue("glow")
    GLOW("glow"),
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
