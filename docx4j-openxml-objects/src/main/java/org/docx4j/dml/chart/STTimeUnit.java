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
 * <p>Java class for ST_TimeUnit.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
 * <p>
 * <pre>
 * &lt;simpleType name="ST_TimeUnit"&gt;
 *   &lt;restriction base="{http://www.w3.org/2001/XMLSchema}string"&gt;
 *     &lt;enumeration value="days"/&gt;
 *     &lt;enumeration value="months"/&gt;
 *     &lt;enumeration value="years"/&gt;
 *   &lt;/restriction&gt;
 * &lt;/simpleType&gt;
 * </pre>
 * 
 */
@XmlType(name = "ST_TimeUnit")
@XmlEnum
public enum STTimeUnit {


    /**
     * Days
     * 
     */
    @XmlEnumValue("days")
    DAYS("days"),

    /**
     * Months
     * 
     */
    @XmlEnumValue("months")
    MONTHS("months"),

    /**
     * Years
     * 
     */
    @XmlEnumValue("years")
    YEARS("years");
    private final String value;

    STTimeUnit(String v) {
        value = v;
    }

    public String value() {
        return value;
    }

    public static STTimeUnit fromValue(String v) {
        for (STTimeUnit c: STTimeUnit.values()) {
            if (c.value.equals(v)) {
                return c;
            }
        }
        throw new IllegalArgumentException(v);
    }

}
