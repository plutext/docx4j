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


package org.docx4j.dml.chart;

import jakarta.xml.bind.annotation.XmlEnum;
import jakarta.xml.bind.annotation.XmlEnumValue;
import jakarta.xml.bind.annotation.XmlType;


/**
 * <p>Java class for ST_Crosses.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
 * <p>
 * <pre>
 * &lt;simpleType name="ST_Crosses"&gt;
 *   &lt;restriction base="{http://www.w3.org/2001/XMLSchema}string"&gt;
 *     &lt;enumeration value="autoZero"/&gt;
 *     &lt;enumeration value="max"/&gt;
 *     &lt;enumeration value="min"/&gt;
 *   &lt;/restriction&gt;
 * &lt;/simpleType&gt;
 * </pre>
 * 
 */
@XmlType(name = "ST_Crosses")
@XmlEnum
public enum STCrosses {


    /**
     * Axis Crosses at Zero
     * 
     */
    @XmlEnumValue("autoZero")
    AUTO_ZERO("autoZero"),

    /**
     * Maximum
     * 
     */
    @XmlEnumValue("max")
    MAX("max"),

    /**
     * Minimum
     * 
     */
    @XmlEnumValue("min")
    MIN("min");
    private final String value;

    STCrosses(String v) {
        value = v;
    }

    public String value() {
        return value;
    }

    public static STCrosses fromValue(String v) {
        for (STCrosses c: STCrosses.values()) {
            if (c.value.equals(v)) {
                return c;
            }
        }
        throw new IllegalArgumentException(v);
    }

}
