/*
 *  Copyright 2010, Plutext Pty Ltd.
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


package org.xlsx4j.sml;

import javax.xml.bind.annotation.XmlEnum;
import javax.xml.bind.annotation.XmlEnumValue;
import javax.xml.bind.annotation.XmlType;


/**
 * <p>Java class for ST_CalendarType.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
 * <p>
 * <pre>
 * &lt;simpleType name="ST_CalendarType">
 *   &lt;restriction base="{http://www.w3.org/2001/XMLSchema}string">
 *     &lt;enumeration value="none"/>
 *     &lt;enumeration value="gregorian"/>
 *     &lt;enumeration value="gregorianUs"/>
 *     &lt;enumeration value="japan"/>
 *     &lt;enumeration value="taiwan"/>
 *     &lt;enumeration value="korea"/>
 *     &lt;enumeration value="hijri"/>
 *     &lt;enumeration value="thai"/>
 *     &lt;enumeration value="hebrew"/>
 *     &lt;enumeration value="gregorianMeFrench"/>
 *     &lt;enumeration value="gregorianArabic"/>
 *     &lt;enumeration value="gregorianXlitEnglish"/>
 *     &lt;enumeration value="gregorianXlitFrench"/>
 *   &lt;/restriction>
 * &lt;/simpleType>
 * </pre>
 * 
 */
@XmlType(name = "ST_CalendarType")
@XmlEnum
public enum STCalendarType {


    /**
     * No Calendar Type
     * 
     */
    @XmlEnumValue("none")
    NONE("none"),

    /**
     * Gregorian
     * 
     */
    @XmlEnumValue("gregorian")
    GREGORIAN("gregorian"),

    /**
     * Gregorian (U.S.) Calendar
     * 
     */
    @XmlEnumValue("gregorianUs")
    GREGORIAN_US("gregorianUs"),

    /**
     * Japanese Emperor Era Calendar
     * 
     */
    @XmlEnumValue("japan")
    JAPAN("japan"),

    /**
     * Taiwan Era Calendar
     * 
     */
    @XmlEnumValue("taiwan")
    TAIWAN("taiwan"),

    /**
     * Korean Tangun Era Calendar
     * 
     */
    @XmlEnumValue("korea")
    KOREA("korea"),

    /**
     * Hijri (Arabic Lunar) Calendar
     * 
     */
    @XmlEnumValue("hijri")
    HIJRI("hijri"),

    /**
     * Thai Calendar
     * 
     */
    @XmlEnumValue("thai")
    THAI("thai"),

    /**
     * Hebrew (Lunar) Calendar
     * 
     */
    @XmlEnumValue("hebrew")
    HEBREW("hebrew"),

    /**
     * Gregorian Middle East French Calendar
     * 
     */
    @XmlEnumValue("gregorianMeFrench")
    GREGORIAN_ME_FRENCH("gregorianMeFrench"),

    /**
     * Gregorian Arabic Calendar
     * 
     */
    @XmlEnumValue("gregorianArabic")
    GREGORIAN_ARABIC("gregorianArabic"),

    /**
     * Gregorian Transliterated English Calendar
     * 
     */
    @XmlEnumValue("gregorianXlitEnglish")
    GREGORIAN_XLIT_ENGLISH("gregorianXlitEnglish"),

    /**
     * Gregorian Transliterated French Calendar
     * 
     */
    @XmlEnumValue("gregorianXlitFrench")
    GREGORIAN_XLIT_FRENCH("gregorianXlitFrench");
    private final String value;

    STCalendarType(String v) {
        value = v;
    }

    public String value() {
        return value;
    }

    public static STCalendarType fromValue(String v) {
        for (STCalendarType c: STCalendarType.values()) {
            if (c.value.equals(v)) {
                return c;
            }
        }
        throw new IllegalArgumentException(v);
    }

}
