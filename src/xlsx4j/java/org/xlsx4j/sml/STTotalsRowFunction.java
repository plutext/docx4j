/*
 *  Copyright 2010, Plutext Pty Ltd.
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


package org.xlsx4j.sml;

import javax.xml.bind.annotation.XmlEnum;
import javax.xml.bind.annotation.XmlEnumValue;
import javax.xml.bind.annotation.XmlType;


/**
 * <p>Java class for ST_TotalsRowFunction.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
 * <p>
 * <pre>
 * &lt;simpleType name="ST_TotalsRowFunction">
 *   &lt;restriction base="{http://www.w3.org/2001/XMLSchema}string">
 *     &lt;enumeration value="none"/>
 *     &lt;enumeration value="sum"/>
 *     &lt;enumeration value="min"/>
 *     &lt;enumeration value="max"/>
 *     &lt;enumeration value="average"/>
 *     &lt;enumeration value="count"/>
 *     &lt;enumeration value="countNums"/>
 *     &lt;enumeration value="stdDev"/>
 *     &lt;enumeration value="var"/>
 *     &lt;enumeration value="custom"/>
 *   &lt;/restriction>
 * &lt;/simpleType>
 * </pre>
 * 
 */
@XmlType(name = "ST_TotalsRowFunction")
@XmlEnum
public enum STTotalsRowFunction {


    /**
     * None
     * 
     */
    @XmlEnumValue("none")
    NONE("none"),

    /**
     * Sum
     * 
     */
    @XmlEnumValue("sum")
    SUM("sum"),

    /**
     * Minimum
     * 
     */
    @XmlEnumValue("min")
    MIN("min"),

    /**
     * Maximum
     * 
     */
    @XmlEnumValue("max")
    MAX("max"),

    /**
     * Average
     * 
     */
    @XmlEnumValue("average")
    AVERAGE("average"),

    /**
     * Non Empty Cell Count
     * 
     */
    @XmlEnumValue("count")
    COUNT("count"),

    /**
     * Count Numbers
     * 
     */
    @XmlEnumValue("countNums")
    COUNT_NUMS("countNums"),

    /**
     * StdDev
     * 
     */
    @XmlEnumValue("stdDev")
    STD_DEV("stdDev"),

    /**
     * Var
     * 
     */
    @XmlEnumValue("var")
    VAR("var"),

    /**
     * Custom Formula
     * 
     */
    @XmlEnumValue("custom")
    CUSTOM("custom");
    private final String value;

    STTotalsRowFunction(String v) {
        value = v;
    }

    public String value() {
        return value;
    }

    public static STTotalsRowFunction fromValue(String v) {
        for (STTotalsRowFunction c: STTotalsRowFunction.values()) {
            if (c.value.equals(v)) {
                return c;
            }
        }
        throw new IllegalArgumentException(v);
    }

}
