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
 * <p>Java class for ST_DateTimeGrouping.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
 * <p>
 * <pre>
 * &lt;simpleType name="ST_DateTimeGrouping">
 *   &lt;restriction base="{http://www.w3.org/2001/XMLSchema}string">
 *     &lt;enumeration value="year"/>
 *     &lt;enumeration value="month"/>
 *     &lt;enumeration value="day"/>
 *     &lt;enumeration value="hour"/>
 *     &lt;enumeration value="minute"/>
 *     &lt;enumeration value="second"/>
 *   &lt;/restriction>
 * &lt;/simpleType>
 * </pre>
 * 
 */
@XmlType(name = "ST_DateTimeGrouping")
@XmlEnum
public enum STDateTimeGrouping {

    @XmlEnumValue("year")
    YEAR("year"),
    @XmlEnumValue("month")
    MONTH("month"),
    @XmlEnumValue("day")
    DAY("day"),
    @XmlEnumValue("hour")
    HOUR("hour"),
    @XmlEnumValue("minute")
    MINUTE("minute"),
    @XmlEnumValue("second")
    SECOND("second");
    private final String value;

    STDateTimeGrouping(String v) {
        value = v;
    }

    public String value() {
        return value;
    }

    public static STDateTimeGrouping fromValue(String v) {
        for (STDateTimeGrouping c: STDateTimeGrouping.values()) {
            if (c.value.equals(v)) {
                return c;
            }
        }
        throw new IllegalArgumentException(v);
    }

}
