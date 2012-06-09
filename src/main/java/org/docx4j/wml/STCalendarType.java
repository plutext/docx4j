/*
 * Copyright 2012 Plutext Pty Ltd.
 * 
 * This file is part of docx4j.
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
 
package org.docx4j.wml;

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
 *     &lt;enumeration value="gregorian"/>
 *     &lt;enumeration value="hijri"/>
 *     &lt;enumeration value="hebrew"/>
 *     &lt;enumeration value="taiwan"/>
 *     &lt;enumeration value="japan"/>
 *     &lt;enumeration value="thai"/>
 *     &lt;enumeration value="korea"/>
 *     &lt;enumeration value="saka"/>
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
     * Gregorian
     * 
     */
    @XmlEnumValue("gregorian")
    GREGORIAN("gregorian"),

    /**
     * Hijri
     * 
     */
    @XmlEnumValue("hijri")
    HIJRI("hijri"),

    /**
     * Hebrew
     * 
     */
    @XmlEnumValue("hebrew")
    HEBREW("hebrew"),

    /**
     * Taiwan
     * 
     */
    @XmlEnumValue("taiwan")
    TAIWAN("taiwan"),

    /**
     * Japanese Emperor Era
     * 
     */
    @XmlEnumValue("japan")
    JAPAN("japan"),

    /**
     * Thai
     * 
     */
    @XmlEnumValue("thai")
    THAI("thai"),

    /**
     * Korean Tangun Era
     * 
     */
    @XmlEnumValue("korea")
    KOREA("korea"),

    /**
     * Saka Era
     * 
     */
    @XmlEnumValue("saka")
    SAKA("saka"),

    /**
     * Gregorian transliterated
     * 						English
     * 
     */
    @XmlEnumValue("gregorianXlitEnglish")
    GREGORIAN_XLIT_ENGLISH("gregorianXlitEnglish"),

    /**
     * Gregorian transliterated
     * 						French
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
