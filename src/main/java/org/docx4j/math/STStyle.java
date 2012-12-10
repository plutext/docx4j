/*
 *  Copyright 2009-2012, Plutext Pty Ltd.
 *   
 *  This file is part of pptx4j, a component of docx4j.

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
package org.docx4j.math;

import javax.xml.bind.annotation.XmlEnum;
import javax.xml.bind.annotation.XmlEnumValue;
import javax.xml.bind.annotation.XmlType;


/**
 * <p>Java class for ST_Style.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
 * <p>
 * <pre>
 * &lt;simpleType name="ST_Style">
 *   &lt;restriction base="{http://www.w3.org/2001/XMLSchema}string">
 *     &lt;enumeration value="p"/>
 *     &lt;enumeration value="b"/>
 *     &lt;enumeration value="i"/>
 *     &lt;enumeration value="bi"/>
 *   &lt;/restriction>
 * &lt;/simpleType>
 * </pre>
 * 
 */
@XmlType(name = "ST_Style")
@XmlEnum
public enum STStyle {


    /**
     * Plain
     * 
     */
    @XmlEnumValue("p")
    P("p"),

    /**
     * Bold
     * 
     */
    @XmlEnumValue("b")
    B("b"),

    /**
     * Italic
     * 
     */
    @XmlEnumValue("i")
    I("i"),

    /**
     * Bold-Italic
     * 
     */
    @XmlEnumValue("bi")
    BI("bi");
    private final String value;

    STStyle(String v) {
        value = v;
    }

    public String value() {
        return value;
    }

    public static STStyle fromValue(String v) {
        for (STStyle c: STStyle.values()) {
            if (c.value.equals(v)) {
                return c;
            }
        }
        throw new IllegalArgumentException(v);
    }

}
