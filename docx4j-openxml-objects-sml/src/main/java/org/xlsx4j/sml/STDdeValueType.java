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
 * <p>Java class for ST_DdeValueType.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
 * <p>
 * <pre>
 * &lt;simpleType name="ST_DdeValueType">
 *   &lt;restriction base="{http://www.w3.org/2001/XMLSchema}string">
 *     &lt;enumeration value="nil"/>
 *     &lt;enumeration value="b"/>
 *     &lt;enumeration value="n"/>
 *     &lt;enumeration value="e"/>
 *     &lt;enumeration value="str"/>
 *   &lt;/restriction>
 * &lt;/simpleType>
 * </pre>
 * 
 */
@XmlType(name = "ST_DdeValueType")
@XmlEnum
public enum STDdeValueType {

    @XmlEnumValue("nil")
    NIL("nil"),
    @XmlEnumValue("b")
    B("b"),
    @XmlEnumValue("n")
    N("n"),
    @XmlEnumValue("e")
    E("e"),
    @XmlEnumValue("str")
    STR("str");
    private final String value;

    STDdeValueType(String v) {
        value = v;
    }

    public String value() {
        return value;
    }

    public static STDdeValueType fromValue(String v) {
        for (STDdeValueType c: STDdeValueType.values()) {
            if (c.value.equals(v)) {
                return c;
            }
        }
        throw new IllegalArgumentException(v);
    }

}
