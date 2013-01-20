
package org.xlsx4j.sml;

import javax.xml.bind.annotation.XmlEnum;
import javax.xml.bind.annotation.XmlEnumValue;
import javax.xml.bind.annotation.XmlType;


/**
 * <p>Java class for ST_DataConsolidateFunction.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
 * <p>
 * <pre>
 * &lt;simpleType name="ST_DataConsolidateFunction">
 *   &lt;restriction base="{http://www.w3.org/2001/XMLSchema}string">
 *     &lt;enumeration value="average"/>
 *     &lt;enumeration value="count"/>
 *     &lt;enumeration value="countNums"/>
 *     &lt;enumeration value="max"/>
 *     &lt;enumeration value="min"/>
 *     &lt;enumeration value="product"/>
 *     &lt;enumeration value="stdDev"/>
 *     &lt;enumeration value="stdDevp"/>
 *     &lt;enumeration value="sum"/>
 *     &lt;enumeration value="var"/>
 *     &lt;enumeration value="varp"/>
 *   &lt;/restriction>
 * &lt;/simpleType>
 * </pre>
 * 
 */
@XmlType(name = "ST_DataConsolidateFunction")
@XmlEnum
public enum STDataConsolidateFunction {

    @XmlEnumValue("average")
    AVERAGE("average"),
    @XmlEnumValue("count")
    COUNT("count"),
    @XmlEnumValue("countNums")
    COUNT_NUMS("countNums"),
    @XmlEnumValue("max")
    MAX("max"),
    @XmlEnumValue("min")
    MIN("min"),
    @XmlEnumValue("product")
    PRODUCT("product"),
    @XmlEnumValue("stdDev")
    STD_DEV("stdDev"),
    @XmlEnumValue("stdDevp")
    STD_DEVP("stdDevp"),
    @XmlEnumValue("sum")
    SUM("sum"),
    @XmlEnumValue("var")
    VAR("var"),
    @XmlEnumValue("varp")
    VARP("varp");
    private final String value;

    STDataConsolidateFunction(String v) {
        value = v;
    }

    public String value() {
        return value;
    }

    public static STDataConsolidateFunction fromValue(String v) {
        for (STDataConsolidateFunction c: STDataConsolidateFunction.values()) {
            if (c.value.equals(v)) {
                return c;
            }
        }
        throw new IllegalArgumentException(v);
    }

}
