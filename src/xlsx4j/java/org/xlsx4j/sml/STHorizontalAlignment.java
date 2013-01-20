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
 * <p>Java class for ST_HorizontalAlignment.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
 * <p>
 * <pre>
 * &lt;simpleType name="ST_HorizontalAlignment">
 *   &lt;restriction base="{http://www.w3.org/2001/XMLSchema}string">
 *     &lt;enumeration value="general"/>
 *     &lt;enumeration value="left"/>
 *     &lt;enumeration value="center"/>
 *     &lt;enumeration value="right"/>
 *     &lt;enumeration value="fill"/>
 *     &lt;enumeration value="justify"/>
 *     &lt;enumeration value="centerContinuous"/>
 *     &lt;enumeration value="distributed"/>
 *   &lt;/restriction>
 * &lt;/simpleType>
 * </pre>
 * 
 */
@XmlType(name = "ST_HorizontalAlignment")
@XmlEnum
public enum STHorizontalAlignment {

    @XmlEnumValue("general")
    GENERAL("general"),
    @XmlEnumValue("left")
    LEFT("left"),
    @XmlEnumValue("center")
    CENTER("center"),
    @XmlEnumValue("right")
    RIGHT("right"),
    @XmlEnumValue("fill")
    FILL("fill"),
    @XmlEnumValue("justify")
    JUSTIFY("justify"),
    @XmlEnumValue("centerContinuous")
    CENTER_CONTINUOUS("centerContinuous"),
    @XmlEnumValue("distributed")
    DISTRIBUTED("distributed");
    private final String value;

    STHorizontalAlignment(String v) {
        value = v;
    }

    public String value() {
        return value;
    }

    public static STHorizontalAlignment fromValue(String v) {
        for (STHorizontalAlignment c: STHorizontalAlignment.values()) {
            if (c.value.equals(v)) {
                return c;
            }
        }
        throw new IllegalArgumentException(v);
    }

}
