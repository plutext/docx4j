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

package org.docx4j.wml;

import javax.xml.bind.annotation.XmlEnum;
import javax.xml.bind.annotation.XmlEnumValue;
import javax.xml.bind.annotation.XmlType;


/**
 * <p>Java class for ST_TextAlignment.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
 * <p>
 * <pre>
 * &lt;simpleType name="ST_TextAlignment">
 *   &lt;restriction base="{http://www.w3.org/2001/XMLSchema}string">
 *     &lt;enumeration value="top"/>
 *     &lt;enumeration value="center"/>
 *     &lt;enumeration value="baseline"/>
 *     &lt;enumeration value="bottom"/>
 *     &lt;enumeration value="auto"/>
 *   &lt;/restriction>
 * &lt;/simpleType>
 * </pre>
 * 
 */
@XmlType(name = "ST_TextAlignment")
@XmlEnum
public enum STTextAlignment {


    /**
     * Align Text at Top
     * 
     */
    @XmlEnumValue("top")
    TOP("top"),

    /**
     * Align Text at Center
     * 
     */
    @XmlEnumValue("center")
    CENTER("center"),

    /**
     * Align Text at Baseline
     * 
     */
    @XmlEnumValue("baseline")
    BASELINE("baseline"),

    /**
     * Align Text at Bottom
     * 
     */
    @XmlEnumValue("bottom")
    BOTTOM("bottom"),

    /**
     * Automatically Determine
     * 						Alignment
     * 
     */
    @XmlEnumValue("auto")
    AUTO("auto");
    private final String value;

    STTextAlignment(String v) {
        value = v;
    }

    public String value() {
        return value;
    }

    public static STTextAlignment fromValue(String v) {
        for (STTextAlignment c: STTextAlignment.values()) {
            if (c.value.equals(v)) {
                return c;
            }
        }
        throw new IllegalArgumentException(v);
    }

}
