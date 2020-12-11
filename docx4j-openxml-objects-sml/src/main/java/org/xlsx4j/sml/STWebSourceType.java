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

import javax.xml.bind.annotation.XmlEnum;
import javax.xml.bind.annotation.XmlEnumValue;
import javax.xml.bind.annotation.XmlType;


/**
 * <p>Java class for ST_WebSourceType.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
 * <p>
 * <pre>
 * &lt;simpleType name="ST_WebSourceType">
 *   &lt;restriction base="{http://www.w3.org/2001/XMLSchema}string">
 *     &lt;enumeration value="sheet"/>
 *     &lt;enumeration value="printArea"/>
 *     &lt;enumeration value="autoFilter"/>
 *     &lt;enumeration value="range"/>
 *     &lt;enumeration value="chart"/>
 *     &lt;enumeration value="pivotTable"/>
 *     &lt;enumeration value="query"/>
 *     &lt;enumeration value="label"/>
 *   &lt;/restriction>
 * &lt;/simpleType>
 * </pre>
 * 
 */
@XmlType(name = "ST_WebSourceType")
@XmlEnum
public enum STWebSourceType {

    @XmlEnumValue("sheet")
    SHEET("sheet"),
    @XmlEnumValue("printArea")
    PRINT_AREA("printArea"),
    @XmlEnumValue("autoFilter")
    AUTO_FILTER("autoFilter"),
    @XmlEnumValue("range")
    RANGE("range"),
    @XmlEnumValue("chart")
    CHART("chart"),
    @XmlEnumValue("pivotTable")
    PIVOT_TABLE("pivotTable"),
    @XmlEnumValue("query")
    QUERY("query"),
    @XmlEnumValue("label")
    LABEL("label");
    private final String value;

    STWebSourceType(String v) {
        value = v;
    }

    public String value() {
        return value;
    }

    public static STWebSourceType fromValue(String v) {
        for (STWebSourceType c: STWebSourceType.values()) {
            if (c.value.equals(v)) {
                return c;
            }
        }
        throw new IllegalArgumentException(v);
    }

}
