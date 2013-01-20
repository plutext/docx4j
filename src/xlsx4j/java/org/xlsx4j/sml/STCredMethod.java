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
 * <p>Java class for ST_CredMethod.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
 * <p>
 * <pre>
 * &lt;simpleType name="ST_CredMethod">
 *   &lt;restriction base="{http://www.w3.org/2001/XMLSchema}string">
 *     &lt;enumeration value="integrated"/>
 *     &lt;enumeration value="none"/>
 *     &lt;enumeration value="stored"/>
 *     &lt;enumeration value="prompt"/>
 *   &lt;/restriction>
 * &lt;/simpleType>
 * </pre>
 * 
 */
@XmlType(name = "ST_CredMethod")
@XmlEnum
public enum STCredMethod {

    @XmlEnumValue("integrated")
    INTEGRATED("integrated"),
    @XmlEnumValue("none")
    NONE("none"),
    @XmlEnumValue("stored")
    STORED("stored"),
    @XmlEnumValue("prompt")
    PROMPT("prompt");
    private final String value;

    STCredMethod(String v) {
        value = v;
    }

    public String value() {
        return value;
    }

    public static STCredMethod fromValue(String v) {
        for (STCredMethod c: STCredMethod.values()) {
            if (c.value.equals(v)) {
                return c;
            }
        }
        throw new IllegalArgumentException(v);
    }

}
