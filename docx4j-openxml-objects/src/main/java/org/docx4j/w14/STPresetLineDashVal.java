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
 * <p>Java class for ST_PresetLineDashVal.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
 * <p>
 * <pre>
 * &lt;simpleType name="ST_PresetLineDashVal">
 *   &lt;restriction base="{http://www.w3.org/2001/XMLSchema}string">
 *     &lt;enumeration value="solid"/>
 *     &lt;enumeration value="dot"/>
 *     &lt;enumeration value="sysDot"/>
 *     &lt;enumeration value="dash"/>
 *     &lt;enumeration value="sysDash"/>
 *     &lt;enumeration value="lgDash"/>
 *     &lt;enumeration value="dashDot"/>
 *     &lt;enumeration value="sysDashDot"/>
 *     &lt;enumeration value="lgDashDot"/>
 *     &lt;enumeration value="lgDashDotDot"/>
 *     &lt;enumeration value="sysDashDotDot"/>
 *   &lt;/restriction>
 * &lt;/simpleType>
 * </pre>
 * 
 */
@XmlType(name = "ST_PresetLineDashVal")
@XmlEnum
public enum STPresetLineDashVal {

    @XmlEnumValue("solid")
    SOLID("solid"),
    @XmlEnumValue("dot")
    DOT("dot"),
    @XmlEnumValue("sysDot")
    SYS_DOT("sysDot"),
    @XmlEnumValue("dash")
    DASH("dash"),
    @XmlEnumValue("sysDash")
    SYS_DASH("sysDash"),
    @XmlEnumValue("lgDash")
    LG_DASH("lgDash"),
    @XmlEnumValue("dashDot")
    DASH_DOT("dashDot"),
    @XmlEnumValue("sysDashDot")
    SYS_DASH_DOT("sysDashDot"),
    @XmlEnumValue("lgDashDot")
    LG_DASH_DOT("lgDashDot"),
    @XmlEnumValue("lgDashDotDot")
    LG_DASH_DOT_DOT("lgDashDotDot"),
    @XmlEnumValue("sysDashDotDot")
    SYS_DASH_DOT_DOT("sysDashDotDot");
    private final String value;

    STPresetLineDashVal(String v) {
        value = v;
    }

    public String value() {
        return value;
    }

    public static STPresetLineDashVal fromValue(String v) {
        for (STPresetLineDashVal c: STPresetLineDashVal.values()) {
            if (c.value.equals(v)) {
                return c;
            }
        }
        throw new IllegalArgumentException(v);
    }

}
