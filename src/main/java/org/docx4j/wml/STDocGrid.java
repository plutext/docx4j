/*
 *  Copyright 2007-2013, Plutext Pty Ltd.
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
 * <p>Java class for ST_DocGrid.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
 * <p>
 * <pre>
 * &lt;simpleType name="ST_DocGrid">
 *   &lt;restriction base="{http://www.w3.org/2001/XMLSchema}string">
 *     &lt;enumeration value="default"/>
 *     &lt;enumeration value="lines"/>
 *     &lt;enumeration value="linesAndChars"/>
 *     &lt;enumeration value="snapToChars"/>
 *   &lt;/restriction>
 * &lt;/simpleType>
 * </pre>
 * 
 */
@XmlType(name = "ST_DocGrid")
@XmlEnum
public enum STDocGrid {


    /**
     * No Document Grid
     * 
     */
    @XmlEnumValue("default")
    DEFAULT("default"),

    /**
     * Line Grid Only
     * 
     */
    @XmlEnumValue("lines")
    LINES("lines"),

    /**
     * Line and Character Grid
     * 
     */
    @XmlEnumValue("linesAndChars")
    LINES_AND_CHARS("linesAndChars"),

    /**
     * Character Grid Only
     * 
     */
    @XmlEnumValue("snapToChars")
    SNAP_TO_CHARS("snapToChars");
    private final String value;

    STDocGrid(String v) {
        value = v;
    }

    public String value() {
        return value;
    }

    public static STDocGrid fromValue(String v) {
        for (STDocGrid c: STDocGrid.values()) {
            if (c.value.equals(v)) {
                return c;
            }
        }
        throw new IllegalArgumentException(v);
    }

}
