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


package org.docx4j.vml;

import javax.xml.bind.annotation.XmlEnum;
import javax.xml.bind.annotation.XmlEnumValue;
import javax.xml.bind.annotation.XmlType;


/**
 * <p>Java class for ST_StrokeArrowType.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
 * <p>
 * <pre>
 * &lt;simpleType name="ST_StrokeArrowType">
 *   &lt;restriction base="{http://www.w3.org/2001/XMLSchema}string">
 *     &lt;enumeration value="none"/>
 *     &lt;enumeration value="block"/>
 *     &lt;enumeration value="classic"/>
 *     &lt;enumeration value="oval"/>
 *     &lt;enumeration value="diamond"/>
 *     &lt;enumeration value="open"/>
 *   &lt;/restriction>
 * &lt;/simpleType>
 * </pre>
 * 
 */
@XmlType(name = "ST_StrokeArrowType")
@XmlEnum
public enum STStrokeArrowType {


    /**
     * No Arrowhead
     * 
     */
    @XmlEnumValue("none")
    NONE("none"),

    /**
     * Block Arrowhead
     * 
     */
    @XmlEnumValue("block")
    BLOCK("block"),

    /**
     * Classic Arrowhead
     * 
     */
    @XmlEnumValue("classic")
    CLASSIC("classic"),

    /**
     * Oval Arrowhead
     * 
     */
    @XmlEnumValue("oval")
    OVAL("oval"),

    /**
     * Diamond Arrowhead
     * 
     */
    @XmlEnumValue("diamond")
    DIAMOND("diamond"),

    /**
     * Open Arrowhead
     * 
     */
    @XmlEnumValue("open")
    OPEN("open");
    private final String value;

    STStrokeArrowType(String v) {
        value = v;
    }

    public String value() {
        return value;
    }

    public static STStrokeArrowType fromValue(String v) {
        for (STStrokeArrowType c: STStrokeArrowType.values()) {
            if (c.value.equals(v)) {
                return c;
            }
        }
        throw new IllegalArgumentException(v);
    }

}
