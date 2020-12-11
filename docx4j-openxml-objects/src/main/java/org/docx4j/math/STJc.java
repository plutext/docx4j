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
 * <p>Java class for ST_Jc.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
 * <p>
 * <pre>
 * &lt;simpleType name="ST_Jc">
 *   &lt;restriction base="{http://www.w3.org/2001/XMLSchema}string">
 *     &lt;enumeration value="left"/>
 *     &lt;enumeration value="right"/>
 *     &lt;enumeration value="center"/>
 *     &lt;enumeration value="centerGroup"/>
 *   &lt;/restriction>
 * &lt;/simpleType>
 * </pre>
 * 
 */
@XmlType(name = "ST_Jc")
@XmlEnum
public enum STJc {


    /**
     * Left Justification
     * 
     */
    @XmlEnumValue("left")
    LEFT("left"),

    /**
     * Right
     * 
     */
    @XmlEnumValue("right")
    RIGHT("right"),

    /**
     * Center (Text)
     * 
     */
    @XmlEnumValue("center")
    CENTER("center"),

    /**
     * Centered as Group (Text)
     * 
     */
    @XmlEnumValue("centerGroup")
    CENTER_GROUP("centerGroup");
    private final String value;

    STJc(String v) {
        value = v;
    }

    public String value() {
        return value;
    }

    public static STJc fromValue(String v) {
        for (STJc c: STJc.values()) {
            if (c.value.equals(v)) {
                return c;
            }
        }
        throw new IllegalArgumentException(v);
    }

}
