/*
 *  Copyright 2010-2013, Plutext Pty Ltd.
 *   
 *  This file is part of xlsx4j, a component of docx4j.

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

import jakarta.xml.bind.annotation.XmlEnum;
import jakarta.xml.bind.annotation.XmlEnumValue;
import jakarta.xml.bind.annotation.XmlType;


/**
 * <p>Java class for ST_UnderlineValues.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
 * <p>
 * <pre>
 * &lt;simpleType name="ST_UnderlineValues">
 *   &lt;restriction base="{http://www.w3.org/2001/XMLSchema}string">
 *     &lt;enumeration value="single"/>
 *     &lt;enumeration value="double"/>
 *     &lt;enumeration value="singleAccounting"/>
 *     &lt;enumeration value="doubleAccounting"/>
 *     &lt;enumeration value="none"/>
 *   &lt;/restriction>
 * &lt;/simpleType>
 * </pre>
 * 
 */
@XmlType(name = "ST_UnderlineValues")
@XmlEnum
public enum STUnderlineValues {

    @XmlEnumValue("single")
    SINGLE("single"),
    @XmlEnumValue("double")
    DOUBLE("double"),
    @XmlEnumValue("singleAccounting")
    SINGLE_ACCOUNTING("singleAccounting"),
    @XmlEnumValue("doubleAccounting")
    DOUBLE_ACCOUNTING("doubleAccounting"),
    @XmlEnumValue("none")
    NONE("none");
    private final String value;

    STUnderlineValues(String v) {
        value = v;
    }

    public String value() {
        return value;
    }

    public static STUnderlineValues fromValue(String v) {
        for (STUnderlineValues c: STUnderlineValues.values()) {
            if (c.value.equals(v)) {
                return c;
            }
        }
        throw new IllegalArgumentException(v);
    }

}
