/*
 *  Copyright 2007-2013, Plutext Pty Ltd.
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


package org.docx4j.wml;

import javax.xml.bind.annotation.XmlEnum;
import javax.xml.bind.annotation.XmlEnumValue;
import javax.xml.bind.annotation.XmlType;


/**
 * <p>Java class for ST_FFTextType.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
 * <p>
 * <pre>
 * &lt;simpleType name="ST_FFTextType">
 *   &lt;restriction base="{http://www.w3.org/2001/XMLSchema}string">
 *     &lt;enumeration value="regular"/>
 *     &lt;enumeration value="number"/>
 *     &lt;enumeration value="date"/>
 *     &lt;enumeration value="currentTime"/>
 *     &lt;enumeration value="currentDate"/>
 *     &lt;enumeration value="calculated"/>
 *   &lt;/restriction>
 * &lt;/simpleType>
 * </pre>
 * 
 */
@XmlType(name = "ST_FFTextType")
@XmlEnum
public enum STFFTextType {


    /**
     * Text Box
     * 
     */
    @XmlEnumValue("regular")
    REGULAR("regular"),

    /**
     * Number
     * 
     */
    @XmlEnumValue("number")
    NUMBER("number"),

    /**
     * Date
     * 
     */
    @XmlEnumValue("date")
    DATE("date"),

    /**
     * Current Time Display
     * 
     */
    @XmlEnumValue("currentTime")
    CURRENT_TIME("currentTime"),

    /**
     * Current Date Display
     * 
     */
    @XmlEnumValue("currentDate")
    CURRENT_DATE("currentDate"),

    /**
     * Field Calculation
     * 
     */
    @XmlEnumValue("calculated")
    CALCULATED("calculated");
    private final String value;

    STFFTextType(String v) {
        value = v;
    }

    public String value() {
        return value;
    }

    public static STFFTextType fromValue(String v) {
        for (STFFTextType c: STFFTextType.values()) {
            if (c.value.equals(v)) {
                return c;
            }
        }
        throw new IllegalArgumentException(v);
    }

}
