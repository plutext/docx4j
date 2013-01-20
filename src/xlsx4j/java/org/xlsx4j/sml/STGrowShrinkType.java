
package org.xlsx4j.sml;

import javax.xml.bind.annotation.XmlEnum;
import javax.xml.bind.annotation.XmlEnumValue;
import javax.xml.bind.annotation.XmlType;


/**
 * <p>Java class for ST_GrowShrinkType.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
 * <p>
 * <pre>
 * &lt;simpleType name="ST_GrowShrinkType">
 *   &lt;restriction base="{http://www.w3.org/2001/XMLSchema}string">
 *     &lt;enumeration value="insertDelete"/>
 *     &lt;enumeration value="insertClear"/>
 *     &lt;enumeration value="overwriteClear"/>
 *   &lt;/restriction>
 * &lt;/simpleType>
 * </pre>
 * 
 */
@XmlType(name = "ST_GrowShrinkType")
@XmlEnum
public enum STGrowShrinkType {

    @XmlEnumValue("insertDelete")
    INSERT_DELETE("insertDelete"),
    @XmlEnumValue("insertClear")
    INSERT_CLEAR("insertClear"),
    @XmlEnumValue("overwriteClear")
    OVERWRITE_CLEAR("overwriteClear");
    private final String value;

    STGrowShrinkType(String v) {
        value = v;
    }

    public String value() {
        return value;
    }

    public static STGrowShrinkType fromValue(String v) {
        for (STGrowShrinkType c: STGrowShrinkType.values()) {
            if (c.value.equals(v)) {
                return c;
            }
        }
        throw new IllegalArgumentException(v);
    }

}
