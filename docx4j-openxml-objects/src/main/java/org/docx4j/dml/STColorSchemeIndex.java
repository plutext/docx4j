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

import jakarta.xml.bind.annotation.XmlEnum;
import jakarta.xml.bind.annotation.XmlEnumValue;
import jakarta.xml.bind.annotation.XmlType;


/**
 * <p>Java class for ST_ColorSchemeIndex.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
 * <p>
 * <pre>
 * &lt;simpleType name="ST_ColorSchemeIndex"&gt;
 *   &lt;restriction base="{http://www.w3.org/2001/XMLSchema}token"&gt;
 *     &lt;enumeration value="dk1"/&gt;
 *     &lt;enumeration value="lt1"/&gt;
 *     &lt;enumeration value="dk2"/&gt;
 *     &lt;enumeration value="lt2"/&gt;
 *     &lt;enumeration value="accent1"/&gt;
 *     &lt;enumeration value="accent2"/&gt;
 *     &lt;enumeration value="accent3"/&gt;
 *     &lt;enumeration value="accent4"/&gt;
 *     &lt;enumeration value="accent5"/&gt;
 *     &lt;enumeration value="accent6"/&gt;
 *     &lt;enumeration value="hlink"/&gt;
 *     &lt;enumeration value="folHlink"/&gt;
 *   &lt;/restriction&gt;
 * &lt;/simpleType&gt;
 * </pre>
 * 
 */
@XmlType(name = "ST_ColorSchemeIndex")
@XmlEnum
public enum STColorSchemeIndex {


    /**
     * Dark 1
     * 
     */
    @XmlEnumValue("dk1")
    DK_1("dk1"),

    /**
     * Light 1
     * 
     */
    @XmlEnumValue("lt1")
    LT_1("lt1"),

    /**
     * Dark 2
     * 
     */
    @XmlEnumValue("dk2")
    DK_2("dk2"),

    /**
     * Light 2
     * 
     */
    @XmlEnumValue("lt2")
    LT_2("lt2"),

    /**
     * Accent 1
     * 
     */
    @XmlEnumValue("accent1")
    ACCENT_1("accent1"),

    /**
     * Accent 2
     * 
     */
    @XmlEnumValue("accent2")
    ACCENT_2("accent2"),

    /**
     * Accent 3
     * 
     */
    @XmlEnumValue("accent3")
    ACCENT_3("accent3"),

    /**
     * Accent 4
     * 
     */
    @XmlEnumValue("accent4")
    ACCENT_4("accent4"),

    /**
     * Accent 5
     * 
     */
    @XmlEnumValue("accent5")
    ACCENT_5("accent5"),

    /**
     * Accent 6
     * 
     */
    @XmlEnumValue("accent6")
    ACCENT_6("accent6"),

    /**
     * Hyperlink
     * 
     */
    @XmlEnumValue("hlink")
    HLINK("hlink"),

    /**
     * 
     * 						Followed Hyperlink
     * 					
     * 
     */
    @XmlEnumValue("folHlink")
    FOL_HLINK("folHlink");
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
