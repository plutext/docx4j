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


package org.docx4j.dml.chart;

import javax.xml.bind.annotation.XmlEnum;
import javax.xml.bind.annotation.XmlEnumValue;
import javax.xml.bind.annotation.XmlType;


/**
 * <p>Java class for ST_BuiltInUnit.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
 * <p>
 * <pre>
 * &lt;simpleType name="ST_BuiltInUnit"&gt;
 *   &lt;restriction base="{http://www.w3.org/2001/XMLSchema}string"&gt;
 *     &lt;enumeration value="hundreds"/&gt;
 *     &lt;enumeration value="thousands"/&gt;
 *     &lt;enumeration value="tenThousands"/&gt;
 *     &lt;enumeration value="hundredThousands"/&gt;
 *     &lt;enumeration value="millions"/&gt;
 *     &lt;enumeration value="tenMillions"/&gt;
 *     &lt;enumeration value="hundredMillions"/&gt;
 *     &lt;enumeration value="billions"/&gt;
 *     &lt;enumeration value="trillions"/&gt;
 *   &lt;/restriction&gt;
 * &lt;/simpleType&gt;
 * </pre>
 * 
 */
@XmlType(name = "ST_BuiltInUnit")
@XmlEnum
public enum STBuiltInUnit {


    /**
     * Hundreds
     * 
     */
    @XmlEnumValue("hundreds")
    HUNDREDS("hundreds"),

    /**
     * Thousands
     * 
     */
    @XmlEnumValue("thousands")
    THOUSANDS("thousands"),

    /**
     * Ten Thousands
     * 
     */
    @XmlEnumValue("tenThousands")
    TEN_THOUSANDS("tenThousands"),

    /**
     * Hundred Thousands
     * 
     */
    @XmlEnumValue("hundredThousands")
    HUNDRED_THOUSANDS("hundredThousands"),

    /**
     * Millions
     * 
     */
    @XmlEnumValue("millions")
    MILLIONS("millions"),

    /**
     * Ten Millions
     * 
     */
    @XmlEnumValue("tenMillions")
    TEN_MILLIONS("tenMillions"),

    /**
     * Hundred Millions
     * 
     */
    @XmlEnumValue("hundredMillions")
    HUNDRED_MILLIONS("hundredMillions"),

    /**
     * Billions
     * 
     */
    @XmlEnumValue("billions")
    BILLIONS("billions"),

    /**
     * Trillions
     * 
     */
    @XmlEnumValue("trillions")
    TRILLIONS("trillions");
    private final String value;

    STBuiltInUnit(String v) {
        value = v;
    }

    public String value() {
        return value;
    }

    public static STBuiltInUnit fromValue(String v) {
        for (STBuiltInUnit c: STBuiltInUnit.values()) {
            if (c.value.equals(v)) {
                return c;
            }
        }
        throw new IllegalArgumentException(v);
    }

}
