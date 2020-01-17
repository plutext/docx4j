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
 * <p>Java class for ST_ErrValType.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
 * <p>
 * <pre>
 * &lt;simpleType name="ST_ErrValType"&gt;
 *   &lt;restriction base="{http://www.w3.org/2001/XMLSchema}string"&gt;
 *     &lt;enumeration value="cust"/&gt;
 *     &lt;enumeration value="fixedVal"/&gt;
 *     &lt;enumeration value="percentage"/&gt;
 *     &lt;enumeration value="stdDev"/&gt;
 *     &lt;enumeration value="stdErr"/&gt;
 *   &lt;/restriction&gt;
 * &lt;/simpleType&gt;
 * </pre>
 * 
 */
@XmlType(name = "ST_ErrValType")
@XmlEnum
public enum STErrValType {


    /**
     * Custom Error Bars
     * 
     */
    @XmlEnumValue("cust")
    CUST("cust"),

    /**
     * Fixed Value
     * 
     */
    @XmlEnumValue("fixedVal")
    FIXED_VAL("fixedVal"),

    /**
     * Percentage
     * 
     */
    @XmlEnumValue("percentage")
    PERCENTAGE("percentage"),

    /**
     * Standard Deviation
     * 
     */
    @XmlEnumValue("stdDev")
    STD_DEV("stdDev"),

    /**
     * Standard Error
     * 
     */
    @XmlEnumValue("stdErr")
    STD_ERR("stdErr");
    private final String value;

    STErrValType(String v) {
        value = v;
    }

    public String value() {
        return value;
    }

    public static STErrValType fromValue(String v) {
        for (STErrValType c: STErrValType.values()) {
            if (c.value.equals(v)) {
                return c;
            }
        }
        throw new IllegalArgumentException(v);
    }

}
