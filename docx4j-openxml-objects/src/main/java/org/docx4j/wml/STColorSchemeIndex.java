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
 * <p>Java class for ST_ColorSchemeIndex.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
 * <p>
 * <pre>
 * &lt;simpleType name="ST_ColorSchemeIndex">
 *   &lt;restriction base="{http://www.w3.org/2001/XMLSchema}string">
 *     &lt;enumeration value="dark1"/>
 *     &lt;enumeration value="light1"/>
 *     &lt;enumeration value="dark2"/>
 *     &lt;enumeration value="light2"/>
 *     &lt;enumeration value="accent1"/>
 *     &lt;enumeration value="accent2"/>
 *     &lt;enumeration value="accent3"/>
 *     &lt;enumeration value="accent4"/>
 *     &lt;enumeration value="accent5"/>
 *     &lt;enumeration value="accent6"/>
 *     &lt;enumeration value="hyperlink"/>
 *     &lt;enumeration value="followedHyperlink"/>
 *   &lt;/restriction>
 * &lt;/simpleType>
 * </pre>
 * 
 */
@XmlType(name = "ST_ColorSchemeIndex")
@XmlEnum
public enum STColorSchemeIndex {


    /**
     * Dark 1 Theme Color
     * 						Reference
     * 
     */
    @XmlEnumValue("dark1")
    DARK_1("dark1"),

    /**
     * Light 1 Theme Color
     * 						Reference
     * 
     */
    @XmlEnumValue("light1")
    LIGHT_1("light1"),

    /**
     * Dark 2 Theme Color
     * 						Reference
     * 
     */
    @XmlEnumValue("dark2")
    DARK_2("dark2"),

    /**
     * Light 2 Theme Color
     * 						Reference
     * 
     */
    @XmlEnumValue("light2")
    LIGHT_2("light2"),

    /**
     * Accent 1 Theme Color
     * 						Reference
     * 
     */
    @XmlEnumValue("accent1")
    ACCENT_1("accent1"),

    /**
     * Accent 2 Theme Color
     * 						Reference
     * 
     */
    @XmlEnumValue("accent2")
    ACCENT_2("accent2"),

    /**
     * Accent 3 Theme Color
     * 						Reference
     * 
     */
    @XmlEnumValue("accent3")
    ACCENT_3("accent3"),

    /**
     * Accent4 Theme Color
     * 						Reference
     * 
     */
    @XmlEnumValue("accent4")
    ACCENT_4("accent4"),

    /**
     * Accent5 Theme Color
     * 						Reference
     * 
     */
    @XmlEnumValue("accent5")
    ACCENT_5("accent5"),

    /**
     * Accent 6 Theme Color
     * 						Reference
     * 
     */
    @XmlEnumValue("accent6")
    ACCENT_6("accent6"),

    /**
     * Hyperlink Theme Color
     * 						Reference
     * 
     */
    @XmlEnumValue("hyperlink")
    HYPERLINK("hyperlink"),

    /**
     * Followed Hyperlink Theme Color
     * 						Reference
     * 
     */
    @XmlEnumValue("followedHyperlink")
    FOLLOWED_HYPERLINK("followedHyperlink");
    private final String value;

    STColorSchemeIndex(String v) {
        value = v;
    }

    public String value() {
        return value;
    }

    public static STColorSchemeIndex fromValue(String v) {
        for (STColorSchemeIndex c: STColorSchemeIndex.values()) {
            if (c.value.equals(v)) {
                return c;
            }
        }
        throw new IllegalArgumentException(v);
    }

}
