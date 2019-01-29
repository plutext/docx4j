/*
 *  Copyright 2010-2013, Plutext Pty Ltd.
 *   
 *  This file is part of xlsx4j, a component of docx4j.

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
 * <p>Java class for ST_PhoneticType.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
 * <p>
 * <pre>
 * &lt;simpleType name="ST_PhoneticType">
 *   &lt;restriction base="{http://www.w3.org/2001/XMLSchema}string">
 *     &lt;enumeration value="halfwidthKatakana"/>
 *     &lt;enumeration value="fullwidthKatakana"/>
 *     &lt;enumeration value="Hiragana"/>
 *     &lt;enumeration value="noConversion"/>
 *   &lt;/restriction>
 * &lt;/simpleType>
 * </pre>
 * 
 */
@XmlType(name = "ST_PhoneticType")
@XmlEnum
public enum STPhoneticType {

    @XmlEnumValue("halfwidthKatakana")
    HALFWIDTH_KATAKANA("halfwidthKatakana"),
    @XmlEnumValue("fullwidthKatakana")
    FULLWIDTH_KATAKANA("fullwidthKatakana"),
    @XmlEnumValue("Hiragana")
    HIRAGANA("Hiragana"),
    @XmlEnumValue("noConversion")
    NO_CONVERSION("noConversion");
    private final String value;

    STPhoneticType(String v) {
        value = v;
    }

    public String value() {
        return value;
    }

    public static STPhoneticType fromValue(String v) {
        for (STPhoneticType c: STPhoneticType.values()) {
            if (c.value.equals(v)) {
                return c;
            }
        }
        throw new IllegalArgumentException(v);
    }

}
