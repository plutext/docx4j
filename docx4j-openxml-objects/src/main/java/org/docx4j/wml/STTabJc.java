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
 * <p>Java class for ST_TabJc.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
 * <p>
 * <pre>
 * &lt;simpleType name="ST_TabJc">
 *   &lt;restriction base="{http://www.w3.org/2001/XMLSchema}string">
 *     &lt;enumeration value="clear"/>
 *     &lt;enumeration value="left"/>
 *     &lt;enumeration value="center"/>
 *     &lt;enumeration value="right"/>
 *     &lt;enumeration value="decimal"/>
 *     &lt;enumeration value="bar"/>
 *     &lt;enumeration value="num"/>
 *   &lt;/restriction>
 * &lt;/simpleType>
 * </pre>
 * 
 */
@XmlType(name = "ST_TabJc")
@XmlEnum
public enum STTabJc {


    /**
     * No Tab Stop
     * 
     */
    @XmlEnumValue("clear")
    CLEAR("clear"),

    /**
     * Left Tab
     * 
     */
    @XmlEnumValue("left")
    LEFT("left"),

    /**
     * Centered Tab
     * 
     */
    @XmlEnumValue("center")
    CENTER("center"),

    /**
     * Right Tab
     * 
     */
    @XmlEnumValue("right")
    RIGHT("right"),

    /**
     * Decimal Tab
     * 
     */
    @XmlEnumValue("decimal")
    DECIMAL("decimal"),

    /**
     * Bar Tab
     * 
     */
    @XmlEnumValue("bar")
    BAR("bar"),

    /**
     * List Tab
     * 
     */
    @XmlEnumValue("num")
    NUM("num");
    private final String value;

    STTabJc(String v) {
        value = v;
    }

    public String value() {
        return value;
    }

    public static STTabJc fromValue(String v) {
        for (STTabJc c: STTabJc.values()) {
            if (c.value.equals(v)) {
                return c;
            }
        }
        throw new IllegalArgumentException(v);
    }

}
