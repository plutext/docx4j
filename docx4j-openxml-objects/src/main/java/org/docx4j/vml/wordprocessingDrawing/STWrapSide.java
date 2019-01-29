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


package org.docx4j.vml.wordprocessingDrawing;

import javax.xml.bind.annotation.XmlEnum;
import javax.xml.bind.annotation.XmlEnumValue;
import javax.xml.bind.annotation.XmlType;


/**
 * <p>Java class for ST_WrapSide.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
 * <p>
 * <pre>
 * &lt;simpleType name="ST_WrapSide">
 *   &lt;restriction base="{http://www.w3.org/2001/XMLSchema}string">
 *     &lt;enumeration value="both"/>
 *     &lt;enumeration value="left"/>
 *     &lt;enumeration value="right"/>
 *     &lt;enumeration value="largest"/>
 *   &lt;/restriction>
 * &lt;/simpleType>
 * </pre>
 * 
 */
@XmlType(name = "ST_WrapSide")
@XmlEnum
public enum STWrapSide {


    /**
     * Both sides
     * 
     */
    @XmlEnumValue("both")
    BOTH("both"),

    /**
     * Left side
     * 
     */
    @XmlEnumValue("left")
    LEFT("left"),

    /**
     * Right side
     * 
     */
    @XmlEnumValue("right")
    RIGHT("right"),

    /**
     * Largest side
     * 
     */
    @XmlEnumValue("largest")
    LARGEST("largest");
    private final String value;

    STWrapSide(String v) {
        value = v;
    }

    public String value() {
        return value;
    }

    public static STWrapSide fromValue(String v) {
        for (STWrapSide c: STWrapSide.values()) {
            if (c.value.equals(v)) {
                return c;
            }
        }
        throw new IllegalArgumentException(v);
    }

}
