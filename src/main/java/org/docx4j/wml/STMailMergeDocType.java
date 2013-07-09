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
 * <p>Java class for ST_MailMergeDocType.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
 * <p>
 * <pre>
 * &lt;simpleType name="ST_MailMergeDocType">
 *   &lt;restriction base="{http://www.w3.org/2001/XMLSchema}string">
 *     &lt;enumeration value="catalog"/>
 *     &lt;enumeration value="envelopes"/>
 *     &lt;enumeration value="mailingLabels"/>
 *     &lt;enumeration value="formLetters"/>
 *     &lt;enumeration value="email"/>
 *     &lt;enumeration value="fax"/>
 *   &lt;/restriction>
 * &lt;/simpleType>
 * </pre>
 * 
 */
@XmlType(name = "ST_MailMergeDocType")
@XmlEnum
public enum STMailMergeDocType {


    /**
     * Catalog Source Document
     * 
     */
    @XmlEnumValue("catalog")
    CATALOG("catalog"),

    /**
     * Envelope Source
     * 						Document
     * 
     */
    @XmlEnumValue("envelopes")
    ENVELOPES("envelopes"),

    /**
     * Mailing Label Source
     * 						Document
     * 
     */
    @XmlEnumValue("mailingLabels")
    MAILING_LABELS("mailingLabels"),

    /**
     * Form Letter Source
     * 						Document
     * 
     */
    @XmlEnumValue("formLetters")
    FORM_LETTERS("formLetters"),

    /**
     * E-Mail Source Document
     * 
     */
    @XmlEnumValue("email")
    EMAIL("email"),

    /**
     * Fax Source Document
     * 
     */
    @XmlEnumValue("fax")
    FAX("fax");
    private final String value;

    STMailMergeDocType(String v) {
        value = v;
    }

    public String value() {
        return value;
    }

    public static STMailMergeDocType fromValue(String v) {
        for (STMailMergeDocType c: STMailMergeDocType.values()) {
            if (c.value.equals(v)) {
                return c;
            }
        }
        throw new IllegalArgumentException(v);
    }

}
