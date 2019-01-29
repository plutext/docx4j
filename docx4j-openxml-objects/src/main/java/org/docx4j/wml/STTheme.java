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
 * <p>Java class for ST_Theme.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
 * <p>
 * <pre>
 * &lt;simpleType name="ST_Theme">
 *   &lt;restriction base="{http://www.w3.org/2001/XMLSchema}string">
 *     &lt;enumeration value="majorEastAsia"/>
 *     &lt;enumeration value="majorBidi"/>
 *     &lt;enumeration value="majorAscii"/>
 *     &lt;enumeration value="majorHAnsi"/>
 *     &lt;enumeration value="minorEastAsia"/>
 *     &lt;enumeration value="minorBidi"/>
 *     &lt;enumeration value="minorAscii"/>
 *     &lt;enumeration value="minorHAnsi"/>
 *   &lt;/restriction>
 * &lt;/simpleType>
 * </pre>
 * 
 */
@XmlType(name = "ST_Theme")
@XmlEnum
public enum STTheme {


    /**
     * Major East Asian Theme
     * 						Font
     * 
     */
    @XmlEnumValue("majorEastAsia")
    MAJOR_EAST_ASIA("majorEastAsia"),

    /**
     * Major Complex Script Theme
     * 						Font
     * 
     */
    @XmlEnumValue("majorBidi")
    MAJOR_BIDI("majorBidi"),

    /**
     * Major ASCII Theme Font
     * 
     */
    @XmlEnumValue("majorAscii")
    MAJOR_ASCII("majorAscii"),

    /**
     * Major High ANSI Theme
     * 						Font
     * 
     */
    @XmlEnumValue("majorHAnsi")
    MAJOR_H_ANSI("majorHAnsi"),

    /**
     * Minor East Asian Theme
     * 						Font
     * 
     */
    @XmlEnumValue("minorEastAsia")
    MINOR_EAST_ASIA("minorEastAsia"),

    /**
     * Minor Complex Script Theme
     * 						Font
     * 
     */
    @XmlEnumValue("minorBidi")
    MINOR_BIDI("minorBidi"),

    /**
     * Minor ASCII Theme Font
     * 
     */
    @XmlEnumValue("minorAscii")
    MINOR_ASCII("minorAscii"),

    /**
     * Minor High ANSI Theme
     * 						Font
     * 
     */
    @XmlEnumValue("minorHAnsi")
    MINOR_H_ANSI("minorHAnsi");
    private final String value;

    STTheme(String v) {
        value = v;
    }

    public String value() {
        return value;
    }

    public static STTheme fromValue(String v) {
        for (STTheme c: STTheme.values()) {
            if (c.value.equals(v)) {
                return c;
            }
        }
        throw new IllegalArgumentException(v);
    }

}
