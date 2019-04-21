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


package org.docx4j.vml;

import javax.xml.bind.annotation.XmlEnum;
import javax.xml.bind.annotation.XmlEnumValue;
import javax.xml.bind.annotation.XmlType;


/**
 * <p>Java class for ST_StrokeLineStyle.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
 * <p>
 * <pre>
 * &lt;simpleType name="ST_StrokeLineStyle">
 *   &lt;restriction base="{http://www.w3.org/2001/XMLSchema}string">
 *     &lt;enumeration value="single"/>
 *     &lt;enumeration value="thinThin"/>
 *     &lt;enumeration value="thinThick"/>
 *     &lt;enumeration value="thickThin"/>
 *     &lt;enumeration value="thickBetweenThin"/>
 *   &lt;/restriction>
 * &lt;/simpleType>
 * </pre>
 * 
 */
@XmlType(name = "ST_StrokeLineStyle")
@XmlEnum
public enum STStrokeLineStyle {


    /**
     * Single Line
     * 
     */
    @XmlEnumValue("single")
    SINGLE("single"),

    /**
     * Two Thin Lines
     * 
     */
    @XmlEnumValue("thinThin")
    THIN_THIN("thinThin"),

    /**
     * Thin Line Outside Thick Line
     * 
     */
    @XmlEnumValue("thinThick")
    THIN_THICK("thinThick"),

    /**
     * Thick Line Outside Thin Line
     * 
     */
    @XmlEnumValue("thickThin")
    THICK_THIN("thickThin"),

    /**
     * Thck Line Between Thin Lines
     * 
     */
    @XmlEnumValue("thickBetweenThin")
    THICK_BETWEEN_THIN("thickBetweenThin");
    private final String value;

    STStrokeLineStyle(String v) {
        value = v;
    }

    public String value() {
        return value;
    }

    public static STStrokeLineStyle fromValue(String v) {
        for (STStrokeLineStyle c: STStrokeLineStyle.values()) {
            if (c.value.equals(v)) {
                return c;
            }
        }
        throw new IllegalArgumentException(v);
    }

}
