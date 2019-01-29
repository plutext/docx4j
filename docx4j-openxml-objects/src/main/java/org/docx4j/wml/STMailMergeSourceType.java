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
 * <p>Java class for ST_MailMergeSourceType.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
 * <p>
 * <pre>
 * &lt;simpleType name="ST_MailMergeSourceType">
 *   &lt;restriction base="{http://www.w3.org/2001/XMLSchema}string">
 *     &lt;enumeration value="database"/>
 *     &lt;enumeration value="addressBook"/>
 *     &lt;enumeration value="document1"/>
 *     &lt;enumeration value="document2"/>
 *     &lt;enumeration value="text"/>
 *     &lt;enumeration value="email"/>
 *     &lt;enumeration value="native"/>
 *     &lt;enumeration value="legacy"/>
 *     &lt;enumeration value="master"/>
 *   &lt;/restriction>
 * &lt;/simpleType>
 * </pre>
 * 
 */
@XmlType(name = "ST_MailMergeSourceType")
@XmlEnum
public enum STMailMergeSourceType {


    /**
     * Database Data Source
     * 
     */
    @XmlEnumValue("database")
    DATABASE("database"),

    /**
     * Address Book Data Source
     * 
     */
    @XmlEnumValue("addressBook")
    ADDRESS_BOOK("addressBook"),

    /**
     * Alternate Document Format Data
     * 						Source
     * 
     */
    @XmlEnumValue("document1")
    DOCUMENT_1("document1"),

    /**
     * Alternate Document Format Data Source
     * 						Two
     * 
     */
    @XmlEnumValue("document2")
    DOCUMENT_2("document2"),

    /**
     * Text File Data Source
     * 
     */
    @XmlEnumValue("text")
    TEXT("text"),

    /**
     * E-Mail Program Data
     * 						Source
     * 
     */
    @XmlEnumValue("email")
    EMAIL("email"),

    /**
     * Native Data Souce
     * 
     */
    @XmlEnumValue("native")
    NATIVE("native"),

    /**
     * Legacy Document Format Data
     * 						Source
     * 
     */
    @XmlEnumValue("legacy")
    LEGACY("legacy"),

    /**
     * Aggregate Data Source
     * 
     */
    @XmlEnumValue("master")
    MASTER("master");
    private final String value;

    STMailMergeSourceType(String v) {
        value = v;
    }

    public String value() {
        return value;
    }

    public static STMailMergeSourceType fromValue(String v) {
        for (STMailMergeSourceType c: STMailMergeSourceType.values()) {
            if (c.value.equals(v)) {
                return c;
            }
        }
        throw new IllegalArgumentException(v);
    }

}
