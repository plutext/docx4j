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
 * <p>Java class for ST_MailMergeDataType.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
 * <p>
 * <pre>
 * &lt;simpleType name="ST_MailMergeDataType">
 *   &lt;restriction base="{http://www.w3.org/2001/XMLSchema}string">
 *     &lt;enumeration value="textFile"/>
 *     &lt;enumeration value="database"/>
 *     &lt;enumeration value="spreadsheet"/>
 *     &lt;enumeration value="query"/>
 *     &lt;enumeration value="odbc"/>
 *     &lt;enumeration value="native"/>
 *   &lt;/restriction>
 * &lt;/simpleType>
 * </pre>
 * 
 */
@XmlType(name = "ST_MailMergeDataType")
@XmlEnum
public enum STMailMergeDataType {


    /**
     * Text File Data Source
     * 
     */
    @XmlEnumValue("textFile")
    TEXT_FILE("textFile"),

    /**
     * Database Data Source
     * 
     */
    @XmlEnumValue("database")
    DATABASE("database"),

    /**
     * Spreadsheet Data Source
     * 
     */
    @XmlEnumValue("spreadsheet")
    SPREADSHEET("spreadsheet"),

    /**
     * Query Data Source
     * 
     */
    @XmlEnumValue("query")
    QUERY("query"),

    /**
     * Open Database Connectivity Data
     * 						Source
     * 
     */
    @XmlEnumValue("odbc")
    ODBC("odbc"),

    /**
     * Office Data Source Object Data
     * 						Source
     * 
     */
    @XmlEnumValue("native")
    NATIVE("native");
    private final String value;

    STMailMergeDataType(String v) {
        value = v;
    }

    public String value() {
        return value;
    }

    public static STMailMergeDataType fromValue(String v) {
        for (STMailMergeDataType c: STMailMergeDataType.values()) {
            if (c.value.equals(v)) {
                return c;
            }
        }
        throw new IllegalArgumentException(v);
    }

}
