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
 * <p>Java class for ST_DocType.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
 * <p>
 * <pre>
 * &lt;simpleType name="ST_DocType">
 *   &lt;restriction base="{http://www.w3.org/2001/XMLSchema}string">
 *     &lt;enumeration value="notSpecified"/>
 *     &lt;enumeration value="letter"/>
 *     &lt;enumeration value="eMail"/>
 *   &lt;/restriction>
 * &lt;/simpleType>
 * </pre>
 * 
 */
@XmlType(name = "ST_DocType")
@XmlEnum
public enum STDocType {


    /**
     * Default Document
     * 
     */
    @XmlEnumValue("notSpecified")
    NOT_SPECIFIED("notSpecified"),

    /**
     * Letter
     * 
     */
    @XmlEnumValue("letter")
    LETTER("letter"),

    /**
     * E-Mail Message
     * 
     */
    @XmlEnumValue("eMail")
    E_MAIL("eMail");
    private final String value;

    STDocType(String v) {
        value = v;
    }

    public String value() {
        return value;
    }

    public static STDocType fromValue(String v) {
        for (STDocType c: STDocType.values()) {
            if (c.value.equals(v)) {
                return c;
            }
        }
        throw new IllegalArgumentException(v);
    }

}
