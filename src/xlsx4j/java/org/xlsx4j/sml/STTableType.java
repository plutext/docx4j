
package org.xlsx4j.sml;

import javax.xml.bind.annotation.XmlEnum;
import javax.xml.bind.annotation.XmlEnumValue;
import javax.xml.bind.annotation.XmlType;


/**
 * <p>Java class for ST_TableType.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
 * <p>
 * <pre>
 * &lt;simpleType name="ST_TableType">
 *   &lt;restriction base="{http://www.w3.org/2001/XMLSchema}string">
 *     &lt;enumeration value="worksheet"/>
 *     &lt;enumeration value="xml"/>
 *     &lt;enumeration value="queryTable"/>
 *   &lt;/restriction>
 * &lt;/simpleType>
 * </pre>
 * 
 */
@XmlType(name = "ST_TableType")
@XmlEnum
public enum STTableType {

    @XmlEnumValue("worksheet")
    WORKSHEET("worksheet"),
    @XmlEnumValue("xml")
    XML("xml"),
    @XmlEnumValue("queryTable")
    QUERY_TABLE("queryTable");
    private final String value;

    STTableType(String v) {
        value = v;
    }

    public String value() {
        return value;
    }

    public static STTableType fromValue(String v) {
        for (STTableType c: STTableType.values()) {
            if (c.value.equals(v)) {
                return c;
            }
        }
        throw new IllegalArgumentException(v);
    }

}
