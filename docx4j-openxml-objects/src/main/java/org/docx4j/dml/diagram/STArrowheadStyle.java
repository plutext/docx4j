/*
 *  Copyright 2007-2008, Plutext Pty Ltd.
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


package org.docx4j.dml.diagram;

import jakarta.xml.bind.annotation.XmlEnum;
import jakarta.xml.bind.annotation.XmlEnumValue;
import jakarta.xml.bind.annotation.XmlType;


/**
 * <p>Java class for ST_ArrowheadStyle.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
 * <p>
 * <pre>
 * &lt;simpleType name="ST_ArrowheadStyle"&gt;
 *   &lt;restriction base="{http://www.w3.org/2001/XMLSchema}token"&gt;
 *     &lt;enumeration value="auto"/&gt;
 *     &lt;enumeration value="arr"/&gt;
 *     &lt;enumeration value="noArr"/&gt;
 *   &lt;/restriction&gt;
 * &lt;/simpleType&gt;
 * </pre>
 * 
 */
@XmlType(name = "ST_ArrowheadStyle")
@XmlEnum
public enum STArrowheadStyle {


    /**
     * Auto
     * 
     */
    @XmlEnumValue("auto")
    AUTO("auto"),

    /**
     * Arrowhead Present
     * 
     */
    @XmlEnumValue("arr")
    ARR("arr"),

    /**
     * No Arrowhead
     * 
     */
    @XmlEnumValue("noArr")
    NO_ARR("noArr");
    private final String value;

    STArrowheadStyle(String v) {
        value = v;
    }

    public String value() {
        return value;
    }

    public static STArrowheadStyle fromValue(String v) {
        for (STArrowheadStyle c: STArrowheadStyle.values()) {
            if (c.value.equals(v)) {
                return c;
            }
        }
        throw new IllegalArgumentException(v);
    }

}
