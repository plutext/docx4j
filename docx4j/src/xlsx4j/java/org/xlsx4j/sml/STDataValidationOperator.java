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
 * <p>Java class for ST_DataValidationOperator.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
 * <p>
 * <pre>
 * &lt;simpleType name="ST_DataValidationOperator">
 *   &lt;restriction base="{http://www.w3.org/2001/XMLSchema}string">
 *     &lt;enumeration value="between"/>
 *     &lt;enumeration value="notBetween"/>
 *     &lt;enumeration value="equal"/>
 *     &lt;enumeration value="notEqual"/>
 *     &lt;enumeration value="lessThan"/>
 *     &lt;enumeration value="lessThanOrEqual"/>
 *     &lt;enumeration value="greaterThan"/>
 *     &lt;enumeration value="greaterThanOrEqual"/>
 *   &lt;/restriction>
 * &lt;/simpleType>
 * </pre>
 * 
 */
@XmlType(name = "ST_DataValidationOperator")
@XmlEnum
public enum STDataValidationOperator {


    /**
     * Between
     * 
     */
    @XmlEnumValue("between")
    BETWEEN("between"),

    /**
     * Not Between
     * 
     */
    @XmlEnumValue("notBetween")
    NOT_BETWEEN("notBetween"),

    /**
     * Equal
     * 
     */
    @XmlEnumValue("equal")
    EQUAL("equal"),

    /**
     * Not Equal
     * 
     */
    @XmlEnumValue("notEqual")
    NOT_EQUAL("notEqual"),

    /**
     * Less Than
     * 
     */
    @XmlEnumValue("lessThan")
    LESS_THAN("lessThan"),

    /**
     * Less Than Or Equal
     * 
     */
    @XmlEnumValue("lessThanOrEqual")
    LESS_THAN_OR_EQUAL("lessThanOrEqual"),

    /**
     * Greater Than
     * 
     */
    @XmlEnumValue("greaterThan")
    GREATER_THAN("greaterThan"),

    /**
     * Greater Than Or Equal
     * 
     */
    @XmlEnumValue("greaterThanOrEqual")
    GREATER_THAN_OR_EQUAL("greaterThanOrEqual");
    private final String value;

    STDataValidationOperator(String v) {
        value = v;
    }

    public String value() {
        return value;
    }

    public static STDataValidationOperator fromValue(String v) {
        for (STDataValidationOperator c: STDataValidationOperator.values()) {
            if (c.value.equals(v)) {
                return c;
            }
        }
        throw new IllegalArgumentException(v);
    }

}
