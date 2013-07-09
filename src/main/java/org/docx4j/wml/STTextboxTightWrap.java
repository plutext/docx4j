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
 * <p>Java class for ST_TextboxTightWrap.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
 * <p>
 * <pre>
 * &lt;simpleType name="ST_TextboxTightWrap">
 *   &lt;restriction base="{http://www.w3.org/2001/XMLSchema}string">
 *     &lt;enumeration value="none"/>
 *     &lt;enumeration value="allLines"/>
 *     &lt;enumeration value="firstAndLastLine"/>
 *     &lt;enumeration value="firstLineOnly"/>
 *     &lt;enumeration value="lastLineOnly"/>
 *   &lt;/restriction>
 * &lt;/simpleType>
 * </pre>
 * 
 */
@XmlType(name = "ST_TextboxTightWrap")
@XmlEnum
public enum STTextboxTightWrap {


    /**
     * Do Not Tight Wrap
     * 
     */
    @XmlEnumValue("none")
    NONE("none"),

    /**
     * Tight Wrap All Lines
     * 
     */
    @XmlEnumValue("allLines")
    ALL_LINES("allLines"),

    /**
     * Tight Wrap First and Last
     * 						Lines
     * 
     */
    @XmlEnumValue("firstAndLastLine")
    FIRST_AND_LAST_LINE("firstAndLastLine"),

    /**
     * Tight Wrap First Line
     * 
     */
    @XmlEnumValue("firstLineOnly")
    FIRST_LINE_ONLY("firstLineOnly"),

    /**
     * Tight Wrap Last Line
     * 
     */
    @XmlEnumValue("lastLineOnly")
    LAST_LINE_ONLY("lastLineOnly");
    private final String value;

    STTextboxTightWrap(String v) {
        value = v;
    }

    public String value() {
        return value;
    }

    public static STTextboxTightWrap fromValue(String v) {
        for (STTextboxTightWrap c: STTextboxTightWrap.values()) {
            if (c.value.equals(v)) {
                return c;
            }
        }
        throw new IllegalArgumentException(v);
    }

}
