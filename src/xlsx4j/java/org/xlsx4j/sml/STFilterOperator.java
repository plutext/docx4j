
package org.xlsx4j.sml;

import javax.xml.bind.annotation.XmlEnum;
import javax.xml.bind.annotation.XmlEnumValue;
import javax.xml.bind.annotation.XmlType;


/**
 * <p>Java class for ST_FilterOperator.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
 * <p>
 * <pre>
 * &lt;simpleType name="ST_FilterOperator">
 *   &lt;restriction base="{http://www.w3.org/2001/XMLSchema}string">
 *     &lt;enumeration value="equal"/>
 *     &lt;enumeration value="lessThan"/>
 *     &lt;enumeration value="lessThanOrEqual"/>
 *     &lt;enumeration value="notEqual"/>
 *     &lt;enumeration value="greaterThanOrEqual"/>
 *     &lt;enumeration value="greaterThan"/>
 *   &lt;/restriction>
 * &lt;/simpleType>
 * </pre>
 * 
 */
@XmlType(name = "ST_FilterOperator")
@XmlEnum
public enum STFilterOperator {

    @XmlEnumValue("equal")
    EQUAL("equal"),
    @XmlEnumValue("lessThan")
    LESS_THAN("lessThan"),
    @XmlEnumValue("lessThanOrEqual")
    LESS_THAN_OR_EQUAL("lessThanOrEqual"),
    @XmlEnumValue("notEqual")
    NOT_EQUAL("notEqual"),
    @XmlEnumValue("greaterThanOrEqual")
    GREATER_THAN_OR_EQUAL("greaterThanOrEqual"),
    @XmlEnumValue("greaterThan")
    GREATER_THAN("greaterThan");
    private final String value;

    STFilterOperator(String v) {
        value = v;
    }

    public String value() {
        return value;
    }

    public static STFilterOperator fromValue(String v) {
        for (STFilterOperator c: STFilterOperator.values()) {
            if (c.value.equals(v)) {
                return c;
            }
        }
        throw new IllegalArgumentException(v);
    }

}
