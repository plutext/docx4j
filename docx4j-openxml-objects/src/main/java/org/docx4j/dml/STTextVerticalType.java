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
 * <p>Java class for ST_TextVerticalType.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
 * <p>
 * <pre>
 * &lt;simpleType name="ST_TextVerticalType"&gt;
 *   &lt;restriction base="{http://www.w3.org/2001/XMLSchema}token"&gt;
 *     &lt;enumeration value="horz"/&gt;
 *     &lt;enumeration value="vert"/&gt;
 *     &lt;enumeration value="vert270"/&gt;
 *     &lt;enumeration value="wordArtVert"/&gt;
 *     &lt;enumeration value="eaVert"/&gt;
 *     &lt;enumeration value="mongolianVert"/&gt;
 *     &lt;enumeration value="wordArtVertRtl"/&gt;
 *   &lt;/restriction&gt;
 * &lt;/simpleType&gt;
 * </pre>
 * 
 */
@XmlType(name = "ST_TextVerticalType")
@XmlEnum
public enum STTextVerticalType {


    /**
     * Vertical Text Type Enum ( Horizontal )
     * 
     */
    @XmlEnumValue("horz")
    HORZ("horz"),

    /**
     * Vertical Text Type Enum ( Vertical )
     * 
     */
    @XmlEnumValue("vert")
    VERT("vert"),

    /**
     * Vertical Text Type Enum ( Vertical 270 )
     * 
     */
    @XmlEnumValue("vert270")
    VERT_270("vert270"),

    /**
     * Vertical Text Type Enum ( WordArt Vertical )
     * 
     */
    @XmlEnumValue("wordArtVert")
    WORD_ART_VERT("wordArtVert"),

    /**
     * Vertical Text Type Enum ( East Asian Vertical )
     * 
     */
    @XmlEnumValue("eaVert")
    EA_VERT("eaVert"),

    /**
     * Vertical Text Type Enum ( Mongolian Vertical )
     * 
     */
    @XmlEnumValue("mongolianVert")
    MONGOLIAN_VERT("mongolianVert"),

    /**
     * Vertical WordArt Right to Left
     * 
     */
    @XmlEnumValue("wordArtVertRtl")
    WORD_ART_VERT_RTL("wordArtVertRtl");
    private final String value;

    STTextVerticalType(String v) {
        value = v;
    }

    public String value() {
        return value;
    }

    public static STTextVerticalType fromValue(String v) {
        for (STTextVerticalType c: STTextVerticalType.values()) {
            if (c.value.equals(v)) {
                return c;
            }
        }
        throw new IllegalArgumentException(v);
    }

}
