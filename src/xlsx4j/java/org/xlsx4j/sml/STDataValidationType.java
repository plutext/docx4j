
package org.xlsx4j.sml;

import javax.xml.bind.annotation.XmlEnum;
import javax.xml.bind.annotation.XmlEnumValue;
import javax.xml.bind.annotation.XmlType;


/**
 * <p>Java class for ST_DataValidationType.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
 * <p>
 * <pre>
 * &lt;simpleType name="ST_DataValidationType">
 *   &lt;restriction base="{http://www.w3.org/2001/XMLSchema}string">
 *     &lt;enumeration value="none"/>
 *     &lt;enumeration value="whole"/>
 *     &lt;enumeration value="decimal"/>
 *     &lt;enumeration value="list"/>
 *     &lt;enumeration value="date"/>
 *     &lt;enumeration value="time"/>
 *     &lt;enumeration value="textLength"/>
 *     &lt;enumeration value="custom"/>
 *   &lt;/restriction>
 * &lt;/simpleType>
 * </pre>
 * 
 */
@XmlType(name = "ST_DataValidationType")
@XmlEnum
public enum STDataValidationType {

    @XmlEnumValue("none")
    NONE("none"),
    @XmlEnumValue("whole")
    WHOLE("whole"),
    @XmlEnumValue("decimal")
    DECIMAL("decimal"),
    @XmlEnumValue("list")
    LIST("list"),
    @XmlEnumValue("date")
    DATE("date"),
    @XmlEnumValue("time")
    TIME("time"),
    @XmlEnumValue("textLength")
    TEXT_LENGTH("textLength"),
    @XmlEnumValue("custom")
    CUSTOM("custom");
    private final String value;

    STDataValidationType(String v) {
        value = v;
    }

    public String value() {
        return value;
    }

    public static STDataValidationType fromValue(String v) {
        for (STDataValidationType c: STDataValidationType.values()) {
            if (c.value.equals(v)) {
                return c;
            }
        }
        throw new IllegalArgumentException(v);
    }

}
