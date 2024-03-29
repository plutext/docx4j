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

import jakarta.xml.bind.annotation.XmlEnum;
import jakarta.xml.bind.annotation.XmlEnumValue;
import jakarta.xml.bind.annotation.XmlType;


/**
 * <p>Java class for ST_TabTlc.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
 * <p>
 * <pre>
 * &lt;simpleType name="ST_TabTlc">
 *   &lt;restriction base="{http://www.w3.org/2001/XMLSchema}string">
 *     &lt;enumeration value="none"/>
 *     &lt;enumeration value="dot"/>
 *     &lt;enumeration value="hyphen"/>
 *     &lt;enumeration value="underscore"/>
 *     &lt;enumeration value="heavy"/>
 *     &lt;enumeration value="middleDot"/>
 *   &lt;/restriction>
 * &lt;/simpleType>
 * </pre>
 * 
 */
@XmlType(name = "ST_TabTlc")
@XmlEnum
public enum STTabTlc {


    /**
     * No tab stop leader
     * 
     */
    @XmlEnumValue("none")
    NONE("none"),

    /**
     * Dotted leader line
     * 
     */
    @XmlEnumValue("dot")
    DOT("dot"),

    /**
     * Dashed tab stop leader
     * 						line
     * 
     */
    @XmlEnumValue("hyphen")
    HYPHEN("hyphen"),

    /**
     * Solid leader line
     * 
     */
    @XmlEnumValue("underscore")
    UNDERSCORE("underscore"),

    /**
     * Heavy solid leader line
     * 
     */
    @XmlEnumValue("heavy")
    HEAVY("heavy"),

    /**
     * Middle dot leader line
     * 
     */
    @XmlEnumValue("middleDot")
    MIDDLE_DOT("middleDot");
    private final String value;

    STTabTlc(String v) {
        value = v;
    }

    public String value() {
        return value;
    }

    public static STTabTlc fromValue(String v) {
        for (STTabTlc c: STTabTlc.values()) {
            if (c.value.equals(v)) {
                return c;
            }
        }
        throw new IllegalArgumentException(v);
    }

}
