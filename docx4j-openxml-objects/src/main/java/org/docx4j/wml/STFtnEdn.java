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
 * <p>Java class for ST_FtnEdn.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
 * <p>
 * <pre>
 * &lt;simpleType name="ST_FtnEdn">
 *   &lt;restriction base="{http://www.w3.org/2001/XMLSchema}string">
 *     &lt;enumeration value="normal"/>
 *     &lt;enumeration value="separator"/>
 *     &lt;enumeration value="continuationSeparator"/>
 *     &lt;enumeration value="continuationNotice"/>
 *   &lt;/restriction>
 * &lt;/simpleType>
 * </pre>
 * 
 */
@XmlType(name = "ST_FtnEdn")
@XmlEnum
public enum STFtnEdn {


    /**
     * Normal
     * 						Footnote/Endnote
     * 
     */
    @XmlEnumValue("normal")
    NORMAL("normal"),

    /**
     * Separator
     * 
     */
    @XmlEnumValue("separator")
    SEPARATOR("separator"),

    /**
     * Continuation Separator
     * 
     */
    @XmlEnumValue("continuationSeparator")
    CONTINUATION_SEPARATOR("continuationSeparator"),

    /**
     * Continuation Notice
     * 						Separator
     * 
     */
    @XmlEnumValue("continuationNotice")
    CONTINUATION_NOTICE("continuationNotice");
    private final String value;

    STFtnEdn(String v) {
        value = v;
    }

    public String value() {
        return value;
    }

    public static STFtnEdn fromValue(String v) {
        for (STFtnEdn c: STFtnEdn.values()) {
            if (c.value.equals(v)) {
                return c;
            }
        }
        throw new IllegalArgumentException(v);
    }

}
