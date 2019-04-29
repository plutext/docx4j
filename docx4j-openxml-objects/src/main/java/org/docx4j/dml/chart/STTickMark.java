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
 * <p>Java class for ST_TickMark.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
 * <p>
 * <pre>
 * &lt;simpleType name="ST_TickMark"&gt;
 *   &lt;restriction base="{http://www.w3.org/2001/XMLSchema}string"&gt;
 *     &lt;enumeration value="cross"/&gt;
 *     &lt;enumeration value="in"/&gt;
 *     &lt;enumeration value="none"/&gt;
 *     &lt;enumeration value="out"/&gt;
 *   &lt;/restriction&gt;
 * &lt;/simpleType&gt;
 * </pre>
 * 
 */
@XmlType(name = "ST_TickMark")
@XmlEnum
public enum STTickMark {


    /**
     * Cross
     * 
     */
    @XmlEnumValue("cross")
    CROSS("cross"),

    /**
     * Inside
     * 
     */
    @XmlEnumValue("in")
    IN("in"),

    /**
     * None
     * 
     */
    @XmlEnumValue("none")
    NONE("none"),

    /**
     * Outside
     * 
     */
    @XmlEnumValue("out")
    OUT("out");
    private final String value;

    STTickMark(String v) {
        value = v;
    }

    public String value() {
        return value;
    }

    public static STTickMark fromValue(String v) {
        for (STTickMark c: STTickMark.values()) {
            if (c.value.equals(v)) {
                return c;
            }
        }
        throw new IllegalArgumentException(v);
    }

}
