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
 * <p>Java class for ST_Em.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
 * <p>
 * <pre>
 * &lt;simpleType name="ST_Em">
 *   &lt;restriction base="{http://www.w3.org/2001/XMLSchema}string">
 *     &lt;enumeration value="none"/>
 *     &lt;enumeration value="dot"/>
 *     &lt;enumeration value="comma"/>
 *     &lt;enumeration value="circle"/>
 *     &lt;enumeration value="underDot"/>
 *   &lt;/restriction>
 * &lt;/simpleType>
 * </pre>
 * 
 */
@XmlType(name = "ST_Em")
@XmlEnum
public enum STEm {


    /**
     * No Emphasis Mark
     * 
     */
    @XmlEnumValue("none")
    NONE("none"),

    /**
     * Dot Emphasis Mark Above
     * 						Characters
     * 
     */
    @XmlEnumValue("dot")
    DOT("dot"),

    /**
     * Comma Emphasis Mark Above
     * 						Characters
     * 
     */
    @XmlEnumValue("comma")
    COMMA("comma"),

    /**
     * Circle Emphasis Mark Above
     * 						Characters
     * 
     */
    @XmlEnumValue("circle")
    CIRCLE("circle"),

    /**
     * Dot Emphasis Mark Below
     * 						Characters
     * 
     */
    @XmlEnumValue("underDot")
    UNDER_DOT("underDot");
    private final String value;

    STEm(String v) {
        value = v;
    }

    public String value() {
        return value;
    }

    public static STEm fromValue(String v) {
        for (STEm c: STEm.values()) {
            if (c.value.equals(v)) {
                return c;
            }
        }
        throw new IllegalArgumentException(v);
    }

}
