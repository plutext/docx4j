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
 * <p>Java class for ST_ThemeColor.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
 * <p>
 * <pre>
 * &lt;simpleType name="ST_ThemeColor">
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
 *     &lt;enumeration value="none"/>
 *     &lt;enumeration value="background1"/>
 *     &lt;enumeration value="text1"/>
 *     &lt;enumeration value="background2"/>
 *     &lt;enumeration value="text2"/>
 *   &lt;/restriction>
 * &lt;/simpleType>
 * </pre>
 * 
 */
@XmlType(name = "ST_ThemeColor")
@XmlEnum
public enum STThemeColor {


    /**
     * Dark 1 Theme Color
     * 
     */
    @XmlEnumValue("dark1")
    DARK_1("dark1"),

    /**
     * Light 1 Theme Color
     * 
     */
    @XmlEnumValue("light1")
    LIGHT_1("light1"),

    /**
     * Dark 2 Theme Color
     * 
     */
    @XmlEnumValue("dark2")
    DARK_2("dark2"),

    /**
     * Light 2 Theme Color
     * 
     */
    @XmlEnumValue("light2")
    LIGHT_2("light2"),

    /**
     * Accent 1 Theme Color
     * 
     */
    @XmlEnumValue("accent1")
    ACCENT_1("accent1"),

    /**
     * Accent 2 Theme Color
     * 
     */
    @XmlEnumValue("accent2")
    ACCENT_2("accent2"),

    /**
     * Accent 3 Theme Color
     * 
     */
    @XmlEnumValue("accent3")
    ACCENT_3("accent3"),

    /**
     * Accent 4 Theme Color
     * 
     */
    @XmlEnumValue("accent4")
    ACCENT_4("accent4"),

    /**
     * Accent 5 Theme Color
     * 
     */
    @XmlEnumValue("accent5")
    ACCENT_5("accent5"),

    /**
     * Accent 6 Theme Color
     * 
     */
    @XmlEnumValue("accent6")
    ACCENT_6("accent6"),

    /**
     * Hyperlink Theme Color
     * 
     */
    @XmlEnumValue("hyperlink")
    HYPERLINK("hyperlink"),

    /**
     * Followed Hyperlink Theme
     * 						Color
     * 
     */
    @XmlEnumValue("followedHyperlink")
    FOLLOWED_HYPERLINK("followedHyperlink"),

    /**
     * No Theme Color
     * 
     */
    @XmlEnumValue("none")
    NONE("none"),

    /**
     * Background 1 Theme Color
     * 
     */
    @XmlEnumValue("background1")
    BACKGROUND_1("background1"),

    /**
     * Text 1 Theme Color
     * 
     */
    @XmlEnumValue("text1")
    TEXT_1("text1"),

    /**
     * Background 2 Theme Color
     * 
     */
    @XmlEnumValue("background2")
    BACKGROUND_2("background2"),

    /**
     * Text 2 Theme Color
     * 
     */
    @XmlEnumValue("text2")
    TEXT_2("text2");
    private final String value;

    STThemeColor(String v) {
        value = v;
    }

    public String value() {
        return value;
    }

    public static STThemeColor fromValue(String v) {
        for (STThemeColor c: STThemeColor.values()) {
            if (c.value.equals(v)) {
                return c;
            }
        }
        throw new IllegalArgumentException(v);
    }

}
