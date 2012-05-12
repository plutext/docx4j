/*
 *  Copyright 2010, Plutext Pty Ltd.
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


package org.xlsx4j.sml;

import javax.xml.bind.annotation.XmlEnum;
import javax.xml.bind.annotation.XmlEnumValue;
import javax.xml.bind.annotation.XmlType;


/**
 * <p>Java class for ST_XmlDataType.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
 * <p>
 * <pre>
 * &lt;simpleType name="ST_XmlDataType">
 *   &lt;restriction base="{http://www.w3.org/2001/XMLSchema}string">
 *     &lt;enumeration value="string"/>
 *     &lt;enumeration value="normalizedString"/>
 *     &lt;enumeration value="token"/>
 *     &lt;enumeration value="byte"/>
 *     &lt;enumeration value="unsignedByte"/>
 *     &lt;enumeration value="base64Binary"/>
 *     &lt;enumeration value="hexBinary"/>
 *     &lt;enumeration value="integer"/>
 *     &lt;enumeration value="positiveInteger"/>
 *     &lt;enumeration value="negativeInteger"/>
 *     &lt;enumeration value="nonPositiveInteger"/>
 *     &lt;enumeration value="nonNegativeInteger"/>
 *     &lt;enumeration value="int"/>
 *     &lt;enumeration value="unsignedInt"/>
 *     &lt;enumeration value="long"/>
 *     &lt;enumeration value="unsignedLong"/>
 *     &lt;enumeration value="short"/>
 *     &lt;enumeration value="unsignedShort"/>
 *     &lt;enumeration value="decimal"/>
 *     &lt;enumeration value="float"/>
 *     &lt;enumeration value="double"/>
 *     &lt;enumeration value="boolean"/>
 *     &lt;enumeration value="time"/>
 *     &lt;enumeration value="dateTime"/>
 *     &lt;enumeration value="duration"/>
 *     &lt;enumeration value="date"/>
 *     &lt;enumeration value="gMonth"/>
 *     &lt;enumeration value="gYear"/>
 *     &lt;enumeration value="gYearMonth"/>
 *     &lt;enumeration value="gDay"/>
 *     &lt;enumeration value="gMonthDay"/>
 *     &lt;enumeration value="Name"/>
 *     &lt;enumeration value="QName"/>
 *     &lt;enumeration value="NCName"/>
 *     &lt;enumeration value="anyURI"/>
 *     &lt;enumeration value="language"/>
 *     &lt;enumeration value="ID"/>
 *     &lt;enumeration value="IDREF"/>
 *     &lt;enumeration value="IDREFS"/>
 *     &lt;enumeration value="ENTITY"/>
 *     &lt;enumeration value="ENTITIES"/>
 *     &lt;enumeration value="NOTATION"/>
 *     &lt;enumeration value="NMTOKEN"/>
 *     &lt;enumeration value="NMTOKENS"/>
 *     &lt;enumeration value="anyType"/>
 *   &lt;/restriction>
 * &lt;/simpleType>
 * </pre>
 * 
 */
@XmlType(name = "ST_XmlDataType")
@XmlEnum
public enum STXmlDataType {


    /**
     * String
     * 
     */
    @XmlEnumValue("string")
    STRING("string"),

    /**
     * Normalized String
     * 
     */
    @XmlEnumValue("normalizedString")
    NORMALIZED_STRING("normalizedString"),

    /**
     * Token
     * 
     */
    @XmlEnumValue("token")
    TOKEN("token"),

    /**
     * Byte
     * 
     */
    @XmlEnumValue("byte")
    BYTE("byte"),

    /**
     * Unsigned Byte
     * 
     */
    @XmlEnumValue("unsignedByte")
    UNSIGNED_BYTE("unsignedByte"),

    /**
     * Base 64 Encoded Binary
     * 
     */
    @XmlEnumValue("base64Binary")
    BASE_64_BINARY("base64Binary"),

    /**
     * Hex Binary
     * 
     */
    @XmlEnumValue("hexBinary")
    HEX_BINARY("hexBinary"),

    /**
     * Integer
     * 
     */
    @XmlEnumValue("integer")
    INTEGER("integer"),

    /**
     * Positive Integer
     * 
     */
    @XmlEnumValue("positiveInteger")
    POSITIVE_INTEGER("positiveInteger"),

    /**
     * Negative Integer
     * 
     */
    @XmlEnumValue("negativeInteger")
    NEGATIVE_INTEGER("negativeInteger"),

    /**
     * Non Positive Integer
     * 
     */
    @XmlEnumValue("nonPositiveInteger")
    NON_POSITIVE_INTEGER("nonPositiveInteger"),

    /**
     * Non Negative Integer
     * 
     */
    @XmlEnumValue("nonNegativeInteger")
    NON_NEGATIVE_INTEGER("nonNegativeInteger"),

    /**
     * Integer
     * 
     */
    @XmlEnumValue("int")
    INT("int"),

    /**
     * Unsigned Integer
     * 
     */
    @XmlEnumValue("unsignedInt")
    UNSIGNED_INT("unsignedInt"),

    /**
     * Long
     * 
     */
    @XmlEnumValue("long")
    LONG("long"),

    /**
     * Unsigned Long
     * 
     */
    @XmlEnumValue("unsignedLong")
    UNSIGNED_LONG("unsignedLong"),

    /**
     * Short
     * 
     */
    @XmlEnumValue("short")
    SHORT("short"),

    /**
     * Unsigned Short
     * 
     */
    @XmlEnumValue("unsignedShort")
    UNSIGNED_SHORT("unsignedShort"),

    /**
     * Decimal
     * 
     */
    @XmlEnumValue("decimal")
    DECIMAL("decimal"),

    /**
     * Float
     * 
     */
    @XmlEnumValue("float")
    FLOAT("float"),

    /**
     * Double
     * 
     */
    @XmlEnumValue("double")
    DOUBLE("double"),

    /**
     * Boolean
     * 
     */
    @XmlEnumValue("boolean")
    BOOLEAN("boolean"),

    /**
     * Time
     * 
     */
    @XmlEnumValue("time")
    TIME("time"),

    /**
     * Date Time
     * 
     */
    @XmlEnumValue("dateTime")
    DATE_TIME("dateTime"),

    /**
     * Duration
     * 
     */
    @XmlEnumValue("duration")
    DURATION("duration"),

    /**
     * Date
     * 
     */
    @XmlEnumValue("date")
    DATE("date"),

    /**
     * gMonth
     * 
     */
    @XmlEnumValue("gMonth")
    G_MONTH("gMonth"),

    /**
     * gYear
     * 
     */
    @XmlEnumValue("gYear")
    G_YEAR("gYear"),

    /**
     * gYearMonth
     * 
     */
    @XmlEnumValue("gYearMonth")
    G_YEAR_MONTH("gYearMonth"),

    /**
     * gDay
     * 
     */
    @XmlEnumValue("gDay")
    G_DAY("gDay"),

    /**
     * gMonthDays
     * 
     */
    @XmlEnumValue("gMonthDay")
    G_MONTH_DAY("gMonthDay"),

    /**
     * Name
     * 
     */
    @XmlEnumValue("Name")
    NAME("Name"),

    /**
     * Qname
     * 
     */
    @XmlEnumValue("QName")
    Q_NAME("QName"),

    /**
     * NCName
     * 
     */
    @XmlEnumValue("NCName")
    NC_NAME("NCName"),

    /**
     * Any URI
     * 
     */
    @XmlEnumValue("anyURI")
    ANY_URI("anyURI"),

    /**
     * Language
     * 
     */
    @XmlEnumValue("language")
    LANGUAGE("language"),

    /**
     * ID
     * 
     */
    ID("ID"),

    /**
     * IDREF
     * 
     */
    IDREF("IDREF"),

    /**
     * IDREFS
     * 
     */
    IDREFS("IDREFS"),

    /**
     * ENTITY
     * 
     */
    ENTITY("ENTITY"),

    /**
     * ENTITIES
     * 
     */
    ENTITIES("ENTITIES"),

    /**
     * Notation
     * 
     */
    NOTATION("NOTATION"),

    /**
     * NMTOKEN
     * 
     */
    NMTOKEN("NMTOKEN"),

    /**
     * NMTOKENS
     * 
     */
    NMTOKENS("NMTOKENS"),

    /**
     * Any Type
     * 
     */
    @XmlEnumValue("anyType")
    ANY_TYPE("anyType");
    private final String value;

    STXmlDataType(String v) {
        value = v;
    }

    public String value() {
        return value;
    }

    public static STXmlDataType fromValue(String v) {
        for (STXmlDataType c: STXmlDataType.values()) {
            if (c.value.equals(v)) {
                return c;
            }
        }
        throw new IllegalArgumentException(v);
    }

}
