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


package org.docx4j.dml;

import javax.xml.bind.annotation.XmlEnum;
import javax.xml.bind.annotation.XmlEnumValue;
import javax.xml.bind.annotation.XmlType;


/**
 * <p>Java class for ST_TextVertOverflowType.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
 * <p>
 * <pre>
 * &lt;simpleType name="ST_TextVertOverflowType"&gt;
 *   &lt;restriction base="{http://www.w3.org/2001/XMLSchema}token"&gt;
 *     &lt;enumeration value="overflow"/&gt;
 *     &lt;enumeration value="ellipsis"/&gt;
 *     &lt;enumeration value="clip"/&gt;
 *   &lt;/restriction&gt;
 * &lt;/simpleType&gt;
 * </pre>
 * 
 */
@XmlType(name = "ST_TextVertOverflowType")
@XmlEnum
public enum STTextVertOverflowType {


    /**
     * Text Overflow Enum ( Overflow )
     * 
     */
    @XmlEnumValue("overflow")
    OVERFLOW("overflow"),

    /**
     * Text Overflow Enum ( Ellipsis )
     * 
     */
    @XmlEnumValue("ellipsis")
    ELLIPSIS("ellipsis"),

    /**
     * Text Overflow Enum ( Clip )
     * 
     */
    @XmlEnumValue("clip")
    CLIP("clip");
    private final String value;

    STTextVertOverflowType(String v) {
        value = v;
    }

    public String value() {
        return value;
    }

    public static STTextVertOverflowType fromValue(String v) {
        for (STTextVertOverflowType c: STTextVertOverflowType.values()) {
            if (c.value.equals(v)) {
                return c;
            }
        }
        throw new IllegalArgumentException(v);
    }

}
