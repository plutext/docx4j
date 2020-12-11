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
 * <p>Java class for ST_MailMergeDest.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
 * <p>
 * <pre>
 * &lt;simpleType name="ST_MailMergeDest">
 *   &lt;restriction base="{http://www.w3.org/2001/XMLSchema}string">
 *     &lt;enumeration value="newDocument"/>
 *     &lt;enumeration value="printer"/>
 *     &lt;enumeration value="email"/>
 *     &lt;enumeration value="fax"/>
 *   &lt;/restriction>
 * &lt;/simpleType>
 * </pre>
 * 
 */
@XmlType(name = "ST_MailMergeDest")
@XmlEnum
public enum STMailMergeDest {


    /**
     * Send Merged Documents to New
     * 						Documents
     * 
     */
    @XmlEnumValue("newDocument")
    NEW_DOCUMENT("newDocument"),

    /**
     * Send Merged Documents to
     * 						Printer
     * 
     */
    @XmlEnumValue("printer")
    PRINTER("printer"),

    /**
     * Send Merged Documents as E-mail
     * 						Messages
     * 
     */
    @XmlEnumValue("email")
    EMAIL("email"),

    /**
     * Send Merged Documents as
     * 						Faxes
     * 
     */
    @XmlEnumValue("fax")
    FAX("fax");
    private final String value;

    STMailMergeDest(String v) {
        value = v;
    }

    public String value() {
        return value;
    }

    public static STMailMergeDest fromValue(String v) {
        for (STMailMergeDest c: STMailMergeDest.values()) {
            if (c.value.equals(v)) {
                return c;
            }
        }
        throw new IllegalArgumentException(v);
    }

}
