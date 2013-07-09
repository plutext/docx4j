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
 * <p>Java class for ST_RubyAlign.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
 * <p>
 * <pre>
 * &lt;simpleType name="ST_RubyAlign">
 *   &lt;restriction base="{http://www.w3.org/2001/XMLSchema}string">
 *     &lt;enumeration value="center"/>
 *     &lt;enumeration value="distributeLetter"/>
 *     &lt;enumeration value="distributeSpace"/>
 *     &lt;enumeration value="left"/>
 *     &lt;enumeration value="right"/>
 *     &lt;enumeration value="rightVertical"/>
 *   &lt;/restriction>
 * &lt;/simpleType>
 * </pre>
 * 
 */
@XmlType(name = "ST_RubyAlign")
@XmlEnum
public enum STRubyAlign {


    /**
     * Center
     * 
     */
    @XmlEnumValue("center")
    CENTER("center"),

    /**
     * Distribute All
     * 						Characters
     * 
     */
    @XmlEnumValue("distributeLetter")
    DISTRIBUTE_LETTER("distributeLetter"),

    /**
     * Distribute all Characters w/ Additional Space
     * 						On Either Side
     * 
     */
    @XmlEnumValue("distributeSpace")
    DISTRIBUTE_SPACE("distributeSpace"),

    /**
     * Left Aligned
     * 
     */
    @XmlEnumValue("left")
    LEFT("left"),

    /**
     * Right Aligned
     * 
     */
    @XmlEnumValue("right")
    RIGHT("right"),

    /**
     * Vertically Aligned to Right of Base
     * 						Text
     * 
     */
    @XmlEnumValue("rightVertical")
    RIGHT_VERTICAL("rightVertical");
    private final String value;

    STRubyAlign(String v) {
        value = v;
    }

    public String value() {
        return value;
    }

    public static STRubyAlign fromValue(String v) {
        for (STRubyAlign c: STRubyAlign.values()) {
            if (c.value.equals(v)) {
                return c;
            }
        }
        throw new IllegalArgumentException(v);
    }

}
