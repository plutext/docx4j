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

import javax.xml.bind.annotation.XmlEnum;
import javax.xml.bind.annotation.XmlEnumValue;
import javax.xml.bind.annotation.XmlType;


/**
 * <p>Java class for ST_FunctionType.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
 * <p>
 * <pre>
 * &lt;simpleType name="ST_FunctionType">
 *   &lt;restriction base="{http://www.w3.org/2001/XMLSchema}token">
 *     &lt;enumeration value="cnt"/>
 *     &lt;enumeration value="pos"/>
 *     &lt;enumeration value="revPos"/>
 *     &lt;enumeration value="posEven"/>
 *     &lt;enumeration value="posOdd"/>
 *     &lt;enumeration value="var"/>
 *     &lt;enumeration value="depth"/>
 *     &lt;enumeration value="maxDepth"/>
 *   &lt;/restriction>
 * &lt;/simpleType>
 * </pre>
 * 
 */
@XmlType(name = "ST_FunctionType")
@XmlEnum
public enum STFunctionType {


    /**
     * Count
     * 
     */
    @XmlEnumValue("cnt")
    CNT("cnt"),

    /**
     * Position
     * 
     */
    @XmlEnumValue("pos")
    POS("pos"),

    /**
     * Reverse Position
     * 
     */
    @XmlEnumValue("revPos")
    REV_POS("revPos"),

    /**
     * Position Even
     * 
     */
    @XmlEnumValue("posEven")
    POS_EVEN("posEven"),

    /**
     * Position Odd
     * 
     */
    @XmlEnumValue("posOdd")
    POS_ODD("posOdd"),

    /**
     * Variable
     * 
     */
    @XmlEnumValue("var")
    VAR("var"),

    /**
     * Depth
     * 
     */
    @XmlEnumValue("depth")
    DEPTH("depth"),

    /**
     * Max Depth
     * 
     */
    @XmlEnumValue("maxDepth")
    MAX_DEPTH("maxDepth");
    private final String value;

    STFunctionType(String v) {
        value = v;
    }

    public String value() {
        return value;
    }

    public static STFunctionType fromValue(String v) {
        for (STFunctionType c: STFunctionType.values()) {
            if (c.value.equals(v)) {
                return c;
            }
        }
        throw new IllegalArgumentException(v);
    }

}
