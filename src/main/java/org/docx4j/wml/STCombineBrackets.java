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
 * <p>Java class for ST_CombineBrackets.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
 * <p>
 * <pre>
 * &lt;simpleType name="ST_CombineBrackets">
 *   &lt;restriction base="{http://www.w3.org/2001/XMLSchema}string">
 *     &lt;enumeration value="none"/>
 *     &lt;enumeration value="round"/>
 *     &lt;enumeration value="square"/>
 *     &lt;enumeration value="angle"/>
 *     &lt;enumeration value="curly"/>
 *   &lt;/restriction>
 * &lt;/simpleType>
 * </pre>
 * 
 */
@XmlType(name = "ST_CombineBrackets")
@XmlEnum
public enum STCombineBrackets {


    /**
     * No Enclosing Brackets
     * 
     */
    @XmlEnumValue("none")
    NONE("none"),

    /**
     * Round Brackets
     * 
     */
    @XmlEnumValue("round")
    ROUND("round"),

    /**
     * Square Brackets
     * 
     */
    @XmlEnumValue("square")
    SQUARE("square"),

    /**
     * Angle Brackets
     * 
     */
    @XmlEnumValue("angle")
    ANGLE("angle"),

    /**
     * Curly Brackets
     * 
     */
    @XmlEnumValue("curly")
    CURLY("curly");
    private final String value;

    STCombineBrackets(String v) {
        value = v;
    }

    public String value() {
        return value;
    }

    public static STCombineBrackets fromValue(String v) {
        for (STCombineBrackets c: STCombineBrackets.values()) {
            if (c.value.equals(v)) {
                return c;
            }
        }
        throw new IllegalArgumentException(v);
    }

}
