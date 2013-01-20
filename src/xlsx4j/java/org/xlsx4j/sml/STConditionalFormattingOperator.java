
package org.xlsx4j.sml;

import javax.xml.bind.annotation.XmlEnum;
import javax.xml.bind.annotation.XmlEnumValue;
import javax.xml.bind.annotation.XmlType;


/**
 * <p>Java class for ST_ConditionalFormattingOperator.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
 * <p>
 * <pre>
 * &lt;simpleType name="ST_ConditionalFormattingOperator">
 *   &lt;restriction base="{http://www.w3.org/2001/XMLSchema}string">
 *     &lt;enumeration value="lessThan"/>
 *     &lt;enumeration value="lessThanOrEqual"/>
 *     &lt;enumeration value="equal"/>
 *     &lt;enumeration value="notEqual"/>
 *     &lt;enumeration value="greaterThanOrEqual"/>
 *     &lt;enumeration value="greaterThan"/>
 *     &lt;enumeration value="between"/>
 *     &lt;enumeration value="notBetween"/>
 *     &lt;enumeration value="containsText"/>
 *     &lt;enumeration value="notContains"/>
 *     &lt;enumeration value="beginsWith"/>
 *     &lt;enumeration value="endsWith"/>
 *   &lt;/restriction>
 * &lt;/simpleType>
 * </pre>
 * 
 */
@XmlType(name = "ST_ConditionalFormattingOperator")
@XmlEnum
public enum STConditionalFormattingOperator {

    @XmlEnumValue("lessThan")
    LESS_THAN("lessThan"),
    @XmlEnumValue("lessThanOrEqual")
    LESS_THAN_OR_EQUAL("lessThanOrEqual"),
    @XmlEnumValue("equal")
    EQUAL("equal"),
    @XmlEnumValue("notEqual")
    NOT_EQUAL("notEqual"),
    @XmlEnumValue("greaterThanOrEqual")
    GREATER_THAN_OR_EQUAL("greaterThanOrEqual"),
    @XmlEnumValue("greaterThan")
    GREATER_THAN("greaterThan"),
    @XmlEnumValue("between")
    BETWEEN("between"),
    @XmlEnumValue("notBetween")
    NOT_BETWEEN("notBetween"),
    @XmlEnumValue("containsText")
    CONTAINS_TEXT("containsText"),
    @XmlEnumValue("notContains")
    NOT_CONTAINS("notContains"),
    @XmlEnumValue("beginsWith")
    BEGINS_WITH("beginsWith"),
    @XmlEnumValue("endsWith")
    ENDS_WITH("endsWith");
    private final String value;

    STConditionalFormattingOperator(String v) {
        value = v;
    }

    public String value() {
        return value;
    }

    public static STConditionalFormattingOperator fromValue(String v) {
        for (STConditionalFormattingOperator c: STConditionalFormattingOperator.values()) {
            if (c.value.equals(v)) {
                return c;
            }
        }
        throw new IllegalArgumentException(v);
    }

}
