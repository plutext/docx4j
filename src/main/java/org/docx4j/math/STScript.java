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
 * <p>Java class for ST_Script.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
 * <p>
 * <pre>
 * &lt;simpleType name="ST_Script">
 *   &lt;restriction base="{http://www.w3.org/2001/XMLSchema}string">
 *     &lt;enumeration value="roman"/>
 *     &lt;enumeration value="script"/>
 *     &lt;enumeration value="fraktur"/>
 *     &lt;enumeration value="double-struck"/>
 *     &lt;enumeration value="sans-serif"/>
 *     &lt;enumeration value="monospace"/>
 *   &lt;/restriction>
 * &lt;/simpleType>
 * </pre>
 * 
 */
@XmlType(name = "ST_Script")
@XmlEnum
public enum STScript {


    /**
     * Roman
     * 
     */
    @XmlEnumValue("roman")
    ROMAN("roman"),

    /**
     * Script
     * 
     */
    @XmlEnumValue("script")
    SCRIPT("script"),

    /**
     * Fraktur
     * 
     */
    @XmlEnumValue("fraktur")
    FRAKTUR("fraktur"),

    /**
     * double-struck
     * 
     */
    @XmlEnumValue("double-struck")
    DOUBLE_STRUCK("double-struck"),

    /**
     * Sans-Serif
     * 
     */
    @XmlEnumValue("sans-serif")
    SANS_SERIF("sans-serif"),

    /**
     * Monospace
     * 
     */
    @XmlEnumValue("monospace")
    MONOSPACE("monospace");
    private final String value;

    STScript(String v) {
        value = v;
    }

    public String value() {
        return value;
    }

    public static STScript fromValue(String v) {
        for (STScript c: STScript.values()) {
            if (c.value.equals(v)) {
                return c;
            }
        }
        throw new IllegalArgumentException(v);
    }

}
