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
 * <p>Java class for ST_TrendlineType.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
 * <p>
 * <pre>
 * &lt;simpleType name="ST_TrendlineType"&gt;
 *   &lt;restriction base="{http://www.w3.org/2001/XMLSchema}string"&gt;
 *     &lt;enumeration value="exp"/&gt;
 *     &lt;enumeration value="linear"/&gt;
 *     &lt;enumeration value="log"/&gt;
 *     &lt;enumeration value="movingAvg"/&gt;
 *     &lt;enumeration value="poly"/&gt;
 *     &lt;enumeration value="power"/&gt;
 *   &lt;/restriction&gt;
 * &lt;/simpleType&gt;
 * </pre>
 * 
 */
@XmlType(name = "ST_TrendlineType")
@XmlEnum
public enum STTrendlineType {


    /**
     * Exponential
     * 
     */
    @XmlEnumValue("exp")
    EXP("exp"),

    /**
     * Linear
     * 
     */
    @XmlEnumValue("linear")
    LINEAR("linear"),

    /**
     * Logarithmic
     * 
     */
    @XmlEnumValue("log")
    LOG("log"),

    /**
     * Moving Average
     * 
     */
    @XmlEnumValue("movingAvg")
    MOVING_AVG("movingAvg"),

    /**
     * Polynomial
     * 
     */
    @XmlEnumValue("poly")
    POLY("poly"),

    /**
     * Power
     * 
     */
    @XmlEnumValue("power")
    POWER("power");
    private final String value;

    STTrendlineType(String v) {
        value = v;
    }

    public String value() {
        return value;
    }

    public static STTrendlineType fromValue(String v) {
        for (STTrendlineType c: STTrendlineType.values()) {
            if (c.value.equals(v)) {
                return c;
            }
        }
        throw new IllegalArgumentException(v);
    }

}
