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
 * <p>Java class for ST_MdxKPIProperty.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
 * <p>
 * <pre>
 * &lt;simpleType name="ST_MdxKPIProperty">
 *   &lt;restriction base="{http://www.w3.org/2001/XMLSchema}string">
 *     &lt;enumeration value="v"/>
 *     &lt;enumeration value="g"/>
 *     &lt;enumeration value="s"/>
 *     &lt;enumeration value="t"/>
 *     &lt;enumeration value="w"/>
 *     &lt;enumeration value="m"/>
 *   &lt;/restriction>
 * &lt;/simpleType>
 * </pre>
 * 
 */
@XmlType(name = "ST_MdxKPIProperty")
@XmlEnum
public enum STMdxKPIProperty {

    @XmlEnumValue("v")
    V("v"),
    @XmlEnumValue("g")
    G("g"),
    @XmlEnumValue("s")
    S("s"),
    @XmlEnumValue("t")
    T("t"),
    @XmlEnumValue("w")
    W("w"),
    @XmlEnumValue("m")
    M("m");
    private final String value;

    STMdxKPIProperty(String v) {
        value = v;
    }

    public String value() {
        return value;
    }

    public static STMdxKPIProperty fromValue(String v) {
        for (STMdxKPIProperty c: STMdxKPIProperty.values()) {
            if (c.value.equals(v)) {
                return c;
            }
        }
        throw new IllegalArgumentException(v);
    }

}
