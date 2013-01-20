
package org.xlsx4j.sml;

import javax.xml.bind.annotation.XmlEnum;
import javax.xml.bind.annotation.XmlEnumValue;
import javax.xml.bind.annotation.XmlType;


/**
 * <p>Java class for ST_CfvoType.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
 * <p>
 * <pre>
 * &lt;simpleType name="ST_CfvoType">
 *   &lt;restriction base="{http://www.w3.org/2001/XMLSchema}string">
 *     &lt;enumeration value="num"/>
 *     &lt;enumeration value="percent"/>
 *     &lt;enumeration value="max"/>
 *     &lt;enumeration value="min"/>
 *     &lt;enumeration value="formula"/>
 *     &lt;enumeration value="percentile"/>
 *   &lt;/restriction>
 * &lt;/simpleType>
 * </pre>
 * 
 */
@XmlType(name = "ST_CfvoType")
@XmlEnum
public enum STCfvoType {

    @XmlEnumValue("num")
    NUM("num"),
    @XmlEnumValue("percent")
    PERCENT("percent"),
    @XmlEnumValue("max")
    MAX("max"),
    @XmlEnumValue("min")
    MIN("min"),
    @XmlEnumValue("formula")
    FORMULA("formula"),
    @XmlEnumValue("percentile")
    PERCENTILE("percentile");
    private final String value;

    STCfvoType(String v) {
        value = v;
    }

    public String value() {
        return value;
    }

    public static STCfvoType fromValue(String v) {
        for (STCfvoType c: STCfvoType.values()) {
            if (c.value.equals(v)) {
                return c;
            }
        }
        throw new IllegalArgumentException(v);
    }

}
