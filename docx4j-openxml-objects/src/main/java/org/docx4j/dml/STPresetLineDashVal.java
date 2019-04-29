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
 * <p>Java class for ST_PresetLineDashVal.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
 * <p>
 * <pre>
 * &lt;simpleType name="ST_PresetLineDashVal"&gt;
 *   &lt;restriction base="{http://www.w3.org/2001/XMLSchema}token"&gt;
 *     &lt;enumeration value="solid"/&gt;
 *     &lt;enumeration value="dot"/&gt;
 *     &lt;enumeration value="dash"/&gt;
 *     &lt;enumeration value="lgDash"/&gt;
 *     &lt;enumeration value="dashDot"/&gt;
 *     &lt;enumeration value="lgDashDot"/&gt;
 *     &lt;enumeration value="lgDashDotDot"/&gt;
 *     &lt;enumeration value="sysDash"/&gt;
 *     &lt;enumeration value="sysDot"/&gt;
 *     &lt;enumeration value="sysDashDot"/&gt;
 *     &lt;enumeration value="sysDashDotDot"/&gt;
 *   &lt;/restriction&gt;
 * &lt;/simpleType&gt;
 * </pre>
 * 
 */
@XmlType(name = "ST_PresetLineDashVal")
@XmlEnum
public enum STPresetLineDashVal {


    /**
     * Solid
     * 
     */
    @XmlEnumValue("solid")
    SOLID("solid"),

    /**
     * Dot
     * 
     */
    @XmlEnumValue("dot")
    DOT("dot"),

    /**
     * Dash
     * 
     */
    @XmlEnumValue("dash")
    DASH("dash"),

    /**
     * Large Dash
     * 
     */
    @XmlEnumValue("lgDash")
    LG_DASH("lgDash"),

    /**
     * Dash Dot
     * 
     */
    @XmlEnumValue("dashDot")
    DASH_DOT("dashDot"),

    /**
     * Large Dash Dot
     * 
     */
    @XmlEnumValue("lgDashDot")
    LG_DASH_DOT("lgDashDot"),

    /**
     * Large Dash Dot Dot
     * 
     */
    @XmlEnumValue("lgDashDotDot")
    LG_DASH_DOT_DOT("lgDashDotDot"),

    /**
     * System Dash
     * 
     */
    @XmlEnumValue("sysDash")
    SYS_DASH("sysDash"),

    /**
     * System Dot
     * 
     */
    @XmlEnumValue("sysDot")
    SYS_DOT("sysDot"),

    /**
     * System Dash Dot
     * 
     */
    @XmlEnumValue("sysDashDot")
    SYS_DASH_DOT("sysDashDot"),

    /**
     * System Dash Dot Dot
     * 
     */
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
