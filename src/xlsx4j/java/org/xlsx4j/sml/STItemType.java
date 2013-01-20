
package org.xlsx4j.sml;

import javax.xml.bind.annotation.XmlEnum;
import javax.xml.bind.annotation.XmlEnumValue;
import javax.xml.bind.annotation.XmlType;


/**
 * <p>Java class for ST_ItemType.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
 * <p>
 * <pre>
 * &lt;simpleType name="ST_ItemType">
 *   &lt;restriction base="{http://www.w3.org/2001/XMLSchema}string">
 *     &lt;enumeration value="data"/>
 *     &lt;enumeration value="default"/>
 *     &lt;enumeration value="sum"/>
 *     &lt;enumeration value="countA"/>
 *     &lt;enumeration value="avg"/>
 *     &lt;enumeration value="max"/>
 *     &lt;enumeration value="min"/>
 *     &lt;enumeration value="product"/>
 *     &lt;enumeration value="count"/>
 *     &lt;enumeration value="stdDev"/>
 *     &lt;enumeration value="stdDevP"/>
 *     &lt;enumeration value="var"/>
 *     &lt;enumeration value="varP"/>
 *     &lt;enumeration value="grand"/>
 *     &lt;enumeration value="blank"/>
 *   &lt;/restriction>
 * &lt;/simpleType>
 * </pre>
 * 
 */
@XmlType(name = "ST_ItemType")
@XmlEnum
public enum STItemType {

    @XmlEnumValue("data")
    DATA("data"),
    @XmlEnumValue("default")
    DEFAULT("default"),
    @XmlEnumValue("sum")
    SUM("sum"),
    @XmlEnumValue("countA")
    COUNT_A("countA"),
    @XmlEnumValue("avg")
    AVG("avg"),
    @XmlEnumValue("max")
    MAX("max"),
    @XmlEnumValue("min")
    MIN("min"),
    @XmlEnumValue("product")
    PRODUCT("product"),
    @XmlEnumValue("count")
    COUNT("count"),
    @XmlEnumValue("stdDev")
    STD_DEV("stdDev"),
    @XmlEnumValue("stdDevP")
    STD_DEV_P("stdDevP"),
    @XmlEnumValue("var")
    VAR("var"),
    @XmlEnumValue("varP")
    VAR_P("varP"),
    @XmlEnumValue("grand")
    GRAND("grand"),
    @XmlEnumValue("blank")
    BLANK("blank");
    private final String value;

    STItemType(String v) {
        value = v;
    }

    public String value() {
        return value;
    }

    public static STItemType fromValue(String v) {
        for (STItemType c: STItemType.values()) {
            if (c.value.equals(v)) {
                return c;
            }
        }
        throw new IllegalArgumentException(v);
    }

}
