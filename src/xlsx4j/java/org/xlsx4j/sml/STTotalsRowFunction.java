
package org.xlsx4j.sml;

import javax.xml.bind.annotation.XmlEnum;
import javax.xml.bind.annotation.XmlEnumValue;
import javax.xml.bind.annotation.XmlType;


/**
 * <p>Java class for ST_TotalsRowFunction.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
 * <p>
 * <pre>
 * &lt;simpleType name="ST_TotalsRowFunction">
 *   &lt;restriction base="{http://www.w3.org/2001/XMLSchema}string">
 *     &lt;enumeration value="none"/>
 *     &lt;enumeration value="sum"/>
 *     &lt;enumeration value="min"/>
 *     &lt;enumeration value="max"/>
 *     &lt;enumeration value="average"/>
 *     &lt;enumeration value="count"/>
 *     &lt;enumeration value="countNums"/>
 *     &lt;enumeration value="stdDev"/>
 *     &lt;enumeration value="var"/>
 *     &lt;enumeration value="custom"/>
 *   &lt;/restriction>
 * &lt;/simpleType>
 * </pre>
 * 
 */
@XmlType(name = "ST_TotalsRowFunction")
@XmlEnum
public enum STTotalsRowFunction {

    @XmlEnumValue("none")
    NONE("none"),
    @XmlEnumValue("sum")
    SUM("sum"),
    @XmlEnumValue("min")
    MIN("min"),
    @XmlEnumValue("max")
    MAX("max"),
    @XmlEnumValue("average")
    AVERAGE("average"),
    @XmlEnumValue("count")
    COUNT("count"),
    @XmlEnumValue("countNums")
    COUNT_NUMS("countNums"),
    @XmlEnumValue("stdDev")
    STD_DEV("stdDev"),
    @XmlEnumValue("var")
    VAR("var"),
    @XmlEnumValue("custom")
    CUSTOM("custom");
    private final String value;

    STTotalsRowFunction(String v) {
        value = v;
    }

    public String value() {
        return value;
    }

    public static STTotalsRowFunction fromValue(String v) {
        for (STTotalsRowFunction c: STTotalsRowFunction.values()) {
            if (c.value.equals(v)) {
                return c;
            }
        }
        throw new IllegalArgumentException(v);
    }

}
